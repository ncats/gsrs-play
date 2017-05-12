(function () {
    var module = angular.module('ginasFilter', []);

    module.controller('FiltersController', function ($scope, $http, $timeout,$interval,$uibModal,$sce, $location) {
    	$scope.allFacets=[];
    	$scope.allSelected=[];
    	
    	$scope.childred=[];
    	
    	$scope.init = function (facets) {
    		$scope.allFacets=facets;
    	}
    	
    	//TODO: this should not be necessary when client-side is complete
    	$scope.addFacet = function (f, childScope) {
    		$scope.allFacets.push(f);
    		$scope.childred.push(childScope);
    	}
    	
    	
    	$scope.clear = function () {
    		_.forEach($scope.allFacets,function(f){
    			f.selectedLabels.length=0;
    			_.forEach(f.values, function(fv){
    				fv.checked=false;
    			});
    		});
    		$scope.apply();
    	}
    	
    	$scope.apply = function (){
    		$location.search("facet",$scope.getAllSelected());
            window.location = $location.absUrl();
            console.log("test");
    	}
    	
    	$scope.getAllSelected = function(){
    		return _.flatMap($scope.allFacets,function(f){
    			return _.map(f.selectedLabels, function(l){
    				return f.name + "/" + l;
    			});
    		});
    	}
    	
    	$scope.collapsed = false;
    	
    	$scope.collapse = function(){
    		$scope.collapsed=true;
    		_.forEach($scope.childred, function (c){
    			c.collapsed=true;
    		});
    	}
    	$scope.expand = function(){
    		$scope.collapsed=false;
    		_.forEach($scope.childred, function (c){
    			c.collapsed=false;
    		});
    	}
    	$scope.toggle = function(){
    		if($scope.collapsed){
    			$scope.collapse();
    		}else{
    			$scope.expand();
    		}
    	}
    });

    module.controller('FilterController', function ($scope, $http, $timeout,$interval,$uibModal,$sce, $location) {
        $scope.fq="";
        $scope.facets =[];
        $scope.filtered=false;
        $scope.fbaseurl="";
        
        $scope.selectedFacets=[];
        
        $scope.parentFacet={};
        
        $scope.facetName;
        
        $scope.ofacets =[];
        
        $scope.originalSelectedFacets=[];
        
        $scope.changed=false;
        
        $scope.collased=false;
        
     
        
        $scope.init = function (base, pfacet) {
            $scope.fbaseurl=base.replace("ffilter=","");
            if(pfacet){
            	$scope.parentFacet=pfacet;
            	$scope.selectedFacets=$scope.parentFacet.selectedLabels;
            	if(!$scope.selectedFacets){
            		$scope.selectedFacets=[];
            		$scope.parentFacet.selectedLabels=$scope.selectedFacets;
            	}
            	$scope.facetName=pfacet.name;
            	$scope.ofacets=pfacet.values;
            	$scope.originalSelectedFacets=_.clone(pfacet.selectedLabels);
            	$scope.$parent.addFacet(pfacet, $scope);
            	
            	_.forEach($scope.selectedFacets, function(s){
            		_.forEach($scope.ofacets, function(of){
            			if(of.label===s){
            				of.checked=true;
            			}
            		});
            	});
            	
            }
        };
        $scope.dofilter = function () {
        	if($scope.fq.length>0){
        		$scope.filtered=true;
            	$scope.refresh();
        	}else{
        		$scope.filtered=false;
        	}
        };
        $scope.refresh = function () {
        	
            var onError=function (data, status, headers, config) {
                $scope.monitor = false;
                $scope.mess = "Polling error!";
            };
            
            
            var responsePromise = $http.get($scope.fbaseurl + "&ffilter=" + $scope.fq);
            responsePromise.success(function (data, status, headers, config) {
            	var exp = new RegExp("(" + $scope.escapeRegExp($scope.fq) + ")", "gim");
            	$scope.facets=_.chain(data.content)
            	               .map(function(f){
            	            	   f.labelHighlight=$sce.trustAsHtml(f.label.replace(exp,"<b>$1</b>"));
            	            	   if(_.includes($scope.selectedFacets, f.label)){
            	            		   f.checked=true;
            	            	   }
            	            	   return f;
            	               }).value();
            });
            responsePromise.error(onError);

        };
        
        $scope.filterurl= function(){
        	return $scope.fbaseurl + "&ffilter=" + $scope.fq;
        };
        
        $scope.fToggle=function(facet){
        	var fval = facet.label + "/";
        	
        	if(facet.checked){
        		$scope.selectedFacets.push(facet.label);
        	}else{
        		_.pull($scope.selectedFacets, facet.label);
        	}
        	_.chain($scope.ofacets)
		   		 .filter({label:facet.label})
		   		 .forEach(function(f){
		   			 f.checked=facet.checked;
		   		 })
		   		 .value();
        	console.log($scope.selectedFacets);
        	console.log($scope.originalSelectedFacets);
        	
        	if(_.isEqual($scope.selectedFacets.sort(),$scope.originalSelectedFacets.sort())){
        		$scope.changed=false;
        		
        	}else{
        		$scope.changed=true;	
        	}
        	
        	
        };

        
        $scope.escapeRegExp = function(str) {
        	  return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        };
        
        $scope.apply = function(){
        	$scope.$parent.apply();
        };
        
        $scope.getFilterQueryString= function(){
        	return _.chain($scope.selectedFacets)
        	 .map(function(f){
        		 return {"name": "facet", "value":$scope.facetName +"/" + f};
        	 })
        	 .value();
        };
        
        $scope.clear = function(){
        	$scope.selectedFacets.length = 0;
        	_.forEach($scope.ofacets, function(f){
        		f.checked=false;
        	});
        	_.forEach($scope.facets, function(f){
        		f.checked=false;
        	});
        };
        
        
    });
    

})();