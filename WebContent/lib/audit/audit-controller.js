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

    $scope.subscriptions = null;

    $scope.isRetrievingData = false;

    $scope.isShowingTransactionsDialog = false;

    $scope.selectedTransactions = false;


    var accountInterval = setInterval(function () {

        $('.djs-group').dblclick(function (e) {
            e.stopPropagation();
            //event.stopPropagation()

            //taskid = e.currentTarget.childNodes[0].dataset.elementId;
            var message = e.currentTarget.childNodes[0].dataset.elementId;
            showSingleTransaction(message);
            //$('#auditing').modal('show');

            //angular.element(document.getElementById('controllerBody')).scope().showSingleTransaction(message);

        });
    }, 100);
    //document.getElementsByClassName("djs-group").ondblclick = function() {console.log("ciao")};



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
        processModelData(model);
        console.log("final model", model);
        $scope.$apply(() => updateInstancesData(model));
        $scope.$apply(() => $scope.isRetrievingData = false);
    }

    $scope.showTransactions = function (transactions) {
        $scope.isShowingTransactionsDialog = true;
        $scope.selectedTransactions = transactions;
        for (const item of transactions) { normalizeNumbersAndDates(item); }
    }

    function showSingleTransaction(messageId){
        $scope.isShowingTransactionsDialog = true;
        console.log($scope.isShowingTransactionsDialog);
        console.log(messageId);
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
            console.log("Istanza", instance);
        }
    }

    function processModelData(model) {
        const subscriptions = {};
        const messages = {};
        for (const instance of model.instances) {
            if (!instance.deployedContract)
                continue;

            processInstanceSubscriptions(instance, subscriptions);
            processInstanceMessages(instance, messages);
        }

        model.subscriptions = subscriptions;
    }

    function processInstanceSubscriptions(instance, actualSubscriptions) {
        const contract = instance.deployedContract;
        if (!contract.subscriptions || !contract.subscriptions[0] || !contract.subscriptions[1])
            return;

        const contractRoles = contract.subscriptions[0];
        const contractActors = contract.subscriptions[1];
        for (let i = 0; i < contractRoles.length; i++) {
            const role = contractRoles[i];
            const actor = contractActors[i];
            if (!actualSubscriptions.hasOwnProperty(role))
                actualSubscriptions[role] = [{ actor: actor, times: 1 }];
            else {
                const act = actualSubscriptions[role].find(x => x.actor == actor);
                if (act != null) {
                    act.times++;
                    continue;
                }

                actualSubscriptions[role].push({ actor: actor, times: 1 });
            }
        }
    }

    function processInstanceMessages(instance, actualMessages) {
        const contract = instance.deployedContract;
        console.log(instance.deployedContract.transactions);
        if (!contract || !contract.transactions || contract.transactions.length <= 0)
            return;

        const result = groupBy(contract.transactions, 'decodedInput');
        console.log(result);
    }

    function groupBy(xs, key) {
        return xs.reduce(function (rv, x) {
            (rv[x[key]] = rv[x[key]] || []).push(x);
            return rv;
        }, {});
    };

    function updateInstancesData(model) {
        $scope.subscriptions = model.subscriptions;
        $scope.subscriptionKeys = Object.keys(model.subscriptions);
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

        const completedInstances = model.instances.filter(x => x.deployedContract.isCompleted);
        if (Array.isArray(completedInstances) && completedInstances.length > 0) {
            $scope.instancesMaxExecutionTime = Math.max(...completedInstances.map(i => i.executionTime));
            $scope.instancesMinExecutionTime = Math.min(...completedInstances.map(i => i.executionTime));
            const totalExecutionTime = completedInstances.map(i => i.executionTime).reduce((a, b) => a + b, 0);
            $scope.instancesAverageExecutionTime = totalExecutionTime / model.instances.length;

            $scope.instancesMinGasUsed = Math.max(...completedInstances.map(i => i.totalGasUsed));
            $scope.instancesMaxGasUsed = Math.min(...completedInstances.map(i => i.totalGasUsed));
            const totalGasUsed = completedInstances.map(i => i.totalGasUsed).reduce((a, b) => a + b, 0);
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
