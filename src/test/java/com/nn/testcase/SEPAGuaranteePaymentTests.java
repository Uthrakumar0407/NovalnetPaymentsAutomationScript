package com.nn.testcase;

import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.DirectDebitSEPACallbackEvents;
import com.nn.callback.SEPAGuaranteeCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import io.qameta.allure.Step;
import org.testng.annotations.*;

import java.util.HashMap;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;

public class SEPAGuaranteePaymentTests extends BaseTest {

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
    public void adminLogin(@Optional("sepag") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }

    @Test(priority = 1, description = "Check whether the capture order guarantee placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void captureOrder_Guarantee(){
        verifyGlobalConfiguration();
        setSepaGuaranteePaymentConfiguration_Guarantee(true,false,false,true,CAPTURE,"","");
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

    @Test(priority = 2, description = "Check whether the authorize order guarantee placed successfully and refund via admin executed successfully",retryAnalyzer = RetryListener.class)
    public void authorizeOrder_Guarantee(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,true,true,AUTHORIZE,"","");
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
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,paymentName, paymentComments, tid);
        captureTransaction(orderNumber,tid,COMPLETION_ORDER_STATUS,paymentName,TID_STATUS_CONFIRMED);
        refundAdmin(Integer.parseInt(orderAmount)/2,COMPLETION_ORDER_STATUS,orderNumber,GUARANTEED_SEPA_BOOKBACK);
    }


    @Test(priority = 3, description = "Check whether the authorize test order guarantee with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMinimumOrder_Guarantee(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,true,false,AUTHORIZE,"100","");
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
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,paymentName, paymentComments, tid);
        wooCommerce.getCallback_sepaGuarantee().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().transactionRefund(tid,orderAmount);
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,orderAmount,REFUND_ORDER_STATUS);
    }

    @Test(priority = 4, description = "Check whether the authorize test order guarantee with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 5000",retryAnalyzer = RetryListener.class)
    public void authorizeAmountMaximumOrder_Guarantee(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,true,true,AUTHORIZE,"5000","");
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

   @Test(priority = 5, description = "Check whether the test order guarantee with b2b is successful and Confirmed via update event",retryAnalyzer = RetryListener.class)
    public void b2bConfirmOrder_Guarantee(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
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

    @Test(priority = 6, description = "Check whether the test order guarantee with b2b is successful and pending to confirmed via update event",retryAnalyzer = RetryListener.class)
    public void b2bUpdateConfirmOrder_Guarantee(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
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

    @Test(priority = 7, description = "Check whether the test order guarantee with b2b is successful and pending to deactivated via update event",retryAnalyzer = RetryListener.class)
    public void b2bUpdatDeactivateOrder_Guarantee(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
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

   @Test(priority = 8, description = "Check whether the test order guarantee with b2b is successful and updated from pending to onhold and onhold to confirm",retryAnalyzer = RetryListener.class)
    public void b2bUpdateOnHoldConfirmOrder_Guarantee(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
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
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,false,ONHOLD_ORDER_STATUS);
        wooCommerce.getCallback().transactionCapture(tid);
        statusCommentsVerification(orderNumber,CAPTURE_COMMENT_,paymentName,true,COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 9, description = "Check whether the test order guarantee with b2b is successful and updated from pending to onhold and onhold to cancel",retryAnalyzer = RetryListener.class)
    public void b2bUpdateOnHoldCancelOrder_Guarantee(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,true,false,true,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().setBillingCompany();
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
        statusCommentsVerification(orderNumber,UPDATE_COMMENT_,paymentName,false,ONHOLD_ORDER_STATUS);
        wooCommerce.getCallback().transactionCancel(tid);
        statusCommentsVerification(orderNumber,CANCEL_COMMENT_,paymentName,false,CANCELLATION_ORDER_STATUS);
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
    @AfterClass(alwaysRun = true)
    public void adminLogout() {
        wooCommerce.getDashBoardPage().adminLogout();
    }
}
