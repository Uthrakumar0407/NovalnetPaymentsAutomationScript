package com.nn.apis;

import com.nn.constants.Constants;
import com.nn.utilities.Log;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.codec.binary.Base64;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static io.restassured.RestAssured.given;

public class NovalnetAPIs {
    private static final String ENDPOINT = "https://payport.novalnet.de/v2/";

    private static Headers getHeaders(){
        Header typeHeader = new Header("Content-Type","application/json");
        Header acceptHeader = new Header("Accept","application/json");
        Header charSetHeader = new Header("Charset","utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(Constants.NOVALNET_ACCESSKEY.getBytes()));
        return new Headers(typeHeader,acceptHeader,charSetHeader,accessKeyHeader);
    }

    private static RequestSpecification getRequestSpecification(){
        return RestAssured.given()
                .baseUri(ENDPOINT)
                .headers(getHeaders());
    }

    @Step("Get transaction detail for the orderNumber = {orderNumber}")
    public static Response getTransactionList(String orderNumber) {
        Response response = getRequestSpecification()
                            .body(getTransactionListPayload(orderNumber))
                            .when()
                            .post("transaction/list")
                            .then()
                            .extract()
                            .response();
        if(response.getStatusCode() != 200) {
            throw new RuntimeException("Failure to get the transaction details from novalnet for orderNumber : "+orderNumber+" and status code is "+response.asPrettyString());
        }
        Log.info(response.asPrettyString());
        return response;
    }

    public static String getTransactionListPayload(String orderNumber){
        return "{\n" +
                "    \"merchant\": {\n" +
                "    \"signature\": \""+Constants.NOVALNET_API_KEY+"\"\n" +
                "    },\n" +
                "    \"transaction\": {\n" +
                "        \"order_no\" : \""+orderNumber+"\"\n" +
                "    },\n" +
                "    \"custom\" : {\n" +
                "        \"lang\" : \"EN\"\n" +
                "    }\n" +
                "}";
    }

    @Test
    public void main() {
        getRefundTransaction("10024");
    }

    public static LinkedHashMap<String, Object> getRefundTransaction(String orderNumber){
        JsonPath jsonPath = getTransactionList(orderNumber).jsonPath();
        List<LinkedHashMap<String,Object>> list = jsonPath.get("list.transactions");
        for(LinkedHashMap<String,Object> lh : list){
            LinkedHashMap<String,Object> tn = (LinkedHashMap<String,Object>)lh.get("transaction");
            if(tn.get("tid_payment") != null){
                return tn;
            }
        }
        return null;
    }

    public static String getRecentTransactionTID(String orderNumber){
        JsonPath jsonPath = getTransactionList(orderNumber).jsonPath();
        return jsonPath.get("list.transactions[0].transaction.tid");
    }


    public static LinkedHashMap<String, Object> getRecentTransaction(String orderNumber){
        JsonPath jsonPath = getTransactionList(orderNumber).jsonPath();
        return jsonPath.get("list.transactions[0].transaction");
    }

}
