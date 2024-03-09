package de.kobich.audiosolutions.frontend.file;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import de.kobich.audiosolutions.frontend.file.editor.filecollection.ui.FileCollectionEditorColumn;


/**
 * Comparator of file collection editor columns.
 */
public class FileCollectionEditorColumnComparator implements Comparator<FileCollectionEditorColumn> {
	private static final Map<FileCollectionEditorColumn, Integer> order;
	
	static {
		order = new Hashtable<FileCollectionEditorColumn, Integer>();
		order.put(FileCollectionEditorColumn.FILE_NAME, 10);
		order.put(FileCollectionEditorColumn.EXTENSION, 20);
		order.put(FileCollectionEditorColumn.RELATIVE_PATH, 30);
		order.put(FileCollectionEditorColumn.SIZE, 42);
		order.put(FileCollectionEditorColumn.LAST_MODIFIED, 43);
	}
	
	/**
	 * Constructor
	 */
	public FileCollectionEditorColumnComparator() {}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(FileCollectionEditorColumn o1, FileCollectionEditorColumn o2) {
		return order.get(o1).compareTo(order.get(o2));
	}

}
