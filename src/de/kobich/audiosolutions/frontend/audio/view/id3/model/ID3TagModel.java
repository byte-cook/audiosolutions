package de.kobich.audiosolutions.frontend.audio.view.id3.model;

import java.util.List;
import java.util.Set;

import de.kobich.component.file.FileDescriptor;

/**
 * ID3 tag model.
 */
public class ID3TagModel {
	private Set<FileDescriptor> mp3Files;
	private List<ID3TagItem> id3TagItems;
	
	public ID3TagModel(Set<FileDescriptor> mp3Files, List<ID3TagItem> audioDataItems) {
		this.mp3Files = mp3Files;
		this.id3TagItems = audioDataItems;
	}

	/**
	 * @return the audioDataItems
	 */
	public List<ID3TagItem> getID3TagItems() {
		return id3TagItems;
	}

	/**
	 * @return the mp3Files
	 */
	public Set<FileDescriptor> getMp3Files() {
		return mp3Files;
	}
	
}
