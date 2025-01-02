package com.nn.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.nn.constants.Constants;
import com.nn.drivers.DriverManager;
import com.nn.utilities.DriverActions;

public class CartPage {

	public CartPage() {
		PageFactory.initElements(DriverManager.getDriver(), this);
	}


	private By produtRemoveBtn = By.cssSelector("td.product-remove a.remove");

	private By productRemoveBtnNew = By.xpath("//button[@class='wc-block-cart-item__remove-link']");
	private By emptyCartMsg = By.cssSelector(".cart-empty.woocommerce-info");

	private By getEmptyCartMsgNew = By.xpath("//h2[contains(text(),'Your cart is currently empty!')]");
	private By shopLoader = By.cssSelector(".blockUI.blockOverlay");
	private By cartUndoBtn = By.cssSelector(".woocommerce-message");
	private By cartUpdateAlert = By.cssSelector("div.woocommerce-message");

	public void load() {
		DriverActions.openURL(Constants.URL_FRONTEND+"index.php/cart/");
		DriverActions.waitForTitleContains("Cart");
		DriverActions.waitForElementVisible(By.xpath("//h1[.='Cart'] | //h2[.='Cart']"));

	}

	public void clearCart() {
		DriverActions.waitForElementVisible(By.xpath("//h1[.='Cart'] | //h2[.='Cart']"));
		List<WebElement> products = DriverActions.getElements(produtRemoveBtn);
		if(!DriverActions.checkElementExist(emptyCartMsg)) {
			for(int i=0;i<products.size();i++) {
				DriverActions.clickElement(produtRemoveBtn);
				DriverActions.waitForElementDisable(shopLoader);
			}
		}

		/*List<WebElement> cartProducts = DriverActions.getElements(productRemoveBtnNew);
		if(!DriverActions.checkElementExist(getEmptyCartMsgNew)){
			for(int i=0;i<cartProducts.size();i++){
				DriverActions.clickElement(productRemoveBtnNew);
				DriverActions.sleep(1);
				}
			}*/
		}


	public void clearCart(String productName) {
		List<WebElement> products = DriverActions.getElements(produtRemoveBtn);
		By prodName = By.cssSelector("td.product-name > a");
		By prodQty = By.cssSelector("td.product-quantity input");
		if(!DriverActions.checkElementExist(emptyCartMsg)) {
			for(int i=0;i<products.size();i++) {
				DriverActions.clickElement(produtRemoveBtn);
				DriverActions.waitForElementDisable(shopLoader);
			}
		}
	}
}
