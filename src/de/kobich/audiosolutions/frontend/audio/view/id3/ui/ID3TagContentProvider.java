package de.kobich.audiosolutions.frontend.audio.view.id3.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.frontend.audio.view.id3.model.ID3TagModel;

public class ID3TagContentProvider implements IStructuredContentProvider {
	public ID3TagContentProvider() {}

	@Override
	public Object[] getElements(Object input) {
		if (input instanceof ID3TagModel) {
			ID3TagModel audioDataModel = (ID3TagModel) input;
			return audioDataModel.getID3TagItems().toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + ID3TagModel.class.getName() + ">");
	}

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

}
