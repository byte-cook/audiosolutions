package de.kobich.audiosolutions.frontend.audio.view.playlist.action;

import java.util.Arrays;
import java.util.List;

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
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditorInput;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

public class CreateNewPlaylistAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(CreateNewPlaylistAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.view.playlists.createNewPlaylist";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

		List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("Create New Playlist", window.getShell(), states) {
			private EditablePlaylist editablePlaylist;
			
			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case WORKER_1:
					PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
					editablePlaylist = playlistService.createNewPlaylist("", false);
					break;
				case UI_2:
					try {
						// Open editor
						PlaylistEditorInput input = new PlaylistEditorInput(editablePlaylist);
						IWorkbenchPage page = window.getActivePage();
						page.openEditor(input, PlaylistEditor.ID);
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
