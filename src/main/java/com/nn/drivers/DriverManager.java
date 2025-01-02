package com.nn.drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverManager {
	
	private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	
	private DriverManager() {
		
	}
	
	public static WebDriver getDriver() {
		return driver.get();
	}



	public static void setDriver(WebDriver driver) {
		DriverManager.driver.set(driver);
	}
	
	public static void quit() {
		if (DriverManager.getDriver() != null) {
			DriverManager.getDriver().quit();
		}
	}
}
