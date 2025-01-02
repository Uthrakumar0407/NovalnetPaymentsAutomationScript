package com.nn.callback;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.nn.reports.AllureManager;
import org.json.JSONObject;

import com.aventstack.extentreports.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nn.constants.Constants;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;
import org.testng.Assert;

public class CallbackProperties {

	private String callbackURL = System.getProperty("CALLBACK_URL");
	private String paymentAccesskey= System.getProperty("NOVALNET_ACCESSKEY");
	private String event = "";
	private String paymentType= "";
	private String cancelType= "";
	private String tid= "";
	private String callbackValueOrAmount= "";
	private String communicationBreakStatus= "";
	private String updateType= "";
	private String cyclesExecuted= "";
	private String nextCycleDate= "";
	private String pendingCycles= "";
	private String cycleDueDate= "";
	private String[] cyclesDates= new String[]{""};
	private String nextSubscriptionCycleDate= "";
	private String subscriptionChangePaymentType= "";
	private String subscriptionAmount= "";
	private String orderNumber = "";

	private String transactionStatus;


	public void setCallbackURL(String callbackURL) {
		this.callbackURL = callbackURL;
	}

	public String getCallbackURL() {
		return callbackURL;
	}

	public void setPaymentAccesskey(String paymentAccesskey) {
		this.paymentAccesskey = paymentAccesskey;
	}

