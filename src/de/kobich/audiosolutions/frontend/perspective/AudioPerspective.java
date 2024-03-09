package de.kobich.audiosolutions.frontend.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.kobich.audiosolutions.frontend.audio.view.id3.ID3TagView;
import de.kobich.audiosolutions.frontend.audio.view.search.AudioSearchView;
import de.kobich.audiosolutions.frontend.file.view.rename.RenameFilesView;

public class AudioPerspective implements IPerspectiveFactory {
	public static final String ID = "de.kobich.audiosolutions.perspective.audio";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		layout.addView(ID3TagView.ID, IPageLayout.LEFT, 0.33f, editorArea);
		layout.addView(RenameFilesView.ID, IPageLayout.BOTTOM, 0.5f, editorArea);
		
		layout.addPerspectiveShortcut(AudioPerspective.ID);
		layout.addShowViewShortcut(AudioSearchView.ID);
		layout.addShowViewShortcut(ID3TagView.ID);
	}
}
