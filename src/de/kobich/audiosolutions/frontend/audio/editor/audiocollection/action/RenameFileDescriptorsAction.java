package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.ui.QueryTextDialog;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.FileDescriptorResult;
import de.kobich.component.file.descriptor.FileDescriptorService;
import de.kobich.component.file.descriptor.IFileDescriptorRenameable;
import de.kobich.component.file.descriptor.RenameFileDescriptor;

/**
 * Action to rename file descriptors.
 * 
 */
public class RenameFileDescriptorsAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(RenameFileDescriptorsAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart editorPart = window.getActivePage().getActiveEditor();
		if (editorPart instanceof ICollectionEditor) {
			final ICollectionEditor editor = (ICollectionEditor) editorPart;
			Set<FileDescriptor> fileDescriptors = editor.getFileDescriptorSelection().getFileDescriptors();
			if (fileDescriptors.isEmpty()) {
				return null;
			}
			
			List<FileDescriptor> fileDescriptorList = new ArrayList<>(fileDescriptors);
			Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());
			final FileDescriptor currentFile = fileDescriptorList.get(0);
			
			final QueryTextDialog dialog = QueryTextDialog.createDialog(window.getShell(), "Rename Files", "Original name:\n " + currentFile.getFile().getPath(),
					"New name:", currentFile.getFile().getName());
			int status = dialog.open();
			if (status == IDialogConstants.OK_ID) {
				List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
				JFaceThreadRunner runner = new JFaceThreadRunner("Rename Files", window.getShell(), states) {
					private FileDescriptorResult result;

					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case UI_1:
							break;
						case WORKER_1:
							String newName = dialog.getText();
							FileDescriptorService fileDescriptorService = AudioSolutions.getService(FileDescriptorService.class);
							// only one file will be renamed
							RenameFileDescriptor renameFile = new RenameFileDescriptor(currentFile, null);
							renameFile.setName(newName);
							
							Set<IFileDescriptorRenameable> files = Collections.singleton(renameFile);
							result = fileDescriptorService.renameFiles(files, super.getProgressMonitor());
							break;
						case UI_2:
							UIEvent event = new UIEvent(ActionType.FILE, editor);
							event.getEditorDelta().copyFromResult(result);
							event.getFileDelta().copyFromResult(result);
							EventSupport.INSTANCE.fireEvent(event);
							break;
						case UI_ERROR:
							Exception e = super.getException();
							logger.error(e.getMessage(), e);
							MessageDialog.openError(window.getShell(), super.getName(), "Files could not be renamed: \n" + e.getMessage());
							break;
						default: 
							break;
						} 

					}
				};
				runner.runProgressMonitorDialog(true, false);
			}
		}
		return null;
	}

}
