package com.nn.testcase.Magento;

import com.nn.Magento.Constants;
import com.nn.apis.MagentoAPI_Helper;
import com.nn.apis.MagentoAPIs;
import com.nn.apis.NovalnetAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.CallbackProperties;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ThunderBirdEmailHelper;
import com.nn.v13.IframeV13;
import io.qameta.allure.testng.AllureTestNg;
import org.openqa.selenium.By;
import org.testng.annotations.*;

import java.util.*;
import java.util.stream.Stream;

import static com.nn.Magento.Constants.*;
import static com.nn.Magento.Constants.PROCESSING_ORDER_STATUS;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.FAILURE_ORDER_STATUS;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.DriverActions.switchToDefaultContent;
import static com.nn.utilities.DriverActions.switchToFrame;

@Listeners({AllureTestNg.class})
public class CreditCard extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .orderPage(new OrderPage())
            .myAccountPage(new MyAccountPage())
            .callback(new CreditCardCallbackEvents())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .iframeV13(new IframeV13())
            .testData2(ExcelHelpers.declineCreditCards())
            .build();


    @BeforeClass(alwaysRun = true)
    public void setUpCustomer() {
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
        createCustomer(CREDITCARD);
        updateProductStock(PRODUCT_CREDIT_CARD_PAY);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(CREDITCARD,true);
    }

    @AfterClass(alwaysRun = true)
    public void tear() {
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(CREDITCARD,true);
    }

    @AfterMethod(alwaysRun = true)
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 1, description = "Check whether the test transaction is successful with payment action set to Capture, " +
            "Inline form set to true, using direct CC card, verifying token exist in the response, partial refund admin, chargeback and credit events "/*,retryAnalyzer = RetryListener.class*/)
    public void firstOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                INLINE,true,
                ENF_3D,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CREDITCARD),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, CREDITCARD);
        TID_Helper.verifyPaymentTokenExist(tid,true);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        refundPartialViaShopBackend(orderNumber,orderAmount,PROCESSING_ORDER_STATUS,CREDITCARD_BOOKBACK);
        magentoPage.getCallback().chargeback(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CHARGEBACK_COMMENT_,orderAmount);
        magentoPage.getCallback().creditEntryCreditCard(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        magentoPage.getCallback().debtCollectionCreditCard(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);
        magentoPage.getCallback().bankTransferByEndCustomer(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_,orderAmount);

    }

    @Test(priority = 2, description = "Check whether the test order is successful with payment action set to Authorize, verifying masked card details in the checkout" +
            " using redirect card, verifying cc 3d, performing capture in shop backend and full refund via shop backend"/*,retryAnalyzer = RetryListener.class*/)
    public void secondOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                INLINE,false,
                ENF_3D,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
               .verifyMaskedCCData(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"))
              .clickNewCardCC()
                .verifyInlineFormDisplayed(false)
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberRedirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        switchToFrame(By.cssSelector("#novalnetPaymentIFrame"));
        magentoPage.getCheckoutPage().fillCreditCardForm(magentoPage.getTestData().get("CardNumberRedirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CREDITCARD),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, CREDITCARD);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        captureOrder(orderNumber,tid);
        refundFullViaShopBackend(orderNumber,orderAmount,REFUND_ORDER_STATUS,CREDITCARD_BOOKBACK);
    }

    @Test(priority = 3, description = "Check whether test order with authorize minimum amount less than order amount is successful, using saved token to place order" +
            ", execute transaction capture event, execute transaction refund event partial order amount"/*,retryAnalyzer = RetryListener.class*/)
    public void thirdOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"100",
                INLINE,true,
                ENF_3D,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
               // .verifyMaskedCCData(magentoPage.getTestData().get("CardNumberRedirect"), magentoPage.getTestData().get("ExpDate"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CREDITCARD),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, CREDITCARD);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCapture(tid);
        verifyNovalnetComments(orderNumber,CAPTURE_COMMENT_);
        verifyInvoiceCreated(orderNumber,true);
        magentoPage.getCallback().transactionRefund(tid,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyNovalnetComments(orderNumber,REFUND_COMMENT_,String.valueOf(Integer.parseInt(orderAmount)/2));
        verifyCreditMemoCreated(orderNumber,true);
        verifyOrderStatus(orderNumber,PROCESSING_ORDER_STATUS);
    }

    @Test(priority = 4, description = "Check whether the test order with authorize minimum amount greater than order amount is successful, using new card details instead of token," +
            " enforce 3d outside EU set to true, verifying redirection takes place for non euro card, execute reminder and collection events."/*,retryAnalyzer = RetryListener.class*/)
    public void fourthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"10000",
                INLINE,true,
                ENF_3D,true
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("US");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .clickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        switchToFrame(By.cssSelector("#novalnetPaymentIFrame"));
        magentoPage.getCheckoutPage() .fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
        .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CREDITCARD),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, CREDITCARD);
        statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,true,paymentComments,paymentName);
        magentoPage.getCallback().paymentReminderOne(tid);
        verifyNovalnetComments(orderNumber,REMINDER_ONE_COMMENT_);
        magentoPage.getCallback().paymentReminderTwo(tid);
        verifyNovalnetComments(orderNumber,REMINDER_TWO_COMMENT_);
        magentoPage.getCallback().submissionToCollection(tid);
        verifyNovalnetComments(orderNumber,COLLECTION_COMMENT_);
        magentoPage.getCallback().creditCardRepresentment(tid,orderAmount);
        verifyNovalnetComments(orderNumber,CREDIT_COMMENT_);
    }

    @Test(priority = 5, description = "Check whether the authorize test order is cancelled via shop backend with payment action set to Authorize"/*,retryAnalyzer = RetryListener.class*/)
    public void fifthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        String productAmount = getProductPrice(PRODUCT_CREDIT_CARD_PAY);
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,Integer.parseInt(productAmount),
                INLINE,true,
                ENF_3D,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                //.fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CREDITCARD),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, CREDITCARD);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        cancelOrder(orderNumber,tid);
    }

    @Test(priority = 6, description = "Check whether the authorize test order is cancelled via callback with payment action set to Authorize"/*,retryAnalyzer = RetryListener.class*/)
    public void sixthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                INLINE,true,
                ENF_3D,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                //.fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CREDITCARD),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, CREDITCARD);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        magentoPage.getCallback().transactionCancel(tid);
        verifyNovalnetComments(orderNumber,CANCEL_COMMENT_2);
        verifyOrderStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        verifyInvoiceCreated(orderNumber,false);
    }

    @Test(priority = 7, description = "Check whether the zero amount booking order is placed successfully, verify token exist in the response and amount booked in the shop backend"/*,retryAnalyzer = RetryListener.class*/)
    public void seventhOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE_ZERO_AMOUNT,
                MIN_AUTH_AMOUNT,"",
                INLINE,true,
                ENF_3D,false
        ));
        createCustomer(CREDITCARD);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CREDITCARD),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();
        TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_CONFIRMED, CREDITCARD);
        TID_Helper.verifyPaymentTokenExist(tid);
        statusCommentsVerification(orderNumber,ONHOLD_ORDER_STATUS,false,paymentComments,paymentName);
        bookAndVerifyZeroAmountBooking(orderAmount,CREDITCARD);
    }

    @Test(priority = 8, dependsOnMethods = "seventhOrder", description = "Check whether the token received from zero amount booking order is not displayed in the checkout "/*,retryAnalyzer = RetryListener.class*/)
    public void eighthOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE_ZERO_AMOUNT,
                MIN_AUTH_AMOUNT,"",
                INLINE,true,
                ENF_3D,false
        ));
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyMaskedCCDataDisplayed(false);
    }

    @Test(priority = 9, description = "Check whether the zero amount booking order with redirect card is by communication break, verifying zero amount booking menu displayed" +
            " book the amount receive error `reference transaction not successful`, execute initial level , verify zero amount booking for this order"/*,retryAnalyzer = RetryListener.class*/)
    public void ninthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE_ZERO_AMOUNT,
                MIN_AUTH_AMOUNT,"",
                INLINE,true,
                ENF_3D,false
        ));
        createCustomer(CREDITCARD);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberRedirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().waitForCCAuthenticationPage();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = magentoPage.getNovalnetAdminPortal().getTID(MagentoAPIs.getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        magentoPage.getCallback().communicationBreakSuccess(tid,"0");
        statusCommentsVerificationAfterCommunicationBreak(tid,orderNumber,ONHOLD_ORDER_STATUS,false);
        magentoPage.getOrderPage().bookTransactionForZeroAmountBookingFailureOrder();
    }

    @Test(priority = 10, description = "Check whether the token is displaying in the checkout page for communication break handled transaction"/*,retryAnalyzer = RetryListener.class*/)
    public void tenthOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                MIN_AUTH_AMOUNT,"",
                INLINE,true,
                ENF_3D,false
        ));
        createCustomer(CREDITCARD);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberRedirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().waitForCCAuthenticationPage();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = magentoPage.getNovalnetAdminPortal().getTID(MagentoAPIs.getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        magentoPage.getCallback().communicationBreakSuccess(tid,"");
        statusCommentsVerificationAfterCommunicationBreak(tid,orderNumber,COMPLETION_ORDER_STATUS,true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyMaskedCCDataDisplayed(false);
    }

    @Test(priority = 11, description ="Check whether the user gets the appropriate error message displays on cart page after using an expired card enabling product remain in cart," +
            " verify product remain in cart"/*,retryAnalyzer = RetryListener.class*/)
    public void expiredCardProductRemain(){
        setProductRemainCartAfterPaymentFailure(true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData2().get("Expired"), magentoPage.getTestData2().get("ExpDate"), magentoPage.getTestData2().get("CVV"))
                .clickPlaceOrderBtn();

        //commented by nizam because error message getting after click submitBtN,not going 3d secure page
        //magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Credit card payment not possible: card expired");
       /* DriverActions.verifyEquals(isCartEmpty(),false,"Verify cart is not empty after payment failure");*/
        //magentoPage.getCheckoutPage().isShippingPageDisplayed(true);
    }

    @Test(priority = 12, description ="Check whether the user gets the appropriate error message displays on cart page after using an restricted card enabling product remain in cart," +
            " verify product remain in cart"/*,retryAnalyzer = RetryListener.class*/)
    public void restrictedCardProductRemain(){
        setProductRemainCartAfterPaymentFailure(true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData2().get("Restricted"), magentoPage.getTestData2().get("ExpDate"), magentoPage.getTestData2().get("CVV"))
                .clickPlaceOrderBtn();
        //magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Restricted card");
      //  DriverActions.verifyEquals(isCartEmpty(),false,"Verify cart is not empty after payment failure");
      //  magentoPage.getCheckoutPage().isShippingPageDisplayed(true);
    }

    @Test(priority = 13, description ="Check whether the user gets the appropriate error message displays on cart page after using card with Insufficient funds or credit limit exceeded card data." +
            " card enabling product remain in cart, verify product remain in cart"/*,retryAnalyzer = RetryListener.class*/)
    public void insufficientFundsCardProductRemain(){
        setProductRemainCartAfterPaymentFailure(true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData2().get("InsufficientFunds"), magentoPage.getTestData2().get("ExpDate"), magentoPage.getTestData2().get("CVV"))
                .clickPlaceOrderBtn();
       // magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Insufficient funds or credit limit exceeded");
       // DriverActions.verifyEquals(isCartEmpty(),false,"Verify cart is not empty after payment failure");
        //magentoPage.getCheckoutPage().isShippingPageDisplayed(true);
    }

    @Test(priority = 14, description ="Check whether the user gets the appropriate error message displays on cart page by cancelling the payment on the OTP page.." +
            " card enabling product remain in cart, verify product remain in cart"/*,retryAnalyzer = RetryListener.class*/)
    public void redirectCardEndUserCancelProductRemain(){
        setProductRemainCartAfterPaymentFailure(true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberRedirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().cancelCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage(CC_END_USER_CANCEL_ERROR);
      //  DriverActions.verifyEquals(isCartEmpty(),false,"Verify cart is not empty after payment failure");
    }

    @Test(priority = 15, description ="Check whether the user gets the appropriate error message displays on cart page after using an expired card disabling product remain in cart," +
            " verify product not remain in cart"/*,retryAnalyzer = RetryListener.class*/)
    public void expiredCardProductNotRemain(){
        setProductRemainCartAfterPaymentFailure(false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData2().get("Expired"), magentoPage.getTestData2().get("ExpDate"), magentoPage.getTestData2().get("CVV"))
                .clickPlaceOrderBtn();
        //magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Credit card payment not possible: card expired");
        //DriverActions.verifyEquals(isCartEmpty(),true,"Verify cart is empty after payment failure");
       // magentoPage.getCheckoutPage().isShippingPageDisplayed(true);
    }

    @Test(priority = 16, description ="Check whether the user gets the appropriate error message displays on cart page after using an restricted card disabling product remain in cart," +
            " verify product not remain in cart"/*,retryAnalyzer = RetryListener.class*/)
    public void restrictedCardProductNotRemain(){
        setProductRemainCartAfterPaymentFailure(false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData2().get("Restricted"), magentoPage.getTestData2().get("ExpDate"), magentoPage.getTestData2().get("CVV"))
                .clickPlaceOrderBtn();
        //magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Restricted card");
      //  DriverActions.verifyEquals(isCartEmpty(),true,"Verify cart is empty after payment failure");
      //  magentoPage.getCheckoutPage().isShippingPageDisplayed(true);

    }

    @Test(priority = 17, description ="Check whether the user gets the appropriate error message displays on cart page after using card with Insufficient funds or credit limit exceeded card data." +
            " card disabling product remain in cart, verify product not remain in cart"/*,retryAnalyzer = RetryListener.class*/)
    public void insufficientFundsCardProductNotRemain(){
        setProductRemainCartAfterPaymentFailure(false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData2().get("InsufficientFunds"), magentoPage.getTestData2().get("ExpDate"), magentoPage.getTestData2().get("CVV"))
                .clickPlaceOrderBtn();
       // magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Insufficient funds or credit limit exceeded");
       // DriverActions.verifyEquals(isCartEmpty(),true,"Verify cart is empty after payment failure");
        //magentoPage.getCheckoutPage().isShippingPageDisplayed(true);
    }

    @Test(priority = 18, description ="Check whether the user gets the appropriate error message displays on cart page by cancelling the payment on the OTP page.." +
            " card disabling product remain in cart, verify product not remain in cart"/*,retryAnalyzer = RetryListener.class*/)
    public void redirectCardEndUserCancelProductNotRemain(){
        setProductRemainCartAfterPaymentFailure(false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberRedirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().cancelCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage(CC_END_USER_CANCEL_ERROR);
        //DriverActions.verifyEquals(isCartEmpty(),true,"Verify cart is empty after payment failure");
    }


    @Test(priority = 19, description = "Check whether the test transaction is successful with guest user "/*,retryAnalyzer = RetryListener.class*/)
    public void guestOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                INLINE,true,
                ENF_3D,false
        ));
        navigateGuestCheckout(PRODUCT_GUEST);
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        String orderNumber = magentoPage.getSuccessPage().getOrderNumberForGuestOrder();
        String tid = NovalnetAPIs.getRecentTransactionTID(orderNumber);
        DriverActions.verifyEquals(TID_Helper.getTIDStatus(tid),TID_STATUS_CONFIRMED,"Verify tid status for guest user");
        magentoPage.getOrderPage().load();
        magentoPage.getOrderPage().searchOrderByOrderNumber(orderNumber);
        DriverActions.verifyEquals(magentoPage.getOrderPage().getOrderNovalnetComments().contains(tid),true,"Verify TID value returned from server");
        magentoPage.getOrderPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        magentoPage.getOrderPage().verifyInvoiceCreated(true);
    }


