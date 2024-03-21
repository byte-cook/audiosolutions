package de.kobich.audiosolutions.frontend.audio.action;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
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
import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.ui.jface.ComboUtils;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.ComboSerializer;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.commons.utils.FileUtils;
import de.kobich.component.file.FileDescriptor;

/**
 * Modifiy import directory dialog.
 */
public class ModifyImportDirectoryDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final String TITLE = "Modify Import Directory";
	private static final String STATE_IMPORT_DIR = "importDirectory";
	private Set<FileDescriptor> fileDescriptors;
	private ListViewer selectedImportDirsViewer;
	private File importDirectory;
	private Combo importDirectoryCombo;

	public ModifyImportDirectoryDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * @param fileDescriptors the fileDescriptors to set
	 */
	public void setFileDescriptors(Set<FileDescriptor> fileDescriptors) {
		this.fileDescriptors = fileDescriptors;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// Set the title
		setTitle(TITLE);
		// Set the message
		setMessage("Changes the import directory of files.", IMessageProvider.INFORMATION);
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
		parentLayout.marginLeft = 3;
		parentLayout.marginRight = 3;
		parentLayout.numColumns = 2;
		Composite importDirComposite = new Composite(parent, SWT.NONE);
		importDirComposite.setLayout(parentLayout);
		importDirComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		importDirComposite.setFont(JFaceResources.getDialogFont());
		
		// selected files
		Label selectedImportDirLabel = new Label(importDirComposite, SWT.NONE);
		selectedImportDirLabel.setText("Selected Import Directories:");
		GridData selectedLabelLayoutData = new GridData();
		selectedLabelLayoutData.horizontalSpan = 2;
		selectedImportDirLabel.setLayoutData(selectedLabelLayoutData);
		Set<File> importDirs = new HashSet<File>();
		if (fileDescriptors != null) {
			for (FileDescriptor fileDescriptor : fileDescriptors) {
				importDirs.add(fileDescriptor.getImportDirectory());
			}
		}
		selectedImportDirsViewer = new ListViewer(importDirComposite);
		GridData listViewerLayoutData = new GridData(GridData.FILL_BOTH);
		listViewerLayoutData.horizontalSpan = 2;
		listViewerLayoutData.heightHint = 100;
		selectedImportDirsViewer.getList().setLayoutData(listViewerLayoutData);
		selectedImportDirsViewer.setContentProvider(new ArrayContentProvider());
		selectedImportDirsViewer.setLabelProvider(new ColumnLabelProvider());
		selectedImportDirsViewer.setInput(importDirs);

		// directory
		Label importDirLabel = new Label(importDirComposite, SWT.NONE);
		importDirLabel.setText("Import Directory:");
		GridData importDirLabelLayoutData = new GridData(SWT.NONE, SWT.NONE, false, false);
		importDirLabelLayoutData.horizontalSpan = 2;
		importDirLabel.setLayoutData(importDirLabelLayoutData);
		importDirectoryCombo = new Combo(importDirComposite, SWT.BORDER);
		GridData startDirectoryLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		importDirectoryCombo.setLayoutData(startDirectoryLayoutData);
		Button browseButton = new Button(importDirComposite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(ModifyImportDirectoryDialog.this.getShell());
				dirDialog.setText("Browse Directory");
				dirDialog.setFilterPath(FileUtils.getFirstExistingPath(importDirectoryCombo.getText()).orElse(new File("")).getAbsolutePath());
				// Open Dialog and save result of selection
				String dir = dirDialog.open();
				if (dir != null) {
					importDirectoryCombo.setText(dir);
				}
			}
		});
		
		if (!importDirs.isEmpty()) {
			try {
				String[] proposals = importDirs.stream().map(File::getAbsolutePath).toArray(String[]::new);
				SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(proposals);
				proposalProvider.setFiltering(true);
				KeyStroke keyStroke = KeyStroke.getInstance("Ctrl+Space");
				DecoratorUtils.createDecorator(importDirectoryCombo, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
				ContentProposalAdapter adapter = new ContentProposalAdapter(importDirectoryCombo, new ComboContentAdapter(), proposalProvider, keyStroke, null);
				adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			}
			catch (ParseException exc) {
			}

		}
		
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
		if (importDirectoryCombo.getText().isEmpty()) {
			setErrorMessage("Please define the import directory.");
			return;
		}
		importDirectory = new File(importDirectoryCombo.getText());
		if (!importDirectory.exists()) {
			setErrorMessage("The import directory does not exist.");
			return;
		}
		else if (!importDirectory.isDirectory()) {
			setErrorMessage("The import directory must be a directory.");
			return;
		}
		ComboUtils.addTextToCombo(importDirectoryCombo.getText(), importDirectoryCombo);
		
		saveState();
		super.okPressed();
	}

	/**
	 * @return the startDirectory
	 */
	public File getImportDirectory() {
		return importDirectory;
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, ModifyImportDirectoryDialog.class.getName());
	}

	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer importDirMemento = new ComboSerializer(STATE_IMPORT_DIR, "");
		importDirMemento.restore(importDirectoryCombo, mementoItem);
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer importDirMemento = new ComboSerializer(STATE_IMPORT_DIR, "");
		importDirMemento.save(importDirectoryCombo, mementoItem);
	}
}
