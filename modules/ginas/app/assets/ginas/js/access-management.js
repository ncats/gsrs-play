(function () {
    'use strict';

    angular
        .module('ginas')
        .directive('accessManagement', accessManagement);

    function accessManagement(CVFields, $timeout) {
        var directive = {
            link: link,
            restrict: 'E',
            templateUrl: baseurl + 'assets/templates/elements/access-management.html',
            scope: {
                objToManageAccess: '='
            }
        };
        return directive;

        function link(scope, element, attrs) {

            scope.variables ={
                isDropdownOpen: false,
                tooltipMessage: ''
            }

            scope.accessOptions = [];

            var domain = 'ACCESS_GROUP';
            var baseTooltipMessage = 'Access is set to: ';
            var dropdownTimer = null;

            function loadDirective() {
                if (!_.isArray(scope.objToManageAccess.access)) {
                    scope.objToManageAccess.access = [];
                }
                getAccessOptions();
            }

            function getAccessOptions() {
                CVFields.getCV(domain).then(function (response) {

                    var accessOptions = _.orderBy(response.data.content[0].terms, ['display'], ['asc']);

                    crosscheckAccesses(accessOptions);

                    //bump Protected, English and United States up to the top
                    scope.accessOptions = _.orderBy(accessOptions, function (cv) {
                        return cv.display == 'English' || cv.display == 'United States' || cv.display == 'PROTECTED';
                    }, ['desc']);
                });
            }

            function crosscheckAccesses (accessOptions) {
                scope.variables.tooltipMessage = baseTooltipMessage;

                if (scope.objToManageAccess.access.length > 0) {
                    _.forEach(accessOptions, function (accessOption) {
                        for(var i = 0; i < scope.objToManageAccess.access.length; i++) {
                            if (accessOption.value === scope.objToManageAccess.access[i] || accessOption.value === scope.objToManageAccess.access[i].value){
                                accessOption.selected = true;
                                scope.variables.tooltipMessage += (accessOption.display + ', ');
                                break;
                            }
                        }
                    });
                    scope.variables.tooltipMessage = scope.variables.tooltipMessage.replace(/, ([^, ]*)$/,'$1');
                } else {
                    scope.variables.tooltipMessage += 'public';
                }
            }

            function resetDropdownTimer () {
                $timeout.cancel(dropdownTimer);
                dropdownTimer = $timeout(function (){
                    scope.variables.isDropdownOpen = false;
                    $timeout.cancel(dropdownTimer);
                }, 2500)
            }

            scope.processMenuChange = function () {
                if (scope.variables.isDropdownOpen) {
                    resetDropdownTimer();
                } else {
                    $timeout.cancel(dropdownTimer);
                }
            }

            scope.toggleAccessOption = function (accessOption) {

                resetDropdownTimer();

                if (scope.objToManageAccess.access.length === 0){
                    scope.variables.tooltipMessage = scope.variables.tooltipMessage.replace('public','');
                }

                if (accessOption.selected) {
                    scope.objToManageAccess.access.push(accessOption.value);

                    if (scope.objToManageAccess.access.length > 1) {
                        scope.variables.tooltipMessage += ', '
                    }

                    scope.variables.tooltipMessage += (accessOption.display);

                } else {
                    var indexRemoved,
                        accessLength = scope.objToManageAccess.access.length;
                    _.remove(scope.objToManageAccess.access, function(selectedOption, index) {

                        if (selectedOption === accessOption.value || selectedOption.value === accessOption.value) {
                            indexRemoved = index;
                            return true;
                        }

                        return false;
                    });

                    var toRemoveFromTooltip = accessOption.display;

                    if (indexRemoved < (accessLength - 1)){
                        toRemoveFromTooltip = (accessOption.display + ', ' );
                    } else if (indexRemoved === (accessLength - 1) && accessLength > 1) {
                        toRemoveFromTooltip = (', ' + accessOption.display);
                    }

                    scope.variables.tooltipMessage = scope.variables.tooltipMessage.replace(toRemoveFromTooltip,'');

                    if (scope.objToManageAccess.access.length === 0) {
                        scope.variables.tooltipMessage += 'public';
                    }
                }
            }

            loadDirective();
        }
    };
})()