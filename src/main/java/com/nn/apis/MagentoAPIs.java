package com.nn.apis;
import com.nn.utilities.Log;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.nn.Magento.Constants.*;

import static com.nn.callback.CallbackProperties.*;


public class MagentoAPIs {





    private static final String CONTENT_TYPE = "application/json";
    private static final int MAX_RETRIES = 5;

    private static ThreadLocal<String> customerEmail = new ThreadLocal<>();

    public static void setCustomerEmail(String email){
        customerEmail.set(email);
    }

    public static String getCustomerEmail(){
        return customerEmail.get();
    }
    private static ThreadLocal<String> customerID = new ThreadLocal<>();

    public static void setCustomerID(String ID){
        customerID.set(ID);
    }

    public static String getCustomerID(){
        return customerID.get();
    }

    public static  String getAuthToken(String baseURI){
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .header("Authorization","Bearer "+BEARER_TOKEN)
                .body("{\"username\":\""+SHOP_BACKEND_USERNAME+"\",\"password\":\""+SHOP_BACKEND_PASSWORD+"\"}")
                .post("integration/admin/token")
                .then()
                .extract().response();
        if(response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to get the auth token for the Base URI {"+SHOP_BASE_URL+"} and for the username: {"+SHOP_BACKEND_USERNAME+"} and password: {"+SHOP_BACKEND_PASSWORD+"} and HTTP status code is "+response.getStatusCode());
        }
        System.out.println("Admin Token: "+response.asString());
        return response.asString();
    }

    public static synchronized String getCustomerToken(String baseURI,String payload){
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .body(payload)
                .post("integration/customer/token")
                .then()
                .extract().response();
        System.out.println("Customer Token: "+response.asString());
        if(response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to get the customer token for the Base URI {"+SHOP_BASE_URL+"} and HTTP status code is "+response.getStatusCode());
        }

        return response.asString();
    }


    public static  Response createCustomerAccount(String baseURI, String payLoad){
        String adminToken = getAdminToken(baseURI);
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .header("Authorization","Bearer " + adminToken)
                .body(payLoad)
                .post("customers")
                .then()
                .extract().response();
        if(response.getStatusCode() != 200) {
            System.out.println("Customer details: \n"+response.asPrettyString());
            throw new RuntimeException("Failure to get the create customer account for the Base URI {"+SHOP_BASE_URL+"} and for the payload{"+payLoad+"} and HTTP status code is "+response.getStatusCode());
        }
        System.out.println("Customer details: \n"+response.asPrettyString());
        setCustomerEmail(response.jsonPath().getString("email"));
        setCustomerID(response.jsonPath().getString("id"));
        return response;
    }

    public static String getAdminToken(String baseURI){
        String body = "{"
                + "\"username\":\"" + SHOP_BACKEND_USERNAME + "\","
                + "\"password\":\"" + SHOP_BACKEND_PASSWORD + "\""
                + "}";
       String response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .body(body)
                .header("Authorization","Bearer " + BEARER_TOKEN)
                .post("integration/admin/token")
                .then()
                .extract()
                .response().asString();
        String token = response.substring(1,response.length()-1);
       return token;
    }

    public static  Response createCustomerAccount(String baseURI, String payLoad, String email){
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .body(payLoad)
                .post("customers")
                .then()
                .extract().response();
        if(response.getStatusCode() != 200) {
            System.out.println("Customer details: \n"+response.asPrettyString());
            //throw new RuntimeException("Failure to get the create customer account for the Base URI {"+SHOP_BASE_URL+"} and for the payload{"+payLoad+"} and HTTP status code is "+response.getStatusCode());
            setCustomerEmail(email);
            Log.info("Customer already exist with this email: "+email);
        }else{
            System.out.println("Customer details: \n"+response.asPrettyString());
            setCustomerEmail(response.jsonPath().getString("email"));
            setCustomerID(response.jsonPath().getString("id"));
        }
        return response;
    }

