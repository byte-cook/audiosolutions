package de.kobich.audiosolutions.frontend.file.editor.filecollection.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.file.IFileDescriptorsSource;
import de.kobich.component.file.FileDescriptor;

/**
 * Tree node for relative path.
 */
public class RelativePathTreeNode extends AbstractTableTreeNode<String, FileDescriptorTreeNode> implements IFileDescriptorsSource, IAdaptable, Comparable<RelativePathTreeNode> {
	public RelativePathTreeNode(String path) {
		super(path);
	}

	@Override
	public Set<FileDescriptor> getFileDescriptors() {
		Set<FileDescriptor> list = new HashSet<FileDescriptor>();
		for (FileDescriptorTreeNode child : getChildren()) {	
			list.add(child.getContent());
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new RelativePathPropertySource(this);
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(RelativePathTreeNode o) {
		return this.getContent().compareToIgnoreCase(o.getContent());
	}
}
