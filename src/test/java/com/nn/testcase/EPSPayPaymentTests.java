package com.nn.testcase;

import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.EPSCallbackEvents;
import com.nn.callback.GiroPayCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.HashMap;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.*;
import static com.nn.utilities.DriverActions.clickElement;


public class EPSPayPaymentTests extends BaseTest {

    private By placeOrderBtn = By.cssSelector("button#novalnetPay_submit");
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
            .callback(new EPSCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("eps") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }

    @Test(priority = 1, description = "Check whether the eps  payment order placed successully and chargeback, credit and refund events executed successfully",retryAnalyzer = RetryListener.class)
    public void callbackEventOrder(){
        verifyGlobalConfiguration();
        setPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isEPSDisplayed();
        wooCommerce.getCheckoutPage().verifyEPSTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithEPS();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, EPS);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName,paymentComments,tid);
        wooCommerce.getCallback().transactionRefund(tid,String.valueOf( (Integer.parseInt(orderAmount)/2)));
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,String.valueOf( (Integer.parseInt(orderAmount)/2)),COMPLETION_ORDER_STATUS);

    }

    @Test(priority = 2, description = "Check whether eps payment order placed successfully by communication break and executing payment event",retryAnalyzer = RetryListener.class)
    public void successOrder(){
        setPaymentConfiguration(true,true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isEPSDisplayed();
        wooCommerce.getCheckoutPage().verifyEPSTestModeDisplayed(true);
        String orderDetails= communicationBreakGetPendingOrderNumber();
        wooCommerce.getCallback().communicationBreakSuccess(wooCommerce.getTxnInfo().get("TID").toString(),orderDetails,"");
        statusCommentsVerificationAfterCommunicationBreak(COMPLETION_ORDER_STATUS);

    }

    @Test(priority = 3, description = "Check whether eps payment order failed successfully by communication break and executing payment event",retryAnalyzer = RetryListener.class)
    public void failureOrder(){
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isEPSDisplayed();
        String orderDetails= communicationBreakGetPendingOrderNumber();
        wooCommerce.getCallback().communicationBreakFailure(wooCommerce.getTxnInfo().get("TID").toString(),orderDetails,"");
        statusCommentsVerificationAfterCommunicationBreak(FAILURE_ORDER_STATUS);
    }
    @Test(priority = 4, description = "Check whether the test transaction is successful with guest user ")
    public void guestOrder(){
        setPaymentConfiguration(true,true);
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Austria");
        wooCommerce.getCheckoutPage().isEPSDisplayed();
        wooCommerce.getCheckoutPage().placeOrderWithEPS();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getGuestOrderSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, EPS);
        wooCommerce.getAdminPage().adminLogin("eps", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }
    @Test(priority = 5,description = "Check whether eps payment logo displayed ")
    public void verifyEPSLogoDisplayed(){
      //  verifyGlobalConfiguration();
        wooCommerce.getAdminPage().ifLoggedOutLogin("eps","wordpress");
        setPaymentConfiguration(true,true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isEPSDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("eps");
    }

    @Test(priority = 6, description = "Verify the display of error text on the checkout page in case a transaction is aborted, leading to a change in the order status to \"failed\".",retryAnalyzer = RetryListener.class)
    public void transactionAbort() {
        setPaymentConfiguration(true,true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isEPSDisplayed();
        wooCommerce.getCheckoutPage().verifyEPSTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        cancelAtNewEPSRedirection();
        //verifyCheckoutErrorMessage(EPS_END_USER_CANCEL_ERROR);
        var errorMessage = wooCommerce.getCheckoutPage().getCheckoutPaymentError();
        verifyEquals(errorMessage,EPS_END_USER_CANCEL_ERROR,"Verify checkout validation error message");
        wooCommerce.getMyAccountPage().loadOrders();
        String orderNumber = wooCommerce.getMyAccountPage().getFailedOrderNumber();
        String tid=NovalnetAPIs.getRecentTransactionTID(orderNumber);
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_FAILURE, EPS);
        statusCommentsVerificationAfterTransactionAbort(orderNumber,FAILURE_ORDER_STATUS);

    }
    @Test(priority = 7, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("eps","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(EPS,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(EPS,false);
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


    @Step("Perform TRANSACTION_REFUND callback event")
    public void transactionRefund(int refundAmount, String status) {
        int totalAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("TotalAmount").toString());
        var response = wooCommerce.getCallback().transactionRefund(wooCommerce.getTxnInfo().get("TID").toString(),String.valueOf(refundAmount));
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(refundAmount));
        var refundComment = wooCommerce.getOrdersPage().getOrderNoteComment(REFUND_COMMENT_);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupComments(refundComment);
    }

    @Step("Communication break at eps redirect page and Get Pending order number")
    public  String communicationBreakGetPendingOrderNumber(){
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        wooCommerce.getCheckoutPage().waitForEPSPayRedirectionPage();
        wooCommerce.getMyAccountPage().loadOrders();
        String orderNumber = wooCommerce.getMyAccountPage().getPendingOrderNumber();
        wooCommerce.getTxnInfo().put("OrderNumber",orderNumber);
        return orderNumber;

    }


    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setEPSPaymentConfiguration(
                        paymentActive,
                        testMode));
    }

    @Step("Setting up the Novalnet API configuration")
    public void verifyGlobalConfiguration() {
        wooCommerce.getDashBoardPage().loadSettingsPage();
        //wooCommerce.getDashBoardPage().openSettingsPage();
        wooCommerce.getSettingsPage().openNovalnetGlobalConfig();
        wooCommerce.getSettingsPage().verifyGlobalConfig();
    }

    @Step("Cancel at EPS redirect page")
    public void cancelAtNewEPSRedirection() {
        By backToBank = By.xpath("//button[text()='Zurück zur Bankensuche']");
        By abortPayment = By.xpath("//button[text()='Zahlung abbrechen']");
        waitForNewEPSRedirectionPage();
        By epsCookie = By.xpath("//button[contains(text(), 'Alle Cookies akzeptieren')]");
        if (checkElementDisplayed(epsCookie)) {
            clickElement(epsCookie);
            clickElement(abortPayment);
        }else {
            clickElement(backToBank);
            waitForElementVisible(abortPayment);
            clickElement(abortPayment);
        }
    }
    @Step("Click Place Order Button")
    public void clickPlaceOrderBtn() {
        switchToDefaultContent();
        waitForElementClickable(placeOrderBtn);
        clickElementWithJs(placeOrderBtn);
        sleep(5); // to be discussed with Nagesh

    }
    @Step("Verify is EPS payment redirected")
    public void waitForEPSRedirectionPage() {
        waitForURLToBe("https://ftg-customer-integration.giropay.de/",120);
    }
    @Step("Verify is EPS payment redirected")
    public void waitForNewEPSRedirectionPage() {
        waitForURLToBe("https://sandbox.paydirekt.de/eps-checkout",120);
    }

    public void verifyCheckoutErrorMessage(String expected){
        var actual = getElementText(By.cssSelector(".woocommerce-error li")).trim();
        verifyEquals(actual,expected,"Verify checkout validation error message");
    }

    @Step("Verify the transaction order status and novalnet payment comments appended successfully - Transaction Abort")
    public void statusCommentsVerificationAfterTransactionAbort( String orderNumber,String orderStaus) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber, orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(REDIRECT_END_USER_CANCEL_ERROR);
    }

}
