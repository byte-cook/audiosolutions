package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.kobich.audiosolutions.frontend.file.IFileDescriptorsSource;
import de.kobich.component.file.FileDescriptor;

public class CollectionEditorViewerFilter extends ViewerFilter {
	public static final String FILTER_PROP = "collection.filter";
	private String search;

	public void setSearchText(String s) {
		this.search = s;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (StringUtils.isEmpty(this.search)) {
			return true;
		}
		
		List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
		if (element instanceof IFileDescriptorsSource) {
			IFileDescriptorsSource source = (IFileDescriptorsSource) element;
			fileDescriptors.addAll(source.getFileDescriptors());
		}
		
		// perform filtering
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			if (select(fileDescriptor)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean select(FileDescriptor fileDescriptor) {
		if (StringUtils.isEmpty(search)) {
			return true;
		}
		
		return FilenameUtils.wildcardMatch(fileDescriptor.getRelativePath(), search, IOCase.INSENSITIVE);
	}
	
	public boolean isFilterProperty(Object element, String property) {
		return FILTER_PROP.equals(property);
	}

}
