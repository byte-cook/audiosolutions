package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.misc.rename.rule.SwappingRenameRule;

public class RenameFilesSwappingTabView extends RenameFilesBaseTabView {
	private static final Logger logger = Logger.getLogger(RenameFilesSwappingTabView.class);
	private static final int VARIABLE_COUNT = 8;
	private List<StructureVariable> variables;
	private Label sourcePatternLabel;
	private Text sourcePatternText;
	private Label targetPatternLabel;
	private Text targetPatternText;

	public RenameFilesSwappingTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
		this.variables = new ArrayList<StructureVariable>();
		for (int i = 1; i <= VARIABLE_COUNT; ++ i) {
			String variableName = "<" + i + ">";
			variables.add(new StructureVariable(variableName));
		}
	}

	@Override
	public void createMainTabControl(Composite composite) {
		Composite group = new Composite(composite, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
	    VariableNameContentProposalProvider proposalProvider = new VariableNameContentProposalProvider(VARIABLE_COUNT);
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		}
		catch (ParseException exc) {
			logger.warn("Key stroke cannot be created", exc);
		}

		sourcePatternLabel = new Label(group, SWT.NONE);
		sourcePatternLabel.setText("Source Pattern:");
		sourcePatternLabel.setEnabled(false);
		sourcePatternText = new Text(group, SWT.SINGLE | SWT.BORDER);
		sourcePatternText.setMessage(proposalProvider.getToken(0) + "-" + proposalProvider.getToken(1));
		sourcePatternText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		sourcePatternText.setEnabled(false);
		sourcePatternText.addModifyListener(previewListener);
		DecoratorUtils.createDecorator(sourcePatternText, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		ContentProposalAdapter sourceAdapter = new ContentProposalAdapter(sourcePatternText, new TextContentAdapter(), proposalProvider, keyStroke, null);
		sourceAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
//		DecoratorUtils.createDecorator(sourcePatternText, "Example: " + proposalProvider.getToken(0) + "-" + proposalProvider.getToken(1), FieldDecorationRegistry.DEC_INFORMATION);
		
		targetPatternLabel = new Label(group, SWT.NONE);
		targetPatternLabel.setText("Target Pattern:");
		targetPatternLabel.setEnabled(false);
		targetPatternText = new Text(group, SWT.SINGLE | SWT.BORDER);
		targetPatternText.setMessage(proposalProvider.getToken(1) + "-" + proposalProvider.getToken(0));
		targetPatternText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		targetPatternText.setEnabled(false);
		targetPatternText.addModifyListener(previewListener);
		DecoratorUtils.createDecorator(targetPatternText, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		ContentProposalAdapter targetAdapter = new ContentProposalAdapter(targetPatternText, new TextContentAdapter(), proposalProvider, keyStroke, null);
		targetAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
//		DecoratorUtils.createDecorator(targetPatternText, "Example: " + proposalProvider.getToken(1) + "-" + proposalProvider.getToken(0), FieldDecorationRegistry.DEC_INFORMATION);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.sourcePatternLabel.dispose();
		this.sourcePatternText.dispose();
		this.targetPatternLabel.dispose();
		this.targetPatternText.dispose();
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		sourcePatternLabel.setEnabled(selected);
		sourcePatternText.setEnabled(selected);
		targetPatternLabel.setEnabled(selected);
		targetPatternText.setEnabled(selected);
	}

	public void reset() {
		sourcePatternText.setText("");
		targetPatternText.setText("");
	}

	@Override
	public IRenameRule[] getRenameRules() {
		IRenameRule[] filters = new IRenameRule[1];
		filters[0] = new SwappingRenameRule(variables, sourcePatternText.getText(), targetPatternText.getText());
		return filters;
	}
	
	@Override
	public String getDescription() {
		return "Swaps text (one-two -> two-one)";
	}

	@Override
	public String getTitle() {
		return "Swapping";
	}
}
