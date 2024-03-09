package de.kobich.audiosolutions.frontend.audio.view.play.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.core.service.play.AudioPlayList;

public class AudioPlayContentProvider implements IStructuredContentProvider {
	private AudioPlayList mediumModel;

	public AudioPlayContentProvider() {}

	@Override
	public Object[] getElements(Object input) {
		if (input instanceof AudioPlayList) {
			mediumModel = (AudioPlayList) input;
			return mediumModel.getFiles().toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + AudioPlayList.class.getName() + ">");
	}

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

}
