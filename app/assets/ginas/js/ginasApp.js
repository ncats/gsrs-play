(function () {
    var ginasApp = angular.module('ginas', ['ngMessages', 'ngResource', 'ui.bootstrap', 'ui.bootstrap.showErrors',
        'ui.bootstrap.datetimepicker', 'LocalStorageModule', 'ngTagsInput', 'xeditable', 'ui.select'
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
            min = parseInt(min); //Make string input int
            max = parseInt(max);
            for (var i = min; i < max; i++)
                input.push(i);
            return input;
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
                var subunit = "";
                break;
            case "structurallyDiverse":
                Substance.substanceClass = substanceClass;
                Substance.structurallyDiverse = {};
                break;
            default:
                console.log('invalid substance class');
                break;
        }
        return Substance;
    });


    ginasApp.factory('lookup', function () {
        var lookup = {
            "names.type": "NAME_TYPE",
            "names.nameOrgs": "NAME_ORG",
            "names.nameJurisdiction": "JURISDICTION",
            "names.domains": "NAME_DOMAIN",
            "names.languages": "LANGUAGE",
            "codes.system": "CODE_SYSTEM",
            "codes.type": "CODE_TYPE",
            "relationships.type": "RELATIONSHIP_TYPE",
            "relationships.interactionType": "INTERACTION_TYPE",
            "relationships.qualification": "QUALIFICATION",
            "references.docType": "DOCUMENT_TYPE"
        };

        lookup.getFromName = function (field, val) {
            var domain = lookup[field];
            if (typeof domain !== "undefined") {
                return {
                    "display": val,
                    "value": val,
                    "domain": domain
                };
            }
            return null;
        };
        return lookup;
    });

    ginasApp.controller("GinasController", function ($scope, $resource, $parse, $location, $modal, $http, $window, $anchorScroll, localStorageService, Substance, data, substanceSearch, substanceIDRetriever, lookup) {

        $scope.range = function (min, step) {
            step = step || 1;
            var input = [];
            for (var i = 1; i <= min; i += step) input.push(i);
            return input;
        };


        $scope.openSelector = function (storein) {
                console.log("openning");
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: baseurl + 'assets/ginas/templates/substanceSelector.html',
                controller: 'SubstanceSelectorInstanceController',
                size: 'lg'

            });

            modalInstance.result.then(function (selectedItem) {
                var subref = {};
                console.log(selectedItem);
                subref.refuuid = selectedItem.uuid;
                subref.refPname = selectedItem.name;
                subref.approvalID = selectedItem.approvalID;
                subref.substanceClass = "reference";
                //this part assumes there's only ever 1 substance reference for a whole substance.
                //However, there are likely to be many (maybe hundreds) in various contexts.
                //If this is to be used as a stop-gap measure, it at least needs to be a key-value
                //pair array for later selection. 
                //storein = subref;
                console.log($scope);
                console.log(storein);
                console.log(getObjectAt($scope,storein));
                setObjectAt($scope,storein,subref);
                $scope.subref=subref;
                //$scope[storein]=subref;
                console.log("selected");
            });
        };

        $scope.toFormSubstance = function (apiSub) {

            //first, flatten nameorgs, this is technically destructive
            //needs to be fixed.
            for (var i in apiSub.names) {
                if (typeof apiSub.names[i].nameOrgs != "undefined") {
                    for (var j in apiSub.names[i].nameOrgs) {
                        if (apiSub.names[i].nameOrgs[j].deprecated) {
                            apiSub.destructive = true;
                        }
                        apiSub.names[i].nameOrgs[j] = apiSub.names[i].nameOrgs[j].nameOrg;
                    }
                }
            }


            console.log($scope);
            apiSub = $scope.expandCV(apiSub, "");
            apiSub = $scope.splitNames(apiSub);

            var references = {};
            for (var v in apiSub.references) {
                references[apiSub.references[v].uuid] = apiSub.references[v];
                apiSub.references[v].id = v - 1 + 2;
            }
            apiSub = $scope.expandReferences(apiSub, references, 0);


            return apiSub;
        };

        $scope.fromFormSubstance = function (formSub) {

            if (formSub.officialNames || formSub.unofficialNames) {
                for (var n in formSub.officialNames) {
                    var name = formSub.officialNames[n];
                    name.type = "of";
                }
                formSub.names = formSub.officialNames.concat(formSub.unofficialNames);
                delete formSub.officialNames;
                delete formSub.unofficialNames;
            }
            if (formSub.q) {
                delete formSub.q;
            }
            if (formSub.subref) {
                delete formSub.subref;
            }

            formSub = $scope.flattenCV(formSub);
            formSub = $scope.collapseReferences(formSub, 0);
            return formSub;
        };

        var ginasCtrl = this;

        $scope.select = ['Substructure', 'Similarity'];
        $scope.type = 'Substructure';
        $scope.cutoff = 0.8;

        //date picker
        $scope.open = function ($event) {
            $scope.status.opened = true;
        };

        $scope.status = {
            opened: false
        };

        //datepicker//

        //adds reference id//
        $scope.refLength = function () {
            if (!$scope.substance.references) {
                return 1;
            }
            return $scope.substance.references.length + 1;
        };
        //add reference id//

        //populates tag fields
        $scope.loadItems = function (field, $query) {
            data.load(field);
            return data.search(field, $query);
        };
        //populates tag fields//

        $scope.retrieveItems = function (field, $query) {
            data.load(field);
            return data.lookup(field, $query);
        };

        $scope.scrollTo = function (prmElementToScrollTo) {
            $location.hash(prmElementToScrollTo);
            $anchorScroll();
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


        $scope.proteinDetails = function (obj, form) {
            $scope.$broadcast('show-errors-check-validity');
            console.log(obj);
            if (form.$valid) {
                this.substance.protein.proteinType = obj.proteinType;
                this.substance.protein.proteinSubType = obj.proteinSubType;
                this.substance.protein.sequenceOrigin = obj.sequenceOrigin;
                this.substance.protein.sequenceType = obj.sequenceType;
                $scope.detailsSet = true;
            }
            $scope.$broadcast('show-errors-reset');
        };

        $scope.setDisulfide = function (site, bridge) {
            site.disulfide = true;
            site.bridge = bridge;
            console.log(site);
        };

        $scope.findSite = function (array, index) {
            var returnSite = {};
            _.forEach(array, function (n) {
                var temp = (_.find(n, 'residueIndex', index));
                if (typeof temp !== "undefined") {
                    console.log(temp);
                    returnSite = temp;
                }
            });
            return returnSite;
        };

        $scope.makeSite = function (obj) {
            console.log(obj);
            var site = {};
            site.subunitIndex = obj.subunitIndex - 1 + 1;
            site.residueIndex = obj.residueIndex - 1 + 1;
            return site;
        };


        $scope.parseDisulfide = function (obj, type) {
            var disulfide = [];

            //PERSIST--add sites to disulfide list
            var site = $scope.makeSite(obj);
            disulfide.push(site);

            //DISPLAY--make array for bridge site
            var bridge = [obj.subunitIndexEnd, obj.residueIndexEnd];

            //DISPLAY-- find site, add disulfide property and bridge
            var subunit = (_.pick($scope.substance.subunits, 'index', obj.subunitIndex - 1)[0]);
            tempSite = $scope.findSite(subunit.display, obj.residueIndex - 1 + 1);
            $scope.setDisulfide(tempSite, bridge);

            //DO IT AGAIN for ending index. can't really be done in a loop, can make and end site object and abstract these though
            var endSite = $scope.makeSite({
                'subunitIndex': obj.subunitIndexEnd - 1 + 1,
                'residueIndex': obj.residueIndexEnd - 1 + 1
            });

            disulfide.push(endSite);
            var endBridge = [obj.subunitIndex, obj.residueIndex];
            subunit = (_.pick($scope.substance.subunits, 'index', obj.subunitIndexEnd - 1)[0]);
            var tempEndSite = $scope.findSite(subunit.display, obj.residueIndexEnd - 1 + 1);
            $scope.setDisulfide(tempEndSite, endBridge);


            $scope.substance.protein.disulfideLinks.push(disulfide);
        };


        $scope.parseGlycosylation = function (obj, type) {
            var link = obj.link;
            if (!$scope.substance.protein.glycosylation[link + "GlycosylationSites"]) {
                $scope.substance.protein.glycosylation[link + "GlycosylationSites"] = [];
            }
            var site = $scope.makeSite(obj);
            site.link = link;
            $scope.substance.protein.glycosylation[link + "GlycosylationSites"].push(site);
            $scope.substance.protein.glycosylation.count++;
            $scope.substance.protein.glycosylation.glycosylationType = obj.glycosylationType;
            console.log($scope.substance.protein.glycosylation);

            //DISPLAY-- find site, add glycosylation property
            var subunit = (_.pick($scope.substance.subunits, 'index', obj.subunitIndex - 1)[0]);
            tempSite = $scope.findSite(subunit.display, obj.residueIndex - 1 + 1);
            tempSite.glycosylationSite = true;
        };

        $scope.parseAgentModification = function (obj, type) {

        };

        //main submission method
        $scope.validate = function (obj, form, type) {
            console.log(type);
            switch (type) {
                case "protein":
                    $scope.proteinDetails(obj, form);
                    break;
                case "disulfideLinks":
                    if (!$scope.substance.protein[type]) {
                        $scope.substance.protein[type] = [];
                    }
                    $scope.parseDisulfide(obj, type);
                    break;
                case "glycosylation":
                    if (!$scope.substance.protein[type]) {
                        $scope.substance.protein[type] = {};
                    }
                    if (!$scope.substance.protein.glycosylation.count) {
                        $scope.substance.protein.glycosylation.count = 0;
                    }
                    $scope.parseGlycosylation(obj, type);
                    break;
                case "agentModification":
                    break;
                //    if (!$scope.substance.protein.modifications) {
                //        $scope.substance.proteinmodifications = {};
                //    }
                //    $scope.parseAgentModifications(obj);
                //    break;
                default:
                    $scope.$broadcast('show-errors-check-validity');
                    if (form.$valid) {
                        if (getObjectAt(this.substance,type)) {
                            if (type == 'references') {
                                if (typeof obj.uuid == "undefined") {
                                    obj.uuid = uuid();
                                }
                                obj.id = this.substance.references.length + 1;
                            }
                            getObjectAt(this.substance,type).push(obj);
                            //this.substance[type].push(obj);
                        } else {
                            setObjectAt(this.substance,type, []);
                            if (type == 'references') {
                                if (typeof obj.uuid == "undefined") {
                                    obj.uuid = uuid();
                                }
                                obj.id = 1;
                            }
                            getObjectAt(this.substance,type).push(obj);
                            //this.substance[type].push(obj);
                        }
                        $scope.$broadcast('show-errors-reset');
                    }
            }
        };

        $scope.toggle = function (el) {
            if (el.selected) {
                el.selected = !el.selected;
            } else {
                el.selected = true;
            }
        };

        $scope.splitNames = function (sub) {
            var names = sub.names;
            var officialNames = [];
            var unofficialNames = [];
            if (names) {
                for (var n in names) {
                    var name = names[n];
                    if (name.type.value == "of") {
                        officialNames.push(name);
                    } else {
                        unofficialNames.push(name);
                    }
                    sub.unofficialNames = unofficialNames;
                    sub.officialNames = officialNames;
                    delete sub.names;
                }
            }
            return sub;

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
            console.log($scope);
            form.$setPristine();
            console.log(form);
            $scope.$broadcast('show-errors-reset');
            console.log($scope);
        };

        $scope.selected = false;

        $scope.info = function (scope, element) {
            console.log($scope);
            console.log(scope);
            console.log(element);
            $scope.selected = !$scope.selected;
        };

        $scope.openModal = function (type) {
            var template;
            switch (type) {
                case "reference":
                    template = "";
                    break;
                case "structuresearch":
                    template = baseurl + 'assets/ginas/templates/substanceselector.html';
                    break;
            }
            var modalInstance = $modal.open({
                animation: true,
                //templateUrl: baseurl + 'assets/ginas/templates/substanceselector.html',
                templateUrl: template,
                // windowTemplateUrl: baseurl + 'assets/ginas/templates/modal-window.html',
                controller: 'ModalController',
                size: 'lg'
            });

        };

        $scope.fetch = function ($query) {
            console.log($query);
            return substanceSearch.load($query);
            //return substanceSearch.search(field, $query);
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

        $scope.expandReferences = function (sub, referenceMap, depth) {
            for (var v in sub) {
                if (depth > 0) {
                    if (v === "references") {
                        for (var r in sub[v]) {
                            sub[v][r] = referenceMap[sub[v][r]];
                        }
                    }
                }
                if (typeof sub[v] === "object") {
                    $scope.expandReferences(sub[v], referenceMap, depth + 1);
                }
            }
            return sub;
        };

        $scope.collapseReferences = function (sub, depth) {
            for (var v in sub) {
                if (depth > 0) {
                    if (v === "references") {
                        for (var r in sub[v]) {

                            sub[v][r] = sub[v][r].uuid;
                        }
                    }
                }
                if (typeof sub[v] === "object") {
                    $scope.collapseReferences(sub[v], depth + 1);
                }
            }
            return sub;
        };


        $scope.flattenCV = function (sub) {
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

        $scope.submitSubstance = function () {
            var sub = angular.copy($scope.substance);
            sub = $scope.fromFormSubstance(sub);
            $http.post(baseurl + 'submit', sub).success(function () {
                console.log("success");
                alert("submitted!");
            });
        };

        $scope.validateSubstance = function () {
            var sub = angular.copy($scope.substance);
            // console.log(angular.copy(sub));
            sub = $scope.fromFormSubstance(sub);
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
                }
                $scope.errorsArray = arr;
            });
        };

        $scope.movesubref = function (relationship) {
            console.log(relationship);
            if (Substance.subref) {
                relationship.subref = Substance.subref;
                console.log(relationship);
                delete Substance.subref;
            }
        };

        $scope.submitpaster = function (input) {
            console.log(input);
            var sub = JSON.parse(input);
            $scope.substance = sub;
            console.log($scope);
        };

        $scope.bugSubmit = function (bugForm) {
            console.log(bugForm);
        };

        $scope.setEditId = function (editid) {
            localStorageService.set('editID', editid);
        };

        if (typeof $window.loadjson !== "undefined" &&
            JSON.stringify($window.loadjson) !== "{}") {
            var sub = $scope.toFormSubstance($window.loadjson);
            $scope.substance = sub;
        } else {
            var edit = localStorageService.get('editID');
            if (edit) {
                localStorageService.remove('structureid');
                substanceIDRetriever.getSubstances(edit).then(function (data) {
                    var sub = $scope.toFormSubstance(data);
                    $scope.substance = sub;
                    localStorageService.remove('editID');
                });
            } else {
                $scope.substance = Substance;
            }
        }

    });

    var uuid = function uuid() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }

        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    };

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

    ginasApp.factory('isDuplicate', function ($q, substanceFactory) {
        return function dupCheck(modelValue) {
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
            return $http.get(url + name.toUpperCase() + "'", {
                headers: {
                    'Content-Type': 'text/plain'
                }
            });
        };
        return substanceFactory;
    }]);

    ginasApp.service('substanceIDRetriever', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances(";
        var substanceIDRet = {
            getSubstances: function (editId) {
                console.log(editId);
                var promise = $http.get(url + editId + ")?view=full", {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    console.log(response);
                    return response.data;
                });
                return promise;
            }
        };
        return substanceIDRet;
    }]);

    ginasApp.service('substanceRetriever', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances?filter=names.name='";
        var substanceRet = {
            getSubstances: function (name) {
                var promise = $http.get(url + name.toUpperCase() + "'", {
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

    ginasApp.service('data', function ($http) {
        var options = {};
        var url = baseurl + "api/v1/vocabularies?filter=domain='";

        this.load = function (field) {
            $http.get(url + field.toUpperCase() + "'", {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
                console.log(data);
                options[field] = data.content[0].terms;
            });
        };

        this.search = function (field, query) {
            return _.chain(options[field])
                .filter(function (x) {
                    return !query || x.display.toLowerCase().indexOf(query.toLowerCase()) > -1;
                })
                .sortBy('display')
                .value();
        };
        this.lookup = function (field, query) {
            console.log(options);
            return _.chain(options[field])
                .filter(function (x) {
                    return !query || x.value.toLowerCase().indexOf(query.toLowerCase()) > -1;
                })
                .sortBy('value')
                .value();
        };
    });

    ginasApp.service('substanceSearch', function ($http) {
        var options = {};
        var url = baseurl + "api/v1/suggest/Name?q=";

        this.load = function (field) {
            $http.get(url + field.toUpperCase(), {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (response) {
                options.data = response;
            });
        };

        this.search = function (query) {
            return options;
        };
    });

    ginasApp.directive('rendered', function ($http) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                id: '='
                /*                size: '=',
                 amap :'='*/

            },
           template: '<img src=\"' + baseurl + 'img/{{id}}.svg\">'
        };
    });

    ginasApp.directive('amount', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            link: function (scope, element, attrs) {

            },
            template: '<div><span class="amt">{{value.nonNumericValue}} {{value.average}} ({{value.low}} to {{value.high}}) {{value.unit}}</span></div>'
        };
    });
    

    ginasApp.directive('aminoAcid', function ($compile) {
        var div = '<div class = "col-md-1">';
        var validTool = '<a href="#" class= "aminoAcid" tooltip="{{acid.subunitIndex}}-{{acid.index}} : {{acid.value}} ({{acid.type}}-{{acid.name}})">{{acid.value}}</a>';
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

    ginasApp.directive('subunit', function () {
        clean = function (s) {
            return s.replace(/[^A-Za-z]/g, '');
        };

        parseSubunit = function (sequence, subunit) {
            var split = sequence.replace(/[^A-Za-z]/g, '').split('');
            var display = [];
            var obj = {};
            var invalid = ['B', 'J', 'O', 'U', 'X', 'Z'];
            for (var i in split) {
                var aa = split[i];
                var valid = _.indexOf(invalid, aa.toUpperCase());
                if (valid >= 0) {
                    obj.value = aa;
                    obj.valid = false;
                    obj.subunitIndex = subunit;
                    obj.residueIndex = i - 1 + 2;
                    display.push(obj);
                    obj = {};
                } else {
                    obj.value = aa;
                    obj.valid = true;
                    obj.name = findName(aa);
                    obj.type = getType(aa);
                    obj.subunitIndex = subunit;
                    obj.residueIndex = i - 1 + 2;
                    if (aa.toUpperCase() == 'C') {
                        obj.cysteine = true;
                    }
                    display.push(obj);
                    obj = {};
                }
            }
            this.display = display;
            display = _.chunk(display, 10);
            return display;
        };

        addGlycosylation = function (subunit, index) {


        };

        findName = function (aa) {
            switch (aa.toUpperCase()) {
                case 'A':
                    return "Alanine";
                case 'C':
                    return "Cysteine";
                case 'D':
                    return "Aspartic acid";
                case 'E':
                    return "Glutamic acid";
                case 'F':
                    return "Phenylalanine";
                case 'G':
                    return "Glycine";
                case 'H':
                    return "Histidine";
                case 'I':
                    return "Isoleucine";
                case 'K':
                    return "Lysine";
                case 'L':
                    return "Leucine";
                case 'M':
                    return "Methionine";
                case 'N':
                    return "Asparagine";
                case 'P':
                    return "Proline";
                case 'Q':
                    return "Glutamine";
                case 'R':
                    return "Arginine";
                case 'S':
                    return "Serine";
                case 'T':
                    return "Threonine";
                case 'V':
                    return "Valine";
                case 'W':
                    return "Tryptophan";
                case 'Y':
                    return "Tyrosine";

                default:
                    return "Tim forgot one";
            }

        };

        getType = function (aa) {
            if (aa == aa.toLowerCase()) {
                return 'D';
            }
            else {
                return 'L';
            }
        };


        return {
            restrict: 'E',
            require: 'ngModel',
            scope: '=',
            link: function (scope, element, attr, ngModel) {
                scope.$watch(function () {
                    var seq = ngModel.$modelValue;
                    if (typeof seq !== "undefined") {
                        seq = clean(seq);
                        scope.subunit.display = parseSubunit(seq, attr.subindex - 1 + 2);
                        scope.subunit.index = attr.subindex - 1 + 2;
                        ngModel.$setViewValue(seq);
                        // console.log(scope);
                    }
                });
            },
            template: '<textarea class="form-control string"  rows="5" ng-model="subunit.sequence" ng-model-options="{ debounce: 1000 }" name="sequence" placeholder="Sequence" title="sequence" id="sequence" required></textarea>',
        };
    });

    ginasApp.directive('sketcher', function ($http, $timeout, localStorageService, Substance) {
        return {
            restrict: 'E',
            require: "ngModel",
            scope: {
                formsubstance: '=structure'
            },
            template: "<div id='sketcherForm' dataformat='molfile' ondatachange='setMol(this)'></div>",
            link: function (scope, element, attrs, ngModelCtrl) {
                //console.log("LINKING");
                sketcher = new JSDraw("sketcherForm");
                var url = window.strucUrl; //baseurl + 'smiles';
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

                    //console.log("CHANGING");
                    $http({
                        method: 'POST',
                        url: url,
                        data: mol,
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (data) {
                        lastmol = data.structure.molfile;
                        scope.formsubstance.structure = data.structure;
                        scope.formsubstance.moieties = data.moieties;
                        scope.formsubstance.q = data.structure.smiles;
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
                    //console.log("I SEE A CHANGE!");
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
                structureid: '='
            },
            template: '<button type="button" class="btn btn-primary" structureid = structureid  export><i class="fa fa-external-link chem-button"></i></button>'
        };
    });

    ginasApp.directive('errorWindow', function () {
        return {
            restrict: 'E',
            scope: {
                error: '='
            },
            templateUrl: baseurl + "assets/ginas/templates/errorwindow.html"
        };
    });

    ginasApp.directive('export', function ($http) {
        return function (scope, element, attrs) {
            element.bind("click", function () {
                var modal = angular.element(document.getElementById('export-mol'));
                $http({
                    method: 'GET',
                    url: baseurl + 'export/' + scope.structureid + '.sdf',
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
            templateUrl: baseurl + "assets/ginas/templates/molexport.html"
        };
    });


    ginasApp.controller('DiverseController', function ($scope, Substance, $rootScope) {
        this.adding = true;

        this.toggleEdit = function () {
            this.editing = !this.editing;
        };

        this.toggleAdd = function () {
            this.adding = !this.adding;
        };

        this.reset = function () {
            $scope.diverse = {};
            $scope.$broadcast('show-errors-reset');
        };

        this.validate = function (obj) {
            $scope.$broadcast('show-errors-check-validity');
            if ($scope.diverseForm.$valid) {
                Substance.structurallyDiverse.sourceMaterialClass = obj.sourceMaterialClass;
                Substance.structurallyDiverse.sourceMaterialType = obj.sourceMaterialType;
                Substance.structurallyDiverse.sourceMaterialState = obj.sourceMaterialState;
                this.toggleAdd();
            }
        };

        this.setEdited = function (obj) {
            $scope.editObj = obj;
            $scope.tempCopy = angular.copy(obj);
        };

        this.update = function (reference) {
            console.log(reference);
            var index = Substance.references.indexOf(reference);
            Substance.references[index] = reference;
            $scope.editObj = null;
            this.toggleEdit();
        };

        this.remove = function (reference) {
            var index = Substance.references.indexOf(reference);
            Substance.references.splice(index, 1);
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


    ginasApp.controller('ModalController', function ($scope, Substance, $modalInstance, substanceSearch, substanceRetriever) {
        $scope.ok = function () {
            console.log("ok");
            $modalInstance.close();
        };

        $scope.cancel = function () {
            console.log("cancel");
            $modalInstance.dismiss('cancel');
        };

        $scope.fetch = function ($query) {
            console.log($query);
            substanceSearch.load($query);
            return substanceSearch.search($query);
        };

        $scope.retrieveSubstance = function (tag) {
            substanceRetriever.getSubstances(tag.key).then(function (data) {
                console.log(data.content[0].structure);
                $scope.relationship.subref.refuuid = data.content[0].structure.id;
                $scope.relationship.subref.refPname = data.content[0].name;
                $scope.relationship.subref.approvalID = data.content[0].approvalID;
                $scope.relationship.subref.substanceClass = "reference";
            });
        };
        $scope.clear = function () {
            $scope.relationship.subref = {};
        };

    });

    ginasApp.controller('SubstanceListController', function ($scope) {
        $scope.bigview = false;
        $scope.initialized = false;
        $scope.toggle = function (src) {
            $scope.initialized = true;
            $scope.bigview = !$scope.bigview;
            $scope.src = src;
        };

    });

    ginasApp.factory('SDFFields', function () {
        var SDFFields = {};
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
            //console.log(model);
        };

        $scope.$watch('radio.model', function (newVal, oldVal) {
            var sdf = window.SDFFields[$scope.path];
            if (typeof sdf === "undefined") {
                sdf = {};
                window.SDFFields[$scope.path] = sdf;
            }
            sdf.path = $scope.path;
            sdf.method = $scope.radio.model;

            console.log(window.SDFFields);
            var l = [];
            for (var k in window.SDFFields) {
                l.push(window.SDFFields[k]);
            }
            $("#mappings").val(JSON.stringify(l));
        });


    });

  
    // Please note that $modalInstance represents a modal window (instance) dependency.
    // It is not the same as the $modal service used above.

    ginasApp.controller('SubstanceSelectorInstanceController', function ($scope, $modalInstance, $http) {

        //$scope.items = items;
        $scope.results = {};
        $scope.selected = {};

        $scope.top = 4;
        $scope.testb = 0;

        $scope.select = function (item) {
            var subref = {};

            console.log(item);
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
            console.log(url);
            var responsePromise = $http.get(url);

            responsePromise.success(function (data, status, headers, config) {
                console.log(data);
                $scope.results = data;
            });

            responsePromise.error(function (data, status, headers, config) {
                //alert("AJAX failed!");
            });
        };

        $scope.search = function () {
            $scope.fetch($scope.term, 0);
        };


        $scope.nextPage = function () {
            console.log($scope.results.skip);
            $scope.fetch($scope.term, $scope.results.skip + $scope.results.top);
        };
        $scope.prevPage = function () {
            $scope.fetch($scope.term, $scope.results.skip - $scope.results.top);
        };


    });


})();
window.SDFFields = {};

function getDisplayFromCV(domain, value) {
    for (var i in window.CV_REQUEST.content) {
        if (window.CV_REQUEST.content[i].domain === domain) {
            var terms = window.CV_REQUEST.content[i].terms;
            for (var t in terms) {
                if (terms[t].value === value) {
                    return terms[t].display;
                }
            }
        }
    }
    return value;
}

function vocabsetup(cv) {
    window.CV_REQUEST = cv;
    console.log("finished");
}

function getObjectAt(obj, path){
        var v = path.split(".");
        var reto=obj;
        for(var i=0;i<v.length;i++){
                reto = reto[v[i]];
                if(typeof reto == "undefined" || reto === null){
                        return undefined;
                }
        }
        return reto;
}
function setObjectAt(obj, path, nobj){
        var v = path.split(".");
        var reto=obj;
        for(var i=0;i<v.length-1;i++){
                if(typeof reto[v[i]] == "undefined" || reto[v[i]] === null){
                        reto[v[i]]={};        
                }
                reto = reto[v[i]];                
        }
        if(typeof reto !== "undefined"){
                reto[v[v.length-1]]=nobj;
        }
        return reto;
}


