package de.kobich.audiosolutions.frontend.file.view.rename.model;

import java.io.File;
import java.util.Comparator;

import org.springframework.lang.Nullable;

import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.IFileDescriptorRenameable;

/**
 * Contains the new name of a file descriptor.
 */
public class RenameFileDescriptor implements IFileDescriptorRenameable {
	private static final Comparator<FileDescriptor> COMPARATOR = new DefaultFileDescriptorComparator(); 
	private final FileDescriptor fileDescriptor;
	private RenameAttributeProvider attributeProvider;
	private String name;
	
	/**
	 * Constructor
	 * @param fileDescriptor
	 */
	public RenameFileDescriptor(FileDescriptor fileDescriptor, @Nullable RenameAttributeProvider attributeProvider) {
		this.fileDescriptor = fileDescriptor;
		this.attributeProvider = attributeProvider;
		this.name = fileDescriptor.getFileName();
	}

	/**
	 * @return the fileDescriptor
	 */
	public FileDescriptor getFileDescriptor() {
		return fileDescriptor;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getOriginalName() {
		return fileDescriptor.getFileName();
	}

	@Override
	public String getCategory() {
		return new File(fileDescriptor.getRelativePath()).getParent();
	}

	@Override
	public String getAttribute(String attribute) {
		if (attributeProvider != null) {
			return attributeProvider.getAttribute(attribute);
		}
		return null;
	}
	
	public void reload() {
		if (attributeProvider != null) {
			this.attributeProvider.reload();
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileDescriptor == null) ? 0 : fileDescriptor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RenameFileDescriptor other = (RenameFileDescriptor) obj;
		if (fileDescriptor == null) {
			if (other.fileDescriptor != null)
				return false;
		}
		else if (!fileDescriptor.equals(other.fileDescriptor))
			return false;
		return true;
	}

	@Override
	public int compareTo(IFileDescriptorRenameable arg0) {
		return COMPARATOR.compare(this.getFileDescriptor(), arg0.getFileDescriptor());
	}
}
