package com.nn.apis;

import com.nn.callback.CallbackProperties;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import com.nn.Magento.Constants;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.util.*;

import static com.nn.Magento.Constants.*;
import static com.nn.apis.MagentoAPIs.*;
public class MagentoAPI_Helper {

    @Test
    public void main(){
       String price = getProductPrice("24-MB02");
    }

    private static int oldStock;
    private static void setOldStock(int stock){
        oldStock= stock;
    }
    public static int getOldStock(){
        return oldStock;
    }

    @Step("Create new customer")
    public static void createCustomer(String paymentType){
        createCustomerAccount(SHOP_BASE_URL,getCustomerPayload(paymentType,generateRandomEmail()));
    }


    @Step("Create new customer")
    public static void createPaypalCustomer(){
        createPaypalCustomerAccount(SHOP_BASE_URL,getCustomerPayload(CallbackProperties.PAYPAL,"ecesankar93@gmail.com"));
    }

    @Step("Create new customer")
    public static void createCustomer(String paymentType, String email){
        createCustomerAccount(SHOP_BASE_URL,getCustomerPayload(paymentType,email),email);
    }

    @Step("Add product {0} with quantity {1} to cart")
    public static void addProductToCart(String productSKU, int qty){
        String customerToken = getCustomerToken(SHOP_BASE_URL,getCustomerTokenPayload(getCustomerEmail()));
        var quoteID = getQuoteID(SHOP_BASE_URL,customerToken);
        addToCart(SHOP_BASE_URL,customerToken,addToCartPayload(productSKU,qty,quoteID));
    }

    public static int getProductStock(String productSKU){
        Response res = getStock(SHOP_BASE_URL,productSKU);
        setOldStock(res.jsonPath().get("qty"));
        return res.jsonPath().get("qty");
    }

    public static String getProductPrice(String productSKU){
        Response res = getProductDetails(SHOP_BASE_URL,productSKU);
        if(res.jsonPath().get("price") instanceof  Float || res.jsonPath().get("price") instanceof  Double){
            float val = res.jsonPath().get("price");
            return String.valueOf(val*100).split("\\.")[0];
        }else if(res.jsonPath().get("price") instanceof  Integer){
            int val = res.jsonPath().get("price");
            return String.valueOf(val*100);
        }else{
            return res.jsonPath().get("price").toString();
        }
    }

    @Step("Update product stock {0}")
    public static void updateProductStock(String productSKU){
        Response res = getStock(SHOP_BASE_URL,productSKU);
        int stock = res.jsonPath().get("stock_item.qty");
        updateStock(SHOP_BASE_URL,productSKU,stock+500);
    }

    @Step("Update product {0} stock {1}")
    public static void updateProductStock(String productSKU,int qty){
        updateStock(SHOP_BASE_URL,productSKU,qty);
    }


    @Step("Update product Price {0}")
    public static void updateProductPrice(String productSKU,double newPrice){
        updatePrice(SHOP_BASE_URL,productSKU,newPrice);
    }

    @Step("Clear shop cart")
    public static void clearCart() {
        String customerToken = getCustomerToken(SHOP_BASE_URL, getCustomerTokenPayload(getCustomerEmail()));
        Response cartData = getCart(SHOP_BASE_URL, customerToken);
        if(cartData.getStatusCode() == 404){
            String message = cartData.getBody().jsonPath().getString("message");
            if(message.equals("Current customer does not have an active cart."))
                return;
            /*else
               throw new RuntimeException("Error occurred while clear the cart "+cartData.asString());*/
        }
        JSONArray cart = new JSONArray(cartData.getBody().asString());

        if (cart.length() > 0) {
            cart.toList()
                    .stream()
                    .map(item -> (Map<String,Object>) item)
                    .map(item -> (int) item.get("item_id"))
                    .forEach(itemID -> {
                        deleteCart(SHOP_BASE_URL, customerToken, itemID);
                        Log.info("Product has been removed from the cart. item_id: " + itemID);
                    });
        } else {
            ExtentTestManager.logMessage("Cart is empty: " + cart);
            Log.info("Cart is empty: " + cart);
            AllureManager.saveLog("Cart is empty: " + cart);
        }
    }

