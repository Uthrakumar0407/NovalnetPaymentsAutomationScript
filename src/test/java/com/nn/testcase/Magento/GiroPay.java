package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPIs;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.GiroPayCallbackEvents;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ThunderBirdEmailHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

public class GiroPay extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .orderPage(new OrderPage())
            .callback(new GiroPayCallbackEvents())
            .myAccountPage(new MyAccountPage())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    public void setUpCustomer() {
            ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
            createCustomer(GIROPAY);
            updateProductStock(PRODUCT_GIRO_PAY);
            magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
            magentoPage.getNovalnetAdminPortal().loadAutomationProject();
            paymentActivation(GIROPAY,true);
    }

    @AfterClass
    public void tear(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GIROPAY,true);
    }

    @BeforeMethod()
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 1, description = "Check whether the GiroPay payment order placed successully and partial refund events executed successfully",retryAnalyzer = RetryListener.class)
    public void firstOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GIROPAY, Map.of(
                TESTMODE,false
        ));
        addProductToCart(PRODUCT_GIRO_PAY, 1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        int productStock = getProductStock(PRODUCT_GIRO_PAY);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GIROPAY);
        magentoPage.getCheckoutPage().placeOrderWithGiropay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GIROPAY),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GIROPAY);
        verifyProductStock(PRODUCT_GIRO_PAY,productStock-1);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
    }

    @Test(priority = 2, description = "Check whether the GiroPay payment order placed successully and full refund events executed successfully",retryAnalyzer = RetryListener.class)
    public void secondOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GIROPAY, Map.of(
                TESTMODE,true
        ));
        addProductToCart(PRODUCT_GIRO_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GIROPAY);
        magentoPage.getCheckoutPage().placeOrderWithGiropay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GIROPAY),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GIROPAY);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().transactionRefund(tid,orderAmount);
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,orderAmount);
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,REFUND_ORDER_STATUS);
    }

    @Test(priority = 3, description = "Check whether GiroPay payment order placed successfully by communication break and executing payment event",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        createCustomer(GIROPAY);
        addProductToCart(PRODUCT_GIRO_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        int productStock = getProductStock(PRODUCT_GIRO_PAY);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GIROPAY);
        magentoPage.getCheckoutPage().clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().waitForGiroPayNewRedirectionPage();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = magentoPage.getNovalnetAdminPortal().getTID(MagentoAPIs.getCustomerEmail());
        //String TID = magentoPage.getCheckoutPage().communicationBreakGetGiroPayTIDPendingOrderNumber();
        String orderNumber=TID_Helper.getOrderNumber(tid);
        magentoPage.getCallback().communicationBreakSuccess(tid,"");
        statusCommentsVerificationAfterCommunicationBreak(tid,orderNumber,COMPLETION_ORDER_STATUS, true);
        verifyProductStock(PRODUCT_GIRO_PAY,productStock-1);

    }


    @Test(priority = 4, description = "Check whether GiroPay payment order failed successfully by communication break and executing payment event",retryAnalyzer = RetryListener.class)
    public void fourthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        createCustomer(GIROPAY);
        addProductToCart(PRODUCT_GIRO_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        int productStock = getProductStock(PRODUCT_GIRO_PAY);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GIROPAY);
        magentoPage.getCheckoutPage().clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().waitForGiroPayNewRedirectionPage();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        //String TID = magentoPage.getCheckoutPage().communicationBreakGetGiroPayTIDPendingOrderNumber();
        String tid = magentoPage.getNovalnetAdminPortal().getTID(MagentoAPIs.getCustomerEmail());
        String orderNumber=TID_Helper.getOrderNumber(tid);
        magentoPage.getCallback().communicationBreakFailure(tid,"");
        statusCommentsVerificationAfterCommunicationBreak(tid,orderNumber,CANCELLATION_ORDER_STATUS, false);
        verifyProductStock(PRODUCT_GIRO_PAY,productStock);
    }

    @Test(priority = 5, description = "Verify the display of error text on the checkout page in case a transaction is aborted, leading to a change in the order status to failed"/*,retryAnalyzer = RetryListener.class*/)
    public void fifthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        createCustomer(GIROPAY);
        addProductToCart(PRODUCT_GIRO_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        int productStock = getProductStock(PRODUCT_GIRO_PAY);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GIROPAY);
        magentoPage.getCheckoutPage().cancelAtNewGiropayRedirection();
        magentoPage.getCheckoutPage().verifyCheckoutErrorMessage(true);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = magentoPage.getNovalnetAdminPortal().getTID(MagentoAPIs.getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_FAILURE, GIROPAY);
        statusCommentsVerificationAfterCommunicationBreak(tid,orderNumber,CANCELLATION_ORDER_STATUS,false);
        verifyProductStock(PRODUCT_GIRO_PAY,productStock);
    }

    //@Test(priority = 6, description = "Verify the order and invoice mail received with novalnet comments",retryAnalyzer = RetryListener.class)
    public void mailVerifyOrder(){
        magentoPage.getShopUserLoginPage().logout();
        createCustomer(GIROPAY,"gopinath_m@novalnetsolutions.com");
        addProductToCart(PRODUCT_GIRO_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("DE");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GIROPAY);
        magentoPage.getCheckoutPage().placeOrderWithGiropay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getSuccessPage().getOrderNumber(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GIROPAY),
                paymentComments = magentoPage.getSuccessPage().getPaymentComment();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GIROPAY);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailOrderSubjectText,orderNumber);
        DriverActions.verifyEquals(mailContent.contains(paymentName),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(paymentComments),true,"Verify order mail has novalnet payment comments");
        String invoiceMailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailInvoiceSubjectText,orderNumber);
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentName),true,"Verify invoice mail has payment name");
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentComments),true,"Verify invoice mail has payment comments");
    }

    @Test(priority = 6, description = "Check whether the test transaction is successful with guest user",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        navigateGuestCheckout(PRODUCT_GUEST);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GIROPAY);
        magentoPage.getCheckoutPage().placeOrderWithGiropay();
        String orderNumber = magentoPage.getSuccessPage().getOrderNumberForGuestOrder();
        String tid = NovalnetAPIs.getRecentTransactionTID(orderNumber);
        DriverActions.verifyEquals(TID_Helper.getTIDStatus(tid),TID_STATUS_CONFIRMED,"Verify tid status for guest user");
        magentoPage.getOrderPage().load();
        magentoPage.getOrderPage().searchOrderByOrderNumber(orderNumber);
        DriverActions.verifyEquals(magentoPage.getOrderPage().getOrderNovalnetComments().contains(tid),true,"Verify TID value returned from server");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        magentoPage.getOrderPage().verifyInvoiceCreated(true);
    }
    //@Test(priority = 8,description = "Check whether the payment logo displayed as per admin portal configurations ",retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogo(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        createCustomer(GIROPAY);
        addProductToCart(PRODUCT_GIRO_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("Giropay",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Giropay",true);
    }

    //@Test(priority = 7, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GIROPAY,false);
        createCustomer(GIROPAY);
        addProductToCart(PRODUCT_GIRO_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(GIROPAY,false);
    }
}
