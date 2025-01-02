package com.nn.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.nn.utilities.Log;

public class ExcelHelpers {

	private static String formatCell(Cell cell) {
		DataFormatter format = new DataFormatter();
		return format.formatCellValue(cell);
	}

	//open xl file
	private static File xl = new File(System.getProperty("user.dir") + "/src/test/resources/TestDataNova.xlsx");


	public static Map<String,String> xlReadPaymentCredentials() {
		Map<String,String> paymentData = new HashMap<String,String>();
		try {
			//read file
			FileInputStream read = new FileInputStream(xl);
			XSSFWorkbook readSpreadSheets = new XSSFWorkbook(read);
			XSSFSheet readSheet = readSpreadSheets.getSheetAt(0);
			Cell cell;

			//SEPA
			cell = readSheet.getRow(34).getCell(1);
			String sepaHolder = formatCell(cell);
			paymentData.put("AccountHolder", sepaHolder);
			cell = readSheet.getRow(35).getCell(1);
			String IBANDE = formatCell(cell);
			paymentData.put("IBANDE", IBANDE);
			cell = readSheet.getRow(36).getCell(1);
			String IBANAT = formatCell(cell);
			paymentData.put("IBANAT", IBANAT);

			//ACH
			cell = readSheet.getRow(116).getCell(1);
			String ACHholder = formatCell(cell);
			paymentData.put("AccountHolderACH",ACHholder);
			cell = readSheet.getRow(117).getCell(1);
			String accountNumber = formatCell(cell);
			paymentData.put("accountNumberACH",accountNumber);
			cell = readSheet.getRow(118).getCell(1);
			String routingNumber = formatCell(cell);
			paymentData.put("routingNumberACH",routingNumber);


			//Credit Card
			cell = readSheet.getRow(40).getCell(1);
			String CCHolder = formatCell(cell);
			paymentData.put("CardHolder", CCHolder);
			cell = readSheet.getRow(41).getCell(1);
			String CCNumberDirect = formatCell(cell);
			paymentData.put("CardNumberDirect", CCNumberDirect);
			cell = readSheet.getRow(42).getCell(1);
			String expMon = formatCell(cell);
			paymentData.put("ExpMon", expMon);
			cell = readSheet.getRow(43).getCell(1);
			String expYear = formatCell(cell);
			paymentData.put("ExpYear", expYear);
			cell = readSheet.getRow(44).getCell(1);
			String cvc = formatCell(cell);
			paymentData.put("CVC", cvc);
			cell = readSheet.getRow(47).getCell(1);
			String CCNumberRedirect = formatCell(cell);
			paymentData.put("CardNumberRedirect", CCNumberRedirect);
			cell = readSheet.getRow(48).getCell(1);
			String expDate = formatCell(cell);
			paymentData.put("ExpDate", expDate);
			readSpreadSheets.close();
			cell = readSheet.getRow(50).getCell(1);
			String expDate2 = formatCell(cell);
			paymentData.put("ExpDate2", expDate2);
			readSpreadSheets.close();

			//MBWay
			cell = readSheet.getRow(121).getCell(1);
			String mobileNoMBWay = formatCell(cell);
			paymentData.put("MobileNO", mobileNoMBWay);


		}catch(IOException e) {
			e.printStackTrace();
			Log.info("Unable to read xl file ");
			System.out.println("Unable to read xl file ");
		}
		return paymentData;
	}

	public static Map<String,String> xlReadInputValidationData(){
		Map<String,String> map = new HashMap<>();
		try {
			//read file
			FileInputStream read = new FileInputStream(xl);
			XSSFWorkbook readSpreadSheets = new XSSFWorkbook(read);
			XSSFSheet readSheet = readSpreadSheets.getSheetAt(1);

			String[] keys = new String[]{"Special","Numerical","Alphabetic","German"};
			for(int i=0;i<keys.length;i++) {
				map.put(keys[i], formatCell(readSheet.getRow(i+1).getCell(1)));
			}
			readSpreadSheets.close();

		}catch(IOException e) {
			e.printStackTrace();
			Log.info("Unable to read xl file ");
			System.out.println("Unable to read xl file ");
		}
		return map;
	}

