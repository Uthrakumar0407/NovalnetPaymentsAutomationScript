package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPIs;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.CashPaymentCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ThunderBirdEmailHelper;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

public class CashPayment extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .orderPage(new OrderPage())
            .myAccountPage(new MyAccountPage())
            .callback(new CashPaymentCallbackEvents())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .build();


    @BeforeClass(alwaysRun = true)
    public void setUpCustomer() {
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
        createCustomer(CASHPAYMENT);
        updateProductStock(PRODUCT_CASH_PAY);
//        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
//        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
//        paymentActivation(CASHPAYMENT,true);
    }

    //@AfterClass
    public void tear(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(CASHPAYMENT,true);
    }

    @AfterMethod()
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 1, description = "Check whether the cash payment transaction is successful , invoice credit and partial refund executed ",retryAnalyzer = RetryListener.class)
    public void firstOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CASHPAYMENT, Map.of(
                TESTMODE,false,
                DUE_DATE,""
          ));
        addProductToCart(PRODUCT_CASH_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CASHPAYMENT)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        //magentoPage.getSuccessPage().verifyDueDate(14);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CASHPAYMENT),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, CASHPAYMENT,14);
        magentoPage.getCallback().cashpaymentCredit(tid,"");
        DriverActions.reloadPage();
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        refundInvoiceTypeShopBackend(orderNumber,String.valueOf(Integer.parseInt(orderAmount)/2),PROCESSING_ORDER_STATUS,tid);

    }

    @Test(priority = 2, description = "Check whether the cash payment transaction is successful , invoice credit and full refund executed ",retryAnalyzer = RetryListener.class)
    public void secondOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CASHPAYMENT, Map.of(
                TESTMODE,true,
                DUE_DATE,7
        ));
        addProductToCart(PRODUCT_CASH_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CASHPAYMENT)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        //magentoPage.getSuccessPage().verifyDueDate(7);
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CASHPAYMENT),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, CASHPAYMENT,7);
        magentoPage.getCallback().cashpaymentCredit(tid,"");
        DriverActions.reloadPage();
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        refundInvoiceTypeShopBackend(orderNumber,orderAmount,REFUND_ORDER_STATUS,tid);
    }

    //@Test(priority = 3, description = "Verify the order and invoice mail received with novalnet comments",retryAnalyzer = RetryListener.class)
    public void mailVerifyOrder(){
        magentoPage.getShopUserLoginPage().logout();
        createCustomer(CASHPAYMENT,"gopinath_m@novalnetsolutions.com");
        addProductToCart(PRODUCT_CASH_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("DE");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(CASHPAYMENT).clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getSuccessPage().getOrderNumber(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = "Barzahlen/viacash",
                paymentComments = magentoPage.getSuccessPage().getPaymentComment();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, CASHPAYMENT);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailOrderSubjectText,orderNumber);
        DriverActions.verifyEquals(mailContent.contains(paymentName),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(paymentComments),true,"Verify order mail has novalnet payment comments");
        magentoPage.getCallback().cashpaymentCredit(tid,"");
        DriverActions.reloadPage();
        String invoiceMailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailInvoiceSubjectText,orderNumber);
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentName),true,"Verify invoice mail has payment name");
        DriverActions.verifyEquals(invoiceMailContent.contains(tid),true,"Verify invoice mail has tid");
    }

    @Test(priority = 3, description = "Check whether the test transaction is successful with guest user",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        updateProductStock(PRODUCT_GUEST_CODE);
        navigateGuestCheckout(PRODUCT_GUEST);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(CASHPAYMENT).clickPlaceOrderBtn();
        String orderNumber = magentoPage.getSuccessPage().getOrderNumberForGuestOrder();
        String tid = NovalnetAPIs.getRecentTransactionTID(orderNumber);
        DriverActions.verifyEquals(TID_Helper.getTIDStatus(tid),TID_STATUS_PENDING,"Verify tid status for guest user");
        magentoPage.getOrderPage().load();
        magentoPage.getOrderPage().searchOrderByOrderNumber(orderNumber);
        DriverActions.verifyEquals(magentoPage.getOrderPage().getOrderNovalnetComments().contains(tid),true,"Verify TID value returned from server");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(PENDING_ORDER_STATUS);
        magentoPage.getOrderPage().verifyInvoiceCreated(false);
    }
    //@Test(priority = 4,description = "Check whether the payment logo displayed as per admin portal configurations ",retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogo(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        createCustomer(CASHPAYMENT);
        addProductToCart(PRODUCT_CASH_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("Barzahlen/viacash",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Barzahlen/viacash",true);
    }

    //test case is removed because cashpayment activation deactivation takes some time.
    //@Test(priority = 3, description = "Verify that CashPayment payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(CASHPAYMENT,false);
        createCustomer(CASHPAYMENT);
        addProductToCart(PRODUCT_CASH_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(CASHPAYMENT,false);
    }


}
