package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.util.Set;

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
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;

public class MoveToFolderAction extends AbstractHandler {
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
				final Wrapper<PlaylistSelection> selection = Wrapper.empty();
				final Wrapper<Set<EditablePlaylistFile>> movedFiles = Wrapper.empty();
				JFaceExec.builder(window.getShell(), "Move To Folder")
					.ui(ctx -> selection.set(playlistEditor.getSelection()))
					.worker(ctx -> {
						EditablePlaylistFolder folder = (EditablePlaylistFolder) dialog.getFirstResult();
						movedFiles.set(playlistEditor.getPlaylist().moveToFolder(selection.orElse(PlaylistSelection.EMPTY).getFolders(), selection.orElse(PlaylistSelection.EMPTY).getFiles(), folder));
					})
					.ui(ctx -> {
						playlistEditor.refresh();
						playlistEditor.setSelection(movedFiles.orElse(Set.of()).toArray());
					})
					.exceptionalDialog("Cannot move folder")
					.runProgressMonitorDialog(true, false);
			}
		}
		return null;
	}

}
