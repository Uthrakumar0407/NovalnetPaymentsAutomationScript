package com.nn.pages.shopware.base;

import com.nn.callback.CallbackEventInterface;
import com.nn.pages.Magento.NovalnetAdminPortal;
import com.nn.pages.shopware.*;
import com.nn.v13.IframeV13;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class Shopware {
    private AdminLoginPage loginPage;
    private OrdersPage ordersPage;
    private SettingsPage settingsPage;
    private CustomerLoginPage customerLoginPage;
    private MyAccountPage myAccountPage;
    private CheckoutPage checkoutPage;
    private IframeV13 iframeV13;
    private CallbackEventInterface callback;
    private CallbackEventInterface callback_multibanco;
    private CallbackEventInterface callback_cashPayment;
    private CallbackEventInterface callback_invoice;
    private CallbackEventInterface callback_prepayment;
    private CallbackEventInterface callback_paypal;
    private CallbackEventInterface callback_postFinance;
    private CallbackEventInterface callback_trustly;
    private Map<String,String> txnInfo;
    private Map<String,String> testData;
    private Map<String,String> testData2;
    private NovalnetAdminPortal novalnetAdminPortal;
}
