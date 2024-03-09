package de.kobich.audiosolutions.frontend.file.editor.filecollection.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorColumn;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorLabelProvider;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.ui.FileCollectionEditorColumn;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.ui.FileCollectionEditorLabelProvider;

public class FileDescriptorPropertySource implements IPropertySource {
	private static final String CATEGORY_FILE = "File";
	private static final String CATEGORY_AUDIO = "Audio";
	private static final String PROPERTY_FILE_PATH = "File Path";
	private static final String PROPERTY_IMPORT_DIR = "Import Directory";
	private final FileDescriptorTreeNode fileDescriptorTreeNode;
	private List<IPropertyDescriptor> propertyDescriptors;
	private FileCollectionEditorLabelProvider fileLabelProvider;
	private AudioCollectionEditorLabelProvider audioLabelProvider;
	
	public FileDescriptorPropertySource(FileDescriptorTreeNode fileDescriptorTreeNode) {
		this.fileDescriptorTreeNode = fileDescriptorTreeNode;
		this.fileLabelProvider = new FileCollectionEditorLabelProvider(false);
		this.audioLabelProvider = new AudioCollectionEditorLabelProvider(false);
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (propertyDescriptors == null) {
			propertyDescriptors = new ArrayList<IPropertyDescriptor>();
			// will be sorted by alphabet automatically
			for (FileCollectionEditorColumn column : FileCollectionEditorColumn.values()) {
				PropertyDescriptor filePropertyDesc = new PropertyDescriptor(column, column.getLabel());
				filePropertyDesc.setCategory(CATEGORY_FILE);
				propertyDescriptors.add(filePropertyDesc);
			}
			// file
			PropertyDescriptor filePropertyDesc = new PropertyDescriptor(PROPERTY_FILE_PATH, PROPERTY_FILE_PATH);
			filePropertyDesc.setCategory(CATEGORY_FILE);
			propertyDescriptors.add(filePropertyDesc);
			// import dir
			PropertyDescriptor importDirPropertyDesc = new PropertyDescriptor(PROPERTY_IMPORT_DIR, PROPERTY_IMPORT_DIR);
			importDirPropertyDesc.setCategory(CATEGORY_FILE);
			propertyDescriptors.add(importDirPropertyDesc);
			
			// audio data
			if (fileDescriptorTreeNode.getContent().hasMetaData(AudioData.class)) {
				// will be sorted by alphabet automatically
				for (AudioCollectionEditorColumn column : AudioCollectionEditorColumn.values()) {
					if (AudioCollectionEditorColumn.FILE_NAME.equals(column)) {
						continue;
					}
					PropertyDescriptor audioPropertyDesc = new PropertyDescriptor(column, column.getLabel());
					audioPropertyDesc.setLabelProvider(new FileCollectionEditorLabelProvider(false));
					audioPropertyDesc.setCategory(CATEGORY_AUDIO);
					propertyDescriptors.add(audioPropertyDesc);
				}
			}
		}
		return propertyDescriptors.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object property) {
		if (property instanceof FileCollectionEditorColumn) {
			FileCollectionEditorColumn column = (FileCollectionEditorColumn) property;
			return fileLabelProvider.getColumnText(fileDescriptorTreeNode, column.getIndex());
		}
		else if (PROPERTY_FILE_PATH.equals(property)) {
			return fileDescriptorTreeNode.getContent().getFile().getAbsolutePath();
		}
		else if (PROPERTY_IMPORT_DIR.equals(property)) {
			return fileDescriptorTreeNode.getContent().getImportDirectory();
		}
		else if (property instanceof AudioCollectionEditorColumn) {
			AudioCollectionEditorColumn column = (AudioCollectionEditorColumn) property;
			return audioLabelProvider.getColumnText(fileDescriptorTreeNode, column.getIndex());
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
