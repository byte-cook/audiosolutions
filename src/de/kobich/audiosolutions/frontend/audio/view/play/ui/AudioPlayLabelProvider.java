package de.kobich.audiosolutions.frontend.audio.view.play.ui;

import java.io.File;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;
import de.kobich.commons.ui.jface.table.ViewerColumn;

public class AudioPlayLabelProvider implements ITableLabelProvider, ITableColorProvider {
	private final AudioPlayView view;
	
	public AudioPlayLabelProvider(AudioPlayView view) {
		this.view = view;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof EditablePlaylistFile file) {
			ViewerColumn column = AudioPlayView.COLUMNS.getByIndex(columnIndex);
			if (AudioPlayView.COLUMN_TRACK.equals(column)) {
				return file.getFileName();
			}
			else if (AudioPlayView.COLUMN_FILE.equals(column)) {
				return file.getFile().getAbsolutePath();
			}
		}
		throw new IllegalStateException("Illegal column index < " + columnIndex + ">, expected<0 - 1>");
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		if (element instanceof EditablePlaylistFile file) {
			File currentFile = view.getPlaylist().getCurrentFile().orElse(null);
			if (view.getProvider().isPlaying() && currentFile != null && currentFile.equals(file.getFile())) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND); 
			}
			else if (!file.getFile().exists()) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			}
		}
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

}
