package com.nn.pages.Magento;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.gherkin.model.ScenarioOutline;
import com.nn.Magento.Constants;
import com.nn.drivers.DriverManager;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nn.Magento.Constants.ONE_CLICK;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.*;
import static com.nn.utilities.DriverActions.checkElementDisplayed;

public class CheckoutPage {

	public CheckoutPage() {
		PageFactory.initElements(DriverManager.getDriver(), this);
	}



	private By addToCart = By.cssSelector("span.counter-number");
	private By checkoutButton = By.xpath("//button[@id='top-cart-btn-checkout']");

	private By shippingNextBtn = By.xpath("//button[@type='submit' and contains(@class, 'continue')]");

	private By checkoutNNPage = By.cssSelector("div.items.payment-methods");

	private By nnPaymentFrame = By.cssSelector("#novalnetPaymentIFrame");

	private By ccFrame = By.cssSelector("#cc_frame");

	private By creditCard = By.cssSelector("#credit_card");

	private By trustly = By.cssSelector("#trustly");

	private By prepayment = By.cssSelector("#prepayment");


	private By ccNumber = By.cssSelector("div#card_number_fld input#card_number");

	private By ccExpiry = By.cssSelector("div#expiry_date_fld input#expiry_date");
	private By ccCVV = By.cssSelector("div#cvc_fld  input#cvc");

	private By maskedCC = By.cssSelector("#payment_ref_cc_form_check + label>span");

	private By maskedSEPA = By.cssSelector("#payment_ref_sepa_form_check + label>span");
	private By savedCCToken = By.cssSelector("#payment_ref_cc_form_check");
	private By enterNewCardCC = By.cssSelector("#normal_cc_form_check+label");
	private By enterNewCardSEPA = By.cssSelector("#normal_sepa_form_check+label");
	private By placeOrderBtn = By.cssSelector("button#novalnetPay_submit");

	private By orderSuccessMessage = By.cssSelector("span.base[data-ui-id='page-title-wrapper']");

	private String successMessage = "Thank you for your purchase!";

	private By loginBtn = By.xpath("//button[@class='btn btn-primary' and text()='Login']");

	private By makePaymentBtn = By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']");

	private By backToShopBtn = By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']");

	private By sofortBankLoginButton = By.xpath("//button[@class='button-right primary has-indicator' and @data-skip-leave-alert='true']");

	private By sofortDemoBank = By.xpath("//p[@class='description' and @data-label='demo-bank']");

	private By sofortCookieDenyAlert = By.xpath("//div[@id='Modal']//button[@class='cookie-modal-deny-all button-tertiary']");

	private By payPalnextBtn = By.xpath("//button[@id='btnNext']");

	private By errorAlert = By.xpath("//*[text()='3D secure authentication failed or was cancelled']");

	//by Nizam
	private By googlePayBtn =By.cssSelector("#googlepay_container button");

	private By payBtn =By.xpath("(//div[@role='button'])[2]");

	private By googlePayIframe = By.cssSelector("#sM432dIframe");



	public CheckoutPage load() {
		openURL(Constants.SHOP_FRONT_END_URL + "checkout/#payment");
		//waitForTitleContains("Checkout");
		waitForURLToBe("checkout");
		return this;
	}

	@Step("Wait for Cart Element")
	public void waitFotCartElement() {
		waitForElementVisible(addToCart);
	}

	private void waitForPaymentDivToLoad(By by) {
		//waitForElementHasAttribute(By.cssSelector(getStringFromBy(by).concat(getStringFromBy(paymentDivBox))),"style","");
	}

	@Step("Click Place Order Button")
	public void clickPlaceOrderBtn() {
		switchToDefaultContent();
		waitForElementClickable(placeOrderBtn);
		clickElementWithJs(placeOrderBtn);
		sleep(5); // to be discussed with Nagesh

	}

	//hack
	@Step("Click Place Order Button")
	public void clickPlaceOrderBtnForSepa() {
		switchToDefaultContent();
		waitForElementClickable(placeOrderBtn);
		clickElementWithJs(placeOrderBtn);
		sleep(10); // to be discussed with Nagesh

	}

	@Step("Click Place Order Button")
	public void clickPlaceOrderBtnforError() {
		switchToDefaultContent();
		waitForElementClickable(placeOrderBtn);
		clickElementWithJs(placeOrderBtn);
		sleep(1); // to be discussed with Nagesh

	}

	@Step("Verify validation error message at checkout")
	public void verifyCheckoutErrorMessage(String expected){
		var actual = getElementText(By.cssSelector(".message-error>div")).trim();
		verifyEquals(actual,expected,"Verify checkout validation error message");
	}

	@Step("Verify validation error message at checkout")
	public void verifyCheckoutErrorMessage(boolean expected){
		var actual = getElementText(By.cssSelector(".message-error>div")).trim();
		verifyEquals(!actual.isEmpty(),expected,"Verify checkout validation error message");
	}

	@Step("Open Checkout Page")
	public CheckoutPage openCheckoutPage() {
		sleep(5);
		if (DriverActions.getURL().contains("#shipping")){
			waitForElementVisible(By.cssSelector("#checkout-shipping-method-load input"));
			clickElementWithJs(getElements(By.cssSelector("#checkout-shipping-method-load input")).get(0));
			clickElementWithJs(shippingNextBtn);
		}
		waitForElementVisible(checkoutNNPage, 120);
		//waitForElementClickable(placeOrderBtn, 60);
		return this;
	}

	public void isShippingPageDisplayed(boolean expected){
		DriverActions.waitForElementVisible(By.cssSelector("#checkout-shipping-method-load input"),30);
		boolean actual = DriverActions.getURL().contains("#shipping");
		DriverActions.verifyEquals(actual,expected);
	}


	public void payWithGooglePay(){
		String currentWindow = DriverManager.getDriver().getWindowHandle();
		switchToWindow();
		switchToFrame(By.cssSelector("iframe[src^='https://payments.google.com/payments/u/0/embedded/']"));
		clickElement(By.cssSelector(".goog-inline-block.jfk-button[data-was-visible]"));
		switchToDefaultContent();
		DriverManager.getDriver().switchTo().window(currentWindow);
	}

	/***
	 * 	Credit card functions
	 * **/


