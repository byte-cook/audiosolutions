package de.kobich.audiosolutions.frontend.audio.view.id3.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.frontend.audio.view.id3.model.ID3TagItem;

public class ID3TagLabelProvider implements ITableLabelProvider {
	public ID3TagLabelProvider() {}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ID3TagItem) {
			ID3TagItem item = (ID3TagItem) element;
			ID3TagColumnType column = ID3TagColumnType.getByIndex(columnIndex);
			switch (column) {
				case TAG:
					return item.getKey().getLabel();
				case VALUE:
					return item.getLabel();
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
