package com.nn.testcase;

import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.PrepaymentCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Step;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.*;

public class PrepaymentTests extends BaseTest {

    WooCommercePage wooCommerce = WooCommercePage.builder()
            .adminPage(new AdminPage())
            .dashBoardPage(new DashboardPage())
            .ordersPage(new OrdersPage())
            .settingsPage(new SettingsPage())
            .productPage(new ProductPage())
            .cartPage(new CartPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .myAccountPage(new MyAccountPage())
            .subscriptionPage(new SubscriptionPage())
            .callback(new PrepaymentCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("prepayment") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }

    @Test(priority = 1, description = "Check whether the prepayment order placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void firstOrder(){
        setPaymentConfiguration(true,false,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().verifyPrepaymentTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT,14);
        TID_Helper.verifyBankDetails(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        statusCommentsVerification(PROCESSING_ORDER_STATUS);
        partialRefundAdmin();
        fullRefundAdmin();
    }

    @Test(priority = 2, description = "Check whether the prepayment order placed successfully and due date update, credit, refund via callback events executed successfully",retryAnalyzer = RetryListener.class)
    public void secondOrder(){
        setPaymentConfiguration(true,false,"10");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().verifyPrepaymentTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT,10);
        secondTestOrder_DueDateUpdate();
        invoiceCredit(wooCommerce.getTxnInfo().get("TotalAmount").toString(),COMPLETION_ORDER_STATUS);
        transactionRefund(wooCommerce.getTxnInfo().get("TotalAmount").toString(),REFUND_ORDER_STATUS);
    }

    @Test(priority = 3, description = "Check whether the prepayment order placed successfully and amount update, credit, refund via callback events executed successfully",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        amountUpdate();
        invoiceCredit(wooCommerce.getTxnInfo().get("TotalAmount").toString(),PROCESSING_ORDER_STATUS);
        bankTransferByEndCustomer();
        debtCollectionDE();
        transactionRefund(wooCommerce.getTxnInfo().get("TotalAmount").toString(),PROCESSING_ORDER_STATUS);
    }
    @Test(priority = 4, description = "Check whether the test transaction is successful with guest user ",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        setPaymentConfiguration(true,false,"");
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getGuestOrderSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        wooCommerce.getAdminPage().adminLogin("prepayment", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),PROCESSING_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }
    @Test(priority = 5, description = "Verify download product displayed inside my download products and download link displayed when the transaction is confirmed",retryAnalyzer = RetryListener.class)
    public void downloadProductConfirmOrder(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("prepayment","wordpress");
        setPaymentConfiguration(true,true,"");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().verifyPrepaymentTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT,14);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(PENDING_ORDER_STATUS,true);
        invoiceCredit(wooCommerce.getTxnInfo().get("TotalAmount").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 6,description = "Check whether pre payment logo displayed ")
    public void verifyPrePaymentLogoDisplayed(){
        setPaymentConfiguration(true,false,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("Prepayment");
    }
    @Test(priority = 7, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("prepayment","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(PREPAYMENT,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(PREPAYMENT,false);
    }

   /* @Test(priority=4, description="Check whether the shop based subscription order is placed successfully", retryAnalyzer = RetryListener.class)
    public void subscriptionOrderShopBased() {
        setSubscriptionConfigurationShopBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        setPaymentConfiguration(true,false,"10");
        navigateCheckout("TEST-4 SUBS");
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,PROCESSING_ORDER_STATUS,false);
        subscriptionInvoiceCredit();
        subscriptionShopBased_RenewalOrder();
        subscriptionShopBased_RenewalOrderInvoiceCredit();
        subscriptionShopBased_ChangePayment();
        subscriptionUpdateAmount();
        subscriptionShopBased_RenewalOrderTwo();
        subscriptionShopBased_RenewalOrderThree();
    }

    @Test(priority=5, description="Check whether the Novalnet based subscription order is placed successfully followup events executed via shop admin", retryAnalyzer = RetryListener.class)
    public void subscriptionOrderNovalnetBased() {
        setSubscriptionConfigurationNovalnetBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        navigateCheckout("TEST-4 SUBS");
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), PREPAYMENT);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,PROCESSING_ORDER_STATUS,true);
        subscriptionInvoiceCredit();
        subscriptionNovalnetBased_ChangePayment();
        subscriptionNovalnetBased_ChangeNextCycleDate();
        subscriptionNovalnetBased_UpdateAmount();
        subscriptionNovalnetBased_RenewalOrder();
        subscriptionNovalnetBased_Suspend();
        subscriptionNovalnetBased_Reactivate();
        subscriptionNovalnetBased_ChangePaymentAdmin();
        subscriptionNovalnetBased_Cancel();
    }


    @Test(priority=6, description="Check whether the Novalnet based subscription order is placed successfully and follow up callback events executed successfully", retryAnalyzer = RetryListener.class)
    public void subscriptionCallbackOrderNovalnetBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().disableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        navigateCheckout("TEST-4 SUBS");
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), PREPAYMENT);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionRenewalCallback();
        subscriptionSuspendCallback();
        subscriptionReactivateCallback();
        subscriptionAmountUpdateCallback();
        subscriptionCycleDateUpdateCallback();
        subscriptionChangePaymentCallback();
        subscriptionCancelCallback();
    }*/




    @Step("Setting up the Novalnet API configuration")
    public void verifyGlobalConfiguration() {
        wooCommerce.getDashBoardPage().loadSettingsPage();
      //  wooCommerce.getDashBoardPage().openSettingsPage();
        wooCommerce.getSettingsPage().openNovalnetGlobalConfig();
        wooCommerce.getSettingsPage().verifyGlobalConfig();
    }

    @Step("Navigate to checkout by adding product {0} to cart")
    public void navigateCheckout(String productName){
        wooCommerce.getCartPage().load();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        wooCommerce.getProductPage().addProductToCartByName(new String[]{productName});
        wooCommerce.getHomePage().openCheckoutPage();
    }

    @Step("Set Payment Configuration active {0}, test mode {1}, and due date {2}")
    public void setPaymentConfiguration(boolean paymentActive,boolean testMode,String dueDate){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(paymentActive,"","",testMode,null,null,null, dueDate,wooCommerce.getSettingsPage().getPayment(PREPAYMENT)));
    }




    @Step("Perform partial refund via admin")
    public void partialRefundAdmin() {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        var refundAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString())/4;
        var total = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getTxnInfo().put("AmountAfterPartialRefund", total-refundAmount);
        wooCommerce.getOrdersPage().initiateRefund(refundAmount);
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("AmountAfterPartialRefund").toString(), TID_STATUS_PENDING);
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(refundAmount));
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(REFUND_COMMENT_);
    }

    @Step("Perform full refund via admin")
    public void fullRefundAdmin() {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        var refundAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("AmountAfterPartialRefund").toString());
        wooCommerce.getOrdersPage().initiateRefund(refundAmount);
        sleep(15);
        TID_Helper.verifyTIDStatus(wooCommerce.getTxnInfo().get("TID").toString(), TID_STATUS_DEACTIVATED);
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(refundAmount));
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(REFUND_ORDER_STATUS);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), REFUND_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), REFUND_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(REFUND_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(REFUND_COMMENT_);
    }

    @Step("Verify the transaction order status and novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderStaus) {
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
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        if(orderStaus.equals(COMPLETION_ORDER_STATUS) || orderStaus.equals(PROCESSING_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }

    @Step("Perform Due Date update via callback")
    public void secondTestOrder_DueDateUpdate() {
        var dueDate = TID_Helper.getDueDate(wooCommerce.getTxnInfo().get("TID").toString());
        var updatedDueDate = DriverActions.addDaysFromDate(dueDate, 5);
        wooCommerce.getCallback().transactionUpdateDueDate(wooCommerce.getTxnInfo().get("TID").toString(),updatedDueDate);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsDueDate(UPDATE_COMMENT_, updatedDueDate);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();
    }

    @Step("Perform invoice credit for the amount {0}")
    public void invoiceCredit(String amount,String status) {
        wooCommerce.getCallback().invoiceCredit(wooCommerce.getTxnInfo().get("TID").toString(), amount);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, amount);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();
    }
    @Step("Perform transaction refund for the amount {0}")
    public void transactionRefund(String amount, String status) {
        wooCommerce.getCallback().transactionRefund(wooCommerce.getTxnInfo().get("TID").toString(),amount);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, amount);
        var refundComment = wooCommerce.getOrdersPage().getOrderNoteComment(REFUND_COMMENT_);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupComments(refundComment);
    }

    public void amountUpdate() {
        var updateAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString())*2;
        wooCommerce.getTxnInfo().put("UpdatedAmount", updateAmount);
        wooCommerce.getCallback().transactionUpdateAmount(wooCommerce.getTxnInfo().get("TID").toString(),String.valueOf(updateAmount));
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(UPDATE_COMMENT_, String.valueOf(updateAmount));
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(UPDATE_COMMENT_);
    }

    public void bankTransferByEndCustomer() {
        wooCommerce.getCallback().bankTransferByEndCustomer(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
    }

    public void debtCollectionDE() {
        wooCommerce.getCallback().debtCollectionDE(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
    }

    @Step("Set shop based subscription configuration")
    public void setSubscriptionConfigurationShopBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().enableSubscription();
        wooCommerce.getSettingsPage().selectSubscriptionPayments(SUBSCRIPTION_SUPPORTED_PAYMENTS);
        wooCommerce.getSettingsPage().enableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().enableSubscriptionShopBased();
        wooCommerce.getSettingsPage().saveGlobalConfig();
    }


    @Step("Subscription status comments verification")
    public void subscriptionStatusCommentsVerification(String subscriptionStatus,String orderStatus,boolean cancelBtnDisplayed) {
        wooCommerce.getSuccessPage().verifySubscriptionStatus(subscriptionStatus);
        wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), orderStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("NovalnetComments").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(subscriptionStatus);

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subscriptionStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(subscriptionStatus);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyNovalnetSubscriptionCancelBtnDisplayed(cancelBtnDisplayed);
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(orderStatus);

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subscriptionStatus);
        wooCommerce.getSubscriptionPage().verifyOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPaymentName(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getSubscriptionPage().verifySubscriptionParentOrderStatus(orderStatus);

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), orderStatus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }

    @Step("Perform Invoice Credit for subscription order")
    public void subscriptionInvoiceCredit() {
        wooCommerce.getCallback().invoiceCredit(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(COMPLETION_ORDER_STATUS);

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifySubscriptionParentOrderStatus(COMPLETION_ORDER_STATUS);
    }

    @Step("Perform Invoice credit for subscription renewal order")
    public void subscriptionShopBased_RenewalOrderInvoiceCredit() {
        wooCommerce.getCallback().invoiceCredit(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(COMPLETION_ORDER_STATUS);

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifySubscriptionParentOrderStatus(COMPLETION_ORDER_STATUS);
    }

    @Step("Place renewal order via shop cron")
    public void subscriptionShopBased_RenewalOrder() {
        wooCommerce.getSubscriptionPage().loadCronSchedulerPage();
        wooCommerce.getSubscriptionPage().searchCronScheduler(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().runCronOnPayment();

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String renewalOrder = wooCommerce.getSubscriptionPage().verifyRenewalOrderPresent();
        wooCommerce.getTxnInfo().put("RenewalOrderNumber", renewalOrder);
        wooCommerce.getSubscriptionPage().verifySubscriptionRenewalOrderStatus(PROCESSING_ORDER_STATUS);

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getTxnInfo().putAll(wooCommerce.getOrdersPage().getRenewalOrderDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), TID_STATUS_PENDING);
        wooCommerce.getOrdersPage().VerifyOrderNotesAndCustomerNotesSame();
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("RenewalOrderNovalnetComments").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(SUBSCRIPTION_STATUS_ACTIVE);

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(PROCESSING_ORDER_STATUS);

    }

    @Step("Change subscription paymenr via customer side")
    public void subscriptionShopBased_ChangePayment() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionChangePayment();
        wooCommerce.getCheckoutPage().clickSEPA();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().changePayment();

        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription("Direct Debit SEPA");
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPaymentName("Direct Debit SEPA");
        String changePaymentTID = wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getTxnInfo().put("ChangePaymentTID", changePaymentTID);
        TID_Helper.verifyTIDInformation(changePaymentTID,"0", TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(changePaymentTID, true);
    }

    @Step("Change subscription paymenr via customer side")
    public void subscriptionNovalnetBased_ChangePayment() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionChangePayment();
        wooCommerce.getCheckoutPage().clickSEPA();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().changePayment();

        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription("Direct Debit SEPA");
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPaymentName("Direct Debit SEPA");
        String changePaymentTID = wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getTxnInfo().put("ChangePaymentTID", changePaymentTID);
        TID_Helper.verifyTIDInformation(changePaymentTID,"0", TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(changePaymentTID, true);
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), DIRECT_DEBIT_SEPA);
    }

    @Step("Update amount of subscription")
    public void subscriptionUpdateAmount() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        int updatedCycleAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString())/2;
        String updatedTotalCycleAmount = wooCommerce.getSubscriptionPage().updateSubscriptionAmount(updatedCycleAmount);
        wooCommerce.getTxnInfo().put("updatedSubscriptionCycleAmount", updatedTotalCycleAmount);
    }

    @Step("Place subscription renewal order via shop cron")
    public void subscriptionShopBased_RenewalOrderTwo() {
        wooCommerce.getSubscriptionPage().loadCronSchedulerPage();
        wooCommerce.getSubscriptionPage().searchCronScheduler(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().runCronOnPayment();
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String renewalOrder = wooCommerce.getSubscriptionPage().verifyRenewalOrderPresent();
        wooCommerce.getTxnInfo().put("RenewalOrderNumber", renewalOrder);
        wooCommerce.getSubscriptionPage().clickRenewalOrder(renewalOrder);
        wooCommerce.getTxnInfo().put("RenewalOrderTIDAfterUpdate",wooCommerce.getOrdersPage().getOrderNotesTID(INITIAL_LEVEL_COMMENT_));
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("RenewalOrderTIDAfterUpdate").toString(), wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(), TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
    }

    @Step("Place subscription renewal order via customer renewal")
    public void subscriptionShopBased_RenewalOrderThree() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionRenewal();
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        wooCommerce.getSuccessPage().verifySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        String renewalTID = wooCommerce.getOrdersPage().getOrderNotesTID(INITIAL_LEVEL_COMMENT_);
        TID_Helper.verifyTIDInformation(renewalTID, wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
    }

    @Step("Set Novalnet based subscription configuration")
    public void setSubscriptionConfigurationNovalnetBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().enableSubscription();
        wooCommerce.getSettingsPage().setSubscriptionTariff();
        wooCommerce.getSettingsPage().selectSubscriptionPayments(SUBSCRIPTION_SUPPORTED_PAYMENTS);
        wooCommerce.getSettingsPage().enableSubscriptionNovalnetBased();
        wooCommerce.getSettingsPage().enableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
    }


    @Step("Subscription novalnet based change next cycle date via admin")
    public void subscriptionNovalnetBased_ChangeNextCycleDate() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String nextCycleDate = wooCommerce.getSubscriptionPage().getNextPaymentDate();
        String updatedCycleDate = addDaysFromDate(nextCycleDate,5);
        wooCommerce.getSubscriptionPage().changeNextPaymentDate(updatedCycleDate);
        TID_Helper.verifySubscriptionNextPaymentDateInTID(wooCommerce.getTxnInfo().get("TID").toString(), updatedCycleDate,"yyyy-MM-dd");
        String orderTotal = wooCommerce.getSubscriptionPage().getOrderTotalWithCurrency();
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_UPDATE_COMMENT_+orderTotal+" on "+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_UPDATE_COMMENT_+orderTotal+" on "+nextCycleDateInTID);
    }


    @Step("Update subscription amount via admin")
    public void subscriptionNovalnetBased_UpdateAmount() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        int updatedCycleAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString())/2;
        String updatedTotalCycleAmount = wooCommerce.getSubscriptionPage().updateSubscriptionAmount(updatedCycleAmount);
        wooCommerce.getTxnInfo().put("updatedSubscriptionCycleAmount", updatedTotalCycleAmount);
        TID_Helper.verifySubscriptionCycleAmountInTID(wooCommerce.getTxnInfo().get("TID").toString(), updatedTotalCycleAmount);
        String orderTotal = wooCommerce.getSubscriptionPage().getOrderTotalWithCurrency();
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_UPDATE_COMMENT_+orderTotal+" on "+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_UPDATE_COMMENT_+orderTotal+" on "+nextCycleDateInTID);
    }

    @Step("Place subscription renewal order using customer renewal")
    public void subscriptionNovalnetBased_RenewalOrder() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionRenewal();
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        Map<String,Object> renewalDeatails = wooCommerce.getSuccessPage().getSuccessPageTransactionDetails();
        renewalDeatails.putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(renewalDeatails.get("TID").toString(), wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        wooCommerce.getSuccessPage().verifySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(PROCESSING_ORDER_STATUS);
    }

    @Step("Suspend subscription via shop admin")
    public void subscriptionNovalnetBased_Suspend() {
        wooCommerce.getCallback().invoiceCredit(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().selectOrderStatus(SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_SUSPEND_COMMENT_);
        TID_Helper.verifyNextCycleDateExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_SUSPEND_COMMENT_);
    }

    @Step("Reactivate subscription via shop admin")
    public void subscriptionNovalnetBased_Reactivate() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().selectOrderStatus(SUBSCRIPTION_STATUS_ACTIVE);
        TID_Helper.verifyNextCycleDateExist(wooCommerce.getTxnInfo().get("TID").toString(), true);
        String today = changePatternOfDate("MMMM dd, yyyy",new Date());
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_REACTIVATE_COMMENT_+" "+wooCommerce.getTxnInfo().get("TID").toString()+" on "+today+". Next charging date : "+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_REACTIVATE_COMMENT_+" "+wooCommerce.getTxnInfo().get("TID").toString()+" on "+today+". Next charging date : "+nextCycleDateInTID);
    }

    @Step("Change Subscription payment via admin")
    public void subscriptionNovalnetBased_ChangePaymentAdmin() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().changePayment("Direct Debit SEPA",wooCommerce.getTestData().get("IBANDE"));
        String changePaymentTID = wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getTxnInfo().put("ChangePaymentAdminTID", changePaymentTID);
        TID_Helper.verifyTIDInformation(changePaymentTID,"0", TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(changePaymentTID, true);
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), DIRECT_DEBIT_SEPA);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifySubscriptionOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), "Direct Debit SEPA");
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription("Direct Debit SEPA");
    }

    @Step("Cancel Subscription via admin")
    public void subscriptionNovalnetBased_Cancel() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().cancelSubscription();
        TID_Helper.verifySubscriptionCancelReasonExist(wooCommerce.getTxnInfo().get("TID").toString(), true);
        TID_Helper.verifyNextCycleDateExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_CANCEL_COMMENT_);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPageStatus(SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_CANCEL_COMMENT_);
    }

    @Step("Renew Subscription via callback")
    public void subscriptionRenewalCallback() {
        wooCommerce.getCallback().invoiceCredit(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getCallback().subscriptionRenewal(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String renewalOrder = wooCommerce.getSubscriptionPage().verifyRenewalOrderPresent();
        wooCommerce.getTxnInfo().put("RenewalOrderNumber", renewalOrder);
        wooCommerce.getSubscriptionPage().verifySubscriptionRenewalOrderStatus(PROCESSING_ORDER_STATUS);

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getTxnInfo().putAll(wooCommerce.getOrdersPage().getRenewalOrderDetails());
        wooCommerce.getOrdersPage().VerifyOrderNotesAndCustomerNotesSame();
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(PROCESSING_ORDER_STATUS);

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("RenewalOrderNovalnetComments").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());

    }

    @Step("Suspend Subscription via callback")
    public void subscriptionSuspendCallback() {
        wooCommerce.getCallback().subscriptionSuspend(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_SUSPEND_COMMENT_);
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPageStatus(SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_SUSPEND_COMMENT_);
    }

    @Step("Reactivate Subscription via callback")
    public void subscriptionReactivateCallback() {
        wooCommerce.getCallback().subscriptionReactivate(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String today = changePatternOfDate("MMMM dd, yyyy",new Date());
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_REACTIVATE_COMMENT_+wooCommerce.getTxnInfo().get("TID").toString()+" on "+today+". Next charging date :"+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_REACTIVATE_COMMENT_+wooCommerce.getTxnInfo().get("TID").toString()+" on "+today+". Next charging date :"+nextCycleDateInTID);
    }

    @Step("Update Subscription Amount via callback")
    public void subscriptionAmountUpdateCallback() {
        int updatedCycleAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString())/2;
        String formattedUpdatedCycleAmount = wooCommerce.getSubscriptionPage().formatAmount(updatedCycleAmount);
        wooCommerce.getTxnInfo().put("UpdatedCycleAmount", formattedUpdatedCycleAmount);
        wooCommerce.getCallback().subscriptionAmountUpdate(wooCommerce.getTxnInfo().get("TID").toString(), String.valueOf(updatedCycleAmount));
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_UPDATE_COMMENT_+formattedUpdatedCycleAmount);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_UPDATE_COMMENT_+formattedUpdatedCycleAmount);
    }

    @Step("Update Subscription Cycle date via callback")
    public void subscriptionCycleDateUpdateCallback() {
        var nextCycleDate = TID_Helper.getNextCycleDateInYMD(wooCommerce.getTxnInfo().get("TID").toString());
        String updatedCycleDate = addDaysFromDate(nextCycleDate,5)+" 00:00:00";
        wooCommerce.getCallback().subscriptionUpdate(wooCommerce.getTxnInfo().get("TID").toString(),wooCommerce.getTxnInfo().get("UpdatedCycleAmount").toString().replaceAll("[^0-9]", ""), updatedCycleDate);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(updatedCycleDate);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(updatedCycleDate);
    }

    @Step("Change Subscription payment via callback")
    public void subscriptionChangePaymentCallback() {
        wooCommerce.getCallback().subscriptionChangePayment(wooCommerce.getTxnInfo().get("TID").toString(), DIRECT_DEBIT_SEPA);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), "Direct Debit SEPA");
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPaymentName("Direct Debit SEPA");
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(CHANGE_PAYMENT_COMMENT_);
        String changePaymentComment = wooCommerce.getSubscriptionPage().getOrderNoteComment(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getSubscriptionPage().verifyCustomerNotesComments(changePaymentComment);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifySubscriptionOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), "Direct Debit SEPA");
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription("Direct Debit SEPA");
    }

    @Step("Cancel Subscription payment via callback")
    public void subscriptionCancelCallback() {
        wooCommerce.getCallback().subscriptionCancel(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPageStatus(SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_CANCEL_COMMENT_);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_CANCEL_COMMENT_);
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


}
