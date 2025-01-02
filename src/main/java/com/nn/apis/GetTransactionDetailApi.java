package com.nn.apis;


import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import com.aventstack.extentreports.Status;
import com.nn.constants.Constants;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.Log;

import io.qameta.allure.Step;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class GetTransactionDetailApi {

	
	private static final String ENDPOINT = "https://payport.novalnet.de/v2";
	
	@Step("Get transaction detail for the tid = {tid}")
	public static Response getTransactionAPI(String tid) {
		Header typeHeader = new Header("Content-Type","application/json");
		Header acceptHeader = new Header("Accept","application/json");
		Header charSetHeader = new Header("Charset","utf-8");
		Header accessKeyHeader = new Header("X-NN-Access-Key",Base64.encodeBase64String(Constants.NOVALNET_ACCESSKEY.getBytes()));
		Headers headers = new Headers(typeHeader,acceptHeader,charSetHeader,accessKeyHeader);
		
		JSONObject params = getParameters(tid);
		
		Response response = given()
				.baseUri(ENDPOINT)
				.headers(headers)
				.body(params.toString())
				.when()
				.post("/transaction/details")
				.then()
				.extract()
				.response();
		if(response.getStatusCode() != 200) {
			throw new RuntimeException("Failure to get the transaction details from novalnet for tid : "+tid+" and status code is "+response.getStatusCode());
		}
		Log.info(response.asString());
		//System.out.println(response.asPrettyString());
//		if(ExtentTestManager.getTest() != null)
//			ExtentTestManager.logMessage(response.asString());
		return response;
	}

	
	private static JSONObject getParameters(String tid) {
		JSONObject data = new JSONObject();
		JSONObject transaction = new JSONObject();
		JSONObject custom = new JSONObject();
		transaction.put("tid", tid);
		custom.put("lang", "EN");
		data.put("transaction", transaction);
		data.put("custom", custom);
		return data;
	}

	public static JsonPath getAsJsonPath(String tid){
		return getTransactionAPI(tid).jsonPath();
	}
	
	public static Map<String,Object> getTransactionDetails(String tid) {
		Response response = getTransactionAPI(tid);
		JsonPath responseObject = response.jsonPath();
		Map<String,Object> result = responseObject.getJsonObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			Map<String,Object> transaction = responseObject.getJsonObject("transaction");
			return 	transaction;
		}
		Log.warn("TID details from novalnet server: "+result);		
		ExtentTestManager.logMessage(Status.FAIL, "API result : <b>"+result.get("status_text")+"</b>");
		ExtentTestManager.logMessage(Status.FAIL, "TID details from novalnet server: "+result);
		return null;
	}
	
	public static Map<String,Object> getSubscriptionDetails(String tid) {
		Response response = getTransactionAPI(tid);
		JsonPath responseObject = response.jsonPath();
		Map<String,Object> result = responseObject.getJsonObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			Map<String,Object> transaction = responseObject.getJsonObject("subscription");
			return 	transaction;
		}
		Log.warn("TID details from novalnet server: "+result);		
		ExtentTestManager.logMessage(Status.FAIL, "API result : <b>"+result.get("status_text")+"</b>");
		ExtentTestManager.logMessage(Status.FAIL, "TID details from novalnet server: "+result);
		return null;
	}
	
	public static String getTIDStatus(String tid) {
		Response response = getTransactionAPI(tid);
		JsonPath responseObject = response.jsonPath();
		Map<String,Object> result = responseObject.getJsonObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			Map<String,Object> transaction = responseObject.getJsonObject("transaction");
			return 	transaction.get("status").toString();
		}
		Log.warn("TID details from novalnet server: "+result);		
		ExtentTestManager.logMessage(Status.FAIL, "API result : <b>"+result.get("status_text")+"</b>");
		ExtentTestManager.logMessage(Status.FAIL, "TID details from novalnet server: "+result);
		return null;
	}
	
	public static Map<String,Object> getKeyValues(String tid,String key) {
		Response response = getTransactionAPI(tid);
		JsonPath responseObject = response.jsonPath();
		Map<String,Object> result = responseObject.getJsonObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			return 	responseObject.getJsonObject(key);
		}
		Log.warn("TID details from novalnet server: "+result);		
		ExtentTestManager.logMessage(Status.FAIL, "API result : <b>"+result.get("status_text")+"</b>");
		ExtentTestManager.logMessage(Status.FAIL, "TID details from novalnet server: "+result);
		return null;
	}
	
	public static Map<String,Object> getKeyValues(String tid,String key, String innerKey) {
		Response response = getTransactionAPI(tid);
		JsonPath responseObject = response.jsonPath();
		Map<String,Object> result = responseObject.getJsonObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			JSONObject data = new JSONObject(response.getBody().asString());
			JSONObject nodeOne = data.getJSONObject(key);
			JSONObject nodeTwo = nodeOne.getJSONObject(innerKey);
			return 	nodeTwo.toMap();
		}
		Log.warn("TID details from novalnet server: "+result);		
		ExtentTestManager.logMessage(Status.FAIL, "API result : <b>"+result.get("status_text")+"</b>");
		ExtentTestManager.logMessage(Status.FAIL, "TID details from novalnet server: "+result);
		return null;
	}
	
	public static String getValue(String tid, String object, String key) {
		Response response = getTransactionAPI(tid);
		JsonPath responseObject = response.jsonPath();
		Map<String,Object> result = responseObject.getJsonObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			Map<String,Object> individualObj = responseObject.getJsonObject(object);
			return 	individualObj.get(key).toString();
		}
		Log.warn("TID details from novalnet server: "+result);		
		ExtentTestManager.logMessage(Status.FAIL, "API result : <b>"+result.get("status_text")+"</b>");
		ExtentTestManager.logMessage(Status.FAIL, "TID details from novalnet server: "+result);
		return null;
	}
	
	public static String getValue(String tid, String object, String innerObj, String key) {
		Response response = getTransactionAPI(tid);
		JsonPath responseObject = response.jsonPath();
		Map<String,Object> result = responseObject.getJsonObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			JSONObject data = new JSONObject(response.getBody().asString());
			JSONObject nodeOne = data.getJSONObject(object);
			JSONObject nodeTwo = nodeOne.getJSONObject(innerObj);
			return 	nodeTwo.getString(key);
		}
		Log.warn("TID details from novalnet server: "+result);		
		ExtentTestManager.logMessage(Status.FAIL, "API result : <b>"+result.get("status_text")+"</b>");
		ExtentTestManager.logMessage(Status.FAIL, "TID details from novalnet server: "+result);
		return null;
	}
	
	
	public static boolean containsValue(String tid, String object, String innerObj, String key) {
		boolean exist = false;
		Response response = getTransactionAPI(tid);
		JSONObject responseJsonObj = new JSONObject(response.getBody().asString());
		JSONObject result = responseJsonObj.getJSONObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			JSONObject node = null;
			JSONObject nodeTwo = null;
			if(responseJsonObj.has(object)) {
				node = responseJsonObj.getJSONObject(object);
				if(node.has(innerObj)) {
					nodeTwo = node.getJSONObject(innerObj);
					if(nodeTwo.has(key)) {
						exist = true;
						return exist;
					}
				}
			}
		}else {
			Log.warn("TID details from novalnet server: "+result);		
			ExtentTestManager.logMessage("<b>API result : ["+result.get("status_text")+"]</b>");
			ExtentTestManager.logMessage("<b>TID details from novalnet server: </b>"+response.asString());
		}	
		return exist;
	}
	
	public static boolean containsValue(String tid, String object, String key) {
		boolean exist = false;
		Response response = getTransactionAPI(tid);
		JSONObject responseJsonObj = new JSONObject(response.getBody().asString());
		JSONObject result = responseJsonObj.getJSONObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			JSONObject node = null;
			if(responseJsonObj.has(object)) {
				node = responseJsonObj.getJSONObject(object);
				if(node.has(key)) {
					exist = true;
					return exist;
				}
			}
		}else {
			Log.warn("TID details from novalnet server: "+result);		
			ExtentTestManager.logMessage("<b>API result : ["+result.get("status_text")+"]</b>");
			ExtentTestManager.logMessage("<b>TID details from novalnet server: </b>"+response.asString());
		}	
		return exist;
	}
	
	public static boolean containsValue(String tid, String object) {
		boolean exist = false;
		Response response = getTransactionAPI(tid);
		JSONObject responseJsonObj = new JSONObject(response.getBody().asString());
		JSONObject result = responseJsonObj.getJSONObject("result");
		String originalTIDStatusCode = result.get("status_code").toString();
		if(!originalTIDStatusCode.equals("200018") && !originalTIDStatusCode.equals("1002") && !originalTIDStatusCode.equals("105") && !originalTIDStatusCode.equals("1007")) {
			if(responseJsonObj.has(object)) {
					exist = true;
					return exist;
			}else {
				return exist;
			}
		}else {
			Log.warn("TID details from novalnet server: "+result);		
			ExtentTestManager.logMessage("<b>API result : ["+result.get("status_text")+"]</b>");
			ExtentTestManager.logMessage("<b>TID details from novalnet server: </b>"+response.asString());
		}	
		return exist;
	}
	
	
	
}
