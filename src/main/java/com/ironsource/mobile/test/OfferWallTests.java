package com.ironsource.mobile.test;

import il.co.topq.mobile.client.impl.MobileClient;
import il.co.topq.mobile.client.impl.WebElement;

import java.util.ArrayList;
import java.util.List;

import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
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
	
	private int x = 0;
	private int y = 0;
	private boolean clearAll = true;

	

	public static final String MCTESTER_ACTIVITY = "com.mobilecore.mctester.MainActivity";

	@Before
	public void prepareMoblileDevice() throws Exception {
		mobile = (MobileSO) system.getSystemObject("mobile");
		//robotiumClient = (MobileClient) mobile.getRobotiumClient();
		uiautomatorClient = mobile.getUiAutomatorClient();
		adb = mobile.getAdbConnection();
		adb.clearLogcat();
		
		report.report("launch MCTester app");
		//robotiumClient.launch(MCTESTER_ACTIVITY);
		Thread.sleep(2000);
	
		uiautomatorClient.pressKey("home");
		uiautomatorClient.click(new Selector().setDescription("Apps").setClassName("android.widget.TextView"));	
		uiautomatorClient.click(new Selector().setText("MCTester").setClassName("android.widget.TextView"));
		
		if(!clearAll) {
			return;
		}
		uiautomatorClient.click(new Selector().setText("Clear all"));
		uiautomatorClient.pressKey("home");
		uiautomatorClient.click(new Selector().setDescription("Apps").setClassName("android.widget.TextView"));
		uiautomatorClient.click(new Selector().setText("MCTester").setClassName("android.widget.TextView"));
		
	}


	@Test
	@TestProperties(name = "2 OfferWall - uiautomator only", paramsInclude = { "x, y, clearAll" })
	public void offerWall2Portrate() throws Exception {

		
		ImageFlowHtmlReport imageFlowHtmlReport = new ImageFlowHtmlReport();
		imageFlowHtmlReport.addScaleButtonWidget();
		imageFlowHtmlReport.addTitledImage("Before App Launch", adb.getScreenshotWithAdb(null));
		
		report.step("waiting for MCTester to load");
		boolean inApp = uiautomatorClient.waitForExists(new Selector().setText("Show stickee"), 5000);
		if(inApp) {
			report.report("Application started");
		} else {
			report.report("screen flow",imageFlowHtmlReport.getHtmlReport(),Reporter.PASS, false, true, false,false);
			throw new Exception("The app did not launch");
		}
		
		imageFlowHtmlReport.addTitledImage("In App", adb.getScreenshotWithAdb(null));
		uiautomatorClient.click(new Selector().setText("Show (not force)"));
		Thread.sleep(2000);
		imageFlowHtmlReport.addTitledImage("After show (not force)", adb.getScreenshotWithAdb(null));
	
		waitForRSCode(RSCode.INAPP, 5000);
		waitForRSCode(RSCode.IMPRESSION, 5000);
		
		report.step("Click on one of the apps");
		uiautomatorClient.click(368,500);
		waitForRSCode(RSCode.CLICK, 10000);
		imageFlowHtmlReport.addTitledImage("After click On app", adb.getScreenshotWithAdb(null));
		
		report.step("waiting for playstore");
		boolean inPlayStore = uiautomatorClient.waitForExists(new Selector().setText("INSTALL"), 5000);
		if(inPlayStore) {
			report.step("In Playstore");
			
		} else {
			report.report("screen flow",imageFlowHtmlReport.getHtmlReport(),Reporter.PASS, false, true, false,false);
			throw new Exception("Did not navigated to Playstore (check internet connection");
		}
		imageFlowHtmlReport.addTitledImage("Playstore", adb.getScreenshotWithAdb(null));
		Thread.sleep(2000);
		report.report("click INSTALL");
		uiautomatorClient.click(new Selector().setText("INSTALL"));
		
		boolean acceptVisible = uiautomatorClient.waitForExists(new Selector().setText("ACCEPT"), 5000);
		if(acceptVisible) {
			report.step("Accept page visible");
		} else {
			report.report("screen flow",imageFlowHtmlReport.getHtmlReport(),Reporter.PASS, false, true, false,false);
			throw new Exception("Accept page not visible");
		}
		imageFlowHtmlReport.addTitledImage("After click INSTALL", adb.getScreenshotWithAdb(null));
		
		report.report("click ACCEPT");		
		uiautomatorClient.click(new Selector().setText("ACCEPT"));
		boolean installStarted = uiautomatorClient.waitForExists(new Selector().setClassName("android.widget.ProgressBar"), 10000);
		if(installStarted) {
			report.step("installing in progress...");
			imageFlowHtmlReport.addTitledImage("while installing after accept", adb.getScreenshotWithAdb(null));
		} else {
			report.report("screen flow",imageFlowHtmlReport.getHtmlReport(),Reporter.PASS, false, true, false,false);
			throw new Exception("Installing not started");
		}

		report.step("waiting for install to finish");
		boolean openButton = uiautomatorClient.waitForExists(new Selector().setText("OPEN"), 600000);
		if(openButton) {
			report.step("Install Completed");
		} else {
			report.report("screen flow",imageFlowHtmlReport.getHtmlReport(),Reporter.PASS, false, true, false,false);
			throw new Exception("Did not finish downloading after 6 minutes");
		}
		Thread.sleep(2000);
		imageFlowHtmlReport.addTitledImage("App Installed", adb.getScreenshotWithAdb(null));
		
		waitForRSCode(RSCode.INSATLL, 600000);
		
		report.report("screen flow",imageFlowHtmlReport.getHtmlReport(),Reporter.PASS, false, true, false,false);

	}

	private void waitForRSCode(RSCode rsCode, int timeout) throws Exception {
		long now = System.currentTimeMillis();
		boolean exist = false;
		List<LogCatMessage> messages;
		while(!exist) {
			if (System.currentTimeMillis() - now > timeout) {
				throw new Exception("Did not find expected RS code: " + rsCode.getRsCode() + " after: " + timeout + " millis");
			}
	
			messages = adb.getMobileCoreLogcatMessages();
			for (LogCatMessage logCatMessage : messages) {
				if(logCatMessage.getMessage().contains("\"RS\":\"" + rsCode.getRsCode() + "\"")) {
					report.report("Found RS Code: " + rsCode.getRsCode());
					exist = true;
				} 
				if(logCatMessage.getMessage().contains("\"RS\":\"E\"")) {
					throw new Exception("Error: Found RS Code: E while waiting for " + rsCode.getRsCode());
				} 
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//ignore
			}
		}
		
	}


	@Test
	@TestProperties(name = "2 OfferWall - Portrate Mode uiautomator", paramsInclude = { "clearAll" })
	public void offerWall2PortrateUiautomator() throws Exception {
		ImageFlowHtmlReport imageFlowHtmlReport = new ImageFlowHtmlReport();
		imageFlowHtmlReport.addScaleButtonWidget();
		
		Thread.sleep(2000);
		imageFlowHtmlReport.addTitledImage("In App", robotiumClient.takeScreenshot());
		
		report.step("Click on 'Show (not force)'");
		uiautomatorClient.click(new Selector().setText("Show (not force)"));
		report.report("wait for javascript to be loaded");
		Thread.sleep(6000);
		
		imageFlowHtmlReport.addTitledImage("After show offerwall", robotiumClient.takeScreenshot());
		
		List<WebElement> elements = robotiumClient.getCurrentWebElements();
		report.report("Closing robotium connection");
		robotiumClient.closeConnection();
		report.startLevel("element list");
		boolean click = false;
		uiautomatorClient.setOrientation("r");
		uiautomatorClient.setOrientation("l");
		for (WebElement webElement : elements) {
			report.report("\n===ELEMENT===");
			report.report("tag: " + webElement.getTag());
			report.report("id: " + webElement.getId());
			report.report("class: " + webElement.getClassName());
			report.report("text:" + webElement.getText());
			report.report("X: " + String.valueOf(webElement.getX()));
			report.report("Y: " + String.valueOf(webElement.getY()));
			if(!("noThanks".equals(webElement.getId())) && !("HTML".equals(webElement.getTag())) &&
					!("BODY".equals(webElement.getTag()))) {
				boolean inPlayStore = false;
				report.report("about to press on x=" + (webElement.getX()+10) + "y=" + (webElement.getY()+10));
				click = uiautomatorClient.click(webElement.getX(), webElement.getY());
				if(click) report.report("CLICKED");
				else { report.report("NOT CLICKED");}
				Thread.sleep(1000);
				inPlayStore = uiautomatorClient.waitForExists(new Selector().setText("Apps"), 5000);
				if(inPlayStore) {
					break;
				}
				
			}
		}
		uiautomatorClient.setOrientation("n");
		report.stopLevel();
		
		
		imageFlowHtmlReport.addTitledImage("after click on app", adb.getScreenshotWithAdb(null));
		
		report.report("screen flow",imageFlowHtmlReport.getHtmlReport(),Reporter.PASS, false, true, false,false);
		
		report.step("Analayzing logcat reports");
		report.report("get logcat messages");
		List<LogCatMessage> messages = mobile.getFilterdMessages();

		report.report("parse logcat message to json objects");
		List<JSONObject> jsonReports = parseJsonReports(messages);

		report.step("verifying result...");
		verifyResult(jsonReports, RSCode.INAPP);

		report.step("verifying result...");
		verifyResult(jsonReports, RSCode.IMPRESSION);
		
		report.step("verifying result...");
		verifyResult(jsonReports, RSCode.CLICK);
		
		
		
		
		// report.step(res.getResponse());
		// JSONParser jp = new JSONParser();
		// JSONObject obj = (JSONObject) jp.parse(res.getResponse());
		// uiautomatorClient.click((Integer)obj.get("x"),(Integer)obj.get("y"));
		// //robotiumClient.clickOnHardwareButton(HardwareButtons.BACK);
		// Thread.sleep(5000);

		// File f1 = adb.getScreenshotWithAdb(null);
		//
		// imageFlowHtmlReport.addTitledImage("clicked on 'Show (not force)'",
		// f1);

		// Thread.sleep(2000);
		// File f2 = adb.getScreenshotWithAdb(null);
		// imageFlowHtmlReport.addTitledImage("clicked on 'Show (not force)' again",
		// f2);

		// File f2 = mobile.capturescreenWithRobotium();
		//

		// report.stopLevel();
		//
		// CommandResponse response =
		// robotiumClient.testingClickWebElement("className", "stars");
		// report.report("WebElements",response.getResponse(), true);
		//
		// Thread.sleep(2000);
		// File f3 = adb.getScreenshotWithAdb(null);
		//
		// imageFlowHtmlReport.addTitledImage("clicked on 'Show (not force)' -> play store?",
		// f3);
		//
		// report.report("screen flow",
		// imageFlowHtmlReport.getHtmlReport(),Reporter.PASS,false,true,false,false);
		//
		//
		// report.step("Analayzing logcat reports");
		// report.report("get logcat messages");
		// List<LogCatMessage> messages = mobile.getFilterdMessages();
		//
		// report.report("parse logcat message to json objects");
		// List<JSONObject> jsonReports = parseJsonReports(messages);
		//
		// report.step("verifying result...");
		// verifyResult(jsonReports, RSCode.INAPP);
		//
		// report.step("verifying result...");
		// verifyResult(jsonReports, RSCode.IMPRESSION);

	}

	// @Test
	// @TestProperties(name = "2 OfferWall - Portrate Mode : close webview",
	// paramsInclude = { "clearAll" })
	// public void offerWall2PortrateCloseWebview() throws Exception {
	//
	// report.step("Click on'Show (not force) button'");
	// report.report("click on button'");
	// mobileClient.clickOnButtonWithText("Show stickee");
	// Thread.sleep(1000);
	// report.report("take screenshot");
	// ReporterHelper.copyFileToReporterAndAddLink(report,
	// mobile.capturescreen(), "screenshot");
	// // Selector[] selectors = new
	// // Selector().setIndex(0).getChildOrSiblingSelector();
	// int count = mobile.getUiAutomatorClient().count(
	// new Selector().setIndex(0));
	//
	// mobileClient.clickOnHardwareButton(HardwareButtons.BACK);
	// ReporterHelper.copyFileToReporterAndAddLink(report,
	// mobile.capturescreen(), "screenshot");
	//
	// report.step("Analayzing logcat reports");
	// report.report("get logcat messages");
	// List<LogCatMessage> messages = mobile.getFilterdMessages();
	//
	// report.report("parse logcat message to json objects");
	// List<JSONObject> jsonReports = parseJsonReports(messages);
	//
	// verifyResult(jsonReports, RSCode.IMPRESSION);
	// verifyResult(jsonReports, RSCode.BACK);
	//
	// }
	
	

	private void verifyResult(List<JSONObject> reports, RSCode expectedCode)
			throws Exception {
		report.report("verifying result, expected: " + expectedCode.getRsCode());
		boolean isRSCodeExist = false;

		for (JSONObject jsonObject : reports) {
			String rs = null;
			rs = (String) jsonObject.get("RS");
			if (rs != null) {
				if (RSCode.convert(rs) == expectedCode) {
					isRSCodeExist = true;
					break;
				} else if (RSCode.convert(rs) == RSCode.ERROR) {
					report.report("ERROR: \"RS\"=\"E\" reported", Reporter.FAIL);
					ReporterHelper.copyFileToReporterAndAddLink(report,
							mobile.capturescreenWithRobotium(), "screenshot");
				}
			}
		}
		String expected = "\"RS\"=\"" + expectedCode.getRsCode() + "\"";
		if (isRSCodeExist) {
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
		//robotiumClient.finishOpenedActivities();
		//robotiumClient.closeConnection();
	}

	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public boolean isClearAll() {
		return clearAll;
	}

	public void setClearAll(boolean clearAll) {
		this.clearAll = clearAll;
	}
	

}
