package de.kobich.audiosolutions.frontend.audio.view.play.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.core.service.play.PersistableAudioPlayingList;

public class AudioPlayContentProvider implements IStructuredContentProvider {

	public AudioPlayContentProvider() {}

	@Override
	public Object[] getElements(Object input) {
		if (input instanceof PersistableAudioPlayingList model) {
			return model.getFilesSorted().toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + PersistableAudioPlayingList.class.getName() + ">");
	}

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

}
