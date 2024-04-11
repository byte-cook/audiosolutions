package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import org.eclipse.jface.viewers.ColumnWeightData;

import de.kobich.commons.ui.jface.tree.TreeColumnData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlaylistEditorColumn {
	NAME("Name", 3, ColumnWeightData.MINIMUM_WIDTH),
	FILE_COUNT("File Count", 1, ColumnWeightData.MINIMUM_WIDTH),
	FILE_EXISTS("Exists", 1, 30),
	FOLDER("Folder", 6, ColumnWeightData.MINIMUM_WIDTH),
	FILE_PATH("File Path", 9, ColumnWeightData.MINIMUM_WIDTH);
	
	@Getter
	private final String label;
	private final int widthShare;
	private final int minimumWidth;
	
	/**
	 * Returns a column by name
	 * @param name
	 * @return
	 */
	public static PlaylistEditorColumn getByName(String name) {
		for (PlaylistEditorColumn column : PlaylistEditorColumn.values()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new IndexOutOfBoundsException("No column with name: " + name);
	}
	
	public TreeColumnData createTreeColumnData() {
		return TreeColumnData.builder()
				.element(this)
				.text(this.getLabel())
				.visible(true)
				.hideable(!PlaylistEditorColumn.NAME.equals(this))
				.widthShare(this.widthShare)
				.minimumWidth(this.minimumWidth)
				.build();
	}
}
