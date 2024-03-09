package de.kobich.audiosolutions.frontend.audio.wizard;

import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.imexport.ExportTemplateType;
import de.kobich.audiosolutions.core.service.imexport.TemplateExportRequest;
import de.kobich.audiosolutions.core.service.imexport.TemplateExportService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.ui.ProgressDialog;
import de.kobich.commons.monitor.progress.ProgressCancelException;
import de.kobich.commons.ui.jface.progress.ProgressMonitorAdapter;
import de.kobich.component.file.FileDescriptor;

public class AudioCollectionTemplateExportWizard extends Wizard implements IExportWizard {
	private static final Logger logger = Logger.getLogger(AudioCollectionTemplateExportWizard.class); 
	private IWorkbenchWindow window;
	private AudioCollectionTemplateExportWizardPage page;
	private boolean canFinish;

	@Override
	public boolean performFinish() {
		try {
			IEditorPart editorPart = window.getActivePage().getActiveEditor();
			if (editorPart instanceof AudioCollectionEditor) {
				AudioCollectionEditor audioCollectionEditor = (AudioCollectionEditor) editorPart;
				Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();

				if (fileDescriptors.isEmpty()) {
					fileDescriptors = audioCollectionEditor.getFileCollection().getFileDescriptors();
//					MessageDialog.openInformation(window.getShell(), "Info", "No files selected");
//					return false;
				}

				ExportTemplateType type = page.getExportTemplateType();
				File templateFile = page.getTemplateFile();
				File targetFile = page.getTargetFile();

				TemplateExportRequest request = new TemplateExportRequest(targetFile, fileDescriptors, type);
				request.setTemplateFile(templateFile);
				ProgressDialog progressDialog = new ProgressDialog(window.getShell());
				RunnableWithProgress progressRunnable = new RunnableWithProgress(request);
				progressDialog.run(true, true, progressRunnable);
				
				if (progressRunnable.getProgressMonitor().isCanceled()) {
					return false;
				}
			}
			else {
				MessageDialog.openInformation(window.getShell(), "Info", "No audio collection editor active");
				return false;
			}
			page.saveState();
			return true;
		} catch (Exception e) {
			String msg = "Files could not be exported: ";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Error", msg + e.getMessage());
			return false;
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.window = workbench.getActiveWorkbenchWindow();
		this.page = AudioCollectionTemplateExportWizardPage.createExportPage();
	}
	
	@Override
	public void addPages() {
		addPage(page);
	}
	
	@Override
	public boolean canFinish() {
		return canFinish;
	}
	
	@Override
	public String getWindowTitle() {
		return "Audio Template Export Settings";
	}

	/**
	 * RunnableWithProgress
	 */
	private class RunnableWithProgress implements IRunnableWithProgress {
		private TemplateExportRequest request;
		private ProgressMonitorAdapter progressMonitor;
		
		public RunnableWithProgress(TemplateExportRequest request) {
			this.request = request;
		}
		
		public void run(IProgressMonitor monitor) {
			progressMonitor = new ProgressMonitorAdapter(monitor);
			request.setProgressMonitor(progressMonitor);
			
			TemplateExportService templateExportService = AudioSolutions.getService(TemplateExportService.class);
			try {
				templateExportService.exportFiles(request);
			}
			catch (ProgressCancelException exc) {
			}
			catch (final Exception exc) {
				logger.error("Files could not be exported", exc);
				window.getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(window.getShell(), "Error", "Export failed: \n" + exc.getMessage());
					}
				});
			}
		}
		
		public ProgressMonitorAdapter getProgressMonitor() {
			return progressMonitor;
		}
	}

	/**
	 * @param canFinish the canFinish to set
	 */
	public void setCanFinish(boolean canFinish) {
		this.canFinish = canFinish;
	}

}
