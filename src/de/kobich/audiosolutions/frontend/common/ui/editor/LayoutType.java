package de.kobich.audiosolutions.frontend.common.ui.editor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LayoutType {
	FLAT("Flat Layout"), HIERARCHICAL("Hierarchical Layout"), ALBUM("Album Layout");
	
	@Getter
	private final String label;
	
}