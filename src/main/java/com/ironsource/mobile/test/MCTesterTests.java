package com.ironsource.mobile.test;

import il.co.topq.mobile.client.impl.MobileClient;
import il.co.topq.mobile.client.impl.WebElement;

import java.util.ArrayList;
import java.util.List;

import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import junit.framework.SystemTestCase4;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.topq.uiautomator.AutomatorService;
import org.topq.uiautomator.Selector;

import com.android.ddmlib.logcat.LogCatMessage;
import com.ironsource.mobile.ADBConnection;
import com.ironsource.mobile.FlowCode;
import com.ironsource.mobile.LogcatHelper;
import com.ironsource.mobile.MobileCoreMsgCode;
import com.ironsource.mobile.MobileSO;
import com.ironsource.mobile.RSCode;
import com.ironsource.mobile.reporters.ImageFlowHtmlReport;

public class MCTesterTests extends SystemTestCase4 {

	private static MobileSO mobile;
	private static ADBConnection adb;
	private static AutomatorService uiautomatorClient;
	private static MobileClient robotiumClient;
	private static List<WebElement> offerwallElemens;
	ImageFlowHtmlReport flowHtmlReport;
	
	public static final String MCTESTER_ACTIVITY = "com.mobilecore.mctester.MainActivity";

	private int logcatReportTimeout = 30000;
	
	
	@Before
	public void setup() throws Exception{
		flowHtmlReport = new ImageFlowHtmlReport();
		if(adb != null) {
			report.report("clearing logcat");
			adb.clearLogcat();
		}
	}

	/**
	 * 1. initialize all clients and servers.
	 * 2. launching the MCtester application.
	 * 3. click on button 'Show if ready' to show offerwall.
	 * 4. get all the webelements of the offerwall.
	 * 5. close all robotium related services and activities.  
	 * 
	 * @throws Exception
	 */
	@Test
	@TestProperties(name = "capture test template", paramsInclude = {})
	public void setupTestsTemplate() throws Exception {
		
		report.report("init mobile system object");
		mobile = (MobileSO) system.getSystemObject("mobile");
			
		report.report("retrieving robotium client");
		robotiumClient = (MobileClient) mobile.getRobotiumClient();
		
		report.report("retrieving uiautomator client");
		uiautomatorClient = mobile.getUiAutomatorClient();
		
		report.report("retrieving adb connection");
		adb = mobile.getAdbConnection();
	
		
		offerwallElemens = new ArrayList<WebElement>();
		
		captureWebview();		
	}

	
	/**
	 * Test close button of the offerwall
	 * 
	 * 1. open MCTester.
	 * 2. click on 'Show (not force)' button to show offerwall.
	 * 3. click on {X} button in the corner of the offerwall.
	 * 4. verify that report is submitted with RS Code Close ('-').
	 * 
	 * @throws Exception
	 */
	@Test
	@TestProperties(name = "close offerwall with the {X} button", paramsInclude = {"logcatReportTimeout"})
	public void closeOfferwallWithX() throws Exception {
		
		flowHtmlReport.addTitledImage("Before MCTester Launch", adb.getScreenshotWithAdb(null));
		uiautomatorClient.click(new Selector().setDescription("Apps").setClassName("android.widget.TextView"));
		Thread.sleep(1500);
		
		uiautomatorClient.click(new Selector().setText("MCTester").setClassName("android.widget.TextView"));
		
		if(!uiautomatorClient.waitForExists(new Selector().setText("Show (not force)"), 50000)) {
			throw new Exception("Application MCTester did not launched");
		} 
		flowHtmlReport.addTitledImage("In App", adb.getScreenshotWithAdb(null));
		
		report.step("MCTester Launched");
		
		uiautomatorClient.click(new Selector().setText("Show (not force)"));
		report.report("wait for transiton of webview to complete");
		
		waitForRSCode(RSCode.WALL, FlowCode.OFFERWALL,logcatReportTimeout);
		waitForRSCode(RSCode.IMPRESSION, FlowCode.OFFERWALL, logcatReportTimeout);
		
		flowHtmlReport.addTitledImage("Clicked on 'Show (not force)'", adb.getScreenshotWithAdb(null));
		boolean elementFound = false;
		for (WebElement element : offerwallElemens) {
			if("noThanks".equals(element.getId())) {
				report.report("found element with id = 'noThanks' and about to click on it");
				uiautomatorClient.click(element.getX(), element.getY());
				report.step("clicked on {x} button");
				elementFound = true;
				break;
			}
		}
		if(!elementFound) {
			throw new Exception("Element with id = 'noThanks' wasent found");
		}
		waitForRSCode(RSCode.CLOSE, FlowCode.OFFERWALL, logcatReportTimeout);
		flowHtmlReport.addTitledImage("After click on {X}", adb.getScreenshotWithAdb(null));
		report.report("screen flow", flowHtmlReport.getHtmlReport(), Reporter.PASS, false, true, false, false);
		clearRecentApps();
	}
	
	
	
