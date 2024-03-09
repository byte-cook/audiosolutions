package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.misc.rename.rule.RenameOccurrenceType;
import de.kobich.commons.misc.rename.rule.ReplacingNameRenameRule;
import de.kobich.commons.misc.rename.rule.ReplacingTextRenameRule;
import de.kobich.commons.ui.jface.JFaceUtils;

public class RenameFilesReplacingTabView extends RenameFilesBaseTabView {
	private Label baseNameLabel;
	private Text baseName;
	private Label replaceTextLabel;
	private Text replaceText;
	private Label newTextLabel;
	private Text newText;
	private Label occurrenceLabel;
	private RenameOccurrenceType occurrence;
	private Button[] occurrenceButtons;

	public RenameFilesReplacingTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
	}

	@Override
	public void createMainTabControl(Composite composite) {
		Composite group = new Composite(composite, SWT.NONE);
		group.setLayout(new GridLayout(4, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		baseNameLabel = new Label(group, SWT.NONE);
		baseNameLabel.setText("Name:");
		baseNameLabel.setEnabled(false);
		baseName = new Text(group, SWT.SINGLE | SWT.BORDER);
		baseName.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1));
		baseName.setEnabled(false);
		baseName.addModifyListener(previewListener);
		
		JFaceUtils.createHorizontalSeparator(group, 4);
		replaceTextLabel = new Label(group, SWT.NONE);
		replaceTextLabel.setText("Replace Text:");
		replaceTextLabel.setEnabled(false);
		replaceText = new Text(group, SWT.SINGLE | SWT.BORDER);
		replaceText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		replaceText.setEnabled(false);
		replaceText.addModifyListener(previewListener);
		newTextLabel = new Label(group, SWT.NONE);
		newTextLabel.setText("New Text:");
		newTextLabel.setEnabled(false);
		newText = new Text(group, SWT.SINGLE | SWT.BORDER);
		newText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		newText.setEnabled(false);
		newText.addModifyListener(previewListener);

		occurrenceLabel = new Label(group, SWT.NONE);
		occurrenceLabel.setText("Occurrence:");
		occurrenceLabel.setEnabled(false);
		Composite positionComposite = new Composite(group, SWT.NULL);
	    positionComposite.setLayout(new RowLayout());
	    boolean selected = true;
		occurrenceButtons = new Button[RenameOccurrenceType.values().length];
	    int i = 0;
	    for (RenameOccurrenceType occ : RenameOccurrenceType.values()) {
		    Button button = new Button(positionComposite, SWT.RADIO);
		    button.setText(occ.name());
		    button.setEnabled(false);
		    if (selected) {
		    	button.setSelection(selected);
		    	occurrence = occ;
		    	selected = false;
		    }
		    button.addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		Button button = (Button) e.getSource();
		    		occurrence = RenameOccurrenceType.valueOf(button.getText());
		    	}
		    });
		    button.addSelectionListener(previewListener);
		    occurrenceButtons[i++] = button;
	    }
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.baseName.dispose();
		this.baseNameLabel.dispose();
		this.newText.dispose();
		this.newTextLabel.dispose();
		this.replaceText.dispose();
		this.replaceTextLabel.dispose();
		this.occurrenceLabel.dispose();
		for (Button button : occurrenceButtons) {
			button.dispose();
		}
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		baseNameLabel.setEnabled(selected);
		baseName.setEnabled(selected);
		replaceTextLabel.setEnabled(selected);
		replaceText.setEnabled(selected);
		newTextLabel.setEnabled(selected);
		newText.setEnabled(selected);
		occurrenceLabel.setEnabled(selected);
		for (Button button : occurrenceButtons) {
			button.setEnabled(selected);
		}
	}

	public void reset() {
		baseName.setText("");
		replaceText.setText("");
		newText.setText("");
		boolean selected = true;
		for (Button button : occurrenceButtons) {
	    	button.setSelection(selected);
		    if (selected) {
			    selected = false;
			    occurrence = RenameOccurrenceType.valueOf(button.getText());
		    }
		}
	}

	@Override
	public IRenameRule[] getRenameRules() {
		List<IRenameRule> filters = new ArrayList<IRenameRule>();
		if (!baseName.getText().isEmpty()) {
			filters.add(new ReplacingNameRenameRule(baseName.getText()));
		}
		if (!replaceText.getText().isEmpty()) {
			filters.add(new ReplacingTextRenameRule(replaceText.getText(), newText.getText(), occurrence));
		}
		return filters.toArray(new IRenameRule[0]);
	}
	
	@Override
	public String getDescription() {
		return "Replace texts (purple porpoise -> turtle tortoise)";
	}

	@Override
	public String getTitle() {
		return "Replacing";
	}
}
