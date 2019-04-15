package com.unicam.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.ws.rs.Consumes;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.core.filters.Filter;
import org.web3j.protocol.http.HttpService;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.unicam.model.ContractObject;
import com.unicam.model.Instance;
import com.unicam.model.Model;
import com.unicam.model.TaskObject;
import com.unicam.model.User;
import com.unicam.translator.Choreography;

@Path("/")
public class Controller {

	private User loggedUser;
	HttpSession session;
	
	//accessing JBoss's Transaction can be done differently but this one works nicely
	TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

	//build the EntityManagerFactory as you would build in in Hibernate ORM
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("OGMPU");
		
	//now id is autoincremental, useless function.
/*	public int getLastId(String collection) {
		MongoCollection<Document> d = db.getCollection(collection);

		FindIterable<Document> allElements = d.find();
		int finalId = 0;
		if (allElements != null) {
			for (Document docUser : allElements) {
				finalId = docUser.getInteger("ID");
			}
		}

		return finalId;

	}*/

	@POST
	@Path("reg/")
	public String sub(User user) throws Exception {
		//the constructor is in the form: address, instances
		tm.begin();
		EntityManager em = emf.createEntityManager();
		//User userc = new User();
		//userc.setAddress("thisismyaddress");
		/*Instance ist = new Instance();
		ist.setCreatedBy("createdby field");
		ist.setName("this is the name of the instance");
		em.persist(ist);
		*/
		//ArrayList lista = new ArrayList<Instance>();
		//lista.add(ist);
	//	userc.setInstances(lista);
		em.persist(user);
		em.flush();
		em.close();
		tm.commit();
		//emf.close();

		return "registered";
	}

	@GET
	@Path("/sel/")
	@Produces(MediaType.TEXT_PLAIN)
	public void ret() throws Exception {
		tm.begin();
		EntityManager em = emf.createEntityManager();

		em.flush();
		em.close();
		tm.commit();
		emf.close();
	/*	MongoCollection<Document> d = db.getCollection("files");
		MongoCollection<Document> Q = db.getCollection("Instances");
		MongoCollection<Document> W = db.getCollection("Models");
		MongoCollection<Document> zzz = db.getCollection("account");
		zzz.deleteMany(new Document());
		d.deleteMany(new Document());
		W.deleteMany(new Document());
		Q.deleteMany(new Document());
		FindIterable<Document> c = d.find();
		for (Document document : c) {
			System.out.println(document);
		}*/
	}

	public User retrieveUser(int id) throws Exception {
		tm.begin();
		EntityManager em = emf.createEntityManager();
		User user = em.find(User.class, id);
		em.flush();
		em.close();
		tm.commit();
		//emf.close();
		return user;
	}

	@POST
	@Path("/login/")
	public int login(User user) throws Exception {
		tm.begin();
		EntityManager em = emf.createEntityManager();
		TypedQuery<User> query = em.createNamedQuery("User.findByAddress", User.class);
		try {
		query.setParameter("address", user.getAddress());
		//Query query1 = em.createQuery("select u from User u where address = '"+ user.getAddress()+"'");
		User loggedUser = query.getSingleResult();
		
		    //  User loggedUser = (User) query1.getSingleResult(); 
		      System.out.println(loggedUser);
		      em.flush();
				em.close();
				//emf.close();
		      return loggedUser.getID();    
		  } catch (Exception nre) {
			  System.out.println(nre);
			  em.flush();
				em.close();
				//emf.close();
			  return -1;
		  }
	}

