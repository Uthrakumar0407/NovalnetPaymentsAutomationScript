package com.nn.apis;

import com.nn.exceptions.ShopwareExceptions;
import com.nn.utilities.Log;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

import static com.nn.apis.RequestMethod.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.nn.callback.CallbackProperties.*;

public class ShopwareAPIs {

    private static final String CONTENT_TYPE = "application/json";
    private static final ThreadLocal<String> customerEmail = new ThreadLocal<>();
    private static final ThreadLocal<String> salutationID = new ThreadLocal<>();
    private synchronized void setSalutationID(String id){
        salutationID.set(id);
    }
    public synchronized String getSalutationID(){
        return salutationID.get();
    }
    private void setCustomerEmail(String email){
        customerEmail.set(email);
    }
    public String getCustomerEmail(){
        return customerEmail.get();
    }
    private static final String BASE_URI = System.getProperty("BASE_URI");
    private static final String ADMIN_URI = System.getProperty("SHOP_FRONT_END_URL");
    private static final String STORE_URL = System.getProperty("STORE_URL");  //Do not include leading forward slash
    private static final String SW_API_KEY = System.getProperty("SW_API_KEY");  // Sales channel -> API access
    private static final String SW_ACCESS_KEY = System.getProperty("SW_ACCESS_KEY"); // Settings -> System -> Integrations -> Add integration
    private static final String SHOP_BACKEND_USER_CRED = System.getProperty("SHOP_BACKEND_USER_CRED");
    private static final String SHOP_BACKEND_USERNAME = System.getProperty("SHOP_BACKEND_USERNAME");
    private static final String CUSTOMER_PASSWORD = "Novalnet@123";
    private static ShopwareAPIs shopwareAPIs;
    private RequestSpecification requestSpecification;

    private ShopwareAPIs(){
        // Don't want to create an instance
    }

    public synchronized static ShopwareAPIs getInstance(){
        if(shopwareAPIs == null){
            synchronized (ShopwareAPIs.class){
                if(shopwareAPIs == null)
                    shopwareAPIs = new ShopwareAPIs();
            }
        }
        return shopwareAPIs;
    }

    private ShopwareAPIs getRequestSpecs(String body){
        requestSpecification = RestAssured.given()
                .baseUri(BASE_URI)
                .contentType(CONTENT_TYPE)
                .header("sw-access-key", SW_API_KEY)
                .body(body).log().all();
        return this;
    }

    private ShopwareAPIs getRequestSpecs(String baseURI, String body){
        requestSpecification = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .body(body).log().all();
        return this;
    }

    private ShopwareAPIs getRequestSpecs(String baseURI, Header header){
        requestSpecification = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .header(header)
                .log().all();
        return this;
    }

    //without body
    private ShopwareAPIs getRequestSpecs(){
        requestSpecification = RestAssured.given()
                .baseUri(BASE_URI)
                .contentType(CONTENT_TYPE)
                .header("sw-access-key", SW_API_KEY)
                .log().all();
        return this;
    }

    private ShopwareAPIs getRequestSpecs(Map<String,Object> headers, String body){
        requestSpecification = RestAssured.given()
                .baseUri(BASE_URI)
                .contentType(CONTENT_TYPE)
                .headers(headers)
                .body(body).log().all();
        return this;
    }

    private ShopwareAPIs getRequestSpecs(String baseURI, Map<String,Object> headers, Map<String,Object> body){
        requestSpecification = RestAssured.given()
                .baseUri(baseURI)
                .contentType(CONTENT_TYPE)
                .headers(headers)
                .body(body).log().all();
        return this;
    }

    private ShopwareAPIs getRequestSpecs(Map<String,Object> headers){
        requestSpecification = RestAssured.given()
                .baseUri(BASE_URI)
                .contentType(CONTENT_TYPE)
                .headers(headers).log().all();
        return this;
    }

    private Response getResponse(RequestMethod requestMethod, String path){
        switch (requestMethod){
            case GET:
                return requestSpecification.get(path);
            case POST:
                return requestSpecification.post(path);
            case PUT:
                return requestSpecification.put(path);
            case DELETE:
                return requestSpecification.delete(path);
            case PATCH:
                return requestSpecification.patch(path);
            default:
               throw new ShopwareExceptions("Invalid request method");
        }
    }

