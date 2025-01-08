package com.nn.pages.Magento;

import com.nn.apis.MagentoAPI_Helper;
import com.nn.drivers.DriverManager;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nn.utilities.DriverActions.*;
import static com.nn.callback.CallbackProperties.*;

public class SuccessPage {

	private By orderSuccessMessage= By.cssSelector("span.base[data-ui-id='page-title-wrapper']");

	private String successMessage="Thank you for your purchase!";

	private By orderNumber= By.cssSelector(".order-number");

	private By testOrder= By.cssSelector("div.box-content > div[style*='background:red']");

	private By testOrderBox= By.cssSelector("div.box-order-billing-method .box-content");

	private By totalOrderAmount= By.cssSelector("td.amount[data-th='Grand Total'] span.price");

	private Map<String, Object> testOrderDetails = new HashMap<>();



/*	@Step("Get success page subscription details")
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
		verifyContainsSoft(subscriptionOrderStatus, status,"<b>Success page subscription order status:</b>");
	}*/

	@Step("Verify payment name displayed in the success page")
	public void verifyPaymentNameDisplayed(String paymentTitle) {
		String novalComments = getElement(testOrderBox).getAttribute("innerText");
		verifyContains(novalComments, paymentTitle,"<b>Front-end success page payment name:</b>");
	}

	@Step("Verify invoice created in the success page")
	public void verifyInvoiceCreated(boolean expected) {
		boolean actual = checkElementExist(By.cssSelector(".items.order-links li:nth-of-type(2)>a"));
		verifyEquals(actual, expected,"<b>Front-end success page invoice created status:</b>");
	}

	@Step("Verify Novalnet Payment Comments")
	public SuccessPage verifyNovalnetCommentsDisplayed() {
		waitForElementVisible(orderNumber, 150);
		clickElementWithJs(orderNumber);
		String orderID = getOrderID();
		verifyEquals(!orderID.isEmpty(), true, "<b>Front-end Novalnet Order ID:</b>");
		if (!orderID.isEmpty()) {
			if (checkElementExist(testOrderBox)) {
				highlightElement(testOrderBox);
			}
		}
		verifyEquals(checkElementDisplayed(testOrderBox), true, "<b>Front-end success page Novalnet transaction comments:</b>");
		ExtentTestManager.logMessage(getElement(testOrderBox).getAttribute("innerText"));
		scrollToElement(testOrderBox);
		ExtentTestManager.addScreenShot("<b>Novalnet comments in the success page:</b>");
		return this;
	}


	public String getOrderID() {
		String orderURL = DriverActions.getURL();
		// Use regular expressions to extract the order ID from the URL
		Pattern pattern = Pattern.compile("order_id/(\\d+)/");
		Matcher matcher = pattern.matcher(orderURL);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}

	public String getOrderNumberForGuestOrder(){
		waitForElementVisible(By.cssSelector(".checkout-success>p>span"),120);
		return getElementText(By.cssSelector(".checkout-success>p>span"));
	}

	public String getOrderNumber(){
		return getElementText(By.cssSelector(".page-title>span")).split("#")[1].trim();
	}

	public String getInvoiceNumber(){
		clickElement(By.cssSelector("a[href*='order/invoice']"));
		return getElementText(By.cssSelector(".order-details-items.invoice .order-title>strong")).split("#")[1].trim();
	}


	public  Map<String, Object> storeTxnDetails(String output){
		String[] str = output.split("\\n");
		String commentWithoutPaymentName = Arrays.stream(str).skip(2).reduce((str1,str2)->str1+str2).get();
		//System.out.println("Comment without payment name: "+commentWithoutPaymentName);
		String totalAmt=getElementText(totalOrderAmount);
		testOrderDetails.put("NovalnetComments", commentWithoutPaymentName);
		// Store values in the map
		testOrderDetails.put("TestOrder", str[0].trim());
		testOrderDetails.put("PaymentMethod", str[1].trim());
		testOrderDetails.put("TID", str[2].trim().replaceAll("[^0-9.]", ""));
		if (!(totalAmt.isEmpty())) {
			testOrderDetails.put("Amount",(totalAmt.replaceAll("[^0-9]", "")));
			testOrderDetails.put("OrderCurrency",totalAmt.charAt(0));
		}
		return testOrderDetails;
	}