	@Step("Verify masked CC card at checkout")
	public CheckoutPage verifyMaskedCCData(String ccNumber, String expDate){
		String masked = getElementText(maskedCC); // 374111XXXXXX111 (12 / 2025) // 400000XXXXXX1091 (12 / 2030)
		StringBuilder sb = new StringBuilder(ccNumber.replaceAll("\\s+", ""));
		sb.replace(6,12,"XXXXXX");
		List<String> list = Stream.of(masked.split("\\s"))
				.map(s->s.replaceAll("[^\\w\\s]",""))
				.filter(s->!s.isBlank())
				.collect(Collectors.toList());
		String expMonth = expDate.substring(0,2);
		String expYear = expDate.substring(2);
		verifyEquals(list.get(0),sb.toString(),"Masked CC card number");
		verifyEquals(list.get(1),expMonth,"Masked CC card expiry month");
		verifyEquals(list.get(2).substring(2),expYear,"Masked CC card expiry year");
		return this;
	}

	@Step("Click CC enter new card details button")
	public CheckoutPage clickNewCardCC(){
		clickElementByRefreshing(enterNewCardCC);
		return this;
	}

	@Step("Click CC enter new card details button")
	public CheckoutPage ifTokenDisplayedclickNewCardCC(){
		if(checkElementDisplayed(enterNewCardCC))
			clickElementByRefreshing(enterNewCardCC);
		return this;
	}

	@Step("Click SEPA enter new iban button")
	public CheckoutPage clickNewCardSEPA(){
		clickElementByRefreshing(enterNewCardSEPA);
		return this;
	}

	@Step("Click SEPA details button")
	public CheckoutPage ifTokenDisplayedclickNewCardSEPA(){
		if(checkElementDisplayed(enterNewCardSEPA))
			clickElementByRefreshing(enterNewCardSEPA);
		return this;
	}

	@Step("Verify masked CC data displayed")
	public CheckoutPage verifyMaskedCCDataDisplayed(boolean expected){
		verifyEquals(checkElementDisplayed(maskedCC),expected,"Verify masked CC data displayed");
		return this;
	}

	@Step("Verify masked SEPA data displayed")
	public CheckoutPage verifyMaskedSEPADataDisplayed(boolean expected){
		verifyEquals(checkElementDisplayed(maskedSEPA),expected,"Verify masked SEPA data displayed");
		return this;
	}

	@Step("Fill CreditCard form with card number {0}, expiry date {1}, cvc/cvv {2}")
	public CheckoutPage fillCreditCardForm(String cardNumber, String expDate, String cvv) {
		switchToFrame(By.cssSelector("#cc_frame"));
		setTextWithoutClear(ccNumber, cardNumber);
		setTextWithoutClear(ccExpiry, expDate);
		setTextWithoutClear(ccCVV, cvv);
		switchToDefaultContent();
		ExtentTestManager.addScreenShot("<b>CreditCard iFrame After credentials filled</b>");
		AllureManager.saveScreenshot("Screenshot: CreditCard iFrame After credentials filled");
		return this;
	}

	@Step("Fill CreditCard form with card number {0}, expiry date {1}, cvc/cvv {2}")
	public CheckoutPage fillCreditCardForm(String userName, String cardNumber, String expDate, String cvv) {
		switchToFrame(By.cssSelector("#cc_frame"));
		setTextWithoutClear(By.cssSelector("#card_holder"), userName);
		setTextWithoutClear(ccNumber, cardNumber);
		setTextWithoutClear(ccExpiry, expDate);
		setTextWithoutClear(ccCVV, cvv);
		switchToDefaultContent();
		ExtentTestManager.addScreenShot("<b>CreditCard iFrame After credentials filled</b>");
		AllureManager.saveScreenshot("Screenshot: CreditCard iFrame After credentials filled");
		return this;
	}

	@Step("Verify CC Inline form Displayed expected {0}")
	public CheckoutPage verifyInlineFormDisplayed(boolean expected) {
		// div.in-line-form
		switchToFrame(By.cssSelector("#cc_frame"));
		boolean actual = checkElementDisplayed(By.cssSelector("div.in-line-form"));
		verifyEquals(actual, expected, "Verify CC Inline form displayed");
		switchToDefaultContent();
		switchToFrame(nnPaymentFrame);
		return this;
	}

	public void submitCCAuthenticationPage() {
		waitForElementVisible(By.cssSelector("form[action^='https://test.saferpay.com/ThreeDSAuthentication/']"),120);
		ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
		//if(waitForElementVisible(By.cssSelector("#submit"),30,"waiting for cc redirection submit btn"))
		clickElement(By.cssSelector("#submit"));
	}

	public void cancelCCAuthenticationPage() {
		waitForElementVisible(By.cssSelector("form[action^='https://test.saferpay.com/ThreeDSAuthentication/']"),120);
		ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
		//if(waitForElementVisible(By.cssSelector("#cancel"),30,"waiting for cc redirection cancel btn"))
		clickElement(By.cssSelector("#cancel"));
	}

	@Step("Wait for CC Aunthentication page")
	public void waitForCCAuthenticationPage() {
		waitForElementVisible(By.cssSelector("form[action^='https://test.saferpay.com/ThreeDSAuthentication/']"),120);
		ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
	}

	@Step("Verify payment error message at checkout expected: {0}")
	public void verifyPaymentErrorMessage(String expected){
		String actual = getElementText(By.xpath("//*[text()='"+expected+"']"));
		verifyEquals(actual,expected);
	}

	/**
	 * Direct Debit SEPA functions
	 * **/
	@Step("Fill IBAN {0}")
	public CheckoutPage fill_IBAN_SEPA(String iban){
		By ibanLoc = By.cssSelector("#iban");
		handleStaleElement(ibanLoc, d -> d.findElement(ibanLoc).sendKeys(iban));
		return this;
	}
	@Step("Fill IBAN {0}")
	public CheckoutPage fill_ACH_Data(String accountNumber,String routingNumber){
		By aaccountNumberACH = By.cssSelector("#ach_acount_no");
		By routingNumberACH = By.cssSelector("#ach_routing_aba_no");
		setText(aaccountNumberACH, accountNumber);
		setText(routingNumberACH,routingNumber);
		return this;
	}
	public CheckoutPage fill_MBWay_Data(String mobileNumber){
		By mobileNoField = By.cssSelector("#mbway_mobile_no");
		setText(mobileNoField,mobileNumber);
		return this;
	}

//	public static void main(String[] args) {
//		String iban = "DE24300209002411761956";
//		String iban2 = "AT671509000028121956";
//		String iban3 = "CH3908704016075473007";
//		StringBuilder sb = new StringBuilder(iban2);
//		int s = 3;
//		int e = sb.length()-3;
//		StringBuilder mask = new StringBuilder();
//		for(int i=0;i<e-s;i++){
//			mask.append("X");
//		}
//		sb.replace(s,e, mask.toString());
//		System.out.println(sb);
//	}

