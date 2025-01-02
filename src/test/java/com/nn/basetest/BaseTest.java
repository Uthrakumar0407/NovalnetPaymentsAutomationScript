package com.nn.basetest;



import com.nn.callback.CreditCardCallbackEvents;
import com.nn.callback.Przelewy24CallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.pages.BasePage;
import com.nn.pages.CheckoutPage;
import com.nn.pages.Magento.*;
import com.nn.pages.MyAccountPage;
import com.nn.pages.SuccessPage;
import com.nn.reports.ExtentTestManager;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.TestRunner;
import org.testng.annotations.*;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Listeners;

import static com.nn.constants.Constants.*;
import com.nn.drivers.DriverManager;
import com.nn.listeners.TestListeners;


import org.testng.annotations.Optional;
import org.testng.collections.Lists;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.*;

/*@Listeners(TestListeners.class)*/
public class BaseTest extends BasePage {

	int MAX_RETRIES = 2;
	int retryCount = 0;

	WooCommercePage wooCommerce = WooCommercePage.builder()
			.adminPage(new AdminPage())
			.dashBoardPage(new DashboardPage())
			.ordersPage(new OrdersPage())
			.settingsPage(new SettingsPage())
			.homePage(new HomePage())
			.productPage(new ProductPage())
			.cartPage(new CartPage())
			.checkoutPage(new CheckoutPage())
			.successPage(new SuccessPage())
			.myAccountPage(new MyAccountPage())
			.subscriptionPage(new SubscriptionPage())
			.callback(new CreditCardCallbackEvents())
			.testData(ExcelHelpers.xlReadPaymentCredentials())
			.txnInfo(new HashMap<>())
			.testData2(ExcelHelpers.declineCreditCards())
			.build();



	@BeforeTest(alwaysRun = true)
	@Parameters({"BROWSER"})
	public void createDriver(@Optional("chrome") String browser) {
		WebDriver driver = setupBrowser(browser);
		DriverManager.setDriver(driver);
	}
	
	@AfterTest(alwaysRun = true)
	public void quitDriver() {
		if(DriverManager.getDriver() != null)
			DriverManager.quit();
	}

	@AfterSuite(alwaysRun = true)
	public void clear(){
		DriverManager.quit();
	}

	public static WebDriver setupBrowser(String browserName) {
		WebDriver driver;
		switch(browserName.trim().toLowerCase()) {
		case "chrome" :
			driver = chromeDriver();
			break;
		case "firefox":
			driver = firefoxDriver();
			break;
		case "edge":
			driver = edgeDriver();
			break;
		default:
			System.out.println("Browser "+browserName+" name is invalid. Launching chrome as default");
			driver = chromeDriver();
		}
		return driver;
	}

	private static WebDriver chromeDriver() {
		WebDriver driver;
		System.out.println("Launching Chrome Driver...");
		System.setProperty("webdriver.http.factory", "jdk-http-client");
		ChromeOptions options = new ChromeOptions();
		if(HEADLESS)
			options.addArguments("--headless"); 	//options.addArguments("--headless=new");
		options.addArguments("--remote-allow-origins=*");
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-infobars");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-popup-blocking");
		driver = new ChromeDriver(options);
		if(HEADLESS)
			driver.manage().window().setSize(new Dimension(1920,1080));
		else
			driver.manage().window().maximize();
		return driver;
	}
	
	private static WebDriver firefoxDriver() {
		WebDriver driver;
		System.setProperty("webdriver.http.factory", "jdk-http-client");
		System.out.println("Launching FireFox Driver...");
		FirefoxOptions options = new FirefoxOptions();
		if(HEADLESS)
			options.addArguments("--headless");
		driver = new FirefoxDriver(options);
		if(HEADLESS)
		driver.manage().window().setSize(new Dimension(1920,1080));
		else
		driver.manage().window().maximize();
		return driver;
	}
	
	private static WebDriver edgeDriver() {
		WebDriver driver;
		System.out.println("Launching Edge Driver...");
		EdgeOptions options = new EdgeOptions();
		driver = new EdgeDriver(options);
		driver.manage().window().maximize();
		return driver;
	}

	public void validateGlobalCofigurations(){
		wooCommerce.getDashBoardPage().loadSettingsPage();
		wooCommerce.getSettingsPage().openNovalnetGlobalConfig();
		wooCommerce.getSettingsPage().validateGlobalConfigFields();

	}


}