    private Response sendRequest(RequestMethod requestMethod, String body, String path){
        try {
            Response response = getRequestSpecs(body).getResponse(requestMethod, path);
            response.then().log().all();
            return response;
        }catch (Exception e){return null;}
    }

    private Response sendRequest(String body, String path){
        try{
        Response response = getRequestSpecs(ADMIN_URI,body).getResponse(POST,path);
        response.then().log().all();
        return response;
        }catch (Exception e){return null;}
    }

    private Response sendRequest(String path){
        try{
        Response response = getRequestSpecs(ADMIN_URI,new Header("Authorization","Bearer "+getBearerToken())).getResponse(GET,path);
        response.then().log().all();
        return response;}catch (Exception e){return null;}
    }

    private Response sendRequest(RequestMethod requestMethod, String path){
        try{
        Response response = getRequestSpecs().getResponse(requestMethod,path);
        response.then().log().all();
        return response;}catch (Exception e){return null;}
    }

    private Response sendRequest(Map<String,Object> headers, String path){
        try{
        Response response = getRequestSpecs(headers).getResponse(RequestMethod.GET,path);
        response.then().log().all();
        return response;}catch (Exception e){return null;}
    }

    private Response sendRequest(RequestMethod requestMethod, Map<String,Object> headers, String body, String path){
        try{
        Response response = getRequestSpecs(headers,body).getResponse(requestMethod,path);
        response.then().log().all();
        return response;}catch (Exception e){return null;}
    }

    private Response sendRequest(String baseURI, RequestMethod requestMethod, Map<String,Object> headers, Map<String,Object> body, String path){
        try{
        Response response = getRequestSpecs(baseURI,headers,body).getResponse(requestMethod,path);
        response.then().log().all();
        return response;
        }catch (Exception e){return null;}
    }

    private synchronized void generateSalutationID(){
        Response response = sendRequest(POST,"{}","salutation");
        validateResponseStatusCode(response,"Error occurred while getting the salutation ID of the shopware. response code is ");
        setSalutationID(response.jsonPath().get("elements[0].id"));
    }

    public void updateProductPrice(String productNumber,float amount){
        String id = getProductID(productNumber);
        sendRequest(ADMIN_URI,PATCH,Map.of("Authorization","Bearer "+getBearerToken()),getUpdateProductPricePayload(productNumber,amount),"api/product/"+id);
    }

    private String getBearerToken(){
        Response response = sendRequest("{\n" +
                "  \"client_id\": \"administration\",\n" +
                "  \"grant_type\": \"password\",\n" +
                "  \"scopes\": \"write\",\n" +
                "  \"username\": \""+SHOP_BACKEND_USERNAME+"\",\n" +
                "  \"password\": \""+SHOP_BACKEND_USER_CRED+"\"\n" +
                "}",
                "api/oauth/token");
        return response.jsonPath().getString("access_token");
    }

