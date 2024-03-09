package de.kobich.audiosolutions.frontend.common.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Action to open a specific preference page.
 */
public class OpenPreferencesAction extends AbstractHandler { 
	public static final String PAGE_ID_PARAM = "de.kobich.audiosolutions.commands.openPreferences.pageId";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String pageId = event.getParameter(PAGE_ID_PARAM);
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(window.getShell(), pageId, null, null);
		dialog.open();
		return null;
	}
}
