package de.kobich.audiosolutions.frontend.file.view.browse.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import de.kobich.audiosolutions.frontend.common.util.FileLabelUtil;

public class FilePropertySource implements IPropertySource {
	private static final String CATEGORY_FILE = "File";
	private static final String PROPERTY_FILE_NAME = "File Name";
	private static final String PROPERTY_FILE_PATH = "File Path";
	private static final String PROPERTY_SIZE = "Size";
	private IPropertyDescriptor[] propertyDescriptors;
	private FileTreeNode fileTreeNode;
	
	public FilePropertySource(FileTreeNode fileTreeNode) {
		this.fileTreeNode = fileTreeNode;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (propertyDescriptors == null) {
			String[] properties = {PROPERTY_FILE_NAME, PROPERTY_FILE_PATH, PROPERTY_SIZE };
			propertyDescriptors = new IPropertyDescriptor[properties.length];
			int i = 0;
			for (String property : properties) {
				PropertyDescriptor filePropertyDesc = new PropertyDescriptor(property, property);
				filePropertyDesc.setCategory(CATEGORY_FILE);
				propertyDescriptors[i] = filePropertyDesc;
				++ i;
			}
		}
		return propertyDescriptors;
	}

	@Override
	public Object getPropertyValue(Object property) {
		if (PROPERTY_FILE_NAME.equals(property)) {
			return fileTreeNode.getContent().getName();
		}
		else if (PROPERTY_FILE_PATH.equals(property)) {
			return fileTreeNode.getContent().getAbsolutePath();
		}
		else if (PROPERTY_SIZE.equals(property)) {
			return FileLabelUtil.getFileSizeLabel(fileTreeNode.getContent());
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
