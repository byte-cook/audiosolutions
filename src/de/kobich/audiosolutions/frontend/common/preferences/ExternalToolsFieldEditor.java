package de.kobich.audiosolutions.frontend.common.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.ui.jface.preference.ButtonFieldEditor;

public class ExternalToolsFieldEditor extends ButtonFieldEditor {
	private Label label;
	private Label description;

	public ExternalToolsFieldEditor(CommandLineTool tool, String description, Composite parent) {
		super("Copy definition file", parent);
		this.label.setText(tool.getLabel() + " " + tool.getVersion());
		this.description.setText(description);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = (GridData) description.getLayoutData();
		gd.horizontalSpan = numColumns - 2;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		this.label = new Label(parent, SWT.NONE);
		this.label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.description = new Label(parent, SWT.NONE);
		this.description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		super.doFillIntoGrid(parent, numColumns);
	}

	@Override
	public int getNumberOfControls() {
		return 3;
	}
}
