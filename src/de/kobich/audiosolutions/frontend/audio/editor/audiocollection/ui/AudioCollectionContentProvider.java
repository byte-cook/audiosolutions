package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AlbumTreeNode;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AudioCollectionModel;
import de.kobich.audiosolutions.frontend.common.ui.editor.EditorLayoutManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AudioCollectionContentProvider implements ITreeContentProvider {
	private final EditorLayoutManager layoutManager;
	private AudioCollectionModel model;
	
	@Deprecated
	public AudioCollectionContentProvider(LayoutType layoutType) {
		this((EditorLayoutManager) null);
	}
	
	@Override
	public Object[] getChildren(Object input) {
		if (input instanceof RelativePathTreeNode) {
			RelativePathTreeNode pathNode = (RelativePathTreeNode) input;
			return pathNode.getChildren().toArray();
		}
		else if (input instanceof AlbumTreeNode) {
			AlbumTreeNode albumNode = (AlbumTreeNode) input;
			return albumNode.getChildren().toArray();
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
		else if (input instanceof AlbumTreeNode) {
			return model;
		}
		else if (input instanceof FileDescriptorTreeNode) {
			FileDescriptorTreeNode node = (FileDescriptorTreeNode) input;
			switch (layoutManager.getActiveLayout()) {
			case FLAT:
				return model;
			case HIERARCHICAL:
				for (RelativePathTreeNode path : model.getPaths()) {
					if (path.getChildren().contains(node)) {
						return path;
					}
				}
				break;
			case ALBUM:
				for (AlbumTreeNode albumNode : model.getAlbums()) {
					if (albumNode.getChildren().contains(node)) {
						return albumNode;
					}
				}
				break;
			default:
				throw new IllegalStateException("Layout unknown");
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
		if (input instanceof AudioCollectionModel) {
			switch (layoutManager.getActiveLayout()) {
			case FLAT:
				return model.getFiles().toArray();
			case HIERARCHICAL:
				return model.getPaths().toArray();
			case ALBUM:
				return model.getAlbums().toArray();
			default:
				throw new IllegalStateException("Layout unknown");
			}
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + AudioCollectionModel.class.getName() + ">");
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof AudioCollectionModel) {
			this.model = (AudioCollectionModel) newInput;
		}
	}
}