    public static boolean isCartEmpty(){
        String customerToken = getCustomerToken(SHOP_BASE_URL, getCustomerTokenPayload(getCustomerEmail()));
        Response cartData = getCart(SHOP_BASE_URL, customerToken);
        if(cartData.getStatusCode() == 404){
            String message = cartData.getBody().jsonPath().getString("message");
            return message.equals("Current customer does not have an active cart.");
        }
        JSONArray cart = new JSONArray(cartData.getBody().asString());
        return cart.length() == 0;
    }

    public static String getNovalnetComments(int orderID){
        Response orderData = getOrder(SHOP_BASE_URL,orderID);
        String paymentData = orderData.jsonPath().get("payment.additional_data");
        if(new JSONObject(paymentData).has("NnComments"))
            return new JSONObject(paymentData).getString("NnComments");
        return null;
    }

    @Step("Verify order invoice created")
    public static boolean verifyInvoiceCreated(String orderID, boolean expected){
        Response orderData = getOrder(SHOP_BASE_URL,Integer.parseInt(orderID));
        boolean flag = orderData.jsonPath()
                                    .getList("status_histories").stream()
                                    .map(e-> (Map<String,Object>) e)
                                    .filter(en -> en.get("entity_name").equals("invoice"))
                                    .anyMatch(s-> s.get("status").equals("complete") && s.get("parent_id").toString().equals(orderID));

        DriverActions.verifyEquals(flag,expected,"Verify order invoice is created: ");
        return flag;
    }

    @Step("Verify credit memo created")
    public static boolean verifyCreditMemoCreated(String orderID, boolean expected){
        Response orderData = getOrder(SHOP_BASE_URL,Integer.parseInt(orderID));
        boolean flag = orderData.jsonPath()
                .getList("status_histories").stream()
                .map(e-> (Map<String,Object>) e)
                .filter(en -> en.get("entity_name").equals("creditmemo"))
                .anyMatch(s-> s.get("parent_id").toString().equals(orderID));

        DriverActions.verifyEquals(flag,expected,"Verify credit memo is created: ");
        return flag;
    }

    @Step("Verify novalnet payment comments")
    public static void verifyNovalnetComments(String orderID,String expected){
        String comments = getNovalnetComments(Integer.parseInt(orderID));
        boolean flag = false;
        if(comments != null){
            flag = Arrays.stream(comments.split("<br>"))
                    .anyMatch(c-> c.contains(expected));
        }
        DriverActions.verifyEquals(flag,true,"Verify novalnet comments <b>"+expected+"</b> in the orders: ");
    }

    @Step("Verify novalnet payment comments")
    public static void verifyNovalnetComments(String orderID,String expected, String amount){
        String comments = getNovalnetComments(Integer.parseInt(orderID));
        boolean flag = false;
        if(comments != null){
            flag = Arrays.stream(comments.split("<br>"))
                    .anyMatch(c-> c.contains(expected) && c.contains(changeAmountFormatMagento(amount)));
        }
        DriverActions.verifyEquals(flag,true,"Verify novalnet comments <b>"+expected+"</b> with amount <b>"+amount+"</b> in the orders: ");
    }

    @Step("Verify novalnet payment comments")
    public static void verifyNovalnetComments(String orderID,String expected, String amount, String dueDate){
        String comments = getNovalnetComments(Integer.parseInt(orderID));
        Date date = DriverActions.getDateFromString("yyyy-MM-dd",dueDate);
        String updateDate = DriverActions.changePatternOfDate("MMMM dd, yyyy",date);
        boolean flag = false;
        if(comments != null){
            flag = Arrays.stream(comments.split("<br>"))
                    .anyMatch(c-> c.contains(expected) && c.contains(changeAmountFormatMagento(amount)) && c.contains(updateDate));
        }
        DriverActions.verifyEquals(flag,true,"Verify novalnet comments <b>"+expected+"</b> with amount <b>"+amount+"</b> in the orders: ");
    }

