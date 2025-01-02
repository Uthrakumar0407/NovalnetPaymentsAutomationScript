package com.nn.pages;

import com.nn.apis.TID_Helper;
import com.nn.callback.InvoiceCallbackEvents;
import com.nn.constants.Constants;
import com.nn.helpers.ExcelHelpers;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.nn.callback.CallbackProperties.*;
import static com.nn.callback.CallbackProperties.CAPTURE_COMMENT_;
import static com.nn.constants.Constants.*;
import static com.nn.utilities.DriverActions.*;
import static com.nn.utilities.DriverActions.verifyAssertEquals;

public class BasePage {

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
            .testData(ExcelHelpers.xlReadPaymentCredentials())
            .txnInfo(new HashMap<>())
            .build();

    @Step("Setting up the Novalnet API configuration")
    public void verifyGlobalConfiguration() {
        wooCommerce.getDashBoardPage().loadSettingsPage();
       // wooCommerce.getDashBoardPage().openSettingsPage();
        wooCommerce.getSettingsPage().openNovalnetGlobalConfig();
        wooCommerce.getSettingsPage().verifyGlobalConfig();
    }


    @Step("Navigate to checkout by adding product {0} to cart")
    public void navigateCheckout(String productName){
        wooCommerce.getCartPage().load();
        wooCommerce.getCartPage().clearCart();
        wooCommerce.getHomePage().openProductPage();
        wooCommerce.getProductPage().addProductToCartByName(new String[]{productName});
        wooCommerce.getHomePage().openCheckoutPage();
    }

