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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import org.web3j.protocol.http.HttpService;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.unicam.model.ContractObject;
import com.unicam.model.Instance;
import com.unicam.model.Model;
import com.unicam.model.TaskObject;
import com.unicam.model.User;
import com.unicam.translator.Choreography;

@Path("/")
public class Controller {
	
	
	
	private User loggedUser;
	//rendere persistente
	private static Map<Integer, User> hashUsers = new HashMap<>();
	HttpSession session;
	MongoClient mongo = new MongoClient("localhost", 27017);
	MongoDatabase db = mongo.getDatabase("EthUsers");

	@POST
	@Path("reg/")
	public String sub(User user) {

		System.out.println(user.getAddress());
		System.out.println(user.getPrivateKey());
		// System.out.println(privateKey);
		MongoCollection<Document> d = db.getCollection("account");
		
		FindIterable<Document> allUsers = d.find();
		int actualUserId = 0;
		if(allUsers!=null) {
			for(Document docUser : allUsers){
				System.out.println("Utente nel db: " + docUser);
				actualUserId = docUser.getInteger("ID");
			}
		}
		
		Document person = new Document();
		person.append("Address", user.getAddress());
		person.append("Private key", user.getPrivateKey());
		person.append("Password", user.getPassword());
		person.append("Contracts", new ArrayList<String>());
		person.append("Roles", new ArrayList<String>());
		person.append("ContractsAddresses", new ArrayList<String>());
		person.append("ID", actualUserId+1);
		d.insertOne(person);
		System.out.println("User in the registration phase: ");
		System.out.println(person);
		
		hashUsers.put(actualUserId+1, new User(user.getAddress(),  user.getPrivateKey(), user.getPassword(),
				new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), actualUserId+1));
		
