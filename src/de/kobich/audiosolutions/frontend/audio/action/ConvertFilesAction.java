package de.kobich.audiosolutions.frontend.audio.action;

import java.io.File;
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
import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.convert.AudioConversionService;
import de.kobich.audiosolutions.core.service.convert.FfmpegConversionOptions;
import de.kobich.audiosolutions.core.service.convert.IAudioConversionOptions;
import de.kobich.audiosolutions.core.service.convert.LameMP3ConversionOptions;
import de.kobich.audiosolutions.core.service.convert.LameMP3ConversionOptions.AudioEncodingMode;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.ColouredMessageConsoleStream;
import de.kobich.audiosolutions.frontend.common.action.JobResultAction;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;
import de.kobich.audiosolutions.frontend.common.util.PlatformUtil;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

/**
 * Action to encode files.
 * 
 */
public class ConvertFilesAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(ConvertFilesAction.class);
	public static final String FORMAT_PARAM = "de.kobich.audiosolutions.commands.audio.convertFiles.format";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String formatParam = event.getParameter(FORMAT_PARAM);
		final AudioFormat audioFormat = AudioFormat.getAudioFormat(formatParam).orElse(null);
		if (audioFormat == null) {
			return null;
		}

		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		final AudioCollectionEditor audioCollectionEditor = SelectionSupport.INSTANCE.getActiveEditor(AudioCollectionEditor.class);
		if (audioCollectionEditor == null) {
			return null;
		}
		final Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
		if (fileDescriptors.isEmpty()) {
			return null;
		}
		
		final AudioConversionService audioConversionService = AudioSolutions.getService(AudioConversionService.class);
		final CommandLineTool tool = audioConversionService.mayConvert(fileDescriptors, audioFormat);

		IConvertFilesOptionPanel options = null;
		if (AudioTool.FFMPEG.equals(tool)) {
			options = new ConvertFilesFfmpegOptionPanel();
		}
		else if (AudioTool.LAME_MP3.equals(tool)) {
			options = new ConvertFilesLameOptionPanel();
		}
		final ConvertFilesDialog dialog = ConvertFilesDialog.createConvertFilesDialog(window.getShell(), fileDescriptors, audioFormat, tool, options);
		int status = dialog.open();
		if (status == IDialogConstants.OK_ID) {
			List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Convert Files", window.getShell(), states) {
				private CommandLineStreams streams;
				private File targetDirectory;
				private IAudioConversionOptions convertionOptions;
				private AudioFileResult result;
				
				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case UI_1:
						// streams
						MessageConsole console = PlatformUtil.createNewConsole("Convert to " + audioFormat.name());
						ColouredMessageConsoleStream out = ColouredMessageConsoleStream.getStandardConsoleStream(console);
						ColouredMessageConsoleStream err = ColouredMessageConsoleStream.getErrorConsoleStream(console);
						InputStream commandDefinitionStream = AudioSolutions.getCommandDefinitionStream(tool);
						this.streams = new CommandLineStreams(out, err);
						this.streams.setCommandDefinitionStream(commandDefinitionStream);
						// target dir
						if (!dialog.isUseSourceDir()) {
							this.targetDirectory = dialog.getTargetDirectoryFile();
						}
						// options
						this.convertionOptions = getConversionOptions(audioFormat, dialog);

						break;
					case WORKER_1:
						result = audioConversionService.convert(fileDescriptors, targetDirectory, audioFormat, convertionOptions, tool, streams);
						Job job = super.getJob();
						if (!super.isJobModal()) {
							job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
							job.setProperty(IProgressConstants.ACTION_PROPERTY, new JobResultAction(super.getName(), super.getParent(), result));
						}
						break;
					case UI_2:
						if (super.isJobModal() || !result.getFailedFiles().isEmpty()) {
							FileResultDialog d = FileResultDialog.createDialog(super.getParent(), super.getName(), result);
							d.open();
						}
						UIEvent event = new UIEvent(ActionType.FILE, null);
						event.getFileDelta().copyFromResult(result);
						EventSupport.INSTANCE.fireEvent(event);
						break;
					case UI_ERROR:
						Exception e = super.getException();
						logger.error(e.getMessage(), e);
						String msg = "Conversion error: " + e.getMessage() + "\nPlease check your command definition.";
						MessageDialog.openError(window.getShell(), super.getName(), msg);
						break;
					default: 
						break;
					}
					
				}
			};
			runner.runBackgroundJob(0, true, false, Activator.getDefault().getImageDescriptor("/icons/audio/audio-convert.png"));
		}

		return null;
	}
	
	private IAudioConversionOptions getConversionOptions(AudioFormat audioFormat, ConvertFilesDialog dialog) {
		IConvertFilesOptionPanel optionPanel = dialog.getOptionPanel().orElse(null);
		if (optionPanel == null) {
			return null;
		}
		
		if (optionPanel instanceof ConvertFilesLameOptionPanel) {
			ConvertFilesLameOptionPanel mp3OptionPanel = (ConvertFilesLameOptionPanel) optionPanel;
			
			LameMP3ConversionOptions mp3ConversionOptions = null;
			AudioEncodingMode encodingMode = mp3OptionPanel.getEncodingMode();
			if (AudioEncodingMode.CONSTANT_BITRATE.equals(encodingMode)) {
				int cbrBitrate = mp3OptionPanel.getCbrBitrate();
				mp3ConversionOptions = new LameMP3ConversionOptions(cbrBitrate);
			}
			else if (AudioEncodingMode.AVERAGE_BITRATE.equals(encodingMode)) {
				int abrBitrate = mp3OptionPanel.getAbrBitrate();
				mp3ConversionOptions = new LameMP3ConversionOptions(abrBitrate, mp3OptionPanel.getAbrMaxBitrate());

			}
			else if (AudioEncodingMode.VARIABLE_BITRATE.equals(encodingMode)) {
				int vbrQuality = mp3OptionPanel.getVbrQuality();
				mp3ConversionOptions = new LameMP3ConversionOptions(vbrQuality, mp3OptionPanel.getVbrMinBitrate(), mp3OptionPanel.getVbrMaxBitrate());
			}
			mp3ConversionOptions.setMono(mp3OptionPanel.isMono());
			return mp3ConversionOptions;
		}
		else if (optionPanel instanceof ConvertFilesFfmpegOptionPanel) {
			ConvertFilesFfmpegOptionPanel ffmpegOptionPanel = (ConvertFilesFfmpegOptionPanel) optionPanel;
			return new FfmpegConversionOptions(ffmpegOptionPanel.getQuality());
		}
		return null;
	}

}
