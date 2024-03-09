package de.kobich.audiosolutions.frontend.common;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Adds the close action to the console.
 */
public class ConsolePageParticipant implements IConsolePageParticipant {
	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		CloseConsoleAction closeAction = new CloseConsoleAction(console);
	
		IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, closeAction);
	}

	@Override
	public void activated() {

	}

	@Override
	public void deactivated() {

	}

	@Override
	public void dispose() {
		deactivated();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

}
