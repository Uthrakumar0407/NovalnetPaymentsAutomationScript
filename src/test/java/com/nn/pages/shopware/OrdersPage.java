package com.nn.pages.shopware;

import static com.nn.utilities.DriverActions.*;

import com.nn.pages.shopware.base.ShopwareOrderStatus;
import com.nn.utilities.Log;
import com.nn.utilities.ShopwareUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.Map;

public class OrdersPage {

    private By orderMenu = By.cssSelector(".navigation-list-item__sw-order>span");
    private By orderMenuOverView = By.cssSelector(".navigation-list-item__sw-order li.sw-order-index");
    private By searchOrderInput = By.cssSelector(".sw-order-list input.sw-search-bar__input");
    private By RefundBtn = By.cssSelector("button.novalnet-payment-pay-action-toolbar__button");
    private By BookAmountBtn = By.cssSelector("button.novalnet-payment-pay-action-toolbar__button");
    private By captureBtn = By.cssSelector(".novalnet-payment-pay-action-toolbar__button:first-of-type");
    private By cancelBtn = By.cssSelector(".novalnet-payment-pay-action-toolbar__button:last-of-type");
    private By modelConfirmBtn = By.cssSelector(".sw-modal__footer>button");
    private By alertSuccess = By.xpath("//div[@class='sw-alert__title' and (contains(text(),'Success') or contains(text(),'Erfolg'))]");
    private By alertError = By.xpath("//div[@class='sw-alert__title' and (contains(text(),'Error') or contains(text(),'Fehler'))]/following-sibling::div/div");
    private By instalmentTable = By.cssSelector(".sw-data-grid__table");

    @Step("Open admin orders page")
    public OrdersPage openOrdersPage() {
        openURL(ShopwareUtils.SHOP_BACK_END_URL + "sw/dashboard/index");
        getElements(By.cssSelector(".sw-alert__close")).forEach(e -> {
            try {
                e.click();
            } catch (Exception ignored) {
            }
        });
        boolean isLoggedIn = setExpectedCondition(d -> d.getTitle().contains("Dashboard"), 10);
        if(!isLoggedIn){
            Log.info("Shop backend logged out at runtime");
            new AdminLoginPage().login();
        }
        while (!getElementAttributeText(orderMenu, "class").contains("router-link-active")) {
            clickElementWithJs(By.cssSelector("li.navigation-list-item__sw-order"));
            sleep(1);
            if (getElementAttributeText(orderMenu, "class").contains("router-link-active"))
                break;
        }
        clickElementByRefreshing(orderMenuOverView);
        waitForElementVisible(By.cssSelector(".sw-order-list .sw-page__smart-bar-amount"));
        return this;
    }

    private void searchOrder(String orderNumber) {
        handleStaleElement(searchOrderInput, d -> {
            WebElement e = d.findElement(searchOrderInput);
            if (!e.getAttribute("value").contains(orderNumber)) {
                e.sendKeys(orderNumber);
                clickOutsideForm();
            }
        });
        waitForElementDisable(By.cssSelector(".sw-order-list .sw-pagination"));
    }

    @Step("Open admin orders details page")
    public OrdersPage openOrderDetail(String orderNumber) {
        // table tr td.sw-data-grid__cell--orderNumber a
        String xpath = "//table//tr/td[contains(@class,'sw-data-grid__cell--orderNumber')]//a[contains(text(),'" + orderNumber + "')]";
        By order = By.xpath(xpath);
        if (!checkElementDisplayed(order))
            searchOrder(orderNumber);
        handleStaleElement(order, d -> d.findElement(order).click());
        waitForElementVisible(By.cssSelector(".sw-order-detail"));
        return this;
    }

    private void clickOrderDetailsTab() {
        By orderDetail = By.cssSelector(".sw-order-detail__tabs-tab-details");
        waitForElementVisible(orderDetail);
        if (!getElement(orderDetail).getAttribute("class").contains("sw-tabs-item--active")) {
            clickElement(orderDetail);
            waitForElementVisible(By.cssSelector(".novalnet-payment-buyer-notification-icon-container>img"), 120);
        }
        setExpectedCondition(d->!d.findElement(By.cssSelector(".novalnet-payment-amount-info-total-amount")).getText().trim().equals("0"),1,"Waiting for order detail page load");
    }

