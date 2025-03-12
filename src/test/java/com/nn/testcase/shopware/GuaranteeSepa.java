package com.nn.testcase.shopware;

import com.nn.apis.ShopwareAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.SEPAGuaranteeCallbackEvents;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.pages.shopware.base.Shopware;
import com.nn.pages.shopware.base.ShopwareOrderStatus;
import com.nn.reports.ExtentTestManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.language.NovalnetCommentsEN.*;
import static com.nn.language.NovalnetCommentsEN.getCaptureComment;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.MIN_ORDER_AMOUNT;
import static com.nn.utilities.DriverActions.verifyContains;
import static com.nn.utilities.DriverActions.verifyEquals;
import static com.nn.utilities.ShopwareUtils.*;
import static com.nn.utilities.ShopwareUtils.ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE;

public class GuaranteeSepa extends BaseTest {
    Shopware shopwareGS = Shopware.builder()
            .callback(new SEPAGuaranteeCallbackEvents())
            .build();

    @BeforeClass
    public void setup() {
        ExtentTestManager.saveToReport("setup", "Setting up prerequisite to place order");
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_INVOICE);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getLoginPage().load().login();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GUARANTEED_DIRECT_DEBIT_SEPA, true);
    }

    @AfterMethod
    public void clear() {
        ShopwareAPIs.getInstance().clearCart();
    }

    @Test(priority = 1, description = "Check whether the capture is set, order placed successfully, verify token exist in the response, full refund shop backend")
//, retryAnalyzer = RetryListener.class)
    public void firstOrder() {
//        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
//        shopware.getNovalnetAdminPortal().loadAutomationProject();
//        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
//                TESTMODE, false,
//                PAYMENT_ACTION, CAPTURE,
//                ALLOW_B2B, true,
//                FORCE_NON_GUARANTEE, true,
//                MIN_ORDER_AMOUNT, "999"
//        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA,2);
        TID_Helper.verifyPaymentTokenExist(tid);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Full refund from shop admin
        shopware.getOrdersPage().proceedRefund(totalAmount);
        verifyContains(shopware.getOrdersPage().getComments(), getRefundComment(totalAmount, tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE); //getRefundComment
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getRefundComment(totalAmount, tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 2, description = "Check whether the authorize is set, verify masked iban displayed, order placed using masked iban token, capture and partial refund in shop backend ")
//,retryAnalyzer = RetryListener.class)
    public void secondOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "",
                ALLOW_B2B, true,
                FORCE_NON_GUARANTEE, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifyMaskedSEPAData(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid, false);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
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
        //Partial refund from shop admin
        String refundAmount = String.valueOf(Integer.parseInt(totalAmount) / 2);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber).proceedRefund(refundAmount);
        verifyContains(shopware.getOrdersPage().getComments(), getRefundComment(refundAmount, tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE); //getRefundComment
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), true, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getRefundComment(refundAmount, tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //Partial to full refund from shop admin
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber).proceedRefund(refundAmount);
        verifyContains(shopware.getOrdersPage().getComments(), getRefundComment(refundAmount, tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE); //getRefundComment
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getRefundComment(refundAmount, tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 3, description = "Check whether the authorize order with the order amount greater than the 'Minimum transaction amount for authorization' is successful, execute transaction capture event and execute transaction refund event ")
//,retryAnalyzer = RetryListener.class)
    public void thirdOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "1000",
                ALLOW_B2B, true,
                FORCE_NON_GUARANTEE, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid, true);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Capture via callback
        var captureComment = shopwareGS.getCallback().transactionCapture(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCaptureComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCaptureComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE); // "getCaptureComment()" is used instead of "getCallbackResponse(captureComment)" reason: "//" in expected comment
        //Refund partial via callback
        var partialRefund = shopwareGS.getCallback().transactionRefund(tid, String.valueOf(Integer.parseInt(totalAmount) / 2));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(partialRefund), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(partialRefund), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }

    @Test(priority = 4, description = "Check whether order with order amount less than the 'Minimum transaction amount for authorization' is successful with iban AT, execute refund and reminder callback events")
