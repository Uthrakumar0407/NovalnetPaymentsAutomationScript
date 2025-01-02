package com.nn.testcase;

import static com.nn.callback.CallbackProperties.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.Test;

import com.nn.apis.GetTransactionDetailApi;
import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.CallbackEventInterface;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.callback.IdealCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.AdminPage;
import com.nn.pages.CartPage;
import com.nn.pages.CheckoutPage;
import com.nn.pages.DashboardPage;
import com.nn.pages.HomePage;
import com.nn.pages.MyAccountPage;
import com.nn.pages.OrdersPage;
import com.nn.pages.ProductPage;
import com.nn.pages.SettingsPage;
import com.nn.pages.SubscriptionPage;
import com.nn.pages.SuccessPage;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;

public class CommonTest extends BaseTest{
	
	AdminPage adminPage;
	DashboardPage dashboardPage;
	SettingsPage settingsPage;
	MyAccountPage myAccount;
	CartPage cartPage;
	ProductPage productPage;
	HomePage homePage;
	CheckoutPage checkoutPage;
	SuccessPage successPage;
	OrdersPage orderPage;
	SubscriptionPage subscriptionPage;
	CallbackEventInterface callback;
	
	
	@Test(priority = 1, groups= {"alpha"}, description="Check whether login successful with valid data")
	public void testLogin() {
		adminPage = new AdminPage();
		adminPage.openAdminPage();
		dashboardPage = adminPage.adminLogin("admin","wordpress");
		
		orderPage = new OrdersPage();
		settingsPage = new SettingsPage();
		homePage = new HomePage();
		productPage = new ProductPage();
		cartPage = new CartPage();
		checkoutPage = new CheckoutPage();
		successPage = new SuccessPage();
		myAccount = new MyAccountPage();
		subscriptionPage = new SubscriptionPage();
		callback = new CreditCardCallbackEvents();

//		myAccount.loadOrders();
//		myAccount.clickOrder("2177");
//		String[] comments = DriverActions.getElementAttributeText(By.xpath("//th[text()='Note:']/following-sibling::td"), "innerText").split("\\r?\\n|\\r");
//		String tid = DriverActions.getFirstMatchRegex(comments[0], "(?:transaction ID:)\\s*(\\d{17})");
//		System.out.println("tid: "+tid.replaceAll("[^0-9]", ""));
//		int i = 0;
//		for(String s : comments)
//			//if(!s.isBlank())
//			System.out.println((i++)+" : "+s);	
		
		
		//		settingsPage = dashboardPage.openSettingsPage();
//		settingsPage.openNovalnetGlobalConfig();
//		settingsPage.verifyGlobalConfig();
//		settingsPage.paymentPageLoad();
//		settingsPage.activatePayment("Novalnet Apple Pay");
//		settingsPage.setCreditCardPaymentConfiguration(true,"Authorize","",true,false,true,false);
//		settingsPage.enableSubscription();
//		settingsPage.setSubscriptionTariff();
//		settingsPage.selectSubscriptionPayments(Arrays.asList(new String[] {"Novalnet Direct Debit SEPA","Novalnet Credit/Debit Cards"}));
//		settingsPage.enableSubscriptionShopBased();
//		settingsPage.saveGlobalConfig();
//		cartPage = new CartPage();
		cartPage.load();
		cartPage.clearCart();
		productPage.load();
		productPage.addProductToCartByName(new String[]{"Happy Ninja"});
		homePage.load();
		checkoutPage = homePage.openCheckoutPage();
		DriverActions.clickElementWithJs(By.cssSelector(".wc_payment_method label[for='payment_method_novalnet_prepayment']"));
		checkoutPage.clickPlaceOrderBtn();
		DriverActions.sleep(10);
		Map<String, Object> map = successPage.getSuccessPageTransactionDetails();
		for(Map.Entry<String, Object> m: map.entrySet())
			System.out.println(m.getKey()+" : "+m.getValue());
//		if(checkoutPage.isCreditCardDisplayed()) {
//			 checkoutPage.fillCreditCardForm("4200000000000000", "1230", "123");}
//			 successPage = checkoutPage.placeOrder();
//			 Map<String,Object> txnInfo = successPage.getSuccessPageTransactionDetails();
//			 TID_Helper.verifyTIDInformation(txnInfo.get("TID").toString(), txnInfo.get("TotalAmount").toString(), TID_STATUS_ON_HOLD);
			 
//			transactionDetails.putAll(successPage.getSuccessPageSubscriptionDetails()); 
//			 successPage.verifyTIDInformation(transactionDetails.get("TID").toString(), transactionDetails.get("TotalAmount").toString(), "CONFIRMED");
//			 successPage.verifySubscriptionStatus("Active");
			// myAccount = new MyAccountPage();
			// myAccount.loadOrders();
//			 myAccount.verifyOrderListingStatus(transactionDetails.get("OrderNumber").toString(), "Completed");
			// myAccount.clickOrder("1570");
			// DriverActions.sleep(3);
//			 myAccount.verifyOrderHistoryPageStatus("Completed");
//			 myAccount.verifyOrderHistoryPageDetails(transactionDetails.get("NovalnetComments").toString(), transactionDetails.get("PaymentName").toString());
//			 myAccount.verifySubscriptionStatusInsideOrdersPage("Active");
//			 myAccount.loadSubscription();
//			 myAccount.verifyOrderListingStatus(transactionDetails.get("SubscriptionOrderNumber").toString(), "Active");
//			 myAccount.clickOrder(transactionDetails.get("SubscriptionOrderNumber").toString());
//			 myAccount.verifyOrderHistorySubscriptionStatus("Active");
			// myAccount.verifyPaymentNameInsideSubscription("CC");
//			 myAccount.verifyOrderStatusInsideSubscriptionPage("Completed");
//			 orderPage = new OrdersPage();
//			 subscriptionPage = new SubscriptionPage();
//			 subscriptionPage.load();
//			 subscriptionPage.verifyOrderListingStatus(transactionDetails.get("SubscriptionOrderNumber").toString(), "Active");
//			 subscriptionPage.verifyOrderListingPaymentName(transactionDetails.get("SubscriptionOrderNumber").toString(), transactionDetails.get("PaymentName").toString());
//			 subscriptionPage.selectBackendOrder("1504");
//			 subscriptionPage.changeNextPaymentDate("2023-04-30");
//			 subscriptionPage.verifyOrderHistoryPaymentName(transactionDetails.get("PaymentName").toString());
//			 subscriptionPage.verifySubscriptionParentOrderStatus("Completed");
//			 
//			 orderPage.createOrderPageLoad();
//			 orderPage.selectCustomer("nageshwaran");
//			 orderPage.addProduct("Happy Ninja");
//			 orderPage.recalculateTotal();
//			 orderPage.verifyOrderListingStatus(transactionDetails.get("OrderNumber").toString(), "Completed");
			// orderPage.selectBackendOrder("1698");
//			 orderPage.verifyOrderHistoryPageStatus("Completed");
//			 orderPage.verifyCustomerNotesComments(transactionDetails.get("NovalnetComments").toString());
//			 orderPage.verifyOrderNotesComments(transactionDetails.get("NovalnetComments").toString());
			 //orderPage.initiateRefund(100);
//			 
//			 subscriptionPage.loadCronSchedulerPage();
//			 subscriptionPage.searchCronScheduler(transactionDetails.get("SubscriptionOrderNumber").toString());
//			 subscriptionPage.runCronOnPayment();
//			 subscriptionPage.load();
//			 subscriptionPage.selectBackendOrder("1781");
//			 subscriptionPage.changePayment("Credit/Debit Cards", "4200000000000000", "1235", "123");
//			 String changePaymentTID = subscriptionPage.getChangePaymentTID("Test order");
//			 TID_Helper.verifyTIDInformation(changePaymentTID,"0", TID_STATUS_CONFIRMED, CREDITCARD);
//			 TID_Helper.verifyPaymentTokenExist(changePaymentTID, true);
//			 TID_Helper.verifySubscriptionPaymentInTID("14750800041626269", CREDITCARD);
//			 String renewalOrder = subscriptionPage.verifyRenewalOrderPresent();
//			 transactionDetails.put("RenewalOrder", renewalOrder);
//			 subscriptionPage.verifySubscriptionRenewalOrderStatus("Completed");
//			 orderPage.load();
//			 orderPage.verifyOrderListingStatus(transactionDetails.get("RenewalOrder").toString(), "Completed");
//			 orderPage.selectBackendOrder(transactionDetails.get("RenewalOrder").toString());
//			 orderPage.verifyOrderHistoryPageStatus("Completed");
//			 transactionDetails.putAll(orderPage.getRenewalOrderDetails());
//			 orderPage.verifyTIDInformation(transactionDetails.get("RenewalOrderTID").toString(), transactionDetails.get("SubscriptionCycleAmount").toString(), "CONFIRMED");
//			 orderPage.verifyOrderNotesComments(transactionDetails.get("RenewalOrderNovalnetComments").toString());			 
//		}
	}
	
