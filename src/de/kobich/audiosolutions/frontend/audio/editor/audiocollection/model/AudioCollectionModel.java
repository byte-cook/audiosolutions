package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AlbumIdentity;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.EditorLayoutManager;
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
import lombok.Getter;

public class AudioCollectionModel implements ICollectionEditorModel {
	private final AudioSearchService searchService;
	private final FileCollection fileCollection;
	private final EditorLayoutManager layoutManager;
	@Getter
	private final Set<FileDescriptorTreeNode> files;
	@Getter
	private final Set<RelativePathTreeNode> paths;
	@Getter
	private final Set<AlbumTreeNode> albums;

	public AudioCollectionModel(FileCollection fileCollection, EditorLayoutManager layoutManager) {
		this.searchService = AudioSolutions.getService(AudioSearchService.class);
		this.fileCollection = fileCollection;
		this.layoutManager = layoutManager;
		this.files = new HashSet<FileDescriptorTreeNode>();
		this.paths = new HashSet<RelativePathTreeNode>();
		this.albums = new HashSet<AlbumTreeNode>();
	}
	
	public void initLayout(LayoutType layout) {
		switch (layout) {
		case FLAT:
			this.files.clear();;
			for (FileDescriptor fileDescriptor : fileCollection.getFileDescriptors()) {
				FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
				files.add(fileDescriptorNode);
			}
			break;
		case HIERARCHICAL:
			DimensionMap2D<String, FileDescriptorTreeNode> path2Files = new DimensionMap2D<>(DimensionType.LIST);
			for (FileDescriptor fileDescriptor : fileCollection.getFileDescriptors()) {
				FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
				String parentPath = getParentPath(fileDescriptor);
				path2Files.addElement(parentPath, fileDescriptorNode);
			}
			// path
			this.paths.clear();
			for (String path : path2Files.keySet()) {
				RelativePathTreeNode pathNode = new RelativePathTreeNode(path);
				List<FileDescriptorTreeNode> children = path2Files.get(path).asList();
				pathNode.getChildren().addAll(children);
				paths.add(pathNode);
			}
			break;
		case ALBUM:
			DimensionMap2D<Long, FileDescriptorTreeNode> album2Files = new DimensionMap2D<>(DimensionType.LIST);
			for (FileDescriptor fileDescriptor : fileCollection.getFileDescriptors()) {
				FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
				Long albumId = getAlbumId(fileDescriptor).orElse(null);
				album2Files.addElement(albumId, fileDescriptorNode);
			}
			// album
			albums.clear();
			for (Long id : album2Files.keySet()) {
				AlbumTreeNode albumNode = createAlbumTreeNode(id);
				List<FileDescriptorTreeNode> children = album2Files.get(id).asList();
				albumNode.getChildren().addAll(children);
				albums.add(albumNode);
			}
			break;
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
		
		// update all available albums (album's artist could have been changed) 
		for (AlbumTreeNode albumNode : this.albums) {
			if (albumNode.getAlbumId().isPresent()) {
				Album album = this.searchService.searchAlbum(albumNode.getAlbumId().get()).orElse(null);
				albumNode.setAlbum(album);
				uiDeltas.updateItem(albumNode, LayoutType.ALBUM);
			}
		}

		return uiDeltas;
	}

	private void add(FileDescriptor fileDescriptor, LayoutDelta uiDeltas) {
		FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
		if (files.contains(fileDescriptorNode)) {
			// file already exists
			return;
		}
		
		switch (layoutManager.getActiveLayout()) {
		case FLAT:
			files.add(fileDescriptorNode);
			uiDeltas.addItem(this, fileDescriptorNode, LayoutType.FLAT);
			break;
		case HIERARCHICAL:
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
			break;
		case ALBUM:
			addAlbum(fileDescriptorNode, uiDeltas);
			break;
		}
	}

	private void remove(FileDescriptor fileDescriptor, LayoutDelta uiDeltas) {
		/*
		 * Note: removed fileDescriptor does not have audio data
		 */
		FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
		switch (layoutManager.getActiveLayout()) {
		case FLAT:
			files.remove(fileDescriptorNode);
			uiDeltas.removeItem(fileDescriptorNode, LayoutType.FLAT);
			break;
		case HIERARCHICAL:
			RelativePathTreeNode path = findParentNode(fileDescriptorNode, this.paths);
			if (path != null) {
				path.getChildren().remove(fileDescriptorNode);
				uiDeltas.removeItem(fileDescriptorNode, LayoutType.values());
				if (path.getChildren().isEmpty()) {
					paths.remove(path);
					uiDeltas.removeItem(path, LayoutType.HIERARCHICAL);
				}
			}
			break;
		case ALBUM:
			AlbumTreeNode albumParentOld = findParentNode(fileDescriptorNode, this.albums);
			if (albumParentOld != null) {
				removeAlbum(albumParentOld, fileDescriptorNode, uiDeltas);
			}
			break;
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

		switch (layoutManager.getActiveLayout()) {
		case FLAT:
		case HIERARCHICAL:
			// FileDescriptorTreeNode already updated before
			break;
		case ALBUM:
			AlbumTreeNode albumParentOld = findParentNode(fileDescriptorNode, this.albums);
			AlbumTreeNode albumParentNew = addAlbum(fileDescriptorNode, uiDeltas);
			boolean albumChangeParent = !albumParentOld.equals(albumParentNew); 
			if (albumChangeParent) {
				removeAlbum(albumParentOld, fileDescriptorNode, uiDeltas);
			}
			break;
		}
	}
	
	private AlbumTreeNode addAlbum(FileDescriptorTreeNode fileDescriptorNode, LayoutDelta uiDeltas) {
		final Long albumId = getAlbumId(fileDescriptorNode.getContent()).orElse(null);
		AlbumTreeNode albumParent = findParentAlbumNode(fileDescriptorNode, albumId).orElse(null);
		if (albumParent != null) {
			// reuse parent node + add file child if necessary
			if (!albumParent.hasChild(fileDescriptorNode)) {
				albumParent.getChildren().add(fileDescriptorNode);
				uiDeltas.addItem(albumParent, fileDescriptorNode, LayoutType.ALBUM);
			}
		}
		else {
			// create new parent node + add file child
			albumParent = createAlbumTreeNode(albumId);
			albumParent.getChildren().add(fileDescriptorNode);
			albums.add(albumParent);
			uiDeltas.addItem(this, albumParent, LayoutType.ALBUM);
		}
		return albumParent;
	}
	
	private void removeAlbum(AlbumTreeNode albumParent, FileDescriptorTreeNode fileDescriptorNode, LayoutDelta uiDeltas) {
		albumParent.getChildren().remove(fileDescriptorNode);
		uiDeltas.refreshItem(albumParent, LayoutType.ALBUM);
		if (albumParent.getChildren().isEmpty()) {
			albums.remove(albumParent);
			uiDeltas.removeItem(albumParent, LayoutType.ALBUM);
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
	
	private Optional<AlbumTreeNode> findParentAlbumNode(FileDescriptorTreeNode node, @Nullable Long albumId) {
		if (albumId == null) {
			return this.albums.stream().filter(a -> a.getAlbumId().isEmpty()).findAny();
		}
		return this.albums.stream().filter(a -> albumId.equals(a.getAlbumId().orElse(null))).findAny();
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
	
	private Optional<Long> getAlbumId(FileDescriptor fileDescriptor) {
		AudioData audioData = fileDescriptor.getMetaDataOptional(AudioData.class).orElse(null);
		if (audioData != null) {
			return audioData.getAlbumIdentifier().flatMap(AlbumIdentity::getPersistentId);
		}
		return Optional.empty();
	}
	
	private AlbumTreeNode createAlbumTreeNode(@Nullable Long albumId) {
		if (albumId != null) {
			Album album = this.searchService.searchAlbum(albumId).orElse(null);
			return new AlbumTreeNode(album);
		}
		return new AlbumTreeNode(null);
	}

}
