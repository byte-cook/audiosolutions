package de.kobich.audiosolutions.frontend.audio.view.mediums.action;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.AudioDataContentProposalProvider;
import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.ui.jface.JFaceUtils;

/**
 * Dialog for lend a medium.
 */
public class LendMediumDialog extends TitleAreaDialog {
	private static final Logger logger = Logger.getLogger(LendMediumDialog.class);
	private String title;
	private String mediumName;
	private Text mediumNameText;
	private String borrower;
	private Text borrowerText;
	
	public static LendMediumDialog createDialog(Shell parentShell) {
		LendMediumDialog dialog = new LendMediumDialog(parentShell);
		dialog.title = "Lend Mediums";
		return dialog;
	}

	protected LendMediumDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// Set the title
		setTitle(title);
		// Set the message
		setMessage("Specify who borrows a medium.", IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button b = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			b.setEnabled(false);
		}
		return b;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		JFaceUtils.createHorizontalSeparator(parent, 1);
		
		Composite contentComposite = new Composite(parent, SWT.NONE);
		contentComposite.setLayout(JFaceUtils.createDialogGridLayout(3, false, JFaceUtils.MARGIN_HEIGHT, JFaceUtils.MARGIN_WIDTH));
		contentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentComposite.setFont(JFaceResources.getDialogFont());

		// medium
		Label mediumNameLabel = new Label(contentComposite, SWT.NONE);
		mediumNameLabel.setText("Medium:");
		mediumNameText = new Text(contentComposite, SWT.BORDER);
		if (mediumName != null) {
			mediumNameText.setText(mediumName);
		}
		GridData mediumNameLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		mediumNameText.setLayoutData(mediumNameLayoutData);
		mediumNameText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				getButton(IDialogConstants.OK_ID).setEnabled(!mediumNameText.getText().isEmpty());
			}
		});
		IContentProposalProvider proposalProvider = new AudioDataContentProposalProvider(AudioAttribute.MEDIUM);
		KeyStroke keyStroke = null; 
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
			DecoratorUtils.createDecorator(mediumNameText, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		}
		catch (ParseException exc) {
			logger.warn("Key stroke cannot be created", exc);
		}
		ContentProposalAdapter adapter = new ContentProposalAdapter(mediumNameText, new TextContentAdapter(), proposalProvider, keyStroke, null);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		// browse button
		Button browseMediums = new Button(contentComposite, SWT.NONE);
		browseMediums.setText("Browse...");
		browseMediums.addSelectionListener(new BrowseMediumSelectionListener());

		// borrower
		Label borrowerLabel = new Label(contentComposite, SWT.NONE);
		borrowerLabel.setText("Borrower:");
		borrowerText = new Text(contentComposite, SWT.BORDER);
		GridData borrowerLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		borrowerLayoutData.horizontalSpan = 2;
		borrowerText.setLayoutData(borrowerLayoutData);
		borrowerText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				getButton(IDialogConstants.OK_ID).setEnabled(!borrowerText.getText().isEmpty());
			}
		});
		
		JFaceUtils.createHorizontalSeparator(parent, 1);
		
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			if (mediumNameText.getText().isEmpty()) {
				setErrorMessage("Medium must be set.");
				return;
			}
			mediumName = mediumNameText.getText();
			if (borrowerText.getText().isEmpty()) {
				setErrorMessage("Borrower must be set.");
				return;
			}
			borrower = borrowerText.getText();
			setErrorMessage(null);
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * @return the borrower
	 */
	public String getBorrower() {
		return borrower;
	}

	/**
	 * @param mediumName
	 */
	public void setMediumName(String mediumName) {
		this.mediumName = mediumName;
	}
	
	/**
	 * @return the mediumName
	 */
	public String getMediumName() {
		return mediumName;
	}
	
	/**
	 * Browse medium listener
	 */
	private class BrowseMediumSelectionListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			AudioSearchService audioSearchService = AudioSolutions.getService(AudioSearchService.class);
			List<Medium> mediums = audioSearchService.searchMediums(null);
			Collections.sort(mediums, (m1, m2) -> m1.getName().compareTo(m2.getName()));

			ElementListSelectionDialog dialog = new ElementListSelectionDialog(LendMediumDialog.this.getShell(), new LabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof Medium) {
						return ((Medium) element).getName();
					}
					return null;
				}
			});
			dialog.setTitle("Browse Mediums");
			dialog.setElements(mediums.toArray(new Medium[0]));
			dialog.setMessage("Select a Medium (* = any string, ? = any char):");
			int status = dialog.open();
			if (status == IDialogConstants.OK_ID) {
				Object result = dialog.getFirstResult();
				if (result instanceof Medium medium) {
					mediumNameText.setText(medium.getName());
				}
			}
		}
	}
}
