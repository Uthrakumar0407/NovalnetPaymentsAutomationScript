package com.nn.testcase.Magento;

import com.nn.pages.Magento.basetest.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static com.nn.Magento.Constants.*;
import static com.nn.Magento.Constants.SHOP_BACKEND_PASSWORD;

public class DeleteAllCustomers  {
    private static final String CONTENT_TYPE = "application/json";
    @Test
    public void deleteCustomersInRange() {
        // Base URL for your Magento API
        String baseURI = System.getProperty("SHOP_BASE_URL");
        String CUSTOMER_START_ID = System.getProperty("CUSTOMER_START_ID");
        String CUSTOMER_END_ID = System.getProperty("CUSTOMER_END_ID");

        // Define the range of customer IDs to delete
        int startId = Integer.parseInt(CUSTOMER_START_ID);
        int endId = Integer.parseInt(CUSTOMER_END_ID);

        // Loop through the range and send DELETE requests
        for (int customerId = Integer.parseInt(CUSTOMER_START_ID); customerId <= Integer.parseInt(CUSTOMER_END_ID); customerId++) {
            try {
                Response response = RestAssured.given()
                        .baseUri(baseURI)
                        .contentType(CONTENT_TYPE)
                        .header("Authorization","Bearer "+getAuthToken(baseURI).replaceAll("^\"|\"$", ""))
                        .delete("customers/" + customerId)
                        .then()
                        .extract().response();

                // Check if the delete was successful
                if (response.getStatusCode() == 200) {
                    System.out.println("Deleted customer with ID: " + customerId);
                } else {
                    System.out.println("Failed to delete customer with ID: " + customerId);
                }
            } catch (Exception e) {
                // Handle any exceptions and log the error message
                System.out.println("Error deleting customer with ID: " + customerId);
                e.printStackTrace();
            }
        }
    }


    public static  String getAuthToken(String baseURI){

        String BEARER_TOKEN = System.getProperty("BEARER_TOKEN");
        String SHOP_BACKEND_USERNAME = System.getProperty("SHOP_BACKEND_USERNAME");
        String SHOP_BACKEND_PASSWORD = System.getProperty("SHOP_BACKEND_PASSWORD");
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

}
