(function () {
    var ginasApp = angular.module('ginas', ['ngMessages', 'ngResource', 'ui.bootstrap', 'ui.bootstrap.showErrors',
        'ui.bootstrap.datetimepicker', 'LocalStorageModule', 'ngTagsInput', 'xeditable', 'ui.select'])
        .config(function (showErrorsConfigProvider, localStorageServiceProvider, $locationProvider) {
            showErrorsConfigProvider.showSuccess(true);
            localStorageServiceProvider
                .setPrefix('ginas');
            $locationProvider.html5Mode({enabled: true, hashPrefix: '!'});
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
                Substance.chemical = {};
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

    ginasApp.controller("GinasController", function ($scope, $resource, $location, $anchorScroll, localStorageService, Substance, data) {
        var ginasCtrl = this;
        $scope.substance = Substance;
        $scope.empty ={};


        //date picker
        $scope.open = function($event) {
            $scope.status.opened = true;
        };


        $scope.status = {
            opened: false
        };

        //datepicker//

        //adds reference id//
        $scope.refLength= function() {
         if(!$scope.substance.references){
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

        $scope.scrollTo = function (prmElementToScrollTo) {
            $location.hash(prmElementToScrollTo);
            $anchorScroll();
        };

        $scope.enabled = false;
        $scope.mol = null;
        //$scope.unbind = localStorageService.bind($scope, 'mol');
        $scope.unbind = localStorageService.bind($scope, 'enabled');
        this.enabled = function getItem(key) {
            return localStorageService.get('enabled') || false;
        };
        this.numbers = true;
        localStorageService.set('enabled', $scope.enabled);

        this.passStructure = function (id) {
            localStorageService.set('structureid', id);
        };
        this.clearStructure = function () {
            console.log("destroy");
            return localStorageService.remove('structureid');
        };

        $scope.validate = function(obj, form, type){
            $scope.$broadcast('show-errors-check-validity');
            if(form.$valid) {
                if(this.substance[type]){
                    if(type=='references'){
                        obj.id = this.substance.references.length + 1;
                    }
                    this.substance[type].push(obj);
                }else {
                    this.substance[type] = [];
                    if(type=='references'){
                        obj.id =  1;
                    }
                    this.substance[type].push(obj);
                }
                $scope.$broadcast('show-errors-reset');
            }
            /*switch (this.substance.substanceClass) {
                case "chemical":

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
            }*/

        };

        $scope.toggle =function(el){
            if (el.selected) {
                el.selected = !el.selected;
            }else{
                el.selected=true;
            }
        };


        $scope.changeSelect = function(val){
            console.log(val);
          val=!val;
            console.log(val);
        };

        $scope.remove = function (obj, field) {
            var index = Substance[field].indexOf(obj);
            Substance[field].splice(index, 1);
        };

        $scope.reset =function(form){
            console.log($scope);
            form.$setPristine();
            console.log(form);
            $scope.$broadcast('show-errors-reset');
console.log($scope);
        };

        $scope.selected= false;

        $scope.info= function(scope, element){
            console.log($scope);
            console.log(scope);
            console.log(element);
            $scope.selected = !$scope.selected;
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

    ginasApp.factory('isDuplicate', function ($q, substanceFactory) {
        return function dupCheck(modelValue) {
            var deferred = $q.defer();
            substanceFactory.getSubstances(modelValue)
                .success(function (response) {
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
        var url = "app/api/v1/substances?filter=names.name='";
        var substanceFactory = {};
        substanceFactory.getSubstances = function (name) {
            return $http.get(url + name.toUpperCase() + "'", {headers: {'Content-Type': 'text/plain'}});
        };
        return substanceFactory;
    }]);

    ginasApp.service('data', function($http) {
        var options = {};
        var url = "app/api/v1/vocabularies?filter=domain='";

        this.load = function(field) {
            $http.get(url + field.toUpperCase() + "'", {headers: {'Content-Type': 'text/plain'}}).success(function(data) {
                options[field] = data.content[0].terms;
            });
        };

        this.search = function(field, query) {
            return _.chain(options[field] )
                .filter(function(x) { return !query || x.display.toLowerCase().indexOf(query.toLowerCase()) > -1; })
                .sortBy('display')
                .value();
        };
    });



    ginasApp.directive('moiety', function () {
        return {
            restrict: 'E',
            scope: {
                moiety: '='
            },
            templateUrl: "app/assets/ginas/templates/moietydisplay.html"
        };
    });

    ginasApp.directive('rendered', function ($http) {
        return {
            restrict: 'E',
            scope: {
                r: '='
            },
            link: function (scope, element) {
                $http({
                    method: 'GET',
                    url: 'app/structure/' + scope.r + '.svg',
                    headers: {'Content-Type': 'text/plain'}
                }).success(function (data) {
                    //console.log(data);
                    element.html(data);
                });
            }
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
                //console.log(scope);
                //console.log(scope.amount);
                //console.log(attrs);
            },
            template: '<div><span class="amt">{{value.nonNumericValue}} {{value.average}} ({{value.low}} to {{value.high}}) {{value.unit}}</span></div>'
        };
    });

    ginasApp.directive('subunit', function () {
        return {
            restrict: 'E',
            require: '^ngModel',
            scope: {
                sequence: '=',
            },
            template: '<textarea class="form-control string text-uppercase" rows="5" ng-model = "sequence" ng-change = "clean(sequence)" name="sequence"  placeholder="Sequence" title="sequence" id="sequence" required></textarea>',
        };
    });

    ginasApp.directive('sketcher', function ($http, $timeout, localStorageService, Substance) {
        return {
            restrict: 'E',
            require: "ngModel",
            scope: {
                structureQuery: '='
            },
            template: "<div id='sketcherForm' dataformat='molfile' ondatachange='setMol(this)'></div>",

            link: function (scope, element, attrs, ngModelCtrl) {
                sketcher = new JSDraw("sketcherForm");
                var url = window.strucUrl;//'/ginas/app/smiles';
                var structureid = (localStorageService.get('structureid') || false);
                if (!structureid) {
                    this.setMol = function () {
                        var mol = sketcher.getMolfile();
                        //console.log(mol);
                        $http({
                            method: 'POST',
                            url: url,
                            data: mol,
                            headers: {'Content-Type': 'text/plain'}
                        }).success(function (data) {

                            if (!Substance.chemical) {
                                Substance.chemical = {};
                            }
                            Substance.chemical.structure = data.structure;
                            Substance.chemical.moieties = data.moieties;
                            Substance.q = data.structure.smiles;
                        });

                        console.log(Substance);
                        console.log(element);
                    };
                }
                else {
                    $http({
                        method: 'GET',
                        url: '/ginas/app/api/v1/structures/' + structureid,
                        /*data: structureid,
                         headers: {'Content-Type': 'text/plain'}*/
                    }).success(function (data) {
                        console.log(data);
                        if (!Substance.chemical) {
                            Substance.chemical = {};
                        }
                        sketcher.setMolfile(data.molfile);
                        /*                        Substance.chemical.structure = data.structure;
                         Substance.chemical.moieties = data.moieties;
                         Substance.q = data.structure.smiles; */
                    });
                    /*

                     console.log(Substance);
                     console.log(element);
                     */


                    /*                    url ='/ginas/app/structure/'+ structureid+'.mol';
                     console.log(url);
                     $http({
                     method: 'GET',
                     url: url,
                     }).success(function (data) {
                     console.log(data);
                     /!*                        if (!Substance.chemical) {
                     Substance.chemical = {};
                     }
                     Substance.chemical.structure = data.structure;
                     Substance.chemical.moieties = data.moieties;
                     Substance.q = data.structure.smiles;*!/
                     sketcher.setMolfile(data);

                     });
                     console.log(Substance);
                     console.log(element);*/
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

    ginasApp.directive('export', function ($http) {
        return function (scope, element, attrs) {
            element.bind("click", function () {
                var modal = angular.element(document.getElementById('export-mol'));
                $http({
                    method: 'GET',
                    url: 'app/structure/' + scope.structureid + '.mol',
                    headers: {'Content-Type': 'text/plain'}
                }).success(function (data) {
                    modal.find('#inputExport').text(data);
                });
                modal.modal('show');

            });
        };
    });

    ginasApp.directive('molExport', function ($http) {
        return {
            restrict: 'E',
            templateUrl: "app/assets/ginas/templates/molexport.html"
        };
    });

    ginasApp.controller('ReferenceController', function ($scope, Substance, $rootScope) {
        $scope.editReference = null;
        $rootScope.refAdded = false;
        this.adding = false;
        this.editing = false;

        this.toggleEdit = function () {
            this.editing = !this.editing;
        };

        this.toggleAdd = function () {
            this.adding = !this.adding;
        };

        this.validate = function (obj) {
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

        this.reset = function () {
            $scope.ref = {};
            $scope.$broadcast('show-errors-reset');
        };
    });

    ginasApp.controller('NameController', function ($scope, Substance, $rootScope) {
        $scope.isEditing = false;
        $scope.addName = null;
        $scope.addingNames = true;
        $rootScope.uniqueName = true;

        this.addNames = function () {
            $scope.addingNames = !$scope.addingNames;
        };

        this.toggleEdit = function () {
            $scope.isEditing = !$scope.isEditing;
        };

        this.reset = function () {
            $scope.name = {};
            $scope.name.name = null;
            $scope.$broadcast('show-errors-reset');
        };

        this.validateName = function (name) {
            $scope.$broadcast('show-errors-check-validity');
            if ($scope.nameForm.$valid) {
                if (!Substance.names) {
                    Substance.names = [];
                }
                Substance.names.push(name);
                this.reset();
                //$rootScope.uniqueName = true;
            }
        };

        this.typeCheck = function () {
            if ($scope.name.type.value === 'of') {
                $rootScope.ofType = true;
                //$('#officialName').modal('show');
            } else {
                $rootScope.ofType = false;
            }
        };

        this.prefCheck = function () {
            console.log("preferred");
        };


        this.cancelEditing = function cancelEditing() {
            var index = Substance.names.indexOf($scope.editName);
            Substance.names[index] = $scope.tempCopy;
            $scope.isEditing = false;
            $scope.editedName = null;
        };

        this.setEditedName = function setEditedName(name) {
            $scope.editName = name;
            $scope.tempCopy = angular.copy(name);
        };

        this.updateName = function updateName(name) {
            var index = Substance.names.indexOf(name);
            Substance.names[index] = name;
            $scope.editName = null;
            $scope.isEditing = false;
        };

        this.removeName = function (name) {
            var index = Substance.names.indexOf(name);
            Substance.names.splice(index, 1);
        };

        this.editName = function (name) {
            Substance.names = angular.copy(name);
            $scope.editorEnabled = true;
            this.name = name;
        };

    });

    ginasApp.controller('OfficialNameController', function ($scope, Substance, $rootScope, $q, data) {




        $scope.isEditing = false;
        $scope.addName = null;
        $scope.addingNames = true;
        $rootScope.uniqueName = true;

        this.addNames = function () {
            $scope.addingNames = !$scope.addingNames;
        };

        this.toggleEdit = function () {
            $scope.isEditing = !$scope.isEditing;
        };

        this.reset = function () {
            $scope.name = {};
            $scope.name.name = null;
            $scope.$broadcast('show-errors-reset');
        };

        this.validateName = function (name, form) {
            $scope.$broadcast('show-errors-check-validity');
            console.log(form);
            console.log($scope.nameForm);
            console.log(form.name);
            console.log(name);
         //   if ($scope.nameForm.$valid) {
                if (!Substance.officialNames) {
                    Substance.officialNames = [];
                }
                Substance.officialNames.push(name);
                this.reset();
                //$rootScope.uniqueName = true;
          //  }
        };

        this.typeCheck = function () {
            if ($scope.name.type.value === 'of') {
                $rootScope.ofType = true;
                //$('#officialName').modal('show');
            } else {
                $rootScope.ofType = false;
            }
        };

        this.prefCheck = function () {
            console.log("preferred");
        };


        this.cancelEditing = function cancelEditing() {
            var index = Substance.officialNames.indexOf($scope.editName);
            Substance.officialNames[index] = $scope.tempCopy;
            $scope.isEditing = false;
            $scope.editedName = null;
        };

        this.setEditedName = function setEditedName(name) {
            $scope.editName = name;
            $scope.tempCopy = angular.copy(name);
        };

        this.updateName = function updateName(name) {
            var index = Substance.names.indexOf(name);
            Substance.officialNames[index] = name;
            $scope.editName = null;
            $scope.isEditing = false;
        };

        this.removeName = function (name) {
            var index = Substance.officialNames.indexOf(name);
            Substance.officialNames.splice(index, 1);
        };

        this.editName = function (name) {
            Substance.officialNames = angular.copy(name);
            $scope.editorEnabled = true;
            this.name = name;
        };

    });

    ginasApp.controller('CodeController', function ($scope, Substance) {
        $scope.isEditingCode = false;
        $scope.editCode = null;
        $scope.addingCodes = true;
        $scope.isEditing = false;

        this.addCodes = function () {
            $scope.addingCodes = !$scope.addingCodes;
        };

        this.toggleEditCode = function () {
            console.log("editing)");
            $scope.isEditingCode = !$scope.isEditingCode;
        };

        this.reset = function () {
            $scope.code = {};
            $scope.code.code = null;
            $scope.$broadcast('show-errors-reset');
        };

        this.validateCode = function (code) {
            $scope.$broadcast('show-errors-check-validity');
            if ($scope.codeForm.$valid) {
                //new array if object doesn't already have one
                if (!Substance.codes) {
                    //console.log("new array");
                    Substance.codes = [];
                }
                Substance.codes.push(code);
                this.reset();
            }
        };


        this.setEditedCode = function setEditedCode(code) {
            console.log(code);
            $scope.editCode = code;
            $scope.tempCopy = angular.copy(code);
        };

        this.updateCode = function (code) {
            var index = Substance.codes.indexOf(code);
            Substance.codes[index] = code;
            $scope.editCode = null;
            $scope.isEditingCode = false;
        };

        this.removeCode = function (code) {
            var index = Substance.codes.indexOf(code);
            Substance.codes.splice(index, 1);
        };
    });

    ginasApp.controller('NoteController', function ($scope, Substance) {
        $scope.isEditingNote = false;
        $scope.editNote = null;

        this.addNotes = function () {
            $scope.addingNotes = !$scope.addingNotes;
        };

        this.toggleEditNote = function () {
            console.log("editing)");
            $scope.isEditingNote = !$scope.isEditingNote;
        };

        this.reset = function () {
            $scope.note = {};
            $scope.$broadcast('show-errors-reset');
        };

        this.validateNote = function (note) {
            $scope.$broadcast('show-errors-check-validity');
            if ($scope.noteForm.$valid) {
                //new array if object doesn't already have one
                if (!Substance.notes) {
                    console.log("new array");
                    Substance.notes = [];
                }
                Substance.notes.push(note);
                this.reset();
            }
        };

        this.setEditedNote = function setEditedNote(note) {
            console.log(note);
            $scope.editNote = note;
            $scope.tempCopy = angular.copy(note);
        };

        this.updateNote = function (note) {
            var index = Substance.notes.indexOf(note);
            Substance.notes[index] = note;
            $scope.editNote = null;
            $scope.isEditingNote = false;
        };

        this.removeNote = function (note) {
            var index = Substance.notes.indexOf(note);
            Substance.notes.splice(index, 1);
        };
    });
    ginasApp.controller('PropertyController', function ($scope, Substance) {
        $scope.isEditingProperty = false;
        $scope.editProperty = null;

        this.addProperties = function () {
            $scope.addingProperties = !$scope.addingProperties;
        };

        this.testsomething = function () {
            return "TTTTTTTTTTTTTTTTTTTTTT";
        };

        this.toggleEditProperty = function () {
            console.log("editing)");
            $scope.isEditingProperty = !$scope.isEditingProperty;
        };

        this.reset = function () {
            $scope.property = {};
            $scope.$broadcast('show-errors-reset');
        };

        this.validateProperty = function (property) {
            $scope.$broadcast('show-errors-check-validity');
            if ($scope.propertyForm.$valid) {
                //new array if object doesn't already have one
                if (!Substance.properties) {
                    console.log("new array");
                    Substance.properties = [];
                }
                Substance.properties.push(property);
                this.reset();
            }
        };

        this.setEditedProperty = function setEditedProperty(property) {
            console.log(property);
            $scope.editProperty = property;
            $scope.tempCopy = angular.copy(property);
        };

        this.updateProperty = function (property) {
            var index = Substance.properties.indexOf(property);
            Substance.properties[index] = property;
            $scope.editProperty = null;
            $scope.isEditingProperty = false;
        };

        this.removeProperty = function (property) {
            var index = Substance.properties.indexOf(property);
            Substance.properties.splice(index, 1);
        };
    });

    ginasApp.controller('StructureController', function ($scope, $http, localStorageService, Substance) {
        $scope.isEditingStructure = false;
        $scope.editStructure = null;
        $scope.noStructure = true;
        this.adding = false;
        this.editing = false;
        localStorageService.remove('mol');

        this.toggleEdit = function () {
            this.editing = !this.editing;
        };

        this.toggleAdd = function () {
            this.adding = !this.adding;
        };

        $scope.addStructure = function () {
            $scope.addingStructure = !$scope.addingStructure;

        };

        this.resolveMol = function (structure) {
            console.log("resolving mol file");
            //sketcher.setMolfile(structure.molfile);
            var url = window.strucUrl;//'/ginas/app/smiles';
            //var mol = structure.molfile;
            //   console.log(mol);
            //   mol = "\n"+mol;
            console.log(structure.molfile);
            $http({
                method: 'POST',
                url: url,
                data: structure.molfile,
                headers: {'Content-Type': 'text/plain'}
            }).success(function (data) {
                sketcher.setMolfile(data.structure.molfile);
                console.log(structure);
                $scope.structure = data.structure;
                console.log(structure);
            });
        };
    });

    ginasApp.controller('SubunitController', function ($scope, Substance) {
        $scope.editReference = null;
        this.adding = false;
        this.editing = false;
        this.display = [];
        this.subunit = {};
        this.toggleEdit = function () {
            this.editing = !this.editing;
        };

        this.toggleAdd = function () {
            this.adding = !this.adding;
        };

        this.clean = function (sequence) {
            console.log("clean");
            return sequence.replace(/[^A-Za-z]/g, '');
        };

        this.parseSubunit = function (sequence) {
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

        this.validate = function (obj) {
            console.log(obj);
        };

        $scope.validateSubunit = function (subunit) {
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

        $scope.setEditedSubunit = function (subunit) {
            console.log("clicked");
            $scope.editSubunit = subunit;
            $scope.tempCopy = angular.copy(subunit);
        };

        $scope.updateSubunit = function (subunit) {
            var index = $scope.substance.protein.subunits.indexOf(subunit);
            $scope.substance.protein.subunits[index] = subunit;
            $scope.editSubunit = null;
            $scope.isEditingSubunit = false;
        };

        $scope.removeSubunit = function (subunit) {
            var index = $scope.substance.protein.subunits.indexOf(subunit);
            $scope.substance.protein.subunits.splice(index, 1);
        };
    });

    ginasApp.controller('DetailsController', function ($scope, Substance) {
        $scope.protein = null;
        this.added = false;
        this.editing = false;

        this.toggleAdd = function () {
            this.added = !this.added;
        };

        this.toggleEdit = function () {
            this.editing = !this.editing;
            this.added = false;
        };

        this.validate = function (obj) {
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

        this.setEdited = function (obj) {
            $scope.editObj = obj;
            $scope.tempCopy = angular.copy(obj);
        };

        this.reset = function () {
            $scope.protein = {};
            $scope.$broadcast('show-errors-reset');
        };

    });

    ginasApp.controller('StrucSearchController', function ($scope) {
        $scope.select = ['Substructure', 'Similarity'];
        $scope.type = 'Substructure';
        $scope.cutoff = 0.8;
        $scope.q = "";

        $scope.controllerFunction = function (valueFromDirective) {
            console.log(valueFromDirective);
        };


    });

    ginasApp.controller('RelationshipController', function ($scope, $rootScope, substanceFactory) {

        this.getSubstances = function (name) {
            console.log(name);
            substanceFactory.getSubstances(name)
                .success(function (response) {
                    console.log(response);
                    if (response.count >= 1) {
                        console.log("adding data");
                        $rootScope.data = response.content;
                    } else {
                        console.log("no results");
                    }
                })
                .error(function (error) {
                    $scope.status = 'Unable to load substance data: ' + error.message;
                });
        };
    });

    ginasApp.controller('TypeaheadController', function ($scope) {

        var nameTypeahead = new Bloodhound({
            datumTokenizer: function (d) {
                return Bloodhound.tokenizers.whitespace(d.key);
            },
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: {
                wildcard: 'QUERY',
                url: '/ginas/app/api/v1/suggest/Name?q=QUERY'
            }
        });
        nameTypeahead.initialize();
        $scope.nameDataSource = {
            name: 'Name',
            displayKey: 'key',
            source: nameTypeahead.ttAdapter(),
            templates: {
                header: '<h4><span class="label label-warning">Name</span></h4>'
            }
        };

        $scope.nameOptions = {
            hint: true,
            highlight: true,
            minLength: 2
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















angular.module('ui.bootstrap.tpls').controller('ProgressDemoCtrl', function ($scope,  $http, $timeout) {
          $scope.max = 100;
          $scope.monitor = false;
          $scope.mess = "";
          $scope.dynamic = 0;
          $scope.stat = {
            recordsPersistedSuccess:0,
            recordsProcessedSuccess:0,
            recordsExtractedSuccess:0
            };
          $scope.refresh = function(id, pollin) {
            $scope.id=id;
            var responsePromise = $http.get("/ginas/app/api/v1/jobs/" + id + "/");
                responsePromise.success(function(data, status, headers, config) {
                    //$scope.myData.fromServer = data.title;
                    if(data.status=="RUNNING"){
                        $scope.mclass="progress-striped active";
                    }else{
                        $scope.mclass="";
                        $scope.monitor=false;
                        $scope.mess="Process complete.";
                    }
                    $scope.max = data.statistics.totalRecords.count;
                    $scope.dynamic = data.statistics.recordsPersistedSuccess +
                          data.statistics.recordsPersistedFailed +
                          data.statistics.recordsProcessedFailed +
                          data.statistics.recordsExtractedFailed;
                    $scope.max = data.statistics.totalRecords.count;
                    $scope.stat = data.statistics;
                    $scope.allExtracted = $scope.max ;
                    $scope.allPersisted = $scope.max ;
                    $scope.allProcessed = $scope.max ;
                    
                });
                responsePromise.error(function(data, status, headers, config) {
                    alert("AJAX failed!");
                });
                if(pollin){
                    $scope.monitor=true;
                    $scope.mess="Polling ...";
                    poll();
                }
          };
          $scope.stopMonitor = function(){
                $scope.monitor=false;
                $scope.mess="";
          };
          var poll = function() {
                $timeout(function() {
                    console.log("they see me pollin'");
                    
                    $scope.refresh($scope.id,false);
                    if($scope.monitor)poll();
                }, 1000);
            }; 
          
          
        });




})();