package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;

public class PlaylistEditingSupport extends EditingSupport {
	private static final Logger logger = Logger.getLogger(PlaylistEditingSupport.class);
	private final PlaylistEditor editor;
	private final PlaylistEditorColumn column;
	private final CellEditor cellEditor;
	
	public PlaylistEditingSupport(PlaylistEditor editor, TreeViewer viewer, PlaylistEditorColumn column) {
		super(viewer);
		this.editor = editor;
		this.column = column;
		this.cellEditor = new TextCellEditor(viewer.getTree());
	}
	
	@Override
    protected CellEditor getCellEditor(Object element) {
        return cellEditor;
    }
	
	@Override
	public boolean canEdit(Object element) {
		switch (column) {
			case NAME:
				return true;
			default: 
				return false;
		}
	}

	@Override
	public Object getValue(Object element) {
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
	public void setValue(Object element, Object value) {
		try {
			String valueStr = String.valueOf(value);
			if (StringUtils.isBlank(valueStr)) {
				return;
			}
			
			switch (column) {
				case NAME:
					if (element instanceof EditablePlaylistFile file) {
						Optional<EditablePlaylistFile> newFile = this.editor.getPlaylist().renameFile(file, valueStr);
						newFile.ifPresent(f -> this.editor.refresh());
					}
					else if (element instanceof EditablePlaylistFolder folder) {
						Optional<EditablePlaylistFolder> newFolder = this.editor.getPlaylist().renameFolder(folder, valueStr);
						newFolder.ifPresent(f -> this.editor.refresh());
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
