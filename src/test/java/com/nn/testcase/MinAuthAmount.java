package com.nn.testcase;

import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import static com.nn.constants.Constants.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.sleep;

public class MinAuthAmount extends BaseTest {

    int BASE_AMOUNT=800;

    WooCommercePage wooCommerce = WooCommercePage.builder()
            .adminPage(new AdminPage())
            .dashBoardPage(new DashboardPage())
            .ordersPage(new OrdersPage())
            .settingsPage(new SettingsPage())
            .homePage(new HomePage())
            .productPage(new ProductPage())
            .cartPage(new CartPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();



    @BeforeMethod(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("admin") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }


    @AfterMethod(alwaysRun = true)
    public void adminLogout() {
        wooCommerce.getDashBoardPage().adminLogout();
    }


    @Test(priority=1, description="Check Credit Card authorize test order status by comparing minAuthAmount and order amount", retryAnalyzer = RetryListener.class)
    public void creditCard_Authorize() {
        setPaymentConfiguration(true,AUTHORIZE,MIN_TXN_AMOUNT_AUTH,false,false,false,false);
        navigateCheckoutWithDeleteTokens(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());

        double orderTXNAmount=Double.parseDouble(ORDER_AMOUNT);
        double minAuthTXNAmount=Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

        if(orderTXNAmount >= minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, CREDITCARD);
            statusCommentsVerification(ONHOLD_ORDER_STATUS);
        }else if(orderTXNAmount < minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
            statusCommentsVerification(COMPLETION_ORDER_STATUS);
        }

    }

    @Test(priority = 2, alwaysRun = true, description = "Check DIRECT debit (SEPA) authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void directDebitSEPA_Authorize(){
        setSepaPaymentConfiguration(true,false,false,AUTHORIZE,MIN_TXN_AMOUNT_AUTH,"");
        navigateCheckoutWithDeleteTokens(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        double orderTXNAmount=Double.parseDouble(ORDER_AMOUNT);
        double minAuthTXNAmount=Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

        if(orderTXNAmount >= minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
            statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, paymentName, paymentComments, tid);
        } else if(orderTXNAmount < minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
            statusCommentsVerification(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid);
        }

    }

