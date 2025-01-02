package com.nn.pages;

import org.openqa.selenium.By;

import com.nn.constants.Constants;
import com.nn.utilities.DriverActions;

import java.util.Arrays;

public class ProductPage {

	public void addProductToCartByName(String[] name) {
		Arrays.stream(name).forEach(s->{
			String addToCart = "[aria-label*='"+s.trim()+"']";
			DriverActions.clickElement(By.cssSelector(addToCart));
			DriverActions.waitForElementVisible(By.xpath("//*[contains(@aria-label,'"+s+"')]/following::a[contains(@class,'added_to_cart')][1]"));
		});
	}

	public void load() {
		DriverActions.openURL(Constants.URL_FRONTEND+"index.php/shop/");
		DriverActions.waitForTitleContains("Products");
	}


}
