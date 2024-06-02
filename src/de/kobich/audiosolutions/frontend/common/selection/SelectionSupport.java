package de.kobich.audiosolutions.frontend.common.selection;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.editor.search.AudioSearchEditor;
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
 * 
 */
public class SelectionSupport implements IPostSelectionProvider, IPartListener2 {
	private static final Logger logger = Logger.getLogger(SelectionSupport.class);
	public static final SelectionSupport INSTANCE = new SelectionSupport();
	private final Map<IEditorPart, PartSelectionDelegator> editorDelegator;
	private final Map<IEditorPart, PartSelectionDelegator> editorPostDelegator;
	private final ListenerList<ISelectionChangedListener> postSelectionChangedListeners;
	private final Map<IEditorPart, Long> editorAccessTime;
	private IEditorPart activeEditor;
	private ISelection selection;
	
	private SelectionSupport() {
		this.editorDelegator = new HashMap<>();
		this.editorPostDelegator = new HashMap<>();
		this.postSelectionChangedListeners = new ListenerList<>();
		this.editorAccessTime = new HashMap<>();
	}
	
	/**
	 * Registers the given editor to this service. Each editor gets its own {@link PartSelectionDelegator} in order to know who fired the event. 
	 * @param editor
	 * @param selectionProvider
	 */
	public void registerEditor(IEditorPart editor, IPostSelectionProvider selectionProvider) {
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
	public void deregisterEditor(IEditorPart editor, ISelectionProvider selectionProvider) {
		if (editorDelegator.containsKey(editor)) {
			PartSelectionDelegator delegator = editorDelegator.get(editor);
			selectionProvider.removeSelectionChangedListener(delegator);
			editorDelegator.remove(editor);
		}
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

	@Override
	public ISelection getSelection() {
		return selection;
	}

	@Override
	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
	}

	@Override
	public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
		this.postSelectionChangedListeners.addListener(listener);
	}

	@Override
	public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
		this.postSelectionChangedListeners.removeListener(listener);
	}

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
			firePostSelectionChanged(new SelectionChangedEvent(this, new StructuredSelection()));
			return;
		}
		
		// set active editor and activation time
		this.activeEditor = editor;
		this.editorAccessTime.put(activeEditor, System.currentTimeMillis());
		logger.info("Active editor: " + activeEditor.getTitle());
		
		// send last selection event of the new active editor
		if (this.editorPostDelegator.containsKey(this.activeEditor)) {
			PartSelectionDelegator postDelegator = this.editorPostDelegator.get(this.activeEditor);
			if (postDelegator.lastEvent != null) {
				firePostSelectionChanged(postDelegator.lastEvent);
			}
			else {
				firePostSelectionChanged(new SelectionChangedEvent(this, StructuredSelection.EMPTY));
			}
		}
	}
	
	/**
	 * Removes the given editor and reset the active one if necessary
	 * @param editor
	 */
	private void resetActiveEditor(IEditorPart editor) {
		this.editorAccessTime.remove(editor);
		if (this.activeEditor == null || !this.activeEditor.equals(editor)) {
			return;
		}
		
		// set previous active editor
		IEditorPart tmpActiveEditor = null;
		long max = 0;
		for (IEditorPart e : editorAccessTime.keySet()) {
			long accessTime = editorAccessTime.get(e);
			if (accessTime > max) {
				tmpActiveEditor = e;
				max = accessTime;
			}
		}
		setActiveEditor(tmpActiveEditor);
	}
	
	/**
	 * Informs about selection changes
	 * @param event
	 */
	private void firePostSelectionChanged(SelectionChangedEvent event) {
		// update selection
		setSelection(event.getSelection());
		
		// inform listeners
		for (ISelectionChangedListener l : postSelectionChangedListeners) {
			l.selectionChanged(event);
		}
	}
	
	/**
	 * Helper class to assign a selection event to one unique editor.
	 */
	@RequiredArgsConstructor
	private static class PartSelectionDelegator implements ISelectionChangedListener {
		private final IEditorPart editor;
		private final SelectionSupport selectionManager;
		private SelectionChangedEvent lastEvent;
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			this.lastEvent = event;
			if (selectionManager.activeEditor != null && selectionManager.activeEditor.equals(this.editor)) {
				selectionManager.firePostSelectionChanged(event);
			}
		}
	}
	

}
