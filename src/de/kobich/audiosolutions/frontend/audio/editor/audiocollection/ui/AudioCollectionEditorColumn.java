package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.commons.ui.jface.tree.TreeColumnData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AudioCollectionEditorColumn {
	// audio
	FILE_NAME("File Name", 3, ColumnWeightData.MINIMUM_WIDTH, true),
	TRACK("Track", 3, ColumnWeightData.MINIMUM_WIDTH, true),
	TRACK_NO("No", 1, 30, true),
	TRACK_FORMAT("Format", 1, 30, true),
	ARTIST("Artist", 2, ColumnWeightData.MINIMUM_WIDTH, true),
	ALBUM("Album", 2, ColumnWeightData.MINIMUM_WIDTH, true),
	ALBUM_PUBLICATION("Publication", 1, 40, true),
	DISK("Disk", 1, 30, true),
	GENRE("Genre", 2, ColumnWeightData.MINIMUM_WIDTH, true),
	MEDIUM("Medium", 1, 30, true),
	// file
	EXISTS("Exists", 1, 30, false),
	RELATIVE_PATH("Relative Path", 4, ColumnWeightData.MINIMUM_WIDTH, false),
	EXTENSION("Extension", 1, 30, false),
	SIZE("Size", 1, 30, false),
	LAST_MODIFIED("Last Modified", 1, 50, false);
	;
	
	@Getter
	private final String label;

	private final int widthShare;
	private final int minimumWidth;
	private final boolean visible;
	
	public EditingSupport createEditingSupport(AudioCollectionEditor editor, TreeViewer viewer) {
		return new AudioCollectionEditingSupport(editor, viewer, this);
	}
	
	public CellLabelProvider createCellLabelProvider() {
		return new AudioCollectionEditorLabelProvider(this);
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
