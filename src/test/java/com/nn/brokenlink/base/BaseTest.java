package com.nn.brokenlink.base;

import com.nn.drivers.DriverManager;
import com.nn.reports.ExtentTestManager;
import com.nn.testcase.SheetsQuickstart;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Allure;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.nn.constants.Constants.HEADLESS;
import com.nn.drivers.DriverManager;
import com.nn.reports.ExtentTestManager;
import com.nn.utilities.DriverActions;
import io.qameta.allure.Allure;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.nn.constants.Constants.HEADLESS;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
public class BaseTest {

    private Set<String> checked200UrlS = new HashSet<>();
    private Set<String> checkedURLs = new HashSet<>();
    private Set<String> checkedimageURLs = new HashSet<>();
    private AtomicInteger count = new AtomicInteger(0);
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens/path";
    static Sheets.Spreadsheets spreadsheets;
    public static final String existingSpreadSheetID = "1AF3YdABMEvVXzE7T6v8sVV4wGFpHee_tp3sufpENJLg";
    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static final Lock lock = new ReentrantLock();

    private static File xl = new File(System.getProperty("user.dir") + "/src/test/resources/HomePage.xlsx");
    @BeforeTest(alwaysRun = true)
    @Parameters({"BROWSER"})
    public void createDriver(@Optional("chrome") String browser) {
        WebDriver driver = setupBrowser(browser);
        DriverManager.setDriver(driver);
    }

    @AfterTest(alwaysRun = true)
    public void quitDriver() {
        if (DriverManager.getDriver() != null) {
            DriverManager.quit();
            System.out.println(count);
        }

    }


    public static WebDriver setupBrowser(String browserName) {
        WebDriver driver;
        switch (browserName.trim().toLowerCase()) {
            case "chrome":
                driver = chromeDriver();
                break;
            case "firefox":
                driver = firefoxDriver();
                break;
            case "edge":
                driver = edgeDriver();
                break;
            default:
                System.out.println("Browser " + browserName + " name is invalid. Launching chrome as default");
                driver = chromeDriver();
        }
        return driver;
    }

    private static WebDriver chromeDriver() {
        WebDriver driver;
        System.out.println("Launching Chrome Driver...");
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        ChromeOptions options = new ChromeOptions();
        if (HEADLESS)
            options.addArguments("--headless");     //options.addArguments("--headless=new");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        driver = new ChromeDriver(options);
        if (HEADLESS)
            driver.manage().window().setSize(new Dimension(1920, 1080));
        else
            driver.manage().window().maximize();
        return driver;
    }


    private static WebDriver firefoxDriver() {
        WebDriver driver;
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.out.println("Launching FireFox Driver...");
        FirefoxOptions options = new FirefoxOptions();
        if (HEADLESS)
            options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        if (HEADLESS)
            driver.manage().window().setSize(new Dimension(1920, 1080));
        else
            driver.manage().window().maximize();
        return driver;
    }

    private static WebDriver edgeDriver() {
        WebDriver driver;
        System.out.println("Launching Edge Driver...");
        EdgeOptions options = new EdgeOptions();
        driver = new EdgeDriver(options);
        driver.manage().window().maximize();
        return driver;
    }


