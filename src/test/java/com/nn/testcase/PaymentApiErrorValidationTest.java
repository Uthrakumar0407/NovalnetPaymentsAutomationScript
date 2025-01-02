package com.nn.testcase;


import com.nn.constants.Constants;
import com.nn.reports.ExtentTestManager;
import io.qameta.allure.Step;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class PaymentApiErrorValidationTest  {

    private static final String ENDPOINT = "https://payport.novalnet.de/v2";
    private static final String ACCESS_KEY="a87ff679a2f3e71d9181a67b7542122c";
    private static final String NOVALNET_TARIFF="10004";

    @Test(dataProvider = "errorCodesProvider")
    public void testPaymentApiErrorCode(String errorCode) {
        // Generate payload based on the error code
        String payload ="";
        Response response=null;
        if(errorCode.trim().equals("0402006")){
        payload=generatePayload(errorCode,"INVOICE","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("419001")){
            payload=generatePayload(errorCode,"INVOICE","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
            response = callRefundApi(String.valueOf(tid),"1000000","Product not statisfied");
            // Validate the response based on the error code
           validateResponse(response, Integer.parseInt(errorCode));
        }

        if(errorCode.trim().equals("437001")){
            payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
            response = callRefundApi(String.valueOf(tid),"1000000","Product not statisfied");
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("402011")){
            payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
            response = callTransactionUpdateApi(String.valueOf(tid),"5000000000","","","");
           // Validate the response based on the error code
           validateResponse(response, Integer.parseInt(errorCode)); }

        if(errorCode.trim().equals("600004")){
            payload=generatePayload(errorCode,"INVOICE","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
            response = cancelTransactionApi(String.valueOf(tid));
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("200002")){
            payload=generatePayload(errorCode,"INVOICE1","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("440013")){
            response = callCaptureApi("14852000036014564");
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}


        if(errorCode.trim().equals("200018")){
            response = cancelTransactionApi("14762000048224542");
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}


        if(errorCode.trim().equals("0440014")){
            payload=generatePayload(errorCode,"GUARANTEED_INVOICE","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("75")){
            payload=generatePayload(errorCode,"GUARANTEED_INVOICE","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
            response = callRefundApi(String.valueOf(tid),"2500","Product not statisfied");
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}




     if(errorCode.trim().equals("103")){
        payload=generatePayload(errorCode,"GUARANTEED_INVOICE","10004","");
        // Call the API with the generated payload
        response = callPaymentApi(payload);
        JSONObject responseBody = new JSONObject(response.getBody().asString());
        JSONObject transaction = responseBody.getJSONObject("transaction");
        long tid = transaction.getLong("tid");
        response = cancelTransactionApi(String.valueOf(tid));
        // Validate the response based on the error code
        validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("440002")){
            payload=generatePayload(errorCode,"GUARANTEED_INVOICE","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
           // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("91")){
            payload=generatePayload(errorCode,"INVOICE","10004","");
            // Call the API with the generated payload
            response = callAuthorizeApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
            response = callRefundApi(String.valueOf(tid),"2500","Product not statisfied");
           // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode)); }

        if(errorCode.trim().equals("99")){
            payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callAuthorizeApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
             response = callRefundApi(String.valueOf(tid),"2500","Product not statisfied");
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode)); }

        if(errorCode.trim().equals("16")){
            payload=generatePayloadWithSubScription(errorCode,"DIRECT_DEBIT_SEPA","10005","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
            response = callSubscriptionCancel(String.valueOf(tid));
            response = callSubscriptionSuspend(String.valueOf(tid));
            // Validate the response based on the error code
           validateSubscriptionResponse(response, Integer.parseInt(errorCode)); }

        if(errorCode.trim().equals("14")){
            payload=generatePayloadWithSubScription(errorCode,"DIRECT_DEBIT_SEPA","10005","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
            response = callSubscriptionCancel(String.valueOf(tid));
            response = callSubscriptionUpdate(String.valueOf(tid));
            // Validate the response based on the error code
            validateSubscriptionResponse(response, Integer.parseInt(errorCode)); }

        if(errorCode.trim().equals("440002")){
            payload=generateInvalidPayload(errorCode,"GUARANTEED_INVOICE","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("1005")){
            payload=generateZeroAmountPayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            JSONObject responseBody = new JSONObject(response.getBody().asString());
            JSONObject transaction = responseBody.getJSONObject("transaction");
            long tid = transaction.getLong("tid");
            response = callTransactionUpdateandDueDateApi(String.valueOf(tid),"0",LocalDate.now().toString(),"","","");
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("1007")){
            response = callTransactionUpdateandDueDateApi(String.valueOf("1485290001770639"),"0",LocalDate.now().toString(),"","","");
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("1008")){
            payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("1002")){
            payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callPaymentApiWithoutAuthentication(payload);
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("1003")){
            payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callPaymentApiWithInvalidHeader(payload);
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("1004")){
            payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callInvalidPaymentApi(payload);
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}



        if(errorCode.trim().equals("502021")){
            payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            System.out.println(response.asPrettyString());
             // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("300030")){
        payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
        // Call the API with the generated payload
        response = callPaymentApi(payload);
        System.out.println(response.asPrettyString());
        // Validate the response based on the error code
        validateResponse(response, Integer.parseInt(errorCode));}

        if(errorCode.trim().equals("0440004")){
            payload=generatePayload(errorCode,"DIRECT_DEBIT_SEPA","10004","");
            // Call the API with the generated payload
            response = callPaymentApi(payload);
            System.out.println(response.asPrettyString());
            // Validate the response based on the error code
            validateResponse(response, Integer.parseInt(errorCode));}
}



    @DataProvider(name = "errorCodesProvider")
    public Object[][] errorCodesProvider() {
        // Provide different error codes as test data
        return new Object[][]{
               {"0402006"},
                {"419001"},
                {"600004"},
                {"437001"},{"402011"},{"200002"},{"440013"},{"0440014"},{"75"},{"103"},{"440002"},{"91"},{"99"},{"16"}, {"200018"},{"14"}, {"440002"}, {"1005"} ,{"1007"},{"1002"},{"1003"},{"1004"},{"0437002"},{"502021"},{"300030"},{"0440004"}
                // Add more error codes as needed
                //435005 - refund ->amount ""
                //502023 call any instalemnt cancell pass sepa txn
                //300040 instalment sepa
                //200070 cashpayment
                //406002 googlepay



        };
    }

    @Step
    private Response callPaymentApi(String payload) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload)
                .when()
                .post("/payment")
                .then()
                .extract()
                .response();
    }

    @Step
    private Response callInvalidPaymentApi(String payload) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload)
                .when()
                .post("/payment1")
                .then()
                .extract()
                .response();
    }
    @Step
    private Response callPaymentApiWithoutAuthentication(String payload) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", "aaa");
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        return given()
                .baseUri(ENDPOINT)
             //   .headers(headers)
                .body(payload)
                .when()
                .post("/payment")
                .then()
                .extract()
                .response();
    }

    @Step
    private Response callPaymentApiWithInvalidHeader(String payload) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", "aaa");
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload)
                .when()
                .post("/payment")
                .then()
                .extract()
                .response();
    }
    @Step
    private Response callAuthorizeApi(String payload) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload)
                .when()
                .post("/authorize")
                .then()
                .extract()
                .response();
    }

    @Step
    private Response callRefundApi(String tid, String refundAmount, String reason) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        // Create payload dynamically using JSONObject
        JSONObject payload = new JSONObject();

        JSONObject transaction = new JSONObject();
        transaction.put("tid", tid);
        transaction.put("amount", refundAmount);
        transaction.put("reason", reason);

        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");

        payload.put("transaction", transaction);
        payload.put("custom", custom);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload.toString())
                .when()
                .post("/transaction/refund")
                .then()
                .extract()
                .response();
    }

    @Step
    private Response callSubscriptionSuspend(String tid) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        // Create payload dynamically using JSONObject
        JSONObject payload = new JSONObject();

        JSONObject subs = new JSONObject();
        subs.put("tid", tid);

        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");

        payload.put("subscription", subs);
        payload.put("custom", custom);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload.toString())
                .when()
                .post("/subscription/suspend")
                .then()
                .extract()
                .response();
    }

    @Step
    private Response callSubscriptionUpdate(String tid) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        // Create payload dynamically using JSONObject
        JSONObject payload = new JSONObject();

        JSONObject subs = new JSONObject();
        subs.put("tid", tid);
        subs.put("amount", 6000);
        subs.put("interval", "3m");
        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");

        payload.put("subscription", subs);
        payload.put("custom", custom);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload.toString())
                .when()
                .post("/subscription/update")
                .then()
                .extract()
                .response();
    }

    @Step
    private Response callSubscriptionReactivate(String tid) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        // Create payload dynamically using JSONObject
        JSONObject payload = new JSONObject();

        JSONObject subs = new JSONObject();
        subs.put("tid", tid);

        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");

        payload.put("subscription", subs);
        payload.put("custom", custom);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload.toString())
                .when()
                .post("/subscription/reactivate")
                .then()
                .extract()
                .response();
    }


    private Response callSubscriptionCancel(String tid) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        // Create payload dynamically using JSONObject
        JSONObject payload = new JSONObject();

        JSONObject subs = new JSONObject();
        subs.put("tid", tid);
        subs.put("reason","product not statisfied");

        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");

        payload.put("subscription", subs);
        payload.put("custom", custom);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload.toString())
                .when()
                .post("/subscription/cancel")
                .then()
                .extract()
                .response();
    }

    @Step
    private Response callCaptureApi(String tid) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        // Create payload dynamically using JSONObject
        JSONObject payload = new JSONObject();

        JSONObject transaction = new JSONObject();
        transaction.put("tid", tid);
        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");

        payload.put("transaction", transaction);
        payload.put("custom", custom);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload.toString())
                .when()
                .post("/transaction/capture")
                .then()
                .extract()
                .response();
    }


    @Step
    private Response callTransactionUpdateApi(String tid, String amount, String orderNo, String invoiceNo, String invoiceRef) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        // Create payload dynamically using JSONObject
        JSONObject payload = new JSONObject();

        JSONObject transaction = new JSONObject();
        transaction.put("tid", tid);
        transaction.put("amount", amount);
        transaction.put("order_no", orderNo);
        transaction.put("invoice_no", invoiceNo);
        transaction.put("invoice_ref", invoiceRef);

        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");

        payload.put("transaction", transaction);
        payload.put("custom", custom);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload.toString())
                .when()
                .post("/transaction/update")
                .then()
                .extract()
                .response();
    }

    @Step
    private Response callTransactionUpdateandDueDateApi(String tid, String amount, String dueDate,String orderNo, String invoiceNo, String invoiceRef) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        // Create payload dynamically using JSONObject
        JSONObject payload = new JSONObject();

        JSONObject transaction = new JSONObject();
        transaction.put("tid", tid);
        transaction.put("amount", amount);
        transaction.put("due_date", dueDate);
        transaction.put("order_no", orderNo);
        transaction.put("invoice_no", invoiceNo);
        transaction.put("invoice_ref", invoiceRef);

        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");

        payload.put("transaction", transaction);
        payload.put("custom", custom);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload.toString())
                .when()
                .post("/transaction/update")
                .then()
                .extract()
                .response();
    }

    @Step
    private Response cancelTransactionApi(String tid) {
        Header typeHeader = new Header("Content-Type", "application/json");
        Header acceptHeader = new Header("Accept", "application/json");
        Header charSetHeader = new Header("Charset", "utf-8");
        Header accessKeyHeader = new Header("X-NN-Access-Key", Base64.encodeBase64String(ACCESS_KEY.getBytes()));
        Headers headers = new Headers(typeHeader, acceptHeader, charSetHeader, accessKeyHeader);

        // Create payload dynamically using JSONObject
        JSONObject payload = new JSONObject();

        JSONObject transaction = new JSONObject();
        transaction.put("tid", tid);

        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");

        payload.put("transaction", transaction);
        payload.put("custom", custom);

        return given()
                .baseUri(ENDPOINT)
                .headers(headers)
                .body(payload.toString())
                .when()
                .post("/transaction/cancel")
                .then()
                .extract()
                .response();
    }

    @Step("Validate Response for Error Code {1}")
    private void validateResponse(Response response, int expectedErrorCode) {
        Assert.assertEquals(response.getStatusCode(), 200, "Unexpected status code");

        // Extract relevant information from the response
        JSONObject responseBody = new JSONObject(response.getBody().asString());

        // Validate Result status code
        JSONObject result = responseBody.getJSONObject("result");
        int actualMainErrorCode=result.getInt("status_code");

        if(!((expectedErrorCode==103) || (expectedErrorCode==99))){

        assertValidation("Result",actualMainErrorCode, expectedErrorCode, result);

        // Validate transaction status code (if applicable)

        if (responseBody.has("invoicing")) {
            JSONObject invoicing = responseBody.getJSONObject("invoicing");
            int actualInvoicingErrorCode = invoicing.getInt("status_code");
            assertValidation("Invoicing",actualInvoicingErrorCode, expectedErrorCode, invoicing);
        }}

        // Validate transaction status code (if applicable)
        if(!((expectedErrorCode==419001) || (expectedErrorCode==402011))){
        if (responseBody.has("transaction")) {
            JSONObject transaction = responseBody.getJSONObject("transaction");
            int actualTransactionErrorCode = transaction.getInt("status_code");
            assertValidation("Transaction",actualTransactionErrorCode, expectedErrorCode, transaction);
        }}

    }

    @Step("Validate Subscription Response for Error Code {1}")
    private void validateSubscriptionResponse(Response response, int expectedErrorCode) {
        Assert.assertEquals(response.getStatusCode(), 200, "Unexpected status code");

        // Extract relevant information from the response
        JSONObject responseBody = new JSONObject(response.getBody().asString());

        // Validate Result status code
        JSONObject result = responseBody.getJSONObject("result");
        int actualMainErrorCode=result.getInt("status_code");
        assertValidation("Result",actualMainErrorCode, expectedErrorCode, result);

    }


    @Step
    public static String generatePayload(String errorCode,String paymentName,String tariffID,String projectID) {

        JSONObject payload = new JSONObject();

        // Merchant details
        JSONObject merchant = new JSONObject();
        if (errorCode.trim().equals("1008"))
            merchant.put("signature", "7ibc7ob5|tuJEH3gNbeWJfIHah||nbobljbnmdli0poys|doU3HJVoym7c");
        else
            merchant.put("signature", "7ibc7ob5|tuJEH3gNbeWJfIHah||nbobljbnmdli0poys|doU3HJVoym7MQ44qf7cpn7pc");
        merchant.put("tariff", tariffID);
        merchant.put("project", projectID);
        payload.put("merchant", merchant);

        // Customer details
        JSONObject customer = new JSONObject();
        customer.put("gender", "{{encodedKeys}}");
        customer.put("first_name", "Norbert");
        customer.put("last_name", "Maier");
        customer.put("email", "test@novanet.de");


        // Billing address
        JSONObject billingAddress = new JSONObject();
        if (errorCode.trim().equals("0440014"))
            billingAddress.put("company", "A.B.C. Gerüstbau GmbH");
        else billingAddress.put("company", "{{billing_company}}");

        billingAddress.put("house_no", "9");
        billingAddress.put("street", "Hauptstr");
        billingAddress.put("city", "Kaiserslautern");
        billingAddress.put("zip", "66862");
        if (errorCode.trim().equals("440002"))
            billingAddress.put("country_code", "AE");
        else
            billingAddress.put("country_code", "DE");

        billingAddress.put("state", "Germany");

        customer.put("billing", billingAddress);
        customer.put("customer_ip", "{{customer_ip}}");
        customer.put("customer_no", "12345");
        customer.put("birth_date", "1960-01-01");
        customer.put("tel", "{{tel}}");
        customer.put("mobile", "{{mobile}}");
        customer.put("fax", "{{fax}}");

        // Shipping details (using same as billing for simplicity)
        JSONObject shippingAddress = new JSONObject(billingAddress);
        customer.put("shipping", shippingAddress);

        customer.put("vat_id", "{{vat_id}}");
        customer.put("tax_id", "{{tax_id}}");
        customer.put("reg_no", "{{reg_no}}");
        customer.put("session", "{{session}}");

        payload.put("customer", customer);

        // Transaction details
        JSONObject transaction = new JSONObject();
        transaction.put("test_mode", 1);
        transaction.put("payment_type", paymentName);
        if (errorCode.trim().equals("0402006"))
            transaction.put("amount", "");
        else
            transaction.put("amount", "5000");
        if(errorCode.trim().equals("502021"))
        transaction.put("currency", "EU");
        else if(errorCode.trim().equals("300030"))
        transaction.put("currency", "BRL");
        else if(errorCode.trim().equals("0440004"))
        transaction.put("currency", "USD");
        else
        transaction.put("currency", "EUR");
        transaction.put("order_no", "{{order_no}}");
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("due_date", "{{inv_due_date}}");
        transaction.put("invoice_no", "{{invoice_no}}");
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("return_url", "{{return_url}}");
        transaction.put("error_return_url", "{{error_return_url}}");


        // Payment_data
        if (paymentName.trim().equals("DIRECT_DEBIT_SEPA")) {
            JSONObject payment_data = new JSONObject();
            payment_data.put("account_holder", "Max Mustermann");
            if (!(errorCode.trim().equals("437001"))) {
                payment_data.put("iban", "DE24300209002411761956");
                payment_data.put("bic", "CMCIDEDDXXX");
            }
         if (errorCode.trim().equals("0437002")) {
            payment_data.put("iban", "DE24300209002411761956");
            payment_data.put("bic", "");
        }
            transaction.put("payment_data", payment_data);

    }


            payload.put("transaction", transaction);

            // Custom details
            JSONObject custom = new JSONObject();
            custom.put("lang", "EN");
            custom.put("input1", "{{input1}}");
            custom.put("inputval1", "{{inputval1}}");

            payload.put("custom", custom);

            return payload.toString();
    }

    public static String generateZeroAmountPayload(String errorCode,String paymentName,String tariffID,String projectID) {

        JSONObject payload = new JSONObject();

        // Merchant details
        JSONObject merchant = new JSONObject();
        merchant.put("signature", "7ibc7ob5|tuJEH3gNbeWJfIHah||nbobljbnmdli0poys|doU3HJVoym7MQ44qf7cpn7pc");
        merchant.put("tariff", tariffID);
        merchant.put("project", projectID);
        payload.put("merchant", merchant);

        // Customer details
        JSONObject customer = new JSONObject();
        customer.put("gender", "{{encodedKeys}}");
        customer.put("first_name", "Norbert");
        customer.put("last_name", "Maier");
        customer.put("email", "test@novanet.de");


        // Billing address
        JSONObject billingAddress = new JSONObject();
        if(errorCode.trim().equals("0440014"))
            billingAddress.put("company", "A.B.C. Gerüstbau GmbH");
        else billingAddress.put("company", "{{billing_company}}");

        billingAddress.put("house_no", "9");
        billingAddress.put("street", "Hauptstr");
        billingAddress.put("city", "Kaiserslautern");
        billingAddress.put("zip", "66862");
        if(errorCode.trim().equals("440002"))
            billingAddress.put("country_code", "AE");
        else
            billingAddress.put("country_code", "DE");

        billingAddress.put("state", "Germany");

        customer.put("billing", billingAddress);
        customer.put("customer_ip", "{{customer_ip}}");
        customer.put("customer_no", "12345");
        customer.put("birth_date", "1960-01-01");
        customer.put("tel", "{{tel}}");
        customer.put("mobile", "{{mobile}}");
        customer.put("fax", "{{fax}}");

        // Shipping details (using same as billing for simplicity)
        JSONObject shippingAddress = new JSONObject(billingAddress);
        customer.put("shipping", shippingAddress);

        customer.put("vat_id", "{{vat_id}}");
        customer.put("tax_id", "{{tax_id}}");
        customer.put("reg_no", "{{reg_no}}");
        customer.put("session", "{{session}}");

        payload.put("customer", customer);

        // Transaction details
        JSONObject transaction = new JSONObject();
        transaction.put("test_mode", 1);
        transaction.put("payment_type", paymentName);
        transaction.put("amount", "0");
        transaction.put("currency", "EUR");
        transaction.put("order_no", "{{order_no}}");
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("due_date", "{{inv_due_date}}");
        transaction.put("invoice_no", "{{invoice_no}}");
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("return_url", "{{return_url}}");
        transaction.put("error_return_url", "{{error_return_url}}");


        // Payment_data
        if(paymentName.trim().equals("DIRECT_DEBIT_SEPA")){
            JSONObject payment_data = new JSONObject();
            payment_data.put("account_holder", "Max Mustermann");
            if(!(errorCode.trim().equals("437001"))){
                payment_data.put("iban", "DE24300209002411761956");
                payment_data.put("bic", "CMCIDEDDXXX");}
            transaction.put("payment_data",payment_data);

        }

        payload.put("transaction", transaction);

        // Custom details
        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");
        custom.put("input1", "{{input1}}");
        custom.put("inputval1", "{{inputval1}}");

        payload.put("custom", custom);

        return payload.toString();
    }

    public static String generateInvalidPayload(String errorCode,String paymentName,String tariffID,String projectID) {

        JSONObject payload = new JSONObject();

        // Merchant details
        JSONObject merchant = new JSONObject();
        merchant.put("signature", "7ibc7ob5|tuJEH3gNbeWJfIHah||nbobljbnmdli0poys|doU3HJVoym7MQ44qf7cpn7pc");
        merchant.put("tariff", tariffID);
        merchant.put("project", projectID);
        payload.put("merchant", merchant);

        // Customer details
        JSONObject customer = new JSONObject();
        customer.put("gender", "{{encodedKeys}}");
        customer.put("first_name", "Norbert1");
        customer.put("last_name", "Maier1");
        customer.put("email", "test1@novanet.de");


        // Billing address
        JSONObject billingAddress = new JSONObject();


        billingAddress.put("house_no", "9");
        billingAddress.put("street", "Hauptstr1");
        billingAddress.put("city", "Kaiserslautern");
        billingAddress.put("zip", "66862");
        billingAddress.put("country_code", "DE");
        billingAddress.put("state", "Germany");

        customer.put("billing", billingAddress);
        customer.put("customer_ip", "{{customer_ip}}");
        customer.put("customer_no", "12345");
        customer.put("birth_date", "1960-01-01");
        customer.put("tel", "{{tel}}");
        customer.put("mobile", "{{mobile}}");
        customer.put("fax", "{{fax}}");

        // Shipping details (using same as billing for simplicity)
        JSONObject shippingAddress = new JSONObject(billingAddress);
        customer.put("shipping", shippingAddress);

        customer.put("vat_id", "{{vat_id}}");
        customer.put("tax_id", "{{tax_id}}");
        customer.put("reg_no", "{{reg_no}}");
        customer.put("session", "{{session}}");

        payload.put("customer", customer);

        // Transaction details
        JSONObject transaction = new JSONObject();
        transaction.put("test_mode", 1);
        transaction.put("payment_type", paymentName);
        transaction.put("amount", "5000");
        transaction.put("currency", "EUR");
        transaction.put("order_no", "{{order_no}}");
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("due_date", "{{inv_due_date}}");
        transaction.put("invoice_no", "{{invoice_no}}");
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("return_url", "{{return_url}}");
        transaction.put("error_return_url", "{{error_return_url}}");

        payload.put("transaction", transaction);

        // Custom details
        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");
        custom.put("input1", "{{input1}}");
        custom.put("inputval1", "{{inputval1}}");

        payload.put("custom", custom);

        return payload.toString();
    }

    @Step
    public static String generatePayloadWithSubScription(String errorCode,String paymentName,String tariffID,String projectID) {

        JSONObject payload = new JSONObject();

        // Merchant details
        JSONObject merchant = new JSONObject();
        merchant.put("signature", "7ibc7ob5|tuJEH3gNbeWJfIHah||nbobljbnmdli0poys|doU3HJVoym7MQ44qf7cpn7pc");
        merchant.put("tariff", tariffID);
        merchant.put("project", projectID);
        payload.put("merchant", merchant);

        // Customer details
        JSONObject customer = new JSONObject();
        customer.put("gender", "{{encodedKeys}}");
        customer.put("first_name", "Norbert");
        customer.put("last_name", "Maier");
        customer.put("email", "test@novanet.de");


        // Billing address
        JSONObject billingAddress = new JSONObject();
        if(errorCode.trim().equals("0440014"))
            billingAddress.put("company", "A.B.C. Gerüstbau GmbH");
        else billingAddress.put("company", "{{billing_company}}");

        billingAddress.put("house_no", "9");
        billingAddress.put("street", "Hauptstr");
        billingAddress.put("city", "Kaiserslautern");
        billingAddress.put("zip", "66862");
        if(errorCode.trim().equals("440002"))
            billingAddress.put("country_code", "AE");
        else
            billingAddress.put("country_code", "DE");

        billingAddress.put("state", "Germany");

        customer.put("billing", billingAddress);
        customer.put("customer_ip", "{{customer_ip}}");
        customer.put("customer_no", "12345");
        customer.put("birth_date", "1960-01-01");
        customer.put("tel", "{{tel}}");
        customer.put("mobile", "{{mobile}}");
        customer.put("fax", "{{fax}}");

        // Shipping details (using same as billing for simplicity)
        JSONObject shippingAddress = new JSONObject(billingAddress);
        customer.put("shipping", shippingAddress);

        customer.put("vat_id", "{{vat_id}}");
        customer.put("tax_id", "{{tax_id}}");
        customer.put("reg_no", "{{reg_no}}");
        customer.put("session", "{{session}}");

        payload.put("customer", customer);

        // Transaction details
        JSONObject transaction = new JSONObject();
        transaction.put("test_mode", 1);
        transaction.put("payment_type", paymentName);
        if(errorCode.trim().equals("0402006"))
            transaction.put("amount", "");
        else
            transaction.put("amount", "5000");
        transaction.put("currency", "EUR");
        transaction.put("order_no", "{{order_no}}");
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("due_date", "{{inv_due_date}}");
        transaction.put("invoice_no", "{{invoice_no}}");
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("return_url", "{{return_url}}");
        transaction.put("error_return_url", "{{error_return_url}}");



        // Payment_data
        if(paymentName.trim().equals("DIRECT_DEBIT_SEPA")){
            JSONObject payment_data = new JSONObject();
            payment_data.put("account_holder", "Max Mustermann");
            if(!(errorCode.trim().equals("437001"))){
                payment_data.put("iban", "DE24300209002411761956");
                payment_data.put("bic", "CMCIDEDDXXX");}
            transaction.put("payment_data",payment_data);

        }

        payload.put("transaction", transaction);


        // Subscription details
        JSONObject subs = new JSONObject();
        subs.put("interval", "3m");
        subs.put("trial_interval", "3m");
        subs.put("trial_amount", 100);
        subs.put("expiry_date", LocalDate.now().plusMonths(10));
        payload.put("subscription", subs);

        // Custom details
        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");
        custom.put("input1", "{{input1}}");
        custom.put("inputval1", "{{inputval1}}");

        payload.put("custom", custom);

        return payload.toString();
    }

    @Step
    public static String generatePayloadwithOrderNo(String errorCode,String paymentName,String tariffID,String projectID) {

        String randomNum = "productName_" + RandomStringUtils.random(10);
        UUID uuid = UUID.nameUUIDFromBytes(randomNum.getBytes());
        String orderNum = uuid.toString().substring(0, 5);

        JSONObject payload = new JSONObject();
        // Merchant details
        JSONObject merchant = new JSONObject();
        merchant.put("signature", "7ibc7ob5|tuJEH3gNbeWJfIHah||nbobljbnmdli0poys|doU3HJVoym7MQ44qf7cpn7pc");
        merchant.put("tariff", tariffID);
        merchant.put("project", projectID);
        payload.put("merchant", merchant);

        // Customer details
        JSONObject customer = new JSONObject();
        customer.put("gender", "{{encodedKeys}}");
        customer.put("first_name", "Norbert");
        customer.put("last_name", "Maier");
        customer.put("email", "test@novanet.de");


        // Billing address
        JSONObject billingAddress = new JSONObject();
        if(errorCode.trim().equals("0440014"))
            billingAddress.put("company", "A.B.C. Gerüstbau GmbH");
        else billingAddress.put("company", "{{billing_company}}");

        billingAddress.put("house_no", "9");
        billingAddress.put("street", "Hauptstr");
        billingAddress.put("city", "Kaiserslautern");
        billingAddress.put("zip", "66862");
        if(errorCode.trim().equals("440002"))
            billingAddress.put("country_code", "AE");
        else
            billingAddress.put("country_code", "DE");

        billingAddress.put("state", "Germany");

        customer.put("billing", billingAddress);
        customer.put("customer_ip", "{{customer_ip}}");
        customer.put("customer_no", "12345");
        customer.put("birth_date", "1960-01-01");
        customer.put("tel", "{{tel}}");
        customer.put("mobile", "{{mobile}}");
        customer.put("fax", "{{fax}}");

        // Shipping details (using same as billing for simplicity)
        JSONObject shippingAddress = new JSONObject(billingAddress);
        customer.put("shipping", shippingAddress);

        customer.put("vat_id", "{{vat_id}}");
        customer.put("tax_id", "{{tax_id}}");
        customer.put("reg_no", "{{reg_no}}");
        customer.put("session", "{{session}}");

        payload.put("customer", customer);

        // Transaction details
        JSONObject transaction = new JSONObject();
        transaction.put("test_mode", 1);
        transaction.put("payment_type", paymentName);
        if(errorCode.trim().equals("0402006"))
            transaction.put("amount", "");
        else
            transaction.put("amount", "5000");
        transaction.put("currency", "EUR");
        transaction.put("order_no", orderNum);
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("due_date", "{{inv_due_date}}");
        transaction.put("invoice_no", "{{invoice_no}}");


        // Payment_data
        if(paymentName.trim().equals("DIRECT_DEBIT_SEPA")){
            JSONObject payment_data = new JSONObject();
            payment_data.put("account_holder", "Max Mustermann");
            if(!(errorCode.trim().equals("437001"))){
                payment_data.put("iban", "DE24300209002411761956");
                payment_data.put("bic", "CMCIDEDDXXX");}
            transaction.put("payment_data",payment_data);

        }

        payload.put("transaction", transaction);

        // Custom details
        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");
        custom.put("input1", "{{input1}}");
        custom.put("inputval1", "{{inputval1}}");

        payload.put("custom", custom);

        return payload.toString();
    }

    public static String generatePayloadWithoutAmount(String errorCode) {

        JSONObject payload = new JSONObject();

        // Merchant details
        JSONObject merchant = new JSONObject();
        merchant.put("signature", "7ibc7ob5|tuJEH3gNbeWJfIHah||nbobljbnmdli0poys|doU3HJVoym7MQ44qf7cpn7pc");
        merchant.put("tariff", "10004");
        payload.put("merchant", merchant);

        // Customer details
        JSONObject customer = new JSONObject();
        customer.put("gender", "{{encodedKeys}}");
        customer.put("first_name", "Norbert");
        customer.put("last_name", "Maier");
        customer.put("email", "test@novanet.de");


        // Billing address
        JSONObject billingAddress = new JSONObject();
        billingAddress.put("company", "{{billing_company}}");
        billingAddress.put("house_no", "9");
        billingAddress.put("street", "Hauptstr");
        billingAddress.put("city", "Kaiserslautern");
        billingAddress.put("zip", "66862");
        billingAddress.put("country_code", "DE");
        billingAddress.put("state", "Germany");

        customer.put("billing", billingAddress);
        customer.put("customer_ip", "{{customer_ip}}");
        customer.put("customer_no", "12345");
        customer.put("birth_date", "1960-01-01");
        customer.put("tel", "{{tel}}");
        customer.put("mobile", "{{mobile}}");
        customer.put("fax", "{{fax}}");

        // Shipping details (using same as billing for simplicity)
        JSONObject shippingAddress = new JSONObject(billingAddress);
        customer.put("shipping", shippingAddress);

        customer.put("vat_id", "{{vat_id}}");
        customer.put("tax_id", "{{tax_id}}");
        customer.put("reg_no", "{{reg_no}}");
        customer.put("session", "{{session}}");

        payload.put("customer", customer);

        // Transaction details
        JSONObject transaction = new JSONObject();
        transaction.put("test_mode", 1);
        transaction.put("payment_type", "INVOICE");

        transaction.put("currency", "EUR");
        transaction.put("order_no", "{{order_no}}");
        transaction.put("hook_url", "{{hook_url}}");
        transaction.put("due_date", "{{inv_due_date}}");
        transaction.put("invoice_no", "{{invoice_no}}");

        payload.put("transaction", transaction);

        // Custom details
        JSONObject custom = new JSONObject();
        custom.put("lang", "EN");
        custom.put("input1", "{{input1}}");
        custom.put("inputval1", "{{inputval1}}");

        payload.put("custom", custom);

        return payload.toString();
    }

    @Step("{1} - {3}")
    public static void assertValidation(String tag ,int actualMainErrorCode, int expectedErrorCode,  JSONObject result){
        Assert.assertEquals(actualMainErrorCode, expectedErrorCode, "Status code" + tag +result.toString());
    }
}