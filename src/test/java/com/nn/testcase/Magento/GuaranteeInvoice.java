package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPIs;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.InvoiceGuaranteeCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ThunderBirdEmailHelper;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

public class GuaranteeInvoice extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .myAccountPage(new MyAccountPage())
            .successPage(new SuccessPage())
            .orderPage(new OrderPage())
            .callback(new InvoiceGuaranteeCallbackEvents())
            .myAccountPage(new MyAccountPage())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .build();

    @BeforeClass(alwaysRun = true)
    public void setUpCustomer() {
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
        createCustomer(GUARANTEED_INVOICE);
        updateProductStock(PRODUCT_INVOICE_G);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GUARANTEED_INVOICE,true);
    }

    @AfterClass(alwaysRun = true)
    public void tear(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GUARANTEED_INVOICE,true);
    }

    @AfterMethod(alwaysRun = true)
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 8, description = "Check whether the transaction is successful with payment action set to Capture and full refund via shop backend executed ",retryAnalyzer = RetryListener.class)
    public void firstOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true,
                MIN_ORDER_AMOUNT,"999"
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .fill_DOB_Invoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        refundFullViaShopBackend(orderNumber,orderAmount,REFUND_ORDER_STATUS,GUARANTEED_INVOICE_BOOKBACK);
    }

    @Test(priority = 9, description = "Check whether the authorize order placed successfully and capture via shop backend and partial refund via shop backend executed successfully",retryAnalyzer = RetryListener.class)
    public void secondOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .fill_DOB_Invoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        captureOrder(orderNumber,tid);
        refundPartialViaShopBackend(orderNumber,orderAmount,PROCESSING_ORDER_STATUS,GUARANTEED_INVOICE_BOOKBACK);
    }

    @Test(priority = 10, description = "Check whether the order with authorize minimum amount less than order amount is successful, transaction capture and transaction refund events executed successfully",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"100",
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(true)
                .fill_DOB_Invoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCapture(tid);
        verifyNovalnetComments(orderNumber,CAPTURE_COMMENT_);
        verifyInvoiceCreated(orderNumber,true);
        magentoPage.getCallback().transactionRefund(tid,orderAmount);
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,orderAmount);
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,REFUND_ORDER_STATUS);
    }

    @Test(priority = 11, description = "Check whether the order with authorize minimum amount greater than order amount is successful and payment reminder and transaction refund events executed successfully",retryAnalyzer = RetryListener.class)
    public void fourthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"10000",
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .fill_DOB_Invoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().paymentReminderOne(tid);
        verifyNovalnetComments(orderNumber,REMINDER_ONE_COMMENT_);
        magentoPage.getCallback().paymentReminderTwo(tid);
        verifyNovalnetComments(orderNumber,REMINDER_TWO_COMMENT_);
        magentoPage.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
    }

    @Test(priority = 12, description = "Check whether the authorize order placed successfully and cancel via shop backend",retryAnalyzer = RetryListener.class)
    public void fifthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        String productAmount = getProductPrice(PRODUCT_INVOICE_G);
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,Integer.parseInt(productAmount)+SHIPPING_RATE,
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .fill_DOB_Invoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        cancelOrder(orderNumber,tid);
    }

    @Test(priority = 13, description = "Check whether the authorize order placed successfully and execute transaction cancel event",retryAnalyzer = RetryListener.class)
    public void sixthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .fill_DOB_Invoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCancel(tid);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
    }

    @Test(priority = 14, description = "Check whether the b2b address is set, DOB field is hidden, invoice guarantee order placed with pending status and confirmed via callback transaction update event",retryAnalyzer = RetryListener.class)
    public void seventhOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        //magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getMyAccountPage().load().setB2BBillingAddressPending();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(false)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        verifyOrderStatus(orderNumber,COMPLETION_ORDER_STATUS);
        verifyNovalnetComments(orderNumber,UPDATE_COMMENT_);
    }

    @Test(priority = 15, description = "Check whether the invoice guarantee with b2b is successful and deactivated via callback transaction update event ")
    public void eighthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        //magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getMyAccountPage().load().setB2BBillingAddressPending();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(false)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCancel(tid);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
    }

    @Test(priority = 16, description = "Check whether the invoice guarantee with b2b is pending and update from pending to onhold and onhold to confirm via callback transaction update and capture events ",retryAnalyzer = RetryListener.class)
    public void ninthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        //magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getMyAccountPage().load().setB2BBillingAddressPending();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(false)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        verifyOrderStatus(orderNumber,ONHOLD_ORDER_STATUS);
        magentoPage.getCallback().transactionCapture(tid);
        verifyOrderStatus(orderNumber,COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 17, description = "Check whether the invoice guarantee with b2b is pending and update from pending to onhold and onhold to cancel via callback transaction update and capture events ",retryAnalyzer = RetryListener.class)
    public void tenthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        //magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getMyAccountPage().load().setB2BBillingAddressPending();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(false)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        verifyOrderStatus(orderNumber,ONHOLD_ORDER_STATUS);
        magentoPage.getCallback().transactionCancel(tid);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
    }

    @Test(priority = 18, description = "Check whether the invoice guarantee with b2b is successful with b2b confirm address set, verify tid status, execute transaction refund ",retryAnalyzer = RetryListener.class)
    public void eleventhOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_INVOICE_G,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        magentoPage.getMyAccountPage().load().setB2BBillingAddress();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(false)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().transactionRefund(REFUND_BY_BANK_TRANSFER_EU,tid,orderAmount);
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,orderAmount);
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,REFUND_ORDER_STATUS);
    }

    @Test(priority = 19, description = "Ensure that payments are not allowed to proceed if the date of birth indicates an age below 18 years.")
    public void guaranteeValidation1(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .fill_DOB_Invoice(DOB_LESS_18)
                .clickPlaceOrderBtnforError();
        magentoPage.getCheckoutPage().verifyCheckoutErrorMessage("You need to be at least 18 years old");
    }

    @Test(priority = 20, description = "Verify that when the \"Force Non-Guarantee\" option is enabled, a saved card with a customer is under 18 years old can be used for an order." +
            " The order should proceed successfully as a normal Invoice payment.")
    public void guaranteeValidation2(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .fill_DOB_Invoice(DOB_LESS_18)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString();
        var paymentType = TID_Helper.getTIDPaymentType(tid);
        DriverActions.verifyEquals(paymentType,INVOICE);
    }

    @Test(priority = 21, description = "Verify that guarantee payments are displayed on the checkout page for customers from Germany in a B2C context.")
    public void guaranteeValidation3(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("DE");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(true);
    }

    @Test(priority = 22, description = "Verify that guarantee payments are displayed on the checkout page for customers from Austria in a B2C context.")
    public void guaranteeValidation4(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("AT");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(true);
    }

    @Test(priority = 23, description = "Verify that guarantee payments are displayed on the checkout page for customers from Switzerland in a B2C context.")
    public void guaranteeValidation5(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("CH");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(true);
    }

    @Test(priority = 24, description = "Verify that guarantee payments are not displayed on the checkout page for customers from England in a B2C context.")
    public void guaranteeValidation6(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("GB");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }

    @Test(priority = 25, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2B context.")
    public void guaranteeValidation7(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().setB2BBillingAddress();
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("GB");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(false);
    }

    @Test(priority = 26, description = "Verify that guarantee payments are displayed on the checkout page for customers from Netharland in a B2B context.")
    public void guaranteeValidation8(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().setB2BBillingAddress();
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("NL");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .verifyInvoice_DateOfBirthDisplayed(false);
    }

    @Test(priority = 27, description = "Verify that guarantee payments are not displayed on the checkout page for customers from United States in a B2B context.")
    public void guaranteeValidation9(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("US");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }

    @Test(priority = 28, description = "Verify whether the regular payment options (Invoice or Direct Debit SEPA) are hidden when the \"force non-guarantee\" option is turned off and the necessary guarantee conditions are not fulfilled.")
    public void guaranteeValidation10(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        updateProductPrice(PRODUCT_SEPA,300);
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,false);
        magentoPage.getCheckoutPage().isPaymentDisplayed(INVOICE,false);
    }

    @Test(priority = 29, description = "Verify that guarantee payments are hidden on the checkout page for customers with different billing and shipping")
    public void guaranteeValidation11(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer("DIFF_BILLING_SHIPPING"); // just a customer creation payload with different billing and shipping
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .changeTheShippingAtCheckout()
                .isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }

    //@Test(priority = 30, description = "Verify the order and invoice mail received with novalnet comments",retryAnalyzer = RetryListener.class)
    public void mailVerifyOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true,
                MIN_ORDER_AMOUNT,"999"
        ));
        createCustomer(GUARANTEED_INVOICE,"gopinath_m@novalnetsolutions.com");
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("DE");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .fill_DOB_Invoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getSuccessPage().getOrderNumber(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getSuccessPage().getPaymentComment();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailOrderSubjectText,orderNumber);
        DriverActions.verifyEquals(mailContent.contains(paymentName),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(paymentComments),true,"Verify order mail has novalnet payment comments");
        String invoiceMailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailInvoiceSubjectText,orderNumber);
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentName),true,"Verify invoice mail has payment name");
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentComments),true,"Verify invoice mail has payment comments");
    }

    @Test(priority = 30, description = "Check whether the test transaction is successful with guest user",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,true,
                MIN_ORDER_AMOUNT,"999"
        ));
        navigateGuestCheckout(PRODUCT_GUEST);
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .fill_DOB_Invoice(DOB)
                .clickPlaceOrderBtn();
        String orderNumber = magentoPage.getSuccessPage().getOrderNumberForGuestOrder();
        String tid = NovalnetAPIs.getRecentTransactionTID(orderNumber);
        DriverActions.verifyEquals(TID_Helper.getTIDStatus(tid),TID_STATUS_CONFIRMED,"Verify tid status for guest user");
        magentoPage.getOrderPage().load();
        magentoPage.getOrderPage().searchOrderByOrderNumber(orderNumber);
        DriverActions.verifyEquals(magentoPage.getOrderPage().getOrderNovalnetComments().contains(tid),true,"Verify TID value returned from server");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        magentoPage.getOrderPage().verifyInvoiceCreated(true);
    }

    @Test(priority = 31,dataProvider = "guaranteeInvoiceCurrencies",description = "Verify that the Guarantee Invoice payment is should not displayed in checkout page for USD,CH, PLN currency")
    public void currencyValidation(String currency){
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getMyAccountPage().changeCurrency(currency);
        magentoPage.getCheckoutPage()
                .load()
                .openCheckoutPage()
                .isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }

    @Test(priority = 32,description = " Verify that the order placed successfully with Registered user with country FR for B2B customers")
    public void b2BOrder_FR_Country(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                FORCE_NON_GUARANTEE,false
        ));
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().setB2BBillingAddress();
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("FR");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        //magentoPage.getSuccessPage().verifyDueDate(30);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE,30);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
    }

   // @Test(priority = 32,description = "Check whether the payment logo displayed as per admin portal configurations ",retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogo(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("Invoice",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Invoice",true);
    }

    //@Test(priority = 31, description = "Verify that guarantee invoice payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GUARANTEED_INVOICE,false);
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }

    @DataProvider
    public Object[][] guaranteeInvoiceCurrencies(){
        return new Object[][]{{"PLN"},{"USD"},{"CHF"}};
    }

}
