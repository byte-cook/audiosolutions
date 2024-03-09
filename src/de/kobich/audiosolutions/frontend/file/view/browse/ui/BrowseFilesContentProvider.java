package de.kobich.audiosolutions.frontend.file.view.browse.ui;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;
import de.kobich.audiosolutions.frontend.file.view.browse.model.DirOnlyFileFilter;
import de.kobich.audiosolutions.frontend.file.view.browse.model.FileTreeNode;

/**
 * Browse file content provider.
 */
public class BrowseFilesContentProvider implements ITreeContentProvider {
	private static final FileFilter DIR_ONLY_FILE_FILTER = new DirOnlyFileFilter();
	private BrowseFilesView view;
	
	public BrowseFilesContentProvider(BrowseFilesView view) {
		this.view = view;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		File parent = getFile(parentElement);
		File[] files = ((File) parent).listFiles(getFileFilter());
		if (files == null) {
			return new Object[0];
		}
		FileTreeNode[] fileNodes = new FileTreeNode[files.length];
		for (int i = 0; i < files.length; ++ i) {
			fileNodes[i] = new FileTreeNode(files[i]);
		}
		Arrays.sort(fileNodes, view.getComparator());
		return fileNodes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof File) {
			return ((File) element).getParentFile();
		}
		else if (element instanceof FileTreeNode) {
			return ((FileTreeNode) element).getContent().getParentFile();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		File file = getFile(element);
		File[] files = file.listFiles(getFileFilter());
		if (files == null || files.length == 0) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		File file = getFile(inputElement);
		File[] files = ((File) file).listFiles(getFileFilter());
		if (files == null) {
			return new Object[0];
		}
		FileTreeNode[] fileNodes = new FileTreeNode[files.length];
		for (int i = 0; i < files.length; ++ i) {
			fileNodes[i] = new FileTreeNode(files[i]);
		}
		Arrays.sort(fileNodes, view.getComparator());
		return fileNodes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		this.view = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	/**
	 * Returns the file filter
	 * @return
	 */
	protected FileFilter getFileFilter() {
		if (view.isFilesVisible()) {
			return null;
		}
		return DIR_ONLY_FILE_FILTER;
	}
	
	/**
	 * Returns the file
	 * @param parentElement
	 * @return
	 */
	protected File getFile(Object parentElement) {
		File parent = null;
		if (parentElement instanceof File) {
			parent = (File) parentElement;
		}
		else if (parentElement instanceof FileTreeNode) {
			parent = ((FileTreeNode) parentElement).getContent();
		}
		return parent;
	}
}
