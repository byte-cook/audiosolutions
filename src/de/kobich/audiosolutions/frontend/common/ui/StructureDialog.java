package de.kobich.audiosolutions.frontend.common.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttribute2StructureVariableMapper;
import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.AudioAttributeComparator;
import de.kobich.audiosolutions.frontend.common.AudioStructureVariableContentProposalProvider;
import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.misc.extract.ExtractStructureResponse;
import de.kobich.commons.misc.extract.Extractor;
import de.kobich.commons.misc.extract.IText;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.ui.jface.ComboUtils;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.ComboSerializer;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.FileDescriptorTextAdapter;

/**
 * Structure Dialog.
 */
public class StructureDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final Logger logger = Logger.getLogger(StructureDialog.class);
	private static final String STATE_STRUCTURE = "fileStructure";
	private static final String defaultStructure = "/" + AudioAttribute2StructureVariableMapper.ALBUM_VAR + "/"
			+ AudioAttribute2StructureVariableMapper.TRACK_VAR + "." + AudioAttribute2StructureVariableMapper.TRACK_FORMAT_VAR;
	private String title;
	private String message;
	private String dialogName;
	private String fileStructure;
	private Combo fileStructureCombo;
	private FileDescriptor previewFileDescriptor;
	private AudioAttribute2StructureVariableMapper mapper;
	private Map<AudioAttribute, AudioAttributeElement> elements;

	public static StructureDialog createAddAudioDataDialog(Shell parentShell) {
		StructureDialog dialog = new StructureDialog(parentShell, "Set Audio Data By Structure", "Sets audio data to files by file structure.");
		dialog.dialogName = "AudioData";
		return dialog;
	}

	public static StructureDialog createID3TagsDialog(Shell parentShell) {
		StructureDialog dialog = new StructureDialog(parentShell, "Set ID3 Tags By Structure", "Sets ID3 tags to files by file structure.");
		dialog.elements.get(AudioAttribute.MEDIUM).visible = false;
		dialog.elements.get(AudioAttribute.DISK).visible = false;
		dialog.elements.get(AudioAttribute.TRACK_FORMAT).visible = false;
		dialog.dialogName = "ID3Tags";
		return dialog;
	}

	/**
	 * Constructor
	 * @param parentShell
	 * @param title
	 * @param message
	 */
	private StructureDialog(Shell parentShell, String title, String message) {
		super(parentShell);
		this.title = title;
		this.message = message;
		this.elements = new Hashtable<AudioAttribute, AudioAttributeElement>();
		elements.put(AudioAttribute.MEDIUM, new AudioAttributeElement("Medium:"));
		elements.put(AudioAttribute.GENRE, new AudioAttributeElement("Genre:"));
		elements.put(AudioAttribute.ARTIST, new AudioAttributeElement("Artist:"));
		elements.put(AudioAttribute.ALBUM, new AudioAttributeElement("Album:"));
		elements.put(AudioAttribute.ALBUM_PUBLICATION, new AudioAttributeElement("Publication:"));
		elements.put(AudioAttribute.DISK, new AudioAttributeElement("Disk:"));
		elements.put(AudioAttribute.TRACK, new AudioAttributeElement("Track:"));
		elements.put(AudioAttribute.TRACK_NO, new AudioAttributeElement("Track No:"));
		elements.put(AudioAttribute.TRACK_FORMAT, new AudioAttributeElement("Track Format:"));

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
		contentComposite.setLayout(JFaceUtils.createDialogGridLayout(2, false, JFaceUtils.MARGIN_HEIGHT, JFaceUtils.MARGIN_WIDTH));
		contentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentComposite.setFont(JFaceResources.getDialogFont());

		// file structure
		Label fileStructureLabel = new Label(contentComposite, SWT.NONE);
		fileStructureLabel.setText("File Structure:");
		GridData labelGridData = new GridData(GridData.FILL_HORIZONTAL);
		labelGridData.horizontalSpan = 2;
		fileStructureLabel.setLayoutData(labelGridData);
		fileStructureCombo = new Combo(contentComposite, SWT.BORDER);
		GridData comboGridData = new GridData(GridData.FILL_HORIZONTAL);
		comboGridData.horizontalSpan = 2;
		fileStructureCombo.setLayoutData(comboGridData);
		fileStructureCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				checkFileStructure();
			}
		});
		IContentProposalProvider proposalProvider = new AudioStructureVariableContentProposalProvider();
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
			DecoratorUtils.createDecorator(fileStructureCombo, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		}
		catch (ParseException exc) {
			logger.warn("Key stroke cannot be created", exc);
		}
		ContentProposalAdapter adapter = new ContentProposalAdapter(fileStructureCombo, new ComboContentAdapter(), proposalProvider, keyStroke, null);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);

		// preview group
		Group previewGroup = new Group(contentComposite, SWT.NONE);
		previewGroup.setText("Preview");
		previewGroup.setLayout(JFaceUtils.createDialogGridLayout(2, false, JFaceUtils.MARGIN_HEIGHT, JFaceUtils.MARGIN_WIDTH));
		GridData previewGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		previewGridData.horizontalSpan = 2;
		previewGroup.setLayoutData(previewGridData);

		// file
		Label fileLabel = new Label(previewGroup, SWT.NONE);
		fileLabel.setText("File:");
		fileLabel.setLayoutData(new GridData());
		Text relativePathText = new Text(previewGroup, SWT.BORDER | SWT.READ_ONLY);
		String relativePath = previewFileDescriptor.getRelativePath();
		relativePathText.setText(relativePath);
		relativePathText.setBackground(JFaceUtils.getDisabledTextBackgroundColor());
		GridData relativePathData = new GridData(GridData.FILL_HORIZONTAL);
		relativePathData.widthHint = 300;
		relativePathText.setLayoutData(relativePathData);

		List<AudioAttribute> attributes = new ArrayList<AudioAttribute>();
		for (AudioAttribute attribute : AudioAttribute.values()) {
			attributes.add(attribute);
		}
		Collections.sort(attributes, new AudioAttributeComparator());
		for (AudioAttribute attribute : attributes) {
			AudioAttributeElement element = elements.get(attribute);
			if (element != null && element.visible) {
				Label mediumLabel = new Label(previewGroup, SWT.NONE);
				mediumLabel.setText(element.label);
				mediumLabel.setLayoutData(new GridData());
				Text text = new Text(previewGroup, SWT.BORDER | SWT.READ_ONLY);
				text.setBackground(JFaceUtils.getDisabledTextBackgroundColor());
				GridData textData = new GridData(GridData.FILL_HORIZONTAL);
				textData.widthHint = 300;
				text.setLayoutData(textData);
				element.textLabel = text;
			}
		}

		JFaceUtils.createHorizontalSeparator(parent, 2);
		return parent;
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		restoreState();
		return control;
	}

	/**
	 * Checks the current given file structure
	 */
	protected void checkFileStructure() {
		for (AudioAttributeElement element : elements.values()) {
			if (element.visible) {
				element.textLabel.setText("");
			}
		}

		Set<IText> texts = new HashSet<IText>();
		texts.add(new FileDescriptorTextAdapter(previewFileDescriptor));
		ExtractStructureResponse response = Extractor.extract(texts, fileStructureCombo.getText(), mapper.getVariables(), null);
		if (!response.getSucceededTexts().isEmpty()) {
			Map<StructureVariable, String> map = response.getSucceededTexts().values().iterator().next();
			for (StructureVariable variable : map.keySet()) {
				AudioAttribute audioAttribute = mapper.getAudioAttribute(variable);
				if (audioAttribute != null) {
					Text text = elements.get(audioAttribute).textLabel;
					if (text != null) {
						String value = map.get(variable);
						if (AudioAttribute.ALBUM_PUBLICATION.equals(audioAttribute)) {
							Date date = AudioAttributeUtils.convert2Date(value);
							if (date == null) {
								continue;
							}
						}
						else if (AudioAttribute.TRACK_NO.equals(audioAttribute)) {
							Integer trackNo = AudioAttributeUtils.convert2Integer(value);
							if (trackNo == null) {
								continue;
							}
						}
						text.setText(value);
					}
				}
			}
		}
		if (!response.getFailedTexts().isEmpty()) {
			setErrorMessage("File structure failed");
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		else {
			setErrorMessage(null);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if (fileStructureCombo.getText().isEmpty()) {
			setErrorMessage("Please define the file structure.");
			return;
		}
		fileStructure = fileStructureCombo.getText();
		ComboUtils.addTextToCombo(fileStructureCombo.getText(), fileStructureCombo);

		saveState();
		super.okPressed();
	}

	/**
	 * @return the fileStructure
	 */
	public String getFileStructure() {
		return fileStructure;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, dialogName + StructureDialog.class.getName());
	}

	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer structureMemento = new ComboSerializer(STATE_STRUCTURE, defaultStructure);
		structureMemento.restore(fileStructureCombo, mementoItem);
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer structureMemento = new ComboSerializer(STATE_STRUCTURE, defaultStructure);
		structureMemento.save(fileStructureCombo, mementoItem);
	}

	/**
	 * @param previewFileDescriptors the previewFileDescriptors to set
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
	 * AudioAttributeElement
	 */
	private class AudioAttributeElement {
		private String label;
		private Text textLabel;
		private boolean visible;

		public AudioAttributeElement(String label) {
			this.label = label;
			this.visible = true;
		}
	}
}
