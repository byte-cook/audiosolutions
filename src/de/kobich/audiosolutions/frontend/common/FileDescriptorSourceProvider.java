package de.kobich.audiosolutions.frontend.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import de.kobich.audiosolutions.frontend.common.listener.IUIEventListener;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.component.file.FileDescriptor;

public class FileDescriptorSourceProvider extends AbstractSourceProvider implements ISelectionListener, IUIEventListener, ISelectionChangedListener, IPartListener2 {
	public static final String FILE_DESCRIPTOR_SELECTION = "fileDescriptorSelection";
	public static final String ACTIVE_COLLECTION_EDITOR = "activeCollectionEditor";
	private Set<FileDescriptor> fileDescriptors;
	private IWorkbenchPart activeCollectionEditor;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map getCurrentState() {
		Map map = new HashMap(1);
		map.put(FILE_DESCRIPTOR_SELECTION, this.fileDescriptors);
		map.put(ACTIVE_COLLECTION_EDITOR, this.activeCollectionEditor);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { FILE_DESCRIPTOR_SELECTION, ACTIVE_COLLECTION_EDITOR };
	}

	@Override
	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) {
		// if an editor is selected
		if (workbenchPart instanceof ICollectionEditor) {
			ICollectionEditor collectionEditor = (ICollectionEditor) workbenchPart;
			FileDescriptorSelection util = collectionEditor.getFileDescriptorSelection();
			
			this.fileDescriptors = util.getFileDescriptors();
			fireSourceChanged(ISources.WORKBENCH, FILE_DESCRIPTOR_SELECTION, fileDescriptors);
			this.activeCollectionEditor = workbenchPart;
			fireSourceChanged(ISources.WORKBENCH, ACTIVE_COLLECTION_EDITOR, activeCollectionEditor);
		}
	}
	
	@Override
	public void eventFired(UIEvent event) {
		switch (event.getActionType()) {
		case FILE:
		case AUDIO_DATA:
		case AUDIO_SAVED:
			fireSourceChanged(ISources.WORKBENCH, FILE_DESCRIPTOR_SELECTION, fileDescriptors);
			break;
		default: 
			break;
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		selectionChanged(SelectionSupport.INSTANCE.getActiveEditor(), event.getSelection());
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		if (partRef.getPart(true).equals(SelectionSupport.INSTANCE.getActiveEditor())) {
			this.fileDescriptors = new HashSet<>();
			fireSourceChanged(ISources.WORKBENCH, FILE_DESCRIPTOR_SELECTION, fileDescriptors);
		}
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if (partRef.getPart(true).equals(SelectionSupport.INSTANCE.getActiveEditor())) {
			ICollectionEditor collectionEditor = SelectionSupport.INSTANCE.getActiveEditor(ICollectionEditor.class);
			if (collectionEditor == null) {
				return;
			}
			FileDescriptorSelection util = collectionEditor.getFileDescriptorSelection();
			this.fileDescriptors = util.getFileDescriptors();
			fireSourceChanged(ISources.WORKBENCH, FILE_DESCRIPTOR_SELECTION, fileDescriptors);
		}
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}
}
