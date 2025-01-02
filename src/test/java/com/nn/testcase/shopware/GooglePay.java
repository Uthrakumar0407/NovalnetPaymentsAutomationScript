package com.nn.testcase.shopware;

import com.nn.apis.ShopwareAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.GooglePayCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.pages.shopware.base.Shopware;
import com.nn.pages.shopware.base.ShopwareOrderStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.GOOGLEPAY;
import static com.nn.language.NovalnetCommentsEN.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.ENF_3D_GPAY;
import static com.nn.utilities.DriverActions.verifyContains;
import static com.nn.utilities.DriverActions.verifyEquals;
import static com.nn.utilities.GooglePayHelper.launchDriverWithGmail;
import static com.nn.utilities.ShopwareUtils.*;
import static com.nn.utilities.ShopwareUtils.ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE;

public class GooglePay extends BaseTest {
    Shopware shopwareGpay = Shopware.builder()
            .callback(new GooglePayCallbackEvents())
            .build();

    @BeforeClass
    public void setup() {
        DriverManager.quit();
        launchDriverWithGmail();
        //"novalnetesolutions2010@gmail.com"
        ShopwareAPIs.getInstance().createCustomer(GOOGLEPAY);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());

    }
    @AfterMethod
    public void clear() {
        ShopwareAPIs.getInstance().clearCart();
    }

    @Test(priority = 1,  description = "Check whether the test transaction is successful with payment action set to Capture, verifying token exist in the response, partial refund admin, chargeback and credit events ")
    public void firstOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, CAPTURE,
                ENF_3D_GPAY, false
        ));
               ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
               shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GOOGLEPAY);
        TID_Helper.verifyPaymentTokenExist(tid, true);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Refund partial
        String refundAmount = String.valueOf(Integer.parseInt(totalAmount) / 2);
        shopware.getOrdersPage().proceedRefund(refundAmount);
        verifyContains(shopware.getOrdersPage().getComments(), getRefundComment(orderNumber), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), true, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getRefundComment(orderNumber), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //chargeback
        var chargebackComment = shopwareGpay.getCallback().chargeback(tid, totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CHARGEBACK.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(chargebackComment), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CHARGEBACK.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(chargebackComment), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Representment
        var representmentComment = shopwareGpay.getCallback().googlePayRepresentment(tid, totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CHARGEBACK.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(representmentComment), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CHARGEBACK.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(representmentComment), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 2, description = "Check whether the test order is successful with payment action set to Authorize, 'enforce 3d outside EU' set to true, verifying redirection takes place for outside EU country, performing capture in shop backend and full refund via shop backend")
//,retryAnalyzer = RetryListener.class)
    public void secondOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                ENF_3D_GPAY, true // need to verify Enforce 3d
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingCountry("United States of America");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Capture
        shopware.getOrdersPage().capture();
        TID_Helper.verifyTIDStatus(tid, TID_STATUS_CONFIRMED);
        verifyContains(shopware.getOrdersPage().getComments(), getCaptureComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), true, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCaptureComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Refund
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber).proceedRefund();
        String newTID = shopware.getOrdersPage().getNewTID();
        verifyContains(shopware.getOrdersPage().getComments(), getRefundComment(orderNumber), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getRefundComment(orderNumber), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }
    @Test(priority = 3, description = "Check whether test order with authorize minimum amount less than order amount is successful" +
            ", execute transaction capture event, execute transaction refund event partial order amount")
