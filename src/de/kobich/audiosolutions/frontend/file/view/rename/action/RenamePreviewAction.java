package de.kobich.audiosolutions.frontend.file.view.rename.action;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
import de.kobich.audiosolutions.frontend.file.view.rename.model.RenameFileDescriptor;
import de.kobich.commons.misc.rename.Renamer;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

/**
 * Action to rename files.
 */
public class RenamePreviewAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(RenamePreviewAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.view.rename.renamePreview";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final RenameFilesView view = (RenameFilesView) window.getActivePage().findView(RenameFilesView.ID);
		
		List<RunningState> states = Arrays.asList(RunningState.UI_1);
		JFaceThreadRunner runner = new JFaceThreadRunner("Rename Files Preview", window.getShell(), states) {

			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case UI_1:
					// reset preview data
					view.getFileModel().reset();
					
					// add rename filter for each tab view
					Set<RenameFileDescriptor> files = view.getFileModel().getRenameables();
					List<IRenameRule> renameRules = view.getRenameRules();
					
					Renamer.rename(files, renameRules);
					
					view.refreshPreview();
					
					// fire event
					ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);
					RenameFilesViewSourceProvider p = (RenameFilesViewSourceProvider) sourceProviderService.getSourceProvider(RenameFilesViewSourceProvider.RENAME_TAB_ENABLED_STATE);
					p.changeState(RenameFilesViewSourceProvider.RENAME_PREVIEW_STATE, Boolean.TRUE);
					break;
				case UI_ERROR:
					Exception exc = super.getException();
					logger.error(exc.getMessage(), exc);
					MessageDialog.openError(super.getParent(), super.getName(), "Error while renaming: \n" + exc.getMessage());
					break;
				default:
					break;
				}
			}
		};
		runner.runBackgroundJob(0, true, false, null);
		return null;
	}

}
