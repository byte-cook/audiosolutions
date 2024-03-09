package de.kobich.audiosolutions.frontend.file.view.browse.action;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.utils.FileUtils;

/**
 * File browser dialog.
 */
public class FileBrowserDialog extends TitleAreaDialog {
	private String title;
	private String message;
	private File source;
	private Text sourceText;
	private File targetDirectory;
	private Text targetDirectoryText;
	
	public static FileBrowserDialog createCopyDialg(Shell parentShell) {
		FileBrowserDialog dialog = new FileBrowserDialog(parentShell);
		dialog.title = "Copy Files/Folders";
		dialog.message = "Copies a file or directory to a given target directory.";
		return dialog;
	}
	
	public static FileBrowserDialog createMoveDialg(Shell parentShell) {
		FileBrowserDialog dialog = new FileBrowserDialog(parentShell);
		dialog.title = "Move Files/Folders";
		dialog.message = "Moves a file or directory to a given target directory.";
		return dialog;
	}

	/**
	 * Constructor
	 * @param parentShell
	 */
	private FileBrowserDialog(Shell parentShell) {
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
		startDirComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		startDirComposite.setFont(JFaceResources.getDialogFont());

		// source directory
		Label sourceLabel = new Label(startDirComposite, SWT.NONE);
		if (source.isDirectory()) {
			sourceLabel.setText("Source Directory:");
		}
		else if (source.isFile()) {
			sourceLabel.setText("Source File:");
		}
		GridData sourceLabelLayoutData = new GridData();
		sourceLabelLayoutData.horizontalSpan = 2;
		sourceLabel.setLayoutData(sourceLabelLayoutData);
		sourceText = new Text(startDirComposite, SWT.BORDER);
		sourceText.setEditable(false);
		GridData sourceLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		sourceLayoutData.horizontalSpan = 2;
		sourceText.setLayoutData(sourceLayoutData);
		sourceText.setText(source.getAbsolutePath());

		// target directory
		Label targetDirLabel = new Label(startDirComposite, SWT.NONE);
		targetDirLabel.setText("Target Directory:");
		GridData startDirLabelLayoutData = new GridData();
		startDirLabelLayoutData.horizontalSpan = 2;
		targetDirLabel.setLayoutData(startDirLabelLayoutData);
		targetDirectoryText = new Text(startDirComposite, SWT.BORDER);
		GridData startDirectoryLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		targetDirectoryText.setLayoutData(startDirectoryLayoutData);
		Button browseButton = new Button(startDirComposite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(FileBrowserDialog.this.getShell());
				dirDialog.setText("Browse Directory");
				dirDialog.setFilterPath(FileUtils.getFirstExistingPath(targetDirectoryText.getText()).orElse(new File("")).getAbsolutePath());
				// Open Dialog and save result of selection
				String dir = dirDialog.open();
				if (dir != null) {
					targetDirectoryText.setText(dir);
				}
			}
		});
		if (targetDirectory != null) {
			targetDirectoryText.setText(targetDirectory.getAbsolutePath());
		}
		
		JFaceUtils.createHorizontalSeparator(parent, 1);
//		restoreState();

		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if (targetDirectoryText.getText().isEmpty()) {
			setErrorMessage("Please define the target directory.");
			return;
		}
		targetDirectory = new File(targetDirectoryText.getText());
		if (!targetDirectory.exists()) {
			setErrorMessage("The target directory does not exist.");
			return;
		}
		if (!targetDirectory.isDirectory()) {
			setErrorMessage("The target directory is no directory.");
			return;
		}
		
		File sourceDirectory = source.getParentFile();
		if (sourceDirectory.equals(targetDirectory)) {
			setErrorMessage("The target directory is equal to source directory.");
			return;
		}
		
//		saveState();
		super.okPressed();
	}

	/**
	 * @return the targetDirectory
	 */
	public File getTargetDirectory() {
		return targetDirectory;
	}

	/**
	 * @param targetDirectory the targetDirectory to set
	 */
	public void setTargetDirectory(File targetDirectory) {
		if (targetDirectory.isFile()) {
			this.targetDirectory = targetDirectory.getParentFile();
		}
		else {
			this.targetDirectory = targetDirectory;
		}
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(File source) {
		this.source = source;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, FileBrowserDialog.class.getName());
	}
}