    @Step("Verify the transaction id, order status, payment name and novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderNumber,String orderStaus, String paymentName, String paymentComments, String tid) {
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber,orderStaus);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStaus);
        if(!orderStaus.equals(FAILURE_ORDER_STATUS))
            wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(paymentComments,paymentName);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber,orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(paymentComments);
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(paymentComments);
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(tid, paymentName);
        if(orderStaus.equals(COMPLETION_ORDER_STATUS) || orderStaus.equals(PROCESSING_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, paymentName);
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, paymentName);
    }
    @Step("Verify the transaction id, order status, payment name and novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderNumber,String orderStaus, String paymentName, String paymentComments, String tid,boolean pending) {
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber,orderStaus);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStaus);
        if(!orderStaus.equals(FAILURE_ORDER_STATUS))
            wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(paymentComments,paymentName);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber,orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(paymentComments,pending);
        wooCommerce.getOrdersPage().verifyCustomerNotesComments(paymentComments);
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(tid, paymentName);
        if(orderStaus.equals(COMPLETION_ORDER_STATUS) || orderStaus.equals(PROCESSING_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, paymentName);
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, paymentName);
    }

    @Step("Verify the transaction id, order status, payment name and novalnet payment comments appended successfully")
    public void statusCommentsVerificationWithInstalmentTable(String orderNumber,String orderStaus, String paymentName, String paymentComments, String tid, int numberOfCycles) {
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber,orderStaus);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStaus);
        if(orderStaus.equals(COMPLETION_ORDER_STATUS))
            verifyInstalmentTable(numberOfCycles,tid);
        else
            verifyInstalmentTableExist(false);
        if(!orderStaus.equals(FAILURE_ORDER_STATUS) && paymentComments.contains(INITIAL_LEVEL_COMMENT_))
            wooCommerce.getMyAccountPage().verifyOrderHistoryPageDetails(paymentComments,paymentName);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber,orderStaus);
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        if(orderStaus.equals(COMPLETION_ORDER_STATUS)){
            verifyInstalmentTable(numberOfCycles,tid);
            verifyInstalmentCancelBtnDisplayed(true);
        }
        else
            verifyInstalmentTableExist(false);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(paymentComments);
        if(paymentComments.contains(INITIAL_LEVEL_COMMENT_))
            wooCommerce.getOrdersPage().verifyCustomerNotesComments(paymentComments);
        wooCommerce.getOrdersPage().verifyPaymentNameAndTID(tid, paymentName);
        if(orderStaus.equals(COMPLETION_ORDER_STATUS) || orderStaus.equals(PROCESSING_ORDER_STATUS))
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, paymentName);
        else
            wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(false, paymentName);
    }





    @Step("Verify the transaction due date and novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderNumber, String paymentComments, String dueDate) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsDueDate(paymentComments, dueDate);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyFollowupComments(paymentComments);
    }

    @Step("Verify the transaction novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderNumber, String paymentComments){
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(paymentComments);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyFollowupComments(paymentComments);
    }

    @Step("Verify the transaction amount, order status and  novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderNumber, String comment, String amount, String status) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber, status);
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(comment, amount);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber, status);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        if(!comment.equals(CREDIT_COMMENT_) && !comment.equals(CHARGEBACK_COMMENT_))
            wooCommerce.getMyAccountPage().verifyFollowupComments(comment);
    }

    @Step("Verify the transaction order status, payment name and novalnet payment comments appended successfully")
    public void statusCommentsVerification(String orderNumber, String comment, String paymentName, boolean paymentNameAfterPaid, String status){
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber, status);
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(comment);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(paymentNameAfterPaid, paymentName);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber,status);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupComments(comment);
    }


    @Step("Perform refund via admin amount {0} and expected status {1}")
    public void refundAdminForInvoiceType(int amount, String expectedStatus,String orderNumber,String tid) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        var total = Integer.parseInt(TID_Helper.getTIDAmount(tid));
        wooCommerce.getOrdersPage().initiateRefund(amount);
        if(total > amount){
            if(TID_Helper.getTIDPaymentType(tid).equals(DIRECT_DEBIT_SEPA))
                TID_Helper.verifyTIDInformation(tid, String.valueOf(total-amount), TID_STATUS_CONFIRMED);
            else
                TID_Helper.verifyTIDInformation(tid, String.valueOf(total-amount), TID_STATUS_PENDING);
        }else if(total == amount){
            if(TID_Helper.getTIDPaymentType(tid).equals(DIRECT_DEBIT_SEPA))
                TID_Helper.verifyTIDInformation(tid, "0", TID_STATUS_DEACTIVATED);
            else{
                sleep(15);
                TID_Helper.verifyTIDInformation(tid, String.valueOf(amount), TID_STATUS_DEACTIVATED);
            }
        }else{
            Assert.fail("Refund amount is higher than the order amount");
        }
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(amount));
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(expectedStatus);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber, expectedStatus);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber, expectedStatus);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(expectedStatus);
        wooCommerce.getMyAccountPage().verifyFollowupComments(REFUND_COMMENT_);
    }


    @Step("Perform refund via admin amount {0} and expected status {1}")
    public void refundAdmin(int amount, String expectedStatus,String orderNumber, String refundPaymentType) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        var refundedAmount =0;
        if(refundPaymentType.equals(INSTALMENT_INVOICE_BOOKBACK) || refundPaymentType.equals(INSTALMENT_SEPA_BOOKBACK)){
            refundedAmount = Integer.parseInt(getValueInInstamentTable(1,3).replaceAll("[^0-9]",""))-amount;
            wooCommerce.getOrdersPage().instalmentRefund(amount);
            if(refundedAmount == 0) {
                verifyInstalmentTableStatus(1,5,REFUND_ORDER_STATUS);
                verifyInstalmentRefundBtnDisplayed(false,1);
            }
            else{
                verifyInstalmentTableStatus(1,5,COMPLETION_ORDER_STATUS);
                verifyInstalmentRefundBtnDisplayed(true,1);
            }
            verifyInstalmentTableCycleAmount(1,3,refundedAmount);
        }else{
            wooCommerce.getOrdersPage().initiateRefund(amount);
        }
        var newTID = getFirstMatchRegex(getElementText(By.xpath("//*[contains(text(),'Refund has been initiated')]")), "(?:New TID:)\\s*(\\d{17})");
        TID_Helper.verifyTIDInformation(newTID.replaceAll("[^0-9]", ""), String.valueOf(amount), TID_STATUS_CONFIRMED, refundPaymentType);
        wooCommerce.getOrdersPage().verifyOrderNotesCommentsAmount(REFUND_COMMENT_, String.valueOf(amount));
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(expectedStatus);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber, expectedStatus);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber, expectedStatus);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(expectedStatus);
        if(refundPaymentType.equals(INSTALMENT_INVOICE_BOOKBACK) || refundPaymentType.equals(INSTALMENT_SEPA_BOOKBACK)){
            if(refundedAmount == 0)
                verifyInstalmentTableStatus(1,5,REFUND_ORDER_STATUS);
            else
                verifyInstalmentTableStatus(1,5,COMPLETION_ORDER_STATUS);
            verifyInstalmentTableCycleAmount(1,4,refundedAmount);
        }
        wooCommerce.getMyAccountPage().verifyFollowupComments(REFUND_COMMENT_);
    }

    @Step("Verify the transaction id, order status, payment name and novalnet payment comments appended successfully")
    public void statusCommentsVerificationForInstalmentRenewal(String orderNumber,String orderStaus, int cycle) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(orderStaus);
        var newTID = getFirstMatchRegex(getElementText(By.xpath("(//div[@class='note_content'])[1]")), "(?:new instalment transaction ID is:)\\s*(\\d{17})").replaceAll("[^0-9]","");
        verifyInstalmentTableStatus(cycle,5,COMPLETION_ORDER_STATUS);
        verifyInstalmentTableTID(cycle,4,newTID);
        verifyInstalmentRefundBtnDisplayed(true,cycle);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber,orderStaus);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(orderStaus);
        verifyInstalmentTableStatus(cycle,5,COMPLETION_ORDER_STATUS);
        verifyInstalmentTableTID(cycle,3,newTID);
    }

    @Step("Verify Instalment cancel comments appended successfully")
    public void statusCommentsVerificationInstalmentCancel(String orderNumber,int totalCycles, String cancelType) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        var cancelComment = "";
        if(cancelType.equals(ALL_CYCLES)){
            wooCommerce.getOrdersPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
            verifyInstalmentTableStatus(1,5,REFUND_ORDER_STATUS);
            verifyInstalmentRefundBtnDisplayed(false,1);
            cancelComment = wooCommerce.getOrdersPage().getOrderNoteComment(INSTALMENT_CANCEL_COMMENT);
        }
        else{
            verifyInstalmentTableStatus(1,5,COMPLETION_ORDER_STATUS);
            verifyInstalmentRefundBtnDisplayed(true,1);
            cancelComment = wooCommerce.getOrdersPage().getOrderNoteComment(INSTALMENT_CANCEL_REMAINING_COMMENT);
        }
        var numberOfCycles = totalCycles;
        while(numberOfCycles > 1){
            verifyInstalmentTableStatus(numberOfCycles--,5,CANCELLATION_ORDER_STATUS);
        }
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        if(cancelType.equals(ALL_CYCLES)){
            wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
            verifyInstalmentTableStatus(1,5,REFUND_ORDER_STATUS);
        }
        if(cancelComment.contains(INSTALMENT_CANCEL_REMAINING_COMMENT)){
            wooCommerce.getMyAccountPage().verifyFollowupComments(INSTALMENT_CANCEL_REMAINING_COMMENT);
            verifyInstalmentTableStatus(1,5,COMPLETION_ORDER_STATUS);
        }
        if(cancelComment.contains(INSTALMENT_CANCEL_COMMENT)){
            wooCommerce.getMyAccountPage().verifyFollowupComments(INSTALMENT_CANCEL_COMMENT);
        }
        numberOfCycles = totalCycles;
        while(numberOfCycles > 1){
            verifyInstalmentTableStatus(numberOfCycles--,5,CANCELLATION_ORDER_STATUS);
        }
    }

    @Step("Capture Transaction in the shop backend")
    public void captureTransaction(String orderNumber, String tid, String status, String paymentName, String tidStatus) {
        wooCommerce.getOrdersPage().selectOrderStatus(status);
        TID_Helper.verifyTIDStatus(tid, tidStatus);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CAPTURE_COMMENT_);
        wooCommerce.getOrdersPage().verifyPaymentNameAfterPaid(true, paymentName);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber, status);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber,status);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(status);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CAPTURE_COMMENT_);
    }


    @Step("Cancel Transaction in the shop backend")
    public void cancelTransaction(String orderNumber, String tid) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().selectOrderStatus(CANCELLATION_ORDER_STATUS);
        TID_Helper.verifyTIDStatus(tid, TID_STATUS_DEACTIVATED);
        wooCommerce.getOrdersPage().verifyOrderNotesComments(CANCEL_COMMENT_);
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().verifyOrderListingStatus(orderNumber, CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().loadOrders();
        wooCommerce.getMyAccountPage().verifyOrderListingStatus(orderNumber,CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().clickOrder(orderNumber);
        wooCommerce.getMyAccountPage().verifyOrderHistoryPageStatus(CANCELLATION_ORDER_STATUS);
        wooCommerce.getMyAccountPage().verifyFollowupComments(CANCEL_COMMENT_);
    }

    @Step("Cancel Instalment in the shop backend")
    public void cancelInstalment(String orderNumber, String cancelType) {
        wooCommerce.getOrdersPage().load();
        wooCommerce.getOrdersPage().selectBackendOrder(orderNumber);
        wooCommerce.getOrdersPage().cancelInstalment(cancelType);
    }

    @Step("Delete old payment tokens")
    public void deletePaymentTokens(){
        wooCommerce.getMyAccountPage().loadPaymentTokens();
        wooCommerce.getMyAccountPage().deletePaymentTokens();
    }

    @Step("Verify Instalment Dates, TID and Status appended in the Instalment table")
    public void verifyInstalmentTable(int numberOfCycles, String tid){
        Map<String,String> cycleDates = getUpcomingMonthDates(numberOfCycles,"MMMM dd, yyyy");
        By table = By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table");
        scrollToElement(table);
        WebElement tableEle = getElement(table);
        List<WebElement> headerEle = tableEle.findElements(By.cssSelector("thead th"));
        boolean cycleDateExist = true;
        WebElement tidEle = null;
        for(int i=0;i<headerEle.size();i++){
            if(headerEle.get(i).getText().contains("Date")){
                List<WebElement> actualDateEle = tableEle.findElements(By.cssSelector("tbody tr td:nth-of-type("+i+1+")"));
                for(int j=0;j<actualDateEle.size();j++){
                    if(!actualDateEle.get(j).getText().equals(cycleDates.get(String.valueOf(j+1)))){
                        ExtentTestManager.logMessage(actualDateEle.get(j).getText());
                        cycleDateExist = false;
                    }
                }
            }
            if(headerEle.get(i).getText().contains("Novalnet transaction ID")){
                tidEle = tableEle.findElement(By.cssSelector("tbody tr:nth-of-type(1) td:nth-of-type("+(i+1)+")"));
            }
        }
        verifyAssertEquals(cycleDateExist, true,"<b>Verify cycle dates appended in Instalment Table:</b>");
        verifyAssertEquals(tidEle.getText(), tid,"<b>Verify transaction ID appended in Instalment Table:</b>");
        WebElement statusEle = tableEle.findElement(By.cssSelector("tbody tr:nth-of-type(1) td:nth-of-type(5)"));
        verifyAssertEquals(statusEle.getText(), COMPLETION_ORDER_STATUS,"<b>Verify Order status appended in Instalment Table:</b>");
    }

    @Step("Verify Instalment table cycle {0} status {1}")
    public void verifyInstalmentTableStatus(int whichCycle,int col,String status){
        scrollToElement(By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table"));
        var value = getValueInInstamentTable(whichCycle,col);
        verifyAssertEquals(value, status,"<b>Verify Instalment Table cycle "+whichCycle+" status :</b>");
    }

    @Step("Verify Instalment table cycle {0} tid {1}")
    public void verifyInstalmentTableTID(int whichCycle,int col,String tid){
        var value = getValueInInstamentTable(whichCycle,col);
        verifyAssertEquals(value, tid,"<b>Verify Instalment Table cycle "+whichCycle+" tid :</b>");
    }

    @Step("Verify Instalment Table exist expected {0}")
    public void verifyInstalmentTableExist(boolean expected){
        var value = checkElementDisplayed(By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table"));
        verifyAssertEquals(value, expected,"<b>Verify Instalment Table exist :</b>");
    }

    @Step("Verify Instalment table cycle {0} amount {1}")
    public void verifyInstalmentTableCycleAmount(int whichCycle,int col,int amount){
        var value = getValueInInstamentTable(whichCycle,col);
        verifyAssertEquals(Integer.parseInt(value.replaceAll("[^0-9]","")), amount,"<b>Verify Instalment Table cycle "+whichCycle+" amount :</b>");
    }

    private String getValueInInstamentTable(int row,int col){
        return getValueInTable(By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table"),col,row);
    }

    public void verifyInstalmentRefundBtnDisplayed(boolean expected,int cycle){
        boolean displayed = checkElementDisplayed(
                By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table tbody tr:nth-of-type("+cycle+") td:nth-of-type(6) [id^='refund_link']"));
        verifyAssertEquals(displayed,expected,"Instalment refund button display status for the cycle "+cycle+" : ");
    }

    public void verifyInstalmentCancelBtnDisplayed(boolean expected){
        boolean displayed = checkElementDisplayed(
                By.cssSelector("#instalment_cancel"));
        verifyAssertEquals(displayed,expected,"Instalment cancel button display status ");
    }


    private boolean verifyInstalment(){
        return checkElementDisplayed(By.cssSelector("[class^='wc_novalnet_instalment_related_orders']>table"));
    }

    public void bookAndVerifyZeroAmountBooking(String orderAmount, String payment){
        wooCommerce.getOrdersPage().bookTransactionForZeroAmountBooking(orderAmount);
        wooCommerce.getOrdersPage().verifyZeroAmountBooking(orderAmount,payment);
    }

}
