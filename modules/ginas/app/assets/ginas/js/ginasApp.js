(function () {
    var tagsInput = angular.module('ngTagsInput', []);

    var ginasApp = angular.module('ginas', ['ngMessages', 'ngResource', 'ui.bootstrap', 'ui.bootstrap.showErrors',
        'LocalStorageModule', 'ngTagsInput', 'jsonFormatter', 'ginasForms', 'ginasFormElements', 'ginasAdmin'
    ]).
        run(['$anchorScroll', function($anchorScroll) {
        $anchorScroll.yOffset = 150;   // always scroll by 100 extra pixels
    }])
        .config(function (localStorageServiceProvider, $locationProvider) {
            localStorageServiceProvider
                .setPrefix('ginas');
            $locationProvider.html5Mode({
                enabled: true,
                hashPrefix: '!'
            });
        });

    ginasApp.factory('Substance', function () {
        var Substance = {};
        var substanceClass = window.location.search.split('=')[1];
        switch (substanceClass) {
            case "chemical":
                Substance.substanceClass = substanceClass;
                Substance.structure = {};
                _.set(Substance.structure, 'opticalActivity',{value:"UNSPECIFIED"});
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

    /*
    ginasApp.service('lookup', function ($http) {
        var options = {};
        var url = baseurl + "api/v1/vocabularies?filter=domain='";

        var lookup = {
            "moieties.stereochemistry": "STEREOCHEMISTRY_TYPE",
            "names.type": "NAME_TYPE",
            "officialNames.type": "NAME_TYPE",
            "unofficialNames.type": "NAME_TYPE",
            "names.nameOrgs": "NAME_ORG",
            "officialNames.nameOrgs": "NAME_ORG",
            "unofficialNames.nameOrgs": "NAME_ORG",
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
            "property.type": "PROPERTY_TYPE",
            "protein.glycosylation.glycosylationType": "GLYCOSYLATION_TYPE",
            "protein.modifications.structuralModifications.structuralModificationType": "STRUCTURAL_MODIFICATION_TYPE",
            "protein.modifications.structuralModifications.locationType": "LOCATION_TYPE",
            "protein.modifications.structuralModifications.extent": "EXTENT_TYPE",
            "structurallyDiverse.sourceMaterialClass" : "SOURCE_MATERIAL_CLASS",
            "structurallyDiverse.sourceMaterialType" : "SOURCE_MATERIAL_TYPE",
            "structurallyDiverse.sourceMaterialState" : "SOURCE_MATERIAL_STATE",
            "component.type":"MIXTURE_TYPE"
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
          //  console.log(field);
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
*/

    ginasApp.service('nameFinder', function ($http) {
        var url = baseurl + "api/v1/substances/search?q=";

        var nameFinder = {
            search: function (query) {
                var promise = $http.get(url + query + "*", {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
           //         console.log(response);
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

    ginasApp.controller("GinasController", function ($scope, $resource, $parse, $location, $compile, $modal, $http, $window, $anchorScroll, $q,
                                                     localStorageService, Substance, UUID, nameFinder, substanceSearch, substanceIDRetriever, CVFields) {
        var ginasCtrl = this;
//        $scope.select = ['Substructure', 'Similarity'];
        $scope.type = 'Substructure';
        $scope.cutoff = 0.8;
        $scope.stage = true;
        $scope.toggleGrid = false;

        $scope.scrollTo = function (prmElementToScrollTo) {
            //  console.log(prmElementToScrollTo);
            $location.hash(prmElementToScrollTo);
            $anchorScroll();
        };
        $scope.viewToggle = function () {
            $scope.submitSubstance = $scope.fromFormSubstance(angular.copy($scope.substance));
        };

        $scope.canApprove = function () {
            var lastEdit = $scope.substance.lastEditedBy;
            if (!lastEdit)
                return false;
            if ($scope.substance.status === "approved") {
                return false;
            }
            console.log(session.username);
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

        $scope.toFormSubstance = function (apiSub) {
        //    var officialNames = [];
        //    var unofficialNames = [];
            //first, flatten nameorgs, this is technically destructive
            //needs to be fixed.
          //  console.log($scope.expandCV(apiSub));
           var formSub = $scope.expandCV(apiSub);
        //    console.log(angular.copy(formSub));
            if (_.has(formSub, 'names')) {
                _.forEach(formSub.names, function (n) {
                    var temp = [];
                    if (n.nameOrgs && n.nameOrgs.length > 0) {
                        _.forEach(n.nameOrgs, function (m) {
                            if (m.deprecated) {
                                formSub.destructive = true;
                            }
                            //temp.push(m);
                            temp.push(m.nameOrg);
                        });
                        n.nameOrgs = temp;
                    }
                });
                _.set(formSub, 'update', true);

            }
            return formSub;
        };

        $scope.fromFormSubstance = function (formSub) {
            console.log(formSub);
           /* if (formSub.officialNames || formSub.unofficialNames) {
                _.forEach(formSub.officialNames, function (n) {
                    n.type = "of";
                });
            }
            if (!formSub.names) {
                if (_.isUndefined(formSub.officialNames)) {
                    formSub.names = formSub.unofficialNames;
                } else if (_.isUndefined(formSub.unofficialNames)) {
                    formSub.names = formSub.officialNames;
                } else {
                    formSub.names = formSub.officialNames.concat(formSub.unofficialNames);
                }
                delete formSub.officialNames;
                delete formSub.unofficialNames;
            }
            */
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
            return formSub;
        };

        $scope.expandCV = function (sub, path) {
                _.forEach(_.keysIn(sub), function (field) {
                    if (path) {
                        var newpath = path + "." + field;
                    } else {
                        var newpath = field;
                    }
                    if (_.isObject(sub[field])){
                        if (_.isArray(sub[field])) {
                            _.forEach((sub[field]), function (value, key){
                                if(_.isObject(value)) {
                                    $scope.expandCV(value, newpath);
                                }else{
                                    CVFields.getDomain(newpath).then(function (data) {
                                        if (!_.isUndefined(data)&& data !== 'substanceClass' ) {
                                            var domain = data;
                                            CVFields.getCV(domain).then(function (data) {
                                                var cv = data.data.content[0].terms;
                                                var newcv = _.find(cv, ['value', value]);
                                                if (_.isUndefined(newcv)) {
                                                    newcv = {};
                                                    _.set(newcv, 'display', value + ' (not in CV)');
                                                }
                                                sub[field][key] = newcv;
                                            });
                                        }
                                    });
                                }
                            });
                        } else {
                            if (!_.isNull(sub[field])) {
                                    $scope.expandCV(sub[field], newpath);
                                //});
                            }
                        }
                    } else {
                        if (!_.isNull(sub[field])) {
                            CVFields.getDomain(newpath).then(function (data) {
                                if (!_.isUndefined(data) && data !== 'SUBSTANCE_CLASS' ) {
                                    var domain = data;
                                    CVFields.getCV(domain).then(function (data) {
                                        var cv = data.data.content[0].terms;
                                        var newcv = _.find(cv, ['value', sub[field]]);
                                        if (_.isUndefined(newcv)) {
                                            newcv = {};
                                            _.set(newcv, 'display', sub[field] + ' (not in CV)');
                                        }
                                        sub[field] = newcv;
                                    });
                                }
                            });
                        }
                    }
                });
            return sub;
        };

/*        $scope.expandCV = function (sub, path) {

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
        };*/

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

        $scope.resolveMol = function (structure){
                console.log("resolve");

                var url = window.strucUrl;
                
                
                $http({
                        method: 'POST',
                        url: url,
                        data: structure.molfile,
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (data) {
                        $scope.substance.structure=data.structure;
                        $scope.substance.moieties=data.moieties;
                        
                        //this is rather hacky, should be extracted and abstracted
                        $('#structureimport').modal('hide');
                    });
                
                
        };

        $scope.isCV = function (ob) {
            if (typeof ob !== "object") return false;
            if (ob === null) return false;
            //if (typeof ob.value !== "undefined") {
                if (typeof ob.display !== "undefined") {
                    return true;
                }
         //   }
            return false;
        };

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

        $scope.defaultSave = function (obj, form, path) {

        };

        //Method for pushing temporary objects into the final message
        //Note: This was changed to use a full path for type.
        //That means that passing something like "nucleicAcid.type"
        //for type, will store the form object into that path inside
        //the substance, unless otherwise caught in the switch.
        //This simplifies some things.
        $scope.validate = function (obj, form, path) {
            console.log($scope);
            $scope.$broadcast('show-errors-check-validity');
/*           // var obj = $scope[objName];
            if(!_.isUndefined(path)) {
                var v = path.split(".");
                var type = _.last(v);
                switch (type) {
                    case "sugars":
                    case "linkages":
                        //  $scope.updateSiteList(obj);
                        break;
                    default:
                        break;
                }
            }*/
            console.log(obj);
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

               // console.log($scope);
               /// form.$setSubmitted(true);
                obj = {};

                console.log($scope);
                form.$setPristine();
                form.$setUntouched();
                $scope.$broadcast('show-errors-reset');
                // form.$setValidity();
               // form.$error= {};
                return true;
            } else {
                console.log("Invalid");
                return false;
            }


        };

        $scope.checkErrors = function(){
            console.log($scope.substanceForm);
            if(_.has($scope.substanceForm, '$error')){
                console.log($scope.substanceForm.$error);
                _.forEach($scope.substanceForm.$error, function(error){
                    console.log(error);
                });
            }
        };

        $scope.submitSubstance = function () {
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
            var r = confirm("Are you sure you'd like to submit this substance?");
            if (r != true) {
                return;
            }
            var sub = angular.copy($scope.substance);
            sub = $scope.fromFormSubstance(sub);
            console.log(JSON.stringify(sub));
            if (_.has(sub, 'update')) {
                $http.put(baseurl + 'api/v1/substances', sub, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).success(function(data){
                    alert('update was performed.');
                });
            } else {
                $http.post(baseurl + 'register/submit', sub).success(function () {
                    console.log("success");
                    alert("submitted!");
                });
            }
        };

      $scope.reset = function (form) {
            console.log(form);
           $scope.$broadcast('show-errors-reset');
            form.$setPristine();
            console.log(form);
        };

        $scope.selected = false;

        $scope.fetch = function ($query) {
            console.log($query);
            return substanceSearch.load($query);
            //return substanceSearch.search(field, $query);
        };

        $scope.approveSubstance = function () {
            var sub = angular.copy($scope.substance);
            sub = $scope.fromFormSubstance(sub);
            var keyid = sub.uuid.substr(0, 8);
            var r = confirm("Are you sure you want to approve this substance?");
            if (r == true) {
                location.href = baseurl + "substance/" + keyid + "/approve";
            }
        };

        $scope.validateSubstance = function () {
            var sub = angular.copy($scope.substance);
            console.log(angular.copy(sub));
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

        $scope.removeItem = function (list, item) {
            _.remove(list, function (someItem) {
                return item === someItem;
            });
        };

        $scope.getRange = function(start, end){
            return _.reverse(_.range(start, (end-1+2)));
        };

        $scope.submitpaster = function (input) {
            var sub = JSON.parse(input);
            //  $scope.substance = sub;
            $scope.substance = $scope.toFormSubstance(sub);
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
            //console.log(angular.copy(sub));
            //_.set(sub,'substanceClass', sub.substanceClass.value);
            $scope.substance = sub;
        } else {
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

        $scope.addToArray = function (obj, array) {
            //array.push(obj);
            // console.log(obj);
            if (!_.has($scope, array)) {
                $scope[array].push(obj);
            } else {
                //  obj = [obj]
                _.set($scope, array, obj);

            }
        };

        //method for injecting a large structure image on the browse page//
        $scope.showLarge = function (id, divid, ctx) {
            var result = document.getElementsByClassName(divid);
            var elementResult = angular.element(result);
            if ($scope.stage === true) {
                $scope.stage = false;
                childScope = $scope.$new();
                var rend;
                if(!_.isUndefined(ctx)){
                 rend ='<rendered size="500" id='+id+' ctx='+ctx+'></rendered>';
                }else{
                    rend = '<rendered size="500" id='+id+'></rendered>';
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

    ginasApp.directive('titleHeader', function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                name: '=',
                kind: '='
            },
            link: function (scope, element, attrs) {
                if (scope.name) {
                    template = angular.element('<h1> Editing <code>' + scope.name + '</code></h1>');
                    element.append(template);
                    $compile(template)(scope);
                } else {
                    template = angular.element('<h1> Registering new <code> ' + scope.kind + '</code></h1>');
                    element.append(template);
                    $compile(template)(scope);
                }
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
                ctx: '@'
            },
            link: function(scope, element, attrs){
                var url = baseurl+ 'img/'+scope.id+'.svg?size={{size||150}}';
                if(!_.isUndefined(scope.ctx)) {
                    url += '&context={{ctx}}';
                }
                if(attrs.smiles){
                    url =baseurl+ "render/"+attrs.smiles;
                }
                var template = angular.element('<img ng-src='+url+'>');
                element.append(template);
                $compile(template)(scope);
            }
         //   template: '<img ng-src=\"' + baseurl + 'img/'+id+'.svg?size={{size||150}}&context={{ctx}}\">'
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

    ginasApp.directive('siteView', function (siteList) {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                parent:'='
            },
            link: function (scope, element, attrs) {
                if(!_.isUndefined(scope.referenceobj)) {
                    if(_.has(scope.referenceobj, 'sites')){
                    scope.referenceobj.$displayString = siteList.siteString(scope.referenceobj.sites);

                    }else{
                        scope.referenceobj.$displayString = siteList.siteString(scope.referenceobj);
                    }
                }
              //  console.log(scope);

            },
           template: '<div><div><span>{{referenceobj.$displayString}}</span><br></div><div ng-if="referenceobj.sites.length"><span>({{referenceobj.sites.length}} sites)</span></div></div>'
        };
    });

    ginasApp.directive('comment', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            template: '<div><span id="comment">{{value|limitTo:40}}...</span></div>',
        };
    });

    ginasApp.directive('access', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            template: '<div><i class="fa fa-lock fa-2x warning"  uib-tooltip="Edit user access" id="access"></i><span ng-repeat = "access in value"><br>{{access.display}}</span></div>'
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

    ginasApp.directive('aminoAcid', function ($compile) {
        var div = '<div class = "col-md-1">';
        var validTool = '<a href="#" class= "aminoAcid" uib-tooltip="{{acid.subunitIndex}}-{{acid.residueIndex}} : {{acid.value}} ({{acid.type}}{{acid.name}})">{{acid.value}}</a>';
        var invalidTool = '<a href="#" class= "invalidAA" uib-tooltip-class="invalidTool" uib-tooltip="INVALID">{{acid.value}}</a>';
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

    ginasApp.directive('subunit', function () {

        return {
            restrict: 'E',
            scope: {
                parent: '=',
                obj: '=',
                residues: '='
            },
            link: function (scope, element, attrs) {
                console.log(scope);
                scope.edit = false;
                scope.getType = function (aa) {
                    if (aa == aa.toLowerCase()) {
                        return 'D-';
                    }
                    else {
                        return 'L-';
                    }
                };

                scope.parseSubunit = function () {
                    // scope.obj.cysteineCount= 0;
                    scope.obj.cysteineIndices = [];
                    var display = [];
                    _.forEach(scope.obj.sequence, function (aa, index) {
                        var obj = {};
                        obj.value = aa;
                        var temp = (_.find(scope.residues, ['value', aa.toUpperCase()]));
                        if (!_.isUndefined(temp)) {
                            obj.name = temp.display;
                            obj.valid = true;
                            if (scope.obj.subunitIndex) {
                                obj.subunitIndex = scope.obj.subunitIndex;
                            } else {
                                obj.subunitIndex = scope.obj.index;
                            }
                            obj.residueIndex = index - 0 + 1;
                            if (scope.parent.substanceClass === 'protein') {
                                obj.type = scope.getType(aa);
                            }
                            if (aa.toUpperCase() == 'C') {
                                obj.cysteine = true;
                                scope.obj.cysteineIndices.push(index + 1);
                            }
                        } else {
                            obj.valid = false;
                        }
                        display.push(obj);
                    });
                    display = _.chunk(display, 10);
                    scope.subunitDisplay = display;
                    scope.parent.$subunitDisplay.push(display);
                };

//******************************************************************this needs a check to delete the subunit if cleaning the subunit results in an empty string
                scope.cleanSequence = function () {
                    console.log('click');
                    scope.obj.sequence = _.filter(scope.obj.sequence, function (aa) {
                        var temp = (_.find(scope.residues, 'value', aa.toUpperCase()));
                        console.log(temp);
                        if (!_.isUndefined(temp)) {
                            return temp;
                        }
                    }).toString().replace(/,/g, '');
                    console.log(scope);
                    scope.parseSubunit();
                };
                scope.parseSubunit();
            },
            templateUrl: baseurl + "assets/templates/subunit.html"
        };
    });

    ginasApp.directive('substanceChooserSelector', function ($templateRequest, $compile) {
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
                var childScope;
                var template;
                scope.stage = true;
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
                        formHolder = '<substance-search-form referenceobj = referenceobj field =field q=q></substance-search-form>';
                        break;
                }

                scope.toggleStage = function () {
                    if (_.isUndefined(scope.referenceobj)) {
                        var x = {};
                        _.set(scope, scope.referenceobj, x);
                    }
                    console.log(scope);
                    var result = document.getElementsByClassName(attrs.formname);
                    var elementResult = angular.element(result);
                    if (scope.stage === true) {
                        scope.stage = false;
                        childScope = scope.$new();
                        var compiledDirective = $compile(formHolder);
                        var directiveElement = compiledDirective(childScope);
                        elementResult.append(directiveElement);
                    } else {
                        scope.q =null;
                        childScope.$destroy();
                        elementResult.empty();
                        scope.stage = true;
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
                referenceobj: '=',
                field:'=',
                q: '='
            },
            templateUrl: baseurl + 'assets/templates/selectors/substanceSelector.html',
            link: function (scope, element, attrs) {
                console.log(scope);
                scope.results = {};
               // scope.searching = false;

                scope.top = 8;
                scope.testb = 0;
                scope.searching = true;



                scope.createSubref = function (selectedItem) {
                 //  var temp = _.pick(selectedItem,['uuid','_name','approvalID']);
                   var temp ={};
                    temp.refuuid = selectedItem.uuid;
                    temp.refPname = selectedItem._name;
                   temp.approvalID = selectedItem.approvalID;
                    temp.substanceClass = "reference";
                    console.log(scope);
                  //  scope.subref = angular.copy(temp);
                    _.set(scope.referenceobj, scope.field, angular.copy(temp));
                //    console.log(scope);
                    scope.q =null;
                    scope.$parent.$parent.toggleStage();
                };

                scope.fetch = function (term, skip) {
                    var url = baseurl + "api/v1/substances/search?q=" +
                        term + "*&top=" + scope.top + "&skip=" + skip;
                    var responsePromise = $http.get(url, {cache: true});

                    responsePromise.success(function (data, status, headers, config) {
                       // console.log(data);
                        scope.searching = false;
                        scope.results = data;
                    });

                    responsePromise.error(function (data, status, headers, config) {
                        scope.searching = false;
                    });
                };

/*                scope.search = function () {
                    scope.searching = true;
                    scope.fetch(scope.term, 0);
                };*/

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
                subref: '='
            },
            link: function(scope, element) {
                console.log(scope);
                var template = angular.element('<div><rendered id = {{subref.refuuid}}></rendered><br/><code>{{subref.refPname}}</code></div>');
                element.append(template);
                $compile(template)(scope);
            }
        };
    });


//modal
    ginasApp.directive('substanceChooser', function ($modal) {
        return {
            restrict: 'E',
            require: "ngModel",
            scope: {
                mymodel: '=ngModel',
                lite: '=lite'
            },
            templateUrl: baseurl + "assets/templates/selectors/substanceSelectorElement.html",
            link: function (scope) {

                 console.log(scope);

                scope.openSelector = function (parentRef, instanceName, test) {
                    //console.log(test);
                    var modalInstance = $modal.open({
                        animation: true,
                        templateUrl: baseurl + 'assets/templates/selectors/substanceSelector.html',
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

    //Ok, this needs to be re-evaluated a bit.
    //Right now, it always round trips, but that doesn't always make sense.
    ginasApp.directive('sketcher', function ($http, $timeout, localStorageService, Substance, CVFields, polymerUtils, UUID) {
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
                                CVFields.getCV("POLYMER_SRU_TYPE").then(function(response){
                                    var cv = response.data.content[0].terms;
                                    data.structuralUnits[i].type = _.find(cv, ['value', data.structuralUnits[i].type]);

                                });
                                //data.structuralUnits[i].type = lookup.expandCVValueDisplay("POLYMER_SRU_TYPE", data.structuralUnits[i].type);
                            }
                            polymerUtils.setSRUConnectivityDisplay(data.structuralUnits);
                            scope.formsubstance.structuralUnits = data.structuralUnits;

                        } else {
                            scope.formsubstance.structure = data.structure;
                            scope.formsubstance.moieties = data.moieties;
                            for (var j = 0; j < data.moieties.length; j++) {
                                data.moieties[j]._id = UUID.newID();
                            }
                            scope.formsubstance.q = data.structure.smiles;
                        }
                        console.log(scope);
                    });
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
                  //  console.log("There is an id, it's:" + structureid);
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
                return '<button type="button" class="btn btn-primary" aria-label ="export molfile" uib-tooltip="View " structureid=structureid format=format export><i class="fa fa-external-link chem-button"></i></button>';
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

    ginasApp.directive('molExport', function ($http) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/molexport.html"
        };
    });

    ginasApp.directive('deleteButton', function () {
        return {
            restrict: 'E',
            template: '<a ng-click="deleteObj()" uib-tooltip="Delete Item"><i class="fa fa-times fa-2x danger"></i></a>',
            link: function (scope, element, attrs) {
                scope.deleteObj = function () {
                    console.log(scope);
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



    ginasApp.directive('errorMessage', function(){
        return {
            restrict: 'E',
            replace: true,
            scope: {
                message:'='
            },
            link: function(scope, element, attrs){
                console.log(scope);
                console.log("hi");
            },
            template: '<span><h1>{{message}}</h1></span>'

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

