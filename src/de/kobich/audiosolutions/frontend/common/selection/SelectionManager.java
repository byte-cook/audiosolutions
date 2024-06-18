package de.kobich.audiosolutions.frontend.common.selection;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SelectionListenerFactory;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.search.AudioSearchEditor;
import de.kobich.audiosolutions.frontend.common.listener.EventListenerAdapter;
import de.kobich.commons.ListenerList;
import lombok.RequiredArgsConstructor;

/**
 * Manages the selection of several collection editors and informs interested listeners about selection changes.
 * <p>
 * A selection changed event will be fired when the selection of the active editor changes. 
 * The active editor is defined as the last editor that was activated; even if this editor is hidden at the moment. 
 * Events of non-active editors are ignored. 
 * <p>
 * If you need to know whether the active editor is hidden, you should implement {@link IPartListener2#partHidden(IWorkbenchPartReference)}.
 * <p>
 * Overview:<br/>
 * Editor --registerEditor--> SelectionManager<br/>
 * Editor --fireEvent--> SelectionManager --fireEvent--> listeners (views).
 * 
 * @see SelectionListenerFactory
 */
public class SelectionManager implements IPartListener2 {
	private static final Logger logger = Logger.getLogger(SelectionManager.class);
	public static final SelectionManager INSTANCE = new SelectionManager();
	private final Map<IEditorPart, PartSelectionDelegator> editorPostDelegator;
	private final ListenerList<ISelectionListener> postSelectionListeners;
	private IEditorPart activeEditor;
	private ISelection selection;
	
	private SelectionManager() {
		this.editorPostDelegator = new HashMap<>();
		this.postSelectionListeners = new ListenerList<>();
	}
	
	/**
	 * Registers the given editor to this service. Each editor gets its own {@link PartSelectionDelegator} in order to know who fired the event. 
	 * @param editor
	 * @param selectionProvider
	 */
	public void registerEditor(IEditorPart editor, IPostSelectionProvider selectionProvider) {
		editor.getSite().setSelectionProvider(selectionProvider);
		
		if (!editorPostDelegator.containsKey(editor)) {
			PartSelectionDelegator postDelegator = new PartSelectionDelegator(editor, this);
			((IPostSelectionProvider) selectionProvider).addPostSelectionChangedListener(postDelegator);
			editorPostDelegator.put(editor, postDelegator);
		}
	}
	
	/**
	 * Deregisters the given editor from this service.
	 * @param editor
	 * @param selectionProvider
	 */
	public void deregisterEditor(IEditorPart editor, IPostSelectionProvider selectionProvider) {
		editor.getSite().setSelectionProvider(null);
		
		if (editorPostDelegator.containsKey(editor) && selectionProvider instanceof IPostSelectionProvider) {
			PartSelectionDelegator postDelegator = editorPostDelegator.get(editor);
			((IPostSelectionProvider) selectionProvider).removePostSelectionChangedListener(postDelegator);
			editorPostDelegator.remove(editor);
		}
	}
	
	/**
	 * Returns the active editor, even if the editor is not visible at the moment (e.g. other parts are maximized)
	 * @return the active editor or null
	 */
	public IEditorPart getActiveEditor() {
		return this.activeEditor;
	}
	
	/**
	 * Returns the active editor, even if the editor is not visible at the moment (e.g. other parts are maximized)
	 * @return the active editor or null
	 */
	public <T> T getActiveEditor(Class<T> clazz) {
		if (this.activeEditor != null && clazz.isAssignableFrom(this.activeEditor.getClass())) {
			return clazz.cast(this.activeEditor);
		}
		return null;
	}

	public ISelection getSelection() {
		return selection;
	}

	private void setSelection(ISelection selection) {
		this.selection = selection;
	}

	public void addPostSelectionListener(ISelectionListener listener) {
		this.postSelectionListeners.addListener(listener);
	}

	public void removePostSelectionListener(ISelectionListener listener) {
		this.postSelectionListeners.removeListener(listener);
	}
	
	/**
	 * @see EventListenerAdapter#fireSelectionChangedOfActiveEditor()
	 */
//	public void fireSelectionChangedOfActiveEditor(ISelectionListener listener) {
//		IEditorPart editor = getActiveEditor();
//		if (editor == null) {
//			return;
//		}
//		
//		PartSelectionDelegator delegator = this.editorPostDelegator.get(editor);
//		if (delegator != null) {
//			listener.selectionChanged(delegator.editor, delegator.lastSelection);
//		}
//	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		switch (partRef.getId()) {
		case AudioCollectionEditor.ID:
		case PlaylistEditor.ID:
		case AudioSearchEditor.ID:
			IEditorPart editor = (IEditorPart) partRef.getPart(false);
			resetActiveEditor(editor);
			break;
		}
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		// called when partRef gets focus
		switch (partRef.getId()) {
		case AudioCollectionEditor.ID:
		case PlaylistEditor.ID:
		case AudioSearchEditor.ID:
			IEditorPart editor = (IEditorPart) partRef.getPart(false);
			setActiveEditor(editor);
			break;
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}
	
	/**
	 * Sets the active editor and resend its last selection event
	 * @param editor
	 */
	private void setActiveEditor(IEditorPart editor) {
		if (this.activeEditor != null && this.activeEditor.equals(editor)) {
			return;
		}
		if (editor == null) {
			this.activeEditor = null;
			firePostSelectionChanged(null, StructuredSelection.EMPTY);
			return;
		}
		
		// set active editor and activation time
		this.activeEditor = editor;
		logger.info("Active editor: " + activeEditor.getTitle());
		
		// send last selection event of the new active editor
		if (this.editorPostDelegator.containsKey(this.activeEditor)) {
			PartSelectionDelegator postDelegator = this.editorPostDelegator.get(this.activeEditor);
			if (postDelegator.lastSelection != null) {
				firePostSelectionChanged(this.activeEditor, postDelegator.lastSelection);
			}
			else {
				firePostSelectionChanged(this.activeEditor, StructuredSelection.EMPTY);
			}
		}
	}
	
	/**
	 * Removes the given editor and reset the active one if necessary
	 * @param editor
	 */
	private void resetActiveEditor(IEditorPart editor) {
		if (this.activeEditor == null || !this.activeEditor.equals(editor)) {
			return;
		}
		
		// set previous active editor
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		setActiveEditor(activeEditor);
	}
	
	/**
	 * Informs about selection changes
	 * @param event
	 */
	private void firePostSelectionChanged(IWorkbenchPart part, ISelection selection) {
		// update selection
		setSelection(selection);
		
		// inform listeners
		for (ISelectionListener l : postSelectionListeners) {
			l.selectionChanged(part, selection);
		}
	}
	
	/**
	 * Helper class to assign a selection event to one unique editor.
	 */
	@RequiredArgsConstructor
	private static class PartSelectionDelegator implements ISelectionChangedListener {
		private final IEditorPart editor;
		private final SelectionManager selectionManager;
		private ISelection lastSelection;
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			this.lastSelection = event.getSelection();
			if (selectionManager.activeEditor != null && selectionManager.activeEditor.equals(this.editor)) {
				selectionManager.firePostSelectionChanged(editor, event.getSelection());
			}
		}
	}
	

}
