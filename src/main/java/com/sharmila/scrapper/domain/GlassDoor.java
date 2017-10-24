package com.sharmila.scrapper.domain;

import java.util.Arrays;
import java.util.Date;

public class GlassDoor {
   
	private String[] title; 
	
	private String companyName;
	private String jobPostDate;
	private String companyIndustry;
	
	
	
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getJobPostDate() {
		return jobPostDate;
	}
	public void setJobPostDate(String jobPostDate) {
		this.jobPostDate = jobPostDate;
	}
	
	
	public String[] getTitle() {
		return title;
	}
	public void setTitle(String[] title) {
		this.title = title;
	}
	public String getCompanyIndustry() {
		return companyIndustry;
	}
	public void setCompanyIndustry(String companyIndustry) {
		this.companyIndustry = companyIndustry;
	}
	@Override
	public String toString() {
		return "GlassDoor [title=" + Arrays.toString(title) + ", companyName=" + companyName + ", jobPostDate="
				+ jobPostDate + ", companyIndustry=" + companyIndustry + "]";
	}
	
	
	
	
	
}
