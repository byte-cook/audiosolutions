package de.kobich.audiosolutions.frontend.audio.view.artists.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.core.service.persist.domain.Artist;

public class ArtistsLabelProvider implements ITableLabelProvider {
	public ArtistsLabelProvider() {}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Artist artist) {
			ArtistsColumnType column = ArtistsColumnType.getByIndex(columnIndex);
			switch (column) {
				case NAME:
					return artist.getName();
			}
		}
		throw new IllegalStateException("Illegal column index < " + columnIndex + ">, expected<0 - 1>");
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {}

}
