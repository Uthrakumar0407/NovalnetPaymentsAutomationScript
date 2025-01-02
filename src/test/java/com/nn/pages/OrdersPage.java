package com.nn.pages;

import com.nn.apis.MagentoAPI_Helper;
import com.nn.apis.TID_Helper;
import com.nn.callback.CallbackProperties;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.aventstack.extentreports.Status;
import com.nn.apis.GetTransactionDetailApi;
import com.nn.constants.Constants;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;


import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.*;

import java.util.*;

public class OrdersPage {
	
	//private By orderSearchInput = By.cssSelector("#post-search-input");
	private By orderSearchInput = By.cssSelector(".search-box>input[name='s']"); // update for new woocommerce shop
	private By orderSubmitBtn = By.cssSelector("#search-submit");
	private By orderNotes = By.cssSelector("ul.order_notes div.note_content");

	private By initialOrderorderNotes = By.xpath("(//div[@class='note_content'])[2]");
	private By orderNotesInAddress = By.cssSelector("div.address p.order_note");
	private By orderTotalAmount = By.xpath("//td[text()='Paid: ']/following-sibling::td/span/bdi");
	private By paymentNameUnderPaidBtn = By.cssSelector(".wc-order-totals .description");
	private By paymentNameOrderTop = By.cssSelector(".woocommerce-order-data__meta.order_number");
	private By TIDOrderTop = By.cssSelector(".woocommerce-order-data__meta.order_number a");
	private By refundBtn = By.cssSelector(".button.refund-items");
	private By refundAmountInput = By.cssSelector("#refund_amount");
	private By refundViaNovalnetBtn = By.cssSelector(".button.button-primary.do-api-refund");
	private By subscriptionRelatedOrders = By.cssSelector(".woocommerce_subscriptions_related_orders");
	private By subscriptionOrder = By.xpath("//td[contains(text(),'Subscription')]/preceding-sibling::td/a");
	private By subscriptionOrderListStatus = By.cssSelector(".subscription-status span");
	private By subscriptionRelatedOrderAmount = By.cssSelector(".woocommerce_subscriptions_related_orders .woocommerce-Price-amount");
	private By orderStatusDropdwon = By.cssSelector("#select2-order_status-container");
	private By orderUpdateBtn = By.cssSelector(".button.save_order.button-primary");
	private By refundOrderQtyInput = By.cssSelector(".refund_order_item_qty");
	private By refundTotalAmountInput = By.cssSelector(".refund_line_total.wc_input_price");
	private By shopLoader = By.cssSelector(".blockUI.blockOverlay");
	private By orderNotesContainer = By.cssSelector("#postbox-container-1");
	private By orderDivStart = By.cssSelector(".wp-heading-inline");
	private By orderUpdateMsg = By.cssSelector("#message");
	
	private By customerSelectContainer = By.cssSelector("#select2-customer_user-container");
	private By customerSearchInput = By.cssSelector("input[aria-owns='select2-customer_user-results']");
	private By customerSearchList = By.cssSelector("#select2-customer_user-results li[role='option']");
	private By paymentMethodDropdown = By.cssSelector("#_payment_method");
	private By sepaIbanInput = By.cssSelector("#novalnet_sepa_iban");
	private By addlineItem = By.cssSelector(".add-line-item");
	private By addProductBtn = By.cssSelector(".add-order-item");
	private By selectProductContainer = By.cssSelector("span[id^='select2-item_id']");
	private By searchProductInput = By.cssSelector("input[aria-owns^='select2-item_id-']");
	private By searchProductList = By.cssSelector("ul[id^='select2-item_id-'] li[role='option']");
	private By addBtn = By.cssSelector("#btn-ok");
	private By recalculateButton = By.cssSelector(".calculate-action");

	private By bookTransactionButton = By.id("novalnet_book_order_amount");

	private By zeroAmountBookingAmount =By.xpath("//input[@id='novalnet_book_amount']");
	private By successMessage = By.xpath("//div[@id='message']");

	private By novalnetComments =By.cssSelector(".order_notes li.customer-note div>p");
	
	
	public OrdersPage load() {
		openURL(Constants.URL+"edit.php?post_type=shop_order");
		waitForTitleContains("Orders");
		return this;
	}
	
	public void createOrderPageLoad() {
		openURL(Constants.URL+"post-new.php?post_type=shop_order");
		waitForTitleContains("Add new order");
	}
	
