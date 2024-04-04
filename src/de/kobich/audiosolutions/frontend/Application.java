package de.kobich.audiosolutions.frontend;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.Policy;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.AudioSolutionsStatus;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

/**
 * This class controls all aspects of the application's execution
 */
@SuppressWarnings("restriction")
public class Application implements IApplication {
	private static final Logger logger = Logger.getLogger(Application.class);

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		Location instanceLoc = Platform.getInstanceLocation();
		try {
			// debug settings
//			System.setProperty(AudioSolutions.UI_DEBUG_PROP, Boolean.TRUE.toString());
//			System.setProperty(AudioSolutions.DB_DEBUG_PROP, Boolean.TRUE.toString());
			boolean debug = Boolean.parseBoolean(System.getProperty(AudioSolutions.UI_DEBUG_PROP));
			if (debug) {
				Policy.DEBUG_SWT_GRAPHICS = true;
			}

			// set data directory if not set
			if (!instanceLoc.isSet()) {
				String asolRootDir = System.getProperty("user.home") + File.separator + ".audiosolutions";
				URL url = new File(asolRootDir).toURI().toURL();
				instanceLoc.set(url, false);
			}
			final File dataRootDirectory = new File(instanceLoc.getURL().toURI());
			
			// check if workspace is locked
			boolean locked = instanceLoc.lock(); 
			if (!locked || !instanceLoc.isSet()) {
				String msg = String.format("The data directory could not be set or is locked: \n%s \n\nAudioSolutions will exit.", dataRootDirectory.getAbsolutePath());
				MessageDialog.openError(null, "Multiple Instances", msg);
				return IApplication.EXIT_OK;
			}
			
			// init AudioSolutions
//			String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
			AudioSolutionsStatus state = AudioSolutions.init(dataRootDirectory);
			switch (state) {
			case LOCKED:
				String lockedMsg = String.format("The data directory is already in use: \n%s \n\nAudioSolutions will exit.", dataRootDirectory.getAbsolutePath());
				MessageDialog.openError(null, "Multiple Instances", lockedMsg);
				return IApplication.EXIT_OK;
			case NOT_WRITABLE:
				String notWritableMsg = String.format("The data directory is not writable: \n%s \n\nAudioSolutions will exit.", dataRootDirectory.getAbsolutePath());
				MessageDialog.openError(null, "Data Directory", notWritableMsg);
				return IApplication.EXIT_OK;
			case VERSION_MISMATCH:
				final String versionFromFile = AudioSolutions.getCurrentVersion().orElse("<Unknown>");
				String migrationMsg = String.format("The data directory uses a different version: \nAudioSolution Version: %s \nData Directory Version: %s \nData Directory: %s \n\nDo you want to continue?", AudioSolutions.CURRENT_VERSION.getLabel(), versionFromFile, dataRootDirectory.getAbsolutePath());
				boolean startMigration = MessageDialog.openQuestion(null, "Data Directory", migrationMsg);
				if (startMigration) { 
					List<RunningState> states = Arrays.asList(RunningState.WORKER_1);
					JFaceThreadRunner runner = new JFaceThreadRunner("Migrate Database", display.getActiveShell(), states) {
						@Override
						protected void run(RunningState state) throws Exception {
							switch (state) {
							case WORKER_1:
								AudioSolutions.migrate(super.getProgressMonitor());
								break;
							default: 
								break;
							}
						}
					};
					runner.runProgressMonitorDialog(true, false);
				}
				else {
					return IApplication.EXIT_OK;
				}
				break;
			case INITIALIZED:
				break;
			}
			
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			MessageDialog.openError(null, "Startup Error", e.getMessage());
			AudioSolutions.shutdown();
			return IApplication.EXIT_OK;
		}
		finally {
			instanceLoc.release();
			display.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
