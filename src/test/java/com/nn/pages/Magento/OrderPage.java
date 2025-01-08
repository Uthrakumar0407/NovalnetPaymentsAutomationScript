package com.nn.pages.Magento;

import com.nn.Magento.Constants;
import com.nn.apis.MagentoAPI_Helper;
import com.nn.apis.TID_Helper;
import com.nn.callback.CallbackProperties;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.nn.callback.CallbackProperties.TID_STATUS_CONFIRMED;
import static com.nn.utilities.DriverActions.*;
import static com.nn.Magento.Constants.*;

public class OrderPage {

    private By captureBtn = By.cssSelector("#order_invoice");
    private By novaCaptureBtn = By.cssSelector("#novalnet_confirm");
    private By cancelBtn = By.cssSelector("#void_payment");
    private By cancelConfirmBtn = By.cssSelector(".action-primary.action-accept");
    private By captureConfirmBtn = By.cssSelector("[data-ui-id='order-items-submit-button']");
    private By invoiceMenu = By.cssSelector("a#sales_order_view_tabs_order_invoices");
    private By invoiceList = By.cssSelector("div#sales_order_view_tabs_order_invoices_content table tbody tr");

    private By creditMemoMenu = By.cssSelector("a#sales_order_view_tabs_order_creditmemos");
    private By creditMemoList = By.cssSelector("div#sales_order_view_tabs_order_creditmemos_content table tbody tr");
    private By novalnetComments = By.cssSelector("div.order-payment-method-title");
    private By orderStatus = By.cssSelector("#order_status");

    private By creditMemoBtn = By.cssSelector("#credit-memo");
    private By refundSubmitBtn = By.cssSelector("[data-ui-id='order-items-submit-button']");

    private By instalmentMenu = By.cssSelector("a#sales_order_view_tabs_order_novalnet_instalment");
    private By instalmentTable = By.cssSelector("#container table.paymnet-summary-information-table");

    private By zeroAmountBookingMenu = By.cssSelector("a#sales_order_view_tabs_order_novalnet_zeroamountbooking");
    private By zeroAmountBookingAmount = By.cssSelector("[ aria-hidden='false'] input[name='nn-amount-to-update']");
    private By zeroAmountBookingBookBtn = By.cssSelector("[ aria-hidden='false'] button");

    public void load(){
       openURL(SHOP_BACK_END_URL+"sales/order/");
        if(waitForElementVisible(By.cssSelector("input[id='username']"),5,"Waiting for admin login")){
            setTextWithoutClear(By.cssSelector("input[id='username']"),SHOP_BACKEND_USERNAME);
            setTextWithoutClear( By.cssSelector("#login"),SHOP_BACKEND_PASSWORD);
            clickElementWithJs( By.xpath("//span[text()='Sign in']"));
            waitForElementVisible( By.cssSelector("li.item-stores > a"));
            if(waitForElementVisible(By.cssSelector("button.action-close"),3,"")){
                clickElementWithJs(By.cssSelector("button.action-close"));
                sleep(1);
            }
            openURL(SHOP_BACK_END_URL+"sales/order/");
        }
        waitForTitleContains("Orders");
        waitForElementVisible(By.cssSelector("div#container div[data-role='grid-wrapper']>table"));
    }

    public void searchOrderByOrderNumber(String orderNumber){
        if(waitForElementVisible(By.cssSelector("button.action-remove"),5,"")){
            clickElementByRefreshing(By.cssSelector("button.action-remove"));
            sleep(1);
        }
        handleStaleElement(By.xpath("(//input[@class='admin__control-text data-grid-search-control'])[1]"),
                d->d.findElement(By.xpath("(//input[@class='admin__control-text data-grid-search-control'])[1]")).sendKeys(orderNumber, Keys.ENTER));
        waitForElementVisible(By.cssSelector(".admin__current-filters-list-wrap"));
        var xpath = "//div[@data-role='grid-wrapper']/table/tbody/tr/td[2]/div[text()='"+orderNumber+"']";
        clickElementByRefreshing(By.xpath(xpath));
       }

