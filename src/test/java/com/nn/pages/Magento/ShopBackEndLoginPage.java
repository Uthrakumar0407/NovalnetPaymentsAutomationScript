package com.nn.pages.Magento;

import com.aventstack.extentreports.Status;
import com.nn.Magento.Constants;
import com.nn.drivers.DriverManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;

import static com.nn.Magento.Constants.*;
import static com.nn.utilities.DriverActions.*;

public class ShopBackEndLoginPage {

    private By signIn = By.cssSelector("li.link.authorization-link > a[href*=\"account/login\"]");
    private By customerLoginTitle = By.cssSelector("h1.page-title span.base[data-ui-id=\"page-title-wrapper\"]");
    private By userName = By.cssSelector("input[id='username']");
    private By password = By.cssSelector("#login");

    private By signInBtn = By.xpath("//span[text()='Sign in']");
    private By storesMenu = By.cssSelector("li.item-stores > a");
    private By storesMenuConfiguration = By.cssSelector("li.item-system-config > a");

    private By salesMenuConfiguration = By.xpath("//div[@class='admin__page-nav-title title _collapsible' and @data-role='title']/strong[text()='Sales']/..");

    private By checkoutConfiguration = By.xpath("//span[text()='Checkout']");

    private By paymentMethodsConfiguration = By.xpath("//span[text()='Payment Methods']");

    private By novalnetConfigureButton = By.xpath("//button[@id='payment_us_novalnet_global-head']");

    private By novalnetConfigurationButton = By.xpath("//a[@id='payment_us_novalnet_global_novalnet-head']");

    private By apiPaymentKey = By.cssSelector("#payment_us_novalnet_global_novalnet_signature");

    private By apiAccessKey = By.cssSelector("#payment_us_novalnet_global_novalnet_payment_access_key");

    private By projectTariffID = By.cssSelector("#payment_us_novalnet_global_novalnet_tariff_id");

    private By paymentMethod = By.cssSelector("#payment_us_novalnet_global_novalnet_active");

    private By onHoldOrder = By.cssSelector("#payment_us_novalnet_global_novalnet_onhold_status");

    private By onCompleteOrder = By.cssSelector("#payment_us_novalnet_global_novalnet_order_status");

    private By callBackUrlLink = By.cssSelector("#payment_us_novalnet_global_novalnet_merchant_script-head");

    private By novalnetAllowManualTestCallbackDropDown = By.cssSelector("#payment_us_novalnet_global_novalnet_merchant_script_test_mode");

    private By saveChangesBtn = By.cssSelector("#save");

    private By successMessageAlert =By.xpath("//div[@class='message message-success success']");

    private By attentionAlert =By.xpath("//button[@class='action-primary action-accept']");

    private By activateBtn = By.cssSelector("#payment_us_novalnet_global_novalnet_vendor_config");

    private By activateOkBtn = By.xpath("//button[@class='action-primary action-accept']");

    private By productRemainCartDropdown = By.cssSelector("#payment_us_novalnet_global_novalnet_restore_cart");

    private boolean toSave=false;
    String successMessage ="You saved the configuration.";



    @Step("Load Shop End User Sign in Home Page")
    public void load() {
        DriverManager.getDriver().get(SHOP_BACK_END_URL);
        waitForTitleContains("Magento Admin");
    }

    @Step("Sigin to Shop")
    public void SigninToShop(String user, String pass) {
        load();
        login(user,pass);
    }


    @Step("Login to Shop Back End Admin Portal")
    public void login(String user, String pass) {
        if(waitForElementVisible(userName,10,"Waiting for admin login")){
            setTextWithoutClear(userName,user);
            setTextWithoutClear(password,pass);
            clickElementWithJs(signInBtn);
            waitForElementVisible(storesMenu);
            if(waitForElementVisible(By.cssSelector("button.action-close"),3,"")){
                clickElementWithJs(By.cssSelector("button.action-close"));
                sleep(1);
            }
        }
    }

