package com.nn.pages;

import org.openqa.selenium.By;

import com.aventstack.extentreports.Status;
import com.nn.constants.Constants;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;

public class LoginPage {

	private By loginUserName = By.id("sw-field--username");
	private By loginPassword = By.id("sw-field--password");
	private By loginButton = By.xpath("//button[@class='sw-button sw-login__login-action sw-button--primary']");
	private By loginForm = By.cssSelector(".sw-login-login");
	private By dashBoardLogo = By.cssSelector(".sw-version__title");
	
	public String login(String userName, String password) {
		DriverActions.openURL(Constants.URL);
		DriverActions.setText(loginUserName, userName);
		DriverActions.setText(loginPassword, password);
		DriverActions.highlightElement(loginForm);
		ExtentTestManager.addScreenShot(Status.INFO,"Shopware login form");
		DriverActions.clickElementWithJs(loginButton);
		DriverActions.waitForPageLoad();
		DriverActions.waitForElementVisible(dashBoardLogo);
		return DriverActions.getPageTitle();
	}
	
	
	
	
}
