package com.nn.callback;

public class Invoice implements ICallback{
    CallbackV2Service callbackV2Service;
    public Invoice(){
        callbackV2Service = new CallbackV2Service();
    }
    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, CallbackV2.TransactionStatus transactionStatus, int amount) {
        return null;
    }

    @Override
    public String payment(String tid, CallbackV2.ResultStatus resultStatus, int amount) {
        return null;
    }

    @Override
    public String transactionUpdate(String tid, CallbackV2.UpdateType updateType, CallbackV2.TransactionStatus transactionStatus) {
        return null;
    }
}
