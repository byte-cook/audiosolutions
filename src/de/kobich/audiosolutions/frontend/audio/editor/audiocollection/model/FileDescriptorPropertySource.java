package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorColumn;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorLabelProvider;

public class FileDescriptorPropertySource implements IPropertySource {
	private static final String CATEGORY_FILE = "File";
	private static final String CATEGORY_AUDIO = "Audio";
	private static final String PROPERTY_FILE_PATH = "File Path";
	private static final String PROPERTY_IMPORT_DIR = "Import Directory";
	private final FileDescriptorTreeNode fileDescriptorTreeNode;
	private List<IPropertyDescriptor> propertyDescriptors;
	
	public FileDescriptorPropertySource(FileDescriptorTreeNode fileDescriptorTreeNode) {
		this.fileDescriptorTreeNode = fileDescriptorTreeNode;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (propertyDescriptors == null) {
			propertyDescriptors = new ArrayList<IPropertyDescriptor>();
			// file
			PropertyDescriptor filePropertyDesc = new PropertyDescriptor(PROPERTY_FILE_PATH, PROPERTY_FILE_PATH);
			filePropertyDesc.setCategory(CATEGORY_FILE);
			propertyDescriptors.add(filePropertyDesc);
			// import dir
			PropertyDescriptor importDirPropertyDesc = new PropertyDescriptor(PROPERTY_IMPORT_DIR, PROPERTY_IMPORT_DIR);
			importDirPropertyDesc.setCategory(CATEGORY_FILE);
			propertyDescriptors.add(importDirPropertyDesc);
			
			// audio data
			boolean hasAudioData = fileDescriptorTreeNode.getContent().hasMetaData(AudioData.class);
			// will be sorted by alphabet automatically
			for (AudioCollectionEditorColumn column : AudioCollectionEditorColumn.values()) {
				if (column.getType().isAudio() && !hasAudioData) {
					continue;
				}
				PropertyDescriptor audioPropertyDesc = new PropertyDescriptor(column, column.getLabel());
				audioPropertyDesc.setCategory(column.getType().isAudio() ? CATEGORY_AUDIO : CATEGORY_FILE);
				propertyDescriptors.add(audioPropertyDesc);
			}
		}
		return propertyDescriptors.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object property) {
		if (PROPERTY_FILE_PATH.equals(property)) {
			return fileDescriptorTreeNode.getContent().getFile().getAbsolutePath();
		}
		else if (PROPERTY_IMPORT_DIR.equals(property)) {
			return fileDescriptorTreeNode.getContent().getImportDirectory();
		}
		else if (property instanceof AudioCollectionEditorColumn) {
			AudioCollectionEditorColumn column = (AudioCollectionEditorColumn) property;
			return AudioCollectionEditorLabelProvider.getColumnText(fileDescriptorTreeNode, column);
		}
		return null;
	}

	@Override
	public boolean isPropertySet(Object arg0) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object arg0) {
	}

	@Override
	public void setPropertyValue(Object arg0, Object arg1) {
	}
}
