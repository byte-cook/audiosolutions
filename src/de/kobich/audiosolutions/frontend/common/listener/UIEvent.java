package de.kobich.audiosolutions.frontend.common.listener;

import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;

/**
 * Represents an UI event to fire.
 * <p />
 * CollectionEditorDelta should be used if the current editor's update mechanism
 * should operate different compared to other listeners.
 */
public class UIEvent {
	private final ActionType actionType;
	private final CollectionEditorDelta editorDelta;
	private final FileDelta fileDelta;
	private final AudioDelta audioDelta;

	public UIEvent(ActionType type) {
		this(type, null);
	}

	/**
	 * @param type
	 * @param actionEditor
	 *            editor which initiated this event
	 */
	public UIEvent(ActionType type, ICollectionEditor actionEditor) {
		this.actionType = type;
		this.editorDelta = actionEditor != null ? new CollectionEditorDelta(type, actionEditor) : null;
		switch (type) {
		case FILE:
		case FILE_MONITOR:
			this.fileDelta = new FileDelta(type, editorDelta);
			this.audioDelta = null;
			break;
		case AUDIO_DATA:
		case AUDIO_SAVED:
		case AUDIO_SEARCH:
			this.fileDelta = null;
			this.audioDelta = new AudioDelta(type, editorDelta);
			break;
		default:
			throw new IllegalStateException("Illegal action type: " + type);
		}
	}

	public ActionType getActionType() {
		return actionType;
	}

	public CollectionEditorDelta getEditorDelta() {
		return editorDelta;
	}

	public FileDelta getFileDelta() {
		return fileDelta;
	}

	public AudioDelta getAudioDelta() {
		return audioDelta;
	}
}
