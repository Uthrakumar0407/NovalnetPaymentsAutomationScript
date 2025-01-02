package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPI_Helper;
import com.nn.apis.MagentoAPIs;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.InvoiceCallbackEvents;
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
import static com.nn.apis.MagentoAPI_Helper.verifyNovalnetComments;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.CREDIT_COMMENT_;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.PAYMENT_ACTION;

public class Invoice extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .orderPage(new OrderPage())
            .successPage(new SuccessPage())
            .myAccountPage(new MyAccountPage())
            .callback(new InvoiceCallbackEvents())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    public void setUpCustomer() {
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
        createCustomer(INVOICE);
        updateProductStock(PRODUCT_INVOICE);
        updateProductPrice(PRODUCT_INVOICE,300);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INVOICE,true);
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                FORCE_NON_GUARANTEE,true
        ));
    }

    @AfterClass(alwaysRun = true)
    public void logout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INVOICE,true);
    }
    @AfterMethod(alwaysRun = true)
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 1, description = "Check whether the capture transaction is successful with payment action set to Capture and partial refund via shop backend executed ",retryAnalyzer = RetryListener.class)
    public void firstOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                DUE_DATE,""
        ));
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        //.verifyDueDate(14);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE,14);

        statusCommentsVerification(orderNumber,PROCESSING_ORDER_STATUS,true,paymentComments,paymentName);
        var updatedDueDate = DriverActions.addDaysFromDate(TID_Helper.getDueDate(tid), 10);
        magentoPage.getCallback().transactionUpdateDueDate(tid,updatedDueDate);
        //commented for now until we make verifyNovalnetComments reliable
        //magentoPage.getOrderPage().verifyNovalnetComments(UPDATE_COMMENT_1,orderAmount,updatedDueDate);
        refundInvoiceTypeShopBackend(orderNumber,String.valueOf(Integer.parseInt(orderAmount)/2),PROCESSING_ORDER_STATUS,tid);
    }

    @Test(priority = 2, description = "Check whether the authorize order placed successfully and capture and full refund via shop backend",retryAnalyzer = RetryListener.class)
    public void secondOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                DUE_DATE,10
        ));
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        //.verifyDueDate(10);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE,10);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        captureOrder(orderNumber,tid);
        magentoPage.getCallback().invoiceCredit(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
        magentoPage.getCallback().invoiceCredit(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_);
        verifyOrderStatus(orderNumber,COMPLETION_ORDER_STATUS);
        refundInvoiceTypeShopBackend(orderNumber,orderAmount,REFUND_ORDER_STATUS,tid);
    }

    @Test(priority = 3, description = "Check whether the authorize order with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100 and followup callback events transaction capture credit and refund executed successfully",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"100"
        ));
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCapture(tid);
        verifyNovalnetComments(orderNumber,CAPTURE_COMMENT_);
        verifyInvoiceCreated(orderNumber,true);
        magentoPage.getCallback().invoiceCredit(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        magentoPage.getCallback().debtCollectionDE(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        magentoPage.getCallback().bankTransferByEndCustomer(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        magentoPage.getCallback().transactionRefund(tid,orderAmount);
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,orderAmount);
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,REFUND_ORDER_STATUS);
        DriverActions.reloadPage();
    }

    @Test(priority = 4, description = "Check whether the authorize order with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 10000 ,follow up callback events transaction update , payment reminder and credit events executed successfully")
    public void fourthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"10000",
                DUE_DATE,7
        ));
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        //.verifyDueDate(7);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE,7);
        statusCommentsVerification(orderNumber,PROCESSING_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().transactionUpdateAmount(tid,String.valueOf(Integer.parseInt(orderAmount)*2));
        magentoPage.getCallback().paymentReminderOne(tid);
        verifyNovalnetComments(orderNumber,REMINDER_ONE_COMMENT_);
        magentoPage.getCallback().paymentReminderTwo(tid);
        verifyNovalnetComments(orderNumber,REMINDER_TWO_COMMENT_);
        magentoPage.getCallback().submissionToCollection(tid);
        verifyNovalnetComments(orderNumber,COLLECTION_COMMENT_);
        magentoPage.getCallback().invoiceCredit(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        verifyOrderStatus(orderNumber,COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 5, description = "Check whether the authorize order is cancelled via shop backend with payment action set to Authorize",retryAnalyzer = RetryListener.class)
    public void fifthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,""
        ));
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        cancelOrder(orderNumber,tid);
    }


    @Test(priority = 6, description = "Check whether the authorize order is cancelled via callback with payment action set to Authorize",retryAnalyzer = RetryListener.class)
    public void sixthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,""
        ));
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCancel(tid);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyInvoiceCreated(orderNumber,false);
    }

    //@Test(priority = 7, description = "Verify novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                DUE_DATE,""
        ));
        createCustomer(INVOICE,"gopinath_m@novalnetsolutions.com");
        addProductToCart(PRODUCT_INVOICE,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("DE");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getSuccessPage().getOrderNumber(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INVOICE),
                paymentComments = magentoPage.getSuccessPage().getPaymentComment();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE,14);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailOrderSubjectText,orderNumber);
        DriverActions.verifyEquals(mailContent.contains(paymentName),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(paymentComments),true,"Verify order mail has novalnet payment comments");
        magentoPage.getCallback().invoiceCredit(tid,orderAmount);
        DriverActions.reloadPage();
        String invoiceMailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailInvoiceSubjectText,orderNumber);
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentName),true,"Verify invoice mail has payment name");
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentComments),true,"Verify invoice mail has novalnet payment comments");
    }

    @Test(priority = 7, description = "Check whether the test transaction is successful with guest user",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                DUE_DATE,""
        ));
        navigateGuestCheckout(PRODUCT_GUEST_3,"DE");
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(INVOICE).clickPlaceOrderBtn();
        String orderNumber = magentoPage.getSuccessPage().getOrderNumberForGuestOrder();
        String tid = NovalnetAPIs.getRecentTransactionTID(orderNumber);
        DriverActions.verifyEquals(TID_Helper.getTIDStatus(tid),TID_STATUS_PENDING,"Verify tid status for guest user");
        magentoPage.getOrderPage().load();
        magentoPage.getOrderPage().searchOrderByOrderNumber(orderNumber);
        DriverActions.verifyEquals(magentoPage.getOrderPage().getOrderNovalnetComments().contains(tid),true,"Verify TID value returned from server");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        magentoPage.getOrderPage().verifyInvoiceCreated(true);
    }

    //@Test(priority = 9,description = "Check whether the payment logo displayed as per admin portal configurations ",retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogo(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        createCustomer(INVOICE);
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("Invoice",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Invoice",true);
    }

    @Test(priority = 8, description = "Verify download product displayed inside my download products and download link displayed when the transaction is confirmed")
    public void downloadProductConfirmOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                DUE_DATE,14
        ));
        createCustomer(INVOICE);
        magentoPage.getShopUserLoginPage().load();
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",5.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(INVOICE) // Added username field cause name on card field is empty for download product which is an issue
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        //.verifyDueDate(14);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE,14);
        magentoPage.getMyAccountPage().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),true,"Verify download link displayed in my download products menu");
    }

    @Test(priority = 9, description = "Verify download product is displayed and download link is not displayed inside my download products when the transaction is on hold, capture transaction, verify download product displayed and download link displayed")
    public void downloadProductOnHoldToConfirmOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE
        ));
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",25.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        magentoPage.getMyAccountPage().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
        captureOrder(orderNumber,tid);
        magentoPage.getMyAccountPage().load().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),true,"Verify download link displayed in my download products menu");
    }

    @Test(priority = 10, description = "Verify download product is displayed and download link is not displayed inside my download products when the transaction is on hold, cancel transaction, verify download product is not displayed and download link is not displayed")
    public void downloadProductOnHoldToCancelOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE
        ));
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",25.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        magentoPage.getMyAccountPage().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
        cancelOrder(orderNumber,tid);
        magentoPage.getMyAccountPage().load().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
    }

    @Test(priority = 11,description = "Validate the due date as per admin portal configuration")
    public void dueDateValidation(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                DUE_DATE,"25"
        ));
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
       // .verifyDueDate(25);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE,25);
        statusCommentsVerification(orderNumber,PROCESSING_ORDER_STATUS,true,paymentComments,paymentName);
    }


    //@Test(priority = 11, description = "Verify that invoice payment is hidden on the checkout page when payment is disabled in admin portal")

    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INVOICE,false);
        createCustomer(INVOICE);
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(INVOICE,false);

    }

}
