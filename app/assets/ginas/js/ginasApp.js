(function () {
    var ginasApp = angular.module('ginas', [])
    .config(function($locationProvider) {
        $locationProvider.html5Mode(true);});

    ginasApp.filter('range', function() {
        return function (input, min, max) {
            min = parseInt(min); //Make string input int
            max = parseInt(max);
            for (var i = min; i < max; i++)
                input.push(i);
            return input;
        };
    });

    ginasApp.factory('Substance', function($location) {
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

    function GinasCtrl(Substance){
        var ginasCtrl = this;
        ginasCtrl.substance= Substance;
    }



    ginasApp.directive("nameValid", function () {
        return {
            restrict :"E",
            template: "<div>Name ok </div>"
        };

    });

    ginasApp.controller("GinasCtrl",GinasCtrl);


    ginasApp.controller('NameController',function($scope, $http, $filter, $rootScope){
        $scope.isEditing = false;
        $scope.editName = null;
        $scope.addName = null;
        $scope.addingNames=false;


        $scope.addNames= function(){
            $scope.addingNames= !$scope.addingNames;
        };

        $scope.toggleEdit= function(){
            $scope.isEditing=!$scope.isEditing;
        };



        $scope.validateName = function(ginasName) {
            $scope.editorEnabled = false;
            $http({
                method: 'GET',
                url: "app/api/v1/substances?filter=names.name='"+ ginasName.name+"'",
                headers: {'Content-Type': 'text/plain'}
            }).success(function (data) {
                console.log(data);
                if(data.count===0){

                }
                return data;
            });

            //new array if object doesn't already have one
            if(!$scope.substance.names){
                $scope.substance.names = [];
            }
            $scope.substance.names.push(ginasName);
            //resets form
            $scope.name={};
            $rootScope.uniqueName =true;

        };






        $scope.clear = function() {
            $scope.name={};

        };


        function cancelEditing() {
            console.log("cancelling");
            var index = $scope.substance.names.indexOf($scope.editName);
            console.log(index);
            $scope.substance.names[index] = $scope.tempCopy;
            console.log(($scope.substance));
            $scope.isEditing = false;
            $scope.editedName = null;

        }
        $scope.cancelEditing = cancelEditing;

        function setEditedName(ginasName) {
            console.log("edit");
            console.log(ginasName);
            $scope.editName = ginasName;
            $scope.tempCopy = angular.copy(ginasName);
            console.log($scope.editName);
        }

        $scope.setEditedName = setEditedName;

        function updateName(ginasName) {
            console.log("submitting");
            console.log($scope.substance);
            console.log(ginasName);
            var index = $scope.substance.names.indexOf(ginasName);
            console.log(index);
           $scope.substance.names[index] = ginasName;
            $scope.editName = null;
            $scope.isEditing = false;
            console.log($scope.substance);
        }

        $scope.updateName = updateName;



        this.removeName = function(remName){
            console.log(remName);
            var index = $scope.substance.names.indexOf(remName);
            $scope.substance.names.splice(index, 1);
            console.log(substance);
        };

        this.editName = function(name){
            //substance.names = angular.copy(name);
            console.log(name);
                    $scope.editorEnabled = true;
                   //this.name = $scope.name;

            };

    });

    ginasApp.controller('ReferenceController',function($scope) {
        $scope.isEditingRef = false;
        $scope.editReference = null;

        $scope.toggleEditRef= function(){
            console.log("editing)");
            $scope.isEditingRef=!$scope.isEditingRef;
        };


        function validateReference(ginasRef) {
            console.log("references");
            $scope.editorEnabled = false;
            //new array if object doesn't already have one
            if (!$scope.substance.references) {
                console.log("new array");
                $scope.substance.references = [];
            }
            ginasRef.id = $scope.substance.references.length +1;
            $scope.substance.references.push(ginasRef);
            $scope.reference = {};
            console.log($scope.substance);

        }
        $scope.validateReference=validateReference;

        $scope.setEditedRef = function(reference) {
            console.log("clicked");
            $scope.editReference = reference;
            $scope.tempCopy = angular.copy(reference);
        };

    $scope.updateReference = function(reference) {
        var index = $scope.substance.references.indexOf(reference);
        $scope.substance.references[index] = reference;
        $scope.editReference = null;
        $scope.isEditingRef = false;
    };

    $scope.removeRef= function(reference){
            var index = $scope.substance.references.indexOf(reference);
            $scope.substance.references.splice(index, 1);
        };
    });

    ginasApp.controller('CodeController',function($scope) {
        $scope.isEditingCode = false;
        $scope.editCode = null;

        $scope.addCodes= function(){
            $scope.addingCodes= !$scope.addingCodes;
        };

        $scope.toggleEditCode= function(){
            console.log("editing)");
            $scope.isEditingCode=!$scope.isEditingCode;
        };


       $scope.validateCode = function(code) {
            console.log("code");
            $scope.editorEnabled = false;
            //new array if object doesn't already have one
            if (!$scope.substance.codes) {
                console.log("new array");
                $scope.substance.codes = [];
            }
            $scope.substance.codes.push(code);
             $scope.code = {};
            console.log($scope.codes);

        };

        $scope.setEditedCode= function(code) {
            console.log("clicked");
            $scope.editCode = code;
            $scope.tempCopy = angular.copy(code);
        };

        $scope.updateCode = function(code) {
            var index = $scope.substance.codes.indexOf(code);
            $scope.substance.codes[index] = code;
            $scope.editCode = null;
            $scope.isEditingCode = false;
        };

        $scope.removeCode= function(code){
            var index = $scope.substance.codes.indexOf(code);
            $scope.substance.codes.splice(index, 1);
        };
    });

    ginasApp.controller('StructureController',function($scope, $http) {
        $scope.isEditingStructure = false;
        $scope.editStructure = null;
        $scope.noStructure= true;

        $scope.addStructure= function(){
            $scope.addingStructure= !$scope.addingStructure;
            sketcher = new JSDraw("sketcherForm");
            //sketcher.options.ondatachange="getSmiles();";
            console.log(sketcher);
        };

        $scope.validateStructure = function(structure) {
            structure.formula =sketcher.getFormula();
            console.log(structure);
            //console.log(sketcher.getSmiles());

            structure.molfile = sketcher.getMolfile();
            structure.mwt = sketcher.getMolWeight();

            //structure.formula = formulaVar;
            $scope.substance.structure= structure;
            //$scope.substance.structure.molfile=sketcher.getMolfile();
            //$scope.substance.structure.mwt=sketcher.getMolWeight();

            $scope.editorEnabled = false;

            $scope.structure = {};
            $scope.noStructure= false;
            console.log($scope.substance);
        };

        $scope.toggleEditStructure= function(){
            console.log("editing)");
            $scope.isEditingStructure=!$scope.isEditingStructure;
        };

        $scope.setEditedStructure= function(structure) {
            console.log("clicked");
            $scope.editStructure = structure;
            $scope.tempCopy = angular.copy(structure);
        };

        $scope.updateStructure = function(structure) {
            $scope.substance.structure = structure;
            $scope.editStructure = null;
            $scope.isEditingStructure = false;
        };

        $scope.removeStructure= function(structure){
        $scope.substance.structure=null;
        };

        $scope.getSmiles= function(){
            console.log("here");
                var smile = sketcher.getSmiles();
                var url = window.envurl;//'/ginas/app/smiles';
                console.log(url);
                console.log(smile);
                data= JSON.stringify(smile);
            console.log(data);

            $http({
                method: 'POST',
                url: url,
                data: smile,
                headers: {'Content-Type': 'text/plain'}
            }).success(function (data) {
                console.log(data);
                $scope.substance.structure.formula= data;
                return data;
            });
    };
    });

    ginasApp.controller('SubunitController', function ($scope) {
        $scope.isEditingSubunit = false;
        $scope.addingSubunit = false;
        $scope.editSubunit = null;

        $scope.addSubunit= function(){
            $scope.addingSubunit= !$scope.addingSubunit;
        };

        $scope.toggleEditSubunit= function(){
            console.log("editing)");
            $scope.isEditingSubunit=!$scope.isEditingSubunit;
        };


        $scope.validateSubunit = function(subunit) {
            console.log("subunit");
            $scope.editorEnabled = false;
            //new array if object doesn't already have one
            if (!$scope.substance.protein.subunits) {
                console.log("new array");
                $scope.substance.protein.subunits = [];
            }
            var j = subunit.sequence.length/10;
            for(var i=0; i<subunit.sequence.length/10; i++){
console.log("Start: " + (i*10) + subunit.sequence.substring((i*10),((i+1)*10))+ " end: " + ((i+1)*10));

            }

          //  console.log(subunit.sequence.split(''));
            $scope.substance.protein.subunits.push(subunit);
            $scope.subunit = {};
            console.log($scope.substance);

        };

        $scope.setEditedSubunit= function(subunit) {
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

        $scope.removeSubunit= function(subunit){
            var index = $scope.substance.protein.subunits.indexOf(subunit);
            $scope.substance.protein.subunits.splice(index, 1);
        };
    });

    ginasApp.controller('DetailsController', function ($scope) {
        $scope.isEditingDetails = false;
        $scope.addingDetails = false;
        $scope.editDetails = null;
        $scope.detailsSet= false;

        $scope.addDetails= function(){
            $scope.addingDetails= !$scope.addingDetails;
        };

        $scope.toggleEditDetails= function(){
            console.log("editing)");
            $scope.isEditingDetails=!$scope.isEditingDetails;
        };


        $scope.validateDetails = function(protein) {
            console.log("subunit");
            $scope.editorEnabled = false;
            $scope.substance.protein.proteinType = protein.type;
            $scope.substance.protein.proteinSubType = protein.subType;
            $scope.substance.protein.sequenceOrigin = protein.sequenceOrigin;
            $scope.substance.protein.sequenceType = protein.sequenceType;

            $scope.protein = {};
            console.log($scope.substance);
            $scope.detailsSet = true;


        };

        $scope.setEditedDetails= function(details) {
            console.log("clicked");
            $scope.editDetails = details;
            $scope.tempCopy = angular.copy(details);
        };

        $scope.updateDetails = function(details) {
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