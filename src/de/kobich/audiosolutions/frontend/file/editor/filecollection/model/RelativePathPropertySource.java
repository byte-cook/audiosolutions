package de.kobich.audiosolutions.frontend.file.editor.filecollection.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class RelativePathPropertySource implements IPropertySource {
	private static final String CATEGORY_FILE = "File";
	private static final String PROPERTY_RELATIVE_PATH = "Relative Path";
	private static final String PROPERTY_FILE_COUNT = "File Count";
	private IPropertyDescriptor[] propertyDescriptors;
	private RelativePathTreeNode relativePathTreeNode;
	
	public RelativePathPropertySource(RelativePathTreeNode fileDescriptorTreeNode) {
		this.relativePathTreeNode = fileDescriptorTreeNode;
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
			PropertyDescriptor pathPropertyDesc = new PropertyDescriptor(PROPERTY_RELATIVE_PATH, PROPERTY_RELATIVE_PATH);
			pathPropertyDesc.setCategory(CATEGORY_FILE);
			propertyDescriptors[0] = pathPropertyDesc;

			PropertyDescriptor countPropertyDesc = new PropertyDescriptor(PROPERTY_FILE_COUNT, PROPERTY_FILE_COUNT);
			countPropertyDesc.setCategory(CATEGORY_FILE);
			propertyDescriptors[1] = countPropertyDesc;
		}
		return propertyDescriptors;
	}

	@Override
	public Object getPropertyValue(Object property) {
		if (PROPERTY_RELATIVE_PATH.equals(property)) {
			return relativePathTreeNode.getContent();
		}
		else if (PROPERTY_FILE_COUNT.equals(property)) {
			return relativePathTreeNode.getChildren().size();
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
