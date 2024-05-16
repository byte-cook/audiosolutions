package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.commons.ui.jface.listener.SelectionProviderIntermediate;

@Deprecated
public class CollectionEditorLayoutManager {
	private final ICollectionEditor editor;
	private final FormToolkit toolkit;
	private final CollectionEditorViewerFilter filter;
	private final Map<LayoutType, ICollectionEditorLayout> layouts;
	private final SelectionProviderIntermediate selectionProvider;
	private ICollectionEditorLayout activeLayout;
	
	public CollectionEditorLayoutManager(ICollectionEditor editor, FormToolkit toolkit, CollectionEditorViewerFilter filter) {
		this.editor = editor;
		this.toolkit = toolkit;
		this.filter = filter;
		this.layouts = new HashMap<LayoutType, ICollectionEditorLayout>();
		this.selectionProvider = new SelectionProviderIntermediate();
	}
	
	public void addLayout(LayoutType type, ICollectionEditorLayout layout) {
		this.layouts.put(type, layout);
	}
	
	/**
	 * Creates all layouts
	 * @param parent
	 * @param fileCollection
	 */
	public void makeLayouts(Composite parent, ICollectionEditorModel input) {
		for (ICollectionEditorLayout layout : this.layouts.values()) {
			layout.createLayout(parent, toolkit, filter);
			layout.setInput(input);
			// register context menu
			Control control = layout.getViewerAdapter(Control.class);
			MenuManager menuManager = new MenuManager();
			Menu menuFlat = menuManager.createContextMenu(control);
			control.setMenu(menuFlat);
			editor.getSite().registerContextMenu(menuManager, layout.getViewerAdapter(ISelectionProvider.class));
		}
		// set active layout
		this.activeLayout = this.layouts.get(LayoutType.FLAT);
		selectionProvider.setSelectionProviderDelegate(activeLayout.getViewerAdapter(StructuredViewer.class));
		selectionProvider.addPostSelectionChangedListener(new LogoImagePostSelectionListener(editor, filter));
		
		editor.getSite().setSelectionProvider(selectionProvider);
		SelectionSupport.INSTANCE.registerEditor(editor, selectionProvider);
	}
	
	/**
	 * Returns the active layout
	 * @return
	 */
	public ICollectionEditorLayout getActiveLayout() {
		return this.activeLayout;
	}

	/**
	 * Updates all layouts
	 * @param layoutDelta
	 */
	public void updateLayout(final LayoutDelta layoutDelta, final FileDescriptorSelection oldSelection) {
		for (ICollectionEditorLayout layout : layouts.values()) {
			layout.updateLayout(layoutDelta, oldSelection, getActiveLayout().equals(layout));
		}
	}

	/**
	 * Switches the layout
	 * @return 
	 */
	public Composite switchLayout(LayoutType layoutType, FileCollection input) {
		// save current selection
		FileDescriptorSelection currentSelection = getSelectedFiles();
		
		// set new layout
		activeLayout = this.layouts.get(layoutType);
		StructuredViewer structuredViewer = activeLayout.getViewerAdapter(StructuredViewer.class);
		selectionProvider.setSelectionProviderDelegate(structuredViewer);
		
		// refresh layout
		activeLayout.refresh();
		
		// set selection
		ISelection selection = activeLayout.createSelection(currentSelection);
		structuredViewer.setSelection(selection, true);
		
		return activeLayout.getComposite();
	}

	/**
	 * Refreshes the editor
	 */
	public void refreshEditor() {
		editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (activeLayout != null) {
					StructuredViewer activeViewer = activeLayout.getViewerAdapter(StructuredViewer.class);
					if (!activeViewer.getControl().isDisposed()) {
						// refresh
						activeLayout.refresh();
						// update selection (required for filtering)
						selectionProvider.setSelection(selectionProvider.getSelection());
					}
				}
			}
		});
	}
	
	/**
	 * Returns the selected files
	 * @return
	 */
	public FileDescriptorSelection getSelectedFiles() {
		if (Display.getCurrent() != null) {
			// current thread is UI thread
			ISelection selection = selectionProvider.getSelection();
			return new FileDescriptorSelection(selection, filter);
		}
		else {
			final List<FileDescriptorSelection> selections = new ArrayList<>();
			editor.getSite().getShell().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					ISelection selection = selectionProvider.getSelection();
					selections.add(new FileDescriptorSelection(selection, filter));
				}
			});
			return selections.get(0);
		}
	}

	public void dispose() {
		SelectionSupport.INSTANCE.deregisterEditor(editor, selectionProvider);
		for (ICollectionEditorLayout layout : this.layouts.values()) {
			layout.dispose();
		}
	}
}
