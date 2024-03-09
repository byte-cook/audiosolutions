package de.kobich.audiosolutions.frontend.audio.wizard;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.commons.ui.jface.ComboUtils;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.ComboSerializer;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;


/**
 * Import/export files wizard page.
 */
public class AudioCollectionImExportWizardPage extends WizardPage implements IMementoItemSerializable, Listener {
	private static final String STATE_FILE = "file";
	private String fileLabel;
	private Combo filePathCombo;
	private String[] filterExtensions;
	private String[] filterNames;
	
	public static AudioCollectionImExportWizardPage createExportPage() {
		AudioCollectionImExportWizardPage page = new AudioCollectionImExportWizardPage("Audio Collection Export Settings", "Audio Collection Export");
		page.fileLabel = "Target File:";
		page.filterExtensions = new String[] {"*.xml", "*.csv"};
		page.filterNames = new String[] {"XML Files (*.xml)", "CSV Files (*.csv)"};
		return page;
	}
	
	public static AudioCollectionImExportWizardPage createImportPage() {
		AudioCollectionImExportWizardPage page = new AudioCollectionImExportWizardPage("Audio Collection Import Settings", "Audio Collection Import");
		page.fileLabel = "Source File:";
		page.filterExtensions = new String[] {"*.xml"};
		page.filterNames = new String[] {"XML Files (*.xml)"};
		return page;
	}

	private AudioCollectionImExportWizardPage(String title, String message) {
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
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(JFaceResources.getDialogFont());

		Label filePathLabel = new Label(composite, SWT.NONE);
		filePathLabel.setText(fileLabel);
		GridData startDirLabelLayoutData = new GridData(SWT.NONE, SWT.NONE, true, false);
		startDirLabelLayoutData.horizontalSpan = 2;
		filePathLabel.setLayoutData(startDirLabelLayoutData);
		filePathCombo = new Combo(composite, SWT.SINGLE | SWT.BORDER);
		GridData startDirectoryLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		filePathCombo.setLayoutData(startDirectoryLayoutData);
		filePathCombo.addListener(SWT.Modify, this);
		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog dialog = new FileDialog(AudioCollectionImExportWizardPage.this.getShell());
				dialog.setText("Browse Target File");
				dialog.setFilterExtensions(filterExtensions);
				dialog.setFilterNames(filterNames);
				// Open Dialog and save result of selection
				String file = dialog.open();
				if (file != null) {
					filePathCombo.setText(file);
				}
			}
		});
		setControl(composite);
		restoreState();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.widget == filePathCombo) {
			if (StringUtils.isNotEmpty(filePathCombo.getText())) {
				boolean validFileExtension = false;
				for (String filterExtension : filterExtensions) {
					validFileExtension = filePathCombo.getText().endsWith(filterExtension.substring(1));
					if (validFileExtension) {
						break;
					}
				}
				if (!validFileExtension) {
					setErrorMessage("File extension is illegal");
				}
				else {
					setErrorMessage(null);
				}
				if (getWizard() instanceof AudioCollectionImportWizard) {
					AudioCollectionImportWizard wizard = (AudioCollectionImportWizard) getWizard();
					boolean fileExists = new File(filePathCombo.getText()).exists();
					if (!fileExists) {
						setErrorMessage("File does not exist");
					}
					setPageComplete(validFileExtension && fileExists);
					wizard.setCanFinish(validFileExtension && fileExists);
				}
				else if (getWizard() instanceof AudioCollectionExportWizard) {
					AudioCollectionExportWizard wizard = (AudioCollectionExportWizard) getWizard();
					setPageComplete(validFileExtension);
					wizard.setCanFinish(validFileExtension);
				}
			}
		}
		getWizard().getContainer().updateButtons();
	}
	
	/**
	 * Returns the target file
	 * @return the target file
	 */
	public File getTargetFile() {
		File file = new File(filePathCombo.getText());
		return file;
	}
	
	@Override
	public void dispose() {
		this.filePathCombo.dispose();
	}
	
	@Override
	public boolean isPageComplete() {
		return true;
	}

	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, AudioCollectionImExportWizardPage.class.getName());
	}

	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer fileMemento = new ComboSerializer(STATE_FILE, "");
		fileMemento.restore(filePathCombo, mementoItem);
	}

	@Override
	public void saveState() {
		ComboUtils.addTextToCombo(filePathCombo.getText(), filePathCombo);
		
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		ComboSerializer fileMemento = new ComboSerializer(STATE_FILE, "");
		fileMemento.save(filePathCombo, mementoItem);
	}
}
