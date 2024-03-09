package de.kobich.audiosolutions.frontend.audio.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.cddb.AudioCDDBService;
import de.kobich.audiosolutions.core.service.cddb.ICDDBRelease;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.proxy.RCPProxyProvider;
import de.kobich.commons.net.IProxyProvider;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.StatusLineUtils;
import de.kobich.component.file.FileDescriptor;

public class CDDBWizardDialog extends WizardDialog {
	private final static Logger logger = Logger.getLogger(CDDBWizardDialog.class);
	private CDDBWizard wizard;
	
	public CDDBWizardDialog(Shell parentShell) {
		super(parentShell, new CDDBWizard());
		this.wizard = (CDDBWizard) super.getWizard();
		this.wizard.setDialogBoundsSettings(getDialogBoundsSettings());
		super.addPageChangedListener(this.wizard);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, CDDBWizardDialog.class.getName());
	}

	public void setFileDescriptors(Set<FileDescriptor> fileDescriptors) {
		this.wizard.fileDescriptors = fileDescriptors;
	}
	
	public void setAudioCollectionEditor(AudioCollectionEditor editor) {
		this.wizard.editor = editor;
	}
	
	/**
	 * CDDB wizard
	 *
	 */
	private static class CDDBWizard extends Wizard implements IPageChangedListener {
		private final CDDBLookupWizardPage lookupWizardPage;
		private final CDDBAudioDataWizardPage previewWizardPage;
		private Set<FileDescriptor> fileDescriptors;
		private AudioCollectionEditor editor;
		
		protected CDDBWizard() {
			super.setWindowTitle("Set Audio Data By CDDB");
			this.lookupWizardPage = new CDDBLookupWizardPage();
			this.previewWizardPage = new CDDBAudioDataWizardPage();
		}
		
		protected void setDialogBoundsSettings(IDialogSettings dialogSettings) {
			this.lookupWizardPage.setDialogBoundsSettings(dialogSettings);
		}
		
		@Override
		public void addPages() {
			super.addPage(lookupWizardPage);
			super.addPage(previewWizardPage);
		}

		@Override
		public void pageChanged(PageChangedEvent event) {
			if (event.getSelectedPage() == previewWizardPage) {
				List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
				JFaceThreadRunner runner = new JFaceThreadRunner("Loading release", getShell(), states) {
					private ICDDBRelease release;
					private List<AudioDataChange> changes;

					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case UI_1:
							previewWizardPage.setErrorMessage(null);
							this.release = lookupWizardPage.getSelectedRelease();
							break;
						case WORKER_1:
							AudioCDDBService cddbService = AudioSolutions.getService(AudioCDDBService.class);
							IProxyProvider proxyProvider = new RCPProxyProvider();
							this.changes = cddbService.assignCDDBTracks(CDDBWizard.this.fileDescriptors, release, proxyProvider, super.getProgressMonitor());
							break;
						case UI_2:
							previewWizardPage.initPage(release, this.changes);
							break;
						case UI_ERROR:
							if (!super.getProgressMonitor().isCanceled()) {
								Exception e = super.getException();
								logger.error(e.getMessage(), e);
								previewWizardPage.setErrorMessage(e.getMessage());
							}
							this.changes = new ArrayList<>();
							previewWizardPage.initPage(release, this.changes);
							break;
						default: 
							break;
						}
					}
				};
				runner.runProgressMonitorDialog(true, true);
			}
			else {
				previewWizardPage.resetPage();
			}
		}
	
		@Override
		public boolean performFinish() {
			List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Set Audio Data By CDDB", getShell(), states) {
				private ICDDBRelease release;
				private Set<AudioDataChange> previewChanges;

				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case UI_1:
						this.release = lookupWizardPage.getSelectedRelease();
						this.previewChanges = previewWizardPage.getCDDBTracks();
						if (previewChanges == null) {
							AudioCDDBService cddbService = AudioSolutions.getService(AudioCDDBService.class);
							IProxyProvider proxyProvider = new RCPProxyProvider();
							List<AudioDataChange> list = cddbService.assignCDDBTracks(CDDBWizard.this.fileDescriptors, release, proxyProvider, super.getProgressMonitor());
							previewChanges = new HashSet<>(list);
						}
						break;
					case WORKER_1:
						logger.debug("Release: " + release.getAlbum());
						logger.debug("Files #: " + previewChanges.size());
						AudioDataService audioDataService = AudioSolutions.getService(AudioDataService.class);
//						audioDataService.addAudioData(previewFiles, null);
						audioDataService.applyChanges(previewChanges, super.getProgressMonitor());
						break;
					case UI_2:
						lookupWizardPage.saveState();
						UIEvent event = new UIEvent(ActionType.AUDIO_DATA, editor);
						for (AudioDataChange change : previewChanges) {
							event.getEditorDelta().getUpdateItems().add(change.getFileDescriptor());
						}
						EventSupport.INSTANCE.fireEvent(event);
						StatusLineUtils.setStatusLineMessage(editor, "Audio data set", false);
						break;
					case UI_ERROR:
						Exception e = super.getException();
						logger.error(e.getMessage(), e);
						MessageDialog.openError(getShell(), "Error", "Audio Data could not be set:\n" + e.getMessage());
						break;
					default: 
						break;
					}
				}
			};
			runner.runProgressMonitorDialog(true, true);
			return runner.isSucceeded();
		}
	}

}
