package com.unicam.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

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
@NamedQuery(name = "ChorchainUser.findAll", query = "SELECT c FROM ChorchainUser c")
@NamedQuery(name="ChorchainUser.findByName", query="SELECT u FROM ChorchainUser u WHERE u.name = :name")
public class ChorchainUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Type(type = "objectid")
    private String id;
    @Column(unique = true)
    private String name;
    private String password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    public ChorchainUser(String name, String password, List<Instance> instances) {
        this.name = name;
        this.password = password;
        this.instances = instances;
    }

    public ChorchainUser() {
        super();
    }
}


