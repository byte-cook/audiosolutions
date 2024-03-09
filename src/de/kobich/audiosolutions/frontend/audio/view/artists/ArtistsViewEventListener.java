package de.kobich.audiosolutions.frontend.audio.view.artists;

import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;

public class ArtistsViewEventListener extends EventListenerAdapter {
	private ArtistsView view;

	public ArtistsViewEventListener(ArtistsView view) {
		super(view, ListenerType.UI_EVENT);
		this.view = view;
	}
	
	@Override
	public void eventFired(UIEvent event) {
		switch (event.getActionType()) {
		case AUDIO_SAVED:
			view.refresh();
			break;
		default:
			break;
		}
	}
}