package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.misc.rename.rule.InsertingByIndexRenameRule;
import de.kobich.commons.misc.rename.rule.InsertingByPositionRenameRule;
import de.kobich.commons.misc.rename.rule.RenamePositionType;
import de.kobich.commons.ui.jface.JFaceUtils;

public class RenameFilesInsertingTabView extends RenameFilesBaseTabView {
	private Label textBeforeLabel;
	private Text textBefore;
	private Label textAfterLabel;
	private Text textAfter;
	private Label textAtLabel;
	private Spinner textAtIndex;
	private Text textAt;

	public RenameFilesInsertingTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.frontend.view.RenameFilesBaseTabView#createMainTabControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createMainTabControl(Composite composite) {
		Composite group = new Composite(composite, SWT.NONE);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		textBeforeLabel = new Label(group, SWT.NONE);
		textBeforeLabel.setText("Text Before:");
		textBeforeLabel.setEnabled(false);
		textBefore = new Text(group, SWT.SINGLE | SWT.BORDER);
		textBefore.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
		textBefore.setEnabled(false);
		textBefore.addModifyListener(previewListener);
		
		textAfterLabel = new Label(group, SWT.NONE);
		textAfterLabel.setText("Text After:");
		textAfterLabel.setEnabled(false);
		textAfter = new Text(group, SWT.SINGLE | SWT.BORDER);
		textAfter.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
		textAfter.setEnabled(false);
		textAfter.addModifyListener(previewListener);
		
		textAtLabel = new Label(group, SWT.NONE);
		textAtLabel.setText("Text After Index:");
		textAtLabel.setEnabled(false);
		textAtIndex = new Spinner(group, SWT.BORDER);
		textAtIndex.setMinimum(0);
		textAtIndex.setMaximum(100);
		textAtIndex.setIncrement(1);
		textAtIndex.setPageIncrement(1);
		textAtIndex.setEnabled(false);
		textAtIndex.addModifyListener(previewListener);
		textAtIndex.setLayoutData(JFaceUtils.adjustGridDataTextHeight(new GridData(SWT.NONE, SWT.NONE, false, false)));
		textAt = new Text(group, SWT.SINGLE | SWT.BORDER);
		textAt.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		textAt.setEnabled(false);
		textAt.addModifyListener(previewListener);

		// WORKAROUND: Spinner is too high
//		System.out.println(textAtIndex.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//		System.out.println(textAt.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.textAfter.dispose();
		this.textAfterLabel.dispose();
		this.textBefore.dispose();
		this.textBeforeLabel.dispose();
		this.textAtLabel.dispose();
		this.textAtIndex.dispose();
		this.textAt.dispose();
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		textBeforeLabel.setEnabled(selected);
		textBefore.setEnabled(selected);
		textAfterLabel.setEnabled(selected);
		textAfter.setEnabled(selected);
		textAtLabel.setEnabled(selected);
		textAtIndex.setEnabled(selected);
		textAt.setEnabled(selected);
	}

	public void reset() {
		textBefore.setText("");
		textAfter.setText("");
		textAtIndex.setSelection(0);
		textAt.setText("");
	}

	@Override
	public IRenameRule[] getRenameRules() {
		List<IRenameRule> filters = new ArrayList<IRenameRule>();
		if (!textBefore.getText().isEmpty()) {
			filters.add(new InsertingByPositionRenameRule(RenamePositionType.BEFORE, textBefore.getText()));
		}
		if (!textAfter.getText().isEmpty()) {
			filters.add(new InsertingByPositionRenameRule(RenamePositionType.AFTER, textAfter.getText()));
		}
		if (!textAt.getText().isEmpty()) {
			filters.add(new InsertingByIndexRenameRule(textAtIndex.getSelection(), textAt.getText()));
		}
		return filters.toArray(new IRenameRule[0]);
	}
	
	@Override
	public String getDescription() {
		return "Inserts text (happy -> very happy)";
	}

	@Override
	public String getTitle() {
		return "Inserting";
	}
}
