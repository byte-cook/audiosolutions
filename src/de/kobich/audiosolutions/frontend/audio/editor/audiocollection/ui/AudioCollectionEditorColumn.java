package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.commons.ui.jface.tree.TreeColumnData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AudioCollectionEditorColumn {
	// audio
	FILE_NAME("File Name", AudioCollectionEditorColumnType.AUDIO, 3, ColumnWeightData.MINIMUM_WIDTH, true, SWT.LEFT),
	TRACK("Track", AudioCollectionEditorColumnType.AUDIO, 3, ColumnWeightData.MINIMUM_WIDTH, true, SWT.LEFT),
	TRACK_NO("No", AudioCollectionEditorColumnType.AUDIO, 1, 30, true, SWT.RIGHT),
	TRACK_FORMAT("Format", AudioCollectionEditorColumnType.AUDIO, 1, 30, true, SWT.LEFT),
	ARTIST("Artist", AudioCollectionEditorColumnType.AUDIO, 2, ColumnWeightData.MINIMUM_WIDTH, true, SWT.LEFT),
	ALBUM("Album", AudioCollectionEditorColumnType.AUDIO, 2, ColumnWeightData.MINIMUM_WIDTH, true, SWT.LEFT),
	ALBUM_PUBLICATION("Publication", AudioCollectionEditorColumnType.AUDIO, 1, 40, true, SWT.LEFT),
	DISK("Disk", AudioCollectionEditorColumnType.AUDIO, 1, 30, true, SWT.LEFT),
	GENRE("Genre", AudioCollectionEditorColumnType.AUDIO, 2, ColumnWeightData.MINIMUM_WIDTH, true, SWT.LEFT),
	MEDIUM("Medium", AudioCollectionEditorColumnType.AUDIO, 1, 30, true, SWT.LEFT),
	// file
	EXISTS("Exists", AudioCollectionEditorColumnType.FILE, 1, 30, false, SWT.LEFT),
	RELATIVE_PATH("Relative Path", AudioCollectionEditorColumnType.FILE, 4, ColumnWeightData.MINIMUM_WIDTH, false, SWT.LEFT),
	EXTENSION("Extension", AudioCollectionEditorColumnType.FILE, 1, 30, false, SWT.LEFT),
	SIZE("Size", AudioCollectionEditorColumnType.FILE, 1, 30, false, SWT.RIGHT),
	LAST_MODIFIED("Last Modified", AudioCollectionEditorColumnType.FILE, 1, 50, false, SWT.LEFT);
	;
	
	public enum AudioCollectionEditorColumnType { 
		AUDIO, FILE;
		
		public boolean isAudio() {
			return AUDIO.equals(this);
		}
		
		public boolean isFile() {
			return FILE.equals(this);
		}
	}
	
	@Getter
	private final String label;
	@Getter
	private final AudioCollectionEditorColumnType type;

	private final int widthShare;
	private final int minimumWidth;
	private final boolean visible;
	@Getter
	private final int style;
	
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
