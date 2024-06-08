package de.kobich.audiosolutions.frontend.audio.action;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionManager;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.commons.ui.jface.StatusLineUtils;
import de.kobich.component.file.FileDescriptor;

/**
 * Sets audio data to files by structure.
 * 
 */
public class SetAudioDataByTextAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

		final AudioCollectionEditor audioCollectionEditor = SelectionManager.INSTANCE.getActiveEditor(AudioCollectionEditor.class);
		if (audioCollectionEditor == null) {
			return null;
		}
		Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
		if (fileDescriptors.isEmpty()) {
			return null;
		}

		SetAudioDataByTextDialog dialog = new SetAudioDataByTextDialog(window.getShell(), fileDescriptors);
		int status = dialog.open();
		if (status == IDialogConstants.OK_ID) {
			final Set<AudioDataChange> changes = dialog.getChanges();

			List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Set Audio Data", window.getShell(), states) {
				private Set<FileDescriptor> fileDescriptors;

				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case UI_1:
						break;
					case WORKER_1:
						AudioDataService audioDataService = AudioSolutions.getService(AudioDataService.class);
						fileDescriptors = audioDataService.applyChanges(changes, super.getProgressMonitor());
						break;
					case UI_2:
						UIEvent uiEvent = new UIEvent(ActionType.AUDIO_DATA, audioCollectionEditor);
						uiEvent.getEditorDelta().getUpdateItems().addAll(fileDescriptors);
						EventSupport.INSTANCE.fireEvent(uiEvent);
						
						StatusLineUtils.setStatusLineMessage(audioCollectionEditor, "Audio data set", false);
						break;
					case UI_ERROR:
						Exception e = super.getException();
						MessageDialog.openError(window.getShell(), super.getName(), "Audio data could not be set: \n" + e.getMessage());
						break;
					default:
						break;
					}

				}
			};
			runner.runBackgroundJob(0, true, false, Activator.getDefault().getImageDescriptor("/icons/audio/audio-data-by-text.png"));

		}

		return null;
	}
}
