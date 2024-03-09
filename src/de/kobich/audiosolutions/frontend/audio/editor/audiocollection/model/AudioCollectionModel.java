package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditorModel;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;
import de.kobich.commons.collections.Dimension.DimensionType;
import de.kobich.commons.collections.DimensionMap2D;
import de.kobich.commons.utils.RelativePathUtils;
import de.kobich.component.file.FileDescriptor;

public class AudioCollectionModel implements ICollectionEditorModel {
	private FileCollection fileCollection;
	private Set<RelativePathTreeNode> paths;
	private Set<AlbumTreeNode> albums;
	private Set<ArtistTreeNode> artists;
	private Set<FileDescriptorTreeNode> files;

	public AudioCollectionModel(FileCollection fileCollection) {
		this.fileCollection = fileCollection;
		this.files = new HashSet<FileDescriptorTreeNode>();
		DimensionMap2D<String, FileDescriptorTreeNode> path2Files = new DimensionMap2D<String, FileDescriptorTreeNode>(DimensionType.LIST);
		DimensionMap2D<String, FileDescriptorTreeNode> album2Files = new DimensionMap2D<String, FileDescriptorTreeNode>(DimensionType.LIST);
		DimensionMap2D<String, FileDescriptorTreeNode> artist2Files = new DimensionMap2D<String, FileDescriptorTreeNode>(DimensionType.LIST);
		for (FileDescriptor fileDescriptor : fileCollection.getFileDescriptors()) {
			FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
			files.add(fileDescriptorNode);

			// path
			String parentPath = getParentPath(fileDescriptor);
			path2Files.addElement(parentPath, fileDescriptorNode);

			// album
			String album = getAudioDataValue(fileDescriptor, AudioAttribute.ALBUM);
			album2Files.addElement(album, fileDescriptorNode);

			// artist
			String artist = getAudioDataValue(fileDescriptor, AudioAttribute.ARTIST);
			artist2Files.addElement(artist, fileDescriptorNode);
		}

		// path
		paths = new HashSet<RelativePathTreeNode>();
		for (String path : path2Files.keySet()) {
			RelativePathTreeNode pathNode = new RelativePathTreeNode(path);
			List<FileDescriptorTreeNode> children = path2Files.get(path).asList();
			pathNode.getChildren().addAll(children);
			paths.add(pathNode);
		}

		// album
		albums = new HashSet<AlbumTreeNode>();
		for (String album : album2Files.keySet()) {
			AlbumTreeNode albumNode = new AlbumTreeNode(album);
			List<FileDescriptorTreeNode> children = album2Files.get(album).asList();
			albumNode.getChildren().addAll(children);
			albums.add(albumNode);
		}

		// artist
		artists = new HashSet<ArtistTreeNode>();
		for (String artist : artist2Files.keySet()) {
			ArtistTreeNode artistNode = new ArtistTreeNode(artist);
			List<FileDescriptorTreeNode> children = artist2Files.get(artist).asList();
			artistNode.getChildren().addAll(children);
			artists.add(artistNode);
		}
	}

	@Override
	public LayoutDelta updateModel(CollectionEditorDelta delta) {
		// update file collection
		this.fileCollection.addFileDescriptors(delta.getAddItems());
		this.fileCollection.updateFileDescriptors(delta.getUpdateItems());
		this.fileCollection.removeFileDescriptors(delta.getRemoveItems());

		// update UI model
		LayoutDelta uiDeltas = new LayoutDelta();
		for (FileDescriptor file : delta.getAddItems()) {
			add(file, uiDeltas);
		}
		for (FileDescriptor file : delta.getUpdateItems()) {
			update(file, uiDeltas, delta.getActionType());
		}
		for (FileDescriptor file : delta.getRemoveItems()) {
			remove(file, uiDeltas);
		}

		Map<FileDescriptor, FileDescriptor> replaceSelection = delta.getReplaceItems();
		for (FileDescriptor old : replaceSelection.keySet()) {
			// just create dummy nodes (equals returns true)
			FileDescriptorTreeNode oldNode = new FileDescriptorTreeNode(old);
			FileDescriptorTreeNode newNode = new FileDescriptorTreeNode(replaceSelection.get(old));
			uiDeltas.replaceItem(oldNode, newNode, LayoutType.values());
		}
		return uiDeltas;
	}

