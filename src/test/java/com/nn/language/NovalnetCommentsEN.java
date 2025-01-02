package com.nn.language;

import com.nn.Magento.Constants;
import com.nn.apis.GetTransactionDetailApi;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.utilities.ShopwareUtils;
import io.restassured.path.json.JsonPath;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

public class NovalnetCommentsEN {

    private NovalnetCommentsEN() {

    }

    public static String getOrderSuccessComment(String tid) {
        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order";
    }

    public static String getOrderSuccessCommentForZeroAmount(String tid) {
        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order\n" +
                "\n" +
                "This order processed as a zero amount booking";
    }

    public static String getRefundComment(String orderNumber) {

        var refund = NovalnetAPIs.getRefundTransaction(orderNumber);
        if (refund == null) {
            throw new RuntimeException("There is no new TID present in the given order number " + orderNumber + " custom object");
        }
        var currency = getCurrencySymbol(refund.get("currency").toString());
        System.out.println("----------------------------------Refund comments verified successfully--------------------------------");
        return "Refund has been initiated for the TID: " + refund.get("tid_payment").toString() + " with the amount of " + currency + ShopwareUtils.getFormattedAmount(refund.get("amount").toString()) + ". New TID:" + refund.get("tid").toString() + " for the refunded amount";
    }
    public static String getRefundCallbackComment(String tid, String refundAmount, String newTID) {
        var currency = getCurrencySymbol(TID_Helper.getTIDCurrency(tid));
        return "Refund has been initiated for the TID: " + tid + " with the amount of " + currency + ShopwareUtils.getFormattedAmount(refundAmount) + ". New TID:" + newTID + " for the refunded amount";
    }

    public static String getRefundComment(String refundAmount, String tid) {
        var currency = getCurrencySymbol(TID_Helper.getTIDCurrency(tid));
        return "Refund has been initiated for the TID: " + tid + " with the amount of " + currency + ShopwareUtils.getFormattedAmount(refundAmount) + ".";
    }

    public static String getUpdateComment(String updateAmount, String tid) {
        var currency = getCurrencySymbol(TID_Helper.getTIDCurrency(tid));
        return "Transaction updated successfully for the TID: " + tid + " with the amount " + currency + ShopwareUtils.getFormattedAmount(updateAmount) + " on " ;
    }

    public static String getUpdateComment(String tid) {
        return "Transaction updated successfully for the TID: " + tid + " on " ;
    }

    public static String getCaptureComment() {
        return "The transaction has been confirmed on " ;
    }

    public static String getCancelComment() {
        return "The transaction has been canceled on " ;
    }

    public static String getZeroAmountBookedComment(String orderNumber) {
        var book = NovalnetAPIs.getRecentTransaction(orderNumber);
        var currency = getCurrencySymbol(book.get("currency").toString());
        return "Your order has been booked with the amount of " + currency + ShopwareUtils.getFormattedAmount(book.get("amount").toString()) + ". Your new TID for the booked amount: " + book.get("tid");
    }
public static String getZeroAmountBookedCommentACH(String orderNumber) {
    var book = NovalnetAPIs.getRecentTransaction(orderNumber);
    var currency = getCurrencySymbolForACH(book.get("currency").toString());
    return "Your order has been booked with the amount of " + currency + ShopwareUtils.getFormattedAmount(book.get("amount").toString()) + ". Your new TID for the booked amount: " + book.get("tid");
}
    public static String getRestrictedCardErro() {
        return "Restricted card";
    }

    public static String getEndUserCancelCreditCardError() {
        return "3D secure authentication failed or was cancelled";
    }

    public static String getCommunicationFailureComment(String tid) {
        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order" + "\n" + "\n" + "User Abort or 3D Secure Authentication Failed or Unexpected Error";
    }

    public static String getEUAbandonedError() {
        return "Customer has abandoned the transaction";
    }

    public static String getInsufficientFundError() {
        return "Insufficient funds or credit limit exceeded";
    }

