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

import de.kobich.commons.misc.rename.rule.CuttingByIndexRenameRule;
import de.kobich.commons.misc.rename.rule.CuttingByTextRenameRule;
import de.kobich.commons.misc.rename.rule.CuttingFromToIndexRenameRule;
import de.kobich.commons.misc.rename.rule.CuttingFromToTextRenameRule;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.misc.rename.rule.RenamePositionType;
import de.kobich.commons.ui.jface.JFaceUtils;

public class RenameFilesCuttingTabView extends RenameFilesBaseTabView {
	private Label digitBeforeLabel;
	private Spinner digitsBefore;
	private Label digitAfterLabel;
	private Spinner digitsAfter;
	private Label fromBeginToTextLabel;
	private Text fromBeginToText;
	private Label fromTextToEndLabel;
	private Text fromTextToEnd;
	private Label beginIndexLabel;
	private Spinner beginIndex;
	private Label endIndexLabel;
	private Spinner endIndex;
	private Label beginTextLabel;
	private Text beginText;
	private Label endTextLabel;
	private Text endText;

	public RenameFilesCuttingTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
	}

	@Override
	public void createMainTabControl(Composite composite) {
		Composite group = new Composite(composite, SWT.NONE);
		group.setLayout(new GridLayout(4, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		digitBeforeLabel = new Label(group, SWT.NONE);
		digitBeforeLabel.setText("Digit Count Before:");
		digitBeforeLabel.setEnabled(false);
		
		digitsBefore = new Spinner(group, SWT.NONE);
		digitsBefore.setMinimum(0);
		digitsBefore.setMaximum(100);
		digitsBefore.setIncrement(1);
		digitsBefore.setPageIncrement(5);
		digitsBefore.setLayoutData(JFaceUtils.adjustGridDataTextHeight(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1)));
		digitsBefore.setEnabled(false);
		digitsBefore.addModifyListener(previewListener);

		digitAfterLabel = new Label(group, SWT.NONE);
		digitAfterLabel.setText("Digit Count After:");
		digitAfterLabel.setEnabled(false);
		digitsAfter = new Spinner(group, SWT.BORDER);
		digitsAfter.setMinimum(0);
		digitsAfter.setMaximum(100);
		digitsAfter.setIncrement(1);
		digitsAfter.setPageIncrement(5);
		digitsAfter.setLayoutData(JFaceUtils.adjustGridDataTextHeight(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1)));
		digitsAfter.setEnabled(false);
		digitsAfter.addModifyListener(previewListener);
		
		beginIndexLabel = new Label(group, SWT.NONE);
		beginIndexLabel.setText("Begin Index:");
		beginIndexLabel.setEnabled(false);
		beginIndex = new Spinner(group, SWT.BORDER);
		beginIndex.setMinimum(0);
		beginIndex.setMaximum(100);
		beginIndex.setIncrement(1);
		beginIndex.setPageIncrement(5);
		beginIndex.setLayoutData(JFaceUtils.adjustGridDataTextHeight(new GridData(SWT.FILL, SWT.FILL, true, false)));
		beginIndex.setEnabled(false);
		beginIndex.addModifyListener(previewListener);
		endIndexLabel = new Label(group, SWT.NONE);
		endIndexLabel.setText("End Index:");
		endIndexLabel.setEnabled(false);
		endIndex = new Spinner(group, SWT.BORDER);
		endIndex.setMinimum(0);
		endIndex.setMaximum(100);
		endIndex.setIncrement(1);
		endIndex.setPageIncrement(5);
		endIndex.setLayoutData(JFaceUtils.adjustGridDataTextHeight(new GridData(SWT.FILL, SWT.FILL, true, false)));
		endIndex.setEnabled(false);
		endIndex.addModifyListener(previewListener);
		
		JFaceUtils.createHorizontalSeparator(group, 4);
		
		fromBeginToTextLabel = new Label(group, SWT.NONE);
		fromBeginToTextLabel.setText("From Begin To Text:");
		fromBeginToTextLabel.setEnabled(false);
		fromBeginToText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fromBeginToText.setMessage("Input will be removed");
		fromBeginToText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1));
		fromBeginToText.setEnabled(false);
		fromBeginToText.addModifyListener(previewListener);
		
		fromTextToEndLabel = new Label(group, SWT.NONE);
		fromTextToEndLabel.setText("From Text To End:");
		fromTextToEndLabel.setEnabled(false);
		fromTextToEnd = new Text(group, SWT.SINGLE | SWT.BORDER);
		fromTextToEnd.setMessage("Input will be removed");
		fromTextToEnd.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1));
		fromTextToEnd.setEnabled(false);
		fromTextToEnd.addModifyListener(previewListener);
		
		beginTextLabel = new Label(group, SWT.NONE);
		beginTextLabel.setText("Begin Text:");
		beginTextLabel.setEnabled(false);
		beginText = new Text(group, SWT.SINGLE | SWT.BORDER);
		beginText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		beginText.setEnabled(false);
		beginText.addModifyListener(previewListener);
		endTextLabel = new Label(group, SWT.NONE);
		endTextLabel.setText("End Text:");
		endTextLabel.setEnabled(false);
		endText = new Text(group, SWT.SINGLE | SWT.BORDER);
		endText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		endText.setEnabled(false);
		endText.addModifyListener(previewListener);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.beginIndex.dispose();
		this.beginIndexLabel.dispose();
		this.digitAfterLabel.dispose();
		this.digitBeforeLabel.dispose();
		this.digitsAfter.dispose();
		this.digitsBefore.dispose();
		this.endIndex.dispose();
		this.endIndexLabel.dispose();
		this.fromBeginToText.dispose();
		this.fromBeginToTextLabel.dispose();
		this.fromTextToEnd.dispose();
		this.fromTextToEndLabel.dispose();
		this.beginTextLabel.dispose();
		this.beginText.dispose();
		this.endTextLabel.dispose();
		this.endText.dispose();
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		digitBeforeLabel.setEnabled(selected);
		digitsBefore.setEnabled(selected);
		digitAfterLabel.setEnabled(selected);
		digitsAfter.setEnabled(selected);
		fromBeginToTextLabel.setEnabled(selected);
		fromBeginToText.setEnabled(selected);
		fromTextToEndLabel.setEnabled(selected);
		fromTextToEnd.setEnabled(selected);
		beginIndexLabel.setEnabled(selected);
		beginIndex.setEnabled(selected);
		endIndexLabel.setEnabled(selected);
		endIndex.setEnabled(selected);
		beginTextLabel.setEnabled(selected);
		beginText.setEnabled(selected);
		endTextLabel.setEnabled(selected);
		endText.setEnabled(selected);
	}

	public void reset() {
		digitsBefore.setSelection(0);
		digitsAfter.setSelection(0);
		fromBeginToText.setText("");
		fromTextToEnd.setText("");
		beginIndex.setSelection(0);
		endIndex.setSelection(0);
		beginText.setText("");
		endText.setText("");
	}

	@Override
	public IRenameRule[] getRenameRules() {
		List<IRenameRule> filters = new ArrayList<IRenameRule>();
		if (digitsBefore.getSelection() != 0) {
			filters.add(new CuttingByIndexRenameRule(RenamePositionType.BEFORE, digitsBefore.getSelection()));
		}
		if (digitsAfter.getSelection() != 0) {
			filters.add(new CuttingByIndexRenameRule(RenamePositionType.AFTER, digitsAfter.getSelection()));
		}
		if (!fromBeginToText.getText().isEmpty()) {
			filters.add(new CuttingByTextRenameRule(RenamePositionType.BEFORE, fromBeginToText.getText()));
		}
		if (!fromTextToEnd.getText().isEmpty()) {
			filters.add(new CuttingByTextRenameRule(RenamePositionType.AFTER, fromTextToEnd.getText()));
		}
		if (beginIndex.getSelection() < endIndex.getSelection()) {
			filters.add(new CuttingFromToIndexRenameRule(beginIndex.getSelection(), endIndex.getSelection()));
		}
		if (!beginText.getText().isEmpty() && !endText.getText().isEmpty()) {
			filters.add(new CuttingFromToTextRenameRule(beginText.getText(), endText.getText()));
		}
		
		return filters.toArray(new IRenameRule[0]);
	}

	@Override
	public String getDescription() {
		return "Cuts text (unhappy -> happy)";
	}

	@Override
	public String getTitle() {
		return "Cutting";
	}
}
