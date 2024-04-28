package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.kobich.audiosolutions.frontend.file.view.rename.RenameFilesView;
import de.kobich.commons.ui.jface.table.ViewerColumn;
import de.kobich.component.file.descriptor.RenameFileDescriptor;

/**
 * Label provider for renaming files view.
 */
public class RenamingFilesPreviewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
	public RenamingFilesPreviewLabelProvider() {}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof RenameFileDescriptor) {
			RenameFileDescriptor previewData = ((RenameFileDescriptor) element);
			ViewerColumn column = RenameFilesView.COLUMNS.getByIndex(columnIndex);
			if (RenameFilesView.COLUMN_FILE_NAME.equals(column)) {
				return previewData.getFileDescriptor().getFileName();
			}
			else if (RenameFilesView.COLUMN_NEW_NAME.equals(column)) {
				return previewData.getName();
			}
		}
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		if (element instanceof RenameFileDescriptor renameFileDesc) {
			ViewerColumn column = RenameFilesView.COLUMNS.getByIndex(columnIndex);
			if (RenameFilesView.COLUMN_NEW_NAME.equals(column)) {
				if (renameFileDesc.getOriginalName().equals(renameFileDesc.getName())) {
					return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
				}
			}
		}
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}
}
