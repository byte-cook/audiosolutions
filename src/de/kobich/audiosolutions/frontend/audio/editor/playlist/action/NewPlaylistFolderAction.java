package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistSelection;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;

public class NewPlaylistFolderAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.editor.newPlaylistFolder";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			Set<String> proposals = new HashSet<>();
			final EditablePlaylistFile file = playlistEditor.getFirstSelectedFile().orElse(null);
			if (file != null) {
				proposals.addAll(playlistEditor.getPlaylist().getFolderNameProposals(file.getFile()));
			}
			
			final NewPlaylistFolderDialog dialog = new NewPlaylistFolderDialog(window.getShell(), proposals);
			int status = dialog.open();
			if (status == IDialogConstants.OK_ID) {
				Wrapper<PlaylistSelection> selection = Wrapper.empty();
				Wrapper<EditablePlaylistFolder> folder = Wrapper.empty();
				JFaceExec.builder(window.getShell(), "New Folder")
					// get current selection
					.ui(ctx -> selection.set(playlistEditor.getSelection()))
					// create folder
					.worker(ctx -> {
						folder.set(playlistEditor.getPlaylist().createOrGetFolder(dialog.getFolderName()));
						
						selection.ifPresent(sel -> {
							if (!sel.isEmpty() && dialog.isMoveToEnabled()) {
								playlistEditor.getPlaylist().moveToFolder(sel.getFolders(), sel.getFiles(), folder.get());
							}
						});
					})
					// refresh and select new folder
					.ui(ctx -> {
						playlistEditor.refresh();
						playlistEditor.setSelection(new Object[] {folder.get()});
					})
					.exceptionalDialog("Create new folder failed")
					.runProgressMonitorDialog(true, false);
			}
		}
		return null;
	}

}
