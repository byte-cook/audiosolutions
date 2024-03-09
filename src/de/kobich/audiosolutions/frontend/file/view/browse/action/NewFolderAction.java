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
import de.kobich.audiosolutions.frontend.common.ui.QueryTextDialog;
import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;
import de.kobich.component.file.FileException;
import de.kobich.component.file.FileResult;
import de.kobich.component.file.io.FileIOService;
import de.kobich.component.file.io.FilesRequest;

/**
 * Action to reset rename view.
 * 
 */
public class NewFolderAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(NewFolderAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		BrowseFilesView view = (BrowseFilesView) window.getActivePage().findView(BrowseFilesView.ID);
		File directory = view.getSelectedFile();
		if (directory.isFile()) {
			directory = directory.getParentFile();
		}
		QueryTextDialog dialog = QueryTextDialog.createDialog(window.getShell(), "New Folder", "Create new folder in: \n" + directory.getPath(),
				"File Name:", "");
		int status = dialog.open();
		if (status == IDialogConstants.OK_ID) {
			try {
				String newFilePath = directory.getPath() + File.separator + dialog.getText();
				File newFile = new File(newFilePath);
				FileIOService fileIOService = AudioSolutions.getService(FileIOService.class);
				FilesRequest request = new FilesRequest(newFile);
				FileResult result = fileIOService.createNewFolders(request);

				UIEvent uiEvent = new UIEvent(ActionType.FILE);
				uiEvent.getFileDelta().copyFromResult(result);
				EventSupport.INSTANCE.fireEvent(uiEvent);
				view.refreshView();
			}
			catch (FileException exc) {
				logger.error(exc.getMessage(), exc);
				MessageDialog.openError(window.getShell(), "New Folder", "New folder could not be created: \n" + exc.getMessage());
			}
		}
		return null;
	}
}
