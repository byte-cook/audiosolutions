package de.kobich.audiosolutions.frontend.audio.wizard;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.imexport.ExportTemplateType;
import de.kobich.audiosolutions.core.service.imexport.TemplateExportService;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.commons.ui.jface.ComboUtils;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.ComboSerializer;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.jface.memento.RadioButtonSerializer;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.commons.utils.FileUtils;


/**
 * Import/export files wizard page.
 */
public class AudioCollectionTemplateExportWizardPage extends WizardPage implements IMementoItemSerializable, Listener {
	private static final String STATE_TEMPLATE_TYPE = "templateType";
	private static final String STATE_TEMPLATE_FILE = "templateFile";
	private static final String STATE_TARGET_FILE = "targetFile";
	private Button artistAlbumTypeButton;
	private Button artistAlbumTrackTypeButton;
	private Button htmlTypeButton;
	private Button customizedTypeButton;
	private Combo templateFilePathCombo;
	private Combo targetFilePathCombo;
	private Label templatePathLabel;
	private Button templateBrowseButton;
	private Link link;
	
	public static AudioCollectionTemplateExportWizardPage createExportPage() {
		AudioCollectionTemplateExportWizardPage page = new AudioCollectionTemplateExportWizardPage("Audio Collection Template Export Settings", "Audio Collection Export");
		return page;
	}
	