    private synchronized String[] getCountryAndStateID(String countryISO){
        if(countryISO.length() != 2){
            throw new ShopwareExceptions("Country ISO value should be two digits");
        }
        Response response;
        int page = 1;
        String countryID = null;
        String stateID = null;
        for (int i = 0; i < 3; i++) {
            response = sendRequest(POST,
                    "{\n" +
                            "  \"page\": "+(page++)+",\n" +
                            "  \"limit\": 100, \n" +
                            "  \"associations\": {\n" +
                            "    \"states\": {}\n" +
                            "  }\n" +
                            "}",
                    "country");
            validateResponseStatusCode(response,"Error occurred while getting the countries of the shopware: ");

            /*** Use this below logic if you don't understand streams ***/

//            List<Object> list = response.jsonPath().getList("elements");
//            for (int j = 0; j < list.size(); j++) {
//                if(response.jsonPath().getString("elements["+j+"].iso").equals(countryISO)){
//                    countryID = response.jsonPath().getString("elements["+j+"].id");
//                    if(response.jsonPath().getList("elements["+j+"].states").size() > 0) {
//                        stateID = response.jsonPath().getString("elements["+j+"].states[0].id");
//                    }
//                }
//                if(countryID != null)
//                    break;
//            }

            Optional<LinkedHashMap<String, Object>> country = response.jsonPath().getList("elements")
                                                                .stream().parallel()
                                                                .map(o -> (LinkedHashMap<String, Object>) o)
                                                                .filter(map -> map.get("iso").equals(countryISO))
                                                                .findAny();
            if(country.isPresent()){
                countryID = country.get().get("id").toString();
                stateID = country.stream()
                                .map(mp-> (List<LinkedHashMap<String,Object>>) mp.get("states"))
                                .filter(li->!li.isEmpty())
                                .map(lin->lin.get(0).get("id").toString())
                                .findFirst().orElse(null);
            }

            if(countryID != null)
                break;
        }
        return new String[]{countryID,(stateID == null ? "" : stateID)};
    }

    public synchronized void createCustomer(String paymentType){
        String payload = getCustomerPayload(paymentType);
        Response response = sendRequest(POST,Map.of("sw-access-key", SW_API_KEY, "sw-context-token", SW_ACCESS_KEY),payload,"account/register");
        validateResponseStatusCode(response,"Error occurred while creating customer : ");
        String email = response.jsonPath().getString("email");
        setCustomerEmail(email);
        Log.info("Customer created with the email: " + getCustomerEmail());
    }

    public synchronized void createCustomer(String paymentType, String customerEmail){
        String payload = getCustomerPayload(paymentType, customerEmail);
        Response response = sendRequest(POST,Map.of("sw-access-key", SW_API_KEY, "sw-context-token", SW_ACCESS_KEY),payload,"account/register");
        //validateResponseStatusCode(response,"Error occurred while creating customer : ");
        setCustomerEmail(customerEmail);
        Log.info("Customer created with the email: " + getCustomerEmail());
    }

private String login(){
        int MAX_RETRIES = 3;
        int retryCount = 0;
        Response response = null;

        while (retryCount < MAX_RETRIES) {
            try {
                response = sendRequest(POST,
                        Map.of("sw-access-key", SW_API_KEY, "sw-context-token", SW_ACCESS_KEY),
                        "{\n" +
                                "    \"username\": \"" + getCustomerEmail() + "\",\n" +
                                "    \"password\": \"" + CUSTOMER_PASSWORD + "\"\n" +

                                "}",
                        "account/login");


               // validateResponseStatusCode(response, "Error occurred while login with customer : ");
                System.out.println(response.asPrettyString());

                return response.getHeader("sw-context-token");
            } catch (Exception e) {
                // Log the exception or perform any necessary actions
                System.out.println("Retrying - login API "+ retryCount);

                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    // Log or handle the failure after maximum retries
                    System.out.println("Maximum retry count reached - login API");
                    break;
                }
            }
        }