	public String getPaymentAccesskey() {
		return paymentAccesskey;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getEvent() {
		return event;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setTID(String tid) {
		this.tid = tid;
	}

	public String getTID() {
		return tid;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setCallbackValueOrAmount(String callbackValueOrAmount) {
		this.callbackValueOrAmount = callbackValueOrAmount;
	}

	public String getCallbackValueOrAmount() {
		return callbackValueOrAmount;
	}

	public void setCommunicationBreakStatus(String communicationBreakStatus) {
		this.communicationBreakStatus = communicationBreakStatus;
	}

	public String getCommunicationBreakStatus() {
		return communicationBreakStatus;
	}

	public void setUpdateType(String updateType) {
		this.updateType = updateType;
	}

	public String getUpdateType() {
		return updateType;
	}

	public void setCyclesExecuted(String cyclesExecuted) {
		this.cyclesExecuted = cyclesExecuted;
	}

	public String getCyclesExecuted() {
		return cyclesExecuted;
	}

	public void setNextCycleDate(String nextCycleDate) {
		this.nextCycleDate = nextCycleDate;
	}

	public String getNextCycleDate() {
		return nextCycleDate;
	}

	public void setPendingCycles(String pendingCycles) {
		this.pendingCycles = pendingCycles;
	}

	public String getPendingCycles() {
		return pendingCycles;
	}

	public void setCycleDueDate(String cycleDueDate) {
		this.cycleDueDate = cycleDueDate;
	}

	public String getCycleDueDate() {
		return cycleDueDate;
	}

	public void setCyclesDates(String[] cyclesDates) {
		this.cyclesDates = cyclesDates;
	}

	public String[] getCyclesDates() {
		return cyclesDates;
	}

	public void setNextSubscriptionCycleDate(String nextSubscriptionCycleDate) {
		this.nextSubscriptionCycleDate = nextSubscriptionCycleDate;
	}

	public String getNextSubscriptionCycleDate() {
		return nextSubscriptionCycleDate;
	}

	public void setSubscriptionChangePaymentType(String subscriptionChangePaymentType) {
		this.subscriptionChangePaymentType = subscriptionChangePaymentType;
	}

	public String getSubscriptionChangePaymentType() {
		return subscriptionChangePaymentType;
	}

	public void setSubscriptionAmount(String subscriptionAmount) {
		this.subscriptionAmount = subscriptionAmount;
	}

	public String getSubscriptionAmount() {
		return subscriptionAmount;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public void resetVariableToDefault(){
		setOrderNumber("");
		setCallbackValueOrAmount("");
		setPendingCycles("");
		setCyclesExecuted("");
	}

	public String sendCallbackRequest() {
		return callBackRequest(
				getTID(),
				getPaymentAccesskey(),
				getEvent(),
				getCallbackURL(),
				getPaymentType(),
				getCallbackValueOrAmount(),
				getUpdateType(),
				getCyclesExecuted(),
				getNextCycleDate(),
				getPendingCycles(),
				getCycleDueDate(),
				getNextSubscriptionCycleDate(),
				getSubscriptionChangePaymentType(),
				getSubscriptionAmount(),
				getCommunicationBreakStatus(),
				getCyclesDates(),
				getOrderNumber(),
				getTransactionStatus(),
				getCancelType()
		);
	}


	public String callBackRequest(
			String TID,
			String accessKey,
			String eventType,
			String callBackURL,
			String paymentType,
			String callbackAmountOrValueUpdate,
			String updateType,
			String cyclesExecuted,
			String nextCycleDate,
			String pendingCycles,
			String cycleDueDate,
			String nextSubscriptionCycleDate,
			String subscriptionChangePaymentType,
			String subscriptionAmount,
			String communicationBreakStatus,
			String[] cyclesDates,
			String orderNumber,
			String transactionStatus,
			String cancelType) {

		//Event and SubEvents
		Map<String, String[]> eventFollowPaymentTypes = new HashMap<String, String[]>();

		String[] events = {"PAYMENT","CREDIT","TRANSACTION_CAPTURE","TRANSACTION_CANCEL","TRANSACTION_UPDATE","TRANSACTION_REFUND","CHARGEBACK","SUBSCRIPTION_CANCEL","SUBSCRIPTION_UPDATE","SUBSCRIPTION_SUSPEND","SUBSCRIPTION_REACTIVATE","RENEWAL","INSTALMENT","INSTALMENT_CANCEL", "PAYMENT_REMINDER_1", "PAYMENT_REMINDER_2", "SUBMISSION_TO_COLLECTION_AGENCY"};

		String[] paymentPaymentTypes = {"CREDITCARD", "DIRECT_DEBIT_SEPA", "GUARANTEED_DIRECT_DEBIT_SEPA", "INSTALMENT_DIRECT_DEBIT_SEPA", "INVOICE", "GUARANTEED_INVOICE", "INSTALMENT_INVOICE", "PREPAYMENT", "CASHPAYMENT", "ONLINE_BANK_TRANSFER", "GIROPAY", "ONLINE_TRANSFER", "IDEAL", "EPS", "PRZELEWY24", "BANCONTACT", "POSTFINANCE", "POSTFINANCE_CARD", "TRUSTLY", "PAYPAL", "MULTIBANCO", "APPLEPAY", "ALIPAY", "WECHATPAY", "GOOGLEPAY","BLIK","PAYCONIQ","MBWAY"};
		String[] creditPaymentTypes = {"INVOICE_CREDIT", "CASHPAYMENT_CREDIT", "BANK_TRANSFER_BY_END_CUSTOMER", "MULTIBANCO_CREDIT", "ONLINE_TRANSFER_CREDIT", "CREDIT_ENTRY_SEPA", "DEBT_COLLECTION_SEPA", "CREDIT_ENTRY_CREDITCARD", "DEBT_COLLECTION_CREDITCARD", "CREDITCARD_REPRESENTMENT", "CREDIT_ENTRY_DE", "DEBT_COLLECTION_DE", "APPLEPAY_REPRESENTMENT", "GOOGLEPAY_REPRESENTMENT","REFUND_REVERSAL"};
		String[] refundPaymentTypes = {"CREDITCARD_BOOKBACK", "GUARANTEED_SEPA_BOOKBACK", "GUARANTEED_INVOICE_BOOKBACK", "REFUND_BY_BANK_TRANSFER_EU", "PRZELEWY24_REFUND", "INSTALMENT_INVOICE_BOOKBACK", "INSTALMENT_SEPA_BOOKBACK", "POSTFINANCE_REFUND", "CASHPAYMENT_REFUND", "PAYPAL_BOOKBACK", "TRUSTLY_REFUND", "ALIPAY_REFUND", "WECHATPAY_REFUND", "APPLEPAY_BOOKBACK", "GOOGLEPAY_BOOKBACK","BLIK_REFUND","MBWAY_REFUND", "DIRECT_DEBIT_ACH_REFUND"};
		String[] chargebackPaymentTypes = {"CREDITCARD_CHARGEBACK", "RETURN_DEBIT_SEPA", "PAYPAL_CHARGEBACK", "REVERSAL", "APPLEPAY_CHARGEBACK", "GOOGLEPAY_CHARGEBACK"};
		String[] capturePaymentTypes = {"CREDITCARD", "DIRECT_DEBIT_SEPA", "APPLEPAY", "PAYPAL", "GOOGLEPAY", "INVOICE", "PREPAYMENT", "GUARANTEED_INVOICE", "GUARANTEED_DIRECT_DEBIT_SEPA", "INSTALMENT_INVOICE", "INSTALMENT_DIRECT_DEBIT_SEPA"};
		String[] cancelPaymentTypes = {"CREDITCARD", "DIRECT_DEBIT_SEPA", "APPLEPAY", "PAYPAL", "GOOGLEPAY", "INVOICE", "PREPAYMENT", "GUARANTEED_INVOICE", "GUARANTEED_DIRECT_DEBIT_SEPA", "INSTALMENT_INVOICE", "INSTALMENT_DIRECT_DEBIT_SEPA", "PRZELEWY24", "POSTFINANCE", "POSTFINANCE_CARD", "TRUSTLY"};
		String[] updatePaymentTypes = {"CREDITCARD", "DIRECT_DEBIT_SEPA", "GUARANTEED_DIRECT_DEBIT_SEPA", "INSTALMENT_DIRECT_DEBIT_SEPA", "INVOICE", "GUARANTEED_INVOICE", "INSTALMENT_INVOICE", "PREPAYMENT", "CASHPAYMENT", "ONLINE_BANK_TRANSFER", "GIROPAY", "ONLINE_TRANSFER", "IDEAL", "EPS", "PRZELEWY24", "BANCONTACT", "POSTFINANCE", "POSTFINANCE_CARD", "TRUSTLY", "PAYPAL", "MULTIBANCO", "APPLEPAY", "ALIPAY", "WECHATPAY", "GOOGLEPAY","MBWAY","PAYCONIQ"};
		String[] instalmentPaymentTypes = {"INSTALMENT_DIRECT_DEBIT_SEPA", "INSTALMENT_INVOICE"};
		String[] subscriptionPaymentTypes = {"CREDITCARD", "DIRECT_DEBIT_SEPA", "APPLEPAY", "PAYPAL", "GOOGLEPAY", "INVOICE", "PREPAYMENT", "GUARANTEED_INVOICE", "GUARANTEED_DIRECT_DEBIT_SEPA"};
		String[] paymentReminderPaymentTypes = {"CREDITCARD", "DIRECT_DEBIT_SEPA", "APPLEPAY", "PAYPAL", "GOOGLEPAY", "REVERSAL", "INVOICE", "GUARANTEED_INVOICE", "GUARANTEED_DIRECT_DEBIT_SEPA", "INSTALMENT_INVOICE", "INSTALMENT_DIRECT_DEBIT_SEPA"};
		String[] collectionPaymentTypes = {"CREDITCARD", "DIRECT_DEBIT_SEPA", "APPLEPAY", "PAYPAL", "GOOGLEPAY", "REVERSAL", "INVOICE"};



		eventFollowPaymentTypes.put("PAYMENT", paymentPaymentTypes);
		eventFollowPaymentTypes.put("CREDIT", creditPaymentTypes);
		eventFollowPaymentTypes.put("TRANSACTION_CAPTURE", capturePaymentTypes);
		eventFollowPaymentTypes.put("TRANSACTION_CANCEL", cancelPaymentTypes);
		eventFollowPaymentTypes.put("TRANSACTION_UPDATE", updatePaymentTypes);
		eventFollowPaymentTypes.put("TRANSACTION_REFUND", refundPaymentTypes);
		eventFollowPaymentTypes.put("CHARGEBACK", chargebackPaymentTypes);
		eventFollowPaymentTypes.put("SUBSCRIPTION_CANCEL", subscriptionPaymentTypes);
		eventFollowPaymentTypes.put("SUBSCRIPTION_UPDATE", subscriptionPaymentTypes);
		eventFollowPaymentTypes.put("SUBSCRIPTION_SUSPEND", subscriptionPaymentTypes);
		eventFollowPaymentTypes.put("SUBSCRIPTION_REACTIVATE", subscriptionPaymentTypes);
		eventFollowPaymentTypes.put("RENEWAL", subscriptionPaymentTypes);
		eventFollowPaymentTypes.put("INSTALMENT", instalmentPaymentTypes);
		eventFollowPaymentTypes.put("INSTALMENT_CANCEL", instalmentPaymentTypes);
		eventFollowPaymentTypes.put("PAYMENT_REMINDER_1", paymentReminderPaymentTypes);
		eventFollowPaymentTypes.put("PAYMENT_REMINDER_2", paymentReminderPaymentTypes);
		eventFollowPaymentTypes.put("SUBMISSION_TO_COLLECTION_AGENCY", collectionPaymentTypes);

		//Event types based on individual Payments
		Map<String, String[]> individualPaymentTypes = new HashMap<String, String[]>();

		String[] creditCardPaymentTypes = {"CREDITCARD", "CREDITCARD_CHARGEBACK", "CREDITCARD_BOOKBACK", "CREDIT_ENTRY_CREDITCARD", "DEBT_COLLECTION_CREDITCARD","CREDITCARD_REPRESENTMENT","BANK_TRANSFER_BY_END_CUSTOMER"};
		String[] directDebitSepaPaymentTypes = {"DIRECT_DEBIT_SEPA", "RETURN_DEBIT_SEPA", "REFUND_BY_BANK_TRANSFER_EU", "CREDIT_ENTRY_SEPA", "DEBT_COLLECTION_SEPA", "BANK_TRANSFER_BY_END_CUSTOMER"};
		String[] invoicePaymentTypes = {"INVOICE", "INVOICE_CREDIT", "REFUND_BY_BANK_TRANSFER_EU", "BANK_TRANSFER_BY_END_CUSTOMER", "DEBT_COLLECTION_DE"};
		String[] prepaymentPaymentTypes = {"PREPAYMENT", "INVOICE_CREDIT", "REFUND_BY_BANK_TRANSFER_EU", "BANK_TRANSFER_BY_END_CUSTOMER", "DEBT_COLLECTION_DE"};
		String[] multibancoPaymentTypes = {"MULTIBANCO", "MULTIBANCO_CREDIT"};
		String[] payPalPaymentTypes = {"PAYPAL", "PAYPAL_BOOKBACK", "PAYPAL_CHARGEBACK", "BANK_TRANSFER_BY_END_CUSTOMER", "CREDIT_ENTRY_DE"};
		String[] onlineTransferPaymentTypes = {"ONLINE_TRANSFER", "REFUND_BY_BANK_TRANSFER_EU", "ONLINE_TRANSFER_CREDIT", "REVERSAL", "DEBT_COLLECTION_DE", "BANK_TRANSFER_BY_END_CUSTOMER"};
		String[] onlineBankTransferPaymentTypes = {"ONLINE_BANK_TRANSFER", "REFUND_BY_BANK_TRANSFER_EU", "DEBT_COLLECTION_DE", "REVERSAL", "ONLINE_TRANSFER_CREDIT"};
		String[] bancontactPaymentTypes = {"BANCONTACT", "REFUND_BY_BANK_TRANSFER_EU"};
		String[] idealPaymentTypes = {"IDEAL", "REFUND_BY_BANK_TRANSFER_EU", "BANK_TRANSFER_BY_END_CUSTOMER", "REVERSAL", "DEBT_COLLECTION_DE", "ONLINE_TRANSFER_CREDIT"};
		String[] epsPaymentTypes = {"EPS", "REFUND_BY_BANK_TRANSFER_EU", "ONLINE_TRANSFER_CREDIT"};
		String[] giropayPaymentTypes = {"GIROPAY", "REFUND_BY_BANK_TRANSFER_EU", "ONLINE_TRANSFER_CREDIT"};
		String[] przelewy24PaymentTypes = {"PRZELEWY24", "PRZELEWY24_REFUND"};
		String[] cashpaymentPaymentTypes = {"CASHPAYMENT", "CASHPAYMENT_REFUND", "CASHPAYMENT_CREDIT"};
		String[] postFinancePaymentTypes = {"POSTFINANCE", "POSTFINANCE_REFUND"};
		String[] postFinanceCardPaymentTypes = {"POSTFINANCE_CARD", "POSTFINANCE_REFUND"};
		String[] guaranteedInvoicePaymentTypes = {"GUARANTEED_INVOICE", "GUARANTEED_INVOICE_BOOKBACK","BANK_TRANSFER_BY_END_CUSTOMER","REFUND_BY_BANK_TRANSFER_EU"};
		String[] guaranteedDirectDebitSepaPaymentTypes = {"GUARANTEED_DIRECT_DEBIT_SEPA", "GUARANTEED_SEPA_BOOKBACK","REFUND_BY_BANK_TRANSFER_EU"};
		String[] instalmentInvoicePaymentTypes = {"INSTALMENT_INVOICE", "INSTALMENT_INVOICE_BOOKBACK","BANK_TRANSFER_BY_END_CUSTOMER","REFUND_BY_BANK_TRANSFER_EU"};
		String[] instalmentDirectDebitSepaPaymentTypes = {"INSTALMENT_DIRECT_DEBIT_SEPA", "INSTALMENT_SEPA_BOOKBACK","REFUND_BY_BANK_TRANSFER_EU"};
		String[] applepayPaymentTypes = {"APPLEPAY", "APPLEPAY_REPRESENTMENT", "APPLEPAY_BOOKBACK", "APPLEPAY_CHARGEBACK"};
		String[] googlepayPaymentTypes = {"GOOGLEPAY", "GOOGLEPAY_REPRESENTMENT", "GOOGLEPAY_BOOKBACK", "GOOGLEPAY_CHARGEBACK"};
		String[] alipayPaymentTypes = {"ALIPAY", "ALIPAY_REFUND"};
		String[] wechatpayPaymentTypes = {"WECHATPAY", "WECHATPAY_REFUND"};
		String[] trustlyPaymentTypes = {"TRUSTLY", "ONLINE_TRANSFER_CREDIT", "DEBT_COLLECTION_DE", "TRUSTLY_REFUND", "REVERSAL"};
		String[] blikPaymentTypes = {"BLIK","BLIK_REFUND"};
		// Adding for new Instalament change - 17Nov2023
		String[] instalmentCancelType = {"ALL_CYCLES", "REMAINING_CYCLES"};
		String[] payconiqPaymnetTypes = {"PAYCONIQ","REFUND_REVERSAL","REFUND_BY_BANK_TRANSFER_EU"};
		String[] mbWayPaymentTypes = {"MBWAY","MBWAY_REFUND"};
		String[] directDebitACHPaymentTypes = {"DIRECT_DEBIT_ACH","DIRECT_DEBIT_ACH_REFUND"};

		individualPaymentTypes.put("CREDITCARD", creditCardPaymentTypes);
		individualPaymentTypes.put("DIRECT_DEBIT_SEPA", directDebitSepaPaymentTypes);
		individualPaymentTypes.put("INVOICE", invoicePaymentTypes);
		individualPaymentTypes.put("PREPAYMENT", prepaymentPaymentTypes);
		individualPaymentTypes.put("MULTIBANCO", multibancoPaymentTypes);
		individualPaymentTypes.put("PAYPAL", payPalPaymentTypes);
		individualPaymentTypes.put("ONLINE_TRANSFER", onlineTransferPaymentTypes);
		individualPaymentTypes.put("ONLINE_BANK_TRANSFER", onlineBankTransferPaymentTypes);
		individualPaymentTypes.put("BANCONTACT", bancontactPaymentTypes);
		individualPaymentTypes.put("IDEAL", idealPaymentTypes);
		individualPaymentTypes.put("EPS", epsPaymentTypes);
		individualPaymentTypes.put("GIROPAY", giropayPaymentTypes);
		individualPaymentTypes.put("PRZELEWY24", przelewy24PaymentTypes);
		individualPaymentTypes.put("CASHPAYMENT", cashpaymentPaymentTypes);
		individualPaymentTypes.put("POSTFINANCE", postFinancePaymentTypes);
		individualPaymentTypes.put("POSTFINANCE_CARD", postFinanceCardPaymentTypes);
		individualPaymentTypes.put("GUARANTEED_INVOICE", guaranteedInvoicePaymentTypes);
		individualPaymentTypes.put("GUARANTEED_DIRECT_DEBIT_SEPA", guaranteedDirectDebitSepaPaymentTypes);
		individualPaymentTypes.put("INSTALMENT_INVOICE", instalmentInvoicePaymentTypes);
		individualPaymentTypes.put("INSTALMENT_DIRECT_DEBIT_SEPA", instalmentDirectDebitSepaPaymentTypes);
		individualPaymentTypes.put("APPLEPAY", applepayPaymentTypes);
		individualPaymentTypes.put("GOOGLEPAY", googlepayPaymentTypes);
		individualPaymentTypes.put("ALIPAY", alipayPaymentTypes);
		individualPaymentTypes.put("WECHATPAY", wechatpayPaymentTypes);
		individualPaymentTypes.put("TRUSTLY", trustlyPaymentTypes);
		individualPaymentTypes.put("BLIK",blikPaymentTypes);
		individualPaymentTypes.put("PAYCONIQ", payconiqPaymnetTypes);
		individualPaymentTypes.put("MBWAY",mbWayPaymentTypes);
		individualPaymentTypes.put("DIRECT_DEBIT_ACH", directDebitACHPaymentTypes);

		String errorMessage = "";
		StringBuilder transactionDetails = getTransactionDetails(TID,accessKey);
		String status = "";
		String tid = "";
		String parent_tid = "";
		JSONObject transactionJsonObject = new JSONObject();
		JSONObject eventJsonObject = new JSONObject();
		JSONObject refundJsonObject = new JSONObject();
		JSONObject transactionDetailsJsonObject = new JSONObject();
		JSONObject resultJsonObject = new JSONObject();

		if(transactionDetails != null) {
			transactionDetailsJsonObject = new JSONObject(transactionDetails.toString());
			resultJsonObject = transactionDetailsJsonObject.getJSONObject("result");
			String originalTIDStatusCode = resultJsonObject.get("status_code").toString();
			String originalTIDStatusText = resultJsonObject.get("status_text").toString();
			String originalTIDStatus = resultJsonObject.get("status").toString();
			if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
				JSONObject customerJsonObject = transactionDetailsJsonObject.getJSONObject("customer");
				transactionJsonObject = transactionDetailsJsonObject.getJSONObject("transaction");
				eventJsonObject = new JSONObject();
				refundJsonObject = new JSONObject();
				String paymentName = transactionJsonObject.getString("payment_type");
				status = transactionJsonObject.get("status").toString();

				if(!callBackURL.equals("")) {
					if(Arrays.asList(events).contains(eventType)) {
						String[] subEventList = eventFollowPaymentTypes.get(eventType);
						if(Arrays.asList(subEventList).contains(paymentType)) {
							String[] eachPaymentList;
							if(individualPaymentTypes.containsKey(paymentName))
								eachPaymentList = individualPaymentTypes.get(paymentName);
							else {
								eachPaymentList = new String[]{""};
								errorMessage+="Invalid paymentName "+paymentName;
							}
							if(Arrays.asList(eachPaymentList).contains(paymentType)) {


								// create parent and child TID
								if("CREDIT".equals(eventType) || "TRANSACTION_REFUND".equals(eventType) || "CHARGEBACK".equals(eventType) || "INSTALMENT".equals(eventType) || "INSTALMENT_CANCEL".equals(eventType) || "RENEWAL".equals(eventType)) {
									parent_tid = transactionJsonObject.get("tid").toString();
									tid = getRandomTID();
									eventJsonObject.put("parent_tid", parent_tid);
									eventJsonObject.put("tid", tid);
									//transactionJsonObject.put("tid", tid);
									if(!status.equals("CONFIRMED")) {
										transactionJsonObject.put("status", "CONFIRMED");
										transactionJsonObject.put("status_code", "100");
									}
								}else if("SUBSCRIPTION_UPDATE".equals(eventType)){
									tid = getRandomTID();
									eventJsonObject.put("tid", tid);
								}else {
									tid = transactionJsonObject.get("tid").toString();
									eventJsonObject.put("tid", tid);
								}

								//Removing payment data
								if(!"PAYMENT".equals(eventType) && !"INSTALMENT".equals(eventType)) {
									if(transactionJsonObject.has("payment_data")) {
										transactionJsonObject.remove("payment_data");
									}
									if(transactionJsonObject.has("nearest_stores")) {
										transactionJsonObject.remove("nearest_stores");
										transactionJsonObject.remove("slip_id");
									}
								}

								// Capture
								if("TRANSACTION_CAPTURE".equals(eventType)) {
									transactionJsonObject.put("status_code", 100);
									if(paymentName.equals("INVOICE"))
										transactionJsonObject.put("status", "PENDING");
									else if(paymentName.equals("INSTALMENT_INVOICE") || paymentName.equals("INSTALMENT_DIRECT_DEBIT_SEPA")) {
										JSONObject instalmentJsonObject = transactionDetailsJsonObject.getJSONObject("instalment");
										Map<String,String> cycleDatesMap = new HashMap<String,String>();
										for(int i=0;i<cyclesDates.length;i++)
											cycleDatesMap.put(String.valueOf(i+1), cyclesDates[i].trim());
										instalmentJsonObject.put("cycle_dates", cycleDatesMap);
										instalmentJsonObject.put("cycles_executed", Integer.parseInt(cyclesExecuted));
										instalmentJsonObject.put("next_cycle_date", nextCycleDate);
										instalmentJsonObject.put("pending_cycles", Integer.parseInt(pendingCycles));
										transactionJsonObject.put("status", "CONFIRMED");
										if(transactionJsonObject.getInt("amount") == instalmentJsonObject.getInt("cycle_amount"))
											transactionJsonObject.put("amount",instalmentJsonObject.getInt("cycle_amount")*(Integer.parseInt(cyclesExecuted)+Integer.parseInt(pendingCycles)));
									} else
										transactionJsonObject.put("status", "CONFIRMED");
								}

								// Cancel
								else if("TRANSACTION_CANCEL".equals(eventType)){
									transactionJsonObject.put("status_code", 103);
									transactionJsonObject.put("status", "DEACTIVATED");
									transactionJsonObject.remove("currency");
									transactionJsonObject.remove("amount");
									if(transactionDetailsJsonObject.has("instalment")){
										transactionDetailsJsonObject.remove("instalment");
									}
								}

								// Refund
								else if("TRANSACTION_REFUND".equals(eventType)) {
									if(!callbackAmountOrValueUpdate.equals("")) {
										refundJsonObject.put("amount", callbackAmountOrValueUpdate);
									}else {
										if(paymentType.equals("INSTALMENT_SEPA_BOOKBACK") || paymentType.equals("INSTALMENT_INVOICE_BOOKBACK")) {
											JSONObject instalment = transactionDetailsJsonObject.getJSONObject("instalment");
											refundJsonObject.put("amount", instalment.get("cycle_amount").toString());
											transactionDetailsJsonObject.remove("instalment");
										} else
											refundJsonObject.put("amount", transactionJsonObject.get("amount").toString());
									}
									refundJsonObject.put("currency", transactionJsonObject.get("currency").toString());
									refundJsonObject.put("payment_type", paymentType);
									refundJsonObject.put("tid", tid);
									transactionJsonObject.put("refund",refundJsonObject);
									transactionJsonObject.put("refunded_amount", refundJsonObject.get("amount"));
								}

								//Initial level... communication break
								else if("PAYMENT".equals(eventType)) {
									if(!orderNumber.equals("")) {
										tid = getRandomTID();
										eventJsonObject.put("tid", tid);
										transactionJsonObject.put("tid", tid);
										transactionJsonObject.put("order_no", orderNumber);
									}
									String resultStatus = resultJsonObject.getString("status");
									if(communicationBreakStatus.equals("FAILURE")) {
										transactionJsonObject.put("status", "FAILURE");
										transactionJsonObject.remove("status_code");
										resultJsonObject.put("status", "FAILURE");
										resultJsonObject.put("status_text", "User Abort or 3D Secure Authentication Failed or Unexpected Error");
									}else {
										if(resultStatus.equals("FAILURE")) {
											resultJsonObject.put("status", "SUCCESS");
											resultJsonObject.put("status_code", "100");
											resultJsonObject.put("status_text", "Successful");
											transactionJsonObject.put("status", "CONFIRMED");
											if(transactionStatus != null)
												transactionJsonObject.put("status", transactionStatus);
											transactionJsonObject.put("status_code", "100");
											if(!callbackAmountOrValueUpdate.equals(""))
												transactionJsonObject.put("amount", callbackAmountOrValueUpdate);
										}
									}
								}

								// Credit
								else if("CREDIT".equals(eventType)) {
									// Online Transfer credit with dummy tid
									if(!orderNumber.equals("")) {
										transactionJsonObject.put("order_no", orderNumber);
									}
									if(!callbackAmountOrValueUpdate.equals("")) {
										transactionJsonObject.put("amount", callbackAmountOrValueUpdate);
									}
									if(transactionJsonObject.has("bank_details")) {
										JSONObject bank_details = transactionJsonObject.getJSONObject("bank_details");
										JSONObject billing = customerJsonObject.getJSONObject("billing");
										bank_details.put("country_code", billing.getString("country_code"));
										bank_details.put("reference", transactionJsonObject.getString("invoice_ref"));
										transactionJsonObject.put("credit_details",bank_details);
										transactionJsonObject.remove("bank_details");
									}
									transactionJsonObject.put("payment_type", paymentType);
								}

								// Update
								else if("TRANSACTION_UPDATE".equals(eventType)) {
									transactionJsonObject.put("update_type", updateType);
									if(updateType.trim().equals("AMOUNT")) {
										transactionJsonObject.put("amount", Integer.parseInt(callbackAmountOrValueUpdate));
									}else if(updateType.trim().equals("DUE_DATE")) {
										transactionJsonObject.put("due_date", callbackAmountOrValueUpdate);
									}else if(updateType.trim().equals("STATUS")) {
										if(callbackAmountOrValueUpdate.equals("ON_HOLD") || callbackAmountOrValueUpdate.equals("CONFIRMED")  || callbackAmountOrValueUpdate.equals("DEACTIVATED")) {
											transactionJsonObject.put("status", callbackAmountOrValueUpdate);
											if((paymentName.equals("INSTALMENT_INVOICE") || paymentName.equals("INSTALMENT_DIRECT_DEBIT_SEPA") ) && (callbackAmountOrValueUpdate.equals("CONFIRMED") || callbackAmountOrValueUpdate.equals("ON_HOLD"))) {
												JSONObject instalmentJsonObject = transactionDetailsJsonObject.getJSONObject("instalment");
												if(callbackAmountOrValueUpdate.equals("CONFIRMED")){
													Map<String,String> cycleDatesMap = new HashMap<String,String>();
													for(int i=0;i<cyclesDates.length;i++)
														cycleDatesMap.put(String.valueOf(i+1), cyclesDates[i]);
													instalmentJsonObject.put("cycle_dates", cycleDatesMap);
													instalmentJsonObject.put("cycles_executed", Integer.parseInt(cyclesExecuted));
													instalmentJsonObject.put("next_cycle_date", nextCycleDate);
													instalmentJsonObject.put("pending_cycles", Integer.parseInt(pendingCycles));
												}
												if(!cyclesExecuted.equals("") && !pendingCycles.equals("")){
													if(transactionJsonObject.getInt("amount") == instalmentJsonObject.getInt("cycle_amount"))
														transactionJsonObject.put("amount",instalmentJsonObject.getInt("cycle_amount")*(Integer.parseInt(cyclesExecuted)+Integer.parseInt(pendingCycles)));
												}
											}
											if(paymentName.equals("INSTALMENT_INVOICE") || paymentName.equals("GUARANTEED_INVOICE")){
												if(!transactionJsonObject.has("due_date")){
													var dueDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now().plusDays(30));
													transactionJsonObject.put("due_date",dueDate);
												}
											}


										} else
											errorMessage += " Invalid update status !!! ON_HOLD or CONFIRMED or DEACTIVATED only applicable sent it in the callbackAmountOrValueUpdate variable";
									}else {
										errorMessage += " Invalid update type !!! ";
									}
								}

								// Chargeback
								else if("CHARGEBACK".equals(eventType)) {
									if(!callbackAmountOrValueUpdate.equals(""))
										transactionJsonObject.put("amount", callbackAmountOrValueUpdate);
									transactionJsonObject.put("reason", "Fraudulent Transaction - No cardholder authentication");
									transactionJsonObject.put("reason_code", "48376");
									transactionJsonObject.put("payment_type", paymentType);
								}

								// Instalment
								else if("INSTALMENT".equals(eventType)) {
									transactionJsonObject.put("tid", tid);
									if(!cycleDueDate.equals(""))
										transactionJsonObject.put("due_date", cycleDueDate);
									JSONObject instalmentJsonObject = transactionDetailsJsonObject.getJSONObject("instalment");
									instalmentJsonObject.put("cycles_executed", Integer.parseInt(cyclesExecuted));
									if(!nextCycleDate.equals(""))
										instalmentJsonObject.put("next_cycle_date", nextCycleDate);
									instalmentJsonObject.put("pending_cycles", Integer.parseInt(pendingCycles));
									if(pendingCycles.equals("0")){
										if(instalmentJsonObject.has("next_cycle_date")){
											instalmentJsonObject.remove("next_cycle_date");
										}
									}
									instalmentJsonObject.remove("cycle_dates");
									instalmentJsonObject.remove("tid");
									instalmentJsonObject.remove("currency");
								}

								// Instalment Cancel
								else if("INSTALMENT_CANCEL".equals(eventType)) {
									JSONObject instalment = transactionDetailsJsonObject.getJSONObject("instalment");
									refundJsonObject.put("amount", instalment.get("cycle_amount").toString());
									refundJsonObject.put("currency", transactionJsonObject.get("currency").toString());
									if(paymentName.equals("INSTALMENT_INVOICE"))
										refundJsonObject.put("payment_type", "INSTALMENT_INVOICE_BOOKBACK");
									else if(paymentName.equals("INSTALMENT_DIRECT_DEBIT_SEPA"))
										refundJsonObject.put("payment_type", "INSTALMENT_SEPA_BOOKBACK");
									refundJsonObject.put("tid", tid);
									JSONObject instalmentData = new JSONObject();
									//adding jSon param for new instalmentCycle change (cancel_type)
									instalmentData.put("cancel_type", getCancelType());
									instalmentData.put("tid", parent_tid);
									transactionDetailsJsonObject.put("instalment", instalmentData);
									transactionJsonObject.put("refund",refundJsonObject);
								}

								//Subscription
								else if("RENEWAL".equals(eventType) || "SUBSCRIPTION_UPDATE".equals(eventType) || "SUBSCRIPTION_SUSPEND".equals(eventType) || "SUBSCRIPTION_REACTIVATE".equals(eventType) || "SUBSCRIPTION_CANCEL".equals(eventType)) {
									if(transactionDetailsJsonObject.has("subscription")) {
										JSONObject subscription = transactionDetailsJsonObject.getJSONObject("subscription");
										if("RENEWAL".equals(eventType)) {
											transactionJsonObject.put("amount", Integer.parseInt(subscription.get("amount").toString()));
											if(!nextSubscriptionCycleDate.isEmpty())
												subscription.put("next_cycle_date", nextSubscriptionCycleDate);
										}else if("SUBSCRIPTION_UPDATE".equals(eventType)) {
											if(!nextSubscriptionCycleDate.isEmpty())
												subscription.put("next_cycle_date", nextSubscriptionCycleDate);
											subscription.put("update_type",new String[] {"RENEWAL_DATE"});
											if(!subscriptionAmount.isEmpty())
												subscription.put("amount", Integer.parseInt(subscriptionAmount));
											subscription.put("update_type", new String[]{"RENEWAL_AMOUNT"});
											if(!subscriptionChangePaymentType.isEmpty()) {
												subscription.put("payment_type", subscriptionChangePaymentType);
												subscription.put("update_type", new String[]{"PAYMENT_DATA"});
												transactionJsonObject.put("payment_type", subscriptionChangePaymentType);
												transactionJsonObject.put("tid", tid);
											}
										}else if("SUBSCRIPTION_SUSPEND".equals(eventType) || "SUBSCRIPTION_CANCEL".equals(eventType) || "SUBSCRIPTION_REACTIVATE".equals(eventType)) {
											if("SUBSCRIPTION_SUSPEND".equals(eventType) || "SUBSCRIPTION_CANCEL".equals(eventType)){
												subscription.remove("amount");
												subscription.remove("currency");
												subscription.remove("next_cycle_date");
												subscription.remove("payment_type");
											}
											if("SUBSCRIPTION_CANCEL".equals(eventType)) {
												subscription.put("cancel_type", "EXPLICIT");
												subscription.put("reason", "Too Expensive");
											}
											if("SUBSCRIPTION_REACTIVATE".equals(eventType)) {
												subscription.remove("payment_type");
												if(nextSubscriptionCycleDate != "")
													subscription.put("next_cycle_date", nextSubscriptionCycleDate);
											}
											if(transactionJsonObject.has("payment_data"))
												transactionJsonObject.remove("payment_data");
											if(transactionJsonObject.has("bank_details"))
												transactionJsonObject.remove("bank_details");
										}
										if(!"RENEWAL".equals(eventType)) {
											transactionJsonObject.remove("amount");
											transactionJsonObject.remove("currency");
										}
										transactionDetailsJsonObject.put("subscription", subscription);
									}else {
										errorMessage += " It is not a Subscription TID !!! ";
									}
								}


								// Payment Reminder and Collection
								else if("PAYMENT_REMINDER_1".equals(eventType) || "PAYMENT_REMINDER_2".equals(eventType) || "SUBMISSION_TO_COLLECTION_AGENCY".equals(eventType)) {
									int orderAmount = Integer.parseInt(transactionJsonObject.get("amount").toString());
									Map<String,String> reminder = new HashMap<String,String>();
									reminder.put("claim_amount", String.valueOf(orderAmount));
									reminder.put("claim_charges", String.valueOf((int)(orderAmount * 0.2)));
									reminder.put("claim_fee", String.valueOf((int)(orderAmount * 0.1)));
									reminder.put("currency", transactionJsonObject.getString("currency"));
									String reminderAmount = String.valueOf(Integer.parseInt(reminder.get("claim_amount")) + Integer.parseInt(reminder.get("claim_charges")) + Integer.parseInt(reminder.get("claim_fee")));
									reminder.put("amount", reminderAmount);
									transactionDetailsJsonObject.put("reminder", reminder);
									if("SUBMISSION_TO_COLLECTION_AGENCY".equals(eventType)) {
										reminder.put("reference", "000025-F00002774");
										reminder.put("status_text", "Collection in progress");
										transactionDetailsJsonObject.put("collection", reminder);
									}
								}

								// To execute the followup events for orders confirmed from communication break. cause we get the result status as FAILURE for progress TIDs
								if(!"PAYMENT".equals(eventType) || !communicationBreakStatus.equals("FAILURE")){
									if(!originalTIDStatus.equals("SUCCESS")) {
										resultJsonObject.put("status", "SUCCESS");
										resultJsonObject.put("status_code", "100");
										resultJsonObject.put("status_text", "Successful");
										if(resultJsonObject.has("additional_message"))
											resultJsonObject.remove("additional_message");
									}
								}

							}else {
								errorMessage += " Invalid payment type for this TID!!! ";
							}
						}else {
							errorMessage += " Invalid payment type !!! or (event and payment type mismatched) ";
						}
					}else {
						errorMessage += " Invalid event type !!! ";
					}
				}else {
					errorMessage += " Please type callback url !!! ";
				}
			}else {
				errorMessage += originalTIDStatusText;
			}
		}else {
			errorMessage += " Invalid TID or TID length not equal to 17 !!! ";
		}


		if(errorMessage.equals("")) {
			status = resultJsonObject.get("status").toString();
			String amount = "";
			String currency = "";
			if(transactionJsonObject.has("amount"))
				amount += transactionJsonObject.get("amount").toString();
			if(transactionJsonObject.has("currency"))
				currency += transactionJsonObject.get("currency").toString();
			// create checksum
			String checkSum = generateCheckSum(tid,eventType,status,amount,currency,accessKey);
			eventJsonObject.put("checksum", checkSum);
			eventJsonObject.put("type", eventType);
			transactionDetailsJsonObject.put("event", eventJsonObject);
			setEventTID(transactionJsonObject.get("tid").toString());
			String callbackJsonString = transactionDetailsJsonObject.toString();
			StringBuilder callback = new StringBuilder(callbackJsonString);
			//System.out.println("***********************Callback Request********************************");
			//System.out.println("\n");
			//System.out.println(printPrettyJson(callback));
			//System.out.println("\n");
			ExtentTestManager.logMessage(Status.INFO,"<b>Callback Request :</b> "+callback);
			AllureManager.saveLog("<b>Callback Request :</b> "+callback);
			Log.info("Callback request "+callback);
			//System.out.println("***********************Callback Response********************************");
			//System.out.println("\n");
			StringBuilder res = sendRequest(callBackURL,callbackJsonString,"");
			//System.out.println(res.toString());
			//System.out.println("\n");
			ExtentTestManager.logMessage(Status.INFO,"<b>Callback Response :</b> "+res.toString());
			AllureManager.saveLog("<b>Callback Response :</b> "+res.toString());
			Log.info("Callback response "+res.toString());
			resetVariableToDefault();
			return res.toString();
		}else {
//				System.out.println("***** ERROR *******\n");
//				System.out.println(errorMessage+"\n");
//				System.out.println("***** ERROR *******\n");
			ExtentTestManager.logMessage(Status.WARNING,"<b>Callback Error :</b> "+errorMessage);
			AllureManager.saveLog("<b>Callback Error :</b> "+errorMessage);
			Log.info("Callback Error "+errorMessage);
			Assert.fail("Callback Error: "+errorMessage);
			return errorMessage;
		}
	}


	private  static ThreadLocal<String> eventTID = new ThreadLocal<>();

	private static void setEventTID(String tid){
		eventTID.set(tid);
	}

	public static String getEventTID(){
		return eventTID.get();
	}

	//To sent the Http Request
	public StringBuilder sendRequest(String callback, String jsonString, String accessKey) {

		StringBuilder response = new StringBuilder();
		try {
			URL obj = new URL(callback);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			byte[] postData = jsonString.getBytes(StandardCharsets.UTF_8);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Charset", "utf-8");
			con.setRequestProperty("Accept", "application/json");
			if(!accessKey.equals("")) {
				con.setRequestProperty("X-NN-Access-Key", Base64.getEncoder().encodeToString(accessKey.getBytes()));
			}

			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(postData);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			//System.out.println("Http Response Code: "+responseCode);
			Log.info("Http Response Code of http request : "+responseCode);
			if (responseCode != 200) {
				Assert.fail("Callback API request failed status code: "+responseCode);
			}
			//System.out.println("\n");
			BufferedReader iny = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String output;

			while ((output = iny.readLine()) != null) {
				response.append(output);
			}
			iny.close();
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return response;
	}

	// To Generate checksum
	public String generateCheckSum(String tid,String eventType,String status,String amount,String currency,String accessKey) {
		String tokenString = tid+eventType+status;
		if(!amount.equals("")) {tokenString += amount;}
		if(!currency.equals("")) {tokenString += currency;}
		if(!accessKey.equals("")) {tokenString += new StringBuilder(accessKey.trim()).reverse().toString();}
		String createdHash = "";
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(tokenString.getBytes(StandardCharsets.UTF_8));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			createdHash =  hexString.toString();
		} catch(RuntimeException ex) {
			return "RuntimeException while generating checksum " + ex;
		}catch(NoSuchAlgorithmException ex) {
			return "RuntimeException while generating checksum " + ex;
		}
		return createdHash;
	}

	// To get the transaction details from Novalnet server
	public StringBuilder getTransactionDetails(String TID,String accessKey) {
		if(TID.length() == 17) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Map<String, Object> dataParameters = new HashMap<String, Object>();
			Map<String, Object> transactionParameters = new HashMap<String, Object>();
			Map<String, Object> customParameters = new HashMap<String, Object>();
			transactionParameters.put("tid", TID);
			customParameters.put("lang", "EN");
			dataParameters.put("transaction", transactionParameters);
			dataParameters.put("custom", customParameters);
			String jsonString = gson.toJson(dataParameters);
			String endpoint = "https://payport.novalnet.de/v2/transaction/details";
			StringBuilder response = sendRequest(endpoint, jsonString, accessKey);
			Log.info(" Transaction Details fetched - successfully");
			//ExtentTestManager.logMessage(Status.INFO, "<br>Original TID details : </br>"+response);
//				System.out.println("***********************Original TID Transaction Details********************************");
//				System.out.println(printPrettyJson(response));
//				System.out.println("\n");
			return response;
		}
		return null;
	}

	//To format the json
	public String printPrettyJson(StringBuilder builder) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(builder);
	}

