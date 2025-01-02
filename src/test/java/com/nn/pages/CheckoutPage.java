
package com.nn.pages;

import com.nn.drivers.DriverManager;
import com.nn.helpers.ExcelHelpers;
import com.nn.reports.AllureManager;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.aventstack.extentreports.Status;
import com.nn.constants.Constants;
import com.nn.reports.ExtentTestManager;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.TRUSTLY;
import static com.nn.utilities.DriverActions.*;

public class CheckoutPage {

	By paymentDivBox = By.cssSelector("+div.payment_box");
	private By paymentDiv = By.cssSelector("#payment");
	private By woocommerceError = By.cssSelector(".woocommerce-error");
	private By shopLoader = By.cssSelector(".blockUI.blockOverlay");
	private By orderTotal = By.cssSelector("tr.order-total bdi");
	private By billingCompany = By.cssSelector("#billing_company");
	private By creditCardDiv = By.cssSelector(".wc_payment_method.payment_method_novalnet_cc");
	private By creditCard = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_cc']");
	private By creditCardiFrame = By.cssSelector("#novalnet_cc_iframe");
	private By inlineForm = By.cssSelector("div.in-line-form");
	private By ccTestMode = By.cssSelector(".payment_method_novalnet_cc .novalnet-test-mode");
	private By creditCardHolder = By.cssSelector("#card_holder");
	private By creditCardNumber = By.cssSelector("#card_number");

	//private By creditCardDecline = By.cssSelector(".woocommerce-notices-wrapper .woocommerce-error li");

	private By creditCardDecline = By.xpath("//ul[contains(@class, 'woocommerce-error')]/li | //div[contains(@class,'error')]//div");
	private By creditCardExp = By.cssSelector("#expiry_date");
	private By creditCardCVV = By.cssSelector("#cvc");
	private By saveCardCheckbox = By.cssSelector("#wc-novalnet_cc-new-payment-method");
	private By saveCardCheckboxDiv = By.cssSelector(".wc_payment_method.payment_method_novalnet_cc .form-row");
	private By oneClickToken = By.cssSelector(".woocommerce-SavedPaymentMethods-token input");
	private By useNewPaymentCheckbox = By.cssSelector("#wc-novalnet_cc-payment-token-new");
	private By placeOrderBtn = By.cssSelector("#place_order");
	private By novalnetPaymentLables = By.cssSelector(".wc_payment_methods label[for*='payment_method_novalnet']");
	//private By orderSuccessMessage = By.cssSelector(".woocommerce-notice--success");
	private By orderSuccessMessage = By.xpath("//p[contains(@class,'thankyou-order-received')] | //p[contains(text(),'Thank you')]");
	private By orderDetailsDiv = By.cssSelector(".woocommerce-order-details");
	private By redirectPage = By.cssSelector("form[action^='https://test.saferpay.com/ThreeDSAuthentication/']");
	private By redirectPageSubmitBtn = By.cssSelector("#submit");
	private By paymentUpdatedAlert = By.xpath("//*[ contains(text(),'Payment method updated.')]");

	//SEPA
	private By directDebitSEPA = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_sepa']");
	private By sepaIban = By.cssSelector("#novalnet_sepa_iban");

	private By sepaTestMode = By.cssSelector(".payment_method_novalnet_sepa .novalnet-test-mode");
	private By sepaUseNewPaymentCheckbox = By.cssSelector("#wc-novalnet_sepa-payment-token-new");
	private By sepaOneClickToken = By.cssSelector(".payment_method_novalnet_sepa .woocommerce-SavedPaymentMethods-token input");
	private By sepaSaveCardCheckbox = By.cssSelector("#wc-novalnet_sepa-new-payment-method");


	//SEPA Guarantee
	private By sepaGuarantee = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_guaranteed_sepa']");
	private By sepaGuaranteeIban = By.cssSelector("#novalnet_guaranteed_sepa_iban");
	private By sepaGuaranteeTestMode = By.cssSelector(".payment_method_novalnet_guaranteed_sepa .novalnet-test-mode");
	private By sepaGuaranteeUseNewPaymentCheckbox = By.cssSelector("#wc-novalnet_guaranteed_sepa-payment-token-new");
	private By sepaGuaranteeOneClickToken = By.cssSelector(".payment_method_novalnet_guaranteed_sepa .woocommerce-SavedPaymentMethods-token input");
	private By sepaGuaranteeSaveCardCheckbox = By.cssSelector("#wc-novalnet_guaranteed_sepa-new-payment-method");
	private By sepaGuaranteeDOB = By.cssSelector("#novalnet_guaranteed_sepa_dob");

	//Instalment SEPA
	private By instalmentSepa = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_instalment_sepa']");
	private By instalmentSepaIban = By.cssSelector("#novalnet_instalment_sepa_iban");
	private By instalmentSepaTestMode = By.cssSelector(".payment_method_novalnet_instalment_sepa .novalnet-test-mode");
	private By instalmentSepaUseNewPaymentCheckbox = By.cssSelector("#wc-novalnet_instalment_sepa-payment-token-new");
	private By instalmentSepaOneClickToken = By.cssSelector(".payment_method_novalnet_instalment_sepa .woocommerce-SavedPaymentMethods-token input");
	private By instalmentSepaSaveCardCheckbox = By.cssSelector("#wc-novalnet_instalment_sepa-new-payment-method");
	private By instalmentSepaDOB = By.cssSelector("#novalnet_instalment_sepa_dob");

	//Invoice Guarantee
	private By invoiceGuarantee = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_guaranteed_invoice']");
	private By invoiceGuaranteeTestMode = By.cssSelector(".payment_method_novalnet_guaranteed_invoice .novalnet-test-mode");
	private By invoiceGuaranteeDOB = By.cssSelector("#novalnet_guaranteed_invoice_dob");

	//Instalment Invoice
	private By instalmentInvoice = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_instalment_invoice']");
	private By instalmentInvoiceTestMode = By.cssSelector(".payment_method_novalnet_instalment_invoice .novalnet-test-mode");
	private By instalmentInvoiceDOB = By.cssSelector("#novalnet_instalment_invoice_dob");
	private By instalmentInvoiceSelectCycles = By.cssSelector("#novalnet_instalment_invoice_period");

	//iDeal
	private By iDeal = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_ideal']");

	private By online_transfer = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_instantbank']");

	private By online_bank_transfer = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_online_bank_transfer']");

	private By trustly = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_trustly']");

	private By giropay = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_giropay']");

	private By postfinance = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_postfinance_card']");

	private By przelewy24 = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_przelewy24']");

	private By eps = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_eps']");

	private By banContact = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_bancontact']");

	private By alipay = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_alipay']");

	private By weChatPay = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_wechatpay']");

	private By multibanco = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_multibanco']");
	private By paypal = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_paypal']");
	private By iDealTestMode = By.cssSelector(".payment_method_novalnet_ideal .novalnet-test-mode");

	private By onlineTransferTestMode = By.cssSelector(".payment_method_novalnet_instantbank .novalnet-test-mode");

	private By onlineBankTransferTestMode = By.cssSelector(".payment_method_novalnet_online_bank_transfer .novalnet-test-mode");

	private By trustlyTestMode = By.cssSelector(".payment_method_novalnet_trustly .novalnet-test-mode");

	private By giropayTestMode = By.cssSelector(".payment_method_novalnet_giropay .novalnet-test-mode");

	private By postFinanceTestMode = By.cssSelector(".payment_method_novalnet_postfinance_card .novalnet-test-mode");

	private By przelewy24TestMode = By.cssSelector(".payment_method_novalnet_przelewy24 .novalnet-test-mode");

	private By epsTestMode = By.cssSelector(".payment_method_novalnet_eps .novalnet-test-mode");

	private By banContactTestMode = By.cssSelector(".payment_method_novalnet_bancontact .novalnet-test-mode");

	private By multiBancoTestMode = By.cssSelector(".payment_method_novalnet_multibanco .novalnet-test-mode");

	private By alipayTestMode = By.cssSelector(".payment_method_novalnet_alipay .novalnet-test-mode");

	private By weChatPayTestMode = By.cssSelector(".payment_method_novalnet_wechatpay .novalnet-test-mode");


	private By paypalTestMode = By.cssSelector(".payment_method_novalnet_paypal .novalnet-test-mode");
	private By nextBtn = By.xpath("//button[@class='btn btn-primary' and text()='Next']");

	private By sofortBankLoginButton = By.xpath("//button[@class='button-right primary has-indicator' and @data-skip-leave-alert='true']");

	private By sofortDemoBank = By.xpath("//p[@class='description' and @data-label='demo-bank']");

	private By sofortCookieDenyAlert = By.xpath("//div[@id='Modal']//button[@class='cookie-modal-deny-all button-tertiary']");


	private By payPalLoginBtn = By.xpath("//button[@id='btnLogin']");

	private By payPalnextBtn = By.xpath("//button[@id='btnNext']");

	private By loginBtn = By.xpath("//button[@class='btn btn-primary' and text()='Login']");
	private By makePaymentBtn = By.xpath("//button[@class='btn btn-primary' and text()='Make Payment']");
	private By backToShopBtn = By.xpath("//button[@class='btn btn-primary' and text()='Back to where you came from']");

	//Prepayment
	private By prepayment = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_prepayment']");

	private By onCashPayment = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_barzahlen']");
	private By prepaymentTestMode = By.cssSelector(".payment_method_novalnet_prepayment .novalnet-test-mode");

	private By OnCashPaymentTestMode = By.cssSelector(".payment_method_novalnet_barzahlen .novalnet-test-mode");

	//Invoice
	private By invoice = By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_invoice']");
	private By invoiceTestMode = By.cssSelector(".payment_method_novalnet_invoice .novalnet-test-mode");

	private By saveCardCheckboxSEPA = By.cssSelector("#wc-novalnet_sepa-new-payment-method");

	private By  maskedCC = By.xpath("//li[@class='woocommerce-SavedPaymentMethods-token']");

	private Map<String, String> billingInfo = new HashMap<>();

	private By gpayButton =By.xpath("//button[@aria-label='Book with GPay']");


	/**
	 * Common functions
	 * **/

	@Step("Set Billing Information ")
	private void setBillingAddressWithDefaultValues(){
		billingInfo = new HashMap<String, String>();
		billingInfo.put("fname", "Norbert");
		billingInfo.put("lname", "Maier");
		billingInfo.put("country", "Germany");
		billingInfo.put("billing_address_1", "Hauptstr");
		billingInfo.put("billing_address_2", "9");
		billingInfo.put("billing_postcode", "66862");
		billingInfo.put("billing_phone", "+4989123456");
		billingInfo.put("billing_city", "Kaiserslautern");
		String email = "admin" + System.currentTimeMillis() + "@novalnet.com";
		billingInfo.put("billing_email", email);

	}

