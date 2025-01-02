package com.nn.apis;

import java.util.Date;
import java.util.Map;

import com.nn.callback.CallbackProperties;
import com.nn.constants.Constants;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.testng.Assert;

import static com.nn.apis.GetTransactionDetailApi.*;
import static com.nn.callback.CallbackProperties.TID_STATUS_CONFIRMED;
import static com.nn.callback.CallbackProperties.TID_STATUS_PENDING;
import static com.nn.utilities.DriverActions.*;

public class TID_Helper {

	public static Map<String,Object> getTransactionDetails(String tid) {
		return getKeyValues(tid, "transaction");
	}

	public static Map<String,Object> getSubscriptionDetails(String tid) {
		return getKeyValues(tid, "subscription");
	}

	public static Map<String,Object> getBankDetails(String tid) {
		return getKeyValues(tid, "transaction","bank_details");
	}

	public static String getTIDStatus(String tid) {
		return getValue(tid, "transaction", "status");
	}
	public static String getTIDCurrency(String tid) {
		return getValue(tid, "transaction", "currency");
	}

	public static String getOrderNumber(String tid) {
		return getValue(tid, "transaction", "order_no");
	}

	public static String getTIDAmount(String tid) {
		return getValue(tid, "transaction", "amount");
	}

	public static String getTIDPaymentType(String tid) {
		return getValue(tid, "transaction", "payment_type");
	}

	public static String getDueDate(String tid) {
		return getValue(tid, "transaction", "due_date");
	}

	public static boolean verifyNearestStoresExists(String tid) {
		return containsValue(tid, "transaction", "nearest_stores");
	}

	@Step("Verify TID {0} with amount {1}, status {2}, paymentName {3} and due date {4}")
	public static void verifyTIDInformation(String tid,String amount,String status,String paymentName, int dueDateInDays) {
		Map<String, Object> tidDetails = getTransactionDetails(tid);
		String tidStatusInNovalnet = tidDetails.get("status").toString();
		String tidAmountInNovalnet = tidDetails.get("amount").toString();
		String paymentNameInNovalnet = tidDetails.get("payment_type").toString();
		String dueDateNameInNovalnet = tidDetails.get("due_date").toString();
		String testModeInNovalnet = tidDetails.get("test_mode").toString();
		String today = changePatternOfDate("yyyy-MM-dd", new Date());
		String expectedDueDate = addDaysFromDate(today, dueDateInDays);
		if(paymentNameInNovalnet.equals("GUARANTEED_DIRECT_DEBIT_SEPA") || paymentNameInNovalnet.equals("DIRECT_DEBIT_SEPA") || paymentNameInNovalnet.equals("INSTALMENT_DIRECT_DEBIT_SEPA")){
			expectedDueDate = getSEPADueDate(dueDateInDays);
		}
		String[] keys = new String[] {"amount","status","payment_type","due_date","test_mode"};
		String[] actual = new String[] {tidAmountInNovalnet,tidStatusInNovalnet,paymentNameInNovalnet,dueDateNameInNovalnet,testModeInNovalnet};
		String[] expected = new String[] {amount,status,paymentName,expectedDueDate,"1"};
		printMap(keys, actual, expected);
	}

	@Step("Verify TID {0} with amount {1}, status {2} and paymentName {3}")
	public static void verifyTIDInformation(String tid,String amount,String status,String paymentName) {
		Map<String, Object> tidDetails = getTransactionDetails(tid);
		ExtentTestManager.logMessage("<b>Novalnet transaction details for the TID: "+tid+"</b>"+printMap(tidDetails));
		String tidStatusInNovalnet = tidDetails.get("status").toString();
		String tidAmountInNovalnet = tidDetails.get("amount").toString();
		String paymentNameInNovalnet = tidDetails.get("payment_type").toString();
		String testModeInNovalnet = tidDetails.get("test_mode").toString();
		String[] keys = new String[] {"amount","status","payment_type","test_mode"};
		String[] actual = new String[] {tidAmountInNovalnet,tidStatusInNovalnet,paymentNameInNovalnet,testModeInNovalnet};
		String[] expected = new String[] {amount,status,paymentName,"1"};
		printMap(keys, actual, expected);
//		verifyEquals(tidAmountInNovalnet, amount,"<b>TID Amount in Novanlet server: </b>");
//		verifyEquals(tidStatusInNovalnet, status,"<b>TID Staus in Novanlet server: </b>");
//		verifyEquals(paymentNameInNovalnet, paymentName,"<b>Payment Name in Novanlet server: </b>");
	}

