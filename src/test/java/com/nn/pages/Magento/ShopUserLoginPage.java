package com.nn.pages.Magento;

import static com.nn.Magento.Constants.*;

import com.nn.Magento.Constants;
import com.nn.drivers.DriverManager;
import com.nn.reports.ExtentTestManager;
import com.nn.testcase.MagentoApiExample;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.Assert;

import java.util.List;

import static com.nn.utilities.DriverActions.*;
import static com.nn.utilities.DriverActions.waitForElementHasAttribute;

public class ShopUserLoginPage {

    private By signIn = By.cssSelector("li.link.authorization-link > a[href*=\"account/login\"]");
    private By customerLoginTitle = By.cssSelector("h1.page-title span.base[data-ui-id=\"page-title-wrapper\"]");
    private By userName = By.cssSelector("input[id='email']");
    private By password = By.xpath("//input[@id='password']");

    private By signInBtn = By.xpath("(//button[@id='send2'])[2]");

    private By currencyDropDown = By.cssSelector("#switcher-language-trigger");

    private By plnCurrency = By.cssSelector(".switcher-dropdown[aria-hidden='false']>li>a");



    @Step("Load Shop End User Sign in Home Page")
    public void load() {
        DriverManager.getDriver().get(SHOP_FRONT_END_URL);
        waitForElementVisible(customerLoginTitle,60);
    }

    @Step("Log out user")
    public void logout(){
        openURL(Constants.SHOP_FRONT_END_URL +"customer/account/logout/");
    }

    @Step("Sigin to Shop")
    public void SigninToShop(String user, String pass) {
        load();
        if(waitForElementVisible(signIn,20,"")){
            clickElementWithJs(signIn);
            waitForElementVisible(userName,60);
            login(user,pass);
            By currency = By.cssSelector("#switcher-language-trigger span");
            if(checkElementDisplayed(currency)){
                if(!getElementText(currency).trim().contains("EUR")){
                    Log.info("Default currency is not set so EUR selected");
                    clickElementByRefreshing(currency);
                    waitForElementVisible(By.cssSelector("#switcher-language-trigger+ul>li>a"));
                    List<WebElement> currencyList = getElements(By.cssSelector("#switcher-language-trigger+ul>li>a"));
                    currencyList
                            .stream()
                            .filter(e -> e.getText().trim().startsWith("EUR".substring(0, 2))) // Check if it starts with the first two letters
                            .findFirst()
                            .ifPresentOrElse(WebElement::click, () -> {
                                throw new RuntimeException("No such currency element for the value " + "EUR");
                            });
                }
            }else{
              //  Assert.fail("Currency not available in the shop: "+"EUR");
            }
        }
    }


    @Step("Login to Shop Front End Portal")
    public void login(String user, String pass) {
        waitForElementVisible(userName,60);
        setText(userName,user);
        setTextWithoutClear(password,pass);
        clickElementWithJs(signInBtn);
    }

    @Step("Select PLN Currency")
    public void selectPLNCurrency() {

        waitForElementVisible(currencyDropDown);
        clickElementWithJs(currencyDropDown);
        waitForElementVisible(plnCurrency);
        clickElementWithJs(plnCurrency);
    }

    @Step("Add product to cart via UI") // this will only work for simple products
    public void addProductToCart(String produtNameInShop){
        openURL(SHOP_FRONT_END_URL+produtNameInShop);
        clickElementByRefreshing(By.cssSelector("#product-addtocart-button"));
        waitForElementVisible(By.cssSelector(".page.messages [data-ui-id='message-success']"));
    }

    @Step("navigate to guest checkout")
    public void navigateToGuestCheckout(){
        scrollToElement(By.cssSelector(".minicart-wrapper"));
        clickElement(By.cssSelector(".minicart-wrapper"));
        waitForElementAttributeToChange(By.cssSelector(".minicart-wrapper>a"),"class","action showcart active");
        clickElementWithJs(By.cssSelector("#top-cart-btn-checkout"));
        sleep(3);
    }


    @Step("Fill customer details for guest checkout")
    public void fillCustomerDetailsForGuest(){
        if(!waitForElementVisible(By.cssSelector("#customer-email"),60,""))
            reloadPage();
        setText(By.cssSelector("#customer-email"),"dummy@gmai.com");
        setText(By.cssSelector("input[name='firstname']"),"Norbert");
        setText(By.cssSelector("input[name='lastname']"),"Maier");
        setText(By.cssSelector("input[name='street[0]']"),"9 , Hauptstr");
        selectDropdownByText(By.cssSelector("select[name='country_id']"),"Germany");
        selectDropdownByText(By.cssSelector("select[name='region_id']"),"Berlin");
        setText(By.cssSelector("input[name='city']"),"Kaiserslautern");
        setText(By.cssSelector("input[name='postcode']"),"66862");
        setText(By.cssSelector("input[name='telephone']"),"045818858555");
        clickOutsideForm();
        //waitForElementHasAttribute(By.cssSelector("tr[data-bind$='selectShippingMethod'] input"),"checked","true");
        waitForElementHasAttribute(By.xpath("//tr[contains(@data-bind,'selectShippingMethod')]/following::input[@type='radio']"),"value","flatrate_flatrate");
        clickElementWithJs(By.xpath("//tr[contains(@data-bind,'selectShippingMethod')]/following::input[@type='radio']"));
        waitForElementHasAttribute(By.cssSelector("button[data-role='opc-continue"),"type","submit");
        waitForElementClickable(By.cssSelector("button[data-role='opc-continue']"));
        //scrollToElement(By.xpath("button[data-role='opc-continue']"));
        clickElementWithJs(By.cssSelector("button[data-role='opc-continue']"));
    }

    @Step("Fill customer details for guest checkout")
    public void fillCustomerDetailsForGuest(String countryISO){
        if(!waitForElementVisible(By.cssSelector("#customer-email"),60,""))
            reloadPage();
        setText(By.cssSelector("#customer-email"),"automation_test@novalnetsolutions.com");
        setText(By.cssSelector("input[name='firstname']"),"Norbert");
        setText(By.cssSelector("input[name='lastname']"),"Maier");
        setText(By.cssSelector("input[name='street[0]']"),"9 , Hauptstr");
        selectDropdownByValue(By.cssSelector("select[name='country_id']"),countryISO);
        if(waitForElementVisible(By.cssSelector("select[name='region_id']"),5,"")){
            selectDropdownByIndex(By.cssSelector("select[name='region_id']"),1);
        }else {
            setText(By.cssSelector("input[name='region']"),"State");
        }
        setText(By.cssSelector("input[name='city']"),"Kaiserslautern");
        setText(By.cssSelector("input[name='postcode']"),"66862");
        setText(By.cssSelector("input[name='telephone']"),"045818858555");
        clickOutsideForm();
        waitForElementAttributeToChange(By.cssSelector("tr[data-bind$='selectShippingMethod'] input"),"checked","true");
        clickElement(By.cssSelector("button[data-role='opc-continue']"));
    }
}
