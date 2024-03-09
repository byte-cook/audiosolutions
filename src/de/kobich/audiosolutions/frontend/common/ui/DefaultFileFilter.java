package de.kobich.audiosolutions.frontend.common.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;

import de.kobich.commons.utils.RelativePathUtils;

public class DefaultFileFilter implements FileFilter, Serializable {
	private static final long serialVersionUID = 8125893860090211676L;
	private String filter;
	
	public DefaultFileFilter(String filter) {
		if (StringUtils.isNotEmpty(filter)) {
			this.filter = RelativePathUtils.convertBackslashToSlash(filter);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File file) {
		if (filter != null && file.isFile()) {
			String path = file.getAbsolutePath();
			path = RelativePathUtils.convertBackslashToSlash(path);
			return FilenameUtils.wildcardMatch(path, filter, IOCase.INSENSITIVE);
		}
		return true;
	}
}
