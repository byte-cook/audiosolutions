package de.kobich.audiosolutions.frontend.audio.editor.search;

import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;

/**
 * Audio search editor event listener.
 */
public class AudioSearchEditorEventListener extends EventListenerAdapter {
	private AudioSearchEditor editor;

	/**
	 * Constructor
	 */
	public AudioSearchEditorEventListener(AudioSearchEditor editor) {
		super(editor, ListenerType.UI_EVENT);
		this.editor = editor;
	}

	@Override
	public void eventFired(final UIEvent event) {
		switch (event.getActionType()) {
		case AUDIO_SAVED:
			editor.startSearch();
			break;
		default:
			break;
		}
	}
}
