package de.kobich.audiosolutions.frontend.audio.view.id3.model;

import java.util.Set;

import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;
import de.kobich.component.file.FileDescriptor;

/**
 * ID3 tag item can contain several files. 
 */
public class ID3TagItem {
	private static final String SEVERAL_VALUES = "<several-values>";
	private MP3ID3TagType key;
	private Set<String> values;
	private Set<FileDescriptor> fileDescriptors;

	/**
	 * Constructor
	 * @param key
	 * @param values
	 * @param fileDescriptors
	 */
	public ID3TagItem(MP3ID3TagType key, Set<String> values, Set<FileDescriptor> fileDescriptors) {
		this.key = key;
		this.values = values;
		this.fileDescriptors = fileDescriptors;
	}
	
	/**
	 * @return the key
	 */
	public MP3ID3TagType getKey() {
		return key;
	}
	
	/**
	 * @return the value
	 */
	public String getLabel() {
		if (values.size() == 1) {
			return values.iterator().next();
		}
		else if (values.isEmpty()) {
			return "";
		}
		return SEVERAL_VALUES;
	}
	
	/**
	 * @return the values
	 */
	public Set<String> getValues() {
		return values;
	}
	
	/**
	 * @param values the values to set
	 */
	public void setValues(Set<String> values) {
		this.values = values;
	}

	/**
	 * @return the files
	 */
	public Set<FileDescriptor> getFileDescriptors() {
		return fileDescriptors;
	}
}
