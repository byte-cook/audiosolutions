package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistSelection;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

public class NewPlaylistFolderAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(NewPlaylistFolderAction.class);
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
				JFaceThreadRunner runner = new JFaceThreadRunner("New Folder", window.getShell(), List.of(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2)) {
					private PlaylistSelection selection;
					private EditablePlaylistFolder folder;

					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case UI_1:
							selection = playlistEditor.getSelection();
							break;
						case WORKER_1:
							this.folder = playlistEditor.getPlaylist().createOrGetFolder(dialog.getFolderName());
							if (!selection.isEmpty() && dialog.isMoveToEnabled()) {
								playlistEditor.getPlaylist().moveToFolder(selection.getFolders(), selection.getFiles(), folder);
							}
							break;
						case UI_2:
							playlistEditor.refresh();
							playlistEditor.setSelection(new Object[] {folder});
							break;
						case UI_ERROR:
							Exception e = super.getException();
							logger.error(e.getMessage(), e);
							String msg = "Create new folder failed: " + e.getMessage();
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
