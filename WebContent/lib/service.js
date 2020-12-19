'use strict'

angular.module('homePage.services', []).factory('service',
		[ "$http", function($http) {
			const service = {};
			let user = {};

			service.setUser = function(localUser){
				user = localUser;
			}
			service.getUser = function(){
				return user;
			}

			service.getModels = function(){
				return $http.get("rest/getModels");
			}
			
			service.registerUser = function(chorchainUser){
				return $http.post("rest/registration/" , chorchainUser);
			}
			
			service.loginUser = function(chorchainUser){
				return $http.post("rest/login/", chorchainUser);
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

			service.getHyperledgerInstances = function(modelId){
				return $http.get("http://127.0.0.1:3000/api/chorinstance/instances?idModel=" + modelId)
			}
			
			service.createInstance = function(model, cookieId, optional, mandatory, visibleAt){
				console.log(model.id);
				const data = {modelID: model.id, optional: optional, mandatory: mandatory, visibleAt: visibleAt};
				return $http.post("rest/createInstance/" + cookieId ,data);
			}

			service.createHyperledgerInstance = function(idModel){
                return $http.get("http://127.0.0.1:3000/api/chorinstance/create?idModel=" + idModel);
			}
			
			service.deploy = function(model, instanceId, cookieId){
				return $http.post("rest/deploy/" + cookieId + "/" + instanceId, model);
			}
			service.hyperledgerDeploy = function(idChorLedger){
				let formData = new FormData();
				let ca = {"idChorLedger": idChorLedger};
				//formData.append("idChorLedger", idChorLedger);
				return $http.post("http://127.0.0.1:3000/api/contract/deploy", ca);
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
			service.newHyperledgerSubscribe = function(instanceId, role, cookieId){
				return $http.get("http://127.0.0.1:3000/api/chorinstance/subscribe?idUser=" + cookieId +
					"&idChorInstance=" + instanceId + "&subRole=" + role);
			}
			service.updateUserEthAddress = function(userAddress, cookieId){
				return $http.post("rest/updateUserEthAddress/" + userAddress + "/" + cookieId);
			}

			return service;
		}]);
