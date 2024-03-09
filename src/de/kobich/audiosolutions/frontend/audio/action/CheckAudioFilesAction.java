package de.kobich.audiosolutions.frontend.audio.action;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
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
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.check.AudioCheckService;
import de.kobich.audiosolutions.core.service.check.CheckAudioFilesOptions;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.ColouredMessageConsoleStream;
import de.kobich.audiosolutions.frontend.common.action.JobResultAction;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;
import de.kobich.audiosolutions.frontend.common.util.PlatformUtil;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

/**
 * Checks audio files.
 * 
 */
public class CheckAudioFilesAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(CheckAudioFilesAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		final AudioCollectionEditor audioCollectionEditor = SelectionSupport.INSTANCE.getActiveEditor(AudioCollectionEditor.class);
		if (audioCollectionEditor == null) {
			return null;
		}
		final Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
		if (fileDescriptors.isEmpty()) {
			return null;
		}

		final AudioCheckService audioCheckService = AudioSolutions.getService(AudioCheckService.class);
		final CommandLineTool tool = audioCheckService.mayCheckAudioFiles(fileDescriptors);
		
		CheckAudioFilesDialog dialog = new CheckAudioFilesDialog(window.getShell(), tool);
		int status = dialog.open();
		if (status == IDialogConstants.OK_ID) {
			final CheckAudioFilesOptions options = dialog.getOptions();
			
			List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Checking Audio Files", window.getShell(), states) {
				private CommandLineStreams streams;
				private AudioFileResult result;
				
				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case UI_1:
						MessageConsole console = PlatformUtil.createNewConsole("Check audio files");
						ColouredMessageConsoleStream out = ColouredMessageConsoleStream.getStandardConsoleStream(console);
						ColouredMessageConsoleStream err = ColouredMessageConsoleStream.getErrorConsoleStream(console);
						InputStream commandDefinitionStream = AudioSolutions.getCommandDefinitionStream(tool);
						this.streams = new CommandLineStreams(out, err);
						this.streams.setCommandDefinitionStream(commandDefinitionStream);
						break;
					case WORKER_1:
						result = audioCheckService.checkAudioFiles(fileDescriptors, options, tool, streams);
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
						// files will not be changed
//						UIEvent event = new UIEvent(UIEventType.FILE_SYSTEM_CHANGED);
//						Collection<File> updatedFiles = ConverterUtils.convert(result.getSucceededFiles(), FileDescriptorConverter.INSTANCE);
//						event.getFileDelta().getUpdateItems().addAll(updatedFiles);
//						EventSupport.INSTANCE.fireEvent(event);
						break;
					case UI_ERROR:
						Exception e = super.getException();
						logger.error(e.getMessage(), e);
						String msg = e.getMessage();
						MessageDialog.openError(window.getShell(), super.getName(), msg);
						break;
					default: 
						break;
					}
					
				}
			};
			runner.runBackgroundJob(0, true, false, Activator.getDefault().getImageDescriptor("/icons/audio/audio-check-files.png"));
		}

		return null;
	}

}
