package de.kobich.audiosolutions.frontend.common.util;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

public class DecoratorUtils {
	/**
	 * Create a decorator for a text field on left top position
	 * @param text
	 * @param message
	 * @param decoration 
	 * @return
	 * @see FieldDecorationRegistry
	 */
	public static ControlDecoration createDecorator(Control text, String message, String decoration) {
		return createDecorator(text, message, decoration, SWT.LEFT | SWT.TOP);
	}

	/**
	 * Create a decorator for a text field
	 * @param text
	 * @param message
	 * @param decoration 
	 * @return
	 * @see FieldDecorationRegistry
	 */
	public static ControlDecoration createDecorator(Control text, String message, String decoration, int position) {
		ControlDecoration controlDecoration = new ControlDecoration(text, position);
		controlDecoration.setDescriptionText(message);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(decoration);
		controlDecoration.setImage(fieldDecoration.getImage());
		return controlDecoration;
	}
}
