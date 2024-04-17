package de.kobich.audiosolutions.frontend.audio.view.play;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

import lombok.Getter;

/**
 * Audio Play View source provider.
 */
@Getter
public class AudioPlayViewSourceProvider extends AbstractSourceProvider {
	private static final String PLAYING_STATE = "audioPlayView.playing";
	private static final String PAUSE_STATE = "audioPlayView.pause";
	private static final String PLAYLIST_EMPTY_STATE = "audioPlayView.playListEmpty";
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { PLAYING_STATE, PAUSE_STATE, PLAYLIST_EMPTY_STATE };
	
	private boolean playing;
	private boolean paused;
	private boolean playlistEmpty;
	
	public static AudioPlayViewSourceProvider getInstance() {
		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		return (AudioPlayViewSourceProvider) sourceProviderService.getSourceProvider(PLAYING_STATE);
	}
	
	@Override
	public void initialize(final IServiceLocator locator) {
		playing = false;
		paused = false;
		playlistEmpty = true;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		map.put(PLAYING_STATE, playing);
		map.put(PAUSE_STATE, paused);
		map.put(PLAYLIST_EMPTY_STATE, playlistEmpty);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}
	
	@Override
	public void dispose() {}
	
	public void setPlaying(boolean b) {
		playing = b;
		fireSourceChanged(ISources.WORKBENCH, PLAYING_STATE, b);
	}

	public void setPaused(boolean b) {
		paused = b;
		fireSourceChanged(ISources.WORKBENCH, PAUSE_STATE, b);
	}

	public void setPlaylistEmpty(boolean b) {
		playlistEmpty = b;
		fireSourceChanged(ISources.WORKBENCH, PLAYLIST_EMPTY_STATE, b);
	}

}
