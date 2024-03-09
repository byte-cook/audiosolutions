package de.kobich.audiosolutions.frontend.file.action;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.ui.DefaultFileFilter;
import de.kobich.commons.ui.jface.ComboUtils;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.ComboSerializer;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.commons.utils.FileUtils;

/**
 * Open file collection editor dialog.
 */
public class OpenFileCollectionEditorDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final String TITLE = "Open Files Collection Editor";
	private static final String STATE_START_DIR = "startDirectory";
	private File startDirectory;
	private Combo startDirectoryCombo;

	public OpenFileCollectionEditorDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// Set the title
		setTitle(TITLE);
		// Set the message
		setMessage("Imports files to a new collection.", IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(TITLE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		JFaceUtils.createHorizontalSeparator(parent, 1);

		GridLayout parentLayout = new GridLayout();
		parentLayout.numColumns = 2;
		Composite startDirComposite = new Composite(parent, SWT.NONE);
		startDirComposite.setLayout(parentLayout);
		startDirComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		startDirComposite.setFont(JFaceResources.getDialogFont());

		// directory
		Label startDirLabel = new Label(startDirComposite, SWT.NONE);
		startDirLabel.setText("Import Directory:");
		GridData startDirLabelLayoutData = new GridData();
		startDirLabelLayoutData.horizontalSpan = 2;
		startDirLabel.setLayoutData(startDirLabelLayoutData);
		startDirectoryCombo = new Combo(startDirComposite, SWT.BORDER);
		GridData startDirectoryLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		startDirectoryCombo.setLayoutData(startDirectoryLayoutData);
		Button browseButton = new Button(startDirComposite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(OpenFileCollectionEditorDialog.this.getShell());
				dirDialog.setText("Browse Directory");
				dirDialog.setFilterPath(FileUtils.getFirstExistingPath(startDirectoryCombo.getText()).orElse(new File("")).getAbsolutePath());
				// Open Dialog and save result of selection
				String dir = dirDialog.open();
				if (dir != null) {
					startDirectoryCombo.setText(dir);
				}
			}
		});
		startDirectoryCombo.setFocus();
		
		JFaceUtils.createHorizontalSeparator(parent, 1);
		restoreState();
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		// directory
		if (startDirectoryCombo.getText().isEmpty()) {
			setErrorMessage("Please define the import directory.");
			return;
		}
		startDirectory = new File(startDirectoryCombo.getText());
		if (!startDirectory.exists()) {
			setErrorMessage("The import directory does not exist.");
			return;
		}
		ComboUtils.addTextToCombo(startDirectoryCombo.getText(), startDirectoryCombo);
		
		saveState();
		super.okPressed();
	}

	/**
	 * @return the startDirectory
	 */
	public File getStartDirectory() {
		return startDirectory;
	}

	/**
	 * @return the fileFilter
	 */
	public FileFilter getFileFilter() {
		return new DefaultFileFilter(null);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, OpenFileCollectionEditorDialog.class.getName());
	}
	
	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer startDirMemento = new ComboSerializer(STATE_START_DIR, "");
		startDirMemento.restore(startDirectoryCombo, mementoItem);
		startDirectoryCombo.setSelection(new Point(0,startDirectoryCombo.getText().length() + 1));
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer startDirMemento = new ComboSerializer(STATE_START_DIR, "");
		startDirMemento.save(startDirectoryCombo, mementoItem);
	}
}
