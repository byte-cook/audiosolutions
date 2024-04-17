package de.kobich.audiosolutions.frontend.audio.view.playlist.action;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import de.kobich.audiosolutions.frontend.audio.view.playlist.PlaylistView;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.commons.ui.jface.JFaceExec;

public class DeletePlaylistsAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.view.playlists.deletePlaylists";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		PlaylistView view = (PlaylistView) window.getActivePage().findView(PlaylistView.ID);
		if (view == null) {
			return null;
		}

		Set<Playlist> playlists = view.getSelectedPlaylists();
		if (playlists.isEmpty()) {
			MessageDialog.openError(window.getShell(), "Delete Playlist", "No playlist selected.");
		}
		
		String msg = "Do you really want to delele the selected playlists?";
		if (playlists.size() == 1) {
			msg = "Do you really want to delele the playlist \"%s\"?".formatted(playlists.iterator().next().getName());
		}
		boolean confirmDelete = MessageDialog.openQuestion(null, "Delete Playlist", msg);
		if (!confirmDelete) {
			return null;
		}
		
		JFaceExec.builder(window.getShell(), "Delete Playlist")
			.worker(ctx -> {
				PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
				playlistService.deletePlaylists(playlists, ctx.getProgressMonitor());
			})
			.ui(ctx -> {
				UIEvent uiEvent = new UIEvent(ActionType.PLAYLIST_DELETED);
				playlists.forEach(p -> uiEvent.getPlaylistDelta().getPlaylistIds().add(p.getId()));
				EventSupport.INSTANCE.fireEvent(uiEvent);
			})
			.exceptionalDialog("Error while deleting playlist")
			.runProgressMonitorDialog(true, true);

		return null;
	}

}
