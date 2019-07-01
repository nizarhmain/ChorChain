'use strict'


var module = angular.module('homePage.controllers', ['ngCookies']);
module.controller("controller", [ "$scope","$window", "$location", "service", '$cookies',
		function($scope,$window, $location,service, $cookies) {
			
			$scope.regUser = {};
			$scope.user = {};
			$scope.role = null;
			$scope.content = {};
			$scope.models = {};
			$scope.instances = {};
			$scope.present = false;
			$scope.msg = null;
			$scope.contracts = {};
			$location.path();
			$scope.cookieId = null;
			//$scope.user.address = "";
			$scope.modelName = "";
			$scope.myContract = {};
			$scope.selectedRoles = [];
			
			// Toggle selection for the roles
			 $scope.toggleSelection = function toggleSelection(roleselected) {
			    var idx = $scope.selectedRoles.indexOf(roleselected);
			    // Is currently selected
			    if (idx > -1) {
			      $scope.selectedRoles.splice(idx, 1);
			    }
			    // Is newly selected
			    else {
			    	$scope.selectedRoles.push(roleselected);
			    }
			 }
			
			$scope.setModelName = function(fileName){
				$scope.modelName = fileName;
			}
			
			$scope.registerUser = function(){
				service.registerUser($scope.regUser).then(function(response){	
					alert(response.data);
				});		
			}
			
			$scope.loginUser = function(){
				service.loginUser($scope.user).then(function(response){
					if(!response.data){
						console.log("Negative response");
					}
					else{
						console.log("logged");
						$cookies.put('UserId', response.data);
						$scope.cookieId = response.data;
						console.log($scope.cookieId);
						window.location.href = 'http://193.205.92.133:8080/ChorChain/homePage.html';
					}
					
				});		
			}
			
			$scope.getModels = function(){
			    $scope.cookieId = $cookies.get('UserId');
				service.getModels().then(function(response){
					$scope.models = response.data;
					console.log("Models: ");
					console.log($scope.models);
				});
			}
			
			$scope.subscribe = function(model, instanceId,role){
				service.subscribe(model, instanceId, $scope.user.role, $cookies.get('UserId')).then(function(response){
					$scope.msg = response.data;
					service.getInstances(model).then(function(response){
						console.log(response);
						$scope.instances = response.data;
						$scope.present = true;
						
					});
				});
			}
			
			
			$scope.getInstances = function(model){
			
				service.getInstances(model).then(function(response){
					$scope.instances = response.data;
					console.log(response.data);
					$scope.present = true;
				});
			}
			
			 $scope.createInstance = function(model){
				var allRoles = angular.copy(model.roles);
				var allRoleslength = angular.copy(allRoles.length);
				for (var i= $scope.selectedRoles.length-1; i>=0; i--) {
					//remove the role selected from the all roles array
					var itemselected = allRoles.indexOf($scope.selectedRoles[i])
					allRoles.splice(itemselected, 1);
			    }
				
				if(allRoles.length == 0){
					console.log("ci sono solo ruoli opzionali")
				}
				
				service.createInstance(model, $cookies.get('UserId'), $scope.selectedRoles, allRoles).then(function(){
					$scope.selectedRoles = [];
					$scope.msg = "Instance created";
					$scope.getInstances(model);
				});
			 }
			
			$scope.deploy = function(model, instanceId){
				service.deploy(model, instanceId, $cookies.get('UserId')).then(function(response){
					console.log(response.data);
					sessionStorage.setItem('contract', JSON.stringify(response.data));
					$window.location.href = 'http://193.205.92.133:8080/ChorChain/deploy.html';
				});
			}
			
			$scope.getContracts = function(){
				console.log("COOKIE: " + $cookies.get('UserId'));
				service.getContracts($cookies.get('UserId')).then(function(response){
					console.log(response.data);
					$scope.contracts = response.data;
				})
			}
			
			$scope.getXml = function(filename){
				service.getXml(filename).then(function(response){
					$scope.model = response.data;
					console.log($scope.model);
				});
			}
			
			$scope.getContractFromInstance = function(instanceId, role){
					
				
				service.getContractFromInstance(instanceId).then(function(response){
					console.log(response.data.abi);
					console.log(response.data.address);
				//	$scope.myContract = new web3.eth.Contract(JSON.parse(response.data.abi), response.data.address);
					
					service.newSubscribe(instanceId, user.role, $cookies.get('UserId')).then(function(receipt){
						console.log("yeee");
					});
				/*	$scope.myContract.methods.subscribe_as_participant($scope.user.role).send({
						from : $scope.user.address,
						gas: 200000,
					}).then(function(receipt){
						console.log(receipt);
						service.newSubscribe(instanceId, user.role, $cookies.get('UserId')).then(function(receipt){
							console.log("yeee");
						});
					});*/
				});
			}
			
			$scope.optionalSubscribe = function(instanceId){
				var userId = $cookies.get('UserId');
				var role = $scope.user.role;
				service.setUser(userId).then(function(response){
					$scope.user = response.data;
					service.getContractFromInstance(instanceId).then(function(response){
						$scope.myContract = new web3.eth.Contract(JSON.parse(response.data.abi), response.data.address);
					
						$scope.myContract.methods.subscribe_as_participant(role).send({
							from : $scope.user.address,
							gas: 200000,
						}).then(function(receipt){
							console.log(receipt);
							service.newSubscribe(instanceId, role, $cookies.get('UserId')).then(function(receipt){
								console.log("yeee");
								
							});
						});
					});
				});
				
			}
			$scope.addMeta = function(){
				$window.addEventListener("load", function() {
				    if (typeof web3 !== "undefined") {
				     web3 = new Web3(web3.currentProvider);
				     console.log(web3);
				      //web3.eth.getAccounts().then(console.log);
				    } else {
				      console.log("No web3? You should consider trying MetaMask!");
				    }

				  });
			}
			
			$scope.setUser = function(){
				var userId = $cookies.get('UserId');
				service.setUser(userId).then(function(response){
					$scope.user = response.data;
					console.log($scope.user);
				});
			}
			
			//$scope.setUser();
			$scope.addMeta();
			
			
   }]);
