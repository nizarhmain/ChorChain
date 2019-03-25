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
	HttpSession session;
	MongoClient mongo = new MongoClient("localhost", 27017);
	MongoDatabase db = mongo.getDatabase("EthUsers");

	public int getLastId(String collection) {
		MongoCollection<Document> d = db.getCollection(collection);

		FindIterable<Document> allElements = d.find();
		int finalId = 0;
		if (allElements != null) {
			for (Document docUser : allElements) {
				finalId = docUser.getInteger("ID");
			}
		}

		return finalId;

	}

	@POST
	@Path("reg/")
	public String sub(User user) {

		MongoCollection<Document> d = db.getCollection("account");

		int actualUserId = getLastId("account");

		Document person = new Document();
		person.append("ID", actualUserId + 1);
		person.append("Address", user.getAddress());
		person.append("Instances", new ArrayList<Document>());

		d.insertOne(person);

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

	public User retrieveUser(int id) {
		MongoCollection<Document> d = db.getCollection("account");
		Document person = new Document();
		person.append("ID", id);

		Document er = d.find(person).first();
		if (er != null)
			return new User(er.getInteger("ID"), er.getString("Address"), (List<Document>) er.get("Instances"));
		else
			return null;

	}

	@POST
	@Path("/login/")
	public int login(User user) {
		MongoCollection<Document> d = db.getCollection("account");
		Document person = new Document();
		person.append("Address", user.getAddress());

		Document er = d.find(person).first();
		if (er != null)
			loggedUser = new User(er.getInteger("ID"), er.getString("Address"), (List<Document>) er.get("Instances"));
		else
			loggedUser = null;

		if (loggedUser != null) {

			return loggedUser.getID();
		} else {
			System.out.println("wrong values");
			return -1;
		}
	}

	@POST
	@Path("/upload")
	public String upload(@FormDataParam("cookieId") int cookieId,
			@FormDataParam("fileName") InputStream uploadedInputStream,
			@FormDataParam("fileName") FormDataContentDisposition fileDetail) throws IOException {

		
		loggedUser = retrieveUser(cookieId);
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
		MongoCollection<Document> d = db.getCollection("Models");
		Document model = new Document();

		model.append("ID", getLastId("Models"));
		model.append("name", fileDetail.getFileName());
		Choreography getRoles = new Choreography();
		getRoles.readFile(new File(filepath));
		getRoles.getParticipants();
		List<String> roles = Choreography.participantsWithoutDuplicates;
		model.append("maxNumber", roles.size());
		model.append("uploadedBy", loggedUser.getAddress());
		model.append("mandatoryRoles", roles);
		model.append("optionalRoles", new ArrayList<String>());
		model.append("instances", new ArrayList<Instance>());
		d.insertOne(model);
		return "<meta http-equiv=\"refresh\" content=\"0; url=http://193.205.92.133:8080/ChorChain/homePage.html\">";
	}

	@POST
	@Path("/getModels")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Model> getAll() {
		MongoCollection<Document> d = db.getCollection("Models");

		FindIterable<Document> c = d.find();
		List<Model> allModels = new ArrayList<Model>();

		for (Document document : c) {
			if(document != null) {
				System.out.println(document.getInteger("ID"));
				System.out.println(document.getString("name"));
				System.out.println(document.getInteger("maxNumber"));
				System.out.println(document.getString("uploadedBy"));
				System.out.println(document.get("mandatoryRoles"));
				System.out.println(document.get("optionalRoles"));
				System.out.println(document.get("instances"));
				Model model = new Model(document.getInteger("ID"), 
					document.getString("name"),
					document.getInteger("maxNumber"), 
					document.getString("uploadedBy"),
					(List<String>) document.get("mandatoryRoles"), (List<String>) document.get("optionalRoles"),
					(List<Document>) document.get("instances"));
				allModels.add(model);
			}
			
		}
		return allModels;
	}

	@POST
	@Path("/createInstance/{cookieId}")
	public void createInstance(Model m, @PathParam("cookieId") int cookieId) {
		// Find the model we want to instantiate
		loggedUser = retrieveUser(cookieId);
		MongoCollection<Document> d = db.getCollection("Models");

		Document toFind = new Document();
		toFind.append("ID", m.getID());
		FindIterable<Document> er = d.find(toFind);
		Document model = er.first();
		List<Document> allModelInstances = (List<Document>) model.get("Instances");
		int lastId = 0;
		for (Document in : allModelInstances) {
			lastId = in.getInteger("ID");
		}
		/*Instance modelInstance = new Instance(lastId, m.getName(), 0, null, m.getMandatoryRoles(),
				loggedUser.getAddress(), false, null, null);*/
		Document modelInstance = new Document();
		modelInstance.append("ID", lastId+1);
		modelInstance.append("Name", m.getName());
		modelInstance.append("Actual_number", 0);
		modelInstance.append("Participants", new HashMap<String, Document>());
		modelInstance.append("Free_roles", m.getMandatoryRoles());
		modelInstance.append("Created_by", loggedUser.getAddress());
		modelInstance.append("Done", false);
		modelInstance.append("Visible_at", new ArrayList<Document>());
		modelInstance.append("Deployed_contract", new Document());
		
		d.deleteOne(model);
		allModelInstances.add(modelInstance);
		model.append("Instances", allModelInstances);
		System.out.println(model);
		d.insertOne(model);
		// Insert instance on the DB

		// MongoCollection<Document> collection = db.getCollection("Instances");
		// get last id of the instance
		// Document lastId = new Document();

		/*
		 * lastId.append("Instance_name", m.getName()); FindIterable<Document> docs =
		 * collection.find(lastId); int id = 0; for (Document inst : docs) { if
		 * (inst.getInteger("Id") > id) { id = inst.getInteger("Id"); } }
		 */
		//
		/*
		 * Document instance = new Document(); instance.append("Instance_name",
		 * m.getName()); instance.append("Max_number", m.getMaxNumber());
		 * instance.append("Actual_number", 0); instance.append("Participants", new
		 * ArrayList<String>()); instance.append("Roles", m.getRoles());
		 * instance.append("Free_roles", m.getRoles()); instance.append("Subbed_roles",
		 * new ArrayList<String>()); instance.append("Created_by",
		 * loggedUser.getAddress()); instance.append("Id", id + 1);
		 * instance.append("Done", false); collection.insertOne(instance);
		 * System.out.println("istanza alla creazione: " + instance);
		 */
	}

	@POST
	@Path("/getInstances/")
	public List<Instance> getAllInstances(Model m) {
		MongoCollection<Document> d = db.getCollection("Models");

		Document toFind = new Document();
		toFind.append("ID", m.getID());
		Document docs = d.find(toFind).first();

		List<Instance> allInstances = new ArrayList<Instance>();
		
		for(Document docuInstance : (List<Document>) docs.get("Instances")) {
			Instance addInstance = new Instance(docuInstance.getInteger("ID"), docuInstance.getString("Name"), 
					docuInstance.getInteger("Actual_number"), (Map<String, 	Document>)docuInstance.get("Participants"), 
					(List<String>)docuInstance.get("Free_roles"), docuInstance.getString("Created_by"), docuInstance.getBoolean("Done"),
					(List<Document>)docuInstance.get("Visible_at"), (Document)docuInstance.get("Deployed_contract"));
			
			allInstances.add(addInstance);
		}
		
		/*
		 * for (Document document : docs) { List<String> participants = (List<String>)
		 * document.get("Participants");
		 * 
		 * Instance model = new Instance(document.getString("Instance_name"),
		 * document.getInteger("Max_number"), document.getInteger("Actual_number"),
		 * participants, (List<String>) document.get("Roles"), (List<String>)
		 * document.get("Free_roles"), (List<String>) document.get("Subbed_roles"),
		 * document.getString("Created_by"), document.getInteger("Id"),
		 * document.getBoolean("Done")); allModels.add(model);
		 * 
		 * }
		 */

		return allInstances;

	}

	@POST
	@Path("/subscribe/{role}/{cookieId}/{instanceID}")
	public String subscribe(@PathParam("role") String role, @PathParam("cookieId") int cookieId,
			@PathParam("instanceID") int instanceId, Model modelInstance) {

		loggedUser = retrieveUser(cookieId);

		MongoCollection<Document> d = db.getCollection("Models");

		Document toFind = new Document();

		toFind.append("ID", modelInstance.getID());

		Document er = d.find(toFind).first();

		List<Document> allModelInstances = (List<Document>) er.get("Instances");

		for(Document docInst : allModelInstances) {
			if(docInst.getInteger("ID") == instanceId) {
				//get the max and the actual number of participants
				int max = modelInstance.getMaxNumber();
				int actual = docInst.getInteger("Actual_number");
				//check if a new subscriber can be added
				if (max >= actual + 1) {
					//increment the actual number of participants
					docInst.append("Actual_number", actual+1);
					//remove the role subscribed from the free roles
					List<String> freeRoles = (List<String>) docInst.get("Free_roles");
					freeRoles.remove(role);
					//update the hashmap of the users subscribed
					Map<String, Document> subscribers = (Map<String, Document>) docInst.get("Participants");
					Document subscriber = new Document();
					subscriber.append("ID", loggedUser.getID());
					subscriber.append("Address", loggedUser.getAddress());
					subscriber.append("Instances", loggedUser.getInstances());
					subscribers.put(role, subscriber);
					
					
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
					d.deleteOne(toFind);
					toFind.append("Instances", allModelInstances);
					d.insertOne(toFind);
					System.out.println("model dopo sub: ");
					System.out.println(toFind);
					return "Subscribe completed";
				}
			}
		}
	
		
		
		
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
		
		List<Instance> allModelInstance = (List<Instance>) modelForDeploy.get("Instances");
		Instance instanceForDeploy = null;
		int index = 0;
		for(Instance inst : allModelInstance) {
			if(inst.getID() == instanceId) {
				instanceForDeploy = inst;
				break;
			}
			index++;
		}
		mongoInstances.deleteOne(modelForDeploy);
		//UNA VOLTA MODIFICATA INSTANZA VA RIAGGIUNTA AL DB DEL MODEL
		
		
		
		
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
		
		allModelInstance.set(index, instanceForDeploy);
		modelForDeploy.append("Instances", allModelInstance);
		mongoInstances.insertOne(modelForDeploy);
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
	@Path("/getCont/{cookieId}/{contractId}")
	public List<ContractObject> getUserContracts(@PathParam("cookieId") int cookieId, @PathParam("contractId") String contractId) {
		System.out.println(cookieId);
		loggedUser = retrieveUser(cookieId);
		
		List<ContractObject> cList = new ArrayList<>();
		//List<Instance> userInstances = loggedUser.getInstances();
		List<Instance> userInstances = null;
		
		for(Instance inst : userInstances) {
			if(inst.getDeployedContract()!=null)
				cList.add(null);
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

		MongoCollection<Document> contractColl = db.getCollection("contracts");
		Document contractExec = new Document();
		contractExec.append("address", userContract.getAddress());
		Document old = contractColl.find(contractExec).first();
		List<TaskObject> tasks = new ArrayList<TaskObject>();
		tasks = (List<TaskObject>) old.get("tasks");
		/*
		 * for (TaskObject t : tasks) { if (t.isActive() == true &&
		 * !t.getName().equals(next)) { t.setActive(false); } if
		 * (t.getName().equals(next) && t.isActive() == false) { t.setActive(true); } }
		 */
		Document newContr = new Document();

	}

	@POST
	@Path("/getUserInfo/{cookieId}")
	public User getUserInfo(@PathParam("cookieId") int cookieId) {
		loggedUser = retrieveUser(cookieId);
		System.out.println(loggedUser);
		return loggedUser;
	}

	private User getHashUser() {
		return loggedUser;

	}

}
