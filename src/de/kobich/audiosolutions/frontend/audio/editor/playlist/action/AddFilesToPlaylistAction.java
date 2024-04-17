package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;

public class AddFilesToPlaylistAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.editor.addFilesToPlaylist";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			FileDialog dialog = new FileDialog(window.getShell(), SWT.OPEN | SWT.MULTI);
			if (dialog.open() != null) {
				final Set<File> selectedFiles = Arrays.asList(dialog.getFileNames()).stream().map(f -> new File(dialog.getFilterPath(), f)).collect(Collectors.toSet());
				Wrapper<EditablePlaylistFolder> folder = Wrapper.empty();
				Wrapper<Set<EditablePlaylistFile>> addedFiles = Wrapper.empty();
				JFaceExec.builder(window.getShell(), "Add Files To Playlist")
					.ui(ctx -> folder.set(playlistEditor.getFirstSelectedFolder().orElse(null)))
					.worker(ctx -> addedFiles.set(playlistEditor.getPlaylist().addFiles(selectedFiles, folder.orElse(null))))
					.ui(ctx -> {
						playlistEditor.refresh();
						playlistEditor.setSelection(addedFiles.orElse(Set.of()).toArray());
					})
					.exceptionalDialog("Add files to playlist failed")
					.runProgressMonitorDialog(true, false);
			}
		}
		return null;
	}

}
