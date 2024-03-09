package de.kobich.audiosolutions.frontend.audio.view.play.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.ISourceProviderService;

import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;
import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayViewSourceProvider;
import de.kobich.commons.ui.jface.table.ViewerColumn;
import de.kobich.component.file.FileDescriptor;

public class AudioPlayLabelProvider implements ITableLabelProvider, ITableColorProvider {
	private final AudioPlayView view;
	private final AudioPlayViewSourceProvider provider;
	
	public AudioPlayLabelProvider(AudioPlayView view) {
		this.view = view;
		ISourceProviderService sourceProviderService = (ISourceProviderService) view.getSite().getService(ISourceProviderService.class);
		this.provider = (AudioPlayViewSourceProvider) sourceProviderService.getSourceProvider(AudioPlayViewSourceProvider.PLAYING_STATE);
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof FileDescriptor) {
			FileDescriptor item = (FileDescriptor) element;
			ViewerColumn column = AudioPlayView.COLUMNS.getByIndex(columnIndex);
			if (AudioPlayView.COLUMN_TRACK.equals(column)) {
				return item.getFileName();
			}
			else if (AudioPlayView.COLUMN_FILE.equals(column)) {
				return item.getFile().getAbsolutePath();
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
		if (element instanceof FileDescriptor) {
			FileDescriptor item = (FileDescriptor) element;
			FileDescriptor currentFile = view.getPlayList().getCurrentFile().orElse(null);
			boolean playing = (Boolean) provider.getCurrentState().get(AudioPlayViewSourceProvider.PLAYING_STATE);
			if (playing && currentFile != null && currentFile.equals(item)) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND); 
			}
		}
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

}
