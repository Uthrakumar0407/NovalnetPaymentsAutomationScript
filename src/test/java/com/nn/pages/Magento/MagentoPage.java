package com.nn.pages.Magento;

import com.nn.apis.MagentoAPI_Helper;
import com.nn.callback.*;
import com.nn.pages.*;
import com.nn.v13.IframeV13;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class MagentoPage {
    private NovalnetAdminPortal novalnetAdminPortal;
    private ShopUserLoginPage shopUserLoginPage;
    private MagentoAPI_Helper magentoAPIHelper;
    private ShopBackEndLoginPage shopBackEndLoginPage;
    private CheckoutPage checkoutPage;
    private MyAccountPage myAccountPage;
    private LoginPage loginPage;
    private SuccessPage successPage;
    private OrderPage orderPage;
    private CallbackEventInterface callback;
    private ICallback iCallback;
    private CreditCardCallbackEvents callback_cc;
    private PayPalCallbackEvents callback_paypal;
    private InvoiceGuaranteeCallbackEvents callback_invoiceGuarantee;
    private SEPAGuaranteeCallbackEvents callback_sepaGuarantee;
    private PrepaymentCallbackEvents callback_prepayment;
    private CallbackEventInterface callback_multibanco;
    private CallbackEventInterface callback_cashPayment;
    private CallbackEventInterface callback_invoice;
    private CallbackEventInterface callback_postFinance;
    private CallbackEventInterface callback_trustly;
    private DIrectDebitACH callback_directDebitACH;
    private Blik callback_blik;
    private IframeV13 iframeV13;
    private Map<String,Object> txnInfo;
    private Map<String,String> testData;
    private Map<String,String> testData2;

}
