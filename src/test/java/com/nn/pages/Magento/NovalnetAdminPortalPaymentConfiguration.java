package com.nn.pages.Magento;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.*;

import com.aventstack.extentreports.Status;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NovalnetAdminPortalPaymentConfiguration {

    private NovalnetAdminPortalPaymentConfiguration(){
        //Don't want to create an instance
    }

    private static By activePaymentDiv = By.cssSelector("div[class='payments-div']");
    private static By paymentConfigiFrame = By.cssSelector("#shopConfigFrame");
    private static By paymentTypeDropdown = By.cssSelector("#payment_types_chosen");
    private static By paymentActionContainer = By.cssSelector("div[class='payments-div'] [id$='payment_action_chosen'] span");
    private static By paymentActionList = By.cssSelector("div[class='payments-div'] [id$='payment_action_chosen'] li");
    private static By testModeBtn = By.cssSelector("div[class='payments-div'] [id$='test_mode']");
    private static By minimumAuthAmount = By.cssSelector("div[class='payments-div'] [id$='auth_amount']");
    private static By minimumGuaranteeAmount = By.cssSelector("div[class='payments-div'] [id$='min_amount']");
    private static By dueDate = By.cssSelector("div[class='payments-div'] [id$='due_date']");
    private static By forceNonGurantee = By.cssSelector("div[class='payments-div'] [id$='force_non_gurantee']");
    private static By allowB2B = By.cssSelector("div[class='payments-div'] [id$='allow_b2b']");

    //cc
    private static By enforce3DBtn = By.cssSelector("#creditcard__enforce_3d");
    private static By inlineFormBtn = By.cssSelector("#creditcard__cc_inline_form");

    //instalment
    private static By selectedInstalmentCycles = By.cssSelector("div[class='payments-div'] [id$='instalment_cycles_chosen'] li.search-choice>span");
    private static By instalmentCycleSearch = By.cssSelector("div[class='payments-div'] [id$='instalment_cycles_chosen'] input");
    private static By instalmentCycleList = By.cssSelector("div[class='payments-div'] [id$='instalment_cycles_chosen'] .chosen-results li");

    //gpay
    private static By gpaySellerName = By.cssSelector("#googlepay__seller_name");
    private static By gpayEnforce3d = By.cssSelector("#googlepay__enforce_3d");

    public static final String TESTMODE = "TESTMODE";
    public static final String ENF_3D = "ENF_3D";
    public static final String INLINE = "INLINE";
    public static final String PAYMENT_ACTION = "PAYMENT_ACTION";
    public static final String MIN_AUTH_AMOUNT = "MIN_AUTH_AMOUNT";
    public static final String MIN_ORDER_AMOUNT = "MIN_ORDER_AMOUNT";
    public static final String DUE_DATE = "DUE_DATE";
    public static final String FORCE_NON_GUARANTEE = "FORCE_NON_GUARANTEE";
    public static final String ALLOW_B2B = "ALLOW_B2B";

    //by  Nizam
    public static final String PAYMENT_LOGO = "PAYMENT_LOGO";

    public static final String BUSINESS_NAME = "BUSINESS_NAME";

    public static final String ENF_3D_GPAY = "ENF_3D_GPAY";


    @Step("Set payment configuration for payment type {0} in the admin portal")
    public static void setPaymentConfiguration(String paymentType, Map<String, Object> configuration){
        selectPayment(getPayment(paymentType));
        paymentConfiguration().accept(configuration);
        clickElement(By.cssSelector("div[class='payments-div'] input[value='Save']"));
        switchToDefaultContent();
        sleep(3);
        clickElementByRefreshing(By.cssSelector("div.sweet-alert button.confirm"));
        int i=0;
        while (checkElementDisplayed(By.cssSelector("div.sweet-alert[data-has-done-function='false'] button.confirm"))&&(i<3)){ // added by nagesh to avoid element click intercepted exception
            clickElementWithJs(By.cssSelector("div.sweet-alert[data-has-done-function='false'] button.confirm"));
            i++;
        }
        ExtentTestManager.logMessage(Status.PASS,"<b>Payment configuration set for "+paymentType+printMap(configuration)+"</b>");
        AllureManager.saveLog("<b>Payment configuration set for "+paymentType+printMap(configuration)+"</b>");
    }

    @Step("Set payment configuration for payment type {0} in the admin portal")
    public static void setPaymentConfiguration(String paymentType, Map<String, Object> configuration, int[] instalemntCycles){
        selectPayment(getPayment(paymentType));
        paymentConfiguration().accept(configuration);
        selectInstalmentCycles(instalemntCycles);
        clickElement(By.cssSelector("div[class='payments-div'] input[value='Save']"));
        switchToDefaultContent();
        sleep(2);
        clickElementByRefreshing(By.cssSelector("div.sweet-alert button.confirm"));
        sleep(1);
        clickElementByRefreshing(By.cssSelector("div.sweet-alert[data-has-done-function='false'] button.confirm"));
        ExtentTestManager.logMessage(Status.PASS,"Payment configuration set for "+paymentType+printMap(configuration));
        AllureManager.saveLog("Payment configuration set for "+paymentType+printMap(configuration));
    }

    private static Consumer<Map<String,Object>> paymentConfiguration(){
        return map -> map.forEach((key,value) -> {
            switch (key) {
                case PAYMENT_ACTION:
                    selectPaymentAction(map.get(PAYMENT_ACTION).toString());
                    break;
                case TESTMODE:
                    selectToggleButton(testModeBtn, Boolean.parseBoolean(map.get(TESTMODE).toString()));
                    break;
                case ENF_3D:
                    selectToggleButton(enforce3DBtn, Boolean.parseBoolean(map.get(ENF_3D).toString()));
                    break;
                case INLINE:
                    selectToggleButton(inlineFormBtn, Boolean.parseBoolean(map.get(INLINE).toString()));
                    break;
                case FORCE_NON_GUARANTEE:
                    selectToggleButton(forceNonGurantee, Boolean.parseBoolean(map.get(FORCE_NON_GUARANTEE).toString()));
                    break;
                case ALLOW_B2B:
                    selectToggleButton(allowB2B, Boolean.parseBoolean(map.get(ALLOW_B2B).toString()));
                    break;
                case MIN_AUTH_AMOUNT:
                    setTextIfNot(minimumAuthAmount, map.get(MIN_AUTH_AMOUNT).toString());
                    break;
                case MIN_ORDER_AMOUNT:
                    setTextIfNot(minimumGuaranteeAmount, map.get(MIN_ORDER_AMOUNT).toString());
                    break;
                case DUE_DATE:
                    setTextIfNot(dueDate, map.get(DUE_DATE).toString());
                    break;
                case BUSINESS_NAME:
                    setTextIfNot(gpaySellerName, map.get(BUSINESS_NAME).toString());
                    break;
                case ENF_3D_GPAY:
                    selectToggleButton(gpaySellerName, Boolean.parseBoolean(map.get(ENF_3D_GPAY).toString()));
                    break;
                default:
                    throw new RuntimeException("Invalid Key "+key+" to set payment configuration");
            }
        });
    }


    private static String getPayment(String paymentType) {
        switch (paymentType) {
            case PAYPAL:
                return "PayPal";
            case CREDITCARD:
                return "Credit card payment";
            case IDEAL:
                return "iDEAL";
            case MULTIBANCO:
                return "Multibanco";
            case PREPAYMENT:
                return "Prepayment";
            case CASHPAYMENT:
                return "Cash Payment";
            case ONLINE_TRANSFER:
                return "Sofort online bank transfer";
            case ONLINE_BANK_TRANSFER:
                return "Online Bank Transfers";
            case INVOICE:
                return "Payment by invoice";
            case GUARANTEED_INVOICE:
                return "Invoice with payment guarantee";
            case INSTALMENT_INVOICE:
                return "Instalment by invoice";
            case BANCONTACT:
                return "Bancontact";
            case EPS:
                return "eps Online Transfer";
            case GIROPAY:
                return "GIROPAY";
            case PRZELEWY24:
                return "Przelewy24";
            case POSTFINANCE_CARD:
                return "PostFinance Card";
            case TRUSTLY:
                return "Trustly";
            case ALIPAY:
                return "Alipay";
            case WECHATPAY:
                return "WeChat Pay";
            case DIRECT_DEBIT_SEPA:
                return "Direct debit (SEPA)";
            case GUARANTEED_DIRECT_DEBIT_SEPA:
                return "Direct debit SEPA with payment guarantee";
            case INSTALMENT_DIRECT_DEBIT_SEPA:
                return "Instalment by SEPA direct debit";
            case GOOGLEPAY:
                return "Google Pay";
            case DIRECT_DEBIT_ACH:
                return "Direct Debit ACH";
            case BLIK:
                return "Blik";
            case PAYCONIQ:
                return "Payconiq";
            case MBWAY:
                return "MB WAY";
            default:
                throw new IllegalArgumentException("Invalid payment method: " + paymentType);
        }
    }


    private static void selectPayment(String paymentType){
        clickElement(By.xpath("//a[text()='Payment plugin configuration']"));
        switchToFrame(paymentConfigiFrame);
        clickElement(paymentTypeDropdown);
        waitForElementAttributeToChange(paymentTypeDropdown,"class","chosen-container-active");
        setText(getElement(paymentTypeDropdown).findElement(By.cssSelector(".chosen-search-input")),paymentType);
        WebElement selectedPayment = getElement(paymentTypeDropdown).findElements(By.cssSelector("ul li"))
                .stream()
                .filter(e-> e.getAttribute("class").contains("highlighted"))
                .findFirst()
                .orElse(null);

        if(selectedPayment != null){
            clickElementWithAction(selectedPayment);
            sleep(1);
        }else {
            Assert.fail("Invalid payment type: "+paymentType);
        }
    }

    private static void selectPaymentAction(String paymentAction){
        if(!getElementText(paymentActionContainer).trim().equals(paymentAction)){
            clickElement(paymentActionContainer);
            getElements(paymentActionList)
                    .stream()
                    .filter(e -> e.getText().equals(paymentAction))
                    .findFirst()
                    .ifPresentOrElse(WebElement::click,
                            ()->{throw new RuntimeException("Invalid value to select in the payment action dropdown.");});
        }
    }

    private static void selectToggleButton(By by, boolean enable) {
        String text = getElementAttributeText(by, "checked");
        if( (enable == true && text == null) || (enable == false && text != null) )
            clickElement(by);
    }

    @Step("Set allowed instalment cycles in the checkout")
    private static void selectInstalmentCycles(int[] cycles){
        if(getElements(By.cssSelector("div[class='payments-div'] [id$='instalment_cycles_chosen'] ul.chosen-choices li")).size() > 1){
            clickElementWithAction(instalmentCycleSearch);
            int size = getElements(By.cssSelector("div[class='payments-div'] [id$='instalment_cycles_chosen'] ul.chosen-choices li")).size();
            while (size > 1){
                setTextWithAction(instalmentCycleSearch, Keys.BACK_SPACE);
                --size;
            }
        }

        Arrays.stream(cycles).forEach(cycle -> {
            clickElementWithAction(instalmentCycleSearch);
            getElements(instalmentCycleList)
                    .stream()
                    .filter(e-> Integer.parseInt(e.getText().replaceAll("[^0-9]", "")) == cycle && e.getAttribute("class").contains("active-result"))
                    .forEach(DriverActions::clickElementWithAction);
        });
    }

    private static void setTextIfNot(By by, String text){
        if(!getInputFieldText(by).trim().equals(text)){
            clearTextWithAction(by);
            setTextWithoutClear(by, text);
        }
    }

    @Step("Activate or Deactivate payment")
    public static void paymentActivation(String payment, boolean activate){
        clickElementByRefreshing(By.xpath("//a[text()='Payment Methods']"));
        clickElementWithJs(By.cssSelector("#edit_payment_methods_modules"));
        sleep(1);
        //getElements(By.xpath("//form[@id='edit_payment_methods_update_form']//tr/td[2]/span")).forEach(e-> System.out.println(e.getText()));
        String xpath = "//form[@id='edit_payment_methods_update_form']//span[contains(text(),'"+getPaymentForActivation(payment)+"')]/../following-sibling::td//input[@type='checkbox']";
        By paymentLoc = By.xpath(xpath);
        boolean actualStatus = checkElementChecked(paymentLoc);
        if(activate != actualStatus){
            clickElementWithJs(paymentLoc);
            sleep(0.5); //added to handle flaky failures
            clickElement(By.cssSelector("#edit_payment_methods_update_span_button"));
            waitForElementVisible(By.cssSelector("#product_creation_success_notify_title"));
            clickElementByRefreshing(By.cssSelector(".overlay_close_btn1>a"));
        }

        if((payment.equals(INVOICE)||payment.equals(PREPAYMENT))&& activate){
            displayBothInvoicePayments();
        }

    }
    public static void toggleActivateAllPayments(boolean activate){
        String[] paymentNames = {PAYPAL,CREDITCARD,IDEAL,MULTIBANCO,ONLINE_TRANSFER,ONLINE_BANK_TRANSFER,INVOICE,PREPAYMENT,GUARANTEED_INVOICE, INSTALMENT_INVOICE,
                BANCONTACT,EPS, GIROPAY,PRZELEWY24,POSTFINANCE_CARD,TRUSTLY,ALIPAY,WECHATPAY,DIRECT_DEBIT_SEPA, GUARANTEED_DIRECT_DEBIT_SEPA,INSTALMENT_DIRECT_DEBIT_SEPA,GOOGLEPAY, DIRECT_DEBIT_ACH, BLIK, PAYCONIQ, MBWAY};
        Log.info("Total Payment methods = "+ paymentNames.length);
        clickElementByRefreshing(By.xpath("//a[text()='Payment Methods']"));
        clickElementByRefreshing(By.cssSelector("#edit_payment_methods_modules"));
        sleep(2);
        for(int i=0;i<paymentNames.length;i++){
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

    public static String getPaymentForActivation(String paymentType) {
        switch (paymentType) {
            case PAYPAL:
                return "PayPal";
            case CREDITCARD:
                return "Credit Card";
            case IDEAL:
                return "iDEAL (online bank transfer Netherlands)";
            case MULTIBANCO:
                return "Multibanco";
            case CASHPAYMENT:
                return "Barzahlen/viacash (Deutschland)";
            case ONLINE_TRANSFER:
                return "Sofort online bank transfer";
            case ONLINE_BANK_TRANSFER:
                return "Online bank transfer";
            case INVOICE:
            case PREPAYMENT:
                return "Prepayment / Invoice";
            case GUARANTEED_INVOICE:
                return "Invoice with payment guarantee";
            case INSTALMENT_INVOICE:
                return "Instalment by invoice";
            case BANCONTACT:
                return "Bancontact";
            case EPS:
                return "eps (online bank transfer Austria)";
            case GIROPAY:
                return "giropay";
            case PRZELEWY24:
                return "Przelewy24 (online bank transfer Poland)";
            case POSTFINANCE_CARD:
                return "Postfinance card";
            case TRUSTLY:
                return "Trustly";
            case ALIPAY:
                return "Alipay";
            case WECHATPAY:
                return "WeChat Pay";
            case DIRECT_DEBIT_SEPA:
                return "Direct Debit SEPA";
            case GUARANTEED_DIRECT_DEBIT_SEPA:
                return "Direct debit SEPA with payment guarantee";
            case INSTALMENT_DIRECT_DEBIT_SEPA:
                return "Instalment by SEPA direct debit";
            case GOOGLEPAY:
                return "Google Pay";
            case DIRECT_DEBIT_ACH:
                return "Direct Debit ACH";
            case BLIK:
                return "Blik";
            case PAYCONIQ:
                return "Payconiq";
            case MBWAY:
                return "MB Way";
            default:
                throw new IllegalArgumentException("Invalid payment method: " + paymentType);
        }
    }


    // added by Nizam

    @Step("Set logo configuration")
    public static void setLogoConfiguration(boolean expected){
        clickElement(By.xpath("//a[text()='Payment plugin configuration']"));
        selectPayment("Global configuration");
        //switchToFrame(paymentConfigiFrame);
        if(checkElementChecked(By.id("global_config__display_payment_logo")) != expected) {
            clickElementWithJs(By.id("global_config__display_payment_logo"));
            clickElement(By.cssSelector("div[class='payments-div'] input[value='Save']"));
            switchToDefaultContent();
            sleep(3);
            clickElementByRefreshing(By.cssSelector("div.sweet-alert button.confirm"));
            clickElementByRefreshing(By.cssSelector("div.sweet-alert[data-has-done-function='false'] button.confirm"));
        }
    }

    public static void displayInvoicePaymentsConfiguration(String paymentType) {
        String payment = getPayment(paymentType);
        clickElement(By.xpath("//a[text()='Payment plugin configuration']"));
        selectPayment("Global configuration");

        List<WebElement> payments = getElements(By.xpath("//div[@id='global_config__display_invoice_chosen']//span"));
        for (int i = 0; i < payments.size(); i++) {
            clearTextWithAction(By.xpath("//div[@id='global_config__display_invoice_chosen']"));
        }
        clickElement(By.xpath("//div[@id='global_config__display_invoice_chosen']"));
        waitForElementAttributeToChange(By.xpath("//div[@id='global_config__display_invoice_chosen']"), "class", "chosen-container-active");
        WebElement dropDown = getElement(By.cssSelector("#global_config__display_invoice_chosen"));
        List<WebElement> dropDownOptions =  dropDown.findElements(By.xpath(".//ul/li"));
        for(WebElement options :dropDownOptions){
            if (options.getText().equals(payment)){
                options.click();
                break;
            }
        }
        clickElement(By.cssSelector("div[class='payments-div'] input[value='Save']"));
        switchToDefaultContent();
        sleep(3);
        clickElementByRefreshing(By.cssSelector("div.sweet-alert button.confirm"));
        int i=0;
        while (checkElementDisplayed(By.cssSelector("div.sweet-alert[data-has-done-function='false'] button.confirm"))&&(i<3)){ // added by nagesh to avoid element click intercepted exceptionclickElementWithJs(By.cssSelector("div.sweet-alert[data-has-done-function='false'] button.confirm"));
            i++;
        }
    }
    public static void displayBothInvoicePayments() {
        clickElement(By.xpath("//a[text()='Payment plugin configuration']"));
        selectPayment("Global configuration");

        if(!(waitForElementVisible(By.xpath("//span[text()=' Payment by invoice']"),1,"")
                && waitForElementVisible(By.xpath("//span[text()=' Prepayment']"),1,""))){
            List<WebElement> payments = getElements(By.xpath("//div[@id='global_config__display_invoice_chosen']//span"));
            for (int i = 0; i < payments.size(); i++) {
                clearTextWithAction(By.xpath("//div[@id='global_config__display_invoice_chosen']"));
            }
            for(int i=0;i<=1;i++){
                clickElement(By.cssSelector("#global_config__display_invoice_chosen"));
                setTextWithAction(By.cssSelector("#global_config__display_invoice_chosen"), Keys.ENTER);
            }
        }
        clickElement(By.cssSelector("div[class='payments-div'] input[value='Save']"));
        switchToDefaultContent();
        sleep(2);
        clickElementByRefreshing(By.cssSelector("div.sweet-alert button.confirm"));
        sleep(1);
        clickElementByRefreshing(By.cssSelector("div.sweet-alert[data-has-done-function='false'] button.confirm"));
    }
}
