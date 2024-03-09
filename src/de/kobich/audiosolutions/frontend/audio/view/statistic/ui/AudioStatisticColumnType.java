package de.kobich.audiosolutions.frontend.audio.view.statistic.ui;

import org.eclipse.swt.SWT;

public enum AudioStatisticColumnType {
	TYPE(0, "Type", 40, SWT.LEFT),
	COUNT(1, "Count", 60, SWT.RIGHT);
	
	private final int index;
	private final String label;
	private final int widthPercent;
	private final int alignment;
	
	private AudioStatisticColumnType(int index, String label, int widthPercent, int alignment) {
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
	public static AudioStatisticColumnType getByIndex(int index) {
		for (AudioStatisticColumnType column : AudioStatisticColumnType.values()) {
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
	public static AudioStatisticColumnType getByName(String name) {
		for (AudioStatisticColumnType column : AudioStatisticColumnType.values()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("No column with name: " + name);
	}
}
