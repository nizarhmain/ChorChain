package com.unicam.model;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.json.JSONObject;

@Entity
public class ContractObject {
	@Id
	private String ID;
	private String address;
	@ElementCollection
	private List<String> tasksID;
	@ElementCollection
	private List<String> tasks;
	@ElementCollection
	private List<String> taskRoles;
	private String abi;
	private String bin;
	@ElementCollection
	private List<String> varNames;
	
	public List<String> getTaskRoles() {
		return taskRoles;
	}
	public void setTaskRoles(List<String> taskRoles) {
		this.taskRoles = taskRoles;
	}
	public String getID() {
		return ID;
	}
	public void setID(String ID) {
		this.ID = ID;
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
	public ContractObject(String ID, String address, List<String> tasksID, List<String> tasks, List<String> taskRoles,
			String abi, String bin, List<String> varNames) {
		super();
		this.ID = ID;
		this.address = address;
		this.tasksID = tasksID;
		this.tasks = tasks;
		this.taskRoles = taskRoles;
		this.abi = abi;
		this.bin = bin;
		this.varNames = varNames;
	}
	
	
	
}
