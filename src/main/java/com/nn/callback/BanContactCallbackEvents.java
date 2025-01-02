package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class BanContactCallbackEvents implements CallbackEventInterface{

	CallbackProperties callback;

	public BanContactCallbackEvents() {
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


	@Step("Perform TRANSACTION_UPDATE event")
	@Override
	public String transactionUpdateStatus(String tid, String status) {
		callback.setTID(tid);
		callback.setUpdateType("STATUS");
		callback.setCallbackValueOrAmount(status);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(BANCONTACT);
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
		callback.setPaymentType(BANCONTACT);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for success")
	@Override
	public String communicationBreakSuccess(String tid, String amount) {
		callback.setCommunicationBreakStatus("SUCCESS");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(BANCONTACT);
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
		callback.setPaymentType(BANCONTACT);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for failure")
	@Override
	public String communicationBreakFailure(String tid, String amount) {
		callback.setCommunicationBreakStatus("FAILURE");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(BANCONTACT);
		return callback.sendCallbackRequest();
	}


}