	//Get Random TID
	public String getRandomTID() {
		Random rand = new Random();
		int length = 15;
		StringBuilder random = new StringBuilder();
		for(int i=0;i<length;i++){
			random.append(rand.nextInt(10));
		}
		String start = "14";
		start += random;
		return start;
	}
	public void setCancelType(String cancelType) {
		this.cancelType = cancelType;
	}
	public String getCancelType() {
		return cancelType;
	}

	//Events
	public static final String CREDIT = "CREDIT";
	public static final String PAYMENT = "PAYMENT";
	public static final String TRANSACTION_REFUND = "TRANSACTION_REFUND";
	public static final String CHARGEBACK = "CHARGEBACK";
	public static final String TRANSACTION_CAPTURE = "TRANSACTION_CAPTURE";
	public static final String TRANSACTION_CANCEL = "TRANSACTION_CANCEL";
	public static final String TRANSACTION_UPDATE = "TRANSACTION_UPDATE";
	public static final String INSTALMENT = "INSTALMENT";
	public static final String INSTALMENT_CANCEL = "INSTALMENT_CANCEL";
	public static final String SUBSCRIPTION_UPDATE = "SUBSCRIPTION_UPDATE";
	public static final String RENEWAL = "RENEWAL";
	public static final String SUBSCRIPTION_SUSPEND = "SUBSCRIPTION_SUSPEND";
	public static final String SUBSCRIPTION_REACTIVATE = "SUBSCRIPTION_REACTIVATE";
	public static final String SUBSCRIPTION_CANCEL = "SUBSCRIPTION_CANCEL";
	public static final String PAYMENT_REMINDER_1 = "PAYMENT_REMINDER_1";
	public static final String PAYMENT_REMINDER_2 = "PAYMENT_REMINDER_2";
	public static final String SUBMISSION_TO_COLLECTION_AGENCY = "SUBMISSION_TO_COLLECTION_AGENCY";
	public static final String INSTALMENT_CANCEL_ALL_CYCLES = "ALL_CYCLES";
	public static final String INSTALMENT_CANCEL_REMAINING_CYCLES = "REMAINING_CYCLES";

