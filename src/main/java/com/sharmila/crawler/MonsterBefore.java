package com.sharmila.crawler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.sharmila.scrapper.domain.Glassdoor;
import com.sharmila.scrapper.domain.Monster;

public class MonsterBefore extends Thread {

	public String htmlToText(String html) {
		return Jsoup.parse(html).text();
	}

	public void run() {
		// "185.119.57.121", "53281"
		// 205.189.37.86
		// 14.139.248.17 80
		System.setProperty("http.proxyHost", "205.189.37.86");
		System.setProperty("http.proxyPort", "53281");
		Map<String, String> jobMap = new HashMap<>();
		String location = "newyork";
		String keyword = "software developer";
		// Glassdoor jobSummary = new Glassdoor();
		try {
			Response checkPrpxy = Jsoup.connect("http://whatismyipaddress.com/").userAgent("Googlebot")

					.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();

			Document proxyDoc = checkPrpxy.parse();
			Elements proxyElement = proxyDoc.select("#main_content #section_left ");
			String hostProxy = System.getProperty("http.proxyHost");
			for (Element e : proxyElement) {
				if (e.getElementsByTag("a").text().equals(hostProxy)) {
					System.out.println(" the proxy works");
				} else {
					System.out.println(" the proxy doesnot work ");
				}

			}

			Response response = Jsoup.connect("https://www.monster.com/jobs/").userAgent("Googlebot")

					.referrer("http://www.google.com").timeout(60000).followRedirects(true).execute();
			System.out.println(" response status " + response.statusMessage());
			Document document = response.parse();
			Elements elements = document
					.select(".browse-jobs-section .col-md-10.col-md-offset-1 div.caption span.fa.fa-globe ");

			Map<String, String> stateMap = new HashMap<>();
			Elements it = null;
			int state = 1;
			for (Element e : elements) {

				for (Element w : e.parent().firstElementSibling().parent().getElementsByClass("fnt4")) {

					stateMap.put(w.getElementsByTag("a").text(), w.getElementsByTag("a").attr("href"));
				}
			}

			for (Map.Entry<String, String> m : stateMap.entrySet()) {

				System.out.println("state --- " + state++ + " state name is :" + m.getKey());

				// System.out.println(m.getKey() + " url : " + m.getValue());
				Response response1 = Jsoup.connect(m.getValue()).userAgent("Googlebot")

						.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();
				Document docByState = response1.parse();

				Elements stateElement = docByState.select(".card-columns.browse-all");
				Map<String, String> stateCity = new HashMap<>();

				for (Element e : stateElement) {

					stateCity.put(e.getElementsByTag("a").get(0).text(), e.getElementsByTag("a").get(0).attr("href"));

				}

				for (Map.Entry<String, String> stateCityUrlMap : stateCity.entrySet()) {

					System.out.println(stateCityUrlMap.getKey() + "------------------" + stateCityUrlMap.getValue());

					Response res = Jsoup.connect(stateCityUrlMap.getValue()).userAgent("Googlebot")

							.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();
					Document doc1 = res.parse();

					String total = doc1.select("div#main.row #totalPages").attr("value");

					int totalNumberofPages = Integer.parseInt(total);
					System.out.println("----------------------The total page is ----" + totalNumberofPages);

					// now loop over the total number of pages of the url
					for (int i = 1; i <= totalNumberofPages; i++) {
						Response response2 = Jsoup.connect(stateCityUrlMap.getValue() + "?page=" + i)
								.userAgent("Googlebot")

								.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();
						Document docByCity = response2.parse();
						Elements cityElements = docByCity.select(
								".jsr-mid .js_result_container.clearfix.primary .js_result_row .js_result_details");

						// save information in monster object
						Monster monster = new Monster();
						for (Element ce : cityElements) {
							System.out.println(
									"-------------------------------------STARTS---------------------------------");
							Elements company = ce.getElementsByClass("company");

							for (Element com : company) {
								System.out.println("company--------" + com.getElementsByTag("a").attr("title"));
								monster.setCompanyName(com.getElementsByTag("a").attr("title"));
							}

							Elements title = ce.getElementsByClass("jobTitle");
							for (Element t : title) {
								System.out.println("title------" + t.getElementsByTag("a").attr("title"));
								monster.setTitle(t.getElementsByTag("a").attr("title"));
							}

							Elements locationElement = ce.getElementsByClass("job-specs-location");
							for (Element loc : locationElement) {
								System.out.println("location------" + loc.getElementsByTag("p").text());
								monster.setCompanyCity(loc.getElementsByTag("p").text());
							}
							// job-specs-date

							Elements dateElement = ce.getElementsByClass("job-specs-date");
							for (Element date : dateElement) {
								System.out.println("date------" + date.getElementsByTag("time").attr("datetime"));
								String dateTime = date.getElementsByTag("time").attr("datetime");
								DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'Hh:mm", Locale.ENGLISH);
								try {
									monster.setPostedDate(format.parse(dateTime));
									System.out.println("date formatted:- " + format.parse(dateTime));
								} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							// writing json to file
							changeObjToJSON(monster, m.getKey());

							// int sleepTime = getRandomNumber();
							System.out.println(
									"----------------------------ENDS-----------------------------------------");
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}
				}

			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public Integer getRandomNumber() {

		int high = 7000;

		int low = 5000;
		Random random = new Random();
		int result = random.nextInt(high - low) + low;
		System.out.println("thread sleep time is " + result);
		return result;
	}

	public String changeObjToJSON(Monster monster, String stateName) {

		Gson gson = new Gson();

		String result = gson.toJson(monster);
		System.out.println("The monster json is " + result);
		//
		try {
			String[] fileName = stateName.split(" ");
			File targetFile = new File("job/" + fileName[0] + ".json");

			Collection<String> collection = new ArrayList<String>();
			collection.add(result);
			try {
				FileUtils.writeStringToFile(targetFile, result + "\n", true);
				// FileUtils.writeLines(targetFile, collection);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String toString() {
		return "GlassdoorCrawler []";
	}

	public static void main(String[] arg) {
		MonsterBefore m = new MonsterBefore();
		m.start();
	}
}
