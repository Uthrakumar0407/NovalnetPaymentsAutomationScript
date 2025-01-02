package com.nn.testcase;

import com.nn.basetest.BaseTest;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.pages.*;
import com.nn.reports.ExtentTestManager;
import io.qameta.allure.Step;
import org.testng.annotations.*;

import java.util.HashMap;

import static com.nn.callback.CallbackProperties.CREDITCARD;
import static com.nn.constants.Constants.CAPTURE;
import static com.nn.constants.Constants.PRODUCT_1;

public class CreditCardPanHashRedirectCard extends BaseTest {

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
    public void adminLogin(@Optional("cc4") String userName, @Optional("wordpress") String password) {
        ExtentTestManager.saveToReport("Admin login","login and set the payment configurations");
        wooCommerce.getAdminPage().openAdminPage();
        wooCommerce.getAdminPage().adminLogin(userName, password);
        setPaymentConfiguration(true,CAPTURE,"",true,false,true,false);
    }


    @AfterClass(alwaysRun = true)
    public void adminLogout() {
        wooCommerce.getDashBoardPage().adminLogout();
    }



    @Test(invocationCount = 10)
    public void redirectCard(){
        navigateCheckout(PRODUCT_1);
        wooCommerce.getCheckoutPage().isCreditCardDisplayed();
        wooCommerce.getCheckoutPage().fillCreditCardForm(wooCommerce.getTestData().get("CardNumberRedirect"),
                                                        wooCommerce.getTestData().get("ExpDate"),
                                                        wooCommerce.getTestData().get("CVC"));
        wooCommerce.getCheckoutPage().placeOrderUsingCreditCardWithRedirection();
    }


    @Step("Set Payment Configuration active {0}, payment action {1}, minimum authorize amount {2}, test mode {3}, one click {4}, inline form {5}, enforce3D {6}, paymentType {7}")
    public void setPaymentConfiguration(boolean paymentActive,
                                        String paymentAction,
                                        String minAuthAmount,
                                        boolean testMode,
                                        boolean oneClick,
                                        boolean inlineFrom,
                                        boolean enforce3D){
        wooCommerce.getSettingsPage().paymentPageLoad();
        wooCommerce.getTxnInfo().putAll(wooCommerce.getSettingsPage()
                .setPaymentConfiguration(
                        paymentActive,
                        paymentAction,
                        minAuthAmount,
                        testMode,
                        oneClick,
                        inlineFrom,
                        enforce3D, null,wooCommerce.getSettingsPage().getPayment(CREDITCARD)));
    }


}
