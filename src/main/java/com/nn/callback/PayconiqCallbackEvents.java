package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class PayconiqCallbackEvents implements CallbackEventInterface {
    CallbackProperties callback;

    public PayconiqCallbackEvents() {
        callback = new CallbackProperties();
    }

    @Step("Perform Payment callback event for success")
    @Override
    public String communicationBreakSuccess(String tid, String orderNumber, String amount) {
        callback.setCommunicationBreakStatus("SUCCESS");
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setOrderNumber(orderNumber);
        callback.setEvent(PAYMENT);
        callback.setPaymentType(PAYCONIQ);
        return callback.sendCallbackRequest();
    }


    @Step("Perform Payment callback event for success")
    @Override
    public String communicationBreakSuccess(String tid, String amount) {
        callback.setCommunicationBreakStatus("SUCCESS");
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(PAYMENT);
        callback.setPaymentType(PAYCONIQ);
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
        callback.setPaymentType(PAYCONIQ);
        return callback.sendCallbackRequest();
    }

    @Step("Perform Payment callback event for failure")
    @Override
    public String communicationBreakFailure(String tid, String amount) {
        callback.setCommunicationBreakStatus("FAILURE");
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(PAYMENT);
        callback.setPaymentType(PAYCONIQ);
        return callback.sendCallbackRequest();
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
    @Step("Perform CREDIT event")
    @Override
    public String credit(String tid, String amount, String paymentType) {
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_REFUND);
        callback.setPaymentType(REFUND_REVERSAL);
        return callback.sendCallbackRequest();
    }
    @Step("Perform INVOICE_CREDIT event")
    public String credit(String tid,String amount) {
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(CREDIT);
        callback.setPaymentType(REFUND_REVERSAL);
        return callback.sendCallbackRequest();
    }

}
