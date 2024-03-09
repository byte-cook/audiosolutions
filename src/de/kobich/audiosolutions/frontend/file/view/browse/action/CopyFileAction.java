package de.kobich.audiosolutions.frontend.file.view.browse.action;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;
import de.kobich.component.file.FileException;
import de.kobich.component.file.FileResult;
import de.kobich.component.file.io.CopyFileRequest;
import de.kobich.component.file.io.FileCreationType;
import de.kobich.component.file.io.FileIOService;

/**
 * Action to copy a file.
 * 
 */
public class CopyFileAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(CopyFileAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		BrowseFilesView view = (BrowseFilesView) window.getActivePage().findView(BrowseFilesView.ID);
		File currentFile = view.getSelectedFile();
		if (currentFile != null && currentFile.exists()) {
			FileBrowserDialog dialog = FileBrowserDialog.createCopyDialg(window.getShell());
			dialog.setSource(currentFile);
			dialog.setTargetDirectory(currentFile);
			int status = dialog.open();
			if (status == IDialogConstants.OK_ID) {
				UIEvent uiEvent = new UIEvent(ActionType.FILE);
				try {
					File targetDir = dialog.getTargetDirectory();

					FileIOService fileIOService = AudioSolutions.getService(FileIOService.class);
					if (currentFile.isFile()) {
						String fileName = currentFile.getName();
						File target = new File(targetDir.getAbsolutePath() + File.separator + fileName);
						CopyFileRequest request = new CopyFileRequest(currentFile, target, FileCreationType.COPY);
						FileResult result = fileIOService.copyFile(request);
						uiEvent.getFileDelta().copyFromResult(result);
					}
					else if (currentFile.isDirectory()) {
						String folderName = currentFile.getName();
						File target = new File(targetDir.getAbsolutePath() + File.separator + folderName);
						CopyFileRequest request = new CopyFileRequest(currentFile, target, FileCreationType.COPY);
						FileResult result = fileIOService.copyFolder(request);
						uiEvent.getFileDelta().copyFromResult(result);
					}
					EventSupport.INSTANCE.fireEvent(uiEvent);
					view.refreshView();
				}
				catch (FileException exc) {
					logger.error(exc.getMessage(), exc);
					MessageDialog.openError(window.getShell(), "Copy File", "File cannot be copied: \n" + exc.getMessage());
					EventSupport.INSTANCE.fireEvent(uiEvent);
				}
			}
		}
		return null;
	}
}
