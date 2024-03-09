package de.kobich.audiosolutions.frontend.file.editor.filecollection.action;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.frontend.common.FileDescriptorConverter;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.ui.FileQueryDialog;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.commons.converter.ConverterUtils;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.FileResult;
import de.kobich.component.file.io.FileIOService;
import de.kobich.component.file.io.FilesRequest;

/**
 * Action to delete file descriptors from collection editor.
 */
public class DeleteFileDescriptionAction extends AbstractHandler {
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart editorPart = window.getActivePage().getActiveEditor();
		if (editorPart instanceof ICollectionEditor) {
			final ICollectionEditor editor = (ICollectionEditor) editorPart;
			final Set<FileDescriptor> fileDescriptors = editor.getFileDescriptorSelection().getFileDescriptors();
			
			List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Delete Files", window.getShell(), states) {
				private FileResult result;
				
				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case UI_1:
						FileQueryDialog dialog = FileQueryDialog.createYesNoDialog(super.getParent(), super.getName(), "Do you really want to delete following file(s)?", fileDescriptors);
						int status = dialog.open();
						if (IDialogConstants.CANCEL_ID == status) {
							super.setNextState(RunningState.UI_2);
						}
						break;
					case WORKER_1:
						Collection<File> files = ConverterUtils.convert(fileDescriptors, FileDescriptorConverter.INSTANCE);
						
						FileIOService fileIOService = AudioSolutions.getService(FileIOService.class);
						FilesRequest request = new FilesRequest(files);
						result = fileIOService.deleteFiles(request);
						break;
					case UI_2:
						if (result != null) {
							UIEvent event = new UIEvent(ActionType.FILE);
							event.getFileDelta().copyFromResult(result);
							EventSupport.INSTANCE.fireEvent(event);
							
							if (!result.getFailedFiles().isEmpty()) {
								MessageDialog.openError(super.getParent(), super.getName(), "File deletion failed. \n");
							}
						}
						break;
					case UI_ERROR:
						Exception e = super.getException();
						MessageDialog.openError(super.getParent(), super.getName(), "File deletion failed: \n" + e.getMessage());
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
