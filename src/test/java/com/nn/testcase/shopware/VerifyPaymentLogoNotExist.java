package com.nn.testcase.shopware;

import com.nn.apis.ShopwareAPIs;
import com.nn.listeners.RetryListener;
import com.nn.pages.shopware.base.BaseTest;
import com.nn.reports.ExtentTestManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.FORCE_NON_GUARANTEE;
import static com.nn.utilities.ShopwareUtils.SW_PRODUCT_01;

public class VerifyPaymentLogoNotExist extends BaseTest {
    @BeforeClass
    public void setup() {
        ExtentTestManager.saveToReport("setup", "Setting up prerequisite to place order");
        ShopwareAPIs.getInstance().createCustomer(CREDITCARD);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getLoginPage().load().login();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        toggleActivateAllPayments(true);
        setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                FORCE_NON_GUARANTEE, true));
        setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                FORCE_NON_GUARANTEE, true));
        setLogoConfiguration(false);
    }
    @AfterClass(alwaysRun = true)
    public void tear() {
        shopware.getCustomerLoginPage().logout();
        shopware.getNovalnetAdminPortal().openNovalnetAdminPortal();
        shopware.getNovalnetAdminPortal().loadAutomationProject();
        setLogoConfiguration(true);
    }
    @Test(priority = 1,description = "Check whether the cash payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoCashPayment(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Barzahlen/viacash", false)
                .exitIframe();
    }
    @Test(priority = 2,description = "Check whether the CreditCard payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoCreditCard(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Credit/Debit Cards", false)
                .exitIframe();
    }
    @Test(priority = 3,description = "Check whether the Direct Debit SEPA payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoDirectDebitSEPA(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Direct Debit SEPA", false)
                .exitIframe();
    }
    @Test(priority = 4,description = "Check whether the GiroPay payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoGiroPay(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Giropay", false)
                .exitIframe();
    }
    @Test(priority = 5,description = "Check whether the Google Pay payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoGooglePay(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Google Pay", false)
                .exitIframe();
    }
    @Test(priority = 6,description = "Check whether the Invoice payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoInvoice(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Invoice", false)
                .exitIframe();
    }
    @Test(priority = 7,description = "Check whether Guarantee Invoice the payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoGuaranteeInvoice(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Invoice", false)
                .exitIframe();
    }
    @Test(priority = 8,description = "Check whether the Guarantee SEPA payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoGuaranteeSEPA(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Direct Debit SEPA", false)
                .exitIframe();
    }
    @Test(priority = 9,description = "Check whether the Instalment by invoice payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoInstalmentInvoice(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Instalment by invoice", false)
                .exitIframe();
    }
    @Test(priority = 10,description = "Check whether the Instalment SEPA payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoInstalmentSEPA(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Instalment by SEPA direct debit", false)
                .exitIframe();
    }
    @Test(priority = 11,description = "Check whether the Online Bank Transfer payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoOnlineBankTransfer(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Online bank transfer", false)
                .exitIframe();
    }
    @Test(priority = 12,description = "Check whether the Paypal payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoPaypal(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("PayPal", false)
                .exitIframe();
    }
    @Test(priority = 13,description = "Check whether the Pre payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoPrePayment(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Prepayment", false)
                .exitIframe();
    }
    @Test(priority = 14,description = "Check whether the Sofort payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoSofort(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Sofort", false)
                .exitIframe();
    }
    @Test(priority = 15,description = "Check whether the Trustly payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoTrustly(){
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Trustly", false)
                .exitIframe();
    }
    @Test(priority = 16,description = "Check whether the Post Finance payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoPostFinance(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(POSTFINANCE_CARD);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Switzerland");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("PostFinance Card", false)
                .exitIframe();
    }
    @Test(priority = 17,description = "Check whether the Wechat Pay payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoWechatPay(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(WECHATPAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("China");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("WeChat Pay", false)
                .exitIframe();
    }
    @Test(priority = 18,description = "Check whether the Alipay payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoAlipay(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(ALIPAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("China");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Alipay", false)
                .exitIframe();
    }
    @Test(priority = 19,description = "Check whether the Bancontact payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoBancontact(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(WECHATPAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Belgium");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Bancontact", false)
                .exitIframe();
    }
    @Test(priority = 20,description = "Check whether the EPS payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoEPS(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(WECHATPAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Austria");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("eps", false)
                .exitIframe();
    }
    @Test(priority = 21,description = "Check whether the Ideal payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoiDeal(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(WECHATPAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Netherlands");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("iDEAL", false)
                .exitIframe();
    }
    @Test(priority = 22,description = "Check whether the Multibanco payment logo displayed as per admin portal configurations ")
    public void verifyPaymentLogoMultibanco(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(MULTIBANCO);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Portugal");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Multibanco", false)
                .exitIframe();
    }
    @Test(priority = 23,description = "Check whether the Przelewy24 payment logo displayed as per admin portal configurations ", retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogoPrzelewy24(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(PRZELEWY24);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Poland");
        shopware.getMyAccountPage().changeCurrency("PLN");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Przelewy24", false)
                .exitIframe();
    }
    @Test(priority = 24,description = "Check whether the Blik payment logo not displayed as per admin portal configurations ", retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogoDirectDebitACH(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(DIRECT_DEBIT_ACH);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("United States of America");
        shopware.getMyAccountPage().changeCurrency("USD");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Direct Debit ACH", false)
                .exitIframe();
    }
    @Test(priority = 25,description = "Check whether the Blik payment logo not displayed as per admin portal configurations ", retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogoBlik(){
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(BLIK);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Poland");
        shopware.getMyAccountPage().changeCurrency("PLN");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Blik", false)
                .exitIframe();
    }
    @Test(priority = 26, description = "Check whether the Payconiq payment logo not displayed as per admin portal configurations ")
    public void verifyPaymentLogoPayconiq() {
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(PAYCONIQ);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Belgium");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("Payconiq", false)
                .exitIframe();
    }
    @Test(priority = 27, description = "Check whether the Multibanco payment logo not displayed as per admin portal configurations ")
    public void verifyPaymentLogoMBWay() {
        shopware.getCustomerLoginPage().logout();
        ShopwareAPIs.getInstance().createCustomer(MBWAY);
        ShopwareAPIs.getInstance().addProductToCart(SW_PRODUCT_01);
        shopware.getCustomerLoginPage().load().login(ShopwareAPIs.getInstance().getCustomerEmail());
        shopware.getMyAccountPage().changeBillingCountry("Portugal");
        shopware.getCheckoutPage().load().enterIframe()
                .isLogoDisplayed("MB Way", false)
                .exitIframe();
    }
}
