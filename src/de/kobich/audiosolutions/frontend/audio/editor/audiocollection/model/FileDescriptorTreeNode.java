package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.file.IFileDescriptorsSource;
import de.kobich.component.file.FileDescriptor;

public class FileDescriptorTreeNode extends AbstractTableTreeNode<FileDescriptor, AbstractTableTreeNode<?, ?>> implements IFileDescriptorsSource, IAdaptable, Comparable<FileDescriptorTreeNode> {
	public FileDescriptorTreeNode(FileDescriptor fileDescriptor) {
		super(fileDescriptor);
	}

	@Override
	public Set<FileDescriptor> getFileDescriptors() {
		Set<FileDescriptor> list = new HashSet<FileDescriptor>();
		list.add(getContent());
		return list;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new FileDescriptorPropertySource(this);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(FileDescriptorTreeNode o) {
		return this.getContent().getFileName().compareToIgnoreCase(o.getContent().getFileName());
	}
}
