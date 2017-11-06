package com.sharmila.crawler;

import java.io.File;

import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import com.sharmila.scrapper.domain.Monster;
import com.sharmila.scrapper.domain.Tracker;

public class MonsterDotCom extends Thread {
	private Response response2;
	private static Map<String, String> stateCity = new HashMap<>();

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

		Response checkPrpxy;
		try {
			checkPrpxy = Jsoup.connect("http://whatismyipaddress.com/").userAgent("Googlebot")

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
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		System.out.println("after proxy");
		// reading from tracker file
		String dataFromTracker = readTrackerfile();
		Map<String, String> mapFromTracker = new HashMap<>();
		if (!(dataFromTracker == null)) {
			System.out.println(" tracker data not null ");
			try {

				JSONObject trackerJson = new JSONObject(dataFromTracker);
				System.out.println("JSON object " + trackerJson);
				String trackerFile = trackerJson.getString("fileName");
				String lineNumber = trackerJson.getString("lineNumber");

				String url = trackerJson.getString("url");
				System.out.println(" tracker url " + url);

				Pattern pattern = Pattern.compile(".page.*");

				Matcher matcher = pattern.matcher(url);
				System.out.println("before matcher find method ");

				Map<String, String> trackMap = new HashMap<>();
				java.lang.reflect.Type type = new TypeToken<Map<String, String>>() {
				}.getType();

				trackMap = new Gson().fromJson(trackerJson.get("trackMap").toString(), type);
				if (matcher.find()) {
					System.out.println(" url does match the page regex ");
					stateCrawl(trackMap, url);

				} else {
					System.out.println(" url doesnt match the page regex ");
				}

			} catch (Exception e) {
				// TODO: handle exception
			}

		} else {
			beginCrawl();
		}
	}