	@Step("Verify masked SEPA iban at checkout")
	public CheckoutPage verifyMaskedSEPAData(String iban){
		String masked = getElementText(maskedSEPA); // DE2XXXXXXXXXXXXXXXX956  AT6XXXXXXXXXXXXXX956
		StringBuilder sb = new StringBuilder(iban.replaceAll("\\s+", ""));
		int s = 3;
		int e = sb.length()-3;
		StringBuilder mask = new StringBuilder();
		for(int i=0;i<e-s;i++){
			mask.append("X");
		}
		sb.replace(s,e, mask.toString());
		verifyEquals(masked,sb.toString(),"Masked SEPA IBAN number");
		return this;
	}

	@Step("Fill IBAN {0}")
	public CheckoutPage fill_InstalmentSEPA_IBAN(String iban){
		By ibanLoc = By.cssSelector("#isepa_iban");
		handleStaleElement(ibanLoc,d->d.findElement(ibanLoc).sendKeys(iban));
		return this;
	}

	@Step("Fill Date of Birth {0}")
	public CheckoutPage fill_DOB_SEPA(String birthdate){
		By css = By.cssSelector(".pf-guarantee_sepa #v13_birth_date");
		handleStaleElement(css,d->d.findElement(css).sendKeys(birthdate));
		return this;
	}

	@Step("Fill Date of Birth {0}")
	public CheckoutPage fill_DOB_InstalmentSEPA(String birthdate){
		By css = By.cssSelector(".pf-instalment_direct_debit_sepa #v13_birth_date");
		handleStaleElement(css,d->d.findElement(css).sendKeys(birthdate));
		return this;
	}

	@Step("Fill Date of Birth {0}")
	public CheckoutPage fill_DOB_Invoice(String birthdate){
		By css = By.cssSelector(".pf-guarantee_invoice #v13_birth_date");
		handleStaleElement(css,d->d.findElement(css).sendKeys(birthdate));
		return this;
	}

	@Step("Fill Date of Birth {0}")
	public CheckoutPage fill_DOB_InstalmentInvoice(String birthdate){
		By css = By.cssSelector(".pf-instalment_invoice #v13_birth_date");
		handleStaleElement(css,d->d.findElement(css).sendKeys(birthdate));
		return this;
	}

	@Step("Select Instalment cycle {0}")
	public CheckoutPage selectInstalmentInvoiceCycle(int numberOfCycles){
		handleStaleElement(By.cssSelector("#instalment_invoice_cycle"),
				driver -> selectDropdownByValue(By.cssSelector("#instalment_invoice_cycle"),String.valueOf(numberOfCycles)));
		return this;
	}

	@Step("Select Instalment cycle {0}")
	public CheckoutPage selectInstalmentSepaCycle(int numberOfCycles){
		handleStaleElement(By.cssSelector("#instalment_sepa_cycle"),
				driver -> selectDropdownByValue(By.cssSelector("#instalment_sepa_cycle"),String.valueOf(numberOfCycles)));
		return this;
	}

	@Step("Verify displayed Instalment cycles {0} in the checkout")
	public CheckoutPage verifySelectedInstalmentCyclesDisplayedAtCheckout(int[] selecteCycles){
		Select select = new Select(getElement(By.cssSelector("#instalment_invoice_cycle")));
		int[] checkoutCycles = select.getOptions().stream()
				.map(e->e.getAttribute("value"))
				.filter(s -> !s.isBlank())
				.map(Integer::parseInt)
				.mapToInt(Integer::intValue)
				.toArray();
		verifyEquals(Arrays.equals(checkoutCycles, selecteCycles),true,
				"Verify displayed instalment cycles are the same of assigned instalment cycles in the checkout");
		return this;
	}

	@Step("Fill Date of Birth {0}")
	public CheckoutPage verifySEPA_DateOfBirthDisplayed(boolean expected){
		By css = By.cssSelector(".pf-guarantee_sepa #v13_birth_date");
		DriverActions.verifyEquals(checkElementDisplayed(css),expected);
		return this;
	}

	@Step("Fill Date of Birth {0}")
	public CheckoutPage verifyInvoice_DateOfBirthDisplayed(boolean expected){
		By css = By.cssSelector(".pf-guarantee_invoice #v13_birth_date");
		DriverActions.verifyEquals(checkElementDisplayed(css),expected);
		return this;
	}

	@Step("Fill Date of Birth {0}")
	public CheckoutPage verifyInstalmentInvoiceDateOfBirthDisplayed(boolean expected){
		By css = By.cssSelector(".pf-instalment_invoice #v13_birth_date");
		DriverActions.verifyEquals(checkElementDisplayed(css),expected);
		return this;
	}

	@Step("Fill Date of Birth {0}")
	public CheckoutPage verifyInstalmentSepaDateOfBirthDisplayed(boolean expected){
		By css = By.cssSelector(".pf-instalment_direct_debit_sepa #v13_birth_date");
		DriverActions.verifyEquals(checkElementDisplayed(css),expected);
		return this;
	}

	/***
	 * 	Trustly functions
	 * **/

	@Step("Place order with Trustly ")
	public SuccessPage placeOrderWithTrustly() {
		switchToDefaultContent();
		clickPlaceOrderBtn();
		//enterTrustlyBankTransferDetails();
		enterNewTrustlyBankTransferDetails();
		waitForElementVisible(orderSuccessMessage, 30);
		return new SuccessPage();
	}

	@Step("Place order with Trustly ")
	public void waitForTrustlyRedirection() {
		switchToDefaultContent();
		clickPlaceOrderBtn();
		waitForURLToBe("https://checkout.test.trustly.com/",120);
	}

	@Step("Place order with Trustly ")
	public void cancelAtTrustlyRedirection() {
		switchToDefaultContent();
		clickPlaceOrderBtn();
		waitForURLToBe("https://checkout.test.trustly.com/",120);
		clickElementByRefreshing(By.cssSelector("[data-testid='modal-navigation']+div>[data-testid='abort-button']"));
		clickElementByRefreshing(By.cssSelector("button[data-testid='abort-order-button']"));
		clickElementByRefreshing(By.cssSelector("button[data-testid='feedback-modal-skip-button']"));
	}

