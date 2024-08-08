package de.kobich.audiosolutions.frontend.file.view.rename.action;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionManager;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;
import de.kobich.audiosolutions.frontend.file.view.rename.RenameFilesView;
import de.kobich.audiosolutions.frontend.file.view.rename.RenameFilesViewSourceProvider;
import de.kobich.audiosolutions.frontend.file.view.rename.model.FileModel;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.commons.ui.jface.StatusLineUtils;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.FileDescriptorResult;
import de.kobich.component.file.descriptor.FileDescriptorService;
import de.kobich.component.file.descriptor.RenameFileDescriptor;

/**
 * Action to rename files.
 * 
 */
public class RenameFilesAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(RenameFilesAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final RenameFilesView view = (RenameFilesView) window.getActivePage().findView(RenameFilesView.ID);
		final FileModel fileModel = view.getFileModel();
		final AudioCollectionEditor editor = SelectionManager.INSTANCE.getActiveEditor(AudioCollectionEditor.class);

		List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("Rename Files", window.getShell(), states) {
			private FileDescriptorResult result;
			private List<IRenameRule> renameRules;

			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case UI_1:
					this.renameRules = view.getRenameRules();
					break;
				case WORKER_1:
					IServiceProgressMonitor progressMonitor = super.getProgressMonitor();
					
					// reset preview data
					fileModel.reset();
					Set<RenameFileDescriptor> files = fileModel.getRenameables();
					
					FileDescriptorService fileDescriptorService = AudioSolutions.getService(FileDescriptorService.class);
					result = fileDescriptorService.renameFiles(files, renameRules, progressMonitor);
					break;
				case UI_2:
					// show failed dialog
					if (!result.getFailedFiles().isEmpty()) {
						// run asynchronously
						super.getParent().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								FileResultDialog dialog = FileResultDialog.createDialog(getParent(), getName(), "Renaming files failed.", result.getFailedFiles());
								dialog.open();
							}
						});
					}
					Map<FileDescriptor, FileDescriptor> renamedFiles = result.getReplacedFiles();
					if (renamedFiles.values().isEmpty()) {
						// nothing happened
						break;
					}
					
					// fire UI event
					UIEvent uiEvent = new UIEvent(ActionType.FILE, editor);
					if (editor != null) {
						uiEvent.getEditorDelta().copyFromResult(result);
						uiEvent.getFileDelta().copyFromResult(result);
					}
/*					else {
						// editor can be null if RenameView is maximized
						uiEvent.getFileDelta().copyFromResult(result);
						
						// update file model manually
						FileModel model = view.getFileModel();
						model.reset();
						for (FileDescriptor old : renamedFiles.keySet()) {
							// 1. remove
							model.removeFile(old);
						}
						for (FileDescriptor old : renamedFiles.keySet()) {
							// 2. add
							FileDescriptor newFile = renamedFiles.get(old);
							model.addFile(newFile);
						}
						view.refreshPreview();
					}*/
					// UI event will also update this FileModel because CollectionEditor fires a SelectionChanged event
					EventSupport.INSTANCE.fireEvent(uiEvent);
					
					// update view's state
					RenameFilesViewSourceProvider p = RenameFilesViewSourceProvider.getInstance();
					p.setFileRenamed(true);
					p.setPreviewRenamed(false);

					StatusLineUtils.setStatusLineMessage(view, "Files renamed", false);
					break;
				case UI_ERROR:
					Exception exc = super.getException();
					logger.error(exc.getMessage(), exc);
					MessageDialog.openError(super.getParent(), super.getName(), "Error while renaming: " + exc.getMessage());
					break;
				default:
					break;
				}
			}
		};
		runner.runProgressMonitorDialog(true, false);

		return null;
	}
}
