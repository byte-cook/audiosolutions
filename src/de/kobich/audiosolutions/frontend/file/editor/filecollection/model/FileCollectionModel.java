package de.kobich.audiosolutions.frontend.file.editor.filecollection.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditorModel;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.commons.collections.Dimension.DimensionType;
import de.kobich.commons.collections.DimensionMap2D;
import de.kobich.commons.utils.RelativePathUtils;
import de.kobich.component.file.FileDescriptor;

public class FileCollectionModel implements ICollectionEditorModel {
	private final FileCollection fileCollection;
	private final List<RelativePathTreeNode> paths;
	private final List<FileDescriptorTreeNode> files;

	/**
	 * Updates this content by the given input
	 * 
	 * @param input
	 */
	public FileCollectionModel(FileCollection fileCollection) {
		this.fileCollection = fileCollection;
		this.files = new ArrayList<FileDescriptorTreeNode>();
		DimensionMap2D<String, FileDescriptorTreeNode> path2Files = new DimensionMap2D<String, FileDescriptorTreeNode>(DimensionType.LIST);
		for (FileDescriptor fileDescriptor : fileCollection.getFileDescriptors()) {
			FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);

			String parentPath = getParentPath(fileDescriptor);
			path2Files.addElement(parentPath, fileDescriptorNode);
			files.add(fileDescriptorNode);
		}

		this.paths = new ArrayList<RelativePathTreeNode>();
		for (String path : path2Files.keySet()) {
			RelativePathTreeNode pathNode = new RelativePathTreeNode(path);
			List<FileDescriptorTreeNode> children = path2Files.get(path).asList();
			pathNode.getChildren().addAll(children);
			paths.add(pathNode);
		}
	}

	@Override
	public LayoutDelta updateModel(CollectionEditorDelta delta) {
		// update file collection
		this.fileCollection.addFileDescriptors(delta.getAddItems());
		this.fileCollection.removeFileDescriptors(delta.getRemoveItems());
		this.fileCollection.updateFileDescriptors(delta.getUpdateItems());
		// update UI objects
		LayoutDelta uiDeltas = new LayoutDelta();
		for (FileDescriptor file : delta.getAddItems()) {
			add(file, uiDeltas);
		}
		for (FileDescriptor file : delta.getRemoveItems()) {
			remove(file, uiDeltas);
		}
		for (FileDescriptor file : delta.getUpdateItems()) {
			update(file, uiDeltas);
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
		RelativePathTreeNode pathNode = findPathNode(parentPath);
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
			uiDeltas.addItem(pathNode, fileDescriptorNode, LayoutType.HIERARCHICAL);
		}
	}

	private void remove(FileDescriptor fileDescriptor, LayoutDelta uiDeltas) {
		FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
		files.remove(fileDescriptorNode);
		uiDeltas.removeItem(fileDescriptorNode, LayoutType.FLAT);
		// path
		RelativePathTreeNode path = findParentNode(fileDescriptorNode, this.paths);
		if (path != null) {
			path.getChildren().remove(fileDescriptorNode);
			uiDeltas.removeItem(fileDescriptorNode, LayoutType.HIERARCHICAL);
			if (path.getChildren().isEmpty()) {
				paths.remove(path);
				uiDeltas.removeItem(path, LayoutType.HIERARCHICAL);
			}
		}
	}

	private void update(FileDescriptor fileDescriptor, LayoutDelta uiDeltas) {
		FileDescriptorTreeNode fileDescriptorNode = new FileDescriptorTreeNode(fileDescriptor);
		uiDeltas.updateItem(fileDescriptorNode, LayoutType.values());
	}

	private RelativePathTreeNode findPathNode(String parentPath) {
		for (RelativePathTreeNode path : this.paths) {
			if (path.getContent().equals(parentPath)) {
				return path;
			}
		}
		return null;
	}
	
	private <T extends AbstractTableTreeNode<?, ?>> T findParentNode(FileDescriptorTreeNode node, List<T> nodes) {
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

	public FileCollection getFileCollection() {
		return fileCollection;
	}

	public List<RelativePathTreeNode> getPaths() {
		return paths;
	}

	public List<FileDescriptorTreeNode> getFiles() {
		return files;
	}
}
