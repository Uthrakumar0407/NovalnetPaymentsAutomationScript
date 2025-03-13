package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPI_Helper;
import com.nn.apis.MagentoAPIs;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.DirectDebitSEPACallbackEvents;
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
import static com.nn.apis.MagentoAPI_Helper.verifyNovalnetComments;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.CREDIT_COMMENT_;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

public class DirectDebitSEPA extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            //git test
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .orderPage(new OrderPage())
            .callback(new DirectDebitSEPACallbackEvents())
            .myAccountPage(new MyAccountPage())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .build();


    @BeforeClass(alwaysRun = true)
    public void setUpCustomerWithGuaranteePaymentConfiguration() {
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
        createCustomer(DIRECT_DEBIT_SEPA);
        updateProductStock(PRODUCT_SEPA);
        updateProductPrice(PRODUCT_SEPA,300);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(DIRECT_DEBIT_SEPA,true);
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                FORCE_NON_GUARANTEE,true
        ));
    }

    @AfterClass(alwaysRun = true)
    public void logout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(DIRECT_DEBIT_SEPA,true);
    }

    @AfterMethod(alwaysRun = true)
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 1, description = "Check whether the test transaction is successful with payment action set to Capture, verify token exist in response, partial refund shop backend," +
            " execute chargeback and execute credit events "/*,retryAnalyzer = RetryListener.class*/)
    public void firstOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                DUE_DATE,""
        ));
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                .clickPlaceOrderBtnForSepa();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA,0);
        TID_Helper.verifyPaymentTokenExist(tid);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        refundInvoiceTypeShopBackend(orderNumber,String.valueOf(Integer.parseInt(orderAmount)/2),PROCESSING_ORDER_STATUS,tid);
        magentoPage.getCallback().chargeback(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CHARGEBACK_COMMENT_,orderAmount);
        magentoPage.getCallback().creditEntrySepa(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        magentoPage.getCallback().debtCollectionSepa(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        magentoPage.getCallback().bankTransferByEndCustomer(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
    }

    @Test(priority = 2, description = "Check whether the test order placed successfully with payment action set to Capture , verify masked card displayed, capture order in shop backend" +
            ", full refund via shop backend"/*,retryAnalyzer = RetryListener.class*/)
    public void secondOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                DUE_DATE,"5"
        ));
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .verifyMaskedSEPAData(magentoPage.getTestData().get("IBANDE"))
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                .clickPlaceOrderBtnForSepa();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA,5,0);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        captureOrder(orderNumber,tid);
        refundInvoiceTypeShopBackend(orderNumber,orderAmount,REFUND_ORDER_STATUS,tid);
    }

    @Test(priority = 3, description = "Check whether the order with authorize minimum amount less than order amount is successful, using new card instead of token, execute transaction capture event," +
            " execute transaction refund event ",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"100"
        ));
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                .clickPlaceOrderBtnForSepa();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCapture(tid);
        verifyNovalnetComments(orderNumber,CAPTURE_COMMENT_);
        verifyInvoiceCreated(orderNumber,true);
        magentoPage.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
    }


    @Test(priority = 4, description = "Check whether the authorize order with minimum amount greater than order amount is successful, using iban from AT, execute payment reminder and collection event",retryAnalyzer = RetryListener.class)
    public void fourthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"10000"
        ));
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANAT"))
                .clickPlaceOrderBtnForSepa();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().paymentReminderOne(tid);
        verifyNovalnetComments(orderNumber,REMINDER_ONE_COMMENT_);
        magentoPage.getCallback().paymentReminderTwo(tid);
        verifyNovalnetComments(orderNumber,REMINDER_TWO_COMMENT_);
        magentoPage.getCallback().submissionToCollection(tid);
        verifyNovalnetComments(orderNumber,COLLECTION_COMMENT_);
    }

    @Test(priority = 5, description = "Check whether the authorize order is placed successfully, verify new iban updated in masked data and cancelled via shop backend "/*,retryAnalyzer = RetryListener.class*/)
    public void fifthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        String productAmount = getProductPrice(PRODUCT_SEPA);
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,Integer.parseInt(productAmount)+SHIPPING_RATE
        ));
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .verifyMaskedSEPAData(magentoPage.getTestData().get("IBANAT"))
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                .clickPlaceOrderBtnForSepa();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        cancelOrder(orderNumber,tid);
    }


    @Test(priority = 6, description = "Check whether the authorize order is placed successfully and cancelled via callback ",retryAnalyzer = RetryListener.class)
    public void sixthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,""
        ));
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                .clickPlaceOrderBtnForSepa();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCancel(tid);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyInvoiceCreated(orderNumber,false);
    }

    @Test(priority = 7, description = "Check whether the zero amount order is placed successfully with authorize zero amount config, book the transaction, verify the booked transaction"/*,retryAnalyzer = RetryListener.class*/)
    public void seventhOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION,AUTHORIZE_ZERO_AMOUNT,
                MIN_AUTH_AMOUNT,""
        ));
        createCustomer(DIRECT_DEBIT_SEPA);
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                .clickPlaceOrderBtnForSepa();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        bookAndVerifyZeroAmountBooking(orderAmount,DIRECT_DEBIT_SEPA);
    }

    @Test(priority = 8, dependsOnMethods = "seventhOrder", description = "Check whether the zero amount order is placed successfully with authorize zero amount config, " +
            "verify masked card data not displayed, book the transaction, verify the booked transaction"/*,retryAnalyzer = RetryListener.class*/)
    public void eighthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION,AUTHORIZE_ZERO_AMOUNT,
                MIN_AUTH_AMOUNT,""
        ));
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .verifyMaskedSEPADataDisplayed(true)
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANAT"))
                .clickPlaceOrderBtnForSepa();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        bookAndVerifyZeroAmountBooking(orderAmount,DIRECT_DEBIT_SEPA);
    }

   /* @Test(priority = 9, description = "Verify the order and invoice mail received with novalnet comments")*/
    public void mailVerifyOrder(){
        magentoPage.getShopUserLoginPage().logout();
      /*  magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE
        ));*/
        createCustomer(DIRECT_DEBIT_SEPA,"test@novalnetsolutions.com");
        addProductToCart(PRODUCT_SEPA,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("DE");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .ifTokenDisplayedclickNewCardSEPA()
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                .clickPlaceOrderBtnForSepa();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getSuccessPage().getOrderNumber(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getSuccessPage().getPaymentComment();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        magentoPage.getShopUserLoginPage().logout();
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailOrderSubjectText,orderNumber);
        DriverActions.verifyEquals(mailContent.contains(paymentName),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(paymentComments),true,"Verify order mail has novalnet payment comments");
        String invoiceMailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailInvoiceSubjectText,orderNumber);
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentName),true,"Verify invoice mail has payment name");
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentComments),true,"Verify invoice mail has payment comments");
    }

    @Test(priority = 9, description = "Check whether the test transaction is successful with guest user"/*,retryAnalyzer = RetryListener.class*/)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE
        ));
        navigateGuestCheckout(PRODUCT_GUEST_2);
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .ifTokenDisplayedclickNewCardSEPA()
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
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
    //@Test(priority = 11,description = "Check whether the payment logo displayed as per admin portal configurations ",retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogo(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        createCustomer(DIRECT_DEBIT_SEPA);
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("Direct Debit SEPA",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Direct Debit SEPA",true);
    }


    /*@Test(priority = 10, description = "Verify download product displayed inside my download products and download link displayed when the transaction is confirmed")*/
    public void downloadProductConfirmOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION,CAPTURE
        ));
        createCustomer(DIRECT_DEBIT_SEPA);
        magentoPage.getShopUserLoginPage().load();
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",5.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA) // Added username field cause name on card field is empty for download product which is an issue
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        magentoPage.getMyAccountPage().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),true,"Verify download link displayed in my download products menu");
    }
    /*@Test(priority = 11,description = "Verify that the Direct Debit SEPA payment is should not displayed in checkout page for USD currency")*/
    public void currencyValidation(){
        addProductToCart(PRODUCT_SEPA,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().changeCurrency("USD");
        magentoPage.getCheckoutPage()
                .load()
                .openCheckoutPage()
                .isPaymentDisplayed(DIRECT_DEBIT_SEPA,false);
    }



    //@Test(priority = 11, description = "Verify that direct debit sepa payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(DIRECT_DEBIT_SEPA,false);
        createCustomer(DIRECT_DEBIT_SEPA);
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(DIRECT_DEBIT_SEPA,false);
    }


}
