(function () {
    var ginasApp = angular.module('ginas', ['ngMessages', 'ui.bootstrap.showErrors','ui.bootstrap.datetimepicker'])
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

    ginasApp.directive('duplicate', function (isDuplicate) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
                ngModel.$asyncValidators.duplicate = isDuplicate;
            }
        };
    });

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


    ginasApp.controller('NameController', function ($scope, Substance, $rootScope) {
        $scope.isEditing = false;
        $scope.editObj = null;
        $scope.addName = null;
        $scope.addingNames = true;
        $rootScope.uniqueName = false;

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

    ginasApp.controller('StructureController', function ($scope, $http) {
        $scope.isEditingStructure = false;
        $scope.editStructure = null;
        $scope.noStructure = true;

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

        $scope.toggleEditStructure = function () {
            console.log("editing)");
            $scope.isEditingStructure = !$scope.isEditingStructure;
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

        $scope.getSmiles = function () {
            console.log("here");
            var smile = sketcher.getSmiles();
            var url = window.envurl;//'/ginas/app/smiles';
            console.log(url);
            console.log(smile);
            data = JSON.stringify(smile);
            console.log(data);

            $http({
                method: 'POST',
                url: url,
                data: smile,
                headers: {'Content-Type': 'text/plain'}
            }).success(function (data) {
                console.log(data);
                $scope.substance.structure.formula = data;
                return data;
            });
        };
    });

    ginasApp.controller('SubunitController', function ($scope) {
        $scope.isEditingSubunit = false;
        $scope.addingSubunit = false;
        $scope.editSubunit = null;

        $scope.addSubunit = function () {
            $scope.addingSubunit = !$scope.addingSubunit;
        };

        $scope.toggleEditSubunit = function () {
            console.log("editing)");
            $scope.isEditingSubunit = !$scope.isEditingSubunit;
        };


        $scope.validateSubunit = function (subunit) {
            console.log("subunit");
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
        this.adding = false;
        this.editing = false;

        this.toggleEdit = function () {
            this.editing = !this.editing;
        };

        this.toggleAdd = function () {
            this.adding = !this.adding;
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
                $scope.protein = {};
                $scope.detailsSet = true;
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

        $scope.updateDetails = function (details) {
            var index = $scope.substance.subunits.indexOf(subunit);
            $scope.substance.subunits[index] = subunit;
            $scope.editSubunit = null;
            $scope.isEditingSubunit = false;
        };

    });

    //ginasApp.directive('ModificationForm', function ($scope) ){
    //
    //}

})();