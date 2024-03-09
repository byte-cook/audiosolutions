package de.kobich.audiosolutions.frontend.audio.action;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.component.file.FileDescriptor;


/**
 * Sets audio data to files by structure.
 */
public class SetAudioDataByCDDBAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(SetAudioDataByCDDBAction.class); 
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			final AudioCollectionEditor audioCollectionEditor = SelectionSupport.INSTANCE.getActiveEditor(AudioCollectionEditor.class);
			if (audioCollectionEditor == null) {
				return null;
			}
			final Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
			if (fileDescriptors.isEmpty()) {
				return null;
			}
				
			CDDBWizardDialog dialog = new CDDBWizardDialog(window.getShell());
			dialog.setFileDescriptors(fileDescriptors);
			dialog.setAudioCollectionEditor(audioCollectionEditor);
			dialog.open();
		} catch (Exception e) {
			String msg = "Error while adding audio data: ";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Error", msg + e.getMessage());
		}
		return null;
	}
	
}