	public void beginCrawl() {
		System.out.println("************BEGINING of THE CRAWL*************** \n");

		try {

			Response response = Jsoup.connect("https://www.monster.com/jobs/").userAgent("Googlebot")

					.referrer("http://www.google.com").timeout(60000).followRedirects(true).execute();
			System.out.println(" response status " + response.statusMessage());
			Document document = response.parse();
			Elements elements = document
					.select(".browse-jobs-section .col-md-10.col-md-offset-1 div.caption span.fa.fa-globe ");

			Map<String, String> stateMap = new HashMap<>();
			Elements it = null;

			for (Element e : elements) {

				for (Element w : e.parent().firstElementSibling().parent().getElementsByClass("fnt4")) {

					stateMap.put(w.getElementsByTag("a").text(), w.getElementsByTag("a").attr("href"));
					File stateUrlFile = new File("stateList.txt");
					FileUtils.writeStringToFile(stateUrlFile, w.getElementsByTag("a").attr("href") + "\n", true);
				}
			}

			stateCrawl(stateMap, "");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void stateCrawl(Map<String, String> stateMap, String url) {
		int state = 1;

		if (url.isEmpty()) {
			for (Map.Entry<String, String> m : stateMap.entrySet()) {

				System.out.println("state --- " + state++ + " state name is :" + m.getKey());

				// System.out.println(m.getKey() + " url : " + m.getValue());
				Response response1;
				try {
					response1 = Jsoup.connect(m.getValue()).userAgent("Googlebot")

							.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();

					Document docByState = response1.parse();

					Elements stateElement = docByState.select(".card-columns.browse-all");

					for (Element e : stateElement) {

						stateCity.put(e.getElementsByTag("a").get(0).text(),
								e.getElementsByTag("a").get(0).attr("href"));

					}
					// write city state data to file
					File stateCityFile = new File("stateCityFile.txt");
					try {
						FileUtils.writeStringToFile(stateCityFile, stateCity + "\n", true);
					} catch (IOException e4) {
						// TODO Auto-generated catch block
						e4.printStackTrace();
					}

					cityCrawl(stateCity, m.getKey());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		} else {
			System.out.println("---url is not empty---------------");
			Pattern pattern = Pattern.compile(".page=(.*)");
			Matcher matcher = pattern.matcher(url);
			int lastpage = 0;
			if (matcher.find()) {
				System.out.println("the last page of the url " + matcher.group(1));
				lastpage = Integer.parseInt(matcher.group(1));
			}
			String newUrl = url.replaceFirst(".page.*", "");
			System.out.println(" the new url " + newUrl);
			int totalNumberofPages = 18;

			File stateUrlFile = new File("stateList.txt");
			List<String> stateUrl = null;
			System.out.println("here");
			try {
				stateUrl = FileUtils.readLines(stateUrlFile);

				stateUrl.addAll(stateUrl);
				// looping over all statelist
				for (int i = 0; i < stateUrl.size(); i++) {
					System.out.println("state name "+stateUrl.get(i));
					System.out.println();
					String stateName=stateUrl.get(i).replaceAll("", "");
					
					Response response1;
					try {
						response1 = Jsoup.connect(stateUrl.get(i)).userAgent("Googlebot")

								.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();

						Document docByState = response1.parse();

						Elements stateElement = docByState.select(".card-columns.browse-all");

						List<String> stateCity = new ArrayList<>();

						for (Element e : stateElement) {

							stateCity.add(e.getElementsByTag("a").get(0).attr("href"));
							
							
						}

						if (newUrl.equals(stateCity.get(i))) {

							System.out.println(" tracker file url matched ");
							// looping over all jobs of a city

							for (int j = lastpage; j <= totalNumberofPages; j++) {
								try {
									response2 = Jsoup.connect(stateCity.get(i + 1) + "?page=" + j).userAgent("Googlebot")

											.referrer("http://www.google.com").timeout(12000).followRedirects(true)
											.execute();
								} catch (IOException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
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

									// int sleepTime = getRandomNumber();
									System.out.println(
											"----------------------------ENDS-----------------------------------------");
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									// writing json to file
									File fileName = changeObjToJSON(monster, stateName);
//									writeToTrackerFile(response2.url().toString(), fileName, i, stateCity,
//											totalNumberofPages);

								}

							}
						} else {
							System.out.println("no it did not ");
						}

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void resumeCrawl(Map<String, String> stateMap, String url, int totalPage, int lastPage) {
		System.out.println("************RESUSIMG THE CRAWL*************** \n");

		for (int i = lastPage + 1; i <= totalPage; i++) {
			try {
				response2 = Jsoup.connect(url + "?page=" + i).userAgent("Googlebot")

						.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();

				Document docByCity = response2.parse();
				Elements cityElements = docByCity
						.select(".jsr-mid .js_result_container.clearfix.primary .js_result_row .js_result_details");

				// save information in monster object
				Monster monster = new Monster();
				for (Element ce : cityElements) {
					System.out.println("-------------------------------------STARTS---------------------------------");
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

					// int sleepTime = getRandomNumber();
					System.out.println("----------------------------ENDS-----------------------------------------");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

		}

	}

	public void cityCrawl(Map<String, String> stateCity, String stateName) {

		for (Map.Entry<String, String> stateCityUrlMap : stateCity.entrySet()) {

			System.out.println(stateCityUrlMap.getKey() + "------------------" + stateCityUrlMap.getValue());

			Response res;
			try {
				res = Jsoup.connect(stateCityUrlMap.getValue()).userAgent("Googlebot")

						.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();

				Document doc1 = res.parse();

				String total = doc1.select("div#main.row #totalPages").attr("value");

				int totalNumberofPages = Integer.parseInt(total);
				System.out.println("----------------------The total page is ----" + totalNumberofPages);
				// now loop over the total number of pages of the url
				for (int i = 1; i <= totalNumberofPages; i++) {
					try {
						response2 = Jsoup.connect(stateCityUrlMap.getValue() + "?page=" + i).userAgent("Googlebot")

								.referrer("http://www.google.com").timeout(12000).followRedirects(true).execute();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					Document docByCity = response2.parse();
					Elements cityElements = docByCity
							.select(".jsr-mid .js_result_container.clearfix.primary .js_result_row .js_result_details");

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

						// int sleepTime = getRandomNumber();
						System.out.println("----------------------------ENDS-----------------------------------------");
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// writing json to file
						File fileName = changeObjToJSON(monster, stateName);
						writeToTrackerFile(response2.url().toString(), fileName, i, stateCity, totalNumberofPages);

					}

				}

			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}

		}

	}

	public void writeToTrackerFile(String url, File fileName, int pageNumber, Map<String, String> map, int totalPage) {

		for (Map.Entry<String, String> loopOverT : map.entrySet()) {
			System.out.println("inside writing tracker file  ");
			System.out.println("*******-" + loopOverT.getValue());
		}
		JSONObject jsonObjectCityMap = new JSONObject(map);
		File trackerFile = new File("trackerFile.txt");
		Collection<String> data;
		Gson gs = new GsonBuilder().disableHtmlEscaping().create();
		Tracker tracker = new Tracker();
		tracker.setFileName(fileName.toString());
		//tracker.setUrl(url);
		tracker.setPageNumber(pageNumber);
		tracker.setTrackMap(jsonObjectCityMap.toString());
		tracker.setTotalPage(totalPage);
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

	public String readTrackerfile() {
		Map<String, String> emptyMap = new HashMap<>();
		File file = new File("trackerFile.txt");
		String trackerData = null;
		if (file.exists() == true) {

			try {
				trackerData = FileUtils.readFileToString(file);
				System.out.println(trackerData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			// String url="www.monster.com/jobs";
			writeToTrackerFile("", file, 0, emptyMap, 0);
		}

		return trackerData;
	}

	public Integer getRandomNumber() {

		int high = 7000;

		int low = 5000;
		Random random = new Random();
		int result = random.nextInt(high - low) + low;
		System.out.println("thread sleep time is " + result);
		return result;
	}

	public File changeObjToJSON(Monster monster, String stateName) {

		Gson gson = new Gson();
		File targetFile = null;
		String result = gson.toJson(monster);
		System.out.println("The monster json is " + result);
		//
		try {
			String[] fileName = stateName.split(" ");
			targetFile = new File("job1/" + fileName[0] + ".json");

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
		return targetFile;
	}

	@Override
	public String toString() {
		return "GlassdoorCrawler []";
	}

	public static void main(String[] arg) {
		MonsterDotCom m = new MonsterDotCom();
		m.start();

	}
}
