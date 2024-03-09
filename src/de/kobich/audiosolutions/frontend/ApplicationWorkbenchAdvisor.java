package de.kobich.audiosolutions.frontend;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.services.ISourceProviderService;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.frontend.common.FileDescriptorSourceProvider;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.preferences.GeneralPreferencePage;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.StatusLineEventListener;
import de.kobich.audiosolutions.frontend.perspective.AudioPerspective;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.commons.ui.jface.sleak.Sleak;

/**
 * This workbench advisor creates the window advisor, and specifies the perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	private static final Logger logger = Logger.getLogger(ApplicationWorkbenchAdvisor.class);
	
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return AudioPerspective.ID;
	}

	@Override
	public String getMainPreferencePageId() {
		return GeneralPreferencePage.ID;
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		configurer.setSaveAndRestore(true);
		
		// register source provider
		configurer.getWorkbench().addWindowListener(WindowListener.INSTANCE);
	}
	
	public void postStartup() {
		boolean debug = Boolean.parseBoolean(System.getProperty(AudioSolutions.UI_DEBUG_PROP));
		if (debug) {
			Sleak sleak = new Sleak();
			sleak.open(true);
		}
		
		JFaceThreadRunner runner = new JFaceThreadRunner("Initialize Application", getWorkbenchConfigurer().getWorkbench().getActiveWorkbenchWindow().getShell(), Arrays.asList(RunningState.WORKER_1)) {
			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
					case WORKER_1:
						AudioSolutions.initSpringContext();
						break;
					case UI_ERROR:
						Exception e = super.getException();
						logger.error(e.getMessage(), e);
						MessageDialog.openError(null, super.getName(), "Application initialization failed: \n" + e.getMessage());
						break;
					default: 
						break;
				}
			}
			
		};
		runner.runBackgroundJob(0, false, true, null);
	}

	@Override
	public boolean preShutdown() {
//		logger.info("Destroy all running processes");
//		ServiceFactory serviceFactory = ServiceFactory.getInstance();
//		final ExecutionProcessRegistry executionProcessRegistry = (ExecutionProcessRegistry) serviceFactory.getService("executionProcessRegistry");
//
//		// check if external processes are still running
//		if (executionProcessRegistry.hasRunningProcesses()) {
//			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//			String dialogBoxTitle = "AudioSolutions";
//			String message = "AudioSolutions may not be able to destroy all sub processes. \n" +
//					"Please destroy them manually if necessary. \n" +
//					"Are you sure you want to close AudioSolutions?";
//			boolean shutdown = MessageDialog.openQuestion(shell, dialogBoxTitle, message);
//			if (shutdown) {
//				Runnable runnable = new Runnable() { 
//					public void run() { 
//						executionProcessRegistry.destroyProcesses();
//					} 
//				}; 
//				Runtime.getRuntime().addShutdownHook(new Thread(runnable));
//			}
//			return shutdown;
//		}
		return true;
	}
	
	private static class WindowListener implements IWindowListener {
		public static WindowListener INSTANCE = new WindowListener();
		
		private WindowListener() {}
		
		@Override
		public void windowOpened(IWorkbenchWindow window) {
			window.addPageListener(PageListener.INSTANCE);
			
			// SelectionSupport
			window.getActivePage().addPartListener(SelectionSupport.INSTANCE);
			// SourceProvider
			ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);
			FileDescriptorSourceProvider sourceProvider = (FileDescriptorSourceProvider) sourceProviderService.getSourceProvider(FileDescriptorSourceProvider.FILE_DESCRIPTOR_SELECTION);
			window.getActivePage().addPartListener(sourceProvider);
			SelectionSupport.INSTANCE.addSelectionChangedListener(sourceProvider);
			EventSupport.INSTANCE.addListener(sourceProvider);
			// StatusLineEventListener
			SelectionSupport.INSTANCE.addSelectionChangedListener(StatusLineEventListener.INSTANCE);
		}
		
		@Override
		public void windowClosed(IWorkbenchWindow window) {
			window.removePageListener(PageListener.INSTANCE);

			// SourceProvider
			ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);
			FileDescriptorSourceProvider sourceProvider = (FileDescriptorSourceProvider) sourceProviderService.getSourceProvider(FileDescriptorSourceProvider.FILE_DESCRIPTOR_SELECTION);
			SelectionSupport.INSTANCE.removeSelectionChangedListener(sourceProvider);
			EventSupport.INSTANCE.removeListener(sourceProvider);
			// StatusLineEventListener
			SelectionSupport.INSTANCE.removeSelectionChangedListener(StatusLineEventListener.INSTANCE);
		}
		
		@Override
		public void windowDeactivated(IWorkbenchWindow window) {
		}
		
		@Override
		public void windowActivated(IWorkbenchWindow window) {
		}
	}
	
	private static class PageListener implements IPageListener {
		public static PageListener INSTANCE = new PageListener();
		
		private PageListener() {}

		@Override
		public void pageOpened(IWorkbenchPage page) {
		}
		
		@Override
		public void pageClosed(IWorkbenchPage page) {
			// SelectionSupport
			page.removePartListener(SelectionSupport.INSTANCE);
			// SourceProvider
			ISourceProviderService sourceProviderService = (ISourceProviderService) page.getWorkbenchWindow().getService(ISourceProviderService.class);
			FileDescriptorSourceProvider sourceProvider = (FileDescriptorSourceProvider) sourceProviderService.getSourceProvider(FileDescriptorSourceProvider.FILE_DESCRIPTOR_SELECTION);
			page.removePartListener(sourceProvider);
		}

		@Override
		public void pageActivated(IWorkbenchPage page) {
		}
		
	}
}
