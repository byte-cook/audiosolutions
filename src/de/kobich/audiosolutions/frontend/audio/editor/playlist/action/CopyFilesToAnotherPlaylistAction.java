package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditorInput;
import de.kobich.audiosolutions.frontend.common.AudioSolutionsConstant;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;

public class CopyFilesToAnotherPlaylistAction extends AbstractHandler {
	private static final Image PLAYLIST_NEW_IMAGE = Activator.getDefault().getImage(ImageKey.PLAYLIST_NEW);
	private static final Image PLAYLIST_IMAGE = Activator.getDefault().getImage(ImageKey.PLAYLIST);
	public static final String ID = "de.kobich.audiosolutions.commands.editor.copyFilesToAnotherPlaylist";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			Collection<EditablePlaylistFile> files = playlistEditor.getSelection().getAllFiles();
			if (files.isEmpty()) {
				files = playlistEditor.getPlaylist().getAllFiles();
			}

			PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
			List<Playlist> availablePlaylists = new ArrayList<>(); 
			playlistService.getPlaylists(null).stream().filter(p -> p.getId() != playlistEditor.getPlaylist().getId().orElse(null)).forEach(p -> availablePlaylists.add(p));
			availablePlaylists.add(AudioSolutionsConstant.NEW_PLAYLIST);
			
			// dialog
			final LabelProvider labelProvider = LabelProvider.createTextImageProvider(o -> ((Playlist) o).getName(), o -> o.equals(AudioSolutionsConstant.NEW_PLAYLIST) ? PLAYLIST_NEW_IMAGE : PLAYLIST_IMAGE);
			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(window.getShell(), labelProvider);
			dialog.setTitle("Copy Selected Files To Playlist");
			dialog.setMessage("Choose a playlist:");
			dialog.setElements(availablePlaylists.toArray());
			int status = dialog.open();
			if (status == IDialogConstants.OK_ID) {
				final Playlist targetPlaylist = (Playlist) dialog.getFirstResult();
				final Set<EditablePlaylistFile> filesFinal = new HashSet<>(files);
				
				final Wrapper<PlaylistEditor> editor = Wrapper.empty();
				JFaceExec.builder(window.getShell(), "Copy Files To Playlist")
					.ui(ctx -> {
						// open playlist editor
						EditablePlaylist editablePlaylist;
						if (targetPlaylist.equals(AudioSolutionsConstant.NEW_PLAYLIST)) {
							editablePlaylist = playlistService.createNewPlaylist("", false);
						}
						else {
							editablePlaylist = playlistService.openPlaylist(targetPlaylist, ctx.getProgressMonitor());
						}
						PlaylistEditorInput input = new PlaylistEditorInput(editablePlaylist);
						editor.set((PlaylistEditor) window.getActivePage().openEditor(input, PlaylistEditor.ID));
					})
					.worker(ctx -> editor.get().getPlaylist().copyFiles(filesFinal))
					.ui(ctx -> editor.get().refresh())
					.exceptionalDialog("Add files to playlist failed")
					.runProgressMonitorDialog(true, false);
				
			}
		}
		return null;
	}

}
