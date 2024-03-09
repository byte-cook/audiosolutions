package de.kobich.audiosolutions.frontend.audio.view.mediums.model;

import java.util.Set;

/**
 * Lent mediums model.
 */
public class MediumModel {
	private Set<MediumItem> mediumItems;
	
	public MediumModel(Set<MediumItem> mediumItems) {
		this.mediumItems = mediumItems;
	}

	/**
	 * @return the audioDataItems
	 */
	public Set<MediumItem> getMediumItems() {
		return mediumItems;
	}
}
