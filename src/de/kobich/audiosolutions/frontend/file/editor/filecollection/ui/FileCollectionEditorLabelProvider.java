package de.kobich.audiosolutions.frontend.file.editor.filecollection.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.common.util.FileLabelUtil;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;
import de.kobich.component.file.FileDescriptor;

public class FileCollectionEditorLabelProvider extends LabelProvider implements ITableLabelProvider {
	private final boolean imageSupport;
	private Image folderImg;
	private Image fileImg;
	
	public FileCollectionEditorLabelProvider(boolean imageSupport) {
		this.imageSupport = imageSupport;
		if (imageSupport) {
			this.folderImg = Activator.getDefault().getImage(ImageKey.FOLDER);
			this.fileImg = Activator.getDefault().getImage(ImageKey.COMMON_FILE);
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!imageSupport) {
			return null;
		}
		FileCollectionEditorColumn column = FileCollectionEditorColumn.getByIndex(columnIndex);
		if (element instanceof RelativePathTreeNode) {
			if (FileCollectionEditorColumn.FILE_NAME.equals(column)) {
				return folderImg;
			}
		}
		else if (element instanceof FileDescriptorTreeNode) {
			// if (FileCollectionEditorColumn.EXISTS.equals(column)) {
			// FileDescriptorTreeNode treeNode = (FileDescriptorTreeNode) element;
			// if (treeNode.getContent().getFile().exists()) {
			// return Activator.getImageDescriptor("/icons/file/checked.png").createImage();
			// }
			// else {
			// return Activator.getImageDescriptor("/icons/file/unchecked.png").createImage();
			// }
			// }
			if (FileCollectionEditorColumn.FILE_NAME.equals(column)) {
				return fileImg;
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		FileCollectionEditorColumn column = FileCollectionEditorColumn.getByIndex(columnIndex);
		if (element instanceof RelativePathTreeNode) {
			RelativePathTreeNode treeNode = (RelativePathTreeNode) element;
			switch (column) {
				case FILE_NAME:
					return treeNode.getContent();
				default:
					return "";
			}
		}
		else if (element instanceof FileDescriptorTreeNode) {
			FileDescriptorTreeNode treeNode = (FileDescriptorTreeNode) element;
			FileDescriptor fileDescriptor = treeNode.getContent();
			switch (column) {
				case FILE_NAME:
					return fileDescriptor.getFileName();
				case EXISTS:
					return fileDescriptor.getFile().exists() ? "yes" : "no";
				case RELATIVE_PATH:
					return fileDescriptor.getRelativePath();
				case EXTENSION:
					return fileDescriptor.getExtension();
				case SIZE:
					return FileLabelUtil.getFileSizeLabel(fileDescriptor.getFile());
				case LAST_MODIFIED:
					return FileLabelUtil.getLastModifiedLabel(fileDescriptor.getFile());
			}
		}
		throw new IllegalStateException("Illegal column index < " + columnIndex + ">, expected<0 - 3>");
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {}

	@Override
	public void dispose() {
		if (imageSupport) {
//			this.fileImg.dispose();
//			this.folderImg.dispose();
		}
	}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {}

}
