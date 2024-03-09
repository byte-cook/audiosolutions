package de.kobich.audiosolutions.frontend.file.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.frontend.common.ui.ProgressDialog;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.IOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.ImportOpeningInfo;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.FileCollectionEditor;
import de.kobich.commons.monitor.progress.ProgressCancelException;
import de.kobich.commons.ui.jface.progress.ProgressMonitorAdapter;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.imexport.ImportFilesRequest;
import de.kobich.component.file.imexport.MetaDataImExportService;
import de.kobich.component.file.imexport.metadata.IMetaDataImporter;


/**
 * Exports files.
 */
public class FileCollectionImportWizard extends Wizard implements IExportWizard {
	private static final Logger logger = Logger.getLogger(FileCollectionImportWizard.class); 
	private IWorkbenchWindow window;
	private FileCollectionImExportWizardPage page;
	private boolean canFinish;
	
	@Override
	public boolean performFinish() {
		try {
			File targetFile = page.getTargetFile();
			List<IMetaDataImporter> metaDataImporters = new ArrayList<IMetaDataImporter>();

			ImportFilesRequest request = new ImportFilesRequest(targetFile, metaDataImporters);
			ProgressDialog progressDialog = new ProgressDialog(window.getShell());
			RunnableWithProgress progressRunnable = new RunnableWithProgress(request);
			progressDialog.run(true, true, progressRunnable);

			if (progressRunnable.getProgressMonitor().isCanceled()) {
				return false;
			}

			IOpeningInfo openingInfo = new ImportOpeningInfo(request.getSourceFile());
			FileCollection fileCollection = new FileCollection(openingInfo, progressRunnable.getFileDescriptors());
			
	    	// Get the view
			IWorkbenchPage workbenchPage = window.getActivePage();
			workbenchPage.openEditor(fileCollection, FileCollectionEditor.ID);

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
		this.page = FileCollectionImExportWizardPage.createImportPage();
		this.canFinish = false;
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
		return "File Import Settings";
	}
	
	/**
	 * RunnableWithProgress
	 */
	private class RunnableWithProgress implements IRunnableWithProgress {
		private ImportFilesRequest request;
		private ProgressMonitorAdapter progressMonitor;
		private Set<FileDescriptor> fileDescriptors;
		
		public RunnableWithProgress(ImportFilesRequest request) {
			this.request = request;
		}
		
		public void run(IProgressMonitor monitor) {
			progressMonitor = new ProgressMonitorAdapter(monitor);
			request.setProgressMonitor(progressMonitor);
			
			MetaDataImExportService metaDataImExportService = AudioSolutions.getService(MetaDataImExportService.class);
			try {
				this.fileDescriptors = metaDataImExportService.importFiles(request);
			}
			catch (ProgressCancelException exc) {
			}
			catch (final Exception exc) {
				logger.error("Files could not be exported", exc);
				window.getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(window.getShell(), "Error", "Import failed: \n" + exc.getMessage());
					}
				});
			}
		}
		
		public ProgressMonitorAdapter getProgressMonitor() {
			return progressMonitor;
		}

		/**
		 * @return the fileDescriptors
		 */
		public Set<FileDescriptor> getFileDescriptors() {
			return fileDescriptors;
		}
	}

	/**
	 * @param canFinish the canFinish to set
	 */
	public void setCanFinish(boolean canFinish) {
		this.canFinish = canFinish;
	}
}
