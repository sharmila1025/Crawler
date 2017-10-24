package com.sharmila.scrapper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sharmila.scrapper.domain.Glassdoor;


public class MonsterCrawler extends Thread {
	Glassdoor jobSummary=new Glassdoor ();
	Map<String, Date> jobMap = new HashMap<>();

	Map<String, Glassdoor> jobSummaryMap = new HashMap<>();

	public void run() {
		// "195.208.128.117", "53281"
		// 205.189.37.86
		System.setProperty("http.proxyHost", "205.189.37.86");
		System.setProperty("http.proxyPort", "53281");

		Map<String, String> jobInfo = new HashMap<>();

		// getting 7 days earlier date
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);

		// DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd
		// 'T' HH:mm");
		//
		// Date parsedDate;

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);

		Document doc = null;
		String job = "software developer";
		String location = "california";
		Elements elements = null;
		String url = "https://www.monster.com/jobs/search/?q=software-developer&where=california";
		String url1 = "https://www.monster.com/jobs/search/?q=" + job + "&where=" + location;

		Response response = null;
		try {
			response = Jsoup.connect(url1).userAgent(
					"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")

					.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();
			doc = response.parse();
			elements = doc.select(".js_result_container .js_result_details ");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		List<String> siteList = new ArrayList<>();
		for (Element e : elements) {
			LocalDateTime localDateTime = LocalDateTime.parse(e.getElementsByTag("time").attr("datetime"), formatter);
			Date parsedDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
			jobMap.put(e.getElementsByTag("a").attr("href"), parsedDate);

		}

		Document weekOldDoc = null;
		for (Map.Entry<String, Date> e : jobMap.entrySet()) {

			if (e.getValue().after(cal.getTime()) | e.getValue().equals(cal.getTime())) {
				System.out.println(e.getKey() + " ---- with date ---" + e.getValue());

				try {
					Response response1 = Jsoup.connect(e.getKey()).userAgent(
							"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")

							.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();
					weekOldDoc = response1.parse();
					// weekOldDoc = Jsoup.connect(e.getKey()).get();

					System.out.println("what!!!!");
				
					Elements elements2 = weekOldDoc.select("#JobSummary .mux-job-summary .summary-section .header ");
					if (elements2.isEmpty()) {
						System.out.println("The element is empty");
					}

					for (Element e1 : elements2) {
						

//						if (e1.getElementsByTag("dt:contains(Location)") != null) {
//							System.out.println("Locations :" + e1.getElementsByTag("dd").text());
//							jobSummary.setLocations(e1.getElementsByTag("dd").text());
//						}
//						if (e1.getElementsByTag("dt:contains(Job Type)") != null) {
//							// System.out.println("Job Type
//							// :"+e1.getElementsByTag("dd").first().text());
//							jobSummary.setJobType(e1.getElementsByTag("dd").first().text());
//						}
//						if (e1.getElementsByTag("dt:contains(Posted)") != null) {
//							// System.out.println("Posted
//							// :"+e1.getElementsByTag("dd").first().text());
//							jobSummary.setJobPostDate(e1.getElementsByTag("dd").first().text());
//						}
//
//						
						
						
						Elements elKey=e1.select(".key");
						Elements elValue=e1.select(".value");
						
						Iterator<Element> itrkey=elKey.iterator();
						Iterator<Element> itrValue=elValue.iterator();
						
						while(itrkey.hasNext() && itrValue.hasNext()){
							Element dt = (Element) itrkey.next();
			                Element dd = (Element) itrValue.next();
			               
			               
			               
			                if(dt.text().equals("Industries")){
			                	jobSummary.setCompanyIndustry(dd.text());
			                	System.out.println(dd.text());
			                }
			                if(dt.text().equals("Reference code")){
			                	System.out.println(dd.text());
			                }
			                
			                jobSummaryMap.put(e.getKey(), jobSummary);
			            
						}
						
					}
					

					

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 

				System.out.println("----------------------------------------------------------------");

			}
		}
		for (Map.Entry<String, Glassdoor> m : jobSummaryMap.entrySet()) {
			
			System.out.println("---- " + m.getValue().getCompanyName());
			System.out.println("---- " + m.getValue().getJobPostDate());
			
		}
		System.out.println("this line");
		try {
			Thread.sleep(22000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public void getJobSummary() {

	}

	public static void main(String[] args) {
		MonsterCrawler monsterCrawler = new MonsterCrawler();
		monsterCrawler.start();

	}
}
