package client.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParser {

	/**
	 * Converts a string of specified formatting to a date
	 * @param s - String
	 * @return date- string converted to a date
	 * @throws ParseException 
	 */
	public static Date stringToDate(String s) throws ParseException{
		return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(s);
	}

	/**
	 * Converts a date to a specified formatting for a string
	 * Used for If-Modified-Since in HTTP request header
	 * @param d - date to be converted
	 * @return date formatted string
	 * @throws ParseException 
	 */
	public static String dateToString(Date d) {
		return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").format(d);
	}

}
