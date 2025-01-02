package com.nn.pages;

import com.nn.drivers.DriverManager;
import com.nn.reports.AllureManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;
import com.nn.reports.ExtentTestManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.*;


import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static com.nn.callback.CallbackProperties.*;



public class SettingsPage {

	private By shopAlert = By.id("message");
	private By novalnetGlobalConfigTabMenu = By.xpath("//a[contains(@href,'tab=novalnet-settings')]");
	private By paymentsTabMenu = By.xpath("//a[contains(@href,'tab=checkout')]");
	private By novalnetGlobalConfigAlert = By.cssSelector("#novalnet_global_settings-description");
	private By novalnetGlobalLoader = By.cssSelector(".blockUI.blockOverlay");
	private By novalnetApiKeyInput = By.cssSelector("#novalnet_public_key");
	private By novalnetAccessKeyInput = By.cssSelector("#novalnet_key_password");
	private By novalnetTariffInput = By.cssSelector("#novalnet_tariff_id");
	private By novalnetTariffContainer = By.cssSelector("span[aria-labelledby='select2-novalnet_tariff_id-container']>.select2-selection__arrow");
	private By novalnetSubscriptionPaymentsDropdown = By.cssSelector("#novalnet_subs_payments");
	private By novalnetSubscriptionTariffDropdown = By.cssSelector("#novalnet_subs_tariff_id");
	private By novalnetAllowManualTestCallbackCheckbox = By.cssSelector("#novalnet_callback_test_mode");
	private By saveChangesBtn = By.xpath("//*[@name='save']");
	private By mainFrame = By.cssSelector("#mainform");

	//Subscription
	private By subscriptionEnableCheckbox = By.cssSelector("#novalnet_enable_subs");
	private By subscriptionPayments = By.cssSelector(".select2-selection--multiple");
	private By subscriptionSelectedPayments = By.cssSelector(".select2-selection--multiple li");
	private By subscriptionSelectedPaymentsRemoveBtn = By.cssSelector(".select2-selection--multiple li span");
	private By subscriptionPaymentsSelectionList = By.cssSelector("#select2-novalnet_subs_payments-results");
	private By subscriptionPaymentsSelectionListOptions = By.cssSelector("#select2-novalnet_subs_payments-results li");
	private By subscriptionTariffContainer = By.cssSelector("span[aria-labelledby='select2-novalnet_subs_tariff_id-container'] span.select2-selection__arrow");
	private By subscriptionTariffInput = By.cssSelector("#novalnet_subs_tariff_id");
	private By subscriptionCancelFrontendCheckbox = By.cssSelector("#novalnet_usr_subcl");
	private By subscriptionEnableShopBasedCheckbox = By.cssSelector("#novalnet_enable_shop_subs");

	// creditcard
	private By successOrderStatus = By.cssSelector("[id$='order_success_status-container']");
	private By ccPayment = By.cssSelector("tr[data-gateway_id$='_cc'] td.name a");

	private By paypalPayment = By.cssSelector("tr[data-gateway_id$='_paypal'] td.name a");

	private By ccEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_cc_enabled");

	private By paypalEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_paypal_enabled");
	private By ccTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_cc_test_mode");
	private By ccPaymentTitle = By.cssSelector("#woocommerce_novalnet_cc_title_en");

	private By paypalPaymentTitle = By.cssSelector("#woocommerce_novalnet_paypal_title_en");
	private By ccOneClickCheckbox = By.cssSelector("#woocommerce_novalnet_cc_tokenization");

	private By ccPaymentAction = By.cssSelector("#select2-woocommerce_novalnet_cc_payment_status-container");

	private By paypalPaymentAction = By.cssSelector("#select2-woocommerce_novalnet_paypal_payment_status-container");
	private By ccAuthorizeMinAmount = By.cssSelector("#woocommerce_novalnet_cc_limit");

	private By paypalAuthorizeMinAmount = By.cssSelector("#woocommerce_novalnet_paypal_limit");
	private By ccInlineForm = By.cssSelector("#woocommerce_novalnet_cc_enable_iniline_form");
	private By ccEnforce3D = By.cssSelector("#woocommerce_novalnet_cc_enforce_3d");




	private By onlineBankTransferPayment = By.cssSelector("tr[data-gateway_id='novalnet_online_bank_transfer'] td.name a");

	private By trustlyPayment = By.cssSelector("tr[data-gateway_id='novalnet_trustly'] td.name a");

	private By giropayPayment = By.cssSelector("tr[data-gateway_id='novalnet_giropay'] td.name a");

	private By postFinancePayment = By.cssSelector("tr[data-gateway_id='novalnet_postfinance_card'] td.name a");

	private By epsPayment = By.cssSelector("tr[data-gateway_id='novalnet_eps'] td.name a");

	private By banContactPayment = By.cssSelector("tr[data-gateway_id='novalnet_bancontact'] td.name a");

	private By alipayPayment = By.cssSelector("tr[data-gateway_id='novalnet_alipay'] td.name a");

	private By weChatPayment = By.cssSelector("tr[data-gateway_id='novalnet_wechatpay'] td.name a");

	private By onlineBankTransferEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_online_bank_transfer_enabled");

	private By trustlyEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_trustly_enabled");

	private By giropayEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_giropay_enabled");

	private By postFinanceEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_postfinance_card_enabled");

	private By epsEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_eps_enabled");

	private By banContactEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_bancontact_enabled");

	private By alipayEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_alipay_enabled");

	private By weChatPayEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_wechatpay_enabled");

	private By trustlyTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_trustly_test_mode");

	private By giropayTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_giropay_test_mode");

	private By postFinanceTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_postfinance_card_test_mode");
	private By epsTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_eps_test_mode");

	private By banContactTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_bancontact_test_mode");
	private By alipayTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_alipay_test_mode");

	private By weChatPayTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_wechatpay_test_mode");
	private By onlineBankTransferTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_online_bank_transfer_test_mode");

	private By paypalTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_paypal_test_mode");


	private By trustlyPaymentTitle = By.cssSelector("#woocommerce_novalnet_trustly_title_en");

	private By onlineBankTransferPaymentTitle = By.cssSelector("#woocommerce_novalnet_online_bank_transfer_title_en");

	private By giropayPaymentTitle = By.cssSelector("#woocommerce_novalnet_giropay_title_en");

	private By postFinancePaymentTitle = By.cssSelector("#woocommerce_novalnet_postfinance_card_title_en");

	private By epsPaymentTitle = By.cssSelector("#woocommerce_novalnet_eps_title_en");

	private By banContactPaymentTitle = By.cssSelector("#woocommerce_novalnet_bancontact_title_en");

	private By alipayPaymentTitle = By.cssSelector("#woocommerce_novalnet_alipay_title_en");

	private By weChatPayPaymentTitle = By.cssSelector("#woocommerce_novalnet_wechatpay_title_en");

	private By payPalPaymentTitle = By.cssSelector("#woocommerce_novalnet_paypal_title_en");

	//Invoice
	private By invoice = By.cssSelector("tr[data-gateway_id='novalnet_invoice'] td.name a");
	private By invoiceEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_invoice_enabled");
	private By invoiceTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_invoice_test_mode");
	private By invoicePaymentTitle = By.cssSelector("#woocommerce_novalnet_invoice_title_en");
	private By invoiceDueDate = By.cssSelector("#woocommerce_novalnet_invoice_payment_duration");
	private By invoicePaymentAction = By.cssSelector("#select2-woocommerce_novalnet_invoice_payment_status-container");
	private By invoiceAuthorizeMinAmount = By.cssSelector("#woocommerce_novalnet_invoice_limit");
	private By invoiceOrderSuccessStatus = By.cssSelector("#select2-woocommerce_novalnet_invoice_order_success_status-container");
	private By invoiceCallbackStatus = By.cssSelector("#select2-woocommerce_novalnet_invoice_callback_status-container");

