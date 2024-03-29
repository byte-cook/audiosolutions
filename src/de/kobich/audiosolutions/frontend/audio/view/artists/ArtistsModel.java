package de.kobich.audiosolutions.frontend.audio.view.artists;

import java.util.List;

import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import lombok.Getter;

public class ArtistsModel {
	@Getter
	private List<Artist> artists;
	
	public ArtistsModel(List<Artist> artistItems) {
		this.artists = artistItems;
	}
}
