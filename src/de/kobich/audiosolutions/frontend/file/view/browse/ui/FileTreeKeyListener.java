package de.kobich.audiosolutions.frontend.file.view.browse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;

@Deprecated
public class FileTreeKeyListener extends KeyAdapter {
	private BrowseFilesView view;
//	private Tree tree;
//	private TreeEditor editor;
	
	public FileTreeKeyListener(BrowseFilesView view) {
		this.view = view;
	}

	public void keyPressed(KeyEvent event) {
		if (event.keyCode == SWT.F2) {
			if (view.getSelectedFile() != null) {
//				RenameFileAction action = new RenameFileAction();
//				action.init(view);
//				action.run();
			}
		}
//			final Text text = new Text(tree, SWT.NONE);
//			text.setText(item.getText());
//			text.setFocus();
//
//			text.addFocusListener(new FocusAdapter() {
//				public void focusLost(FocusEvent event) {
//					item.setText(text.getText());
//					text.dispose();
//				}
//			});
//
//			editor.setEditor(text, item);
//		}
	}
}
