(function () {
    'use strict';
    var ginasApp = angular.module('ginas', ['ngAria', 'ngMessages', 'ngResource', 'ui.bootstrap', 'ui.bootstrap.showErrors',
        'LocalStorageModule', 'ngTagsInput', 'jsonFormatter', 'ginasForms', 'ginasFormElements', 'ginasAdmin', 'diff-match-patch',
        'angularSpinners', 'filterListener', 'validatorListener'
    ]).run(function($anchorScroll, $location, $window) {
            $anchorScroll.yOffset = 150;   // always scroll by 100 extra pixels
     })
        .config(function (localStorageServiceProvider, $locationProvider) {
            localStorageServiceProvider
                .setPrefix('ginas');
            $locationProvider.html5Mode({
                enabled: true,
                hashPrefix: '!',
                requireBase: true
            });
        });

    ginasApp.factory('Substance', function ($q, CVFields, UUID, polymerUtils, siteList) {

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
                                          //  _.set(newcv, 'value', value + ' (not in CV)');
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
                                  //  _.set(newcv, 'value', sub[field] + ' (not in CV)');

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
                    _.set(substance.structure, 'opticalActivity', {value: "UNSPECIFIED", display:"UNSPECIFIED"});
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
                    break;
            }
            if (!substance.references) {
                substance.references = [];
            }
            substance.access = [{value: 'protected', display: 'PROTECTED'}];
            return substance;
        };

        substance.$$getClass = function () {
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
                    _.forEach(sub.protein.disulfideLinks, function (link, key) {
                         _.forEach(link.sites, function (site, sitekey) {
                            link.sites[sitekey] = _.pick(site, ['subunitIndex','residueIndex']);
                            });
                    });
                }
                if (_.has(sub.protein, 'otherLinks')) {
                    _.forEach(sub.protein.otherLinks, function (value, key) {
                        var otherLink = {};
                        var sites = _.toArray(value.sites);
                        if (sites.length % 2 != 0) {
                            sites = _.dropRight(sites);
                        }
                        sub.protein.otherLinks[key].sites = sites;
                    });
                }
            }
            sub = flattenCV(sub);
            if (_.has(sub, 'moieties')) {
                _.forEach(sub.moieties, function (m) {
                    if(!_.has(sub, '$$update') || m["$$new"]){
                                                m.id = UUID.newID();
                    }
                });
            }
            if (_.has(sub, 'structure')) {
                //apparently needs to be reset as well
                if (!_.has(sub, '$$update')) {
                                        var nid=UUID.newID();
                    sub.structure.id = nid;
                }
                //sub.structure.id = UUID.newID();
                if (sub.substanceClass === 'polymer') {
                    //_.set(sub, 'polymer.idealizedStructure', sub.structure);
                    sub = _.omit(sub, 'structure');
                }
            }

            if (_.has(sub, 'polymer')) {
                polymerUtils.setSRUFromConnectivityDisplay(sub.polymer.structuralUnits);
            }


            if (_.has(sub, 'modifications')) {
               if (_.has(sub.modifications, 'structuralModifications')) {
                    _.forEach(sub.modifications.structuralModifications, function (mod) {
                        if (mod.$$residueModified) {
                            mod.residueModified = _.join(mod.$$residueModified, ';');
                        }
                    });
                }
            }

                if (_.has(sub, 'nucleicAcid')) {
                if (_.has(sub.nucleicAcid, 'sugars')) {
                    _.forEach(sub.nucleicAcid.sugars, function (sugar) {
                        if (sugar.sitesShorthand) {
                            _.unset(sugar, 'sitesShorthand');

                        }
                    });
                }
                if (_.has(sub.nucleicAcid, 'linkages')) {
                    _.forEach(sub.nucleicAcid.linkages, function (linkage) {
                        if (linkage.sitesShorthand) {
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
            if(disp === "") return undefined;
            return disp;
        };
        utils.sruDisplayToConnectivity = function (display) {
            if(!display){
                return {};
            }
            var errors =[];
            var connections = display.split(";");
            var regex = /^\s*[A-Z][A-Z]*[0-9]*_(R[0-9][0-9]*)[-][A-Z][A-Z]*[0-9]*_(R[0-9][0-9]*)\s*$/g;


            var map = {};

            for (var i = 0; i < connections.length; i++) {
                var con = connections[i].trim();
                if (con === "")continue;

                regex.lastIndex = 0;
                var res = regex.exec(con);
                if (res == null) {
                   var text =  "Connection '" + con + "' is not properly formatted";
                    errors.push({text: text, type: 'warning'});
                } else {
                    if (!map[res[1]]) {
                        map[res[1]] = [];
                    }
                    map[res[1]].push(res[2]);
                }
            }
            map.errors = errors;
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
        var url = baseurl + "api/v1/substances/search?q=";
        var substanceRet = {
            getSubstances: function (name) {
              //  var promise = $http.get(url, {params: {"filter": "names.name='" + name.toUpperCase() + "'"}, cache: true}, {
                var promise = $http.get(url + "root_names_name='" + name + "'", {cache: true}, {
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
        var onLoad = function (reader, deferred, scope) {
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
        var onProgress = function (reader, scope) {
            return function (event) {
                scope.$broadcast('fileProgress', {
                    total: event.total,
                    loaded: event.loaded
                });
            };
        };

        // Instantiate a new Filereader with the wrapped properties
        var getReader = function (deferred, scope) {
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
        var readAsText = function (file, scope) {
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

    ginasApp.controller("GinasController", function ($rootScope, $scope, $resource, $location, $compile, $uibModal, $http, $window, $anchorScroll, polymerUtils,
                                                     localStorageService, Substance, UUID, substanceSearch, substanceIDRetriever, CVFields, molChanger, toggler, resolver,
                                                     spinnerService) {
        $scope.substance = $window.loadjson;
        $scope.updateNav = false;
        $scope.validating = false;
        $scope.submitting = false;

        $window.SDFFields = {};

        $scope.getClass = function (path) {
            var t = $location.path().split('/');
            var r = (_.indexOf(t, path) >= 0) ? 'active' : '';
            return r;
        };

        $scope.printr = function(r){
            console.log(r);
        }
	
        $scope.submitq= function(qinput) {
            if ($scope.q.indexOf("\"") < 0 && $scope.q.indexOf("*") < 0 && $scope.q.indexOf(":") < 0 && $scope.q.indexOf(" AND ") < 0 && $scope.q.indexOf(" OR ") < 0) {
                $scope.q = "\"" + $scope.q + "\"";
            }
            return true;
        };

        if (typeof $window.loadjson !== "undefined" &&
            JSON.stringify($window.loadjson) !== "{}") {
            Substance.$$setSubstance($window.loadjson).then(function(data){
                _.set(data, '$$update', true);
                $scope.substance = data;
            });
        } else {
            var temp = localStorageService.get('tempsubstance');
            localStorageService.remove('tempsubstance');
            if (temp && $location.$$search.kind != undefined) {
                Substance.$$setSubstance(temp).then(function (data) {
                    $scope.substance = data;
                });
            } else {
                var substanceClass = $location.$$search.kind;
                $scope.substance = Substance.$$setClass(substanceClass);
            }
        }

        var windowElement = angular.element($window);
        var u = $location.path().split('/');
        var inter = _.intersection(u, ["edit","wizard"]);
        if(inter.length > 0){
            $scope.updateNav = true;
        }
            windowElement.on('beforeunload', function (event) {
                if($scope.updateNav == true) {
                    return "Navigating away from this page will lose all unsaved changed.";
                }
            });

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

        $scope.resolveName = function(name, div){
            resolver.resolve(name, 'structureSearchSpinner').then(function (response) {
                if (response.data.length > 0) {
                    $scope.structureSearchResolve = response.data;
                }
                $scope.name = null;
                var template = angular.element('<substance-viewer data=structureSearchResolve parent = substance></substance-viewer>');
                toggler.refresh($scope, div, template);
                spinnerService.hideAll();
                $timeout(function() {
                    $anchorScroll(div);
                }, 0, false);
            });
        };

        $scope.toggleGrid = function () {
            localStorageService.set('gridView', $scope.gridView);
        };

        $scope.toggleFacet = function (facet) {
            $scope[facet] = !$scope[facet];
        };

        $scope.getToggleStatus = function (facet) {
            if (_.isUndefined($scope[facet])) {
                $scope[facet] = false;
            }
            return $scope[facet];
        };

        $scope.redirectVersion = function () {
            var base = $window.location.pathname.split('/v/')[0];
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
            if (!lastEdit){
                return false;
                }
            if ($scope.substance.status === "approved") {
                return false;
            }
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

        $scope.$on('validate', function(event, obj, form, path){
            $scope.validate(obj, form, path);
        });

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

                obj = null;
                form.$setPristine();
                form.$setUntouched();
                $scope.$broadcast('show-errors-reset');
                return true;
            } else {
                return false;
            }
        };

        $scope.checkErrors = function () {
            if (_.has($scope.substanceForm, '$error')) {
                _.forEach($scope.substanceForm.$error, function (error) {
                    //console.log(error);
                });
            }
        };

        $scope.open = function (url) {
            $scope.modalInstance = $uibModal.open({
                templateUrl: url,
                scope: $scope,
                size: 'lg',
                backdrop: 'static'
            });
        };

        $scope.close = function () {
            $scope.modalInstance.close();
        };

        $scope.submitSubstanceConfirm = function () {
            $scope.validating =true;
           // var f = function () {
                var url = baseurl + "assets/templates/modals/substance-submission.html";
                $scope.open(url);
           // };
            $scope.validateSubstance();
        };

        $scope.dismissAll = function () {
            $scope.errorsArray = [];
            $scope.canSubmit = $scope.noErrors();
        };

        $scope.dismiss = function (err) {
            _.pull($scope.errorsArray, err);
            $scope.canSubmit = $scope.noErrors();
        };

        $scope.canSubmit = false;

        $scope.noErrors = function () {
            var errs = _.filter($scope.errorsArray, function (err) {
                if (err.messageType === "ERROR" || err.messageType === "WARNING") {
                    return err;
                }
            });
            return (errs.length <= 0);
        };

        $scope.parseErrorArray = function (errorArr) {
            _.forEach(errorArr, function (value) {
                if (value.messageType == "WARNING")
                    value.class = "warning";
                if (value.messageType == "ERROR")
                    value.class = "danger";
                if (value.messageType == "INFO")
                    value.class = "info";
                if (value.messageType == "SUCCESS")
                    value.class = "success";
            });
            errorArr = _.difference(errorArr, (_.filter(errorArr, ['class', 'info'])));
            return errorArr;
        };

        $scope.validateSubstance = function (callback) {

            //this should cascade to all forms to check and see if validation is ok
            $scope.$broadcast('show-errors-check-validity');
            //this is the api error checking
            $scope.checkErrors();


            var sub = angular.toJson($scope.substance.$$flattenSubstance());
            $scope.errorsArray = [];
            $http.post(baseurl + 'api/v1/substances/@validate', sub).then(
	    function success(response) {
		console.log("Response!");
                $scope.validating = false;
                $scope.errorsArray = $scope.parseErrorArray(response.data.validationMessages);
                $scope.canSubmit = $scope.noErrors();
                // if (callback) {
                //     callback();
                // }
            },
	    function failure(response) {
		var msg = {
			message:response.data,
			messageType:"ERROR",
			error:true
		};
                $scope.validating = false;
                $scope.errorsArray = [msg];
                $scope.canSubmit = $scope.noErrors();
            }
	    ).finally(function () {
                $scope.validating = false;
            });
        };

//this is already a function the substance object has, not really needed.
                $scope.getSubstanceClass = function() {
                        return $scope.substance.substanceClass;
                }

        $scope.submitSubstance = function () {
            var url;
            var sub = {};
            $scope.close();
            //this should cascade to all forms to check and see if validation is ok
            //  $scope.$broadcast('show-errors-check-validity');
            //this is the api error checking
            //  $scope.checkErrors();
            $scope.submitting = true;
            if (_.has($scope.substance, '$$update')) {
                    
                sub = angular.toJson($scope.substance.$$flattenSubstance());
                $http.put(baseurl + 'api/v1/substances', sub, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(function (response) {
                    $scope.updateNav = false;
                    url = baseurl + "assets/templates/modals/update-success.html";
                    $scope.postRedirect = response.data.uuid;
                    $scope.open(url);
                }, function (response) {
                    $scope.errorsArray = $scope.parseErrorArray(response.data.validationMessages);
                    url = baseurl + "assets/templates/modals/submission-failure.html";
                    $scope.submitting = false;
                    $scope.open(url);
                });
            } else {
                sub = angular.toJson($scope.substance.$$flattenSubstance());
                $http.post(baseurl + 'api/v1/substances', sub, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(function (response) {
                    $scope.updateNav = false;
                    $scope.postRedirect = response.data.uuid;
                    var url = baseurl + "assets/templates/modals/submission-success.html";
                    $scope.submitting = false;
                    $scope.open(url);
                }, function (response) {
                    $scope.errorsArray = $scope.parseErrorArray(response.data.validationMessages);
                    url = baseurl + "assets/templates/modals/submission-failure.html";
                    $scope.submitting = false;
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
            var sub = angular.toJson($scope.substance.$$flattenSubstance());
            var keyid = $scope.substance.uuid.substr(0, 8);
            location.href = baseurl + "substance/" + keyid + "/approve";
        };

        $scope.removeItem = function (list, item) {
            _.remove(list, function (someItem) {
                return item === someItem;
            });
        };

        $scope.applyPaster = function () {
            $scope.close();
            var originalSubstanceClass = $scope.substance.substanceClass;
            if (originalSubstanceClass === "chemical") {
                originalSubstanceClass = "structure";
            }
            delete $scope.substance[originalSubstanceClass];
            $scope.substance.$$changeClass($scope.substanceClass);

            if ($scope.substance.substanceClass === 'chemical') {
                molChanger.setMol($scope.substance.structure.molfile);
            }
            if ($scope.substance.substanceClass === 'polymer') {
                molChanger.setMol($scope.substance.polymer.idealizedStructure.molfile);
            }
        };

        $scope.viewSubstance = function () {
            $scope.updateNav = false;
            $window.location.search = null;
            $window.location.href = baseurl + 'substance/' + $scope.postRedirect.split('-')[0];
            //  $window.location.search =null;
        };

        $scope.addSameSubstanceType = function () {
            $scope.updateNav = false;
            $window.location.search = null;
            $window.location.href = baseurl + 'wizard?kind=' + $scope.substanceClass;
        };

        $scope.addDifferentSubstance = function () {
            $scope.updateNav = false;
            $window.location.search = null;
            $window.location.href = baseurl + 'register';
        };

        $scope.redirect = function () {
            localStorageService.set('tempsubstance', $scope.substance);
            $window.location.search = "kind=" + $scope.substance.substanceClass;

        };

        $scope.getRange = function (start, end) {
            return _.reverse(_.range(start, (end - 1 + 2)));
        };

        $scope.submitpaster = function (input) {
            $scope.substanceClass = $location.$$search.kind;
            Substance.$$setSubstance(JSON.parse(input)).then(function (data) {
                $scope.substance = data;
                if($scope.substance.substanceClass =="chemical"){
                    molChanger.setMol($scope.substance.structure.molfile);
                }
                if ($scope.substance.substanceClass != $scope.substanceClass) {

                    var url = baseurl + "assets/templates/modals/paste-redirect-modal.html";
                    $scope.open(url);
                }
            });

        };

        $scope.bugSubmit = function (bugForm) {
        };

        $scope.setEditId = function (editid) {
            localStorageService.set('editID', editid);
        };

        //method for injecting a large structure image on the browse page//
        $scope.showLarge = function (id, divid, ctx) {
            var template;
            if (!_.isUndefined(ctx)) {
                template = angular.element('<rendered size="500" id=' + id + ' ctx=' + ctx + '></rendered>');
            } else {
                template = angular.element('<rendered size="500" id=' + id + '></rendered>');
            }
                toggler.toggle($scope, divid, template);
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
                ctx: '@',
                smiles: '@'
            },
            link: function (scope, element, attrs) {
                
                scope.$watch('smiles', function (val) {
                                      if (val) {
                                          scope.relink();
                                 }
                          });
                          scope.$watch('id', function (val) {
                                      if (val) {
                                          scope.relink();
                                 }
                          });
                          scope.relink = function(){
                              var url = baseurl + 'img/' + scope.id + '.svg?size={{size||150}}';
                    if (!_.isUndefined(scope.ctx)) {
                        url += '&context={{ctx}}';
                    }
                    if (attrs.smiles) {
                        var smiles = attrs.smiles
                                                        .replace(/[;]/g,'%3B')
                                                        .replace(/[#]/g,'%23')
                                                        .replace(/[+]/g,'%2B')
                                                        .replace(/[|]/g,'%7C');
                        url = baseurl + "render?structure=" + smiles + "&size={{size||150}}&standardize=true";
                    }
                    var template = angular.element('<img width=height={{size||150}} height={{size||150}} ng-src="' + url + '" alt = "rendered image" class="tooltip-img" ng-cloak>');

                    element.html(template);
                    $compile(template)(scope);
                          };
                          scope.relink();
                                          
            }
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
                    scope.formatValue = function (v){
                            if (v) {
                            if(typeof v === "object"){
                                    if(v.display){
                                            return v.display;
                                    }else if(v.value){
                                            return v.value;
                                    }else{
                                            return null;
                                    }
                            }else{
                                    return v;
                            }
                    }
                    return null;
                    };
            
                scope.display = function () {
                    if (!_.isUndefined(scope.value) && !_.isNull(scope.value)) {
                        var ret = "";
                        var addedunits = false;
                        var unittext = scope.formatValue(scope.value.units);
                        if(!unittext){
                                unittext="";
                        }


                        if (scope.value) {
                                var atype=scope.formatValue(scope.value.type);
                            if (atype) {
                                ret += atype + "\n";
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
                return $http.get(url + uuid + ")/references", {cache: true}, {
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
                return $http.get(url + uuid + ")/references", {cache: true}, {
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
                    var templateString = angular.element('<div class ="row reftable"><div class ="col-md-8">' + _.join(links, ', ') + ' </div><div class="col-md-4"><span class="btn btn-primary pull-right" type="button" uib-tooltip="Show all references" ng-click="toggle()"><i class="fa fa-long-arrow-down"></i></span><div></div>');
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
                if (!_.isNull(scope.citation.url)&& !_.isUndefined(scope.citation.url)) {
                    template = angular.element('<a href = {{citation.url}} target = "_blank"><span>{{citation.citation}}</span></a>');
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
                        if (scope.field) {
                            if(_.isUndefined(scope.referenceobj[scope.field])){
                                _.set(scope.referenceobj, scope.field, {});
                            }
                            scope.referenceobj[scope.field].$$displayString = siteList.siteString(scope.referenceobj[scope.field]);
                        } else {
                            /*
                             alert('error');
                             */
                        }

                    }
                }
            },
            template: '<div><div><span>{{referenceobj[field].$$displayString || referenceobj.$$displayString}}</span><br></div><div ng-if="referenceobj[field].length"><span>({{referenceobj[field].length}} sites)</span></div></div>'
        };
    });

/*    ginasApp.directive('comment', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            template: '<div><span id="comment-text">{{value|limitTo:40}}...</span></div>'
        };
    });*/



    ginasApp.service('APIFetcher', function ($http) {
        var url = baseurl + "api/v1/substances(";
        var fetcher = {
            fetch: function (uuid) {
                return $http.get(url + uuid + ")",{cache: true},{
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
            template: '<div ng-repeat="p in parameters">{{p.name || p.parameterName || \'Please add a name\'}} <amount value="p.value"></amount></div>'

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
                    template = angular.element('<a ng-click="clicked()" class= "invalidAA" tooltip-class="invalidTool" uib-tooltip="INVALID">{{acid.value}}</a>');
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

                scope.clicked = function(){
                        scope.scrollTo(scope.acidClass,scope.acid);
                        scope.$emit("selected",scope.acid);
                };
            }
        };
    });

    ginasApp.directive('subunit', function (CVFields, APIFetcher, subunitParser) {

        return {
            restrict: 'E',
            scope: {
                parent: '=?',
                obj: '=?',
                uuid: '=',
                index: '@',
                view: '@',
                numbers: '=',
                selected: '='
            },
            link: function (scope, element, attrs) {
                if(attrs.subclass){
                    subunitParser.getResidues(attrs.subclass);
                }

                scope.numbers = true;
                scope.edit = true;

                if(scope.obj) {
                    scope.obj.subunitIndex = _.toInteger(scope.index);
                }

                scope.toggleEdit = function () {
                    scope.edit = !scope.edit;
                };

                scope.isSelected = function (site){                
                        if(!scope.selected)return;
                        
                        var isselected=false;
                        _.forEach(scope.selected, function(selSite){
                                if(selSite.subunitIndex === site.subunitIndex && selSite.residueIndex === site.residueIndex){
                                        isselected=true;
                                }
                        });
                        return isselected;
                };

                scope.parseSubunit = function(){
                    subunitParser.parseSubunit(scope.parent, scope.obj, scope.index);
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

                scope.cleanSequence = function () {
                    scope.obj.sequence = subunitParser.cleanSequence(scope.obj.sequence);
                    scope.parseSubunit();
                };

                var display = [];
                if (_.isUndefined(scope.parent)) {
                    APIFetcher.fetch(scope.uuid).then(function (data) {
                        scope.parent = data;
                        if (_.has(data, 'protein')) {
                            scope.obj = data.protein.subunits[scope.index];
                            scope.index = scope.index-0+1;
                        } else {
                            scope.obj = data.nucleicAcid.subunits[scope.index];
                            scope.index = scope.index-0+1;
                        }
                        scope.parseSubunit();

                    });
                } else {
                    scope.parseSubunit();
                }

            },
            templateUrl: baseurl + "assets/templates/elements/subunit.html"
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

        this.getMol = function () {
            return sketcher.getMolfile();
        };

        this.getSmiles = function () {
            return sketcher.getSmiles();
        };
    });

    ginasApp.directive('sketcher', function ($compile, $http, $timeout, UUID, polymerUtils, CVFields, localStorageService, molChanger, Substance) {
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

                                scope.merge = function(oldStructure, newStructure){
                                        var definitionalChange=(oldStructure["hash"] !== newStructure["hash"]);
                                        _.forIn(newStructure, function(value, key){
                                                var cvname=null;
                                                switch(key){
                                                        case "stereochemistry":
                                                                cvname="STEREOCHEMISTRY_TYPE";
                                                                break;
                                                        case "opticalActivity":
                                                                cvname="OPTICAL_ACTIVITY";
                                                                break;
                                                        default:
                                                                oldStructure[key]=value;
                                                }
                                                if(cvname!==null){
                                                        CVFields.search(cvname, value).then(function(response){
                                                                        oldStructure[key]=response[0];
                                                                });
                                                }

                                        });
                                        return definitionalChange;
                                };

                scope.updateMol = function () {
                        var url = baseurl + 'structure';
                        $http.post(url, scope.mol, {
                            headers: {
                                'Content-Type': 'text/plain'
                            }
                        }).success(function (data) {
                            if (scope.parent.substanceClass === "polymer") {
                                scope.parent.polymer.idealizedStructure = data.structure;
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
                            if (scope.parent.structure) {
                                data.structure.id = scope.parent.structure.id;
                            } else {
                                scope.parent.structure = {};
                            }
                            var defChange = scope.merge(scope.parent.structure, data.structure);

                            if (defChange) {  
                                scope.parent.moieties = [];
                                _.forEach(data.moieties, function (m) {
                                    m["$$new"] = true;
                                    //this is used to make a cv element out of the moiety units, which are re-written as strings with each round trip
                                    if(!_.isObject(m.countAmount.type)){
                                        var temp = {value: m.countAmount.type, display: m.countAmount.type};
                                        m.countAmount.type = temp;
                                    }
                                    if(!_.isObject(m.countAmount.units)){
                                        var temp = {value: m.countAmount.units, display: m.countAmount.units};
                                        m.countAmount.units = temp;
                                    }
                                    var moi = {};
                                    scope.merge(moi, m);
                                    scope.parent.moieties.push(moi);
                                });
                            }


                            if (data.structure) {
                                _.set(scope.parent, 'q', data.structure.smiles);
                            }
                        });
                };

                scope.sketcher = new JSDraw("sketcherForm");
                scope.sketcher.options.data = scope.mol;
                scope.sketcher.setMolfile(scope.mol);
                scope.sketcher.options.ondatachange = function () {
                    scope.mol = scope.sketcher.getMolfile();
                    if(attrs.ajax =='false') {
                        $timeout(function() {
                            _.set(scope.parent, 'q', scope.sketcher.getSmiles());
                        }, 0);
                    }else{
                        scope.updateMol();
                    }
                };
                molChanger.setSketcher(scope.sketcher);
                var structureid = (localStorageService.get('structureid') || false);
                if (localStorageService.get('editID')) {
                    structureid = false;
                }

                if (scope.parent.substanceClass === 'polymer' && (scope.parent.polymer.displayStructure)) {
                    scope.sketcher.setMolfile(scope.parent.polymer.displayStructure.molfile);
                }else {
                        if(!_.isUndefined(scope.parent.polymer)){
                            if(!_.isUndefined(scope.parent.polymer.idealizedStructure)) {
                                       scope.mol = scope.parent.polymer.idealizedStructure.molfile;
                                if (!_.isNull(scope.mol)) {
                                    scope.updateMol();
                                }
                            }
                    }
                }

                if (structureid) {
                    var url = baseurl + 'api/v1/structures/' + structureid;
                    $http.get( url, {cache: true}).then(function (response) {
                        scope.sketcher.setMolfile(response.data.molfile);
                        _.set(scope.parent, 'q', response.data.smiles);
                        localStorageService.remove('structureid');
                    });
                }

            }
        };
    });

    ginasApp.directive('modalButton', function ($compile, $templateRequest, $http, $uibModal, molChanger, FileReader) {
        return {
            scope: {
                type: '=',
                structureid: '=',
                format: '@',
                referenceobj: '=',
                parent: '='
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
                        template = angular.element(' <a aria-label="Export" uib-tooltip ="Export" ng-click = "getExport()"><span class="sr-only">Export Data</span><i class="fa fa-external-link fa-2x success"></i></a>');
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
                    FileReader.readAsText(file, scope).then(function (response) {
                        scope.molfile = response;
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
                        switch (scope.format) {
                                case "fas":
                                        scope.formatName="FASTA";
                                        break;
                                case "mol":
                                        scope.formatName="Molfile";
                                        break;        
                                case "sdf":
                                        scope.formatName="SD File";
                                        break;        
                                default:
                                        scope.formatName="Export";
                                        break;                        
                        }
                    if(_.isUndefined(scope.structureid)){
                        var url = baseurl + 'structure';
                        var mol = molChanger.getMol();
                        $http.post(url, mol, {
                            headers: {
                                'Content-Type': 'text/plain'
                            }
                        }).then(function (response) {
                            scope.exportData = response.data.structure.molfile;
                            scope.exportSmiles = response.data.structure.smiles;
                            scope.open();
                        });
                    }else {
                        var url = baseurl + 'export/' + scope.structureid + '.' + scope.format;
                        $http.get(url, {
                            headers: {
                                'Content-Type': 'text/plain'
                            }
                        }).success(function (response) {
                            scope.exportData = response;
                            if(scope.format != 'fas') {
                                url = baseurl + 'export/' + scope.structureid + '.smiles';
                                $http.get(url, {
                                    headers: {
                                        'Content-Type': 'text/plain'
                                    }
                                }).success(function (response) {
                                    scope.exportSmiles = response;
                                });
                            }
//                           var warnHead = response.headers("EXPORT-WARNINGS").split("___")[0];
                            //                          scope.warnings = JSON.parse(warnHead);
                            scope.open();
                        });
                    }
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
                var template = '<div>';
                _.forEach(scope.text.split('|'), function (c) {
                    scope.link.push(c);
                    scope.codes.push(c.split('['));
                });
                _.forEach(scope.codes, function (c, key) {
                    var link = "app/substances?q=comments:%22" + _.join(_.slice(scope.link, 0, key + 1), '|') + "%22";
                    template += '<ul class="tree-list"><li><a href = "' + link + '" uib-tooltip="Search ginas for ' + c[0] + '" target ="_self">' + c[0] + '</a>';
                    if (c[1]) {
                        template += '<span>' + '[' + c[1] + '</span>';
                    }
                });
                for (var i = 0; i < scope.codes.length; i++) {
                    template += '</li></ul>';
                }
                template += '</div>';
                element.append(angular.element(template));
                $compile(template)(scope);
            }
        }
    });

})();
