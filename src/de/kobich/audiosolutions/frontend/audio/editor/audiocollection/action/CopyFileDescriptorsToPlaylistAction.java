package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditorInput;
import de.kobich.audiosolutions.frontend.common.AudioSolutionsConstant;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

public class CopyFileDescriptorsToPlaylistAction extends AbstractHandler {
	private static final Image PLAYLIST_NEW_IMAGE = Activator.getDefault().getImage(ImageKey.PLAYLIST_NEW);
	private static final Image PLAYLIST_IMAGE = Activator.getDefault().getImage(ImageKey.PLAYLIST);
	public static final String ID = "de.kobich.audiosolutions.commands.editor.copyFileDescriptorsToPlaylist";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IEditorPart editorPart = window.getActivePage().getActiveEditor();
		
		Set<FileDescriptor> fileDescriptors = Set.of();
		if (editorPart instanceof AudioCollectionEditor audioCollectionEditor) {
			fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
			if (fileDescriptors.isEmpty()) {
				fileDescriptors = audioCollectionEditor.getFileCollection().getFileDescriptors();
			}
			
			PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
			List<Playlist> availablePlaylists = new ArrayList<>(); 
			playlistService.getPlaylists(null).stream().forEach(p -> availablePlaylists.add(p));
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
				final Set<File> files = fileDescriptors.stream().map(FileDescriptor::getFile).collect(Collectors.toSet());
				
				List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
				JFaceThreadRunner runner = new JFaceThreadRunner("Copy Files To Playlist", window.getShell(), states) {
					private PlaylistEditor editor;
					private Set<EditablePlaylistFile> addedFiles;
	
					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case UI_1:
							// open playlist editor
							EditablePlaylist editablePlaylist;
							if (targetPlaylist.equals(AudioSolutionsConstant.NEW_PLAYLIST)) {
								editablePlaylist = playlistService.createNewPlaylist("", false);
							}
							else {
								editablePlaylist = playlistService.openPlaylist(targetPlaylist, super.getProgressMonitor());
							}
							PlaylistEditorInput input = new PlaylistEditorInput(editablePlaylist);
							editor = (PlaylistEditor) window.getActivePage().openEditor(input, PlaylistEditor.ID);
							break;
						case WORKER_1:
							addedFiles = editor.getPlaylist().addFiles(files, null);
							break;
						case UI_2:
							editor.refresh();
							editor.setSelection(addedFiles.toArray());
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
