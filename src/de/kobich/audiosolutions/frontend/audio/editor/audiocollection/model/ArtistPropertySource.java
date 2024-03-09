package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class ArtistPropertySource implements IPropertySource {
	private static final String CATEGORY_AUDIO = "Audio";
	private static final String PROPERTY_ARTIST = "Artist";
	private static final String PROPERTY_FILE_COUNT = "File Count";
	private IPropertyDescriptor[] propertyDescriptors;
	private ArtistTreeNode albumTreeNode;
	
	public ArtistPropertySource(ArtistTreeNode fileDescriptorTreeNode) {
		this.albumTreeNode = fileDescriptorTreeNode;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (propertyDescriptors == null) {
			propertyDescriptors = new IPropertyDescriptor[2];
			// will be sorted by alphabet automatically
			PropertyDescriptor pathPropertyDesc = new PropertyDescriptor(PROPERTY_ARTIST, PROPERTY_ARTIST);
			pathPropertyDesc.setCategory(CATEGORY_AUDIO);
			propertyDescriptors[0] = pathPropertyDesc;

			PropertyDescriptor countPropertyDesc = new PropertyDescriptor(PROPERTY_FILE_COUNT, PROPERTY_FILE_COUNT);
			countPropertyDesc.setCategory(CATEGORY_AUDIO);
			propertyDescriptors[1] = countPropertyDesc;
		}
		return propertyDescriptors;
	}

	@Override
	public Object getPropertyValue(Object property) {
		if (PROPERTY_ARTIST.equals(property)) {
			return albumTreeNode.getContent();
		}
		else if (PROPERTY_FILE_COUNT.equals(property)) {
			return albumTreeNode.getChildren().size();
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
