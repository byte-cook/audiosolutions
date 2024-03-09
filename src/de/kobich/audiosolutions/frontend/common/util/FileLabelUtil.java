package de.kobich.audiosolutions.frontend.common.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLabelUtil {
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Returns the size of the file
	 * @param file
	 * @return
	 */
	public static String getFileSizeLabel(File file) {
		if (file.isDirectory()) {
			return "";
		}
		long fileSize = file.length();
		if (fileSize > 1000000000L) {
			return fileSize / 1000000000L + " GB";
		}
		else if (fileSize > 1000000) {
			return fileSize / 1000000 + " MB";
		}
		else if (fileSize > 1000) {
			return fileSize / 1000 + " KB";
		}
		return fileSize + " Byte";
	}
	
	public static String getLastModifiedLabel(File file) {
		long lastModified = file.lastModified();
		return DATE_FORMAT.format(new Date(lastModified));
	}
}
