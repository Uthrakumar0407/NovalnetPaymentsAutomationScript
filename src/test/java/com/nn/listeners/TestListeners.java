package com.nn.listeners;

import com.nn.drivers.DriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.*;
import com.aventstack.extentreports.Status;
import com.nn.helpers.EmailHelpers;
import com.nn.helpers.PropertyHelpers;
import com.nn.helpers.ScreenRecorderHelper;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentReportManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import org.testng.annotations.AfterSuite;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import static com.nn.constants.Constants.*;


import java.awt.AWTException;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TestListeners implements ITestListener{
	
	private ScreenRecorderHelper screenRecord;
	
	static int numberOfTestCasesRun = 0;
	static int numberOfTestCasesPassed = 0;
	static int numberOfTestCasesSkipped = 0;

	private boolean failedTestsObtained = false;

	private static Map<ITestResult, Integer> retryCountMap = new HashMap<>();

	private static Map<String, List<ITestResult>> failedTestsMap = new HashMap<>();
	private static List<ITestResult> failedTests = new ArrayList<>();
	private static boolean testsFailed = false;
	private static int numberOfTestCasesFailed = 0;
	private static String suiteName;





	public TestListeners() {
		/*try {
			screenRecord = new ScreenRecorderHelper();
		} catch (IOException | AWTException e) {
			e.printStackTrace();
		}*/
	}

	public String getTestCaseName(ITestResult test) {
		String[] className = test.getMethod().getTestClass().getName().split("\\.");
		return className[className.length-1].toUpperCase()+"."+test.getName();
	}
	
	public String getTestCaseDescription(ITestResult test) {
		return test.getMethod().getDescription().length() < 1 ? getTestCaseName(test) : test.getMethod().getDescription() ;
	}
	
	
	 @Override
	 public void onStart(ITestContext result) {
		 /*PropertyHelpers.loadFile();
		 Log.info("Starting Test suite "+result.getStartDate());*/
	 }
	
	 @Override
	 public void onFinish(ITestContext result) {

		 Log.info("On test finish" + "Count of failed Tests" + numberOfTestCasesFailed);
		 ExtentReportManager.getExtentReports().flush();
		 if(SENT_REPORT_TO_USER_IN_EMAIL.trim().equalsIgnoreCase(YES) && REPORT_SENT_EMAIL.length() > 0) {
			 Log.info("Email sending is started............");
			 EmailHelpers.sendEmailWithTestCounts(numberOfTestCasesRun,numberOfTestCasesPassed,numberOfTestCasesFailed,numberOfTestCasesSkipped);
			 Log.info("Test Report is sent to this email : "+REPORT_SENT_EMAIL);
		 }
		 Log.info("Finish Test suite "+result.getEndDate());
		 DriverActions.softAssertAll();
		 DriverManager.quit();
		/* if (testsFailed && !failedTestsObtained) {
			 failedTests.addAll(result.getFailedTests().getAllResults());
			 failedTestsObtained = true;
			 System.out.println("retry failed Tests");
			 Reporter.log("retry failed Tests");
			 retryFailedTests();
		 } else if (testsFailed) {
			 System.out.println("retry failed Tests");
			 Reporter.log("retry failed Tests");
			 retryFailedTests();
		 }*/

		 /*// retry failed tests for this suite, if any
		 if (!failedTests.isEmpty()) {
			 System.out.println("Retrying failed tests for suite " + suiteName + "...");
			 retryFailedTests(failedTests);
			 failedTests.clear(); // clear the list to ensure it is only called once
		 }

		 // clear the map for this suite
		 failedTestsMap.remove(suiteName);*/

	 }
	 
	 @Override
	 public void onTestStart(ITestResult result) {
		 numberOfTestCasesRun+=1;
		 Log.info(getTestCaseDescription(result)+" test case is starting.. ");
		 ExtentTestManager.saveToReport("TC_0"+numberOfTestCasesRun+" : "+getTestCaseName(result),"<b style=\"font-size:20px\">" + getTestCaseDescription(result) + "</b>");
		 ExtentTestManager.logMessage(Status.INFO, "OS : "+System.getProperty("os.name")+" | Browser : "+DriverActions.getBrowserInformation());
		 ExtentTestManager.addBrowsers();
		 Log.info("OS : "+System.getProperty("os.name")+" | Browser : "+DriverActions.getBrowserInformation());
		 
		 if(RECORD_VIDEO.toLowerCase().trim().equals(YES)) {
			 screenRecord.startRecording(getTestCaseName(result));
		 }
	 }




/*
	@AfterSuite
	 public static void retryFailedTests() {
		// code to retry failed tests using TestNG
		List<ITestResult> failedTests = TestListeners.getFailedTests();
		List<ITestResult> permanentFailures = new ArrayList<>();
		System.out.println("Count of failed Tests" + numberOfTestCasesFailed);
		System.out.println("Count of failed Tests: " + failedTests.size());
		Reporter.log("Count of failed Tests: " + failedTests.size());
		if (!failedTests.isEmpty()) {
			System.out.println("Retrying failed tests...");
			TestNG retryRunner = new TestNG();
			XmlSuite retrySuite = new XmlSuite();
			retrySuite.setName("RetrySuite");
			XmlTest retryTest = new XmlTest(retrySuite);
			retryTest.setName("RetryTest");
			retryTest.setXmlSuite(retrySuite);
			List<XmlClass> retryClasses = new ArrayList<>();
			Map<String, Integer> retryCountMap = new HashMap<>();
			int maxRetries = 2; // set maximum number of retries here
			for (ITestResult result : failedTests) {
				System.out.println("Inside loop" + numberOfTestCasesFailed);
				String methodName = result.getMethod().getMethodName();
				String className = result.getMethod().getRealClass().getName();
				String key = className + "." + methodName;
				int retryCount = retryCountMap.getOrDefault(key, 0);
				if (retryCount < maxRetries) {
					retryCountMap.put(key, retryCount + 1);
					XmlClass xmlClass = new XmlClass(result.getMethod().getRealClass());
					xmlClass.getIncludedMethods().add(new XmlInclude(methodName));
					retryClasses.add(xmlClass);
				} else {
					// mark as permanent failure if maximum retries reached
					System.out.println("Maximum retries reached for " + key + ", marking as permanent failure.");
					permanentFailures.add(result);
				}
			}
			TestListeners.getFailedTests().remove(permanentFailures);
			retryTest.setXmlClasses(retryClasses);
			retryRunner.setXmlSuites(Collections.singletonList(retrySuite));
			retryRunner.run();
			failedTests.clear();
			testsFailed = false;
		}
	}
*/

	/*@AfterSuite
	public static void retryFailedTests(List<ITestResult> failedTests) {
		// code to retry failed tests using TestNG
		List<ITestResult> permanentFailures = new ArrayList<>();
		System.out.println("Count of failed Tests: " + failedTests.size());
		Reporter.log("Count of failed Tests: " + failedTests.size());
		if (!failedTests.isEmpty()) {
			System.out.println("Retrying failed tests...");
			TestNG retryRunner = new TestNG();
			XmlSuite retrySuite = new XmlSuite();
			retrySuite.setName("RetrySuite");
			XmlTest retryTest = new XmlTest(retrySuite);
			retryTest.setName("RetryTest");
			retryTest.setXmlSuite(retrySuite);
			List<XmlClass> retryClasses = new ArrayList<>();
			Map<String, Integer> retryCountMap = new HashMap<>();
			int maxRetries = 2; // set maximum number of retries here
			for (ITestResult result : failedTests) {
				String methodName = result.getMethod().getMethodName();
				String className = result.getMethod().getRealClass().getName();
				String key = className + "." + methodName;
				int retryCount = retryCountMap.getOrDefault(key, 0);
				if (retryCount < maxRetries) {
					retryCountMap.put(key, retryCount + 1);
					XmlClass xmlClass = new XmlClass(result.getMethod().getRealClass());
					xmlClass.getIncludedMethods().add(new XmlInclude(methodName));
					retryClasses.add(xmlClass);
				} else {
					// mark as permanent failure if maximum retries reached
					System.out.println("Maximum retries reached for " + key + ", marking as permanent failure.");
					permanentFailures.add(result);
				}
			}
			failedTestsMap.clear(); // clear the map of failed tests
			TestListeners.numberOfTestCasesFailed -= permanentFailures.size(); // subtract permanent failures from total failures
			TestListeners.failedTestsMap.putAll(failedTestsMap); // update the map of failed tests
			//TestListeners.getFailedTests().removeAll(permanentFailures); // remove permanent failures from the list of failed tests
			retryTest.setXmlClasses(retryClasses);
			retryRunner.setXmlSuites(Collections.singletonList(retrySuite));
			retryRunner.run();
			failedTests.clear();
			testsFailed = false;
		}
	}
*/

	@Override
	 public void onTestSuccess(ITestResult result) {
		 if(SCREENSHOT_PASS.equals(YES)) {
			 ExtentTestManager.addScreenShot(Status.PASS, "SCREENSHOT. Click base64 img");
		 }
		 
		 Log.info(getTestCaseDescription(result)+ " is passed");
		 ExtentTestManager.logMessage(Status.PASS, getTestCaseName(result)+ " test case is passed");
		 AllureManager.saveLog(getTestCaseName(result)+ " is passed");
		 
		 if(RECORD_VIDEO.toLowerCase().trim().equals(YES)) {
			 screenRecord.stopRecording(true);
		 }
		 numberOfTestCasesPassed+=1;

		// Run time reporter
		{
			String className = result.getInstance().getClass().getName();
			String methodName = result.getMethod().getMethodName();
			String status = "Passed";
		//	String stackTrace = result.getThrowable().toString();

			// Create an HTML file for storing failure information
			String htmlFileName = System.getenv("WORKSPACE") + "/RunTimeReporter.html";
			try (FileWriter writer = new FileWriter(htmlFileName, true)) {
				writer.write("<html><head><title>Test Pass Report</title></head><body>");
				writer.write("<h1>Test Failure Report</h1>");
				writer.write("<p>Total Test Cases Passed: " + numberOfTestCasesPassed + "</p>");
				writer.write("<p>Total Test Cases Failed: " + numberOfTestCasesFailed + "</p>");
				writer.write("<p>Total Test Cases Skipped: " + numberOfTestCasesSkipped + "</p>");
				writer.write("<table border=\"1\" cellpadding=\"5\">");
				writer.write("<tr>");
				writer.write("<th>Test Class</th>");
				writer.write("<th>Test Method</th>");
				writer.write("<th>Status</th>");
				writer.write("<th>Stack Trace</th>");
				writer.write("</tr>");

				writer.write("<tr>");
				writer.write("<td>" + className + "</td>");
				writer.write("<td>" + methodName + "</td>");
				writer.write("<td>" + status + "</td>");
			//	writer.write("<td>" + stackTrace.replace("\n", "<br>") + "</td>");
				writer.write("</tr>");

				writer.write("</table>");
				writer.write("<hr>");
				writer.write("</body></html>");

				System.out.println("Failure information saved to " + htmlFileName);
			} catch (IOException e) {
				System.out.println("Failed to save failure information to HTML file: " + e.getMessage());
			}




		}

	}
	 
	 @Override
	 @Step("Test failed. Attaching screenshot")
	 public void onTestFailure(ITestResult result) {

		 numberOfTestCasesFailed++;
		 if(SCREENSHOT_FAIL.equals(YES)) {
			 ExtentTestManager.addScreenShot(Status.FAIL, "SCREENSHOT. Click base64 img");
		 }
			/* try {
				 byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
				 Allure.addAttachment("Failed step", new ByteArrayInputStream(screenshotBytes));
				 System.out.println("Screenshot captured and attached to Allure report.");
			 } catch (Exception e) {
				 System.out.println("Failed to capture and attach screenshot to Allure report: " + e.getMessage());
			 }*/

		 Log.error(getTestCaseName(result)+ " test case is failed");
		 Log.error(result.getThrowable().toString());
		 ExtentTestManager.logMessage(Status.FAIL, getTestCaseName(result)+ " test case is failed");
		 ExtentTestManager.logMessage(Status.FAIL, result.getThrowable().toString());
		 
		 AllureManager.saveLog(getTestCaseName(result)+ " test case is failed");
		 AllureManager.saveLog(result.getThrowable().toString());
		 try {
			 byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
			 Allure.addAttachment("SCREENSHOT. Click base64 img", new ByteArrayInputStream(screenshotBytes));
			 System.out.println("Screenshot captured and attached to Allure report.");
		 } catch (Exception e) {
			 System.out.println("Failed to capture and attach screenshot to Allure report: " + e.getMessage());
		 }
		// AllureManager.saveScreenshot();
		 
		 if(RECORD_VIDEO.toLowerCase().trim().equals(YES)) {
			 screenRecord.stopRecording(true);
		 }

        // Run time reporter
		 {
			 String className = result.getInstance().getClass().getName();
			 String methodName = result.getMethod().getMethodName();
			 String status = "Failed";
			 String stackTrace = result.getThrowable().toString();

			 // Create an HTML file for storing failure information
			 String htmlFileName = System.getenv("WORKSPACE") + "/FailedOnes.html";
			 try (FileWriter writer = new FileWriter(htmlFileName, true)) {
				 writer.write("<html><head><title>Test Failure Report</title></head><body>");
				 writer.write("<h1>Test Failure Report</h1>");
				 writer.write("<p>Total Test Cases Passed: " + numberOfTestCasesPassed + "</p>");
				 writer.write("<p>Total Test Cases Failed: " + numberOfTestCasesFailed + "</p>");
				 writer.write("<p>Total Test Cases Skipped: " + numberOfTestCasesSkipped + "</p>");
				 writer.write("<table border=\"1\" cellpadding=\"5\">");
				 writer.write("<tr>");
				 writer.write("<th>Test Class</th>");
				 writer.write("<th>Test Method</th>");
				 writer.write("<th>Status</th>");
				 writer.write("<th>Stack Trace</th>");
				 writer.write("</tr>");

				 writer.write("<tr>");
				 writer.write("<td>" + className + "</td>");
				 writer.write("<td>" + methodName + "</td>");
				 writer.write("<td>" + status + "</td>");
				 writer.write("<td>" + stackTrace.replace("\n", "<br>") + "</td>");
				 writer.write("</tr>");

				 writer.write("</table>");
				 writer.write("<hr>");
				 writer.write("</body></html>");

				 System.out.println("Failure information saved to " + htmlFileName);
			 } catch (IOException e) {
				 System.out.println("Failed to save failure information to HTML file: " + e.getMessage());
			 }




		 }
		/* numberOfTestCasesFailed+=1;
		 failedTests.add(result);
		 testsFailed=true;*/



		 // new code  added  - Update the TestFinish method to store the failed test cases for each test tag separately:

		/* String testName = result.getTestContext().getCurrentXmlTest().getName();
		 List<ITestResult> failedTests = failedTestsMap.getOrDefault(testName, new ArrayList<>());
		 failedTests.add(result);
		 failedTestsMap.put(testName, failedTests);
		 numberOfTestCasesFailed++;
		 testsFailed = true;*/

	 }

	/*public static List<ITestResult> getFailedTests() {
		return failedTests;
	}*/

	/*public static List<ITestResult> getFailedTests() {
		List<ITestResult> allFailedTests = new ArrayList<>();
		for (List<ITestResult> failedTests : failedTestsMap.values()) {
			allFailedTests.addAll(failedTests);
		}
		return allFailedTests;
	}*/
	 @Override
	 public void onTestSkipped(ITestResult result) {
		 if(SCREENSHOT_FAIL.equals(YES)) {
			 ExtentTestManager.addScreenShot(Status.SKIP, "SCREENSHOT. Click base64 img");
		 }
		 Log.warn(getTestCaseName(result)+ " test case is skipped");
		 ExtentTestManager.logMessage(Status.SKIP, getTestCaseName(result)+ " test case is skipped");
		 ExtentTestManager.logMessage(Status.SKIP, result.getThrowable().toString());
		 
		 AllureManager.saveLog(getTestCaseName(result)+ " test case is skipped");
		 
		 if(RECORD_VIDEO.toLowerCase().trim().equals(YES)) {
			 screenRecord.stopRecording(true);
		 }
		 numberOfTestCasesSkipped+=1;

		 // Run time reporter
		 {
			 String className = result.getInstance().getClass().getName();
			 String methodName = result.getMethod().getMethodName();
			 String status = "Skipped";
		//	 String stackTrace = result.getThrowable().toString();

			 // Create an HTML file for storing failure information
			 String htmlFileName = System.getenv("WORKSPACE") + "/SkippedOnes.html";
			 try (FileWriter writer = new FileWriter(htmlFileName, true)) {
				 writer.write("<html><head><title>Test Skipped Report</title></head><body>");
				 writer.write("<h1>Test Failure Report</h1>");
				 writer.write("<p>Total Test Cases Passed: " + numberOfTestCasesPassed + "</p>");
				 writer.write("<p>Total Test Cases Failed: " + numberOfTestCasesFailed + "</p>");
				 writer.write("<p>Total Test Cases Skipped: " + numberOfTestCasesSkipped + "</p>");
				 writer.write("<table border=\"1\" cellpadding=\"5\">");
				 writer.write("<tr>");
				 writer.write("<th>Test Class</th>");
				 writer.write("<th>Test Method</th>");
				 writer.write("<th>Status</th>");
				 writer.write("<th>Stack Trace</th>");
				 writer.write("</tr>");

				 writer.write("<tr>");
				 writer.write("<td>" + className + "</td>");
				 writer.write("<td>" + methodName + "</td>");
				 writer.write("<td>" + status + "</td>");
			//	 writer.write("<td>" + stackTrace.replace("\n", "<br>") + "</td>");
				 writer.write("</tr>");

				 writer.write("</table>");
				 writer.write("<hr>");
				 writer.write("</body></html>");

				 System.out.println("Failure information saved to " + htmlFileName);
			 } catch (IOException e) {
				 System.out.println("Failed to save failure information to HTML file: " + e.getMessage());
			 }




		 }

	 }	 
	 
}
