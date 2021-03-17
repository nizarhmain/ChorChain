pragma solidity ^0.5.3; 
	pragma experimental ABIEncoderV2;
	contract bikeRental{
	event functionDone(string);
	mapping (string=>uint) position;

	enum State {DISABLED, ENABLED, DONE} State s; 
	mapping(string => string) operator; 
	struct Element{
	string ID;
	State status;
}
	struct StateMemory{
	bool insuranceReq;
string bikeId;
string description;
bool ask;
 uint amount;
uint credits;
string data;
string feedback;
string voucherId;
 string bike_Id;
uint insuranceCost;
string voucherData;
string bikeType;
bool isAvailable;
 uint cost;
string insuranceData;
}
	Element[] elements;
	  StateMemory currentMemory;
	string[] elementsID = ["StartEvent_0gb8jks", "ExclusiveGateway_0uhgcse", "ExclusiveGateway_1e98v4d", "Message_0l75vce", "Message_0hzpgno", "EventBasedGateway_1nphygh", "Message_0cq2w1g", "Message_1ufjjj2", "ExclusiveGateway_04bkb0l", "Message_0to30q0", "ExclusiveGateway_0cfvdeh", "Message_0g4xpdf", "Message_0is10sh", "ParallelGateway_0himv1h", "EndEvent_11pwcmo", "Message_1989eur", "ParallelGateway_0yw95j2", "ExclusiveGateway_1ksw1j2", "Message_1dp5xa4", "ExclusiveGateway_0wc677m", "Message_009a0bz", "Message_0nkjynd", "Message_0b1e9t1", "ExclusiveGateway_05xdg8u", "Message_02ckm6k", "Message_06bv1qa", "Message_0psi2ab", "Message_0lvlunm"];
	string[] roleList = [ "Bike center", "Customer" ]; 
	string[] optionalList = ["Insurer" ]; 
	mapping(string=>address payable) roles; 
	mapping(string=>address payable) optionalRoles; 
constructor() public{
    //struct instantiation
    for (uint i = 0; i < elementsID.length; i ++) {
        elements.push(Element(elementsID[i], State.DISABLED));
        position[elementsID[i]]=i;
    }
         
         //roles definition
         //mettere address utenti in base ai ruoli
	roles["Bike center"] = 0x14c11711AE59C0bA8c5eaeEbcbFC4311055DAcdc;
	roles["Customer"] = 0x14c11711AE59C0bA8c5eaeEbcbFC4311055DAcdc;
	optionalRoles["Insurer"] = 0x0000000000000000000000000000000000000000;         
         //enable the start process
         init();
    }
    modifier checkMand(string memory role){ 
	require(msg.sender == roles[role]); 
	_; }
modifier checkOpt(string memory role){
	require(msg.sender == optionalRoles[role]); 
	_; }
modifier Owner(string memory task) 
{ 
	require(elements[position[task]].status==State.ENABLED);
	_;
}
function init() internal{
       bool result=true;
       	for(uint i=0; i<roleList.length;i++){
       	     if(roles[roleList[i]]==0x0000000000000000000000000000000000000000){
                result=false;
                break;
            }
       	}
       	if(result){
       	    enable("StartEvent_0gb8jks");
				StartEvent_0gb8jks();
       	}
			emit functionDone("Contract creation"); 
   }
 function getRoles() public view returns( string[] memory, address[] memory){
    uint c = roleList.length;
    string[] memory allRoles = new string[](c);
    address[] memory allAddresses = new address[](c);
    
    for(uint i = 0; i < roleList.length; i ++){
        allRoles[i] = roleList[i];
        allAddresses[i] = roles[roleList[i]];
    }
    return (allRoles, allAddresses);
}   function getOptionalRoles() public view returns( string[] memory, address[] memory){
       require(optionalList.length > 0);
       uint c = optionalList.length;
       string[] memory allRoles = new string[](c);
       address[] memory allAddresses = new address[](c);
       
       for(uint i = 0; i < optionalList.length; i ++){
           allRoles[i] = optionalList[i];
           allAddresses[i] = optionalRoles[optionalList[i]];
       }
    
       return (allRoles, allAddresses);
   }

function subscribe_as_participant(string memory _role) public {
        if(optionalRoles[_role]==0x0000000000000000000000000000000000000000){
          optionalRoles[_role]=msg.sender;
        }
    }
function() external payable{
    
}function StartEvent_0gb8jks() private {
	require(elements[position["StartEvent_0gb8jks"]].status==State.ENABLED);
	done("StartEvent_0gb8jks");
	enable("ExclusiveGateway_0uhgcse");  
	ExclusiveGateway_0uhgcse (); 
}

function ExclusiveGateway_0uhgcse() private {
	require(elements[position["ExclusiveGateway_0uhgcse"]].status==State.ENABLED);
	done("ExclusiveGateway_0uhgcse");
	enable("Message_02ckm6k");  
}

function ExclusiveGateway_1e98v4d() private {
	require(elements[position["ExclusiveGateway_1e98v4d"]].status==State.ENABLED);
	done("ExclusiveGateway_1e98v4d");
if(currentMemory.isAvailable==false){enable("ExclusiveGateway_0uhgcse"); 
 ExclusiveGateway_0uhgcse(); 
}
else if(currentMemory.isAvailable==true){enable("Message_0l75vce"); 
 }
}

function Message_0l75vce(bool insuranceReq) public checkMand(roleList[1]) {
	require(elements[position["Message_0l75vce"]].status==State.ENABLED);  
	done("Message_0l75vce");
currentMemory.insuranceReq=insuranceReq;
	enable("ExclusiveGateway_05xdg8u");
ExclusiveGateway_05xdg8u(); 
}

function Message_0hzpgno(string memory bikeId) public checkMand(roleList[0]) {
	require(elements[position["Message_0hzpgno"]].status==State.ENABLED);  
	done("Message_0hzpgno");
currentMemory.bikeId=bikeId;
	enable("EventBasedGateway_1nphygh");
EventBasedGateway_1nphygh(); 
}

function EventBasedGateway_1nphygh() private {
	require(elements[position["EventBasedGateway_1nphygh"]].status==State.ENABLED);
	done("EventBasedGateway_1nphygh");
	enable("Message_0cq2w1g"); 
	enable("Message_1989eur"); 
}

function Message_0cq2w1g(string memory description) public checkMand(roleList[1]) {
	require(elements[position["Message_0cq2w1g"]].status==State.ENABLED);  
	done("Message_0cq2w1g");
currentMemory.description=description;
disable("Message_1989eur");
	enable("Message_1ufjjj2");
}

function Message_1ufjjj2(bool ask, uint amount) public checkMand(roleList[0]) {
	require(elements[position["Message_1ufjjj2"]].status==State.ENABLED);  
	done("Message_1ufjjj2");
currentMemory.ask=ask;
currentMemory.amount=amount;
	enable("ExclusiveGateway_04bkb0l");
ExclusiveGateway_04bkb0l(); 
}

function ExclusiveGateway_04bkb0l() private {
	require(elements[position["ExclusiveGateway_04bkb0l"]].status==State.ENABLED);
	done("ExclusiveGateway_04bkb0l");
if(currentMemory.ask==true){enable("Message_0to30q0"); 
 }
else if(currentMemory.ask==false){enable("ExclusiveGateway_0cfvdeh"); 
 ExclusiveGateway_0cfvdeh(); 
}
}

function Message_0to30q0() public payable checkMand(roleList[1]) {
	require(elements[position["Message_0to30q0"]].status==State.ENABLED);  
	done("Message_0to30q0");
roles["Bike center"].transfer(msg.value);
	enable("ExclusiveGateway_0cfvdeh");
ExclusiveGateway_0cfvdeh(); 
}

function ExclusiveGateway_0cfvdeh() private {
	require(elements[position["ExclusiveGateway_0cfvdeh"]].status==State.ENABLED);
	done("ExclusiveGateway_0cfvdeh");
	enable("ExclusiveGateway_1ksw1j2");  
ExclusiveGateway_1ksw1j2(); 
}

function Message_0g4xpdf(uint credits) public checkMand(roleList[0]) {
	require(elements[position["Message_0g4xpdf"]].status==State.ENABLED);  
	done("Message_0g4xpdf");
currentMemory.credits=credits;
	enable("ParallelGateway_0himv1h");
ParallelGateway_0himv1h(); 
}

function Message_0is10sh(string memory data) public checkMand(roleList[0]) {
	require(elements[position["Message_0is10sh"]].status==State.ENABLED);  
	done("Message_0is10sh");
currentMemory.data=data;
	enable("ParallelGateway_0himv1h");
ParallelGateway_0himv1h(); 
}

function ParallelGateway_0himv1h() private { 
	require(elements[position["ParallelGateway_0himv1h"]].status==State.ENABLED);
	done("ParallelGateway_0himv1h");
	if( elements[position["Message_0is10sh"]].status==State.DONE && elements[position["Message_0g4xpdf"]].status==State.DONE ) { 
	enable("EndEvent_11pwcmo"); 
EndEvent_11pwcmo(); 
}} 

function EndEvent_11pwcmo() private {
	require(elements[position["EndEvent_11pwcmo"]].status==State.ENABLED);
	done("EndEvent_11pwcmo");  }

function Message_1989eur(string memory feedback) public checkMand(roleList[1]) {
	require(elements[position["Message_1989eur"]].status==State.ENABLED);  
	done("Message_1989eur");
currentMemory.feedback=feedback;
disable("Message_0cq2w1g");
	enable("ExclusiveGateway_1ksw1j2");
ExclusiveGateway_1ksw1j2(); 
}

function ParallelGateway_0yw95j2() private { 
	require(elements[position["ParallelGateway_0yw95j2"]].status==State.ENABLED);
	done("ParallelGateway_0yw95j2");
	enable("Message_0g4xpdf"); 
	enable("Message_0is10sh"); 
}

function ExclusiveGateway_1ksw1j2() private {
	require(elements[position["ExclusiveGateway_1ksw1j2"]].status==State.ENABLED);
	done("ExclusiveGateway_1ksw1j2");
	enable("Message_1dp5xa4");  
}

function Message_1dp5xa4(string memory voucherId, string memory bike_Id) public checkMand(roleList[1]) {
	require(elements[position["Message_1dp5xa4"]].status==State.ENABLED);  
	done("Message_1dp5xa4");
currentMemory.voucherId=voucherId;
currentMemory.bike_Id=bike_Id;
	enable("ParallelGateway_0yw95j2");
ParallelGateway_0yw95j2(); 
}

function ExclusiveGateway_0wc677m() private {
	require(elements[position["ExclusiveGateway_0wc677m"]].status==State.ENABLED);
	done("ExclusiveGateway_0wc677m");
	enable("Message_0nkjynd");  
}

function Message_009a0bz(uint insuranceCost) public checkMand(roleList[0]) {
	require(elements[position["Message_009a0bz"]].status==State.ENABLED);  
	done("Message_009a0bz");
currentMemory.insuranceCost=insuranceCost;
	enable("Message_0psi2ab");
}

function Message_0nkjynd() public payable checkMand(roleList[1]) {
	require(elements[position["Message_0nkjynd"]].status==State.ENABLED);  
	done("Message_0nkjynd");
roles["Bike center"].transfer(msg.value);
	enable("Message_0b1e9t1");
}
function Message_0b1e9t1(string memory voucherData) public checkMand(roleList[0]){
	require(elements[position["Message_0b1e9t1"]].status==State.ENABLED);
	done("Message_0b1e9t1");
currentMemory.voucherData=voucherData;
	enable("Message_0hzpgno");
}

function ExclusiveGateway_05xdg8u() private {
	require(elements[position["ExclusiveGateway_05xdg8u"]].status==State.ENABLED);
	done("ExclusiveGateway_05xdg8u");
if(currentMemory.insuranceReq==false){enable("ExclusiveGateway_0wc677m"); 
 ExclusiveGateway_0wc677m(); 
}
else if(currentMemory.insuranceReq==true){enable("Message_009a0bz"); 
 }
}

function Message_02ckm6k(string memory bikeType) public checkMand(roleList[1]) {
	require(elements[position["Message_02ckm6k"]].status==State.ENABLED);  
	done("Message_02ckm6k");
currentMemory.bikeType=bikeType;
	enable("Message_06bv1qa");
}

function Message_06bv1qa(bool isAvailable, uint cost) public checkMand(roleList[0]) {
	require(elements[position["Message_06bv1qa"]].status==State.ENABLED);  
	done("Message_06bv1qa");
currentMemory.isAvailable=isAvailable;
currentMemory.cost=cost;
	enable("ExclusiveGateway_1e98v4d");
ExclusiveGateway_1e98v4d(); 
}

function Message_0psi2ab() public payable checkMand(roleList[1]) {
	require(elements[position["Message_0psi2ab"]].status==State.ENABLED);  
	done("Message_0psi2ab");
optionalRoles["Insurer"].transfer(msg.value);
	enable("Message_0lvlunm");
}
function Message_0lvlunm(string memory insuranceData) public checkOpt(optionalList[0]){
	require(elements[position["Message_0lvlunm"]].status==State.ENABLED);
	done("Message_0lvlunm");
currentMemory.insuranceData=insuranceData;
	enable("ExclusiveGateway_0wc677m");
ExclusiveGateway_0wc677m(); 
}

 function enable(string memory _taskID) internal {
	elements[position[_taskID]].status=State.ENABLED; }
    function disable(string memory _taskID) internal { elements[position[_taskID]].status=State.DISABLED; }

    function done(string memory _taskID) internal { elements[position[_taskID]].status=State.DONE; 			emit functionDone(_taskID);
		 }
   
    function getCurrentState()public view  returns(Element[] memory, StateMemory memory){
        // emit stateChanged(elements, currentMemory);
        return (elements, currentMemory);
    }
    
    function compareStrings (string memory a, string memory b) internal pure returns (bool) { 
        return keccak256(abi.encode(a)) == keccak256(abi.encode(b)); 
    }
}