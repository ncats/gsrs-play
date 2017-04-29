(function () {
    var module = angular.module('ginasScheduled', []);


    module.controller('ScheduledJobController', function ($scope, $http, $timeout,$interval,$uibModal) {
        $scope.state ={};
        
        $scope.visible=true;
        $scope.pollDelayRunning=100;
        $scope.pollDelayIdle=5000;
        
        $scope.init = function (initState, pollin) {
        	$scope.set(initState);
            $scope.monitor=pollin;
            $scope.refresh(true);
            
//           var stopTime = $interval($scope.updateTime, 1000);
//            $interval.cancel(stopTime);
        };
        
        $scope.untilNextRun = function(){
        	return $scope.state.nextRun-(new Date()-0);
        };
        $scope.refresh = function (spawn) {
            
            var onError=function (data, status, headers, config) {
                $scope.monitor = false;
                $scope.mess = "Polling error!";
            };
            
            
            var responsePromise = $http.get(baseurl + "api/v1/scheduledjobs(" + $scope.state.id +")");
            responsePromise.success(function (data, status, headers, config) {
            	if(JSON.stringify(data)!==JSON.stringify($scope.state)){
            		$scope.set(data);
            	}
            	
                if ($scope.monitor && spawn) {
                    $scope.mess = "Polling ... " + data.status;
                    if($scope.state.running){
                    	$timeout(function () {
                    		$scope.refresh(true);
                    	}, Math.min($scope.untilNextRun(),$scope.pollDelayRunning));
                    }else{
                    	$timeout(function () {
                    		$scope.refresh(true);
                    	}, Math.min($scope.untilNextRun(),$scope.pollDelayIdle));
                    }
                }
            });
            responsePromise.error(onError);

        };
        
        $scope.set = function(n){
        	$scope.state=n;
        	if(n.cronSchedule){
        		var t=n.cronSchedule.split(" ");
        		t[0]=""; //quartz fix
        		var mod=t.join(" ").trim();
        		n.cronScheduleHuman=prettyCron.toString(mod);
        		if(n.cronSchedule.indexOf("#")>0){
        			var nth = n.cronSchedule.split("#")[1].split(" ")[0];
        			switch(nth){
        				case "1":
        					var cr=n.cronScheduleHuman;
        					cr=cr.replace("on ", "on the first ");
        					cr=cr + " of the month ";
        					n.cronScheduleHuman=cr;
        					break;
        				case "2":
        					var cr=n.cronScheduleHuman;
        					cr=cr.replace("on ", "on the second ");
        					cr=cr + " of the month ";
        					n.cronScheduleHuman=cr;
        					break;
        				case "3":
        					var cr=n.cronScheduleHuman;
        					cr=cr.replace("on ", "on the third ");
        					cr=cr + " of the month ";
        					n.cronScheduleHuman=cr;
        					break;
        				case "4":
        					var cr=n.cronScheduleHuman;
        					cr=cr.replace("on ", "on the forth ");
        					cr=cr + " of the month ";
        					n.cronScheduleHuman=cr;
        					break;
        			}
        		}
        		
        	}
        	if(!n.running && n.lastFinished){
        		console.log("how");
        		n.lastDurationHuman=humanizeDuration(n.lastFinished-n.lastStarted, { round: true });
        	}
        }
        
        
        
        $scope.stopMonitor = function () {
            $scope.monitor = false;
            $scope.mess = "";
        };
        
        $scope.disable = function () {
            $http.get($scope.state["@disable"])
                 .then(function (dat) {
                	 $scope.refresh();
                	 });
        };
        
        $scope.enable = function () {
            $http.get($scope.state["@enable"])
                 .then(function (dat) {
                	 $scope.refresh();
                	 });
        };
        
        $scope.execute = function () {
            $http.get($scope.state["@execute"])
                 .then(function (dat) {
                	 console.log("executed");
                	 $scope.refresh();
                	 });
        };
        
        
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
    
    module.controller('ScheduledJobsController', function ($scope, $http, $timeout,$uibModal) {
        $scope.jobs =[];
        
        $scope.init = function () {
            $scope.refresh();
        };
        $scope.refresh = function () {
        	
            var onError=function (data, status, headers, config) {
                $scope.monitor = false;
                $scope.mess = "Polling error!";
            };
            
            
            var responsePromise = $http.get(baseurl + "api/v1/scheduledjobs");
            responsePromise.success(function (data, status, headers, config) {
            	$scope.jobs=data.content;
            });
            responsePromise.error(onError);

        };
        

    });


})();