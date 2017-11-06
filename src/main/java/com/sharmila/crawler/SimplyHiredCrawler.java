package com.sharmila.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SimplyHiredCrawler extends Thread {

	public void run(){
		System.setProperty("http.proxyHost", "14.139.248.17");
		System.setProperty("http.proxyPort","80");
		
		String l="New York"+"%2C"+"NY";
		String q="software developer";
		try {
	
			Response response=Jsoup.connect("http://www.simplyhired.com").ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
					.data("from","homepage_searchbox")
					.data("q", q)
					.data("l", l)
					.data("action", "/search")
					.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();
			
			Document document=response.parse();
			
			Elements elements=document.select("*");
			List<String> siteList = new ArrayList<>();
			for (Element e : elements) {

				System.out.println(e);
				
			}
			//	Document document1 = Jsoup.connect("http://www.simplyhired.com"+siteList.get(0)).get();
//				Response response1=Jsoup.connect("http://www.simplyhired.com"+siteList.get(0)).ignoreContentType(true)
//						.userAgent(
//								"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
//						.referrer("http://www.google.com").followRedirects(true).execute();
//				
//				Document document1=response1.parse();
//				Elements elements1=document.select("#content").select(".jobs");
//				System.out.println(elements1);
//			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		SimplyHiredCrawler sHC=new SimplyHiredCrawler();
		sHC.start();
	}
}
