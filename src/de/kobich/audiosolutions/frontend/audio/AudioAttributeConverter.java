package de.kobich.audiosolutions.frontend.audio;

import java.util.HashMap;
import java.util.Map;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.commons.converter.IConverter;

public class AudioAttributeConverter implements IConverter<AudioAttribute, String> {
	public static final AudioAttributeConverter INSTANCE = new AudioAttributeConverter();
	private final Map<AudioAttribute, String> map;
	
	private AudioAttributeConverter() {
		this.map = new HashMap<AudioAttribute, String>();
		this.map.put(AudioAttribute.ALBUM, "Album");
		this.map.put(AudioAttribute.ALBUM_PUBLICATION, "Publication");
		this.map.put(AudioAttribute.ARTIST, "Artist");
		this.map.put(AudioAttribute.DISK, "Disk");
		this.map.put(AudioAttribute.GENRE, "Genre");
		this.map.put(AudioAttribute.MEDIUM, "Medium");
		this.map.put(AudioAttribute.RATING, "Rating");
		this.map.put(AudioAttribute.TRACK, "Track");
		this.map.put(AudioAttribute.TRACK_FORMAT, "Track Format");
		this.map.put(AudioAttribute.TRACK_NO, "Track No");
	}

	@Override
	public String convert(AudioAttribute s) {
		if (this.map.containsKey(s)) {
			return this.map.get(s);
		}
		throw new IllegalStateException("AudioAttribute not available: " + s);
	}

	@Override
	public AudioAttribute reconvert(String t) {
		if (this.map.containsValue(t)) {
			for (AudioAttribute key : this.map.keySet()) {
				if (this.map.get(key).equals(t)) {
					return key;
				}
			}
		}
		throw new IllegalStateException("String not available: " + t);
	}

}
