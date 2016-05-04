(function () {
    var ginasFormElements = angular.module('ginasFormElements', []);

    ginasFormElements.factory('CVFields', function ($http, $q) {
        var vocabulariesUrl = baseurl + "api/v1/vocabularies";
        var CV = {
            //used for expanding a cv
            getByField: function (path) {
                var patharr = path.split('.');
                if (patharr.length > 2) {
                    patharr = _.takeRight(patharr, 2);
                }
                var pathString = _.join(patharr, '.');
                return $http.get(vocabulariesUrl,{params: {"filter": "fields.term ='"+pathString+"'"}, cache: true}, {
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
            },
            //used to load cv in form elements
            getCV: function (domain) {
                return $http.get(vocabulariesUrl, {params: {"filter": "domain='" + domain.toUpperCase() + "'"}, cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    return data;
                });
            },
            //used in admin for cv count
            count: function () {
                return $http.get(vocabulariesUrl, {cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    return data;
                });
            },
            //used to download cv
            all: function (cache) {
                var allurl = baseurl + "api/v1/vocabularies?top=999";
                return $http.get(allurl, {cache: cache}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    return data;
                });
            },
            //not currently used, but may become useful
            search: function (field, query) {
                return _.chain(CV[field])
                    .filter(function (x) {
                        return !query || x.display.toLowerCase().indexOf(query.toLowerCase()) > -1;
                    })
                    .sortBy('display').value();
            },
            //not currently used, but may become useful
            searchTags: function (domain, query) {
                return CV.getCV(domain).then(function (data) {
                    return _.chain(data.data.content[0].terms)
                        .filter(function (x) {
                            return !query || x.display.toLowerCase().indexOf(query.toLowerCase()) > -1;
                        })
                        .sortBy('display')
                        .value();
                });
            },

            updateCV: function (domainobj) {
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
                        alert("new domain added");
                        return response;
                    });
                }
                return promise;
            },

            addCV: function (field, newcv) {
                CV.getCV(field).then(function (response) {
                    response.data.content[0].terms.push(newcv);
                    $http.put(baseurl + 'api/v1/vocabularies', response.data.content[0], {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).success(function (data) {
                        //  alert('update was performed.');
                    });
                });
            },

            addTerms: function (cv) {
                CV.getCV(cv.domain).then(function (response) {
                    var t2 = response.data.content[0].terms.concat(cv.terms);
                    var cv2 = response.data.content[0];
                    cv2.terms = t2;
                    $http.put(baseurl + 'api/v1/vocabularies', cv2, {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).success(function (data) {

                        alert('update was performed.');
                    });
                });
            },

            addDomain: function (domain) {
                console.log(domain);
                return CV.getCV(domain.domain).then(function (response) {
                    if (response.data.count == 0) {
                        $http.post(baseurl + 'api/v1/vocabularies', domain, {
                            headers: {
                                'Content-Type': 'application/json'
                            }
                        }).success(function (response) {
                            alert("new domain added");
                            return response;
                        });
                    }
                });
            }
        };
        return CV;
    });
    ginasFormElements.factory('substanceFactory', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances";
        var substanceFactory = {};
        substanceFactory.getSubstances = function (name) {
            return $http.get(url, {params: {"filter": "names.name='" + name + "'"}, cache: true}, {
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



    //filterFunction allows for choosing which cv to be loaded for each dropdown. Foe example, displaying amino acid vs nucleic acid bases based on input.
    //filter is an object that a $watch is set on. when that object changes, the currently loaded cv is filtered based on the value. these filtering options are set in the cv
    ginasFormElements.directive('dropdownSelect', function (CVFields, filterService) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/dropdown-select.html",
            replace: true,
            scope: {
                obj: '=ngModel',
                cv:'@',
                field: '@',
                label: '@',
                values: '=?',
                filter: '=',
                filterField:'@filter',
                filterFunction: '&?'
            },
            link: function (scope, element, attrs) {
                var other = [{
                display: "Other",
                    value: "Other",
                    filter: " = ",
                    selected: false
            }];
                if(_.isUndefined(scope.obj)){
                  //  scope.obj={};
                }
                if (scope.cv) {
                    CVFields.getCV(scope.cv).then(function (response) {
                        scope.values = _.orderBy(response.data.content[0].terms, ['display'], ['asc']);

                        if (response.data.content[0].filterable == true) {
                            filterService._register(scope);
                        }

                        if (response.data.content[0].editable == true) {
                           scope.values =  _.union(scope.values, other);
                        }

                        _.forEach(scope.values, function (term) {
                            if (term.selected == true) {
                                scope.obj = term;
                            }
                        });
                    });
                }

                scope.makeNewCV = function () {
                    if(_.isUndefined(scope.obj)){
                        scope.obj={};
                    }
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
            }
    };
});

    ginasFormElements.directive('dropdownViewEdit', function (CVFields, filterService) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/dropdown-view-edit.html",
            replace: true,
            scope: {
                formname: '=',
                cv:'@',
                obj: '=',
                field: '@',
                label: '@',
                values: '=?',
                filter: '=',
                filterField:'@filter',
                filterFunction: '&?'
            },
            link: function (scope, element, attrs) {
                if (scope.cv) {
                    CVFields.getCV(scope.cv).then(function (response) {
                        scope.values = _.orderBy(response.data.content[0].terms, ['display'], ['asc']);

                        if (response.data.content[0].filterable == true) {
                            filterService._register(scope, true);
                        }

                        if (response.data.content[0].editable == true) {
                            scope.values =  _.union(scope.values, other);
                        }
                    });
                }

                scope.makeNewCV = function () {
                    var exists = _.find(scope.values, function (cv) {
                        return _.isEqual(_.lowerCase(cv.display), _.lowerCase(scope.obj[scope.field].new)) || _.isEqual(_.lowerCase(cv.value), _.lowerCase(scope.obj[scope.field].new));
                    });
                    if (!exists && scope.obj[scope.field].new !== '') {
                        var cv = {};
                        cv.display = scope.obj[scope.field].new;
                        cv.value = scope.obj[scope.field].new;
                        scope.values.push(cv);
                        CVFields.updateCV(attrs.cv, cv);
                        scope.obj[scope.field] = cv;
                    } else {
                        alert(scope.obj[scope.field].new + ' exists in the cv');
                        scope.obj[scope.field] = {};
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
                tags: '=?',
                field: '@',
                cv: '@',
                label: '@',
                filter: '=',
                filterFunction: '&'
            },
            link: function (scope, element, attrs) {
                if (attrs.max) {
                    scope.max = attrs.max;
                } else {
                    scope.max = 'MAX_SAFE_INTEGER';
                }
                
                //this allows the switching of cv depending on an external value
                if (scope.filter) {
                    scope.$watch('filter', function (newValue) {
                        if(!_.isUndefined(newValue)) {
                            var cv = scope.filterFunction({type: newValue});
                            CVFields.getCV(cv).then(function (response) {
                                scope.obj = [];
                                scope.tags = response.data.content[0].terms;
                            });
                        }
                    });
                }

                if (attrs.cv) {
                    scope.tags = [];
                    CVFields.getCV(attrs.cv).then(function (response) {
                        scope.tags = response.data.content[0].terms;
                        _.forEach(scope.tags, function (term) {
                            if (term.selected == true) {
                                if(_.isUndefined(scope.obj)){
                                    scope.obj=[];
                                }
                                scope.obj.push(term);
                            }
                        });
                        /*if (scope.cv == 'LANGUAGE') {
                            var values = _.orderBy(response.data.content[0].terms, function (cv) {
                                return cv.display == 'English';
                            }, ['desc']);
                            scope.tags = values;
                        } else {*/
                            scope.tags = response.data.content[0].terms;
                        //}
                    });
                }
                
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
                values: '=?',
                field: '@',
                tags: '=?',
                cv: '@',
                label: '@',
                filter: '=',
                filterFunction: '&'
            },
            link: function (scope, element, attrs) {
                scope.tags = [];

                if (attrs.max) {
                    scope.max = attrs.max;
                } else {
                    scope.max = 'MAX_SAFE_INTEGER';
                }

                if (scope.filter) {
                    scope.$watch('filter', function (newValue, oldValue) {
                        if(!_.isUndefined(newValue)) {
                            var cv = scope.filterFunction({type: newValue});
                            if (!_.isNull(cv)) {
                                CVFields.getCV(cv).then(function (response) {
                                    if (!_.isEqual(newValue, oldValue)) {
                                        scope.obj[scope.field] = [];
                                    }
                                    // scope.obj[scope.field] = [];
                                    scope.tags = response.data.content[0].terms;
                                });
                            }
                        }
                    });
                }

                if(scope.cv) {
                    CVFields.getCV(scope.cv).then(function (data) {
                        var values = _.orderBy(data.data.content[0].terms, function (cv) {
                            return cv.display == 'English';
                        }, ['desc']);
                        scope.tags = values;

                    });
                }
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

    ginasFormElements.directive('textInput', function (filterService) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/templates/elements/text-input.html",
            require: '^ngModel',
            replace: true,
            scope: {
                obj: '=ngModel',
                field: '@',
                label: '@',
                form: '=?',
                filter: '=?',
                filterFunction: '&?',
                validator: '&?'
            },
            link: function (scope, elem, attrs, ngModel) {

                scope.validatorFunction = function () {
                    if(scope.validator) {
                        scope.errorMessages = scope.validator({model: scope.obj});
                    }
                };

                if (scope.filter &&!_.isEmpty(scope.filter)){
                    var filter = scope.filter;
                    scope.$watch('filter', function (newValue) {
                        if(!_.isUndefined(newValue)) {
                         scope.errorMessages = scope.validator({model:scope.obj});
                        }
                    });
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
                label: '@',
                form: '=?',
                filter: '=',
                filterFunction: '&',
                validator: '&'
            },
            link: function (scope, element, attrs, ngModel) {
                scope.edit = false;
                scope.editing = function () {
                    scope.edit = !scope.edit;
                };
                scope.validatorFunction = function () {
                    scope.errorMessages = scope.validator({model:scope.obj});
                };

                if (scope.filter){
                    var filter = scope.filter;
                    scope.$watch('filter', function (newValue) {
                        if(!_.isUndefined(newValue)) {
                            scope.errorMessages = scope.validator({model:scope.obj});
                        }
                    });
                }
            }
        };
    });

    ginasFormElements.directive('closeButton', function () {
        return {
            restrict: 'E',
            template: '<div class ="col-md-1 pull-right"><a ng-click="$parent.toggle();" class="pull-right"><i class="fa fa-times fa-2x danger" uib-tooltip="Close"></i></a></div>'
        };
    });

    ginasFormElements.service('resolver', function ($http, spinnerService) {
        var resolver = {};

        resolver.resolve = function (name, loading) {
                        if(loading){
             spinnerService.show(loading);
             }
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

    ginasFormElements.directive('substanceViewer', function (molChanger, UUID) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                parent: '=',
                obj: '=',
                format: '@'
            },
            templateUrl: baseurl + "assets/templates/forms/substance-viewer.html",
            link: function (scope, element, attrs) {
                if (scope.data.content) {
                    scope.subs = scope.data.content;
                } else {
                    scope.subs = scope.data;
                }
                scope.select = function (selected) {
                    if(scope.parent) {
                        var reference = {
                            uuid: UUID.newID(),
                            apply: true,
                            docType: {value: "resolver", display: "resolver"},
                            citation: selected.source,
                            documentDate: moment()._d
                        };
                        if (scope.obj) {
                            if (_.isUndefined(scope.obj.references)) {
                                _.set(scope.obj, 'references', []);
                            }
                            scope.obj.references.push(reference.uuid);
                        }
                        scope.parent.references.push(reference);
                    }
                    if (selected.value && selected.value.molfile) {
                        molChanger.setMol(selected.value.molfile);
                    }

                    if (!_.isUndefined(scope.parent.structure)) {
                        if (_.isUndefined(scope.parent.structure.references)) {
                            _.set(scope.parent.structure, 'references', []);
                        }
                        scope.parent.structure.references.push(reference.uuid);
                    }

                    if (scope.format == "subref") {
                        scope.$parent.createSubref(selected);
                    }
                    delete scope.subs;
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
                    var temp = {};
                    temp.refuuid = selectedItem.uuid;
                    temp.refPname = selectedItem._name;
                    temp.approvalID = selectedItem.approvalID;
                    temp.substanceClass = "reference";
                    scope.subref = angular.copy(temp);

                };

                scope.loadSubstances = function ($query) {
                    var results = nameFinder.search($query);
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