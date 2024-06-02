package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioState;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AlbumTreeNode;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.RelativePathTreeNode;
import de.kobich.audiosolutions.frontend.common.util.FileLabelUtil;
import de.kobich.component.file.FileDescriptor;

public class AudioCollectionEditorLabelProvider extends ColumnLabelProvider {
	private final AudioCollectionEditorColumn column;
	private Image folderImg;
	private Image albumImg;
	private Image audioFileNewWarnImg;
	private Image audioFileNewImg;
	private Image audioFileWarnImg;
	private Image audioFileImg;
	private Image audioFileEditWarnImg;
	private Image audioFileEditImg;
	private Image audioFileRemoveImg;
	private Image fileImg;
	
	public AudioCollectionEditorLabelProvider(AudioCollectionEditorColumn column) {
		this.column = column;
		this.folderImg = Activator.getDefault().getImage(ImageKey.FOLDER);
		this.albumImg = Activator.getDefault().getImage(ImageKey.ALBUM);
		this.audioFileNewWarnImg = Activator.getDefault().getImage(ImageKey.AUDIO_FILE_NEW_WARN);
		this.audioFileNewImg = Activator.getDefault().getImage(ImageKey.AUDIO_FILE_NEW);
		this.audioFileWarnImg = Activator.getDefault().getImage(ImageKey.AUDIO_FILE_WARN);
		this.audioFileImg = Activator.getDefault().getImage(ImageKey.AUDIO_FILE);
		this.audioFileEditWarnImg = Activator.getDefault().getImage(ImageKey.AUDIO_FILE_EDIT_WARN);
		this.audioFileEditImg = Activator.getDefault().getImage(ImageKey.AUDIO_FILE_EDIT);
		this.audioFileRemoveImg = Activator.getDefault().getImage(ImageKey.AUDIO_FILE_REMOVE);
		this.fileImg = Activator.getDefault().getImage(ImageKey.COMMON_FILE);
	}

	@Override
	public Image getImage(Object element) {
		if (AudioCollectionEditorColumn.FILE_NAME.equals(column)) {
			if (element instanceof RelativePathTreeNode) {
				return folderImg;
			}
			else if (element instanceof AlbumTreeNode) {
				return albumImg;
			}
			else if (element instanceof FileDescriptorTreeNode) {
				FileDescriptorTreeNode audioFile = (FileDescriptorTreeNode) element;
				FileDescriptor fileDescriptor = audioFile.getContent();
				if (fileDescriptor.hasMetaData(AudioData.class)) {
					AudioData audioData = (AudioData) fileDescriptor.getMetaData();
					switch (audioData.getState()) {
						case TRANSIENT_INCOMPLETE:
							return audioFileNewWarnImg;
						case TRANSIENT:
							return audioFileNewImg;
						case PERSISTENT_INCOMPLETE:
							return audioFileWarnImg;
						case PERSISTENT:
							return audioFileImg;
						case PERSISTENT_MODIFIED_INCOMPLETE:
							return audioFileEditWarnImg;
						case PERSISTENT_MODIFIED:
							return audioFileEditImg;
						case REMOVED:
							return audioFileRemoveImg;
					}
				}
				else {
					return fileImg;
				}
			}
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		return AudioCollectionEditorLabelProvider.getColumnText(element, column);
	}
		
	public static String getColumnText(Object element, AudioCollectionEditorColumn column) {
		if (element instanceof RelativePathTreeNode) {
			RelativePathTreeNode pathNode = (RelativePathTreeNode) element;
			switch (column) {
				case FILE_NAME:
					return pathNode.getContent();
				default:
					return "";
			}
		}
		else if (element instanceof AlbumTreeNode) {
			AlbumTreeNode albumNode = (AlbumTreeNode) element;
			switch (column) {
				case FILE_NAME:
					return albumNode.getLabel();
				case ARTIST:
					return albumNode.getArtistName().orElse(null);
				case ALBUM:
					return albumNode.getAlbumName().orElse(null);
				case ALBUM_PUBLICATION:
					Date publicationDate = albumNode.getAlbumPublication().orElse(null);
					return AudioAttributeUtils.convert2String(publicationDate);
				default:
					return "";
			}
		}
		else if (element instanceof FileDescriptorTreeNode) {
			FileDescriptorTreeNode audioFile = (FileDescriptorTreeNode) element;
			FileDescriptor fileDescriptor = audioFile.getContent();
			
			switch (column) {
				case FILE_NAME:
					return fileDescriptor.getFileName();
				case EXISTS:
					return fileDescriptor.getFile().exists() ? "yes" : "no";
				case RELATIVE_PATH:
					return fileDescriptor.getRelativePath();
				case EXTENSION:
					return fileDescriptor.getExtension();
				case SIZE:
					return FileLabelUtil.getFileSizeLabel(fileDescriptor.getFile());
				case LAST_MODIFIED:
					return FileLabelUtil.getLastModifiedLabel(fileDescriptor.getFile());
				case ALBUM:
					return getAudioAttributeText(fileDescriptor, AudioAttribute.ALBUM);
				case ALBUM_PUBLICATION:
					return getAudioAttributeText(fileDescriptor, AudioAttribute.ALBUM_PUBLICATION);
				case ARTIST:
					return getAudioAttributeText(fileDescriptor, AudioAttribute.ARTIST);
				case DISK:
					return getAudioAttributeText(fileDescriptor, AudioAttribute.DISK);
				case GENRE:
					return getAudioAttributeText(fileDescriptor, AudioAttribute.GENRE);
				case MEDIUM:
					return getAudioAttributeText(fileDescriptor, AudioAttribute.MEDIUM);
				case TRACK:
					return getAudioAttributeText(fileDescriptor, AudioAttribute.TRACK);
				case TRACK_FORMAT:
					return getAudioAttributeText(fileDescriptor, AudioAttribute.TRACK_FORMAT);
				case TRACK_NO:
					return getAudioAttributeText(fileDescriptor, AudioAttribute.TRACK_NO);
			}
		}
		throw new IllegalStateException("Illegal element <" + element + ">");
	}
	
	private static String getAudioAttributeText(FileDescriptor fileDescriptor, AudioAttribute attribute) {
		AudioData audioData = fileDescriptor.getMetaDataOptional(AudioData.class).orElse(null);
		if (audioData != null) {
			if (AudioState.REMOVED.equals(audioData.getState())) {
				return "";
			}

			switch (attribute) {
				case TRACK_NO:
					Integer trackNo = audioData.getAttribute(AudioAttribute.TRACK_NO, Integer.class);
					return AudioAttributeUtils.convert2String(trackNo);
				case ALBUM_PUBLICATION:
					String publication = audioData.getAttribute(AudioAttribute.ALBUM_PUBLICATION);
					Date publicationDate = AudioAttributeUtils.convert2Date(publication);
					return AudioAttributeUtils.convert2String(publicationDate);
				default:
					return audioData.getAttribute(attribute);
			}
		}
		else {
			return "";
		}
	}

}