	@Step("Enter Billing Information ")
	public void enterBillingInfoWithDefaultValues() {
		if(billingInfo == null) {
			setBillingAddressWithDefaultValues();
		}
		String firstName = billingInfo.get("fname");
		String lastName = billingInfo.get("lname");
		String country = billingInfo.get("country");
		String street1 = billingInfo.get("billing_address_1");
		String street2 = billingInfo.get("billing_address_2");
		String postalCode = billingInfo.get("billing_postcode");
		String phone = billingInfo.get("billing_phone");
		String city = billingInfo.get("billing_city");
		String email = billingInfo.get("billing_email");

		WebElement fname = DriverManager.getDriver().findElement(By.xpath("//input[@id='billing_first_name']"));
		fname.sendKeys(firstName);

		WebElement lname = DriverManager.getDriver().findElement(By.xpath("//input[@id='billing_last_name']"));
		lname.sendKeys(lastName);

        /*	if (billingInfo.containsKey("company")) {
			WebElement companyField = DriverManager.getDriver().findElement(By.xpath("//input[@id='billing_company']"));
			companyField.sendKeys(billingInfo.get("company"));
		}*/

		// Click on the country dropdown to expand it
		WebElement countryDropdown = DriverManager.getDriver().findElement(By.xpath("//span[@id='select2-billing_country-container']"));
		countryDropdown.click();

		// Select "Germany" from the dropdown
		WebElement countryOption = DriverManager.getDriver().findElement(By.xpath("//li[@role='option' and text()='" + country + "']"));
		countryOption.click();


		WebElement street1Field = DriverManager.getDriver().findElement(By.xpath("//input[@id='billing_address_1']"));
		street1Field.sendKeys(street1);


		WebElement street2Field = DriverManager.getDriver().findElement(By.xpath("//input[@id='billing_address_2']"));
		street2Field.sendKeys(street2);


        /*if (billingInfo.containsKey("state")) {

		}*/

		WebElement postalCodeField = DriverManager.getDriver().findElement(By.xpath("//input[@id='billing_postcode']"));
		postalCodeField.sendKeys(postalCode);

		WebElement phoneField = DriverManager.getDriver().findElement(By.xpath("//input[@id='billing_phone']"));
		phoneField.sendKeys(phone);

		WebElement emailField = DriverManager.getDriver().findElement(By.xpath("//input[@id='billing_email']"));
		emailField.sendKeys(email);


	}

