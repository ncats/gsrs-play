(function () {
    var ginasApp = angular.module('ginas', ['ngMessages','ng-resource', 'ui.bootstrap.showErrors', 'ui.bootstrap.datetimepicker'])
        .config(function ($locationProvider, showErrorsConfigProvider) {
            $locationProvider.html5Mode({
                enabled : true
            });
            showErrorsConfigProvider.showSuccess(true);
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

    ginasApp.factory('Substance', function ($location) {
        var Substance = {};
        var substanceClass = $location.$$search.kind;
        Substance.substanceClass = substanceClass;
        switch (substanceClass) {
            case "Chemical":
                Substance.chemical = {};
                break;
            case "Protein":
                Substance.protein = {};
                var subunit = "";
                break;
            default:
                console.log('invalid substance class');
                break;
        }
        return Substance;
    });

    function GinasCtrl(Substance) {
        var ginasCtrl = this;
        ginasCtrl.substance = Substance;
    }

    ginasApp.controller("GinasCtrl", GinasCtrl);

    ginasApp.directive('datepicker', function() {
        return {
            restrict: 'A',
            require : 'ngModel',
            link : function (scope, element, attrs, ngModelCtrl) {
                $(function(){
                    $(element).datepicker({
                        dateFormat:'dd/mm/yy',
                        onSelect:function (date) {
                            scope.$apply(function () {
                                ngModelCtrl.$setViewValue(date);
                            });
                        }
                    });
                });
            }
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

    ginasApp.factory('isDuplicate', function ($http, $q) {
        return function dupCheck(modelValue) {
            var deferred = $q.defer();
            $http.get("app/api/v1/substances?filter=names.name='" + modelValue.toUpperCase() + "'", {headers: {'Content-Type': 'text/plain'}}).then(
                function (response) {
                    if (response.data.count >= 1) {
                        deferred.reject();
                    } else {
                        deferred.resolve();
                    }
                });

            return deferred.promise;
        };
    });

/*    ginasApp.directive('subunit', function(){
        var editTemplate = '<textarea class="form-control string text-uppercase" ng-show="editing" ng-dblclick="edit()" rows="5" ng-model = "subunit.sequence" name="sequence"  placeholder="Sequence" title="sequence" id="sequence" required></textarea>';
        var displayTemplate = '<div class="panel-body" ng-hide="editing" ng-dblclick="edit()"><div class = "row string text-uppercase" >preview</div></div>';
        return {
            restrict: "E",
            //template : '<textarea class="form-control string text-uppercase" rows="5" ng-model = "subunit.sequence" ng-model-options="{ debounce: 1000 }" ng-change = "SubunitCtrl.clean(subunit.sequence); SubunitCtrl.parseSubunit(subunit.sequence)" name="sequence"  placeholder="Sequence" title="sequence" id="sequence" ng-transclude required></textarea>',
            compile:function(tElement, tAttrs, transclude){
                console.log(tElement);
                var displayElement = angular.element(displayTemplate);
                var subunit = tElement.text();
                tElement.html(editTemplate);
                tElement.append(displayElement);
                return function(scope, element, attrs){
                    scope.editing=true;
                    console.log(scope);
                    console.log(element);
                   scope.sequence = subunit;
                    console.log(scope);
                    scope.edit = function(){
                        var elementHTML = scope.SubunitCtrl.parseSubunit(subunit);
                        displayElement.html= elementHTML;
                        scope.editing= !scope.editing;
                    };
                };
            }
        };
    });*/

    ginasApp.directive('subunit', function() {
        return {
            restrict: 'E',
            require: '^ngModel',
            scope: {
                sequence: '='
            },
            template: '<textarea class="form-control string text-uppercase" rows="5" ng-model = "sequence" ng-change = "clean(sequence)" name="sequence"  placeholder="Sequence" title="sequence" id="sequence" required></textarea>',
        };
    });

/*    ginasApp.directive('structure', function() {
        return {
            restrict: 'E',
            template: '<img class="struc-thumb img-responsive" alt="Structure" title="Structure" src='@ix.ncats.controllers.routes.App.structure(s.id, "svg", size)>',
        };
    });*/

        ginasApp.structureFactory = angular.factory('structureFactory', function($resource) {
                var structureImage = $resource('/api/persons/:id', {id: '@id'});
                 return persons;
                });


    ginasApp.controller('NameController', function ($scope, Substance, $rootScope) {
        $scope.isEditing = false;
        $scope.editObj = null;
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
                $rootScope.uniqueName = true;
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
            console.log("cancelling");
            var index = Substance.names.indexOf($scope.editName);
            console.log(index);
            Substance.names[index] = $scope.tempCopy;
            console.log((Substance));
            $scope.isEditing = false;
            $scope.editedName = null;
        };

        this.setEditedName = function setEditedName(name) {
            $scope.editName = name;
            $scope.tempCopy = angular.copy(name);
            console.log($scope.editName);
        };

        this.updateName = function updateName(name) {
            console.log(name);
            var index = Substance.names.indexOf(name);
            console.log(index);
            Substance.names[index] = name;
            $scope.editName = null;
            $scope.isEditing = false;
            console.log(Substance);
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

    ginasApp.controller('CodeController', function ($scope, Substance) {
        $scope.isEditingCode = false;
        $scope.editCode = null;

        this.addCodes = function () {
            $scope.addingCodes = !$scope.addingCodes;
        };

        this.toggleEditCode = function () {
            console.log("editing)");
            $scope.isEditingCode = !$scope.isEditingCode;
        };

        this.reset = function () {
            $scope.code = {};
            $scope.$broadcast('show-errors-reset');
        };

        this.validateCode = function (code) {
            $scope.$broadcast('show-errors-check-validity');
            if ($scope.codeForm.$valid) {
                //new array if object doesn't already have one
                if (!Substance.codes) {
                    console.log("new array");
                    Substance.codes = [];
                }
                Substance.codes.push(code);
                reset();
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

    ginasApp.controller('StructureController', function ($scope, $http, Substance) {
        $scope.isEditingStructure = false;
        $scope.editStructure = null;
        $scope.noStructure = true;
        this.adding = false;
        this.editing = false;


        this.toggleEdit = function () {
            this.editing = !this.editing;
        };

        this.toggleAdd = function () {
            this.adding = !this.adding;
        };

        $scope.addStructure = function () {
            $scope.addingStructure = !$scope.addingStructure;
            sketcher = new JSDraw("sketcherForm");
            //sketcher.options.ondatachange="getSmiles();";
            console.log(sketcher);
        };

        $scope.validateStructure = function (structure) {
            structure.formula = sketcher.getFormula();
            console.log(structure);
            //console.log(sketcher.getSmiles());

            structure.molfile = sketcher.getMolfile();
            structure.mwt = sketcher.getMolWeight();

            //structure.formula = formulaVar;
            $scope.substance.structure = structure;
            //$scope.substance.structure.molfile=sketcher.getMolfile();
            //$scope.substance.structure.mwt=sketcher.getMolWeight();

            $scope.editorEnabled = false;

            $scope.structure = {};
            $scope.noStructure = false;
            console.log($scope.substance);
        };

        this.resolveMol= function(structure){
            console.log("resolving mol file");
            sketcher.setMolfile(structure.molfile);
            var url = window.strucUrl;//'/ginas/app/smiles';
            var mol = sketcher.getMolfile();
            console.log(mol);
            $http({
                method: 'POST',
                url: url,
                data: mol,
                headers: {'Content-Type': 'text/plain'}
            }).success(function (data) {
                console.log(data);
                console.log(structure);
                $scope.structure= data.structure;
                console.log(structure);
/*            structure.mwt= sketcher.getMolWeight();
            structure.formula= sketcher.getSmiles();*/
           // this.molchange(sketcher);
        });
        };

        this.molchange= function(sketch){
            console.log("molchange");
            $('#mol-weight').val(sketch.getMolWeight());
            $('#molfile').val(sketch.getMolfile());
            $('#structure').typeahead('val', '"'+sketch.getSmiles()+'"');
            this.getSmiles(sketch.getSmiles());
        };

        this.setQuery = function(value) {
            console.log(value);
            $.ajax({
                type: "POST",
                url: '@ix.ncats.controllers.routes.App.smiles',
                contentType: 'text/plain',
                data: value,
                success: function (data) {
                    console.log(' => '+data);
                    $('#formula').val(data);
                },
                error: function (xhr, status) {
                    console.error("Can't convert to smiles");
                },
                dataType: 'text'
            });
        };

        $scope.setEditedStructure = function (structure) {
            console.log("clicked");
            $scope.editStructure = structure;
            $scope.tempCopy = angular.copy(structure);
        };

        $scope.updateStructure = function (structure) {
            $scope.substance.structure = structure;
            $scope.editStructure = null;
            $scope.isEditingStructure = false;
        };

        $scope.removeStructure = function (structure) {
            $scope.substance.structure = null;
        };

        $scope.resolve= function(){
            console.log("resolve");
            var url = window.strucUrl;//'/ginas/app/smiles';
            var mol = sketcher.getMolfile();
            console.log(mol);
            $http({
                method: 'POST',
                url: url,
                data: mol,
                headers: {'Content-Type': 'text/plain'}
            }).success(function (data) {
                console.log(data);
                //$scope.substance.structure.formula = data;
                return data;
            });
        };

        this.getSmiles = function () {
            console.log("here");
            var smile = sketcher.getSmiles();
            var url2 = window.envurl;//'/ginas/app/smiles';
            console.log(url2);
            console.log(smile);
            data = JSON.stringify(smile);
            console.log(data);
            $http({
                method: 'POST',
                url: url2,
                data: smile,
                headers: {'Content-Type': 'text/plain'}
            }).success(function (data) {
                console.log(data);
                structure.formula = data;
                return data;
            });
        };
    });

    ginasApp.controller('SubunitController', function ($scope, Substance) {
        $scope.editReference = null;
        this.adding = false;
        this.editing = false;
        this.display=[];
        this.subunit={};
        this.toggleEdit = function () {
            this.editing = !this.editing;
        };

        this.toggleAdd = function () {
            this.adding = !this.adding;
        };

        this.clean = function(sequence){
            console.log("clean");
            return sequence.replace(/[^A-Za-z]/g, '');
        };

        this.parseSubunit = function (sequence){
            console.log(sequence);
            var split = sequence.replace(/[^A-Za-z]/g, '').split('');
            var display=[];
            console.log(split);
            var invalid = ['B','J','O','U','X','Z'];
            for(var i in split){
                var obj={};
                console.log(split[i]);
                var valid = dojo.indexOf(invalid, split[i].toUpperCase());
                console.log(valid);
                if(valid>=0){
                    obj.value = split[i];
                    obj.valid=false;
                    display.push(obj);
                    obj={};
                }else{
                    obj.value = split[i];
                    obj.valid= true;
                    display.push(obj);
                    obj={};
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
            this.added=false;
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
                this.added= true;
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

    //ginasApp.directive('ModificationForm', function ($scope) ){
    //
    //}

})();