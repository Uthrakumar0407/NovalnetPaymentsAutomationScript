package com.nn.testcase.Magento;

import com.aventstack.extentreports.Status;
import com.nn.apis.MagentoAPIs;
import com.nn.apis.TID_Helper;
import com.nn.callback.CreditCardCallbackEvents;
import com.nn.drivers.DriverManager;
import com.nn.helpers.ExcelHelpers;
import com.nn.listeners.RetryListener;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.apis.MagentoAPI_Helper.verifyOrderStatus;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.AUTHORIZE;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;
import static com.nn.utilities.DriverActions.checkElementDisplayed;
import static com.nn.utilities.DriverActions.sleep;

public class MinAuthAmount extends BaseTest {

    int SHIPPING_RATE=500;
    MagentoPage magentoPage = MagentoPage.builder()
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .shopUserLoginPage(new ShopUserLoginPage())
            .shopBackEndLoginPage(new ShopBackEndLoginPage())
            .checkoutPage(new CheckoutPage())
            .myAccountPage(new MyAccountPage())
            .successPage(new SuccessPage())
            .txnInfo(new HashMap<>())
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .build();

    @Test(priority = 1, description = "Check Credit Card authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void creditCard_Authorize(){
        createCustomer(CREDITCARD);
        updateProductStock(PRODUCT_CC_MIN_AUTH_AMOUNT);
        updateProductPrice(PRODUCT_CC_MIN_AUTH_AMOUNT,Double.parseDouble(ORDER_AMOUNT));
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration((CREDITCARD), Map.of(
                TESTMODE,false,
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,MIN_TXN_AMOUNT_AUTH,
                INLINE,true,
                ENF_3D,false
        ));
        addProductToCart(PRODUCT_CC_MIN_AUTH_AMOUNT,1);
        magentoPage.getShopUserLoginPage().logout();
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(CREDITCARD)
                .fillCreditCardForm(magentoPage.getTestData().get("CardNumberDirect"), magentoPage.getTestData().get("ExpDate"), magentoPage.getTestData().get("CVC"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(CREDITCARD),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();

        double orderTXNAmount=Double.parseDouble(ORDER_AMOUNT)+SHIPPING_RATE; // added shipping
        double minAuthTXNAmount=Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

        if(orderTXNAmount >= minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, CREDITCARD);
            statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, false, paymentComments, paymentName);
        }
        else if(orderTXNAmount < minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, CREDITCARD);
            statusCommentsVerification(orderNumber, COMPLETION_ORDER_STATUS, true, paymentComments, paymentName);
        }

    }

    @Test(priority = 2, description = "Check DIRECT debit (SEPA) authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void directDebitSEPA_Authorize(){
        createCustomer(DIRECT_DEBIT_SEPA);
        updateProductStock(PRODUCT_SEPA_MIN_AUTH_AMOUNT);
        updateProductPrice(PRODUCT_SEPA_MIN_AUTH_AMOUNT,Double.parseDouble(ORDER_AMOUNT));
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        double orderTXNAmount=Double.parseDouble(ORDER_AMOUNT)+SHIPPING_RATE; // added shipping
        int orderValue = (int) orderTXNAmount+1000;
        setPaymentConfiguration((GUARANTEED_DIRECT_DEBIT_SEPA), Map.of(
                MIN_ORDER_AMOUNT,orderValue,
                FORCE_NON_GUARANTEE,true
        ));
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration((DIRECT_DEBIT_SEPA), Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,MIN_TXN_AMOUNT_AUTH


        ));
        addProductToCart(PRODUCT_SEPA_MIN_AUTH_AMOUNT,1);
        magentoPage.getShopUserLoginPage().logout();
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA)
                .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(DIRECT_DEBIT_SEPA),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();


        double minAuthTXNAmount=Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

        if(orderTXNAmount >= minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, DIRECT_DEBIT_SEPA);
            statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, false, paymentComments, paymentName);
        }   else if(orderTXNAmount < minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, DIRECT_DEBIT_SEPA);
            statusCommentsVerification(orderNumber, COMPLETION_ORDER_STATUS, true, paymentComments, paymentName);

        }
    }


    @Test(priority = 3, description = "Check Payment by Invoice authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void invoice_Authorize(){
        createCustomer(INVOICE);
        updateProductStock(PRODUCT_INVOICE_MIN_AUTH_AMOUNT);
        updateProductPrice(PRODUCT_INVOICE_MIN_AUTH_AMOUNT,Double.parseDouble(ORDER_AMOUNT));
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        double orderTXNAmount=Double.parseDouble(ORDER_AMOUNT)+SHIPPING_RATE; // added shipping
        int orderValue = (int) orderTXNAmount+1000;
        setPaymentConfiguration((GUARANTEED_INVOICE), Map.of(
                MIN_ORDER_AMOUNT,orderValue,
                FORCE_NON_GUARANTEE,true
        ));
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        setPaymentConfiguration((INVOICE), Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,MIN_TXN_AMOUNT_AUTH
        ));
        addProductToCart(PRODUCT_INVOICE_MIN_AUTH_AMOUNT,1);
        magentoPage.getShopUserLoginPage().logout();
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getCheckoutPage()
                .isPaymentMethodDisplayed(INVOICE)
                .clickPlaceOrderBtn();
        magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
        magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
        String tid = magentoPage.getTxnInfo().get("TID").toString(),
                orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INVOICE),
                paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();


        double minAuthTXNAmount=Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

        if(orderTXNAmount >= minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, INVOICE);
            statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, false, paymentComments, paymentName);
        }else if(orderTXNAmount < minAuthTXNAmount) {
            TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_PENDING, INVOICE);
            statusCommentsVerification(orderNumber, PROCESSING_ORDER_STATUS, true, paymentComments, paymentName);
        }

    }


    @Test(priority = 4, description = "Check Direct debit SEPA with payment guarantee authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void guarantee_SEPA_Authorize(){
        createCustomer(GUARANTEED_DIRECT_DEBIT_SEPA);
        updateProductStock(PRODUCT_GUARANTEE_SEPA_MIN_AUTH_AMOUNT);
        updateProductPrice(PRODUCT_GUARANTEE_SEPA_MIN_AUTH_AMOUNT,Double.parseDouble(ORDER_AMOUNT));
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        double orderTXNAmount=Double.parseDouble(ORDER_AMOUNT)+SHIPPING_RATE; // added shipping
        int orderValue = (int) orderTXNAmount;
        setPaymentConfiguration((GUARANTEED_DIRECT_DEBIT_SEPA), Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,MIN_TXN_AMOUNT_AUTH,
                MIN_ORDER_AMOUNT,orderValue,
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_GUARANTEE_SEPA_MIN_AUTH_AMOUNT,1);
        magentoPage.getShopUserLoginPage().logout();
        navigateCheckout(MagentoAPIs.getCustomerEmail());
        magentoPage.getMyAccountPage().load().changeBillingCompany();
        magentoPage.getCheckoutPage()
                .load();
        if(orderValue<999){
             magentoPage.getCheckoutPage()
                    .isPaymentMethodDisplayed(DIRECT_DEBIT_SEPA);
             sleep(5);
            byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("SCREENSHOT - Direct debit SEPA with payment guarantee  is NOT displayed as Minimum Order Value Amount must be greater than or equal to 999 ", new ByteArrayInputStream(screenshotBytes));}
        else {
            magentoPage.getCheckoutPage()
                    .isPaymentMethodDisplayed(GUARANTEED_DIRECT_DEBIT_SEPA)
                    .verifySEPA_DateOfBirthDisplayed(true)
                    .fill_IBAN_SEPA(magentoPage.getTestData().get("IBANDE"))
                    .fill_DOB_SEPA(DOB)
                    .clickPlaceOrderBtn();
            magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
            magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
            String tid = magentoPage.getTxnInfo().get("TID").toString(),
                    orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                    orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                    paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_DIRECT_DEBIT_SEPA),
                    paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();

            double minAuthTXNAmount = Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

            if (orderTXNAmount >= minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_DIRECT_DEBIT_SEPA);
                statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, false, paymentComments, paymentName);
            } else if (orderTXNAmount < minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_DIRECT_DEBIT_SEPA);
                statusCommentsVerification(orderNumber, COMPLETION_ORDER_STATUS, true, paymentComments, paymentName);

            }
        }//outer if min order amount

    }

    @Test(priority = 5, description = "Check Invoice with payment guarantee authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void guarantee_INVOICE_Authorize(){
        createCustomer(GUARANTEED_INVOICE);
        updateProductStock(PRODUCT_GUARANTEE_INVOICE_MIN_AUTH_AMOUNT);
        updateProductPrice(PRODUCT_GUARANTEE_INVOICE_MIN_AUTH_AMOUNT,Double.parseDouble(ORDER_AMOUNT));
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        double orderTXNAmount=Double.parseDouble(ORDER_AMOUNT)+SHIPPING_RATE; // added shipping
        int orderValue = (int) orderTXNAmount;
        setPaymentConfiguration((GUARANTEED_INVOICE), Map.of(
                PAYMENT_ACTION,AUTHORIZE,
                MIN_AUTH_AMOUNT,MIN_TXN_AMOUNT_AUTH,
                MIN_ORDER_AMOUNT,orderValue,
                ALLOW_B2B,false,
                FORCE_NON_GUARANTEE,true
        ));
        addProductToCart(PRODUCT_GUARANTEE_INVOICE_MIN_AUTH_AMOUNT,1);
        magentoPage.getShopUserLoginPage().logout();
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        magentoPage.getMyAccountPage().load();
        magentoPage.getCheckoutPage().load();
        magentoPage.getCheckoutPage().openCheckoutPage();
        if(orderValue<999) {
            magentoPage.getCheckoutPage()
                    .isPaymentMethodDisplayed(INVOICE);
            sleep(5);
            byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("SCREENSHOT - Invoice with payment guarantee  is NOT displayed as Minimum Order Value Amount must be greater than or equal to 999 ", new ByteArrayInputStream(screenshotBytes));
        }
        else {
            magentoPage.getCheckoutPage()
                    .isPaymentMethodDisplayed(GUARANTEED_INVOICE)
                    .verifyInvoice_DateOfBirthDisplayed(true)
                    .fill_DOB_Invoice(DOB)
                    .clickPlaceOrderBtn();
            magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
            magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
            String tid = magentoPage.getTxnInfo().get("TID").toString(),
                    orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                    orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                    paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(GUARANTEED_INVOICE),
                    paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString();


            double minAuthTXNAmount = Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

            if (orderTXNAmount >= minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_ON_HOLD, GUARANTEED_INVOICE);
                statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, false, paymentComments, paymentName);
            } else if (orderTXNAmount < minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, GUARANTEED_INVOICE);
                statusCommentsVerification(orderNumber, COMPLETION_ORDER_STATUS, true, paymentComments, paymentName);
            }
        }// end of min order amount
    }

    @Test(priority = 6, description = "Check Instalment by SEPA Direct debit authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void instalment_DebitSEPA_Authorize() {
        createCustomer(INSTALMENT_DIRECT_DEBIT_SEPA);
        updateProductStock(PRODUCT_INSTALMENT_SEPA_MIN_AUTH_AMOUNT);
        updateProductPrice(PRODUCT_INSTALMENT_SEPA_MIN_AUTH_AMOUNT,Double.parseDouble(ORDER_AMOUNT));
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        double orderTXNAmount=Double.parseDouble(ORDER_AMOUNT)+SHIPPING_RATE; // added shipping
        int orderValue = (int) orderTXNAmount;
        int[] allowedInstalmentCycles = new int[]{2};
        setPaymentConfiguration((INSTALMENT_DIRECT_DEBIT_SEPA), Map.of(
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, MIN_TXN_AMOUNT_AUTH,
                MIN_ORDER_AMOUNT,orderValue,
                ALLOW_B2B, false
        ),allowedInstalmentCycles);

        addProductToCart(PRODUCT_INSTALMENT_SEPA_MIN_AUTH_AMOUNT, 1);
        magentoPage.getShopUserLoginPage().logout();
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        magentoPage.getMyAccountPage().load().changeBillingCompany();
        if(orderValue<1998) {
            magentoPage.getCheckoutPage().load().openCheckoutPage();
            Assert.assertFalse(checkElementDisplayed(magentoPage.getCheckoutPage().getPaymentElement(INSTALMENT_DIRECT_DEBIT_SEPA)), "Instalment by Direct Debit Sepa is NOT Displayed");
            sleep(5);
            byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("SCREENSHOT - Instalment by SEPA Direct debit is NOT displayed as Minimum Order Value Amount must be greater than or equal to 1998 ", new ByteArrayInputStream(screenshotBytes));
        }
        else {
            magentoPage.getCheckoutPage().load().openCheckoutPage()
                    .isPaymentMethodDisplayed(INSTALMENT_DIRECT_DEBIT_SEPA)
                    .fill_InstalmentSEPA_IBAN(magentoPage.getTestData().get("IBANDE"))
                    .verifyInstalmentSepaDateOfBirthDisplayed(true)
                    .selectInstalmentSepaCycle(2)
                    .fill_DOB_InstalmentSEPA(DOB)
                    .clickPlaceOrderBtn();
            magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
            magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
            String tid = magentoPage.getTxnInfo().get("TID").toString(),
                    orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                    orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                    paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_DIRECT_DEBIT_SEPA),
                    paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                    cycleAmount = String.valueOf(Integer.parseInt(orderAmount) / 2);

            double minAuthTXNAmount = Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

            if (orderTXNAmount >= minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_ON_HOLD, INSTALMENT_DIRECT_DEBIT_SEPA);
                statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, false, paymentComments, paymentName);
            } else if (orderTXNAmount < minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, INSTALMENT_DIRECT_DEBIT_SEPA);
                statusCommentsVerification(orderNumber, COMPLETION_ORDER_STATUS, true, paymentComments, paymentName);
            }
        }// instalment check
    }


    @Test(priority = 7, description = "Check Instalment by Invoice authorize test order status by comparing minAuthAmount and order amount",retryAnalyzer = RetryListener.class)
    public void instalment_Invoice_Authorize() {
        createCustomer(INSTALMENT_INVOICE);
        updateProductStock(PRODUCT_INSTALMENT_INVOICE_MIN_AUTH_AMOUNT);
        updateProductPrice(PRODUCT_INSTALMENT_INVOICE_MIN_AUTH_AMOUNT, Double.parseDouble(ORDER_AMOUNT));
        magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        double orderTXNAmount = Double.parseDouble(ORDER_AMOUNT) + SHIPPING_RATE; // added shipping
        int orderValue = (int) orderTXNAmount;
        magentoPage.getNovalnetAdminPortal().loadAutomationProject();
        int[] allowedInstalmentCycles = new int[]{2};
        setPaymentConfiguration((INSTALMENT_INVOICE), Map.of(
                PAYMENT_ACTION, AUTHORIZE,
                MIN_AUTH_AMOUNT, MIN_TXN_AMOUNT_AUTH,
                MIN_ORDER_AMOUNT, orderValue,
                ALLOW_B2B, false
        ), allowedInstalmentCycles);

        addProductToCart(PRODUCT_INSTALMENT_INVOICE_MIN_AUTH_AMOUNT, 1);
        magentoPage.getShopUserLoginPage().logout();
        signInShopFrontend(MagentoAPIs.getCustomerEmail());
        magentoPage.getMyAccountPage().load().changeBillingCompany();
        if (orderValue < 1998) {
            magentoPage.getCheckoutPage().load().openCheckoutPage();
            Assert.assertFalse(checkElementDisplayed(magentoPage.getCheckoutPage().getPaymentElement(INSTALMENT_INVOICE)),"Instalment by Invoice is NOT Displayed");
            sleep(5);
            byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("SCREENSHOT - Instalment by Invoice is NOT displayed as Minimum Order Value Amount must be greater than or equal to 1998 ", new ByteArrayInputStream(screenshotBytes));
        } else {
            magentoPage.getCheckoutPage().load().openCheckoutPage()
                    .isPaymentMethodDisplayed(INSTALMENT_INVOICE)
                    .verifyInstalmentInvoiceDateOfBirthDisplayed(true)
                    .selectInstalmentInvoiceCycle(2)
                    .fill_DOB_InstalmentInvoice(DOB)
                    .clickPlaceOrderBtn();
            magentoPage.getSuccessPage().verifyNovalnetCommentsDisplayed();
            magentoPage.getTxnInfo().putAll(magentoPage.getSuccessPage().getSuccessPageTransactionDetails());
            String tid = magentoPage.getTxnInfo().get("TID").toString(),
                    orderNumber = magentoPage.getTxnInfo().get("OrderNumber").toString(),
                    orderAmount = magentoPage.getTxnInfo().get("TotalAmount").toString(),
                    paymentName = magentoPage.getSuccessPage().getPaymentFromSuccessPage(INSTALMENT_INVOICE),
                    paymentComments = magentoPage.getTxnInfo().get("NovalnetComments").toString(),
                    cycleAmount = String.valueOf(Integer.parseInt(orderAmount) / 2);
            double minAuthTXNAmount = Double.parseDouble(MIN_TXN_AMOUNT_AUTH);

            if (orderTXNAmount >= minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, cycleAmount, TID_STATUS_ON_HOLD, INSTALMENT_INVOICE);
                statusCommentsVerification(orderNumber, ONHOLD_ORDER_STATUS, false, paymentComments, paymentName);
            } else if (orderTXNAmount < minAuthTXNAmount) {
                TID_Helper.verifyTIDInformation(tid, orderAmount, TID_STATUS_CONFIRMED, INSTALMENT_INVOICE);
                statusCommentsVerification(orderNumber, COMPLETION_ORDER_STATUS, true, paymentComments, paymentName);
            }
        }
    }// instalment check
    }
