package com.nn.pages;

import java.util.Map;

import com.nn.callback.*;


import com.nn.callback.CreditCardCallbackEvents;
import com.nn.callback.PayPalCallbackEvents;
import com.nn.callback.PrepaymentCallbackEvents;
import com.nn.testcase.InvoiceGuaranteePaymentTests;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WooCommercePage {

	
	private AdminPage adminPage;
	private CartPage cartPage;
	private CheckoutPage checkoutPage;
	private DashboardPage dashBoardPage;
	private HomePage homePage;
	private LoginPage loginPage;
	private MyAccountPage myAccountPage;
	private OrdersPage ordersPage;
	private ProductPage productPage;
	private SettingsPage settingsPage;
	private SubscriptionPage subscriptionPage;
	private SuccessPage successPage;
	private CallbackEventInterface callback;
	private CreditCardCallbackEvents callback_cc;
	private PayPalCallbackEvents callback_paypal;
	private InvoiceGuaranteeCallbackEvents callback_invoiceGuarantee;
	private SEPAGuaranteeCallbackEvents callback_sepaGuarantee;
	private PrepaymentCallbackEvents callback_prepayment;
	private MultibancoCallbackEvents callback_multibanco;
	private CashPaymentCallbackEvents callback_cashPayment;
	private InvoiceCallbackEvents callback_invoice;
	private PostFinanceCardPaymentCallbackEvents callback_postFinance;
	private TrustlyCallbackEvents callback_trustly;
	private Map<String,Object> txnInfo;
	private Map<String,String> testData;
	private Map<String,String> testData2;

	
}
