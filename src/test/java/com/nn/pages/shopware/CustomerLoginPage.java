package com.nn.pages.shopware;

import static com.nn.utilities.DriverActions.*;

import static com.nn.utilities.ShopwareUtils.SHOP_FRONT_END_URL;

import io.opentelemetry.api.trace.propagation.internal.W3CTraceContextEncoding;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class CustomerLoginPage {

    private By emailEle = By.cssSelector("#loginMail");
    private By password = By.cssSelector("#loginPassword");
    private By loginBtn = By.cssSelector(".login-submit > button");


    private By guestFirstName = By.cssSelector("#personalFirstName");
    private By guestLastName = By.cssSelector("#personalLastName");
    private By guestMail = By.cssSelector("#personalMail");
    private By guestStreetAddress = By.cssSelector("#billingAddressAddressStreet");
    private By guestPostalCode = By.cssSelector("#billingAddressAddressZipcode");
    private By guestCity = By.cssSelector("#billingAddressAddressCity");
    private By guestCountry = By.cssSelector("#billingAddressAddressCountry");
    private By guestSubmitBtn = By.cssSelector(".register-submit>button");


    public CustomerLoginPage load() {
        openURL(SHOP_FRONT_END_URL + "account/login");
        waitForElementVisible(By.cssSelector(".header-logo-main"));
        return this;
    }

    public void login(String email) {
        setText(emailEle, email);
        setText(password, "Novalnet@123");
        clickElement(loginBtn);
        waitForElementVisible(By.cssSelector(".account-content-main"));
    }

    public CustomerLoginPage logout() {
        openURL(SHOP_FRONT_END_URL + "account/logout");
        waitForElementVisible(By.cssSelector("form.login-form"));
        return this;
    }

    public CustomerLoginPage guestRegister(String country) {
        openURL(SHOP_FRONT_END_URL + "checkout/register");
        waitForElementVisible(By.cssSelector(".register-form"));
        setText(guestFirstName, "Norbert");
        setText(guestLastName, "Maier");
        setText(guestMail, "automation_test@gmail.com");
        setText(guestStreetAddress, "Hauptstr, 9");
        setText(guestPostalCode, "66862");
        setText(guestCity, "Kaiserslautern");
        selectDropdownByText(guestCountry, country);
        scrollToBottom();
        clickElementWithJs(guestSubmitBtn);
        return this;
    }

}
