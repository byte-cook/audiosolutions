package de.kobich.audiosolutions.frontend.file.view.browse.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;

/**
 * Action to reset rename view.
 */
public class RefreshFilesAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		BrowseFilesView view = (BrowseFilesView) window.getActivePage().findView(BrowseFilesView.ID);
		if (view != null) {
			view.refreshView();
		}
		return null;
	}

}
