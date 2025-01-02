package com.nn.testcase;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
public class WordPressApiExample {

    private static final String BASE_URL = "http://192.168.2.91/gopinath_m/Shops/wordpress_6_2"; // Replace with your site URL
    private static final String CONSUMER_KEY = "ck_31492210aefda4ef37c5cc406b348b7af149a6cb"; // Replace with your consumer key
    private static final String CONSUMER_SECRET = "cs_cb7144300fbdc5f5ff27b3172dd890c2b31ccb21"; // Replace with your consumer secret
    private static final String USER_API_ENDPOINT = "/wp-json/wp/v2/users";

    public static void main(String[] args) {
        createAdministratorUser();
    }

    private static void createAdministratorUser() {
        HttpClient httpClient = HttpClients.createDefault();

        // Step 1: Create the user as a "customer"
        HttpPost httpPost = new HttpPost(BASE_URL + USER_API_ENDPOINT);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        String userJson = "{\"username\":\"new_admin_user\",\"email\":\"admin999@example.com\",\"password\":\"password123\",\"roles\":[\"customer\"]}";

        try {
            StringEntity entity = new StringEntity(userJson);
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);

            // Read the response
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(responseEntity.getContent()))) {
                    String line;
                    StringBuilder responseContent = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        responseContent.append(line);
                    }

                    System.out.println("Create User Response: " + responseContent.toString());
                }
            }

            // Step 2: Update the user's role to "Administrator"
            int userId = getUserIdFromResponse(response);
            if (userId > 0) {
                HttpPut httpPut = new HttpPut(BASE_URL + USER_API_ENDPOINT + "/" + userId);
                httpPut.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

                String roleUpdateJson = "{\"roles\":[\"administrator\"]}";
                entity = new StringEntity(roleUpdateJson);
                httpPut.setEntity(entity);

                response = httpClient.execute(httpPut);

                // Read the response
                responseEntity = response.getEntity();
                if (responseEntity != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(responseEntity.getContent()))) {
                        String line;
                        StringBuilder responseContent = new StringBuilder();
                        while ((line = br.readLine()) != null) {
                            responseContent.append(line);
                        }

                        System.out.println("Update Role Response: " + responseContent.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getUserIdFromResponse(HttpResponse response) {
        try {
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(responseEntity.getContent()))) {
                    String line;
                    StringBuilder responseContent = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        responseContent.append(line);
                    }

                    String responseJson = responseContent.toString();
                    // Extract the user ID from the response
                    // Assuming the response format is JSON and contains an "id" field
                    // You can use a JSON parsing library like Jackson or Gson for more robust parsing
                    return Integer.parseInt(responseJson.substring(responseJson.indexOf("\"id\":") + 5, responseJson.indexOf(",\"date_created\"")));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
