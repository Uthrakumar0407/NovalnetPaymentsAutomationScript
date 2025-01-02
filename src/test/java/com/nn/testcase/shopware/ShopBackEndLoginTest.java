package com.nn.testcase.shopware;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.nn.apis.ShopwareAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.BlikCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.pages.shopware.AdminLoginPage;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.pages.shopware.base.Shopware;
import com.nn.pages.shopware.base.ShopwareOrderStatus;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import com.nn.utilities.ShopwareUtils;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Map;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.EXPLICIT_TIMEOUT;
import static com.nn.constants.Constants.HEADLESS;
import static com.nn.language.NovalnetCommentsEN.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.DriverActions.*;
import static com.nn.utilities.ShopwareUtils.*;

public class ShopBackEndLoginTest  {

    private By adminDashboard = By.cssSelector(".sw-admin-menu__header");
    private By userName = By.cssSelector("#sw-field--username");
    private By password = By.cssSelector("#sw-field--password");
    private By loginBtn = By.cssSelector(".sw-login__submit > button");

    @Test(invocationCount = 5)
    public void testLogin() throws Exception {

        WebDriver driver = chromeDriver();
        DriverManager.setDriver(driver);
        driver.get(ShopwareUtils.SHOP_BACK_END_URL);
            login();
            driver.close();

    }

    public void login() throws Exception {
        scrollToElement(userName);
        highlightElement(userName);
        addScreenShot("Shop backend login page screenshot");
        setText1(userName, ShopwareUtils.SHOP_BACKEND_USERNAME);
        scrollToElement(password);
        highlightElement(password);
        addScreenShot("Shop backend login page screenshot");
        setText1(password,ShopwareUtils.SHOP_BACKEND_PASSWORD);
        sleep(20);
        addScreenShot("Shop backend login page screenshot");
        waitForElementRefreshed(loginBtn);
        waitForElementClickable(loginBtn);
        scrollToElement(loginBtn);
        highlightElement(loginBtn);
        addScreenShot("Shop backend login page screenshot");
        //clickElementByRefreshing(loginBtn);
        DriverManager.getDriver().findElement(loginBtn).isDisplayed();
        waitForStaleness(loginBtn);
        addScreenShot("Shop backend login page screenshot");
        DriverManager.getDriver().findElement(loginBtn).click();
        addScreenShot("Shop backend login page screenshot");

        addScreenShot("Shop backend login page screenshot");
     //  clickElementWithJs1(loginBtn);
       sleep(120);
        addScreenShot("Shop backend login page screenshot");
        DriverActions.getElements(By.cssSelector(".sw-alert__close")).forEach(e->{try {e.click();}catch (Exception ignored){}});
        addScreenShot("Shop backend login page screenshot");
        //DriverActions.waitForElementVisible(adminDashboard,60);
        DriverActions.getElements(By.cssSelector(".sw-alert__close")).forEach(e->{try {e.click();}catch (Exception ignored){}});
        addScreenShot("Shop backend login page screenshot");
        addScreenShot("Shop backend login page screenshot");
    }


    private  WebDriver chromeDriver() {
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

    @Step("Add screenshot to report")
    public static void addScreenShot(String info) {
        String base64Image = "data:image/png;base64," + ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BASE64);
        byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment(info, new ByteArrayInputStream(screenshotBytes));
    }


    @Step("Set text {1} on {0}")
    public void setText1(By by, String text) {
        //waitForPageLoad();
        waitForElementVisible1(by);

        clearText1(by);
        getElement1(by).sendKeys(text);
        Log.info("Set text : '"+text+"' on element : "+by.toString());

    }

    public boolean waitForElementVisible1(By by) {
        try {
            WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
            wait.until(ExpectedConditions.visibilityOfElementLocated(by));
            return true;
        }catch(Throwable error) {
            Log.error("Timeout waiting for the element Visible. "+by.toString());
            Assert.fail("Timeout waiting for the element Visible. " + by.toString());
            return false;
        }
    }

    @Step("Clear text element {0}")
    public void clearText1(By by) {
        //waitForPageLoad();
        waitForElementVisible(by);

        getElement1(by).clear();
        Log.info("Clear text on element : "+by.toString());

    }

    public WebElement getElement1(By by) {
        return DriverManager.getDriver().findElement(by);
    }

    @Step("Click element {0}")
    public  void clickElementWithJs1(By by) {
        //waitForPageLoad();
        waitForElementVisible1(by);

        scrollToElement(by);
        getJsExecutor().executeScript("arguments[0].click();", getElement1(by));
        Log.info("Click Element with Js: "+by);

    }
}
