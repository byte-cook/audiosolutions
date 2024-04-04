package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.commons.ui.jface.tree.TreeColumnLayoutManager;

public class PlaylistEditorLabelProvider implements ITableLabelProvider {
	private final TreeColumnLayoutManager layoutManager;
	private Image folderImg;
	private Image fileImg;
	
	public PlaylistEditorLabelProvider(TreeColumnLayoutManager layoutManager) {
		this.layoutManager = layoutManager;
		this.folderImg = Activator.getDefault().getImage(ImageKey.FOLDER);
		this.fileImg = Activator.getDefault().getImage(ImageKey.AUDIO_FILE);
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		PlaylistEditorColumn column = (PlaylistEditorColumn) layoutManager.getElementByIndex(columnIndex).orElseThrow();
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
	public String getColumnText(Object element, int columnIndex) {
		PlaylistEditorColumn column = (PlaylistEditorColumn) layoutManager.getElementByIndex(columnIndex).orElseThrow();
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
					return file.getFileName();
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
		throw new IllegalStateException("Illegal column index <" + columnIndex + ">, expected<0 - 3>");
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {}

}
