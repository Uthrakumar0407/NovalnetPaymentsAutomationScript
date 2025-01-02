package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class CreditCardCallbackEvents implements CallbackEventInterface{


	CallbackProperties callback;

	public CreditCardCallbackEvents() {
		callback = new CallbackProperties();
	}

	@Step("Perform TRANSACTION_CAPTURE event")
	@Override
	public String transactionCapture(String tid) {
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_CAPTURE);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_CANCEL event")
	@Override
	public String transactionCancel(String tid) {
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_CANCEL);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}


	@Step("Perform TRANSACTION_CANCEL event")
	@Override
	public String transactionCancel(String tid,String orderNumber) {
		callback.setTID(tid);
		callback.setTID(orderNumber);
		callback.setEvent(TRANSACTION_CANCEL);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_REFUND event")
	@Override
	public String transactionRefund(String tid) {
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_REFUND);
		callback.setPaymentType(CREDITCARD_BOOKBACK);
		return callback.sendCallbackRequest();
	}


	@Step("Perform TRANSACTION_REFUND event")
	@Override
	public String transactionRefund(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_REFUND);
		callback.setPaymentType(CREDITCARD_BOOKBACK);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_UPDATE event")
	@Override
	public String transactionUpdateStatus(String tid, String status) {
		callback.setTID(tid);
		callback.setUpdateType("STATUS");
		callback.setCallbackValueOrAmount(status);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}


	@Step("Perform CREDITCARD_CHARGEBACK event")
	@Override
	public String chargeback(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CHARGEBACK);
		callback.setPaymentType(CREDITCARD_CHARGEBACK);
		return callback.sendCallbackRequest();
	}

	@Step("Perform CREDIT_ENTRY_CREDITCARD event")
	@Override
	public String creditEntryCreditCard(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(CREDIT_ENTRY_CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform DEBT_COLLECTION_CREDITCARD event")
	@Override
	public String debtCollectionCreditCard(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(DEBT_COLLECTION_CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform CREDITCARD_REPRESENTMENT event")
	@Override
	public String creditCardRepresentment(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(CREDITCARD_REPRESENTMENT);
		return callback.sendCallbackRequest();
	}

	@Step("Perform BANK_TRANSFER_BY_END_CUSTOMER event")
	@Override
	public String bankTransferByEndCustomer(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(BANK_TRANSFER_BY_END_CUSTOMER);
		return callback.sendCallbackRequest();
	}

	@Step("Perform {2} event")
	@Override
	public String credit(String tid, String amount, String paymentType){
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(paymentType);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for success")
	@Override
	public String communicationBreakSuccess(String tid, String orderNumber, String amount) {
		callback.setCommunicationBreakStatus("SUCCESS");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setOrderNumber(orderNumber);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for success")
	@Override
	public String communicationBreakSuccess(String tid,String amount) {
		callback.setCommunicationBreakStatus("SUCCESS");
		callback.setTID(tid);
		callback.setCallbackValueOrAmount(amount);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for failure")
	@Override
	public String communicationBreakFailure(String tid, String orderNumber, String amount) {
		callback.setCommunicationBreakStatus("FAILURE");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setOrderNumber(orderNumber);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}
	@Step("Perform Payment Event for communication break")
	public String communicationBreakFailure(String tid, String amount) {
		callback.setCommunicationBreakStatus("FAILURE");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}


	@Step("Perform RENEWAL event")
	@Override
	public String subscriptionRenewal(String tid) {
		callback.setTID(tid);
		callback.setEvent(RENEWAL);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_UPDATE event")
	@Override
	public String subscriptionUpdate(String tid,String amount, String cycleDate) {
		callback.setSubscriptionAmount(amount);
		callback.setNextSubscriptionCycleDate(cycleDate);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_UPDATE event")
	@Override
	public String subscriptionAmountUpdate(String tid,String amount) {
		callback.setSubscriptionAmount(amount);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_UPDATE event")
	@Override
	public String subscriptionCycleDateUpdate(String tid,String cycleDate) {
		callback.setNextSubscriptionCycleDate(cycleDate);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_UPDATE event")
	@Override
	public String subscriptionChangePayment(String tid,String paymentType) {
		callback.setSubscriptionChangePaymentType(paymentType);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_SUSPEND event")
	@Override
	public String subscriptionSuspend(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_SUSPEND);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_REACTIVATE event")
	@Override
	public String subscriptionReactivate(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_REACTIVATE);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_CANCEL event")
	@Override
	public String subscriptionCancel(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_CANCEL);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform {1} event")
	@Override
	public String paymentReminder(String tid, String event) {
		callback.setTID(tid);
		callback.setEvent(event);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform PAYMENT_REMINDER_1 event")
	@Override
	public String paymentReminderOne(String tid) {
		callback.setTID(tid);
		callback.setEvent(PAYMENT_REMINDER_1);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}


	@Step("Perform PAYMENT_REMINDER_2 event")
	@Override
	public String paymentReminderTwo(String tid) {
		callback.setTID(tid);
		callback.setEvent(PAYMENT_REMINDER_2);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBMISSION_TO_COLLECTION_AGENCY event")
	@Override
	public String submissionToCollection(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBMISSION_TO_COLLECTION_AGENCY);
		callback.setPaymentType(CREDITCARD);
		return callback.sendCallbackRequest();
	}

}
