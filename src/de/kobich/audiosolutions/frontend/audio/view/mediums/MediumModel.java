package de.kobich.audiosolutions.frontend.audio.view.mediums;

import java.util.List;

import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import lombok.Getter;

public class MediumModel {
	@Getter
	private List<Medium> mediums;
	
	public MediumModel(List<Medium> mediumItems) {
		this.mediums = mediumItems;
	}

}
