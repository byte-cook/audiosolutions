package de.kobich.audiosolutions.frontend.audio.view.mediums.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.kobich.audiosolutions.core.service.persist.domain.Medium;
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
		if (e1 instanceof Medium item1 && e2 instanceof Medium item2) {
			// Determine which column and do the appropriate sort
			switch (column) {
				case MEDIUM:
					rc = NaturalSortStringComparator.INSTANCE.compare(item1.getName().toLowerCase(), item2.getName().toLowerCase());
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
