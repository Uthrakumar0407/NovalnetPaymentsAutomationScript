package com.nn.testcase;

import com.aventstack.extentreports.Status;
import com.nn.apis.GetTransactionDetailApi;
import com.nn.apis.TID_Helper;
import com.nn.basetest.BaseTest;
import com.nn.callback.CallbackProperties;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.callback.PayPalCallbackEvents;
import com.nn.callback.PrepaymentCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.*;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.*;

public class SubscriptionPaymentTests extends BaseTest {

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
            .callback_cc(new CreditCardCallbackEvents())
            .callback_prepayment(new PrepaymentCallbackEvents())
            .callback_paypal(new PayPalCallbackEvents())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("subs") String userName, @Optional("wordpress") String password) {
        ExtentTestManager.saveToReport("Setup","Setup for subscription");
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
        verifyGlobalConfiguration();
    }

    @Test(priority=1, description="CREDITCARD - Check whether the shop based subscription order is placed successfully, followu up verification and renewal orders placed successfully", retryAnalyzer = RetryListener.class)
    public void creditCard_ShopBased() {
        setSubscriptionConfigurationShopBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setCreditCardPaymentConfiguration(true,CAPTURE,"",true,false,true,false));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,false);
        subscriptionRenewalOrderCron(SUBSCRIPTION_STATUS_ACTIVE, COMPLETION_ORDER_STATUS, CREDITCARD, TID_STATUS_CONFIRMED);
        subscriptionChangePaymentToSEPA_Customer();
        subscriptionUpdateAmountAdmin();
        subscriptionRenewalOrderCron(wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(),DIRECT_DEBIT_SEPA,TID_STATUS_CONFIRMED);
        //subscriptionRenewalOrderCustomer_CC();
    }

    @Test(priority = 2, description="CREDITCARD - Check whether the Novalnet based subscription capture order is placed successfully, followup events via shop admin and renewal order placed successfully", retryAnalyzer = RetryListener.class)
    public void creditCard_Capture_NovalnetBased() {
        setSubscriptionConfigurationNovalnetBased(NOVALNET_SUBSCRIPTION_TARIFF);
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setCreditCardPaymentConfiguration(true,CAPTURE,"",true,false,true,false));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), wooCommerce.getTxnInfo().get("SubscriptionOrderNextPayment").toString(), CREDITCARD);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,true);
        subscriptionChangePaymentToSEPA_Customer();
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), DIRECT_DEBIT_SEPA);
        subscriptionChangeNextCycleDateVerifyTID();
        subscriptionUpdateAmountVerifyTID();
        //subscriptionRenewalOrderCustomer_CC();
        subscriptionSuspendVerifyTID();
        subscriptionReactivateVerifyTID();
        subscriptionChangePaymentCC_DifferentCard_Admin();
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), CREDITCARD);
        subscriptionCancelVerifyTID();
    }

    @Test(priority = 3, description="CREDITCARD - Check whether the Novalnet based subscription authorize order is placed successfully and all the followup events executed via callback successfully", retryAnalyzer = RetryListener.class)
    public void creditCard_Authorize_NovalnetBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().disableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setCreditCardPaymentConfiguration(true,AUTHORIZE,"",true,false,true,false));
        navigateCheckout(PRODUCT_SUBS_2);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, CREDITCARD);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), CREDITCARD);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ONHOLD,ONHOLD_ORDER_STATUS,false);
        subscriptionCaptureViaAdmin(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,TID_STATUS_CONFIRMED);
        subscriptionRenewalCallback(CREDITCARD,SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS);
        subscriptionSuspendCallback(CREDITCARD);
        subscriptionReactivateCallback(CREDITCARD);
        subscriptionAmountUpdateCallback(CREDITCARD);
        subscriptionCycleDateUpdateCallback(CREDITCARD);
        subscriptionChangePaymentCallback(CREDITCARD, DIRECT_DEBIT_SEPA, "Direct Debit SEPA");
        subscriptionCancelCallback(CREDITCARD);
    }

 /*   @Test(priority = 4, description = "PREPAYMENT - Check whether the shop based subscription order is placed successfully",retryAnalyzer = RetryListener.class)
    public void prepayment_ShopBased() {
        setSubscriptionConfigurationShopBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage().setPaymentConfiguration(true,"","",false,null,null,null, "10",wooCommerce.getSettingsPage().getPayment(PREPAYMENT)));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,PROCESSING_ORDER_STATUS,false);
        subscriptionInvoiceCredit(wooCommerce.getTxnInfo().get("TID").toString(),wooCommerce.getTxnInfo().get("OrderNumber").toString());
        subscriptionRenewalOrderCron(SUBSCRIPTION_STATUS_ACTIVE, PROCESSING_ORDER_STATUS, PREPAYMENT, TID_STATUS_PENDING);
        subscriptionInvoiceCredit(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(),wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        subscriptionChangePaymentToSEPA_Customer();
        subscriptionUpdateAmountAdmin();
        subscriptionRenewalOrderCron(wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(),DIRECT_DEBIT_SEPA,TID_STATUS_CONFIRMED);
        subscriptionRenewalOrderCustomer_Prepayment();
    }

    @Test(priority=5, description="PREPAYMENT - Check whether the Novalnet based subscription order is placed successfully followup events executed via shop admin",retryAnalyzer = RetryListener.class)
    public void prepayment_NovalnetBased() {
        setSubscriptionConfigurationNovalnetBased(NOVALNET_SUBSCRIPTION_TARIFF);
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage().setPaymentConfiguration(true,"","",false,null,null,null, "10",wooCommerce.getSettingsPage().getPayment(PREPAYMENT)));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), PREPAYMENT);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,PROCESSING_ORDER_STATUS,true);
        subscriptionInvoiceCredit(wooCommerce.getTxnInfo().get("TID").toString(),wooCommerce.getTxnInfo().get("OrderNumber").toString());
        subscriptionChangePaymentToSEPA_Customer();
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), DIRECT_DEBIT_SEPA);
        subscriptionChangeNextCycleDateVerifyTID();
        subscriptionUpdateAmountVerifyTID();
        subscriptionRenewalOrderCustomer_Prepayment();
        subscriptionSuspendVerifyTID();
        subscriptionReactivateVerifyTID();
        subscriptionChangePaymentToSEPA_Admin();
        subscriptionCancelVerifyTID();
    }

    @Test(priority=6, description="PREPAYMENT - Check whether the Novalnet based subscription order is placed successfully and follow up callback events executed successfully",retryAnalyzer = RetryListener.class)
    public void prepayment_Callback_NovalnetBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().disableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage().setPaymentConfiguration(true,"","",false,null,null,null, "10",wooCommerce.getSettingsPage().getPayment(PREPAYMENT)));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), PREPAYMENT);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionRenewalCallback(PREPAYMENT,SUBSCRIPTION_STATUS_ACTIVE,PROCESSING_ORDER_STATUS);
        subscriptionSuspendCallback(PREPAYMENT);
        subscriptionReactivateCallback(PREPAYMENT);
        subscriptionAmountUpdateCallback(PREPAYMENT);
        subscriptionCycleDateUpdateCallback(PREPAYMENT);
        subscriptionChangePaymentCallback(PREPAYMENT, DIRECT_DEBIT_SEPA, "Direct Debit SEPA");
        subscriptionCancelCallback(PREPAYMENT);
    }


    @Test(priority=7, description="DIRECT_DEBIT_SEPA - Check whether the shop based subscription order is placed successfully, followu up verification and renewal orders placed successfully",retryAnalyzer = RetryListener.class)
    public void sepa_ShopBased() {
        setSubscriptionConfigurationShopBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setDirectDebitSepaConfiguration(true,true,false,CAPTURE,"",""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,false);
        subscriptionRenewalOrderCron(SUBSCRIPTION_STATUS_ACTIVE, COMPLETION_ORDER_STATUS, DIRECT_DEBIT_SEPA, TID_STATUS_CONFIRMED);
        subscriptionChangePaymentToCC_Customer();
        subscriptionUpdateAmountAdmin();
        subscriptionRenewalOrderCron(wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(),CREDITCARD,TID_STATUS_CONFIRMED);
    }

    @Test(priority = 8, description="DIRECT_DEBIT_SEPA - Check whether the Novalnet based subscription capture order is placed successfully, followup events via shop admin and renewal order placed successfully",retryAnalyzer = RetryListener.class)
    public void sepa_Capture_NovalnetBased() {
        setSubscriptionConfigurationNovalnetBased(NOVALNET_SUBSCRIPTION_TARIFF);
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setDirectDebitSepaConfiguration(true,true,false,CAPTURE,"",""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), wooCommerce.getTxnInfo().get("SubscriptionOrderNextPayment").toString(), DIRECT_DEBIT_SEPA);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,true);
        subscriptionChangePaymentToCC_Customer();
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), CREDITCARD);
        subscriptionChangeNextCycleDateVerifyTID();
        subscriptionUpdateAmountVerifyTID();
        subscriptionSuspendVerifyTID();
        subscriptionReactivateVerifyTID();
        subscriptionChangePaymentToSEPA_Admin();
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), DIRECT_DEBIT_SEPA);
        subscriptionCancelVerifyTID();
    }


    @Test(priority = 9, description="DIRECT_DEBIT_SEPA - Check whether the Novalnet based subscription authorize order is placed successfully and all the followup events executed via callback successfully",retryAnalyzer = RetryListener.class)
    public void sepa_Authorize_NovalnetBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().disableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setDirectDebitSepaConfiguration(true,true,false,AUTHORIZE,"",""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isSepaDisplayed();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), DIRECT_DEBIT_SEPA);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ONHOLD,ONHOLD_ORDER_STATUS,false);
        subscriptionCaptureViaAdmin(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,TID_STATUS_CONFIRMED);
        subscriptionRenewalCallback(DIRECT_DEBIT_SEPA,SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS);
        subscriptionSuspendCallback(DIRECT_DEBIT_SEPA);
        subscriptionReactivateCallback(DIRECT_DEBIT_SEPA);
        subscriptionAmountUpdateCallback(DIRECT_DEBIT_SEPA);
        subscriptionCycleDateUpdateCallback(DIRECT_DEBIT_SEPA);
        subscriptionChangePaymentCallback(DIRECT_DEBIT_SEPA, CREDITCARD, "Credit/Debit Cards");
        subscriptionCancelCallback(DIRECT_DEBIT_SEPA);
    }

    @Test(priority=10, description="GUARANTEED_DIRECT_DEBIT_SEPA - Check whether the shop based subscription order is placed successfully, followu up verification and renewal orders placed successfully",retryAnalyzer = RetryListener.class)
    public void sepaGuarantee_ShopBased() {
        setSubscriptionConfigurationShopBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setSEPAGuaranteeConfiguration(true,true,false,false,CAPTURE,"",""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,false);
        subscriptionRenewalOrderCron(SUBSCRIPTION_STATUS_ACTIVE, COMPLETION_ORDER_STATUS, GUARANTEED_DIRECT_DEBIT_SEPA, TID_STATUS_CONFIRMED);
        subscriptionChangePaymentToCC_Customer();
        subscriptionUpdateAmountAdmin();
        subscriptionRenewalOrderCron(wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(),CREDITCARD,TID_STATUS_CONFIRMED);
    }
    @Test(priority = 11, description="GUARANTEED_DIRECT_DEBIT_SEPA - Check whether the Novalnet based subscription capture order is placed successfully, followup events via shop admin and renewal order placed successfully",retryAnalyzer = RetryListener.class)
    public void sepaGuarantee_Capture_NovalnetBased() {
        setSubscriptionConfigurationNovalnetBased(NOVALNET_SUBSCRIPTION_TARIFF);
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setSEPAGuaranteeConfiguration(true,true,false,false,CAPTURE,"",""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), wooCommerce.getTxnInfo().get("SubscriptionOrderNextPayment").toString(), GUARANTEED_DIRECT_DEBIT_SEPA);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,true);
        subscriptionChangePaymentToCC_Customer();
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), CREDITCARD);
        subscriptionChangeNextCycleDateVerifyTID();
        subscriptionUpdateAmountVerifyTID();
        subscriptionSuspendVerifyTID();
        subscriptionReactivateVerifyTID();
        subscriptionChangePaymentAdmin("Prepayment",TID_STATUS_CONFIRMED,PREPAYMENT);
        subscriptionCancelVerifyTID();
    }

    @Test(priority = 12, description="GUARANTEED_DIRECT_DEBIT_SEPA - Check whether the Novalnet based subscription authorize order is placed successfully and all the followup events executed via callback successfully",retryAnalyzer = RetryListener.class)
    public void sepaGuarantee_Authorize_NovalnetBased() {
        setSubscriptionConfigurationNovalnetBased(NOVALNET_SUBSCRIPTION_TARIFF);
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().disableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setSEPAGuaranteeConfiguration(true,true,false,false,AUTHORIZE,"",""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isSepaGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillSepaGuaranteeIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().fillSepaGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), GUARANTEED_DIRECT_DEBIT_SEPA);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ONHOLD,ONHOLD_ORDER_STATUS,false);
        subscriptionCaptureViaAdmin(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,TID_STATUS_CONFIRMED);
        subscriptionRenewalCallback(GUARANTEED_DIRECT_DEBIT_SEPA,SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS);
        subscriptionSuspendCallback(GUARANTEED_DIRECT_DEBIT_SEPA);
        subscriptionReactivateCallback(GUARANTEED_DIRECT_DEBIT_SEPA);
        subscriptionAmountUpdateCallback(GUARANTEED_DIRECT_DEBIT_SEPA);
        subscriptionCycleDateUpdateCallback(GUARANTEED_DIRECT_DEBIT_SEPA);
        subscriptionChangePaymentCallback(GUARANTEED_DIRECT_DEBIT_SEPA, CREDITCARD, "Credit/Debit Cards");
        subscriptionCancelCallback(GUARANTEED_DIRECT_DEBIT_SEPA);
    }*/

    @Test(priority=13, description="INVOICE - Check whether the shop based subscription order is placed successfully, followu up verification and renewal orders placed successfully",retryAnalyzer = RetryListener.class)
    public void invoice_ShopBased() {
        setSubscriptionConfigurationShopBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceConfiguration(true,true,CAPTURE,"",""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, INVOICE);
        TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,PROCESSING_ORDER_STATUS,false);
        subscriptionRenewalOrderCron(SUBSCRIPTION_STATUS_ACTIVE, PROCESSING_ORDER_STATUS, INVOICE, TID_STATUS_PENDING);
        subscriptionChangePaymentToCC_Customer();
        subscriptionUpdateAmountAdmin();
        subscriptionRenewalOrderCron(wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(),CREDITCARD,TID_STATUS_CONFIRMED);
    }

    @Test(priority = 14, description="INVOICE - Check whether the Novalnet based subscription capture order is placed successfully, followup events via shop admin and renewal order placed successfully",retryAnalyzer = RetryListener.class)
    public void invoice_Capture_NovalnetBased() {
        setSubscriptionConfigurationNovalnetBased(NOVALNET_SUBSCRIPTION_TARIFF);
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceConfiguration(true,true,CAPTURE,"",""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, INVOICE);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), wooCommerce.getTxnInfo().get("SubscriptionOrderNextPayment").toString(), INVOICE);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,PROCESSING_ORDER_STATUS,true);
        subscriptionChangePaymentToCC_Customer();
        subscriptionChangeNextCycleDateVerifyTID();
        subscriptionUpdateAmountVerifyTID();
        subscriptionSuspendVerifyTID();
        subscriptionReactivateVerifyTID();
        subscriptionChangePaymentToSEPA_Admin();
        subscriptionCancelVerifyTID();
    }

    @Test(priority = 15, description="INVOICE - Check whether the Novalnet based subscription authorize order is placed successfully and all the followup events executed via callback successfully",retryAnalyzer = RetryListener.class)
    public void invoice_Authorize_NovalnetBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().disableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice with payment guarantee");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceConfiguration(true,true,AUTHORIZE,"",""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isInvoiceDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, INVOICE);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), INVOICE);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ONHOLD,ONHOLD_ORDER_STATUS,false);
        subscriptionCaptureViaAdmin(SUBSCRIPTION_STATUS_ACTIVE,PROCESSING_ORDER_STATUS,TID_STATUS_PENDING);
        subscriptionRenewalCallback(INVOICE,SUBSCRIPTION_STATUS_ACTIVE,PROCESSING_ORDER_STATUS);
        subscriptionSuspendCallback(INVOICE);
        subscriptionReactivateCallback(INVOICE);
        subscriptionAmountUpdateCallback(INVOICE);
        subscriptionCycleDateUpdateCallback(INVOICE);
        subscriptionChangePaymentCallback(INVOICE, CREDITCARD, "Credit/Debit Cards");
        subscriptionCancelCallback(INVOICE);
    }

  /*  @Test(priority=16, description="GUARANTEED_INVOICE - Check whether the shop based subscription order is placed successfully, followu up verification and renewal orders placed successfully",retryAnalyzer = RetryListener.class)
    public void invoiceGuarantee_ShopBased() {
        setSubscriptionConfigurationShopBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceGuaranteeConfiguration(true,true,false,CAPTURE,""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,false);
        subscriptionRenewalOrderCron(SUBSCRIPTION_STATUS_ACTIVE, COMPLETION_ORDER_STATUS, GUARANTEED_INVOICE, TID_STATUS_CONFIRMED);
        subscriptionChangePaymentToCC_Customer();
        subscriptionUpdateAmountAdmin();
        subscriptionRenewalOrderCron(wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(),CREDITCARD,TID_STATUS_CONFIRMED);
    }

    @Test(priority = 17, description="GUARANTEED_INVOICE - Check whether the Novalnet based subscription capture order is placed successfully, followup events via shop admin and renewal order placed successfully",retryAnalyzer = RetryListener.class)
    public void invoiceGuarantee_Capture_NovalnetBased() {
        setSubscriptionConfigurationNovalnetBased(NOVALNET_SUBSCRIPTION_TARIFF);
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceGuaranteeConfiguration(true,true,false,CAPTURE,""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), wooCommerce.getTxnInfo().get("SubscriptionOrderNextPayment").toString(), GUARANTEED_INVOICE);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,true);
        subscriptionChangePaymentToCC_Customer();
        subscriptionChangeNextCycleDateVerifyTID();
        subscriptionUpdateAmountVerifyTID();
        subscriptionSuspendVerifyTID();
        subscriptionReactivateVerifyTID();
        subscriptionChangePaymentToSEPA_Admin();
        subscriptionCancelVerifyTID();
    }

    @Test(priority = 18, description="GUARANTEED_INVOICE - Check whether the Novalnet based subscription authorize order is placed successfully and all the followup events executed via callback successfully",retryAnalyzer = RetryListener.class)
    public void invoiceGuarantee_Authorize_NovalnetBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().disableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getSettingsPage().deActivatePayment("Novalnet Invoice");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setInvoiceGuaranteeConfiguration(true,true,false,AUTHORIZE,""));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().setB2CBillingAtCheckout();
        wooCommerce.getCheckoutPage().isInvoiceGuaranteeDisplayed();
        wooCommerce.getCheckoutPage().fillInvoiceGuaranteeDOB(DOB);
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), GUARANTEED_INVOICE);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_ONHOLD,ONHOLD_ORDER_STATUS,false);
        subscriptionCaptureViaAdmin(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,TID_STATUS_CONFIRMED);
        subscriptionRenewalCallback(GUARANTEED_INVOICE,SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS);
        subscriptionSuspendCallback(GUARANTEED_INVOICE);
        subscriptionReactivateCallback(GUARANTEED_INVOICE);
        subscriptionAmountUpdateCallback(GUARANTEED_INVOICE);
        subscriptionCycleDateUpdateCallback(GUARANTEED_INVOICE);
        subscriptionChangePaymentCallback(GUARANTEED_INVOICE, CREDITCARD, "Credit/Debit Cards");
        subscriptionCancelCallback(GUARANTEED_INVOICE);
    }
*/
    @Test(priority=19, description="PAYPAL - Check whether the shop based subscription order is placed successfully, followu up verification and renewal orders placed successfully",retryAnalyzer = RetryListener.class)
    public void paypal_ShopBased() {
        verifyGlobalConfiguration();
        setSubscriptionConfigurationShopBased();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPayPalPaymentConfiguration(true,CAPTURE,"",true));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PAYPAL);
        TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_PENDING,PENDING_ORDER_STATUS,false,true);
        statusUpdateEvent(COMPLETION_ORDER_STATUS);
        subscriptionChangePaymentToSEPA_Customer();
        subscriptionUpdateAmountAdmin();
        subscriptionRenewalOrderCron(wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(),DIRECT_DEBIT_SEPA,TID_STATUS_CONFIRMED);
    }

    @Test(priority = 20, description="PAYPAL - Check whether the Novalnet based subscription capture order is placed successfully, followup events via shop admin and renewal order placed successfully",retryAnalyzer = RetryListener.class)
    public void paypal_Capture_NovalnetBased() {
        setSubscriptionConfigurationNovalnetBased(NOVALNET_SUBSCRIPTION_TARIFF);
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Direct Debit SEPA");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPayPalPaymentConfiguration(true,CAPTURE,"",true));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_PENDING, PAYPAL);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), PAYPAL);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionStatusCommentsVerification(SUBSCRIPTION_STATUS_PENDING,PENDING_ORDER_STATUS,true,true);
        statusUpdateEvent(COMPLETION_ORDER_STATUS);
        subscriptionChangePaymentToSEPA_Customer();
        subscriptionChangeNextCycleDateVerifyTID();
        subscriptionUpdateAmountVerifyTID();
        subscriptionSuspendVerifyTID();
        subscriptionReactivateVerifyTID();
        subscriptionChangePaymentCC_DifferentCard_Admin();
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), CREDITCARD);
        subscriptionCancelVerifyTID();
    }

    @Test(priority = 21, description="PAYPAL - Check whether the Novalnet based subscription authorize order is placed successfully and all the followup events executed via callback successfully",retryAnalyzer = RetryListener.class)
    public void paypal_Authorize_NovalnetBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().disableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getSettingsPage().activatePayment("Novalnet Credit/Debit Cards");
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPayPalPaymentConfiguration(true,AUTHORIZE,"",true));
        navigateCheckout(PRODUCT_SUBS_1);
        wooCommerce.getCheckoutPage().isPayPalDisplayed();
        wooCommerce.getCheckoutPage().placeOrderWithPayPal();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageTransactionDetails());
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString(), TID_STATUS_ON_HOLD, PAYPAL);
        if(TID_Helper.verifySubscriptionValuesExist(wooCommerce.getTxnInfo().get("TID").toString(), true))
            TID_Helper.verifySubscriptionValuesInTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), PAYPAL);
        else
            Assert.fail("Subscription values is not in the TID for Novalnet based subscription");
        subscriptionCaptureViaAdmin(SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS,TID_STATUS_PENDING);
        subscriptionRenewalCallback(PAYPAL,SUBSCRIPTION_STATUS_ACTIVE,COMPLETION_ORDER_STATUS);
        subscriptionSuspendCallback(PAYPAL);
        subscriptionReactivateCallback(PAYPAL);
        subscriptionAmountUpdateCallback(PAYPAL);
        subscriptionCycleDateUpdateCallback(PAYPAL);
        subscriptionChangePaymentCallback(PAYPAL, CREDITCARD, "Credit/Debit Cards");
        subscriptionCancelCallback(PAYPAL);
    }

    @Step("Setting up the Novalnet API configuration")
    public void verifyGlobalConfiguration() {
            wooCommerce.getDashBoardPage().loadSettingsPage();
            wooCommerce.getSettingsPage().openNovalnetGlobalConfig();
            wooCommerce.getSettingsPage().verifyGlobalConfig();

    }

    @Step("Enable Subscription and activate payments")
    public void enableSubscriptionActivatePayments(){
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().enableSubscription();
        wooCommerce.getSettingsPage().selectSubscriptionPayments(SUBSCRIPTION_SUPPORTED_PAYMENTS);
    }

    @Step("Set Shop based subscription configuration")
    public void setSubscriptionConfigurationShopBased() {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().enableSubscription();
        wooCommerce.getSettingsPage().selectSubscriptionPayments(SUBSCRIPTION_SUPPORTED_PAYMENTS);
        wooCommerce.getSettingsPage().enableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().enableSubscriptionShopBased();
        wooCommerce.getSettingsPage().saveGlobalConfig();
    }

    @Step("Set Novalnet based subscription configuration")
    public void setSubscriptionConfigurationNovalnetBased(String tariff) {
        wooCommerce.getSettingsPage().novalnetGlobalConfigPageLoad();
        wooCommerce.getSettingsPage().enableSubscription();
        wooCommerce.getSettingsPage().enableSubscriptionNovalnetBased();
        wooCommerce.getSettingsPage().setSubscriptionTariff(tariff);
        wooCommerce.getSettingsPage().selectSubscriptionPayments(SUBSCRIPTION_SUPPORTED_PAYMENTS);
        wooCommerce.getSettingsPage().enableSubscriptionCancelfrontend();
        wooCommerce.getSettingsPage().saveGlobalConfig();
    }


    @Step("Navigate to checkout by adding product {0} to cart")
    public void navigateCheckout(String productName){
        wooCommerce.getCartPage().load();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        wooCommerce.getProductPage().addProductToCartByName(new String[]{productName});
        wooCommerce.getHomePage().openCheckoutPage();
    }

    @Step("Subscription status comments verification")
    public void subscriptionStatusCommentsVerification(String subscriptionStatus,String orderStatus,boolean cancelBtnDisplayed) {
       wooCommerce.getSuccessPage().verifySubscriptionStatus(subscriptionStatus);
        wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), orderStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("NovalnetComments").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(subscriptionStatus);

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subscriptionStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(subscriptionStatus);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyNovalnetSubscriptionCancelBtnDisplayed(cancelBtnDisplayed);
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(orderStatus);

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subscriptionStatus);
        wooCommerce.getSubscriptionPage().verifyOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPaymentName(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getSubscriptionPage().verifySubscriptionParentOrderStatus(orderStatus);

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), orderStatus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        if(orderStatus.equals(COMPLETION_ORDER_STATUS) || orderStatus.equals(PROCESSING_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentName").toString());
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentName").toString());
    }
    @Step("Subscription status comments verification")
    public void subscriptionStatusCommentsVerification(String subscriptionStatus,String orderStatus,boolean cancelBtnDisplayed,boolean pending) {
        wooCommerce.getSuccessPage().verifySubscriptionStatus(subscriptionStatus);
        wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), orderStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("NovalnetComments").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(subscriptionStatus);

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subscriptionStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(subscriptionStatus);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyNovalnetSubscriptionCancelBtnDisplayed(cancelBtnDisplayed);
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(orderStatus);

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subscriptionStatus);
        wooCommerce.getSubscriptionPage().verifyOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPaymentName(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getSubscriptionPage().verifySubscriptionParentOrderStatus(orderStatus);

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), orderStatus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString());
        wooCommerce.getOrdersPage().verifyOrderNotesComments(wooCommerce.getTxnInfo().get("NovalnetComments").toString(),pending);
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        if(orderStatus.equals(COMPLETION_ORDER_STATUS) || orderStatus.equals(PROCESSING_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentName").toString());
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, wooCommerce.getTxnInfo().get("PaymentName").toString());
    }

    @Step("Place Subscription renewal order via cron")
    public void subscriptionRenewalOrderCron(String subsStatus, String orderStatus, String paymentName, String tidStatus) {
        wooCommerce.getSubscriptionPage().loadCronSchedulerPage();
        wooCommerce.getSubscriptionPage().searchCronScheduler(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().runCronOnPayment();

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String renewalOrder = wooCommerce.getSubscriptionPage().verifyRenewalOrderPresent();
        wooCommerce.getTxnInfo().put("RenewalOrderNumber", renewalOrder);
        wooCommerce.getSubscriptionPage().verifySubscriptionRenewalOrderStatus(orderStatus);

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), orderStatus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getTxnInfo().putAll(wooCommerce.getOrdersPage().getRenewalOrderDetails());
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString(), tidStatus, paymentName);
        wooCommerce.getOrdersPage().VerifyOrderNotesAndCustomerNotesSame();
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), orderStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("RenewalOrderNovalnetComments").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(subsStatus);

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subsStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(subsStatus);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(orderStatus);

    }

    @Step("Change Subscription payment to SEPA via customer change payment")
    public void subscriptionChangePaymentToSEPA_Customer() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionChangePayment();
        wooCommerce.getCheckoutPage().clickSEPA();
        wooCommerce.getCheckoutPage().fillIBAN(wooCommerce.getTestData().get("IBANDE"));
        wooCommerce.getCheckoutPage().changePayment();

        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription("Direct Debit SEPA");
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPaymentName("Direct Debit SEPA");
        String changePaymentTID = wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getTxnInfo().put("ChangePaymentTID", changePaymentTID);
        TID_Helper.verifyTIDInformation(changePaymentTID,"0", TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(changePaymentTID, true);
        if(GetTransactionDetailApi.containsValue(wooCommerce.getTxnInfo().get("TID").toString(),"subscription"))
            TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), DIRECT_DEBIT_SEPA);
    }

    @Step("Change Subscription payment to Credit/Debit Cards via customer change payment")
    public void subscriptionChangePaymentToCC_Customer() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionChangePayment();
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"), wooCommerce.getTestData().get("ExpDate"), wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().changePayment();

        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription("Credit/Debit Cards");
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPaymentName("Credit/Debit Cards");
        String changePaymentTID = wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getTxnInfo().put("ChangePaymentTID", changePaymentTID);
        TID_Helper.verifyTIDInformation(changePaymentTID,"0", TID_STATUS_CONFIRMED, CREDITCARD);
        TID_Helper.verifyPaymentTokenExist(changePaymentTID, true);
        if(GetTransactionDetailApi.containsValue(wooCommerce.getTxnInfo().get("TID").toString(),"subscription"))
            TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), CREDITCARD);
    }

    @Step("Update subscription amount via shop admin")
    public void subscriptionUpdateAmountAdmin() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        int updatedCycleAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString())/2;
        String updatedTotalCycleAmount = wooCommerce.getSubscriptionPage().updateSubscriptionAmount(updatedCycleAmount);
        wooCommerce.getTxnInfo().put("updatedSubscriptionCycleAmount", updatedTotalCycleAmount);
    }

    @Step("Change Subscription next cycle date and verify TID")
    public void subscriptionChangeNextCycleDateVerifyTID() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String nextCycleDate = wooCommerce.getSubscriptionPage().getNextPaymentDate();
        String updatedCycleDate = addDaysFromDate(nextCycleDate,5);
        wooCommerce.getSubscriptionPage().changeNextPaymentDate(updatedCycleDate);
        TID_Helper.verifySubscriptionNextPaymentDateInTID(wooCommerce.getTxnInfo().get("TID").toString(), updatedCycleDate,"yyyy-MM-dd");
        String orderTotal = wooCommerce.getSubscriptionPage().getOrderTotalWithCurrency();
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        String updateComment = wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_UPDATE_COMMENT_+orderTotal+" on "+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(updateComment);
    }

    @Step("Update subscription amount and verify TID")
    public void subscriptionUpdateAmountVerifyTID() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        int updatedCycleAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString())/2;
        String updatedTotalCycleAmount = wooCommerce.getSubscriptionPage().updateSubscriptionAmount(updatedCycleAmount);
        wooCommerce.getTxnInfo().put("updatedSubscriptionCycleAmount", updatedTotalCycleAmount);
        TID_Helper.verifySubscriptionCycleAmountInTID(wooCommerce.getTxnInfo().get("TID").toString(), updatedTotalCycleAmount);
        String orderTotal = wooCommerce.getSubscriptionPage().getOrderTotalWithCurrency();
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        String updateComment = wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_UPDATE_COMMENT_+orderTotal+" on "+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(updateComment);
    }

    @Step("Suspend subscription via shop backend and verify TID")
    public void subscriptionSuspendVerifyTID() {
        wooCommerce.getSubscriptionPage().load();
        //wooCommerce.getSubscriptionPage().selectOrderStatus(SUBSCRIPTION_STATUS_ONHOLD);
        String orderNumber = wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString();
        hoverOnElement(By.xpath("//*[contains(@id,'"+orderNumber+"')]//mark"));
        waitForElementVisible(By.xpath("(//span[@class='on-hold'])[1]"));
        clickElement(By.xpath("(//span[@class='on-hold'])[1]"));
        waitForElementVisible(By.xpath("//tr[contains(@id,'"+orderNumber+"')][contains(@class,'subscription status-on-hold')]"));
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_SUSPEND_COMMENT_);
        TID_Helper.verifyNextCycleDateExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_SUSPEND_COMMENT_);
    }

    @Step("Reactivate subscription via shop backend and verify TID")
    public void subscriptionReactivateVerifyTID() {
        wooCommerce.getSubscriptionPage().load();


        String orderNumber = wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString();
        hoverOnElement(By.xpath("//*[contains(@id,'"+orderNumber+"')]//mark"));
        waitForElementVisible(By.xpath("(//span[@class='active'])[1]"));
        clickElement(By.xpath("(//span[@class='active'])[1]"));
        waitForElementVisible(By.xpath("//tr[contains(@id,'"+orderNumber+"')][contains(@class,'subscription status-active')]"));
        TID_Helper.verifyNextCycleDateExist(wooCommerce.getTxnInfo().get("TID").toString(), true);
        String today = changePatternOfDate("MMMM d, yyyy",new Date());
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_REACTIVATE_COMMENT_+" "+wooCommerce.getTxnInfo().get("TID").toString()+" on "+today+". Next charging date : "+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_REACTIVATE_COMMENT_+" "+wooCommerce.getTxnInfo().get("TID").toString()+" on "+today+". Next charging date : "+nextCycleDateInTID);
    }

    @Step("Place Subscription renewal order using cron")
    public void subscriptionRenewalOrderCron(String updatedCycleAmount, String updatedPaymentName, String tidStatus) {
        wooCommerce.getSubscriptionPage().loadCronSchedulerPage();
        wooCommerce.getSubscriptionPage().searchCronScheduler(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().runCronOnPayment();

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String renewalOrder = wooCommerce.getSubscriptionPage().verifyRenewalOrderPresent();
        wooCommerce.getTxnInfo().put("RenewalOrderNumber", renewalOrder);
        wooCommerce.getSubscriptionPage().clickRenewalOrder(renewalOrder);
        wooCommerce.getTxnInfo().put("RenewalOrderTIDAfterUpdate",wooCommerce.getOrdersPage().getOrderNotesTID(INITIAL_LEVEL_COMMENT_));
        TID_Helper.verifyTIDInformation(wooCommerce.getTxnInfo().get("RenewalOrderTIDAfterUpdate").toString(), updatedCycleAmount, tidStatus, updatedPaymentName);
    }


    @Step("Place subscription renewal order CreditCard using shop frontend")
    public void subscriptionRenewalOrderCustomer_CC() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionRenewal();
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        Map<String,Object> renewalDetails = wooCommerce.getSuccessPage().getSuccessPageTransactionDetails();
        renewalDetails.putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(renewalDetails.get("TID").toString(), wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(), TID_STATUS_CONFIRMED, CREDITCARD);
        wooCommerce.getSuccessPage().verifySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyRenewalOrderExist(renewalDetails.get("OrderNumber").toString());
    }

    @Step("Place subscription renewal order via customer renewal")
    public void subscriptionRenewalOrderCustomer_Prepayment() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().clickSubscriptionRenewal();
        wooCommerce.getCheckoutPage().isPrepaymentDisplayed();
        wooCommerce.getCheckoutPage().placeOrder();
        wooCommerce.getSuccessPage().verifyNovalnetCommentsDisplayed();
        Map<String,Object> renewalDetails = wooCommerce.getSuccessPage().getSuccessPageTransactionDetails();
        renewalDetails.putAll(wooCommerce.getSuccessPage().getSuccessPageSubscriptionDetails());
        TID_Helper.verifyTIDInformation(renewalDetails.get("TID").toString(), wooCommerce.getTxnInfo().get("updatedSubscriptionCycleAmount").toString(), TID_STATUS_PENDING, PREPAYMENT);
        wooCommerce.getSuccessPage().verifySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSuccessPage().verifyPaymentNameDisplayed(wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyRenewalOrderExist(renewalDetails.get("OrderNumber").toString());
    }


    @Step("Change payment of subscription to CreditCard via shop backned")
    public void subscriptionChangePaymentCC_DifferentCard_Admin() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().changePayment("Credit/Debit Cards",wooCommerce.getTestData().get("CardNumberDirect"),wooCommerce.getTestData().get("ExpDate"),wooCommerce.getTestData().get("CVC"));
        String changePaymentTID = wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        Log.info("changePaymentTID: "+changePaymentTID);
        wooCommerce.getTxnInfo().put("ChangePaymentAdminTID", changePaymentTID);
        TID_Helper.verifyTIDInformation(changePaymentTID,"0", TID_STATUS_CONFIRMED, CREDITCARD);
        TID_Helper.verifyPaymentTokenExist(changePaymentTID, true);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifySubscriptionOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), "Credit/Debit Cards");
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription("Credit/Debit Cards");
    }

    @Step("Change payment of subscription via shop backned")
    public void subscriptionChangePaymentToSEPA_Admin() {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().changePayment("Direct Debit SEPA",wooCommerce.getTestData().get("IBANDE"));
        String changePaymentTID = wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getTxnInfo().put("ChangePaymentAdminTID", changePaymentTID);
        TID_Helper.verifyTIDInformation(changePaymentTID,"0", TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
        TID_Helper.verifyPaymentTokenExist(changePaymentTID, true);
        TID_Helper.verifySubscriptionPaymentInTID(wooCommerce.getTxnInfo().get("TID").toString(), DIRECT_DEBIT_SEPA);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifySubscriptionOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), "Direct Debit SEPA");
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription("Direct Debit SEPA");
    }

    @Step("Change payment of subscription via shop backned")
    public void subscriptionChangePaymentAdmin(String paymentNameInShop, String tidStatus, String paymentName) {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().changePayment(paymentNameInShop);
        String changePaymentTID = wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getTxnInfo().put("ChangePaymentAdminTID", changePaymentTID);
        TID_Helper.verifyTIDInformation(changePaymentTID,"0", tidStatus, paymentName);
        //TID_Helper.verifySubscriptionPaymentInTID(changePaymentTID, paymentName);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifySubscriptionOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), paymentNameInShop);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(paymentNameInShop);
    }

    @Step("Cancel subscription via shop backend")
    public void subscriptionCancelVerifyTID() {
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().cancelSubscription();
        TID_Helper.verifySubscriptionCancelReasonExist(wooCommerce.getTxnInfo().get("TID").toString(), true);
        TID_Helper.verifyNextCycleDateExist(wooCommerce.getTxnInfo().get("TID").toString(), false);
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_CANCEL_COMMENT_);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPageStatus(SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_CANCEL_COMMENT_);
    }

    @Step("Capture subscription order via admin")
    public void subscriptionCaptureViaAdmin(String subscriptionStatus, String orderStatus, String tidStatus) {

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getOrdersPage().selectOrderStatus(orderStatus);
        if(TID_Helper.getTIDPaymentType(wooCommerce.getTxnInfo().get("TID").toString()).equals((PAYPAL)))
            TID_Helper.verifyPayPalTIDStatus(wooCommerce.getTxnInfo().get("TID").toString(), wooCommerce.getTxnInfo().get("TotalAmount").toString());
         else
            TID_Helper.verifyTIDStatus(wooCommerce.getTxnInfo().get("TID").toString(), tidStatus);

        wooCommerce.getOrdersPage().verifyOrderNotesComments(CAPTURE_COMMENT_);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());
        wooCommerce.getOrdersPage().verifySubscriptionOrderListStatus(subscriptionStatus);

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subscriptionStatus);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPageStatus(subscriptionStatus);
        String nextCycleDate = wooCommerce.getSubscriptionPage().getNextPaymentDate();
        wooCommerce.getTxnInfo().put("SubscriptionNextCycleDate", nextCycleDate);
        TID_Helper.verifySubscriptionNextPaymentDateInTID(wooCommerce.getTxnInfo().get("TID").toString(), nextCycleDate,"yyyy-MM-dd");

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("OrderNumber").toString(), orderStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("OrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CAPTURE_COMMENT_);
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(subscriptionStatus);

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subscriptionStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(subscriptionStatus);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(orderStatus);

    }

    @Step("Renewal order of subscription via callback")
    public void subscriptionRenewalCallback(String paymentName, String subscriptionStatus, String orderStatus) {
        wooCommerce.getCallback().subscriptionRenewal(wooCommerce.getTxnInfo().get("TID").toString(),paymentName);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String renewalOrder = wooCommerce.getSubscriptionPage().verifyRenewalOrderPresent();
        wooCommerce.getTxnInfo().put("RenewalOrderNumber", renewalOrder);
        wooCommerce.getSubscriptionPage().verifySubscriptionRenewalOrderStatus(orderStatus);

        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), orderStatus);
        wooCommerce.getOrdersPage().selectBackendOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getTxnInfo().putAll(wooCommerce.getOrdersPage().getRenewalOrderDetails());
        wooCommerce.getOrdersPage().VerifyOrderNotesAndCustomerNotesSame();
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(wooCommerce.getTxnInfo().get("RenewalOrderTID").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), subscriptionStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(subscriptionStatus);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(wooCommerce.getTxnInfo().get("PaymentName").toString());
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(orderStatus);

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString(), orderStatus);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("RenewalOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStatus);
        wooCommerce.getMyAccountPage().verifySubscriptionStatusInsideOrdersPage(subscriptionStatus);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(wooCommerce.getTxnInfo().get("RenewalOrderNovalnetComments").toString(), wooCommerce.getTxnInfo().get("PaymentName").toString());

    }

    @Step("Suspend Subscription via callback")
    public void subscriptionSuspendCallback(String paymentName) {
        wooCommerce.getCallback().subscriptionSuspend(wooCommerce.getTxnInfo().get("TID").toString(),paymentName);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_SUSPEND_COMMENT_);
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPageStatus(SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ONHOLD);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_SUSPEND_COMMENT_);
    }

    @Step("Reactivate Subscription via callback")
    public void subscriptionReactivateCallback(String paymentName) {
        wooCommerce.getCallback().subscriptionReactivate(wooCommerce.getTxnInfo().get("TID").toString(),paymentName);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String today = changePatternOfDate("MMMM dd, yyyy",new Date());
        String nextCycleDateInTID = TID_Helper.getNextCycleDate(wooCommerce.getTxnInfo().get("TID").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_REACTIVATE_COMMENT_+wooCommerce.getTxnInfo().get("TID").toString()+" on "+today+". Next charging date :"+nextCycleDateInTID);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_REACTIVATE_COMMENT_+wooCommerce.getTxnInfo().get("TID").toString()+" on "+today+". Next charging date :"+nextCycleDateInTID);
    }

    @Step("Update subscription amount via callback")
    public void subscriptionAmountUpdateCallback(String paymentName) {
        int updatedCycleAmount = Integer.parseInt(wooCommerce.getTxnInfo().get("SubscriptionCycleAmount").toString())/2;
        String formattedUpdatedCycleAmount = wooCommerce.getSubscriptionPage().formatAmount(updatedCycleAmount);
        wooCommerce.getTxnInfo().put("UpdatedCycleAmount", formattedUpdatedCycleAmount);
        wooCommerce.getCallback().subscriptionAmountUpdate(wooCommerce.getTxnInfo().get("TID").toString(), String.valueOf(updatedCycleAmount),paymentName);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_UPDATE_COMMENT_+"€"+formattedUpdatedCycleAmount);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_UPDATE_COMMENT_+"€"+formattedUpdatedCycleAmount);
    }

    @Step("Update subscription cycle date via callback")
    public void subscriptionCycleDateUpdateCallback(String paymentName) {
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        String nextCycleDate = wooCommerce.getSubscriptionPage().getNextPaymentDate();
        wooCommerce.getTxnInfo().put("SubscriptionNextCycleDate", nextCycleDate);
        String nextCycleDateUpdated = wooCommerce.getTxnInfo().get("SubscriptionNextCycleDate").toString();
        String updatedCycleDate = addDaysFromDate(nextCycleDateUpdated,5)+" 00:00:00";
        wooCommerce.getCallback().subscriptionCycleDateAmountUpdate(wooCommerce.getTxnInfo().get("TID").toString(),updatedCycleDate,
                wooCommerce.getTxnInfo().get("UpdatedCycleAmount").toString().replaceAll("[^0-9]", ""), paymentName);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(updatedCycleDate);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(updatedCycleDate);
    }

    @Step("Change subscription payment via callback")
    public void subscriptionChangePaymentCallback(String actualPayment, String changePayment, String paymentNameInShop) {
        wooCommerce.getCallback().subscriptionChangePayment(wooCommerce.getTxnInfo().get("TID").toString(), actualPayment, changePayment);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), paymentNameInShop);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPaymentName(paymentNameInShop);
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(CHANGE_PAYMENT_COMMENT_);
        //commented due to change in 12.5.6 - Transaction ID display has been removed in Customer Order Provided Note - Change Payment
      /*  String changePaymentComment = wooCommerce.getSubscriptionPage().getOrderNoteComment(INITIAL_LEVEL_COMMENT_);
        wooCommerce.getSubscriptionPage().verifyCustomerNotesComments(changePaymentComment);
      */
        //Added as part of 12.5.6
        String orderNotesTID=wooCommerce.getSubscriptionPage().getChangePaymentTID(INITIAL_LEVEL_COMMENT_);
        String callBackTID=CallbackProperties.getEventTID();
        verifyEquals(callBackTID,orderNotesTID, "Change Payment TID Validation");

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifySubscriptionOrderListingPaymentName(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), paymentNameInShop);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyFollowupComments(CHANGE_PAYMENT_COMMENT_);
        wooCommerce.getMyAccountPage().verifyPaymentNameInsideSubscription(paymentNameInShop);
    }

    @Step("Cancel subscription via callback")
    public void subscriptionCancelCallback(String paymentName) {
        wooCommerce.getCallback().subscriptionCancel(wooCommerce.getTxnInfo().get("TID").toString(),paymentName);
        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifyOrderHistoryPageStatus(SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getSubscriptionPage().verifyOrderNotesComments(SUBSCRIPTION_CANCEL_COMMENT_);
        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_CANCELLED);
        wooCommerce.getMyAccountPage().verifyFollowupComments(SUBSCRIPTION_CANCEL_COMMENT_);
    }

    @Step("Perform Invoice Credit for subscription order")
    public void subscriptionInvoiceCredit(String tid, String orderNumber) {
        wooCommerce.getCallback().invoiceCreditEvent(tid, wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber, COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(CREDIT_COMMENT_, wooCommerce.getTxnInfo().get("TotalAmount").toString());
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, wooCommerce.getTxnInfo().get("PaymentTitle").toString());

        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber, COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(COMPLETION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupCommentsExist();

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(COMPLETION_ORDER_STATUS);

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifySubscriptionParentOrderStatus(COMPLETION_ORDER_STATUS);
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

        wooCommerce.getMyAccountPage().loadSubscription();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().clickOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getMyAccountPage().verifyOrderHistorySubscriptionStatus(SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getMyAccountPage().verifyOrderStatusInsideSubscriptionPage(orderStaus);

        wooCommerce.getSubscriptionPage().load();
        wooCommerce.getSubscriptionPage().verifyOrderListingStatus(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString(), SUBSCRIPTION_STATUS_ACTIVE);
        wooCommerce.getSubscriptionPage().selectBackendOrder(wooCommerce.getTxnInfo().get("SubscriptionOrderNumber").toString());
        wooCommerce.getSubscriptionPage().verifySubscriptionParentOrderStatus(orderStaus);
    }

}
