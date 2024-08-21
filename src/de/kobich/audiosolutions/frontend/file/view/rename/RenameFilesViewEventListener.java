package de.kobich.audiosolutions.frontend.file.view.rename;

import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesPreviewListener;
import de.kobich.component.file.FileDescriptor;

public class RenameFilesViewEventListener extends EventListenerAdapter {
	private final RenameFilesView view;
	private final RenameFilesPreviewListener previewListener;

	public RenameFilesViewEventListener(RenameFilesView view, RenameFilesPreviewListener previewListener) {
		super(view, ListenerType.COLLECTION_EDITOR_SELECTION, ListenerType.PART, ListenerType.UI_EVENT);
		this.view = view;
		this.previewListener = previewListener;
	}

	/**
	 * Selection listener
	 */
	@Override
	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) {
		if (workbenchPart instanceof AudioCollectionEditor collectionEditor) {
			FileDescriptorSelection util = collectionEditor.getFileDescriptorSelection();
			
			Set<FileDescriptor> fileDescriptors = util.getFileDescriptors();
			if (!fileDescriptors.isEmpty()) {
				view.getFileModel().clear();
				for (FileDescriptor fileDescriptor : fileDescriptors) {
					if (fileDescriptor.getFile().exists()) {
						view.getFileModel().addFile(fileDescriptor);
					}
				}
				if (view.getFileModel().isEmpty()) {
					view.fireDeselection();
				}
				else {
					view.fireSelection();
				}
			}
			else {
				view.fireDeselection();
			}
		}
		else {
			view.fireDeselection();
		}
	}

	@Override
	public void partOpened(IWorkbenchPartReference partReference) {
		// fire event of active editor if this view is opened
		if (RenameFilesView.ID.equals(partReference.getId())) {
			super.fireSelectionChangedOfActiveEditor(partReference);
		}
	}
	
	@Override
	public void eventFired(UIEvent event) {
		switch (event.getActionType()) {
		case FILE:
			view.getFileModel().reload();
			previewListener.updatePreview();
			break;
		case AUDIO_DATA:
			previewListener.updatePreview();
			break;
		default:
			break;
		}
	}
	
}
