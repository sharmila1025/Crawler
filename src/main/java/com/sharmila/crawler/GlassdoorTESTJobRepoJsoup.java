package com.sharmila.crawler;

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

import javax.swing.plaf.metal.MetalIconFactory.FolderIcon16;

import org.jsoup.Jsoup;
import org.apache.commons.io.FileUtils;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.sharmila.esclient.ElasticClient;
import com.sharmila.scrapper.domain.BuyingIntentData;
import com.sharmila.scrapper.domain.JobData;
import com.sharmila.scrapper.domain.Tracker;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.FilterBuilders.*;

public class GlassdoorTESTJobRepoJsoup {

	private Client client = ElasticClient.CLIENT.getInstance();

	public void getCompanyInfo() {
		File employerIdFile = new File("test/employerId.txt");
		List<String> empIDList;
		try {
			empIDList = FileUtils.readLines(employerIdFile);

			Set<String> empID = new HashSet<>(empIDList);
			System.out.println("the empId " + empID.size());
			for (String s : empID) {

				BuyingIntentData companyData = new BuyingIntentData();

				String companyDetailsUrl = "https://www.glassdoor.com/Overview/companyOverviewBasicInfoAjax.htm?&employerId="
						+ s + "&title=+Overview&linkCompetitors=true";
				System.out.println(companyDetailsUrl + "\n");
				Document document = fromGoogleBot(companyDetailsUrl);

				Elements elements = document.select("div#EmpBasicInfo.module.empBasicInfo  ");
				System.out.println(" here ");
				for (Element element : elements) {

					Elements infos = element.getElementsByClass("infoEntity");

					for (Element info : infos) {

						if (info.getElementsByTag("label").text().equals("Headquarters")) {
							// companyData.setHeadQuaters(info.getElementsByClass("value").text());
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
						// later we can update ES with the employer ID
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
		for (int j = 1; j < stateList.size(); j++) {
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
				// add the job data to the jobDataList
				List<String> jobDataList = new ArrayList<>();
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

								Set<String> titleSet = new HashSet<>();
								// System.out.println("---Title ---" +
								// flex.getElementsByClass("jobLink").text());
								titleSet.add(allElement.getElementsByClass("jobLink").text());
								// System.out.println(" JOB LINK ----" +
								// "https://www.glassdoor.com"+
								// flex.getElementsByClass("jobLink").attr("href"));
								glassDoor.setJobDetailUrl(
										"https://www.glassdoor.com" + flex.getElementsByClass("jobLink").attr("href"));

								glassDoor.setJobTitle(titleSet);

								Elements jobLocationTime = allElement.getElementsByClass("empLoc");

								for (Element ejobLocationTime : jobLocationTime) {

									String[] companyName = ejobLocationTime.getElementsByTag("div").get(0).text()
											.split("–");
									// System.out.println("---Company name--" +
									// companyName[0]);
									//
									// System.out.println(
									// "---Location name--" +
									// ejobLocationTime.getElementsByClass("loc").text());
									glassDoor.setCompanyName(companyName[0]);
									glassDoor.setLocation(ejobLocationTime.getElementsByClass("loc").text());

								}
								Elements time = allElement.getElementsByClass("showHH");

								for (Element t : time) {
									// System.out.println(" text " +
									// t.getElementsByTag("span").get(0).text());
									glassDoor.setEntryDate(t.getElementsByTag("span").get(0).text());
								}
								// write glassdoor data to a file
								System.out.println(" GLASSDOOR OBJ " + glassDoor.toString());

								writeDataToFile(glassDoor, stateName, empID);
								jobDataList.add(new Gson().toJson(glassDoor));
								System.out.println(
										"--********************* JOB INFO ENDS***********************************--");
							}

						}

					}
					for (int k = 0; k < jobDataList.size(); k++) {
						System.out.println(" ------- " + jobDataList.get(k));
					}
					System.out.println("job List size " + jobDataList.size());
					bulkIndex(jobDataList, "glassdoorjob");

					jobDataList.clear();

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

		File file = new File("test/" + stateName + ".json");

		Gson gs = new GsonBuilder().disableHtmlEscaping().create();

		Collection<String> glassdoorCollection = new ArrayList<String>();
		glassdoorCollection.add(gs.toJson(glassdoor));

		// write emp id to file

		File employerIdFile = new File("glassdoor/employerId.txt");
		Collection<String> EmpIDCollection = new HashSet();
		EmpIDCollection.add(empId);
		try {
			// append the data
			FileUtils.writeLines(file, glassdoorCollection, true);
			FileUtils.writeLines(employerIdFile, EmpIDCollection, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean bulkIndex(List<String> jobList, String docName) {
		System.out.println(" THE JOB LIST SIZE IS " + jobList.size());
		boolean failure = true;
		System.out.println(" glassdoor bulk index method ");
		for (int j = 0; j < jobList.size(); j++) {
			System.out.println("---" + jobList.get(j).toString());

			JSONParser parser = new JSONParser();
			// File filePath = new File("test/");
			// File[] fileList = filePath.listFiles();
			BulkResponse bulkResponse = null;

			try {

				String data;

				Object obj;

				// data = new Gson().toJson(jobList.get(j));
				obj = parser.parse(jobList.get(j));

				byte[] json = new ObjectMapper().writeValueAsBytes(obj);

				BulkRequestBuilder reqBuilder = client.prepareBulk();

				reqBuilder.add(client.prepareIndex("crawldata", docName).setSource(json));

				bulkResponse = reqBuilder.execute().actionGet();
				System.out.println(" The job data is " + json);
				System.out.println(" the bulk response has failures " + bulkResponse.hasFailures());
			}

			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!bulkResponse.hasFailures()) {
				failure = false;
			}
		}
		jobList.clear();
		return failure;
	}

	// get companyId and put into hashset
	public HashSet<String> getCompanyIdList() {
		Set<String> companyIdSet = new HashSet();
		File filePath = new File("test/");
		File[] fileList = filePath.listFiles();

		for (int j = 0; j < fileList.length; j++) {
			if (fileList[j].isFile() && fileList[j].toString().endsWith(".json")) {
				String job = null;
				File file = new File(filePath.toString() + fileList[j]);
				try {
					FileUtils.readLines(file, job);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JSONObject jsonObj = null;
				try {
					jsonObj = new JSONObject(job);
					companyIdSet.add(jsonObj.getString("employerId"));
					// writing the set to the file
					File fileEmpId = new File("test/employerId.txt");
					try {
						FileUtils.writeLines(fileEmpId, companyIdSet, true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		return (HashSet<String>) companyIdSet;

	}

	public List<String> readStateJobFile(String fileName) {

		File file = new File(fileName);
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

	public static void main(String[] args) {
		GlassdoorTESTJobRepoJsoup g = new GlassdoorTESTJobRepoJsoup();
	//	g.crawl();

		g.getCompanyIdList();
		// g.getCompanyInfo();
	}
}
