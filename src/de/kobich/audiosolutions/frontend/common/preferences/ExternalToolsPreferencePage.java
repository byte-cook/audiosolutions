package de.kobich.audiosolutions.frontend.common.preferences;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.check.AudioCheckService;
import de.kobich.audiosolutions.core.service.convert.AudioConversionService;
import de.kobich.audiosolutions.core.service.normalize.AudioNormalizationService;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.ui.jface.preference.ButtonFieldEditor;
import de.kobich.commons.ui.jface.preference.LabelFieldEditor;
import de.kobich.commons.ui.jface.preference.SeparatorFieldEditor;

public class ExternalToolsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public ExternalToolsPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		super.noDefaultAndApplyButton();
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		super.addField(new LabelFieldEditor("Configure external tools by editing definition files.", parent));
		
		// lame
		ExternalToolsFieldEditor lameMp3Tool = new ExternalToolsFieldEditor(AudioTool.LAME_MP3, "High quality MPEG Audio Layer III (MP3) encoder", parent);
		lameMp3Tool.addSelectionListener(new ConversionToolSelectionAdapter(parent.getShell(), AudioTool.LAME_MP3));
		super.addField(lameMp3Tool);
		ExternalToolsFieldEditor lameWavTool = new ExternalToolsFieldEditor(AudioTool.LAME_WAV, "High quality WAV encoder", parent);
		lameWavTool.addSelectionListener(new ConversionToolSelectionAdapter(parent.getShell(), AudioTool.LAME_WAV));
		super.addField(lameWavTool);
		// ffmpeg
		ExternalToolsFieldEditor convertTool = new ExternalToolsFieldEditor(AudioTool.FFMPEG, "Hyper fast Audio and Video encoder", parent);
		convertTool.addSelectionListener(new ConversionToolSelectionAdapter(parent.getShell(), AudioTool.FFMPEG));
		super.addField(convertTool);
		// avconv
//		ExternalToolsFieldEditor convertTool = new ExternalToolsFieldEditor(AudioTool.AVCONV, "Very fast video and audio converter", parent);
//		convertTool.addSelectionListener(new ConversionToolSelectionAdapter(parent.getShell(), AudioTool.AVCONV));
//		super.addField(convertTool);
		// mp3gain
		ExternalToolsFieldEditor mp3gainTool = new ExternalToolsFieldEditor(AudioTool.MP3GAIN, "Lossless mp3 normalizer", parent);
		mp3gainTool.addSelectionListener(new NormalizeToolSelectionAdapter(parent.getShell(), AudioTool.MP3GAIN));
		super.addField(mp3gainTool);
		// mp3check
		ExternalToolsFieldEditor mp3checkTool = new ExternalToolsFieldEditor(AudioTool.MP3CHECK, "Checks mp3 files", parent);
		mp3checkTool.addSelectionListener(new CheckToolSelectionAdapter(parent.getShell(), AudioTool.MP3CHECK));
		super.addField(mp3checkTool);
		
		// directory
		super.addField(new SeparatorFieldEditor(parent));
		ButtonFieldEditor buttonFieldEditor = new ButtonFieldEditor("Open Command Definition Directory", parent);
		buttonFieldEditor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				File file = AudioSolutions.getCommandDefinitionDir();
				IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
				IEditorDescriptor editorDescriptor = editorRegistry.getDefaultEditor(file.getAbsolutePath());
				if (editorDescriptor == null) {
					if (!Program.launch(file.getAbsolutePath())) {
						MessageDialog.openError(parent.getShell(), "Info", "No suitable editor could be found for:\n" + file.getAbsolutePath());
					}
				}
			}
		});
		super.addField(buttonFieldEditor);
	}

	private static class ConversionToolSelectionAdapter extends SelectionAdapter {
		private final Shell parent;
		private final CommandLineTool tool;
		
		public ConversionToolSelectionAdapter(Shell parent, CommandLineTool tool) {
			this.parent = parent;
			this.tool = tool;
		}
		
		public void widgetSelected(SelectionEvent e) {
			try {
				FileDialog dialog = new FileDialog(parent);
				dialog.setText("Command Definition File: " + tool.getFileName());
//				dialog.setFileName(tool.getFileName());
				dialog.setFilterPath(AudioSolutions.getCommandDefinitionDir().getAbsolutePath());
				String file = dialog.open();
				if (file != null) {
					AudioConversionService audioConversionService = AudioSolutions.getService(AudioConversionService.class);
					audioConversionService.copyInternalCommandDefinition(tool, new File(file));
				}
			}
			catch (AudioException exc) {
				MessageDialog.openError(parent, parent.getText(), "Copy failed.\n" + exc.getMessage());
			}
		}
	};
	
	private static class NormalizeToolSelectionAdapter extends SelectionAdapter {
		private final Shell parent;
		private final CommandLineTool tool;
		
		public NormalizeToolSelectionAdapter(Shell parent, CommandLineTool tool) {
			this.parent = parent;
			this.tool = tool;
		}

		public void widgetSelected(SelectionEvent e) {
			try {
				FileDialog dialog = new FileDialog(parent.getShell());
				dialog.setText("Command Definition File: " + tool.getFileName());
				dialog.setFilterPath(AudioSolutions.getCommandDefinitionDir().getAbsolutePath());
//				dialog.setFileName(tool.getFileName());
				String file = dialog.open();
				if (file != null) {
					AudioNormalizationService audioNormalizationService = AudioSolutions.getService(AudioNormalizationService.class);
					audioNormalizationService.copyInternalCommandDefinition(tool, new File(file));
				}
			}
			catch (AudioException exc) {
				MessageDialog.openError(parent, parent.getText(), "Copy failed.\n" + exc.getMessage());
			}
		}
	}
	
	private static class CheckToolSelectionAdapter extends SelectionAdapter {
		private final Shell parent;
		private final CommandLineTool tool;
		
		public CheckToolSelectionAdapter(Shell parent, CommandLineTool tool) {
			this.parent = parent;
			this.tool = tool;
		}

		public void widgetSelected(SelectionEvent e) {
			try {
				FileDialog dialog = new FileDialog(parent.getShell());
				dialog.setText("Command Definition File: " + tool.getFileName());
				dialog.setFilterPath(AudioSolutions.getCommandDefinitionDir().getAbsolutePath());
//				dialog.setFileName(tool.getFileName());
				String file = dialog.open();
				if (file != null) {
					AudioCheckService audioCheckService = AudioSolutions.getService(AudioCheckService.class);
					audioCheckService.copyInternalCommandDefinition(tool, new File(file));
				}
			}
			catch (AudioException exc) {
				MessageDialog.openError(parent, parent.getText(), "Copy failed.\n" + exc.getMessage());
			}
		}
	}
}
