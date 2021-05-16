var express = require("express");
var cors = require("cors");

var bodyParser = require("body-parser");
const Web3 = require("web3");

var app = express();
app.use(cors());

app.use(bodyParser.json());

const port = 3000;

function executeAction(
  from,
  abi,
  contract_address,
  method_name,
  params,
  privateFor,
  node
) {
  const member1 = new Web3(`http://127.0.0.1:${node}`);

  console.log(from)
  console.log(privateFor)

  if (node === '20000') {
    privateFor = [ '1iTZde/ndBHvzhcl7V68x44Vx7pl8nwx9LqnM/AfJUg=' ]
  }

  if (node === '20004') {
    privateFor = [ 'BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=' ]
  }

  let contractInstance = new member1.eth.Contract(abi, contract_address);

  let _params = Object.values(params);

  var methodArgs = {
    from: from,
    gas: 0x55d4a80,
    privateFor: privateFor,
  };

  const functionAbi = abi.find((e) => {
    return e.name === method_name;
  });

  let callOrSend = functionAbi.constant ? "call" : "send";

  let web3Method = contractInstance.methods[method_name](..._params);

  web3Method[callOrSend](methodArgs).then((err, res) => {
    console.log(err);
    console.log(res);
  });
}

// get node of account
async function getNodeOfAccount(account) {
  const member1 = new Web3("http://127.0.0.1:20000");
  const member2 = new Web3("http://127.0.0.1:20002");
  const member3 = new Web3("http://127.0.0.1:20004");

  let accounts = await member1.eth.personal.getAccounts();
  let accounts_2 = await member2.eth.personal.getAccounts();
  let accounts_3 = await member3.eth.personal.getAccounts();

  // we know which node the account is in. So we will extract the information about the node itself

  if (accounts.includes(account)) {
    console.log('what')
    return "20000";
  }
  if (accounts_2.includes(account)) {
    console.log('here')
    return "20002";
  }
  if (accounts_3.includes(account)) {
    console.log('bzz')
    return "20004";
  }
}

app.post("/getFromNode", async function(req,res) {
  let account = req.body.account;

  let node = await getNodeOfAccount(account)

  if(node === "20000") {
    res.send("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")
  }
  if(node === "20002") {

  }
  if(node === "20004") {
    res.send("1iTZde/ndBHvzhcl7V68x44Vx7pl8nwx9LqnM/AfJUg=")
  }

})

app.post("/quorum", async function (req, res) {
  console.log(req.body);

  // we need contract and address
  let from = req.body.from;
  let address = req.body.address;
  let password = req.body.password;
  let abi = JSON.parse(req.body.abi);
  let method = req.body.method.split("(")[0];
  let params = req.body.parArray;
  let privateFor = req.body.privateFor;

  console.log(method);

  let node = await getNodeOfAccount(from);
  console.log(node);

  // unlock account
  const member = new Web3(`http://127.0.0.1:${node}`);

  await member.eth.personal.unlockAccount(from, password, 100000)
  // executeAction(from, abi, address, method, params, privateFor);
  console.log(from)
  console.log(address)
  console.log(privateFor)

  executeAction(from, abi, address, method, params, privateFor, node);

  res.send("about");
});

app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`);
});
