package com.nn.callback;

import static com.nn.callback.CallbackV2.Event.*;
import static com.nn.callback.CallbackV2.CreditCardPaymentType.*;
import static com.nn.callback.CallbackV2.Payment.CREDITCARD;
public class CreditCard implements ICallback{
    CallbackV2Service callbackV2Service;

    public CreditCard(){
        callbackV2Service = new CallbackV2Service();
    }

    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, CallbackV2.TransactionStatus transactionStatus, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT)
                .paymentType(String.valueOf(CREDITCARD))
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
                .paymentType(String.valueOf(CREDITCARD))
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
                .paymentType(String.valueOf(CREDITCARD))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionCancel(String tid) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_CANCEL)
                .paymentType(String.valueOf(CREDITCARD))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionRefund(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_REFUND)
                .paymentType(String.valueOf(CREDITCARD_BOOKBACK))
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionUpdate(String tid, CallbackV2.UpdateType updateType, CallbackV2.TransactionStatus transactionStatus) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_UPDATE)
                .paymentType(String.valueOf(CREDITCARD))
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
                .paymentType(String.valueOf(CREDIT_ENTRY_CREDITCARD))
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

    @Override
    public String chargeback(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(CHARGEBACK)
                .paymentType(String.valueOf(CREDITCARD_CHARGEBACK))
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
                .paymentType(String.valueOf(CREDITCARD))
                .nextSubscriptionCycleDate(nextCycleDate)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String subscriptionUpdateAmount(String tid, int amount){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_UPDATE)
                .paymentType(String.valueOf(CREDITCARD))
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
                .paymentType(String.valueOf(CREDITCARD))
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
                .paymentType(String.valueOf(CREDITCARD))
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
                .paymentType(String.valueOf(CREDITCARD))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String subscriptionReactivate(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_REACTIVATE)
                .paymentType(String.valueOf(CREDITCARD))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String subscriptionCancel(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_CANCEL)
                .paymentType(String.valueOf(CREDITCARD))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String paymentReminderOne(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT_REMINDER_1)
                .paymentType(String.valueOf(CREDITCARD))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }


    @Override
    public String paymentReminderTwo(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT_REMINDER_2)
                .paymentType(String.valueOf(CREDITCARD))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String submissionToCollection(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBMISSION_TO_COLLECTION_AGENCY)
                .paymentType(String.valueOf(CREDITCARD))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
}
