package de.kobich.audiosolutions.frontend.audio.action;

import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import de.kobich.audiosolutions.core.service.normalize.mp3.MP3GainNormalizationOptions.AudioNormalizingMode;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.component.file.FileDescriptor;

/**
 * Normalize files dialog.
 */
public class NormalizeFilesDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final String STATE_MODE = "mode";
	private static final String STATE_LOWER_GAIN_INSTEAD_CLIPPING = "lowerGainInsteadOfClipping";
	private static final String STATE_SUGGESTED_DEZIBEL = "suggestedDezibel";
	private String title;
	private AudioNormalizingMode normalizingMode;
	private float suggestedDecibel;
	private Spinner suggestedDecibelSpinner;
	private boolean lowerGainInsteadOfClipping;
	private Button lowerGainInsteadOfClippingButton;
	private Button trackButton;
	private Button albumButton;
	private CommandLineTool tool;

	/**
	 * Create normalize dialog
	 * @param parentShell
	 * @param encoderType
	 * @return
	 */
	public static NormalizeFilesDialog createNormalizeDialog(Shell parentShell, Set<FileDescriptor> fileDescriptors, CommandLineTool tool) {
		String title = "Normlize Files";
		NormalizeFilesDialog dialog = new NormalizeFilesDialog(parentShell, title);
		dialog.tool = tool;
		return dialog;
	}

	/**
	 * Constructor
	 * @param parentShell
	 * @param title
	 * @param message
	 */
	private NormalizeFilesDialog(Shell parentShell, String title) {
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
			setMessage("Normlizes audio files to the target directory using " + tool.getLabel() + " normalizing engine.", IMessageProvider.INFORMATION);
		}
		else {
			setMessage("No suitable normalizer found", IMessageProvider.ERROR);
		}
		return contents;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		if (tool == null) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		return c;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		JFaceUtils.createHorizontalSeparator(parent, 1);
		
		GridLayout parentLayout = new GridLayout();
		parentLayout.marginLeft = 3;
		parentLayout.marginRight = 3;
		parentLayout.numColumns = 1;
		Composite contentComposite = new Composite(parent, SWT.NONE);
		contentComposite.setLayout(parentLayout);
		contentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentComposite.setFont(JFaceResources.getDialogFont());

		// general composite
		Group generalComposite = new Group(contentComposite, SWT.NONE);
		GridLayout generalLayout = new GridLayout();
		generalLayout.marginLeft = 3;
		generalLayout.marginRight = 3;
		generalLayout.numColumns = 2;
		generalComposite.setLayout(generalLayout);
		generalComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		generalComposite.setFont(JFaceResources.getDialogFont());

		// suggested gain in decibel
		Label gainLabel = new Label(generalComposite, SWT.NONE);
		gainLabel.setText("Gain [dB]:");
		suggestedDecibelSpinner = new Spinner(generalComposite, SWT.BORDER);
		suggestedDecibelSpinner.setLayoutData(JFaceUtils.adjustGridDataTextHeight(new GridData(GridData.FILL_HORIZONTAL)));
		suggestedDecibelSpinner.setDigits(1);
		suggestedDecibelSpinner.setMinimum(10);
		suggestedDecibelSpinner.setMaximum(2550);
		suggestedDecibelSpinner.setIncrement(1);
		suggestedDecibelSpinner.setSelection(890);

		new Label(generalComposite, SWT.NONE);
		lowerGainInsteadOfClippingButton = new Button(generalComposite, SWT.CHECK);
		lowerGainInsteadOfClippingButton.setText("Lower Gain Instead Of Clipping");
		lowerGainInsteadOfClippingButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// mode composite
		Composite modeComposite = new Composite(contentComposite, SWT.NONE);
		GridLayout modeLayout = new GridLayout();
		modeLayout.marginLeft = 3;
		modeLayout.marginRight = 3;
		modeLayout.numColumns = 3;
		modeComposite.setLayout(modeLayout);
		modeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		modeComposite.setFont(JFaceResources.getDialogFont());

		// encoding modes
		Label modeLabel = new Label(modeComposite, SWT.NONE);
		modeLabel.setText("Mode:");
		trackButton = new Button(modeComposite, SWT.RADIO);
		trackButton.setText("Track Adjustment");
		albumButton = new Button(modeComposite, SWT.RADIO);
		albumButton.setText("Album Adjustment");

		JFaceUtils.createHorizontalSeparator(parent, 1);
		restoreState();
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		suggestedDecibel = (float) (suggestedDecibelSpinner.getSelection() / 10.0);
		lowerGainInsteadOfClipping = lowerGainInsteadOfClippingButton.getSelection();
		
		if (albumButton.getSelection()) {
			normalizingMode = AudioNormalizingMode.ALBUM_GAIN;
		}
		else if (trackButton.getSelection()) {
			normalizingMode = AudioNormalizingMode.TRACK_GAIN;
		}

		saveState();
		super.okPressed();
	}

	/**
	 * @return the encodingMode
	 */
	public AudioNormalizingMode getNormalizingMode() {
		return normalizingMode;
	}

	/**
	 * @return the suggestedDecibel
	 */
	public float getSuggestedDecibel() {
		return suggestedDecibel;
	}

	/**
	 * @return the lowerGainInsteadOfClipping
	 */
	public boolean isLowerGainInsteadOfClipping() {
		return lowerGainInsteadOfClipping;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, NormalizeFilesDialog.class.getName());
	}

	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		String lowerGainInsteadOfClipping = mementoItem.getString(STATE_LOWER_GAIN_INSTEAD_CLIPPING, Boolean.TRUE.toString());
		lowerGainInsteadOfClippingButton.setSelection(Boolean.parseBoolean(lowerGainInsteadOfClipping));

		int suggestedDecibel = mementoItem.getInteger(STATE_SUGGESTED_DEZIBEL, 900);
		suggestedDecibelSpinner.setSelection(suggestedDecibel);
		
		String mode = mementoItem.getString(STATE_MODE, "album");
		if ("track".equals(mode)) {
			trackButton.setSelection(true);
			albumButton.setSelection(false);
		}
		else if ("album".equals(mode)) {
			trackButton.setSelection(false);
			albumButton.setSelection(true);
		}
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		mementoItem.putString(STATE_LOWER_GAIN_INSTEAD_CLIPPING, "" + lowerGainInsteadOfClippingButton.getSelection());
		mementoItem.putInteger(STATE_SUGGESTED_DEZIBEL, suggestedDecibelSpinner.getSelection());
		if (trackButton.getSelection()) {
			mementoItem.putString(STATE_MODE, "track");
		}
		else if (albumButton.getSelection()) {
			mementoItem.putString(STATE_MODE, "album");
		}
	}
}
