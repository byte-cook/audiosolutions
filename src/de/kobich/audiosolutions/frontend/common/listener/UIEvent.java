package de.kobich.audiosolutions.frontend.common.listener;

import javax.annotation.Nullable;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorDelta;
import lombok.Getter;

/**
 * Represents an UI event to fire.
 * <p />
 * CollectionEditorDelta should be used if the current editor's update mechanism
 * should operate different compared to other listeners.
 */
@Getter
public class UIEvent {
	private final ActionType actionType;
	private final CollectionEditorDelta editorDelta;
	private final FileDelta fileDelta;
	private final AudioDelta audioDelta;
	private final PlaylistDelta playlistDelta;

	public UIEvent(ActionType type) {
		this(type, null);
	}

	/**
	 * @param type
	 * @param actionEditor editor which initiated this event
	 */
	public UIEvent(ActionType type, @Nullable AudioCollectionEditor actionEditor) {
		this.actionType = type;
		this.editorDelta = actionEditor != null ? new CollectionEditorDelta(type, actionEditor) : null;
		switch (type) {
		case FILE:
		case FILE_MONITOR:
			this.fileDelta = new FileDelta(type, editorDelta);
			this.audioDelta = null;
			this.playlistDelta = null;
			break;
		case AUDIO_DATA:
		case AUDIO_SAVED:
		case AUDIO_SEARCH:
			this.fileDelta = null;
			this.audioDelta = new AudioDelta(type, editorDelta);
			this.playlistDelta = null;
			break;
		case PLAYLIST_SAVED:
		case PLAYLIST_DELETED:
			this.fileDelta = null;
			this.audioDelta = null;
			this.playlistDelta = new PlaylistDelta();
			break;
		default:
			throw new IllegalStateException("Illegal action type: " + type);
		}
	}

}
