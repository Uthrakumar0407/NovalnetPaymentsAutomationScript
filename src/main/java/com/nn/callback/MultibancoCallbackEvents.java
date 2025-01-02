package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class MultibancoCallbackEvents implements CallbackEventInterface{

	CallbackProperties callback;

	public MultibancoCallbackEvents() {
		callback = new CallbackProperties();
	}

	@Step("Perform TRANSACTION_UPDATE event")
	@Override
	public String transactionUpdateStatus(String tid, String status) {
		callback.setTID(tid);
		callback.setUpdateType("STATUS");
		callback.setCallbackValueOrAmount(status);
		callback.setEvent(TRANSACTION_UPDATE);
		callback.setPaymentType(MULTIBANCO);
		return callback.sendCallbackRequest();
	}
	@Step("Perform MULTIBANCO_CREDIT event")
	@Override
	public String multibancoCredit(String tid, String amount){
		callback.setCallbackValueOrAmount(amount);
		callback.setTID(tid);
		callback.setEvent(CREDIT);
		callback.setPaymentType(MULTIBANCO_CREDIT);
		return callback.sendCallbackRequest();
	}


}
