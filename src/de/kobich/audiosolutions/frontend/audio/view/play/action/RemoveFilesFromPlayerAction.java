package de.kobich.audiosolutions.frontend.audio.view.play.action;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;

public class RemoveFilesFromPlayerAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(RemoveFilesFromPlayerAction.class);
	private IWorkbenchWindow window;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			AudioPlayView audioPlayView = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
			if (audioPlayView != null) {
			 	List<EditablePlaylistFile> files2Remove = audioPlayView.getSelectedPlayItems();
				if (files2Remove.isEmpty()) {
					files2Remove = audioPlayView.getPlaylist().getFilesSorted();
					boolean confirmed = MessageDialog.openQuestion(window.getShell(), "Delete Play List", "Do you want to delete the complete play list?");
					if (!confirmed) {
						return null;
					}
				}
				
				Wrapper<List<EditablePlaylistFile>> FILES = Wrapper.of(files2Remove);
				JFaceExec.builder(window.getShell())
					.worker(ctx -> audioPlayView.removeFiles(FILES.get()))
					.ui(ctx -> audioPlayView.refresh())
					.runProgressMonitorDialog(true, false);
			}
		}
		catch (Exception e) {
			String msg = "Files cannot be removed from play list:\n";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Audio Player", msg + e.getMessage());
		}
		return null;
	}
}
