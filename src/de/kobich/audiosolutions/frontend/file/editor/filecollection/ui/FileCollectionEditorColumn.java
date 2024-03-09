package de.kobich.audiosolutions.frontend.file.editor.filecollection.ui;

public enum FileCollectionEditorColumn {
	FILE_NAME(0, "File Name", 150, 30),
	EXISTS(1, "Exists", 40, 5),
	RELATIVE_PATH(2, "Relative Path", 200, 35),
	EXTENSION(3, "Extension", 50, 5),
	SIZE(4, "Size", 25, 10),
	LAST_MODIFIED(5, "Last Modified", 75, 15);
	
	private final int index;
	private final String label;
	private final int width;
	private final int widthPercent;
	
	private FileCollectionEditorColumn(int index, String label, int width, int widthPercent) {
		this.index = index;
		this.label = label;
		this.width = width;
		this.widthPercent = widthPercent;
	}
	
	/**
	 * @return index
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * @return the widthPercent
	 */
	public int getWidthPercent() {
		return widthPercent;
	}

	/**
	 * Returns a column by index
	 * @param index
	 * @return
	 */
	public static FileCollectionEditorColumn getByIndex(int index) {
		for (FileCollectionEditorColumn column : FileCollectionEditorColumn.values()) {
			if (column.getIndex() == index) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("No column with index: " + index);
	}
	
	/**
	 * Returns a column by name
	 * @param name
	 * @return
	 */
	public static FileCollectionEditorColumn getByName(String name) {
		for (FileCollectionEditorColumn column : FileCollectionEditorColumn.values()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("No column with name: " + name);
	}
}
