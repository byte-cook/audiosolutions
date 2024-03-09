package de.kobich.audiosolutions.frontend.file.view.browse.action;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;


/**
 * Opens a file by standard editor or by system editor.
 */
public class OpenWithSystemAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(OpenWithSystemAction.class); 
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		BrowseFilesView view = (BrowseFilesView) window.getActivePage().findView(BrowseFilesView.ID);
		File file = view.getSelectedFile();
		try {
			// open file
			if (!Program.launch(file.getAbsolutePath())) {
				MessageDialog.openInformation(window.getShell(), "Info", "No suitable editor could be found for:\n" + file.getName());
			}
		} catch (Exception e) {
			String msg = "Error while opening files";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Error", msg + e.getMessage());
		}
		return null;
	}
}
