package de.kobich.audiosolutions.frontend.audio.view.playlist;

import java.util.List;

import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PlaylistModel {
	@Getter
	private List<Playlist> playlists;
	
}
