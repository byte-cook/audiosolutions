package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AlbumTreeNode;
import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta.AddItem;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta.ReplaceItem;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;
import de.kobich.component.file.FileDescriptor;

/**
 * Abstract layout providing some helper methods.
 */
@Deprecated
public abstract class AbstractCollectionEditorLayout implements ICollectionEditorLayout {
	
	/**
	 * Returns a selection for this layout (tries to select as less nodes as possible)
	 * @param selection
	 * @param contentProvider
	 * @param layoutType
	 * @return
	 */
	protected ISelection createSelection(FileDescriptorSelection selection, ITreeContentProvider contentProvider, LayoutType layoutType) {
		List<AbstractTableTreeNode<?, ?>> nodes = new ArrayList<>();
		Set<FileDescriptor> filesInNodes = new HashSet<>();
		
		for (FileDescriptor fileDescriptor : selection.getFileDescriptors()) {
			// check if nodes already contains file descriptor (e.g. RelativePathTreeNode's children files) 
			if (filesInNodes.contains(fileDescriptor)) {
				continue;
			}
			
			// create file node
			FileDescriptorTreeNode node = new FileDescriptorTreeNode(fileDescriptor);
			
			// get parent node
			AbstractTableTreeNode<?, ?> parentNode = null;
			Object parent = contentProvider.getParent(node);
			if (parent instanceof RelativePathTreeNode) {
				parentNode = (RelativePathTreeNode) contentProvider.getParent(node);
			}
			else if (parent instanceof AlbumTreeNode) {
				parentNode = (AlbumTreeNode) contentProvider.getParent(node);
			}
			
			// if all children of parent node should be selected 
			if (parentNode != null && selection.getFileDescriptors().containsAll(parentNode.getFileDescriptors())) {
				nodes.add(parentNode);
				filesInNodes.addAll(parentNode.getFileDescriptors());
			}
			else {
				nodes.add(node);
				filesInNodes.add(node.getContent());
			}
		}
		return new StructuredSelection(nodes);
	}

	/**
	 * Update table layout by using layout delta
	 * @param viewer
	 * @param layoutDelta
	 * @param layoutType
	 * @param activeLayout
	 */
	protected void updateTableLayout(TableViewer viewer, LayoutDelta layoutDelta, LayoutType layoutType, FileDescriptorSelection oldSelection, boolean activeLayout) {
		viewer.getTable().setRedraw(false);
		// add
		if (layoutDelta.getAddItems().containsKey(layoutType)) {
			for (AddItem item : layoutDelta.getAddItems().get(layoutType)) {
				viewer.add(item.element);
			}
		}
		// selection
		if (activeLayout) {
			// do selection before remove because remove fires a SelectionChanged event if a selected item is affected
			setSelection(viewer, layoutDelta, layoutType, oldSelection);
		}
		// update
		if (layoutDelta.getUpdateItems().containsKey(layoutType)) {
			for (Object element : layoutDelta.getUpdateItems().get(layoutType)) {
				viewer.update(element, null);
			}
		}
		// refresh
		if (layoutDelta.getRefreshItems().containsKey(layoutType)) {
			for (Object element : layoutDelta.getRefreshItems().get(layoutType)) {
				viewer.refresh(element);
			}
		}
		// remove
		if (layoutDelta.getRemoveItems().containsKey(layoutType)) {
			for (Object element : layoutDelta.getRemoveItems().get(layoutType)) {
				viewer.remove(element);
			}
		}
		viewer.getTable().setRedraw(true);
	}

	/**
	 * Update tree layout by using layout delta
	 * @param viewer
	 * @param layoutDelta
	 * @param layoutType
	 * @param activeLayout
	 */
	protected void updateTreeLayout(TreeViewer viewer, LayoutDelta layoutDelta, LayoutType layoutType, FileDescriptorSelection oldSelection, boolean activeLayout) {
		viewer.getTree().setRedraw(false);
		// add
		if (layoutDelta.getAddItems().containsKey(layoutType)) {
			for (AddItem item : layoutDelta.getAddItems().get(layoutType)) {
				viewer.update(item.parent, new String[] { CollectionEditorViewerFilter.FILTER_PROP });
				viewer.add(item.parent, item.element);
			}
		}
		// selection
		if (activeLayout) {
			// do selection before remove because remove fires a SelectionChanged event if a selected item is affected
			setSelection(viewer, layoutDelta, layoutType, oldSelection);
		}
		// update
		if (layoutDelta.getUpdateItems().containsKey(layoutType)) {
			for (Object element : layoutDelta.getUpdateItems().get(layoutType)) {
				viewer.update(element, null);
			}
		}
		// refresh
		if (layoutDelta.getRefreshItems().containsKey(layoutType)) {
			for (Object element : layoutDelta.getRefreshItems().get(layoutType)) {
				viewer.refresh(element);
			}
		}
		// remove
		if (layoutDelta.getRemoveItems().containsKey(layoutType)) {
			for (Object element : layoutDelta.getRemoveItems().get(layoutType)) {
				viewer.remove(element);
			}
		}
		viewer.getTree().setRedraw(true);

	}

	private void setSelection(Viewer viewer, LayoutDelta layoutDelta, LayoutType layoutType, FileDescriptorSelection oldSelection) {
		List<Object> newSelectionElements = new ArrayList<Object>();
		
		// add old selection
		newSelectionElements.addAll(oldSelection.getElements());
		// replace
		if (layoutDelta.getReplaceItems().containsKey(layoutType)) {
			for (ReplaceItem item : layoutDelta.getReplaceItems().get(layoutType)) {
				for (Object o : oldSelection.getElements()) {
					if (o instanceof FileDescriptorTreeNode) {
						if (item.oldElement.equals(o)) {
							newSelectionElements.remove(item.oldElement);
							newSelectionElements.add(item.newElement);
						}
					}
				}
			}
		}
		
		ISelection newSel = new StructuredSelection(newSelectionElements);
		FileDescriptorSelection newSelection = new FileDescriptorSelection(newSel, oldSelection.getEditorFilter());
		boolean selectionChanged = !newSelection.getFileDescriptors().equals(oldSelection.getFileDescriptors());
		if (selectionChanged) {
			viewer.setSelection(newSel);
		}
	}
}
