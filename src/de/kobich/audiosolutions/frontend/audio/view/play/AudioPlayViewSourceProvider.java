package de.kobich.audiosolutions.frontend.audio.view.play;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Audio Play View source provider.
 */
public class AudioPlayViewSourceProvider extends AbstractSourceProvider {
	private static Map<String, Boolean> map;
	private static String[] sourceNames;
	// variables
	public static final String PLAYING_STATE = "audioPlayView.playing";
	public static final String PAUSE_STATE = "audioPlayView.pause";
	public static final String PLAYLIST_EMPTY_STATE = "audioPlayView.playListEmpty";
	
	@Override
	public void initialize(final IServiceLocator locator) {
		map = new HashMap<String, Boolean>();
		map.put(PLAYING_STATE, Boolean.FALSE);
		map.put(PAUSE_STATE, Boolean.FALSE);
		map.put(PLAYLIST_EMPTY_STATE, Boolean.TRUE);
	}
	
	@Override
	public void dispose() {}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		return getMap();
	}

	@Override
	public String[] getProvidedSourceNames() {
		if (sourceNames == null) {
			sourceNames = new String[] {PLAYING_STATE, PAUSE_STATE, PLAYLIST_EMPTY_STATE };
		}
		return sourceNames;
	}

	/**
	 * Changes a given state
	 * @param state
	 * @param value
	 */
	public void changeState(String state, Boolean value) {
		getMap().put(state, value);
		fireSourceChanged(ISources.WORKBENCH, state, value);
	}
	
	/**
	 * Returns the state map
	 * @return
	 */
	protected static Map<String, Boolean> getMap() {
		return map;
	}
}
