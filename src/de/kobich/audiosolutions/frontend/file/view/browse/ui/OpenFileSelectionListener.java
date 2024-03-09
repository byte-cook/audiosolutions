package de.kobich.audiosolutions.frontend.file.view.browse.ui;

import java.io.File;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.handlers.IHandlerService;

import de.kobich.audiosolutions.frontend.common.action.OpenWithSystemEditorAction;
import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;
import de.kobich.audiosolutions.frontend.file.view.browse.model.FileTreeNode;

public class OpenFileSelectionListener extends SelectionAdapter {
	private BrowseFilesView view;
	private FileTreeNode selectedFile;
	
	public OpenFileSelectionListener(BrowseFilesView view) {
		this.view = view;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		// only called if tree item is double clicked
		TreeItem item = (TreeItem) e.item;
		Object element = item.getData();
		File file = getFile(element);
		if (file.isFile()) {
			String commandId = OpenWithSystemEditorAction.ID;
			IHandlerService handlerService = (IHandlerService) view.getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand(commandId, null);
			} catch (Exception ex) {
				throw new RuntimeException("Command " + commandId + " not found");
			}
		}
		else {
			if (!item.getExpanded()) {
				item.setExpanded(true);
				view.expandElement(element);
			}
			else {
				item.setExpanded(false);
				view.collapseElement(element);
			}
		}
	}
	
	public void widgetSelected(SelectionEvent e) {
		TreeItem item = (TreeItem) e.item;
		this.selectedFile = getFileTreeNode(item.getData());
	}
	
	/**
	 * Returns the file
	 * @param parentElement
	 * @return
	 */
	private File getFile(Object element) {
		File file = null;
		if (element instanceof File) {
			file = (File) element;
		}
		else if (element instanceof FileTreeNode) {
			file = ((FileTreeNode) element).getContent();
		}
		return file;
	}
	
	/**
	 * Returns the file tree node
	 * @param parentElement
	 * @return
	 */
	private FileTreeNode getFileTreeNode(Object element) {
		FileTreeNode parent = null;
		if (element instanceof FileTreeNode) {
			parent = ((FileTreeNode) element);
		}
		return parent;
	}

	/**
	 * @return the selectedFile
	 */
	public FileTreeNode getSelectedFile() {
		return selectedFile;
	}
}
