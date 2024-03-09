package de.kobich.audiosolutions.frontend.audio.view.artists.model;

import java.util.List;

/**
 * Lent mediums model.
 */
public class ArtistsModel {
	private List<ArtistItem> artistItems;
	
	public ArtistsModel(List<ArtistItem> artistItems) {
		this.artistItems = artistItems;
	}

	/**
	 * @return the artistItems
	 */
	public List<ArtistItem> getArtistItems() {
		return artistItems;
	}
}