//,retryAnalyzer = RetryListener.class)
    public void fourthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "6000",
                FORCE_NON_GUARANTEE, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANAT"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid, true);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Full refund via callback
        var partialRefund = shopwareGS.getCallback().transactionRefund(tid, String.valueOf(totalAmount));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(partialRefund), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(partialRefund), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //paymentReminderOne
        var paymentReminderOne = shopwareGS.getCallback().paymentReminderOne(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(paymentReminderOne), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(paymentReminderOne), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //paymentReminderTwo
        var paymentReminderTwo = shopwareGS.getCallback().paymentReminderTwo(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(paymentReminderTwo), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(paymentReminderTwo), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }

    @Test(priority = 5, description = "Check whether the authorize is set, verify masked iban displayed, order placed using masked iban token, cancel in shop backend ")
//,retryAnalyzer = RetryListener.class)
    public void fifthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "",
                FORCE_NON_GUARANTEE, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifyMaskedSEPAData(shopware.getTestData().get("IBANAT"))
//                .clickNewCardSEPA()
//                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isIbanAndDobDetailsFilled(shopware.getTestData().get("IBANDE"), DOB, GUARANTEED_DIRECT_DEBIT_SEPA, false, true);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
//        TID_Helper.verifyPaymentTokenExist(tid, true);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Cancel from back-end
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

    @Test(priority = 6, description = "Check whether the authorize is set, verify masked iban displayed, order placed using masked iban token, execute transaction cancel event")
//,retryAnalyzer = RetryListener.class)
    public void sixthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "",
                FORCE_NON_GUARANTEE, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifyMaskedSEPAData(shopware.getTestData().get("IBANAT"))
                /*.clickNewCardSEPA()
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))*/
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isIbanAndDobDetailsFilled(shopware.getTestData().get("IBANDE"), DOB, GUARANTEED_DIRECT_DEBIT_SEPA, false, true);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
       // TID_Helper.verifyPaymentTokenExist(tid, false);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Cancel via callback
        var CancelFromCallback = shopwareGS.getCallback().transactionCancel(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }

    @Test(priority = 7, description = "Check whether B2B address is set, DOB is not displayed, verify AT iban displayed masked at checkout, order placed successfully using new card, " +
            "verify transaction is pending, and execute transaction update with confirmed")
//,retryAnalyzer = RetryListener.class)
    public void seventhOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true,
                FORCE_NON_GUARANTEE, true,
                MIN_ORDER_AMOUNT, "999"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid);
        verifyEquals(initialComment, getGuaranteePendingCommentForSEPA(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Update event to confirm
        shopwareGS.getCallback().transactionUpdateStatus(tid, TID_STATUS_CONFIRMED);
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getUpdateComment(totalAmount, tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getUpdateComment(totalAmount, tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 8, description = "Check whether B2B address is set, DOB is not displayed, order placed successfully using new card, " +
            "verify transaction is pending, and execute transaction update with deactivated")
    public void eighthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true,
                FORCE_NON_GUARANTEE, true,
                MIN_ORDER_AMOUNT, "999"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifySEPA_DateOfBirthDisplayed(false)
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid);
        verifyEquals(initialComment, getGuaranteePendingCommentForSEPA(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Update event pending to confirm
        shopwareGS.getCallback().transactionUpdateStatus(tid, TID_STATUS_DEACTIVATED);
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 9, description = "Check whether B2B address is set, DOB is not displayed, order placed successfully, transaction is pending, execute transaction update event from pending to onhold" +
            " and execute transaction capture event ")//,retryAnalyzer = RetryListener.class)
    public void ninthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true,
                FORCE_NON_GUARANTEE, true,
                MIN_ORDER_AMOUNT, "999"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifySEPA_DateOfBirthDisplayed(false)
                .clickNewCardSEPA()
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid);
        verifyEquals(initialComment, getGuaranteePendingCommentForSEPA(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Update event pending to onhold
        shopwareGS.getCallback().transactionUpdateStatus(tid, TID_STATUS_ON_HOLD);
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getPendingToOnHoldComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getPendingToOnHoldComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Capture via callback
        shopwareGS.getCallback().transactionCapture(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCaptureComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCaptureComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }

    @Test(priority = 10, description = "Check whether B2B address is set, DOB is not displayed, order placed successfully, transaction is pending, execute transaction update event from pending to onhold" +
            " and execute transaction cancel event ")//,retryAnalyzer = RetryListener.class)
    public void tenthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true,
                FORCE_NON_GUARANTEE, true,
                MIN_ORDER_AMOUNT, "999"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifySEPA_DateOfBirthDisplayed(false)
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid);
        verifyEquals(initialComment, getGuaranteePendingCommentForSEPA(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Update event pending to onhold
        shopwareGS.getCallback().transactionUpdateStatus(tid, TID_STATUS_ON_HOLD);
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getPendingToOnHoldComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getPendingToOnHoldComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Cancel order via callback
        shopwareGS.getCallback().transactionCancel(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 11, description = "Check whether B2B transaction confirm address is set, DOB is not displayed, order placed successfully")//,retryAnalyzer = RetryListener.class)
    public void eleventhOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true,
                FORCE_NON_GUARANTEE, true,
                MIN_ORDER_AMOUNT, "999"
        ));
        ShopwareAPIs.getInstance().createCustomer(DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().load().setB2BBillingAddress();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifySEPA_DateOfBirthDisplayed(false)
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isIbanAndDobDetailsFilled(shopware.getTestData().get("IBANDE"), DOB, GUARANTEED_DIRECT_DEBIT_SEPA, true, false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }
    @Test(priority = 12, description = "Ensure that payments are not allowed to proceed if the date of birth indicates an age below 18 years.")
    public void guaranteeValidation1() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false,
                FORCE_NON_GUARANTEE, false
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB_LESS_18, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isDOBFilled(DOB_LESS_18, GUARANTEED_DIRECT_DEBIT_SEPA, true);
        verifyEquals(shopware.getCheckoutPage().getCheckoutV13FormValidation(), getBelow18Error());
    }

    @Test(priority = 13, description = "Verify that guarantee payments are displayed on the checkout page for customers from Germany in a B2C context.")
    public void guaranteeValidation2() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifySEPA_DateOfBirthDisplayed(true);
    }

    @Test(priority = 14, description = "Verify that guarantee payments are displayed on the checkout page for customers from Austria in a B2C context.")
    public void guaranteeValidation3() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Austria");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifySEPA_DateOfBirthDisplayed(true);
    }

    @Test(priority = 15, description = "Verify that guarantee payments are displayed on the checkout page for customers from Switzerland in a B2C context.")
    public void guaranteeValidation4() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Switzerland");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .verifySEPA_DateOfBirthDisplayed(true);
    }

    @Test(priority = 16, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2C context.")
    public void guaranteeValidation5() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("United Kingdom");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 17, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2B context.")
    public void guaranteeValidation6() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCompany();
        shopware.getMyAccountPage().changeBillingCountry("United Kingdom");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 18, description = "Verify that guarantee payments are displayed on the checkout page for customers from Netherland in a B2B context.")
    public void guaranteeValidation7() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getMyAccountPage().changeBillingCountry("Netherlands");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 19, description = "Verify that guarantee payments are displayed on the checkout page for customers from United States in a B2B context.")
    public void guaranteeValidation8() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCompany();
        shopware.getMyAccountPage().changeBillingCountry("United States of America");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 20, description = "Verify that guarantee payments are hidden on the checkout page for customers when amount is less 1998 EUR")
    public void guaranteeValidation9() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 21, description = "Verify that guarantee payments are hidden on the checkout page for customers when currency is not EUR")
    public void guaranteeValidation10() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeCurrency("USD");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 22, description = "Verify that guarantee payments are hidden on the checkout page for customers with different billing and shipping")
    public void guaranteeValidation11() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().setDifferentShippingAddress();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 23, description = "Check whether the test transaction is successful with guest user ")
    public void guestOrder() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        shopware.getMyAccountPage().addProductToCart(SW_PRODUCT_GUEST_01);
        shopware.getCustomerLoginPage().guestRegister("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isIbanAndDobDetailsFilled(shopware.getTestData().get("IBANDE"), DOB, GUARANTEED_DIRECT_DEBIT_SEPA, false, false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
    }
    @Test(priority = 17, description = "Verify download button and download link are not displayed in front-end orders page when the transaction is in 100 status")
    public void downloadProductConfirmOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION, CAPTURE
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_DIGITAL_PRODUCT2);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isIbanAndDobDetailsFilled(shopware.getTestData().get("IBANDE"), DOB, GUARANTEED_DIRECT_DEBIT_SEPA, false, false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(tid, true);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), true, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), true, DOWNLOAD_LINK);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 21, description = "Verify download button and download link are not displayed in front-end orders page when the transaction is captured from on-hold")
    public void downloadProductOnHoldToConfirmOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION, AUTHORIZE
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_DIGITAL_PRODUCT2);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isIbanAndDobDetailsFilled(shopware.getTestData().get("IBANDE"), DOB, GUARANTEED_DIRECT_DEBIT_SEPA, false, false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
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
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION, AUTHORIZE
        ));
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_DIGITAL_PRODUCT2);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isIbanAndDobDetailsFilled(shopware.getTestData().get("IBANDE"), DOB, GUARANTEED_DIRECT_DEBIT_SEPA, false, false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(GUARANTEED_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Cancel
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber).cancel();
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
    }
}

