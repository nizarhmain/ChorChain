package com.unicam.model;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.bson.Document;

@XmlRootElement
public class Instance {

	private int ID;
	private String name;
	private int actualNumber;
	private Map<String, Document> participants;
	private List<String> freeRoles;
	private String createdBy;
	private boolean done;
	private List<Document> visibleAt;
	private Document deployedContract;

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

	public int getActualNumber() {
		return actualNumber;
	}

	public void setActualNumber(int actualNumber) {
		this.actualNumber = actualNumber;
	}

	public Map<String, Document> getParticipants() {
		return participants;
	}

	public void setParticipants(Map<String, Document> participants) {
		this.participants = participants;
	}

	public List<String> getFreeRoles() {
		return freeRoles;
	}

	public void setFreeRoles(List<String> freeRoles) {
		this.freeRoles = freeRoles;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public List<Document> getVisibleAt() {
		return visibleAt;
	}

	public void setVisibleAt(List<Document> visibleAt) {
		this.visibleAt = visibleAt;
	}

	public Document getDeployedContract() {
		return deployedContract;
	}

	public void setDeployedContract(Document deployedContract) {
		this.deployedContract = deployedContract;
	}

	public Instance(int iD, String name, int actualNumber, Map<String, Document> participants, List<String> freeRoles,
			String createdBy, boolean done, List<Document> visibleAt, Document deployedContract) {
		super();
		ID = iD;
		this.name = name;
		this.actualNumber = actualNumber;
		this.participants = participants;
		this.freeRoles = freeRoles;
		this.createdBy = createdBy;
		this.done = done;
		this.visibleAt = visibleAt;
		this.deployedContract = deployedContract;
	}

	public Instance() {
		super();
	}

}
