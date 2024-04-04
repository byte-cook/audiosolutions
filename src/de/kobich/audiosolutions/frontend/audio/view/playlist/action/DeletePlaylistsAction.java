package de.kobich.audiosolutions.frontend.audio.view.playlist.action;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
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
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

public class DeletePlaylistsAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(DeletePlaylistsAction.class);
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

		List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("Delete Playlist", window.getShell(), states) {
			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case WORKER_1:
					PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
					playlistService.deletePlaylists(playlists, super.getProgressMonitor());
					break;
				case UI_2:
					UIEvent event = new UIEvent(ActionType.PLAYLIST_DELETED);
					playlists.forEach(p -> event.getPlaylistDelta().getPlaylistIds().add(p.getId()));
					EventSupport.INSTANCE.fireEvent(event);
					break;
				case UI_ERROR:
					if (super.getProgressMonitor().isCanceled()) {
						return;
					}
					Exception e = super.getException();
					logger.error(e.getMessage(), e);
					MessageDialog.openError(super.getParent(), super.getName(), "Error while openinig playlist: \n" + e.getMessage());
					break;
				default: 
					break;
				}
			}
		};
		runner.runProgressMonitorDialog(true, true);
		return null;
	}

}
