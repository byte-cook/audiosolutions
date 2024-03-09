package de.kobich.audiosolutions.frontend.audio.view.id3.action;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.mp3.id3.ID3TagVersion;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.mp3.id3.WriteID3TagsByAudioDataRequest;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.view.id3.ID3TagView;
import de.kobich.audiosolutions.frontend.audio.view.id3.ID3TagViewPredicate;
import de.kobich.audiosolutions.frontend.common.FileDescriptorConverter;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;
import de.kobich.commons.converter.ConverterUtils;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

/**
 * Adds audio data to files.
 * 
 */
public class SetID3TagsByAudioDataAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(SetID3TagsByAudioDataAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		AudioCollectionEditor audioCollectionEditor = SelectionSupport.INSTANCE.getActiveEditor(AudioCollectionEditor.class);
		if (audioCollectionEditor == null) {
			return null;
		}
		
		final Set<FileDescriptor> fileDescriptors = new HashSet<>(CollectionUtils.select(audioCollectionEditor.getFileDescriptorSelection().getExistingFiles(), ID3TagViewPredicate.INSTANCE));
		if (fileDescriptors.isEmpty()) {
			return null;
		}

		List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("MP3 ID3 Tags", window.getShell(), states) {
			private AudioFileResult result;

			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case WORKER_1:
					WriteID3TagsByAudioDataRequest request = new WriteID3TagsByAudioDataRequest(fileDescriptors, ID3TagVersion.ID3_V2);
					request.setProgressMonitor(super.getProgressMonitor());
					IFileID3TagService id3TagService = AudioSolutions.getService(IFileID3TagService.JAUDIO_TAGGER, IFileID3TagService.class);
					result = id3TagService.writeID3TagsByAudioData(request);
					break;
				case UI_2:
					// show failed dialog
					if (!result.getFailedFiles().isEmpty()) {
						// run asynchronously
						super.getParent().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								FileResultDialog dialog = FileResultDialog.createDialog(getParent(), getName(), "ID3 tag could not be written.", result.getFailedFiles());
								dialog.open();
							}
						});
					}

					ID3TagView id3TagView = (ID3TagView) window.getActivePage().findView(ID3TagView.ID);
					if (id3TagView != null) {
						id3TagView.fireSelection(fileDescriptors);
					}

					UIEvent event = new UIEvent(ActionType.FILE);
					Collection<File> updatedFiles = ConverterUtils.convert(result.getSucceededFiles(), FileDescriptorConverter.INSTANCE);
					event.getFileDelta().getModifyItems().addAll(updatedFiles);
					EventSupport.INSTANCE.fireEvent(event);
					break;
				case UI_ERROR:
					if (super.getProgressMonitor().isCanceled()) {
						return;
					}
					Exception e = super.getException();
					logger.error(e.getMessage(), e);
					MessageDialog.openError(super.getParent(), super.getName(), "Operation failed: \n" + e.getMessage());
					break;
				default:
					break;
				}
			}
		};
		runner.runProgressMonitorDialog(true, false);
			
		return null;
	}

}
