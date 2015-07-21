(function() {
    var showErrorsModule;

    showErrorsModule = angular.module('ui.bootstrap.showErrors', []);

    showErrorsModule.directive('showErrors', ['$http',
        '$timeout', 'showErrorsConfig', '$interpolate', function($http, $timeout, showErrorsConfig, $interpolate) {
            var getShowSuccess, getTrigger, linkFn;
            getTrigger = function(options) {
                var trigger;
                trigger = showErrorsConfig.trigger;
                if (options && (options.trigger !== null)) {
                    trigger = options.trigger;
                }
                return trigger;
            };
            getShowSuccess = function(options) {
                var showSuccess;
                showSuccess = showErrorsConfig.showSuccess;
                if (options && (options.showSuccess !== null)) {
                    showSuccess = options.showSuccess;
                }
                return showSuccess;
            };
            linkFn = function(scope, el, attrs, formCtrl) {
                var blurred, inputEl, inputName, inputNgEl, options, showSuccess, toggleClasses, trigger;
                blurred = false;
                options = scope.$eval(attrs.showErrors);
                showSuccess = getShowSuccess(options);
                trigger = getTrigger(options);
                inputEl = el[0].querySelector('.form-control[name]');
                inputNgEl = angular.element(inputEl);
                inputName = $interpolate(inputNgEl.attr('name') || '')(scope);
                //console.log(options);
                //console.log(showSuccess);
                //console.log(scope);
                //console.log(inputName);
                //console.log(scope.nameForm);
                if (!inputName) {
                    throw "show-errors element has no child input elements with a 'name' attribute and a 'form-control' class";
                }

            //    if(inputName =='subName'){
            //console.log(el);
            //        console.log(formCtrl);
            //        formCtrl.subName.$parsers.unshift(function (value) {
            //            $http({
            //                method: 'GET',
            //                url: "app/api/v1/substances?filter=names.name='" + value.toUpperCase() + "'",
            //                headers: {'Content-Type': 'text/plain'}
            //            }).success(function (data) {
            //                if (data.count === 0) {
            //                    console.log("success");
            //                    console.log(formCtrl.subName);
            //                    formCtrl[inputName].$setValidity('valid', true);
            //                    //ctrl.$setValidity(yourFieldName, true);
            //                    //formCtrl.subName.$invalid=false;
            //                    //formCtrl.subName.$valid=true;
            //                    //.$setValidity('invalid', false);
            //                //    console.log(formCtrl.subName);
            //                    console.log((formCtrl.subName.$invalid));
            //                //    showSuccess=true;
            //                    console.log(formCtrl[inputName].$invalid);
            //                    console.log(formCtrl[inputName]);
            //                    formCtrl[inputName].$invalid=false;
            //                    formCtrl[inputName].$valid=true;
            //                    console.log(formCtrl[inputName]);
            //                    $scope.$broadcast('show-errors-check-validity');
            //
            //                    //    toggleClasses(false);
            //                    return toggleClasses(formCtrl[inputName].$invalid);
            //
            //                }else {
            //                //    formCtrl.subName.$setValidity('duplicate', false);
            //                    console.log("exists");
            //
            //                    return undefined;
            //                }
            //        });
            //    });
            //    }

                inputNgEl.bind(trigger, function() {
                    blurred = true;
                    console.log("binding "+ formCtrl[inputName].$viewValue);
                    return toggleClasses(formCtrl[inputName].$invalid);
                });
                //scope.$watch(function() {
                //    return formCtrl[inputName] && formCtrl[inputName].$invalid;
                //}, function(invalid) {
                //    if (!blurred) {
                //        return;
                //    }
                //    return toggleClasses(invalid);
                //});
                scope.$on('show-errors-check-validity', function() {
                    return toggleClasses(formCtrl[inputName].$invalid);
                });
                scope.$on('show-errors-reset', function() {
                    return $timeout(function() {
                        el.removeClass('has-error');
                        el.removeClass('has-success');
                        blurred = false;
                        return blurred;
                    }, 0, false);
                });
                toggleClasses = function(invalid) {
                    el.toggleClass('has-error', invalid);
                    if (showSuccess) {
                        console.log(invalid);
                        console.log(el.toggleClass('has-success', !invalid));
                        return el.toggleClass('has-success', !invalid);
                    }
                    return toggleClasses;
                };
            };
            return {
                restrict: 'A',
                require: '^form',
                compile: function(elem, attrs) {
                    if (attrs.showErrors.indexOf('skipFormGroupCheck') === -1) {
                        if (!(elem.hasClass('err-check') || elem.hasClass('err-check'))) {
                            throw "show-errors element does not have the 'form-group' or 'input-group' class";
                        }
                    }
                    return linkFn;
                }
            };
        }
    ]);

    showErrorsModule.provider('showErrorsConfig', function() {
        var _showSuccess, _trigger;
        _showSuccess = false;
        _trigger = 'blur';
        this.showSuccess = function(showSuccess) {
            _showSuccess = showSuccess;
            return _showSuccess;
        };
        this.trigger = function(trigger) {
            _trigger = trigger;
            return _trigger;
        };
        this.$get = function() {
            return {
                showSuccess: _showSuccess,
                trigger: _trigger
            };
        };
    });

}).call(this);