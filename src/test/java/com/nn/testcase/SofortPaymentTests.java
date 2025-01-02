package com.nn.testcase;

import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.IdealCallbackEvents;
import com.nn.callback.SofortCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.testcase.shopware.Sofort;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nn.callback.CallbackProperties.IDEAL;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.*;


public class SofortPaymentTests extends BaseTest {

    private By sofortCookieDenyAlert = By.xpath("//div[@id='Modal']//button[@class='cookie-modal-deny-all button-tertiary']");
    private By sofortDemoBank = By.xpath("//p[@class='description' and @data-label='demo-bank']");


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
            .callback(new SofortCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("sofort") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }

    @Test(priority = 1, description = "Check whether the Sofort payment order placed successully and chargeback, credit and refund events executed successfully",retryAnalyzer = RetryListener.class)
    public void callbackEventOrder(){
        setPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isOnlineTransferDisplayed();
        wooCommerce.getCheckoutPage().verifyOnlineTransferTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithOnlineTransfer();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, ONLINE_TRANSFER);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName,paymentComments,tid);
        wooCommerce.getCallback().chargeback(tid,orderAmount);
        statusCommentsVerification(orderNumber,CHARGEBACK_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().bankTransferByEndCustomer(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().debtCollectionDE(tid,orderAmount);
        statusCommentsVerification(orderNumber,CREDIT_COMMENT_,orderAmount,COMPLETION_ORDER_STATUS);
        wooCommerce.getCallback().transactionRefund(tid,String.valueOf( (Integer.parseInt(orderAmount)/2)));
        statusCommentsVerification(orderNumber,REFUND_COMMENT_,String.valueOf( (Integer.parseInt(orderAmount)/2)),COMPLETION_ORDER_STATUS);

    }

    @Test(priority = 2, description = "Check whether Sofort payment order placed successfully by communication break and executing payment event",retryAnalyzer = RetryListener.class)
    public void successOrder(){
        setPaymentConfiguration(true,true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isOnlineTransferDisplayed();
        wooCommerce.getCheckoutPage().verifyOnlineTransferTestModeDisplayed(true);
        Map<String, String> orderTIDDetails= communicationBreakGetTIDPendingOrderNumber();
        wooCommerce.getCallback().communicationBreakSuccess(orderTIDDetails.get("tidValue"),orderTIDDetails.get("orderNumber"),"100");
        DriverManager.getDriver().navigate().refresh();
        statusCommentsVerificationAfterCommunicationBreak(COMPLETION_ORDER_STATUS);
    }

    @Test(priority = 3, description = "Check whether Sofort payment order failed successfully by communication break and executing payment event",retryAnalyzer = RetryListener.class)
    public void failureOrder(){
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isOnlineTransferDisplayed();
        Map<String, String> orderTIDDetails= communicationBreakGetTIDPendingOrderNumber();
        wooCommerce.getCallback().communicationBreakFailure(orderTIDDetails.get("tidValue"),orderTIDDetails.get("orderNumber"),"100");
        DriverManager.getDriver().navigate().refresh();
        statusCommentsVerificationAfterCommunicationBreak(FAILURE_ORDER_STATUS);
    }
    @Test(priority = 4, description = "Check whether the test transaction is successful with guest user ")
    public void guestOrder(){
        setPaymentConfiguration(true,true);
        wooCommerce.getDashBoardPage().adminLogout();
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isOnlineTransferDisplayed();
        wooCommerce.getCheckoutPage().placeOrderWithOnlineTransfer();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getGuestOrderSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, ONLINE_TRANSFER);
        wooCommerce.getAdminPage().adminLogin("sofort", "wordpress");
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }
    @Test(priority = 5,description = "Check whether sofort payment logo displayed ")
    public void verifySofortLogoDisplayed(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("sofort","wordpress");
        setPaymentConfiguration(true,true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isOnlineTransferDisplayed();
        wooCommerce.getCheckoutPage().isLogoDisplayed("Sofort");
    }

    @Test(priority = 6, description = "Verify the display of error text on the checkout page in case a transaction is aborted, leading to a change in the order status to \"failed\".",retryAnalyzer = RetryListener.class)
    public void transactionAbort() {
        setPaymentConfiguration(true,true);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isOnlineTransferDisplayed();
        wooCommerce.getCheckoutPage().verifyOnlineTransferTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        String tid=cancelAtSofortPaymentRedirection();
        //verifyCheckoutErrorMessage(REDIRECT_END_USER_CANCEL_ERROR);
        var errorMessage = wooCommerce.getCheckoutPage().getCheckoutPaymentError();
        verifyEquals(errorMessage,REDIRECT_END_USER_CANCEL_ERROR,"Verify checkout validation error message");
        wooCommerce.getMyAccountPage().loadOrders();
        String orderNumber = wooCommerce.getMyAccountPage().getFailedOrderNumber();
        verifyEquals(NovalnetAPIs.getRecentTransactionTID(orderNumber), tid.trim(),"Transaction ID Validation");
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_FAILURE, ONLINE_TRANSFER);
        statusCommentsVerificationAfterTransactionAbort(orderNumber,FAILURE_ORDER_STATUS);

    }
    @Test(priority = 7, description = "Verify that payment is hidden on the checkout page when payment is disabled in shop backend")
    public void deactivatePaymentVerifyCheckout(){
        wooCommerce.getAdminPage().ifLoggedOutLogin("sofort","wordpress");
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment(ONLINE_TRANSFER,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isPaymentDisplayed(ONLINE_TRANSFER,false);
    }

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
        wooCommerce.getCallback().credit(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(),eventName);
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

    @Step("Communication break at Sofort redirect page and Get Pending order number")
    public  Map<String, String> communicationBreakGetTIDPendingOrderNumber(){
        Map<String, String> tidOrderDetails = new HashMap<>();
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        wooCommerce.getCheckoutPage().waitForSofortRedirectionPage();
        wooCommerce.getCheckoutPage().rejectSofortBankCookiePopup();
        String tid= getSofortTID();
        wooCommerce.getMyAccountPage().loadOrders();
        String orderNumber = wooCommerce.getMyAccountPage().getPendingOrderNumber();
        tidOrderDetails.put("orderNumber", orderNumber);
        tidOrderDetails.put("tidValue", tid.trim());
        wooCommerce.getTxnInfo().put("OrderNumber",orderNumber);
        return tidOrderDetails;

    }

    @Step("Communication break at Sofort redirect page and Get Pending order number")
    public  Map<String, String> communicationBreakGetTIDFailedOrderNumber(){
        Map<String, String> tidOrderDetails = new HashMap<>();
        wooCommerce.getCheckoutPage().clickPlaceOrderBtn();
        wooCommerce.getCheckoutPage().waitForSofortRedirectionPage();
        wooCommerce.getCheckoutPage().rejectSofortBankCookiePopup();
        String tid= getSofortTID();
        wooCommerce.getMyAccountPage().loadOrders();
        String orderNumber = wooCommerce.getMyAccountPage().getFailedOrderNumber();
        tidOrderDetails.put("orderNumber", orderNumber);
        tidOrderDetails.put("tidValue", tid.trim());
        wooCommerce.getTxnInfo().put("OrderNumber",orderNumber);
        return tidOrderDetails;

    }
    @Step("Get TID of Sofort payment ")
    public String getSofortTID() {
         By sofortCookieDenyAlert = By.xpath("//div[@id='Modal']//button[@class='cookie-modal-deny-all button-tertiary']");
        if(checkElementDisplayed(sofortCookieDenyAlert)){clickElementWithJs(sofortCookieDenyAlert);}
        By tIDLink = By.xpath("//p[contains(@class, 'amount') and contains(@class, 'js-toggle-details') and @data-currency='€']");
        clickElementWithJs(tIDLink);
        waitForElementVisible(By.xpath("//p[@class='reason'][1]"));
        WebElement tdIDelement = DriverManager.getDriver().findElement(By.xpath("//p[@class='reason'][1]"));
        String tidValue = tdIDelement.getText();
        String patternString = "\\b(\\d+)\\b";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(tidValue);
        if (matcher.find());
        String result = matcher.group(1);
        return  result;
    }

    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(
                        paymentActive,"","",testMode,null,null,null, null,wooCommerce.getSettingsPage().getPayment(ONLINE_TRANSFER)));
    }

    @Step("Setting up the Novalnet API configuration")
    public void verifyGlobalConfiguration() {
        wooCommerce.getDashBoardPage().loadSettingsPage();
        //wooCommerce.getDashBoardPage().openSettingsPage();
        wooCommerce.getSettingsPage().openNovalnetGlobalConfig();
        wooCommerce.getSettingsPage().verifyGlobalConfig();
    }

    @Step("Abort order with Sofort Payment ")
    public String cancelAtSofortPaymentRedirection() {
        waitForSofortRedirectionPage();
        rejectSofortBankCookiePopup();
        String TID = getSofortTID();
        clickElementByRefreshing(By.cssSelector(".back-to-merchant.cancel-transaction"));
        clickElementByRefreshing(By.cssSelector("#CancelTransaction"));
        return TID;
    }
    @Step("Verify is Sofort payment redirected")
    public void waitForSofortRedirectionPage() {
        waitForURLToBe("https://www.sofort.com/", 120);
    }

    @Step("Close Sofort Bank Cookie popup")
    public void rejectSofortBankCookiePopup() {
        waitForURLToBe("https://www.sofort.com/", 120);
        if (waitForElementVisible(sofortCookieDenyAlert,3,"")) {
            sleep(2);
            clickElementWithJs(sofortCookieDenyAlert);
            //waitForElementVisible(sofortCookieDenyAlert);
            waitForElementVisible(sofortDemoBank, 45);
            waitForElementClickable(sofortDemoBank);
            clickElementWithJs(sofortDemoBank);
        } else {
            waitForElementVisible(sofortDemoBank, 45);
            if (checkElementDisplayed(sofortCookieDenyAlert)) {
                clickElementWithJs(sofortCookieDenyAlert);
            }
            waitForElementClickable(sofortDemoBank);
            clickElementWithJs(sofortDemoBank);
        }

    }


    @Step("Verify validation error message at checkout")
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
