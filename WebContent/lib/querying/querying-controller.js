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

    $scope.contractsQueryTransaction = null;


    $scope.queryResult = null;

    $scope.queryResults = null;

    $scope.contractTransactionsResult = null;

    $scope.stringQueryResults = null;


    $scope.onEntitySelectionChanged = function () {
        if (this.selectedEntity == null)
            return;

        $scope.fields = graphqlClientService.getEntityFields(this.selectedEntity);
        $scope.rules = graphqlClientService.getEntityBaseRules(this.selectedEntity);
        $scope.selectedFields = {};
        updateUI();
    }

    $scope.onFieldsSelectionChanged = function () { updateUI(); }

    $scope.onRuleValueChanged = function () { updateUI(); }

    $scope.executeQuery = function () {
        $scope.isQuerying = true;
        const query = graphqlClientService.buildJsonQuery(this.selectedEntity, this.rules, this.selectedFields);
        graphqlClientService.executeQuery(query)
            .then(queryExecuted)
            .catch(errorExecutingQuery);
    }

    $scope.executeContractTransactionsQuery = function () {
        $scope.isQuerying = true;
        graphqlClientService.getContractTransactions($scope.contractsQueryTransaction)
            .then(transactionsQueryExecuted)
            .catch(errorExecutingQuery);
    }

    $scope.closeErrorDialog = function () {
        $scope.queryExecutionErrorOccurred = false;
        $scope.queryExecutionErrorMessage = null;
    }

    $scope.closeResultsDialog = function () {
        $scope.contractTransactionsResult = null;
        $scope.queryResults = null;
        $scope.queryResult = null;
        $scope.stringQueryResults = null;
    }

    $scope.exportResults = function () {
        let data = '';
        if (this.stringQueryResults)
            data = this.stringQueryResults;
        else if (this.queryResult)
            data = this.queryResult
        else if (this.queryResults)
            data = this.queryResults
        else if (this.contractTransactionsResult)
            data = this.contractTransactionsResult;

        const fileContent = JSON.stringify(data);
        const uri = encodeURI("data:application/json;charset=utf-8," + fileContent);
        startFileDownload(uri);
    }

    $scope.addNewConstratint = function () {
        const index = $scope.rules.length - 1;
        $scope.rules[index].LogicalOperator = '∧';
        $scope.rules[index].OperatorEditable = false;
        $scope.rules.push({
            Property: null,
            Operator: null,
            Value: null,
            LogicalOperator: null,
            Mandatory: false
        });
    }

    $scope.executeStaticQuery = function () {
        $scope.isQuerying = true;
        testUtilityMethod4()
            .then(result => {
                $scope.$apply(function () {
                    $scope.isQuerying = false;
                    $scope.queryResults = result;
                })
            })
            .catch(errorExecutingQuery);
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

    function transactionsQueryExecuted(transactions) {
        $scope.$apply(function () {
            $scope.isQuerying = false;
            $scope.contractTransactionsResult = transactions;
        })
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
            for (const item of value) { normalizeNumbersAndDates(item); }
            const finalResult = filterResults(value, $scope.rules);
            if (!Array.isArray(finalResult) || finalResult.length <= 0) {
                queryEmptyResults();
                return;
            }

            $scope.queryResults = finalResult;
        } else {
            normalizeNumbersAndDates(value);
            const finalResult = filterResults(value, $scope.rules);
            if (!finalResult) {
                queryEmptyResults();
                return;
            }

            $scope.queryResult = finalResult;
        }
    }

    function startFileDownload(encodedUri) {
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", 'chorchain-query-results.json');
        link.hidden = true;
        document.body.appendChild(link); // Required for FF
        link.click();
    }

    function normalizeNumbersAndDates(obj) {
        const numericalProp = ['nonce', 'value', 'gas', 'gasLimit', 'gasPrice', 'gasUsed', 'cumulativeGasUsed',
            'difficulty', 'totalDifficulty', 'number'];

        for (const prop in obj) {
            if (numericalProp.indexOf(prop) == -1)
                continue;

            const newValue = parseInt(obj[prop]);
            if (newValue == null || isNaN(newValue))
                continue;

            obj[prop] = newValue;
        }

        if (obj.hasOwnProperty('timestamp')) {
            const intValue = parseInt(obj.timestamp);
            const date = new Date(intValue * 1000);
            obj.timestamp = date.toLocaleDateString();
        }
    }

    function filterResults(result, rules) {
        if (!result || !rules)
            return result;

        const filteringRules = rules.filter(r => !r.Mandatory && isValidRule(r));
        if (!Array.isArray(filteringRules) || filteringRules.length <= 0)
            return result;

        if (!Array.isArray(result)) {
            for (const rule of filteringRules) {
                if (!valueRespectsRule(result, rule)) {
                    return null;
                }
            }

            return result;
        }

        const filteredResult = [];
        for (const item of result) {
            let invalidItem = false;
            for (const rule of filteringRules) {
                if (!valueRespectsRule(item, rule)) {
                    invalidItem = true;
                    break;
                }
            }

            if (!invalidItem)
                filteredResult.push(item);
        }

        return filteredResult;
    }

    function isValidRule(rule) {
        return rule.Operator != null && rule.Property != null && rule.Value != null;
    }

    function valueRespectsRule(value, rule) {
        if (!rule)
            return true;

        if (!value || !value[rule.Property])
            return false;

        switch (rule.Operator) {
            case '>': return value[rule.Property] > rule.Value;
            case '>=': return value[rule.Property] >= rule.Value;
            case '<': return value[rule.Property] < rule.Value;
            case '<=': return value[rule.Property] <= rule.Value;
            case '=': return value[rule.Property] == rule.Value;
            case '!=': return value[rule.Property] != rule.Value;
            default: return false;
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

    async function testUtilityMethod4() {
        const address = '0x2af5d197d4e226e75e5c51fbd2c9cdcc0e8cba00';
        const abi = getContractAbi();
        return await graphqlClientService.getContractTransactionsWithWeb3(address, abi);
    }

    function getContractAbi() {
        return [{ "constant": false, "inputs": [{ "internalType": "string", "name": "motivation", "type": "string" }], "name": "Message_1xm9dxy", "outputs": [], "payable": false, "stateMutability": "nonpayable", "type": "function" }, { "constant": true, "inputs": [], "name": "getCurrentState", "outputs": [{ "components": [{ "internalType": "string", "name": "ID", "type": "string" }, { "internalType": "enum HotelBooking.State", "name": "status", "type": "uint8" }], "internalType": "struct HotelBooking.Element[]", "name": "", "type": "tuple[]" }, { "components": [{ "internalType": "uint256", "name": "quotation", "type": "uint256" }, { "internalType": "bool", "name": "confirmation", "type": "bool" }, { "internalType": "string", "name": "date", "type": "string" }, { "internalType": "uint256", "name": "bedrooms", "type": "uint256" }, { "internalType": "bool", "name": "confirm", "type": "bool" }, { "internalType": "string", "name": "motivation", "type": "string" }, { "internalType": "string", "name": "booking_id", "type": "string" }, { "internalType": "bool", "name": "cancel", "type": "bool" }, { "internalType": "string", "name": "ID", "type": "string" }], "internalType": "struct HotelBooking.StateMemory", "name": "", "type": "tuple" }], "payable": false, "stateMutability": "view", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "string", "name": "date", "type": "string" }, { "internalType": "uint256", "name": "bedrooms", "type": "uint256" }], "name": "Message_045i10y", "outputs": [], "payable": false, "stateMutability": "nonpayable", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "address payable", "name": "to", "type": "address" }], "name": "Message_1etcmvl", "outputs": [], "payable": true, "stateMutability": "payable", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "bool", "name": "confirm", "type": "bool" }], "name": "Message_0r9lypd", "outputs": [], "payable": false, "stateMutability": "nonpayable", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "uint256", "name": "quotation", "type": "uint256" }], "name": "Message_1em0ee4", "outputs": [], "payable": false, "stateMutability": "nonpayable", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "bool", "name": "confirmation", "type": "bool" }], "name": "Message_1nlagx2", "outputs": [], "payable": false, "stateMutability": "nonpayable", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "string", "name": "ID", "type": "string" }], "name": "Message_1joj7ca", "outputs": [], "payable": false, "stateMutability": "nonpayable", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "string", "name": "booking_id", "type": "string" }], "name": "Message_1ljlm4g", "outputs": [], "payable": false, "stateMutability": "nonpayable", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "bool", "name": "cancel", "type": "bool" }], "name": "Message_0m9p3da", "outputs": [], "payable": false, "stateMutability": "nonpayable", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "string", "name": "_role", "type": "string" }], "name": "subscribe_as_participant", "outputs": [], "payable": false, "stateMutability": "nonpayable", "type": "function" }, { "constant": false, "inputs": [{ "internalType": "address payable", "name": "to", "type": "address" }], "name": "Message_0o8eyir", "outputs": [], "payable": true, "stateMutability": "payable", "type": "function" }, { "inputs": [], "payable": false, "stateMutability": "nonpayable", "type": "constructor" }, { "payable": true, "stateMutability": "payable", "type": "fallback" }, { "anonymous": false, "inputs": [{ "indexed": false, "internalType": "uint256", "name": "", "type": "uint256" }], "name": "stateChanged", "type": "event" }, { "anonymous": false, "inputs": [{ "indexed": false, "internalType": "string", "name": "", "type": "string" }], "name": "functionDone", "type": "event" }];
    }

}]);