		return "registered";

	}

	@GET
	@Path("/sel/")
	@Produces(MediaType.TEXT_PLAIN)
	public void ret() {
		MongoCollection<Document> d = db.getCollection("files");
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
		}
	}

	@POST
	@Path("/login/")
	public int login(User user) {
		MongoCollection<Document> d = db.getCollection("account");
		Document person = new Document();
		person.append("Address", user.getAddress());
		person.append("Private key", user.getPrivateKey());
		person.append("Password", user.getPassword());
		// person.append("contracts", new ArrayList<ContractObject>());
		System.out.println("USER PASSED FORM FRONTEND AT THE LOGIN");
		System.out.println(person);
		Document er = d.find(person).first();
		System.out.println("USER founded in the DB ");
		System.out.println(er);
		if (er != null) {
			loggedUser = hashUsers.get(er.getInteger("ID"));
			System.out.println(hashUsers.size());
			System.out.println(hashUsers.get(er.getInteger("ID")));
			
			
		/*	List<String> actualRoles = (List<String>) er.get("roles");
			List<String> actualContracts = (List<String>) er.get("contracts");
			List<String> actualAddresses = (List<String>) er.get("contractsAddresses");
			loggedUser = new User(user.getAddress(), user.getPrivateKey(), user.getPassword(), actualContracts,
					actualRoles, actualAddresses, er.getInteger("ID"));
			System.out.println(er);
			System.out.println(loggedUser.getAddress());
			System.out.println("logged");*/
			System.out.println("ID DELL'UTENTE LOGGATO:" +  loggedUser.getID());
			return loggedUser.getID();
		} else {
			System.out.println("wrong values");
			return -1;
		}
	}

	@POST
	@Path("/upload")
	public String upload(@FormDataParam("cookieId") int cookieId, @FormDataParam("fileName") InputStream uploadedInputStream,
			@FormDataParam("fileName") FormDataContentDisposition fileDetail)
			throws IOException {
		System.out.println(cookieId);
		
		loggedUser = hashUsers.get(cookieId);
		String filepath = ContractFunctions.projectPath + "/bpmn/"
				+ fileDetail.getFileName();
		
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
		MongoCollection<Document> d = db.getCollection("Models");
		Document model = new Document();
		model.append("File_name", fileDetail.getFileName());
		
		model.append("Uploaded_by", loggedUser.getAddress());
		// model.append("Participants", "address");
		model.append("Roles", null);
		// model.append("Free_roles", null);
		List<String> rolesToAdd = new ArrayList<String>();
		Choreography getRoles = new Choreography();
		getRoles.readFile(new File(filepath));
		getRoles.getParticipants();
		List<String> roles = Choreography.participantsWithoutDuplicates;
		// List<Integer> numberToAdd = new ArrayList<Integer>();

		/*
		 * String[] role = roles.split(","); String[] maxR = maxRoles.split(",");
		 * System.out.println(role); System.out.println(maxR); for(String r : role) {
		 * rolesToAdd.add(r); System.out.println(r); } for(String r : maxR) {
		 * System.out.println(r); numberToAdd.add(Integer.parseInt(r)); }
		 */

		model.append("Roles", roles);
		model.append("Max_number", roles.size());
		// model.append("Free_roles", roles);
		// model.append("max_per_role", numberToAdd);
		// aggiungere immagine
		System.out.println(model);
		d.insertOne(model);
		return "<meta http-equiv=\"refresh\" content=\"0; url=http://193.205.92.133:8080/ChorChain/homePage.html\">";
	}

	@POST
	@Path("/getModels")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Model> getAll() {
		MongoCollection<Document> d = db.getCollection("Models");

		FindIterable<Document> c = d.find();

		// List<String> names = new ArrayList<String>();
		// Integer[] numb = new Integer[3];

		List<Model> allModels = new ArrayList<Model>();
		// Model[] allModels = new Model[3];

		int i = 0;
		for (Document document : c) {

			Model model = new Model(document.getString("File_name"), document.getInteger("Max_number"),
					document.getString("Uploaded_by"), (List<String>) document.get("Roles"));
			allModels.add(model);

		}

		return allModels;
	}

	@POST
	@Path("/createInstance/{cookieId}")
	public void createInstance(Model m, @PathParam("cookieId") int cookieId) {
		// Find the model we want to instantiate
		loggedUser = hashUsers.get(cookieId);
		MongoCollection<Document> d = db.getCollection("Models");
		// FindIterable<Document> c = d.find();
		Document toFind = new Document();
		toFind.append("File_name", m.getName());
		FindIterable<Document> er = d.find(toFind);
		Document model = er.first();
		// Insert instance on the DB
		// List<String> participants = new ArrayList<String>();
		// participants.add(address);
		MongoCollection<Document> collection = db.getCollection("Instances");
		// get last id of the instance
		Document lastId = new Document();
		lastId.append("Instance_name", m.getName());
		FindIterable<Document> docs = collection.find(lastId);
		int id = 0;
		for (Document inst : docs) {
			if (inst.getInteger("Id") > id) {
				id = inst.getInteger("Id");
			}
		}
		//
		Document instance = new Document();
		instance.append("Instance_name", m.getName());
		instance.append("Max_number", m.getMaxNumber());
		instance.append("Actual_number", 0);
		instance.append("Participants", new ArrayList<String>());
		instance.append("Roles", m.getRoles());
		instance.append("Free_roles", m.getRoles());
		instance.append("Subbed_roles", new ArrayList<String>());
		instance.append("Created_by", loggedUser.getAddress());
		instance.append("Id", id + 1);
		instance.append("Done", false);
		collection.insertOne(instance);
		System.out.println("istanza alla creazione: " + instance);
	}

	@POST
	@Path("/getInstances/{name}")
	public List<Instance> getAllInstances(@PathParam("name") String name) {
		MongoCollection<Document> d = db.getCollection("Instances");
		Document toFind = new Document();
		toFind.append("Instance_name", name);
		FindIterable<Document> docs = d.find(toFind);

		List<Instance> allModels = new ArrayList<Instance>();

		for (Document document : docs) {
			List<String> participants = (List<String>) document.get("Participants");

			Instance model = new Instance(document.getString("Instance_name"), document.getInteger("Max_number"),
					document.getInteger("Actual_number"), participants, (List<String>) document.get("Roles"),
					(List<String>) document.get("Free_roles"),  (List<String>) document.get("Subbed_roles"), document.getString("Created_by"),
					document.getInteger("Id"), document.getBoolean("Done"));
			allModels.add(model);

		}

		return allModels;

	}

	@POST
	@Path("/subscribe/{role}/{cookieId}")
	public String subscribe(@PathParam("role") String role,  @PathParam("cookieId") int cookieId, Instance instance) {
		loggedUser = hashUsers.get(cookieId);
		MongoCollection<Document> d = db.getCollection("Instances");

		Document toFind = new Document();

		toFind.append("Instance_name", instance.getName());
		// toFind.append("Max_number", instance.getMaxNumber());
		// toFind.append("Actual_number", instance.getActualNumber());
		toFind.append("Id", instance.getId());

		FindIterable<Document> er = d.find(toFind);

		Document toSub = er.first();

		int max = toSub.getInteger("Max_number");
		int actual = toSub.getInteger("Actual_number");
		if (max >= actual + 1) {
			// update roles arrays
			List<String> oldRoles = (List<String>) toSub.get("Free_roles");
			for (String oldRole : oldRoles) {
				if (oldRole.equals(role)) {
					d.deleteOne(toFind);
					// update actual number of subbed users
					toSub.append("Actual_number", actual + 1);
					List<String> addUser = (List<String>) toSub.get("Participants");
					addUser.add(loggedUser.getAddress());
					toSub.append("Participants", addUser);
					oldRoles.remove(oldRole);
					System.out.println("ruolo rimosso: "+oldRole);
					toSub.append("Free_roles", oldRoles);
					//get and update the subbed roles array
					List<String> subbed = (List<String>) toSub.get("Subbed_roles");
					System.out.println("Sto aggiungendo " + role);
					subbed.add(role);
					System.out.println(Arrays.toString(subbed.toArray()));
					toSub.append("Subbed_roles", subbed);
					d.insertOne(toSub);
					//Find the logged user on the DB
					MongoCollection<Document> accounts = db.getCollection("account");
					Document person = new Document();
					person.append("Address", loggedUser.getAddress());
					person.append("Private key", loggedUser.getPrivateKey());
					//person.append("Password", loggedUser.getPassword());
					Document us = accounts.find(person).first();
					System.out.println("USER IN DEPLOYYY: ");
					System.out.println(us);
					//Get the roles of the user and add the new one
					List<String> actualRoles = (List<String>) us.get("Roles");
					System.out.println(role);
						actualRoles.add(role);
					//Get the contracts of the user and add the new one
					List<String> actualContracts = (List<String>) us.get("Contracts");
					actualContracts.add(instance.getName());
					
					//Update the user on the DB and on the server
					accounts.deleteOne(person);
					person.append("Roles", actualRoles);
					person.append("Contracts", actualContracts);
					person.append("ContractsAddresses", us.get("ContractsAddresses"));
					person.append("ID", us.getInteger("ID"));
					accounts.insertOne(person);
					loggedUser.setUserRoles(actualRoles);
					loggedUser.setContracts(actualContracts);

					System.out.println(person);
					
					System.out.println(loggedUser);

					break;
				}

			}
			
			//List<String> actualContracts = (List<String>) us.get("contracts");
			
			return "Subscribe completed!";
		} else {
			return "Max number of participants reached";
		}

	}

	@POST
	@Path("/deploy/{cookieId}")
	public ContractObject deploy(Instance instance, @PathParam("cookieId") int cookieId)
			throws Exception {
		
		loggedUser = hashUsers.get(cookieId);
		MongoCollection<Document> mongoInstances = db.getCollection("Instances");
		Document toFind = new Document();
		toFind.append("Id", instance.getId());
		Document InstanceForDeploy = mongoInstances.find(toFind).first();
		mongoInstances.deleteOne(InstanceForDeploy);
		InstanceForDeploy.append("Done", true);
		
		String path = ContractFunctions.projectPath + "/compiled/";
		
		ContractFunctions contract = new ContractFunctions();
		System.out.println(Arrays.toString(instance.getSubbedRoles().toArray()));
		System.out.println(Arrays.toString(((List<String>) InstanceForDeploy.get("Subbed_roles")).toArray()));
		ContractObject contractReturn = contract.createSolidity(instance.getName(), (List<String>) InstanceForDeploy.get("Subbed_roles"), instance.getParticipants());
		
		System.out.println("Starting to compile...");
		//Thread.sleep(5000);
		contract.compile(instance.getName());
		System.out.println("Compiled");
		//Thread.sleep(10000);
		String cAddress = contract.deploy(instance.getName());
		
		
		contractReturn.setAddress(cAddress);

		contractReturn.setAbi(contract.readLineByLineJava8(path + contract.parseName(instance.getName(), ".abi"), false));
		contractReturn.setBin(contract.readLineByLineJava8(path + contract.parseName(instance.getName(), ".bin"), true));
		//get all the users in the db subscribed to the model
		//and insert the deployed contract address
		MongoCollection<Document> accounts = db.getCollection("account");
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
				if(user.getAddress().equals(participant))
					user.setContractAddresses(addresses);
			}
					}

		// adding the contract deployed on the DB
		MongoCollection<Document> contractColl = db.getCollection("contracts");
		Document deployedCont = new Document();
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

		System.out.println(deployedCont);
		
		
		

		return contractReturn;

	}

	@POST
	@Path("/getCont/{cookieId}")
	public List<ContractObject> getUserContracts(@PathParam("cookieId") int cookieId) {
		System.out.println(cookieId);
		loggedUser = hashUsers.get(cookieId);
		List<String> userCaddress = loggedUser.getContractAddresses();
		List<ContractObject> cList = new ArrayList<>();
		for (String add : userCaddress) {
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
		}
		return cList;

	}

	@POST
	@Path("/setActive/{nextActive}")
	public void setNextActive(@PathParam("nextActive") String next, ContractObject userContract) {

		MongoCollection<Document> contractColl = db.getCollection("contracts");
		Document contractExec = new Document();
		contractExec.append("address", userContract.getAddress());
		Document old = contractColl.find(contractExec).first();
		List<TaskObject> tasks = new ArrayList<TaskObject>();
		tasks = (List<TaskObject>) old.get("tasks");
		/*for (TaskObject t : tasks) {
			if (t.isActive() == true && !t.getName().equals(next)) {
				t.setActive(false);
			}
			if (t.getName().equals(next) && t.isActive() == false) {
				t.setActive(true);
			}
		}*/
		Document newContr = new Document();

	}
	
	
	@POST
	@Path("/getUserInfo/{cookieId}")
	public User getUserInfo(@PathParam("cookieId") int cookieId) {
		loggedUser = hashUsers.get(cookieId);
		System.out.println(loggedUser);
		return loggedUser;
	}
	
	private User getHashUser() {
		return loggedUser;
		
	}
	

}