	@Step("Verify TID {0} with amount {1}, status {2}")
	public static void verifyTIDInformation(String tid, String amount, String status) {
		Map<String, Object> tidDetails = getTransactionDetails(tid);
		ExtentTestManager.logMessage("<b>Novalnet transaction details for the TID: "+tid+"</b>"+printMap(tidDetails));
		String tidStatusInNovalnet = tidDetails.get("status").toString();
		String tidAmountInNovalnet = tidDetails.get("amount").toString();
		String[] keys = new String[] {"amount","status"};
		String[] actual = new String[] {tidAmountInNovalnet,tidStatusInNovalnet};
		String[] expected = new String[] {amount,status};
		printMap(keys, actual, expected);
//		verifyEquals(tidStatusInNovalnet, status,"<b>TID Staus in Novanlet server:</b>");
//		verifyEquals(tidAmountInNovalnet, amount,"<b>TID Amount in Novanlet server:</b>");
	}

	@Step("Verify TID {0} with status {1}")
	public static void verifyTIDStatus(String tid, String status) {
		Map<String, Object> tidDetails = getTransactionDetails(tid);
		ExtentTestManager.logMessage("<b>Novalnet transaction details for the TID: "+tid+"</b>"+printMap(tidDetails));
		String tidStatusInNovalnet = tidDetails.get("status").toString();
		verifyEquals(tidStatusInNovalnet, status,"<b>TID Staus in Novanlet server:</b>");
	}

	@Step("Verify TID {0} with status {1}")
	public static void verifyTrustlyTIDStatus(String tid,String amount) {
		Map<String, Object> tidDetails = getTransactionDetails(tid);
		ExtentTestManager.logMessage("<b>Novalnet transaction details for the TID: "+tid+"</b>"+printMap(tidDetails));
		String tidStatusInNovalnet = tidDetails.get("status").toString();
		String paymentNameInNovalnet = tidDetails.get("payment_type").toString();
		String amountInNovalnet = tidDetails.get("amount").toString();
		boolean actual=tidStatusInNovalnet.contains(TID_STATUS_PENDING) || tidStatusInNovalnet.contains(TID_STATUS_CONFIRMED);
		verifyEquals(actual, true,"<b>TID Status in Novanlet server:</b>");
		verifyEquals(amountInNovalnet, amount,"<b>TID Amount in Novanlet server:</b>");
		verifyEquals(paymentNameInNovalnet, CallbackProperties.TRUSTLY,"<b>TID Payment Type in Novanlet server:</b>");
	}

	@Step("Verify TID {0} with status {1}")
	public static void verifyPayPalTIDStatus(String tid,String amount) {
		Map<String, Object> tidDetails = getTransactionDetails(tid);
		ExtentTestManager.logMessage("<b>Novalnet transaction details for the TID: "+tid+"</b>"+printMap(tidDetails));
		String tidStatusInNovalnet = tidDetails.get("status").toString();
		String paymentNameInNovalnet = tidDetails.get("payment_type").toString();
		String amountInNovalnet = tidDetails.get("amount").toString();
		boolean actual=tidStatusInNovalnet.contains(TID_STATUS_PENDING) || tidStatusInNovalnet.contains(TID_STATUS_CONFIRMED);
		verifyEquals(actual, true,"<b>TID Status in Novanlet server:</b>");
		verifyEquals(amountInNovalnet, amount,"<b>TID Amount in Novanlet server:</b>");
		verifyEquals(paymentNameInNovalnet, CallbackProperties.PAYPAL,"<b>TID Payment Type in Novanlet server:</b>");
	}

