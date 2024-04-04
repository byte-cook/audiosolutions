package de.kobich.audiosolutions.frontend.audio.view.playlist.ui;

import lombok.Getter;

@Getter
public enum PlaylistColumnType {
	NAME(0, "Name", 100);
	
	private final int index;
	private final String label;
	private final int widthPercent;
	
	private PlaylistColumnType(int index, String label, int widthPercent) {
		this.index = index;
		this.label = label;
		this.widthPercent = widthPercent;
	}
	
	/**
	 * Returns a column by index
	 * @param index
	 * @return
	 */
	public static PlaylistColumnType getByIndex(int index) {
		for (PlaylistColumnType column : PlaylistColumnType.values()) {
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
	public static PlaylistColumnType getByName(String name) {
		for (PlaylistColumnType column : PlaylistColumnType.values()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("No column with name: " + name);
	}
}
