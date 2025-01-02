package com.nn.pages;

import static com.nn.utilities.DriverActions.*;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

import com.nn.constants.Constants;
import com.nn.drivers.DriverManager;
import com.nn.utilities.Log;

public class AdminPage {

	private By userName = By.cssSelector("#user_login");
	private By password = By.cssSelector("#user_pass");
	private By loginBtn = By.cssSelector("#wp-submit");
	private By languageDropdown = By.cssSelector("#language-switcher-locales");
	private By languageChangeBtn = By.cssSelector("input[value='Change']");

	@Step("Load Shop Admin Page")
	public AdminPage load() {
		DriverManager.getDriver().get(Constants.URL);
		waitForTitleContains("Log In");
		return this;
	}
	
//	public void openAdminPage() {
//		DriverActions.openURL(Constants.URL);
//		DriverActions.waitForTitleContains("WordPress");
//	}

	@Step("Open Shop Admin Page")
	public void openAdminPage() {
		DriverManager.getDriver().get(Constants.URL);
		Log.info("Open URL: "+Constants.URL);
		waitForTitleContains("WordPress");
	}
	
//	public DashboardPage adminLogin() {
//		DriverActions.setText(userName, Constants.USERNAME);
//		DriverActions.setText(password, Constants.PASSWORD);
//		DriverActions.clickElement(loginBtn);
//		return new DashboardPage();
//	}

	@Step("Login to Shop Admin Page")
	public DashboardPage adminLogin(String user,String pass) {
		openAdminPage();
		waitForElementVisible(userName,60);
		DriverManager.getDriver().findElement(userName).sendKeys(user);
		sleep(1);
		DriverManager.getDriver().findElement(password).sendKeys(pass);
		sleep(1);
		DriverManager.getDriver().findElement(loginBtn).click();
		waitForTitleContains("Dashboard", 60);
		return new DashboardPage();
	}
	
	private boolean isUserLoggedIn(){
		openAdminPage();
		return !waitForElementVisible(userName,5,"Waiting userNameField");
	}
	public void ifLoggedOutLogin(String user, String password){
		if(!isUserLoggedIn()){
			adminLogin(user,password);
		}
	}
}
