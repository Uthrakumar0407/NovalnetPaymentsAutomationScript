package com.nn.pages.Magento;

import com.nn.apis.MagentoAPI_Helper;
import com.nn.apis.MagentoAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.CallbackProperties;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Step;
import org.testng.Assert;

import java.util.Date;
import java.util.HashMap;

import static com.nn.Magento.Constants.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.getDateFromString;
import static com.nn.utilities.DriverActions.openURL;

public class BasePage {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .orderPage(new OrderPage())
            .callback(new CreditCardCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();


    @Step("Setting up the Novalnet API configuration")
    public void verifyGlobalConfiguration() {
        magentoPage.getShopBackEndLoginPage().SigninToShop(SHOP_BACKEND_USERNAME,SHOP_BACKEND_PASSWORD);
        magentoPage.getShopBackEndLoginPage().loadShopGlobalConfig();
        magentoPage.getShopBackEndLoginPage().verifyShopGlobalConfig();
    }

    @Step("Set value for product remain in cart after payment failure")
    public void setProductRemainCartAfterPaymentFailure(boolean enabled){
        magentoPage.getShopBackEndLoginPage().load();
        magentoPage.getShopBackEndLoginPage().loadShopGlobalConfig();
        magentoPage.getShopBackEndLoginPage().enableProductRemainCartDropdown(enabled);
    }


   @Step("Navigate to checkout for the user {0}")
    public void navigateCheckout(String userName){
       ExtentTestManager.logMessage("User email = " + userName);
       magentoPage.getShopUserLoginPage().SigninToShop(userName,SHOP_FRONTEND_PASSWORD);
       magentoPage.getCheckoutPage().load();
       magentoPage.getCheckoutPage().openCheckoutPage();
    }

    @Step("Navigate to checkout for the guest user with the product {0}")
    public void navigateGuestCheckout(String productNameInShop){
        magentoPage.getShopUserLoginPage().load();
        magentoPage.getShopUserLoginPage().addProductToCart(productNameInShop);
        magentoPage.getShopUserLoginPage().navigateToGuestCheckout();
        magentoPage.getShopUserLoginPage().fillCustomerDetailsForGuest();
    }

    @Step("Navigate to checkout for the guest user with the product {0}")
    public void navigateGuestCheckout(String productNameInShop, String countryISO){
        magentoPage.getShopUserLoginPage().load();
        magentoPage.getShopUserLoginPage().addProductToCart(productNameInShop);
        magentoPage.getShopUserLoginPage().navigateToGuestCheckout();
        magentoPage.getShopUserLoginPage().fillCustomerDetailsForGuest(countryISO);
    }
    @Step("Sign in to shop frontend for the user {0}")
    public void signInShopFrontend(String userName){
        magentoPage.getShopUserLoginPage().SigninToShop(userName,SHOP_FRONTEND_PASSWORD);
    }

    @Step("Verify the transaction id, order status, payment name and novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderNumber,String orderStaus, boolean isInvoiceCreated, String paymentComments,String paymentName) {
        magentoPage.getSuccessPage().verifyPaymentNameDisplayed(paymentName);
        magentoPage.getSuccessPage().verifyInvoiceCreated(isInvoiceCreated);
        magentoPage.getOrderPage().searchOrderByOrderID(orderNumber);
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(orderStaus);
        //magentoPage.getOrderPage().verifyNovalnetComments2(paymentComments);
        magentoPage.getOrderPage().verifyInvoiceCreated(isInvoiceCreated);
    }

    @Step("Verify the transaction id, order status, Invoice Created successfully")
    public void statusCommentsVerificationAfterCommunicationBreak(String TID,String orderNumber,String orderStatus, boolean isInvoiceCreated) {
        magentoPage.getOrderPage().load();
        magentoPage.getOrderPage().searchOrderByOrderNumber(orderNumber);
        DriverActions.verifyEquals(magentoPage.getOrderPage().getOrderNovalnetComments().contains(TID),true,"Verify TID value returned from server");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(orderStatus);
        magentoPage.getOrderPage().verifyInvoiceCreated(isInvoiceCreated);
    }

    @Step("Instalment recurring verification")
    public void instalmentRecurringVerification(String cycleAmount, int paid, int remaining, String nextCycleDate){
        DriverActions.reloadPage();
        magentoPage.getOrderPage().openInstalmentMenu();
        if(!nextCycleDate.isBlank()){
            Date cycleDate = DriverActions.getDateFromString("yyyy-MM-dd HH:mm:SS", nextCycleDate);
            magentoPage.getOrderPage().verifyInstalmentInformation(cycleAmount,paid,remaining,DriverActions.changePatternOfDate("MMMM d, yyyy",cycleDate));
        }
        else
            magentoPage.getOrderPage().verifyInstalmentInformation(cycleAmount,paid,remaining);
        magentoPage.getOrderPage().verifyInstalmentTablePaidStatus(paid);
        magentoPage.getOrderPage().verifyInstalmentTableRefundButtonPresent(paid);
        magentoPage.getOrderPage().verifyInstalmentTableTIDPresent(paid,CallbackProperties.getEventTID());
    }

    @Step("Perform Partial Refund Via Shop Backend")
    public void refundPartialViaShopBackend(String orderNumber, String orderAmount, String orderStatus, String refundPaymentType){
        magentoPage.getOrderPage().searchOrderByOrderID(orderNumber);
        int refundAmount = Integer.parseInt(orderAmount);
        magentoPage.getOrderPage().refundOrder(refundAmount);
        var tid = magentoPage.getOrderPage().getNewTID(CallbackProperties.REFUND_COMMENT_, String.valueOf(refundAmount));
        TID_Helper.verifyTIDInformation(tid,String.valueOf(refundAmount),TID_STATUS_CONFIRMED,refundPaymentType);
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(orderStatus);
        magentoPage.getOrderPage().verifyNovalnetComments(CallbackProperties.REFUND_COMMENT_,String.valueOf(refundAmount));
        magentoPage.getOrderPage().verifyCreditMemoCreated(true);
    }

    @Step("Perform Full Refund Via Shop Backend")
    public void refundFullViaShopBackend(String orderNumber,String orderAmount,String orderStatus, String refundPaymentType){
        magentoPage.getOrderPage().searchOrderByOrderID(orderNumber);
        magentoPage.getOrderPage().refundOrder();
        var tid = magentoPage.getOrderPage().getNewTID(CallbackProperties.REFUND_COMMENT_, orderAmount);
        TID_Helper.verifyTIDInformation(tid,orderAmount,TID_STATUS_CONFIRMED,refundPaymentType);
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(orderStatus);
        magentoPage.getOrderPage().verifyNovalnetComments(CallbackProperties.REFUND_COMMENT_,orderAmount);
        magentoPage.getOrderPage().verifyCreditMemoCreated(true);
    }

    @Step("Perform refund via shop backend")
    public void refundInvoiceTypeShopBackend(String orderNumber, String refundAmount, String orderStatus, String tid){
        magentoPage.getOrderPage().searchOrderByOrderID(orderNumber);
        int refund = Integer.parseInt(refundAmount);
        int total = Integer.parseInt(TID_Helper.getTIDAmount(tid));
        if(total == refund){
            magentoPage.getOrderPage().refundOrder();
        }else{
            magentoPage.getOrderPage().refundOrder(refund);
        }
        String paymentType = TID_Helper.getTIDPaymentType(tid);
        if(total > refund){
            if(paymentType.equals(DIRECT_DEBIT_SEPA))
                TID_Helper.verifyTIDInformation(tid, String.valueOf(total-refund), TID_STATUS_CONFIRMED);
            else
                TID_Helper.verifyTIDInformation(tid, String.valueOf(total-refund), TID_STATUS_PENDING);
        }else if(total == refund){
            if(paymentType.equals(DIRECT_DEBIT_SEPA))
                TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_DEACTIVATED);
            else
                TID_Helper.verifyTIDInformation(tid, String.valueOf(refund), TID_STATUS_DEACTIVATED);
        }else{
            Assert.fail("Refund amount is higher than the order amount");
        }
        magentoPage.getOrderPage().verifyNovalnetComments(CallbackProperties.REFUND_COMMENT_, refundAmount);
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(orderStatus);
        magentoPage.getOrderPage().verifyCreditMemoCreated(true);
    }