	/**
	 * Test full flow from click to install
	 * 
	 * 1. open MCTester.
	 * 2. click on 'Show (not force)' button to show offerwall.
	 * 3. verify that report is submitted with RS Code Wall ('W') and RS Code Impression ('D').
	 * 4. click on one of the items represent an application and redirect to the play store.
	 * 5. verify that report is submitted with RS Code Click ('C').
	 * 6. wait for play store to launch.
	 * 8. verify that report is submitted with RS Code Report ('S').
	 * 7. download and install the application and wait for completion.
	 * 8. verify that report is submitted with RS Code Install ('+'). 
	 * 
	 * @throws Exception
	 */
	@Test
	@TestProperties(name = "full click to download flow", paramsInclude = {"logcatReportTimeout"})
	public void fullClickToDownloadFlow() throws Exception {
		
		flowHtmlReport.addTitledImage("Before MCTester Launch", adb.getScreenshotWithAdb(null));
		
		uiautomatorClient.pressKey("home");
		uiautomatorClient.click(new Selector().setDescription("Apps").setClassName("android.widget.TextView"));
		Thread.sleep(1500);
		
		uiautomatorClient.click(new Selector().setText("MCTester").setClassName("android.widget.TextView"));
		
		if(!uiautomatorClient.waitForExists(new Selector().setText("Show (not force)"), 50000)) {
			throw new Exception("Application MCTester did not launched");
		} 
		flowHtmlReport.addTitledImage("In App", adb.getScreenshotWithAdb(null));
		
		report.step("MCTester Launched");
		
		waitForManagerMessageToContain(MobileCoreMsgCode.OFFERWALL_MANAGER, "from:LOADING , to:READY_TO_SHOW" , 15000);
		
		uiautomatorClient.click(new Selector().setText("Show (not force)"));
		report.report("wait for transiton of webview to complete");
		waitForRSCode(RSCode.WALL, FlowCode.OFFERWALL, 600000);
		waitForRSCode(RSCode.IMPRESSION, FlowCode.OFFERWALL, logcatReportTimeout);
		
		flowHtmlReport.addTitledImage("Clicked on 'Show (not force)'", adb.getScreenshotWithAdb(null));
		
		boolean elementFound = false;
		for (WebElement element : offerwallElemens) {
			if("stars".equals(element.getClassName())) {
				report.report("found element with className = 'stars' and about to click on it");
				uiautomatorClient.click(element.getX(), element.getY());
				report.step("clicked on 'stars' of the first app");
				elementFound = true;
				break;
			}
		}
		if(!elementFound) {
			throw new Exception("Element with className = 'stars' wasen't found");
		}
		waitForRSCode(RSCode.CLICK, FlowCode.OFFERWALL, 10000);
		
		flowHtmlReport.addTitledImage("After click on application in offrewall", adb.getScreenshotWithAdb(null));
		
		report.step("waiting for playstore");
		if (!uiautomatorClient.waitForExists(new Selector().setText("INSTALL"), 5000)) {
		    throw new Exception("Did not navigated to Playstore (check internet connection");
		} 
		
		flowHtmlReport.addTitledImage("Playstore", adb.getScreenshotWithAdb(null));
		Thread.sleep(2000);
		report.report("click INSTALL");
		uiautomatorClient.click(new Selector().setText("INSTALL"));

		if (!uiautomatorClient.waitForExists(new Selector().setText("ACCEPT"), 5000)) {
			throw new Exception("Accept page not visible");
		} 
		flowHtmlReport.addTitledImage("After click INSTALL", adb.getScreenshotWithAdb(null));

		report.report("click ACCEPT");
		uiautomatorClient.click(new Selector().setText("ACCEPT"));
		
		if (!uiautomatorClient.waitForExists(new Selector().setClassName("android.widget.ProgressBar"), 10000)) {
			throw new Exception("Installing not started");
			
		} 
		report.step("installing in progress...");
		flowHtmlReport.addTitledImage("while installing after accept", adb.getScreenshotWithAdb(null));

		report.report("waiting for install to finish");
		
		if (!uiautomatorClient.waitForExists(new Selector().setText("OPEN"), 600000)) {
			throw new Exception("Did not finish downloading after 6 minutes");
		}  
		report.step("Install Completed");
		Thread.sleep(2000);
		flowHtmlReport.addTitledImage("App Installed", adb.getScreenshotWithAdb(null));
		waitForRSCode(RSCode.INSATLL, FlowCode.OFFERWALL, 600000);
		report.report("screen flow", flowHtmlReport.getHtmlReport(), Reporter.PASS, false, true, false, false);
		clearRecentApps();
	}
	
