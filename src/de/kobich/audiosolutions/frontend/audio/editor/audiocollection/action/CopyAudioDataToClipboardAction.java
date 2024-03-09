package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.action;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.clipboard.AudioClipboardContentService;
import de.kobich.audiosolutions.core.service.clipboard.AudioClipboardContentType;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.commons.ui.jface.StatusLineUtils;
import de.kobich.component.file.FileDescriptor;

/**
 * Action to set editor layout.
 */
public class CopyAudioDataToClipboardAction extends AbstractHandler {
	public static final String TYPE_PARAM = "de.kobich.audiosolutions.commands.audio.clipboardType";

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

		String type = event.getParameter(TYPE_PARAM);
		final IEditorPart editorPart = window.getActivePage().getActiveEditor();
		if (editorPart instanceof AudioCollectionEditor) {
			AudioCollectionEditor audioCollectionEditor = (AudioCollectionEditor) editorPart;
			Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();

			if (fileDescriptors.isEmpty()) {
				// select all
				fileDescriptors = audioCollectionEditor.getFileCollection().getFileDescriptors();
			}

			final AudioClipboardContentType contentType = AudioClipboardContentType.getByName(type);
			if (contentType != null) {
				final Set<FileDescriptor> fileDescriptorsF = fileDescriptors;
				
				List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
				JFaceThreadRunner runner = new JFaceThreadRunner("Copy To Clipboard", window.getShell(), states) {
					private String content;
					
					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
						case WORKER_1:
							AudioClipboardContentService clipboardContentService = AudioSolutions.getService(AudioClipboardContentService.class);
							this.content = clipboardContentService.getClipboardContent(fileDescriptorsF, contentType);
							break;
						case UI_2:
							if (StringUtils.isNotEmpty(content)) {
								// copy to clipboard
								Clipboard cb = new Clipboard(Display.getDefault());
								TextTransfer textTransfer = TextTransfer.getInstance();
								cb.setContents(new Object[] { content }, new Transfer[] { textTransfer });
								
								String msg = "Copy to clipboard succeeded.";
								StatusLineUtils.setStatusLineMessage(editorPart, msg, false);
							}
							else {
								String msg = "No data available to copy into clipboard.";
								MessageDialog.openError(super.getParent(), "Clipboard", msg);
							}
							break;
						default: 
							break;
						}
					}
				};
				runner.runProgressMonitorDialog(true, false);
			}
		}
		return null;
	}

}
