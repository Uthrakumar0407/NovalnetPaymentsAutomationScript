package com.nn.callback;

import com.google.gson.*;
import com.nn.apis.GetTransactionDetailApi;
import com.nn.constants.Constants;
import com.nn.utilities.DriverActions;
import com.nn.utilities.Log;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.nn.callback.CallbackV2.Event.*;
import static com.nn.callback.CallbackV2.InstalmentCancelTypes.*;
import static com.nn.callback.CallbackV2.Payment.*;
import static com.nn.callback.CallbackV2.RefundPaymentTypes.*;
import static com.nn.callback.CallbackV2.ResultStatus.*;
import static com.nn.callback.CallbackV2.SubscriptionUpdateType.*;
import static com.nn.callback.CallbackV2.SubscriptionUpdateType.AMOUNT;
import static com.nn.callback.CallbackV2.TransactionStatus.*;
import static com.nn.callback.CallbackV2.UpdateType.*;

public class CallbackV2Service {

    private String getCallbackRequestPayload(CallbackV2 callback){
        Response tidResponse = GetTransactionDetailApi.getTransactionAPI(callback.getTid());
        JsonPath tidResponseJson = tidResponse.jsonPath();
        //validate tid
        if(!validateTID(tidResponseJson)){
            Assert("Invalid tid \n"+tidResponseJson.prettyPrint());
        }
        //validate event
        Objects.requireNonNull(callback.getEvent(),"Invalid event type");
        Enum<?>[] payments = CallbackV2.getPaymentsGroupByEvents().get(callback.getEvent());
        Objects.requireNonNull(payments,"Invalid event type");
        //validate payment type
        boolean isValidPayment = Arrays.stream(payments)
                .map(String::valueOf)
                .anyMatch(callback.getPaymentType()::equals);
        if(!isValidPayment){
            Assert(String.format("Invalid payment type %s for the given event %s",callback.getPaymentType(), callback.getEvent()));
        }
        String tidPaymentType = tidResponseJson.get("transaction.payment_type");
        Enum<?>[] relatedPayments = CallbackV2.getIndividualPayments().get(CallbackV2.Payment.valueOf(tidPaymentType));
        boolean isValidTIDForGivenEvents = Arrays.stream(relatedPayments)
                .map(String::valueOf)
                .anyMatch(callback.getPaymentType()::equals);
        //validate payment type with given events
        if(!isValidTIDForGivenEvents){
            Assert(String.format("Invalid tid %s for the given payment type %s and event type %s and the tid original payment type is %s",callback.getTid(), tidPaymentType, callback.getEvent(), callback.getPaymentType()));
        }
        //validate subscription tid
        boolean validateSubscriptionEventAgainstTID = Arrays.stream(CallbackV2.SubscriptionEvents.values())
                .map(String::valueOf)
                .anyMatch(s->s.equals(String.valueOf(callback.getEvent())) && tidResponseJson.get("subscription") == null);
        if(validateSubscriptionEventAgainstTID){
            Assert(String.format("Invalid tid %s for subscription",callback.getTid()));
        }


        //create parent and child tid
        JsonObject callbackJson = new JsonObject();
        JsonObject jsonObject = JsonParser.parseString(tidResponse.asString()).getAsJsonObject();

        JsonObject event = new JsonObject();
        event.addProperty("checksum","checksum"); //temp to insert the checksum at top
        callbackJson.add("event",event);

        //adding tid values into callback payload
        jsonObject.entrySet().forEach(ev->callbackJson.add(ev.getKey(),ev.getValue()));


        JsonObject transactionJson = callbackJson.getAsJsonObject("transaction");
        JsonObject resultJson = callbackJson.getAsJsonObject("result");

        List<CallbackV2.Event> childParentTIDPayments = List.of(CREDIT,TRANSACTION_REFUND, CHARGEBACK, INSTALMENT, INSTALMENT_CANCEL, RENEWAL);
        if(childParentTIDPayments.contains(callback.getEvent())){
            event.addProperty("parent_tid",transactionJson.get("tid").getAsLong());
            event.addProperty("tid",Long.parseLong(getRandomTID()));
            if(!transactionJson.get("status").getAsString().equals(String.valueOf(CONFIRMED))){
                transactionJson.addProperty("status",String.valueOf(CONFIRMED));
                transactionJson.addProperty("status_code",100);
            }
        }else{
            event.addProperty("tid",transactionJson.get("tid").getAsLong());
        }
        event.addProperty("type",String.valueOf(callback.getEvent()));

        //To remove the payment data for unnecessary events
        if(!callback.getEvent().equals(PAYMENT) && !callback.getEvent().equals(INSTALMENT)){
            if(transactionJson.has("payment_data")){
                transactionJson.remove("payment_data");
            }
            if(transactionJson.has("nearest_stores")){
                transactionJson.remove("nearest_stores");
            }
            if(transactionJson.has("txn_secret")){
                transactionJson.remove("txn_secret");
            }
        }

        //To execute the followup events for orders confirmed from communication break. because we get the result status as FAILURE for progress TIDs
        if(!callback.getEvent().equals(PAYMENT)){
            resultJson.addProperty("status","SUCCESS");
            resultJson.addProperty("status_code",100);
            resultJson.addProperty("status_text","Successful");
            if(transactionJson.has("additional_message")){
                transactionJson.remove("additional_message");
            }
        }

        //To make the tid transaction status to confirmed for the followup events
        List<CallbackV2.Event> followUpPaymentsHasTxnStatusConfirmed = List.of(SUBSCRIPTION_REACTIVATE,SUBSCRIPTION_CANCEL,SUBSCRIPTION_SUSPEND,SUBSCRIPTION_UPDATE);
        if(followUpPaymentsHasTxnStatusConfirmed.contains(callback.getEvent())){
            if(!transactionJson.get("status").getAsString().equals(String.valueOf(CONFIRMED))){
                transactionJson.addProperty("status",String.valueOf(CONFIRMED));
                transactionJson.addProperty("status_code",100);
            }if(transactionJson.get("payment_type").getAsString().equals(String.valueOf(INVOICE)) || transactionJson.get("payment_type").getAsString().equals(String.valueOf(PREPAYMENT))){
                transactionJson.addProperty("status",String.valueOf(PENDING));
            }
        }

        //Event specific changes
        switch (callback.getEvent()){
            case PAYMENT:
                resultJson.addProperty("status",String.valueOf(callback.getResultStatus()));
                transactionJson.addProperty("amount",callback.getAmount());
                if(callback.getResultStatus().equals(SUCCESS)){
                    resultJson.addProperty("status_code",100);
                    resultJson.addProperty("status_text","Successful");
                    resultJson.addProperty("status_text","Successful");
                    transactionJson.addProperty("status", String.valueOf(callback.getTransactionStatus()));
                    transactionJson.addProperty("status_code", 100);
                    if(callback.getTransactionStatus().equals(ON_HOLD)){
                        var paymentType = transactionJson.get("payment_type").getAsString();
                        if(paymentType.equals(String.valueOf(CREDITCARD)) || paymentType.equals(String.valueOf(GOOGLEPAY)) || paymentType.equals(String.valueOf(APPLEPAY))){
                            transactionJson.addProperty("status_code", 98);
                        }
                        if(paymentType.equals(String.valueOf(DIRECT_DEBIT_SEPA)) || paymentType.equals(String.valueOf(GUARANTEED_DIRECT_DEBIT_SEPA)) || paymentType.equals(String.valueOf(INSTALMENT_DIRECT_DEBIT_SEPA))){
                            transactionJson.addProperty("status_code", 99);
                        }
                        if(paymentType.equals(String.valueOf(INVOICE)) || paymentType.equals(String.valueOf(GUARANTEED_INVOICE)) || paymentType.equals(String.valueOf(INSTALMENT_INVOICE))){
                            transactionJson.addProperty("status_code", 91);
                        }
                        if(paymentType.equals(String.valueOf(PAYPAL))){
                            transactionJson.addProperty("status_code", 85);
                        }
                    }
                    if(callback.getTransactionStatus().equals(PENDING)){
                        var paymentType = transactionJson.get("payment_type").getAsString();
                        if(paymentType.equals(String.valueOf(PRZELEWY24))){
                            transactionJson.addProperty("status_code", 86);
                        }
                        if(paymentType.equals(String.valueOf(TRUSTLY)) || paymentType.equals(String.valueOf(GUARANTEED_DIRECT_DEBIT_SEPA)) || paymentType.equals(String.valueOf(INSTALMENT_DIRECT_DEBIT_SEPA)) || paymentType.equals(String.valueOf(GUARANTEED_INVOICE)) || paymentType.equals(String.valueOf(INSTALMENT_INVOICE))){
                            transactionJson.addProperty("status_code", 75);
                        }
                        if(paymentType.equals(String.valueOf(POSTFINANCE_CARD)) || paymentType.equals(String.valueOf(POSTFINANCE))){
                            transactionJson.addProperty("status_code", 83);
                        }
                        if(paymentType.equals(String.valueOf(PAYPAL))){
                            transactionJson.addProperty("status_code", 90);
                        }
                    }
                }else {
                    transactionJson.addProperty("status", String.valueOf(callback.getResultStatus()));
                    if(transactionJson.get("payment_type").getAsString().equals(String.valueOf(CREDITCARD))){
                        transactionJson.addProperty("status_code", 96);
                        resultJson.addProperty("status_code", 96);
                        resultJson.addProperty("status_text","3D-Secure authentication failed");
                    }else{
                        transactionJson.addProperty("status_code", 94);
                        resultJson.addProperty("status_code", 94);
                        resultJson.addProperty("status_text","User aborted the transaction");
                    }
                }
                break;

            case TRANSACTION_CAPTURE:
                transactionJson.addProperty("status_code",100);
                if(transactionJson.get("payment_type").getAsString().equals(String.valueOf(INVOICE))) {
                    transactionJson.addProperty("status", String.valueOf(PENDING));
                }else {
                    transactionJson.addProperty("status",String.valueOf(CONFIRMED));
                }
                var paymentType = transactionJson.get("payment_type").getAsString();
                if(paymentType.equals(String.valueOf(INSTALMENT_DIRECT_DEBIT_SEPA)) || paymentType.equals(String.valueOf(INSTALMENT_INVOICE))){
                    JsonObject instalment = callbackJson.getAsJsonObject("instalment");
                    instalment.addProperty("cycles_executed", 1);
                    instalment.addProperty("pending_cycles", callback.getInstalmentCycleCount()-1);
                    var cycleDates = new JsonObject();
                    for(int i=1;i<=callback.getInstalmentCycleCount();i++){
                        cycleDates.addProperty(String.valueOf(i),DriverActions.getUpcomingMonthDatesInArr(callback.getInstalmentCycleCount())[i-1]);
                    }
                    instalment.add("cycle_dates", cycleDates);
                }
                if(paymentType.equals(String.valueOf(GUARANTEED_INVOICE)) || paymentType.equals(String.valueOf(INSTALMENT_INVOICE))){
                    if(!transactionJson.has("due_date")){
                        transactionJson.addProperty("due_date",DriverActions.calculateUpcomingDate(30));
                    }
                }
                break;

            case TRANSACTION_CANCEL:
                transactionJson.addProperty("status_code",103);
                transactionJson.addProperty("status", String.valueOf(DEACTIVATED));
                if(callbackJson.has("instalment")){
                    callbackJson.remove("instalment");
                }
                transactionJson.remove("currency");
                transactionJson.remove("amount");
                break;

            case TRANSACTION_REFUND:
                var refundJson = new JsonObject();
                if(callback.getAmount() != 0){
                    refundJson.addProperty("amount", callback.getAmount());
                }
                else{
                    if(callback.getPaymentType().equals(String.valueOf(INSTALMENT_INVOICE_BOOKBACK)) || callback.getPaymentType().equals(String.valueOf(INSTALMENT_SEPA_BOOKBACK))){
                        refundJson.addProperty("amount",callbackJson.getAsJsonObject("instalment").get("cycle_amount").getAsInt());
                    }else {
                        refundJson.addProperty("amount",transactionJson.get("amount").getAsInt());
                    }
                }
                if(callbackJson.has("instalment")){
                    callbackJson.remove("instalment");
                }
                transactionJson.addProperty("refunded_amount",refundJson.get("amount").getAsInt());
                refundJson.addProperty("payment_type",callback.getPaymentType());
                refundJson.addProperty("currency",transactionJson.get("currency").getAsString());
                refundJson.addProperty("tid",event.get("tid").getAsLong());
                transactionJson.add("refund",refundJson);
                break;

            case TRANSACTION_UPDATE:
                var updateType = callback.getUpdateType();
                transactionJson.addProperty("update_type",String.valueOf(updateType));
                if(updateType.equals(STATUS)){
                    transactionJson.addProperty("status",String.valueOf(callback.getTransactionStatus()));
                    if(callback.getTransactionStatus().equals(CONFIRMED)){
                        transactionJson.addProperty("status_code",100);
                    } else if(callback.getTransactionStatus().equals(DEACTIVATED)){
                        transactionJson.addProperty("status_code",103);
                        if(callbackJson.has("instalment")){
                            callbackJson.remove("instalment");
                        }
                    } else if (callback.getTransactionStatus().equals(PENDING) && callback.getPaymentType().equals(String.valueOf(PAYPAL))) {
                        transactionJson.addProperty("status_code",90);
                    } else if (callback.getTransactionStatus().equals(ON_HOLD)) {
                        var statusCode = (callback.getPaymentType().equals(String.valueOf(INSTALMENT_INVOICE)) || callback.getPaymentType().equals(String.valueOf(GUARANTEED_INVOICE)) ? 91 : 99 );
                        transactionJson.addProperty("status_code",statusCode);
                    }
                    if(callback.getPaymentType().equals(String.valueOf(INSTALMENT_DIRECT_DEBIT_SEPA)) || callback.getPaymentType().equals(String.valueOf(INSTALMENT_INVOICE))){
                        if(callback.getInstalmentCycleCount() != 0){
                            JsonObject instalment = callbackJson.getAsJsonObject("instalment");
                            instalment.addProperty("cycles_executed", 1);
                            instalment.addProperty("pending_cycles", callback.getInstalmentCycleCount()-1);
                            var cycleDates = new JsonObject();
                            for(int i=1;i<=callback.getInstalmentCycleCount();i++){
                                cycleDates.addProperty(String.valueOf(i),DriverActions.getUpcomingMonthDatesInArr(callback.getInstalmentCycleCount())[i-1]);
                            }
                            instalment.add("cycle_dates", cycleDates);
                        }
                    }
                }else if(updateType.equals(CallbackV2.UpdateType.AMOUNT)){
                    transactionJson.addProperty("amount", callback.getAmount());
                }else if(updateType.equals(DUE_DATE)){
                    transactionJson.addProperty("due_date", callback.getDueDate());
                }else if(updateType.equals(AMOUNT_DUE_DATE)){
                    transactionJson.addProperty("amount", callback.getAmount());
                    transactionJson.addProperty("due_date", callback.getDueDate());
                }
                if(callback.getPaymentType().equals(String.valueOf(INVOICE))){
                    transactionJson.addProperty("status_code",100);
                    transactionJson.addProperty("status",String.valueOf(PENDING));
                }
                //update the due date for pending tid
                if(callback.getPaymentType().equals(String.valueOf(GUARANTEED_INVOICE)) || callback.getPaymentType().equals(String.valueOf(INSTALMENT_INVOICE))){
                    if(!transactionJson.has("due_date")){
                        transactionJson.addProperty("due_date",DriverActions.calculateUpcomingDate(30));
                    }
                }
                break;

            case CREDIT:
                transactionJson.addProperty("payment_type",callback.getPaymentType());
                transactionJson.addProperty("tid",event.get("tid").getAsLong());
                if(callback.getAmount() != 0){
                    transactionJson.addProperty("amount", callback.getAmount());
                }if(transactionJson.has("bank_details")){
                    transactionJson.add("credit_details",transactionJson.get("bank_details"));
                    transactionJson.remove("bank_details");
                }
                break;

            case CHARGEBACK:
                transactionJson.addProperty("payment_type",callback.getPaymentType());
                transactionJson.addProperty("tid",event.get("tid").getAsLong());
                if(callback.getAmount() != 0){
                    transactionJson.addProperty("amount", callback.getAmount());
                }
                transactionJson.addProperty("reason","Fraudulent Transaction - No cardholder authentication");
                transactionJson.addProperty("reason_code",48376);
                break;

            case INSTALMENT:
                JsonObject instalment = callbackJson.getAsJsonObject("instalment");
                instalment.addProperty("cycles_executed",callback.getCyclesExecuted());
                instalment.addProperty("pending_cycles",callback.getPendingCycles());
                if(instalment.get("pending_cycles").getAsInt() == 0){
                    if(instalment.has("next_cycle_date"))
                        instalment.remove("next_cycle_date");
                    //to verify the last cycle amount with total amount
                    if(transactionJson.get("amount").getAsInt() != instalment.get("cycle_amount").getAsInt()){
                        var total = instalment.get("cycles_executed").getAsInt() * instalment.get("cycle_amount").getAsInt();
                        var diff = transactionJson.get("amount").getAsInt() - total;
                        instalment.addProperty("cycle_amount", instalment.get("cycle_amount").getAsInt()+ diff);
                    }
                }else{
                    instalment.addProperty("next_cycle_date",callback.getNextInstalmentCycleDate());
                }
                transactionJson.addProperty("tid",event.get("tid").getAsLong());
                if(instalment.has("cycle_dates")){
                    instalment.remove("cycle_dates");
                }
                instalment.remove("tid");
                instalment.remove("currency");
                break;

            case INSTALMENT_CANCEL:
                if(callback.getInstalmentCancelType().equals(ALL_CYCLES)){
                    var instalmentCancelRefund = new JsonObject();
                    instalmentCancelRefund.addProperty("amount",callbackJson.get("instalment").getAsJsonObject().get("cycle_amount").getAsInt());
                    instalmentCancelRefund.addProperty("currency",transactionJson.get("currency").getAsString());
                    if(transactionJson.get("payment_type").getAsString().equals(String.valueOf(INSTALMENT_INVOICE))){
                        instalmentCancelRefund.addProperty("payment_type", String.valueOf(INSTALMENT_INVOICE_BOOKBACK));
                    }
                    if(transactionJson.get("payment_type").getAsString().equals(String.valueOf(INSTALMENT_DIRECT_DEBIT_SEPA))){
                        instalmentCancelRefund.addProperty("payment_type", String.valueOf(INSTALMENT_SEPA_BOOKBACK));
                    }
                    instalmentCancelRefund.addProperty("tid",event.get("tid").getAsLong());
                    transactionJson.add("refund",instalmentCancelRefund);
                }
                if(callback.getInstalmentCancelType().equals(REMAINING_CYCLES)){
                  event.addProperty("tid",event.get("tid").getAsLong());
                  event.remove("parent_tid");
                }
                var instalmentCancel = new JsonObject();
                instalmentCancel.addProperty("cancel_type", String.valueOf(callback.getInstalmentCancelType()));
                instalmentCancel.addProperty("tid", transactionJson.get("tid").getAsLong());
                callbackJson.add("instalment",instalmentCancel);
                break;

            case PAYMENT_REMINDER_1:
            case PAYMENT_REMINDER_2:
                transactionJson.addProperty("payment_type", callback.getPaymentType());
                transactionJson.addProperty("status", String.valueOf(PENDING));
                transactionJson.addProperty("status_code", 100);
                var reminder = new JsonObject();
                reminder.addProperty("claim_amount", transactionJson.get("amount").getAsInt());
                reminder.addProperty("currency", transactionJson.get("currency").getAsString());
                reminder.addProperty("claim_charges", transactionJson.get("amount").getAsInt()*0.1);
                reminder.addProperty("claim_fee", transactionJson.get("amount").getAsInt()*0.2);
                reminder.addProperty("amount", reminder.get("claim_amount").getAsInt()+reminder.get("claim_charges").getAsInt()+reminder.get("claim_fee").getAsInt());
                reminder.addProperty("date", transactionJson.get("date").getAsString());
                callbackJson.add("reminder",reminder);
                break;

            case SUBMISSION_TO_COLLECTION_AGENCY:
                transactionJson.addProperty("payment_type", callback.getPaymentType());
                transactionJson.addProperty("status", String.valueOf(PENDING));
                transactionJson.addProperty("status_code", 100);
                var collection = new JsonObject();
                collection.addProperty("claim_amount", transactionJson.get("amount").getAsInt());
                collection.addProperty("currency", transactionJson.get("currency").getAsString());
                collection.addProperty("claim_charges", transactionJson.get("amount").getAsInt()*0.1);
                collection.addProperty("claim_fee", transactionJson.get("amount").getAsInt()*0.2);
                collection.addProperty("amount", collection.get("claim_amount").getAsInt()+collection.get("claim_charges").getAsInt()+collection.get("claim_fee").getAsInt());
                collection.addProperty("reference", "000025-F00002774");
                collection.addProperty("status_text", "Collection in progress");
                collection.addProperty("date", transactionJson.get("date").getAsString());
                callbackJson.add("collection",collection);
                break;

            case RENEWAL:
                transactionJson.addProperty("amount", callbackJson.get("subscription").getAsJsonObject().get("amount").getAsInt());
                callbackJson.get("subscription").getAsJsonObject().addProperty("next_cycle_date",callback.getNextSubscriptionCycleDate());
                transactionJson.addProperty("tid",event.get("tid").getAsLong());
                break;

            case SUBSCRIPTION_UPDATE:
                if(callback.getSubscriptionUpdateType().equals(AMOUNT)){
                    callbackJson.get("subscription").getAsJsonObject().addProperty("amount", callback.getAmount());
                    transactionJson.remove("amount");
                } else if (callback.getSubscriptionUpdateType().equals(NEXT_CYCLE_DATE)) {
                    callbackJson.get("subscription").getAsJsonObject().addProperty("next_cycle_date", callback.getNextSubscriptionCycleDate());
                    transactionJson.remove("amount");
                } else if (callback.getSubscriptionUpdateType().equals(PAYMENT_TYPE)) {
                    callbackJson.get("subscription").getAsJsonObject().addProperty("payment_type", String.valueOf(callback.getSubscriptionChangePaymentType()));
                    transactionJson.addProperty("payment_type", String.valueOf(callback.getSubscriptionChangePaymentType()));
                    transactionJson.addProperty("tid",Long.parseLong(getRandomTID()));
                    transactionJson.addProperty("amount",0);
                    if(callback.getSubscriptionChangePaymentType().equals(CallbackV2.SubscriptionPayments.CREDITCARD) || callback.getSubscriptionChangePaymentType().equals(CallbackV2.SubscriptionPayments.DIRECT_DEBIT_SEPA) || callback.getSubscriptionChangePaymentType().equals(CallbackV2.SubscriptionPayments.PAYPAL)){
                        var paymentData = new JsonObject();
                        paymentData.addProperty("token","RcB2w22w00a02c-R22w22wXVT22w-V22w24y20u18sDB18sB00a16q14oNZ04ePJ");
                        transactionJson.add("payment_data",paymentData);
                    }
                }
                break;

            case SUBSCRIPTION_SUSPEND:
                callbackJson.get("subscription").getAsJsonObject().remove("amount");
                callbackJson.get("subscription").getAsJsonObject().remove("currency");
                callbackJson.get("subscription").getAsJsonObject().remove("next_cycle_date");
                callbackJson.get("subscription").getAsJsonObject().remove("payment_type");
                transactionJson.remove("amount");
                transactionJson.remove("currency");
                break;

            case SUBSCRIPTION_REACTIVATE:
                callbackJson.get("subscription").getAsJsonObject().remove("payment_type");
                if(!callbackJson.get("subscription").getAsJsonObject().has("next_cycle_date")){
                    callbackJson.get("subscription").getAsJsonObject().addProperty("next_cycle_date",DriverActions.getUpcomingMonthDatesInArr(2)[1]);
                }
                transactionJson.remove("amount");
                transactionJson.remove("currency");
                break;

            case SUBSCRIPTION_CANCEL:
                callbackJson.get("subscription").getAsJsonObject().remove("amount");
                callbackJson.get("subscription").getAsJsonObject().remove("currency");
                callbackJson.get("subscription").getAsJsonObject().remove("next_cycle_date");
                callbackJson.get("subscription").getAsJsonObject().remove("payment_type");
                transactionJson.remove("amount");
                transactionJson.remove("currency");
                callbackJson.get("subscription").getAsJsonObject().addProperty("cancel_type","EXPLICIT");
                callbackJson.get("subscription").getAsJsonObject().addProperty("reason","Too expensive");
                break;

            default:
                throw new RuntimeException("Invalid event type: "+callback.getEvent());
        }

        //creating checksum after payload creation
        var amount = transactionJson.has("amount") ? transactionJson.get("amount").getAsString() : null;
        var currency = transactionJson.has("currency") ? transactionJson.get("currency").getAsString() : null;
        String checksum = generateCheckSum(event.get("tid").getAsString(),event.get("type").getAsString(),resultJson.get("status").getAsString(), amount,currency);
        event.addProperty("checksum",checksum);
        Log.info("****************Callback Payload**********************\n");
        printPrettyJson(callbackJson);
        return callbackJson.toString();
    }

