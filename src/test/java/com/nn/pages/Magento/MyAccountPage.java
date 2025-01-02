package com.nn.pages.Magento;

import com.nn.Magento.Constants;
import com.nn.helpers.ExcelHelpers;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.nn.utilities.DriverActions.*;

public class MyAccountPage {

    @Step("Open my account page")
    public MyAccountPage load() {
        openURL(Constants.SHOP_FRONT_END_URL + "customer/account/index/");
        waitForTitleContains("My Account");
        return this;

    }

    @Step("Change billing company")
    public MyAccountPage changeCountry(String countryISO){
        By country = By.cssSelector("#country");
        if(checkElementDisplayed(By.cssSelector("#btn-cookie-allow")))
            clickElementWithJs(By.cssSelector("#btn-cookie-allow"));
        clickElementByRefreshing(By.cssSelector("div[class*='billing'] a[href*='customer/address/edit/']>span"));
        if(!getDropdownSelectedOptionValue(country).trim().equals(countryISO)){
            try {
                selectDropdownByValue(country,countryISO);
                //new Select(getElement(country)).selectByValue(countryISO);
            }catch (Throwable error){
                ExtentTestManager.logMessage("Given country ["+countryISO+"] is not present in the Magento Shop ");
                AllureManager.saveLog("Given country ["+countryISO+"] is not present in the Magento Shop ");
                Log.error("Given country ["+countryISO+"] is not present in the Magento Shop ");
                Assert.fail("Given country ["+countryISO+"] is not present in the Magento Shop "+error.getMessage());
            }
            sleep(1.5);
            if(getElement(By.cssSelector("#region_id")).getAttribute("disabled") == null){
                selectDropdownByIndex(By.cssSelector("#region_id"),1);
            }
            if(!getElement(By.cssSelector("#region")).getAttribute("style").contains("display: none;")){
                setText(By.cssSelector("#region"),"State");
            }
            clickElementByRefreshing(By.cssSelector("button[data-action='save-address']"));
            waitForElementVisible(By.cssSelector("div.message.success"));
        }
        openURL(Constants.SHOP_FRONT_END_URL+"checkout/#shipping");
        Log.info("Loading the shipping url");
        clickElementWithJs(By.cssSelector("#checkout-shipping-method-load input"));
        clickElementWithJs(By.xpath("//button[@type='submit' and contains(@class, 'continue')]"));
        return this;
    }

    @Step("Change billing company")
    public MyAccountPage changeCountry2(String countryISO){
        By country = By.cssSelector("#country");
        if(checkElementDisplayed(By.cssSelector("#btn-cookie-allow")))
            clickElementWithJs(By.cssSelector("#btn-cookie-allow"));
        clickElementByRefreshing(By.cssSelector("div[class*='billing'] a[href*='customer/address/edit/']>span"));
        if(!getDropdownSelectedOptionValue(country).trim().equals(countryISO)){
            try {
                new Select(getElement(country)).selectByValue(countryISO);
            }catch (Throwable error){
                ExtentTestManager.logMessage("Given country ["+countryISO+"] is not present in the Magento Shop ");
                AllureManager.saveLog("Given country ["+countryISO+"] is not present in the Magento Shop ");
                Log.error("Given country ["+countryISO+"] is not present in the Magento Shop ");
                Assert.fail("Given country ["+countryISO+"] is not present in the Magento Shop "+error.getMessage());
            }
            sleep(1.5);
            if(getElement(By.cssSelector("#region_id")).getAttribute("disabled") == null){
                selectDropdownByIndex(By.cssSelector("#region_id"),1);
            }
            if(!getElement(By.cssSelector("#region")).getAttribute("style").contains("display: none;")){
                setText(By.cssSelector("#region"),"State");
            }
            clickElementByRefreshing(By.cssSelector("button[data-action='save-address']"));
            waitForElementVisible(By.cssSelector("div.message.success"));
        }
        return this;
    }
    @Step("Change currency")
    public void changeCurrency(String currencyISO){
        By currency = By.cssSelector("#switcher-language-trigger span");
        if(checkElementDisplayed(currency)){
            if(!getElementText(currency).trim().contains(currencyISO)){
                clickElementByRefreshing(currency);
                waitForElementVisible(By.cssSelector("#switcher-language-trigger+ul>li>a"));
                List<WebElement> currencyList = getElements(By.cssSelector("#switcher-language-trigger+ul>li>a"));
                currencyList
                        .stream()
                        .filter(e -> e.getText().trim().startsWith(currencyISO.substring(0, 2))) // Check if it starts with the first two letters
                        .findFirst()
                        .ifPresentOrElse(WebElement::click, () -> {
                            throw new RuntimeException("No such currency element for the value " + currencyISO);
                        });
            }
        }else{
            Assert.fail("Currency not available in the shop: "+currencyISO);
        }
    }

