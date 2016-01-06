(function () {
    var ginasFormElements = angular.module('ginasFormElements', []);

    ginasFormElements.factory('CVFields', function ($http, $q) {

        var lookup = {
            "stereoChemistry": "STEREOCHEMISTRY_TYPE",
            "names.type": "NAME_TYPE",
            "names.nameOrgs": "NAME_ORG",
            "names.nameJurisdiction": "JURISDICTION",
            "names.domains": "NAME_DOMAIN",
            "names.languages": "LANGUAGE",
            "codes.codeSystem": "CODE_SYSTEM",
            "codes.type": "CODE_TYPE",
            "relationships.type": "RELATIONSHIP_TYPE",
            "relationships.interactionType": "INTERACTION_TYPE",
            "relationships.qualification": "QUALIFICATION",
            "references.docType": "DOCUMENT_TYPE"
        };


        var url = baseurl + "api/v1/vocabularies?filter=domain='";
        var deferred = $q.defer();
        var CV = {
            lookuptable: lookup,

            load: function (field) {
                if (!_.has(CV, field)) {
                    var promise = $http.get(url + field.toUpperCase() + "'&top=999", {cache: true}, {
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (data) {
                        CV[field] = data.content[0].terms;
                        return CV[field];
                    });
                    return promise;
                }
            },

            fetch: function (field) {
                //if (!_.has(CV, field)) {
                return $http.get(url + field.toUpperCase() + "'", {cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    //CV[field] = data.content[0].terms;
                    //  console.log(data);
                    return data;
                });
                //  }
            },

            search: function (field, query) {
                return _.chain(CV[field])
                    .filter(function (x) {
                        return !query || x.display.toLowerCase().indexOf(query.toLowerCase()) > -1;
                    })
                    .sortBy('display')
                    .value();
            },

            lookup: function (field, query) {
                return _.chain(CV[field])
                    .filter(function (x) {
                        return !query || x.value.toLowerCase().indexOf(query.toLowerCase()) > -1;
                    })
                    .sortBy('value')
                    .value();
            },

            retrieve: function (field) {
                if (field === 'NAME_TYPE') {
                    var temp = angular.copy(CV[field]);
                    temp = _.remove(temp, function (n) {
                        return n.value !== 'of';
                    });
                    return temp;
                } else {
                    return CV[field];
                }
            }
        };
        return CV;
    });

    ginasFormElements.factory('isDuplicate', function ($q, substanceFactory) {
        return function dupCheck(modelValue) {
                var deferred = $q.defer();
            if(!_.isUndefined(modelValue)) {
                substanceFactory.getSubstances(modelValue)
                    .success(function (response) {
                        if (response.count >= 1) {
                            deferred.reject();
                        } else {
                            deferred.resolve();
                        }
                    });
            }else {
                deferred.resolve();
            }
                return deferred.promise;
        };
    });

    ginasFormElements.factory('substanceFactory', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances?filter=names.name='";
        var substanceFactory = {};
        substanceFactory.getSubstances = function (name) {
            return $http.get(url + name.toUpperCase() + "'", {cache: true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            });
        };
        return substanceFactory;
    }]);

    ginasFormElements.directive('checkBox', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/check-box.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@'
            }
        };
    });

    ginasFormElements.directive('checkBoxViewEdit', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/check-box-view-edit.html",
            replace: true,
            scope: {
                obj: '=',
                field: '@'
            },
            link: function (scope, element, attrs) {
                scope.editing = function (obj, field) {
                    _.set(obj, '_editing' + field, true);
                };
            }
        };
    });

    ginasFormElements.directive('datePicker', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/date-picker.html",
            require: '^ngModel',
            replace: true,
            scope: {
                object: '=ngModel',
                field: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                //date picker
                scope.status = {
                    opened: false
                };

                scope.open = function ($event) {
                    scope.status.opened = true;
                };
            }
        };
    });

    ginasFormElements.directive('datePickerViewEdit', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/date-picker-view-edit.html",
            replace: true,
            scope: {
                obj: '=obj',
                field: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                //date picker
                scope.status = {
                    opened: false
                };

                scope.open = function ($event) {
                    scope.status.opened = true;
                };
            }
        };
    });

    ginasFormElements.directive('dropdownSelect', function (CVFields) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/dropdown-select.html",
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                CVFields.fetch(attrs.cv).then(function (data) {
                    if (attrs.cv === 'NAME_TYPE') {
                        var temp = angular.copy(data.data.content[0].terms);
                        temp = _.remove(temp, function (n) {
                            return n.value !== 'of';
                        });
                        scope.values = temp;
                    } else {
                        scope.values = data.data.content[0].terms;
                    }
                });
            }
        };
    });

    ginasFormElements.directive('dropdownViewEdit', function (CVFields) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/dropdown-view-edit.html",
            replace: true,
            scope: {
                formname: '=',
                obj: '=',
                field: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                CVFields.fetch(attrs.cv).then(function (data) {
                    if (attrs.cv === 'NAME_TYPE') {
                        var temp = angular.copy(data.data.content[0].terms);
                        temp = _.remove(temp, function (n) {
                            return n.value !== 'of';
                        });
                        scope.values = temp;
                    } else {
                        scope.values = data.data.content[0].terms;
                    }
                });

                scope.editing = function (obj) {
                    if (_.has(obj, '_editing')) {
                        obj._editing = !obj._editing;
                    } else {
                        _.set(obj, '_editing', true);
                    }
                };
            }
        };
    });

    ginasFormElements.directive('multiSelect', function (CVFields) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/multi-select.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                cv: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                CVFields.load(attrs.cv);

                scope.loadItems = function (cv, $query) {
                    return CVFields.search(cv, $query);
                };

            }
        };
    });

    ginasFormElements.directive('multiViewEdit', function (CVFields) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/multi-view-edit.html",
            replace: true,
            scope: {
                obj: '=',
                field: '@',
                cv: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                CVFields.load(scope.cv);

                scope.loadItems = function (cv, $query) {
                    return CVFields.search(cv, $query);
                };

                scope.editing = function (obj) {
                    if (_.has(obj, '_editing')) {
                        obj._editing = !obj._editing;
                    } else {
                        _.set(obj, '_editing', true);
                    }
                };
            }
        };
    });

    ginasFormElements.directive('textBox', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/text-box.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@'
            }
        };
    });

    ginasFormElements.directive('textBoxViewEdit', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/text-box-view-edit.html",
            replace: true,
            scope: {
                obj: '=obj',
                field: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                scope.editing = function (obj) {
                    if (_.has(obj, '_editing')) {
                        obj._editing = !obj._editing;
                    } else {
                        _.set(obj, '_editing', true);
                    }
                };
            }
        };
    });

    ginasFormElements.directive('textInput', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/text-input.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                label: '@',
                form:'@',
                validate:'='
            },
            link: function(scope, element){
                //console.log(scope);
            }
        };
    });

    ginasFormElements.directive('textViewEdit', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/text-view-edit.html",
            replace: true,
            scope: {
                obj: '=',
                field: '@',
                label: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                scope.edit = false;
                scope.editing = function () {
                    scope.edit = !scope.edit;
                };
            }
        };
    });

    ginasFormElements.directive('duplicate', function (isDuplicate) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
               // console.log(ngModel);
              //  console.log(scope);
                ngModel.$asyncValidators.duplicate = isDuplicate;
            }
        };
    });

})();