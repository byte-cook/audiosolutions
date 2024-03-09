package de.kobich.audiosolutions.frontend.file.view.browse.action;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileResult;
import de.kobich.component.file.io.FileIOService;
import de.kobich.component.file.io.FilesRequest;

/**
 * Action to delete files.
 */
public class DeleteFileAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(DeleteFileAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final BrowseFilesView view = (BrowseFilesView) window.getActivePage().findView(BrowseFilesView.ID);
		final File currentFile = view.getSelectedFile();
		if (currentFile != null && currentFile.exists()) {
			
			List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Delete Files", window.getShell(), states) {
				private FileResult result;
				
				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case UI_1:
						boolean confirmed = MessageDialog.openQuestion(super.getParent(), super.getName(), "Do you really want to delete: " + currentFile.getPath());
						if (!confirmed) {
							super.setNextState(RunningState.UI_2);
						}
						break;
					case WORKER_1:
						FileIOService fileIOService = AudioSolutions.getService(FileIOService.class);
						FilesRequest request = new FilesRequest(currentFile);
						result = fileIOService.deleteFiles(request);
						break;
					case UI_2:
						UIEvent event = new UIEvent(ActionType.FILE);
						event.getFileDelta().copyFromResult(result);
						EventSupport.INSTANCE.fireEvent(event);
						view.refreshView();
						break;
					case UI_ERROR:
						Exception e = super.getException();
						logger.error(e.getMessage(), e);
						MessageDialog.openError(super.getParent(), super.getName(), "File deletion failed: \n" + e.getMessage());

						EventSupport.INSTANCE.fireEvent(new UIEvent(ActionType.FILE));
						view.refreshView();
						break;
					default: 
						break;
					}
				}
			};
			runner.runProgressMonitorDialog(true, false);
		}
		return null;
	}

}
