package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistSelection;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

public class MoveToFolderAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(MoveToFolderAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.editor.moveToFolder";
	private static final Image FOLDER_IMAGE = Activator.getDefault().getImage(ImageKey.FOLDER);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			Set<EditablePlaylistFolder> folders = playlistEditor.getPlaylist().getAvaiableFolders(true);
			
			if (playlistEditor.getSelection().isEmpty()) {
				MessageDialog.openError(window.getShell(), "Move To Folder", "Please select a file or folder.");
				return null;
			}
			
			final LabelProvider labelProvider = LabelProvider.createTextImageProvider(o -> ((EditablePlaylistFolder) o).getPath(), o -> FOLDER_IMAGE);
			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(window.getShell(), labelProvider);
			dialog.setTitle("Move Selected Files To Folder");
			dialog.setMessage("Choose a folder:");
			dialog.setElements(folders.toArray());
			int status = dialog.open();
			if (status == IDialogConstants.OK_ID) {
				JFaceThreadRunner runner = new JFaceThreadRunner("Move To Folder", window.getShell(), List.of(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2)) {
					private PlaylistSelection selection;
					private Set<EditablePlaylistFile> movedFiles;

					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case UI_1:
							selection = playlistEditor.getSelection();
							break;
						case WORKER_1:
							EditablePlaylistFolder folder = (EditablePlaylistFolder) dialog.getFirstResult();
							movedFiles = playlistEditor.getPlaylist().moveToFolder(selection.getFolders(), selection.getFiles(), folder);
							break;
						case UI_2:
							playlistEditor.refresh();
							playlistEditor.setSelection(movedFiles.toArray());
							break;
						case UI_ERROR:
							Exception e = super.getException();
							logger.error(e.getMessage(), e);
							MessageDialog.openError(window.getShell(), super.getName(), e.getMessage());
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
