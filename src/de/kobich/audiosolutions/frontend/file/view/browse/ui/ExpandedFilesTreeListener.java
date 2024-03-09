package de.kobich.audiosolutions.frontend.file.view.browse.ui;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.TreeItem;

import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;
import de.kobich.audiosolutions.frontend.file.view.browse.model.FileTreeNode;

/**
 * Tree listener to determine all expanded elements. 
 */
public class ExpandedFilesTreeListener implements TreeListener {
	private Map<File, FileTreeNode> expandedElements;

	public ExpandedFilesTreeListener(BrowseFilesView view) {
		this.expandedElements = new HashMap<File, FileTreeNode>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.TreeListener#treeCollapsed(org.eclipse.swt.events.TreeEvent)
	 */
	public void treeCollapsed(TreeEvent e) {
		TreeItem item = (TreeItem) e.item;
		File file = getFile(item.getData());
		expandedElements.remove(file);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.TreeListener#treeExpanded(org.eclipse.swt.events.TreeEvent)
	 */
	public void treeExpanded(TreeEvent e) {
		TreeItem item = (TreeItem) e.item;
		FileTreeNode fileTreeNode = getFileTreeNode(item.getData());
		File file = getFile(item.getData());
		expandedElements.put(file, fileTreeNode);
	}

	/**
	 * @return the expandedElements
	 */
	public Collection<FileTreeNode> getExpandedElements() {
		return expandedElements.values();
	}
	
	/**
	 * Adds expanded element
	 * @param element
	 */
	public void expandElement(Object element) {
		FileTreeNode fileTreeNode = getFileTreeNode(element);
		File file = getFile(element);
		expandedElements.put(file, fileTreeNode);
	}
	
	/**
	 * Removes expanded element
	 * @param element
	 */
	public void collapseElement(Object element) {
		File file = getFile(element);
		expandedElements.remove(file);
	}
	
	/**
	 * Returns the file
	 * @param parentElement
	 * @return
	 */
	private File getFile(Object element) {
		File file = null;
		if (element instanceof File) {
			file = (File) element;
		}
		else if (element instanceof FileTreeNode) {
			file = ((FileTreeNode) element).getContent();
		}
		return file;
	}
	
	/**
	 * Returns the file
	 * @param parentElement
	 * @return
	 */
	protected FileTreeNode getFileTreeNode(Object element) {
		FileTreeNode parent = null;
		if (element instanceof FileTreeNode) {
			parent = ((FileTreeNode) element);
		}
		return parent;
	}
}
