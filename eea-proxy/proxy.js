const fs = require("fs");
const path = require("path");

const Web3 = require("web3");
const EEAClient = require("./eea-client");
const { orion, besu } = require("./keys.js");

var express = require("express");
var app = express();

const port = 3000;

// web3.eth.getTransactionReceipt() will never show the contract address, just the private marker

// thats for port 20000
const web3 = new EEAClient(new Web3(besu.node2.url), 2018);

const getPrivateContractAddress = (transactionHash) => {
  console.log("Transaction Hash ", transactionHash);
  return web3.priv
    .getTransactionReceipt(transactionHash, orion.node1.publicKey)
    .then((privateTransactionReceipt) => {
      console.log("Private Transaction Receipt\n", privateTransactionReceipt);
      return privateTransactionReceipt.contractAddress;
    });
};

const getTransactionReceipt = (transactionHash) => {
  return web3.eth.getTransactionReceipt(transactionHash).then((receipt) => {
    return receipt;
  });
};

app.get("/getPrivateContractAddress/:hash", (req, res) => {
  let transactionHash = req.params.hash;
  console.log("Transaction Hash ", transactionHash);
  // web3.eth.getTransactionReceipt() will never show the contract address, just the private marker
  return web3.priv
    .getTransactionReceipt(transactionHash, orion.node1.publicKey)
    .then((privateTransactionReceipt) => {
      console.log("Private Transaction Receipt\n", privateTransactionReceipt);

      if (privateTransactionReceipt === null) {

        res.send("this contract is probably private")

        // getTransactionReceipt(transactionHash).then((receipt) => {
        //   res.send(receipt);
        // });

      } else {
          // res.send(privateTransactionReceipt);
        res.send(privateTransactionReceipt.contractAddress);
      }
    });
});

app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`);
});
