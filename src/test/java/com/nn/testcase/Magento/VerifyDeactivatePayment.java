package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPIs;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.utilities.GooglePayHelper;
import com.nn.utilities.Log;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.addProductToCart;
import static com.nn.apis.MagentoAPI_Helper.createCustomer;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.BANCONTACT;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.DriverActions.*;
import static com.nn.utilities.DriverActions.clickElementByRefreshing;

public class VerifyDeactivatePayment extends BaseTest {

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
    public void setupPaymentActivation_PreCheck(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        toggleActivateAllPayments(true);

    }

    @AfterClass(alwaysRun = true)
    public void setupPaymentActivation_PostCheck(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        toggleActivateAllPayments(true);

    }
    @Test(priority = 1, description = "Verify that Alipay payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutAliPay(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(ALIPAY,false);
        createCustomer(ALIPAY);
        addProductToCart(PRODUCT_AlI_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(ALIPAY,false);
    }
    @Test(priority = 2, description = "Verify that Bancontact payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutBancontact(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(BANCONTACT,false);
        createCustomer(BANCONTACT);
        addProductToCart(PRODUCT_BAN_CONTACT_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(BANCONTACT,false);
    }
    //test case is removed because cashpayment activation deactivation takes some time.
    //@Test(priority = 3, description = "Verify that CashPayment payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutCashPayment(){
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
    @Test(priority = 4, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutCreditCard(){
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
    @Test(priority = 5, description = "Verify that direct debit sepa payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutDirectDebitSEPA(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(DIRECT_DEBIT_SEPA,false);
        createCustomer(DIRECT_DEBIT_SEPA);
        addProductToCart(PRODUCT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(DIRECT_DEBIT_SEPA,false);
    }
    @Test(priority = 6, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckouteps(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(EPS,false);
        createCustomer(EPS);
        addProductToCart(PRODUCT_EPS_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(EPS,false);
    }
    @Test(priority = 7, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutGiroPay(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GIROPAY,false);
        createCustomer(GIROPAY);
        addProductToCart(PRODUCT_GIRO_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(GIROPAY,false);
    }
    @Test(priority = 8, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal",retryAnalyzer = RetryListener.class)
    public void deactivatePaymentVerifyCheckoutGooglePay(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GOOGLEPAY,false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(GooglePayHelper.EMAILID, GooglePayHelper.PASS);
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        magentoPage.getCheckoutPage().isPaymentDisplayed(GOOGLEPAY,false);
    }
    @Test(priority = 9, description = "Verify that guarantee invoice payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutGuaranteeInvoice(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GUARANTEED_INVOICE,false);
        createCustomer(GUARANTEED_INVOICE);
        addProductToCart(PRODUCT_INVOICE_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(GUARANTEED_INVOICE,false);
    }
    @Test(priority = 10, description = "Verify that guarantee sepa payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutGuaranteeSEPA(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(GUARANTEED_DIRECT_DEBIT_SEPA,false);
        createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        addProductToCart(PRODUCT_SEPA_G,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA,false);
    }
    @Test(priority = 11, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutIdeal(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(IDEAL,false);
        createCustomer(IDEAL);
        addProductToCart(PRODUCT_IDEAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(IDEAL,false);
    }
    @Test(priority = 12, description = "Verify that instalment invoice payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutInstalmentInvoice(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INSTALMENT_INVOICE,false);
        createCustomer(INSTALMENT_INVOICE);
        addProductToCart(PRODUCT_INSTALLMENT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(INSTALMENT_INVOICE,false);
    }
    @Test(priority = 13, description = "Verify that instalment sepa payment is hidden on the checkout page when payment id disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutInstalmetSEPA(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INSTALMENT_DIRECT_DEBIT_SEPA,false);
        createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        addProductToCart(PRODUCT_INSTALLMENT_SEPA,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA,false);
    }
    @Test(priority = 14, description = "Verify that invoice payment is hidden on the checkout page when payment is disabled in admin portal")

    public void deactivatePaymentVerifyCheckoutInvoice(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INVOICE,false);
        createCustomer(INVOICE);
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(INVOICE,false);
    }
    @Test(priority = 15, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutMultibanco(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(MULTIBANCO,false);
        createCustomer(MULTIBANCO);
        addProductToCart(PRODUCT_MULTI_BANCO_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(MULTIBANCO,false);
    }
    @Test(priority = 16, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutOnlineBankTransfer(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(ONLINE_BANK_TRANSFER,false);
        createCustomer(ONLINE_BANK_TRANSFER);
        addProductToCart(PRODUCT_ONLINE_BANK_TRANSFER_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(ONLINE_BANK_TRANSFER,false);
    }
    @Test(priority = 17, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPaypal(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PAYPAL,false);
        createCustomer(PAYPAL);
        addProductToCart(PRODUCT_PAYPAL_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(PAYPAL,false);
    }
    @Test(priority = 18, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPostFinanceCard(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(POSTFINANCE_CARD,false);
        createCustomer(POSTFINANCE_CARD);
        addProductToCart(PRODUCT_POST_FINANCE_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(POSTFINANCE_CARD,false);
    }
    @Test(priority = 19, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPrePayment(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PREPAYMENT,false);
        createCustomer(PREPAYMENT);
        addProductToCart(PRODUCT_PRE_PAYMENT_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(PREPAYMENT,false);
    }

    @Test(priority = 20, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutSofort(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(ONLINE_TRANSFER,false);
        createCustomer(ONLINE_TRANSFER);
        addProductToCart(PRODUCT_SOFORT,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(ONLINE_TRANSFER,false);
    }
    @Test(priority = 21, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutTrustly(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(TRUSTLY,false);
        createCustomer(TRUSTLY);
        addProductToCart(PRODUCT_TRUSTLY_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(TRUSTLY,false);
    }
    @Test(priority = 22, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutWechatPay(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(WECHATPAY,false);
        createCustomer(WECHATPAY);
        addProductToCart(PRODUCT_WE_CHAT_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(WECHATPAY,false);
    }


    @Test(priority = 23, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPrzelewy24(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PRZELEWY24,false);
        createCustomer(PRZELEWY24);
        addProductToCart(PRODUCT_PRZELEWY24_PAY,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().changeCurrency("PLN");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(PRZELEWY24,false);
    }
    @Test(priority = 24, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutBlik(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(BLIK,false);
        createCustomer(BLIK);
        addProductToCart(PRODUCT_BLIK,1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().changeCurrency("PLN");
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(BLIK,false);
    }
    @Test(priority = 25, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckoutPayconiq(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PAYCONIQ,false);
        createCustomer(PAYCONIQ);
        addProductToCart(PRODUCT_PAYCONIQ,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(PAYCONIQ,false);
    }
    @Test(priority = 26, description = "Verify the payment is hidden on the checkout page when payment is disabled in admin portal")
    public void deactivatePaymentVerifyCheckouMBWay(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(MBWAY,false);
        createCustomer(MBWAY);
        addProductToCart(PRODUCT_MBWAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentDisplayed(MBWAY,false);
    }
    @Test(priority = 27,description = "Verify the Prepayment is hidden on the checkout page when payment is disabled in admin portal and selected in the Display Invoice Payments configuration")
    public void verifyDisplayInvoicePaymentsFieldForPrepayment(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(INVOICE,true);
        paymentActivation(PREPAYMENT,true);
        displayInvoicePaymentsConfiguration(INVOICE);
        createCustomer(PREPAYMENT);
        addProductToCart(PRODUCT_PRE_PAYMENT_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentDisplayed(INVOICE,true);
        magentoPage.getCheckoutPage().isPaymentDisplayed(PREPAYMENT,false);
    }
    @Test(priority = 28,description = "Verify the Invoice payment is hidden on the checkout page when payment is disabled in admin portal and selected in the Display Invoice Payments configuration")
    public void verifyDisplayInvoicePaymentsFieldForInvoice(){
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(PREPAYMENT,true);
        paymentActivation(INVOICE,true);
        displayInvoicePaymentsConfiguration(PREPAYMENT);
        createCustomer(INVOICE);
        addProductToCart(PRODUCT_INVOICE,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage().isPaymentDisplayed(PREPAYMENT,true);
        magentoPage.getCheckoutPage().isPaymentDisplayed(INVOICE,false);
    }

    public void toggleActivateAllPayments(boolean activate){
        String[] paymentNames = {PAYPAL,CREDITCARD,IDEAL,MULTIBANCO,ONLINE_TRANSFER,ONLINE_BANK_TRANSFER,INVOICE,PREPAYMENT,GUARANTEED_INVOICE, INSTALMENT_INVOICE,
                BANCONTACT,EPS, GIROPAY,PRZELEWY24,POSTFINANCE_CARD,TRUSTLY,ALIPAY,WECHATPAY,DIRECT_DEBIT_SEPA, GUARANTEED_DIRECT_DEBIT_SEPA,INSTALMENT_DIRECT_DEBIT_SEPA,GOOGLEPAY,BLIK,PAYCONIQ,MBWAY};
        Log.info("Total Payment methods = "+ paymentNames.length);
        clickElementByRefreshing(By.xpath("//a[text()='Payment Methods']"));
        clickElementByRefreshing(By.cssSelector("#edit_payment_methods_modules"));
        for(int i=0;i<paymentNames.length;i++){
            sleep(1);
            String xpath = "//form[@id='edit_payment_methods_update_form']//span[contains(text(),'"+getPaymentForActivation(paymentNames[i])+"')]/../following-sibling::td//input[@type='checkbox']";
            By paymentLoc = By.xpath(xpath);
            boolean actualStatus = checkElementChecked(paymentLoc);
            if(activate != actualStatus){
                clickElementWithJs(paymentLoc);
                Log.info( "Count = " + i + "Payment ativation status -  " + paymentNames[i] + " = "+ activate);
                sleep(0.5); //added to handle flaky failures
            }
        }
        clickElement(By.cssSelector("#edit_payment_methods_update_span_button"));
        waitForElementVisible(By.cssSelector("#product_creation_success_notify_title"));
        clickElementByRefreshing(By.cssSelector(".overlay_close_btn1>a"));
    }
}
