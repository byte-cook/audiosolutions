package de.kobich.audiosolutions.frontend.audio.view.statistic.ui;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class AudioStatisticItem {
	private final AudioAttribute attribute;
	private final String label;
	@Setter
	private long count;

	
}
