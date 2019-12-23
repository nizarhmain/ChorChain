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

    $scope.onEntitySelectionChanged = function() {
        if (this.selectedEntity == null)
            return;
        
        $scope.fields = graphqlClientService.getEntityFields(this.selectedEntity);
        $scope.rules = graphqlClientService.getEntityBaseRules(this.selectedEntity);
        updateUI(this.selectedEntity);
    }

    $scope.executeQuery = function() {
        // console.log("---------rules---------");
        // console.log(this.rules);
        // console.log("---------projection---------");
        // console.log(this.selectedFields);
    }

    function updateUI(entity) {
        $scope.canStartQueryExecution = entity != null;
        
        // if (!entity) {
        //     $scope.canStartQueryExecution = false;
        //     return;
        // }
        
        // for (const rule of $scope.rules) {
        //     if (rule.Mandatory && rule.Value == null) {
        //         $scope.canStartQueryExecution = false;
        //         return;        
        //     }
        // }

        // $scope.canStartQueryExecution = true;
    }

}]);
