package de.kobich.audiosolutions.frontend.audio.view.play;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
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
		// only check selection of audio collection editor
		if (workbenchPart instanceof AudioCollectionEditor) {
			AudioCollectionEditor collectionEditor = (AudioCollectionEditor) workbenchPart;
			FileDescriptorSelection util = collectionEditor.getFileDescriptorSelection();

			List<FileDescriptor> audioFiles = new ArrayList<FileDescriptor>();
			Set<FileDescriptor> fileDescriptors = util.getFileDescriptors();
			for (FileDescriptor fileDescriptor : fileDescriptors) {
				audioFiles.add(fileDescriptor);
			}
	
			if (!audioFiles.isEmpty()) {
				view.fireSelection(audioFiles);
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