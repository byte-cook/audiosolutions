package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.kobich.audiosolutions.frontend.file.view.rename.model.RenameFileDescriptor;
import de.kobich.commons.misc.rename.DefaultRenameableComparator;
import de.kobich.commons.misc.rename.IRenameable;

/**
 * Preview files comparator.
 */
public class RenameFilesPreviewComparator extends ViewerComparator {
	private static final Comparator<IRenameable> COMPARATOR = new DefaultRenameableComparator();
	
	/**
	 * Constructor
	 */
	public RenameFilesPreviewComparator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int rc = 0;
		if (e1 instanceof RenameFileDescriptor && e2 instanceof RenameFileDescriptor) {
			RenameFileDescriptor item1 = (RenameFileDescriptor) e1;
			RenameFileDescriptor item2 = (RenameFileDescriptor) e2;
			
			rc = COMPARATOR.compare(item1, item2);
		}
		return rc;
	}
}
