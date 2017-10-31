package com.sharmila.scrapper.domain;

import java.util.List;
import java.util.Set;

public class JobData {

	private String companyId;
	private String companyName;
	private String location;
	private String city;
	private List<String> industry;
	private Set<String> jobTitle;
	private String entryDate;
	private String companyLinkedinUrl;
	private String companyDetailUrl;
	private String description;
	private String searchKey;
	private int jobVersion;
	private String employerId;
	
	
	//sourceUrl is the url of the crawled page 
	private String sourceUrl;
	
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
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
	public String getCompanyLinkedinUrl() {
		return companyLinkedinUrl;
	}
	public void setCompanyLinkedinUrl(String companyLinkedinUrl) {
		this.companyLinkedinUrl = companyLinkedinUrl;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSearchKey() {
		return searchKey;
	}
	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}
	public int getJobVersion() {
		return jobVersion;
	}
	public void setJobVersion(int jobVersion2) {
		this.jobVersion = jobVersion2;
	}
	public String getCompanyDetailUrl() {
		return companyDetailUrl;
	}
	public void setCompanyDetailUrl(String companyDetailUrl) {
		this.companyDetailUrl = companyDetailUrl;
	}
	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
	
	public String getEmployerId() {
		return employerId;
	}
	public void setEmployerId(String employerId) {
		this.employerId = employerId;
	}
	@Override
	public String toString() {
		return "JobData [companyId=" + companyId + ", companyName=" + companyName + ", location=" + location + ", city="
				+ city + ", industry=" + industry + ", jobTitle=" + jobTitle + ", entryDate=" + entryDate
				+ ", companyLinkedinUrl=" + companyLinkedinUrl + ", companyDetailUrl=" + companyDetailUrl
				+ ", description=" + description + ", searchKey=" + searchKey + ", jobVersion=" + jobVersion
				+ ", employerId=" + employerId + ", sourceUrl=" + sourceUrl + "]";
	}
	
	
	
	
	
}
