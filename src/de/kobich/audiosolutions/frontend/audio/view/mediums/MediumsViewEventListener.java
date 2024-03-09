package de.kobich.audiosolutions.frontend.audio.view.mediums;

import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;

public class MediumsViewEventListener extends EventListenerAdapter {
	private MediumsView view;

	public MediumsViewEventListener(MediumsView view) {
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