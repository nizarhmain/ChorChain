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
			
			service.subscribe = function(instance, role, cookieId){
				
				return $http.post("rest/subscribe/" + role + "/" + cookieId, instance);
			}
			
			service.getInstances = function(name){
				return $http.post("rest/getInstances/" + name);
			}
			
			service.createInstance = function(model, cookieId){
				return $http.post("rest/createInstance/" + cookieId, model);
			}
			
			service.deploy = function(instance, cookieId){
				return $http.post("rest/deploy/" + cookieId , instance);
			}
			service.getContracts = function(cookieId){
				return $http.post("rest/getCont/" + cookieId);
			}

			return service;
		}]);