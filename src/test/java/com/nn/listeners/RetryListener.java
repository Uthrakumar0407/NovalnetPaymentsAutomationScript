package com.nn.listeners;

import org.testng.*;

import com.nn.utilities.Log;

import java.util.ArrayList;
import java.util.List;

public class RetryListener implements IInvokedMethodListener, IRetryAnalyzer {

	private static final int MAX_RETRY_COUNT = 1;
	private int retryCount = 0;
	private List<ITestResult> failedTests = new ArrayList<>();

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		// Do nothing
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		if (testResult.getStatus() == ITestResult.FAILURE) {
			failedTests.add(testResult);
		}
	}

	@Override
	public boolean retry(ITestResult result) {
		if (retryCount < MAX_RETRY_COUNT) {
			retryCount++;
			return true; // Retry the test case
		}
		return false; // Do not retry the test case
	}

	public List<ITestResult> getFailedTests() {
		return failedTests;
	}
}