const path = require("path");
const fs = require("fs-extra");
const Web3 = require("web3");

// stole the code from here https://github.com/ConsenSys/quorum-remix/blob/c179fc36849169c3071d29e5419ae7faf9964675/src/api/index.js#L169

// god bless your REMIX

// WARNING: the keys here are demo purposes ONLY. Please use a tool like Orchestrate or EthSigner for production, rather than hard coding private keys
const member1AccountAddress = "0xf0e2db6c8dc6c681bb5d6ad121a107f300e9b2b5";
// const member1AccountAddress = "0xed9d02e382b34818e88b88a309c7fe71e65f419d"
const member3TMPubKey = "1iTZde/ndBHvzhcl7V68x44Vx7pl8nwx9LqnM/AfJUg=";

const member1 = new Web3("http://127.0.0.1:20000");
const member2 = new Web3("http://127.0.0.1:20002");
const member3 = new Web3("http://127.0.0.1:20004");
member1.eth.defaultAccount = member1AccountAddress;

// // this is the private key used for the signature
// const signAcct = member1.eth.accounts.decrypt({
//   address: "f0e2db6c8dc6c681bb5d6ad121a107f300e9b2b5",
//   crypto: {
//     cipher: "aes-128-ctr",
//     ciphertext:
//       "f2af258ee3733513333652be19197ae7eace4b5e79a346cf25b02a857e6043f3",
//     cipherparams: { iv: "587d7faaa6403b8a73273d0ad58dd71f" },
//     kdf: "scrypt",
//     kdfparams: {
//       dklen: 32,
//       n: 262144,
//       p: 1,
//       r: 8,
//       salt: "b93c7d69c5bb0a760c3b7fdf791c47896a552c5c977648b392a24d708674dcf3",
//     },
//     mac: "d83bcb555c92fc5a32ceacabbb6b99f59515ec3986b9fe5995c67e027bd750c8",
//   },
//   id: "5392d73f-08dd-42b8-bca9-6f6d35c419d9",
//   version: 3,
// }, "");
// //

// console.log(member1.eth.defaultAccount);

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

// deployToQuorum(contractInstance, contractOptions, member1AccountAddress, [
//    member3TMPubKey,
// ]);

// executeAction(
//   parsed_abi,
//   "0xfD657Eb45E1412181603e18eB495782E2F6979FA",
//   "Message_02ckm6k", ["cyberpunk"]
// );


executeAction(
  parsed_abi,
  "0xfD657Eb45E1412181603e18eB495782E2F6979FA",
  "getCurrentState", []
);


function executeAction(abi, contract_address, method_name, params) {
  let contractInstance = new member1.eth.Contract(abi, contract_address);

  // console.log(rawTransactionManager);

  var _params = Object.values(params)

  var methodArgs = {
    from: member1AccountAddress,
    privateFor: [member3TMPubKey],
  };

  const functionAbi = abi.find((e) => {
    return e.name === method_name;
  });

  console.log(functionAbi);

  let callOrSend = functionAbi.constant ? "call" : "send";

  let web3Method = contractInstance.methods[method_name](..._params);

  web3Method[callOrSend](methodArgs).then((err, res) => {
    console.log(err)
    console.log(res)
  });

}

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