	//Payment
	public static final String CREDITCARD = "CREDITCARD";
	public static final String DIRECT_DEBIT_SEPA              = "DIRECT_DEBIT_SEPA";
	public static final String GUARANTEED_DIRECT_DEBIT_SEPA   = "GUARANTEED_DIRECT_DEBIT_SEPA";
	public static final String INSTALMENT_DIRECT_DEBIT_SEPA   = "INSTALMENT_DIRECT_DEBIT_SEPA";
	public static final String INVOICE                        = "INVOICE";
	public static final String GUARANTEED_INVOICE             = "GUARANTEED_INVOICE";
	public static final String INSTALMENT_INVOICE             = "INSTALMENT_INVOICE";
	public static final String PREPAYMENT                     = "PREPAYMENT";
	public static final String CASHPAYMENT                    = "CASHPAYMENT";
	public static final String ONLINE_BANK_TRANSFER           = "ONLINE_BANK_TRANSFER";
	public static final String GIROPAY                        = "GIROPAY";
	public static final String ONLINE_TRANSFER                = "ONLINE_TRANSFER";

	public static final String IDEAL                          = "IDEAL";

	public static final String EPS                            = "EPS";
	public static final String PRZELEWY24                     = "PRZELEWY24";
	public static final String BANCONTACT                     = "BANCONTACT";
	public static final String POSTFINANCE                    = "POSTFINANCE";
	public static final String POSTFINANCE_CARD               = "POSTFINANCE_CARD";
	public static final String TRUSTLY                        = "TRUSTLY";

