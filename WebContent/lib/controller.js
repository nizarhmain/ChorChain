'use strict'


var module = angular.module('homePage.controllers', ['ngCookies']);
module.controller("controller", [ "$scope","$window", "$location", "service",'$cookies',
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
			$scope.user.address = "";
		
			
			
			
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
					/*service.getInstances(model).then(function(response){
						console.log(response);
						$scope.instances = response.data;
						$scope.present = true;
						
					});*/
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
				service.createInstance(model, $cookies.get('UserId')).then(function(){
					
					$scope.msg = "Instance created";
				});
			}
			
			$scope.deploy = function(model, instanceId){
				service.deploy(model, instanceId, $cookies.get('UserId')).then(function(response){
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
			
		
			
		
		    
   }]);
