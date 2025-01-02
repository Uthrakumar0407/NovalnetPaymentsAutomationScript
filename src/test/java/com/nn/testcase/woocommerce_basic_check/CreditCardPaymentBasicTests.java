package com.nn.testcase.woocommerce_basic_check;

import com.nn.basetest.BaseTest;
import com.nn.callback.AlipayCallbackEvents;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.pages.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.HashMap;

public class CreditCardPaymentBasicTests extends BaseTest {

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
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .testData2(ExcelHelpers.declineCreditCards())
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("cc") String userName, @Optional("wordpress") String password) {
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }

    @Test
    public void test1(){
      validateGlobalCofigurations();

    }




}
