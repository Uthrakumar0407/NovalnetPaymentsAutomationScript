package com.nn.pages.shopware;

import static com.nn.utilities.DriverActions.*;

import com.nn.utilities.DriverActions;
import com.nn.utilities.ShopwareUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import static com.nn.utilities.ShopwareUtils.*;

public class SettingsPage {

    private By productActivationKey = By.cssSelector("[id='NovalnetPayment.settings.clientId']");
    private By paymentAccessKey = By.cssSelector("[id='NovalnetPayment.settings.accessKey']");
    private By tariff = By.cssSelector("[name='NovalnetPayment.settings.tariff']");
    private By orderConfirmationEmail = By.xpath("//input[@name='NovalnetPayment.settings.emailMode']");
    private By callbackURL = By.cssSelector("[id='NovalnetPayment.settings.callbackUrl']");
    private By callbackUrlConfigureBtn = By.cssSelector(".novalnet_payment-settings-merchant-credentials > button");
    private By callbackEnableBtn = By.cssSelector("[name='NovalnetPayment.settings.deactivateIp']");
    private By shopLoader = By.cssSelector(".sw-loader-element");
    private By alertSuccess = By.xpath("//div[@class='sw-alert__title' and (contains(text(),'Success') or contains(text(),'Erfolg'))]");

    public SettingsPage load(){
        openURL(SHOP_BACK_END_URL+"novalnet/payment/settings/credentials");
        closeAlert();
        return this;
    }

    private void closeAlert(){
        DriverActions.getElements(By.cssSelector(".sw-alert__close")).forEach(e->{
            try {
                e.click();
            }catch (Exception ignored){

            }
        });
    }

    public SettingsPage verifyGlobalConfiguration(){
        closeAlert();
        if(!waitForElementVisible(By.cssSelector(".novalnet-payment-settings-fields .sw-alert__message"),30,"")){
            clickElementByRefreshing(By.cssSelector("li.navigation-list-item__sw-settings"));
            clickElementByRefreshing(By.cssSelector(".sw-tabs-item.sw-settings__tab-plugins"));
            clickElementByRefreshing(By.cssSelector("a#novalnet-payment"));
           //waitForElementVisible(By.cssSelector(".novalnet-payment-settings-fields .sw-alert__message"));
            closeAlert();
        }
        // scrollToElement(By.cssSelector(".novalnet-payment-settings-fields .sw-alert__message")); //code review bug fix
        boolean flag = false;
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
        /*if(selectTariff()){
            flag = true;
        }*/
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
        return this;
    }

    private SettingsPage loadPaymentsPage(){
        openURL(ShopwareUtils.SHOP_BACK_END_URL+"sw/settings/payment/overview");
        waitForElementVisible(By.cssSelector(".sw-payment-card"));
        return this;
    }

    public void enablePayments(){
        loadPaymentsPage();
        String xpath = "//div[contains(text(),'Novalnet Payment') or contains(text(),'Novalnet Zahlung')]/ancestor::div[contains(@class,'sw-payment-card')]//input";
        if(!checkElementChecked(By.xpath(xpath))){
            clickElementWithJs(By.xpath(xpath));
            waitForElementVisible(alertSuccess);
        }
    }

    public void enablePayments(boolean enable){
        loadPaymentsPage();
        String xpath = "//div[contains(text(),'Novalnet Payment')]/ancestor::div[contains(@class,'sw-payment-card')]//input";
        if(enable != checkElementChecked(By.xpath(xpath))){
            clickElementWithJs(By.xpath(xpath));
            waitForElementVisible(alertSuccess);
        }
    }

    private boolean selectTariff(){
      //  clickElementByRefreshing(By.cssSelector("[name='NovalnetPayment.settings.tariff'] .sw-select__selection-indicators"));
        clickElementByRefreshing(By.xpath("(//div[@class='sw-select__selection-indicators'])[2]"));
        sleep(1);
        String css = ".sw-select-result-list__content li[class$='"+ShopwareUtils.NOVALNET_TARIFF+"']";
        By tariffSelected = By.cssSelector(css);
        if(!checkElementDisplayed(By.cssSelector(css+" .icon--regular-checkmark-xs"))){
            clickElementByRefreshing(tariffSelected);
            return true;
        }
        return false;
    }


}
