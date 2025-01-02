package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class DirectDebitACHCallbackEvents implements CallbackEventInterface{

    CallbackProperties callback;
    public DirectDebitACHCallbackEvents() {
        callback = new CallbackProperties();
    }
    @Step("Perform TRANSACTION_REFUND event")
    public String transactionRefund(String tid,String amount) {
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_REFUND);
        callback.setPaymentType(DIRECT_DEBIT_ACH_REFUND);
        return callback.sendCallbackRequest();
    }

    @Step("Perform TRANSACTION_UPDATE event with status")
    @Override
    public String transactionUpdateStatus(String tid, String status) {
        callback.setTID(tid);
        callback.setUpdateType("STATUS");
        callback.setCallbackValueOrAmount(status);
        callback.setEvent(TRANSACTION_UPDATE);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }

    @Step("Perform TRANSACTION_UPDATE event with amount")
    @Override
    public String transactionUpdateAmount(String tid, String amount) {
        callback.setTID(tid);
        callback.setUpdateType("AMOUNT");
        callback.setCallbackValueOrAmount(amount);
        callback.setEvent(TRANSACTION_UPDATE);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }
    @Step("Perform RENEWAL event")
    @Override
    public String subscriptionRenewal(String tid) {
        callback.setTID(tid);
        callback.setEvent(RENEWAL);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }

    @Step("Perform SUBSCRIPTION_UPDATE event")
    @Override
    public String subscriptionUpdate(String tid,String amount, String cycleDate) {
        callback.setSubscriptionAmount(amount);
        callback.setNextSubscriptionCycleDate(cycleDate);
        callback.setTID(tid);
        callback.setEvent(SUBSCRIPTION_UPDATE);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }

    @Step("Perform SUBSCRIPTION_UPDATE event")
    @Override
    public String subscriptionAmountUpdate(String tid,String amount) {
        callback.setSubscriptionAmount(amount);
        callback.setTID(tid);
        callback.setEvent(SUBSCRIPTION_UPDATE);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }

    @Step("Perform SUBSCRIPTION_UPDATE event")
    @Override
    public String subscriptionCycleDateUpdate(String tid,String cycleDate) {
        callback.setNextSubscriptionCycleDate(cycleDate);
        callback.setTID(tid);
        callback.setEvent(SUBSCRIPTION_UPDATE);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }

    @Step("Perform SUBSCRIPTION_UPDATE event")
    @Override
    public String subscriptionChangePayment(String tid,String paymentType) {
        callback.setSubscriptionChangePaymentType(paymentType);
        callback.setTID(tid);
        callback.setEvent(SUBSCRIPTION_UPDATE);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }

    @Step("Perform SUBSCRIPTION_SUSPEND event")
    @Override
    public String subscriptionSuspend(String tid) {
        callback.setTID(tid);
        callback.setEvent(SUBSCRIPTION_SUSPEND);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }

    @Step("Perform SUBSCRIPTION_REACTIVATE event")
    @Override
    public String subscriptionReactivate(String tid) {
        callback.setTID(tid);
        callback.setEvent(SUBSCRIPTION_REACTIVATE);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }

    @Step("Perform SUBSCRIPTION_CANCEL event")
    @Override
    public String subscriptionCancel(String tid) {
        callback.setTID(tid);
        callback.setEvent(SUBSCRIPTION_CANCEL);
        callback.setPaymentType(DIRECT_DEBIT_ACH);
        return callback.sendCallbackRequest();
    }
}