	@Test(priority = 2, retryAnalyzer=RetryListener.class, description="Navigate to Novalnet payment Config")
	public void openNovalnetConfiguration() {
			Response response = GetTransactionDetailApi.getTransactionAPI("14762800048012307");
			JsonPath jsonPath = response.jsonPath();
			String resultStatus = jsonPath.get("result.status");
			var resultStatusCode = jsonPath.get("result.status_code");
			var iban = jsonPath.get("transaction.bank_details.iban");
			System.out.println(resultStatus+" "+resultStatusCode);
			System.out.println("IBAN " + iban);

	}
	
	@Test(priority = 3, groups= {"beta"}, description="Check whether the multidropdown selection working for novlanet live mode dropdown")
	public void multiSelectDropdown() {

	}

	public static void main(String[] args) throws InterruptedException {
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("http://novalnet.de/");
		Thread.sleep(5000);
		List<WebElement> list = driver.findElements(By.tagName("a"));
		System.out.println(list.size());
		list.addAll(driver.findElements(By.tagName("img")));
		System.out.println(list.size());
		for (WebElement element : list) {
			String url = element.getAttribute("href");

			// Skip empty URLs or URLs starting with "mailto:"
			if (url == null || url.isEmpty() || url.startsWith("mailto:") || url.startsWith("tel:"))
				continue;

			try {
				// Create a HttpURLConnection for the URL
				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

				// Send a GET request to the URL
				connection.setRequestMethod("GET");

				// Get the response code
				int responseCode = connection.getResponseCode();

				// Check if the response code indicates a broken link
				if (responseCode >= 400) {
					System.out.println("Broken link found: " + url);
				}
			} catch (Exception e) {
				System.out.println("Error occurred while checking link: " + url);
			}
		}
		driver.quit();

	}

	public static String getSEPADueDate(int dueDate){
		String today = LocalDate.now().getDayOfWeek().toString();
		LocalDate expectedDueDate = LocalDate.now().plusDays(dueDate);
		if(today.equals("THURSDAY")){
			dueDate += 2;
		}
		if(today.equals("FRIDAY")){
			dueDate += 2;
		}
		if(today.equals("SATURDAY")){
			dueDate += 1;
		}
		if(expectedDueDate.getDayOfWeek() == DayOfWeek.SUNDAY){
			expectedDueDate.plusDays(1);
		}
		if(expectedDueDate.getDayOfWeek() == DayOfWeek.SATURDAY){
			expectedDueDate.plusDays(2);
		}

		return expectedDueDate.toString();
	}
	
	
}
