package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.io.File;
import java.io.FileFilter;

import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor.CollectionEditorType;


public class FileOpeningInfo implements IOpeningInfo {
	private static final long serialVersionUID = -5776407658532844387L;
	private final File directory;
	private final FileFilter fileFilter;
	
	public FileOpeningInfo(File directory, FileFilter fileFilter) {
		this.directory = directory;
		this.fileFilter = fileFilter;
	}

	/**
	 * @return the directory
	 */
	public File getDirectory() {
		return directory;
	}

	public FileFilter getFileFilter() {
		return fileFilter;
	}

	@Override
	public String getName() {
		return directory.getAbsolutePath();
	}

	@Override
	public CollectionEditorType getEditorType() {
		return CollectionEditorType.DIRECTORY;
	}
}
