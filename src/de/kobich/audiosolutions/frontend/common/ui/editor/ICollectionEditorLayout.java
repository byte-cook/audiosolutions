package de.kobich.audiosolutions.frontend.common.ui.editor;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;


public interface ICollectionEditorLayout {
	void createLayout(Composite parent, FormToolkit toolkit, ViewerFilter filter);
	
	void setInput(ICollectionEditorModel model);
	
	void updateLayout(LayoutDelta layoutDelta, FileDescriptorSelection oldSelection, boolean active);
	
	void refresh();
	
	void setFocus();
	
	<T> T getViewerAdapter(Class<T> clazz);
	
	Composite getComposite();
	
	/**
	 * Returns a correct selection for this layout
	 * @param selection
	 * @return
	 */
	ISelection createSelection(FileDescriptorSelection selection);
	
	void dispose();
}
