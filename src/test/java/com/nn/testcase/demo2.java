package com.nn.testcase;

import com.nn.apis.ShopwareAPIs;
import com.nn.basetest.BaseTest;
import com.nn.drivers.DriverManager;
import com.nn.pages.shopware.AdminLoginPage;
import com.nn.pages.shopware.CustomerLoginPage;
import com.nn.pages.shopware.SettingsPage;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DBUtil;
import com.nn.utilities.ShopwareUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.Map;

import static com.nn.callback.CallbackProperties.DIRECT_DEBIT_SEPA;
import static com.nn.callback.CallbackProperties.GUARANTEED_DIRECT_DEBIT_SEPA;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.pages.shopware.base.BaseTest.setupBrowser;
import static com.nn.utilities.DriverActions.*;
import static com.nn.utilities.ShopwareUtils.NOVALNET_ACCESSKEY;
import static com.nn.utilities.ShopwareUtils.NOVALNET_API_KEY;


public class demo2  extends BaseTest{

    private By productActivationKey = By.cssSelector("[id='NovalnetPayment.settings.clientId']");
    private By paymentAccessKey = By.cssSelector("[id='NovalnetPayment.settings.accessKey']");
    private By tariff = By.cssSelector("[name='NovalnetPayment.settings.tariff']");
    private By orderConfirmationEmail = By.xpath("//input[@name='NovalnetPayment.settings.emailMode']");
    private By callbackURL = By.cssSelector("[id='NovalnetPayment.settings.callbackUrl']");
    private By callbackUrlConfigureBtn = By.cssSelector(".novalnet_payment-settings-merchant-credentials > button");
    private By callbackEnableBtn = By.cssSelector("[name='NovalnetPayment.settings.deactivateIp']");
    private By shopLoader = By.cssSelector(".sw-loader-element");
    private By alertSuccess = By.xpath("//div[@class='sw-alert__title' and (contains(text(),'Success') or contains(text(),'Erfolg'))]");




    @Test
    public void verifyGlobalConfig() throws Exception{


            WebDriver driver = setupBrowser("chrome");
            DriverManager.setDriver(driver);
            ExtentTestManager.saveToReport("verifyGlobalConfiguration","Check whether the global configuration set in shop backend and payments activated");
        ExtentTestManager.saveToReport("setup", "Setting up prerequisite to place order");
        AdminLoginPage ap=new AdminLoginPage();
        ap.load();
        ap.login();
        SettingsPage sp=new SettingsPage();
        sp.load();
        sp.verifyGlobalConfiguration();
            DriverManager.quit();
        }


    @Step("Verify Global Configuration")
    public void verifyGlobalConfiguration(){
        //  closeAlert();
//        if(!waitForElementVisible(By.cssSelector(".novalnet-payment-settings-fields .sw-alert__message"),60,"")){
//            clickElementByRefreshing(By.cssSelector("li.navigation-list-item__sw-settings"));
//            clickElementByRefreshing(By.cssSelector(".sw-tabs-item.sw-settings__tab-plugins"));
//            clickElementByRefreshing(By.cssSelector("a#novalnet-payment"));
        //  waitForElementVisible(By.cssSelector(".novalnet-payment-settings-fields .sw-alert__message"));
        // }
        // scrollToElement(By.cssSelector(".novalnet-payment-settings-fields .sw-alert__message"));
        boolean flag = true;
        if(!getInputFieldText(productActivationKey).equals(NOVALNET_API_KEY)){
            handleStaleElement(productActivationKey,d->{
                WebElement e = d.findElement(productActivationKey);
                e.clear();
                e.sendKeys(NOVALNET_API_KEY);
            });
            clickOutsideForm();
            waitForElementDisable(shopLoader);
            if(!getInputFieldText(paymentAccessKey).equals(NOVALNET_ACCESSKEY)){
                handleStaleElement(paymentAccessKey,d->{
                    WebElement e = d.findElement(paymentAccessKey);
                    e.clear();
                    e.sendKeys(NOVALNET_ACCESSKEY);
                });
                clickOutsideForm();
                waitForElementDisable(shopLoader);
            }
            flag = true;
        }
        if(selectTariff()){
            flag = true;
        }
        if(!checkElementChecked(orderConfirmationEmail)){
            clickElementWithJs(orderConfirmationEmail);
            flag = true;
        }
        if(!checkElementChecked(callbackEnableBtn)){
            clickElementWithJs(callbackEnableBtn);
            flag = true;
        }
        if(flag){
            clickElementByRefreshing(By.cssSelector("button.sw-button--primary"));
            waitForElementVisible(alertSuccess);
            sleep(5);
        }

    }

    private boolean selectTariff(){
        clickElementByRefreshing(By.cssSelector("[name='NovalnetPayment.settings.tariff'] .sw-select__selection-indicators"));
        sleep(1);
        String css = ".sw-select-result-list__content li[class$='"+ ShopwareUtils.NOVALNET_TARIFF+"']";
        By tariffSelected = By.cssSelector(css);
        if(!checkElementDisplayed(By.cssSelector(css+" .icon--regular-checkmark-xs"))){
            clickElementByRefreshing(tariffSelected);
            return true;
        }
        return false;
    }

}
