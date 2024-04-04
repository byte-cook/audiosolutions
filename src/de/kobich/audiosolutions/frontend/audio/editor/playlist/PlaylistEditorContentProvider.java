package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.common.ui.editor.EditorLayoutManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlaylistEditorContentProvider implements ITreeContentProvider {
	private final EditorLayoutManager layoutManager;
	private EditablePlaylist model;
	
	@Override
	public Object[] getChildren(Object input) {
		if (input instanceof EditablePlaylistFolder folder) {
			return folder.getFiles().toArray();
		}
		else if (input instanceof EditablePlaylistFile file) {
			return List.of().toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">");
	}

	@Override
	public Object getParent(Object input) {
		if (input instanceof EditablePlaylistFolder folder) {
			return model;
		}
		else if (input instanceof EditablePlaylistFile file) {
			for (EditablePlaylistFolder folder : model.getFolders()) {
				if (folder.getFiles().contains(file)) {
					return folder;
				}
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
		if (input instanceof EditablePlaylist playlist) {
			switch (layoutManager.getActiveLayout()) {
				case FLAT:
					List<Object> files = new ArrayList<>();
					playlist.getFolders().forEach(f -> files.addAll(f.getFiles()));
					return files.toArray();
				case HIERARCHICAL:
					List<Object> folders = new ArrayList<>();
					folders.addAll(playlist.getFolders());
					return folders.toArray();
				default:
					break;
			}
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + EditablePlaylist.class.getName() + ">");
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof EditablePlaylist playlist) {
			this.model = playlist;
		}
	}
}
