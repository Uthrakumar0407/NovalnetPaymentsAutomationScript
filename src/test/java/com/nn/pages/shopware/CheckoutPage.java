package com.nn.pages.shopware;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.*;


import com.nn.drivers.DriverManager;
import com.nn.pages.Magento.SuccessPage;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;
import com.nn.utilities.ShopwareUtils;
import com.nn.v13.IframeV13;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.util.HashMap;
import java.util.Map;

public class CheckoutPage {

    private By submitOrderBtn = By.cssSelector("#confirmFormSubmit");
    private By orderConfirmation = By.cssSelector(".finish-header");
    private By RestrictedCardErrorAlert = By.cssSelector(".alert-content-container");
    private By sofortCookieDenyAlert = By.xpath("//div[@id='Modal']//button[@class='cookie-modal-deny-all button-tertiary']");
    private By sofortDemoBank = By.xpath("//p[@class='description' and @data-label='demo-bank']");
    private By sofortBankLoginButton = By.xpath("//button[@class='button-right primary has-indicator' and @data-skip-leave-alert='true']");
    private By loginBtn = By.xpath("//button[@class='btn btn-primary' and text()='Login']");
    private By makePaymentBtn = By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']");
    private By backToShopBtn = By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']");
    private By InvalidMobileNumber = By.cssSelector(".alert-content");

    @Step("Load checkout page")
    public IframeV13 load() {
        openURL(ShopwareUtils.SHOP_FRONT_END_URL + "checkout/confirm");
        waitForElementVisible(By.cssSelector(".checkout-container"));
        if (checkElementDisplayed(By.cssSelector(".cookie-permission-container .cookie-permission-button")))
            clickElementWithJs(By.cssSelector(".cookie-permission-container .cookie-permission-button"));
        confirmTermsConditions();
        return new IframeV13();
    }

    @Step("Confirm terms and conditions")
    private void confirmTermsConditions() {
        if (!checkElementChecked(By.cssSelector("#tos"))) {
            clickElementWithAction(By.cssSelector("#tos"));
        }
        if (checkElementDisplayed(By.cssSelector("#revocation")) && !checkElementChecked(By.cssSelector("#revocation"))) {
            clickElement(By.cssSelector("#revocation"));
        }
        //When user enter into checkout first time click the payment iFrame loaded again. temporary fix
        switchToFrame(By.cssSelector("#novalnetPaymentIframe"));
        clickElementWithJs(getElements(By.cssSelector(".payment-type input[name='payment_key']+strong")).get(0));
        sleep(3);
        switchToDefaultContent();
        reloadPage();
    }

    @Step("Click submit order button")
    public CheckoutPage clickSubmitOrderBtn() {
        scrollToElement(submitOrderBtn);
        clickSomewhereShopware();
        clickElementByRefreshingSEPA(submitOrderBtn);
        waitForSuccessPage();
        return this;
    }

    public String getCheckoutV13FormValidation() {
        return getElementText(By.cssSelector("div.alert-danger div.alert-content"));
    }

    public void submitCCAuthenticationPage() {
        scrollToElement(submitOrderBtn);
        clickElementByRefreshing(submitOrderBtn);
        waitForElementVisible(By.cssSelector("form[action^='https://test.saferpay.com/ThreeDSAuthentication/']"));
        ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
        //if(waitForElementVisible(By.cssSelector("#submit"),30,"waiting for cc redirection submit btn"))
        clickElement(By.cssSelector("#submit"));
    }

    public void cancelCCAuthenticationPage() {
        waitForElementVisible(By.cssSelector("form[action^='https://test.saferpay.com/ThreeDSAuthentication/']"));
        ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
        clickElement(By.cssSelector("#cancel"));
        sleep(3);
        ExtentTestManager.addScreenShot("<b> 3D secure authentication failed or was cancelled page </b> ");
    }

    public void waitForCCAuthenticationPage() {
        waitForElementVisible(By.cssSelector("form[action^='https://test.saferpay.com/ThreeDSAuthentication/']"));
        ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
    }

    @Step("Get total order amount")
    public String getGrandTotalAmount() {
        return getElementText(By.cssSelector("dd.checkout-aside-summary-total")).replaceAll("[^0-9]", "");
    }

