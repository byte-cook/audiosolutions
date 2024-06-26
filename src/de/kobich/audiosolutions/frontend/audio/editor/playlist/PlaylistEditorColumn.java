package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;

import de.kobich.commons.ui.jface.tree.TreeColumnData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlaylistEditorColumn {
	NAME("Name", 3, ColumnWeightData.MINIMUM_WIDTH, SWT.LEFT),
	FILE_COUNT("File Count", 1, 50, SWT.RIGHT),
	FILE_EXISTS("Exists", 1, 30, SWT.LEFT),
	FOLDER("Folder", 6, ColumnWeightData.MINIMUM_WIDTH, SWT.LEFT),
	FILE_PATH("File Path", 9, ColumnWeightData.MINIMUM_WIDTH, SWT.LEFT);
	
	@Getter
	private final String label;
	private final int widthShare;
	private final int minimumWidth;
	@Getter
	private final int style;
	
	public EditingSupport createEditingSupport(PlaylistEditor editor, TreeViewer viewer) {
		return new PlaylistEditingSupport(editor, viewer, this);
	}
	
	public CellLabelProvider createCellLabelProvider() {
		return new PlaylistEditorLabelProvider(this);
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
