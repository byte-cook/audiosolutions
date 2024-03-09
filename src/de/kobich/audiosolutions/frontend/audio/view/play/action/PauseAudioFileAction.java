package de.kobich.audiosolutions.frontend.audio.view.play.action;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.play.IAudioPlayingService;
import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;

public class PauseAudioFileAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(PauseAudioFileAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.audio.PauseAudioFile";
	public static final String STATE_ID = "org.eclipse.ui.commands.toggleState";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			AudioPlayView audioPlayView = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
			if (audioPlayView != null) {
				IAudioPlayingService audioPlayService = AudioSolutions.getService(IAudioPlayingService.JAVA_ZOOM_PLAYER, IAudioPlayingService.class);
				boolean oldValue = HandlerUtil.toggleCommandState(event.getCommand());
				if (!oldValue) {
					audioPlayService.pause(audioPlayView.getPlayerClient());
				}
				else {
					audioPlayService.resume(audioPlayView.getPlayerClient());
				}
			}
		}
		catch (Exception e) {
			String msg = "Cannot play file:\n";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Audio Player", msg + e.getMessage());
		}
		return null;
	}
	
	public static void setState(IWorkbenchPartSite site, Boolean b) {
		ICommandService commandService = (ICommandService) site.getService(ICommandService.class);
		Command pauseCommand = commandService.getCommand(PauseAudioFileAction.ID);
		State pauseState = pauseCommand.getState(PauseAudioFileAction.STATE_ID);
		pauseState.setValue(b);
	}
}