	//Invoice Guarantee
	private By invoiceGuarantee = By.cssSelector("tr[data-gateway_id='novalnet_guaranteed_invoice'] td.name a");
	private By invoiceGuaranteeEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_guaranteed_invoice_enabled");
	private By invoiceGuaranteeTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_guaranteed_invoice_test_mode");
	private By invoiceGuaranteePaymentTitle = By.cssSelector("#woocommerce_novalnet_guaranteed_invoice_title_en");
	private By invoiceGuaranteePaymentAction = By.cssSelector("#select2-woocommerce_novalnet_guaranteed_invoice_payment_status-container");
	private By invoiceGuaranteeAuthorizeMinAmount = By.cssSelector("#woocommerce_novalnet_guaranteed_invoice_limit");
	private By invoiceGuaranteeOrderSuccessStatus = By.cssSelector("#select2-woocommerce_novalnet_invoice_order_success_status-container");
	private By invoiceGuaranteeForceNonGuarantee = By.cssSelector("#woocommerce_novalnet_guaranteed_invoice_force_normal_payment");
	private By invoiceGuaranteeMinOrderAmount = By.cssSelector("#woocommerce_novalnet_guaranteed_invoice_min_amount");
	private By invoiceGuaranteeAllowB2B = By.cssSelector("#woocommerce_novalnet_guaranteed_invoice_allow_b2b");

	//Instalment Invoice
	private By instalmentInvoice = By.cssSelector("tr[data-gateway_id='novalnet_instalment_invoice'] td.name a");
	private By instalmentInvoiceEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_instalment_invoice_enabled");
	private By instalmentInvoiceTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_instalment_invoice_test_mode");
	private By instalmentInvoicePaymentTitle = By.cssSelector("#woocommerce_novalnet_instalment_invoice_title_en");
	private By instalmentInvoicePaymentAction = By.cssSelector("#select2-woocommerce_novalnet_instalment_invoice_payment_status-container");
	private By instalmentInvoiceAuthorizeMinAmount = By.cssSelector("#woocommerce_novalnet_instalment_invoice_limit");
	private By instalmentInvoiceMinOrderAmount = By.cssSelector("#woocommerce_novalnet_instalment_invoice_min_amount");
	private By instalmentInvoiceAllowB2B = By.cssSelector("#woocommerce_novalnet_instalment_invoice_allow_b2b");


	//Direct Debit SEPA
	private By sepa = By.cssSelector("tr[data-gateway_id='novalnet_sepa'] td.name a");
	private By sepaEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_sepa_enabled");
	private By sepaTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_sepa_test_mode");
	private By sepaPaymentTitle = By.cssSelector("#woocommerce_novalnet_sepa_title_en");
	private By sepaDueDate = By.cssSelector("#woocommerce_novalnet_sepa_payment_duration");
	//private By sepaPaymentAction = By.cssSelector("#select2-woocommerce_novalnet_sepa_payment_status-container");

	private By paymentActionDropDown = By.cssSelector("[id$='payment_status-container'] + span");

	private By sepaAuthorizeMinAmount = By.cssSelector("#woocommerce_novalnet_sepa_limit");
	private By sepaOrderSuccessStatus = By.cssSelector("#select2-woocommerce_novalnet_sepa_order_success_status-container");
	private By sepaOneClickCheckbox = By.cssSelector("#woocommerce_novalnet_sepa_tokenization");


	//SEPA Guarantee
	private By sepaGuarantee = By.cssSelector("tr[data-gateway_id='novalnet_guaranteed_sepa'] td.name a");
	private By sepaGuaranteeEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_guaranteed_sepa_enabled");
	private By sepaGuaranteeTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_guaranteed_sepa_test_mode");
	private By sepaGuaranteePaymentTitle = By.cssSelector("#woocommerce_novalnet_guaranteed_sepa_title_en");
	private By sepaGuaranteeDueDate = By.cssSelector("#woocommerce_novalnet_guaranteed_sepa_payment_duration");
	private By sepaGuaranteePaymentAction = By.cssSelector("#select2-woocommerce_novalnet_guaranteed_sepa_payment_status-container");
	private By sepaGuaranteeAuthorizeMinAmount = By.cssSelector("#woocommerce_novalnet_guaranteed_sepa_limit");
	private By sepaGuaranteeOrderSuccessStatus = By.cssSelector("#select2-woocommerce_novalnet_guaranteed_sepa_order_success_status-container");
	private By sepaGuaranteeOneClickCheckbox = By.cssSelector("#woocommerce_novalnet_guaranteed_sepa_tokenization");
	private By sepaGuaranteeAllowB2B = By.cssSelector("#woocommerce_novalnet_guaranteed_sepa_allow_b2b");
	private By sepaGuaranteeMinOrderAmount = By.cssSelector("#woocommerce_novalnet_guaranteed_sepa_min_amount");
	private By sepaGuaranteeForceNonGuarantee = By.cssSelector("#woocommerce_novalnet_guaranteed_sepa_force_normal_payment");

	//Instalment SEPA
	private By instalmentSepa = By.cssSelector("tr[data-gateway_id='novalnet_instalment_sepa'] td.name a");
	private By instalmentSepaEnablePaymentCheckbox = By.cssSelector("#woocommerce_novalnet_instalment_sepa_enabled");
	private By instalmentSepaTestModeCheckbox = By.cssSelector("#woocommerce_novalnet_instalment_sepa_test_mode");
	private By instalmentSepaPaymentTitle = By.cssSelector("#woocommerce_novalnet_instalment_sepa_title_en");
	private By instalmentSepaDueDate = By.cssSelector("#woocommerce_novalnet_instalment_sepa_payment_duration");
	private By instalmentSepaPaymentAction = By.cssSelector("#select2-woocommerce_novalnet_instalment_sepa_payment_status-container");
	private By instalmentSepaAuthorizeMinAmount = By.cssSelector("#woocommerce_novalnet_instalment_sepa_limit");
	private By instalmentSepaOneClickCheckbox = By.cssSelector("#woocommerce_novalnet_instalment_sepa_tokenization");
	private By instalmentSepaAllowB2B = By.cssSelector("#woocommerce_novalnet_instalment_sepa_allow_b2b");
	private By instalmentSepaMinOrderAmount = By.cssSelector("#woocommerce_novalnet_instalment_sepa_min_amount");


	private By AuthorizeMinAmount = By.cssSelector("[id$='limit']");
	private By InlineForm = By.cssSelector("[id$='iniline_form']");
	private By Enforce3D = By.cssSelector("#woocommerce_novalnet_cc_enforce_3d");
	private By PaymentAction = By.cssSelector("[id$='payment_status-container']");
	private By OneClickCheckbox = By.cssSelector("#woocommerce_novalnet_cc_tokenization");
	private By PaymentTitle = By.cssSelector("[id$='title_en']");
	private By TestModeCheckbox = By.cssSelector("[id$='test_mode']");
	private By EnablePaymentCheckbox = By.cssSelector("[id$='enabled']");
	private By DueDate = By.cssSelector("[id$='payment_duration']");
	private By gpayPayment = By.cssSelector("tr[data-gateway_id$='_googlepay'] td.name a");

	private By gpayEnabledCheckBox = By.cssSelector("#woocommerce_novalnet_googlepay_enabled");

	private By gpayTestModeCheckBox = By.cssSelector("#woocommerce_novalnet_googlepay_test_mode");

	private By gpayEnforce3dCheckBox = By.cssSelector("#woocommerce_novalnet_googlepay_enforce_3d");

	private By gpayPaymentAction = By.cssSelector("#select2-woocommerce_novalnet_googlepay_payment_status-container");

