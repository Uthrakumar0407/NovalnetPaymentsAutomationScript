package com.nn.testcase.shopware;

import com.nn.apis.ShopwareAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.PrepaymentCallbackEvents;
import com.nn.listeners.RetryListener;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.pages.shopware.base.Shopware;
import com.nn.pages.shopware.base.ShopwareOrderStatus;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.PREPAYMENT;
import static com.nn.language.NovalnetCommentsEN.getOrderSuccessCommentWithBankDetailWithDueDate;
import static com.nn.language.NovalnetCommentsEN.getRefundComment;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.DriverActions.verifyContains;
import static com.nn.utilities.DriverActions.verifyEquals;
import static com.nn.utilities.ShopwareUtils.*;

public class Prepayment extends BaseTest {

    Shopware shopwarePre = Shopware.builder()
            .callback(new PrepaymentCallbackEvents())
            .build();

    @BeforeClass
    public void setup() {
        ExtentTestManager.saveToReport("setup", "Setting up prerequisite to place order");
        ShopwareAPIs.getInstance().createCustomer(PREPAYMENT);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getLoginPage().load().login();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PREPAYMENT, true);
    }

    @AfterMethod
    public void clear() {
        ShopwareAPIs.getInstance().clearCart();
    }

    @Test(priority = 1, description = "Check whether the prepayment transaction is successful , invoice credit and partial refund executed ")
    public void firstOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PREPAYMENT, Map.of(
                TESTMODE,false,
                DUE_DATE,""
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PREPAYMENT)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, PREPAYMENT,14);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(PREPAYMENT), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(PREPAYMENT), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(PREPAYMENT), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //udpate due date
        var updatedDueDate = DriverActions.addDaysFromDate(TID_Helper.getDueDate(tid), 10);
        var updateComment = shopwarePre.getCallback().transactionUpdateDueDate(tid,updatedDueDate);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(updateComment), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(updateComment), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //refund
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        var refundAmount = String.valueOf(Integer.parseInt(totalAmount) / 2);
        shopware.getOrdersPage().proceedRefund(refundAmount);
        verifyContains(shopware.getOrdersPage().getComments(), getRefundComment(refundAmount,tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), true, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getRefundComment(refundAmount,tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 2, description = "Check whether the prepayment transaction is successful , invoice credit and full refund executed ")
    public void secondOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PREPAYMENT, Map.of(
                TESTMODE,true,
                DUE_DATE,"20"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PREPAYMENT)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, PREPAYMENT,20);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(PREPAYMENT), PAYMENT_NAME_IN_SUCCESS_PAGE);
        var credit = shopwarePre.getCallback().invoiceCredit(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(credit), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(credit), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //refund
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        shopware.getOrdersPage().proceedRefund();
        verifyContains(shopware.getOrdersPage().getComments(), getRefundComment(totalAmount,tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getRefundComment(totalAmount,tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 3, description = "Check whether the prepayment transaction is successful , invoice credit and other callback events executed ",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PREPAYMENT)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, PREPAYMENT);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(PREPAYMENT), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //invoice credit
        var invoiceCredit = shopwarePre.getCallback().invoiceCredit(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(invoiceCredit), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(invoiceCredit), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //invoice credit
        var debtCollectionDE = shopwarePre.getCallback().debtCollectionDE(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(debtCollectionDE), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(debtCollectionDE), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //invoice credit
        var bankTransferByEndCustomer = shopwarePre.getCallback().bankTransferByEndCustomer(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(bankTransferByEndCustomer), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(bankTransferByEndCustomer), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //refund
        var refund =  shopwarePre.getCallback().transactionRefund(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(refund), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(refund), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 4, description = "Check whether the test transaction is successful with guest user")
    public void guestOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getMyAccountPage().addProductToCart(SW_PRODUCT_GUEST_01);
        shopware.getCustomerLoginPage().guestRegister("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PREPAYMENT)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, PREPAYMENT);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(PREPAYMENT), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }
}
