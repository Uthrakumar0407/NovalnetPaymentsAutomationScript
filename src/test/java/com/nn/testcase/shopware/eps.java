package com.nn.testcase.shopware;

import com.nn.apis.MagentoAPIs;
import com.nn.apis.ShopwareAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.EPSCallbackEvents;
import com.nn.callback.EPSCallbackEvents;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.pages.shopware.base.Shopware;
import com.nn.pages.shopware.base.ShopwareOrderStatus;
import com.nn.reports.ExtentTestManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.TID_STATUS_CONFIRMED;
import static com.nn.language.NovalnetCommentsEN.*;
import static com.nn.language.NovalnetCommentsEN.getOrderSuccessComment;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.DriverActions.verifyContains;
import static com.nn.utilities.DriverActions.verifyEquals;
import static com.nn.utilities.ShopwareUtils.*;
import static com.nn.utilities.ShopwareUtils.ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE;

public class eps extends BaseTest {

    Shopware shopwareEps = Shopware.builder()
            .callback(new EPSCallbackEvents())
            .build();

    @BeforeClass
    public void setup() {
        ExtentTestManager.saveToReport("setup", "Setting up prerequisite to place order");
        ShopwareAPIs.getInstance().createCustomer(EPS);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getLoginPage().load().login();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(EPS, true);
    }

    @AfterMethod
    public void clear() {
        ShopwareAPIs.getInstance().clearCart();
    }

    @Test(priority = 1, description = "Check whether the EPS payment order placed successfully and refund, chargeback, and credit events executed successfully")
    public void firstOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(EPS, Map.of(
                TESTMODE, false
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(EPS)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithEPS();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, EPS);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(EPS), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(EPS), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(EPS), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Refund partial via callback
        var partialRefund = shopwareEps.getCallback().transactionRefund(tid, String.valueOf(Integer.parseInt(totalAmount) / 2));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(partialRefund), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(partialRefund), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //onlineTransferCredit
        var onlineTransferCredit = shopwareEps.getCallback().onlineTransferCredit(tid, totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(onlineTransferCredit), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(onlineTransferCredit), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 2, description = "Check whether the EPS payment order placed successfully and full refund events executed successfully")//,retryAnalyzer = RetryListener.class)
    public void secondOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(EPS, Map.of(
                TESTMODE, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(EPS)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithEPS();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, EPS);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(EPS), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(EPS), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(EPS), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Full refund via callback
        var refund = shopwareEps.getCallback().transactionRefund(tid, totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(refund), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(refund), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 3, description = "Check whether the EPS payment order placed successfully through communication break")
    public void thirdOrder(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(EPS);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(EPS)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().waitForEPSRedirectionPage();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = shopware.getNovalnetAdminPortal().getTID(ShopwareAPIs.getInstance().getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        shopwareEps.getCallback().communicationBreakSuccess(tid, totalAmount);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(EPS), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(EPS), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), true, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().getComments(), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }
    @Test(priority = 4, description = "Check whether the EPS payment order placed & failed through communication break")
    public void fourthOrder(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(EPS);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(EPS)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().waitForEPSRedirectionPage();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = shopware.getNovalnetAdminPortal().getTID(ShopwareAPIs.getInstance().getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        shopwareEps.getCallback().communicationBreakFailure(tid, totalAmount);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(EPS), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(EPS), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), getCommunicationFailureComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().getComments(), getCommunicationFailureComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }
    @Test(priority = 5, description = "Verify the display of error text on the checkout page in case a transaction is aborted, leading to a change in the order status to failed")//,retryAnalyzer = RetryListener.class)
    public void fifthOrder(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(EPS);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(EPS)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().cancelAtEPSRedirection();
        verifyEquals(!shopware.getCheckoutPage().getCheckoutError().isEmpty(), true, "Verify end user cancelled error message in checkout page");
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = shopware.getNovalnetAdminPortal().getTID(ShopwareAPIs.getInstance().getCustomerEmail());
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_FAILURE, EPS);
    }
    @Test(priority = 6, description = "Check whether the test transaction is successful with guest user")
    public void guestOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getMyAccountPage().addProductToCart(SW_PRODUCT_GUEST_01);
        shopware.getCustomerLoginPage().guestRegister("Austria");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(EPS)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithEPS();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, EPS);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(EPS), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }
}
