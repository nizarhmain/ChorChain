'use strict'

angular.module('querying').controller('auditController', ["$scope", "graphqlClientService", function ($scope, graphqlClientService) {

    $scope.models = [];

    $scope.selectedModel;

    $scope.isRetrievingData = false;


    $scope.getModels = async function () {
        $scope.isRetrievingData = true;
        try {
            const models = await graphqlClientService.getModels();
            $scope.$apply(function () {
                $scope.models = models;
                $scope.isRetrievingData = false;
            });
        } catch (error) {
            errorRetrievingData();
        }
    }

    $scope.selectModel = async function (model) {
        $scope.selectedModel = model;
        const xml = await graphqlClientService.getModelFile(model.name);
        bpmnjs.setXML(xml).then(function (result) {
            return bpmnjs.displayChoreography({});
        }).then(function (result) {
            bpmnjs.get('canvas').zoom('fit-viewport');
        }).catch(function (error) {
            console.error('something went wrong: ', error);
        });
    }


    function errorRetrievingData() {
        $scope.$apply(function () {
            $scope.models = [];
            $scope.isRetrievingData = false;
        });
    }

}]);
