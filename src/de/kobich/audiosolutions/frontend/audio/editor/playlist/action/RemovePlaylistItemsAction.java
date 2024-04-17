package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistSelection;
import de.kobich.commons.ui.jface.JFaceExec;

public class RemovePlaylistItemsAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.editor.removePlaylistItems";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor(); //SelectionSupport.INSTANCE.getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			PlaylistSelection selection = playlistEditor.getSelection();
			if (selection.isEmpty()) {
				boolean confirmed = MessageDialog.openQuestion(window.getShell(), "Remove Files/Folders", "Do you want to delete the complete playlist?");
				if (confirmed) {
					JFaceExec.builder(window.getShell(), "Remove Files/Folders")
						.worker(ctx -> playlistEditor.getPlaylist().getFolders().clear())
						.ui(ctx -> playlistEditor.refresh())
						.exceptionalDialog("Removing items failed")
						.runProgressMonitorDialog(true, false);
				}
			}
			else {
				JFaceExec.builder(window.getShell(), "Remove Files/Folders")
					.worker(ctx -> {
						EditablePlaylist playlist = playlistEditor.getPlaylist();
						for (EditablePlaylistFolder folder : selection.getFolders()) {
							playlist.remove(folder);
						}
						for (EditablePlaylistFile file : selection.getFiles()) {
							playlist.remove(file);
						}
					})
					.ui(ctx -> playlistEditor.refresh())
					.exceptionalDialog("Removing items failed")
					.runProgressMonitorDialog(true, false);
				
			}
		}
		return null;
	}

}
