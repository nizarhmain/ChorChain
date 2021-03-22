package com.unicam.permissioned;

import com.unicam.model.ContractObject;
import com.unicam.rest.ContractFunctions;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.besu.response.privacy.PrivateTransactionReceipt;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.request.Transaction;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BesuFunctions {

   private List<String> participants;
    public List<String> tasks;
    public List<ContractObject> allFunctions;
    public String CONTRACT_ADDRESS = "";
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

    // PUBLIC SMART CONTRACT
    public String deployPublic(String bin) throws Exception {

        String binar = new String ( Files.readAllBytes( Paths.get(projectPath + "/resources/" + "_home_nizapizza_uni_ChorChain_src_com_unicam_resources_" + ContractFunctions.parseNameNoExtension(bin, ".bin") + "_sol_" + ContractFunctions.parseNameNoExtension(bin, ".bin") + ".bin" )));

        //Unlocking the account
        // PersonalUnlockAccount personalUnlockAccount = adm.personalUnlockAccount(VirtualProsAccount, "123nizarhmain").send();
        //Getting the nonce

        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                unlockedEthSignAcc, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();


        BigInteger GAS_PRICE = BigInteger.valueOf(13_500_000_000L);
        BigInteger GAS_LIMIT = BigInteger.valueOf(9_000_000L);

        BigInteger blockGasLimit = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock().getGasLimit();

        Transaction transaction = Transaction.createContractTransaction(
                unlockedEthSignAcc,
                nonce,
                GAS_PRICE,
                GAS_LIMIT,
                BigInteger.ZERO,
                "0x"+binar);

        //send sync
        EthSendTransaction transactionResponse = ethsigner.ethSendTransaction(transaction).send();

        // pendingTransaction = true;
        if(transactionResponse.hasError()) {
            System.out.println(transactionResponse.getError().getData());
            System.out.println(transactionResponse.getError().getMessage());
        }
        String transactionHash = transactionResponse.getTransactionHash();
        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
        Thread.sleep(5000);
        return transactionReceipt.getResult().getContractAddress();
   }

    // PRIVATE SMART CONTRACT
    public String deploy(String bin, List<Base64String> privateFor) {

        String underscore_file_path = projectPath.replace('/', '_') ;
        try {
            // reading the binary files from the bpmn generated file
            String binar = new String ( Files.readAllBytes( Paths.get
                    (projectPath + "/resources/" + underscore_file_path + "_resources_" + ContractFunctions.parseNameNoExtension(bin, ".bin") + "_sol_" + ContractFunctions.parseNameNoExtension(bin, ".bin") + ".bin" )));

            BesuPrivacyGasProvider ZERO_GAS_PROVIDER =
                    new BesuPrivacyGasProvider(BigInteger.valueOf(0));

            Base64String orion_node_1 = Base64String.wrap("A1aVtMxLCUHmBVHXoZzzBgPbW/wj5axDpW9X8l91SGo=");
            privateFor.add(orion_node_1);

            Credentials orion1_key = Credentials.create("8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");

            // Build new privacy group using the create API
            final Base64String privacyGroupId =
                    eea.privCreatePrivacyGroup(
                            privateFor,
                            "AliceBobCharlie",
                            "AliceBobCharlie group")
                            .send()
                            .getPrivacyGroupId();

            final BigInteger nonce =
                    eea
                            .privGetTransactionCount(orion1_key.getAddress(), privacyGroupId)
                            .send()
                            .getTransactionCount();

            // create transaction
            RawPrivateTransaction transaction = RawPrivateTransaction.createContractTransaction(
                    nonce,
                    ZERO_GAS_PROVIDER.getGasPrice(),
                    ZERO_GAS_PROVIDER.getGasLimit(),
                    binar,
                    orion_node_1,
                    privacyGroupId,
                    Restriction.RESTRICTED
            );

            // sign transaction
            final String signedTransactionData =
                    Numeric.toHexString(
                            PrivateTransactionEncoder.signMessage(transaction, 2018, orion1_key));

            //send the transaction
            final String transactionHash = eea.eeaSendRawTransaction(signedTransactionData).send().getTransactionHash();

            final PollingPrivateTransactionReceiptProcessor receiptProcessor =
                    new PollingPrivateTransactionReceiptProcessor(eea, 1000, 120);
            final PrivateTransactionReceipt receipt =
                    receiptProcessor.waitForTransactionReceipt(transactionHash);

            return receipt.getContractAddress();
        } catch (IOException | TransactionException e) {
            e.printStackTrace();
            return "error happened could not successfully send the transaction";
        }
    }



}
