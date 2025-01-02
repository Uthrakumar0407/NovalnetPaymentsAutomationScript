package com.nn.testcase;

import com.nn.basetest.BaseTest;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.*;

import java.util.HashMap;

import static com.nn.basetest.BaseTest.setupBrowser;
import static com.nn.callback.CallbackProperties.CC_EXPIRED_CARD_ERROR;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.*;
import static com.nn.utilities.DriverActions.verifyEquals;

public class IframeValidation extends com.nn.pages.iframe.BaseTest {



    @Test(priority = 1, dataProvider = "creditCardNumberInputs", description = "Check whether the Credit Card Number field with the failure test data and ")
    public void numberField(String input){
        loadIframe();
        verifyCCiFrameDisplayed(true);
        verifyNumberFieldDisplayed();
        checkDefaultValue(CARDNUMBER);
        checkPlaceHolderDisplayed(CARDNUMBER);
        fillCreditCardForm("Novalnet tester",input,"0125","123");
        verifyNumberFiledAllowedDatas(input);
        checkLogo(input);
        clickSubmit();
        verifyError(CARDNUMBER);
        sleep(3);
        verifyPanHashUniqueIdisEmpty(input);
     }

    @Test(priority = 2, dataProvider = "nameHolderInputs", description = "Check whether the Credit Card Number field with the failure test data and ")
     public void nameField(String input){
         loadIframe();
        verifyNameFieldDisplayed();
         checkDefaultValue(CARDHOLDERNAME);
         checkPlaceHolderDisplayed(CARDHOLDERNAME);
         fillCreditCardForm(input,"4200 0000 0000 0000","0125","123");
        verifyNameFieldAllowedDatas(input);
         checkLogo(input);
         clickSubmit();
         verifyError(CARDHOLDERNAME);
         sleep(3);
         verifyPanHashUniqueIdisEmpty(input);
     }






   @DataProvider
    public Object[][] creditCardNumberInputs(){
        return new Object[][]{{"~!@#$%^&*()_+{}|:”>?</*-+/.,;’][=-`"},{"abcdefghijklmnopqrstuvwxyz"},{"ABCDEFGHIJKLMNOPQRSTUVWXYZ"},{"42345678901234567890"},
                {"4000 0000 0000 1091"},{"5200 0000 0000 1096"},{"3741 111111 11111"},{""},{"                 "},{"12 34"},{"3741 111111 11111 9876543210"},
                {" 1234567890"}};
    }

    @DataProvider
    public Object[][] nameHolderInputs(){
        return new Object[][]{{""},{"          "},{"~!@#$%^&*()_+{}|:”>?</*-+/.,;’][=-`"},{""}};
    }


    @DataProvider
    public Object[][] expiryDateInputs(){
         return new Object[][]{{"0124"},{"  "},{"0100"},{"012 "},{" 25"},{"0000"},{"!@#"},{"$%^"},{"&*()"},{"+_-="},{".,;:"}};
    }

    @DataProvider
    public Object[][] cvvInputs(){
         return new Object[][]{{"12"},{"12 "},{" 12"},{"00a"},{"abc"},{"!@#"},{"$%^"},{"&*()"},{"+_-="},{".,;:"}};
    }


    @DataProvider
    public Object[][] combinedInputs_Failure() {
        // Combine data from multiple data providers
        Object[][] creditCardData = creditCardNumberInputs_Failure_Combination();
        Object[][] expiryDateData = expiryDateInputs_Failure_Combination();
        Object[][] cvvData = cvvInputs_Failure_Combination();
        Object[][] nameHolderData = nameHolderInputs_Failure_Combination();

        // Determine the total number of combinations
        int totalRows = Math.max(Math.max(creditCardData.length, expiryDateData.length), Math.max(cvvData.length, nameHolderData.length));

        // Create a combined array
        Object[][] combinedData = new Object[totalRows][4];

        // Fill the combined array with data from individual providers
        for (int i = 0; i < totalRows; i++) {
            combinedData[i][0] = (i < nameHolderData.length) ? nameHolderData[i][0] : null;
            combinedData[i][1] = (i < creditCardData.length) ? creditCardData[i][0] : null;
            combinedData[i][2] = (i < expiryDateData.length) ? expiryDateData[i][0] : null;
            combinedData[i][3] = (i < cvvData.length) ? cvvData[i][0] : null;
        }

        return combinedData;
    }
    @DataProvider
    public Object[][] creditCardNumberInputs_Failure_Combination(){
        return new Object[][]{{"4200 0000 0000 0001"},{"4200 0000 0000"}, {"4200 0000 00000 "},{" "}};

    }

    @DataProvider
    public Object[][] expiryDateInputs_Failure_Combination(){
        return new Object[][]{{"0124"},{"0100"},{"012 "},{"  "}};
    }

    @DataProvider
    public Object[][] cvvInputs_Failure_Combination(){
        return new Object[][]{{"12"},{"12 "},{" 12"},{""}};
    }

    @DataProvider
    public Object[][] nameHolderInputs_Failure_Combination(){
        return new Object[][]{{"1234546789"},{"!@#$%*()_+{}|?><:[]\';/,"},{" "},{" "}};
    }
}
