package de.kobich.audiosolutions.frontend.audio.view.statistic.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class AudioStatisticLabelProvider implements ITableLabelProvider {
	public AudioStatisticLabelProvider() {}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof AudioStatisticItem) {
			AudioStatisticItem item = (AudioStatisticItem) element;
			AudioStatisticColumnType column = AudioStatisticColumnType.getByIndex(columnIndex);
			switch (column) {
				case TYPE:
					return item.getLabel();
				case COUNT:
					NumberFormat format = NumberFormat.getInstance();
					if (format instanceof DecimalFormat) {
						DecimalFormat decimalFormat = (DecimalFormat) format;
						decimalFormat.applyPattern("#,###");
					}
					return format.format(item.getCount());
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
