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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute2StructureVariableMapper;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.mp3.id3.ID3TagVersion;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.mp3.id3.WriteID3TagsByStructureRequest;
import de.kobich.audiosolutions.frontend.audio.view.id3.ID3TagView;
import de.kobich.audiosolutions.frontend.audio.view.id3.ID3TagViewPredicate;
import de.kobich.audiosolutions.frontend.common.FileDescriptorConverter;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;
import de.kobich.audiosolutions.frontend.common.ui.StructureDialog;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.commons.converter.ConverterUtils;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressData;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

/**
 * Adds id3 tags to files by structure.
 * 
 */
public class SetID3TagsByStructureAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(SetID3TagsByStructureAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final ICollectionEditor fileCollectionEditor = SelectionSupport.INSTANCE.getActiveEditor(ICollectionEditor.class);
		if (fileCollectionEditor == null) {
			return null;
		}
		
		final Set<FileDescriptor> fileDescriptors = new HashSet<>(CollectionUtils.select(fileCollectionEditor.getFileDescriptorSelection().getExistingFiles(), ID3TagViewPredicate.INSTANCE));
		if (fileDescriptors.isEmpty()) {
			return null;
		}

		StructureDialog dialog = StructureDialog.createID3TagsDialog(window.getShell());
		dialog.setMapper(AudioAttribute2StructureVariableMapper.getInstance());
		dialog.setPreviewFileDescriptors(fileDescriptors);
		int status = dialog.open();
		if (status == IDialogConstants.OK_ID) {
			final String fileStructure = dialog.getFileStructure();

			List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("MP3 ID3 Tags", window.getShell(), states) {
				private AudioFileResult result;

				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case WORKER_1:
						IServiceProgressMonitor progressMonitor = super.getProgressMonitor();
						try {
							progressMonitor.beginTask(new ProgressData("Write ID3 tags...", fileDescriptors.size()));
							WriteID3TagsByStructureRequest request = new WriteID3TagsByStructureRequest(fileDescriptors, ID3TagVersion.ID3_V2,
									fileStructure);
							request.setProgressMonitor(progressMonitor);
							IFileID3TagService id3TagService = AudioSolutions.getService(IFileID3TagService.JAUDIO_TAGGER, IFileID3TagService.class);
							result = id3TagService.writeID3TagsByStructure(request);
						}
						finally {
							progressMonitor.endTask(new ProgressData("ID3 tags successfully written"));
						}
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

		}
		return null;
	}

}
