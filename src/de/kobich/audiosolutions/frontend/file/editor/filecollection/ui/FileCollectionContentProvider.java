package de.kobich.audiosolutions.frontend.file.editor.filecollection.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileCollectionModel;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;


public class FileCollectionContentProvider implements ITreeContentProvider {
	private final LayoutType layoutType;
	private FileCollectionModel model;
	
	public FileCollectionContentProvider(LayoutType layoutType) {
		this.layoutType = layoutType;
	}
	
	@Override
	public Object[] getChildren(Object input) {
		if (input instanceof RelativePathTreeNode) {
			RelativePathTreeNode pathNode = (RelativePathTreeNode) input;
			return pathNode.getChildren().toArray();
		}
		else if (input instanceof FileDescriptorTreeNode) {
			FileDescriptorTreeNode pathNode = (FileDescriptorTreeNode) input;
			return pathNode.getChildren().toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + FileCollection.class.getName() + ">");
	}

	@Override
	public Object getParent(Object input) {
		if (input instanceof RelativePathTreeNode) {
			return model;
		}
		else if (input instanceof FileDescriptorTreeNode) {
			FileDescriptorTreeNode node = (FileDescriptorTreeNode) input;
			switch (layoutType) {
			case FLAT:
				return model;
			default:
				for (RelativePathTreeNode path : model.getPaths()) {
					if (path.getChildren().contains(node)) {
						return path;
					}
				}
				break;
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object input) {
		return getChildren(input).length > 0;
	}

	@Override
	public Object[] getElements(Object input) {
		if (input instanceof FileCollectionModel) {
			switch (layoutType) {
			case FLAT:
				return model.getFiles().toArray();
			default:
				return model.getPaths().toArray();
			}
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + FileCollectionModel.class.getName() + ">");
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof FileCollectionModel) {
			this.model = (FileCollectionModel) newInput;
		}
	}
}
