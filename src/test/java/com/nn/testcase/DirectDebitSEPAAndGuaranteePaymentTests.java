package com.nn.testcase;

import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.DirectDebitSEPA;
import com.nn.callback.DirectDebitSEPACallbackEvents;
import com.nn.callback.ICallback;
import com.nn.callback.SEPAGuaranteeCallbackEvents;
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

public class DirectDebitSEPAAndGuaranteePaymentTests extends BaseTest {

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
            .callback(new DirectDebitSEPACallbackEvents())
            .callback_sepaGuarantee(new SEPAGuaranteeCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("sepa") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }

    @AfterClass(alwaysRun = true)
    public void adminLogout() {
        wooCommerce.getDashBoardPage().adminLogout();
    }
    @Test(priority = 1,alwaysRun = true, description = "Check whether the capture sepa order placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void captureOrder(){
        verifyGlobalConfiguration();
        setSepaPaymentConfiguration(true,false,false,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().verifySepaTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().verifySepaSaveCardCheckboxChecked(false);
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA,2);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
        refundAdminForInvoiceType(Integer.parseInt(orderAmount)/2,COMPLETION_ORDER_STATUS,orderNumber,tid);
    }

    @Test(priority = 2,alwaysRun = true, description = "Check whether the authorize sepa order placed successfully and capture via shop backend",retryAnalyzer = RetryListener.class)
    public void authorizeOrder(){
        setSepaPaymentConfiguration(true,true,true,AUTHORIZE,"","5");
        deletePaymentTokens();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().verifySepaTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().verifySepaSaveCardCheckboxChecked(true);
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA,5,0);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_CONFIRMED);
        wooCommerce.getCallback().chargeback(tid,orderAmount);
        statusCommentsVerification(orderNumber,CHARGEBACK_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().creditEntrySepa(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().debtCollectionSepa(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().bankTransferByEndCustomer(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        refundAdminForInvoiceType(Integer.parseInt(orderAmount),REFUND_ORDER_STATUS,orderNumber,tid);
    }

    @Test(priority = 3, alwaysRun = true, description = "Check whether the authorize sepa order with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMinimumOrder(){
        setSepaPaymentConfiguration(true,true,true,AUTHORIZE,"100","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().verifySepaTokenDisplayedAndChecked(true);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        wooCommerce.getCallback().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().paymentReminderOne(tid);
        statusCommentsVerification(orderNumber, REMINDER_ONE_COMMENT_);
        wooCommerce.getCallback().paymentReminderTwo(tid);
        statusCommentsVerification(orderNumber, REMINDER_ONE_COMMENT_);
        wooCommerce.getCallback().submissionToCollection(tid);
        statusCommentsVerification(orderNumber, COLLECTION_COMMENT_);
    }

   @Test(priority = 4,alwaysRun = true, description = "Check whether the authorize sepa order with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 5000",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMaximumOrder() {
        setSepaPaymentConfiguration(true, true,true, AUTHORIZE, "5000", "");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().clickUseNewPaymentMethodSEPA();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
    }

    @Test(priority = 5, alwaysRun = true, description = "Check whether the authorize sepa order placed successfully and cancelled via shop admin",retryAnalyzer = RetryListener.class)
    public void cancelTransactionOrder(){
        setSepaPaymentConfiguration(true, true,false, AUTHORIZE, "", "");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
        cancelTransaction(orderNumber,tid);
    }

    @Test(priority = 6, alwaysRun = true, description = "Check whether the authorize sepa order placed successfully and cancelled via callback event",retryAnalyzer = RetryListener.class)
    public void transactionCancelOrder(){
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
        wooCommerce.getCallback().transactionCancel(tid);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
    }


    @Test(priority = 7, alwaysRun = true, description = "Check whether the capture order sepa guarantee placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void captureOrder_Guarantee(){
        verifyGlobalConfiguration();
        //setSepaGuaranteePaymentConfiguration_Guarantee(true,false,false,true,CAPTURE,"","");
        setSepaGuaranteePaymentConfiguration_Guarantee(true,false,false,true,CAPTURE,"","","1800");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifySepaGuaranteeTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().verifySepaGuaranteeSaveCardCheckboxChecked(false);
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA,2);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
        refundAdmin(Integer.parseInt(orderAmount),REFUND_ORDER_STATUS,orderNumber,GUARANTEED_SEPA_BOOKBACK);
    }

    @Test(priority = 8,alwaysRun = true, description = "Check whether the authorize order sepa guarantee placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void authorizeOrder_Guarantee(){
        //setSepaGuaranteePaymentConfiguration_Guarantee(true,true,true,true,AUTHORIZE,"","");
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,true,true,AUTHORIZE,"","","1800");
        deletePaymentTokens();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifySepaGuaranteeTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().verifySepaGuaranteeSaveCardCheckboxChecked(true);
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid,true);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_CONFIRMED);
        refundAdmin(Integer.parseInt(orderAmount)/2,COMPLETION_ORDER_STATUS,orderNumber,GUARANTEED_SEPA_BOOKBACK);
    }


    @Test(priority = 9, alwaysRun = true, description = "Check whether the authorize test order sepa guarantee with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMinimumOrder_Guarantee(){
       // setSepaGuaranteePaymentConfiguration_Guarantee(true,true,true,false,AUTHORIZE,"100","");
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,true,false,AUTHORIZE,"100","","1800");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifySepaGuaranteeTokenDisplayedAndChecked(true);
        wooCommerce.getCheckoutPage().verifySepaGuaranteeDateOfBirthIsDisplayed(true);
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyReferenceTokenExist(tid,true);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        wooCommerce.getCallback_sepaGuarantee().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback_sepaGuarantee().transactionRefund(tid,orderAmount);
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,orderAmount,REFUND_ORDER_STATUS);
    }

    @Test(priority = 10, alwaysRun = true, description = "Check whether the authorize test order sepa guarantee with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 5000",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMaximumOrder_Guarantee(){
//        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,true,true,AUTHORIZE,"5000","");
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,true,true,AUTHORIZE,"5000","","1800");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().clickUseNewPaymentMethodSEPAGuarantee();
        wooCommerce.getCheckoutPage().verifySepaGuaranteeSaveCardCheckboxChecked(true);
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
        wooCommerce.getCallback_sepaGuarantee().paymentReminderOne(tid);
        statusCommentsVerification(orderNumber, REMINDER_ONE_COMMENT_);
        wooCommerce.getCallback_sepaGuarantee().paymentReminderTwo(tid);
        statusCommentsVerification(orderNumber, REMINDER_TWO_COMMENT_);
        wooCommerce.getCallback_sepaGuarantee().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2),COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 11, alwaysRun = true, description = "Check whether the test order sepa guarantee with b2b is successful and Confirmed via update event",retryAnalyzer = RetryListener.class)
    public void b2bConfirmOrder_Guarantee(){
        //setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","","1800");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifySepaGuaranteeDateOfBirthIsDisplayed(false);
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
    }

    @Test(priority = 12,alwaysRun = true, description = "Check whether the test order sepa guarantee with b2b is successful and pending to confirmed via update event",retryAnalyzer = RetryListener.class)
    public void b2bUpdateConfirmOrder_Guarantee(){
        //setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","","1800");
        navigateCheckout(PRODUCT_1);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_DIRECT_DEBIT_SEPA);
        statusCommentsVerification(orderNumber,PENDING_ORDER_STATUS,paymentName, paymentComments, tid);
        wooCommerce.getCallback_sepaGuarantee().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 13, alwaysRun = true, description = "Check whether the test order sepa guarantee with b2b is successful and pending to deactivated via update event",retryAnalyzer = RetryListener.class)
    public void b2bUpdatDeactivateOrder_Guarantee(){
        //setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","","1800");
        navigateCheckout(PRODUCT_1);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_DIRECT_DEBIT_SEPA);
        wooCommerce.getCallback_sepaGuarantee().transactionUpdateStatus(tid,TID_STATUS_DEACTIVATED);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
    }

    @Test(priority = 14, alwaysRun = true, description = "Check whether the test order sepa guarantee with b2b is successful and updated from pending to onhold and onhold to confirm",retryAnalyzer = RetryListener.class)
    public void b2bUpdateOnHoldConfirmOrder_Guarantee(){
        //setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","","1800");
        navigateCheckout(PRODUCT_1);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_DIRECT_DEBIT_SEPA);
        wooCommerce.getCallback_sepaGuarantee().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,false,ONHOLD_ORDER_STATUS_WC);
        wooCommerce.getCallback_sepaGuarantee().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 15, alwaysRun = true, description = "Check whether the test order sepa guarantee with b2b is successful and updated from pending to onhold and onhold to cancel",retryAnalyzer = RetryListener.class)
    public void b2bUpdateOnHoldCancelOrder_Guarantee(){
        //setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","","1800");
        navigateCheckout(PRODUCT_1);
        //wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        //wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckoutPending();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, GUARANTEED_DIRECT_DEBIT_SEPA);
        wooCommerce.getCallback_sepaGuarantee().transactionUpdateStatus(tid,TID_STATUS_ON_HOLD);
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,false,ONHOLD_ORDER_STATUS_WC);
        wooCommerce.getCallback_sepaGuarantee().transactionCancel(tid);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
    }
    @Test(priority = 16, description = "Check whether the zero amount booking order is placed successfully, verify token exist in the response and amount booked in the shop backend",retryAnalyzer = RetryListener.class)
    public void zeroAmountOrder(){
        verifyGlobalConfiguration();
        setSepaPaymentConfiguration(true,false,true,AUTHORIZE_WITH_ZERO_AMOUNT,"","");
        deletePaymentTokens();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().verifySepaTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().verifySepaSaveCardCheckboxChecked(false);
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
        bookAndVerifyZeroAmountBooking(orderAmount,DIRECT_DEBIT_SEPA);
    }
    @Test(priority = 17,dependsOnMethods = "zeroAmountOrder",description = "Check whether the zero amount order one click token displayed")
    public void checkZeroAmountTokenDisplayed() {
        verifyGlobalConfiguration();
        setSepaPaymentConfiguration(true,false,true,AUTHORIZE_WITH_ZERO_AMOUNT,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().verifySepaTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().verifySepaTokenDisplayedAndChecked(false);
    }

    @Test(priority = 18, description = "Ensure that payments are not allowed to proceed if the date of birth indicates an age below 18 years.")
    public void guaranteeValidation1(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,false,CAPTURE,"","","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB_BELOW_18);
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        DriverActions.verifyEquals(wooCommerce.getCheckoutPage().getCheckoutPaymentError(),"You need to be at least 18 years old","");
    }

    @Test(priority = 19, description = "Verify that when the \"Force Non-Guarantee\" option is enabled, a customer is under 18 years old can be used for an order." +
            " The order should proceed successfully as a normal Invoice payment.",retryAnalyzer = RetryListener.class)
    public void guaranteeValidation2(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(DIRECT_DEBIT_SEPA,true);
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,false,CAPTURE,"","","999",true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB_BELOW_18);
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        var tid = wooCommerce.getSuccessPage().getSuccessPageTransactionDetails().get("TID").toString();
        DriverActions.verifyEquals(TID_Helper.getTIDPaymentType(tid),DIRECT_DEBIT_SEPA);
    }

    @Test(priority = 20, description = "Verify that guarantee payments are displayed on the checkout page for customers from Germany in a B2C context.")
    public void guaranteeValidation3(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,false,CAPTURE,"","","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2BBillingAtCheckout();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,true);
        wooCommerce.getCheckoutPage().verifySepaGuaranteeDateOfBirthIsDisplayed(true);
    }

    @Test(priority = 21, description = "Verify that guarantee payments are displayed on the checkout page for customers from Austria in a B2C context.")
    public void guaranteeValidation4(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,false,CAPTURE,"","","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Austria");
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,true);
        wooCommerce.getCheckoutPage().verifySepaGuaranteeDateOfBirthIsDisplayed(true);
    }

    @Test(priority = 22, description = "Verify that guarantee payments are displayed on the checkout page for customers from Switzerland in a B2C context.")
    public void guaranteeValidation5(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,false,CAPTURE,"","","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Switzerland");
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,true);
        wooCommerce.getCheckoutPage().verifySepaGuaranteeDateOfBirthIsDisplayed(true);
    }

    @Test(priority = 23, description = "Verify that guarantee payments are not displayed on the checkout page for customers from England in a B2C context.")
    public void guaranteeValidation6(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,false,CAPTURE,"","","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("United Kingdom (UK)");
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,false);
    }

    @Test(priority = 24, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2B context.")
    public void guaranteeValidation7(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,true,CAPTURE,"","","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("United Kingdom (UK)");
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,false);
    }

    @Test(priority = 25, description = "Verify that guarantee payments are displayed on the checkout page for customers from Netharland in a B2B context.")
    public void guaranteeValidation8(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,true,CAPTURE,"","","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Netherlands");
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,true);
    }

    @Test(priority = 26, description = "Verify that guarantee payments are not displayed on the checkout page for customers from United States in a B2B context.")
    public void guaranteeValidation9(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,true,CAPTURE,"","","999",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("United States (US)");
        wooCommerce.getCheckoutPage().setBillingCompany();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,false);
    }

    @Test(priority = 27, description = "Verify whether the regular payment options (Invoice or Direct Debit SEPA) are hidden when the \"force non-guarantee\" option is turned off and the necessary guarantee conditions are not fulfilled.")
    public void guaranteeValidation10(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,true,CAPTURE,"","","999",false);
        navigateCheckout(PRODUCT_2);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(DIRECT_DEBIT_SEPA,false);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,false);
    }

    @Test(priority = 28, description = "Verify that guarantee payments are hidden on the checkout page for customers with different billing and shipping")
    public void guaranteeValidation11(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().setSEPAGuaranteeConfigurationWithMinOrderAmount(true,false,false,true,CAPTURE,"","","999",false);
        wooCommerce.getMyAccountPage().setShippingAddress();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().clickDifferentShipping();
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,false);
    }

    @Test(priority = 29, description = "Check whether the test transaction is successful with guest user ",retryAnalyzer = RetryListener.class)
    public void guestOrder_SEPA(){
        setSepaPaymentConfiguration(true,false,false,CAPTURE,"","");
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_2);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().verifySepaTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().verifySepaSaveCardCheckboxChecked(false);
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getGuestOrderSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        wooCommerce.getAdminPage().adminLogin("sepa", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }
    @Test(priority = 30, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout_SEPA(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("sepa","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(DIRECT_DEBIT_SEPA,false);
        navigateCheckout(PRODUCT_2);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(DIRECT_DEBIT_SEPA,false);
    }
    @Test(priority = 31, description = "Check whether the test transaction is successful with guest user ",retryAnalyzer = RetryListener.class)
    public void guestOrder_Guarantee(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","","1800");
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getGuestOrderSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        wooCommerce.getAdminPage().adminLogin("sepa", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }
    @Test(priority = 32, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout_Guarantee(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("sepa","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(GUARANTEED_DIRECT_DEBIT_SEPA,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,false);
    }
    @Test(priority = 33,description = "Check whether DirectDebit SEPA payment logo displayed ")
    public void verifySEPALogoDisplayed(){
        setSepaPaymentConfiguration(true,false,false,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("Direct Debit SEPA");
    }
    @Test(priority = 34,description = "Check whether Guarantee DirectDebit SEPA payment logo displayed ")
    public void verifyGuaranteeSEPALogoDisplayed(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","","1800");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("Direct Debit SEPA");
    }
    @Test(priority = 35,description = "Verify download option displayed when the transaction is confirmed"/*,retryAnalyzer = RetryListener.class*/)
    public void downloadProductConfirmOrder_SEPA(){
        setSepaPaymentConfiguration(true,false,false,CAPTURE,"","");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 36,description = "Verify download option should not displayed when transaction is on hold,then capture the order verify download option should be displayed",retryAnalyzer = RetryListener.class)
    public void downloadProductOnHoldToCaptureOrder_SEPA(){
        setSepaPaymentConfiguration(true,false,false,AUTHORIZE,"","");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_CONFIRMED);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 37,description = "Verify download option not displayed when transaction is on hold,then cancel the order verfy download option should not be displayed",retryAnalyzer = RetryListener.class)
    public void downloadProductOnHoldToCacelOrder_SEPA(){
        setSepaPaymentConfiguration(true,false,false,AUTHORIZE,"","");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        cancelTransaction(orderNumber,tid);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
    }
    @Test(priority = 38,description = "Verify download option displayed when the transaction is confirmed",retryAnalyzer = RetryListener.class)
    public void downloadProductConfirmOrder_GuaranteSEPA(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","","1800");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 39,description = "Verify download option should not displayed when transaction is on hold,then capture the order verify download option should be displayed",retryAnalyzer = RetryListener.class)
    public void downloadProductOnHoldToCaptureOrder_GuaranteeSEPA(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,AUTHORIZE,"","","1800");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_CONFIRMED);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 40,description = "Verify download option should not displayed when transaction is on hold,then cancel the order verify download option should not be displayed",retryAnalyzer = RetryListener.class)
    public void downloadProductOnHoldToCancelOrder_GuaranteeSEPA(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,AUTHORIZE,"","","1800");
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS_WC,paymentName, paymentComments, tid);
        cancelTransaction(orderNumber,tid);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
    }

    @Step("Set Direct Debit SEPA Payment Configuration active {0}, test mode {1}, payment action {2},allow b2b{3}, paymentAction{4}, authorize minimum amount {5} and due date {6}")
    public void setSepaGuaranteePaymentConfiguration_Guarantee(boolean paymentActive,
                                                               boolean testMode,
                                                               boolean oneClick,
                                                               boolean allowB2B,
                                                               String paymentAction,
                                                               String authMinAmount,
                                                               String dueDate){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setSEPAGuaranteeConfiguration(paymentActive,testMode,oneClick,allowB2B,paymentAction,authMinAmount,dueDate));
    }

    @Step("Set Direct Debit SEPA Payment Configuration active {0}, test mode {1}, payment action {2},allow b2b{3}, paymentAction{4}, authorize minimum amount {5} and due date {6} and minimum order amount {7}")
    public void setSepaGuaranteePaymentConfiguration_Guarantee(boolean paymentActive,
                                                               boolean testMode,
                                                               boolean oneClick,
                                                               boolean allowB2B,
                                                               String paymentAction,
                                                               String authMinAmount,
                                                               String dueDate,String minOrderAmount){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setSEPAGuaranteeConfigurationWithMinOrderAmount(paymentActive,testMode,oneClick,allowB2B,paymentAction,authMinAmount,dueDate,minOrderAmount));
    }

    @Step("Set Direct Debit SEPA Payment Configuration active {0}, test mode {1}, payment action {2}, authorize minimum amount {3} and due date {4}")
    public void setSepaPaymentConfiguration(boolean paymentActive,
                                               boolean testMode,
                                                boolean oneClick,
                                               String paymentAction,
                                               String authMinAmount,
                                               String dueDate){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setDirectDebitSepaConfiguration(paymentActive,testMode,oneClick,paymentAction,authMinAmount,dueDate));
    }
}