	/**
	 * Test close offerwall using hardware button 'back'
	 * 
	 * 1. open MCTester.
	 * 2. click on 'Show (not force)' button to show offerwall.
	 * 3. verify that report is submitted with RS Code Wall ('W') and RS Code Impression ('D').
	 * 4. press on hardware button 'back'.
	 * 5. verify that report is submitted with RS Code Click ('Q').
	 * 
	 * @throws Exception
	 */
	@Test
	@TestProperties(name = "Test close offerwall using hardware button 'back'", paramsInclude = {"logcatReportTimeout"})
	public void closeApplicationUsingBackButton() throws Exception {
		
		flowHtmlReport.addTitledImage("Before MCTester Launch", adb.getScreenshotWithAdb(null));
		uiautomatorClient.click(new Selector().setDescription("Apps").setClassName("android.widget.TextView"));
		Thread.sleep(1500);
		
		uiautomatorClient.click(new Selector().setText("MCTester").setClassName("android.widget.TextView"));
		
		if(!uiautomatorClient.waitForExists(new Selector().setText("Show (not force)"), 50000)) {
			throw new Exception("Application MCTester did not launched");
		} 
		flowHtmlReport.addTitledImage("In App", adb.getScreenshotWithAdb(null));
		
		report.step("MCTester Launched");
		
		uiautomatorClient.click(new Selector().setText("Show (not force)"));
		report.report("wait for transiton of webview to complete");
		waitForRSCode(RSCode.WALL, FlowCode.OFFERWALL, logcatReportTimeout);
		waitForRSCode(RSCode.IMPRESSION, FlowCode.OFFERWALL, logcatReportTimeout);
		
		flowHtmlReport.addTitledImage("Clicked on 'Show (not force)'", adb.getScreenshotWithAdb(null));
		
		report.report("about to press 'back' button");
		uiautomatorClient.pressKey("back");
		report.step("pressed back button");
		
		if(!uiautomatorClient.waitUntilGone(new Selector().setClassName("android.webkit.WebView"),5000)) {
			throw new Exception("The offerwall did'nt vanish after 5 seconds since 'back' pressed");
		}
		waitForRSCode(RSCode.BACK, FlowCode.OFFERWALL, logcatReportTimeout);
	}
	
	
	private void captureWebview () throws Exception {
		report.report("launch MCTester App");
		robotiumClient.launch(MCTESTER_ACTIVITY);
		waitForManagerMessageToContain(MobileCoreMsgCode.OFFERWALL_MANAGER, "from:LOADING , to:READY_TO_SHOW" , 15000);
		robotiumClient.clickOnButtonWithText("Show if ready");
		Thread.sleep(8000);
		
		report.report("gather all offerwall elements");
		offerwallElemens = robotiumClient.getCurrentWebElements();
		if(offerwallElemens.size() == 0) {
			throw new Exception("could not capture offerwall elements");
		} else {
			report.startLevel("Offerwall Elements");
			StringBuffer sb = new StringBuffer("OfferWall Elements").append("\n")
					.append("============================================").append("\n");
			for (WebElement element : offerwallElemens) {
				sb.append(element.toString());
			}
			report.report(sb.toString());
			report.stopLevel();
		}
		report.report("about to close MCTester and Robotium Server");
		robotiumClient.finishOpenedActivities();
		robotiumClient.closeConnection();
		adb.stopActivity(MCTESTER_ACTIVITY);
		
		clearRecentApps();

	}
	
