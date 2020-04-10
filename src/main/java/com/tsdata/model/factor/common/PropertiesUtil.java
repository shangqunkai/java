package com.tsdata.model.factor.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	private static Properties properties = new Properties();
	static {
		try {
			InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream("brConfig.properties");
			properties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Properties getProperties() {
		return properties;
	}

	public static boolean containsKey(String key) {
		return properties.containsKey(key);
	}

	public static Integer getIntegerValue(String key) {
		return Integer.valueOf(Integer.parseInt(properties.getProperty(key)));
	}

	public static String getStringValue(String key) {
		return properties.getProperty(key);
	}

	public static boolean getBooleanValue(String key) {
		return Boolean.parseBoolean(properties.getProperty(key));
	}

	public static Object getValue(String key) {
		return properties.getProperty(key);
	}

	public static void main(String[] rags) {
		System.out.print(getStringValue("mkey"));
	}
}