	public void selectBackendOrder(String orderNumber) {
		//tr[@id='post-780']
		//tr[@id='post-754']/td[contains(@class,'order_status')]/mark/span
		By order = By.partialLinkText(orderNumber);
		if (!checkElementExist(order)) {
			setText(orderSearchInput, orderNumber);
			clickElement(orderSubmitBtn);
		}
		clickElement(order);
		waitForElementVisible(By.cssSelector("#order_data"),20);
		scrollToElement(orderDivStart);
	}
	
	public void verifyOrderListingStatus(String orderNumber,String status) {
		var actualStatus = getBackendOrderListingStatus(orderNumber); 
		//ExtentTestManager.logMessage("<b>Shop backend order listing page status actual: ["+actualStatus+"] and expected : ["+status+"]</b>");
		verifyAssertEquals(actualStatus,status,"<b>Back-end order listing page status:</b>");
	}
	
	public String getBackendOrderListingStatus(String orderNumber) {
		//String orderRow = "//tr[@id='post-"+orderNumber.trim()+"']";
		String orderRow = "//a[@class='order-view']/strong[contains(.,'" +orderNumber.trim()+"')]"; // updated - for new woocommerce server shop

		By orderRowLoc = By.xpath(orderRow);
		String status;
		if(checkElementExist(orderRowLoc)) {
			scrollToElement(orderDivStart);
			//status = getElementText(By.xpath(orderRow+"/td[contains(@class,'order_status')]/mark/span"));
			status = getElementText(By.xpath(orderRow+"/ancestor::td/following-sibling::td[contains(@class,'order_status')]/mark/span")); // update for new woocommerce server shop

		}else {
			setText(orderSearchInput, orderNumber);
			clickElement(orderSubmitBtn);
			scrollToElement(orderDivStart);
			status = getElementText(By.xpath(orderRow+"/ancestor::td/following-sibling::td[contains(@class,'order_status')]/mark/span"));
		}
		//highlightElement(By.xpath(orderRow+"/ancestor::td/following-sibling::td[contains(@class,'order_status')]/mark/span"));
		//ExtentTestManager.addScreenShot("Shop Backend Order Listing status for order number "+orderNumber);
		//ExtentTestManager.logMessage("<b>Shop Backend Order Listing status ["+status+"] for order number "+orderNumber+"</b>");
		return status;
	}
	
	public void selectOrderStatus(String status) {
		if(!verifyElementTextEquals(getOrderStatus(), status)) {
			clickElementWithAction(orderStatusDropdwon);
			String xpath = "ul[id$='order_status-results']>li[id$='-"+status.trim().toLowerCase().replace(" ", "-")+"']";
			clickElement(By.cssSelector(xpath));
			clickElement(orderUpdateBtn);
			waitForElementVisible(orderUpdateMsg,20);
		}
	}

	public void cancelInstalment(String allOrRemaining) {
		By instalmentCancelBtn = By.cssSelector("#instalment_cancel");
		scrollToElement(instalmentCancelBtn);
		clickElement(instalmentCancelBtn);
		if(allOrRemaining.equals("REMAINING_CYCLES")){
			clickElement(By.cssSelector("#stop_upcoming_instalment"));
		}else if(allOrRemaining.equals("ALL_CYCLES")){
			clickElement(By.cssSelector("#entire_instalment_cancel"));
		}
		alertAccept();
		sleep(5);
	}
	
	public List<String> getOrderNotes() {
		List<WebElement>  orderNotesEle = getElements(initialOrderorderNotes);
		List<String> orderNotesList= new ArrayList<>();
		for(WebElement e : orderNotesEle )
			orderNotesList.add(e.getAttribute("innerText"));
		return orderNotesList;
	}
	
	public String getCustomerOrderNotes() {
		String notes = getElementAttributeText(orderNotesInAddress,"innerText");
		highlightElement(orderNotesInAddress);
		return notes;
	}
	
	public boolean isPaymentNameDisplayedInOrderTop(String paymentName) {
		return getElementText(paymentNameOrderTop).contains(paymentName);
	}
	
	public boolean isPaymentNameDisplayedInOrderBottom(String paymentName) {		
		return getElementText(paymentNameUnderPaidBtn).contains(paymentName);
	}
	
	public boolean verifyTIDOrderPageTop(String tid) {
		return verifyContainsAssert(TIDOrderTop,tid);
	}
	
