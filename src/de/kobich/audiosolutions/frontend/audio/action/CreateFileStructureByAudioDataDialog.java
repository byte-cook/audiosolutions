package de.kobich.audiosolutions.frontend.audio.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttribute2StructureVariableMapper;
import de.kobich.audiosolutions.core.service.io.AudioIOService;
import de.kobich.audiosolutions.core.service.io.GetFilePathByAudioDataRequest;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.AudioStructureVariableContentProposalProvider;
import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.ui.jface.ComboUtils;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.ComboSerializer;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.commons.utils.FileUtils;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.io.FileCreationType;

/**
 * Creates file structure by audio data dialog.
 */
public class CreateFileStructureByAudioDataDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final Logger logger = Logger.getLogger(CreateFileStructureByAudioDataDialog.class);
	private static final String STATE_TARGET_DIR = "targetDir";
	private static final String STATE_FILE_PATTERN = "filePattern";
	private static final String STATE_CREATION_TYPE = "creationType";
	private String title;
	private String message;
	private FileDescriptor previewFileDescriptor;
	private AudioAttribute2StructureVariableMapper mapper;
	private File targetDirectory;
	private Combo targetDirectoryCombo;
	private String filePattern;
	private Combo filePatternCombo;
	private Text sourceFileText;
	private Text targetFileText;
	private Button moveCreationTypeButton;
	private Button copyCreationTypeButton;
	private FileCreationType creationType;

	/**
	 * Creates the dialog
	 * @param parentShell
	 * @return
	 */
	public static CreateFileStructureByAudioDataDialog createDialog(Shell parentShell) {
		CreateFileStructureByAudioDataDialog dialog = new CreateFileStructureByAudioDataDialog(parentShell, "Create File Structure", "Create file structure by audio data.");
		return dialog;
	}

	/**
	 * Constructor
	 * @param parentShell
	 * @param title
	 * @param message
	 */
	private CreateFileStructureByAudioDataDialog(Shell parentShell, String title, String message) {
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
		
		Composite contentComposite = new Composite(parent, SWT.NONE);
		GridLayout parentLayout = new GridLayout();
		parentLayout.marginLeft = 3;
		parentLayout.marginRight = 3;
		parentLayout.numColumns = 2;
		contentComposite.setLayout(parentLayout);
		// workaround to show complete combo text: new GridData(SWT.FILL, SWT.NONE, true, false)
		GridData contentGridData = new GridData(GridData.FILL_BOTH);
		contentComposite.setLayoutData(contentGridData);
		contentComposite.setFont(JFaceResources.getDialogFont());
		
		// target directory
		Label targetDirLabel = new Label(contentComposite, SWT.NONE);
		targetDirLabel.setText("Target Directory:");
		GridData startDirLabelLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		startDirLabelLayoutData.horizontalSpan = 2;
		targetDirLabel.setLayoutData(startDirLabelLayoutData);
		targetDirectoryCombo = new Combo(contentComposite, SWT.BORDER);
		GridData startDirectoryLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		startDirectoryLayoutData.horizontalSpan = 1;
		targetDirectoryCombo.setLayoutData(startDirectoryLayoutData);
		targetDirectoryCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				updatePreview();
			}
		});
		Button browseButton = new Button(contentComposite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(CreateFileStructureByAudioDataDialog.this.getShell());
				dirDialog.setText("Browse Directory");
				dirDialog.setFilterPath(FileUtils.getFirstExistingPath(targetDirectoryCombo.getText()).orElse(new File("")).getAbsolutePath());
				// Open Dialog and save result of selection
				String dir = dirDialog.open();
				if (dir != null) {
					targetDirectoryCombo.setText(dir);
				}
			}
		});
		// creation type
		Label creationTypeLabel = new Label(contentComposite, SWT.NONE);
		creationTypeLabel.setText("Creation Type:");
		GridData creationTypeLabelLayoutData = new GridData();
		creationTypeLabelLayoutData.horizontalSpan = 2;
		creationTypeLabel.setLayoutData(creationTypeLabelLayoutData);
		Composite typeComposite = new Composite(contentComposite, SWT.NULL);
	    typeComposite.setLayout(new RowLayout());
	    copyCreationTypeButton = new Button(typeComposite, SWT.RADIO);
	    copyCreationTypeButton.setText("Copy");
	    moveCreationTypeButton = new Button(typeComposite, SWT.RADIO);
	    moveCreationTypeButton.setText("Move");

		// file pattern
		Label filePatternLabel = new Label(contentComposite, SWT.NONE);
		filePatternLabel.setText("File Pattern:");
		GridData filePatternLabelLayoutData = new GridData();
		filePatternLabelLayoutData.horizontalSpan = 2;
		filePatternLabel.setLayoutData(filePatternLabelLayoutData);
		filePatternCombo = new Combo(contentComposite, SWT.BORDER);
		GridData filePatternLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		filePatternLayoutData.horizontalSpan = 2;
		filePatternCombo.setLayoutData(filePatternLayoutData);
		filePatternCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				updatePreview();
			}
		});
		IContentProposalProvider proposalProvider = new AudioStructureVariableContentProposalProvider();
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
			DecoratorUtils.createDecorator(filePatternCombo, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		}
		catch (ParseException exc) {
			logger.warn("Key stroke cannot be created", exc);
		}
		ContentProposalAdapter adapter = new ContentProposalAdapter(filePatternCombo, new ComboContentAdapter(), proposalProvider, keyStroke, null);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);

		// preview group
		Group previewGroup = new Group(contentComposite, SWT.NONE);
		previewGroup.setText("Preview");
		GridLayout generalLayout = new GridLayout();
		generalLayout.marginLeft = 3;
		generalLayout.marginRight = 3;
		generalLayout.numColumns = 2;
		previewGroup.setLayout(generalLayout);
		GridData generalLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		generalLayoutData.horizontalSpan = 2;
		generalLayoutData.widthHint = 500;
		previewGroup.setLayoutData(generalLayoutData);
		previewGroup.setFont(JFaceResources.getDialogFont());
		// source file
		Label sourceFileLabel = new Label(previewGroup, SWT.NONE);
		sourceFileLabel.setText("Source File:");
		sourceFileText = new Text(previewGroup, SWT.BORDER | SWT.READ_ONLY);
		sourceFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sourceFileText.setText(previewFileDescriptor.getFile().getAbsolutePath());
		sourceFileText.setBackground(JFaceUtils.getDisabledTextBackgroundColor());
		// target file
		Label targetFileLabel = new Label(previewGroup, SWT.NONE);
		targetFileLabel.setText("Target File:");
		targetFileText = new Text(previewGroup, SWT.BORDER | SWT.READ_ONLY);
		targetFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		targetFileText.setBackground(JFaceUtils.getDisabledTextBackgroundColor());

		JFaceUtils.createHorizontalSeparator(parent, 1);
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		// target directory
		if (targetDirectoryCombo.getText().isEmpty()) {
			setErrorMessage("Please define the target directory.");
			return;
		}
		targetDirectory = new File(targetDirectoryCombo.getText());
		if (!targetDirectory.exists()) {
			setErrorMessage("The target directory does not exist.");
			return;
		}
		ComboUtils.addTextToCombo(targetDirectoryCombo.getText(), targetDirectoryCombo);
		
		// creation type
		creationType = FileCreationType.COPY;
		if (moveCreationTypeButton.getSelection()) {
			creationType = FileCreationType.MOVE;
		}
		
		// file pattern
		if (filePatternCombo.getText().isEmpty()) {
			setErrorMessage("Please define the file pattern.");
			return;
		}
		filePattern = filePatternCombo.getText();
		ComboUtils.addTextToCombo(filePatternCombo.getText(), filePatternCombo);
		
		saveState();
		super.okPressed();
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		restoreState();
		return control;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, CreateFileStructureByAudioDataDialog.class.getName());
	}
	
	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer targetDirMemento = new ComboSerializer(STATE_TARGET_DIR, "");
		targetDirMemento.restore(targetDirectoryCombo, mementoItem);
		String creationType = mementoItem.getString(STATE_CREATION_TYPE, FileCreationType.COPY.name());
		if (FileCreationType.COPY.name().equals(creationType)) {
			copyCreationTypeButton.setSelection(true);
		}
		else if (FileCreationType.MOVE.name().equals(creationType)) {
			moveCreationTypeButton.setSelection(true);
		}
		ComboSerializer filePatternMemento = new ComboSerializer(STATE_FILE_PATTERN, "");
		filePatternMemento.restore(filePatternCombo, mementoItem);
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer targetDirMemento = new ComboSerializer(STATE_TARGET_DIR, "");
		targetDirMemento.save(targetDirectoryCombo, mementoItem);
		mementoItem.putString(STATE_CREATION_TYPE, creationType.name());
		ComboSerializer filePatternMemento = new ComboSerializer(STATE_FILE_PATTERN, "");
		filePatternMemento.save(filePatternCombo, mementoItem);
	}
	
	/**
	 * Updates the preview
	 */
	private void updatePreview() {
		AudioIOService audioIOService = AudioSolutions.getService(AudioIOService.class);
		File rootDirectory = new File(targetDirectoryCombo.getText());
		String filePattern = filePatternCombo.getText();
		Map<StructureVariable, AudioAttribute> variableMap = mapper.getMap();
		GetFilePathByAudioDataRequest request = new GetFilePathByAudioDataRequest(previewFileDescriptor, rootDirectory, filePattern, variableMap);
		File path = audioIOService.getFilePathByAudioData(request);
		if (path != null) {
			targetFileText.setText(path.getAbsolutePath());
			setErrorMessage(null);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		}
		else {
			targetFileText.setText("");
			setErrorMessage("File structure failed");
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}
	
	/**
	 * @param the preview file descriptors
	 */
	public void setPreviewFileDescriptors(Set<FileDescriptor> previewFileDescriptors) {
		List<FileDescriptor> list = new ArrayList<FileDescriptor>(previewFileDescriptors);
		Collections.sort(list, new DefaultFileDescriptorComparator());
		this.previewFileDescriptor = list.get(0);
	}

	/**
	 * @param mapper the mapper to set
	 */
	public void setMapper(AudioAttribute2StructureVariableMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * @return the targetDirectory
	 */
	public File getTargetDirectory() {
		return targetDirectory;
	}

	/**
	 * @return the filePattern
	 */
	public String getFilePattern() {
		return filePattern;
	}

	/**
	 * @return the creationType
	 */
	public FileCreationType getCreationType() {
		return creationType;
	}
}