    @Step("Capture transaction via shop backend")
    public void captureOrder(String orderNumber,String tid){
        magentoPage.getOrderPage().searchOrderByOrderID(orderNumber);
        magentoPage.getOrderPage().captureOrder();
        var payment = TID_Helper.getTIDPaymentType(tid);
        if(payment.equals(CallbackProperties.PAYPAL) || payment.equals(CallbackProperties.INVOICE)){
            if(!payment.equals(CallbackProperties.PAYPAL))
                TID_Helper.verifyTIDStatus(tid,TID_STATUS_PENDING);
            if(payment.equals(CallbackProperties.INVOICE))
                magentoPage.getOrderPage().verifyOrderHistoryPageStatus(PROCESSING_ORDER_STATUS);
            else
                magentoPage.getOrderPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        }else{
            TID_Helper.verifyTIDStatus(tid,TID_STATUS_CONFIRMED);
            magentoPage.getOrderPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        }
        magentoPage.getOrderPage().verifyNovalnetComments(CallbackProperties.CAPTURE_COMMENT_);
        magentoPage.getOrderPage().verifyInvoiceCreated(true);
    }

    @Step("Cancel transaction via shop backend")
    public void cancelOrder(String orderNumber,String tid){
        magentoPage.getOrderPage().searchOrderByOrderID(orderNumber);
        magentoPage.getOrderPage().cancelOrder();
        TID_Helper.verifyTIDStatus(tid,TID_STATUS_DEACTIVATED);
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
        magentoPage.getOrderPage().verifyNovalnetComments(CallbackProperties.CANCEL_COMMENT_2);
        magentoPage.getOrderPage().verifyInvoiceCreated(false);
    }

    public void bookAndVerifyZeroAmountBooking(String orderAmount, String payment){
        magentoPage.getOrderPage().bookTransactionForZeroAmountBooking(orderAmount);
        magentoPage.getOrderPage().verifyZeroAmountBooking(orderAmount,payment);
    }

    //by Nizam
    @Step("Verify the transaction id, order status, payment name and novalnet payment comments appended successfully")
    public void statusCommentsGooglePayVerification(String orderNumber,String orderStaus, boolean isInvoiceCreated, String paymentComments,String paymentName) {
        magentoPage.getSuccessPage().verifyPaymentNameDisplayed(paymentName);
        magentoPage.getSuccessPage().verifyInvoiceCreated(isInvoiceCreated);
        openURL(SHOP_BACK_END_URL+"sales/order/view/order_id/"+orderNumber+"/");
        magentoPage.getShopBackEndLoginPage().login(SHOP_BACKEND_USERNAME,SHOP_BACKEND_PASSWORD);
        openURL(SHOP_BACK_END_URL+"sales/order/view/order_id/"+orderNumber+"/");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(orderStaus);
        //magentoPage.getOrderPage().verifyNovalnetComments2(paymentComments);
        magentoPage.getOrderPage().verifyInvoiceCreated(isInvoiceCreated);
    }


}