    @Step("Get success page transactions details")
    public Map<String, String> getSuccessPageTransactionDetails() {
        waitForSuccessPage();
        Map<String, String> map = new HashMap<>();
        map.put("OrderNumber", getOrderNumber());
        map.put("PaymentName", getPaymentName());
        map.put("Comments", getComments());
        map.put("TID", getTID());
        return map;
    }

    public void waitForSuccessPage() {
        waitForElementVisible(orderConfirmation, 60);
    }

    private String getOrderNumber() {
        return getElementAttributeText(By.cssSelector(".finish-ordernumber"), "data-order-number");
    }

    private String getPaymentName() {
        return getElementText(By.cssSelector(".finish-order-details p:first-of-type")).split(":")[1].strip();
    }

    private String getComments() {
        return getElements(By.cssSelector(".finish-order-details p:not(.panel--body)"))
                .get(1)
                .getText()
                .replace("Comments:", "")
                .replace("Kommentare:", "")
                .trim();
    }

    private String getTID() {
        return getElements(By.cssSelector(".finish-order-details p:not(.panel--body)"))
                .get(1)
                .getAttribute("innerHTML")
                .split("<br>")[0]
                .replaceAll("[^0-9]", "");
    }

    public String getCheckoutError() {
        return getElementText(RestrictedCardErrorAlert);
    }

    @Step("Place order with iDeal")
    public void placeOrderWithIDeal() {
        waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Next']"));
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Login']"));
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']"));
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']"));
    }

    @Step("Place order with iDeal")
    public void cancelOrderWithIDeal() {
        waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
        clickElement(By.xpath("//button[.='Abort']"));
    }

    @Step("Place order with iDeal")
    public void waitForIDealRedirectionPage() {
        waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
    }

    @Step("Place order with Online Transfer - Sofort")
    public void placeOrderWithOnlineTransfer() {
        rejectSofortBankCookiePopup();
        enterSofortBankDetails();
        enterSofortTANDetails();
    }

    @Step("Close Sofort Bank Cookie popup")
    public void rejectSofortBankCookiePopup() {
        waitForURLToBe("https://www.sofort.com/", 120);
        if (waitForElementVisible(sofortCookieDenyAlert,3,"")) {
            sleep(2);
            clickElementWithJs(sofortCookieDenyAlert);
            //waitForElementVisible(sofortCookieDenyAlert);
            waitForElementVisible(sofortDemoBank, 45);
            waitForElementClickable(sofortDemoBank);
            clickElementWithJs(sofortDemoBank);
        } else {
            waitForElementVisible(sofortDemoBank, 45);
            if (checkElementDisplayed(sofortCookieDenyAlert)) {
                clickElementWithJs(sofortCookieDenyAlert);
            }
            waitForElementClickable(sofortDemoBank);
            clickElementWithJs(sofortDemoBank);
        }

    }

    @Step("Enter Sofort Bank Details")
    public void enterSofortBankDetails() {
        waitForElementVisible(sofortBankLoginButton,30);
        setTextByRefreshing(By.xpath("//input[@name='data[BackendForm][LOGINNAME__USER_ID]' and @id='BackendFormLOGINNAMEUSERID']"),"1111");
        setTextByRefreshing(By.xpath("//input[@id='BackendFormUSERPIN' and @type='password']"),"1234");
        clickElementByRefreshing(By.xpath("//button[@class='button-right primary has-indicator' and @data-skip-leave-alert='true']"));
    }

    @Step("Enter Sofort TAN Details")
    public void enterSofortTANDetails() {
        waitForElementVisible(sofortBankLoginButton,30);
        clickElementByRefreshing(By.xpath("//button[@class='button-right primary has-indicator' and @data-skip-leave-alert='true']"));
        setTextByRefreshing(By.xpath("//input[@name='data[BackendForm][tan]' and @class='encrypted' and @id='BackendFormTan']"),"12345");
        clickElementByRefreshing(By.xpath("//button[contains(@class, 'button-right') and contains(@class, 'primary') and contains(@class, 'has-indicator') and @data-skip-leave-alert='true']"));
    }

    @Step("Get TID of Sofort payment ")
    public String getSofortTID() {
        By sofortCookieDenyAlert = By.xpath("//div[@id='Modal']//button[@class='cookie-modal-deny-all button-tertiary']");
        if (checkElementDisplayed(sofortCookieDenyAlert)) {
            clickElementWithJs(sofortCookieDenyAlert);
        }
        By tIDLink = By.xpath("//p[contains(@class, 'amount') and contains(@class, 'js-toggle-details') and @data-currency='€']");
        clickElementWithJs(tIDLink);
        return getElementText(By.xpath("//p[@class='reason'][1]")).trim();
    }

