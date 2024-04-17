package de.kobich.audiosolutions.frontend.audio.editor.search.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenMenuHyperlinkAdapter extends HyperlinkAdapter {
	private final Decorations parent;
	private final IWorkbenchWindow window;
	private final AudioSearchQuery query;

	@Override
	public void linkActivated(HyperlinkEvent e) {
		Menu menu = new Menu(parent, SWT.POP_UP);
		
		// play
		MenuItem playItem = new MenuItem(menu, SWT.NONE);
		playItem.setText("Play");
		playItem.setImage(Activator.getDefault().getImage(ImageKey.AUDIO_PLAY));
		playItem.addSelectionListener(new PlaySelectionAdapter(window, query));
		
		// TODO: play as next
		MenuItem playAsNextItem = new MenuItem(menu, SWT.NONE);
		playAsNextItem.setText("Play As Next");
		playAsNextItem.setImage(Activator.getDefault().getImage(ImageKey.AUDIO_PLAY));
		playAsNextItem.addSelectionListener(new AppendToPlayerSelectionAdapter(window, query, true));
		
		// append to player
		MenuItem appendToPlayerItem = new MenuItem(menu, SWT.NONE);
		appendToPlayerItem.setText("Append To Player");
		appendToPlayerItem.setImage(Activator.getDefault().getImage(ImageKey.AUDIO_PLAY_ADD));
		appendToPlayerItem.addSelectionListener(new AppendToPlayerSelectionAdapter(window, query, false));
		
		// copy files to another playlist
		MenuItem addToPlaylistItem = new MenuItem(menu, SWT.NONE);
		addToPlaylistItem.setText("Copy Files To Another Playlist");
		addToPlaylistItem.setImage(Activator.getDefault().getImage(ImageKey.PLAYLIST));
		addToPlaylistItem.addSelectionListener(new CopyFilesToAnotherPlaylistSelectionAdapter(window, query));
		
		menu.setVisible(true);
	}

}
