angular.module('filterListener', [])
    .factory('filterService', function ($rootScope) {
        var filters = [];
        return {
            _register: function (watch, change, value, scope) {
                console.log("registering a field to watch");
                console.log(watch);
                console.log(change);
                console.log(value);
                console.log(scope);
/*                if (!data.hasOwnProperty('field')) {
                    throw new Error("Filter must specify a field when registering with the filter service.");
                }*/
                var filter ={
                    watchField: watch,
                    changeField: change,
                    currentValue: value
                };
                filters.push(filter);
                console.log(filters);
                console.log($rootScope);
                $rootScope.$watchCollection(filters, function(newNames, oldNames) {
                    console.log(filter);
                   console.log(newNames);
                   console.log(oldNames);
                });
            },
            _unregister: function (field) {
                if (filters.hasOwnProperty(field)) {
                    delete filters[field];
                }
            },
            _unregisterAll: function () {
                for (var field in filters) {
                    delete filters[field];
                }
            },
            show: function (field) {
                var filter = filters[field];
                if (!filter) {
                    throw new Error("No filter named '" + field + "' is registered.");
                }
                filter.show();
            },
            hide: function (field) {
                var filter = filters[field];
                if (!filter) {
                    throw new Error("No filter named '" + field + "' is registered.");
                }
                filter.hide();
            }
        };
    });

/*
angular.module('filterListener')
    .directive('filteredcv', function () {
        return {
            restrict: 'A',
            controller: ['$scope', 'filterService', function ($scope, filterService) {
                console.log("inside the filtered directive controller");
                console.log($scope);
                // Declare a mini-API to hand off to our service so the service
                // doesn't have a direct reference to this directive's scope.
                var api = {
                    filter: $scope.filter,
                    show: function () {
                        $scope.show = true;
                    },
                    hide: function () {
                        $scope.show = false;
                    }
                };

                // Register this filter with the filter service.
                    filterService._register(api);

            }]
        };
    });
*/