    public static  Response createPaypalCustomerAccount(String baseURI, String payLoad){
        Response response = null;
        try{
                     response = RestAssured.given()
                    .baseUri(baseURI)
                    .contentType(CONTENT_TYPE)
                    .body(payLoad)
                    .post("customers")
                    .then()
                    .extract().response();
            if(response.getStatusCode() != 200) {
                System.out.println("Customer details: \n"+response.asPrettyString());
               // throw new RuntimeException("Failure to get the create customer account for the Base URI {"+SHOP_BASE_URL+"} and for the payload{"+payLoad+"} and HTTP status code is "+response.getStatusCode());
                Log.info("User already exist ");
            }
            System.out.println("Customer details: \n"+response.asPrettyString());
            setCustomerEmail("ecesankar93@gmail.com");
        }catch (Exception e){
            Log.info("User already exist ");
            setCustomerEmail("ecesankar93@gmail.com");
        }

        return response;
    }

    public static Response getOrder(String baseURI, int orderID){
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                .get("orders/"+orderID)
                .then()
                .extract().response();

        System.out.println("Order details: \n"+response.asPrettyString());
        if(response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to get the order details for the order number "+orderID+" and HTTP status code is "+response.getStatusCode());
        }
        return response;
    }

    public static Response getStock(String baseURI, String productSKU){
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                .get("stockStatuses/"+productSKU)
                .then()
                .extract().response();

        System.out.println("Stock details: \n"+response.asPrettyString());
        if(response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to get the stock details for the product SKU "+productSKU+" and HTTP status code is "+response.getStatusCode());
        }
        return response;
    }

