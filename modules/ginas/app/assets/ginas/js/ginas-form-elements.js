(function () {
    var ginasFormElements = angular.module('ginasFormElements', []);

    ginasFormElements.factory('CVFields', function ($http, $q) {

        var load = function(){
            return $http.get( baseurl + "api/v1/vocabularies?filter=domain='CV_DOMAIN'",{cache:true},{
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
             //   console.log(data);
                // lookup= data.content[0].terms;
                return data;
            });
        };


        var url = baseurl + "api/v1/vocabularies?filter=domain='";
        var CV = {
           getDomain: function(path){
                var ret;
             return load().then(function(data){
                 var terms = data.data.content[0].terms;
                var patharr = path.split('.');
                if(patharr.length>2){
                    patharr=  _.takeRight(patharr, 2);
                }
                var pathString = _.join(patharr, '.');
                     var domain = _.find(terms, function(cv) {
                        return cv.value == pathString;
                    });
                    if(!_.isUndefined(domain)){
                        ret = domain.display;
                    }
                    return ret;
                });
            },

            getCV: function(domain){
                return $http.get(url + domain.toUpperCase() + "'", {cache: true}, {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    return data;
                });
            },

            count: function(){
                 var counturl = baseurl + "api/v1/vocabularies";
                return $http.get(counturl,{cache:true},{
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).success(function (data) {
                    return data;
                });
            },

            all: function(){
                 var allurl = baseurl + "api/v1/vocabularies?top=999";
                return $http.get(allurl,{cache:true},{
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
                    .sortBy('display')
                    .value();
            },

/*
            lookup: function (field, query) {
                return _.chain(CV[field])
                    .filter(function (x) {
                        return !query || x.value.toLowerCase().indexOf(query.toLowerCase()) > -1;
                    })
                    .sortBy('value')
                    .value();
            },
*/

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

    ginasFormElements.service('download', function($location, $http){
        createURL = function(){
            console.log('creating url');
            var current = ($location.$$url).split('app')[1];
            var ret = baseurl + "api/v1" +current +'?view=full';
            console.log(ret);
            return ret;
        };
        var download = {};

        download.fetch = function(){
            console.log('fetching');
            var url = createURL();
            return $http.get(url,{cache:true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
                console.log(data);
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
                CVFields.getCV(attrs.cv).then(function (data) {
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
                CVFields.getCV(attrs.cv).then(function (data) {
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
                CVFields.load(scope.cv);

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
                //CVFields.getCV(scope.cv).then(function(data){
                //   // console.log(scope.obj[scope.field]);
                //});

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

    ginasFormElements.directive('closeButton', function () {
        return {
            restrict: 'E',
            template: '<div class ="col-md-1 pull-right"><a ng-click="$parent.toggle();" class="pull-right"><i class="fa fa-times fa-2x danger" uib-tooltip="Close"></i></a></div>'
        };
    });

    ginasFormElements.directive('resolveButton', function ($compile, resolver, toggler) {
        return {
            restrict: 'E',
            scope:{
                name: '=',
                parent: '='
            },
            template: '<div class ="col-md-1 pull-left"><button class="btn btn-primary" ng-click="resolve(name);">Resolve Name</button></div>',
            link: function(scope,element, attrs){
                scope.stage=true;
                scope.resolve= function(name){
/*                    var result = document.getElementsByClassName(attrs.divid);
                    var elementResult = angular.element(result);*/
                    resolver.resolve(name).then(function(data){
                        console.log(data.data);
                        _.set(scope, 'data', data.data);
                        if(data.data.length>0) {
                            var template = angular.element('<substance-viewer data= data parent = parent></substance-viewer>');
                        }else{
                            template = angular.element('<div><h3>Name does not resolve to existing structure</h3></div>');
                        }
                        toggler.toggle(scope, attrs.divid, template);
/*                        elementResult.append(template);
                        $compile(template)(scope);*/
                    });

                    scope.close= function(){
                        toggler.toggle(scope, attrs.divid, template);
                    }
                }
            }
        };
    });

    ginasFormElements.service('resolver', function($location, $http){
        var resolver = {};

        resolver.resolve= function(name){
            var url = baseurl + "resolve/"+name;
            return $http.get(url,{cache:true}, {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
                return data;
            });
        };

        return resolver;
    });

ginasFormElements.directive('substanceViewer', function(){
    return{
        restrict: 'E',
        scope:{
            data: '=',
            parent: '='
        },
        templateUrl: baseurl + "assets/templates/forms/substance-viewer.html",
        link: function(scope,element, attrs) {
            console.log(scope);
            scope.select = function(selected){
                console.log(selected);
                _.set(scope.parent, 'structure', selected.value);
                scope.$parent.close();
            };
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

    ginasFormElements.directive('downloadButton', function ($compile, download) {
        return {
            restrict:'E',
            scope:{
                data: '='
            },
            link:function (scope, elm, attrs) {
                    var json;
                    var url;
                    if(_.isUndefined(scope.data)){
                        download.fetch().then(function (data) {
                            console.log(data);
                           json =  JSON.stringify(data.data);
                            var b= new Blob([json], {type: "application/json"});
                            url = URL.createObjectURL(b);
                            elm.append($compile(
                                '<a class="btn btn-primary" download="results.json"' +
                                'href="' + url + '" target = "_sef">' +
                                '<i class="fa fa-download" uib-tooltip="Download Results"></i>' +
                                '</a>'
                            )(scope));
                        });
                    }else {
                        json = JSON.stringify(scope.data);
                        var b = new Blob([json], {type: "application/json"});
                        url = URL.createObjectURL(b);
                        elm.append($compile(
                            '<a class="btn btn-primary" download="results.json"' +
                            'href="' + url + '" target = "_sef">' +
                            '<i class="fa fa-download" uib-tooltip="Download Results"></i>' +
                            '</a>'
                        )(scope));
                    }
            }
        };
    });




})();