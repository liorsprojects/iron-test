package com.ironsource.mobile.test;

import jsystem.framework.TestProperties;
import junit.framework.SystemTestCase4;

import org.junit.Before;
import org.junit.Test;

import com.ironsource.mobile.FiddlerCli;

public class FiddlerOperations extends SystemTestCase4 {
	
	FiddlerCli fiddler;
	
	@Before
	public void init () throws Exception{
		fiddler = (FiddlerCli) system.getSystemObject("fiddler");
	}
	
	@Test
	public void liorisabitch() throws Exception{
		fiddler.startFiddler();
		sleep(10000);
		fiddler.stopFiddler();
	}
	
	@Test
	@TestProperties(name = "Start Fiddler Proxy")
	public void startFiddler() throws Exception{
		fiddler.startFiddler();
	}
	
	@Test
	@TestProperties(name = "Stop Fiddler Proxy")
	public void stopFiddler() throws Exception{
		fiddler.stopFiddler();
	}

}
