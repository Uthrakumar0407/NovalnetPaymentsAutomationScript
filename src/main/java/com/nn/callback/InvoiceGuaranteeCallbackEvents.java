package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class InvoiceGuaranteeCallbackEvents implements CallbackEventInterface{

    CallbackProperties callback;

    public InvoiceGuaranteeCallbackEvents(){
        callback = new CallbackProperties();
    }

    @Step("Perform TRANSACTION_CAPTURE event")
    @Override
    public String transactionCapture(String tid) {
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_CAPTURE);
        callback.setPaymentType(GUARANTEED_INVOICE);
        return callback.sendCallbackRequest();
    }

    @Step("Perform TRANSACTION_CANCEL event")
    @Override
    public String transactionCancel(String tid) {
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_CANCEL);
        callback.setPaymentType(GUARANTEED_INVOICE);
        return callback.sendCallbackRequest();
    }

    @Step("Perform TRANSACTION_REFUND event")
    public String transactionRefund(String tid,String amount) {
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_REFUND);
        callback.setPaymentType(GUARANTEED_INVOICE_BOOKBACK);
        return callback.sendCallbackRequest();
    }

    @Step("Perform TRANSACTION_REFUND event")
    public String transactionRefund(String eventType, String tid,String amount) {
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_REFUND);
        callback.setPaymentType(eventType);
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

    @Step("Perform TRANSACTION_UPDATE event with status")
    @Override
    public String transactionUpdateStatus(String tid, String status) {
        callback.setTID(tid);
        callback.setUpdateType("STATUS");
        callback.setCallbackValueOrAmount(status);
        callback.setEvent(TRANSACTION_UPDATE);
        callback.setPaymentType(GUARANTEED_INVOICE);
        return callback.sendCallbackRequest();
    }

    @Step("Perform PAYMENT_REMINDER_1 event")
    @Override
    public String paymentReminderOne(String tid) {
        callback.setTID(tid);
        callback.setEvent(PAYMENT_REMINDER_1);
        callback.setPaymentType(GUARANTEED_INVOICE);
        return callback.sendCallbackRequest();
    }

    @Step("Perform PAYMENT_REMINDER_2 event")
    @Override
    public String paymentReminderTwo(String tid) {
        callback.setTID(tid);
        callback.setEvent(PAYMENT_REMINDER_2);
        callback.setPaymentType(GUARANTEED_INVOICE);
        return callback.sendCallbackRequest();
    }
}
