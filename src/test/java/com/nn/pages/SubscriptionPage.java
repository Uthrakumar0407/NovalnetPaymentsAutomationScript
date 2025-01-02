package com.nn.pages;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.*;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.aventstack.extentreports.Status;
import com.nn.apis.GetTransactionDetailApi;
import com.nn.constants.Constants;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;

public class SubscriptionPage {

	private By historyPagePaymentName = By.xpath("(//div[@class='address'])[1]/p[4]");
	private By subscriptionRelatedOrder = By.cssSelector(".woocommerce_subscriptions_related_orders");
	private By subscriptionRenewalOrder = By.xpath("//td[contains(text(),'Renewal Order')]/preceding-sibling::td/a");
	private By subscriptionRenewalOrderStatus = By.xpath("//td[contains(text(),'Renewal Order')]/following-sibling::td/mark/span");
	private By subscriptionParentOrderStatus = By.xpath("//td[contains(text(),'Parent Order')]/following-sibling::td/mark/span");
	private By orderStatusDropdwon = By.cssSelector("#select2-order_status-container");
	private By orderNotes = By.cssSelector("div.note_content");
	private By orderNotesInAddress = By.xpath("//div[@class='address']/p[contains(text(),'Novalnet transaction ID')]");
	private By orderSearchInput = By.cssSelector(".search-box>input[name='s']");
	private By orderSubmitBtn = By.cssSelector("#search-submit");
	private By orderUpdateBtn = By.cssSelector(".button.save_order.button-primary");
	private By orderUpdateMsg = By.cssSelector("#message");
	private By orderDivStart = By.cssSelector(".wp-heading-inline");
	private By suspendBtn = By.xpath("//span[@class='on-hold']/a");
	private By reActivateBtn = By.xpath("//span[@class='active']/a");
	private By cancelBtn = By.xpath("//span[@class='cancelled']/a");
	private By editSubscriptionAmountBtn = By.cssSelector(".edit-order-item");
	private By subscriptionAmountInput = By.cssSelector(".line_total.wc_input_price");
	private By subscriptionAmountSaveBtn = By.cssSelector(".button-primary.save-action");
	private By recalculateButton = By.cssSelector(".calculate-action");
	private By shopLoader = By.cssSelector(".blockUI.blockOverlay");
	private By nextPaymentCalender = By.cssSelector("#next_payment");
	private By nextMonth = By.cssSelector(".ui-datepicker-next");
	private By prevMonth = By.cssSelector(".ui-datepicker-prev");
	private By currentDate = By.xpath("//td[@data-handler='selectDay']/a");
	private By calenderYearMonth = By.xpath("//td[@data-handler='selectDay']");
	private By editBillingAddress = By.cssSelector(".order_data_column:nth-child(2) a.edit_address");
	private By changePaymentDropdown = By.cssSelector("#_payment_method");
	private By changePaymentCheckbox = By.cssSelector("#novalnet_payment_change");
	private By creditCardiFrame = By.cssSelector("#novalnet_cc_iframe");
	private By creditCardNumber = By.cssSelector("#card_number");
	private By creditCardExp = By.cssSelector("#expiry_date");
	private By creditCardCVV = By.cssSelector("#cvc");
	private By sepaIban = By.cssSelector("#novalnet_sepa_iban");

	//cron page
	private By searchInCronPage = By.cssSelector("#plugin-search-input");
	private By searchBtn = By.cssSelector("#search-submit");
	private By cronRunBtn = By.xpath("//td[contains(text(),'subscription_payment')]//span[@class='run']/a");
	private By cronRunAlert = By.cssSelector("#message");
	private By orderTotal = By.xpath("//td[text()='Order Total:']/following-sibling::td//bdi");

	String renewalOrderStatus = "//td/a[contains(text(),'988')]/../following-sibling::td/mark/span";
	String renewalOrderAmount = "//td/a[contains(text(),'988')]/../following-sibling::td//span[@class='woocommerce-Price-amount amount']";


	public SubscriptionPage load() {
		openURL(Constants.URL+"edit.php?post_type=shop_subscription");
		waitForTitleContains("Subscriptions");
		return this;
	}

