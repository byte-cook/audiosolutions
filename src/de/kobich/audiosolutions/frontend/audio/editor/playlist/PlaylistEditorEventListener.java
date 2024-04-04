package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;

public class PlaylistEditorEventListener extends EventListenerAdapter {
	private final PlaylistEditor editor;
	
	public PlaylistEditorEventListener(PlaylistEditor editor) {
		super(editor, ListenerType.UI_EVENT);
		this.editor = editor;
	}
	
	@Override
	public void eventFired(final UIEvent event) {
		switch (event.getActionType()) {
		case PLAYLIST_DELETED:
			if (event.getPlaylistDelta().getPlaylistIds().contains(editor.getPlaylist().getId().orElse(null))) {
				editor.getEditorSite().getPage().closeEditor(editor, false);
				
			}
			break;
		default:
			break;
		}
	}

}
