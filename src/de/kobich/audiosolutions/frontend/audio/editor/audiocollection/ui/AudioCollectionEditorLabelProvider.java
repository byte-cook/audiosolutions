package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

import java.util.Date;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioState;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AlbumTreeNode;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.ArtistTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.IMetaData;

public class AudioCollectionEditorLabelProvider extends LabelProvider implements ITableLabelProvider {
	private final boolean imageSupport;
	private Image folderImg;
	private Image artistImg;
	private Image albumImg;
	private Image audioFileNewWarnImg;
	private Image audioFileNewImg;
	private Image audioFileWarnImg;
	private Image audioFileImg;
	private Image audioFileEditWarnImg;
	private Image audioFileEditImg;
	private Image audioFileRemoveImg;
	private Image fileImg;
	
	public AudioCollectionEditorLabelProvider(boolean imageSupport) {
		this.imageSupport = imageSupport;
		if (imageSupport) {
			this.folderImg = Activator.getDefault().getImage(ImageKey.FOLDER);
			this.artistImg = Activator.getDefault().getImage(ImageKey.ARTIST);
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
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!imageSupport) {
			return null;
		}
		AudioCollectionEditorColumn column = AudioCollectionEditorColumn.getByIndex(columnIndex);
		if (AudioCollectionEditorColumn.FILE_NAME.equals(column)) {
			if (element instanceof RelativePathTreeNode) {
				return folderImg;
			}
			else if (element instanceof AlbumTreeNode) {
				return albumImg;
			}
			else if (element instanceof ArtistTreeNode) {
				return artistImg;
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
	public String getColumnText(Object element, int columnIndex) {
		AudioCollectionEditorColumn column = AudioCollectionEditorColumn.getByIndex(columnIndex);
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
				default:
					return "";
			}
		}
		else if (element instanceof ArtistTreeNode) {
			ArtistTreeNode artistNode = (ArtistTreeNode) element;
			switch (column) {
				case FILE_NAME:
					return artistNode.getContent();
				default:
					return "";
			}
		}
		else if (element instanceof FileDescriptorTreeNode) {
			FileDescriptorTreeNode audioFile = (FileDescriptorTreeNode) element;
			FileDescriptor fileDescriptor = audioFile.getContent();
			
			if (AudioCollectionEditorColumn.FILE_NAME.equals(column)) {
				return fileDescriptor.getFileName();
			}
			
			IMetaData metaData = fileDescriptor.getMetaData();
			if (metaData instanceof AudioData) {
				AudioData audioData = (AudioData) metaData;
				if (AudioState.REMOVED.equals(audioData.getState())) {
					return "";
				}
				switch (column) {
					case ALBUM:
						return audioData.getAttribute(AudioAttribute.ALBUM);
					case ALBUM_PUBLICATION:
						String publication = audioData.getAttribute(AudioAttribute.ALBUM_PUBLICATION);
						Date publicationDate = AudioAttributeUtils.convert2Date(publication);
						return AudioAttributeUtils.convert2String(publicationDate);
					case ARTIST:
						return audioData.getAttribute(AudioAttribute.ARTIST);
					case DISK:
						return audioData.getAttribute(AudioAttribute.DISK);
					case GENRE:
						return audioData.getAttribute(AudioAttribute.GENRE);
					case MEDIUM:
						return audioData.getAttribute(AudioAttribute.MEDIUM);
					case TRACK:
						return audioData.getAttribute(AudioAttribute.TRACK);
					case TRACK_FORMAT:
						return audioData.getAttribute(AudioAttribute.TRACK_FORMAT);
					case TRACK_NO:
						Integer trackNo = audioData.getAttribute(AudioAttribute.TRACK_NO, Integer.class);
						if (trackNo != null) {
							return AudioAttributeUtils.convert2String(trackNo);
						}
						else {
							return "";
						}
					default: 
						break;
				}
			}
			else {
				return "";
			}
		}
		throw new IllegalStateException("Illegal column index < " + columnIndex + ">, expected<0 - 3>");
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {}

}
