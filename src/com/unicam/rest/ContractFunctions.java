package com.unicam.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ser.Serializers;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.besu.response.privacy.PrivateTransactionReceipt;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.eea.Eea;
import org.web3j.protocol.eea.crypto.PrivateTransactionDecoder;
import org.web3j.protocol.eea.crypto.PrivateTransactionEncoder;
import org.web3j.protocol.eea.crypto.RawPrivateTransaction;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.BesuPrivacyGasProvider;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.PollingPrivateTransactionReceiptProcessor;
import org.web3j.utils.Base64String;
import org.web3j.utils.Numeric;

import com.unicam.model.ContractObject;
import com.unicam.model.Parameters;
import com.unicam.model.User;
//import com.unicam.resources.Choreography.InfoNextEventResponse;
import com.unicam.translator.Choreography;

import io.reactivex.Flowable;



import org.bson.Document;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.Type;
import org.web3j.tuples.*;
import org.web3j.tuples.generated.*;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Restriction;

import static org.web3j.utils.Restriction.RESTRICTED;

public class ContractFunctions {

	//private String address;
	//private String privateKey;
	//private String fileName;
	private List<String> participants;
	public List<String> tasks;
	public List<ContractObject> allFunctions;
	public String CONTRACT_ADDRESS = "";
	private static final String VirtualProsAccount = "0x76aE023f51f19b0F3c001aA54951d217dc90FFa6";
	private static final String unlockedEthSignAcc = "0xFE3B557E8Fb62b89F4916B721be55cEb828dBd73";



	
	public static boolean pendingTransaction = false;

	public static String projectPath = "/home/nizapizza/uni/ChorChain/src/com/unicam";

	// public static String projectPath = System.getenv("ChorChain"); 

	String rpc_endpoint = "http://localhost:8545";
	String signer_proxy = "http://localhost:18545";

	String besu_node1_url = "http://localhost:20000";

	Web3j web3j = Web3j.build(new HttpService(rpc_endpoint));
	Web3j ethsigner = Web3j.build(new HttpService(signer_proxy));

	Besu eea = Besu.build(new HttpService(besu_node1_url));

	Admin adm = Admin.build(new HttpService(rpc_endpoint));


