'use strict'

angular.module('querying', []).service('graphqlClientService', function ($http) {

    this.availableEntities = ['Block', 'Blocks', 'Transaction', 'GasPrice'];

    this.availableOperator = ['=', '!=', '>', '>=', '<', '<='];
    
    this.availableLogicalOperator = ['And', 'Or'];

    this.getEntityFields = function(entity) {
        if (entity == 'Block' || entity == 'Blocks') {
            return [
                'Number',
                'Hash',
                'Nonce',
                'TransactionsRoot',
                'TransactionsNonce',
                'ReceiptsRoot',
                'StateRoot',
                'ExtraData',
                'GasLimit',
                'GasUsed',
                'Timestamp'
            ];
        }
        
        if (entity == 'Transaction' || entity == 'Transactions') {
            return ['Hash', 'Nonce', 'Index', 'Value'];
        }

        return [];
    }

    this.getEntityBaseRules = function(entity) {
        if (entity == 'Block') {
            return [{
                Property: 'Number',
                Operator: '=',
                Value: null,
                Mandatory: true
            }];
        }

        if (entity == 'Blocks') {
            return [{
                Property: 'Number',
                Operator: '>',
                Value: null,
                LogicalOperator: 'And',
                Mandatory: true
            },
            {
                Property: 'Number',
                Operator: '<',
                Value: null,
                Mandatory: true
            }];
        }

        if (entity == 'Transaction') {
            return [{
                Property: 'Hash',
                Operator: '=',
                Value: null,
                Mandatory: true
            }];
        }
    }

});


// 193.205.92.133:8547