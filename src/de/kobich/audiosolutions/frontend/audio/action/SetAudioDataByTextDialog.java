package de.kobich.audiosolutions.frontend.audio.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioDataChange.AudioDataChangeBuilder;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.audio.AudioAttributeConverter;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;
import lombok.Getter;

public class SetAudioDataByTextDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final String STATE_ATTRIBUTE = "attribute";
	private final List<FileDescriptor> fileDescriptors;
	private final String title;
	private final String detailsMessage;
	private Combo attributeCombo;
	private Text previewFileText;
	private StyledText valueListText;
	@Getter
	private Set<AudioDataChange> changes;
	private AudioAttribute attribute;
	private final BulletText bulletText;

	public SetAudioDataByTextDialog(Shell parentShell, Set<FileDescriptor> fileDescriptors) {
		super(parentShell);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		this.title = "Set Audio Data By Text";
		this.detailsMessage = "Choose audio attribute to set and input audio data line by line for the corresponding file.";
		this.fileDescriptors = new ArrayList<FileDescriptor>(fileDescriptors);
		Collections.sort(this.fileDescriptors, new DefaultFileDescriptorComparator());
		this.bulletText = new BulletText(this.fileDescriptors);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		this.setTitle(detailsMessage);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		JFaceUtils.createHorizontalSeparator(parent, 1);

		Composite main = new Composite(parent, SWT.NONE);
		GridLayout parentLayout = new GridLayout();
		parentLayout.marginWidth = parentLayout.marginHeight = 5;
		main.setLayout(parentLayout);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite info = new Composite(main, SWT.NONE);
		GridLayout infoLayout = new GridLayout(3, false);
		info.setLayout(infoLayout);
		info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label attributeLabel = new Label(info, SWT.NONE);
		attributeLabel.setText("Audio Attribute: ");
		attributeCombo = new Combo(info, SWT.READ_ONLY);
		attributeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (AudioAttribute att : Arrays.asList(AudioAttribute.TRACK, AudioAttribute.TRACK_NO, AudioAttribute.ALBUM, AudioAttribute.ARTIST)) {
			attributeCombo.add(AudioAttributeConverter.INSTANCE.convert(att));
		}
		attributeCombo.setText(AudioAttributeConverter.INSTANCE.convert(AudioAttribute.TRACK));
		Button setValueButton = new Button(info, SWT.PUSH);
		setValueButton.setText("Set Values");
		setValueButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				setValues();
			}
		});
		Label previewLabel = new Label(info, SWT.NONE);
		previewLabel.setText("Associated File:");
		previewFileText = new Text(info, SWT.BORDER | SWT.READ_ONLY);
		previewFileText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 2, 1));
		previewFileText.setBackground(JFaceUtils.getDisabledTextBackgroundColor());

		// Group
		Composite group = new Composite(main, SWT.NONE);
		GridLayout groupLayout = new GridLayout(1, true);
		group.setLayout(groupLayout);
		GridData data = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(data);
		
		Label valueListLabel = new Label(group, SWT.NONE);
		valueListLabel.setText("Values:");
		
		valueListText = new StyledText(group, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		valueListText.setLayoutData(gd);
		valueListText.setFocus();
		valueListText.addLineStyleListener(new LineStyleListener() {
			@Override
			public void lineGetStyle(LineStyleEvent event) {
				int line = valueListText.getLineAtOffset(event.lineOffset);

				StyleRange style = new StyleRange();
				double aw = new GC(valueListText).getFontMetrics().getAverageCharacterWidth();
				double width = bulletText.getMaxLength() * aw + 20;
				style.metrics = new GlyphMetrics(0, 0, (int) width);
				Bullet bullet = new Bullet(ST.BULLET_TEXT, style);
				bullet.text = bulletText.get(line);
				
				event.bullet = bullet;
				event.bulletIndex = line;
			}
		});
		valueListText.addExtendedModifyListener(new ExtendedModifyListener() {
			@Override
			public void modifyText(ExtendedModifyEvent event) {
				valueListText.redraw();
			}
		});
		valueListText.addLineBackgroundListener(new LineBackgroundListener() {
			@Override
			public void lineGetBackground(LineBackgroundEvent event) {
				int line = valueListText.getLineAtOffset(event.lineOffset);
				if (!bulletText.containsFile(line)) {
					event.lineBackground = SetAudioDataByTextDialog.this.getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY);
				}
			}
		});
		valueListText.addCaretListener(new CaretListener() {
			@Override
			public void caretMoved(CaretEvent event) {
				int lineNo = valueListText.getLineAtOffset(event.caretOffset);
				updatePreviewFile(lineNo);
			}
		});
		updatePreviewFile(0);
		
		JFaceUtils.createHorizontalSeparator(parent, 1);

		restoreState();
		return parent;
	}

	private void updatePreviewFile(int lineNo) {
		if (lineNo < fileDescriptors.size()) {
			String file = fileDescriptors.get(lineNo).getRelativePath();
			previewFileText.setText(file);
		}
		else {
			previewFileText.setText("<no file>");
		}
	}
	
	private void setValues() {
		AudioAttribute attribute = AudioAttributeConverter.INSTANCE.reconvert(attributeCombo.getText());
		
		StringBuilder sb = new StringBuilder();
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			String line = null;
			if (fileDescriptor.hasMetaData(AudioData.class)) {
				line = fileDescriptor.getMetaData(AudioData.class).getAttribute(attribute);
			}
			
			sb.append(line != null ? line : "");
			sb.append("\n");
		}
		
		if (StringUtils.isNotBlank(sb.toString())) {
			valueListText.setText(sb.toString());
		}
	}

	protected void okPressed() {
		// check
		if (valueListText.getText().isEmpty()) {
			this.setErrorMessage("No values to set");
			return;
		}
		
		// attribute
		this.attribute = AudioAttributeConverter.INSTANCE.reconvert(attributeCombo.getText());
		// file descriptor attribute
		this.changes = new HashSet<>();
		String values = valueListText.getText();
		String[] lines = values.split("\n");
		for (int i = 0; i < lines.length; ++ i) {
			String line = lines[i].trim();
			if (line.isEmpty() || i >= fileDescriptors.size()) {
				continue;
			}
			
			AudioDataChangeBuilder builder = AudioDataChange.builder();
			builder.fileDescriptor(fileDescriptors.get(i));
			
			switch (this.attribute) {
			case TRACK:
				builder.track(line);
				this.changes.add(builder.build());
				break;
			case TRACK_NO:
				Integer no = AudioAttributeUtils.convert2Integer(line);
				if (no != null) {
					builder.trackNo(no);
					this.changes.add(builder.build());
				}
				break;
			case ALBUM:
				builder.album(line);
				this.changes.add(builder.build());
				break;
			case ARTIST:
				builder.artist(line);
				this.changes.add(builder.build());
				break;
			default:
				break;
			}
		}

		saveState();
		super.okPressed();
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, SetAudioDataByTextDialog.class.getName());
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		String attribute = this.attributeCombo.getText();
		mementoItem.putString(STATE_ATTRIBUTE, attribute);
	}

	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		String attribute = mementoItem.getString(STATE_ATTRIBUTE, "track");
		this.attributeCombo.setText(attribute);
	}

	/**
	 * Helper class to return bullet text
	 *
	 */
	private class BulletText extends ArrayList<String> {
		private static final long serialVersionUID = -3466918917866713489L;
		private final int maxLength;
		
		public BulletText(List<FileDescriptor> fileDescriptors) {
			int max = 0;
			for (FileDescriptor file : fileDescriptors) {
				String text = file.getFileName();
				super.add(text);
				max = Math.max(max, text.length());
			}
			this.maxLength = max;
		}
		
		public int getMaxLength() {
			return maxLength;
		}
		
		public boolean containsFile(int index) {
			return index < super.size();
		}

		/* (non-Javadoc)
		 * @see java.util.ArrayList#get(int)
		 */
		@Override
		public String get(int index) {
			if (index < super.size()) {
				return super.get(index);
			}
			return "<no file>";
		}
	}
}
