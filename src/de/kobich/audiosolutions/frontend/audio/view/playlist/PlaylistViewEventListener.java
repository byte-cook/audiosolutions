package de.kobich.audiosolutions.frontend.audio.view.playlist;

import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;

public class PlaylistViewEventListener extends EventListenerAdapter {
	private PlaylistView view;

	public PlaylistViewEventListener(PlaylistView view) {
		super(view, ListenerType.UI_EVENT);
		this.view = view;
	}
	
	@Override
	public void eventFired(UIEvent event) {
		switch (event.getActionType()) {
		case PLAYLIST_SAVED:
		case PLAYLIST_DELETED:
			this.view.refresh();
			break;
		default:
			break;
		}
	}
	
}