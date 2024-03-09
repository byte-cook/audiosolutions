package de.kobich.audiosolutions.frontend.audio.view.statistic.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioStatistics;

public class AudioStatisticContentProvider implements IStructuredContentProvider {

	public AudioStatisticContentProvider() {}

	@Override
	public Object[] getElements(Object input) {
		if (input instanceof AudioStatistics stat) {
			List<AudioStatisticItem> items = new ArrayList<>();
			items.add(new AudioStatisticItem(AudioAttribute.MEDIUM, "Medium", stat.getMediumCount()));
			items.add(new AudioStatisticItem(AudioAttribute.GENRE, "Genre", stat.getGenreCount()));
			items.add(new AudioStatisticItem(AudioAttribute.ARTIST, "Artist", stat.getArtistCount()));
			items.add(new AudioStatisticItem(AudioAttribute.ALBUM, "Album", stat.getAlbumCount()));
			items.add(new AudioStatisticItem(AudioAttribute.TRACK, "Track", stat.getTrackCount()));
			return items.toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + AudioStatistics.class.getName() + ">");
	}

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

}
