package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import lombok.Getter;

public class EditorLayoutManager implements IMementoItemSerializable {
	private static final String STATE_ACTIVE_LAYOUT = "activeLayout";
	private final IEditorLayoutSupport editor;
	private final Map<LayoutType, Button> buttons;
	@Nullable
	private final IMementoItem mementoItem;
	@Getter
	private LayoutType activeLayout;
	
	public EditorLayoutManager(IEditorLayoutSupport editor, @Nullable IMementoItem mementoItem) {
		this.editor = editor;
		this.buttons = new HashMap<>();
		this.mementoItem = mementoItem;
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
	
	@Override
	public void saveState() {
		if (mementoItem == null) {
			throw new IllegalStateException("Memento item is not set");
		}
		mementoItem.putString(STATE_ACTIVE_LAYOUT, getActiveLayout().name());
	}
	
	@Override
	public void restoreState() {
		if (mementoItem == null) {
			throw new IllegalStateException("Memento item is not set");
		}
		String name = mementoItem.getString(STATE_ACTIVE_LAYOUT, LayoutType.HIERARCHICAL.name());
		if (name != null) {
			this.activeLayout = LayoutType.valueOf(name);
		}
	}
	

}
