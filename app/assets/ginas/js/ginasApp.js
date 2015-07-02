(function () {
    var ginasApp = angular.module('ginas', [])
    .config(function($locationProvider) {
        $locationProvider.html5Mode(true);});

    ginasApp.controller('GinasController', function ($scope, $http, $location) {
        $scope.substance= {};
        $scope.substance.substanceClass = $location.$$search.kind;
       // console.log(substance);
    });

    ginasApp.controller('NameController',function($scope, $http, $filter){
    //  $scope.name = {};
        $scope.isEditing = false;
        $scope.editName = null;
        function validateName (ginasName) {
      //      $scope.name=ginasName;
            console.log("clicked");
            $scope.editorEnabled = false;
            //new array if object doesn't already have one
            if(!$scope.substance.names){
                console.log("new array");
                $scope.substance.names = [];
            }
            $scope.substance.names.push(ginasName);
            //resets form
            $scope.name={};
            var index = $scope.substance.names.indexOf(ginasName);
            console.log(index);
        }


          function startEditing() {
              console.log("edit yo!");
             $scope.isEditing = true;
        }

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
        $scope.startEditing = startEditing;

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
        $scope.validateName= validateName;
    });
})();