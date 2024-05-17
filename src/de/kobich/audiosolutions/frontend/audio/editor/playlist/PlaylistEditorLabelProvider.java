package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;

public class PlaylistEditorLabelProvider extends ColumnLabelProvider {
	private final PlaylistEditorColumn column;
	private Image folderImg;
	private Image fileImg;
	
	public PlaylistEditorLabelProvider(PlaylistEditorColumn column) {
		this.column = column;
		this.folderImg = Activator.getDefault().getImage(ImageKey.FOLDER);
		this.fileImg = Activator.getDefault().getImage(ImageKey.AUDIO_FILE);
	}

	@Override
	public Image getImage(Object element) {
		if (PlaylistEditorColumn.NAME.equals(column)) {
			if (element instanceof EditablePlaylistFolder folder) {
				return folderImg;
			}
			else if (element instanceof EditablePlaylistFile file) {
				return fileImg;
			}
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof EditablePlaylistFolder folder) {
			switch (column) {
				case NAME:
					return folder.getPath();
				case FILE_COUNT:
					return String.valueOf(folder.getFiles().size());
				default:
					return "";
			}
		}
		else if (element instanceof EditablePlaylistFile file) {
			switch (column) {
				case NAME:
					return file.getName();
				case FILE_EXISTS: 
					return String.valueOf(file.getFile().exists());
				case FOLDER:
					return file.getFolder().getPath();
				case FILE_PATH:
					return file.getFile().getAbsolutePath();
				default:
					return "";
			}
		}
		throw new IllegalStateException("Illegal element <" + element + ">");
	}

}