        // Handle failure scenario after max retries
        return null; // Or throw an exception indicating login failure
   }

   /* private String login() {
        int MAX_RETRIES = 3;
        int retryCount = 0;
        Response response = null;

        while (retryCount < MAX_RETRIES) {
            try {
                // Include the context token in the headers map
                Map<String, String> headers = Map.of(
                        "sw-access-key", SW_API_KEY,
                        "sw-context-token", SW_CONTEXT_TOKEN // Add the context token here
                );

                response = sendRequest(POST, headers,
                        "{\n" +
                                "    \"username\": \"" + getCustomerEmail() + "\",\n" +
                                "    \"password\": \"" + CUSTOMER_PASSWORD + "\"\n" +
                                "}",
                        "account/login");

                // validateResponseStatusCode(response, "Error occurred while login with customer : ");
                System.out.println(response.asPrettyString());

                return response.jsonPath().getString("contextToken");
            } catch (Exception e) {
                // Log the exception or perform any necessary actions
                System.out.println("Retrying - login API " + retryCount);

                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    // Log or handle the failure after maximum retries
                    System.out.println("Maximum retry count reached - login API");
                    break;
                }
            }
        }

        // Handle failure scenario after max retries
        return null; // Or throw an exception indicating login failure
    }*/


   /* private String getProductID(String productNumber){
        Response response = sendRequest(GET,"product");
        validateResponseStatusCode(response,"Error occurred while getting product details");
        return response.jsonPath().getList("elements")
                .stream().parallel()
                .map(e-> (LinkedHashMap<String,Object>) e)
                .filter(p-> p.get("productNumber").equals(productNumber))
                .map(map2->(LinkedHashMap<String,Object>) map2.get("coverId"))
                .map(map3->map3.get("productId").toString())
                .findAny()
                .orElseThrow(()-> new ShopwareExceptions("Product ID not found for the product " + productNumber));
    }*/

    private String getProductID(String productNumber) {
        Response response = sendRequest(GET, "product");
        validateResponseStatusCode(response, "Error occurred while getting product details");

        List<Object> elements = response.jsonPath().getList("elements");

        for (Object element : elements) {
            LinkedHashMap<String, Object> product = (LinkedHashMap<String, Object>) element;
            if (product.get("productNumber").equals(productNumber)) {
                LinkedHashMap<String, Object> cover = (LinkedHashMap<String, Object>) product.get("cover");
                return cover.get("productId").toString();
            }
        }

        throw new ShopwareExceptions("Product ID not found for the product " + productNumber);
    }





    public String getProductPrice(String productNumber){
        Response response = sendRequest(GET,"product");
        validateResponseStatusCode(response,"Error occurred while getting product details");
        return response.jsonPath().getList("elements")
                .stream().parallel()
                .map(e-> (LinkedHashMap<String,Object>) e)
                .filter(p-> p.get("productNumber").equals(productNumber))
                .map(e1-> (LinkedHashMap<String,Object>)e1.get("calculatedPrice"))
                .map(e2->e2.get("totalPrice").toString())
                .findAny()
                .orElseThrow(()-> new ShopwareExceptions("Product Price not found for the product " + productNumber));
    }

    private String getVariantID(String productID){
        Response response = sendRequest(POST,"product/"+productID);
        validateResponseStatusCode(response,"Error occurred while getting product variant ID ");
        String variantId = response.jsonPath().get("product.cheapestPriceContainer.value."+productID+".default.variant_id");
        if(variantId == null){
            throw new ShopwareExceptions("Could not get the variant ID of the product please verify product Number or try different product");
        }
        return variantId;
    }

    public void addProductToCart(String productNumber){
        Response response = sendRequest(POST,
                            Map.of("sw-access-key", SW_API_KEY, "sw-context-token",login()),
                            "{\n" +
                            "  \"items\": [\n" +
                            "    {\n" +
                            "      \"type\": \"product\",\n" +
                            "      \"referencedId\": \""+getProductID(productNumber)+"\"\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}",
                            "checkout/cart/line-item");
        validateResponseStatusCode(response,"Error occurred while adding product to cart");
        System.out.println("Product "+productNumber+" added to the cart");
    }

    public void clearCart() {
        synchronized (this) {
            // Fetching the login token outside of the loop to avoid multiple calls
            String contextToken = login();

            // Performing cart clearing operation
            Response response = sendRequest(
                    Map.of("sw-access-key", SW_API_KEY, "sw-context-token", contextToken),
                    "checkout/cart"
            );

            int productCount = response.jsonPath().getList("lineItems").size();
            Log.info("Total number of products in cart: " + productCount);

            if (productCount > 0) {
                List<String> productIDs = response.jsonPath().getList("lineItems")
                        .stream()
                        .map(o -> ((LinkedHashMap<String, Object>) o).get("id").toString())
                        .collect(Collectors.toList());

                // Synchronize on each product deletion
                productIDs.parallelStream().forEach(productID -> {
                    synchronized (this) {
                        sendRequest(
                                DELETE,
                                Map.of("sw-access-key", SW_API_KEY, "sw-context-token", contextToken),
                                "{\n" +
                                        "    \"ids\": [\n" +
                                        "             \"" + productID + "\"             \n" +
                                        "    ]\n" +
                                        "}",
                                "checkout/cart/line-item"
                        );
                        System.out.println("Product removed from the cart with the ID " + productID);
                    }
                });
            }
        }
    }

    private void validateResponseStatusCode(Response response, String message){
        if(response.getStatusCode() != 200){
            throw new ShopwareExceptions(message+response.asPrettyString());
        }
    }

    private String getCustomerPayload(String paymentType){
        generateSalutationID();
        String salutationID = getSalutationID();
        String[] countryState;
        switch (paymentType){
            case CREDITCARD:
            case DIRECT_DEBIT_SEPA:
            case INVOICE:
            case INSTALMENT_INVOICE:
            case INSTALMENT_DIRECT_DEBIT_SEPA:
            case ONLINE_TRANSFER:
            case GIROPAY:
            case ONLINE_BANK_TRANSFER:
            case TRUSTLY:
            case PREPAYMENT:
            case GUARANTEED_INVOICE:
            case GUARANTEED_DIRECT_DEBIT_SEPA:
            case CASHPAYMENT:
            case PAYPAL:
            case GOOGLEPAY:
                countryState = getCountryAndStateID("DE");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + generateRandomEmail() + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case IDEAL:
                countryState = getCountryAndStateID("NL");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + generateRandomEmail() + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case EPS:
                countryState = getCountryAndStateID("AT");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + generateRandomEmail() + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                    /*    "    \"countryStateId\": \"" + countryState[1] + "\"\n" +*/
                        "  }\n" +
                        "}";
            case ALIPAY:
            case WECHATPAY:
                countryState = getCountryAndStateID("CN");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + generateRandomEmail() + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Beijing\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                     /*   "    \"countryStateId\": \"" + countryState[1] + "\"\n" +*/
                        "  }\n" +
                        "}";
            case BANCONTACT:
            case PAYCONIQ:
                countryState = getCountryAndStateID("BE");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + generateRandomEmail() + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                   /*     "    \"countryStateId\": \"" + countryState[1] + "\"\n" +*/
                        "  }\n" +
                        "}";
            case POSTFINANCE_CARD:
                countryState = getCountryAndStateID("CH");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + generateRandomEmail() + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                /*        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +*/
                        "  }\n" +
                        "}";
            case PRZELEWY24:
            case BLIK:
                countryState = getCountryAndStateID("PL");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + generateRandomEmail() + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case MULTIBANCO:
            case MBWAY:
                countryState = getCountryAndStateID("PT");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + generateRandomEmail() + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case DIRECT_DEBIT_ACH:
                countryState = getCountryAndStateID("US");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + generateRandomEmail() + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";

            default:
                throw new ShopwareExceptions("Invalid payment type to get the payload");
        }
    }

    private String getCustomerPayload(String paymentType, String email){
        generateSalutationID();
        String salutationID = getSalutationID();
        String[] countryState;
        switch (paymentType){
            case CREDITCARD:
            case DIRECT_DEBIT_SEPA:
            case INVOICE:
            case INSTALMENT_INVOICE:
            case INSTALMENT_DIRECT_DEBIT_SEPA:
            case ONLINE_TRANSFER:
            case GIROPAY:
            case ONLINE_BANK_TRANSFER:
            case TRUSTLY:
            case PREPAYMENT:
            case GUARANTEED_INVOICE:
            case GUARANTEED_DIRECT_DEBIT_SEPA:
            case CASHPAYMENT:
            case PAYPAL:
            case GOOGLEPAY:
                countryState = getCountryAndStateID("DE");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case IDEAL:
                countryState = getCountryAndStateID("NL");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case EPS:
                countryState = getCountryAndStateID("AT");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case ALIPAY:
            case WECHATPAY:
                countryState = getCountryAndStateID("CN");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Beijing\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case BANCONTACT:
                countryState = getCountryAndStateID("BE");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case POSTFINANCE_CARD:
                countryState = getCountryAndStateID("CH");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case PRZELEWY24:
            case BLIK:
                countryState = getCountryAndStateID("PL");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case MULTIBANCO:
                countryState = getCountryAndStateID("PT");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            case DIRECT_DEBIT_ACH:
                countryState = getCountryAndStateID("USA");
                return "{\n" +
                        "  \"salutationId\": \"" + salutationID + "\",\n" +
                        "  \"firstName\": \"Nobert\",\n" +
                        "  \"lastName\": \"Maier\",\n" +
                        "  \"email\": \"" + email + "\",\n" +
                        "  \"password\": \""+ CUSTOMER_PASSWORD + "\",\n" +
                        "  \"storefrontUrl\": \"" + STORE_URL + "\",\n" +
                        "  \"billingAddress\": {\n" +
                        "    \"street\": \"9, Hauptstr\",\n" +
                        "    \"zipcode\": \"66862\",\n" +
                        "    \"city\": \"Kaiserslautern\",\n" +
                        "    \"countryId\": \"" + countryState[0] + "\",\n" +
                        "    \"countryStateId\": \"" + countryState[1] + "\"\n" +
                        "  }\n" +
                        "}";
            default:
                throw new ShopwareExceptions("Invalid payment type to get the payload");
        }
    }

    private Map<String, Object> getUpdateProductPricePayload(String productNumber, float amount){
        Map<String,Object> map = new LinkedHashMap<>();
        Response response = sendRequest("api/product");
        response.jsonPath().getList("data");
        for (int i = 0; i < response.jsonPath().getList("data").size(); i++) {
            String productNum = response.jsonPath().get("data["+i+"].attributes.productNumber");
            if(productNum.equals(productNumber)){
                map.put("name",response.jsonPath().get("data["+i+"].attributes.name"));
                map.put("productNumber",productNumber);
                map.put("stock",100);
                map.put("taxId",response.jsonPath().get("data["+i+"].attributes.taxId"));
                map.put("price",List.of(Map.of("currencyId",response.jsonPath().get("data["+i+"].attributes.price[0].currencyId"),
                        "net",amount-amount*0.19,
                        "gross",amount,
                        "linked",false)));
                break;
            }
        }
        if(map.isEmpty()){
            throw new ShopwareExceptions("There is no product in the shop for this ID: "+productNumber);
        }
        return map;
    }

    private String generateRandomEmail() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        String random = uuidString.substring(uuidString.length() - 6);
        return "sw6_"+getCurrentDateTimeCustom()+"_"+random+"@gmail.com";
    }

    private synchronized String getCurrentDateTimeCustom() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
        return formatter.format(now);
    }

    @Test
    public void main() {
       // ShopwareAPIs.getInstance().createCustomer(GOOGLEPAY);
        //System.out.println("Produce price: "+ShopwareAPIs.getInstance().getProductPrice("SWDEMO1000"));
        //ShopwareAPIs.getInstance().addProductToCart("SWDEMO10001");
        //ShopwareAPIs.getInstance().addProductToCart("SWDEMO10005.4");
//        ShopwareAPIs.getInstance().addProductToCart("SWDEMO10002");
//        ShopwareAPIs.getInstance().addProductToCart("SWDEMO10006");
        //ShopwareAPIs.getInstance().clearCart();

        //System.out.println(ShopwareAPIs.getInstance().getProductID("SWDEMO10002"));
        //ShopwareAPIs.getInstance().updateProductPrice("SWDEMO10002",230.50f);
    }


    public static String getShopWareOAuthAccessToken(String swAccessKey, String admin, String pwd, String baseURI) {
        String tokenEndpoint = baseURI + "/api/oauth/token";
        String requestBody = "client_id=administration" +
                "&grant_type=password" +
                "&scopes=write" +
                "&username=" + admin +
                "&password=" + pwd;
        // Obtain the access token
        Response tokenResponse = RestAssured.given()
                .auth().preemptive().basic("AUTH", swAccessKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(requestBody)
                .post(tokenEndpoint);
        // Extract the access token from the response
        String accessToken = tokenResponse.jsonPath().get("access_token");
        return accessToken;
    }
}