	public static final String PAYPAL                         = "PAYPAL";
	public static final String MULTIBANCO                     = "MULTIBANCO";
	public static final String APPLEPAY                       = "APPLEPAY";
	public static final String GOOGLEPAY                      = "GOOGLEPAY";
	public static final String ALIPAY                         = "ALIPAY";
	public static final String WECHATPAY                      = "WECHATPAY";
	public static final String DIRECT_DEBIT_ACH               = "DIRECT_DEBIT_ACH";
	public static final String BLIK                           ="BLIK";

	public static final String PAYCONIQ                       = "PAYCONIQ";

	public static final String MBWAY                          ="MBWAY";

	//Refund
	public static final String CREDITCARD_BOOKBACK = "CREDITCARD_BOOKBACK";
	public static final String GUARANTEED_SEPA_BOOKBACK = "GUARANTEED_SEPA_BOOKBACK";
	public static final String GUARANTEED_INVOICE_BOOKBACK = "GUARANTEED_INVOICE_BOOKBACK";
	public static final String REFUND_BY_BANK_TRANSFER_EU = "REFUND_BY_BANK_TRANSFER_EU";
	public static final String PRZELEWY24_REFUND = "PRZELEWY24_REFUND";
	public static final String INSTALMENT_INVOICE_BOOKBACK = "INSTALMENT_INVOICE_BOOKBACK";
	public static final String INSTALMENT_SEPA_BOOKBACK = "INSTALMENT_SEPA_BOOKBACK";
	public static final String POSTFINANCE_REFUND = "POSTFINANCE_REFUND";
	public static final String CASHPAYMENT_REFUND = "CASHPAYMENT_REFUND";
	public static final String PAYPAL_BOOKBACK = "PAYPAL_BOOKBACK";
	public static final String TRUSTLY_REFUND = "TRUSTLY_REFUND";
	public static final String ALIPAY_REFUND = "ALIPAY_REFUND";
	public static final String WECHATPAY_REFUND = "WECHATPAY_REFUND";
	public static final String APPLEPAY_BOOKBACK = "APPLEPAY_BOOKBACK";
	public static final String GOOGLEPAY_BOOKBACK= "GOOGLEPAY_BOOKBACK";
	public static final String DIRECT_DEBIT_ACH_REFUND= "DIRECT_DEBIT_ACH_REFUND";

