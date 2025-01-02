package com.nn.pages.shopware.base;

import com.nn.drivers.DriverManager;
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

import static com.nn.constants.Constants.HEADLESS;

public class BaseTest extends BasePage{


    @BeforeSuite(alwaysRun = true)
    @Parameters({"BROWSER"})
    public void verifyGlobalConfig(@Optional("chrome") String browser) {
     //   WebDriverManager.chromedriver().setup();
        WebDriver driver = setupBrowser(browser);
        DriverManager.setDriver(driver);
        ExtentTestManager.saveToReport("verifyGlobalConfiguration","Check whether the global configuration set in shop backend and payments activated");
     //  verifyGlobalConfigAndActivatePayments();
        DriverManager.quit();
    }

    @AfterSuite(alwaysRun = true)
    public void clear(){
        DriverManager.quit();
    }

    @BeforeTest(alwaysRun = true)
    @Parameters({"BROWSER"})
    public void createDriver(@Optional("chrome") String browser) {
        WebDriver driver = setupBrowser(browser);
        DriverManager.setDriver(driver);
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
     //   System.setProperty("webdriver.chrome.driver", "chromedriver");
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
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
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
