package de.kobich.audiosolutions.frontend.common.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.commons.misc.validate.rule.AutoNumberingRule;
import de.kobich.commons.misc.validate.rule.DigitRule;
import de.kobich.commons.misc.validate.rule.ForbiddenTextsRule;
import de.kobich.commons.misc.validate.rule.IValidationRule;
import de.kobich.commons.misc.validate.rule.LetterRule;
import de.kobich.commons.misc.validate.rule.LowerCaseRule;
import de.kobich.commons.misc.validate.rule.SentenceCaseRule;
import de.kobich.commons.misc.validate.rule.StartCaseRule;
import de.kobich.commons.misc.validate.rule.UpperCaseRule;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;

/**
 * Validate Audio Data Dialog.
 */
public class ValidationDialog extends TitleAreaDialog {
	public static final String NO_VALIDATION = "<no-validation>";
	public static final String SEPARATOR = ",";
	private String title;
	private String message;
	private List<IValidationRule> rules;
	private Button ignoreCaseButton;
	private Button lowerCaseButton;
	private Button upperCaseButton;
	private Button startCaseButton;
	private Button sentenceCaseButton;
	private Button ignoreCharButton;
	private Button digitCharButton;
	private Button autoNumberingCharButton;
	private Button letterCharButton;
	private Text forbiddenText;
	
	/**
	 * Creates the validation dialog
	 * @param parentShell
	 * @return
	 */
	public static ValidationDialog createDialog(Shell parentShell) {
		ValidationDialog dialog = new ValidationDialog(parentShell, "Validation", "Define validation settings.");
		return dialog;
	}

