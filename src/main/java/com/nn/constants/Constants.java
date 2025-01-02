package com.nn.constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nn.helpers.PropertyHelpers;
import org.openqa.selenium.By;

public final class Constants {

    public static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("HEADLESS"));
    public static final String URL = System.getProperty("URL");
    public static final String CALLBACK_URL = System.getProperty("CALLBACK_URL");
    public static final String URL_FRONTEND = System.getProperty("URL_FRONTEND");
    public static final String BROWSER = System.getProperty("BROWSER");
    public static final String USERNAME = System.getProperty("USERNAME");
    public static final String PASSWORD = System.getProperty("PASSWORD");
    public static final long EXPLICIT_TIMEOUT = 30; //Long.parseLong(System.getProperty("EXPLICIT_TIMEOUT"));
    public static final long IMPLICIT_TIMEOUT = 10;//Long.parseLong(System.getProperty("IMPLICIT_TIMEOUT"));
    public static final long PAGE_LOAD_TIMEOUT = 60;//Long.parseLong(System.getProperty("PAGE_LOAD_TIMEOUT"));
    public static final String SCREENSHOT_FAIL ="yes";
    public static final String SCREENSHOT_PASS = System.getProperty("SCREENSHOT_PASS");
    public static final String RECORD_VIDEO = System.getProperty("RECORD_VIDEO");
    public static final String REPORT_TITLE =System.getProperty("REPORT_TITLE");
    public static final String PROJECT_PATH = System.getProperty("user.dir")+File.separator;
    public static final String EXTENT_HTML_REPORT_PATH = PROJECT_PATH + "ExtentReports/ExtentReport.html";
    public static final String EXTENT_PDF_REPORT_PATH = PROJECT_PATH + "ExtentReports/PdfReport.pdf";
    public static final String REPORT_SENT_EMAIL = System.getProperty("REPORT_EMAIL");
    public static final String SENT_REPORT_TO_USER_IN_EMAIL = System.getProperty("SENT_REPORT_TO_USER_IN_EMAIL");
          
    public static final String YES = "yes";
    public static final String NO = "no";
    
    //public static final String NOVALNET_API_KEY =  System.getProperty("NOVALNET_API_KEY");

    //public static final String NOVALNET_API_KEY =  System.getProperty("NOVALNET_API_KEY_FILE");

  //  public static final String NOVALNET_API_KEY =  "n7ibc7ob5t|doU3HJVoym7MQ44qonbobljblnmdli0p|qJEH3gNbeWJfIHah||f7cpn7pc";

    public static final String NOVALNET_API_KEY =System.getProperty("NOVALNET_API_KEY").replaceAll("\\\\", "")
            .replaceAll("^\\|+|\\|+$", "").replaceAll("(?<=\\|)\\|\\|(?=\\|)", "| |");
    //public static final String NOVALNET_API_KEY = "7ibc7ob5|xtJEH3gNbeWJfIHah||nbobljbnmdli0poyw|doU3HJVoym7MQ44qf7cpn7pc";
    public static final String NOVALNET_ACCESSKEY =  System.getProperty("NOVALNET_ACCESSKEY");
    //public static final String NOVALNET_ACCESSKEY =  "a87ff679a2f3e71d9181a67b7542122c";
    public static final String NOVALNET_TARIFF = System.getProperty("NOVALNET_TARIFF");
    public static final String NOVALNET_SUBSCRIPTION_TARIFF = System.getProperty("NOVALNET_SUBSCRIPTION_TARIFF");


    public static String MIN_TXN_AMOUNT_AUTH = System.getProperty("MIN_TXN_AMOUNT_AUTH");

    public static String ORDER_AMOUNT = System.getProperty("ORDER_AMOUNT");


    public static  String ALLOWED_COUNTRY = System.getProperty("ALLOWED_COUNTRY");
    public static String ALLOWED_CURRENCY = System.getProperty("ALLOWED_CURRENCY");

    public static  String NON_ALLOWED_COUNTRY = System.getProperty("NON_ALLOWED_COUNTRY");
    public static String NON_ALLOWED_CURRENCY = System.getProperty("NON_ALLOWED_CURRENCY");

    public static final String SHOP_BASE_URL =  System.getProperty("SHOP_BASE_URL");

    public static final String CONSUMER_KEY =  System.getProperty("CONSUMER_KEY");

    public static final String CONSUMER_SECRET =  System.getProperty("CONSUMER_SECRET");

    public static String PAYMENT_TYPE = System.getProperty("PaymentType");
    
    //Shop order status
  	public static final String COMPLETION_ORDER_STATUS = "Completed";
  	public static final String ONHOLD_ORDER_STATUS = "On hold";
  	public static final String CANCELLATION_ORDER_STATUS = "Cancelled";
  	public static final String PROCESSING_ORDER_STATUS = "Processing";
  	public static final String REFUND_ORDER_STATUS = "Refunded";
  	public static final String PENDING_ORDER_STATUS = "Pending payment";
  	public static final String FAILURE_ORDER_STATUS = "Failed";
    public static final String CAPTURE = "Capture";
    public static final String AUTHORIZE = "Authorize";
    public static final String DOB = "12.12.1970";
    public static final String DOB_BELOW_18 = "12.12.2020";
    public static final String REMAINING_CYCLES = "REMAINING_CYCLES";
    public static final String ALL_CYCLES = "ALL_CYCLES";

    //Products
    public static final String PRODUCT_1 = "Happy Ninja";
    public static final String PRODUCT_2 = "San Ninja";
    public static final String PRODUCT_3 = "Expert Ninja";
    public static final String PRODUCT_SUBS_1 = "TEST-4 SUBS";
    public static final String PRODUCT_SUBS_2 = "TEST-2 SUBS";
    public static final String PRODUCT_SUBS_3 = "";
    public static final String DOWNLOADABLE_PRODUCT = "Downloadable";
  	//Subscription order status
  	public static final String SUBSCRIPTION_STATUS_ACTIVE = "Active";
  	public static final String SUBSCRIPTION_STATUS_ONHOLD = "On hold";
  	public static final String SUBSCRIPTION_STATUS_CANCELLED = "Pending Cancellation";
  	public static final String SUBSCRIPTION_STATUS_PENDING = "Pending";
  	
  	//TID Status
