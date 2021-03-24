const path = require("path");
const fs = require("fs-extra");
const Web3 = require("web3");

// WARNING: the keys here are demo purposes ONLY. Please use a tool like Orchestrate or EthSigner for production, rather than hard coding private keys
const member1AccountAddress = "0xf0e2db6c8dc6c681bb5d6ad121a107f300e9b2b5";
// const member1AccountAddress = "0xed9d02e382b34818e88b88a309c7fe71e65f419d"
const member3TMPubKey = "1iTZde/ndBHvzhcl7V68x44Vx7pl8nwx9LqnM/AfJUg=";

const member1 = new Web3("http://127.0.0.1:20000");
const member2 = new Web3("http://127.0.0.1:20002");
const member3 = new Web3("http://127.0.0.1:20004");
member1.eth.defaultAccount = member1AccountAddress;

// abi and bytecode generated from simplestorage.sol:
// > solcjs --bin --abi simplestorage.sol
// const bytecode = constractJson.evm.bytecode.object;

const constractJsonPath = path.resolve(__dirname, "./contracts.json");
const constractJson = JSON.parse(fs.readFileSync(constractJsonPath))[
  "contracts"
];

const json_interface = constractJson["BikeStorage.sol:BikeStorage"];
const abi = constractJson["BikeStorage.sol:BikeStorage"].abi;
const bytecode = constractJson["BikeStorage.sol:BikeStorage"].bin;

const parsed_abi = JSON.parse(abi);

// const bytecode = constractJson.evm.bytecode.object

let contractInstance = new member1.eth.Contract(parsed_abi);

const contractOptions = {
  data: "0x" + bytecode,
  privateFor: [member3TMPubKey],
};

console.log(contractOptions);

deployToQuorum(contractInstance, contractOptions, member1AccountAddress, [
  member3TMPubKey,
]);

function deployToQuorum(contractInstance, contractOptions, from, to) {
  let deployedTxHash = "";
  let deployedContractAddress = "";

  contractInstance
    .deploy(contractOptions)
    .send(
      {
        from: from,
        // that is for 9 000 000
        gas: 0x55d4a80,
        privateFor: to,
      },
      function (error, transactionHash) {
        deployedTxHash = transactionHash;
        console.log("error= " + error + "; transactionHash=" + deployedTxHash);
      }
    )
    .on("error", function (error) {
      console.error(error);
    })
    .on("transactionHash", function (transactionHash) {
      console.log(
        "Contract transaction send: TransactionHash: " +
          transactionHash +
          " waiting to be mined..."
      );
    })
    .on("receipt", function (receipt) {
      console.log("receipt: " + receipt.contractAddress); // contains the new contract address
    })
    .on("confirmation", function (confirmationNumber, receipt) {
      console.log("confirmation: " + confirmationNumber); // contains the new contract address
    })
    .then(function (newContractInstance) {
      deployedContractAddress = newContractInstance.options.address;
      console.log("newContractInstance address: " + deployedContractAddress); // instance with the new contract address })
    });
}
