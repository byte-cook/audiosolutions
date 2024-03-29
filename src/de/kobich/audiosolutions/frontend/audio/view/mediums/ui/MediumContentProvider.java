package de.kobich.audiosolutions.frontend.audio.view.mediums.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.frontend.audio.view.mediums.MediumModel;

public class MediumContentProvider implements IStructuredContentProvider {
	private MediumModel mediumModel;

	public MediumContentProvider() {}

	@Override
	public Object[] getElements(Object input) {
		if (input instanceof MediumModel) {
			mediumModel = (MediumModel) input;
			return mediumModel.getMediums().toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + MediumModel.class.getName() + ">");
	}

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

}
