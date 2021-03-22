package com.unicam.permissioned;

import com.unicam.model.ContractObject;
import com.unicam.rest.ContractFunctions;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GanacheFunctions {


    private List<String> participants;
    public List<String> tasks;
    public List<ContractObject> allFunctions;
    public String CONTRACT_ADDRESS = "";
    private static final String VirtualProsAccount = "0x76aE023f51f19b0F3c001aA54951d217dc90FFa6";

    public static boolean pendingTransaction = false;

    public static String projectPath = "/home/nizapizza/uni/ChorChain/src/com/unicam";

    // public static String projectPath = System.getenv("ChorChain");
    // ganache endpoint
    String rpc_endpoint = "http://localhost:7545";
    Web3j web3j = Web3j.build(new HttpService(rpc_endpoint));
    Admin adm = Admin.build(new HttpService(rpc_endpoint));


    public String deploy(String bin) {

        try {
            if (pendingTransaction) {
                System.out.println("C'� una transazione pendente");
                return "ERROR";
            }

            String binar = new String(Files.readAllBytes(Paths.get(
                    projectPath + "/resources/" + "_Users_nizapizza_uni_ChorChain_src_com_unicam_resources_" + ContractFunctions.parseNameNoExtension(bin, ".bin") + "_sol_" + ContractFunctions.parseNameNoExtension(bin, ".bin") + ".bin")));

            //Unlocking the account
            PersonalUnlockAccount personalUnlockAccount = adm.personalUnlockAccount(VirtualProsAccount, "123nizarhmain").send();
            //Getting the nonce

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    VirtualProsAccount, DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            BigInteger GAS_PRICE = BigInteger.valueOf(13_500_000_000L);
            BigInteger GAS_LIMIT = BigInteger.valueOf(9_000_000L);

            Transaction transaction = Transaction.createContractTransaction(
                    VirtualProsAccount,
                    nonce,
                    GAS_PRICE,
                    GAS_LIMIT,
                    BigInteger.ZERO,
                    "0x" + binar);

            //send sync
            EthSendTransaction transactionResponse = web3j.ethSendTransaction(transaction).send();

            // pendingTransaction = true;
            if (transactionResponse.hasError()) {
                System.out.println(transactionResponse.getError().getData());
                System.out.println(transactionResponse.getError().getMessage());
            }
            String transactionHash = transactionResponse.getTransactionHash();
            EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
            Thread.sleep(5000);

            return transactionReceipt.getResult().getContractAddress();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "error happened could not successfully send the transaction";
        }

    }



}