    public void checkAllLinks() throws IOException, GeneralSecurityException {
        DriverManager.getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
        DriverActions.waitForAllElementLocated(By.tagName("a"));

        List<WebElement> allLinks = DriverActions.getElements(By.tagName("a"));
        for (WebElement link : allLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && href.contains("novalnet")) {
                verifyLink( "N/A", href);
            }
        }

    }

    public void checkSublinks() throws IOException, GeneralSecurityException {
        List<String> novalnetLinks = getAllNovalnetLinks();

        for (String url : novalnetLinks) {
            DriverActions.openURL(url);
            DriverActions.waitForAllElementLocated(By.tagName("a"));
            List<WebElement> innerLinks = DriverActions.getElements(By.tagName("a"));
            for (WebElement innerLink : innerLinks) {
                String subUrl = innerLink.getAttribute("href");
                if (subUrl != null && !subUrl.isEmpty()
                        && (subUrl.startsWith("https://") || subUrl.startsWith("http://"))) {
                    verifyLink(url, subUrl);
                }
            }
        }

    }


    private List<String> getAllNovalnetLinks() {
        DriverManager.getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
        DriverActions.waitForAllElementLocated(By.tagName("a"));
        List<WebElement> allLinks = DriverActions.getElements(By.tagName("a"));
        List<String> novalnetLinks = new ArrayList<>();

        for (WebElement link : allLinks) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("novalnet")) {
                novalnetLinks.add(href);
            }
        }

        return novalnetLinks;
    }

    private void verifyLink(String sourceUrl, String url) throws IOException, GeneralSecurityException {
        boolean result =false;
        for(String successURs :checked200UrlS){
            result = successURs.equals(url);
            if (result==true)
                break;
        }
        if(result!=true){
            int statusCode = 0;
            String statusMessage = null;
            int currentCount = count.incrementAndGet();

            try {
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000) // 30 seconds connect
                        // timeout
                        .setSocketTimeout(30 * 1000) // 30 seconds socket timeout
                        .build();

                HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
                HttpGet request = new HttpGet(url);
                HttpResponse response = httpClient.execute(request);
                statusCode = response.getStatusLine().getStatusCode();
                StatusLine statusLine = response.getStatusLine();
                statusMessage = statusLine.getReasonPhrase();

                if (statusCode == 200) {
                    System.out.println(currentCount + ": " + url + ": " + "Link is valid(HTTP response code: " + statusCode + ")");
                    if(checked200UrlS.add(url)){
                        writeDataToSheet("BROKENLINKS_DE", new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), xl);
                    }

                } else {
                    writeDataToSheet("BROKENLINKS_DE", new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), xl);
                    System.err.println(currentCount + ": " + url + ": " + "Link is broken (HTTP response code: "
                            + statusCode + ")");
                }
            } catch (Exception e) {
                if (statusCode == 0) {
                    writeDataToSheet("BROKENLINKS_DE", new ArrayList<Object>(Arrays.asList(url, statusCode, "null", sourceUrl)), xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                } else {
                    writeDataToSheet("BROKENLINKS_DE", new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                }
            }
        }

        }


    public void metaDataCheck() throws IOException, GeneralSecurityException {
      //  getSpreadsheetInstance();
        List<String> urls = getAllNovalnetLinks();
        for(String url:urls) {
            DriverActions.openURL(url);
            String value = null;
            List<WebElement> metaTags = DriverActions.getElements(By.tagName("meta"));
            for (WebElement metTag : metaTags) {
                value = metTag.getAttribute("name").replaceAll("\\s", "");
                if (value.equals("Description")) {
                    WebElement descriptionMetaTag = DriverActions.getElement(By.xpath("//meta[@name='Description']"));
                    String description = descriptionMetaTag.getAttribute("content");
                 //   writeDataGoogleSheets("META_DESCRIPTIION_DE", new ArrayList<Object>(Arrays.asList(url, "YES", description, description.length(), value)), existingSpreadSheetID);
                    break;
                }
            }



        }

        }

    public void verifyH1Tags() throws IOException, GeneralSecurityException {
        List<String> novalnetLinks = getAllNovalnetLinks();
        List<List<Object>> dataToWrite = new ArrayList<>();

        for (String url : novalnetLinks) {
            DriverActions.openURL(url);
            if (url != null && !url.isEmpty() && url.contains("novalnet") && checkedURLs.add(url)) {
               DriverActions.waitForPageLoad();
               List<WebElement> h1Tags = DriverActions.getElements(By.xpath("//h1"));
                     dataToWrite.add(Arrays.asList(url, h1Tags.size()));
            }
        }

      //  writeDataGoogleSheetsBatch("BROKENLINKS_DE", dataToWrite, existingSpreadSheetID);

    }

    public void verifyImageAltAttributes() throws GeneralSecurityException, IOException {
        String altValue=null;
      //  getSpreadsheetInstance();
        List<String> novalnetLinks = getAllNovalnetLinks();
        List<List<Object>> dataToWrite = new ArrayList<>();

        Set<String> urlsToSkip = new HashSet<>(Arrays.asList(
                "https://www.novalnet.com/wp-content/uploads/2024/03/kreditkarte-pcidss.webp",
                "https://www.novalnet.com/wp-content/uploads/2024/03/Add-a-heading-1.svg",
                "https://www.novalnet.com/wp-content/uploads/2024/03/software_hosted-1.webp",
                "https://www.novalnet.com/wp-content/uploads/2022/08/alliance-for-cyber-security.svg",
                "https://www.novalnet.com/wp-content/uploads/2024/06/cross-border-payments-150x150.jpg",
                "https://www.novalnet.com/wp-content/uploads/2024/06/influencer-marketing-150x150.jpg",
                "https://www.novalnet.com/wp-content/uploads/2024/06/ai-fraud-prevention-150x150.jpg",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/linkedin.svg",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/twitter_icon.png",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/fb_icon.png",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/instagram-icon.svg",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/payment-plugin-form.webp",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/form-bg.webp",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/x_icon.png"
        ));
        for(String novalnetURL:novalnetLinks) {
            DriverActions.openURL(novalnetURL);
            if (checkedURLs.add(novalnetURL)) {
                DriverActions.waitForAllElementLocated(By.xpath("//img"));
                List<WebElement> images = DriverActions.getElements(By.xpath("//img"));
                for (WebElement image : images) {
                    String imageURL = image.getAttribute("src");
                     altValue = image.getAttribute("alt");

                    if (urlsToSkip.contains(imageURL)) {
                        continue;
                    }
                     if(altValue.isEmpty()){
                         altValue="NIL";
                     }


                    dataToWrite.add(Arrays.asList(novalnetURL, imageURL, altValue));
                }
            }
        }
        //writeDataGoogleSheetsBatch("BROKENLINKS_DE", dataToWrite, existingSpreadSheetID);
    }

    /*public void verifyImageAltAttributes() throws GeneralSecurityException, IOException {
        getSpreadsheetInstance();
        Set<String> urlsToSkip = new HashSet<>(Arrays.asList(
                "https://www.novalnet.com/wp-content/uploads/2024/03/kreditkarte-pcidss.webp",
                "https://www.novalnet.com/wp-content/uploads/2024/03/Add-a-heading-1.svg",
                "https://www.novalnet.com/wp-content/uploads/2024/03/software_hosted-1.webp",
                "https://www.novalnet.com/wp-content/uploads/2022/08/alliance-for-cyber-security.svg",
                "https://www.novalnet.com/wp-content/uploads/2024/06/cross-border-payments-150x150.jpg",
                "https://www.novalnet.com/wp-content/uploads/2024/06/influencer-marketing-150x150.jpg",
                "https://www.novalnet.com/wp-content/uploads/2024/06/ai-fraud-prevention-150x150.jpg",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/linkedin.svg",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/twitter_icon.png",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/fb_icon.png",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/instagram-icon.svg",
                "https://www.novalnet.com/wp-content/themes/wp-bootstrap-starter/images/x_icon.png"
        ));
        List<List<Object>> dataToWrite = new ArrayList<>();
            DriverActions.openURL("https://www.novalnet.com/contact/");
                DriverActions.waitForAllElementLocated(By.xpath("//img"));
                List<WebElement> images = DriverActions.getElements(By.xpath("//img"));
                for (WebElement image : images) {
                    String imageURL = image.getAttribute("src");
                    String alt = image.getAttribute("alt");
                    if (urlsToSkip.contains(imageURL)) {
                        continue;
                    }
                    dataToWrite.add(Arrays.asList("www.novalnet.com", imageURL, alt));

            }
        writeDataGoogleSheetsBatch("BROKENLINKS_DE", dataToWrite, existingSpreadSheetID);
        }*/




    public void writeDataToSheet(String sheetName, List<Object> data, File filePath) throws IOException {
        lock.lock();  // Lock the block to prevent concurrent writes
        try {
            // Load the existing workbook
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // Get the sheet, or create it if it doesn't exist
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            }

            // Get the next available row number
            int nextRowNum = getNextRow(sheet);
            Row row = sheet.createRow(nextRowNum);

            // Write the data into the cells
            int cellNum = 0;
            for (Object value : data) {
                Cell cell = row.createCell(cellNum++);
                // Check the type of value before setting the cell value
                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else if (value instanceof Double) {
                    cell.setCellValue((Double) value);
                } else {
                    cell.setCellValue(value.toString());  // Default to string conversion
                }
            }

            // Close the input stream
            fileInputStream.close();

            // Write the changes back to the file
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);

            // Close the workbook and output stream
            workbook.close();
            outputStream.close();
        } finally {
            lock.unlock();  // Ensure the lock is released even if an exception occurs
        }
    }

    // Helper method to get the next available row number
    private int getNextRow(Sheet sheet) {
        // Iterate over each row to find the first completely empty row
        for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null || isRowEmpty(row)) {
                return rowNum;
            }
        }
        return sheet.getPhysicalNumberOfRows(); // If all rows are filled, return the next physical row number
    }

    // Helper method to check if a row is empty
    private boolean isRowEmpty(Row row) {
        for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

}




    /* public void metaDataCheck() throws IOException, GeneralSecurityException {
        getSpreadsheetInstance();
        List<String> urls = getAllNovalnetLinks();

        for(String url:urls) {
            DriverActions.openURL(url);
            boolean flag=false;
            String value = null;
            List<WebElement> metaTags = DriverActions.getElements(By.tagName("meta"));
            for (WebElement metTag : metaTags) {
                value = metTag.getAttribute("name").replaceAll("\\s", "");
                if (value.equals("description")) {
                    flag=true;
                }
                if (value.equals("Description") && flag==true) {
                    writeDataGoogleSheets("META_DATA_EN", new ArrayList<Object>(Arrays.asList(url)), existingSpreadSheetID);
                    break;
                }

            }

        }

    }*/


