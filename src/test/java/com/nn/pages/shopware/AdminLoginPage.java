package com.nn.pages.shopware;

import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.ShopwareUtils;
import org.openqa.selenium.By;

import static com.nn.utilities.DriverActions.sleep;

public class AdminLoginPage {
    private By adminDashboard = By.cssSelector(".sw-admin-menu__header");
    private By userName = By.cssSelector("#sw-field--username");
    private By password = By.cssSelector("#sw-field--password");
    private By loginBtn = By.cssSelector(".sw-login__submit > button");

    public AdminLoginPage load(){
        DriverActions.openURL(ShopwareUtils.SHOP_BACK_END_URL);
        return this;
    }

    public void login(){
        DriverActions.setText(userName,ShopwareUtils.SHOP_BACKEND_USERNAME);
        DriverActions.setText(password,ShopwareUtils.SHOP_BACKEND_PASSWORD);
        DriverActions.clickElement(loginBtn);
        sleep(10);
        DriverActions.getElements(By.cssSelector(".sw-alert__close")).forEach(e->{try {e.click();}catch (Exception ignored){}});
        ExtentTestManager.addScreenShot("Shop backend login page screenshot");
        DriverActions.waitForElementVisible(adminDashboard,60);
        DriverActions.getElements(By.cssSelector(".sw-alert__close")).forEach(e->{try {e.click();}catch (Exception ignored){}});
    }


}
