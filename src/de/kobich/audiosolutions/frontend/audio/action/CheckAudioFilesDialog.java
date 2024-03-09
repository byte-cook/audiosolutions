package de.kobich.audiosolutions.frontend.audio.action;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.kobich.audiosolutions.core.service.check.CheckAudioFilesOptions;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;

public class CheckAudioFilesDialog extends TitleAreaDialog {
	private static final String STATE_CRC = "ignoreCrc";
	private static final String STATE_MODE = "ignoreMode";
	private static final String STATE_LAYERS = "ignoreLayers";
	private static final String STATE_BITRATE = "ignoreBitrate";
	private static final String STATE_VERSION = "ignoreVersion";
	private static final String STATE_FREQUENCY = "ignoreFrequency";
	private static final String STATE_EMPHASIS = "ignoreEmphasis";
	private String title;
	private final CommandLineTool tool;
	private Button crcButton;
	private Button modeButton;
	private Button layerButton;
	private Button bitrateButton;
	private Button versionButton;
	private Button samplingButton;
	private Button emphasisButton;
	private CheckAudioFilesOptions options;

	public CheckAudioFilesDialog(Shell parentShell, CommandLineTool tool) {
		super(parentShell);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		this.title = "Check Audio Files";
		this.tool = tool;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		this.setTitle(title);
		
		if (tool != null) {
			setMessage("Checks audio files using " + tool.getLabel(), IMessageProvider.INFORMATION);
		}
		else {
			setMessage("No suitable encoder found", IMessageProvider.ERROR);
		}
		return contents;
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

		// Group
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 1;
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(group, SWT.NONE);
		label.setText("Options:");
//		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
//		gd.horizontalSpan = 2;
//		label.setLayoutData(gd);
		
//		-C --any-crc
		this.crcButton = new Button(group, SWT.CHECK);
		crcButton.setText("Ignore crc anomalies");

//		-M --any-mode
		this.modeButton = new Button(group, SWT.CHECK);
		modeButton.setText("Ignore mode anomalies");

//		-L --any-layer
		this.layerButton = new Button(group, SWT.CHECK);
		layerButton.setText("Ignore layer anomalies");

//		-K --any-bitrate
		this.bitrateButton = new Button(group, SWT.CHECK);
		bitrateButton.setText("Ignore bitrate anomalies");

//		-I --any-version
		this.versionButton = new Button(group, SWT.CHECK);
		versionButton.setText("Ignore version anomalies");

//		-F --any-sampling
		this.samplingButton = new Button(group, SWT.CHECK);
		samplingButton.setText("Ignore sampling frequency anomalies");
		
//		-P --any-emphasis
		this.emphasisButton = new Button(group, SWT.CHECK);
		emphasisButton.setText("Ignore emphasis anomalies");

		JFaceUtils.createHorizontalSeparator(parent, 1);

		this.restoreComboState(getDialogBoundsSettings());
		return parent;
	}

	protected void okPressed() {
		this.options = new CheckAudioFilesOptions();
		this.options.setIgnoreBitrate(bitrateButton.getSelection());
		this.options.setIgnoreCrc(crcButton.getSelection());
		this.options.setIgnoreEmphasis(emphasisButton.getSelection());
		this.options.setIgnoreSamplingFrequency(samplingButton.getSelection());
		this.options.setIgnoreLayer(layerButton.getSelection());
		this.options.setIgnoreMode(modeButton.getSelection());
		this.options.setIgnoreVersion(versionButton.getSelection());

		// okPessed
		saveComboState(getDialogBoundsSettings());
		super.okPressed();
	}
	
	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(CheckAudioFilesDialog.class.getName());
		if (section == null) {
			section = settings.addNewSection(CheckAudioFilesDialog.class.getName());
		}
		return section;
	}

	private void restoreComboState(IDialogSettings settings) {
		IMementoItem mementoItem = new DialogSettingsAdapter(settings);
		bitrateButton.setSelection(Boolean.parseBoolean(mementoItem.getString(STATE_BITRATE, Boolean.FALSE.toString())));
		crcButton.setSelection(Boolean.parseBoolean(mementoItem.getString(STATE_CRC, Boolean.FALSE.toString())));
		emphasisButton.setSelection(Boolean.parseBoolean(mementoItem.getString(STATE_EMPHASIS, Boolean.FALSE.toString())));
		samplingButton.setSelection(Boolean.parseBoolean(mementoItem.getString(STATE_FREQUENCY, Boolean.FALSE.toString())));
		layerButton.setSelection(Boolean.parseBoolean(mementoItem.getString(STATE_LAYERS, Boolean.FALSE.toString())));
		modeButton.setSelection(Boolean.parseBoolean(mementoItem.getString(STATE_MODE, Boolean.FALSE.toString())));
		versionButton.setSelection(Boolean.parseBoolean(mementoItem.getString(STATE_VERSION, Boolean.FALSE.toString())));
	}

	private void saveComboState(IDialogSettings settings) {
		IMementoItem mementoItem = new DialogSettingsAdapter(settings);
		mementoItem.putString(STATE_BITRATE, "" + bitrateButton.getSelection());
		mementoItem.putString(STATE_CRC, "" + crcButton.getSelection());
		mementoItem.putString(STATE_EMPHASIS, "" + emphasisButton.getSelection());
		mementoItem.putString(STATE_FREQUENCY, "" + samplingButton.getSelection());
		mementoItem.putString(STATE_LAYERS, "" + layerButton.getSelection());
		mementoItem.putString(STATE_MODE, "" + modeButton.getSelection());
		mementoItem.putString(STATE_VERSION, "" + versionButton.getSelection());
	}

	public CheckAudioFilesOptions getOptions() {
		return options;
	}
	
}
