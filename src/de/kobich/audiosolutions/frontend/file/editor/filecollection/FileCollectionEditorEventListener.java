package de.kobich.audiosolutions.frontend.file.editor.filecollection;

import java.util.Arrays;
import java.util.List;

import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

/**
 * Audio files editor.
 */
public class FileCollectionEditorEventListener extends EventListenerAdapter {
	private FileCollectionEditor editor;

	/**
	 * Constructor
	 */
	public FileCollectionEditorEventListener(FileCollectionEditor editor) {
		super(editor, ListenerType.UI_EVENT);
		this.editor = editor;
	}

	@Override
	public void eventFired(final UIEvent event) {
		boolean active = event.getEditorDelta() != null ? event.getEditorDelta().isActionEditor(this.editor) : false;
		if (active) {
			// update active editor immediately
			updateEditor(event);
		}
		else {
			// update inactive editor in background
			List<RunningState> states = Arrays.asList(RunningState.WORKER_1);
			JFaceThreadRunner runner = new JFaceThreadRunner("Update Collection Editor", editor.getSite().getShell(), states) {
				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case WORKER_1:
						updateEditor(event);
						break;
					default: 
						break;
					}
				}
			};
			runner.runBackgroundJob(0, false, true, null);
		}
	}
	
	private void updateEditor(UIEvent event) {
		switch (event.getActionType()) {
		case FILE:
		case FILE_MONITOR:
			editor.update(event.getFileDelta());
			break;
		case AUDIO_DATA:
		case AUDIO_SAVED:
		case AUDIO_SEARCH:
			editor.update(event.getAudioDelta());
			break;
		default:
			break;
		}
	}
}
