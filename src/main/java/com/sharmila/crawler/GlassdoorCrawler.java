package com.sharmila.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.sharmila.scrapper.domain.Glassdoor;

import static org.hamcrest.CoreMatchers.instanceOf;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

public class GlassdoorCrawler extends Thread {

	private Map<String, String> companyLocationMap = new HashMap<>();

	public String htmlToText(String html) {
		return Jsoup.parse(html).text();
	}

	public void crawler(String company, String location) {
		// "185.119.57.121", "53281"
		// 205.189.37.86
		System.setProperty("http.proxyHost", "205.189.37.86");
		System.setProperty("http.proxyPort", "53281");

		Response response = null;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -15);

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		Date parsedDate;

		String[] titles = null;

		Map<String, String> jobMap = new HashMap<>();

		Glassdoor jobSummary = new Glassdoor();

		try {
			System.out.println("******************************************");
			System.out.println("crawling beings of----- " + company + " from--------- " + location);
			response = Jsoup.connect("https://www.glassdoor.com/Job/jobs.htm")

					.userAgent("Mozilla/5.0").timeout(10 * 1000).method(Method.POST).data("suggestCount", "0")
					.data("suggestChosen", "false").data("clickSource", "searchBtn").data("typedKeyword", "")
					.data("sc.keyword", company).data("LocationSearch", location).data("locT", "").data("locId", "")
					.data("jobType", "")

					.followRedirects(true).execute();

			Document document = response.parse();
			System.out.println(" first url " + response.url());
			// for(Map.Entry<String, String> m:response.headers().entrySet()){
			// System.out.println("key : "+m.getKey()+",Value : "+m.getValue());
			// }
			// System.out.println(" the response
			// "+response.headers().entrySet());
			Elements elements = document.select("#JobResults ul.jlGrid li.jl");
			int count = 0;
			for (Element e : elements) {

				jobMap.put(e.getElementsByTag("a").attr("href"), e.getElementsByClass("minor").text());

			}

			for (Map.Entry<String, String> map : jobMap.entrySet()) {
				System.out.println("this is " + count++);
				Response response1 = Jsoup.connect("https://www.glassdoor.com" + map.getKey()).userAgent("Mozilla/5.0")
						.timeout(10 * 1000).execute();
				Document document2 = response1.parse();
				System.out.println("requested url :" + "https://www.glassdoor.com" + map.getKey());
				String source = response1.url().toString();
				System.out.println("URL " + source);
				Elements elements2 = document2.select("article#MainCol");

				for (Element element : elements2) {

					Elements sc = element.getElementsByTag("script");
					String jsonBeforeParse = sc.html();

					String jsonAfterParse = htmlToText(jsonBeforeParse);

					if (jsonAfterParse.isEmpty()) {
						System.out.println(" JSON is empty \n ");
					}
					JSONObject jsonObject = null;
					// span.minor
					try {

						jsonObject = new JSONObject(jsonAfterParse);

						System.out.println(jsonObject);

						try {
							parsedDate = format.parse(jsonObject.getString("datePosted"));
							if (parsedDate.after(cal.getTime()) | parsedDate.equals(cal.getTime())) {
								JSONObject jobLocation = (JSONObject) jsonObject.get("jobLocation");
								JSONObject addressParse = (JSONObject) jobLocation.get("address");
								JSONObject hiringOrganization = (JSONObject) jsonObject.get("hiringOrganization");

								// jobSummary.setSource(response1.url());
								jobSummary.setCompanyName(hiringOrganization.getString("name"));
								jobSummary.setJobPostDate(jsonObject.getString("datePosted"));
								jobSummary.setValidThrough(jsonObject.getString("validThrough"));
								jobSummary.setTitle(jsonObject.getString("title"));

								// set source
								Pattern pattern = Pattern.compile("www.*.com");
								Matcher matcher = pattern.matcher(source);
								if (matcher.find()) {
									jobSummary.setSource(matcher.group(0));
								}

								System.out.println("Here is the job summary :");
								System.out.println(jobSummary.toString());

								if (jobSummary.toString().isEmpty()) {
									System.out.println(" No job post found ");
								}
								// scan and score ;bulk update

								System.out.println(
										"*********************end detailed crawl***********************************");
							}
						} catch (ParseException e1) {

							e1.printStackTrace();
						}

						if (jobSummary.toString().isEmpty()) {
							System.out.println(" No job post found ");
						}
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
				try {
					Thread.sleep(11000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			jobMap.clear();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {

		}
		try {
			Thread.sleep(11000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("********************* crawling ends here******************************");
	}

	public void run() {

		String CompanyData = null;
		try {
			FileUtils.readLines(new File("test/employerId.json"), CompanyData);
			JSONObject jsonObj = new JSONObject(CompanyData);
			jsonObj.getString("employerId");
			// get all
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void parseDoc(String companyInfo) {

	}

	@Override
	public String toString() {
		return "GlassdoorCrawler []";
	}

	public static void main(String[] arg) {
		GlassdoorCrawler glassdoorCrawler = new GlassdoorCrawler();

		glassdoorCrawler.start();

	}
}