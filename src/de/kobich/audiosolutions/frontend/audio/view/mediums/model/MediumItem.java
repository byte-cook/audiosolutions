package de.kobich.audiosolutions.frontend.audio.view.mediums.model;

import de.kobich.audiosolutions.core.service.persist.domain.Medium;

/**
 * Lent medium. 
 */
public class MediumItem {
	private final Medium medium;

	/**
	 * Constructor
	 * @param medium the lent medium
	 */
	public MediumItem(Medium medium) {
		this.medium = medium;
	}

	/**
	 * @return the medium
	 */
	public Medium getMedium() {
		return medium;
	}
}
