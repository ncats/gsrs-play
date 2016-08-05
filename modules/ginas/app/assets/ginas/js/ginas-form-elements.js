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
                return $http.get(vocabulariesUrl, {
                    params: {"filter": "fields.term ='" + pathString + "'"},
                    cache: true
                }, {
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
                return $http.get(vocabulariesUrl, {
                    params: {"filter": "domain='" + domain.toUpperCase() + "'"},
                    cache: true
                }, {
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
            search: function (domain, query) {
                return CV.getCV(domain).then(function (data) {
                    return _.chain(data.data.content[0].terms)
                        .filter(function (x) {
                            return !query || x.value.toLowerCase().indexOf(query.toLowerCase()) > -1;
                        })
                        .sortBy('display')
                        .value();
                });
            },

            updateCV: function (domainobj) {
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
        var url = baseurl + "api/v1/substances/search?q=";
        var substanceFactory = {};
        substanceFactory.getSubstances = function (name) {
           // return $http.get(url, {params: {"filter": "names.name='" + name + "'"}, cache: true}, {
            return $http.get(url + "root_names_name='" + name + "'", {cache: true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            });
        };
        return substanceFactory;
    }]);

   ginasFormElements.service('download', function ($location, $http) {
        var cache = true;
        createURL = function (id) {
            var current = ($location.$$url).split('app')[1];
            var ret;
            var c = current.split('?');
            if(id){
                ret = baseurl + "api/v1/substances(" + id + ")?view=full";
                return ret;
            } else {

            if(c.length>1){
                var q= c[0] + '/search?'+ c[1];
                ret = baseurl + "api/v1" + q + '&view=full';
            } else {
                //quick fix to download vocabularies /cv's
                //should probably just be able to take a substance type
                if(current =="/admin"){
                    ret = baseurl + "api/v1/vocabularies?view=full&top=99";
                    cache = false;
                }else {
                    ret = baseurl + "api/v1" + current + '?view=full';
                }
            }
            return ret;
        }};

        var download = {};

        download.fetch = function (id) {
            var url = createURL(id);
            return $http.get(url, {cache: cache}, {
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
                label: '@',
                required: '=?'
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
                label: '@',
                required: '=?'
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
                field: '@',
                required: '=?'
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
                label: '@',
                required: '=?'
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

    ginasFormElements.directive('gsrsInput', function ($templateRequest, $compile, CVFields, filterService) {
        return {
            restrict: 'E',
            //templateUrl: baseurl + "assets/templates/elements/text-view-edit2.html",
            replace: true,
            require: '?ngModel',
            scope: {
                formname: '=', //the actual object of the form -- used for validation
                obj: '=ngModel',  //angular ng-model of obj.field type
                field: '@',  // text name of the field -- used for labels and form field retrieval
                label: '@',  //used if the label needs to display something other than the field
                formatter: '=?', //form object to watch to format values with. example: filtering code validation based on codeSystem type, mainly if the vocabulary term has a regex associated with it
                formattercheck: '@formatter', //string literal ofthe object name. this is a placeholder to check and see if there is supposed to be a formatter to watch.
                formatterFunction: '&?', //Formatters change how model values will appear in the view, like subunit display
                filter: '=?', //form object to watch to filter values with. example: filtering codeSystemType validation based on codeSystem
                filterFunction: '&?', // function called to filter input variables
                //validator function should accept a model object, and return an array of errors
                validator: '&?', //optional function passed in from the form directive and used to validate on init and change, used in conjunction with a formatter if available.
                blurValidator: '&?',  //optional validation function that is passed in from the form. this only fires on blur
                changeFunction:'&?', //validators happen in the background, and mainly set validation, so scope variable changes should go here
                //change functions happen before the field is set, so you can't get the new value without a watch
                required: '=?',  //optional variable that enables required form validation
                values: '=?' //array that can be passed with a custom cv for dropdowns and multi select
            },
            link: function (scope, element, attrs, ngModelCtrl) {

                //this function returns different templates, based on input. all above scope variables are callable
                //doing this first makes sure everything is attached to the scope before compiling the directive
                var templateurl;
                switch (attrs.type) {
                    case "dropdown":
                        templateurl = baseurl + "assets/templates/elements/dropdown-select.html";
                        break;
                    case "multi":
                        //used for the multiselect to filter and return the cv
                        scope.loadItems = function ($query) {
                            var filtered = _.filter(scope.values, function (cv) {
                                return cv.display.toLowerCase().indexOf($query.toLowerCase()) != -1;
                            });
                            var sorted = _.orderBy(filtered, function (cv) {
                                return _.startsWith(cv.display.toLowerCase(), $query.toLowerCase());
                            }, ['desc']);
                            return sorted;
                        };

                        //used to delete a multi -select list
                        scope.empty= function(){
                            scope.obj =[];
                        };
                        templateurl = baseurl + "assets/templates/elements/multi-select.html";
                        break;
                    case "text":
                        templateurl = baseurl + "assets/templates/elements/text-view-edit.html";
                        break;
                    case "text-box":
                        //this passes along the desired row count for a text box, without making it a scope variable
                        if (attrs.rows) {
                            scope.rows = attrs.rows;
                        }
                        templateurl = baseurl + "assets/templates/elements/text-box-view-edit.html";
                        break;
                }
                var temp = scope.obj;
                $templateRequest(templateurl).then(function (html) {
                    //this toggles the view of the element, an input if it is true, a link to toggle with the value if it is false
                    scope.edit = _.isUndefined(scope.obj);

                    //toggles the undo button for editing
                    scope.change = function () {
                        if (scope.obj) {
                            scope.edit = false;
                            if (!_.isEmpty(temp) && temp.value) {
                                scope.changed = true;
                            }
                        }

                    };

                    scope.undo = function () {
                        if (scope.changed == true) {
                            scope.obj = temp;
                            scope.edit = false;
                            scope.changed = false;
                        }
                    };

                    //this is used to:  1. filter one dropdown list based on the input of another down to one automatically selected value
                    //                  2. filter one dropdown list based on the input of another to a subset
                    //                  3. select a different cv for a dropdown, based on the input of another (structural modification residue)
                    if (scope.filter) {
                        filterService._register(scope, true);
                    }

                    //this will manage the cv retrieval for dropdown/multi select
                    if(attrs.cv){
                        scope.cv = attrs.cv;
                        CVFields.getCV(attrs.cv).then(function (response) {
                            scope.values = _.orderBy(response.data.content[0].terms, ['display'], ['asc']);
                            //bump English and United States up to the top
                            scope.values = _.orderBy( scope.values, function (cv) {
                                return cv.display == 'English' ||  cv.display == 'United States';
                            }, ['desc']);

                            if (response.data.content[0].filterable == true) {
                                filterService._register(scope, true);
                            }

                            if (response.data.content[0].editable == true) {
                                var other = [{
                                    display: "Other",
                                    value: "Other",
                                    filter: " = ",
                                    selected: false
                                }];
                                scope.values = _.union(scope.values, other);
                            }
                            _.forEach(scope.values, function(term){
                                if(term.selected == true){
                                    if (_.isUndefined(scope.obj)) {
                                        if (attrs.type == 'multi') {
                                            scope.obj = [];
                                            scope.obj.push(term);
                                        } else {
                                            scope.obj = term;
                                        }
                                        scope.edit = false;
                                    }
                                }
                                });

                        });

                        //adds a new cv element to the cv. the update cv function is not working however, and should be fixed
                        /////////////////////////////fix updateCV method
                        scope.makeNewCV = function () {
                            if(!_.isUndefined(scope.obj.new)) {
                                var exists = _.find(scope.values, function (cv) {
                                    return _.isEqual(_.lowerCase(cv.display), _.lowerCase(scope.obj.new)) || _.isEqual(_.lowerCase(cv.value), _.lowerCase(scope.obj.new));
                                });
                                if (!exists && scope.obj.new !== '') {
                                    var cv = {};
                                    cv.display = scope.obj.new;
                                    cv.value = scope.obj.new;
                                    scope.values.push(cv);
                                    CVFields.updateCV(attrs.cv, cv);
                                    scope.obj = cv;
                                } else {
                                    alert(scope.obj.new + ' exists in the cv');
                                    scope.obj = {};
                                }
                            }else{
                                scope.obj = undefined;
                            }
                        };

                        //this doesn't toggle so much as set false...
                        scope.toggleEdit = function () {
                            if (scope.obj) {
                                scope.edit = false;
                            }
                        };

                    }

                    //this sets a validator if there is a formatter/regex available. could also be set as ngModelCtrl.$formatters
                    //using the string literal for initialization
                    if (scope.validator && scope.formattercheck) {
                        //this allows invalid options to be shown, so if a formatter changes and the validation fails, it will not be shown
                        ngModelCtrl.$options ={
                           allowInvalid: true
                        };
                       //set the custom validator to the model controller
                        ngModelCtrl.$validators.customFormat = function (modelValue, viewValue) {
                            scope.errorMessages = [];
                                //pass the model and formatter, which should have a regex associated with it
                                scope.errorMessages = scope.validator({
                                    model: {
                                        model: modelValue,
                                        formatter: scope.formatter
                                    }
                                });
                                return !scope.errorMessages.length > 0;
                        };

                        //watch for a change in the formatting object, validation will need to be called again if the formatter changes
                        var formatter = scope.formatter;
                        scope.$watch('formatter', function () {
                            ngModelCtrl.$validate();
                        });
                    }

                    //validator with no formatting control -- used to parse subunits. nothing is returned for validation
                    if (scope.validator && !scope.formattercheck) {
                        ngModelCtrl.$validators.custom = function (modelValue) {
                            scope.errorMessages = scope.validator({model: modelValue});
                           if(!_.isUndefined(scope.errorMessages)) {
                               scope.obj = modelValue;
                               return !scope.errorMessages.length > 0;
                           }
                            return true;
                        };
                    }

                    //nothing uses this yet, it changes the viewValue of the model, but not the modelValue
                    if(scope.formatterFunction) {
                        ngModelCtrl.$formatters.format = function (modelValue) {
                            var ret = scope.formatterFunction({model: modelValue});
                        };
                    }

                    //blur function is a wrapper for the function passed in from the form, it adds the edit toggling.
                    scope.blurFunction = function() {
                        if (scope.blurValidator) {
                            ngModelCtrl.$validators.blur = function (modelValue) {
                                scope.errorMessages = scope.blurValidator({model: modelValue});
                                scope.obj = modelValue;
                                return !scope.errorMessages.length > 0;
                            };
                        }
                        if(scope.obj) {
                            scope.edit = false;
                        }
                    };
                    //multi select max allowed tags
                    if (attrs.max) {
                        scope.max = attrs.max;
                    } else {
                        scope.max = 'MAX_SAFE_INTEGER';
                    }

                    var template = angular.element(html);
                    element.append(template);
                    $compile(template)(scope);
                });
            }
        };
    });




    ginasFormElements.service('resolver', function ($http, spinnerService) {
        var resolver = {};

        resolver.resolve = function (name, loading) {
            if (loading) {
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

    ginasFormElements.directive('formHeader', function ($compile, $templateRequest) {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                type: '@',
                referenceobj: '=?',
                parent: '=',
                path: '@',
                iscollapsed: '=?',
                heading: '@'
            },
            link: function (scope, element, attrs) {
                scope.getLength = function () {
                    if (!_.isUndefined(_.get(scope.parent, scope.path))) {
                        scope.length = _.get(scope.parent, scope.path).length;
                    } else {
                        scope.length = 0;
                    }
                    return scope.length;
                };
                scope.toggle = function () {
                    scope.iscollapsed = !scope.iscollapsed;
                };

                scope.heading = _.startCase(scope.type);

                if (_.isUndefined(scope.path)) {
                    scope.path = scope.type;
                }
                scope.length = scope.getLength();

                if (scope.length == 0) {
                    scope.iscollapsed = true;
                }

                $templateRequest(baseurl + "assets/templates/selectors/form-header.html").then(function (html) {
                    template = angular.element(html);
                    element.append(template);
                    $compile(template)(scope);
                });
            }
        };
    });


    ginasFormElements.directive('substanceViewer', function (molChanger, UUID) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                parent: '=',
                obj: '=',
                format: '@',
                required: '=?'
            },
            templateUrl: baseurl + "assets/templates/forms/substance-viewer.html",
            link: function (scope, element, attrs) {
                if (scope.data.content) {
                    scope.subs = scope.data.content;
                } else {
                    scope.subs = scope.data;
                }
                scope.select = function (selected) {
                    if (scope.parent) {
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
                        if (selected.value && selected.value.molfile) {
                            molChanger.setMol(selected.value.molfile);
                        }

                        if (!_.isUndefined(scope.parent.structure)) {
                            if (_.isUndefined(scope.parent.structure.references)) {
                                _.set(scope.parent.structure, 'references', []);
                            }
                            scope.parent.structure.references.push(reference.uuid);
                        }
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
                        '<rendered id= {{ref.uuid}} size="200"></rendered><br/><code>{{ref._name}}</code><br/><code>{{ref.source}}</code><br>' +
                        '<button class = "btn btn-primary" ng-click="$parent.select(ref)">Select</button>' +
                        '</div>');
                } else if (scope.ref.uuid) {
                    var url = baseurl + 'substance/' + scope.ref.uuid.split('-')[0];
                    template = angular.element('<div>' +
                        '<rendered id = {{ref.uuid}} size="200"></rendered><br/><code>{{ref._name}}</code><br/><code>Ginas Duplicate</code><br/>' +
                        '<div class ="row"><div class="col-md-3 col-md-offset-3"><a class = "btn btn-primary" href = "' + url + '" target="_blank">View</a></div>' +
                        '<div class = "col-md-3"><a class = "btn btn-primary" href = "' + url + '/edit" target="_self">Edit</a></div>' +
                        '</div></div>');
                } else if (scope.ref == "empty") {
                    template = angular.element('<div><code>No substances found</code></div>');
                } else {
                    template = angular.element('<div>' +
                        '<rendered id= {{ref.value.id}} size="200"></rendered><br/><code>{{ref._name}}</code><br/><code>{{ref.source}}</code><br>' +
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
                label: '@',
                required: '=?'
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

    ginasFormElements.directive('substanceChooserSelector', function ($templateRequest, $compile, toggler, substanceFactory, spinnerService, CVFields) {
        return {
            replace: true,
            restrict: 'E',
            scope: {
                subref: '=ngModel',
                referenceobj: '=',
                formname: '@',
                field: '@',
                label: '@',
                type: '@'
            },
            link: function (scope, element, attrs, ngModel) {
                var template;
                scope.toggle = function () {
                    if (scope.stage == false) {
                        scope.q = null;
                    }
                    toggler.toggle(scope, scope.formname, template, scope.referenceobj);
                };

                scope.stage = true;

                scope.fetch = function (term, skip) {
                    if (_.isUndefined(scope.referenceobj) || scope.referenceobj == null) {
                        scope.referenceobj = {};
                    }
                    spinnerService.show('subrefSpinner');
                    substanceFactory.getSubstances(scope.q).then(function (response) {
                        scope.data = response.data.content;
                        spinnerService.hide('subrefSpinner');
                        template = angular.element('<substance-viewer data = data obj =referenceobj format= "subref"></substance-viewer>');
                        toggler.refresh(scope, scope.formname, template);
                    });
                };

                scope.createSubref = function (selectedItem) {
                    var temp = {};
                    temp.refuuid = selectedItem.uuid;
                    temp.refPname = selectedItem._name;
                    temp.approvalID = selectedItem.approvalID;
                    temp.substanceClass = "reference";
                    if (attrs.definition) {
                        var r = {relatedSubstance: temp};
                        CVFields.getCV('RELATIONSHIP_TYPE').then(function (response) {
                            var type = _.find(response.data.content[0].terms, ['value', 'SUB_ALTERNATE->SUBSTANCE']);
                            //var type = _.find(response.data.content[0].terms, ['value', 'Alternative Definition']);
                            r.type = type;
                        });
                        if (!_.has(scope.referenceobj, 'relationships')) {
                            _.set(scope.referenceobj, 'relationships', []);
                        }
                        scope.referenceobj.relationships.push(r);
                    }

                    _.set(scope.referenceobj, scope.field, angular.copy(temp));
                    scope.q = null;
                    scope.stage = false;
                    toggler.toggle(scope, scope.formname);
                };


                switch (scope.type) {
                    case "lite":
                        $templateRequest(baseurl + "assets/templates/substance-select.html").then(function (html) {
                            template = angular.element(html);
                            element.append(template);
                            $compile(template)(scope);
                        });
                        break;
                    case "search":
                        $templateRequest(baseurl + "assets/templates/substance-search.html").then(function (html) {
                            template = angular.element(html);
                            element.append(template);
                            $compile(template)(scope);
                        });
                        if (attrs.definition) {
                            scope.definition = attrs.definition;
                        }
                        break;
                }
            }
        };
    });

    ginasFormElements.directive('substanceView', function ($compile) {
        return {
            replace: true,
            restrict: 'E',
            scope: {
                subref: '=',
                size: '='
            },
            link: function (scope, element) {
                var template = angular.element('<div><rendered id = {{subref.refuuid}} size = {{size}}></rendered><br/><code>{{subref.refPname}}</code></div>');
                element.append(template);
                $compile(template)(scope);
            }
        };
    });
    
    //////////////BUTTONS///////////////////////////

    ginasFormElements.directive('downloadButton', function ($compile, $timeout, download, CVFields) {
        return {
            restrict: 'E',
            scope: {
                data: '=?',
                format: '=?',
                uuid: '@?',
                refresh: '='
            },
            link: function (scope, element, attrs) {
                var json;
                scope.url = '';
                if(scope.refresh){
                    element.empty();
                }
                scope.make = function () {
                    if (_.isUndefined(scope.data) && _.isUndefined(scope.uuid) ) {
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
                    } else if (_.isUndefined(scope.data) && (!_.isUndefined(scope.uuid)) ) {
                        download.fetch(scope.uuid).then(function (data) {
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
                    }else {
                        var b;
                        var fileType = "json";
                        if (scope.format) {
                            b = new Blob([scope.data]);
                            fileType = scope.format;
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

    ginasFormElements.directive('deleteButton', function () {
        return {
            restrict: 'E',
            template: '<label ng-if=!showlabel>Delete</label><br/><a ng-click="deleteObj()" uib-tooltip="Delete Item"><i class="fa fa-trash fa-2x danger"></i></a>',
            link: function (scope, element, attrs) {
                    scope.showlabel= attrs.showlabel;

                    scope.deleteObj = function () {
                            scope.$emit('delete');
                            if (scope.parent) {
                                var arr = _.get(scope.parent, attrs.path);
                                arr.splice(arr.indexOf(scope.obj), 1);
                            } else if(scope.substance) {
                                scope.substance[attrs.path].splice(scope.substance[attrs.path].indexOf(scope.obj), 1);
                            }else{
                               // console.log("not a substance");
                            }
                            scope.$emit('removed');
                    };

            }
        };
    });

    ginasFormElements.directive('infoButton', function ($compile, toggler) {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                type: '@',
                path: '@',
                heading: '@'
            },
            link: function (scope, element, attrs) {
                scope.stage = true;
                var template;
                var url = baseurl + "assets/templates/info/";
                if (attrs.mark == "exclaim") {
                    template = angular.element('<span ng-click ="showInfo()"><i class="fa fa-exclamation-circle fa-lg" uib-tooltip="click for description"></i></span>');
                } else {
                    template = angular.element('<span ng-click ="showInfo()"><i class="fa fa-question-circle fa-lg"  uib-tooltip="click for description"></i></span>');
                }
                element.append(template);
                $compile(template)(scope);
                if (attrs.info) {
                    url = url + attrs.info + '-info.html';
                } else {
                    url = url + 'code-info.html';
                }


                scope.showInfo = function () {
                    toggler.show(scope, scope.type, url);
                };
            }
        }
    });

    ginasFormElements.directive('modalFormButton', function ($uibModal) {
        return {
            scope: {
                referenceobj: '=?',
                parent: '=',
                label: '@?',
                edit: '=?',
                subclass: '@?',
                field: '@?',
                formname: '=', //the actual object of the form -- used for validation
                required: '='
            },
            templateUrl: function(tElem, tAttrs){
                var templateurl;
                switch (tAttrs.type) {
                    case "access":
                        templateurl =  baseurl + "assets/templates/selectors/access-selector.html";
                        break;
                    case "amount":
                        templateurl =  baseurl + "assets/templates/selectors/amount-selector.html";
                        break;
                    case "nameorg":
                        templateurl =  baseurl + "assets/templates/selectors/name-org-selector.html";
                        break; 
                    case "parameter":
                        templateurl =  baseurl + "assets/templates/selectors/parameter-selector.html";
                        break;
                    case "reference":
                        templateurl =  baseurl + "assets/templates/selectors/reference-selector.html";
                        break;
                    case "sites":
                        templateurl =  baseurl + "assets/templates/selectors/site-selector.html";
                        break;
                   /* case "terms":
                        templateurl =  baseurl + "assets/templates/selectors/cv-terms-selector.html";
                        break;*/
                }
                return templateurl;
            },



////////////See if this is can be moved to reference form or header form///////////////////////////////
            link: function(scope, element){
                //this is used in the reference form to apply the references to structure/protein etc.
                if(_.isUndefined(scope.referenceobj)){
                    var subClass = scope.parent.substanceClass;
                    if(subClass ==="chemical"){
                        subClass = "structure";
                    }
                    if(subClass ==="specifiedSubstanceG1"){
                        subClass = "specifiedSubstance";
                    }
                    scope.referenceobj = _.get(scope.parent, subClass);
                }

            },

         //   templateUrl: baseurl + "assets/templates/selectors/reference-selector.html",

            controller: function ($scope, $element, $attrs) {

                                
                $scope.opened=false;
                var modalInstance;

                $scope.close = function () {
                    $scope.opened= false;
                    //this has a listener in the reference form that applies the reference to the array of the object
                    //might need to check the type before calling it.
                    $scope.$broadcast('save');
                    modalInstance.close();
                };

                $scope.cancel = function () {
                    $scope.opened= false;
                    $scope.$broadcast('cancel');
                    modalInstance.close();
                };



                $scope.open = function () {
                    modalInstance = $uibModal.open({
                        templateUrl: function(tElem, tAttrs){
                            var templateurl;
                            switch ($attrs.type) {
                                case "access":
                                    templateurl =  baseurl + "assets/templates/modals/access-modal.html";
                                    break;
                                case "amount":
                                    if(!$scope.field){
                                        $scope.field = "amount";
                                    }
                                    templateurl =  baseurl + "assets/templates/modals/amount-modal.html";
                                    break;
                                case "nameorg":
                                    templateurl =  baseurl + "assets/templates/modals/name-org-modal.html";
                                    break;
                                case "parameter":
                                    templateurl =  baseurl + "assets/templates/modals/parameter-modal.html";
                                    break;
                                case "reference":
                                    templateurl =  baseurl + "assets/templates/modals/reference-modal.html";
                                    break;
                                case "sites":
                                    $scope.formtype=$attrs.formtype;
                                    $scope.residueregex = $attrs.residueregex;
                                    templateurl =  baseurl + "assets/templates/modals/site-modal.html";
                                    break;
                                case "terms":
                                    if($attrs.index){
                                        $scope.index = $attrs.index;
                                    }
                                    if($scope.cv){
                                        $scope.cv = $attrs.cv;
                                    }
                                    templateurl =  baseurl + "assets/templates/modals/cv-terms-modal.html";
                                    break;
                            }
                            return templateurl;

                        },
                        size: 'xl',
                        scope: $scope,
                        resolve: {
                            parent: function () {
                                return $scope.substance;
                            }
                        }
                    });
                    //this handles clicking outside of the modal to close it
                    modalInstance.result.then(function(){
                    }, function(){
                        $scope.close();
                    });

                };
            }
        }
    });

    ginasFormElements.directive('referenceApply', function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                apply: '=?ngModel', //obj.apply
                obj: '=?', //this is the reference object itself from the form,
                referenceobj: '=',
                parent: '='
            },
            link: function (scope, element, attrs) {
                var uuid;
                var index;
                var template;
               // scope.apply = false;
                if (_.isUndefined(scope.referenceobj)) {
                    scope.referenceobj = {};
                }

                if (_.isUndefined(scope.referenceobj.references)) {
                    var x = [];
                    _.set(scope.referenceobj, 'references', x);
                }

                scope.isReferenced = function () {
                    return index >= 0;
                };

                        template = angular.element('<div class = "text-center"><label>Apply</label><br><input type="checkbox" ng-model="apply" ng-click="updateReference();" placeholder="{{field}}" title="{{field}}" id="{{field}}s"/></div>');
                        element.append(template);
                        $compile(template)(scope);
                        uuid = scope.obj.uuid;
                        index = _.indexOf(scope.referenceobj.references, uuid);
                        _.set(scope.obj, '$$apply', index >= 0);
                        scope.parent.references = _.orderBy(scope.parent.references, ['$$apply'], ['desc']);


                scope.updateReference = function () {
                    index = _.indexOf(scope.referenceobj.references, uuid);
                    if (index >= 0) {
                        scope.referenceobj.references.splice(index, 1);
                        scope.obj.$$apply = false;
                    } else {
                        scope.referenceobj.references.push(uuid);
                        scope.obj.$$apply = true;
                    }
                };

            }
        };
    });

    ginasFormElements.directive('enforceMaxTags', function() {
        return {
            require: 'ngModel',
            link: function(scope, element, attrs, ngCtrl) {
                var maxTags = attrs.maxTags ? parseInt(attrs.maxTags, '10') : null;
                ngCtrl.$validators.checkLength = function(value) {
                    if (value && maxTags && value.length > maxTags) {
/*
                        errors.push({text: 'Max number allowed is '+maxTags , type: 'danger'});
*/
                        value.splice(value.length - 1, 1);
                    }
                    return value;
                };
            }
        };
    });
})();
