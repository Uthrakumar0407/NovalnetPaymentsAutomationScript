package com.nn.pages.iframe;

import com.aventstack.extentreports.Status;
import com.nn.drivers.DriverManager;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;

import static com.nn.constants.Constants.*;
import static com.nn.reports.ExtentTestManager.*;
import static com.nn.utilities.DriverActions.*;

public class BasePage {


    private By creditCardNumber = By.cssSelector("#card_number");
    private By creditCardiFrame = By.cssSelector("#novalnet_cc_iframe");

    private By creditCardExp = By.xpath("//input[@id='expiry_date']");
    private By creditCardCVV = By.xpath("//input[@id='cvc']");

    private By creditCardHolder = By.cssSelector("#card_holder");

    private By submitBtn = By.cssSelector("#submit_button_2");

    private By panHash = By.cssSelector("#novalnet_cc_pan_hash");

    private By uniqueId = By.cssSelector("#novalnet_cc_unique_id");

    private By doRedirect =By.cssSelector("#novalnet_cc_do_redirect");

    private By cardNamePlaceHolder = By.xpath("//input[@placeholder='Name on card']");

    private By cardNumberPlaceHolder = By.xpath("//input[@placeholder='XXXX XXXX XXXX XXXX']");

    private By cardExpiryDatePlaceHolder = By.xpath("//input[@placeholder='MM / YY']");

    private By cvvPlaceHolder = By.xpath("//input[@placeholder='XXX']");

    public void verifyPanHashUniqueIdisEmpty(String input){
        boolean panHashResult = getElementAttributeText(panHash,"value").trim().isEmpty();
        boolean uniqueIdResult = getElementAttributeText(uniqueId,"value").trim().isEmpty();
        if(input.equals("4000 0000 0000 1091")||input.equals("5200 0000 0000 1096")||input.equals("3741 111111 11111")||input.equals("3741 111111 11111 9876543210")){
            verifyEquals(panHashResult,false);
            verifyEquals(uniqueIdResult,false);
        }else{
            verifyEquals(panHashResult,true);
            verifyEquals(uniqueIdResult,true);
        }

    }

    @Step("Fill CreditCard form with card number {0}, expiry date {1}, cvc/cvv {2}")
    public void fillCreditCardForm(String name,String cardNumber, String expDate2, String cvv) {

        setText(creditCardHolder,name);
        waitForElementVisible(creditCardNumber,60);
        setText(creditCardNumber, cardNumber);
        setText(creditCardExp, expDate2);
        setText(creditCardCVV, cvv);
        ExtentTestManager.addScreenShot("<b>CreditCard iFrame After credentials filled</b>");
        AllureManager.saveScreenshot("Screenshot: CreditCard iFrame After credentials filled");
    }

    @Step("Verify CC iFrame Displayed")
    public void verifyCCiFrameDisplayed(boolean expected) {
        boolean actual = waitForElementVisible(creditCardiFrame,2,"iFrame Display check");
        if(actual)
            highlightElement(creditCardiFrame);
        verifyAssertEquals(actual, expected,"<b> Creditcard iFrame Display status: </b>");
    }

    @Step
    public void loadIframe(){
        DriverActions.openURL("http://localhost/mohamedn_m/v2_iframe/v2iframe.html");
        waitForTitleContains("v2 iframe");
    }

    public void clickSubmit(){
        waitForElementVisible(submitBtn);
        clickElement(submitBtn);
    }

    public void verifyNumberFieldDisplayed(){
        switchToFrame(creditCardiFrame);
        waitForElementVisible(creditCardNumber);
        checkElementDisplayed(creditCardNumber);
        ExtentTestManager.addScreenShot(Status.PASS,"credit card number field");
        byte[] screenshotBytes1 = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment("Payment screenshot", new ByteArrayInputStream(screenshotBytes1));
    }

