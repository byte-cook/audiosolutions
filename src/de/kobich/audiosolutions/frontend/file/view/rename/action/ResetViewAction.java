package de.kobich.audiosolutions.frontend.file.view.rename.action;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import de.kobich.audiosolutions.frontend.file.view.rename.RenameFilesView;
import de.kobich.audiosolutions.frontend.file.view.rename.RenameFilesViewSourceProvider;

/**
 * Action to reset rename view.
 */
public class ResetViewAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(ResetViewAction.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			RenameFilesView view = (RenameFilesView) window.getActivePage().findView(RenameFilesView.ID);
			view.reset();
			view.refreshPreview();
			
			// fire event
			ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);
			RenameFilesViewSourceProvider p = (RenameFilesViewSourceProvider) sourceProviderService.getSourceProvider(RenameFilesViewSourceProvider.RENAME_TAB_ENABLED_STATE);
			p.changeState(RenameFilesViewSourceProvider.RENAME_TAB_ENABLED_STATE, Boolean.FALSE);
			p.changeState(RenameFilesViewSourceProvider.RENAME_PREVIEW_STATE, Boolean.FALSE);
		} catch (Exception exc) {
			String msg = "Error while reset view: ";
			logger.error(msg, exc);
			MessageDialog.openError(window.getShell(), "Rename View", msg + exc.getMessage());
		}
		
		return null;
	}

}