//    @Test(priority = 20, description = "Verify the order and invoice mail received with novalnet comments")
    public void mailVerifyOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                INLINE,true,
                ENF_3D,false
        ));
        createCustomer(CREDITCARD,"gopinath_m@novalnetsolutions.com");
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().load().changeCountry("DE").changeCurrency("EUR");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getSuccessPage().getOrderNumber(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CREDITCARD),
                paymentComments = magentoPage.getSuccessPage().getPaymentComment();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, CREDITCARD);
        magentoPage.getShopUserLoginPage().logout();
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailOrderSubjectText,orderNumber);
        DriverActions.verifyEquals(mailContent.contains(paymentName),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(paymentComments),true,"Verify order mail has novalnet payment comments");
        String invoiceMailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoWithRetry(ThunderBirdEmailHelper.connectToThunderBird(),mailInvoiceSubjectText,orderNumber);
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentName),true,"Verify invoice mail has payment name");
        DriverActions.verifyEquals(invoiceMailContent.contains(paymentComments),true,"Verify invoice mail has payment comments");
    }

    @Test(priority = 20, description = "Verify download product displayed inside my download products and download link displayed when the transaction is confirmed"/*,retryAnalyzer = RetryListener.class*/)
    public void downloadProductConfirmOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                INLINE,true,
                ENF_3D,false
        ));
        createCustomer(CREDITCARD);
        magentoPage.getShopUserLoginPage().load();
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",25.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(CREDITCARD) // Added username field cause name on card field is empty for download product which is an issue
                .fillCreditCardForm("Nobert Maier",magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, CREDITCARD);
        magentoPage.getMyAccountPage().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),true,"Verify download link displayed in my download products menu");
    }

    @Test(priority = 21, description = "Verify download product is displayed and download link is not displayed inside my download products when the transaction is on hold, capture transaction, verify download product displayed and download link displayed"/*,retryAnalyzer = RetryListener.class*/)
    public void downloadProductOnHoldToConfirmOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                INLINE,true,
                ENF_3D,false
        ));
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",25.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm("Nobert Maier",magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, CREDITCARD);
        magentoPage.getMyAccountPage().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
        captureOrder(orderNumber,tid);
        magentoPage.getMyAccountPage().load().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),true,"Verify download link displayed in my download products menu");
    }

    @Test(priority = 22, description = "Verify download product is displayed and download link is not displayed inside my download products when the transaction is on hold, cancel transaction, verify download product is not displayed and download link is not displayed"/*,retryAnalyzer = RetryListener.class*/)
    public void downloadProductOnHoldToCancelOrder(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,"",
                INLINE,true,
                ENF_3D,false
        ));
        magentoPage.getShopUserLoginPage().addProductToCart(MagentoAPI_Helper.createandReturnDownloadbleProductWithSku("test",25.00,1));
        magentoPage.getCheckoutPage().load()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm("Nobert Maier",magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString();
        TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, CREDITCARD);
        magentoPage.getMyAccountPage().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
        cancelOrder(orderNumber,tid);
        magentoPage.getMyAccountPage().load().openMyDownloadProducts();
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOrderDisplayed(orderNumber),true,"Verify download order displayed in my download products menu");
        DriverActions.verifyEquals(magentoPage.getMyAccountPage().isDownloadOptionDisplayed(orderNumber),false,"Verify download link displayed in my download products menu");
    }

    @Test(priority = 23,description = "Check whether the Credit Card payment order placed & failed by communication break"/*,retryAnalyzer = RetryListener.class*/)
    public void communicationBreakFailureOrder(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                MIN_AUTH_AMOUNT,"",
                INLINE,true,
                ENF_3D,false
        ));
        createCustomer(CREDITCARD);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberRedirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().waitForCCAuthenticationPage();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        String tid = magentoPage.getNovalnetAdminPortal().getTID(MagentoAPIs.getCustomerEmail());
        String orderNumber = TID_Helper.getOrderNumber(tid);
        magentoPage.getCallback().communicationBreakFailure(tid,"0");
        statusCommentsVerificationAfterCommunicationBreak(tid,orderNumber,CANCELLATION_ORDER_STATUS,false);
    }

  //   @Test(priority = 23, dataProvider = "creditCardCurrencies", description = "Check whether the test transaction is successful with multiple currencies")
    public void multiCurrencyOrder(String currency){
        magentoPage.getShopUserLoginPage().logout();
        createCustomer(CREDITCARD);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().changeCurrency(currency); // currency will be provided by dataProvider
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString();
        DriverActions.verifyEquals(TID_Helper.getTIDCurrency(tid),currency,"Verify tid currency ");
    }



    //For multi currency order
    @DataProvider
    public Object[][] creditCardCurrencies(){
        return new Object[][]{{"AED"},{"AUD"}, {"CHF"}, {"DKK"}, {"GBP"}, {"HKD"}, {"HUF"},
                {"INR"}, {"JPY"}, {"KRW"},{"MGA"}, {"PLN"}, {"SEK"}, {"RUB"},{"USD"}};
    }
  //  @Test(priority = 25,description = "Check whether the payment logo displayed as per admin portal configurations ",retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogo(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(false);
        createCustomer(CREDITCARD);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("Credit/Debit Cards",false);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Credit/Debit Cards",true);
    }


    //@Test(priority = 24, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(CREDITCARD,false);
        createCustomer(CREDITCARD);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(CREDITCARD,false);
    }



}
