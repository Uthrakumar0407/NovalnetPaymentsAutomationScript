package com.nn.testcase;

import com.nn.callback.CallbackProperties;

public class TestCB {

    public static void main (String a[]){

        String cbRequest="{\"result\":{\"status_code\":100,\"status_text\":\"successful\",\"status\":\"SUCCESS\"},\"merchant\":{\"vendor\":4,\"project\":9339},\"event\":{\"parent_tid\":\"14838900085101866\",\"checksum\":\"36558199735a48107b195086a43fd4084e71272e01e04751ec3e38d10b734583\",\"type\":\"TRANSACTION_REFUND\",\"tid\":\"14000511747766550\"},\"transaction\":{\"date\":\"2023-10-05 15:57:06\",\"order_no\":\"3000000561\",\"amount\":2400,\"payment_type\":\"PRZELEWY24\",\"txn_secret\":\"a7a871af0176f875faaa6152cdb1e1be\",\"status_code\":100,\"test_mode\":1,\"currency\":\"PLN\",\"tid\":14838900085101866,\"status\":\"CONFIRMED\",\"refund\":{\"amount\":\"2400\",\"payment_type\":\"PRZELEWY24_REFUND\",\"currency\":\"PLN\",\"tid\":\"14000511747766550\"},\"refunded_amount\":\"2400\"},\"customer\":{\"customer_no\":\"3839\",\"gender\":\"u\",\"customer_ip\":\"125.21.64.250\",\"last_name\":\"Mustermann\",\"tel\":\"+49 (0)89 123456\",\"first_name\":\"Max\",\"email\":\"test_05102023192343_99ac95@novalnet.com\",\"billing\":{\"zip\":\"12345\",\"country_code\":\"PL\",\"city\":\"Musterhausen\",\"street\":\"Musterstr\",\"house_no\":\"2\"}}}";

        CallbackProperties cbProp = new CallbackProperties();

        System.out.println(cbProp.sendRequest("http://192.168.2.110/automation/Magento_246p2/pub/rest/V1/novalnet/callback/",cbRequest,""));
    }
}
