package de.kobich.audiosolutions.frontend.audio.view.artists.ui;

public enum ArtistsColumnType {
	NAME(0, "Name", 100);
	
	private final int index;
	private final String label;
	private final int widthPercent;
	
	private ArtistsColumnType(int index, String label, int widthPercent) {
		this.index = index;
		this.label = label;
		this.widthPercent = widthPercent;
	}
	
	/**
	 * @return index
	 */
	public final int getIndex() {
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
	public int getWidthPercent() {
		return widthPercent;
	}
	
	/**
	 * Returns a column by index
	 * @param index
	 * @return
	 */
	public static ArtistsColumnType getByIndex(int index) {
		for (ArtistsColumnType column : ArtistsColumnType.values()) {
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
	public static ArtistsColumnType getByName(String name) {
		for (ArtistsColumnType column : ArtistsColumnType.values()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("No column with name: " + name);
	}
}
