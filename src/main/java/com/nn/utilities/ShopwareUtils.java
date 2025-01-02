package com.nn.utilities;

import io.restassured.path.json.JsonPath;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.nn.callback.CallbackProperties.*;

public class ShopwareUtils {

    public static final String BROWSER = System.getProperty("BROWSER");

    public static final String NOVALNET_API_KEY = System.getProperty("NOVALNET_API_KEY").replaceAll("\\\\", "")
            .replaceAll("^\\|+|\\|+$", "").replaceAll("(?<=\\|)\\|\\|(?=\\|)", "| |");
    public static final String NOVALNET_ACCESSKEY = System.getProperty("NOVALNET_ACCESSKEY");
    public static final String NOVALNET_TARIFF = System.getProperty("NOVALNET_TARIFF");
    public static final String NOVALNET_SUBSCRIPTION_TARIFF = System.getProperty("NOVALNET_SUBSCRIPTION_TARIFF");

    public static final String SHOP_BACKEND_USERNAME = System.getProperty("SHOP_BACKEND_USERNAME");
    public static final String SHOP_BACKEND_PASSWORD = System.getProperty("SHOP_BACKEND_USER_CRED");

    public static final String CALLBACK_URL = System.getProperty("CALLBACK_URL");

    public static final String SHOP_FRONT_END_URL = System.getProperty("SHOP_FRONT_END_URL");

    public static final String SHOP_BACK_END_URL = System.getProperty("SHOP_BACK_END_URL");

    public static final String PROJECT_ID = System.getProperty("PROJECT_ID");



    public static String getFormattedAmount(String amount) {
        // Check if the amount already contains a decimal point
        if (amount.contains(".")) {
            return amount; // Return the input string as it is
        }
        int len = amount.length();
        if (len == 0) return amount;
        if (len == 1) return "0.0" + amount; // 0.01
        if (len == 2) return "0." + amount; // 0.15
        if (len < 6) { // 19.99
            return amount.substring(0, len - 2) + "." + amount.substring(len - 2);
        }
        StringBuilder res = new StringBuilder();
        int i = 0;
        while (i < len - 2) {
            int l = (len - 2) - i;
            int rem = l % 3;
            int e = rem == 0 ? i + 3 : rem;
            res.append(amount, i, e);
            if (i + 3 != len - 2)
                res.append(",");
            i = i + (rem == 0 ? 3 : rem);
        }
        res.append(".").append(amount.substring(len - 2));
        return res.toString(); // 1,600.00
    }

    public static String getFormattedDate(String date) {
        Date d = DriverActions.getDateFromString("yyyy-MM-dd", date);
        return DriverActions.changePatternOfDate("dd/MM/yyyy", d);
    }

