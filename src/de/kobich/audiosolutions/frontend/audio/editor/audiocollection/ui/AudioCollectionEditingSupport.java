package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.component.file.FileDescriptor;

public class AudioCollectionEditingSupport extends EditingSupport {
	private static final Logger logger = Logger.getLogger(AudioCollectionEditingSupport.class);
	private final AudioCollectionEditor editor;
	private final AudioCollectionEditorColumn column;
	private final CellEditor cellEditor;

	public AudioCollectionEditingSupport(AudioCollectionEditor editor, TreeViewer viewer, AudioCollectionEditorColumn column) {
		super(viewer);
		this.editor = editor;
		this.column = column;
		this.cellEditor = new TextCellEditor(viewer.getTree());
	}
	
	@Override
    protected CellEditor getCellEditor(Object element) {
        return cellEditor;
    }

	@Override
	public boolean canEdit(Object element) {
		if (element instanceof FileDescriptorTreeNode) {
			switch (column) {
				case ALBUM:
				case ALBUM_PUBLICATION:
				case ARTIST:
				case DISK:
				case GENRE:
				case MEDIUM:
				case TRACK:
				case TRACK_FORMAT:
				case TRACK_NO:
					return true;
				default: 
					return false;
			}
		}
		return false;
	}

	@Override
	public Object getValue(Object element) {
		if (element instanceof FileDescriptorTreeNode treeNode) {
			FileDescriptor fileDescriptor = treeNode.getContent();
			if (fileDescriptor.hasMetaData(AudioData.class)) {
				AudioData audioData = (AudioData) fileDescriptor.getMetaData();

				switch (column) {
					case ALBUM:
						return getAudioAttributeValue(audioData, AudioAttribute.ALBUM);
					case ALBUM_PUBLICATION:
						return getAudioAttributeValue(audioData, AudioAttribute.ALBUM_PUBLICATION);
					case ARTIST:
						return getAudioAttributeValue(audioData, AudioAttribute.ARTIST);
					case DISK:
						return getAudioAttributeValue(audioData, AudioAttribute.DISK);
					case GENRE:
						return getAudioAttributeValue(audioData, AudioAttribute.GENRE);
					case MEDIUM:
						return getAudioAttributeValue(audioData, AudioAttribute.MEDIUM);
					case TRACK:
						return getAudioAttributeValue(audioData, AudioAttribute.TRACK);
					case TRACK_FORMAT:
						return getAudioAttributeValue(audioData, AudioAttribute.TRACK_FORMAT);
					case TRACK_NO:
						return getAudioAttributeValue(audioData, AudioAttribute.TRACK_NO);
					default: 
						return "";
				}
			}
			else {
				return "";
			}
		}
		return null;
	}
	
	/**
	 * Returns the audio attribute if available, else an empty string
	 * @param audioData
	 * @param attribute
	 * @return
	 */
	protected String getAudioAttributeValue(AudioData audioData, AudioAttribute attribute) {
		if (audioData.hasAttribute(attribute)) {
			return audioData.getAttribute(attribute);
		}
		return "";
	}

	@Override
	public void setValue(Object element, Object value) {
		try {
			if (element instanceof FileDescriptorTreeNode treeNode) {
				FileDescriptor fileDescriptor = treeNode.getContent();

				AudioDataChange change = null;
				switch (column) {
					case ALBUM:
						change = AudioDataChange.builder().album(String.valueOf(value)).build();
						break;
					case ALBUM_PUBLICATION:
						Date publication = AudioAttributeUtils.convert2Date(String.valueOf(value));
						if (publication != null) {
							change = AudioDataChange.builder().albumPublication(publication).build();
						}
						break;
					case ARTIST:
						change = AudioDataChange.builder().artist(String.valueOf(value)).build();
						break;
					case DISK:
						change = AudioDataChange.builder().disk(String.valueOf(value)).build();
						break;
					case GENRE:
						change = AudioDataChange.builder().genre(String.valueOf(value)).build();
						break;
					case MEDIUM:
						change = AudioDataChange.builder().medium(String.valueOf(value)).build();
						break;
					case TRACK:
						change = AudioDataChange.builder().track(String.valueOf(value)).build();
						break;
					case TRACK_FORMAT:
						change = AudioDataChange.builder().trackFormat(String.valueOf(value)).build();
						break;
					case TRACK_NO:
						Integer no = AudioAttributeUtils.convert2Integer(String.valueOf(value));
						if (no != null) {
							change = AudioDataChange.builder().trackNo(no).build();
						}
						break;
					default: 
						break;
				}

				if (change != null) {
					AudioDataService audioDataService = AudioSolutions.getService(AudioDataService.class);
					audioDataService.applyChanges(Set.of(fileDescriptor), change, null);
				
					UIEvent event = new UIEvent(ActionType.AUDIO_DATA, this.editor);
					event.getEditorDelta().getUpdateItems().add(fileDescriptor);
					EventSupport.INSTANCE.fireEvent(event);
				}
			}
		}
		catch (Exception exc) {
			logger.error("Table cell could not be modified", exc);
		}
	}

}
