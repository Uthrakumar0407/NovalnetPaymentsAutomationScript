package com.nn.pages.Magento;

import com.aventstack.extentreports.Status;
import com.nn.Magento.Constants;
import com.nn.drivers.DriverManager;
import com.nn.pages.AdminPage;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static com.nn.Magento.Constants.PROJECT_ID;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.constants.Constants.*;
import static com.nn.constants.Constants.NOVALNET_ACCESSKEY;
import static com.nn.utilities.DriverActions.*;

public class NovalnetAdminPortal {
    private By userName = By.cssSelector("input[id='login_username']");
    private By password = By.cssSelector("input[id='login_password']");
    private By captcha = By.cssSelector("input[id='captcha']");
    private By loginBtn = By.cssSelector("button[id='login_button']");

    private By dashBoardExpan = By.cssSelector("div.sidebar > div.logo-details > i.fa.fa-bars#btn");

    private By projects = By.cssSelector("a[href='/products/own'] > i.fa.fa-sitemap");
    private By searchMenu = By.cssSelector("a[href='/searchbeta'] > i.fa.fa-search");
    private By emailSearchInput = By.cssSelector("#email");
    private By searchBtn = By.cssSelector("#searchbeta_page_btn_submit");
    private By searchResults = By.cssSelector(".table.search_table");
    private By projectsOwnPage = By.cssSelector("a[href='/products/own'] > i.fa.fa-sitemap");

   // private By automationProject = By.cssSelector("");

    private By automtionProjectDropdown = By.cssSelector("a[href='/products/own'].activits-item");

    private By apiCred = By.cssSelector("li[class=''] > a[rel='view4']");

    private By paymentMethods = By.cssSelector("li[class=''] > a[rel='view2']");

    private By novalnetGlobalLoader = By.cssSelector("td.clipboard.f_td-3");

    private By paymentAccessKey = By.xpath("(//div[@id='view4']//tr[@class='tr-3']/td[@class='clipboard f_td-3'])[1]");

    private By paymentApiKey = By.xpath("(//div[@id='view4']//tr[@class='tr-3']/td[@class='clipboard f_td-3'])[2]");

    private By projectID = By.xpath("(//div[@id='view4']//tr[@class='tr-2']/td[2])[1]");

    private By projectTariffID = By.xpath("//tr[@class='tr-2']/td[@class='f_td-3']//tr[1]");

    private By subsTariffID = By.xpath("//tr[@class='tr-2']/td[@class='f_td-3']//tr[2]");
    //String prodId = "9339";
    String projectUpdateMsg="Project Updated";
   private String automationProject = "tr.tr-3.tablemousemovebackground.cursorpointer[onclick=\"document.location.href='/products/view/id/";
    private By paymentMethodElements = By.xpath("//td//span[@class='bold']");

    private By editPaymentButton= By.xpath("//a[@id='edit_payment_methods_modules']");

    private By updatePayment= By.xpath("//input[@id='edit_payment_methods_update_span_button']");

    private By projectUpdate= By.xpath("//span[@id='product_creation_success_notify_title']");
    private By closeButton= By.xpath("//div[@class='overlay_close_btn1']//a[@class='closeLink']");

    private By backToProject= By.xpath(" //a[@id='edit_product_overview']");



    @Step("Load Novalnet Admin Portal")
    private void load() {
        DriverManager.getDriver().get(Constants.ADMIN_URL);
       // waitForTitleContains("Welcome to Admin Portal");

    }

    @Step("Open Novalnet Admin Dashbaord")
    public void openNovalnetAdminPortal() {
       load();
       login("KG06","Kumaravel_06","1234");
        }


