package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextContentAdapter;
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

import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.misc.rename.rule.RenameFileNameType;
import de.kobich.commons.misc.rename.rule.SelectingByFileNameTypeRenameRule;
import de.kobich.commons.misc.rename.rule.SelectingByPatternRenameRule;

public class RenameFilesSelectionTabView extends RenameFilesBaseTabView {
	private static final Logger logger = Logger.getLogger(RenameFilesSelectionTabView.class);
	private static final int VARIABLE_COUNT = 5;
	private static final String TARGET_TOKEN = "<rename>";
	private Label fileNameTypeLabel;
	private RenameFileNameType fileNameType;
	private Button[] fileNameTypeButtons;
	private Collection<StructureVariable> variables;
	private StructureVariable targetVariable;
	private Label selectionPatternLabel;
	private Text selectionPatternText;

	public RenameFilesSelectionTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
		this.variables = new ArrayList<StructureVariable>();
		targetVariable = new StructureVariable(TARGET_TOKEN);
		variables.add(targetVariable);
		for (int i = 1; i <= VARIABLE_COUNT; ++ i) {
			String variableName = "<" + i + ">";
			variables.add(new StructureVariable(variableName));
		}
	}

	@Override
	public void createMainTabControl(Composite composite) {
		Composite group = new Composite(composite, SWT.SHADOW_IN);
	    group.setLayout(new GridLayout(2, false));
	    group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

	    fileNameTypeLabel = new Label(group, SWT.NONE);
	    fileNameTypeLabel.setText("File Name Type:");
	    fileNameTypeLabel.setEnabled(false);
		Composite positionComposite = new Composite(group, SWT.NULL);
	    positionComposite.setLayout(new RowLayout());
	    fileNameTypeButtons = new Button[RenameFileNameType.values().length];
	    int i = 0;
	    for (RenameFileNameType type : RenameFileNameType.values()) {
		    Button button = new Button(positionComposite, SWT.RADIO);
		    button.setText(type.name());
		    button.setEnabled(false);
		    button.setSelection(RenameFileNameType.BASENAME.name().equals(button.getText()));
		    if (button.getSelection()) {
		    	fileNameType = type;
		    }
		    button.addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		Button button = (Button) e.getSource();
		    		fileNameType = RenameFileNameType.valueOf(button.getText());
		    	}
		    });
		    button.addSelectionListener(previewListener);
		    fileNameTypeButtons[i++] = button;
	    }
	    
	    // pattern selection
	    VariableNameContentProposalProvider proposalProvider = new VariableNameContentProposalProvider(VARIABLE_COUNT);
	    proposalProvider.addToken(TARGET_TOKEN);
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		}
		catch (ParseException exc) {
			logger.warn("Key stroke cannot be created", exc);
		}

	    selectionPatternLabel = new Label(group, SWT.NONE);
	    selectionPatternLabel.setText("Selection Pattern:");
	    selectionPatternLabel.setEnabled(false);
	    selectionPatternText = new Text(group, SWT.SINGLE | SWT.BORDER);
	    selectionPatternText.setMessage(TARGET_TOKEN + " will be modified");
	    selectionPatternText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	    selectionPatternText.setEnabled(false);
	    selectionPatternText.addModifyListener(previewListener);
	    DecoratorUtils.createDecorator(selectionPatternText, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		ContentProposalAdapter sourceAdapter = new ContentProposalAdapter(selectionPatternText, new TextContentAdapter(), proposalProvider, keyStroke, null);
		sourceAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
//		DecoratorUtils.createDecorator(selectionPatternText, TARGET_TOKEN + " will be modified", FieldDecorationRegistry.DEC_INFORMATION);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.selectionPatternLabel.dispose();
		this.selectionPatternText.dispose();
		this.fileNameTypeLabel.dispose();
		for (Button button : fileNameTypeButtons) {
			button.dispose();
		}
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		selectionPatternLabel.setEnabled(selected);
		selectionPatternText.setEnabled(selected);
		fileNameTypeLabel.setEnabled(selected);
		for (Button button : fileNameTypeButtons) {
			button.setEnabled(selected);
		}
	}

	public void reset() {
		selectionPatternLabel.setEnabled(false);
		selectionPatternText.setEnabled(false);
		selectionPatternText.setText("");
		for (Button button : fileNameTypeButtons) {
	    	button.setSelection(RenameFileNameType.BASENAME.name().equals(button.getText()));
		    if (button.getSelection()) {
			    fileNameType = RenameFileNameType.valueOf(button.getText());
		    }
		}
	}

	@Override
	public IRenameRule[] getRenameRules() {
		List<IRenameRule> filters = new ArrayList<IRenameRule>();
		filters.add(new SelectingByFileNameTypeRenameRule(fileNameType));
		if (!selectionPatternText.getText().isEmpty() && !selectionPatternText.getText().equals(TARGET_TOKEN)) {
			filters.add(new SelectingByPatternRenameRule(variables, targetVariable, selectionPatternText.getText()));
		}
		return filters.toArray(new IRenameRule[0]);
	}

	@Override
	public String getDescription() {
		return "Allows to select a part of the file name for modifications";
	}

	@Override
	public String getTitle() {
		return "Selecting";
	}

}