//  	public static final String TID_STATUS_CONFIRMED = "CONFIRMED";
//  	public static final String TID_STATUS_ON_HOLD = "ON_HOLD";
//  	public static final String TID_STATUS_PENDING = "PENDING";
//  	public static final String TID_STATUS_FAILURE = "FAILURE";
//  	public static final String TID_STATUS_DEACTIVATED = "DEACTIVATED";
  	public static final String TID_STATUS_PROGRESS = "PROGRESS";

    private static String[] subsPayments = {"Novalnet Credit/Debit Cards","Novalnet Direct Debit SEPA",
            "Novalnet Direct Debit SEPA with payment guarantee","Novalnet PayPal","Novalnet Invoice","Novalnet Invoice with payment guarantee",
            "Novalnet Prepayment","Novalnet Google Pay","Novalnet Apple Pay"};
    public  static final List<String> SUBSCRIPTION_SUPPORTED_PAYMENTS = new ArrayList<>(Arrays.asList(subsPayments));

    public static final By CC_PAYMENT = By.cssSelector("tr[data-gateway_id$='_cc'] td.name a");
    public static final By PayPal_PAYMENT = By.cssSelector("tr[data-gateway_id$='_paypal'] td.name a");;

    public static final String AUTHORIZE_WITH_ZERO_AMOUNT = "zero_amount_booking";

    public static String mailOrderSubject ="order is now complete";

    public static final String CARDNUMBER = "CARDNUMBER";

    public static final String CVV = "CVV";

    public static final String EXP = "EXP";

    public static final String CARDHOLDERNAME = "CARDHOLDERNAME";

}
