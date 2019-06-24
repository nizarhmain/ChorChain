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
			
			service.createInstance = function(model, cookieId){
				return $http.post("rest/createInstance/" + cookieId, model);
			}
			
			service.deploy = function(model, instanceId, cookieId){
				return $http.post("rest/deploy/" + cookieId + "/" + instanceId, model);
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

			return service;
		}]);