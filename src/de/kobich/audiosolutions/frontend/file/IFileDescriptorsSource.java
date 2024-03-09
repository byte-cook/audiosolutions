package de.kobich.audiosolutions.frontend.file;

import java.util.Set;

import de.kobich.component.file.FileDescriptor;

/**
 * Represents file descriptors
 */
public interface IFileDescriptorsSource extends IUISource {
	/**
	 * Returns file descriptors
	 * @return file descriptors
	 */
	public Set<FileDescriptor> getFileDescriptors();
}
