package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.io.File;

import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor.CollectionEditorType;


public class ImportOpeningInfo implements IOpeningInfo {
	private static final long serialVersionUID = 6814438343268381156L;
	private File file;
	
	public ImportOpeningInfo(File file) {
		this.file = file;
	}

	/**
	 * @return the file
	 */
	public File getSourceFile() {
		return file;
	}

	@Override
	public String getName() {
		return getSourceFile().getAbsolutePath();
	}

	@Override
	public CollectionEditorType getEditorType() {
		return CollectionEditorType.IMPORT;
	}
}