	private void add(FileDescriptor fileDescriptor, LayoutDelta uiDeltas) {
		FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
		if (files.contains(fileDescriptorNode)) {
			// file already exists
			return;
		}
		files.add(fileDescriptorNode);
		uiDeltas.addItem(null, fileDescriptorNode, LayoutType.FLAT);
		// path
		String parentPath = getParentPath(fileDescriptor);
		RelativePathTreeNode pathNode = findParentNodeByContent(parentPath, this.paths);
		if (pathNode != null) {
			pathNode.getChildren().add(fileDescriptorNode);
			uiDeltas.addItem(pathNode, fileDescriptorNode, LayoutType.HIERARCHICAL);
		}
		else {
			// new path node
			pathNode = new RelativePathTreeNode(parentPath);
			pathNode.getChildren().add(fileDescriptorNode);
			paths.add(pathNode);
			uiDeltas.addItem(this, pathNode, LayoutType.HIERARCHICAL);
		}
		// album
		addAlbum(fileDescriptorNode, uiDeltas);
		// artist
		addArtist(fileDescriptorNode, uiDeltas);
	}

	private void remove(FileDescriptor fileDescriptor, LayoutDelta uiDeltas) {
		/*
		 * Note: removed fileDescriptor does not have audio data
		 */
		FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
		files.remove(fileDescriptorNode);
		uiDeltas.removeItem(fileDescriptorNode, LayoutType.FLAT);
		// path
		RelativePathTreeNode path = findParentNode(fileDescriptorNode, this.paths);
		if (path != null) {
			path.getChildren().remove(fileDescriptorNode);
			uiDeltas.removeItem(fileDescriptorNode, LayoutType.values());
			if (path.getChildren().isEmpty()) {
				paths.remove(path);
				uiDeltas.removeItem(path, LayoutType.HIERARCHICAL);
			}
		}
		// album
		AlbumTreeNode albumParentOld = findParentNode(fileDescriptorNode, this.albums);
		if (albumParentOld != null) {
			removeAlbum(albumParentOld, fileDescriptorNode, uiDeltas);
		}
		// artist
		ArtistTreeNode artistParentOld = findParentNode(fileDescriptorNode, this.artists);
		if (artistParentOld != null) {
			removeArtist(artistParentOld, fileDescriptorNode, uiDeltas);
		}
	}

	private void update(FileDescriptor fileDescriptor, LayoutDelta uiDeltas, ActionType actionType) {
		/*
		 * Update is quite complex because modified meta data means that ALBUM+ARTIST layouts must be reordered.
		 * Example: File's album attribute is changed from A to B
		 * - Old Structure: A -> File(album=A) 
		 * - New Structure: B -> File(album=B)
		 * -> remove File from A, remove A, add B, add File to B
		 */
		FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
		uiDeltas.updateItem(fileDescriptorNode, LayoutType.values());
		
		if (!ActionType.AUDIO_SAVED.equals(actionType)) {
			// only persistent audio data will affect UI
			return;
		}
		
		// album
		AlbumTreeNode albumParentOld = findParentNode(fileDescriptorNode, this.albums);
		AlbumTreeNode albumParentNew = addAlbum(fileDescriptorNode, uiDeltas);
		boolean albumChangeParent = !albumParentOld.equals(albumParentNew); 
		if (albumChangeParent) {
			removeAlbum(albumParentOld, fileDescriptorNode, uiDeltas);
		}
		// artist
		ArtistTreeNode artistParentOld = findParentNode(fileDescriptorNode, this.artists);
		ArtistTreeNode artistParentNew = addArtist(fileDescriptorNode, uiDeltas);
		boolean artistParentChanged = !artistParentOld.equals(artistParentNew); 
		if (artistParentChanged) {
			removeArtist(artistParentOld, fileDescriptorNode, uiDeltas);
		}
	}
	
	private AlbumTreeNode addAlbum(FileDescriptorTreeNode fileDescriptorNode, LayoutDelta uiDeltas) {
		String album = getAudioDataValue(fileDescriptorNode.getContent(), AudioAttribute.ALBUM);
		AlbumTreeNode albumParent = findParentNodeByContent(album, this.albums);
		if (albumParent != null) {
			// reuse parent node + add file child if necessary
			if (!albumParent.hasChild(fileDescriptorNode)) {
				albumParent.getChildren().add(fileDescriptorNode);
				uiDeltas.addItem(albumParent, fileDescriptorNode, LayoutType.ALBUM);
			}
		}
		else {
			// create new parent node + add file child 
			albumParent = new AlbumTreeNode(album);
			albumParent.getChildren().add(fileDescriptorNode);
			albums.add(albumParent);
			uiDeltas.addItem(this, albumParent, LayoutType.ALBUM);
		}
		return albumParent;
	}
	
