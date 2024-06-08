package de.kobich.audiosolutions.frontend.common.listener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import de.kobich.audiosolutions.frontend.common.selection.SelectionManager;

public class EventListenerAdapter implements IPartListener2, ISelectionListener, IUIEventListener {
	public static enum ListenerType {
		COLLECTION_EDITOR_SELECTION, PART, UI_EVENT
	}
	private final Set<ListenerType> eventTypes;
	private final IWorkbenchPage page;
	
	public EventListenerAdapter(IViewPart viewPart, ListenerType... eventTypes) {
		this.page = viewPart.getViewSite().getPage();
		this.eventTypes = new HashSet<>(Arrays.asList(eventTypes));
	}
	
	public EventListenerAdapter(IEditorPart editorPart, ListenerType... eventTypes) {
		this.page = editorPart.getEditorSite().getPage();
		this.eventTypes = new HashSet<>(Arrays.asList(eventTypes));
	}
	
	/**
	 * Registers this listener
	 * @see https://eclipse.org/articles/Article-WorkbenchSelections/article.html
	 */
	public void register() {
		for (ListenerType type : this.eventTypes) {
			switch (type) {
			case COLLECTION_EDITOR_SELECTION:
				// Listeners registered in this way are notified when the selection of the particular part is changed.  
				// This works even if there is currently no part with such a id. As soon as the part is created its initial selection will be propagated to the listeners registered for it. 
				// When the part is disposed, the listener is passed a null selection if the listener implements INullSelectionListener. 
				// -> all collection editors will fire a selectionChanged event (via mouse click and programmatically)
				// -> required to get an event if editor is not active (e.g. file renamed in FileRenameView)
//				page.addPostSelectionListener(AudioCollectionEditor.ID, this);
//				page.addPostSelectionListener(FileCollectionEditor.ID, this);
				
				SelectionManager.INSTANCE.addPostSelectionListener(this);
				break;
			case PART:
				page.getWorkbenchWindow().getPartService().addPartListener(this);
				break;
			case UI_EVENT:
				EventSupport.INSTANCE.addListener(this);
				break;
			}
		}
	}

	/**
	 * Deregisters this listener
	 */
	public void deregister() {
		for (ListenerType type : this.eventTypes) {
			switch (type) {
			case COLLECTION_EDITOR_SELECTION:
//				page.removePostSelectionListener(AudioCollectionEditor.ID, this);
//				page.removePostSelectionListener(FileCollectionEditor.ID, this);
				
				SelectionManager.INSTANCE.removePostSelectionListener(this);
				break;
			case PART:
				page.getWorkbenchWindow().getPartService().removePartListener(this);
				break;
			case UI_EVENT:
				EventSupport support = EventSupport.INSTANCE;
				support.removeListener(this);
				break;
			}
		}
	}
	
	/**
	 * Fires selection changed event of active editor
	 * @param partReference
	 */
	protected void fireSelectionChangedOfActiveEditor(IWorkbenchPartReference partReference) {
		IEditorPart editorPart = partReference.getPage().getActiveEditor();
		if (editorPart != null && editorPart.getEditorSite() != null && editorPart.getEditorSite().getSelectionProvider() != null) {
			ISelection selection = editorPart.getEditorSite().getSelectionProvider().getSelection();
			selectionChanged(editorPart, selection);
		}
	}
	
	/**
	 * Indicates if the workbenchPart is active
	 * @param workbenchPart
	 * @return
	 */
//	protected boolean isActive(IWorkbenchPart workbenchPart) {
//		boolean isActiveEditor = page.getActiveEditor() != null && page.getActiveEditor().equals(workbenchPart);
//		boolean isActivePart = page.getActivePart() != null && page.getActivePart().equals(workbenchPart); 
//		return isActiveEditor || isActivePart;
//	}

	@Override
	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) {}

	@Override
	public void eventFired(UIEvent event) {}

	@Override
	public void partActivated(IWorkbenchPartReference partReference) {}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partReference) {}

	@Override
	public void partClosed(IWorkbenchPartReference partReference) {}

	@Override
	public void partDeactivated(IWorkbenchPartReference partReference) {}

	@Override
	public void partHidden(IWorkbenchPartReference partReference) {}

	@Override
	public void partInputChanged(IWorkbenchPartReference partReference) {}

	@Override
	public void partOpened(IWorkbenchPartReference partReference) {}

	@Override
	public void partVisible(IWorkbenchPartReference partReference) {}

}