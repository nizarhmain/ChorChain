package com.unicam.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.bson.Document;

@XmlRootElement
public class User {
	private int ID;
	private String address;
	private List<Document> instances;

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<Document> getInstances() {
		return instances;
	}

	public void setInstances(List<Document> instances) {
		this.instances = instances;
	}

	public User(int iD, String address, List<Document> instances) {
		super();
		ID = iD;
		this.address = address;
		this.instances = instances;
	}

	public User() {
		super();
	}

}
