package com.nn.utilities;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.nn.drivers.DriverManager;
import com.nn.reports.AllureManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.nn.Magento.Constants.PRODUCT_CREDIT_CARD_PAY;
import static com.nn.apis.MagentoAPI_Helper.createGooglePayCustomer;
import static com.nn.apis.MagentoAPI_Helper.updateProductStock;
import static com.nn.constants.Constants.EXPLICIT_TIMEOUT;
import static com.nn.constants.Constants.HEADLESS;
import static com.nn.utilities.DriverActions.*;
import com.nn.reports.ExtentTestManager;


public class GooglePayHelper {

 private static final String CLIENT_ID = "552816382863-096005plaikj9f3qdtapcajjj48r64ff.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String APPLICATION_NAME = "novalnet solutions";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static String signIn="//a[@data-action='sign in']";
    private static String email="//input[@type='email' and @name='identifier']";
    private static String  pwd="Passwd";
    private static String nextBtn="#identifierNext button";

    //by Nizam
    public static final String EMAILID = "novalnetesolutions2010@gmail.com";
    public static final String PASS = "Novalnet@123";

    public static void launchGmail() throws GeneralSecurityException, IOException, InterruptedException {

        try {

//            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader("credentials.json"));
//
//            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                    httpTransport, JSON_FACTORY, clientSecrets, Collections.singletonList("https://www.googleapis.com/auth/gmail.readonly"))
//                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
//                    .setAccessType("offline")
//                    .build();
//
//            // Use a different LocalServerReceiver with port 8080
//            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8081).build();
//
//
//            // Authorize using AuthorizationCodeInstalledApp with the custom receiver
//            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");


            Map<String, String> credentials = new HashMap<>();
            credentials.put("key", CLIENT_ID);
            credentials.put("secret", CLIENT_SECRET);

           ChromeOptions options = new ChromeOptions();
            options.addArguments("credentials=" + credentials);
            options.addArguments("--auth-server-whitelist=*");
           options.addArguments("--disable-web-security");
            options.addArguments("--user-data-dir=chrome-profile");
            options.addArguments("--disable-infobars");

            options.addArguments("--no-sandbox");
            options.addArguments("--disable-blink-features=AutomationControlled");

            WebDriver driver = new ChromeDriver(options);

            // Navigate to the Gmail website using the webdriver.
            //  driver.navigate().to("https://mail.google.com?auth=0Adeu5BXm6qRjxPwHSkc-RP40hzFIRZ-RvJlXqrYevBXIpzRFShqDoMbjbfKfhApghbdUOA");
            driver.navigate().to("https://mail.google.com");

            // start if profile is not set
            try {
                driver.findElement(By.xpath(signIn));
                driver.findElement(By.xpath(signIn)).click();
            }catch(Exception e){}

            try {
                driver.findElement(By.xpath(email));
                driver.findElement(By.xpath(email)).sendKeys("novalnetesolutions2010@gmail.com");
                driver.findElement(By.xpath(nextBtn)).click();
                Thread.sleep(5000);
            }catch(Exception e){} // need to add log

            try {
                driver.findElement(By.name(pwd));
                driver.findElement(By.name(pwd)).sendKeys("Novalnet@payment@No1Gateway");
                driver.findElement(By.xpath(nextBtn)).click();
                Thread.sleep(5000);
               }catch(Exception e){} // need to add log
            // end if profile is not set
          performWebBetaAutomation(driver); //commented as this test code to google pay using paygate
           // driver.quit();
        } catch (Exception e) {
            // need to add logic later while automating google pay
        }

    }