	public ContractObject createSolidity(String fileName, Map<String, User> participants, List<String> freeRoles, List<String> mandatoryRoles) {
		Choreography cho = new Choreography();
		File f = new File(projectPath + File.separator + "bpmn"+ File.separator + fileName);
		try {
			//System.out.println(f.getAbsolutePath());
			cho.start(f, participants, freeRoles, mandatoryRoles);
			//allFunctions = cho.allFunctions;

			//Thread.sleep(15000);
			// System.out.print(cho.tasks);
		} catch (Exception e) {
			tasks = null;
			e.printStackTrace();
		}
		return cho.finalContract;
	}
	
	
	public void compile(String fileName) {
		try {
			//System.out.println(getServletContext().getInitParameter("upload.location"));
		
			 String fin = parseName(fileName, ".sol");

			String solPath = projectPath + File.separator + "resources" + File.separator + fin;
			//System.out.println("Solidity PATTT: " + solPath);
			String destinationPath = projectPath +  File.separator + "resources";//sostituire compiled a resources
			//System.out.println("destination path "+destinationPath);
			String[] comm = { "solcjs", solPath, "--bin", "--abi", "--overwrite", "-o", destinationPath };

			// String comm = "solc " + solPath + " --bin --abi -o" + destinationPath;

			Runtime rt = Runtime.getRuntime();

			Process p = rt.exec(comm);
			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			while ((line = bri.readLine()) != null) {
			     System.out.println(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
			     System.out.println(line);
			}
			bre.close();
			p.waitFor();
			  
			
			//System.out.println("abi-bin done");

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public Credentials getCredentialFromPrivateKey(String privateKey) throws IOException, CipherException {
		// return WalletUtils.loadCredentials("andrea",
		// "src/main/resources/UTC--2018-12-06T16-44-54.114315504Z--19a3f868355394b5423106fb31f201da646139af");
		return Credentials.create(privateKey);
	}
	public static String parseName(String name, String extension) {
		String[] oldName = name.split("\\.");
		String newName = oldName[0] + extension;
		return newName;
	}


	public static String parseNameNoExtension(String name, String extension) {
		String[] oldName = name.split("\\.");
		String newName = oldName[0];
		return newName;
	}


	public String reflection(String toExec, String role) {
		String finalName = "";
		//System.out.println("sobo dentro al metodo");

		try {
			Class c = Class.forName("com.unicam.resources.Abstract");

			Method methods[] = c.getDeclaredMethods();

			Credentials credentials;

			credentials = WalletUtils.loadCredentials("123",
					projectPath + "/ChorChain/src/com/unicam/resources/UTC--2019-01-16T15-25-24.286179700Z--1adc6ea9d2ddc4dcb45bfc36f01ca8e266026155");
			//credentials = getCredentialFromPrivateKey("02D671CA1DC73973ED1E8FB53AA73235CC788DA792E41DB4170616EDED86D23D");
			

			RemoteCall returnv;

			// controllo l'array contenente i metodi per cercare il deploy
			for (Method method : methods) {
				if (method.getName() == "deploy" && toExec == "deploy") {

					Parameter[] params = method.getParameters();

					if (params.length == 3
							&& !params[1].getType().toString().equals(TransactionManager.class.toString())) {

						Object arglist[] = new Object[3];
						arglist[0] = web3j;
						arglist[1] = credentials;
						arglist[2] = new DefaultGasProvider();
						//System.out.println(arglist.length);
						returnv = (RemoteCall) method.invoke(c, arglist);
						// invio la remote call generata dal deploy
						Object address = returnv.send();
						CONTRACT_ADDRESS = ((Contract) address).getContractAddress();

						//System.out.println("Contract deployed at --> " + CONTRACT_ADDRESS + "<--");
						return null;

					}
				} else if (method.getName() == "subscribe_as_participant" && toExec == "subscribe_as_participant") {
					Class[] parameterTypes = new Class[4];
					parameterTypes[0] = String.class;
					parameterTypes[1] = Web3j.class;
					parameterTypes[2] = Credentials.class;
					parameterTypes[3] = ContractGasProvider.class;

					Method loadContract = c.getMethod("load", parameterTypes);
					Contract contract = (Contract) loadContract.invoke(c, CONTRACT_ADDRESS, web3j, credentials,
							new DefaultGasProvider());

					// com.unicam.resources.Choreography contract =
					// com.unicam.resources.Choreography.load(CONTRACT_ADDRESS, web3j, credentials,
					// new DefaultGasProvider());

					Object arglist[] = new Object[2];
					arglist[0] = role;
					arglist[1] = new BigInteger("0");
					RemoteCall<TransactionReceipt> returnv1 = (RemoteCall<TransactionReceipt>) method.invoke(contract,
							arglist);
					TransactionReceipt t = returnv1.send();
				    if (t != null) {

						Class[] parameter = new Class[1];
						parameter[0] = TransactionReceipt.class;
						Method getEvent = c.getMethod("getInfoEvents", parameter);

						List<Object> events = (List<Object>) getEvent.invoke(contract, t);
						for (Object e : events) {
							Field fi = e.getClass().getDeclaredField("next");
							//System.out.println(fi.get(e));
							finalName = (String) fi.get(e);
							return finalName;
							/*for (ContractObject co : allFunctions) {
								if(co.getName() == finalName) {
									return co;
								}
							}*/
						
						}
					}
					return null;

				}

			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;

	}

	public String readLineByLineJava8(String filePath, boolean bin) {
		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			if(bin)
				stream.forEach(s -> contentBuilder.append(s));
			else
				stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

	
	public String deploy(String bin) throws Exception {
		//  if(pendingTransaction == true) {
		//	  System.out.println("C'� una transazione pendente");
		//	  return "ERROR";
		 // }


		String underscore_file_path = projectPath.replace('/', '_') ;

		String binar = new String ( Files.readAllBytes( Paths.get(projectPath + "/resources/" + underscore_file_path + "_resources_" + parseNameNoExtension(bin, ".bin") + "_sol_" + parseNameNoExtension(bin, ".bin") + ".bin" )));
		
		  //Unlocking the account
		   // PersonalUnlockAccount personalUnlockAccount = adm.personalUnlockAccount(VirtualProsAccount, "123nizarhmain").send();
		  //Getting the nonce



        // the following statement is usßed to log any messages

		  BigInteger GAS_PRICE = BigInteger.valueOf(13_500_000_000L);
		  BigInteger GAS_LIMIT = BigInteger.valueOf(9_000_000L);

		Base64String orion_node_1 = Base64String.wrap("A1aVtMxLCUHmBVHXoZzzBgPbW/wj5axDpW9X8l91SGo=");
		Base64String orion_node_2 = Base64String.wrap("Ko2bVqD+nNlNYL5EE7y3IdOnviftjiizpjRt+HTuFBs=");

		List<Base64String> privateForList = new ArrayList<Base64String>();
		privateForList.add(orion_node_1);
		privateForList.add(orion_node_2);

		String privKeynode1 = "8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63";

		BesuPrivacyGasProvider ZERO_GAS_PROVIDER =
				new BesuPrivacyGasProvider(BigInteger.valueOf(0));

		String weird = "60c0604052600460808190527f48302e310000000000000000000000000000000000000000000000000000000060a090815261003e91600691906100d0565b5034801561004b57600080fd5b506040516109ab3803806109ab8339810160409081528151602080840151838501516060860151336000908152600185529586208590559484905590850180519395909491939101916100a3916003918601906100d0565b506004805460ff191660ff841617905580516100c69060059060208401906100d0565b505050505061016b565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061011157805160ff191683800117855561013e565b8280016001018555821561013e579182015b8281111561013e578251825591602001919060010190610123565b5061014a92915061014e565b5090565b61016891905b8082111561014a5760008155600101610154565b90565b6108318061017a6000396000f3006080604052600436106100955763ffffffff60e060020a60003504166306fdde0381146100a7578063095ea7b31461013157806318160ddd1461016957806323b872dd14610190578063313ce567146101ba57806354fd4d50146101e557806370a08231146101fa57806395d89b411461021b578063a9059cbb14610230578063cae9ca5114610254578063dd62ed3e146102bd575b3480156100a157600080fd5b50600080fd5b3480156100b357600080fd5b506100bc6102e4565b6040805160208082528351818301528351919283929083019185019080838360005b838110156100f65781810151838201526020016100de565b50505050905090810190601f1680156101235780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561013d57600080fd5b50610155600160a060020a0360043516602435610372565b604080519115158252519081900360200190f35b34801561017557600080fd5b5061017e6103d9565b60408051918252519081900360200190f35b34801561019c57600080fd5b50610155600160a060020a03600435811690602435166044356103df565b3480156101c657600080fd5b506101cf6104cc565b6040805160ff9092168252519081900360200190f35b3480156101f157600080fd5b506100bc6104d5565b34801561020657600080fd5b5061017e600160a060020a0360043516610530565b34801561022757600080fd5b506100bc61054b565b34801561023c57600080fd5b50610155600160a060020a03600435166024356105a6565b34801561026057600080fd5b50604080516020600460443581810135601f8101849004840285018401909552848452610155948235600160a060020a031694602480359536959460649492019190819084018382808284375094975061063f9650505050505050565b3480156102c957600080fd5b5061017e600160a060020a03600435811690602435166107da565b6003805460408051602060026001851615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561036a5780601f1061033f5761010080835404028352916020019161036a565b820191906000526020600020905b81548152906001019060200180831161034d57829003601f168201915b505050505081565b336000818152600260209081526040808320600160a060020a038716808552908352818420869055815186815291519394909390927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925928290030190a35060015b92915050565b60005481565b600160a060020a038316600090815260016020526040812054821180159061042a5750600160a060020a03841660009081526002602090815260408083203384529091529020548211155b80156104365750600082115b156104c157600160a060020a03808416600081815260016020908152604080832080548801905593881680835284832080548890039055600282528483203384528252918490208054879003905583518681529351929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a35060016104c5565b5060005b9392505050565b60045460ff1681565b6006805460408051602060026001851615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561036a5780601f1061033f5761010080835404028352916020019161036a565b600160a060020a031660009081526001602052604090205490565b6005805460408051602060026001851615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561036a5780601f1061033f5761010080835404028352916020019161036a565b3360009081526001602052604081205482118015906105c55750600082115b156106375733600081815260016020908152604080832080548790039055600160a060020a03871680845292819020805487019055805186815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a35060016103d3565b5060006103d3565b336000818152600260209081526040808320600160a060020a038816808552908352818420879055815187815291519394909390927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925928290030190a383600160a060020a031660405180807f72656365697665417070726f76616c28616464726573732c75696e743235362c81526020017f616464726573732c627974657329000000000000000000000000000000000000815250602e019050604051809103902060e060020a9004338530866040518563ffffffff1660e060020a0281526004018085600160a060020a0316600160a060020a0316815260200184815260200183600160a060020a0316600160a060020a03168152602001828051906020019080838360005b8381101561077f578181015183820152602001610767565b50505050905090810190601f1680156107ac5780820380516001836020036101000a031916815260200191505b509450505050506000604051808303816000875af19250505015156107d057600080fd5b5060019392505050565b600160a060020a039182166000908152600260209081526040808320939094168252919091522054905600a165627a7a723058203f2de808df5359509254dc2a0d616b226de2b64f0bf28bae7323aeba4487199b0029";

		Credentials orion1_priv_key = Credentials.create("8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");

		// Build new privacy group using the create API
		final Base64String privacyGroupId =
				eea.privCreatePrivacyGroup(
						Arrays.asList(
								orion_node_1, orion_node_2),
						"AliceBobCharlie",
						"AliceBobCharlie group")
						.send()
						.getPrivacyGroupId();

		final BigInteger nonce =
				eea
						.privGetTransactionCount(orion1_priv_key.getAddress(), privacyGroupId)
						.send()
						.getTransactionCount();

		RawPrivateTransaction privTrac = RawPrivateTransaction.createContractTransaction(
				nonce,
				ZERO_GAS_PROVIDER.getGasPrice(),
				ZERO_GAS_PROVIDER.getGasLimit(),
				weird,
                orion_node_1,
			 	privacyGroupId,
				Restriction.RESTRICTED
				);

		// String signed_hexString = Numeric.toHexString(PrivateTransactionEncoder.signMessage(privateTransaction, Credentials.create(privKeynode1)));
		// String qwe = "qwe";


		// EthSendTransaction transactionResponse = eea.eeaSendRawTransaction(signed_hexString).sendAsync().get();

	try {

		PrivateTransactionEncoder.signMessage(privTrac,  orion1_priv_key);

		final String signedTransactionData =
				Numeric.toHexString(
						PrivateTransactionEncoder.signMessage(privTrac, 2018, orion1_priv_key));

		  //send sync
		 final String transactionHash = eea.eeaSendRawTransaction(signedTransactionData).send().getTransactionHash();


		final PollingPrivateTransactionReceiptProcessor receiptProcessor =
				new PollingPrivateTransactionReceiptProcessor(eea, 1 * 1000, 120);
		final PrivateTransactionReceipt receipt =
				receiptProcessor.waitForTransactionReceipt(transactionHash);

		  //Optional<TransactionReceipt> receiptOptional = transactionReceipt.getTransactionReceipt();
		  //for (int i = 0; i < 222220; i++) {
	       //     if (!transactionReceipt.getTransactionReceipt().isPresent()) {
	                //Thread.sleep(5000);
	        //        transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
	      		    
	     //       } else {
	       //         break;
	         //   }
		  //}

		return receipt.getContractAddress();
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return "fail";
	}

		  //TransactionReceipt transactionReceiptFinal = transactionReceipt.getTransactionReceipt().get();


		// logger.info(transactionReceipt.getError().toString());
		// logger.info(transactionReceipt.getRawResponse());
		  //System.out.println(transactionReceiptFinal.getContractAddress());
		  
		  //String contractAddress = transactionReceiptFinal.getContractAddress();
		  //logger.info(contractAddress);
		  // pendingTransaction = false;
		  //System.out.println(contractAddress);
		  //return contractAddress;


		
	}


	public void testMethod() {

		// FIXME: This should be made public in the contract wrapper
		final String HUMAN_STANDARD_TOKEN_BINARY =
				"60c0604052600460808190527f48302e310000000000000000000000000000000000000000000000000000000060a090815261003e91600691906100d0565b5034801561004b57600080fd5b506040516109ab3803806109ab8339810160409081528151602080840151838501516060860151336000908152600185529586208590559484905590850180519395909491939101916100a3916003918601906100d0565b506004805460ff191660ff841617905580516100c69060059060208401906100d0565b505050505061016b565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061011157805160ff191683800117855561013e565b8280016001018555821561013e579182015b8281111561013e578251825591602001919060010190610123565b5061014a92915061014e565b5090565b61016891905b8082111561014a5760008155600101610154565b90565b6108318061017a6000396000f3006080604052600436106100955763ffffffff60e060020a60003504166306fdde0381146100a7578063095ea7b31461013157806318160ddd1461016957806323b872dd14610190578063313ce567146101ba57806354fd4d50146101e557806370a08231146101fa57806395d89b411461021b578063a9059cbb14610230578063cae9ca5114610254578063dd62ed3e146102bd575b3480156100a157600080fd5b50600080fd5b3480156100b357600080fd5b506100bc6102e4565b6040805160208082528351818301528351919283929083019185019080838360005b838110156100f65781810151838201526020016100de565b50505050905090810190601f1680156101235780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561013d57600080fd5b50610155600160a060020a0360043516602435610372565b604080519115158252519081900360200190f35b34801561017557600080fd5b5061017e6103d9565b60408051918252519081900360200190f35b34801561019c57600080fd5b50610155600160a060020a03600435811690602435166044356103df565b3480156101c657600080fd5b506101cf6104cc565b6040805160ff9092168252519081900360200190f35b3480156101f157600080fd5b506100bc6104d5565b34801561020657600080fd5b5061017e600160a060020a0360043516610530565b34801561022757600080fd5b506100bc61054b565b34801561023c57600080fd5b50610155600160a060020a03600435166024356105a6565b34801561026057600080fd5b50604080516020600460443581810135601f8101849004840285018401909552848452610155948235600160a060020a031694602480359536959460649492019190819084018382808284375094975061063f9650505050505050565b3480156102c957600080fd5b5061017e600160a060020a03600435811690602435166107da565b6003805460408051602060026001851615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561036a5780601f1061033f5761010080835404028352916020019161036a565b820191906000526020600020905b81548152906001019060200180831161034d57829003601f168201915b505050505081565b336000818152600260209081526040808320600160a060020a038716808552908352818420869055815186815291519394909390927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925928290030190a35060015b92915050565b60005481565b600160a060020a038316600090815260016020526040812054821180159061042a5750600160a060020a03841660009081526002602090815260408083203384529091529020548211155b80156104365750600082115b156104c157600160a060020a03808416600081815260016020908152604080832080548801905593881680835284832080548890039055600282528483203384528252918490208054879003905583518681529351929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a35060016104c5565b5060005b9392505050565b60045460ff1681565b6006805460408051602060026001851615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561036a5780601f1061033f5761010080835404028352916020019161036a565b600160a060020a031660009081526001602052604090205490565b6005805460408051602060026001851615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561036a5780601f1061033f5761010080835404028352916020019161036a565b3360009081526001602052604081205482118015906105c55750600082115b156106375733600081815260016020908152604080832080548790039055600160a060020a03871680845292819020805487019055805186815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a35060016103d3565b5060006103d3565b336000818152600260209081526040808320600160a060020a038816808552908352818420879055815187815291519394909390927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925928290030190a383600160a060020a031660405180807f72656365697665417070726f76616c28616464726573732c75696e743235362c81526020017f616464726573732c627974657329000000000000000000000000000000000000815250602e019050604051809103902060e060020a9004338530866040518563ffffffff1660e060020a0281526004018085600160a060020a0316600160a060020a0316815260200184815260200183600160a060020a0316600160a060020a03168152602001828051906020019080838360005b8381101561077f578181015183820152602001610767565b50505050905090810190601f1680156107ac5780820380516001836020036101000a031916815260200191505b509450505050506000604051808303816000875af19250505015156107d057600080fd5b5060019392505050565b600160a060020a039182166000908152600260209081526040808320939094168252919091522054905600a165627a7a723058203f2de808df5359509254dc2a0d616b226de2b64f0bf28bae7323aeba4487199b0029";

		final Credentials ALICE =
				Credentials.create("8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");
		final Credentials BOB =
				Credentials.create("c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3");
		final Credentials CHARLIE =
				Credentials.create("ae6ae8e5ccbfb04590405997ee2d52d2b330726137b875053c36d94e974d162f");

		final Base64String ENCLAVE_KEY_ALICE =
				Base64String.wrap("A1aVtMxLCUHmBVHXoZzzBgPbW/wj5axDpW9X8l91SGo=");
		final Base64String ENCLAVE_KEY_BOB =
				Base64String.wrap("Ko2bVqD+nNlNYL5EE7y3IdOnviftjiizpjRt+HTuFBs=");
		final Base64String ENCLAVE_KEY_CHARLIE =
				Base64String.wrap("k2zXEin4Ip/qBGlRkJejnGWdP9cjkK+DAvKNW31L2C8=");

		final BesuPrivacyGasProvider ZERO_GAS_PROVIDER =
				new BesuPrivacyGasProvider(BigInteger.valueOf(0));
		Besu nodeAlice;
		Besu nodeBob;
		Besu nodeCharlie;
		nodeAlice = Besu.build(new HttpService("http://localhost:20000"));
		nodeBob = Besu.build(new HttpService("http://localhost:20002"));
		nodeCharlie = Besu.build(new HttpService("http://localhost:20004"));
		try {
			// Build new privacy group using the create API
			Base64String privacyGroupId =
					nodeBob.privCreatePrivacyGroup(
							Arrays.asList(
									ENCLAVE_KEY_ALICE, ENCLAVE_KEY_BOB, ENCLAVE_KEY_CHARLIE),
							"AliceBobCharlie",
							"AliceBobCharlie group")
							.send()
							.getPrivacyGroupId();

			BigInteger nonce =
					nodeCharlie
							.privGetTransactionCount(ALICE.getAddress(), privacyGroupId)
							.send()
							.getTransactionCount();
			final RawPrivateTransaction rawPrivateTransaction =
					RawPrivateTransaction.createContractTransaction(
							nonce,
							ZERO_GAS_PROVIDER.getGasPrice(),
							ZERO_GAS_PROVIDER.getGasLimit(),
							HUMAN_STANDARD_TOKEN_BINARY,
							ENCLAVE_KEY_ALICE,
							privacyGroupId,
							RESTRICTED);


			PrivateTransactionEncoder.signMessage(rawPrivateTransaction, ALICE);

			final String signedTransactionData =
					Numeric.toHexString(
							PrivateTransactionEncoder.signMessage(rawPrivateTransaction, 2018, ALICE));

			final String transactionHash;
			transactionHash = nodeAlice.eeaSendRawTransaction(signedTransactionData).send().getTransactionHash();

			final PollingPrivateTransactionReceiptProcessor receiptProcessor =
					new PollingPrivateTransactionReceiptProcessor(nodeAlice, 1 * 1000, 120);
			final PrivateTransactionReceipt receipt =
					receiptProcessor.waitForTransactionReceipt(transactionHash);


		} catch (IOException | TransactionException e) {
			e.printStackTrace();
		}
	}


		private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
	
	public void signOffline(Parameters parameters, ContractObject contractDb, String account, String functionName) throws Exception {
		LinkedHashMap<String, String> hashed = contractDb.getTaskIdAndRole();
		//System.out.println("size di hashed: " + hashed.size());
		int b = 0;
		int z = 0;
		for(int i = 0; i < contractDb.getTasks().size(); i++) {
			//System.out.println("iiiiii: " + i);
			if(contractDb.getTasks().get(i).equals(functionName)) {
				//System.out.println("task trovato : " + contractDb.getTasks().get(i));
				//System.out.println("VALORE DI I : " + i);
				z = i;
				break;
			}
		}
		
		for(Map.Entry<String, String> params : hashed.entrySet()) {
			 //System.out.println("valore di b " + b + " e di i " + z);
			if(b == z) {
				functionName = params.getKey().split("\\(")[0];
				//System.out.println("chiave: " + params.getKey());
				break;
			}else {
				b++;
			}
		 }
		
		//System.out.println("NOME NUOVO DELLA FUNZIONE: " + functionName);
		
		BigInteger GAS_PRICE = BigInteger.valueOf(7_600_000_000L);
		BigInteger GAS_LIMIT = BigInteger.valueOf(6_700_000L);

		EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
				  account, DefaultBlockParameterName.LATEST).sendAsync().get();
		 BigInteger nonce = ethGetTransactionCount.getTransactionCount();
		 //System.out.println(nonce);
		 
		
		 List<Type> t = new ArrayList<Type>();
		 for(Map.Entry<String, String> params : parameters.getParamsAndValue().entrySet()) {
				if(params.getKey().equals("uint")) {
					int intValue = Integer.parseInt(params.getValue());
					t.add(new Uint(BigInteger.valueOf(intValue)));
				}else if(params.getKey().equals("string")) {
					t.add(new Utf8String(params.getValue()));
				}else if(params.getKey().equals("bool")) {
					boolean boolValue = Boolean.parseBoolean(params.getValue());
					t.add(new Bool(boolValue));
				}else if(params.getKey().equals("address")) {
					t.add(new Address(params.getValue()));
				}
			}
		
		 
		 
		 Function function = new Function(
				  functionName, 
				  t, 
				  Collections.emptyList()
				  );
		  
		  
		 String encoded = FunctionEncoder.encode(function);
		
		 
		 RawTransaction ta = RawTransaction.createTransaction(
				 nonce, 
				// BigInteger.valueOf(131),
				 GAS_PRICE, 
				 GAS_LIMIT,
				 //"0xcc8bdb5dd918c9ec86e31b416f627ad0cc5ea22d",
				 contractDb.getAddress(),
				 encoded
				 );

			Credentials credentials = getCredentialFromPrivateKey(parameters.getPrivateKey());
			byte[] signedMessage = TransactionEncoder.signMessage(ta, credentials);
			String hexValue = Numeric.toHexString(signedMessage);
			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
			//  if(ethSendTransaction.hasError()) {
				  //System.out.println(ethSendTransaction.getError().getData());
				  //System.out.println(ethSendTransaction.getError().getMessage());}
			String transactionHash = ethSendTransaction.getTransactionHash();
			EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
			  
			  for (int i = 0; i < 222220; i++) {
				  //System.out.println("Wait: " + i);
		            if (!transactionReceipt.getTransactionReceipt().isPresent()) {
		                transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
		            } else {
		                break;
		            }
			  }
			  TransactionReceipt transactionReceiptFinal = transactionReceipt.getTransactionReceipt().get();
			  //System.out.println(transactionReceiptFinal.getLogs());
			  //System.out.println(transactionReceiptFinal.getLogsBloom());
			
	}

}
