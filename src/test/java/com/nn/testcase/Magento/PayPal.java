package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPI_Helper;
import com.nn.apis.MagentoAPIs;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.PayPalCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ThunderBirdEmailHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

public class PayPal extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .orderPage(new OrderPage())
            .myAccountPage(new MyAccountPage())
            .callback(new PayPalCallbackEvents())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .build();


    @BeforeClass(alwaysRun = true)
    public void setUpCustomer() {
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
        createCustomer(PAYPAL);
        updateProductStock(PRODUCT_PAYPAL_PAY);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PAYPAL,true);
    }

    @AfterClass
    public void tear(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PAYPAL,true);
    }

    @AfterMethod(alwaysRun = true)
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 1, description = "Check whether the test transaction is successful with Capture and verify tid status pending, execute transaction update event pending to confirm," +
            " partial refund, chargeback, credit events executed ",retryAnalyzer = RetryListener.class)
    public void firstOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE
        ));
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(PAYPAL),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, PAYPAL);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        verifyNovalnetComments(orderNumber,UPDATE_COMMENT_,orderAmount);
        magentoPage.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2));
        magentoPage.getCallback().chargeback(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CHARGEBACK_COMMENT_,orderAmount);
        magentoPage.getCallback().creditEntryDE(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);

    }

    @Test(priority = 2, description = "Check whether the test order is successful with Authorize, tid status on hold, capture shop backend, execute reminder and collection," +
            " and transaction Full Refund via callback",retryAnalyzer = RetryListener.class)
    public void secondOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,""

        ));
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
         magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(PAYPAL),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, PAYPAL);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        captureOrder(orderNumber,tid);
        magentoPage.getCallback().paymentReminderOne(tid);
        verifyNovalnetComments(orderNumber,REMINDER_ONE_COMMENT_);
        magentoPage.getCallback().paymentReminderTwo(tid);
        verifyNovalnetComments(orderNumber,REMINDER_TWO_COMMENT_);
        magentoPage.getCallback().submissionToCollection(tid);
        verifyNovalnetComments(orderNumber,COLLECTION_COMMENT_);
        magentoPage.getCallback().transactionRefund(tid,orderAmount);
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,orderAmount);
    }

    @Test(priority = 3, description = "Check whether the test order with authorize minimum amount less than order amount, tid status on hold, execute transaction capture and transaction refund",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"100"
           ));
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(PAYPAL),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, PAYPAL);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCapture(tid);
        verifyNovalnetComments(orderNumber,CAPTURE_COMMENT_);
        verifyInvoiceCreated(orderNumber,true);
        magentoPage.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
    }

    @Test(priority = 4, description = "Check whether the test order with authorize minimum amount greater than order amount & tid status pending and execute transaction update pending to deactivated",retryAnalyzer = RetryListener.class)
    public void fourthOrder() {
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "10000"

        ));
        addProductToCart(PRODUCT_PAYPAL_PAY, 1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(PAYPAL),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, PAYPAL);
        statusCommentsVerification(orderNumber, PENDING_ORDER_STATUS, false, paymentComments, paymentName);
        magentoPage.getCallback().transactionCancel(tid);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
    }

    @Test(priority = 5, description = "Check whether the authorize test order is cancelled via shop backend",retryAnalyzer = RetryListener.class)
    public void fifthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        String productAmount = getProductPrice(PRODUCT_PAYPAL_PAY);
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,Integer.parseInt(productAmount)+SHIPPING_RATE
                ));
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(PAYPAL),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, PAYPAL);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        cancelOrder(orderNumber,tid);
    }

    @Test(priority = 6, description = "Check whether the authorize test order is cancelled via callback",retryAnalyzer = RetryListener.class)
    public void sixthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE

        ));
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(PAYPAL),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, PAYPAL);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCancel(tid);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyInvoiceCreated(orderNumber,false);
    }

    @Test(priority = 7, description = "Check whether the test order is successful by communication break",retryAnalyzer = RetryListener.class)
    public void seventhOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE

        ));
        createCustomer(PAYPAL);
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().waitForPayPalRedirection();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = magentoPage.getNovalnetAdminPortal().getTID(MagentoAPIs.getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        magentoPage.getCallback().communicationBreakSuccess(tid,"");
        statusCommentsVerificationAfterCommunicationBreak(tid,orderNumber,COMPLETION_ORDER_STATUS,true);
    }

    @Test(priority = 8, description = "Check whether the test order is placed & failed by communication break",retryAnalyzer = RetryListener.class)
    public void eighthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE

        ));
        createCustomer(PAYPAL);
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().waitForPayPalRedirection();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = magentoPage.getNovalnetAdminPortal().getTID(MagentoAPIs.getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        magentoPage.getCallback().communicationBreakFailure(tid,"");
        statusCommentsVerificationAfterCommunicationBreak(tid,orderNumber,CANCELLATION_ORDER_STATUS,false);
    }

    @Test(priority = 9, description = "Verify the display of error text on the checkout page in case a transaction is aborted, leading to a change in the order status to \"failed\".",retryAnalyzer = RetryListener.class)
    public void ninthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE
        ));
        createCustomer(PAYPAL);
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().cancelAtPayPalRedirection();
        magentoPage.getCheckoutPage().verifyCheckoutErrorMessage(PAYPAL_END_USER_CANCEL_ERROR);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = magentoPage.getNovalnetAdminPortal().getTID(MagentoAPIs.getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_FAILURE, PAYPAL);
        statusCommentsVerificationAfterCommunicationBreak(tid,orderNumber,CANCELLATION_ORDER_STATUS,false);
    }

    @Test(priority = 10, description = "Check whether the test transaction is successful with Capture and verify tid status confirmed, proceed full refund shop backend",retryAnalyzer = RetryListener.class)
    public void tenthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE
        ));
        createPaypalCustomer();
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(PAYPAL),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, PAYPAL);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        refundFullViaShopBackend(orderNumber,orderAmount,REFUND_ORDER_STATUS,PAYPAL_BOOKBACK);
    }

    //@Test(priority = 11, description = "Verify the order and invoice mail received with novalnet comments",retryAnalyzer = RetryListener.class)
    public void mailVerifyOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE
        ));
        createCustomer(PAYPAL,"gopinath_m@novalnetsolutions.com");
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("DE");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getSuccessPage().getOrderNumber(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(PAYPAL),
                paymentComments = magentoPage.getSuccessPage().getPaymentComment();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, PAYPAL);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailOrderSubjectText,orderNumber);
        DriverActions.verifyEquals(mailContent.contains(paymentName),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(paymentComments),true,"Verify order mail has novalnet payment comments");
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        DriverActions.reloadPage();
        String invoiceMailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailInvoiceSubjectText,orderNumber);
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentName),true,"Verify invoice mail has payment name");
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentComments),true,"Verify invoice mail has novalnet payment comments");
    }

    @Test(priority = 11, description = "Check whether the test transaction is successful with guest user",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE
        ));
        navigateGuestCheckout(PRODUCT_GUEST);
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        String orderNumber = magentoPage.getSuccessPage().getOrderNumberForGuestOrder();
        String tid = NovalnetAPIs.getRecentTransactionTID(orderNumber);
        DriverActions.verifyEquals(TID_Helper.getTIDStatus(tid),TID_STATUS_PENDING,"Verify tid status for guest user");
        magentoPage.getOrderPage().load();
        magentoPage.getOrderPage().searchOrderByOrderNumber(orderNumber);
        DriverActions.verifyEquals(magentoPage.getOrderPage().getOrderNovalnetComments().contains(tid),true,"Verify TID value returned from server");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(PENDING_ORDER_STATUS);
        magentoPage.getOrderPage().verifyInvoiceCreated(false);
    }

    @Test(priority = 12, description = "Verify download product displayed inside my download products and download link displayed when the transaction is confirmed")
    public void downloadProductConfirmOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                PAYMENT_ACTION,CAPTURE
        ));
        createCustomer(PAYPAL);
        magentoPage.getShopUserLoginPage().load();
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",20.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, PAYPAL);
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        magentoPage.getMyAccountPage().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),true,"Verify download link displayed in my download products menu");
    }

    @Test(priority = 13, description = "Verify download product is displayed and download link is not displayed inside my download products when the transaction is on hold, capture transaction, verify download product displayed and download link displayed")
    public void downloadProductOnHoldToConfirmOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,""
        ));
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",25.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, PAYPAL); //After this downloadProductConfirmOrder fix we have to revert back this comment
//        magentoPage.getMyAccountPage().openMyDownloadProducts();
//        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
//        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
        captureOrder(orderNumber,tid);
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        magentoPage.getMyAccountPage().load().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),true,"Verify download link displayed in my download products menu");
    }

    @Test(priority = 14, description = "Verify download product is displayed and download link is not displayed inside my download products when the transaction is on hold, cancel transaction, verify download product is not displayed and download link is not displayed")
    public void downloadProductOnHoldToCancelOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,""
        ));
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",25.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(PAYPAL)
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().placeOrderWithPayPal();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, PAYPAL);//After this downloadProductConfirmOrder fix we have to revert back this comment
//        magentoPage.getMyAccountPage().openMyDownloadProducts();
//        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
//        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
        cancelOrder(orderNumber,tid);
        magentoPage.getMyAccountPage().load().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),false,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
    }



   // @Test(priority = 16,description = "Check whether the payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogo(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        createPaypalCustomer();
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("PayPal",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("PayPal",true);
    }
    //@Test(priority = 15, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PAYPAL,false);
        createCustomer(PAYPAL);
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(PAYPAL,false);
    }

}
