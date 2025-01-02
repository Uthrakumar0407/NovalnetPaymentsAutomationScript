package com.nn.testcase;

import com.aventstack.extentreports.Status;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.IdealCallbackEvents;
import com.nn.callback.PayPalCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.HashMap;

import static com.nn.callback.CallbackProperties.IDEAL;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.*;
import static com.nn.utilities.DriverActions.clickElementByRefreshing;


public class PayPalPaymentTests extends BaseTest {

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
            .callback(new PayPalCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("paypal") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }

    @Test(priority = 1, description = "Check whether the PayPal capture payment order placed successfully and status is shown as pending, call transaction update ,full refund ,charge back and credit events executed successfully",retryAnalyzer = RetryListener.class)
    public void pendingPayPalPayment(){
        verifyGlobalConfiguration();
        setPaymentConfiguration(true,CAPTURE,"",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PAYPAL);
        statusCommentsVerification(PENDING_ORDER_STATUS,true);
        statusUpdateEvent(COMPLETION_ORDER_STATUS);
        chargeback();
        creditEvent();
        transactionRefund(Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString()));
    }

    @Test(priority = 2, description = "Check whether Authorize Paypal payment order placed successfully and status is shown as on_hold , call transaction capture , payment reminder events and refund executed successfully",retryAnalyzer = RetryListener.class)
    public void authorizePayPalPaymentAndReminder(){
        setPaymentConfiguration(true,AUTHORIZE,"",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, PAYPAL);
        statusCommentsVerification(ONHOLD_ORDER_STATUS,false);
        captureTransaction();
        paymentReminder(PAYMENT_REMINDER_1);
        paymentReminder(PAYMENT_REMINDER_2);
        submissionToCollection();
        transactionRefund(Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString())/2);
    }

    @Test(priority = 3, description = "Check whether the authorize test order with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMinimumOrder(){
        setPaymentConfiguration(true,AUTHORIZE,"100",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, PAYPAL);
        statusCommentsVerification(ONHOLD_ORDER_STATUS,false);
        cancelTransaction();
    }

    @Test(priority = 4, description = "Check whether the authorize test order with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 5000",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMaximumOrder(){
        setPaymentConfiguration(true,AUTHORIZE,"5000",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PAYPAL);
        statusCommentsVerification(PENDING_ORDER_STATUS,true);
    }

    @Test(priority = 5, description = "Check whether the test order is successfully payment event by communication break success",retryAnalyzer = RetryListener.class)
    public void communicationBreakSuccessPayPal(){
        setPaymentConfiguration(true,AUTHORIZE,"",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        var orderNumber = communicationBreakGetOrderNumber();
        String tid=NovalnetAPIs.getRecentTransactionTID(orderNumber);
        wooCommerce.getCallback().communicationBreakSuccess(tid.trim(),"");
        statusCommentsVerificationAfterCommunicationBreak(COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 6, description = "Check whether the test order is successfully payment event by communication break success",retryAnalyzer = RetryListener.class)
    public void communicationBreakFailurePayPal(){
        setPaymentConfiguration(true,AUTHORIZE,"",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        var orderNumber = communicationBreakGetOrderNumber();
        String tid=NovalnetAPIs.getRecentTransactionTID(orderNumber);
        wooCommerce.getCallback().communicationBreakFailure(tid.trim(),orderNumber,"");
        statusCommentsVerificationAfterCommunicationBreak(FAILURE_ORDER_STATUS);
    }

    @Test(priority = 7, description = "Check whether the authorize test order is cancelled via shop backend",retryAnalyzer = RetryListener.class)
    public void transactionCancelViaShopBackend(){
        setPaymentConfiguration(true,AUTHORIZE,"",true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, PAYPAL);
        statusCommentsVerification(ONHOLD_ORDER_STATUS,false);
        cancelTransaction();
    }

    @Test(priority = 8, description = "Check whether the authorize test order is cancelled via CallBack",retryAnalyzer = RetryListener.class)
    public void transactionCancelViaCallBack(){
        setPaymentConfiguration(true,AUTHORIZE,"",true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, PAYPAL);
        statusCommentsVerification(ONHOLD_ORDER_STATUS,false);
        cancelEvent();
    }

    @Test(priority = 9, description = "Check whether the test transaction is successful with guest user ")
    public void guestOrder(){
        setPaymentConfiguration(true,CAPTURE,"",false);
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getGuestOrderSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PAYPAL);
        wooCommerce.getAdminPage().adminLogin("paypal", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),PENDING_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(PENDING_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString(),true);
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }
    @Test(priority = 10,description = "Check whether paypal payment logo displayed ")
    public void verifyPaypalLogoDisplayed(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("paypal","wordpress");
        setPaymentConfiguration(true,CAPTURE,"",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("PayPal");
    }
    @Test(priority = 11, description = "Verify the display of error text on the checkout page in case a transaction is aborted, leading to a change in the order status to \"failed\".",retryAnalyzer = RetryListener.class)
    public void transactionAbort() {
        setPaymentConfiguration(true,AUTHORIZE,"",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        cancelAtPayPalRedirection();
        //verifyCheckoutErrorMessage(PAYPAL_END_USER_CANCEL_ERROR);
        var errorMessage = wooCommerce.getCheckoutPage().getCheckoutPaymentError();
        verifyPaypalErrorMessage(errorMessage,PAYPAL_END_USER_CANCEL_ERROR,REDIRECT_END_USER_CANCEL_ERROR,"Verify checkout validation error message");
        wooCommerce.getMyAccountPage().loadOrders();
        String orderNumber = wooCommerce.getMyAccountPage().getFailedOrderNumber();
        String tid= NovalnetAPIs.getRecentTransactionTID(orderNumber);
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_FAILURE, PAYPAL);
        statusCommentsVerificationAfterTransactionAbort(orderNumber,FAILURE_ORDER_STATUS);

    }
    @Test(priority = 12, description = "Verify download product displayed inside my download products and download link displayed when the transaction is confirmed")
    public void downloadProductConfirmOrder(){

        setPaymentConfiguration(true,CAPTURE,"",false);
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PAYPAL);
        statusCommentsVerification(PENDING_ORDER_STATUS,true);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusUpdateEvent(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);

    }
    @Test(priority = 13,description = "Verify download option should not displayed when transaction is on hold,then capture the order verify download option should be displayed")
    public void downloadProductOnHoldToCaptureOrder(){

        setPaymentConfiguration(true,AUTHORIZE,"",false);
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, PAYPAL);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(ONHOLD_ORDER_STATUS,false);
        captureTransaction();
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }

    @Test(priority = 14,description = "Verify download option should not displayed when transaction is on hold,then cancel the order verify download option should not be displayed")
    public void downloadProductOnHoldToCancelOrder(){
        setPaymentConfiguration(true,AUTHORIZE,"",false);
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, PAYPAL);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(ONHOLD_ORDER_STATUS,false);
        cancelTransaction();
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
    }
    @Test(priority = 15, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("paypal","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(PAYPAL,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(PAYPAL,false);
    }
    @Step("Perform TRANSACTION_CAPTURE callback event" )
    public void transactionCapture() {
        wooCommerce.getCallback().transactionCapture(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CAPTURE_COMMENT_,false);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CAPTURE_COMMENT_);
    }
    @Step("Capture Transaction in the shop backend")
    public void captureTransaction() {
        wooCommerce.getOrdersPage().selectOrderStatus(COMPLETION_ORDER_STATUS);
        //TID_Helper.verifyTIDStatus(wooCommerce.getTxnInfo().get("TID").toString(), TID_STATUS_PENDING);
        TID_Helper.verifyPayPalTIDStatus(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CAPTURE_COMMENT_,false);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CAPTURE_COMMENT_);

    }

    @Step("Perform TRANSACTION_REFUND callback event")
    public void transactionRefund(int refundAmount) {
        int totalAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString());
        String status;
        if(refundAmount < totalAmount)
            status = COMPLETION_ORDER_STATUS;
        else
            status = REFUND_ORDER_STATUS;
        var response = wooCommerce.getCallback().transactionRefund(wooCommerce.getTxnInfo().get("TID").toString(),String.valueOf(refundAmount));
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(refundAmount));
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupComments(REFUND_COMMENT_);
    }


    @Step("Perform Payment Reminder callback event {0}")
    public void paymentReminder(String event) {
        var response = wooCommerce.getCallback().paymentReminder(wooCommerce.getTxnInfo().get("TID").toString(),event);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        if(event.equals(PAYMENT_REMINDER_1))
            wooCommerce.getMyAccountPage().verifyFollowupComments(REMINDER_ONE_COMMENT_);
        else
            wooCommerce.getMyAccountPage().verifyFollowupComments(REMINDER_TWO_COMMENT_);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        if(event.equals(PAYMENT_REMINDER_1))
            wooCommerce.getOrdersPage().verifyOrderNotesComments(REMINDER_ONE_COMMENT_,false);
        else
            wooCommerce.getOrdersPage().verifyOrderNotesComments(REMINDER_TWO_COMMENT_,false);
    }

    @Step("Perform SUBMISSION_TO_COLLECTION callback event")
    public void submissionToCollection() {
        var response = wooCommerce.getCallback().submissionToCollection(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesComments(COLLECTION_COMMENT_,false);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(COLLECTION_COMMENT_);
    }

    @Step("Cancel Transaction in the shop backend")
    public void cancelTransaction() {
        wooCommerce.getOrdersPage().selectOrderStatus(CANCELLATION_ORDER_STATUS);
        TID_Helper.verifyTIDStatus(wooCommerce.getTxnInfo().get("TID").toString(), TID_STATUS_DEACTIVATED);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CANCEL_COMMENT_,false);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CANCEL_COMMENT_);
    }
    @Step("Perform partial refund through the shop's backend")
    public void partialRefundAdmin() {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        var refundAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString())/2;
        wooCommerce.getOrdersPage().initiateRefund(refundAmount);
        var newTID = getFirstMatchRegex(wooCommerce.getOrdersPage().getOrderNoteComment(REFUND_COMMENT_), "(?:New TID:)\\s*(\\d{17})");
        TID_Helper.verifyTIDInformation(newTID.replaceAll("[^0-9]", ""), String.valueOf(refundAmount), TID_STATUS_CONFIRMED, CREDITCARD_BOOKBACK);
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(refundAmount));
        var refundComment = wooCommerce.getOrdersPage().getOrderNoteComment(REFUND_COMMENT_);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(refundComment);
    }


    @Step("Verify the transaction order status and novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderStaus,boolean pending) {
        //wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStaus);
        if(!orderStaus.equals(FAILURE_ORDER_STATUS))
            wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("NovalnetComments").toString(),wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString(),pending);
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        if(orderStaus.equals(COMPLETION_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }

    @Step("Verify the transaction update status and novalnet paypal payment comments appended successfully")
    public void statusUpdateEvent(String orderStaus) {
        wooCommerce.getCallback().transactionUpdateStatus(wooCommerce.getTxnInfo().get("TID").toString(),TID_STATUS_CONFIRMED);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(UPDATE_COMMENT_);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStaus);
    }
    @Step("Verify the transaction order status and novalnet payment comments appended successfully")
    public void statusCommentsVerificationAfterCommunicationBreak(String orderStaus) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getTxnInfo().put("NovalnetComments",wooCommerce.getOrdersPage().getOrderNoteComment(INITIAL_LEVEL_COMMENT_));
        if(!orderStaus.equals(FAILURE_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        //wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        if(orderStaus.equals(COMPLETION_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStaus);
        if(!orderStaus.equals(FAILURE_ORDER_STATUS))
            wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("NovalnetComments").toString(),wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }

    @Step("Perform chargeback callback event")
    public void chargeback() {
        var response = wooCommerce.getCallback().chargeback(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CHARGEBACK_COMMENT_, wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();
    }

    @Step("Perform credit callback event {0}")
    public void creditEvent() {
        wooCommerce.getCallback().creditEntryDE(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();
    }

    @Step("Perform TRANSACTION_REFUND callback event")
    public void transactionRefund(int refundAmount, String status) {
        int totalAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString());
        var response = wooCommerce.getCallback().transactionRefund(wooCommerce.getTxnInfo().get("TID").toString(),String.valueOf(refundAmount));
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(refundAmount));
        var refundComment = wooCommerce.getOrdersPage().getOrderNoteComment(REFUND_COMMENT_);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupComments(refundComment);
    }


    @Step("Perform Cancel callback event {0}")
    public void cancelEvent() {
        wooCommerce.getCallback().transactionCancel(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), CANCELLATION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CANCEL_COMMENT_,false);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();
    }
    @Step("Communication break at PayPal redirect page and Get Pending order number")
    public String communicationBreakGetOrderNumber(){
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        wooCommerce.getCheckoutPage().waitForPaypalRedirectionPage();
        wooCommerce.getMyAccountPage().loadOrders();
        String orderNumber = wooCommerce.getMyAccountPage().getPendingOrderNumber();
        wooCommerce.getTxnInfo().put("OrderNumber",orderNumber);
        return orderNumber;
    }

    @Step("Navigate to checkout by adding product {0} to cart")
    public void navigateCheckout(String productName){
        wooCommerce.getCartPage().load();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        wooCommerce.getProductPage().addProductToCartByName(new String[]{productName});
        wooCommerce.getHomePage().openCheckoutPage();
    }


    @Step("Set Payment Configuration active {0}, payment action {1}, minimum authorize amount {2}, test mode {3}, one click {4}, inline form {5}, enforce3D {6}")
    public void setPaymentConfiguration(boolean paymentActive,
                                        String paymentAction,
                                        String minAuthAmount,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(
                        paymentActive,
                        paymentAction,
                        minAuthAmount,
                        testMode,
                        null,
                        null,
                        null, null,wooCommerce.getSettingsPage().getPayment(PAYPAL)));
    }

    @Step("Setting up the Novalnet API configuration")
    public void verifyGlobalConfiguration() {
        wooCommerce.getDashBoardPage().loadSettingsPage();
        //wooCommerce.getDashBoardPage().openSettingsPage();
        wooCommerce.getSettingsPage().openNovalnetGlobalConfig();
        wooCommerce.getSettingsPage().verifyGlobalConfig();
    }


    public By getCcPayment() {
        return CC_PAYMENT;
    }

    @Step("Abort Transaction with PayPal")
    public void cancelAtPayPalRedirection() {
        //clickPlaceOrderBtn();
        waitForURLToBe("https://www.sandbox.paypal.com/",120);
        clickElementByRefreshing(By.xpath("//a[contains(text(),'Cancel and return')]"));
    }

    @Step("Verify validation error message at checkout")
    public void verifyCheckoutErrorMessage(String expected){
        var actual = getElementText(By.cssSelector(".woocommerce-error li")).trim();
        verifyEquals(actual,expected,"Verify checkout validation error message");
    }

    @Step("Verify the transaction order status and novalnet payment comments appended successfully - Transaction Abort")
    public void statusCommentsVerificationAfterTransactionAbort( String orderNumber,String orderStaus) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber, orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(REDIRECT_END_USER_CANCEL_ERROR,false);
    }

    public void verifyPaypalErrorMessage(String actualError,String expectedError1,String expectedError2,String message){

        boolean actual =  actualError.equals(expectedError1)||actualError.equals(expectedError2);
          if(actual){
              Log.info(message+" Verify Paypal Transcation abort error message ");
              ExtentTestManager.logMessage(Status.PASS, message+" Verify Paypal Transcation abort error message ");
              AllureManager.saveLog(message+"Verify Paypal Transcation abort error message");
          }else {
              ExtentTestManager.logMessage(Status.FAIL, message+"Verify Paypal Transcation abort error message");
              AllureManager.saveLog(message+"Verify Paypal Transcation abort error message");
              Assert.assertTrue(actual);
          }

    }

    }
