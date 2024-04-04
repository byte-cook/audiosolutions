package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;

public class PlaylistEditorComparator extends ViewerComparator {
	public static enum Direction {
		ASCENDING, DESCENDING
	}

	private Direction direction;
	private PlaylistEditorColumn column;

	/**
	 * Constructor
	 * @param defaultColumn
	 */
	public PlaylistEditorComparator(PlaylistEditorColumn defaultColumn) {
		this.column = defaultColumn;
	}

	/**
	 * Sets the column to sort
	 * @param column
	 */
	public Direction setSortColumn(PlaylistEditorColumn column) {
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
		if (element instanceof EditablePlaylistFolder) {
			return 1;
		}
		if (element instanceof EditablePlaylistFile) {
			return 2;
		}
		return 10;
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
		if (e1 instanceof EditablePlaylistFolder folder1 && e2 instanceof EditablePlaylistFolder folder2) {
			// Determine which column and do the appropriate sort
			switch (column) {
				case NAME:
					rc = folder1.getPath().compareToIgnoreCase(folder2.getPath());
					break;
				case FILE_COUNT:
					rc = Integer.compare(folder1.getFiles().size(), folder2.getFiles().size());
					break;
				default:
					rc = 0;
					break;
			}
		}
		else if (e1 instanceof EditablePlaylistFile file1 && e2 instanceof EditablePlaylistFile file2) {
			// Determine which column and do the appropriate sort
			switch (column) {
				case NAME:
					rc = file1.getFileName().compareToIgnoreCase(file2.getFileName());
					break;
				case FOLDER:
					rc = file1.getFolder().getPath().compareToIgnoreCase(file2.getFolder().getPath());
					break;
				case FILE_COUNT:
					rc = 0;
					break;
				case FILE_EXISTS:
					rc = Boolean.compare(file1.getFile().exists(), file2.getFile().exists());
					break;
				case FILE_PATH:
					rc = file1.getFile().compareTo(file2.getFile());
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
