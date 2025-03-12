package com.nn.utilities;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.restassured.path.json.JsonPath;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.Status;
import static com.nn.constants.Constants.*;
import com.nn.drivers.DriverManager;
import com.nn.reports.AllureManager;
import com.nn.reports.ExtentTestManager;

import io.qameta.allure.Step;


public class DriverActions {


    //WebDriver instance
    private static WebDriver driver() {
		return DriverManager.getDriver();
    }
    
    private static SoftAssert softAssert = new SoftAssert();
    
    public static void softAssertAll() {
		softAssert.assertAll();
    }
    
    public static WebElement getElement(By by) {
		return driver().findElement(by);
    }


	public static List<WebElement> getElements(By by){
		return driver().findElements(by);
    }
    
    public static JavascriptExecutor getJsExecutor() {
		return (JavascriptExecutor) driver();
    }
    
    
    public static String getBrowserInformation() {
		try {
			Capabilities capability = ((RemoteWebDriver) driver()).getCapabilities();
			return capability.getBrowserName().toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public static String getFirstMatchRegex(String inputString, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(inputString);
        String firstMatch = null;
        if(m.find()) {
            firstMatch = m.group(0);
        }
        return firstMatch;
    }

	public static String replaceUnicodeCharsWithValues(String text){
		Pattern pattern  = Pattern.compile("[^\\x00-\\x7F]");
		Matcher matcher = pattern.matcher(text);
		if(matcher.find())
			return text.replaceAll(matcher.pattern().toString(),matcher.group());
		return text;
	}
    
    public static Date getDateFromString(String pattern, String date) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		Date dateObject = null;
		try {
			dateObject = format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateObject;
	}
    
    public static String addDaysFromDate(String date,int addDays) {
		return LocalDate.parse(date).plusDays(addDays).toString();
	}

	public static String getSEPADueDate(int dueDate){
		Log.info("Calculating SEPA due date");
		LocalDate currentDate = LocalDate.now();
		LocalDate newDueDate= currentDate.plusDays(Math.max(dueDate,3)); //Math.max ensuring the minimum due date is 3 days
		//the new due date should not be set in weekend days
		if (newDueDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
			newDueDate = newDueDate.plusDays(2); // Skip Saturday, move to Monday
		} else if (newDueDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
			newDueDate = newDueDate.plusDays(1); // Skip Sunday, move to Monday
		}
		return newDueDate.toString();
	}
	public static String changePatternOfDate(String pattern, Date date) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(date);
	}

	public static Map<String,String> getUpcomingMonthDates(int numberOfMonths){
		LocalDateTime today = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		Map<String,String> upcomingMonthDates = new HashMap<>();
		for(int i=0;i<numberOfMonths;i++){
			upcomingMonthDates.put(String.valueOf(i+1),formatter.format(today.plusMonths(i)));
		}
		return upcomingMonthDates;
	}

	public static String calculateUpcomingDate(int numberOfDays){
		LocalDateTime today = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return formatter.format(today.plusDays(numberOfDays)).toString();
	}

//	public static void main(String[] args) {
//		System.out.println(getUpcomingMonthDatesInArr(2)[1]);
//	}

	public static String[] getUpcomingMonthDatesInArr(int numberOfMonths){
		LocalDateTime today = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String[] upcomingMonthDates = new String[numberOfMonths];
		for(int i=0;i<numberOfMonths;i++){
			upcomingMonthDates[i] = formatter.format(today.plusMonths(i));
		}
		return upcomingMonthDates;
	}

	public static String getUpcomingMonthDate(long numberOfMonths){
		LocalDateTime today = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return formatter.format(today.plusMonths(numberOfMonths));
	}

	public static Map<String,String> getUpcomingMonthDates(int numberOfMonths, String pattern){
		LocalDateTime today = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		Map<String,String> upcomingMonthDates = new HashMap<>();
		for(int i=0;i<numberOfMonths;i++){
			upcomingMonthDates.put(String.valueOf(i+1),formatter.format(today.plusMonths(i)));
		}
		return upcomingMonthDates;
	}

	public static Map<String,String> getUpcomingMonthDates(long numberOfMonths, String pattern){
		LocalDateTime today = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		Map<String,String> upcomingMonthDates = new HashMap<>();
		for(int i=1;i<=numberOfMonths;i++){
			upcomingMonthDates.put(String.valueOf(i),formatter.format(today.plusMonths(i)));
		}
		return upcomingMonthDates;
	}

	public static String printMap(Map<String,Object> map) {
		String text = "<table><thead>"
				+ "<tr>"
				+ "<th>Key</th>"
				+ "<th>Value</th>"
				+ "</tr>"
				+ "</thead><tbody>";
		for(Map.Entry<String, Object> m : map.entrySet()) {
			text += "<tr>"
					+ "<td>" +m.getKey()+ "</td>"
					+ "<td>"+m.getValue()+"</td>"
					+ "</tr>";
		}
		text += "</tbody></table>";
		return text;
	}

	public static String printMap(Map<String,Object> mapActual, Map<String,Object> mapExpected) {
		String text = "<table><thead>"
				+ "<tr>"
				+ "<th>Key</th>"
				+ "<th>Actual</th>"
				+ "<th>Expected</th>"
				+ "<th>Result</th>"
				+ "</tr>"
				+ "</thead><tbody>";
		boolean result = true;
		for(String key : mapActual.keySet()) {
			if(!mapActual.get(key).equals(mapExpected.get(key))){
				result = false;
			}
			text += "<tr>"
					+ "<td>" +key+ "</td>"
					+ "<td>" +mapActual.get(key)+ "</td>"
					+ "<td>" +mapExpected.get(key)+ "</td>"
					+ "<td>" +(mapActual.get(key).equals(mapExpected.get(key)) ? "<span style =\"color: green;\">MATCHED</span>" : "<span style =\"color: red;\">NOT MATCHED</span>")+ "</td>"
					+ "</tr>";
		}
		text += "</tbody></table>";
		if(result) {
			ExtentTestManager.logMessage(Status.PASS,text);
		}else {
			AllureManager.saveLog(text);
			ExtentTestManager.logMessage(Status.FAIL,text);
			Assert.fail(text);
		}
		return text;
	}

	public static String printMap(String[] key, String[] actual, String[] expected) {

		String pass = "<i class='fa-sharp fa-solid fa-badge-check' style = 'color:green'></i>";
		String fail = "<i class='fa-regular fa-octagon-xmark' style = 'color:red'></i>";
		String text = "<table><thead>"
				+ "<tr>"
				+ "<th>Type</th>"
				+ "<th>Actual value</th>"
				+ "<th>Expected Value</th>"
				+ "<th>Result</th>"
				+ "</tr>"
				+ "</thead><tbody>";
		boolean result = true;
		for(int i=0; i<actual.length;i++) {
			if(!actual[i].equals(expected[i])){
				result = false;
			}
			text += "<tr>"
					+ "<td>" +key[i]+ "</td>"
					+ "<td>" +actual[i]+ "</td>"
					+ "<td>" +expected[i]+ "</td>"
					+ "<td>" +(actual[i].equals(expected[i]) ? "<span style =\"color: green;\">MATCHED</span>" : "<span style =\"color: red;\">NOT MATCHED</span>")+ "</td>"
					//+ "<td>" +(actual[i].equals(expected[i]) ?  pass : fail)+ "</td>"
					+ "</tr>";
		}
		text += "</tbody></table>";
		AllureManager.attachHtml(text);
		if(result) {
			ExtentTestManager.logMessage(Status.PASS,text);
		}else {
			AllureManager.saveLog(text);
			ExtentTestManager.logMessage(Status.FAIL,text);
			Assert.fail(text);
		}
		return text;
	}
    
    @Step("Verify Equals: {2}- {0} and {1}")
    public static void verifyEquals(Object actual, Object expected, String message) {
		//waitForPageLoad();

		if(actual.equals(expected)) {
			Log.info(message+" Verify equals: expected [" + expected + "] and found [" + actual+"]");
			ExtentTestManager.logMessage(Status.PASS, message+" Verify equals: expected <b>[" + expected + "]</b> and found <b>[" + actual+"]</b>");
			AllureManager.saveLog(message+" Verify equals: expected [" + expected + "] and found [" + actual+"]");
		}else {
			Log.warn(message+" Verify equals: expected [" + expected + "] but found [" + actual+"]");
			ExtentTestManager.logMessage(Status.FAIL, message+" Verify equals: expected <b>[" + expected + "]</b> but found <b>[" + actual+"]</b>");
			AllureManager.saveLog(message+" Verify equals: expected [" + expected + "] but found [" + actual+"]");
			Assert.assertEquals(actual, expected, message);

		}
    }

	@Step("Verify Contains: {2}- {0} and {1}")
	public static void verifyContains(String actual, String expected, String message) {
		//waitForPageLoad();

		boolean result = actual.contains(expected) && !actual.isBlank();
		if(result) {
			Log.info(message+" Verify contains: ["+actual+"] text contains the expected text ["+expected+"]");
			ExtentTestManager.logMessage(Status.PASS, message+" Verify contains: <b>["+actual+"]</b> text contains the expected text <b>["+expected+"]</b>");
			AllureManager.saveLog(message+" Verify contains: ["+actual+"] text contains the expected text ["+expected+"]");
		}else {
			Log.warn(message+" Verify contains: ["+actual+"] text does not contain the expected text ["+expected+"]");
			ExtentTestManager.logMessage(Status.FAIL, message+" Verify contains: <b>["+actual+"]</b> text does not contain the expected text <b>["+expected+"]</b>");
			AllureManager.saveLog(message+" Verify contains: ["+actual+"] text does not contain the expected text ["+expected+"]");
			Assert.assertTrue(result);
		}
	}

    
    @Step("Verify Equals: {0} and {1}")
    public static void verifyEquals(Object actual, Object expected) {
		//waitForPageLoad();

		if(actual.equals(expected)) {
			Log.info("Verify equals: expected [" + expected + "] and found [" + actual+"]");
			ExtentTestManager.logMessage(Status.PASS, "Verify equals: expected [" + expected + "] and found [" + actual+"]");
			AllureManager.saveLog("Verify equals: expected [" + expected + "] and found [" + actual+"]");
		}else {
			Log.warn("Verify equals: expected [" + expected + "] but found [" + actual+"]");
			ExtentTestManager.logMessage(Status.FAIL, "Verify equals: expected [" + expected + "] but found [" + actual+"]");
			AllureManager.saveLog("Verify equals: expected [" + expected + "] but found [" + actual+"]");
			Assert.assertEquals(actual, expected, "Fail. Not match. '" + actual.toString() + "' != '" + expected.toString() + "'");
		}
    }
    
    @Step("Verify Equals Assert: {0} and {1}")
    public static void verifyAssertEquals(Object actual, Object expected) {
		//waitForPageLoad();

		if(actual.equals(expected)) {
			Log.info("Verify equals: expected [" + expected + "] and found [" + actual+"]");
			ExtentTestManager.logMessage(Status.PASS, "Verify equals: expected [" + expected + "] and found [" + actual+"]");
			AllureManager.saveLog("Verify equals: expected [" + expected + "] and found [" + actual+"]");
		}else {
			Log.info("Verify equals: expected [" + expected + "] but found [" + actual+"]");
			ExtentTestManager.logMessage(Status.FAIL, "Verify equals: expected [" + expected + "] but found [" + actual+"]");
			AllureManager.saveLog("Verify equals: expected [" + expected + "] but found [" + actual+"]");
			Assert.assertEquals(actual, expected, "Fail. Not match. '" + actual.toString() + "' != '" + expected.toString() + "'");
		}
    }
    
    @Step("Verify Equals Assert:{2}- {0} and {1}")
    public static void verifyAssertEquals(Object actual, Object expected, String message) {
		//waitForPageLoad();

		if(actual.equals(expected)) {
			Log.info(message+" Verify equals: expected [" + expected + "] and found [" + actual+"]");
			ExtentTestManager.logMessage(Status.PASS, message+" Verify equals: expected <b>[" + expected + "]</b> and found <b>[" + actual+"]</b>");
			AllureManager.saveLog(message+" Verify equals: expected [" + expected + "] and found [" + actual+"]");
		}else {
			Log.info(message+" Verify equals: expected [" + expected + "] but found [" + actual+"]");
			ExtentTestManager.logMessage(Status.FAIL, message+" Verify equals: expected <b>[" + expected + "]</b> but found <b>[" + actual+"]</b>");
			AllureManager.saveLog(message+" Verify equals: expected [" + expected + "] but found [" + actual+"]");
			Assert.assertEquals(actual, expected, message);
		}
    }
    
    @Step("Verify Contains Assert: {0} and {1}")
    public static void verifyContainsAssert(String actual, String expected) {
		//waitForPageLoad();

		String withoutSpaceActual = actual.replaceAll("\\s","");
		String withoutSpaceExpected = expected.replaceAll("\\s","");
		boolean result = withoutSpaceActual.contains(withoutSpaceExpected) && !actual.isBlank();
		if(result) {
			Log.info("Verify contains: '"+actual+"' text contains the expected text '"+expected+"'");
			ExtentTestManager.logMessage(Status.PASS, "Verify contains: '"+actual+"' text contains the expected text '"+expected+"'");
			AllureManager.saveLog("Verify contains: '"+actual+"' text contains the expected text '"+expected+"'");
		}else {
			Log.warn("Verify contains: '"+actual+"' text does not contain the expected text '"+expected+"'");
			ExtentTestManager.logMessage(Status.FAIL, "Verify contains: '"+actual+"' text does not contain the expected text '"+expected+"'");
			AllureManager.saveLog("Verify contains: '"+actual+"' text does not contain the expected text '"+expected+"'");
			Assert.assertTrue(result);
		}
    }
    
    @Step("Verify Contains Assert: {0} and {1}")
    public static void verifyContainsAssert(String actual, String expected, String message) {
		//waitForPageLoad();

		String withoutSpaceActual = actual.replaceAll("\\s","");
		String withoutSpaceExpected = expected.replaceAll("\\s","");
		boolean result = withoutSpaceActual.contains(withoutSpaceExpected) && !actual.isBlank();
		if(result) {
			Log.info(message+" Verify contains: ["+actual+"] text contains the expected text ["+expected+"]");
			ExtentTestManager.logMessage(Status.PASS, message+" Verify contains: <b>["+actual+"]</b> text contains the expected text <b>["+expected+"]</b>");
			AllureManager.saveLog(message+" Verify contains: ["+actual+"] text contains the expected text ["+expected+"]");
		}else {
			Log.warn(message+" Verify contains: ["+actual+"] text does not contain the expected text ["+expected+"]");
			ExtentTestManager.logMessage(Status.FAIL, message+" Verify contains: <b>["+actual+"]</b> text does not contain the expected text <b>["+expected+"]</b>");
			AllureManager.saveLog(message+" Verify contains: ["+actual+"] text does not contain the expected text ["+expected+"]");
			Assert.assertTrue(result);
		}
    }
    
    @Step("Verify Contains Assert: {0} and {1}")
    public static boolean verifyContainsAssert(By by, String expected) {
		//waitForPageLoad();

		String actual = getElementText(by).trim();
		boolean result = actual.contains(expected.trim()) && !actual.isBlank();
		if(result) {
			Log.info("Verify contains: '"+actual+"' text from '"+by.toString()+"' contains the expected text '"+expected+"'");
			ExtentTestManager.logMessage(Status.PASS, "Verify contains: '"+actual+"' text from '"+by.toString()+"' contains the expected text '"+expected+"'");
			AllureManager.saveLog("Verify contains: '"+actual+"' text from '"+by.toString()+"' contains the expected text '"+expected+"'");
		}else {
			Log.warn("Verify contains: '"+actual+"' text from '"+by.toString()+"' does not contain the expected text '"+expected+"'");
			ExtentTestManager.logMessage(Status.FAIL, "Verify contains: '"+actual+"' text from '"+by.toString()+"' does not contain the expected text '"+expected+"'");
			AllureManager.saveLog("Verify contains: '"+actual+"' text from '"+by.toString()+"' does not contain the expected text '"+expected+"'");
			Assert.assertTrue(result);
		}
		return result;
    }
    
    
    @Step("Verify Contains Assert: {0} and {1}")
    public static boolean verifyContainsAssert(By by, String expected, String message) {
		//waitForPageLoad();

		String actual = getElementText(by).trim();
		boolean result = actual.contains(expected.trim()) && !actual.isBlank();
		if(result) {
			Log.info(message+" Verify contains: ["+actual+"] text from {"+by.toString()+"} contains the expected text ["+expected+"]");
			ExtentTestManager.logMessage(Status.PASS, message+" Verify contains: <b>["+actual+"]</b> text from {"+by.toString()+"} contains the expected text <b>["+expected+"]</b>");
			AllureManager.saveLog(message+" Verify contains: ["+actual+"] text from {"+by.toString()+"} contains the expected text ["+expected+"]");
		}else {
			Log.warn(message+" Verify contains: ["+actual+"] text from {"+by.toString()+"} does not contain the expected text ["+expected+"]");
			ExtentTestManager.logMessage(Status.FAIL, message+" Verify contains: <b>["+actual+"]</b> text from {"+by.toString()+"} does not contain the expected text <b>["+expected+"]</b>");
			AllureManager.saveLog(message+" Verify contains: ["+actual+"] text from {"+by.toString()+"} does not contain the expected text ["+expected+"]");
			Assert.assertTrue(result);
		}
		return result;
    }
    
    @Step("Verify Equals Not Assert: {0} and {1}")
    public static boolean verifyElementTextEquals(Object actual, Object expected) {
		//waitForPageLoad();

		boolean result = actual.equals(expected);
		if(result) {
			Log.info("TRUE : Verify equals: expected [" + expected + "] and found [" + actual+"]");
			//ExtentTestManager.logMessage("TRUE : Verify equals: expected [" + expected + "] and found [" + actual+"]");
			//AllureManager.saveLog("TRUE : Verify equals: expected [" + expected + "] and found [" + actual+"]");
		}else {
			Log.warn("FALSE: Verify equals: expected [" + expected + "] but found [" + actual+"]");
			//ExtentTestManager.logMessage("FALSE: Verify equals: expected [" + expected + "] but found [" + actual+"]");
			//AllureManager.saveLog("FALSE: Verify equals: expected [" + expected + "] but found [" + actual+"]");
		}
		return result;
    }
    
    
    
    @Step("Check element exist {0}")
    public static boolean checkElementExist(By by) {
		//waitForPageLoad();
		List<WebElement> element = getElements(by);
		if(element.size() > 0) {
			Log.info("Check element exist "+by);
			return true;
		} else {
			Log.warn("Check element does not exist "+by);
			return false;
		}
	}
    
    @Step("Check element displayed {0}")
    public static boolean checkElementDisplayed(By by) {
		try {
			boolean status = getElement(by).isDisplayed();
			Log.info("Element is displayed "+by);
			ExtentTestManager.logMessage("Element is displayed "+by);
			return status;
		}catch(Exception e) {
			Log.info("Element is not displayed "+by);
			ExtentTestManager.logMessage("Element is not displayed "+by);
			return false;
		}
    }
    
    @Step("Check element displayed {0}")
    public static boolean checkElementDisplayed(WebElement element) {
		//waitForPageLoad();
		try {
			boolean status = element.isDisplayed();
			Log.info("Element is displayed "+element);
			//ExtentTestManager.logMessage("Element is displayed "+element);
			return status;
		}catch(Exception e) {
			Log.info("Element is not displayed "+element);
			//ExtentTestManager.logMessage("Element is not displayed "+element);
			return false;
		}
    }
    
    @Step("Check element is checked {0}")
    public static boolean checkElementChecked(By by) {
		waitForElementPresent(by);
		Log.info("Element checkbox status : "+getElement(by).isSelected()+" on "+by);
		return getElement(by).isSelected();
    }
    
    @Step("Open URL : {0}")
    public static void openURL(String url) {
		driver().get(url);

		Log.info("Open URL : "+url);
		ExtentTestManager.logMessage(Status.PASS, "Open URL : "+url);
		////waitForPageLoad();
    }
    
    @Step("Click element {0}")
    public static void clickElement(By by) {
		//waitForPageLoad();
		waitForElementVisible(by);

		getElement(by).click();
		Log.info("Click Element : "+by);
		ExtentTestManager.logMessage(Status.PASS, "Click Element : "+by);
    }

	@Step("Click element {0}")
	public static void clickElement(WebDriver driver, By by) {
		driver.findElement(by).click();
		Log.info("Click Element : "+by);
		ExtentTestManager.logMessage(Status.PASS, "Click Element : "+by);
	}


	public static String handleDataLoadedDynamically(Function<WebDriver,String> function){
		long wait = EXPLICIT_TIMEOUT;
		String value = null;
		while(wait > 0) {
			try {
				value = function.apply(driver());
				break;
			} catch (Exception e) {
				wait--;
				Log.info("Got " + e.getClass().getSimpleName() + ". So Retrying to get the value ");
				sleep(1);
			}
		}
		return value;
	}

	@Step("Click element {0}")
	public static void clickElementByRefreshing(By by) {
		long wait = EXPLICIT_TIMEOUT;
		boolean flag = false;
		while(wait > 0) {
			try {
				driver().findElement(by).click();
				flag = true;
				Log.info("Element is clicked "+by);
				break;
			} catch (Exception e) {
				wait--;
				Log.info("Unable to Click Element : " + by + " because of " + e.getClass().getSimpleName() + ". So Retrying to click");
				sleep(1.5);
			}
		}
		if(!flag)
			Assert.fail("Timeout waiting for element to click "+by);
	}

	@Step("Set text {1} on element {0}")
	public static void setTextByRefreshing(By by, String text) {
		long wait = EXPLICIT_TIMEOUT;
		boolean flag = false;
		while(wait > 0) {
			try {
				setText(by,text);
				flag = true;
				break;
			} catch (StaleElementReferenceException | ElementClickInterceptedException e) {
				wait--;
				Log.info("Unable to interact with element : " + by + " because of " + e.getClass().getSimpleName() + ". So Retrying...");
				sleep(1);
			}
		}
		if(!flag)
			Assert.fail("Timeout waiting for element to interact "+by);
	}

	@Step("Get selected option {1} on element {0}")
	public static String getSelectedOptionByRefreshing(By by) {
		long wait = 20;
		boolean flag = false;
		String value = null;
		while(wait > 0) {
			try {
				Select select = new Select(getElement(by));
				value = select.getFirstSelectedOption().getAttribute("value");
				flag = true;
				Log.info("Options value is selected "+value);
				ExtentTestManager.logMessage("Options value: "+value+" is selected from the dropdown "+by);
				break;
			} catch (Throwable e) {
				wait--;
				Log.info("No options are selected so far on element : " + by + " so " + e.getClass().getSimpleName() + ". is thrown. Now Retrying...");
				ExtentTestManager.logMessage("No options are selected so far on element : " + by + " so " + e.getClass().getSimpleName() + ". is thrown. Now Retrying...");
				sleep(1);
			}
		}
		if(!flag){
			Assert.fail("No options are selected for the element: "+by);
		}
		return value;
	}

	@Step("Interact element {0}")
	public static void handleStaleElement(By by,Consumer<WebDriver> action) {
		long wait = EXPLICIT_TIMEOUT;
		boolean flag = false;
		while(wait > 0) {
			try {
				action.accept(driver());
				flag = true;
				ExtentTestManager.logMessage(Status.PASS, "Interacted with the Element : "+by);
				Log.info("Interacted with the Element : "+by);
				break;
			} catch (Throwable e) {
				wait--;
				Log.info("Unable to interact with element : " + by + " because of " + e.getClass().getSimpleName() + ". So Retrying...");
				ExtentTestManager.logMessage("Unable to interact with element : " + by + " because of " + e.getClass().getSimpleName() + ". So Retrying...");
				sleep(1);
			}
		}
		if(!flag){
			AllureManager.saveLog(("Timeout waiting for element to interact "+by));
			Assert.fail("Timeout waiting for element to interact "+by);
		}
	}

	@Step("Interact element {0}")
	public static void handleStale(By by, Function<WebDriver,String> condition) {
		long wait = EXPLICIT_TIMEOUT;
		boolean flag = false;
		while(wait > 0) {
			try {
				condition.apply(driver());
				flag = true;
				ExtentTestManager.logMessage(Status.PASS, "Interacted with the Element : "+by);
				break;
			} catch (Throwable e) {
				wait--;
				Log.info("Unable to interact with element : " + by + " because of " + e.getClass().getSimpleName() + ". So Retrying...");
				ExtentTestManager.logMessage("Unable to interact with element : " + by + " because of " + e.getClass().getSimpleName() + ". So Retrying...");
				sleep(1);
			}
		}
		if(!flag){
			AllureManager.saveLog(("Timeout waiting for element to interact "+by));
			Assert.fail("Timeout waiting for element to interact "+by);
		}

	}

	@Step("Interact element {0}")
	public static <T> T handleStale1(By by, Function<WebDriver,Object> condition) {
		long wait = EXPLICIT_TIMEOUT;
		boolean flag = false;
		T t = null;
		while(wait > 0) {
			try {
				Object obj = condition.apply(driver());
				t = (T) obj;
				flag = true;
				ExtentTestManager.logMessage(Status.PASS, "Interacted with the Element : "+by);
				break;
			} catch (Throwable e) {
				wait--;
				Log.info("Unable to interact with element : " + by + " because of " + e.getClass().getSimpleName() + ". So Retrying...");
				ExtentTestManager.logMessage("Unable to interact with element : " + by + " because of " + e.getClass().getSimpleName() + ". So Retrying...");
				sleep(1);
			}
		}
		if(!flag){
			AllureManager.saveLog(("Timeout waiting for element to interact "+by));
			Assert.fail("Timeout waiting for element to interact "+by);
		}
		return t;
	}
    
    @Step("Click element {0} with timeout {1}")
    public static void clickElement(By by, int timeout) {
		//waitForPageLoad();
		waitForElementVisible(by,timeout);

		getElement(by).click();
		Log.info("Click Element : "+by);
		ExtentTestManager.logMessage(Status.PASS, "Click Element : "+by);
    }
    
    @Step("Click element {0}")
    public static void clickElementWithAction(By by) {
		//waitForPageLoad();
		waitForElementVisible(by);

		Actions action = new Actions(driver());
		action.moveToElement(getElement(by))
				.click().build().perform();
		Log.info("Click Element with Action: "+by);
		ExtentTestManager.logMessage(Status.PASS, "Click Element : "+by);
    }
    
    @Step("Click element {0}")
    public static void hoverAndClickElementWithAction(By by) {
		try {
			//waitForPageLoad();
			getJsExecutor().executeScript("arguments[0].scrollIntoView(true);", getElement(by));
			hoverOnElementUsingJs(by);
			getJsExecutor().executeScript("arguments[0].click();", getElement(by));
			Log.info("Hover and Click Element : "+by);
			ExtentTestManager.logMessage(Status.PASS, "Click Element : "+by);
		}catch(MoveTargetOutOfBoundsException e) {
			Log.info("MoveTargetOutOfBoundsException : "+e.getStackTrace());
		}
    }
    
    @Step("Click element {0}")
    public static void clickElementWithAction(WebElement element) {
		//waitForPageLoad();

		Actions action = new Actions(driver());
		action.moveToElement(element)
				.click().build().perform();
		Log.info("Click Element with Action: "+element.toString());
		ExtentTestManager.logMessage(Status.PASS, "Click Element : "+element.toString());
    }
    
    @Step("Click element {0}")
    public static void clickElementWithJs(By by) {
		//waitForPageLoad();
		waitForElementPresent(by);

		scrollToElement(by);
		getJsExecutor().executeScript("arguments[0].click();", getElement(by));
		Log.info("Click Element with Js: "+by);
		ExtentTestManager.logMessage(Status.PASS, "Click Element : "+by);
    }

	@Step("Double click element{0}")
	public static void doubleClickElement(By by){
		waitForElementVisible(by);
		Actions actions = new Actions(driver());
		actions.doubleClick(getElement(by)).perform();
	}
    
    @Step("Click element {0}")
    public static void clickElementWithJs(WebElement element) {
		//waitForPageLoad();
		//waitForElementPresent(element);

		//scrollToElement(element);
		getJsExecutor().executeScript("arguments[0].click();", element);
		Log.info("Click Element with Js: "+element.toString());
		ExtentTestManager.logMessage(Status.PASS, "Click Element : "+element.toString());
	}
    
    @Step("Clear text element {0}")
    public static void clearText(By by) {
		//waitForPageLoad();
		waitForElementVisible(by);

		getElement(by).clear();
		Log.info("Clear text on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Clear text on element : "+by.toString());
    }

	@Step("Clear text element {0}")
	public static void clearText(WebElement by) {
		//waitForPageLoad();
		waitForElementVisible(by);

		by.clear();
		Log.info("Clear text on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Clear text on element : "+by.toString());
	}
    
    @Step("Clear text element {0}")
    public static void clearTextWithAction(By by) {
		//waitForPageLoad();
		waitForElementVisible(by);

		Actions action = new Actions(driver());
		action.moveToElement(getElement(by))
				.click()
				.keyDown(Keys.CONTROL).sendKeys("a")
				.keyUp(Keys.CONTROL).sendKeys(Keys.BACK_SPACE)
				.build().perform();
		Log.info("Clear text on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Clear text on element : "+by.toString());
    }
    
    public static void element(By locator) {
		try {
			FluentWait<WebDriver> wait = new FluentWait<>(driver())
					.withTimeout(Duration.ofSeconds(20))
					.pollingEvery(Duration.ofMillis(500))
					.ignoring(org.openqa.selenium.NoSuchElementException.class)
					.ignoring(StaleElementReferenceException.class)
					.ignoring(InvalidArgumentException.class);
			wait.until(d -> driver().findElement(locator)).click();
		}catch(org.openqa.selenium.TimeoutException e) {
			Assert.fail("Time out waiting for the element "+locator);
		}
    }
    
    @Step("Set text {1} on {0}")
    public static void setText(By by, String text) {
		//waitForPageLoad();
		waitForElementVisible(by);

		clearText(by);
		getElement(by).sendKeys(text);
		Log.info("Set text : '"+text+"' on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Set text : '"+text+"' on element : "+by.toString());
    }


	@Step("Set text {1} on {0}")
	public static void setText(WebElement by, String text) {
		//waitForPageLoad();
		waitForElementVisible(by);

		clearText(by);
		by.sendKeys(text);
		Log.info("Set text : '"+text+"' on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Set text : '"+text+"' on element : "+by.toString());
	}

	@Step("Set text {1} on {0}")
	public static void setTextWithoutClear(By by, String text) {
		//waitForPageLoad();
		waitForElementVisible(by);

		getElement(by).sendKeys(text);
		Log.info("Set text : '"+text+"' on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Set text : '"+text+"' on element : "+by.toString());
	}
    
    @Step("Set text {1} on {0} and press key{2}")
    public static void setTextAndKey(By by, String text, Keys key) {
		//waitForPageLoad();
		waitForElementVisible(by);

		clearText(by);
		getElement(by).sendKeys(text);
		new Actions(driver()).moveToElement(getElement(by)).sendKeys(key).build().perform();

		Log.info("Set text : '"+text+"' on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Set text : '"+text+"' on element : "+by.toString());
    }
    
    @Step("Set text {1} on {0}")
    public static void setTextWithAction(By by, String text) {
		//waitForPageLoad();
		waitForElementVisible(by);

		clearTextWithAction(by);
		new Actions(driver()).moveToElement(getElement(by)).sendKeys(text).build().perform();
		Log.info("Set text with action : '"+text+"' on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Set text with action : '"+text+"' on element : "+by.toString());
    }
    
    @Step("Press key {1} on {0}")
    public static void setTextWithAction(By by, Keys keys) {
		//waitForPageLoad();
		waitForElementVisible(by);

		//clearTextWithAction(by);
		new Actions(driver()).moveToElement(getElement(by)).sendKeys(keys).build().perform();
		Log.info("Set text with action : '"+keys.toString()+"' on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Set text with action : "+keys.toString()+" on element : "+by.toString());
    }
    
    @Step("Get text of element {0}")
    public static String getElementText(By by) {
		//waitForPageLoad();
		waitForElementVisible(by);

		String text = getElement(by).getText();
		Log.info("Get text : '"+text+"' on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Get text : '"+text+"' on element : "+by.toString());
		return text;
    }
    
    @Step("Get text of input element {0}")
    public static String getInputFieldText(By by) {
		//waitForPageLoad();
		waitForElementVisible(by);

		String text = getElement(by).getAttribute("value");
		Log.info("Get text from input field : '"+text+"' on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Get text from input field : '"+text+"' on element : "+by.toString());
		return text;
    }
    
    @Step("Get text of attribute {1} on element {0}")
    public static String getElementAttributeText(By by, String attribute) {
		//waitForPageLoad();
		waitForElementVisible(by);

		String text = getElement(by).getAttribute(attribute);
		Log.info("Get text : '"+text+"' from attribute "+attribute+" on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Get text : '"+text+"' from attribute "+attribute+" on element : "+by.toString());
		return text;
    }
    
    @Step("Get css value of {1} on element {0}")
    public static String getCSSValueOfElement(By by, String css) {
		//waitForPageLoad();
		waitForElementVisible(by);

		String cssValue = getElement(by).getCssValue(css);
		Log.info("Get cssValue : '"+cssValue+"' on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Get cssValue : '"+cssValue+"' on element : "+by.toString());
		return cssValue;
    }
    
    public static List<String> getListElementsText(By by){
		waitForElementVisible(by);

		List<WebElement> listElements = getElements(by);
		List<String> list = new ArrayList<>();

		for(WebElement e : listElements)
			list.add(e.getText().trim());

		return list;
    }
    
    @Step("Get current page title")
    public static String getPageTitle() {
		Log.info("Get page title : "+driver().getTitle());
		ExtentTestManager.logMessage(Status.PASS, "Get page title : "+driver().getTitle());
		return driver().getTitle();
    }
    
    public static String getURL() {
		Log.info("Get page URL : "+driver().getCurrentUrl());
		ExtentTestManager.logMessage(Status.PASS, "Get page URL : "+driver().getCurrentUrl());
		return driver().getCurrentUrl();
    }
    
    public static void reloadPage() {
		driver().navigate().refresh();
		//waitForPageLoad();
		Log.info("Page reloaded URL :"+getURL());
    }
    
    @Step("Scroll to element {0}")
    public static void scrollToElement(By by) {
		waitForElementPresent(by);
		getJsExecutor().executeScript("arguments[0].scrollIntoView(true);", getElement(by));
		sleep(0.5);
		Log.info("Scroll to element : "+by);
    }

	@Step("Scroll to element {0}")
	public static void scrollToBottom() {
		sleep(2);
		getJsExecutor().executeScript("window.scrollTo(0, document.body.scrollHeight);");
		Log.info("Scroll to bottom");
	}
    
    @Step("Scroll to element {0}")
    public static void scrollToElement(WebElement element) {
		getJsExecutor().executeScript("arguments[0].scrollIntoView(true);", element);
		Log.info("Scroll to element : "+element.toString());
    }
    
    @Step("highlight element {0}")
    public static WebElement highlightElement(By by) {
		if(driver() instanceof JavascriptExecutor) {
			//scrollToElement(by);
			getJsExecutor().executeScript("arguments[0].style.border='3px solid red'", getElement(by));
			sleep(0.5);
		}
		return getElement(by);
    }
    
    @Step("unhighlight element {0}")
    public static WebElement unhighlightElement(By by) {
		if(driver() instanceof JavascriptExecutor) {
			//scrollToElement(by);
			getJsExecutor().executeScript("arguments[0].style.border=''", getElement(by));
			sleep(0.25);
		}
		return getElement(by);
    }
    
    
    @Step("highlight element {0}")
    public static WebElement highlightElement(WebElement element) {
		if(driver() instanceof JavascriptExecutor) {
			//scrollToElement(by);
			getJsExecutor().executeScript("arguments[0].style.border='3px solid red'", element);
			sleep(0.5);
		}
		return element;
    }
    
    @Step("hover element {0}")
    public static boolean hoverOnElement(By by) {
		try {
			new Actions(driver()).moveToElement(getElement(by)).build().perform();
			return true;
		}catch(Exception e) {
			Log.info(e.getMessage());
			return false;
		}
    }
    
    public static void hoverOnElementUsingJs(By by) {
		String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover',true, false); "
				+ "arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
		getJsExecutor().executeScript(mouseOverScript, getElement(by));
    }
    
    public static void screenshotElement(By by, String elementName) {
		File file = getElement(by).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(file, new File("./MediaFiles/Images/"+elementName+".png"));
		}catch(Exception e) {
			throw new RuntimeException();
		}
    }
    
