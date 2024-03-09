package de.kobich.audiosolutions.frontend.audio.view.id3.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.kobich.audiosolutions.frontend.audio.view.id3.model.ID3TagItem;

/**
 * Audio files sorter.
 */
public class ID3TagComparator extends ViewerComparator {
	public static enum Direction {
		ASCENDING, DESCENDING
	}

	private Direction direction;
	private ID3TagColumnType column;
	private ID3TagItemComparator comparator;

	/**
	 * Constructor
	 * @param defaultColumn
	 */
	public ID3TagComparator(ID3TagColumnType defaultColumn) {
		this.column = defaultColumn;
		this.comparator = new ID3TagItemComparator();
	}

	/**
	 * Sets the column to sort
	 * @param column
	 */
	public Direction setSortColumn(ID3TagColumnType column) {
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
		if (e1 instanceof ID3TagItem && e2 instanceof ID3TagItem) {
			ID3TagItem item1 = (ID3TagItem) e1;
			ID3TagItem item2 = (ID3TagItem) e2;

			// Determine which column and do the appropriate sort
			switch (column) {
				case TAG:
					rc = comparator.compare(item1, item2);
					break;
				default:
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
