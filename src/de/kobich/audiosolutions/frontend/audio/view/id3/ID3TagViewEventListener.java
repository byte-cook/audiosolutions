package de.kobich.audiosolutions.frontend.audio.view.id3;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.component.file.FileDescriptor;

public class ID3TagViewEventListener extends EventListenerAdapter {
	private ID3TagView view;
	private boolean visible;

	public ID3TagViewEventListener(ID3TagView view) {
		super(view, ListenerType.COLLECTION_EDITOR_SELECTION, ListenerType.SELECTION, ListenerType.PART);
		this.view = view;
	}

	@Override
	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) {
		if (workbenchPart instanceof ICollectionEditor) {
			ICollectionEditor collectionEditor = (ICollectionEditor) workbenchPart;
			FileDescriptorSelection util = collectionEditor.getFileDescriptorSelection();
			Set<FileDescriptor> fileDescriptors = util.getExistingFiles();
			
			Set<FileDescriptor> mp3Files = new HashSet<>(CollectionUtils.select(fileDescriptors, ID3TagViewPredicate.INSTANCE));
			if (visible) {
				if (!mp3Files.isEmpty()) {
					view.fireSelection(mp3Files);
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
		if (ID3TagView.ID.equals(partReference.getId())) {
			super.fireSelectionChangedOfActiveEditor(partReference);
		}
	}
	
	@Override
	public void partHidden(IWorkbenchPartReference partReference) {
		if (ID3TagView.ID.equals(partReference.getId())) {
			this.visible = false;
		}
	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partReference) {
		if (ID3TagView.ID.equals(partReference.getId())) {
			this.visible = true;
			super.fireSelectionChangedOfActiveEditor(partReference);
		}
	}

}