    public static String getToday() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now());
    }

    public static String getCallbackResponse(String response) {
        JsonPath jsonPath = new JsonPath(response);
        return DriverActions.replaceUnicodeCharsWithValues(jsonPath.get("message"))
                .replaceAll("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$", "").replace("\u00A0", " ").trim();
    }

    public static String getUpcomingDay(int count) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now().plusDays(count));
    }

    public static String getUpcomingMonths(int count) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now().plusMonths(count));
    }


    public static String getPaymentName(String paymentType) {
        switch (paymentType) {
            case PAYPAL:
                return "PayPal";
            case CREDITCARD:
                return "Credit/Debit Cards";
            case IDEAL:
                return "iDEAL";
            case MULTIBANCO:
                return "Multibanco";
            case PREPAYMENT:
                return "Prepayment";
            case CASHPAYMENT:
                return "Barzahlen/viacash";
            case ONLINE_TRANSFER:
                return "Sofort";
            case ONLINE_BANK_TRANSFER:
                return "Online bank transfer";
            case INVOICE:
            case GUARANTEED_INVOICE:
                return "Invoice";
            case INSTALMENT_INVOICE:
                return "Instalment by invoice";
            case BANCONTACT:
                return "Bancontact";
            case EPS:
                return "eps";
            case GIROPAY:
                return "Giropay";
            case PRZELEWY24:
                return "Przelewy24";
            case POSTFINANCE_CARD:
                return "PostFinance Card";
            case TRUSTLY:
                return "Trustly";
            case ALIPAY:
                return "Alipay";
            case WECHATPAY:
                return "WeChat Pay";
            case DIRECT_DEBIT_SEPA:
            case GUARANTEED_DIRECT_DEBIT_SEPA:
                return "Direct Debit SEPA";
            case INSTALMENT_DIRECT_DEBIT_SEPA:
                return "Instalment by SEPA direct debit";
            case GOOGLEPAY:
                return "Google Pay";
            case BLIK:
                return "Blik";
            case MBWAY:
                return "MB Way";
            case PAYCONIQ:
                return "Payconiq";
            case DIRECT_DEBIT_ACH:
                return "Direct Debit ACH";
            default:
                throw new IllegalArgumentException("Invalid payment method: " + paymentType);
        }
    }

    public static final String SW_PRODUCT_01 = "SWDEMO10006"; //50 EUR
    public static final String SW_PRODUCT_02 = "SWDEMO100013"; //3 EUR
    public static final String SW_DIGITAL_PRODUCT1 = "SW10000"; //3 EUR
    public static final String SW_DIGITAL_PRODUCT2 = "SW10001"; //50 EUR
    public static final String SW_PRODUCT_GUEST_01 = "Main-product-free-shipping-with-highlighting/SWDEMO10006";
    public static final String SW_PRODUCT_GUEST_02 = "Main-product-with-reviews/SWDEMO100013";
    public static final String TRANSACTION_COMMENT_IN_SUCCESS_PAGE = "Verify novalnet transaction comments in success page";
    public static final String PAYMENT_NAME_IN_SUCCESS_PAGE = "Verify payment name in success page";
    public static final String ORDER_STATUS_IN_MY_ACCOUNT_PAGE = "Verify shopware order status in my account page";
    public static final String PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_LIST_PAGE = "Verify payment name in my account order list page";
    public static final String PAYMENT_NAME_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE = "Verify payment name in my account order detail page";
    public static final String TRANSACTION_COMMENT_IN_MY_ACCOUNT_PAGE = "Verify payment comments in my account page";
    public static final String ORDER_STATUS_IN_ADMIN_ORDER_LISTING_PAGE = "Verify order status in admin order listing page";
    public static final String ORDER_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE = "Verify order status in admin orders detail page";
    public static final String TRANSACTION_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE = "Verify payment comments in admin orders detail page";
    public static final String REFUND_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE = "Verify refund comments in admin order detail page";
    public static final String REFUND_BUTTON_DISPLAYED = "Verify refund button displayed";
    public static final String BOOK_AMOUNT_BUTTON_DISPLAYED = "Verify refund button displayed";
    public static final String REFUND_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE = "Verify refund comments in my account order detail page";
    public static final String CHARGEBACK_COMMENT_IN_ADMIN_ORDER_DETAIL_PAGE = "Verify chargeback comments in admin order detail page";
    public static final String TRANSACTION_COMMENT_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE = "Verify refund comments in my account order detail page";
    public static final String INSTALMENT_TABLE_TID_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE = "Verify Instalment recurring tid updated in my account orders page";
    public static final String INSTALMENT_TABLE_TID_IN_ADMIN_ORDER_DETAIL_PAGE = "Verify Instalment recurring tid updated in admin orders page";
    public static final String INSTALMENT_TABLE_STATUS_IN_ADMIN_ORDER_DETAIL_PAGE = "Verify Instalment cycle status updated in admin orders page";
    public static final String INSTALMENT_TABLE_STATUS_IN_MY_ACCOUNT_ORDER_DETAIL_PAGE = "Verify Instalment cycle status updated in my account orders page";
    public static final String INSTALMENT_TABLE_REFUND_BUTTON_ENABLED_IN_ORDER_DETAIL_PAGE = "Verify Instalment cycle refund button enabled in admin orders page";
    public static final String CAPTURE_CANCEL_BUTTON_DISPLAYED = "Verify capture and cancel button displayed in admin orders page";
    public static final String MAIL_SUBJECT = "Your order with ";
    public static final String DOWNLOAD_BTN = "Verify download button displayed on the checkout page";
    public static final String DOWNLOAD_LINK = "Verify download link displayed on the checkout page";


}