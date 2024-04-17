package de.kobich.audiosolutions.frontend.audio.view.play.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;

/**
 * Action to toggle loop enabled.
 */
public class ToggleLoopEnabledAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.audio.toggleLoopEnabled";
	public static final String STATE_ID = "org.eclipse.ui.commands.toggleState";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		AudioPlayView view = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
		boolean newValue = !HandlerUtil.toggleCommandState(event.getCommand());
		view.setLoopEnabled(newValue);
		return null;
	}
	
	public static boolean getInitialValue() {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command commandLock = commandService.getCommand(ID);
		Object lock = commandLock.getState(STATE_ID).getValue();
		if (lock instanceof Boolean b) {
			return b;
		}
		return false;
	}
}
