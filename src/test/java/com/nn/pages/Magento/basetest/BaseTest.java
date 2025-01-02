package com.nn.pages.Magento.basetest;


import com.nn.callback.IdealCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.listeners.TestListeners;
import com.nn.pages.Magento.*;
import com.nn.reports.ExtentTestManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.*;

import java.util.HashMap;

import static com.nn.Magento.Constants.SHOP_BACKEND_PASSWORD;
import static com.nn.Magento.Constants.SHOP_BACKEND_USERNAME;
import static com.nn.constants.Constants.HEADLESS;

/*@Listeners(TestListeners.class)*/
public class BaseTest extends BasePage {

	int MAX_RETRIES = 2;
	int retryCount = 0;

	MagentoPage magentoPage = MagentoPage.builder()
			.novalnetAdminPortal(new NovalnetAdminPortal())
			.shopUserLoginPage(new ShopUserLoginPage())
			.shopBackEndLoginPage(new ShopBackEndLoginPage())
			.checkoutPage(new CheckoutPage())
			.successPage(new SuccessPage())
			.callback(new IdealCallbackEvents())
			.txnInfo(new HashMap<>())
			.build();



	//@BeforeSuite(alwaysRun = true)
	public void setUpGlobalConfiguration(@Optional("chrome") String browser) {
		ExtentTestManager.saveToReport("Setup","Setting up the Global Configuration Once.");
		WebDriverManager.chromedriver().setup();
		WebDriver driver = setupBrowser(browser);
		DriverManager.setDriver(driver);
		verifyGlobalConfiguration();
		DriverManager.quit();
	}

	@AfterSuite(alwaysRun = true)
	public void clear(){
		DriverManager.quit();
	}

	@BeforeTest(alwaysRun = true)
	@Parameters({"BROWSER"})
	public void createDriver(@Optional("chrome") String browser) {
		ExtentTestManager.saveToReport("Setup","Log into Shop using admin login before test starts.");
		WebDriver driver = setupBrowser(browser);
		DriverManager.setDriver(driver);
		magentoPage.getShopBackEndLoginPage().SigninToShop(SHOP_BACKEND_USERNAME,SHOP_BACKEND_PASSWORD);
	}
	
	@AfterTest(alwaysRun = true)
	public void quitDriver() {
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
			options.addArguments("--headless"); //options.addArguments("--headless=new");
		options.addArguments("--remote-allow-origins=*");
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-infobars");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-popup-blocking");
		options.addArguments("--no-sandbox");
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


}
