package com.nn.pages;

import static com.nn.constants.Constants.URL;
import static com.nn.utilities.DriverActions.checkElementDisplayed;
import static com.nn.utilities.DriverActions.openURL;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

import com.nn.utilities.DriverActions;

public class DashboardPage {

	private By dashboardLogo = By.cssSelector("#menu-dashboard");
	private By woocommerceMenu = By.cssSelector("#toplevel_page_woocommerce");
	private By settingsMenu = By.cssSelector("a[href='admin.php?page=wc-settings']");
	private By ordersMenu = By.cssSelector("a[href='edit.php?post_type=shop_order']");
	private By subscriptionMenu = By.cssSelector("a[href='edit.php?post_type=shop_subscription']");
	private By adminBarMyAccount = By.cssSelector("#wp-admin-bar-my-account");
	private By adminBarLogOut = By.cssSelector("#wp-admin-bar-logout > a");

	public SettingsPage openSettingsPage() {
		DriverActions.waitForElementVisible(woocommerceMenu);
		DriverActions.hoverOnElement(woocommerceMenu);
		DriverActions.clickElementWithAction(settingsMenu);
		return new SettingsPage();
	}

	public SettingsPage loadSettingsPage() {
		DriverActions.openURL(URL+"admin.php?page=wc-settings");
		DriverActions.waitForTitleContains("WooCommerce settings");
		//DriverActions.clickElementWithAction(settingsMenu);
		return new SettingsPage();
	}

	public OrdersPage openOrdersPage() {
		DriverActions.hoverOnElement(woocommerceMenu);
		DriverActions.clickElementWithAction(ordersMenu);
		return new OrdersPage();
	}

	public SubscriptionPage openSubscriptionPage() {
		DriverActions.hoverOnElement(woocommerceMenu);
		DriverActions.clickElementWithAction(subscriptionMenu);
		return new SubscriptionPage();
	}


	@Step("Admin log out")
	public AdminPage adminLogout(){
		loadSettingsPage();
		if (checkElementDisplayed(adminBarMyAccount)) {

			DriverActions.hoverOnElement(adminBarMyAccount);
			DriverActions.clickElementWithAction(adminBarLogOut);
			DriverActions.sleep(2);

		}
		return new AdminPage();
	}




}
