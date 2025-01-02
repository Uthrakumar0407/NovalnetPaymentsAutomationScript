package com.nn.pages;

import com.nn.helpers.ExcelHelpers;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.aventstack.extentreports.Status;
import com.nn.constants.Constants;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MyAccountPage {
	
	private By woocommerceInfo = By.cssSelector(".woocommerce-info");
	private By userName = By.cssSelector("#username");
	private By password = By.cssSelector("#password");
	private By loginBtn = By.cssSelector(".woocommerce-form-login__submit");
	private By myAccountOptionsDiv = By.cssSelector(".has-global-padding.is-layout-constrained.entry-content");
	private By myAccount = By.linkText("My account");
	private By orders = By.linkText("Orders");
	private By followupComments = By.cssSelector(".woocommerce-OrderUpdate-description");
	private By subscription = By.linkText("My Subscription");
	private By ordersTable = By.cssSelector(".woocommerce-orders-table");
	private By orderDetails = By.cssSelector(".woocommerce-order-details");
	private By subscriptionTable = By.cssSelector(".shop_table.subscription_details");
	private By orderStatus = By.cssSelector("mark.order-status");
	private By orderNextPageBtn = By.cssSelector("a[class$='--next button']");
	private By novalnetComments = By.xpath("//th[text()='Note:']/following-sibling::td");
	private By totalAmount = By.cssSelector(".woocommerce-order-overview__total .woocommerce-Price-amount");
	private By paymentNameInTable = By.xpath("//th[text()='Payment method:']/following-sibling::td");
	private By paymentTokenDeleteBtn = By.cssSelector(".button.delete");
	//private By woocommerceAlert = By.cssSelector(".woocommerce-message");

	private By woocommerceAlert = By.xpath("//div[contains(@class,'banner is-success')] | //div[contains(@class,'woocommerce-message')]");
	private By noPaymentTokenInfo = By.cssSelector(".woocommerce-Message--info");
	private By relatedOrdersInsideOrder = By.cssSelector("table[class$=-orders-table--subscriptions]");
	private By subscriptionStatusrelatedOrdersInsideOrder = By.cssSelector("td.subscription-status");
	private By pendingOrderNumber = By.cssSelector("tr[class$='status-pending order'] td[class$='cell-order-number'] a");
	private By failedOrderNumber = By.cssSelector("tr[class$='status-failed order'] td[class$='cell-order-number'] a");
	private By failedOrderPayBtn = By.cssSelector("a[href*='pay_for_order']");
	
	//Subscription
	private By subscriptionOrderHistoryStatus = By.xpath("//td[text()='Status']/following-sibling::td");
	private By subscriptionOrderHistoryLastOrderDate = By.xpath("//td[text()='Last order date']/following-sibling::td");
	private By subscriptionOrderHistoryNextPaymentDate = By.xpath("//td[text()='Next payment date']/following-sibling::td");
	private By subscriptionOrderHistoryEndDate = By.xpath("//td[text()='End date']/following-sibling::td");
	private By subscriptionOrderHistoryPaymentName = By.xpath("//td[text()='Payment']/following-sibling::td/span");
	private By subscriptionOrderHistoryChangePayment = By.cssSelector(".change_payment_method");
	private By relatedOrdersInsideSubscription = By.cssSelector("table[class$=-orders-table--subscriptions]");
	private By OrderHistoryStatusrelatedOrdersInsideSubscription = By.cssSelector("td.order-status");
	private By subscriptionCancelBtn = By.cssSelector(".button.cancel");
	private By subscriptionCancelReasonDropdown = By.cssSelector("#novalnet_subscription_cancel_reason");
	private By subscriptionCancelConfirmBtn = By.cssSelector("#novalnet_cancel");
	private By subscriptionRenewal = By.cssSelector(".subscription_renewal_early");
	

	public MyAccountPage login() {
		setText(userName, "test");
		setText(password, "novalnet123");
		clickElement(loginBtn);
		waitForElementVisible(myAccountOptionsDiv);
		return this;
	}
	
	
	public void openOrders() {
		clickElement(myAccount);
		waitForTitleContains("My account");
		clickElement(orders);
		waitForElementVisible(ordersTable);
	}
	
	public void openSubscription() {
		clickElement(myAccount);
		waitForTitleContains("My account");
		clickElement(subscription);
		waitForElementVisible(subscriptionTable);
	}

	@Step("Load My Account Orders")
	public void loadOrders() {
		openURL(Constants.URL_FRONTEND+"index.php/my-account/orders/");
		waitForTitleContains("My account");
		waitForElementVisible(ordersTable);
	}
	
	public void loadSubscription() {
		openURL(Constants.URL_FRONTEND+"index.php/my-account/subscriptions/");
		waitForTitleContains("My account");
		waitForElementVisible(ordersTable);
	}
	
	public void loadPaymentTokens() {
		openURL(Constants.URL_FRONTEND+"index.php/my-account/payment-methods");
		waitForTitleContains("My account");
		waitForElementVisible(myAccountOptionsDiv);
	}
	
	public void deletePaymentTokens() {
		List<WebElement> tokens = getElements(paymentTokenDeleteBtn);
		for(int i=0;i<tokens.size();i++) {
			clickElement(paymentTokenDeleteBtn);
			waitForElementVisible(woocommerceAlert);
		}
	}
	
	public String getOrderListingStatus(String orderNumber) {
		//a[contains(text(),'792')]/../following-sibling::td[contains(@class,'-status')]
		//a[contains(text(),'')]/../following-sibling::td[contains(@class,'-next-payment')]/small
		if(selectOrder(orderNumber)) {
			String xpath = "//a[contains(text(),'"+orderNumber+"')]/../following-sibling::td[contains(@class,'-status')]";
			var status = getElementText(By.xpath(xpath));
			highlightElement(By.xpath(xpath));
			return status;
		}
		return null;
	}
	
	public String getSubscriptionOrderListingPaymentName(String orderNumber) {
		//a[contains(text(),'792')]/../following-sibling::td[contains(@class,'-status')]
		//a[contains(text(),'')]/../following-sibling::td[contains(@class,'-next-payment')]/small
		if(selectOrder(orderNumber)) {
			String xpath = "//a[contains(text(),'"+orderNumber+"')]/../following-sibling::td[contains(@class,'-next-payment')]/small";
			var status = getElementText(By.xpath(xpath));
			highlightElement(By.xpath(xpath));
			return status;
		}
		return null;
	}
	
	public String getSubscriptionNextPayment(String orderNumber) {
		if(selectOrder(orderNumber)) {
			String xpath = "//a[contains(text(),'"+orderNumber+"')]/../following-sibling::td[contains(@class,'cell-order-date')]";
			var status = getElementAttributeText(By.xpath(xpath), "innerText").split("\\r?\\n|\\r")[0];
			highlightElement(By.xpath(xpath));
			return status;
		}
		return null;
	}
	
	public void clickChangePaymentBtn() {
		clickElement(subscriptionOrderHistoryChangePayment);
		waitForElementVisible(woocommerceInfo);
	}
	
	public void verifyOrderListingStatus(String orderNumber,String status) {
		verifyAssertEquals(getOrderListingStatus(orderNumber),status,"<b>Front-end order listing page status: </b>");
	}
	
	
	public void verifySubscriptionOrderListingPaymentName(String orderNumber,String paymentName) {
		verifyContainsAssert(getSubscriptionOrderListingPaymentName(orderNumber),paymentName,"<b>Shop Frontend Order listing page paymentName:</b>");
	}
	
	public boolean verifyFollowupCommentsExist() {
		boolean status = checkElementExist(followupComments);
		Log.info("Shop frontend followup comments status:"+status);
		ExtentTestManager.logMessage("<b>Shop frontend followup comments exist:</b> ["+status+"]");
		return status;
	}
	
	
	
	public String getOrderHistoryPageStatus() {
		var status = getElementText(orderStatus).trim();
		highlightElement(orderStatus);
		return status;
	}
	
	public void verifyOrderHistoryPageStatus(String status) {
		verifyAssertEquals(getOrderHistoryPageStatus(),status,"<b>Front-end order detail page status: </b>");
	}
	
	public void verifyOrderHistorySubscriptionStatus(String status) {
		verifyAssertEquals(getElementText(subscriptionOrderHistoryStatus),status,"<b>Shop Frontend Order history page Subscription status</b>");
	}
	
	public void verifyPaymentNameInsideSubscription(String paymentName) {
		String actual = "";
		if(checkElementExist(subscriptionOrderHistoryPaymentName)) {
			actual = getElementText(subscriptionOrderHistoryPaymentName);
			verifyContainsAssert(actual, paymentName,"<b>Shop Frontend Order history page Subscription payment name</b>");
		}else {
			verifyContainsAssert(actual, paymentName,"<b>Shop Frontend Order history page Subscription payment name</b>");
			ExtentTestManager.addScreenShot("<b>Shop Frontend Order history page Subscription payment name does not exist</b>");
		}
	}
	
	public void verifySubscriptionStatusInsideOrdersPage(String status) {
		String actual = getElementText(subscriptionStatusrelatedOrdersInsideOrder);
		verifyContainsAssert(actual, status,"<b>Shop Frontend Order history page Subscription status inside orders page</b>");
	}
	
	public void verifyOrderStatusInsideSubscriptionPage(String status) {
		String actual = getElementText(OrderHistoryStatusrelatedOrdersInsideSubscription);
		verifyContainsAssert(actual, status,"<b>Shop Frontend Order history page Order status inside Subscription page</b>");
	}
	
	public void verifyRenewalOrderPresentInsideSubscriptionPage(String orderNumber) {
		boolean actual = checkElementDisplayed(By.linkText(orderNumber));
		verifyAssertEquals(actual, true,"<b>Shop Frontend renewal order inside Subscription page</b>");
	}
	
	public boolean selectOrder(String orderNumber) {
		By orderElement = By.partialLinkText(orderNumber);
		while(!checkElementExist(orderElement)) {
			if(checkElementExist(orderNextPageBtn)) {
				clickElementWithJs(orderNextPageBtn);
				waitForElementVisible(ordersTable);
			}
			else {
				ExtentTestManager.logMessage(Status.FAIL,"Order is not found for the order number "+orderNumber);
				Log.warn("Order is not found for the order number "+orderNumber);
				Assert.fail();
				return false;
			}
		}
		return true;
	}
	
	public void clickOrder(String orderNumber) {
		if(selectOrder(orderNumber)) {
			By orderElement = By.partialLinkText(orderNumber);
			if(checkElementExist(orderElement)) {
				clickElementWithJs(orderElement);
				waitForElementVisible(By.cssSelector(".woocommerce-customer-details"));
			}
		}
	}
	
	public void verifyOrderHistoryPageDetails(String novalnetComment, String paymentName) {
		scrollToElement(orderDetails);
		highlightElement(novalnetComments);
		String comments = getElementAttributeText(novalnetComments, "innerText");
		//ExtentTestManager.addScreenShot("<b>Shop frontend novalnet comments: ["+getElementText(novalnetComments)+"] </b>");
		verifyContainsAssert(novalnetComment, comments,"<b>Front-end Novalnet transaction comments:</b>");
		highlightElement(paymentNameInTable);
			verifyContainsAssert(paymentNameInTable, paymentName,"<b>Front-end payment name in order details: </b>");
	}

	public void verifyOrderHistoryPageNovalnetComment(String novalnetComment) {
		scrollToElement(orderDetails);
		highlightElement(novalnetComments);
		String comments = getElementAttributeText(novalnetComments, "innerText");
		//ExtentTestManager.addScreenShot("<b>Shop frontend novalnet comments: ["+getElementText(novalnetComments)+"] </b>");
		verifyContainsAssert(novalnetComment, comments,"<b>Front-end Novalnet transaction comments:</b>");
	}
	
	public String getFollowupComment(String commentStartWith) {
		String comment = "";
		if(checkElementExist(followupComments)) {
			List<WebElement> elements = getElements(followupComments);
			for(WebElement e :elements) {
				String note = e.getAttribute("innerText").trim();
				if(note.contains(commentStartWith) || commentStartWith.contains(note)) {
					comment = note;
					highlightElement(e);
					break;
				}
			}
			ExtentTestManager.logMessage("<b>Front-end novalnet followup comments: ["+comment+"] </b>");
		}
		return comment;
	}
	
	public void verifyFollowupCommentsWithRegex(String commentStart,String regex, String valueToVerify) {
		String orderNote = getFollowupComment(commentStart);
		if(!orderNote.isBlank()) {
			String value = getFirstMatchRegex(orderNote, regex);
			verifyAssertEquals(value.replaceAll("[^0-9]", ""), valueToVerify,"<b>Front-end novalnet followup comments verification with amount </b>");
		}else {
			Log.info("Front-end novalnet followup comments are not available");
			ExtentTestManager.logMessage("Front-end novalnet followup comments are not available");
		}
	}
	
	public void verifyFollowupCommentsDueDate(String commentStart, String valueToVerify) {
		String orderNote = getFollowupComment(commentStart);
		if(!orderNote.isBlank()) {
			String value = getFirstMatchRegex(orderNote, "(?:due date)(.*?)\\€");
			Date actual = getDateFromString("MMM dd, yyyy", value);
			Date expected = getDateFromString("yyyy-MM-dd", valueToVerify);
			verifyAssertEquals(actual, expected,"<b>Front-end novalnet followup comments verification with amount </b>");
			return;
		}
		Log.info("Front-end novalnet followup comments are not available");
		ExtentTestManager.logMessage("Front-end novalnet followup comments are not available");
	}
	
	public void verifyFollowupComments(String comments) {
		String actual = getFollowupComment(comments);

		if(comments.contains(CREDIT_COMMENT_)){
			verifyContainsAssert(comments, actual,"<b>Front-end novalnet followup comments:</b>");
		}
		else{
			verifyContainsAssert(actual, comments,"<b>Front-end novalnet followup comments:</b>");
		}
	}
	
	public String getPendingOrderNumber() {
		String pendingOrder = getElementText(pendingOrderNumber).replaceAll("[^0-9]", "");
		highlightElement(pendingOrderNumber);
		ExtentTestManager.addScreenShot("<b>Front-end pending order before executing callback event PAYMENT </b>");
		return pendingOrder;
	}
	
	public String getFailedOrderNumber() {
		String pendingOrder = getElementText(failedOrderNumber).replaceAll("[^0-9]", "");
		highlightElement(failedOrderNumber);
		ExtentTestManager.addScreenShot("<b>Front-end failed order before executing callback event Online Transfer Credit</b>");
		return pendingOrder;
	}
	
	public void clickFailedOrderPayBtn(String orderNumber) {
		String text = getElementAttributeText(failedOrderPayBtn, "href");
		if(text.contains(orderNumber)) {
			clickElement(failedOrderPayBtn);
			waitForTitleContains("Checkout");
			sleep(1.5);
		}
		else {
			Log.info("Invalid order number for Repayment of failed transaction");
			ExtentTestManager.logMessage(Status.FAIL,"Invalid order number for Repayment of failed transaction");
			Assert.fail();
		}
	}
	
	public void clickSubscriptionChangePayment() {
		clickElement(subscriptionOrderHistoryChangePayment);
		waitForTitleContains("Checkout");
		sleep(1.5);
	}
	

	public void verifyNovalnetSubscriptionCancelBtnDisplayed(boolean expected) {
		boolean novlnetCancelBtn = false;
		if(checkElementDisplayed(subscriptionCancelBtn))
			novlnetCancelBtn = getElementAttributeText(subscriptionCancelBtn, "href").contains("novalnet-api=novalnet_subscription_cancel");
		verifyAssertEquals(novlnetCancelBtn, expected,"<b>Front-end Novalnet subscription cancel button:</b>");
	}
	
	public void verifySubscriptionCancelBtnDisplayed(boolean expected) {
		boolean novlnetCancelBtn = checkElementDisplayed(subscriptionCancelBtn);
		verifyAssertEquals(novlnetCancelBtn, expected,"<b>Front-end subscription cancel button:</b>");
	}

	
	public void cancelSubscription() {
		clickElement(subscriptionCancelBtn);
		selectDropdownByText(subscriptionCancelReasonDropdown, "Other");
		clickElement(subscriptionCancelConfirmBtn);
		waitForElementVisible(woocommerceAlert);
	}
	
	public CheckoutPage clickSubscriptionRenewal() {
		clickElement(subscriptionRenewal);
		waitForElementVisible(woocommerceAlert);
		sleep(3);
		return new CheckoutPage();
	}

	@Step("Verify TID appended in the Instalment table")
	public void verifyInstalmentCycleTID(int cycle, String tid,String status){
		By table = By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table");
		WebElement tableEle = getElement(table);
		List<WebElement> headerEle = tableEle.findElements(By.cssSelector("thead th"));
		List<WebElement> rowEle = tableEle.findElements(By.cssSelector("tbody tr:nth-of-type("+cycle+")"));
		boolean tidExist = false;
		boolean statusUpdated = false;
		for(int i=0;i<headerEle.size();i++){
			if(headerEle.get(i).getText().contains("Novalnet transaction ID")){
				tidExist = rowEle.get(i).findElements(By.cssSelector("td")).get(i).getText().contains(tid);
			}
			if(headerEle.get(i).getText().contains("Status")){
				tidExist = rowEle.get(i).findElements(By.cssSelector("td")).get(i).getText().contains(status);
			}
		}
		verifyAssertEquals(tidExist, true,"<b>Verify transaction ID appended in Instalment Table:</b>");
		verifyAssertEquals(statusUpdated, true,"<b>Verify status appended in Instalment Table:</b>");
	}

	@Step("Verify Instalment Dates, TID and Status appended in the Instalment table")
	public void verifyInstalmentTable(int numberOfCycles, String tid){
		Map<String,String> cycleDates = getUpcomingMonthDates(numberOfCycles,"MMMM dd, yyyy");
		By table = By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table");
		WebElement tableEle = getElement(table);
		List<WebElement> headerEle = tableEle.findElements(By.cssSelector("thead th"));
		boolean cycleDateExist = true;
		for(int i=0;i<headerEle.size();i++){
			if(headerEle.get(i).getText().contains("Date")){
				List<WebElement> actualDateEle = tableEle.findElements(By.cssSelector("tbody tr td:nth-of-type("+i+1+")"));
				for(int j=0;j<actualDateEle.size();j++){
					if(!actualDateEle.get(j).getText().equals(cycleDates.get(String.valueOf(j+1)))){
						cycleDateExist = false;
					}
				}
			}
		}
		verifyAssertEquals(cycleDateExist, true,"<b>Verify cycle dates appended in Instalment Table:</b>");
		WebElement tidEle = tableEle.findElement(By.cssSelector("tbody tr:nth-of-type(1) td:nth-of-type(3)"));
		verifyAssertEquals(tidEle.getText().contains(tid), true,"<b>Verify transaction ID appended in Instalment Table:</b>");
		WebElement statusEle = tableEle.findElement(By.cssSelector("tbody tr:nth-of-type(1) td:nth-of-type(5)"));
		verifyAssertEquals(statusEle.getText().contains(Constants.COMPLETION_ORDER_STATUS), true,"<b>Verify Order status appended in Instalment Table:</b>");
	}

	@Step("Verify Instalment table cycle {0} status {1}")
	public void verifyInstalmentTableStatus(int whichCycle,String status){
		By table = By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table");
		verifyAssertEquals(getValueInTable(table,5,whichCycle), status,"<b>Verify Instalment Table cycle "+whichCycle+" status :</b>");
	}

	@Step("Verify Instalment table cycle {0} tid {1}")
	public void verifyInstalmentTableTID(int whichCycle,String tid){
		By table = By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table");
		verifyAssertEquals(getValueInTable(table,3,whichCycle), tid,"<b>Verify Instalment Table cycle "+whichCycle+" tid :</b>");
	}

	@Step("Set shipping address")
	public void setShippingAddress() {
		openURL(Constants.URL_FRONTEND+"index.php/my-account/edit-address/shipping/");
		waitForTitleContains("My account");
		waitForElementVisible(By.cssSelector(".woocommerce-address-fields"));
		setShippingAddress(ExcelHelpers.addressGuaranteeB2B());
		clickElement(By.cssSelector("button[name='save_address']"));
		waitForElementVisible(woocommerceAlert);
	}

	@Step("Enter billing address at checkout")
	private void setShippingAddress(Map<String,String> address){
		if(!getInputFieldText(By.xpath("//input[@id='shipping_first_name']")).trim().equals(address.get("FirstName"))){
			setText(By.xpath("//input[@id='shipping_first_name']"),address.get("FirstName"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='shipping_last_name']")).trim().equals(address.get("LastName"))){
			setText(By.xpath("//input[@id='shipping_last_name']"),address.get("LastName"));
		}
		clearText(By.xpath("//input[@id='shipping_company']"));

		if(!getInputFieldText(By.xpath("//input[@id='shipping_address_1']")).trim().equals(address.get("HouseNo")+" , "+address.get("Street"))){
			setText(By.xpath("//input[@id='shipping_address_1']"),address.get("HouseNo")+" , "+address.get("Street"));
		}
		clearText(By.xpath("//input[@id='shipping_address_2']"));
		if(!getInputFieldText(By.xpath("//input[@id='shipping_postcode']")).trim().equals(address.get("Zip"))){
			setText(By.xpath("//input[@id='shipping_postcode']"),address.get("Zip"));
		}
		if(!getInputFieldText(By.xpath("//input[@id='shipping_city']")).trim().equals(address.get("City"))){
			setText(By.xpath("//input[@id='shipping_city']"),address.get("City"));
		}
		if(!getElementText(By.xpath("//span[@id='select2-shipping_country-container']")).trim().equals(address.get("Country"))){
			clickElementByRefreshing(By.xpath("//span[@id='select2-shipping_country-container']"));
			String css = "input[aria-owns='select2-shipping_country-results']";
			setText(By.cssSelector(css), address.get("Country"));
			pressEnter(By.xpath("//span[@id='select2-shipping_country-container']"));
		}
		if(!getElementText(By.xpath("//span[@id='select2-shipping_state-container']")).trim().contains(address.get("State"))){
			clickElementByRefreshing(By.xpath("//span[@id='select2-shipping_state-container']"));
			String css = "input[aria-owns='select2-shipping_state-results']";
			setText(By.cssSelector(css), address.get("State"));
			pressEnter(By.xpath("//span[@id='select2-shipping_state-container']"));
		}

	}
	public boolean isDownloadProductDisplayed(){
		boolean displayed = checkElementDisplayed(By.cssSelector(".woocommerce-MyAccount-downloads-file"));
		return displayed;
	}

}
