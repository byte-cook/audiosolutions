package de.kobich.audiosolutions.frontend.file.view.browse.ui;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.kobich.audiosolutions.frontend.file.view.browse.model.FileTreeNode;

/**
 * File filter.
 */
public class BrowseFilesViewerFilter extends ViewerFilter {
	private String wildcardMatcher;

	public BrowseFilesViewerFilter() {
		this.wildcardMatcher = "*";
	}

	/**
	 * @param wildcardMatcher the wildcardMatcher to set
	 */
	public void setWildcardMatcher(String wildcardMatcher) {
		this.wildcardMatcher = wildcardMatcher;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof FileTreeNode) {
			FileTreeNode node = (FileTreeNode) element;
			if (node.getContent().isFile()) {
				String fileName = node.getContent().getName();
				if (FilenameUtils.wildcardMatch(fileName, wildcardMatcher, IOCase.INSENSITIVE)) {
					return true;
				}
			}
			else {
				return true;
			}
		}
		return false;
	}

}