	public boolean verifyPaymentNameDisplayedInOrderBottom() {
		return checkElementExist(paymentNameUnderPaidBtn);
	}
	
	public boolean verifyPaymentNameDisplayedInOrderTop(String tid) {
		return checkElementExist(paymentNameUnderPaidBtn);
	}
	
	public String getOrderStatus() {
		return getElementText(orderStatusDropdwon);
	}
	
	public void verifyOrderHistoryPageStatus(String status) {
		//ExtentTestManager.addScreenShot("Shop backend order history page status dropdown");
		var actualStatus= getOrderStatus();
		highlightElement(orderStatusDropdwon);
		//ExtentTestManager.logMessage("<b>Shop backend order history page status in dropdown actual: ["+actualStatus+"] and expected : ["+status+"]</b>");
		if(status.equals("Refunded")){
			waitForElementVisible(By.xpath("//span[text()='Refunded']"));
			actualStatus= getOrderStatus();
		}
		verifyAssertEquals(actualStatus,status,"<b>Back-end order detail page status:</b>");
	}
	
	public void verifyPaymentNameAndTID(String tid, String paymentName) {
		if(checkElementExist(TIDOrderTop)) {
			verifyAssertEquals(getElementText(TIDOrderTop).trim().replaceAll("[^0-9]", ""), tid,"<b>Back-end order detail page tid on top: </b>");
			highlightElement(TIDOrderTop);	
		}
		if(checkElementExist(paymentNameOrderTop)){
			verifyContainsAssert(getElementText(paymentNameOrderTop).trim(), paymentName,"<b>Back-end order detail page payment name on top: </b>");
			highlightElement(paymentNameOrderTop);	
		}
	}
	
	public void verifyPaymentNameAfterPaid(boolean expected, String paymentName) {
		boolean actual = checkElementExist(paymentNameUnderPaidBtn);
		if(actual){
			var payment = getElementText(paymentNameUnderPaidBtn).trim();
			if(expected == false) {
				verifyAssertEquals(payment.contains(paymentName), expected, "<b>Back-end order detail payment name exist under order paid text after amount paid: </b>");
				return;
			}
			verifyContainsAssert(payment, paymentName,"<b>Back-end order detail payment name exist under order paid text after amount paid: </b>");
			highlightElement(paymentNameUnderPaidBtn);	
		}else {
			verifyAssertEquals(actual, expected, "<b>Back-end order detail payment name exist under order paid text after amount paid: </b>");
		}

	}
	
	public boolean VerifyOrderNotesAndCustomerNotesSame() {
		List<String> notes = getOrderNotes();
		String customerNotes = getCustomerOrderNotes();
		for(String s : notes) {
			if(customerNotes.contains(s)) {
				Log.info("Novalnet comments in the order notes and customer notes is same the backend order history page");
				ExtentTestManager.logMessage(Status.PASS, "<b>Novalnet comments in the order notes and customer notes is same the backend order history page");
				return true;
			}
		}
		Log.info("Novalnet comments in the order notes and customer notes is not same the backend order history page");
		ExtentTestManager.logMessage(Status.WARNING, "<b>Novalnet comments in the order notes and customer notes is not same the backend order history page");
		return false;
	}
	
	public boolean VerifyNovalnetComments(String comments) {
		List<String> notes = getOrderNotes();
			if(notes.contains(comments)) {
				Log.info("Novalnet comments are in the backend order history page");
				ExtentTestManager.logMessage(Status.PASS, "Novalnet comments are in the backend order history page");
				return true;
			}
			Log.info("Novalnet comments are not in the backend order history page");
			ExtentTestManager.logMessage(Status.WARNING, "Novalnet comments are not in the backend order history page");
		return false;
	}
	
