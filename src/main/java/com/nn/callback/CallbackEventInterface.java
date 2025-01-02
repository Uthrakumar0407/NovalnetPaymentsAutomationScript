package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.CREDIT;
import static com.nn.callback.CallbackProperties.INVOICE_CREDIT;

public interface CallbackEventInterface {


	@Step("Perform TRANSACTION_CAPTURE event")
	default String transactionCapture(String tid) {
		return null;
	}

	@Step("Perform TRANSACTION_CAPTURE event")
	default String transactionCapture(String tid,String[] cycleDates,String executedCycles,String pendingCycles, String nextCycleDate) {
		return null;
	}

	@Step("Perform TRANSACTION_CANCEL event")
	default String transactionCancel(String tid) {
		return null;
	}

	@Step("Perform TRANSACTION_CANCEL event")
	default String transactionCancel(String tid, String orderNumber) {
		return null;
	}

	@Step("Perform TRANSACTION_REFUND event")
	default String transactionRefund(String tid, String amount) {
		return null;
	}

	@Step("Perform TRANSACTION_REFUND event")
	default String transactionRefund(String eventType, String tid, String amount) {
		return null;
	}

	@Step("Perform TRANSACTION_REFUND event")
	default String transactionRefund(String tid) {
		return null;
	}
	@Step("Perform TRANSACTION_UPDATE event")
	default String transactionUpdateStatus(String tid, String status) {
		return null;
	}

	@Step("Perform TRANSACTION_UPDATE event")
	default String transactionUpdateStatus(String tid,String status,String[] cycleDates,String executedCycles,String pendingCycles, String nextCycleDate) {
		return null;
	}
	@Step("Perform TRANSACTION_UPDATE event")
	default String transactionUpdateStatus(String tid, String status, String[] cyclesDates) {
		return null;
	}
	@Step("Perform TRANSACTION_UPDATE event")
	default String transactionUpdateAmount(String tid, String amount) {
		return null;
	}
	@Step("Perform TRANSACTION_UPDATE event")
	default String transactionUpdateDueDate(String tid, String dueDate) {
		return null;
	}
	@Step("Perform CHARGEBACK event")
	default String chargeback(String tid) {
		return null;
	}

	@Step("Perform CHARGEBACK event")
	default String chargeback(String tid, String amount) {
		return null;
	}

	@Step("Perform CREDIT event")
	default String credit(String tid) {
		return null;
	}
	@Step("Perform CREDIT event")
	default String credit(String tid, String amount) {
		return null;
	}
	@Step("Perform CREDIT event {2}")
	default String credit(String tid, String amount, String paymentType) {
		var callback = new CallbackProperties();
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(paymentType);
		return callback.sendCallbackRequest();
	}
	@Step("Perform CREDIT_ENTRY_CREDITCARD event")
	default String creditEntryCreditCard(String tid, String amount) {
		return null;
	}
	@Step("Perform DEBT_COLLECTION_CREDITCARD event")
	default String debtCollectionCreditCard(String tid, String amount) {
		return null;
	}
	@Step("Perform CREDITCARD_REPRESENTMENT event")
	default String creditCardRepresentment(String tid, String amount) {
		return null;
	}
	@Step("Perform BANK_TRANSFER_BY_END_CUSTOMER event")
	default String bankTransferByEndCustomer(String tid, String amount) {
		return null;
	}
	@Step("Perform CREDIT_ENTRY_SEPA event")
	default String creditEntrySepa(String tid, String amount) {
		return null;
	}
	@Step("Perform DEBT_COLLECTION_SEPA event")
	default String debtCollectionSepa(String tid, String amount) {
		return null;
	}
	@Step("Perform APPLEPAY_REPRESENTMENT event")
	default String applePayRepresentment(String tid, String amount) {
		return null;
	}
	@Step("Perform CREDIT_ENTRY_DE event")
	default String creditEntryDE(String tid, String amount) {
		return null;
	}
	@Step("Perform GOOGLEPAY_REPRESENTMENT event")
	default String googlePayRepresentment(String tid, String amount) {
		return null;
	}
	@Step("Perform ONLINE_TRANSFER_CREDIT event")
	default String onlineTransferCredit(String tid, String amount) {
		return null;
	}
	@Step("Perform ONLINE_TRANSFER_CREDIT event")
	default String onlineTransferCredit(String tid, String orderNumber, String amount) {
		return null;
	}
	@Step("Perform DEBT_COLLECTION_DE event")
	default String debtCollectionDE(String tid, String amount) {
		return null;
	}

	@Step("Perform INVOICE_CREDIT event")
	default String invoiceCredit(String tid, String amount) {
		return null;
	}
	@Step("Perform INVOICE_CREDIT event")
	default String invoiceCreditEvent(String tid, String amount) {
		var callback = new CallbackProperties();
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(INVOICE_CREDIT);
		return callback.sendCallbackRequest();
	}
	
