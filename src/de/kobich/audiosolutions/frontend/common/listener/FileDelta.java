package de.kobich.audiosolutions.frontend.common.listener;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.frontend.common.FileDescriptorConverter;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorDelta;
import de.kobich.commons.converter.ConverterUtils;
import de.kobich.component.file.FileResult;
import de.kobich.component.file.descriptor.FileDescriptorResult;

public class FileDelta {
	private final ActionType actionType;
	private final CollectionEditorDelta editorDelta;
	private final Set<File> createItems;
	private final Set<File> modifyItems;
	private final Set<File> deleteItems;

	public FileDelta(ActionType type, CollectionEditorDelta editorDelta) {
		this.actionType = type;
		this.editorDelta = editorDelta;
		this.createItems = new HashSet<File>();
		this.modifyItems = new HashSet<File>();
		this.deleteItems = new HashSet<File>();
	}

	public ActionType getActionType() {
		return actionType;
	}

	public CollectionEditorDelta getEditorDelta() {
		return editorDelta;
	}

	public Set<File> getCreateItems() {
		return createItems;
	}

	public Set<File> getModifyItems() {
		return modifyItems;
	}

	public Set<File> getDeleteItems() {
		return deleteItems;
	}
	
	public void copyFromResult(FileResult result) {
		this.getCreateItems().addAll(result.getCreatedFiles());
		this.getDeleteItems().addAll(result.getDeletedFiles());
	}

	public void copyFromResult(AudioFileResult result) {
		this.getCreateItems().addAll(result.getCreatedFiles());
	}
	
	public void copyFromResult(FileDescriptorResult result) {
		Collection<File> addedFiles = ConverterUtils.convert(result.getAddedFiles(), FileDescriptorConverter.INSTANCE);
		this.getCreateItems().addAll(addedFiles);
		Collection<File> deletedFiles = ConverterUtils.convert(result.getRemovedFiles(), FileDescriptorConverter.INSTANCE);
		this.getDeleteItems().addAll(deletedFiles);
		Collection<File> modifyFiles = ConverterUtils.convert(result.getUpdatedFiles(), FileDescriptorConverter.INSTANCE);
		this.getModifyItems().addAll(modifyFiles);
	}
	
	public Set<File> getAllItems() {
		Set<File> items = new HashSet<File>();
		items.addAll(createItems);
		items.addAll(modifyItems);
		items.addAll(deleteItems);
		return items;
	}
}