	/**
	 * clear open activities
	 * 
	 * 1. press home button.
	 * 2. press recent activities button.
	 * 3. try to swipe out all open activities.
	 * 4. press home button.
	 * 
	 * @throws Exception
	 * 
	 */
	//TODO - each rom implemented the recent activity screen in a different way.
	private void clearRecentApps() throws Exception {
		uiautomatorClient.pressKey("home");
		Thread.sleep(1000);
		uiautomatorClient.pressKey("recent");
		Thread.sleep(1000);
		report.step("about to close all open apps");
		List<Selector> recentSelectors = new ArrayList<Selector>();
		recentSelectors.add(new Selector().setText("Robotium Server"));
		recentSelectors.add(new Selector().setText("MCTester"));
		recentSelectors.add(new Selector().setText("Play Store"));
		
		for (Selector selector : recentSelectors) {
			if(uiautomatorClient.waitForExists(selector, 1000)) {
				uiautomatorClient.swipe(selector, "r", 5);
				report.report("closed " + selector.getText() + "app");
			}
			Thread.sleep(1000);		
		}
		report.step("no more apps to close");
		
		uiautomatorClient.pressKey("home");
		

	}
	
	@After
	public void tearDown() throws Exception{
		if (!isPass()){
			flowHtmlReport.addTitledImage("Failed Here", adb.getScreenshotWithAdb(null));
			report.report("screen flow", flowHtmlReport.getHtmlReport(), Reporter.PASS, false, true, false, false);
		}
	}
	
	
	private void waitForManagerMessageToContain(MobileCoreMsgCode code, String msg, int timeout) throws Exception {
		long now = System.currentTimeMillis();
		boolean exist = false;
		report.report("waiting for " + code.toString() + " to preduce message : " + msg);
		List<LogCatMessage> messages;
		while (!exist) {
			if (System.currentTimeMillis() - now > timeout) {
				throw new Exception(code.toString() + " Did not preduce message that contains '" + msg + "' after: " + timeout + " millis");
			}

			messages = adb.getMobileCoreLogcatMessages(code);
			for (LogCatMessage logCatMessage : messages) {
				String message = logCatMessage.getMessage();
				if (message.contains(msg)) {
					report.step(code.toString() + " preduced message contains '" + msg + "'");
					exist = true;
					break;
				}
			}
			Thread.sleep(1000);
		}
	}
	
	private void waitForRSCode(RSCode rsCode, FlowCode flowCode, int timeout) throws Exception {
		long now = System.currentTimeMillis();
		boolean exist = false;
		report.report("waiting for RS Code '"+ rsCode.getRsCode() +"' for Flow " + flowCode.getFlowCode());
		List<LogCatMessage> messages;
		String lookString = "\"Flow\":\"" + flowCode.getFlowCode() + "\"";
		report.report("looking for :  " + lookString);
		while (!exist) {
			if (System.currentTimeMillis() - now > timeout) {
				throw new Exception("Did not find expected RS code: " + rsCode.getRsCode() + " after: " + timeout + " millis");
			}
			messages = adb.getMobileCoreLogcatMessages(MobileCoreMsgCode.RS);
			for (LogCatMessage logCatMessage : messages) {
				String msg = logCatMessage.getMessage();
				
				
				if (msg.contains("\"RS\":\"" + rsCode.getRsCode() + "\"") && msg.contains("\"Flow\":\"" + flowCode.getFlowCode() + "\"")) {
					report.step("Found RS Code: " + rsCode.getRsCode());
					exist = true;
				}
				if (logCatMessage.getMessage().contains("\"RS\":\"E\"")) {
					JSONObject jsonMsg = LogcatHelper.extractMsgAsJson(logCatMessage.getMessage());
					String errorMsg = (String) jsonMsg.get("Err");
					report.report("Error: Found RS Code: E while waiting for " + rsCode.getRsCode(), Reporter.FAIL);
					
					throw new Exception("Error message: " + errorMsg);
				}
			}
			Thread.sleep(1000);
		}
	}
	
	
	public int getLogcatReportTimeout() {
		return logcatReportTimeout;
	}

	@ParameterProperties(description = "timeout in milliseconds to wait for RS code")
	public void setLogcatReportTimeout(int logcatReportTimeout) {
		this.logcatReportTimeout = logcatReportTimeout;
	}
}
