package de.kobich.audiosolutions.frontend.audio.action;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
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
 * Convert files dialog.
 */
public class ConvertFilesDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final String STATE_TARGET_DIR = "targetDir";
	private static final String USE_SOURCE_DIR = "useSourceDir";
	private String title;
	private File targetDirectoryFile;
	private boolean useSourceDir;
	private Label targetDirectoryLabel;
	private Combo targetDirectoryCombo;
	private Button targetBrowseButton;
	private Button useSouceDirButton;
	private AudioFormat outputFormat;
	private IConvertFilesOptionPanel optionPanel;
	private CommandLineTool tool;

	/**
	 * Create convert dialog
	 * @param parentShell
	 * @param encoderType
	 * @return
	 */
	public static ConvertFilesDialog createConvertFilesDialog(Shell parentShell, Set<FileDescriptor> fileDescriptors, AudioFormat outputFormat, CommandLineTool tool, IConvertFilesOptionPanel optionPanel) {
		// only lame encoder supported
		String title = "Convert Files";
		ConvertFilesDialog dialog = new ConvertFilesDialog(parentShell, title);
		dialog.outputFormat = outputFormat;
		dialog.optionPanel = optionPanel;
		dialog.tool = tool;
		return dialog;
	}

	/**
	 * Constructor
	 * @param parentShell
	 * @param title
	 * @param message
	 */
	private ConvertFilesDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title = title;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// Set the title
		setTitle(title);
		// Set the message
		if (tool != null) {
			setMessage("Convert audio files to the target directory using " + tool.getLabel() + " encoding engine.", IMessageProvider.INFORMATION);
		}
		else {
			setMessage("No suitable encoder found", IMessageProvider.ERROR);
		}
		return contents;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		if (tool == null) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		return control;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		JFaceUtils.createHorizontalSeparator(parent, 1);
		
		Composite contentComposite = new Composite(parent, SWT.NONE);
		contentComposite.setLayout(JFaceUtils.createDialogGridLayout(1, false, JFaceUtils.MARGIN_HEIGHT, JFaceUtils.MARGIN_WIDTH));
		contentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentComposite.setFont(JFaceResources.getDialogFont());

		// general composite
		Composite generalComposite = new Composite(contentComposite, SWT.NONE);
		generalComposite.setLayout(JFaceUtils.createDialogGridLayout(2, false));
		generalComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		generalComposite.setFont(JFaceResources.getDialogFont());

		// target directory
		this.targetDirectoryLabel = new Label(generalComposite, SWT.NONE);
		targetDirectoryLabel.setText("Target Directory:");
		GridData targetDirectoryLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		targetDirectoryLayoutData.horizontalSpan = 2;
		targetDirectoryLabel.setLayoutData(targetDirectoryLayoutData);
		targetDirectoryCombo = new Combo(generalComposite, SWT.BORDER);
		GridData targetDirectoryComboLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		targetDirectoryCombo.setLayoutData(targetDirectoryComboLayoutData);
		this.targetBrowseButton = new Button(generalComposite, SWT.PUSH);
		targetBrowseButton.setText("Browse...");
		targetBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(ConvertFilesDialog.this.getShell());
				dirDialog.setText("Browse Directory");
				dirDialog.setFilterPath(FileUtils.getFirstExistingPath(targetDirectoryCombo.getText()).orElse(new File("")).getAbsolutePath());
				String dir = dirDialog.open();
				if (dir != null) {
					targetDirectoryCombo.setText(dir);
				}
			}
		});
		useSouceDirButton = new Button(generalComposite, SWT.CHECK);
		useSouceDirButton.setText("Use Source Directory");
		useSouceDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fireUseSourceDir();
			}
		});

		// option composite
		if (optionPanel != null) {
			Composite optionComposite = new Composite(contentComposite, SWT.NONE);
			optionComposite.setLayout(JFaceUtils.createDialogGridLayout(1, false));
			optionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			optionComposite.setFont(JFaceResources.getDialogFont());
			JFaceUtils.createHorizontalSeparator(optionComposite, 1);
			
			optionPanel.createOptionComposite(optionComposite);
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
		useSourceDir = useSouceDirButton.getSelection();
		
		// target directory
		if (!useSourceDir && targetDirectoryCombo.getText().isEmpty()) {
			setErrorMessage("Please define the target directory.");
			return;
		}
		targetDirectoryFile = new File(targetDirectoryCombo.getText());
		ComboUtils.addTextToCombo(targetDirectoryCombo.getText(), targetDirectoryCombo);

		if (optionPanel != null) {
			optionPanel.okPressed();
		}
		
		saveState();
		super.okPressed();
	}

	private void fireUseSourceDir() {
		boolean enabled = !useSouceDirButton.getSelection();
		targetDirectoryLabel.setEnabled(enabled);
		targetDirectoryCombo.setEnabled(enabled);
		targetBrowseButton.setEnabled(enabled);
	}

	/**
	 * @return the targetDirectory
	 */
	public File getTargetDirectoryFile() {
		return targetDirectoryFile;
	}

	/**
	 * @return the useSourceDir
	 */
	public boolean isUseSourceDir() {
		return useSourceDir;
	}
	
	public Optional<IConvertFilesOptionPanel> getOptionPanel() {
		return Optional.ofNullable(optionPanel);
	}
	
	public <T extends IConvertFilesOptionPanel> Optional<T> getOptionPanel(Class<T> clazz) {
		if (optionPanel == null) {
			return Optional.empty();
		}
		if (clazz.isAssignableFrom(optionPanel.getClass())) {
			return Optional.of(clazz.cast(optionPanel));
		}
		return Optional.empty();
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, ConvertFilesDialog.class.getName() + outputFormat);
	}

	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer targetDirMemento = new ComboSerializer(STATE_TARGET_DIR, "");
		targetDirMemento.restore(targetDirectoryCombo, mementoItem);
		String useSourceDir = mementoItem.getString(USE_SOURCE_DIR, Boolean.TRUE.toString());
		boolean useSource = Boolean.parseBoolean(useSourceDir);
		useSouceDirButton.setSelection(useSource);
		fireUseSourceDir();
	
		if (optionPanel != null) {
			optionPanel.restoreState(mementoItem);
		}
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer targetDirMemento = new ComboSerializer(STATE_TARGET_DIR, "");
		targetDirMemento.save(targetDirectoryCombo, mementoItem);
		mementoItem.putString(USE_SOURCE_DIR, "" + useSouceDirButton.getSelection());
		
		if (optionPanel != null) {
			optionPanel.saveState(mementoItem);
		}
	}
}
