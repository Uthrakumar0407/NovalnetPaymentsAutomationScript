package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class CashPaymentCallbackEvents implements CallbackEventInterface{

	CallbackProperties callback;

	public CashPaymentCallbackEvents() {
		callback = new CallbackProperties();
	}

	@Step("Perform CASHPAYMENT_REFUND event")
	@Override
	public String transactionRefund(String tid) {
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_REFUND);
		callback.setPaymentType(CASHPAYMENT_REFUND);
		return callback.sendCallbackRequest();
	}
	@Step("Perform CASHPAYMENT_REFUND event")
	@Override
	public String transactionRefund(String tid,String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(TRANSACTION_REFUND);
		callback.setPaymentType(CASHPAYMENT_REFUND);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_UPDATE event with status {1}")
	@Override
	public String transactionUpdateStatus(String tid, String status) {
		callback.setTID(tid);
		callback.setUpdateType("STATUS");
		callback.setCallbackValueOrAmount(status);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(CASHPAYMENT);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_UPDATE event with amount {1}")
	@Override
	public String transactionUpdateAmount(String tid, String amount) {
		callback.setTID(tid);
		callback.setUpdateType("AMOUNT");
		callback.setCallbackValueOrAmount(amount);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(CASHPAYMENT);
		return callback.sendCallbackRequest();
	}

	@Step("Perform CASHPAYMENT_CREDIT event")
	@Override
	public String cashpaymentCredit(String tid, String amount) {
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(CASHPAYMENT_CREDIT);
		return callback.sendCallbackRequest();
	}

	@Step("Perform TRANSACTION_UPDATE event with dueDate {1}")
	@Override
	public String transactionUpdateDueDate(String tid, String dueDate) {
		callback.setTID(tid);
		callback.setUpdateType("DUE_DATE");
		callback.setCallbackValueOrAmount(dueDate);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(CASHPAYMENT);
		return callback.sendCallbackRequest();
	}
	
	
}
