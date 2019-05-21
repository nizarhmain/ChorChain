package com.unicam.model;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.bson.types.ObjectId;
import org.hibernate.annotations.Type;
import org.hibernate.ogm.datastore.document.options.AssociationStorage;
import org.hibernate.ogm.datastore.document.options.AssociationStorageType;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorage;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorageType;
import org.json.JSONObject;

@Entity
@AssociationStorage(AssociationStorageType.ASSOCIATION_DOCUMENT)
@AssociationDocumentStorage(AssociationDocumentStorageType.COLLECTION_PER_ASSOCIATION)
public class ContractObject {
	 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Type(type = "objectid")
    private String id;
	private String address;
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> tasksID;
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> tasks;
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> taskRoles;
	private String abi;
	private String bin;
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> varNames;
	
	public List<String> getTaskRoles() {
		return taskRoles;
	}
	public void setTaskRoles(List<String> taskRoles) {
		this.taskRoles = taskRoles;
	}
	public String getID() {
		return id;
	}
	public void setID(String ID) {
		this.id = ID;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public List<String> getTasksID() {
		return tasksID;
	}
	public void setTasksID(List<String> tasksID) {
		this.tasksID = tasksID;
	}
	public List<String> getTasks() {
		return tasks;
	}
	public void setTasks(List<String> tasks) {
		this.tasks = tasks;
	}
	public String getAbi() {
		return abi;
	}
	public void setAbi(String abi) {
		this.abi = abi;
	}
	public String getBin() {
		return bin;
	}
	public void setBin(String bin) {
		this.bin = bin;
	}
	
	public List<String> getVarNames() {
		return varNames;
	}
	public void setVarNames(List<String> varNames) {
		this.varNames = varNames;
	}
	public ContractObject(String address, List<String> tasksID, List<String> tasks, List<String> taskRoles,
			String abi, String bin, List<String> varNames) {
		super();
		this.address = address;
		this.tasksID = tasksID;
		this.tasks = tasks;
		this.taskRoles = taskRoles;
		this.abi = abi;
		this.bin = bin;
		this.varNames = varNames;
	}
	public ContractObject() {
		super();
	}
	
	
	
	
	
}
