'use strict'

angular.module('querying', []).service('graphqlClientService', function ($http) {

    this.availableEntities = ['block', 'blocks', 'transaction', 'transactions', 'gasPrice', 'protocolVersion'];

    this.availableOperator = ['=', '!=', '>', '>=', '<', '<='];

    this.availableLogicalOperator = ['and', 'or'];

    this.getEntityFields = function (entity) {
        if (entity == 'block' || entity == 'blocks') {
            return ['number', 'hash', 'nonce', 'transactionsRoot', 'transactionCount',
                'receiptsRoot', 'stateRoot', 'extraData', 'gasLimit', 'gasUsed', 'timestamp',
                'logsBloom', 'mixHash', 'difficulty', 'totalDifficulty'];
        }

        if (entity == 'transaction' || entity == 'transactions') {
            return ['hash', 'nonce', 'index', 'from', 'to', 'value', 'gas', 'gasPrice', 'gasUsed',
                'cumulativeGasUsed', 'inputData', 'blockNumber', 'blockHash', 'status'];
        }

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
                LogicalOperator: 'and',
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
                LogicalOperator: 'and',
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


    function buildJsonQueryFilter(filteringRules) {
        if (!Array.isArray(filteringRules) || filteringRules.length <= 0)
            return '';

        let filterExpression = '';
        const mandatoryRules = filteringRules.filter(r => r.Mandatory);
        for (const rule of mandatoryRules) {
            const property = getFilteringRulePropertyName(rule);
            filterExpression += `${property}: ${rule.Value},`;
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
            filterExpression += `\n\t\t${field}`;
        }

        return filterExpression + '\n\t}';
    }

});


// 193.205.92.133:8547