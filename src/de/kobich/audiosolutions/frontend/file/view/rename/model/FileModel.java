package de.kobich.audiosolutions.frontend.file.view.rename.model;

import java.util.HashSet;
import java.util.Set;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.component.file.FileDescriptor;


/**
 * Model for renaming files view.
 */
public class FileModel {
	private Set<RenameFileDescriptor> renameables;
	
	public FileModel() {
		this.renameables = new HashSet<RenameFileDescriptor>();
	}
	
	/**
	 * @return the selectedFileProviders
	 */
	public Set<RenameFileDescriptor> getRenameables() {
		return renameables;
	}
	
	/**
	 * Add file
	 * @param file
	 */
	public void addFile(FileDescriptor fileDescriptor) {
		IFileID3TagService id3TagService = AudioSolutions.getService(IFileID3TagService.JAUDIO_TAGGER, IFileID3TagService.class);
		RenameAttributeProvider attributeProvider = new RenameAttributeProvider(fileDescriptor, id3TagService);
		RenameFileDescriptor renameable = new RenameFileDescriptor(fileDescriptor, attributeProvider);
		renameables.add(renameable);
	}
	
	public void removeFile(FileDescriptor fileDescriptor) {
		IFileID3TagService id3TagService = AudioSolutions.getService(IFileID3TagService.JAUDIO_TAGGER, IFileID3TagService.class);
		RenameAttributeProvider attributeProvider = new RenameAttributeProvider(fileDescriptor, id3TagService);
		RenameFileDescriptor renameable = new RenameFileDescriptor(fileDescriptor, attributeProvider);
		renameables.remove(renameable);
	}
	
	public void reloadFiles() {
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
