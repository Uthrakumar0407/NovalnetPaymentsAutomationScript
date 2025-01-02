package com.nn.testcase;

import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.CreditCardCallbackEvents;
import static com.nn.Magento.Constants.ONHOLD_ORDER_STATUS_WC;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.*;

import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.reports.ExtentTestManager;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.*;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreditCardPaymentTests extends BaseTest {

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
            .callback(new CreditCardCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .testData2(ExcelHelpers.declineCreditCards())
            .build();



    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("cc") String userName, @Optional("wordpress") String password) {
        ExtentTestManager.saveToReport("Setup","Setting up for cc");
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(CREDITCARD,true);
    }


    @AfterClass(alwaysRun = true)
    public void adminLogout() {
        wooCommerce.getDashBoardPage().adminLogout();
    }



    @Test(priority=1, description="Check whether the capture test transaction is successful with payment action set to Capture and Inline form set to true and check the follow up events executed ", retryAnalyzer = RetryListener.class)
    public void captureOrder() {
        verifyGlobalConfiguration();
        setPaymentConfiguration(true,CAPTURE,"",true,false,true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().verifyCCTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().verifySaveCardCheckboxChecked(false);
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().verifyInlineFormDisplayed(true);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        statusCommentsVerification(COMPLETION_ORDER_STATUS);
        partialRefundAdmin();
        chargeback();
        creditEvent(CREDIT_ENTRY_CREDITCARD);
        creditEvent(DEBT_COLLECTION_CREDITCARD);
        creditEvent(CREDITCARD_REPRESENTMENT);
        creditEvent(BANK_TRANSFER_BY_END_CUSTOMER);
    }
    @Test(priority=2, description="Check whether the authorize test order is successful with payment action set to Authorize", retryAnalyzer = RetryListener.class)
    public void authorizeOrder() {
        setPaymentConfiguration(true,AUTHORIZE,"",false,false,false,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().verifyCCTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().verifySaveCardCheckboxChecked(false);
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberRedirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().verifyInlineFormDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderUsingCreditCardWithRedirection();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, CREDITCARD);
        statusCommentsVerification(ONHOLD_ORDER_STATUS_WC);
        captureTransaction();
        fullRefundAdmin();
    }

    @Test(priority=3, description="Check whether the authorize test order with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100", retryAnalyzer = RetryListener.class)
    public void authorizeAmountMinimumOrder() {
        setPaymentConfiguration(true,AUTHORIZE,"100",true,true,true,false);
        navigateCheckoutWithDeleteTokens(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().verifyCCTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().verifySaveCardCheckboxChecked(true);
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().verifyInlineFormDisplayed(true);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, CREDITCARD);
        statusCommentsVerification(ONHOLD_ORDER_STATUS);
        transactionCapture();
        paymentReminder(PAYMENT_REMINDER_1);
        paymentReminder(PAYMENT_REMINDER_2);
        submissionToCollection();
        transactionRefund(Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString()));
    }

    @Test(priority=4, description="Check whether the authorize test order with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 5000", retryAnalyzer = RetryListener.class)
    public void authorizeAmountMaximumOrder() {
        setPaymentConfiguration(true,AUTHORIZE,"5000",true,true,true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().verifyCCiFrameDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        TID_Helper.verifyReferenceTokenExist(wooCommerce.getTxnInfo().get("TID").toString(), true);
        statusCommentsVerification(COMPLETION_ORDER_STATUS);
        transactionRefund(Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString())/2);
    }

    @Test(priority=5, description="Check whether the test order with enforce 3d enabled is successful with payment action set to Authorize and enforce3D set to true", retryAnalyzer = RetryListener.class)
    public void enforce3dOrder() {
        setPaymentConfiguration(true,AUTHORIZE,"",true,true,true,true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().clickUseNewPaymentMethod();
        wooCommerce.getCheckoutPage().verifyCCiFrameDisplayed(true);
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().uncheckSaveCardCheckbox();
        wooCommerce.getCheckoutPage().verifyCCAuthenticationPageDisplayed(true);
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, CREDITCARD);
        TID_Helper.verifyPaymentTokenExist(wooCommerce.getTxnInfo().get("TID").toString(),false);
        statusCommentsVerification(ONHOLD_ORDER_STATUS);
        cancelTransaction();
    }

    @Test(priority=6, description="Check whether the Transaction Cancel Event successfully executed", retryAnalyzer = RetryListener.class)
    public void transactionCancelOrder() {
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, CREDITCARD);
        transactionCancel();
    }

    @Test(priority=7, description="Check whether the test order is successfully payment event by communication break " , retryAnalyzer = RetryListener.class)
    public void paymentSuccessOrder() {
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().clickUseNewPaymentMethod();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberRedirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        communicationBreakGetOrderNumber();
        wooCommerce.getCallback().communicationBreakSuccess(wooCommerce.getTxnInfo().get("TID").toString(),wooCommerce.getTxnInfo().get("OrderNumber").toString(),"");
        statusCommentsVerificationAfterCommunicationBreak(ONHOLD_ORDER_STATUS_WC);
    }

    @Test(priority=8, description="Check whether the test order is successfully using payment after failure by communication break and execute payment with failure", retryAnalyzer = RetryListener.class)
    public void paymentFailureRePayOrder() {
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().clickUseNewPaymentMethod();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberRedirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        communicationBreakGetOrderNumber();
        wooCommerce.getCallback().communicationBreakFailure(wooCommerce.getTxnInfo().get("TID").toString(),wooCommerce.getTxnInfo().get("OrderNumber").toString(),"");
        statusCommentsVerificationAfterCommunicationBreak(FAILURE_ORDER_STATUS);
        eighthTestOrder_PayAfterFailure();
    }
    @Test(priority = 9, description = "Check whether the zero amount booking order is placed successfully, verify token exist in the response and amount booked in the shop backend",retryAnalyzer = RetryListener.class)
    public void zeroAmountOrder() {
        setPaymentConfiguration(true,AUTHORIZE_WITH_ZERO_AMOUNT,"",true,true,false,false);
        navigateCheckoutWithDeleteTokens(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().verifyCCTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_CONFIRMED, CREDITCARD);
        TID_Helper.verifyPaymentTokenExist(tid);
        statusCommentsVerification(COMPLETION_ORDER_STATUS);
        bookAndVerifyZeroAmountBooking(orderAmount,CREDITCARD);
    }
    @Test(priority = 10,dependsOnMethods = "zeroAmountOrder",description = "Check whether the zero amount booking order is placed successfully, verify token exist in the response and amount booked in the shop backend",retryAnalyzer = RetryListener.class)
    public void checkZeroAmountTokenDisplayed() {
        setPaymentConfiguration(true,AUTHORIZE_WITH_ZERO_AMOUNT,"",true,true,false,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().verifyMaskedCCDataDisplayed(false);
    }
    @Test(priority = 11,description = "Check whether the zero amount booking order with redirect card is by communication break, verifying zero amount booking menu displayed" +
            " book the amount receive error `reference transaction not successful`, execute initial level , verify zero amount booking for this order")
    public void zeroAmountCommunicationBreakOrder() {
        setPaymentConfiguration(true,AUTHORIZE_WITH_ZERO_AMOUNT,"",true,true,false,false);
        navigateCheckoutWithDeleteTokens(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberRedirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        String orderNumber = communicationBreakGetOrderNumber();
        String tid = NovalnetAPIs.getRecentTransactionTID(orderNumber);
        wooCommerce.getCallback().communicationBreakSuccess(tid,"");
        statusCommentsVerificationAfterCommunicationBreak(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().bookTransactionForZeroAmountBookingFailureOrder();
    }
    @Test(priority = 12,dependsOnMethods = "zeroAmountCommunicationBreakOrder",description = "Check whether the token is displaying in the checkout page for communication break handled transaction")
    public void checkZeroAmountTokenDisplayedForCommunicationBreakOrder() {
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().verifyCCTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().verifyMaskedCCDataDisplayed(false);
    }

    @Test(priority = 13, description = "Check whether the user gets the appropriate error message displays on cart page after using an expired card," +
            " verify product remain in cart", retryAnalyzer = RetryListener.class)
    public void expiredCard() {
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().ifTokenDisplayedClickNew();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData2().get("Expired"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        wooCommerce.getCheckoutPage().waitCCAuthenticationPage();
        var errorMessage = wooCommerce.getCheckoutPage().getCheckoutPaymentError();
        verifyEquals(errorMessage,CC_EXPIRED_CARD_ERROR,"Verify expired card error message");
    }

    @Test(priority = 14, description = "Check whether the user gets the appropriate error message displays on cart page after using an restricted card," +
            " verify product remain in cart",retryAnalyzer = RetryListener.class)
    public void restrictedCard() {
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().ifTokenDisplayedClickNew();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData2().get("Restricted"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        wooCommerce.getCheckoutPage().waitCCAuthenticationPage();
        var errorMessage = wooCommerce.getCheckoutPage().getCheckoutPaymentError();
        verifyEquals(errorMessage,CC_RESTRICTED_CARD_ERROR,"Verify restricted card error message");
    }

    @Test(priority = 15, description ="Check whether the user gets the appropriate error message displays on cart page after using card with Insufficient funds or credit limit exceeded card data.",retryAnalyzer = RetryListener.class)
    public void insufficientFundsCard(){
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().ifTokenDisplayedClickNew();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData2().get("InsufficientFunds"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        wooCommerce.getCheckoutPage().waitCCAuthenticationPage();
        var errorMessage = wooCommerce.getCheckoutPage().getCheckoutPaymentError();
        verifyEquals(errorMessage,CC_INSUFFICIENT_CARD_ERROR,"Verify in sufficient card error message");
    }

    @Test(priority = 16, description ="Check whether the user gets the appropriate error message displays on cart page by cancelling the payment on the OTP page..",retryAnalyzer = RetryListener.class )
    public void redirectCardEndUserCancel(){
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().ifTokenDisplayedClickNew();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberRedirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        wooCommerce.getCheckoutPage().cancelAtCCAuthenticationPage();
        var errorMessage = wooCommerce.getCheckoutPage().getCheckoutPaymentError();
        verifyEquals(errorMessage,CC_END_USER_CANCEL_ERROR,"Verify end user cancel error message");
    }

    @Test(priority = 17, description = "Check whether the test transaction is successful with guest user ",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        setPaymentConfiguration(true,CAPTURE,"",true,false,true,false);
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getGuestOrderSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        wooCommerce.getAdminPage().adminLogin("cc", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }

    @Test(priority = 18, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("cc","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(CREDITCARD,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(CREDITCARD,false);
    }
    @Test(priority = 19,description = "Check whether creditcard payment logo displayed ")
    public void verifyCreditCardLogoDisplayed(){
        setPaymentConfiguration(true, CAPTURE, "", false, false, true, false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("Credit/Debit Cards");
    }
    @Test(priority = 20, description = "Verify download option displayed when the transaction is confirmed",retryAnalyzer = RetryListener.class)
    public void downloadProductConfirmOrder(){
        setPaymentConfiguration(true, CAPTURE, "", false, false, true, false);
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        String orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString();
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority = 21,description = "Verify download option should not be displayed when transaction is on hold,then capture the order download option should be displayed",retryAnalyzer = RetryListener.class)
    public void downloadProductOnHoldToCaptureOrder(){
        setPaymentConfiguration(true, AUTHORIZE, "", false, false, true, false);
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, CREDITCARD);
        String orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString();
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(ONHOLD_ORDER_STATUS);
        captureTransaction();
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), true);
    }
    @Test(priority =22,description = "Verify download option should not be displayed when transaction is on hold,then  cancel the order verify download option should not be displayed",retryAnalyzer = RetryListener.class)
    public void downloadProductOnHoldToCancelOrder(){
        setPaymentConfiguration(true, AUTHORIZE, "", false, false, true, false);
        navigateCheckout(DOWNLOADABLE_PRODUCT);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, CREDITCARD);
        String orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString();
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
        statusCommentsVerification(ONHOLD_ORDER_STATUS);
        cancelTransaction();
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        verifyEquals(wooCommerce.getMyAccountPage().isDownloadProductDisplayed(), false);
    }


    /*@Test(priority=9, description="Check whether the shop based subscription order is placed successfully", retryAnalyzer = RetryListener.class)
    public void subscriptionOrderShopBased() {
        setSubscriptionConfigurationShopBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        setPaymentConfiguration(true,"Capture","",true,false,true,false);
        navigateCheckout("TEST-4 SUBS");
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,false);
        subscriptionRenewalOrderCron();
        subscriptionChangePayment();
        subscriptionRenewalOrderTwoCron();
        subscriptionRenewalOrderThreeFrontend();
    }


    @Test(priority = 10, description="Check whether the Novalnet based subscription capture order is placed successfully", retryAnalyzer = RetryListener.class)
    public void subscriptionCaptureOrderNovalnetBased() {
        setSubscriptionConfigurationNovalnetBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        setPaymentConfiguration(true,"Capture","",true,false,true,false);
        navigateCheckout("TEST-4 SUBS");
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), wooCommerce.getTxnInfo().get("SubscriptionOrderNextPayment").toString(), CREDITCARD);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,true);
        subscriptionChangePayment();
        subscriptionChangeNextCycleDate();
        subscriptionUpdateAmount();
        subscriptionRenewalOrder();
        subscriptionSuspend();
        subscriptionReactivate();
        subscriptionChangePaymentAdmin();
        subscriptionCancel();
    }


    @Test(priority = 11, description="Check whether the Novalnet based subscription authorize order is placed successfully", retryAnalyzer = RetryListener.class)
    public void subscriptionAuthorizeOrderNovalnetBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().disableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        setPaymentConfiguration(true,"Authorize","",true,false,true,false);
        navigateCheckout("TEST-2 SUBS");
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, CREDITCARD);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), CREDITCARD);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ONHOLD,ONHOLD_ORDER_STATUS,false);
        subscriptionCaptureViaAdmin();
        subscriptionRenewalCallback();
        subscriptionSuspendCallback();
        subscriptionReactivateCallback();
        subscriptionAmountUpdateCallback();
        subscriptionCycleDateUpdate();
        subscriptionChangePaymentCallback();
        subscriptionCancelCallback();
    }*/



    @Test(priority = 23,enabled = true, description = "Validation of the CC iFrame with different CC inputs - alphanumeric, special character, german character",retryAnalyzer = RetryListener.class)
    public void validateCreditCardIFrame(){
        setPaymentConfiguration(true,CAPTURE,"",true,false,true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().validateCreditCardForm();
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

    @Step("Change Subscription next cycle date")
    public void subscriptionChangeNextCycleDate() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String nextCycleDate = wooCommerce.getSubscriptionPage().getNextPaymentDate();
        String updatedCycleDate = addDaysFromDate(nextCycleDate,5);
        wooCommerce.getSubscriptionPage().changeNextPaymentDate(updatedCycleDate);
        TID_Helper.verifySubscriptionNextPaymentDateInTID(wooCommerce.getTxnInfo().get("TID").toString(), updatedCycleDate,"yyyy-MM-dd");
        String orderTotal = wooCommerce.getSubscriptionPage().getOrderTotalWithCurrency();
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        String updateComment = wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_UPDATE_COMMENT_+orderTotal+" on "+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(updateComment);
    }

    @Step("Update subscription amount and verify TID")
    public void subscriptionUpdateAmount() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        int updatedCycleAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString())/2;
        String updatedTotalCycleAmount = wooCommerce.getSubscriptionPage().updateSubscriptionAmount(updatedCycleAmount);
        wooCommerce.getTxnInfo().put("updatedSubscriptionCycleAmount", updatedTotalCycleAmount);
        TID_Helper.verifySubscriptionCycleAmountInTID(wooCommerce.getTxnInfo().get("TID").toString(), updatedTotalCycleAmount);
        String orderTotal = wooCommerce.getSubscriptionPage().getOrderTotalWithCurrency();
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        String updateComment = wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_UPDATE_COMMENT_+orderTotal+" on "+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(updateComment);
    }

    @Step("Place Subscription renewal order novalnetbased in shop frontend")
    public void subscriptionRenewalOrder() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionRenewal();
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        Map<String,Object> renewalDetails = wooCommerce.getSuccessPage().getSuccessPageTransactionDetails();
        renewalDetails.putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(renewalDetails.get("TID").toString(), wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        wooCommerce.getSuccessPage().verifySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(COMPLETION_ORDER_STATUS);
    }

    @Step("Suspend subscription via shop backend")
    public void subscriptionSuspend() {
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

    @Step("Reactivate subscription via shop backend")
    public void subscriptionReactivate() {
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

    @Step("Change payment of subscription via shop backned")
    public void subscriptionChangePaymentAdmin() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().changePayment(wooCommerce.getTxnInfo().get("PaymentTitle").toString(),wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        String changePaymentTID = wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getTxnInfo().put("ChangePaymentAdminTID", changePaymentTID);
        TID_Helper.verifyTIDInformation(changePaymentTID,"0", TID_STATUS_CONFIRMED, CREDITCARD);
        TID_Helper.verifyPaymentTokenExist(changePaymentTID, true);
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), CREDITCARD);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifySubscriptionOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }

    @Step("Cancel subscription via shop backend")
    public void subscriptionCancel() {
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

    @Step("Capture subscription order via admin")
    public void subscriptionCaptureViaAdmin() {

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().selectOrderStatus(COMPLETION_ORDER_STATUS);
        TID_Helper.verifyTIDStatus(wooCommerce.getTxnInfo().get("TID").toString(), TID_STATUS_CONFIRMED);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CAPTURE_COMMENT_);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifySubscriptionOrderListStatus(SUBSCRIPTION_STATUS_ACTIVE);

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPageStatus(SUBSCRIPTION_STATUS_ACTIVE);
        String nextCycleDate = wooCommerce.getSubscriptionPage().getNextPaymentDate();
        wooCommerce.getTxnInfo().put("SubscriptionNextCycleDate", nextCycleDate);
        TID_Helper.verifySubscriptionNextPaymentDateInTID(wooCommerce.getTxnInfo().get("TID").toString(), nextCycleDate,"yyyy-MM-dd");

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CAPTURE_COMMENT_);
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(SUBSCRIPTION_STATUS_ACTIVE);

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(COMPLETION_ORDER_STATUS);

    }

    @Step("Renewal order of subscription")
    public void subscriptionRenewalCallback() {
        wooCommerce.getCallback().subscriptionRenewal(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String renewalOrder = wooCommerce.getSubscriptionPage().verifyRenewalOrderPresent();
        wooCommerce.getTxnInfo().put("RenewalOrderNumber", renewalOrder);
        wooCommerce.getSubscriptionPage().verifySubscriptionRenewalOrderStatus(COMPLETION_ORDER_STATUS);

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getTxnInfo().putAll(wooCommerce.getOrdersPage().getRenewalOrderDetails());
        wooCommerce.getOrdersPage().VerifyOrderNotesAndCustomerNotesSame();
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(COMPLETION_ORDER_STATUS);

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
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

    @Step("Update subscription amount via callback")
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

    @Step("Update subscription cycle date via callback")
    public void subscriptionCycleDateUpdate() {
        String nextCycleDate = wooCommerce.getTxnInfo().get("SubscriptionNextCycleDate").toString();
        String updatedCycleDate = addDaysFromDate(nextCycleDate,5)+" 00:00:00";
        wooCommerce.getCallback().subscriptionUpdate(wooCommerce.getTxnInfo().get("TID").toString(),wooCommerce.getTxnInfo().get("UpdatedCycleAmount").toString().replaceAll("[^0-9]", ""), updatedCycleDate);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(updatedCycleDate);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(updatedCycleDate);
    }

    @Step("Change subscription payment via callback")
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

    @Step("Cancel subscription via callback")
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



    @Step("Set Shop based subscription configuration")
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


    @Step("Place order Subscription renewal order shop based")
    public void subscriptionRenewalOrderCron() {
        wooCommerce.getSubscriptionPage().loadCronSchedulerPage();
        wooCommerce.getSubscriptionPage().searchCronScheduler(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().runCronOnPayment();

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String renewalOrder = wooCommerce.getSubscriptionPage().verifyRenewalOrderPresent();
        wooCommerce.getTxnInfo().put("RenewalOrderNumber", renewalOrder);
        wooCommerce.getSubscriptionPage().verifySubscriptionRenewalOrderStatus(COMPLETION_ORDER_STATUS);

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getTxnInfo().putAll(wooCommerce.getOrdersPage().getRenewalOrderDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), TID_STATUS_CONFIRMED);
        wooCommerce.getOrdersPage().VerifyOrderNotesAndCustomerNotesSame();
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("RenewalOrderNovalnetComments").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(SUBSCRIPTION_STATUS_ACTIVE);

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(COMPLETION_ORDER_STATUS);

    }


    @Step("Change Subscription payment")
    public void subscriptionChangePayment() {
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

    @Step("Update subscription amount")
    public void subscriptionShopBased_UpdateAmount() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        int updatedCycleAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString())/2;
        String updatedTotalCycleAmount = wooCommerce.getSubscriptionPage().updateSubscriptionAmount(updatedCycleAmount);
        wooCommerce.getTxnInfo().put("updatedSubscriptionCycleAmount", updatedTotalCycleAmount);
    }

    @Step("Place Subscription renewal order shop based using cron")
    public void subscriptionRenewalOrderTwoCron() {
        subscriptionShopBased_UpdateAmount();
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

    @Step("Place subscription renewal order using shop frontend")
    public void subscriptionRenewalOrderThreeFrontend() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionRenewal();
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        wooCommerce.getSuccessPage().verifySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        String renewalTID = wooCommerce.getOrdersPage().getOrderNotesTID(INITIAL_LEVEL_COMMENT_);
        TID_Helper.verifyTIDInformation(renewalTID, wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
    }



    @Step("Proceed payment via Pay now after payment failure")
    public void eighthTestOrder_PayAfterFailure() {
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickFailedOrderPayBtn(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().clickUseNewPaymentMethod();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().verifyCCAuthenticationPageDisplayed(true);
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD);
    }

    @Step("Communication break at CC redirect page and Get Pending order number")
    public String communicationBreakGetOrderNumber(){
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        wooCommerce.getCheckoutPage().waitForCCAuthenticationPage();
        wooCommerce.getMyAccountPage().loadOrders();
        String orderNumber = wooCommerce.getMyAccountPage().getPendingOrderNumber();
        wooCommerce.getTxnInfo().put("OrderNumber",orderNumber);
        return orderNumber;
    }


    @Step("Delete old payment tokens")
    public void deletePaymentTokens(){
        wooCommerce.getMyAccountPage().loadPaymentTokens();
        wooCommerce.getMyAccountPage().deletePaymentTokens();
    }

    @Step("Set Payment Configuration active {0}, payment action {1}, minimum authorize amount {2}, test mode {3}, one click {4}, inline form {5}, enforce3D {6}, paymentType {7}")
    public void setPaymentConfiguration(boolean paymentActive,
                                        String paymentAction,
                                        String minAuthAmount,
                                        boolean testMode,
                                        boolean oneClick,
                                        boolean inlineFrom,
                                        boolean enforce3D){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(
                        paymentActive,
                        paymentAction,
                        minAuthAmount,
                        testMode,
                        oneClick,
                        inlineFrom,
                        enforce3D, null,wooCommerce.getSettingsPage().getPayment(CREDITCARD)));
    }

    @Step("Navigate to checkout by adding product {0} to cart")
    public void navigateCheckout(String productName){
        wooCommerce.getCartPage().load();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        wooCommerce.getProductPage().addProductToCartByName(new String[]{productName});
        wooCommerce.getHomePage().openCheckoutPage();
    }

    @Step("Navigate to checkout by adding product {0} to cart")
    public void navigateCheckoutWithDeleteTokens(String productName){
        wooCommerce.getCartPage().load();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        wooCommerce.getProductPage().addProductToCartByName(new String[]{productName});
        deletePaymentTokens();
        wooCommerce.getHomePage().openCheckoutPage();
    }

    @Step("Place order with cardNumber {3}, expDate {4}, cvc {5}")
    public void placeOrder(boolean isTestMode, boolean isOneClickCheck, boolean isInlineForm, String cardNumber, String expDate, String cvc){
        if (wooCommerce.getCheckoutPage().isCreditCardDisplayed()) {
            wooCommerce.getCheckoutPage().verifyCCTestModeDisplayed(isTestMode);
            wooCommerce.getCheckoutPage().verifySaveCardCheckboxChecked(isOneClickCheck);
            wooCommerce.getCheckoutPage().fillCreditCardForm(cardNumber, expDate, cvc);
            wooCommerce.getCheckoutPage().verifyInlineFormDisplayed(isInlineForm);
            wooCommerce.getCheckoutPage().placeOrder();
            wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
            wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        } else {
            Assert.fail("Credit card payment is not displayed");
        }
    }

    @Step("Place order using CreditCard")
    public void placeOrder(){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
        .setCreditCardPaymentConfiguration(true,"Capture","",true,false,true,false));
        wooCommerce.getHomePage().load();
        wooCommerce.getHomePage().openCartPage();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        wooCommerce.getProductPage().addProductToCartByName(new String[]{"Happy Ninja"});
        wooCommerce.getHomePage().openCheckoutPage();
        if (wooCommerce.getCheckoutPage().isCreditCardDisplayed()) {
            wooCommerce.getCheckoutPage().verifyCCTestModeDisplayed(true);
            wooCommerce.getCheckoutPage().verifySaveCardCheckboxChecked(false);
            wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
            wooCommerce.getCheckoutPage().verifyInlineFormDisplayed(true);
            wooCommerce.getCheckoutPage().placeOrder();
            wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
            wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
            TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        } else {
            Assert.fail("Credit card payment is not displayed");
        }

    }


        // add code for followup
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
            if(orderStaus.equals(COMPLETION_ORDER_STATUS))
                wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
            else
                wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        }

    @Step("Verify the transaction order status and novalnet payment comments appended successfully")
    public void statusCommentsVerificationAfterCommunicationBreak(String orderStaus) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getTxnInfo().put("NovalnetComments",wooCommerce.getOrdersPage().getOrderNoteComment(INITIAL_LEVEL_COMMENT_));
        if(!orderStaus.equals(FAILURE_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        //wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        if(orderStaus.equals(COMPLETION_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStaus);
        if(!orderStaus.equals(FAILURE_ORDER_STATUS))
            wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("NovalnetComments").toString(),wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }

    @Step("Setting up the Novalnet API configuration")
    public void verifyGlobalConfiguration() {
        wooCommerce.getDashBoardPage().loadSettingsPage();
      //  wooCommerce.getDashBoardPage().openSettingsPage();
        wooCommerce.getSettingsPage().openNovalnetGlobalConfig();
        wooCommerce.getSettingsPage().verifyGlobalConfig();
    }

    @Step("Perform partial refund through the shop's backend")
    public void partialRefundAdmin() {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        var refundAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString())/2;
        wooCommerce.getOrdersPage().initiateRefund(refundAmount);
        var newTID = getFirstMatchRegex(getElementText(By.xpath("(//div[@class='note_content'])[1]")), "(?:New TID:)\\s*(\\d{17})");
        TID_Helper.verifyTIDInformation(newTID.replaceAll("[^0-9]", ""), String.valueOf(refundAmount), TID_STATUS_CONFIRMED, CREDITCARD_BOOKBACK);
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(refundAmount));
        var refundComment = getElementText(By.xpath("(//div[@class='note_content'])[1]"));
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(refundComment);
    }

    @Step("Perform chargeback callback event")
    public void chargeback() {
        var response = wooCommerce.getCallback().chargeback(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CHARGEBACK_COMMENT_, wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();
    }

    @Step("Perform credit callback event {0}")
    public void creditEvent(String eventName) {
        var response = wooCommerce.getCallback().credit(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(),eventName);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();
    }

    @Step("Capture Transaction in the shop backend")
    public void captureTransaction() {
        wooCommerce.getOrdersPage().selectOrderStatus(COMPLETION_ORDER_STATUS);
        TID_Helper.verifyTIDStatus(wooCommerce.getTxnInfo().get("TID").toString(), TID_STATUS_CONFIRMED);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CAPTURE_COMMENT_);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CAPTURE_COMMENT_);

    }

    @Step("Cancel Transaction in the shop backend")
    public void cancelTransaction() {
        wooCommerce.getOrdersPage().selectOrderStatus(CANCELLATION_ORDER_STATUS);
        TID_Helper.verifyTIDStatus(wooCommerce.getTxnInfo().get("TID").toString(), TID_STATUS_DEACTIVATED);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CANCEL_COMMENT_);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CANCEL_COMMENT_);
    }

    @Step("Initiate Full Refund in the shop backend ")
    public void fullRefundAdmin() {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        var refundAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().initiateRefund(refundAmount);
        var newTID = getFirstMatchRegex(wooCommerce.getOrdersPage().getOrderNoteComment(REFUND_COMMENT_), "(?:New TID:)\\s*(\\d{17})");
        TID_Helper.verifyTIDInformation(newTID.replaceAll("[^0-9]", ""), String.valueOf(refundAmount), TID_STATUS_CONFIRMED, CREDITCARD_BOOKBACK);
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

    @Step("Perform TRANSACTION_CAPTURE callback event" )
    public void transactionCapture() {
        wooCommerce.getCallback().transactionCapture(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CAPTURE_COMMENT_);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CAPTURE_COMMENT_);
    }

    @Step("Perform TRANSACTION_CANCEL callback event")
    public void transactionCancel() {
        wooCommerce.getCallback().transactionCancel(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), CANCELLATION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CANCEL_COMMENT_);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CANCEL_COMMENT_);
    }



    @Step("Perform Payment Reminder callback event {0}")
    public void paymentReminder(String event) {
        var response = wooCommerce.getCallback().paymentReminder(wooCommerce.getTxnInfo().get("TID").toString(),event);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        if(event.equals(PAYMENT_REMINDER_1))
            wooCommerce.getMyAccountPage().verifyFollowupComments(REMINDER_ONE_COMMENT_);
        else
            wooCommerce.getMyAccountPage().verifyFollowupComments(REMINDER_TWO_COMMENT_);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        if(event.equals(PAYMENT_REMINDER_1))
            wooCommerce.getOrdersPage().verifyOrderNotesComments(REMINDER_ONE_COMMENT_);
        else
            wooCommerce.getOrdersPage().verifyOrderNotesComments(REMINDER_TWO_COMMENT_);
    }

    @Step("Perform SUBMISSION_TO_COLLECTION callback event")
    public void submissionToCollection() {
        var response = wooCommerce.getCallback().submissionToCollection(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesComments(COLLECTION_COMMENT_);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(COLLECTION_COMMENT_);
    }

    @Step("Perform TRANSACTION_REFUND callback event")
    public void transactionRefund(int refundAmount) {
        int totalAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString());
        String status;
        if(refundAmount < totalAmount)
            status = COMPLETION_ORDER_STATUS;
        else
            status = REFUND_ORDER_STATUS;
        var response = wooCommerce.getCallback().transactionRefund(wooCommerce.getTxnInfo().get("TID").toString(),String.valueOf(refundAmount));
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(refundAmount));
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupComments(REFUND_COMMENT_);
    }


}
