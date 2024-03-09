package de.kobich.audiosolutions.frontend.common.listener;

import java.util.HashSet;
import java.util.Set;

import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorDelta;
import de.kobich.component.file.FileDescriptor;

public class AudioDelta {
	private final ActionType actionType;
	private final CollectionEditorDelta editorDelta;
	private final Set<FileDescriptor> insertItems;
	private final Set<FileDescriptor> updateItems;
	private final Set<FileDescriptor> deleteItems;

	public AudioDelta(ActionType type, CollectionEditorDelta editorDelta) {
		this.actionType = type;
		this.editorDelta = editorDelta;
		this.insertItems = new HashSet<FileDescriptor>();
		this.updateItems = new HashSet<FileDescriptor>();
		this.deleteItems = new HashSet<FileDescriptor>();
	}

	public ActionType getActionType() {
		return actionType;
	}

	public CollectionEditorDelta getEditorDelta() {
		return editorDelta;
	}

	public Set<FileDescriptor> getInsertItems() {
		return insertItems;
	}

	public Set<FileDescriptor> getUpdateItems() {
		return updateItems;
	}

	public Set<FileDescriptor> getDeleteItems() {
		return deleteItems;
	}
	
	public Set<FileDescriptor> getAllItems() {
		Set<FileDescriptor> items = new HashSet<FileDescriptor>();
		items.addAll(insertItems);
		items.addAll(updateItems);
		items.addAll(deleteItems);
		return items;
	}
}
