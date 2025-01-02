package com.nn.callback;

public interface ICallback {

    String payment(String tid, CallbackV2.ResultStatus resultStatus, CallbackV2.TransactionStatus transactionStatus, int amount);
    String payment(String tid, CallbackV2.ResultStatus resultStatus, int amount);

    default String transactionCapture(String tid){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }
    default String transactionCapture(String tid, int instalmentCycleCount){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }
    default String transactionCancel(String tid){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }
    default String transactionRefund(String tid, int amount){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String transactionRefund(String tid, CallbackV2.RefundPaymentTypes refundPaymentType, int amount){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

     default String transactionUpdate(String tid, CallbackV2.UpdateType updateType, CallbackV2.TransactionStatus transactionStatus){
        throw new V2CallbackException("this method is not overridden in the sub class");
     }

    default String transactionUpdate(String tid, CallbackV2.UpdateType updateType, CallbackV2.TransactionStatus transactionStatus, int instalmentCycleCount){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String transactionUpdate(String tid, CallbackV2.UpdateType updateType, int amount){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String transactionUpdate(String tid, CallbackV2.UpdateType updateType, String dueDate){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String transactionUpdate(String tid, CallbackV2.UpdateType updateType, int amount, String dueDate){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String credit(String tid, int amount){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String credit(String tid, CallbackV2.CreditPaymentTypes creditPaymentType, int amount){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String chargeback(String tid, int amount){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    // YYYY-MM-DD HH:mm:ss    -> nextCycleDate should be in this format
    default String renewal(String tid, String nextCycleDate){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String subscriptionUpdateAmount(String tid, int amount){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    // YYYY-MM-DD HH:mm:ss    -> nextCycleDate should be in this format
    default String subscriptionUpdateNextCycleDate(String tid, String nextCycleDate){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String subscriptionUpdateChangePayment(String tid, CallbackV2.SubscriptionPayments subscriptionPayment){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String subscriptionSuspend(String tid){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String subscriptionReactivate(String tid){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String subscriptionCancel(String tid){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }
    
    default String paymentReminderOne(String tid){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String paymentReminderTwo(String tid){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String submissionToCollection(String tid){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    // YYYY-MM-DD HH:mm:ss    -> nextCycleDate should be in this format
    default String instalment(String tid, int cycleExecuted, int pendingCycles, String nextCycleDate){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String instalment(String tid, int cycleExecuted, int pendingCycles){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

    default String instalmentCancel(String tid, CallbackV2.InstalmentCancelTypes instalmentCancelType){
        throw new V2CallbackException("this method is not overridden in the sub class");
    }

}
