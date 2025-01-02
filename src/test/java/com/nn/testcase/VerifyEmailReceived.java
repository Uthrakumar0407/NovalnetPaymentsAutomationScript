package com.nn.testcase;

import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.*;
import com.nn.constants.Constants;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ThunderBirdEmailHelper;
import io.qameta.allure.Step;
import org.testng.annotations.*;

import javax.mail.Store;
import java.util.HashMap;

import static com.nn.Magento.Constants.mailInvoiceSubjectText;
import static com.nn.Magento.Constants.mailOrderSubjectText;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.constants.Constants.CAPTURE;
import static com.nn.utilities.DriverActions.verifyEquals;

public class VerifyEmailReceived extends BaseTest {

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
            .myAccountPage(new MyAccountPage())
            .subscriptionPage(new SubscriptionPage())
            .callback(new CreditCardCallbackEvents())
            .callback_multibanco(new MultibancoCallbackEvents())
            .callback_cashPayment(new CashPaymentCallbackEvents())
            .callback_invoice(new InvoiceCallbackEvents())
            .callback_prepayment(new PrepaymentCallbackEvents())
            .callback_paypal(new PayPalCallbackEvents())
            .callback_postFinance(new PostFinanceCardPaymentCallbackEvents())
            .callback_trustly(new TrustlyCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .testData2(ExcelHelpers.declineCreditCards())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("email") String userName, @Optional("wordpress") String password) {
        ExtentTestManager.saveToReport("Setup","Setting up for cc");
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
        //verifyGlobalConfiguration();
    }


    @AfterClass(alwaysRun = true)
    public void adminLogout() {
        wooCommerce.getDashBoardPage().adminLogout();
        Store store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllEmails(store,mailOrderSubject);
        store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllEmails(store,"order");
        store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllEmails(store,"Payment Confirmation");
        store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllEmails(store,"Booking Confirmation");
        store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllEmails(store,"order");

  // Junk folder
        store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllJunkEmails(store,mailOrderSubject);
        store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllJunkEmails(store,"order");
        store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllJunkEmails(store,"Payment Confirmation");
        store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllJunkEmails(store,"Booking Confirmation");
    }

