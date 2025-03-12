package com.nn.testcase;


import com.google.api.services.gmail.Gmail;
import com.nn.basetest.BaseTest;
import com.nn.drivers.DriverManager;

import com.nn.Magento.Constants;
import com.nn.apis.MagentoAPI_Helper;

import com.nn.pages.AdminPage;
import com.nn.pages.Magento.MagentoPage;
import com.nn.pages.Magento.NovalnetAdminPortal;

import static com.nn.constants.Constants.*;
import static com.nn.constants.Constants.AUTHORIZE;
import static com.nn.constants.Constants.CAPTURE;
import static com.nn.pages.Magento.NovalnetAdminPortalPaymentConfiguration.*;

import com.nn.pages.Magento.ShopBackEndLoginPage;
import com.nn.pages.SettingsPage;
import com.nn.utilities.DriverActions;
import com.nn.utilities.GmailEmailRetriever;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
///import org.openqa.selenium.devtools.v110.css.model.Value;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.events.WebDriverListener;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.nn.utilities.DriverActions.clickElement;
import static com.nn.callback.CallbackProperties.*;

import static com.nn.Magento.Constants.*;


public class demo extends BaseTest {




//    WooCommercePage wooCommerce = WooCommercePage.builder()
//            .adminPage(new AdminPage())
//            .dashBoardPage(new DashboardPage())
//            .ordersPage(new OrdersPage())
//            .settingsPage(new SettingsPage())
//            .homePage(new HomePage())
//            .productPage(new ProductPage())
//            .cartPage(new CartPage())
//            .checkoutPage(new CheckoutPage())
//            .successPage(new SuccessPage())
//            .myAccountPage(new MyAccountPage())
//            .subscriptionPage(new SubscriptionPage())
//            .callback(new CreditCardCallbackEvents())
//            .testData(ExcelHelpers.xlReadPaymentCredentials())
//            .txnInfo(new HashMap<>())
//            .build();
//static WebDriver driver=null;
//
//    public static WebDriver getDriver(ChromeOptions options) {
//        WebDriverManager.chromedriver().setup();
//        driver = new ChromeDriver(options);
//        return driver;
//    }
   // @Test
    public void test11(){

        DriverActions.openURL("https://paygate.novalnet.de/paygate.jsp?vendor=4&product=14&tariff=30&lang=en");

        WebElement ele = DriverActions.handleStale1(By.cssSelector("#gender"), (d)->d.findElement(By.cssSelector("#gender")));
        System.out.println(ele.toString());

        String s = DriverActions.handleStale1(By.cssSelector("#credit_card+strong"), (d)->d.findElement(By.cssSelector("#credit_card+strong")).getText());
        System.out.println(s);

        List<String> ls = DriverActions.handleStale1(By.cssSelector("#gender"), (d)->{
         Select sl = new Select(d.findElement(By.cssSelector("#gender")));
         return sl.getOptions();
        });
        System.out.println(ls);

        String s1 = DriverActions.handleStale1(By.cssSelector("#gender"), (d)->{
            Select sl = new Select(d.findElement(By.cssSelector("#gender")));
            return sl.getFirstSelectedOption().getText();
        });

        System.out.println(s1);

        DriverActions.handleStaleElement(By.cssSelector("#gender"),(d) -> {
            Select s2 = new Select(d.findElement(By.cssSelector("#gender")));
            s2.selectByVisibleText("Male");
        });



//        clickElement(By.xpath("//a[text()='Payment plugin configuration']"));
//        sleep(2);
////        clickElement(By.cssSelector("#payment_types_chosen"));
////        sleep(2);
//        DriverActions.getElements(By.cssSelector("#payment_types_chosen ul.chosen-results>li")).stream()
//                .map(e->e.getText().trim())
//                .forEach(System.out::println);
//        System.out.println(DriverActions.getElements(By.cssSelector("#payment_types_chosen ul.chosen-results>li")).size());
//        for(WebElement e: DriverActions.getElements(By.cssSelector("#payment_types_chosen ul.chosen-results>li"))){
//            System.out.println("Name: "+e.getText());
//        }
//        ShopBackEndLoginPage backEndLoginPage = new ShopBackEndLoginPage();
//        backEndLoginPage.load();
//        backEndLoginPage.login("shopadmin","TamiL*23");
//        OrderPage order = new OrderPage();
//        order.selectBackendOrder("31");
//        order.verifyOrderHistoryPageStatus("On Hold");
//        order.verifyInvoiceCreated(false);
//        order.captureOrder();
        //order.verifyOrderHistoryPageStatus("Complete");
//        order.verifyNovalnetComments("The transaction has been confirmed");
//        order.verifyNovalnetComments("Refund has been initiated for the TID","100");
//        order.getNewTID("Refund has been initiated for the TID","100");
        //order.verifyInvoiceCreated(true);
//        order.refundOrder(3200);
//        order.verifyOrderHistoryPageStatus("Processing");
//        order.verifyNovalnetComments("Refund has been initiated for the TID");
//        order.verifyCreditMemoCreated(true);
//        order.refundOrder(3200);
    }

//    @BeforeClass(alwaysRun = true)
//    @Parameters({"username", "password"})
//    public void adminLogin(@Optional("demouser") String userName, @Optional("wordpress") String password) {
////        wooCommerce.getAdminPage().openAdminPage();
////        wooCommerce.getAdminPage().adminLogin(userName, password);
//
//    }



