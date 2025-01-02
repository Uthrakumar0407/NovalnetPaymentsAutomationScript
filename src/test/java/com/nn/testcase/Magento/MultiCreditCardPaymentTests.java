package com.nn.testcase.Magento;

import com.nn.apis.MagentoAPIs;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.helpers.ExcelHelpers;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.CAPTURE;
import static com.nn.Magento.Constants.PRODUCT_CREDIT_CARD_PAY;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.CREDITCARD;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.ENF_3D;

public class MultiCreditCardPaymentTests extends BaseTest {

    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .successPage(new SuccessPage())
            .callback(new CreditCardCallbackEvents())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.declineCreditCards())
            .build();


    @BeforeClass(alwaysRun = true)
    public void setUpCustomer() {
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
        createCustomer(CREDITCARD);
        updateProductStock(PRODUCT_CREDIT_CARD_PAY);
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration((CREDITCARD), Map.of(
                TESTMODE,true,
                PAYMENT_ACTION,CAPTURE,
                INLINE,true,
                ENF_3D,false
        ));
    }

    @Test
    public void expiredCardProductRemain(){
        setProductRemainCartAfterPaymentFailure(true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("Expired"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVV"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Credit card payment not possible: card expired");
        DriverActions.verifyEquals(isCartEmpty(),false,"Verify cart is not empty after payment failure");
    }

    @Test
    public void restrictedCardProductRemain(){
        setProductRemainCartAfterPaymentFailure(true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("Restricted"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVV"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Restricted card");
        DriverActions.verifyEquals(isCartEmpty(),false,"Verify cart is not empty after payment failure");
    }

    @Test
    public void insufficientFundsCardProductRemain(){
        setProductRemainCartAfterPaymentFailure(true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("InsufficientFunds"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVV"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Insufficient funds or credit limit exceeded");
        DriverActions.verifyEquals(isCartEmpty(),false,"Verify cart is not empty after payment failure");
    }

    @Test
    public void expiredCardProductNotRemain(){
        setProductRemainCartAfterPaymentFailure(false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("Expired"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVV"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Credit card payment not possible: card expired");
        DriverActions.verifyEquals(isCartEmpty(),true,"Verify cart is empty after payment failure");
    }

    @Test
    public void restrictedCardProductNotRemain(){
        setProductRemainCartAfterPaymentFailure(false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("Restricted"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVV"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Restricted card");
        DriverActions.verifyEquals(isCartEmpty(),true,"Verify cart is empty after payment failure");
    }

    @Test
    public void insufficientFundsCardProductNotRemain(){
        setProductRemainCartAfterPaymentFailure(false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("InsufficientFunds"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVV"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().submitCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("Insufficient funds or credit limit exceeded");
        DriverActions.verifyEquals(isCartEmpty(),true,"Verify cart is empty after payment failure");
    }

    @Test
    public void redirectCardEndUserCancelProductNotRemain(){
        setProductRemainCartAfterPaymentFailure(false);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("Restricted"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVV"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().cancelCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("3D secure authentication failed or was cancelled");
        DriverActions.verifyEquals(isCartEmpty(),true,"Verify cart is empty after payment failure");
    }

    @Test
    public void redirectCardEndUserCancelProductRemain(){
        setProductRemainCartAfterPaymentFailure(true);
        addProductToCart(PRODUCT_CREDIT_CARD_PAY,1);
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .verifyInlineFormDisplayed(true)
                .fillCreditCardForm(magentoPage.getTestData().get("Restricted"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVV"))
                .clickPlaceOrderBtn();
        magentoPage.getCheckoutPage().cancelCCAuthenticationPage();
        magentoPage.getCheckoutPage().verifyPaymentErrorMessage("3D secure authentication failed or was cancelled");
        DriverActions.verifyEquals(isCartEmpty(),false,"Verify cart is not empty after payment failure");
    }

}
