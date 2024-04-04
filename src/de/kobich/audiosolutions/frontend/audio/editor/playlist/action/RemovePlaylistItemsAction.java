package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistSelection;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

public class RemovePlaylistItemsAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(RemovePlaylistItemsAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.editor.removePlaylistItems";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor(); //SelectionSupport.INSTANCE.getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			PlaylistSelection selection = playlistEditor.getSelection();
			if (selection.isEmpty()) {
				boolean confirmed = MessageDialog.openQuestion(window.getShell(), "Remove Files/Folders", "Do you want to delete the complete playlist?");
				if (confirmed) {
					JFaceThreadRunner runner = new JFaceThreadRunner("Remove Files/Folders", window.getShell(), List.of(RunningState.WORKER_1, RunningState.UI_1)) {
						@Override
						protected void run(RunningState state) throws Exception {
							switch (state) {
							case WORKER_1:
								playlistEditor.getPlaylist().getFolders().clear();
								break;
							case UI_1:
								playlistEditor.refresh();
								break;
							case UI_ERROR:
								Exception e = super.getException();
								logger.error(e.getMessage(), e);
								String msg = "Removing items failed: " + e.getMessage();
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
			else {
				JFaceThreadRunner runner = new JFaceThreadRunner("Remove Files/Folders", window.getShell(), List.of(RunningState.WORKER_1, RunningState.UI_1)) {
					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case WORKER_1:
							EditablePlaylist playlist = playlistEditor.getPlaylist();
							for (EditablePlaylistFolder folder : selection.getFolders()) {
								playlist.remove(folder);
							}
							for (EditablePlaylistFile file : selection.getFiles()) {
								playlist.remove(file);
							}
							break;
						case UI_1:
							playlistEditor.refresh();
							break;
						case UI_ERROR:
							Exception e = super.getException();
							logger.error(e.getMessage(), e);
							String msg = "Removing items failed: " + e.getMessage();
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