  //  @Test
//    public void authorizeOrder() {
//
//        System.out.println(" Printing Key " + System.getProperty("NOVALNET_API_KEY"));
//
//
//
//        wooCommerce.getDashBoardPage().openSettingsPage();
//        wooCommerce.getSettingsPage().openNovalnetGlobalConfig();
//        wooCommerce.getSettingsPage().verifyGlobalConfig();
//    }
//
//    //@Test
//    public void testAddress(){
//
//        String [] prd= new String[]{"Happy Ninja"};
//        wooCommerce.getCartPage().load();
//        wooCommerce.getCartPage().clearCart();
//        wooCommerce.getHomePage().openProductPage();
//        wooCommerce.getProductPage().addProductToCartByName(prd);
//        wooCommerce.getHomePage().openCheckoutPage();
//        wooCommerce.getHomePage().openCheckoutPage().enterBillingInfoWithDefaultValues();
//        System.out.println("Iam done");
//
//    }



    @DataProvider(name = "data")
    public Object[][] data(){
        Object[][] d = countries.stream()
                .map(s->new Object[]{s})
                .toArray(Object[][]::new);
        return d;
    }

   // @Test(dataProvider = "data")
    public void dataProviderTest(String country){
        System.out.println(country);
    }

//    @Test()
    public void myTest() throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://admin.novalnet.de/");
        driver.findElement(By.cssSelector("#login_username")).sendKeys("KG06");
        driver.findElement(By.cssSelector("#login_password")).sendKeys("Kumaravel_06");
        driver.findElement(By.cssSelector("#captcha")).sendKeys("1234");
        driver.findElement(By.cssSelector("#login_button")).click();
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/products/own'] > i.fa.fa-sitemap")));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();",driver.findElement(By.cssSelector("a[href='/products/own']>span")));
        Thread.sleep(5);
        driver.findElement(By.xpath("//tr/td[.='9339']")).click();
        Thread.sleep(3);
        driver.findElement(By.xpath("//a[.='Payment plugin configuration']")).click();
        driver.switchTo().frame(driver.findElement(By.cssSelector("#shopConfigFrame")));
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#payment-types + div")));
        driver.findElement(By.cssSelector("#payment-types + div")).click();
        driver.findElement(By.cssSelector("#payment-types +div .chosen-search-input")).sendKeys("Credit card payment", Keys.ENTER);
        List<WebElement> countries = driver.findElements(By.cssSelector("#creditcard__allowed_currencies_chosen li>span"));
        System.out.println("CreditCard allowed countries count: "+countries.size());
        countries.forEach(e-> System.out.println("\""+e.getText().trim()+"\""+","));
        driver.quit();
    }

    Map<String,List<String>> map = new HashMap<>();

    public static final int COUNT = 10;