    @Step("Communication break at Sofort redirect page and Get Pending order number")
    public String communicationBreakGetSofortTIDPendingOrderNumber() {
        waitForSofortRedirectionPage();
        rejectSofortBankCookiePopup();
        return getSofortTID();
    }

    @Step("Verify is Sofort payment redirected")
    public void waitForSofortRedirectionPage() {
        waitForURLToBe("https://www.sofort.com/", 120);
    }

    @Step("Communication break at Sofort redirect page and Get Pending order number")
    public String cancelAtSofortRedirection() {
        waitForSofortRedirectionPage();
        rejectSofortBankCookiePopup();
        String TID = getSofortTID();
        clickElementByRefreshing(By.cssSelector(".back-to-merchant.cancel-transaction"));
        clickElementByRefreshing(By.cssSelector("#CancelTransaction"));
        return TID;
    }

    @Step("Place order with Alipay")
    public void placeOrderWithAlipay() {
        waitForElementVisible(loginBtn, 30);
        clickElement(loginBtn);
        clickElement(makePaymentBtn);
        clickElement(backToShopBtn);
    }

    @Step("Place order with Alipay")
    public void waitForAlipayRedirectionPage() {
        waitForElementVisible(loginBtn, 30);
    }

    @Step("Place order with iDeal")
    public void cancelAtAlipayRedirectionPage() {
        clickElement(By.xpath("//button[.='Abort']"));
    }


    @Step("Place order with BanContact")
    public void placeOrderWithBanContact() {
        waitForElementVisible(makePaymentBtn, 30);
        clickElement(makePaymentBtn);
        clickElement(backToShopBtn);
    }

    @Step("Place order with BanContact")
    public void waitForBanContactRedirection() {
        waitForElementVisible(makePaymentBtn, 30);
    }

    @Step("Place order with iDeal")
    public void cancelAtBanContaceRedirectionPage() {
        clickElement(By.xpath("//button[.='Abort']"));
    }

    @Step("Place order with eps ")
    public void placeOrderWithEPS() {
        enterNewEPSBankDetails();
    }

    @Step("Enter eps Bank Transfer Details ")
    public void enterNewEPSBankDetails() {
        waitForURLToBe("https://sandbox.paydirekt.de/eps-checkout", 120);
        By epsCookie = By.xpath("//button[contains(text(), 'Alle Cookies akzeptieren')]");
        if (waitForElementVisible(epsCookie, 5, "")) {
            clickElement(epsCookie);
            setText(By.cssSelector("#bank_search"), "HYPTAT22XXX");
            clickElement(By.xpath("//div[contains(text(),'Hypo Tirol AG')]"));
        }
        clickElement(By.xpath("//button[contains(text(), 'Weiter zum Bezahlen')]"));
        clickElement(By.cssSelector("#confirmButton"));
    }

    @Step("Communication break at EPS redirect page and Get Pending order number")
    public String communicationBreakGetEPSTIDPendingOrderNumber() {
        Map<String, String> tidOrderDetails = new HashMap<>();
        waitForEPSRedirectionPage();
        String TID = getEPSTID();
        return TID;

    }

    @Step("Cancel at EPS redirect page")
    public void cancelAtEPSRedirection() {
        waitForEPSRedirectionPage();
        By backToBank = By.xpath("//button[text()='Zurück zur Bankensuche']");
        By abortPayment = By.xpath("//button[text()='Zahlung abbrechen']");
        By epsCookie = By.xpath("//button[contains(text(), 'Alle Cookies akzeptieren')]");
        if (waitForElementVisible(epsCookie, 5, "")) {
            clickElement(epsCookie);
            clickElement(abortPayment);
        } else {
            clickElement(backToBank);
            waitForElementVisible(abortPayment);
            clickElement(abortPayment);
        }
    }

    @Step("Verify is EPS payment redirected")
    public void waitForEPSRedirectionPage() {
        waitForURLToBe("https://sandbox.paydirekt.de/eps-checkout", 120);
    }

