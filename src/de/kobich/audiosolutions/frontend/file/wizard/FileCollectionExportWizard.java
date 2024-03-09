package de.kobich.audiosolutions.frontend.file.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
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
import de.kobich.audiosolutions.frontend.common.ui.ProgressDialog;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.FileCollectionEditor;
import de.kobich.commons.monitor.progress.ProgressCancelException;
import de.kobich.commons.ui.jface.progress.ProgressMonitorAdapter;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.imexport.ExportFilesRequest;
import de.kobich.component.file.imexport.MetaDataExportingFormatType;
import de.kobich.component.file.imexport.MetaDataImExportService;
import de.kobich.component.file.imexport.metadata.IMetaDataExporter;


/**
 * Exports files.
 */
public class FileCollectionExportWizard extends Wizard implements IExportWizard {
	private static final Logger logger = Logger.getLogger(FileCollectionExportWizard.class); 
	private IWorkbenchWindow window;
	private FileCollectionImExportWizardPage page;
	private boolean canFinish;
	
	@Override
	public boolean performFinish() {
		try {
			IEditorPart editorPart = window.getActivePage().getActiveEditor();
			if (editorPart instanceof FileCollectionEditor) {
				FileCollectionEditor fileCollectionEditor = (FileCollectionEditor) editorPart;
				Set<FileDescriptor> fileDescriptors = fileCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
				
				if (fileDescriptors.isEmpty()) {
					fileDescriptors = fileCollectionEditor.getFileCollection().getFileDescriptors();
//					MessageDialog.openInformation(window.getShell(), "Info", "No files selected");
//					return false;
				}

				File targetFile = page.getTargetFile();
				String extension = FilenameUtils.getExtension(targetFile.getName());
				MetaDataExportingFormatType format = MetaDataExportingFormatType.valueOf(extension.toUpperCase());
				List<IMetaDataExporter> metaDataExporters = new ArrayList<IMetaDataExporter>();

				ExportFilesRequest request = new ExportFilesRequest(targetFile, fileDescriptors, format, metaDataExporters);
				ProgressDialog progressDialog = new ProgressDialog(window.getShell());
				RunnableWithProgress progressRunnable = new RunnableWithProgress(request);
				progressDialog.run(true, true, progressRunnable);
				
				if (progressRunnable.getProgressMonitor().isCanceled()) {
					return false;
				}
			}
			else {
				MessageDialog.openInformation(window.getShell(), "Info", "No file collection editor active");
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
	public void init(IWorkbench workbench, IStructuredSelection structuredSelection) {
		this.window = workbench.getActiveWorkbenchWindow();
		this.page = FileCollectionImExportWizardPage.createExportPage();
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
		return "File Export Settings";
	}
	
	/**
	 * RunnableWithProgress
	 */
	private class RunnableWithProgress implements IRunnableWithProgress {
		private ExportFilesRequest request;
		private ProgressMonitorAdapter progressMonitor;
		
		public RunnableWithProgress(ExportFilesRequest request) {
			this.request = request;
		}
		
		public void run(IProgressMonitor monitor) {
			progressMonitor = new ProgressMonitorAdapter(monitor);
			request.setProgressMonitor(progressMonitor);
			
			MetaDataImExportService metaDataImExportService = AudioSolutions.getService(MetaDataImExportService.class);
			try {
				metaDataImExportService.exportFiles(request);
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
