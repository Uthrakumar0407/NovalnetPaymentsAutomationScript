package com.nn.pages.shopware.base;

import com.nn.callback.CreditCardCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.pages.Magento.NovalnetAdminPortal;
import com.nn.pages.shopware.*;
import com.nn.v13.IframeV13;

import java.util.HashMap;

public class BasePage {


    public Shopware shopware = Shopware.builder()
            .loginPage(new AdminLoginPage())
            .ordersPage(new OrdersPage())
            .settingsPage(new SettingsPage())
            .customerLoginPage(new CustomerLoginPage())
            .checkoutPage(new CheckoutPage())
            .myAccountPage(new MyAccountPage())
            .iframeV13(new IframeV13())
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .testData2(ExcelHelpers.declineCreditCards())
            .build();

    public void verifyGlobalConfigAndActivatePayments(){
        shopware.getLoginPage()
                .load()
                .login();
        shopware.getSettingsPage()
                .load()
                .verifyGlobalConfiguration()
                .enablePayments();
    }

}
