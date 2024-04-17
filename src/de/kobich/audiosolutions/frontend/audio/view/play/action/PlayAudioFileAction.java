package de.kobich.audiosolutions.frontend.audio.view.play.action;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFileComparator;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
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
			
			if (AUDIO_VIEW_CALLER_VALUE.equals(caller) && !audioPlayView.getPlaylist().isEmpty()) {
				EditablePlaylistFile startFile = null;
				List<EditablePlaylistFile> files = audioPlayView.getSelectedPlayItems();
				if (!files.isEmpty()) {
					files.sort(EditablePlaylistFileComparator.INSTANCE);
					startFile = files.get(0);
				}
				audioPlayView.startPlaying(startFile);
			}
			else {
				IEditorPart editorPart = window.getActivePage().getActiveEditor();
				if (editorPart instanceof AudioCollectionEditor audioCollectionEditor) {
					Set<File> files = audioCollectionEditor.getFileDescriptorSelection().getExistingFiles().stream().map(FileDescriptor::getFile).collect(Collectors.toSet());
					audioPlayView.appendFilesAndPlay(files);
				}
				else if (editorPart instanceof PlaylistEditor playlistEditor) {
					Set<File> files = playlistEditor.getSelection().getExistingFiles().stream().map(EditablePlaylistFile::getFile).collect(Collectors.toSet());
					audioPlayView.appendFilesAndPlay(files);
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
