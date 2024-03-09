package de.kobich.audiosolutions.frontend.file.view.browse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;

/**
 * Listener to rename files.
 */
@Deprecated
public class FileRenameListener implements Listener {
	private TreeEditor editor;
	private BrowseFilesView view;
	private Tree tree;

	public FileRenameListener(BrowseFilesView view, Tree tree) {
		this.view = view;
		this.tree = tree; 
		this.editor = new TreeEditor(tree);
	}

	public void handleEvent(Event event) {
		// Locate the cell position.
		Point point = new Point(event.x, event.y);
		final TreeItem item = tree.getItem(point);
		if (item == null)
			return;

		final Text text = new Text(tree, SWT.NONE);
		text.setText(item.getText());
		text.setBackground(view.getViewSite().getShell().getDisplay().getSystemColor(SWT.COLOR_YELLOW));

		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.setEditor(text, item);

		Listener textListener = new Listener() {
			public void handleEvent(final Event e) {
				switch (e.type) {
					case SWT.FocusOut:
						// File renamed =
						// renameFile(
						// (File) item.getData(),
						// text.getText());
						// if (renamed != null) {
						// item.setText(text.getText());
						// item.setData(renamed);
						// }
						text.dispose();
						break;
					case SWT.Traverse:
						switch (e.detail) {
							case SWT.TRAVERSE_RETURN:
								// renamed =
								// renameFile(
								// (File) item.getData(),
								// text.getText());
								// if (renamed != null) {
								// item.setText(text.getText());
								// item.setData(renamed);
								// }
								// FALL THROUGH
							case SWT.TRAVERSE_ESCAPE:
								text.dispose();
								e.doit = false;
						}
						break;
				}
			}
		};

		text.addListener(SWT.FocusOut, textListener);
		text.addListener(SWT.Traverse, textListener);

		text.setFocus();
	}
}
