package com.nn.Magento;

import org.openqa.selenium.By;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Constants {

    public static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("HEADLESS"));

    public static final String BROWSER = System.getProperty("BROWSER");

    //public static final String SHOP_BACKEND_PASSWORD = "novalnet123";

    public static final String SHOP_BACKEND_PASSWORD=System.getProperty("SHOP_BACKEND_USER_CRED");

    public static final String SHOP_FRONTEND_PASSWORD = "Novalnet@123";

    public static final String ADMIN_URL = "https://admin.novalnet.de/?lang=en#login/";

    public static final long EXPLICIT_TIMEOUT = 30; //Long.parseLong(System.getProperty("EXPLICIT_TIMEOUT"));
    public static final long IMPLICIT_TIMEOUT = 10;//Long.parseLong(System.getProperty("IMPLICIT_TIMEOUT"));
    public static final long PAGE_LOAD_TIMEOUT = 60;//Long.parseLong(System.getProperty("PAGE_LOAD_TIMEOUT"));

    public static final String YES = "yes";
    public static final String NO = "no";
    

    public static final String NOVALNET_API_KEY =System.getProperty("NOVALNET_API_KEY").replaceAll("\\\\", "")
            .replaceAll("^\\|+|\\|+$", "").replaceAll("(?<=\\|)\\|\\|(?=\\|)", "| |");
    public static final String NOVALNET_ACCESSKEY =  System.getProperty("NOVALNET_ACCESSKEY");
    public static final String NOVALNET_TARIFF = System.getProperty("NOVALNET_TARIFF");
    public static final String NOVALNET_SUBSCRIPTION_TARIFF = System.getProperty("NOVALNET_SUBSCRIPTION_TARIFF");

//    public static final String BEARER_TOKEN = "p0q3xbymjspitc7lzkl6g7vczck35hwz";

    public static final String BEARER_TOKEN = System.getProperty("BEARER_TOKEN");
    //public static final String SHOP_BACKEND_USERNAME = "shopadmin";
    public static final String SHOP_BACKEND_USERNAME = System.getProperty("SHOP_BACKEND_USERNAME");