    @Step("Get admin order listing status for order {0}")
    public String getOrderListingStatus(String orderNumber) {
        String xpath = "//table//tr/td[contains(@class,'sw-data-grid__cell--orderNumber')]//a[contains(text(),'" + orderNumber + "')]/../../following-sibling::td[contains(@class,'transactions-last()')]";
        By status = By.xpath(xpath);
        if (!checkElementDisplayed(status))
            searchOrder(orderNumber);
        return getElementText(status);
    }

    @Step("Get order status in admin orders page")
    public String getOrderStatus() {
        waitForElementVisible(By.cssSelector("div[class*='order-state-payment'] div.sw-single-select__selection-text"), 60);
        return getElementText(By.cssSelector("div[class*='order-state-payment'] div.sw-single-select__selection-text"));
    }

    @Step("Get order status in novalnet tab inside admin orders page")
    public String getOrderStatusNovalnet() {
        clickOrderDetailsTab();
        return getElementText(By.xpath("//div[contains(text(),'Novalnet')]/ancestor::div[contains(@class,'sw-order-detail-state-card')]//div[contains(@class,'sw-single-select__selection-text')]"));
    }

    @Step("Get novalnet payment comments in admin orders page")
    public String getComments() {
        clickOrderDetailsTab();
        waitForElementVisible(By.cssSelector(".novalnet-payment-checkout-info-comments"), 60);
        scrollToElement(By.cssSelector(".novalnet-payment-checkout-info-comments"));
        System.out.println("----------------------------Order comments successfully fetched-----------------------------------------");
        return getElementText(By.cssSelector(".novalnet-payment-checkout-info-comments")).trim();
    }

    public String getNewTID(){
        return getFirstMatchRegex(getComments(),"New TID:(\\d+)").replaceAll("[^0-9]","").trim();
    }

    @Step("Check novalnet refund button displayed")
    public boolean isRefundBtnDisplayed() {
        clickOrderDetailsTab();
        waitForElementVisible(RefundBtn, 5, "");
        return checkElementDisplayed(RefundBtn);
    }

    public boolean isBookAmountBtnDisplayed() {
        clickOrderDetailsTab();
        waitForElementVisible(BookAmountBtn, 5, "");
        return checkElementDisplayed(BookAmountBtn);
    }

    @Step("Proceed refund")
    public void proceedRefund(String amount) {
        clickOrderDetailsTab();
        clickElement(RefundBtn);
        if (!getInputFieldText(By.xpath("//div[@label='Refund amount']/div[2]/input")).equals(amount))
            setText(By.xpath("//div[@label='Refund amount']/div[2]/input"), amount);
        clickElementByRefreshing(modelConfirmBtn);
        waitForElementVisible(alertSuccess);
        sleep(5);
        reloadPage();
    }

    @Step("Capture a transaction")
    public void capture() {
        clickOrderDetailsTab();
        clickElement(captureBtn);
        clickElement(modelConfirmBtn);
        waitForElementVisible(alertSuccess, 60);
        reloadPage();
    }

    @Step("Cancel a transaction")
    public void cancel() {
        clickOrderDetailsTab();
        clickElement(cancelBtn);
        clickElement(modelConfirmBtn);
        waitForElementVisible(alertSuccess, 100);
        reloadPage();
    }

    public boolean isCaptureAndCancelButtonDisplayed() {
        clickOrderDetailsTab();
        return waitForElementVisible(captureBtn,5,"") && waitForElementVisible(cancelBtn,3,"");
    }

    @Step("Cancel all remaining cycles")
    public void cancelAllRemainingCycles() {
        clickElement(By.cssSelector("button.novalnet-payment-pay-action-toolbar__button"));
        sleep(2); // cause both same locators so
        clickElement(By.cssSelector("button.novalnet-payment-pay-action-toolbar__button:last-of-type"));
        clickElement(modelConfirmBtn);
        waitForElementVisible(alertSuccess, 60);
        reloadPage();
    }

