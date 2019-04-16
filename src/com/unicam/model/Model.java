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
import org.bson.types.ObjectId;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.Type;
import org.hibernate.ogm.datastore.document.options.AssociationStorage;
import org.hibernate.ogm.datastore.document.options.AssociationStorageType;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorage;
import org.hibernate.ogm.datastore.mongodb.options.AssociationDocumentStorageType;

@XmlRootElement
@Entity
@AssociationStorage(AssociationStorageType.ASSOCIATION_DOCUMENT)
@AssociationDocumentStorage(AssociationDocumentStorageType.COLLECTION_PER_ASSOCIATION)
@NamedQuery(name = "Model.findAll", query = "SELECT m FROM Model m")
public class Model {
	 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Type(type = "objectid")
    private String id;
	private String name;
	private int maxNumber;
	private String uploadedBy;
	@ElementCollection(fetch=FetchType.EAGER)
	private List<String> mandatoryRoles;
	@ElementCollection(fetch=FetchType.EAGER)
	private List<String> optionalRoles;
	@OneToMany(targetEntity=Instance.class, fetch = FetchType.EAGER)
	private List<Instance> instances;

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

	public int getMaxNumber() {
		return maxNumber;
	}

	public void setMaxNumber(int maxNumber) {
		this.maxNumber = maxNumber;
	}

	public String getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public List<String> getMandatoryRoles() {
		return mandatoryRoles;
	}

	public void setMandatoryRoles(List<String> mandatoryRoles) {
		this.mandatoryRoles = mandatoryRoles;
	}

	public List<String> getOptionalRoles() {
		return optionalRoles;
	}

	public void setOptionalRoles(List<String> optionalRoles) {
		this.optionalRoles = optionalRoles;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}

	public Model(String name, int maxNumber, String uploadedBy, List<String> mandatoryRoles,
			List<String> optionalRoles, List<Instance> instances) {
		super();
		//_id = iD;
		this.name = name;
		this.maxNumber = maxNumber;
		this.uploadedBy = uploadedBy;
		this.mandatoryRoles = mandatoryRoles;
		this.optionalRoles = optionalRoles;
		this.instances = instances;
	}

	public Model() {
		super();
	}

}