	@Step("Enter Online Bank Transfer Details ")
	public void enterTrustlyBankTransferDetails() {
		waitForURLToBe("https://checkout.test.trustly.com/",120);
		String clickPayWithTrustly = "//span[@data-testid='primary-button-body' and contains(text(), 'Pay with Trustly')]";
		String demoBankRedirect = "//span[text()='Commerzbank']";
		String continueBtn1 = "//span[@class='MuiButton-label' and text()='Continue']";
		String continueBtn2 = "//span[text()='Continue']";
		String continueBtn3 = "//button[@data-testid='continue-button']";
		String paymentBtn = "//span[@data-testid='primary-button-body' and contains(text(), 'Confirm payment')]";
		String login = "//input[@data-testid='Input-text-loginid']";
		String passCode = "//input[@data-testid='Input-password-challenge_response']";
		String otp = "//h3[contains(@class, 'sc-dlnjwi')]";
		String checkingAccountBtn = "//span[contains(@data-testid, 'headline') and text()='Checking account']";
		waitForElementVisible(By.xpath(clickPayWithTrustly));
		sleep(2);
		waitForElementClickable(DriverManager.getDriver().findElement(By.xpath(clickPayWithTrustly)));
		clickElementWithJs(By.xpath(clickPayWithTrustly));
		waitForElementVisible(By.xpath(demoBankRedirect));
		clickElementWithJs(By.xpath(demoBankRedirect));
		sleep(2);
		waitForElementVisible(By.xpath(continueBtn2));
		clickElementWithJs(By.xpath(continueBtn2));
		sleep(10);
		waitForElementVisible(By.xpath(login));
		clickElement(By.xpath(login));
		sleep(2);
		setText(By.xpath(login), "idabarese456");
		clickElement(By.xpath(continueBtn3));
		sleep(2);
		waitForElementVisible(By.xpath(otp));
		clickElement(By.xpath(passCode));
		sleep(2);
		setText(By.xpath(passCode), getElementText(By.xpath(otp)));
		clickElementWithJs(By.xpath(continueBtn2));
		sleep(2);
		waitForElementVisible(By.xpath(checkingAccountBtn));
		clickElementWithJs(By.xpath(checkingAccountBtn));
		sleep(2);
		waitForElementVisible(By.xpath(continueBtn2));
		clickElementWithJs(By.xpath(continueBtn2));
		sleep(2);
		waitForElementVisible(By.xpath(passCode));
		clickElement(By.xpath(passCode));
		sleep(2);
		setText(By.xpath(passCode), getElementText(By.xpath(otp)));
		waitForElementVisible(By.xpath(paymentBtn));
		clickElementWithJs(By.xpath(paymentBtn));
		sleep(5);
	}
	@Step("Enter Online Bank Transfer Details ")
	public void enterNewTrustlyBankTransferDetails() {
		waitForURLToBe("https://checkout.test.trustly.com/",120);
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

	@Step("Place order with Online Transfer - Sofort")
	public com.nn.pages.SuccessPage placeOrderWithOnlineTransfer() {
		clickPlaceOrderBtn();
		rejectSofortBankCookiePopup();
		enterSofortBankDetails();
		enterSofortTANDetails();
		waitForElementVisible(orderSuccessMessage, 30);
		return new com.nn.pages.SuccessPage();
	}

	@Step("Close Sofort Bank Cookie popup")
	public void rejectSofortBankCookiePopup() {
		waitForURLToBe("https://www.sofort.com/",120);
		if (checkElementDisplayed(sofortCookieDenyAlert)) {
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
		waitForElementVisible(sofortBankLoginButton, 30);
		setText(By.xpath("//input[@name='data[BackendForm][LOGINNAME__USER_ID]' and @id='BackendFormLOGINNAMEUSERID']"), "1111");
		setText(By.xpath("//input[@id='BackendFormUSERPIN' and @type='password']"), "1234");
		clickElementWithJs(By.xpath("//button[@class='button-right primary has-indicator' and @data-skip-leave-alert='true']"));
	}

	@Step("Enter Sofort TAN Details")
	public void enterSofortTANDetails() {
		waitForElementVisible(sofortBankLoginButton, 30);
		clickElementWithJs(By.xpath("//button[@class='button-right primary has-indicator' and @data-skip-leave-alert='true']"));
		setText(By.xpath("//input[@name='data[BackendForm][tan]' and @class='encrypted' and @id='BackendFormTan']"),"12345");
		clickElementWithJs(By.xpath("//button[contains(@class, 'button-right') and contains(@class, 'primary') and contains(@class, 'has-indicator') and @data-skip-leave-alert='true']"));
	}


	@Step("Verify Payment Method Displayed")
	public CheckoutPage isPaymentMethodDisplayed(String paymentType) {
		sleep(2.5);
		switchToFrame(nnPaymentFrame);
		By paymentElement = getPaymentElement(paymentType);
		waitForElementVisible(paymentElement, 120);
		handleStaleElement(paymentElement, d -> clickElementWithJs(paymentElement));
		var status = checkElementDisplayed(paymentElement);
		ExtentTestManager.logMessage("<b>" + paymentType + " payment status: </b>" + (status ? "Displayed" : "Not Displayed"));
		if (!status)
			Assert.fail("<b>" + paymentType + " payment status: </b>" + (status ? "Displayed" : "Not Displayed"));
		return this;
	}

	@Step("Verify Payment Method Displayed")
	public boolean isPaymentDisplayed(String paymentType, boolean expected) {
		waitForElementVisible(By.cssSelector("#opc-sidebar"),60);
		scrollToElement(By.cssSelector("#opc-sidebar"));
		highlightElement(By.cssSelector("#opc-sidebar"));
		scrollToElement(nnPaymentFrame);
		switchToFrame(nnPaymentFrame);
		By paymentElement = getPaymentElement(paymentType);
		boolean actual = waitForElementVisible(paymentElement,10,"");
		if(actual != expected)
			Assert.fail("<b>" + paymentType + " payment display status actual: [ " +actual+ " ] expected [ "+expected+" ]");
		if(actual == true){
			if(!checkElementChecked(paymentElement))
				handleStaleElement(paymentElement,d->d.findElement(paymentElement).click());
			//scrollToElement(paymentElement);
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

	@Step("Place order with iDeal")
	public SuccessPage placeOrderWithIDeal() {
		clickPlaceOrderBtn();
		waitForURLToBe("https://r3.girogate.de/",60);
		clickElement(By.xpath("//button[text()='Next']"));
		clickElement(By.xpath("//button[text()='Login']"));
        clickElement(By.xpath("//button[text()='Make Payment']"));
		clickElement(By.xpath("//button[text()='Back to where you came from']"));
		waitForElementVisible(orderSuccessMessage, 30);
		return new SuccessPage();
	}
	@Step("Place order with Blik")
	public SuccessPage placeOrderWithBlik() {
		clickPlaceOrderBtn();
		waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Next']"));
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Login']"));
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']"));
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']"));
		waitForElementVisible(orderSuccessMessage, 30);
		return new SuccessPage();
	}
	@Step("Place order with Payconiq")
	public SuccessPage placeOrderWithPayconiq() {
		clickPlaceOrderBtn();
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Login']"));
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']"));
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']"));
		waitForElementVisible(orderSuccessMessage, 30);
		return new SuccessPage();
	}
	@Step("Place order with MBWay")
	public SuccessPage placeOrderWithMBWay() {
		clickPlaceOrderBtn();
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Login']"));
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']"));
		clickElement(By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']"));
		waitForElementVisible(orderSuccessMessage, 30);
		return new SuccessPage();
	}

	@Step("Place order with iDeal")
	public void waitForIDealRedirectionPage() {
		clickPlaceOrderBtn();
		waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
	}
	@Step("Place order with Blik")
	public void waitForBlikRedirectionPage() {
		clickPlaceOrderBtn();
		waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
	}
	@Step("Place order with Payconiq")
	public void waitForPayconiqRedirection() {
		clickPlaceOrderBtn();
		waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Login']"), 30);
	}
	@Step("Place Order with MBWay")
	public void waitForMBWayRedirection(){
		clickPlaceOrderBtn();
		waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Login']"), 30);
	}

	@Step("Place order with iDeal")
	public void cancelAtIDealRedirectionPage() {
		clickPlaceOrderBtn();
		waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
		clickElement(By.xpath("//button[.='Abort']"));
	}
	@Step("Abort the order with blik")
	public void cancelAtBlikRedirectionPage(){
		clickPlaceOrderBtn();
		waitForElementVisible(By.xpath("//button[@class='btn btn-primary' and text()='Next']"), 30);
		clickElement(By.xpath("//button[.='Abort']"));
	}
	@Step("Abort the order with Payconiq")
	public void cancelAtPayconiqRedirectionPage(){
		clickPlaceOrderBtn();
		waitForElementVisible(By.xpath("//button[.='Abort']"), 30);
		clickElement(By.xpath("//button[.='Abort']"));
	}
	@Step("Abort the order with Payconiq")
	public void cancelAtMBwayRedirectionPage(){
		clickPlaceOrderBtn();
		waitForElementVisible(By.xpath("//button[.='Abort']"), 30);
		clickElement(By.xpath("//button[.='Abort']"));
	}


	@Step("Place order with Multibanco")
	public com.nn.pages.SuccessPage placeOrderWithMultibanco() {
		clickPlaceOrderBtn();
		waitForElementVisible(orderSuccessMessage, 30);
		return new com.nn.pages.SuccessPage();
	}

	@Step("Place order with Giropay ")
	public com.nn.pages.SuccessPage placeOrderWithGiropay() {
		clickPlaceOrderBtn();
		//enterGiropayBankDetails();//commented reason is giropay UI totally changed
		waitForURLToBe("https://link.tink.com/",60);
		clickElement(By.xpath("//span[text()='Continue']"));
		setText(By.cssSelector("#username"),"u83188312");
		setText(By.cssSelector("#password"),"zhx571");
		clickElement(By.xpath("//span[text()='Continue']"));
		clickElement(By.xpath("//span[text()='2FA App Code']"));
		setText(By.xpath("(//input)[1]"),"1234");
		clickElement(By.xpath("//span[text()='Continue']"));
		clickElement(By.xpath("(//input)[1]"));
		clickElement(By.xpath("//span[text()='Continue']"));
		setText(By.xpath("(//input)[1]"),"1234");
        clickElement(By.xpath("//button[@type='submit']"));
		waitForElementVisible(orderSuccessMessage, 30);

		return new com.nn.pages.SuccessPage();
	}

	@Step("Enter new GiroPay Bank Transfer Details ")
	public void enterNewGiropayBankDetails() {
		waitForURLToBe("https://sandbox.paydirekt.de/checkout/",30);
		By giropayCookies = (By.xpath("//button[contains(text(), 'Alle Cookies akzeptieren')]"));
		if(checkElementDisplayed(giropayCookies)) {
			clickElement(giropayCookies);
			clickElement(By.xpath("//div[contains(text(), 'Testbank')]"));
			clickElement(By.xpath("//div[contains(text(), 'Test- und Spielbank AG')]"));
			clickElement(By.xpath("//button[@name='claimCheckoutButton']"));
			clickElement(By.cssSelector("#submitButton"));
			WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(30));
			wait.until(ExpectedConditions.numberOfWindowsToBe(2));
			DriverManager.getDriver().close();
			Set<String> currentWindows = DriverManager.getDriver().getWindowHandles();
			List<String> windowList = new ArrayList<>(currentWindows);
			String updatedWindow = windowList.get(0);
			DriverManager.getDriver().switchTo().window(updatedWindow);
		}else if(checkElementDisplayed(By.xpath("//button[@name='claimCheckoutButton']"))){
			clickElement(By.xpath("//button[@name='claimCheckoutButton']"));
			clickElement(By.cssSelector("#submitButton"));
			WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(30));
			wait.until(ExpectedConditions.numberOfWindowsToBe(2));
			DriverManager.getDriver().close();
			Set<String> currentWindows = DriverManager.getDriver().getWindowHandles();
			List<String> windowList = new ArrayList<>(currentWindows);
			String updatedWindow = windowList.get(0);
			DriverManager.getDriver().switchTo().window(updatedWindow);
		}else{
			clickElement(By.xpath("//div[contains(text(), 'Testbank')]"));
			clickElement(By.xpath("//div[contains(text(), 'Test- und Spielbank AG')]"));
			clickElement(By.xpath("//button[@name='claimCheckoutButton']"));
			clickElement(By.cssSelector("#submitButton"));
			WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(30));
			wait.until(ExpectedConditions.numberOfWindowsToBe(2));
			DriverManager.getDriver().close();
			Set<String> currentWindows = DriverManager.getDriver().getWindowHandles();
			List<String> windowList = new ArrayList<>(currentWindows);
			String updatedWindow = windowList.get(0);
			DriverManager.getDriver().switchTo().window(updatedWindow);
		}
	}
	@Step("Enter GiroPay Bank Transfer Details ")
	public void enterGiropayBankDetails() {
		waitForURLToBe("https://ftg-customer-integration.giropay.de/",120);
		String submitBtn1 = "//input[@type='submit' and @value='Jetzt bezahlen']";
		String submitBtn2 = "//input[@name='weiterButton' and @type='submit' and @value='Weiter']";
		String submitBtn3 = "//input[@type='submit' and @value='Login']";
		String submitBtn4 = "//input[@type='submit' and @value='Weiter']";
		String submitBtn5 = "//input[@type='submit' and @value='Jetzt bezahlen']";
		String logIn = "//input[@name='account/addition[@name=benutzerkennung]']";
		String pin = "//input[@name='ticket/pin']";
		String tan = "//input[@name='ticket/tan']";
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


			}
		}
		waitForElementVisible(By.xpath(submitBtn1));
		setText(By.xpath(logIn), "chiptanscatest4");
		setText(By.xpath(pin), "12345");
		clickElementWithJs(By.xpath(submitBtn1));
		clickElementWithJs(By.xpath(submitBtn2));
		setText(By.xpath(tan), "123456");
		clickElementWithJs(By.xpath(submitBtn3));
		clickElementWithJs(By.xpath(submitBtn4));
		setText(By.xpath(tan), "123456");
		clickElementWithJs(By.xpath(submitBtn5));

	}