	public static boolean verifyPaymentTokenExist(String tid, boolean expected) {
		boolean actual = containsValue(tid, "transaction", "payment_data", "token");
		verifyAssertEquals(actual,expected , "<b>Token value exist status for the tid:"+tid+":</b>");
		return actual;
	}

	public static void verifyNextCycleDateExist(String tid, boolean expected) {
		boolean actual = containsValue(tid, "subscription", "next_cycle_date");
		verifyAssertEquals(actual,expected , "<b>Next cycle date value exist status for the tid:"+tid+":</b>");
	}

	public static void verifySubscriptionCancelReasonExist(String tid, boolean expected) {
		boolean actual = containsValue(tid, "subscription", "reason");
		verifyAssertEquals(actual,expected , "<b>Subscription cancel reason value exist status for the tid:"+tid+":</b>");
	}

	public static String getNextCycleDate(String tid) {
		return getValue(tid, "subscription", "next_cycle_date");
	}

	@Step("Verify Reference token exist in TID")
	public static String  verifyPaymentTokenExist(String tid) {
		String token = getValue(tid, "transaction", "payment_data", "token");
		verifyEquals(token!=null, true,"<b>Reference Token exist status the tid:</b>");
		return token;
	}

	@Step("Verify Reference token exist in TID")
	public static void verifyReferenceTokenExist(String tid, boolean expected) {
		boolean token = containsValue(tid, "custom", "reference_token");
		verifyEquals(token, expected,"<b>Reference Token exist status the tid:</b>");
	}

	@Step("Verify Subscription values exist in TID")
	public static boolean verifySubscriptionValuesExist(String tid, boolean expected) {
		boolean actual = containsValue(tid, "subscription");
		verifyEquals(actual, expected,"<b>Subscription Values in the the tid:</b>");
		return actual;
	}

	@Step("Verify Instalment values exist in TID")
	public static boolean verifyInstalmentValuesExist(String tid, boolean expected) {
		boolean actual = containsValue(tid, "instalment");
		verifyEquals(actual, expected,"<b>Instalment Values in the the tid:</b>");
		return actual;
	}

	@Step("Verify Instalment cycle date values exist in TID")
	public static boolean verifyInstalmentCycleDatesExist(String tid, boolean expected) {
		boolean actual = containsValue(tid, "instalment","cycle_dates");
		verifyEquals(actual, expected,"<b>Instalment cycle dates in the the tid:</b>");
		return actual;
	}

	@Step("Verify Instalment values in TID")
	public static void verifyInstalmentValuesInTID(String tid, String numberOfCycles, String cycleAmount) {
		Map<String,Object> instalment = getKeyValues(tid, "instalment");
		String instalmentTID = instalment.get("tid").toString();
		String instalmentCycleAmount = instalment.get("cycle_amount").toString();
		Map<String,String> cycleDates = (Map<String, String>) instalment.get("cycle_dates");
		Map<String,String> upcomingMonths = getUpcomingMonthDates(Integer.parseInt(numberOfCycles));
		for(int i=0;i<Integer.parseInt(numberOfCycles);i++){
			if(!cycleDates.get(String.valueOf(i+1)).contains(upcomingMonths.get(String.valueOf(i+1)))){
				Assert.fail("Instalment cycle dates are not matched");
			}
		}
		String[] keys = new String[] {"cycle_amount","tid"};
		String[] actual = new String[] {instalmentCycleAmount,instalmentTID};
		String[] expected = new String[] {cycleAmount,tid};
		printMap(keys, actual, expected);
	}

	public static boolean verifyBankDetailsExist(String tid, boolean expected) {
		boolean actual = containsValue(tid, "transaction","bank_details");
		verifyEquals(actual, expected,"<b>Bank details in the the tid:</b>");
		return actual;
	}

	@Step("Verify Bank details in TID")
	public static void verifyBankDetails(String tid, Map<String,Object> expected) {
		Map<String,Object> actual = getBankDetails(tid);
		System.out.println(actual);
		printMap(actual, expected);
		//ExtentTestManager.logMessage("<b>Bank details verification: "+tid+"</b>"+text);
	}

