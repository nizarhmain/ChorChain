package com.unicam.rest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
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
import org.bson.types.ObjectId;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.core.filters.Filter;
import org.web3j.protocol.http.HttpService;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
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

	// accessing JBoss's Transaction can be done differently but this one works
	// nicely
	TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

	// build the EntityManagerFactory as you would build in in Hibernate ORM
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("OGMPU");
	

	// now id is autoincremental, useless function.
	/*
	 * public int getLastId(String collection) { MongoCollection<Document> d =
	 * db.getCollection(collection);
	 * 
	 * FindIterable<Document> allElements = d.find(); int finalId = 0; if
	 * (allElements != null) { for (Document docUser : allElements) { finalId =
	 * docUser.getInteger("ID"); } }
	 * 
	 * return finalId;
	 * 
	 * }
	 */

	@POST
	@Path("reg/")
	public String sub(User user) throws Exception {
		String result;
		tm.begin();
		EntityManager em = emf.createEntityManager();
		
		try {
			em.persist(user);
			
			return "Registered";
		} catch (MongoWriteException e) {
			tm.rollback();
			if (e.getCode() == 11000) {
				return "Address already registered";
			} else
				return "Some error occurred";
		}
		finally {
			System.out.println("finito reg");
			tm.commit();
			em.clear();
			em.close();
		}
		
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
		/*
		 * MongoCollection<Document> d = db.getCollection("files");
		 * MongoCollection<Document> Q = db.getCollection("Instances");
		 * MongoCollection<Document> W = db.getCollection("Models");
		 * MongoCollection<Document> zzz = db.getCollection("account");
		 * zzz.deleteMany(new Document()); d.deleteMany(new Document());
		 * W.deleteMany(new Document()); Q.deleteMany(new Document());
		 * FindIterable<Document> c = d.find(); for (Document document : c) {
		 * System.out.println(document); }
		 */
	}

	public User retrieveUser(String id) throws Exception {
		tm.begin();
		EntityManager em = emf.createEntityManager();
		User user = em.find(User.class, id);
		tm.commit();
		em.clear();
		em.close();
		
		return user;
	}

	@POST
	@Path("/login/")
	public String login(User user) throws Exception {
		tm.begin();
		EntityManager em = emf.createEntityManager();
		
		TypedQuery<User> query = em.createNamedQuery("User.findByAddress", User.class);
		try {
			query.setParameter("address", user.getAddress());
			User loggedUser = query.getSingleResult();
			tm.commit();
			return loggedUser.getID();
		} catch (Exception nre) {
			tm.rollback();
			return null;
		}finally {
			em.clear();
			em.close();
		}
	}

	@POST
	@Path("/upload")
	public String upload(@FormDataParam("cookieId") String cookieId,
			@FormDataParam("fileName") InputStream uploadedInputStream,
			@FormDataParam("fileName") FormDataContentDisposition fileDetail) throws Exception {
		//tm.begin();

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
		Model modelUploaded = new Model(fileDetail.getFileName(), loggedUser.getAddress(), roles, new ArrayList<Instance>());
		tm.begin();
		EntityManager em = emf.createEntityManager();
		
		em.persist(modelUploaded);
		tm.commit();
		
		em.close();
		//em.flush();
		
		return "<meta http-equiv=\"refresh\" content=\"0; url=http://193.205.92.133:8080/ChorChain/homePage.html\">";
	}

	@POST
	@Path("/getModels")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Model> getAll() throws Exception {
		List<Model> allModels = new ArrayList<Model>();
		tm.begin();
		EntityManager em = emf.createEntityManager();
		try {
			
			TypedQuery<Model> query = em.createNamedQuery("Model.findAll", Model.class);
			allModels = query.getResultList();
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
		}finally {
			
			em.close();
		}
		return allModels;
		
	}
	
	@POST
	@Path("/createInstance/{cookieId}/{optionalRoles}/{mandatoryRoles}/{visibleAt}")
	public void createInstance(Model m, @PathParam("cookieId") String cookieId, @PathParam("optionalRoles") List<String> optionalRoles,
		@PathParam("mandatoryRoles") List<String> mandatoryRoles, @PathParam("visibleAt") List<String> visibleAt) throws Exception {
		
		tm.begin();
		EntityManager em = emf.createEntityManager();
		try {
			
			loggedUser = em.find(User.class, cookieId);
			
			//if visibleAt is null, it means that the instance is public, otherwise I need to insert also
			//the user address into the array
			if(visibleAt.get(0).equals("null")) {
				visibleAt = null;
			} else {
				visibleAt.add(loggedUser.getAddress());
			}
			
			if(optionalRoles.get(0).equals("null")) {
				optionalRoles = null;
			}
			
			
			Model model = em.find(Model.class, m.getID());
			List<Instance> modelInstances = model.getInstances();
			ContractObject deployedContract = new ContractObject();
			//List<String> visibleAt = new ArrayList<String>();
			Instance modelInstance = new Instance(m.getName(), 0, mandatoryRoles.size(), new HashMap<String, User>(), mandatoryRoles, optionalRoles,
					mandatoryRoles, optionalRoles, loggedUser.getAddress(), false,  visibleAt, deployedContract);

			
			modelInstances.add(modelInstance);
			model.setInstances(modelInstances);
			
			em.persist(deployedContract);
			em.persist(modelInstance);
			
			//List<Instance> userInstances = loggedUser.getInstances();
			//userInstances.add(modelInstance);
			//loggedUser.setInstances(userInstances);
			em.close();
			tm.commit();

		} catch (Exception e) {
			  tm.rollback();
			  e.printStackTrace();
		}
		

		//tm.commit();
		//System.out.println("transaction after create inst" + tm.getTransaction());

	}

	@POST
	@Path("/getInstances/")
	public List<Instance> getAllInstances(Model m) throws Exception {
		List<Instance> allInstances = null;
		try {
			
			EntityManager em = emf.createEntityManager();
			Model model = em.find(Model.class, m.getID());
			System.out.println(model.toString());
			allInstances = model.getInstances();
			
			System.out.println(allInstances);
			em.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return allInstances;
	}

	@POST
	@Path("/subscribe/{role}/{cookieId}/{instanceID}")
	public String subscribe(@PathParam("role") String role, @PathParam("cookieId") String cookieId,
			@PathParam("instanceID") String instanceId, Model modelInstance) throws Exception {

		// TO MODIFY : model has to be retrieved from the DB, not from the frontend.
		// delete modelInstance from params and pass only the model id.

		tm.begin();
		EntityManager em = emf.createEntityManager();
		
		try {
			
			loggedUser = em.find(User.class, cookieId);
	
			Instance instanceToSub = em.find(Instance.class, instanceId);
			
			int max = instanceToSub.getMaxNumber();
			int actual = instanceToSub.getActualNumber();
			System.out.println(max);
			System.out.println(actual);
			if (max >= actual + 1) {
				instanceToSub.setActualNumber(actual + 1);
				List<String> freeRoles = instanceToSub.getFreeRoles();
				freeRoles.remove(role);
				Map<String, User> subscribers = instanceToSub.getParticipants();
				subscribers.put(role, loggedUser);
				em.merge(instanceToSub);
				
				List<Instance> userInstances = loggedUser.getInstances();
				userInstances.add(instanceToSub);
				loggedUser.setInstances(userInstances);
				
				tm.commit();
				System.out.println(instanceToSub.toStringInstance());
			}
			
		} catch (Exception e) {
			 tm.rollback();
			e.printStackTrace();
		} finally {
			em.close();
			
		}

		//tm.commit();
		//System.out.println("tm status after subscribe" + tm.getStatus());

		System.out.println(loggedUser);
		return "Subscribe went wrong";

	}
	
	@POST
	@Path("/deploy/{cookieId}/{instanceID}")
	public ContractObject deploy(Model modelInstance, @PathParam("cookieId") String cookieId,
			@PathParam("instanceID") String instanceId) throws Exception {
		ContractObject contractReturn = new ContractObject();
		tm.begin();
		EntityManager em = emf.createEntityManager();
		try {
			loggedUser = em.find(User.class, cookieId);

			Instance instanceForDeploy = em.find(Instance.class, instanceId);

			

			String path = ContractFunctions.projectPath + File.separator + "resources" + File.separator;//modificare compiled con resources

			ContractFunctions contract = new ContractFunctions();

			contractReturn = instanceForDeploy.getDeployedContract();

			contractReturn = contract.createSolidity(instanceForDeploy.getName(), instanceForDeploy.getParticipants(), instanceForDeploy.getOptionalRoles(), instanceForDeploy.getMandatoryRoles());

			System.out.println("Starting to compile...");
			// Thread.sleep(5000);
			contract.compile(instanceForDeploy.getName());
			System.out.println("Compiled");
			// Thread.sleep(10000);
			String cAddress = contract.deploy(instanceForDeploy.getName());
			if(cAddress.equals("ERROR")) {
				tm.rollback();
				return null;
			}

			contractReturn.setAddress(cAddress);

			contractReturn.setAbi(
					contract.readLineByLineJava8(path + contract.parseName(instanceForDeploy.getName(), ".abi"), false));
			contractReturn.setBin("0x"
					+ contract.readLineByLineJava8(path + contract.parseName(instanceForDeploy.getName(), ".bin"), true));
			// instanceForDeploy.setDeployedContract(contractReturn);
			instanceForDeploy.setDeployedContract(contractReturn);
			instanceForDeploy.setDone(true);
			em.persist(contractReturn);
			em.merge(instanceForDeploy);
			tm.commit();
			em.close();
			
			
			
		}catch(Exception e){
			tm.rollback();
			e.printStackTrace();
		}finally {
			return contractReturn;
		}
	}

	@POST
	@Path("/getCont/{cookieId}/")
	public List<Instance> getUserContracts(@PathParam("cookieId") String cookieId) {

		try {
			loggedUser = retrieveUser(cookieId);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		List<Instance> cList = new ArrayList<>();
		List<Instance> userInstances = loggedUser.getInstances();
		for(Instance i : userInstances) {
			if(i.isDone() == true) cList.add(i);
		}

		return cList;

	}
	
	@POST
	@Path("/getPart/{instanceId}/")
	public Map<String, String> getInstParticipants(@PathParam("instanceId") String instanceId) throws Exception {
		EntityManager em = emf.createEntityManager();
		Map<String, String> participants = new HashMap<String, String>();

		try {
			Instance inst = em.find(Instance.class, instanceId);
			Map<String,User> map = inst.getParticipants();
			for(Map.Entry<String, User> sub : map.entrySet()) {
				participants.put(sub.getKey(), sub.getValue().getID());
			}
			em.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		} 
		return participants;
	}

	

	@POST
	@Path("/getUserInfo/{cookieId}")
	public User getUserInfo(@PathParam("cookieId") String cookieId) throws Exception {
		loggedUser = retrieveUser(cookieId);
		System.out.println(loggedUser);
		return loggedUser;
	}


	
	@POST
	@Path("/saveModel/{fileName}")
	public void saveModel(@PathParam("fileName") String filename, String xml) throws Exception {
		//File uploaded = new File(ContractFunctions.projectPath +  File.separator + "bpmn"+  File.separator + filename + ".bpmn");
		FileWriter wChor = new FileWriter(new File(ContractFunctions.projectPath +  File.separator + "bpmn"+  File.separator + filename + ".bpmn"));
		System.out.println("MA RIESCO A ENTRARE????");
		BufferedWriter bChor = new BufferedWriter(wChor);
		bChor.write(xml);
		bChor.flush();
		bChor.close();
		tm.begin();
		EntityManager em = emf.createEntityManager();
		try {
			System.out.println("MA SONO DENTRO AL TRY");
			Choreography getRoles = new Choreography();
			getRoles.readFile(new File(ContractFunctions.projectPath +  File.separator + "bpmn"+  File.separator + filename + ".bpmn"));
			System.out.println("finito readModel");
			getRoles.getParticipants();
			System.out.println("finito get roles");
			List<String> roles = Choreography.participantsWithoutDuplicates;
			System.out.println("finito participants");
			Model modelUploaded = new Model(filename, loggedUser.getAddress(), roles, new ArrayList<Instance>());
			System.out.println(modelUploaded);
			em.persist(modelUploaded);
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
		}finally {
			em.close();
		}
	}
	
	@POST
	@Path("/getXml/{fileName}")
	public String getxml(@PathParam("fileName") String fileName) {
		String xml = "";
		try {
			xml = new String(Files.readAllBytes(Paths.get(ContractFunctions.projectPath + File.separator + "bpmn"+  File.separator + fileName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return xml;
	}
	
	@GET
	@Path("/requestNewXml/")
	public String newXml() {
		String xml = "";
		try {
			xml = new String(Files.readAllBytes(Paths.get(ContractFunctions.projectPath + File.separator + "bpmn"+  File.separator + "newDiagram.bpmn")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return xml;
	}
	
	@GET
	@Path("/changeOptional/{role}/{instanceId}/{modelId}")
	public void changeOptionalRole(@PathParam("role") String optionalRole, @PathParam("instanceId") String instanceId, 
			 @PathParam("modelId") String modelId) throws Exception {
		tm.begin();
		EntityManager em = emf.createEntityManager();
		System.out.println(optionalRole + instanceId);
		try {
			Instance actualInstance = em.find(Instance.class, instanceId);
			List<String> mandatory = actualInstance.getFreeRoles();
			mandatory.remove(optionalRole);
		
			List<String> optional = actualInstance.getFreeRolesOptional();
			optional.add(optionalRole);
			//Model actualModel = em.find(Model.class, modelId);
			actualInstance.setMaxNumber(mandatory.size());
			//em.merge(actualModel);
			em.merge(actualInstance);
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
		}finally {
			em.close();
		}
	}
	
	@POST
	@Path("/getContractFromInstance/{instanceId}")
	public ContractObject getContractFromInstance(@PathParam("instanceId") String instanceId) {
		ContractObject contract = new ContractObject();
		EntityManager em = emf.createEntityManager();
		Instance instance = em.find(Instance.class, instanceId);
		try {
			contract = instance.getDeployedContract();
			em.close();
		}
		catch(Exception e) {
			
			e.printStackTrace();
		} 
		return contract;
		
	}
	
	@POST
	@Path("/newSubscribe/{instanceId}/{role}/{cookieId}")
	public void newSubscribe(@PathParam("instanceId") String instanceId, @PathParam("role") String role, 
			@PathParam("cookieId") String cookieId) throws Exception {
		tm.begin();
		EntityManager em = emf.createEntityManager();
		//loggedUser = retrieveUser(cookieId);
		loggedUser = em.find(User.class, cookieId);
		Instance instance = em.find(Instance.class, instanceId);
		try {
			//List<String> optionalRoles = instance.getFreeRoles();
			//optionalRoles.remove(role);
			Map<String, User> subscribers = instance.getParticipants();
			subscribers.put(role, loggedUser);
			em.merge(instance);
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
		}finally {
			em.close();
		}
	}

}
