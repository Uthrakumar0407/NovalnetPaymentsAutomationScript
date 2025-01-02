package com.nn.testcase;

import com.nn.apis.MagentoAPI_Helper;
import com.nn.apis.MagentoAPIs;
import com.nn.pages.Magento.NovalnetAdminPortal;
import com.nn.testcase.Magento.NovalnetAdminPortalTest;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

public class TestFF {

    public static void main(String[] args) throws Exception {


        //MagentoAPI_Helper.createUSCustomer("test12@gmail.com");
     //   System.out.println(NovalnetAdminPortalTest.getRegionId("US","Florida"));

        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(30 * 1000) // 30 seconds connect timeout
                    .setSocketTimeout(30 * 1000)  // 30 seconds socket timeout
                    .build();

            HttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            HttpGet request = new HttpGet("https://www.bitkom.org/Presse/Presseinformation/Kundenbewertungen-sind-wichtigste-Kaufhilfe.html");

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {

                System.out.println("Link is valid(HTTP response code: " + statusCode + ")");

            } else {
                System.err.println("Link is broken (HTTP response code: " + statusCode + ")");

            }
        } catch (Exception e) {

            System.err.println("Exception occurred: " + e.getMessage());

        }

        Response respons = RestAssured.given().get("https://www.bitkom.org/Presse/Presseinformation/Kundenbewertungen-sind-wichtigste-Kaufhilfe.html").then().extract().response();
        respons.getStatusCode();
    }
    }




