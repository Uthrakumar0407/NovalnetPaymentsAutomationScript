package com.nn.testcase.Magento;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPI_Helper.*;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

import com.nn.apis.MagentoAPIs;
import com.nn.drivers.DriverManager;
import com.nn.pages.Magento.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.reports.ExtentTestManager;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.*;

public class AllowedCountriesCurrenciesValidationTest extends BaseTest{


    MagentoPage magentoPage = MagentoPage.builder()
            .checkoutPage(new CheckoutPage())
            .myAccountPage(new MyAccountPage())
            .shopUserLoginPage(new ShopUserLoginPage())
            .novalnetAdminPortal(new NovalnetAdminPortal())
            .build();

    List<String> countries;
    List<String> currencies;
    String paymentType;

    @BeforeClass
    public void closeExistingDriver(){

        try {
            if (Arrays.asList(ALLOWED_COUNTRY.split(",")) == null || Arrays.asList(ALLOWED_CURRENCY.split(",")) == null ||
                    Arrays.asList(ALLOWED_COUNTRY.split(",")).isEmpty() || Arrays.asList(ALLOWED_CURRENCY.split(",")).isEmpty()) {
                Assert.fail("Possible field values are missing either country or currency.");
            }
            countries = Arrays.asList(ALLOWED_COUNTRY.split(","));
            currencies = Arrays.asList(ALLOWED_CURRENCY.split(","));
            paymentType = PAYMENT_TYPE;
            ExtentTestManager.saveToReport("Setting up DataProvider","Intializing extent test manager");
            updatePaymentConfiguration(paymentType);
            quitDriver();
        }catch(Exception e) {Assert.fail("Possible field values are missing either country or currency.");}
    }

    @BeforeMethod
    public void setUpCustomer(){
        WebDriver driver = setupBrowser("chrome");
        DriverManager.setDriver(driver);
        createCustomer(paymentType);
        addProductToCart(PRODUCT_ADMIN,1);
        ExtentTestManager.saveToReport("Setup customer "+MagentoAPIs.getCustomerEmail(),"Set up customer and data");
        magentoPage.getShopUserLoginPage().SigninToShop(MagentoAPIs.getCustomerEmail(),SHOP_FRONTEND_PASSWORD);
    }

    @AfterMethod
    public void closeCustomer(){
        quitDriver();
    }

    @Test(dataProvider = "countryISO")
    public void allowedCurrencyCountry(String currency, String country) {
        magentoPage.getMyAccountPage()
                .load()
                .changeCountry(country)
                .changeCurrency(currency);
        magentoPage.getCheckoutPage()
                .load()
                .openCheckoutPage()
                .isPaymentDisplayed(paymentType,true);
    }



    @DataProvider(name = "countryISO", parallel = true)
    public Object[][] countryProvider(){
        return currencies.stream()
                .flatMap(currency -> countries.stream().map(country-> new Object[]{currency, country}))
                .toArray(Object[][]::new);
    }


    @DataProvider(name = "currencyISO")
    public Object[][] currencyProvider(){
        return Objects.requireNonNull(PaymentsLocalization.getCurrency(CREDITCARD))
                .stream()
                .map(s-> new Object[]{s})
                .toArray(Object[][]::new);
    }



    public void updatePaymentConfiguration(String paymentType){
        switch (paymentType){
            case DIRECT_DEBIT_SEPA:
                magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
                magentoPage.getNovalnetAdminPortal().loadAutomationProject();
                setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                        FORCE_NON_GUARANTEE,true
                ));
                updateProductPrice(PRODUCT_ADMIN,300);
                break;
            case GUARANTEED_DIRECT_DEBIT_SEPA:
            case "GUARANTEED_DIRECT_DEBIT_SEPA_B2B":
                magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
                magentoPage.getNovalnetAdminPortal().loadAutomationProject();
                setPaymentConfiguration(GUARANTEED_DIRECT_DEBIT_SEPA, Map.of(
                        MIN_AUTH_AMOUNT,"",
                        MIN_ORDER_AMOUNT,"999",
                        ALLOW_B2B,true
                ));
                updateProductPrice(PRODUCT_ADMIN,1000);
                break;
            case INVOICE:
                magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
                magentoPage.getNovalnetAdminPortal().loadAutomationProject();
                magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
                magentoPage.getNovalnetAdminPortal().loadAutomationProject();
                setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                        FORCE_NON_GUARANTEE,true
                ));
                updateProductPrice(PRODUCT_ADMIN,300);
                break;
            case GUARANTEED_INVOICE:
            case "GUARANTEED_INVOICE_B2B":
                magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
                magentoPage.getNovalnetAdminPortal().loadAutomationProject();
                setPaymentConfiguration(GUARANTEED_INVOICE, Map.of(
                        MIN_AUTH_AMOUNT,"",
                        MIN_ORDER_AMOUNT,"999",
                        FORCE_NON_GUARANTEE,true,
                        ALLOW_B2B,true
                ));
                updateProductPrice(PRODUCT_ADMIN,1000);
                break;
            case INSTALMENT_INVOICE:
            case "INSTALMENT_INVOICE_B2B":
                magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
                magentoPage.getNovalnetAdminPortal().loadAutomationProject();
                setPaymentConfiguration(INSTALMENT_INVOICE, Map.of(
                        MIN_AUTH_AMOUNT,"",
                        MIN_ORDER_AMOUNT,"1998",
                        ALLOW_B2B,true
                ));
                updateProductPrice(PRODUCT_ADMIN,2000);
                break;
            case INSTALMENT_DIRECT_DEBIT_SEPA:
            case "INSTALMENT_DIRECT_DEBIT_SEPA_B2B":
                magentoPage.getNovalnetAdminPortal().openNovalnetAdminPortal();
                magentoPage.getNovalnetAdminPortal().loadAutomationProject();
                setPaymentConfiguration(INSTALMENT_DIRECT_DEBIT_SEPA, Map.of(
                        MIN_AUTH_AMOUNT,"",
                        MIN_ORDER_AMOUNT,"1998",
                        ALLOW_B2B,true
                ));
                updateProductPrice(PRODUCT_ADMIN,2000);
                break;
            default:

        }
    }
}
