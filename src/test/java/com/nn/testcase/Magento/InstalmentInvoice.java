package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPIs;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.InstalmentInvoiceCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ThunderBirdEmailHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
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

public class InstalmentInvoice extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .myAccountPage(new MyAccountPage())
            .orderPage(new OrderPage())
            .successPage(new SuccessPage())
            .callback(new InstalmentInvoiceCallbackEvents())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .build();

    @BeforeClass(alwaysRun = true)
    public void setUpGlobalConfiguration() {
        ExtentTestManager.saveToReport("Setup","Setting up the Global configuration and creating new customer.");
        createCustomer(INSTALMENT_INVOICE);
        updateProductStock(PRODUCT_INSTALLMENT_INVOICE);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INSTALMENT_INVOICE,true);
    }

    @AfterClass(alwaysRun = true)
    public void tear(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INSTALMENT_INVOICE,true);
    }

    @AfterMethod(alwaysRun = true)
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 1, description = "Check whether the test transaction is successful with Capture, set allowed cycles, verify allowed instalment cycles displayed at checkout instalment dropdown," +
            " verify instalment object in tid, verify instalment table success page and orders page, execute instalment recurring and refund at shop backend for 1st cycle",retryAnalyzer = RetryListener.class)
    public void firstOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        int[] allowedInstalmentCycles = new int[]{2,3,4,5};
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                MIN_ORDER_AMOUNT,"1998"
        ),allowedInstalmentCycles);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifySelectedInstalmentCyclesDisplayedAtCheckout(allowedInstalmentCycles)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed().verifyInstalmentTableDisplayed(true);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, INSTALMENT_INVOICE);
        TID_Helper.verifyInstalmentValuesInTID(tid,"3",cycleAmount);
        magentoPage.getSuccessPage().verifyInstalmentTable(cycleAmount,3);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getOrderPage().verifyInstalmentTable(tid,3,cycleAmount);
        magentoPage.getCallback().instalment(tid,"2","1", DriverActions.getUpcomingMonthDate(2));
        instalmentRecurringVerification(cycleAmount,2,1,DriverActions.getUpcomingMonthDate(2));
        magentoPage.getCallback().instalment(tid,"3","0");
        instalmentRecurringVerification(cycleAmount,3,0,"");
        magentoPage.getOrderPage().proceedInstalmentRefund(cycleAmount,INSTALMENT_INVOICE_BOOKBACK);
    }

    @Test(priority = 2, description = "Check whether the order placed successfully with authorize, verify tid status, verify instalment object exist in tid, verify instalment table displayed," +
            " capture via shop backend and partial refund via shop backend",retryAnalyzer = RetryListener.class)
    public void secondOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                ALLOW_B2B,true
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_ON_HOLD, INSTALMENT_INVOICE);
        //TID_Helper.verifyBankDetails(tid, magentoPage.getSuccessPage().getSuccessPageBankDetails(magentoPage.getTxnInfo()));
        TID_Helper.verifyInstalmentCycleDatesExist(tid,false);
        magentoPage.getSuccessPage().verifyInstalmentTableDisplayed(false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        captureOrder(orderNumber,tid);
        refundPartialViaShopBackend(orderNumber,cycleAmount,PROCESSING_ORDER_STATUS,INSTALMENT_INVOICE_BOOKBACK);
    }

    @Test(priority = 3, description = "Check whether the order with authorize minimum amount less than order amount, allow b2b disabled, set company name in address, verify DOB field displayed," +
            " verify tid status is on hold, execute transaction capture and transaction refund event",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"100",
                ALLOW_B2B,false
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(true)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_ON_HOLD, INSTALMENT_INVOICE);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCapture(tid,DriverActions.getUpcomingMonthDatesInArr(3),"1","2",DriverActions.getUpcomingMonthDate(2));
        verifyNovalnetComments(orderNumber,CAPTURE_COMMENT_);
        verifyInvoiceCreated(orderNumber,true);
        magentoPage.getCallback().transactionRefund(tid,cycleAmount);
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,cycleAmount);
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
    }

    @Test(priority = 4, description = "Check whether the test order with authorize minimum amount greater than order amount is successful, verify tid status is confirmed, execute reminder and refund callback events",retryAnalyzer = RetryListener.class)
    public void fourthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"10000",
                ALLOW_B2B,false
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, INSTALMENT_INVOICE);
        magentoPage.getSuccessPage().verifyInstalmentTable(cycleAmount,3);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().paymentReminderOne(tid);
        verifyNovalnetComments(orderNumber,REMINDER_ONE_COMMENT_);
        magentoPage.getCallback().paymentReminderTwo(tid);
        verifyNovalnetComments(orderNumber,REMINDER_TWO_COMMENT_);
        magentoPage.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(cycleAmount)/2));
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(cycleAmount)/2));
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
    }

    @Test(priority = 5, description = "Check whether the order placed successfully with authorize order amount and auth amount equal, verify tid status, verify instalment object exist in tid, verify instalment table displayed," +
            " cancel via shop backend",retryAnalyzer = RetryListener.class)
    public void fifthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        String productAmount = getProductPrice(PRODUCT_INSTALLMENT_INVOICE);
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,Integer.parseInt(productAmount)+SHIPPING_RATE, // because amount received as euro converting into cents
                ALLOW_B2B,false
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_ON_HOLD, INSTALMENT_INVOICE);
        TID_Helper.verifyInstalmentCycleDatesExist(tid,false);
        magentoPage.getSuccessPage().verifyInstalmentTableDisplayed(false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        cancelOrder(orderNumber,tid);
    }

    @Test(priority = 6, description = "Check whether the order placed successfully with authorize, verify tid status, verify instalment object exist in tid, verify instalemnt table displayed," +
            " cancel via callback",retryAnalyzer = RetryListener.class)
    public void sixthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                ALLOW_B2B,false
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_ON_HOLD, INSTALMENT_INVOICE);
        TID_Helper.verifyInstalmentCycleDatesExist(tid,false);
        magentoPage.getSuccessPage().verifyInstalmentTableDisplayed(false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCancel(tid);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
    }

    @Test(priority = 7, description = "Check whether the B2B pending to confirm test transaction is successful, b2b address set, verify dob field is hidden, verify tid status is pending, execute transaction update event",retryAnalyzer = RetryListener.class)
    public void seventhOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        //magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getMyAccountPage().load().setB2BBillingAddressPending();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(false)
                .selectInstalmentInvoiceCycle(3)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_PENDING, INSTALMENT_INVOICE);
        TID_Helper.verifyInstalmentCycleDatesExist(tid,false);
        magentoPage.getSuccessPage().verifyInstalmentTableDisplayed(false);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED,DriverActions.getUpcomingMonthDatesInArr(3),"1","2",DriverActions.getUpcomingMonthDate(2));
        verifyOrderStatus(orderNumber,COMPLETION_ORDER_STATUS);
        verifyNovalnetComments(orderNumber,UPDATE_COMMENT_);
    }

    @Test(priority = 8, description = "Check whether the B2B pending to cancel test transaction is successful, b2b address set, verify dob field is hidden, verify tid status is pending, execute transaction cancel event",retryAnalyzer = RetryListener.class)
    public void eighthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        //magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getMyAccountPage().load().setB2BBillingAddressPending();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(false)
                .selectInstalmentInvoiceCycle(3)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_PENDING, INSTALMENT_INVOICE);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCancel(tid);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
    }

    @Test(priority = 9, description = "Check whether the B2B pending to on hold to confirm test transaction is successful, b2b address set, verify dob field is hidden, verify tid status is pending, execute transaction update," +
            " transaction capture and instalment cancel  event",retryAnalyzer = RetryListener.class)
    public void ninthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        //magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getMyAccountPage().load().setB2BBillingAddressPending();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(false)
                .selectInstalmentInvoiceCycle(3)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_PENDING, INSTALMENT_INVOICE);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        verifyOrderStatus(orderNumber,ONHOLD_ORDER_STATUS);
        magentoPage.getCallback().transactionCapture(tid,DriverActions.getUpcomingMonthDatesInArr(3),"1","2",DriverActions.getUpcomingMonthDate(2));
        verifyOrderStatus(orderNumber,COMPLETION_ORDER_STATUS);
        magentoPage.getCallback().instalmentCancel(tid);
        verifyNovalnetComments(orderNumber,INSTALMENT_CANCEL_COMMENT);
    }

    @Test(priority = 10, description = "Check whether the B2B pending to on hold to cancel test transaction is successful, b2b address set, verify dob field is hidden, verify tid status is pending, execute transaction update," +
            " transaction cancel",retryAnalyzer = RetryListener.class)
    public void tenthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        //magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getMyAccountPage().load().setB2BBillingAddressPending();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(false)
                .selectInstalmentInvoiceCycle(3)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/3);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_PENDING, INSTALMENT_INVOICE);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        verifyOrderStatus(orderNumber,ONHOLD_ORDER_STATUS);
        magentoPage.getCallback().transactionCancel(tid);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
    }

    @Test(priority = 11, description = "Check whether the B2B confirm test transaction is successful, b2b confirm address is set, verify date of birth displayed, verify tid status confirmed, " +
            " verify instalment table displayed at success and orders page, instalment cancel via shop backend",retryAnalyzer = RetryListener.class)
    public void eleventhOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true
        ));
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        magentoPage.getMyAccountPage().load().setB2BBillingAddress();
        magentoPage.getCheckoutPage().load().openCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(false)
                .selectInstalmentInvoiceCycle(4)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/4);
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, INSTALMENT_INVOICE);
        magentoPage.getSuccessPage().verifyInstalmentTable(cycleAmount,4);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getOrderPage().verifyInstalmentTable(tid,4,cycleAmount);
        magentoPage.getOrderPage().instalmentCancel(cycleAmount,INSTALMENT_INVOICE_BOOKBACK);
    }

    @Test(priority = 12, description = "Ensure that payments are not allowed to proceed if the date of birth indicates an age below 18 years.")
    public void guaranteeValidation1(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB_LESS_18)
                .clickPlaceOrderBtnforError();
        magentoPage.getCheckoutPage().verifyCheckoutErrorMessage("You need to be at least 18 years old");
    }

    @Test(priority = 13, description = "Verify that guarantee payments are displayed on the checkout page for customers from Germany in a B2C context.")
    public void guaranteeValidation2(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("DE");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(true);
    }

    @Test(priority = 14, description = "Verify that guarantee payments are displayed on the checkout page for customers from Austria in a B2C context.")
    public void guaranteeValidation3(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("AT");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(true);
    }
    @Test(priority = 15, description = "Verify that guarantee payments are displayed on the checkout page for customers from Switzerland in a B2C context.")
    public void guaranteeValidation4(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("CH");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(true);
    }

    @Test(priority = 16, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2C context.")
    public void guaranteeValidation5(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("GB");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

    @Test(priority = 17, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2B context.")
    public void guaranteeValidation6(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().setB2BBillingAddress();
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("GB");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(false);
    }

    @Test(priority = 18, description = "Verify that guarantee payments are displayed on the checkout page for customers from Netherland in a B2B context.")
    public void guaranteeValidation7(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().setB2BBillingAddress();
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("NL");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifyInstalmentInvoiceDateOfBirthDisplayed(false);
    }


    @Test(priority = 19, description = "Verify that guarantee payments are displayed on the checkout page for customers from United States in a B2B context.")
    public void guaranteeValidation8(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().setB2BBillingAddress();
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("US");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(INSTALMENT_INVOICE,false);

    }

    @Test(priority = 19, description = "Verify that guarantee payments are hidden on the checkout page for customers when amount is less 1998 EUR")
    public void guaranteeValidation9(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry("DE");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

    @Test(priority = 20, description = "Verify that guarantee payments are hidden on the checkout page for customers when currency is not EUR")
    public void guaranteeValidation10(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage()
                .load()
                .changeCurrency("USD");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

    @Test(priority = 21, description = "Verify that guarantee payments are hidden on the checkout page for customers with different billing and shipping")
    public void guaranteeValidation11(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false
        ));
        createCustomer("DIFF_BILLING_SHIPPING");
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .changeTheShippingAtCheckout()
                .isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

    //@Test(priority = 22, description = "Verify the order and invoice mail received with novalnet comments",retryAnalyzer = RetryListener.class)
    public void mailVerifyOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        int[] allowedInstalmentCycles = new int[]{2,3,4,5};
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                MIN_ORDER_AMOUNT,"1998"
        ),allowedInstalmentCycles);
        createCustomer(INSTALMENT_INVOICE,"gopinath_m@novalnetsolutions.com");
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("DE");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .verifySelectedInstalmentCyclesDisplayedAtCheckout(allowedInstalmentCycles)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed().verifyInstalmentTableDisplayed(true);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getSuccessPage().getOrderNumber(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                paymentComments = magentoPage.getSuccessPage().getPaymentComment();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, INSTALMENT_INVOICE);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailOrderSubjectText,orderNumber);
        DriverActions.verifyEquals(mailContent.contains(paymentName),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(paymentComments),true,"Verify order mail has novalnet payment comments");
        String invoiceMailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailInvoiceSubjectText,orderNumber);
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentName),true,"Verify invoice mail has payment name");
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentComments),true,"Verify invoice mail has payment comments");
    }

    @Test(priority = 22, description = "Check whether the test transaction is successful with guest user",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        int[] allowedInstalmentCycles = new int[]{2,3,4,5};
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,true,
                MIN_ORDER_AMOUNT,"1998"
        ),allowedInstalmentCycles);
        navigateGuestCheckout(PRODUCT_GUEST);
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB)
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
   // @Test(priority = 24,description = "Check whether the payment logo displayed as per admin portal configurations ",retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogo(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("Instalment by invoice",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Instalment by invoice",true);
    }

    //@Test(priority = 23, description = "Verify that instalment invoice payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INSTALMENT_INVOICE,false);
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

}
