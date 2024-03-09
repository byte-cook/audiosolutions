package de.kobich.audiosolutions.frontend.audio.view.id3.ui;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;
import de.kobich.audiosolutions.frontend.audio.view.id3.model.ID3TagItem;

public class ID3TagItemComparator implements Comparator<ID3TagItem> {
	private static final Map<MP3ID3TagType, Integer> order;
	
	static {
		order = new Hashtable<MP3ID3TagType, Integer>();
		order.put(MP3ID3TagType.ARTIST, 10);
		order.put(MP3ID3TagType.ALBUM, 20);
		order.put(MP3ID3TagType.ALBUM_YEAR, 21);
		order.put(MP3ID3TagType.GENRE, 30);
		order.put(MP3ID3TagType.TRACK, 40);
		order.put(MP3ID3TagType.FORMAT, 41);
		order.put(MP3ID3TagType.TRACK_NO, 42);
		order.put(MP3ID3TagType.COMMENT, 43);
		order.put(MP3ID3TagType.DURATION_SECONDS, 50);
		order.put(MP3ID3TagType.ENCODING_TYPE, 60);
		order.put(MP3ID3TagType.MP3_BITRATE, 61);
		order.put(MP3ID3TagType.MP3_CHANNELS, 62);
		order.put(MP3ID3TagType.SAMPLE_RATE, 63);
		
	}
	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ID3TagItem o1, ID3TagItem o2) {
		return order.get(o1.getKey()).compareTo(order.get(o2.getKey()));
	}

}
