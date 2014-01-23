package com.ironsource.mobile.reporers;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageFlowHtmlReport {
	
	private static final String SIMPLE_TITLE_FORMAT = "<p>%s</p>";
	private static final String SIMPLE_IMG_FORMAT = "<img src=\"%s\"/>";
	private static final String BLANK_ROW = "<br/>";
	private String jqueryLocation;
	private String widgetLocation;
	private String cssLocation;
	
	
	StringBuilder htmlBody;
	
	public ImageFlowHtmlReport() throws URISyntaxException {
		URL resourceUrl = getClass().getResource("/jquery.js");
		Path resourcePath = Paths.get(resourceUrl.toURI());
		File f = resourcePath.toFile();
		jqueryLocation = f.getAbsolutePath();
		
		resourceUrl = getClass().getResource("/widgets.js");
		resourcePath = Paths.get(resourceUrl.toURI());
		widgetLocation = resourcePath.toFile().getAbsolutePath();
		
		resourceUrl = getClass().getResource("/widgets.css");
		resourcePath = Paths.get(resourceUrl.toURI());
		cssLocation = resourcePath.toFile().getAbsolutePath();
		
		htmlBody = new StringBuilder("<h3>Test Screenshot flow</h3>");
	}
	
	public void addTitledImage(String title, File imagefile) {
		htmlBody.append(BLANK_ROW);
		htmlBody.append(String.format(SIMPLE_TITLE_FORMAT, title));
		htmlBody.append(String.format(SIMPLE_IMG_FORMAT, imagefile.getAbsoluteFile()));
		htmlBody.append(BLANK_ROW);
	}
	
	public String getHtmlReport() {
		return "<html><head>" + "<link type=\"text/css\" href=\""+ cssLocation +"\" rel=\"stylesheet\">" +
				"<script src='" + jqueryLocation +"'></script>" +
				"<script src='" + widgetLocation +"'></script>" +
				"</head><body>" + htmlBody.toString() +"</body></html>";
	}
	
	public void addScaleButtonWidget() {
		
		String scaleButtonWidget = "<div id=\"scaleWidget\"><button id='plus' type='button'>+</button><button id='minus' type='butto'>-</button></div>";
		htmlBody.append(scaleButtonWidget);
	}
}
