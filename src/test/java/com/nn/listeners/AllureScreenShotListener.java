package com.nn.listeners;

import com.nn.reports.ExtentTestManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.testng.*;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.*;

public class AllureScreenShotListener extends TestListenerAdapter {
    @Override
    public void onTestFailure(ITestResult result) {
        // Attach screenshot on failure
        attachScreenshot(result);
    }

   // @Step("Add screenshot to report")
   // @Attachment(value = "Screenshot", type = "image/png")
    public void attachScreenshot(ITestResult result) {
        // capture screenshot and return as bytes
        ExtentTestManager.addScreenShot("Capturing screenshot for test: " + result.getName());
    }

}