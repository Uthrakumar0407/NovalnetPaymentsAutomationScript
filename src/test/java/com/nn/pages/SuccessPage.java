package com.nn.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

import com.nn.apis.GetTransactionDetailApi;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SuccessPage {
	
	//private By orderSuccessMessage = By.cssSelector(".woocommerce-notice--success");
	// private By orderDetails = By.cssSelector("div.woocommerce"); // commented  by Nizam
	//private By orderNumber = By.cssSelector("li[class$='order'] strong");

	private By orderSuccessMessage = By.xpath("//p[contains(@class,'thankyou-order-received')] | //p[contains(text(),'Thank you')]");

	private By orderNumber=By.xpath("//li[contains(., 'Order number:')]/descendant::span[contains(@class, 'wc-block-order-confirmation-summary-list-item__value')] | //li[contains(., 'Order number:')]/strong");
	//private By novalnetComments = By.xpath("//th[text()='Note:']/following-sibling::td");

	private By novalnetComments = By.xpath("//*[@class='wc-block-order-confirmation-order-note']");
	//private By totalAmount = By.cssSelector(".woocommerce-order-overview__total .woocommerce-Price-amount");
	private By totalAmount = By.xpath("//*[contains(concat(' ', normalize-space(@class), ' '), 'woocommerce-order-overview__total')]//*[contains(concat(' ', normalize-space(@class), ' '), 'woocommerce-Price-amount')] | (//span[@class='woocommerce-Price-amount amount'])[1]");

	//private By paymentName = By.cssSelector(".woocommerce-order-overview__payment-method strong");
	private By paymentName = By.xpath("//*[contains(concat(' ', normalize-space(@class), ' '), 'woocommerce-order-overview__payment-method')]//strong | //li//span[contains(text(),'Payment method')]/following-sibling::span");
	private By paymentNameInTable = By.xpath("//*[text()='Payment method:']//following-sibling::span");
	private By relatedOrders = By.cssSelector(".woocommerce-orders-table--subscriptions");
	private By subscriptionOrderNumber = By.cssSelector(".woocommerce-orders-table--subscriptions td[class$='order-number'] a");
	private By subscriptionOrderStatus = By.cssSelector(".woocommerce-orders-table--subscriptions td[class$='order-status']");
	private By subscriptionOrderNextPayment = By.cssSelector(".woocommerce-orders-table--subscriptions td[class*='subscription-next-payment']");
	private By subscriptionTotal = By.cssSelector(".subscription-total  .woocommerce-Price-amount");
	private By subscriptionViewBtn = By.cssSelector(".woocommerce-orders-table--subscriptions .woocommerce-button.button.view");

	private By orderDetails = By.xpath("//h1 | //h3"); // replaced for the commented // private By orderDetails = By.cssSelector("div.woocommerce"); // commented  by Nizam

	@Step("Get success page subscription details")
	public Map<String,Object> getSuccessPageSubscriptionDetails() {
		Map<String,Object> map = new HashMap<>();
		if(checkElementExist(relatedOrders)) {
			highlightElement(relatedOrders);
			map.put("SubscriptionOrderNumber", getElementText(subscriptionOrderNumber).trim().replaceAll("[^0-9]", ""));
			map.put("SubscriptionOrderStatus", getElementText(subscriptionOrderStatus).trim());
			map.put("SubscriptionOrderNextPayment", getElementText(subscriptionOrderNextPayment).trim());
			map.put("SubscriptionCycleAmount", getElementAttributeText(subscriptionTotal,"innerText").trim().replaceAll("[^0-9]", ""));
		}
		ExtentTestManager.logMessage(map.toString());
		return map;
	}
	
	public void verifySubscriptionStatus(String status) {
		//ExtentTestManager.logMessage("<b>Success page subscription order status actual: ["+getElementText(subscriptionOrderStatus)+"] and expected: ["+status+"]</b>");
		verifyContainsAssert(subscriptionOrderStatus, status,"<b>Success page subscription order status:</b>");
	}
	
	public void verifyPaymentNameDisplayed(String paymentTitle) {
		highlightElement(paymentNameInTable);
		verifyContainsAssert(paymentName, paymentTitle,"<b>Front-end success page payment name:</b>");
		verifyContainsAssert(paymentNameInTable, paymentTitle,"<b>Front-end success page payment name in table:</b>");
	}

	@Step("Verify Novalnet Payment Comments")
	public void verifyNovalnetCommentsDisplayed() {
		//ExtentTestManager.addScreenShot("<b>Success Page Screenshot:</b>");
		waitForElementVisible(orderSuccessMessage,120);
		scrollToElement(orderDetails);
		if(checkElementExist(novalnetComments)) {
			highlightElement(novalnetComments);
		}
		//ExtentTestManager.addScreenShot("<b>Novalnet comments in the success page: </b>");
		ExtentTestManager.addScreenShot("<b>Front-end success page screenshot:</b>");
		verifyEquals(checkElementDisplayed(novalnetComments), true, "<b>Front-end sucesspage Novalnet transaction comments:</b>");
		//ExtentTestManager.logMessage(getElementText(novalnetComments));
	}

	@Step("Get SuccessPage Order Details")
	public Map<String,Object> getSuccessPageTransactionDetails() {
		Map<String,Object> map = new HashMap<>();
		scrollToElement(orderNumber);
		map.put("OrderNumber", getElementText(orderNumber).trim());
		String paymentNameInShop = getElementText(paymentName).trim();
		map.put("PaymentName", paymentNameInShop);
		String totalAmt = getElementAttributeText(totalAmount,"innerText").trim();
		map.put("TotalAmount", Integer.parseInt(totalAmt.replaceAll("[^0-9]", "")));
		//char[] ch = totalAmt.toCharArray();
		map.put("OrderCurrency",  totalAmt.charAt(totalAmt.length()-1));
		
		String novalComments = getElementAttributeText(novalnetComments,"innerText");
		String[] comment = novalComments.trim().split("\\r?\\n|\\r");
		String tid = getFirstMatchRegex(comment[2], "(?:transaction ID:)\\s*(\\d{17})").replaceAll("[^0-9]", "");
		String testOrder = comment[3].trim();
		map.put("NovalnetComments", novalComments);
		map.put("TID", tid);
		map.put("TestOrderText", testOrder);
		
		if(paymentNameInShop.equals("Prepayment") || paymentNameInShop.equals("Invoice") || paymentNameInShop.equals("Instalment by Invoice")) {
			if(!comment[5].contains("Your order is under verification and we will soon update you with the order status")) {
				if(comment[5].contains("following account on or before")) {
					map.put("DueDate", comment[5].split("following account on or before")[1].trim());
				}
				if(paymentNameInShop.equals("Instalment by Invoice")){
					map.put("CycleAmount", getFirstMatchRegex(comment[5],"€(\\d+).(\\d{2})").replaceAll("[^0-9]", ""));
				}
				map.put("InvoiceAmount", getFirstMatchRegex(comment[5],"€(\\d+).(\\d{2})").replaceAll("[^0-9]", ""));
				map.put("account_holder", comment[7].split(":")[1].trim());
				map.put("bank_name", comment[8].split(":")[1].trim());
				map.put("bank_place", comment[9].split(":")[1].trim());
				map.put("bic", comment[11].split(":")[1].trim());
				map.put("iban", comment[10].split(":")[1].trim());
				map.put("ReferenceOne", comment[14].split(":")[1].replaceAll("[^0-9]", ""));
				map.put("ReferenceTwo", comment[15].split(":")[1].trim());
			}else {
				map.put("GuaranteePendingText", comment[5].trim());
			}
		}
		
		ExtentTestManager.logMessage("<b>Successpage novalnet comments: </b>"+novalComments);
		return map;
	}
	public Map<String,Object> getGuestOrderSuccessPageTransactionDetails() {
		Map<String,Object> map = new HashMap<>();
		scrollToElement(orderNumber);
		map.put("OrderNumber", getElementText(orderNumber).trim());
		String totalAmt = getElementAttributeText(totalAmount,"innerText").trim();
		map.put("TotalAmount", Integer.parseInt(totalAmt.replaceAll("[^0-9]", "")));
		//char[] ch = totalAmt.toCharArray();
		map.put("OrderCurrency",  totalAmt.charAt(totalAmt.length()-1));

		String novalComments = getElementAttributeText(novalnetComments,"innerText");
		String[] comment = novalComments.split("\\r?\\n|\\r");
		String tid = getFirstMatchRegex(comment[2], "(?:transaction ID:)\\s*(\\d{17})").replaceAll("[^0-9]", "");
		String testOrder = comment[1].trim();
		map.put("NovalnetComments", novalComments);
		map.put("TID", tid);
		map.put("TestOrderText", testOrder);
		ExtentTestManager.logMessage("<b>Successpage novalnet comments: </b>"+novalComments);
		return map;
	}
	public Map<String,Object> getSuccessPageBankDetails(Map<String,Object> map) {
		Map<String,Object> m = new HashMap<>();
		m.put("account_holder", map.get("account_holder"));
		m.put("bank_name", map.get("bank_name"));
		m.put("bank_place", map.get("bank_place"));
		m.put("bic", map.get("bic"));
		m.put("iban", map.get("iban"));
		return m;
	}
	public String getPaymentFromSuccessPage(String paymentType) {
		switch (paymentType) {
			case PAYPAL:
				return "PayPal";
			case CREDITCARD:
				return "Credit/Debit Cards";
			case IDEAL:
				return "iDEAL";
			case MULTIBANCO:
				return "Multibanco";
			case PREPAYMENT:
				return "Prepayment";
			case CASHPAYMENT:
				return "Cash Payment";
			case ONLINE_TRANSFER:
				return "Sofort";
			case ONLINE_BANK_TRANSFER:
				return "Online bank transfer";
			case INVOICE:
			case GUARANTEED_INVOICE:
				return "Invoice";
			case INSTALMENT_INVOICE:
				return "Instalment by invoice";
			case BANCONTACT:
				return "Bancontact";
			case EPS:
				return "eps";
			case GIROPAY:
				return "Giropay";
			case PRZELEWY24:
				return "Przelewy24";
			case POSTFINANCE_CARD:
				return "PostFinance Card";
			case TRUSTLY:
				return "Trustly";
			case ALIPAY:
				return "Alipay";
			case WECHATPAY:
				return "WeChat Pay";
			case DIRECT_DEBIT_SEPA:
			case GUARANTEED_DIRECT_DEBIT_SEPA:
				return "Direct Debit SEPA";
			case INSTALMENT_DIRECT_DEBIT_SEPA:
				return "Instalment by SEPA direct debit";
			case GOOGLEPAY:
				return "Google Pay";
			default:
				throw new IllegalArgumentException("Invalid payment method: " + paymentType);
		}
	}
	
}