	private AudioCollectionTemplateExportWizardPage(String title, String message) {
		super(title);
		setTitle(title);
		setMessage(message, IMessageProvider.INFORMATION);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardExportResourcesPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(JFaceUtils.createDialogGridLayout(2, false, JFaceUtils.MARGIN_HEIGHT, JFaceUtils.MARGIN_WIDTH));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setFont(JFaceResources.getDialogFont());

		// template type
		Label typeLabel = new Label(composite, SWT.NONE);
		typeLabel.setText("Template Type:");
		GridData typeLabelData = new GridData(GridData.FILL_HORIZONTAL);
		typeLabelData.horizontalSpan = 2;
		typeLabel.setLayoutData(typeLabelData);
		Composite typeComposite = new Composite(composite, SWT.NONE);
		GridLayout modeLayout = new GridLayout();
		modeLayout.numColumns = ExportTemplateType.values().length;
		typeComposite.setLayout(modeLayout);
		typeComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		typeComposite.setFont(JFaceResources.getDialogFont());
		artistAlbumTypeButton = new Button(typeComposite, SWT.RADIO);
		artistAlbumTypeButton.setText(ExportTemplateType.ARTIST_ALBUM.getName());
		artistAlbumTypeButton.addListener(SWT.Selection, this);
		artistAlbumTypeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reloadTemplateFileComposite();
			}
		});
		artistAlbumTrackTypeButton = new Button(typeComposite, SWT.RADIO);
		artistAlbumTrackTypeButton.setText(ExportTemplateType.ARTIST_ALBUM_TRACK.getName());
		artistAlbumTrackTypeButton.addListener(SWT.Selection, this);
		artistAlbumTrackTypeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reloadTemplateFileComposite();
			}
		});
		htmlTypeButton = new Button(typeComposite, SWT.RADIO);
		htmlTypeButton.setText(ExportTemplateType.TRACKS_HTML.getName());
		htmlTypeButton.addListener(SWT.Selection, this);
		htmlTypeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reloadTemplateFileComposite();
			}
		});
		customizedTypeButton = new Button(typeComposite, SWT.RADIO);
		customizedTypeButton.setText(ExportTemplateType.CUSTOMIZED.getName());
		customizedTypeButton.addListener(SWT.Selection, this);
		customizedTypeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reloadTemplateFileComposite();
			}
		});

		// copy definition
		link = new Link(composite, SWT.NONE);
		link.setText("<a>Copy Predefined Templates</a>");
		GridData linkData = new GridData(GridData.FILL_HORIZONTAL);
		linkData.horizontalSpan = 2;
		link.setLayoutData(linkData);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					FileDialog dialog = new FileDialog(AudioCollectionTemplateExportWizardPage.this.getShell());
					dialog.setText("Browse Target File");
					dialog.setFilterPath(System.getProperty("user.home"));
					// Open Dialog and save result of selection
					String file = dialog.open();
					if (file != null) {
						TemplateExportService templateExportService = AudioSolutions.getService(TemplateExportService.class);
						templateExportService.copyInternalExportTemplates(getExportTemplateType(), new File(file));
					}
				}
				catch (AudioException exc) {
					MessageDialog.openError(getShell(), getShell().getText(), "Copy failed.\n" + exc.getMessage());
				}
			}
		});
		
		// template file
		templatePathLabel = new Label(composite, SWT.NONE);
		templatePathLabel.setText("Template File:");
		GridData templateLabelLayoutData = new GridData(SWT.NONE, SWT.NONE, true, false);
		templateLabelLayoutData.horizontalSpan = 2;
		templatePathLabel.setLayoutData(templateLabelLayoutData);
		templateFilePathCombo = new Combo(composite, SWT.SINGLE | SWT.BORDER);
		GridData templateDirectoryLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		templateFilePathCombo.setLayoutData(templateDirectoryLayoutData);
		templateFilePathCombo.addListener(SWT.Modify, this);
		templateBrowseButton = new Button(composite, SWT.PUSH);
		templateBrowseButton.setText("Browse...");
		templateBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog dialog = new FileDialog(AudioCollectionTemplateExportWizardPage.this.getShell());
				dialog.setText("Browse Template File");
				dialog.setFilterPath(FileUtils.getFirstExistingPath(templateBrowseButton.getText()).orElse(new File("")).getAbsolutePath());
				// Open Dialog and save result of selection
				String file = dialog.open();
				if (file != null) {
					templateFilePathCombo.setText(file);
				}
			}
		});

		// target file
		Label filePathLabel = new Label(composite, SWT.NONE);
		filePathLabel.setText("Target File:");
		GridData startDirLabelLayoutData = new GridData(SWT.NONE, SWT.NONE, true, false);
		startDirLabelLayoutData.horizontalSpan = 2;
		filePathLabel.setLayoutData(startDirLabelLayoutData);
		targetFilePathCombo = new Combo(composite, SWT.SINGLE | SWT.BORDER);
		GridData startDirectoryLayoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		targetFilePathCombo.setLayoutData(startDirectoryLayoutData);
		targetFilePathCombo.addListener(SWT.Modify, this);
		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog dialog = new FileDialog(AudioCollectionTemplateExportWizardPage.this.getShell());
				dialog.setText("Browse Target File");
				dialog.setFilterPath(FileUtils.getFirstExistingPath(targetFilePathCombo.getText()).orElse(new File("")).getAbsolutePath());
				// Open Dialog and save result of selection
				String file = dialog.open();
				if (file != null) {
					targetFilePathCombo.setText(file);
				}
			}
		});
		setControl(composite);
		restoreState();
	}
	
	private void reloadTemplateFileComposite() {
		boolean enabled = customizedTypeButton.getSelection();
		templatePathLabel.setEnabled(enabled);
		templateFilePathCombo.setEnabled(enabled);
		templateBrowseButton.setEnabled(enabled);
		link.setEnabled(!enabled);
	}
	
	@Override
	public void handleEvent(Event event) {
		boolean templateFileSet = StringUtils.isNotEmpty(templateFilePathCombo.getText());
		boolean targetFileSet = StringUtils.isNotEmpty(targetFilePathCombo.getText());
		if (getWizard() instanceof AudioCollectionTemplateExportWizard) {
			AudioCollectionTemplateExportWizard wizard = (AudioCollectionTemplateExportWizard) getWizard();
			boolean canFinish = targetFileSet;
			if (customizedTypeButton.getSelection()) {
				canFinish = targetFileSet && templateFileSet;
			}
			setPageComplete(canFinish);
			wizard.setCanFinish(canFinish);
		}
		getWizard().getContainer().updateButtons();
	}
	
	public ExportTemplateType getExportTemplateType() {
		if (artistAlbumTypeButton.getSelection()) {
			return ExportTemplateType.ARTIST_ALBUM;
		}
		else if (artistAlbumTrackTypeButton.getSelection()) {
			return ExportTemplateType.ARTIST_ALBUM_TRACK;
		}
		else if (htmlTypeButton.getSelection()) {
			return ExportTemplateType.TRACKS_HTML;
		}
		else if (customizedTypeButton.getSelection()) {
			return ExportTemplateType.CUSTOMIZED;
		}
		return null;
	}
	
	/**
	 * Returns the template file
	 * @return the template file
	 */
	public File getTemplateFile() {
		File file = new File(templateFilePathCombo.getText());
		return file;
	}
	
	/**
	 * Returns the target file
	 * @return the target file
	 */
	public File getTargetFile() {
		File file = new File(targetFilePathCombo.getText());
		return file;
	}
	
	@Override
	public void dispose() {
		this.templateFilePathCombo.dispose();
		this.targetFilePathCombo.dispose();
		this.artistAlbumTrackTypeButton.dispose();
		this.artistAlbumTypeButton.dispose();
		this.customizedTypeButton.dispose();
		this.htmlTypeButton.dispose();
		this.link.dispose();
		this.templateBrowseButton.dispose();
		super.dispose();
	}
	
	@Override
	public boolean isPageComplete() {
		return true;
	}

	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, AudioCollectionTemplateExportWizardPage.class.getName());
	}

	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		RadioButtonSerializer targetTypeSerializer = new RadioButtonSerializer(STATE_TEMPLATE_TYPE, artistAlbumTypeButton.getText());
		targetTypeSerializer.restore(new Button[] {artistAlbumTypeButton, artistAlbumTrackTypeButton, htmlTypeButton, customizedTypeButton}, mementoItem);
		ComboSerializer templateFileMemento = new ComboSerializer(STATE_TEMPLATE_FILE, "");
		templateFileMemento.restore(templateFilePathCombo, mementoItem);
		ComboSerializer targetFileMemento = new ComboSerializer(STATE_TARGET_FILE, "");
		targetFileMemento.restore(targetFilePathCombo, mementoItem);
		reloadTemplateFileComposite();
	}

	@Override
	public void saveState() {
		ComboUtils.addTextToCombo(targetFilePathCombo.getText(), targetFilePathCombo);
		
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		RadioButtonSerializer targetTypeSerializer = new RadioButtonSerializer(STATE_TEMPLATE_TYPE, artistAlbumTypeButton.getText());
		targetTypeSerializer.save(new Button[] {artistAlbumTypeButton, artistAlbumTrackTypeButton, htmlTypeButton, customizedTypeButton}, mementoItem);
		ComboSerializer templateFileMemento = new ComboSerializer(STATE_TEMPLATE_FILE, "");
		templateFileMemento.save(templateFilePathCombo, mementoItem);
		ComboSerializer targetFileMemento = new ComboSerializer(STATE_TARGET_FILE, "");
		targetFileMemento.save(targetFilePathCombo, mementoItem);
	}
}
