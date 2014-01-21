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
	private static final String STYLE = "<style>a,p,h1,h2,h3,div,img " +
											"{" +
												"display: block" +
											"}"	+
												"p{color:red}" +
											"img{width:30%}" +
											"#scaleWidget {position:absolute;right:10px;top:10px;}" + 
											"#scaleWidget button{float:left;}" +
										"</style>";
	private String jqueryLocation;
	
	StringBuilder htmlBody;
	
	public ImageFlowHtmlReport() throws URISyntaxException {
		URL resourceUrl = getClass().getResource("/jquery.js");
		Path resourcePath = Paths.get(resourceUrl.toURI());
		File f = resourcePath.toFile();
		jqueryLocation = f.getAbsolutePath();
	
		htmlBody = new StringBuilder("<h3>Test Screenshot flow</h3>");
	}
	
	public void addTitledImage(String title, File imagefile) {
		htmlBody.append(BLANK_ROW);
		htmlBody.append(String.format(SIMPLE_TITLE_FORMAT, title));
		htmlBody.append(String.format(SIMPLE_IMG_FORMAT, imagefile.getAbsoluteFile()));
		htmlBody.append(BLANK_ROW);
	}
	
	public String getHtmlReport() {
		return "<html><head>" + STYLE + "<script src='"+ jqueryLocation +"'></script>" +"</head><body>" + htmlBody.toString() +"</body></html>";
	}
	
	public void addScaleButtonWidget() {
		String widgetScript = "<script>$(document).ready(function(){$('#plus').click(function(){ "+
				"console.log('clicked +');$('img').each(function(){$(this).width($(this).width() + 10)});});$('#minus').click(function(){"+
				"console.log('clicked -');$('img').each(function(){$(this).width($(this).width() - 10)});})});</script>";
		String scaleButtonWidget = "<div id=\"scaleWidget\"><button id='plus' type='button'>+</button><button id='minus' type='butto'>-</button></div>";
		htmlBody.append(widgetScript + scaleButtonWidget);
	}
}
