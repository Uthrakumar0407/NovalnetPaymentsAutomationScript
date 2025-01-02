package com.nn.testcase;

import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.InvoiceGuaranteeCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import io.qameta.allure.Step;
import org.testng.annotations.*;

import java.util.HashMap;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;

public class InvoiceGuaranteePaymentTests extends BaseTest {

    WooCommercePage wooCommerce = WooCommercePage.builder()
            .adminPage(new AdminPage())
            .dashBoardPage(new DashboardPage())
            .ordersPage(new OrdersPage())
            .settingsPage(new SettingsPage())
            .homePage(new HomePage())
            .productPage(new ProductPage())
            .cartPage(new CartPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .myAccountPage(new MyAccountPage())
            .subscriptionPage(new SubscriptionPage())
            .callback(new InvoiceGuaranteeCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("invoiceg") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }

    @AfterClass(alwaysRun = true)
    public void adminLogout() {
        wooCommerce.getDashBoardPage().adminLogout();
    }

    @Test(priority = 1, description = "Check whether the capture invoice guarantee order placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void captureOrder_Guarantee(){
        verifyGlobalConfiguration();
        setInvoicePaymentConfiguration_Guarantee(true,false,true,CAPTURE,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE,14);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
        refundAdmin(Integer.parseInt(orderAmount),REFUND_ORDER_STATUS,orderNumber,GUARANTEED_INVOICE_BOOKBACK);
    }

    @Test(priority = 2, description = "Check whether the authorize invoice guarantee order placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void authorizeOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,true,true,AUTHORIZE,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,paymentName, paymentComments, tid);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_CONFIRMED);
        wooCommerce.getCallback().bankTransferByEndCustomer(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        refundAdmin(Integer.parseInt(orderAmount)/2,COMPLETION_ORDER_STATUS,orderNumber,GUARANTEED_INVOICE_BOOKBACK);
    }

    @Test(priority = 3, description = "Check whether the authorize invoice guarantee test order with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMinimumOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,true,false,AUTHORIZE,"100");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeDateOfBirthIsDisplayed(true);
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,paymentName, paymentComments, tid);
        wooCommerce.getCallback().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().transactionRefund(tid,orderAmount);
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,orderAmount,REFUND_ORDER_STATUS);
    }

    @Test(priority = 4, description = "Check whether the authorize invoice guarantee test order with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 5000",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMaximumOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,true,true,AUTHORIZE,"5000");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeDateOfBirthIsDisplayed(true);
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
        wooCommerce.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2),COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 5, description = "Check whether the test order invoice guarantee with b2b is successful and Confirmed via update event",retryAnalyzer = RetryListener.class)
    public void b2bConfirmOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE,30);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
    }

    @Test(priority = 6, description = "Check whether the test order invoice guarantee with b2b is successful and pending to confirmed via update event",retryAnalyzer = RetryListener.class)
    public void b2bUpdateConfirmOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeDateOfBirthIsDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,paymentName, paymentComments, tid);
        wooCommerce.getCallback().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 7, description = "Check whether the test order invoice guarantee with b2b is successful and pending to deactivated via update event",retryAnalyzer = RetryListener.class)
    public void b2bUpdatDeactivateOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        wooCommerce.getCallback().transactionUpdateStatus(tid,TID_STATUS_DEACTIVATED);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
    }

    @Test(priority = 8, description = "Check whether the test order invoice guarantee with b2b is successful and updated from pending to onhold and onhold to confirm",retryAnalyzer = RetryListener.class)
    public void b2bUpdateOnHoldConfirmOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        wooCommerce.getCallback().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,false,ONHOLD_ORDER_STATUS);
        wooCommerce.getCallback().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 9, description = "Check whether the test order invoice guarantee with b2b is successful and updated from pending to onhold and onhold to cancel",retryAnalyzer = RetryListener.class)
    public void b2bUpdateOnHoldCancelOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        wooCommerce.getCallback().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,false,ONHOLD_ORDER_STATUS);
        wooCommerce.getCallback().transactionCancel(tid);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
    }
    @Test(priority = 10, description = "Check whether the test transaction is successful with guest user ")
    public void guestOrder_GuaranteeInvoice(){
        setInvoicePaymentConfiguration_Guarantee(true,false,false,CAPTURE,"");
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        wooCommerce.getAdminPage().adminLogin("invoiceg", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }
    @Test(priority = 11, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout_GuaranteeeInvoice(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("invoiceg","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(GUARANTEED_INVOICE,false);
        navigateCheckout(PRODUCT_2);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }


    @Step("Set Invoice with payment guarantee Configuration active {0}, test mode {1}, allow B2B {2}, payment action {3} and authorize minimum amount {4}")
    public void setInvoicePaymentConfiguration_Guarantee(boolean paymentActive,
                                               boolean testMode,
                                               boolean allowB2B,
                                               String paymentAction,
                                               String authMinAmount){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceGuaranteeConfiguration(paymentActive,testMode,allowB2B,paymentAction,authMinAmount));
    }
}
