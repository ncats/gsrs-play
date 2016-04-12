(function () {
    'use strict';
    var ginasApp = angular.module('ginas', ['ngAria','ngMessages', 'ngResource', 'ui.bootstrap', 'ui.bootstrap.showErrors',
        'LocalStorageModule', 'ngTagsInput', 'jsonFormatter', 'ginasForms', 'ginasFormElements', 'ginasAdmin', 'diff-match-patch',
        'angularSpinners', 'filterListener'
    ]).run(['$anchorScroll', function ($anchorScroll) {
            $anchorScroll.yOffset = 150;   // always scroll by 100 extra pixels
        }])
        .config(function (localStorageServiceProvider, $locationProvider) {
            localStorageServiceProvider
                .setPrefix('ginas');
            $locationProvider.html5Mode({
                enabled: true,
                hashPrefix: '!',
                requireBase: true
            });
        });


    ginasApp.filter('dependency', function () {
         function smartCV(incItems, value) {
             console.log(incItems);
             console.log(value);
             if (value) {
                console.log(value);
                 return incItems;
             }
             return incItems;
         }
            return smartCV;
            /*var out = [{}];

            if(value){
                for(x=0; x<incItems.length; x++){
                    if(incItems[x].Value == value)
                        out.push(incItems[x]);
                }
                return out;
            }
            else if(!value){
                return incItems
            }*/
    });

    ginasApp.factory('Substance', function ($q, CVFields, UUID, polymerUtils) {

        function isCV(ob) {
            if (typeof ob !== "object") return false;
            if (ob === null) return false;
            //   if (typeof ob.value !== "undefined") {
            if (typeof ob.display !== "undefined") {
                return true;
            }
            // }
            return false;
        }

        function flattenCV(sub) {
            for (var v in sub) {
                if (isCV(sub[v])) {
                    if (sub[v].value) {
                        sub[v] = sub[v].value;
                    } else {
                        sub[v] = _.replace(sub[v].display, ' (not in CV)', '');
                        //  sub[v] = sub[v].display;
                    }
                } else {
                    if (typeof sub[v] === "object") {
                        flattenCV(sub[v]);
                    }
                }
            }
            return sub;
        }

        function expandCV(sub, path) {
            _.forEach(_.keysIn(sub), function (field) {
                if (path) {
                    var newpath = path + "." + field;
                } else {
                    var newpath = field;
                }
                if (_.isObject(sub[field])) {
                    if (_.isArray(sub[field])) {
                        _.forEach((sub[field]), function (value, key) {
                            if (_.isObject(value)) {
                                expandCV(value, newpath);
                            } else {
                                CVFields.getByField(newpath).then(function (response) {
                                    if (response.data.count > 0) {
                                        var cv = response.data.content[0].terms;
                                        var newcv = _.find(cv, ['value', value]);
                                        if (_.isUndefined(newcv)) {
                                            newcv = {};
                                            _.set(newcv, 'display', value + ' (not in CV)');
                                        }
                                        sub[field][key] = newcv;

                                    }
                                });
                            }
                        });
                    } else {
                        if (!_.isNull(sub[field])) {
                            expandCV(sub[field], newpath);
                            //});
                        }
                    }
                } else {
                    if (!_.isNull(sub[field])) {
                        CVFields.getByField(newpath).then(function (response) {
                            if (response.data.content.length > 0) {
                                var cv = response.data.content[0].terms;
                                var newcv = _.find(cv, ['value', sub[field]]);
                                if (_.isUndefined(newcv)) {
                                    newcv = {};
                                    _.set(newcv, 'display', sub[field] + ' (not in CV)');
                                }
                                sub[field] = newcv;
                            }
                        });
                    }
                }
            });
            return sub;
        }
        var substance = {};

        substance.$$setClass = function (subClass) {
            var substanceClass = subClass;
            switch (substanceClass) {
                case "chemical":
                    substance.substanceClass = substanceClass;
                    substance.structure = {};
                    _.set(substance.structure, 'opticalActivity', {value: "UNSPECIFIED"});
                    substance.moieties = [];
                    break;
                case "protein":
                    substance.substanceClass = substanceClass;
                    substance.protein = {};
                    substance.protein.subunits = [];
                    substance.protein.glycosylation = {
                        'CGlycosylationSites': [],
                        'NGlycosylationSites': [],
                        'OGlycosylationSites': []
                    };
                    break;
                case "structurallyDiverse":
                    substance.substanceClass = substanceClass;
                    substance.structurallyDiverse = {};
                    break;
                case "nucleicAcid":
                    substance.substanceClass = substanceClass;
                    substance.nucleicAcid = {};
                    substance.nucleicAcid.subunits = [];
                    break;
                case "mixture":
                    substance.substanceClass = substanceClass;
                    substance.mixture = {};
                    break;
                case "polymer":
                    substance.substanceClass = substanceClass;
                    substance.polymer = {};
                    break;
                case "specifiedSubstanceG1":
                    substance.substanceClass = substanceClass;
                    substance.specifiedSubstance = {};
                    break;
                default:
                    substance.substanceClass = substanceClass;
                    console.log('invalid substance class');
                    break;
            }
            if (!substance.references) {
               substance.references = [];
        }
            substance.definitionType = {value:"PRIMARY", display:"Primary"};
            substance.access =[{value:'protected', display: 'PROTECTED'}];
            return substance;
        };

        substance.$$getClass = function(){
            return substance.substanceClass;
        };

        substance.$$changeClass = function (newClass) {
            substance.substanceClass = newClass;
            return substance;
        };

        substance.$$setSubstance = function (sub) {
            _.forEach(sub, function (value, key) {
                _.set(substance, key, value);
            });
            return $q.when(expandCV(substance));
        };

        //returns a flattened clone of the substance
        substance.$$flattenSubstance = function () {
            var sub = _.cloneDeep(substance);
            if (sub.q) {
                delete sub.q;
            }

            if (sub.substanceClass === 'protein') {
                if (_.has(sub.protein, 'disulfideLinks')) {
                    _.forEach(sub.protein.disulfideLinks, function (value, key) {
                        var disulfideLink = {};
                        var sites = _.toArray(value.sites);
                     //   console.log(sites.length % 2);
                        if(sites.length % 2 != 0) {
                           sites = _.dropRight(sites);
                        }
                        disulfideLink.sites = sites;
                        sub.protein.disulfideLinks[key] = disulfideLink;
                   //    console.log(sub.protein.disulfideLinks);
                    });
                }
                if (_.has(sub.protein, 'otherLinks')) {
               //     console.log(sub.protein.otherLinks);
                    _.forEach(sub.protein.otherLinks, function (value, key) {
                        var otherLink = {};
                        var sites = _.toArray(value.sites);
                        console.log(sites.length % 2);
                        if(sites.length % 2 != 0) {
                            sites = _.dropRight(sites);
                        }
                        sub.protein.otherLinks[key].sites = sites;
                    });
                }
                console.log(JSON.stringify(sub.protein.otherLinks));
            }

            sub = flattenCV(sub);
            if (_.has(sub, 'moieties')) {
                _.forEach(sub.moieties, function (m) {
                    //m.id = UUID.newID();
                });
            }
            if (_.has(sub, 'structure')) {
                //apparently needs to be reset as well
                //sub.structure.id = UUID.newID();
                if (sub.substanceClass === 'polymer') {
                    _.set(sub, 'polymer.idealizedStructure', sub.structure);
                    sub = _.omit(sub, 'structure');
                }

            }
            
            if (_.has(sub, 'polymer')) {
            	console.log("Polymer setting");
            	console.log(sub.polymer.structuralUnits);
            	polymerUtils.setSRUFromConnectivityDisplay(sub.polymer.structuralUnits);
            }


            if (_.has(sub, 'nucleicAcid')) {
                console.log("nucleic setting");
                if (_.has(sub.nucleicAcid, 'sugars')) {
                    _.forEach(sub.nucleicAcid.sugars, function (sugar) {
                       if(sugar.sitesShorthand){
                           console.log(sugar.sitesShorthand);
                          // sugar.sitesShorthand = null;//
                           _.unset(sugar, 'sitesShorthand');

                       }
                    });
                }
                if (_.has(sub.nucleicAcid, 'linkages')) {
                    _.forEach(sub.nucleicAcid.linkages, function (linkage) {
                        if(linkage.sitesShorthand){
                            console.log(linkage.sitesShorthand);
                         //   linkage.sitesShorthand = null;
                            _.unset(linkage, 'sitesShorthand');
                        }
                    });
                }
                }
            return sub;
        };

        return substance;
    });

    ginasApp.factory('polymerUtils', function () {
        var utils = {};
        utils.getAttachmentMapUnits = function (srus) {
            var rmap = {};
            for (var i in srus) {
                var lab = srus[i].label;
                if (!lab) {
                    lab = "{" + i + "}";
                }
                for (var k in srus[i].attachmentMap) {
                    if (srus[i].attachmentMap.hasOwnProperty(k)) {
                        rmap[k] = lab;
                    }
                }
            }
            return rmap;
        };
        utils.sruConnectivityToDisplay = function (amap, rmap) {
            var disp = "";
            for (var k in amap) {
                if (amap.hasOwnProperty(k)) {
                    var start = rmap[k] + "_" + k;
                    for (var i in amap[k]) {
                        var end = rmap[amap[k][i]] + "_" + amap[k][i];
                        disp += start + "-" + end + ";\n";
                    }
                }
            }
            return disp;
        };
        utils.sruDisplayToConnectivity = function (display) {
            var connections = display.split(";");
            var regex=/^\s*[A-Z][A-Z]*[0-9]*_(R[0-9][0-9]*)[-][A-Z][A-Z]*[0-9]*_(R[0-9][0-9]*)\s*$/g;


    		var map={};
    		
            for(var i=0;i<connections.length;i++){
                var con=connections[i].trim();
                if(con==="")continue;

				regex.lastIndex = 0;
    			var res = regex.exec(con);
            	if(res==null){
            		throw "Connection '" + con + "' is not properly formatted";
            	}else{
					if(!map[res[1]]){
						map[res[1]]=[];
					}
            		map[res[1]].push(res[2]);
            	}
            }
            return map;
        };
        utils.setSRUConnectivityDisplay = function (srus) {
            var rmap = utils.getAttachmentMapUnits(srus);
            for (var i in srus) {
                var disp = utils.sruConnectivityToDisplay(srus[i].attachmentMap, rmap);
                srus[i]._displayConnectivity = disp;
            }
        };
        utils.setSRUFromConnectivityDisplay = function (srus) {
            for (var i in srus) {
                var map = utils.sruDisplayToConnectivity(srus[i]._displayConnectivity);
                srus[i].attachmentMap = map;
            }
        };

        return utils;
    });

    ginasApp.service('nameFinder', function ($http) {
        var url = baseurl + "api/v1/substances/search?q=";

        var nameFinder = {
            search: function (query) {
                var promise = $http.get(url + query + "*", {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    return response.data.content;
                });
                return promise;
            }
        };
        return nameFinder;
    });

    ginasApp.factory('substanceIDRetriever', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances(";
        var editSubstance = {
            getSubstance: function (editId) {
                var promise = $http.get(url + editId + ")?view=full", {cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    return response.data;
                });
                return promise;
            }
        };
        return editSubstance;
    }]);

    ginasApp.service('substanceRetriever', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances?filter=names.name='";
        var substanceRet = {
            getSubstances: function (name) {
                var promise = $http.get(url + name.toUpperCase() + "'", {cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    return response.data;
                });
                return promise;
            }
        };
        return substanceRet;
    }]);

    ginasApp.service('substanceSearch', function ($http, $q) {
        var options = {};
        var url = baseurl + "api/v1/substances/search?q=";

        this.load = function (field) {
            $http.get(url + field.toUpperCase(), {cache: true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (response) {
                options.data = response;
            });
        };

        this.search = function ($q) {
            return options;
        };
    });

    ginasApp.service('UUID', function () {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }

        this.newID = function () {
            var uuid = s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
            return (uuid);
        };
    });

    ginasApp.factory('FileReader', ['$q', '$window', function ($q, $window) {

        // Wrap the onLoad event in the promise
        var onLoad = function(reader, deferred, scope) {
            return function () {
                scope.$apply(function () {
                    deferred.resolve(reader.result);
                });
            };
        };

        // Wrap the onLoad event in the promise
        var onError = function (reader, deferred, scope) {
            return function () {
                scope.$apply(function () {
                    deferred.reject(reader.result);
                });
            };
        };
        
        // Wrap the onProgress event by broadcasting an event
        var onProgress = function(reader, scope) {
            return function (event) {
                scope.$broadcast('fileProgress', {
                    total: event.total,
                    loaded: event.loaded
                });
            };
        };

        // Instantiate a new Filereader with the wrapped properties
        var getReader = function(deferred, scope) {
            var reader = new $window.FileReader();
            reader.onload = onLoad(reader, deferred, scope);
            reader.onerror = onError(reader, deferred, scope);
            reader.onprogress = onProgress(reader, scope);
            return reader;
        };

        // Read a file as a data url
        var readAsDataURL = function (file, scope) {
            var deferred = $q.defer();

            var reader = getReader(deferred, scope);
            reader.readAsDataURL(file);

            return deferred.promise;
        };

        // Read a file as a text
        var readAsText = function(file, scope) {
            var deferred = $q.defer();

            var reader = getReader(deferred, scope);
            reader.readAsText(file, 'UTF-8');

            return deferred.promise;
        };

        // Read a file as a binary data
        var readAsBinaryString = function (file, scope) {
            var deferred = $q.defer();

            var reader = getReader(deferred, scope);
            reader.readAsBinaryString(file);

            return deferred.promise;
        };

        return {
            readAsDataURL: readAsDataURL,
            readAsBinaryString: readAsBinaryString,
            readAsText: readAsText
        };
    }]);

    ginasApp.controller("GinasController", function ($scope, $resource, $location, $compile, $uibModal, $http, $window, $anchorScroll,polymerUtils,
                                                     localStorageService, Substance, UUID, substanceSearch, substanceIDRetriever, CVFields, molChanger) {
       // var ginasCtrl = this;
//        $scope.select = ['Substructure', 'Similarity'];
        $scope.substance = $window.loadjson;
        if (typeof $window.loadjson !== "undefined" &&
            JSON.stringify($window.loadjson) !== "{}") {
            Substance.$$setSubstance($window.loadjson).then(function(data){
                _.set(data, 'update', true);
                $scope.substance = data;
            });
        } else {
            var temp = localStorageService.get('tempsubstance');
            localStorageService.remove('tempsubstance');
            if (temp && $location.$$search.kind != undefined) {
                Substance.$$setSubstance(temp).then(function(data){
                    $scope.substance = data;
                });
            } else {
                var substanceClass = $location.$$search.kind;
                $scope.substance = Substance.$$setClass(substanceClass);
            }
        }

        $scope.type = 'Substructure';
        $scope.cutoff = 0.8;
        $scope.stage = true;
        $scope.gridView = localStorageService.get('gridView') || false;
        $scope.diff = false;
        $scope.scrollTo = function (prmElementToScrollTo) {
            $location.hash(prmElementToScrollTo);
            $anchorScroll();
        };
        $scope.viewToggle = function () {
            $scope.submitSubstanceView = angular.fromJson(angular.toJson($scope.substance.$$flattenSubstance()));
        };

        $scope.toggleGrid = function () {
            localStorageService.set('gridView', $scope.gridView);
        };

        $scope.toggleFacet = function(facet){
                $scope[facet] = !$scope[facet];
        };

        $scope.getToggleStatus = function(facet){
            if(_.isUndefined($scope[facet])){
                $scope[facet]= false;
            }
            return $scope[facet];
        };

        $scope.redirectVersion = function(){
            var base =  $window.location.pathname.split('/v/')[0];
           var newLocation = "/v/" + $scope.versionNumber;
            $window.location.pathname = base + newLocation;
        };

        $scope.compare = function () {
            //$scope.left = angular.toJson(Substance.$$flattenSubstance(angular.copy($scope.substance)));
            $scope.left = angular.toJson($scope.substance.$$flattenSubstance());
            $scope.right = angular.toJson(angular.copy($window.loadjson));
            $scope.substancesEqual = angular.equals($scope.right, $scope.left);
        };

        $scope.canApprove = function () {
            var lastEdit = $scope.substance.lastEditedBy;
            if (!lastEdit)
                return false;
            if ($scope.substance.status === "approved") {
                return false;
            }
            //console.log(session.username);
            if (lastEdit === session.username) {
                return false;
            }
            return true;
        };

        $scope.moment = function (time) {
            return moment(time).fromNow();
        };
        //local storage functions//
        $scope.unbind = localStorageService.bind($scope, 'enabled');
        this.enabled = function getItem(key) {
            return localStorageService.get('enabled') || false;
        };
        this.numbers = true;
        localStorageService.set('enabled', $scope.enabled);

        //passes structure id from chemlist search to structure search//
        $scope.passStructure = function (id) {
            localStorageService.set('structureid', id);
        };

        $scope.clearStructure = function () {
            localStorageService.remove('structureid');
        };

        //Method for pushing temporary objects into the final message
        //Note: This was changed to use a full path for type.
        //That means that passing something like "nucleicAcid.type"
        //for type, will store the form object into that path inside
        //the substance, unless otherwise caught in the switch.
        //This simplifies some things.
        $scope.validate = function (obj, form, path) {
            $scope.$broadcast('show-errors-check-validity');
            if (form.$valid) {
                if (_.has($scope.substance, path)) {
                    var temp = _.get($scope.substance, path);
                    temp.push(obj);
                    _.set($scope.substance, path, temp);
                } else {
                    var x = [];
                    x.push(angular.copy(obj));
                    _.set($scope.substance, path, x);
                }

                /// form.$setSubmitted(true);
                obj = {};

                form.$setPristine();
                form.$setUntouched();
                $scope.$broadcast('show-errors-reset');
                // form.$setValidity();
                // form.$error= {};
                return true;
            } else {
                //console.log("Invalid");
                return false;
            }


        };

        $scope.checkErrors = function () {
            if (_.has($scope.substanceForm, '$error')) {
                //console.log($scope.substanceForm.$error);
                _.forEach($scope.substanceForm.$error, function (error) {
                    //console.log(error);
                });
            }
        };

        $scope.open = function (url) {
           $scope.modalInstance = $uibModal.open({
                templateUrl: url,
                scope: $scope,
                size: 'lg'
            });
        };

        $scope.close = function () {
            $scope.modalInstance.close();
        };

        $scope.submitSubstanceConfirm = function () {
            var f = function () {
                var url = baseurl + "assets/templates/modals/substance-submission.html";
                $scope.open(url);
            };
            $scope.validateSubstance(f);
        };

		$scope.dismissAll = function (){
				$scope.errorsArray =[];
			$scope.canSubmit=$scope.noErrors();
		};

        $scope.dismiss = function (err){
				_.pull($scope.errorsArray,err);
			$scope.canSubmit=$scope.noErrors();
		};

		$scope.canSubmit=false;

		$scope.noErrors = function (){
            var errs = _.filter($scope.errorsArray, function(err){
                if(err.messageType==="ERROR" || err.messageType==="WARNING" ){
                    return err;
                }
            });
			 return (errs.length<=0);
		};

        $scope.parseErrorArray = function(errorArr){
            _.forEach(errorArr, function(value){
                if (value.messageType == "WARNING")
                    value.class = "warning";
                if (value.messageType == "ERROR")
                    value.class = "danger";
                if (value.messageType == "INFO")
                    value.class = "info";
                if (value.messageType == "SUCCESS")
                    value.class = "success";
            });
            errorArr= _.difference(errorArr,(_.filter(errorArr, ['class', 'info'])));
            return errorArr;
        };

        $scope.validateSubstance = function (callback) {
            var sub = angular.toJson($scope.substance.$$flattenSubstance());
            console.log(sub);
            $scope.errorsArray = [];
            $http.post(baseurl + 'api/v1/substances/@validate', sub).success(function (response) {
                $scope.errorsArray = $scope.parseErrorArray(response.validationMessages);
                  $scope.canSubmit=$scope.noErrors();
               if(callback){
                   callback();
               }
            });
        };


        $scope.submitSubstance = function () {
            var url;
            var sub={};
            $scope.close();
            //  $scope.$broadcast('show-errors-check-validity');
            //      $scope.checkErrors();
            /*console.log($scope.nameForm);
             if($scope.nameForm.$dirty){
             alert('Name Form not saved');
             }*/
            /*            console.log($scope);
             if(!$scope.substanceForm.$valid) {
             alert("not valid");
             }*/
            /*            var r = confirm("Are you sure you'd like to submit this substance?");
             if (r != true) {
             return;
             }*/
            console.log($scope);
            if (_.has($scope.substance, 'update')) {
                sub = angular.toJson($scope.substance.$$flattenSubstance());
                console.log(JSON.stringify(sub));
                $http.put(baseurl + 'api/v1/substances', sub, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(function (response) {
                    console.log(response);
                    url = baseurl + "assets/templates/modals/update-success.html";
                    $scope.postRedirect = response.data.uuid;
                    $scope.open(url);
                }, function(response){
                    console.log(response);
                    $scope.errorsArray = $scope.parseErrorArray(response.data.validationMessages);
                    url = baseurl + "assets/templates/modals/submission-failure.html";
                    $scope.open(url);
                });
            } else {
                sub = angular.toJson($scope.substance.$$flattenSubstance());
                $http.post(baseurl + 'api/v1/substances', sub, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(function (response) {
                    //console.log(response);
                    $scope.postRedirect = response.data.uuid;
                    var url = baseurl + "assets/templates/modals/submission-success.html";
                    $scope.open(url);
                }, function(response){
                    $scope.errorsArray = $scope.parseErrorArray(response.data.validationMessages);
                    url = baseurl + "assets/templates/modals/submission-failure.html";
                    $scope.open(url);
                });
            }
        };

        $scope.reset = function (form) {
            $scope.$broadcast('show-errors-reset');
            form.$setPristine();
        };

        $scope.selected = false;

        $scope.fetch = function ($query) {
            return substanceSearch.load($query);
            //return substanceSearch.search(field, $query);
        };

        $scope.approveSubstanceConfirm = function () {
            $scope.validateSubstance();
            var url = baseurl + "assets/templates/modals/substance-approval.html";
            $scope.open(url);
        };
        $scope.approveSubstance = function () {
           var  sub = angular.toJson(sub.$$flattenSubstance());
            var keyid = sub.uuid.substr(0, 8);
          //  location.href = baseurl + "substance/" + keyid + "/approve";
        };

/*
        $scope.getSiteResidue = function (subunits, site) {
            var si = site.subunitIndex;
            var ri = site.residueIndex;
            for (var i = 0; i < subunits.length; i++) {
                if (subunits[i].subunitIndex === si) {
                    var res = subunits[i].sequence.substr(ri - 1, 1);
                    return res;
                }
            }
            return "";
        };
*/

        /*$scope.getAllSitesMatching = function (regexFilter) {
            var subs = $scope.getSubunits();
            var list = [];
            if (regexFilter) {
                var re = new RegExp(regexFilter, 'ig');
                var match;
                for (var i = 0; i < subs.length; i++) {
                    var sub = subs[i];
                    while ((match = re.exec(sub.sequence)) !== null) {
                        list.push({
                            subunitIndex: sub.subunitIndex,
                            residueIndex: match.index + 1
                        });
                    }
                }
                return list;
            }
            return $scope.getAllSites();
        };*/

        $scope.removeItem = function (list, item) {
            _.remove(list, function (someItem) {
                return item === someItem;
            });
        };

        $scope.applyPaster = function(){
            $scope.close();
            var originalSubstanceClass = $scope.substance.substanceClass;
            if(originalSubstanceClass ==="chemical"){
                originalSubstanceClass = "structure";
            }
            delete $scope.substance[originalSubstanceClass];
            $scope.substance.$$changeClass($scope.substanceClass);

            if ($scope.substance.substanceClass === 'chemical') {
                molChanger.setMol($scope.substance.structure.molfile);
            }
            if ($scope.substance.substanceClass === 'polymer') {
                molChanger.setMol($scope.substance.idealizedStructure.molfile);
            }
        };

        $scope.viewSubstance = function(){
            console.log("new");
            $window.location.search = null;
            console.log($window.location);
            console.log($location);
            $window.location.href = baseurl+'substance/' + $scope.postRedirect.split('-')[0];
          //  $window.location.search =null;
        };

        $scope.redirect = function(){
            localStorageService.set('tempsubstance', $scope.substance);
             $window.location.search = "kind="+ $scope.substance.substanceClass;

        };

        $scope.getRange = function (start, end) {
            return _.reverse(_.range(start, (end - 1 + 2)));
        };

        $scope.submitpaster = function (input) {
            $scope.substanceClass = $location.$$search.kind;
            Substance.$$setSubstance(JSON.parse(input)).then(function(data){
                $scope.substance = data;
                if($scope.substance.substanceClass != $scope.substanceClass) {
                    var url = baseurl + "assets/templates/modals/paste-redirect-modal.html";
                    $scope.open(url);
                }
            });

        };

        $scope.bugSubmit = function (bugForm) {
            //console.log(bugForm);
        };

        $scope.setEditId = function (editid) {
            localStorageService.set('editID', editid);
        };

        //method for injecting a large structure image on the browse page//
        $scope.showLarge = function (id, divid, ctx) {
            var result = document.getElementsByClassName(divid);
            var elementResult = angular.element(result);
            if ($scope.stage === true) {
                $scope.stage = false;
                var childScope = $scope.$new();
                var rend;
                if (!_.isUndefined(ctx)) {
                    rend = '<rendered size="500" id=' + id + ' ctx=' + ctx + '></rendered>';
                } else {
                    rend = '<rendered size="500" id=' + id + '></rendered>';
                }
                var compiledDirective = $compile(rend);
                var directiveElement = compiledDirective(childScope);
                elementResult.append(directiveElement);
            } else {
                childScope.$destroy();
                elementResult.empty();
                $scope.stage = true;
            }
        };

    });

    ginasApp.directive('loading', function ($http) {
        return {
            template: "<div class=\"sk-folding-cube\">\n" +
            "  <div class=\"sk-cube1 sk-cube\"></div>\n" +
            "  <div class=\"sk-cube2 sk-cube\"></div>\n" +
            "  <div class=\"sk-cube4 sk-cube\"></div>\n" +
            "  <div class=\"sk-cube3 sk-cube\"></div>\n" +
            "</div>"
        };
    });

    ginasApp.directive('scrollSpy', function ($timeout) {
        return function (scope, elem, attr) {
            scope.$watch(attr.scrollSpy, function (value) {
                $timeout(function () {
                    elem.scrollspy('refresh');
                }, 200);
            }, true);
        };
    });

    ginasApp.directive('rendered', function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                id: '@',
                size: '@',
                ctx: '@'
            },
            link: function (scope, element, attrs) {
                var url = baseurl + 'img/' + scope.id + '.svg?size={{size||150}}';
                if (!_.isUndefined(scope.ctx)) {
                    url += '&context={{ctx}}';
                }
                if (attrs.smiles) {
                    url = baseurl + "render?structure=" + attrs.smiles +"&size={{size||150}}";
                }
                var template = angular.element('<img ng-src=' + url + ' alt = "rendered image" class="tooltip-img" ng-cloak>');
                element.append(template);
                $compile(template)(scope);
            }
            //   template: '<img ng-src=\"' + baseurl + 'img/'+id+'.svg?size={{size||150}}&context={{ctx}}\">'
        };
    });

    ginasApp.directive('amount', function ($compile) {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            link: function (scope, element, attrs) {
                scope.display = function () {
                    if (!_.isUndefined(scope.value) && !_.isNull(scope.value)) {
                        var ret = "";
                        var addedunits = false;
                        var unittext = "";
                        if (scope.value.units) {
                            if (scope.value.units.display) {
                                unittext = scope.value.units.display;
                            } else {
                                unittext = scope.value.units;
                            }
                        }


                        if (scope.value) {
                            if (scope.value.type) {
                                if (scope.value.type.display) {
                                    ret += scope.value.type.display + "\n";
                                } else {
                                    ret += scope.value.type + "\n";
                                }
                            }
                            if (scope.value.average || scope.value.high || scope.value.low) {
                                if (scope.value.average) {
                                    ret += scope.value.average;
                                    if (scope.value.units) {
                                        ret += " " + unittext;
                                        addedunits = true;
                                    }
                                }
                                if (scope.value.high || scope.value.low) {
                                    ret += " [";

                                    if (scope.value.high && !scope.value.low) {
                                        ret += "<" + scope.value.high;
                                    } else if (!scope.value.high && scope.value.low) {
                                        ret += ">" + scope.value.low;
                                    } else if (scope.value.high && scope.value.low) {
                                        ret += scope.value.low + " to " + scope.value.high;
                                    }
                                    ret += "] ";
                                    if (!addedunits) {
                                        if (scope.value.units) {
                                            ret += " " + unittext;
                                            addedunits = true;
                                        }
                                    }
                                }
                                ret += " (average) ";
                            }

                            if (scope.value.highLimit || scope.value.lowLimit) {
                                ret += "\n[";
                            }
                            if (scope.value.highLimit && !scope.value.lowLimit) {
                                ret += "<" + scope.value.highLimit;
                            } else if (!scope.value.highLimit && scope.value.lowLimit) {
                                ret += ">" + scope.value.lowLimit;
                            } else if (scope.value.highLimit && scope.value.lowLimit) {
                                ret += scope.value.lowLimit + " to " + scope.value.highLimit;
                            }
                            if (scope.value.highLimit || scope.value.lowLimit) {
                                ret += "] ";
                                if (!addedunits) {
                                    if (scope.value.units) {
                                        ret += " " + unittext;
                                        addedunits = true;
                                    }
                                }
                                ret += " (limits)";
                            }

                        }
                        return ret;
                    }
                };

                var template = angular.element('<div><span class="amt">{{display()}}<br><i>{{value.nonNumericValue}}</i></span></div>');

                element.append(template);
                $compile(template)(scope);
            }
        };
    });

    ginasApp.factory('referenceRetriever', function ($http) {
        var url = baseurl + "api/v1/substances(";
        var references = {};
        var refFinder = {
            getAll: function (uuid) {
                return $http.get(url + uuid + ")/references",{cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    _.forEach(response.data, function (ref, index) {
                        _.set(ref, '$$index', index + 1);
                    });
                    return response.data;
                });
            },
            getIndex: function (uuid, refuuid) {
                return $http.get(url + uuid + ")/references",{cache: true},{
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    return _.indexOf(response.data, refuuid);
                });
            }
        };
        return refFinder;
    });

    ginasApp.directive('referencesmanager', function ($compile, $templateRequest, referenceRetriever, toggler) {
        return {
            controller: function ($scope) {
                this.scope = $scope;
                this.referenceRetriever = referenceRetriever;
                $scope.addClass = [];
                this.setClass = function (index) {
                    $scope.addClass[index] = "success";
                };
                this.getClass = function (index) {
                    return $scope.addClass[index];
                };

                this.removeClass = function () {
                    $scope.addClass = [];
                };

                this.scrollTo = function () {
                    $scope.scrollTo('refs');
                };

                this.toggle = function (scope, divid) {
                    $scope.addClass = [];
                    var url = baseurl + "assets/templates/reference-table.html";
                    scope.references = _.sortBy(scope.objreferences, '$$index', function (ref) {
                        if (!scope.stage == false || _.isUndefined(scope.stage)) {
                            $scope.addClass[ref.$$index] = "success";
                        }
                    });
                    toggler.show(scope, divid, url);

                };
            }
        };
    });

    ginasApp.directive('reftable', function () {
        return {
            scope: {
                substance: '=?',
                objreferences: '=?',
                references: '=?',
                divid: '=?'
            },
            require: '^referencesmanager',
            link: function (scope, element, attrs, referencesCtrl) {
                referencesCtrl.referenceRetriever.getAll(scope.substance).then(function (response) {
                    scope.references = response;
                });

                scope.getClass = function (index) {
                    return referencesCtrl.getClass(index);
                }
            },
            templateUrl: baseurl + "assets/templates/reference-table.html"
        };
    });

    ginasApp.directive('indices', function ($compile) {
        return {
            scope: {
                substance: '=',
                references: '='
            },
            require: '^referencesmanager',
            link: function (scope, element, attrs, referencesCtrl) {
                var indices = [];
                var links = [];
                var objreferences = [];

                referencesCtrl.referenceRetriever.getAll(scope.substance).then(function (response) {
                    _.forEach(scope.references, function (ref) {
                        objreferences.push(_.find(response, ['uuid', ref.term]));
                        indices.push(_.indexOf(response, _.find(response, ['uuid', ref.term])) + 1);
                        // });
                    });
                    indices = _.sortBy(indices);
                    scope.objreferences = objreferences;

                    _.forEach(indices, function (i) {
                        var link = '<a ng-click="showActive(' + i + ')" uib-tooltip="view reference">' + i + '</a>';
                        links.push(link);
                    });
                    var templateString = angular.element('<div class ="row"><div class ="col-md-8">' + _.join(links, ', ') + ' </div><div class="col-md-4"><span class="btn btn-primary pull-right" type="button" uib-tooltip="Show all references" ng-click="toggle()"><i class="fa fa-long-arrow-down"></i></span><div></div>');
                    element.append(angular.element(templateString));
                    $compile(templateString)(scope);
                });

                scope.toggle = function () {
                    referencesCtrl.toggle(scope, attrs.divid);
                };

                scope.showActive = function (index) {
                    referencesCtrl.removeClass();
                    referencesCtrl.setClass(index);
                    referencesCtrl.scrollTo();
                };
            }
        };
    });

    ginasApp.directive('citation', function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                citation: '='
            },
            link: function (scope, element, attrs) {
                var template;
                if (!_.isNull(scope.citation.url)) {
                    template = angular.element('<a href = {{citation.url}} target = "_self"><span>{{citation.citation}}</span></a>');
                } else {
                    template = angular.element('<span>{{citation.citation}}</span>');
                }
                element.append(template);
                $compile(template)(scope);
            }
        };

    });

    ginasApp.directive('siteView', function (siteList) {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                parent: '=',
                field: '='
            },
            link: function (scope, element, attrs) {
                if (!_.isUndefined(scope.referenceobj) && !_.isEmpty(scope.referenceobj)) {
                    if (_.has(scope.referenceobj, 'sites')) {
                        scope.referenceobj.sites.$$displayString = siteList.siteString(scope.referenceobj.sites);
                    } else {
                        if(scope.field) {
                            scope.referenceobj[scope.field].$$displayString = siteList.siteString(scope.referenceobj[scope.field]);
                        }else{
/*
                    alert('error');
*/
                        }

                    }
                }
            },
            template: '<div><div><span>{{referenceobj.$$displayString || referenceobj[field].$$displayString}}</span><br></div><div ng-if="referenceobj.sites.length"><span>({{referenceobj.sites.length}} sites)</span></div></div>'
        };
    });

    ginasApp.directive('comment', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            template: '<div><span id="comment-text">{{value|limitTo:40}}...</span></div>'
        };
    });

    ginasApp.directive('access', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            template: '<div><i class="fa fa-lock fa-2x warning" id="access-directive"></i><span ng-repeat = "access in value"><br>{{access.display || access}}</span></div>'
        };
    });

    ginasApp.directive('parameters', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                parameters: '='
            },
            template: '<div ng-repeat="p in parameters">{{p.name||p.parameterName}} <amount value="p.value"></amount></div>'
        };
    });

    ginasApp.directive('aminoAcid', function ($compile, $templateRequest, $anchorScroll) {

        return {
            restrict: 'E',
            scope: {
                acid: '='
            },
            link: function (scope, element, attrs) {
                scope.bridged = false;
                var aa = scope.acid;
                var template;
                if (aa.valid == false) {
                    template = angular.element('<a href="#" class= "invalidAA" tooltip-class="invalidTool" uib-tooltip="INVALID">{{acid.value}}</a>');
                    element.html(template).show();
                    $compile(element.contents())(scope);
                } else if (aa.valid === 'true' && aa.index % 10 === 0) {
                    template = angular.element('&nbsp;');
                    element.html(template).show();
                    $compile(element.contents())(scope);

                } else {
                    if (_.has(aa, 'structuralModifications')) {
                        scope.acidClass = "modification";
                    } else if (_.has(aa, 'disulfide')) {
                        scope.acidClass = "disulfide";
                    } else if (_.has(aa, 'otherLinks')) {
                        scope.acidClass = "otherLinks";
                    } else if (_.has(aa, 'glycosylation')) {
                        scope.acidClass = "glycosylation";
                    } else if (_.has(aa, 'sugar')) {
                        scope.acidClass = "sugar";
                    } else if (_.has(aa, 'linkage')) {
                        scope.acidClass = "sugar";
                    } else {
                        scope.acidClass = 'plain';
                    }

                    if (scope.acidClass === 'disulfide' || scope.acidClass === 'otherLinks') {
                        $templateRequest(baseurl + "assets/templates/tooltips/bridge-tooltip-template.html").then(function (html) {
                            template = angular.element(html);
                            element.html(template).show();
                            $compile(element.contents())(scope);
                        });
                    } else {
                        $templateRequest(baseurl + "assets/templates/tooltips/tooltip-template.html").then(function (html) {
                            template = angular.element(html);
                            element.html(template).show();
                            $compile(element.contents())(scope);

                        });
                    }
                }

                scope.showBridge = function () {
                    scope.bridged = !scope.bridged;
                };

                scope.scrollTo = function (div, acid) {
                    $anchorScroll(div);
                };
            }
        };
    });

    ginasApp.service('APIFetcher', function ($http) {
        var url = baseurl + "api/v1/substances(";
        var fetcher = {
            fetch: function (uuid) {
                return $http.get(url + uuid + ")", {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    return response.data;
                });
            }
        };
        return fetcher;

    });

    ginasApp.directive('subunit', function (CVFields, APIFetcher) {

        return {
            restrict: 'E',
            scope: {
                parent: '=?',
                obj: '=?',
                uuid: '=',
                index: '=',
                view: '@',
                numbers: '='
            },
            link: function (scope, element, attrs) {
                scope.numbers = true;
                scope.edit = false;

                scope.toggleEdit = function () {
                    scope.edit = !scope.edit;
                };

                scope.getResidues = function () {
                    if (scope.parent.substanceClass === 'protein') {
                        CVFields.getCV("AMINO_ACID_RESIDUE").then(function (data) {
                            scope.residues = data.data.content[0].terms;
                            scope.parseSubunit();
                        });
                    } else {
                        CVFields.getCV("NUCLEIC_ACID_BASE").then(function (data) {
                            scope.residues = data.data.content[0].terms;
                            scope.parseSubunit();
                        });
                    }
                };


                scope.getType = function (aa) {
                    if (aa == aa.toLowerCase()) {
                        return 'D-';
                    }
                    else {
                        return 'L-';
                    }
                };

                scope.objectParser = function (subObj, siteObj, name) {
                    var cv;
                    var newobj;
                    _.forEach(subObj, function (value, key) {
                        if (_.isArray(value) && value.length > 0) {
                            _.forEach(value, function (mod) {
                                if (name === 'glycosylation') {
                                    if (mod.subunitIndex == siteObj.subunitIndex && mod.residueIndex == siteObj.residueIndex) {
                                        _.set(siteObj, name, true);
                                    }
                                } else if (name === 'structuralModifications') {
                                    _.forEach(mod.sites, function (site) {
                                        if (site.subunitIndex == siteObj.subunitIndex && site.residueIndex == siteObj.residueIndex) {
                                            _.set(siteObj, name, mod.molecularFragment);
                                        }
                                    });
                                } else if (name === 'sugar' || name === 'linkage') {
                                    _.forEach(mod.sites, function (site) {
                                        if (site.subunitIndex == siteObj.subunitIndex && site.residueIndex == siteObj.residueIndex) {
                                            var obj = mod[name];
                                            _.set(siteObj, name, obj);
                                            if (!_.has(obj, 'display')) {
                                                var type = _.toUpper(name);
                                                type = 'NUCLEIC_ACID_' + type;
                                                CVFields.getCV(type).then(function (data) {
                                                    cv = data.data.content[0].terms;
                                                    newobj = _.find(cv, ['value', obj]);
                                                    _.set(siteObj, name, newobj);
                                                });
                                            }
                                        }
                                    });
                                } else {
                                    if (mod.sites[0].subunitIndex == siteObj.subunitIndex && mod.sites[0].residueIndex == siteObj.residueIndex) {
                                        var bridge = mod.sites[1];
                                        _.set(siteObj, name, bridge);
                                    } else if (mod.sites[1].subunitIndex == siteObj.subunitIndex && mod.sites[1].residueIndex == siteObj.residueIndex) {
                                        var bridge = mod.sites[0];
                                        _.set(siteObj, name, bridge);
                                    }
                                }
                            });
                        }
                    });
                };

                scope.parseSubunit = function () {
                    if (!_.has(scope.parent, '$$subunitDisplay')) {
                        _.set(scope.parent, '$$subunitDisplay', []);
                    }
                    scope.obj.$$cysteineIndices = [];
                    var display = [];
                    _.forEach(scope.obj.sequence, function (aa, index) {
                        var obj = {};
                        obj.value = aa;
                        var temp = (_.find(scope.residues, ['value', aa.toUpperCase()]));
                        if (!_.isUndefined(temp)) {
                            obj=_.pickBy(temp, _.isString);
                            obj.value = aa;
                            //obj.name = temp.display;
                            obj.valid = true;
                            if (scope.obj.subunitIndex) {
                                obj.subunitIndex = scope.obj.subunitIndex;
                            } else {
                                obj.subunitIndex = scope.obj.index;
                            }
                            obj.residueIndex = index - 0 + 1;

                            if (_.has(scope.parent, 'modifications.structuralModifications')) {
                                scope.objectParser(scope.parent.modifications, obj, 'structuralModifications');
                            }

                            if (scope.parent.substanceClass === 'protein') {
                                obj.type = scope.getType(aa);
                                if (_.has(scope.parent.protein, 'glycosylation')) {
                                    scope.objectParser(scope.parent.protein.glycosylation, obj, 'glycosylation');
                                }
                                if (_.has(scope.parent.protein, 'disulfideLinks')) {
                                    var linksObj = {};
                                    _.set(linksObj, 'links', scope.parent.protein.disulfideLinks);
                                    scope.objectParser(linksObj, obj, 'disulfide');
                                }
                                if (_.has(scope.parent.protein, 'otherLinks')) {
                                    var linksObj = {};
                                    _.set(linksObj, 'otherLinks', scope.parent.protein.otherLinks);
                                    scope.objectParser(linksObj, obj, 'otherLinks');
                                }

                            } else {
                                if (_.has(scope.parent.nucleicAcid, 'sugars')) {
                                    var linksObj = {};
                                    _.set(linksObj, 'sugar', scope.parent.nucleicAcid.sugars);
                                    scope.objectParser(linksObj, obj, 'sugar');
                                }
                                if (_.has(scope.parent.nucleicAcid, 'linkages')) {
                                    var linksObj = {};
                                    _.set(linksObj, 'linkage', scope.parent.nucleicAcid.linkages);
                                    scope.objectParser(linksObj, obj, 'linkage');
                                }
                            }
                            if (aa.toUpperCase() == 'C') {
                                obj.cysteine = true;
                                scope.obj.$$cysteineIndices.push(index + 1);
                            }

                        } else {
                            obj.valid = false;
                        }


                        display.push(obj);
                    });
                    display = _.chunk(display, 10);
                    _.set(scope.obj, '$$subunitDisplay', display);
                    scope.parent.$$subunitDisplay.push(display);
                };

                scope.highlight = function (acid) {
                    var bridge = {};
                    if (_.has(acid, 'disulfide')) {
                        bridge = acid.disulfide;
                    } else {
                        bridge = acid.otherLinks;
                    }
                    var allAA = element[0].querySelectorAll('amino-acid');
                    var targetElement = angular.element(allAA[bridge.residueIndex - 1]);
                    targetElement.isolateScope().showBridge();
                };


//******************************************************************this needs a check to delete the subunit if cleaning the subunit results in an empty string
                scope.cleanSequence = function () {
                    scope.obj.sequence = _.filter(scope.obj.sequence, function (aa) {
                        var temp = (_.find(scope.residues, ['value', aa.toUpperCase()]));
                        if (!_.isUndefined(temp)) {
                            return temp;
                        }
                    }).toString().replace(/,/g, '');
                    scope.parseSubunit();
                };

                var display = [];
                if (_.isUndefined(scope.parent)) {
                    APIFetcher.fetch(scope.uuid).then(function (data) {
                        scope.parent = data;
                        if (_.has(data, 'protein')) {
                            scope.obj = data.protein.subunits[scope.index];
                        } else {
                            scope.obj = data.nucleicAcid.subunits[scope.index];
                        }
                        scope.getResidues();
                    });
                } else {
                    scope.getResidues();
                }

            },
            templateUrl: baseurl + "assets/templates/subunit.html"
        };
    });

    ginasApp.directive('substanceChooserSelector', function ($templateRequest, $compile, toggler, substanceFactory, spinnerService, CVFields) {
        return {
            replace: true,
            restrict: 'E',
            scope: {
                subref: '=ngModel',
                referenceobj: '=',
                formname: '@',
                field: '@',
                label: '@',
                type: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                var formHolder;
                var template;

                scope.toggle = function () {
                    if(scope.stage==false){
                        scope.q = null;
                    }
                    toggler.toggle(scope, scope.formname, formHolder, scope.referenceobj);
                };

                scope.stage = true;

                scope.fetch = function (term, skip) {
                    var cap =  _.toUpper(term);
                    spinnerService.show('subrefSpinner');
                    var url = baseurl + "api/v1/substances?filter=names.name='" +
                            // var url = baseurl + "api/v1/substances/search?q=" +
                       cap + "'&top=" + scope.top + "&skip=" + skip;
                    substanceFactory.getSubstances(scope.q).then(function (response) {
                        /*                 $http.get(url, {cache: true}).then(function (response, status, headers, config) {*/
                        console.log(response);
                        scope.data = response.data.content;
                        spinnerService.hide('subrefSpinner');
                        var template = angular.element('<substance-viewer data = data format= "subref"></substance-viewer>');
                        toggler.refresh(scope, scope.formname, template);
                    });
                };

                scope.createSubref = function (selectedItem) {
                    console.log("GGGGGGG");
                    var temp = {};
                    temp.refuuid = selectedItem.uuid;
                    temp.refPname = selectedItem._name;
                    temp.approvalID = selectedItem.approvalID;
                    temp.substanceClass = "reference";
                    if (attrs.definition) {
                        var r = {relatedSubstance: temp};
                        CVFields.getCV('RELATIONSHIP_TYPE').then(function (response) {
                            var type = _.find(response.data.content[0].terms, ['value', 'SUB_ALTERNATE->SUBSTANCE']);
                            r.type = type;
                        });
                        /*
                         var r = {type:{value:'SUB_ALTERNATE->SUBSTANCE', display:'SUB_ALTERNATE->SUBSTANCE'}, relatedSubstance: temp};
                         */
                        if (!_.has(scope.referenceobj, 'relationships')) {
                            _.set(scope.referenceobj, 'relationships', []);
                        }
                        scope.referenceobj.relationships.push(r);
                    }
                    _.set(scope.referenceobj, scope.field, angular.copy(temp));
                    scope.q = null;
                    scope.toggle();
                };




                switch (scope.type) {
                    case "lite":
                        $templateRequest(baseurl + "assets/templates/substance-select.html").then(function (html) {
                            template = angular.element(html);
                            element.append(template);
                            $compile(template)(scope);
                        });
                        break;
                    case "search":
                        $templateRequest(baseurl + "assets/templates/substance-search.html").then(function (html) {
                            template = angular.element(html);
                            element.append(template);
                            $compile(template)(scope);
                        });
                        if (attrs.definition) {
                            scope.definition = attrs.definition;
                        }
                        formHolder = '<substance-search-form referenceobj = referenceobj formname =formname field =field q=q  definition={{definition}}></substance-search-form>';
                        break;
                }
            }
        };
    });

    ginasApp.directive('substanceSearchForm', function (CVFields, toggler, substanceFactory, spinnerService) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                field: '=',
                q: '=',
                formname:'='
            },
            templateUrl: baseurl + 'assets/templates/selectors/substanceSelector.html',
            link: function (scope, element, attrs) {
                scope.results = {};
                scope.top = 8;
                scope.testb = 0;
                scope.createSubref = function (selectedItem) {
                    var temp = {};
                    temp.refuuid = selectedItem.uuid;
                    temp.refPname = selectedItem._name;
                    temp.approvalID = selectedItem.approvalID;
                    temp.substanceClass = "reference";
                    if (attrs.definition) {
                        var r = {relatedSubstance: temp};
                        CVFields.getCV('RELATIONSHIP_TYPE').then(function (response) {
                            var type = _.find(response.data.content[0].terms, ['value', 'SUB_ALTERNATE->SUBSTANCE']);
                            r.type = type;
                        });
                        /*
                         var r = {type:{value:'SUB_ALTERNATE->SUBSTANCE', display:'SUB_ALTERNATE->SUBSTANCE'}, relatedSubstance: temp};
                         */
                        if (!_.has(scope.referenceobj, 'relationships')) {
                            _.set(scope.referenceobj, 'relationships', []);
                        }
                        scope.referenceobj.relationships.push(r);
                    }
                    _.set(scope.referenceobj, scope.field, angular.copy(temp));
                    scope.q = null;
                    scope.$parent.$parent.toggle();
                };

                scope.fetch = function (term, skip) {
                    spinnerService.show('subrefSpinner');
                    var url = baseurl + "api/v1/substances?filter=names.name='" +
                   // var url = baseurl + "api/v1/substances/search?q=" +
                        term + "'&top=" + scope.top + "&skip=" + skip;
                        substanceFactory.getSubstances(scope.q).then(function (response) {
      /*                 $http.get(url, {cache: true}).then(function (response, status, headers, config) {*/
                        console.log(response);
                        scope.data = response.data.content;
                     spinnerService.hide('subrefSpinner');
                           var template = angular.element('<substance-viewer data = data format="subref"></substance-viewer>');
                            toggler.refresh(scope, scope.formname, template);
                 });
/*
                    responsePromise.error(function (data, status, headers, config) {
                        scope.searching = false;
                    });*/
                };

                scope.fetch(scope.q, 0);

                scope.nextPage = function () {
                    scope.fetch(scope.term, scope.results.skip + scope.results.top);
                };
                scope.prevPage = function () {
                    scope.fetch(scope.term, scope.results.skip - scope.results.top);
                };

            }
        };
    });

    ginasApp.directive('substanceView', function ($compile) {
        return {
            replace: true,
            restrict: 'E',
            scope: {
                subref: '=',
                size:'='
            },
            link: function (scope, element) {
                var template = angular.element('<div><rendered id = {{subref.refuuid}} size = {{size}}></rendered><br/><code>{{subref.refPname}}</code></div>');
                element.append(template);
                $compile(template)(scope);
            }
        };
    });

    //this is solely to set the molfile in the sketcher externally
    ginasApp.service('molChanger', function ($http, CVFields, UUID) {
        var sketcher;
        this.setSketcher = function (sketcherInstance) {
            sketcher = sketcherInstance;
        };
        this.setMol = function (mol) {
            sketcher.setMolfile(mol);
        };
    });

    ginasApp.directive('sketcher', function ($compile, $http, UUID, polymerUtils, CVFields, localStorageService, molChanger) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                structure: '=?',
                mol: '=ngModel'
            },

            link: function (scope, element, attrs) {
                var url = baseurl + 'structure';

                if (!_.isUndefined(scope.parent.structure)) {
                    scope.mol = scope.parent.structure.molfile;
                }
                var template = angular.element('<div id="sketcherForm" dataformat="molfile"></div>');
                element.append(template);
                $compile(template)(scope);

                scope.updateMol = function () {
                    var url = baseurl + 'structure';
                    $http.post(url, scope.mol, {
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (data) {
                        if (scope.parent.substanceClass === "polymer") {
                            scope.parent.idealizedStructure = data.structure;
                            scope.structure = data.structure;
                                CVFields.getCV("POLYMER_SRU_TYPE").then(function (response) {
                                    for (var i in data.structuralUnits) {
                                        var cv = response.data.content[0].terms;
                                        data.structuralUnits[i].type = _.find(cv, ['value', data.structuralUnits[i].type]);
                                    }
                                    polymerUtils.setSRUConnectivityDisplay(data.structuralUnits);
                                    scope.parent.polymer.structuralUnits = data.structuralUnits;
                                });
                        }
                        scope.parent.structure = data.structure;
                        scope.parent.moieties = [];
                        _.forEach(data.moieties, function (m) {
                            //m._id = UUID.newID();
                            scope.parent.moieties.push(m);
                        });
                        if(data.structure){
                        	_.set(scope.parent, 'q', data.structure.smiles);
                        }
                    });
                };

                scope.sketcher = new JSDraw("sketcherForm");
                scope.sketcher.options.data = scope.mol;
                scope.sketcher.setMolfile(scope.mol);
                scope.sketcher.options.ondatachange = function () {
                	//console.log("DATA CHANGED");
                    scope.mol = scope.sketcher.getMolfile();
                    scope.updateMol();
                };
                molChanger.setSketcher(scope.sketcher);
                var structureid = (localStorageService.get('structureid') || false);
                if (localStorageService.get('editID')) {
                    structureid = false;
                }

                if (scope.parent.substanceClass === 'polymer' &&  scope.parent.polymer.displayStructure) {
                    scope.mol = scope.parent.polymer.displayStructure.molfile;
                    scope.updateMol();
                }

                if (structureid) {
                    $http({
                        method: 'GET',
                        url: baseurl + 'api/v1/structures/' + structureid
                    }).success(function (data) {
                        scope.sketcher.setMolfile(data.molfile);
                        _.set(scope.parent, 'q', data.smiles);
                        localStorageService.remove('structureid');
                    });
                }

            }
        };
    });

    ginasApp.directive('modalButton', function ($compile, $templateRequest, $http, $uibModal, molChanger, FileReader) {
        return {
            /*            restrict: 'AE',
             replace: 'true',*/
            scope: {
                type: '=',
                structureid: '=',
                format: '@',
                referenceobj:'=',
                parent:'='
            },
            link: function (scope, element, attrs) {
                var modalInstance;
                var childScope;
                var template;
                var templateUrl;
                scope.warnings = [];
                scope.stage = true;
                switch (attrs.type) {
                    case "upload":
                        template = angular.element(' <a aria-label="Upload" uib-tooltip ="Upload" structureid=structureid format=format export><span class="sr-only">Upload Data</span><i class="fa fa-upload fa-2x"></i></a>');
                        element.append(template);
                        $compile(template)(scope);
                        break;
                    case "import":
                        template = angular.element(' <a aria-label="Import" uib-tooltip ="Import" ng-click="open()"><span class="sr-only">Import Data</span><i class="fa fa-clipboard fa-2x success"></i></a>');
                        element.append(template);
                        $compile(template)(scope);
                        templateUrl = baseurl + "assets/templates/modals/mol-import.html";
                        break;
                    case "export":
                        template = angular.element(' <a aria-label="Export" uib-tooltip ="Export" structureid=structureid format=format ng-click = "getExport()"><span class="sr-only">Export Data</span><i class="fa fa-external-link fa-2x success"></i></a>');
                        element.append(template);
                        $compile(template)(scope);
                        templateUrl = baseurl + "assets/templates/modals/mol-export.html";
                        break;
                    case "reference":
                        template = angular.element(' <a aria-label="Export" uib-tooltip ="Export" structureid=structureid format=format ng-click = "open()"><span class="sr-only">Export Data</span><i class="fa fa-external-link fa-2x success"></i></a>');
                        element.append(template);
                        $compile(template)(scope);
                        templateUrl = baseurl + "assets/templates/modals/reference-modal.html";
                        break;
                }

                scope.setPreview = function (file) {
                    FileReader.readAsText(file, scope).then(function(response){
                        scope.molfile=response;
                    });
                };

                scope.resolveMol = function (mol) {
                    var url = baseurl + 'structure';
                    $http.post(url, mol, {
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (data) {
                        if (!_.isEmpty(data)) {
                            molChanger.setMol(data.structure.molfile);
                            scope.close();
                        } else {
                            var warning = {type: 'warning', message: 'not a vaild molfile'};
                            scope.warnings.push(warning);
                        }
                    });
                };

                scope.getSmiles = function () {
                    var url = baseurl + 'export/' + scope.structureid + '.smiles';
                    $http.get(url, {
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (response) {
                        return response;
                    });
                };


                scope.getExport = function () {
                    var url = baseurl + 'export/' + scope.structureid + '.' + scope.format;
                    $http.get(url, {
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (response) {
                        scope.exportData = response;
                        url = baseurl + 'export/' + scope.structureid + '.smiles';
                        $http.get(url, {
                            headers: {
                                'Content-Type': 'text/plain'
                            }
                        }).success(function (response) {
                            scope.exportSmiles = response;
                        });
//                           var warnHead = response.headers("EXPORT-WARNINGS").split("___")[0];
                        //                          scope.warnings = JSON.parse(warnHead);
                        scope.open();
                    });
                };

                scope.close = function () {
                    modalInstance.close();
                };

                scope.open = function () {
                    modalInstance = $uibModal.open({
                        templateUrl: templateUrl,
                        size: 'lg',
                        scope: scope
                    });
                }
            }
        }
    });

    ginasApp.directive('referenceModalButton', function ($uibModal) {
        return {
            scope: {
                referenceobj:'=',
                parent:'=',
                edit:'=?'
            },
            templateUrl: baseurl + "assets/templates/selectors/reference-selector.html",
            controller: function ($scope) {
                var modalInstance;
                $scope.close = function () {
                    $scope.$broadcast ('save');
                    modalInstance.close();
                };

                $scope.open = function () {
                    modalInstance = $uibModal.open({
                        templateUrl: baseurl + "assets/templates/modals/reference-modal.html",
                        size: 'xl',
                        scope: $scope,
                        resolve: {
                            parent :function(){
                                return $scope.substance;
                            }
                        }
                    });
                }
            }
        }
    });

    ginasApp.directive('switch', function () {
        return {
            restrict: 'AE',
            replace: true,
            transclude: true,
            template: function (element, attrs) {
                var html = '';
                html += '<span';
                html += ' class="toggleSwitch' + (attrs.class ? ' ' + attrs.class : '') + '"';
                html += attrs.ngModel ? ' ng-click="' + attrs.ngModel + '=!' + attrs.ngModel + (attrs.ngChange ? '; ' + attrs.ngChange + '()"' : '"') : '';
                html += ' ng-class="{ checked:' + attrs.ngModel + ' }"';
                html += '>';
                html += '<small></small>';
                html += '<input aria-label ="toggle-switch" type="checkbox"';
                html += attrs.id ? ' id="' + attrs.id + '"' : '';
                html += attrs.name ? ' name="' + attrs.name + '"' : '';
                html += attrs.ngModel ? ' ng-model="' + attrs.ngModel + '"' : '';
                html += ' style="display:none" />';
                html += '<span class="switch-text">';
                /*adding new container for switch text*/
                html += attrs.on ? '<span class="on">' + attrs.on + '</span>' : '';
                /*switch text on value set by user in directive html markup*/
                html += attrs.off ? '<span class="off">' + attrs.off + '</span>' : ' ';
                /*switch text off value set by user in directive html markup*/
                html += '</span>';
                return html;
            }
        };
    });

    ginasApp.directive('deleteButton', function () {
        return {
            restrict: 'E',
            template: '<a ng-click="deleteObj()" uib-tooltip="Delete Item"><i class="fa fa-times fa-2x danger"></i></a>',
            link: function (scope, element, attrs) {
                scope.deleteObj = function () {
                    if (scope.parent) {
                        var arr = _.get(scope.parent, attrs.path);
                        arr.splice(arr.indexOf(scope.obj), 1);
                    } else {
                        scope.substance[attrs.path].splice(scope.substance[attrs.path].indexOf(scope.obj), 1);
                    }
                };
            }
        };
    });

    ginasApp.directive('errorMessage', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                message: '='
            },
            link: function (scope, element, attrs) {

            },
            template: '<span><h1>{{message}}</h1></span>'

        };

    });

    ginasApp.directive("treeView", function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                text: '='
            },
            link: function (scope, element, attrs) {
                scope.codes = [];
                scope.link = [];
                var template ='<div>';
                _.forEach(scope.text.split('|'), function(c){
                    scope.link.push(c);
;                    scope.codes.push(c.split('['));
                });
                _.forEach(scope.codes, function(c, key) {
                    var link = "app/substances?q=comments:%22"+ _.join(_.slice(scope.link,0,key+1),'|')+ "%22";
                    template += '<ul class="tree-list"><li><a href = "' + link + '" uib-tooltip="Search ginas for '+ c[0]+'" target ="_self">'+ c[0]+'</a>';
                    if(c[1]){
                    template += '<span>'+'['+ c[1]+'</span>';
                    }
                });
                for(var i = 0; i< scope.codes.length; i++){
                    template+='</li></ul>';
                }
                template+='</div>';
                    element.append(angular.element(template));
                $compile(template)(scope);
         }
}
    });

})();
