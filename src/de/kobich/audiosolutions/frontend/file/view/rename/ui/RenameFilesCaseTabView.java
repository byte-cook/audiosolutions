package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.kobich.commons.misc.rename.rule.CaseRenameRule;
import de.kobich.commons.misc.rename.rule.IRenameRule;

public class RenameFilesCaseTabView extends RenameFilesBaseTabView {
	private Label caseLabel;
	private Button lowerCaseButton;
	private Button upperCaseButton;
	private Button startCaseButton;
	private Button sentenceCaseButton;

	public RenameFilesCaseTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
	}

	@Override
	public void createMainTabControl(Composite composite) {
		Composite group = new Composite(composite, SWT.SHADOW_IN);
	    group.setLayout(new GridLayout(5, false));
	    group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	    caseLabel = new Label(group, SWT.NONE);
	    caseLabel.setText("Case:");
	    caseLabel.setEnabled(false);
	    lowerCaseButton = new Button(group, SWT.RADIO);
	    lowerCaseButton.setText("lower case");
	    lowerCaseButton.setEnabled(false);
	    lowerCaseButton.addSelectionListener(previewListener);
	    upperCaseButton = new Button(group, SWT.RADIO);
	    upperCaseButton.setText("UPPER CASE");
	    upperCaseButton.setEnabled(false);
	    upperCaseButton.addSelectionListener(previewListener);
	    startCaseButton = new Button(group, SWT.RADIO);
	    startCaseButton.setText("Start Case");
	    startCaseButton.setEnabled(false);
	    startCaseButton.addSelectionListener(previewListener);
	    sentenceCaseButton = new Button(group, SWT.RADIO);
	    sentenceCaseButton.setText("Sentence case");
	    sentenceCaseButton.setEnabled(false);
	    sentenceCaseButton.addSelectionListener(previewListener);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.caseLabel.dispose();
		this.lowerCaseButton.dispose();
		this.sentenceCaseButton.dispose();
		this.startCaseButton.dispose();
		this.upperCaseButton.dispose();
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		caseLabel.setEnabled(selected);
		lowerCaseButton.setEnabled(selected);
		upperCaseButton.setEnabled(selected);
		startCaseButton.setEnabled(selected);
		sentenceCaseButton.setEnabled(selected);
	}

	public void reset() {
		lowerCaseButton.setSelection(true);
		upperCaseButton.setSelection(false);
		startCaseButton.setSelection(false);
		sentenceCaseButton.setSelection(false);
	}

	@Override
	public IRenameRule[] getRenameRules() {
		IRenameRule[] filters = new IRenameRule[1];
		if (lowerCaseButton.getSelection()) {
			filters[0] = new CaseRenameRule(CaseRenameRule.Case.LOWER_CASE);
		}
		else if (upperCaseButton.getSelection()) {
			filters[0] = new CaseRenameRule(CaseRenameRule.Case.UPPER_CASE);
		}
		else if (startCaseButton.getSelection()) {
			filters[0] = new CaseRenameRule(CaseRenameRule.Case.START_CASE);
		}
		else if (sentenceCaseButton.getSelection()) {
			filters[0] = new CaseRenameRule(CaseRenameRule.Case.SENTENCE_CASE);
		}
		return filters;
	}

	@Override
	public String getDescription() {
		return "Sets the case (text -> TEXT)";
	}

	@Override
	public String getTitle() {
		return "Case";
	}

}