    @Step("Verify order status")
    public static void verifyOrderStatus(String orderID,String status){
        Response response = getOrder(SHOP_BASE_URL,Integer.parseInt(orderID));
        var actual = response.jsonPath().getString("status");
        if(actual.equals("holded"))
            actual = "On Hold";
        DriverActions.verifyEquals(actual.toLowerCase(),status.toLowerCase(),"Verify order status: ");
    }

    @Step("Verify product stock after order placed")
    public static void verifyProductStock(String productSKU,int expected){
        int stock = getProductStock(productSKU);
        DriverActions.verifyEquals(stock,expected,"Verification of prodcut Stock after order placed: ");
    }

    public static String changeAmountFormatMagento(String amount){
        if(amount.isEmpty()) return amount;
        int len = amount.length();
        if(len == 1) return "0.0"+amount;
        if(len == 2) return "0."+amount;
        return amount.substring(0,len-2)+"."+amount.substring(len-2);
    }


    @Step("Update customer with New Guarantee B2B Address")
    public static void updateGuaranteeB2BDEAddress(){
        String cusID=getCustomerID();
        updateGuaranteeB2BDECustomerAddress(SHOP_BASE_URL,cusID,updateGuaranteeB2BDEAddressPayload(cusID));
    }

    @Step("Create Downloadable Product")
    public static String createandReturnDownloadbleProductWithSku(String productName, double price, int qty) {
        try {

            // Generate a random name based on input string
            String randomName = "productName_" + RandomStringUtils.randomAlphanumeric(10);

            // Generate a UUID with the prefix "download"
            UUID uuid = UUID.nameUUIDFromBytes(randomName.getBytes());
            // Get the first 5 characters of the UUID as SKU

            String sku = uuid.toString().substring(0, 5);
            // Create the downloadable product
            Response downloadableResponse = createDownloadableProduct(sku, sku, price, qty);

            if (downloadableResponse.getStatusCode() == 200) {
                System.out.println("Downloadable Product Response Code: " + downloadableResponse.getStatusCode());
                return sku+".html";
            } else {
                System.err.println("Error creating downloadable product. Status Code: " + downloadableResponse.getStatusCode());
                return null; // Return null to indicate an error
            }
        } catch (Exception e) {
            System.err.println("An error occurred while creating downloadable product: " + e.getMessage());
            return null; // Return null to indicate an error
        }
    }


    @Step("Create Downloadable Product")
    public static String createandReturnVirtualProductWithSku(String productName, double price, int qty) {
        try {

            // Generate a random name based on input string
            String randomName = "productName_" + RandomStringUtils.randomAlphanumeric(10);

            // Generate a UUID with the prefix "download"
            UUID uuid = UUID.nameUUIDFromBytes(randomName.getBytes());
            // Get the first 5 characters of the UUID as SKU

            String sku = uuid.toString().substring(0, 5);
            // Create the virtual product
            Response virtualProductResponse = createVirtualProduct(sku, sku, price, qty);

            if (virtualProductResponse.getStatusCode() == 200) {
                System.out.println("Virtual Product Response Code: " + virtualProductResponse.getStatusCode());
                return sku;
            } else {
                System.err.println("Error creating virtual product. Status Code: " + virtualProductResponse.getStatusCode());
                return null; // Return null to indicate an error
            }
        } catch (Exception e) {
            System.err.println("An error occurred while creating virtual product: " + e.getMessage());
            return null; // Return null to indicate an error
        }
    }

    @Step("Create new GooglePay customer")
    public static void createGooglePayCustomer(){
        createGooglePayCustomerAccount(SHOP_BASE_URL,getCustomerPayload(CallbackProperties.GOOGLEPAY,"novalnetesolutions2010@gmail.com"));
    }

}
