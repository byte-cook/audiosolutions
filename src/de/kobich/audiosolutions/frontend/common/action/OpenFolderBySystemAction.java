package de.kobich.audiosolutions.frontend.common.action;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.commons.ui.jface.StatusLineUtils;
import de.kobich.component.file.FileDescriptor;

/**
 * Opens a file's folder by system.
 */
public class OpenFolderBySystemAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(OpenFolderBySystemAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.file.openFolderBySystem";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			IEditorPart editorPart = window.getActivePage().getActiveEditor();
			if (editorPart instanceof AudioCollectionEditor audioCollectionEditor) {
				Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
				
				// open all selected files
				boolean allFilesExist = true;
				Set<File> folders = new HashSet<>();
				for (FileDescriptor fileDescriptor : fileDescriptors) {
					if (fileDescriptor.getFile().exists()) {
						File file = fileDescriptor.getFile().getParentFile();
						folders.add(file);
					}
					else {
						allFilesExist = false;
					}
				}
				
				if (allFilesExist) {
					for (File folder : folders) {
						Program.launch(folder.getAbsolutePath());
					}
				}
				else {
					StatusLineUtils.setStatusLineMessage(editorPart, "Some files could not be opened because they do not exist", true);
				}
			}
		} catch (Exception e) {
			String msg = "Error while opening files";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Error", msg + e.getMessage());
		}
		return null;
	}
}
