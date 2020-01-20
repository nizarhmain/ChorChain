package com.unicam.model;

import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.Type;
import org.hibernate.ogm.datastore.document.options.AssociationStorage;
import org.hibernate.ogm.datastore.document.options.AssociationStorageType;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorage;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorageType;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@Entity
@AssociationStorage(AssociationStorageType.ASSOCIATION_DOCUMENT)
@AssociationDocumentStorage(AssociationDocumentStorageType.COLLECTION_PER_ASSOCIATION)
@NamedQuery(name = "User.findAll", query = "SELECT t FROM User t")
@NamedQuery(name="User.findByAddress", query="SELECT u FROM User u WHERE u.address = :address")
public class User {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Type(type = "objectid")
    private String id;
    @Column(unique = true)
	private String address;
	@OneToMany(targetEntity=Instance.class, fetch = FetchType.EAGER)
	private List<Instance> instances;

	public String getID() {
		return id;
	}

	public void setID(String iD) {
		id = iD;
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


