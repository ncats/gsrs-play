(function () {
    var ginasDownloads = angular.module('ginasDownloads', []);


    ginasDownloads.controller('ExportJobController', function ($scope, $http, $timeout) {
        $scope.state ={};
        $scope.id ={};
        
        $scope.init = function (initState, pollin) {
        	$scope.state=initState;
            $scope.status = $scope.state.status;
            $scope.monitor=pollin;
            $scope.id=$scope.state.id;
            $scope.refresh($scope.state.id);
        };
        $scope.refresh = function (id) {
            $scope.id = id;
            var onError=function (data, status, headers, config) {
                $scope.monitor = false;
                $scope.mess = "Polling error!";
            };
            var responsePromise = $http.get(baseurl + "downloads/" + id);
            responsePromise.success(function (data, status, headers, config) {
            	$scope.state=data;
                if ($scope.monitor) {
                    $scope.mess = "Polling ... " + data.status;
                    if(!$scope.state.complete){
                    	$scope.refresh(id);
                    }
                }
            });
            responsePromise.error(onError);

        };
        
        $scope.stopMonitor = function () {
            $scope.monitor = false;
            $scope.mess = "";
        };


    });


})();