    @Step("Get TID of EPS payment ")
    public String getEPSTID() {
        String continueBtn = "//input[@name='continueBtn' and @type='button' and @value='Weiter zum Bezahlen' and contains(@class,'blueButton')]";
        String tid = "";
        if (checkElementDisplayed(By.xpath(continueBtn))) {
            String bankName = "//input[@id='tags' and @type='text' and contains(@class,'ui-autocomplete-input')]";
            String acceptBic = "//div[@id='layerBic']//button[@id='yes' and @type='button' and contains(@class,'btn') and contains(@class,'btn-primary') and @data-value='1' and contains(text(),'Annehmen')]";
            String login = "//input[@id='sbtnLogin']";
            String signIn1 = "//input[@id='sbtnSign']";
            String signIn2 = "//input[@id='sbtnSignSingle']";
            String okBtn = "//input[@id='sbtnOk']";
            String submitBtn = "//input[@name='back2Shop']";
            reloadPage();
            reloadPage();
            sleep(1);
            setText(By.xpath(bankName), "HYPTAT22XXX");
            sleep(1);
            waitForElementVisible(By.cssSelector(".ui-menu-item"));
            setTextWithAction(By.xpath(bankName), Keys.ARROW_DOWN);
            pressEnter(By.xpath(bankName));
            sleep(1);
            waitForElementVisible(By.xpath(continueBtn));
            clickElementWithJs(By.xpath(continueBtn));
            sleep(2.5);
            if (!(checkElementDisplayed(By.xpath(login)))) {
                sleep(1);
                waitForElementClickable(By.xpath(acceptBic));
                clickElementWithJs(By.xpath(acceptBic));
            }
            sleep(2.5);
            waitForElementClickable(By.xpath(login));
            clickElementWithJs(By.xpath(login));
            waitForElementClickable(By.xpath(signIn1));

            String TIDValue = getElementText(By.xpath("//table[@summary='Auftragsdetails']/tbody/tr[3]/td[2]"));
            tid = TIDValue.split("\\s+")[0];

        }
        return tid;

    }

    @Step("Place order with Giropay ")
    public void placeOrderWithGiropay() {
        enterGiropayBankDetails();
    }

    @Step("Enter GiroPay Bank Transfer Details ")
    public void enterGiropayBankDetails() {
        waitForURLToBe("https://sandbox.paydirekt.de/checkout/", 60);
        By giropayCookies = (By.xpath("//button[contains(text(), 'Alle Cookies akzeptieren')]"));
        if (waitForElementVisible(giropayCookies, 5, "")) {
            clickElement(giropayCookies);
            clickElement(By.xpath("//div[contains(text(), 'Testbank')]"));
            clickElement(By.xpath("//div[contains(text(), 'Test- und Spielbank AG')]"));
        }
        clickElement(By.xpath("//button[@name='claimCheckoutButton']"));
        clickElement(By.cssSelector("#submitButton"));
        waitForNumberOfWindowsToBe(2);
        DriverManager.getDriver().close();
        String updatedWindow = DriverManager.getDriver().getWindowHandles().toArray()[0].toString();
        DriverManager.getDriver().switchTo().window(updatedWindow);
    }

    @Step("Communication break at GiroPay redirect page and Get Pending order number")
    public String communicationBreakGetGiroPayTIDPendingOrderNumber() {
        Map<String, String> tidOrderDetails = new HashMap<>();
        waitForGiroPayRedirectionPage();
        String TID = getGiroPayTID();
        return TID;

    }

    @Step("Cancel at EPS redirect page")
    public void cancelAtGiropayRedirection() {
        waitForGiroPayRedirectionPage();
        By backToBank = By.xpath("//button[text()='Zurück zur Bankensuche']");
        By abortPaymnet = By.xpath("//button[text()='Zahlung abbrechen']");
        By epsCookie = By.xpath("//button[contains(text(), 'Alle Cookies akzeptieren')]");
        if (waitForElementVisible(epsCookie, 5, "")) {
            clickElement(epsCookie);
            sleep(1);
        } else {
            clickElement(backToBank);
            sleep(1);
        }
        clickElement(abortPaymnet);
    }

    @Step("Verify is GiroPay payment redirected")
    public void waitForGiroPayRedirectionPage() {
        waitForURLToBe("https://sandbox.paydirekt.de/checkout/", 120);
    }