    public void searchOrderByOrderID(String orderNumber) {
        openURL(SHOP_BACK_END_URL+"sales/order/view/order_id/"+orderNumber+"/");
        if(waitForElementVisible(By.cssSelector("input[id='username']"),5,"Waiting for admin login")){
            setTextWithoutClear(By.cssSelector("input[id='username']"),SHOP_BACKEND_USERNAME);
            setTextWithoutClear( By.cssSelector("#login"),SHOP_BACKEND_PASSWORD);
            clickElementWithJs( By.xpath("//span[text()='Sign in']"));
            waitForElementVisible( By.cssSelector("li.item-stores > a"));
            if(waitForElementVisible(By.cssSelector("button.action-close"),3,"")){
                clickElementWithJs(By.cssSelector("button.action-close"));
                sleep(1);
            }
            openURL(SHOP_BACK_END_URL+"sales/order/view/order_id/"+orderNumber+"/");
        }
        waitForElementVisible(By.cssSelector("#anchor-content div#sales_order_view_tabs"));
    }

    @Step("verify invoice created")
    public void verifyInvoiceCreated(boolean expected){
        clickElement(invoiceMenu);
        sleep(2);
        waitForElementAttributeToChange(By.cssSelector("[data-component='sales_order_view_invoice_grid.sales_order_view_invoice_grid.sales_order_invoice_columns']"),
                "style","display: none;");
        boolean actual = getElement(invoiceList)
                    .findElements(By.cssSelector("td>div"))
                    .stream()
                    .map(WebElement::getText)
                    .anyMatch(s-> s.contains("Paid") || s.contains("Pending"));
        verifyEquals(actual,expected,"Verify Invoice created: ");
    }

    @Step("verify Credit Memo created")
    public void verifyCreditMemoCreated(boolean expected){
        clickElement(creditMemoMenu);
        sleep(2);
        waitForElementAttributeToChange(By.cssSelector("[data-component='sales_order_view_creditmemo_grid.sales_order_view_creditmemo_grid.sales_order_creditmemo_columns']"),
                "style","display: none;");
        boolean actual = getElement(creditMemoList)
                    .findElements(By.cssSelector("td>div"))
                    .stream()
                    .map(WebElement::getText)
                    .anyMatch(s-> s.contains("Refunded"));
        verifyEquals(actual,expected,"Verify Credit Memo created: ");
    }

    @Step("Capture transaction via shop backend")
    public void captureOrder(){
        if(checkElementDisplayed(novaCaptureBtn)){
            clickElement(novaCaptureBtn);
            clickElementByRefreshing(cancelConfirmBtn);
        }else{
            clickElement(captureBtn);
            clickElement(captureConfirmBtn);
        }
        waitForElementVisible(By.cssSelector("#messages .message-success"),30);
    }

    @Step("Capture transaction via shop backend")
    public void cancelOrder(){
        clickElement(cancelBtn);
        clickElementByRefreshing(cancelConfirmBtn);
        waitForElementVisible(By.cssSelector("#messages .message-success"),30);
    }

    @Step("Verify Novalnet Comments {0}")
    public void verifyNovalnetComments(String expected){
        String actual = Arrays.stream(getElementAttributeText(novalnetComments,"innerHTML")
                        .replaceAll("<!--.*?-->", "")
                        .split("<br>"))
                        .filter(s->!s.isBlank())
                        .filter(s->!s.contains("</"))
                        .map(String::trim)
                        .reduce((s1,s2)->s1+s2)
                        .get().replaceAll("\\s{2,}", " ");

        verifyContains(actual,expected,"Verify Novalnet payment comments exist: ");
    }

    @Step("Verify Novalnet Comments {0}")
    public void verifyNovalnetComments2(String expected){
        String actual = Arrays.stream(getElementAttributeText(novalnetComments,"innerHTML")
                        .replaceAll("<!--.*?-->", "")
                        .split("<br>"))
                        .filter(s->!s.isBlank())
                        .filter(s->!s.contains("</"))
                        .map(String::trim)
                        .reduce((s1,s2)->s1+s2)
                        .get().replaceAll("\\s{2,}", " ");

        verifyContains(expected,actual,"Verify Novalnet payment comments exist: ");
    }


    @Step("Get Novalnet Order Comments ")
    public String getOrderNovalnetComments(){
        String orderComments = getElementAttributeText(novalnetComments,"innerHTML");
        return orderComments;
    }

