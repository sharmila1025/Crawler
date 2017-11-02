package com.sharmila.scrapper.domain;

import java.util.List;
import java.util.Set;

public class BuyingIntentData {

	private String companyId;
	private String companyName;
	private String location;
	private String city;
	private List<String> industry;
	private Set<String> jobTitle;
	private String entryDate;
	private String companyLinkedinUrl;
	private String description;
	private String searchKey;
	private int jobVersion;
	private String crawlSource;
	private String website;
	private String employerId;
	
	
	private String size;
	private String type;
	private String revenue;
	private String headQuaters;
	private String founded;
	
	
	
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
	
	public String getCrawlSource() {
		return crawlSource;
	}
	public void setCrawlSource(String crawlSource) {
		this.crawlSource = crawlSource;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getEmployerId() {
		return employerId;
	}
	public void setEmployerId(String employerId) {
		this.employerId = employerId;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRevenue() {
		return revenue;
	}
	public void setRevenue(String revenue) {
		this.revenue = revenue;
	}
	public String getHeadQuaters() {
		return headQuaters;
	}
	public void setHeadQuaters(String headQuaters) {
		this.headQuaters = headQuaters;
	}
	public String getFounded() {
		return founded;
	}
	public void setFounded(String founded) {
		this.founded = founded;
	}
	
}