    @Step("Login to Novalnet Admin Portal")
    private void login(String user, String pass, String securityCode) {
         if(waitForElementVisible(userName,5,"")){
            setTextWithoutClear(userName,user);
            waitForElementClickable(password,2);
            setTextWithoutClear(password,pass);
            setText(captcha,securityCode);
            clickElementWithJs(loginBtn);
            //waitForTitleContains("Welcome to Admin Portal", 60);
        }

         waitForElementPresent(By.xpath("//img[@src='/img/english-flag.png']/parent::a"));
         if(getElement(By.xpath("//img[@src='/img/english-flag.png']/parent::a")).getAttribute("class").isEmpty()) {
             Log.info("Changing language");
             clickElementWithJs(By.xpath("//img[@src='/img/english-flag.png']"));
         }
   }

   /* @Step("Load Automation Project")
    public void skipGerman() {
        waitForElementVisible(By.cssSelector("#btn"));
        clickElementWithJs(By.cssSelector("#btn"));
        clickElementWithJs(By.xpath("(//*[text()='Projects'])[1]"));
        waitForElementVisible(projectsOwnPage,60);
        By automationProjectElement = By.cssSelector(automationProject + PROJECT_ID + "'\"]");
        clickElementWithJs(automationProjectElement);
        waitForElementVisible(automtionProjectDropdown,60);

    }*/

    @Step("Load Automation Project")
    public void loadAutomationProject() {
        waitForElementVisible(By.cssSelector("#btn"));
        clickElementWithJs(By.cssSelector("#btn"));
        clickElementWithJs(By.xpath("(//*[text()='Projects'])[1]"));
        waitForElementVisible(projectsOwnPage,60);
        By automationProjectElement = By.cssSelector(automationProject + PROJECT_ID + "'\"]");
        clickElementWithJs(automationProjectElement);
        waitForElementVisible(automtionProjectDropdown,60);

    }


    @Step("Open Novalnet Admin Global Config")
    public void openNovalnetGlobalConfig() {
        clickElementWithJs(apiCred);
        waitForPageLoad();
        //waitForElementVisible(novalnetGlobalLoader);
    }

    @Step("Verify Novalnet Admin Global Config")
    public void verifyAdminGlobalConfig() {
        String apiKey = getElementText(paymentApiKey);
        String accessKey = getElementText(paymentAccessKey);
        String tariffID = getElementText(projectTariffID);
        String tariffValue =tariffID.replaceAll("\\D+", "");

         if (apiKey.equals(NOVALNET_API_KEY) || accessKey.equals(NOVALNET_ACCESSKEY) || tariffID.equals(NOVALNET_TARIFF) ) {
            verifyEquals(tariffValue, NOVALNET_TARIFF, "<b>Novalnet Tariff ID:</b>");
            verifyEquals(apiKey, NOVALNET_API_KEY, "<b>Novalnet API Key:</b>");
            verifyEquals(accessKey, NOVALNET_ACCESSKEY, "<b>Novalnet Payment Access Key:</b>");
        }
        ExtentTestManager.addScreenShot(Status.PASS, "<b>Verification of Novalnet Global Configuration settings in Admin Portal</b>");

    }


    @Step("Open Payment Methods Form")
    public void openPaymentMethods() {
        clickElementWithJs(paymentMethods);
        waitForPageLoad();
        waitForElementVisible(paymentMethodElements);
    }

    @Step("Get Payment Enable Status")
    public boolean getPaymentStatus(String paymentType) {
        By payment = getPaymentElement(paymentType);
        if (payment != null) {
        if(getElement(payment).getAttribute("checked")!=null) return true;
        }
        return false;
    }

    @Step("Get Payment Enable Status")
    public boolean enablePayment(String paymentType) {
        By payment = getPaymentElement(paymentType);
        if(!(getPaymentStatus(paymentType))){
            clickElementWithJs(editPaymentButton);
            clickElementWithAction(getElement(payment));
            clickElementWithJs(updatePayment);
            waitForElementVisible(projectUpdate);
            if(getElement(projectUpdate).getText().equals(projectUpdateMsg)){
                clickElementWithJs(closeButton);
                waitForElementVisible(backToProject);
                return true;
            }

        }
        return false;
    }


