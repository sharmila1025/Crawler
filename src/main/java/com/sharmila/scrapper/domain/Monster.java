package com.sharmila.scrapper.domain;

import java.util.Date;
import java.util.Map;

public class Monster {
	private  String title;
	private String jobUrl;
	private Date postedDate;
	private String companyName;
	private String companyCity;
	private String pageUrl;

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Date getPostedDate() {
		return postedDate;
	}
	public void setPostedDate(Date postedDate) {
		this.postedDate = postedDate;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getCompanyCity() {
		return companyCity;
	}
	public void setCompanyCity(String companyCity) {
		this.companyCity = companyCity;
	}
	public String getJobUrl() {
		return jobUrl;
	}
	public void setJobUrl(String url) {
		this.jobUrl = url;
	}
	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	
	
	
	

}
