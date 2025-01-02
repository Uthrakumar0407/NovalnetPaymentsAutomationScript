package com.nn.testcase.Magento;

import com.nn.drivers.DriverManager;
import com.nn.pages.Magento.*;
import com.nn.Magento.Constants.*;
import com.nn.pages.Magento.basetest.BaseTest;
import com.nn.pages.Magento.BasePage;
import static com.nn.callback.CallbackProperties.*;
import static com.nn.utilities.DriverActions.getElement;

import com.nn.reports.ExtentTestManager;
import com.nn.testcase.MagentoApiExample;
import com.nn.utilities.DriverActions;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NovalnetAdminPortalTest /*extends BaseTest */{


static WebDriver driver=null;

    private By cookieStr = By.cssSelector("div#daextlwcnf-cookie-notice-button-2");

 /*   public static WebDriver getDriver(ChromeOptions options) {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        return driver;
    }*/
  //  @Test
    public void test1() throws Exception{
        NovalnetAdminPortal nLogin= new NovalnetAdminPortal();
        nLogin.openNovalnetAdminPortal();
        nLogin.loadAutomationProject();
        nLogin.openPaymentMethods();
        System.out.println("PayPal Payment status = " +nLogin.getPaymentStatus(PAYPAL));
        nLogin.enablePayment(PAYPAL);
        System.out.println("PayPal Payment status = " +nLogin.getPaymentStatus(PAYPAL));

    }

    //@Test
    public void test12() throws Exception{
        NovalnetAdminPortal nLogin= new NovalnetAdminPortal();
        nLogin.openNovalnetAdminPortal();
        nLogin.loadAutomationProject();
        nLogin.openNovalnetGlobalConfig();
        nLogin.verifyAdminGlobalConfig();


    }


    @Test
    public static void getRegionID()
    {
        try {
            String countryId = "PL";
            String regionName = "Mazowieckie";
            String regionId = getRegionId(countryId, regionName);
            System.out.println("Region ID for " + regionName + ": " + regionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getRegionId(String countryId, String regionName) throws Exception {
      //  String endpoint = "http://192.168.2.126/nageshwaran_k/Shops/migration/mag1smore/pub/rest/V1/directory/countries/" + countryId + "/regions";
       // String endpoint = "http://192.168.2.126/nageshwaran_k/Shops/migration/mag1smore/pub/rest/V1/directory/countries/" + countryId + "/regions/" + regionName;
        String endpoint = "http://192.168.2.126/nageshwaran_k/Shops/migration/mag1smore/pub/rest/V1/directory/countries/" + countryId;
        HttpGet request = new HttpGet(endpoint);
        HttpResponse response = HttpClientBuilder.create().build().execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);
        JSONArray regions = new JSONArray(responseBody);

        for (int i = 0; i < regions.length(); i++) {
            JSONObject region = regions.getJSONObject(i);
            String name = region.getString("name");
            String id = region.getString("id");

            if (name.equalsIgnoreCase(regionName)) {
                return id;
            }
        }

        throw new Exception("Region ID not found for " + regionName);
    }
   // @Test
    public void brokenLink() throws Exception{
        DriverManager.getDriver().get("https://www.novalnet.de/");
     //   DriverManager.getDriver().get("https://card.novalnet.de/");
        DriverActions.waitForPageLoad();
        if(DriverActions.checkElementDisplayed(cookieStr)) DriverActions.clickElementWithJs(cookieStr);

        // Find all links on the page
        List<WebElement> links = DriverManager.getDriver().findElements(By.tagName("a"));
        ExtentTestManager.logMessage("count of links " + links.size());

        // Check the response status for each link on the main page
        System.out.println("Broken Links on Main Page:");
        ExtentTestManager.logMessage("SBroken Links on Main Page:");
        checkLinks(links);

        //Iterate over sublinks and check their pages
        for (WebElement link : links) {
            String url = link.getAttribute("href");
            if (url != null && !url.isEmpty() && !url.equals("#") && !url.startsWith("tel:") && !url.startsWith("mailto:")) {
                String linkText = link.getAttribute("innerHTML");

                // Check for scheduled maintenance message
                if (linkText != null && linkText.contains("We are on scheduled maintenance")) {
                    System.out.println("Scheduled maintenance page: " + url);
                    ExtentTestManager.logMessage("Scheduled maintenance page: " + url);
                    continue;
                }
                // Check for empty GUI
                if (isGUIEmpty(url)) {
                    System.out.println("Empty GUI for link: " + url);
                    ExtentTestManager.logMessage("Empty GUI for link: " + url);
                }
                DriverManager.getDriver().get(url);
                List<WebElement> subLinks = DriverManager.getDriver().findElements(By.tagName("a"));
                ExtentTestManager.logMessage("count of sub links " + subLinks.size());
                System.out.println("Sublink: " + url);
                ExtentTestManager.logMessage("Sublink: " + url);
                checkLinks(subLinks);
            }
        }




    }

    private static void checkLinks(List<WebElement> links) {
        for (WebElement link : links) {
            String url = link.getAttribute("href");
            System.out.println("Links " + url);
            ExtentTestManager.logMessage("Inside Sublink: " + url);
            if (url != null && !url.isEmpty() && !url.equals("#") && !url.startsWith("tel:") && !url.startsWith("mailto:")) {
                String linkText = link.getAttribute("innerHTML");
                // Check for scheduled maintenance message
                if (linkText != null && linkText.contains("We are on scheduled maintenance")) {
                    System.out.println("Scheduled maintenance page: " + url);
                    ExtentTestManager.logMessage("Scheduled maintenance page: " + url);
                    continue;
                }
                // Check for empty GUI
                if (isGUIEmpty(url)) {
                    System.out.println("Empty GUI for link: " + url);
                    ExtentTestManager.logMessage("Empty GUI for link: " + url);
                }
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("HEAD");
                    connection.connect();
                    int statusCode = connection.getResponseCode();
                    if (statusCode == HttpURLConnection.HTTP_NOT_FOUND || statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                        System.out.println("Broken link: " + url + ", Status code: " + statusCode);
                        ExtentTestManager.logMessage("Broken link: " + url + ", Status code: " + statusCode);
                      //  ExtentTestManager.addScreenShot("<b> Broken" +url+" </b>");
                    }
                } catch (Exception e) {
                    ExtentTestManager.addScreenShot("<b>" +url+" </b>");
                    e.printStackTrace();
                }
            }
        }
    }

    // Check if the GUI is empty for a given URL
    private static boolean isGUIEmpty(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Check for empty response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String contentType = connection.getContentType();
                if (contentType != null && contentType.startsWith("text/html")) {
                    int contentLength = connection.getContentLength();
                    if (contentLength == -1) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    //@Test
    public void test9() throws Exception{
        DriverManager.getDriver().get("http://192.168.2.91/gopinath_m/Shops/magento246/pub/sales/order/view/order_id/64/");
        ShopUserLoginPage sLogin = new ShopUserLoginPage();
        sLogin.SigninToShop("test_12062023115405_6a1fcd@novalnet.com","Novalnet@123");
        By testOrderBox= By.cssSelector("div.box-order-billing-method .box-content");
        System.out.println(getElement(testOrderBox).getAttribute("innerText"));

    }

  // @Test
    public void test99() throws Exception{
        String orderURL="http://192.168.2.91/gopinath_m/Shops/magento246/pub/admin/sales/order/view/order_id/62/";
        // Use regular expressions to extract the order ID from the URL
       ShopUserLoginPage sLogin = new ShopUserLoginPage();
    //   sLogin.SigninToShop("test_12062023115405_6a1fcd@novalnet.com","Novalnet@123");
        Pattern pattern = Pattern.compile("order_id/(\\d+)/");
        Matcher matcher = pattern.matcher(orderURL);
      //  DriverManager.getDriver().get(orderURL);
      // sLogin.SigninToShop("test_12062023115405_6a1fcd@novalnet.com","Novalnet@123");
        if (matcher.find()) {
            System.out.println(matcher.group(1));

        } else {
            System.out.println("empty");
        }
    }


  //  @Test
    /*public void test2() throws Exception{
        ShopUserLoginPage nLogin= new ShopUserLoginPage();
        List<String> customerData = MagentoApiExample.createAccountAddToCart();
        String user = customerData.get(0);
        String pass= customerData.get(1);

        nLogin.SigninToShop(user,pass);

    }*/




}