	public static final String BLIK_REFUND = "BLIK_REFUND";

	public static final String MBWAY_REFUND = "MBWAY_REFUND";

	//Credit
	public static final String INVOICE_CREDIT = "INVOICE_CREDIT";
	public static final String CASHPAYMENT_CREDIT = "CASHPAYMENT_CREDIT";
	public static final String BANK_TRANSFER_BY_END_CUSTOMER = "BANK_TRANSFER_BY_END_CUSTOMER";
	public static final String MULTIBANCO_CREDIT = "MULTIBANCO_CREDIT";
	public static final String ONLINE_TRANSFER_CREDIT = "ONLINE_TRANSFER_CREDIT";
	public static final String CREDIT_ENTRY_SEPA = "CREDIT_ENTRY_SEPA";
	public static final String DEBT_COLLECTION_SEPA = "DEBT_COLLECTION_SEPA";
	public static final String CREDIT_ENTRY_CREDITCARD = "CREDIT_ENTRY_CREDITCARD";
	public static final String DEBT_COLLECTION_CREDITCARD = "DEBT_COLLECTION_CREDITCARD";
	public static final String CREDITCARD_REPRESENTMENT = "CREDITCARD_REPRESENTMENT";
	public static final String CREDIT_ENTRY_DE = "CREDIT_ENTRY_DE";
	public static final String DEBT_COLLECTION_DE = "DEBT_COLLECTION_DE";
	public static final String APPLEPAY_REPRESENTMENT = "APPLEPAY_REPRESENTMENT";
	public static final String GOOGLEPAY_REPRESENTMENT = "GOOGLEPAY_REPRESENTMENT";

