package com.nn.testcase.homepage;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

public class LearnRestAPI {

    @Test
    public void getMethod(){
        RestAssured.baseURI = "https://automationexercise.com/api/productsList";
        RequestSpecification requestSpecification = RestAssured.given();
        Response response = requestSpecification.request(Method.GET);
        System.out.println(response.asPrettyString());
        System.out.println(response.getStatusLine());

        String prodct = response.jsonPath().getString("products[0].id");
        System.out.println(prodct);

    }
    @Test
    public void postMethod(){
       Response response = RestAssured.given().header("Content-Type","application/json")
                .body("{\n" +
                        "   \"name\": \"Apple MacBook Pro 16\",\n" +
                        "   \"data\": {\n" +
                        "      \"year\": 2019,\n" +
                        "      \"price\": 1849.99,\n" +
                        "      \"CPU model\": \"Intel Core i9\",\n" +
                        "      \"Hard disk size\": \"1 TB\"\n" +
                        "   }\n" +
                        "}")
                .baseUri("https://api.restful-api.dev/objects")
                .request(Method.POST);

    }
}
