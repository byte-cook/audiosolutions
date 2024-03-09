package de.kobich.audiosolutions.frontend.audio.action;

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

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.descriptor.PersistedFileFilter;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
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
import lombok.Getter;

/**
 * Open audio collection editor dialog.
 */
public class OpenAudioCollectionEditorDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final String TITLE = "Open Audio Collection Editor";
	private static final String STATE_START_DIR = "startDirectory";
	private static final String STATE_ONLY_INCLUDE_NOT_IN_DB = "onlyIncludeNotInDB";
	@Getter
	private File startDirectory;
	private Combo startDirectoryCombo;
	@Getter
	private FileFilter fileFilter;
	private Button onlyIncludeFilesNotInDatabaseButton;

	public OpenAudioCollectionEditorDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// Set the title
		setTitle(TITLE);
		// Set the message
		setMessage("Import files to add/modify/delete audio data.", IMessageProvider.INFORMATION);
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
		startDirLabel.setLayoutData(JFaceUtils.createGridDataWithSpan(SWT.NONE, 2, 1));
		startDirectoryCombo = new Combo(startDirComposite, SWT.BORDER);
		startDirectoryCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button browseButton = new Button(startDirComposite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(OpenAudioCollectionEditorDialog.this.getShell());
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
		
		onlyIncludeFilesNotInDatabaseButton = new Button(startDirComposite, SWT.CHECK);
		onlyIncludeFilesNotInDatabaseButton.setText("Only include files that are not in the database");
		onlyIncludeFilesNotInDatabaseButton.setLayoutData(JFaceUtils.createGridDataWithSpan(SWT.NONE, 2, 1));

		JFaceUtils.createHorizontalSeparator(parent, 1);
		restoreState();
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
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
		
		if (onlyIncludeFilesNotInDatabaseButton.getSelection()) {
			AudioPersistenceService persistenceService = AudioSolutions.getService(AudioPersistenceService.class);
			this.fileFilter = new PersistedFileFilter(startDirectory, persistenceService).negate();
		}
		else {
			this.fileFilter = new DefaultFileFilter(null);
		}

		saveState();
		super.okPressed();
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, OpenAudioCollectionEditorDialog.class.getName());
	}
	
	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer startDirMemento = new ComboSerializer(STATE_START_DIR, "");
		startDirMemento.restore(startDirectoryCombo, mementoItem);
		startDirectoryCombo.setSelection(new Point(0, startDirectoryCombo.getText().length() + 1));
		String onlyIncludeNotDB = mementoItem.getString(STATE_ONLY_INCLUDE_NOT_IN_DB, Boolean.FALSE.toString());
		onlyIncludeFilesNotInDatabaseButton.setSelection(Boolean.parseBoolean(onlyIncludeNotDB));
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer startDirMemento = new ComboSerializer(STATE_START_DIR, "");
		startDirMemento.save(startDirectoryCombo, mementoItem);
		mementoItem.putString(STATE_ONLY_INCLUDE_NOT_IN_DB, "" + onlyIncludeFilesNotInDatabaseButton.getSelection());
	}
}
