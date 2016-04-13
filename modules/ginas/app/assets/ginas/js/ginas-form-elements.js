(function () {
    var ginasFormElements = angular.module('ginasFormElements', []);

    ginasFormElements.factory('CVFields', function ($http, $q) {

        var load = function () {
            return $http.get(baseurl + "api/v1/vocabularies?filter=domain='CV_DOMAIN'", {cache: true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
                return data;
            });
        };


        var url = baseurl + "api/v1/vocabularies?filter=domain='";
        var CV = {


            getByField: function (path) {
                var ret;
                /*             return load().then(function(data){
                 var terms = data.data.content[0].terms;*/
                var patharr = path.split('.');
                if (patharr.length > 2) {
                    patharr = _.takeRight(patharr, 2);
                }
                var pathString = _.join(patharr, '.');
                return $http.get(baseurl + "api/v1/vocabularies?filter=fields.term='" + pathString + "'", {cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    if (data.content.length > 0) {
                        return data;
                    } else {
                        return 0;
                    }
                });
                /* var domain = _.find(terms, function(cv) {
                 return cv.value == pathString;
                 });
                 if(!_.isUndefined(domain)){
                 ret = domain.display;
                 }
                 return ret;*/
                //       });
            },

            getCV: function (domain) {
                return $http.get(url + domain.toUpperCase() + "'", {cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    return data;
                });
            },

            count: function () {
                var counturl = baseurl + "api/v1/vocabularies";
                return $http.get(counturl, {cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    return data;
                });
            },

            all: function () {
                var allurl = baseurl + "api/v1/vocabularies?top=999";
                return $http.get(allurl, {cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    return data;
                });
            },

            load: function (field) {
                // console.log(lookup);
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
                // console.log(lookup);
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
                    .sortBy('display').value();
            },

            searchTags: function (domain, query) {
                CV.getCV(domain).then(function (data) {
                    console.log(data);
                    return _.chain(data.data.content[0].terms)
                        .filter(function (x) {
                            return !query || x.display.toLowerCase().indexOf(query.toLowerCase()) > -1;
                        })
                        .sortBy('display')
                        .value();
                });
            },

            updateCV: function (domainobj) {
                console.log(domainobj);
                var url;
                var promise;
                if (domainobj.id) {
                    promise = $http.put(baseurl + 'api/v1/vocabularies', domainobj, {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).success(function (data) {
                        alert('update was performed.');
                        return data;
                    });
                } else {
                    promise = $http.post(baseurl + 'api/v1/vocabularies', domainobj, {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).success(function (response) {
                        console.log(response);
                        alert("new domain added");
                        return response;
                    });
                }
                return promise;
            },

            addCV: function (field, newcv) {
                CV.getCV(field).then(function (response) {
                    console.log(response);
                    response.data.content[0].terms.push(newcv);
                    console.log(response);
                    $http.put(baseurl + 'api/v1/vocabularies', response.data.content[0], {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).success(function (data) {
                        console.log(data);
                        //  alert('update was performed.');
                    });
                });
            },

            addTerms: function (cv) {
                CV.getCV(cv.domain).then(function (response) {
                    console.log(response);
                    var t2 = response.data.content[0].terms.concat(cv.terms);
                    var cv2 = response.data.content[0];
                    cv2.terms = t2;
                    console.log(cv2);
                    $http.put(baseurl + 'api/v1/vocabularies', cv2, {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).success(function (data) {

                        alert('update was performed.');
                    });
                });
            },

            addDomain: function (cv) {
                console.log("adding domain");
                console.log(cv);
                var c = {
                    domain: cv.domain
                    //terms: []
                };
                var promise = $http.post(baseurl + 'api/v1/vocabularies', c, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).success(function (response) {
                    console.log(response);
                    alert("new domain added");
                    return response;
                });
                return promise;
            }
        };
        return CV;
    });
    /*

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
     */

    /*    ginasFormElements.directive('duplicate', function (substanceFactory) {
     return {
     restrict: 'A',
     require: 'ngModel',
     link: function (scope, element, attrs, ngModel) {
     console.log(ngModel);
     console.log(scope);
     console.log(scope.name.name);
     if(!_.isUndefined(scope.name.name)) {
     substanceFactory.getSubstances(scope.name.name).then(function (response) {
     console.log(response);
     ngModel.$asyncValidators.duplicate = (response.count >= 1);
     });
     }
     }
     };
     });*/
    /*
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
     */
    ginasFormElements.factory('substanceFactory', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances?filter=names.name='";
        var substanceFactory = {};
        substanceFactory.getSubstances = function (name) {
            return $http.get(url + name + "'", {cache: true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            });
        };
        return substanceFactory;
    }]);

    ginasFormElements.service('download', function ($location, $http) {
        createURL = function () {
            var current = ($location.$$url).split('app')[1];
            var ret = baseurl + "api/v1" + current + '?view=full';
            return ret;
        };

        var download = {};

        download.fetch = function () {
            var url = createURL();
            return $http.get(url, {cache: true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
                return data.content;
            });

        };
        return download;
    });


    ginasFormElements.directive('checkBox', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/check-box.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                label: '@'
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
                field: '@',
                label: '@'
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

                scope.today = function () {
                    scope.object = new Date();
                };
                scope.today();

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
                field: '@',
                label: '@'
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

    ginasFormElements.directive('dropdownSelect', function (CVFields, filterService) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/dropdown-select.html",
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                label: '@',
                values: '=?',
                filter: '=',
                filterName: '@filter'
            },
            link: function (scope, element, attrs) {
                /*
                 console.log(attrs);
                 */
                if(_.isUndefined(scope.obj)){
                    scope.obj={};
                }
                scope.filterFunction = function () {
                    var filtered = [];
                    var family = scope.filter;
                    if (!_.isUndefined(family)) {
                        console.log(scope);
                        _.forEach(scope.values, function (value) {
                            if (_.isEqual(family, value.filter.split('=')[1])) {
                                filtered.push(value);
                            }
                        });
                        scope.values = filtered;
                        console.log(filtered);
                    }
                };
                if (scope.filter) {
                    console.log("there's a filter");
                    console.log(scope.filter);
                    console.log(scope.field);
                    filterService._register(scope.filterName, attrs.ngModel, scope.filter);

                    scope.$watch(scope.filter, function (newNames, oldNames) {
                        console.log(scope.filter);
                        console.log(newNames);
                        console.log(oldNames);
                    });

                }

                if (attrs.cv) {
                    CVFields.getCV(attrs.cv).then(function (response) {
                        scope.values = _.orderBy(response.data.content[0].terms, ['display'], ['asc']);
                        if (response.data.content[0].editable == true) {
                            scope.values.push(
                                {
                                    display: "Other",
                                    value: "Other",
                                    filter: " = ",
                                    selected: false
                                });
                        }
                        if (scope.filter) {
                            scope.filterFunction();

                            //   scope.filterCV(scope[filter]);
                        }
                        _.forEach(scope.values, function(term){
                            if(term.selected == true){
                                console.log(scope);
                                scope.obj  = term;
                            }
                        });


                    });
                }

                scope.makeNewCV = function () {
                    console.log(scope);
                    console.log(scope.obj.new);
                    var exists = _.find(scope.values, function (cv) {
                        return _.isEqual(_.lowerCase(cv.display), _.lowerCase(scope.obj.new)) || _.isEqual(_.lowerCase(cv.value), _.lowerCase(scope.obj.new));
                    });
                    if (!exists && scope.obj.new !== '') {
                        var cv = {};
                        cv.display = scope.obj.new;
                        cv.value = scope.obj.new;
                        scope.values.push(cv);
                        CVFields.updateCV(attrs.cv, cv);
                        console.log(cv);
                        scope.obj = cv;
                    } else {
                        alert(scope.obj.new + ' exists in the cv');
                        scope.obj = {};
                    }
                };

                scope.filterCV = function (field) {
                    console.log('filter by ' + field);
                };


            }
        }
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
                label: '@',
                values: '=?'
            },
            link: function (scope, element, attrs) {
                if (attrs.cv) {
                    CVFields.getCV(attrs.cv).then(function (response) {
                        scope.values = _.orderBy(response.data.content[0].terms, ['display'], ['asc']);
                        if (response.data.content[0].editable == true) {
                            scope.values.push(
                                {
                                    display: "Other",
                                    value: 'Other',
                                    filter: " = ",
                                    selected: false
                                });
                        }
                    });
                }

                scope.makeNewCV = function () {
                    console.log(scope);
                    console.log(scope.obj.new);
                    var exists = _.find(scope.values, function (cv) {
                        return _.isEqual(_.lowerCase(cv.display), _.lowerCase(scope.obj.new)) || _.isEqual(_.lowerCase(cv.value), _.lowerCase(scope.obj.new));
                    });
                    if (!exists && scope.obj.new !== '') {
                        var cv = {};
                        cv.display = scope.obj.new;
                        cv.value = scope.obj.new;
                        scope.values.push(cv);
                        CVFields.updateCV(attrs.cv, cv);
                        console.log(cv);
                        scope.obj = cv;
                    } else {
                        alert(scope.obj.new + ' exists in the cv');
                        scope.obj = {};
                    }
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

    ginasFormElements.directive('multiSelect', function (CVFields) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/multi-select.html",
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                cv: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                scope.tags = [];
                if (attrs.max) {
                    scope.max = attrs.max;
                } else {
                    scope.max = 'MAX_SAFE_INTEGER';
                }
                CVFields.getCV(scope.cv).then(function (data) {
                    if (scope.cv == 'LANGUAGE') {
                        var values = _.orderBy(data.data.content[0].terms, function (cv) {
                            return cv.display == 'English';
                        }, ['desc']);
                        scope.tags = values;
                    } else {
                        scope.tags = data.data.content[0].terms;
                    }

                });

                scope.loadItems = function ($query) {
                    var filtered = _.filter(scope.tags, function (cv) {
                        return cv.display.toLowerCase().indexOf($query.toLowerCase()) != -1;
                    });
                    var sorted = _.orderBy(filtered, function (cv) {
                        return _.startsWith(cv.display.toLowerCase(), $query.toLowerCase());
                    }, ['desc']);
                    return sorted;
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
                scope.tags = [];

                if (attrs.max) {
                    scope.max = attrs.max;
                } else {
                    scope.max = 'MAX_SAFE_INTEGER';
                }

                CVFields.getCV(scope.cv).then(function (data) {
                    var values = _.orderBy(data.data.content[0].terms, function (cv) {
                        return cv.display == 'English';
                    }, ['desc']);
                    scope.tags = values;

                });

                scope.loadItems = function ($query) {
                    var filtered = _.filter(scope.tags, function (cv) {
                        return cv.display.toLowerCase().indexOf($query.toLowerCase()) != -1;
                    });
                    var sorted = _.orderBy(filtered, function (cv) {
                        return _.startsWith(cv.display.toLowerCase(), $query.toLowerCase());
                    }, ['desc']);
                    return sorted;
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
                field: '@',
                name: '=',
                label: '@'
            }
        };
    });

    ginasFormElements.directive('textBoxViewEdit', function () {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/text-box-view-edit.html",
            replace: true,
            scope: {
                obj: '=',
                field: '@',
                label: '@',
                name: '=',
                validator: '&',
                changeValidator: '&'
            },
            link: function (scope, element, attrs, ngModel) {

                scope.editing = function (obj) {
                    scope.errors = [];
                    try {
                        scope.validator(obj);
                        if (_.has(obj, '_editing')) {
                            obj._editing = !obj._editing;
                        } else {
                            _.set(obj, '_editing', true);
                        }
                    } catch (e) {
                        scope.errors.push(e);
                    }
                };
                scope.errors = [];

                if (!scope.validator) {
                    scope.validator = function () {
                        //console.log("default validating");
                    };
                }

                if (!scope.changeValidator) {
                    scope.changeValidator = function () {
                        //console.log("default validating");
                    };
                }

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
                form: '@',
                validator: '&'
            },
            link: function (scope, elem, attrs, ngModel) {
                scope.validatorFunction = function () {
                    scope.validator({name: scope.obj});
                }
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


    ginasFormElements.directive('closeButton', function () {
        return {
            restrict: 'E',
            template: '<div class ="col-md-1 pull-right"><a ng-click="$parent.toggle();" class="pull-right"><i class="fa fa-times fa-2x danger" uib-tooltip="Close"></i></a></div>'
        };
    });

    ginasFormElements.directive('resolveButton', function ($compile, resolver, toggler) {
        return {
            restrict: 'E',
            scope: {
                name: '=',
                parent: '='
            },
            template: '<div class ="col-md-1 pull-left"><button class="btn btn-primary" ng-click="resolve(name);">Resolve Name</button></div>',
            link: function (scope, element, attrs) {
                scope.stage = true;
                scope.resolve = function (name) {
                    /*                    var result = document.getElementsByClassName(attrs.divid);
                     var elementResult = angular.element(result);*/
                    resolver.resolve(name).then(function (data) {
                        console.log(data.data);
                        _.set(scope, 'data', data.data);
                        if (data.data.length > 0) {
                            var template = angular.element('<substance-viewer data= data parent = parent></substance-viewer>');
                        } else {
                            template = angular.element('<div><h3>Name: ' + name + ' does not resolve to existing structure</h3></div>');
                        }
                        toggler.toggle(scope, attrs.divid, template);
                        /*                        elementResult.append(template);
                         $compile(template)(scope);*/
                    });

                    scope.close = function () {
                        toggler.toggle(scope, attrs.divid, template);
                    }
                }
            }
        };
    });

    ginasFormElements.service('resolver', function ($http, spinnerService) {
        var resolver = {};

        resolver.resolve = function (name, loading) {
            /*            if(loading){
             spinnerService.show(loading);
             }*/
            var url = baseurl + "resolve/" + name;
            return $http.get(url, {cache: true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
                return data;
            }).finally(function () {
                /*                if (loading) {
                 console.log("hiding spinner");
                 spinnerService.hide(loading);
                 }*/
            });

        };

        return resolver;
    });

    ginasFormElements.directive('substanceViewer', function (molChanger, CVFields) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                parent: '=',
                format: '@'
            },
            templateUrl: baseurl + "assets/templates/forms/substance-viewer.html",
            link: function (scope, element, attrs) {
                console.log(scope);
                if (scope.data.content) {
                    scope.subs = scope.data.content;
                } else {
                    scope.subs = scope.data;
                }
                scope.select = function (selected) {
                    console.log(selected);
                    if (selected.value && selected.value.molfile) {
                        molChanger.setMol(selected.value.molfile);
                    }
                    if (scope.format == "subref") {
                        console.log("passing");
                        scope.$parent.createSubref(selected);
                    }
                };
            }
        }
    });

    ginasFormElements.directive('subref', function ($compile, spinnerService) {
        return {
            replace: true,
            restrict: 'E',
            scope: {
                ref: '=',
                format: '='
            },
            link: function (scope, element, attrs) {
                console.log(scope);
                var template;
                if (scope.format == "subref") {
                    template = angular.element('<div>' +
                        '<rendered id= {{ref.uuid}} size="200"></rendered><br><code>{{ref.source}}</code><br>' +
                        '<button class = "btn btn-primary" ng-click="$parent.select(ref)">Select</button>' +
                        '</div>');
                } else if (scope.ref.uuid) {
                    var url = baseurl + 'substance/' + scope.ref.uuid.split('-')[0];
                    template = angular.element('<div>' +
                        '<rendered id = {{ref.uuid}} size="200"></rendered><br/><code>Ginas Duplicate</code><br/>' +
                        '<div class ="row"><div class="col-md-3 col-md-offset-3"><a class = "btn btn-primary" href = "' + url + '" target="_blank">View</a></div>' +
                        '<div class = "col-md-3"><a class = "btn btn-primary" href = "' + url + '/edit" target="_self">Edit</a></div>' +
                        '</div></div>');
                } else if (scope.ref == "empty") {
                    template = angular.element('<div><code>No substances found</code></div>');
                } else {
                    template = angular.element('<div>' +
                        '<rendered id= {{ref.value.id}} size="200"></rendered><br><code>{{ref.source}}</code><br>' +
                        '<button class = "btn btn-primary" ng-click="$parent.select(ref)">Use</button>' +
                        '</div>');
                }
                element.append(template);
                $compile(template)(scope);
                spinnerService.hideAll();
            }
        }
    });

    ginasFormElements.directive('substanceTypeahead', function (nameFinder) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/substance-typeahead.html",
            replace: true,
            scope: {
                subref: '=',
                field: '@'
            },
            link: function (scope, element, attrs) {

                scope.createSubref = function (selectedItem) {
                    console.log(selectedItem);
                    var temp = {};
                    temp.refuuid = selectedItem.uuid;
                    temp.refPname = selectedItem._name;
                    temp.approvalID = selectedItem.approvalID;
                    temp.substanceClass = "reference";
                    console.log(temp);
                    scope.subref = angular.copy(temp);
                    console.log(scope);

                };

                scope.loadSubstances = function ($query) {
                    var results = nameFinder.search($query);
                    console.log(results);
                    return results;
                };
            }
        };
    });

    ginasFormElements.directive('substanceChooserViewEdit', function (nameFinder) {
        return {
            templateUrl: baseurl + 'assets/templates/elements/substancechooser-view-edit.html',
            replace: true,
            restrict: 'E',
            scope: {
                obj: '=',
                field: '@',
                label: '@'
            },
            link: function (scope, element, attrs) {
                console.log(scope);
                scope.loadSubstances = function ($query) {
                    return nameFinder.search($query);
                };

                scope.createSubref = function (selectedItem) {
                    var subref = {};
                    subref.refuuid = selectedItem.uuid;
                    subref.refPname = selectedItem._name;
                    subref.approvalID = selectedItem.approvalID;
                    subref.substanceClass = "reference";
                    scope.obj[scope.field] = angular.copy(subref);
                    scope.diverse = [];
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

    ginasFormElements.directive('downloadButton', function ($compile, $timeout, download) {
        return {
            restrict: 'E',
            scope: {
                data: '='
            },
            link: function (scope, element, attrs) {
                var json;
                scope.url = '';
                scope.make = function () {
                    if (_.isUndefined(scope.data)) {
                        download.fetch().then(function (data) {
                            console.log(data);
                            json = JSON.stringify(data.data);
                            var b = new Blob([json], {type: "application/json"});
                            scope.url = URL.createObjectURL(b);
                            element.replaceWith($compile(
                                '<a class="btn btn-primary" download="results.json"' +
                                'href="' + scope.url + '" target = "_self" id ="download">' +
                                '<i class="fa fa-download" uib-tooltip="Download Page Results"></i>' +
                                '</a>'
                            )(scope));
                            document.getElementById('download').click();
                        });
                    } else {
                        var b;
                        var fileType = "json";
                        if (attrs.format === 'mol') {
                            b = new Blob([scope.data]);
                            fileType = "mol";
                        } else {
                            json = JSON.stringify(scope.data);
                            b = new Blob([json], {type: "application/json"});
                        }
                        scope.url = URL.createObjectURL(b);
                        element.replaceWith($compile(
                            '<a class="btn btn-primary" download="results.' + fileType +
                            '" href="' + scope.url + '" target = "_self" id ="download">' +
                            '<i class="fa fa-download" uib-tooltip="Download Results"></i>' +
                            '</a>'
                        )(scope));
                        $timeout(function () {
                            document.getElementById('download').click();
                        }, 100);
                    }
                }
            },
            template: '<a class="btn btn-primary" ng-click ="make()"><i class="fa fa-download" uib-tooltip="Download Results"></i></a>'
        };
    });

})();