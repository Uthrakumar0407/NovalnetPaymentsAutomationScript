package com.nn.testcase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MagentoApiExample {

    static String authToken9="";
    public static void main(String[] args) {
        //createAccounAddtoCart();
     //   getOrder(27);
      //  getInvoice(13);
        getRegion();
    }


    public static void getRegion() {
        String url = "http://192.168.2.91/gopinath_m/Shops/magento246/pub/rest/V1/directory/countries/PT";
        String authToken = getAuthToken();

        try {
            // Create URL object
            URL requestUrl = new URL(url);

            // Create connection
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            String aToken = authToken.replaceAll("^\"|\"$", "");
            connection.setRequestProperty("Authorization", "Bearer " + aToken);

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Read the response
            BufferedReader reader;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            // Process the response
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Print the response
            System.out.println("Region ID Response Code: " + responseCode);
            System.out.println("Region ID Response: " + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getInvoice(int invoiceID ) {
        String url = "http://localhost/gopinath_m/Shops/magento246/pub/rest/V1/invoices/"+ invoiceID;
        String authToken = getAuthToken();

        try {
            // Create URL object
            URL requestUrl = new URL(url);

            // Create connection
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            String aToken = authToken.replaceAll("^\"|\"$", "");
            connection.setRequestProperty("Authorization", "Bearer " + aToken);

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Read the response
            BufferedReader reader;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            // Process the response
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Print the response
            System.out.println("Invoice Response Code: " + responseCode);
            System.out.println("Invoice Response: " + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createAccountTest(){
        String customerName = generateRandomEmail();
        Map<Integer, Integer> customerStoreMap = new HashMap<>();
        customerStoreMap = createAccount(customerName);
        // Retrieve the values from the map
        int customerId = 0;
        int storeId = 0;

        for (Map.Entry<Integer, Integer> entry : customerStoreMap.entrySet()) {
            customerId = entry.getKey();
            storeId = entry.getValue();
            break; // Assuming there is only one entry in the map
        }
        System.out.println("name  "+customerName + " pwd  "+ "N0V@LN3T@adm1n2");
        getCustomerToken(customerName,"N0V@LN3T@adm1n2",storeId);
    }

    public static void createAccounAddtoCart(){
        String customerName = generateRandomEmail();
        Map<Integer, Integer> customerStoreMap = new HashMap<>();
        customerStoreMap = createAccount(customerName);
        // Retrieve the values from the map
        int customerId = 0;
        int storeId = 0;

        for (Map.Entry<Integer, Integer> entry : customerStoreMap.entrySet()) {
            customerId = entry.getKey();
            storeId = entry.getValue();
            break; // Assuming there is only one entry in the map
        }
        System.out.println("name  "+customerName + " pwd  "+ "N0V@LN3T@adm1n2");
        String customerToken=getCustomerToken(customerName,"N0V@LN3T@adm1n2",storeId);
        int quoteID=Integer.parseInt(getQuoteId(customerToken,customerId,storeId));
        addToCart(customerId,"24-WB04",1, customerToken, customerName,quoteID);
        getCartId(customerId,storeId,authToken9);


    }

    public static Map<String, String> createAccounAddtoMagentoCart() {
        Map<String, String> credentials = new HashMap<>();

        String customerName = generateRandomEmail();
        Map<Integer, Integer> customerStoreMap = createAccount(customerName);

        // Retrieve the values from the map
        int customerId = 0;
        int storeId = 0;

        for (Map.Entry<Integer, Integer> entry : customerStoreMap.entrySet()) {
            customerId = entry.getKey();
            storeId = entry.getValue();
            break; // Assuming there is only one entry in the map
        }

        String customerPassword = "N0V@LN3T@adm1n2";
        System.out.println("name  " + customerName + " pwd  " + customerPassword);

        String customerToken = getCustomerToken(customerName, customerPassword, storeId);
        int quoteID = Integer.parseInt(getQuoteId(customerToken, customerId, storeId));

        addToCart(customerId, "24-WB04", 1, customerToken, customerName, quoteID);
        getCartId(customerId, storeId, authToken9);

        // Add customer email and password to the map
        credentials.put("email", customerName);
        credentials.put("password", customerPassword);

        return credentials;
    }

    public static String getAuthToken() {
        String authToken = "";
        try {
            String endpointUrl = "http://localhost/gopinath_m/Shops/magento246/pub/rest/V1/integration/admin/token";

            // Set the payload for authentication request
             String payload = "{\"username\":\"shopadmin\",\"password\":\"novalnet123\"}";
             // Create the URL object
            URL url = new URL(endpointUrl);
            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setRequestProperty("Authorization", "Bearer jb12sy9urxljvpma7zasqqp8gfoi10a4");
            /*connection.setRequestProperty("Authorization", "Bearer vfkr2xk8i394d2kbdcbfctzjte3u9jtz");*/
            connection.setDoOutput(true);

            // Send the request payload
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(payload.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                authToken = reader.readLine();
                reader.close();

                // Print the authentication token
                System.out.println("Authentication successful! Admin token: " + authToken);
            } else {
                System.out.println("Authentication failed. Response Code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();


        } catch (IOException e) {
            e.printStackTrace();
        }
return authToken;
    }

    public static String getCustomerToken(String username, String password, int storeCode){

        String customerToken = "";
        try {
            String endpointUrl = "http://localhost/gopinath_m/Shops/magento246/pub/rest/default/V1/integration/customer/token";

            // Set the payload for authentication request
            String payload = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

            // Create the URL object
            URL url = new URL(endpointUrl);

            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send the request payload
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(payload.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                customerToken = reader.readLine();
                reader.close();

                // Print the authentication token
                System.out.println("Authentication successful! Customer token: " + customerToken);
            } else {
                System.out.println("Authentication failed. Response Code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return customerToken;
    }

    public static String getQuoteId(String customerToken,int customerID, int storeID) {
        String quoteId = "";
        try {
            String endpointUrl = "http://localhost/gopinath_m/Shops/magento246/pub/rest/V1/carts/mine/";
                 // Create the URL object
            URL url = new URL(endpointUrl);
            String payLoad="{}";

            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            String custToken = customerToken.replaceAll("^\"|\"$", "");
            connection.setRequestProperty("Authorization", "Bearer " + custToken);

            connection.setDoOutput(true);
            // Send the request payload
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(payLoad.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                quoteId = reader.readLine();
                reader.close();

                // Print the quote ID
                System.out.println("Quote ID: " + quoteId);
            } else {
                System.out.println("Request failed. Response Code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return quoteId;
    }






    public static void getOrder(int orderID) {

        String url = "http://localhost/gopinath_m/Shops/magento246/pub/rest/V1/orders/"+ orderID;
        String authToken = getAuthToken();

        try {
            // Create URL object
            URL requestUrl = new URL(url);

            // Create connection
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            String aToken = authToken.replaceAll("^\"|\"$", "");
            connection.setRequestProperty("Authorization", "Bearer " + aToken);

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Read the response
            BufferedReader reader;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            // Process the response
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Print the response
            System.out.println("Order Response Code: " + responseCode);
            System.out.println("Order Response: " + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public static Map<Integer, Integer>  createAccount(String userName) {
        // Set the endpoint URL
        int customerId=0;
        int storeID=0;


      /*  String endpointUrl = "http://192.168.2.143/uthrakumar/Magento/magento246/pub/rest/V1/integration/admin/token";
        String createAccountEndpoint = "http://192.168.2.143/uthrakumar/Magento/magento246/pub/rest/V1/customers";
*/
        String endpointUrl = "http://localhost/gopinath_m/Shops/magento246/pub/rest/V1/integration/admin/token";
        String createAccountEndpoint = "http://localhost/gopinath_m/Shops/magento246/pub/rest/V1/customers";
       /* String endpointUrl = "https://magento2.novalnet.de/rest/V1/integration/admin/token";
        String createAccountEndpoint = "https://magento2.novalnet.de/rest/V1/customers";*/

        // Set the payload for authentication request
        /*String payload = "{\"username\":\"demoadmin\",\"password\":\"N0V@LN3T@adm1n2\"}";*/
        String payload = "{\"username\":\"shopadmin\",\"password\":\"novalnet123\"}";

        try {
            // Create the URL object
            URL url = new URL(endpointUrl);

            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            //uthirakumar shop
         //   connection.setRequestProperty("Authorization", "Bearer vpzgqswozv5qw8o0cw1ffqz7xfqrbu93");
            // my shop
            connection.setRequestProperty("Authorization", "Bearer jb12sy9urxljvpma7zasqqp8gfoi10a4");
            //demo shop
            /*connection.setRequestProperty("Authorization", "Bearer vfkr2xk8i394d2kbdcbfctzjte3u9jtz");*/

            connection.setDoOutput(true);

            // Send the request payload
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(payload.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                authToken9 = reader.readLine();
                reader.close();

                // Print the authentication token
                System.out.println("Authentication successful! Admin token: " + authToken9);
            } else {
                System.out.println("Authentication failed. Response Code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();


            // Step 2: Create a new customer account
            String requestBody = "{\n" +
                    "  \"customer\": {\n" +
                    "    \"email\": \"" + userName + "\",\n" +
                    "    \"firstname\": \"John\",\n" +
                    "    \"lastname\": \"Doe\",\n" +
                    "    \"addresses\": [\n" +
                    "      {\n" +
                    "        \"city\": \"Berlin\",\n" +
                    "        \"company\": \"ABC Company\",\n" +
                    "        \"country_id\": \"DE\",\n" +
                    "        \"custom_attributes\": [],\n" +
                    "        \"customer_id\": 0,\n" +
                    "        \"default_billing\": false,\n" +
                    "        \"default_shipping\": true,\n" +
                    "        \"extension_attributes\": {},\n" +
                    "        \"fax\": \"123456789\",\n" +
                    "        \"firstname\": \"John\",\n" +
                    "        \"id\": 0,\n" +
                    "        \"lastname\": \"Doe\",\n" +
                    "        \"middlename\": \"\",\n" +
                    "        \"postcode\": \"66862\",\n" +
                    "        \"prefix\": \"\",\n" +
                    "        \"region\": {\n" +
                    "          \"region_code\": \"DE\",\n" +
                    "          \"region\": \"Germany\",\n" +
                    "          \"region_id\": 0,\n" +
                    "          \"extension_attributes\": {}\n" +
                    "        },\n" +
                    "        \"region_id\": 0,\n" +
                    "        \"street\": [\n" +
                    "          \"123 Main St\"\n" +
                    "        ],\n" +
                    "        \"suffix\": \"\",\n" +
                    "        \"telephone\": \"045818858555\",\n" +
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
                    "  \"password\": \"N0V@LN3T@adm1n2\",\n" +
                    "  \"redirectUrl\": \"\"\n" +
                    "}";
            URL createAccountUrl = new URL(createAccountEndpoint);
            HttpURLConnection createAccountConnection = (HttpURLConnection) createAccountUrl.openConnection();
            createAccountConnection.setRequestMethod("POST");
            createAccountConnection.setRequestProperty("Authorization", "Bearer " + authToken9);
            createAccountConnection.setRequestProperty("Content-Type", "application/json");
            createAccountConnection.setDoOutput(true);

            byte[] requestBodyBytes = requestBody.getBytes(StandardCharsets.UTF_8);
            createAccountConnection.getOutputStream().write(requestBodyBytes);

            int createAccountResponseCode = createAccountConnection.getResponseCode();
            BufferedReader createAccountReader = new BufferedReader(new InputStreamReader(createAccountConnection.getInputStream()));
            StringBuilder createAccountResponseBody = new StringBuilder();
            String createAccountLine;
            while ((createAccountLine = createAccountReader.readLine()) != null) {
                createAccountResponseBody.append(createAccountLine);
            }
            createAccountReader.close();

            // Print the response
            //  System.out.println("Authentication Response Code: " + authResponseCode);
            System.out.println("Admin Token: " + authToken9);
            System.out.println("Create Account Response Code: " + createAccountResponseCode);
            System.out.println("Create Account Response Body: " + createAccountResponseBody.toString());



            customerId = extractCustomerId(createAccountResponseBody.toString());
            storeID=extractStoreId(createAccountResponseBody.toString());
            System.out.println("Customer ID: " + customerId);
            System.out.println("Store ID: " + storeID);
            // Close connections
            createAccountConnection.disconnect();


        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<Integer, Integer> customerStoreMap = new HashMap<>();
        customerStoreMap.put(customerId, storeID);

        return customerStoreMap;
    }






    public static int extractStoreId(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            int customerId = jsonResponse.getInt("store_id");
            return customerId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if customer ID extraction fails
    }

    public static int extractCustomerId(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            int customerId = jsonResponse.getInt("id");
            return customerId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if customer ID extraction fails
    }

    public static String generateTrimmedUuid() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        String trimmedUuid = uuidString.substring(uuidString.length() - 5);
        return trimmedUuid;
    }
    private static String generateRandomEmail() {
        String uuid =generateTrimmedUuid();
        String email = "admin-" + uuid + "@novalnet.com";
        return email;
    }
    public static void addToCart(){
        String customerName = generateRandomEmail();
        Map<Integer, Integer> customerStoreMap = new HashMap<>();
        customerStoreMap = createAccount(customerName);
        // Retrieve the values from the map
        int customerId = 0;
        int storeId = 0;

        for (Map.Entry<Integer, Integer> entry : customerStoreMap.entrySet()) {
            customerId = entry.getKey();
            storeId = entry.getValue();
            break; // Assuming there is only one entry in the map
        }


          // Add the product to the cart
          addProductToCart(String.valueOf(customerId), "MS08", getAuthToken(), getCartId(customerId,storeId,getAuthToken()));

    }

    public static String getCartId(int customerId, int storeId, String authToken) {
        String endpointUrl = "http://localhost/gopinath_m/Shops/magento246/pub/rest/V1/carts/mine/items";
        /*String payload = "{\"customerId\": " + customerId + ", \"storeId\": " + storeId + "}";*/
        String payload="{}";

        try {
            // Create the URL object
            URL url = new URL(endpointUrl);

            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            // Set the authorization token
            connection.setRequestProperty("Authorization", "Bearer " + authToken);

           /* // Set the payload
            connection.setDoOutput(true);
            connection.getOutputStream().write(payload.getBytes());*/

            // Get the response
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }
                reader.close();

                // Extract the cart ID from the response

                String cartId = responseBody.toString();
                System.out.println("Get cart response =" + responseBody.toString() + " Cart ID =" + cartId);
                return cartId;
            } else {
                System.out.println("Failed to get cart ID. Response Code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void addProductToCart(String customerName, String productSku, String authToken, String cartId) {
        // Set the endpoint URL for adding the product to the cart
        String addToCartEndpointUrl = "http://localhost/gopinath_m/Shops/magento246/pub/rest/V1/carts/" + cartId + "/items";

        try {
            // Set the payload for adding the product to the cart
            String payload = "{\n" +
                    "  \"cartItem\": {\n" +
                    "    \"sku\": \"" + productSku + "\",\n" +
                    "    \"qty\": 1,\n" +
                    "    \"quoteId\": \"" + cartId + "\"\n" +
                    "  }\n" +
                    "}";

            // Create the URL object
            URL url = new URL(addToCartEndpointUrl);

            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + authToken);
            connection.setDoOutput(true);

            // Send the request payload
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            // Get the response
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Print the response
                System.out.println("Product added to cart successfully!");
                System.out.println("Response: " + response.toString());
            } else {
                System.out.println("Failed to add product to cart. Response Code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToCart(int customerId, String sku, int qty, String customerToken, String email, int quoteID) {
        try {
            String baseUrl = "http://localhost/gopinath_m/Shops/magento246/pub/"; // Replace with your Magento base URL

            /*String endpointUrl = baseUrl + "/rest/V1/carts/" + customerId + "/items";*/
          /*    String endpointUrl = baseUrl + "/rest/default/V1/carts/" + customerId;*/
            String endpointUrl = baseUrl + "/rest/V1/carts/mine/items";



            String payload = "{ \"cartItem\": { \"sku\": \"" + sku + "\", \"qty\": " + qty + ", \"quote_id\": \"" + quoteID + "\" } }";

            System.out.println("payLoad cart " + payload);

            // Create the URL object
            URL url = new URL(endpointUrl);

            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            String custToken = customerToken.replaceAll("^\"|\"$", "");
            connection.setRequestProperty("Authorization", "Bearer " + custToken);
             connection.setDoOutput(true);
            connection.getOutputStream().write(payload.getBytes());

            // Get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Item added to the cart successfully.");
                BufferedReader addCartReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder addCartResponseBody = new StringBuilder();
                String addCartLine;
                while ((addCartLine = addCartReader.readLine()) != null) {
                    addCartResponseBody.append(addCartLine);
                }
                addCartReader.close();


                System.out.println("Add Cart Response Code: " + addCartResponseBody);
                System.out.println("Add Cart Response Body: " + addCartResponseBody.toString());
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                System.out.println("Unauthorized. Please check your authentication token or customer secret.");
            } else {
                System.out.println("Failed to add item to the cart. Response Code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addToCart2(int customerId, int storeId,String customerToken) {
        try {
            String baseUrl = "http://localhost/gopinath_m/Shops/magento246/pub/"; // Replace with your Magento base URL
            String authToken = getAuthToken(); // Replace with your authentication token
            String sku = "24-MB01"; // Replace with the SKU of the product to add

            String jsCode = "var payload = { cartItem: { sku: '" + sku + "', qty: 1, customer_id: " + customerId + ", store_id: " + storeId + " } };\n"
                    + "var xhr = new XMLHttpRequest();\n"
                    + "xhr.open('POST', '" + baseUrl + "/rest/V1/carts/" + customerId + "/items', true);\n"
                    + "xhr.setRequestHeader('Content-Type', 'application/json');\n"
                    + "xhr.setRequestHeader('Authorization', 'Bearer " + customerToken + "');\n"
                    + "xhr.onreadystatechange = function () {\n"
                    + "    if (xhr.readyState === 4) {\n"
                    + "        if (xhr.status === 200) {\n"
                    + "            console.log('Item added to the cart successfully.');\n"
                    + "        } else if (xhr.status === 401) {\n"
                    + "            console.log('Unauthorized. Please check your authentication token.');\n"
                    + "        } else {\n"
                    + "            console.log('Failed to add item to the cart. Response Code: ' + xhr.status);\n"
                    + "        }\n"
                    + "    }\n"
                    + "};\n"
                    + "xhr.send(JSON.stringify(payload));";

            String javaCode = "String jsCode = \"" + jsCode.replace("\"", "\\\"").replace("\n", "\\n") + "\";\n"
                    + "String script = \"<script>\" + jsCode + \"</script>\";\n"
                    + "String html = \"<html><body>\" + script + \"</body></html>\";\n"
                    + "String postData = \"html=\" + html;\n"
                    + "byte[] postDataBytes = postData.getBytes(\"UTF-8\");\n"
                    + "URL url = new URL(\"" + baseUrl + "/rest/default/V1/checkout/cart/add\")\n"
                    + "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n"
                    + "conn.setRequestMethod(\"POST\");\n"
                    + "conn.setRequestProperty(\"Content-Type\", \"application/x-www-form-urlencoded\");\n"
                    + "conn.setRequestProperty(\"Content-Length\", String.valueOf(postDataBytes.length));\n"
                    + "conn.setDoOutput(true);\n"
                    + "conn.getOutputStream().write(postDataBytes);\n"
                    + "int responseCode = conn.getResponseCode();\n"
                    + "if (responseCode == 200) {\n"
                    + "    System.out.println(\"Item added to the cart successfully.\");\n"
                    + "} else if (responseCode == 401) {\n"
                    + "    System.out.println(\"Unauthorized. Please check your authentication token.\");\n"
                    + "} else {\n"
                    + "    System.out.println(\"Failed to add item to the cart. Response Code: \" + responseCode);\n"
                    + "}\n"
                    + "conn.disconnect();";

            System.out.println(javaCode);
        } catch (Exception e) {
        }
    }

     public static void addToCart2() {

        try {
            String baseUrl = "http://localhost/gopinath_m/Shops/magento246/pub/"; // Replace with your Magento base URL
            String authToken = getAuthToken(); // Replace with your authentication token
            String sku = "24-MB01"; // Replace with the SKU of the product to add

            String jsCode = "var payload = { cartItem: { sku: '" + sku + "', qty: 1 } };\n"
                    + "var xhr = new XMLHttpRequest();\n"
                    + "xhr.open('POST', '" + baseUrl + "/rest/default/V1/carts/mine/items', true);\n"
                    + "xhr.setRequestHeader('Content-Type', 'application/json');\n"
                    + "xhr.setRequestHeader('Authorization', 'Bearer " + authToken + "');\n"
                    + "xhr.onreadystatechange = function () {\n"
                    + "    if (xhr.readyState === 4) {\n"
                    + "        if (xhr.status === 200) {\n"
                    + "            console.log('Item added to the cart successfully.');\n"
                    + "        } else {\n"
                    + "            console.log('Failed to add item to the cart. Response Code: ' + xhr.status);\n"
                    + "        }\n"
                    + "    }\n"
                    + "};\n"
                    + "xhr.send(JSON.stringify(payload));";

            String javaCode = "String jsCode = \"" + jsCode.replace("\"", "\\\"").replace("\n", "\\n") + "\";\n"
                    + "String script = \"<script>\" + jsCode + \"</script>\";\n"
                    + "String html = \"<html><body>\" + script + \"</body></html>\";\n"
                    + "String postData = \"html=\" + html;\n"
                    + "byte[] postDataBytes = postData.getBytes(\"UTF-8\");\n"
                    + "URL url = new URL(\"" + baseUrl + "/rest/default/V1/checkout/cart/add\")\n"
                    + "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n"
                    + "conn.setRequestMethod(\"POST\");\n"
                    + "conn.setRequestProperty(\"Content-Type\", \"application/x-www-form-urlencoded\");\n"
                    + "conn.setRequestProperty(\"Content-Length\", String.valueOf(postDataBytes.length));\n"
                    + "conn.setDoOutput(true);\n"
                    + "conn.getOutputStream().write(postDataBytes);\n"
                    + "int responseCode = conn.getResponseCode();\n"
                    + "if (responseCode == 200) {\n"
                    + "    System.out.println(\"Item added to the cart successfully.\");\n"
                    + "} else {\n"
                    + "    System.out.println(\"Failed to add item to the cart. Response Code: \" + responseCode);\n"
                    + "}\n"
                    + "conn.disconnect();";

            System.out.println(javaCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void addToCart1(String customerToken,int customerID){

        String baseUrl = "http://localhost/gopinath_m/Shops/magento246/pub/rest/V1";
       // String authToken = getAuthToken(); // Replace with your valid authentication token

        String productSku = "24-MB01";
        String productName = "Joust Duffle Bag";
        int quantity = 1;
        double price = 34.0000;
        String currencySymbol = "€";
        String attributeSet = "Bag";
        String color = "Black";

        JSONObject payloadObject = new JSONObject();
        JSONObject cartItemObject = new JSONObject();
        JSONObject extensionAttributesObject = new JSONObject();
        JSONObject productOptionObject = new JSONObject();
        try {

            cartItemObject.put("qty", 1);
            cartItemObject.put("quote_id", "Joust Duffle Bag");
            cartItemObject.put("extension_attributes", extensionAttributesObject);
            cartItemObject.put("item_id", 1);
            cartItemObject.put("name", "Joust Duffle Bag");
            cartItemObject.put("price","€ 34.00");
            cartItemObject.put("product_option", productOptionObject);
            cartItemObject.put("product_type", "Bag");
            cartItemObject.put("sku", "24-MB01");

            // Set value for extension_attributes
            extensionAttributesObject.put("value", "<Error: Too many levels of nesting to fake this schema>");

            // Set value for product_option
            productOptionObject.put("value", "<Error: Too many levels of nesting to fake this schema>");

            // Set the cartItem object as the payload
            payloadObject.put("cartItem", cartItemObject);
            // Step 1: Create the payload
            //     String payload = "[{\"item_id\":0,\"sku\":\"" + productSku + "\",\"qty\":" + quantity + ",\"name\":\"" + productName + "\",\"price\":\"" + currencySymbol + price + "\",\"product_type\":\"" + attributeSet + "\",\"quote_id\":\"string\",\"product_option\":{\"extension_attributes\":{\"custom_options\":[{\"option_id\":\"string\",\"option_value\":\"string\",\"extension_attributes\":{\"file_info\":{\"base64_encoded_data\":null,\"type\":null,\"name\":null}}}],\"bundle_options\":[{\"option_id\":0,\"option_qty\":0,\"option_selections\":[0],\"extension_attributes\":{}}],\"downloadable_option\":{\"downloadable_links\":[0]},\"giftcard_item_option\":{\"giftcard_amount\":\"string\",\"custom_giftcard_amount\":0,\"giftcard_sender_name\":\"string\",\"giftcard_recipient_name\":\"string\",\"giftcard_sender_email\":\"string\",\"giftcard_recipient_email\":\"string\",\"giftcard_message\":\"string\",\"extension_attributes\":{\"giftcard_created_codes\":[\"string\"]}},\"configurable_item_options\":[{\"option_id\":\"string\",\"option_value\":0,\"extension_attributes\":{}}],\"grouped_options\":[{\"id\":0,\"qty\":0,\"extension_attributes\":{}}]}}},\"extension_attributes\":{\"discounts\":[{\"discount_data\":{\"amount\":0,\"base_amount\":0,\"original_amount\":0,\"base_original_amount\":0},\"rule_label\":\"string\",\"rule_id\":0}],\"negotiable_quote_item\":{\"item_id\":0,\"original_price\":0,\"original_tax_amount\":0,\"original_discount_amount\":0,\"extension_attributes\":{}}}}]";
            String payload = payloadObject.toString();
            // Step 2: Send the request to add the item to the cart
            URL cartUrl = new URL(baseUrl + "/carts/"+customerID+"/items");
            HttpURLConnection cartConnection = (HttpURLConnection) cartUrl.openConnection();
            cartConnection.setRequestMethod("POST");
            cartConnection.setRequestProperty("Authorization", "Bearer " + customerToken);
            cartConnection.setRequestProperty("Content-Type", "application/json");
            cartConnection.setDoOutput(true);

            OutputStream outputStream = cartConnection.getOutputStream();
            outputStream.write(payload.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int cartResponseCode = cartConnection.getResponseCode();
            if (cartResponseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Item added to the cart successfully.");
            } else {
                System.out.println("Failed to add item to the cart. Response Code: " + cartResponseCode);
            }

            cartConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

