package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class BlikCallbackEvents implements CallbackEventInterface{

    CallbackProperties callback;

    public BlikCallbackEvents() {
        callback = new CallbackProperties();
    }

    @Step("Perform TRANSACTION_REFUND event")
    @Override
    public String transactionRefund(String tid) {
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_REFUND);
        callback.setPaymentType(BLIK_REFUND);
        return callback.sendCallbackRequest();
    }
    @Step("Perform TRANSACTION_REFUND event")
    @Override
    public String transactionRefund(String tid,String amount) {
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_REFUND);
        callback.setPaymentType(BLIK_REFUND);
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
        callback.setPaymentType(BLIK);
        return callback.sendCallbackRequest();
    }


    @Step("Perform Payment callback event for success")
    @Override
    public String communicationBreakSuccess(String tid, String amount) {
        callback.setCommunicationBreakStatus("SUCCESS");
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(PAYMENT);
        callback.setPaymentType(BLIK);
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
        callback.setPaymentType(BLIK);
        return callback.sendCallbackRequest();
    }

    @Step("Perform Payment callback event for failure")
    @Override
    public String communicationBreakFailure(String tid, String amount) {
        callback.setCommunicationBreakStatus("FAILURE");
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(PAYMENT);
        callback.setPaymentType(BLIK);
        return callback.sendCallbackRequest();
    }
}
