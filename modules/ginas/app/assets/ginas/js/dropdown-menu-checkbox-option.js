(function() {
    'use strict';

    angular
        .module('ginas')
        .directive('dropdownMenuCheckboxOption', dropdownMenuCheckboxOption);

    function dropdownMenuCheckboxOption() {
        var directive = {
            link: link,
            restrict: 'A',
            scope: {
                toggleProperty: '=',
                callOnToggle: '&?'
            }
        };
        return directive;

        function link(scope, element, attrs) {

            var options = [];

            var checkboxInput = $(element).find('input:checkbox:first');


            if (scope.toggleProperty) {
                checkboxInput.prop('checked', true);
                options.push($(element).attr('data-value'));
            }

            $(element).on('click', function (event) {

                var $target = $(event.currentTarget),
                    val = $target.attr('data-value'),
                    $inp = $target.find('input'),
                    idx;

                if ((idx = options.indexOf(val)) > -1) {
                    options.splice(idx, 1);
                    setTimeout(function () { $inp.prop('checked', false) }, 0);
                } else {
                    options.push(val);
                    setTimeout(function () { $inp.prop('checked', true) }, 0);
                }

                scope.$apply(function () {
                    scope.toggleProperty = !scope.toggleProperty;
                })

                if(scope.callOnToggle){
                    scope.$apply(function () {
                        scope.callOnToggle();
                    });
                }

                $(event.target).blur();

                return false;
            });

        }
    }

})();