package com.sharmila.scrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.sharmila.scrapper.domain.JobData;
import com.sharmila.scrapper.domain.Tracker;

public class GlassdoorJobRepoJsoup {
	public String htmlToText(String html) {
		return Jsoup.parse(html).text();
	}

	public void crawl() {

		String stateName = null;
		String dataId = null;
		// crawling by states
		Document document = fromGoogleBot("https://www.glassdoor.com/sitedirectory/city-jobs.htm");
		Elements elements = document.select(".stateList li");

		List<String> stateList = new ArrayList<>();
		List<String> cityList = new ArrayList<>();
		for (Element e : elements) {
			// System.out.println(e.getElementsByTag("a").attr("href"));
			stateList.add(e.getElementsByTag("a").attr("href"));

		}

		// looping and crawling over all states
		for (String s : stateList) {
			Document docState = fromGoogleBot("https://www.glassdoor.com" + s);
			Elements elementState = docState.select(".cityList li");
			Pattern pattern = Pattern.compile(".city-jobs\\/(.*?)-IS-(.*)");
			Matcher matcher = pattern.matcher(s);
			if (matcher.find()) {

				System.out.println("stateName :" + matcher.group(1));
				stateName = matcher.group(1);
			}

			int count = 1;
			int cityNumber = 1;
			for (Element e : elementState) {

				// adding all the cities of the particular state to cityList
				// System.out.println(e.getElementsByTag("a").attr("href"));
				cityList.add(e.getElementsByTag("a").attr("href"));

			}

			// crawl the first city of the cityList for one state

			Document docCityMain = fromGoogleBot("https://www.glassdoor.com" + cityList.get(0));
			Elements elementsCityMain = docCityMain.select("#JobResults article#MainCol");

			for (Element e : elementsCityMain) {
				JobData glassDoor = new JobData();
				// remove .htm from url before assigning page number and after
				// that assign page number

				String url = cityList.get(0).replaceAll("\\.htm", "");

				int totalPages = Integer.parseInt(e.getElementById("TotalPages").attr("value"));
				for (int i = 1; i <= totalPages; i++) {
					// construct uri to crawl through each page
					System.out.println("THIS " + "https://www.glassdoor.com" + url + "_IP" + i + ".htm");
					Document docCity = fromGoogleBot("https://www.glassdoor.com" + url + "_IP" + i + ".htm");

					Elements jobScript = docCity.select("article#MainCol");
					for (Element jsc : jobScript) {
						try {
							String d = jsc.getElementsByTag("script").html();
							String scriptJson = htmlToText(jsc.getElementsByTag("script").html());
							System.out.println("*************************");
							System.out.println(scriptJson);
							System.out.println("*************************");
							JSONObject jsonObject = new JSONObject(scriptJson);
							JSONArray jsonArray = new JSONArray();
							jsonArray = jsonObject.getJSONArray("itemListElement");
							for (int j = 0; i < jsonArray.length(); i++) {

								JSONObject js = (JSONObject) jsonArray.get(i);

								glassDoor.setCompanyDetailUrl(js.getString("url"));

								// System.out.println(" Company detail url :" +
								// js.getString("url"));
								String[] urlJobDataId = js.getString("url").split("jl=");

								if (urlJobDataId[1].equals(dataId)) {
									System.out.println("JOB ID MATCHES ");
									glassDoor.setCompanyDetailUrl(js.getString("url"));
								}

							}

						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						
						
						Elements allElement = docCity.select("article#MainCol .jlGrid li.jl");

						for (Element allEle : allElement) {
							System.out.println("--****--");

							System.out.println(" JOB DATA ID " + allEle.attr("data-id"));
							dataId = allEle.attr("data-id");

							Elements flex = docCity.select("div.flexbox");
							for (Element eCity : flex) {

								Elements jobTitle = eCity.getElementsByClass("jobLink");
								for (Element eJobTitle : jobTitle) {

									// System.out.println("---TITLE-----" +
									// eJobTitle.text());
									Set<String> titleSet = new HashSet<>();
									titleSet.add(eJobTitle.text());
									glassDoor.setJobTitle(titleSet);
								}

								Elements jobLocation = eCity.getElementsByClass("empLoc");

								for (Element ejobLocation : jobLocation) {

									glassDoor.setCompanyName(ejobLocation.getElementsByTag("div").text());
									glassDoor.setLocation(ejobLocation.getElementsByTag("span").text());
								}

								Elements jobTime = eCity.getElementsByClass("showHH");

								for (Element j : jobTime) {

									// System.out.println("----TIME-----" +
									// j.getElementsByClass("minor").text());
									glassDoor.setEntryDate(j.getElementsByClass("minor").text());
								}
							}

						}
					}

					
					// write glassdoor data to a file
					System.out.println(" GLASSDOOR OBJ " + glassDoor.toString());
					writeDataToFile(glassDoor, stateName);

				}

			}

		}

	}

	public static Document fromGoogleBot(String url) {

		Document doc = null;
		try {
			Response response = Jsoup.connect(url).ignoreContentType(true).userAgent("Mediapartners-Google")
					.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();

			doc = response.parse();

			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
		return doc;
	}

	public void writeDataToFile(JobData glassdoor, String stateName) {

		File trackerFile = new File("glassdoor/" + stateName + ".json");

		Gson gs = new GsonBuilder().disableHtmlEscaping().create();

		Collection<String> glassdoorCollection = new ArrayList<String>();
		glassdoorCollection.add(gs.toJson(glassdoor));
		try {
			// append the data
			FileUtils.writeLines(trackerFile, glassdoorCollection, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		GlassdoorJobRepoJsoup g = new GlassdoorJobRepoJsoup();
		g.crawl();
	}
}
