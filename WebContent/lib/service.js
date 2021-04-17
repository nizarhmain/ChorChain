'use strict'

angular.module('homePage.services', []).factory('service',
		[ "$http", function($http) {
			var service = {};
			var user = {};
			
			service.setUser = function(localUser){
				user = localUser;
			}
			service.getUser = function(){
				return user;
			}

			service.getModels = function(){
				return $http.get("rest/getModels");
			}
			
			service.registerUser = function(user){
				
				return $http.post("rest/reg/" , user);
			}
			
			service.loginUser = function(user){
				return $http.post("rest/login/", user);
			}
			
			service.getModels = function(){
				return $http.post("rest/getModels");
			}
			
			service.subscribe = function(model, instanceId, role, cookieId){
				
				return $http.post("rest/subscribe/" + role + "/" + cookieId + "/" + instanceId, model);
			}
			
			service.getInstances = function(model){
				return $http.post("rest/getInstances/", model);
			}
			
			service.createInstance = function(model, cookieId, optional, mandatory, visibleAt){
				console.log(model.id);
				 var data = {modelID:model.id, optional:optional, mandatory:mandatory, visibleAt:visibleAt};

				return $http.post("rest/createInstance/" + cookieId ,data);
			}
			
			service.deploy = function(model, instanceId, cookieId){

				let private_key = angular.element( document.getElementById( 'private_key' ) )[0].value;
				let node_from = angular.element( document.getElementById( 'node_from' ) )[0].value;
				let node_for = angular.element( document.getElementById( 'node_to' ) )[0].value;

				console.log(private_key)
				console.log(node_from)
				console.log(node_for)

				return $http.post("rest/deploy/" + cookieId + "/" + instanceId + '?private_key=' + private_key, model);
			}
			service.getContracts = function(cookieId){
				return $http.post("rest/getCont/" + cookieId);
			}
			service.getXml = function(modelname){
				return $http.post("rest/getXml/" + modelname);
			}
			service.getContractFromInstance = function(instanceId){
				return $http.post("rest/getContractFromInstance/" + instanceId);
			}
			service.setUser = function(userId){
				return $http.post("rest/getUserInfo/" + userId);
			}
			service.newSubscribe = function(instanceId, role, cookieId){
				return $http.post("rest/newSubscribe/" + instanceId + "/" + role + "/" + cookieId);
			}

			return service;
		}]);