    @Step("Get TID of GiroPay payment ")
    public String getGiroPayTID() {
        String logIn = "//input[@name='account/addition[@name=benutzerkennung]']";
        String continueBtn = "//input[@name='continueBtn' and @type='button' and @value='Weiter zum Bezahlen' and contains(@class,'blueButton')]";
        sleep(5);
        if (!(checkElementDisplayed(By.xpath(logIn)))) {
            if (checkElementDisplayed(By.xpath(continueBtn))) {
                String bankName = "//input[@id='tags' and @type='text' and contains(@class,'ui-autocomplete-input')]";
                String acceptBic = "//div[@id='layerBic']//button[@id='yes' and @type='button' and contains(@class,'btn') and contains(@class,'btn-primary') and @data-value='1' and contains(text(),'Annehmen')]";
                if (checkElementDisplayed(By.xpath(bankName))) {
                    setText(By.xpath(bankName), "TESTDETTXXX");
                    sleep(1.5);
                    waitForElementVisible(By.cssSelector(".ui-menu-item"));
                    setTextWithAction(By.xpath(bankName), Keys.ARROW_DOWN);
                    pressEnter(By.xpath(bankName));
                    clickElementWithJs(By.xpath(continueBtn));
                    waitForElementClickable(By.xpath(acceptBic));
                    clickElementWithJs(By.xpath(acceptBic));
                    sleep(5);
                }
                return getElementText(By.cssSelector("div.sf-decoratedControl.ym-g70:nth-of-type(1) > span.sf-text:nth-of-type(1)")).trim();
            }

        }
        return getElementText(By.cssSelector("div.sf-decoratedControl.ym-g70:nth-of-type(1) > span.sf-text:nth-of-type(1)")).trim();
    }

    @Step("Place order with Online Bank Transfer ")
    public void placeOrderWithOnlineBankTransfer() {
        enterOnlineBankTransferDetails();
    }

    @Step("Place order with Online Bank Transfer ")
    public void waitForOnlineBankTransferRedirection() {
        waitForURLToBe("https://link.tink.com/", 120);
    }

    @Step("Place order with Online Bank Transfer ")
    public void cancelAtOnlineBankTransferRedirection() {
        waitForURLToBe("https://link.tink.com/", 120);
        clickElementByRefreshing(By.cssSelector("button[data-testid='close-button']>span"));
    }

    @Step("Enter Online Bank Transfer Details ")
    public void enterOnlineBankTransferDetails() {
        waitForURLToBe("https://link.tink.com/", 120);
        String demoBank = "//button[@data-test='providerListItem']/span[contains(span[@data-test='providerName'], 'Demo providers - Payments')]";
        String demoBankRedirect = "//button[@data-test='providerListItem']/span[contains(span[@data-test='providerName'], 'Demo Open Banking Redirect (payment successful)')]";
        String continueBtn = "button.MuiButton-sizeLarge>span.MuiButton-label";
        String identifyBtn = "//input[@class='button' and @value='Identify']";
        String paynowBtn = "button[data-testid='pay-button']";
        clickElementWithJs(By.xpath(demoBank));
        clickElementWithJs(By.xpath(demoBankRedirect));
        clickElementWithJs(By.cssSelector(continueBtn));
        clickElementWithJs(By.xpath(identifyBtn));
        clickElementWithJs(By.cssSelector(paynowBtn));
        clickElementWithJs(By.xpath(identifyBtn));
    }

    @Step("Place order with Post Finance ")
    public void placeOrderWithPostFinance() {
        enterPostFinanceCardDetails();
    }

    @Step("Place order with Post Finance ")
    public void waitForPostFinanceRedirection() {
        waitForURLToBe("https://epayment-t2.postfinance.ch/", 120);
    }

    @Step("Cancel at Post Finance redirection page")
    public void cancelAtPostFinanceRedirection() {
        waitForURLToBe("https://epayment-t2.postfinance.ch/", 120);
        clickElementByRefreshing(By.cssSelector("button.fpui-button--secondary"));
    }

