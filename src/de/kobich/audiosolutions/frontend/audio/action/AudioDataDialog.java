package de.kobich.audiosolutions.frontend.audio.action;

import java.util.Date;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioDataChange.AudioDataChangeBuilder;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.AudioControl;
import de.kobich.audiosolutions.frontend.common.AudioControl.AudioControlType;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.component.file.FileDescriptor;
import lombok.Getter;

/**
 * Audio Data Dialog.
 */
public class AudioDataDialog extends TitleAreaDialog {
	private final String title;
	private final String message;
	private final String dialogName;
	private final AudioControl artistControl;
	private final AudioControl albumControl;
	private final AudioControl albumPublicationControl;
	private final AudioControl diskControl;
	private final AudioControl genreControl;
	private final AudioControl trackControl;
	private final AudioControl trackFormatControl;
	private final AudioControl trackNoControl;
	private final AudioControl ratingControl;
	private final AudioControl mediumControl;
	@Getter
	private AudioDataChange audioDataChange;

	/**
	 * Creates the edit dialog
	 * @param parentShell
	 * @return
	 */
	public static AudioDataDialog createEditAudioDataDialog(Shell parentShell) {
		return new AudioDataDialog(parentShell, "Edit Audio Data", "Edit audio data of files.");
	}
	
	/**
	 * Constructor
	 * @param parentShell
	 * @param title
	 * @param message
	 */
	private AudioDataDialog(Shell parentShell, String title, String message) {
		super(parentShell);
		this.title = title;
		this.message = message;
		this.dialogName = "AudioData";
		this.artistControl = new AudioControl(AudioControlType.ARTIST);
		this.albumControl = new AudioControl(AudioControlType.ALBUM);
		this.albumPublicationControl = new AudioControl(AudioControlType.ALBUM_PUBLICATION);
		this.diskControl = new AudioControl(AudioControlType.DISK);
		this.genreControl = new AudioControl(AudioControlType.GENRE);
		this.trackControl = new AudioControl(AudioControlType.TRACK);
		this.trackFormatControl = new AudioControl(AudioControlType.TRACK_FORMAT);
		this.trackNoControl = new AudioControl(AudioControlType.TRACK_NO);
		this.ratingControl = new AudioControl(AudioControlType.RATING);
//		for (RatingType type: RatingType.values()) {
//			ratingControl.addValue(type.name());
//		}
		this.mediumControl = new AudioControl(AudioControlType.MEDIUM);
		
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
		GridData contentGridData = new GridData(SWT.FILL, SWT.NONE, false, false);
		contentComposite.setLayoutData(contentGridData);
		contentComposite.setFont(JFaceResources.getDialogFont());
		
		// audio controls
		this.artistControl.createLabel(contentComposite);
		this.artistControl.createControl(contentComposite);
		this.albumControl.createLabel(contentComposite);
		this.albumControl.createControl(contentComposite);
		this.albumPublicationControl.createLabel(contentComposite);
		this.albumPublicationControl.createControl(contentComposite);
		this.diskControl.createLabel(contentComposite);
		this.diskControl.createControl(contentComposite);
		this.genreControl.createLabel(contentComposite);
		this.genreControl.createControl(contentComposite);
		this.trackControl.createLabel(contentComposite);
		this.trackControl.createControl(contentComposite);
		this.trackFormatControl.createLabel(contentComposite);
		this.trackFormatControl.createControl(contentComposite);
		this.trackNoControl.createLabel(contentComposite);
		this.trackNoControl.createControl(contentComposite);
		this.ratingControl.createLabel(contentComposite);
		this.ratingControl.createControl(contentComposite);
		this.mediumControl.createLabel(contentComposite);
		this.mediumControl.createControl(contentComposite);

		JFaceUtils.createHorizontalSeparator(parent, 1);
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		// check track no
		if (!trackNoControl.isDeleteValue() && !trackNoControl.isKeepValue()) {
			Integer trackNoInt = AudioAttributeUtils.convert2Integer(trackNoControl.getValue());
			if (trackNoInt == null) {
				setErrorMessage("Track no must be numeric.");
				return;
			}
		}
		
		// check album publication
		if (!albumPublicationControl.isDeleteValue() && !albumPublicationControl.isKeepValue()) {
			Date albumPublicationDate = AudioAttributeUtils.convert2Date(albumPublicationControl.getValue());
			if (albumPublicationDate == null) {
				setErrorMessage("Publication has illegal format.");
				return;
			}
		}
		
		// audio control
		AudioDataChangeBuilder builder = AudioDataChange.builder();
		this.artistControl.apply(builder);
		this.albumControl.apply(builder);
		this.albumPublicationControl.apply(builder);
		this.diskControl.apply(builder);
		this.genreControl.apply(builder);
		this.trackControl.apply(builder);
		this.trackFormatControl.apply(builder);
		this.trackNoControl.apply(builder);
		this.ratingControl.apply(builder);
		this.mediumControl.apply(builder);
		this.audioDataChange = builder.build();

		super.okPressed();
	}

	/**
	 * @return the medium
	 */
	public void setFileDescriptors(Set<FileDescriptor> fileDescriptors) {
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			this.artistControl.addValueByFileDescriptor(fileDescriptor);
			this.albumControl.addValueByFileDescriptor(fileDescriptor);
			this.albumPublicationControl.addValueByFileDescriptor(fileDescriptor);
			this.diskControl.addValueByFileDescriptor(fileDescriptor);
			this.genreControl.addValueByFileDescriptor(fileDescriptor);
			this.trackControl.addValueByFileDescriptor(fileDescriptor);
			this.trackFormatControl.addValueByFileDescriptor(fileDescriptor);
			this.trackNoControl.addValueByFileDescriptor(fileDescriptor);
			this.ratingControl.addValueByFileDescriptor(fileDescriptor);
			this.mediumControl.addValueByFileDescriptor(fileDescriptor);
		}
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, AudioDataDialog.class.getName() + "-" + dialogName);
	}
	
}
