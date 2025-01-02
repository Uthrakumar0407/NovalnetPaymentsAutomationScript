package com.nn.pages.shopware;

import com.nn.exceptions.ShopwareExceptions;
import com.nn.helpers.ExcelHelpers;
import com.nn.pages.shopware.base.ShopwareOrderStatus;
import com.nn.utilities.Log;
import com.nn.utilities.ShopwareUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.Map;

import static com.nn.utilities.DriverActions.*;

public class MyAccountPage {

    private By mainContent = By.cssSelector(".account-content-main");
    private By noOrdersWarning = By.cssSelector(".account-orders-main .icon-warning");
    private By instalmentTable = By.cssSelector("table.novalnetinstalment-table");

    @Step("Load my account orders page")
    public MyAccountPage load(){
        openURL(ShopwareUtils.SHOP_FRONT_END_URL+"account");
        waitForElementVisible(mainContent);
        if(checkElementDisplayed(By.cssSelector(".cookie-permission-container .cookie-permission-button")))
            clickElementWithJs(By.cssSelector(".cookie-permission-container .cookie-permission-button"));
        return this;
    }

    @Step("Open my account orders page")
    public MyAccountPage openOrdersPage(){
        openURL(ShopwareUtils.SHOP_FRONT_END_URL+"account/order");
        waitForElementVisible(mainContent);
        if(checkElementDisplayed(By.cssSelector(".cookie-permission-container .cookie-permission-button")))
            clickElementWithJs(By.cssSelector(".cookie-permission-container .cookie-permission-button"));
        return this;
    }

    @Step("Click order {0} in my account orders page")
    public MyAccountPage expandOrder(String orderNumber){
        By expandBtn = By.cssSelector(".order-item-header button[data-bs-target='#order"+orderNumber+"']");
        By nextPageBtn = By.cssSelector("li.page-item.page-next");
        boolean flag = false;
        if(checkElementDisplayed(nextPageBtn)){
            while(!getElement(nextPageBtn).getAttribute("class").contains("disabled")){
                if(checkElementDisplayed(expandBtn)){
                    scrollToElement(expandBtn);
                    clickElementByRefreshing(expandBtn);
                    waitForElementAttributeToChange(expandBtn,"aria-expanded","true");
                    flag =true;
                    break;
                }
                scrollToElement(nextPageBtn);
                clickElementByRefreshing(nextPageBtn);
                sleep(1.5);
            }
        }else{
            scrollToElement(expandBtn);
            clickElementByRefreshing(expandBtn);
            waitForElementAttributeToChange(expandBtn,"aria-expanded","true");
            flag = true;
        }
        if(!flag)
            throw new ShopwareExceptions("No order present in my account page for the order number "+orderNumber);
        return this;
    }

    @Step("Get my account order status")
    public String getOrderStatus(String orderNumber){
        //button[@data-bs-target='#order10122']/preceding::strong[contains(.,'Lieferstatus') or contains(.,'Payment status')]/following-sibling::span
        //String xpath = "(//span[.='"+orderNumber+"']/ancestor::div[@class='order-wrapper']//span[@class='order-table-body-value'])[3]";
        String xpath = "//button[@data-bs-target='#order"+orderNumber+"']/preceding::strong[contains(.,'Lieferstatus') or contains(.,'Payment status')]/following-sibling::span";
        return getElementText(By.xpath(xpath));
    }

    @Step("Get payment name in my account page")
    public String getPaymentName(String orderNumber){
        //button[@data-bs-target='#order10000']/preceding::strong[contains(.,'Zahlungsart') or contains(.,'Payment method')]/following-sibling::span
        //String xpath = "(//span[.='"+orderNumber+"']/ancestor::div[@class='order-wrapper']//span[@class='order-table-body-value'])[4]";
        String xpath = "//button[@data-bs-target='#order"+orderNumber+"']/preceding::strong[contains(.,'Zahlungsart') or contains(.,'Payment method')]/following-sibling::span";
        return getElementText(By.xpath(xpath));
    }

    @Step("Get payment name in my account order detail page")
    public String getPaymentNameInside(String orderNumber){
        String css = "#order"+orderNumber+" dd.order-item-detail-labels-value:nth-of-type(3)";
        return getElementText(By.cssSelector(css));
    }

    @Step("Get novlanet payment comments in my account orders page")
    public String getComments(String orderNumber){
        String css = "#order"+orderNumber+" dd.novalnetorder-comments-header";
        return getElementText(By.cssSelector(css)).trim();
    }

    public void addProductToCart(String productName){
        openURL(ShopwareUtils.SHOP_FRONT_END_URL+productName);
        clickElement(By.cssSelector("#productDetailPageBuyProductForm button[title]"));
        setExpectedCondition(d->{
                String attribute = d.findElement(By.cssSelector("a[href*='checkout/confirm']")).getAttribute("class");
                return attribute != null && !attribute.contains("disabled");
            }, 1,"Waiting for product added to cart");
    }

