package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.file.IFileDescriptorsSource;
import de.kobich.component.file.FileDescriptor;
import lombok.Setter;

/**
 * Tree node for albums.
 */
public class AlbumTreeNode extends AbstractTableTreeNode<Long, FileDescriptorTreeNode> implements IFileDescriptorsSource, IAdaptable, Comparable<AlbumTreeNode> {
	@Setter
	private Album album;
	
	public AlbumTreeNode(Album album) {
		super(album != null ? album.getId() : null);
		this.album = album;
	}
	
	public String getLabel() {
		if (album != null) {
			final String artistName = album.getArtist().map(Artist::getName).orElse("Various Artists");
			return artistName + " - " + album.getName();
		}
		else {
			return "<No album>"; 
		}
	}
	
	public Optional<Long> getAlbumId() {
		return Optional.ofNullable(getContent());
	}
	
	public Optional<String> getAlbumName() {
		if (album != null) {
			return Optional.of(album.getName());
		}
		return Optional.empty();
	}
	
	public Optional<String> getArtistName() {
		if (album != null) {
			return album.getArtist().map(Artist::getName);
		}
		return Optional.empty();
	}
	
	public Optional<Date> getAlbumPublication() {
		if (album != null) {
			return Optional.ofNullable(album.getPublication());
		}
		return Optional.empty();
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new AlbumPropertySource(this);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(AlbumTreeNode o) {
		return Long.compare(this.getContent(), o.getContent());
	}
}
