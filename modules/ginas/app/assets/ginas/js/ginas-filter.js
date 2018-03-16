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
            $location.search("page",1);
            window.location = $location.absUrl();
        }
        
        $scope.getAllSelected = function(){
        	return _.flatMap($scope.childred,function(cs){
                       return cs.getSelected();
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
        $scope.facetType="any";
        $scope.ofacetType="any";

        
        $scope.selectedFacets=[];
        
        $scope.parentFacet={};
        
        $scope.facetName;
        
        $scope.ofacets =[];
        
        $scope.originalSelectedFacets=[];
        
        $scope.changed=false;
        
        $scope.collased=false;
        
        $scope.defquantity=5;
        $scope.defquantitymax=20;
        $scope.refreshing=false;
        

        
        $scope.negated={};
        $scope.originalnegated=[];
        $scope.negatedList=[];
        
        
        $scope.quantity=$scope.defquantity;
        
        $scope.isEnhanced=true;
        
        $scope.mode = "SERVER"; // or CLIENT or OFF
        
        $scope.hexEncode= function(str){
            var hex, i;
            var result = "";
            for (i=0; i<str.length; i++) {
                hex = str.charCodeAt(i).toString(16);
                result += ("000"+hex).slice(-4);
            }
            return result;
        };

     
        
        $scope.init = function (base, pfacet, mode) {
            if(mode){
                $scope.mode=mode;
            }
            $scope.fbaseurl=base.replace("ffilter=","");
            if(pfacet){
                if(pfacet.enhanced===false){
                	$scope.isEnhanced=false;
                }
            	
            	
                if(pfacet.prefix==="^"){
                         $scope.facetType="all";
                         $scope.ofacetType="all";
                }
                if(pfacet.prefix==="!"){
                         $scope.facetType="not";
                         $scope.ofacetType="not";
                         $scope.isnegated=true;
                }
            	
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
                        
                        fac.mid= "f" + $scope.hexEncode(s);
                        selfac.push(fac);
                    }else{
                               selfac.push({
                                        label:   s,
                                        display: getDisplayFacetValue($scope.facetName, s), 
                                        checked: true,
                                        mid:     "f" + $scope.hexEncode(s)
                                       });
                    }
                });
                
                _.chain($scope.ofacets)
                 .filter(function(fo){return !fo.checked;})
                 .forEach(function (fo){
                	 fo.mid="f" + $scope.hexEncode(fo.label);
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
            if($scope.mode === "SERVER"){
                $scope.refreshServer();
            }else if($scope.mode === "CLIENT"){
                $scope.refreshClient();
            }
        }
        
        $scope.refreshServer = function () {
            $scope.refreshing =true;
            
            var onError=function (data, status, headers, config) {
                $scope.monitor = false;
                $scope.mess = "Polling error!";
                $scope.refreshing=false;
            };
            
            
            var responsePromise = $http.get($scope.fbaseurl + "&ffilter=" + $scope.fq);
            responsePromise.success(function (data, status, headers, config) {
                if(data.ffilter === $scope.fq){
                    var exp = new RegExp("(" + $scope.escapeRegExp($scope.fq) + ")", "gim");
                    $scope.facets=_.chain(data.content)
                                   .map(function(f){
                                       var lab = getDisplayFacetValue($scope.facetName,f.label);
                                       f.labelHighlight=$sce.trustAsHtml(lab.replace(exp,"<b>$1</b>"));
                                       f.mid="mm"+$scope.hexEncode(f.label);
                                       if(_.includes($scope.selectedFacets, f.label)){
                                           f.checked=true;
                                       }
                                       return f;
                                   }).value();
                    $scope.refreshing=false;
                }
            });
            responsePromise.error(onError);

        };
        $scope.refreshClient = function () {
            $scope.refreshing =true;
            
            var exp = new RegExp("(" + $scope.escapeRegExp($scope.fq) + ")", "gim");
            $scope.facets=_.chain($scope.ofacets)
                                   .filter(function(f){
                                     return exp.exec(f.label)!==null;
                                   })
                                   .map(function(f){
                                       var lab = getDisplayFacetValue($scope.facetName,f.label);
                                       f.labelHighlight=$sce.trustAsHtml(lab.replace(exp,"<b>$1</b>"));
                                       if(_.includes($scope.selectedFacets, f.label)){
                                           f.checked=true;
                                       }
                                       return f;
                                   }).value();
            $scope.refreshing=false;

        };
        
        $scope.filterurl= function(){
            return $scope.fbaseurl + "&ffilter=" + $scope.fq;
        };
        
        $scope.testChanged = function(){
               
               
               
               if(_.isEqual($scope.selectedFacets.sort(),
                                    $scope.originalSelectedFacets.sort()) 
                       && $scope.ofacetType === $scope.facetType               
                       ){
                       $scope.changed=false;
               }else{
                       $scope.changed=true;    
               }
        };

        $scope.fToggle=function(facet){
            var fval = facet.label + "/";
            
            _.pull($scope.selectedFacets, facet.label);
            
            if(facet.checked){
                $scope.selectedFacets.push(facet.label);
            }
            
            _.chain($scope.ofacets)
                    .filter({label:facet.label})
                    .forEach(function(f){
                        f.checked=facet.checked;
                    })
                    .value();
            
            $scope.testChanged();
            
            
        };
        $scope.typeChange = function(negated){
               if(negated===true){
                       $scope.isnegated=true;
                       $scope.facetType="not";
               }else if(negated===false){
                       $scope.isnegated=false;
                       $scope.facetType="any";
               }else{
                       $scope.isnegated=false;
               }
               $scope.testChanged();
        }


        $scope.getSelected = function(){
               var f=$scope.parentFacet;
               var pre="";
               if($scope.facetType==="all"){
                       pre="^";
               }else if($scope.facetType==="not"){
                       pre="!";
               }
               
               return _.map(f.selectedLabels, function(l){
                               return pre + f.name + "/" + l.replace(/\//g,"$$$");
                       });
        }

        
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
            $scope.testChanged();
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
        
        $scope.showFacetSearch = function(){
                       // show facet search field 
                       // if number of items in a facet is greater than predefined number (5)
                       // and the facet is not collapsed
                       return ($scope.ofacets.length > $scope.defquantity && !$scope.collapsed);
        };

        
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
    var q = [];
    location.search.substr(1).split("&").forEach(function(item) {
        var s = item.split("="),
        sk = decodeURIComponent(s[0]),
        sv = decodeURIComponent(s[1]).replace(/\+/g,' ');
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
    if(name.toLowerCase()==="substancestereochemistry"){
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
    if(name==="GInAS Tag"){
        return "Source Tag";
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

window.onpopstate = function(event) {
	if(document.location.href.indexOf("#")==-1){		  
	  window.location.reload();
	}
};