    public void changeCurrency(String currencyISO){
        try{
            WebElement currencyEle = getElement(By.cssSelector("#currenciesDropdown-top-bar+div.dropdown-menu>div[title='"+currencyISO+"']"));
            if(!currencyEle.getAttribute("class").contains("item-checked")){
                clickElement(By.cssSelector("#currenciesDropdown-top-bar"));
                clickElementByRefreshing(By.cssSelector("#currenciesDropdown-top-bar+div.dropdown-menu>div[title='"+currencyISO+"']"));
            }
        }catch (Exception e){
            Log.info(e.getMessage());
            Assert.fail("No currency exist in the shop for the given value: "+currencyISO);
        }
    }

    public String getInstalmentTableTID(int row){
        int index = row+(row-1);
        waitForElementVisible(instalmentTable);
        sleep(0.5);
        return getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type("+index+")>td:nth-of-type(2)")).getText().trim();
    }

    public String getInstalmentTableAmount(int row){
        int index = row+(row-1);
        waitForElementVisible(instalmentTable);
        return getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type("+index+")>td:nth-of-type(3)"))
                .getText().trim().replaceAll("[^0-9]","");
    }

    public String getInstalmentTableInstalmentDate(int row){
        int index = row+(row-1);
        waitForElementVisible(instalmentTable);
        return getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type("+index+")>td:nth-of-type(4)")).getText().trim();
    }

    public String getInstalmentTableStatus(int row){
        int index = row+(row-1);
        waitForElementVisible(instalmentTable);
        return getElement(instalmentTable).findElement(By.cssSelector("tbody>tr:nth-of-type("+index+")>td:nth-of-type(5)")).getText().trim();
    }

    @Step("verify instalment table in my account page")
    public void verifyInstalmentTable(String tid, int numberOfCycles, String orderAmount){
        Map<String,String> upcomingDates = getUpcomingMonthDates((long) numberOfCycles-1,"dd/MM/yyyy");
        waitForElementVisible(instalmentTable);
        sleep(1);
        boolean isNextCycleDatesExist = getElement(instalmentTable)
                                        .findElements(By.cssSelector("tbody>tr>td:nth-of-type(4)"))
                                        .stream()
                                        .map(WebElement::getText)
                                        .filter(text->!text.contains("-"))
                                        .allMatch(upcomingDates::containsValue);
        verifyEquals(isNextCycleDatesExist,true,"Verify Instalment cycle dates updated in the table");
        int totalAmount = 0;
        int i = numberOfCycles;
        while(i>0) {
            String cycleAmount = getInstalmentTableAmount(i--);
            totalAmount+=Integer.parseInt(cycleAmount);
        }
        verifyEquals(String.valueOf(totalAmount),orderAmount,"Verify instalment cycle amount updated in the tables");
        verifyEquals(getInstalmentTableTID(1),tid,"Verify Instalment table tid updated correctly for cycle 1");
        verifyEquals(getInstalmentTableStatus(1), ShopwareOrderStatus.PAID.get(),"Verify Instalment table order status updated correctly for cycle 1");
        int j = 2;
        while (j<=numberOfCycles){
            verifyEquals(getInstalmentTableStatus(j), ShopwareOrderStatus.PENDING.get(),"Verify Instalment table order status updated correctly for cycle "+j);
            verifyEquals(getInstalmentTableTID(j), "-","Verify Instalment table tid value updated for cycle "+j);
            j++;
        }
        verifyEquals(getInstalmentTableInstalmentDate(numberOfCycles),"-","Verify Instalment last cycle next instalment date not updated");
    }

    public void changeBillingCompany(){
        openURL(ShopwareUtils.SHOP_FRONT_END_URL+"account/address");
        waitForElementVisible(By.cssSelector(".account-address"));
        clickElement(By.xpath("//a[@title='Edit address']/span"));
        Map<String,String> address = ExcelHelpers.addressGuaranteeB2B();
        if(!getInputFieldText(By.cssSelector("#addresscompany")).equals(address.get("Company"))){
            setText(By.cssSelector("#addresscompany"),address.get("Company"));
            clickElement(By.cssSelector("button.address-form-submit"));
            waitForElementVisible(By.cssSelector(".alert-success"));
        }
    }

    public void changeBillingForPending(){
       openURL(ShopwareUtils.SHOP_FRONT_END_URL+"account/address");
        waitForElementVisible(By.cssSelector(".account-address"));
        clickElement(By.xpath("//a[@title='Edit address']/span"));
        Map<String,String> address = ExcelHelpers.addressGuaranteeB2BPending();
        //if(!getInputFieldText(By.cssSelector("#addresscompany")).equals(address.get("Company"))){
        setText(By.cssSelector("#addresscompany"),address.get("Company"));
        setText(By.cssSelector("#addressAddressStreet"),address.get("Street"));
        setTextWithoutClear(By.cssSelector("#addressAddressStreet"),", "+address.get("HouseNo"));
        setText(By.cssSelector("#addressAddressCity"),address.get("City"));
        setText(By.cssSelector("#addressAddressZipcode"),address.get("Zip"));
        clickElement(By.cssSelector("button.address-form-submit"));
            waitForElementVisible(By.cssSelector(".alert-success"));
    }
    public void changeBillingCountry(String country){
        openURL(ShopwareUtils.SHOP_FRONT_END_URL+"account/address");
        waitForElementVisible(By.cssSelector(".account-address"));
        clickElement(By.xpath("//a[@title='Edit address']/span"));
        if(!getDropdownSelectedOptionText(By.cssSelector("#addressAddressCountry")).equals(country))
            selectDropdownByText(By.cssSelector("#addressAddressCountry"),country);
        clickElement(By.cssSelector("button.address-form-submit"));
        waitForElementVisible(By.cssSelector(".alert-success"));
    }

