package com.nn.testcase.shopware;

import com.nn.apis.ShopwareAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.InstalmentSEPACallbackEvents;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.pages.shopware.base.Shopware;
import com.nn.pages.shopware.base.ShopwareOrderStatus;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.language.NovalnetCommentsEN.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.MIN_ORDER_AMOUNT;
import static com.nn.utilities.DriverActions.verifyContains;
import static com.nn.utilities.DriverActions.verifyEquals;
import static com.nn.utilities.ShopwareUtils.*;
import static com.nn.utilities.ShopwareUtils.ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE;

public class InstalmentSepa extends BaseTest {
    Shopware shopwareIS = Shopware.builder()
            .callback(new InstalmentSEPACallbackEvents())
            .build();

    @BeforeClass
    public void setup() {
        ExtentTestManager.saveToReport("setup", "Setting up prerequisite to place order");
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_INVOICE);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getLoginPage().load().login();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INSTALMENT_DIRECT_DEBIT_SEPA, true);
    }

    @AfterMethod
    public void clear() {
        ShopwareAPIs.getInstance().clearCart();
    }

    @Test(priority = 1, description = "Check whether the test transaction is successful with Capture, set allowed cycles, verify allowed instalment cycles displayed at checkout instalment dropdown," +
            " verify instalment object in tid, verify instalment table success page and orders page, execute instalment recurring and refund at shop backend for 1st cycle")
    public void firstOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        int[] allowedInstalmentCycles = new int[]{2, 3, 4, 5};
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true,
                MIN_ORDER_AMOUNT, "1998"
        ), allowedInstalmentCycles);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .verifySelectedInstalmentCyclesDisplayedAtCheckout(allowedInstalmentCycles)
                .selectInstalmentSepaCycle(3)
                .fill_DOB_InstalmentSEPA(DOB)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, INSTALMENT_DIRECT_DEBIT_SEPA,2);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        shopware.getMyAccountPage().verifyInstalmentTable(tid, 3, totalAmount);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getOrdersPage().verifyInstalmentTable(tid, 3, totalAmount);
        //recurring
        var second = 2;
        var secondCycle = shopwareIS.getCallback().instalment(tid, String.valueOf(second), "1", DriverActions.getUpcomingMonthDate(2));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(secondCycle), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableTID(second), getEventTID(), INSTALMENT_TABLE_TID_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(second), ShopwareOrderStatus.PAID.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableRefundBtnEnabled(second), true, INSTALMENT_TABLE_REFUND_BUTTON_ENABLED_IN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(secondCycle), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableTID(second), getEventTID(), INSTALMENT_TABLE_TID_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(second), ShopwareOrderStatus.PAID.get(), INSTALMENT_TABLE_STATUS_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);

        var third = 3;
        var thirdCycle = shopwareIS.getCallback().instalment(tid, String.valueOf(third), "0");
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(thirdCycle), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableTID(third), getEventTID(), INSTALMENT_TABLE_TID_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(third), ShopwareOrderStatus.PAID.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableRefundBtnEnabled(third), true, INSTALMENT_TABLE_REFUND_BUTTON_ENABLED_IN_ORDER_DETAIL_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(thirdCycle), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableTID(third), getEventTID(), INSTALMENT_TABLE_TID_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(third), ShopwareOrderStatus.PAID.get(), INSTALMENT_TABLE_STATUS_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        //instalment table refund
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        shopware.getOrdersPage().proceedInstalmentTableRefund(1);
        verifyContains(shopware.getOrdersPage().getComments(), getRefundComment(orderNumber), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableRefundBtnEnabled(1), false, INSTALMENT_TABLE_REFUND_BUTTON_ENABLED_IN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(1), ShopwareOrderStatus.REFUNDED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getRefundComment(orderNumber), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(1), ShopwareOrderStatus.REFUNDED.get(), INSTALMENT_TABLE_STATUS_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 2, description = "Check whether the order placed successfully with authorize, verify tid status, verify instalment object exist in tid, verify instalment table displayed," +
            " capture via shop backend and partial refund via shop backend")
    public void secondOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "",
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentSepaCycle(4)
                .fill_DOB_InstalmentSEPA(DOB)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, String.valueOf(cycleAmount), TID_STATUS_ON_HOLD, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Capture order
        shopware.getOrdersPage().capture();
        TID_Helper.verifyTIDStatus(tid, TID_STATUS_CONFIRMED);
        verifyContains(shopware.getOrdersPage().getComments(), getCaptureComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getOrdersPage().verifyInstalmentTable(tid, 4, totalAmount);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCaptureComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        shopware.getMyAccountPage().verifyInstalmentTable(tid, 4, totalAmount);
        //instalment table refund partial
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        shopware.getOrdersPage().proceedInstalmentTableRefund(1, String.valueOf(cycleAmount));
        verifyContains(shopware.getOrdersPage().getComments(), getRefundComment(orderNumber), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableRefundBtnEnabled(1), false, INSTALMENT_TABLE_REFUND_BUTTON_ENABLED_IN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(1), ShopwareOrderStatus.REFUNDED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getRefundComment(orderNumber), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(1), ShopwareOrderStatus.REFUNDED.get(), INSTALMENT_TABLE_STATUS_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 3, description = "Check whether the order with authorize minimum amount less than order amount, allow b2b disabled, set company name in address, verify DOB field displayed," +
            " verify tid status is on hold, execute transaction capture and transaction refund event")
    public void thirdOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "100",
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingCompany();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .verifyInstalmentSepaDateOfBirthDisplayed(true)
                .selectInstalmentSepaCycle(4)
                .fill_DOB(DOB, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, String.valueOf(cycleAmount), TID_STATUS_ON_HOLD, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Capture via callback
        shopwareIS.getCallback().transactionCapture(tid, DriverActions.getUpcomingMonthDatesInArr(4), "1", "2", DriverActions.getUpcomingMonthDate(2));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCaptureComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getOrdersPage().verifyInstalmentTable(tid, 4, totalAmount);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCaptureComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        shopware.getMyAccountPage().verifyInstalmentTable(tid, 4, totalAmount);
        //Refund via callback
        var refundComment = shopwareIS.getCallback().transactionRefund(tid, String.valueOf(cycleAmount));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(refundComment), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableRefundBtnEnabled(1), false, INSTALMENT_TABLE_REFUND_BUTTON_ENABLED_IN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(1), ShopwareOrderStatus.REFUNDED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(refundComment), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(1), ShopwareOrderStatus.REFUNDED.get(), INSTALMENT_TABLE_STATUS_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 4, description = "Check whether the test order with authorize minimum amount greater than order amount is successful, verify tid status is confirmed, execute reminder and refund callback events")
    public void fourthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "10000",
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(4, INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_DOB(DOB, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        shopware.getMyAccountPage().verifyInstalmentTable(tid, 4, totalAmount);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getOrdersPage().verifyInstalmentTable(tid, 4, totalAmount);
        //Payment reminder one
        shopwareIS.getCallback().paymentReminderOne(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), REMINDER_ONE_COMMENT_, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), REMINDER_ONE_COMMENT_, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Payment reminder two
        shopwareIS.getCallback().paymentReminderTwo(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), REMINDER_TWO_COMMENT_, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), REMINDER_TWO_COMMENT_, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Refund via callback
        var refundComment = shopwareIS.getCallback().transactionRefund(tid, String.valueOf(cycleAmount / 2));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(refundComment), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableRefundBtnEnabled(1), true, INSTALMENT_TABLE_REFUND_BUTTON_ENABLED_IN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(1), ShopwareOrderStatus.PAID.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(refundComment), TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(1), ShopwareOrderStatus.PAID.get(), INSTALMENT_TABLE_STATUS_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 5, description = "Check whether the order placed successfully with authorize order amount and auth amount equal, verify tid status, verify instalment object exist in tid, verify instalment table displayed," +
            " cancel via shop backend")
    public void fifthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        String productAmount = ShopwareAPIs.getInstance().getProductPrice(SW_PRODUCT_01);
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, productAmount,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingCompany();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(4, INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_DOB(DOB, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, String.valueOf(cycleAmount), TID_STATUS_ON_HOLD, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Cancel order
        shopware.getOrdersPage().cancel();
        TID_Helper.verifyTIDStatus(tid, TID_STATUS_DEACTIVATED);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableDisplayed(), false, "Verify Instalment table displayed in admin orders page");
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().isInstalmentTableDisplayed(), false, "Verify Instalment table displayed in my account page");
    }

    @Test(priority = 6, description = "Check whether the order placed successfully with authorize, verify tid status, verify instalment object exist in tid, verify instalemnt table displayed," +
            " cancel via callback")
    public void sixthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, "",
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingCompany();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(4, INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_DOB(DOB, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, String.valueOf(cycleAmount), TID_STATUS_ON_HOLD, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Cancel order via callback
        shopwareIS.getCallback().transactionCancel(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableDisplayed(), false, "Verify Instalment table displayed in admin orders page");
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().isInstalmentTableDisplayed(), false, "Verify Instalment table displayed in my account page");
    }

    @Test(priority = 7, description = "Check whether the B2B pending to confirm test transaction is successful, b2b address set, verify dob field is hidden, verify tid status is pending, execute transaction update event")
    public void seventhOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(4, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, String.valueOf(cycleAmount), TID_STATUS_PENDING, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getGuaranteePendingCommentForSEPA(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.IN_PROGRESS.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Update callback event
        shopwareIS.getCallback().transactionUpdateStatus(tid, TID_STATUS_CONFIRMED, DriverActions.getUpcomingMonthDatesInArr(4), "1", "3", DriverActions.getUpcomingMonthDate(2));
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        shopware.getMyAccountPage().verifyInstalmentTable(tid, 4, totalAmount);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getOrdersPage().verifyInstalmentTable(tid, 4, totalAmount);
    }

    @Test(priority = 8, description = "Check whether the B2B pending to cancel test transaction is successful, b2b address set, verify dob field is hidden, verify tid status is pending, execute transaction cancel event")
//,retryAnalyzer = RetryListener.class)
    public void eighthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(4, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, String.valueOf(cycleAmount), TID_STATUS_PENDING, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getGuaranteePendingCommentForSEPA(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE); //Same pending comment need to be followed for Instalment Invoice
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //cancel callback event
        shopwareIS.getCallback().transactionCancel(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableDisplayed(), false, "Verify Instalment table displayed in admin orders page");
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().isInstalmentTableDisplayed(), false, "Verify Instalment table displayed in my account page");
    }

    @Test(priority = 9, description = "Check whether the B2B pending to on hold to confirm test transaction is successful, b2b address set, verify dob field is hidden, verify tid status is pending, execute transaction update," +
            " transaction capture and instalment cancel  event")
    public void ninthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(4, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, String.valueOf(cycleAmount), TID_STATUS_PENDING, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getGuaranteePendingCommentForSEPA(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //Update event to on hold
        shopwareIS.getCallback().transactionUpdateStatus(tid, TID_STATUS_ON_HOLD, DriverActions.getUpcomingMonthDatesInArr(4), "1", "3", DriverActions.getUpcomingMonthDate(2));
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getPendingToOnHoldComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getPendingToOnHoldComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //Capture via callback
        shopwareIS.getCallback().transactionCapture(tid, DriverActions.getUpcomingMonthDatesInArr(4), "1", "3", DriverActions.getUpcomingMonthDate(2));
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCaptureComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getOrdersPage().verifyInstalmentTable(tid, 4, totalAmount);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCaptureComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        shopware.getMyAccountPage().verifyInstalmentTable(tid, 4, totalAmount);
        //Instalment cancel via callback
        var instlamentCancelComment = shopwareIS.getCallback().instalmentCancelAllCycle(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCallbackResponse(instlamentCancelComment), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(1), ShopwareOrderStatus.REFUNDED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(2), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(3), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(4), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCallbackResponse(instlamentCancelComment), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(1), ShopwareOrderStatus.REFUNDED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(2), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(3), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(4), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 10, description = "Check whether the B2B pending to on hold to cancel test transaction is successful, b2b address set, verify dob field is hidden, verify tid status is pending, execute transaction update," +
            " transaction cancel")
    public void tenthOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(4, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, String.valueOf(cycleAmount), TID_STATUS_PENDING, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getGuaranteePendingCommentForSEPA(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //Update event to on hold
        shopwareIS.getCallback().transactionUpdateStatus(tid, TID_STATUS_ON_HOLD);
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getPendingToOnHoldComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyContains(shopware.getOrdersPage().getComments(), getPendingToOnHoldComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.AUTHORIZED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        //cancel callback event
        shopwareIS.getCallback().transactionCancel(tid);
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getCancelComment(), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().isInstalmentTableDisplayed(), false, "Verify Instalment table displayed in admin orders page");
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.CANCELLED.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getCancelComment(), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().isInstalmentTableDisplayed(), false, "Verify Instalment table displayed in my account page");
    }

    @Test(priority = 11, description = "Check whether the B2B confirm test transaction is successful, b2b confirm address is set, verify date of birth displayed, verify tid status confirmed, " +
            " verify instalment table displayed at success and orders page, instalment cancel via shop backend")
    public void eleventhOrder() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().setB2BBillingAddress();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(4, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        var cycleAmount = Integer.parseInt(totalAmount) / 4;
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
        //My account page
        shopware.getMyAccountPage().load().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentName(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE);
        verifyEquals(shopware.getMyAccountPage().getPaymentNameInside(orderNumber), getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getComments(orderNumber), initialComment, TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        shopware.getMyAccountPage().verifyInstalmentTable(tid, 4, totalAmount);
        //Admin orders page
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getOrdersPage().openOrderDetail(orderNumber);
        verifyEquals(shopware.getOrdersPage().getOrderStatus(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getComments(), initialComment, TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        shopware.getOrdersPage().verifyInstalmentTable(tid, 4, totalAmount);
        shopware.getOrdersPage().cancelAllRemainingCycles();
        shopware.getOrdersPage().openOrdersPage().openOrderDetail(orderNumber);
        verifyContains(shopware.getOrdersPage().getComments(), getInstalmentStoppedComment(tid), TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getOrderStatusNovalnet(), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(1), ShopwareOrderStatus.PAID.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(2), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(3), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().getInstalmentTableStatus(4), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getOrdersPage().openOrdersPage().getOrderListingStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE);
        shopware.getMyAccountPage().openOrdersPage().expandOrder(orderNumber);
        verifyEquals(shopware.getMyAccountPage().getOrderStatus(orderNumber), ShopwareOrderStatus.PAID.get(), ORDER_STATUS_IN_MY_ACCOUNT_PAGE);
        verifyContains(shopware.getMyAccountPage().getComments(orderNumber), getInstalmentStoppedComment(tid), TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(1), ShopwareOrderStatus.PAID.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(2), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(3), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
        verifyEquals(shopware.getMyAccountPage().getInstalmentTableStatus(4), ShopwareOrderStatus.CANCELED.get(), INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE);
    }

    @Test(priority = 12, description = "Ensure that payments are not allowed to proceed if the date of birth indicates an age below 18 years.")
    public void guaranteeValidation1() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(3, INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_DOB(DOB_LESS_18, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isDOBFilled(DOB_LESS_18, INSTALMENT_DIRECT_DEBIT_SEPA, true);
        verifyEquals(shopware.getCheckoutPage().getCheckoutV13FormValidation(), getBelow18Error());
    }

    @Test(priority = 13, description = "Verify that guarantee payments are displayed on the checkout page for customers from Germany in a B2C context.")
    public void guaranteeValidation2() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .verifyInstalmentSepaDateOfBirthDisplayed(true);
    }

    @Test(priority = 14, description = "Verify that guarantee payments are displayed on the checkout page for customers from Austria in a B2C context.")
    public void guaranteeValidation3() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Austria");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .verifyInstalmentSepaDateOfBirthDisplayed(true);
    }

    @Test(priority = 15, description = "Verify that guarantee payments are displayed on the checkout page for customers from Switzerland in a B2C context.")
    public void guaranteeValidation4() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Switzerland");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .verifyInstalmentSepaDateOfBirthDisplayed(true);
    }

    @Test(priority = 16, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2C context.")
    public void guaranteeValidation5() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("United Kingdom");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 17, description = "Verify that guarantee payments are displayed on the checkout page for customers from England in a B2B context.")
    public void guaranteeValidation6() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCompany();
        shopware.getMyAccountPage().changeBillingCountry("United Kingdom");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA,false);
    }

    @Test(priority = 18, description = "Verify that guarantee payments are displayed on the checkout page for customers from Netherland in a B2B context.")
    public void guaranteeValidation7() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingForPending();
        shopware.getMyAccountPage().changeBillingCountry("Netherlands");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 19, description = "Verify that guarantee payments are displayed on the checkout page for customers from United States in a B2B context.")
    public void guaranteeValidation8() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCompany();
        shopware.getMyAccountPage().changeBillingCountry("United States of America");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 19, description = "Verify that guarantee payments are hidden on the checkout page for customers when amount is less 1998 EUR")
    public void guaranteeValidation9() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 20, description = "Verify that guarantee payments are hidden on the checkout page for customers when currency is not EUR")
    public void guaranteeValidation10() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeCurrency("USD");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 21, description = "Verify that guarantee payments are hidden on the checkout page for customers with different billing and shipping")
    public void guaranteeValidation11() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().setDifferentShippingAddress();
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 22, description = "Check whether the test transaction is successful with guest user ")
    public void guestOrder() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, true
        ));
        shopware.getMyAccountPage().addProductToCart(SW_PRODUCT_GUEST_01);
        shopware.getCustomerLoginPage().guestRegister("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentCycle(3, INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_DOB(DOB, INSTALMENT_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getIframeV13().isIbanAndDobDetailsFilled(shopware.getTestData().get("IBANDE"), DOB, INSTALMENT_DIRECT_DEBIT_SEPA, false, false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, INSTALMENT_DIRECT_DEBIT_SEPA);
        verifyEquals(initialComment, getOrderSuccessComment(tid), TRANSACTION_COMMENT_IN_SUCCESS_PAGE);
        verifyEquals(paymentName, getPaymentName(INSTALMENT_DIRECT_DEBIT_SEPA), PAYMENT_NAME_IN_SUCCESS_PAGE);
    }

}