    @Step("Load Global Config")
    public void loadShopGlobalConfig_deprecate() {
        clickElementByRefreshing(storesMenu);
        waitForElementVisible(storesMenuConfiguration,60);
        clickElementWithJs(storesMenuConfiguration);
        waitForElementVisible(salesMenuConfiguration);
        sleep(0.2);
        clickElementWithJs(salesMenuConfiguration);
        if (checkElementDisplayed(attentionAlert)){handleStaleElement(attentionAlert,d->d.findElement(attentionAlert).click());}
        sleep(1);
        int maxTry = 5;
        while (maxTry > 0){
            if(getElementAttributeText(salesMenuConfiguration,"aria-expanded").equals("true")){
                break;
            }else {
                clickElementWithAction(salesMenuConfiguration);
                sleep(1);
            }
            maxTry--;
        }
        waitForElementVisible(paymentMethodsConfiguration);
        clickElementWithJs(paymentMethodsConfiguration);
        if (checkElementDisplayed(attentionAlert)){handleStaleElement(attentionAlert,d->d.findElement(attentionAlert).click());}
        waitForElementVisible(novalnetConfigureButton);
        clickElementWithJs(novalnetConfigureButton);
        waitForElementVisible(novalnetConfigurationButton);
        clickElementWithJs(novalnetConfigurationButton);
        waitForElementVisible(projectTariffID);
        pressEnter(projectTariffID);
        pressEnter(projectTariffID);
        sleep(1);
        if (checkElementDisplayed(attentionAlert)){handleStaleElement(attentionAlert,d->d.findElement(attentionAlert).click());}

    }
    @Step("Load Global Config")
    public void loadShopGlobalConfig() {
        waitForElementPresent(storesMenuConfiguration);
        openURL(getElement(storesMenuConfiguration).getAttribute("href"));
        waitForElementVisible(By.cssSelector("#system_config_tabs"));
        openURL(getElement(By.xpath("//span[text()='Payment Methods']/parent::a")).getAttribute("href"));
        if (checkElementDisplayed(attentionAlert)){handleStaleElement(attentionAlert,d->d.findElement(attentionAlert).click());}
        waitForElementVisible(novalnetConfigureButton);
        clickElementWithJs(novalnetConfigureButton);
        waitForElementVisible(novalnetConfigurationButton);
        clickElementWithJs(novalnetConfigurationButton);
        waitForElementVisible(projectTariffID);
        pressEnter(projectTariffID);
        pressEnter(projectTariffID);
        sleep(1);
        if (checkElementDisplayed(attentionAlert)){handleStaleElement(attentionAlert,d->d.findElement(attentionAlert).click());}
    }

    @Step("Set value for product remain in cart after transaction cancelled")
    public void enableProductRemainCartDropdown(boolean enabled){
        String value = enabled ? "Yes" : "No";
        handleStaleElement(productRemainCartDropdown,driver -> {
            Select s = new Select(driver.findElement(productRemainCartDropdown));
            if(!s.getFirstSelectedOption().getText().equals(value)){
                s.selectByVisibleText(value);
                clickElementWithJs(saveChangesBtn);
                waitForElementVisible(successMessageAlert);
            }
        });
    }
    @Step("Load CallBack Settings")
    public void loadCallBackSettings() {
        clickElementWithJs(callBackUrlLink);
        waitForElementVisible(novalnetAllowManualTestCallbackDropDown);
      }

    @Step("Set Order Status")
    public void setShopOrderStatus() {
        String onHoldOrderStatus = getDropdownSelectedOptionValueOrEmpty(onHoldOrder);
        String onCompleteOrderStatus = getDropdownSelectedOptionValueOrEmpty(onCompleteOrder);
        if (!verifyElementTextEquals(onHoldOrderStatus, "holded")) {
            selectDropdownByValue(onHoldOrder, "holded");
            pressTab(onHoldOrder);
            toSave=true;

        }

        if (!verifyElementTextEquals(onCompleteOrderStatus, "complete")) {
            selectDropdownByValue(onCompleteOrder, "complete");
            pressTab(onCompleteOrder);
            toSave=true;

        }
    }
    @Step("Set CallBack to Allow Manual Yes")
    public void allowManualTestingOfWebhook() {
        String allowManualCallBackStatus = getDropdownSelectedOptionValueOrEmpty(novalnetAllowManualTestCallbackDropDown);
        if (!verifyElementTextEquals(allowManualCallBackStatus, "1")) {
            selectDropdownByValue(novalnetAllowManualTestCallbackDropDown, "1");
            pressTab(novalnetAllowManualTestCallbackDropDown);
            toSave=true;

        }
    }

    @Step("Activate Project")
    public void activateProject() {
        clickElementWithJs(activateBtn);
        waitForElementVisible(activateOkBtn);
        clickElementWithJs(activateOkBtn);
        waitForElementVisible(projectTariffID);
    }

