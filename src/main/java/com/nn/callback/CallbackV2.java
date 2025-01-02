package com.nn.callback;

import lombok.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CallbackV2 {

    private String paymentAccesskey;
    private Event event;
    private String paymentType;
    private String tid;
    private int amount;
    private ResultStatus resultStatus;
    private TransactionStatus transactionStatus;
    private UpdateType updateType;
    private int cyclesExecuted;
    private String nextInstalmentCycleDate;
    private int pendingCycles;
    private String dueDate;
    private int instalmentCycleCount;
    private String nextSubscriptionCycleDate;
    private SubscriptionPayments subscriptionChangePaymentType;
    private String subscriptionAmount;
    private String orderNumber;
    private InstalmentCancelTypes instalmentCancelType;
    private SubscriptionUpdateType subscriptionUpdateType;

    public enum ResultStatus {
        SUCCESS, FAILURE
    }

    public enum TransactionStatus {
        CONFIRMED, ON_HOLD, DEACTIVATED, PENDING
    }

    public enum UpdateType {
        STATUS, AMOUNT, DUE_DATE, AMOUNT_DUE_DATE
    }

    public enum SubscriptionUpdateType {
        AMOUNT, PAYMENT_TYPE, NEXT_CYCLE_DATE
    }

    public enum InstalmentCancelTypes {
        ALL_CYCLES, REMAINING_CYCLES
    }

    public enum Event {
        PAYMENT, CREDIT, TRANSACTION_CAPTURE, TRANSACTION_CANCEL, TRANSACTION_UPDATE, TRANSACTION_REFUND, CHARGEBACK,
        SUBSCRIPTION_CANCEL, SUBSCRIPTION_UPDATE, SUBSCRIPTION_SUSPEND, SUBSCRIPTION_REACTIVATE, RENEWAL, INSTALMENT, INSTALMENT_CANCEL,
        PAYMENT_REMINDER_1, PAYMENT_REMINDER_2, SUBMISSION_TO_COLLECTION_AGENCY
    }

    public enum SubscriptionEvents {
        SUBSCRIPTION_CANCEL, SUBSCRIPTION_UPDATE, SUBSCRIPTION_SUSPEND, SUBSCRIPTION_REACTIVATE, RENEWAL
    }

    public enum Payment {
        CREDITCARD, DIRECT_DEBIT_SEPA,DIRECT_DEBIT_ACH, GUARANTEED_DIRECT_DEBIT_SEPA, INSTALMENT_DIRECT_DEBIT_SEPA,
        INVOICE, GUARANTEED_INVOICE, INSTALMENT_INVOICE, PREPAYMENT, CASHPAYMENT, ONLINE_BANK_TRANSFER, GIROPAY, ONLINE_TRANSFER,
        IDEAL, EPS, PRZELEWY24, BANCONTACT, POSTFINANCE, POSTFINANCE_CARD, TRUSTLY, PAYPAL, MULTIBANCO, APPLEPAY, ALIPAY, WECHATPAY, GOOGLEPAY, BLIK
    }

    public enum SubscriptionPayments {
        CREDITCARD, DIRECT_DEBIT_SEPA, APPLEPAY, PAYPAL, GOOGLEPAY, INVOICE, PREPAYMENT, GUARANTEED_INVOICE, GUARANTEED_DIRECT_DEBIT_SEPA,DIRECT_DEBIT_ACH
    }

    public enum CreditPaymentTypes {
        INVOICE_CREDIT, CASHPAYMENT_CREDIT, BANK_TRANSFER_BY_END_CUSTOMER, MULTIBANCO_CREDIT, ONLINE_TRANSFER_CREDIT,
        CREDIT_ENTRY_SEPA, DEBT_COLLECTION_SEPA, CREDIT_ENTRY_CREDITCARD, DEBT_COLLECTION_CREDITCARD,
        CREDITCARD_REPRESENTMENT, CREDIT_ENTRY_DE, DEBT_COLLECTION_DE, APPLEPAY_REPRESENTMENT, GOOGLEPAY_REPRESENTMENT
    }

    public enum RefundPaymentTypes {
        CREDITCARD_BOOKBACK, GUARANTEED_SEPA_BOOKBACK, GUARANTEED_INVOICE_BOOKBACK, REFUND_BY_BANK_TRANSFER_EU,
        PRZELEWY24_REFUND, INSTALMENT_INVOICE_BOOKBACK, INSTALMENT_SEPA_BOOKBACK, POSTFINANCE_REFUND,
        CASHPAYMENT_REFUND, PAYPAL_BOOKBACK, TRUSTLY_REFUND, ALIPAY_REFUND, WECHATPAY_REFUND,
        APPLEPAY_BOOKBACK, GOOGLEPAY_BOOKBACK, BLIK_REFUND
    }

    public enum ChargebackPaymentTypes {
        CREDITCARD_CHARGEBACK, RETURN_DEBIT_SEPA, PAYPAL_CHARGEBACK, REVERSAL, APPLEPAY_CHARGEBACK,
        GOOGLEPAY_CHARGEBACK
    }

    public enum CapturePaymentTypes {
        CREDITCARD, DIRECT_DEBIT_SEPA, APPLEPAY, PAYPAL, GOOGLEPAY, INVOICE, PREPAYMENT, GUARANTEED_INVOICE,
        GUARANTEED_DIRECT_DEBIT_SEPA, INSTALMENT_INVOICE, INSTALMENT_DIRECT_DEBIT_SEPA
    }

    public enum CancelPaymentTypes {
        CREDITCARD, DIRECT_DEBIT_SEPA, APPLEPAY, PAYPAL, GOOGLEPAY, INVOICE, PREPAYMENT, GUARANTEED_INVOICE,
        GUARANTEED_DIRECT_DEBIT_SEPA, INSTALMENT_INVOICE, INSTALMENT_DIRECT_DEBIT_SEPA, PRZELEWY24,
        POSTFINANCE, POSTFINANCE_CARD, TRUSTLY
    }

    public enum UpdatePaymentTypes {
        CREDITCARD, DIRECT_DEBIT_SEPA, GUARANTEED_DIRECT_DEBIT_SEPA, INSTALMENT_DIRECT_DEBIT_SEPA, INVOICE,
        GUARANTEED_INVOICE, INSTALMENT_INVOICE, PREPAYMENT, CASHPAYMENT, ONLINE_BANK_TRANSFER, GIROPAY,
        ONLINE_TRANSFER, IDEAL, EPS, PRZELEWY24, BANCONTACT, POSTFINANCE, POSTFINANCE_CARD, TRUSTLY,
        PAYPAL, MULTIBANCO, APPLEPAY, ALIPAY, WECHATPAY, GOOGLEPAY
    }

    public enum InstalmentPaymentTypes {
        INSTALMENT_DIRECT_DEBIT_SEPA, INSTALMENT_INVOICE
    }

    public enum SubscriptionPaymentTypes {
        CREDITCARD, DIRECT_DEBIT_SEPA, APPLEPAY, PAYPAL, GOOGLEPAY, INVOICE, PREPAYMENT, GUARANTEED_INVOICE,
        GUARANTEED_DIRECT_DEBIT_SEPA,DIRECT_DEBIT_ACH
    }

    public enum PaymentReminderPaymentTypes {
        CREDITCARD, DIRECT_DEBIT_SEPA, APPLEPAY, PAYPAL, GOOGLEPAY, REVERSAL, INVOICE, GUARANTEED_INVOICE,
        GUARANTEED_DIRECT_DEBIT_SEPA, INSTALMENT_INVOICE, INSTALMENT_DIRECT_DEBIT_SEPA
    }

    public enum CollectionPaymentTypes {
        CREDITCARD, DIRECT_DEBIT_SEPA, APPLEPAY, PAYPAL, GOOGLEPAY, REVERSAL, INVOICE
    }

    public enum CreditCardPaymentType {
        CREDITCARD, CREDITCARD_CHARGEBACK, CREDITCARD_BOOKBACK, CREDIT_ENTRY_CREDITCARD,
        DEBT_COLLECTION_CREDITCARD, CREDITCARD_REPRESENTMENT, BANK_TRANSFER_BY_END_CUSTOMER
    }

    public enum DirectDebitSepaPaymentType {
        DIRECT_DEBIT_SEPA, RETURN_DEBIT_SEPA, REFUND_BY_BANK_TRANSFER_EU, CREDIT_ENTRY_SEPA,
        DEBT_COLLECTION_SEPA, BANK_TRANSFER_BY_END_CUSTOMER,
    }

    public enum InvoicePaymentType {
        INVOICE, INVOICE_CREDIT, REFUND_BY_BANK_TRANSFER_EU, BANK_TRANSFER_BY_END_CUSTOMER,
        DEBT_COLLECTION_DE
    }

    public enum PrepaymentPaymentType {
        PREPAYMENT, INVOICE_CREDIT, REFUND_BY_BANK_TRANSFER_EU, BANK_TRANSFER_BY_END_CUSTOMER,
        DEBT_COLLECTION_DE
    }

    public enum MultibancoPaymentType {
        MULTIBANCO, MULTIBANCO_CREDIT
    }

    public enum PayPalPaymentType {
        PAYPAL, PAYPAL_BOOKBACK, PAYPAL_CHARGEBACK, BANK_TRANSFER_BY_END_CUSTOMER,
        CREDIT_ENTRY_DE
    }

    public enum OnlineTransferPaymentType {
        ONLINE_TRANSFER, REFUND_BY_BANK_TRANSFER_EU, ONLINE_TRANSFER_CREDIT, REVERSAL,
        DEBT_COLLECTION_DE, BANK_TRANSFER_BY_END_CUSTOMER
    }

    public enum OnlineBankTransferPaymentType {
        ONLINE_BANK_TRANSFER, REFUND_BY_BANK_TRANSFER_EU, DEBT_COLLECTION_DE, REVERSAL,
        ONLINE_TRANSFER_CREDIT
    }

    public enum BancontactPaymentType {
        BANCONTACT, REFUND_BY_BANK_TRANSFER_EU
    }

    public enum IdealPaymentType {
        IDEAL, REFUND_BY_BANK_TRANSFER_EU, BANK_TRANSFER_BY_END_CUSTOMER, REVERSAL,
        DEBT_COLLECTION_DE, ONLINE_TRANSFER_CREDIT
    }

    public enum EpsPaymentType {
        EPS, REFUND_BY_BANK_TRANSFER_EU, ONLINE_TRANSFER_CREDIT
    }

    public enum GiropayPaymentType {
        GIROPAY, REFUND_BY_BANK_TRANSFER_EU, ONLINE_TRANSFER_CREDIT
    }

    public enum Przelewy24PaymentType {
        PRZELEWY24, PRZELEWY24_REFUND
    }

    public enum CashPaymentType {
        CASHPAYMENT, CASHPAYMENT_REFUND, CASHPAYMENT_CREDIT
    }

    public enum PostFinancePaymentType {
        POSTFINANCE, POSTFINANCE_REFUND
    }

    public enum PostFinanceCardPaymentType {
        POSTFINANCE_CARD, POSTFINANCE_REFUND
    }

    public enum GuaranteedInvoicePaymentType {
        GUARANTEED_INVOICE, GUARANTEED_INVOICE_BOOKBACK, BANK_TRANSFER_BY_END_CUSTOMER,
        REFUND_BY_BANK_TRANSFER_EU
    }

    public enum GuaranteedDirectDebitSepaPaymentType {
        GUARANTEED_DIRECT_DEBIT_SEPA, GUARANTEED_SEPA_BOOKBACK, REFUND_BY_BANK_TRANSFER_EU
    }

    public enum InstalmentInvoicePaymentType {
        INSTALMENT_INVOICE, INSTALMENT_INVOICE_BOOKBACK, BANK_TRANSFER_BY_END_CUSTOMER,
        REFUND_BY_BANK_TRANSFER_EU
    }

    public enum InstalmentDirectDebitSepaPaymentType {
        INSTALMENT_DIRECT_DEBIT_SEPA, INSTALMENT_SEPA_BOOKBACK, REFUND_BY_BANK_TRANSFER_EU
    }

    public enum ApplePayPaymentType {
        APPLEPAY, APPLEPAY_REPRESENTMENT, APPLEPAY_BOOKBACK, APPLEPAY_CHARGEBACK
    }

    public enum GooglePayPaymentType {
        GOOGLEPAY, GOOGLEPAY_REPRESENTMENT, GOOGLEPAY_BOOKBACK, GOOGLEPAY_CHARGEBACK
    }

    public enum AlipayPaymentType {
        ALIPAY, ALIPAY_REFUND
    }

    public enum WeChatPayPaymentType {
        WECHATPAY, WECHATPAY_REFUND
    }

    public enum TrustlyPaymentType {
        TRUSTLY, ONLINE_TRANSFER_CREDIT, DEBT_COLLECTION_DE, TRUSTLY_REFUND, REVERSAL
    }

    public enum BlikPaymentType {
        BLIK, BLIK_REFUND
    }
    public enum DirectDebitACHPaymentType{
        DIRECT_DEBIT_ACH,DIRECT_DEBIT_ACH_REFUND
    }

    protected static Map<Payment, Enum<?>[]> getIndividualPayments() {
        Map<Payment, Enum<?>[]> result = new HashMap<>();
        result.put(Payment.CREDITCARD, CreditCardPaymentType.values());
        result.put(Payment.DIRECT_DEBIT_SEPA, DirectDebitSepaPaymentType.values());
        result.put(Payment.GUARANTEED_DIRECT_DEBIT_SEPA, GuaranteedDirectDebitSepaPaymentType.values());
        result.put(Payment.INSTALMENT_DIRECT_DEBIT_SEPA, InstalmentDirectDebitSepaPaymentType.values());
        result.put(Payment.INVOICE, InvoicePaymentType.values());
        result.put(Payment.GUARANTEED_INVOICE, GuaranteedInvoicePaymentType.values());
        result.put(Payment.INSTALMENT_INVOICE, InstalmentInvoicePaymentType.values());
        result.put(Payment.PREPAYMENT, PrepaymentPaymentType.values());
        result.put(Payment.CASHPAYMENT, CashPaymentType.values());
        result.put(Payment.ONLINE_BANK_TRANSFER, OnlineBankTransferPaymentType.values());
        result.put(Payment.GIROPAY, GiropayPaymentType.values());
        result.put(Payment.ONLINE_TRANSFER, OnlineTransferPaymentType.values());
        result.put(Payment.IDEAL, IdealPaymentType.values());
        result.put(Payment.EPS, EpsPaymentType.values());
        result.put(Payment.PRZELEWY24, Przelewy24PaymentType.values());
        result.put(Payment.BANCONTACT, BancontactPaymentType.values());
        result.put(Payment.POSTFINANCE, PostFinancePaymentType.values());
        result.put(Payment.POSTFINANCE_CARD, PostFinanceCardPaymentType.values());
        result.put(Payment.TRUSTLY, TrustlyPaymentType.values());
        result.put(Payment.PAYPAL, PayPalPaymentType.values());
        result.put(Payment.MULTIBANCO, MultibancoPaymentType.values());
        result.put(Payment.APPLEPAY, ApplePayPaymentType.values());
        result.put(Payment.ALIPAY, AlipayPaymentType.values());
        result.put(Payment.WECHATPAY, WeChatPayPaymentType.values());
        result.put(Payment.GOOGLEPAY, GooglePayPaymentType.values());
        result.put(Payment.BLIK, BlikPaymentType.values());
        return Collections.unmodifiableMap(result);
    }

    protected static Map<Event, Enum<?>[]> getPaymentsGroupByEvents() {
        Map<Event, Enum<?>[]> result = new HashMap<>();
        result.put(Event.PAYMENT, Payment.values());
        result.put(Event.CREDIT, CreditPaymentTypes.values());
        result.put(Event.TRANSACTION_CAPTURE, CapturePaymentTypes.values());
        result.put(Event.TRANSACTION_CANCEL, CancelPaymentTypes.values());
        result.put(Event.TRANSACTION_UPDATE, UpdatePaymentTypes.values());
        result.put(Event.TRANSACTION_REFUND, RefundPaymentTypes.values());
        result.put(Event.CHARGEBACK, ChargebackPaymentTypes.values());
        result.put(Event.SUBSCRIPTION_CANCEL, SubscriptionPayments.values());
        result.put(Event.SUBSCRIPTION_UPDATE, SubscriptionPayments.values());
        result.put(Event.SUBSCRIPTION_SUSPEND, SubscriptionPayments.values());
        result.put(Event.SUBSCRIPTION_REACTIVATE, SubscriptionPayments.values());
        result.put(Event.RENEWAL, SubscriptionPayments.values());
        result.put(Event.INSTALMENT, InstalmentPaymentTypes.values());
        result.put(Event.INSTALMENT_CANCEL, InstalmentPaymentTypes.values());
        result.put(Event.PAYMENT_REMINDER_1, PaymentReminderPaymentTypes.values());
        result.put(Event.PAYMENT_REMINDER_2, PaymentReminderPaymentTypes.values());
        result.put(Event.SUBMISSION_TO_COLLECTION_AGENCY, CollectionPaymentTypes.values());
        return Collections.unmodifiableMap(result);
    }

}
