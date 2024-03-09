package de.kobich.audiosolutions.frontend.file;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import de.kobich.component.file.ant.FileAntOutputLevel;


/**
 * Output level comparator.
 */
public class AntOutputLevelComparator implements Comparator<FileAntOutputLevel> {
	private static final Map<FileAntOutputLevel, Integer> order;
	
	static {
		order = new Hashtable<FileAntOutputLevel, Integer>();
		order.put(FileAntOutputLevel.DEBUG, 10);
		order.put(FileAntOutputLevel.VERBOSE, 20);
		order.put(FileAntOutputLevel.INFO, 30);
		order.put(FileAntOutputLevel.WARN, 31);
		order.put(FileAntOutputLevel.ERR, 32);
	}
	
	/**
	 * Constructor
	 */
	public AntOutputLevelComparator() {}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(FileAntOutputLevel o1, FileAntOutputLevel o2) {
//		return o1.name().compareTo(o2.name());
		return order.get(o1).compareTo(order.get(o2));
	}

}
