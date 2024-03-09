package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

public enum AudioCollectionEditorColumn {
	FILE_NAME(0, "File Name", 150, 15),
	TRACK(1, "Track", 150, 15),
	TRACK_NO(2, "No", 30, 5),
	TRACK_FORMAT(3, "Format", 30, 5),
	ARTIST(4, "Artist", 100, 15),
	ALBUM(5, "Album", 100, 10),
	ALBUM_PUBLICATION(6, "Publication", 60, 10),
	DISK(7, "Disk", 50, 5),
	GENRE(8, "Genre", 60, 10),
	MEDIUM(9, "Medium", 60, 10);
	
	private final int index;
	private final String label;
	private final int width;
	private final int widthPercent;
	
	private AudioCollectionEditorColumn(int index, String label, int width, int widthPercent) {
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
	 * @return the width
	 */
	public int getWidthPercent() {
		return widthPercent;
	}

	/**
	 * Returns a column by index
	 * @param index
	 * @return
	 */
	public static AudioCollectionEditorColumn getByIndex(int index) {
		for (AudioCollectionEditorColumn column : AudioCollectionEditorColumn.values()) {
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
	public static AudioCollectionEditorColumn getByName(String name) {
		for (AudioCollectionEditorColumn column : AudioCollectionEditorColumn.values()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("No column with name: " + name);
	}
}
