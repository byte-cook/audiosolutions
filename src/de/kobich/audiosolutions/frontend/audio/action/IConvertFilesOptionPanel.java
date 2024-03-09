package de.kobich.audiosolutions.frontend.audio.action;

import org.eclipse.swt.widgets.Composite;

import de.kobich.commons.ui.memento.IMementoItem;

public interface IConvertFilesOptionPanel {

	void createOptionComposite(Composite parent);

	void okPressed();

	void restoreState(IMementoItem mementoItem);

	void saveState(IMementoItem mementoItem);

}