    public String sendCallbackRequest(CallbackV2 callbackV2){
        String callbackPayload = getCallbackRequestPayload(callbackV2);
        var response = sendRequest(System.getProperty("CALLBACK_URL"),callbackPayload);
        Log.info("****************Callback Response**********************\n");
        Log.info(response);
        return response;
    }

    private boolean validateTID(JsonPath response){
        int statusCode = response.get("result.status_code");
        return statusCode != 200018 && statusCode != 1002 && statusCode != 1003 && statusCode != 105 && statusCode != 1007;
    }

    private void Assert(String message){
        Assert.fail("Error occurred in callback process: "+message);
    }

    private String generateCheckSum(String tid,String eventType,String status,String amount,String currency) {
        String tokenString = tid+eventType+status;
        if(amount != null)
            tokenString += amount;
        if(currency != null)
            tokenString += currency;
        tokenString += new StringBuilder(Constants.NOVALNET_ACCESSKEY.trim()).reverse().toString();
        String createdHash;
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenString.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            createdHash =  hexString.toString();
        } catch(RuntimeException | NoSuchAlgorithmException ex) {
            return "RuntimeException while generating checksum " + ex;
        }
        return createdHash;
    }

    private String getRandomTID() {
        Random rand = new Random();
        int length = 15;
        StringBuilder random = new StringBuilder();
        for(int i=0;i<length;i++){
            random.append(rand.nextInt(10));
        }
        return "14"+random;
    }

    private void printPrettyJson(Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(object));
    }

    private static String sendRequest(String Url, String jsonString) {
        HttpResponse<String> httpResponse;
        int responseCode;
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(Url))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .header("Content-Type", "application/json")
                    .header("Charset", "utf-8");

            HttpRequest request = requestBuilder.build();

            httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            responseCode = httpResponse.statusCode();
            Log.info("Http Response Code of callback request : " + responseCode);
        }catch (Exception e){
            throw new RuntimeException("API request failed during callback \n"+e.getMessage());
        }

        if (responseCode != 200) {
            throw new V2CallbackException("API request failed with status code: " + responseCode);
        }

        return httpResponse.body();
    }

}
