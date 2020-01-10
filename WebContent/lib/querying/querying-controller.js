'use strict'

angular.module('querying').controller('queryingController', ["$scope", "graphqlClientService", function ($scope, graphqlClientService) {

    $scope.entities = graphqlClientService.availableEntities;

    $scope.fields = [];

    $scope.rules = [];

    $scope.operators = graphqlClientService.availableOperator;

    $scope.logicalOperators = graphqlClientService.availableLogicalOperator;

    $scope.selectedEntity = null;

    $scope.canStartQueryExecution = false;

    $scope.selectedFields = {};

    $scope.queryExecutionErrorOccurred = false;

    $scope.queryExecutionErrorMessage = null;

    $scope.isQuerying = false;


    $scope.queryResult = null;

    $scope.queryResults = null;

    $scope.stringQueryResults = null;


    $scope.onEntitySelectionChanged = function () {
        if (this.selectedEntity == null)
            return;

        $scope.fields = graphqlClientService.getEntityFields(this.selectedEntity);
        $scope.rules = graphqlClientService.getEntityBaseRules(this.selectedEntity);
        $scope.selectedFields = {};
        updateUI();
    }

    $scope.onFieldsSelectionChanged = function () {  updateUI(); }

    $scope.onRuleValueChanged = function () { updateUI(); }

    $scope.executeQuery = function () {
        $scope.isQuerying = true;
        const query = graphqlClientService.buildJsonQuery(this.selectedEntity, this.rules, this.selectedFields);
        graphqlClientService.executeQuery(query)
            .then(queryExecuted)
            .catch(errorExecutingQuery);
    }

    $scope.closeErrorDialog = function () {
        $scope.queryExecutionErrorOccurred = false;
        $scope.queryExecutionErrorMessage = null;
    }

    $scope.closeResultsDialog = function () {
        $scope.queryResults = null;
        $scope.queryResult = null;
        $scope.stringQueryResults = null;
    }

    function updateUI() {
        $scope.queryExecutionErrorOccurred = false;
        if ($scope.selectedEntity == null) {
            $scope.canStartQueryExecution = false;
            return;
        }

        if ($scope.selectedFields == null || Object.keys($scope.selectedFields).length <= 0 ||
            Object.keys($scope.selectedFields).find(k => $scope.selectedFields[k] == true) == null) {
            $scope.canStartQueryExecution = $scope.fields && $scope.fields.length <= 0;
            return;
        }

        if (Array.isArray($scope.rules) && $scope.rules.length > 0) {
            const mandatoryRules = $scope.rules.filter(r => r.Mandatory);
            const brokenRule = mandatoryRules.find(r => r.Value == null || r.Value.length <= 0);
            if (brokenRule != null) {
                $scope.canStartQueryExecution = false;
                return;
            }
        }

        $scope.canStartQueryExecution = true;
    }

    function queryExecuted(response) {
        $scope.isQuerying = false;
        if (response.data == null || response.data['data'] == null) {
            errorExecutingQuery(null);
            return;
        }

        const value = response.data['data'];
        if (Object.keys(value).length <= 0) {
            queryEmptyResults();
            return;
        }

        if (value.hasOwnProperty('gasPrice')) {
            $scope.stringQueryResults = `The current gas price is ${value['gasPrice']}`;
        } else if (value.hasOwnProperty('protocolVersion')) {
            $scope.stringQueryResults = `The current protocolVersion is ${value['protocolVersion']}`;
        } else {
            showTableResults(value);
        }
    }

    function errorExecutingQuery(_) {
        $scope.queryExecutionErrorOccurred = true;
        $scope.queryExecutionErrorMessage = 'An error occurred during query execution!';
        $scope.isQuerying = false;
    }

    function queryEmptyResults() {
        $scope.queryExecutionErrorOccurred = true;
        $scope.queryExecutionErrorMessage = 'Query result is empty';
        $scope.isQuerying = false;
    }

    function showTableResults(result) {
        const keys = Object.keys(result);
        if (!keys || keys.length <= 0) {
            queryEmptyResults();
            return;
        }

        const value = result[keys[0]];
        if (Array.isArray(value)) {
            $scope.queryResults = value;
        } else {
            $scope.queryResult = value;
        }
    }


    // TODO: only as examples, to remove later
    async function testUtilityMethod1() {
        const transaction = await graphqlClientService.getTransactionData('0x21d5d464cd474b7a58f5d5bf75ba11a287fb07455f8a4fd01e6eee44d95e4dbe')
        console.log(transaction);
    }

    async function testUtilityMethod2() {
        const transactions = await graphqlClientService.getContractTransactions('0x21d5d464cd474b7a58f5d5bf75ba11a287fb07455f8a4fd01e6eee44d95e4dbe')
        console.log(transactions);
    }

    async function testUtilityMethod3() {
        const block = await graphqlClientService.getBlockData(4131251);
        console.log(block);
    }

}]);
