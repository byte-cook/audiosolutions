package de.kobich.audiosolutions.frontend.common.ui.editor;

import org.eclipse.ui.IEditorPart;

import de.kobich.audiosolutions.core.service.info.FileInfo;
import de.kobich.audiosolutions.frontend.common.listener.AudioDelta;
import de.kobich.audiosolutions.frontend.common.listener.FileDelta;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;

/**
 * File collection editors.
 */
public interface ICollectionEditor extends IEditorPart {
	public static enum CollectionEditorType {
		DIRECTORY("Directory"), SEARCH("Search"), IMPORT("Import");
		
		private final String label;
		
		private CollectionEditorType(String label) {
			this.label = label;
		}
		
		public String getLabel() {
			return this.label;
		}
	}
	
	/**
	 * Returns the editor input
	 */
	FileCollection getFileCollection();
	
	/**
	 * Returns the current selection
	 */
	FileDescriptorSelection getFileDescriptorSelection();
	
	/**
	 * Switches the layout
	 */
	void switchLayout();

	/**
	 * Updates this editor
	 */
	void update(AudioDelta delta);

	/**
	 * Updates this editor
	 */
	void update(FileDelta delta);
	
	/**
	 * Shows stream as logo
	 * @param is
	 */
	void showLogo(FileInfo fileInfo);
	
	/**
	 * Shows default logo
	 */
	void showDefaultLogo();
}
