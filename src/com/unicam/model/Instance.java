package com.unicam.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.hibernate.annotations.Type;
import org.hibernate.ogm.datastore.document.options.AssociationStorage;
import org.hibernate.ogm.datastore.document.options.AssociationStorageType;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorage;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorageType;

import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement
@Entity
@AssociationStorage(AssociationStorageType.ASSOCIATION_DOCUMENT)
@AssociationDocumentStorage(AssociationDocumentStorageType.COLLECTION_PER_ASSOCIATION)
public class Instance {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Type(type = "objectid")
	private String id;
	private String name;
	private int actualNumber;
	@JsonIgnore
	@OneToMany(targetEntity=User.class, fetch = FetchType.EAGER)
	@MapKeyColumn(name="role")
	private Map<String, User> participants = new HashMap<String, User>();
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> freeRoles;
	private String createdBy;
	private boolean done;
	//@OneToMany(targetEntity=User.class, fetch = FetchType.EAGER)
	@ElementCollection(fetch=FetchType.EAGER)
	private List<String> visibleAt;
	@OneToOne
	private ContractObject deployedContract;

	public String getID() {
		return id;
	}

	public void setID(String iD) {
		id = iD;
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

	

	public ContractObject getDeployedContract() {
		return deployedContract;
	}

	public void setDeployedContract(ContractObject deployedContract) {
		this.deployedContract = deployedContract;
	}

	public Instance(String name, int actualNumber, Map<String, User> participants, List<String> freeRoles,
			String createdBy, boolean done, List<String> visibleAt, ContractObject deployedContract) {
		super();
		//_id = _id;
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

	public String toStringInstance() {
		return "Instance [id=" + id + ", name=" + name + ", actualNumber=" + actualNumber + ", participants="
				+ participants + ", freeRoles=" + freeRoles + ", createdBy=" + createdBy + ", done=" + done
				+ ", visibleAt=" + visibleAt + ", deployedContract=" + deployedContract + "]";
	}

	public List<String> getVisibleAt() {
		return visibleAt;
	}

	public void setVisibleAt(List<String> visibleAt) {
		this.visibleAt = visibleAt;
	}

}