	public static final String REFUND_REVERSAL = "REFUND_REVERSAL";

	//Chargeback
	public static final String CREDITCARD_CHARGEBACK = "CREDITCARD_CHARGEBACK";
	public static final String RETURN_DEBIT_SEPA = "RETURN_DEBIT_SEPA";
	public static final String PAYPAL_CHARGEBACK = "PAYPAL_CHARGEBACK";
	public static final String REVERSAL = "REVERSAL";
	public static final String APPLEPAY_CHARGEBACK = "APPLEPAY_CHARGEBACK";
	public static final String GOOGLEPAY_CHARGEBACK = "GOOGLEPAY_CHARGEBACK";

	//Callback comments Startswith
	public static final String INITIAL_LEVEL_COMMENT_ = "Test order";
	public static final String CAPTURE_COMMENT_ = "The transaction has been confirmed";
	public static final String CANCEL_COMMENT_ = "The transaction has been cancelled";
	public static final String CANCEL_COMMENT_2 = "The transaction has been canceled";
	public static final String REFUND_COMMENT_ = "Refund has been initiated";
	public static final String CHARGEBACK_COMMENT_ = "Chargeback executed successfully";
	public static final String CREDIT_COMMENT_ = "Credit has been successfully received";
	public static final String UPDATE_COMMENT_ = "Transaction updated successfully";
	public static final String UPDATE_COMMENT_1 = "The transaction has been updated";
	public  static final String INSTALMENT_RENEWAL_COMMENT = "A new instalment has been received for the Transaction";
	public  static final String INSTALMENT_CANCEL_COMMENT = "Instalment has been cancelled for the TID";
	public  static final String INSTALMENT_CANCEL_REMAINING_COMMENT = "Instalment has been stopped for the TID";
	public static final String REMINDER_ONE_COMMENT_ = "Payment Reminder 1 has been sent to the customer";
	public static final String REMINDER_TWO_COMMENT_ = "Payment Reminder 2 has been sent to the customer";
	public static final String COLLECTION_COMMENT_ = "The transaction has been submitted to the collection agency";
	public static final String CHANGE_PAYMENT_COMMENT_ = "Successfully changed the payment method for next subscription";
	public static final String SUBSCRIPTION_SUSPEND_COMMENT_ = "This subscription transaction has been suspended on";
	public static final String SUBSCRIPTION_REACTIVATE_COMMENT_ = "Subscription has been reactivated for the TID:";
	public static final String SUBSCRIPTION_UPDATE_COMMENT_ = "Subscription updated successfully. You will be charged ";
	public static final String SUBSCRIPTION_CANCEL_COMMENT_ = "Subscription has been cancelled";
	public static final String ZERO_AMOUNT_BOOKING = "This order processed as a zero amount booking";
	public static final String ZERO_AMOUNT_BOOKING_CONFIRMATION = "Your order has been booked with the amount of";


