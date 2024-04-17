package de.kobich.audiosolutions.frontend.audio.editor.search.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditorInput;
import de.kobich.audiosolutions.frontend.common.AudioSolutionsConstant;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.component.file.FileDescriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CopyFilesToAnotherPlaylistSelectionAdapter extends SelectionAdapter {
	private static final Image PLAYLIST_NEW_IMAGE = Activator.getDefault().getImage(ImageKey.PLAYLIST_NEW);
	private static final Image PLAYLIST_IMAGE = Activator.getDefault().getImage(ImageKey.PLAYLIST);
	private final IWorkbenchWindow window;
	private final AudioSearchQuery query;
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
		List<Playlist> availablePlaylists = new ArrayList<>(); 
		playlistService.getPlaylists(null).stream().forEach(p -> availablePlaylists.add(p));
		availablePlaylists.add(AudioSolutionsConstant.NEW_PLAYLIST);
		
		// dialog
		final LabelProvider labelProvider = LabelProvider.createTextImageProvider(o -> ((Playlist) o).getName(), o -> o.equals(AudioSolutionsConstant.NEW_PLAYLIST) ? PLAYLIST_NEW_IMAGE : PLAYLIST_IMAGE);
		final ElementListSelectionDialog dialog = new ElementListSelectionDialog(window.getShell(), labelProvider);
		dialog.setTitle("Copy Files To Playlist");
		dialog.setMessage("Choose a playlist:");
		dialog.setElements(availablePlaylists.toArray());
		int status = dialog.open();
		if (status == IDialogConstants.OK_ID) {
			final Playlist targetPlaylist = (Playlist) dialog.getFirstResult();

			final Wrapper<PlaylistEditor> editor = Wrapper.empty();
			final Wrapper<Set<EditablePlaylistFile>> addedFiles = Wrapper.empty();
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
				.worker(ctx -> {
					AudioSearchService searchService = AudioSolutions.getService(AudioSearchService.class);
					Set<FileDescriptor> fileDescriptors = searchService.search(query, ctx.getProgressMonitor());
					Set<File> files = fileDescriptors.stream().map(FileDescriptor::getFile).collect(Collectors.toSet());
					addedFiles.set(editor.get().getPlaylist().addFiles(files, null));
				})
				.ui(ctx -> {
					editor.ifPresent(pe -> {
						pe.refresh();
						pe.setSelection(addedFiles.orElse(Set.of()).toArray());
					});
				})
				.exceptionalDialog("Cannot copy files to playlist")
				.runProgressMonitorDialog(true, false);
			
		}
	}
}
