package de.kobich.audiosolutions.frontend.common;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import de.kobich.audiosolutions.core.service.AudioAttribute;


/**
 * Audio attribute comparator.
 */
public class AudioAttributeComparator implements Comparator<AudioAttribute> {
	private static final Map<AudioAttribute, Integer> order;
	
	static {
		order = new Hashtable<AudioAttribute, Integer>();
		order.put(AudioAttribute.ARTIST, 20);
		order.put(AudioAttribute.ALBUM, 30);
		order.put(AudioAttribute.ALBUM_PUBLICATION, 31);
		order.put(AudioAttribute.DISK, 32);
		order.put(AudioAttribute.GENRE, 40);
		order.put(AudioAttribute.TRACK, 50);
		order.put(AudioAttribute.TRACK_FORMAT, 51);
		order.put(AudioAttribute.TRACK_NO, 52);
		order.put(AudioAttribute.RATING, 60);
		order.put(AudioAttribute.MEDIUM, 70);
	}
	
	/**
	 * Constructor
	 */
	public AudioAttributeComparator() {}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(AudioAttribute o1, AudioAttribute o2) {
//		return o1.name().compareTo(o2.name());
		return order.get(o1).compareTo(order.get(o2));
	}

}
