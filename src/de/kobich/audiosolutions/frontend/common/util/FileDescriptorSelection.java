package de.kobich.audiosolutions.frontend.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorViewerFilter;
import de.kobich.audiosolutions.frontend.file.IFileDescriptorsSource;
import de.kobich.component.file.FileDescriptor;

/**
 * Represents a selection of FileDescriptor's.
 */
public class FileDescriptorSelection {
	private final CollectionEditorViewerFilter editorFilter;
	private final List<Object> elements;
	private final Set<FileDescriptor> fileDescriptors;
	private final Set<FileDescriptor> nonMetaDataFiles;
	private final Set<FileDescriptor> metaDataFiles;
	private final Set<FileDescriptor> existingFiles;
	private final Set<FileDescriptor> nonExistingFiles;
	
	public FileDescriptorSelection(ISelection selection, CollectionEditorViewerFilter filter) {
		this.editorFilter = filter;
		this.elements = new ArrayList<>();
		this.fileDescriptors = new HashSet<FileDescriptor>();
		this.nonMetaDataFiles = new HashSet<FileDescriptor>();
		this.metaDataFiles = new HashSet<FileDescriptor>();
		this.existingFiles = new HashSet<FileDescriptor>();
		this.nonExistingFiles = new HashSet<FileDescriptor>();
		selectionChanged(selection);
	}
			
	/**
	 * Called if selection changed
	 * @param selection
	 */
	private void selectionChanged(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			for (Object element : structuredSelection.toList()) {
				elements.add(element);
				
				if (element instanceof IFileDescriptorsSource) {
					IFileDescriptorsSource fileDescriptorsSource = (IFileDescriptorsSource) element;
					for (FileDescriptor fileDescriptor : fileDescriptorsSource.getFileDescriptors()) {
						if (!editorFilter.select(fileDescriptor)) {
							// skip non visible files
							continue;
						}
						
						fileDescriptors.add(fileDescriptor);
						if (fileDescriptor.hasMetaData()) {
							metaDataFiles.add(fileDescriptor);
						}
						else {
							nonMetaDataFiles.add(fileDescriptor);
						}
						if (fileDescriptor.getFile().exists()) {
							existingFiles.add(fileDescriptor);
						}
						else {
							nonExistingFiles.add(fileDescriptor);
						}
					}
				}
			}
		}
	}

	public List<Object> getElements() {
		return Collections.unmodifiableList(elements);
	}

	public CollectionEditorViewerFilter getEditorFilter() {
		return editorFilter;
	}

	public Set<FileDescriptor> getFileDescriptors() {
		return Collections.unmodifiableSet(fileDescriptors);
	}

	public Set<FileDescriptor> getMetaDataFiles() {
		return Collections.unmodifiableSet(metaDataFiles);
	}

	public Set<FileDescriptor> getNonMetaDataFiles() {
		return Collections.unmodifiableSet(nonMetaDataFiles);
	}

	public Set<FileDescriptor> getExistingFiles() {
		return Collections.unmodifiableSet(existingFiles);
	}

	public Set<FileDescriptor> getNonExistingFiles() {
		return Collections.unmodifiableSet(nonExistingFiles);
	}
	
}
