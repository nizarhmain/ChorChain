package com.unicam.model;

import java.util.List;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.bson.Document;

@XmlRootElement
@Entity
public class Instance {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int ID;
	private String name;
	private int actualNumber;
	@OneToMany(targetEntity=User.class, fetch = FetchType.EAGER)
	private Map<String, User> participants;
	@ElementCollection
	private List<String> freeRoles;
	private String createdBy;
	private boolean done;
	@OneToMany(targetEntity=User.class, fetch = FetchType.EAGER)
	private List<User> visibleAt;
	@OneToOne(targetEntity=ContractObject.class, fetch = FetchType.EAGER)
	private ContractObject deployedContract;

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

	public Map<String, User> getParticipants() {
		return participants;
	}

	public void setParticipants(Map<String, User> participants) {
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

	public List<User> getVisibleAt() {
		return visibleAt;
	}

	public void setVisibleAt(List<User> visibleAt) {
		this.visibleAt = visibleAt;
	}

	public ContractObject getDeployedContract() {
		return deployedContract;
	}

	public void setDeployedContract(ContractObject deployedContract) {
		this.deployedContract = deployedContract;
	}

	public Instance(int iD, String name, int actualNumber, Map<String, User> participants, List<String> freeRoles,
			String createdBy, boolean done, List<User> visibleAt, ContractObject deployedContract) {
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
