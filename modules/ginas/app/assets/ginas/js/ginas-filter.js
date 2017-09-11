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
    	}
    	
    	$scope.getAllSelected = function(){
    		return _.flatMap($scope.allFacets,function(f){
    			return _.map(f.selectedLabels, function(l){
    				return f.name + "/" + l.replace(/\//g,"$$$");
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
        
        $scope.defquantity=5;
        $scope.defquantitymax=20;
        
        
        $scope.quantity=$scope.defquantity;
        
        
     
        
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
            	_.forEach($scope.ofacets, function(f){
            		f.display = getDisplayFacetValue($scope.facetName, f.label);
            	});
            	$scope.facetDisplay=getDisplayFacetName($scope.facetName);
            	
            	$scope.originalSelectedFacets=_.clone(pfacet.selectedLabels);
            	$scope.$parent.addFacet(pfacet, $scope);
            	
            	var selfac=[];
            	
            	_.forEach($scope.selectedFacets, function(s){
            		var found=false;
            		var fac=_.find($scope.ofacets, function(off){return off.label===s});
            		
            		if(fac){
            			fac.checked=true;
            			
            			selfac.push(fac);
            		}else{
            			selfac.push({label:s,display: getDisplayFacetValue($scope.facetName, s), checked:true});
            		}
            	});
            	
            	_.chain($scope.ofacets)
            	 .filter(function(fo){return !fo.checked;})
            	 .forEach(function (fo){
            		 selfac.push(fo);
            	 })
            	 .value();
            	$scope.ofacets=selfac;
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
        	if(_.isEqual($scope.selectedFacets.sort(),$scope.originalSelectedFacets.sort())){
        		$scope.changed=false;
        	}else{
        		$scope.changed=true;	
        	}
        };
        
        $scope.showMore = function(){
        	$scope.quantity=$scope.defquantitymax;
        }
        $scope.showLess = function(){
        	$scope.quantity=$scope.defquantity;
        }
        
        $scope.showShowMore = function(){
        	if($scope.quantity!==$scope.defquantity){
        		return false;
        	}
        	if($scope.filtered){
        		return ($scope.quantity<$scope.facets.length);
        	}else{
        		return ($scope.quantity<$scope.ofacets.length);
        	}
        }
        
        $scope.showShowLess = function(){
        	if($scope.quantity===$scope.defquantity){
        		return false;
        	}
        	return !$scope.showShowMore();
        }
        
        
    });
    

})();

var gglob = window;

function removeFacet (k,v) {
    
	v=v.replace(/\//g,"$$$");
    //console.log('k='+k+' v='+v);
    var q = [];
    location.search.substr(1).split("&").forEach(function(item) {
        var s = item.split("="),
	    sk = decodeURIComponent(s[0]),
	    sv = decodeURIComponent(s[1]).replace(/\+/g,' ');
	//console.log('sk='+sk+' sv='+sv);
	if (sk == 'facet' && sv.startsWith(k)) {
       if (v == sv.split("/")[1]) {
	      // remove
	   }else {
	      q.push(item);
	   }
    }else {
           q.push(item);
	}
    });
    var url = location.href.split("?")[0];
    if (q.length > 0) {
       url = url + '?'+q[0];
       for (var i = 1; i < q.length; ++i) {
          url = url + '&'+q[i];
       }
    }
    location.href = url;
}

function getDisplayFacetName(name){
	if(name==="SubstanceStereoChemistry"){
		return "Stereochemistry";
	}
	if(name==="root_lastEdited"){
		 return "Last Edited";
	}
	if(name==="root_approved"){
		return "Last Validated";
	}
	if(name==="root_lastEditedBy"){
		return "Last Edited By";
	}
	if(name==="Substance Class"){
		return "Substance Type";
	}
	return name.trim();	
}

function getDisplayFacetValue(name, label){
	if(name === "Substance Class"){
		if(label==="specifiedSubstanceG1" || label==="specifiedSubstance"){
			return "Group 1 Specified Substance";
		}
		return _.startCase( label);
	}else if(name === "Record Status"){
		if(label === "approved"){
			return "Validated (UNII)";
		}
	}else if(name === "Relationships"){
		if(label.indexOf("->")>=0){
			return label.split("->")[1] + " of " + label.split("->")[0];
		}
	}
	
	if(label === "EP"){
		return "PH. EUR";
	}
	if ("non-approved" === label) {
        return "Non-Validated";
    }
	
	if (name === "root_approved" || name ==="root_lastEdited"){
        return label.substr(1); // skip the prefix character
    }
	
	
	return label;
}

