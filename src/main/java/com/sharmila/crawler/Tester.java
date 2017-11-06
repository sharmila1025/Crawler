package com.sharmila.crawler;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.sharmila.scrapper.domain.Monster;
import com.sharmila.scrapper.domain.Tracker;

public class Tester extends Thread {

	private Document document;

	Map<String, String> stateCity = new HashMap<>();
	List<String> stateUrlList = new ArrayList<>();
	List<String> cityUrlList = new ArrayList<>();

	public void getMonsterDataCity(String trackerData) {
		Monster monster = new Monster();
		String url;
		try {
			JSONObject jsonObj = new JSONObject(trackerData);
			url = jsonObj.getString("pageUrl");
			int totalNumberofPages;
			int pageNumber = 0;
			int cityIndex = 0;
			// load stateURLFile
			File file = new File("stateList.txt");
			String data = null;
			List<String> stateUrlList = new ArrayList<>();

			try {
				stateUrlList = FileUtils.readLines(file);

				String parsedUrl = url.replaceAll(".page.*", "");

				System.out.println("Parsed URL: " + parsedUrl);

				for (int i = 0; i < stateUrlList.size(); i++) {

					String stateName = getGroupUsingRegex(".l-(.*?)\\.", stateUrlList.get(i), 1);

					document = fromGoogleBot(stateUrlList.get(i));
					Elements stateElement = document.select(".card-columns.browse-all");

					for (Element e : stateElement) {

						cityUrlList.add(e.getElementsByTag("a").get(0).attr("href"));
					}

					if (!url.isEmpty()) {
						System.out.println("URL is not empty");
						if (parsedUrl.equalsIgnoreCase(cityUrlList.get(i))) {

							pageNumber = jsonObj.getInt("pageNumber");

							// get the index of the city from the cityUrlList
							cityIndex = i;
							System.out.println("-------------RESUMING CRAWL -----------");

							// https://www.monster.com/jobs/l-alabaster,-al.aspx?page=4

							getMonsterObject(pageNumber, cityIndex, stateName, true);

						}
					} else {

						pageNumber = 1;
						System.out.println("URL EMPTY");
						getMonsterObject(pageNumber, cityIndex, stateName, false);

					}

				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	public Monster getMonsterObject(int pageNumber, int cityIndex, String stateName, boolean status) {
		Monster monster = new Monster();

		for (int j = cityIndex; j < cityUrlList.size(); j++) {
			document = fromGoogleBot(cityUrlList.get(j));

			String total = document.select("div#main.row #totalPages").attr("value");

			int totalNumberofPages = Integer.parseInt(total);
			System.out.println("----------------------The total page is ----" + totalNumberofPages);

			// now loop over the total number of pages of the
			// url

			System.out.println(" the page number is " + pageNumber);
			for (int k = pageNumber; k < totalNumberofPages; k++) {
				// stateCityUrlMap.getValue()
				System.out.println(" THIS IS PAGE NUMBER  " + k);
				document = fromGoogleBot(cityUrlList.get(j) + "?page=" + k);
				Elements cityElements = document
						.select(".jsr-mid .js_result_container.clearfix.primary .js_result_row .js_result_details");

				// save information in monster object

				for (Element ce : cityElements) {
					
					System.out.println("-------------------------------------STARTS---------------------------------");

					monster.setPageUrl(cityUrlList.get(j) + "?page=" + k);
					Elements company = ce.getElementsByClass("company");

					for (Element com : company) {
						System.out.println("company--------" + com.getElementsByTag("a").attr("title"));
						monster.setCompanyName(com.getElementsByTag("a").attr("title"));
					}

					Elements title = ce.getElementsByClass("jobTitle");
					for (Element t : title) {
						System.out.println("title------" + t.getElementsByTag("a").attr("title"));
						monster.setTitle(t.getElementsByTag("a").attr("title"));
						monster.setJobUrl(t.getElementsByTag("a").attr("href"));
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

					// write to file
					writeMonsterDataToFile(monster, stateName, status);

					writeToTrackerFile(cityUrlList.get(j) + "?page=" + k, monster.getJobUrl(), k);

					System.out.println("-------------------------------------ENDS---------------------------------");
				}

			}
		}

		return monster;
	}

	public String writeMonsterDataToFile(Monster monster, String stateName, boolean status) {

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Collection<String> collection = new ArrayList<String>();
		if (status == false) {
			// begin
			String result = gson.toJson(monster);
			System.out.println("The monster json is " + result);
			//
			try {
				String[] fileName = stateName.split(" ");
				File targetFile = new File("job/" + fileName[0] + ".json");

				collection.add(result);
				try {

					FileUtils.writeLines(targetFile, collection, true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (JsonIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// resume writing

			List<String> jsonStateList = new ArrayList<>();

			File fileState = new File("job/" + stateName + ".json");

			try {
				jsonStateList = FileUtils.readLines(fileState);

				for (int x = 0; x < jsonStateList.size(); x++) {
					int lastIndex = (jsonStateList.size()) - 1;
					
						System.out.println(" Last " + jsonStateList.get(x));

						// now we can check the monster
						// object with the obtained string
						// then skip append this string

						JSONObject monsterObj = new JSONObject(monster);
						JSONObject fileLastObj = new JSONObject(jsonStateList.get(lastIndex));
						if (monsterObj.getString("jobUrl").equals(fileLastObj.getString("jobUrl"))) {

							System.out.println("NO UPDATE");
							break;

						}else{
							collection.add(gson.toJson(monster));
							try {

								FileUtils.writeLines(fileState, collection, true);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";

	}

	public void crawlHomePage() {
		document = fromGoogleBot("https://www.monster.com/jobs/");
		Elements elements = document
				.select(".browse-jobs-section .col-md-10.col-md-offset-1 div.caption span.fa.fa-globe ");

		Elements it = null;
		int state = 1;
		File stateUrlFile = new File("stateList.txt");
		// delete file if exits to be done
		if (stateUrlFile.exists()) {
			try {
				FileUtils.forceDelete(stateUrlFile);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		for (Element e : elements) {

			for (Element w : e.parent().firstElementSibling().parent().getElementsByClass("fnt4")) {

				stateUrlList.add(w.getElementsByTag("a").attr("href"));
				try {

					FileUtils.writeStringToFile(stateUrlFile, w.getElementsByTag("a").attr("href") + "\n", true);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		}

	}

	public void run() {

		crawlHomePage();
		readTrackerfile();
		String trackerData = readTrackerfile();
		getMonsterDataCity(trackerData);

	}

	public String readTrackerfile() {
		Map<String, String> emptyMap = new HashMap<>();
		File file = new File("trackerFile.txt");
		String trackerData = null;
		if (file.exists()) {

			try {
				trackerData = FileUtils.readFileToString(file);
				System.out.println(trackerData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			writeToTrackerFile("", "", 0);
		}

		return trackerData;
	}

	public void writeToTrackerFile(String lastUrlPage, String jobUrl, int pageNumber) {

		File trackerFile = new File("trackerFile.txt");
		Collection<String> data;
		Gson gs = new GsonBuilder().disableHtmlEscaping().create();
		Tracker tracker = new Tracker();

		tracker.setPageURL(lastUrlPage);
		tracker.setPageNumber(pageNumber);

		tracker.setJobUrl(jobUrl);
		String trackerInfo = gs.toJson(tracker);

		Collection<String> trackerCollection = new ArrayList<String>();
		trackerCollection.add(trackerInfo);
		try {
			FileUtils.writeLines(trackerFile, trackerCollection);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// generate random number
	public Integer getRandomNumber() {

		int high = 7000;

		int low = 5000;
		Random random = new Random();
		int result = random.nextInt(high - low) + low;
		System.out.println("thread sleep time is " + result);
		return result;
	}

	public String getGroupUsingRegex(String regex, String input, int group) {
		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(input);
		String groupData = null;
		if (matcher.find()) {
			groupData = matcher.group(group);
		}

		return groupData;
	}

	public static Document fromGoogleBot(String url) {

		Document doc = null;
		try {
			Response response = Jsoup.connect(url).ignoreContentType(true).userAgent("Googlebot")
					.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();

			doc = response.parse();

			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
		return doc;
	}

	public String readFile(String fileName) {
		File file = new File(fileName);
		String data = null;
		try {
			FileUtils.readLines(file, data);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return data;
	}

	@Override
	public String toString() {
		return "GlassdoorCrawler []";
	}

	public static void main(String[] arg) {
		Tester m = new Tester();
		m.start();
		
	}
}
