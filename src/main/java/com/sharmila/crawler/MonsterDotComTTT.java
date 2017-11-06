package com.sharmila.crawler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
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
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.sharmila.scrapper.domain.Glassdoor;
import com.sharmila.scrapper.domain.Monster;
import com.sharmila.scrapper.domain.Tracker;

public class MonsterDotComTTT extends Thread {

	private Document document;

	Map<String, String> stateCity = new HashMap<>();
	List<String> stateUrlList = new ArrayList<>();
	List<String> cityUrlList = new ArrayList<>();

	public void beginCrawl() {

		int totalNumberofPages = 0;

		document = fromGoogleBot("https://www.monster.com/jobs/");
		Elements elements = document
				.select(".browse-jobs-section .col-md-10.col-md-offset-1 div.caption span.fa.fa-globe ");

		Elements it = null;
		int state = 1;
		File stateUrlFile = new File("stateList.txt");
		// delete file if exits to be done
		for (Element e : elements) {

			for (Element w : e.parent().firstElementSibling().parent().getElementsByClass("fnt4")) {

				// stateMap.put(w.getElementsByTag("a").text(),
				// w.getElementsByTag("a").attr("href"));
				stateUrlList.add(w.getElementsByTag("a").attr("href"));
				try {
					FileUtils.writeStringToFile(stateUrlFile, w.getElementsByTag("a").attr("href") + "\n", true);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		}
		
		//Get monster data from the method getMonsterData() ;this methods is repeated in resumeCrawl function too

		getMonsterData();
	}

	public void resumeCrawl(String trackerData) {

		// now resume crawl from last page

		try {
			JSONObject jsonObj = new JSONObject(trackerData);

			String url = jsonObj.getString("pageUrl");
			int pageNumber = jsonObj.getInt("pageNumber");
			int totalNumberofPages = jsonObj.getInt("totalPage");
			// load stateURLFile
			File file = new File("stateList.txt");
			String data = null;
			List<String> stateUrlList = new ArrayList<>();
			try {
				stateUrlList = FileUtils.readLines(file);

				String parsedUrl = url.replaceAll(".page.*", "");

				System.out.println("Parsed URL: " + parsedUrl);
				
				
				for (int i = 0; i < stateUrlList.size(); i++) {

					document = fromGoogleBot(stateUrlList.get(i));
					Elements stateElement = document.select(".card-columns.browse-all");

					for (Element e : stateElement) {

						// stateCity.put(e.getElementsByTag("a").get(0).text(),
						// e.getElementsByTag("a").get(0).attr("href"));
						cityUrlList.add(e.getElementsByTag("a").get(0).attr("href"));
					}

					if (parsedUrl.equalsIgnoreCase(cityUrlList.get(i))) {

						// begin crawl from this url with page index
						System.out.println("The url did match,---------- before resuming crawl");

						System.out.println("-------------- NOW RESUMING CRAWL----------");
						for (int j = 0; j < cityUrlList.size(); j++) {
							// crawl over the city

							document = fromGoogleBot(cityUrlList.get(j));

							String total = document.select("div#main.row #totalPages").attr("value");

							totalNumberofPages = Integer.parseInt(total);
							System.out.println("----------------------The total page is ----" + totalNumberofPages);

							// now loop over the total number of pages of the
							// url
							for (int k = 1; k <= totalNumberofPages; k++) {
								// stateCityUrlMap.getValue()
								document = fromGoogleBot(cityUrlList.get(j) + "?page=" + k);
								Elements cityElements = document.select(
										".jsr-mid .js_result_container.clearfix.primary .js_result_row .js_result_details");

								// save information in monster object
								Monster monster = new Monster();
								int count = 0;
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

										System.out.println(" job url ---" + t.getElementsByTag("a").attr("href"));
										monster.setJobUrl(t.getElementsByTag("a").attr("href"));
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
										System.out
												.println("date------" + date.getElementsByTag("time").attr("datetime"));
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

									String stateName = getGroupUsingRegex(".l-(.*?)\\.", stateUrlList.get(j), 1);

									// before appending data to the file check
									// the last line number from the file and
									// append the data

									// *************************************************
									List<String> jsonStateList = new ArrayList<>();

									File fileState = new File("job/" + stateName + ".json");
									System.out.println(" file name " + "job/" + stateName + ".json");
									jsonStateList = FileUtils.readLines(fileState);
									for (int x = 0; x < jsonStateList.size(); x++) {
										if (x == (jsonStateList.size() - 1)) {
											System.out.println(" Last " + jsonStateList.get(x));

											// now we can check the monster
											// object with the obtained string
											// then skip append this string

											JSONObject monsterObj = new JSONObject(monster);
											JSONObject fileLastObj = new JSONObject(jsonStateList.get(x));
											if (monsterObj.getString("url").equals(fileLastObj.getString("jobUrl"))) {
												System.out.println(" IT IS EQUAL");
												System.out.println("NO UPDATE");

											} else {
												System.out.println("UPDATE");
												// now check for the

												// *************************************************
												// writing json to file
												writeMonsterDataToFile(monster, stateName);

												// writing data to tracker file
												// with
												// URL,PAGE num, TOTAL page

												writeToTrackerFile(cityUrlList.get(j) + "?page=" + k, k,
														totalNumberofPages, count++, monster.getJobUrl());

											}

										}
									}

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

					} else {
						System.out.println("URL DID NOT MATCH");
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void getMonsterData() {

		int totalNumberofPages = 0;
		for (int i = 0; i <= stateUrlList.size(); i++) {

			document = fromGoogleBot(stateUrlList.get(i));
			Elements stateElement = document.select(".card-columns.browse-all");

			for (Element e : stateElement) {

				cityUrlList.add(e.getElementsByTag("a").get(0).attr("href"));
			}

			for (int j = 0; j <= cityUrlList.size(); j++) {

				document = fromGoogleBot(cityUrlList.get(j));

				String total = document.select("div#main.row #totalPages").attr("value");

				totalNumberofPages = Integer.parseInt(total);
				System.out.println("----------------------The total page is ----" + totalNumberofPages);

				// now loop over the total number of pages of the url
				for (int k = 1; k <= totalNumberofPages; k++) {
					// stateCityUrlMap.getValue()
					document = fromGoogleBot(cityUrlList.get(j) + "?page=" + k);
					Elements cityElements = document
							.select(".jsr-mid .js_result_container.clearfix.primary .js_result_row .js_result_details");

					// save information in monster object
					Monster monster = new Monster();
					int count = 0;
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

						String stateName = getGroupUsingRegex(".l-(.*?)\\.", stateUrlList.get(i), 1);
						// writing json to file
					
						
						writeMonsterDataToFile(monster, stateName);

						// writing data to tracker file

						writeToTrackerFile(cityUrlList.get(j) + "?page=" + k, k, totalNumberofPages, count++,
								monster.getJobUrl());
						// int sleepTime = getRandomNumber();
						System.out.println("----------------------------ENDS-----------------------------------------");
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
	}

	public void run() {
		readTrackerfile();
		String trackerData = readTrackerfile();
		if (!trackerData.isEmpty()) {
			try {
				JSONObject jsonObj = new JSONObject(trackerData);
				if (jsonObj.getString("pageUrl").equals("")) {
					System.out.println("data is empty in tracker file");
					beginCrawl();
				} else {
					System.out.println("tracker file URL is not empty ");
					resumeCrawl(trackerData);

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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

			writeToTrackerFile("", 0, 0, 0, "");
		}

		return trackerData;
	}

	public void writeToTrackerFile(String lastUrlPage, int pageNumber, int totalPage, int lineNumber, String jobUrl) {

		File trackerFile = new File("trackerFile.txt");
		Collection<String> data;
		Gson gs = new GsonBuilder().disableHtmlEscaping().create();
		Tracker tracker = new Tracker();

		tracker.setPageURL(lastUrlPage);
		tracker.setPageNumber(pageNumber);
		tracker.setLineNumber(lineNumber);
		tracker.setTotalPage(totalPage);
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

	public String writeMonsterDataToFile(Monster monster, String stateName) {

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

	public String htmlToText(String html) {
		return Jsoup.parse(html).text();
	}

	public String getGroupUsingRegex(String regex, String input, int group) {
		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(input);
		String groupData = null;
		if (matcher.find()) {
			groupData = matcher.group(group);
		}
		System.out.println("The match is " + groupData);
		return groupData;
	}

	public static Document fromGoogleBot(String url) {

		Document doc = null;
		try {
			Response response = Jsoup.connect(url).ignoreContentType(true).userAgent("Googlebot")
					.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();

			doc = response.parse();

			Thread.sleep(1100);
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
		// m.getGroupUsingRegex(".?page=(.*)",
		// "https://www.monster.com/jobs/l-gulf-shores,-al.aspx?page=6", 1);
	}
}
