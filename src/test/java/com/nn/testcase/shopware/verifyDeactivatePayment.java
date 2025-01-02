package com.nn.testcase.shopware;

import com.nn.apis.ShopwareAPIs;
import com.nn.listeners.RetryListener;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.reports.ExtentTestManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.ShopwareUtils.SW_PRODUCT_01;
import static com.nn.utilities.ShopwareUtils.SW_PRODUCT_02;

public class verifyDeactivatePayment extends BaseTest {
    @BeforeClass
    public void setupPaymentActivation_PreCheck() {
        ExtentTestManager.saveToReport("setupPaymentActivation_PreCheck", "setupPaymentActivation_PreCheck");
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        toggleActivateAllPayments(true);
    }

    @AfterClass
    public void setupPaymentActivation_PostCheck() {
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        toggleActivateAllPayments(true);
    }

    @BeforeMethod
    public void login() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
    }

    @Test(priority = 1, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutCreditCard() {
        paymentActivation(CREDITCARD, false);
        ShopwareAPIs.getInstance().createCustomer(CREDITCARD);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(CREDITCARD, false);
    }

    @Test(priority = 2, description = "Verify that direct debit sepa payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutDirectDebitSEPA() {
        paymentActivation(DIRECT_DEBIT_SEPA, false);
        ShopwareAPIs.getInstance().createCustomer(DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 3, description = "Verify that invoice payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutInvoice() {
        paymentActivation(INVOICE, false);
        ShopwareAPIs.getInstance().createCustomer(INVOICE);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(INVOICE, false);
    }

    @Test(priority = 4, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPrePayment() {
        paymentActivation(PREPAYMENT, false);
        ShopwareAPIs.getInstance().createCustomer(PREPAYMENT);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(PREPAYMENT, false);
    }

    //  @Test(priority = 5, description = "Verify that CashPayment payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutCashPayment() {
        paymentActivation(CASHPAYMENT, false);
        ShopwareAPIs.getInstance().createCustomer(CASHPAYMENT);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(CASHPAYMENT, false);
    }

    @Test(priority = 6, description = "Verify that guarantee invoice payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutGuaranteeInvoice() {
        paymentActivation(GUARANTEED_INVOICE, false);
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_INVOICE);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(GUARANTEED_INVOICE, false);
    }

    @Test(priority = 7, description = "Verify that guarantee sepa payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutGuaranteeSEPA() {
        paymentActivation(GUARANTEED_DIRECT_DEBIT_SEPA, false);
        ShopwareAPIs.getInstance().createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 8, description = "Verify that instalment invoice payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutInstalmentInvoice() {
        paymentActivation(INSTALMENT_INVOICE, false);
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_INVOICE);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(INSTALMENT_INVOICE, false);
    }

    @Test(priority = 9, description = "Verify that instalment sepa payment is hidden on the checkout page when payment id disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutInstalmetSEPA() {
        paymentActivation(INSTALMENT_DIRECT_DEBIT_SEPA, false);
        ShopwareAPIs.getInstance().createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA, false);
    }

    @Test(priority = 10, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal", retryAnalyzer = RetryListener.class)
    public void deactivatePaymentVerifyCheckoutGooglePay() {
        paymentActivation(GOOGLEPAY, false);
        ShopwareAPIs.getInstance().createCustomer(GOOGLEPAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(GOOGLEPAY, false);
    }

    @Test(priority = 11, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPaypal() {
        paymentActivation(PAYPAL, false);
        ShopwareAPIs.getInstance().createCustomer(PAYPAL);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(PAYPAL, false);
    }

    @Test(priority = 12, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutOnlineBankTransfer() {
        paymentActivation(ONLINE_BANK_TRANSFER, false);
        ShopwareAPIs.getInstance().createCustomer(ONLINE_BANK_TRANSFER);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(ONLINE_BANK_TRANSFER, false);
    }

    @Test(priority = 13, description = "Verify that Bancontact payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutBancontact() {
        paymentActivation(BANCONTACT, false);
        ShopwareAPIs.getInstance().createCustomer(BANCONTACT);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(BANCONTACT, false);
    }

    @Test(priority = 14, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutSofort() {
        paymentActivation(ONLINE_TRANSFER, false);
        ShopwareAPIs.getInstance().createCustomer(ONLINE_TRANSFER);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(ONLINE_TRANSFER, false);
    }

    @Test(priority = 15, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutIdeal() {
        paymentActivation(IDEAL, false);
        ShopwareAPIs.getInstance().createCustomer(IDEAL);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Netherlands");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(IDEAL, false);
    }

    @Test(priority = 16, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutEps() {
        paymentActivation(EPS, false);
        ShopwareAPIs.getInstance().createCustomer(EPS);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Austria");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(EPS, false);
    }

    @Test(priority = 17, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutGiroPay() {
        paymentActivation(GIROPAY, false);
        ShopwareAPIs.getInstance().createCustomer(GIROPAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(GIROPAY, false);
    }

    @Test(priority = 18, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutMultibanco() {
        paymentActivation(MULTIBANCO, false);
        ShopwareAPIs.getInstance().createCustomer(MULTIBANCO);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Portugal");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(MULTIBANCO, false);
    }

    @Test(priority = 19, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutTrustly() {
        paymentActivation(TRUSTLY, false);
        ShopwareAPIs.getInstance().createCustomer(TRUSTLY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(TRUSTLY, false);
    }

    @Test(priority = 20, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutWechatPay() {
        paymentActivation(WECHATPAY, false);
        ShopwareAPIs.getInstance().createCustomer(WECHATPAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("China");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(WECHATPAY, false);
    }

    @Test(priority = 21, description = "Verify that Alipay payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutAliPay() {
        paymentActivation(ALIPAY, false);
        ShopwareAPIs.getInstance().createCustomer(ALIPAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("China");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(ALIPAY, false);
    }

    @Test(priority = 22, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPostFinanceCard() {
        paymentActivation(POSTFINANCE_CARD, false);
        ShopwareAPIs.getInstance().createCustomer(POSTFINANCE_CARD);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Switzerland");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(POSTFINANCE_CARD, false);
    }

    @Test(priority = 23, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPrzelewy24() {
        paymentActivation(PRZELEWY24, false);
        ShopwareAPIs.getInstance().createCustomer(PRZELEWY24);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Poland");
        shopware.getMyAccountPage().changeCurrency("PLN");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(PRZELEWY24, false);
    }

    @Test(priority = 24, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutDirectDebitACH() {
        paymentActivation(DIRECT_DEBIT_ACH, false);
        ShopwareAPIs.getInstance().createCustomer(DIRECT_DEBIT_ACH);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("United States of America");
        shopware.getMyAccountPage().changeCurrency("USD");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(DIRECT_DEBIT_ACH, false);
    }

    @Test(priority = 25, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutBlik() {
        paymentActivation(BLIK, false);
        ShopwareAPIs.getInstance().createCustomer(BLIK);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Poland");
        shopware.getMyAccountPage().changeCurrency("PLN");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(BLIK, false);
    }

    @Test(priority = 26, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPayconiq() {
        paymentActivation(PAYCONIQ, false);
        ShopwareAPIs.getInstance().createCustomer(PAYCONIQ);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(PAYCONIQ, false);
    }

    @Test(priority = 27, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutMBWay() {
        paymentActivation(MBWAY, false);
        ShopwareAPIs.getInstance().createCustomer(MBWAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Portugal");
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(MBWAY, false);
    }

    @Test(priority = 28, description = "Verify the Prepayment is hidden on the checkout page when payment is disabled in admin portal and selected in the Display Invoice Payments configuration")
    public void verifyDisplayInvoicePaymentsFieldForPrepayment() {
        paymentActivation(PREPAYMENT, true);
        displayInvoicePaymentsConfiguration(PREPAYMENT);
        ShopwareAPIs.getInstance().createCustomer(PREPAYMENT);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(INVOICE, false);
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(PREPAYMENT, true);
    }
    @Test(priority = 29, description = "Verify the Invoice payment is hidden on the checkout page when payment is disabled in admin portal and selected in the Display Invoice Payments configuration")
    public void verifyDisplayInvoicePaymentsFieldForInvoice() {
        paymentActivation(INVOICE, true);
        displayInvoicePaymentsConfiguration(INVOICE);
        ShopwareAPIs.getInstance().createCustomer(INVOICE);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_02);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(INVOICE, true);
        shopware.getCheckoutPage().load().enterIframe().isPaymentDisplayed(PREPAYMENT, false);
    }
}
