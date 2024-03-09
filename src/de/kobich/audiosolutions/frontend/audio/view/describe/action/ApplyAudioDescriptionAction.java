package de.kobich.audiosolutions.frontend.audio.view.describe.action;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.describe.AudioDescriptionService;
import de.kobich.audiosolutions.core.service.describe.AudioDescriptionType;
import de.kobich.audiosolutions.core.service.describe.SetAudioDescriptionRequest;
import de.kobich.audiosolutions.frontend.audio.view.describe.AudioDescriptionView;
import de.kobich.component.file.FileDescriptor;

/**
 * Sets the audio description.
 */
public class ApplyAudioDescriptionAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(ApplyAudioDescriptionAction.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		AudioDescriptionView view = (AudioDescriptionView) window.getActivePage().findView(AudioDescriptionView.ID);
		if (view != null) {
			String description = view.getAudioDescription();
			AudioDescriptionType type = view.getAudioDescriptionType();
			List<FileDescriptor> fileDescriptors = view.getFileDescriptors();
			
			try {
				logger.info("Set audio description: " + description);
				AudioDescriptionService audioDescriptionService = AudioSolutions.getService(AudioDescriptionService.class);
				SetAudioDescriptionRequest request = new SetAudioDescriptionRequest(type, fileDescriptors, description);
				audioDescriptionService.setAudioDescription(request);
			}
			catch (final Exception exc) {
				logger.error("Error while setting audio description", exc);
				window.getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(window.getShell(), "Audio Description", "Error while setting audio description: \n" + exc.getMessage());
					}
				});
			}
		}
		return null;
	}
}
