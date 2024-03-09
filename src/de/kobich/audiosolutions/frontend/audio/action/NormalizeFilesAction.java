package de.kobich.audiosolutions.frontend.audio.action;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.normalize.AudioNormalizationService;
import de.kobich.audiosolutions.core.service.normalize.IAudioNormalizationOptions;
import de.kobich.audiosolutions.core.service.normalize.mp3.MP3GainNormalizationOptions;
import de.kobich.audiosolutions.core.service.normalize.mp3.MP3GainNormalizationOptions.AudioNormalizingMode;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.ColouredMessageConsoleStream;
import de.kobich.audiosolutions.frontend.common.FileDescriptorConverter;
import de.kobich.audiosolutions.frontend.common.action.JobResultAction;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.util.PlatformUtil;
import de.kobich.commons.converter.ConverterUtils;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

/**
 * Action to normalize files.
 */
public class NormalizeFilesAction extends AbstractHandler { 

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		// get file descriptors
		final ICollectionEditor collectionEditor = SelectionSupport.INSTANCE.getActiveEditor(ICollectionEditor.class);
		if (collectionEditor == null) {
			return null;
		}
		final Set<FileDescriptor> fileDescriptors = collectionEditor.getFileDescriptorSelection().getFileDescriptors();
		if (fileDescriptors.isEmpty()) {
			return null;
		}
		
		// get encoding service
		final AudioNormalizationService normalizationService = AudioSolutions.getService(AudioNormalizationService.class);
		final CommandLineTool tool = normalizationService.mayNormalize(fileDescriptors);
		
		final NormalizeFilesDialog dialog = NormalizeFilesDialog.createNormalizeDialog(window.getShell(), fileDescriptors, tool);
		int status = dialog.open();
		if (status == IDialogConstants.OK_ID) {
			
			List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Normalize Files", window.getShell(), states) {
				private CommandLineStreams streams;
				private IAudioNormalizationOptions options;
				private AudioFileResult result;
				
				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case UI_1:
						// streams
						MessageConsole console = PlatformUtil.createNewConsole("MP3 Gain");
						ColouredMessageConsoleStream out = ColouredMessageConsoleStream.getStandardConsoleStream(console);
						ColouredMessageConsoleStream err = ColouredMessageConsoleStream.getErrorConsoleStream(console);
						this.streams = new CommandLineStreams(out, err);
						InputStream commandDefinitionStream = AudioSolutions.getCommandDefinitionStream(AudioTool.MP3GAIN);
						this.streams.setCommandDefinitionStream(commandDefinitionStream);
						// option
						float suggestedDecibel = dialog.getSuggestedDecibel();
						AudioNormalizingMode normalizingMode = dialog.getNormalizingMode();
						MP3GainNormalizationOptions mp3GainOptions = new MP3GainNormalizationOptions(normalizingMode, suggestedDecibel);
						mp3GainOptions.setLowerGainInsteadOfClipping(dialog.isLowerGainInsteadOfClipping());
						this.options = mp3GainOptions;
						break;
					case WORKER_1:
						result = normalizationService.normalize(fileDescriptors, options, tool, streams);
						Job job = super.getJob();
						if (!super.isJobModal()) {
							job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
							job.setProperty(IProgressConstants.ACTION_PROPERTY, new JobResultAction(super.getName(), super.getParent(), result));
						}
						break;
					case UI_2:
						if (super.isJobModal()) {
							FileResultDialog d = FileResultDialog.createDialog(super.getParent(), super.getName(), result);
							d.open();
						}
						UIEvent event = new UIEvent(ActionType.FILE);
						Collection<File> updatedFiles = ConverterUtils.convert(result.getSucceededFiles(), FileDescriptorConverter.INSTANCE);
						event.getFileDelta().getModifyItems().addAll(updatedFiles);
						EventSupport.INSTANCE.fireEvent(event);
						break;
					case UI_ERROR:
						Exception e = super.getException();
						String msg = e.getMessage();
						MessageDialog.openError(super.getParent(), super.getName(), msg);
						break;
					default: 
						break;
					}
					
				}
			};
			runner.runBackgroundJob(0, true, false, Activator.getDefault().getImageDescriptor("/icons/audio/audio-normalize.png"));
		}
		
		return null;
	}
}
