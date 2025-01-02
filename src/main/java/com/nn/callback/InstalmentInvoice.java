package com.nn.callback;

import static com.nn.callback.CallbackV2.InstalmentInvoicePaymentType.*;
import static com.nn.callback.CallbackV2.Event.*;


public class InstalmentInvoice implements ICallback{

    CallbackV2Service callbackV2Service;

    public InstalmentInvoice(){
        callbackV2Service = new CallbackV2Service();
    }

    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, CallbackV2.TransactionStatus transactionStatus, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .resultStatus(resultStatus)
                .transactionStatus(transactionStatus)
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .resultStatus(resultStatus)
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionCapture(String tid, int instalmentCycleCount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_CAPTURE)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .instalmentCycleCount(instalmentCycleCount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionCancel(String tid) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_CANCEL)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionRefund(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_REFUND)
                .paymentType(String.valueOf(INSTALMENT_INVOICE_BOOKBACK))
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionUpdate(String tid, CallbackV2.UpdateType updateType, CallbackV2.TransactionStatus transactionStatus) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_UPDATE)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .updateType(updateType)
                .transactionStatus(transactionStatus)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionUpdate(String tid, CallbackV2.UpdateType updateType, CallbackV2.TransactionStatus transactionStatus, int instalmentCycleCount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_UPDATE)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .instalmentCycleCount(instalmentCycleCount)
                .updateType(updateType)
                .transactionStatus(transactionStatus)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String credit(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(CREDIT)
                .paymentType(String.valueOf(BANK_TRANSFER_BY_END_CUSTOMER))
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionRefund(String tid, CallbackV2.RefundPaymentTypes refundPaymentType,  int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_REFUND)
                .paymentType(String.valueOf(refundPaymentType))
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String paymentReminderOne(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT_REMINDER_1)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }


    @Override
    public String paymentReminderTwo(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT_REMINDER_2)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    // YYYY-MM-DD HH:mm:ss    -> nextCycleDate should be in this format
    @Override
    public String instalment(String tid, int cycleExecuted, int pendingCycles, String nextCycleDate){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(INSTALMENT)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .cyclesExecuted(cycleExecuted)
                .nextInstalmentCycleDate(nextCycleDate)
                .pendingCycles(pendingCycles)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String instalment(String tid, int cycleExecuted, int pendingCycles){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(INSTALMENT)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .cyclesExecuted(cycleExecuted)
                .pendingCycles(pendingCycles)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String instalmentCancel(String tid, CallbackV2.InstalmentCancelTypes instalmentCancelType){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(INSTALMENT_CANCEL)
                .paymentType(String.valueOf(INSTALMENT_INVOICE))
                .instalmentCancelType(instalmentCancelType)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }


}
