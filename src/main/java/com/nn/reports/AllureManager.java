package com.nn.reports;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.nn.drivers.DriverManager;

import io.qameta.allure.Attachment;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class AllureManager implements TestLifecycleListener {

	@Attachment(value = "{0}", type = "text/plain")
	public static String saveLog(String message) {
		return message;
	}

	@Attachment(value = "{0}", type = "text/html")
	public static String attachHtml(String html) {
		return html;
	}

	@Attachment(value="Screenshot", type = "image/png")
	public static byte[] saveScreenshot() {
		return ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
	}

	@Attachment(value= "{name}", type = "image/png")
	public static byte[] saveScreenshot(String name) {
		return ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
	}

/*
	@Step(value = "{0}")
	public static String log(String description) {
		return description;
	}
*/

	public void onTestFailure(ITestResult result) {
		saveScreenshot();
		io.qameta.allure.Allure.getLifecycle().updateTestCase(
				tc -> tc.setStatus(Status.FAILED)
		);
	}

	public void onTestSuccess(ITestResult result) {
		saveScreenshot();
		io.qameta.allure.Allure.getLifecycle().updateTestCase(
				tc -> tc.setStatus(Status.PASSED)
		);
	}

}
