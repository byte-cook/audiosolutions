package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

import org.eclipse.jface.viewers.ColumnWeightData;

import de.kobich.commons.ui.jface.tree.TreeColumnData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AudioCollectionEditorColumn {
	// audio
	FILE_NAME(0, "File Name", 150, 15, 3, ColumnWeightData.MINIMUM_WIDTH, true),
	TRACK(1, "Track", 150, 15, 3, ColumnWeightData.MINIMUM_WIDTH, true),
	TRACK_NO(2, "No", 30, 5, 1, 30, true),
	TRACK_FORMAT(3, "Format", 30, 5, 1, 30, true),
	ARTIST(4, "Artist", 100, 15, 2, ColumnWeightData.MINIMUM_WIDTH, true),
	ALBUM(5, "Album", 100, 10, 2, ColumnWeightData.MINIMUM_WIDTH, true),
	ALBUM_PUBLICATION(6, "Publication", 60, 10, 1, 40, true),
	DISK(7, "Disk", 50, 5, 1, 30, true),
	GENRE(8, "Genre", 60, 10, 2, ColumnWeightData.MINIMUM_WIDTH, true),
	MEDIUM(9, "Medium", 60, 10, 1, 30, true),
	// file
	EXISTS(1, "Exists", 40, 5, 1, 30, false),
	RELATIVE_PATH(2, "Relative Path", 200, 35, 4, ColumnWeightData.MINIMUM_WIDTH, false),
	EXTENSION(3, "Extension", 50, 5, 1, 30, false),
	SIZE(4, "Size", 25, 10, 1, 30, false),
	LAST_MODIFIED(5, "Last Modified", 75, 15, 1, 40, false);
	;
	
	private final int index;
	@Getter
	private final String label;
	private final int width;
	private final int widthPercent;

	private final int widthShare;
	private final int minimumWidth;
	private final boolean visible;
	
	@Deprecated
	public int getIndex() {
		return index;
	}
	@Deprecated
	public int getWidth() {
		return width;
	}
	@Deprecated
	public int getWidthPercent() {
		return widthPercent;
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
	
	public TreeColumnData createTreeColumnData() {
		return TreeColumnData.builder()
				.element(this)
				.text(this.getLabel())
				.visible(true)
				.hideable(!AudioCollectionEditorColumn.FILE_NAME.equals(this))
				.visible(this.visible)
				.widthShare(this.widthShare)
				.minimumWidth(this.minimumWidth)
				.build();
	}
}
