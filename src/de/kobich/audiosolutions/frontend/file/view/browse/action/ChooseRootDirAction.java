package de.kobich.audiosolutions.frontend.file.view.browse.action;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;

/**
 * Action to reset rename view.
 */
public class ChooseRootDirAction extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		BrowseFilesView view = (BrowseFilesView) window.getActivePage().findView(BrowseFilesView.ID);
		DirectoryDialog dialog = new DirectoryDialog(window.getShell());
		dialog.setFilterPath(view.getRootDirectory().getPath());
		String path = dialog.open();
		if (path != null) {
			view.setRootDirectory(new File(path));
		}
		return null;
	}

}
