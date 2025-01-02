package com.nn.language;

import com.nn.Magento.Constants;
import com.nn.apis.TID_Helper;
import com.nn.utilities.ShopwareUtils;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Map;

public class NovalnetCommentsDE {

    private NovalnetCommentsDE(){

    }

    public static String getOrderSuccessComment(String tid){
        return "Novalnet-Transaktions-ID: "+tid+"\n" +
                "Testbestellung";
    }

    public static String getOrderSuccessCommentWithBankDetail(String tid){
        Map<String,Object> bank = TID_Helper.getBankDetails(tid);
        var amount = ShopwareUtils.getFormattedAmount(TID_Helper.getTIDAmount(tid));
        var dueDate = ShopwareUtils.getFormattedDate(TID_Helper.getDueDate(tid));
        var orderNum = TID_Helper.getOrderNumber(tid);
        var currency = Currency.getInstance(TID_Helper.getTIDCurrency(tid)).getSymbol();
        return "Novalnet transaction ID: "+tid+"\n" +
                "Test order\n" +
                "Please transfer the amount of "+currency + amount +" to the following account on or before "+dueDate+"\n" +
                "\n" +
                "Account holder: "+bank.get("account_holder")+"\n" +
                "Bank: "+bank.get("bank_name")+"\n" +
                "Place: "+bank.get("bank_place")+"\n" +
                "IBAN: "+bank.get("iban")+"\n" +
                "BIC: "+bank.get("bic")+"\n" +
                "\n" +
                "Please use any of the following payment references when transferring the amount. This is necessary to match it with your corresponding order\n" +
                "Payment Reference 1: TID "+tid+"\n" +
                "Payment Reference 2: BNR-"+ Constants.PROJECT_ID +"-"+orderNum;
    }

    public static void main(String[] args) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        System.out.println(decimalFormat.format(100 / 100.0));
    }


}
