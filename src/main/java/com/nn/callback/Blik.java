package com.nn.callback;
import static com.nn.callback.CallbackV2.Event.*;
import static com.nn.callback.CallbackV2.RefundPaymentTypes.BLIK_REFUND;

public class Blik implements ICallback {
    CallbackV2Service callbackV2Service;
    public Blik(){
        callbackV2Service = new CallbackV2Service();
    }

    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, CallbackV2.TransactionStatus transactionStatus, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .tid(tid)
                .resultStatus(resultStatus)
                .transactionStatus(transactionStatus)
                .amount(amount)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .tid(tid)
                .resultStatus(resultStatus)
                .amount(amount)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }

    @Override
    public String transactionRefund(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_REFUND)
                .paymentType(String.valueOf(BLIK_REFUND))
                .tid(tid)
                .amount(amount)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
}