    @Step("Change billing company")
    public void changeBillingCompany(){
        Map<String,String> address = ExcelHelpers.addressGuaranteeB2B();
        clickElement(By.linkText("Edit Address"));
        if(!getInputFieldText(By.cssSelector("#company")).equals(address.get("Company"))){
            setText(By.cssSelector("#company"),address.get("Company"));
            clickElement(By.cssSelector("button[title='Save Address']"));
            waitForElementVisible(By.cssSelector("div.message.success"));
        }
    }

    @Step("set B2B billing address")
    public void setB2BBillingAddress(){
        Map<String,String> address = ExcelHelpers.addressGuaranteeB2B();
        clickElement(By.linkText("Edit Address"));
        if(!getInputFieldText(By.cssSelector("#firstname")).equals(address.get("FirstName")))
            setText(By.cssSelector("#firstname"),address.get("FirstName"));
        if(!getInputFieldText(By.cssSelector("#lastname")).equals(address.get("LastName")))
            setText(By.cssSelector("#lastname"),address.get("LastName"));
        if(!getInputFieldText(By.cssSelector("#street_1")).equals(address.get("HouseNo")+" , "+address.get("Street")))
            setText(By.cssSelector("#street_1"),address.get("HouseNo")+" , "+address.get("Street"));
        if(!getInputFieldText(By.cssSelector("#zip")).equals(address.get("Zip")))
            setText(By.cssSelector("#zip"),address.get("Zip"));
        if(!getInputFieldText(By.cssSelector("#company")).equals(address.get("Company")))
            setText(By.cssSelector("#company"),address.get("Company"));
        if(!getInputFieldText(By.cssSelector("#city")).equals(address.get("City")))
            setText(By.cssSelector("#city"),address.get("City"));
        if(checkElementDisplayed(By.cssSelector("#region_id")))
            if(!getDropdownSelectedOptionText(By.cssSelector("#region_id")).equals(address.get("State")))
                selectDropdownByText(By.cssSelector("#region_id"),address.get("State"));
        clickElement(By.cssSelector("button[title='Save Address']"));
        waitForElementVisible(By.cssSelector("div.message.success"));
    }
    @Step("set B2B billing address")
    public void setB2BBillingAddressPending(){
        Map<String,String> address = ExcelHelpers.addressGuaranteeB2BPending();
        clickElement(By.linkText("Edit Address"));
        if(!getInputFieldText(By.cssSelector("#firstname")).equals(address.get("FirstName")))
            setText(By.cssSelector("#firstname"),address.get("FirstName"));
        if(!getInputFieldText(By.cssSelector("#lastname")).equals(address.get("LastName")))
            setText(By.cssSelector("#lastname"),address.get("LastName"));
        if(!getInputFieldText(By.cssSelector("#street_1")).equals(address.get("HouseNo")+" , "+address.get("Street")))
            setText(By.cssSelector("#street_1"),address.get("HouseNo")+" , "+address.get("Street"));
        if(!getInputFieldText(By.cssSelector("#zip")).equals(address.get("Zip")))
            setText(By.cssSelector("#zip"),address.get("Zip"));
        if(!getInputFieldText(By.cssSelector("#company")).equals(address.get("Company")))
            setText(By.cssSelector("#company"),address.get("Company"));
        if(!getInputFieldText(By.cssSelector("#city")).equals(address.get("City")))
            setText(By.cssSelector("#city"),address.get("City"));
        if(checkElementDisplayed(By.cssSelector("#region_id")))
            if(!getDropdownSelectedOptionText(By.cssSelector("#region_id")).equals(address.get("State")))
                selectDropdownByText(By.cssSelector("#region_id"),address.get("State"));
        clickElement(By.cssSelector("button[title='Save Address']"));
        waitForElementVisible(By.cssSelector("div.message.success"));
    }

    public MyAccountPage openMyDownloadProducts(){
        clickElement(By.cssSelector("#block-collapsible-nav a[href*='downloadable/customer/products']"));
        waitForElementDisable(By.cssSelector("#block-collapsible-nav a[href*='downloadable/customer/products']"));
        return this;
    }

    public boolean isDownloadOrderDisplayed(String orderID){
        String css = "#my-downloadable-products-table td a[href*='order_id/"+orderID+"']";
        return checkElementDisplayed(By.cssSelector(css));
    }

    public boolean isDownloadOptionDisplayed(String orderID){
        String xpath = "//td/a[contains(@href,'order_id/"+orderID+"')]/../following-sibling::td/a[@id='download_']";
        return checkElementDisplayed(By.xpath(xpath));
    }


}
