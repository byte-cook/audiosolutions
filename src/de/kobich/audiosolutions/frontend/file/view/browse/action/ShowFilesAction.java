package de.kobich.audiosolutions.frontend.file.view.browse.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;

/**
 * Action to toggle show files in browse view.
 */
public class ShowFilesAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.view.browser.showFiles";
	public static final String STATE_ID = "org.eclipse.ui.commands.toggleState";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		BrowseFilesView view = (BrowseFilesView) window.getActivePage().findView(BrowseFilesView.ID);
		boolean oldValue = HandlerUtil.toggleCommandState(event.getCommand());
		view.setFilesVisible(!oldValue);
		view.refreshView();
		
		return null;
	}
}