    public static Response updateStock(String baseURI, String productSKU, int qty) {
        int retryCount = 0;
        Response response = null;

        while (retryCount <= MAX_RETRIES) {
            try {
                Response productDetails = getStock(baseURI, productSKU);
                int itemId = productDetails.jsonPath().get("stock_item.item_id");
                int currentDefaultStockQuantity = productDetails.jsonPath().getInt("stock_item.qty");

                // Check if the current default stock is less than 100, if yes update default stock
                if (currentDefaultStockQuantity <= 500) {
                    int newDefaultStockQuantity = currentDefaultStockQuantity + qty + 500;
                    System.out.println("Default stock is less than or equal to 100");
                    String payload = "{\"stockItem\":{\"qty\":" + newDefaultStockQuantity + ", \"is_in_stock\": true}}";
                    RequestSpecification requestSpec = RestAssured.given()
                            .baseUri(baseURI)
                            .contentType(CONTENT_TYPE)
                            .header("Authorization", "Bearer " + getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                            .body(payload);

                    response = requestSpec.put("products/" + productSKU + "/stockItems/" + itemId)
                            .then()
                            .extract().response();

                    if (response.getStatusCode() == 200) {
                        return response;
                    }
                } else {
                   System.out.println("Default stock is greater than 100");
                    return productDetails;
                }

                retryCount++;
            } catch (Exception e) {
                retryCount++;
                e.printStackTrace();
            }
        }

        throw new RuntimeException("Failure to update the product details for the product SKU " + productSKU +
                " after multiple attempts. Last HTTP status code received: " + (response != null ? response.getStatusCode() : "unknown"));
    }



    public static Response updatePrice(String baseURI, String productSKU, double price) {
        int retryCount = 0;
        Response response = null;

        while (retryCount <= MAX_RETRIES) {
            try {
                Response productDetails = getProductDetails(baseURI, productSKU);
                double currentPrice = Double.parseDouble(productDetails.jsonPath().get("price").toString());
                // Convert the price to the correct format (dividing by cents 100)
                double newPrice = price / 100.0;
                // Update the price only if it is different from the current price
                if (currentPrice != newPrice) {
                    System.out.println("Updating price for product SKU: " + productSKU);
                    String payload = "{\"product\": {\"price\": " + newPrice + "}}";
                    RequestSpecification requestSpec = RestAssured.given()
                            .baseUri(baseURI)
                            .contentType(CONTENT_TYPE)
                            .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                            .body(payload);

                    response = requestSpec.put("products/" + productSKU)
                            .then()
                            .extract().response();

                    if (response.getStatusCode() == 200) {
                        return response;
                    }
                } else {
                    System.out.println("The new price is the same as the current price.");
                    return productDetails;
                }

                retryCount++;
            } catch (Exception e) {
                retryCount++;
                e.printStackTrace();
            }
        }

        throw new RuntimeException("Failed to update the price for product SKU: " + productSKU +
                " after multiple attempts. Last HTTP status code received: " + (response != null ? response.getStatusCode() : "unknown"));
    }

    public static Response getProductDetails(String baseURI, String productSKU) {
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                .get("products/"+productSKU)
                .then()
                .extract().response();

        System.out.println("Product details: \n"+response.asPrettyString());
        if(response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to get the product details for the sku "+productSKU+" and HTTP status code is "+response.getStatusCode());
        }
        return response;
    }
    public static Response getInvoice(String baseURI, int invoiceID){
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                .get("invoices/"+invoiceID)
                .then()
                .extract().response();

        System.out.println("Invoice details: \n"+response.asPrettyString());
        if(response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to get the invoice details for the invoice number "+invoiceID+" and HTTP status code is "+response.getStatusCode());
        }
        return response;
    }

    public static Response getCart(String baseURI, String customerToken){
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .accept(CONTENT_TYPE)
                .header("Authorization","Bearer "+customerToken.replaceAll("^\"|\"$", ""))
                .get("carts/mine/items")
                .then()
                .extract().response();

        System.out.println("Cart details: \n"+response.asPrettyString());
//        if(response.getStatusCode() != 200) {
//            throw new RuntimeException("Failure to get the cart details and HTTP status code is "+response.getStatusCode());
//        }
        return response;
    }

    public static Response deleteCart(String baseURI, String customerToken, int itemID){
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .header("Authorization","Bearer "+customerToken.replaceAll("^\"|\"$", ""))
                .delete("carts/mine/items/"+itemID)
                .then()
                .extract().response();

        System.out.println("Cart deletion status: "+response.asPrettyString());
        if(response.getStatusCode() != 200) {
           // throw new RuntimeException("Failure to get the cart details and HTTP status code is "+response.getStatusCode());
        }
        return response;
    }

    public static synchronized Response addToCart(String baseURI, String customerToken, String payload){
        int retryCount = 0;
        Response response = null;


        while (retryCount <= MAX_RETRIES) {
            try {
                RequestSpecification requestSpec = RestAssured.given()
                        .baseUri(baseURI)
                        .contentType(CONTENT_TYPE)
                        .accept(CONTENT_TYPE)
                        .header("Authorization", "Bearer " + customerToken.replaceAll("^\"|\"$", ""))
                        .body(payload);

                response = requestSpec.post("carts/mine/items")
                        .then()
                        .extract().response();

                if (response.getStatusCode() == 200) {
                    return response;
                }

                retryCount++;
            } catch (Exception e) {
                retryCount++;
                e.printStackTrace();
            }
        }

        throw new RuntimeException("Failure to add the product to the cart after multiple attempts. " +
                "Last HTTP status code received: " + response.getStatusCode());

    }

    public static String getQuoteID(String baseURI, String customerToken){
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .header("Authorization","Bearer "+customerToken.replaceAll("^\"|\"$", ""))
                .post("carts/mine")
                .then()
                .extract().response();

        System.out.println("Quote ID: "+response.asString());
        if(response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to get the Quote details and HTTP status code is "+response.getStatusCode());
        }
        return response.asString();
    }



    public static String getCustomerTokenPayload(Response response){
        JsonPath json = response.jsonPath();
        String payload = "{\"username\":\""+json.getString("email")+"\",\"password\":\"Novalnet@123\"}";
        System.out.println(payload);
        return payload;
    }

    public static String getCustomerTokenPayload(String email){
        String payload = "{\"username\":\""+email+"\",\"password\":\"Novalnet@123\"}";
        System.out.println(payload);
        return payload;
    }

    public static String addToCartPayload(String sku, int qty, String quoteID){
        String payload = "{ \"cartItem\": { \"sku\": \"" + sku + "\", \"qty\": " + qty + ", \"quote_id\": \"" + quoteID + "\" } }";
        System.out.println(payload);
        return payload;
    }

    public static String customerPayload_diff_bill_ship_add(String email) {
        return "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Norbert\",\n" +
                "    \"lastname\": \"Maier\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Kaiserslautern\",\n" +
                "        \"country_id\": \"DE\",\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"postcode\": \"66862\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"DE\",\n" +
                "          \"region\": \"Germany\",\n" +
                "          \"region_id\": 80\n" +
                "        },\n" +
                "        \"street\": [\n" +
                "          \"9, Hauptstr\"\n" +
                "        ],\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"lastname\": \"Mustermann\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"city\": \"Musterhausen\",\n" +
                "        \"country_id\": \"DE\",\n" +
                "        \"default_billing\": false,\n" +
                "        \"default_shipping\": false,\n" +
                "        \"postcode\": \"12345\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"DE\",\n" +
                "          \"region\": \"Germany\",\n" +
                "          \"region_id\": 81\n" +
                "        },\n" +
                "        \"street\": [\n" +
                "          \"2, Musterstr\"\n" +
                "        ],\n" +
                "        \"telephone\": \"+49 (0)89 987654\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"lastname\": \"Mustermann\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\"\n" +
                "}";
}


    public static String customerPayload(String email){
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Max\",\n" +
                "    \"lastname\": \"Mustermann\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Musterhausen\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"DE\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Mustermann\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"12345\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"DE\",\n" +
                "          \"region\": \"Germany\",\n" +
                "          \"region_id\": 80,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 80,\n" +
                "        \"street\": [\n" +
                "          \"2, Musterstr\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
               "    \"gender\": 0,\n" +
              "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }

    /*public static String customerPayload(String email) {
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Jane\",\n" +
                "    \"lastname\": \"Doe\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"defaultShipping\": true,\n" +
                "        \"defaultBilling\": true,\n" +
                "        \"firstname\": \"Jane\",\n" +
                "        \"lastname\": \"Doe\",\n" +
                "        \"region\": {\n" +
                "          \"regionCode\": \"NY\",\n" +
                "          \"region\": \"New York\",\n" +
                "          \"regionId\": 43\n" +
                "        },\n" +
                "        \"postcode\": \"10755\",\n" +
                "        \"street\": [\n" +
                "          \"123 Oak Ave\"\n" +
                "        ],\n" +
                "        \"city\": \"Purchase\",\n" +
                "        \"telephone\": \"512-555-1111\",\n" +
                "        \"countryId\": \"US\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\"\n" +
                "}";
        return requestBody;
    }*/


    public static String customerPayload_PT(String email){
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Max\",\n" +
                "    \"lastname\": \"Mustermann\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Musterhausen\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"PT\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Mustermann\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"12345\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"PT\",\n" +
                "          \"region\": \"Portugal\",\n" +
                "          \"region_id\": 1027,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 1027,\n" +
                "        \"street\": [\n" +
                "          \"2, Musterstr\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
              /*  "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }

    public static String customerPayload_CH(String email){
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Max\",\n" +
                "    \"lastname\": \"Mustermann\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Musterhausen\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"CH\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Mustermann\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"12345\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"CH\",\n" +
                "          \"region\": \"Switzerland\",\n" +
                "          \"region_id\": 104,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 104,\n" +
                "        \"street\": [\n" +
                "          \"2, Musterstr\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
           /*     "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }
    public static  String customerPayload_BE(String email){
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Max\",\n" +
                "    \"lastname\": \"Mustermann\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Musterhausen\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"BE\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Mustermann\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"12345\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"BE\",\n" +
                "          \"region\": \"Belgium\",\n" +
                "          \"region_id\": 0,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 0,\n" +
                "        \"street\": [\n" +
                "          \"2, Musterstr\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
             /*   "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }

    public static  String getCustomerPayload_NL(String email){
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Max\",\n" +
                "    \"lastname\": \"Mustermann\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Amsterdam\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"NL\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Mustermann\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"1015 DS\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"NL\",\n" +
                "          \"region\": \"Netherlands\",\n" +
                "          \"region_id\": 0,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 0,\n" +
                "        \"street\": [\n" +
                "          \"Prinsengracht 191\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
            /*    "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }

    public static  String getCustomerPayload_AT(String email){
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Max\",\n" +
                "    \"lastname\": \"Mustermann\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Vienna\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"AT\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Mustermann\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"1010\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"AT\",\n" +
                "          \"region\": \"Austria\",\n" +
                "          \"region_id\": 0,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 0,\n" +
                "        \"street\": [\n" +
                "          \"Graben 30\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
           /*     "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }

    public static  String getCustomerPayload_CN(String email) {
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Max\",\n" +
                "    \"lastname\": \"Mustermann\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Beijing\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"CN\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Mustermann\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"100000\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"CN\",\n" +
                "          \"region\": \"Beijing Shi\",\n" +
                "          \"region_id\": 684,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 684,\n" +
                "        \"street\": [\n" +
                "          \"123 Main Street\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
             /*   "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }


    public static  String getCustomerPayload_B2C(String email){
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Norbert\",\n" +
                "    \"lastname\": \"Maier\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Kaiserslautern\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"DE\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Norbert\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Maier\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"66862\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"DE\",\n" +
                "          \"region\": \"Germany\",\n" +
                "          \"region_id\": 80,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 0,\n" +
                "        \"street\": [\n" +
                "          \"9, Hauptstr\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
              /*  "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }

    public static  String getCustomerPayload_B2B(String email){
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Company\",\n" +
                "    \"lastname\": \"Germany\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Mainz\",\n" +
                "        \"company\": \"A.B.C. Gerüstbau GmbH\",\n" +
                "        \"country_id\": \"DE\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Company\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Germany\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"55120\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"DE\",\n" +
                "          \"region\": \"Germany\",\n" +
                "          \"region_id\": 80,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 80,\n" +
                "        \"street\": [\n" +
                "          \"23, Industriestraße\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
              /*  "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }

    public static String customerPayload_PL(String email){
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Max\",\n" +
                "    \"lastname\": \"Mustermann\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Musterhausen\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"PL\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Mustermann\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"12345\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"PL\",\n" +
                "          \"region\": \"Poland\",\n" +
                "          \"region_id\": 1017,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 1017,\n" +
                "        \"street\": [\n" +
                "          \"2, Musterstr\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
                /*"    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }

    public static String updateGuaranteeB2BDEAddressPayload(String cusId) {
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"firstname\": \"Company\",\n" +
                "    \"lastname\": \"Germany\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Mainz\",\n" +
                "        \"company\": \"A.B.C. Gerüstbau GmbH\",\n" +
                "        \"country_id\": \"DE\",\n" +
                "        \"custom_attributes\": {},\n" +
                "        \"customer_id\": " + cusId + ",\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Company\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Germany\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"55120\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"DE\",\n" +
                "          \"region\": \"Germany\",\n" +
                "          \"region_id\": 80,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 80,\n" +
                "        \"street\": [\n" +
                "          \"23, Industriestraße\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+49 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    }\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }
    public static  String getCustomerPayload_US(String email) {
        String requestBody = "{\n" +
                "  \"customer\": {\n" +
                "    \"email\": \"" + email + "\",\n" +
                "    \"firstname\": \"Max\",\n" +
                "    \"lastname\": \"Mustermann\",\n" +
                "    \"addresses\": [\n" +
                "      {\n" +
                "        \"city\": \"Miami\",\n" +
                "        \"company\": \"\",\n" +
                "        \"country_id\": \"US\",\n" +
                "        \"custom_attributes\": [],\n" +
                "        \"customer_id\": 0,\n" +
                "        \"default_billing\": true,\n" +
                "        \"default_shipping\": true,\n" +
                "        \"extension_attributes\": {},\n" +
                "        \"fax\": \"123456789\",\n" +
                "        \"firstname\": \"Max\",\n" +
                "        \"id\": 0,\n" +
                "        \"lastname\": \"Mustermann\",\n" +
                "        \"middlename\": \"\",\n" +
                "        \"postcode\": \"100000\",\n" +
                "        \"prefix\": \"\",\n" +
                "        \"region\": {\n" +
                "          \"region_code\": \"FL\",\n" +
                "          \"region\": \"Florida\",\n" +
                "          \"region_id\": 18,\n" +
                "          \"extension_attributes\": {}\n" +
                "        },\n" +
                "        \"region_id\": 18,\n" +
                "        \"street\": [\n" +
                "          \"123 Main Street\"\n" +
                "        ],\n" +
                "        \"suffix\": \"\",\n" +
                "        \"telephone\": \"+1 (0)89 123456\",\n" +
                "        \"vat_id\": \"\"\n" +
                "      }\n" +
                "    ],\n" +
             /*   "    \"confirmation\": \"\",\n" +
                "    \"created_at\": \"\",\n" +
                "    \"created_in\": \"\",\n" +
                "    \"custom_attributes\": [],\n" +
                "    \"default_billing\": \"\",\n" +
                "    \"default_shipping\": \"\",\n" +
                "    \"disable_auto_group_change\": 0,\n" +
                "    \"dob\": \"\",\n" +
                "    \"extension_attributes\": {\n" +
                "      \"is_subscribed\": false\n" +
                "    },\n" +
                "    \"gender\": 0,\n" +*/
                "    \"group_id\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"middlename\": \"\",\n" +
                "    \"prefix\": \"\",\n" +
                "    \"store_id\": 0,\n" +
                "    \"suffix\": \"\",\n" +
                "    \"taxvat\": \"\",\n" +
                "    \"updated_at\": \"\",\n" +
                "    \"website_id\": 0\n" +
                "  },\n" +
                "  \"password\": \"Novalnet@123\",\n" +
                "  \"redirectUrl\": \"\"\n" +
                "}";
        return requestBody;
    }




    public static Response updateGuaranteeB2BDECustomerAddress(String baseURI, String customerId, String payLoad) {
        Response response = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                .body(payLoad)
                .put("customers/"+customerId)
                .then()
                .extract().response();
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to update address for customer ID: " + customerId + ". HTTP status code is " + response.getStatusCode());
        }

        System.out.println("Updated address details:\n" + response.asPrettyString());

        return response;
    }


    public static  String getCustomerPayload(String paymentType,String customerEmail) {
        switch (paymentType) {
            case PAYPAL:
            case CREDITCARD:
            case DIRECT_DEBIT_SEPA:
            case PREPAYMENT:
            case CASHPAYMENT:
            case ONLINE_TRANSFER:
            case ONLINE_BANK_TRANSFER:
            case INVOICE:
            case GIROPAY:
            case TRUSTLY:
            case GOOGLEPAY:
                return customerPayload(customerEmail);
            case PRZELEWY24:
                return customerPayload_PL(customerEmail);
            case MULTIBANCO:
                return customerPayload_PT(customerEmail);
            case POSTFINANCE_CARD:
                return customerPayload_CH(customerEmail);
           case BANCONTACT:
                return customerPayload_BE(customerEmail);
            case IDEAL:
                return getCustomerPayload_NL(customerEmail);
            case GUARANTEED_INVOICE:
            case INSTALMENT_INVOICE:
            case GUARANTEED_DIRECT_DEBIT_SEPA:
            case INSTALMENT_DIRECT_DEBIT_SEPA:
                return getCustomerPayload_B2C(customerEmail);
            case "GUARANTEED_INVOICE_B2B":
            case "INSTALMENT_INVOICE_B2B":
            case "GUARANTEED_DIRECT_DEBIT_SEPA_B2B":
            case "INSTALMENT_DIRECT_DEBIT_SEPA_B2B":
                return getCustomerPayload_B2B(customerEmail);
            case EPS:
                return getCustomerPayload_AT(customerEmail);
            case ALIPAY:
            case WECHATPAY:
                return getCustomerPayload_CN(customerEmail);
            case "DIFF_BILLING_SHIPPING":
                return customerPayload_diff_bill_ship_add(customerEmail);
            case DIRECT_DEBIT_ACH:
                return getCustomerPayload_US(customerEmail);
            case BLIK:
                return customerPayload_PL(customerEmail);
            case PAYCONIQ:
                return customerPayload_BE(customerEmail);
            case MBWAY:
                return customerPayload_PT(customerEmail);
            default:
                throw new IllegalArgumentException("Invalid payment method: " + paymentType);
        }
    }

    public static synchronized String generateRandomEmail() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        String random = uuidString.substring(uuidString.length() - 6);
        return "test_"+getCurrentDateTimeCustom()+"_"+random+"@gmail.com";
    }