//,retryAnalyzer = RetryListener.class)
    public void thirdOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                ENF_3D_GPAY, false,
                MIN_AUTH_AMOUNT, 1000
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Capture via callback
        var captureComment = shopwareGpay.getCallback().transactionCapture(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(captureComment), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(captureComment), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Refund partial via callback
        var partialRefund = shopwareGpay.getCallback().transactionRefund(tid, String.valueOf(Integer.parseInt(totalAmount) / 2));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(partialRefund), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(partialRefund), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 4, description = "Check whether the test order with authorize minimum amount greater than order amount is successful," +
            "  execute reminder and collection events.")//,retryAnalyzer = RetryListener.class)
    public void fourthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "6000",
                ENF_3D_GPAY, false
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GOOGLEPAY);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //paymentReminderOne
        var paymentReminderOne = shopwareGpay.getCallback().paymentReminderOne(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(paymentReminderOne), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(paymentReminderOne), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //paymentReminderTwo
        var paymentReminderTwo = shopwareGpay.getCallback().paymentReminderTwo(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(paymentReminderTwo), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(paymentReminderTwo), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //submissionToCollection
        var submissionToCollection = shopwareGpay.getCallback().submissionToCollection(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(submissionToCollection), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(submissionToCollection), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 5, description = "Check whether the authorize test order is cancelled via shop backend with payment action set to Authorize")
//,retryAnalyzer = RetryListener.class)
    public void fifthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "",
                ENF_3D_GPAY, false
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Cancel
        shopware.getOrdersPage().cancel();
        TID_Helper.verifyTIDStatus(tid, TID_STATUS_DEACTIVATED);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 6, description = "Check whether the authorize test order is cancelled via callback with payment action set to Authorize")
//,retryAnalyzer = RetryListener.class)
    public void sixthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "",
                ENF_3D_GPAY, false,
                BUSINESS_NAME, "Autoamtion@123"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .isBusinessNameDisplayed("Autoamtion@123", true)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Cancel via callback
        var cancelFromAdmin = shopwareGpay.getCallback().transactionCancel(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(cancelFromAdmin), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(cancelFromAdmin), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 7, description = "Check whether the test transaction is successful with guest user ")//,retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,CAPTURE,
                ENF_3D_GPAY,false
        ));
        shopware.getMyAccountPage().addProductToCart(SW_PRODUCT_GUEST_01);
        shopware.getCustomerLoginPage().guestRegister("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GOOGLEPAY);
        TID_Helper.verifyPaymentTokenExist(tid, true);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }
    @Test(priority = 8, dataProvider = "googlePayCurrencies",description = "Check whether the test transaction is successful with multiple currencies")
    public void multiCurrencyOrder(String currency){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(GOOGLEPAY);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeCurrency(currency);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount().replaceAll("^0+","");
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        verifyEquals(TID_Helper.getTIDCurrency(tid),currency,"Verify tid currency ");
        verifyEquals(TID_Helper.getTIDAmount(tid),totalAmount);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
    }
    @DataProvider
    public Object[][] googlePayCurrencies(){
        return new Object[][]{{"CHF"},{"GBP"}, {"INR"}, {"JPY"},{"PLN"},{"USD"},{"RUB"}};
    }
    @Test(priority = 17, description = "Verify download button and download link are not displayed in front-end orders page when the transaction is in 100 status")
    public void downloadProductConfirmOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, CAPTURE,
                MIN_AUTH_AMOUNT, "",
                ENF_3D_GPAY, false
        ));
        ShopwareAPIs.getInstance().createCustomer(GOOGLEPAY);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_DIGITAL_PRODUCT1);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GOOGLEPAY);
        TID_Helper.verifyPaymentTokenExist(tid, true);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), true, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), true, DOWNLOAD_LINK);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 21, description = "Verify download button and download link are not displayed in front-end orders page when the transaction is captured from on-hold")
    public void downloadProductOnHoldToConfirmOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "",
                ENF_3D_GPAY, false
        ));
        ShopwareAPIs.getInstance().createCustomer(GOOGLEPAY);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_DIGITAL_PRODUCT1);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Capture
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber).capture();
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), true, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), true, DOWNLOAD_LINK);
    }
    @Test(priority = 21, description = "Verify download button and download link are not displayed in front-end orders page when the transaction is cancelled from on-hold")
    public void downloadProductOnHoldToCancelOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "",
                ENF_3D_GPAY, false
        ));
        ShopwareAPIs.getInstance().createCustomer(GOOGLEPAY);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_DIGITAL_PRODUCT1);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Cancel
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber).cancel();
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
    }
    @Test(priority = 22, description = "Check whether the zero amount booking order is placed successfully, verify token exist in the response and amount booked in the shop backend")
    public void zeroAmountOrder() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE_ZERO_AMOUNT,
                MIN_AUTH_AMOUNT, "",
                ENF_3D_GPAY, false
        ));
        ShopwareAPIs.getInstance().createCustomer(GOOGLEPAY);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GOOGLEPAY)
                .clickGooglePayButton()
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().payWithGooglePay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_CONFIRMED, GOOGLEPAY);
        TID_Helper.verifyPaymentTokenExist(tid);
        verifyEquals(initialComment, getOrderSuccessCommentForZeroAmount(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GOOGLEPAY), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //bookAmount
        shopware.getOrdersPage().bookAmount(String.valueOf(Integer.parseInt(totalAmount)));
        verifyContains(shopware.getOrdersPage().getComments(), getZeroAmountBookedComment(orderNumber), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), true, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getZeroAmountBookedComment(orderNumber), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }
}