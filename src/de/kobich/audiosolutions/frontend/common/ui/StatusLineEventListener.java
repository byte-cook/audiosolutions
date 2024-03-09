package de.kobich.audiosolutions.frontend.common.ui;

import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;

public class StatusLineEventListener implements ISelectionListener, ISelectionChangedListener {
	public static final StatusLineEventListener INSTANCE = new StatusLineEventListener();
	
	public StatusLineContributionItem selectedItem;
	public StatusLineContributionItem availableItem;

	@Override
	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) {
		// if an editor is selected
		if (workbenchPart instanceof ICollectionEditor) {
			ICollectionEditor collectionEditor = (ICollectionEditor) workbenchPart;
			FileDescriptorSelection util = collectionEditor.getFileDescriptorSelection();
			updateStatusLine(collectionEditor, util);
		}
		else {
			selectedItem.setText("");
			availableItem.setText("");
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		selectionChanged(SelectionSupport.INSTANCE.getActiveEditor(), event.getSelection());
	}
	
	private void updateStatusLine(ICollectionEditor collectionEditor, FileDescriptorSelection util) {
		int selected = util.getFileDescriptors().size();
		int nonExists = util.getNonExistingFiles().size();
		int available = collectionEditor.getFileCollection().getFileDescriptors().size();
		
		if (selected > 0) {
			String selectedText = String.format(selected > 1 ? "%d files selected" : "%d file selected", selected);
			if (nonExists > 0) {
				selectedText += String.format(nonExists > 1 ? " (%d files do not exist)" : " (%d file does not exist)", nonExists);
			}
			selectedItem.setText(selectedText);
		}
		else {
			selectedItem.setText("");
		}
		
		if (available > 0) {
			String availableText = String.format(available > 1 ? "%d files" : "%d file", available);
			availableItem.setText(availableText);
		}
		else {
			availableItem.setText("");
		}
	}
}
