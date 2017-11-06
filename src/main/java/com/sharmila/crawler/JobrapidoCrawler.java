package com.sharmila.crawler;

import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class JobrapidoCrawler extends Thread {

	public void run(){
		String query="plumber";
		String location="newyork";
	
		String url="http://us.jobrapido.com/?w="+query+"&l="+location+"&r=auto";
		try {
			Response response=Jsoup.connect(url).execute();
			Document document=response.parse();
			Elements elements=document.select("*");
			System.out.println(elements);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		JobrapidoCrawler crawler=new JobrapidoCrawler();
		crawler.start();
		
	}
}