	private ArtistTreeNode addArtist(FileDescriptorTreeNode fileDescriptorNode, LayoutDelta uiDeltas) {
		String artist = getAudioDataValue(fileDescriptorNode.getContent(), AudioAttribute.ARTIST);
		ArtistTreeNode artistParent = findParentNodeByContent(artist, this.artists);
		if (artistParent != null) {
			// reuse parent node + add file child if necessary
			if (!artistParent.hasChild(fileDescriptorNode)) {
				artistParent.getChildren().add(fileDescriptorNode);
				uiDeltas.addItem(artistParent, fileDescriptorNode, LayoutType.ARTIST);
			}
		}
		else {
			// create new parent node + add file child 
			artistParent = new ArtistTreeNode(artist);
			artistParent.getChildren().add(fileDescriptorNode);
			artists.add(artistParent);
			uiDeltas.addItem(this, artistParent, LayoutType.ARTIST);
		}
		return artistParent;
	}
	
	private void removeAlbum(AlbumTreeNode albumParent, FileDescriptorTreeNode fileDescriptorNode, LayoutDelta uiDeltas) {
		albumParent.getChildren().remove(fileDescriptorNode);
		uiDeltas.refreshItem(albumParent, LayoutType.ALBUM);
		if (albumParent.getChildren().isEmpty()) {
			albums.remove(albumParent);
			uiDeltas.removeItem(albumParent, LayoutType.ALBUM);
		}
	}
	
	private void removeArtist(ArtistTreeNode artistParent, FileDescriptorTreeNode fileDescriptorNode, LayoutDelta uiDeltas) {
		artistParent.getChildren().remove(fileDescriptorNode);
		uiDeltas.refreshItem(artistParent, LayoutType.ARTIST);
//		uiDeltas.removeItem(fileDescriptorNode, LayoutType.ARTIST);
		if (artistParent.getChildren().isEmpty()) {
			artists.remove(artistParent);
			uiDeltas.removeItem(artistParent, LayoutType.ARTIST);
		}
	}

	/**
	 * Returns the new parent node if available, else return null
	 */
	private <T extends AbstractTableTreeNode<?, ?>> T findParentNodeByContent(String content, Set<T> nodes) {
		for (T node : nodes) {
			if (node.getContent().equals(content)) {
				return node;
			}
		}
		return null;
	}
	
	/**
	 * Returns the current parent node
	 */
	private <T extends AbstractTableTreeNode<?, ?>> T findParentNode(FileDescriptorTreeNode node, Set<T> nodes) {
		for (T parent : nodes) {
			if (parent.getChildren().contains(node)) {
				return parent;
			}
		}
		return null;
	}

	private String getParentPath(FileDescriptor fileDescriptor) {
		String relativePath = fileDescriptor.getRelativePath();
		String parentPath = RelativePathUtils.getParent(relativePath);
		parentPath = RelativePathUtils.ensureStartingSlash(parentPath);
		if (parentPath == null) {
			parentPath = File.pathSeparator;
		}
		return parentPath;
	}

	private String getAudioDataValue(FileDescriptor fileDescriptor, AudioAttribute attribute) {
		String value = AudioData.DEFAULT_VALUE;
		if (fileDescriptor.hasMetaData(AudioData.class)) {
			AudioData audioData = (AudioData) fileDescriptor.getMetaData();
			if (audioData.hasAttribute(attribute)) {
				value = audioData.getAttribute(attribute);
			}
		}
		return value;
	}

	public FileCollection getFileCollection() {
		return fileCollection;
	}

	public void setFileCollection(FileCollection fileCollection) {
		this.fileCollection = fileCollection;
	}

	public Set<RelativePathTreeNode> getPaths() {
		return paths;
	}

	public Set<AlbumTreeNode> getAlbums() {
		return albums;
	}

	public Set<ArtistTreeNode> getArtists() {
		return artists;
	}

	public Set<FileDescriptorTreeNode> getFiles() {
		return files;
	}

}
