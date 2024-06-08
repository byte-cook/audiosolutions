package de.kobich.audiosolutions.frontend.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import de.kobich.audiosolutions.frontend.common.listener.IUIEventListener;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.component.file.FileDescriptor;

public class FileDescriptorSourceProvider extends AbstractSourceProvider implements ISelectionListener, IUIEventListener, IPartListener2 {
	private static final String FILE_DESCRIPTOR_SELECTION = "fileDescriptorSelection";
	private static final String ACTIVE_COLLECTION_EDITOR = "activeCollectionEditor";
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { FILE_DESCRIPTOR_SELECTION, ACTIVE_COLLECTION_EDITOR };
	
	private Set<FileDescriptor> fileDescriptors;
	private IWorkbenchPart activeCollectionEditor;
	
	public static FileDescriptorSourceProvider getInstance() {
		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		return (FileDescriptorSourceProvider) sourceProviderService.getSourceProvider(FILE_DESCRIPTOR_SELECTION);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		Map<String, Object> map = new HashMap<>();
		map.put(FILE_DESCRIPTOR_SELECTION, this.fileDescriptors);
		map.put(ACTIVE_COLLECTION_EDITOR, this.activeCollectionEditor);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
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
		if (partRef.getPart(true).equals(SelectionManager.INSTANCE.getActiveEditor())) {
			this.fileDescriptors = new HashSet<>();
			fireSourceChanged(ISources.WORKBENCH, FILE_DESCRIPTOR_SELECTION, fileDescriptors);
		}
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if (partRef.getPart(true).equals(SelectionManager.INSTANCE.getActiveEditor())) {
			ICollectionEditor collectionEditor = SelectionManager.INSTANCE.getActiveEditor(ICollectionEditor.class);
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
