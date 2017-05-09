(function () {
    var ginasDownloads = angular.module('ginasDownloads', []);

    ginasDownloads.controller('ExportJobController', function ($scope, $http, $timeout,$uibModal) {
        $scope.state ={};
        $scope.id ={};
        
        $scope.visible=true;
        
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
        
        $scope.cancel = function () {
            $http.get($scope.state.cancelUrl)
                 .then(function (dat) {
                	 console.log("Cancelled");
                	 });
        };
        
        $scope.remove = function () {
        	$scope.monitor = false;
        	
        	
        	$scope.confirm("Confirm Delete","Are you sure you'd like to delete this export?",function(){
        		$http.get($scope.state.removeUrl)
                .then(function (dat) {
               	 	$scope.visible=false;
               	 });	
        	})
        	
        	
            
        };
        $scope.formatSize = function formatBytes(a,b){
        	if(0==a)return"0 Bytes";
        	var c=1e3,d=b||2,e=["Bytes","KB","MB","GB","TB","PB","EB","ZB","YB"],f=Math.floor(Math.log(a)/Math.log(c));
        	return parseFloat((a/Math.pow(c,f)).toFixed(d))+" "+e[f]
        }
        
        $scope.confirm = function(title, message, cb){
    		$scope.warnTitle=title;
    		$scope.warnMessage=message;
    		
        	$scope.modalInstance = $uibModal.open({
                        templateUrl: baseurl + "assets/templates/modals/confirm-modal.html",
                        scope: $scope
                });
            $scope.close=function(tf){
            	$scope.modalInstance.close();
            	if(tf)cb();
            };
    	};
    });
})();