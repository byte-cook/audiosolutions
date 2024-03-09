package de.kobich.audiosolutions.frontend.common.ui;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.commons.ui.jface.ComboUtils;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.ComboSerializer;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.commons.utils.FileUtils;

/**
 * Dialog to query a file.
 */
public class QueryFileDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final String STATE_START_DIR = "startDirectory";
	private String title;
	private String message;
	private String label;
	private boolean queryFile;
	private boolean requiredExists;
	private String[] filterExtensions;
	private String[] filterNames;
	private File file;
	private Combo fileCombo;
	
	/**
	 * Creates a dialog instance
	 * @param parentShell
	 * @param title
	 * @param message
	 * @return
	 */
	public static QueryFileDialog createFileDialog(Shell parentShell, String title, String message, String label, boolean queryFile, boolean requiredExists, String[] filterExtensions
			, String[] filterNames) {
		QueryFileDialog dialog = new QueryFileDialog(parentShell);
		dialog.title = title;
		dialog.message = message;
		dialog.label = label;
		dialog.queryFile = queryFile;
		dialog.requiredExists = requiredExists;
		dialog.filterExtensions = filterExtensions;
		dialog.filterNames = filterNames;
		return dialog;
	}

	private QueryFileDialog(Shell parentShell) {
		super(parentShell);
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
		parentLayout.numColumns = 2;
		Composite startDirComposite = new Composite(parent, SWT.NONE);
		startDirComposite.setLayout(parentLayout);
		startDirComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		startDirComposite.setFont(JFaceResources.getDialogFont());

		// directory
		Label dirLabel = new Label(startDirComposite, SWT.NONE);
		dirLabel.setText(label);
		GridData dirLabelLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		dirLabelLayoutData.horizontalSpan = 2;
		dirLabel.setLayoutData(dirLabelLayoutData);
		fileCombo = new Combo(startDirComposite, SWT.BORDER);
		GridData fileLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		fileCombo.setLayoutData(fileLayoutData);
		Button browseButton = new Button(startDirComposite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (queryFile) {
					FileDialog fileDialog = new FileDialog(QueryFileDialog.this.getShell());
					fileDialog.setText("Browse File");
					fileDialog.setFilterPath(FileUtils.getFirstExistingPath(fileCombo.getText()).orElse(new File("")).getAbsolutePath());
					fileDialog.setFilterExtensions(filterExtensions);
					fileDialog.setFilterNames(filterNames);
					
					// Open dialog and save result of selection
					String dir = fileDialog.open();
					if (dir != null) {
						fileCombo.setText(dir);
					}
				}
				else {
					DirectoryDialog dirDialog = new DirectoryDialog(QueryFileDialog.this.getShell());
					dirDialog.setText("Browse Directory");
					dirDialog.setFilterPath(fileCombo.getText());
					// Open Dialog and save result of selection
					String dir = dirDialog.open();
					if (dir != null) {
						fileCombo.setText(dir);
					}
				}
			}
		});
		
		JFaceUtils.createHorizontalSeparator(parent, 1);
		parent.pack();
		restoreState();
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		// directory
		if (fileCombo.getText().isEmpty()) {
			setErrorMessage("Please define the file/directory.");
			return;
		}
		file = new File(fileCombo.getText());
		if (requiredExists && !file.exists()) {
			setErrorMessage("The file/directory does not exist.");
			return;
		}
		ComboUtils.addTextToCombo(fileCombo.getText(), fileCombo);
		
		saveState();
		super.okPressed();
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, QueryFileDialog.class.getName());
	}
	
	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer startDirMemento = new ComboSerializer(STATE_START_DIR, "");
		startDirMemento.restore(fileCombo, mementoItem);
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer startDirMemento = new ComboSerializer(STATE_START_DIR, "");
		startDirMemento.save(fileCombo, mementoItem);
	}
}
