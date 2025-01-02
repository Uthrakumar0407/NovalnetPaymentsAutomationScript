package com.nn.testcase.shopware;

import com.nn.apis.ShopwareAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.InvoiceCallbackEvents;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.pages.shopware.base.Shopware;
import com.nn.pages.shopware.base.ShopwareOrderStatus;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.nn.Magento.Constants.AUTHORIZE;
import static com.nn.Magento.Constants.CAPTURE;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.language.NovalnetCommentsEN.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.DriverActions.verifyContains;
import static com.nn.utilities.DriverActions.verifyEquals;
import static com.nn.utilities.ShopwareUtils.*;
import static com.nn.utilities.ShopwareUtils.ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE;

public class Invoice extends BaseTest {

    Shopware shopwareInvoice = Shopware.builder()
            .callback(new InvoiceCallbackEvents())
            .build();

    @BeforeClass
    public void setup() {
        ExtentTestManager.saveToReport("setup", "Setting up prerequisite to place order");
        ShopwareAPIs.getInstance().createCustomer(INVOICE);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getLoginPage().load().login();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INVOICE, true);
        paymentActivation(GUARANTEED_INVOICE, true);
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                FORCE_NON_GUARANTEE, true));
    }

    @AfterMethod
    public void clear() {
        ShopwareAPIs.getInstance().clearCart();
    }

    @Test(priority = 1, description = "Check whether the capture transaction is successful with payment action set to Capture and partial refund via shop backend executed ")
    public void firstOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                DUE_DATE,""
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, INVOICE,14);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //udpate due date
        var updatedDueDate = DriverActions.addDaysFromDate(TID_Helper.getDueDate(tid), 10);
        var updateComment = shopwareInvoice.getCallback().transactionUpdateDueDate(tid,updatedDueDate);
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

    @Test(priority = 2, description = "Check whether the authorize order placed successfully and capture and full refund via shop backend")
    public void secondOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                DUE_DATE,10
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, INVOICE,10);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithoutDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Capture
        shopware.getOrdersPage().capture();
        TID_Helper.verifyTIDStatus(tid, TID_STATUS_PENDING);
        verifyContains(shopware.getOrdersPage().getComments(), getCaptureComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), true, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCaptureComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Credit
        var creditAmount = String.valueOf(Integer.parseInt(totalAmount)/2);
        var partialCredit = shopwareInvoice.getCallback().invoiceCredit(tid,creditAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(partialCredit), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID_PARTIALLY.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID_PARTIALLY.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID_PARTIALLY.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(partialCredit), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);

        partialCredit = shopwareInvoice.getCallback().invoiceCredit(tid,creditAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(partialCredit), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(partialCredit), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
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

    @Test(priority = 3, description = "Check whether the authorize order with authorize minimum amount less than order amount is successful with payment action set to Authorize and minAuthAmount set to 100 and followup callback events transaction capture credit and refund executed successfully")
    public void thirdOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"100"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, INVOICE);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithoutDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);

        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isCaptureAndCancelButtonDisplayed(),true,CAPTURE_CANCEL_BUTTON_DISPLAYED);
        //capture via callback
        shopwareInvoice.getCallback().transactionCapture(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCaptureComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), true, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCaptureComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //invoice credit
        var invoiceCredit = shopwareInvoice.getCallback().invoiceCredit(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(invoiceCredit), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(invoiceCredit), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //invoice credit
        var debtCollectionDE = shopwareInvoice.getCallback().debtCollectionDE(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(debtCollectionDE), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(debtCollectionDE), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //invoice credit
        var bankTransferByEndCustomer = shopwareInvoice.getCallback().bankTransferByEndCustomer(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(bankTransferByEndCustomer), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(bankTransferByEndCustomer), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //refund
        var refund =  shopwareInvoice.getCallback().transactionRefund(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(refund), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.REFUNDED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(refund), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 4, description = "Check whether the authorize order with authorize minimum amount greater than order amount is successful with payment action set to Authorize and minAuthAmount set to 10000 ,follow up callback events transaction update , payment reminder and credit events executed successfully")
    public void fourthOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"10000"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, INVOICE);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //udpate due date
        var updateAmount = String.valueOf(Integer.parseInt(totalAmount)*2);
        var updateComment = shopwareInvoice.getCallback().transactionUpdateAmount(tid,updateAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(updateComment), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(updateComment), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //reminder one
        var reminderOne = shopwareInvoice.getCallback().paymentReminderOne(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(reminderOne), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(reminderOne), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //reminder two
        var reminderTwo = shopwareInvoice.getCallback().paymentReminderOne(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(reminderTwo), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(reminderTwo), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //reminder two
        var collection = shopwareInvoice.getCallback().paymentReminderOne(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(collection), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(collection), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //refund
        var credit = shopwareInvoice.getCallback().invoiceCredit(tid,totalAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(credit), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID_PARTIALLY.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID_PARTIALLY.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID_PARTIALLY.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(credit), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 5, description = "Check whether the authorize order is cancelled via shop backend with payment action set to Authorize")
    public void fifthOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        String productAmount = ShopwareAPIs.getInstance().getProductPrice(SW_PRODUCT_01);
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,productAmount
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, INVOICE,10);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithoutDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
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

    @Test(priority = 6, description = "Check whether the authorize order is cancelled via callback with payment action set to Authorize")
    public void sixthOrder(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,""
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, INVOICE);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithoutDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //Cancel
        shopwareInvoice.getCallback().transactionCancel(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isRefundBtnDisplayed(), false, REFUND_BUTTON_DISPLAYED);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }

    //Guest order need to add cause of product issue
    @Test(priority = 7, description = "Check whether the test transaction is successful with guest user")
    public void guestOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                DUE_DATE,""
        ));
        shopware.getMyAccountPage().addProductToCart(SW_PRODUCT_GUEST_02);
        shopware.getCustomerLoginPage().guestRegister("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, INVOICE,14);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithDueDate(tid,totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }
    @Test(priority = 17, description = "Verify download button and download link are not displayed in front-end orders page when the transaction is in 100 status")
    public void downloadProductConfirmOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION, CAPTURE
        ));
        ShopwareAPIs.getInstance().createCustomer(INVOICE);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_DIGITAL_PRODUCT1);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, INVOICE);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithDueDate(tid, totalAmount), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
    }
    @Test(priority = 21, description = "Verify download button and download link are not displayed in front-end orders page when the transaction is captured from on-hold")
    public void downloadProductOnHoldToConfirmOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION, AUTHORIZE
        ));
        ShopwareAPIs.getInstance().createCustomer(INVOICE);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_DIGITAL_PRODUCT1);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, INVOICE);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithoutDueDate(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Capture via backend
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber).capture();
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
        //Credit
        var creditAmount = String.valueOf(totalAmount);
        var partialCredit = shopwareInvoice.getCallback().invoiceCredit(tid,creditAmount);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(partialCredit), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), true, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), true, DOWNLOAD_LINK);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(partialCredit), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }
    @Test(priority = 21, description = "Verify download button and download link are not displayed in front-end orders page when the transaction is cancelled from on-hold")
    public void downloadProductOnHoldToCancelOrder(){
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INVOICE, Map.of(
                PAYMENT_ACTION, AUTHORIZE
        ));
        ShopwareAPIs.getInstance().createCustomer(INVOICE);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        ShopwareAPIs.getInstance().addProductToCart(SW_DIGITAL_PRODUCT1);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_ON_HOLD, INVOICE);
        verifyEquals(initialComment, getOrderSuccessCommentWithBankDetailWithoutDueDate(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INVOICE), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INVOICE), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Cancel via backend
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber).cancel();
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().isDownloadButtonDisplayed(orderNumber), false, DOWNLOAD_BTN);
        verifyEquals(shopware.getMyAccountPage().isDownloadLinkDisplayed(orderNumber), false, DOWNLOAD_LINK);
    }
}