//    @Test
    public static void main() throws IOException, InterruptedException, GeneralSecurityException {
        //GmailEmailRetriever.getEmail();
        WebDriver driver = new ChromeDriver();
        driver.get("https://paygate.novalnet.de/paygate.jsp?vendor=4&product=14&tariff=30&lang=en");
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));

        Actions actions = new Actions(driver);

        WebElement gender = driver.findElement(By.id("gender"));
        Select selectGender = new Select(gender);
        selectGender.selectByVisibleText("Male");

        WebElement firstName = driver.findElement(By.id("first_name"));
        actions.sendKeys(firstName,"Nobert").perform();

        WebElement lastName  = driver.findElement(By.id("last_name"));
        actions.sendKeys(lastName,"Maier").perform();


        WebElement email = driver.findElement(By.id("email"));
        actions.sendKeys(email,"mohamedn_m@brandcrock.com").perform();

        WebElement company = driver.findElement(By.id("company"));
        actions.sendKeys(company,"A.B.C. Gerüstbau GmbH").perform();

        WebElement street = driver.findElement(By.id("street"));
        actions.sendKeys(street,"Hauptstr").perform();

        WebElement houseNo = driver.findElement(By.id("house_no"));
        actions.sendKeys(houseNo,"9").perform();

        WebElement zip =driver.findElement(By.id("zip"));
        actions.sendKeys(zip,"66862").perform();

        WebElement city = driver.findElement(By.id("city"));
        actions.sendKeys(city,"Kaiserslautern").perform();


        WebElement country = driver.findElement(By.id("country_code"));
        Select selectCountry = new Select(country);
        selectCountry.selectByVisibleText("Germany");

        WebElement mobileNo = driver.findElement(By.id("mobile"));
        actions.sendKeys(mobileNo,"01747781423").perform();


        WebElement selectCC = driver.findElement(By.id("googlepay"));
        actions.click(selectCC).perform();

        driver.findElement(By.cssSelector("div.gpay-card-info-animation-container")).click();

        Thread.sleep(3000);

        driver.switchTo().frame(driver.findElement(By.cssSelector("iframe.gpay-card-info-iframe")));

//        WebElement holderName = driver.findElement(By.id("card_holder"));
//        actions.sendKeys(holderName,"Norbert").perform();
//
//        WebElement cardNo = driver.findElement(By.id("cc_no"));
//        actions.sendKeys(cardNo,"4200 0000 0000 0000").perform();
//
//        WebElement month = driver.findElement(By.id("cc_exp_month"));
//        Select mymonth = new Select(month);
//        mymonth.selectByVisibleText("01");
//
//        WebElement year = driver.findElement(By.id("cc_exp_year"));
//        Select myyear = new Select(year);
//        myyear.selectByVisibleText("2025");
//
//        WebElement cvcNo = driver.findElement(By.id("cvc"));
//        actions.sendKeys(cvcNo,"0123").perform();
//
//        WebElement payButton = driver.findElement(By.xpath("//input[@id='cvc']/following::div[3]"));
//        actions.click(payButton).perform();
//
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn_confirm_button")));
//
//        WebElement confirmBtn  =driver.findElement(By.id("btn_confirm_button"));
//        actions.click(confirmBtn).perform();

        //driver.quit();

//        int c = COUNT;
//        while(c > 0){
//            System.out.println("Counting: "+c--);
//        }


//        String filePath = "/var/www/nageshwaran_k/workspace/GitNovalet/AutomationPaymentTests/src/test/resources/payments_supported_country_currency.json";
//        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
//        try{
//            JSONObject jsonObject = new JSONObject(jsonContent);
//            JSONObject payment = jsonObject.getJSONObject("CREDITCARD");
//            JSONArray currency = payment.getJSONArray("currency");
//            JSONArray countries = payment.getJSONArray("country");
//            System.out.println(currency);
//            System.out.println(countries);
//            List<String> countryISO = countries.toList()
//                    .stream()
//                    .map(Objects::toString)
//                    .map(s->s.split("-")[1].strip())
//                    .collect(Collectors.toList());
//            System.out.println("Countries length after trim: "+countryISO.size());
//            System.out.println(countryISO);
//        }catch (Exception e){
//            System.out.println("Error occred while parsing json "+e.getMessage());
//        }

//        Map<String,Integer> map = new HashMap<>();
//        map.put("dhoni",7);
//        map.put("virat",18);
//        map.put("rohit",45);
//        map.put("rahul",1);
//
//        Collection<Integer> c = map.values();
//        System.out.println(c);
//        Set<String> strings = map.keySet();
//        for(String k : map.keySet()){
//            System.out.println(map.get(k));
//        }
//        Set<Map.Entry<String,Integer>> entrySet = map.entrySet();
//        for(Map.Entry<String,Integer> entry : map.entrySet()){
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue());
//        }
//        Map<String,Integer> map1 = new Hashtable<>();
//        Map<String,Integer> map2 = new LinkedHashMap<>();
//        map2.put("sachin",10);
//        map2.put("dhoni",7);
//        map2.put("virat",18);
//        map2.put("rohit",45);
//        map2.put("rahul",1);
//        System.out.println(map);
//        System.out.println(map2);
//
//        Map<Integer,String> map3 = new TreeMap<>();
//        map3.put(10,"sachin");
//        map3.put(7,"dhoni");
//        map3.put(18,"virat");
//        map3.put(45,"rohit");
//        map3.put(1,"rahul");
//        System.out.println(map3);

