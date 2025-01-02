package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPIs;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

public class VerifyPaymentLogoExist extends BaseTest {


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
                .testData2(ExcelHelpers.declineCreditCards())
                .build();

        @BeforeClass(alwaysRun = true)
        public void setUpCustomer() {
            ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
            createCustomer(INVOICE);
            addProductToCart(PRODUCT_INVOICE,1);
            updateProductStock(PRODUCT_INVOICE);
            magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
            magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
            magentoPage.getNovalnetAdminPortal().loadAutomationProject();
            toggleActivateAllPayments(true);
            setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                    FORCE_NON_GUARANTEE,true));
            setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                    FORCE_NON_GUARANTEE,true));
            setLogoConfiguration(true);
        }

        @AfterClass(alwaysRun = true)
        public void tear() {
            magentoPage.getShopUserLoginPage().logout();
        }


        @Test(priority = 1,description = "Check whether the cash payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoCashpayment(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Barzahlen/viacash",true);
        }

        @Test(priority = 2,description = "Check whether the CreditCard payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoCreditCard(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Credit/Debit Cards",true);
        }
        @Test(priority = 3,description = "Check whether the Direct Debit SEPA payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoDirectDebitSEPA(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Direct Debit SEPA",true);
        }

        @Test(priority = 4,description = "Check whether the GiroPay payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoGiroPay(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Giropay",true);
        }
        @Test(priority = 5,description = "Check whether the Google Pay payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoGooglePay(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Google Pay",true);
        }

        @Test(priority = 6,description = "Check whether the Invoice payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoInvoice(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Invoice",true);
        }

        @Test(priority = 7,description = "Check whether Guarantee Invoice the payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoGuaranteeInvoice(){
            addProductToCart(PRODUCT_INVOICE_G,1);
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Invoice",true);
        }
        @Test(priority = 8,description = "Check whether the Guarantee SEPA payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoGuaranteeSEPA(){
            addProductToCart(PRODUCT_SEPA_G,1);
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Direct Debit SEPA",true);
        }

        @Test(priority = 9,description = "Check whether the Instalment Invoice payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoInstalmentInvoice(){
            addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Instalment by invoice",true);
        }
        @Test(priority = 10,description = "Check whether the Instalment SEPA payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoInstalmentSEPA(){
            addProductToCart(PRODUCT_INSTALLMENT_SEPA,1);
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Instalment by SEPA direct debit",true);
        }
        @Test(priority = 11,description = "Check whether the Online Bank Transfer payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoOnlineBankTransfer(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Online bank transfer",true);
        }
        @Test(priority = 12,description = "Check whether the Paypal payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoPaypal(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("PayPal",true);
        }
        @Test(priority = 13,description = "Check whether the Pre payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoPrePayment(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Prepayment",true);
        }
        @Test(priority = 14,description = "Check whether the Sofort payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoSofort(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Sofort",true);
        }
        @Test(priority = 15,description = "Check whether the Trustly payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoTrustly(){
            magentoPage.getCheckoutPage().load();
            magentoPage.getCheckoutPage().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Trustly",true);
        }

        @Test(priority = 16,description = "Check whether the Post Finance payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoPostFinance(){
            magentoPage.getShopUserLoginPage().logout();
            createCustomer(POSTFINANCE_CARD);
            addProductToCart(PRODUCT_POST_FINANCE_PAY,1);
            navigateCheckout(MagentoAPIs.getCustomerEmail());
            magentoPage.getCheckoutPage().isLogoDisplayed("PostFinance Card",true);
        }
        @Test(priority = 17,description = "Check whether the Wechat Pay payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoWechatPay(){
            magentoPage.getShopUserLoginPage().logout();
            createCustomer(WECHATPAY);
            addProductToCart(PRODUCT_WE_CHAT_PAY,1);
            navigateCheckout(MagentoAPIs.getCustomerEmail());
            magentoPage.getCheckoutPage().isLogoDisplayed("WeChat Pay",true);
        }
        @Test(priority = 18,description = "Check whether the Alipay payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoAlipay(){
            magentoPage.getShopUserLoginPage().logout();
            createCustomer(ALIPAY);
            addProductToCart(PRODUCT_AlI_PAY,1);
            navigateCheckout(MagentoAPIs.getCustomerEmail());
            magentoPage.getCheckoutPage().isLogoDisplayed("Alipay",true);
        }
        @Test(priority = 19,description = "Check whether the Bancontact payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoBancontact(){
            magentoPage.getShopUserLoginPage().logout();
            createCustomer(BANCONTACT);
            addProductToCart(PRODUCT_BAN_CONTACT_PAY,1);
            navigateCheckout(MagentoAPIs.getCustomerEmail());
            magentoPage.getCheckoutPage().isLogoDisplayed("Bancontact",true);
        }
        @Test(priority = 20,description = "Check whether the EPS payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoEPS(){
            magentoPage.getShopUserLoginPage().logout();
            createCustomer(EPS);
            addProductToCart(PRODUCT_EPS_PAY,1);
            navigateCheckout(MagentoAPIs.getCustomerEmail());
            magentoPage.getCheckoutPage().isLogoDisplayed("eps",true);
        }
        @Test(priority = 21,description = "Check whether the Ideal payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoiDeal(){
            magentoPage.getShopUserLoginPage().logout();
            createCustomer(IDEAL);
            addProductToCart(PRODUCT_IDEAL_PAY,1);
            navigateCheckout(MagentoAPIs.getCustomerEmail());
            magentoPage.getCheckoutPage().isLogoDisplayed("iDEAL",true);
        }
        @Test(priority = 22,description = "Check whether the Multibanco payment logo displayed as per admin portal configurations ")
        public void verifyPaymentLogoMultibanco(){
            magentoPage.getShopUserLoginPage().logout();
            createCustomer(MULTIBANCO);
            addProductToCart(PRODUCT_MULTI_BANCO_PAY,1);
            navigateCheckout(MagentoAPIs.getCustomerEmail());
            magentoPage.getCheckoutPage().isLogoDisplayed("Multibanco",true);
        }
        @Test(priority = 23,description = "Check whether the Przelewy24 payment logo displayed as per admin portal configurations ", retryAnalyzer = RetryListener.class)
        public void verifyPaymentLogoPrzelewy24(){
            magentoPage.getShopUserLoginPage().logout();
            createCustomer(PRZELEWY24);
            addProductToCart(PRODUCT_PRZELEWY24_PAY,1);
            navigateCheckout(MagentoAPIs.getCustomerEmail());
            magentoPage.getMyAccountPage().changeCurrency("PLN");
            magentoPage.getCheckoutPage().load().openCheckoutPage();
            magentoPage.getCheckoutPage().isLogoDisplayed("Przelewy24",true);
        }

    @Test(priority = 24,description = "Check whether the Blik payment logo displayed as per admin portal configurations ", retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogoBlik(){
        magentoPage.getShopUserLoginPage().logout();
        createCustomer(BLIK);
        addProductToCart(PRODUCT_BLIK,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getMyAccountPage().changeCurrency("PLN");
        magentoPage.getCheckoutPage().load().openCheckoutPage();
        magentoPage.getCheckoutPage().isLogoDisplayed("Blik",true);
    }
    @Test(priority = 25,description = "Check whether the Payconiq payment logo displayed as per admin portal configurations ", retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogoPayconiq(){
        magentoPage.getShopUserLoginPage().logout();
        createCustomer(PAYCONIQ);
        addProductToCart(PRODUCT_PAYCONIQ,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("Payconiq",true);
    }
    @Test(priority = 25,description = "Check whether the MBWay payment logo displayed as per admin portal configurations ", retryAnalyzer = RetryListener.class)
    public void verifyPaymentLogoMBWay(){
        magentoPage.getShopUserLoginPage().logout();
        createCustomer(MBWAY);
        addProductToCart(PRODUCT_MBWAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isLogoDisplayed("MB Way",true);
    }
    }

