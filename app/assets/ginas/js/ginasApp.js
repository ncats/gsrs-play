(function() {
    var ginasApp = angular.module('ginas', ['ngMessages', 'ngResource', 'ui.bootstrap', 'ui.bootstrap.showErrors',
        'ui.bootstrap.datetimepicker', 'LocalStorageModule', 'ngTagsInput', 'xeditable', 'ui.select'
    ])
        .config(function(showErrorsConfigProvider, localStorageServiceProvider, $locationProvider) {
            showErrorsConfigProvider.showSuccess(true);
            localStorageServiceProvider
                .setPrefix('ginas');
            $locationProvider.html5Mode({
                enabled: true,
                hashPrefix: '!'
            });
        });

    ginasApp.filter('range', function() {
        return function(input, min, max) {
            min = parseInt(min); //Make string input int
            max = parseInt(max);
            for (var i = min; i < max; i++)
                input.push(i);
            return input;
        };
    });

    ginasApp.factory('Substance', function() {
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


    ginasApp.factory('lookup', function() {
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

        lookup.getFromName = function(field, val) {
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

    ginasApp.controller("GinasController", function($scope, $resource, $location, $modal, $http, $anchorScroll, localStorageService, Substance, data, substanceSearch, substanceIDRetriever, lookup) {

        $scope.toFormSubstance = function(apiSub) {

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



            apiSub = $scope.expandCV(apiSub, "");
            apiSub = $scope.splitNames(apiSub);

            var references = {};
            for(var v in apiSub.references){
                references[apiSub.references[v].uuid]=apiSub.references[v];
                apiSub.references[v].id=v-1+2;
            }
            apiSub = $scope.expandReferences(apiSub,references,0);



            return apiSub;
        };

        var ginasCtrl = this;
        //localStorageService.set('substance', Substance);

        var edit = localStorageService.get('editID');
        if (edit) {
            substanceIDRetriever.getSubstances(edit).then(function(data) {
                var sub = $scope.toFormSubstance(data);
                //   sub = $scope.expandCV(data);
                //  console.log(angular.copy(sub));
                $scope.substance = sub;

                localStorageService.remove('editID');
            });
        } else {
            $scope.substance = Substance;
        }

        /*        function setdata($scope){
         console.log("getting substance");

         }*/

        /*
         $scope.substance = $scope.getSubstance();
         */

        $scope.select = ['Substructure', 'Similarity'];
        $scope.type = 'Substructure';
        $scope.cutoff = 0.8;

        //date picker
        $scope.open = function($event) {
            $scope.status.opened = true;
        };

        $scope.status = {
            opened: false
        };

        //datepicker//

        //adds reference id//
        $scope.refLength = function() {
            if (!$scope.substance.references) {
                return 1;
            }
            return $scope.substance.references.length + 1;
        };
        //add reference id//

        //populates tag fields
        $scope.loadItems = function(field, $query) {
            data.load(field);
            return data.search(field, $query);
        };
        //populates tag fields//

        $scope.retrieveItems = function(field, $query) {
            data.load(field);
            return data.lookup(field, $query);
        };

        $scope.scrollTo = function(prmElementToScrollTo) {
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
        $scope.passStructure = function(id) {
            localStorageService.set('structureid', id);
        };
        $scope.clearStructure = function() {
            localStorageService.remove('structureid');
        };
        ///


        //main submission method
        $scope.validate = function(obj, form, type) {
            $scope.$broadcast('show-errors-check-validity');
            if (form.$valid) {
                if (this.substance[type]) {
                    if (type == 'references') {
                        if(typeof obj.uuid == "undefined"){
                            obj.uuid = uuid();
                        }
                        obj.id = this.substance.references.length + 1;
                    }
                    this.substance[type].push(obj);
                } else {
                    this.substance[type] = [];
                    if (type == 'references') {
                        if (typeof obj.uuid == "undefined") {
                            obj.uuid = uuid();
                        }
                        obj.id = 1;
                    }
                    this.substance[type].push(obj);
                }
                $scope.$broadcast('show-errors-reset');
            }
        };

        $scope.toggle = function(el) {
            if (el.selected) {
                el.selected = !el.selected;
            } else {
                el.selected = true;
            }
        };

        $scope.splitNames = function(sub) {
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

        $scope.changeSelect = function(val) {
            console.log(val);
            val = !val;
            console.log(val);
        };

        $scope.remove = function(obj, field) {
            var index = Substance[field].indexOf(obj);
            Substance[field].splice(index, 1);
        };

        $scope.reset = function(form) {
            console.log($scope);
            form.$setPristine();
            console.log(form);
            $scope.$broadcast('show-errors-reset');
            console.log($scope);
        };

        $scope.selected = false;

        $scope.info = function(scope, element) {
            console.log($scope);
            console.log(scope);
            console.log(element);
            $scope.selected = !$scope.selected;
        };

        $scope.openModal = function(type) {
            var template;
            switch (type) {
                case "reference":
                    template = "";
                    break;
                case "structuresearch":
                    template = 'app/assets/ginas/templates/substanceselector.html';
                    break;
            }
            var modalInstance = $modal.open({
                animation: true,
                //templateUrl: 'app/assets/ginas/templates/substanceselector.html',
                templateUrl: template,
                // windowTemplateUrl: 'app/assets/ginas/templates/modal-window.html',
                controller: 'ModalController',
                size: 'lg'
            });

        };

        $scope.fetch = function($query) {
            console.log($query);
            return substanceSearch.load($query);
            //return substanceSearch.search(field, $query);
        };

        $scope.expandCV = function(sub, path) {

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
                    console.log("##### display:" + w);
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

        $scope.expandReferences = function(sub, referenceMap, depth) {
            for (var v in sub) {
                if(depth>0) {
                    if (v === "references") {
                        for (var r in sub[v]) {
                            sub[v][r] = referenceMap[sub[v][r]];
                        }
                    }
                }
                if(typeof sub[v] === "object"){
                    $scope.expandReferences(sub[v],referenceMap, depth+1);
                }
            }
            return sub;
        };

        $scope.collapseReferences = function(sub, depth) {
            for (var v in sub) {
                if(depth>0) {
                    if (v === "references") {
                        for (var r in sub[v]) {
                            //console.log(r + " is a reference");
                            sub[v][r] = sub[v][r].uuid;
                        }
                    }
                }
                if(typeof sub[v] === "object"){
                    $scope.collapseReferences(sub[v], depth+1);
                }
            }
            return sub;
        };


        $scope.flattenCV = function(sub) {
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

        $scope.isCV = function(ob) {
            if (typeof ob !== "object") return false;
            if (ob === null) return false;
            if (typeof ob.value !== "undefined") {
                if (typeof ob.display !== "undefined") {
                    return true;
                }
            }
            return false;
        };

        $scope.submit = function() {
            var sub = angular.copy($scope.substance);
            if (sub.officialNames || sub.unofficialNames) {
                for (var n in sub.officialNames) {
                    var name = sub.officialNames[n];
                    name.type = "of";
                }
                sub.names = sub.officialNames.concat(sub.unofficialNames);
                delete sub.officialNames;
                delete sub.unofficialNames;
            }
            if (sub.q) {
                delete sub.q;
            }
            if (sub.subref) {
                delete sub.subref;
            }
            data = $scope.flattenCV(JSON.parse(JSON.stringify(sub)));
            data = $scope.collapseReferences(data,0);
            console.log(data);
            $http.post('app/submit', data).success(function() {
                console.log("success");
                alert("submitted!");
            });
        };

        $scope.movesubref = function(relationship) {
            console.log(relationship);
            if (Substance.subref) {
                relationship.subref = Substance.subref;
                console.log(relationship);
                delete Substance.subref;
            }
        };

        $scope.submitpaster = function(input) {
            console.log(input);
            var sub = JSON.parse(input);
            $scope.substance = sub;
            console.log($scope);
        };

        $scope.setEditId = function(editid) {
            localStorageService.set('editID', editid);
        };


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

    ginasApp.directive('scrollSpy', function($timeout) {
        return function(scope, elem, attr) {
            scope.$watch(attr.scrollSpy, function(value) {
                $timeout(function() {
                    elem.scrollspy('refresh');
                }, 200);
            }, true);
        };
    });

    ginasApp.directive('duplicate', function(isDuplicate) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function(scope, element, attrs, ngModel) {
                ngModel.$asyncValidators.duplicate = isDuplicate;
            }
        };
    });

    ginasApp.factory('isDuplicate', function($q, substanceFactory) {
        return function dupCheck(modelValue) {
            var deferred = $q.defer();
            substanceFactory.getSubstances(modelValue)
                .success(function(response) {
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

    ginasApp.factory('substanceFactory', ['$http', function($http) {
        var url = "app/api/v1/substances?filter=names.name='";
        var substanceFactory = {};
        substanceFactory.getSubstances = function(name) {
            return $http.get(url + name.toUpperCase() + "'", {
                headers: {
                    'Content-Type': 'text/plain'
                }
            });
        };
        return substanceFactory;
    }]);

    ginasApp.service('substanceIDRetriever', ['$http', function($http) {
        var url = "app/api/v1/substances(";
        var substanceIDRet = {
            getSubstances: function(editId) {
                console.log(editId);
                var promise = $http.get(url + editId + ")?view=full", {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function(response) {
                    console.log(response);
                    return response.data;
                });
                return promise;
            }
        };
        return substanceIDRet;
    }]);

    ginasApp.service('substanceRetriever', ['$http', function($http) {
        var url = "app/api/v1/substances?filter=names.name='";
        var substanceRet = {
            getSubstances: function(name) {
                var promise = $http.get(url + name.toUpperCase() + "'", {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function(response) {
                    return response.data;
                });
                return promise;
            }
        };
        return substanceRet;
    }]);

    ginasApp.service('data', function($http) {
        var options = {};
        var url = "app/api/v1/vocabularies?filter=domain='";

        this.load = function(field) {
            $http.get(url + field.toUpperCase() + "'", {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function(data) {
                console.log(data);
                options[field] = data.content[0].terms;
            });
        };

        this.search = function(field, query) {
            return _.chain(options[field])
                .filter(function(x) {
                    return !query || x.display.toLowerCase().indexOf(query.toLowerCase()) > -1;
                })
                .sortBy('display')
                .value();
        };
        this.lookup = function(field, query) {
            console.log(options);
            return _.chain(options[field])
                .filter(function(x) {
                    return !query || x.value.toLowerCase().indexOf(query.toLowerCase()) > -1;
                })
                .sortBy('value')
                .value();
        };
    });

    ginasApp.service('substanceSearch', function($http) {
        var options = {};
        var url = "app/api/v1/suggest/Name?q=";

        this.load = function(field) {
            $http.get(url + field.toUpperCase(), {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function(response) {
                options.data = response;
            });
        };

        this.search = function(query) {
            return options;
        };
    });

    ginasApp.directive('rendered', function($http) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                id: '='
                /*                size: '=',
                 amap :'='*/

            },
            link: function(scope, element) {
                $http({
                    method: 'GET',
                    url: 'app/img/' + scope.id + '.svg',
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function(data) {
                    element.html(data);
                });
            }
        };
    });

    ginasApp.directive('amount', function() {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            link: function(scope, element, attrs) {

            },
            template: '<div><span class="amt">{{value.nonNumericValue}} {{value.average}} ({{value.low}} to {{value.high}}) {{value.unit}}</span></div>'
        };
    });

    ginasApp.directive('subunit', function() {
        return {
            restrict: 'E',
            require: '^ngModel',
            scope: {
                sequence: '=',
            },
            template: '<textarea class="form-control string text-uppercase" rows="5" ng-model = "sequence" ng-change = "clean(sequence)" name="sequence"  placeholder="Sequence" title="sequence" id="sequence" required></textarea>',
        };
    });

    ginasApp.directive('sketcher', function($http, $timeout, localStorageService, Substance) {
        return {
            restrict: 'E',
            require: "ngModel",
            scope: {
                structureQuery: '='
            },
            template: "<div id='sketcherForm' dataformat='molfile' ondatachange='setMol(this)'></div>",

            link: function(scope, element, attrs, ngModelCtrl) {
                sketcher = new JSDraw("sketcherForm");
                var url = window.strucUrl; //'/ginas/app/smiles';
                var structureid = (localStorageService.get('structureid') || false);
                if (!structureid) {
                    this.setMol = function() {
                        var mol = sketcher.getMolfile();
                        $http({
                            method: 'POST',
                            url: url,
                            data: mol,
                            headers: {
                                'Content-Type': 'text/plain'
                            }
                        }).success(function(data) {
                            Substance.structure = data.structure;
                            Substance.moieties = data.moieties;
                            Substance.q = data.structure.smiles;
                            console.log(Substance);
                        });
                    };
                } else {
                    $http({
                        method: 'GET',
                        url: '/ginas/app/api/v1/structures/' + structureid
                    }).success(function(data) {
                        console.log(data);

                        sketcher.setMolfile(data.molfile);
                        Substance.q = data.smiles;
                        console.log(Substance);
                        localStorageService.remove('structureid');
                    });
                }
            }
        };
    });

    ginasApp.directive('switch', function() {
        return {
            restrict: 'AE',
            replace: true,
            transclude: true,
            template: function(element, attrs) {
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

    ginasApp.directive('exportButton', function() {
        return {
            restrict: 'E',
            scope: {
                structureid: '='
            },
            template: '<button type="button" class="btn btn-primary" structureid = structureid  export><i class="fa fa-external-link chem-button"></i></button>'
        };
    });

    ginasApp.directive('export', function($http) {
        return function(scope, element, attrs) {
            element.bind("click", function() {
                var modal = angular.element(document.getElementById('export-mol'));
                $http({
                    method: 'GET',
                    url: 'app/structure/' + scope.structureid + '.mol',
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function(data) {
                    modal.find('#inputExport').text(data);
                });
                modal.modal('show');

            });
        };
    });

    ginasApp.directive('molExport', function($http) {
        return {
            restrict: 'E',
            templateUrl: "app/assets/ginas/templates/molexport.html"
        };
    });

    ginasApp.controller('SubunitController', function($scope, Substance) {
        $scope.editReference = null;
        this.adding = false;
        this.editing = false;
        this.display = [];
        this.subunit = {};
        this.toggleEdit = function() {
            this.editing = !this.editing;
        };

        this.toggleAdd = function() {
            this.adding = !this.adding;
        };

        this.clean = function(sequence) {
            console.log("clean");
            return sequence.replace(/[^A-Za-z]/g, '');
        };

        this.parseSubunit = function(sequence) {
            console.log(sequence);
            var split = sequence.replace(/[^A-Za-z]/g, '').split('');
            var display = [];
            console.log(split);
            var invalid = ['B', 'J', 'O', 'U', 'X', 'Z'];
            for (var i in split) {
                var obj = {};
                console.log(split[i]);
                var valid = dojo.indexOf(invalid, split[i].toUpperCase());
                console.log(valid);
                if (valid >= 0) {
                    obj.value = split[i];
                    obj.valid = false;
                    display.push(obj);
                    obj = {};
                } else {
                    obj.value = split[i];
                    obj.valid = true;
                    display.push(obj);
                    obj = {};
                }

            }

            this.display = display;
            console.log(display);
            return display;
        };

        /*        this.validate = function (obj) {
         $scope.$broadcast('show-errors-check-validity');
         if ($scope.refForm.$valid) {
         //new array if object doesn't already have one
         if (!Substance.references) {
         Substance.references = [];
         }
         obj.id = Substance.references.length + 1;
         Substance.references.push(obj);
         $scope.ref = {};
         $rootScope.refAdded = true;
         }
         };*/

        this.validate = function(obj) {
            console.log(obj);
        };

        $scope.validateSubunit = function(subunit) {
            console.log(subunit);


        };


        /*
         $scope.validateSubunit = function (subunit) {
         console.log(subunit);
         $scope.editorEnabled = false;
         //new array if object doesn't already have one
         if (!$scope.substance.protein.subunits) {
         console.log("new array");
         $scope.substance.protein.subunits = [];
         }
         var j = subunit.sequence.length / 10;
         for (var i = 0; i < subunit.sequence.length / 10; i++) {
         console.log("Start: " + (i * 10) + subunit.sequence.substring((i * 10), ((i + 1) * 10)) + " end: " + ((i + 1) * 10));

         }

         //  console.log(subunit.sequence.split(''));
         $scope.substance.protein.subunits.push(subunit);
         $scope.subunit = {};
         console.log($scope.substance);

         };
         */

        $scope.setEditedSubunit = function(subunit) {
            console.log("clicked");
            $scope.editSubunit = subunit;
            $scope.tempCopy = angular.copy(subunit);
        };

        $scope.updateSubunit = function(subunit) {
            var index = $scope.substance.protein.subunits.indexOf(subunit);
            $scope.substance.protein.subunits[index] = subunit;
            $scope.editSubunit = null;
            $scope.isEditingSubunit = false;
        };

        $scope.removeSubunit = function(subunit) {
            var index = $scope.substance.protein.subunits.indexOf(subunit);
            $scope.substance.protein.subunits.splice(index, 1);
        };
    });

    ginasApp.controller('DetailsController', function($scope, Substance) {
        $scope.protein = null;
        this.added = false;
        this.editing = false;

        this.toggleAdd = function() {
            this.added = !this.added;
        };

        this.toggleEdit = function() {
            this.editing = !this.editing;
            this.added = false;
        };

        this.validate = function(obj) {
            console.log(obj);
            $scope.$broadcast('show-errors-check-validity');
            if ($scope.detForm.$valid) {
                $scope.editorEnabled = false;
                Substance.protein.proteinType = obj.proteinType;
                Substance.protein.proteinSubType = obj.proteinSubType;
                Substance.protein.sequenceOrigin = obj.sequenceOrigin;
                Substance.protein.sequenceType = obj.sequenceType;
                this.added = true;
                $scope.detailsSet = true;
            }
        };

        this.setEdited = function(obj) {
            $scope.editObj = obj;
            $scope.tempCopy = angular.copy(obj);
        };

        this.reset = function() {
            $scope.protein = {};
            $scope.$broadcast('show-errors-reset');
        };

    });


    ginasApp.controller('DiverseController', function($scope, Substance, $rootScope) {
        this.adding = true;

        this.toggleEdit = function() {
            this.editing = !this.editing;
        };

        this.toggleAdd = function() {
            this.adding = !this.adding;
        };

        this.reset = function() {
            $scope.diverse = {};
            $scope.$broadcast('show-errors-reset');
        };

        this.validate = function(obj) {
            $scope.$broadcast('show-errors-check-validity');
            if ($scope.diverseForm.$valid) {
                Substance.structurallyDiverse.sourceMaterialClass = obj.sourceMaterialClass;
                Substance.structurallyDiverse.sourceMaterialType = obj.sourceMaterialType;
                Substance.structurallyDiverse.sourceMaterialState = obj.sourceMaterialState;
                this.toggleAdd();
            }
        };

        this.setEdited = function(obj) {
            $scope.editObj = obj;
            $scope.tempCopy = angular.copy(obj);
        };

        this.update = function(reference) {
            console.log(reference);
            var index = Substance.references.indexOf(reference);
            Substance.references[index] = reference;
            $scope.editObj = null;
            this.toggleEdit();
        };

        this.remove = function(reference) {
            var index = Substance.references.indexOf(reference);
            Substance.references.splice(index, 1);
        };


    });

    ginasApp.controller('ProgressJobController', function($scope, $http, $timeout) {
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
        $scope.init = function(id, pollin, status) {
            $scope.status = status;
            $scope.details = pollin;
            $scope.refresh(id, pollin);
        };
        $scope.refresh = function(id, pollin) {
            $scope.id = id;
            var responsePromise = $http.get("/ginas/app/api/v1/jobs/" + id + "/");
            responsePromise.success(function(data, status, headers, config) {
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
                        $scope.mess = "Process complete.";
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

            });
            responsePromise.error(function(data, status, headers, config) {
                //alert("AJAX failed!");
            });
            if (pollin) {
                $scope.monitor = true;
                $scope.mess = "Polling ...";
                poll();
            }
        };
        $scope.stopMonitor = function() {
            $scope.monitor = false;
            $scope.mess = "";
        };
        var poll = function() {
            $timeout(function() {
                //console.log("they see me pollin'");

                $scope.refresh($scope.id, false);
                if ($scope.monitor) poll();
            }, 1000);
        };


    });


    ginasApp.controller('ModalController', function($scope, Substance, $modalInstance, substanceSearch, substanceRetriever) {
        $scope.ok = function() {
            console.log("ok");
            $modalInstance.close();
        };

        $scope.cancel = function() {
            console.log("cancel");
            $modalInstance.dismiss('cancel');
        };

        $scope.fetch = function($query) {
            console.log($query);
            substanceSearch.load($query);
            return substanceSearch.search($query);
        };

        $scope.retrieveSubstance = function(tag) {
            substanceRetriever.getSubstances(tag.key).then(function(data) {
                console.log(data.content[0].structure);
                $scope.relationship.subref.refuuid = data.content[0].structure.id;
                $scope.relationship.subref.refPname = data.content[0].name;
                $scope.relationship.subref.approvalID = data.content[0].approvalID;
                $scope.relationship.subref.substanceClass = "reference";
            });
        };
        $scope.clear = function() {
            $scope.relationship.subref = {};
        };

    });

    ginasApp.controller('SubstanceListController', function($scope) {
        $scope.bigview = false;
        $scope.initialized = false;
        $scope.toggle = function() {
            $scope.initialized = true;
            $scope.bigview = !$scope.bigview;
        };

    });

    ginasApp.factory('SDFFields', function() {
        var SDFFields = {};
    });


    ginasApp.controller('SDFieldController', function($scope) {
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

        $scope.init = function(path, model) {
            $scope.path = path;
            $scope.checkModel = model;
            //console.log(model);
        };

        $scope.$watch('radio.model', function(newVal, oldVal) {
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

    ginasApp.controller('SubstanceSelectorController', function($scope, $modal, Substance) {


        $scope.open = function(size) {

            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'substanceSelector.html',
                controller: 'SubstanceSelectorInstanceController',
                size: 'lg'

            });

            modalInstance.result.then(function(selectedItem) {
                var subref = {};
                console.log(selectedItem);
                subref.refuuid = selectedItem.uuid;
                subref.refPname = selectedItem.name;
                subref.approvalID = selectedItem.approvalID;
                subref.substanceClass = "reference";
                Substance.subref = subref;
            });
        };


    });

    // Please note that $modalInstance represents a modal window (instance) dependency.
    // It is not the same as the $modal service used above.

    ginasApp.controller('SubstanceSelectorInstanceController', function($scope, $modalInstance, $http) {

        //$scope.items = items;
        $scope.results = {};
        $scope.selected = {

        };

        $scope.top = 4;
        $scope.testb = 0;

        $scope.select = function(item) {
            var subref = {};

            console.log(item);
            $modalInstance.close(item);
        };

        $scope.ok = function() {
            $modalInstance.close($scope.selected.item);
        };

        $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
        };

        $scope.fetch = function(term, skip) {
            var url = "/ginas/app/api/v1/substances/search?q=" +
                term + "*&top=" + $scope.top + "&skip=" + skip;
            console.log(url);
            var responsePromise = $http.get(url);

            responsePromise.success(function(data, status, headers, config) {
                console.log(data);
                $scope.results = data;
            });

            responsePromise.error(function(data, status, headers, config) {
                //alert("AJAX failed!");
            });
        };

        $scope.search = function() {
            $scope.fetch($scope.term, 0);
        };




        $scope.nextPage = function() {
            console.log($scope.results.skip);
            $scope.fetch($scope.term, $scope.results.skip + $scope.results.top);
        };
        $scope.prevPage = function() {
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