	public String verifyOrderNotesComments(String comments) {
          String actual="";
           String guarantee = getElementText(By.xpath("//div[@class='note_content']"));

		  if(guarantee.contains("Your order is under verification")){
			  actual = getElementText(By.xpath("//div[@class='note_content']"));
			  ExtentTestManager.logMessage("<b>Back-end order notes: ["+actual+"]</b>");
			  verifyContainsAssert(comments, actual,"<b>Back-end order notes: </b>");
		  }

		   else if(!guarantee.contains("Your order is under verification")){
			   String zerAmount =getElementText(By.xpath("(//div[@class='note_content'])[2]"));
			  if(zerAmount.equals("This order processed as a zero amount booking")){
				  actual = getElementText(By.xpath("(//div[@class='note_content'])[3]"));
				  ExtentTestManager.logMessage("<b>Back-end order notes: ["+actual+"]</b>");
				  verifyContainsAssert(comments, actual,"<b>Back-end order notes: </b>");
			  }
		   }
		     else if(comments.contains("Novalnet transaction ID")){
			 actual = getElementText(By.xpath("(//div[@class='note_content'])[2]"));
			 ExtentTestManager.logMessage("<b>Back-end order notes: ["+actual+"]</b>");
			 verifyContainsAssert(comments, actual,"<b>Back-end order notes: </b>");
		}
		else if(comments.equals(CAPTURE_COMMENT_)||comments.contains(PAYMENT_REMINDER_1) || comments.contains(PAYMENT_REMINDER_2)){
			actual = getElementText(By.xpath("(//div[@class='note_content'])[1]"));
			ExtentTestManager.logMessage("<b>Back-end order notes: ["+actual+"]</b>");
			verifyContainsAssert(actual, comments,"<b>Back-end order notes: </b>");
		}

		return actual;
	}

	public String verifyOrderNotesComments(String comments,boolean pending) {
		String actual="";

		if(pending){
			actual = getElementText(By.xpath("//div[@class='note_content']"));
			ExtentTestManager.logMessage("<b>Back-end order notes: ["+actual+"]</b>");
			verifyContainsAssert(comments, actual,"<b>Back-end order notes: </b>");
		}
		else if(comments.contains("Novalnet transaction ID")){
			actual = getElementText(By.xpath("(//div[@class='note_content'])[2]"));
			ExtentTestManager.logMessage("<b>Back-end order notes: ["+actual+"]</b>");
			verifyContainsAssert(comments, actual,"<b>Back-end order notes: </b>");
		}
		else if(comments.equals(CAPTURE_COMMENT_)||comments.contains(PAYMENT_REMINDER_1) || comments.contains(PAYMENT_REMINDER_2)){
			actual = getElementText(By.xpath("(//div[@class='note_content'])[1]"));
			ExtentTestManager.logMessage("<b>Back-end order notes: ["+actual+"]</b>");
			verifyContainsAssert(actual, comments,"<b>Back-end order notes: </b>");
		}

		return actual;
	}

	@Step("Get ordernote comments")
	public String getOrderNoteComment(String commentStartsWith) {
		List<WebElement>  orderNotesEle = getElements(By.xpath("//div[@class='note_content']"));
		StringBuilder comments = new StringBuilder();
		for (WebElement e : orderNotesEle) {
			comments.append(e.getText()).append("\n");
			highlightElement(e);
		}
		return comments.toString().trim();
	}

	public String getBackendOrderNoteInitialComment(){
		return getElementText(initialOrderorderNotes);
	}
	
	public void verifyOrderNotesCommentsWithRegex(String commentStart,String regex, String valueToVerify) {
		String orderNote = getOrderNoteComment(commentStart);
		ExtentTestManager.logMessage("<b>"+orderNote+"</b>");
		String value = getFirstMatchRegex(orderNote, regex);
		verifyAssertEquals(value.replaceAll("[^0-9]", ""), valueToVerify,"<b>Back-end order notes verification amount</b>");
	}
	
	public void verifyOrderNotesCommentsAmount(String commentStart, String valueToVerify) {
		String orderNote = getOrderNoteComment(commentStart);
		ExtentTestManager.logMessage("<b>"+orderNote+"</b>");
		String value = getFirstMatchRegex(orderNote, "€(\\d+\\.\\d{2})");
		verifyAssertEquals(value.replaceAll("[^0-9]", ""), valueToVerify,"<b>Verify the order amount in back-end order notes</b>");
	}
	
	public void verifyOrderNotesCommentsDueDate(String commentStart, String valueToVerify) {
		// value to verify in this format MMM dd, yyyy  - April 7, 2023
		String orderNote = getOrderNoteComment(commentStart);
		ExtentTestManager.logMessage("<b>"+orderNote+"</b>");
		String value = orderNote.split("due date")[1].trim();
		Date actual = getDateFromString("MMM dd, yyyy", value);
		Date expected = getDateFromString("yyyy-MM-dd", valueToVerify);
		verifyAssertEquals(changePatternOfDate("MMM dd, yyyy", actual), changePatternOfDate("MMM dd, yyyy", expected),"<b>Back-end order notes verification due date: </b>");
	}
	