    public static String getExpiredCardError() {
        return "Credit card payment not possible: card expired";
    }

    private static String getCurrencySymbol(String ISO) {
        return Currency.getInstance(ISO).getSymbol();
    }


    public static String getOrderSuccessCommentWithBankDetailWithoutDueDate(String tid) {
        JsonPath tidResponse = GetTransactionDetailApi.getAsJsonPath(tid);
        var amount = ShopwareUtils.getFormattedAmount(tidResponse.get("transaction.amount").toString());
        var orderNum = tidResponse.get("transaction.order_no").toString();
        var currency = Currency.getInstance(tidResponse.get("transaction.currency").toString()).getSymbol();

        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order\n" +
                "Please transfer the amount of " + currency + amount + " to the following account." + "\n" +
                "\n" +
                "Account holder: " + tidResponse.get("transaction.bank_details.account_holder") + "\n" +
                "Bank: " + tidResponse.get("transaction.bank_details.bank_name") + "\n" +
                "Place: " + tidResponse.get("transaction.bank_details.bank_place") + "\n" +
                "IBAN: " + tidResponse.get("transaction.bank_details.iban") + "\n" +
                "BIC: " + tidResponse.get("transaction.bank_details.bic") + "\n" +
                "\n" +
                "Please use any of the following payment references when transferring the amount. This is necessary to match it with your corresponding order\n" +
                "Payment Reference 1: TID " + tid + "\n" +
                "Payment Reference 2: BNR-" + Constants.PROJECT_ID + "-" + orderNum;
    }