    public static void launchGmailinFF() throws GeneralSecurityException, IOException, InterruptedException {

      /*  HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader("credentials.json"));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, Collections.singletonList("https://www.googleapis.com/auth/gmail.readonly"))
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // Use a different LocalServerReceiver with port 8080
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8081).build();


        // Authorize using AuthorizationCodeInstalledApp with the custom receiver
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");


        Map<String, String> credentials = new HashMap<>();
        credentials.put("key", CLIENT_ID);
        credentials.put("secret", CLIENT_SECRET);


        FirefoxOptions options = new FirefoxOptions().setProfile(new FirefoxProfile());
        options.addArguments("credentials=" + credentials);
        options.addArguments("--auth-server-whitelist=*");
        options.addArguments("--disable-web-security");
        options.addArguments("--user-data-dir=firefox-profile");
        options.addArguments("--disable-infobars");

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-blink-features=AutomationControlled");

      */  WebDriver driver = new FirefoxDriver(/*options*/);

        // Navigate to the Gmail website using the webdriver.
        driver.navigate().to("https://mail.google.com?auth=0Adeu5BXm6qRjxPwHSkc-RP40hzFIRZ-RvJlXqrYevBXIpzRFShqDoMbjbfKfhApghbdUOA");


        try
        {

            // Locate the username and password fields and fill them in
            WebElement usernameField = driver.findElement(By.id("username")); // Replace with the actual username field ID
            WebElement passwordField = driver.findElement(By.id("password")); // Replace with the actual password field ID

            // Enter your credentials
            usernameField.sendKeys("novalnetesolutions2010@gmail.com");
            passwordField.sendKeys("Novalnet@payment@No1Gateway");

            // Submit the login form
            WebElement loginButton = driver.findElement(By.id("loginButton")); // Replace with the actual login button ID
            loginButton.click();

            // You can now perform actions on the logged-in page

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*    FirefoxOptions options = new FirefoxOptions();
            options.setProfile(new FirefoxProfile()); // Use a default profile

            WebDriver driver = new FirefoxDriver(options);



            options.setLogLevel(FirefoxDriverLogLevel.ERROR);  // Set log level to ERROR to minimize console output

            // Add your custom options here, e.g., options.addArguments("--your-custom-option");



            // Navigate to Gmail
          //  driver.get("https://mail.google.com");
            driver.get("https://mail.google.com?auth=0Adeu5BXm6qRjxPwHSkc-RP40hzFIRZ-RvJlXqrYevBXIpzRFShqDoMbjbfKfhApghbdUOA");
            // start if profile is not set
            try {
                driver.findElement(By.xpath(signIn));
                driver.findElement(By.xpath(signIn)).click();
            }catch(Exception e){}

            try {
                driver.findElement(By.xpath(email));
                driver.findElement(By.xpath(email)).sendKeys("novalnetesolutions2010@gmail.com");
                driver.findElement(By.xpath(nextBtn)).click();
                Thread.sleep(5000);
            }catch(Exception e){} // need to add log

            try {
                driver.findElement(By.name(pwd));
                driver.findElement(By.name(pwd)).sendKeys("Novalnet@payment@No1Gateway");
                driver.findElement(By.xpath(nextBtn)).click();
                Thread.sleep(5000);
            }catch(Exception e){} // need to add log
            // end if profile is not set
            //   performWebAutomation(driver); //commented as this test code to google pay using paygate
            // driver.quit();
        } catch (Exception e) {
            // need to add logic later while automating google pay
        }
*/
        }


