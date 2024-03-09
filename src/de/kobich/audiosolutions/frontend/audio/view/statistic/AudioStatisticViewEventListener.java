package de.kobich.audiosolutions.frontend.audio.view.statistic;

import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;

public class AudioStatisticViewEventListener extends EventListenerAdapter {
	private AudioStatisticView view;

	public AudioStatisticViewEventListener(AudioStatisticView view) {
		super(view, ListenerType.UI_EVENT);
		this.view = view;
	}
	
	@Override
	public void eventFired(UIEvent event) {
		switch (event.getActionType()) {
		case AUDIO_SAVED:
			view.refreshView();
			break;
		default:
			break;
		}
	}
}