//        List<String> list = new ArrayList<>();
//        list.add("A");
//        list.add("B");
//        list.add("C");
//
//
//
//        List<String> list3 = new ArrayList<>();
//        list3.add("D");
//        list3.add("E");
//        list3.add("F");
//
//
//        Collections.shuffle(list);
//
//        System.out.println(list);



//        Object[] objArr = {"Nagesh","Dhoni","Virat"};
//
//        String[] strArr = (String[]) objArr;
//
       // Object o = 100;
 //       String s = (String) o;
//
//        System.out.println(s);

        //String[] strArr = new String[objArr.length];
//        int i =0;
//        for(Object o : objArr){
//            strArr[i++] = (String) o;
//        }

        //System.out.println(Arrays.toString(strArr));

//        List< List<String> > list1 = new ArrayList<>();
//        list1.add(list3);
//        list1.add(list);
//
//        System.out.println(list1);
////        list1.addAll();
//        List< List<String> > list2 = new ArrayList<>();
//        list2.addAll(list1);
//        list2.add(List.of("sdf","sdf"));
//
//        System.out.println(list2);
//        list2.set(2,List.of("re"));
//        System.out.println(list2);
//        System.out.println(list.containsAll(list3));

//        list1.addAll(list2);


    }



    List<String> countries = List.of(
            "Germany",
            "United Kingdom",
            "Austria",
            "Switzerland",
            "U.S. Virgin Islands",
            "Equatorial Guinea",
            "Ethiopia",
            "Egypt",
            "Algeria",
            "Azerbaijan",
            "Åland Islands",
            "Aruba",
            "Australia",
            "American Samoa",
            "Argentina",
            "Angola",
            "Armenia",
            "Albania",
            "Anguilla",
            "Antigua and Barbuda",
            "Afghanistan",
            "Andorra",
            "Ascension Island",
            "Bahamas",
            "Bahrain",
            "Bangladesh",
            "Barbados",
            "Belarus",
            "Belgium",
            "Belize",
            "Benin",
            "Bermuda",
            "Bhutan",
            "Bolivia",
            "Bosnia and Herzegovina",
            "Botswana",
            "Bouvet Island",
            "Brazil",
            "British Virgin Islands",
            "Brunei",
            "Bulgaria",
            "Burkina Faso",
            "Burundi",
            "Ceuta, Melilla",
            "Chile",
            "China",
            "Clipperton-Ile",
            "Cook Islands",
            "Costa Rica",
            "Côte d’Ivoire",
            "Denmark",
            "Diego Garcia",
            "Dominica",
            "Dominican Republic",
            "Djibouti",
            "Ecuador",
            "El Salvador",
            "Eritrea",
            "Estonia",
            "Falkland Islands",
            "Faroe Islands",
            "Fiji",
            "Finland",
            "France",
            "French Guiana",
            "French Polynesia",
            "French Southern Territories",
            "Gabon",
            "Gambia",
            "Georgia",
            "Ghana",
            "Gibraltar",
            "Grenada",
            "Greece",
            "Greenland",
            "Guadeloupe",
            "Guam",
            "Guatemala",
            "Guernsey",
            "Guinea",
            "Guinea-Bissau",
            "Guyana",
            "Haiti",
            "Heard Island and McDonald Islands",
            "Honduras",
            "Hong Kong SAR China",
            "India",
            "Indonesia",
            "Isle of Man",
            "Iraq",
            "Iran",
            "Ireland",
            "Iceland",
            "Israel",
            "Italy",
            "Jamaica",
            "Japan",
            "Yemen",
            "Jersey",
            "Jordan",
            "Yugoslavia",
            "Cayman Islands",
            "Cambodia",
            "Cameroon",
            "Canada",
            "Canary Islands",
            "Cape Verde",
            "Kazakhstan",
            "Qatar",
            "Kenya",
            "Kyrgyzstan",
            "Kiribati",
            "Cocos [Keeling] Islands",
            "Colombia",
            "Comoros",
            "Congo - Kinshasa",
            "Congo - Brazzaville",
            "North Korea",
            "South Korea",
            "Croatia",
            "Cuba",
            "Kuwait",
            "Laos",
            "Lesotho",
            "Latvia",
            "Lebanon",
            "Liberia",
            "Libya",
            "Liechtenstein",
            "Lithuania",
            "Luxembourg",
            "Macau SAR China",
            "Madagascar",
            "Malawi",
            "Malaysia",
            "Maldives",
            "Mali",
            "Malta",
            "Morocco",
            "Marshall Islands",
            "Martinique",
            "Mauritania",
            "Mauritius",
            "Mayotte",
            "Macedonia",
            "Mexico",
            "Micronesia",
            "Moldova",
            "Monaco",
            "Mongolia",
            "Montenegro",
            "Montserrat",
            "Mozambique",
            "Myanmar [Burma]",
            "Namibia",
            "Nauru",
            "Nepal",
            "New Caledonia",
            "New Zealand",
            "Nicaragua",
            "Netherlands",
            "Netherlands Antilles",
            "Niger",
            "Nigeria",
            "Niue",
            "Northern Mariana Islands",
            "Norfolk Island",
            "Norway",
            "Oman",
            "Timor-Leste",
            "Pakistan",
            "Palestinian Territories",
            "Palau",
            "Panama",
            "Papua New Guinea",
            "Paraguay",
            "Peru",
            "Philippines",
            "Pitcairn Islands",
            "Poland",
            "Portugal",
            "Puerto Rico",
            "Réunion",
            "Rwanda",
            "Romania",
            "Russia",
            "Solomon Islands",
            "Zambia",
            "Samoa",
            "San Marino",
            "São Tomé and Príncipe",
            "Saudi Arabia",
            "Sweden",
            "Senegal",
            "Serbia",
            "Seychelles",
            "Sierra Leone",
            "Zimbabwe",
            "Singapore",
            "Slovakia",
            "Slovenia",
            "Somalia",
            "Spain",
            "Sri Lanka",
            "Saint Helena",
            "Saint Kitts and Nevis",
            "Saint Lucia",
            "Saint Pierre and Miquelon",
            "Saint Vincent and the Grenadines",
            "South Africa",
            "Sudan",
            "South Georgia and the South Sandwich Islands",
            "Suriname",
            "Svalbard and Jan Mayen",
            "Swaziland",
            "Syria",
            "Tajikistan",
            "Taiwan",
            "Tanzania",
            "Thailand",
            "Togo",
            "Tokelau",
            "Tonga",
            "Trinidad and Tobago",
            "Tristan da Cunha",
            "Chad",
            "Czech Republic",
            "Tunisia",
            "Turkey",
            "Turkmenistan",
            "Turks and Caicos Islands",
            "Tuvalu",
            "Uganda",
            "Ukraine",
            "Hungary",
            "U.S. Minor Outlying Islands",
            "Uruguay",
            "Uzbekistan",
            "Vanuatu",
            "Vatican City",
            "Venezuela",
            "United Arab Emirates",
            "United States",
            "Vietnam",
            "Wallis and Futuna",
            "Christmas Island",
            "Western Sahara",
            "Central African Republic",
            "Cyprus",
            "Kosovo",
            "France, Metropolitan",
            "North Ireland",
            "South Sudan",
            "Curaçao",
            "Sint Maarten"
    );

