package com.unicam.model;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

import org.bson.Document;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.ogm.datastore.document.options.AssociationStorage;
import org.hibernate.ogm.datastore.document.options.AssociationStorageType;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorage;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorageType;

@XmlRootElement
@Entity
@AssociationStorage(AssociationStorageType.ASSOCIATION_DOCUMENT)
@AssociationDocumentStorage(AssociationDocumentStorageType.COLLECTION_PER_ASSOCIATION)
@NamedQuery(name = "User.findAll", query = "SELECT t FROM User t")
@NamedQuery(name="User.findByAddress", query="SELECT u FROM User u WHERE u.address = :address")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int _id;
	private String address;
//	@OneToMany(targetEntity=Instance.class, fetch = FetchType.EAGER)
	@OneToMany
	private List<Instance> instances;

	public int getID() {
		return _id;
	}

	public void setID(int iD) {
		_id = iD;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}

	public User(String address, List<Instance> instances) {
		super();
		this.address = address;
		this.instances = instances;
	}

	public User() {
		super();
	}

}


