package de.kobich.audiosolutions.frontend.audio.view.describe;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.describe.AudioDescriptionService;
import de.kobich.audiosolutions.core.service.describe.AudioDescriptionType;
import de.kobich.audiosolutions.core.service.describe.GetAudioDescriptionRequest;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.component.file.FileDescriptor;

public class AudioDescriptionView extends ViewPart { 
	public static final Logger logger = Logger.getLogger(AudioDescriptionView.class);
	public static final String ID = "de.kobich.audiosolutions.view.audio.descriptionView";
	private AudioDescriptionViewEventListener eventListener;
	private List<FileDescriptor> fileDescriptors;
	private List<AudioAttributeElement> elements;
	private int currentIndex;
	private Composite typeGroup;
	private Button[] typeButtons;
	private Label typeLabel;
	private Label typeInfoLabel;
	private Text descriptionAreaText;
//	private boolean pin;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.eventListener = new AudioDescriptionViewEventListener(this);
		this.elements = new ArrayList<AudioAttributeElement>();
		this.elements.add(new AudioAttributeElement(AudioDescriptionType.ARTIST, "Artist", "Affects all tracks of the same artist"));
		this.elements.add(new AudioAttributeElement(AudioDescriptionType.ALBUM, "Album", "Affects all tracks on the same album"));
		this.elements.add(new AudioAttributeElement(AudioDescriptionType.TRACK, "Track", "Affects only the selected track"));
//		ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
//		Command pinCommand = commandService.getCommand(PinViewAction.ID);
//		State pinState = pinCommand.getState(PinViewAction.STATE_ID);
//		Boolean pinValue = Boolean.FALSE;
//		if (pinState != null) {
//			pinValue = (Boolean) pinState.getValue();
//		}
//		this.pin = pinValue.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(JFaceUtils.createViewGridLayout(1, false, JFaceUtils.MARGIN_TOP));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		// edit audio data
		Composite topComposite = new Composite(parent, SWT.NONE);
		topComposite.setLayout(JFaceUtils.createViewGridLayout(2, false, JFaceUtils.MARGIN_WIDTH));
		topComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// type
		typeLabel = new Label(topComposite, SWT.NONE);
		typeLabel.setText("Description Type:");
		final int typeCount = elements.size();
		typeGroup = new Composite(topComposite, SWT.NONE);
		typeGroup.setLayout(JFaceUtils.createViewGridLayout(typeCount, false));
		typeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		typeButtons = new Button[typeCount];
		for (int i = 0; i < typeCount; ++ i) {
			AudioAttributeElement element = elements.get(i);
			typeButtons[i] = new Button(typeGroup, SWT.RADIO);
			typeButtons[i].setText(element.label);
			final int index = i;
			typeButtons[i].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					currentIndex = index;
					typeInfoLabel.setText(elements.get(currentIndex).description);
					AudioDescriptionType type = getAudioDescriptionType();
					getViewSite().getShell().getDisplay().asyncExec(new TaskRunnable(fileDescriptors, type));
				}
			});
		}
		currentIndex = 0;
		typeButtons[currentIndex].setSelection(true);
		typeInfoLabel = new Label(topComposite, SWT.NONE);
		typeInfoLabel.setForeground(JFaceUtils.getInfoTextForegroundColor());
		GridData infoData = new GridData(GridData.FILL_HORIZONTAL);
		infoData.horizontalSpan = 2;
		typeInfoLabel.setLayoutData(infoData);
		typeInfoLabel.setText(elements.get(currentIndex).description);
		
		// text area
		descriptionAreaText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData textLayoutData = new GridData(GridData.FILL_BOTH);
		descriptionAreaText.setLayoutData(textLayoutData);
		
		fireDeselection();

		// register for events
		eventListener.register();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		this.typeGroup.setFocus();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		this.eventListener.deregister();
		this.typeGroup.dispose();
		this.descriptionAreaText.dispose();
		this.typeLabel.dispose();
		for (Button button : this.typeButtons) {
			button.dispose();
		}
		this.typeInfoLabel.dispose();
		super.dispose();
	}
	
	public String getAudioDescription() {
		return descriptionAreaText.getText();
	}
	
	public AudioDescriptionType getAudioDescriptionType() {
		return elements.get(currentIndex).type;
	}

	/**
	 * @return the fileDescriptors
	 */
	public List<FileDescriptor> getFileDescriptors() {
		return fileDescriptors;
	}
	
	/**
	 * Pins the view
	 */
