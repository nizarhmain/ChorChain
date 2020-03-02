'use strict'

angular.module('querying').controller('auditController', ["$scope", "graphqlClientService", function ($scope, graphqlClientService) {

    $scope.models = [];

    $scope.selectedModel;

    $scope.totalInstances = 0;

    $scope.completedInstances = 0;

    $scope.completedInstancesPercentage = 0;

    $scope.isRetrievingData = false;


    $scope.getModels = async function () {
        $scope.isRetrievingData = true;
        try {
            const models = await graphqlClientService.getModels();
            $scope.$apply(() => {
                $scope.models = models;
                $scope.isRetrievingData = false;
            });
        } catch (error) {
            errorRetrievingData();
        }
    }

    $scope.selectModel = async function (model) {
        $scope.selectedModel = model;
        $scope.isRetrievingData = true;
        await showModelBPMN(model);
        await retrieveModelData(model);
        $scope.$apply(() => updateInstancesData(model));
        $scope.$apply(() => $scope.isRetrievingData = false);
    }


    function errorRetrievingData() {
        $scope.$apply(function () {
            $scope.models = [];
            $scope.isRetrievingData = false;
        });
    }

    async function showModelBPMN(model) {
        const xml = await graphqlClientService.getModelFile(model.name);
        bpmnjs.setXML(xml)
            .then((_) => bpmnjs.displayChoreography({}))
            .then((_) => bpmnjs.get('canvas').zoom('fit-viewport'))
            .catch((_) => console.error(error));
    }

    async function retrieveModelData(model) {
        for (const instance of model.instances) {
            const contract = instance.deployedContract;
            if (!contract || !contract.address || !contract.abi)
                continue;

            await graphqlClientService.getContractDataWithWeb3(contract);
            // console.log("Contratto modificato: ", contract);
        }
    }

    function updateInstancesData(model) {
        $scope.totalInstances = model.instances.length;
        $scope.completedInstances = model.instances.filter(x => x.deployedContract.isCompleted).length;
        $scope.completedInstancesPercentage = $scope.totalInstances != 0 ? $scope.completedInstances * 100 / $scope.totalInstances : 0;
        for (const instance of model.instances) {
            if (!Array.isArray(instance.deployedContract.transactions))
                continue;

            let totalGas = 0;
            for (const transaction of instance.deployedContract.transactions) {
                totalGas += parseInt(transaction.gasUsed);
            }

            instance.totalGasUsed = totalGas;
        }
    }

}]);
