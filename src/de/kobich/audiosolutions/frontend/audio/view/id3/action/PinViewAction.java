package de.kobich.audiosolutions.frontend.audio.view.id3.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.audio.view.id3.ID3TagView;

/**
 * Action to pin the view.
 */
public class PinViewAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.view.id3tag.pinView";
	public static final String STATE_ID = "org.eclipse.ui.commands.toggleState";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		ID3TagView view = (ID3TagView) window.getActivePage().findView(ID3TagView.ID);
		boolean oldValue = HandlerUtil.toggleCommandState(event.getCommand());
		view.setPinView(!oldValue);
		
		return null;
	}
}
