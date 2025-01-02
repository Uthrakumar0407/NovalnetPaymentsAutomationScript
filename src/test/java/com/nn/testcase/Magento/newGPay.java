package com.nn.testcase.Magento;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;

public class newGPay {

    @Test
    public void testGPay() throws Exception{
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "localhost:9988");
        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.get("https://accounts.google.com/servicelogin?hl=en-gb");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
        driver.findElement(By.xpath("//input[@name='identifier']")).sendKeys("mohamednizamnovalnet@gmail.com");
        driver.findElement(By.xpath("(//span[@class='VfPpkd-vQzf8d'])[2]")).click();
        driver.findElement(By.xpath("//input[@name='Passwd']")).sendKeys("novalnet123");
        driver.findElement(By.xpath("//div[@id='passwordNext']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='XY0ASe']")));
        driver.navigate().refresh();
        driver.navigate().to("https://paygate.novalnet.de/paygate.jsp?vendor=4&product=14&tariff=30&lang=en&amount=100");
        WebElement gender = driver.findElement(By.id("gender"));
        Select mygender = new Select(gender);
        mygender.selectByVisibleText("Male");
        driver.findElement(By.id("first_name")).sendKeys("Norbert");
        driver.findElement(By.id("last_name")).sendKeys("Maier");
        driver.findElement(By.id("email")).sendKeys("aa@aa.com");
        driver.findElement(By.id("company")).sendKeys("A.B.C. Gerüstbau GmbH");
        driver.findElement(By.id("street")).sendKeys("Hauptstr");
        driver.findElement(By.id("house_no")).sendKeys("9");
        driver.findElement(By.id("zip")).sendKeys("66862");
        driver.findElement(By.id("city")).sendKeys("Kaiserslautern");
        WebElement country = driver.findElement(By.id("country_code"));
        Select mycountry = new Select(country);
        mycountry.selectByVisibleText("Germany");
        driver.findElement(By.id("mobile")).sendKeys("01747781423");
        driver.findElement(By.id("googlepay")).click();
        WebDriverWait wait2 = new WebDriverWait(driver,Duration.ofSeconds(30));
        wait2.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'payment-form pf-googlepay')]//button")));
        WebElement Gpaybutton = driver.findElement(By.xpath("//div[contains(@class,'payment-form pf-googlepay')]//button"));
        Gpaybutton.click();
        Thread.sleep(5000);

        WebElement iframeElement = driver.findElement(By.cssSelector("#iframeStart+div iframe"));
        driver.switchTo().frame(iframeElement);


    }
}