	@Step("Get SuccessPage Transaction Details")
	public Map<String,Object> getSuccessPageTransactionDetails() {

		String novalComments = getElement(testOrderBox).getAttribute("innerText");
		Map<String, Object> getTxnDetails = storeTxnDetails(novalComments);
		Map<String, Object> map = new HashMap<>();
		String orderNumber = getOrderID().trim();
		map.put("OrderNumber", orderNumber);
		String paymentNameInShop = getTxnDetails.get("PaymentMethod").toString();
		map.put("PaymentName", paymentNameInShop);
		String totalAmt = getTxnDetails.containsKey("Amount") ? getTxnDetails.get("Amount").toString() : "";
		map.put("TotalAmount", totalAmt);
		String orderCurrency = getTxnDetails.containsKey("OrderCurrency") ? getTxnDetails.get("OrderCurrency").toString() : "";
		map.put("OrderCurrency", orderCurrency);
		map.put("NovalnetComments", getTxnDetails.get("NovalnetComments").toString());
		map.put("TID", getTxnDetails.get("TID"));
		map.put("TestOrderText", getTxnDetails.get("TestOrder"));

		String[] comment = novalComments.split("\\r?\\n|\\r");
		System.out.println(Arrays.toString(comment));
		if(paymentNameInShop.equals("Prepayment") || paymentNameInShop.equals("Invoice") || paymentNameInShop.equals("Instalment by Invoice")) {
			if(!novalComments.contains("Your order is being verified. Once confirmed")) {
				if(comment[5].contains("following account on or before")) {
					map.put("DueDate", comment[5].split("following account on or before")[1].trim());
				}
				if(paymentNameInShop.equals("Instalment by Invoice")){
					map.put("CycleAmount", comment[5].split(map.get("OrderCurrency").toString())[0].replaceAll("[^0-9]", ""));
				}
				map.put("InvoiceAmount", comment[5].split(map.get("OrderCurrency").toString())[0].replaceAll("[^0-9]", ""));
				map.put("account_holder", comment[6].split(":")[1].trim());
				String[] bankSplit = comment[9].split(":");
				String[] bankWords = bankSplit[1].trim().split(" ");
				String bankPlace = bankWords[bankWords.length - 1];
				String bankName = bankSplit[1].substring(0, bankSplit[1].lastIndexOf(bankPlace)).trim();
				map.put("bank_name", bankName);
				map.put("bank_place", bankPlace);
				map.put("bank_name", bankName);
				map.put("bank_place", bankPlace);
				map.put("bic", comment[8].split(":")[1].trim());
				map.put("iban", comment[7].split(":")[1].trim());
				map.put("ReferenceOne", comment[11].split(":")[1].replaceAll("[^0-9]", ""));
				map.put("ReferenceTwo", comment[12].split(":")[1].trim());
			}else {
				map.put("GuaranteePendingText", comment[4].trim());
			}
		}
		Log.info("<b>Successpage novalnet comments: </b>" + map);
		ExtentTestManager.logMessage("<b>Successpage novalnet comments: </b>"+novalComments);
		AllureManager.saveLog("<b>Successpage novalnet comments: </b>"+novalComments);
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

	public String getPaymentComment(){
		return getElementAttributeText(By.cssSelector(".box-order-billing-method>div"),"innerText")
				.replaceAll("\n"," ")
				.replaceAll("\\s+"," ");
	}

	@Step("Verify Instalment table displayed expected {0}")
	public SuccessPage verifyInstalmentTableDisplayed(boolean expected){
		By table = By.cssSelector("div.box-order-billing-method table.table-order-items");
		verifyEquals(checkElementDisplayed(table),expected,"Verify Instalment table displayed");
		return this;
	}

	@Step("Verify Instalment table at the success page")
	public SuccessPage verifyInstalmentTable(String cycleAmount, int totalNumberOfCycles){
		List<WebElement> cycleAmounts = getElements(By.xpath("(//table[@class='data table table-order-items'])[2]//tr//td[3]"));
		List<WebElement> actualCycleCount = getElements(By.xpath("(//table[@class='data table table-order-items'])[2]//tbody/tr"));
		for(int i=0;i<cycleAmounts.size();i++){
			String actualcycleAmount = cycleAmounts.get(i).getText().replaceAll("[^0-9]", "");
			verifyEquals(actualcycleAmount,cycleAmount,"Verify Instalment cycle amount");
		}
		verifyEquals(actualCycleCount.size(),totalNumberOfCycles,"Verify Instalment cycle count");
		return this;
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
			case BLIK:
				return "Blik";
			case PAYCONIQ:
				return "Payconiq";
			case MBWAY:
				return "MB Way";
			default:
				throw new IllegalArgumentException("Invalid payment method: " + paymentType);
		}
	}

	/*public SuccessPage verifyDueDate(int dueDateInDays)  {

		Date date =null;
		String today = changePatternOfDate("yyyy-MM-dd", new Date());
		String expectedDueDate = addDaysFromDate(today, dueDateInDays);
		String text = getElementText(testOrderBox);
		Pattern datePattern = Pattern.compile("([a-zA-Z]+ \\d{1,2}, \\d{4})");
		//Pattern datePattern = Pattern.compile("(\\d{1,2} [a-zA-Z]+ \\d{4})");
		Matcher matcher = datePattern.matcher(text);
		if (matcher.find()) {
			String actaulDate = matcher.group(1);
			System.out.println(actaulDate);
			String originalDate = actaulDate;
			SimpleDateFormat originalDateFormat = new SimpleDateFormat("d MMMM yyyy");
         try {
	     date= originalDateFormat.parse(originalDate);
         }catch (ParseException e){
	     Log.info(e.getMessage());
		 Assert.fail("Date not found in the text");
        }

	    SimpleDateFormat desiredDateFormat = new SimpleDateFormat("yyyy-MM-dd");String actaulFormattedDate = desiredDateFormat.format(date);
	    verifyEquals(actaulFormattedDate,expectedDueDate,"Verify the dueDate in shop successPage");

        } else {
				Assert.fail("Due date not matched to the expected date");
			}
			return this;
		}*/

}
