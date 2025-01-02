package com.nn.pages.shopware;

import static com.nn.pages.shopware.SubscriptionConfigPage.Config.*;
import static com.nn.utilities.DriverActions.*;

import com.nn.drivers.DriverManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ShopwareUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionConfigPage {
    private By customerCancelOption = By.cssSelector("input[name$='customerCancelOption']");
    private By notifyEmail = By.cssSelector("input[name$='notifyEmail']");
    private By nextCycleDate = By.cssSelector("input[name$='nextCycleDate']");
    private By cycleSuspend = By.cssSelector("input[name$='cycleSuspend']");
    private By changePaymentMethod = By.cssSelector("input[name$='changePaymentMethod']");
    private By changeProductQuantity = By.cssSelector("input[name$='changeProductQuantity']");
    private By calculateShippingOnce = By.cssSelector("input[name$='calculateShippingOnce']");
    private By reminderEmail = By.cssSelector("input[name$='reminderEmail']");
    private By mixedCheckout = By.cssSelector("input[name$='mixedCheckout']");
    private By retryPayment = By.cssSelector("input[name$='retryPayment']");


    public enum Config {
        SELECT_SALES_CHANNEL, SUBSCRIPTION_PAYMENT_METHODS, ALLOW_EU_CANCEL, SEND_CANCELLATION_NOTIFICATION, ALLOW_EU_CHANGE_CYCLE_DATE, ALLOW_EU_SUSPEND, ALLOW_EU_CHANGE_PAYMENT,
        ALLOW_EU_CHANGE_PRODUCT_AND_QUANTITY, CALCULATE_SHIPPING_FOR_INITIAL_ORDER, SEND_RENEWAL_REMINDER_TO_EU, MIXED_CHECKOUT, RETRY_FAILED_PAYMENT_UNTIL
    }

    private static void selectToggleButton(By by, Boolean expected) {
        Boolean actual = checkElementChecked(by);
        if (expected != actual) {
            clickElement(by);
        }
    }

    private static void setText(By by, String text) {
        if (!getInputFieldText(by).trim().equals(text)) {
            setTextWithAction(by, text);
        }
    }
    private static void selectAllSaleChannel() {
        By allSales = By.cssSelector("div[class$='select__selection-text']");
        if (!getElementText(allSales).equals("All Sales Channels")) {
            clickElement(allSales);
            clickElement(By.cssSelector("li[class$='sw-select-option--0']"));
        }
    }
    private static void selectNovalnetPayment() {
        List<WebElement> elementsList = getElements(By.xpath("//span[@class='sw-select-selection-list__item']"));
        for (WebElement element : elementsList) {
            if (!element.getText().trim().equals("Novalnet Payment")) {
                clickElement(By.xpath("//input[@class='sw-select-selection-list__input']"));
                clickElement(By.xpath("//span/div[@class='sw-highlight-text' and text() = 'Novalnet Payment']"));
                clickElement(By.xpath("//span[@class='sw-button__content']"));
            }
        }
    }
    public void setSubscriptionConfiguration(Map<Config, Object> configuration) {
        for (Map.Entry<Config, Object> kv : configuration.entrySet()) {
            switch (kv.getKey()) {
                case ALLOW_EU_CANCEL:
                    selectToggleButton(customerCancelOption, Boolean.parseBoolean(kv.getValue().toString()));
                    break;
                case SEND_CANCELLATION_NOTIFICATION:
                    setText(notifyEmail, kv.getValue().toString());
                    break;
                case ALLOW_EU_CHANGE_CYCLE_DATE:
                    selectToggleButton(nextCycleDate, Boolean.parseBoolean(kv.getValue().toString()));
                    break;
                case ALLOW_EU_SUSPEND:
                    selectToggleButton(cycleSuspend, Boolean.parseBoolean(kv.getValue().toString()));
                    break;
                case ALLOW_EU_CHANGE_PAYMENT:
                    selectToggleButton(changePaymentMethod, Boolean.parseBoolean(kv.getValue().toString()));
                    break;
                case ALLOW_EU_CHANGE_PRODUCT_AND_QUANTITY:
                    selectToggleButton(changeProductQuantity, Boolean.parseBoolean(kv.getValue().toString()));
                    break;
                case CALCULATE_SHIPPING_FOR_INITIAL_ORDER:
                    selectToggleButton(calculateShippingOnce, Boolean.parseBoolean(kv.getValue().toString()));
                    break;
                case SEND_RENEWAL_REMINDER_TO_EU:
                    selectToggleButton(reminderEmail, Boolean.parseBoolean(kv.getValue().toString()));
                    break;
                case MIXED_CHECKOUT:
                    selectToggleButton(mixedCheckout, Boolean.parseBoolean(kv.getValue().toString()));
                    break;
                case RETRY_FAILED_PAYMENT_UNTIL:
                    setText(retryPayment, kv.getValue().toString());
                    break;
                default:
                    throw new RuntimeException("Invalid key");
            }
        }
    }
    @Test
    private static void checkMethod() {
        DriverManager.setDriver(new ChromeDriver());
        ExtentTestManager.saveToReport("checkMethod", "");
        DriverManager.getDriver().get("http://192.168.2.140/keerthana/SW6/6532/public/admin#/sw/extension/config/NovalnetSubscription");
        DriverManager.getDriver().manage().window().maximize();
        getElement(By.cssSelector("#sw-field--username")).sendKeys("admin");
        getElement(By.cssSelector("#sw-field--password")).sendKeys("shopware");
        DriverActions.clickElement(By.cssSelector(".sw-login__submit > button"));
        selectNovalnetPayment();
//        clickElement(By.xpath("//input[@class='sw-select-selection-list__input']"));
//        List<WebElement> elementsList = getElements(By.xpath("//span[@class='sw-select-selection-list__item']"));
//        for (WebElement element: elementsList) {
//            if (!element.getText().trim().equals("Novalnet Payment")){
//                clickElement(By.xpath("//span/div[@class='sw-highlight-text' and text() = 'Novalnet Payment']"));
//                clickElement(By.xpath("//span[@class='sw-button__content']"));
//            }

        //    DriverManager.getDriver().get("http://192.168.2.140/keerthana/SW6/6532/public/admin#/sw/extension/config/NovalnetSubscription");
        // selectAllSaleChannel();

        // DriverManager.getDriver().quit();
    }
//    public static void main(String[] args) {
//        DriverManager.getDriver().get("http://192.168.2.140/keerthana/SW6/6532/public/admin#/sw/extension/config/NovalnetSubscription");
//        SubscriptionConfigPage ref = new SubscriptionConfigPage();
//                Map<Config,Object> configMap = new HashMap<>();
//        configMap.put(ALLOW_EU_CANCEL, true);
//        configMap.put(ALLOW_EU_SUSPEND, true);
//        ref.setSubscriptionConfiguration(configMap);
//
//    }
}


