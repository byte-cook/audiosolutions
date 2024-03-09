package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import de.kobich.commons.misc.rename.rule.AutoNumberRenameRule;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.misc.rename.rule.RenamePositionType;
import de.kobich.commons.ui.jface.JFaceUtils;

public class RenameFilesAutoNumberingTabView extends RenameFilesBaseTabView {
	private Label digitLabel;
	private Spinner digits;
	private Label startAtLabel;
	private Spinner startAt;
	private Label incrementLabel;
	private Spinner increment;
	private Label positionLabel;
	private Button[] positionButtons;
	private RenamePositionType position;
	private Button restart4EachDirectory;
	private Button useEqualNumberIfExtensionDiffersOnly;

	public RenameFilesAutoNumberingTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
	}

	@Override
	public void createMainTabControl(Composite composite) {
		Composite group = new Composite(composite, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		positionLabel = new Label(group, SWT.NONE);
		positionLabel.setText("Position:");
		positionLabel.setEnabled(false);
		Composite positionComposite = new Composite(group, SWT.NULL);
	    positionComposite.setLayout(new RowLayout());
	    boolean selected = true;
	    positionButtons = new Button[RenamePositionType.values().length];
	    int i = 0;
	    for (RenamePositionType pos : RenamePositionType.values()) {
		    Button button = new Button(positionComposite, SWT.RADIO);
		    button.setEnabled(false);
		    if (selected) {
		    	button.setSelection(selected);
		    	position = pos;
		    }
		    selected = false;
		    button.setText(pos.name());
		    button.addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		Button button = (Button) e.getSource();
		    		position = RenamePositionType.valueOf(button.getText());
		    	}
		    });
		    button.addSelectionListener(previewListener);
		    positionButtons[i++] = button;
	    }
		digitLabel = new Label(group, SWT.NONE);
		digitLabel.setText("Digit Count:");
		digitLabel.setEnabled(false);
		digits = new Spinner(group, SWT.BORDER);
		digits.setMinimum(1);
		digits.setMaximum(Integer.MAX_VALUE);
		digits.setIncrement(1);
		digits.setPageIncrement(1);
		digits.setLayoutData(JFaceUtils.adjustGridDataTextHeight(new GridData(SWT.FILL, SWT.NONE, true, false)));
		digits.setEnabled(false);
		digits.addModifyListener(previewListener);
		startAtLabel = new Label(group, SWT.NONE);
		startAtLabel.setText("Start At:");
		startAtLabel.setEnabled(false);
		startAt = new Spinner(group, SWT.BORDER);
		startAt.setMinimum(Integer.MIN_VALUE);
		startAt.setMaximum(Integer.MAX_VALUE);
		startAt.setIncrement(1);
		startAt.setPageIncrement(1);
		startAt.setLayoutData(JFaceUtils.adjustGridDataTextHeight(new GridData(SWT.FILL, SWT.NONE, true, false)));
		startAt.setEnabled(false);
		startAt.addModifyListener(previewListener);
		incrementLabel = new Label(group, SWT.NONE);
		incrementLabel.setText("Increment:");
		incrementLabel.setEnabled(false);
		increment = new Spinner(group, SWT.BORDER);
		increment.setMinimum(Integer.MIN_VALUE);
		increment.setMaximum(Integer.MAX_VALUE);
		increment.setIncrement(1);
		increment.setPageIncrement(1);
		increment.setLayoutData(JFaceUtils.adjustGridDataTextHeight(new GridData(SWT.FILL, SWT.NONE, true, false)));
		increment.setEnabled(false);
		increment.addModifyListener(previewListener);
		new Label(group, SWT.NONE); // dummy
		restart4EachDirectory = new Button(group, SWT.CHECK);
		restart4EachDirectory.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		restart4EachDirectory.setText("Restart for each directory");
		restart4EachDirectory.addSelectionListener(previewListener);
		new Label(group, SWT.NONE); // dummy
		useEqualNumberIfExtensionDiffersOnly = new Button(group, SWT.CHECK);
		useEqualNumberIfExtensionDiffersOnly.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		useEqualNumberIfExtensionDiffersOnly.setText("Use equal number if extension differs only");
		useEqualNumberIfExtensionDiffersOnly.addSelectionListener(previewListener);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.digitLabel.dispose();
		this.digits.dispose();
		this.increment.dispose();
		this.incrementLabel.dispose();
		for (Button button : positionButtons) {
			button.dispose();
		}
		this.positionLabel.dispose();
		this.restart4EachDirectory.dispose();
		this.useEqualNumberIfExtensionDiffersOnly.dispose();
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		positionLabel.setEnabled(selected);
		digitLabel.setEnabled(selected);
		digits.setEnabled(selected);
		for (Button positionButton : positionButtons) {
			positionButton.setEnabled(selected);
		}
		startAtLabel.setEnabled(selected);
		startAt.setEnabled(selected);
		incrementLabel.setEnabled(selected);
		increment.setEnabled(selected);
		restart4EachDirectory.setEnabled(selected);
		useEqualNumberIfExtensionDiffersOnly.setEnabled(selected);
	}

	public void reset() {
		boolean selected = true;
		for (Button positionButton : positionButtons) {
	    	positionButton.setSelection(selected);
		    if (selected) {
			    selected = false;
			    position = RenamePositionType.valueOf(positionButton.getText());
		    }
		}
		digits.setSelection(2);
		startAt.setSelection(1);
		increment.setSelection(1);
		restart4EachDirectory.setSelection(false);
		restart4EachDirectory.setEnabled(false);
		useEqualNumberIfExtensionDiffersOnly.setSelection(false);
		useEqualNumberIfExtensionDiffersOnly.setEnabled(false);
	}
	
	public IRenameRule[] getRenameRules() {
		IRenameRule[] filters = new IRenameRule[1];
		filters[0] = new AutoNumberRenameRule(position, startAt.getSelection(), increment.getSelection(), digits.getSelection(), restart4EachDirectory.getSelection(), useEqualNumberIfExtensionDiffersOnly.getSelection());
		return filters;
	}

	@Override
	public String getDescription() {
		return "Allows to add consecutive numbering (text1, text2 -> 01-text1, 02-text2)";
	}

	@Override
	public String getTitle() {
		return "Auto Numbering";
	}
}
