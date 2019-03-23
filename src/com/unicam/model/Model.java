package com.unicam.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.bson.Document;

@XmlRootElement
public class Model {

	private int ID;
	private String name;
	private int maxNumber;
	private String uploadedBy;
	private List<String> mandatoryRoles;
	private List<String> optionalRoles;
	private List<Document> instances;

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
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

	public List<String> getMandatoryRoles() {
		return mandatoryRoles;
	}

	public void setMandatoryRoles(List<String> mandatoryRoles) {
		this.mandatoryRoles = mandatoryRoles;
	}

	public List<String> getOptionalRoles() {
		return optionalRoles;
	}

	public void setOptionalRoles(List<String> optionalRoles) {
		this.optionalRoles = optionalRoles;
	}

	public List<Document> getInstances() {
		return instances;
	}

	public void setInstances(List<Document> instances) {
		this.instances = instances;
	}

	public Model(int iD, String name, int maxNumber, String uploadedBy, List<String> mandatoryRoles,
			List<String> optionalRoles, List<Document> instances) {
		super();
		ID = iD;
		this.name = name;
		this.maxNumber = maxNumber;
		this.uploadedBy = uploadedBy;
		this.mandatoryRoles = mandatoryRoles;
		this.optionalRoles = optionalRoles;
		this.instances = instances;
	}

	public Model() {
		super();
	}

}