    public static String getOrderSuccessCommentWithBankDetailWithDueDate(String tid, String totalAmount) {
        JsonPath tidResponse = GetTransactionDetailApi.getAsJsonPath(tid);
        var amount = ShopwareUtils.getFormattedAmount(totalAmount);
        var dueDate = tidResponse.get("transaction.due_date");
        if (dueDate != null)
            dueDate = ShopwareUtils.getFormattedDate(dueDate.toString());
        var orderNum = tidResponse.get("transaction.order_no").toString();
        var currency = Currency.getInstance(tidResponse.get("transaction.currency").toString()).getSymbol();
        var dynamicDueDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now().plusDays(30));
        String transferAmountLine = dueDate != null ? "Please transfer the amount of " + currency + amount + " to the following account on or before " + dueDate + "\n"
                : "Please transfer the amount of " + currency + amount + " to the following account on or before " + dynamicDueDate + "\n";

        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order\n" +
                transferAmountLine +
                "\n" +
                "Account holder: " + tidResponse.get("transaction.bank_details.account_holder") + "\n" +
                "Bank: " + tidResponse.get("transaction.bank_details.bank_name") + "\n" +
                "Place: " + tidResponse.get("transaction.bank_details.bank_place") + "\n" +
                "IBAN: " + tidResponse.get("transaction.bank_details.iban") + "\n" +
                "BIC: " + tidResponse.get("transaction.bank_details.bic") + "\n" +
                "\n" +
                "Please use any of the following payment references when transferring the amount. This is necessary to match it with your corresponding order\n" +
                "Payment Reference 1: TID " + tid + "\n" +
                "Payment Reference 2: BNR-" + Constants.PROJECT_ID + "-" + orderNum;
    }
    public static String getOrderInstalmentInvoiceUpdateComment(String tid, String totalAmount) {
        JsonPath tidResponse = GetTransactionDetailApi.getAsJsonPath(tid);
        var amount = ShopwareUtils.getFormattedAmount(totalAmount);
        var dueDate = tidResponse.get("transaction.due_date");
        if (dueDate != null)
            dueDate = ShopwareUtils.getFormattedDate(dueDate.toString());
        var orderNum = tidResponse.get("transaction.order_no").toString();
        var currency = Currency.getInstance(tidResponse.get("transaction.currency").toString()).getSymbol();
        var dynamicDueDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now().plusDays(30));
        String transferAmountLine = dueDate != null ? "Please transfer the amount of " + currency + amount + " to the following account on or before " + dueDate + "\n"
                : "Please transfer the amount of " + currency + amount + " to the following account on or before " + dynamicDueDate + "\n";

        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order\n" +
                transferAmountLine +
                "\n" +
                "Account holder: " + tidResponse.get("transaction.bank_details.account_holder") + "\n" +
                "Bank: " + tidResponse.get("transaction.bank_details.bank_name") + "\n" +
                "Place: " + tidResponse.get("transaction.bank_details.bank_place") + "\n" +
                "IBAN: " + tidResponse.get("transaction.bank_details.iban") + "\n" +
                "BIC: " + tidResponse.get("transaction.bank_details.bic") + "\n" +
                "\n" +
                "Please use any of the following payment references when transferring the amount. This is necessary to match it with your corresponding order\n" +
                "Payment Reference 1: TID " + tid + "\n" +
                "Payment Reference 2: BNR-" + Constants.PROJECT_ID + "-" + orderNum + "\n" +"\n" +
                "Transaction updated successfully for the TID: " + tid + " with the amount " + currency + ShopwareUtils.getFormattedAmount(totalAmount) + " on ";
    }

    public static String getOrderSuccessCommentWithBankDetailWithoutDueDate(String tid, String totalAmount) {
        JsonPath tidResponse = GetTransactionDetailApi.getAsJsonPath(tid);
        var amount = ShopwareUtils.getFormattedAmount(totalAmount);
        var orderNum = tidResponse.get("transaction.order_no").toString();
        var currency = Currency.getInstance(tidResponse.get("transaction.currency").toString()).getSymbol();

        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order\n" +
                "Please transfer the amount of " + currency + amount + " to the following account." + "\n" +
                "\n" +
                "Account holder: " + tidResponse.get("transaction.bank_details.account_holder") + "\n" +
                "Bank: " + tidResponse.get("transaction.bank_details.bank_name") + "\n" +
                "Place: " + tidResponse.get("transaction.bank_details.bank_place") + "\n" +
                "IBAN: " + tidResponse.get("transaction.bank_details.iban") + "\n" +
                "BIC: " + tidResponse.get("transaction.bank_details.bic") + "\n" +
                "\n" +
                "Please use any of the following payment references when transferring the amount. This is necessary to match it with your corresponding order\n" +
                "Payment Reference 1: TID " + tid + "\n" +
                "Payment Reference 2: BNR-" + Constants.PROJECT_ID + "-" + orderNum;
    }

    public static String getCashPaymentComments(String tid) {
        JsonPath tidResponse = GetTransactionDetailApi.getAsJsonPath(tid);
        var dueDate = ShopwareUtils.getFormattedDate(tidResponse.get("transaction.due_date").toString());
        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order\n" +
                "Slip expiry date: " + dueDate + "\n" +
                "\n" +
                "Store(s) near to you:\n" +
                tidResponse.get("transaction.nearest_stores.1.store_name").toString() + "\n" +
                tidResponse.get("transaction.nearest_stores.1.street").toString() + "\n" +
                tidResponse.get("transaction.nearest_stores.1.city").toString() + "\n" +
                tidResponse.get("transaction.nearest_stores.1.zip").toString() + "\n" +
                getCountryName(tidResponse.get("transaction.nearest_stores.1.country_code").toString()) + "\n" +
                "\n" +
                tidResponse.get("transaction.nearest_stores.2.store_name").toString() + "\n" +
                tidResponse.get("transaction.nearest_stores.2.street").toString() + "\n" +
                tidResponse.get("transaction.nearest_stores.2.city").toString() + "\n" +
                tidResponse.get("transaction.nearest_stores.2.zip").toString() + "\n" +
                getCountryName(tidResponse.get("transaction.nearest_stores.1.country_code").toString()) + "\n" +
                "\n" +
                tidResponse.get("transaction.nearest_stores.3.store_name").toString() + "\n" +
                tidResponse.get("transaction.nearest_stores.3.street").toString() + "\n" +
                tidResponse.get("transaction.nearest_stores.3.city").toString() + "\n" +
                tidResponse.get("transaction.nearest_stores.3.zip").toString() + "\n" +
                getCountryName(tidResponse.get("transaction.nearest_stores.1.country_code").toString());
    }

    public static String getMultibancoComment(String tid) {
        JsonPath tidResponse = GetTransactionDetailApi.getAsJsonPath(tid);
        var amount = ShopwareUtils.getFormattedAmount(tidResponse.get("transaction.amount").toString());
        var currency = Currency.getInstance(tidResponse.get("transaction.currency").toString()).getSymbol();

        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order\n" +
                "\n" +
                "Please use the following payment reference details to pay the amount of " + currency + amount + " at a Multibanco ATM or through your internet banking.\n" +
                "Payment Reference: " + tidResponse.get("transaction.partner_payment_reference").toString();
    }

    //bank detail text for guarantee Invoice
    public static String getGuaranteePendingCommentForInvoice(String tid) {
        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order\n" +
                "Your order is being verified. Once confirmed, we will send you our bank details to which the order amount should be transferred. Please note that this may take up to 24 hours";
    }

    public static String getPendingToOnHoldComment(String tid) {
        return "The transaction status has been changed from pending to on-hold for the TID: " + tid + " on "+ShopwareUtils.getToday() ;
    }

    public static String getInstalmentStoppedComment(String tid) {
        return "Instalment has been stopped for the TID: " + tid + " on " + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
    }
    public static String getInstalmentCancelAllCyclesCancelComment(String tid) {
        return "Instalment has been cancelled for the TID: " + tid + " on " + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
    }
    public static String getInstalmentCancelAllCyclesRefundComment(String tid, String amount) {
        var currency = getCurrencySymbol(TID_Helper.getTIDCurrency(tid));
        return "Refund has been initiated with the amount " + currency+ShopwareUtils.getFormattedAmount(amount);
    }

    public static String getBelow18Error() {
        return "You need to be at least 18 years old";
    }

    private static String getCountryName(String countryCode) {
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
    }

    //common comment without bank detail text
    public static String getGuaranteePendingCommentForSEPA(String tid) {
        return "Novalnet transaction ID: " + tid + "\n" +
                "Test order\n" +
                "Your order is under verification and we will soon update you with the order status. Please note that this may take upto 24 hours.";
    }
    public static String getPaymentReminderOneComment(){
        return "Payment Reminder 1 has been sent to the customer.";
    }
    public static String getPaymentReminderTwoComment(){
        return "Payment Reminder 2 has been sent to the customer.";
    }

    public static String getSubmissionToCollectionComment(){
        return "The transaction has been submitted to the collection agency. Collection Reference:";
    }

    public static String getCreditComment(String tid, String amount){
        var currency = getCurrencySymbol(TID_Helper.getTIDCurrency(tid));
        return "Credit has been successfully received for the TID: "+tid+" with amount "+ currency+ShopwareUtils.getFormattedAmount(amount) +" on "+ShopwareUtils.getToday();
    }

    public static String getChargebackComment(String tid, String amount){
        var currency = getCurrencySymbol(TID_Helper.getTIDCurrency(tid));
        return "Chargeback executed successfully for the TID: "+tid+" with amount "+ currency+ShopwareUtils.getFormattedAmount(amount) +" on "+ShopwareUtils.getToday();
    }
    public static String getInvalidMobileNumberError() {
        return "Mobile number is invalid";
    }

    private static String getCurrencySymbolForACH(String ISO) {
        Currency usCurrency = Currency.getInstance(new Locale("en", "US"));
        return usCurrency.getSymbol();
    }
    public static void main(String[] args) {
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        System.out.println(decimalFormat.format(100 / 100.0));
//        var dynamicDueDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now().plusDays(30));
//        System.out.println(dynamicDueDate);
        //System.out.println(Currency.getInstance("INR").getSymbol().length());
    }


}
