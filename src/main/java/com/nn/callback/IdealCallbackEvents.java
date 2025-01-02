package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class IdealCallbackEvents implements CallbackEventInterface{

	CallbackProperties callback;

	public IdealCallbackEvents() {
		callback = new CallbackProperties();
	}

	@Step("Perform TRANSACTION_REFUND event")
	@Override
	public String transactionRefund(String tid) {
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_REFUND);
		callback.setPaymentType(REFUND_BY_BANK_TRANSFER_EU);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_REFUND event")
	@Override
	public String transactionRefund(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_REFUND);
		callback.setPaymentType(REFUND_BY_BANK_TRANSFER_EU);
		return callback.sendCallbackRequest();
	}

	@Step("Perform REVERSAL event")
	@Override
	public String chargeback(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CHARGEBACK);
		callback.setPaymentType(REVERSAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_UPDATE event")
	@Override
	public String transactionUpdateStatus(String tid, String status) {
		callback.setTID(tid);
		callback.setUpdateType("STATUS");
		callback.setCallbackValueOrAmount(status);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(IDEAL);
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

	@Step("Perform ONLINE_TRANSFER_CREDIT event")
	@Override
	public String onlineTransferCredit(String tid, String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(ONLINE_TRANSFER_CREDIT);
		return callback.sendCallbackRequest();
	}

	@Step("Perform ONLINE_TRANSFER_CREDIT event")
	@Override
	public String onlineTransferCredit(String tid, String orderNumber, String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setOrderNumber(orderNumber);
		callback.setEvent(CREDIT);
		callback.setPaymentType(ONLINE_TRANSFER_CREDIT);
		return callback.sendCallbackRequest();
	}

	@Step("Perform DEBT_COLLECTION_DE event")
	@Override
	public String debtCollectionDE(String tid, String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(DEBT_COLLECTION_DE);
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
		callback.setPaymentType(IDEAL);
		return callback.sendCallbackRequest();
	}


	@Step("Perform Payment callback event for success")
	@Override
	public String communicationBreakSuccess(String tid, String amount) {
		callback.setCommunicationBreakStatus("SUCCESS");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(IDEAL);
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
		callback.setPaymentType(IDEAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for failure")
	@Override
	public String communicationBreakFailure(String tid, String amount) {
		callback.setCommunicationBreakStatus("FAILURE");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(IDEAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform PAYMENT_REMINDER_1 event")
	@Override
	public String paymentReminderOne(String tid) {
		callback.setTID(tid);
		callback.setEvent(PAYMENT_REMINDER_1);
		callback.setPaymentType(IDEAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform PAYMENT_REMINDER_2 event")
	@Override
	public String paymentReminderTwo(String tid) {
		callback.setTID(tid);
		callback.setEvent(PAYMENT_REMINDER_2);
		callback.setPaymentType(IDEAL);
		return callback.sendCallbackRequest();
	}

	@Step("Perform SUBMISSION_TO_COLLECTION_AGENCY event")
	@Override
	public String submissionToCollection(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBMISSION_TO_COLLECTION_AGENCY);
		callback.setPaymentType(IDEAL);
		return callback.sendCallbackRequest();
	}

}