//	public synchronized void setPinView(boolean pin) {
//		this.pin = pin;
//	}

	/**
	 * Called if no files are selected
	 */
	public void fireDeselection() {
//		if (pin) {
//			return;
//		}
		// fire event
		ISourceProviderService sourceProviderService = (ISourceProviderService) getSite().getService(ISourceProviderService.class);
		AudioDescriptionViewSourceProvider p = (AudioDescriptionViewSourceProvider) sourceProviderService.getSourceProvider(AudioDescriptionViewSourceProvider.MODIFIED_STATE);
		p.changeState(AudioDescriptionViewSourceProvider.MODIFIED_STATE, Boolean.FALSE);

		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (getViewSite().getShell() != null && !getViewSite().getShell().isDisposed()) {
					setContentDescription("Please select one audio file.");
					if (typeGroup != null && !typeGroup.isDisposed()) {
						typeGroup.setEnabled(false);
						typeLabel.setEnabled(false);
						for (Button b : typeButtons) {
							b.setEnabled(false);
						}
					}
					descriptionAreaText.setEnabled(false);
				}
			}
		});
	}

	/**
	 * Called if files are selected
	 */
	public void fireSelection(List<FileDescriptor> fileDescriptors) {
//		if (pin) {
//			return;
//		}
		
		AudioDescriptionType type = getAudioDescriptionType();
		getViewSite().getShell().getDisplay().asyncExec(new TaskRunnable(fileDescriptors, type));
	}
	
	/**
	 * TaskRunnable
	 */
	private class TaskRunnable implements Runnable {
		private List<FileDescriptor> fileDescriptors;
		private AudioDescriptionType type;
		
		public TaskRunnable(List<FileDescriptor> fileDescriptors, AudioDescriptionType type) {
			this.fileDescriptors = fileDescriptors;
			this.type = type;
		}
		
		public void run() {
			try {
				// fire event
				ISourceProviderService sourceProviderService = (ISourceProviderService) getSite().getService(ISourceProviderService.class);
				if (sourceProviderService == null) {
					return;
				}
				AudioDescriptionViewSourceProvider p = (AudioDescriptionViewSourceProvider) sourceProviderService.getSourceProvider(AudioDescriptionViewSourceProvider.MODIFIED_STATE);
				p.changeState(AudioDescriptionViewSourceProvider.MODIFIED_STATE, Boolean.TRUE);

				AudioDescriptionView.this.fileDescriptors = fileDescriptors;
				FileDescriptor fileDescriptor = fileDescriptors.get(0);
				
				AudioDescriptionService audioDescriptionService = AudioSolutions.getService(AudioDescriptionService.class);
				GetAudioDescriptionRequest request = new GetAudioDescriptionRequest(type, fileDescriptors);
				String description = audioDescriptionService.getAudioDescription(request).orElse("");
				descriptionAreaText.setText(description);
				if (typeGroup != null && !typeGroup.isDisposed()) {
					typeGroup.setEnabled(true);
					typeLabel.setEnabled(true);
					for (Button b : typeButtons) {
						b.setEnabled(true);
					}
				}
				descriptionAreaText.setEnabled(true);
				setContentDescription(fileDescriptor.getFileName() + " selected");
			}
			catch (Exception exc) {
				logger.error("Audio description could not be read", exc);
			}
		}
	}
	
	/**
	 * AudioAttributeElement
	 */
	private class AudioAttributeElement {
		private AudioDescriptionType type;
		private String label;
		private String description;
		
		public AudioAttributeElement(AudioDescriptionType type, String label, String description) {
			this.label = label;
			this.type = type;
			this.description = description;
		}
	}
}