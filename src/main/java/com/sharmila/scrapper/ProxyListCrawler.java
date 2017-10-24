package com.sharmila.scrapper;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class ProxyListCrawler extends Thread {
	
	public void run(){
		
		System.setProperty("http.proxyHost", "205.189.37.86");
		System.setProperty("http.proxyPort", "53281");
		
		
	//	Response response=Jsoup.connect("https://free-proxy-list.net/").refferrer("www.go")
		
	}

	public static void main(String args[]){
		ProxyListCrawler pListCrawler=new ProxyListCrawler();
		pListCrawler.start();
	}
}