    @Step("Cancel all instalment cycles")
    public void cancelAllCycles() {
        clickElement(By.cssSelector("button.novalnet-payment-pay-action-toolbar__button"));
        sleep(2);
        clickElement(By.cssSelector("button.novalnet-payment-pay-action-toolbar__button:first-of-type"));
        clickElement(modelConfirmBtn);
        waitForElementVisible(alertSuccess, 60);
        reloadPage();
    }

    @Step("Proceed refund")
    public void proceedRefund() {
        clickOrderDetailsTab();
        clickElement(RefundBtn);
        clickElementByRefreshing(By.cssSelector(".sw-modal__footer>button"));
        waitForElementVisible(alertSuccess);
        reloadPage();
    }

    @Step("Book amount for zero amount booking with {0}")
    public void bookAmount(String amount) {
        clickOrderDetailsTab();
        clickElement(BookAmountBtn);
        if (!getInputFieldText(By.xpath("//div[@label='Transaction booking amount']/div[2]/input")).equals(amount)) {
            setText(By.xpath("//div[@label='Transaction booking amount']/div[2]/input"), amount);
        }
        clickElementByRefreshing(modelConfirmBtn);
        waitForElementVisible(alertSuccess);
        sleep(5);
        reloadPage();
    }

    @Step("Book amount for zero amount booking for {0}")
    public void bookAmountForCommunicationBreakSuccessOrder(String amount) {
        clickOrderDetailsTab();
        clickElement(BookAmountBtn);
        if (!getInputFieldText(By.xpath("//div[@label='Transaction booking amount']/div[2]/input")).equals(amount)) {
            setText(By.xpath("//div[@label='Transaction booking amount']/div[2]/input"), amount);
        }
        clickElementByRefreshing(modelConfirmBtn);
        waitForElementVisible(alertError);
        sleep(5);
        reloadPage();
    }

    @Step("Get instalment table tid for the cycle {0}")
    public String getInstalmentTableTID(int row) {
        clickOrderDetailsTab();
        waitForElementVisible(instalmentTable);
        return getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type(" + row + ")>td:nth-of-type(2) span")).getText().trim();
    }

    @Step("Get instalment table amount for the cycle {0}")
    public String getInstalmentTableAmount(int row) {
        clickOrderDetailsTab();
        waitForElementVisible(instalmentTable);
        return getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type(" + row + ")>td:nth-of-type(3) span"))
                .getText().replaceAll("[^0-9]", "");
    }

    @Step("Get instalment table next cycle date for the cycle {0}")
    public String getInstalmentTableNextCycleDate(int row) {
        clickOrderDetailsTab();
        waitForElementVisible(instalmentTable);
        return getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type(" + row + ")>td:nth-of-type(6) span")).getText().trim();
    }

    @Step("Get instalment table status for the cycle {0}")
    public String getInstalmentTableStatus(int row) {
        clickOrderDetailsTab();
        waitForElementVisible(instalmentTable);
        return getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type(" + row + ")>td:nth-of-type(7) span")).getText().trim();
    }

    @Step("Verify instalment table refund enabled for the cycle {0}")
    public boolean isInstalmentTableRefundBtnEnabled(int row) {
        clickOrderDetailsTab();
        waitForElementVisible(instalmentTable);
        WebElement refundEle = getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type(" + row + ")>td:nth-of-type(8) button"));
        if (!refundEle.getAttribute("class").contains("is--active")) {
            clickElementWithJs(refundEle);
            setExpectedCondition(d -> refundEle.getAttribute("class").contains("is--active"), 1, "Waiting for refund pop up to appear");
        }
        return !getElement(By.cssSelector("span.sw-context-menu-item__text")).getAttribute("class").contains("is--disabled");
    }

