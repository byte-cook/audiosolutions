package de.kobich.audiosolutions.frontend.audio.view.artists.model;

import de.kobich.audiosolutions.core.service.persist.domain.Artist;

/**
 * Audio artist. 
 */
public class ArtistItem {
	private final Artist artist;

	/**
	 * Constructor
	 * @param artist the lent artist
	 */
	public ArtistItem(Artist artist) {
		this.artist = artist;
	}

	/**
	 * @return the artist
	 */
	public Artist getArtist() {
		return artist;
	}
}
