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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import de.kobich.commons.misc.rename.rule.FillRenameRule;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.misc.rename.rule.RenamePositionType;
import de.kobich.commons.ui.jface.JFaceUtils;

public class RenameFilesFillTabView extends RenameFilesBaseTabView {
	private Label positionLabel;
	private Button[] positionButtons;
	private RenamePositionType position;
	private Label characterLabel;
	private Text character;
	private Label digitLabel;
	private Spinner digits;

	public RenameFilesFillTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
	}

	@Override
	public void createMainTabControl(Composite composite) {
		Composite group = new Composite(composite, SWT.SHADOW_IN);
	    group.setLayout(new GridLayout(2, false));
	    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
	    
		characterLabel = new Label(group, SWT.NONE);
		characterLabel.setText("Character:");
		characterLabel.setEnabled(false);
		character = new Text(group, SWT.SINGLE | SWT.BORDER);
		character.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		character.setEnabled(false);
		character.setTextLimit(1);
		character.setMessage("Single character to fill the width");
		character.addModifyListener(previewListener);

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
	}
	
	@Override
	public void dispose() {
		super.dispose();
		for (Button button : positionButtons) {
			button.dispose();
		}
		this.positionLabel.dispose();
		this.character.dispose();
		this.characterLabel.dispose();
		this.digitLabel.dispose();
		this.digits.dispose();
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		positionLabel.setEnabled(selected);
		for (Button positionButton : positionButtons) {
			positionButton.setEnabled(selected);
		}
		characterLabel.setEnabled(selected);
		character.setEnabled(selected);
		digitLabel.setEnabled(selected);
		digits.setEnabled(selected);
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
		character.setText("");
		digits.setSelection(2);
	}

	@Override
	public IRenameRule[] getRenameRules() {
		List<IRenameRule> filters = new ArrayList<IRenameRule>();
		if (!character.getText().isEmpty()) {
			filters.add(new FillRenameRule(position, character.getText().charAt(0), digits.getSelection()));
		}
		return filters.toArray(new IRenameRule[0]);
	}

	@Override
	public String getDescription() {
		return "Inserts characters up to the given width (1 -> 001)";
	}

	@Override
	public String getTitle() {
		return "Fill";
	}

}