    public void verifyNameFieldDisplayed(){
        switchToFrame(creditCardiFrame);
        waitForElementVisible(creditCardHolder);
        checkElementDisplayed(creditCardHolder);
        ExtentTestManager.addScreenShot(Status.PASS,"credit card name field");
        byte[] screenshotBytes1 = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment("Payment screenshot", new ByteArrayInputStream(screenshotBytes1));
    }
    public void verifyError(String fieldName){
        switchToFrame(creditCardiFrame);
        boolean result;
        if(fieldName.equals(CARDNUMBER)){
             result = getElementAttributeText(creditCardNumber,"class").contains("error");
        }else if(fieldName.equals(CARDHOLDERNAME)){
            result = getElementAttributeText(creditCardHolder,"class").contains("error");
        }else if(fieldName.equals(CVV)){
            result = getElementAttributeText(creditCardCVV,"class").contains("error");
        }else if(fieldName.equals(EXP)){
            result = getElementAttributeText(creditCardExp,"class").contains("error");
        }
        switchToDefaultContent();
    }

    public void verifyNumberFiledAllowedDatas(String input){

         if(input.equals("42345678901234567890")){
            verifyAssertEquals(getElementAttributeText(creditCardNumber,"value").replaceAll("\\s","").length(),19);
        }
        else if(input.equals("3741 111111 11111")||input.equals("5200 0000 0000 1096")||input.equals("4000 0000 0000 1091")){
            verifyAssertEquals(getElementAttributeText(creditCardNumber,"value"),input);
        }
        else if(input.equals("12 34")){
            verifyAssertEquals(getElementAttributeText(creditCardNumber,"value"),"1234");
        }else if(input.equals("3741 111111 11111 9876543210")){
             verifyAssertEquals(getElementAttributeText(creditCardNumber,"value").replaceAll("\\s","").length(),15);
         }else if(input.equals(" 1234567890")){
             verifyAssertEquals(getElementAttributeText(creditCardNumber,"value").replaceAll("\\s","").length(),10);
         }
        else {
            verifyAssertEquals(getElementAttributeText(creditCardNumber,"value"),"");
        }
    }

    public void verifyNameFieldAllowedDatas(String input){
        if (input.contains(".&")){

        }
    }

    public void checkLogo(String input){
        if(input.equals("4200 0000 0000 0000")){
           verifyAssertEquals(getElementAttributeText(By.cssSelector("#card_number_fld"),"class"),"visa");
        }else if(input.equals("5200 0000 0000 0007")){
            verifyAssertEquals(getElementAttributeText(By.cssSelector("#card_number_fld"),"class"),"mastercard");
        }else if(input.equals("3741 111111 11111")){
            verifyAssertEquals(getElementAttributeText(By.cssSelector("#card_number_fld"),"class"),"amex");
        }
        switchToDefaultContent();

    }

    public void checkPlaceHolderDisplayed(String fieldName){

        boolean result = false;
        if(fieldName.equals(CARDHOLDERNAME)){
            waitForElementVisible(cardNamePlaceHolder);
            result = checkElementDisplayed(cardNamePlaceHolder);
        }else if(fieldName.equals(CARDNUMBER)){
            waitForElementVisible(cardNumberPlaceHolder);
            result = checkElementDisplayed(cardNumberPlaceHolder);
        }else if(fieldName.equals(EXP)){
            waitForElementVisible(cardExpiryDatePlaceHolder);
            result = checkElementDisplayed(cardExpiryDatePlaceHolder);
        }else if(fieldName.equals(CVV)){
            waitForElementVisible(cvvPlaceHolder);
            result = checkElementDisplayed(cvvPlaceHolder);
        }

           verifyAssertEquals(result,true);
           ExtentTestManager.addScreenShot(Status.PASS,"verify cardholder place holder displayed");
           AllureManager.saveScreenshot("verify cardholder place holder displayed");

    }

    public void checkDefaultValue(String fieldName){
        if(fieldName.equals(CARDNUMBER)){
            verifyAssertEquals(getElementText(creditCardNumber),"");
        }else if(fieldName.equals(CARDHOLDERNAME)){
            verifyAssertEquals(getElementText(creditCardHolder),"");
        }else if(fieldName.equals(EXP)){
            verifyAssertEquals(getElementText(creditCardExp),"");
        }else if(fieldName.equals(creditCardCVV)){
            verifyAssertEquals(getElementText(creditCardCVV),"");
        }
    }

}