	/**
	 * Constructor
	 * @param parentShell
	 * @param title
	 * @param message
	 */
	private ValidationDialog(Shell parentShell, String title, String message) {
		super(parentShell);
		this.title = title;
		this.message = message;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// Set the title
		setTitle(title);
		// Set the message
		setMessage(message, IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		JFaceUtils.createHorizontalSeparator(parent, 1);
		
		GridLayout parentLayout = new GridLayout();
		parentLayout.marginLeft = 3;
		parentLayout.marginRight = 3;
		parentLayout.numColumns = 1;
		Composite contentComposite = new Composite(parent, SWT.NONE);
		contentComposite.setLayout(parentLayout);
		contentComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		contentComposite.setFont(JFaceResources.getDialogFont());

		// case
		Group caseGroup = new Group(contentComposite, SWT.SHADOW_IN);
	    caseGroup.setText("Case");
	    caseGroup.setLayout(new GridLayout(5, false));
	    caseGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	    ignoreCaseButton = new Button(caseGroup, SWT.RADIO);
	    ignoreCaseButton.setText("Ignore");
	    lowerCaseButton = new Button(caseGroup, SWT.RADIO);
	    lowerCaseButton.setText("lower case");
	    upperCaseButton = new Button(caseGroup, SWT.RADIO);
	    upperCaseButton.setText("UPPER CASE");
	    startCaseButton = new Button(caseGroup, SWT.RADIO);
	    startCaseButton.setText("Start Case");
	    sentenceCaseButton = new Button(caseGroup, SWT.RADIO);
	    sentenceCaseButton.setText("Sentence case");
	    ignoreCaseButton.setSelection(true);

		// characters
		Group charGroup = new Group(contentComposite, SWT.SHADOW_IN);
	    charGroup.setText("Allowed Characters");
	    charGroup.setLayout(new GridLayout(5, false));
	    charGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	    ignoreCharButton = new Button(charGroup, SWT.RADIO);
	    ignoreCharButton.setText("Ignore");
	    digitCharButton = new Button(charGroup, SWT.RADIO);
	    digitCharButton.setText("Digits (0-9)");
	    autoNumberingCharButton = new Button(charGroup, SWT.RADIO);
	    autoNumberingCharButton.setText("Consecutively Numbering");
	    letterCharButton = new Button(charGroup, SWT.RADIO);
	    letterCharButton.setText("Letters (A-Z a-z)");
	    ignoreCharButton.setSelection(true);

		// forbidden
		Group forbiddenGroup = new Group(contentComposite, SWT.SHADOW_IN);
	    forbiddenGroup.setText("Forbidden");
	    forbiddenGroup.setLayout(new GridLayout());
	    forbiddenGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		Label forbiddenLabel = new Label(forbiddenGroup, SWT.NONE);
		forbiddenLabel.setText("Forbidden Texts (comma-separated):");
		forbiddenText = new Text(forbiddenGroup, SWT.SINGLE | SWT.BORDER);
		forbiddenText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		updateRules();

		JFaceUtils.createHorizontalSeparator(parent, 2);
//		restoreState();
		return parent;
	}
	
	/**
	 * Updates selected by current validation rules
	 */
	private void updateRules() {
		if (rules == null) {
			return;
		}
		for (IValidationRule rule : rules) {
			if (rule instanceof LowerCaseRule) {
				ignoreCaseButton.setSelection(false);
				lowerCaseButton.setSelection(true);
			}
			else if (rule instanceof UpperCaseRule) {
				ignoreCaseButton.setSelection(false);
				upperCaseButton.setSelection(true);
			}
			else if (rule instanceof StartCaseRule) {
				ignoreCaseButton.setSelection(false);
				startCaseButton.setSelection(true);
			}
			else if (rule instanceof SentenceCaseRule) {
				ignoreCaseButton.setSelection(false);
				sentenceCaseButton.setSelection(true);
			}
			else if (rule instanceof DigitRule) {
				ignoreCharButton.setSelection(false);
				autoNumberingCharButton.setSelection(false);
				digitCharButton.setSelection(true);
			}
			else if (rule instanceof LetterRule) {
				ignoreCharButton.setSelection(false);
				autoNumberingCharButton.setSelection(false);
				letterCharButton.setSelection(true);
			}
			else if (rule instanceof AutoNumberingRule) {
				ignoreCharButton.setSelection(false);
				autoNumberingCharButton.setSelection(true);
				letterCharButton.setSelection(false);
			}
			else if (rule instanceof ForbiddenTextsRule) {
				ForbiddenTextsRule r = (ForbiddenTextsRule) rule;
				String[] texts = r.getForbiddenTexts();
				String text = "";
				for (String t : texts) {
					if (StringUtils.isNotEmpty(text)) {
						text += SEPARATOR;
					}
					text += t;
				}
				forbiddenText.setText(text);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		rules = new ArrayList<IValidationRule>();
		// case
		if (lowerCaseButton.getSelection()) {
			rules.add(new LowerCaseRule());
		}
		else if (upperCaseButton.getSelection()) {
			rules.add(new UpperCaseRule());
		}
		else if (startCaseButton.getSelection()) {
			rules.add(new StartCaseRule());
		}
		else if (sentenceCaseButton.getSelection()) {
			rules.add(new SentenceCaseRule());
		}
		// characters
		if (digitCharButton.getSelection()) {
			rules.add(new DigitRule());
		}
		else if (letterCharButton.getSelection()) {
			rules.add(new LetterRule());
		}
		else if (autoNumberingCharButton.getSelection()) {
			rules.add(new AutoNumberingRule(1, 1));
		}
		// forbidden
		if (StringUtils.isNotEmpty(forbiddenText.getText())) {
			String[] texts = forbiddenText.getText().split(SEPARATOR);
			rules.add(new ForbiddenTextsRule(texts));
		}
		
//		saveState();
		super.okPressed();
	}

	/**
	 * @return validation rules
	 */
	public List<IValidationRule> getRules() {
		return rules;
	}

	/**
	 * @param rules the rules to set
	 */
	public void setRules(List<IValidationRule> rules) {
		this.rules = rules;
	}
	
	/**
	 * Returns the rules label
	 * @return
	 */
	public String getRulesLabel() {
		String label = null;
		if (rules.isEmpty()) {
			label = NO_VALIDATION;
		}
		else {
			label = "";
			for (IValidationRule rule : rules) {
				if (StringUtils.isNotEmpty(label)) {
					label += ", ";
				}
				label += rule.getName();
			}
		}
		return label;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, ValidationDialog.class.getName());
	}
}
