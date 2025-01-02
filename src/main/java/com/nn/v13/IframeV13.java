package com.nn.v13;

import com.aventstack.extentreports.Status;
import com.nn.drivers.DriverManager;
import com.nn.exceptions.ShopwareExceptions;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nn.Magento.Constants.DOB_LESS_18;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.*;

public class IframeV13 {

    private By nnPaymentFrame = By.xpath("//iframe[@id='novalnetPaymentIframe']");

    private By ccFrame = By.cssSelector("#cc_frame");

    private By ccNumber = By.cssSelector("div#card_number_fld input#card_number");

    private By ccExpiry = By.cssSelector("div#expiry_date_fld input#expiry_date");
    private By ccCVV = By.cssSelector("div#cvc_fld  input#cvc");

    private By maskedCC = By.cssSelector("#payment_ref_cc_form_check + label>span");

    private By maskedSEPA = By.cssSelector("#payment_ref_sepa_form_check + label>span");

    private By enterNewCardCC = By.cssSelector("#normal_cc_form_check+label");
    private By enterNewCardSEPA = By.cssSelector("#normal_sepa_form_check+label");
    private By googlePayBtn = By.cssSelector("#googlepay_container button");

    @Step("Verify masked CC card at checkout")
    public IframeV13 verifyMaskedCCData(String ccNumber, String expDate) {
        String masked = getElementText(maskedCC); // 374111XXXXXX111 (12 / 2025) // 400000XXXXXX1091 (12 / 2030)
        StringBuilder sb = new StringBuilder(ccNumber.replaceAll("\\s+", ""));
        sb.replace(6, 12, "XXXXXX");
        List<String> list = Stream.of(masked.split("\\s"))
                .map(s -> s.replaceAll("[^\\w\\s]", ""))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        String expMonth = expDate.substring(0, 2);
        String expYear = expDate.substring(2);
        verifyEquals(list.get(0), sb.toString(), "Masked CC card number");
        verifyEquals(list.get(1), expMonth, "Masked CC card expiry month");
        verifyEquals(list.get(2).substring(2), expYear, "Masked CC card expiry year");
        return this;
    }

    // use this if above logic failed
    private String getMaskedCreditCardNumber(String cardNumber) {
        Pattern pattern = Pattern.compile("(^\\d{6})\\d{6}(\\d+)");
        Matcher matcher = pattern.matcher(cardNumber);
        if (matcher.find()) {
            return matcher.group(1) + "XXXXXX" + matcher.group(2);
        }
        return null;
    }

    @Step("Click CC enter new card details button")
    public IframeV13 clickNewCardCC() {
        clickElementByRefreshing(enterNewCardCC);
        return this;
    }

    @Step("Click CC enter new card details button")
    public IframeV13 ifTokenDisplayedclickNewCardCC() {
        if (checkElementDisplayed(enterNewCardCC))
            clickElementByRefreshing(enterNewCardCC);
        return this;
    }

    @Step("Click SEPA enter new card details button")
    public IframeV13 ifTokenDisplayedclickNewCardSEPA() {
        if (checkElementDisplayed(enterNewCardSEPA))
            clickElementByRefreshing(enterNewCardSEPA);
        return this;
    }

    @Step("Click SEPA enter new iban button")
    public IframeV13 clickNewCardSEPA() {
        clickElementByRefreshing(enterNewCardSEPA);
        return this;
    }

    @Step("Verify masked CC data displayed")
    public IframeV13 verifyMaskedCCDataDisplayed(boolean expected) {
        verifyEquals(checkElementDisplayed(maskedCC), expected, "Verify masked CC data displayed");
        return this;
    }

    @Step("Verify masked SEPA data displayed")
    public IframeV13 verifyMaskedSEPADataDisplayed(boolean expected) {
        verifyEquals(checkElementDisplayed(maskedSEPA), expected, "Verify masked SEPA data displayed");
        return this;
    }

    @Step("Fill CreditCard form with card number {0}, expiry date {1}, cvc/cvv {2}")
    public IframeV13 fillCreditCardForm(String cardNumber, String expDate, String cvv) {
        switchToFrame(ccFrame);
        setTextWithoutClear(ccNumber, cardNumber);
        setTextWithoutClear(ccExpiry, expDate);
        setTextWithoutClear(ccCVV, cvv);
        switchToParentFrame();
        ExtentTestManager.addScreenShot("<b>CreditCard iFrame After credentials filled</b>");
        AllureManager.saveScreenshot("Screenshot: CreditCard iFrame After credentials filled");
        return this;
    }

