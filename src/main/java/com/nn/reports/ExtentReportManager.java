package com.nn.reports;

import java.io.File;
import java.io.IOException;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.nn.constants.Constants;

import tech.grasshopper.reporter.ExtentPDFReporter;

public class ExtentReportManager {

	private static final ExtentReports extentReports = new ExtentReports();
	
	public synchronized static ExtentReports getExtentReports() {
//		ExtentPDFReporter pdf = new ExtentPDFReporter("./ExtentReports/PdfReport.pdf");
//		try {
//			pdf.loadJSONConfig(new File("src/test/resources/pdf-config.json"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		extentReports.attachReporter(pdf);
		ExtentSparkReporter spark = new ExtentSparkReporter("./ExtentReports/ExtentReport.html");
		spark.config().setDocumentTitle(Constants.REPORT_TITLE);
		spark.config().setReportName(Constants.REPORT_TITLE);
		extentReports.attachReporter(spark);
		extentReports.setSystemInfo("OS", System.getProperty("os.name"));
		extentReports.setSystemInfo("Author", "Novalnet Testing Team");
		return extentReports;
	}
}
