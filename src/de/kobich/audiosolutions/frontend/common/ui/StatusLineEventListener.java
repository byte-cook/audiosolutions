package de.kobich.audiosolutions.frontend.common.ui;

import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistSelection;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;

public class StatusLineEventListener implements ISelectionListener {
	public static final StatusLineEventListener INSTANCE = new StatusLineEventListener();
	
	public StatusLineContributionItem selectedItem;
	public StatusLineContributionItem availableItem;

	@Override
	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) {
		// if an editor is selected
		if (workbenchPart instanceof ICollectionEditor collectionEditor) {
			updateStatusLine(collectionEditor, collectionEditor.getFileDescriptorSelection());
		}
		else if (workbenchPart instanceof PlaylistEditor playlistEditor) {
			updateStatusLine(playlistEditor, playlistEditor.getSelection());
		}
		else {
			selectedItem.setText("");
			availableItem.setText("");
		}
	}
	
	private void updateStatusLine(ICollectionEditor collectionEditor, FileDescriptorSelection selection) {
		int selected = selection.getFileDescriptors().size();
		int nonExisting = selection.getNonExistingFiles().size();
		setSelectedText(selected, nonExisting);
		
		int available = collectionEditor.getFileCollection().getFileDescriptors().size();
		setAvailableText(available);
	}
	
	private void updateStatusLine(PlaylistEditor playlistEditor, PlaylistSelection selection) {
		int selected = selection.getAllFiles().size();
		int nonExisting = selected - selection.getExistingFiles().size();
		setSelectedText(selected, nonExisting);
		
		int available = playlistEditor.getPlaylist().getAllFiles().size();
		setAvailableText(available);
	}
	
	private void setSelectedText(int selected, int nonExisting) {
		if (selected > 0) {
			String selectedText = String.format(selected > 1 ? "%d files selected" : "%d file selected", selected);
			if (nonExisting > 0) {
				selectedText += String.format(nonExisting > 1 ? " (%d files do not exist)" : " (%d file does not exist)", nonExisting);
			}
			selectedItem.setText(selectedText);
		}
		else {
			selectedItem.setText("");
		}
	}
	
	private void setAvailableText(int available) {
		if (available > 0) {
			String availableText = String.format(available > 1 ? "%d files" : "%d file", available);
			availableItem.setText(availableText);
		}
		else {
			availableItem.setText("");
		}
	}
}
