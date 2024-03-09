package de.kobich.audiosolutions.frontend.file.view.browse.model;

import java.io.File;
import java.util.Comparator;


/**
 * Compares file tree nodes.
 * Sorts directories before files, otherwise alphabetical ignoring case.
 */
public class DirFirstFileTreeNodeComparator implements Comparator<FileTreeNode> {
	/**
	 * Constructor
	 */
	public DirFirstFileTreeNodeComparator() {}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(FileTreeNode fileNodeA, FileTreeNode fileNodeB) {
		File fileA = fileNodeA.getContent();
		File fileB = fileNodeB.getContent();
        if (fileA.isDirectory() && !fileB.isDirectory()) {
            return -1;

        } else if (!fileA.isDirectory() && fileB.isDirectory()) {
            return 1;

        } else {
            return fileA.getName().compareToIgnoreCase(fileB.getName());
        }

	}

}
