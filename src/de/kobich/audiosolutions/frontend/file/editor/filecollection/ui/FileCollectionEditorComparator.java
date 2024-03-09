package de.kobich.audiosolutions.frontend.file.editor.filecollection.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;

/**
 * Audio files sorter.
 */
public class FileCollectionEditorComparator extends ViewerComparator {
	public static enum Direction {
		ASCENDING, DESCENDING
	}

	private Direction direction;
	private FileCollectionEditorColumn column;

	/**
	 * Constructor
	 * @param defaultColumn
	 */
	public FileCollectionEditorComparator(FileCollectionEditorColumn defaultColumn) {
		this.column = defaultColumn;
	}

	/**
	 * Sets the column to sort
	 * @param column
	 */
	public Direction setSortColumn(FileCollectionEditorColumn column) {
		if (column.equals(this.column)) {
			// Same column as last sort; toggle the direction
			if (Direction.ASCENDING.equals(direction)) {
				direction = Direction.DESCENDING;
			}
			else {
				direction = Direction.ASCENDING;
			}
		}
		else {
			// New column; do an ascending sort
			this.column = column;
			direction = Direction.ASCENDING;
		}
		return direction;
	}

	/**
	 * Orders the items in such a way that books appear before moving boxes, which appear before board games.
	 */
	@Override
	public int category(Object element) {
		if (element instanceof RelativePathTreeNode) {
			return 1;
		}
		if (element instanceof FileDescriptorTreeNode) {
			return 2;
		}
		return 3;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);
		if (cat1 != cat2) {
			return cat1 - cat2;
		}
		
		int rc = 0;
		if (e1 instanceof RelativePathTreeNode && e2 instanceof RelativePathTreeNode) {
			RelativePathTreeNode file1 = (RelativePathTreeNode) e1;
			RelativePathTreeNode file2 = (RelativePathTreeNode) e2;

			// Determine which column and do the appropriate sort
			switch (column) {
				case FILE_NAME:
					rc = file1.getContent().compareToIgnoreCase(file2.getContent());
					break;
				default:
					rc = 0;
					break;
			}
		}
		else if (e1 instanceof FileDescriptorTreeNode && e2 instanceof FileDescriptorTreeNode) {
			FileDescriptorTreeNode file1 = (FileDescriptorTreeNode) e1;
			FileDescriptorTreeNode file2 = (FileDescriptorTreeNode) e2;

			// Determine which column and do the appropriate sort
			switch (column) {
				case FILE_NAME:
					rc = file1.getContent().getFileName().compareToIgnoreCase(file2.getContent().getFileName());
					break;
				case EXISTS:
					rc = Boolean.valueOf(file1.getContent().getFile().exists()).compareTo(Boolean.valueOf(file2.getContent().getFile().exists()));
					break;
				case RELATIVE_PATH:
					rc = file1.getContent().getRelativePath().compareToIgnoreCase(file2.getContent().getRelativePath());
					break;
				case EXTENSION:
					rc = file1.getContent().getExtension().compareToIgnoreCase(file2.getContent().getExtension());
					break;
				case SIZE:
					rc = file1.getContent().getFile().length() > file2.getContent().getFile().length() ? 1 : -1;
					break;
				case LAST_MODIFIED:
					rc = file1.getContent().getFile().lastModified() > file2.getContent().getFile().lastModified() ? 1 : -1;
					break;
			}
		}

		// If descending order, flip the direction
		if (Direction.DESCENDING.equals(direction)) {
			rc = -rc;
		}

		return rc;
	}
}
