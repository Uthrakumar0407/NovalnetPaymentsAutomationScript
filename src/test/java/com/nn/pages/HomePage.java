package com.nn.pages;

import static com.nn.utilities.DriverActions.*;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

import com.nn.constants.Constants;

public class HomePage {
	
	private By cartBtn = By.linkText("Cart");
	private By checkoutBtn = By.linkText("Checkout");
	private By myAccBtn = By.linkText("My account");
	private By productPageBtn = By.linkText("Shop");
	private By shopLoader = By.cssSelector(".blockUI.blockOverlay");

	public void load() {
		openURL(Constants.URL_FRONTEND);
	}
	
	public ProductPage openProductPage() {
		clickElementWithJs(productPageBtn);
		waitForPageLoad();
		waitForTitleContains("Shop");
		return new ProductPage();
	}
	
	public MyAccountPage openMyAccountPage() {
		scrollToElement(myAccBtn);
		clickElementWithJs(myAccBtn);
		waitForPageLoad();
		waitForTitleContains("My account");
		return new MyAccountPage();
	}
	
	public CartPage openCartPage() {
		scrollToElement(cartBtn);
		clickElementWithJs(cartBtn);
		waitForPageLoad();
		waitForTitleContains("Cart");
		return new CartPage();
	}

	@Step("Navigate to checkout page")
	public CheckoutPage openCheckoutPage() {
		clickElementWithJs(checkoutBtn);
		waitForPageLoad();
		waitForTitleContains("Checkout");
		sleep(2.5);
		return new CheckoutPage();
	}		
	
	
	
}
