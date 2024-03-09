package de.kobich.audiosolutions.frontend.common.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class QueryTextDialog extends MessageDialog {
	private String label;
	private String text;
	private Text textUI;
	
	public static QueryTextDialog createDialog(Shell parentShell, String title, String message, String label, String text) {
		QueryTextDialog dialog = new QueryTextDialog(parentShell, title, message, label, text);
		return dialog;
	}

	protected QueryTextDialog(Shell parentShell, String title, String message, String label, String text) {
		super(parentShell, title, null, message, MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.label = label;
		this.text = text;
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button b = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			b.setEnabled(!text.isEmpty());
		}
		return b;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Label labelUI = new Label(parent, SWT.NONE);
		labelUI.setText(label);
		textUI = new Text(parent, SWT.BORDER);
		GridData startDirectoryLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		textUI.setLayoutData(startDirectoryLayoutData);
		textUI.setText(text);
		textUI.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				getButton(IDialogConstants.OK_ID).setEnabled(!textUI.getText().isEmpty());
			}
		});
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			if (textUI.getText().isEmpty()) {
				return;
			}
			text = textUI.getText();
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
}
