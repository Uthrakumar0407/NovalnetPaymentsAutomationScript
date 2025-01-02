package com.nn.testcase;

import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.InstalmentInvoiceCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Step;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.HashMap;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;

public class InstalmentInvoicePaymentTests extends BaseTest {

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
            .callback(new InstalmentInvoiceCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();


    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("installment") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }

    @Test(priority = 1, description = "Check whether the capture invoice order placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void captureOrder(){
        verifyGlobalConfiguration();
       // setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"");
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","4000");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().verifyInstalmentInvoiceTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().fillInstalmentInvoiceDOB(DOB);
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("3");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("3");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();

        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, INSTALMENT_INVOICE,14);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        TID_Helper.verifyInstalmentValuesInTID(tid,"3",cycleAmount);
        verifyInstalmentTable(3,tid);
        statusCommentsVerificationWithInstalmentTable(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid,3);
        wooCommerce.getCallback().instalment(tid,"2","1", DriverActions.getUpcomingMonthDate(2));
        statusCommentsVerificationForInstalmentRenewal(orderNumber,COMPLETION_ORDER_STATUS,2);
        wooCommerce.getCallback().instalment(tid,"3","0");
        statusCommentsVerificationForInstalmentRenewal(orderNumber,COMPLETION_ORDER_STATUS,3);
        refundAdmin(Integer.parseInt(cycleAmount),COMPLETION_ORDER_STATUS,orderNumber,INSTALMENT_INVOICE_BOOKBACK);
    }

    @Test(priority = 2, description = "Check whether the authorize order placed successfully and capture and partial refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void authorizeOrder(){
     //   setInstalmentInvoicePaymentConfiguration(true,true,true,AUTHORIZE,"");
        setInstalmentInvoicePaymentConfiguration(true,true,true,AUTHORIZE,"","4000");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().verifyInstalmentInvoiceTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().fillInstalmentInvoiceDOB(DOB);
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("4");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("4");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();

        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_ON_HOLD, INSTALMENT_INVOICE);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        TID_Helper.verifyInstalmentCycleDatesExist(tid,false);
        verifyInstalmentTableExist(false);
        statusCommentsVerificationWithInstalmentTable(orderNumber,ONHOLD_ORDER_STATUS,paymentName, paymentComments, tid,4);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_CONFIRMED);
        TID_Helper.verifyInstalmentValuesExist(tid,true);
        verifyInstalmentTable(4,tid);
        wooCommerce.getCallback().bankTransferByEndCustomer(tid,cycleAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,cycleAmount,COMPLETION_ORDER_STATUS);
        refundAdmin(Integer.parseInt(cycleAmount)/2,COMPLETION_ORDER_STATUS,orderNumber,INSTALMENT_INVOICE_BOOKBACK);
        cancelInstalment(orderNumber,REMAINING_CYCLES);
        statusCommentsVerificationInstalmentCancel(orderNumber,4,REMAINING_CYCLES);
    }

    @Test(priority = 3, description = "Check whether the authorize test order with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 2500",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMinimumOrder(){
      //  setInstalmentInvoicePaymentConfiguration(true,false,false,AUTHORIZE,"2500");
        setInstalmentInvoicePaymentConfiguration(true,false,false,AUTHORIZE,"2500","4000");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().verifyInstalmentInvoiceDateOfBirthIsDisplayed(true);
        wooCommerce.getCheckoutPage().fillInstalmentInvoiceDOB(DOB);
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("4");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("4");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();

        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_ON_HOLD, INSTALMENT_INVOICE);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        TID_Helper.verifyInstalmentCycleDatesExist(tid,false);
        verifyInstalmentTableExist(false);
        statusCommentsVerificationWithInstalmentTable(orderNumber,ONHOLD_ORDER_STATUS,paymentName, paymentComments, tid,4);
        wooCommerce.getCallback().transactionCapture(tid,DriverActions.getUpcomingMonthDatesInArr(4),"1","3",DriverActions.getUpcomingMonthDate(2));
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
        verifyInstalmentTable(4,tid);
        wooCommerce.getCallback().transactionRefund(tid,cycleAmount);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        verifyInstalmentTableStatus(1,5,REFUND_ORDER_STATUS);
        verifyInstalmentRefundBtnDisplayed(false,1);
        verifyInstalmentTableCycleAmount(1,3,0);
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, cycleAmount);
    }

    @Test(priority = 4, description = "Check whether the authorize test order with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 5000",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMaximumOrder(){
      //  setInstalmentInvoicePaymentConfiguration(true,false,true,AUTHORIZE,"5000");
        setInstalmentInvoicePaymentConfiguration(true,false,true,AUTHORIZE,"5000","4000");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().verifyInstalmentInvoiceDateOfBirthIsDisplayed(true);
        wooCommerce.getCheckoutPage().fillInstalmentInvoiceDOB(DOB);
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("4");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("4");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();

        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, INSTALMENT_INVOICE);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        TID_Helper.verifyInstalmentCycleDatesExist(tid,true);
        verifyInstalmentTable(4,tid);
        statusCommentsVerificationWithInstalmentTable(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid,4);
        var refundAmount = String.valueOf(Integer.parseInt(cycleAmount)/2);
        wooCommerce.getCallback().transactionRefund(tid,refundAmount);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        verifyInstalmentTableStatus(1,5,COMPLETION_ORDER_STATUS);
        verifyInstalmentRefundBtnDisplayed(true,1);
        verifyInstalmentTableCycleAmount(1,3,Integer.parseInt(cycleAmount)-Integer.parseInt(refundAmount));
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, refundAmount);

    }

    @Test(priority = 5, description = "Check whether the test order with b2b is successful and Confirmed via update event",retryAnalyzer = RetryListener.class)
    public void b2bConfirmOrder(){
      //  setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"");
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","4000");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().verifyInstalmentInvoiceDateOfBirthIsDisplayed(false);
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("4");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("4");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, INSTALMENT_INVOICE,30);
        TID_Helper.verifyBankDetails(tid, wooCommerce.getSuccessPage().getSuccessPageBankDetails(wooCommerce.getTxnInfo()));
        TID_Helper.verifyInstalmentCycleDatesExist(tid,true);
        verifyInstalmentTable(4,tid);
        statusCommentsVerificationWithInstalmentTable(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid,4);
        wooCommerce.getCallback().instalmentCancel(tid);
        statusCommentsVerificationInstalmentCancel(orderNumber,4,ALL_CYCLES);
    }

    @Test(priority = 6, description = "Check whether the test order with b2b is successful and pending to confirmed via update event",retryAnalyzer = RetryListener.class)
    public void b2bUpdateConfirmOrder(){
       // setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"");
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","4000");
        navigateCheckout(PRODUCT_3);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("4");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("4");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                //cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();
                cycleAmount =String.valueOf(Integer.parseInt(orderAmount)/4);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_PENDING, INSTALMENT_INVOICE);
        TID_Helper.verifyInstalmentCycleDatesExist(tid,false);
        verifyInstalmentTableExist(false);
        statusCommentsVerificationWithInstalmentTable(orderNumber,PENDING_ORDER_STATUS,paymentName, paymentComments, tid,4);
        wooCommerce.getCallback().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED,DriverActions.getUpcomingMonthDatesInArr(4),"1","3",DriverActions.getUpcomingMonthDate(2));
        statusCommentsVerificationWithInstalmentTable(orderNumber,COMPLETION_ORDER_STATUS,paymentName, UPDATE_COMMENT_, tid,4);
    }

    @Test(priority = 7, description = "Check whether the test order with b2b is successful and pending to deactivated via update event",retryAnalyzer = RetryListener.class)
    public void b2bUpdatDeactivateOrder(){
    //    setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"");
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","4000");
        navigateCheckout(PRODUCT_3);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("4");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("4");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                //cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();
        cycleAmount =String.valueOf(Integer.parseInt(orderAmount)/4);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_PENDING, INSTALMENT_INVOICE);
        wooCommerce.getCallback().transactionUpdateStatus(tid,TID_STATUS_DEACTIVATED);
        statusCommentsVerificationWithInstalmentTable(orderNumber,CANCELLATION_ORDER_STATUS,paymentName, CANCEL_COMMENT_, tid,4);
    }

    @Test(priority = 8, description = "Check whether the test order with b2b is successful and updated from pending to onhold and onhold to confirm",retryAnalyzer = RetryListener.class)
    public void b2bUpdateOnHoldConfirmOrder(){
      //  setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"");
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","4000");
        navigateCheckout(PRODUCT_3);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("4");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("4");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                //cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();
                cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/4);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_PENDING, INSTALMENT_INVOICE);
        wooCommerce.getCallback().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD,DriverActions.getUpcomingMonthDatesInArr(4),"1","3",DriverActions.getUpcomingMonthDate(2));
        statusCommentsVerificationWithInstalmentTable(orderNumber,ONHOLD_ORDER_STATUS,paymentName, UPDATE_COMMENT_, tid,4);
        wooCommerce.getCallback().transactionCapture(tid,DriverActions.getUpcomingMonthDatesInArr(4),"1","3",DriverActions.getUpcomingMonthDate(2));
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
        verifyInstalmentTable(4,tid);
    }

    @Test(priority = 9, description = "Check whether the test order with b2b is successful and updated from pending to onhold and onhold to cancel",retryAnalyzer = RetryListener.class)
    public void b2bUpdateOnHoldCancelOrder(){
      //  setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"");
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","4000");
        navigateCheckout(PRODUCT_3);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("4");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("4");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                //cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();
        cycleAmount = String.valueOf(Integer.parseInt(orderAmount)/4);
        TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_PENDING, INSTALMENT_INVOICE);
        wooCommerce.getCallback().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD,DriverActions.getUpcomingMonthDatesInArr(4),"1","3",DriverActions.getUpcomingMonthDate(2));
        statusCommentsVerificationWithInstalmentTable(orderNumber,ONHOLD_ORDER_STATUS,paymentName, UPDATE_COMMENT_, tid,4);
        wooCommerce.getCallback().transactionCancel(tid);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
    }

    @Test(priority = 10, description = "Ensure that payments are not allowed to proceed if the date of birth indicates an age below 18 years.")
    public void guaranteeValidation1(){
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","1998");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().fillInstalmentInvoiceDOB(DOB_BELOW_18);
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("3");
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        DriverActions.verifyEquals(wooCommerce.getCheckoutPage().getCheckoutPaymentError(),"You need to be at least 18 years old","");
    }

    @Test(priority = 11, description = "Verify that guarantee payments are displayed on the checkout page for customers from Germany in a B2C context.")
    public void guaranteeValidation2(){
        setInstalmentInvoicePaymentConfiguration(true,false,false,CAPTURE,"","1998");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckout();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,true);
        wooCommerce.getCheckoutPage().verifyInstalmentInvoiceDateOfBirthIsDisplayed(true);
    }

    @Test(priority = 12, description = "Verify that guarantee payments are displayed on the checkout page for customers from Austria in a B2C context.")
    public void guaranteeValidation3(){
        setInstalmentInvoicePaymentConfiguration(true,false,false,CAPTURE,"","1998");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Austria");
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,true);
        wooCommerce.getCheckoutPage().verifyInstalmentInvoiceDateOfBirthIsDisplayed(true);
    }

    @Test(priority = 13, description = "Verify that guarantee payments are displayed on the checkout page for customers from Switzerland in a B2C context.")
    public void guaranteeValidation4(){
        setInstalmentInvoicePaymentConfiguration(true,false,false,CAPTURE,"","1998");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Switzerland");
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,true);
        wooCommerce.getCheckoutPage().verifyInstalmentInvoiceDateOfBirthIsDisplayed(true);
    }

    @Test(priority = 14, description = "Verify that guarantee payments are not displayed on the checkout page for customers from England in a B2C context.")
    public void guaranteeValidation5(){
        setInstalmentInvoicePaymentConfiguration(true,false,false,CAPTURE,"","1998");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("United Kingdom (UK)");
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

    @Test(priority = 15, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2B context.")
    public void guaranteeValidation6(){
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","1998");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("United Kingdom (UK)");
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

    @Test(priority = 16, description = "Verify that guarantee payments are displayed on the checkout page for customers from Netharland in a B2B context.")
    public void guaranteeValidation7(){
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","1998");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Netherlands");
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,true);
    }

    @Test(priority = 17, description = "Verify that guarantee payments are not displayed on the checkout page for customers from United States in a B2B context.")
    public void guaranteeValidation8(){
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","1998");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("United States (US)");
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

    @Test(priority = 18, description = "Verify that guarantee payments are hidden on the checkout page for customers when amount is less 1998 EUR")
    public void guaranteeValidation9(){
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","1998");
        navigateCheckout(PRODUCT_2);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INVOICE,false);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

    @Test(priority = 19, description = "Verify that guarantee payments are hidden on the checkout page for customers with different billing and shipping")
    public void guaranteeValidation10(){
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","1998");
        wooCommerce.getMyAccountPage().setShippingAddress();
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().clickDifferentShipping();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }
    @Test(priority = 20, description = "Check whether the test transaction is successful with guest user ")
    public void guestOrder_InstalmentInvoice(){
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","4000");
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().fillInstalmentInvoiceDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getGuestOrderSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, INSTALMENT_INVOICE);
        wooCommerce.getAdminPage().adminLogin("installment", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }
    @Test(priority = 21,description = "Check whether Instalment Invoice payment logo displayed ")
    public void verifyInstalmentInvoiceLogoDisplayed(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("installment","wordpress");
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","4000");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("Instalment by Invoice");
    }
    @Test(priority = 22, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout_InstalmentInvoice(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("installment","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(INSTALMENT_INVOICE,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }

    @Step("Set Instalment Invoice payment configuration active {0}, test mode {1}, allow B2B {2}, payment action {3} and authorize minimum amount {4}")
    public void setInstalmentInvoicePaymentConfiguration(boolean paymentActive,
                                               boolean testMode,
                                               boolean allowB2B,
                                               String paymentAction,
                                               String authMinAmount){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInstalmentInvoiceGuaranteeConfiguration(paymentActive,testMode,allowB2B,paymentAction,authMinAmount));
    }

    @Step("Set Instalment Invoice payment configuration active {0}, test mode {1}, allow B2B {2}, payment action {3} and authorize minimum amount {4} and minimum order amount {5} ")
    public void setInstalmentInvoicePaymentConfiguration(boolean paymentActive,
                                                         boolean testMode,
                                                         boolean allowB2B,
                                                         String paymentAction,
                                                         String authMinAmount,String minOrderAmt){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInstalmentInvoiceWithMinOrderAmountGuaranteeConfiguration(paymentActive,testMode,allowB2B,paymentAction,authMinAmount,minOrderAmt));
    }

}

