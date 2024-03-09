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

public class PlayAudioFileAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(PlayAudioFileAction.class);
	public static final String CALLER_PARAM = "de.kobich.audiosolutions.commands.audio.playAudioFile.caller";
	public static final String AUDIO_VIEW_CALLER_VALUE = "playerView";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String caller = event.getParameter(CALLER_PARAM);

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			PlatformUtil.showView(AudioPlayView.ID);
			AudioPlayView audioPlayView = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
			if (audioPlayView == null) {
				return null;
			}
			
			if (AUDIO_VIEW_CALLER_VALUE.equals(caller) && !audioPlayView.getPlayList().getFiles().isEmpty()) {
				FileDescriptor startFile = null;
				List<FileDescriptor> files = audioPlayView.getSelectedPlayItems();
				if (!files.isEmpty()) {
					startFile = files.get(0);
				}
				audioPlayView.startPlaying(startFile);
			}
			else {
				IEditorPart editorPart = window.getActivePage().getActiveEditor();
				if (editorPart instanceof AudioCollectionEditor) {
					AudioCollectionEditor audioCollectionEditor = (AudioCollectionEditor) editorPart;
					List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
					fileDescriptors.addAll(audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors());
					Collections.sort(fileDescriptors, new AudioFileDescriptorComparator());
					
					audioPlayView.addFilesToPlayList(fileDescriptors);
					FileDescriptor startFile = !fileDescriptors.isEmpty() ? fileDescriptors.get(0) : null;
					audioPlayView.startPlaying(startFile);
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
	
}
