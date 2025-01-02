package com.nn.testcase.shopware;

import com.nn.apis.ShopwareAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.Przelewy24CallbackEvents;
import com.nn.listeners.RetryListener;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.pages.shopware.base.Shopware;
import com.nn.pages.shopware.base.ShopwareOrderStatus;
import com.nn.reports.ExtentTestManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.PRZELEWY24;
import static com.nn.language.NovalnetCommentsEN.*;
import static com.nn.language.NovalnetCommentsEN.getCancelComment;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.DriverActions.verifyContains;
import static com.nn.utilities.DriverActions.verifyEquals;
import static com.nn.utilities.ShopwareUtils.*;

public class Przelewy24 extends BaseTest {

    Shopware shopwareP24 = Shopware.builder()
            .callback(new Przelewy24CallbackEvents())
            .build();

    @BeforeClass
    public void setup() {
        ExtentTestManager.saveToReport("setup", "Setting up prerequisite to place order");
        ShopwareAPIs.getInstance().createCustomer(PRZELEWY24);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeCurrency("PLN");
        shopware.getLoginPage().load().login();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PRZELEWY24, true);
    }

    @AfterMethod
    public void clear() {
        ShopwareAPIs.getInstance().clearCart();
    }

    @Test(priority = 1, description = "Check whether the PRZELEWY24 payment order placed successfully and partial refund via callback executed successfully")
    public void firstOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PRZELEWY24, Map.of(
                TESTMODE, false
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PRZELEWY24)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithPrzelewy(false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, PRZELEWY24);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Refund partial via callback
        var partialRefund = shopwareP24.getCallback().transactionRefund(tid, String.valueOf(Integer.parseInt(totalAmount) / 2));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), REFUND_COMMENT_, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), REFUND_COMMENT_, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        
    }

    @Test(priority = 2, description = "Check whether the PRZELEWY24 payment order placed successfully and full refund events executed successfully")
    public void secondOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PRZELEWY24, Map.of(
                TESTMODE, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PRZELEWY24)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithPrzelewy(false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, PRZELEWY24);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Full refund via callback
        var refund = shopwareP24.getCallback().transactionRefund(tid, totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), REFUND_COMMENT_, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), REFUND_COMMENT_, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }

    @Test(priority = 3, description = "Check whether the PRZELEWY24 payment order placed successfully with pending status & execute transaction update pending to confirmed")
    public void thirdOrder(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PRZELEWY24)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithPrzelewy(true);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, PRZELEWY24);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Update event
        var updateComment = shopwareP24.getCallback().transactionUpdateStatus(tid, TID_STATUS_CONFIRMED);
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(updateComment), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(updateComment), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }


    @Test(priority = 4, description = "Check whether the PRZELEWY24 payment order placed successfully with pending status & execute transaction update pending to canceled")
    public void fourthOrder(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PRZELEWY24)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithPrzelewy(true);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, PRZELEWY24);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Cancel callback event
        shopwareP24.getCallback().transactionCancel(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }

    @Test(priority = 5, description = "Check whether the PRZELEWY24 payment order placed successfully by communication break")
    public void fifthOrder(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(PRZELEWY24);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeCurrency("PLN");
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PRZELEWY24)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().waitForPrzelewyRedirection();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = shopware.getNovalnetAdminPortal().getTID(ShopwareAPIs.getInstance().getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        shopwareP24.getCallback().communicationBreakSuccess(tid, totalAmount);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), true, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().getComments(), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 6, description = "Check whether the PRZELEWY24 payment order placed & failed by communication break")
    public void sixthOrder(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(PRZELEWY24);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeCurrency("PLN");
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PRZELEWY24)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().waitForPrzelewyRedirection();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = shopware.getNovalnetAdminPortal().getTID(ShopwareAPIs.getInstance().getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        shopwareP24.getCallback().communicationBreakFailure(tid, totalAmount);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), getCommunicationFailureComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().getComments(), getCommunicationFailureComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 7, description = "Check whether the test transaction is successful with guest user")
    public void guestOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getMyAccountPage().changeCurrency("PLN");
        shopware.getMyAccountPage().addProductToCart(SW_PRODUCT_GUEST_01);
        shopware.getCustomerLoginPage().guestRegister("Poland");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PRZELEWY24)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithPrzelewy(false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, PRZELEWY24);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(PRZELEWY24), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }
}