	public void verifyCustomerNotesComments(String comments) {
		String customerNotes = getCustomerOrderNotes();
		String updatedCustomerComment = comments.replace("Note:","").trim();
		 String updatedCustomerNotes = customerNotes.replace("Customer provided note:","").trim();

		 if(customerNotes.contains("Customer provided note:")){
			 verifyContainsAssert(updatedCustomerComment, updatedCustomerNotes,"<b>Back-end customer provided notes: </b>");
		 }else{
			 verifyContainsAssert(updatedCustomerNotes, updatedCustomerComment,"<b>Back-end customer provided notes: </b>");
		 }
		 }

	
	
	public void initiateRefund() {
		clickElementWithJs(refundBtn);
		By qty = By.cssSelector("tr.item td.quantity div.view");
		List<WebElement> qtys = getElements(qty);
		List<WebElement> qtyInputs = getElements(refundOrderQtyInput);
		for(int i=0;i<qtys.size();i++) {
			String q = qtys.get(i).getText().replaceAll("[^0-9]", "");
			qtyInputs.get(i).sendKeys(q);
			clickSomewhere();
		}
		clickElementWithJs(refundViaNovalnetBtn);
		alertAccept();
		waitForElementDisable(shopLoader,20);
		sleep(1);
		scrollToElement(orderDivStart);
	}
	
	public void initiateRefund(int amount) {
		int total = Integer.parseInt(getElementText(orderTotalAmount).replaceAll("[^0-9]", ""));		
		clickElement(refundBtn);
		By productPriceInput = By.cssSelector("tr.item td.line_cost input.refund_line_total");
		if(amount <= total && amount > 0) {
			setText(productPriceInput, formatAmount(amount));
		}else {
			setText(productPriceInput, formatAmount(total));
		}
		pressTab(productPriceInput);
		//clickSomewhere();
		int notesCount = getElements(initialOrderorderNotes).size();
		clickElement(refundViaNovalnetBtn);
		alertAccept();
		waitForElementVisible(By.cssSelector("#order_refunds tr"),60);
		waitForOrderNotesCommentsUpdate(notesCount);
		scrollToElement(orderDivStart);
	}

	@Step("Proceed Instalment refund for cycle one")
	public void instalmentRefund(int amount) {
		clickElement(By.cssSelector("#refund_link_1"));
		setText(By.cssSelector("#novalnet_instalment_refund_amount_1"),formatAmount(amount));
		clickElement(By.cssSelector("#div_refund_link_1 button.do-api-refund"));
		alertAccept();
		if(checkElementExist(shopLoader))
			waitForElementDisable(shopLoader,20);
		waitForElementVisible(By.cssSelector("#order_refunds tr"),60);
		scrollToElement(orderDivStart);
	}

	public String formatAmount(int amount) {
		String s = String.valueOf(amount);
		if(s.length() >= 1) {
			if(s.length() == 1) {
				return  "0.0"+s;
			}else if(s.length() == 2) {
				return  "0."+s;
			}else {
				return s.substring(0,s.length()-2)+"."+s.substring(s.length()-2);
			}
		}
		return String.valueOf(amount);
	}
	
	public boolean verifySubscriptionOrderDisplayed() {
		boolean actual = checkElementDisplayed(subscriptionRelatedOrders);
		if(actual) {
			scrollToElement(subscriptionRelatedOrders);
			ExtentTestManager.addScreenShot("<b>Related orders inside the order history page</b>");
		} 
		return actual;
	}
	
	public SubscriptionPage clickRelatedSubscriptionOrder() {
		clickElement(subscriptionOrder);
		waitForTitleContains("Edit Subscription");
		return new SubscriptionPage();
	}
	
	public void verifySubscriptionOrderListStatus(String status) {
		var actualStatus = getElementText(subscriptionOrderListStatus);
		//ExtentTestManager.logMessage("<b>Shop backend order history page subscription status in related orders actual: ["+actualStatus+"] and expected : ["+status+"]</b>");
		verifyAssertEquals(actualStatus,status,"<b>Shop backend order history page subscription status in related orders</b>");
	}
	