        @Test(priority=1, description="Verify the Bancontact Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderCrediCardPayment() {
        setPaymentConfiguration(CREDITCARD,true,CAPTURE,"",true,false,true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().verifyCCTestModeDisplayed(true);
        wooCommerce.getCheckoutPage().verifySaveCardCheckboxChecked(false);
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().verifyInlineFormDisplayed(true);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 2, description = "Verify the Bancontact Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderBancontactPayment(){
        setBancontactPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Belgium");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isBancontactDisplayed();
        wooCommerce.getCheckoutPage().verifyBancontactTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithBanContact();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, BANCONTACT);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 3, description = "Verify the cash Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderCashPayment(){
        setCashPaymentConfiguration(true,false,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isOnCashPaymentDisplayed();
        wooCommerce.getCheckoutPage().verifyOnCashPaymentTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, CASHPAYMENT);
        cashPaymentCredit(wooCommerce.getTxnInfo().get("TotalAmount").toString(),COMPLETION_ORDER_STATUS);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 4, description = "Verify the SEPA Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderSEPAPayment(){
        setSepaPaymentConfiguration(true,false,false,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().verifySepaTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 5, description = "Verify the GuaranteeSEPA  Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderGuaranteeSEPAPayment(){
        setSepaGuaranteePaymentConfiguration_Guarantee(true,false,false,true,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().EnterBillingCompany("");
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().verifySepaGuaranteeTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 6, description = "Verify the invoice Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderInvoicePayment(){
        setInvoicePaymentConfiguration(true,false,CAPTURE,"","");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, INVOICE);
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        wooCommerce.getCallback_invoice().invoiceCredit(tid,orderAmount);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 7, description = "Verify the GuaranteeInvoice Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderGuaranteeInvoicePayment(){
        setInvoicePaymentConfiguration_Guarantee(true,false,true,CAPTURE,"","1800");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().verifyInvoiceGuaranteeTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 8, description = "Verify the EPS Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderEPSPayment(){
        setEPSPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Austria");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isEPSDisplayed();
        wooCommerce.getCheckoutPage().verifyEPSTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithEPS();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, EPS);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    //@Test(priority = 9, description = "Verify the Giropay Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderGiroPayPayment(){
        setGiroPayPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isGiropayDisplayed();
        wooCommerce.getCheckoutPage().verifyGiropayTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithGiropay();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, GIROPAY);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 10, description = "Verify the iDeal Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderiDealPayment(){
        setiDealPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isIDealDisplayed();
        wooCommerce.getCheckoutPage().verifyIDealTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithIDeal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, IDEAL);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 11, description = "Verify the InstalmentInvoice Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderInstalmentInvoicePayment(){
        setInstalmentInvoicePaymentConfiguration(true,false,true,CAPTURE,"","4000");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isInstalmentInvoiceDisplayed();
        wooCommerce.getCheckoutPage().fillInstalmentInvoiceDOB(DOB);
        wooCommerce.getCheckoutPage().verifyInstalmentInvoiceTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().selectInstalmentCyclesInvoice("3");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedInvoice("3");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, INSTALMENT_INVOICE);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 12, description = "Verify the InstalmentSEPA Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderInstalmentSEPAPayment(){
        setInstalmentSEPAPaymentConfiguration(true,false,false,true,CAPTURE,"","","4000");
        navigateCheckout(PRODUCT_3);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isInstalmentSepaDisplayed();
        wooCommerce.getCheckoutPage().verifyInstalmentSepaTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().fillInstalmentSepaIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillInstalmentSepaDOB(DOB);
        wooCommerce.getCheckoutPage().selectInstalmentCyclesForSEPA("3");
        wooCommerce.getCheckoutPage().verifySelectedInstalmentCyclesDisplayedSEPA("3");
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, INSTALMENT_DIRECT_DEBIT_SEPA);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 13, description = "Verify the Multibanco Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderMultibancoPayment(){
        setMultibancoPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isMultibancoDisplayed();
        wooCommerce.getCheckoutPage().verifyMultibancoTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, MULTIBANCO);
        String tid = wooCommerce.getTxnInfo().get("TID").toString(),
                orderAmount = wooCommerce.getTxnInfo().get("TotalAmount").toString();
        wooCommerce.getCallback_multibanco().multibancoCredit(tid,orderAmount);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    //@Test(priority = 14, description = "Verify the Online bank transfer payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOnlineBankTransferPayment(){
        setOnlineBankTransferPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isOnlineBankTransferDisplayed();
        wooCommerce.getCheckoutPage().verifyOnlineTransferTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithOnlineBankTransfer();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, ONLINE_BANK_TRANSFER);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 15, description = "Verify the paypal Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyPaypalPayment(){
        setPaypalPaymentConfiguration(true,CAPTURE,"",false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().verifyPayPalTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PAYPAL);
        statusUpdateEvent(COMPLETION_ORDER_STATUS);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    //@Test(priority = 16, description = "Verify the postfinance Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyPostFinancePayment(){
        setPostfinancePaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isPostFinanceDisplayed();
        wooCommerce.getCheckoutPage().verifyPostFinanceTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithPostFinance();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, POSTFINANCE_CARD);
        var tid = wooCommerce.getTxnInfo().get("TID");
        wooCommerce.getCallback_postFinance().transactionUpdateStatus((String) tid, TID_STATUS_CONFIRMED);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    } @Test(priority = 17, description = "Verify the Pre Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyPrePayment(){
        setPrePaymentConfiguration(true,false,"");
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().verifyPrepaymentTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        prePaymnetCredit(wooCommerce.getTxnInfo().get("TotalAmount").toString(),COMPLETION_ORDER_STATUS);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 18, description = "Verify the sofort Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifySofortPayment(){
        setSofortPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isOnlineTransferDisplayed();
        wooCommerce.getCheckoutPage().verifyOnlineTransferTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithOnlineTransfer();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, ONLINE_TRANSFER);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 19, description = "Verify the Trustly Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyTrustlyPayment(){
        setTrustlyPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("Germany");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isTrustlyDisplayed();
        wooCommerce.getCheckoutPage().verifyTrustlyTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithTrustly();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        var tid = wooCommerce.getTxnInfo().get("TID");
        var status = TID_Helper.getTIDStatus((String) tid);
        verifyEquals(status.equals(TID_STATUS_CONFIRMED) || status.equals(TID_STATUS_PENDING) , true, "Verify Trustly status");
        wooCommerce.getCallback_trustly().transactionUpdateStatus((String) tid, TID_STATUS_CONFIRMED);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 20, description = "Verify the WechatPay Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyWechatPayPayment(){
        setWeChatPayPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("china");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isWeChatPayDisplayed();
        wooCommerce.getCheckoutPage().verifyWeChatPayTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithWeChatPay();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, WECHATPAY);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 21, description = "Verify the Alipay Payment Order and Invoice mail received with novalnet comments", retryAnalyzer = RetryListener.class)
    public void mailVerifyOrderAlipayPayment(){
        setAlipayPaymentConfiguration(true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().setBillingAtCheckout("china");
        wooCommerce.getCheckoutPage().setBillingEmail("automation_test@novalnetsolutions.com");
        wooCommerce.getCheckoutPage().isAlipayDisplayed();
        wooCommerce.getCheckoutPage().verifyalipayTestModeDisplayed(false);
        wooCommerce.getCheckoutPage().placeOrderWithAlipay();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, ALIPAY);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForWoocommerce(ThunderBirdEmailHelper.connectToThunderBird(), mailOrderSubject,wooCommerce.getTxnInfo().get("OrderNumber").toString());
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("PaymentTitle").toString()),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(wooCommerce.getTxnInfo().get("NovalnetComments").toString().replaceAll("\\s{2,}|\r?\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Step("Set Payment Configuration active {0}, payment action {1}, minimum authorize amount {2}, test mode {3}, one click {4}, inline form {5}, enforce3D {6}, paymentType {7}")
    public void setPaymentConfiguration(String paymentType, boolean paymentActive,
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
                        enforce3D, null,wooCommerce.getSettingsPage().getPayment(paymentType)));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setAlipayPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setAlipayPaymentConfiguration(
                        paymentActive,
                        testMode));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setBancontactPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setbancontactPaymentConfiguration(
                        paymentActive,
                        testMode));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}, and due date {2}")
    public void setCashPaymentConfiguration(boolean paymentActive,boolean testMode,String dueDate){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(paymentActive,"","",testMode,null,null,null, dueDate,wooCommerce.getSettingsPage().getPayment(CASHPAYMENT)));
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
    @Step("Set Direct Debit SEPA Payment Configuration active {0}, test mode {1}, payment action {2},allow b2b{3}, paymentAction{4}, authorize minimum amount {5} and due date {6}")
    public void setSepaGuaranteePaymentConfiguration_Guarantee(boolean paymentActive,
                                                               boolean testMode,
                                                               boolean oneClick,
                                                               boolean allowB2B,
                                                               String paymentAction,
                                                               String authMinAmount,
                                                               String dueDate){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setSEPAGuaranteeConfiguration(paymentActive,testMode,oneClick,allowB2B,paymentAction,authMinAmount,dueDate));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setEPSPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setEPSPaymentConfiguration(
                        paymentActive,
                        testMode));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setGiroPayPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setGiropayPaymentConfiguration(
                        paymentActive,
                        testMode));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setiDealPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(
                        paymentActive,"","",testMode,null,null,null,null, wooCommerce.getSettingsPage().getPayment(IDEAL)));
    }
    public void setInstalmentInvoicePaymentConfiguration(boolean paymentActive,
                                                         boolean testMode,
                                                         boolean allowB2B,
                                                         String paymentAction,
                                                         String authMinAmount,String minOrderAmt){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInstalmentInvoiceWithMinOrderAmountGuaranteeConfiguration(paymentActive,testMode,allowB2B,paymentAction,authMinAmount,minOrderAmt));
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
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setMultibancoPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(
                        paymentActive,"","",testMode,null,null,null, null,wooCommerce.getSettingsPage().getPayment(MULTIBANCO)));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setOnlineBankTransferPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setOnlineBankTransferPaymentConfiguration(
                        paymentActive,
                        testMode));
    }
    @Step("Set Payment Configuration active {0}, payment action {1}, minimum authorize amount {2}, test mode {3}, one click {4}, inline form {5}, enforce3D {6}")
    public void setPaypalPaymentConfiguration(boolean paymentActive,
                                        String paymentAction,
                                        String minAuthAmount,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(
                        paymentActive,
                        paymentAction,
                        minAuthAmount,
                        testMode,
                        null,
                        null,
                        null, null,wooCommerce.getSettingsPage().getPayment(PAYPAL)));
    }
    @Step("Perform invoice credit for the amount {0}")
    public void cashPaymentCredit(String amount,String status) {
        wooCommerce.getCallback_cashPayment().cashpaymentCredit(wooCommerce.getTxnInfo().get("TID").toString(), amount);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, amount);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setPostfinancePaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPostFinancePaymentConfiguration(
                        paymentActive,
                        testMode));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}, and due date {2}")
    public void setPrePaymentConfiguration(boolean paymentActive,boolean testMode,String dueDate){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(paymentActive,"","",testMode,null,null,null, dueDate,wooCommerce.getSettingsPage().getPayment(PREPAYMENT)));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setSofortPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(
                        paymentActive,"","",testMode,null,null,null, null,wooCommerce.getSettingsPage().getPayment(ONLINE_TRANSFER)));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setTrustlyPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setTrustlyPaymentConfiguration(
                        paymentActive,
                        testMode));
    }
    @Step("Set Payment Configuration active {0}, test mode {1}")
    public void setWeChatPayPaymentConfiguration(boolean paymentActive,
                                        boolean testMode){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setWeChatPayPaymentConfiguration(
                        paymentActive,
                        testMode));
    }
    @Step("Verify the transaction update status and novalnet paypal payment comments appended successfully")
    public void statusUpdateEvent(String orderStaus) {
        wooCommerce.getCallback_paypal().transactionUpdateStatus(wooCommerce.getTxnInfo().get("TID").toString(),TID_STATUS_CONFIRMED);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(),orderStaus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(UPDATE_COMMENT_);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStaus);
    }
    @Step("Perform invoice credit for the amount {0}")
    public void prePaymnetCredit(String amount,String status) {
        wooCommerce.getCallback_prepayment().invoiceCredit(wooCommerce.getTxnInfo().get("TID").toString(), amount);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, amount);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), status);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();
    }
}