    @Step("Verify CC Inline form Displayed expected {0}")
    public IframeV13 verifyInlineFormDisplayed(boolean expected) {
        switchToFrame(ccFrame);
        boolean actual = checkElementDisplayed(By.cssSelector("div.in-line-form"));
        verifyEquals(actual, expected, "Verify CC Inline form displayed");
        switchToParentFrame();
        return this;
    }

    @Step("Fill IBAN {0}")
    public IframeV13 fill_IBAN_SEPA(String iban) {
        By ibanLoc = By.cssSelector("#iban");
        handleStaleElement(ibanLoc, d -> d.findElement(ibanLoc).sendKeys(iban));
        return this;
    }

    @Step("Verify masked SEPA iban at checkout")
    public IframeV13 verifyMaskedSEPAData(String iban) {
        String masked = getElementText(maskedSEPA); // DE2XXXXXXXXXXXXXXXX956  AT6XXXXXXXXXXXXXX956
        StringBuilder sb = new StringBuilder(iban.replaceAll("\\s+", ""));
        int s = 3;
        int e = sb.length() - 3;
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < e - s; i++) {
            mask.append("X");
        }
        sb.replace(s, e, mask.toString());
        verifyEquals(masked, sb.toString(), "Masked SEPA IBAN number");
        return this;
    }

    //use this if above logic is failed
    private String getMaskedIBAN(String iban) {
        Pattern pattern = Pattern.compile("^(\\D{2}\\d{1})\\d+(\\d{3})$");
        Matcher matcher = pattern.matcher(iban);
        if (matcher.find()) {
            return matcher.group(1) + "X".repeat(iban.length() - 6) + matcher.group(2);
        }
        return null;
    }

    @Step("Fill IBAN {0}")
    public IframeV13 fill_InstalmentSEPA_IBAN(String iban) {
        By ibanLoc = By.cssSelector("#isepa_iban");
        handleStaleElement(ibanLoc, d -> d.findElement(ibanLoc).sendKeys(iban));
        return this;
    }

    @Step("Fill Date of Birth {0}")
    public IframeV13 fill_DOB_SEPA(String birthdate) {
        By css = By.cssSelector(".pf-guarantee_sepa #v13_birth_date");
        handleStaleElement(css, d -> d.findElement(css).sendKeys(birthdate));
        return this;
    }

    @Step("Fill Date of Birth {0}")
    public IframeV13 fill_DOB_InstalmentSEPA(String birthdate) {
        By css = By.cssSelector(".pf-instalment_direct_debit_sepa #v13_birth_date");
        handleStaleElement(css, d -> d.findElement(css).sendKeys(birthdate));
        return this;
    }

    @Step("Fill Date of Birth {0}")
    public IframeV13 fill_DOB_Invoice(String birthdate) {
        By css = By.cssSelector(".pf-guarantee_invoice #v13_birth_date");
        handleStaleElement(css, d -> d.findElement(css).sendKeys(birthdate));
        return this;
    }

    @Step("Fill Date of Birth {0}")
    public IframeV13 fill_DOB_InstalmentInvoice(String birthdate) {
        By css = By.cssSelector(".pf-instalment_invoice #v13_birth_date");
        handleStaleElement(css, d -> d.findElement(css).sendKeys(birthdate));
        return this;
    }

    @Step("Select Instalment cycle {0}")
    public IframeV13 selectInstalmentInvoiceCycle(int numberOfCycles) {
        handleStaleElement(By.cssSelector("#instalment_invoice_cycle"),
                driver -> selectDropdownByValue(By.cssSelector("#instalment_invoice_cycle"), String.valueOf(numberOfCycles)));
        return this;
    }

    @Step("Select Instalment cycle {0}")
    public IframeV13 selectInstalmentSepaCycle(int numberOfCycles) {
        handleStaleElement(By.cssSelector("#instalment_sepa_cycle"),
                driver -> selectDropdownByValue(By.cssSelector("#instalment_sepa_cycle"), String.valueOf(numberOfCycles)));
        return this;
    }

    public IframeV13 selectInstalmentCycle(int numberOfCycles, String paymentType) {
        if (paymentType.equals(INSTALMENT_DIRECT_DEBIT_SEPA)) {
            handleStaleElement(By.cssSelector("#instalment_sepa_cycle"),
                    driver -> selectDropdownByValue(By.cssSelector("#instalment_sepa_cycle"), String.valueOf(numberOfCycles)));
        } else if (paymentType.equals(INSTALMENT_INVOICE)) {
            handleStaleElement(By.cssSelector("#instalment_invoice_cycle"),
                    driver -> selectDropdownByValue(By.cssSelector("#instalment_invoice_cycle"), String.valueOf(numberOfCycles)));
        } else {
            Assert.fail("Instalmet cycle not applicable for this payment " + paymentType);
        }
        return this;
    }

    @Step("Fill Date of Birth {0}")
    public IframeV13 fill_DOB(String birthdate, String paymentType) {
        switch (paymentType) {
            case INSTALMENT_DIRECT_DEBIT_SEPA:
                By instSepa = By.cssSelector(".pf-instalment_direct_debit_sepa #v13_birth_date");
                handleStaleElement(instSepa, d -> d.findElement(instSepa).sendKeys(birthdate));
                break;
            case INSTALMENT_INVOICE:
                By instInvoice = By.cssSelector(".pf-instalment_invoice #v13_birth_date");
                handleStaleElement(instInvoice, d -> d.findElement(instInvoice).sendKeys(birthdate));
                break;
            case GUARANTEED_INVOICE:
                By gInvoice = By.cssSelector(".pf-guarantee_invoice #v13_birth_date");
                handleStaleElement(gInvoice, d -> d.findElement(gInvoice).sendKeys(birthdate));
                break;
            case GUARANTEED_DIRECT_DEBIT_SEPA:
                By gSepa = By.cssSelector(".pf-guarantee_sepa #v13_birth_date");
                handleStaleElement(gSepa, d -> d.findElement(gSepa).sendKeys(birthdate));
                break;
            default:
                Assert.fail("DOB is not applicable for this payment type " + paymentType);
        }
        return this;
    }

    @Step("Verify displayed Instalment cycles {0} in the checkout")
    public IframeV13 verifySelectedInstalmentCyclesDisplayedAtCheckout(int[] selecteCycles) {
        Select select = new Select(getElement(By.cssSelector("#instalment_invoice_cycle")));
        int[] checkoutCycles = select.getOptions().stream()
                .map(e -> e.getAttribute("value"))
                .filter(s -> !s.isBlank())
                .map(Integer::parseInt)
                .mapToInt(Integer::intValue)
                .toArray();
        verifyEquals(Arrays.equals(checkoutCycles, selecteCycles), true,
                "Verify displayed instalment cycles are the same of assigned instalment cycles in the checkout");
        return this;
    }

    @Step("Fill Date of Birth {0}")
    public IframeV13 verifySEPA_DateOfBirthDisplayed(boolean expected) {
        By css = By.cssSelector(".pf-guarantee_sepa #v13_birth_date");
        DriverActions.verifyEquals(checkElementDisplayed(css), expected);
        return this;
    }

    @Step("Fill Date of Birth {0}")
    public IframeV13 verifyInvoice_DateOfBirthDisplayed(boolean expected) {
        By css = By.cssSelector(".pf-guarantee_invoice #v13_birth_date");
        DriverActions.verifyEquals(checkElementDisplayed(css), expected);
        return this;
    }

    @Step("Fill Date of Birth {0}")
    public IframeV13 verifyInstalmentInvoiceDateOfBirthDisplayed(boolean expected) {
        By css = By.cssSelector(".pf-instalment_invoice #v13_birth_date");
        DriverActions.verifyEquals(checkElementDisplayed(css), expected);
        return this;
    }

    @Step("Fill Date of Birth {0}")
    public IframeV13 verifyInstalmentSepaDateOfBirthDisplayed(boolean expected) {
        By css = By.cssSelector(".pf-instalment_direct_debit_sepa #v13_birth_date");
        DriverActions.verifyEquals(checkElementDisplayed(css), expected);
        return this;
    }

    public IframeV13 enterIframe() {
        switchToFrame(nnPaymentFrame);
        return this;
    }

    public void exitIframe() {
        switchToDefaultContent();
    }

    @Step("Verify Payment Method Displayed")
    public IframeV13 isPaymentDisplayed(String paymentType) {
        sleep(3);
        By paymentElement = getPaymentElement(paymentType);
        waitForElementPresent(paymentElement, 60);
        handleStaleElement(paymentElement, d -> clickElementWithJs(d.findElement(paymentElement)));
        //ExtentTestManager.logMessage("<b>" + paymentType + " payment status: </b>" + (checkElementDisplayed(paymentElement) ? "Displayed" : "Not Displayed"));
        return this;
    }

    @Step("Verify Payment Method Displayed")
    public boolean isPaymentDisplayed(String paymentType, boolean expected) {
        By paymentElement = getPaymentElement(paymentType);
        boolean actual = waitForElementVisible(paymentElement, 10, "");
        if (actual != expected)
            Assert.fail("<b>" + paymentType + " payment display status actual: [ " + actual + " ] expected [ " + expected + " ]");
        if (actual) {
            if (!checkElementChecked(paymentElement))
                handleStaleElement(paymentElement, d -> d.findElement(paymentElement).click());
            highlightElement(paymentElement);
            ExtentTestManager.addScreenShot(Status.PASS, "Payment screenshot");
            byte[] screenshotBytes2 = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Payment screenshot", new ByteArrayInputStream(screenshotBytes2));
            var status = checkElementDisplayed(paymentElement);
            switchToDefaultContent();
            ExtentTestManager.logMessage("<b>" + paymentType + " payment status: </b>" + (status ? "Displayed" : "Not Displayed"));
            AllureManager.saveLog("<b>" + paymentType + " payment status: </b>" + (status ? "Displayed" : "Not Displayed"));
            switchToDefaultContent();
            return status;
        }
        switchToDefaultContent();
        ExtentTestManager.logMessage("<b>" + paymentType + " payment status: </b>" + (actual ? "Displayed" : "Not Displayed"));
        AllureManager.saveLog("<b>" + paymentType + " payment status: </b>" + (actual ? "Displayed" : "Not Displayed"));
        return false;
    }

    public IframeV13 clickGooglePayButton() {
        waitForElementClickable(googlePayBtn);
        clickElementWithJs(googlePayBtn);
        sleep(2);
        switchToDefaultContent();
        return this;
    }

    public IframeV13 isBusinessNameDisplayed(String businessName, boolean expected) {
        String currentWindowTitle = DriverManager.getDriver().getTitle();
        switchToWindow();
        switchToFrame(By.cssSelector("iframe[src^='https://payments.google.com/payments/u/0/embedded/']"));
        boolean result = getElement(By.xpath("//div[contains(@class,'b3-updatable-cart-dialog-summary-label') and @data-was-visible='true']")).isDisplayed();
        verifyEquals(result, expected);
        String businessNameInGpaySheet = getElement(By.xpath("//div[contains(@class,'b3-updatable-cart-dialog-summary-label') and @data-was-visible='true']")).getText();
        String resultbusinessNameInGpaySheet = businessNameInGpaySheet.trim().split(" ")[1];
        verifyEquals(businessName, resultbusinessNameInGpaySheet);
        DriverActions.switchToNewWindowOrTabByTitle(currentWindowTitle);
        return this;
    }

    @Step("Get Payment Type in Checkout Page")
    public By getPaymentElement(String paymentType) {
        switch (paymentType) {
            case PAYPAL:
                return By.cssSelector("#paypal");
            case CREDITCARD:
                return By.cssSelector("#credit_card");
            case DIRECT_DEBIT_SEPA:
                return By.cssSelector("#sepa");
            case GUARANTEED_DIRECT_DEBIT_SEPA:
            case "GUARANTEED_DIRECT_DEBIT_SEPA_B2B":
                return By.cssSelector("#guarantee_sepa");
            case INVOICE:
                return By.cssSelector("#invoice");
            case GUARANTEED_INVOICE:
            case "GUARANTEED_INVOICE_B2B":
                return By.cssSelector("#guarantee_invoice");
            case INSTALMENT_INVOICE:
            case "INSTALMENT_INVOICE_B2B":
                return By.cssSelector("#instalment_invoice");
            case INSTALMENT_DIRECT_DEBIT_SEPA:
            case "INSTALMENT_DIRECT_DEBIT_SEPA_B2B":
                return By.cssSelector("#instalment_direct_debit_sepa");
            case IDEAL:
                return By.cssSelector("#ideal");
            case CASHPAYMENT:
                return By.cssSelector("#cashpayment");
            case MULTIBANCO:
                return By.cssSelector("#multibanco");
            case PREPAYMENT:
                return By.cssSelector("#prepayment");
            case ONLINE_TRANSFER:
                return By.cssSelector("#instant_bank_transfer");
            case ONLINE_BANK_TRANSFER:
                return By.cssSelector("#online_bank_transfer");
            case GIROPAY:
                return By.cssSelector("#giropay");
            case EPS:
                return By.cssSelector("#eps");
            case BANCONTACT:
                return By.cssSelector("#bancontact");
            case ALIPAY:
                return By.cssSelector("#alipay");
            case POSTFINANCE_CARD:
                return By.cssSelector("#postfinance_card");
            case WECHATPAY:
                return By.cssSelector("#wechatpay");
            case TRUSTLY:
                return By.cssSelector("#trustly");
            case PRZELEWY24:
                return By.cssSelector("#przelewy24");
            case GOOGLEPAY:
                return By.cssSelector("#googlepay");
            case BLIK:
                return By.cssSelector("#blik");
            case MBWAY:
                return By.cssSelector("#mbway");
            case PAYCONIQ:
                return By.cssSelector("#payconiq");
            case DIRECT_DEBIT_ACH:
                return By.cssSelector("#direct_debit_ach");
            default:
                throw new IllegalArgumentException("Invalid payment method: " + paymentType);
        }
    }

    public IframeV13 isLogoDisplayed(String paymentName, boolean expected) {
        String xpath = "//img[contains(@alt,'" + paymentName + "')]";
        By paymentDiv = By.xpath("//img[contains(@alt,'" + paymentName + "')]/ancestor::div[contains(@class,'payment-type')]");
        waitForElementPresent(paymentDiv, 30);
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        Log.info("Scrolling to payment element");
        js.executeScript("arguments[0].scrollIntoView(true);", getElement(paymentDiv));
        boolean logoDisplayed = waitForElementVisible(By.xpath(xpath), 20, "Waiting for logo to display");
        verifyEquals(logoDisplayed, expected);
        return this;
    }
    public IframeV13 fill_MBWay_Data(String mobileNumber){
        By mobileNoField = By.cssSelector("#mbway_mobile_no");
        setText(mobileNoField,mobileNumber);
        return this;
    }
    @Step("Fill ACH Account number")
    public IframeV13 fill_ach_account_no(String ach_account_no) {
        By ach_account_no_loc = By.cssSelector("#ach_acount_no");
        handleStaleElement(ach_account_no_loc, d -> d.findElement(ach_account_no_loc).sendKeys(ach_account_no));
        return this;
    }
    @Step("Fill ACH routing number")
    public IframeV13 fill_ach_ABA(String ach_routing_no) {
        By ach_routing_aba_no = By.cssSelector("#ach_routing_aba_no");
        handleStaleElement(ach_routing_aba_no, d -> d.findElement(ach_routing_aba_no).sendKeys(ach_routing_no));
        return this;
    }

    public IframeV13 isCardDetailsFilled(String cardNumber, String expDate, String cvv, boolean maskedCC){
        int i = 6;
        boolean flag = false;
        boolean errorDisplayed =  waitForElementVisible(By.cssSelector(".alert-content"), 6, "" );
        if (errorDisplayed){
            while (i>0) {
                try{
                    enterIframe();
                    if (!maskedCC) {
                        fillCreditCardForm(cardNumber, expDate, cvv).exitIframe();
                    }
                    //  sleep(5);
                    scrollToElement(By.cssSelector("#confirmOrderForm"));
                    clickElementByRefreshing(By.cssSelector("#confirmOrderForm"));
                    if (cardNumber.equals("4000000000001091")) {
                        waitForElementVisible(By.cssSelector("form[action^='https://test.saferpay.com/ThreeDSAuthentication/']"));
                        flag = true;
                    }else if (cardNumber.equals("4200000000000000")){
                        waitForElementVisible(By.cssSelector(".finish-header"), 30);
                        flag = true;
                    }
                    break;
                }
                catch (Exception e){
                    i--;
                    Log.info("Issue while clicking GooglePay button");
                }
            }
            if(!flag)
                Assert.fail("Timeout waiting for element to click ");
        }
        return null;
    }
    public IframeV13 isIbanDetailsFilled(String iban){
        int i = 6;
        boolean flag = false;
        boolean errorDisplayed =  waitForElementVisible(By.cssSelector(".alert-content"), 6, "" );
        if (errorDisplayed){
            while (i>0) {
                try{
                    enterIframe();
                    fill_IBAN_SEPA(iban).exitIframe();
                    scrollToElement(By.cssSelector("#confirmFormSubmit"));
                    clickElementByRefreshing(By.cssSelector("#confirmFormSubmit"));
                    if (iban.equals("DE24300209002411761956")){
                        waitForElementVisible(By.cssSelector(".finish-header"), 30);
                        flag = true;
                    }
                    break;
                }
                catch (Exception e){
                    i--;
                    Log.info("Issue while clicking Submit order button");
                }
            }
            if(!flag)
                Assert.fail("Timeout waiting for element to click Submit order button");
        }
        return null;
    }
    public IframeV13 isIbanAndDobDetailsFilled(String iban, String dob, String paymentType, boolean B2B, boolean maskedSEPA){
        int i = 6;
        boolean flag = false;
        boolean errorDisplayed =  waitForElementVisible(By.cssSelector(".alert-content"), 6, "" );
        if (errorDisplayed){
            while (i>0) {
                try{
                    enterIframe();
                    if (maskedSEPA) {
                        clickElementByRefreshing(By.cssSelector("#payment_ref_sepa_form_check + label"));
                    }else {
                        fill_IBAN_SEPA(iban);
                    }
                    if (!B2B) {
                        fill_DOB(dob, paymentType).exitIframe();
                    }else {
                        exitIframe();
                    }
                    scrollToElement(By.cssSelector("#confirmFormSubmit"));
                    clickElementByRefreshing(By.cssSelector("#confirmFormSubmit"));
                    if ((iban.equals("DE24300209002411761956") || iban.equals("AT671509000028121956")) & !dob.equals(DOB_LESS_18) || maskedSEPA){
                        waitForElementVisible(By.cssSelector(".finish-header"), 30);
                        flag = true;
                    }
                    break;
                }
                catch (Exception e){
                    i--;
                    Log.info("Issue while clicking Submit order button");
                }
            }
            if(!flag)
                Assert.fail("Timeout waiting for element to click Submit order button");
        }
        return null;
    }
    public IframeV13 isACHDetailsFilled(String accountNum, String ABA){
        int i = 6;
        boolean flag = false;
        boolean errorDisplayed =  waitForElementVisible(By.cssSelector(".alert-content"), 6, "" );
        if (errorDisplayed){
            while (i>0) {
                try{
                    enterIframe();
                    fill_ach_account_no(accountNum).fill_ach_ABA(ABA).exitIframe();
                    scrollToElement(By.cssSelector("#confirmFormSubmit"));
                    clickElementByRefreshing(By.cssSelector("#confirmFormSubmit"));
                        waitForElementVisible(By.cssSelector(".finish-header"), 30);
                        flag = true;
                    break;
                }
                catch (Exception e){
                    i--;
                    Log.info("Issue while clicking Submit order button");
                }
            }
            if(!flag)
                Assert.fail("Timeout waiting for element to click Submit order button");
        }
        return null;
    }
    public IframeV13 isDOBFilled(String DOB, String paymentType, boolean below18){
        int i = 6;
        boolean flag = false;
        boolean errorDisplayed =  waitForElementVisible(By.cssSelector(".alert-content"), 6, "" );
        if (errorDisplayed){
            while (i>0) {
                try{
                    enterIframe();
                    fill_DOB(DOB, paymentType).exitIframe();
                    scrollToElement(By.cssSelector("#confirmFormSubmit"));
                    clickElementByRefreshing(By.cssSelector("#confirmFormSubmit"));
                    if (!below18) {
                        waitForElementVisible(By.cssSelector(".finish-header"), 30);
                    }
                    flag = true;
                    break;
                }
                catch (Exception e){
                    i--;
                    Log.info("Issue while clicking Submit order button");
                }
            }
            if(!flag)
                Assert.fail("Timeout waiting for element to click Submit order button");
        }
        return null;
    }
    public IframeV13 isMBWAYDetailsFilled(String mobile){
        int i = 6;
        boolean flag = false;
        boolean errorDisplayed =  waitForElementVisible(By.cssSelector(".alert-content"), 6, "" );
        if (errorDisplayed){
            while (i>0) {
                try{
                    enterIframe();
                    fill_MBWay_Data(mobile).exitIframe();
                    scrollToElement(By.cssSelector("#confirmFormSubmit"));
                    clickElementByRefreshing(By.cssSelector("#confirmFormSubmit"));
                    waitForElementVisible(By.xpath("//button[@class='btn btn-primary']"), 30);
                    flag = true;
                    break;
                }
                catch (Exception e){
                    i--;
                    Log.info("Issue while clicking Submit order button");
                }
            }
            if(!flag)
                Assert.fail("Timeout waiting for element to click Submit order button");
        }
        return null;
    }
}
