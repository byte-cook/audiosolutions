package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.lang.Nullable;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

public class AddFilesToPlaylistAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(AddFilesToPlaylistAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.editor.addFilesToPlaylist";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor(); //SelectionSupport.INSTANCE.getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			FileDialog dialog = new FileDialog(window.getShell(), SWT.OPEN | SWT.MULTI);
			if (dialog.open() != null) {
				Set<File> selectedFiles = Arrays.asList(dialog.getFileNames()).stream().map(f -> new File(dialog.getFilterPath(), f)).collect(Collectors.toSet());
				JFaceThreadRunner runner = new JFaceThreadRunner("Add Files To Playlist", window.getShell(), List.of(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2)) {
					@Nullable
					private EditablePlaylistFolder folder;
					private Set<EditablePlaylistFile> addedFiles;

					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case UI_1:
							folder = playlistEditor.getFirstSelectedFolder().orElse(null);
							break;
						case WORKER_1:
							addedFiles = playlistEditor.getPlaylist().addFiles(selectedFiles, folder);
							break;
						case UI_2:
							playlistEditor.refresh();
							playlistEditor.setSelection(addedFiles.toArray());
							break;
						case UI_ERROR:
							Exception e = super.getException();
							logger.error(e.getMessage(), e);
							String msg = "Add files to playlist failed: " + e.getMessage();
							MessageDialog.openError(window.getShell(), super.getName(), msg);
							break;
						default:
							break;
						}
					}
				};
				runner.runProgressMonitorDialog(true, false);
			}
		}
		return null;
	}

}
