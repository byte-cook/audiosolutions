package de.kobich.audiosolutions.frontend.audio.editor.audiocollection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

/**
 * Audio files editor.
 */
public class AudioCollectionEditorEventListener extends EventListenerAdapter implements PropertyChangeListener {
	private AudioCollectionEditor editor;

	/**
	 * Constructor
	 */
	public AudioCollectionEditorEventListener(AudioCollectionEditor editor) {
		super(editor, ListenerType.UI_EVENT);
		this.editor = editor;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (FileDescriptor.METADATA_PROP.equals(evt.getPropertyName())) {
			editor.setDirty(true);
			if (evt.getNewValue() != null) {
				if (evt.getNewValue() instanceof AudioData) {
					AudioData audioData = (AudioData) evt.getNewValue();
					audioData.addPropertyChangeListener(this);
				}
			}
			else {
				if (evt.getOldValue() instanceof AudioData) {
					AudioData audioData = (AudioData) evt.getOldValue();
					audioData.removePropertyChangeListener(this);
				}
			}
		}
		else if (AudioData.TRACK_ID_PROP.equals(evt.getPropertyName())) {
			editor.setDirty(true);
		}
		else if (AudioData.AUDIO_ATTRIBUTE_PROP.equals(evt.getPropertyName())) {
			editor.setDirty(true);
		}
		else if (AudioData.AUDIO_STATE_PROP.equals(evt.getPropertyName())) {
			editor.setDirty(true);
		}
		else if (FileCollection.ADD_FILE_PROP.equals(evt.getPropertyName())) {
			FileDescriptor add = (FileDescriptor) evt.getNewValue();
			add.addPropertyChangeListener(this);
			if (add.hasMetaData() && add.getMetaData() instanceof AudioData) {
				AudioData audioData = (AudioData) add.getMetaData();
				audioData.addPropertyChangeListener(this);
			}
		}
		else if (FileCollection.REMOVE_FILE_PROP.equals(evt.getPropertyName())) {
			FileDescriptor remove = (FileDescriptor) evt.getOldValue();
			remove.removePropertyChangeListener(this);
			if (remove.hasMetaData() && remove.getMetaData() instanceof AudioData) {
				AudioData audioData = (AudioData) remove.getMetaData();
				audioData.removePropertyChangeListener(this);
			}
		}
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
