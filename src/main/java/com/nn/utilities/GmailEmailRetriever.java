package com.nn.utilities;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.mail.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.*;

public class GmailEmailRetriever {

 private static final String CLIENT_ID = "552816382863-096005plaikj9f3qdtapcajjj48r64ff.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String APPLICATION_NAME = "novalnet solutions";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    public static String getEmailWithSubject(String subject){
        String result = null;
        try{
            result = getEmail(subject);
        }catch (Exception e){
            Assert.fail("Error occurred while reading mail via gmail"+e.getMessage());
        }
        return result;
    }
    private static String getEmail(String subject) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader("credentials.json"));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, Collections.singletonList("https://www.googleapis.com/auth/gmail.readonly"))
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // Use a different LocalServerReceiver with port 8080
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8081).build();

        // Authorize using AuthorizationCodeInstalledApp with the custom receiver

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        Gmail service = getGmailService(credential);
        String content= retrieveEmail(subject, service);
        return content;


    }

    private static String retrieveEmail(String sub, Gmail service) throws IOException {
        String query = "subject:" + sub;
        String emailContent = null;

        List<Message> messages = listMessagesWithQuery(service, "novalnetesolutions2010@gmail.com", query);

        for (Message message : messages) {
            String messageId = message.getId();
            Message fullMessage = getMessage(service, "novalnetesolutions2010@gmail.com", messageId);
            String emailContentFromMessage = extractTextEmailContent(fullMessage);

            // Append the content to the emailContent variable
            if (emailContentFromMessage != null) {
                if (emailContent == null) {
                    emailContent = emailContentFromMessage;
                } else {
                    emailContent += "\n\n" + emailContentFromMessage;
                }
            }
        }

        return emailContent; // Return the accumulated content after processing all emails
    }




    private static Gmail getGmailService(Credential credentials) throws GeneralSecurityException, IOException {
        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static List<Message> listMessagesWithQuery(Gmail service, String userId, String query) throws IOException {
        ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();
        return response.getMessages();
    }

    private static Message getMessage(Gmail service, String userId, String messageId) throws IOException {
        return service.users().messages().get(userId, messageId).execute();
    }

    private static String extractEmailContent(Message message) {
        MessagePartHeader subjectHeader = extractHeader(message.getPayload().getHeaders(), "Subject");
        String subject = subjectHeader != null ? subjectHeader.getValue() : "No subject";

        String bodyData = message.getPayload().getBody().getData();
        String bodyDecoded = base64UrlDecode(bodyData);

        return "Subject: " + subject + "\n" + "Content:\n" + bodyDecoded;
    }

    private static String extractTextEmailContent(Message message) throws IOException {
        MessagePartHeader subjectHeader = extractHeader(message.getPayload().getHeaders(), "Subject");
        String subject = subjectHeader != null ? subjectHeader.getValue() : "No subject";

        String emailContent = "";
        List<MessagePart> parts = message.getPayload().getParts();

        if (parts != null && parts.size() > 0) {
            emailContent = extractPartContent(parts.get(0)); // Extract content from the first part
        } else {
            MessagePart messagePart = message.getPayload();
            if (messagePart.getBody() != null) {
                //  emailContent = base64UrlDecode(messagePart.getBody(Map<String, String> credentials = new HashMap<>();

            }
        }

        return "Subject: " + subject + "\n" + "Content:\n" + emailContent;
    }

    private static String extractPartContent(MessagePart part) {
        String content = "";

        if (part.getMimeType().equals("text/plain") || part.getMimeType().equals("text/html")) {
            String bodyData = part.getBody().getData();
            content = base64UrlDecode(bodyData);
        }

        return content;
    }

    private static MessagePartHeader extractHeader(List<MessagePartHeader> headers, String name) {
        for (MessagePartHeader header : headers) {
            if (header.getName().equals(name)) {
                return header;
            }
        }
        return null;
    }

    private static String base64UrlDecode(String input) {
        byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(input);
        return new String(decodedBytes);
    }

    public static boolean hasAttachment(Message message) {
        MessagePart payload = message.getPayload();
        if (payload != null) {
            List<MessagePart> parts = payload.getParts();
            if (parts != null && !parts.isEmpty()) {
                for (MessagePart part : parts) {
                    if (part.getFilename() != null && !part.getFilename().isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void openAttachment(Gmail service, String userId, String messageId, String attachmentId) throws IOException {
        try {
            Message message = service.users().messages().get(userId, messageId).execute();
            List<MessagePart> parts = message.getPayload().getParts();

            for (MessagePart part : parts) {
                if (part.getFilename() != null && part.getBody() != null && part.getPartId().equals(attachmentId)) {
                    String attachmentData = part.getBody().getData();

                    // Decode base64 attachment data
                    byte[] attachmentBytes = Base64.getDecoder().decode(attachmentData);

                    // Now, you have the attachment content in the 'attachmentBytes' array
                    // You can process, save, or manipulate it as needed

                    // Example: Save the attachment to a file
                    saveAttachmentToFile("attachment.txt", attachmentBytes);
                }
            }
        } catch (IOException e) {
            // Handle exception appropriately
            e.printStackTrace();
        }
    }

    public static void saveAttachmentToFile(String fileName, byte[] attachmentBytes) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            fileOutputStream.write(attachmentBytes);
        } catch (IOException e) {
            // Handle exception appropriately
            e.printStackTrace();
        }

    }
}


