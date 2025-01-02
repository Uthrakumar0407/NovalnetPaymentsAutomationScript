package com.nn.testcase.shopware;

import com.nn.apis.ShopwareAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.*;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.pages.shopware.base.Shopware;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ThunderBirdEmailHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.mail.Store;

import java.util.Map;

import static com.nn.Magento.Constants.CAPTURE;
import static com.nn.Magento.Constants.DOB;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.EPS;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.ENF_3D;
import static com.nn.utilities.DriverActions.verifyEquals;
import static com.nn.utilities.ShopwareUtils.*;

public class VerifyEmailReceived extends BaseTest {

    Shopware shopwareMail = Shopware.builder()
            .callback(new CreditCardCallbackEvents())
            .callback_multibanco(new MultibancoCallbackEvents())
            .callback_cashPayment(new CashPaymentCallbackEvents())
            .callback_invoice(new InvoiceCallbackEvents())
            .callback_prepayment(new PrepaymentCallbackEvents())
            .callback_paypal(new PayPalCallbackEvents())
            .callback_postFinance(new PostFinanceCardPaymentCallbackEvents())
            .callback_trustly(new TrustlyCallbackEvents())
            .build();

    @BeforeClass
    public void setup() {
        ExtentTestManager.saveToReport("setup", "Setting up prerequisite to place order");
        ShopwareAPIs.getInstance().createCustomer(CREDITCARD,"automation_test@novalnetsolutions.com");
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeCurrency("EUR");
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        toggleActivateAllPayments(true);
    }

    @AfterClass(alwaysRun = true)
    public void deleteAllEmails(){
        Store store= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllEmails(store,MAIL_SUBJECT);
        Store store1= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllEmails(store1,"Order confirmation");
        Store store2= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllEmails(store2,"order");
        Store store3= ThunderBirdEmailHelper.connectToThunderBird();
        ThunderBirdEmailHelper.deleteAllEmails(store3,"confirmation");
    }

    @BeforeMethod(alwaysRun = true)
    public void clearCartS(){
        ShopwareAPIs.getInstance().clearCart();
    }