    public static synchronized String getCurrentDateTimeCustom() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
        return formatter.format(now);
    }



    public static void printAddressDetails(String baseURI, String customerId) {
        String endpoint = "customers/" + customerId + "/billingAddress";

        Response response = RestAssured.given()
                .baseUri(baseURI)
                .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                .contentType(CONTENT_TYPE)
                .get(endpoint)
                .then()
                .extract().response();

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to get address details for customer ID: " + customerId  + ". HTTP status code is " + response.getStatusCode());
        }

        System.out.println("Address details:\n" + response.asPrettyString());
    }

    // Method to create a downloadable product
    public static Response createDownloadableProduct(String sku, String name, double price, int stockQty) {
        String payload = "{\n" +
                "  \"product\": {\n" +
                "    \"sku\": \"" + sku + "\",\n" +
                "    \"name\": \"" + name + "\",\n" +
                "    \"type_id\": \"downloadable\",\n" +
                "    \"attribute_set_id\": 4,\n" +
                "    \"price\": " + price + ",\n" +
                "    \"status\": 1,\n" +
                "    \"visibility\": 4,\n" +
                "    \"extension_attributes\": {\n" +
                "      \"downloadable_product_links\": [\n" +
                "        {\n" +
                "          \"title\": \"Download Link 1\",\n" +
                "          \"link_type\": \"file\",\n" +
                "          \"is_shareable\": 1,\n" +
                "          \"link_file\": \"/n/o/novalnetlogo.png\",\n" +
                "          \"price\": 9.99,\n" +
                "          \"number_of_downloads\": 5,\n" +
                "          \"sort_order\": 1\n" +
                "        }\n" +
                "      ],\n" +
                "      \"stock_item\": {\n" +
                "        \"qty\": " + stockQty + ",\n" +
                "        \"is_in_stock\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Response response = createProduct(payload, SHOP_BASE_URL);



        return response ;
    }

    // Method to create a virtual product
    public static Response createVirtualProduct(String sku, String name, double price, int stockQty) {
        String payload = "{"
                + "\"product\": {"
                + "\"sku\": \"" + sku + "\","
                + "\"name\": \"" + name + "\","
                + "\"type_id\": \"virtual\","
                + "\"attribute_set_id\": 4," // Replace with your attribute set ID
                + "\"price\": " + price + ","
                + "\"status\": 1,"
                + "\"visibility\": 4,"
                + "\"extension_attributes\": {"
                + "    \"stock_item\": {"
                + "        \"qty\": " + stockQty + ","
                + "        \"is_in_stock\": true"
                + "    }"
                + "}"
                + "}"
                + "}";

        Response response = createProduct(payload, SHOP_BASE_URL);



        return response;
    }


    private static Response createProduct(String payload, String baseURI) {
        RequestSpecification requestSpec = RestAssured.given()
                .baseUri(baseURI)
                .contentType("application/json")
                .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                .body(payload);

        return requestSpec.post("products")
                .then()
                .extract().response();
    }



    public static boolean isLinkUrlValid(String linkUrl) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(linkUrl);

        try {
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();

            // Check if the status code is 200 (OK)
            if (statusCode == 200) {
                // File is accessible and exists
                System.out.println("file accessible");
                return true;
            } else {
                // File is not accessible or does not exist
                System.out.println("file not accessible");
                return false;
            }
        } catch (IOException e) {
            // An exception occurred, indicating an issue with the URL
            return false;
        }
        }


    public static  Response createGooglePayCustomerAccount(String baseURI, String payLoad){
        Response response = null;
        try{
            response = RestAssured.given()
                    .baseUri(baseURI)
                    .contentType(CONTENT_TYPE)
                    .body(payLoad)
                    .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                    .post("customers")
                    .then()
                    .extract().response();
            if(response.getStatusCode() != 200) {
                System.out.println("Customer details: \n"+response.asPrettyString());
                // throw new RuntimeException("Failure to get the create customer account for the Base URI {"+SHOP_BASE_URL+"} and for the payload{"+payLoad+"} and HTTP status code is "+response.getStatusCode());
                Log.info("User already exist ");
            }
            System.out.println("Customer details: \n"+response.asPrettyString());
            setCustomerEmail("novalnetesolutions2010@gmail.com");
        }catch (Exception e){
            Log.info("User already exist ");
            setCustomerEmail("novalnetesolutions2010@gmail.com");
        }

        return response;
    }

    }
