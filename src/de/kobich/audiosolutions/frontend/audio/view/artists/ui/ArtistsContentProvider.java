package de.kobich.audiosolutions.frontend.audio.view.artists.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.frontend.audio.view.artists.ArtistsModel;

public class ArtistsContentProvider implements IStructuredContentProvider {

	public ArtistsContentProvider() {}

	@Override
	public Object[] getElements(Object input) {
		if (input instanceof ArtistsModel) {
			ArtistsModel model = (ArtistsModel) input;
			return model.getArtists().toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + ArtistsModel.class.getName() + ">");
	}

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

}
