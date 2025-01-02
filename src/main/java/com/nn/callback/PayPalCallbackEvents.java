package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class PayPalCallbackEvents implements CallbackEventInterface{


	CallbackProperties callback;

	public PayPalCallbackEvents() {
		callback = new CallbackProperties();
	}

	@Step("Perform TRANSACTION_CAPTURE event")
	@Override
	public String transactionCapture(String tid) {
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_CAPTURE);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_UPDATE event")
	@Override
	public String transactionUpdateStatus(String tid, String status) {
		callback.setTID(tid);
		callback.setUpdateType("STATUS");
		callback.setCallbackValueOrAmount(status);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_CANCEL event")
	@Override
	public String transactionCancel(String tid) {
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_CANCEL);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_CANCEL event")
	@Override
	public String transactionCancel(String tid,String orderNumber) {
		callback.setTID(tid);
		callback.setTID(orderNumber);
		callback.setEvent(TRANSACTION_CANCEL);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_REFUND event")
	@Override
	public String transactionRefund(String tid) {
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_REFUND);
		callback.setPaymentType(PAYPAL_BOOKBACK);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_REFUND event")
	@Override
	public String transactionRefund(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_REFUND);
		callback.setPaymentType(PAYPAL_BOOKBACK);
		return callback.sendCallbackRequest();
	}

	@Step("Perform PAYPAL_CHARGEBACK event")
	@Override
	public String chargeback(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CHARGEBACK);
		callback.setPaymentType(PAYPAL_CHARGEBACK);
		return callback.sendCallbackRequest();
	}

	@Step("Perform CREDIT_ENTRY_DE event")
	@Override
	public String creditEntryDE(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(CREDIT_ENTRY_DE);
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
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for success")
	@Override
	public String communicationBreakSuccess(String tid,String amount) {
		callback.setCommunicationBreakStatus("SUCCESS");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(PAYPAL);
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
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for failure")
	@Override
	public String communicationBreakFailure(String tid, String amount) {
		callback.setCommunicationBreakStatus("FAILURE");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform RENEWAL event")
	@Override
	public String subscriptionRenewal(String tid) {
		callback.setTID(tid);
		callback.setEvent(RENEWAL);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_UPDATE event with amount {1} and cycleDate {2}")
	@Override
	public String subscriptionUpdate(String tid,String amount, String cycleDate) {
		callback.setSubscriptionAmount(amount);
		callback.setNextSubscriptionCycleDate(cycleDate);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_UPDATE event with amount {1}")
	@Override
	public String subscriptionAmountUpdate(String tid,String amount) {
		callback.setSubscriptionAmount(amount);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_UPDATE event with cycleDate {1}")
	@Override
	public String subscriptionCycleDateUpdate(String tid,String cycleDate) {
		callback.setNextSubscriptionCycleDate(cycleDate);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_UPDATE event with payment {1}")
	@Override
	public String subscriptionChangePayment(String tid,String paymentType) {
		callback.setSubscriptionChangePaymentType(paymentType);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_SUSPEND event")
	@Override
	public String subscriptionSuspend(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_SUSPEND);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_REACTIVATE event")
	@Override
	public String subscriptionReactivate(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_REACTIVATE);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBSCRIPTION_CANCEL event")
	@Override
	public String subscriptionCancel(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_CANCEL);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform {1} event")
	@Override
	public String paymentReminder(String tid, String event) {
		callback.setTID(tid);
		callback.setEvent(event);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform PAYMENT_REMINDER_1 event")
	@Override
	public String paymentReminderOne(String tid) {
		callback.setTID(tid);
		callback.setEvent(PAYMENT_REMINDER_1);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform PAYMENT_REMINDER_2 event")
	@Override
	public String paymentReminderTwo(String tid) {
		callback.setTID(tid);
		callback.setEvent(PAYMENT_REMINDER_2);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBMISSION_TO_COLLECTION_AGENCY event")
	@Override
	public String submissionToCollection(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBMISSION_TO_COLLECTION_AGENCY);
		callback.setPaymentType(PAYPAL);
		return callback.sendCallbackRequest();
	}

}