	public void loadCronSchedulerPage() {
		openURL(Constants.URL+"tools.php?page=action-scheduler");
		waitForTitleContains("Scheduled Actions");
	}

	public void searchCronScheduler(String subscriptionOrderNumber) {
		setText(searchInCronPage, subscriptionOrderNumber);
		clickElement(searchBtn);
		waitForPageLoad();
	}


	public void runCronOnPayment() {
		waitForElementVisible(By.xpath("//td[contains(text(),'subscription_payment')]"));
		//hoverAndClickElementWithAction(cronRunBtn);
		openURL(getElement(cronRunBtn).getAttribute("href"));
		waitForElementVisible(cronRunAlert);
	}

	public void clickSubscriptionSuspend(String subscriptionOrderNumber) {
		if(getElement(suspendBtn).getAttribute("href").contains("post="+subscriptionOrderNumber)) {
			openURL(getElement(suspendBtn).getAttribute("href"));
			load();
		}else {
			Log.info("Invalid subscription order number. So I could not click suspend button");
			ExtentTestManager.logMessage(Status.WARNING,"<b>Invalid subscription order number. So I could not click suspend button");
			Assert.fail();
		}
	}

	public void clickSubscriptionReactivate(String subscriptionOrderNumber) {
		if(getElement(reActivateBtn).getAttribute("href").contains("post="+subscriptionOrderNumber)) {
			openURL(getElement(suspendBtn).getAttribute("href"));
			load();
		}else {
			Log.info("Invalid subscription order number. So I could not click reactivate button");
			ExtentTestManager.logMessage(Status.WARNING,"<b>Invalid subscription order number. So I could not click reactivate button");
			Assert.fail();
		}
	}

	public void clickSubscriptionCancel(String subscriptionOrderNumber) {
		if(getElement(cancelBtn).getAttribute("href").contains("post="+subscriptionOrderNumber)) {
			openURL(getElement(suspendBtn).getAttribute("href"));
		}else {
			Log.info("Invalid subscription order number. So I could not click cancel button");
			ExtentTestManager.logMessage(Status.WARNING,"<b>Invalid subscription order number. So I could not click cancel button");
			Assert.fail();
		}
	}

	public String updateSubscriptionAmount(int amount) {
		hoverOnElementUsingJs(editSubscriptionAmountBtn);
		clickElementWithJs(editSubscriptionAmountBtn);
		setText(subscriptionAmountInput, formatAmount(amount));
		clickElementByRefreshing(subscriptionAmountSaveBtn);
		waitForElementDisable(shopLoader);
		recalculateTotal();
		waitForElementDisable(shopLoader);
		return getElementText(orderTotal).replaceAll("[^0-9]", "");
	}

	public void recalculateTotal() {
		clickElementByRefreshing(recalculateButton);
		alertAccept();
	}

	public String getOrderTotalWithCurrency() {
		return getElementAttributeText(orderTotal,"innerText").trim();
	}

