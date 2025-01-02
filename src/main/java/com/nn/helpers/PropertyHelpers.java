package com.nn.helpers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyHelpers {

	private static String configFilePath = "/src/test/resources/config.properties";
	private static Properties properties;
	private static FileInputStream input;
	private static FileOutputStream output;
	
	/*public static Properties loadFile() {
		properties = new Properties();
		try {
			input = new FileInputStream(System.getProperty("user.dir") + configFilePath);
			properties.load(input);
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return properties;
	}
	
	public static String getValue(String key) {
		String value = null;
		try {
			if(input == null) {
				properties = new Properties();
				input = new FileInputStream(System.getProperty("user.dir") + configFilePath);
				properties.load(input);
				input.close();
			}
			value = properties.getProperty(key);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public static void setValue(String key, String value) {
		try {
			if(input == null) {
				properties = new Properties();
				input = new FileInputStream(System.getProperty("user.dir") + configFilePath);
				properties.load(input);
				input.close();
				output = new FileOutputStream(System.getProperty("user.dir") + configFilePath);
			}
			properties.setProperty(key, value);
            properties.store(output, null);
            output.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
*/

	public static Properties loadFile() {
		Properties properties = new Properties();


		// Retrieve values from VM arguments
		properties.setProperty("URL", System.getProperty("URL"));
		properties.setProperty("USERNAME", System.getProperty("USERNAME"));
		properties.setProperty("PASSWORD", System.getProperty("PASSWORD"));
		properties.setProperty("BROWSER", System.getProperty("BROWSER"));
		properties.setProperty("HEADLESS", System.getProperty("HEADLESS"));
		properties.setProperty("EXPLICIT_TIMEOUT", System.getProperty("EXPLICIT_TIMEOUT"));
		properties.setProperty("IMPLICIT_TIMEOUT", System.getProperty("IMPLICIT_TIMEOUT"));
		properties.setProperty("PAGE_LOAD_TIMEOUT", System.getProperty("PAGE_LOAD_TIMEOUT"));
		properties.setProperty("CALLBACK_URL", System.getProperty("CALLBACK_URL"));
		properties.setProperty("URL_FRONTEND", System.getProperty("URL_FRONTEND"));
		properties.setProperty("SCREENSHOT_FAIL", System.getProperty("SCREENSHOT_FAIL"));
		properties.setProperty("SCREENSHOT_PASS", System.getProperty("SCREENSHOT_PASS"));
		properties.setProperty("RECORD_VIDEO", System.getProperty("RECORD_VIDEO"));
		properties.setProperty("REPORT_TITLE", System.getProperty("REPORT_TITLE"));
		properties.setProperty("REPORT_EMAIL", System.getProperty("REPORT_EMAIL"));
		properties.setProperty("SENT_REPORT_TO_USER_IN_EMAIL", System.getProperty("SENT_REPORT_TO_USER_IN_EMAIL"));
		//properties.setProperty("NOVALNET_API_KEY", "n7ibc7ob5t|doU3HJVoym7MQ44qonbobljblnmdli0p|qJEH3gNbeWJfIHah||f7cpn7pc");
		//properties.setProperty("NOVALNET_API_KEY", System.getProperty("NOVALNET_API_KEY"));
		//properties.setProperty("NOVALNET_API_KEY", System.getProperty("NOVALNET_API_KEY_FILE"));

		properties.setProperty("NOVALNET_API_KEY",System.getProperty("NOVALNET_API_KEY").replaceAll("\\\\", "")
				.replaceAll("^\\|+|\\|+$", "").replaceAll("(?<=\\|)\\|\\|(?=\\|)", "| |"));
		properties.setProperty("NOVALNET_ACCESSKEY", System.getProperty("NOVALNET_ACCESSKEY"));
		properties.setProperty("NOVALNET_TARIFF", System.getProperty("NOVALNET_TARIFF"));
		properties.setProperty("NOVALNET_SUBSCRIPTION_TARIFF", System.getProperty("NOVALNET_SUBSCRIPTION_TARIFF"));


		return properties;
	}
}