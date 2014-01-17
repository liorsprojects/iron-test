package com.ironsource.mobile.test;

import il.co.topq.mobile.client.impl.MobileClient;
import il.co.topq.mobile.common.client.enums.HardwareButtons;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import jsystem.treeui.reporter.ReportersPanel;
import junit.framework.SystemTestCase4;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.topq.uiautomator.AutomatorService;
import org.topq.uiautomator.Selector;

import com.android.ddmlib.logcat.LogCatMessage;
import com.ironsource.mobile.ADBConnection;
import com.ironsource.mobile.MobileSO;
import com.ironsource.mobile.RSCode;
import com.ironsource.mobile.reporers.ImageFlowHtmlReport;

public class OfferWallTests extends SystemTestCase4 {

	private MobileSO mobile;
	private MobileClient robotiumClient;
	private AutomatorService uiautomatorClient;
	private ADBConnection adb;
	private boolean clearAll = true;

	public static final String MCTESTER_ACTIVITY = "com.mobilecore.mctester.MainActivity";

	@Before
	public void prepareMoblileDevice() throws Exception {
		mobile = (MobileSO) system.getSystemObject("mobile");
		robotiumClient = (MobileClient) mobile.getRobotiumClient();
		uiautomatorClient = mobile.getUiAutomatorClient();
		adb = mobile.getAdbConnection();
		adb.clearLogcat();
		report.report("launch MCTester app");
		robotiumClient.launch(MCTESTER_ACTIVITY);
	}

	@Test
	@TestProperties(name = "2 OfferWall - Portrate Mode", paramsInclude = { "clearAll" })
	public void offerWall2Portrate() throws Exception {

		report.step("Click on 'Show stickee' button");
		robotiumClient.clickOnButtonWithText("Show stickee");
		Thread.sleep(1000);
		report.report("take screenshot");
		ReporterHelper.copyFileToReporterAndAddLink(report,
				mobile.capturescreenWithRobotium(), "screenshot : after click");

		report.step("Analayzing logcat reports");
		report.report("get logcat messages");
		List<LogCatMessage> messages = mobile.getFilterdMessages();

		report.report("parse logcat message to json objects");
		List<JSONObject> jsonReports = parseJsonReports(messages);

		report.step("verifying result...");
		verifyResult(jsonReports, RSCode.IMPRESSION);

	}
	
	@Test
	@TestProperties(name = "2 OfferWall - Portrate Mode uiautomator", paramsInclude = { "clearAll" })
	public void offerWall2PortrateUiautomator() throws Exception {

		ImageFlowHtmlReport imageFlowHtmlReport = new ImageFlowHtmlReport();
		imageFlowHtmlReport.addScaleButtonWidget();
		report.step("Click on 'Show stickee' button");
		Thread.sleep(1000);
		uiautomatorClient.click(new Selector().setText("Show (not force)"));
		
		File f1 = adb.getScreenshotWithAdb(null);

		imageFlowHtmlReport.addTitledImage("clicked on 'Show (not force)'", f1);
		
		File f2 = mobile.capturescreenWithRobotium();
		
		imageFlowHtmlReport.addTitledImage("clicked on 'Show (not force)' again", f2);
		
		report.report("screen flow", imageFlowHtmlReport.getHtmlReport(),Reporter.PASS,false,true,false,false);
		
		report.stopLevel();
		
		report.stopLevel();
		report.step("Analayzing logcat reports");
		report.report("get logcat messages");
		List<LogCatMessage> messages = mobile.getFilterdMessages();

		report.report("parse logcat message to json objects");
		List<JSONObject> jsonReports = parseJsonReports(messages);

		report.step("verifying result...");
		verifyResult(jsonReports, RSCode.INAPP);
		
		report.step("verifying result...");
		verifyResult(jsonReports, RSCode.IMPRESSION);

	}

//	@Test
//	@TestProperties(name = "2 OfferWall - Portrate Mode : close webview", paramsInclude = { "clearAll" })
//	public void offerWall2PortrateCloseWebview() throws Exception {
//
//		report.step("Click on'Show (not force) button'");
//		report.report("click on button'");
//		mobileClient.clickOnButtonWithText("Show stickee");
//		Thread.sleep(1000);
//		report.report("take screenshot");
//		ReporterHelper.copyFileToReporterAndAddLink(report,
//				mobile.capturescreen(), "screenshot");
//		// Selector[] selectors = new
//		// Selector().setIndex(0).getChildOrSiblingSelector();
//		int count = mobile.getUiAutomatorClient().count(
//				new Selector().setIndex(0));
//
//		mobileClient.clickOnHardwareButton(HardwareButtons.BACK);
//		ReporterHelper.copyFileToReporterAndAddLink(report,
//				mobile.capturescreen(), "screenshot");
//
//		report.step("Analayzing logcat reports");
//		report.report("get logcat messages");
//		List<LogCatMessage> messages = mobile.getFilterdMessages();
//
//		report.report("parse logcat message to json objects");
//		List<JSONObject> jsonReports = parseJsonReports(messages);
//
//		verifyResult(jsonReports, RSCode.IMPRESSION);
//		verifyResult(jsonReports, RSCode.BACK);
//
//	}

	private void verifyResult(List<JSONObject> reports, RSCode expectedCode)
			throws Exception {
		report.report("verifying result, expected: " + expectedCode.getRsCode());
		boolean isDExist = false;

		for (JSONObject jsonObject : reports) {
			String rs = null;
			rs = (String) jsonObject.get("RS");
			if (rs != null) {
				if (RSCode.convert(rs) == expectedCode) {
					isDExist = true;
					break;
				} else if (RSCode.convert(rs) == RSCode.ERROR) {
					report.report("ERROR: \"RS\"=\"E\" reported", Reporter.FAIL);
					ReporterHelper.copyFileToReporterAndAddLink(report,
							mobile.capturescreenWithRobotium(), "screenshot");
				}
			}
		}
		String expected = "\"RS\"=\"" + expectedCode.getRsCode() + "\"";
		if (isDExist) {
			report.report("Found " + expected);
		} else {
			report.report("Not found " + expected, Reporter.FAIL);
		}
	}

	private List<JSONObject> parseJsonReports(List<LogCatMessage> messages) {

		List<JSONObject> reports = new ArrayList<JSONObject>();
		for (LogCatMessage logCatMessage : messages) {

			String msg = logCatMessage.getMessage();
			String[] split = msg.split("\\{", 2);
			if (split.length != 2) {
				continue;
			}

			if (!split[1].contains("\"RS\"")) {
				continue;
			}

			String jsonString = "{" + split[1];
			JSONParser parser = new JSONParser();
			Object obj = null;
			try {
				obj = parser.parse(jsonString);
				reports.add((JSONObject) obj);
			} catch (ParseException e) {
				report.report("Failed Parsing Json", Reporter.WARNING);
			}

		}
		return reports;
	}

	@After
	public void tear() throws Exception {
		report.report("tearing down test");
		robotiumClient.finishOpenedActivities();
		robotiumClient.closeConnection();
	}

	public boolean isClearAll() {
		return clearAll;
	}

	public void setClearAll(boolean clearAll) {
		this.clearAll = clearAll;
	}

}
