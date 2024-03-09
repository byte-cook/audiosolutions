package de.kobich.audiosolutions.frontend.common.ui.editor;

import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.commons.collections.Dimension.DimensionType;
import de.kobich.commons.collections.DimensionMap2D;

/**
 * Represents the difference between the viewer's model (= input) and the UI. 
 */
public class LayoutDelta {
	private final DimensionMap2D<LayoutType, AddItem> addItems;
	private final DimensionMap2D<LayoutType, AbstractTableTreeNode<?, ?>> updateItems;
	private final DimensionMap2D<LayoutType, AbstractTableTreeNode<?, ?>> removeItems;
	private final DimensionMap2D<LayoutType, AbstractTableTreeNode<?, ?>> refreshItems;
	private final DimensionMap2D<LayoutType, ReplaceItem> replaceItems;

	public LayoutDelta() {
		this.addItems = new DimensionMap2D<LayoutType, AddItem>(DimensionType.SET);
		this.updateItems = new DimensionMap2D<LayoutType, AbstractTableTreeNode<?, ?>>(DimensionType.SET);
		this.removeItems = new DimensionMap2D<LayoutType, AbstractTableTreeNode<?, ?>>(DimensionType.SET);
		this.refreshItems = new DimensionMap2D<LayoutType, AbstractTableTreeNode<?, ?>>(DimensionType.SET);
		this.replaceItems = new DimensionMap2D<LayoutType, ReplaceItem>(DimensionType.SET);
	}

	/**
	 * Adds one node to the viewer as a child of parent
	 * @param parent
	 * @param element
	 * @param layouts
	 */
	public void addItem(Object parent, AbstractTableTreeNode<?, ?> element, LayoutType... layouts) {
		AddItem item = new AddItem();
		item.parent = parent;
		item.element = element;
		for (LayoutType layout : layouts) {
			addItems.addElement(layout, item);
		}
	}

	/**
	 * Updates one element
	 * @param element
	 * @param layouts
	 */
	public void updateItem(AbstractTableTreeNode<?, ?> element, LayoutType... layouts) {
		for (LayoutType layout : layouts) {
			updateItems.addElement(layout, element);
		}
	}

	/**
	 * Removes one element
	 * @param element
	 * @param layouts
	 */
	public void removeItem(AbstractTableTreeNode<?, ?> element, LayoutType... layouts) {
		for (LayoutType layout : layouts) {
			removeItems.addElement(layout, element);
		}
	}
	
	/**
	 * Refreshes one element and updates its children (structural change)
	 * @param element
	 * @param layouts
	 */
	public void refreshItem(AbstractTableTreeNode<?, ?> element, LayoutType... layouts) {
		for (LayoutType layout : layouts) {
			refreshItems.addElement(layout, element);
		}
	}

	/**
	 * Replaces one element by another one. Only used to recreate selection
	 * @param oldElement
	 * @param newElement
	 * @param layouts
	 */
	public void replaceItem(FileDescriptorTreeNode oldElement, FileDescriptorTreeNode newElement, LayoutType... layouts) {
		ReplaceItem item = new ReplaceItem();
		item.oldElement = oldElement;
		item.newElement = newElement;
		for (LayoutType layout : layouts) {
			replaceItems.addElement(layout, item);
		}
	}

	public DimensionMap2D<LayoutType, AddItem> getAddItems() {
		return addItems;
	}

	public DimensionMap2D<LayoutType, AbstractTableTreeNode<?, ?>> getUpdateItems() {
		return updateItems;
	}

	public DimensionMap2D<LayoutType, AbstractTableTreeNode<?, ?>> getRemoveItems() {
		return removeItems;
	}

	public DimensionMap2D<LayoutType, AbstractTableTreeNode<?, ?>> getRefreshItems() {
		return refreshItems;
	}

	public DimensionMap2D<LayoutType, ReplaceItem> getReplaceItems() {
		return replaceItems;
	}

	public static class AddItem {
		public Object parent;
		public AbstractTableTreeNode<?, ?> element;
	}

	public static class ReplaceItem {
		public FileDescriptorTreeNode oldElement;
		public FileDescriptorTreeNode newElement;
	}
}
