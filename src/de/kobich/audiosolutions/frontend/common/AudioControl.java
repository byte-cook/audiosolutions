package de.kobich.audiosolutions.frontend.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioDataChange.AudioDataChangeBuilder;
import de.kobich.audiosolutions.core.service.RatingType;
import de.kobich.audiosolutions.frontend.audio.AudioDataContentProposalProvider;
import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.component.file.FileDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class AudioControl {
	private static final Logger logger = Logger.getLogger(AudioControl.class);
	private static final String KEEP_VALUE = "<keep-value>";
	private static final String DELETE_VALUE = "<delete-value>";
	
	@RequiredArgsConstructor
	public enum AudioControlType {
		MEDIUM("Medium:", AudioAttribute.MEDIUM),
		ARTIST("Artist:", AudioAttribute.ARTIST), 
		ALBUM("Album:", AudioAttribute.ALBUM), 
		ALBUM_PUBLICATION("Album Publication:", AudioAttribute.ALBUM_PUBLICATION),
		DISK("Disk:", AudioAttribute.DISK), 
		TRACK("Track:", AudioAttribute.TRACK), 
		TRACK_NO("Track Number:", AudioAttribute.TRACK_NO), 
		TRACK_FORMAT("Track Format:", AudioAttribute.TRACK_FORMAT), 
		GENRE("Genre:", AudioAttribute.GENRE), 
		RATING("Rating:", AudioAttribute.RATING);
		
		private final String label;
		private final AudioAttribute attribute;
	}

	private final AudioControlType type;
	private final Set<String> values = new HashSet<>();
	private int fileCountWithoutAudioData = 0;
	
	@Setter
	private String defaultValue;

	private Combo combo;
	private Text text;
	
//	public void addValue(String value) {
//		this.values.add(value);
//	}
	
	public void addValueByFileDescriptor(FileDescriptor fileDescriptor) {
		AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
		if (audioData == null) {
			++fileCountWithoutAudioData;
		}
		else {
			Optional<String> value = switch (type) {
				case MEDIUM -> getAudioDataByType(audioData, audioData::getMedium);
				case ARTIST -> getAudioDataByType(audioData, audioData::getArtist);
				case ALBUM -> getAudioDataByType(audioData, audioData::getAlbum);
				case ALBUM_PUBLICATION -> getAudioDataByType(audioData, () -> {
					Date publication = audioData.getAlbumPublication().orElse(null);
					return Optional.ofNullable(AudioAttributeUtils.convert2String(publication));
				});
				case DISK -> getAudioDataByType(audioData, audioData::getDisk);
				case TRACK -> getAudioDataByType(audioData, audioData::getTrack);
				case TRACK_FORMAT -> getAudioDataByType(audioData, audioData::getTrackFormat);
				case TRACK_NO -> getAudioDataByType(audioData, () -> {
					Integer no = audioData.getTrackNo().orElse(null);
					if (no == null) {
						return Optional.empty();
					}
					return Optional.ofNullable(String.valueOf(no));
				});
				case GENRE -> getAudioDataByType(audioData, audioData::getGenre);
				case RATING -> getAudioDataByType(audioData, () -> {
					RatingType rating = audioData.getRating().orElse(null);
					if (rating == null) {
						return Optional.empty();
					}
					return Optional.ofNullable(AudioAttributeUtils.convert2String(rating));
				});
			};
			
			if (value.isPresent()) {
				this.values.add(value.get());
			}
			else {
				++fileCountWithoutAudioData;
			}
		}
	}
	
	public void createLabel(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(this.type.label);

		if (type.attribute.isRequired()) {
			DecoratorUtils.createDecorator(label, "Attribute is required", FieldDecorationRegistry.DEC_REQUIRED, SWT.RIGHT | SWT.TOP);
		}
	}
	
	public Control createControl(Composite parent) {
		// special case for rating
		if (AudioControlType.RATING.equals(type)) {
			return createRatingControl(parent);
		}
		
		Control control;
		IControlContentAdapter contentAdapter;
		// different values are available
		if (this.values.size() > 1) {
			this.defaultValue = KEEP_VALUE;
			control = createCombo(parent, false);
			contentAdapter = new ComboContentAdapter();
		}
		// only one value is available
		else if (this.values.size() == 1) {
			if (fileCountWithoutAudioData == 0) {
				// all files with the same value
				this.defaultValue = this.values.iterator().next();
				control = createText(parent);
				contentAdapter = new TextContentAdapter();
			}
			else {
				// only one value but there are other files without values
				this.defaultValue = KEEP_VALUE;
				control = createCombo(parent, false);
				contentAdapter = new ComboContentAdapter();
			}
		}
		// no value is available
		else {
			control = createText(parent);
			contentAdapter = new TextContentAdapter();
		}
		
		if (type.attribute.isRequired()) {
			IContentProposalProvider proposalProvider = new AudioDataContentProposalProvider(type.attribute);
			KeyStroke keyStroke = null; 
			try {
				keyStroke = KeyStroke.getInstance("Ctrl+Space");
				DecoratorUtils.createDecorator(control, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
			}
			catch (ParseException exc) {
				logger.warn("Key stroke cannot be created", exc);
			}
			ContentProposalAdapter adapter = new ContentProposalAdapter(control, contentAdapter, proposalProvider, keyStroke, null);
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		}
		if (AudioControlType.ALBUM_PUBLICATION.equals(type)) {
			DecoratorUtils.createDecorator(control, "Format: YYYY-MM-DD", FieldDecorationRegistry.DEC_INFORMATION);
		}
		return control;
	}
	
	private Control createRatingControl(Composite parent) {
		if (this.values.size() == 1 && fileCountWithoutAudioData == 0) {
			this.defaultValue = this.values.iterator().next();
		}
		else {
			this.defaultValue = KEEP_VALUE;
		}
		// set all possible values
		this.values.clear();
		List.of(RatingType.values()).forEach(type -> this.values.add(type.name()));
		return createCombo(parent, true);
	}

	private Combo createCombo(Composite parent, boolean readOnly) {
		int style = SWT.BORDER;
		if (readOnly) {
			style |= SWT.READ_ONLY;
		}
		this.combo = new Combo(parent, style);
		GridData controlData = new GridData(SWT.FILL, SWT.NONE, true, false);
		controlData.widthHint = 150;
		combo.setLayoutData(controlData);
		
		List<String> valueList = new ArrayList<>(this.values);
		Collections.sort(valueList);
		valueList.add(0, DELETE_VALUE);
		valueList.add(0, KEEP_VALUE);
		for (String value : valueList) {
			combo.add(value);
		}
		
		if (StringUtils.isNotEmpty(this.defaultValue)) {
			combo.setText(this.defaultValue);
		}
		return combo;
	}
	
	private Text createText(Composite parent) {
        this.text = new Text(parent, SWT.BORDER);
        GridData controlData = new GridData(SWT.FILL, SWT.NONE, true, false); 
        controlData.widthHint = 150;
        text.setLayoutData(controlData);
        if (StringUtils.isNotEmpty(this.defaultValue)) {
            text.setText(this.defaultValue);
        }
        return text;
    }
	
	public String getValue() {
		if (combo != null) {
			return combo.getText();
		}
		else if (text != null) {
			return text.getText();
		}
		return null;
	}
	
	public boolean isDeleteValue() {
		if (combo != null) {
			return DELETE_VALUE.equals(getValue());
		}
		else if (text != null) {
			return StringUtils.isBlank(getValue());
		}
		return false;
	}
	
	public boolean isKeepValue() {
		if (combo != null) {
			return KEEP_VALUE.equals(getValue()) || Objects.equals(this.defaultValue, getValue());
		}
		else if (text != null) {
			return Objects.equals(this.defaultValue, getValue());
		}
		return true;
	}

	public void apply(AudioDataChangeBuilder change) {
		if (isKeepValue()) {
			return;
		}
		
		switch (type) {
		case MEDIUM:
			setAudioDataByType(getValue(), change::medium, change::mediumRemove);
			break;
		case ARTIST:
			setAudioDataByType(getValue(), change::artist, change::artistRemove);
			break;
		case ALBUM:
			setAudioDataByType(getValue(), change::album, change::albumRemove);
			break;
		case ALBUM_PUBLICATION:
			String publicationString = getValue();
			Date publication = AudioAttributeUtils.convert2Date(publicationString);
			setAudioDataByType(publication, change::albumPublication, change::albumPublicationRemove);
			break;
		case DISK:
			setAudioDataByType(getValue(), change::disk, change::diskRemove);
			break;
		case TRACK:
			setAudioDataByType(getValue(), change::track, change::trackRemove);
			break;
		case TRACK_FORMAT:
			setAudioDataByType(getValue(), change::trackFormat, change::trackFormatRemove);
			break;
		case TRACK_NO:
			String noString = getValue();
			Integer no = AudioAttributeUtils.convert2Integer(noString);
			setAudioDataByType(no, change::trackNo, change::trackNoRemove);
			break;
		case GENRE:
			setAudioDataByType(getValue(), change::genre, change::genreRemove);
			break;
		case RATING:
			String ratingString = getValue();
			RatingType rating = AudioAttributeUtils.convert2RatingType(ratingString);
			setAudioDataByType(rating, change::rating, change::ratingRemove);
			break;
		}
	}
	
	private <T> void setAudioDataByType(T value, Consumer<T> setConsumer, Consumer<Boolean> removeConsumer) {
		if (isDeleteValue()) {
			removeConsumer.accept(true);
		}
		else {
			setConsumer.accept(value);
		}
	}
	
	private Optional<String> getAudioDataByType(AudioData audioData, Supplier<Optional<String>> getter) {
		String value = getter.get().orElse(null);
		if (AudioData.DEFAULT_VALUE.equals(value)) {
			return Optional.empty();
		}
		return Optional.ofNullable(value);
	}
}
