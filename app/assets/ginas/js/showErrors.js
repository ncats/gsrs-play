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
                inputEl = el[0].querySelector('[name]');
                inputNgEl = angular.element(inputEl);
                inputName = $interpolate(inputNgEl.attr('name') || '')(scope);
                if (!inputName) {
                    throw "show-errors element has no child input elements with a 'name' attribute and a 'form-control' class";
                }

                inputNgEl.bind(trigger, function() {
                    blurred = true;
                    return toggleClasses(formCtrl[inputName].$invalid);
                });
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