package com.nn.callback;

import static com.nn.callback.CallbackV2.CreditCardPaymentType.CREDITCARD_CHARGEBACK;
import static com.nn.callback.CallbackV2.CreditCardPaymentType.CREDIT_ENTRY_CREDITCARD;
import static com.nn.callback.CallbackV2.Event.*;
import static com.nn.callback.CallbackV2.DirectDebitSepaPaymentType.*;
import static com.nn.callback.CallbackV2.Payment.CREDITCARD;
import static com.nn.callback.CallbackV2.Payment.DIRECT_DEBIT_SEPA;

public class DirectDebitSEPA implements ICallback {

    CallbackV2Service callbackV2Service;

    public DirectDebitSEPA(){
        callbackV2Service = new CallbackV2Service();
    }
    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, CallbackV2.TransactionStatus transactionStatus, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
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
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .resultStatus(resultStatus)
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String transactionCapture(String tid) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_CAPTURE)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String transactionCancel(String tid) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_CANCEL)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String transactionRefund(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_REFUND)
                .paymentType(String.valueOf(REFUND_BY_BANK_TRANSFER_EU))
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionUpdate(String tid, CallbackV2.UpdateType updateType, CallbackV2.TransactionStatus resultStatus) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_UPDATE)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .updateType(updateType)
                .transactionStatus(resultStatus)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String credit(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(CREDIT)
                .paymentType(String.valueOf(CREDIT_ENTRY_SEPA))
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String credit(String tid, CallbackV2.CreditPaymentTypes creditPaymentType, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(CREDIT)
                .paymentType(String.valueOf(creditPaymentType))
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    public String chargeback(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(CHARGEBACK)
                .paymentType(String.valueOf(RETURN_DEBIT_SEPA))
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    // YYYY-MM-DD HH:mm:ss    -> nextCycleDate should be in this format
    public String renewal(String tid, String nextCycleDate){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(RENEWAL)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .nextSubscriptionCycleDate(nextCycleDate)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String subscriptionUpdateAmount(String tid, int amount){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_UPDATE)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .subscriptionUpdateType(CallbackV2.SubscriptionUpdateType.AMOUNT)
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    // YYYY-MM-DD HH:mm:ss    -> nextCycleDate should be in this format
    public String subscriptionUpdateNextCycleDate(String tid, String nextCycleDate){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_UPDATE)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .subscriptionUpdateType(CallbackV2.SubscriptionUpdateType.NEXT_CYCLE_DATE)
                .nextSubscriptionCycleDate(nextCycleDate)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String subscriptionUpdateChangePayment(String tid, CallbackV2.SubscriptionPayments subscriptionPayment){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_UPDATE)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .subscriptionUpdateType(CallbackV2.SubscriptionUpdateType.PAYMENT_TYPE)
                .subscriptionChangePaymentType(subscriptionPayment)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String subscriptionSuspend(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_SUSPEND)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String subscriptionReactivate(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_REACTIVATE)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String subscriptionCancel(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_CANCEL)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String paymentReminderOne(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT_REMINDER_1)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }


    @Override
    public String paymentReminderTwo(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT_REMINDER_2)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String submissionToCollection(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBMISSION_TO_COLLECTION_AGENCY)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }



}
