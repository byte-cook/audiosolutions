package de.kobich.audiosolutions.frontend.file.view.rename.ui;

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
import de.kobich.audiosolutions.frontend.file.view.rename.model.RenameFileDescriptorAttributeType;
import de.kobich.commons.misc.rename.rule.AttributeRenameRule;
import de.kobich.commons.misc.rename.rule.IRenameRule;

public class RenameFilesAttributeTabView extends RenameFilesBaseTabView {
	private static final Logger logger = Logger.getLogger(RenameFilesAttributeTabView.class);
	private static final int VARIABLE_COUNT = 8;
	private Label patternLabel;
	private Text patternText;

	public RenameFilesAttributeTabView(RenameFilesPreviewListener previewListener) {
		super(previewListener);
	}

	@Override
	public void createMainTabControl(Composite composite) {
		GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
		Composite group = new Composite(composite, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(gridData);
		
	    AttributeContentProposalProvider proposalProvider = new AttributeContentProposalProvider(VARIABLE_COUNT);
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		}
		catch (ParseException exc) {
			logger.warn("Key stroke cannot be created", exc);
		}

		patternLabel = new Label(group, SWT.NONE);
		patternLabel.setText("Source Pattern:");
		patternLabel.setEnabled(false);
		patternText = new Text(group, SWT.SINGLE | SWT.BORDER);
		patternText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		patternText.setEnabled(false);
		patternText.addModifyListener(previewListener);
		DecoratorUtils.createDecorator(patternText, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		ContentProposalAdapter sourceAdapter = new ContentProposalAdapter(patternText, new TextContentAdapter(), proposalProvider, keyStroke, null);
		sourceAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.patternLabel.dispose();
		this.patternText.dispose();
	}
	
	@Override
	public void enabledChanged(boolean selected) {
		patternLabel.setEnabled(selected);
		patternText.setEnabled(selected);
	}

	public void reset() {
		patternText.setText("");
	}

	@Override
	public IRenameRule[] getRenameRules() {
		IRenameRule[] filters = new IRenameRule[1];
		filters[0] = new AttributeRenameRule(patternText.getText(), RenameFileDescriptorAttributeType.getNames());
		return filters;
	}
	
	@Override
	public String getDescription() {
		return "Uses file attributes to rename (<file-modified> -> 2014-03-12)";
	}

	@Override
	public String getTitle() {
		return "File Attribute";
	}
}