    @Step("Verify and Set Shop Global Config")
    public void verifyShopGlobalConfig() {

        String apiKey = getInputFieldText(apiPaymentKey);
        String accessKey = getInputFieldText(apiAccessKey);
        String paymentMethodValue = getDropdownSelectedOptionValueOrEmpty(paymentMethod);
        String tariffID = getSelectedOptionByRefreshing(projectTariffID);

        if (!apiKey.equals(NOVALNET_API_KEY) || !accessKey.equals(NOVALNET_ACCESSKEY) || !tariffID.equals(NOVALNET_TARIFF)) {
            if (!verifyElementTextEquals(apiKey, NOVALNET_API_KEY)) {
                setText(apiPaymentKey, NOVALNET_API_KEY);
                pressTab(apiPaymentKey);
                toSave=true;

            }
            if (!verifyElementTextEquals(accessKey, NOVALNET_ACCESSKEY)) {
                setText(apiAccessKey, NOVALNET_ACCESSKEY);
                pressTab(apiAccessKey);
                toSave=true;

            }
            if (!verifyElementTextEquals(getDropdownSelectedOptionValueOrEmpty(projectTariffID), NOVALNET_TARIFF)) {
                activateProject();
                selectDropdownByValue(projectTariffID, NOVALNET_TARIFF);
                pressTab(projectTariffID);
                toSave=true;


            }
            if (!verifyElementTextEquals(getDropdownSelectedOptionValueOrEmpty(paymentMethod), "1")) {
                selectDropdownByValue(paymentMethod, "1");
                pressTab(paymentMethod);
                toSave=true;
            }
        }
            setShopOrderStatus();
            ExtentTestManager.addScreenShot(Status.PASS, "<b>Verification of Novalnet Global Configuration settings in shop backend</b>");
            loadCallBackSettings();
            allowManualTestingOfWebhook();
            if(toSave) {
                if (checkElementDisplayed(attentionAlert)){clickElementWithJs(attentionAlert);}
                waitForElementClickable(saveChangesBtn,1);
                clickElementWithJs(saveChangesBtn);
                waitForElementVisible(successMessageAlert);
                verifyElementTextEquals(getElementText(successMessageAlert), successMessage);
            }

        ExtentTestManager.addScreenShot(Status.PASS, "<b>Verification of Novalnet Global Configuration Call back URL settings in shop backend</b>");


    }


    @Step("Print Shop Global Config")
    public void printGlobalConfig(){
        loadShopGlobalConfig();
        verifyEquals(getDropdownSelectedOptionValueOrEmpty(projectTariffID), NOVALNET_TARIFF, "<b>Novalnet Tariff ID:</b>");
        verifyEquals(getInputFieldText(apiPaymentKey), NOVALNET_API_KEY, "<b>Novalnet API Key:</b>");
        verifyEquals(getInputFieldText(apiAccessKey), NOVALNET_ACCESSKEY, "<b>Novalnet Payment Access Key:</b>");
        verifyEquals(getDropdownSelectedOptionValueOrEmpty(paymentMethod), "1", "<b>Novalnet Payment Method Status ID:</b>");
        verifyEquals(getDropdownSelectedOptionValueOrEmpty(onHoldOrder), "holded", "<b>Novalnet On Hold Order Status:</b>");
        verifyEquals(getDropdownSelectedOptionValueOrEmpty(onCompleteOrder), "complete", "<b>Novalnet On Complete Order status:</b>");

        System.out.println(getDropdownSelectedOptionValueOrEmpty(projectTariffID));
        System.out.println(getInputFieldText(apiPaymentKey));
        System.out.println(getInputFieldText(apiAccessKey));
        System.out.println(getDropdownSelectedOptionValueOrEmpty(paymentMethod));
        System.out.println(getDropdownSelectedOptionValueOrEmpty(onHoldOrder));
        System.out.println(getDropdownSelectedOptionValueOrEmpty(onCompleteOrder));
        loadCallBackSettings();
        verifyEquals(getDropdownSelectedOptionValueOrEmpty(novalnetAllowManualTestCallbackDropDown), "1", "<b>Novalnet Allow call back status:</b>");
        System.out.println(getDropdownSelectedOptionValueOrEmpty(novalnetAllowManualTestCallbackDropDown));

    }
}


