package de.kobich.audiosolutions.frontend.file.view.rename.model;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.IRenameAttributeProvider;
import de.kobich.component.file.descriptor.RenameFileDescriptor;
import lombok.Getter;
import lombok.Setter;


/**
 * Model for renaming files view.
 */
public class FileModel {
	@Getter
	private final Set<RenameFileDescriptor> renameables;
	@Setter
	@Nullable
	private IRenameAttributeProvider attributeProvider;
	
	public FileModel() {
		this.renameables = new HashSet<RenameFileDescriptor>();
	}
	
	/**
	 * Add file
	 * @param file
	 */
	public void addFile(FileDescriptor fileDescriptor) {
		RenameFileDescriptor renameable = new RenameFileDescriptor(fileDescriptor, attributeProvider);
		renameables.add(renameable);
	}
	
	public void removeFile(FileDescriptor fileDescriptor) {
		RenameFileDescriptor renameable = new RenameFileDescriptor(fileDescriptor, attributeProvider);
		renameables.remove(renameable);
	}
	
	public void reload() {
		for (RenameFileDescriptor renameable : renameables) {
			renameable.reload();
		}
	}
	
	/**
	 * Resets the preview data
	 */
	public void reset() {
		for (RenameFileDescriptor renameable : renameables) {
			RenameFileDescriptor previewRenameable = renameable; 
			previewRenameable.setName(previewRenameable.getFileDescriptor().getFileName());
		}
	}
	
	/**
	 * Remove all items
	 */
	public void clear() {
		renameables.clear();
	}
	
	/**
	 * Returns the number of elements
	 */
	public int size() {
		return renameables.size();
	}
	
	/**
	 * Returns true if this list contains no elements 
	 * @return
	 */
	public boolean isEmpty() {
		return renameables.isEmpty();
	}
}
