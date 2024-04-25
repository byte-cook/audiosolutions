package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.component.file.FileResult;

public class CopyFilesToDirectoryAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.editor.copyFilesToDirectory";
	private static final String STATE_DIR = "currentDir";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			Collection<EditablePlaylistFile> files = playlistEditor.getSelection().getAllFiles();
			if (files.isEmpty()) {
				files = playlistEditor.getPlaylist().getAllFiles();
			}
			
			IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
			IMementoItem item = MementoUtils.getMementoItemToSave(dialogSettings, ID);
			String currentDir = item.getString(STATE_DIR, System.getProperty("user.home"));
			
			DirectoryDialog dialog = new DirectoryDialog(window.getShell(), SWT.SAVE);
			dialog.setText("Select Target Directory");
			dialog.setFilterPath(currentDir);
			String selectedPath = dialog.open();
			if (selectedPath != null) {
				item.putString(STATE_DIR, selectedPath);
				
				final File targetDir = new File(selectedPath);
				final Set<EditablePlaylistFile> filesFinal = new HashSet<>(files);
				final Wrapper<FileResult> fileResult = Wrapper.empty();
				JFaceExec.builder(window.getShell(), "Copy Files To Directory")
					.worker(ctx -> {
						PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
						fileResult.set(playlistService.copyFilesToDir(filesFinal, targetDir, ctx.getProgressMonitor()));
					})
					.ui(ctx -> {
						fileResult.ifPresent(r -> {
							if (!r.getFailedFiles().isEmpty()) {
								FileResultDialog d = FileResultDialog.createDialog(ctx.getParent(), ctx.getName(), r);
								d.open();
							}
						});
					})
					.exceptionalDialog("Copying files failed")
					.runProgressMonitorDialog(true, true);
			}
		}
		return null;
	}

}
