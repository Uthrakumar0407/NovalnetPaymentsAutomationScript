package com.nn.utilities;

import javax.mail.*;
import javax.mail.search.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nn.drivers.DriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.nn.constants.Constants.mailOrderSubject;

public class ThunderBirdEmailHelper {

    private static String orderSubjectText="Your Main Website Store order confirmation";

    private static String invoiceSubjectText="Invoice for your Main Website Store order";


    public static Store connectToThunderBird() {
        // Set the properties for connecting to the IMAP server
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imap");
        properties.put("mail.imap.host", "mail1.novalnetsolutions.com");
        properties.put("mail.imap.port", "993"); // IMAP port
        properties.put("mail.imap.ssl.enable", "true");

        try {
            // Create a session with the properties
            Session session = Session.getInstance(properties);
            // Connect to the email server
            Store store = session.getStore();
            store.connect("automation_test@novalnetsolutions.com", "Hhfh4ewo&owgbjeEwOjfpjf#feojYfi2"); // never share
            return store;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static List<String> getAllEmails(Store store, String subjectText) {
        List<String> emailContents = new ArrayList<>();
        Folder inbox = null;

        try {
            if (store != null) {
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                SearchTerm searchTerm = new SubjectTerm(subjectText);
                Message[] messages = inbox.search(searchTerm);

                for (Message message : messages) {
                    Object content = message.getContent();
                    Document doc = Jsoup.parse(content.toString());
                    String emailContent = doc.text();
                    emailContents.add(emailContent);
                }
            } else {
                System.out.println("Unable to connect to Thunderbird");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the inbox, but keep the store open for further processing if needed
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return emailContents;
    }


    private static String getTodayLatestEmail(Store store, String subject) {
        Folder inbox = null;
        LocalDate today = LocalDate.now();
        String emailContent = "";
        try {
            // Open the inbox folder
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Create a search term for subject and today's date
            Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            SearchTerm searchTerm = new AndTerm(
                    new SubjectTerm(subject),
                    new ReceivedDateTerm(ComparisonTerm.EQ, todayDate)
            );
            // Perform the search
            Message[] messages = inbox.search(searchTerm);
            // Sort the messages by received date in descending order
            Arrays.sort(messages, (m1, m2) -> {
                try {
                    Date d1 = m1.getReceivedDate();
                    Date d2 = m2.getReceivedDate();
                    if (d1 == null || d2 == null) {
                        return 0;
                    }
                    return d2.compareTo(d1);
                } catch (MessagingException e) {
                    e.printStackTrace();
                    return 0;
                }
            });
            // Retrieve the latest email
            if (messages.length > 0) {
                Message latestEmail = messages[0];
                // Extract and return email content
                Object content = latestEmail.getContent();
                Document doc = Jsoup.parse(content.toString());
                // Extract plain text
                emailContent = doc.text();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the inbox and store
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return emailContent;
    }


    public static String getEmailByOrderNo(Store store, String subject, String orderNo) {
        Folder inbox = null;
        String emailContent = "";
        try {
            if (store != null) {
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                SearchTerm subjectTerm = new SubjectTerm(subject);
                Message[] messages = inbox.search(subjectTerm);
                for (Message message : messages) {
                    Object content = message.getContent();
                    Document doc = Jsoup.parse(content.toString());
                    String mailContent = doc.text();
                    String actualOrderNo = findOrderNumber(mailContent, orderNo);
                    if (actualOrderNo != null) {
                        emailContent = mailContent;
                        break; // Found a matching email, exit the loop
                    }
                }
            } else {
                System.out.println("Unable to connect to Thunderbird");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!emailContent.isEmpty()) {
            return emailContent;
        } else {
            return "No email with order number: " + orderNo + " found.";
        }

    }

    public static String getTodaysEmailByOrderNoWithRetry(Store store, String subject, String orderNo) {
        DriverActions.setExpectedCondition(d-> getTodaysEmailByOrderNo(store,subject,orderNo) != null,5,
                "Mail is not received for the order number :"+orderNo+" with this subject: "+subject);
        return getTodaysEmailByOrderNo(store,subject,orderNo);
    }

    public static String getTodaysEmailByOrderNoForWoocommerce(Store store, String subject, String orderNo) {
        DriverActions.setExpectedCondition(d-> getEmailByOrderNoShopware(store,subject,orderNo,"[Order #") != null,5,
                "Mail is not received for the order number :"+orderNo+" with this subject: "+subject);
        return getEmailByOrderNoShopware(store,subject,orderNo,"[Order #");
    }

    public static String getTodaysEmailByOrderNoForShopware(Store store, String subject, String orderNo) {
        DriverActions.setExpectedCondition(d-> getEmailByOrderNoShopware(store,subject,orderNo,"Order number: ") != null,5,
                "Mail is not received for the order number :"+orderNo+" with this subject: "+subject);
        return getEmailByOrderNoShopware(store,subject,orderNo,"Order number: ");
    }

    public static String getEmailByOrderNoShopware(Store store, String subject, String orderNo, String prefix) {
        Folder inbox = null;
        LocalDate today = LocalDate.now();
        var folders = new String[]{"INBOX", "Junk"};
        try {
            for (String folder : folders) {
                // Open the inbox folder
                inbox = store.getFolder(folder);
                inbox.open(Folder.READ_ONLY);

                // Create a search term for subject and today's date
                Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
                SearchTerm searchTerm = new AndTerm(
                        new SubjectTerm(subject),
                        new ReceivedDateTerm(ComparisonTerm.EQ, todayDate)
                );

                // Perform the search
                Message[] messages = inbox.search(searchTerm);

                // Iterate through the messages
                for (Message message : messages) {
                    // Extract and return email content
                    Object content = message.getContent();

                    if (content instanceof Multipart) {
                        Multipart multiPart = (Multipart) content;

                        // Initialize a StringBuilder to store the complete email content
                        StringBuilder emailContentBuilder = new StringBuilder();
                        StringBuilder commentsBuilder = new StringBuilder(); // To store Comments

                        for (int i = 0; i < multiPart.getCount(); i++) {
                            BodyPart part = multiPart.getBodyPart(i);

                            // Check if the part is text or HTML
                            if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
                                String partContent = part.getContent().toString();

                                if (part.isMimeType("text/html")) {
                                    partContent = Jsoup.parse(partContent).text();
                                }

                                // Append the part content to the email content builder
                                emailContentBuilder.append(partContent);

                                // Check if the "Comments" section is present
                                if (partContent.contains("Comments:")) {
                                    commentsBuilder.append(partContent);
                                }
                            }
                        }

                        // Extract and print the complete email content
                        String mailContent = emailContentBuilder.toString();

                        if (mailContent.contains(prefix + orderNo)) {
                            System.out.println("Mail content for order number " + orderNo + ": " + mailContent);
                            String commentsSection = commentsBuilder.toString();
                            if (!commentsSection.isEmpty()) {
                                System.out.println("Comments: " + commentsSection);
                            }
                            return mailContent;
                        }
                    }else {
                        var txt = Jsoup.parse(content.toString()).text();
                        if (txt.contains(prefix + orderNo)) {
                            return txt;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the inbox and store
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Mail is not received for the order number: " + orderNo + " with this subject: " + subject);
        return null;
    }

    public static String getTodaysEmailByOrderNo(Store store, String subject, String orderNo) {
        Folder inbox = null;
        LocalDate today = LocalDate.now();

        try {
            // Open the inbox folder
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Create a search term for subject and today's date
            Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            SearchTerm searchTerm = new AndTerm(
                    new SubjectTerm(subject),
                    new ReceivedDateTerm(ComparisonTerm.EQ, todayDate)
            );

            // Perform the search
            Message[] messages = inbox.search(searchTerm);

            // Iterate through the messages
            for (Message message : messages) {
                // Extract and return email content
                Object content = message.getContent();
                Document doc = Jsoup.parse(content.toString());

                // Extract and print plain text
                String mailContent = doc.text();

                String actualOrderNo = findOrderNumber(mailContent, orderNo);
                if (actualOrderNo != null) {
                    Log.info("Mail content for order number "+orderNo+": "+mailContent);
                    return mailContent;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the inbox and store
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.info("Mail is not received still.. for the order number: "+orderNo+" with this subject: "+subject);
        return null;
    }

    public static String getTodaysEmailByOrderNo(Store store, String subject, String orderNo, String prefix) {
        Folder inbox = null;
        LocalDate today = LocalDate.now();

        try {
            // Open the inbox folder
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Create a search term for subject and today's date
            Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            SearchTerm searchTerm = new AndTerm(
                    new SubjectTerm(subject),
                    new ReceivedDateTerm(ComparisonTerm.EQ, todayDate)
            );

            // Perform the search
            Message[] messages = inbox.search(searchTerm);

            // Iterate through the messages
            for (Message message : messages) {
                // Extract and return email content
                Object content = message.getContent();
                Document doc = Jsoup.parse(content.toString());

                // Extract and print plain text
                String mailContent = doc.text();

                if (mailContent.contains(prefix+orderNo)) {
                    Log.info("Mail content for order number "+orderNo+": "+mailContent);
                    return mailContent;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the inbox and store
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.info("Mail is not received still.. for the order number: "+orderNo+" with this subject: "+subject);
        return null;
    }


    public static void deleteAllEmails(Store store, String subjectText) {

        if (store != null) {
            try {
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE); // Open inbox in read-write mode

                SearchTerm searchTerm = new SubjectTerm(subjectText);
                Message[] messages = inbox.search(searchTerm);

                for (Message message : messages) {
                    // Delete the message
                    message.setFlag(Flags.Flag.DELETED, true);
                }

                // Expunge to permanently delete the marked messages
                inbox.expunge();

                // Close the inbox and the store
                inbox.close(true);
                store.close();

                System.out.println("Deleted emails with subject: " + subjectText);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Unable to connect to Thunderbird");
        }
    }


    public static void deleteAllJunkEmails(Store store, String subjectText) {

        if (store != null) {
            try {
                Folder inbox = store.getFolder("Junk");
                inbox.open(Folder.READ_WRITE); // Open inbox in read-write mode

                SearchTerm searchTerm = new SubjectTerm(subjectText);
                Message[] messages = inbox.search(searchTerm);

                for (Message message : messages) {
                    // Delete the message
                    message.setFlag(Flags.Flag.DELETED, true);
                }

                // Expunge to permanently delete the marked messages
                inbox.expunge();

                // Close the inbox and the store
                inbox.close(true);
                store.close();

                System.out.println("Deleted emails with subject: " + subjectText);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Unable to connect to Thunderbird");
        }
    }



    private static String findOrderNumber(String content,String orderNo) {
        // Define a regex pattern to match the full order number string "Order #3000000008"
        Pattern pattern = Pattern.compile("Order #[0-9]+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            // Check if the matched text is the full "Order #3000000008" format
            if (matcher.group().equals("Order #" + orderNo)) {
                return matcher.group();
            }
        }

        return null;
    }

    private static String findOrderNumber(String content,String orderNo, String regex) {
        return DriverActions.getFirstMatchRegex(content,regex);
    }

    private static void displayAllEmailContents(String subject) {
        Store store =connectToThunderBird();
        List<String> emailContents = getAllEmails(store, subject);
        // Process email contents
        for (String emailContent : emailContents) {
            System.out.println("Email Content :\n" + emailContent);
        }
        // Close the store here after processing all emails
        if (store != null) {
            try {
                store.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static void displayTodaysLatestEmailContent(String subject) {
        Store store =connectToThunderBird();
        String emailContent = getTodayLatestEmail(store,subject);
        // Close the store here after processing all emails
        if (store != null) {
            try {
                System.out.println("Email Content:\n" + emailContent);
                store.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void displayTodaysEmailContentByOrderNo(String subject, String orderNo) {
        Store store =connectToThunderBird();
        String emailContent = getTodaysEmailByOrderNo(store,subject,orderNo);
        // Close the store here after processing all emails
        if (store != null) {
            try {
                System.out.println("Email Content:\n" + emailContent);
                store.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void displayEmailContentByOrderNo(String subject, String orderNo) {
        Store store =connectToThunderBird();
        String emailContent = getEmailByOrderNo(store,subject,orderNo);
        // Close the store here after processing all emails
        if (store != null) {
            try {
                System.out.println("Email Content:\n" + emailContent);
                store.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getEmailByOrderNo(Store store, String subject, String orderNo, String prefix) {
        Folder inbox = null;
        LocalDate today = LocalDate.now();

        try {
            // Open the inbox folder
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Create a search term for subject and today's date
            Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            SearchTerm searchTerm = new AndTerm(
                    new SubjectTerm(subject),
                    new ReceivedDateTerm(ComparisonTerm.EQ, todayDate)
            );

            // Perform the search
            Message[] messages = inbox.search(searchTerm);

            // Iterate through the messages
            for (Message message : messages) {
                // Extract and return email content
                Object content = message.getContent();

                // Check if the content is multipart
                if (content instanceof Multipart) {
                    Multipart multiPart = (Multipart) content;

                    // Iterate through the parts of the email
                    for (int i = 0; i < multiPart.getCount(); i++) {
                        BodyPart part = multiPart.getBodyPart(i);

                        // Check if the part is text or HTML
                        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
                            String mailContent = part.getContent().toString();
                            Document doc = Jsoup.parse(mailContent);

                            // Extract and print plain text
                            mailContent = doc.text();

                            if (mailContent.contains(prefix + orderNo)) {
                                System.out.println("Mail content for order number " + orderNo + ": " + mailContent);
                                return mailContent;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the inbox and store
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Mail is not received still.. for the order number: " + orderNo + " with this subject: " + subject);
        return null;
    }


}
