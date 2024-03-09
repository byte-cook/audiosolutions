package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.file.IFileDescriptorsSource;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.component.file.FileDescriptor;

/**
 * Tree node for artists.
 */
public class ArtistTreeNode extends AbstractTableTreeNode<String, FileDescriptorTreeNode> implements IFileDescriptorsSource, IAdaptable, Comparable<ArtistTreeNode> {
	public ArtistTreeNode(String name) {
		super(name);
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
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new ArtistPropertySource(this);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ArtistTreeNode o) {
		return this.getContent().compareToIgnoreCase(o.getContent());
	}
}