	@Step("Place order with Online Bank Transfer ")
	public com.nn.pages.SuccessPage placeOrderWithOnlineBankTransfer() {
		clickPlaceOrderBtn();
		enterOnlineBankTransferDetails();
		waitForElementVisible(orderSuccessMessage, 30);
		return new com.nn.pages.SuccessPage();
	}

	@Step("Place order with Online Bank Transfer ")
	public void waitForOnlineBankTransferRedirection() {
		clickPlaceOrderBtn();
		waitForURLToBe("https://link.tink.com/",120);
	}

	@Step("Place order with Online Bank Transfer ")
	public void cancelAtOnlineBankTransferRedirection() {
		clickPlaceOrderBtn();
		waitForURLToBe("https://link.tink.com/",120);
		clickElementByRefreshing(By.cssSelector("button[data-testid='close-button']>span"));
	}

	@Step("Enter Online Bank Transfer Details ")
	public void enterOnlineBankTransferDetails() {
		waitForURLToBe("https://link.tink.com/",120);
	     clickElement(By.xpath("//span[text()='Continue']"));
		 setText(By.cssSelector("#username"),"");
	}


	@Step("Place order with Alipay")
	public com.nn.pages.SuccessPage placeOrderWithAlipay() {
		clickPlaceOrderBtn();
		waitForElementVisible(loginBtn, 30);
		clickElement(loginBtn);
		clickElement(makePaymentBtn);
		clickElement(backToShopBtn);
		waitForElementVisible(orderSuccessMessage, 30);
		return new com.nn.pages.SuccessPage();
	}

