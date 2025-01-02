package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPI_Helper;
import com.nn.apis.MagentoAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.GooglePayCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.GmailEmailRetriever;
import com.nn.utilities.GooglePayHelper;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.Magento.Constants.PRODUCT_CREDIT_CARD_PAY;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.apis.MagentoAPI_Helper.addProductToCart;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.GOOGLEPAY;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.DriverActions.sleep;
import static com.nn.utilities.GooglePayHelper.launchDriverWithGmail;

public class GooglePay extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .orderPage(new OrderPage())
            .myAccountPage(new MyAccountPage())
            .callback(new GooglePayCallbackEvents())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .testData2(ExcelHelpers.declineCreditCards())
            .build();


    @BeforeClass(alwaysRun = true)
    public void setUpCustomer() {
        DriverManager.quit();
        launchDriverWithGmail();
        createCustomer(GOOGLEPAY);
        updateProductStock(PRODUCT_CREDIT_CARD_PAY);


    }
    @BeforeMethod
    public void clearShopCart(){
       MagentoAPI_Helper.clearCart();
    }
     @AfterClass
    public void tear() {
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GOOGLEPAY,true);
    }

    @Test(priority = 1, description = "Check whether the test transaction is successful with payment action set to Capture, " +
            "verifying token exist in the response, partial refund admin, chargeback and credit events ")
    public void firstOrder()  {
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,CAPTURE,
                ENF_3D_GPAY,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton()
                .payWithGooglePay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GOOGLEPAY),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GOOGLEPAY);
        TID_Helper.verifyPaymentTokenExist(tid,true);
        sleep(1);
        statusCommentsGooglePayVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        refundPartialViaShopBackend(orderNumber,orderAmount,PROCESSING_ORDER_STATUS,GOOGLEPAY_BOOKBACK);
        magentoPage.getCallback().chargeback(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CHARGEBACK_COMMENT_,orderAmount);
        magentoPage.getCallback().googlePayRepresentment(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
    }
    @Test(priority = 2, description = "Check whether the test order is successful with payment action set to Authorize" +
            " verifying 3d secure, performing capture in shop backend and full refund via shop backend"/*,retryAnalyzer = RetryListener.class*/)
    public void secondOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                ENF_3D_GPAY,true
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton()
                .payWithGooglePay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GOOGLEPAY),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        TID_Helper.verifyPaymentTokenExist(tid,true);
        sleep(1);
        //   statusCommentsGooglePayVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        captureOrder(orderNumber,tid);
        refundFullViaShopBackend(orderNumber,orderAmount,REFUND_ORDER_STATUS,GOOGLEPAY_BOOKBACK);
    }
    @Test(priority = 3, description = "Check whether test order with authorize minimum amount less than order amount is successful" +
            ", execute transaction capture event, execute transaction refund event partial order amount",retryAnalyzer = RetryListener.class)
    public void thirdOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"100",
                ENF_3D_GPAY,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton()
                .payWithGooglePay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GOOGLEPAY),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        //statusCommentsGooglePayVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        sleep(1);
        magentoPage.getCallback().transactionCapture(tid);
        verifyNovalnetComments(orderNumber,CAPTURE_COMMENT_);
        verifyInvoiceCreated(orderNumber,true);
        magentoPage.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
    }
    @Test(priority = 4, description = "Check whether the test order with authorize minimum amount greater than order amount is successful," +
            " enforce 3d outside EU set to true, verifying redirection takes place for non euro card, execute reminder and collection events.",retryAnalyzer = RetryListener.class)
    public void fourthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"10000",
                ENF_3D_GPAY,true
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton()
                .payWithGooglePay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GOOGLEPAY),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GOOGLEPAY);
        sleep(1);
        //  statusCommentsGooglePayVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().paymentReminderOne(tid);
        verifyNovalnetComments(orderNumber,REMINDER_ONE_COMMENT_);
        magentoPage.getCallback().paymentReminderTwo(tid);
        verifyNovalnetComments(orderNumber,REMINDER_TWO_COMMENT_);
        magentoPage.getCallback().submissionToCollection(tid);
        verifyNovalnetComments(orderNumber,COLLECTION_COMMENT_);
        magentoPage.getCallback().googlePayRepresentment(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_);
    }
    @Test(priority = 5, description = "Check whether the authorize test order is cancelled via shop backend with payment action set to Authorize",retryAnalyzer = RetryListener.class)
    public void fifthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        String productAmount = getProductPrice(PRODUCT_CREDIT_CARD_PAY);
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,Integer.parseInt(productAmount)+SHIPPING_RATE,
                ENF_3D_GPAY,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton()
                .payWithGooglePay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GOOGLEPAY),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        sleep(1);
        //   statusCommentsGooglePayVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        cancelOrder(orderNumber,tid);
    }
    @Test(priority = 6, description = "Check whether the authorize test order is cancelled via callback with payment action set to Authorize",retryAnalyzer = RetryListener.class)
    public void sixthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                ENF_3D_GPAY,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton()
                .payWithGooglePay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GOOGLEPAY),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GOOGLEPAY);
        sleep(1);
        statusCommentsGooglePayVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        cancelOrder(orderNumber,tid);
        magentoPage.getCallback().transactionCancel(tid);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyInvoiceCreated(orderNumber,false);
    }
    @Test(priority = 7, dataProvider = "googlePayCurrencies", description = "Check whether the test transaction is successful with multiple currencies",retryAnalyzer = RetryListener.class)
    public void multiCurrencyOrder(String currency){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,CAPTURE,
                ENF_3D_GPAY,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().changeCurrency(currency); // currency will be provided by dataProvider
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton()
                .payWithGooglePay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        DriverActions.verifyEquals(TID_Helper.getTIDCurrency(tid),currency,"Verify tid currency ");
    }
    @DataProvider
    public Object[][] googlePayCurrencies(){
        return new Object[][]{{"CHF"},{"GBP"}, {"INR"}, {"JPY"},{"PLN"},{"USD"},{"RUB"}};
    }
    @Test(priority = 8, description = "Check whether the test transaction is successful with guest user ",retryAnalyzer = RetryListener.class)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,CAPTURE,
                ENF_3D_GPAY,false
        ));
        navigateGuestCheckout(PRODUCT_GUEST);
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton()
                .payWithGooglePay();
        String orderNumber = magentoPage.getSuccessPage().getOrderNumberForGuestOrder();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = magentoPage.getNovalnetAdminPortal().getTIDUsingOrderNumber(orderNumber);
        DriverActions.verifyEquals(TID_Helper.getTIDStatus(tid),TID_STATUS_CONFIRMED,"Verify tid status for guest user");
        sleep(1);
        magentoPage.getOrderPage().load();
        magentoPage.getOrderPage().searchOrderByOrderNumber(orderNumber);
        DriverActions.verifyEquals(magentoPage.getOrderPage().getOrderNovalnetComments().contains(tid),true,"Verify TID value returned from server");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        magentoPage.getOrderPage().verifyInvoiceCreated(true);
    }
    @Test(priority = 9, description = "Check whether the business name displayed in the Gpay sheet as per the admin portal configuration "/*,retryAnalyzer = RetryListener.class*/)
    public void seventhOrder()  {
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                BUSINESS_NAME,"Magento@123"
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton();
        magentoPage.getCheckoutPage().isBusinessNameDisplayed("Magento@123",true);
    }
  //  @Test(priority = 10,description = "Check whether the payment logo displayed as per admin portal configurations "/*,retryAnalyzer = RetryListener.class*/)
    public void verifyPaymentLogo(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getShopUserLoginPage().SigninToShop(GooglePayHelper.EMAILID, GooglePayHelper.PASS);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Google Pay",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Google Pay",true);
    }
    @DataProvider
    public Object[][] paymentNames(){
        return new Object[][]{{"Prepayment"},{"PostFinance E-Finance"},{"PostFinance Card"},{"Direct Debit ACH"},{"Google Pay"},{"Barzahlen/viacash"},{"PayPal"},{"Bancontact"},{"iDEAL"},
                {"Alipay"},{"WeChat Pay"},{"Giropay"},{"Online bank transfer"},{"Multibanco"},{"Trustly"},{"Sofort "},{"eps"},{"Przelewy24"},{"Credit/Debit Cards"},{"Invoice"},{"Direct Debit SEPA"},
                {"Instalment by SEPA direct debit"},{"Instalment by invoice"}};
    }
    //@Test(priority = 10, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal",retryAnalyzer = RetryListener.class)
    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GOOGLEPAY,false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentDisplayed(GOOGLEPAY,false);
    }

    //Zero amount not implemented in payment module
    /*@Test(priority = 12, description = "Check whether the zero amount booking order is placed successfully, verify token exist in the response and amount booked in the shop backend")
    public void seventhOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GOOGLEPAY, Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,AUTHORIZE_ZERO_AMOUNT,
                MIN_AUTH_AMOUNT,"",
                ENF_3D_GPAY,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(GooglePayHelper.EMAILID, GooglePayHelper.PASS);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isPaymentMethodDisplayed(GOOGLEPAY);
        magentoPage.getCheckoutPage().clickGooglePayButton()
                .payWithGooglePay();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GOOGLEPAY),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_CONFIRMED, GOOGLEPAY);
        TID_Helper.verifyPaymentTokenExist(tid);
        statusCommentsGooglePayVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        bookAndVerifyZeroAmountBooking(orderAmount,GOOGLEPAY);
    }*/




}