    public static void screenShotElement(By by, String message) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
		File file = getElement(by).getScreenshotAs(OutputType.FILE);
		String path ;
		try {
			path = "./MediaFiles/Images/"+by.toString()+dateFormat.format(new Date())+".png".replaceAll("\\s", "");
			FileUtils.copyFile(file, new File(path));
		}catch(Exception e) {
			throw new RuntimeException();
		}
		ExtentTestManager.addScreenShot(path,message);
    }
    

    
    //Handle Dropdowns
    
    public static void selectDropdownByText(By by, String text) {

		new Select(getElement(by)).selectByVisibleText(text);
		Log.info("Select dropdown by visible text "+text+"  on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Select dropdown by visible text "+text+"  on element : "+by.toString());
    }
    
    public static void selectDropdownByValue(By by, String value) {
		waitForElementVisible(by);
		new Select(getElement(by)).selectByValue(value);
		Log.info("Select dropdown by value "+value+"  on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Select dropdown by value "+value+"  on element : "+by.toString());
    }
    
    public static void selectDropdownByIndex(By by, int index) {

		new Select(getElement(by)).selectByIndex(index);
		Log.info("Select dropdown by index "+index+"  on element : "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Select dropdown by index "+index+"  on element : "+by.toString());
    }
    
    public static void selectDropdownDeselectAll(By by) {

		new Select(getElement(by)).deselectAll();
		Log.info("Deselect all in the multidropdown "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Deselect all in the multidropdown "+by.toString());
    }
    
    public static void selectDropdownMultiple(By by,String[] options) {

		Select select = new Select(getElement(by));
		select.deselectAll();
		for(String s : options)
			select.selectByVisibleText(s);
		Log.info("Select multiple values : "+Arrays.toString(options)+" in the multidropdown "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Select multiple values : "+Arrays.toString(options)+" in the multidropdown "+by.toString());
    }
    
    public static String getDropdownSelectedOptionText(By by) {
		waitForElementVisible(by);
		String option = new Select(getElement(by)).getFirstSelectedOption().getText();
		Log.info("Get selected option : "+option+" in dropdown "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Get selected option : "+option+" in dropdown "+by.toString());
		return option;
    }

    public static String getDropdownSelectedOptionValue(By by) {
		waitForElementVisible(by);
		String option = new Select(getElement(by)).getFirstSelectedOption().getAttribute("value");
		Log.info("Get selected option : "+option+" in dropdown "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Get selected option : "+option+" in dropdown "+by.toString());
		return option;
    }

	public static String getDropdownSelectedOptionTextOrEmpty(By by) {
		try {
			waitForElementVisible(by);
			Select dropdown = new Select(getElement(by));

			WebElement firstSelectedOption = dropdown.getFirstSelectedOption();

			if (firstSelectedOption == null || firstSelectedOption.getText().isEmpty()) {
				Log.info("Dropdown is empty: " + by.toString());
				ExtentTestManager.logMessage(Status.INFO, "Dropdown is empty: " + by.toString());
				return "";
			}

			String option = firstSelectedOption.getText();
			Log.info("Get selected option: " + option + " in dropdown " + by.toString());
			ExtentTestManager.logMessage(Status.PASS, "Get selected option: " + option + " in dropdown " + by.toString());
			return option;
		} catch (NoSuchElementException e) {
			Log.info("No options are selected in the dropdown: " + by.toString());
			ExtentTestManager.logMessage(Status.INFO, "No options are selected in the dropdown: " + by.toString());
			return "";
		} catch (Exception e) {
			Log.info("No options are selected in the dropdown: " + by.toString());
			ExtentTestManager.logMessage(Status.INFO, "No options are selected in the dropdown: " + by.toString());
			return "";
		}
	}


	public static String getDropdownSelectedOptionValueOrEmpty(By by) {
		waitForElementVisible(by);
		Select dropdown = new Select(getElement(by));

		try {
			WebElement firstSelectedOption = dropdown.getFirstSelectedOption();

			if (firstSelectedOption == null || firstSelectedOption.getText().isEmpty()) {
				Log.info("Dropdown is empty: " + by.toString());
				ExtentTestManager.logMessage(Status.INFO, "Dropdown is empty: " + by.toString());
				return "";
			}

			String optionValue = firstSelectedOption.getAttribute("value");
			Log.info("Get selected option value: " + optionValue + " in dropdown " + by.toString());
			ExtentTestManager.logMessage(Status.PASS, "Get selected option value: " + optionValue + " in dropdown " + by.toString());
			return optionValue;
		} catch (NoSuchElementException e) {
			Log.info("No options are selected in the dropdown: " + by.toString());
			ExtentTestManager.logMessage(Status.INFO, "No options are selected in the dropdown: " + by.toString());
			return "";
		} catch (Exception e) {
			Log.info("No options are selected in the dropdown: " + by.toString());
			ExtentTestManager.logMessage(Status.INFO, "No options are selected in the dropdown: " + by.toString());
			return "";
		}
	}


	public static List<String> getDropdownAllSelectedOption(By by) {
		waitForElementPresent(by);
		List<WebElement> options =  new Select(getElement(by)).getAllSelectedOptions();
		List<String> allSelectedOptions = new ArrayList<>();
		for(WebElement e : options)
			allSelectedOptions.add(e.getText());
		return allSelectedOptions;
    }
    
    
    public static boolean selectDropdownNotInSelectClass(By by, String text) {

		try {
			List<WebElement> dropDown = getElements(by);

			for(WebElement element : dropDown) {
				Log.info(element.getText());
				if(element.getText().toLowerCase().trim().equals(text.toLowerCase().trim())) {
					element.click();
					return true;
				}
			}

		}catch(Exception e) {
			Log.info(e.getMessage());
		}
		return false;
    }
    
    public static void pressTab(By by) {
		new Actions(driver()).moveToElement(getElement(by)).sendKeys(Keys.TAB).build().perform();
    }

	public static void pressTab(WebElement by) {
		new Actions(driver()).moveToElement(by).sendKeys(Keys.TAB).build().perform();
	}

	public static void pressEnter(By by) {
		new Actions(driver()).moveToElement(getElement(by)).sendKeys(Keys.ENTER).build().perform();
	}
    public static void clickSomewhere() {
		new Actions(driver()).moveByOffset(0, 0).click().build().perform();
    }

	public static void clickOutsideForm() {
		WebElement body = driver().findElement(By.tagName("body"));
		Actions actions = new Actions(driver());
		actions.moveToElement(body).click().perform();
	}
    //Handle Windows
    
    public static void openNewTab() {
		driver().switchTo().newWindow(WindowType.TAB);
		Log.info("Open and Switch to new Tab");
    }
    
    public static void openNewWindow() {
		driver().switchTo().newWindow(WindowType.WINDOW);
		Log.info("Open and Switch to new Window");
    }
    
    public static void switchToWindow() {
		String currentWindow = driver().getWindowHandle();
		Set<String> allWindows = driver().getWindowHandles();
		for(String window : allWindows) {
			if(!window.equals(currentWindow)) {
				driver().switchTo().window(window);
			}
		}
    }
    
    public static void switchToNewWindowOrTabByIndex(int index) {
		try {
			Set<String> windowsOrTabs = driver().getWindowHandles();
			driver().switchTo().window(windowsOrTabs.toArray()[index].toString());
		}catch(NoSuchWindowException e) {
			Log.info(e.getMessage());
		}
    }
    
    public static void switchToNewWindowOrTabByTitle(String title) {
		try {
			Set<String> windowsOrTabs = driver().getWindowHandles();
			for(String tabOrWindow : windowsOrTabs) {
				if(driver().switchTo().window(tabOrWindow).getTitle().toLowerCase().trim().equals(title.toLowerCase().trim())) {
					driver().switchTo().window(tabOrWindow);
					break;
				}
			}
		}catch(NoSuchWindowException e) {
			Log.info(e.getMessage());
		}
    }
    
    //Handle iFrames
    public static void switchToFrame(By by) {
		waitForFrameAvailableSwitchToIt(by);
		Log.info("Switch to iFrame by element "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Switch to iFrame by element : "+by.toString());
    }

	public static void switchToFrame(WebElement by) {
		waitForFrameAvailableSwitchToIt(by);
		Log.info("Switch to iFrame by element "+by.toString());
		ExtentTestManager.logMessage(Status.PASS, "Switch to iFrame by element : "+by.toString());
	}
    
    public static void switchToFrame(int index, int timeout) {
		waitForFrameAvailableSwitchToIt(index, timeout);
		Log.info("Switch to iFrame by index "+index);
		ExtentTestManager.logMessage(Status.PASS, "Switch to iFrame by index : "+index);
    }
    
    public static void switchToDefaultContent() {
		driver().switchTo().defaultContent();
		Log.info("Switch to default content ");
		ExtentTestManager.logMessage(Status.PASS, "Switch to default content");
    }

	public static void switchToParentFrame() {
		driver().switchTo().parentFrame();
		Log.info("Switch to parent frame ");
		ExtentTestManager.logMessage(Status.PASS, "Switch to parent Frame");
	}
    
    public static boolean findElementInFrame(By iFrame, By by) {
		switchToFrame(iFrame);
		boolean status = checkElementDisplayed(by);
		Log.info("Find element "+by.toString()+" in frame: ["+status+"]");
		//ExtentTestManager.logMessage(Status.INFO, "Find element "+by.toString()+" in frame: ["+status+"]");
		switchToDefaultContent();
		return status;
    }
    
    //Handle Alerts
    
    public static void alertAccept() {
		waitForAlertPresent();
		driver().switchTo().alert().accept();
    }
    
    public static void alertDismiss() {
		waitForAlertPresent();
		driver().switchTo().alert().dismiss();
    }
    
    public static String alertGetText() {
		waitForAlertPresent();
		return driver().switchTo().alert().getText();
    }
    
    public static void alertSetText(String text) {
		waitForAlertPresent();
		driver().switchTo().alert().sendKeys(text);
    }

	//Web Table

	public static String getValueInTable(By tableLocCss, int col, int row){
		return getElement(tableLocCss).findElement(By.cssSelector("tbody tr:nth-of-type("+row+") td:nth-of-type("+col+")")).getText();
	}

	public static WebElement getElementInTable(By tableLocCss, int col, int row){
		return getElement(tableLocCss).findElement(By.cssSelector("tbody tr:nth-of-type("+row+") td:nth-of-type("+col+")"));
	}

	public static String getValueInTable(By tableLocCss, String colName, int row){
		List<WebElement> headerEle = getElements(By.cssSelector("thead th"));
		for(int i=0;i<headerEle.size();i++){
			if(headerEle.get(i).getText().contains(colName)){
				return getElement(tableLocCss).findElement(By.cssSelector("tbody tr:nth-of-type("+row+") td:nth-of-type("+i+")")).getText();
			}
		}
		return null;
	}
    
    
    //Handle Waits
    
    public static void sleep(double seconds) {
		try {
			//Log.info("Wait for "+seconds+" seconds");
			Thread.sleep((long) (seconds * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

	public static boolean waitForElementVisible(By by) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			return true;
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element Visible. "+by.toString());
			Assert.fail("Timeout waiting for the element Visible. " + by.toString());
			return false;
		}
	}
    
    public static boolean waitForElementVisible(WebElement element) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.visibilityOf(element));
			return true;
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element Visible. "+element.toString());
			Assert.fail("Timeout waiting for the element Visible. " + element.toString());
			return false;
		}
	}
    
    public static boolean waitForElementVisible(WebElement element, int timeout) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeout), Duration.ofMillis(500));
			wait.until(ExpectedConditions.visibilityOf(element));
			return true;
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element Visible. "+element.toString());
			Assert.fail("Timeout waiting for the element Visible. " + element.toString());
			return false;
		}
	}
    
    public static boolean waitForElementVisible(WebElement element, int timeout, String message) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeout));
			wait.until(ExpectedConditions.visibilityOf(element));
			return true;
		}catch(Throwable error) {
			Log.info(message+element);
			return false;
		}
	}

	public static boolean waitForElementsVisible(WebElement element, int timeout) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeout));
			wait.until(ExpectedConditions.visibilityOfAllElements(element));
			Log.info("Elements "+element);
			return true;
		}catch(Throwable error) {
			Log.info(element);
			return false;
		}
	}

	public static boolean waitForElementVisible(By element, int timeout, String message) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeout));
			wait.until(ExpectedConditions.visibilityOfElementLocated(element));
			return true;
		}catch(Throwable error) {
			Log.info(message+element);
			return false;
		}
	}
    public static void waitForElementVisible(By by, int seconds) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(seconds), Duration.ofMillis(500));
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element Visible. "+by.toString());
			Assert.fail("Timeout waiting for the element Visible. " + by.toString());
		}
    }
    
    public static void waitForElementRefreshed(WebElement element) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.refreshed(ExpectedConditions.stalenessOf(element)));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element Refreshed. "+element.toString());
			Assert.fail("Timeout waiting for the element Visible. " + element.toString());
		}
    }

	public static void waitForElementRefreshed(By by) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(by)));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element Refreshed. "+by.toString());
			Assert.fail("Timeout waiting for the element Refreshed. " + by);
		}
	}
    
    public static void waitForElementPresent(By by) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.presenceOfElementLocated(by));
		}catch(Throwable error) {
			Log.error("Element not present. "+by.toString());
			Assert.fail("Element not present. " + by.toString());
		}

	}
    
    public static void waitForElementToBeClickable(WebElement element) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.elementToBeClickable(element));
		}catch(Throwable error) {
			Log.error("Element not clickable. "+element.toString());
			Assert.fail("Element not clickable. " + element.toString());
		}

	}

	public static void waitForElementToBeSelected(WebElement element) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.elementToBeSelected(element));
		} catch (Throwable error) {
			Log.error("Element not selected. " + element.toString());
			Assert.fail("Element not selected. " + element.toString());
		}
	}

	public static void waitForElementToBeSelected(By by) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.elementToBeSelected(by));
		} catch (Throwable error) {
			Log.error("Element not selected. " + by.toString());
			Assert.fail("Element not selected. " + by.toString());
		}
	}
    public static void waitForElementPresent(By by, int seconds) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(seconds), Duration.ofMillis(500));
			wait.until(ExpectedConditions.presenceOfElementLocated(by));
		}catch(Throwable error) {
			Log.error("Element not present. "+by.toString());
			Assert.fail("Element not present. " + by.toString());
		}
    }
    
    public static void waitForElementDisable(By by) {
		try {
			if(checkElementExist(by)) {
				WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
				wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
			}
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element to become Invisible. "+by.toString());
			Assert.fail("Timeout waiting for the element to become Invisible. " + by.toString());
		}
    }
   
    public static void waitForElementDisable(By by, int timeout) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeout), Duration.ofMillis(500));
			wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element to become Invisible. "+by.toString());
			Assert.fail("Timeout waiting for the element to become Invisible. " + by.toString());
		}
    }
   
    
    public static void waitForElementClickable(By by) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.elementToBeClickable(by));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element to be clickable. "+by.toString());
			Assert.fail("Timeout waiting for the element to be clickable. " + by.toString());
		}
    }
    
    public static void waitForElementClickable(WebElement element) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.elementToBeClickable(element));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element to be clickable. "+element.toString());
			Assert.fail("Timeout waiting for the element to be clickable. " + element.toString());
		}
    }
    
    public static void waitForElementClickable(By by, int seconds) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(seconds), Duration.ofMillis(500));
			wait.until(ExpectedConditions.elementToBeClickable(by));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element to be clickable. "+by.toString());
			Assert.fail("Timeout waiting for the element to be clickable. " + by.toString());
		}
    }
    
    public static void waitForFrameAvailableSwitchToIt(By by) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(by));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the iFrame availability "+by.toString());
			Assert.fail("Timeout waiting for the iFrame availability. " + by.toString());
		}
    }

	public static void waitForFrameAvailableSwitchToIt(WebElement by) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(by));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the iFrame availability "+by.toString());
			Assert.fail("Timeout waiting for the iFrame availability. " + by.toString());
		}
	}
    
    public static void waitForFrameAvailableSwitchToIt(int index) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(index));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the iFrame availability "+index);
			Assert.fail("Timeout waiting for the iFrame availability. "+ index);
		}
    }
    
    public static void waitForFrameAvailableSwitchToIt(String name) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(name));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the iFrame availability "+name);
			Assert.fail("Timeout waiting for the iFrame availability. "+ name);
		}
    }
    
    public static void waitForFrameAvailableSwitchToIt(By by, int timeout) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeout), Duration.ofMillis(500));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(by));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the iFrame availability "+by.toString());
			Assert.fail("Timeout waiting for the iFrame availability. " + by.toString());
		}
    }
    
    public static void waitForFrameAvailableSwitchToIt(int index, int timeout) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeout), Duration.ofMillis(500));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(index));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the iFrame availability "+index);
			Assert.fail("Timeout waiting for the iFrame availability. " + index);
		}
    }
    
    public static void waitForTitleContains(String title) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.titleContains(title));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the title changes. "+title);
			Assert.fail("Timeout waiting for the title changes . " + title);
		}
    }


	public static void waitForTitleContains(String title, long timeOut) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeOut), Duration.ofMillis(500));
			wait.until(ExpectedConditions.titleContains(title));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the title changes. "+title);
			Assert.fail("Timeout waiting for the title changes . " + title);
		}
	}

    public static void waitForURLToBe(String url) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.urlContains(url));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the title changes. "+url);
			Assert.fail("Timeout waiting for the title changes . " + url);
		}
    }

	public static void waitForURLToBe(String url, int timeout) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeout), Duration.ofMillis(500));
			wait.until(ExpectedConditions.urlContains(url));
		}catch(Throwable error) {
			Log.error("Timeout waiting for the title changes. "+url);
			Assert.fail("Timeout waiting for the title changes . " + url);
		}
	}
    
    public static void waitForAlertPresent() {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT));
			wait.until(ExpectedConditions.alertIsPresent());
		}catch(Throwable error) {
			Log.error("Timeout waiting for the alert present. ");
			Assert.fail("Timeout waiting for the alert present. ");
		}
    }
    
    public static boolean verifyAlertPresent(long timeout) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeout));
			wait.until(ExpectedConditions.alertIsPresent());
			return true;
		}catch(Throwable error) {
			Log.error("Alert is not present");
			return false;
		}
    }
    
    
    
    public static boolean waitForElementHasAttribute(By by, String attribute, String value) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(),Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.attributeToBe(by, attribute, value));
			ExtentTestManager.logMessage("Payment Div Found");
			return true;
		}catch(Throwable error) {
			Log.error("Element "+by.toString()+" In this attribute "+attribute+" Not has this value "+value+" but found this actual value "+getElementAttributeText(by, attribute));
			Assert.fail("Element "+by.toString()+" In this attribute "+attribute+" Not has this value "+value+" but found this actual value "+getElementAttributeText(by, attribute));
			return false;
		}
    }

	public static boolean waitForNumberOfWindowsToBe(int numberOfWindows) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(10));
			wait.until(ExpectedConditions.numberOfWindowsToBe(numberOfWindows));
			return true;
		}catch(Throwable error) {
			int size = driver().getWindowHandles().size();
			Log.error("Number of windows are not matched and available number of windows "+size);
			Assert.fail("Number of windows are not matched and available number of windows "+size);
			return false;
		}
    }

	public static void setExpectedCondition(ExpectedCondition<Boolean> expectedCondition, int pollingInterval, String message){
		try{
			WebDriverWait wait = new WebDriverWait(driver(),Duration.ofSeconds(EXPLICIT_TIMEOUT),Duration.ofSeconds(pollingInterval));
			wait.until(expectedCondition);
		}catch (Exception e){
			Log.error(message);
			Assert.fail(message);
		}
	}

	public static boolean setExpectedCondition(ExpectedCondition<Boolean> expectedCondition, int timeOut){
		try{
			WebDriverWait wait = new WebDriverWait(driver(),Duration.ofSeconds(timeOut));
			wait.until(expectedCondition);
			return true;
		}catch (Exception e){
			return false;
		}
	}

	public static boolean waitForElementHasAttribute(WebElement ele, String attribute, String value) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(),Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.attributeToBe(ele, attribute, value));
			return true;
		}catch(Throwable error) {
			Log.error("Element "+ele.toString()+" In this attribute "+attribute+" Not has this value "+value+" but found this actual value ");
			Assert.fail("Element "+ele.toString()+" In this attribute "+attribute+" Not has this value "+value+" but found this actual value ");
			return false;
		}
	}
    public static void waitForElementAttributeToChange(By by, String attribute, String value) {
		try{
			WebDriverWait wait = new WebDriverWait(driver(),Duration.ofSeconds(EXPLICIT_TIMEOUT));
			wait.until( driver -> {
				String enabled = getElement(by).getAttribute(attribute);
				return enabled != null && enabled.contains(value);
			});
		}catch (TimeoutException e){
			Log.error("Element "+by.toString()+" In this attribute "+attribute+" Not has this value "+value+" but found this actual value "+getElementAttributeText(by, attribute));
			Assert.fail("Element "+by.toString()+" In this attribute "+attribute+" Not has this value "+value+" but found this actual value "+getElementAttributeText(by, attribute));
		}
    }
           
    public static void waitForPageLoad() {
		WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(PAGE_LOAD_TIMEOUT));

		ExpectedCondition<Boolean> pageLoad = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver input) {
				return getJsExecutor().executeScript("return document.readyState").equals("complete");
			}
		};

		boolean pageLoaded = getJsExecutor().executeScript("return document.readyState").equals("complete");

		if(!pageLoaded) {
			Log.info("Javascript is not loaded ");
			try {
				wait.until(pageLoad);
			}catch(Throwable error) {
				error.printStackTrace();
				Assert.fail("Timeout waiting for page load (Javascript). (" + PAGE_LOAD_TIMEOUT + "s)");
			}
		}
    }


	public static void waitForStaleness(By loc) throws Exception{
		WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT));
		try {
			WebElement bodyElement=getElement(By.tagName("body"));
			// Click outside the form by executing a JavaScript click on the body element
			JavascriptExecutor jsExecutor = (JavascriptExecutor) driver();
			jsExecutor.executeScript("arguments[0].click();", bodyElement);
		} catch (StaleElementReferenceException e) {
			// Handle the StaleElementReferenceException and retry the wait
			sleep(2);
			Log.info("Stale Element not found: #novalnet_instalment_sepa_dob exception caught");
			wait.until(ExpectedConditions.stalenessOf(getElement(loc)));
		} catch (AssertionError e) {
			Log.error("An error occurred while waiting for the staleness element: " + e.getMessage());
			Assert.fail("An error occurred while waiting for the staleness element.");
		} catch (Throwable error) {
			// Handle any other exceptions or errors that occurred during the wait
			Log.error("An error occurred while waiting for the staleness element: " + error.getMessage());
			Assert.fail("An error occurred while waiting for the staleness element.");
		}
	}

	public static String getPaymentDivBox(By paymentDiv, By paymentDivBox){
		String paymentDivSelector = paymentDiv.toString().replace("By.cssSelector: ", "");
		String paymentDivBoxSelector = paymentDivBox.toString().replace("By.cssSelector: ", "");
		return paymentDivSelector+paymentDivBoxSelector;

	}

	public static String getStringFromBy(By by) {
		String byString = by.toString();
		if (byString.startsWith("By.cssSelector: ")) {
			return byString.replace("By.cssSelector: ", "");
		} else if (byString.startsWith("By.id: ")) {
			return byString.replace("By.id: ", "");
		} else if (byString.startsWith("By.name: ")) {
			return byString.replace("By.name: ", "");
		} else if (byString.startsWith("By.xpath: ")) {
			return byString.replace("By.xpath: ", "");
		} else {
			return byString;
		}
	}

	public void clickElement(){
		Wait<WebDriver> wait = new FluentWait<>(driver())
				.pollingEvery(Duration.ofMillis(500))
				.withTimeout(Duration.ofSeconds(30))
				.ignoring(NoSuchElementException.class)
				.ignoring(ElementClickInterceptedException.class)
				.ignoring(StaleElementReferenceException.class);
		WebElement element = wait.until(d->d.findElement(By.cssSelector("")));
	}

	public String copyData(String inputText){
		openURL("https://clipboardjs.com/");
		waitForTitleContains("clipboard.js");
		scrollToElement(By.xpath("//input[@id='foo']"));
		setText(By.xpath("//input[@id='foo']"),inputText);
		clickElement(By.xpath("(//button[@type='button'])[1]"));
		waitForElementVisible(By.xpath("//button[@aria-label='Copied!']"));
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		// Get the contents of the clipboard as Transferable
		Transferable transferable = clipboard.getContents(null);
		String copiedValue=null;
		// Check if the clipboard contains string data
		if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				// Get the copied value as a String
				copiedValue = (String) transferable.getTransferData(DataFlavor.stringFlavor);

				// Print or use the copied value
				System.out.println("Copied Value: " + copiedValue);
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.info("Clipboard does not contain string data.");
		}
		verifyEquals(copiedValue,inputText,"Input text: "+ inputText + "does not equals to " + copiedValue );
		return copiedValue;
	}

	public static void rightClick(){
		Actions actions = new Actions(driver());
		actions.moveByOffset(200,100).contextClick().build().perform();
		Log.info("riht click on the page");

	}
    public static void clickEscbtn(){
		Actions actions = new Actions(driver());
		actions.keyUp(Keys.ESCAPE).build().perform();
		Log.info("Click Esc Btn");
	}

	public static boolean waitForAllElementLocated(By by) {
		try {
			WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(EXPLICIT_TIMEOUT), Duration.ofMillis(500));
			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
			return true;
		}catch(Throwable error) {
			Log.error("Timeout waiting for the element Visible. "+by.toString());
			Assert.fail("Timeout waiting for the element Visible. " + by.toString());
			return false;
		}
	}
}
