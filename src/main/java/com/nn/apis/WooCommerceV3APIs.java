package com.nn.apis;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class WooCommerceV3APIs {

    //public static String signature=null;

//server shop
private static final String CONSUMER_KEY = "ck_b1362280f15858813895942ef16a54dbf79d9c81";
    private static final String CONSUMER_SECRET = "cs_4365c7a19872d7d9f0e5110213f78156a8ac11fb";

    private static String SHOP_BASE_URL = "http://woocommerce.novalnet.che/";;

   //Keerthana shop
   /* private static final String CONSUMER_KEY = "ck_6096d6e1584f85f3d55c18feb6914cd28faec658";


    private static final String CONSUMER_SECRET = "cs_86508e6baec091f17a363a61d3ad03e84be2439b";

    private static String SHOP_BASE_URL = "http://192.168.2.140/keerthana/WooCom/wp622/wordpress/";*/




   //Saranya shop
  /*  private static final String CONSUMER_SECRET = "cs_bd97f9a4470059354c13b6dbb879e92926d6ec26";

    private static final String CONSUMER_KEY = "ck_58bb8f41f9d4b1a948e7761e29757fa1e938d323";


    private static String SHOP_BASE_URL = "http://192.168.2.88/saranya_y/woocommerce/woo_820_w_63/";*/



    //Nageshwaran shop
/*    private static final String CONSUMER_SECRET = "cs_bd97f9a4470059354c13b6dbb879e92926d6ec26";

    private static final String CONSUMER_KEY = "ck_58bb8f41f9d4b1a948e7761e29757fa1e938d323";


    private static String SHOP_BASE_URL = "http://192.168.2.126/nageshwaran_k/Shops/wordpress_6_2/wordpress/";*/


    //Gopi shop

   /* private static final String CONSUMER_KEY = "ck_f0828e93d784d5a44a999c7bb4891edcaa3811fc";
    private static final String CONSUMER_SECRET = "cs_0fd31aebf063cfbe4af74a16dbeb45b3fb07e906";

    private static String SHOP_BASE_URL = "http://192.168.2.91/gopinath_m/Shops/wordpress_6_2/";*/




    public static void main(String a[]) throws Exception {

        // API call - Setup woocommerce shop with automation users and products
/*        WooCommerceV3APIs.setupShopWithAutomationUsers();
        WooCommerceV3APIs.createSimpleProduct(SHOP_BASE_URL,CONSUMER_SECRET,CONSUMER_KEY,"Happy Ninja" ,18.00f);
        WooCommerceV3APIs.createSimpleProduct(SHOP_BASE_URL,CONSUMER_SECRET,CONSUMER_KEY,"Expert Ninja" ,40.00f);
        WooCommerceV3APIs.createSimpleProduct(SHOP_BASE_URL,CONSUMER_SECRET,CONSUMER_KEY,"San Ninja" ,8.00f);
        WooCommerceV3APIs.createSubscriptionProduct_Test1Subs();
        WooCommerceV3APIs.createSubscriptionProduct_Test2Subs();
        WooCommerceV3APIs.createSubscriptionProduct_Test4Subs();
        WooCommerceV3APIs.createVirtualProduct(SHOP_BASE_URL,CONSUMER_SECRET,CONSUMER_KEY,"Virtual" ,18.00f);
        WooCommerceV3APIs.createDownloadbleProduct(SHOP_BASE_URL,CONSUMER_SECRET,CONSUMER_KEY,"Downloadable" ,18.00f);*/
        WooCommerceV3APIs.createCustomer("http://192.168.2.113/mohamedn_m/Woocommerce_shops/Woocommerce_831/","del9@gmail.cm");
    }


    public static void setupShopWithAutomationUsers() throws Exception{
        List<String> emailAddresses = new ArrayList<>();
        emailAddresses.add("sepa@gmail.com");
        emailAddresses.add("cc@gmail.com");
        emailAddresses.add("paypal@gmail.com");
        emailAddresses.add("invoice@gmail.com");
        emailAddresses.add("installment@gmail.com");
        emailAddresses.add("installmentsepa@gmail.com");
        emailAddresses.add("invoiceg@gmail.com");
        emailAddresses.add("ideal@gmail.com");
        emailAddresses.add("admin@gmail.com");
        emailAddresses.add("admin1@gmail.com");
        emailAddresses.add("admin2@gmail.com");
        emailAddresses.add("admin3@gmail.com");
        emailAddresses.add("admin4@gmail.com");
        emailAddresses.add("admin5@gmail.com");
        emailAddresses.add("oncash@gmail.com");
        emailAddresses.add("sofort@gmail.com");
        emailAddresses.add("onlinebanktransfer@gmail.com");
        emailAddresses.add("giropay@gmail.com");
        emailAddresses.add("eps@gmail.com");
        emailAddresses.add("bancontact@gmail.com");
        emailAddresses.add("multibanco@gmail.com");
        emailAddresses.add("allipay@gmail.com");
        emailAddresses.add("postfinance@gmail.com");
        emailAddresses.add("wechatpay@gmail.com");
        emailAddresses.add("trustly@gmail.com");
        emailAddresses.add("subs@gmail.com");
        emailAddresses.add("prepayment@gmail.com");
        emailAddresses.add("email@gmail.com");


        setupCustomer(emailAddresses);
    }
    public static void createCustomer(String SHOP_BASE_URL,String emailAddress) throws Exception {

        try {

            JSONObject customer = new JSONObject();
            customer.put("email", emailAddress);
            customer.put("password", "wordpress");
            customer.put("first_name", "First");
            customer.put("last_name", "Last");
            customer.put("role", "customer");


            // Add billing information to the customer
            JSONObject billingInfo = new JSONObject();
            billingInfo.put("first_name", "Norbert");
            billingInfo.put("last_name", "Maier");
            billingInfo.put("company", "");
            billingInfo.put("address_1", "9, Hauptstr");
            billingInfo.put("address_2", "");
            billingInfo.put("city", "Kaiserslautern");
            billingInfo.put("state", "DE-BE");
            billingInfo.put("postcode", "66862");
            billingInfo.put("country", "DE");
            billingInfo.put("email", "automation_test@novalnetsolutions.com");
            billingInfo.put("phone", "045818858555");
            customer.put("billing", billingInfo);
                // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters();
            String signature = generateSignature("POST", SHOP_BASE_URL + "wp-json/wc/v3/customers", oauthParams, CONSUMER_SECRET);

                // Send POST request with RestAssured
                Response response = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(customer.toString())
                        .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                        .post(SHOP_BASE_URL + "wp-json/wc/v3/customers");

                // Print the response
                System.out.println("Response:");
                System.out.println(response.asString());
                if (response.getStatusCode() == 201) {
                    // Get the user ID from the response JSON
                    int userId = getUserIdFromResponse(response.asString());
                    if (userId != -1) {
                        System.out.println("User created with ID: " + userId);
                        // updateRoleToAdministrator(SHOP_BASE_URL,userId);
                    } else {
                        System.out.println("Failed to extract user ID from the response.");
                    }
                } else {
                    System.out.println("Failed to create user. Status code: " + response.getStatusCode());
                }

        } catch (Exception e) {
        }
    }

    public static void createSimpleProduct(String SHOP_BASE_URL, String consumerSecret, String consumerKey ,String productName, float amount) throws Exception {

        try {

            JSONObject product = new JSONObject();
            product.put("name", productName);
            product.put("type", "simple");
            product.put("regular_price", String.format("%.2f", amount));
            product.put("description", productName);
            product.put("short_description", productName);

            JSONArray categories = new JSONArray();
            JSONObject category = new JSONObject();
            category.put("id", 9);
            categories.put(category);
            product.put("categories", categories);

            JSONArray images = new JSONArray();
            JSONObject image = new JSONObject();
            image.put("src", SHOP_BASE_URL+"wp-content/uploads/woocommerce-placeholder.png");
            images.put(image);
            product.put("images", images);

            // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters(consumerKey);
            String signature = generateSignature("POST", SHOP_BASE_URL + "wp-json/wc/v3/products", oauthParams, consumerSecret);

            // Send POST request with RestAssured
            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(product.toString())
                    .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .post(SHOP_BASE_URL + "wp-json/wc/v3/products");

            // Print the response
            System.out.println("Response:");
            System.out.println(response.asString());
            if (response.getStatusCode() == 201) {

            } else {
                System.out.println("Failed to create Simple Product. Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
        }
    }


    public static void createVirtualProduct(String SHOP_BASE_URL, String consumerSecret, String consumerKey ,String productName, float amount) throws Exception {

        try {

            JSONObject product = new JSONObject();
            product.put("name", productName);
            product.put("type", "simple");
            product.put("regular_price", String.format("%.2f", amount));
            product.put("description", productName);
            product.put("short_description", productName);
            product.put("virtual", "true");

            JSONArray categories = new JSONArray();
            JSONObject category = new JSONObject();
            category.put("id", 9);
            categories.put(category);
            product.put("categories", categories);

            JSONArray images = new JSONArray();
            JSONObject image = new JSONObject();
            image.put("src", SHOP_BASE_URL+"wp-content/uploads/woocommerce-placeholder.png");
            images.put(image);
            product.put("images", images);

            // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters(consumerKey);
            String signature = generateSignature("POST", SHOP_BASE_URL + "wp-json/wc/v3/products", oauthParams, consumerSecret);

            // Send POST request with RestAssured
            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(product.toString())
                    .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .post(SHOP_BASE_URL + "wp-json/wc/v3/products");

            // Print the response
            System.out.println("Response:");
            System.out.println(response.asString());
            if (response.getStatusCode() == 201) {

            } else {
                System.out.println("Failed to create Simple Product. Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
        }
    }


    public static void createDownloadbleProduct(String SHOP_BASE_URL, String consumerSecret, String consumerKey ,String productName, float amount) throws Exception {

        try {

            JSONObject product = new JSONObject();
            product.put("name", productName);
            product.put("type", "simple");
            product.put("regular_price", String.format("%.2f", amount));
            product.put("description", productName);
            product.put("short_description", productName);
            product.put("downloadable", "true");

            JSONArray categories = new JSONArray();
            JSONObject category = new JSONObject();
            category.put("id", 9);
            categories.put(category);
            product.put("categories", categories);

            JSONArray images = new JSONArray();
            JSONObject image = new JSONObject();
            image.put("src", SHOP_BASE_URL+"wp-content/uploads/woocommerce-placeholder.png");
            images.put(image);
            product.put("images", images);

            // Add downloadable options
            product.put("downloadable", true);
            JSONArray downloads = new JSONArray();
            JSONObject download = new JSONObject();
            download.put("id", "7561ff9f-de28-400b-b82e-feba19817806");
            download.put("name", "NovalnetLogo-y3huy9.png");
            download.put("file", SHOP_BASE_URL+"wp-content/uploads/2023/10/woocommerce-placeholder-1.png");
            downloads.put(download);
            product.put("downloads", downloads);
            product.put("download_limit", -1);
            product.put("download_expiry", -1);



            // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters(consumerKey);
            String signature = generateSignature("POST", SHOP_BASE_URL + "wp-json/wc/v3/products", oauthParams, consumerSecret);

            // Send POST request with RestAssured
            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(product.toString())
                    .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .post(SHOP_BASE_URL + "wp-json/wc/v3/products");

            // Print the response
            System.out.println("Response:");
            System.out.println(response.asString());
            if (response.getStatusCode() == 201) {

            } else {
                System.out.println("Failed to create Simple Product. Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
        }
    }



    public static void createSimpleProductAndUpdateToSubscription(
            String SHOP_BASE_URL,
            String consumerSecret,
            String consumerKey,
            String productName,
            float simpleProductPrice,
            int billingInterval,
            float signupFee,
            int freeTrialDays) {

        try {
            // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters(consumerKey);
            String signature = generateSignature("POST", SHOP_BASE_URL + "wp-json/wc/v3/products", oauthParams, consumerSecret);


            // Create a new simple product JSON
            JSONObject product = new JSONObject();
            product.put("name", productName);
            product.put("type", "simple");
            product.put("regular_price", String.format("%.2f", simpleProductPrice));
            product.put("description", productName);
            product.put("short_description", productName);

            JSONArray categories = new JSONArray();
            JSONObject category = new JSONObject();
            category.put("id", 9);
            categories.put(category);
            product.put("categories", categories);

            JSONArray images = new JSONArray();
            JSONObject image = new JSONObject();
            image.put("src", SHOP_BASE_URL+"wp-content/uploads/woocommerce-placeholder.png");
            images.put(image);
            product.put("images", images);

            // Send a POST request to create the simple product
            Response simpleProductResponse = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(product.toString())
                     .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .post(SHOP_BASE_URL + "wp-json/wc/v3/products");

            if (simpleProductResponse.getStatusCode() != 201) {
                System.out.println("Failed to create Simple Product. Status code: " + simpleProductResponse.getStatusCode());
                return;
            }

            // Get the product ID from the response JSON
            int simpleProductId = simpleProductResponse.jsonPath().getInt("id");

            // Now, update the simple product to a subscription product
            JSONObject subscriptionAttributes = new JSONObject();
            subscriptionAttributes.put("interval", billingInterval);
            subscriptionAttributes.put("sign_up_fee", signupFee);
            subscriptionAttributes.put("trial_period", freeTrialDays);

            JSONObject subscriptionProduct = new JSONObject();
            subscriptionProduct.put("type", "subscription");
            subscriptionProduct.put("regular_price", simpleProductPrice); // Use the same price as the simple product
            subscriptionProduct.put("subscription", subscriptionAttributes);

            // Send a PUT request to update the product to a subscription
            Response subscriptionProductResponse = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(subscriptionProduct.toString())
                    .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .put(SHOP_BASE_URL + "wp-json/wc/v3/products/" + simpleProductId);

            // Print the response
            System.out.println("Response for Subscription Product:");
            System.out.println(subscriptionProductResponse.asString());
            if (subscriptionProductResponse.getStatusCode() == 200) {
                // Product update was successful
            } else {
                System.out.println("Failed to update product to a subscription. Status code: " + subscriptionProductResponse.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



// You can keep the createSimpleProduct method as it is, and call it within createSimpleProductAndUpdateToSubscription.

    private static void setupCustomer(List<String> emailAddresses) throws Exception {

        try {
            for (String email : emailAddresses) {
                JSONObject customer = new JSONObject();
                customer.put("email", email);
                customer.put("password", "wordpress");
                customer.put("first_name", "First");
                customer.put("last_name", "Last");
                customer.put("role", "customer");

                // Add billing information to the customer
                JSONObject billingInfo = new JSONObject();
                billingInfo.put("first_name", "Norbert");
                billingInfo.put("last_name", "Maier");
                billingInfo.put("company", "");
                billingInfo.put("address_1", "9, Hauptstr");
                billingInfo.put("address_2", "");
                billingInfo.put("city", "Kaiserslautern");
                billingInfo.put("state", "DE-BE");
                billingInfo.put("postcode", "66862");
                billingInfo.put("country", "DE");
                billingInfo.put("email", "automation_test@novalnetsolutions.com");
                billingInfo.put("phone", "045818858555");
                customer.put("billing", billingInfo);

                // Generate OAuth parameters and signature
                Map<String, String> oauthParams = generateOAuthParameters();
                String signature = generateSignature("POST", SHOP_BASE_URL + "wp-json/wc/v3/customers", oauthParams, CONSUMER_SECRET);

                // Send POST request with RestAssured
                Response response = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(customer.toString())
                        .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                        .post(SHOP_BASE_URL + "wp-json/wc/v3/customers");

                // Print the response
                System.out.println("Response:");
                System.out.println(response.asString());
                if (response.getStatusCode() == 201) {
                    // Get the user ID from the response JSON
                    int userId = getUserIdFromResponse(response.asString());
                    if (userId != -1) {
                        System.out.println("User created with ID: " + userId);
                        // updateRoleToAdministrator(SHOP_BASE_URL,userId);
                    } else {
                        System.out.println("Failed to extract user ID from the response.");
                    }
                } else {
                    System.out.println("Failed to create user. Status code: " + response.getStatusCode());
                }
            }
        } catch (Exception e) {
        }
    }
    private static int createSimpleProductWithTest4SubsAndGetProductId(String SHOP_BASE_URL, String consumerSecret, String consumerKey, String productName, float amount) {

        try {
            // Add the price_html attribute
            JSONObject product = new JSONObject();
            product.put("name", productName);
            product.put("type", "simple");
            product.put("regular_price", String.format("%.2f", 20.00f)); // Hardcoded price
            product.put("description", productName);
            product.put("short_description", productName);
            product.put("price_html", "<span class=\"woocommerce-Price-amount amount\"><bdi><span class=\"woocommerce-Price-currencySymbol\">&euro;</span>20.00</bdi></span> <span class=\"subscription-details\"> / month for 5 months</span>");

        // Add related_ids
            JSONArray relatedIds = new JSONArray();
            relatedIds.put(10);
            relatedIds.put(11);
            relatedIds.put(24);
            relatedIds.put(21);
            relatedIds.put(1305);
            product.put("related_ids", relatedIds);

    // Add meta_data
            JSONArray metaData = new JSONArray();

            JSONObject metaData1 = new JSONObject();
            metaData1.put("id", 343);
            metaData1.put("key", "_subscription_payment_sync_date");
            metaData1.put("value", "0");
            metaData.put(metaData1);

            JSONObject metaData2 = new JSONObject();
            metaData2.put("id", 344);
            metaData2.put("key", "_subscription_price");
            metaData2.put("value", "20");
            metaData.put(metaData2);

            JSONObject metaData3 = new JSONObject();
            metaData3.put("id", 348);
            metaData3.put("key", "_subscription_trial_length");
            metaData3.put("value", "0");
            metaData.put(metaData3);

            JSONObject metaData4 = new JSONObject();
            metaData4.put("id", 349);
            metaData4.put("key", "_subscription_sign_up_fee");
            metaData4.put("value", "");
            metaData.put(metaData4);


            JSONObject metaData5 = new JSONObject();
            metaData5.put("id", 350);
            metaData5.put("key", "_subscription_period");
            metaData5.put("value", "month");
            metaData.put(metaData5);


            JSONObject metaData6 = new JSONObject();
            metaData6.put("id", 351);
            metaData6.put("key", "_subscription_period_interval");
            metaData6.put("value", "1");
            metaData.put(metaData6);

            JSONObject metaData7 = new JSONObject();
            metaData7.put("id", 352);
            metaData7.put("key", "_subscription_length");
            metaData7.put("value", "5");
            metaData.put(metaData7);

            JSONObject metaData8 = new JSONObject();
            metaData8.put("id", 353);
            metaData8.put("key", "_subscription_trial_period");
            metaData8.put("value", "day");
            metaData.put(metaData8);

            JSONObject metaData9 = new JSONObject();
            metaData9.put("id", 354);
            metaData9.put("key", "_subscription_limit");
            metaData9.put("value", "no");
            metaData.put(metaData9);

            // Add other meta_data entries as needed

            product.put("meta_data", metaData);

            JSONArray categories = new JSONArray();
            JSONObject category = new JSONObject();
            category.put("id", 9);
            categories.put(category);
            product.put("categories", categories);

            JSONArray images = new JSONArray();
            JSONObject image = new JSONObject();
            image.put("src", SHOP_BASE_URL + "wp-content/uploads/woocommerce-placeholder.png");
            images.put(image);
            product.put("images", images);
            product.put("price_html", "<span class=\"woocommerce-Price-amount amount\"><bdi><span class=\"woocommerce-Price-currencySymbol\">&euro;</span>" + String.format("%.2f", amount) + "</bdi></span> <span class=\"subscription-details\"> every " + 2 + " months with a " + 7 + "-day free trial</span>");

            // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters(consumerKey);
           String signature = generateSignature("POST", SHOP_BASE_URL + "wp-json/wc/v3/products", oauthParams, consumerSecret);

            // Send POST request with RestAssured
            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(product.toString())
                    .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .post(SHOP_BASE_URL + "wp-json/wc/v3/products");

            // Print the response
            System.out.println("Response:");
            System.out.println(response.asString());
            if (response.getStatusCode() == 201) {
                // Get the product ID from the response JSON
                int productId = response.jsonPath().getInt("id");
                return productId;
            } else {
                System.out.println("Failed to create Simple Product. Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // Return -1 in case of an error
    }

    private static int createSimpleProductWithTest1SubsAndGetProductId(String SHOP_BASE_URL, String consumerSecret, String consumerKey) {

        try {
            // Add the price_html attribute
            JSONObject product = new JSONObject();
            product.put("name", "TEST-1 SUBS");
            product.put("type", "simple");
            product.put("regular_price", String.format("%.2f", 20.00f)); // Hardcoded price
            product.put("description", "TEST-1 SUBS");
            product.put("short_description", "TEST-1 SUBS");
        //    product.put("price_html", "<span class=\"woocommerce-Price-amount amount\"><bdi><span class=\"woocommerce-Price-currencySymbol\">&euro;</span>15.00</bdi></span> <span class=\"subscription-details\"> / every 2 months with a 7-day free trial</span>");

            // Add related_ids
           /* JSONArray relatedIds = new JSONArray();
            relatedIds.put(25);
            relatedIds.put(1305);
            relatedIds.put(10);
            relatedIds.put(23);
            relatedIds.put(24);
            product.put("related_ids", relatedIds);
*/
            // Add meta_data
            JSONArray metaData = new JSONArray();

            JSONObject metaData1 = new JSONObject();
        //    metaData1.put("id", 311);
            metaData1.put("key", "_subscription_payment_sync_date");
            metaData1.put("value", "0");
            metaData.put(metaData1);

            JSONObject metaData2 = new JSONObject();
          //  metaData2.put("id", 312);
            metaData2.put("key", "_subscription_price");
            metaData2.put("value", "15");
            metaData.put(metaData2);

            JSONObject metaData3 = new JSONObject();
        //    metaData3.put("id", 316);
            metaData3.put("key", "_subscription_trial_length");
            metaData3.put("value", "7");
            metaData.put(metaData3);

            JSONObject metaData4 = new JSONObject();
         //   metaData4.put("id", 317);
            metaData4.put("key", "_subscription_sign_up_fee");
            metaData4.put("value", "");
            metaData.put(metaData4);


            JSONObject metaData5 = new JSONObject();
       //     metaData5.put("id", 318);
            metaData5.put("key", "_subscription_period");
            metaData5.put("value", "month");
            metaData.put(metaData5);


            JSONObject metaData6 = new JSONObject();
       //     metaData6.put("id", 319);
            metaData6.put("key", "_subscription_period_interval");
            metaData6.put("value", "2");
            metaData.put(metaData6);

            JSONObject metaData7 = new JSONObject();
       //     metaData7.put("id", 320);
            metaData7.put("key", "_subscription_length");
            metaData7.put("value", "0");
            metaData.put(metaData7);

            JSONObject metaData8 = new JSONObject();
         //   metaData8.put("id", 321);
            metaData8.put("key", "_subscription_trial_period");
            metaData8.put("value", "day");
            metaData.put(metaData8);

            JSONObject metaData9 = new JSONObject();
        //    metaData9.put("id", 322);
            metaData9.put("key", "_subscription_limit");
            metaData9.put("value", "no");
            metaData.put(metaData9);

            // Add other meta_data entries as needed

            product.put("meta_data", metaData);

            /*JSONArray categories = new JSONArray();
            JSONObject category = new JSONObject();
            category.put("id", 15);
            categories.put(category);
            product.put("categories", categories);*/

            JSONArray images = new JSONArray();
            JSONObject image = new JSONObject();
            image.put("src", SHOP_BASE_URL + "wp-content/uploads/woocommerce-placeholder.png");
            images.put(image);
            product.put("images", images);
       //   product.put("price_html", "<span class=\"woocommerce-Price-amount amount\"><bdi><span class=\"woocommerce-Price-currencySymbol\">&euro;</span>" + String.format("%.2f", 20.00f) + "</bdi></span> <span class=\"subscription-details\"> every " + 2 + " months with a " + 7 + "-day free trial</span>");

            // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters(consumerKey);
            String signature = generateSignature("POST", SHOP_BASE_URL + "wp-json/wc/v3/products", oauthParams, consumerSecret);

            // Send POST request with RestAssured
            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(product.toString())
                    .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .post(SHOP_BASE_URL + "wp-json/wc/v3/products");

            // Print the response
            System.out.println("Response:");
            System.out.println(response.asString());
            if (response.getStatusCode() == 201) {
                // Get the product ID from the response JSON
                int productId = response.jsonPath().getInt("id");
                return productId;
            } else {
                System.out.println("Failed to create Simple Product. Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // Return -1 in case of an error
    }

    private static int createSimpleProductWithTest2SubsAndGetProductId(String SHOP_BASE_URL, String consumerSecret, String consumerKey) {

        try {
            // Add the price_html attribute
            JSONObject product = new JSONObject();
            product.put("name", "TEST-2 SUBS");
            product.put("type", "simple");
            product.put("regular_price", String.format("%.2f", 10.00f)); // Hardcoded price
            product.put("description", "TEST-2 SUBS");
            product.put("short_description", "TEST-2 SUBS");
            product.put("price_html", "<span class=\"woocommerce-Price-amount amount\"><bdi><span class=\"woocommerce-Price-currencySymbol\">&euro;</span>10.00</bdi></span> <span class=\"subscription-details\"> /month and a  sign-up fee</span>");

            // Add related_ids
            JSONArray relatedIds = new JSONArray();
            relatedIds.put(24);
            relatedIds.put(25);
            relatedIds.put(23);
            relatedIds.put(21);
            relatedIds.put(10);
            product.put("related_ids", relatedIds);

            // Add meta_data
            JSONArray metaData = new JSONArray();

            JSONObject metaData1 = new JSONObject();
            metaData1.put("id", 41);
            metaData1.put("key", "_subscription_payment_sync_date");
            metaData1.put("value", "0");
            metaData.put(metaData1);

            JSONObject metaData2 = new JSONObject();
            metaData2.put("id", 42);
            metaData2.put("key", "_subscription_price");
            metaData2.put("value", "10");
            metaData.put(metaData2);

            JSONObject metaData3 = new JSONObject();
            metaData3.put("id", 46);
            metaData3.put("key", "_subscription_trial_length");
            metaData3.put("value", "0");
            metaData.put(metaData3);

            JSONObject metaData4 = new JSONObject();
            metaData4.put("id", 47);
            metaData4.put("key", "_subscription_sign_up_fee");
            metaData4.put("value", "5");
            metaData.put(metaData4);


            JSONObject metaData5 = new JSONObject();
            metaData5.put("id", 48);
            metaData5.put("key", "_subscription_period");
            metaData5.put("value", "month");
            metaData.put(metaData5);


            JSONObject metaData6 = new JSONObject();
            metaData6.put("id", 49);
            metaData6.put("key", "_subscription_period_interval");
            metaData6.put("value", "1");
            metaData.put(metaData6);

            JSONObject metaData7 = new JSONObject();
            metaData7.put("id", 50);
            metaData7.put("key", "_subscription_length");
            metaData7.put("value", "0");
            metaData.put(metaData7);

            JSONObject metaData8 = new JSONObject();
            metaData8.put("id", 51);
            metaData8.put("key", "_subscription_trial_period");
            metaData8.put("value", "day");
            metaData.put(metaData8);

            JSONObject metaData9 = new JSONObject();
            metaData9.put("id", 52);
            metaData9.put("key", "_subscription_limit");
            metaData9.put("value", "no");
            metaData.put(metaData9);

            // Add other meta_data entries as needed

            product.put("meta_data", metaData);

            JSONArray categories = new JSONArray();
            JSONObject category = new JSONObject();
            category.put("id", 9);
            categories.put(category);
            product.put("categories", categories);

            JSONArray images = new JSONArray();
            JSONObject image = new JSONObject();
            image.put("src", SHOP_BASE_URL + "wp-content/uploads/woocommerce-placeholder.png");
            images.put(image);
            product.put("images", images);
            product.put("price_html", "<span class=\"woocommerce-Price-amount amount\"><bdi><span class=\"woocommerce-Price-currencySymbol\">&euro;</span>" + String.format("%.2f", 10.00f) + "</bdi></span> <span class=\"subscription-details\"> every " + 2 + " months with a " + 7 + "-day free trial</span>");

            // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters(consumerKey);
            String signature = generateSignature("POST", SHOP_BASE_URL + "wp-json/wc/v3/products", oauthParams, consumerSecret);

            // Send POST request with RestAssured
            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(product.toString())
                    .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .post(SHOP_BASE_URL + "wp-json/wc/v3/products");

            // Print the response
            System.out.println("Response:");
            System.out.println(response.asString());
            if (response.getStatusCode() == 201) {
                // Get the product ID from the response JSON
                int productId = response.jsonPath().getInt("id");
                return productId;
            } else {
                System.out.println("Failed to create Simple Product. Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // Return -1 in case of an error
    }
    public static void createSubscriptionProduct_Test4Subs(){
        int id = WooCommerceV3APIs.createSimpleProductWithTest4SubsAndGetProductId(SHOP_BASE_URL, CONSUMER_SECRET, CONSUMER_KEY, "TEST-4 SUBS", 20.00f);

        if (id != -1) {
            WooCommerceV3APIs.updateProductToSubscription(SHOP_BASE_URL, CONSUMER_SECRET, CONSUMER_KEY, id);
            System.out.println("Successfully created and converted product to subscription. Product ID: " + id);
        } else {
            System.out.println("Failed to create a product or update it to a subscription.");
        }
    }

    public static void createSubscriptionProduct_Test1Subs(){
        int id = WooCommerceV3APIs.createSimpleProductWithTest1SubsAndGetProductId(SHOP_BASE_URL, CONSUMER_SECRET, CONSUMER_KEY);

        if (id != -1) {
            WooCommerceV3APIs.updateProductToSubscription(SHOP_BASE_URL, CONSUMER_SECRET, CONSUMER_KEY, id);
            System.out.println("Successfully created and converted product to subscription. Product ID: " + id);
        } else {
            System.out.println("Failed to create a product or update it to a subscription.");
        }
    }

    public static void createSubscriptionProduct_Test2Subs(){
        int id = WooCommerceV3APIs.createSimpleProductWithTest2SubsAndGetProductId(SHOP_BASE_URL, CONSUMER_SECRET, CONSUMER_KEY);

        if (id != -1) {
            WooCommerceV3APIs.updateProductToSubscription(SHOP_BASE_URL, CONSUMER_SECRET, CONSUMER_KEY, id);
            System.out.println("Successfully created and converted product to subscription. Product ID: " + id);
        } else {
            System.out.println("Failed to create a product or update it to a subscription.");
        }
    }
    public static void updateProductToSubscription(String SHOP_BASE_URL, String consumerSecret, String consumerKey, int productId) {
        try {
            JSONObject productUpdate = new JSONObject();
            productUpdate.put("type", "subscription");

            // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters(consumerKey);
            String signature = generateSignature("PATCH", SHOP_BASE_URL + "wp-json/wc/v3/products/" + productId, oauthParams, consumerSecret);

            // Send PATCH request with RestAssured
            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(productUpdate.toString())
                    .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .patch(SHOP_BASE_URL + "wp-json/wc/v3/products/" + productId);

            // Print the response
            System.out.println("Response:");
            System.out.println(response.asString());
            if (response.getStatusCode() == 200) {
                // Product update was successful
            } else {
                System.out.println("Failed to update product. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getUserIdFromResponse(String responseJson) {
        try {
            JSONObject jsonResponse = new JSONObject(responseJson);
            if (jsonResponse.has("id")) {
                return jsonResponse.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void updateRoleToAdministrator(String baseUrl, int userId) {
        // Define the endpoint for the specific user by appending the user ID
        String updateUserEndpoint = baseUrl + "wp-json/wp/v3/customers/" + userId;


        // Create a JSONObject for the role update data
        JSONObject requestBody = new JSONObject();
        requestBody.put("roles", new String[]{"administrator"});

        // Create a request specification
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());


        // Make a patch request to update the user role
        Response response = request.put(updateUserEndpoint);

        // Check the response status code
        if (response.getStatusCode() == 200) {
            System.out.println("User role updated to administrator successfully.");
        } else {
            System.out.println("Failed to update user role. Status code: " + response.getStatusCode());
        }
    }

    private static String generateSignature(String method, String url, Map<String, String> oauthParams, String consumerSecret) throws Exception {
        // Combine and encode the parameters
        List<String> parameterPairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : oauthParams.entrySet()) {
            parameterPairs.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        Collections.sort(parameterPairs);
        String parameterString = String.join("&", parameterPairs);

        // Construct the signature base string
        String signatureBaseString = method.toUpperCase() + "&" + URLEncoder.encode(url, "UTF-8") + "&" + URLEncoder.encode(parameterString, "UTF-8");

        // Generate the signature
        SecretKeySpec signingKey = new SecretKeySpec((CONSUMER_SECRET + "&").getBytes("UTF-8"), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        byte[] signatureBytes = mac.doFinal(signatureBaseString.getBytes("UTF-8"));

        // Encode the signature using Apache Commons Codec
        String signature = new String(Base64.encodeBase64(signatureBytes));

        return signature;
    }

    private static Map<String, String> generateOAuthParameters() {
        Map<String, String> oauthParams = new HashMap<>();
        oauthParams.put("oauth_consumer_key", CONSUMER_KEY);
        oauthParams.put("oauth_signature_method", "HMAC-SHA1");
        oauthParams.put("oauth_version", "1.0");
        oauthParams.put("oauth_nonce", UUID.randomUUID().toString());
        oauthParams.put("oauth_timestamp", Long.toString(System.currentTimeMillis() / 1000));

        return oauthParams;
    }

    private static Map<String, String> generateOAuthParameters(String consumerKey) {
        Map<String, String> oauthParams = new HashMap<>();
        oauthParams.put("oauth_consumer_key", consumerKey);
        oauthParams.put("oauth_signature_method", "HMAC-SHA1");
        oauthParams.put("oauth_version", "1.0");
        oauthParams.put("oauth_nonce", UUID.randomUUID().toString());
        oauthParams.put("oauth_timestamp", Long.toString(System.currentTimeMillis() / 1000));

        return oauthParams;
    }

    private static String generateAuthorizationHeader(Map<String, String> oauthParams, String signature) throws UnsupportedEncodingException {
        StringBuilder header = new StringBuilder("OAuth ");
        for (Map.Entry<String, String> entry : oauthParams.entrySet()) {
            header.append(entry.getKey()).append("=\"").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("\", ");
        }
        header.append("oauth_signature=\"").append(URLEncoder.encode(signature, "UTF-8")).append("\"");
        return header.toString();
    }

    private static String mapToJson(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        // Remove the trailing comma
        if (json.charAt(json.length() - 1) == ',') {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }



    @Step
    public static Map<String, String> getOrderDetails(String orderNum,String shopBaseURL,String consumerKey,String consumerSecret) {
        Map<String, String> orderDetails = new HashMap<>();

        try {
            // Generate OAuth parameters and signature
            Map<String, String> oauthParams = generateOAuthParameters(consumerKey);
            String signature = generateSignature("GET", shopBaseURL + "wp-json/wc/v3/orders/" + orderNum, oauthParams, consumerSecret);

            // Send GET request with RestAssured
            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body("")
                    .header("Authorization", generateAuthorizationHeader(oauthParams, signature))
                    .get(shopBaseURL + "wp-json/wc/v3/orders/" + orderNum);

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response.asString());

            // Extract specific fields and store them in a map
            orderDetails.put("payment_method_title", jsonResponse.optString("payment_method_title"));
            String totalWithCurrency = jsonResponse.optString("currency_symbol") + jsonResponse.optString("total");
            orderDetails.put("total_with_currency", totalWithCurrency);
            orderDetails.put("customer_note", jsonResponse.optString("customer_note"));

        } catch (Exception e) {
            // Handle or log the exception appropriately
            e.printStackTrace();
        }

        return orderDetails;
    }


}



