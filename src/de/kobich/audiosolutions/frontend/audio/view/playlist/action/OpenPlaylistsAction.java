package de.kobich.audiosolutions.frontend.audio.view.playlist.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
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
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

public class OpenPlaylistsAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(OpenPlaylistsAction.class);
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

		List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("Open Playlists", window.getShell(), states) {
			private List<EditablePlaylist> editablePlaylists = new ArrayList<>();
			
			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case WORKER_1:
					PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
					for (Playlist playlist : playlists) {
						EditablePlaylist editablePlaylist = playlistService.openPlaylist(playlist, super.getProgressMonitor());
						editablePlaylists.add(editablePlaylist);
					}
					break;
				case UI_2:
					try {
						// Open editors
						for (EditablePlaylist editablePlaylist : editablePlaylists) {
							PlaylistEditorInput input = new PlaylistEditorInput(editablePlaylist);
							IWorkbenchPage page = window.getActivePage();
							page.openEditor(input, PlaylistEditor.ID);
						}
					}
					catch (final Exception exc) {
						MessageDialog.openError(window.getShell(), super.getName(), "Error while opening playlist: \n" + exc.getMessage());
					}
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
