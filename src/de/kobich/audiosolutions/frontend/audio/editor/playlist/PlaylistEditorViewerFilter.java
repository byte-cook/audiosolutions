package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;

public class PlaylistEditorViewerFilter extends ViewerFilter {
	public static final String FILTER_PROP = "collection.filter";
	private String search;

	public void setSearchText(String s) {
		this.search = s;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (StringUtils.isEmpty(this.search)) {
			return true;
		}
		
		if (element instanceof EditablePlaylistFile file) {
			if (select(file)) {
				return true;
			}
		}
		else if (element instanceof EditablePlaylistFolder folder) {
			if (select(folder)) {
				return true;
			}
			
			for (EditablePlaylistFile file : folder.getFiles()) {
				if (select(file)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean select(EditablePlaylistFolder folder) {
		if (StringUtils.isEmpty(search)) {
			return true;
		}
		return FilenameUtils.wildcardMatch(folder.getPath(), search, IOCase.INSENSITIVE);
	}
	
	public boolean select(EditablePlaylistFile file) {
		if (StringUtils.isEmpty(search)) {
			return true;
		}
		return FilenameUtils.wildcardMatch(file.getName(), search, IOCase.INSENSITIVE) || FilenameUtils.wildcardMatch(file.getFile().getAbsolutePath(), search, IOCase.INSENSITIVE);
	}
	
	public boolean isFilterProperty(Object element, String property) {
		return FILTER_PROP.equals(property);
	}

}
