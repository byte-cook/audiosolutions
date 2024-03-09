package de.kobich.audiosolutions.frontend.audio.view.play.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.service.AudioFileDescriptorComparator;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;
import de.kobich.audiosolutions.frontend.common.util.PlatformUtil;
import de.kobich.component.file.FileDescriptor;

/**
 * Adds file to play list.
 */
public class AddFilesToPlayListAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(AddFilesToPlayListAction.class);
	private IWorkbenchWindow window;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			IEditorPart editorPart = window.getActivePage().getActiveEditor();
			if (editorPart instanceof AudioCollectionEditor) {
				AudioCollectionEditor audioCollectionEditor = (AudioCollectionEditor) editorPart;
				List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
				fileDescriptors.addAll(audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors());
				Collections.sort(fileDescriptors, new AudioFileDescriptorComparator());
	
				PlatformUtil.showView(AudioPlayView.ID);
				AudioPlayView audioPlayView = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
				if (audioPlayView != null) {
					audioPlayView.addFilesToPlayList(fileDescriptors);
				}
			}
		}
		catch (Exception e) {
			String msg = "Files cannot be added to play list:\n";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Audio Player", msg + e.getMessage());
		}
		return null;
	}
}
