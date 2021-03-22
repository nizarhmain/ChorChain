const fs = require("fs");
const path = require("path");

const Web3 = require("web3");
const EEAClient = require("./eea-client");
const { orion, besu } = require("./keys.js");

var express = require("express");
var cors = require('cors')

var bodyParser = require('body-parser')

var app = express();
app.use(cors())

app.use(bodyParser.json())


const port = 3000;

// web3.eth.getTransactionReceipt() will never show the contract address, just the private marker

// thats for port 20000
const web3 = new EEAClient(new Web3(besu.node1.url), 2018);

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


// private from has to be the node asking 
const getValue = (contract, contractAddress, action) => {
  const functionAbi = contract.find((e) => {
    return e.name === action;
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
      console.log("result:", result);
      return result.output;
    });
};


const storeValue = (contract, contractAddress, action, values) => {
  const functionAbi = contract.find(e => {
    return e.name === action;
  });

  const functionArgs = web3.eth.abi
    .encodeParameters(functionAbi.inputs, values).slice(2);

    console.log(functionArgs)

  const functionCall = {
    to: contractAddress,
    data: functionAbi.signature + functionArgs,
    privateFrom: orion.node1.publicKey,
    privateFor: [orion.node2.publicKey],
    privateKey: besu.node1.privateKey
  };
  return web3.eea.sendRawTransaction(functionCall).then((transactionHash) => {
    console.log(transactionHash)
      return web3.priv.getTransactionReceipt(
        transactionHash,
        orion.node1.publicKey
      );
    })
    .then((result) => {
      console.log("result:", result);
      return result.output;
    });;
};



app.post('/eea', function (req, res) {

  // we need contract and address
	let json_interface = JSON.parse(req.body.abi);

  let contract = new web3.eth.Contract(json_interface);


  getValue(contract._jsonInterface, req.body.address, "getOptionalRoles")

  res.send('about')
})


app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`);
});
