'use strict'

angular.module('querying').controller('auditController', ["$scope", "graphqlClientService", function ($scope, graphqlClientService) {

    $scope.models = [];

    $scope.selectedModel;

    $scope.totalInstances = 0;

    $scope.instancesMaxExecutionTime = 0;

    $scope.instancesMinExecutionTime = 0;

    $scope.instancesAverageExecutionTime = 0;

    $scope.instancesMaxGasUsed = 0;

    $scope.instancesMinGasUsed = 0;

    $scope.instancesAverageGasUsed = 0;

    $scope.completedInstances = 0;

    $scope.completedInstancesPercentage = 0;

    $scope.isRetrievingData = false;

    $scope.isShowingTransactionsDialog = false;

    $scope.selectedTransactions = false;


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

    $scope.showTransactions = function (transactions) {
        $scope.isShowingTransactionsDialog = true;
        $scope.selectedTransactions = transactions;
        for (const item of transactions) { normalizeNumbersAndDates(item); }
    }

    $scope.closeTransactionsDialog = function () {
        $scope.selectedTransactions = null;
        $scope.isShowingTransactionsDialog = false;
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
        }

        console.log(model);
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
            instance.executionTime = isNaN(instance.deployedContract.executionTime) ? 0 : instance.deployedContract.executionTime;
        }

        if (Array.isArray($scope.completedInstances) && $scope.completedInstances.length > 0) {
            $scope.instancesMaxExecutionTime = Math.max(...$scope.completedInstances.map(i => i.executionTime));
            $scope.instancesMinExecutionTime = Math.min(...$scope.completedInstances.map(i => i.executionTime));
            const totalExecutionTime = $scope.completedInstances.map(i => i.executionTime).reduce((a, b) => a + b, 0);
            $scope.instancesAverageExecutionTime = totalExecutionTime / model.instances.length;

            $scope.instancesMinGasUsed = Math.max(...$scope.completedInstances.map(i => i.totalGasUsed));
            $scope.instancesMaxGasUsed = Math.min(...$scope.completedInstances.map(i => i.totalGasUsed));
            const totalGasUsed = $scope.completedInstances.map(i => i.totalGasUsed).reduce((a, b) => a + b, 0);
            $scope.instancesAverageGasUsed = totalGasUsed / model.instances.length;
        }
    }

    // TODO: duplicated function, move to the service!
    function normalizeNumbersAndDates(obj) {
        const numericalProps = ['nonce', 'value', 'gas', 'gasLimit', 'gasPrice', 'gasUsed', 'cumulativeGasUsed'];
        for (const prop in obj) {
            if (numericalProps.indexOf(prop) == -1)
                continue;

            const newValue = parseInt(obj[prop]);
            if (newValue == null || isNaN(newValue))
                continue;

            obj[prop] = newValue;
        }

        // console.log("CONVERTO: ", obj);
        if (obj.block && obj.block.timestamp) {
            // console.log("ha il timestamp del blocco");
            const intValue = parseInt(obj.block.timestamp);
            // console.log("Intero parsato ", intValue);
            const date = new Date(intValue * 1000);
            // console.log("Data: ", date);
            obj.block.timestamp = date.toLocaleDateString() + " " + date.toLocaleTimeString();
            // console.log("RIsultato: ", obj);
        }
    }

}]);
