package de.kobich.audiosolutions.frontend.file;

import java.io.File;
import java.util.List;

/**
 * Represents files.
 */
public interface IFileSource extends IUISource {
	/**
	 * Returns file 
	 * @return file 
	 */
	public List<File> getFiles();
}