    public boolean isInstalmentTableDisplayed(){
        return checkElementDisplayed(instalmentTable);
    }

    @Step("set B2B billing address")
    public void setB2BBillingAddress(){
        openURL(ShopwareUtils.SHOP_FRONT_END_URL+"account/address");
        Map<String,String> address = ExcelHelpers.addressGuaranteeB2B();
        clickElement(By.xpath("//a[@title='Edit address']/span"));
        if(!getDropdownSelectedOptionText(By.cssSelector("#addressAddressCountry")).equals(address.get("Country")))
            selectDropdownByText(By.cssSelector("#addressAddressCountry"),address.get("Country"));
        if(!getInputFieldText(By.cssSelector("#addresspersonalFirstName")).equals(address.get("FirstName")))
            setText(By.cssSelector("#addresspersonalFirstName"),address.get("FirstName"));
        if(!getInputFieldText(By.cssSelector("#addresspersonalLastName")).equals(address.get("LastName")))
            setText(By.cssSelector("#addresspersonalLastName"),address.get("LastName"));
        if(!getInputFieldText(By.cssSelector("#addressAddressStreet")).equals(address.get("HouseNo")+" , "+address.get("Street")))
            setText(By.cssSelector("#addressAddressStreet"),address.get("HouseNo")+" , "+address.get("Street"));
        if(!getInputFieldText(By.cssSelector("#addressAddressZipcode")).equals(address.get("Zip")))
            setText(By.cssSelector("#addressAddressZipcode"),address.get("Zip"));
        if(!getInputFieldText(By.cssSelector("#addresscompany")).equals(address.get("Company")))
            setText(By.cssSelector("#addresscompany"),address.get("Company"));
        if(!getInputFieldText(By.cssSelector("#addressAddressCity")).equals(address.get("City")))
            setText(By.cssSelector("#addressAddressCity"),address.get("City"));
        sleep(1);
        clickOutsideForm();
        sleep(1);
        clickElement(By.cssSelector("button.address-form-submit"));
        waitForElementVisible(By.cssSelector(".alert-success"));
    }

    @Step("set Different shipping address")
    public void setDifferentShippingAddress(){
        openURL(ShopwareUtils.SHOP_FRONT_END_URL+"account/address");
        Map<String,String> address = ExcelHelpers.addressGuaranteeB2B();
        clickElement(By.cssSelector(".address-action-create>a"));
        if(!getDropdownSelectedOptionText(By.cssSelector("#addressAddressCountry")).equals(address.get("Country")))
            selectDropdownByText(By.cssSelector("#addressAddressCountry"),address.get("Country"));
        if(!getInputFieldText(By.cssSelector("#addresspersonalFirstName")).equals(address.get("FirstName")))
            setText(By.cssSelector("#addresspersonalFirstName"),address.get("FirstName"));
        if(!getInputFieldText(By.cssSelector("#addresspersonalLastName")).equals(address.get("LastName")))
            setText(By.cssSelector("#addresspersonalLastName"),address.get("LastName"));
        if(!getInputFieldText(By.cssSelector("#addressAddressStreet")).equals(address.get("HouseNo")+" , "+address.get("Street")))
            setText(By.cssSelector("#addressAddressStreet"),address.get("HouseNo")+" , "+address.get("Street"));
        if(!getInputFieldText(By.cssSelector("#addressAddressZipcode")).equals(address.get("Zip")))
            setText(By.cssSelector("#addressAddressZipcode"),address.get("Zip"));
        if(!getInputFieldText(By.cssSelector("#addresscompany")).equals(address.get("Company")))
            setText(By.cssSelector("#addresscompany"),address.get("Company"));
        if(!getInputFieldText(By.cssSelector("#addressAddressCity")).equals(address.get("City")))
            setText(By.cssSelector("#addressAddressCity"),address.get("City"));
        sleep(1);
        clickOutsideForm();
        sleep(1);
        clickElement(By.cssSelector("button.address-form-submit"));
        waitForElementVisible(By.cssSelector(".alert-success"));
        clickElementWithJs(By.cssSelector("button.address-action-set-default-shipping"));
    }
    public boolean isDownloadButtonDisplayed(String orderNumber){
        return waitForElementVisible(By.cssSelector("#order"+orderNumber+" a.download-item-view-file-text-btn"), 3, "");
    }
    public boolean isDownloadLinkDisplayed(String orderNumber){
        return waitForElementVisible(By.cssSelector("#order"+orderNumber+" .download-item-file-name>a"), 3, "");
    }
}
