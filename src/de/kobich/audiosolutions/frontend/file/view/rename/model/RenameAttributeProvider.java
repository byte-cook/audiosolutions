package de.kobich.audiosolutions.frontend.file.view.rename.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;
import de.kobich.audiosolutions.core.service.mp3.id3.ReadID3TagRequest;
import de.kobich.audiosolutions.core.service.mp3.id3.ReadID3TagResponse;
import de.kobich.component.file.FileDescriptor;

public class RenameAttributeProvider {
	private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final FileDescriptor fileDescriptor;
	private final IFileID3TagService id3TagService;
	private Map<MP3ID3TagType, String> id3Values;
	
	public RenameAttributeProvider(FileDescriptor fileDescriptor, IFileID3TagService id3TagService) {
		this.fileDescriptor = fileDescriptor;
		this.id3TagService = id3TagService;
	}

	public String getAttribute(String attribute) {
		try {
			RenameFileDescriptorAttributeType md = RenameFileDescriptorAttributeType.getByName(attribute);
			switch (md) {
			case FILE_SIZE:
				return "" + fileDescriptor.getFile().length();
			case FILE_BASE_NAME: 
				return FilenameUtils.getBaseName(fileDescriptor.getFileName()); 
			case FILE_EXTENSION: 
				return FilenameUtils.getExtension(fileDescriptor.getFileName());
			case FILE_MODIFIED: 
				return DATE_FORMAT.format(new Date(fileDescriptor.getFile().lastModified()));
			case AUDIO_ALBUM:
			case AUDIO_ARTIST:
			case AUDIO_DISK:
			case AUDIO_ALBUM_PUBLICATION:
			case AUDIO_GENRE:
			case AUDIO_MEDIUM:
			case AUDIO_TRACK:
				if (md.getAttribute() != null && fileDescriptor.hasMetaData(AudioData.class)) {
					AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
					AudioAttribute audioAttribute = md.getAttribute();
					if (audioData.hasAttribute(audioAttribute)) {
						return audioData.getAttribute(audioAttribute);
					}
				}
				return "";
			case AUDIO_TRACK_NO:
				if (md.getAttribute() != null && fileDescriptor.hasMetaData(AudioData.class)) {
					AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
					AudioAttribute audioAttribute = md.getAttribute();
					if (audioData.hasAttribute(audioAttribute)) {
						String value = audioData.getAttribute(audioAttribute);
						// use at least 2 characters (add leading zero if required)
						value = StringUtils.leftPad(value, 2, '0');
						return value;
					}
				}
				return "";
			case ID3_ALBUM:
			case ID3_ALBUM_YEAR:
			case ID3_ARTIST:
			case ID3_GENRE:
			case ID3_TRACK:
				if (md.getTagType() != null) {
					Map<MP3ID3TagType, String> id3Values = getID3TagValues();
					return id3Values != null ? id3Values.get(md.getTagType()) : null;
				}
				return "";
			case ID3_TRACK_NO:
				if (md.getTagType() != null) {
					Map<MP3ID3TagType, String> id3Values = getID3TagValues();
					if (id3Values != null) {
						String value = id3Values.get(md.getTagType());
						// use at least 2 characters (add leading zero if required)
						value = StringUtils.leftPad(value, 2, '0');
						return value;
					}
				}
				return "";
			}
			return null;
		}
		catch (AudioException exc) {
			return null;
		}		
	}
	
	public void reload() {
		this.id3Values = null;
	}

	private Map<MP3ID3TagType, String> getID3TagValues() throws AudioException {
		if (id3Values == null) {
			ReadID3TagRequest request = new ReadID3TagRequest(fileDescriptor);
			ReadID3TagResponse response = id3TagService.readID3Tags(request);
			id3Values = response.getSucceededFiles().get(fileDescriptor);
		}
		return id3Values;
	}

}
