package de.kobich.audiosolutions.frontend.audio.view.mediums.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.kobich.audiosolutions.frontend.audio.view.mediums.model.MediumItem;
import de.kobich.commons.collections.NaturalSortStringComparator;

/**
 * Audio files sorter.
 */
public class MediumComparator extends ViewerComparator {
	public static enum Direction {
		ASCENDING, DESCENDING
	}

	private Direction direction;
	private MediumColumnType column;

	/**
	 * Constructor
	 * @param defaultColumn
	 */
	public MediumComparator(MediumColumnType defaultColumn) {
		this.column = defaultColumn;
	}

	/**
	 * Sets the column to sort
	 * @param column
	 */
	public Direction setSortColumn(MediumColumnType column) {
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
		if (e1 instanceof MediumItem && e2 instanceof MediumItem) {
			MediumItem item1 = (MediumItem) e1;
			MediumItem item2 = (MediumItem) e2;

			// Determine which column and do the appropriate sort
			switch (column) {
				case MEDIUM:
					rc = NaturalSortStringComparator.INSTANCE.compare(item1.getMedium().getName().toLowerCase(), item2.getMedium().getName().toLowerCase());
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
