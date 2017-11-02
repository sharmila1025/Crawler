package com.sharmila.scrapper.domain;

import java.util.List;
import java.util.Set;

public class JobData {

	private String companyName;
	private String location;
	private String city;
	private List<String> industry;
	private Set<String> jobTitle;
	private String entryDate;
	private String jobDetailUrl;
	private String crawlSource;
	private String employerId;

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public List<String> getIndustry() {
		return industry;
	}

	public void setIndustry(List<String> industry) {
		this.industry = industry;
	}

	public Set<String> getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(Set<String> jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(String entryDate) {
		this.entryDate = entryDate;
	}

	public String getJobDetailUrl() {
		return jobDetailUrl;
	}

	public void setJobDetailUrl(String jobDetailUrl) {
		this.jobDetailUrl = jobDetailUrl;
	}

	public String getCrawlSource() {
		return crawlSource;
	}

	public void setCrawlSource(String crawlSource) {
		this.crawlSource = crawlSource;
	}

	public String getEmployerId() {
		return employerId;
	}

	public void setEmployerId(String employerId) {
		this.employerId = employerId;
	}

}
