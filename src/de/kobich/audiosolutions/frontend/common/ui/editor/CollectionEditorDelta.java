package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.FileDescriptorResult;

/**
 * Delta of file collections which belongs to one editor.
 */
public class CollectionEditorDelta {
	private final ActionType actionType;
	private final ICollectionEditor actionEditor;
	private final Set<FileDescriptor> addItems;
	private final Set<FileDescriptor> updateItems;
	private final Set<FileDescriptor> removeItems;
	private final Map<FileDescriptor, FileDescriptor> replaceItems;
	
	public CollectionEditorDelta(ActionType type, ICollectionEditor actionEditor) {
		this.actionType = type;
		this.actionEditor = actionEditor;
		this.addItems = new HashSet<FileDescriptor>();
		this.updateItems = new HashSet<FileDescriptor>();
		this.removeItems = new HashSet<FileDescriptor>();
		this.replaceItems = new HashMap<FileDescriptor, FileDescriptor>();
	}

	public ActionType getActionType() {
		return actionType;
	}
	
	public boolean isActionEditor(ICollectionEditor editor) {
		return this.actionEditor != null ? this.actionEditor.equals(editor) : false;
	}
	
	/**
	 * Returns files which are replaced by other files. This is just an additional information and only used to recreate the selection. 
	 * You still need to use {@link #getAddItems()} and {@link #getRemoveItems()}
	 * @return
	 */
	public Map<FileDescriptor, FileDescriptor> getReplaceItems() {
		return replaceItems;
	}

	public Set<FileDescriptor> getAddItems() {
		return addItems;
	}

	public Set<FileDescriptor> getUpdateItems() {
		return updateItems;
	}

	public Set<FileDescriptor> getRemoveItems() {
		return removeItems;
	}
	
	public void copyFromResult(AudioFileResult result) {
		this.getAddItems().addAll(result.getReplacedFiles().values());
		this.getRemoveItems().addAll(result.getReplacedFiles().keySet());
		this.getReplaceItems().putAll(result.getReplacedFiles());
	}
	
	public void copyFromResult(FileDescriptorResult result) {
		this.getAddItems().addAll(result.getAddedFiles());
		this.getRemoveItems().addAll(result.getRemovedFiles());
		this.getUpdateItems().addAll(result.getUpdatedFiles());
		this.getReplaceItems().putAll(result.getReplacedFiles());
	}
	
	public Set<FileDescriptor> getAllItems() {
		Set<FileDescriptor> allItems = new HashSet<FileDescriptor>();
		allItems.addAll(addItems);
		allItems.addAll(updateItems);
		allItems.addAll(removeItems);
		allItems.addAll(replaceItems.keySet());
		allItems.addAll(replaceItems.values());
		return allItems;
	}
	
}