    private static void performWebBetaAutomation(WebDriver driver) throws InterruptedException {



       driver.get("https://mail.google.com");

        try{
            Thread.sleep(5);
            if(driver.findElement(By.xpath(signIn)).isDisplayed()){
                driver.findElement(By.xpath(signIn)).click();
                driver.findElement(By.xpath("email")).sendKeys("novalnetesolutions2010@gmail.com");
                driver.findElement(By.xpath(nextBtn)).click();
                driver.findElement(By.name(pwd)).sendKeys("Novalnet@payment@No1Gateway");
                driver.findElement(By.xpath(nextBtn)).click();
                Thread.sleep(5000);
            }
        }catch (Exception e){
            System.out.println("Element is not displayed already logged in");
        }

        driver.get("https://adminbeta.novalnet.de/v13_iframe.php?acss=Tm92YWxuZXQxMjM0NTQ=&test=0");
        Thread.sleep(5000);
        driver.switchTo().frame(driver.findElement(By.cssSelector("#v13Form")));
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.cssSelector("#googlepay")));
        js.executeScript("arguments[0].click();", driver.findElement(By.cssSelector("#googlepay")));
        Thread.sleep(5000);
        driver.findElement(By.cssSelector("#googlepay_container button")).click();
        Thread.sleep(5000);
        System.out.println("Available window size: "+driver.getWindowHandles().size());
        String currentWindow = driver.getWindowHandle();
        driver.getWindowHandles().forEach(w->{
            if(!w.equals(currentWindow))
                driver.switchTo().window(w)
                        ;
        });
        System.out.println(driver.getTitle());
        Thread.sleep(5000);
        driver.switchTo().frame(driver.findElement(By.cssSelector("iframe[src^='https://payments.google.com/payments/u/0/embedded/']")));
        driver.findElement(By.cssSelector(".goog-inline-block.jfk-button[data-was-visible]")).click();
        Thread.sleep(10000);
        driver.quit();

    }

    private static void performWebAutomation(WebDriver driver) {

        try {
            driver.get("https://paygate.novalnet.de/paygate.jsp?vendor=4&product=14&tariff=30&lang=en");
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));

            Actions actions = new Actions(driver);

            WebElement gender = driver.findElement(By.id("gender"));
            Select selectGender = new Select(gender);
            selectGender.selectByVisibleText("Male");

            WebElement firstName = driver.findElement(By.id("first_name"));
            actions.sendKeys(firstName, "Nobert").perform();

            WebElement lastName = driver.findElement(By.id("last_name"));
            actions.sendKeys(lastName, "Maier").perform();


            WebElement email = driver.findElement(By.id("email"));
            email.sendKeys("novalnetesolutions2010@gmail.com");

            WebElement company = driver.findElement(By.id("company"));
            actions.sendKeys(company, "A.B.C. Gerüstbau GmbH").perform();

            WebElement street = driver.findElement(By.id("street"));
            actions.sendKeys(street, "Hauptstr").perform();

            WebElement houseNo = driver.findElement(By.id("house_no"));
            actions.sendKeys(houseNo, "9").perform();

            WebElement zip = driver.findElement(By.id("zip"));
            actions.sendKeys(zip, "66862").perform();

            WebElement city = driver.findElement(By.id("city"));
            actions.sendKeys(city, "Kaiserslautern").perform();


            WebElement country = driver.findElement(By.id("country_code"));
            Select selectCountry = new Select(country);
            selectCountry.selectByVisibleText("Germany");

            WebElement mobileNo = driver.findElement(By.id("mobile"));
            actions.sendKeys(mobileNo, "01747781423").perform();


            WebElement selectCC = driver.findElement(By.id("googlepay"));
            actions.click(selectCC).perform();
            Thread.sleep(2000);
            driver.findElement(By.cssSelector("div.gpay-card-info-animation-container")).click();

            Thread.sleep(5000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5000));





// Switch to the iframe using its ID


// Now, you can interact with elements inside the iframe using JavaScript
            ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('J9kIQ');");


            System.out.println(driver.getPageSource());

            // Assuming 'driver' is your WebDriver instance
            // Assuming 'driver' is your WebDriver instance
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("frames['sM432dIframe'].window.focus();");


