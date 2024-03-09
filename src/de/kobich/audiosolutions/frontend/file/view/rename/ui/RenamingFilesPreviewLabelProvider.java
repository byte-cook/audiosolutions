package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.frontend.file.view.rename.RenameFilesView;
import de.kobich.audiosolutions.frontend.file.view.rename.model.RenameFileDescriptor;
import de.kobich.commons.ui.jface.table.ViewerColumn;

/**
 * Label provider for renaming files view.
 */
public class RenamingFilesPreviewLabelProvider extends LabelProvider implements ITableLabelProvider {
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

	public void addListener(ILabelProviderListener arg0) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	public void removeListener(ILabelProviderListener arg0) {
	}
}
