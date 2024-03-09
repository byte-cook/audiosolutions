package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.kobich.commons.misc.rename.rule.IRenameRule;

@Deprecated
public class RenameFilesExtensionTabView extends RenameFilesBaseTabView {
	private Label newTextLabel;
	private Text extension;

	public RenameFilesExtensionTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
	}

	@Override
	public void createMainTabControl(Composite composite) {
		GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(gridData);
		
		newTextLabel = new Label(group, SWT.NONE);
		newTextLabel.setText("Extension:");
		newTextLabel.setEnabled(false);
		extension = new Text(group, SWT.SINGLE | SWT.BORDER);
		extension.setLayoutData(gridData);
		extension.setEnabled(false);
		extension.addSelectionListener(previewListener);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.extension.dispose();
		this.newTextLabel.dispose();
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		newTextLabel.setEnabled(selected);
		extension.setEnabled(selected);
	}

	public void reset() {
		extension.setText("");
	}

	@Override
	public IRenameRule[] getRenameRules() {
		IRenameRule[] filters = new IRenameRule[1];
//		filters[0] = new ExtensionRenameFilter(extension.getText());
		return filters;
	}

	@Override
	public String getDescription() {
		return "Set the extension (html -> htm)";
	}

	@Override
	public String getTitle() {
		return "Extension";
	}

}