// Switch to the iframe using JavaScript
            js.executeScript("var iframe = document.getElementById('sM432dIframe');"
                    + "iframe.contentWindow.focus();");

            System.out.println(driver.getPageSource());

        // Switch to the iframe once it's available




        WebElement iframe= driver.findElement(By.cssSelector("#iframeStart+div iframe"));
        driver.switchTo().frame(iframe);
        new Actions(driver).sendKeys(Keys.TAB).perform();
        sleep(1000);
        new Actions(driver).sendKeys(Keys.TAB).perform();
        sleep(1000);
        new Actions(driver).sendKeys(Keys.TAB).perform();
        sleep(1000);
        new Actions(driver).sendKeys(Keys.TAB).perform();




     //   WebElement iframe = driver.findElement(By.cssSelector("#iframeBody"));

      /*  driver.switchTo().frame(0); // Switch to the first iframe

// Execute JavaScript to get the HTML content of the entire iframe


        /*driver.findElement(By.xpath ("//body[@id='iframeBody']"));

        driver.switchTo().frame(driver.findElement(By.cssSelector("iframe[data-node-index='0;0']")));

        // Now, you can interact with the elements inside the iframe
        // For example, let's find and click the element with a specific XPath
        WebElement element = driver.findElement(By.xpath("//div[contains(@style, 'background-image: url(https://www.gstatic.com/instantbuy/svg/dark/en.svg)')]"));
        element.click();
        highlightElement(element);
        sleep(5);
*/

       /* WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1000));

        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("#iframeBody")));*/

       /* checkElementDisplayed(By.cssSelector("#iframeBody"));
        getElements(By.tagName("iframe")).stream()
                //.filter(e->!e.getAttribute("src").contains("https://secure.novalnet.de/"))
                .forEach(e1->{
                    System.out.println(e1.getAttribute("src"));
                    switchToFrame(e1,driver);
                    if(checkElementDisplayed(By.cssSelector("#iframeBody"))){
                        clickElement(By.xpath("//div[text()='Pay']"));
                    }
                    driver.switchTo().parentFrame();
                });*/
        // Switch to the iframe


        // Now you are inside the iframe, you can interact with its elements
        // For example, locate and click the "Pay" button within the iframe
        WebElement payButton = driver.findElement(By.xpath("//div[contains(text(),'Pay OXID DEMO SHOP')]"));
        payButton.click();
        }catch (Exception e){e.printStackTrace();};

 /*       WebElement iframe = driver.findElement(By.id("iframeStart"));
        driver.switchTo().frame(iframe);
        WebElement payButton = driver.findElement(By.xpath("//div[@class='b3id-widget-button']"));
        payButton.click();

*/
    }

    public static void launchGooglePayDriver(){
        Map<String, String> credentials = new HashMap<>();
        credentials.put("key", CLIENT_ID);
        credentials.put("secret", CLIENT_SECRET);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("credentials=" + credentials);
        options.addArguments("--auth-server-whitelist=*");
        options.addArguments("--disable-web-security");
        options.addArguments("--user-data-dir=chrome-profile");
        options.addArguments("--disable-infobars");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        DriverManager.setDriver(driver);
    }

    public static void loginIntoGoogleAccount(){
            DriverActions.openURL("https://mail.google.com");
            sleep(3);
            if(checkElementDisplayed(By.xpath(signIn))){
                clickElement(By.xpath(signIn));
                setText(By.xpath("email"),"novalnetesolutions2010@gmail.com");
                clickElement(By.xpath(nextBtn));
                setText(By.name(pwd),"Novalnet@payment@No1Gateway");
                clickElement(By.xpath(nextBtn));
                sleep(5);
            }
    }

    public static void main(String[] args) throws InterruptedException {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("key", CLIENT_ID);
        credentials.put("secret", CLIENT_SECRET);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("credentials=" + credentials);
        options.addArguments("--auth-server-whitelist=*");
        options.addArguments("--disable-web-security");
        options.addArguments("--user-data-dir=chrome-profile");
        options.addArguments("--disable-infobars");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        DriverManager.setDriver(driver);

        DriverManager.getDriver().get("https://mail.google.com");

        try{
            Thread.sleep(5);
            if(DriverManager.getDriver().findElement(By.xpath(signIn)).isDisplayed()){
                DriverManager.getDriver().findElement(By.xpath(signIn)).click();
                DriverManager.getDriver().findElement(By.xpath("email")).sendKeys("novalnetesolutions2010@gmail.com");
                DriverManager.getDriver().findElement(By.xpath(nextBtn)).click();
                DriverManager.getDriver().findElement(By.name(pwd)).sendKeys("Novalnet@payment@No1Gateway");
                DriverManager.getDriver().findElement(By.xpath(nextBtn)).click();
                Thread.sleep(5000);
            }
        }catch (Exception e){
            System.out.println("Element is not displayed already logged in");
        }

        driver.get("https://adminbeta.novalnet.de/v13_iframe.php?acss=Tm92YWxuZXQxMjM0NTQ=&test=0");
        Thread.sleep(5000);
        driver.switchTo().frame(driver.findElement(By.cssSelector("#v13Form")));
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.cssSelector("#googlepay")));
        js.executeScript("arguments[0].click();", driver.findElement(By.cssSelector("#googlepay")));
        Thread.sleep(5000);
        driver.findElement(By.cssSelector("#googlepay_container button")).click();
        Thread.sleep(5000);
        System.out.println("Available window size: "+driver.getWindowHandles().size());
        String currentWindow = driver.getWindowHandle();
        driver.getWindowHandles().forEach(w->{
            if(!w.equals(currentWindow))
                driver.switchTo().window(w);
        });
        System.out.println(driver.getTitle());
        Thread.sleep(5000);
        driver.switchTo().frame(driver.findElement(By.cssSelector("iframe[src^='https://payments.google.com/payments/u/0/embedded/']")));
        driver.findElement(By.cssSelector(".goog-inline-block.jfk-button[data-was-visible]")).click();
        Thread.sleep(5000);
    }


    public static void waitForFrameAvailableSwitchToIt(WebElement by,WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(by));
        }catch(Throwable error) {
            Log.error("Timeout waiting for the iFrame availability "+by.toString());
            Assert.fail("Timeout waiting for the iFrame availability. " + by.toString());
        }
    }

    public static void switchToFrame(WebElement by, WebDriver driver) {
        waitForFrameAvailableSwitchToIt(by,driver);
        Log.info("Switch to iFrame by element "+by.toString());

    }
        private static void performWebAutomation1(WebDriver driver) throws InterruptedException {

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
        email.sendKeys("novalnetesolutions2010@gmail.com");

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

        Thread.sleep(5000);

        try {
            // Locate the Google Pay iframe by src attribute
       /*     WebElement iframe = driver.findElement(By.cssSelector("iframe[src*='https://pay.google.com/gp/p/ui/payframe']"));

            // Switch to the iframe
            driver.switchTo().frame(iframe);
*/
            // Get all child elements within the <div>

            WebElement iframeStartDiv = driver.findElement(By.id("iframeStart"));
            List<WebElement> childElements = iframeStartDiv.findElements(By.xpath(".//*"));

            // Loop through and print the details of each child element
            for (WebElement element : childElements) {
                System.out.println("Element TagName: " + element.getTagName());
                System.out.println("Element Text: " + element.getText());
                // Add more details as needed
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the WebDriver
            driver.quit();
        }


  //     driver.switchTo().frame(driver.findElement(By.cssSelector("iframe.gpay-card-info-iframe")));

/*




        // Get the iframe element
     //   WebElement iframe = driver.findElement(By.cssSelector("iframe[src*='https://pay.google.com/gp/p/ui/payframe']"));
        WebElement iframe = findIframeByPartialId(driver, "frame");

        if (iframe != null) {
            // Switch to the found iframe
            Log.info("found frame");
            driver.switchTo().frame(iframe);
        }
*/


        // Switch to the iframe


   /*     // Find the PAY button element
        WebElement payButton = null;
        if (driver.findElements(By.cssSelector("div.b3-buy-flow .goog-inline-block")).size() > 0) {
            // Continue with the wait and click logic
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", payButton);
        } else {
            System.out.println("The 'PAY' button was not found in the iframe.");
            driver.quit();
        }*/


        // Attempt to find and click the "PAY" button in frames
      /*  if (clickPayButtonInFrames(driver)) {
            System.out.println("Clicked the 'PAY' button.");
        } else {
            System.out.println("The 'PAY' button was not found in any frame.");
        }
*/

       /* WebElement iframe = driver.findElement(By.cssSelector("iframe[src*='https://pay.google.com/gp/p/ui/payframe']"));
        driver.switchTo().frame(iframe);*/

        // Locate the <div> element by its id
/*        WebElement iframeStartDiv = driver.findElement(By.id("iframeStart"));

        // Get all child elements within the <div>
        List<WebElement> childElements = iframeStartDiv.findElements(By.xpath(".//*"));

        // Loop through and print the details of each child element
        for (WebElement element : childElements) {
            System.out.println("Element TagName: " + element.getTagName());
            System.out.println("Element Text: " + element.getText());
            // Add more details as needed
        }*/
        // Wait for the page to load



       //  iframe = driver.findElement(By.cssSelector("iframe[src*='https://pay.google.com/gp/p/ui/payframe']"));

        // Switch to the iframe
   /*     driver.switchTo().frame(iframe);

        // Simulate pressing the Tab key twice using Keys class


        // Simulate pressing Enter using Keys class

        iframe.sendKeys(Keys.TAB, Keys.TAB);
        iframe.sendKeys(Keys.TAB, Keys.TAB);
        iframe.sendKeys(Keys.TAB, Keys.TAB);

        // Simulate pressing the Enter key using Actions class

        actions.sendKeys(Keys.ENTER).perform();*/

        // Switch back to the default content
        driver.switchTo().defaultContent();
     Thread.sleep(5000);

     /*   String parentHandle = driver.getWindowHandle();
       for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(parentHandle)) {
                driver.switchTo().window(handle);
            }
        }*/

        // Start the recursive search for the "PAY" button
        /*findAndClickPayButton(driver);*/
 /*       driver.switchTo().frame(driver.findElement(By.id("iframeBody")));

        // Find and click the "PAY" button using the provided XPath
        WebElement payButton = driver.findElement(By.cssSelector("div.b3-buy-flow .goog-inline-block"));
        payButton.click();driver.switchTo().defaultContent();

        // Switch back to the default content (outside the iframe)
        driver.switchTo().defaultContent();
        Thread.sleep(5000);
        }
  */
    }


    private static void findAndClickPayButton(WebDriver driver) {
        // Locate the "PAY" button in the current frame, if it exists
     //   WebElement payButton = findPayButtonInCurrentFrame(driver);

    /*    if (payButton != null) {
            payButton.click(); // Click the "PAY" button if found
        } else */{
            // If the button is not found in the current frame, check all child iframes
            for (WebElement iframe : driver.findElements(By.tagName("iframe"))) {
                // Switch to the child iframe
                driver.switchTo().frame(iframe);

                // Recursively search for the "PAY" button in the child iframe
                findPayButtonInCurrentFrame(driver);

                // Switch back to the parent frame to continue searching
                driver.switchTo().parentFrame();
            }
        }
    }
        private static WebElement findPayButtonInCurrentFrame(WebDriver driver) {
            try {
                return driver.findElement(By.cssSelector("div.b3-buy-flow .goog-inline-block")); // Replace with the actual button locator
            } catch (org.openqa.selenium.NoSuchElementException e) {
                return null; // Button not found in the current frame
            }
        }

    private static WebElement findIframeByPartialId(WebDriver driver, String partialId) {
        for (WebElement iframe : driver.findElements(By.tagName("iframe"))) {
            if (iframe.getAttribute("id").contains(partialId)) {
                return iframe;
            }
        }
        return null;
    }


    private static boolean clickPayButtonInFrames(WebDriver driver) {
        // Get the total number of frames on the page
        int frameCount = driver.findElements(By.tagName("iframe")).size();

        // Loop through each frame and attempt to find the "PAY" button
        for (int i = 0; i < frameCount; i++) {
            driver.switchTo().defaultContent(); // Switch back to the main content

            try {
                // Switch to the next frame
                WebElement frame = driver.findElements(By.tagName("iframe")).get(i);
                String frameSource = frame.getAttribute("src");
                System.out.println("Frame Source: " + frameSource);
                driver.switchTo().frame(frame);

                // Print all elements in the frame
                printElementsInFrame(driver);
            } catch (org.openqa.selenium.NoSuchElementException | org.openqa.selenium.TimeoutException e) {
                System.out.println("Frame elements could not be printed for this frame.");
                continue;
            }
        }
    // Return false if the button was not found in any frame
        return false;
    }

    private static void printElementsInFrame(WebDriver driver) {
        // Find all elements in the frame
        java.util.List<WebElement> elements = driver.findElements(By.xpath("//*"));

        // Iterate through the elements and print their details
        for (WebElement element : elements) {
            String tagName = element.getTagName();
            String xpath = getElementXPath(driver, element);
            System.out.println("Element TagName: " + tagName);
            System.out.println("Element XPath: " + xpath);
            // Add more details as needed
        }
    }


    // Function to get the XPath of an element
    private static String getElementXPath(WebDriver driver, WebElement element) {
        return (String) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("function absoluteXPath(element) {" +
                        "var comp, comps = [];" +
                        "var parent = null;" +
                        "var xpath = '';" +
                        "var getPos = function(element) {" +
                        "var position = 1, curNode;" +
                        "if (element.nodeType == Node.ATTRIBUTE_NODE) {" +
                        "return null;" +
                        "}" +
                        "for (curNode = element.previousSibling; curNode; curNode = curNode.previousSibling) {" +
                        "if (curNode.nodeName == element.nodeName) {" +
                        "++position;" +
                        "}" +
                        "}" +
                        "return position;" +
                        "};" +

                        "if (element instanceof Document) {" +
                        "return '/';" +
                        "}" +

                        "for (; element && !(element instanceof Document); element = element.nodeType == Node.ATTRIBUTE_NODE ? element.ownerElement : element.parentNode) {" +
                        "comp = comps[comps.length] = {};" +
                        "switch (element.nodeType) {" +
                        "case Node.TEXT_NODE:" +
                        "comp.name = 'text()';" +
                        "break;" +
                        "case Node.ATTRIBUTE_NODE:" +
                        "comp.name = '@' + element.nodeName;" +
                        "break;" +
                        "case Node.PROCESSING_INSTRUCTION_NODE:" +
                        "comp.name = 'processing-instruction()';" +
                        "break;" +
                        "case Node.COMMENT_NODE:" +
                        "comp.name = 'comment()';" +
                        "break;" +
                        "case Node.ELEMENT_NODE:" +
                        "comp.name = element.nodeName;" +
                        "break;" +
                        "}" +
                        "comp.position = getPos(element);" +
                        "}" +

                        "for (var i = comps.length - 1; i >= 0; i--) {" +
                        "comp = comps[i];" +
                        "xpath += '/' + comp.name.toLowerCase();" +
                        "if (comp.position !== null) {" +
                        "xpath += '[' + comp.position + ']';" +
                        "}" +
                        "}" +

                        "return xpath;" +

                        "}" +

                        "return absoluteXPath(arguments[0]);", element);
    }

    // added new function to launch gmail

    public static void launchDriverWithGmail(){
        ExtentTestManager.saveToReport("Setup", "Setting up the customer.");
     //   createGooglePayCustomer();
        Map<String, String> credentials = new HashMap<>();
        credentials.put("key", CLIENT_ID);
        credentials.put("secret", CLIENT_SECRET);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("credentials=" + credentials);
        options.addArguments("--auth-server-whitelist=*");
        options.addArguments("--disable-web-security");
        options.addArguments("--user-data-dir=chrome-profile");
        options.addArguments("--disable-infobars");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-blink-features=AutomationControlled");
        if(HEADLESS)
            options.addArguments("--headless");
       // System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver-linux64"); // temp fix for now
        WebDriver driver = new ChromeDriver(options);
        DriverManager.setDriver(driver);
        driver.navigate().to("https://mail.google.com");
        driver.manage().window().maximize();
        sleep(10);
        // start if profile is not set
        if (getPageTitle().equals("Gmail: Private and secure email at no cost | Google Workspace")) {
            clickElement(By.xpath(signIn));
         //   setText(By.xpath(email), "novalnetesolutions2010@gmail.com");
          //  clickElement(By.cssSelector(nextBtn));
            setTextAndKey(By.xpath(email), "novalnetesolutions2010@gmail.com", Keys.ENTER);
            // need to add log
          //  setText(By.name(pwd), "Novalnet@payment@No1Gateway");
          //  clickElement(By.cssSelector(nextBtn));
            setTextAndKey(By.name(pwd), "Novalnet@payment@No1Gateway", Keys.ENTER);
            // need to add log
        }
        else if (getPageTitle().equals("Gmail")){

            //setText(By.xpath(email), "novalnetesolutions2010@gmail.com");
            setTextAndKey(By.xpath(email), "novalnetesolutions2010@gmail.com", Keys.ENTER);

            /*try {
                byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("SCREENSHOT. Click base64 img", new ByteArrayInputStream(screenshotBytes));
                System.out.println("Screenshot captured and attached to Allure report.");
            } catch (Exception e) {
                System.out.println("Failed to capture and attach screenshot to Allure report: " + e.getMessage());
            }*/
           // clickElement(By.cssSelector(nextBtn));
            // need to add log
          //  setText(By.name(pwd), "Novalnet@payment@No1Gateway");
            setTextAndKey(By.name(pwd), "Novalnet@payment@No1Gateway", Keys.ENTER);
            //clickElement(By.cssSelector(nextBtn));
        }
    }
}

