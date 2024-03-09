package de.kobich.audiosolutions.frontend.audio.action;

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

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

public class RemoveAudioDataAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(RemoveAudioDataAction.class); 
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final AudioCollectionEditor audioCollectionEditor = SelectionSupport.INSTANCE.getActiveEditor(AudioCollectionEditor.class);
		if (audioCollectionEditor == null) {
			return null;
		}
		Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
		if (fileDescriptors.isEmpty()) {
			return null;
		}
		
		List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("Remove Audio Data", window.getShell(), states) {
			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case WORKER_1:
					AudioDataService audioDataService = AudioSolutions.getService(AudioDataService.class);
					audioDataService.removeAudioData(fileDescriptors, super.getProgressMonitor());
					break;
				case UI_2:
					UIEvent uiEvent = new UIEvent(ActionType.AUDIO_DATA, audioCollectionEditor);
					uiEvent.getEditorDelta().getUpdateItems().addAll(fileDescriptors);
					EventSupport.INSTANCE.fireEvent(uiEvent);
					break;
				case UI_ERROR:
					if (super.getProgressMonitor().isCanceled()) {
						return;
					}
					Exception e = super.getException();
					logger.error(e.getMessage(), e);
					MessageDialog.openError(super.getParent(), super.getName(), "Error while removing audio data: \n" + e.getMessage());
					break;
				default:
					break;
				}
			}
		};
		runner.runProgressMonitorDialog(true, true);
			
		return null;
	}
}
