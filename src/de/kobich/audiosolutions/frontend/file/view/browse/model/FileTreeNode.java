package de.kobich.audiosolutions.frontend.file.view.browse.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import de.kobich.audiosolutions.frontend.file.IFileSource;

/**
 * File tree node.
 */
public class FileTreeNode implements IFileSource, IAdaptable {
	private File content;
	private List<FileTreeNode> children;
	private List<File> files;
	 
	public FileTreeNode(File file) {
		this.content = file;
		this.children = new ArrayList<FileTreeNode>();
	}
	
	/**
	 * @return the children
	 */
	public List<FileTreeNode> getChildren() {
		return children;
	}

	/**
	 * @return the content
	 */
	public File getContent() {
		return content;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new FilePropertySource(this);
		}
		return null;
	}

	@Override
	public List<File> getFiles() {
		if (files == null) {
			files = new ArrayList<File>();
			files.add(getContent());
		}
		return files;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FileTreeNode other = (FileTreeNode) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		}
		else if (!content.equals(other.content))
			return false;
		return true;
	}
}