//    public static final String SHOP_BASE_URL = "http://192.168.2.180/nageshwaran_k/Shops/migration/mag1smore/pub/rest/V1/";

    public static final String SHOP_BASE_URL = System.getProperty("SHOP_BASE_URL");

    public static final String CALLBACK_URL = System.getProperty("CALLBACK_URL");

    public static final String SHOP_FRONT_END_URL = System.getProperty("SHOP_FRONT_END_URL");

    public static final String SHOP_BACK_END_URL = System.getProperty("SHOP_BACK_END_URL");


    public static final String PROJECT_ID = System.getProperty("PROJECT_ID");

    public static boolean ONE_CLICK = false; /*System.getProperty("ONE_CLICK");*/

    public static String MIN_TXN_AMOUNT_AUTH = System.getProperty("MIN_TXN_AMOUNT_AUTH");

    public static String ORDER_AMOUNT = System.getProperty("ORDER_AMOUNT");

    public static String mailOrderSubjectText="Your Main Website Store order confirmation";

    public static String mailInvoiceSubjectText="Invoice for your Main Website Store order";




    
    
    //Shop order status
  	public static final String COMPLETION_ORDER_STATUS = "Complete";
  	public static final String ONHOLD_ORDER_STATUS = "On Hold";
    public static final String ONHOLD_ORDER_STATUS_WC = "On hold";
  	public static final String CANCELLATION_ORDER_STATUS = "Canceled";
  	public static final String PROCESSING_ORDER_STATUS = "Processing";
  	public static final String REFUND_ORDER_STATUS = "Closed";
  	public static final String PENDING_ORDER_STATUS = "Pending";
  	public static final String FAILURE_ORDER_STATUS = "Failed";
    public static final String CAPTURE = "Capture";
    public static final String AUTHORIZE = "Authorize";
    public static final String AUTHORIZE_ZERO_AMOUNT = "Authorize with zero amount";
    public static final String DOB = "12.12.1970";
    public static final String DOB_LESS_18 = "12.12.2020";
    public static final String REMAINING_CYCLES = "REMAINING_CYCLES";
    public static final String ALL_CYCLES = "ALL_CYCLES";
    public static final int SHIPPING_RATE=0;

    //Products

    public static final String PRODUCT_EPS_PAY = "24-MB01";

    public static final String PRODUCT_INSTALLMENT_INVOICE = "24-MB02";
    public static final String PRODUCT_SOFORT = "24-MB03";

    public static final String PRODUCT_GIRO_PAY = "24-MB04";

    public static final String PRODUCT_INVOICE_G = "24-MB05";

    public static final String PRODUCT_SEPA_G = "24-MB06";

    public static final String PRODUCT_AlI_PAY = "24-WB01";

    public static final String PRODUCT_BAN_CONTACT_PAY = "24-WB02";

    public static final String PRODUCT_CASH_PAY = "24-WB03";

    public static final String PRODUCT_CREDIT_CARD_PAY = "24-WB04";

    public static final String PRODUCT_IDEAL_PAY = "24-WB05";

    public static final String PRODUCT_INVOICE = "24-WB05";

    public static final String PRODUCT_MULTI_BANCO_PAY = "24-WB06";

    public static final String PRODUCT_PAYPAL_PAY = "24-WB07";


    public static final String PRODUCT_ONLINE_BANK_TRANSFER_PAY = "24-UG01";

    public static final String PRODUCT_PRE_PAYMENT_PAY = "24-UG02";

    public static final String PRODUCT_TRUSTLY_PAY = "24-UG03";

    public static final String PRODUCT_WE_CHAT_PAY = "24-UG04";

    public static final String PRODUCT_PRZELEWY24_PAY = "24-UG05";

    public static final String PRODUCT_SEPA = "24-UG06";
    public static final String PRODUCT_POST_FINANCE_PAY = "24-UG07";

    public static final String PRODUCT_INSTALLMENT_SEPA = "24-UB02";

    public static final String PRODUCT_CC_MIN_AUTH_AMOUNT = "24-WG081-gray";

    public static final String PRODUCT_SEPA_MIN_AUTH_AMOUNT = "24-WG082-gray";

    public static final String PRODUCT_INVOICE_MIN_AUTH_AMOUNT = "24-WG083-gray";

    public static final String PRODUCT_GUARANTEE_SEPA_MIN_AUTH_AMOUNT = "24-WG081-pink";

    public static final String PRODUCT_GUARANTEE_INVOICE_MIN_AUTH_AMOUNT = "24-WG082-pink";

    public static final String PRODUCT_INSTALMENT_SEPA_MIN_AUTH_AMOUNT = "24-WG081-blue";
    public static final String PRODUCT_INSTALMENT_INVOICE_MIN_AUTH_AMOUNT = "24-WG082-blue";

    public static final String PRODUCT_ADMIN = "24-MG01";
    public static final String PRODUCT_GUEST = "fusion-backpack.html";
    public static final String PRODUCT_GUEST_2 = "affirm-water-bottle.html";
    public static final String PRODUCT_GUEST_3 = "savvy-shoulder-tote.html";
    public static final String PRODUCT_GUEST_CODE = "24-MB02";
    public static final String PRODUCT_GUEST_CODE_2 = "24-UG06";
    public static final String PRODUCT_DOWNLOAD = "5acfa.html";

    public static final String PRODUCT_PAYCONIQ = "24-MG03";

    public static final String PRODUCT_BLIK = "24-MG02";

    public static final String PRODUCT_MBWAY = "24-MG04";

    public static final String PRODUCT_SUBS_1 = "TEST-4 SUBS";
    public static final String PRODUCT_SUBS_2 = "TEST-2 SUBS";
    public static final String PRODUCT_SUBS_3 = "";
  	
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
}