	public static void verifyDueDate(String tid, int days) {
		String today = changePatternOfDate("yyyy-MM-dd", new Date());
		String expectedDate = addDaysFromDate(today, days);
		String actual = getValue(tid, "transaction", "due_date");
		verifyEquals(actual, expectedDate,"<b>Due date Verification for the tid: "+tid+"</b>");
	}

	@Step("Verify Subscription values exist in TID {0} , cycleAmount {1}, next cycle date {2} and payment type {3}")
	public static void verifySubscriptionValuesInTID(String tid, String cycleAmount, String nextCycleDate, String paymentType) {
		Map<String,Object> subscriptionDetails = getSubscriptionDetails(tid);
		ExtentTestManager.logMessage("<b>Novalnet subscription details for the TID:"+tid+"</b>"+printMap(subscriptionDetails));
		String amount = subscriptionDetails.get("amount").toString();
		String nextCycle = subscriptionDetails.get("next_cycle_date").toString();
		String payment = subscriptionDetails.get("payment_type").toString();
		String subsTID = subscriptionDetails.get("tid").toString();

		var nextCycleObj = getDateFromString("yyyy-MM-dd HH:mm:SS", nextCycle);
		String updatedNextCycle = changePatternOfDate("yyyy-MM-dd", nextCycleObj);
		var nextCycleDateObj = getDateFromString("MMM dd, yyyy", nextCycleDate);
		String updatedNextCycleDate = changePatternOfDate("yyyy-MM-dd", nextCycleDateObj);

		String[] keys = new String[] {"amount","payment_type","tid","next_cycle_date"};
		String[] actual = new String[] {amount,payment,subsTID,updatedNextCycle};
		String[] expected = new String[] {cycleAmount,paymentType,tid,updatedNextCycleDate};
		printMap(keys, actual, expected);

//		verifyEquals(amount, cycleAmount, "<b>Suscription cycle amount : </b>");
//		verifyEquals(payment, paymentType, "<b>Suscription payment type : </b>");
//		verifyEquals(subsTID, tid, "<b>Suscription tid : </b>");
//		verifyEquals(updatedNextCycle, updatedNextCycleDate, "<b>Suscription next cycle date :</b>");
	}

	@Step("Verify Subscription values exist in TID {0} , cycleAmount {1}, and payment type {2}")
	public static void verifySubscriptionValuesInTID(String tid, String cycleAmount, String paymentType) {
		Map<String,Object> subscriptionDetails = getSubscriptionDetails(tid);
		ExtentTestManager.logMessage("<b>Novalnet subscription details for the TID:"+tid+"</b>"+printMap(subscriptionDetails));
		String amount = subscriptionDetails.get("amount").toString();
		String payment = subscriptionDetails.get("payment_type").toString();
		String subsTID = subscriptionDetails.get("tid").toString();

		String[] keys = new String[] {"amount","payment_type","tid"};
		String[] actual = new String[] {amount,payment,subsTID};
		String[] expected = new String[] {cycleAmount,paymentType,tid};
		printMap(keys, actual, expected);

//		verifyEquals(amount, cycleAmount, "<b>Suscription cycle amount : </b>");
//		verifyEquals(payment, paymentType, "<b>Suscription payment type : </b>");
//		verifyEquals(subsTID, tid, "<b>Suscription tid : </b>");
	}

	@Step("Verify Subscription values exist in TID {0} and payment type {1}")
	public static void verifySubscriptionPaymentInTID(String tid, String paymentType) {
		Map<String,Object> subscriptionDetails = getSubscriptionDetails(tid);
		String payment = subscriptionDetails.get("payment_type").toString();
		String subsTID = subscriptionDetails.get("tid").toString();
		verifyEquals(payment, paymentType, "<b>Suscription payment type : </b>");
		verifyEquals(subsTID, tid, "<b>Suscription tid : </b>");
	}

