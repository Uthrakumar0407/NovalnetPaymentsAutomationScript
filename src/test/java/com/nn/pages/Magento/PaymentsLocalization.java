package com.nn.pages.Magento;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PaymentsLocalization {


    private static String filePath = "src/test/resources/payments_supported_country_currency.json";

    public static List<String> getCountries(String payment){
        try{
                String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
                JSONObject jsonObject = new JSONObject(jsonContent);
                JSONObject payment_type = jsonObject.getJSONObject(payment);
                JSONArray countries = payment_type.getJSONArray("country");
                return countries.toList()
                        .stream()
                        .map(Objects::toString)
                        .map(s->{
                            String[] s1 = s.split("-");
                            return s1[s1.length-1].strip();
                        })
                        .collect(Collectors.toList());
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
    }

    public static List<String> getCurrency(String payment){
        try{
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(jsonContent);
            JSONObject payment_type = jsonObject.getJSONObject(payment);
            JSONArray currencies = payment_type.getJSONArray("currency");
            return currencies.toList()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



}

