package de.kobich.audiosolutions.frontend.audio.view.describe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;

public class AudioDescriptionViewEventListener extends EventListenerAdapter {
	private AudioDescriptionView view;
	private boolean visible;

	public AudioDescriptionViewEventListener(AudioDescriptionView view) {
		super(view, ListenerType.COLLECTION_EDITOR_SELECTION, ListenerType.SELECTION, ListenerType.PART);
		this.view = view;
	}

	/**
	 * Selection listener
	 */
	@Override
	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) {
		if (workbenchPart instanceof ICollectionEditor) {
			ICollectionEditor collectionEditor = (ICollectionEditor) workbenchPart;
			FileDescriptorSelection util = collectionEditor.getFileDescriptorSelection();
			
			List<FileDescriptor> audioFiles = new ArrayList<FileDescriptor>();
			Set<FileDescriptor> fileDescriptors = util.getFileDescriptors();
			for (FileDescriptor fileDescriptor : fileDescriptors) {
				if (fileDescriptor.hasMetaData(AudioData.class)) {
					AudioData audioData = (AudioData) fileDescriptor.getMetaData();
					if (audioData.getState().isPersistent()) {
						audioFiles.add(fileDescriptor);
					}
				}
			}
		
			if (visible) {
				if (!audioFiles.isEmpty()) {
					Collections.sort(audioFiles, new DefaultFileDescriptorComparator());
					view.fireSelection(audioFiles);
				}
				else {
					view.fireDeselection();
				}
			}
		}
		else {
			view.fireDeselection();
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
		if (AudioDescriptionView.ID.equals(partReference.getId())) {
			super.fireSelectionChangedOfActiveEditor(partReference);
		}
	}
	
	/**
	 * View listener
	 */
	@Override
	public void partHidden(IWorkbenchPartReference partReference) {
		if (AudioDescriptionView.ID.equals(partReference.getId())) {
			this.visible = false;
		}
	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partReference) {
		if (AudioDescriptionView.ID.equals(partReference.getId())) {
			this.visible = true;
			super.fireSelectionChangedOfActiveEditor(partReference);
		}
	}

}