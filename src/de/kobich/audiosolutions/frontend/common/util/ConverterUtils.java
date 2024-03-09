package de.kobich.audiosolutions.frontend.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class ConverterUtils {
	/**
	 * Converts a string array to string
	 * @param list
	 * @param separator
	 * @return
	 */
	public static String convert2String(String[] list, String separator) {
		String result = "";
		for (String s : list) {
			if (!StringUtils.isBlank(result)) {
				result += separator;
			}
			result += s;
		}
		return result;
	}
	
	/**
	 * Converts a string to string array
	 * @param text
	 * @param separator
	 * @return
	 */
	public static String[] convert2StringArray(String text, String separator) {
		return text.split(separator);
	}
	
	/**
	 * Converts a date to string
	 * @param date
	 * @return
	 */
	public static String convert2String(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}
}