	public static Map<String,String> addressGuaranteeB2C(){
		Map<String,String> map = new HashMap<>();
		try {
			//read file
			FileInputStream read = new FileInputStream(xl);
			XSSFWorkbook readSpreadSheets = new XSSFWorkbook(read);
			XSSFSheet readSheet = readSpreadSheets.getSheetAt(0);

			String[] keys = new String[]{"FirstName","LastName","Gender","DOB","HouseNo","Street","City","Zip","State","Country"};
			int lineStarting = 12;
			for(int i=0;i<keys.length;i++) {
				map.put(keys[i], formatCell(readSheet.getRow(i+lineStarting).getCell(1)));
			}
			readSpreadSheets.close();

		}catch(IOException e) {
			e.printStackTrace();
			Log.info("Unable to read xl file ");
			System.out.println("Unable to read xl file ");
		}
		return map;
	}

	public static Map<String,String> addressGuaranteeB2B(){
		Map<String,String> map = new HashMap<>();
		try {
			//read file
			FileInputStream read = new FileInputStream(xl);
			XSSFWorkbook readSpreadSheets = new XSSFWorkbook(read);
			XSSFSheet readSheet = readSpreadSheets.getSheetAt(0);

			String[] keys = new String[]{"FirstName","LastName","Company","HouseNo","Street","City","Zip","State","Country"};
			int lineStarting = 24;
			for(int i=0;i<keys.length;i++) {
				map.put(keys[i], formatCell(readSheet.getRow(i+lineStarting).getCell(1)));
			}
			readSpreadSheets.close();

		}catch(IOException e) {
			e.printStackTrace();
			Log.info("Unable to read xl file ");
			System.out.println("Unable to read xl file ");
		}
		return map;
	}
	public static Map<String,String> addressGuaranteeB2BPending(){
		Map<String,String> map = new HashMap<>();
		try {
			//read file
			FileInputStream read = new FileInputStream(xl);
			XSSFWorkbook readSpreadSheets = new XSSFWorkbook(read);
			XSSFSheet readSheet = readSpreadSheets.getSheetAt(0);

			String[] keys = new String[]{"FirstName","LastName","Company","HouseNo","Street","City","Zip","State","Country"};
			int lineStarting = 105;
			for(int i=0;i<keys.length;i++) {
				map.put(keys[i], formatCell(readSheet.getRow(i+lineStarting).getCell(1)));
			}
			readSpreadSheets.close();

		}catch(IOException e) {
			e.printStackTrace();
			Log.info("Unable to read xl file ");
			System.out.println("Unable to read xl file ");
		}
		return map;
	}

	public static Map<String,String> addressChina(){
		Map<String,String> map = new HashMap<>();
		try {
			//read file
			FileInputStream read = new FileInputStream(xl);
			XSSFWorkbook readSpreadSheets = new XSSFWorkbook(read);
			XSSFSheet readSheet = readSpreadSheets.getSheetAt(0);

			String[] keys = new String[]{"FirstName","LastName","Gender","DOB","HouseNo","Street","City","Zip","State","Country"};
			int lineStarting = 86;
			for(int i=0;i<keys.length;i++) {
				map.put(keys[i], formatCell(readSheet.getRow(i+lineStarting).getCell(1)));
			}
			readSpreadSheets.close();

		}catch(IOException e) {
			e.printStackTrace();
			Log.info("Unable to read xl file ");
			System.out.println("Unable to read xl file ");
		}
		return map;
	}

	public static Map<String,String> declineCreditCards(){
		Map<String,String> map = new HashMap<>();
		try {
			//read file
			FileInputStream read = new FileInputStream(xl);
			XSSFWorkbook readSpreadSheets = new XSSFWorkbook(read);
			XSSFSheet readSheet = readSpreadSheets.getSheetAt(0);

			String[] keys = new String[]{"Expired","Restricted","InsufficientFunds","ExpDate","CVV"};
			int lineStarting = 98;
			for(int i=0;i<keys.length;i++) {
				map.put(keys[i], formatCell(readSheet.getRow(i+lineStarting).getCell(1)));
			}
			readSpreadSheets.close();

		}catch(IOException e) {
			e.printStackTrace();
			Log.info("Unable to read xl file ");
			System.out.println("Unable to read xl file ");
		}
		return map;
	}
	

}
