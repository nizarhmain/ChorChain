'use strict'

angular.module('querying', []).service('graphqlClientService', function ($http) {

    // 'transactions',
    this.availableEntities = ['block', 'blocks', 'transaction', 'gasPrice', 'protocolVersion'];

    this.availableOperator = ['=', '!=', '>', '>=', '<', '<='];

    this.availableLogicalOperator = ['∧', '∨'];

    this.getEntityFields = function (entity) {
        if (entity == 'block' || entity == 'blocks')
            return getBlockFields();

        if (entity == 'transaction' || entity == 'transactions')
            return getTransactionFields();

        return [];
    }

    this.getEntityBaseRules = function (entity) {
        if (entity == 'block') {
            return [{
                Property: 'number',
                Operator: '=',
                Value: null,
                Mandatory: true
            }];
        }

        if (entity == 'blocks') {
            return [{
                Property: 'number',
                Operator: '>',
                Value: null,
                LogicalOperator: '∧',
                Mandatory: true
            },
            {
                Property: 'number',
                Operator: '<',
                Value: null,
                Mandatory: true
            }];
        }

        if (entity == 'transaction') {
            return [{
                Property: 'hash',
                Operator: '=',
                Value: null,
                Mandatory: true
            }];
        }

        if (entity == 'transactions') {
            return [{
                Property: 'blockNumber',
                Operator: '>',
                Value: null,
                LogicalOperator: '∧',
                Mandatory: true
            },
            {
                Property: 'blockNumber',
                Operator: '<',
                Value: null,
                Mandatory: true
            }];
        }
    }

    this.buildJsonQuery = function (selectedEntity, filteringRules, projectionFields) {
        const filterExpression = buildJsonQueryFilter(filteringRules);
        const projectionExpression = buildJsonQueryProjection(projectionFields);
        return `{\n\t${selectedEntity}${filterExpression} ${projectionExpression}\n}`;
    }

    this.executeQuery = function (query) {
        const request = { query: query };
        const jsonRequest = JSON.stringify(request);
        return $http.post('http://193.205.92.133:8547/graphql', jsonRequest);
    }

    this.getTransactionData = async function (hash) {
        const query = buildTransactionDataQuery(hash);
        const response = await this.executeQuery(query);
        return extractTransactionFromResponse(response);
    }

    this.getBlockData = async function (number) {
        const query = buildBlockDataQuery(number);
        const response = await this.executeQuery(query);
        return extractBlockFromResponse(response);
    }

    this.getContractTransactions = async function (creationHash) {
        const query = buildLimitsQuery(creationHash);
        const limitsResponse = await this.executeQuery(query);
        const limits = extractLimitsFromResponse(limitsResponse);
        if (!limits || isNaN(limits.start) || isNaN(limits.end))
            return null;

        // If the range is small make all in one query
        if (limits.end - limits.start <= 25000) {
            const newQuery = buildLimitedTransactionsQuery(limits.start, limits.end);
            const response = await this.executeQuery(newQuery);
            return extractTransactionsFromLimitedResponse(response);
        }

        let contractTransactions = [];
        do {
            const partialEndBlockNumber = limits.start + Math.min(1000, limits.end - limits.start);
            const newQuery = buildLimitedTransactionsQuery(limits.start, partialEndBlockNumber);
            const response = await this.executeQuery(newQuery);
            const transactions = extractTransactionsFromLimitedResponse(response);
            contractTransactions = contractTransactions.concat(transactions);
            limits.start += Math.min(1000, limits.end - limits.start);
        } while (limits.start < limits.end);


        return contractTransactions;
    }


    function getTransactionFields() {
        return ['hash', 'nonce', 'index', 'from', 'to', 'value', 'gas', 'gasPrice', 'gasUsed',
            'cumulativeGasUsed', 'inputData', 'blockNumber', 'blockHash', 'status'];
    }

    function getBlockFields() {
        return ['number', 'hash', 'nonce', 'transactionsRoot', 'transactionCount',
            'receiptsRoot', 'stateRoot', 'extraData', 'gasLimit', 'gasUsed', 'timestamp',
            'logsBloom', 'mixHash', 'difficulty', 'totalDifficulty'];
    }

    function buildJsonQueryFilter(filteringRules) {
        if (!Array.isArray(filteringRules) || filteringRules.length <= 0)
            return '';

        let filterExpression = '';
        const mandatoryRules = filteringRules.filter(r => r.Mandatory);
        for (const rule of mandatoryRules) {
            const property = getFilteringRulePropertyName(rule);
            if (property == 'number' || property == 'from' || property == 'to')
                filterExpression += `${property}: ${rule.Value},`;
            else
                filterExpression += `${property}: "${rule.Value}",`;
        }

        filterExpression = filterExpression.slice(0, -1); // remove last unuseful comma
        return `(${filterExpression})`;
    }

    function getFilteringRulePropertyName(rule) {
        if (!rule || !rule.Operator)
            return '';

        if (rule.Operator == '>')
            return 'from';

        if (rule.Operator == '<')
            return 'to';

        return rule.Property;
    }

    function buildJsonQueryProjection(projectionFields) {
        if (!projectionFields || !Object.keys(projectionFields) || Object.keys(projectionFields).length <= 0)
            return '';

        let filterExpression = '{';
        for (const field in projectionFields) {
            if (!projectionFields[field])
                continue;
            
            if (field == 'blockNumber') {
                filterExpression += `\n\t\tblock { number }`;
                continue;
            }
            
            if (field == 'blockHash') {
                filterExpression += `\n\t\tblock { hash }`;
                continue;
            }

            if (field == 'from' || field == 'to') {
                filterExpression += `\n\t\t${field} { address }`;
                continue;
            }

            filterExpression += `\n\t\t${field}`;
        }

        return filterExpression + '\n\t}';
    }

    function buildTransactionDataQuery(hash) {
        const filterExpr = `transaction(hash:\"${hash}\")`;
        const projectionExpr = buildTransactionFilterExpression();
        return `{\n\t${filterExpr} ${projectionExpr}\n}`;
    }

    function buildBlockDataQuery(number) {
        const filterExpr = `block(number:${number})`;
        const projectionExpr = buildBlockFilterExpression();
        return `{\n\t${filterExpr} ${projectionExpr}\n}`;
    }

    function buildTransactionFilterExpression() {
        const fields = getTransactionFields();
        let projectionExpr = '{';
        for (const field of fields) {
            if (field == 'from' || field == 'to') {
                projectionExpr += `\n\t\t${field} { address }`
                continue;
            }

            if (field == 'blockNumber') {
                projectionExpr += `\n\t\tblock { number }`
                continue;
            }

            if (field == 'blockHash') {
                projectionExpr += `\n\t\tblock { hash }`
                continue;
            }

            projectionExpr += `\n\t\t${field}`;
        }

        projectionExpr += '\n\t}';
        return projectionExpr;
    }

    function buildBlockFilterExpression() {
        const fields = getBlockFields();
        let projectionExpr = '{';
        for (const field of fields) { projectionExpr += `\n\t\t${field}`; }
        return projectionExpr + '\n\t}';
    }

    function buildLimitsQuery(hash) {
        return `{
            block {
              number
            },
            transaction(hash: "${hash}") {
              block { number }
            }
        }`;
    }

    function extractLimitsFromResponse(response) {
        if (!response || !response['data'] || !response['data']['data'])
            return;

        const result = response['data']['data'];
        if (!result.block || !result.transaction || !result.transaction.block)
            return;

        const start = parseInt(result.transaction.block.number);
        const end = parseInt(result.block.number);
        return { start: start, end: end };
    }

    function buildLimitedTransactionsQuery(start, end) {
        return `{
            blocks(from: ${start}, to: ${end}) {
              transactions {
                  hash
              }
            }
        }`;
    }

    function extractTransactionsFromLimitedResponse(response) {
        const blocks = extractObjectFromResponse(response, 'blocks');
        if (!Array.isArray(blocks))
            return [];

        return blocks.map(b => b.transactions.map(t => t.hash)).flat();
    }

    function extractTransactionFromResponse(response) {
        return extractObjectFromResponse(response, 'transaction');
    }

    function extractBlockFromResponse(response) {
        return extractObjectFromResponse(response, 'block');
    }

    function extractObjectFromResponse(response, objectName) {
        if (!response || !response['data'] || !response['data']['data'])
            return null;

        const result = response['data']['data'];
        if (!result[objectName])
            return null;

        return result[objectName];
    }

});


// 193.205.92.133:8547

// Transactions
// 0xde231da3b01de517f9356c89430d71e16613e7b5ee6b378e199401013d6cf458 ...very recent
// 0x21d5d464cd474b7a58f5d5bf75ba11a287fb07455f8a4fd01e6eee44d95e4dbe ...recent
// 0xcfb12ffb489b66deb000f06f399e76a186207802303c9901de1f2a9c0f133384 ...medium
// 0x2fd47a3cc677cd43a12838a4a603b59ab0c2de182f233f53e987fb4bfe9ead7f ...old