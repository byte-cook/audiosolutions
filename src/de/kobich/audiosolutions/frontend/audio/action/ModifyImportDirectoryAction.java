package de.kobich.audiosolutions.frontend.audio.action;

import java.io.File;
import java.util.Arrays;
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
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor.CollectionEditorType;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.commons.ui.jface.StatusLineUtils;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.FileDescriptorResult;

/**
 * Opens file collection editor.
 * 
 */
public class ModifyImportDirectoryAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(ModifyImportDirectoryAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart editorPart = window.getActivePage().getActiveEditor();
		if (editorPart instanceof AudioCollectionEditor) {
			final AudioCollectionEditor editor = (AudioCollectionEditor) editorPart;
			if (!CollectionEditorType.SEARCH.equals(editor.getFileCollection().getEditorType())) {
				/**
				 * This operation is intended to be used only for persistent audio files.
				 * You need to search for audio files to modify their import directory.
				 */
				String msg = "Operation is not supported. \n\nSearch for audio files to modify their import directory.";
				MessageDialog.openError(window.getShell(), "Modify Import Directory", msg);
				return null;
			}
			final Set<FileDescriptor> fileDescriptors = editor.getFileDescriptorSelection().getFileDescriptors();

			ModifyImportDirectoryDialog dialog = new ModifyImportDirectoryDialog(window.getShell());
			dialog.setFileDescriptors(fileDescriptors);
			int status = dialog.open();
			if (status == IDialogConstants.OK_ID) {
				final File importDirectory = dialog.getImportDirectory();
				logger.debug("Modify import directory: " + importDirectory.getAbsolutePath());

				List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
				JFaceThreadRunner runner = new JFaceThreadRunner("Modify Import Directory", window.getShell(), states) {
					private FileDescriptorResult result;

					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case WORKER_1:
							AudioDataService audioDataService = AudioSolutions.getService(AudioDataService.class);
							result = audioDataService.modifyImportDirectories(fileDescriptors, importDirectory, super.getProgressMonitor());
							break;
						case UI_2:
							// update editor
							editor.setDirty(true);
							
							UIEvent event = new UIEvent(ActionType.AUDIO_DATA, editor);
							event.getEditorDelta().copyFromResult(result);
							EventSupport.INSTANCE.fireEvent(event);
							
							StatusLineUtils.setStatusLineMessage(editor, result.getUpdatedFiles().size() + " file(s) modified", false);
							break;
						case UI_ERROR:
							if (super.getProgressMonitor().isCanceled()) {
								return;
							}
							Exception e = super.getException();
							logger.error(e.getMessage(), e);
							MessageDialog.openError(super.getParent(), super.getName(), "Import directory could not be changed: \n" + e.getMessage());
							break;
						default: 
							break;
						}
					}
				};
				runner.runProgressMonitorDialog(true, true);
			}
		}
		return null;
	}
}