    @Step("Proceed refund in instalment table")
    public void proceedInstalmentTableRefund(int row, String amount) {
        clickOrderDetailsTab();
        waitForElementVisible(instalmentTable);
        WebElement refundEle = getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type(" + row + ")>td:nth-of-type(8) button"));
        if (!refundEle.getAttribute("class").contains("is--active")) {
            clickElementWithJs(refundEle);
            setExpectedCondition(d -> refundEle.getAttribute("class").contains("is--active"), 1, "Waiting for refund pop up to appear");
        }
        if (getElement(By.cssSelector("span.sw-context-menu-item__text")).getAttribute("class").contains("is--disabled")) {
            Assert.fail("Instalment table refund button disabled for the cycle " + row);
        }
        clickElementWithJs(By.cssSelector("span.sw-context-menu-item__text"));
        if (!getInputFieldText(By.xpath("//div[@label='Refund amount']/div[2]/input")).equals(amount))
            setText(By.xpath("//div[@label='Refund amount']/div[2]/input"), amount);
        clickElementByRefreshing(modelConfirmBtn);
        waitForElementVisible(alertSuccess);
        sleep(5);
        reloadPage();
    }

    @Step("Proceed refund in instalment table")
    public void proceedInstalmentTableRefund(int row) {
        clickOrderDetailsTab();
        waitForElementVisible(instalmentTable);
        WebElement refundEle = getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type(" + row + ")>td:nth-of-type(8) button"));
        if (!refundEle.getAttribute("class").contains("is--active")) {
            clickElementWithJs(refundEle);
            setExpectedCondition(d -> refundEle.getAttribute("class").contains("is--active"), 1, "Waiting for refund pop up to appear");
        }
        if (getElement(By.cssSelector("span.sw-context-menu-item__text")).getAttribute("class").contains("is--disabled")) {
            Assert.fail("Instalment table refund button disabled for the cycle " + row);
        }
        clickElementWithJs(By.cssSelector("span.sw-context-menu-item__text"));
        clickElementByRefreshing(modelConfirmBtn);
        waitForElementVisible(alertSuccess);
        sleep(5);
        reloadPage();
    }

    @Step("verify instalment table in admin order page")
    public void verifyInstalmentTable(String tid, int numberOfCycles, String orderAmount) {
        clickOrderDetailsTab();
        Map<String, String> upcomingDates = getUpcomingMonthDates((long) numberOfCycles - 1, "yyyy-MM-dd");
        waitForElementVisible(instalmentTable);
        sleep(1);
        boolean isNextCycleDatesExist = getElement(instalmentTable)
                .findElements(By.cssSelector("tbody>tr>td:nth-of-type(6) span"))
                .stream()
                .map(WebElement::getText)
                .filter(text -> !text.isBlank())
                .allMatch(upcomingDates::containsValue);
        verifyEquals(isNextCycleDatesExist, true, "Verify Instalment cycle dates updated in the table");
        int totalAmount = 0;
        int i = numberOfCycles;
        while (i > 0) {
            String cycleAmount = getInstalmentTableAmount(i--);
            totalAmount += Integer.parseInt(cycleAmount);
        }
        verifyEquals(String.valueOf(totalAmount), orderAmount, "Verify instalment cycle amount updated in the tables");
        verifyEquals(getInstalmentTableTID(1), tid, "Verify Instalment table tid updated correctly for cycle 1");
        verifyEquals(getInstalmentTableStatus(1), ShopwareOrderStatus.PAID.get(), "Verify Instalment table order status updated correctly for cycle 1");
        verifyEquals(isInstalmentTableRefundBtnEnabled(1), true, "Verify Instalment table refund button enabled for cycle 1");
        int j = 2;
        while (j <= numberOfCycles) {
            verifyEquals(getInstalmentTableStatus(j), ShopwareOrderStatus.PENDING.get(), "Verify Instalment table order status updated correctly for cycle " + j);
            verifyEquals(getInstalmentTableTID(j), "", "Verify Instalment table tid value updated for cycle " + j);
            verifyEquals(isInstalmentTableRefundBtnEnabled(j), false, "Verify Instalment table refund button enabled for cycle " + j);
            j++;
        }
        verifyEquals(getInstalmentTableNextCycleDate(numberOfCycles), "", "Verify Instalment last cycle next instalment date not updated");
    }

    public boolean isInstalmentTableDisplayed() {
        return checkElementDisplayed(instalmentTable);
    }

}
