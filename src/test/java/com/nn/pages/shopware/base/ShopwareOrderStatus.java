package com.nn.pages.shopware.base;

public enum ShopwareOrderStatus {
    PAID("Paid"),
    PAID_PARTIALLY("Paid (partially)"),
    CANCELLED("Cancelled"),
    CANCELED("Canceled"),
    REFUNDED("Refunded"),
    IN_PROGRESS("In Progress"),
    AUTHORIZED("Authorized"),
    OPEN("Open"),
    FAILED("Failed"),
    CHARGEBACK("Chargeback"),
    PENDING("Pending");

    private final String stringValue;

    private ShopwareOrderStatus(String stringValue) {
        this.stringValue = stringValue;
    }

    public String get(){
        return stringValue;
    }

    public static void main(String arg[]){
        System.out.println(ShopwareOrderStatus.FAILED.get());
    }
}
