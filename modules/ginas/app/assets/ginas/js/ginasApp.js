(function () {
    'use strict';
    var ginasApp = angular.module('ginas', ['ngAria', 'ngMessages', 'ngResource', 'ui.bootstrap', 'ui.bootstrap.showErrors',
            'LocalStorageModule', 'ngTagsInput', 'jsonFormatter', 'ginasForms', 'ginasFormElements', 'ginasAdmin', 'ginasDownloads', 'ginasScheduled', 'diff-match-patch',
            'angularSpinners', 'filterListener', 'validatorListener', 'ginasFilter'
        ]).run(function ($rootScope, $anchorScroll) {
            $anchorScroll.yOffset = 150; // always scroll by 100 extra pixels
            $rootScope.isGlobalLoading = false;
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
            substance.substanceClass = substanceClass;
            switch (substanceClass) {
                case "chemical":
                    if (!substance.structure) {
                        substance.structure = {};
                        _.set(substance.structure, 'opticalActivity', {
                            value: "UNSPECIFIED",
                            display: "UNSPECIFIED"
                        });
                        substance.moieties = [];
                    }
                    break;
                case "protein":
                    if (!substance.protein) {
                        substance.protein = {};
                        substance.protein.subunits = [];
                        substance.protein.glycosylation = {
                            'CGlycosylationSites': [],
                            'NGlycosylationSites': [],
                            'OGlycosylationSites': []
                        };
                    }
                    break;
                case "structurallyDiverse":
                    if (!substance.structurallyDiverse) {
                        substance.structurallyDiverse = {};
                    }
                    break;
                case "nucleicAcid":
                    if (!substance.nucleicAcid) {
                        substance.nucleicAcid = {};
                        substance.nucleicAcid.subunits = [];
                    }
                    break;
                case "mixture":
                    if (!substance.mixture) {
                        substance.mixture = {};
                    }
                    break;
                case "polymer":
                    if (!substance.polymer) {
                        substance.polymer = {};
                    }
                    break;
                case "specifiedSubstanceG1":
                    if (!substance.specifiedSubstance) {
                        substance.specifiedSubstance = {};
                    }
                    break;
                default:
                    break;
            }
            if (!substance.references) {
                substance.references = [];
            }
            if (!substance.access) {
                substance.access = [{
                    value: 'protected',
                    display: 'PROTECTED'
                }];
            }
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
            if (sub.protein) {
                if (sub.protein.proteinSubType) {
                    sub.protein.proteinSubTypes = sub.protein.proteinSubType.split("|");
                }
            }
            if(sub.polymer){
                if(sub.polymer.structuralUnits){
                    polymerUtils.setSRUConnectivityDisplay(sub.polymer.structuralUnits);
                }
            }
            substance.$$setClass(substance.$$getClass());
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
                            link.sites[sitekey] = _.pick(site, ['subunitIndex', 'residueIndex']);
                        });
                    });
                }
                if (_.has(sub.protein, 'otherLinks')) {
                    _.forEach(sub.protein.otherLinks, function (value, key) {
                        var otherLink = {};
                        var sites = _.toArray(value.sites);
                        // TODO: Previously we would throw away odd-number
                        // sites, anticipating that other links typically connected
                        // sets of 2 residues. This was not a good idea as some
                        // links are between odd numbers of sites. However, some
                        // form of warning should probably be present which makes the
                        // meaning of the sets of otherLinks more clear.
                        //if (sites.length % 2 != 0) {
                        //    sites = _.dropRight(sites);
                        //}
                        sub.protein.otherLinks[key].sites = sites;
                    });
                }

            }
            sub = flattenCV(sub);

            var st;
            if (sub.protein) {
                st = sub.protein.proteinSubTypes;
            }
            if (st) {
                sub.protein.proteinSubType = st.join("|");
            }
            if (_.has(sub, 'moieties')) {
                _.forEach(sub.moieties, function (m) {
                    if (!_.has(sub, '$$update') || m["$$new"]) {
                        m.id = UUID.newID();
                    }
                });
            }
            if (_.has(sub, 'structure')) {
                //apparently needs to be reset as well
                if (!_.has(sub, '$$update')) {
                    var nid = UUID.newID();
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
                _.forEach(sub.polymer.structuralUnits, function (sru) {
                    if (_.has(sru, "attachmentMap")) {
                        delete sru.attachmentMap["$errors"];
                    }
                });
            }
            if (_.has(sub, 'modifications')) {
                if (_.has(sub.modifications, 'structuralModifications')) {
                    _.forEach(sub.modifications.structuralModifications, function (mod) {
                        if (mod.$$residueModified) {
                            mod.residueModified = _.join(mod.$$residueModified, ';');
                        }
                        //Updates structural modifications to blank out sites if
                        //the location type is residue-specific
                        if(mod.locationType === "RESIDUE_SPECIFIC" || (mod.locationType && mod.locationType.value === "RESIDUE_SPECIFIC")){
                            mod.sites=[];
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
            if (disp === "") return undefined;
            return disp;
        };
        utils.sruDisplayToConnectivity = function (display) {
            if (!display) {
                return {};
            }
            var errors = [];
            var connections = display.split(";");
            var regex = /^\s*[A-Za-z][A-Za-z]*[0-9]*_(R[0-9][0-9]*)[-][A-Za-z][A-Za-z]*[0-9]*_(R[0-9][0-9]*)\s*$/g;
            var map = {};
            for (var i = 0; i < connections.length; i++) {
                var con = connections[i].trim();
                if (con === "") continue;
                regex.lastIndex = 0;
                var res = regex.exec(con);
                if (res == null) {
                    var text = "Connection '" + con + "' is not properly formatted";
                    errors.push({ text: text, type: 'warning' });
                } else {
                    if (!map[res[1]]) {
                        map[res[1]] = [];
                    }
                    map[res[1]].push(res[2]);
                }
            }
            if (errors.length > 0) {
                map.$errors = errors;
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
        var url = baseurl + "api/v1/substances/search";
        var nameFinder = {
            search: function (query) {
                var promise = $http.get(url, {
                    params: { "q": "root_names_name:" + query + "*" },
                }, {
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
                var promise = $http.get(url + editId + ")?view=full", { cache: true }, {
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
    ginasApp.service('typeaheadService', function ($http) {
        var url = baseurl + "api/v1/suggest";
        var suggest = {
            search: function (query, typePriority, ukeys) {
                var promise = $http.get(url, {
                    params: { "q": query }
                }, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                    ///TODO sort by weight///
                    //TODO search multiple field types//
                }).then(function (response) {
                    if (!ukeys) ukeys = [];
                    if (!typePriority) typePriority = function (t) { return 0; };
                    ukeys.length = 0;
                    var pairs = _.chain(response.data)
                        .map(function (v, k) {
                            return { "key": k, "values": v, "i": typePriority(k) };
                        })
                        .sortBy("i")
                        .filter(function (kv) {
                            return kv.i >= 0;
                        })
                        .flatMap(function (kvp) {
                            ukeys.push(kvp.key);
                            return _.map(kvp.values, function (v) {
                                //need to get out the most important part
                                //always get the part in first <b> and 
                                //extend
                                var lim = 30;
                                var start = 0;
                                var bef = "";
                                var sindex = v.highlight.indexOf("<b>");
                                var eindex = v.highlight.indexOf("</b>") - 3;
                                if (eindex > lim) {
                                    start = eindex - lim;
                                }
                                if (start > 0) {
                                    bef = "...";
                                } else {
                                    start = 0;
                                }
                                return { "k": kvp.key, "v": v.key, "d": bef + v.key.substring(start) };
                            });
                        })
                        .value();
                    return pairs;
                });
                return promise;
            }
        };
        return suggest;
    });
    ginasApp.service('substanceSearch', function ($http, $q) {
        var options = {};
        var url = baseurl + "api/v1/substances/search";
        this.load = function (field) {
            $http.get(url, {
                params: { "q": field.toUpperCase() }
            }, { cache: true }, {
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
    ginasApp.controller("TypeAheadController", function ($rootScope, $scope, $location, typeaheadService) {
        $scope.types = [];
        $scope.showTypes = ["Approval_ID", "Display_Name", "CAS", "Name"];
        $scope.qmod = "query";
        if ($scope.searchVariables) {
            $scope.searchVariables.query = '';
        }

        if ($location.search()['q']
            && !$location.search()['type']
            && $location.search()['cutoff'] !== null
            && $location.path().indexOf('structure') === -1) {
            $scope.searchVariables[$scope.qmod] = $location.search()['q'];
        }
        $scope.init = function (qmod) {
            $scope.qmod = qmod;
        }
        $scope.nameFor = function (suggest) {
            if (suggest === "Approval_ID") return "Approval ID";
            if (suggest === "Display_Name") return "Preferred Term";
            return suggest;
        };
        $scope.onSelect = function ($item, $model, $label) {
            if ($scope.searchVariables) {
                $scope.searchVariables.isInteractedWith = false;
                $scope.searchVariables[$scope.qmod] = '"' + $item.v + '"';
            }
        };
        $scope.getSuggestions = function (query) {
            var ret = typeaheadService.search(query, function (t) {
                if (t === "Approval_ID" && query.length < 3) {
                    return -1;
                }
                return $scope.showTypes.indexOf(t);
            }, $scope.types);
            return ret;
        };
    });
    ginasApp.controller("GinasController", function ($rootScope, $scope, $document, $location, $compile, $uibModal, $http, $window, $anchorScroll, $timeout, polymerUtils,
        localStorageService, Substance, UUID, substanceSearch, substanceIDRetriever, CVFields, molChanger, toggler, resolver,
        substanceFactory,
        spinnerService, typeaheadService, subunitParser) {
        $scope.substance = $window.loadjson;
        $scope.updateNav = false;
        $scope.validating = false;
        $scope.submitting = false;
        $scope.searchLimit = "global";
        $scope.loadingSuggest = false;
        $scope.noResults = false;
        $scope.show = false;
        $scope.sequence = "";
        $scope.searchVariables = {
            isInteractedWith: false
        };
        var currentKeyPressFunction;
        $scope.cleanSequence = function (seqType) {
            $scope.sequence = subunitParser.cleanSequence($scope.sequence, _.lowerCase(seqType));
        }
        $scope.preload = function (seqType) {
            if ("protein" === seqType.toLowerCase()) {
                $scope.seqType = "Protein";
                subunitParser.getResidues("protein");
            } else if ("nucleicacid" === seqType.toLowerCase()) {
                $scope.seqType = "NucleicAcid";
                subunitParser.getResidues("nucleicAcid");
            } else {
                $scope.seqType = _.capitalize(seqType);
                subunitParser.getResidues("protein");
            }
        }
        $window.SDFFields = {};
        $scope.getClass = function (path) {
            var t = $location.path().split('/');
            var r = (_.indexOf(t, path) >= 0) ? 'active' : '';
            return r;
        };
        $scope.getSuggestions = function (query) {
            var ret = typeaheadService.search(query);
            return ret;
        };
        var searchEventListener = function(event) {
            $scope.searchVariables.isInteractedWith = true;
                }

        $scope.listenForInteraction = function (event) {
            event.target.addEventListener('keypress', searchEventListener);
            event.target.addEventListener('paste', searchEventListener);
            }
        $scope.removeInteractionListener = function (event) {
            event.target.removeEventListener('click', searchEventListener);
            event.target.removeEventListener('paste', searchEventListener);
        }
        $scope.submitq = function (query, action, isFromQueryBuilder) {
            // if (query.indexOf("\"") < 0 && query.indexOf("*") < 0 && query.indexOf(":") < 0 && query.indexOf(" AND ") < 0 && query.indexOf(" OR ") < 0) {
            //     $scope.q = "\"" + query + "\"";
            // } else {
            //     $scope.q = query;
            // }
            if (!isFromQueryBuilder && $scope.searchVariables.isInteractedWith && query.indexOf('"') < 0 && query.indexOf("*") < 0 && query.indexOf(":") < 0 && query.indexOf(" AND ") < 0 && query.indexOf(" OR ") < 0) {
                $scope.q = "\"" + query + "\"";
            } else {
                $scope.q = query;
            }
            switch ($scope.searchLimit) {
                case "global":
                    break;
                case "names":
                    $scope.q = 'root_names_name:' + $scope.q;
                    break;
                case "codes":
                    $scope.q = 'root_codes_code:' + $scope.q;
                    break;
            }
            //First, we get the absolute url where we currently are            
            var whereiam = window.location.href;
            //Then we get the base path of the app, which will have
            //a terminal "/", but we want to remove that slash, which
            //we do with the following regex.
            var base = baseurl.replace(/.$/g, "");
            if (base && base.length > 0) {
                //We only want the part of the URL _before_ the base path.
                //The reason is that angular is trying to be smart
                //and preserve all paths to be from the base path, so you can't give
                //it a full path, because it will append the base path.
                whereiam = whereiam.split(base)[0];
            } else {
                whereiam = whereiam.replace(/(^[^?]*).*$/g, "$1");
                if (whereiam[whereiam.length - 1] == '/') {
                    whereiam = whereiam.substring(0, whereiam.length - 1);
                }
            }

            //The action already has the base path built in, so this 
            //is the "new" absolute base path + the action we want
            var nav = whereiam + action;

            var rq = $scope.q;

            $location.search({});
            $location.search("q", rq);
            $location.hash("");
            //This just gets angular's encoding of the query portion of the URL,
            //which will be explicitly added.
            var qpart = _.chain(($location.absUrl().split("?")))
                .filter(function (a, b) { return b; }) //get rid of first element
                .value()
                .join("?");

            window.location = nav + "?" + qpart.replace(";", "%3B");
        };
        if (typeof $window.loadjson !== "undefined" &&
            JSON.stringify($window.loadjson) !== "{}") {
            $scope.substance = Substance;
            Substance.$$setSubstance($window.loadjson).then(function (data) {
                if (data.names) {
                    data.names.sort(function (a, b) {
                        if (a.displayName && !b.displayName) return -1;
                        if (!a.displayName && b.displayName) return 1;
                        //if(a.preferred && !b.preferred)return 1;
                        //if(!a.preferred && b.preferred)return -1;
                        return a.name.localeCompare(b.name);
                    });
                }
                _.set(data, '$$update', true);
                data = data.$$setClass(data.$$getClass());
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
        var inter = _.intersection(u, ["edit", "wizard"]);
        if (inter.length > 0) {
            $scope.updateNav = true;
        }
        windowElement.on('beforeunload', function (event) {
            if ($scope.updateNav == true) {
                return "Navigating away from this page will lose all unsaved changed.";
            }
        });
        $scope.type = $location.search().type;
        if (!$scope.type) {
            $scope.type = "Substructure";
        }
        $scope.cutoff = $location.search().cutoff - 0;
        if (!Number.isFinite($scope.cutoff)) {
            $scope.cutoff = 0.8;
        }
        $scope.stage = true;
        $scope.gridView = localStorageService.get('gridView') || false;
        $scope.diff = false;
        $scope.scrollTo = function (prmElementToScrollTo) {
            $location.hash(prmElementToScrollTo);
            $anchorScroll();
        };
        $scope.viewToggle = function () {
            $scope.show = !$scope.show;
            if ($scope.show) {
                $scope.submitSubstanceView = angular.fromJson(angular.toJson($scope.substance.$$flattenSubstance()));
                if ($location.hash() !== 'json-area') {
                    $location.hash('json-area');
                } else {
                    $anchorScroll();
                }
            }
        };
        $scope.resolveName = function (name, div) {
            $scope.structureSearchResolve = [];
            resolver.resolve(name, 'structureSearchSpinner').then(function (response) {
                if (response.data.length > 0) {
                    $scope.structureSearchResolve = _.union($scope.structureSearchResolve, response.data);
                }
                $scope.name = null;
                var template = angular.element('<substance-viewer data=structureSearchResolve parent = substance></substance-viewer>');
                toggler.refresh($scope, div, template);
                spinnerService.hideAll();
                $timeout(function () {
                    $anchorScroll(div);
                }, 0, false);
            });
            substanceFactory.getSubstances(name).then(function (response) {
                var duplicate = [];
                if (response.data.count > 0) {
                    _.forEach(response.data.content, function (sub) {
                        _.set(sub, 'refType', 'duplicate');
                        $scope.structureSearchResolve.push(sub);
                    });
                }
                return duplicate;
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
        $scope.redirectVersion = function (v) {
            if (!v) {
                v = $scope.versionNumber;
            }
            var base = $window.location.pathname.split('/v/')[0];
            var newLocation = "/v/" + v;
            $window.location.pathname = base + newLocation;
        };
        //We can put this here, but it makes it difficult to expand in the future.
        //The server knows how things can be sorted, we need to either ajax
        //(which can cause latency problems), or we can have it pre-stored
        //server-side, and injected.
        $scope.sortValues = [
            {
                "value": "default",
                "display": "Relevance"
            },
            {
                "value": "^Display Name",
                "display": "Display Name, A-Z"
            },
            {
                "value": "$Display Name",
                "display": "Display Name, Z-A"
            },
            {
                "value": "^Reference Count",
                "display": "Least References"
            },
            {
                "value": "$Reference Count",
                "display": "Most References"
            },
            {
                "value": "^root_lastEdited",
                "display": "Oldest Change"
            },
            {
                "value": "$root_lastEdited",
                "display": "Newest Change"
            },
            {
                "value": "$root_structure_mwt",
                "display": "Highest Molecular Weight"
            },
            {
                "value": "^root_structure_mwt",
                "display": "Lowest Molecular Weight"
            }
        ];
        var suppliedOrder = _.find($scope.sortValues, {
            value: $location.search()["order"]
        });
        $scope.selectedSort = suppliedOrder || { value: "Sort By" };
        $scope.showDeprecated = $location.search()["showDeprecated"] || "false";
        $scope.showDeprecatedChange = function (model) {
            $location.search("showDeprecated", $scope.showDeprecated);
            window.location = $location.absUrl();
        };
        $scope.sortSubstances = function (model) {
            $location.search("order", $scope.selectedSort.value);
            window.location = $location.absUrl();
        };
        $scope.showPriv = function () {
            $scope.showprivates = !$scope.showprivates;
        };
        // $scope.showprivates = false;
        //Prepare an export file for download
        $scope.downloadFile = function (url) {
            if ($scope.showprivates) {
                url = url + '&publicOnly=' + (!$scope.showprivates ? 1 : 0);
            }
            $http.get(url)
                .then(function (response) {
                    var dl = response.data;
                    if (dl) {
                        if (dl.isReady) {
                            var d = new Date();
                            var datestr = d.toISOString().split("T")[0] + "_" + d.toTimeString().split(" ")[0].split(":").join("_");
                            var proposedfname = "export-" + datestr + "." + dl.url.split("format=")[1].split("&")[0];
                            $scope.exportData = {};
                            if (dl.isCached) {
                                $scope.exportData.cached = dl.cached;
                                $scope.baseurl = baseurl;
                            }
                            $scope.fileNamePrompt(proposedfname, function (fname) {
                                var nurl = dl.url + "&genUrl=" + encodeURIComponent(window.location.href) + "&filename=" + encodeURIComponent(fname);
                                //alert(nurl);
                                $http.get(nurl).then(function (rep) {
                                    var meta = rep.data;
                                    window.location.href = baseurl + "myDownloads/" + meta.id;
                                }, function (rep) {
                                    $scope.exportUnavailableWarning();
                                });
                            })
                        } else if (dl.isPresent) { //busy
                            $scope.exportUnavailableWarning();
                        } else { //unknown result set
                            $scope.exportUnavailableWarning();
                        }
                    } else {
                        $scope.exportUnavailableWarning();
                    }
                });
        };
        $scope.exportUnavailableWarning = function () {
            $scope.modalInstance = $uibModal.open({
                templateUrl: baseurl + "assets/templates/modals/export-warning.html",
                scope: $scope
            });
        };
        $scope.fileNamePrompt = function (fname, cb) {
            $scope.exportFname = fname;
            $scope.modalInstance = $uibModal.open({
                templateUrl: baseurl + "assets/templates/modals/filename-prompt.html",
                scope: $scope
            });
            $scope.mclose = function (fname2) {
                $scope.close();
                cb(fname2);
            };
        };
        $scope.compare = function () {
            //$scope.left = angular.toJson(Substance.$$flattenSubstance(angular.copy($scope.substance)));
            $scope.left = angular.toJson($scope.substance.$$flattenSubstance());
            $scope.right = angular.toJson(angular.copy($window.loadjson));
            $scope.substancesEqual = angular.equals($scope.right, $scope.left);
        };
        $scope.canApprove = function () {
            var lastEdit = $scope.substance.lastEditedBy;
            if (!lastEdit) {
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
        $scope.$on('validate', function (event, obj, form, path) {
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
        $scope.openImgModal = function (uuid, imgUrl) {
            var approveURL = baseurl + 'api/v1/substances(' + uuid + ')/names';
            $scope.image = imgUrl;
            $scope.setSysNames(uuid)
            $scope.getSmilesInchi(uuid);
            $scope.modalInstance = $uibModal.open({
                templateUrl: baseurl + "assets/templates/modals/image-modal.html",
                scope: $scope,
                windowClass: 'image-window',
                size: 'image'
            });
        };
        $scope.setSysNames = function (uuid) {
            var approveURL = baseurl + 'api/v1/substances(' + uuid + ')/names';
            $http.get(approveURL, {cache: false}, {
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(function (response) {
                var namelist = [];
                for (var i = 0; i < response.data.length; i++) {
                    if (response.data[i].type == 'sys') {
                        namelist.push(response.data[i].name);
                    }
                }
                $scope.sysNames = namelist;
            });
        };
        $scope.getSmilesInchi = function (uuid) {
            var url = baseurl + 'export/' + uuid + '.smiles';

            $http({
                url: url,
                method: 'GET',
                transformResponse:[]
            }, {
                headers: {
                    'Content-Type': 'text/plain'
                },
                responseType: 'text'
            }).success(function (response) {
                $scope.smiles = response;
            });
            url = baseurl + 'api/v1/substances(' + uuid + ')structure!$inchikey()';
            $http.get(url, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (response) {
                $scope.inchikey = response;
            });
        };
        $scope.close = function () {
            $scope.modalInstance.close();
        };
        $scope.submitSubstanceConfirm = function () {
            $scope.isLoading = true;
            $scope.validating = true;
            // var f = function () {
            var url = baseurl + "assets/templates/modals/substance-submission.html";
            $scope.open(url);
            // };
            var noGlogalLoading = true;
            $scope.validateSubstance(noGlogalLoading);
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

        //remove second success message recieved from API call
        $scope.RemoveDuplicateSuccess = function(){
            var toRemove = $scope.errorsArray.findIndex(function (e) {return ((e.message === "Substance is valid")&&(e.actionType === "IGNORE"))});
            if((toRemove >= 0) && $scope.errorsArray.some(function (e) {return ((e.message === "Substance is valid")&&(e.actionType === "DO_NOTHING"))})){
                $scope.errorsArray.splice(toRemove,1);
            }
        }
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
        $scope.validateSubstance = function (noGlobalLoading) {
            $rootScope.isGlobalLoading = !noGlobalLoading;
            //this should cascade to all forms to check and see if validation is ok
            $scope.$broadcast('show-errors-check-validity');
            //this is the api error checking
            $scope.checkErrors();
            //TODO: Remove later. This just adds uuids to names which don't have UUIDs.
            //This should not be necessary, but appears to be.
            //**************************
            _.chain(angular.element(document.body).scope().substance.names)
                .filter(function (n) { return !n.uuid; })
                .forEach(function (n) { n.uuid = angular.element(document.body).injector().get("UUID").newID(); })
                .value();
            //**************************
            $scope.errorsArray = [];
            $scope.sequenceArray = [];
            var sub = $scope.substance.$$flattenSubstance();

            //add an element to the error array if a protein or nucleic acid sequence is still being edited.
           if(((sub.protein) || (sub.nucleicAcid)) && ((sub.substanceClass == "protein") || (sub.substanceClass == "nucleicAcid"))){
               var subunitLoc = sub.substanceClass;
               if((sub[subunitLoc].subunits) && (sub[subunitLoc].subunits.length > 0 )){
                   _.forEach(sub[subunitLoc].subunits, function(val,key){
                      // Add the missing sequence property for unsaved subunits so API call can check additional errors properly
                       if(!val.sequence){
                           sub[subunitLoc].subunits[key].sequence = "";
                       }
                       if(val.editing){
                          if(val.editing == 'open'){
                             var error = {actionType:"FAIL", appliedChange:false, class:"danger", links:[], message:"Unsaved changes to subunit " + val.subunitIndex + "", messageType:"ERROR", suggestedChange:false, _discriminator:"GinasProcessingMessage"};
                             $scope.sequenceArray.push(error);
                           }
                            delete sub[subunitLoc].subunits[key].editing;
                        }
                   })

               }
           }
           sub = angular.toJson(sub);
            $http.post(baseurl + 'api/v1/substances/@validate', sub).then(
                function success(response) {
                    $scope.validating = false;
                    $scope.errorsArray = $scope.errorsArray.concat($scope.sequenceArray, $scope.parseErrorArray(response.data.validationMessages));
                    $scope.RemoveDuplicateSuccess();
                    $scope.canSubmit = $scope.noErrors();
                    // if (callback) {
                    //     callback();
                    // }
                    if ($scope.errorsArray && $scope.errorsArray.length) {
                        if ($location.hash() !== 'errors-area') {
                            $location.hash('errors-area');
                        } else {
                            $anchorScroll();
                        }
                    }
                },
                function failure(response) {
                    var msg = {
                        message: response.data,
                        messageType: "ERROR",
                        error: true
                    };
                    $scope.validating = false;
                    $scope.errorsArray = [msg];
                    $scope.canSubmit = $scope.noErrors();
                }
            ).finally(function () {
                $rootScope.isGlobalLoading = false;
                $scope.isLoading = false;
                $scope.validating = false;
            });
        };
        //this is already a function the substance object has, not really needed.
        $scope.getSubstanceClass = function () {
            return $scope.substance.substanceClass;
        }
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
            var url;
            $scope.close();
            $scope.updateNav = false;
            var sub = angular.toJson($scope.substance.$$flattenSubstance());
            var keyid = $scope.substance.uuid;
            var approveURL = baseurl + "api/v1/substances(" + keyid + ")/@approve";
            $scope.submitting = true;
            $http.get(approveURL, { cache: false }, {
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(function (response) {
                $scope.updateNav = false;
                url = baseurl + "assets/templates/modals/update-success.html";
                $scope.postRedirect = response.data.uuid;
                $scope.open(url);
                $scope.submitpaster(response.data);
            }, function (response) {
                var messages = [];
                var msg = {
                    message: "Unknown error",
                    messageType: "ERROR",
                    error: true
                };
                if (response.data && typeof response.data === "object") {
                    if (response.data.validationMessages) {
                        messages = response.data.validationMessages;
                    } else {
                        if (response.data.message) {
                            msg.message = response.data.message;
                        }
                        messages.push(msg);
                    }
                } else {
                    if (response.data) {
                        msg.message = response.data;
                    }
                    messages.push(msg);
                }
                $scope.errorsArray = $scope.parseErrorArray(messages);
                url = baseurl + "assets/templates/modals/submission-failure.html";
                $scope.submitting = false;
                $scope.open(url);
            });
        };
        $scope.submitSubstance = function () {
            var url;
            var sub = {};
            $scope.close();
            //this should cascade to all forms to check and see if validation is ok
            //  $scope.$broadcast('show-errors-check-validity');
            //this is the api error checking
            //  $scope.checkErrors();
            $scope.submitting = true;
            var url1 = baseurl + "assets/templates/modals/submission-loader.html";
            $scope.open(url1);
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
                    $scope.close(url1);
                    $scope.open(url);
                    $scope.submitpaster(response.data);
                }, function (response) {
                    $scope.errorsArray = $scope.parseErrorArray(response.data.validationMessages);
                    url = baseurl + "assets/templates/modals/submission-failure.html";
                    $scope.submitting = false;
                    $scope.close(url1);
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
                    $scope.close(url1);
                    $scope.open(url);
                }, function (response) {
                    $scope.errorsArray = $scope.parseErrorArray(response.data.validationMessages);
                    url = baseurl + "assets/templates/modals/submission-failure.html";
                    $scope.submitting = false;
                    $scope.close(url1);
                    $scope.open(url);
                });
            }
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
        $scope.browseSubstances = function () {
            $scope.updateNav = false;
            $window.location.search = null;
            $window.location.href = baseurl + 'substances/';
        };

        $scope.viewSubstance = function () {
            $scope.updateNav = false;
            $window.location.search = null;
            $window.location.href = baseurl + 'substance/' + $scope.postRedirect;
            //  $window.location.search =null;
        };
        $scope.editNewSubstance = function () {
            $scope.updateNav = false;
            $window.location.search = null;
            $window.location.href = baseurl + 'substance/' + $scope.postRedirect + '/edit';
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
            if (!$scope.substanceClass) {
                $scope.substanceClass = $scope.substance.substanceClass;
            }
            var inp = input;
            if (typeof inp == "string") {
                inp = JSON.parse(inp);
            }
            Substance.$$setSubstance(inp).then(function (data) {
                $scope.substance = data;
                if ($scope.substance.substanceClass == "chemical") {
                    molChanger.setMol($scope.substance.structure.molfile);
                }
                if ($scope.substance.substanceClass != $scope.substanceClass) {
                    var url = baseurl + "assets/templates/modals/paste-redirect-modal.html";
                    $scope.open(url);
                }
                setTimeout(function () {
                    var oldClass = $scope.substance.substanceClass;
                    $scope.substance.substanceClass = "concept";
                    $scope.$apply();
                    setTimeout(function () {
                        $scope.substance.substanceClass = oldClass;
                        $scope.$apply();
                    }, 1);
                }, 1);
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
        //------------------------------------------------
        // GSRS Functions Begin
        //------------------------------------------------
        //************************************************/
        //Flatten CV and Expand CV
        //************************************************/
        function isCV2(ob) {
            if (typeof ob !== "object") return false;
            if (ob === null) return false;
            if (typeof ob.display !== "undefined") {
                return true;
            }
            return false;
        }
        function flattenCV2(sub) {
            for (var v in sub) {
                if (isCV2(sub[v])) {
                    if (sub[v].value) {
                        sub[v] = sub[v].value;
                    } else {
                        sub[v] = _.replace(sub[v].display, ' (not in CV)', '');
                    }
                } else {
                    if (typeof sub[v] === "object") {
                        flattenCV2(sub[v]);
                    }
                }
            }
            return sub;
        }
        function expandCV2(sub, path) {
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
                                expandCV2(value, newpath);
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
                            expandCV2(sub[field], newpath);
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

    }); //controller
    /**** controller GinasController ENDS **************************************/
    /***** GSRS Functions ENDS *************************************************/


    ginasApp.directive('escKey', function () {
        return function (scope, element, attrs) {
            var elm = element;
            if (attrs.escGlobal !== 'undefined') {
                elm = angular.element(document).find('body');
            }
            elm.bind('keydown keypress', function (event) {
                if (event.which === 27) { // 27 = esc key
                    scope.$apply(function () {
                        scope.$eval(attrs.escKey);
                    });
                    event.preventDefault();
                }
            });
        };
    });
    ginasApp.directive('modalScopeBinding', function () {
        return {
            link: function ($scope, element, attr) {
                var modalScopeVariableName = attr.modalScopeBinding;
                $scope[modalScopeVariableName] = element[0].innerHTML;
            }
        }
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
    ginasApp.directive('stringToNumber', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
                ngModel.$parsers.push(function (value) {
                    return '' + value;
                });
                ngModel.$formatters.push(function (value) {
                    return parseFloat(value);
                });
            }
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
                scope.relink = function () {
                    var url = baseurl + 'img/' + scope.id + '.svg?size={{size||150}}';
                    if (!_.isUndefined(scope.ctx)) {
                        url += '&context={{ctx}}';
                    } else {
                        url += '&context=' + Math.random();
                    }
                    if (attrs.smiles) {
                        var smiles = attrs.smiles
                            .replace(/[;]/g, '%3B')
                            .replace(/[#]/g, '%23')
                            .replace(/[+]/g, '%2B')
                            .replace(/[|]/g, '%7C');
                        url = baseurl + "render?structure=" + smiles + "&size={{size||150}}&standardize=true";
                    }
                    var template = angular.element('<img width=height={{size||150}} height={{size||150}} ng-src="' + url + '" class="tooltip-img" aria-label = "'+scope.id+ ' thumbnail" ng-cloak>');
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
                scope.formatValue = function (v) {
                    if (v) {
                        if (typeof v === "object") {
                            if (v.display) {
                                return v.display;
                            } else if (v.value) {
                                return v.value;
                            } else {
                                return null;
                            }
                        } else {
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
                        if (!unittext) {
                            unittext = "";
                        }
                        if (scope.value) {
                            var atype = scope.formatValue(scope.value.type);
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
    ginasApp.factory('referenceRetriever', function ($http, $q) {
        var url = baseurl + "api/v1/substances(";
        var references = {};
        var refFinder = {
            getAll: function (uuid, version) {
                if (references[uuid + "_" + version]) {
                    return $q(function (resolve, reject) {
                        resolve(references[uuid + "_" + version]);
                    });
                }
                var processReferences = function (refs) {
                    _.forEach(refs, function (ref, index) {
                        _.set(ref, '$$index', index + 1);
                    });
                    return refs;
                };
                var httpDoer = function (burl) {
                    return $http.get(burl, { cache: true }, {
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    });
                };
                var getVersion = function () {
                    return $q(function (resolve, reject) {
                        var onWrongVersion = function (e) {
                            httpDoer(url + uuid + ")/@edits")
                                .then(function (response) {
                                    var edits = response.data;
                                    var mversion = _.chain(edits)
                                        .filter(function (e) {
                                            return e.version === version;
                                        })
                                        .value();
                                    var ovalURL = mversion[0].oldValue;
                                    return httpDoer(ovalURL)
                                        .then(function (response) {
                                            references[uuid + "_" + version] = processReferences(response.data.references);
                                            resolve(references[uuid + "_" + version]);
                                        }).catch(function (e) {
                                            reject(e);
                                        });
                                })
                                .catch(function (e) {
                                    reject(e);
                                });
                        };
                        httpDoer(url + uuid + ")")
                            .then(function (response) {
                                var s = response.data;
                                if (s.version === version) {
                                    httpDoer(url + uuid + ")/references")
                                        .then(function (response) {
                                            references[uuid + "_" + version] = processReferences(response.data);
                                            resolve(references[uuid + "_" + version]);
                                        });
                                } else {
                                    onWrongVersion();
                                }
                            })
                            .catch(onWrongVersion);


                    });
                };

                if (version) {
                    return getVersion();
                }

                return httpDoer(url + uuid + ")/references")
                    .then(function (response) {
                        references[uuid + "_" + version] = processReferences(response.data);
                        return references[uuid + "_" + version];
                    });
            },
            getIndex: function (uuid, refuuid, version) {
                return $http.get(url + uuid + ")/references", {
                    cache: true
                }, {
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

    ginasApp.directive('infoTooltip', function ($compile, $templateRequest) {
        return {
            restrict: 'E',
            //replace: true,
            scope: {
                facetinfo: '='
            },

            link: function (scope, element, attrs) {
                var facetinfo = scope.facetinfo;
                var template;

                $templateRequest(baseurl + "assets/templates/popovers/popover-template.html").then(function (html) {
                    template = angular.element(html);
                    element.html(template).show();
                    $compile(element.contents())(scope);
                });
            }
        }
    });

    ginasApp.directive('infoPopup', function ($compile, $sce) {
        var dir = {
            restrict: 'E',
            //replace: true,
            scope: {
                type: '@',
                icon: '@',
                showPopup: '=',
                trigger: '@'
            },
            xemplate: '<a class="info-pop" popover-trigger="{{trigger}}" popover-is-open="showPopup" popover-placement="TYPEVAR" data-container="body" popover-append-to-body="false" uib-popover-template="\'htmlvar\'"><i class="fa {{icon}}"></i></a>',
            compile: function (element, attrs, linker) {

                var original = element.html(); // grab original
                element.html("");




                var rnd = 'pop-' + (Math.random() + "").replace(".", "") + '.html';

                var template = '<script type="text/ng-template" id="' + rnd + '">' +
                    original +
                    '</script>';

                //element.html(dir.xemplate); // set template html manually

                return function (scope, element, attributes) {
                    if (typeof scope.type === 'undefined') {
                        scope.type = "auto";
                    }
                    if (typeof scope.icon === 'undefined') {
                        scope.icon = "fa-info-circle";
                    }
                    if (typeof scope.trigger === 'undefined') {
                        scope.trigger = "click";
                    }


                    //scope.showPopup=true;
                    var elm = angular.element(template + dir.xemplate.replace("htmlvar", rnd).replace("TYPEVAR", scope.type));

                    scope.htmlvar = $sce.trustAsHtml(original);
                    element.append($compile(elm)(scope));


                }
            }
        };
        return dir;
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
                sreferences: '=?',
                divid: '=?',
                version: '=?'
            },
            require: '^referencesmanager',
            link: function (scope, element, attrs, referencesCtrl) {


                if (scope.sreferences) {
                    scope.freferences = {};
                    _.chain(scope.sreferences)
                        .map(function (r) {
                            if (r.term) {
                                return r.term;
                            }
                            return r;
                        })
                        .map(function (r) {
                            scope.freferences[r] = true;
                            return r;
                        })
                        .value();
                }

                //Not sure about this, I think it's using AJAX, which it shouldn't.
                referencesCtrl.referenceRetriever.getAll(scope.substance, scope.version).then(function (response) {

                    //TODO: re-evaluate
                    scope.references = response;
                    if (scope.freferences) {
                        scope.references = _.chain(scope.references)
                            .filter(function (r) {
                                return scope.freferences[r.uuid];
                            })
                            .value();
                    }



                });

                scope.getClass = function (index) {
                    return referencesCtrl.getClass(index);
                };

                scope.baseurl = baseurl;
                if (scope.baseurl[scope.baseurl.length - 1] === '/') {
                    scope.baseurl = scope.baseurl.substring(0, scope.baseurl.length - 1);
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
                    var templateString = angular.element('<div class ="row reftable"><div class ="col-md-8">'
                        + _.join(links, ",")
                        + ' </div><div class="col-md-4"><span class="btn btn-primary pull-right" type="button" uib-tooltip="Show all references" ng-click="toggle()"><i class="fa fa-long-arrow-down"></i></span><div></div>');
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

    ginasApp.directive('refCount', function ($compile) {
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
                    scope.buttonLabel = "view";
                    var templateString = angular.element('<div class ="reftable">'
                        + '<div style = "float:left" class =" center-text">'
                        + '</div><div style = "float:left"><button class="btn btn-primary reference-button" type="button" uib-tooltip="Show all references" ng-click="toggle()" >{{buttonLabel}} '+links.length+'<br/>reference(s)</button></div></div>');
                    element.append(angular.element(templateString));
                    $compile(templateString)(scope);
                });

                scope.toggle = function () {
                    referencesCtrl.toggle(scope, attrs.divid);
                    scope.buttonLabel = scope.buttonLabel === 'view' ? 'hide' : 'view';
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
                if (!_.isNull(scope.citation.url) && !_.isUndefined(scope.citation.url)) {
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
                            if (_.isUndefined(scope.referenceobj[scope.field])) {
                                _.set(scope.referenceobj, scope.field, []);
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
        var versionurl = baseurl + "api/v1/substances($UUID$)/version";
        var editurl = baseurl + "api/v1/edits($UUID$)/$oldValue";
        var fetcher = {
            fetchCurrentVersion: function (uuid) {
                var url2 = versionurl.replace("$UUID$", uuid);
                return $http.get(url2, {
                    cache: true
                }, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    return response.data;
                });
            },
            fetch: function (uuid, version) {
                if (version) {
                    return fetcher.fetchVersion(uuid, version);
                }

                var url2 = url + uuid + ")";
                return $http.get(url2, {
                    cache: true
                }, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    return response.data;
                });
            },
            fetchVersion: function (uuid, version) {
                return fetcher.fetchCurrentVersion(uuid)
                    .then(function (r) {
                        if (r + "" === version + "") {
                            return fetcher.fetch(uuid);
                        } else {
                            var url2 = url + uuid + ")/@edits";
                            return $http.get(url2, {
                                cache: true
                            }, {
                                headers: {
                                    'Content-Type': 'text/plain'
                                }
                            }).then(function (response) {
                                console.log("ERROR");
                                console.log(response);
                                var oversion = _.chain(response.data)
                                    .filter(function (edit) {
                                        return version + "" === edit.version;
                                    })
                                    .value();

                                if (oversion.length >= 1) {
                                    var nurl = oversion[0].oldValue;
                                    nurl = editurl.replace("$UUID$", nurl.split("(")[1].split(")")[0]);

                                    return $http.get(nurl, {
                                        cache: true
                                    }, {
                                        headers: {
                                            'Content-Type': 'text/plain'
                                        }
                                    }).then(function (response) {
                                        return response.data;
                                    });
                                } else {
                                    return fetcher.fetch(uuid);
                                }
                            });
                        }
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
                    }
                    /*else if (scope.acidClass === 'plain') {

                                           $templateRequest(baseurl + "assets/templates/tooltips/tooltip-template-plain.html").then(function (html) {
                                               template = angular.element(html);
                                               element.html(template).show();
                                               $compile(element.contents())(scope);
                                           });

                                       }*/
                    else {

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

                scope.clicked = function () {
                    scope.scrollTo(scope.acidClass, scope.acid);
                    scope.$emit("selected", scope.acid);
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
                version: '=?',
                subid: '=?',
                view: '@',
                numbers: '=',
                selected: '='
            },
            link: function (scope, element, attrs) {
                var sclass = attrs.subclass;
                if (_.isUndefined(sclass)) {
                    if (_.has(scope.parent, 'protein')) {
                        sclass = "protein";
                    } else {
                        sclass = "nucleicAcid";
                    }
                }
                scope.numbers = true;
                scope.edit = true;

                if (scope.obj) {
                    scope.subid = scope.obj.uuid;
                    scope.obj.subunitIndex = _.toInteger(scope.index);
                }

                scope.preformatSeq = function (seq) {
                    var ret = "";
                    if (seq) {
                        for (var i = 0; i < seq.length; i += 10) {
                            if (i % 60 == 0) {
                                ret += "\n";
                            }
                            ret += seq.substr(i, 10) + "     ";

                        }
                    }
                    return ret.trim();
                };

                scope.postFormatSeq = function (seq) {
                    return seq.replace(/\s/g, "");
                };

                scope.fastaFormat = function () {
                    if (!scope.obj) return "";
                    var seq = scope.obj.sequence;
                    var ret = "";
                    if (seq) {
                        seq = seq.replace(/\s/g, "");
                        for (var i = 0; i < seq.length; i += 60) {
                            if (i + 60 < seq.length) {
                                ret += seq.substr(i, 60) + "\n";
                            } else {
                                ret += seq.substr(i, 60);
                            }
                        }
                    }
                    return ret;
                };

                scope.toggleEdit = function () {
                    scope.edit = !scope.edit;
                    if (scope.edit) { //edit starts
                        scope.startEdit();
                        scope.obj.editing = "open";
                    } else { //edit is done
                        scope.obj.editing = "closed";
                        scope.obj.sequence = scope.postFormatSeq(scope.obj.$sequence);
                        scope.parseSubunit();
                        scope.fastaview = scope.fastaFormat();
                    }

                };

                scope.startEdit = function () {
                    scope.obj.editing = "open";
                    scope.obj.$sequence = scope.preformatSeq(scope.obj.sequence);
                };

                scope.isSelected = function (site) {
                    if (!scope.selected) return;

                    var isselected = false;
                    _.forEach(scope.selected, function (selSite) {
                        if (selSite.subunitIndex === site.subunitIndex && selSite.residueIndex === site.residueIndex) {
                            isselected = true;
                        }
                    });
                    return isselected;
                };

                scope.parseSubunit = function () {
                    subunitParser.parseSubunit(scope.parent, scope.obj, scope.index);
                };

                scope.highlight = function (acid) {
                    var bridge = {};
                    if (_.has(acid, 'disulfide')) {
                        bridge = acid.disulfide;
                    } else {
                        bridge = acid.otherLinks;
                    }
                    if (bridge && bridge.residueIndex) {
                        var allAA = element[0].querySelectorAll('amino-acid');
                        var targetElement = angular.element(allAA[bridge.residueIndex - 1]);
                        targetElement.isolateScope().showBridge();
                    }
                };

                scope.cleanSequence = function () {
                    scope.obj.sequence = subunitParser.cleanSequence(scope.obj.sequence);
                    scope.obj.$sequence = scope.preformatSeq(subunitParser.cleanSequence(scope.obj.$sequence));
                    scope.parseSubunit();
                };

                var display = [];
                if (_.isUndefined(scope.parent)) {
                    APIFetcher.fetch(scope.uuid, scope.version).then(function (data) {
                        scope.parent = data;
                        var sclass;
                        if (_.has(data, 'protein')) {
                            scope.obj = data.protein.subunits[scope.index];
                            scope.index = scope.index - 0 + 1;
                            sclass = "protein";
                        } else {
                            scope.obj = data.nucleicAcid.subunits[scope.index];
                            scope.index = scope.index - 0 + 1;
                            sclass = "nucleicAcid";
                        }
                        subunitParser.getResidues(sclass).then(function () {
                            scope.parseSubunit();
                            if (_.isUndefined(scope.obj.sequence)) {
                                scope.edit = true;
                                scope.startEdit();
                            }
                        });
                        scope.fastaview = scope.fastaFormat();
                        //scope.parseSubunit();
                    });
                } else {
                    subunitParser.getResidues(sclass).then(function () {
                        scope.parseSubunit();
                        if (_.isUndefined(scope.obj.sequence)) {
                            scope.edit = true;
                            scope.startEdit();
                        }
                        scope.fastaview = scope.fastaFormat();

                    });
                }
                scope.edit = false;
            },
            templateUrl: baseurl + "assets/templates/elements/subunit.html"
        };
    });
    //used to set the molfile in the sketcher externally
    ginasApp.service('structureImgUp', function ($http, CVFields, UUID) {
        var sk;
        this.setSketcher = function (sketcherInstance) {
            sk = sketcherInstance;
        };
        this.setImage = function (img) {
            sk.modalToClipboard(img);
        };
    });


    ginasApp.service('molChanger', function ($http, CVFields, UUID) {

        var sk;
        var that = this;
        this.setSketcher = function (sketcherInstance) {
            sk = sketcherInstance;
        };
        this.setMol = function (mol) {
            sk.sketcher.setMolfile(mol);
        };

        this.getMol = function () {
            return sk.getMol();
        };

        this.getSmiles = function () {
            return sk.sketcher.getSmiles();
        };
        this.clean = function () {
            var smi= that.getSmiles();

            var url = baseurl + 'structure';
            $http.post(url, smi, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
                if (!_.isEmpty(data)) {
                    that.setMol(data.structure.molfile);
                } else {
                    alert('Unable to clean the structure');
                }
    });
        };
    });
    ginasApp.directive('sketcher', function ($compile, $http, $timeout, UUID, polymerUtils, CVFields, localStorageService, molChanger, structureImgUp, Substance, $rootScope) {
        var t = {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                structure: '=?',
                mol: '=ngModel'
            },

            link: function (scope, element, attrs) {

                var setLoading = function (b) {
                    $rootScope.isGlobalLoading = b;
                    try {
                        $timeout(function () {
                            $rootScope.$apply();
                        });
                    } catch (error) {
                        console.error(error);
                    }

                };

                scope.showCanvas = true;
                scope.fromImage = false;
                scope.canvasLabel = "";

                var url = baseurl + 'structure';

                if (!_.isUndefined(scope.parent.structure)) {
                    scope.mol = scope.parent.structure.molfile;
                }
                var canvasHTML = '<div class="text-center" id = "canvas-wrapper" ng-show = "canvasLabel != \'\'" >' +
                    '<div id = "canvas-label" class=" col-md-12" style = "text-align:center;padding-bottom:5px;padding-top:5px"  ><b>{{canvasLabel}}</b> &nbsp ' +
                    '<a class="btn btn-primary" ng-show="fromImage&&showCanvas" ng-click="showCanvas=!showCanvas" class="">Hide</a>' +
                    '<a class="btn btn-primary" ng-show="fromImage&&!showCanvas" ng-click="showCanvas=!showCanvas" class="">Show</a>' +
                    '</div><div class=" col-md-12" text-center" ng-show = "invalidStructure" ng-init = "invalidStructure = false"><b style = "color:red">Structure not detectable</b></span></div>' +
                    '<canvas height="1" ng-show="showCanvas" id="clip_canvas" style="max-width:800px;"></canvas>' +
                    '</div>';
                var template = angular.element('<div><div id="sketcherForm" dataformat="molfile"></div> <div class = "col-md-12" id = "testing">' +
                    '<div class = "clean-structure"><a ng-click="cleanStructure()" uib-tooltip="Clean Structure"><i class="fa fa-magic fa-2x"></i>Clean Structure</a></div>' +
                    '<div class="text-center">' +
                    'Load an image by pasting a copied image into the canvas with <code>ctrl + v</code>, or dragging a local image file' +
                    '</div> <div id = "canvas_cont">' + canvasHTML + '</div> </div> </div>');
                element.append(template);
                $compile(template)(scope);

                scope.cleanStructure = function(){
                    molChanger.clean();
                }


                scope.merge = function (oldStructure, newStructure) {
                    var definitionalChange = (oldStructure["hash"] !== newStructure["hash"]);
                    _.forIn(newStructure, function (value, key) {
                        var cvname = null;
                        switch (key) {
                            case "stereochemistry":
                                cvname = "STEREOCHEMISTRY_TYPE";
                                break;
                            case "opticalActivity":
                                cvname = "OPTICAL_ACTIVITY";
                                break;
                            default:
                                oldStructure[key] = value;
                        }
                        if (cvname !== null) {
                            CVFields.search(cvname, value).then(function (response) {
                                oldStructure[key] = response[0];
                            });
                        }

                    });
                    return definitionalChange;
                };

                scope.updateMolServer = function (mfile, force, reset) {
                    setLoading(false);
                    var url = baseurl + 'structure';
                    $http.post(url, mfile, {
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (data) {
                        if (force && scope.parent.substanceClass === "polymer") {
                            scope.parent.polymer.idealizedStructure = data.structure;
                            scope.structure = data.structure;
                            CVFields.getCV("POLYMER_SRU_TYPE").then(function (response) {
                                var cv = response.data.content[0].terms;
                                for (var i in data.structuralUnits) {
                                    // var cv = response.data.content[0].terms;
                                    data.structuralUnits[i].type = _.find(cv, ['value', data.structuralUnits[i].type]);
                                }
                                polymerUtils.setSRUConnectivityDisplay(data.structuralUnits);

                                //merge amounts with whatever is already there
                                var amounts = {};

                                _.chain(scope.parent.polymer.structuralUnits)
                                    .map(function (sru) {
                                        if (sru.amount) {
                                            sru.amount.uuid = null;
                                        }
                                        amounts[sru.label] = sru.amount;
                                    })
                                    .value();

                                _.chain(data.structuralUnits)
                                    .map(function (sru) {
                                        var oldAmount = amounts[sru.label];
                                        if (oldAmount) {
                                            sru.amount = oldAmount;
                                        }
                                    })
                                    .value();

                                polymerUtils.setSRUFromConnectivityDisplay(data.structuralUnits);
                                scope.parent.polymer.structuralUnits = data.structuralUnits;
                            });
                        }
                        if (scope.parent.structure) {
                            if (data.structure) {
                                data.structure.id = scope.parent.structure.id;
                            }
                        } else {
                            scope.parent.structure = {};
                        }
                        var defChange = scope.merge(scope.parent.structure, data.structure);

                        if (defChange) {
                            scope.parent.moieties = [];
                            _.forEach(data.moieties, function (m) {
                                m["$$new"] = true;
                                //this is used to make a cv element out of the moiety units, which are re-written as strings with each round trip
                                if (!_.isObject(m.countAmount.type)) {
                                    var temp = { value: m.countAmount.type, display: m.countAmount.type };
                                    m.countAmount.type = temp;
                                }
                                if (!_.isObject(m.countAmount.units)) {
                                    var temp = { value: m.countAmount.units, display: m.countAmount.units };
                                    m.countAmount.units = temp;
                                }
                                var moi = {};
                                scope.merge(moi, m);
                                scope.parent.moieties.push(moi);
                            });
                        }


                        if (data.structure) {
                            _.set(scope.parent, 'q', data.structure.smiles);
                            if (reset) {
                                scope.sketcher.setMolfile(data.structure.molfile);
                            }
                        }

                    });
                };
                scope.updateMol = function (force) {
                    scope.updateMolServer(scope.mol, force, false);
                };
                scope.$parent.updateMol = scope.updateMol;

                scope.sketcher = new JSDraw("sketcherForm");
                scope.sketcher.options.data = scope.mol;
                scope.sketcher.setMolfile(scope.mol);

                if (afterSketcherMade) {
                    afterSketcherMade();
                }
                scope.clean = function (mol) {

                    //remove "mul" from multiple amount
                    mol = mol.replace(/M[ ]*SMT.*mul.*/g, "@")
                        .replace(/\n/g, "|_|")
                        .replace(/[@][|][_][|]/g, "")
                        .replace(/[|][_][|]/g, "\n");

                    return mol;
                };
                // Extract information about charges from jsdraw XML.
                // This is only needed due to a bug in jsdraw where molfiles don't
                // produce the right charge components when charges are over 3.
                // Returns null if no charges found.

                scope.getMChargeFromXML = function (xml) {
                    var rep = function (v, n) {
                        var t = "";
                        for (var i = 0; i < n; i++) {
                            t = t + v;
                        }
                        return t;
                    };

                    var leftPad = function (v, p) {
                        return rep(" ", p - v.length) + v;
                    };

                    var aai = 1;

                    var charges = _.chain($(xml).find("a[i]"))
                        .map(function (a) {
                            var ai = $(a).attr("i");
                            var ac = $(a).attr("c");
                            if (typeof ac === 'undefined') {
                                ac = 0;
                            }
                            var o = {
                                "i": (aai++),
                                "c": ac - 0
                            };
                            o.toString = function () {
                                return leftPad(o.i + "", 4) + leftPad(o.c + "", 4);
                            };
                            return o;
                        })
                        .filter(function (a) {
                            return a.c != 0;
                        })
                        .value();

                    if (charges.length > 0) {
                        var chgCount = function (count) {
                            return "M  CHG" + leftPad(count + "", 3);
                        };

                        return _.chain(charges)
                            .chunk(8)
                            .map(function(c){ return chgCount(c.length) +
                                    _.chain(c)
                                    .map(function(ic){return ic.toString();})
                                    .value()
                                    .join("");
                            })
                            .value()
                            .join("\n");
                    }
                    return null;
                };

                scope.getMol = function () {

                    var chargeLine = scope.getMChargeFromXML(scope.sketcher.getXml());
                    var mfile = scope.sketcher.getMolfile();

                    mfile=mfile.replace(/0.0000[ ]D[ ][ ][ ]/g,"0.0000 H   ");
                    //can't find charge section
                    if (mfile.indexOf("M  CHG") < 0) {

                        if (chargeLine !== null) {
                            var lines = mfile.split("\n");
                            for (var i = lines.length - 1; i >= 3; i--) {
                                if (lines[i] === "M  END") {
                                    var old = lines[i];
                                    lines[i] = chargeLine;
                                    lines[i + 1] = old;
                                    mfile = lines.join("\n");
                                    break;
                                }
                            }
                        }
                    }
                    //alert("using:" + mfile);
                    return scope.clean(mfile);
                };

                scope.sketcher.options.ondatachange = function () {
                    scope.mol = scope.getMol();
                    if (attrs.ajax == 'false') {
                        $timeout(function () {
                            _.set(scope.parent, 'q', scope.mol);
                        }, 0);
                    } else {
                        scope.updateMol();
                    }
                };
                molChanger.setSketcher(scope);
                structureImgUp.setSketcher(scope);
                var structureid = (localStorageService.get('structureid') || false);

                if (localStorageService.get('editID')) {
                    structureid = false;
                }

                if (scope.parent.substanceClass === 'polymer' && (scope.parent.polymer.displayStructure)) {
                    scope.sketcher.setMolfile(scope.parent.polymer.displayStructure.molfile);
                } else {
                    if (!_.isUndefined(scope.parent.polymer)) {
                        if (!_.isUndefined(scope.parent.polymer.idealizedStructure)) {
                            scope.mol = scope.parent.polymer.idealizedStructure.molfile;
                            if (!_.isNull(scope.mol)) {
                                scope.updateMol();
                            }
                        }
                    }
                }
                if (attrs.load) {
                    var load = attrs.load;
                    $timeout(function () {
                        scope.sketcher.setMolfile(load);
                    }, 0);
                }
                if (structureid) {
                    var url = baseurl + 'api/v1/structures/' + structureid;
                    $http.get(url, { cache: true }).then(function (response) {
                        scope.sketcher.setMolfile(response.data.molfile);
                        _.set(scope.parent, 'q', response.data.smiles);
                        localStorageService.remove('structureid');
                    });
                }


                var sketcherElm = {
                    "get": function () {
                        return element[0];
                    }
                };

                var CLIPBOARD = new CLIPBOARD_CLASS("clip_canvas", true);


                /**
                 * image pasting into canvas
                 *
                 * @param {string} canvas_id - canvas id
                 * @param {boolean} autoresize - if canvas will be resized
                 */
                function CLIPBOARD_CLASS(canvas_id, autoresize) {
                    var _self = this;
                    var canvas = null;
                    var ctx = null;
                    //handlers
                    document.addEventListener('paste', function (e) {
                        _self.paste_auto(e, 'paste');
                    }, false);

                    document.addEventListener('dragstart', function (e) {
                        e = e || event;
                        e.preventDefault();
                    }, false);

                    document.addEventListener('dragover', function (e) {
                        e = e || event;
                        e.preventDefault();
                    }, false);

                    document.addEventListener('dragleave', function (e) {
                        e = e || event;
                        e.preventDefault();
                    }, false);
                    sketcherElm.get().addEventListener('dragover', function (e) {
                        e = e || event;
                        e.stopPropagation();
                        e.preventDefault();
                        sketcherElm.get().parentElement.classList.add('dragover');
                    }, false);
                    sketcherElm.get().addEventListener('dragleave', function (e) {
                        e = e || event;
                        e.stopPropagation();
                        e.preventDefault();
                        sketcherElm.get().parentElement.classList.remove('dragover');
                    }, false);

                    document.addEventListener('drop', function (e) {
                        e.stopPropagation();
                        e.preventDefault();
                        sketcherElm.get().parentElement.classList.remove('dragover');
                        _self.paste_auto(e, 'drop');
                    }, false);

                    //bypass canvas functions if loading from modal
                    this.modalImport = function(file) {
                        canvas = document.getElementById(canvas_id);
                        ctx = document.getElementById(canvas_id).getContext("2d");
                        this.loadImage(file)();
                    }

                    // local pointer of "this" keyword for the surrounding
                    // function
                    var _thisfun = this;

                    // supplier of the load event for an image, but does not actually
                    // load yet, until specifically called (this is a function that returns
                    // a function)
                    this.loadImage = function(blob){
                    	var source=null;
                    	if(typeof blob  ==="string"){
                    		source=blob;
                    	}else{
                    		var URLObj = window.URL || window.webkitURL;
                    		source = URLObj.createObjectURL(blob);
                    	}

                    	return function(){
                    		_thisfun.paste_createImage(source);
                    		return true;
                    	};
                    }

                    //method to receive pasted/droped data
                    this.paste_auto = function(e, method) {
                    	var _this=this;
                    	canvas = document.getElementById(canvas_id);

                        ctx = document.getElementById(canvas_id).getContext("2d");
                        var gotImage = false;
                        var text = null;

                        //specifically handles drop
                        if (method == 'drop') {
                            var items = e.dataTransfer.files;

                            //if there are no files dropped, there could be html/text dropped
                            //handle those, butonly those that have embedded src tags
                            //then do no other processing
                            if(items.length==0){
                            	for(var ii=0;ii<e.dataTransfer.items.length;ii++){
                            		if(e.dataTransfer.items[ii].type==="text/html"){
                            			e.dataTransfer.items[ii].getAsString(function(s){
                            				if(s.indexOf("<img") ==0){
                            					var url = JSON.parse(s.split("src=")[1].split(/[ |>]+/)[0].trim());
                            					if(_this.loadImage(url)()){
                                                    scope.invalidStructure = false;
                                                    e.preventDefault();
                                                }
                                            }
                                        });
                                    }
                                }
                                return;
                            }

                        }else if (method == 'paste'){
                        	// get the items from the clipboard if the method is
                        	// paste, but not if it's a drop event
                            var items = e.clipboardData.items;
                        }

                        //cancel processing if items is empty
                        if (!items) return;


                        var activated=false;

                        //we only consider going forward if the sketcher is active. The sketcher is only active if
                        //1. There is no item in the page that has the :focus property (e.g. an input / textarea) AND
                        //2. The method is NOT paste (e.g. drop, which works regardless of focus) OR the sketcher is flagged as active
                        activated= (method!=='paste' || scope.sketcher.activated) && ($(':focus').length==0);
                    	if (activated) {
                    		// this function will load text if it's received
                    		// and will be called later
                    		var loadText=function (r) {
                                if (r) {
                                    var text = r;
                                        try {
                                            if (text.indexOf("<div") == -1) {
                                                setLoading(true);
                                                scope.updateMolServer(text, true, true);
                                            }
                                        } catch (e) {
                                            if (text.indexOf("<div") == -1) {
                                                setLoading(true);
                                                scope.updateMolServer(text, true, true);
                                            }
                                        }
                                }
                            };
                            // this method queues up (but does not load) an image if it's received
                            // and will be called later. You must call the function returned
                            // by this method to actually activate the event.
                            var loadImage=function(img){
                            	scope.invalidStructure = false;
                            	var blob;
                                if(method == "drop"){
                                    blob = img;
                                }else if(method == "paste") {
                                    blob =img.getAsFile();
                                }
                                return _this.loadImage(blob);
                            }
                            //map of the clipboard elements by type
                    		var clip={};

                    		//iterate through the items, put the image
                    		//and plain text elements into the clip map
                    		for (var i = 0; i < items.length; i++) {
                    			if (items[i].type.indexOf("image") !== -1) {
                    				//if(!clip["image"])clip["image"]=[];
                    				clip["image"]=items[i];
                    			}else if (items[i].type.indexOf("text/plain") !== -1) {
                    				clip["text"]=items[i];
                    			}
                    		}

                    		//If there's text but no image, try to interpret the text
                    		if(clip["text"] && ! clip["image"]){
                    			clip["text"].getAsString(loadText);
        						e.preventDefault();

        				    //If there's image but no text, try to interpret the image
                    		}else if(clip["image"] && ! clip["text"]){
                    			loadImage(clip["image"])();
                    			e.preventDefault();

                    	    //If there's image and text, a choice must be made, but the browser will
                    	    //invalidate the items after the callback, so we must queue up the image
                    	    //to be loaded first, and only call it to be loaded after we make a decision
                    		//about the text

                    		}else if(clip["image"] && clip["text"]){

                    			//this queues up the image to be loaded, calling
                    			//the callback will load the image
                    			var callback=loadImage(clip["image"]);

                    			//async call to get the text element copied
                    			clip["text"].getAsString(function(r){
                    				//if the text "///" exists, that probably means there's a file URL present,
                    				//which is typically part of a paste event when an image is copied locally on some
                    				//platforms. Don't interpret this as text.
                    				if(r.indexOf("///")>-1){
                    					//load as image
                    					callback();
                    				//otherwise it's probably text that was important, try to interpret it
                    				}else{
                    					loadText(r);
                    				}
                    			});
                    			//we will always cancel the event if we made it this far, even if nothing meaningful comes from it.
                    			//Since the activation check is present, this is almost always okay.
                    			e.preventDefault();
                    		}

                        }
                    };
                    //draw pasted image to canvas
                    this.paste_createImage = function (source) {
                        scope.fromImage = true;
                        var myEl = angular.element(document.querySelector('canvas'));
                        var pastedImage = new Image();
                        pastedImage.onload = function () {
                            if (autoresize === true) {
                                //resize
                                canvas.width = pastedImage.width;
                                canvas.height = pastedImage.height;

                                myEl.addClass('canvas-display');
                                scope.canvasLabel = "Original Image";

                                //$compile(angular.element(document.getElementById('canvas-label').innerHTML ="<b>Original Image<b>"))(scope);
                            } else {
                                //clear canvas
                                ctx.clearRect(0, 0, canvas.width, canvas.height);
                                myEl.removeClass('canvas-display');
                                scope.canvasLabel = "Original Image";
                            }
                            ctx.fillStyle = "#FFFFFF";
                            ctx.fillRect(0, 0, pastedImage.width, pastedImage.height);
                            ctx.drawImage(pastedImage, 0, 0);
                            var dataURL = canvas.toDataURL();
                            if(dataURL.length>100000){
                            	dataURL = canvas.toDataURL('image/jpeg', 100000/dataURL.length);
                            	console.log(dataURL.length);

                            }
                            setLoading(true);
                            //TODO: Change to use angular
                            $.ajax({
                                url: "/ginas/app/api/v1/foo/ocrStructure",
                                type: "POST",
                                headers: {  'Access-Control-Allow-Origin': 'http://localhost:9000' },
                                data: dataURL,
                                contentType: 'application/json',
                                success: function (response) {
                                    setLoading(false);
                                    var myresp = JSON.parse(response);
                                    scope.sketcher.setMolfile(myresp.molfile);

                                },
                                error: function (rep, error, t) {
                                    //error handling
                                    setLoading(false);
                                    scope.invalidStructure = true;
                                    console.log(error);
                                }
                            });
                        };
                        pastedImage.crossOrigin = "anonymous";
                        pastedImage.src = source;
                    };
                }

                scope.modalToClipboard = function(data){
                    CLIPBOARD.modalImport(data);
                }
            }
        };


        return t;
    });
    ginasApp.directive('modalButton', function ($compile, $templateRequest, $http, $uibModal, molChanger, structureImgUp, FileReader) {
        return {
            scope: {
                type: '=',
                structureid: '=',
                format: '@',
                format2: '@',
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
                        template = angular.element(' <a aria-label="Upload" uib-tooltip ="Upload" tabindex="0" role="button" structureid=structureid format=format ><span class="sr-only">Upload Data</span><i class="fa fa-upload fa-2x success"></i></a>');
                        element.append(template);
                        $compile(template)(scope);
                        break;
                    case "image":
                        template = angular.element(' <a aria-label="Upload" uib-tooltip ="Import image file" active ="image" tabindex="0" role="button" structureid=structureid format=format ng-keypress="open();" ng-click="open()"><span class="sr-only">Upload Image</span><i class="fa success fa-file-image-o fa-2x"></i></a>');
                        element.append(template);
                        scope.active ="image";
                        $compile(template)(scope);
                        templateUrl = baseurl + "assets/templates/modals/mol-import.html";
                        break;
                    case "import":
                        template = angular.element(' <a aria-label="Import" uib-tooltip ="Import text / Molfile" active = "text" tabindex="0" role="button" ng-keypress="open();" ng-click="open()"><span class="sr-only">Import Data</span><i class="fa fa-clipboard fa-2x success"></i></a>');
                        element.append(template);
                        scope.active = "text";
                        $compile(template)(scope);
                        templateUrl = baseurl + "assets/templates/modals/mol-import.html";
                        break;
                    case "export":
                        template = angular.element(' <a aria-label="Export" uib-tooltip ="Export" tabindex="0" role="button" ng-keypress="getExport()" ng-click = "getExport()"><span class="sr-only">Export Data</span><i class="fa fa-external-link fa-2x success"></i></a>');
                        element.append(template);
                        $compile(template)(scope);
                        templateUrl = baseurl + "assets/templates/modals/mol-export.html";
                        break;
                }

                scope.setPreview = function (file) {
                    FileReader.readAsText(file, scope).then(function (response) {
                        scope.molfile = response;
                    });
                };

                scope.imageUpload = function(element,file){
                    if ((file.type.match('image.*'))){
                        FileReader.readAsDataURL(element.uploadImg, scope).then(function(response){
                            structureImgUp.setImage(response);
                        });
                    }
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
                            var warning = {
                                type: 'warning',
                                message: 'not a vaild molfile'
                            };
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

                scope.getFormat = function (fmt) {
                    var url = baseurl + 'export/' + scope.structureid + '.' + fmt;
                    return $http.get(url, {
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (response) {
                        return response;
                    });
                };

                scope.getExportDisplay = function (fmt) {
                    switch (fmt) {
                        case "fas":
                            return "FASTA";
                        case "mol":
                            return "Molfile";
                        case "sdf":
                            return "SD File";
                        default:
                            return "Export";
                    }
                };

                scope.getExport = function () {
                    scope.formatName = scope.getExportDisplay(scope.format);
                    if (scope.format2) {
                        scope.formatName2 = scope.getExportDisplay(scope.format2);
                    }

                    if (_.isUndefined(scope.structureid)) {
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
                    } else {

                        var url = baseurl + 'export/' + scope.structureid + '.' + scope.format;
                        $http.get(url, {
                            headers: {
                                'Content-Type': 'text/plain'
                            }
                        }).success(function (response) {
                            if (scope.format2) {
                                scope.getFormat(scope.format2).then(function (d) {
                                    scope.exportData2 = d.data;
                                });
                            }
                            scope.exportData = response;
                            if (scope.format != 'fas') {
                                url = baseurl + 'export/' + scope.structureid + '.smiles';

                                $http({
                                    url: url,
                                    method: 'GET',
                                    transformResponse:[]
                                }, {
                                    headers: {
                                        'Content-Type': 'text/plain'
                                    },
                                    responseType: 'text'
                                }).success(function (response) {
                                    scope.exportSmiles = response;
                                });
                            }

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



    //*****************************************************************
    // Experimental !!!
    //*****************************************************************


    /*
     * 
     * 
     * I want another directive (possibly same as below) for a simlpe in-line
     * preview of a substance with hover-over / click to expand.
     * 
     * <substance-line 
     *     substanceuuid="f982d178-7bcb-448a-9fd3-25c59e181c7b"
     *     preview-on="['hover','click']"
     *     
     *     preview-substance-views="['Preferred Term','img','Approval ID']"
     * >
     * </substance-line>
     * 
     * The above will just show the name + approvalID
     * 
     * 
     * 
     * 
     */

    ginasApp.directive("substanceLine", function ($compile, APIFetcher) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                substance: '=',
                substanceuuid: '@',
                showPopup: '@'
            },
            link: function (scope, element, attrs) {
                var div = "<span ng-class='iclass' ng-click='click()' aria-label = 'keep open' class='stop-prop' >\n" +
                    "<info-popup ng-mouseleave='mouesOut()' ng-mouseover='mouesIn()'  aria-label = 'info-popup' icon='fa-search-plus' show-popup=showPopup trigger='none' >" +
                    "  <div style='text-align: center;'>" +
                    "  <substance-preview substance-views=\"['Preferred Term','img','Approval ID','iconButtons']\"" +
                    "     substanceuuid='" + scope.substanceuuid + "'>" +
                    "  </substance-preview>" +
                    "  </div>" +
                    "</info-popup>\n";
                // div += "<span>{{_substance._name}} [{{_substance._approvalIDDisplay}}]</span>\n" + " </span>";
                div += "</span>";
                scope.iclass = "";


                scope.mouesIn = function () {
                    if (!scope.showPopup) {
                        scope.fromMouse = true;
                        scope.showPopup = true;
                    }
                };

                scope.mouesOut = function () {
                    if (scope.fromMouse) {
                        scope.showPopup = false;
                        scope.fromMouse = false;
                    }
                };

                scope.click = function () {
                    scope.iclass = "keep-open";
                    scope.fromMouse = false;
                    scope.showPopup = !scope.showPopup;
                    setTimeout(function () {
                        scope.iclass = "";
                        scope.$apply();
                    }, 10);
                };

                scope.update = function () {
                    //console.log("Updating");
                };

                if (!scope.substance) {
                    var uuid = scope.substanceuuid;
                    if (uuid) {
                        APIFetcher.fetch(uuid, scope.version)
                            .then(function (s) {

                                scope._substance = s;
                                scope.update();
                            });
                    }
                } else {
                    scope._substance = scope.substance;
                    scope.update();
                }
                element.empty().append($compile(div)(scope));
            }
        }
    });


    /*
        <substance-preview> Directive
        	
    	This will be a basic widget for previewing a substance.
    	
    	A few examples:
    	
    	<!-- From a scope substance object -->
    	<substance-preview substance="someScopeSubstanceJson" ></substance-preview>
    	<!-- From a scope substance uuid -->
    	<substance-preview substanceUUID="5ce23012-506e-47f5-8601-44b7d605a929" ></substance-preview>
    	
    	For now, if you have the two above, you can discover ways of doing it later.
    	
    	Now, we also need to decide what things to show:
    		1. Name
    		2. Structure
    		3. Edit Icon
    		4. Link
    		5. etc ...
    		
	<substance-preview substance-views="['Preferred Term','img','Approval ID']"
    	                   substanceuuid="f982d178-7bcb-448a-9fd3-25c59e181c7b">
    	                   
    	</substance-preview>
    	
    	The above is an example of how to do that. As of this moment, it will use whatever
    	fetchers are in the js api, and can render those. But there will need to be more
    	room for interaction.                   
    */

    ginasApp.directive("substancePreview", function ($compile, APIFetcher) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                substance: '=',
                substanceuuid: '@',
                substanceViews: '='
            },
            link: function (scope, element, attrs) {
                //Set the global helper


                //######################
                //HELPER FUNCTIONS START
                //######################
                const iconButtonMaker = function () {
                    var u = {};
                    u.setURL = function (url) {
                        u.url = url;
                        return u;
                    };
                    u.setTitle = function (t) {
                        u.title = t;
                        return u;
                    };
                    u.setIcon = function (i) {
                        u.icon = i;
                        return u;
                    };
                    return u;
                };

                const ViewFetcher = function () {
                    var vf = {};

                    vf.name = function (n) {
                        if (n) {
                            vf._name = n;
                            return vf;
                        } else {
                            return vf._name;
                        }
                    };

                    vf.isSet = false;

                    vf.setFunction = function (f) {
                        vf._f = f;
                        vf.isSet = true;
                        return this;
                    };

                    vf.make = function (s) {
                        //should be a promise?
                        return vf._f(s);
                    };

                    vf.IDENTITY = function () {
                        return ViewFetcher().setFunction(function (s) {
                            return JPromise.ofScalar(vf._prepareSubstance(s));
                        });
                    };

                    vf._prepareSubstance = function (s) {
                        var mods = SubstanceBuilder.fromSimple(s);
                        mods._urls = [];

                        mods._urls.push(iconButtonMaker()
                            .setURL(baseurl + "substance/" + mods.uuid + "/edit")
                            .setTitle("Edit Record")
                            .setIcon("fa-pencil"));


                        if (mods.structure) {
                            mods._urls.push(iconButtonMaker()
                                .setURL(baseurl + "structure?q=" + mods.structure.id)
                                .setTitle("Search Structure")
                                .setIcon("fa-search"));
                        }

                        if (mods.protein && mods.protein.subunits) {
                            _.chain(mods.protein.subunits)
                                .map(function (su) {
                                    mods._urls.push(iconButtonMaker()
                                        .setURL(baseurl + "sequence?id=" + su.uuid)
                                        .setTitle("Sequence Subunit " + su.subunitIndex + " Search")
                                        .setIcon("fa-search"));
                                })
                                .value();
                        }

                        mods._urls.push(iconButtonMaker()
                            .setURL(baseurl + "substance/" + mods.uuid)
                            .setTitle("View Record")
                            .setIcon("fa-sign-in"));

                        return mods;
                    };

                    vf.fromFetcher = function (f) {
                        if (!f) return vf;
                        return vf.name(f.name).setFunction(function (s) {
                            var mods = vf._prepareSubstance(s);
                            return f.fetcher(mods);
                        });
                    };

                    vf.after = function (m) {
                        var vft = ViewFetcher().name(vf.name());
                        return vft.setFunction(function (s) {
                            return vf.make(s)
                                .andThen(function (r) {
                                    return m(r, vft);
                                });
                        });

                    };

                    return vf;
                };

                const kvclean = function (e, vf) {
                    var elm = "<div class='row'>" +
                        "<div ng-hide='hideTitles' class='col-md-12'>" + vf.name() + "</div>"
                        + "<div class='col-md-12'>" + e + "</div>";
                    return elm;
                };

                const fetchFetcher = function (vf) {
                    var fetcher = FetcherRegistry.getFetcher(vf);
                    if (fetcher) {
                        return ViewFetcher().name(vf)
                            .fromFetcher(fetcher)
                            .after(kvclean);
                    }
                    if (vf === "img") {
                        return ViewFetcher()
                            .fromFetcher(FetcherRegistry.getFetcher("UUID"))
                            .after(function (uu) {
                                return "<div><rendered id='" + uu + "'></rendered></div>";
                            });
                    }
                    if (vf === "iconButtons") {
                        return ViewFetcher()
                            .IDENTITY()
                            .name("TEST")
                            .after(function (s) {
                                scope.iconButtons = s._urls;
                                return "											<div class=\"col-md-12 text-center\">\n" +
                                    "											        <ul class=\"list-inline list-unstyled tools\">\n" +
                                    "											            <!-- basic icons -->\n" +
                                    "											            <li ng-repeat=\"u in iconButtons\">\n" +
                                    "											                <a href=\"{{u.url}}\" uib-tooltip=\"{{u.title}}\" target=\"_self\" aria-label=\"{{u.title}}\">\n" +
                                    "											                    <span class=\"sr-only\">\n" +
                                    "											                    {{u.title}}\n" +
                                    "											                    </span>\n" +
                                    "											                    <span class=\"fa {{u.icon}} fa-2x success\"></span>\n" +
                                    "											                </a>                \n" +
                                    "											            </li>\n" +
                                    "											        </ul>\n" +
                                    "											</div>";
                            });
                    }
                };
                //######################
                //HELPER FUNCTIONS END
                //######################

                scope.allViews = [];
                scope.views = [];

                scope.hideTitles = true;

                //calculate views based on specified views
                scope.views = _.chain(scope.substanceViews)
                    .map(fetchFetcher)
                    .value();

                scope.update = function () {
                    element.html("");
                    _.chain(scope.views)
                        .map(function (v) {
                            v.make(scope._substance)
                                .get(function (e) {
                                    element.append($compile(e)(scope));
                                });
                        })
                        .value();
                };

                if (!scope.substance) {
                    var uuid = scope.substanceuuid;
                    if (uuid) {
                        APIFetcher.fetch(uuid, scope.version)
                            .then(function (s) {
                                scope._substance = s;
                                scope.update();
                            });
                    }
                } else {
                    scope._substance = scope.substance;
                    scope.update();
                }
            },
            template: function (element, scope) {
                return "<div></div>";
            }
        }
    });
    //*****************************************************************
    //Views and General UI
    //*****************************************************************

    ginasApp.directive("treeView", function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                text: '@'
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
                    var link = baseurl + "substances?q=comments:%22" + _.join(_.slice(scope.link, 0, key + 1), '|') + "%22";
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

    ginasApp.directive('card', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                eid: '@',
                cardTitle: '@',
                count: '@',
                audit: '=',
                initcollapse: '=',

            },
            link: function (scope) {
                scope.scrollTo = scope.$parent.scrollTo;
                scope.getRange = scope.$parent.getRange;
                scope.redirectVersion = function () {
                    scope.$parent.redirectVersion(scope.versionNumber);
                };
            },
            template: function (element, scope) {
                var content = element[0].innerHTML;
                return [
                    '<div ng-cloak class="row detail-card info fade-ng-cloak ng-cloak" id="{{eid}}">',
                    "<button class=\"btn btn-primary label-offset det\" ng-class=\"(collapse)?'collapsed':''\" ng-init = \"collapse=initcollapse\" ng-click = \"collapse = !collapse\">",
                    "<i ng-show=\"collapse\" class=\"fa fa-caret-right\"></i>",
                    "<i ng-hide=\"collapse\" class=\"fa fa-caret-down\"></i>",
                    "{{cardTitle}}&nbsp;<span ng-if=\"count!=0\" class=\"badge\"> {{count}}</span></button>",
                    "<div class=\"col-md-12 table-responsive card-content\" uib-collapse = \"collapse\">",
                    content,
                    '</div>',
                    '</div>'
                ].join("\n");
            }
        };
    });

    ginasApp.directive('clickOutside', function ($document) {

        return {
            restrict: 'A',
            scope: {
                clickOutside: '&'
            },
            link: function (scope, el, attr) {

                $document.on('click', function (e) {
                    if (el !== e.target && !el[0].contains(e.target)) {
                        scope.$apply(function () {
                            scope.$eval(scope.clickOutside);
                        });
                    }
                });
            }
        }

    });

})();


//Routing intercept
//this is not strictly angular kosher
window.onhashchange = function (w) {
    var nhash = w.newURL.split("#")[1];
    if (nhash.indexOf(":") >= 0) {
        var wkeys = nhash.split(":");
        $("#" + wkeys[0]).val(wkeys[1]);
        $("#" + wkeys[0]).change();
        $(".temp-focus").removeClass("temp-focus");
        $("." + wkeys[2].replace("/", "_")).addClass("temp-focus");
    }
}

/* Add 'Show more...' to Description section on cards in list view
runs on page load */
$(function () {
    $('.list-item .text-block').each(function (event) {
        /* set the max content length after which a show more link will be added */
        var max_length = 250; //show two lines of description by default

        /* check for content length */
        if ($(this).html().length > max_length) {

            var short_content = $(this).html().substr(0, max_length); /* split the content in two parts */
            var long_content = $(this).html().substr(max_length);
            var ellipses = "..."

            /* alter the html to allow the read more functionality */
            $(this).html('<span class="less_text">' + short_content + '</span>' +
                '<span class="ellipses">' + ellipses + '</span>' +
                '<a href="#" class="show_more"></br>Show More</a>' +
                '<span class="more_text" style="display:none;">' + long_content + '</span>' +
                '<a href="#" class="show_less" style="display:none;"></br>Show Less</a>');

            /* find the a.read_more element within the new html and bind the following code to it */
            $(this).find('a.show_more').click(function (event) {
                /* prevent the a from changing the url */
                event.preventDefault();
                /* hide the show more button */
                $(this).hide();
                $(this).parents('.text-block').find('.ellipses').hide();
                /* show the .more_text span and show less link*/
                $(this).parents('.text-block').find('.more_text').show();
                $(this).parents('.text-block').find('.show_less').show();


            });

            $(this).find('a.show_less').click(function (event) {
                /* prevent the a from changing the url */
                event.preventDefault();
                /* hide the show less button */
                $(this).hide();
                /* hide the extra text */
                $(this).parents('.text-block').find('.more_text').hide();
                /* show the ellipses and the read more button */
                $(this).parents('.text-block').find('.ellipses').show();
                $(this).parents('.text-block').find('.show_more').show();
            });
        }
    });
});

/* remove info-popups if clicked anywhere outside */
$(function () {
    $('body').click(function (event) {
        //if any present
        if ($(".popover").length > 0 &&
            //and if does not have popover parent
            event.target.closest('.popover') === null &&
            //and if does not have info-popup parent
            event.target.closest('info-popup') === null) {
            //remove popover div
            $('.popover').remove();
        } else {
            return;
        }
    });
});

/* controls the "show smiles/inchi" block in the Structure card */
$(function () {
    $('.show-smiles-inchi').click(function (event) {
        event.preventDefault();
        $(this).hide();
        $("#smiles-inchi").show();
    });
});
/* controls the "hide smiles/inchi" block in the Structure card */
$(function () {
    $('.hide-smiles-inchi').click(function (event) {
        event.preventDefault();
        $('#smiles-inchi').hide();
        $(".show-smiles-inchi").show();
    });
});

/* controls the "show/hide references" table on the overview card*/
$(function () {
    $('.show-overview-audit').click(function (event) {
        event.preventDefault();
        $(this).hide();
        $("#overview-audit").show();
        $('.hide-overview-audit').show();
    });
});
$(function () {
    $('.hide-overview-audit').click(function (event) {
        event.preventDefault();
        $('#overview-audit').hide();
        $(".show-overview-audit").show();
        $(this).hide();
    });
});

/* format numbers: 1000 => 1,000 */
$(function () {
    $(".badge, .label-default").each(function () {
        /* if this is a filter on top of the page -- do not apply script
        since it should be shown as is (can be Year, etc.) */
        if ($(this).parents('.alert-dismissible').length) {
            return;
        }

        $(this).text(function (i, old) {

            //if not a valid number -- exit
            if (isNaN(old)) {
                return;
            }

            // i,old = index, old text
            // convert to string and trim
            old = old.toString().trim();
            var count_i = 0;
            var new_arr = [];

            // count from the back of the string-array
            for (i = old.length - 1; i > -1; i--) {
                if (count_i === 3) {
                    //add a comma to array 
                    //after each set of 3 elements
                    new_arr.push(",");
                    count_i = 0;
                }
                new_arr.push(old[i]);
                count_i += 1;
            }
            //reverse array, join, and return
            return new_arr.reverse().join("");
        });
    });

});


Number.isFinite = Number.isFinite || function (value) {
    return typeof value === 'number' && isFinite(value);
};

function restoreVersion(uuid, version) {
    if (confirm("Are you sure you'd like to restore version " + version + "?")) {
        var simpleModal = function (title) {
            var mod = {};
            var mid = ("mod-over" + Math.random()).replace(".", "");
            mod._title = title;
            mod._contents = "";
            mod.id = mid;
            mod._accept = function () {};
            mod._reject = function () {};
            mod.show = function () {
                var ofun = window["rawModDone"];
                if (!ofun) {
                    ofun = function () {};
                }
                window["rawModDone"] = function (b, t) {
                    ofun(b);
                    if (b === mod.id) {
                        $("#" + mod.id).remove();
                        if (t) {
                            mod._accept();
                        } else {
                            mod._reject();
                        }
                    }
                };
                //
                var raw=(function(){/*
                                <div id="{{mid}}" style="z-index:999999;position:fixed;top:0px;width:100%;height:100%;background: rgba(0, 0, 0, 0.6);">
                                   <div style="
                                       text-align: center;
                                       padding: 100px;
                                       max-width:600px;
                                       margin:auto;
                                   ">
                                      <div style="color:white;font-weight:bold;">
                                         {{title}}
                                      </div>
                                      <div>
                                         {{contents}}
                                      </div>
                                      <div>
                                         <button onclick="rawModDone('{{mid}}',false)">Cancel</button>
                                         <button onclick="rawModDone('{{mid}}',true)">OK</button>
                                      </div>
                                   </div>
            </div>*/}.toString()).substring(14).replace(/\*\/.*/g,"");
                raw = raw.replace(/\{\{mid\}\}/g, mod.id)
                    .replace("{{title}}", mod._title);
                raw = raw.replace("{{contents}}", mod._contents);

                document.body.appendChild($(raw)[0]);
                return mod;
            };
            mod.accept = function (cb) {
                mod._accept = cb;
                return mod;
            };
            mod.reject = function (cb) {
                mod._reject = cb;
                return mod;
            };
            mod.contents = function (cont) {
                mod._contents = cont;
                return mod;
            };
            mod.title = function (title) {
                mod._title = title;
                return mod;
            };
            mod.rawText = function (raw) {
                return mod.contents("<textarea style='margin:10px;min-width:300px;min-height:300px;'>" + raw + "</textarea>");
            }
            return mod;
        };

        var setLoading = function (b) {
            angular.element(document.body).scope().isGlobalLoading = b;
            angular.element(document.body).scope().$apply();
        };
        try {
            setLoading(true);
            var onError = function (e) {
                if (confirm("There was a problem restoring that version ... would you like to see the error details?")) {
                    simpleModal("Error restoring record. Here is some information on the error to share with a system admin / developers.")
                        .rawText(JSON.stringify(e, null, 2))
                        .show();
                }
            };

            return GGlob.SubstanceFinder
                .get(uuid)
                .andThen(function (s) {
                    return s.restoreVersion(version);
                })
                .get(function (e) {
                    if (!e || e.isError) {
                        onError(e);
                    } else {
                        alert("Version " + version + " restored");
                        location.href = baseurl + "substance/" + e.uuid;
                    }
                    setLoading(false);
                });
        } catch (e) {
            onError(e);
            setLoading(false);
        }
    }
}