package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlaylistEditorCellModifier implements ICellModifier {
	private static final Logger logger = Logger.getLogger(PlaylistEditorCellModifier.class);
	private final PlaylistEditor editor;

	@Override
	public boolean canModify(Object element, String property) {
		PlaylistEditorColumn column = PlaylistEditorColumn.getByName(property);
		switch (column) {
			case NAME:
				return true;
			default: 
				return false;
		}
	}

	@Override
	public Object getValue(Object element, String property) {
		PlaylistEditorColumn column = PlaylistEditorColumn.getByName(property);
		switch (column) {
			case NAME:
				if (element instanceof EditablePlaylistFile file) {
					return file.getName();
				}
				if (element instanceof EditablePlaylistFolder folder) {
					return folder.getPath();
				}
			default: 
				return null;
		}
	}

	@Override
	public void modify(Object element, String property, Object value) {
		try {
			PlaylistEditorColumn column = PlaylistEditorColumn.getByName(property);
			switch (column) {
				case NAME:
					if (element instanceof Item) {
						Item tableItem = (Item) element;
						if (tableItem.getData() instanceof EditablePlaylistFile file) {
							Optional<EditablePlaylistFile> newFile = this.editor.getPlaylist().renameFile(file, String.valueOf(value));
							newFile.ifPresent(f -> this.editor.refresh());
						}
						else if (tableItem.getData() instanceof EditablePlaylistFolder folder) {
							Optional<EditablePlaylistFolder> newFolder = this.editor.getPlaylist().renameFolder(folder, String.valueOf(value));
							newFolder.ifPresent(f -> this.editor.refresh());
						}
					}
					break;
				default: 
					break;
			}
		}
		catch (Exception exc) {
			logger.error(exc, exc);
			editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(editor.getSite().getShell(), "Edit Playlist", exc.getMessage());
				}
			});
		}
	}

}
