package com.sharmila.scrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.sharmila.scrapper.domain.CompanyData;
import com.sharmila.scrapper.domain.JobData;
import com.sharmila.scrapper.domain.Tracker;

public class GlassdoorJobRepoJsoup {
	public String htmlToText(String html) {
		return Jsoup.parse(html).text();
	}

	public List<String> readStateJobFile(String statename) {

		File file = new File("glassdoor/" + statename + ".json");
		List<String> data = null;
		if (file.exists()) {

			try {
				data = FileUtils.readLines(file);
				System.out.println(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			System.out.println("FILE doesnot exist");
		}

		return data;
	}

	public void getCompanyInfo() {

		// read employerIdFile and crawl each unique Id
		File employerIdFile = new File("glassdoor/employerId.txt");
		try {
			List<String> empID = FileUtils.readLines(employerIdFile);
			for (int i = 0; i < empID.size(); i++) {
				CompanyData companyData = new CompanyData();

				String companyDetailsUrl = "https://www.glassdoor.com/Overview/companyOverviewBasicInfoAjax.htm?&employerId="
						+ empID.get(i) + "&title=+Overview&linkCompetitors=true";
				Document document = fromGoogleBot(companyDetailsUrl);
				Elements elements = document.select("div#EmpBasicInfo.module.empBasicInfo  ");
				System.out.println(" here ");
				for (Element element : elements) {

					Elements infos = element.getElementsByClass("infoEntity");

					for (Element info : infos) {
						// System.out.println(" INFO ENTITY FOUND " + info);
						// System.out.println(info.getElementsByTag("label").text());
						if (info.getElementsByTag("label").text().equals("Headquarters")) {
							companyData.setHeadQuaters(info.getElementsByClass("value").text());
							System.out.println("Headquaters " + info.getElementsByClass("value").text());
						}

						else if (info.getElementsByTag("label").text().equals("Industry")) {
							List<String> industryList = new ArrayList<>();
							industryList.add(info.getElementsByClass("value").text());
							companyData.setIndustry(industryList);
							System.out.println("Industry " + info.getElementsByClass("value").text());
						}

						else if (info.getElementsByTag("label").text().equals("Website")) {

							companyData.setWebsite(info.getElementsByClass("value").text());
							System.out.println("Industry " + info.getElementsByClass("value").text());
						} else if (info.getElementsByTag("label").text().equals("Size")) {

							companyData.setSize(info.getElementsByClass("value").text());
							System.out.println("Industry " + info.getElementsByClass("value").text());
						} else if (info.getElementsByTag("label").text().equals("Founded")) {

							companyData.setFounded(info.getElementsByClass("value").text());
							System.out.println("Founded " + info.getElementsByClass("value").text());
						} else if (info.getElementsByTag("label").text().equals("Type")) {

							companyData.setType(info.getElementsByClass("value").text());
							System.out.println("Type " + info.getElementsByClass("value").text());
						}
						// Revenue
						else if (info.getElementsByTag("label").text().equals("Revenue")) {

							companyData.setRevenue(info.getElementsByClass("value").text());
							System.out.println("revenue " + info.getElementsByClass("value").text());
						}
					}

				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void crawl() {

		String stateName = null;

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
		for (int j = 4; j < stateList.size(); j++) {
			System.out.println(" STATE " + stateList.get(j));
			Document docState = fromGoogleBot("https://www.glassdoor.com" + stateList.get(j));
			Elements elementState = docState.select(".cityList li");
			Pattern pattern = Pattern.compile(".city-jobs\\/(.*?)-IS-(.*)");
			Matcher matcher = pattern.matcher(stateList.get(j));
			if (matcher.find()) {

				System.out.println("stateName :" + matcher.group(1));
				stateName = matcher.group(1);
			}

			int count = 1;
			int cityNumber = 1;
			cityList.clear();
			for (Element e : elementState) {

				// adding all the cities of the particular state to cityList
				// System.out.println(e.getElementsByTag("a").attr("href"));
				cityList.add(e.getElementsByTag("a").attr("href"));

			}

			// crawl the first city of the cityList for one state

			Document docCityMain = fromGoogleBot("https://www.glassdoor.com" + cityList.get(0));
			Elements elementsCityMain = docCityMain.select("#JobResults article#MainCol");
			stateLoop: for (Element e : elementsCityMain) {
				JobData glassDoor = new JobData();
				// remove .htm from url before assigning page number and after
				// that assign page number

				String url = cityList.get(0).replaceAll("\\.htm", "");
				String empID = null;
				int hit = 0;
				int totalPages = Integer.parseInt(e.getElementById("TotalPages").attr("value"));
				System.out.println(" TOTAL PAGES " + totalPages);
				for (int i = 1; i <= totalPages; i++) {
					// construct uri to crawl through each page
					System.out.println("THIS " + "https://www.glassdoor.com" + url + "_IP" + i + ".htm");
					Document docCity = fromGoogleBot("https://www.glassdoor.com" + url + "_IP" + i + ".htm");
					Elements wholeELement = docCity.select("*");
					Elements elem1 = docCity.select("article#MainCol #ResultsFooter");

					System.out.println("----before resultsfooter loop----");
					// if the document has class noResult then goto label
					// stateLoop and crawl next state
					if (wholeELement.hasClass("noResultsMessage")
							|| (docCity.getElementById("ResultsFooter").text().matches("Page 1 of " + totalPages))
									&& i >= 2) {
						System.out.println(" before label stateloop ");
						// clearing the cityList before jumping to stateLoop
						// label
						cityList.clear();
						break stateLoop;
					} else {
						for (Element ele : elem1) {
							System.out.println(" got the class ");

							Element eFooter = ele.getElementById("ResultsFooter");

							Elements allElements = docCity.select("#JobResults ul.jlGrid li.jl ");
							int num = 0;
							for (Element allElement : allElements) {
								System.out.println(
										"--********************* JOB INFO STARTS***********************************--"
												+ num++);

								// extract data-emp-id from each li
								empID = allElement.attr("data-emp-id");
								glassDoor.setEmployerId(empID);
								// glassDoor = getCompanyInfo(empID);
								// getting the first class named flexbox
								Element flex = allElement.getElementsByClass("flexbox").get(0);

								System.out.println();
								System.out.println();
								Set<String> titleSet = new HashSet<>();
								System.out.println("---Title ---" + flex.getElementsByClass("jobLink").text());
								titleSet.add(allElement.getElementsByClass("jobLink").text());
								System.out.println(" JOB LINK ----" + "https://www.glassdoor.com"
										+ flex.getElementsByClass("jobLink").attr("href"));
								glassDoor.setCompanyDetailUrl(
										"https://www.glassdoor.com" + flex.getElementsByClass("jobLink").attr("href"));

								glassDoor.setJobTitle(titleSet);

								Elements jobLocationTime = allElement.getElementsByClass("empLoc");

								for (Element ejobLocationTime : jobLocationTime) {
									System.out.println();
									System.out.println();

									String[] companyName = ejobLocationTime.getElementsByTag("div").get(0).text()
											.split("–");
									System.out.println("---Company name--" + companyName[0]);

									System.out.println(
											"---Location name--" + ejobLocationTime.getElementsByClass("loc").text());
									glassDoor.setCompanyName(companyName[0]);
									glassDoor.setLocation(ejobLocationTime.getElementsByClass("loc").text());
									System.out.println();
									System.out.println();

								}
								Elements time = allElement.getElementsByClass("showHH");

								for (Element t : time) {
									System.out.println(" text " + t.getElementsByTag("span").get(0).text());
									glassDoor.setEntryDate(t.getElementsByTag("span").get(0).text());
								}
								// write glassdoor data to a file
								System.out.println(" GLASSDOOR OBJ " + glassDoor.toString());
								writeDataToFile(glassDoor, stateName, empID);
								System.out.println(
										"--********************* JOB INFO ENDS***********************************--");
							}
						}
					}
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

	public void writeDataToFile(JobData glassdoor, String stateName, String empId) {

		File trackerFile = new File("test/" + stateName + ".json");

		Gson gs = new GsonBuilder().disableHtmlEscaping().create();

		Collection<String> glassdoorCollection = new ArrayList<String>();
		glassdoorCollection.add(gs.toJson(glassdoor));

		// write emp id to file

		File employerIdFile = new File("glassdoor/employerId.txt");
		Collection<String> EmpIDCollection = new HashSet();
		EmpIDCollection.add(empId);
		try {
			// append the data
			FileUtils.writeLines(trackerFile, glassdoorCollection, true);
			FileUtils.writeLines(employerIdFile, EmpIDCollection, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		GlassdoorJobRepoJsoup g = new GlassdoorJobRepoJsoup();
		g.crawl();
		// JobData glassDoor = g.getCompanyInfo("");
		// glassDoor.setCity("WHATEVER");
		// System.out.println(glassDoor.toString());
		// g.getDataFromURL();
		// g.getCompanyInfo("https://www.glassdoor.com/job-listing/registered-nurse-rn-med-surg-stepdown-unit-st-vincent-s-east-ft-nights-72-hours-bi-weekly-saint-vincents-health-system-JV_IC1127429_KO0,89_KE90,118.htm?jl=2568690532&ctt=1508921829872");
	}
}