    @Step("Get Payment Enable Status")
    public By getPaymentElement(String paymentType) {
        switch (paymentType) {
            case PAYPAL:
                return By.xpath("//td//span[@class='bold' and contains(text(),'PayPal')] /../following-sibling::td//input");
            case CREDITCARD:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Credit Card')] /../following-sibling::td//input");
            case DIRECT_DEBIT_SEPA:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Direct Debit SEPA')] /../following-sibling::td//input");
            case GUARANTEED_INVOICE:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Invoice with payment guarantee')] /../following-sibling::td//input");
            case GUARANTEED_DIRECT_DEBIT_SEPA:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Direct debit SEPA with payment guarantee')] /../following-sibling::td//input");
            case INSTALMENT_INVOICE:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Instalment by invoice')] /../following-sibling::td//input");
            case INSTALMENT_DIRECT_DEBIT_SEPA:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Instalment by SEPA direct debit')] /../following-sibling::td//input");
            case IDEAL:
                return By.xpath("//td//span[@class='bold' and contains(text(),'iDEAL')] /../following-sibling::td//input");
            case CASHPAYMENT:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Barzahlen/viacash')] /../following-sibling::td//input");
            case MULTIBANCO:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Multibanco')] /../following-sibling::td//input");
            case PREPAYMENT:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Prepayment')] /../following-sibling::td//input");
            case INVOICE:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Prepayment / Invoice')] /../following-sibling::td//input");
            case ONLINE_TRANSFER:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Sofort')] /../following-sibling::td//input");
            case ONLINE_BANK_TRANSFER:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Online bank transfer')] /../following-sibling::td//input");
            case GIROPAY:
                return By.xpath("//td//span[@class='bold' and contains(text(),'giropay')] /../following-sibling::td//input");
            case EPS:
                return By.xpath("//td//span[@class='bold' and contains(text(),'eps')] /../following-sibling::td//input");
            case BANCONTACT:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Bancontact')] /../following-sibling::td");
            case ALIPAY:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Alipay')] /../following-sibling::td");
            case POSTFINANCE_CARD:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Postfinance card')] /../following-sibling::td//input");
            case WECHATPAY:
                return By.xpath("//td//span[@class='bold' and contains(text(),'WeChat Pay')] /../following-sibling::td//input");
            case TRUSTLY:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Trustly')] /../following-sibling::td//input");
            case PRZELEWY24:
                return By.xpath("//td//span[@class='bold' and contains(text(),'Przelewy24')] /../following-sibling::td//input");
            default:
                throw new IllegalArgumentException("Invalid payment method: " + paymentType);
        }
    }

    //This function will get the tid in the first row of the table.
    //So please make sure you are using a new email to place order
    @Step("Get TID from Admin portal")
    public String getTID(String email){
        //css-> .table.search_table>tbody>tr:nth-of-type(1)>td:nth-of-type(4)
        //xpath-> //table[contains(@class,'search_table')]/tbody/tr[1]/td[4]
        clickElement(searchMenu);
        setText(emailSearchInput,email);
        clickElement(searchBtn);
        waitForElementVisible(searchResults,30);
        String tid = getElementText(By.xpath("//table[contains(@class,'search_table')]/tbody/tr[1]/td[4]"));
        ExtentTestManager.addScreenShot("tid list");
        return tid.trim();
    }

    @Step("Get TID from Admin portal")
    public String getTIDUsingOrderNumber(String orderNumber){
        clickElement(searchMenu);
        setText(By.cssSelector("#order_no"),orderNumber);
        clickElement(searchBtn);
        waitForElementVisible(searchResults,30);
        String tid = getElementText(By.xpath("//table[contains(@class,'search_table')]/tbody/tr[1]/td[4]"));
        ExtentTestManager.addScreenShot("tid list");
        return tid.trim();
    }

}