	public String formatAmount(int amount) {
//		String s = String.valueOf(amount);
//		String ans = "";
//		char[] ch = s.toCharArray();
//		if(s.length() >= 1) {
//			if(s.length() == 1) {
//				ans = "0,0"+s;
//			}else if(s.length() == 2) {
//				ans = "0,"+s;
//			}else {
//				for(int i=0;i<ch.length;i++){
//			        if(i==ch.length-2)
//			            ans += ",";
//			        ans += String.valueOf(ch[i]);
//			    }
//			}
//		}
//		return ans;
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

	public String getNextPaymentDate() {
		return getInputFieldText(nextPaymentCalender);
	}

	public void changeNextPaymentDate(String nextPaymentDate) {
		//Date should be in this format YYYY-mm-DD 2023-12-31
		if(!getInputFieldText(nextPaymentCalender).equals(nextPaymentDate)) {
			clickElement(nextPaymentCalender);
			String[] splitDate = nextPaymentDate.split("-");
			int date = Integer.parseInt(splitDate[2]);
			int month = Integer.parseInt(splitDate[1])-1;
			String year = splitDate[0];

			while(!getElementAttributeText(calenderYearMonth, "data-year").equals(year) || !getElementAttributeText(calenderYearMonth, "data-month").equals(String.valueOf(month))) {
				String yearValue = getElementAttributeText(calenderYearMonth, "data-year");
				String monthValue = getElementAttributeText(calenderYearMonth, "data-month");
				if(Integer.parseInt(yearValue) > Integer.parseInt(year) || Integer.parseInt(monthValue) > month) {
					clickElement(prevMonth);
					sleep(0.2);
				}else {
					clickElement(nextMonth);
					sleep(0.2);
				}
			}

			for(WebElement d : getElements(currentDate)) {
				if(d.getText().trim().equals(String.valueOf(date))) {
					d.click();
				}
			}
		}
		scrollToElement(orderUpdateBtn);
		clickElement(orderUpdateBtn);
		waitForElementVisible(orderUpdateMsg,20);
		verifyEquals(getInputFieldText(nextPaymentCalender), nextPaymentDate);
	}

	public String verifyRenewalOrderPresent() {
		String orderNumber = null;
		boolean status = checkElementDisplayed(subscriptionRenewalOrder);
		if(status) {
			for(WebElement e : getElements(subscriptionRenewalOrder)) {
				orderNumber = e.getText().replaceAll("[^0-9]", "");
				break;
			}
		}
		ExtentTestManager.logMessage("Subscription renewal order present status: "+status);
		return orderNumber;
	}

	public boolean verifyRenewalOrderExist(String orderNumber) {
		boolean status = checkElementDisplayed(subscriptionRenewalOrder);
		if(status) {
			for(WebElement e : getElements(subscriptionRenewalOrder)) {
				if(e.getText().replaceAll("[^0-9]", "").equals(orderNumber)){
					ExtentTestManager.logMessage("<b>Subscription renewal order present status: "+true);
					return true;
				}
			}
		}
		ExtentTestManager.logMessage("<b>Subscription renewal order present status: "+false);
		return false;
	}

	public void clickRenewalOrder(String orderNumber) {
		By order = By.partialLinkText(orderNumber);
		clickElement(order);
		waitForTitleContains("Edit order");
		sleep(1);
	}

	public void selectBackendOrder(String orderNumber) {
		By order = By.partialLinkText(orderNumber);
		if(checkElementExist(order)) {
			clickElement(order);
		}else {
			setText(orderSearchInput, orderNumber);
			clickElement(orderSubmitBtn);
			clickElement(order);
		}
		scrollToElement(orderDivStart);
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

	public String getBackendOrderListingStatus(String orderNumber) {
		//String orderRow = "//tr[@id='post-"+orderNumber.trim()+"']";
		String orderRow = "tr[id$='-"+orderNumber.trim()+"']>td.order_title a:first-of-type"; // updated - for new woocommerce server shop
		By orderRowLoc = By.cssSelector(orderRow);
		//    tr[id$='-101']>td.order_title a:first-of-type
		//    tr[id$='-101']>td.status mark>span
		String status;
		if(checkElementExist(orderRowLoc)) {
			scrollToElement(orderDivStart);
			//status = getElementText(By.xpath(orderRow+"//mark/span"));
			status = getElementText(By.cssSelector("tr[id$='-"+orderNumber.trim()+"']>td.status mark>span")); // update for new woocommerce server shop
		}else {
			setText(orderSearchInput, orderNumber);
			clickElement(orderSubmitBtn);
			scrollToElement(orderDivStart);
			//status = getElementText(By.xpath(orderRow+"//mark/span"));
			status = getElementText(By.cssSelector("tr[id$='-"+orderNumber.trim()+"']>td.status mark>span")); // update for new woocommerce server shop
		}
		//	highlightElement(By.xpath(orderRow+"//mark/span"));
		return status;
	}

	public String getBackendOrderListingPaymentName(String orderNumber) {
		//String orderRow = "//tr[@id='post-"+orderNumber.trim()+"']";
		String orderRow = "tr[id$='-"+orderNumber.trim()+"']>td.order_title a:first-of-type"; // updated - for new woocommerce server shop
		By orderRowLoc = By.cssSelector(orderRow);
		String status;
		if(checkElementExist(orderRowLoc)) {
			scrollToElement(orderDivStart);
			status = getElementText(By.cssSelector("tr[id$='-"+orderNumber.trim()+"']>td.recurring_total>small"));

		}else {
			setText(orderSearchInput, orderNumber);
			clickElement(orderSubmitBtn);
			scrollToElement(orderDivStart);
			status = getElementText(By.cssSelector("tr[id$='-"+orderNumber.trim()+"']>td.recurring_total>small"));
		}
		//	highlightElement(By.xpath(orderRow+"/td/small"));
		return status;
	}

	public void verifyOrderListingStatus(String orderNumber,String status) {
		var actualStatus = getBackendOrderListingStatus(orderNumber);
		//ExtentTestManager.logMessage("<b>Shop backend Subscription order listing page status actual: ["+actualStatus+"] and expected : ["+status+"]</b>");
		verifyAssertEquals(actualStatus,status,"<b>Back-end subscription order list page status: </b>");
	}

	public void verifyOrderListingPaymentName(String orderNumber,String paymentName) {
		var actualStatus = getBackendOrderListingPaymentName(orderNumber);
		//ExtentTestManager.logMessage("<b>Shop backend Subscription order listing page paymentName actual: ["+actualStatus+"] and expected : ["+paymentName+"]</b>");
		verifyContainsAssert(actualStatus,paymentName,"<b>Back-end subscription order list page payment name: </b>");
	}

	public void verifyOrderHistoryPaymentName(String paymentName) {
		var actualStatus = getElementText(historyPagePaymentName);
		//ExtentTestManager.logMessage("<b>Shop backend Subscription order history page paymentName actual: ["+actualStatus+"] and expected : ["+paymentName+"]</b>");
		verifyContainsAssert(actualStatus,paymentName,"<b>Back-end subscription order detail page payment name: </b>");
	}

	public void verifySubscriptionRelatedOrdersPresent() {
		var status = checkElementDisplayed(subscriptionRelatedOrder);
		if(status) {
			scrollToElement(subscriptionRelatedOrder);
			highlightElement(subscriptionRelatedOrder);
		}
		//ExtentTestManager.addScreenShot("<b> Subscription related orders staus : "+status+" and expected : true</b>");
	}

	public void verifySubscriptionRenewalOrderStatus(String status) {
		String actual = getElementText(subscriptionRenewalOrderStatus);
		//ExtentTestManager.logMessage("<b>Shop backend Subscription order history page renewal order status actual: ["+actual+"] and expected : ["+status+"]</b>");
		verifyAssertEquals(actual, status,"<b>Back-end subscription order detail page renewal order status: </b>");
	}

	public void verifySubscriptionParentOrderStatus(String status) {
		String actual = getElementText(subscriptionParentOrderStatus);
		//ExtentTestManager.logMessage("<b>Shop backend Subscription order history page parent order status actual: ["+actual+"] and expected : ["+status+"]</b>");
		verifyAssertEquals(actual, status,"<b>Back-end subscription order detail page parent order status: </b>");
	}

	public String getOrderStatus() {
		return getElementText(orderStatusDropdwon);
	}

	public void verifyOrderHistoryPageStatus(String status) {
		//ExtentTestManager.addScreenShot("Shop backend order history page status dropdown");
		var actualStatus= getOrderStatus();
		highlightElement(orderStatusDropdwon);
		//ExtentTestManager.logMessage("<b>Shop backend order history page status in dropdown actual: ["+actualStatus+"] and expected : ["+status+"]</b>");
		verifyAssertEquals(actualStatus,status,"<b>Back-end subscription order detail page status: </b>");
	}

	public String verifyOrderNotesComments(String comments) {
		String actual= getOrderNoteComment(comments);

		if(comments.contains(SUBSCRIPTION_SUSPEND_COMMENT_)){
			actual = getElementText(By.xpath("//*[contains(text(),'This subscription transaction has been suspended on')]"));
			ExtentTestManager.logMessage("<b>Back-end subscription order notes: ["+actual+"]</b>");
			verifyContainsAssert(actual, comments,"<b>Back-end subscription order notes: </b>");
		} else if (comments.contains(SUBSCRIPTION_REACTIVATE_COMMENT_)) {
			actual = getElementText(By.xpath("//*[contains(text(),'Subscription has been reactivated for the TID:')]"));
			ExtentTestManager.logMessage("<b>Back-end subscription order notes: ["+actual+"]</b>");
			verifyContainsAssert(actual, comments,"<b>Back-end subscription order notes: </b>");
		}

		else{
			ExtentTestManager.logMessage("<b>Back-end subscription order notes: ["+actual+"]</b>");
			verifyContainsAssert(actual, comments,"<b>Back-end subscription order notes: </b>");
		}
		return actual;

	}

	public String getCustomerOrderNotes() {
		String notes = getElementText(orderNotesInAddress);
		highlightElement(orderNotesInAddress);
		return notes;
	}

	public void verifyCustomerNotesComments(String comments) {
		String customerNotes = getCustomerOrderNotes();
		verifyContainsAssert(customerNotes, comments,"<b>Back-end customer provided notes:</b>");
	}


	public String getOrderNoteComment(String commentStartsWith) {
		List<WebElement>  orderNotesEle = getElements(orderNotes);
		String comment = "";
		for(WebElement e : orderNotesEle ) {
			String note = e.getAttribute("innerText").trim();
			if(note.contains(commentStartsWith.trim())) {
				comment = note;
				highlightElement(e);
				break;
			}
		}
		return comment;
	}

	public String getChangePaymentTID(String commentStartsWith) {
		return getFirstMatchRegex(getOrderNoteComment(commentStartsWith), "(?:transaction ID:)\\s*(\\d{17})").replaceAll("[^0-9]", "");
	}

	public void fillCreditCardForm(String cardNumber, String expDate, String cvv) {
		switchToFrame(creditCardiFrame);
		waitForElementVisible(creditCardNumber,30);
		setText(creditCardNumber, cardNumber);
		setText(creditCardExp, expDate);
		setText(creditCardCVV, cvv);
		switchToDefaultContent();
		ExtentTestManager.addScreenShot("<b>CreditCard iFrame After credentials filled</b>");
	}

	public void changePayment(String paymentName,String cardNumber, String expDate, String cvv) {
		clickElement(editBillingAddress);
		selectDropdownByText(changePaymentDropdown, paymentName);
		fillCreditCardForm(cardNumber, expDate, cvv);
		clickChangePaymentCheckbox();
		int count = getElements(orderNotes).size();
		clickElement(orderUpdateBtn);
		waitForOrderNotesCommentsUpdate(count);
	}

	public void changePayment(String paymentName, String iban) {
		clickElement(editBillingAddress);
		selectDropdownByText(changePaymentDropdown, paymentName);
		setText(sepaIban, iban);
		clickChangePaymentCheckbox();
		int count = getElements(orderNotes).size();
		clickElement(orderUpdateBtn);
		waitForElementVisible(orderUpdateMsg,20);
		waitForOrderNotesCommentsUpdate(count);
	}

	public void changePayment(String paymentName) {
		clickElement(editBillingAddress);
		selectDropdownByText(changePaymentDropdown, paymentName);
		clickChangePaymentCheckbox();
		int count = getElements(orderNotes).size();
		clickElement(orderUpdateBtn);
		waitForElementVisible(orderUpdateMsg,20);
		waitForOrderNotesCommentsUpdate(count);
	}

	void clickChangePaymentCheckbox() {
		getElements(By.xpath("//input[contains(@id,'novalnet_payment_change')]"))
				.stream()
				.filter(WebElement::isDisplayed)
				.filter(WebElement::isEnabled)
				.findFirst()
				.ifPresentOrElse(e->{
					if(!e.isSelected())
						e.click();
				}, () -> {
					throw new RuntimeException("Unable to click change payment checkbox");
				});
		sleep(1);
	}

	void waitForOrderNotesCommentsUpdate(int oldCount){
		setExpectedCondition(d->d.findElements(orderNotes).size() > oldCount,1,"Waiting for order notes comments to be updated");
	}

}
