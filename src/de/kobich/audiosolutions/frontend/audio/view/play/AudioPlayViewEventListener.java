package de.kobich.audiosolutions.frontend.audio.view.play;

import java.io.File;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistSelection;
import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.component.file.FileDescriptor;

public class AudioPlayViewEventListener extends EventListenerAdapter {
	private AudioPlayView view;

	public AudioPlayViewEventListener(AudioPlayView view) {
		super(view, ListenerType.COLLECTION_EDITOR_SELECTION, ListenerType.SELECTION, ListenerType.PART);
		this.view = view;
	}

	/**
	 * Selection listener
	 */
	@Override
	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) {
		if (workbenchPart instanceof AudioCollectionEditor collectionEditor) {
			FileDescriptorSelection fileDescriptorSelection = collectionEditor.getFileDescriptorSelection();
			List<File> files = fileDescriptorSelection.getExistingFiles().stream().map(FileDescriptor::getFile).toList();
			if (!files.isEmpty()) {
				view.fireSelection(files);
			}
			else {
				view.fireDeselection();
			}
		}
		else if (workbenchPart instanceof PlaylistEditor playlistEditor) {
			// TODO playlist add selection
			PlaylistSelection playlistSelection = playlistEditor.getSelection();
			List<File> files = playlistSelection.getExistingFiles().stream().map(EditablePlaylistFile::getFile).toList();
			if (!files.isEmpty()) {
				view.fireSelection(files);
			}
			else {
				view.fireDeselection();
			}
		}
	}

	@Override
	public void partClosed(IWorkbenchPartReference partReference) {
		IEditorPart editorPart = partReference.getPage().getActiveEditor();
		if (editorPart == null) {
			view.fireDeselection();
		}
	}
	
	@Override
	public void partOpened(IWorkbenchPartReference partReference) {
		// fire event of active editor if this view is opened
		if (AudioPlayView.ID.equals(partReference.getId())) {
			super.fireSelectionChangedOfActiveEditor(partReference);
		}
	}

}