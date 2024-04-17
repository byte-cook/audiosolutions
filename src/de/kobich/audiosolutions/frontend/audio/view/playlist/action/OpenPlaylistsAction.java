package de.kobich.audiosolutions.frontend.audio.view.playlist.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditorInput;
import de.kobich.audiosolutions.frontend.audio.view.playlist.PlaylistView;
import de.kobich.commons.ui.jface.JFaceExec;

public class OpenPlaylistsAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.view.playlists.openPlaylists";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		PlaylistView view = (PlaylistView) window.getActivePage().findView(PlaylistView.ID);
		if (view == null) {
			return null;
		}

		Set<Playlist> playlists = view.getSelectedPlaylists();
		if (playlists.isEmpty()) {
			MessageDialog.openError(window.getShell(), "Open Playlists", "No playlist selected.");
		}
		
		final List<EditablePlaylist> editablePlaylists = new ArrayList<>();
		JFaceExec.builder(window.getShell(), "Open Playlists")
			.worker(ctx -> {
				PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
				for (Playlist playlist : playlists) {
					EditablePlaylist editablePlaylist = playlistService.openPlaylist(playlist, ctx.getProgressMonitor());
					editablePlaylists.add(editablePlaylist);
				}
			})
			.ui(ctx -> {
				// Open editors
				for (EditablePlaylist editablePlaylist : editablePlaylists) {
					PlaylistEditorInput input = new PlaylistEditorInput(editablePlaylist);
					IWorkbenchPage page = window.getActivePage();
					page.openEditor(input, PlaylistEditor.ID);
				}
			})
			.exceptionalDialog("Error while opening playlist")
			.runProgressMonitorDialog(true, true);

		return null;
	}

}
