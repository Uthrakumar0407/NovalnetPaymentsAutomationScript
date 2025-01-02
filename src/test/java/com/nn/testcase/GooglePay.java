package com.nn.testcase;

import com.nn.basetest.BaseTest;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.helpers.ExcelHelpers;
import com.nn.pages.*;
import io.qameta.allure.Step;
import org.testng.annotations.*;

import java.util.HashMap;

import static com.nn.callback.CallbackProperties.CREDITCARD;
import static com.nn.callback.CallbackProperties.GOOGLEPAY;
import static com.nn.constants.Constants.CAPTURE;
import static com.nn.constants.Constants.PRODUCT_1;
import static com.nn.utilities.GooglePayHelper.launchDriverWithGmail;

public class GooglePay extends BaseTest {

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
            .build();

    @BeforeClass(alwaysRun = true)
    @Parameters({"username", "password"})
    public void adminLogin(@Optional("shopadmin") String userName, @Optional("novalnet123") String password) {
        DriverManager.quit();
        launchDriverWithGmail();
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
    }


    @AfterClass(alwaysRun = true)
    public void adminLogout() {
        wooCommerce.getDashBoardPage().adminLogout();
    }

    @Test(priority = 1, description = "Check whether the test transaction is successful with payment action set to Capture, " +
            "verifying token exist in the response, partial refund admin, chargeback and credit events ")
    public void firstOrder() {
        verifyGlobalConfiguration();
        setGpayConfiguration(true,CAPTURE,"",true,false);
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isgpayDisplayed();
        wooCommerce.getCheckoutPage().clickGooglePayButton();


    }
    @Step("Set Payment Configuration active {0}, payment action {1}, minimum authorize amount {2}, test mode {3}, one click {4}, inline form {5}, enforce3D {6}, paymentType {7}")
    public void setGpayConfiguration(boolean paymentActive,
                                        String paymentAction,
                                        String minAuthAmount,
                                        boolean testMode,
                                        boolean enforce3D){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setGpayPaymentConfiguration(
                        paymentActive,
                        paymentAction,
                        minAuthAmount,
                        testMode,
                        enforce3D));
    }
    }



