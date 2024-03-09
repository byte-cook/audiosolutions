package de.kobich.audiosolutions.frontend.file.view.browse.model;

import java.io.File;
import java.io.FileFilter;

/**
 * Only accepts directories.
 */
public class DirOnlyFileFilter implements FileFilter {

	/*
	 * (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return true;
		}
		return false;
	}

}
