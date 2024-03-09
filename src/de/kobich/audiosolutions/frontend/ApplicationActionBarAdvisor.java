package de.kobich.audiosolutions.frontend;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import de.kobich.audiosolutions.frontend.common.ui.StatusLineEventListener;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions added to a workbench window. Each window will be populated
 * with new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	// Actions - important to allocate these only in makeActions, and then use them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	
	private static final String STATUS_LINE_SELECTED_FILES = "statusline.selected";
	private static final String STATUS_LINE_AVAILABLE_FILES = "statusline.available";
	private List<IWorkbenchAction> actions;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		super.makeActions(window);
		actions = new ArrayList<IWorkbenchAction>();
		actions.add(ActionFactory.QUIT.create(window));
		actions.add(ActionFactory.CLOSE.create(window));
		actions.add(ActionFactory.CLOSE_ALL.create(window));
		actions.add(ActionFactory.CLOSE_OTHERS.create(window));
		actions.add(ActionFactory.ABOUT.create(window));
		actions.add(ActionFactory.SAVE.create(window));
		actions.add(ActionFactory.SAVE_AS.create(window));
		actions.add(ActionFactory.SAVE_ALL.create(window));
		actions.add(ActionFactory.EXPORT.create(window));
		actions.add(ActionFactory.IMPORT.create(window));
		actions.add(ActionFactory.PREFERENCES.create(window));
		actions.add(ActionFactory.SHOW_VIEW_MENU.create(window));
		
		for (IWorkbenchAction action : actions) {
			register(action);
		}
	}
	
	@Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
		StatusLineContributionItem selectedItem = new StatusLineContributionItem(STATUS_LINE_SELECTED_FILES, StatusLineContributionItem.CALC_TRUE_WIDTH);
		statusLine.add(selectedItem);
		StatusLineEventListener.INSTANCE.selectedItem = selectedItem;
		StatusLineContributionItem availableItem = new StatusLineContributionItem(STATUS_LINE_AVAILABLE_FILES, StatusLineContributionItem.CALC_TRUE_WIDTH);
		statusLine.add(availableItem);
		StatusLineEventListener.INSTANCE.availableItem = availableItem;
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
//		IToolBarManager taskToolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
//		coolBar.add(new ToolBarContributionItem(taskToolbar, "Task"));
//		taskToolbar.add(createCRCFileAction);
//		taskToolbar.add(validateCRCFilesAction);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		actions = null;
//		for (IWorkbenchAction action : actions) {
//			action.dispose();
//		}
	}
}