	@Step("Enter billing address at checkout")
	public void setBillingAddressCheckout(Map<String,String> address){
		if(!getInputFieldText(By.xpath("//input[@id='billing_first_name']")).trim().equals(address.get("FirstName"))){
			setText(By.xpath("//input[@id='billing_first_name']"),address.get("FirstName"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_last_name']")).trim().equals(address.get("LastName"))){
			setText(By.xpath("//input[@id='billing_last_name']"),address.get("LastName"));
		}
		clearText(By.xpath("//input[@id='billing_company']"));

		if(!getInputFieldText(By.xpath("//input[@id='billing_address_1']")).trim().equals(address.get("HouseNo")+" , "+address.get("Street"))){
			setText(By.xpath("//input[@id='billing_address_1']"),address.get("HouseNo")+" , "+address.get("Street"));
		}
		clearText(By.xpath("//input[@id='billing_address_2']"));
		if(!getInputFieldText(By.xpath("//input[@id='billing_postcode']")).trim().equals(address.get("Zip"))){
			setText(By.xpath("//input[@id='billing_postcode']"),address.get("Zip"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_city']")).trim().equals(address.get("City"))){
			setText(By.xpath("//input[@id='billing_city']"),address.get("City"));
		}

		if(!getInputFieldText(By.xpath("//input[@id='billing_phone']")).trim().equals("045818858555")){
			setTextWithAction(By.xpath("//input[@id='billing_phone']"),"045818858555");
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_email']")).trim().equals("automation_test@novalnetsolutions.com")){
			setTextWithAction(By.xpath("//input[@id='billing_email']"),"automation_test@novalnetsolutions.com");
		}

		if(!getElementText(By.xpath("//span[@id='select2-billing_country-container']")).trim().equals(address.get("Country"))){
			clickElement(By.xpath("//span[@id='select2-billing_country-container']"));
			//li[contains(@id,'select2-billing_country-result') and text()='Ghana']
			String css = "input[aria-owns='select2-billing_country-results']";
			setText(By.cssSelector(css), address.get("Country"));
			pressEnter(By.xpath("//span[@id='select2-billing_country-container']"));

		}
		if(!getElementText(By.xpath("//span[@id='select2-billing_state-container']")).trim().contains(address.get("State"))){
			clickElement(By.xpath("//span[@id='select2-billing_state-container']"));
			//li[contains(@id,'select2-billing_state-result') and text()='Berlin']
			String css = "input[aria-owns='select2-billing_state-results']";
			setText(By.cssSelector(css), address.get("State"));
			pressEnter(By.xpath("//span[@id='select2-billing_state-container']"));
		}

	}



	@Step("Enter B2C billing address at checkout")
	public void setB2CBillingAtCheckout(){
		Map<String,String> address = ExcelHelpers.addressGuaranteeB2C();
		if(!getInputFieldText(By.xpath("//input[@id='billing_first_name']")).trim().equals(address.get("FirstName"))){
			setText(By.xpath("//input[@id='billing_first_name']"),address.get("FirstName"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_last_name']")).trim().equals(address.get("LastName"))){
			setText(By.xpath("//input[@id='billing_last_name']"),address.get("LastName"));
		}
		clearText(By.xpath("//input[@id='billing_company']"));
		if(!getElementText(By.xpath("//span[@id='select2-billing_country-container']")).trim().equals(address.get("Country"))){
			clickElement(By.xpath("//span[@id='select2-billing_country-container']"));
			//li[contains(@id,'select2-billing_country-result') and text()='Ghana']
			String css = "input[aria-owns='select2-billing_country-results']";
			setText(By.cssSelector(css), address.get("Country"));
			pressEnter(By.xpath("//span[@id='select2-billing_country-container']"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_address_1']")).trim().equals(address.get("HouseNo")+" , "+address.get("Street"))){
			setText(By.xpath("//input[@id='billing_address_1']"),address.get("HouseNo")+" , "+address.get("Street"));
		}
		clearText(By.xpath("//input[@id='billing_address_2']"));
		if(!getInputFieldText(By.xpath("//input[@id='billing_postcode']")).trim().equals(address.get("Zip"))){
			setText(By.xpath("//input[@id='billing_postcode']"),address.get("Zip"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_city']")).trim().equals(address.get("City"))){
			setText(By.xpath("//input[@id='billing_city']"),address.get("City"));
		}
		if(!getElementText(By.xpath("//span[@id='select2-billing_state-container']")).trim().equals(address.get("State"))){
			clickElement(By.xpath("//span[@id='select2-billing_state-container']"));
			//li[contains(@id,'select2-billing_state-result') and text()='Berlin']
			String css = "input[aria-owns='select2-billing_state-results']";
			setText(By.cssSelector(css), address.get("State"));
			pressEnter(By.xpath("//span[@id='select2-billing_state-container']"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_phone']")).trim().equals("045818858555")){
			setText(By.xpath("//input[@id='billing_phone']"),"045818858555");
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_email']")).trim().equals("automation_test@novalnetsolutions.com")){
			setText(By.xpath("//input[@id='billing_email']"),"automation_test@novalnetsolutions.com");
		}
		sleep(3);
	}

	@Step("Enter billing address at checkout")
	public void setBillingAtCheckout(String country){
		Map<String,String> address = ExcelHelpers.addressGuaranteeB2C();
		if(!getInputFieldText(By.xpath("//input[@id='billing_first_name']")).trim().equals(address.get("FirstName"))){
			setText(By.xpath("//input[@id='billing_first_name']"),address.get("FirstName"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_last_name']")).trim().equals(address.get("LastName"))){
			setText(By.xpath("//input[@id='billing_last_name']"),address.get("LastName"));
		}
		clearText(By.xpath("//input[@id='billing_company']"));
		if(!getElementText(By.xpath("//span[@id='select2-billing_country-container']")).trim().equals(country)){
			clickElement(By.xpath("//span[@id='select2-billing_country-container']"));
			//li[contains(@id,'select2-billing_country-result') and text()='Ghana']
			String css = "input[aria-owns='select2-billing_country-results']";
			setText(By.cssSelector(css), country);
			pressEnter(By.xpath("//span[@id='select2-billing_country-container']"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_address_1']")).trim().equals(address.get("HouseNo")+" , "+address.get("Street"))){
			setText(By.xpath("//input[@id='billing_address_1']"),address.get("HouseNo")+" , "+address.get("Street"));
		}
		clearText(By.xpath("//input[@id='billing_address_2']"));
		if(!getInputFieldText(By.xpath("//input[@id='billing_postcode']")).trim().equals(address.get("Zip"))){
			setText(By.xpath("//input[@id='billing_postcode']"),address.get("Zip"));
		}
		if(getElementText(By.xpath("//span[@id='select2-billing_country-container']")).trim().equals("Belgium")||
			getElementText(By.xpath("//span[@id='select2-billing_country-container']")).trim().equals("Austria"))
			setText(By.xpath("//input[@id='billing_postcode']"),"1000");

		if(!getInputFieldText(By.xpath("//input[@id='billing_city']")).trim().equals(address.get("City"))){
			setText(By.xpath("//input[@id='billing_city']"),address.get("City"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_phone']")).trim().equals("045818858555")){
			setText(By.xpath("//input[@id='billing_phone']"),"045818858555");
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_email']")).trim().equals("automation_test@novalnetsolutions.com")){
			setText(By.xpath("//input[@id='billing_email']"),"automation_test@novalnetsolutions.com");
		}
		if(waitForElementVisible(By.cssSelector("label[for='billing_state']>.required"),3,"")){
			clickElement(By.xpath("//span[@id='select2-billing_state-container']"));
			pressEnter(By.xpath("//span[@id='select2-billing_state-container']"));
		}
		sleep(3);

	}

	@Step("Enter B2B billing address at checkout")
	public void setB2BBillingAtCheckout(){
		Map<String,String> address = ExcelHelpers.addressGuaranteeB2B();
		if(!getInputFieldText(By.xpath("//input[@id='billing_first_name']")).trim().equals(address.get("FirstName"))){
			setText(By.xpath("//input[@id='billing_first_name']"),address.get("FirstName"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_last_name']")).trim().equals(address.get("LastName"))){
			setText(By.xpath("//input[@id='billing_last_name']"),address.get("LastName"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_company']")).trim().equals(address.get("Company"))){
			setText(By.xpath("//input[@id='billing_company']"),address.get("Company"));
		}
		if(!getElementText(By.xpath("//span[@id='select2-billing_country-container']")).trim().equals(address.get("Country"))){
			clickElement(By.xpath("//span[@id='select2-billing_country-container']"));
			//li[contains(@id,'select2-billing_country-result') and text()='Ghana']
			String css = "input[aria-owns='select2-billing_country-results']";
			setText(By.cssSelector(css), address.get("Country"));
			pressEnter(By.xpath("//span[@id='select2-billing_country-container']"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_address_1']")).trim().equals(address.get("HouseNo")+" , "+address.get("Street"))){
			setText(By.xpath("//input[@id='billing_address_1']"),address.get("HouseNo")+" , "+address.get("Street"));
		}
		clearText(By.xpath("//input[@id='billing_address_2']"));
		if(!getInputFieldText(By.xpath("//input[@id='billing_postcode']")).trim().equals(address.get("Zip"))){
			setText(By.xpath("//input[@id='billing_postcode']"),address.get("Zip"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_city']")).trim().equals(address.get("City"))){
			setText(By.xpath("//input[@id='billing_city']"),address.get("City"));
		}
		if(!getElementText(By.xpath("//span[@id='select2-billing_state-container']")).trim().equals(address.get("State"))){
			clickElement(By.xpath("//span[@id='select2-billing_state-container']"));
			//li[contains(@id,'select2-billing_state-result') and text()='Berlin']
			String css = "input[aria-owns='select2-billing_state-results']";
			setText(By.cssSelector(css), address.get("State"));
			pressEnter(By.xpath("//span[@id='select2-billing_state-container']"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_phone']")).trim().equals("045818858555")){
			setText(By.xpath("//input[@id='billing_phone']"),"045818858555");
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_email']")).trim().equals("automation_test@novalnetsolutions.com")){
			setText(By.xpath("//input[@id='billing_email']"),"automation_test@novalnetsolutions.com");
		}
		sleep(3);
	}
	@Step("Enter B2B billing address at checkout")
	public void setB2BBillingAtCheckoutPending(){
		Map<String,String> address = ExcelHelpers.addressGuaranteeB2BPending();
		if(!getInputFieldText(By.xpath("//input[@id='billing_first_name']")).trim().equals(address.get("FirstName"))){
			setText(By.xpath("//input[@id='billing_first_name']"),address.get("FirstName"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_last_name']")).trim().equals(address.get("LastName"))){
			setText(By.xpath("//input[@id='billing_last_name']"),address.get("LastName"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_company']")).trim().equals(address.get("Company"))){
			setText(By.xpath("//input[@id='billing_company']"),address.get("Company"));
		}
		if(!getElementText(By.xpath("//span[@id='select2-billing_country-container']")).trim().equals(address.get("Country"))){
			clickElement(By.xpath("//span[@id='select2-billing_country-container']"));
			//li[contains(@id,'select2-billing_country-result') and text()='Ghana']
			String css = "input[aria-owns='select2-billing_country-results']";
			setText(By.cssSelector(css), address.get("Country"));
			pressEnter(By.xpath("//span[@id='select2-billing_country-container']"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_address_1']")).trim().equals(address.get("HouseNo")+" , "+address.get("Street"))){
			setText(By.xpath("//input[@id='billing_address_1']"),address.get("HouseNo")+" , "+address.get("Street"));
		}
		clearText(By.xpath("//input[@id='billing_address_2']"));
		if(!getInputFieldText(By.xpath("//input[@id='billing_postcode']")).trim().equals(address.get("Zip"))){
			setText(By.xpath("//input[@id='billing_postcode']"),address.get("Zip"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_city']")).trim().equals(address.get("City"))){
			setText(By.xpath("//input[@id='billing_city']"),address.get("City"));
		}
		if(!getElementText(By.xpath("//span[@id='select2-billing_state-container']")).trim().equals(address.get("State"))){
			clickElement(By.xpath("//span[@id='select2-billing_state-container']"));
			//li[contains(@id,'select2-billing_state-result') and text()='Berlin']
			String css = "input[aria-owns='select2-billing_state-results']";
			setText(By.cssSelector(css), address.get("State"));
			pressEnter(By.xpath("//span[@id='select2-billing_state-container']"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_phone']")).trim().equals("045818858555")){
			setText(By.xpath("//input[@id='billing_phone']"),"045818858555");
		}
		if(!getInputFieldText(By.xpath("//input[@id='billing_email']")).trim().equals("automation_test@novalnetsolutions.com")){
			setText(By.xpath("//input[@id='billing_email']"),"automation_test@novalnetsolutions.com");
		}
		sleep(3);
	}

	@Step("Set Billing Company")
	public void setBillingCompany(){
		Map<String,String> address = ExcelHelpers.addressGuaranteeB2B();
		if(!getInputFieldText(By.xpath("//input[@id='billing_company']")).trim().equals(address.get("Company"))){
			setText(By.xpath("//input[@id='billing_company']"),address.get("Company"));
			pressTab(By.xpath("//input[@id='billing_company']"));
			sleep(2);
		}
	}
	@Step("Set Billing Company")
	public void EnterBillingCompany(String company){
		Map<String,String> address = ExcelHelpers.addressGuaranteeB2B();
		if(!getInputFieldText(By.xpath("//input[@id='billing_company']")).trim().equals(company)){
			setText(By.xpath("//input[@id='billing_company']"),company);
			pressTab(By.xpath("//input[@id='billing_company']"));
			sleep(2);
		}
	}
	@Step("Set Billing Company")
	public void setBillingEmail(String email){
		if(!getInputFieldText(By.xpath("//input[@id='billing_email']")).trim().equals(email)){
			setText(By.xpath("//input[@id='billing_email']"),email);
			clickOutsideForm();
			sleep(1);
		}
	}

	@Step("Click different shipping checkbox")
	public void clickDifferentShipping(){
		clickElementWithJs(By.cssSelector("#ship-to-different-address-checkbox"));
		setExpectedCondition(d -> d.findElement(By.cssSelector("#ship-to-different-address-checkbox")).isSelected(),1,"Waiting for different shipping checkbox checked");
		sleep(2.5);
	}


	public void load() {
		openURL(Constants.URL_FRONTEND+"index.php/checkout/");
		waitForElementVisible(paymentDiv,30);
	}

	public void selectPaymentWithPaymentName(String paymentName) {
		//label[contains(@for,'payment_method_novalnet') and contains(text(),'Credit/Debit Cards')]
		String xpath = "//label[contains(@for,'payment_method_novalnet') and contains(text(),'"+paymentName+"')]";
		clickElementWithJs(By.xpath(xpath));
	}

	@Step("Click place order button")
	public void clickPlaceOrderBtn() {
		clickElementWithJs(placeOrderBtn);
	}

	public void waitForCreditCardError(String expectedError){
		waitForElementVisible(creditCardDecline,30);
		verifyAssertEquals(getElementText(creditCardDecline).trim(),expectedError,"CreditCard Error");
	}

	@Step("Double Click place order button")
	public void doubleclickPlaceOrderBtn() {
		doubleClickElement(placeOrderBtn);
		waitForElementVisible(orderSuccessMessage,30);
	}

	@Step("Login to PayPal")
	public void logintoPayPalandCompletePayment() {
		waitForURLToBe("https://www.sandbox.paypal.com/", 120);
		boolean isLoggedIn = setExpectedCondition(d->d.getTitle().contains("PayPal Checkout"),6);
		if (!isLoggedIn) {
			setTextAndKey(By.cssSelector("#email"), "pb-buyer@novalnet.de", Keys.ENTER);
			setTextAndKey(By.cssSelector("#password"), "novalnet123", Keys.ENTER);
		}
		clickElement(By.cssSelector("#payment-submit-btn"));
	}


	@Step("Login to PayPal")
	public void waitForPaypalRedirectionPage() {
		waitForURLToBe("https://www.sandbox.paypal.com/", 120);
	}

	@Step("Click place order button")
	public SuccessPage placeOrder(){
		clickPlaceOrderBtn();
		waitForTitleContains("Order Confirmation",120);
		waitForElementVisible(orderSuccessMessage,120);
		return new SuccessPage();
	}

	public void waitCCAuthenticationPage() {
		if(setExpectedCondition(d->d.getCurrentUrl().contains("https://test.saferpay.com/"),30)){
			ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
			if(waitForElementVisible(By.cssSelector("#submit"),5,"")){
				clickElement(By.cssSelector("#submit"));
			}
		}
	}

	public void cancelAtCCAuthenticationPage() {
		waitForURLToBe("https://test.saferpay.com/",60);
		clickElement(By.cssSelector("#cancel"));
	}

	public String getCheckoutPaymentError(){
		waitForElementVisible(By.xpath("//ul[contains(@class, 'woocommerce-error')]/li | //div[contains(@class,'error')]//div"));
		return getElementText(By.xpath("//ul[contains(@class, 'woocommerce-error')]/li | //div[contains(@class,'error')]//div")).strip();
	}


	private void waitForPaymentDivToLoad(By by){
		waitForElementHasAttribute(By.cssSelector(getStringFromBy(by).concat(getStringFromBy(paymentDivBox))),"style","");
	}

	/***
	 * 	Credit card functions
	 * **/

	@Step("Verify CreditCard Displayed")
	public boolean isCreditCardDisplayed() {
		waitForElementClickable(creditCard,15);
		clickElementWithJs(creditCard);
		//waitForPaymentDivToLoad(creditCard);
		//highlightElement(creditCardiFrame);
		//ExtentTestManager.addScreenShot("<b>Payment list in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(creditCard);
		ExtentTestManager.logMessage("<b>Creditcard payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Creditcard payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Fill CreditCard form with card number {0}, expiry date {1}, cvc/cvv {2}")
	public void fillCreditCardForm(String cardNumber, String expDate2, String cvv) {
		switchToFrame(creditCardiFrame);
		waitForElementVisible(creditCardNumber,60);
		setText(creditCardNumber, cardNumber);
		setText(creditCardExp, expDate2);
		setText(creditCardCVV, cvv);
		switchToDefaultContent();
		ExtentTestManager.addScreenShot("<b>CreditCard iFrame After credentials filled</b>");
		AllureManager.saveScreenshot("Screenshot: CreditCard iFrame After credentials filled");
	}

	@Step("Verify Save for checkbox")
	public void verifySaveCardCheckboxChecked(boolean expected) {
		boolean displayed = checkElementDisplayed(saveCardCheckbox);
		boolean checked =false;
		verifyAssertEquals(displayed, expected,"<b>Creditcard Save for future checkbox display status: </b>");
		if(displayed) {
			checked =checkElementChecked(saveCardCheckbox);
			verifyAssertEquals(checked, expected,"<b>Creditcard Save for future checkbox enabled status: </b>");
			highlightElement(saveCardCheckboxDiv);
		}
	}

	@Step("Uncheck CC save card checkbox")
	public void uncheckSaveCardCheckbox() {
		if(checkElementChecked(saveCardCheckbox)){
			clickElement(saveCardCheckbox);
		}
	}

	public void verifyCCTokenDisplayedAndChecked(boolean expected) {
		boolean displayed = checkElementDisplayed(oneClickToken);
		boolean checked =false;
		if(displayed) {
			checked =checkElementChecked(oneClickToken);
			verifyAssertEquals(checked, expected,"<b>Creditcard payment Token checked status: </b>");
			highlightElement(creditCardDiv);
		}
		verifyAssertEquals(displayed, expected,"<b>Creditcard payment Token display status: </b>");
	}


	@Step("Click use new payment method checkbox CC")
	public void clickUseNewPaymentMethod() {
		if(!checkElementChecked(useNewPaymentCheckbox))
			clickElementWithJs(useNewPaymentCheckbox);
	}

	public void ifTokenDisplayedClickNew(){
		if(checkElementDisplayed(oneClickToken))
			clickElementWithJs(useNewPaymentCheckbox);
	}

	@Step("Verify InlineForm Displayed")
	public void verifyInlineFormDisplayed(boolean expected) {
		boolean actual =findElementInFrame(creditCardiFrame, inlineForm);
		verifyAssertEquals(actual, expected,"<b> Creditcard Inline Form Display status: </b> ");
	}

	@Step("Creditcard Test Mode")
	public void verifyCCTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(ccTestMode);
		if(actual)
			highlightElement(ccTestMode);
		verifyAssertEquals(actual, expected,"<b> Creditcard Test Mode status: </b>");
	}

	@Step("Verify CC iFrame Displayed")
	public void verifyCCiFrameDisplayed(boolean expected) {
		boolean actual = waitForElementVisible(creditCardiFrame,2,"iFrame Display check");
		if(actual)
			highlightElement(creditCardiFrame);
		verifyAssertEquals(actual, expected,"<b> Creditcard iFrame Display status: </b>");
	}

	@Step("Place order with creditcard Redirection")
	public SuccessPage placeOrderUsingCreditCardWithRedirection(){
		clickPlaceOrderBtn();
		waitForElementVisible(redirectPage,30);
		ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
		clickElement(redirectPageSubmitBtn);
		waitForElementVisible(orderSuccessMessage,60);
		return new SuccessPage();
	}

	@Step("Wait for CC Authentication Page")
	public MyAccountPage changePayment() {
		clickPlaceOrderBtn();
		waitForElementVisible(paymentUpdatedAlert,30);
		return new MyAccountPage();
	}

	@Step("Wait for CC Authentication Page")
	public void waitForCCAuthenticationPage() {
		waitForElementVisible(redirectPage,120);
		ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
	}

	@Step("Verify CC Authentication page displayed")
	public SuccessPage verifyCCAuthenticationPageDisplayed(boolean expected) {
		clickPlaceOrderBtn();
		/*waitForPageLoad();*/
		waitForElementVisible(By.cssSelector("form[action^='https://test.saferpay.com/ThreeDSAuthentication/']"),60);
		boolean actual = checkElementExist(redirectPage);
		if(actual) {
			ExtentTestManager.addScreenShot("<b> CC Authentication page </b> ");
			clickElement(redirectPageSubmitBtn);
		}
		verifyAssertEquals(actual, expected,"<b> Creditcard Authentication Page for Enforce 3D Outside Eu Display status: </b>");
		return new SuccessPage();
	}

	@Step("Verify CC iFrame with different inputs")
	public void validateCreditCardForm(){

		clickElementWithJs(creditCard);
		switchToFrame(creditCardiFrame);
		waitForElementVisible(creditCardHolder,30);
		clearText(creditCardHolder);
		switchToDefaultContent();
		clickPlaceOrderBtn();
		sleep(1);
		ExtentTestManager.addScreenShot("CreditCard iFrame");
		boolean ccError = checkElementDisplayed(By.cssSelector("#novalnet_cc_error li"));
		if(ccError)ExtentTestManager.logMessage(Status.PASS,"Novalnet error message is displayed for invalid cards");
		else ExtentTestManager.logMessage(Status.FAIL,"Novalnet error message is not displayed for invalid cards");
		By[] ccInputs = new By[]{creditCardHolder,creditCardNumber,creditCardExp,creditCardCVV};
		switchToFrame(creditCardiFrame);
		for(int i=0;i<ccInputs.length;i++){
			if(getElementAttributeText(ccInputs[i],"class").contains("error")){
				ExtentTestManager.logMessage(Status.PASS,"CreditCard "+ccInputs[i].toString()+" field throws error for empty validation");
			}else{
				ExtentTestManager.logMessage(Status.WARNING,"CreditCard "+ccInputs[i].toString()+" field not throws error for empty validation");
			}
		}
		Map<String,String> validationData = ExcelHelpers.xlReadInputValidationData();
		setText(creditCardHolder,validationData.get("Special"));
		verifyAssertEquals(getElementAttributeText(creditCardHolder,"value"),"&-”’.","Cardholder field special character validation");
		setText(creditCardHolder,validationData.get("Numerical"));
		verifyAssertEquals(getElementAttributeText(creditCardHolder,"value"),"","Cardholder field numerical validation");
		setText(creditCardHolder,validationData.get("Alphabetic"));
		verifyAssertEquals(getElementAttributeText(creditCardHolder,"value"),validationData.get("Alphabetic"),"Cardholder field alphabetic validation");
		setText(creditCardHolder,validationData.get("German"));
		verifyAssertEquals(getElementAttributeText(creditCardHolder,"value"),validationData.get("German"),"Cardholder field German validation");

		setText(creditCardNumber,validationData.get("Special"));
		verifyAssertEquals(getElementAttributeText(creditCardNumber,"value"),"","Card number field special character validation");
		setText(creditCardNumber,validationData.get("Numerical"));
		verifyAssertEquals(getElementAttributeText(creditCardNumber,"value").replaceAll("[^0-9]",""),validationData.get("Numerical"),"Card number field numerical value validation");
		setText(creditCardNumber,"0123456789012345");
		verifyAssertEquals(getElementAttributeText(creditCardNumber,"value").replaceAll("[^0-9]","").length(),16,"Card number field numerical value accepting length validation");
		setText(creditCardNumber,validationData.get("Alphabetic"));
		verifyAssertEquals(getElementAttributeText(creditCardNumber,"value"),"","Card number field alphabetic validation");
		setText(creditCardNumber,validationData.get("German"));
		verifyAssertEquals(getElementAttributeText(creditCardNumber,"value"),"","Card number field German validation");

		setText(creditCardExp,validationData.get("Special"));
		verifyAssertEquals(getElementAttributeText(creditCardExp,"value"),"","Card expiry date field special character validation");
		setText(creditCardExp,"1255");
		verifyAssertEquals(getElementAttributeText(creditCardExp,"value").replaceAll("[^0-9]",""),"1255","Card expiry date field numerical value validation");
		setText(creditCardExp,"01234567890123");
		verifyAssertEquals(getElementAttributeText(creditCardExp,"value").replaceAll("[^0-9]","").length(),4,"Card expiry date field numerical value accepting length validation");
		setText(creditCardExp,validationData.get("Alphabetic"));
		verifyAssertEquals(getElementAttributeText(creditCardExp,"value"),"","Card expiry date field alphabetic validation");
		setText(creditCardExp,validationData.get("German"));
		verifyAssertEquals(getElementAttributeText(creditCardExp,"value"),"","Card expiry date field German validation");

		setText(creditCardCVV,validationData.get("Special"));
		verifyAssertEquals(getElementAttributeText(creditCardCVV,"value"),"","Card cvv field special character validation");
		setText(creditCardCVV,"345");
		verifyAssertEquals(getElementAttributeText(creditCardCVV,"value"),"345","Card cvv field numerical value validation");
		setText(creditCardCVV,"12345");
		verifyAssertEquals(getElementAttributeText(creditCardCVV,"value").length(),4,"Card cvv field numerical value accepting length validation");
		setText(creditCardCVV,validationData.get("Alphabetic"));
		verifyAssertEquals(getElementAttributeText(creditCardCVV,"value"),"","Card cvv field alphabetic validation");
		setText(creditCardCVV,validationData.get("German"));
		verifyAssertEquals(getElementAttributeText(creditCardCVV,"value"),"","Card cvv field German validation");
		// added code for decline CC
		setText(creditCardHolder,"Test User");
		setText(creditCardNumber,"4000 0000 0000 0002");
		setText(creditCardExp,("12/30"));
		setText(creditCardCVV,"123");
		switchToDefaultContent();
		clickPlaceOrderBtn();
		waitForElementVisible(creditCardDecline,30);
		verifyAssertEquals(getElementText(creditCardDecline),"Credit card payment not possible: card expired","Card Decline Error");
		highlightElement(creditCardDecline);
		ExtentTestManager.addScreenShot("Credit card payment not possible: card expired");


	}


	/**
	 * SEPA functions
	 * */

	public void clickSEPA() {
		clickElementWithJs(directDebitSEPA);
	}

	public void fillIBAN(String iban) {
		try{
			setText(sepaIban,iban);
		}catch(StaleElementReferenceException e){
			sleep(2);
			setText(sepaIban,iban);
		}
	}

	/**
	 * iDeal functions
	 * **/

	@Step("Verify iDeal payment displayed")
	public boolean isIDealDisplayed() {
		waitForElementClickable(iDeal,15);
		clickElementWithJs(iDeal);
		ExtentTestManager.addScreenShot("<b>iDeal payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(iDeal);
		ExtentTestManager.logMessage("<b>iDeal payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>iDeal payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify Online Transfer - sofort  payment displayed")
	public boolean isOnlineTransferDisplayed() {
		waitForElementClickable(online_transfer,15);
		clickElementWithJs(online_transfer);
		ExtentTestManager.addScreenShot("<b>Online Transfer -Sofort payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(online_transfer);
		ExtentTestManager.logMessage("<b>Online Transfer - sofort payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Online Transfer - Sofort payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify Online Bank Transfer payment displayed")
	public boolean isOnlineBankTransferDisplayed() {
		waitForElementClickable(online_bank_transfer,15);
		clickElementWithJs(online_bank_transfer);
		ExtentTestManager.addScreenShot("<b>Online Bank Transfer payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(online_bank_transfer);
		ExtentTestManager.logMessage("<b>Online Bank Transfer payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Online Bank Transfer payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify Trustly payment displayed")
	public boolean isTrustlyDisplayed() {
		waitForElementClickable(trustly,15);
		clickElementWithJs(trustly);
		ExtentTestManager.addScreenShot("<b>Trustly payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(trustly);
		ExtentTestManager.logMessage("<b>Trustly payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Trustly payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify Giropay payment displayed")
	public boolean isGiropayDisplayed() {
		waitForElementClickable(giropay,15);
		clickElementWithJs(giropay);
		ExtentTestManager.addScreenShot("<b>Giropay payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(giropay);
		ExtentTestManager.logMessage("<b>Giropay payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Giropay payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify Post Finance Card payment displayed")
	public boolean isPostFinanceDisplayed() {
		waitForElementClickable(postfinance,15);
		clickElementWithJs(postfinance);
		ExtentTestManager.addScreenShot("<b>Post Finance payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(postfinance);
		ExtentTestManager.logMessage("<b>Post Finance payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Post Finance payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify Przelewy24 payment displayed")
	public boolean isPrzelewy24Displayed() {
		waitForElementClickable(przelewy24,15);
		clickElementWithJs(przelewy24);
		ExtentTestManager.addScreenShot("<b>przelewy24 payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(przelewy24);
		ExtentTestManager.logMessage("<b>przelewy24 payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>przelewy24 payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify eps  payment displayed")
	public boolean isEPSDisplayed() {
		waitForElementClickable(eps,15);
		clickElementWithJs(eps);
		ExtentTestManager.addScreenShot("<b>eps payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(eps);
		ExtentTestManager.logMessage("<b>eps payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>eps payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}


	@Step("Verify Bancontact  payment displayed")
	public boolean isBancontactDisplayed() {
		waitForElementClickable(banContact,15);
		clickElementWithJs(banContact);
		ExtentTestManager.addScreenShot("<b>Ban contact payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(banContact);
		ExtentTestManager.logMessage("<b>Ban contact payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Ban Contact payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify Alipay  payment displayed")
	public boolean isAlipayDisplayed() {
		waitForElementClickable(alipay,15);
		clickElementWithJs(alipay);
		ExtentTestManager.addScreenShot("<b>Alipay payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(alipay);
		ExtentTestManager.logMessage("<b>Alipay payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Alipay payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify WeChatPay  payment displayed")
	public boolean isWeChatPayDisplayed() {
		waitForElementClickable(weChatPay,15);
		clickElementWithJs(weChatPay);
		ExtentTestManager.addScreenShot("<b>WeChatPay payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(weChatPay);
		ExtentTestManager.logMessage("<b>WeChatPay payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>WeChatPay payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify Multibanco  payment displayed")
	public boolean isMultibancoDisplayed() {
		waitForElementClickable(multibanco,15);
		clickElementWithJs(multibanco);
		ExtentTestManager.addScreenShot("<b>Multibanco contact payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(multibanco);
		ExtentTestManager.logMessage("<b>Multibanco contact payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Multibanco payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify PayPal payment displayed")
	public boolean isPayPalDisplayed() {
		waitForElementClickable(paypal,15);
		clickElementWithJs(paypal);
		ExtentTestManager.addScreenShot("<b>PayPal payment in the checkout page Screenshot</b>");
		var status = checkElementDisplayed(paypal);
		ExtentTestManager.logMessage("<b>PayPal payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		if(!status)
			Assert.fail("<b>Paypal payment status: </b>"+(status ? "Displayed" : "Not Displayed"));
		return true;
	}

	@Step("Verify iDeal test mode displayed")
	public void verifyIDealTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(iDealTestMode);
		if(actual)
			highlightElement(iDealTestMode);
		verifyAssertEquals(actual, expected,"<b> iDeal Test Mode status: </b>");
	}

	@Step("Verify Online Transfer - Sofort test mode displayed")
	public void verifyOnlineTransferTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(onlineTransferTestMode);
		if(actual)
			highlightElement(onlineTransferTestMode);
		verifyAssertEquals(actual, expected,"<b> Online Transfer - sofort Test Mode status: </b>");
	}

	@Step("Verify Online Bank Transfer test mode displayed")
	public void verifyOnlineBankTransferTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(onlineBankTransferTestMode);
		if(actual)
			highlightElement(onlineBankTransferTestMode);
		verifyAssertEquals(actual, expected,"<b> Online Bank Transfer Test Mode status: </b>");
	}

	@Step("Verify Trustly test mode displayed")
	public void verifyTrustlyTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(trustlyTestMode);
		if(actual)
			highlightElement(trustlyTestMode);
		verifyAssertEquals(actual, expected,"<b> Trustly Test Mode status: </b>");
	}

	@Step("Verify Giropay test mode displayed")
	public void verifyGiropayTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(giropayTestMode);
		if(actual)
			highlightElement(giropayTestMode);
		verifyAssertEquals(actual, expected,"<b> Giropay Test Mode status: </b>");
	}

	@Step("Verify Post Finance test mode displayed")
	public void verifyPostFinanceTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(postFinanceTestMode);
		if(actual)
			highlightElement(postFinanceTestMode);
		verifyAssertEquals(actual, expected,"<b> Post Finance Test Mode status: </b>");
	}

	@Step("Verify przelewy24 test mode displayed")
	public void verifyPrzelewy24TestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(przelewy24TestMode);
		if(actual)
			highlightElement(przelewy24TestMode);
		verifyAssertEquals(actual, expected,"<b> przelewy24 Test Mode status: </b>");
	}

	@Step("Verify eps test mode displayed")
	public void verifyEPSTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(epsTestMode);
		if(actual)
			highlightElement(epsTestMode);
		verifyAssertEquals(actual, expected,"<b> eps Test Mode status: </b>");
	}

	@Step("Verify Ban Contact test mode displayed")
	public void verifyBancontactTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(banContactTestMode);
		if(actual)
			highlightElement(banContactTestMode);
		verifyAssertEquals(actual, expected,"<b> Ban Contact Test Mode status: </b>");
	}

	@Step("Verify Alipay test mode displayed")
	public void verifyalipayTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(alipayTestMode);
		if(actual)
			highlightElement(alipayTestMode);
		verifyAssertEquals(actual, expected,"<b> Alipay Contact Test Mode status: </b>");
	}

	@Step("Verify WeChatPay test mode displayed")
	public void verifyWeChatPayTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(weChatPayTestMode);
		if(actual)
			highlightElement(weChatPayTestMode);
		verifyAssertEquals(actual, expected,"<b> Alipay Contact Test Mode status: </b>");
	}

	@Step("Verify Multibanco test mode displayed")
	public void verifyMultibancoTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(multiBancoTestMode);
		if(actual)
			highlightElement(multiBancoTestMode);
		verifyAssertEquals(actual, expected,"<b> Multibanco Test Mode status: </b>");
	}

	@Step("Verify PayPal test mode displayed")
	public void verifyPayPalTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(paypalTestMode);
		if(actual)
			highlightElement(paypalTestMode);
		verifyAssertEquals(actual, expected,"<b> Paypal Test Mode status: </b>");
	}


	@Step("Place order with iDeal")
	public SuccessPage placeOrderWithIDeal() {
		clickPlaceOrderBtn();
		waitForElementVisible(nextBtn,30);
		clickElement(nextBtn);
		clickElement(loginBtn);
		clickElement(makePaymentBtn);
		clickElement(backToShopBtn);
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with Alipay")
	public SuccessPage placeOrderWithAlipay() {
		clickPlaceOrderBtn();
		waitForElementVisible(loginBtn,30);
		clickElement(loginBtn);
		clickElement(makePaymentBtn);
		clickElement(backToShopBtn);
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with WeChatPay")
	public SuccessPage placeOrderWithWeChatPay() {
		clickPlaceOrderBtn();
		waitForElementVisible(loginBtn,30);
		clickElement(loginBtn);
		clickElement(makePaymentBtn);
		clickElement(backToShopBtn);
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with BanContact")
	public SuccessPage placeOrderWithBanContact() {
		clickPlaceOrderBtn();
		waitForElementVisible(makePaymentBtn,30);
		clickElement(makePaymentBtn);
		clickElement(backToShopBtn);
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with Multibanco")
	public SuccessPage placeOrderWithMultibanco() {
		clickPlaceOrderBtn();
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with Online Transfer - Sofort")
	public SuccessPage placeOrderWithOnlineTransfer() {
		clickPlaceOrderBtn();
		rejectSofortBankCookiePopup();
		enterSofortBankDetails();
		enterSofortTANDetails();
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with Online Bank Transfer ")
	public SuccessPage placeOrderWithOnlineBankTransfer() {
		clickPlaceOrderBtn();
		enterOnlineBankTransferDetails();
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with Trustly ")
	public SuccessPage placeOrderWithTrustly() {
		clickPlaceOrderBtn();
		//enterTrustlyBankTransferDetails();
		enterNewTrustlyBankTransferDetails();
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with Giropay ")
	public SuccessPage placeOrderWithGiropay() {
		clickPlaceOrderBtn();
		//enterGiropayBankDetails();//commented reason is giropay UI totally changed
		enterNewGiropayBankDetails();
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with Post Finance ")
	public SuccessPage placeOrderWithPostFinance() {
		clickPlaceOrderBtn();
		enterPostFinanceCardDetails();
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with przelewy24 ")
	public SuccessPage placeOrderWithPrzelewy24() {
		clickPlaceOrderBtn();
		enterPrzelewy24BankDetails();
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Place order with eps ")
	public SuccessPage placeOrderWithEPS() {
		clickPlaceOrderBtn();
		//enterEPSankDetails();//Commented reason is eps redirect page UI totally changed
		enterNewEPSBankDetails();
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Enter Online Bank Transfer Details ")
	public void  enterOnlineBankTransferDetails() {
		waitForURLToBe("https://link.tink.com/", 120);
		String userName = "#username";
		String pass = "#password";
		String continueBtn = "button.MuiButton-sizeLarge>span.MuiButton-label";

		setText(By.cssSelector(userName),"u83188312");
		setText(By.cssSelector(pass),"zhx571");
		clickElementWithJs(By.cssSelector(continueBtn));
		clickElementWithJs(By.xpath("//*[text()='2FA Decoupled']"));
		waitForElementVisible(By.xpath("//button[@type='submit']"));
		clickElementWithJs(By.xpath("//button[@type='submit']"));
		waitForElementVisible(By.xpath("//*[contains(text(),'The current otp is')]"));
		String input = getElementText(By.xpath("//*[contains(text(),'The current otp is')]"));
		Pattern pattern = Pattern.compile("\\b\\d{4}\\b");
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {

			String otp = matcher.group();
			setText(By.cssSelector("#otpinput"), otp);
		} else {
			Log.info("No OTP found in the input string");

		}
		clickElementWithJs(By.xpath("//button[@type='submit']"));

	}



	@Step("Enter Online Bank Transfer Details ")
	public void  enterTrustlyBankTransferDetails() {
		waitForURLToBe("https://checkout.test.trustly.com/");
		String clickPayWithTrustly="//span[@data-testid='primary-button-body' and contains(text(), 'Pay with Trustly')]";
		String demoBankRedirect="//span[text()='Commerzbank']";
		String continueBtn1="//span[@class='MuiButton-label' and text()='Continue']";
		String continueBtn2="//span[text()='Continue']";
		String continueBtn3="//button[@data-testid='continue-button']";
		String paymentBtn="//span[@data-testid='primary-button-body' and contains(text(), 'Confirm payment')]";
		String login="//input[@data-testid='Input-text-loginid']";
		String passCode="//input[@data-testid='Input-password-challenge_response']";
		String otp="//h3[contains(@class, 'sc-dlnjwi')]";
		String checkingAccountBtn="//span[contains(@data-testid, 'headline') and text()='Checking account']";
		waitForElementVisible(By.xpath(clickPayWithTrustly));
		sleep(2);
		waitForElementClickable(DriverManager.getDriver().findElement(By.xpath(clickPayWithTrustly)));
		clickElementWithJs(By.xpath(clickPayWithTrustly));
		waitForElementVisible(By.xpath(demoBankRedirect));
		clickElementWithJs(By.xpath(demoBankRedirect));
		sleep(2);
		waitForElementVisible(By.xpath(continueBtn2));
		clickElementWithJs(By.xpath(continueBtn2));
		sleep(5);
		waitForElementVisible(By.xpath(login));
		clickElement(By.xpath(login));
		sleep(2);
		setText(By.xpath(login),"idabarese456");
		clickElement(By.xpath(continueBtn3));
		sleep(2);
		waitForElementVisible(By.xpath(otp));
		clickElement(By.xpath(passCode));
		sleep(2);
		setText(By.xpath(passCode),getElementText(By.xpath(otp)));
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
		setText(By.xpath(passCode),getElementText(By.xpath(otp)));
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

	@Step("Enter GiroPay Bank Transfer Details ")
	public void  enterGiropayBankDetails() {
		waitForURLToBe("https://ftg-customer-integration.giropay.de/");
		String submitBtn1="//input[@type='submit' and @value='Jetzt bezahlen']";
		String submitBtn2="//input[@name='weiterButton' and @type='submit' and @value='Weiter']";
		String submitBtn3="//input[@type='submit' and @value='Login']";
		String submitBtn4="//input[@type='submit' and @value='Weiter']";
		String submitBtn5="//input[@type='submit' and @value='Jetzt bezahlen']";
		String logIn="//input[@name=\"account/addition[@name=benutzerkennung]\"]";
		String pin="//input[@name='ticket/pin']";
		String tan="//input[@name='ticket/tan']";
		String continueBtn = "//input[@name='continueBtn' and @type='button' and @value='Weiter zum Bezahlen' and contains(@class,'blueButton')]";
		if (checkElementDisplayed(By.xpath(continueBtn))) {
			String bankName = "//input[@id='tags' and @type='text' and contains(@class,'ui-autocomplete-input')]";
			String acceptBic = "//div[@id='layerBic']//button[@id='yes' and @type='button' and contains(@class,'btn') and contains(@class,'btn-primary') and @data-value='1' and contains(text(),'Annehmen')]";
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
		waitForElementVisible(By.xpath(submitBtn1));
		setText(By.xpath(logIn),"chiptanscatest4");
		setText(By.xpath(pin),"12345");
		clickElementWithJs(By.xpath(submitBtn1));
		clickElementWithJs(By.xpath(submitBtn2));
		setText(By.xpath(tan),"123456");
		clickElementWithJs(By.xpath(submitBtn3));
		clickElementWithJs(By.xpath(submitBtn4));
		setText(By.xpath(tan),"123456");
		clickElementWithJs(By.xpath(submitBtn5));

	}
	@Step("Enter new GiroPay Bank Transfer Details ")
	public void enterNewGiropayBankDetails() {
		waitForURLToBe("https://sandbox.paydirekt.de/checkout/",60);
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


	@Step("Enter Post Finance Card Payment Details ")
	public void  enterPostFinanceCardDetails() {
		waitForURLToBe("https://epayment-t2.postfinance.ch/");
		String submitBtn="//button[@type='submit' and contains(@class, 'fpui-button--primary') and contains(text(), 'Next')]";
		String card="//img[@class='card-select-option--image' and @alt='Card Icon']";
		String id="//input[@formcontrolname='cardId' and @name='cardId' and @id='cardId']";
		String cardNumber="//input[@formcontrolname='cardNumber' and @name='cardNumber' and @id='cardNumber' and @type='text']";
		String otp="//input[@formcontrolname='otpToken']";
		String otpText="//p[@class='leading-snug text-xl sm:text-3xl']";

		waitForElementVisible(By.xpath(card));
		clickElementWithJs(By.xpath(card));
		clickElementWithJs(By.xpath(id));
		setText(By.xpath(id),"129 026 394 145");
		clickElementWithJs(By.xpath(submitBtn));
		sleep(2);
		if(checkElementDisplayed(By.xpath(cardNumber))) {
			clickElementWithJs(By.xpath(cardNumber));
			setText(By.xpath(cardNumber),"69968016");
		}
		if(checkElementDisplayed(By.xpath(otp))) {
			clickElementWithJs(By.xpath(otp));
			setText(By.xpath(otp),getElementText(By.xpath(otpText)));
		}

		clickElementWithJs(By.xpath(submitBtn));
	}

	@Step("Enter przelewy24 Bank Transfer Details ")
	public void  enterPrzelewy24BankDetails() {
		waitForURLToBe("https://sandbox-go.przelewy24.pl/");

	}

	@Step("Enter eps Bank Transfer Details ")
	public void  enterEPSankDetails() {
		waitForURLToBe("https://ftg-customer-integration.giropay.de/");
		String continueBtn = "//input[@name='continueBtn' and @type='button' and @value='Weiter zum Bezahlen' and contains(@class,'blueButton')]";
		if (checkElementDisplayed(By.xpath(continueBtn))) {
			String bankName = "//input[@id='tags' and @type='text' and contains(@class,'ui-autocomplete-input')]";
			String acceptBic = "//div[@id='layerBic']//button[@id='yes' and @type='button' and contains(@class,'btn') and contains(@class,'btn-primary') and @data-value='1' and contains(text(),'Annehmen')]";
			String login = "//input[@id='sbtnLogin']";
			String signIn1 = "//input[@id='sbtnSign']";
			String signIn2 = "//input[@id='sbtnSignSingle']";
			String okBtn="//input[@id='sbtnOk']";
			String submitBtn="//input[@name='back2Shop']";
			setText(By.xpath(bankName), "HYPTAT22XXX");
			sleep(1.5);
			waitForElementVisible(By.cssSelector(".ui-menu-item"));
			setTextWithAction(By.xpath(bankName), Keys.ARROW_DOWN);
			pressEnter(By.xpath(bankName));
			clickElementWithJs(By.xpath(continueBtn));
			waitForElementClickable(By.xpath(acceptBic));
			clickElementWithJs(By.xpath(acceptBic));
			waitForElementClickable(By.xpath(login));
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
	@Step("Enter new eps Bank Transfer Details ")
	public void  enterNewEPSBankDetails() {
		waitForURLToBe("https://sandbox.paydirekt.de/eps-checkout",120);
		By epsCookie = By.xpath("//button[contains(text(), 'Alle Cookies akzeptieren')]");
		if(checkElementDisplayed(epsCookie)){
			clickElement(epsCookie);
			setText(By.cssSelector("#bank_search"),"HYPTAT22XXX");
			clickElement(By.xpath("//div[contains(text(),'Hypo Tirol AG')]"));
			clickElement(By.xpath("//button[contains(text(), 'Weiter zum Bezahlen')]"));
			clickElement(By.cssSelector("#confirmButton"));
		}else if(waitForElementVisible(By.cssSelector("#bank_search"),5,"")){
			setText(By.cssSelector("#bank_search"),"HYPTAT22XXX");
			clickElement(By.xpath("//div[contains(text(),'Hypo Tirol AG')]"));
			clickElement(By.xpath("//button[contains(text(), 'Weiter zum Bezahlen')]"));
			clickElement(By.cssSelector("#confirmButton"));

		}else{
			clickElement(By.xpath("//button[contains(text(), 'Weiter zum Bezahlen')]"));
			clickElement(By.cssSelector("#confirmButton"));
		}
	}


	@Step("Close Sofort Bank Cookie popup")
	public void rejectSofortBankCookiePopup() {
		waitForURLToBe("https://www.sofort.com/");
		if(waitForElementVisible(sofortCookieDenyAlert,5,"Waiting for sofort cookie")){
			clickElementWithJs(sofortCookieDenyAlert);
		}
		clickElementWithJs(sofortDemoBank);
	}

	@Step("Enter Sofort Bank Details")
	public void enterSofortBankDetails() {
		waitForElementVisible(sofortBankLoginButton);
		setTextByRefreshing(By.xpath("//input[@name='data[BackendForm][LOGINNAME__USER_ID]' and @id='BackendFormLOGINNAMEUSERID']"),"1111");
		setTextByRefreshing(By.xpath("//input[@id='BackendFormUSERPIN' and @type='password']"),"1234");
		clickElementByRefreshing(By.xpath("//button[@class='button-right primary has-indicator' and @data-skip-leave-alert='true']"));
	}

	@Step("Enter Sofort TAN Details")
	public void enterSofortTANDetails() {
		waitForElementVisible(sofortBankLoginButton);
		clickElementByRefreshing(By.xpath("//button[@class='button-right primary has-indicator' and @data-skip-leave-alert='true']"));
		setTextByRefreshing(By.xpath("//input[@name='data[BackendForm][tan]' and @class='encrypted' and @id='BackendFormTan']"),"12345");
		clickElementByRefreshing(By.xpath("//button[contains(@class, 'button-right') and contains(@class, 'primary') and contains(@class, 'has-indicator') and @data-skip-leave-alert='true']"));
	}

	@Step("Place order with PayPal")
	public SuccessPage placeOrderWithPayPal() {
		clickPlaceOrderBtn();
		logintoPayPalandCompletePayment();
		waitForElementVisible(orderSuccessMessage,30);
		return new SuccessPage();
	}

	@Step("Verify is iDeal payment redirected")
	public void waitForIDealRedirectionPage() {
		clickPlaceOrderBtn();
		waitForElementVisible(nextBtn,30);
	}

	@Step("Verify is Sofort payment redirected")
	public void waitForSofortRedirectionPage() {
		waitForURLToBe("https://www.sofort.com/");
	}


	@Step("Verify is Online Bank Transfer payment redirected")
	public void waitForOnlineBankTransferRedirectionPage() {
		waitForURLToBe("https://link.tink.com/", 120);
	}

	@Step("Verify is Trustly payment redirected")
	public void waitForTrustlyRedirectionPage() {
		waitForURLToBe("https://checkout.test.trustly.com/");
	}

	@Step("Verify is GiroPay payment redirected")
	public void waitForGiroPayRedirectionPage() {
		waitForURLToBe("https://sandbox.paydirekt.de/checkout/", 60);
	}

	@Step("Verify is Post Finance payment redirected")
	public void waitForPostFinanceRedirectionPage() {
		waitForURLToBe("https://epayment-t2.postfinance.ch/");
	}

	@Step("Verify is eps payment redirected")
	public void waitForEPSPayRedirectionPage() {
		waitForURLToBe("https://sandbox.paydirekt.de/eps-checkout", 120);
	}

	@Step("Verify is Bancontact payment redirected")
	public void waitForBancontactPayRedirectionPage() {
		waitForURLToBe("https://r3.girogate.de/");
	}

	@Step("Verify is Alipay payment redirected")
	public void waitForAliPayRedirectionPage() {
		waitForURLToBe("https://r2.girogate.de/alipay/");
	}

	@Step("Verify is WeChatPay payment redirected")
	public void waitForWeChatPayRedirectionPage() {
		waitForURLToBe("https://r2.girogate.de/");
	}

	@Step("Verify is MultiBanco payment redirected")
	public void waitForMultibancoPayRedirectionPage() {
		waitForElementVisible(orderSuccessMessage,30);
	}

	public void abortIDealPayment() {
		clickPlaceOrderBtn();
		waitForElementVisible(nextBtn,30);
		clickElement(By.cssSelector(".btn-secondary"));
		waitForElementVisible(woocommerceError,20);
	}

	/***
	 *
	 * Prepayment functions
	 *
	 * **/

	@Step("Verify Prepayment displayed")
	public boolean isPrepaymentDisplayed() {
		boolean displayed = checkElementDisplayed(prepayment);
		if(displayed) {
			waitForElementClickable(prepayment,15);
			clickElementWithJs(prepayment);
			ExtentTestManager.addScreenShot("<b>Prepayment in the checkout page Screenshot</b>");
		}else {
			scrollToElement(paymentDiv);
			ExtentTestManager.logMessage(Status.FAIL,"<b>Prepayment is not displayed in the checkout page.");
			Assert.fail("Prepayment is not displayed in the checkout page.");
		}
		return displayed;
	}


	@Step("Verify OnCashpayment displayed")
	public boolean isOnCashPaymentDisplayed() {
		boolean displayed = checkElementDisplayed(onCashPayment);
		if(displayed) {
			waitForElementClickable(onCashPayment,15);
			clickElementWithJs(onCashPayment);
			ExtentTestManager.addScreenShot("<b>On Cash Payment in the checkout page Screenshot</b>");
		}else {
			scrollToElement(paymentDiv);
			ExtentTestManager.logMessage(Status.FAIL,"<b>On Cash Payment is not displayed in the checkout page.");
			Assert.fail("On Cash Payment is not displayed in the checkout page.");
		}
		return displayed;
	}


	@Step("Verify Prepayment test mode displayed")
	public void verifyPrepaymentTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(prepaymentTestMode);
		if(actual)
			highlightElement(prepaymentTestMode);
		verifyAssertEquals(actual, expected,"<b> Prepayment Test Mode status: </b>");
	}

	@Step("Verify On Cash Payment test mode displayed")
	public void verifyOnCashPaymentTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(OnCashPaymentTestMode);
		if(actual)
			highlightElement(OnCashPaymentTestMode);
		verifyAssertEquals(actual, expected,"<b> On Cash Payment Test Mode status: </b>");
	}

	/****
	 *
	 * Invoice functions
	 *
	 * **/

	@Step("Verify Invoice displayed")
	public boolean isInvoiceDisplayed() {
		boolean displayed = checkElementDisplayed(invoice);
		if(displayed) {
			waitForElementClickable(invoice,15);
			clickElementWithJs(invoice);
			waitForPaymentDivToLoad(invoice);
			ExtentTestManager.addScreenShot("<b>Invoice in the checkout page Screenshot</b>");
		}else {
			scrollToElement(paymentDiv);
			ExtentTestManager.logMessage(Status.FAIL,"<b>Invoice is not displayed in the checkout page.");
			Assert.fail("Invoice is not displayed in the checkout page.");
		}
		return displayed;
	}

	@Step("Verify Invoice test mode displayed")
	public void verifyInvoiceTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(invoiceTestMode);
		if(actual)
			highlightElement(invoiceTestMode);
		verifyAssertEquals(actual, expected,"<b> Invoice Test Mode status: </b>");
	}

	/***
	 *
	 * Direct Debit SEPA functions
	 * ***/

	@Step("Verify Direct Debit SEPA displayed")
	public boolean isSepaDisplayed() {
		boolean displayed = checkElementDisplayed(directDebitSEPA);
		if(displayed) {
			waitForElementClickable(directDebitSEPA,15);
			clickElementWithJs(directDebitSEPA);
			waitForPaymentDivToLoad(directDebitSEPA);
			ExtentTestManager.addScreenShot("<b>Direct Debit SEPA in the checkout page Screenshot</b>");
		}else {
			scrollToElement(paymentDiv);
			ExtentTestManager.logMessage(Status.FAIL,"<b>Direct Debit SEPA is not displayed in the checkout page.");
			Assert.fail("Direct Debit SEPA is not displayed in the checkout page.");
		}
		return displayed;
	}

	@Step("Verify Direct Debit SEPA test mode displayed")
	public void verifySepaTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(sepaTestMode);
		if(actual)
			highlightElement(sepaTestMode);
		verifyAssertEquals(actual, expected,"<b> Direct Debit SEPA Test Mode status: </b>");
	}

	@Step("Verify one click save card for checkbox")
	public void verifySepaSaveCardCheckboxChecked(boolean expected) {
		boolean displayed = checkElementDisplayed(sepaSaveCardCheckbox);
		boolean checked =false;
		verifyAssertEquals(displayed, expected,"<b>SEPA Save for future checkbox display status: </b>");
		if(displayed) {
			checked =checkElementChecked(sepaSaveCardCheckbox);
			verifyAssertEquals(checked, expected,"<b>SEPA Save for future checkbox enabled status: </b>");
		}
	}

	@Step("Verify SEPA payment token displayed and checked")
	public void verifySepaTokenDisplayedAndChecked(boolean expected) {
		boolean displayed = checkElementDisplayed(sepaOneClickToken);
		boolean checked =false;
		if(displayed) {
			checked =checkElementChecked(sepaOneClickToken);
			verifyAssertEquals(checked, expected,"<b>Direct Debit SEPA payment Token checked status: </b>");
		}
		verifyAssertEquals(displayed, expected,"<b>Direct Debit SEPA payment Token display status: </b>");
	}

	@Step("Click use new payment method checkbox SEPA")
	public void clickUseNewPaymentMethodSEPA() {
		if(!checkElementChecked(sepaUseNewPaymentCheckbox))
			clickElementWithJs(sepaUseNewPaymentCheckbox);
	}

	/***
	 * Guarantee SEPA functions
	 * ***/

	@Step("Verify Direct Debit SEPA  with payment guarantee displayed")
	public boolean isSepaGuaranteeDisplayed() {
		boolean displayed = checkElementDisplayed(sepaGuarantee);
		if(displayed) {
			waitForElementClickable(sepaGuarantee,15);
			clickElementWithJs(sepaGuarantee);
			sleep(2);
			ExtentTestManager.addScreenShot("<b>Direct Debit SEPA  with payment guarantee in the checkout page Screenshot</b>");
		}else {
			scrollToElement(paymentDiv);
			ExtentTestManager.logMessage(Status.FAIL,"<b>Direct Debit SEPA  with payment guarantee is not displayed in the checkout page.");
			Assert.fail("Direct Debit SEPA  with payment guarantee is not displayed in the checkout page.");
		}
		return displayed;
	}

	@Step("Verify Direct Debit SEPA with payment guarantee test mode displayed")
	public void verifySepaGuaranteeTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(sepaGuaranteeTestMode);
		if(actual)
			highlightElement(sepaGuaranteeTestMode);
		verifyAssertEquals(actual, expected,"<b> Direct Debit SEPA with payment guarantee Test Mode status: </b>");
	}

	@Step("Verify one click save card for checkbox")
	public void verifySepaGuaranteeSaveCardCheckboxChecked(boolean expected) {
		boolean displayed = checkElementDisplayed(sepaGuaranteeSaveCardCheckbox);
		boolean checked =false;
		verifyAssertEquals(displayed, expected,"<b>SEPA Guarantee Save for future checkbox display status: </b>");
		if(displayed) {
			checked =checkElementChecked(sepaGuaranteeSaveCardCheckbox);
			verifyAssertEquals(checked, expected,"<b>SEPA Guarantee Save for future checkbox enabled status: </b>");
		}
	}

	@Step("Verify SEPA payment token displayed and checked")
	public void verifySepaGuaranteeTokenDisplayedAndChecked(boolean expected) {
		boolean displayed = checkElementDisplayed(sepaGuaranteeOneClickToken);
		boolean checked =false;
		if(displayed) {
			checked =checkElementChecked(sepaGuaranteeOneClickToken);
			verifyAssertEquals(checked, expected,"<b>Direct Debit SEPA with payment guarantee payment Token checked status: </b>");
		}
		verifyAssertEquals(displayed, expected,"<b>Direct Debit SEPA with payment guarantee payment Token display status: </b>");
	}


	@Step("Click use new payment method checkbox SEPA Guarantee")
	public void clickUseNewPaymentMethodSEPAGuarantee() {
		if(!checkElementChecked(sepaGuaranteeUseNewPaymentCheckbox))
			try {
				clickElementWithJs(sepaGuaranteeUseNewPaymentCheckbox);
			}catch(StaleElementReferenceException e){
				sleep(2);
				clickElementWithJs(sepaGuaranteeUseNewPaymentCheckbox);
			}
	}


	@Step("Enter IBAN for SEPA Guarantee")
	public void fillSepaGuaranteeIBAN(String iban){
		try{
			setText(sepaGuaranteeIban,iban);
		}catch(StaleElementReferenceException e){
			sleep(2);
			setText(sepaGuaranteeIban,iban);
		}
	}

	@Step("Enter Date of Birth for SEPA Guarantee")
	public void fillSepaGuaranteeDOB(String birthDate){
		setText(sepaGuaranteeDOB,birthDate);
	}

	@Step("Verify Date of Birth for SEPA Guarantee displayed")
	public void verifySepaGuaranteeDateOfBirthIsDisplayed(boolean expected){
		verifyAssertEquals(checkElementDisplayed(sepaGuaranteeDOB), expected,"<b>Direct Debit SEPA with payment guarantee date of birth display status: </b>");
	}


	/***
	 * Instalment SEPA functions
	 * ***/

	@Step("Verify Instalment Direct Debit SEPA displayed")
	public boolean isInstalmentSepaDisplayed() {
		boolean displayed = checkElementDisplayed(instalmentSepa);
		if(displayed) {
			waitForElementClickable(instalmentSepa,15);
			clickElementWithJs(instalmentSepa);
			waitForPaymentDivToLoad(instalmentSepa);
			ExtentTestManager.addScreenShot("<b>Instalment Direct Debit SEPA in the checkout page Screenshot</b>");
		}else {
			scrollToElement(paymentDiv);
			ExtentTestManager.logMessage(Status.FAIL,"<b>Instalment Direct Debit SEPA is not displayed in the checkout page.");
			Assert.fail("Instalment Direct Debit SEPA is not displayed in the checkout page.");
		}
		return displayed;
	}

	@Step("Verify Instalment Direct Debit SEPA test mode displayed")
	public void verifyInstalmentSepaTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(instalmentSepaTestMode);
		if(actual)
			highlightElement(instalmentSepaTestMode);
		verifyAssertEquals(actual, expected,"<b> Instalment Direct Debit SEPA Test Mode status: </b>");
	}

	@Step("Verify one click save card for checkbox")
	public void verifyInstalmentSepaSaveCardCheckboxChecked(boolean expected) {
		boolean displayed = checkElementDisplayed(instalmentSepaSaveCardCheckbox);
		boolean checked =false;
		verifyAssertEquals(displayed, expected,"<b>Instalment Direct Debit SEPA Save for future checkbox display status: </b>");
		if(displayed) {
			checked =checkElementChecked(instalmentSepaSaveCardCheckbox);
			verifyAssertEquals(checked, expected,"<b>Instalment Direct Debit SEPA Save for future checkbox enabled status: </b>");
		}
	}

	@Step("Verify Instalment Direct Debit SEPA payment token displayed and checked")
	public void verifyInstalmentSepaTokenDisplayedAndChecked(boolean expected) {
		boolean displayed = checkElementDisplayed(instalmentSepaOneClickToken);
		boolean checked =false;
		if(displayed) {
			checked =checkElementChecked(instalmentSepaOneClickToken);
			verifyAssertEquals(checked, expected,"<b>Instalment Direct Debit SEPA payment Token checked status: </b>");
		}
		verifyAssertEquals(displayed, expected,"<b>Instalment Direct Debit SEPA Token display status: </b>");
	}


	@Step("Click use new payment method checkbox Instalment Direct Debit SEPA")
	public void clickUseNewPaymentMethodInstalmentSepa() {
		if(!checkElementChecked(instalmentSepaUseNewPaymentCheckbox))
			clickElementWithJs(instalmentSepaUseNewPaymentCheckbox);
	}


	@Step("Enter IBAN for Instalment Direct Debit SEPA")
	public void fillInstalmentSepaIBAN(String iban){
		try {
			waitForStaleness(instalmentSepaDOB);
		}catch(Exception e){e.printStackTrace();}
		try{
			setText(instalmentSepaIban,iban);
		}catch(StaleElementReferenceException e){
			sleep(2);
			setText(instalmentSepaIban,iban);
		}
	}

	@Step("Enter Date of Birth for Instalment Direct Debit SEPA")
	public void fillInstalmentSepaDOB(String birthDate){
		setText(instalmentSepaDOB,birthDate);
	}

	@Step("Verify Date of Birth for Instalment Direct Debit SEPA displayed")
	public void verifyInstalmentSepaDateOfBirthIsDisplayed(boolean expected){
		try {
			waitForStaleness(instalmentSepaDOB);
		}catch(Exception e){e.printStackTrace();}
		verifyAssertEquals(checkElementDisplayed(instalmentSepaDOB), expected,"<b>Instalment Direct Debit SEPA date of birth display status: </b>");
	}

	@Step("Select Instalment SEPA cycles")
	public String selectInstalmentCyclesForSEPA(String numberOfCycles){
		try {
			selectDropdownByValue(By.cssSelector("#novalnet_instalment_sepa_period"), numberOfCycles);
		}catch(StaleElementReferenceException e){sleep(2);
			selectDropdownByValue(By.cssSelector("#novalnet_instalment_sepa_period"), numberOfCycles);
		}
		var cycleAmount = getDropdownSelectedOptionText(By.cssSelector("#novalnet_instalment_sepa_period"));
		Log.info("Installment cycle Amount =" + cycleAmount );
		return cycleAmount.split("x")[1].replaceAll("[^0-9]","");
	}

	@Step("Verify Selected Instalment SEPA cycles {0} displayed")
	public void verifySelectedInstalmentCyclesDisplayedSEPA(String numberOfCycles){
		String css = ".payment_method_novalnet_instalment_sepa [id^='novalnet_instalment_sepa_table_"+numberOfCycles+"']";
		verifyAssertEquals(checkElementDisplayed(By.cssSelector(css)), true,"<b>Instalment SEPA instalment selected cycles at checkout display status: </b>");
	}


	/***
	 * Guarantee Invoice functions
	 * ***/

	@Step("Verify Invoice with payment guarantee displayed")
	public boolean isInvoiceGuaranteeDisplayed() {
		boolean displayed = checkElementDisplayed(invoiceGuarantee);
		if(displayed) {
			waitForElementClickable(invoiceGuarantee,15);
			clickElementWithJs(invoiceGuarantee);
			sleep(2);
			ExtentTestManager.addScreenShot("<b>Invoice  with payment guarantee in the checkout page Screenshot</b>");
		}else {
			scrollToElement(paymentDiv);
			ExtentTestManager.logMessage(Status.FAIL,"<b>Invoice  with payment guarantee is not displayed in the checkout page.");
			Assert.fail("Invoice  with payment guarantee is not displayed in the checkout page.");
		}
		return displayed;
	}

	@Step("Verify Invoice with payment guarantee test mode displayed")
	public void verifyInvoiceGuaranteeTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(invoiceGuaranteeTestMode);
		if(actual)
			highlightElement(invoiceGuaranteeTestMode);
		verifyAssertEquals(actual, expected,"<b> Invoice with payment guarantee Test Mode status: </b>");
	}

	@Step("Enter Date of Birth for Invoice Guarantee")
	public void fillInvoiceGuaranteeDOB(String birthDate){
		setText(invoiceGuaranteeDOB,birthDate);
	}

	@Step("Verify Date of Birth for Invoice Guarantee displayed")
	public void verifyInvoiceGuaranteeDateOfBirthIsDisplayed(boolean expected){
		try {
			waitForStaleness(invoiceGuaranteeDOB);
		}catch(Exception e){e.printStackTrace();}

		if(expected==false){
			waitForElementDisable(invoiceGuaranteeDOB);
		}
		verifyAssertEquals(checkElementDisplayed(invoiceGuaranteeDOB), expected,"<b>Invoice with payment guarantee date of birth display status: </b>");
	}

	/***
	 * Instalment Invoice functions
	 * ***/

	@Step("Verify Instalment Invoice displayed")
	public boolean isInstalmentInvoiceDisplayed() {
		boolean displayed = checkElementDisplayed(instalmentInvoice);
		if(displayed) {
			waitForElementClickable(instalmentInvoice,15);
			clickElementWithJs(instalmentInvoice);
			waitForPaymentDivToLoad(instalmentInvoice);
			ExtentTestManager.addScreenShot("<b>Instalment Invoice in the checkout page Screenshot</b>");
		}else {
			scrollToElement(paymentDiv);
			ExtentTestManager.logMessage(Status.FAIL,"<b>Instalment Invoice is not displayed in the checkout page.");
			Assert.fail("Instalment Invoice is not displayed in the checkout page.");
		}
		return displayed;
	}

	@Step("Verify Instalment Invoice test mode displayed")
	public void verifyInstalmentInvoiceTestModeDisplayed(boolean expected) {
		boolean actual = checkElementDisplayed(instalmentInvoiceTestMode);
		if(actual)
			highlightElement(instalmentInvoiceTestMode);
		verifyAssertEquals(actual, expected,"<b>Instalment Invoice Test Mode status: </b>");
	}

	@Step("Enter Date of Birth for Instalment Invoice")
	public void fillInstalmentInvoiceDOB(String birthDate){
		setText(instalmentInvoiceDOB,birthDate);
	}

	@Step("Verify Date of Birth for Instalment Invoice displayed")
	public void verifyInstalmentInvoiceDateOfBirthIsDisplayed(boolean expected){
		try {
			waitForStaleness(instalmentInvoiceDOB);
		}catch(Exception e){e.printStackTrace();}
		verifyAssertEquals(checkElementDisplayed(instalmentInvoiceDOB), expected,"<b>Instalment Invoice date of birth display status: </b>");
	}

	@Step("Select Instalment Invoice cycles")
	public void selectInstalmentCyclesInvoice(String numberOfCycles){
		try {
			selectDropdownByValue(instalmentInvoiceSelectCycles, numberOfCycles);
		}catch(StaleElementReferenceException e){
			sleep(2);
			selectDropdownByValue(instalmentInvoiceSelectCycles, numberOfCycles);
		}
	}

	@Step("Verify Selected Instalment Invoice cycles {0} displayed")
	public void verifySelectedInstalmentCyclesDisplayedInvoice(String numberOfCycles){
		String css = ".payment_method_novalnet_instalment_invoice [id^='novalnet_instalment_invoice_table_"+numberOfCycles+"']";
		verifyAssertEquals(checkElementDisplayed(By.cssSelector(css)), true,"<b>Instalment Invoice instalment selected cycles at checkout display status: </b>");
	}

	@Step("Uncheck CC save card checkbox")
	public void uncheckSEPASaveCardCheckbox() {
		if(checkElementChecked(saveCardCheckboxSEPA)){
			clickElement(saveCardCheckboxSEPA);
		}
	}

	@Step("Verify masked CC data displayed")
	public CheckoutPage verifyMaskedCCDataDisplayed(boolean expected){
		verifyEquals(checkElementDisplayed(maskedCC),expected,"Verify masked CC data displayed");
		return this;
	}

	public int getTokenCount(){
		return getElements(maskedCC).size();
	}
	@Step("Verify current card details masked CC data displayed")
	public CheckoutPage verifyCurrentMaskedCCDataDisplayed(int previousCount){
		int currentTokenCount = getElements(maskedCC).size();
		verifyEquals(previousCount,currentTokenCount,"Verify recent order masked CC data displayed");
		return this;
	}
	@Step("Verify chekout page Gpay button displayed")
	public boolean isgpayDisplayed() {
		boolean displayed = waitForElementVisible(gpayButton,30,"");
		if(displayed) {
			waitForElementClickable(gpayButton,15);
			ExtentTestManager.addScreenShot("<b>Gpay in the checkout page Screenshot</b>");
		}else {
			ExtentTestManager.logMessage(Status.FAIL,"<b>Gpay is not displayed in the checkout page.");
			Assert.fail("Direct Debit SEPA is not displayed in the checkout page.");
		}
		return displayed;
	}
	public CheckoutPage clickGooglePayButton(){
		waitForElementClickable(gpayButton);
		clickElementWithJs(gpayButton);
		sleep(2);
		return this;
	}

	@Step("Verify {0} payment displayed")
	public void isPaymentDisplayed(String paymentType, boolean expected) {
		ExtentTestManager.addScreenShot("<b>Checkout page Screenshot</b>");
		var actual = waitForElementVisible(getPayment(paymentType),3,"Waiting for payment to visible");
		if(actual != expected){
			scrollToElement(getPayment(paymentType));
			Assert.fail("Checkout payment display verification expected "+expected+" but found "+actual);
		}
		if(actual){
			clickElementWithJs(getPayment(paymentType));
			sleep(1.5);
		}
		ExtentTestManager.logMessage(Status.PASS,"Payment "+paymentType+" is displayed in the checkout page");
		Log.info("Payment "+paymentType+" is displayed in the checkout page");
	}

	private By getPayment(String paymentType) {
		String selectorPrefix = "#payment li>label[for$='";
		String selectorSuffix = "']";
		switch (paymentType) {
			case PAYPAL:
				return By.cssSelector(selectorPrefix + "paypal" + selectorSuffix);
			case CREDITCARD:
				return By.cssSelector(selectorPrefix + "_cc" + selectorSuffix);
			case IDEAL:
				return By.cssSelector(selectorPrefix + "_ideal" + selectorSuffix);
			case MULTIBANCO:
				return By.cssSelector(selectorPrefix + "_multibanco" + selectorSuffix);
			case PREPAYMENT:
				return By.cssSelector(selectorPrefix + "_prepayment" + selectorSuffix);
			case CASHPAYMENT:
				return By.cssSelector(selectorPrefix + "novalnet_barzahlen" + selectorSuffix);
			case DIRECT_DEBIT_SEPA:
				return By.cssSelector(selectorPrefix + "sepa" + selectorSuffix);
			case GUARANTEED_DIRECT_DEBIT_SEPA:
				return By.cssSelector(selectorPrefix + "guaranteed_sepa" + selectorSuffix);
			case APPLEPAY:
				return By.cssSelector(selectorPrefix + "applepay" + selectorSuffix);
			case GOOGLEPAY:
				return By.cssSelector(selectorPrefix + "googlepay" + selectorSuffix);
			case INVOICE:
				return By.cssSelector(selectorPrefix + "invoice" + selectorSuffix);
			case GUARANTEED_INVOICE:
				return By.cssSelector(selectorPrefix + "guaranteed_invoice" + selectorSuffix);
			case ALIPAY:
				return By.cssSelector(selectorPrefix + "alipay" + selectorSuffix);
			case ONLINE_BANK_TRANSFER:
				return By.cssSelector(selectorPrefix + "online_bank_transfer" + selectorSuffix);
			case GIROPAY:
				return By.cssSelector(selectorPrefix + "giropay" + selectorSuffix);
			case BANCONTACT:
				return By.cssSelector(selectorPrefix + "bancontact" + selectorSuffix);
			case PRZELEWY24:
				return By.cssSelector(selectorPrefix + "przelewy24" + selectorSuffix);
			case EPS:
				return By.cssSelector(selectorPrefix + "eps" + selectorSuffix);
			case INSTALMENT_INVOICE:
				return By.cssSelector(selectorPrefix + "instalment_invoice" + selectorSuffix);
			case INSTALMENT_DIRECT_DEBIT_SEPA:
				return By.cssSelector(selectorPrefix + "instalment_sepa" + selectorSuffix);
			case POSTFINANCE_CARD:
				return By.cssSelector(selectorPrefix + "postfinance_card" + selectorSuffix);
			case ONLINE_TRANSFER:
				return By.cssSelector(selectorPrefix + "instantbank" + selectorSuffix);
			case WECHATPAY:
				return By.cssSelector(selectorPrefix + "wechatpay" + selectorSuffix);
			case TRUSTLY:
				return By.cssSelector(selectorPrefix + "trustly" + selectorSuffix);
			default:
				throw new IllegalArgumentException("Invalid payment method: " + paymentType);
		}
	}
	public void isLogoDisplayed(String paymnetName){
		String xpath = "//img[@alt='"+paymnetName+"']";
		boolean logoDisplayed = waitForElementVisible(By.xpath(xpath),20,"Wait for logo displayed");
		verifyEquals(logoDisplayed, true);
	}

}


