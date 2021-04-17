
var express = require("express");
var cors = require('cors')

var bodyParser = require('body-parser')
const Web3 = require("web3");

var app = express();
app.use(cors())

app.use(bodyParser.json())

const port = 3000;

function executeAction(from, abi, contract_address, method_name, params, privateFor) {
  const member1 = new Web3("http://127.0.0.1:20000");

  let contractInstance = new member1.eth.Contract(abi, contract_address);


  let _params = Object.values(params)

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




app.post('/quorum', function (req, res) {

    console.log(req.body)

  // we need contract and address
	let from = req.body.from;
	let address = req.body.address
	let abi = JSON.parse(req.body.abi);
	let method = req.body.method.split('(')[0];
	let params = (req.body.parArray);
    let privateFor = (req.body.privateFor);

    console.log(method)
  
    executeAction(from, abi, address, method, params, privateFor);


  res.send('about')
})


app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`);
});
