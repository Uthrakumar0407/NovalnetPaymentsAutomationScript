package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class GiroPayCallbackEvents implements CallbackEventInterface{

	CallbackProperties callback;

	public GiroPayCallbackEvents() {
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
		callback.setPaymentType(GIROPAY);
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

	@Step("Perform Payment callback event for success")
	@Override
	public String communicationBreakSuccess(String tid, String orderNumber, String amount) {
		callback.setCommunicationBreakStatus("SUCCESS");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setOrderNumber(orderNumber);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(GIROPAY);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for success")
	@Override
	public String communicationBreakSuccess(String tid,String amount) {
		callback.setCommunicationBreakStatus("SUCCESS");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(GIROPAY);
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
		callback.setPaymentType(GIROPAY);
		return callback.sendCallbackRequest();
	}

	@Step("Perform Payment callback event for failure")
	@Override
	public String communicationBreakFailure(String tid, String amount) {
		callback.setCommunicationBreakStatus("FAILURE");
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(PAYMENT);
		callback.setPaymentType(GIROPAY);
		return callback.sendCallbackRequest();
	}
}
