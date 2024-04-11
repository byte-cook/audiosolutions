package de.kobich.audiosolutions.frontend.audio.view.play.action;

import java.io.File;
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
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;
import de.kobich.audiosolutions.frontend.common.util.PlatformUtil;
import de.kobich.component.file.FileDescriptor;

/**
 * Adds file to play list.
 */
public class AppendFilesToPlayerAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(AppendFilesToPlayerAction.class);
	private IWorkbenchWindow window;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			PlatformUtil.showView(AudioPlayView.ID);
			AudioPlayView audioPlayView = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
			if (audioPlayView != null) {
				IEditorPart editorPart = window.getActivePage().getActiveEditor();
				if (editorPart instanceof AudioCollectionEditor audioCollectionEditor) {
					Set<File> files = audioCollectionEditor.getFileDescriptorSelection().getExistingFiles().stream().map(FileDescriptor::getFile).collect(Collectors.toSet());
					audioPlayView.appendFiles(files);
				}
				else if (editorPart instanceof PlaylistEditor playlistEditor) {
					// TODO playlist
					Set<File> files = playlistEditor.getSelection().getExistingFiles().stream().map(EditablePlaylistFile::getFile).collect(Collectors.toSet());
					audioPlayView.appendFiles(files);
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
