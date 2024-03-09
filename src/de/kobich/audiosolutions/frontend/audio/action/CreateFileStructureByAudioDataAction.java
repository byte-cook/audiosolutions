package de.kobich.audiosolutions.frontend.audio.action;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttribute2StructureVariableMapper;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.io.AudioIOService;
import de.kobich.audiosolutions.core.service.io.CreateFileStructureByAudioDataRequest;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressData;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.io.FileCreationType;

/**
 * Creates file structure by audio data.
 * 
 */
public class CreateFileStructureByAudioDataAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(CreateFileStructureByAudioDataAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		final AudioCollectionEditor audioCollectionEditor = SelectionSupport.INSTANCE.getActiveEditor(AudioCollectionEditor.class);
		if (audioCollectionEditor == null) {
			return null;
		}
		final Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
		if (fileDescriptors.isEmpty()) {
			return null;
		}

		final AudioAttribute2StructureVariableMapper mapper = AudioAttribute2StructureVariableMapper.getInstance();
		CreateFileStructureByAudioDataDialog dialog = CreateFileStructureByAudioDataDialog.createDialog(window.getShell());
		dialog.setPreviewFileDescriptors(fileDescriptors);
		dialog.setMapper(mapper);

		int status = dialog.open();
		if (status == IDialogConstants.OK_ID) {
			final File rootDirectory = dialog.getTargetDirectory();
			final String filePattern = dialog.getFilePattern();
			final FileCreationType type = dialog.getCreationType();

			List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Create File Structure", window.getShell(), states) {
				private AudioFileResult result;

				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case UI_1:
						break;
					case WORKER_1:
						IServiceProgressMonitor progressMonitor = super.getProgressMonitor();
						try {
							progressMonitor.beginTask(new ProgressData("Create File Structure...", fileDescriptors.size()));
							Map<StructureVariable, AudioAttribute> variableMap = mapper.getMap();
							CreateFileStructureByAudioDataRequest request = new CreateFileStructureByAudioDataRequest(fileDescriptors,
									rootDirectory, filePattern, variableMap);
							request.setType(type);
							request.setProgressMonitor(progressMonitor);

							AudioIOService audioIOService = AudioSolutions.getService(AudioIOService.class);
							result = audioIOService.createFileStructureByAudioData(request);
						}
						finally {
							progressMonitor.endTask(new ProgressData("File Structure created"));
						}
						break;
					case UI_2:
						UIEvent event = new UIEvent(ActionType.FILE, audioCollectionEditor);
						event.getEditorDelta().copyFromResult(result);
						event.getFileDelta().copyFromResult(result);
						EventSupport.INSTANCE.fireEvent(event);
						break;
					case UI_ERROR:
						Exception e = super.getException();
						logger.error(e.getMessage(), e);
						MessageDialog.openError(super.getParent(), super.getName(), "Operation failed: \n" + e.getMessage());
						break;
					default: 
						break;
					}
				}
			};
			runner.runProgressMonitorDialog(true, true);
		}
		
		return null;
	}

}
