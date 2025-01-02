package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPIs;
import com.nn.callback.DIrectDebitACH;
import com.nn.callback.DirectDebitSEPACallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

public class DirectDebitACH extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .orderPage(new OrderPage())
            .iCallback(new DIrectDebitACH())
            .myAccountPage(new MyAccountPage())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .build();
    @BeforeClass(alwaysRun = true)
    public void setUpCustomerWithGuaranteePaymentConfiguration() {
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
        createCustomer(DIRECT_DEBIT_ACH);
        updateProductStock(PRODUCT_SEPA);
        updateProductPrice(PRODUCT_SEPA,300);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(DIRECT_DEBIT_ACH,true);
    }
    @AfterClass(alwaysRun = true)
    public void logout(){
        magentoPage.getShopUserLoginPage().logout();
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        paymentActivation(DIRECT_DEBIT_ACH,true);
    }
    @AfterMethod(alwaysRun = true)
    public void clearCartProducts(){
        clearCart();
    }

    @Test(priority = 1, description = "Check whether the test transaction is successful with payment action set to Capture, verify token exist in response, partial refund shop backend,")
    public void firstOrder() {
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration(DIRECT_DEBIT_ACH, Map.of(
                TESTMODE, false,
                PAYMENT_ACTION, CAPTURE
        ));
        addProductToCart(PRODUCT_SEPA, 1);
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
        magentoPage.getMyAccountPage().changeCurrency("USD");
        magentoPage.getCheckoutPage()
                .load()
                .openCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_ACH)
                .fill_ACH_Data(magentoPage.getTestData().get("accountNumberACH"),magentoPage.getTestData().get("routingNumberACH"))
                .clickPlaceOrderBtn();
    }
}