    @Step("Enter Post Finance Card Payment Details ")
    public void enterPostFinanceCardDetails() {
        waitForURLToBe("https://epayment-t2.postfinance.ch/", 120);
        String submitBtn = "//button[@type='submit' and contains(@class, 'fpui-button--primary') and contains(text(), 'Next')]";
        String card = "//img[@class='card-select-option--image' and @alt='Card Icon']";
        String id = "//input[@formcontrolname='cardId' and @name='cardId' and @id='cardId']";
        String cardNumber = "//input[@formcontrolname='cardNumber' and @name='cardNumber' and @id='cardNumber' and @type='text']";
        String otp = "//input[@formcontrolname='otpToken']";
        String otpText = "//p[@class='leading-snug text-xl sm:text-3xl']";

        waitForElementVisible(By.xpath(card));
        clickElementWithJs(By.xpath(card));
        clickElementWithJs(By.xpath(id));
        setText(By.xpath(id), "129 026 394 145");
        clickElementWithJs(By.xpath(submitBtn));
        sleep(2);
        if (checkElementDisplayed(By.xpath(cardNumber))) {
            clickElementWithJs(By.xpath(cardNumber));
            setText(By.xpath(cardNumber), "69968016");
        }
        if (checkElementDisplayed(By.xpath(otp))) {
            clickElementWithJs(By.xpath(otp));
            setText(By.xpath(otp), getElementText(By.xpath(otpText)));
        }

        clickElementWithJs(By.xpath(submitBtn));
    }

    @Step("Place order with BanContact")
    public void placeOrderWithPrzelewy(boolean wantPending) {
        waitForURLToBe("https://sandbox-go.przelewy24.pl/", 120);
        clickElement(By.cssSelector("div>img[alt='mBank - mTransfer']"));
        if (!wantPending)
            clickElement(By.cssSelector("#user_account_pbl_correct"));
        else
            clickElement(By.cssSelector("#user_account_pbl_pending"));
    }

    @Step("Place order with BanContact")
    public void waitForPrzelewyRedirection() {
        waitForURLToBe("https://sandbox-go.przelewy24.pl/", 120);
    }

    @Step("Place order with Trustly ")
    public void placeOrderWithTrustly() {
        switchToDefaultContent();
        enterTrustlyBankTransferDetails();
    }

    @Step("Place order with Trustly ")
    public void waitForTrustlyRedirection() {
        switchToDefaultContent();
        waitForURLToBe("https://checkout.test.trustly.com/", 120);
    }

    @Step("Place order with Trustly ")
    public void cancelAtTrustlyRedirection() {
        switchToDefaultContent();
        waitForURLToBe("https://checkout.test.trustly.com/", 120);
        clickElementByRefreshing(By.cssSelector("[data-testid='modal-navigation']+div>[data-testid='abort-button']"));
        clickElementByRefreshing(By.cssSelector("button[data-testid='abort-order-button']"));
        clickElementByRefreshing(By.cssSelector("button[data-testid='feedback-modal-skip-button']"));
    }

    @Step("Enter Online Bank Transfer Details ")
    public void enterTrustlyBankTransferDetails() {
        waitForURLToBe("https://checkout.test.trustly.com/", 120);
        String clickPayWithTrustly = "//button[@data-testid='onboarding-modal-button']";
        String demoBankRedirect = "//span[text()='Commerzbank']";
        String continueBtnStep = "//button[@data-testid='summary-step-continue-cta-button']";
        String continueBtn = "//button[@data-testid='continue-button']";
        String login = "//input[@data-testid='Input-text-loginid']";
        String passCode = "//input[@data-testid='Input-password-challenge_response']";
        String otp = "//span[@data-testid='message-default']/..//h3";
        String checkingAccountBtn = "//span[contains(@data-testid, 'headline') and text()='Checking account']";
        waitForElementVisible(By.xpath(clickPayWithTrustly));
        sleep(2);
        clickElementWithJs(By.xpath(clickPayWithTrustly));
        waitForElementVisible(By.xpath(demoBankRedirect));
        clickElementWithJs(By.xpath(demoBankRedirect));
        sleep(2);
        waitForElementVisible(By.xpath(continueBtn));
        clickElementWithJs(By.xpath(continueBtn));
        waitForElementVisible(By.xpath(login));
        clickElementWithJs(By.xpath(login));
        sleep(2);
        setTextByRefreshing(By.xpath(login), "idabarese456");
        clickElementWithJs(By.xpath(continueBtn));
        sleep(2);
        waitForElementVisible(By.xpath(otp));
        clickElementWithJs(By.xpath(passCode));
        sleep(2);
        setText(By.xpath(passCode), getElementText(By.xpath(otp)));
        clickElementWithJs(By.xpath(continueBtn));
        sleep(2);
        waitForElementVisible(By.xpath(checkingAccountBtn));
        clickElementWithJs(By.xpath(checkingAccountBtn));
        sleep(2);
        waitForElementVisible(By.xpath(continueBtnStep));
        clickElementWithJs(By.xpath(continueBtnStep));
        sleep(2);
        waitForElementVisible(By.xpath(passCode));
        clickElementWithJs(By.xpath(passCode));
        sleep(2);
        setText(By.xpath(passCode), getElementText(By.xpath(otp)));
        waitForElementVisible(By.xpath(continueBtn));
        clickElementWithJs(By.xpath(continueBtn));
        sleep(5);
    }