	public Map<String,Object> getRenewalOrderDetails() {
		String customerNotes = getCustomerOrderNotes();
		Map<String,Object> map = new HashMap<>();
		map.put("RenewalOrderNovalnetComments", customerNotes.trim());
		String tid = getFirstMatchRegex(customerNotes, "(?:transaction ID:)\\s*(\\d{17})").replaceAll("[^0-9]", "");
		map.put("RenewalOrderTID", tid);
		return map;
	}
	
	
	public String getOrderNotesTID(String commentStartsWith) {
		return getFirstMatchRegex(getOrderNoteComment(commentStartsWith), "(?:transaction ID:)\\s*(\\d{17})").replaceAll("[^0-9]", "");
	}
	
	
	public void selectCustomer(String customerEmail) {
		clickElement(customerSelectContainer);
		setText(customerSearchInput, customerEmail);
		waitForElementDisable(By.cssSelector("#select2-customer_user-results li[aria-disabled='true']"));
		waitForElementVisible(customerSearchList);
		for(WebElement e : getElements(customerSearchList)) {
			if(e.getText().contains(customerEmail)) {
				clickElementWithAction(e);
				return;
			}
		}
		ExtentTestManager.logMessage(Status.FAIL, "<br>Customer with this email: "+customerEmail+" not found while backend order creation process </br>");
		Assert.fail("Customer with this email: "+customerEmail+" not found while backend order creation process ");
	}
	
	
	public void addProduct(String productName) {
		clickElement(addlineItem);
		clickElement(addProductBtn);
		clickElement(selectProductContainer);
		setText(searchProductInput, productName);
		waitForElementDisable(By.cssSelector("ul[id^='select2-item_id-'] li[aria-disabled='true']"));
		waitForElementVisible(searchProductList);
		for(WebElement e: getElements(searchProductList)) {
			if(e.getText().contains(productName)) {
				clickElementWithAction(e);
				clickElementWithAction(addBtn);
				waitForElementVisible(By.cssSelector("#order_line_items"));
				return;
			}
		}
		ExtentTestManager.logMessage(Status.FAIL, "<br>Product in this name: "+productName+" not found while backend order creation process </br>");
		Assert.fail("Product in this name: "+productName+" not found while backend order creation process ");	
	}
	
	public void recalculateTotal() {
		clickElement(recalculateButton);
		alertAccept();
	}

	public void bookTransactionForZeroAmountBooking(String amount){
		String total = formatAmount(Integer.parseInt(amount));
		if(!total.equals(getInputFieldText(zeroAmountBookingAmount)))
			setText(zeroAmountBookingAmount,total);
		clickElement(bookTransactionButton);
		alertAccept();
		waitForElementVisible(successMessage,30);
	}



	public void verifyZeroAmountBooking(String amount,String payment){
		var tid = getZeroAmountBookedNewTID(CallbackProperties.ZERO_AMOUNT_BOOKING_CONFIRMATION, amount);
		TID_Helper.verifyTIDInformation(tid,amount,TID_STATUS_CONFIRMED,payment);
	}

	@Step("Get zero amount booked TID")
	public String getZeroAmountBookedNewTID(String expected, String amount){
		Optional<String> actual = getElements(novalnetComments)
				.stream()
				.map(WebElement::getText)
				.map(String::trim)
				.filter(s->s.contains(expected) && s.contains(formatAmount(Integer.parseInt(amount))))
				.map(s-> getFirstMatchRegex(s,"Your new TID for the booked amount: (\\d+)"))
				.findFirst();
//	for(WebElement e : getElements(novalnetComments)){
//		String text = e.getText().trim();
//		if(text.contains(expected) && text.contains(formatAmount(Integer.parseInt(amount))));
//			return getFirstMatchRegex(text,"Your new TID for the booked amount: (\\d+)").replaceAll("[^0-9]","");
//	}
		return actual.map(s -> s.replaceAll("[^0-9]", "")).orElse(null);
	}
	public void bookTransactionForZeroAmountBookingFailureOrder(){
		clickElement(bookTransactionButton);
		alertAccept();
		waitForElementVisible(successMessage,30);
		String errorMsg = getElementText(By.cssSelector("#woocommerce_errors>p")).trim();
		verifyContains(errorMsg,"referenced transaction was not successful","Zero amount booking for communication break transaction");
	}
	void waitForOrderNotesCommentsUpdate(int oldCount){
		setExpectedCondition(d->d.findElements(By.cssSelector("ul.order_notes div.note_content")).size() > oldCount,1,"Waiting for order notes comments to be updated");
	}
}
