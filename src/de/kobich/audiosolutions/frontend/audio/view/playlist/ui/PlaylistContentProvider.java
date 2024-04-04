package de.kobich.audiosolutions.frontend.audio.view.playlist.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.frontend.audio.view.playlist.PlaylistModel;

public class PlaylistContentProvider implements IStructuredContentProvider {

	public PlaylistContentProvider() {}

	@Override
	public Object[] getElements(Object input) {
		if (input instanceof PlaylistModel model) {
			return model.getPlaylists().toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + PlaylistModel.class.getName() + ">");
	}

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

}
