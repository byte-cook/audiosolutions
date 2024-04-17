package de.kobich.audiosolutions.frontend.audio.view.playlist.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditorInput;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;

public class CreateNewPlaylistAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.view.playlists.createNewPlaylist";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		Wrapper<EditablePlaylist> editablePlaylist = Wrapper.empty();
		JFaceExec.builder(window.getShell(), "Create New Playlist")
			.worker(ctx -> {
				PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
				editablePlaylist.set(playlistService.createNewPlaylist("", false));
			})
			.ui(ctx -> {
				// Open editor
				PlaylistEditorInput input = new PlaylistEditorInput(editablePlaylist.get());
				IWorkbenchPage page = window.getActivePage();
				page.openEditor(input, PlaylistEditor.ID);
			})
			.exceptionalDialog("Error while creating playlist")
			.runProgressMonitorDialog(true, true);

		return null;
	}

}