    public String getOrderID() {
        String orderURL = DriverActions.getURL();
        // Use regular expressions to extract the order ID from the URL
        Pattern pattern = Pattern.compile("order_id/(\\d+)/");
        Matcher matcher = pattern.matcher(orderURL);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    @Step("Verify novalnet comments {0} with amount {1}")
    public void verifyNovalnetComments(String expected, String amount){
        String actual = Arrays.stream(getElementAttributeText(novalnetComments,"innerHTML").split("<br>"))
                .filter(s->!s.isBlank())
                .filter(s->!s.contains("</"))
                .map(String::trim)
                .filter(s->s.contains(expected) && s.contains(MagentoAPI_Helper.changeAmountFormatMagento(amount)))
                .findFirst().get();
        verifyContains(actual,expected,"Verify Novalnet payment comments exist: ");
    }

    @Step("Verify novalnet comments {0} with amount {1} and due date {}")
    public void verifyNovalnetComments(String expected, String amount, String dueDate){
        DriverActions.reloadPage();
        Date date = DriverActions.getDateFromString("yyyy-MM-dd",dueDate);
        String updateDate1 = DriverActions.changePatternOfDate("MMMM d, yyyy",date);
        String updateDate2 = DriverActions.changePatternOfDate("MMMM d yyyy",date);
        String updateDate3 = DriverActions.changePatternOfDate("d MMMM yyyy",date);
        System.out.println("Updated due date "+updateDate1);
        String actual = Arrays.stream(getElementAttributeText(novalnetComments,"innerHTML").split("<br>"))
                .filter(s->!s.isBlank())
                .filter(s->!s.contains("</"))
                .map(String::trim)
                .filter(s->s.contains(expected) && s.contains(MagentoAPI_Helper.changeAmountFormatMagento(amount)) &&
                        (s.contains(updateDate1) || s.contains(updateDate2) || s.contains(updateDate3)))
                .findFirst().orElse(null);
        verifyContains(actual,expected,"Verify Novalnet payment comments exist: ");
    }

    @Step("Get new TID")
    public String getNewTID(String expected, String amount){
        String newTID = null;
        Optional<String> actual = Arrays.stream(getElementAttributeText(novalnetComments,"innerHTML").split("<br>"))
                .filter(s->!s.isBlank())
                .filter(s->!s.contains("</"))
                .map(String::trim)
                .filter(s->s.contains(expected) && s.contains(MagentoAPI_Helper.changeAmountFormatMagento(amount)))
                .map(s-> getFirstMatchRegex(s,"New TID:(\\d+)"))
                //.filter(Objects::nonNull)
                .findFirst();
        if(actual.isPresent()){
            newTID = actual.get().replaceAll("[^0-9]","");
        }
        return newTID;
    }

    @Step("Verify order history page status in shop backend")
    public void verifyOrderHistoryPageStatus(String expected){
        String actual = getElementText(orderStatus);
        verifyEquals(actual,expected,"Verify order history page status: ");
    }

    @Step("Refund via shop backend for the amount {0}")
    public void refundOrder(int refundAmount){
        clickElement(invoiceMenu);
        clickElementByRefreshing(invoiceList);
        //handleStaleElement(d-> clickElement(invoiceList));
        clickElement(creditMemoBtn);
        //String totalAmount = getElementText(By.cssSelector("td strong>.price")).trim().replaceAll("[^0-9]","");
        //int refund = Integer.parseInt(totalAmount)-refundAmount;
        clearText(By.cssSelector(".col-refund.col-qty>input"));
        clickOutsideForm();
        clickElement(By.cssSelector("[data-ui-id='order-items-update-button']"));
        scrollToElement(By.cssSelector("#adjustment_positive"));
        clickElementByRefreshing(By.cssSelector("#shipping_amount"));
        clearText(By.cssSelector("#shipping_amount"));
        clickElementByRefreshing(By.cssSelector("#adjustment_positive"));
        setTextWithAction(By.cssSelector("#adjustment_positive"), MagentoAPI_Helper.changeAmountFormatMagento(String.valueOf(refundAmount)));
        clickOutsideForm();
        clickElementByRefreshing(By.cssSelector("[data-ui-id='update-totals-button']"));
        clickElementByRefreshing(refundSubmitBtn);
        waitForElementVisible(By.cssSelector("#messages .message-success"),30);
    }

    @Step("Refund via shop backend")
    public void refundOrder(){
        clickElement(invoiceMenu);
        clickElementByRefreshing(invoiceList);
        clickElement(creditMemoBtn);
        clickElementWithJs(refundSubmitBtn);
        waitForElementVisible(By.cssSelector("#messages .message-success"),30);
    }

    @Step("Open instalment menu")
    public void openInstalmentMenu(){
        clickElement(instalmentMenu);
        waitForElementVisible(instalmentTable);
    }

    @Step("Verify Instalment table displayed expected {0}")
    public void verifyInstalmentTableDisplayed(boolean expected){
        verifyEquals(checkElementDisplayed(instalmentMenu),expected,"Verify Instalment table displayed");
        verifyEquals(checkElementDisplayed(instalmentTable),expected,"Verify Instalment table displayed");
    }

    @Step("Verify Instalment table values in the order history page")
    public void verifyInstalmentTable(String tid, int numberOfCycles, String cycleAmount){
        openInstalmentMenu();
        Map<String,String> cycleDates = getUpcomingMonthDates(numberOfCycles,"yyyy-MM-dd");
        System.out.println(cycleDates);
        boolean cycleDatesExist = getElement(instalmentTable).findElements(By.cssSelector("tr td:nth-of-type(4)"))
                .stream()
                .skip(1)
                .map(e-> e.getText().trim())
                .filter(s-> !s.contains("-"))
                .allMatch(cycleDates::containsValue);
        verifyEquals(cycleDatesExist,true,"Verify Instalment cycle dates present in the table");
        verifyInstalmentTableRefundButtonPresent(1);
        verifyInstalmentCancelButtonDisplayed(true);
        verifyInstalmentTablePaidStatus(1);
        verifyInstalmentTableTIDPresent(1,tid);
        verifyInstalmentInformation(cycleAmount,1,numberOfCycles-1,cycleDates.get("2"));
    }

    @Step("Verify instalment cancel button displayed expected {0}")
    public void verifyInstalmentCancelButtonDisplayed(boolean expected){
        verifyEquals(checkElementDisplayed(By.xpath("(//button[@value='CANCEL_ALL_CYCLES'])[1]")),expected,"Verify Instalment cancel button displayed");
    }

    @Step("Execute instalment cancel via shop backend")
    public void instalmentCancel(String cycleAmount, String paymentType){
        openInstalmentMenu();
        clickElement(By.cssSelector("#container input[value='Instalment cancel']"));
        verifyNovalnetComments(CallbackProperties.REFUND_COMMENT_,cycleAmount);
        var tid = getNewTID(CallbackProperties.REFUND_COMMENT_,cycleAmount);
        TID_Helper.verifyTIDInformation(tid,cycleAmount,TID_STATUS_CONFIRMED,paymentType);
    }

    @Step("Verify instalment tid displayed for the cycle {0} and tid {1}")
    public void verifyInstalmentTableTIDPresent(int whichCycle, String tid){
        String xpath = "//table[@class='admin__table-secondary paymnet-summary-information-table']//tr["+(whichCycle+1)+"]/td[2]";
        verifyEquals(getElementText(By.xpath(xpath)),tid,"Verify Instalment recurring tid "+tid+" displayed for the cycle "+whichCycle );
    }

    @Step("Verify instalment refund button displayed for the cycle {0}")
    public void verifyInstalmentTableRefundButtonPresent(int whichCycle){
        String css = "#container table.paymnet-summary-information-table tr:nth-of-type("+(whichCycle+1)+") td a[id^='nn-refund']";
        verifyEquals(checkElementDisplayed(By.cssSelector(css)),true,"Verify Instalment recurring refund button displayed for the cycle "+whichCycle );
    }

    @Step("Proceed instalment refund in the instalment table")
    public void proceedInstalmentRefund(String amount, String paymentName){
        openInstalmentMenu();
        clickElement(By.cssSelector("#container table.paymnet-summary-information-table tr:nth-of-type(2) td a[id^='nn-refund']"));
        if(!getInputFieldText(By.cssSelector("#container table.paymnet-summary-information-table tr:nth-of-type(2) td input[name='nn-refund-amount']")).equals(amount))
            setText(By.cssSelector("#container table.paymnet-summary-information-table tr:nth-of-type(2) td input[name='nn-refund-amount']"),amount);
        clickElement(By.cssSelector("#container table.paymnet-summary-information-table tr:nth-of-type(2) td input[value='Refund']"));
        var tid = getNewTID(CallbackProperties.REFUND_COMMENT_,amount);
        TID_Helper.verifyTIDInformation(tid,amount,TID_STATUS_CONFIRMED,paymentName);
    }

    @Step("Verify instalment cycle paid status for the cycle {0}")
    public void verifyInstalmentTablePaidStatus(int whichCycle){
        String css = "#container table.paymnet-summary-information-table tr:nth-of-type("+(whichCycle+1)+") td:nth-of-type(5) b";
        verifyEquals(checkElementDisplayed(By.cssSelector(css)),true,"Verify Instalment cycle paid status for the cycle "+whichCycle );
    }

    @Step("Verify Instalment information in the order page total amount {0}, cycle amount {1}, paid cycles {2}, remaining cycles {3}, next cycle date {4}")
    public void verifyInstalmentInformation(String cycleAmount, int paidCycles, int remainingCycles, String nextCycleDate){
        By table = By.cssSelector("div[id^='ui-id'] table.order-account-information-table");
        List<WebElement> rows = getElement(table).findElements(By.cssSelector("tr>td>b"));
        verifyEquals(rows.get(0).getText().replaceAll("[^0-9]", ""),String.valueOf(Integer.parseInt(cycleAmount)*paidCycles),"Verify total amount matched in instalment information");
        verifyEquals(rows.get(1).getText().replaceAll("[^0-9]", ""),cycleAmount,"Verify cycle amount matched in instalment information");
        verifyEquals(Integer.parseInt(rows.get(2).getText()),paidCycles,"Verify paid cycles matched in instalment information");
        verifyEquals(Integer.parseInt(rows.get(3).getText()),remainingCycles,"Verify remaining cycles matched in instalment information");
        verifyEquals(rows.get(4).getText(),nextCycleDate,"Verify instalment next cycle date matched in instalment information");
    }

    @Step("Verify Instalment information in the order page paid cycles {2}, remaining cycles {3}, next cycle date {4}")
    public void verifyInstalmentInformation(String cycleAmount, int paidCycles, int remainingCycles){
        By table = By.cssSelector("div[id^='ui-id'] table.order-account-information-table");
        List<WebElement> rows = getElement(table).findElements(By.cssSelector("tr>td>b"));
        verifyEquals(rows.get(0).getText().replaceAll("[^0-9]", ""),String.valueOf(Integer.parseInt(cycleAmount)*paidCycles),"Verify total amount matched in instalment information");
        verifyEquals(rows.get(1).getText().replaceAll("[^0-9]", ""),cycleAmount,"Verify cycle amount matched in instalment information");
        verifyEquals(Integer.parseInt(rows.get(2).getText()),paidCycles,"Verify paid cycles matched in instalment information");
        verifyEquals(Integer.parseInt(rows.get(3).getText()),remainingCycles,"Verify remaining cycles matched in instalment information");
    }

    @Step("Verify Instalment information in the order page paid cycles {2}, remaining cycles {3}")
    public void verifyInstalmentInformation(int paidCycles, int remainingCycles){
        By table = By.cssSelector("div[id^='ui-id'] table.order-account-information-table");
        List<WebElement> rows = getElement(table).findElements(By.cssSelector("tr>td>b"));
        verifyEquals(Integer.parseInt(rows.get(2).getText()),paidCycles,"Verify paid cycles matched in instalment information");
        verifyEquals(Integer.parseInt(rows.get(3).getText()),remainingCycles,"Verify remaining cycles matched in instalment information");
    }

    public void bookTransactionForZeroAmountBooking(String amount){
        clickElement(zeroAmountBookingMenu);
        if(!getInputFieldText(zeroAmountBookingAmount).equals(amount))
            setText(zeroAmountBookingAmount,amount);
        clickElement(zeroAmountBookingBookBtn);
        waitForElementVisible(By.cssSelector("#messages .message-success"),30);
    }

    public void bookTransactionForZeroAmountBookingFailureOrder(){
        clickElement(zeroAmountBookingMenu);
        clickElement(zeroAmountBookingBookBtn);
        alertAccept();
        waitForElementVisible(By.cssSelector("#messages .message-error>div"),30);
        String errorMsg = getElementText(By.cssSelector("#messages .message-error>div")).trim();
        verifyContains(errorMsg,"referenced transaction was not successful","Zero amount booking for communication break transaction");
    }

    public void verifyZeroAmountBooking(String amount,String payment){
        var tid = getZeroAmountBookedNewTID(CallbackProperties.ZERO_AMOUNT_BOOKING_CONFIRMATION, amount);
        TID_Helper.verifyTIDInformation(tid,amount,TID_STATUS_CONFIRMED,payment);
        verifyInvoiceCreated(true);
    }

    @Step("Get zero amount booked TID")
    public String getZeroAmountBookedNewTID(String expected, String amount){
        String newTID = null;
        Optional<String> actual = Arrays.stream(getElementAttributeText(novalnetComments,"innerHTML").split("<br>"))
                .filter(s->!s.isBlank())
                .filter(s->!s.contains("</"))
                .map(String::trim)
                .filter(s->s.contains(expected) && s.contains(MagentoAPI_Helper.changeAmountFormatMagento(amount)))
                .map(s-> getFirstMatchRegex(s,"Your new TID for the booked amount: (\\d+)"))
                .filter(match->match != null)
                .findFirst();
        if(actual.isPresent()){
            newTID = actual.get().replaceAll("[^0-9]","");
        }
        return newTID;
    }

}
