package de.kobich.audiosolutions.frontend.audio.view.describe;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.describe.AudioDescriptionService;
import de.kobich.audiosolutions.core.service.describe.AudioDescriptionType;
import de.kobich.audiosolutions.core.service.describe.GetAudioDescriptionRequest;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.component.file.FileDescriptor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class AudioDescriptionView extends ViewPart { 
	public static final Logger logger = Logger.getLogger(AudioDescriptionView.class);
	public static final String ID = "de.kobich.audiosolutions.view.audio.descriptionView";
	private AudioDescriptionViewEventListener eventListener;
	private List<FileDescriptor> fileDescriptors;
	private List<AudioDescriptionElement> elements;
	private TabFolder folder;
//	private boolean pin;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.eventListener = new AudioDescriptionViewEventListener(this);
		this.elements = new ArrayList<AudioDescriptionElement>();
		
//		ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
//		Command pinCommand = commandService.getCommand(PinViewAction.ID);
//		State pinState = pinCommand.getState(PinViewAction.STATE_ID);
//		Boolean pinValue = Boolean.FALSE;
//		if (pinState != null) {
//			pinValue = (Boolean) pinState.getValue();
//		}
//		this.pin = pinValue.booleanValue();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(JFaceUtils.createViewGridLayout(1, false, JFaceUtils.MARGIN_TOP));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		this.folder = new TabFolder(parent, SWT.NONE);
		this.folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.folder.setLayout(JFaceUtils.createViewGridLayout(1, false, JFaceUtils.MARGIN_WIDTH));
		this.folder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AudioDescriptionElement tab = getAudioDescriptionElement();
				getViewSite().getShell().getDisplay().asyncExec(new TaskRunnable(fileDescriptors, tab));
			}
		});

		this.elements.add(AudioDescriptionElement.create(AudioDescriptionType.ARTIST, "Artist", "Affects all tracks of the same artist", folder));
		this.elements.add(AudioDescriptionElement.create(AudioDescriptionType.ALBUM, "Album", "Affects all tracks on the same album", folder));
		this.elements.add(AudioDescriptionElement.create(AudioDescriptionType.TRACK, "Track", "Affects only the selected track", folder));

		fireDeselection();
		eventListener.register();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		this.folder.setFocus();
	}
	
	@Override
	public void dispose() {
		this.eventListener.deregister();
		
		this.elements.forEach(AudioDescriptionElement::dispose);
		this.folder.dispose();
		super.dispose();
	}
	
	public AudioDescriptionElement getAudioDescriptionElement() {
		return elements.get(this.folder.getSelectionIndex());
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
		AudioDescriptionViewSourceProvider.getInstance().setModified(false);

		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (getViewSite().getShell() != null && !getViewSite().getShell().isDisposed()) {
					setContentDescription("Please select one audio file.");
					getAudioDescriptionElement().setEnabled(false);
					folder.setEnabled(false);
					
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
		
		AudioDescriptionElement tab = getAudioDescriptionElement();
		getViewSite().getShell().getDisplay().asyncExec(new TaskRunnable(fileDescriptors, tab));
	}
	
	/**
	 * TaskRunnable
	 */
	private class TaskRunnable implements Runnable {
		private List<FileDescriptor> fileDescriptors;
		private AudioDescriptionElement tab;
		
		public TaskRunnable(List<FileDescriptor> fileDescriptors, AudioDescriptionElement tab) {
			this.fileDescriptors = fileDescriptors;
			this.tab = tab;
		}
		
		public void run() {
			try {
				// fire event
				AudioDescriptionViewSourceProvider.getInstance().setModified(true);

				AudioDescriptionView.this.fileDescriptors = fileDescriptors;
				FileDescriptor fileDescriptor = fileDescriptors.get(0);
				
				AudioDescriptionService audioDescriptionService = AudioSolutions.getService(AudioDescriptionService.class);
				GetAudioDescriptionRequest request = new GetAudioDescriptionRequest(tab.type, fileDescriptors);
				String description = audioDescriptionService.getAudioDescription(request).orElse("");
				if (folder.isDisposed()) {
					return;
				}
				folder.setEnabled(true);
				tab.setEnabled(true);
				tab.descriptionAreaText.setText(description);
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
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class AudioDescriptionElement {
		@Getter
		private final AudioDescriptionType type;
		private final Label typeInfoLabel;
		private final Text descriptionAreaText;
		
		public static AudioDescriptionElement create(AudioDescriptionType type, String label, String description, TabFolder folder) {
			TabItem item = new TabItem(folder, SWT.NULL);
			item.setText(label);
			
			Composite tabComposite = new Composite(folder, SWT.NONE);
			tabComposite.setLayout(JFaceUtils.createViewGridLayout(1, false, JFaceUtils.MARGIN_WIDTH, JFaceUtils.MARGIN_TOP));
			item.setControl(tabComposite);
			
			Label typeInfoLabel = new Label(tabComposite, SWT.NONE);
			typeInfoLabel.setForeground(JFaceUtils.getInfoTextForegroundColor());
			typeInfoLabel.setText(description);
			typeInfoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			// text area
			Text descriptionAreaText = new Text(tabComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			GridData textLayoutData = new GridData(GridData.FILL_BOTH);
			descriptionAreaText.setLayoutData(textLayoutData);
			
			return new AudioDescriptionElement(type, typeInfoLabel, descriptionAreaText);
		}
		
		public void setEnabled(boolean b) {
			typeInfoLabel.setEnabled(b);
			descriptionAreaText.setEnabled(b);
		}
		
		public String getText() {
			return descriptionAreaText.getText();
		}
		
		public void dispose() {
			typeInfoLabel.dispose();
			descriptionAreaText.dispose();
		}
	}
}