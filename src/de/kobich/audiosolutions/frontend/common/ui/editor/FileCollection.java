/*
 * ABaseCollection.java
 *
 * Created on 9. Februar 2004, 22:41
 */

package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor.CollectionEditorType;
import de.kobich.commons.utils.CloneUtils;
import de.kobich.component.file.FileDescriptor;

/**
 * Represents a file collection.
 */
public class FileCollection implements IEditorInput, Cloneable, Serializable {
	private static final long serialVersionUID = -7677376867145462360L;
	public static final String ADD_FILE_PROP = "addFileDescriptor";
	public static final String REMOVE_FILE_PROP = "removeFileDescriptor";
	private final Set<FileDescriptor> fileDescriptors;
	private final Map<String, FileDescriptor> filesMap; 
	private IOpeningInfo openingInfo;
	private final PropertyChangeSupport support;
	
	/**
	 * Constructor
	 */
	public FileCollection(IOpeningInfo openingInfo, Set<FileDescriptor> fileDescriptors) {
		this.openingInfo = openingInfo;
		this.fileDescriptors = fileDescriptors;
		this.filesMap = new HashMap<>();
		for (FileDescriptor f : fileDescriptors) {
			this.filesMap.put(f.getRelativePath(), f);
		}
		this.support = new PropertyChangeSupport(this);
	}
	
	@Override
	public String getName() {
		return openingInfo.getName();
	}

	@Override
	public String getToolTipText() {
		return openingInfo.getName();
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class arg0) {
		return null;
	}

	public Set<FileDescriptor> getFileDescriptors() {
		return fileDescriptors;
	}
	
	/**
	 * Removes file descriptors
	 * @param fileDescriptors the fileDescriptors to set
	 */
	public void removeFileDescriptors(Set<FileDescriptor> fileDescriptors) {
		this.fileDescriptors.removeAll(fileDescriptors);
		for (FileDescriptor f : fileDescriptors) {
			this.filesMap.remove(f.getRelativePath());
			this.support.firePropertyChange(REMOVE_FILE_PROP, f, null);
		}
	}
	
	/**
	 * Adds file descriptors
	 * @param fileDescriptors the fileDescriptors to set
	 */
	public void addFileDescriptors(Set<FileDescriptor> fileDescriptors) {
		this.fileDescriptors.addAll(fileDescriptors);
		for (FileDescriptor f : fileDescriptors) {
			this.filesMap.put(f.getRelativePath(), f);
			this.support.firePropertyChange(ADD_FILE_PROP, null, f);
		}
	}

	/**
	 * Updates files descriptors
	 * @param fileDescriptors
	 */
	public void updateFileDescriptors(Set<FileDescriptor> fileDescriptors) {
		// remove + add = update
		// must be called in order to fire property change events
		removeFileDescriptors(fileDescriptors);
		addFileDescriptors(fileDescriptors);
	}

	public void setOpeningInfo(IOpeningInfo openingInfo) {
		this.openingInfo = openingInfo;
	}
	
	public boolean containsFileDescriptor(String relativePath) {
		return this.filesMap.containsKey(relativePath);
	}

	public CollectionEditorType getEditorType() {
		return openingInfo.getEditorType();
	}
	
	public <T extends IOpeningInfo> T getOpeningInfo(Class<T> clazz) {
		if (this.openingInfo.getClass().isAssignableFrom(clazz))
			return clazz.cast(this.openingInfo);
		return null;
	}
	
	public IOpeningInfo getOpeningInfo() {
		return this.openingInfo;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileDescriptors == null) ? 0 : fileDescriptors.hashCode());
		result = prime * result + ((openingInfo == null) ? 0 : openingInfo.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FileCollection other = (FileCollection) obj;
		if (fileDescriptors == null) {
			if (other.fileDescriptors != null)
				return false;
		}
		else if (!fileDescriptors.equals(other.fileDescriptors))
			return false;
		if (openingInfo == null) {
			if (other.openingInfo != null)
				return false;
		}
		else if (!openingInfo.equals(other.openingInfo))
			return false;
		return true;
	}
	
	@Override
	public FileCollection clone() {
		return CloneUtils.deepCopy(this);
	}
}