    @Test(priority = 3, description = "Check Payment by Invoice authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void invoice_Authorize(){
        setInvoicePaymentConfiguration(true,false,AUTHORIZE,MIN_TXN_AMOUNT_AUTH,"");
        navigateCheckout(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
        double orderTXNAmount=Double.parseDouble(ORDER_AMOUNT);
        double minAuthTXNAmount=Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

        if(orderTXNAmount >= minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
            statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, paymentName, paymentComments, tid);
        }else if(orderTXNAmount < minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE);
            statusCommentsVerification(orderNumber, PROCESSING_ORDER_STATUS, paymentName, paymentComments, tid);
        }

    }

    @Test(priority = 4, alwaysRun = true, description = "Check Direct debit SEPA with payment guarantee authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void guarantee_SEPA_Authorize(){

        setSepaGuaranteePaymentConfiguration_Guarantee(true,false,false,false,AUTHORIZE,MIN_TXN_AMOUNT_AUTH,"",ORDER_AMOUNT);
        if(Integer.parseInt(ORDER_AMOUNT)<999){
            setSepaPaymentConfiguration(true,false,false,AUTHORIZE,MIN_TXN_AMOUNT_AUTH,"");
            navigateCheckout(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
            wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
            wooCommerce.getCheckoutPage().setBillingCompany();
            wooCommerce.getCheckoutPage().isSepaDisplayed();
            sleep(5);
            byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("SCREENSHOT - Direct debit SEPA with payment guarantee  is NOT displayed as Minimum Order Value Amount must be greater than or equal to 999 ", new ByteArrayInputStream(screenshotBytes));}
        else {
            navigateCheckout(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
            wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
            wooCommerce.getCheckoutPage().setBillingCompany();
            wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
            wooCommerce.getCheckoutPage().verifySepaGuaranteeDateOfBirthIsDisplayed(true);
            wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
            wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
            wooCommerce.getCheckoutPage().placeOrder();
            wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
            wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
            String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                    orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                    orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                    paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                    paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();

            double orderTXNAmount = Double.parseDouble(ORDER_AMOUNT);
            double minAuthTXNAmount = Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

            if (orderTXNAmount >= minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
                statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, paymentName, paymentComments, tid);
            } else if (orderTXNAmount < minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
                statusCommentsVerification(orderNumber, COMPLETION_ORDER_STATUS, paymentName, paymentComments, tid);
            }
        }//outer if min order amount check


    }

    @Test(priority = 5, description = "Check Invoice with payment guarantee authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void guarantee_INVOICE_Authorize(){
        setInvoicePaymentConfiguration_Guarantee(true,false,false,AUTHORIZE,MIN_TXN_AMOUNT_AUTH,ORDER_AMOUNT);
        if(Integer.parseInt(ORDER_AMOUNT)<999){
            setInvoicePaymentConfiguration(true,false,AUTHORIZE,MIN_TXN_AMOUNT_AUTH,"");
            navigateCheckout(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
            wooCommerce.getCheckoutPage().isInvoiceDisplayed();
            sleep(5);
            byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("SCREENSHOT - Invoice with payment guarantee  is NOT displayed as Minimum Order Value Amount must be greater than or equal to 999 ", new ByteArrayInputStream(screenshotBytes));}
        else {
            navigateCheckout(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
            wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
            wooCommerce.getCheckoutPage().setBillingCompany();
            wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
            wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeDateOfBirthIsDisplayed(true);
            wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
            wooCommerce.getCheckoutPage().placeOrder();
            wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
            wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
            String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                    orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                    orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                    paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                    paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
            double orderTXNAmount = Double.parseDouble(ORDER_AMOUNT);
            double minAuthTXNAmount = Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

            if (orderTXNAmount >= minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
                statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, paymentName, paymentComments, tid);
            } else if (orderTXNAmount < minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
                statusCommentsVerification(orderNumber, COMPLETION_ORDER_STATUS, paymentName, paymentComments, tid);
            }
        }//guarantee check

    }

    @Test(priority = 6, description = "Check Instalment by SEPA Direct debit authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void instalment_DebitSEPA_Authorize() {
        setInstalmentSEPAPaymentConfiguration(true,false,false,false,AUTHORIZE,MIN_TXN_AMOUNT_AUTH,"",ORDER_AMOUNT);
        if(Integer.parseInt(ORDER_AMOUNT)<1998){
            navigateCheckout(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
            sleep(5);
            byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("SCREENSHOT - Instalment by SEPA Direct debit is NOT displayed as Minimum Order Value Amount must be greater than or equal to 1998", new ByteArrayInputStream(screenshotBytes));}
        else {
            navigateCheckout(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
            wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
            wooCommerce.getCheckoutPage().setBillingCompany();
            wooCommerce.getCheckoutPage().isInstalmentSepaDisplayed();
            wooCommerce.getCheckoutPage().verifyInstalmentSepaDateOfBirthIsDisplayed(true);
            wooCommerce.getCheckoutPage().fillInstalmentSepaIBAN(wooCommerce.getTestData().get("IBANDE"));
            wooCommerce.getCheckoutPage().fillInstalmentSepaDOB(DOB);
            wooCommerce.getCheckoutPage().placeOrder();
            wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
            wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
            String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                    orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                    orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                    paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                    paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString();
            double orderTXNAmount = Double.parseDouble(ORDER_AMOUNT);
            double minAuthTXNAmount = Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

            if (orderTXNAmount >= minAuthTXNAmount) {
                TID_Helper.verifyTIDStatus(tid, TID_STATUS_ON_HOLD);
                statusCommentsVerificationWithInstalmentTable(orderNumber, ONHOLD_ORDER_STATUS, paymentName, paymentComments, tid, 2);
            }   else if (orderTXNAmount < minAuthTXNAmount) {
                TID_Helper.verifyTIDStatus(tid,TID_STATUS_CONFIRMED);
                statusCommentsVerificationWithInstalmentTable(orderNumber,COMPLETION_ORDER_STATUS,paymentName, paymentComments, tid,2);
        }
        }//instalment check
    }


    @Test(priority = 7, description = "Check Instalment by Invoice authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void instalment_Invoice_Authorize() {
        setInstalmentInvoicePaymentConfiguration(true,false,false,AUTHORIZE,MIN_TXN_AMOUNT_AUTH,ORDER_AMOUNT);
        if(Integer.parseInt(ORDER_AMOUNT)<1998){
            navigateCheckout(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
            sleep(5);
            byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("SCREENSHOT - Instalment by SEPA Direct debit is NOT displayed as Minimum Order Value Amount must be greater than or equal to 1998", new ByteArrayInputStream(screenshotBytes));}
        else {
            navigateCheckout(PRODUCT_2, calculateCartCount(ORDER_AMOUNT));
            wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
            wooCommerce.getCheckoutPage().setBillingCompany();
            wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
            wooCommerce.getCheckoutPage().verifyInstalmentInvoiceDateOfBirthIsDisplayed(true);
            wooCommerce.getCheckoutPage().fillInstalmentInvoiceDOB(DOB);
            wooCommerce.getCheckoutPage().placeOrder();
            wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
            wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
            String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                    orderNumber = wooCommerce.getTxnInfo().get("OrderNumber").toString(),
                    orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString(),
                    paymentName = wooCommerce.getTxnInfo().get("PaymentName").toString(),
                    paymentComments = wooCommerce.getTxnInfo().get("NovalnetComments").toString(),
                    cycleAmount = wooCommerce.getTxnInfo().get("CycleAmount").toString();

            double orderTXNAmount = Double.parseDouble(ORDER_AMOUNT);
            double minAuthTXNAmount = Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

            if (orderTXNAmount >= minAuthTXNAmount) {
                TID_Helper.verifyTIDStatus(tid, TID_STATUS_ON_HOLD);
                statusCommentsVerificationWithInstalmentTable(orderNumber, ONHOLD_ORDER_STATUS, paymentName, paymentComments, tid, 2);
            } else if (orderTXNAmount < minAuthTXNAmount) {
                TID_Helper.verifyTIDStatus(tid, TID_STATUS_CONFIRMED);
                statusCommentsVerificationWithInstalmentTable(orderNumber, COMPLETION_ORDER_STATUS, paymentName, paymentComments, tid, 2);
            }
        }//instalment check
    }


    @Step("Set Payment Configuration active {0}, payment action {1}, minimum authorize amount {2}, test mode {3}, one click {4}, inline form {5}, enforce3D {6}, paymentType {7}")
    public void setPaymentConfiguration(boolean paymentActive,
                                        String paymentAction,
                                        String minAuthAmount,
                                        boolean testMode,
                                        boolean oneClick,
                                        boolean inlineFrom,
                                        boolean enforce3D){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(
                        paymentActive,
                        paymentAction,
                        minAuthAmount,
                        testMode,
                        oneClick,
                        inlineFrom,
                        enforce3D, null,wooCommerce.getSettingsPage().getPayment(CREDITCARD)));
    }

    @Step("Set Direct Debit SEPA Payment Configuration active {0}, test mode {1}, payment action {2}, authorize minimum amount {3} and due date {4}")
    public void setSepaPaymentConfiguration(boolean paymentActive,
                                            boolean testMode,
                                            boolean oneClick,
                                            String paymentAction,
                                            String authMinAmount,
                                            String dueDate){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setDirectDebitSepaConfiguration(paymentActive,testMode,oneClick,paymentAction,authMinAmount,dueDate));
    }
    @Step("Set Direct Debit SEPA Payment Configuration active {0}, test mode {1}, payment action {2},allow b2b{3}, paymentAction{4}, authorize minimum amount {5} and due date {6} and minimum order amount {7}")
    public void setSepaGuaranteePaymentConfiguration_Guarantee(boolean paymentActive,
                                                               boolean testMode,
                                                               boolean oneClick,
                                                               boolean allowB2B,
                                                               String paymentAction,
                                                               String authMinAmount,
                                                               String dueDate,String minOrderAmount){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setSEPAGuaranteeConfigurationWithMinOrderAmount(paymentActive,testMode,oneClick,allowB2B,paymentAction,authMinAmount,dueDate,minOrderAmount));
    }




    @Step("Set Invoice Payment Configuration active {0}, test mode {1}, payment action {2}, authorize minimum amount {3} and due date {4}")
    public void setInvoicePaymentConfiguration(boolean paymentActive,
                                               boolean testMode,
                                               String paymentAction,
                                               String authMinAmount,
                                               String dueDate){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceConfiguration(paymentActive,testMode,paymentAction,authMinAmount,dueDate));
    }

    @Step("Set Invoice with payment guarantee Configuration active {0}, test mode {1}, allow B2B {2}, payment action {3} and authorize minimum amount {4} and minimum order amount {5}")
    public void setInvoicePaymentConfiguration_Guarantee(boolean paymentActive,
                                                         boolean testMode,
                                                         boolean allowB2B,
                                                         String paymentAction,
                                                         String authMinAmount,String minOrderAmount){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceGuaranteeWithMinOrderAmountConfiguration(paymentActive,testMode,allowB2B,paymentAction,authMinAmount,minOrderAmount));
    }

    @Step("Set Instalment Invoice payment configuration active {0}, test mode {1}, allow B2B {2}, payment action {3} and authorize minimum amount {4} and minimum order amount {5}")
    public void setInstalmentSEPAPaymentConfiguration(boolean paymentActive,
                                                      boolean testMode,
                                                      boolean oneClick,
                                                      boolean allowB2B,
                                                      String paymentAction,
                                                      String authMinAmount,
                                                      String dueDate,String minOrderAmount){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInstalmentSEPAWithMinOrderAmountConfiguration(paymentActive,testMode,oneClick,allowB2B,paymentAction,authMinAmount,dueDate,minOrderAmount));
    }

    @Step("Set Instalment Invoice payment configuration active {0}, test mode {1}, allow B2B {2}, payment action {3} and authorize minimum amount {4} and minimum order amount {5}")
    public void setInstalmentInvoicePaymentConfiguration(boolean paymentActive,
                                                         boolean testMode,
                                                         boolean allowB2B,
                                                         String paymentAction,
                                                         String authMinAmount,String minOrderAmt){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInstalmentInvoiceWithMinOrderAmountGuaranteeConfiguration(paymentActive,testMode,allowB2B,paymentAction,authMinAmount,minOrderAmt));
    }
    @Step("Navigate to checkout by adding product {0} to cart")
    public void navigateCheckoutWithDeleteTokens(String productName,int cartCount){
        wooCommerce.getCartPage().load();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        for(int i=1;i<=cartCount;i++)    { wooCommerce.getProductPage().addProductToCartByName(new String[]{productName}); sleep(0.3);}
        deletePaymentTokens();
        wooCommerce.getHomePage().openCheckoutPage();
    }

    @Step("Navigate to checkout by adding product {0} to cart")
    public void navigateCheckoutWithDeleteTokens(String productName){
        wooCommerce.getCartPage().load();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        wooCommerce.getProductPage().addProductToCartByName(new String[]{productName});
        deletePaymentTokens();
        wooCommerce.getHomePage().openCheckoutPage();
    }

    @Step("Navigate to checkout by adding product {0} to cart")
    public void navigateCheckout(String productName, int cartCount){
        wooCommerce.getCartPage().load();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        for(int i=1;i<=cartCount;i++){wooCommerce.getProductPage().addProductToCartByName(new String[]{productName}); sleep(0.3);};
        wooCommerce.getHomePage().openCheckoutPage();
    }

    @Step("Verify the transaction order status and novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderStaus) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        if(orderStaus.equals(COMPLETION_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
    }



    @Step("Get Product from order amount {0}")
    public String getProduct(String orderAmount) {
        if (orderAmount.equals("1800")) return PRODUCT_1;
        if (orderAmount.equals("800")) return PRODUCT_2;
        if (orderAmount.equals("4000")) return PRODUCT_3;
        return "PRODUCT_1";
    }


    public int calculateCartCount(String orderAmount) {
        int cartCount = Integer.parseInt(orderAmount) / BASE_AMOUNT;
        return cartCount;
    }
}
