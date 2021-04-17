package com.unicam.permissioned;

import com.unicam.rest.ContractFunctions;
import okhttp3.OkHttpClient;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.besu.response.privacy.PrivateTransactionReceipt;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.enclave.Enclave;
import org.web3j.quorum.enclave.Tessera;
import org.web3j.quorum.enclave.protocol.EnclaveService;
import org.web3j.quorum.tx.QuorumTransactionManager;
import org.web3j.tx.response.PollingPrivateTransactionReceiptProcessor;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class QuorumFunctions {



    String rpc_endpoint = "http://localhost:20000";

    Web3j web3j = Web3j.build(new HttpService(rpc_endpoint));
    Admin adm = Admin.build(new HttpService(rpc_endpoint));

    private static final String VirtualProsAccount = "0xf0e2db6c8dc6c681bb5d6ad121a107f300e9b2b5";

    public static boolean pendingTransaction = false;


    public String deploy(String bin) {
        try {
            if (pendingTransaction) {
                System.out.println("C'e' una transazione pendente");
                return "ERROR";
            }

            String binar = new String(Files.readAllBytes(Paths.get(
                    ContractFunctions.projectPath + "/resources/" + "_home_nizapizza_uni_ChorChain_src_com_unicam_resources_" + ContractFunctions.parseNameNoExtension(bin, ".bin") + "_sol_" + ContractFunctions.parseNameNoExtension(bin, ".bin") + ".bin")));

            //Unlocking the account
            // PersonalUnlockAccount personalUnlockAccount = adm.personalUnlockAccount(VirtualProsAccount, "").send();
            //Getting the nonce

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    VirtualProsAccount, DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            BigInteger GAS_PRICE = BigInteger.valueOf(13_500_000_000L);
            BigInteger GAS_LIMIT = BigInteger.valueOf(9_000_000L);

            Transaction transaction = Transaction.createContractTransaction(
                    VirtualProsAccount,
                    nonce,
                    BigInteger.ZERO,
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

            final PollingTransactionReceiptProcessor receiptProcessor =
                    new PollingTransactionReceiptProcessor(web3j, 1000, 120);
            final TransactionReceipt transactionReceipt =
                    receiptProcessor.waitForTransactionReceipt(transactionHash);

            // EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
            // Thread.sleep(5000);

            return transactionReceipt.getContractAddress();
        } catch (IOException | InterruptedException | ExecutionException | TransactionException e) {
            e.printStackTrace();
            return "error happened could not successfully send the transaction";
        }

    }


    public String deployPrivate(String bin, String privateKey) throws Exception {
        if(pendingTransaction == true) {
            System.out.println("C'� una transazione pendente");
            return "ERROR";
        }

        // String binar = new String ( Files.readAllBytes( Paths.get(projectPath + "/resources/" + parseName(bin, ".bin"))));

        String binar = new String ( Files.readAllBytes( Paths.get(ContractFunctions.projectPath + "/resources/" + "_home_nizapizza_uni_ChorChain_src_com_unicam_resources_" + ContractFunctions. parseNameNoExtension(bin, ".bin") + "_sol_" + ContractFunctions.parseNameNoExtension(bin, ".bin") + ".bin" )));

        //Unlocking the account
        PersonalUnlockAccount personalUnlockAccount = adm.personalUnlockAccount(VirtualProsAccount, "123nizarhmain").send();
        //Getting the nonce


        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                VirtualProsAccount, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        // 1 539 897 945
        BigInteger GAS_LIMIT = BigInteger.valueOf(10_000_000_000L);

        BigInteger blockGasLimit = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock().getGasLimit();


        Quorum quorum = Quorum.build(new HttpService("http://localhost:20000"));

        OkHttpClient okclient = new OkHttpClient.Builder().build();

        EnclaveService enclaveService = new EnclaveService("http://localhost", 9081, okclient);
        Enclave enclave = new Tessera(enclaveService, quorum);

        Credentials credentials = Credentials.create(privateKey);

        String TM_FROM_KEY = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

        List<String> TM_TO_KEY_ARRAY = Arrays.asList("1iTZde/ndBHvzhcl7V68x44Vx7pl8nwx9LqnM/AfJUg=");

        QuorumTransactionManager qrtxm = new QuorumTransactionManager(
                quorum, credentials, TM_FROM_KEY, TM_TO_KEY_ARRAY,
                enclave,
                30,     // Retry times
                2000);  // Sleep

        EthSendTransaction transactionResponse =  qrtxm.sendTransaction(
                BigInteger.ZERO,
                GAS_LIMIT,
                null,
                "0x" + binar,
                BigInteger.ZERO
        );

        // pendingTransaction = true;
        if(transactionResponse.hasError()) {
            System.out.println(transactionResponse.getError().getData());
            System.out.println(transactionResponse.getError().getMessage());
        }
        String transactionHash = transactionResponse.getTransactionHash();
        // EthGetTransactionReceipt transactionReceipt = quorum.ethGetTransactionReceipt(transactionHash).send();

        final PollingTransactionReceiptProcessor receiptProcessor =
                new PollingTransactionReceiptProcessor(web3j, 1000, 120);
        final TransactionReceipt transactionReceipt =
                receiptProcessor.waitForTransactionReceipt(transactionHash);

        return transactionReceipt.getContractAddress();


    }



}
