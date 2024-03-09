package de.kobich.audiosolutions.frontend.audio.view.mediums.ui;

public enum MediumColumnType {
	MEDIUM(0, "Medium", 40),
	BORROWER(1, "Borrower", 40),
	DATE(2, "Date", 20);
	
	private final int index;
	private final String label;
	private final int widthPercent;
	
	private MediumColumnType(int index, String label, int widthPercent) {
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
	public static MediumColumnType getByIndex(int index) {
		for (MediumColumnType column : MediumColumnType.values()) {
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
	public static MediumColumnType getByName(String name) {
		for (MediumColumnType column : MediumColumnType.values()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("No column with name: " + name);
	}
}