	@Step("Verify Subscription values exist in TID {0} and cycleAmount {1}")
	public static void verifySubscriptionCycleAmountInTID(String tid, String cycleAmount) {
		Map<String,Object> subscriptionDetails = getSubscriptionDetails(tid);
		String amount = subscriptionDetails.get("amount").toString();
		verifyEquals(amount, cycleAmount, "<b>Suscription cycle amount : </b>");
	}

	public static void verifySubscriptionNextPaymentDateInTID(String tid, String nextCycleDate) {
		Map<String,Object> subscriptionDetails = getSubscriptionDetails(tid);
		String nextCycle = subscriptionDetails.get("next_cycle_date").toString();

		var nextCycleObj = getDateFromString("yyyy-MM-dd HH:mm:SS", nextCycle);
		String updatedNextCycle = changePatternOfDate("yyyy-MM-dd", nextCycleObj);
		var nextCycleDateObj = getDateFromString("MMM dd, yyyy", nextCycleDate);
		String updatedNextCycleDate = changePatternOfDate("yyyy-MM-dd", nextCycleDateObj);

		verifyEquals(updatedNextCycle, updatedNextCycleDate, "<b>Suscription next cycle date :</b>");
	}

	@Step("Verify Subscription next payment date in TID {0} , next cycle date {1}")
	public static void verifySubscriptionNextPaymentDateInTID(String tid, String nextCycleDate, String pattern) {
		Map<String,Object> subscriptionDetails = getSubscriptionDetails(tid);
		String nextCycle = subscriptionDetails.get("next_cycle_date").toString();

		var nextCycleObj = getDateFromString("yyyy-MM-dd HH:mm:SS", nextCycle);
		String updatedNextCycle = changePatternOfDate("yyyy-MM-dd", nextCycleObj);
		var nextCycleDateObj = getDateFromString(pattern, nextCycleDate);
		String updatedNextCycleDate = changePatternOfDate("yyyy-MM-dd", nextCycleDateObj);

		verifyEquals(updatedNextCycle, updatedNextCycleDate, "<b>Suscription next cycle date :</b>");
	}

	public static String getNextCycleDateInYMD(String tid) {
		var date =  getValue(tid, "subscription", "next_cycle_date");
		var dateObj = getDateFromString("yyyy-MM-dd HH:mm:SS", date);
		return changePatternOfDate("yyyy-MM-dd", dateObj);
	}

	@Step("Verify TID {0} with amount {1}, status {2}, paymentName {3} and due date {4}")
	public static void verifyTIDInformation(String tid,String amount,String status,String paymentName, int dueDateInDays, int defaultDueDateInDays) {
		Map<String, Object> tidDetails = getTransactionDetails(tid);
		String tidStatusInNovalnet = tidDetails.get("status").toString();
		String tidAmountInNovalnet = tidDetails.get("amount").toString();
		String paymentNameInNovalnet = tidDetails.get("payment_type").toString();
		String dueDateNameInNovalnet = tidDetails.get("due_date").toString();
		String testModeInNovalnet = tidDetails.get("test_mode").toString();
		String today = changePatternOfDate("yyyy-MM-dd", new Date());
		String expectedDueDate = addDaysFromDate(today, dueDateInDays);
		if(defaultDueDateInDays == 2 && (paymentNameInNovalnet.equals("GUARANTEED_DIRECT_DEBIT_SEPA") || paymentNameInNovalnet.equals("DIRECT_DEBIT_SEPA") || paymentNameInNovalnet.equals("INSTALMENT_DIRECT_DEBIT_SEPA"))){
			expectedDueDate = getSEPADueDate(dueDateInDays);
		}
		String[] keys = new String[] {"amount","status","payment_type","due_date","test_mode"};
		String[] actual = new String[] {tidAmountInNovalnet,tidStatusInNovalnet,paymentNameInNovalnet,dueDateNameInNovalnet,testModeInNovalnet};
		String[] expected = new String[] {amount,status,paymentName,expectedDueDate,"1"};
		printMap(keys, actual, expected);
	}


}
