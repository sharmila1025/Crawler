package com.sharmila.scrapper.domain;

import java.util.Map;

public class Tracker {

	private String pageUrl ;
	private String fileName;
	private int lineNumber;
	private int pageNumber;
	private String trackMap;
	private int totalPage;
	private String jobUrl;
	
	
	
	public String getPageURL() {
		return pageUrl;
	}
	public void setPageURL(String url) {
		this.pageUrl = url;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getTrackMap() {
		return trackMap;
	}
	public void setTrackMap(String trackMap) {
		this.trackMap = trackMap;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public String getJobUrl() {
		return jobUrl;
	}
	public void setJobUrl(String jobUrl) {
		this.jobUrl = jobUrl;
	}
	
	 	
}
