package com.unicam.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Instance {
	
	private String name;
	private int maxNumber;
	private int actualNumber;
	private List<String> participants;
	private List<String> roles;
	private List<String> freeRoles;
	private List<String> subbedRoles;
	private String createdBy;
	private int Id;
	private boolean done;
	
	
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
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
	public int getActualNumber() {
		return actualNumber;
	}
	public void setActualNumber(int actualNumber) {
		this.actualNumber = actualNumber;
	}
	public List<String> getParticipants() {
		return participants;
	}
	public void setParticipants(List<String> participants) {
		this.participants = participants;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public List<String> getFreeRoles() {
		return freeRoles;
	}
	public void setFreeRoles(List<String> freeRoles) {
		this.freeRoles = freeRoles;
	}
	
	public List<String> getSubbedRoles() {
		return subbedRoles;
	}
	public void setSubbedRoles(List<String> subbedRoles) {
		this.subbedRoles = subbedRoles;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
	
	
	
	
	public Instance(String name, int maxNumber, int actualNumber, List<String> participants, List<String> roles,
			List<String> freeRoles, List<String> subbedRoles, String createdBy, int id, boolean done) {
		super();
		this.name = name;
		this.maxNumber = maxNumber;
		this.actualNumber = actualNumber;
		this.participants = participants;
		this.roles = roles;
		this.freeRoles = freeRoles;
		this.subbedRoles = subbedRoles;
		this.createdBy = createdBy;
		Id = id;
		this.done = done;
	}
	public Instance() {
		super();
	}
	
	
}
