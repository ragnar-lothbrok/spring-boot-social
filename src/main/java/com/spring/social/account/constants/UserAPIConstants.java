package com.spring.social.account.constants;

import java.text.SimpleDateFormat;

/**
 * This interface will cater all the constants values.
 * 
 * @author raghunandangupta
 *
 */
public interface UserAPIConstants {
	public static SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat SQL_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static SimpleDateFormat FACEBOOK_DATE_FORMAT = new SimpleDateFormat("mm/dd/yyyy");
	public static String TWITTER = "TWITTER";
	public static final String SEPERATOR = "~";
	public static final String FACEBOOK = "FACEBOOK";
	public static final String GOOGLE = "GOOGLE";
	public static final Integer PASSWORD_STRENGTH = 62;
	public static final String PASSWORD_SALT_SEPERATOR = ":";
	public static final String PENDING = "PENDING";
	public static final String SENT = "SENT";
	public static final String RESET_PASSWORD_TOKEN = "X-RESET-TOKEN";
	public static final String RESET_PASSWORD_DONE = "PASSWORD RESET DONE";
	public static final String RESET_PASSWORD_PENDING = "PASSWORD RESET PENDING";
}