	default String cashpaymentCredit(String tid, String amount) {
		return null;
	}
	
	default String multibancoCredit(String tid, String amount) {
		return null;
	}

	default String instalment(String tid, String executedCycles, String pendingCycles, String nextCycleDate) {
		return null;
	}

	default String instalment(String tid, String executedCycles, String pendingCycles) {
		return null;
	}

	default String instalmentCancel(String tid){return null;}
	default String instalmentCancelAllCycle(String tid){return null;}
	default String instalmentCancelAllRemainingCycle(String tid){return null;}
	@Step("Perform Payment Event for communication break")
	default String communicationBreakSuccess(String tid, String amount) {
		return null;
	}

	@Step("Perform Payment Event for communication break")
	default String communicationBreakSuccess(String tid, String transactionStatus, int amount) {
		return null;
	}

	@Step("Perform Payment Event for communication break")
	default String communicationBreakSuccess(String tid, String orderNumber, String amount) {
		return null;
	}

	@Step("Perform Payment Event for communication break")
	default String communicationBreakFailure(String tid, String amount) {
		return null;
	}

	@Step("Perform Payment Event for communication break")
	default String communicationBreakFailure(String tid, String orderNumber, String amount) {
		return null;
	}

	@Step("Perform Subscription Renewal Event")
	default String subscriptionRenewal(String tid) {
		return null;
	}

	@Step("Perform Subscription Renewal Event")
	default String subscriptionRenewal(String tid, String paymentName) {
		var callback = new CallbackProperties();
		callback.setEvent(CallbackProperties.RENEWAL);
		callback.setTID(tid);
		callback.setPaymentType(paymentName);
		return callback.sendCallbackRequest();
	}
	
	default String subscriptionUpdate(String tid, String amount, String cycleDate) {
		return null;
	}
	
	default String subscriptionAmountUpdate(String tid, String amount) {
		return null;
	}

	default String subscriptionAmountUpdate(String tid, String amount, String paymentName) {
		var callback = new CallbackProperties();
		callback.setEvent(CallbackProperties.SUBSCRIPTION_UPDATE);
		callback.setTID(tid);
		callback.setPaymentType(paymentName);
		callback.setSubscriptionAmount(amount);
		return callback.sendCallbackRequest();
	}
	
	default String subscriptionCycleDateUpdate(String tid, String cycleDate) {
		return null;
	}

	default String subscriptionCycleDateAmountUpdate(String tid, String cycleDate, String amount, String paymentName) {
		var callback = new CallbackProperties();
		callback.setEvent(CallbackProperties.SUBSCRIPTION_UPDATE);
		callback.setTID(tid);
		callback.setPaymentType(paymentName);
		callback.setSubscriptionAmount(amount);
		callback.setNextSubscriptionCycleDate(cycleDate);
		return callback.sendCallbackRequest();
	}
	
	default String subscriptionChangePayment(String tid, String paymentName) {
		return null;
	}

	default String subscriptionChangePayment(String tid, String paymentNameActual, String paymentNameChange) {
		var callback = new CallbackProperties();
		callback.setSubscriptionChangePaymentType(paymentNameChange);
		callback.setTID(tid);
		callback.setEvent(CallbackProperties.SUBSCRIPTION_UPDATE);
		callback.setPaymentType(paymentNameActual);
		return callback.sendCallbackRequest();
	}
	
	default String subscriptionSuspend(String tid) {
		return null;
	}

	default String subscriptionSuspend(String tid, String paymentName) {
		var callback = new CallbackProperties();
		callback.setEvent(CallbackProperties.SUBSCRIPTION_SUSPEND);
		callback.setTID(tid);
		callback.setPaymentType(paymentName);
		return callback.sendCallbackRequest();
	}
	
	default String subscriptionReactivate(String tid) {
		return null;
	}

	default String subscriptionReactivate(String tid, String paymentName) {
		var callback = new CallbackProperties();
		callback.setEvent(CallbackProperties.SUBSCRIPTION_REACTIVATE);
		callback.setTID(tid);
		callback.setPaymentType(paymentName);
		return callback.sendCallbackRequest();
	}
	
	default String subscriptionCancel(String tid) {
		return null;
	}

	default String subscriptionCancel(String tid, String paymentName) {
		var callback = new CallbackProperties();
		callback.setEvent(CallbackProperties.SUBSCRIPTION_CANCEL);
		callback.setTID(tid);
		callback.setPaymentType(paymentName);
		return callback.sendCallbackRequest();
	}

	default String paymentReminder(String tid, String event) {
		return null;
	}

	@Step("Perform PAYMENT_REMINDER_1 event")
	default String paymentReminderOne(String tid) {
		return null;
	}
	@Step("Perform PAYMENT_REMINDER_2 event")
	default String paymentReminderTwo(String tid) {
		return null;
	}
	@Step("Perform SUBMISSION_TO_COLLECTION_AGENCY event")
	default String submissionToCollection(String tid) {
		return null;
	}
	
}
