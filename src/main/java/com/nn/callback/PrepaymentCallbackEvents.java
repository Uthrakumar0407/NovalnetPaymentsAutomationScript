package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class PrepaymentCallbackEvents implements CallbackEventInterface{

	CallbackProperties callback;
	
	public PrepaymentCallbackEvents() {
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
	@Step("Perform BANK_TRANSFER_BY_END_CUSTOMER event")
	@Override
	public String bankTransferByEndCustomer(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(BANK_TRANSFER_BY_END_CUSTOMER);
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
	@Step("Perform INVOICE_CREDIT event")
	@Override
	public String invoiceCredit(String tid, String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(INVOICE_CREDIT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform TRANSACTION_UPDATE event with status {1}")
	@Override
	public String transactionUpdateStatus(String tid, String status) {
		callback.setTID(tid);
		callback.setUpdateType("STATUS");
		callback.setCallbackValueOrAmount(status);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform TRANSACTION_UPDATE event with amount {1}")
	@Override
	public String transactionUpdateAmount(String tid, String amount) {
		callback.setTID(tid);
		callback.setUpdateType("AMOUNT");
		callback.setCallbackValueOrAmount(amount);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform TRANSACTION_UPDATE event with due date {1}")
	@Override
	public String transactionUpdateDueDate(String tid, String dueDate) {
		callback.setTID(tid);
		callback.setUpdateType("DUE_DATE");
		callback.setCallbackValueOrAmount(dueDate);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform RENEWAL event")
	@Override
	public String subscriptionRenewal(String tid) {
		callback.setTID(tid);
		callback.setEvent(RENEWAL);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform SUBSCRIPTION_UPDATE event with amount {1} and cycle date {2}")
	@Override
	public String subscriptionUpdate(String tid,String amount, String cycleDate) {
		callback.setSubscriptionAmount(amount);
		callback.setNextSubscriptionCycleDate(cycleDate);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform SUBSCRIPTION_UPDATE event with amount {1}")
	@Override
	public String subscriptionAmountUpdate(String tid,String amount) {
		callback.setSubscriptionAmount(amount);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform SUBSCRIPTION_UPDATE event with cycle date {1}")
	@Override
	public String subscriptionCycleDateUpdate(String tid,String cycleDate) {
		callback.setNextSubscriptionCycleDate(cycleDate);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform SUBSCRIPTION_UPDATE event with payment {1}")
	@Override
	public String subscriptionChangePayment(String tid,String paymentType) {
		callback.setSubscriptionChangePaymentType(paymentType);
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_UPDATE);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform SUBSCRIPTION_SUSPEND event")
	@Override
	public String subscriptionSuspend(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_SUSPEND);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform SUBSCRIPTION_REACTIVATE event")
	@Override
	public String subscriptionReactivate(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_REACTIVATE);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}
	@Step("Perform SUBSCRIPTION_CANCEL event")
	@Override
	public String subscriptionCancel(String tid) {
		callback.setTID(tid);
		callback.setEvent(SUBSCRIPTION_CANCEL);
		callback.setPaymentType(PREPAYMENT);
		return callback.sendCallbackRequest();
	}


	
	
}
