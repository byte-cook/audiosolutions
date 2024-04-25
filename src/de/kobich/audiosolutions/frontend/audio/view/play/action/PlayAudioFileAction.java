package de.kobich.audiosolutions.frontend.audio.view.play.action;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;
import de.kobich.audiosolutions.frontend.common.util.PlatformUtil;
import de.kobich.commons.ui.jface.JFaceExec;

public class PlayAudioFileAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(PlayAudioFileAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			PlatformUtil.showView(AudioPlayView.ID);
			AudioPlayView audioPlayView = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
			if (audioPlayView == null) {
				return null;
			}
			
			JFaceExec.builder(window.getShell())
				.worker(ctx -> audioPlayView.startPlaying(null))
				.ui(ctx -> audioPlayView.refresh())
				.runProgressMonitorDialog(true, false);
		}
		catch (Exception e) {
			String msg = "Cannot play file:\n";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Audio Player", msg + e.getMessage());
		}
		return null;
	}
	
}
