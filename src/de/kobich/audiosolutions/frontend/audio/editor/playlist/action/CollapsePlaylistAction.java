package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;

public class CollapsePlaylistAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.editor.collapsePlaylist";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			playlistEditor.collapseAll();
		}
		return null;
	}

}
