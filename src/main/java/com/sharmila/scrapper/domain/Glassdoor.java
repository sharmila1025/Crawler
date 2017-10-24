package com.sharmila.scrapper.domain;

public class Glassdoor {



   
	private String title; 
	
	private String companyName;
	private String jobPostDate;
	private String companyIndustry;
	private String validThrough;
	private String source;
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
	
	
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getValidThrough() {
		return validThrough;
	}
	public void setValidThrough(String validThrough) {
		this.validThrough = validThrough;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
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
		return "Glassdoor [title=" + title + ", companyName=" + companyName + ", jobPostDate=" + jobPostDate
				+ ", companyIndustry=" + companyIndustry + ", validThrough=" + validThrough + ", source=" + source
				+ "]";
	}
	
	
	
	
	
}
