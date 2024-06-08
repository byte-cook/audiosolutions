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
import de.kobich.audiosolutions.core.service.AlbumIdentity;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
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
 * Action to assign files to the same album
 */
public class AssignFilesToSameAlbumAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(AssignFilesToSameAlbumAction.class); 
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		final AudioCollectionEditor audioCollectionEditor = SelectionManager.INSTANCE.getActiveEditor(AudioCollectionEditor.class);
		if (audioCollectionEditor == null) {
			return null;
		}
		final Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
		if (fileDescriptors.isEmpty()) {
			return null;
		}

		List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("Set Audio Data", window.getShell(), states) {
			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case WORKER_1:
					AudioDataService audioDataService = AudioSolutions.getService(AudioDataService.class);
					
					final AudioDataChange change = AudioDataChange.builder().albumIdentity(AlbumIdentity.createNew()).build();
					audioDataService.applyChanges(fileDescriptors, change, super.getProgressMonitor());
					break;
				case UI_2:
					UIEvent uiEvent = new UIEvent(ActionType.AUDIO_DATA, audioCollectionEditor);
					uiEvent.getEditorDelta().getUpdateItems().addAll(fileDescriptors);
					EventSupport.INSTANCE.fireEvent(uiEvent);
					
					StatusLineUtils.setStatusLineMessage(audioCollectionEditor, "Audio data set", false);
					break;
				case UI_ERROR:
					if (super.getProgressMonitor().isCanceled()) {
						return;
					}
					Exception exc = super.getException();
					logger.error(exc.getMessage(), exc);
					MessageDialog.openError(super.getParent(), super.getName(), "Audio Data could not be set.\n" + exc.getMessage());
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
