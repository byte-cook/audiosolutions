package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.frontend.file.view.rename.model.FileModel;

/**
 * Content provider for renaming files view.
 */
public class RenamingFilesPreviewContentProvider implements IStructuredContentProvider {
	@Override
	public Object[] getElements(Object input) {
		if (input instanceof FileModel) {
			FileModel model = (FileModel) input;
			return model.getRenameables().toArray();
		}
		throw new IllegalStateException("Illegal input type < " + input.getClass().getName() + ">, expected<" + FileModel.class.getName() + ">");
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

	}

}
