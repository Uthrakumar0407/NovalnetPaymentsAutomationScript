package com.nn.callback;

import io.qameta.allure.Step;

import static com.nn.callback.CallbackProperties.*;

public class InstalmentSEPACallbackEvents implements CallbackEventInterface{

    CallbackProperties callback;
    public InstalmentSEPACallbackEvents(){
        callback = new CallbackProperties();
    }

    @Step("Perform TRANSACTION_CAPTURE event")
    @Override
    public String transactionCapture(String tid) {
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_CAPTURE);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        return callback.sendCallbackRequest();
    }

    @Step("Perform TRANSACTION_CAPTURE event")
    @Override
    public String transactionCapture(String tid,String[] cycleDates,String executedCycles,String pendingCycles, String nextCycleDate) {
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_CAPTURE);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        callback.setCyclesDates(cycleDates);
        callback.setCyclesExecuted(executedCycles);
        callback.setPendingCycles(pendingCycles);
        callback.setNextCycleDate(nextCycleDate);
        return callback.sendCallbackRequest();
    }

    @Step("Perform TRANSACTION_CANCEL event")
    @Override
    public String transactionCancel(String tid) {
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_CANCEL);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        return callback.sendCallbackRequest();
    }

    @Step("Perform TRANSACTION_REFUND event")
    public String transactionRefund(String tid,String amount) {
        callback.setCallbackValueOrAmount(amount);
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_REFUND);
        callback.setPaymentType(INSTALMENT_SEPA_BOOKBACK);
        return callback.sendCallbackRequest();
    }


    @Step("Perform TRANSACTION_UPDATE event with status")
    @Override
    public String transactionUpdateStatus(String tid, String status) {
        callback.setTID(tid);
        callback.setUpdateType("STATUS");
        callback.setCallbackValueOrAmount(status);
        callback.setEvent(TRANSACTION_UPDATE);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        return callback.sendCallbackRequest();
    }

    @Step("Perform TRANSACTION_UPDATE event with status")
    @Override
    public String transactionUpdateStatus(String tid,String status,String[] cycleDates,String executedCycles,String pendingCycles, String nextCycleDate) {
        callback.setTID(tid);
        callback.setEvent(TRANSACTION_UPDATE);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        callback.setUpdateType("STATUS");
        callback.setCallbackValueOrAmount(status);
        callback.setCyclesDates(cycleDates);
        callback.setCyclesExecuted(executedCycles);
        callback.setPendingCycles(pendingCycles);
        callback.setNextCycleDate(nextCycleDate);
        return callback.sendCallbackRequest();
    }

    @Step("Perform PAYMENT_REMINDER_1 event")
    @Override
    public String paymentReminderOne(String tid) {
        callback.setTID(tid);
        callback.setEvent(PAYMENT_REMINDER_1);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        return callback.sendCallbackRequest();
    }

    @Step("Perform PAYMENT_REMINDER_2 event")
    @Override
    public String paymentReminderTwo(String tid) {
        callback.setTID(tid);
        callback.setEvent(PAYMENT_REMINDER_2);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        return callback.sendCallbackRequest();
    }

    @Step("Perform INSTALMENT event")
    @Override
    public String instalment(String tid,String executedCycles, String pendingCycles) {
        callback.setTID(tid);
        callback.setEvent(INSTALMENT);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        callback.setCyclesExecuted(executedCycles);
        callback.setPendingCycles(pendingCycles);
        return callback.sendCallbackRequest();
    }

    @Step("Perform INSTALMENT event")
    @Override
    public String instalment(String tid,String executedCycles, String pendingCycles, String nextCycleDate) {
        callback.setTID(tid);
        callback.setEvent(INSTALMENT);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        callback.setCyclesExecuted(executedCycles);
        callback.setPendingCycles(pendingCycles);
        callback.setNextCycleDate(nextCycleDate);
        return callback.sendCallbackRequest();
    }

    @Step("Perform INSTALMENT_CANCEL event")
    @Override
    public String instalmentCancel(String tid) {
        callback.setTID(tid);
        callback.setEvent(INSTALMENT_CANCEL);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        return callback.sendCallbackRequest();
    }
    @Step("Perform INSTALMENT_CANCEL_ALL_CYCLES event")
    @Override
    public String instalmentCancelAllCycle(String tid) {
        callback.setTID(tid);
        callback.setEvent(INSTALMENT_CANCEL);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        callback.setCancelType(INSTALMENT_CANCEL_ALL_CYCLES);
        return callback.sendCallbackRequest();
    }

    @Step("Perform INSTALMENT_CANCEL_ALL_REMAINING_CYCLES event")
    @Override
    public String instalmentCancelAllRemainingCycle(String tid) {
        callback.setTID(tid);
        callback.setEvent(INSTALMENT_CANCEL);
        callback.setPaymentType(INSTALMENT_DIRECT_DEBIT_SEPA);
        callback.setCancelType(INSTALMENT_CANCEL_REMAINING_CYCLES);
        return callback.sendCallbackRequest();
    }
}
