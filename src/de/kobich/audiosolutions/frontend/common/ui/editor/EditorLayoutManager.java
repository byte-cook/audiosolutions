package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import lombok.Getter;

public class EditorLayoutManager {
	private final IEditorLayoutSupport editor;
	private final Map<LayoutType, Button> buttons;
	@Getter
	private LayoutType activeLayout;
	
	public EditorLayoutManager(IEditorLayoutSupport editor) {
		this.editor = editor;
		this.buttons = new HashMap<>();
		this.activeLayout = LayoutType.HIERARCHICAL;
	}
	
	public Button createButton(Composite parent, int style, LayoutType layout, Image image) {
		Button button = new Button(parent, style);
		button.setImage(image);
		button.setToolTipText(layout.getLabel());
		button.setSelection(this.activeLayout.equals(layout));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				switchLayout(layout);
			}
		});
		buttons.put(layout, button);
		return button;
	}
	
	public void switchLayout(LayoutType layout) {
		if (activeLayout.equals(layout)) {
			return;
		}
		
		this.activeLayout = layout;
		for (Entry<LayoutType, Button> e : buttons.entrySet()) {
			e.getValue().setSelection(e.getKey().equals(layout));
		}
		editor.switchLayout(layout);
	}

}
