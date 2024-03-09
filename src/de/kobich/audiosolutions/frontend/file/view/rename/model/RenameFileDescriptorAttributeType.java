package de.kobich.audiosolutions.frontend.file.view.rename.model;

import java.util.HashSet;
import java.util.Set;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;

public enum RenameFileDescriptorAttributeType {
	FILE_SIZE("<file-size>"),
	FILE_BASE_NAME("<file-base-name>"),
	FILE_EXTENSION("<file-extension>"),
	FILE_MODIFIED("<file-modified>"),
	AUDIO_ALBUM("<audio-album>", AudioAttribute.ALBUM),
	AUDIO_ALBUM_PUBLICATION("<audio-album-publication>", AudioAttribute.ALBUM_PUBLICATION),
	AUDIO_ARTIST("<audio-artist>", AudioAttribute.ARTIST),
	AUDIO_DISK("<audio-disk>", AudioAttribute.DISK),
	AUDIO_MEDIUM("<audio-medium>", AudioAttribute.MEDIUM),
	AUDIO_GENRE("<audio-genre>", AudioAttribute.GENRE),
	AUDIO_TRACK("<audio-track>", AudioAttribute.TRACK),
	AUDIO_TRACK_NO("<audio-track-no>", AudioAttribute.TRACK_NO),
	ID3_ALBUM("<id3-album>", MP3ID3TagType.ALBUM),
	ID3_ALBUM_YEAR("<id3-album-year>", MP3ID3TagType.ALBUM_YEAR),
	ID3_ARTIST("<id3-artist>", MP3ID3TagType.ARTIST),
	ID3_GENRE("<id3-genre>", MP3ID3TagType.GENRE),
	ID3_TRACK("<id3-track>", MP3ID3TagType.TRACK),
	ID3_TRACK_NO("<id3-track-no>", MP3ID3TagType.TRACK_NO),
	;
	
	private final String name;
	private final MP3ID3TagType tagType;
	private final AudioAttribute attribute;
	
	private RenameFileDescriptorAttributeType(String name) {
		this(name, null, null);
	}
	private RenameFileDescriptorAttributeType(String name, AudioAttribute attribute) {
		this(name, attribute, null);
	}
	private RenameFileDescriptorAttributeType(String name, MP3ID3TagType tagType) {
		this(name, null, tagType);
	}
	private RenameFileDescriptorAttributeType(String name, AudioAttribute attribute, MP3ID3TagType tagType) {
		this.name = name;
		this.tagType = tagType;
		this.attribute = attribute;
	}
	
	public MP3ID3TagType getTagType() {
		return tagType;
	}

	public String getName() {
		return name;
	}

	public AudioAttribute getAttribute() {
		return attribute;
	}
	public static RenameFileDescriptorAttributeType getByName(String name) {
		for (RenameFileDescriptorAttributeType md : RenameFileDescriptorAttributeType.values()) {
			if (md.getName().equals(name)) {
				return md;
			}
		}
		return null;
	}
	
	public static Set<String> getNames() {
		Set<String> names = new HashSet<String>();
		for (RenameFileDescriptorAttributeType md : RenameFileDescriptorAttributeType.values()) {
			names.add(md.getName());
		}
		return names;
	}
}
