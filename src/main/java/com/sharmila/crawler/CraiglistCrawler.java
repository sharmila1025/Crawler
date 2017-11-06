package com.sharmila.crawler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.math3.random.RandomGenerator;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CraiglistCrawler extends Thread {

	Random randomizer = new Random();
	
	Map<String,String> freeProxyMap=new HashMap<>();
	List<Map<String,String>> mapList=new ArrayList<>();
	
	
	
	public List<Map<String,String>> loadMap(){
		
		freeProxyMap.put("61.91.235.226", "8080");
		freeProxyMap.put("185.119.57.121", "53281");
		freeProxyMap.put("103.41.122.14", "65103");
		freeProxyMap.put("195.208.128.117", "53281");
		freeProxyMap.put("14.139.248.17", "80");
		freeProxyMap.put("205.189.37.86", "53281");
		mapList.add(freeProxyMap);
		return mapList;
	}
	
	
	
	public void run() {
		
		
		System.setProperty("http.proxyHost", "205.189.37.86");
		System.setProperty("http.proxyPort","53281");
		
		Document document = null;
		Document doc = null;
		Elements elements=null;
		Document document1 = null;
		try {
			Response response = Jsoup.connect("https://geo.craigslist.org/iso/us").ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
					.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();

			doc = response.parse();

			Thread.sleep(11000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}

		Elements elementList = doc.select(".geo-site-list-container ul.height6.geo-site-list").select("a[href]");
	
		// Set<Element> siteList=new LinkedHashSet<>();
		List<String> siteList = new ArrayList<>();
		for (Element e : elementList) {

			
			siteList.add(e.attr("href"));

		}
		// can guess the class and loop over document---- but not done that way

		try {
			document = Jsoup.connect(siteList.get(4)).get();
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		Elements secondSite = document.select(".jobs #jjj .cats").select("a[href]");
		List<String> e2List = new ArrayList<>();
		for (Element e2 : secondSite) {

			e2List.add(e2.attr("href"));
			System.out.println(e2.attr("href"));
		}

		try {
			document1 = Jsoup.connect(siteList.get(4) + e2List.get(18)).get();
			
			System.out.println("The name of the sites "+(siteList.get(4) + e2List.get(18)));
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		// Document document1 =
		// Jsoup.connect("https://abilene.craigslist.org/search/acc").get();=

		Elements thirdSite = document1.select("#sortable-results ul.rows li");
		// System.out.println(":::::::::::"+secondSite);
		// System.out.println(" third site "+thirdSite);

		// adding result row in the map and doing operation on them

		Map<String, String> jobMap = new HashMap<>();

		// getting 7 days earlier date
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		System.out.println("date before a week " + cal.getTime());

		for (Element els : thirdSite) {
			
			jobMap.put(els.getElementsByTag("a").attr("href"), els.getElementsByTag("time").attr("datetime"));
		}

		for (Map.Entry<String, String> e : jobMap.entrySet()) {

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
			Date parsedDate;
			Document withWeekDoc = null;
			try {
				parsedDate = format.parse(e.getValue());
				if (parsedDate.after(cal.getTime()) | parsedDate.equals(cal.getTime())) {
					System.out.println(e.getKey() + " ---- with date ---" + e.getValue());

					if (!(e.getKey().startsWith("https://") | (e.getKey().startsWith("www")))) {
						
						try {
							System.out.println(" starts without prefix");
							withWeekDoc = Jsoup.connect(siteList.get(36)  + e.getKey()).get();
							
							System.out.println("  whole site "+(siteList.get(36) + e.getKey()));
							
						} catch (IOException e1) {
							withWeekDoc = Jsoup.connect(siteList.get(36)+e2List.get(18)  + e.getKey()).get();
							e1.printStackTrace();
						}
					} else {
						e2List.get(18);
						System.out.println(" starts with prefix");
						//with main domain + search category  and link
						withWeekDoc = Jsoup.connect(e.getKey()).get();
					}
					elements = withWeekDoc.select("#postingbody");
					
					//just checking
					
					if(elements.isEmpty()){
						System.out.println("elements is empty ");
					}
					for (Element el : elements) {
						System.out.println("///////" + el.ownText());

						Pattern pattern = Pattern.compile("(locations.*\\.)(.)");
						Pattern pattern2 = Pattern.compile("(call.*\\.)(.)");
						Matcher matcher = pattern.matcher(el.ownText().toLowerCase());
						Matcher matcher2 = pattern2.matcher(el.ownText().toLowerCase());
						if (matcher.find()) {

							System.out.println("Address " + matcher.group());

						}
						if (matcher2.find()) {
							System.out.println("phone number " + matcher2.group());
						}
					}
					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

				}
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
	}

	public static void main(String[] args) throws IOException, ParseException {
		CraiglistCrawler sc = new CraiglistCrawler();
		sc.start();

	}
}