	//Generic Server error messages
	public static final String PAYPAL_END_USER_CANCEL_ERROR = "transaction not yet finished by the user ";

	public static final String REDIRECT_END_USER_CANCEL_ERROR = "Customer has abandoned the transaction";



	//public static final String EPS_END_USER_CANCEL_ERROR = "Transaction rejected";

	public static final String EPS_END_USER_CANCEL_ERROR = "user aborted";;

	public static final String CC_END_USER_CANCEL_ERROR = "3D secure authentication failed or was cancelled";
	public static final String CC_EXPIRED_CARD_ERROR = "Credit card payment not possible: card expired";
	public static final String CC_RESTRICTED_CARD_ERROR = "Restricted card";
	public static final String CC_INSUFFICIENT_CARD_ERROR = "Insufficient funds or credit limit exceeded";

	public static final String CC_INVALID_CREDITCARD_ERROR = "Your credit card details are invalid";
	public static final String CC_INVALID_CREDICARD_ERROR1 ="Invalid credit card number";

	public static final String CC_NUMBER_FIELD = "CC NUMBER";
	public static final String CC_EXP_DATE_FIELD = "EXP DATE";
	public static final String CC_CVV_FIELD = "CVV";
	public static final String CC_NAME_FIELD = "CC_NAME_FIELD";
	public static final String TID_STATUS_CONFIRMED = "CONFIRMED";
	public static final String TID_STATUS_ON_HOLD = "ON_HOLD";
	public static final String TID_STATUS_PENDING = "PENDING";
	public static final String TID_STATUS_FAILURE = "FAILURE";
	public static final String TID_STATUS_DEACTIVATED = "DEACTIVATED";
}