	@Step("Place order with Alipay")
	public void waitForAlipayRedirectionPage() {
		clickPlaceOrderBtn();
		waitForElementVisible(loginBtn, 30);
	}

	@Step("Place order with iDeal")
	public void cancelAtAlipayRedirectionPage() {
		clickPlaceOrderBtn();
		clickElement(By.xpath("//button[.='Abort']"));
	}

	@Step("Place order with WeChatPay")
	public com.nn.pages.SuccessPage placeOrderWithWeChatPay() {
		clickPlaceOrderBtn();
		waitForElementVisible(loginBtn, 30);
		clickElement(loginBtn);
		clickElement(makePaymentBtn);
		clickElement(backToShopBtn);
		waitForElementVisible(orderSuccessMessage, 30);
		return new com.nn.pages.SuccessPage();
	}

	@Step("Place order with WeChatPay")
	public void waitForWeChatPayRedirection() {
		clickPlaceOrderBtn();
		waitForElementVisible(loginBtn, 30);
	}

	@Step("Cancel at we chat pay")
	public void cancelAtWeChatPayRedirectionPage() {
		clickPlaceOrderBtn();
		clickElement(By.xpath("//button[.='Abort']"));
	}

	@Step("Place order with BanContact")
	public com.nn.pages.SuccessPage placeOrderWithBanContact() {
		clickPlaceOrderBtn();
		waitForElementVisible(makePaymentBtn, 30);
		clickElement(makePaymentBtn);
		clickElement(backToShopBtn);
		waitForElementVisible(orderSuccessMessage, 30);
		return new com.nn.pages.SuccessPage();
	}

	@Step("Place order with BanContact")
	public com.nn.pages.SuccessPage placeOrderWithPrzelewy(boolean wantPending) {
		clickPlaceOrderBtn();
		waitForURLToBe("https://sandbox-go.przelewy24.pl/",120);
		clickElement(By.cssSelector("div>img[alt='mBank - mTransfer']"));
		if(!wantPending)
			clickElement(By.cssSelector("#user_account_pbl_correct"));
		else
			clickElement(By.cssSelector("#user_account_pbl_pending"));
		waitForElementVisible(orderSuccessMessage, 30);
		return new com.nn.pages.SuccessPage();
	}



	@Step("Place order with BanContact")
	public void waitForPrzelewyRedirection() {
		clickPlaceOrderBtn();
		waitForURLToBe("https://sandbox-go.przelewy24.pl/",120);
	}

	@Step("Place order with BanContact")
	public void waitForBanContactRedirection() {
		clickPlaceOrderBtn();
		waitForElementVisible(makePaymentBtn, 30);
	}

	@Step("Place order with iDeal")
	public void cancelAtBanContaceRedirectionPage() {
		clickPlaceOrderBtn();
		clickElement(By.xpath("//button[.='Abort']"));
	}

	@Step("Place order with Post Finance ")
	public com.nn.pages.SuccessPage placeOrderWithPostFinance() {
		clickPlaceOrderBtn();
		enterPostFinanceCardDetails();
		waitForElementVisible(orderSuccessMessage, 30);
		return new com.nn.pages.SuccessPage();
	}

	@Step("Place order with Post Finance ")
	public void waitForPostFinanceRedirection() {
		clickPlaceOrderBtn();
		waitForURLToBe("https://epayment-t2.postfinance.ch/",120);
	}

	@Step("Cancel at Post Finance redirection page")
	public void cancelAtPostFinanceRedirection() {
		clickPlaceOrderBtn();
		waitForURLToBe("https://epayment-t2.postfinance.ch/",120);
		clickElementByRefreshing(By.cssSelector("button.fpui-button--secondary"));
	}

