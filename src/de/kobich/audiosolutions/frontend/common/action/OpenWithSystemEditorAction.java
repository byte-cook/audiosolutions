package de.kobich.audiosolutions.frontend.common.action;

import java.io.File;
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

import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.commons.ui.jface.StatusLineUtils;
import de.kobich.component.file.FileDescriptor;


/**
 * Opens a file by system editor.
 */
public class OpenWithSystemEditorAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(OpenWithSystemEditorAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.file.openWithSystemEditor";
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			IEditorPart editorPart = window.getActivePage().getActiveEditor();
			if (editorPart instanceof ICollectionEditor) {
				ICollectionEditor fileCollectionEditor = (ICollectionEditor) editorPart;
				Set<FileDescriptor> fileDescriptors = fileCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
				
				// open all selected files
				boolean allFilesExist = true;
				for (FileDescriptor fileDescriptor : fileDescriptors) {
					if (fileDescriptor.getFile().exists()) {
						File file = fileDescriptor.getFile();
						Program.launch(file.getAbsolutePath());
					}
					else {
						allFilesExist = false;
					}
				}
				
				if (!allFilesExist) {
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
