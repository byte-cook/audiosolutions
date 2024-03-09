package de.kobich.audiosolutions.frontend.file.view.browse.ui;

import java.io.File;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;
import de.kobich.audiosolutions.frontend.file.view.browse.model.FileTreeNode;

/**
 * Browse file label provider.
 */
public class BrowseFilesLabelProvider implements ILabelProvider {
	private FileIconProvider fileIconProvider;

	public BrowseFilesLabelProvider(BrowseFilesView view) {
		this.fileIconProvider = new FileIconProvider(view);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		File parent = getFile(element);
		return fileIconProvider.getImage(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		File parent = getFile(element);
		return parent.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener arg0) {}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		this.fileIconProvider.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener arg0) {}

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
