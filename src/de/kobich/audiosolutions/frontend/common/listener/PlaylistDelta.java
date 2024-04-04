package de.kobich.audiosolutions.frontend.common.listener;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public class PlaylistDelta {
	@Getter
	private final Set<Long> playlistIds;
	
	public PlaylistDelta() {
		this.playlistIds = new HashSet<>();
	}
}
