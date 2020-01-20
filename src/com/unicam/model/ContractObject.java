package com.unicam.model;

import org.hibernate.annotations.Type;
import org.hibernate.ogm.datastore.document.options.AssociationStorage;
import org.hibernate.ogm.datastore.document.options.AssociationStorageType;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorage;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorageType;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;


@Entity
@AssociationStorage(AssociationStorageType.ASSOCIATION_DOCUMENT)
@AssociationDocumentStorage(AssociationDocumentStorageType.COLLECTION_PER_ASSOCIATION)
public class ContractObject {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Type(type = "objectid")
    private String id;
	private String address;
	private String ContractCreationHash;
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> tasks;
	private String abi;
	private String bin;
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> varNames;
	@Lob
	private LinkedHashMap<String, String> taskIdAndRole = new LinkedHashMap<String, String>();

	
	public LinkedHashMap<String, String> getTaskIdAndRole() {
		return taskIdAndRole;
	}
	public void setTaskIdAndRole(LinkedHashMap<String, String> taskIdAndRole) {
		this.taskIdAndRole = taskIdAndRole;
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


    public String getContractCreationHash() {
        return ContractCreationHash;
    }

    public void setContractCreationHash(String contractCreationHash) {
        this.ContractCreationHash = contractCreationHash;
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
	
	public ContractObject(String address, String contractCreationHash, List<String> tasks, String abi, String bin, List<String> varNames,
                          LinkedHashMap<String, String> taskIdAndRole) {
		super();
		this.address = address;
        this.ContractCreationHash = contractCreationHash;
        this.tasks = tasks;
		this.abi = abi;
		this.bin = bin;
		this.varNames = varNames;
		this.taskIdAndRole = taskIdAndRole;
	}
	public ContractObject() {
		super();
	}
	
	
	
	
	
}
