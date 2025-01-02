package com.nn.reports;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.nn.constants.Constants;
import com.nn.drivers.DriverManager;
import com.nn.utilities.DriverActions;


public class ExtentTestManager {

	static Map<Integer, ExtentTest> extentTestMap = new HashMap<>();
    static ExtentReports extent = ExtentReportManager.getExtentReports();
    
    public static ExtentTest getTest() {
    	return extentTestMap.get((int) Thread.currentThread().getId());
    }
    //used synchronized keyword for other threads has to wait while current thread accessing this method
    public static synchronized ExtentTest saveToReport(String testCase, String desc) {
    	ExtentTest test = extent.createTest(testCase,desc);
    	extentTestMap.put((int) Thread.currentThread().getId(), test);
    	return test;
    }
    
    public static void logMessage(Status status, String message) {
        getTest().log(status, message);
    }
    
    public static void logMessage(String message) {
        getTest().info(message);
    }
    
    public static void addScreenShot(Status status, String message) {
    	String base64Image = "data:image/png;base64," + ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BASE64);
    	getTest().log(status,message, MediaEntityBuilder.createScreenCaptureFromBase64String(base64Image).build());
    }
    
    public static void addScreenShot(String path,String messae) {
    	getTest().info(messae, MediaEntityBuilder.createScreenCaptureFromPath(path).build());
    }

    @Step("Add screenshot to report")
    public static void addScreenShot(String info) {
    	String base64Image = "data:image/png;base64," + ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BASE64);
    	getTest().info(info, MediaEntityBuilder.createScreenCaptureFromBase64String(base64Image).build());
        byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment(info, new ByteArrayInputStream(screenshotBytes));
    }
    
    public static String getBrowserInfo() {
    	String browser = "";
    	if(Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("BROWSER") == null) {
    		browser = Constants.BROWSER.toUpperCase();
    	}else {
    		browser = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("BROWSER").toUpperCase(); 
    	}
    	return browser;
    }
    
    
    synchronized public static void addBrowsers() {
    	getTest().assignDevice(DriverActions.getBrowserInformation());
    }
    
    
}
