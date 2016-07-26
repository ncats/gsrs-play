(function () {
    var ginasAdmin = angular.module('ginasAdmin', []);

    ginasAdmin.factory('SDFFields', function () {
        var SDFFields = {};
        return SDFFields;
    });

    ginasAdmin.controller('ProgressJobController', function ($scope, $http, $timeout) {
        $scope.max = 100;
        $scope.monitor = false;
        $scope.mess = "";
        $scope.message = "";
        $scope.dynamic = 0;
        $scope.status = "UNKNOWN";
        $scope.averagePersistRate=0;
        $scope.stat = {
            recordsPersistedSuccess: 0,
            recordsProcessedSuccess: 0,
            recordsExtractedSuccess: 0
        };
        $scope.isDone= function(){
        	return $scope.status==="COMPLETE";
        	
        }
        $scope.humanTimeLeft= {};
        $scope.humanTimeTotal= {};
        $scope.humanTimeEstimate = {};
        $scope.toFullHumanTime= function(v){
        	
        	var ret="";
        	if(v.years()>0){
        		ret+=v.years() + " years ";
        	}
        	if(v.months()>0 || ret!== ""){
        		ret+=v.months() + " months ";
        	}
        	if(v.days()>0 || ret!== ""){
        		ret+=v.days() + " days ";
        	}
        	if(v.hours()>0  || ret!== ""){
        		ret+=v.hours() + " hours ";
        	}
        	if(v.minutes()>0  || ret!== ""){
        		ret+=v.minutes() + " minutes ";
        	}
        	if(v.seconds()>0  || ret!== ""){
        		ret+=v.seconds() + " seconds";
        	}
        	return ret;
        };
        
        $scope.init = function (id, pollin, status) {
            $scope.status = status;
            $scope.details = pollin;
            $scope.refresh(id, pollin);
        };
        $scope.refresh = function (id, pollin) {
            $scope.id = id;
            $scope.monitor = pollin;
            var onError=function (data, status, headers, config) {
                $scope.monitor = false;
                $scope.mess = "Polling error!";
                $scope.monitor = false;
            };
            var responsePromise = $http.get(baseurl + "api/v1/jobs/" + id + "/");
            responsePromise.success(function (data, status, headers, config) {
            	try{
            	//$scope.myData.fromServer = data.title;
                if ($scope.status !== data.status && $scope.status!=="UNKNOWN") {
                	location.reload();
                	return;
                }
                if (data.status == "RUNNING" || data.status == "PENDING") {
                    $scope.mclass = "progress-striped active";
                } else {
                    if ($scope.stopnext) {
                        $scope.mclass = "";
                        $scope.monitor = false;
                        $scope.mess = "Process : " + data.status;
                    } else {
                        $scope.stopnext = true;
                    }
                }
                if(data.statistics){
                	$scope.message = data.message;
	                $scope.stat = data.statistics;
	                $scope.max = data.statistics.totalRecords.count;
	                $scope.dynamic = data.statistics.recordsPersistedSuccess +
	                    data.statistics.recordsPersistedFailed +
	                    data.statistics.recordsProcessedFailed +
	                    data.statistics.recordsExtractedFailed;
	                $scope.max = data.statistics.totalRecords.count;
	                
	                $scope.allExtracted = $scope.max;
	                $scope.allPersisted = $scope.max;
	                $scope.allProcessed = $scope.max;
	                var dur=moment.duration($scope.stat.estimatedTimeLeft,"milliseconds");
	                $scope.humanTimeLeft.simple = dur.humanize();
	                $scope.humanTimeLeft.full = $scope.toFullHumanTime(dur);
	                var end=data.stop;
	                if(!end){
	                	end=new Date()-0;
	                	$scope.averagePersistRate=1000.0/$scope.stat.averageTimeToPersist;
	                }else{
	                	$scope.averagePersistRate=$scope.recordsPersistedSuccess*1000/(end-data.start);
	                }
	                
	                dur=moment.duration(end-data.start,"milliseconds");
	                $scope.humanTimeTotal.simple = dur.humanize();
	                $scope.humanTimeTotal.full = $scope.toFullHumanTime(dur);
	                
	                dur=moment.duration((end-data.start)+$scope.stat.estimatedTimeLeft,"milliseconds");
	                $scope.humanTimeEstimate.simple = dur.humanize();
	                $scope.humanTimeEstimate.full = $scope.toFullHumanTime(dur);
                }
                
                }catch(e){
                
                }
                if ($scope.monitor) {
                    $scope.monitor = true;
                    $scope.mess = "Polling ... " + data.status;
                    $scope.refresh(id, $scope.monitor);
                }
            });
            responsePromise.error(onError);

        };
        $scope.stopMonitor = function () {
            $scope.monitor = false;
            $scope.mess = "";
        };
        var poll = function () {
            $timeout(function () {
                //console.log("they see me pollin'");

                $scope.refresh($scope.id, false);
                if ($scope.monitor) poll();
            }, 1000);
        };


    });

    ginasAdmin.controller('SDFieldController', function ($scope) {
        $scope.radio = {
            model: 'NULL_TYPE'
        };
        $scope.path = "";
        $scope.radioModel = 'NULL_TYPE';

        $scope.checkModel = [
            "NULL_TYPE",
            "DONT_IMPORT",
            "ADD_CODE",
            "ADD_NAME",
            "ADD_NAME"
        ];

        $scope.init = function (path, model) {
            $scope.path = path;
            $scope.checkModel = model;
        };

        $scope.$watch('radio.model', function (newVal, oldVal) {
            var sdf = window.SDFFields[$scope.path];
            if (typeof sdf === "undefined") {
                sdf = {};
                window.SDFFields[$scope.path] = sdf;
            }
            sdf.path = $scope.path;
            sdf.method = $scope.radio.model;

            var l = [];
            for (var k in window.SDFFields) {
                l.push(window.SDFFields[k]);
            }
            //set the submission value
            $("#mappings").val(JSON.stringify(l));
        });


    });
/*
// Please note that $modalInstance represents a modal window (instance) dependency.
// It is not the same as the $uibModal service used above.

    ginasAdmin.controller('ModalInstanceCtrl', function ($scope, $uibModalInstance, items) {

        $scope.items = items;
        $scope.selected = {
            item: $scope.items[0]
        };

        $scope.ok = function () {
            $uibModalInstance.close($scope.selected.item);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });*/


})();