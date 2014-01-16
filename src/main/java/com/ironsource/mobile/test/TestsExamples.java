//package com.ironsource.mobile.test;
//
//import il.co.topq.mobile.client.impl.MobileClient;
//import il.co.topq.mobile.common.datamodel.CommandResponse;
//
//import java.io.File;
//import java.util.List;
//
//import jsystem.framework.TestProperties;
//import junit.framework.SystemTestCase4;
//
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.android.ddmlib.logcat.LogCatMessage;
//import com.ironsource.mobile.LogcatHelper;
//import com.ironsource.mobile.MobileSO;
//
//
//
//public class TestsExamples extends SystemTestCase4 {
//
//	
//	private MobileSO mobile;
//	private MobileClient mobileClient;
//	
//	
//	
//	@Before
//	public void init() throws Exception {
//		report.step("Launch MCTester Application");
//		mobile = (MobileSO) system.getSystemObject("mobile");
//		mobileClient = (MobileClient) mobile.getMobileClient();
//		
//	}
//	
//	
//	@Test
//	@TestProperties(name = "Click And Capture" ,paramsInclude = { "" })
//	public void clickAndCaptureTest() throws Exception {
//		CommandResponse res = mobileClient.getSystemTime();
//		String time = res.getResponse();
//		report.report("System Time: " + time);
//		mobile.clearLogcat();
//		Thread.sleep(3000);
//		mobileClient.clickOnButton(0);
//		List<LogCatMessage> messages = mobile.getFilterdMessages();
//		report.report("Message count: " + messages.size());
//		
//		for (LogCatMessage logCatMessage : messages) {
//			report.report("===ORIGINAL===" + logCatMessage.toString());
//			String msg = logCatMessage.getMessage();
//			report.report("\nThe Message\n"+msg+"\n");
//			String[] split = msg.split("\\{", 2);
//			if(split.length != 2) {
//				continue;
//			}
//			
//			if(!split[1].contains("\"RS\"")) {
//				continue;
//			}
//			
//			
//			String jsonString  = "{" + split[1];
//			
//			JSONParser parser = new JSONParser();
//			Object obj = parser.parse(jsonString);
//			JSONObject jsonObject = (JSONObject)obj;
//			String rs = (String) jsonObject.get("RS");
//			report.report("\n\n" + rs+"\n\n");
//		}
////			JSONArray offersArray = (JSONArray)jsonObject.get("Offers");
////			for (int i = 0; i < offersArray.size(); i++) {
////				JSONObject objApp = (JSONObject)offersArray.get(i);
////				String name = (String)objApp.get("Name");
////				report.report("\nApp " + i + " Name is: " + name);
////			}
//		
//		
//		//LogcatHelper.extractMsgAsJson(messages);
//		File f = mobileClient.takeScreenshot();
//		report.report(f.getAbsolutePath());
//		
//	}
//	
//	
//	
//	@After
//	public void tear() throws Exception {
//		mobileClient.finishOpenedActivities();
//		mobileClient.closeConnection();
//	}
//	
//
//	
//}
