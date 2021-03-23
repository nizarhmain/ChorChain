package com.unicam.permissioned;

import com.unicam.rest.ContractFunctions;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.besu.response.privacy.PrivateTransactionReceipt;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.eea.crypto.PrivateTransactionEncoder;
import org.web3j.protocol.eea.crypto.RawPrivateTransaction;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.BesuPrivacyGasProvider;
import org.web3j.tx.response.PollingPrivateTransactionReceiptProcessor;
import org.web3j.utils.Base64String;
import org.web3j.utils.Numeric;
import org.web3j.utils.Restriction;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class QuorumFunctions {



    String rpc_endpoint = "http://localhost:21001";

    Web3j web3j = Web3j.build(new HttpService(rpc_endpoint));
    Admin adm = Admin.build(new HttpService(rpc_endpoint));

    private static final String VirtualProsAccount = "0xed9d02e382b34818e88b88a309c7fe71e65f419d";

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
            PersonalUnlockAccount personalUnlockAccount = adm.personalUnlockAccount(VirtualProsAccount, "").send();
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
            EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
            Thread.sleep(5000);

            return transactionReceipt.getResult().getContractAddress();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "error happened could not successfully send the transaction";
        }

    }



}