	@POST
	@Path("/upload")
	public String upload(@FormDataParam("cookieId") int cookieId,
			@FormDataParam("fileName") InputStream uploadedInputStream,
			@FormDataParam("fileName") FormDataContentDisposition fileDetail) throws Exception {

		
		try {
			loggedUser = retrieveUser(cookieId);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		String filepath = ContractFunctions.projectPath + "/bpmn/" + fileDetail.getFileName();

		OutputStream outputStream = null;
		int read = 0;
		try {
			byte[] bytes = new byte[1024];
			File to = new File(filepath);
			System.out.println(to.getPath());
			to.createNewFile();
			outputStream = new FileOutputStream(to);
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Choreography getRoles = new Choreography();
		getRoles.readFile(new File(filepath));
		getRoles.getParticipants();
		List<String> roles = Choreography.participantsWithoutDuplicates;
		Model modelUploaded = new Model(fileDetail.getFileName(), roles.size(),
				loggedUser.getAddress(), roles, new ArrayList<String>(), new ArrayList<Instance>());
		
		tm.begin();
		
		EntityManager em = emf.createEntityManager();
		em.persist(modelUploaded);
		em.flush();
		em.close();
		tm.commit();

		return "<meta http-equiv=\"refresh\" content=\"0; url=http://193.205.92.133:8080/ChorChain/homePage.html\">";
	}

	@POST
	@Path("/getModels")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Model> getAll() throws Exception {
		tm.begin();
		EntityManager em = emf.createEntityManager();
		TypedQuery<Model> query = em.createNamedQuery("Model.findAll", Model.class);
		List<Model> allModels = query.getResultList();
		em.flush();
		em.close();
		tm.commit();
		
		return allModels;
	}

	@POST
	@Path("/createInstance/{cookieId}")
	public void createInstance(Model m, @PathParam("cookieId") int cookieId) throws Exception {
		// Find the model we want to instantiate
		//loggedUser = retrieveUser(cookieId);
		tm.begin();
		
		EntityManager em = emf.createEntityManager();
		loggedUser = em.find(User.class, cookieId);
		Model model = em.find(Model.class, m.getID());
		List<Instance> modelInstances = model.getInstances();
		ContractObject deployedContract = new ContractObject();
		Instance modelInstance = new Instance(m.getName(), 0, new HashMap<String, User>(), m.getMandatoryRoles(),
				loggedUser.getAddress(), false, new ArrayList<User>(), deployedContract);
		
		List<Instance> userInstances = loggedUser.getInstances();
		userInstances.add(modelInstance);
		loggedUser.setInstances(userInstances);
		modelInstances.add(modelInstance);
		model.setInstances(modelInstances);
		em.persist(modelInstance);
		em.persist(deployedContract);
		em.getTransaction().commit();
	//	em.merge(model);
		//em.refresh(model);
		em.flush();
		em.close();
		tm.commit();
		
	}

	@POST
	@Path("/getInstances/")
	public List<Instance> getAllInstances(Model m) throws Exception {
		tm.begin();
		EntityManager em = emf.createEntityManager();
		Model model = em.find(Model.class, m.getID());
		List<Instance> allInstances = model.getInstances();
		em.flush();
		em.close();
		tm.commit();
		return allInstances;
	}

	@POST
	@Path("/subscribe/{role}/{cookieId}/{instanceID}")
	public String subscribe(@PathParam("role") String role, @PathParam("cookieId") int cookieId,
			@PathParam("instanceID") int instanceId, Model modelInstance) throws Exception {

		//TO MODIFY : model has to be retrieved from the DB, not from the frontend.
		//delete modelInstance from params and pass only the model id.
		
		tm.begin();
		EntityManager em = emf.createEntityManager();
		
		loggedUser = em.find(User.class, cookieId);
		
		Model modelToSub = em.find(Model.class, modelInstance.getID());
		
		List<Instance> allModelInstances = modelToSub.getInstances();
		
		
		for(Instance instance : allModelInstances) {
			
			if(instance.getID() == instanceId) {
				
				int max = modelToSub.getMaxNumber();
				int actual = instance.getActualNumber();
				if(max >= actual+1) {
					
					instance.setActualNumber(actual+1);
					
					List<String> freeRoles = instance.getFreeRoles();
					freeRoles.remove(role);
					
					Map<String, User> subscribers = instance.getParticipants();
					subscribers.put(role, loggedUser);
					
					List<Instance> userInstances = loggedUser.getInstances();
					for(int i = 0; i < userInstances.size(); i++) {
						if(userInstances.get(i).getID() == instance.getID())
							userInstances.set(i, instance);
					}
					
				}
			}
		
		}
		
		em.getTransaction().commit();
		em.flush();
		em.close();
		tm.commit();
		/*

			if(docInst.getInteger("ID") == instanceId) {
				
				//check if a new subscriber can be added
				if (max >= actual + 1) {
					//increment the actual number of participants

					MongoCollection<Document> accounts = db.getCollection("account");
					Document person = new Document();
					person.append("ID", loggedUser.getID());
					Document us = accounts.find(person).first();

					// Get the instances of the user and add the new one
					List<Document> actualInstances = (List<Document>) us.get("Instances");
					// System.out.println(role);
					actualInstances.add(docInst);
					

					// Update the user on the DB and on the server
					accounts.deleteOne(person);
					person.append("Address", loggedUser.getAddress());
					person.append("Instances", actualInstances);
					accounts.insertOne(person);
					loggedUser.setInstances(actualInstances);
					System.out.println("utente dopo sub: ");
					System.out.println(person);
					
					toFind.append("Instances", allModelInstances);
				
					d.updateOne(Filters.eq("ID", toFind.getInteger("ID")), new Document("$set", new Document("Instances", allModelInstances)));

					System.out.println("model dopo sub: ");
					System.out.println(toFind);
					return "Subscribe completed";
				}
			}
		}*/
	
		
		
		
		/*for (Instance inst : allModelInstances) {	
			if (inst.getID() == instanceId) {
				int max = modelInstance.getMaxNumber();
				int actual = inst.getActualNumber();
				if (max >= actual + 1) {
				    inst.setActualNumber(actual + 1);
					// set participants
					List<String> freeRoles = inst.getFreeRoles();
					freeRoles.remove(role);
					MongoCollection<Document> accounts = db.getCollection("account");
					Document person = new Document();
					person.append("ID", loggedUser.getID());
					Document us = accounts.find(person).first();

					// Get the instances of the user and add the new one
					List<Instance> actualInstances = (List<Instance>) us.get("Instances");
					// System.out.println(role);
					actualInstances.add(inst);
					// Get the contracts of the user and add the new one

					// Update the user on the DB and on the server
					accounts.deleteOne(person);
					person.append("Instances", actualInstances);
					accounts.insertOne(person);
					loggedUser.setInstances(actualInstances);
					
					return "Subscribe completed";
				}
			}
		}*/

		// addUser.add(loggedUser.getAddress());

		

		System.out.println(loggedUser);
		return "Subscribe went wrong";

	}

	

	@POST
	@Path("/deploy/{cookieId}/{instanceID}")
	public ContractObject deploy(Model modelInstance, @PathParam("cookieId") int cookieId, @PathParam("instanceID") int instanceId)
			throws Exception {
		
		loggedUser = retrieveUser(cookieId);
		MongoCollection<Document> mongoInstances = db.getCollection("Models");
		Document toFind = new Document();
		toFind.append("ID", modelInstance.getID());
		Document modelForDeploy = mongoInstances.find(toFind).first();
		
		List<Document> allModelInstance = (List<Document>) modelForDeploy.get("Instances");
		Instance instanceForDeploy = new Instance();
		int index = 0;
		for(Document inst : allModelInstance) {
			if(inst.getInteger("ID") == instanceId) {
				instanceForDeploy =  new Instance(inst.getInteger("ID"), inst.getString("Name"), 
						inst.getInteger("Actual_number"), (Map<String, 	Document>)inst.get("Participants"), 
						(List<String>)inst.get("Free_roles"), inst.getString("Created_by"), inst.getBoolean("Done"),
						(List<Document>)inst.get("Visible_at"), (Document)inst.get("Deployed_contract"));
				break;
			}
			index++;
		}
		
		
		
		String path = ContractFunctions.projectPath + File.separator + "compiled" + File.separator;
		
		ContractFunctions contract = new ContractFunctions();
		
		ContractObject contractReturn = contract.createSolidity(instanceForDeploy.getName(), instanceForDeploy.getParticipants());//settare partecipanti
		
		System.out.println("Starting to compile...");
		//Thread.sleep(5000);
		contract.compile(instanceForDeploy.getName());
		System.out.println("Compiled");
		//Thread.sleep(10000);
		String cAddress = contract.deploy(instanceForDeploy.getName());
		
		
		contractReturn.setAddress(cAddress);

		contractReturn.setAbi(contract.readLineByLineJava8(path + contract.parseName(instanceForDeploy.getName(), ".abi"), false));
		contractReturn.setBin("0x"+contract.readLineByLineJava8(path + contract.parseName(instanceForDeploy.getName(), ".bin"), true));
		//instanceForDeploy.setDeployedContract(contractReturn);
		instanceForDeploy.setDeployedContract(null);
		instanceForDeploy.setDone(true);
		
		Document instToAdd = new Document();
		instToAdd.append("ID", instanceForDeploy.getID());
		instToAdd.append("Name", instanceForDeploy.getName());
		instToAdd.append("Actual_number", instanceForDeploy.getActualNumber());
		instToAdd.append("Participants", instanceForDeploy.getParticipants());
		instToAdd.append("Free_roles", instanceForDeploy.getFreeRoles());
		instToAdd.append("Created_by", loggedUser.getAddress());
		instToAdd.append("Done", true);
		instToAdd.append("Visible_at", instanceForDeploy.getVisibleAt());
		Document docuContract = new Document();
		docuContract.append("ID", instanceForDeploy.getID());
		docuContract.append("Address", contractReturn.getAddress());
		docuContract.append("TasksID", contractReturn.getTasksID());
		docuContract.append("Tasks", contractReturn.getTasks());
		docuContract.append("TaskRoles", contractReturn.getTaskRoles());
		docuContract.append("Abi", contractReturn.getAbi());
		docuContract.append("Bin", contractReturn.getBin());
		docuContract.append("VarNames", contractReturn.getVarNames());
		
		instToAdd.append("Deployed_contract", docuContract);
		
		
	
		allModelInstance.set(index, instToAdd);
		modelForDeploy.append("Instances", allModelInstance);

		
		mongoInstances.updateOne(Filters.eq("ID", instanceForDeploy.getID()), new Document("$set", modelForDeploy));
		
		MongoCollection<Document> accounts = db.getCollection("account");
		Document person = new Document();
		person.append("ID", loggedUser.getID());
		Document us = accounts.find(person).first();
		
		for(Document inst : (List<Document>) us.get("Instances")) {
			if(inst.getInteger("ID") == instanceId) {
				inst =  instToAdd;
				break;
			}
		}
		
		mongoInstances.updateOne(Filters.eq("ID", loggedUser.getID()), new Document("$set", us));
		

		//get all the users in the db subscribed to the model
		//and insert the deployed contract address
		/*MongoCollection<Document> accounts = db.getCollection("account");
		for(String participant : instance.getParticipants()) {
			Document person = new Document();
			person.append("Address", participant);
			Document us = accounts.find(person).first();
			List<String> addresses = (List<String>) us.get("ContractsAddresses");
			accounts.deleteOne(us);
			addresses.add(cAddress);
			us.append("ContractAddresses", addresses);
			accounts.insertOne(us);
			//DA RIVEDERE perche ID probabilmente nullo o sbagliato
			//hashUsers.get(us.getInteger("ID")).setContractAddresses(addresses);
			for (User user : hashUsers.values()) {
				if(user.getAddress().equals(participant)) {
					
				}
					
			}
					}*/

		// adding the contract deployed on the DB
		/*
		deployedCont.append("name", contractReturn.getName());
		deployedCont.append("address", contractReturn.getAddress());
		List<String> t = contractReturn.getTasks();

		deployedCont.append("tasksID", contractReturn.getTasksID());
		deployedCont.append("tasks", contractReturn.getTasks());
		deployedCont.append("taskRoles", contractReturn.getTaskRoles());
		deployedCont.append("abi", contractReturn.getAbi());
		deployedCont.append("bin", contractReturn.getBin());
		deployedCont.append("varNames", contractReturn.getVarNames());
		contractColl.insertOne(deployedCont);
*/
	
		
		
		

		return contractReturn;

	}

	@POST
	@Path("/getCont/{cookieId}/")
	public List<Document> getUserContracts(@PathParam("cookieId") int cookieId) {
		
		loggedUser = retrieveUser(cookieId);
		
		List<Document> cList = new ArrayList<>();
		//List<Instance> userInstances = loggedUser.getInstances();
		List<Document> userInstances = loggedUser.getInstances();
		
		for(Document inst : userInstances) {
			if(inst.get("DeployedContract")!=null)
				cList.add((Document) inst.get("DeployedContract"));
				//cList.add(inst.getDeployedContract());
		}
		/*for (String add : userCaddress) {
			if (add != "") {
				Document getDoc = new Document();
				getDoc.append("address", add);
				MongoCollection<Document> collection = db.getCollection("contracts");
				Document contr = collection.find(getDoc).first();
				System.out.println("contract at get: "+ contr);
				ContractObject userContract = new ContractObject(contr.getString("name"), contr.getString("address"),
						(List<String>)contr.get("tasksID"),(List<String>) contr.get("tasks"), 
						(List<String>) contr.get("taskRoles"),contr.getString("abi"),
						contr.getString("bin"), (List<String>) contr.get("varNames"));
				//ContractObject userContract = new ContractObject(contr.getString("name"), null, null, null, null);
				cList.add(userContract);
			}
		}*/
		return cList;

	}

	@POST
	@Path("/setActive/{nextActive}")
	public void setNextActive(@PathParam("nextActive") String next, ContractObject userContract) {

	}

	@POST
	@Path("/getUserInfo/{cookieId}")
	public User getUserInfo(@PathParam("cookieId") int cookieId) throws Exception {
		loggedUser = retrieveUser(cookieId);
		System.out.println(loggedUser);
		return loggedUser;
	}

	private User getHashUser() {
		return loggedUser;

	}

}
