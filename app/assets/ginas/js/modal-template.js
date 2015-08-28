(function() {
    var modalTemplate;

    modalTemplate = angular.module('ui.bootstrap', []);

    modalTemplate.directive('modalTemplate',[function() {
        return {
            restrict: 'E',
            templateUrl: 'ginas/app/assets/ginas/templates/modal-window.html',
            scope: {
                modal: '='
            },
            controller: function ($scope) {
                console.log($scope);

                $scope.ok = function () {
                    $scope.modal.instance.close($scope.selected);
                };

                $scope.cancel = function () {
                    $scope.modal.instance.dismiss('cancel');
                };

/*                $scope.modal.instance.result.then(function (selectedItem) {
                    $scope.selected = selectedItem;
                }, function () {
                    $log.info('Modal dismissed at: ' + new Date());
                });*/
            }
        };

    }]);



}).call(this);