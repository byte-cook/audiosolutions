package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileResult;

public class CopyFilesToDirectoryAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(CopyFilesToDirectoryAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.editor.copyFilesToDirectory";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			Collection<EditablePlaylistFile> files = playlistEditor.getSelection().getAllFiles();
			if (files.isEmpty()) {
				files = playlistEditor.getPlaylist().getAllFiles();
			}
			
			DirectoryDialog dialog = new DirectoryDialog(window.getShell(), SWT.SAVE);
			dialog.setText("Select Target Directory");
			String selectedPath = dialog.open();
			if (selectedPath != null) {
				final File targetDir = new File(selectedPath);
				final Set<EditablePlaylistFile> filesFinal = new HashSet<>(files);
				JFaceThreadRunner runner = new JFaceThreadRunner("Copy Files To Directory", window.getShell(), List.of(RunningState.WORKER_1, RunningState.UI_2)) {
					private FileResult fileResult;

					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case WORKER_1:
							PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
							fileResult = playlistService.copyFilesToDir(filesFinal, targetDir, super.getProgressMonitor());
							break;
						case UI_2:
							if (!fileResult.getFailedFiles().isEmpty()) {
								FileResultDialog d = FileResultDialog.createDialog(super.getParent(), super.getName(), fileResult);
								d.open();
							}
							break;
						case UI_ERROR:
							Exception e = super.getException();
							logger.error(e.getMessage(), e);
							String msg = "Copying files failed: " + e.getMessage();
							MessageDialog.openError(window.getShell(), super.getName(), msg);
							break;
						default:
							break;
						}
					}
				};
				runner.runProgressMonitorDialog(true, false);
			}
		}
		// TODO add AudiCollEditor
		return null;
	}

}
