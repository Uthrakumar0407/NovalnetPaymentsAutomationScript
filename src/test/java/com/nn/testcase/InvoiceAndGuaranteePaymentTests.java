package com.nn.testcase;

import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.InvoiceCallbackEvents;
import com.nn.callback.InvoiceGuaranteeCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Step;
import org.testng.annotations.*;

import java.util.HashMap;

import static com.nn.Magento.Constants.ONHOLD_ORDER_STATUS;
import static com.nn.Magento.Constants.ONHOLD_ORDER_STATUS_WC;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.verifyEquals;

public class InvoiceAndGuaranteePaymentTests extends BaseTest {

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
            .callback(new InvoiceCallbackEvents())
            .callback_invoiceGuarantee(new InvoiceGuaranteeCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("invoice") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }
    @AfterClass(alwaysRun = true)
    public void adminLogout() {
        wooCommerce.getDashBoardPage().adminLogout();
    }

    @Test(priority = 1, description = "Check whether the capture invoice order placed successfully and refund via admin executed successfully"/*,retryAnalyzer = RetryListener.class*/)
    public void captureOrder(){
        verifyGlobalConfiguration();
        setInvoicePaymentConfiguration(true,false,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());

        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE,14);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        statusCommentsVerification(orderNumber,PROCESSING_ORDER_STATUS,paymentName, paymentComments, tid);
        var updatedDueDate = DriverActions.addDaysFromDate(TID_Helper.getDueDate(tid), 10);
        wooCommerce.getCallback().transactionUpdateDueDate(tid,updatedDueDate);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,updatedDueDate);
        refundAdminForInvoiceType(Integer.parseInt(orderAmount)/2,PROCESSING_ORDER_STATUS,orderNumber,tid);
        refundAdminForInvoiceType(Integer.parseInt(orderAmount)/2,REFUND_ORDER_STATUS,orderNumber,tid);
    }


    @Test(priority = 2, description = "Check whether the authorize invoice order placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void authorizeOrder(){
        setInvoicePaymentConfiguration(true,true,AUTHORIZE,"","10");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE,10);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName,paymentComments,tid);
        captureTransaction(orderNumber,tid,PROCESSING_ORDER_STATUS,paymentName,TID_STATUS_PENDING);
        wooCommerce.getCallback().invoiceCredit(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2),PROCESSING_ORDER_STATUS);
        wooCommerce.getCallback().invoiceCredit(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2),COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().transactionRefund(tid,orderAmount);
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,orderAmount,REFUND_ORDER_STATUS);
    }

    @Test(priority = 3, description = "Check whether the authorize invoice test order with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100")
    public void authorizeAmountMinimumOrder(){
        setInvoicePaymentConfiguration(true,true,AUTHORIZE,"100","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS_WC, paymentName, paymentComments, tid);
        wooCommerce.getCallback().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,PROCESSING_ORDER_STATUS);
        wooCommerce.getCallback().invoiceCredit(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().debtCollectionDE(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().bankTransferByEndCustomer(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2),COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 4, description = "Check whether the authorize invoice test order with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 5000")
    public void authorizeAmountMaximumOrder() {
        setInvoicePaymentConfiguration(true, true, AUTHORIZE, "5000", "");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        statusCommentsVerification(orderNumber, PROCESSING_ORDER_STATUS, paymentName, paymentComments, tid);
        String updatedAmount = String.valueOf(Integer.parseInt(orderAmount) * 2);
        wooCommerce.getCallback().transactionUpdateAmount(tid,updatedAmount);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,updatedAmount,PROCESSING_ORDER_STATUS);
        wooCommerce.getCallback().paymentReminderOne(tid);
        statusCommentsVerification(orderNumber, REMINDER_ONE_COMMENT_);
        wooCommerce.getCallback().paymentReminderTwo(tid);
        statusCommentsVerification(orderNumber, REMINDER_TWO_COMMENT_);
        wooCommerce.getCallback().submissionToCollection(tid);
        statusCommentsVerification(orderNumber, COLLECTION_COMMENT_);
        wooCommerce.getCallback().invoiceCredit(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,PROCESSING_ORDER_STATUS);
    }
    @Test(priority = 5, description = "Check whether the authorize invoice order placed successfully and cancelled via shop admin",retryAnalyzer = RetryListener.class)
    public void cancelTransactionOrder(){
        setInvoicePaymentConfiguration(true,true,AUTHORIZE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        cancelTransaction(orderNumber,tid);
    }

    @Test(priority = 6, description = "Check whether the authorize invoice order placed successfully and cancelled via callback event")
    public void transactionCancelOrder(){
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        wooCommerce.getCallback().transactionCancel(tid);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
    }

    @Test(priority = 7, description = "Check whether the capture invoice guarantee order placed successfully and refund via admin executed successfully")
    public void captureOrder_Guarantee(){
       // setInvoicePaymentConfiguration_Guarantee(true,false,true,CAPTURE,"");
        setInvoicePaymentConfiguration_Guarantee(true,false,true,CAPTURE,"","1800");
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

    @Test(priority = 8, description = "Check whether the authorize invoice guarantee order placed successfully and refund via admin executed successfully")
    public void authorizeOrder_Guarantee(){
      //  setInvoicePaymentConfiguration_Guarantee(true,true,true,AUTHORIZE,"");
        setInvoicePaymentConfiguration_Guarantee(true,true,true,AUTHORIZE,"","1800");
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
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_CONFIRMED);
        wooCommerce.getCallback_invoiceGuarantee().bankTransferByEndCustomer(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        refundAdmin(Integer.parseInt(orderAmount)/2,COMPLETION_ORDER_STATUS,orderNumber,GUARANTEED_INVOICE_BOOKBACK);
    }

    @Test(priority = 9, description = "Check whether the authorize invoice guarantee test order with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100")
    public void authorizeAmountMinimumOrder_Guarantee(){
       // setInvoicePaymentConfiguration_Guarantee(true,true,false,AUTHORIZE,"100");
        setInvoicePaymentConfiguration_Guarantee(true,true,false,AUTHORIZE,"100","1800");
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
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        wooCommerce.getCallback_invoiceGuarantee().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback_invoiceGuarantee().transactionRefund(tid,orderAmount);
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,orderAmount,REFUND_ORDER_STATUS);
    }

    @Test(priority = 10, description = "Check whether the authorize invoice guarantee test order with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 5000")
    public void authorizeAmountMaximumOrder_Guarantee(){
        //setInvoicePaymentConfiguration_Guarantee(true,true,true,AUTHORIZE,"5000");
        setInvoicePaymentConfiguration_Guarantee(true,true,true,AUTHORIZE,"5000","1800");
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
        wooCommerce.getCallback_invoiceGuarantee().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2),COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 11, description = "Check whether the test order invoice guarantee with b2b is successful and Confirmed via update event")
    public void b2bConfirmOrder_Guarantee(){
     //   setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"","1800");
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

    @Test(priority = 12, description = "Check whether the test order invoice guarantee with b2b is successful and pending to confirmed via update event")
    public void b2bUpdateConfirmOrder_Guarantee(){
     //   setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"","1800");
        navigateCheckout(PRODUCT_1);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
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
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,paymentName, paymentComments, tid,true);
        wooCommerce.getCallback_invoiceGuarantee().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 13, description = "Check whether the test order invoice guarantee with b2b is successful and pending to deactivated via update event")
    public void b2bUpdatDeactivateOrder_Guarantee(){
       // setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"","1800");
        navigateCheckout(PRODUCT_1);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
       // wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        wooCommerce.getCallback_invoiceGuarantee().transactionUpdateStatus(tid,TID_STATUS_DEACTIVATED);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
    }

    @Test(priority = 14, description = "Check whether the test order invoice guarantee with b2b is successful and updated from pending to onhold and onhold to confirm")
    public void b2bUpdateOnHoldConfirmOrder_Guarantee(){
    //    setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"","1800");
        navigateCheckout(PRODUCT_1);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
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
        wooCommerce.getCallback_invoiceGuarantee().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,false,ONHOLD_ORDER_STATUS_WC);
        wooCommerce.getCallback_invoiceGuarantee().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 15, description = "Check whether the test order invoice guarantee with b2b is successful and updated from pending to onhold and onhold to cancel")
    public void b2bUpdateOnHoldCancelOrder_Guarantee(){
     //   setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"");
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"","1800");
        navigateCheckout(PRODUCT_1);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_INVOICE);
        wooCommerce.getCallback_invoiceGuarantee().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,false,ONHOLD_ORDER_STATUS_WC);
        wooCommerce.getCallback_invoiceGuarantee().transactionCancel(tid);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
    }

    @Test(priority = 16, description = "Ensure that payments are not allowed to proceed if the date of birth indicates an age below 18 years.")
    public void guaranteeValidation1(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,false,CAPTURE,"","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB_BELOW_18);
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        DriverActions.verifyEquals(wooCommerce.getCheckoutPage().getCheckoutPaymentError(),"You need to be at least 18 years old","");
    }

    @Test(priority = 17, description = "Verify that when the \"Force Non-Guarantee\" option is enabled, a customer is under 18 years old can be used for an order." +
            " The order should proceed successfully as a normal Invoice payment.",retryAnalyzer = RetryListener.class)
    public void guaranteeValidation2(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(INVOICE,true);
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,false,CAPTURE,"","999",true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB_BELOW_18);
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        var tid = wooCommerce.getSuccessPage().getSuccessPageTransactionDetails().get("TID").toString();
        DriverActions.verifyEquals(TID_Helper.getTIDPaymentType(tid),INVOICE);
    }

    @Test(priority = 18, description = "Verify that guarantee payments are displayed on the checkout page for customers from Germany in a B2C context.")
    public void guaranteeValidation3(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,false,CAPTURE,"","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckout();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,true);
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeDateOfBirthIsDisplayed(true);
    }

    @Test(priority = 19, description = "Verify that guarantee payments are displayed on the checkout page for customers from Austria in a B2C context.")
    public void guaranteeValidation4(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,false,CAPTURE,"","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Austria");
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,true);
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeDateOfBirthIsDisplayed(true);
    }

    @Test(priority = 20, description = "Verify that guarantee payments are displayed on the checkout page for customers from Switzerland in a B2C context.")
    public void guaranteeValidation5(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,false,CAPTURE,"","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Switzerland");
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,true);
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeDateOfBirthIsDisplayed(true);
    }

    @Test(priority = 21, description = "Verify that guarantee payments are not displayed on the checkout page for customers from England in a B2C context.")
    public void guaranteeValidation6(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,false,CAPTURE,"","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("United Kingdom (UK)");
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }

    @Test(priority = 22, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2B context.")
    public void guaranteeValidation7(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,true,CAPTURE,"","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("United Kingdom (UK)");
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }

    @Test(priority = 23, description = "Verify that guarantee payments are displayed on the checkout page for customers from Netharland in a B2B context.")
    public void guaranteeValidation8(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,true,CAPTURE,"","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Netherlands");
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,true);
    }

    @Test(priority = 24, description = "Verify that guarantee payments are not displayed on the checkout page for customers from United States in a B2B context.")
    public void guaranteeValidation9(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,true,CAPTURE,"","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("United States (US)");
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }

    @Test(priority = 25, description = "Verify whether the regular payment options (Invoice or Direct Debit SEPA) are hidden when the \"force non-guarantee\" option is turned off and the necessary guarantee conditions are not fulfilled.")
    public void guaranteeValidation10(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,true,CAPTURE,"","999",false);
        navigateCheckout(PRODUCT_2);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INVOICE,false);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }
    @Test(priority = 26, description = "Verify that guarantee payments are hidden on the checkout page for customers with different billing and shipping")
    public void guaranteeValidation11(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setInvoiceGuaranteeWithMinOrderAmountConfiguration(true,false,true,CAPTURE,"","999",false);
        wooCommerce.getMyAccountPage().setShippingAddress();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().clickDifferentShipping();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }
    @Test(priority = 27, description = "Check whether the test transaction is successful with guest user ")
    public void guestOrder_Invoice(){
        setInvoicePaymentConfiguration(true,false,CAPTURE,"","");
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_2);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getGuestOrderSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, INVOICE);
        wooCommerce.getAdminPage().adminLogin("invoice", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),PROCESSING_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }
    @Test(priority = 28,description = "Check whether invoice payment logo displayed ")
    public void verifyInvoiceLogoDisplayed(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("invoice","wordpress");
        setInvoicePaymentConfiguration(true,false,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("Invoice");
    }
    @Test(priority = 29,description = "Check whether Guarantee invoice payment logo displayed ")
    public void verifyGuaranteeInvoiceLogoDisplayed(){
        setInvoicePaymentConfiguration_Guarantee(true,true,true,CAPTURE,"","1800");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("Invoice");
    }
    @Test(priority = 30,description = "Verify download option displayed when the transaction is confirmed")
    public void downloadProductConfirmOrder_Invoice(){
        setInvoicePaymentConfiguration(true,false,CAPTURE,"","");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE,14);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 31,description = "Verify download option should not displayed when transaction is on hold,then capture the order verify download option should be displayed")
    public void downloadProductOnHoldToCaptureOrder_Invoice(){
        setInvoicePaymentConfiguration(true,false,AUTHORIZE,"","");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_PENDING);
        wooCommerce.getCallback().invoiceCredit(tid,String.valueOf(Integer.parseInt(orderAmount)));
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 32,description = "Verify download option should not displayed when transaction is on hold,then cancel the order verify download option should not be displayed")
    public void downloadProductOnHoldToCancelOrder_Invoice(){
        setInvoicePaymentConfiguration(true,false,AUTHORIZE,"","");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        cancelTransaction(orderNumber,tid);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
    }

    @Test(priority = 33,description = "Verify download option displayed when the transaction is confirmed",retryAnalyzer = RetryListener.class)
    public void downloadProductConfirmOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,false,true,CAPTURE,"","1800");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE,14);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 34,description = "Verify download option should not displayed when transaction is on hold,then capture the order verify download option should be displayed")
    public void downloadProductOnHoldToCaptureOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,false,true,AUTHORIZE,"","1800");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_CONFIRMED);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 35,description = "Verify download option should not displayed when transaction is on hold,then cancel the order verify download option should not be displayed")
    public void downloadProductOnHoldToCancelOrder_Guarantee(){
        setInvoicePaymentConfiguration_Guarantee(true,false,true,AUTHORIZE,"","1800");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        cancelTransaction(orderNumber,tid);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
    }


    @Test(priority = 36, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout_Invoice(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("invoice","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(INVOICE,false);
        navigateCheckout(PRODUCT_2);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INVOICE,false);
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

    @Step("Set Invoice with payment guarantee Configuration active {0}, test mode {1}, allow B2B {2}, payment action {3} and authorize minimum amount {4} and minimum order amount {5}")
    public void setInvoicePaymentConfiguration_Guarantee(boolean paymentActive,
                                                         boolean testMode,
                                                         boolean allowB2B,
                                                         String paymentAction,
                                                         String authMinAmount,String minOrderAmount){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceGuaranteeWithMinOrderAmountConfiguration(paymentActive,testMode,allowB2B,paymentAction,authMinAmount,minOrderAmount));
    }
    @Step("Set Invoice Payment Configuration active {0}, test mode {1}, payment action {2}, authorize minimum amount {3} and due date {4}")
    public void setInvoicePaymentConfiguration(boolean paymentActive,
                                               boolean testMode,
                                               String paymentAction,
                                               String authMinAmount,
                                               String dueDate){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceConfiguration(paymentActive,testMode,paymentAction,authMinAmount,dueDate));
    }

}
