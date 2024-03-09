package de.kobich.audiosolutions.frontend.audio.view.id3.ui;

import org.eclipse.swt.SWT;

public enum ID3TagColumnType {
	TAG(0, "ID3 Tag", 30, SWT.LEFT),
	VALUE(1, "Value", 70, SWT.LEFT);
	
	private final int index;
	private final String label;
	private final int widthPercent;
	private final int alignment;
	
	private ID3TagColumnType(int index, String label, int widthPercent, int alignment) {
		this.index = index;
		this.label = label;
		this.widthPercent = widthPercent;
		this.alignment = alignment;
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
	 * @return the alignment
	 */
	public int getAlignment() {
		return alignment;
	}

	/**
	 * Returns a column by index
	 * @param index
	 * @return
	 */
	public static ID3TagColumnType getByIndex(int index) {
		for (ID3TagColumnType column : ID3TagColumnType.values()) {
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
	public static ID3TagColumnType getByName(String name) {
		for (ID3TagColumnType column : ID3TagColumnType.values()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("No column with name: " + name);
	}
}
