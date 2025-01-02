package com.nn.callback;

import static com.nn.callback.CallbackV2.DirectDebitSepaPaymentType.REFUND_BY_BANK_TRANSFER_EU;
import static com.nn.callback.CallbackV2.Event.*;
import static com.nn.callback.CallbackV2.Payment.DIRECT_DEBIT_SEPA;
import static com.nn.callback.CallbackV2.Payment.GUARANTEED_DIRECT_DEBIT_SEPA;

public class GuaranteeSEPA implements ICallback{

    CallbackV2Service callbackV2Service;

    public GuaranteeSEPA(){
        callbackV2Service = new CallbackV2Service();
    }
    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, CallbackV2.TransactionStatus transactionStatus, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(PAYMENT)
                .paymentType(String.valueOf(GUARANTEED_DIRECT_DEBIT_SEPA))
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
                .paymentType(String.valueOf(GUARANTEED_DIRECT_DEBIT_SEPA))
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
                .paymentType(String.valueOf(GUARANTEED_DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
    @Override
    public String transactionCancel(String tid) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_CANCEL)
                .paymentType(String.valueOf(GUARANTEED_DIRECT_DEBIT_SEPA))
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
   /* @Override
    public String transactionRefund(String tid, int amount) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_REFUND)
                .paymentType(String.valueOf())
                .amount(amount)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }*/

    @Override
    public String transactionUpdate(String tid, CallbackV2.UpdateType updateType, CallbackV2.TransactionStatus transactionStatus) {
        CallbackV2 callbackV2 = CallbackV2.builder()
                .event(TRANSACTION_UPDATE)
                .paymentType(String.valueOf(DIRECT_DEBIT_SEPA))
                .updateType(updateType)
                .transactionStatus(transactionStatus)
                .tid(tid)
                .build();
        return callbackV2Service.sendCallbackRequest(callbackV2);
    }
}
