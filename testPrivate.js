const fs = require("fs");
const path = require("path");

const Web3 = require("web3");
const EEAClient = require("../src");
const EventEmitterAbi = require("./solidity/EventEmitter/EventEmitter.json")
  .output.abi;

const { orion, besu } = require("./keys.js");

const binary = fs.readFileSync(
  path.join(__dirname, "./solidity/EventEmitter/EventEmitter.bin")
);

// only node 1 and 2 can see the contractAddress, for the others it shows up as null
const web3 = new EEAClient(new Web3(besu.node1.url), 2018);
// const web3 = new EEAClient(new Web3(besu.node3.url), 2018);
// const web3 = new EEAClient(new Web3(besu.node3.url), 2018);
// eslint-disable-next-line no-new

new web3.eth.Contract(EventEmitterAbi);

const getPrivateTransactionReceipt = (transactionHash) => {
  return web3.priv
    .getTransactionReceipt(transactionHash, orion.node1.publicKey)
    .then((result) => {
      console.log("Transaction Hash:", transactionHash);
      console.log("Event Emitted:", result.logs[0].data);
      return result;
    });
};

const getValue = (contractAddress) => {
  const functionAbi = EventEmitterAbi.find((e) => {
    return e.name === "value";
  });

  const functionCall = {
    to: contractAddress,
    data: functionAbi.signature,
    privateFrom: orion.node1.publicKey,
    privateFor: [orion.node2.publicKey],
    privateKey: besu.node1.privateKey,
  };

  return web3.eea
    .sendRawTransaction(functionCall)
    .then((transactionHash) => {
      return web3.priv.getTransactionReceipt(
        transactionHash,
        orion.node1.publicKey
      );
    })
    .then((result) => {
      console.log("Get Value:", result.output);
      return result.output;
    });
};

const getPrivateContractAddress = (transactionHash) => {
  console.log("Transaction Hash ", transactionHash);
  // web3.eth.getTransactionReceipt() will never show the contract address, just the private marker
  return web3.priv
    .getTransactionReceipt(transactionHash, orion.node1.publicKey)
    .then((privateTransactionReceipt) => {
      console.log("Private Transaction Receipt\n", privateTransactionReceipt);
      return privateTransactionReceipt.contractAddress;
    });
};

// contract that was created, no idea how to find this address elsewhere actually

// getValue("0xebf56429e6500e84442467292183d4d621359838");

web3.eth.getTransactionReceipt("0x38ff0832f05a053e39b9ccb55aafe0e7b8709ac8")


// getPrivateContractAddress(
//   "0x39645440f623218e0c1a1753937791fa95ca62dfa1820b739cf1b9866ad7151d"
// ).then((addr) => {
//   console.log(addr);
// });

//console.log(orion.node1.publicKey);
//console.log(orion.node2.publicKey);
// console.log(orion.node3.publicKey);

const create_privacy_call = {
  addresses: [orion.node1.publicKey, orion.node2.publicKey],
  name: "One and two",
  description: "orion 1 and orion 2 privacy group",
};

//create privacy group
// web3.priv.createPrivacyGroup(create_privacy_call).then(result => console.log(result));

// web3.priv.findPrivacyGroup({addresses: [orion.node1.publicKey, orion.node2.publicKey]}).then(res => console.log(res))

// web3.priv.getTransactionCount({ data: ["0xfe3b557e8fb62b89f4916b721be55ceb828dbd73", "vJQATtOeaXc/2xQXgTtbN648gVrHS6UMuFcZs2qdmAI="] }).then(res => console.log(res))