	private By gpayPaymentTitle = By.cssSelector("#woocommerce_novalnet_googlepay_title_en");

	private By gpayAuthorizMinimumAmount = By.cssSelector("#woocommerce_novalnet_googlepay_limit");


	public SettingsPage load() {
		openURL(URL + "admin.php?page=wc-settings");
		waitForTitleContains("WooCommerce settings");
		return this;
	}

	public void openNovalnetGlobalConfig() {
		clickElementWithJs(novalnetGlobalConfigTabMenu);
		waitForPageLoad();
		waitForElementDisable(novalnetGlobalLoader);
	}

	public void openPaymentsPage() {
		clickElementWithJs(paymentsTabMenu);
		waitForPageLoad();
	}

	@Step("Load Novalnet global configuration page")
	public SettingsPage novalnetGlobalConfigPageLoad() {
		openURL(URL + "admin.php?page=wc-settings&tab=novalnet-settings");
			waitForTitleContains("WooCommerce settings",60);
			waitForElementDisable(novalnetGlobalLoader);
		return this;
	}

	@Step("Load payment page")
	public SettingsPage paymentPageLoad() {
		openURL(URL + "admin.php?page=wc-settings&tab=checkout");
		try {
			WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.titleContains("WooCommerce settings"));
		} catch (Exception e) {
			DriverManager.getDriver().navigate().refresh();
			sleep(2);
			if (verifyAlertPresent(3)) {
				Log.info("Inside Alert - Settings Page");
				alertAccept();
			}
			openURL(URL + "admin.php?page=wc-settings&tab=checkout");
		}
		waitForTitleContains("WooCommerce settings");
		return this;
	}

	public void verifyGlobalConfig() {

		String apiKey = getInputFieldText(novalnetApiKeyInput);
		String accessKey = getInputFieldText(novalnetAccessKeyInput);
		String tariffID = getInputFieldText(novalnetTariffInput);

		System.out.println(apiKey.equals(NOVALNET_API_KEY));
		System.out.println(accessKey.equals(NOVALNET_ACCESSKEY));
		System.out.println(tariffID.equals(NOVALNET_TARIFF));

		if (!apiKey.equals(NOVALNET_API_KEY) || !accessKey.equals(NOVALNET_ACCESSKEY) || !tariffID.equals(NOVALNET_TARIFF) || !checkElementChecked(novalnetAllowManualTestCallbackCheckbox)) {
			if (!verifyElementTextEquals(apiKey, NOVALNET_API_KEY)) {
				setText(novalnetApiKeyInput, NOVALNET_API_KEY);
				//clickSomewhere();
				pressTab(novalnetApiKeyInput);
				if (checkElementExist(novalnetGlobalLoader))
					waitForElementDisable(novalnetGlobalLoader, 15);
				sleep(3);
			}
			if (!verifyElementTextEquals(accessKey, NOVALNET_ACCESSKEY)) {
				setText(novalnetAccessKeyInput, NOVALNET_ACCESSKEY);
				//clickSomewhere();
				pressTab(novalnetAccessKeyInput);
				waitForElementDisable(novalnetGlobalLoader);
				waitForElementVisible(By.cssSelector("#select2-novalnet_tariff_id-container"));
			}
			if (!verifyElementTextEquals(getInputFieldText(novalnetTariffInput), NOVALNET_TARIFF)) {
				clickElement(novalnetTariffContainer);
				String tariffLocator = "ul[id$='tariff_id-results']>li[id$='-" + NOVALNET_TARIFF + "']";
				if (!checkElementExist(By.cssSelector(tariffLocator))) {
					clickElement(novalnetTariffContainer);
				}
				clickElement(By.cssSelector(tariffLocator));
			}
			allowManulTestingOfWebhook();
			clickElement(saveChangesBtn);
			waitForElementVisible(shopAlert);
		}
		verifyEquals(getInputFieldText(novalnetTariffInput), NOVALNET_TARIFF, "<b>Novalnet Tariff ID:</b>");
		verifyEquals(getInputFieldText(novalnetApiKeyInput), NOVALNET_API_KEY, "<b>Novalnet API Key:</b>");
		verifyEquals(getInputFieldText(novalnetAccessKeyInput), NOVALNET_ACCESSKEY, "<b>Novalnet Payment Access Key:</b>");
		waitForElementDisable(novalnetGlobalLoader);
		scrollToElement(mainFrame);
		ExtentTestManager.addScreenShot(Status.PASS, "<b>Verification of Novalnet Global Configuration in shop backend</b>");
	}

	public void allowManulTestingOfWebhook() {
		if (!checkElementChecked(novalnetAllowManualTestCallbackCheckbox)) {
			clickElement(novalnetAllowManualTestCallbackCheckbox);
		}
	}

	public void setSubscriptionTariff() {
		scrollToElement(subscriptionTariffContainer);
		if (!verifyElementTextEquals(getInputFieldText(subscriptionTariffInput), "4853")) {
			waitForElementDisable(novalnetGlobalLoader);
			clickElement(subscriptionTariffContainer);
			String tariffLocator = "ul[id$='subs_tariff_id-results']>li[id$='-4853']";
			clickElement(By.cssSelector(tariffLocator));
		}
		verifyEquals(getInputFieldText(subscriptionTariffInput), NOVALNET_SUBSCRIPTION_TARIFF, "<b>Novalnet Subscription Tariff: ");
	}

	public void setSubscriptionTariff(String tariff) {
		scrollToElement(subscriptionTariffContainer);
		if (!verifyElementTextEquals(getInputFieldText(subscriptionTariffInput), tariff)) {
			waitForElementDisable(novalnetGlobalLoader);
			clickElement(subscriptionTariffContainer);
			String tariffLocator = "ul[id$='subs_tariff_id-results']>li[id$='-"+tariff+"']";
			clickElement(By.cssSelector(tariffLocator));
		}
		verifyEquals(getInputFieldText(subscriptionTariffInput), NOVALNET_SUBSCRIPTION_TARIFF, "<b>Novalnet Subscription Tariff: ");
	}

	public void selectSubscriptionPayments(List<String> subsPayments) {
		if (subsPayments.size() == 0)
			return;
		List<WebElement> selectedPaymentsEle = getElements(subscriptionSelectedPayments);
		List<String> selectedPayments = new ArrayList<>();

		for (WebElement e : selectedPaymentsEle) {
			if (e.getAttribute("title").trim().length() > 0 && !e.getAttribute("title").trim().isBlank())
				selectedPayments.add(e.getAttribute("title").trim());
		}

		if (!new HashSet<String>(subsPayments).equals(new HashSet<String>(selectedPayments))) {
			clickElementWithJs(subscriptionSelectedPayments);
			waitForElementVisible(subscriptionPaymentsSelectionListOptions);
			if (checkElementExist(novalnetGlobalLoader))
				waitForElementDisable(novalnetGlobalLoader, 15);

			int size = getElements(subscriptionPaymentsSelectionListOptions).size();
			for (int i = 0; i < size; i++) {
				List<WebElement> list = getElements(subscriptionPaymentsSelectionListOptions);
				if (subsPayments.contains(list.get(i).getText().trim()) && list.get(i).getAttribute("data-selected").equals("false")) {
					list.get(i).click();
					sleep(0.5);
					clickElement(subscriptionSelectedPayments);

				} else if (!subsPayments.contains(list.get(i).getText().trim()) && list.get(i).getAttribute("data-selected").equals("true")) {
					list.get(i).click();
					sleep(0.5);
					clickElement(subscriptionSelectedPayments);
				}
			}
			//clickSomewhere();			
			pressTab(subscriptionSelectedPayments);
		}
	}

	public void enableSubscription() {
		if (!checkElementChecked(subscriptionEnableCheckbox)) {
			clickElementWithJs(subscriptionEnableCheckbox);
			sleep(0.5);
		}
	}

	public void enableSubscriptionShopBased() {
		if (!checkElementChecked(subscriptionEnableShopBasedCheckbox)) {
			scrollToElement(getElement(subscriptionEnableShopBasedCheckbox));
			clickElementWithJs(subscriptionEnableShopBasedCheckbox);
			alertAccept();
		}
		ExtentTestManager.addScreenShot("<b>Novalnet Subscription Global Configuration Screenshot");
	}

	public void enableSubscriptionNovalnetBased() {
		if (checkElementChecked(subscriptionEnableShopBasedCheckbox)) {
			scrollToElement(subscriptionEnableShopBasedCheckbox);
			clickElementWithJs(subscriptionEnableShopBasedCheckbox);
			alertAccept();
		}
		ExtentTestManager.addScreenShot("<b>Novalnet Subscription Global Configuration Screenshot");
	}

	@Step("Save Global Config")
	public void saveGlobalConfig() {
		clickElementWithJs(saveChangesBtn);
		waitForElementVisible(shopAlert);
	}

	public void enableSubscriptionCancelfrontend() {
		if (!checkElementChecked(subscriptionCancelFrontendCheckbox)) {
			clickElementWithJs(subscriptionCancelFrontendCheckbox);
		}
	}

	@Step("Disable subscription cancel shop frontend")
	public void disableSubscriptionCancelfrontend() {
		if (checkElementChecked(subscriptionCancelFrontendCheckbox)) {
			clickElementWithJs(subscriptionCancelFrontendCheckbox);
		}
	}

	@Step("Activate payment {0}")
	public boolean activatePayment(String paymentName) {
		//td[@class='name']/a[text()='Novalnet Direct Debit SEPA']/../following-sibling::td[@class='status']//span
		String toggleBtnPath = "//td[@class='name']/a[contains(text(),'" + paymentName.trim() + "')]/../following-sibling::td[@class='status']//span";
		if (getElementAttributeText(By.xpath(toggleBtnPath), "class").contains("input-toggle--disabled")) {
			clickElement(By.xpath(toggleBtnPath));
			waitForElementAttributeToChange(By.xpath(toggleBtnPath), "class", "input-toggle--enabled");
		}
		return true;
	}

	@Step("Deactivate payment {0}")
	public boolean deActivatePayment(String paymentName) {
		//td[@class='name']/a[text()='Novalnet Direct Debit SEPA']/../following-sibling::td[@class='status']//span
		String toggleBtnPath = "//td[@class='name']/a[contains(text(),'" + paymentName.trim() + "')]/../following-sibling::td[@class='status']//span";
		if (getElementAttributeText(By.xpath(toggleBtnPath), "class").contains("input-toggle--enabled")) {
			clickElement(By.xpath(toggleBtnPath));
			waitForElementAttributeToChange(By.xpath(toggleBtnPath), "class", "input-toggle--disabled");
		}
		return true;
	}

	@Step("Set Payment Configuration for CreditCard active = {0}, payment action = {1}, minimum authorize amount = {2}, test mode = {3}, one click = {4}, inline form = {5}, enforce3D = {6}")
	public Map<String, Object> setCreditCardPaymentConfiguration(
			boolean paymentActive,
			String paymentAction,
			String minAuthAmount,
			boolean testMode,
			boolean oneClick,
			boolean inlineForm,
			boolean enforce3D) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("PaymentAction", paymentAction);
		map.put("MinimumAuthAmount", minAuthAmount);
		map.put("TestMode", testMode);
		map.put("OneClick", oneClick);
		map.put("InlineForm", inlineForm);
		map.put("Enforce3D", enforce3D);
		ExtentTestManager.logMessage("<b>CreditCard Backend Configuration : </b><br>" + printMap(map));
		AllureManager.attachHtml("<b>CreditCard Backend Configuration : </b><br>" + printMap(map));
		clickElement(ccPayment);
		waitForElementVisible(ccEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, oneClick, inlineForm, enforce3D};
		List<By> by = new ArrayList<>();
		by.add(ccEnablePaymentCheckbox);
		by.add(ccTestModeCheckbox);
		by.add(ccOneClickCheckbox);
		by.add(ccInlineForm);
		by.add(ccEnforce3D);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}
		if (!paymentAction.equals("") && !paymentAction.equals(null)) {
			if (!verifyElementTextEquals(getElementText(ccPaymentAction), paymentAction)) {
				scrollToElement(ccPaymentTitle);
				clickElementWithAction(ccPaymentAction);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElement(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(ccAuthorizeMinAmount, minAuthAmount);
		}


		map.put("PaymentTitle", getInputFieldText(ccPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	@Step("Set Payment Configuration for PayPal active = {0}, payment action = {1}, minimum authorize amount = {2}, test mode = {3}")
	public Map<String, Object> setPayPalPaymentConfiguration(
			boolean paymentActive,
			String paymentAction,
			String minAuthAmount,
			boolean testMode
	) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("PaymentAction", paymentAction);
		map.put("MinimumAuthAmount", minAuthAmount);
		map.put("TestMode", testMode);
		ExtentTestManager.logMessage("<b>PayPal Backend Configuration : </b><br>" + printMap(map));
		AllureManager.attachHtml("<b>PayPal Backend Configuration : </b><br>" + printMap(map));
		clickElement(paypalPayment);
		waitForElementVisible(paypalEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(paypalEnablePaymentCheckbox);
		by.add(paypalTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}
		if (!paymentAction.equals("") && !paymentAction.equals(null)) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals("Authorize")) {
			setText(paypalAuthorizeMinAmount, minAuthAmount);
		}


		map.put("PaymentTitle", getInputFieldText(paypalPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}



	public Map<String, Object> setOnlineBankTransferPaymentConfiguration(
			boolean paymentActive,
			boolean testMode) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		ExtentTestManager.logMessage("<b>OnlineBankTransfer Backend Configuration : </b><br>" + printMap(map));
		clickElement(onlineBankTransferPayment);
		waitForElementVisible(onlineBankTransferEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(onlineBankTransferEnablePaymentCheckbox);
		by.add(onlineBankTransferTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		map.put("PaymentTitle", getInputFieldText(onlineBankTransferPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setTrustlyPaymentConfiguration(
			boolean paymentActive,
			boolean testMode) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		ExtentTestManager.logMessage("<b>Trustly Backend Configuration : </b><br>" + printMap(map));
		clickElement(trustlyPayment);
		waitForElementVisible(trustlyEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(trustlyEnablePaymentCheckbox);
		by.add(trustlyTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		map.put("PaymentTitle", getInputFieldText(trustlyPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setGiropayPaymentConfiguration(
			boolean paymentActive,
			boolean testMode) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		ExtentTestManager.logMessage("<b>GiroPay Backend Configuration : </b><br>" + printMap(map));
		clickElement(giropayPayment);
		waitForElementVisible(giropayEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(giropayEnablePaymentCheckbox);
		by.add(giropayTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		map.put("PaymentTitle", getInputFieldText(giropayPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}


	public Map<String, Object> setPostFinancePaymentConfiguration(
			boolean paymentActive,
			boolean testMode) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		ExtentTestManager.logMessage("<b>Post Finance Backend Configuration : </b><br>" + printMap(map));
		clickElement(postFinancePayment);
		waitForElementVisible(postFinanceEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(postFinanceEnablePaymentCheckbox);
		by.add(postFinanceTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		map.put("PaymentTitle", getInputFieldText(postFinancePaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setEPSPaymentConfiguration(
			boolean paymentActive,
			boolean testMode) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		ExtentTestManager.logMessage("<b>EPS Backend Configuration : </b><br>" + printMap(map));
		clickElement(epsPayment);
		waitForElementVisible(epsEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(epsEnablePaymentCheckbox);
		by.add(epsTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		map.put("PaymentTitle", getInputFieldText(epsPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setbancontactPaymentConfiguration(
			boolean paymentActive,
			boolean testMode) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		ExtentTestManager.logMessage("<b>Bancontact Backend Configuration : </b><br>" + printMap(map));
		clickElement(banContactPayment);
		waitForElementVisible(banContactEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(banContactEnablePaymentCheckbox);
		by.add(banContactTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		map.put("PaymentTitle", getInputFieldText(banContactPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setAlipayPaymentConfiguration(
			boolean paymentActive,
			boolean testMode) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		ExtentTestManager.logMessage("<b>Alipay Backend Configuration : </b><br>" + printMap(map));
		clickElement(alipayPayment);
		waitForElementVisible(alipayEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(alipayEnablePaymentCheckbox);
		by.add(alipayTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		map.put("PaymentTitle", getInputFieldText(alipayPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setWeChatPayPaymentConfiguration(
			boolean paymentActive,
			boolean testMode) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		ExtentTestManager.logMessage("<b>WeChatPay Backend Configuration : </b><br>" + printMap(map));
		clickElement(weChatPayment);
		waitForElementVisible(weChatPayEnablePaymentCheckbox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(weChatPayEnablePaymentCheckbox);
		by.add(weChatPayTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		map.put("PaymentTitle", getInputFieldText(weChatPayPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}





	public Map<String, Object> setInvoiceConfiguration(
			boolean paymentActive,
			boolean testMode,
			String paymentAction,
			String authMinAmount,
			String dueDate) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("PaymentAction", paymentAction);
		map.put("DueDate", dueDate);
		map.put("AuthorizeMinAmount", authMinAmount);
		ExtentTestManager.logMessage("<b>Invoice payment Configuration : </b>" + printMap(map));
		clickElement(invoice);
		waitForElementVisible(invoiceEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode};
		List<By> by = new ArrayList<>();
		by.add(invoiceEnablePaymentCheckbox);
		by.add(invoiceTestModeCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		setText(invoiceDueDate, dueDate);

		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(invoiceAuthorizeMinAmount, authMinAmount);
		}

		scrollToElement(invoicePaymentTitle);

		if (!getElementText(invoiceOrderSuccessStatus).equals(PROCESSING_ORDER_STATUS)) {
			clickElementWithAction(invoiceOrderSuccessStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + PROCESSING_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}

		if (!getElementText(invoiceCallbackStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(invoiceCallbackStatus);
			String path = "ul[id$='callback_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(invoicePaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setDirectDebitSepaConfiguration(
			boolean paymentActive,
			boolean testMode,
			boolean oneClick,
			String paymentAction,
			String authMinAmount,
			String dueDate) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("OneClick", oneClick);
		map.put("PaymentAction", paymentAction);
		map.put("DueDate", dueDate);
		map.put("AuthorizeMinAmount", authMinAmount);
		ExtentTestManager.logMessage("<b>Direct Debit SEPA payment Configuration : </b>" + printMap(map));
		clickElement(sepa);
		waitForElementVisible(sepaEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, oneClick};
		List<By> by = new ArrayList<>();
		by.add(sepaEnablePaymentCheckbox);
		by.add(sepaTestModeCheckbox);
		by.add(sepaOneClickCheckbox);
		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		setText(sepaDueDate, dueDate);

		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				setExpectedCondition(d->d.findElement(By.cssSelector("select[id$='_payment_status'] + span .select2-selection--single")).getAttribute("aria-expanded").equals("true"),
						1,"Wait for payment action dropdown appear");
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(sepaAuthorizeMinAmount, authMinAmount);
		}

		scrollToElement(sepaPaymentTitle);

		if (!getElementText(sepaOrderSuccessStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(sepaOrderSuccessStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(sepaPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setSEPAGuaranteeConfiguration(
			boolean paymentActive,
			boolean testMode,
			boolean oneClick,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount,
			String dueDate) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("OneClick", oneClick);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("DueDate", dueDate);
		map.put("AuthorizeMinAmount", authMinAmount);
		ExtentTestManager.logMessage("<b>Direct Debit SEPA with payment guarantee payment Configuration : </b>" + printMap(map));
		clickElement(sepaGuarantee);
		waitForElementVisible(sepaGuaranteeEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, oneClick, allowB2B};
		List<By> by = new ArrayList<>();
		by.add(sepaGuaranteeEnablePaymentCheckbox);
		by.add(sepaGuaranteeTestModeCheckbox);
		by.add(sepaGuaranteeOneClickCheckbox);
		by.add(sepaGuaranteeAllowB2B);
		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		//setText(sepaGuaranteeDueDate, dueDate);

		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(sepaGuaranteeAuthorizeMinAmount, authMinAmount);
		}

		scrollToElement(sepaGuaranteePaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(sepaGuaranteePaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}


	public Map<String, Object> setSEPAGuaranteeConfigurationWithMinOrderAmount(
			boolean paymentActive,
			boolean testMode,
			boolean oneClick,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount,
			String dueDate, String minOrderAmount) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("OneClick", oneClick);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("DueDate", dueDate);
		map.put("AuthorizeMinAmount", authMinAmount);
		map.put("MinOrderAmount", minOrderAmount);
		ExtentTestManager.logMessage("<b>Direct Debit SEPA with payment guarantee payment Configuration : </b>" + printMap(map));
		clickElement(sepaGuarantee);
		waitForElementVisible(sepaGuaranteeEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, oneClick, allowB2B};
		List<By> by = new ArrayList<>();
		by.add(sepaGuaranteeEnablePaymentCheckbox);
		by.add(sepaGuaranteeTestModeCheckbox);
		by.add(sepaGuaranteeOneClickCheckbox);
		by.add(sepaGuaranteeAllowB2B);
		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		//setText(sepaGuaranteeDueDate, dueDate);

		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				setExpectedCondition(d->d.findElement(By.cssSelector("select[id$='_payment_status'] + span .select2-selection--single")).getAttribute("aria-expanded").equals("true"),
						1,"Wait for payment action dropdown appear");
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(sepaGuaranteeAuthorizeMinAmount, authMinAmount);
			}
		setText(sepaGuaranteeMinOrderAmount, minOrderAmount);

		scrollToElement(sepaGuaranteePaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(sepaGuaranteePaymentTitle).trim());

		clickElement(saveChangesBtn);
		if(Integer.parseInt(minOrderAmount)<999) {
			ExtentTestManager.addScreenShot("<b>Minimum Order Value Amount must be greater than or equal to 999 for Guarantee Sepa</b>");
		}else{
			waitForElementVisible(shopAlert);
			scrollToElement(mainFrame);
		}
		return map;
	}

	public Map<String, Object> setSEPAGuaranteeConfigurationWithMinOrderAmount(
			boolean paymentActive,
			boolean testMode,
			boolean oneClick,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount,
			String dueDate, String minOrderAmount, boolean forceNonGuarantee) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("OneClick", oneClick);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("DueDate", dueDate);
		map.put("AuthorizeMinAmount", authMinAmount);
		map.put("MinOrderAmount", minOrderAmount);
		ExtentTestManager.logMessage("<b>Direct Debit SEPA with payment guarantee payment Configuration : </b>" + printMap(map));
		clickElement(sepaGuarantee);
		waitForElementVisible(sepaGuaranteeEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, oneClick, allowB2B, forceNonGuarantee};
		List<By> by = new ArrayList<>();
		by.add(sepaGuaranteeEnablePaymentCheckbox);
		by.add(sepaGuaranteeTestModeCheckbox);
		by.add(sepaGuaranteeOneClickCheckbox);
		by.add(sepaGuaranteeAllowB2B);
		by.add(sepaGuaranteeForceNonGuarantee);
		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		//setText(sepaGuaranteeDueDate, dueDate);

		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(sepaGuaranteeAuthorizeMinAmount, authMinAmount);
		}
		setText(sepaGuaranteeMinOrderAmount, minOrderAmount);

		scrollToElement(sepaGuaranteePaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(sepaGuaranteePaymentTitle).trim());

		clickElement(saveChangesBtn);
		if(Integer.parseInt(minOrderAmount)<999) {
			ExtentTestManager.addScreenShot("<b>Minimum Order Value Amount must be greater than or equal to 999 for Guarantee Sepa</b>");
		}else{
			waitForElementVisible(shopAlert);
			scrollToElement(mainFrame);
		}
		return map;
	}


	public Map<String, Object> setInstalmentSEPAConfiguration(
			boolean paymentActive,
			boolean testMode,
			boolean oneClick,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount,
			String dueDate) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("OneClick", oneClick);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("DueDate", dueDate);
		map.put("AuthorizeMinAmount", authMinAmount);
		ExtentTestManager.logMessage("<b>Instalment by Direct Debit SEPA payment Configuration : </b>" + printMap(map));
		clickElement(instalmentSepa);
		waitForElementVisible(instalmentSepaEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, oneClick, allowB2B};
		List<By> by = new ArrayList<>();
		by.add(instalmentSepaEnablePaymentCheckbox);
		by.add(instalmentSepaTestModeCheckbox);
		by.add(instalmentSepaOneClickCheckbox);
		by.add(instalmentSepaAllowB2B);
		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		setText(instalmentSepaDueDate, dueDate);

		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(instalmentSepaAuthorizeMinAmount, authMinAmount);
		}

		scrollToElement(instalmentSepaPaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(instalmentSepaPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setInstalmentSEPAWithMinOrderAmountConfiguration(
			boolean paymentActive,
			boolean testMode,
			boolean oneClick,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount,
			String dueDate,String minOrderAmt) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("OneClick", oneClick);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("DueDate", dueDate);
		map.put("AuthorizeMinAmount", authMinAmount);
		map.put("MinOrderAmount", minOrderAmt);
		ExtentTestManager.logMessage("<b>Instalment by Direct Debit SEPA payment Configuration : </b>" + printMap(map));
		clickElement(instalmentSepa);
		waitForElementVisible(instalmentSepaEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, oneClick, allowB2B};
		List<By> by = new ArrayList<>();
		by.add(instalmentSepaEnablePaymentCheckbox);
		by.add(instalmentSepaTestModeCheckbox);
		by.add(instalmentSepaOneClickCheckbox);
		by.add(instalmentSepaAllowB2B);
		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}

		//setText(instalmentSepaDueDate, dueDate);

		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(instalmentSepaAuthorizeMinAmount, authMinAmount);
		}
		setText(instalmentSepaMinOrderAmount, minOrderAmt);

		scrollToElement(instalmentSepaPaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(instalmentSepaPaymentTitle).trim());

		clickElement(saveChangesBtn);
		if(Integer.parseInt(minOrderAmt)<1998) {
			ExtentTestManager.addScreenShot("<b>Minimum Order Value Amount must be greater than or equal to 1998 for Instalment Sepa</b>");
		}else{
			waitForElementVisible(shopAlert);
			scrollToElement(mainFrame);
		}
		return map;
	}

	public Map<String, Object> setInvoiceGuaranteeConfiguration(
			boolean paymentActive,
			boolean testMode,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("AuthorizeMinAmount", authMinAmount);
		ExtentTestManager.logMessage("<b>Invoice with payment guarantee payment Configuration : </b>" + printMap(map));
		clickElement(invoiceGuarantee);
		waitForElementVisible(invoiceGuaranteeEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, allowB2B};
		List<By> by = new ArrayList<>();
		by.add(invoiceGuaranteeEnablePaymentCheckbox);
		by.add(invoiceGuaranteeTestModeCheckbox);
		by.add(invoiceGuaranteeAllowB2B);

		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}


		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(invoiceGuaranteeAuthorizeMinAmount, authMinAmount);
		}

		scrollToElement(invoiceGuaranteePaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(invoiceGuaranteePaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setInvoiceGuaranteeWithMinOrderAmountConfiguration(
			boolean paymentActive,
			boolean testMode,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount, String minOrderAmount) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("AuthorizeMinAmount", authMinAmount);
		map.put("MinOrderAmount", minOrderAmount);
		ExtentTestManager.logMessage("<b>Invoice with payment guarantee payment Configuration : </b>" + printMap(map));
		clickElement(invoiceGuarantee);
		waitForElementVisible(invoiceGuaranteeEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, allowB2B};
		List<By> by = new ArrayList<>();
		by.add(invoiceGuaranteeEnablePaymentCheckbox);
		by.add(invoiceGuaranteeTestModeCheckbox);
		by.add(invoiceGuaranteeAllowB2B);

		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}


		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(invoiceGuaranteeAuthorizeMinAmount, authMinAmount);
			}
		setText(invoiceGuaranteeMinOrderAmount, minOrderAmount);

		scrollToElement(invoiceGuaranteePaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(invoiceGuaranteePaymentTitle).trim());

		clickElement(saveChangesBtn);
		if(Integer.parseInt(minOrderAmount)<999) {
			ExtentTestManager.addScreenShot("<b>Minimum Order Value Amount must be greater than or equal to 999 for Guarantee Invoice</b>");
		}else{
			waitForElementVisible(shopAlert);
			scrollToElement(mainFrame);
		}
		return map;
	}

	public Map<String, Object> setInvoiceGuaranteeWithMinOrderAmountConfiguration(
			boolean paymentActive,
			boolean testMode,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount, String minOrderAmount, boolean forceNonGuarantee) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("AuthorizeMinAmount", authMinAmount);
		map.put("MinOrderAmount", minOrderAmount);
		ExtentTestManager.logMessage("<b>Invoice with payment guarantee payment Configuration : </b>" + printMap(map));
		clickElement(invoiceGuarantee);
		waitForElementVisible(invoiceGuaranteeEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, allowB2B, forceNonGuarantee};
		List<By> by = new ArrayList<>();
		by.add(invoiceGuaranteeEnablePaymentCheckbox);
		by.add(invoiceGuaranteeTestModeCheckbox);
		by.add(invoiceGuaranteeAllowB2B);
		by.add(invoiceGuaranteeForceNonGuarantee);

		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}


		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(invoiceGuaranteeAuthorizeMinAmount, authMinAmount);
		}
		setText(invoiceGuaranteeMinOrderAmount, minOrderAmount);

		scrollToElement(invoiceGuaranteePaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}




		map.put("PaymentTitle", getInputFieldText(invoiceGuaranteePaymentTitle).trim());

		clickElement(saveChangesBtn);
		if(Integer.parseInt(minOrderAmount)<999) {
			ExtentTestManager.addScreenShot("<b>Minimum Order Value Amount must be greater than or equal to 999 for Guarantee Invoice</b>");
		}else{
			waitForElementVisible(shopAlert);
			scrollToElement(mainFrame);
		}
		return map;
	}

	public Map<String, Object> setInstalmentInvoiceGuaranteeConfiguration(
			boolean paymentActive,
			boolean testMode,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("AuthorizeMinAmount", authMinAmount);
		ExtentTestManager.logMessage("<b>Instalment Invoice payment Configuration : </b>" + printMap(map));
		clickElement(instalmentInvoice);
		waitForElementVisible(instalmentInvoiceEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, allowB2B};
		List<By> by = new ArrayList<>();
		by.add(instalmentInvoiceEnablePaymentCheckbox);
		by.add(instalmentInvoiceTestModeCheckbox);
		by.add(instalmentInvoiceAllowB2B);

		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}


		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(instalmentInvoiceAuthorizeMinAmount, authMinAmount);
		}

		scrollToElement(instalmentInvoicePaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(instalmentInvoicePaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public Map<String, Object> setInstalmentInvoiceWithMinOrderAmountGuaranteeConfiguration(
			boolean paymentActive,
			boolean testMode,
			boolean allowB2B,
			String paymentAction,
			String authMinAmount,String minOrderAmt) {
		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("TestMode", testMode);
		map.put("AllowB2B", allowB2B);
		map.put("PaymentAction", paymentAction);
		map.put("AuthorizeMinAmount", authMinAmount);
		map.put("MinOrderAmount", minOrderAmt);
		ExtentTestManager.logMessage("<b>Instalment Invoice payment Configuration : </b>" + printMap(map));
		clickElement(instalmentInvoice);
		waitForElementVisible(instalmentInvoiceEnablePaymentCheckbox);

		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, allowB2B};
		List<By> by = new ArrayList<>();
		by.add(instalmentInvoiceEnablePaymentCheckbox);
		by.add(instalmentInvoiceTestModeCheckbox);
		by.add(instalmentInvoiceAllowB2B);

		for (int i = 0; i < key.length; i++) {
			if (key[i]) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (!key[i]) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}


		if (!paymentAction.equals("")) {
			if (!verifyElementTextEquals(getElementText(paymentActionDropDown), paymentAction)) {
				clickElementWithAction(paymentActionDropDown);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(instalmentInvoiceAuthorizeMinAmount, authMinAmount);
		}
		setText(instalmentInvoiceMinOrderAmount, minOrderAmt);

		scrollToElement(instalmentInvoicePaymentTitle);

		if (!getElementText(successOrderStatus).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatus);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}


		map.put("PaymentTitle", getInputFieldText(instalmentInvoicePaymentTitle).trim());

		clickElement(saveChangesBtn);
		if(Integer.parseInt(minOrderAmt)<1998) {
			ExtentTestManager.addScreenShot("<b>Minimum Order Value Amount must be greater than or equal to 1998 for Instalment Invoice</b>");
		}else{
			waitForElementVisible(shopAlert);
			scrollToElement(mainFrame);
		}
		return map;
	}



	// added generic function to setPaymentConfiguration
	public void setGlobalPaymentConfiguration(
			Map<String, By> locators,
			Map<String, Object> options
	) {

		ExtentTestManager.logMessage("<b>Payment Backend Configuration: </b><br>" + printMap(options));
		clickElement(locators.get("paymentMethodLocator"));
		waitForElementVisible(locators.get("enablePaymentCheckbox"));
		scrollToElement(mainFrame);

		Boolean paymentActive = options.get("paymentActive") != null ? (Boolean) options.get("paymentActive") : null;
		Boolean testMode = options.get("testMode") != null ? (Boolean) options.get("testMode") : null;
		Boolean allowB2B = options.get("allowB2B") != null ? (Boolean) options.get("allowB2B") : null;
		Boolean oneClick = options.get("oneClick") != null ? (Boolean) options.get("oneClick") : null;
		Boolean inlineForm = options.get("inlineForm") != null ? (Boolean) options.get("inlineForm") : null;
		Boolean enforce3D = options.get("enforce3D") != null ? (Boolean) options.get("enforce3D") : null;

		Boolean[] keys = {
				paymentActive,
				testMode,
				allowB2B,
				oneClick,
				inlineForm,
				enforce3D
		};

		List<String> checkboxKeys = Arrays.asList(
				"enablePaymentCheckbox",
				"testModeCheckbox",
				"allowB2BCheckbox",
				"oneClickCheckbox",
				"inlineFormCheckbox",
				"enforce3DCheckbox"
		);

		for (int i = 0; i < keys.length; i++) {
			String checkboxKey = checkboxKeys.get(i);
			Boolean checkboxValue = keys[i];

			if (checkboxValue != null) {
				By checkboxLocator = locators.get(checkboxKey);
				if (checkboxLocator != null) {
					if (checkboxValue) {
						if (!checkElementChecked(checkboxLocator)) {
							clickElement(checkboxLocator);
						}
					} else {
						if (checkElementChecked(checkboxLocator)) {
							clickElement(checkboxLocator);
						}
					}
				}
			}
		}



		String paymentAction = (String) options.get("paymentAction");
		By paymentActionElement = locators.get("paymentActionElement");
		if (!paymentAction.isEmpty()) {
			if (!verifyElementTextEquals(getElementText(paymentActionElement), paymentAction)) {
				clickElementWithAction(paymentActionElement);
				setExpectedCondition(d->d.findElement(By.cssSelector("select[id$='_payment_status'] + span .select2-selection--single")).getAttribute("aria-expanded").equals("true"),
						1,"Wait for payment action dropdown appear");
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElementWithAction(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(locators.get("minAuthAmountInputField"), (String) options.get("minAuthAmount"));
		}

		String dueDate = (String) options.get("dueDate");
		By dueDateInputFieldLocator = locators.get("dueDateInputField");
		if (dueDateInputFieldLocator != null && dueDate != null) {
			setText(dueDateInputFieldLocator, dueDate);
		}


		options.put("PaymentTitle", getInputFieldText(locators.get("paymentTitleInputField")).trim());

		// Set Success Order Status
		By successOrderStatusElement = locators.get("successOrderStatusElement");
		if (successOrderStatusElement != null && getElementText(successOrderStatusElement) != null
				&& !getElementText(successOrderStatusElement).equals(COMPLETION_ORDER_STATUS)) {
			clickElementWithAction(successOrderStatusElement);
			String path = "ul[id$='order_success_status-results']>li[id$='-" + COMPLETION_ORDER_STATUS.trim().toLowerCase() + "']";
			clickElementWithAction(By.cssSelector(path));
		}

		clickElement(locators.get("saveButton"));
		waitForElementVisible(locators.get("shopAlertElement"));
		scrollToElement(mainFrame);

	}


	@Step("Set Payment Configuration for Payment active = {0}, payment action = {1}, minimum authorize amount = {2}, test mode = {3}, one click = {4}, inline form = {5}, enforce3D = {6},paymentType = {7}")
	public Map<String, Object> setPaymentConfiguration(boolean paymentActive, String paymentAction, String minAuthAmount, Boolean testMode, Boolean oneClick, Boolean inlineForm, Boolean enforce3D,String dueDate, By paymentType) {
		Map<String, Object> actions = new HashMap<>();
		actions.put("paymentActive", paymentActive);
		actions.put("paymentAction", paymentAction);
		actions.put("minAuthAmount", minAuthAmount);
		actions.put("testMode", testMode);
		actions.put("oneClick", oneClick);
		actions.put("inlineForm", inlineForm);
		actions.put("enforce3D", enforce3D);
		actions.put("dueDate", dueDate);
		ExtentTestManager.logMessage("<b>CreditCard Backend Configuration : </b><br>" + printMap(actions));
		AllureManager.attachHtml("<b>CreditCard Backend Configuration : </b><br>" + printMap(actions));

		Map<String, By> locators = new HashMap<>();
		locators.put("paymentMethodLocator", paymentType);
		locators.put("enablePaymentCheckbox", EnablePaymentCheckbox);
		locators.put("testModeCheckbox", TestModeCheckbox);
		locators.put("allowB2BCheckbox", null);
		locators.put("oneClickCheckbox", OneClickCheckbox);
		locators.put("inlineFormCheckbox", InlineForm);
		locators.put("enforce3DCheckbox", Enforce3D);
		locators.put("paymentActionElement", paymentActionDropDown);
		locators.put("paymentTitleInputField", PaymentTitle);
		locators.put("minAuthAmountInputField", AuthorizeMinAmount);
		locators.put("dueDateInputField", DueDate);
		locators.put("successOrderStatusElement", null);
		locators.put("completionOrderStatusElement", null);
		locators.put("saveButton", saveChangesBtn);
		locators.put("shopAlertElement", shopAlert);

		setGlobalPaymentConfiguration(locators, actions);

		return actions;
	}

	public By getPayment(String paymentType) {
		switch (paymentType) {
			case PAYPAL:
				return By.cssSelector("tr[data-gateway_id$='_paypal'] td.name a");
			case CREDITCARD:
				return By.cssSelector("tr[data-gateway_id$='_cc'] td.name a");
			case IDEAL:
				return By.cssSelector("tr[data-gateway_id$='_ideal'] td.name a");
			case MULTIBANCO:
				return By.cssSelector("tr[data-gateway_id$='_multibanco'] td.name a");
			case PREPAYMENT:
				return By.cssSelector("tr[data-gateway_id$='_prepayment'] td.name a");
			case CASHPAYMENT:
				return By.cssSelector("tr[data-gateway_id='novalnet_barzahlen'] td.name a");
			case ONLINE_TRANSFER:
				return By.cssSelector("tr[data-gateway_id='novalnet_instantbank'] td.name a");
			case GOOGLEPAY:
					return By.cssSelector("tr[data-gateway_id$='_googlepay'] td.name a");
			default:
				throw new IllegalArgumentException("Invalid payment method: " + paymentType);
		}
	}


	private By getPaymentToggle(String paymentType) {
		switch (paymentType) {
			case PAYPAL:
				return By.cssSelector("tr[data-gateway_id$='paypal'] td.status>a>span");
			case CREDITCARD:
				return By.cssSelector("tr[data-gateway_id$='_cc'] td.status>a>span");
			case IDEAL:
				return By.cssSelector("tr[data-gateway_id$='_ideal'] td.status>a>span");
			case MULTIBANCO:
				return By.cssSelector("tr[data-gateway_id$='_multibanco'] td.status>a>span");
			case PREPAYMENT:
				return By.cssSelector("tr[data-gateway_id$='_prepayment'] td.status>a>span");
			case CASHPAYMENT:
				return By.cssSelector("tr[data-gateway_id$='novalnet_barzahlen'] td.status>a>span");
			case DIRECT_DEBIT_SEPA:
				return By.xpath("//tr[contains(@data-gateway_id, 'novalnet_sepa')]/td[@class='status']/a/span");
			case GUARANTEED_DIRECT_DEBIT_SEPA:
				return By.cssSelector("tr[data-gateway_id$='guaranteed_sepa'] td.status>a>span");
			case APPLEPAY:
				return By.cssSelector("tr[data-gateway_id$='applepay'] td.status>a>span");
			case GOOGLEPAY:
				return By.cssSelector("tr[data-gateway_id$='googlepay'] td.status>a>span");
			case INVOICE:
				return By.cssSelector("tr[data-gateway_id$='invoice'] td.status>a>span");
			case GUARANTEED_INVOICE:
				return By.cssSelector("tr[data-gateway_id$='guaranteed_invoice'] td.status>a>span");
			case ALIPAY:
				return By.cssSelector("tr[data-gateway_id$='alipay'] td.status>a>span");
			case ONLINE_BANK_TRANSFER:
				return By.cssSelector("tr[data-gateway_id$='online_bank_transfer'] td.status>a>span");
			case GIROPAY:
				return By.cssSelector("tr[data-gateway_id$='giropay'] td.status>a>span");
			case BANCONTACT:
				return By.cssSelector("tr[data-gateway_id$='bancontact'] td.status>a>span");
			case PRZELEWY24:
				return By.cssSelector("tr[data-gateway_id$='przelewy24'] td.status>a>span");
			case EPS:
				return By.cssSelector("tr[data-gateway_id$='eps'] td.status>a>span");
			case INSTALMENT_INVOICE:
				return By.cssSelector("tr[data-gateway_id$='instalment_invoice'] td.status>a>span");
			case INSTALMENT_DIRECT_DEBIT_SEPA:
				return By.cssSelector("tr[data-gateway_id$='instalment_sepa'] td.status>a>span");
			case POSTFINANCE_CARD:
				return By.cssSelector("tr[data-gateway_id$='postfinance_card'] td.status>a>span");
			case ONLINE_TRANSFER:
				return By.cssSelector("tr[data-gateway_id$='instantbank'] td.status>a>span");
			case WECHATPAY:
				return By.cssSelector("tr[data-gateway_id$='wechatpay'] td.status>a>span");
			case TRUSTLY:
				return By.cssSelector("tr[data-gateway_id$='trustly'] td.status>a>span");
			default:
				throw new IllegalArgumentException("Invalid payment method: " + paymentType);
		}
	}

	public void activatePayment(String paymentType, boolean activate){
		By paymentToggle = getPaymentToggle(paymentType);
		var currentStatus = !getElement(paymentToggle).getAttribute("class").contains("--disabled");
		if(currentStatus != activate){
			clickElement(paymentToggle);
			sleep(1.5);
		}
	}


	@Step("Set Payment Configuration for CreditCard active = {0}, payment action = {1}, minimum authorize amount = {2}, test mode = {3}, one click = {4}, inline form = {5}, enforce3D = {6}")
	public Map<String, Object> setGpayPaymentConfiguration(
			boolean paymentActive,
			String paymentAction,
			String minAuthAmount,
			boolean testMode,
			boolean enforce3D) {

		Map<String, Object> map = new HashMap<>();
		map.put("PaymentActive", paymentActive);
		map.put("PaymentAction", paymentAction);
		map.put("MinimumAuthAmount", minAuthAmount);
		map.put("TestMode", testMode);
		map.put("Enforce3D", enforce3D);
		ExtentTestManager.logMessage("<b>CreditCard Backend Configuration : </b><br>" + printMap(map));
		AllureManager.attachHtml("<b>CreditCard Backend Configuration : </b><br>" + printMap(map));
		clickElement(gpayPayment);
		waitForElementVisible(gpayEnabledCheckBox);
		scrollToElement(mainFrame);
		boolean key[] = {paymentActive, testMode, enforce3D};
		List<By> by = new ArrayList<>();
		by.add(gpayEnabledCheckBox);
		by.add(gpayTestModeCheckBox);
		by.add(gpayEnforce3dCheckBox);
		for (int i = 0; i < key.length; i++) {
			if (key[i] == true) {
				if (!checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
			if (key[i] == false) {
				if (checkElementChecked(by.get(i)))
					clickElement(by.get(i));
			}
		}
		if (!paymentAction.equals("") && !paymentAction.equals(null)) {
			if (!verifyElementTextEquals(getElementText(gpayPaymentAction), paymentAction)) {
				scrollToElement(gpayPaymentTitle);
				clickElementWithAction(gpayPaymentAction);
				String path = "ul[id$='payment_status-results']>li[id$='-" + paymentAction.trim().toLowerCase() + "']";
				clickElement(By.cssSelector(path));
			}
		}

		if (paymentAction.equals(AUTHORIZE)) {
			setText(gpayAuthorizMinimumAmount, minAuthAmount);
		}


		map.put("PaymentTitle", getInputFieldText(gpayPaymentTitle).trim());

		clickElement(saveChangesBtn);
		waitForElementVisible(shopAlert);
		scrollToElement(mainFrame);
		return map;
	}

	public void validateGlobalConfigFields(){
		String defaultAPIValue = DriverActions.getElementAttributeText(novalnetApiKeyInput,"value");
		DriverActions.verifyEquals(defaultAPIValue,"");

	}

}
