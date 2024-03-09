package de.kobich.audiosolutions.frontend.audio.view.mediums.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.frontend.audio.view.mediums.model.MediumItem;

public class MediumLabelProvider implements ITableLabelProvider {
	public MediumLabelProvider() {}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof MediumItem) {
			MediumItem item = (MediumItem) element;
			MediumColumnType column = MediumColumnType.getByIndex(columnIndex);
			switch (column) {
				case MEDIUM:
					return item.getMedium().getName();
				case BORROWER:
					return item.getMedium().getBorrower();
				case DATE:
					Date date = item.getMedium().getBorrowingDate();
					if (date != null) {
						DateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						return parser.format(date);
					}
					return null;
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