	@Step("Enter Post Finance Card Payment Details ")
	public void enterPostFinanceCardDetails() {
		waitForURLToBe("https://epayment-t2.postfinance.ch/",120);
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
			case DIRECT_DEBIT_ACH:
				return By.cssSelector("#direct_debit_ach");
			case BLIK:
				return By.cssSelector("#blik");
			case PAYCONIQ:
				return By.cssSelector("#payconiq");
			case MBWAY:
				return By.cssSelector("#mbway");
			default:
				throw new IllegalArgumentException("Invalid payment method: " + paymentType);
		}
	}


	@Step("Communication break at Sofort redirect page and Get Pending order number")
	public String communicationBreakGetSofortTIDPendingOrderNumber() {
		clickPlaceOrderBtn();
		waitForSofortRedirectionPage();
		rejectSofortBankCookiePopup();
		String TID = getSofortTID();
		return TID;
	}

	@Step("Communication break at Sofort redirect page and Get Pending order number")
	public String cancelAtSofortRedirection() {
		clickPlaceOrderBtn();
		waitForSofortRedirectionPage();
		rejectSofortBankCookiePopup();
		String TID = getSofortTID();
		clickElement(By.cssSelector(".back-to-merchant.cancel-transaction"));
		clickElementByRefreshing(By.cssSelector("#CancelTransaction"));
		return TID;
	}

	@Step("Communication break at GiroPay redirect page and Get Pending order number")
	public String communicationBreakGetGiroPayTIDPendingOrderNumber() {
		Map<String, String> tidOrderDetails = new HashMap<>();
		clickPlaceOrderBtn();
		waitForGiroPayNewRedirectionPage();
		String TID = getGiroPayTID();
		return TID;

	}

	@Step("Cancel at EPS redirect page")
	public void cancelAtGiropayRedirection() {
		clickPlaceOrderBtn();
		waitForGiroPayNewRedirectionPage();
		sleep(2);
		if(checkElementDisplayed(By.cssSelector("#backUrl"))){
			clickElement(By.cssSelector("#backUrl"));
			clickElementByRefreshing(By.cssSelector(".modal-content #yes"));
		}else{
			clickElement(By.xpath("//button/span[contains(text(),'giropay abbrechen + zurück zum Shop')]"));
			clickElementByRefreshing(By.xpath("//div[@class='modal-footer']/button[.='Ja']"));
		}
	}
	@Step("Cancel at GioroPay redirect page")
	public void cancelAtNewGiropayRedirection() {
		By backToBank = By.xpath("//button[text()='Zurück zur Bankensuche']");
		By abortPaymnet = By.xpath("//button[text()='Zahlung abbrechen']");
		clickPlaceOrderBtn();
		waitForGiroPayNewRedirectionPage();
		By epsCookie = By.xpath("//button[contains(text(), 'Alle Cookies akzeptieren')]");
		if (checkElementDisplayed(epsCookie)) {
			clickElement(epsCookie);
			clickElement(abortPaymnet);
		}else {
			clickElement(backToBank);
			/*clickElement(By.xpath("//div[contains(text(), 'Testbank')]"));
			clickElement(abortPaymnet);*/
			waitForElementVisible(abortPaymnet,30);
			clickElement(abortPaymnet);
		}
	}
	@Step("Verify is Sofort payment redirected")
	public void waitForSofortRedirectionPage() {
		waitForURLToBe("https://www.sofort.com/",120);
	}

	@Step("Verify is GiroPay payment redirected")
	public void waitForGiroPayRedirectionPage() {
		waitForURLToBe("https://ftg-customer-integration.giropay.de/",120);
	}
	@Step("Verify is GiroPay payment redirected")
	public void waitForGiroPayNewRedirectionPage() {
		waitForURLToBe("https://sandbox.paydirekt.de/checkout/",30);
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

	@Step("Place order with PayPal")
	public void placeOrderWithPayPal() {
		//clickPlaceOrderBtn();
		logintoPayPalandCompletePayment();
		waitForElementVisible(orderSuccessMessage, 30);

	}

	@Step("Wait for PayPal redirect page")
	public void waitForPayPalRedirection() {
		//clickPlaceOrderBtn();
		waitForURLToBe("https://www.sandbox.paypal.com/",120);
	}

	@Step("Place order with PayPal")
	public void cancelAtPayPalRedirection() {
		//clickPlaceOrderBtn();
		waitForURLToBe("https://www.sandbox.paypal.com/",120);
		clickElementByRefreshing(By.xpath("//a[contains(text(),'Cancel and return')]"));
	}

	@Step("Login to PayPal")
	public void logintoPayPalandCompletePayment() {
		waitForURLToBe("https://www.sandbox.paypal.com/",120);
		sleep(3);
		String title = getPageTitle();
		if(title.contains("Log in to your PayPal account")){
			setTextAndKey(By.cssSelector("#email"),"pb-buyer@novalnet.de",Keys.ENTER);
			setTextAndKey(By.cssSelector("#password"),"novalnet123",Keys.ENTER);
		}
		clickElement(By.cssSelector("#payment-submit-btn"));
	}

	@Step("Place order with eps ")
	public com.nn.pages.SuccessPage placeOrderWithEPS() {
		clickPlaceOrderBtn();
		//enterEPSankDetails();//Commented reason is eps redirect page UI totally changed
		enterNewEPSBankDetails();
		waitForElementVisible(orderSuccessMessage,30);
		return new com.nn.pages.SuccessPage();
	}

	@Step("Enter new eps Bank Transfer Details ")
	public void  enterNewEPSBankDetails() {
		clickElement(By.xpath("//button[text()='Next']"));
		clickElement(By.xpath("//button[text()='Login']"));
		clickElement(By.xpath("//button[text()='Make Payment']"));
		clickElement(By.xpath("//button[text()='Back to where you came from']"));
	}
	@Step("Enter eps Bank Transfer Details ")
	public void  enterEPSankDetails() {
		waitForURLToBe("https://ftg-customer-integration.giropay.de/",120);
		String continueBtn = "//input[@name='continueBtn' and @type='button' and @value='Weiter zum Bezahlen' and contains(@class,'blueButton')]";
		if (checkElementDisplayed(By.xpath(continueBtn))) {
			String bankName = "//input[@id='tags' and @type='text' and contains(@class,'ui-autocomplete-input')]";
			String acceptBic = "//div[@id='layerBic']//button[@id='yes' and @type='button' and contains(@class,'btn') and contains(@class,'btn-primary') and @data-value='1' and contains(text(),'Annehmen')]";
			String login = "//input[@id='sbtnLogin']";
			String signIn1 = "//input[@id='sbtnSign']";
			String signIn2 = "//input[@id='sbtnSignSingle']";
			String okBtn="//input[@id='sbtnOk']";
			String submitBtn="//input[@name='back2Shop']";
			reloadPage();
			reloadPage();
			sleep(1);
			setText(By.xpath(bankName), "HYPTAT22XXX");
			sleep(1);
			waitForElementVisible(By.cssSelector(".ui-menu-item"));
			setTextWithAction(By.xpath(bankName), Keys.ARROW_DOWN);
			pressEnter(By.xpath(bankName));
			clickElementWithJs(By.xpath(continueBtn));
			if(!(checkElementDisplayed(By.xpath(login)))) {
				sleep(2);
				//waitForElementClickable(By.xpath(acceptBic)); //temporary commented cause of sandbox issue
				if(checkElementDisplayed(By.xpath(acceptBic)))
					clickElementWithJs(By.xpath(acceptBic));
				waitForElementClickable(By.xpath(login));
			}
			sleep(1);
			clickElementWithJs(By.xpath(login));
			waitForElementClickable(By.xpath(signIn1));
			clickElementWithJs(By.xpath(signIn1));
			waitForElementClickable(By.xpath(signIn2));
			clickElementWithJs(By.xpath(signIn2));
			waitForElementClickable(By.xpath(okBtn));
			clickElementWithJs(By.xpath(okBtn));
			waitForElementClickable(By.xpath(submitBtn));
			clickElementWithJs(By.xpath(submitBtn));
			sleep(5);
		}


	}

	@Step("Communication break at EPS redirect page and Get Pending order number")
	public String communicationBreakGetEPSTIDPendingOrderNumber() {
		Map<String, String> tidOrderDetails = new HashMap<>();
		clickPlaceOrderBtn();
		waitForNewEPSRedirectionPage();
		String TID = getEPSTID();
		return TID;

	}

	@Step("Cancel at EPS redirect page")
	public void cancelAtEPSRedirection() {
		clickPlaceOrderBtn();
		waitForNewEPSRedirectionPage();
		clickElement(By.cssSelector("#backUrl"));
		clickElementByRefreshing(By.cssSelector(".modal-content #yes"));
	}
	@Step("Cancel at EPS redirect page")
	public void cancelAtNewEPSRedirection() {
		By backToBank = By.xpath("//button[text()='Zurück zur Bankensuche']");
		By abortPayment = By.xpath("//button[text()='Zahlung abbrechen']");
		clickPlaceOrderBtn();
		waitForNewEPSRedirectionPage();
		By epsCookie = By.xpath("//button[contains(text(), 'Alle Cookies akzeptieren')]");
		if (checkElementDisplayed(epsCookie)) {
			clickElement(epsCookie);
			clickElement(abortPayment);
		}else {
			clickElement(backToBank);
			waitForElementVisible(abortPayment);
			clickElement(abortPayment);
		}
	}
	@Step("Verify is EPS payment redirected")
	public void waitForEPSRedirectionPage() {
		waitForURLToBe("https://ftg-customer-integration.giropay.de/",120);
	}
	@Step("Verify is EPS payment redirected")
	public void waitForNewEPSRedirectionPage() {
		waitForURLToBe("https://sandbox.paydirekt.de/eps-checkout",120);
	}

	@Step("Get TID of EPS payment ")
	public String getEPSTID() {
		String continueBtn = "//input[@name='continueBtn' and @type='button' and @value='Weiter zum Bezahlen' and contains(@class,'blueButton')]";
		String tid="";
		if (checkElementDisplayed(By.xpath(continueBtn))) {
			String bankName = "//input[@id='tags' and @type='text' and contains(@class,'ui-autocomplete-input')]";
			String acceptBic = "//div[@id='layerBic']//button[@id='yes' and @type='button' and contains(@class,'btn') and contains(@class,'btn-primary') and @data-value='1' and contains(text(),'Annehmen')]";
			String login = "//input[@id='sbtnLogin']";
			String signIn1 = "//input[@id='sbtnSign']";
			String signIn2 = "//input[@id='sbtnSignSingle']";
			String okBtn="//input[@id='sbtnOk']";
			String submitBtn="//input[@name='back2Shop']";
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
			if(!(checkElementDisplayed(By.xpath(login)))) {
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

	@Step("Change the shipping address at checkout")
	public CheckoutPage changeTheShippingAtCheckout(){
		By billShippCheck = By.cssSelector("#billing-address-same-as-shipping-novalnetPay");
		waitForElementVisible(billShippCheck,60);
		if(checkElementChecked(billShippCheck)){
			handleStaleElement(billShippCheck,(d)->d.findElement(billShippCheck).click());
			sleep(1);
			By address = By.xpath("//input[@id='billing-address-same-as-shipping-novalnetPay']/../following-sibling::fieldset//select[@name='billing_address_id']");
			handleStaleElement(address,(d)->{
				Select s = new Select(d.findElement(address));
				var val = s.getFirstSelectedOption().getText();
				s.getOptions().stream()
						.filter(e->!e.getText().contains(val) && !e.getText().contains("New Address"))
						.findFirst()
						.ifPresentOrElse(WebElement::click, ()-> {throw new RuntimeException("No element present");});
			});
			clickElement(By.xpath("//input[@id='billing-address-same-as-shipping-novalnetPay']/../following-sibling::fieldset//button[contains(@class,'update')]"));
			sleep(3);
			reloadPage();
		}
		return this;
	}


	public CheckoutPage isLogoDisplayed(String paymentName,boolean expected){
		switchToFrame(nnPaymentFrame);
		String xpath = "//img[contains(@alt,'"+paymentName+"')]";
		By paymentDiv=By.xpath("//img[contains(@alt,'"+paymentName+"')]/ancestor::div[contains(@class,'payment-type')]");
		waitForElementPresent(paymentDiv,120);
		JavascriptExecutor js = (JavascriptExecutor)DriverManager.getDriver();
		Log.info("Scrolling ro payment element");
		js.executeScript("arguments[0].scrollIntoView(true);", getElement(paymentDiv));
		boolean logoDisplayed = waitForElementVisible(By.xpath(xpath),20,"Waiting for logo to display");
		verifyEquals(logoDisplayed, expected);
		return this;
	}


	public CheckoutPage isBusinessNameDisplayed(String businessName,boolean expected) {
		String currentWindowTitle= DriverManager.getDriver().getTitle();
		switchToWindow();
		switchToFrame(By.cssSelector("iframe[src^='https://payments.google.com/payments/u/0/embedded/']"));
		boolean result = getElement(By.xpath("//div[contains(@class,'b3-updatable-cart-dialog-summary-label') and @data-was-visible='true']")).isDisplayed();
		verifyEquals(result, expected);
		String businessNameInGpaySheet = getElement(By.xpath("//div[contains(@class,'b3-updatable-cart-dialog-summary-label') and @data-was-visible='true']")).getText();
		String resultbusinessNameInGpaySheet = businessNameInGpaySheet.trim().split(" ")[1];
		verifyEquals(businessName,resultbusinessNameInGpaySheet);
		DriverActions.switchToNewWindowOrTabByTitle(currentWindowTitle);
		return this;
	}

	public CheckoutPage isBusinessNameDisplayed(boolean expected) {
		String currentWindowTitle= DriverManager.getDriver().getTitle();
		switchToWindow();
		switchToFrame(By.cssSelector("iframe[src^='https://payments.google.com/payments/u/0/embedded/']"));
		boolean result = getElement(By.xpath("//div[contains(@class,'b3-updatable-cart-dialog-summary-label') and @data-was-visible='true']")).isDisplayed();
		verifyEquals(result, expected);
		DriverActions.switchToNewWindowOrTabByTitle(currentWindowTitle);
		return this;
	}

	// by Nizam
	public CheckoutPage clickGooglePayButton(){
		waitForElementClickable(googlePayBtn);
		clickElementWithJs(googlePayBtn);
		sleep(2);
		switchToDefaultContent();
		return this;
	}


}
