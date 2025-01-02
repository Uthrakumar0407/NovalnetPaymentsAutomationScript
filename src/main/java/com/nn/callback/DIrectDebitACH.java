package com.nn.callback;

import static com.nn.callback.CallbackV2.DirectDebitACHPaymentType.DIRECT_DEBIT_ACH_REFUND;
import static com.nn.callback.CallbackV2.DirectDebitSepaPaymentType.REFUND_BY_BANK_TRANSFER_EU;
import static com.nn.callback.CallbackV2.Event.*;
import static com.nn.callback.CallbackV2.Payment.DIRECT_DEBIT_ACH;
import static com.nn.callback.CallbackV2.Payment.DIRECT_DEBIT_SEPA;

public class DIrectDebitACH implements ICallback {


    CallbackV2Service callbackV2Service;

    public DIrectDebitACH(){
        callbackV2Service = new CallbackV2Service();
    }


    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, CallbackV2.TransactionStatus transactionStatus, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT)
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH))
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
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH))
                .resultStatus(resultStatus)
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String transactionRefund(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_REFUND)
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH_REFUND))
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
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH))
                .nextSubscriptionCycleDate(nextCycleDate)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String subscriptionUpdateAmount(String tid, int amount){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_UPDATE)
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH))
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
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH))
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
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH))
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
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String subscriptionReactivate(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_REACTIVATE)
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String subscriptionCancel(String tid){
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(SUBSCRIPTION_CANCEL)
                .paymentType(String.valueOf(DIRECT_DEBIT_ACH))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }


}