    @Step("Place order with WeChatPay")
    public void placeOrderWithWeChatPay() {
        waitForElementVisible(loginBtn, 30);
        clickElement(loginBtn);
        clickElement(makePaymentBtn);
        clickElement(backToShopBtn);
    }

    @Step("Place order with WeChatPay")
    public void waitForWeChatPayRedirection() {
        waitForElementVisible(loginBtn, 30);
    }

    @Step("Cancel at we chat pay")
    public void cancelAtWeChatPayRedirectionPage() {
        clickElement(By.xpath("//button[.='Abort']"));
    }

    @Step("Place order with PayPal")
    public void placeOrderWithPayPal() {
        waitForURLToBe("https://www.sandbox.paypal.com/", 120);
        sleep(3);
        String title = getPageTitle();
        if (title.contains("Log in to your PayPal account")) {
            setTextAndKey(By.cssSelector("#email"), "pb-buyer@novalnet.de", Keys.ENTER);
            setTextAndKey(By.cssSelector("#password"), "novalnet123", Keys.ENTER);
        }
        clickElement(By.cssSelector("#payment-submit-btn"));
    }

    @Step("Wait for PayPal redirect page")
    public void waitForPayPalRedirection() {
        waitForURLToBe("https://www.sandbox.paypal.com/", 120);
    }

    @Step("Place order with PayPal")
    public void cancelAtPayPalRedirection() {
        waitForURLToBe("https://www.sandbox.paypal.com/", 120);
        clickElementByRefreshing(By.xpath("//a[contains(text(),'Cancel and return')]"));
    }

    public void payWithGooglePay() {
        String currentWindow = DriverManager.getDriver().getWindowHandle();
        try {
            switchToWindow();
            switchToFrame(By.cssSelector("iframe[src^='https://payments.google.com/payments/u/0/embedded/']"));
            clickElement(By.cssSelector(".goog-inline-block.jfk-button[data-was-visible]"));
            switchToDefaultContent();
        }catch (Throwable error){
            Log.info("Issue while clicking GooglePay button");
            DriverManager.getDriver().close();
        }
        DriverManager.getDriver().switchTo().window(currentWindow);
    }

    @Step("Place order with Blik")
    public void placeOrderWithBlikPayment() {
            waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
            clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Next']"));
            clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Login']"));
            clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']"));
            clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']"));
        }
        @Step("Place order with Blik")
        public void waitForBlikRedirectionPage() {
            waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
        }
    @Step("Place order with iDeal")
    public void cancelOrderWithBlik() {
        waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
        clickElement(By.xpath("//button[.='Abort']"));
    }
    @Step("Place order with MBWay")
    public SuccessPage placeOrderWithMBWay() {
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Login']"));
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']"));
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']"));
        return new SuccessPage();}

    @Step("wait for redirection page")
    public void waitForMBWAYRedirectionPage() {
        waitForElementVisible(By.xpath("//button[@class='btn btn-primary']"), 30);
    }
    public void cancelOrderWithMBWay() {
        waitForElementVisible(By.xpath("//button[@class='btn btn-primary']"), 30);
        clickElement(By.xpath("//button[.='Abort']"));
    }
    public String getInvalidMobileNumberError() {
        return getElementText(InvalidMobileNumber);
    }
    @Step("Place order with MBWay")
    public SuccessPage placeOrderWithPayconiq() {
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Login']"));
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']"));
        clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']"));
        return new SuccessPage();}
    @Step("wait for redirection page")
    public void waitForPayconiqRedirectionPage() {
        waitForElementVisible(By.xpath("//button[@class='btn btn-primary']"), 30);
    }
}