//@Test
    public void testUpdatePrice() throws Exception{

        MagentoAPI_Helper.createCustomer(CREDITCARD);
        MagentoAPI_Helper.getProductStock(PRODUCT_CREDIT_CARD_PAY);
    MagentoAPI_Helper.updateProductPrice(PRODUCT_CREDIT_CARD_PAY, Double.parseDouble("56.00"));
}

  //  @Test(invocationCount = 20)
    public void unitTest() throws Exception {
        ShopBackEndLoginPage bLogin = new ShopBackEndLoginPage();
        bLogin.SigninToShop(SHOP_BACKEND_USERNAME,SHOP_BACKEND_PASSWORD);
        bLogin.loadShopGlobalConfig();

    }

@BeforeTest
public void adminLogin(){
    AdminPage admin = new AdminPage();
    admin.openAdminPage();

    admin.adminLogin("admin", "wordpress");

}
    @Test(invocationCount = 3)
    public void testWooCommerceSettingsPopup() throws Exception {


        SettingsPage settingsPage= new SettingsPage();
        settingsPage.paymentPageLoad();
        settingsPage.activatePayment(CREDITCARD,true);
        settingsPage.setPaymentConfiguration(false,CAPTURE,"",true,false,true,false,null,settingsPage.getPayment(CREDITCARD));

        settingsPage.paymentPageLoad();

        settingsPage.setPaymentConfiguration(true,AUTHORIZE,"",true,false,true,false,null,settingsPage.getPayment(CREDITCARD));

        settingsPage.paymentPageLoad();
    }
}
