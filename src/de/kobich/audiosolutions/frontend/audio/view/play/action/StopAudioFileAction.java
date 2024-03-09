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

public class StopAudioFileAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(StopAudioFileAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			AudioPlayView audioPlayView = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
			if (audioPlayView != null) {
				IAudioPlayingService audioPlayService = AudioSolutions.getService(IAudioPlayingService.JAVA_ZOOM_PLAYER, IAudioPlayingService.class);
				
				// stop old context
				audioPlayService.stop(audioPlayView.getPlayerClient());
				
				// fire event
//				ISourceProviderService sourceProviderService = (ISourceProviderService) audioPlayView.getSite().getService(ISourceProviderService.class);
//				AudioPlayViewSourceProvider p = (AudioPlayViewSourceProvider) sourceProviderService.getSourceProvider(AudioPlayViewSourceProvider.FILE_SELECTED_STATE);
//				p.changeState(AudioPlayViewSourceProvider.PLAYING_STATE, Boolean.FALSE);
//				p.changeState(AudioPlayViewSourceProvider.PAUSE_STATE, Boolean.FALSE);
//				PauseAudioFileAction.setState(audioPlayView.getSite(), Boolean.FALSE);
			}
		}
		catch (Exception e) {
			String msg = "Cannot stop file:\n";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Audio Player", msg + e.getMessage());
		}
		return null;
	}
}
