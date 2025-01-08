package com.nn.testcase.Magento;

import com.nn.Magento.Constants;
import com.nn.apis.MagentoAPIs;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.SofortCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ThunderBirdEmailHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.FAILURE_ORDER_STATUS;
import static com.nn.constants.Constants.PRODUCT_1;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

public class Sofort extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .orderPage(new OrderPage())
            .successPage(new SuccessPage())
            .callback(new SofortCallbackEvents())
            .myAccountPage(new MyAccountPage())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    public void setUpCustomer() {
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
        createCustomer(ONLINE_TRANSFER);
        updateProductStock(PRODUCT_SOFORT);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(ONLINE_TRANSFER,true);
    }

    @AfterClass
    public void tear(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(ONLINE_TRANSFER,true);
    }

    @AfterMethod(alwaysRun = true)
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 1, description = "Check whether the Sofort payment order placed successully and chargeback, credit and refund events executed successfully"/*,retryAnalyzer = RetryListener.class*/)
    public void firstOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(ONLINE_TRANSFER, Map.of(
                TESTMODE,false
        ));
        addProductToCart(PRODUCT_SOFORT,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        //    int productStock = getProductStock(PRODUCT_1);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(ONLINE_TRANSFER);
        magentoPage.getCheckoutPage().placeOrderWithGiropay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(ONLINE_TRANSFER),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, ONLINE_TRANSFER);
        //verifyProductStock(PRODUCT_1,productStock-1);file:///home/gopinath_m/gopinath/AutomationPaymentTests/ExtentReports/ExtentReport.html
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().chargeback(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CHARGEBACK_COMMENT_,orderAmount);
        magentoPage.getCallback().debtCollectionDE(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        magentoPage.getCallback().onlineTransferCredit(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        magentoPage.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
    }

    @Test(priority = 2, description = "Check whether the Sofort payment order placed successully and full refund events executed successfully",retryAnalyzer = RetryListener.class)
    public void secondOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(ONLINE_TRANSFER, Map.of(
                TESTMODE,true
        ));
        addProductToCart(PRODUCT_SOFORT,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(ONLINE_TRANSFER);
        magentoPage.getCheckoutPage().placeOrderWithOnlineTransfer();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(ONLINE_TRANSFER),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, ONLINE_TRANSFER);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().transactionRefund(tid,orderAmount);
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,orderAmount);
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,REFUND_ORDER_STATUS);
    }



    @Test(priority = 3, description = "Check whether Sofort payment order placed successfully by communication break and executing payment event",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        addProductToCart(PRODUCT_SOFORT,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        int productStock = getProductStock(PRODUCT_SOFORT);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(ONLINE_TRANSFER);
        String TID = magentoPage.getCheckoutPage().communicationBreakGetSofortTIDPendingOrderNumber();
        String orderNumber=TID_Helper.getOrderNumber(TID);
        magentoPage.getCallback().communicationBreakSuccess(TID,"");
        statusCommentsVerificationAfterCommunicationBreak(TID,orderNumber,COMPLETION_ORDER_STATUS, true);
        verifyProductStock(PRODUCT_SOFORT,productStock-1);

    }


    @Test(priority = 4, description = "Check whether Sofort payment order failed successfully by communication break and executing payment event",retryAnalyzer = RetryListener.class)
    public void fourthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        addProductToCart(PRODUCT_SOFORT,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        int productStock = getProductStock(PRODUCT_SOFORT);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(ONLINE_TRANSFER);
        String TID = magentoPage.getCheckoutPage().communicationBreakGetSofortTIDPendingOrderNumber();
        String orderNumber=TID_Helper.getOrderNumber(TID);
        magentoPage.getCallback().communicationBreakFailure(TID,"");
        statusCommentsVerificationAfterCommunicationBreak(TID,orderNumber,CANCELLATION_ORDER_STATUS, false);
        verifyProductStock(PRODUCT_SOFORT,productStock);
    }

    @Test(priority = 5, description = "Verify the display of error text on the checkout page in case a transaction is aborted, leading to a change in the order status to failed",retryAnalyzer = RetryListener.class)
    public void fifthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        addProductToCart(PRODUCT_SOFORT,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        int productStock = getProductStock(PRODUCT_SOFORT);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(ONLINE_TRANSFER);
        String TID = magentoPage.getCheckoutPage().cancelAtSofortRedirection();
        magentoPage.getCheckoutPage().verifyCheckoutErrorMessage(REDIRECT_END_USER_CANCEL_ERROR);
        String orderNumber=TID_Helper.getOrderNumber(TID);
        TID_Helper.verifyTIDInformation(TID, "0", TID_STATUS_FAILURE, ONLINE_TRANSFER);
        statusCommentsVerificationAfterCommunicationBreak(TID,orderNumber,CANCELLATION_ORDER_STATUS, false);
        verifyProductStock(PRODUCT_SOFORT,productStock);
    }

   // @Test(priority = 6, description = "Verify novalnet payment comments displayed in the mail after successful order",retryAnalyzer = RetryListener.class)
    public void mailVerifyOrder(){
        magentoPage.getShopUserLoginPage().logout();
        createCustomer(ONLINE_TRANSFER,"gopinath_m@novalnetsolutions.com");
        addProductToCart(PRODUCT_SOFORT,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("DE");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(ONLINE_TRANSFER);
        magentoPage.getCheckoutPage().placeOrderWithOnlineTransfer();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getSuccessPage().getOrderNumber(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(ONLINE_TRANSFER),
                paymentComments = magentoPage.getSuccessPage().getPaymentComment();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, ONLINE_TRANSFER);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailOrderSubjectText,orderNumber);
        DriverActions.verifyEquals(mailContent.contains(paymentName),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(paymentComments),true,"Verify order mail has novalnet payment comments");
        magentoPage.getCallback().invoiceCredit(tid,orderAmount);
        DriverActions.reloadPage();
        String invoiceMailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailInvoiceSubjectText,orderNumber);
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentName),true,"Verify invoice mail has payment name");
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentComments),true,"Verify invoice mail has novalnet payment comments");
    }

    @Test(priority = 6, description = "Check whether the test transaction is successful with guest user",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        navigateGuestCheckout(PRODUCT_GUEST);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(ONLINE_TRANSFER);
        magentoPage.getCheckoutPage().placeOrderWithOnlineTransfer();
        String orderNumber = magentoPage.getSuccessPage().getOrderNumberForGuestOrder();
        String tid = NovalnetAPIs.getRecentTransactionTID(orderNumber);
        DriverActions.verifyEquals(TID_Helper.getTIDStatus(tid),TID_STATUS_CONFIRMED,"Verify tid status for guest user");
        magentoPage.getOrderPage().load();
        magentoPage.getOrderPage().searchOrderByOrderNumber(orderNumber);
        DriverActions.verifyEquals(magentoPage.getOrderPage().getOrderNovalnetComments().contains(tid),true,"Verify TID value returned from server");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        magentoPage.getOrderPage().verifyInvoiceCreated(true);
    }
    //@Test(priority = 9,description = "Check whether the payment logo displayed as per admin portal configurations ",retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogo(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        createCustomer(ONLINE_TRANSFER);
        addProductToCart(PRODUCT_SOFORT,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("Sofort",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Sofort",true);
    }

    //@Test(priority = 7, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(ONLINE_TRANSFER,false);
        createCustomer(ONLINE_TRANSFER);
        addProductToCart(PRODUCT_SOFORT,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(ONLINE_TRANSFER,false);
    }
}
