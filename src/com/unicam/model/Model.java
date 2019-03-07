package com.unicam.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Model {
	
	private String name;
	private int maxNumber;
	private String uploadedBy;
	private List<String> roles;	
	
	
	
	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getMaxNumber() {
		return maxNumber;
	}
	
	public void setMaxNumber(int maxNumber) {
		this.maxNumber = maxNumber;
	}
	
	public String getUploadedBy() {
		return uploadedBy;
	}
	
	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public Model(String name, int maxNumber, String uploadedBy, List<String> roles) {
		super();
		this.name = name;
		this.maxNumber = maxNumber;
		this.uploadedBy = uploadedBy;
		this.roles = roles;
	}

	public Model() {
		super();
	}


	
	
}
