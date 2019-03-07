package com.unicam.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User {
	private String address;
	private String privateKey;
	private String password;
	private List<String> contracts;
	private List<String> userRoles;
	private List<String> contractAddresses;
	private int ID;

	public List<String> getContractAddresses() {
		return contractAddresses;
	}

	public void setContractAddresses(List<String> contractAddresses) {
		this.contractAddresses = contractAddresses;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<String> userRoles) {
		this.userRoles = userRoles;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public List<String> getContracts() {
		return contracts;
	}

	public void setContracts(List<String> contracts) {
		this.contracts = contracts;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public User(String address, String privateKey, String password, List<String> contracts, List<String> userRoles,
			List<String> contractAddresses, int iD) {
		super();
		this.address = address;
		this.privateKey = privateKey;
		this.password = password;
		this.contracts = contracts;
		this.userRoles = userRoles;
		this.contractAddresses = contractAddresses;
		ID = iD;
	}

	public User() {
		super();
	}

}
