(function () {
    var ginasApp = angular.module('ginas', ['ngMessages', 'ngResource', 'ui.bootstrap', 'ui.bootstrap.showErrors',
        'ui.bootstrap.datetimepicker', 'LocalStorageModule', 'ngTagsInput', 'xeditable', 'ui.select', 'jsonFormatter'
    ])
        .config(function (showErrorsConfigProvider, localStorageServiceProvider, $locationProvider) {
            showErrorsConfigProvider.showSuccess(true);
            localStorageServiceProvider
                .setPrefix('ginas');
            $locationProvider.html5Mode({
                enabled: true,
                hashPrefix: '!'
            });
        });

    ginasApp.filter('range', function () {
        return function (input, min, max) {
            console.log(input);
            min = parseInt(min); //Make string input int
            max = parseInt(max);
            for (var i = min; i < max; i++)
                input.push(i);
            return input;
        };
    });

    ginasApp.filter('arrays', function () {
        return function (input) {
            var obj = {};
            _.forIn(input, function (value, key) {
                if (_.isArray(value)) {
                    _.set(obj, key, value);
                }
            });
            return obj;
        };
    });

    ginasApp.factory('Substance', function () {
        var Substance = {};
        var substanceClass = window.location.search.split('=')[1];
        switch (substanceClass) {
            case "chemical":
                Substance.substanceClass = substanceClass;
                Substance.structure = {};
                Substance.moieties = [];
                break;
            case "protein":
                Substance.substanceClass = substanceClass;
                Substance.protein = {};
                Substance.protein.subunits = [];
                break;
            case "structurallyDiverse":
                Substance.substanceClass = substanceClass;
                Substance.structurallyDiverse = {};
                break;
            case "nucleicAcid":
                Substance.substanceClass = substanceClass;
                Substance.nucleicAcid = {};
                Substance.nucleicAcid.subunits = [];
                break;
            case "mixture":
                Substance.substanceClass = substanceClass;
                Substance.mixture = {};
                break;
            case "polymer":
                Substance.substanceClass = substanceClass;
                Substance.polymer = {};
                break;
            case "specifiedSubstanceG1":
                Substance.substanceClass = substanceClass;
                Substance.specifiedSubstance = {};
                break;
            default:
                Substance.substanceClass = substanceClass;
//                Substance.polymer = {};
                console.log('invalid substance class');
                break;
        }
        Substance.references = [];
        return Substance;
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
                        disp += start + "-" + end + ";";
                    }
                }
            }
            return disp;
        };
        utils.setSRUConnectivityDisplay = function (srus) {
            var rmap = utils.getAttachmentMapUnits(srus);
            for (var i in srus) {
                var disp = utils.sruConnectivityToDisplay(srus[i].attachmentMap, rmap);
                srus[i]._displayConnectivity = disp;
            }
        };

        return utils;
    });

    ginasApp.factory('isDuplicate', function ($q, substanceFactory) {
        return function dupCheck(modelValue) {
            console.log(modelValue);
            var deferred = $q.defer();
            substanceFactory.getSubstances(modelValue)
                .success(function (response) {
                    console.log(response);
                    if (response.count >= 1) {
                        deferred.reject();
                    } else {
                        deferred.resolve();
                    }
                });
            return deferred.promise;
        };
    });

    ginasApp.factory('substanceFactory', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances?filter=names.name='";
        var substanceFactory = {};
        substanceFactory.getSubstances = function (name) {
            return $http.get(url + name.toUpperCase() + "'", {cache: true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            });
        };
        return substanceFactory;
    }]);

    ginasApp.service('lookup', function ($http) {
        var options = {};
        var url = baseurl + "api/v1/vocabularies?filter=domain='";

        var lookup = {
            "names.type": "NAME_TYPE",
            "officialNames.type": "NAME_TYPE",
            "unofficialNames.type": "NAME_TYPE",
            "names.nameOrgs": "NAME_ORG",
            "officialNames.nameOrgs": "NAME_ORG",
            "names.nameJurisdiction": "JURISDICTION",
            "officialNames.nameJurisdiction": "JURISDICTION",
            "names.domains": "NAME_DOMAIN",
            "officialNames.domains": "NAME_DOMAIN",
            "names.languages": "LANGUAGE",
            "officialNames.languages": "LANGUAGE",
            "unofficialNames.languages": "LANGUAGE",
            "codes.codeSystem": "CODE_SYSTEM",
            "codes.type": "CODE_TYPE",
            "relationships.type": "RELATIONSHIP_TYPE",
            "relationships.interactionType": "INTERACTION_TYPE",
            "relationships.qualification": "QUALIFICATION",
            "references.docType": "DOCUMENT_TYPE",
            "protein.proteinType": "PROTEIN_TYPE",
            "protein.proteinSubtype": "PROTEIN_SUBTYPE",
            "protein.sequenceOrigin": "SEQUENCE_ORIGIN",
            "protein.sequenceType": "SEQUENCE_TYPE",
            "protein.modifications.structuralModifications.structuralModificationType": "STRUCTURAL_MODIFICATION_TYPE",
            "protein.modifications.structuralModifications.locationType": "LOCATION_TYPE",
            "protein.modifications.structuralModifications.extent": "EXTENT_TYPE"
        };


        lookup.load = function (field) {
            $http.get(url + field.toUpperCase() + "'", {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
                console.log(data);
                return data.content[0].terms;
            });
            // console.log(options);
        };


        lookup.getFromName = function (field, val) {
            var domain = lookup[field];
            if (typeof domain !== "undefined") {
                return {
                    "display": getDisplayFromCV(domain, val),
                    "value": val,
                    "domain": domain
                };
            }
            return null;
        };
        lookup.expandCVValueDisplay = function (domain, value) {
            var disp = getDisplayFromCV(domain, value);
            return {value: value, display: disp, domain: domain};
        };
        lookup.getList = function (domain) {
            return getCVListForDomain(domain);
        };
        return lookup;
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
                    console.log(response);
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

    ginasApp.factory('CVFields', function ($http, $q) {

        var lookup = {
            "stereoChemistry": "STEREOCHEMISTRY_TYPE",
            "names.type": "NAME_TYPE",
            "names.nameOrgs": "NAME_ORG",
            "names.nameJurisdiction": "JURISDICTION",
            "names.domains": "NAME_DOMAIN",
            "names.languages": "LANGUAGE",
            "codes.codeSystem": "CODE_SYSTEM",
            "codes.type": "CODE_TYPE",
            "relationships.type": "RELATIONSHIP_TYPE",
            "relationships.interactionType": "INTERACTION_TYPE",
            "relationships.qualification": "QUALIFICATION",
            "references.docType": "DOCUMENT_TYPE"
        };


        var url = baseurl + "api/v1/vocabularies?filter=domain='";
        var deferred = $q.defer();
        var CV = {
            lookuptable: lookup,

            load: function (field) {
                if (!_.has(CV, field)) {
                    var promise = $http.get(url + field.toUpperCase() + "'", {cache: true}, {
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (data) {
                        CV[field] = data.content[0].terms;
                        return CV[field];
                    });
                    return promise;
                }
            },

            search: function (field, query) {
                return _.chain(CV[field])
                    .filter(function (x) {
                        return !query || x.display.toLowerCase().indexOf(query.toLowerCase()) > -1;
                    })
                    .sortBy('display')
                    .value();
            },

            lookup: function (field, query) {
                return _.chain(CV[field])
                    .filter(function (x) {
                        return !query || x.value.toLowerCase().indexOf(query.toLowerCase()) > -1;
                    })
                    .sortBy('value')
                    .value();
            },

            retrieve: function (field) {
                if (field === 'NAME_TYPE') {
                    var temp = angular.copy(CV[field]);
                    temp = _.remove(temp, function (n) {
                        return n.value !== 'of';
                    });
                    return temp;
                } else {
                    return CV[field];
                }
            }
        };
        return CV;
    });

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

    ginasApp.controller("GinasController", function ($scope, $resource, $parse, $location, $compile, $modal, $http, $window, $anchorScroll, $q, localStorageService, Substance, UUID, CVFields, nameFinder, substanceSearch, substanceIDRetriever, lookup) {

        var ginasCtrl = this;
        $scope.ref = {};
        $scope.disulf = {};
        $scope.select = ['Substructure', 'Similarity'];
        $scope.type = 'Substructure';
        $scope.cutoff = 0.8;
        $scope.stage = true;

        $scope.canApprove = function(){
        	var lastEdit=$scope.substance.lastEditedBy;
        	if(!lastEdit)
        		return false; 
        	if($scope.substance.status==="approved"){
        		return false;
        	}
        	if(lastEdit === session.username){
        		return false;
        	}
        	return true;
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
        ///


        $scope.range = function (min) {
            var input = [];
            for (var i = 1; i <= min; i++) input.push(i);
            return input;
        };

        $scope.openSelector = function (path) {
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: baseurl + 'assets/templates/substanceSelector.html',
                controller: 'SubstanceSelectorInstanceController',
                size: 'lg'

            });

            modalInstance.result.then(function (selectedItem) {
                var subref = {};
                subref.refuuid = selectedItem.uuid;
                subref.refPname = selectedItem._name;
                subref.approvalID = selectedItem.approvalID;
                subref.substanceClass = "reference";
                _.set($scope, path, subref);
                console.log($scope);
            });
        };


        $scope.lookup = lookup;


        $scope.toFormSubstance = function (apiSub) {
            // console.log(apiSub);
            var officialNames = [];
            var unofficialNames = [];
            //first, flatten nameorgs, this is technically destructive
            //needs to be fixed.
            if (_.has(apiSub, 'names')) {
                _.forEach(apiSub.names, function (n) {
                    var temp = [];
                    if (n.nameOrgs.length > 0) {
                        _.forEach(n.nameOrgs, function (m) {
                            if (m.deprecated) {
                                apiSub.destructive = true;
                            }
                            temp.push(m.nameOrg);
                        });
                        n.nameOrgs = temp;
                    }
                    if (n.type === "of") {
                        officialNames.push(n);
                    } else {
                        unofficialNames.push(n);
                    }
                    delete apiSub.names;
                });
                _.set(apiSub, 'officialNames', officialNames);
                _.set(apiSub, 'unofficialNames', unofficialNames);
            }
            apiSub = $scope.expandCV(apiSub, "");
            return apiSub;
        };

        $scope.fromFormSubstance = function (formSub) {
            if (formSub.officialNames || formSub.unofficialNames) {
                _.forEach(formSub.officialNames, function (n) {
                    n.type = "of";
                });
            }
            if (_.isUndefined(formSub.officialNames)) {
                formSub.names = formSub.unofficialNames;
            } else if (_.isUndefined(formSub.unofficialNames)) {
                formSub.names = formSub.officialNames;
            } else {
                formSub.names = formSub.officialNames.concat(formSub.unofficialNames);
            }
            delete formSub.officialNames;
            delete formSub.unofficialNames;

            if (formSub.q) {
                delete formSub.q;
            }
            if (formSub.subref) {
                delete formSub.subref;
            }

            formSub = $scope.flattenCV(formSub);
            if (formSub.moieties) {
                _.forEach(formSub.moieties, function (m) {
                    m.id = UUID.newID();
                });
            }
            if (formSub.structure) {
                //apparently needs to be reset as well
                formSub.structure.id = UUID.newID();
            }
            console.log(formSub);
            return formSub;
        };

        $scope.scrollTo = function (prmElementToScrollTo) {
            $location.hash(prmElementToScrollTo);
            $anchorScroll();
        };

        $scope.expandCV = function (sub, path) {

            for (var v in sub) {

                var newpath = path;
                if (newpath.length >= 1) {
                    if (!angular.isArray(sub)) {
                        newpath += ".";
                    }
                }
                if (!angular.isArray(sub)) {
                    newpath = newpath + v;
                }
                var newcv = lookup.getFromName(newpath, sub[v]);
                if (angular.isArray(sub[v])) {
                    newcv = null;
                }

                if (newcv !== null) {
                    var w = getDisplayFromCV(newcv.domain, newcv.value);
                    newcv.display = w;
                    sub[v] = newcv;

                } else {
                    if (typeof sub[v] === "object") {
                        $scope.expandCV(sub[v], newpath);
                    }
                }
            }
            return sub;
        };

        $scope.flattenCV = function (sub) {
            //  console.log(sub);
            for (var v in sub) {
                if ($scope.isCV(sub[v])) {
                    sub[v] = sub[v].value;
                } else {
                    if (typeof sub[v] === "object") {
                        $scope.flattenCV(sub[v]);
                    }
                }
            }
            return sub;
        };

        $scope.isCV = function (ob) {
            if (typeof ob !== "object") return false;
            if (ob === null) return false;
            if (typeof ob.value !== "undefined") {
                if (typeof ob.display !== "undefined") {
                    return true;
                }
            }
            return false;
        };

        $scope.setLinkProperty = function (site, bridge, property) {
            _.set(site, property, true);
            site.bridge = bridge;
        };

        $scope.findSite = function (array, index) {
            var returnSite = {};
            _.forEach(array, function (n) {
                var temp = (_.find(n, 'residueIndex', index));
                if (typeof temp !== "undefined") {
                    returnSite = temp;
                }
            });
            return returnSite;
        };

        $scope.makeSite = function (obj) {
            var site = {};
            site.subunitIndex = obj.subunitIndex - 1 + 1;
            site.residueIndex = obj.residueIndex - 1 + 1;
            return site;
        };


        $scope.parseLink = function (obj, path) {
            var link = [];

            //PERSIST--add sites to link list
            var site = $scope.makeSite(obj);
            link.push(site);

            //DISPLAY--make array for bridge site
            var bridge = [obj.subunitIndexEnd, obj.residueIndexEnd];

            //DISPLAY-- find site, add link property and bridge
            var subunit = (_.pick($scope.substance.protein.subunits, 'index', obj.subunitIndex - 1)[0]);
            tempSite = $scope.findSite(subunit.display, obj.residueIndex - 1 + 1);
            $scope.setLinkProperty(tempSite, bridge);

            //DO IT AGAIN for ending index. can't really be done in a loop, can make and end site object and abstract these though
            var endSite = $scope.makeSite({
                'subunitIndex': obj.subunitIndexEnd - 1 + 1,
                'residueIndex': obj.residueIndexEnd - 1 + 1
            });

            link.push(endSite);
            var endBridge = [obj.subunitIndex, obj.residueIndex];
            subunit = (_.pick($scope.substance.protein.subunits, 'index', obj.subunitIndexEnd - 1)[0]);
            var tempEndSite = $scope.findSite(subunit.display, obj.residueIndexEnd - 1 + 1);

            //switch property if other link
            var prop = "disulfide";
            if (_.has(obj, "linkageType")) {
                prop = "otherLink";
            }

            //set display properties
            $scope.setLinkProperty(tempSite, bridge, prop);
            $scope.setLinkProperty(tempEndSite, endBridge, prop);

            return link;
        };


        $scope.parseGlycosylation = function (obj, path) {
            /*            if (!$scope.substance.protein.glycosylation.count) {
             $scope.substance.protein.glycosylation.count = 0;
             }*/
            var link = obj.link;
            var site = $scope.makeSite(obj);
            site.link = link;
            path = path + "." + link + "GlycosylationSites";
            console.log(path);

            //  $scope.substance.protein.glycosylation.count++;
            //DISPLAY-- find site, add glycosylation property
            var subunit = (_.pick($scope.substance.protein.subunits, 'index', obj.subunitIndex - 1)[0]);
            tempSite = $scope.findSite(subunit.display, obj.residueIndex - 1 + 1);
            tempSite.glycosylationSite = true;
            return site;
        };

        /*        $scope.cleanSequence = function (s) {
         return s.replace(/[^A-Za-z]/g, '');
         };*/
        $scope.getResidueAtSite = function (site) {
            var msub = $scope.getSubunitWithIndex(site.subunitIndex);
            if (msub === null)return null;
            return msub.sequence[site.residueIndex - 1];
        };
        $scope.getSubunitWithIndex = function (subIndex) {
            var subs = $scope.getSubunits();
            for (var i in subs) {
                if (subs[i].subunitIndex === subIndex) {
                    return subs[i];
                }
            }
            return null;
        };
        $scope.getSubunits = function () {
            var subs = [];
            if ($scope.substance.nucleicAcid) {
                subs = $scope.substance.nucleicAcid.subunits;
            } else if ($scope.substance.protein) {
                subs = $scope.substance.protein.subunits;
            }
            return subs;
        };

        $scope.defaultSave = function (obj, form, path, list, name) {
            $scope.$broadcast('show-errors-check-validity');
            /*            console.log(obj);
             console.log(form);
             console.log($scope);*/

            if (form.$valid) {
                if (_.has($scope.substance, path)) {
                    if (!list) {
                        _.set($scope.substance, path, obj);
                    } else {
                        var temp = _.get($scope.substance, path);
                        temp.push(obj);
                        _.set($scope.substance, path, temp);
                    }
                } else {
                    if (!list) {
                        _.set($scope.substance, path, obj);
                    } else {
                        var x = [];
                        x.push(angular.copy(obj));
                        _.set($scope.substance, path, x);
                    }
                }
                $scope[name] = {};
                $scope.reset(form);
                form.$setSubmitted(true);
                console.log($scope);
            } else {
                console.log(form);
                console.log("Invalid");
            }
        };

        $scope.siteDisplayListToSiteList = function (slist) {
            var toks = slist.split(";");
            var sites = [];
            for (var i in toks) {
                var l = toks[i];
                if (l === "")continue;
                var rng = l.split("-");
                if (rng.length > 1) {
                    var site1 = $scope.siteDisplayToSite(rng[0]);
                    var site2 = $scope.siteDisplayToSite(rng[1]);
                    if (site1.subunitIndex != site2.subunitIndex) {
                        throw "\"" + rng + "\" is not a valid shorthand for a site range. Must be between the same subunits.";
                    }
                    if (site2.residueIndex <= site1.residueIndex) {
                        throw "\"" + rng + "\" is not a valid shorthand for a site range. Second residue index must be greater than first.";
                    }
                    sites.push(site1);
                    for (var j = site1.residueIndex + 1; j < site2.residueIndex; j++) {
                        sites.push({
                            subunitIndex: site1.subunitIndex,
                            residueIndex: j
                        });
                    }
                    sites.push(site2);
                } else {
                    sites.push($scope.siteDisplayToSite(rng[0]));
                }
            }
            return sites;

        };

        $scope.addFields = function (obj, path) {
            if (!_.has($scope.substance, path)) {
                return obj;
            }
            var temp = _.get($scope.substance, [path]);
            console.log(temp);
            console.log(obj);
            _.forIn(obj, function (value, key) {
                temp[key] = value;
                console.log(value, key);
            });
            console.log(temp);
            return temp;
        };


        //Method for pushing temporary objects into the final message
        //Note: This was changed to use a full path for type.
        //That means that passing something like "nucleicAcid.type"
        //for type, will store the form object into that path inside
        //the substance, unless otherwise caught in the switch.
        //This simplifies some things.
        $scope.validate = function (objName, form, path, list) {
            console.log($scope);
            console.log(form);
            var obj = $scope[objName];
            console.log(obj);
            var v = path.split(".");
            var type = _.last(v);
            //  console.log(type);
            var subClass = ($scope.substance.substanceClass);
            switch (type) {
                case "sugars":
                case "linkages":
                case "structuralModifications":
                case "disulfideLinks":
                case "otherLinks":
                case "glycosites":
                    //console.log($scope.checkSites(obj.displaySites,$scope.substance.nucleicAcid.subunits,obj));
                    //if(true)return "test";
                    $scope.updateSiteList(obj);
                    // console.log(JSON.parse(JSON.stringify(obj)));
                    $scope.defaultSave(obj, form, path, list, objName);
                    break;
                case "subunits":

                    if (!obj.subunitIndex) {
                        var t = _.get($scope.substance, path);
                        if (t) {
                            obj.subunitIndex = t.length + 1;
                        } else {
                            obj.subunitIndex = 1;
                        }
                    } else {
                    }
                    obj.display = $scope.parseSubunit(obj.sequence, obj.subunitIndex);
                    if (obj._editType !== "edit") {
                        $scope.defaultSave(obj, form, path, list, objName);
                    }
                    obj._editType = "add";
                    break;
                case "protein":
                    //   var prot = $scope.addFields(obj, path);
                    $scope.defaultSave(prot, form, path, list, objName);
                    break;
                //case "disulfideLinks":
                //    var d = $scope.parseLink(obj, path);
                //    $scope.defaultSave(d, form, path, list, objName);
                //    break;
                //case "otherLinks":
                //    var ol = {};
                //    var otl = $scope.parseLink(obj, path);
                //    _.set(ol, "sites", otl);
                //    _.set(ol, "linkageType", obj.linkageType);
                //    $scope.defaultSave(ol, form, path, list, objName);
                //    break;
                case "glycosylation":
                    var g = $scope.parseGlycosylation(obj, path);
                    _.set($scope.substance, path + ".glycosylationType", obj.glycosylationType);
                    $scope.defaultSave(g, form, path + "." + obj.link + 'Glycosylation', list, objName);
                    break;
                /*                case "structurallyDiverse":
                 var diverse = $scope.addFields(obj, path);
                 $scope.defaultSave(diverse, form, path, list, objName);

                 break;*/
                default:
                    if (obj._editType !== "edit") {
                        $scope.defaultSave(obj, form, path, list, objName);
                        console.log($scope);
                    }
                    obj._editType = "add";
                    break;
            }
            //$scope[objName] = {};
        };

        $scope.toggle = function (el) {
            console.log(el);
            if (!el)return;
            if (_.has(el, "selected")) {
                el.selected = !el.selected;
            } else {
                el.selected = true;
            }
        };

        $scope.changeSelect = function (val) {
            console.log(val);
            val = !val;
            console.log(val);
        };

        $scope.remove = function (obj, field) {
            var index = Substance[field].indexOf(obj);
            Substance[field].splice(index, 1);
        };

        $scope.reset = function (form) {
            console.log(form);
            form.$setPristine();
            $scope.$broadcast('show-errors-reset');
        };

        $scope.selected = false;

        $scope.info = function (scope, element) {
            console.log($scope);
            console.log(scope);
            console.log(element);
            $scope.selected = !$scope.selected;
        };


        $scope.fetch = function ($query) {
            console.log($query);
            return substanceSearch.load($query);
            //return substanceSearch.search(field, $query);
        };


        $scope.submitSubstance = function () {
        	var r = confirm("Are you sure you'd like to submit this substance?");
			if (r != true) {
            	return;
			}
            var sub = angular.copy($scope.substance);
            sub = $scope.fromFormSubstance(sub);
            if (_.has(sub, 'update')) {
                $.ajax({
                    url: baseurl + 'api/v1/substances(' + sub.uuid + ')/_',
                    type: 'PUT',
                    beforeSend: function (request) {
                        request.setRequestHeader("Content-Type", "application/json");
                    },
                    data: JSON.stringify(sub),
                    success: function (data) {
                        alert('Load was performed.');
                    }
                });
            } else {
                $http.post(baseurl + 'register/submit', sub).success(function () {
                    console.log("success");
                    alert("submitted!");
                });
            }
        };
        $scope.approveSubstance = function () {
        	var sub = angular.copy($scope.substance);
            sub = $scope.fromFormSubstance(sub);
            var keyid=sub.uuid.substr(0,8);
            var r = confirm("Are you sure you want to approve this substance?");
			if (r == true) {
            	location.href=baseurl + "substance/" + keyid +"/approve";
			}
        };

        $scope.validateSubstance = function () {
            var sub = angular.copy($scope.substance);
            // console.log(angular.copy(sub));
            sub = $scope.fromFormSubstance(sub);
            $scope.errorsArray = [];
            //   console.log(sub);
            $http.post(baseurl + 'register/validate', sub).success(function (response) {
                var arr = [];
                for (var i in response) {
                    if (response[i].messageType != "INFO")
                        arr.push(response[i]);
                    if (response[i].messageType == "WARNING")
                        response[i].class = "alert-warning";
                    if (response[i].messageType == "ERROR")
                        response[i].class = "alert-danger";
                    if (response[i].messageType == "INFO")
                        response[i].class = "alert-info";
                    if (response[i].messageType == "SUCCESS")
                        response[i].class = "alert-success";


                }
                $scope.errorsArray = arr;
            });
        };

        $scope.checkDuplicateChemicalSubstance = function () {
            var sub = angular.copy($scope.substance);
            sub = $scope.fromFormSubstance(sub);
            $scope.structureErrorsArray = [];
            $http.post(baseurl + 'register/duplicateCheck', sub).success(function (response) {
                var arr = [];

                for (var i in response) {
                    console.log(response[i]);
                    if (response[i].messageType != "INFO")
                        arr.push(response[i]);
                    if (response[i].messageType == "WARNING")
                        response[i].class = "alert-warning";
                    if (response[i].messageType == "ERROR")
                        response[i].class = "alert-danger";
                    if (response[i].messageType == "INFO")
                        response[i].class = "alert-info";
                    if (response[i].messageType == "SUCCESS")
                        response[i].class = "alert-success";


                }
                $scope.structureErrorsArray = arr;
            });
        };

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

        $scope.siteDisplayToSite = function (site) {
            var subres = site.split("_");

            if (site.match(/^[0-9][0-9]*_[0-9][0-9]*$/g) === null) {
                throw "\"" + site + "\" is not a valid shorthand for a site. Must be of form \"{subunit}_{residue}\"";
            }

            return {
                subunitIndex: subres[0] - 0,
                residueIndex: subres[1] - 0
            };
        };
        $scope.sitesToDislaySites = function (sitest) {
            var sites = [];
            angular.extend(sites, sitest);
            sites.sort(function (site1, site2) {
                var d = site1.subunitIndex - site2.subunitIndex;
                if (d === 0) {
                    d = site1.residueIndex - site2.residueIndex;
                }
                return d;

            });
            var csub = 0;
            var cres = 0;
            var rres = 0;
            var finish = false;
            var disp = "";
            for (var i = 0; i < sites.length; i++) {

                var site = sites[i];
                if (site.subunitIndex == csub && site.residueIndex == cres)
                    continue;
                finish = false;
                if (site.subunitIndex == csub) {
                    if (site.residueIndex == cres + 1) {
                        if (rres === 0) {
                            rres = cres;
                        }
                    } else {
                        finish = true;
                    }
                } else {
                    finish = true;
                }
                if (finish && csub !== 0) {
                    if (rres !== 0) {
                        disp += csub + "_" + rres + "-" + csub + "_" + cres + ";";
                    } else {
                        disp += csub + "_" + cres + ";";
                    }
                    rres = 0;
                }
                csub = site.subunitIndex;
                cres = site.residueIndex;
            }
            if (sites.length > 0) {
                if (rres !== 0) {
                    disp += csub + "_" + rres + "-" + csub + "_" + cres;
                } else {
                    disp += csub + "_" + cres;
                }
            }
            return disp;
        };
        $scope.getAllSitesDisplay = function (link) {
            var sites = "";
            var subs = $scope.getSubunits();
            for (var i in subs) {
                var subunit = subs[i];
                if (sites !== "") {
                    sites += ";";
                }
                if (link) {
                    sites += subunit.subunitIndex + "_1-" + subunit.subunitIndex + "_" + (subunit.sequence.length - 1);
                } else {
                    sites += subunit.subunitIndex + "_1-" + subunit.subunitIndex + "_" + subunit.sequence.length;
                }
            }
            return sites;

        };

        $scope.getAllSites = function (link) {
            return $scope.siteDisplayListToSiteList($scope.getAllSitesDisplay(link));
        };
        $scope.getAllSitesMatching = function (regexFilter) {
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
        };

        $scope.getAllLeftoverSitesDisplay = function (link) {
            if (link) {
                return $scope.sitesToDislaySites($scope.getAllSitesWithoutLinkage());
            } else {
                return $scope.sitesToDislaySites($scope.getAllSitesWithoutSugar());
            }
        };
        $scope.getCysteineCount = function () {
            var allsites = $scope.getAllSitesMatching('C');
            return allsites.length;
        };
        $scope.getAllCysteinsWithoutLinkage = function () {
            var retsites = [];

            if ($scope.substance.protein) {

                var allsites = $scope.getAllSitesMatching('C');
                var asitesmap = {};
                var asites = [];
                if ($scope.substance.protein.disulfideLinks) {
                    for (var i = 0; i < $scope.substance.protein.disulfideLinks.length; i++) {
                        asites = asites.concat($scope.substance.protein.disulfideLinks[i].sites);
                    }
                }
                var site;
                for (var s = 0; s < asites.length; s++) {
                    site = asites[s];
                    asitesmap[site.subunitIndex + "_" + site.residueIndex] = true;
                }
                for (s = 0; s < allsites.length; s++) {
                    site = allsites[s];
                    if (!asitesmap[site.subunitIndex + "_" + site.residueIndex]) {
                        retsites.push(site);
                    }
                }
            } else {
                console.log("NO UNITS");
            }
            return retsites;


        };
        $scope.getAllSitesWithoutSugar = function () {
            var retsites = [];

            if ($scope.substance.nucleicAcid && $scope.substance.nucleicAcid.sugars) {

                var asites = [];
                for (var i = 0; i < $scope.substance.nucleicAcid.sugars.length; i++) {
                    asites = asites.concat($scope.substance.nucleicAcid.sugars[i].sites);
                }
                var allsites = $scope.getAllSites();
                var asitesmap = {};
                var site;
                for (var s in asites) {

                    site = asites[s];
                    asitesmap[site.subunitIndex + "_" + site.residueIndex] = true;
                }
                for (s in allsites) {
                    site = allsites[s];
                    if (!asitesmap[site.subunitIndex + "_" + site.residueIndex]) {
                        retsites.push(site);
                    }
                }
            } else {
                retsites = $scope.getAllSites();
            }
            return retsites;


        };
        $scope.getAllSitesWithoutLinkage = function () {
            var retsites = [];

            if ($scope.substance.nucleicAcid && $scope.substance.nucleicAcid.linkages) {

                var asites = [];
                for (var i = 0; i < $scope.substance.nucleicAcid.linkages.length; i++) {
                    asites = asites.concat($scope.substance.nucleicAcid.linkages[i].sites);
                }
                var allsites = $scope.getAllSites(true);
                var asitesmap = {};
                var site;
                for (var s in asites) {

                    site = asites[s];
                    asitesmap[site.subunitIndex + "_" + site.residueIndex] = true;
                }
                for (s in allsites) {
                    site = allsites[s];
                    if (!asitesmap[site.subunitIndex + "_" + site.residueIndex]) {
                        retsites.push(site);
                    }
                }
            } else {
                retsites = $scope.getAllSites(true);
            }
            return retsites;


        };

        $scope.isSiteSugarSpecified = function (site) {


        };

        $scope.getSiteDuplicates = function (sites1, sites2) {
            var retsites = [];
            var asitesmap = {};
            var site;
            for (var s in sites1) {
                site = sites1[s];
                asitesmap[site.subunitIndex + "_" + site.residueIndex] = true;
            }
            for (s in sites2) {
                site = sites2[s];
                if (asitesmap[site.subunitIndex + "_" + site.residueIndex]) {
                    retsites.push(site);
                }
            }
            return retsites;
        };

        $scope.getAllSugarSitesExcept = function (sugar) {
            var asites = [];
            if ($scope.substance.nucleicAcid.sugars)
                for (var i = 0; i < $scope.substance.nucleicAcid.sugars.length; i++) {
                    if ($scope.substance.nucleicAcid.sugars[i] != sugar) {
                        asites = asites.concat($scope.substance.nucleicAcid.sugars[i].sites);
                    }
                }
            return asites;
        };

        $scope.getAllLinkageSitesExcept = function (linkage) {
            var asites = [];
            if ($scope.substance.nucleicAcid.linkages)
                for (var i = 0; i < $scope.substance.nucleicAcid.linkages.length; i++) {
                    if ($scope.substance.nucleicAcid.linkages[i] != linkage) {
                        asites = asites.concat($scope.substance.nucleicAcid.linkages[i].sites);
                    }
                }
            return asites;
        };

        $scope.removeItem = function (list, item) {
            _.remove(list, function (someItem) {
                return item === someItem;
            });
        };


        //***************************BOILERPLATE SET EDIT*****************************************//
        //*************************REUSABLE****************************************//

        $scope.setEdit = function (obj, path) {
            if (obj) {
                $scope[path] = obj;
                $scope[path]._editType = "edit";
            } else {
                $scope[path] = null;
            }
        };

        $scope.setEditMonomer = function (mon) {
            if (mon) {
                $scope.component = mon;
                $scope.component._editType = "edit";
            } else {
                $scope.component = null;
            }

        };
        $scope.setEditSRU = function (sru) {
            if (sru) {
                $scope.srucomponent = sru;
                $scope.srucomponent._editType = "edit";
            } else {
                $scope.srucomponent = null;
            }

        };

        $scope.validateSites = function (dispSites, link) {
            var subunits = $scope.getSubunits();

            var t = $scope.checkSites(dispSites, subunits, link);
            console.log(t);
            return t;
        };

        $scope.checkSites = function (dispSites, subunits, link) {
            try {

                var sites = $scope.siteDisplayListToSiteList(dispSites);
                var dsites = [];

                if (link.linkage) {
                    dsites = $scope.getSiteDuplicates($scope.getAllLinkageSitesExcept(link), sites);
                } else if (link.sugar) {
                    dsites = $scope.getSiteDuplicates($scope.getAllSugarSitesExcept(link), sites);
                }
                if (dsites.length > 0) {
                    throw "Site(s) " + $scope.sitesToDislaySites(dsites) + " already specified!";
                }


                for (var s in sites) {
                    var site = sites[s];
                    if (link.linkage) {
                        var sited = {
                            subunitIndex: site.subunitIndex,
                            residueIndex: site.residueIndex + 1
                        };
                        site = sited;
                    }
                    if ($scope.getSiteResidue(subunits, site) === "") {
                        throw "Site " + sites[s].subunitIndex + "_" + sites[s].residueIndex + " does not exist in subunits";
                    }
                }
            } catch (e) {
                return e;
            }
        };

        $scope.updateSiteList = function (obj, sitelist, fromlist) {
            if (!sitelist) {
                if (!obj.sites) {
                    obj.sites = [];
                }
                sitelist = obj.sites;
            }
            if (!fromlist) {
                sitelist.length = 0;
            }
            if (obj._displaySites === $scope.sitesToDislaySites(sitelist)) {
                return;
            }

            try {
                if (!fromlist) {
                    $scope.replaceArray(sitelist, $scope.siteDisplayListToSiteList(obj._displaySites));
                }
                obj._displaySites = $scope.sitesToDislaySites(sitelist);
                if (!fromlist) {
                    $scope.replaceArray(sitelist, $scope.siteDisplayListToSiteList(obj._displaySites));
                }
                obj._uniqueResidues = [];
                obj._residueCounts = {};
                for (var i in sitelist) {
                    var r = $scope.getResidueAtSite(sitelist[i]);
                    if (!obj._residueCounts[r])
                        obj._residueCounts[r] = 0;
                    obj._residueCounts[r]++;
                }
                for (var k in obj._residueCounts) {
                    if (obj._residueCounts.hasOwnProperty(k)) {
                        obj._uniqueResidues.push(k);
                    }
                }
            } catch (e) {
                return e;
            }

        };
        $scope.pushArrayIntoArray = function (dest, src) {
            for (var i in src) {
                dest.push(src[i]);
            }
            return dest;
        };
        $scope.replaceArray = function (dest, src) {
            dest.length = 0;
            $scope.pushArrayIntoArray(dest, src);
            return dest;
        };

        $scope.submitpaster = function (input) {
            console.log(input);
            var sub = JSON.parse(input);
          //  $scope.substance = sub;
              $scope.substance = $scope.toFormSubstance(sub);
            console.log($scope);
        };

        $scope.bugSubmit = function (bugForm) {
            console.log(bugForm);
        };

        $scope.setEditId = function (editid) {
            console.log(editid);
            localStorageService.set('editID', editid);
        };

        if (typeof $window.loadjson !== "undefined" &&
            JSON.stringify($window.loadjson) !== "{}") {
            var sub = $scope.toFormSubstance($window.loadjson);
            $scope.substance = sub;
        } else {
            console.log($scope);
            //var edit = localStorageService.get('editID');
            //console.log(edit);
            //if (edit) {
            //    localStorageService.remove('structureid');
            //    substanceIDRetriever.getSubstance(edit).then(function (data) {
            //        var sub = $scope.toFormSubstance(data);
            //        $scope.substance = sub;
            //
            //      //This removes the substance, so reloading returns an empty form
            //      //  localStorageService.remove('editID');
            //    });
            //
            //} else {
            $scope.substance = Substance;
        }


        $scope.loadSubstances = function ($query) {
            return nameFinder.search($query);
        };

        $scope.createSubref = function (selectedItem, path) {
            console.log(selectedItem);
            console.log(path);
            var subref = {};
            subref.refuuid = selectedItem.uuid;
            subref.refPname = selectedItem._name;
            subref.approvalID = selectedItem.approvalID;
            subref.substanceClass = "reference";
            console.log(subref);
            _.set($scope.substance, path, subref);
            subref = {};
            console.log($scope);
        };

        $scope.addToArray = function (obj, array) {
            //array.push(obj);
            console.log($scope);
            // console.log(obj);
            if (!_.has($scope, array)) {
                $scope[array].push(obj);
            } else {
                //  obj = [obj]
                _.set($scope, array, obj);

            }
            console.log($scope);
        };

        //method for injecting a large structure image on the browse page//
        $scope.showLarge = function (id, divid) {
            console.log(id);
            console.log(divid);
            var result = document.getElementsByClassName(divid);
            var elementResult = angular.element(result);
            if ($scope.stage === true) {
                $scope.stage = false;
                childScope = $scope.$new();
                var compiledDirective = $compile('<rendered size="500" id =' + id + '></rendered>');
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

//sugarSites
    ginasApp.directive('naSites', function () {
        return {
            require: 'ngModel',
            link: function (scope, ele, attrs, c) {
                scope.$watch(attrs.ngModel, function () {
                    if (attrs.naSites.length < 2)return;


                    var repObj = JSON.parse(attrs.naSites);
                    var subunits = [];
                    if (scope.substance.nucleicAcid) {
                        subunits = scope.substance.nucleicAcid.subunits;
                    } else if (scope.substance.protein) {
                        subunits = scope.substance.protein.subunits;
                    } else {
                        return;
                    }
                    if (!c.$modelValue)
                        return;

                    var ret = scope.checkSites(c.$modelValue, subunits, repObj);

                    if (ret) {
                        c.$setValidity('siteInvalid', false);
                    } else {
                        c.$setValidity('siteInvalid', true);
                    }

                    //hack to have dynamic messages
                    if (!c.$errorMsg)c.$errorMsg = {};
                    if (c.$modelValue.length < 1) {
                        c.$errorMsg.naSites = "";
                    } else {
                        c.$errorMsg.naSites = ret;
                    }

                    return ret;

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

    ginasApp.directive('duplicate', function (isDuplicate) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
                ngModel.$asyncValidators.duplicate = isDuplicate;
            }
        };
    });

    ginasApp.directive('rendered', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                id: '@',
                size: '@'
            },
            template: '<img ng-src=\"' + baseurl + 'img/{{id}}.svg?size={{size||150}}\">'
        };
    });

    ginasApp.directive('submitButtons', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                objectName: '@submit',
                form: '=form',
                path: '@path',
                list: '@list'
            },
            templateUrl: baseurl + "assets/templates/submit-buttons.html",
            link: function (scope, element, attrs) {
                scope.validate = function () {
                    console.log(scope);
                    if (scope.list === "false" || scope.list === false) {
                        scope.$parent.validate(scope.objectName, scope.form, scope.path, false);
                    } else {
                        scope.$parent.validate(scope.objectName, scope.form, scope.path, true);
                    }
                };
                scope.reset = function () {
                    scope.$parent[scope.objectName] = null;
                    scope.$parent.reset(scope.form);
                };
            }
        };
    });

    ginasApp.directive('formSelector', function ($compile, $templateRequest) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                parent: '=',
                field: '@'
            },
            link: function (scope, element, attrs) {
                var formHolder;
                var childScope;
                var template;
                scope.stage = true;

                switch (attrs.type) {
                    case "amount":
                        if (attrs.mode == "edit") {
                            template = angular.element('<a ng-click ="toggleStage()"><amount value ="referenceobj" ></amount></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/amount-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);

                            });
                        }
                        formHolder = '<amount-form amount=referenceobj.amount></amount-form>';
                        break;
                    case "site":
/*                        if (attrs.mode == "edit") {
                            console.log(scope.referenceobj.sites);
                            template = angular.element('<a ng-click ="toggleStage()"><site-view value ="referenceobj.sites" ></site-view></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {*/
                            $templateRequest(baseurl + "assets/templates/site-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);

                            });
                       // }
                        formHolder = '<site-string-form referenceobj = referenceobj parent = parent field = field></site-string-form>';
                        break;
                    case "reference":
                        if (attrs.mode == "edit") {
                            $templateRequest(baseurl + "assets/templates/reference-selector-view.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        } else {
                            $templateRequest(baseurl + "assets/templates/reference-selector2.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<reference-form referenceobj = referenceobj parent = parent></reference-form>';
                        break;
                    case "parameter":
                        if (attrs.mode == "edit") {
                            template = angular.element('<a ng-click ="toggleStage()"><parameters parameters ="referenceobj"></parameters></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/parameter-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<parameter-form referenceobj = referenceobj field="field" parent = parent></parameter-form>';
                        break;
                    case "physicalParameter":
                        if (attrs.mode == "edit") {
                            template = angular.element('<a ng-click ="toggleStage()"><parameters parameters ="referenceobj.parameters"></parameters></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/parameter-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<physical-parameter-form referenceobj = referenceobj field = field parent = parent></physical-parameter-form>';
                        break;
                    case "access":
                        if (attrs.mode == "edit") {
                            template = angular.element('<a ng-click ="toggleStage()"><access value = referenceobj.access></access></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/access-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<access-form referenceobj = referenceobj parent = parent></access-form>';
                        break;
                    case "textbox":
                        if (attrs.mode == "edit") {
                            //this only works if the attribute is named "comments" will probably need to be addressed later
                            template = angular.element('<a ng-click ="toggleStage()"><comment value = "referenceobj.comments"></comment></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/comment-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<div ng-blur ="toggleStage()"><comment-form referenceobj = referenceobj parent = parent></comment-form></div>';
                        break;
                    case "upload":
                        if (attrs.mode == "edit") {
                            template = angular.element('<a ng-click ="toggleStage()"><comment value = "referenceobj.comments"></comment></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/upload-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<div ng-blur ="toggleStage()"><comment-form referenceobj = referenceobj parent = parent></comment-form></div>';
                        break;


                }


                scope.toggleStage = function () {
                    if (_.isUndefined(scope.referenceobj)) {
                        var x = {};
                        _.set(scope, 'referenceobj', x);
                    }
                    var result = document.getElementsByClassName(attrs.divid);
                    var elementResult = angular.element(result);
                    if (scope.stage === true) {
                        scope.stage = false;
                        childScope = scope.$new();
                        var compiledDirective = $compile(formHolder);
                        var directiveElement = compiledDirective(childScope);
                        elementResult.append(directiveElement);
                    } else {
                        childScope.$destroy();
                        elementResult.empty();
                        scope.stage = true;
                    }
                };
            }
        };
    });

    ginasApp.directive('parameterForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/parameter-form.html",
            link: function (scope, element, attrs) {
                console.log(scope);

                scope.validate = function () {
                    console.log(scope.referenceobj);
                    if (_.has(scope.referenceobj, 'parameters')) {
                        var temp = _.get(scope.referenceobj, 'parameters');
                        temp.push(scope.parameter);
                        _.set(scope.referenceobj, 'parameters', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(scope.parameter));
                        _.set(scope.referenceobj, 'parameters', x);
                    }
                    scope.parameter = {};
                    scope.parameterForm.$setPristine();
                };

                scope.deleteObj = function (obj, parent) {
                    parent.splice(_.indexOf(parent, obj), 1);
                };
            }
        };
    });

    ginasApp.directive('physicalParameterForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/physical-parameter-form.html",
            link: function (scope, element, attrs) {
                console.log(scope);

                scope.validate = function () {
                    console.log(scope.referenceobj);
                    if (_.has(scope.referenceobj, 'parameters')) {
                        var temp = _.get(scope.referenceobj, 'parameters');
                        temp.push(scope.physicalParameter);
                        _.set(scope.referenceobj, 'parameters', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(scope.physicalParameter));
                        _.set(scope.referenceobj, 'parameters', x);
                    }
                    scope.physicalParameter = {};
                    scope.physicalParameterForm.$setPristine();
                };

                scope.deleteObj = function (obj, parent) {
                    parent.splice(_.indexOf(parent, obj), 1);
                };
            }
        };
    });

    ginasApp.directive('accessForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/access-form.html",
            link: function (scope, element, attrs) {
                scope.validate = function () {
                    console.log(scope.referenceobj);
                    if (_.has(scope.referenceobj, 'access')) {
                        var temp = _.get(scope.referenceobj, 'access');
                        temp.push(scope.access);
                        _.set(scope.referenceobj, 'access', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(scope.access));
                        _.set(scope.referenceobj, 'access', x);
                    }
                    scope.access = {};
                    scope.accessForm.$setPristine();
                };

                scope.deleteObj = function (obj, parent) {
                    parent.splice(_.indexOf(parent, obj), 1);
                };
            }
        };
    });

    ginasApp.directive('commentForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/comment-form.html",
            link: function (scope, element, attrs) {
                console.log(scope);
            }
        };
    });

    ginasApp.directive('referenceForm', function (UUID) {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/reference-form.html",
            link: function (scope, element, attrs) {
                scope.validate = function () {
                    if (!_.isUndefined(scope.ref.citation)) {
                        _.set(scope.ref, "uuid", UUID.newID());
                        if (scope.ref.apply) {
                            scope.saveReference(scope.ref.uuid, scope.referenceobj);
                            scope.saveReference(angular.copy(scope.ref), scope.parent);
                        } else {
                            scope.saveReference(scope.ref, scope.parent);
                        }
                        scope.ref = {};
                        scope.ref.apply = true;
                        scope.refForm.$setPristine();
                    }
                };

                scope.saveReference = function (reference, parent) {
                    if (_.has(parent, 'references')) {
                        var temp = _.get(parent, 'references');
                        temp.push(reference);
                        _.set(parent, 'references', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(reference));
                        _.set(parent, 'references', x);
                    }
                };
            }
        };
    });

    ginasApp.directive('referenceFormOnly', function (UUID) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/reference-form-only.html",
            link: function (scope, element, attrs) {
                scope.validate = function () {
                    scope.saveReference(scope.ref, scope.parent);
                    scope.ref = {};
                    scope.ref.apply = true;
                    scope.refForm.$setPristine();
                };

                scope.saveReference = function (reference, parent) {
                    if (_.has(parent, 'references')) {
                        var temp = _.get(parent, 'references');
                        temp.push(reference);
                        _.set(parent, 'references', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(reference));
                        _.set(parent, 'references', x);
                    }
                };


                //this probably (definitely) needs to cascade down to all objects that use this reference

                scope.deleteObj = function (ref) {
                    console.log(scope);
                    scope.parent.references.splice(scope.parent.references.indexOf(ref), 1);
                };
            }
        };
    });

    ginasApp.directive('amountForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                amount: '=amount'
            },
            templateUrl: baseurl + "assets/templates/amount-form.html"
        };
    });

    ginasApp.directive('siteForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                sites: '=sites'
            },
            templateUrl: baseurl + "assets/templates/site-form.html"
        };
    });

    ginasApp.directive('siteStringForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                parent:'=',
                sites: '=',
                field: '='
            },
            link: function(scope, element, attrs){
                console.log(scope);

                scope.sitesToDislaySites = function (sitest) {
                    var sites = [];
                    angular.extend(sites, sitest);
                    sites.sort(function (site1, site2) {
                        var d = site1.subunitIndex - site2.subunitIndex;
                        if (d === 0) {
                            d = site1.residueIndex - site2.residueIndex;
                        }
                        return d;

                    });
                    var csub = 0;
                    var cres = 0;
                    var rres = 0;
                    var finish = false;
                    var disp = "";
                    for (var i = 0; i < sites.length; i++) {

                        var site = sites[i];
                        if (site.subunitIndex == csub && site.residueIndex == cres)
                            continue;
                        finish = false;
                        if (site.subunitIndex == csub) {
                            if (site.residueIndex == cres + 1) {
                                if (rres === 0) {
                                    rres = cres;
                                }
                            } else {
                                finish = true;
                            }
                        } else {
                            finish = true;
                        }
                        if (finish && csub !== 0) {
                            if (rres !== 0) {
                                disp += csub + "_" + rres + "-" + csub + "_" + cres + ";";
                            } else {
                                disp += csub + "_" + cres + ";";
                            }
                            rres = 0;
                        }
                        csub = site.subunitIndex;
                        cres = site.residueIndex;
                    }
                    if (sites.length > 0) {
                        if (rres !== 0) {
                            disp += csub + "_" + rres + "-" + csub + "_" + cres;
                        } else {
                            disp += csub + "_" + cres;
                        }
                    }
                    return disp;
                };

                scope.validate = function(){
console.log(scope.stage);
                };

                scope.print= function(){
                    //console.log(scope.siteDisplayListToSiteList(scope.referenceobj.display));
                    console.log("changing"+ scope.referenceobj.display);
/*                    console.log(scope.referenceobj.display);
                    console.log(scope.referenceobj.sites);*/

                };


    /*            if(scope.referenceobj.sites.length > 0) {
                    scope.referenceobj.display = scope.sitesToDislaySites(scope.referenceobj.sites);
                }*/
            },
            templateUrl: baseurl + "assets/templates/site-string-form.html"
        };
    });

    ginasApp.directive('amount', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            template: '<div><span class="amt">{{value.nonNumericValue}} {{value.average}} ({{value.low}} to {{value.high}}) {{value.units.display || value.units}}</span></div>'
        };
    });

    ginasApp.directive('siteView', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            link: function(scope){
       //         console.log(scope);
            },
            template: '<div><span>{{value[0].subunitIndex}}_{{value[0].residueIndex}}; {{value[1].subunitIndex}}_{{value[1].residueIndex}}</span></div>'
        };
    });

    ginasApp.directive('comment', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            template: '<div><span class="comment">{{value|limitTo:40}}...</span></div>'
        };
    });

    ginasApp.directive('access', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            template: '<div><i class="fa fa-lock fa-2x warning"  tooltip="Edit user access"></i><span ng-repeat = "access in value"><br>{{access.display}}</span></div>'
        };
    });

    ginasApp.directive('parameters', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                parameters: '='
            },
            template: '<div ng-repeat="p in parameters">{{p.name||p.parameterName}} <amount value="p.amount"></amount></div>'
        };
    });

    ginasApp.directive('referenceApply', function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                apply: '=ngModel',
                obj: '=',
                referenceobj: '=',
                parent: '='
            },
            link: function (scope, element, attrs) {
                var uuid;
                var index;
                var template;
                scope.apply = true;
                if (_.isUndefined(scope.referenceobj)) {
                    scope.referenceobj = {};
                }

                if (_.isUndefined(scope.referenceobj.references)) {
                    var x = [];
                    _.set(scope.referenceobj, 'references', x);
                }

                scope.isReferenced = function () {
                    return index >= 0;
                };

                switch (attrs.type) {
                    case "view":
                        template = angular.element('<div class = "text-center"><label for="apply" class="text-capitalize">Apply</label><br/><input type="checkbox" ng-model= apply placeholder="Apply" title="Apply" id="apply" checked/></div>');
                        element.append(template);
                        $compile(template)(scope);
                        break;
                    case "edit":
                        template = angular.element('<div class = "text-center"><input type="checkbox" ng-model="obj.apply" ng-click="updateReference();" placeholder="{{field}}" title="{{field}}" id="{{field}}s"/></div>');
                        element.append(template);
                        $compile(template)(scope);
                        uuid = scope.obj.uuid;
                        index = _.indexOf(scope.referenceobj.references, uuid);
                        scope.obj.apply = scope.isReferenced();

                        break;
                }

                scope.updateReference = function () {
                    console.log("update)");
                    console.log(scope);
                    index = _.indexOf(scope.referenceobj.references, uuid);
                    console.log(index);
                    if (index >= 0) {
                        scope.referenceobj.references.splice(index, 1);
                        scope.obj.apply = false;
                    } else {
                        scope.referenceobj.references.push(uuid);
                        scope.obj.apply = true;
                    }
                };

            }
        };
    });

    ginasApp.directive('aminoAcid', function ($compile) {
        var div = '<div class = "col-md-1">';
        var validTool = '<a href="#" class= "aminoAcid" tooltip="{{acid.subunitIndex}}-{{acid.residueIndex}} : {{acid.value}} ({{acid.type}}-{{acid.name}})">{{acid.value}}</a>';
        var invalidTool = '<a href="#" class= "invalidAA" tooltip-class="invalidTool" tooltip="INVALID">{{acid.value}}</a>';
        var space = '&nbsp;';
        var close = '</div>';
        getTemplate = function (aa) {
            var template = '';
            var index = aa.index;
            if (index % 10 === 0) {
                template = space;
            } else {
                var valid = aa.valid;
                if (valid) {
                    template = validTool;
                } else {
                    template = invalidTool;
                }
            }
            return template;
        };

        return {
            restrict: 'E',
            scope: {
                acid: '='
            },
            link: function (scope, element, attrs) {
                var aa = scope.acid;
                element.html(getTemplate(aa)).show();
                $compile(element.contents())(scope);
            }
        };

    });

    ginasApp.directive('structuralModificationForm', function (CVFields) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/structural-modifications-form.html",
            link: function (scope, element, attrs) {
                console.log(scope);

                scope.siteDisplayListToSiteList = function (slist) {
                    var toks = slist.split(";");
                    var sites = [];
                    for (var i in toks) {
                        var l = toks[i];
                        if (l === "")continue;
                        var rng = l.split("-");
                        if (rng.length > 1) {
                            var site1 = scope.siteDisplayToSite(rng[0]);
                            var site2 = scope.siteDisplayToSite(rng[1]);
                            if (site1.subunitIndex != site2.subunitIndex) {
                                throw "\"" + rng + "\" is not a valid shorthand for a site range. Must be between the same subunits.";
                            }
                            if (site2.residueIndex <= site1.residueIndex) {
                                throw "\"" + rng + "\" is not a valid shorthand for a site range. Second residue index must be greater than first.";
                            }
                            sites.push(site1);
                            for (var j = site1.residueIndex + 1; j < site2.residueIndex; j++) {
                                sites.push({
                                    subunitIndex: site1.subunitIndex,
                                    residueIndex: j
                                });
                            }
                            sites.push(site2);
                        } else {
                            sites.push(scope.siteDisplayToSite(rng[0]));
                        }
                    }
                    console.log(sites);
                    return sites;

                };



                scope.siteDisplayToSite = function (site) {
                    var subres = site.split("_");

                    if (site.match(/^[0-9][0-9]*_[0-9][0-9]*$/g) === null) {
                        throw "\"" + site + "\" is not a valid shorthand for a site. Must be of form \"{subunit}_{residue}\"";
                    }

                    return {
                        subunitIndex: subres[0] - 0,
                        residueIndex: subres[1] - 0
                    };
                };

               // scope.mod.display = scope.siteDisplayListToSiteList(scope.mod.sites) || "";

                scope.validate = function () {
                    scope.parent.protein.modifications.structuralModifications.push(scope.mod);
                    scope.mod = {};
                    scope.strucModForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    console.log(scope);
                    scope.parentprotein.modifications.structuralModifications.splice(scope.parentprotein.modifications.structuralModificationsindexOf(obj), 1);
                };
            }
        };
    });

    ginasApp.directive('physicalModificationForm', function (CVFields) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/physical-modification-form.html",
            link: function (scope, element, attrs) {
                console.log(scope);

                scope.validate = function () {
                    scope.parent.protein.modifications.physicalModifications.push(scope.physicalModification);
                    scope.physicalModification = {};
                    scope.physicalModForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    console.log(scope);
                    scope.parentprotein.modifications.physicalModifications.splice(scope.parentprotein.modifications.physicalModificationsindexOf(obj), 1);
                };
            }
        };
    });


    ginasApp.directive('subunitForm', function (CVFields) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                residues: '='
            },
            templateUrl: baseurl + "assets/templates/subunit-form.html",
            link: function (scope, element, attrs) {
                console.log(scope);

                scope.validate = function () {
                    scope.subunit.subunitIndex = scope.parent[scope.parent.substanceClass].subunits.length + 1;
                    scope.parent[scope.parent.substanceClass].subunits.push(scope.subunit);
                    scope.subunit = {};
                    scope.subunitForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    console.log(scope);
                    scope.parent[scope.parent.substanceClass].subunits.splice(scope.parent[scope.parent.substanceClass].subunits.indexOf(obj), 1);
                };
            }
        };
    });

    ginasApp.directive('subunit', function (CVFields) {

        return {
            restrict: 'E',
            scope: {
                parent: '=',
                obj: '=',
                residues: '='
            },
            link: function (scope, element, attrs) {
                scope.edit = false;

                scope.aaCheck = function (aa) {
                    var invalid = ['B', 'J', 'O', 'U', 'X', 'Z'];
                    return !(/^[a-zA-Z]*$/.test(aa) == false || (_.indexOf(invalid, aa.toUpperCase()) >= 0));
                };

                scope.getType = function (aa) {
                    if (aa == aa.toLowerCase()) {
                        return 'D';
                    }
                    else {
                        return 'L';
                    }
                };

                scope.parseSubunit = function () {
                    var display = [];
                    _.forEach(scope.obj.sequence, function (aa) {
                        var obj = {};
                        obj.value = aa;
                        obj.valid = scope.aaCheck(aa);
                        if (obj.valid) {
                            if (scope.obj.subunitIndex) {
                                obj.subunitIndex = scope.obj.subunitIndex;
                            } else {
                                obj.subunitIndex = scope.obj.index;
                            }
                            obj.residueIndex = _.indexOf(scope.obj.sequence, aa) - 1 + 2;
                            obj.name = (_.find(scope.residues, 'value', aa)).display;
                            if (scope.parent.substanceClass === 'protein') {
                                obj.type = scope.getType(aa);
                            }
                            if (aa.toUpperCase() == 'C') {
                                obj.cysteine = true;
                            }
                        }
                        display.push(obj);
                    });
                    this.display = display;
                    display = _.chunk(display, 10);
                    scope.subunitDisplay = display;
                };

//******************************************************************this needs a check to delete the subunit if cleaning the subunit results in an empty string
                scope.cleanSequence = function () {
                    scope.obj.sequence = _.filter(scope.obj.sequence, function (aa) {
                        return scope.aaCheck(aa);
                    }).toString().replace(/,/g, '');
                    scope.parseSubunit();
                };

                if (scope.parent.substanceClass === 'protein') {
                    CVFields.load("AMINO_ACID_RESIDUES").then(function (data) {
                        scope.residues = CVFields.retrieve("AMINO_ACID_RESIDUES");
                        scope.parseSubunit();
                    });
                } else {
                    CVFields.load("NUCLEIC_ACID_BASES").then(function (data) {
                        scope.residues = CVFields.retrieve("NUCLEIC_ACID_BASES");
                        scope.parseSubunit();
                    });
                }
            },
            templateUrl: baseurl + "assets/templates/subunit.html"
        };
    });

    ginasApp.directive('substanceChooserSelector', function ($templateRequest, $compile, nameFinder) {
        return {
            // templateUrl: baseurl + 'assets/templates/substance-select.html',
            replace: true,
            restrict: 'E',
            scope: {
                subref: '=ngModel',
                formname: '=',
                field: '@',
                label: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                var formHolder;
                var childScope;
                var template;
                scope.stage = true;
                switch (attrs.type) {
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
                        formHolder = '<substance-search-form subref = "subref"></substance-search-form>';
                        element.append(formHolder);
                        break;
                }

                scope.toggleStage = function () {
                    if (_.isUndefined(scope.subref)) {
                        var x = {};
                        _.set(scope, 'subref', x);
                    }
                    var result = document.getElementsByClassName(attrs.formname);
                    var elementResult = angular.element(result);
                    if (scope.stage === true) {
                        scope.stage = false;
                        childScope = scope.$new();
                        var compiledDirective = $compile(formHolder);
                        var directiveElement = compiledDirective(childScope);
                        elementResult.append(directiveElement);
                    } else {
                        childScope.$destroy();
                        elementResult.empty();
                        scope.stage = true;
                    }
                };
            }
        };
    });

    ginasApp.directive('substanceChooserViewEdit', function (nameFinder) {
        return {
            templateUrl: baseurl + 'assets/templates/substancechooser-view-edit.html',
            replace: true,
            restrict: 'E',
            scope: {
                obj: '=',
                field: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                scope.loadSubstances = function ($query) {
                    return nameFinder.search($query);
                };

                scope.createSubref = function (selectedItem) {
                    var subref = {};
                    subref.refuuid = selectedItem.uuid;
                    subref.refPname = selectedItem._name;
                    subref.approvalID = selectedItem.approvalID;
                    subref.substanceClass = "reference";
                    scope.obj[scope.field] = angular.copy(subref);
                    scope.diverse = [];
                };

                scope.editing = function (obj) {
                    if (_.has(obj, '_editing')) {
                        obj._editing = !obj._editing;
                    } else {
                        _.set(obj, '_editing', true);
                    }
                };

            }
        };
    });

    ginasApp.directive('substanceSearchForm', function ($http) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                subref: '='
            },
            templateUrl: baseurl + 'assets/templates/substanceSelector.html',
            link: function (scope, element, attrs) {
                scope.results = {};
                scope.searching = false;

                scope.top = 8;
                scope.testb = 0;

                scope.createSubref = function (selectedItem) {
                    console.log(selectedItem);
                    var temp = {};
                    temp.refuuid = selectedItem.uuid;
                    temp.refPname = selectedItem._name;
                    temp.approvalID = selectedItem.approvalID;
                    temp.substanceClass = "reference";
                    console.log(temp);
                    scope.subref = angular.copy(temp);
                    console.log(scope);

                };

                scope.fetch = function (term, skip) {
                    var url = baseurl + "api/v1/substances/search?q=" +
                        term + "*&top=" + scope.top + "&skip=" + skip;
                    var responsePromise = $http.get(url, {cache: true});

                    responsePromise.success(function (data, status, headers, config) {
                        console.log(data);
                        scope.searching = false;
                        scope.results = data;
                    });

                    responsePromise.error(function (data, status, headers, config) {
                        scope.searching = false;
                    });
                };

                scope.search = function () {
                    scope.searching = true;
                    scope.fetch(scope.term, 0);
                };


                scope.nextPage = function () {
                    scope.fetch(scope.term, scope.results.skip + scope.results.top);
                };
                scope.prevPage = function () {
                    scope.fetch(scope.term, scope.results.skip - scope.results.top);
                };

            }
        };
    });

    ginasApp.directive('substanceView', function () {
        return {
            replace: true,
            restrict: 'E',
            scope: {
                subref: '='
            },
            template: '<div><rendered id = {{subref.refuuid}}></rendered><br/><code>{{subref.refPname}}</code></div>'
        };
    });

    ginasApp.directive('substanceTypeahead', function (nameFinder) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/substance-typeahead.html",
            replace: true,
            scope: {
                subref: '=',
                field: '@'
            },
            link: function (scope, element, attrs) {

                scope.createSubref = function (selectedItem) {
                    console.log(selectedItem);
                    var temp = {};
                    temp.refuuid = selectedItem.uuid;
                    temp.refPname = selectedItem._name;
                    temp.approvalID = selectedItem.approvalID;
                    temp.substanceClass = "reference";
                    console.log(temp);
                    scope.subref = angular.copy(temp);
                    console.log(scope);

                };

                scope.loadSubstances = function ($query) {
                    var results = nameFinder.search($query);
                    console.log(results);
                    return results;
                };
            }
        };
    });

    ginasApp.directive('substanceChooser', function ($modal) {
        return {
            restrict: 'E',
            require: "ngModel",
            scope: {
                mymodel: '=ngModel',
                lite: '=lite'
            },
            templateUrl: baseurl + "assets/templates/substanceSelectorElement.html",
            link: function (scope) {
                /*
                 console.log(scope);
                 */
                scope.openSelector = function (parentRef, instanceName, test) {
                    //console.log(test);
                    var modalInstance = $modal.open({
                        animation: true,
                        templateUrl: baseurl + 'assets/templates/substanceSelector.html',
                        controller: 'SubstanceSelectorInstanceController',
                        size: 'lg'

                    });

                    modalInstance.result.then(function (selectedItem) {

                        //if(!parentRef[instanceName])parentRef[instanceName]={};
                        var oref = {};
                        oref.refuuid = selectedItem.uuid;
                        oref.refPname = selectedItem._name;
                        oref.approvalID = selectedItem.approvalID;
                        oref.substanceClass = "reference";
                        scope.mymodel = oref;
                        //_.set($scope, path, subref);
                    });
                };
            }
        };
    });

    ginasApp.directive('siteSelector', function () {
        return {
            restrict: 'E',
            scope: {
                siteContainer: '=ngModel',
                siteList: '=',
                displayType: '@',
                residueRegex: '@',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/site-selector.html",
            link: function (scope, element, attrs, parentCtrl) {
                //console.log(scope);
                scope.validateSites = scope.$parent.validateSites;
                scope.updateSiteList = scope.$parent.updateSiteList;
                scope.subunits = parent.subunits;
                scope.range = function (min) {
                    var input = [];
                    for (var i = 1; i <= min; i++) input.push(i);
                    return input;
                };
                scope.edit = false;
                scope.validResidues = function (su) {
                    if (!su)return [];
                    var list = [];
                    if (scope.residueRegex) {
                        var re = new RegExp(scope.residueRegex, 'ig');
                        var match;
                        //while ((match = re.exec(scope.parent.subunits[su - 1].sequence)) !== null) {
                        //    list.push(match.index + 1);
                        //}
                        return list;
                    } else {
                        return scope.range(scope.subunits[su - 1].sequence.length);
                    }
                };
                scope.size = 12;
                if (attrs.size) {
                    scope.size = attrs.size;
                }
                scope.$watch(
                    function watchSelected() {
                        return scope.siteContainer;
                    },
                    function updateSelected(value) {
                        if (value) {
                            if (!scope.siteList) {
                                scope.siteList = scope.siteContainer.sites;
                            }
                        }
                    }
                );
                scope.$watch(
                    function watchSelected() {
                        return JSON.stringify(scope.siteList);
                    },
                    function updateSelected(value, ovalue) {
                        if (value && ovalue) {
                            scope.updateSiteList(scope.siteContainer, scope.siteList, true);
                        }
                    }
                );
            }
        };
    });

    //Ok, this needs to be re-evaluated a bit.
    //Right now, it always round trips, but that doesn't always make sense.
    ginasApp.directive('sketcher', function ($http, $timeout, localStorageService, Substance, lookup, polymerUtils) {
        return {
            restrict: 'E',
            require: "ngModel",
            scope: {
                formsubstance: '=structure'
            },
            template: "<div id='sketcherForm' dataformat='molfile' ondatachange='setMol(this)'></div>",
            link: function (scope, element, attrs, ngModelCtrl) {

                sketcher = new JSDraw("sketcherForm");
                var url = window.strucUrl;
                var structureid = (localStorageService.get('structureid') || false);
                if (localStorageService.get('editID'))
                    structureid = false;
                var lastmol = "";
                var ignorechange = false;
                window.setMol = function (sk) {
                    if (ignorechange)return;
                    //console.log(scope);

                    var mol = sk.getMolfile();
                    if (lastmol === mol)return;
                    $http({
                        method: 'POST',
                        url: url,
                        data: mol,
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (data) {
                        lastmol = data.structure.molfile;
                        if (scope.formsubstance === null || typeof scope.formsubstance === "undefined") {
                            scope.formsubstance = {};
                        }
                        console.log("Type:" + attrs.type);
                        if (attrs.type === "structure") {
                            scope.formsubstance = data.structure;
                        } else if (attrs.type === "polymer") {
                            scope.formsubstance.idealizedStructure = data.structure;
                            for (var i in data.structuralUnits) {
                                data.structuralUnits[i].type = lookup.expandCVValueDisplay("POLYMER_SRU_TYPE", data.structuralUnits[i].type);
                            }
                            polymerUtils.setSRUConnectivityDisplay(data.structuralUnits);
                            scope.formsubstance.structuralUnits = data.structuralUnits;

                        } else {
                            scope.formsubstance.structure = data.structure;
                            scope.formsubstance.moieties = data.moieties;
                            for (var j = 0; j < data.moieties.length; j++) {
                                data.moieties[j]._id = scope.uuid();
                            }
                            scope.formsubstance.q = data.structure.smiles;
                        }
                        console.log(scope);
                    });
                };

                scope.uuid = function uuid() {
                    function s4() {
                        return Math.floor((1 + Math.random()) * 0x10000)
                            .toString(16)
                            .substring(1);
                    }

                    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                        s4() + '-' + s4() + s4() + s4();
                };

                scope.$watch(function (scope) {
                    if (typeof scope.formsubstance == "undefined") {
                        return "undefined";
                    }
                    if (typeof scope.formsubstance.structure == "undefined") {
                        return "undefined";
                    }
                    return scope.formsubstance.structure.molfile;

                }, function (value) {
                    if (lastmol !== value) {

                        ignorechange = true;
                        sketcher.setMolfile(value);
                        ignorechange = false;
                        lastmol = sketcher.getMolfile();
                    }
                });
                if (structureid) {
                    console.log("There is an id, it's:" + structureid);
                    $http({
                        method: 'GET',
                        url: baseurl + 'api/v1/structures/' + structureid
                    }).success(function (data) {
                        console.log(data);
                        console.log("fetched");
                        lastmol = data.molfile;
                        sketcher.setMolfile(data.molfile);
                        scope.formsubstance.q = data.smiles;
                        console.log(Substance);
                        localStorageService.remove('structureid');
                    });
                }

            }
        };
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
                html += '<input type="checkbox"';
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

    ginasApp.directive('exportButton', function () {
        return {
            restrict: 'E',
            scope: {
                structureid: '=',
                format: '='
            },
            template: function () {
                return '<button type="button" class="btn btn-primary" aria-label ="export molfile" structureid=structureid format=format export><i class="fa fa-external-link chem-button"></i></button>';
            }
        };
    });

    //selector for which button to show, and the associalted modal window
    ginasApp.directive('modalButton', function ($compile, $templateRequest) {
        return {
            restrict: 'E',
            scope: {
                type: '='
            },

//            <export-button structureid ="'@chem.structure.id'" ></export-button></li>

            link: function (scope, element, attrs, ngModel) {
                var modalWindow;
                var childScope;
                var template;
                scope.stage = true;
                switch (attrs.type) {
                    case "upload":
                        template = angular.element(' <a href = "#" aria-label="Export" structureid=structureid format=format export><i class="fa fa-upload fa-2x"></i></a>');
                        element.append(template);
                        $compile(template)(scope);
                        break;
                    case "import":
                        template = angular.element(' <a href = "#" aria-label="Import" structureid=structureid format=format export><i class="fa fa-clipboard fa-2x success"></i></a>');
                        element.append(template);
                        $compile(template)(scope);
                        break;
                    case "export":
                        template = angular.element(' <a href = "#" aria-label="Export" structureid=structureid format=format export><i class="fa fa-external-link fa-2x success"></i></a>');
                        element.append(template);
                        $compile(template)(scope);
                        $templateRequest(baseurl + "assets/templates/molexport.html").then(function (html) {
                            modalWindow = angular.element(html);
                            //  modalWindow = '<substance-search-form subref = "subref"></substance-search-form>';
                            element.append(modalWindow);

                        });
                        break;
                }
                scope.openModal = function () {
                    console.log('clicked');
                    modalWindow.modal('show');
                }
            }
        }
    });

    ginasApp.directive('errorWindow', function () {
        return {
            restrict: 'E',
            scope: {
                error: '='
            },
            templateUrl: baseurl + "assets/templates/errorwindow.html"
        };
    });

    ginasApp.directive('export', function ($http) {
        return function (scope, element, attrs) {
            element.bind("click", function () {
                var modal = angular.element(document.getElementById('export-mol'));
                var format = scope.format;
                if (!format) {
                    format = "sdf";
                }
                $http({
                    method: 'GET',
                    url: baseurl + 'export/' + scope.structureid + '.' + format,
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    var warnHead = response.headers("EXPORT-WARNINGS").split("___")[0];
                    console.log(warnHead);
                    var warnings = JSON.parse(warnHead);

                    modal.find('#inputExport').text(response.data);
                    if (warnings.length > 0) {
                        var html = "<div class=\"alert alert-danger alert-dismissible\" role=\"alert\">\n" +
                            "                        <button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"> <span aria-hidden=\"true\">&times;</span>\n" +
                            "                        </button>\n" +
                            "                        <span><h4 class=\"warntype\"></h4><span class=\"message\">" + warnings[0].message + "</span></span>";
                        modal.find('.warn').html(html);
                    } else {
                        modal.find('.warn').html("");
                    }

                    modal.modal('show');
                }, function (response) {
                    alert("ERROR exporting data");
                });
            });
        };
    });
    ginasApp.service("molFetch", function ($http) {


    });

    ginasApp.directive('molExport', function ($http) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/molexport.html"
        };
    });

    ginasApp.directive('deleteButton', function () {
        return {
            restrict: 'E',
            template: '<a ng-click="deleteObj()"><i class="fa fa-times fa-2x danger"></i></a>',
            link: function (scope, element, attrs) {
                //scope.path = attrs.path;
                scope.deleteObj = function () {
                    console.log(scope);
                    if (scope.parent) {
                        console.log(scope.parent);
                        console.log(attrs.path);
                        var arr = _.get(scope.parent, attrs.path);
                        console.log(arr);
                        arr.splice(arr.indexOf(scope.obj), 1);
                    } else {
                        scope.substance[attrs.path].splice(scope.substance[attrs.path].indexOf(scope.obj), 1);
                    }
                };
            }
        };
    });

    ginasApp.directive('textInput', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/text-input.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                label: '@'
            }
        };
    });

    ginasApp.directive('textViewEdit', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/text-view-edit.html",
            replace: true,
            scope: {
                obj: '=',
                field: '@',
                label: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                scope.edit = false;
                scope.editing = function () {
                    scope.edit = !scope.edit;
                };
            }
        };
    });

    ginasApp.directive('textBox', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/text-box.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@'
            }
        };
    });

    ginasApp.directive('textBoxViewEdit', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/text-box-view-edit.html",
            replace: true,
            scope: {
                obj: '=obj',
                field: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                scope.editing = function (obj) {
                    if (_.has(obj, '_editing')) {
                        obj._editing = !obj._editing;
                    } else {
                        _.set(obj, '_editing', true);
                    }
                };
            }
        };
    });

    ginasApp.directive('datePicker', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/date-picker.html",
            require: '^ngModel',
            replace: true,
            scope: {
                object: '=ngModel',
                field: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                //date picker
                scope.status = {
                    opened: false
                };

                scope.open = function ($event) {
                    scope.status.opened = true;
                };
            }
        };
    });

    ginasApp.directive('datePickerViewEdit', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/date-picker-view-edit.html",
            replace: true,
            scope: {
                obj: '=obj',
                field: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                //date picker
                scope.status = {
                    opened: false
                };

                scope.open = function ($event) {
                    scope.status.opened = true;
                };
            }
        };
    });

    ginasApp.directive('dropdownSelect', function (CVFields) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/dropdown-select.html",
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                CVFields.load(attrs.cv);

                scope.getValues = function (field) {
                    return CVFields.retrieve(attrs.cv);
                };
            }
        };
    });

    ginasApp.directive('dropdownViewEdit', function (CVFields) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/dropdown-view-edit.html",
            replace: true,
            scope: {
                formname: '=',
                obj: '=',
                field: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                CVFields.load(attrs.cv);

                scope.getValues = function () {
                    return CVFields.retrieve(attrs.cv);
                };

                scope.editing = function (obj) {
                    if (_.has(obj, '_editing')) {
                        obj._editing = !obj._editing;
                    } else {
                        _.set(obj, '_editing', true);
                    }
                };
            }
        };
    });

    ginasApp.directive('multiSelect', function (CVFields) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/multi-select.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                cv: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                CVFields.load(scope.cv);
                scope.loadItems = function (cv, $query) {
                    return CVFields.search(cv, $query);
                };
            }
        };
    });

    ginasApp.directive('multiViewEdit', function (CVFields) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/multi-view-edit.html",
            replace: true,
            scope: {
                obj: '=',
                field: '@',
                cv: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                CVFields.load(scope.cv);

                scope.loadItems = function (cv, $query) {
                    return CVFields.search(cv, $query);
                };

                scope.editing = function (obj) {
                    if (_.has(obj, '_editing')) {
                        obj._editing = !obj._editing;
                    } else {
                        _.set(obj, '_editing', true);
                    }
                };
            }
        };
    });

    ginasApp.directive('checkBoxViewEdit', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/check-box-view-edit.html",
            replace: true,
            scope: {
                obj: '=',
                field: '@'
            },
            link: function (scope, element, attrs) {
                scope.editing = function (obj, field) {
                    _.set(obj, '_editing' + field, true);
                };
            }
        };
    });

    ginasApp.directive('checkBox', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/check-box.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@'
            }
        };
    });

    ginasApp.controller('ProgressJobController', function ($scope, $http, $timeout) {
        $scope.max = 100;
        $scope.monitor = false;
        $scope.mess = "";
        $scope.dynamic = 0;
        $scope.status = "UNKNOWN";
        $scope.stat = {
            recordsPersistedSuccess: 0,
            recordsProcessedSuccess: 0,
            recordsExtractedSuccess: 0
        };
        $scope.init = function (id, pollin, status) {
            $scope.status = status;
            $scope.details = pollin;
            $scope.refresh(id, pollin);
        };
        $scope.refresh = function (id, pollin) {
            $scope.id = id;
            $scope.monitor = pollin;
            var responsePromise = $http.get(baseurl + "api/v1/jobs/" + id + "/");
            responsePromise.success(function (data, status, headers, config) {
                //$scope.myData.fromServer = data.title;
                if ($scope.status != data.status) {
                    //alert($scope.status + "!=" + data.status);
                    //location.reload();
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
                $scope.max = data.statistics.totalRecords.count;
                $scope.dynamic = data.statistics.recordsPersistedSuccess +
                    data.statistics.recordsPersistedFailed +
                    data.statistics.recordsProcessedFailed +
                    data.statistics.recordsExtractedFailed;
                $scope.max = data.statistics.totalRecords.count;
                $scope.stat = data.statistics;
                $scope.allExtracted = $scope.max;
                $scope.allPersisted = $scope.max;
                $scope.allProcessed = $scope.max;

                if ($scope.monitor) {
                    $scope.monitor = true;
                    $scope.mess = "Polling ... " + data.status;
                    $scope.refresh(id, $scope.monitor);
                }
            });
            responsePromise.error(function (data, status, headers, config) {
                $scope.monitor = false;
                $scope.mess = "Polling error!";
                $scope.monitor = false;
//                      refresh(id,pollin);
            });

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

    ginasApp.factory('SDFFields', function () {
        var SDFFields = {};
        return SDFFields;
    });


    ginasApp.controller('SDFieldController', function ($scope) {
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

    ginasApp.controller('ReferenceSelectorInstanceController', function ($scope, $modalInstance) {
        $scope.closeReferences = function () {
            $modalInstance.close();
        };
    });

    ginasApp.controller('SubstanceSelectorInstanceController', function ($scope, $modalInstance, $http) {

        //$scope.items = items;
        $scope.results = {};
        $scope.selected = null;
        $scope.searching = false;

        $scope.top = 8;
        $scope.testb = 0;

        $scope.select = function (item) {
            $scope.selected = item;
            $modalInstance.close(item);
        };

        $scope.ok = function () {
            $modalInstance.close($scope.selected.item);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.fetch = function (term, skip) {
            var url = baseurl + "api/v1/substances/search?q=" +
                term + "*&top=" + $scope.top + "&skip=" + skip;
            // console.log(url);
            var responsePromise = $http.get(url);

            responsePromise.success(function (data, status, headers, config) {
                console.log(data);
                $scope.searching = false;
                $scope.results = data;
            });

            responsePromise.error(function (data, status, headers, config) {
                $scope.searching = false;
            });
        };

        $scope.search = function () {
            $scope.searching = true;
            $scope.fetch($scope.term, 0);
        };


        $scope.nextPage = function () {
            $scope.fetch($scope.term, $scope.results.skip + $scope.results.top);
        };
        $scope.prevPage = function () {
            $scope.fetch($scope.term, $scope.results.skip - $scope.results.top);
        };


    });
})();