    @Test(priority = 1, description = "Verify the EPS Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderEPS(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Austria");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(EPS)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithEPS();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, EPS);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 2, description = "Verify the Cash Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderCashPayment(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(CASHPAYMENT)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, CASHPAYMENT);
        shopwareMail.getCallback_cashPayment().cashpaymentCredit(tid,totalAmount);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\s{2,}|\\r?\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 3, description = "Verify the Credit Card Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderCreditCard(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(CREDITCARD, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                INLINE, true,
                ENF_3D, false
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(CREDITCARD)
                .ifTokenDisplayedclickNewCardCC()
                .fillCreditCardForm(shopware.getTestData().get("CardNumberDirect"), shopware.getTestData().get("ExpDate"), shopware.getTestData().get("CVC"))
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, CREDITCARD);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 4, description = "Verify the Direct Debit SEPA Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderDirectDebitSEPA(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                FORCE_NON_GUARANTEE,true
        ));
        setPaymentConfiguration(DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(DIRECT_DEBIT_SEPA)
                .ifTokenDisplayedclickNewCardSEPA()
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 5, description = "Verify the Invoice Payment Order and Invoice mail received with novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderInvoice(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                FORCE_NON_GUARANTEE,true
        ));
        setPaymentConfiguration(INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                DUE_DATE,""
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
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
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, INVOICE,14);
        shopwareMail.getCallback_invoice().invoiceCredit(tid,totalAmount);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\s{2,}|\\r?\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 6, description = "Verify the Guarantee Invoice Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderGuaranteeInvoice(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,false,
                MIN_ORDER_AMOUNT,"999"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_INVOICE)
                .fill_DOB(DOB, GUARANTEED_INVOICE)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\s{2,}|\\r?\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 7, description = "Verify the Guarantee SEPA Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderGuaranteeSEPA(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,false,
                MIN_ORDER_AMOUNT,"999"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                .ifTokenDisplayedclickNewCardSEPA()
                .fill_IBAN_SEPA(shopware.getTestData().get("IBANDE"))
                .fill_DOB(DOB, GUARANTEED_DIRECT_DEBIT_SEPA)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 8, description = "Verify the Instalment Invoice Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderInstalmentInvoice(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,CAPTURE,
                ALLOW_B2B,false,
                MIN_ORDER_AMOUNT,"1998"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_INVOICE)
                .selectInstalmentInvoiceCycle(3)
                .fill_DOB_InstalmentInvoice(DOB)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, INSTALMENT_INVOICE);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\s{2,}|\\r?\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 9, description = "Verify the Instalment Sepa Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderInstalmentSEPA() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE,
                ALLOW_B2B, false,
                MIN_ORDER_AMOUNT, "1998"
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                .fill_InstalmentSEPA_IBAN(shopware.getTestData().get("IBANDE"))
                .selectInstalmentSepaCycle(3)
                .fill_DOB_InstalmentSEPA(DOB)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, INSTALMENT_DIRECT_DEBIT_SEPA);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

  //  @Test(priority = 10, description = "Verify the Online Bank Transfer Payment Order and Invoice mail received with novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderOnlineBankTransfer(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(ONLINE_BANK_TRANSFER)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithOnlineBankTransfer();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, ONLINE_BANK_TRANSFER);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 11, description = "Verify the PayPal Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderPaypal() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(PAYPAL, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PAYPAL)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithPayPal();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, PAYPAL);
        shopwareMail.getCallback_paypal().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 12, description = "Verify the PrePayment Order and Invoice mail received with payment comments")
    public void mailVerifyOrderPrePayment(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PREPAYMENT)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, PREPAYMENT);
        shopwareMail.getCallback_prepayment().invoiceCredit(tid,totalAmount);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\s{2,}|\\r?\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 13, description = "Verify the Trustly Payment Order and Invoice mail received with novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderTrustly(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(TRUSTLY)
                .exitIframe();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithTrustly();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        verifyEquals(TID_Helper.getTIDPaymentType(tid),TRUSTLY,"Verify payment name");
        shopwareMail.getCallback_trustly().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 14, description = "Verify the PostFinance Card Payment Order and Invoice mail received with  novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderPostFinance(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Switzerland");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(POSTFINANCE_CARD)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithPostFinance();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, POSTFINANCE_CARD);
        shopwareMail.getCallback_postFinance().transactionUpdateStatus(tid,TID_STATUS_CONFIRMED);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 15, description = "Verify the WeChatPay Payment Order and Invoice mail received with novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderWeChatPay(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("China");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(WECHATPAY)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithWeChatPay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, WECHATPAY);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 16, description = "Verify the AliPay Payment Order and invoice mail received with novalnet comments")
    public void mailVerifyOrderAlipay(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("China");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(ALIPAY)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithAlipay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, ALIPAY);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 17, description = "Verify the BanContact Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderBancontact(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Belgium");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(BANCONTACT)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithBanContact();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, BANCONTACT);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 18, description = "Verify the iDeal Payment Order and Invoice mail received with novalnet comments")
    public void mailVerifyOrderiDeal(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Netherlands");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(IDEAL)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithIDeal();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, IDEAL);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 19, description = "Verify the MultiBanco Payment Order and Invoice mail received with novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderMultibanco(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Portugal");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(MULTIBANCO)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_PENDING, MULTIBANCO);
        shopwareMail.getCallback_multibanco().multibancoCredit(tid,totalAmount);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\s{2,}|\\r?\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

 //   @Test(priority = 20, description = "Verify the Sofort Payment Order and Invoice mail received with novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderSofort(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(ONLINE_TRANSFER)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithOnlineTransfer();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, ONLINE_TRANSFER);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

   // @Test(priority = 21, description = "Verify the GiroPay Payment Order and Invoice mail received with novalnet comments"/**/)
    public void mailVerifyOrderGiroPay(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeBillingCountry("Germany");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(GIROPAY)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithGiropay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, GIROPAY);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }

    @Test(priority = 22, description = "Verify the Przelewy24 Payment Order and Invoice mail received with  novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderPrzelewy24(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeCurrency("PLN");
        shopware.getMyAccountPage().load().changeBillingCountry("Poland");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PRZELEWY24)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithPrzelewy(false);
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, PRZELEWY24);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 23, description = "Verify the Direct Debit ACH Payment Order and Invoice mail received with  novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderDirectDebitACH(){
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_ACH, Map.of(
                TESTMODE, true,
                PAYMENT_ACTION, CAPTURE
        ));
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeCurrency("USD");
        shopware.getMyAccountPage().load().changeBillingCountry("United States of America");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(DIRECT_DEBIT_ACH)
                .fill_ach_account_no(shopware.getTestData().get("accountNumberACH"))
                .fill_ach_ABA(shopware.getTestData().get("routingNumberACH"))
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().clickSubmitOrderBtn().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_ACH);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 24, description = "Verify the Blik Payment Order and Invoice mail received with  novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderBlik(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeCurrency("PLN");
        shopware.getMyAccountPage().load().changeBillingCountry("Poland");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(BLIK)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithBlikPayment();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, BLIK);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 25, description = "Verify the Payconiq Payment Order and Invoice mail received with Novalnet comments")
    public void mailVerifyOrderPayconiq(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeCurrency("EUR");
        shopware.getMyAccountPage().load().changeBillingCountry("Belgium");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(PAYCONIQ)
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithPayconiq();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, PAYCONIQ);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
    @Test(priority = 26, description = "Verify the MBWay Payment Order and Invoice mail received with novalnet payment comments displayed in the mail after successful order")
    public void mailVerifyOrderMBWay(){
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getMyAccountPage().load().changeCurrency("EUR");
        shopware.getMyAccountPage().changeBillingCountry("Portugal");
        shopware.getCheckoutPage()
                .load()
                .enterIframe()
                .isPaymentDisplayed(MBWAY)
                .fill_MBWay_Data(shopware.getTestData().get("MobileNO"))
                .exitIframe();
        var totalAmount = shopware.getCheckoutPage().getGrandTotalAmount();
        shopware.getCheckoutPage().clickSubmitOrderBtn().placeOrderWithMBWay();
        shopware.getTxnInfo().putAll(shopware.getCheckoutPage().getSuccessPageTransactionDetails());
        var initialComment = shopware.getTxnInfo().get("Comments");
        var tid = shopware.getTxnInfo().get("TID");
        var orderNumber = shopware.getTxnInfo().get("OrderNumber");
        var paymentName = shopware.getTxnInfo().get("PaymentName");
        TID_Helper.verifyTIDInformation(tid, totalAmount, TID_STATUS_CONFIRMED, MBWAY);
        String mailContent = ThunderBirdEmailHelper.getTodaysEmailByOrderNoForShopware(ThunderBirdEmailHelper.connectToThunderBird(),MAIL_SUBJECT,orderNumber);
        DriverActions.verifyEquals(mailContent.contains("Novalnet Payment"),true,"Verify order mail has payment name");
        DriverActions.verifyEquals(mailContent.contains(initialComment.replaceAll("\\s{2,}|\\r?\n"," ")),true,"Verify order mail has novalnet payment comments");
    }
}
