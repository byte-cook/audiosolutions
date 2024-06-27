package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.action;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
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
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.FileResult;
import de.kobich.component.file.io.FileIOService;
import de.kobich.component.file.io.FilesRequest;

/**
 * Action to delete file descriptors from collection editor.
 */
public class DeleteFileDescriptionAction extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart editorPart = window.getActivePage().getActiveEditor();
		if (editorPart instanceof ICollectionEditor) {
			final ICollectionEditor editor = (ICollectionEditor) editorPart;
			final Set<FileDescriptor> fileDescriptors = editor.getFileDescriptorSelection().getFileDescriptors();
			
			final Wrapper<FileResult> RESULT = Wrapper.empty();
			JFaceExec.builder(window.getShell(), "Delete Files")
				.ui(ctx -> {
					FileQueryDialog dialog = FileQueryDialog.createYesNoDialog(ctx.getParent(), ctx.getName(), "Do you really want to delete following file(s)?", fileDescriptors);
					int status = dialog.open();
					ctx.setCanceled(Window.OK != status);
				})
				.worker(ctx -> {
					Collection<File> files = ConverterUtils.convert(fileDescriptors, FileDescriptorConverter.INSTANCE);
					
					FileIOService fileIOService = AudioSolutions.getService(FileIOService.class);
					FilesRequest request = new FilesRequest(files);
					RESULT.set(fileIOService.deleteFiles(request));
				})
				.ui(ctx -> {
					if (RESULT.isPresent()) {
						UIEvent uiEvent = new UIEvent(ActionType.FILE);
						uiEvent.getFileDelta().copyFromResult(RESULT.get());
						EventSupport.INSTANCE.fireEvent(uiEvent);
						
						if (!RESULT.get().getFailedFiles().isEmpty()) {
							MessageDialog.openError(ctx.getParent(), ctx.getName(), "File deletion failed. \n");
						}
					}
				})
				.exceptionalDialog("File deletion failed")
				.runProgressMonitorDialog(true, false);
		}
		return null;
	}
}
