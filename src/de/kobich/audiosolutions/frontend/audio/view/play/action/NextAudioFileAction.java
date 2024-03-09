package de.kobich.audiosolutions.frontend.audio.view.play.action;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.play.IAudioPlayingService;
import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;

/**
 * Jump to next audio file.
 */
public class NextAudioFileAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(NextAudioFileAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			AudioPlayView audioPlayView = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
			if (audioPlayView != null) {
				IAudioPlayingService audioPlayService = AudioSolutions.getService(IAudioPlayingService.JAVA_ZOOM_PLAYER, IAudioPlayingService.class);
				audioPlayService.next(audioPlayView.getPlayerClient());
			}
		}
		catch (Exception e) {
			String msg = "Cannot play file:\n";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Audio Player", msg + e.getMessage());
		}
		return null;
	}
}
