'use strict'

angular.module('querying').controller('personalPageController', ["$scope", "graphqlClientService", function ($scope, graphqlClientService) {

    $scope.user = null;

    $scope.showNotLoggedInMessage = false;

    $scope.isRetrievingData = false;

    $scope.selectedModel = null;


    $scope.initializeUser = async function () {
        if (graphqlClientService.isLoggedIn()) {
            $scope.isRetrievingData = true;
            const user = await graphqlClientService.getCurrentUser();
            await retreiveDataWithUser(user);
            console.log("final", user);
            const models = await graphqlClientService.getModels();
            console.log("I modelli scaricati", models);
            updateBindings(models, user);
        } else {
            $scope.showNotLoggedInMessage = true;
        }
    }

    $scope.selectModel = function (model) {
        $scope.selectedModel = model;
    }


    async function retreiveDataWithUser(user) {
        if (!user || !user.instances)
            return;

        for (const instance of user.instances) {
            if (!instance.deployedContract)
                continue;

            await graphqlClientService.getContractDataWithWeb3(instance.deployedContract);
        }
    }

    function updateBindings(models, user) {
        let userModels = models.filter(m => m.instances.find(i => user.instances.find(ii => ii.id === i.id)));
        userModels = userModels.sort((a, b) => a.name.localeCompare(b.name));

        for (const model of userModels) {
            model.totalInstances = model.instances.length;
            model.instances = model.instances.map(i => user.instances.find(ii => ii.id === i.id));
            model.completedInstances = model.instances.filter(i => i.deployedContract.isCompleted).length;
            model.completedInstancesPercentage = model.completedInstances * 100 / model.totalInstances;
            for (const instance of model.instances) {
                if (instance.deployedContract.subscriptions) {
                    const indices = getAllIndexes(instance.deployedContract.subscriptions[1], user.address);
                    instance.userRoles = [];
                    for (const index of indices) {
                        if (instance.deployedContract.subscriptions[0].length <= index)
                            continue;
                        instance.userRoles.push(instance.deployedContract.subscriptions[0][index]);
                    }
                }

                if (!Array.isArray(instance.deployedContract.transactions)) {
                    instance.executionTime = 0;
                    instance.totalGasUsed = 0;
                    continue;
                }

                let totalGas = 0;
                let totalFee = 0;
                for (const transaction of instance.deployedContract.transactions) {
                    totalGas += parseInt(transaction.gasUsed);
                    totalFee += transaction.fee ? transaction.fee : 0;
                }

                instance.totalGasUsed = totalGas;
                instance.totalFee = totalFee;
                instance.executionTime = isNaN(instance.deployedContract.executionTime) ? 0 : instance.deployedContract.executionTime;
            }

            const allRoles = model.instances.map(i => Array.isArray(i.userRoles) ? i.userRoles : []);
            model.userRoles = [...new Set([].concat.apply([], allRoles))];

            model.maxExecutionTime = Math.max(...model.instances.map(i => i.executionTime));
            model.minExecutionTime = Math.min(...model.instances.map(i => i.executionTime));
            const totalExecutionTime = model.instances.map(i => i.executionTime).reduce((a, b) => a + b, 0);
            model.avgExecutionTime = totalExecutionTime / model.instances.length;

            model.maxGasUsed = Math.max(...model.instances.map(i => i.totalGasUsed));
            model.minGasUsed = Math.min(...model.instances.map(i => i.totalGasUsed));
            const total = model.instances.map(i => i.totalGasUsed).reduce((a, b) => a + b, 0);
            model.avgGasUsed = total / model.instances.length;
        }

        $scope.$apply(() => {
            $scope.user = user;
            $scope.models = userModels;
            $scope.isRetrievingData = false;
        });
    }


    function getAllIndexes(arr, val) {
        var indexes = [], i = -1;
        while ((i = arr.indexOf(val, i + 1)) != -1) {
            indexes.push(i);
        }
        return indexes;
    }

}]);
