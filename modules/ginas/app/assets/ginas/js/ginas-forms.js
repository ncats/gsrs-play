(function () {
    var ginasForms = angular.module('ginasForms', ['bootstrap.fileField']);

    ginasForms.service('toggler', function ($compile, $templateRequest, spinnerService) {

        var childScope;
        this.stageCheck = function () {
            return this.stage;
        };

        this.show = function (scope, element, url) {
            if(_.isUndefined(scope.type)){
                scope.type = element;
            }
            if(_.isUndefined(scope.stage)){
                scope.stage = true;
            }
            var template = "";
            var result = document.getElementsByClassName(scope.type);
            var elementResult = angular.element(result);
            if (scope.stage === true) {
                scope.stage = false;
                $templateRequest(url).then(function (html) {
                    template = angular.element(html);
                    elementResult.append(template);
                    $compile(elementResult)(scope);
                });
            } else {
                elementResult.empty();
                scope.stage = true;
            }
        };

        this.refresh = function (scope, element, template, loading) {
/*            if(scope.data.length > 0) {
                spinnerService.hideAll();
                spinnerService.show('drawSpinner');*/
                var result = angular.element(document.getElementsByClassName(element));
                result.html(template);
                $compile(result)(scope);
         //   }
        };

        this.toggle = function (scope, element, template) {
            var result = document.getElementsByClassName(element);
            var elementResult = angular.element(result);
            if (scope.stage === true) {
                scope.stage = false;
                childScope = scope.$new();
                var compiledDirective = $compile(template);
                var directiveElement = compiledDirective(childScope);
                elementResult.append(directiveElement);
            } else {
                if(childScope) {
                    childScope.$destroy();
                }
                elementResult.empty();
                scope.stage = true;
            }
        };
    });

    ginasForms.directive('formHeader', function ($compile, $templateRequest) {
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
                scope.getLength = function(){
                    if (!_.isUndefined(_.get(scope.parent, scope.path))) {
                        scope.length = _.get(scope.parent, scope.path).length;
                    }else{
                        scope.length =0;
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
            /*link: function (scope, element, attrs) {
                scope.getLength = function(){
                    if (!_.isUndefined(_.get(scope.parent, scope.path))) {
                        scope.length = _.get(scope.parent, scope.path).length;
                    }else{
                        scope.length =0;
                    }
                    return scope.length;
                };
                scope.toggle = function () {
                    scope.iscollapsed = !scope.iscollapsed;
                };
               	if(!scope.heading || scope.heading==""){
					scope.heading=scope.title;
				}
				if(!scope.heading || scope.heading==""){
					scope.heading = _.startCase(scope.type);
					console.log("heading wasn't defined");
					console.log(scope.heading);
				}else{
					console.log("heading was defined");
				}
				scope.title=scope.heading;
				
                
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
            }*/
        };
    });

    ginasForms.directive('formmanager', function ($compile, $templateRequest, toggler) {
        return {
            controller: function ($scope) {
                this.scope = $scope;
               // this.referenceRetriever = referenceRetriever;
                $scope.addClass = [];
                this.setClass = function (index) {
                    $scope.addClass[index] = "success";
                };
                this.getClass = function (index) {
                    return $scope.addClass[index];
                };

                this.removeClass = function () {
                    $scope.addClass = [];
                };

                this.scrollTo = function () {
                    $scope.scrollTo('refs');
                };

                this.toggle = function (scope, divid) {
                    $scope.addClass = [];
                    var url = baseurl + "assets/templates/reference-table.html";
                    scope.references = _.sortBy(scope.objreferences, '$$index', function (ref) {
                        if (!scope.stage == false || _.isUndefined(scope.stage)) {
                            $scope.addClass[ref.$$index] = "success";
                        }
                    });
                    toggler.show(scope, divid, url);

                };
            }
        };
    });

    ginasForms.directive('infoButton', function ($compile, toggler) {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                type: '@',
                path: '@'
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

    ginasForms.directive('accessForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/access-form.html",
            link: function (scope, element, attrs) {
                scope.validate = function () {
                    if (_.has(scope.referenceobj, 'access')) {
                        var temp = _.get(scope.referenceobj, 'access');
                        temp.push(scope.access);
                        _.set(scope.referenceobj, 'access', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(scope.access));
                        _.set(scope.referenceobj, 'access', x);
                    }
                    scope.access = {};
                    scope.accessForm.$setPristine();
                };

                scope.deleteObj = function (obj, parent) {
                    parent.splice(_.indexOf(parent, obj), 1);
                };
            }
        };
    });

    ginasForms.directive('agentModificationForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/agent-modification-form.html"
        };
    });

    ginasForms.directive('amountForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                amount: '=',
                referenceobj: '=',
                parent: '=',
                field: '=',
                filter:'='
            },
            templateUrl: baseurl + "assets/templates/forms/amount-form.html",
            link: function(scope){
                if(scope.filter){
                }

    }
        };
    });

    ginasForms.directive('codeForm', function ($compile, $templateRequest, toggler, validatorFactory) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: baseurl + "assets/templates/forms/code-form.html",
            scope: {
                parent: '='
            },
            link: function (scope, element, attrs) {
                scope.errors = [];
                scope.parse = function(code) {
                    if (!_.isUndefined(code) || !_.isNull(code)) {
                        if (_.isEmpty(code)) {
                            scope.errors.push({text: 'no code system yet', class: 'warning'});
                        }

                        var codeSystem;
                        var codeValue;
                        //this is for adding a new code
                        if (scope.code &&!_.isEmpty(scope.code.codeSystem)) {
                            codeSystem = scope.code.codeSystem.value;
                            codeValue = code;
                        } else{
                            //this is for editing one
                            codeSystem = code.codeSystem.value || code.codeSystem;
                            codeValue = code.code;
                        }
                        scope.errors = [];
                        var valid = validatorFactory.validate(codeSystem, codeValue);
                        if (valid == false) {
                            scope.errors.push({text: 'invalid', class: 'danger'});
                        } else {
                            scope.errors.push({text: 'valid', class: 'success'});
                        }
                    } else {
                        if (scope.errors.length < 1) {
                            scope.errors.push({text: 'no code system yet', class: 'warning'});
                        }
                    }
                    return scope.errors;
                }
            }
        };
    });

    ginasForms.directive('conceptUpgradeForm', function ($window, localStorageService) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: baseurl + "assets/templates/forms/concept-upgrade-form.html",
            scope: {
                parent: '='
            },
            link: function (scope, element, attrs) {

                scope.iscollapsed = false;
                if(scope.parent.uuid){
                    scope.editid = scope.parent.uuid.split('-')[0];
                }

                scope.changeClass = function (newClass) {
                    var upgradeSub= scope.parent.$$setClass(newClass);
                    _.set(upgradeSub, 'update', true);
                    localStorageService.set('tempsubstance', upgradeSub);
                   $window.location.href = $window.location.origin + baseurl + "wizard?kind=" +newClass;
                };
            }
        };
    });

    ginasForms.directive('commentForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '=',
                label: '=',
                field: '=',
                name: '='
            },
            templateUrl: baseurl + "assets/templates/forms/comment-form.html"
        };
    });

    ginasForms.directive('cvForm', function ($compile, $uibModal, CVFields) {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {},
            templateUrl: baseurl + "assets/templates/admin/cv-form.html",
            link: function (scope, element, attrs) {
                var formHolder;
                // scope.stage= true;
                CVFields.count().then(function (response) {
                    scope.count = response.data.total;
                });

                scope.edit = function () {
                    formHolder = '<edit-cv-form></edit-cv-form>';
                    scope.toggleStage();
                };

                scope.create = function () {
                    formHolder = '<new-cv-form></new-cv-form>';
                    scope.toggleStage();
                };

                scope.import = function () {
                    formHolder = '<load-cv-form></load-cv-form>';
                    scope.toggleStage();
                };

                scope.download = function () {
                    scope.cv={};
                    CVFields.all(false).then(function (response) {
                        scope.cv = response.data.content;
                        formHolder = '<save-cv-form cv = cv></save-cv-form>';
                        scope.toggleStage();
                    });
                };


                scope.close = function () {
                    modalInstance.close();
                };

                scope.open = function(url){
                    modalInstance = $uibModal.open({
                        templateUrl: url,
                        scope: scope
                    });
                };

                scope.toggleStage = function () {
                    var result = document.getElementsByClassName('cvForm');
                    var elementResult = angular.element(result);
                    elementResult.empty();
                    childScope = scope.$new();
                    var compiledDirective = $compile(formHolder);
                    var directiveElement = compiledDirective(childScope);
                    elementResult.append(directiveElement);
                };
            }
        };
    });

    ginasForms.directive('disulfideLinkForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                referenceobj: '=',
                formype: '@',
                residueregex: '@'

            },
            templateUrl: baseurl + "assets/templates/forms/disulfide-link-form.html",
            link: function (scope, element, attrs) {

                scope.getAllCysteinesWithoutLinkage = function () {
                    var count = 0;
                    _.forEach(scope.parent.protein.subunits, function (subunit) {
                        if (!_.isUndefined(subunit.$$cysteineIndices)) {
                            count += subunit.$$cysteineIndices.length;
                        }
                    });
                    if (_.has(scope.parent, 'disulfideLinks')) {
                        count -= scope.parent.protein.disulfideLinks.length * 2;
                    }
                    return count;
                };

                scope.validate = function () {
                    if (!scope.parent.protein.disulfideLinks) {
                        scope.parent.protein.disulfideLinks = [];

                    }
                    scope.parent.protein.disulfideLinks.push(scope.disulfideLink);
                    scope.disulfideLink = {};
                    scope.disulfideLinksForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.protein.disulfideLinks.splice(scope.parent.protein.disulfideLinks.indexOf(obj), 1);
                };
            }
        };
    });

    ginasForms.directive('diverseDetailsForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/diverse-details-form.html"
        };
    });

    ginasForms.directive('diversePlantForm', function (CVFields) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/diverse-plant-form.html",
            link: function (scope) {
            }
        };
    });

    ginasForms.directive('diverseOrganismForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: function (scope) {
               return baseurl + "assets/templates/forms/diverse-organism-form.html";
            }
        };
/*            templateUrl: baseurl + "assets/templates/forms/diverse-organism-form.html"
        };*/
    });

    ginasForms.directive('diverseSourceForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/diverse-source-form.html"
        };
    });

    ginasForms.directive('diverseTypeForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/diverse-type-form.html",
            link: function (scope, element, attrs) {
                scope.parent.$$diverseType = "whole";

                if(scope.parent.structurallyDiverse.displayParts ==='WHOLE'){
                    _.set(scope.parent, '$$diverseType', 'whole');
                }else{
                    _.set(scope.parent, '$$diverseType', 'part');
                }

                scope.checkType = function () {
                    if (scope.parent.$$diverseType === 'whole') {
                        scope.parent.$$diverseType = 'whole';
                        _.set(scope.parent.structurallyDiverse, 'part', ['WHOLE']);
                    } else {
                        _.set(scope.parent.structurallyDiverse, 'part', []);
                    }
                };
            }
        };
    });

    ginasForms.directive('cvTermsForm', function (CVFields) {
        return {
            restrict: 'E',
            replace: true,
            /*scope: {
                terms: '=',
                domain: '='
            },*/
            templateUrl: baseurl + "assets/templates/admin/cv-terms.html",
            link: function(scope){
            }
        };
    });


    ginasForms.directive('editCvForm', function ($templateRequest, CVFields, toggler) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/admin/edit-cv-form.html",
            link: function (scope) {
                CVFields.all().then(function (response){
                    scope.domains = response.data.content;
                });

                scope.stage = true;

                scope.flattenFields = function(fields){
                    _.forEach(fields, function(value, key) {
                        if(!value.value && value.display){
                            fields[key] = value.display;
                        }else if(!value.value && !value.display) {
                            fields[key] = value;
                        }else{
                            fields[key] = value.value;
                        }
                    });
                    return fields;
                };

                scope.getValues = function () {
                    CVFields.getCV(scope.vocab.display).then(function (data) {
                        scope.domain = data.data.content[0];
                    });
                    scope.create = true;
                };

                scope.deleteCV = function(obj){
                    var r = confirm("Are you sure you want to delete this CV?");
                    if (r == true) {
                        var terms = scope.domain.terms.splice(scope.domain.terms.indexOf(obj), 1);
                        if(scope.domain.fields){
                            scope.domain.fields = scope.flattenFields(scope.domain.fields);
                        }
                        CVFields.updateCV(scope.domain);
                    }
                };

                scope.addCV = function(term){
                        scope.domain.terms.push(term);
                    if(scope.domain.fields){
                        scope.domain.fields = scope.flattenFields(scope.domain.fields);
                    }
                        CVFields.updateCV(scope.domain).then(function(response){
                        scope.domain.terms = response.data.terms;
                            scope.term={};
                        });
                };



                scope.addDomain = function(domain){
                    if(!domain.terms) {
                        _.set(domain, 'terms', []);
                    }
                   domain.fields = scope.flattenFields(domain.fields);
                        CVFields.addDomain(domain).then(function(response){
                           // scope.domains.push(response.data);
                        });
                    scope.domain={};
                };

                scope.updateCV = function(obj){
                        if(obj){
                            obj.fields = scope.flattenFields(obj.fields);
                            CVFields.updateCV(obj).then(function(response) {
                                _.forEach(response.data.fields, function (value, key) {
                                    obj.fields[key] = {'value': value, 'display': value};
                                });
                            });
                        }else {
                            if(scope.domain.fields){
                                scope.domain.fields = scope.flattenFields(scope.domain.fields);
                            }
                            CVFields.updateCV(scope.domain).then(function(response) {
                                _.forEach(response.data.fields, function (value, key) {
                                    scope.domain.fields[key] = {'value': value, 'display': value};
                                });
                                scope.domain.terms = response.data.terms;
                            });
                        }
                };

                scope.showTerms= function(obj, divid){
                    if(!obj.terms) {
                        _.set(obj, 'terms', []);
                    }
                    scope.domain = obj;

                    if(!divid){
                        scope.type = obj.id;
                    }
                   // var formHolder = '<cv-terms-form domain = domain terms = {{terms}} ></cv-terms-form>';
                    var url = baseurl + "assets/templates/admin/cv-terms.html";
                    toggler.show(scope, divid, url);


                }

            }
        };
    });

    ginasForms.directive('formSelector', function ($compile, $templateRequest, toggler) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                type:'@',
                parent: '=',
                filter:'=',
                field: '@',
                label: '@',
                divid: '@',
                name: '@referenceobj'
            },
            link: function (scope, element, attrs) {
                var formHolder;
                var template;

                if (_.isUndefined(scope.referenceobj)) {
                    var x = {};
                    _.set(scope, 'referenceobj', x);
                }

                if(scope.filter){
                }

                scope.toggle = function () {
                    toggler.toggle(scope, scope.divid, formHolder, scope.referenceobj);
                };
                scope.stage = true;

                switch (scope.type) {
                    case "amount":
                        if(!scope.field){
                            scope.field = 'amount';
                        }
                            $templateRequest(baseurl + "assets/templates/selectors/amount-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);

                            });
                        formHolder = '<amount-form referenceobj = referenceobj parent = parent field=field amount=referenceobj.amount filter = filter></amount-form>';
                        break;
                    case "site":
                        scope.formtype = attrs.formtype;
                        scope.residueregex = attrs.residueregex;
                        scope.mode = attrs.mode;
                        $templateRequest(baseurl + "assets/templates/selectors/site-selector.html").then(function (html) {
                            template = angular.element(html);
                            element.append(template);
                            $compile(template)(scope);

                        });
                        formHolder = '<site-string-form referenceobj = referenceobj field=field parent = parent residueregex=residueregex formtype = formtype></site-string-form>';
                        break;
                    case "reference":
                            $templateRequest(baseurl + "assets/templates/selectors/reference-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        formHolder = '<reference-form referenceobj = referenceobj parent = parent></reference-form>';
                        break;
                    case "parameter":
                            $templateRequest(baseurl + "assets/templates/selectors/parameter-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        formHolder = '<parameter-form referenceobj = referenceobj field="field" parent = parent></parameter-form>';
                        break;
                    case "physicalParameter":
                        if (attrs.mode == "edit") {
                            template = angular.element('<a ng-click ="toggleStage()"><parameters parameters ="referenceobj.parameters"></parameters></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/selectors/parameter-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<physical-parameter-form referenceobj = referenceobj field = field parent = parent></physical-parameter-form>';
                        break;
                    case "nameOrgs":
                            $templateRequest(baseurl + "assets/templates/selectors/name-org-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                     //   }
                        formHolder = '<name-org-form referenceobj = referenceobj field = field parent = parent></name-org-form>';
                        break;
                    case "access":
                            $templateRequest(baseurl + "assets/templates/selectors/access-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        formHolder = '<access-form referenceobj = referenceobj parent = parent></access-form>';
                        break;
                    case "textbox":
/*                        if (attrs.mode == "edit") {
                            template = angular.element('<div><label for="comments" class="text-capitalize">{{label || field}}</label><a ng-click ="toggle()"><comment value = "referenceobj[field]" id="comment-directive"></comment></a></div>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {*/
                            $templateRequest(baseurl + "assets/templates/selectors/comment-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                    //    }
                        formHolder = '<comment-form referenceobj = referenceobj parent = parent label = label field = field name = name ></comment-form>';
                        break;
                }


                /*scope.toggleStage = function () {
                    if (_.isUndefined(scope.referenceobj)) {
                        var x = {};
                        _.set(scope, 'referenceobj', x);
                    }
                    var result = document.getElementsByClassName(attrs.divid);
                    var elementResult = angular.element(result);
                    if (scope.stage === true) {
                        scope.stage = false;
                        childScope = scope.$new();
                        var compiledDirective = $compile(formHolder);
                        var directiveElement = compiledDirective(childScope);
                        elementResult.append(directiveElement);
                    } else {
                        childScope.$destroy();
                        elementResult.empty();
                        scope.stage = true;

                    }
                };*/
            }
        };
    });

    ginasForms.directive('glycosylationForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                referenceobj: '='
            },
            templateUrl: baseurl + "assets/templates/forms/glycosylation-form.html",
            link: function (scope, element, attrs) {
                _.forEach(scope.parent.protein.glycosylation, function(value){
                    if(_.isArray(value) && value.length>0){
                        scope.iscollapsed=false;
                        }
                });
            }
        };
    });

    ginasForms.directive('loadCvForm', function (FileReader, CVFields) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: baseurl + "assets/templates/admin/load-cv-form.html",
            link: function (scope, element, attrs) {

                scope.cv={
                };

                scope.getPos = function(item,arr){
                    if (scope.cv.domains.indexOf(item) == -1
                        && scope.cv.value[arr.indexOf(item)]==true){
                        scope.cv.domains.push(item)
                    }
                    if (scope.cv.domains.indexOf(item) != -1
                        &&  ! scope.cv.value[arr.indexOf(item)] ){
                        scope.cv.domains.splice(scope.cv.domains.indexOf(item),1)
                    }
                    return arr.indexOf(item);
                };
                
                scope.loadCVFile = function (file) {
                    var tempValues = [];
                    scope.values=[];
                        FileReader.readAsText(file, scope).then(function(response){
                            scope.data = angular.fromJson(response);
                            for(var i=0; i<10; i++){
                                var keys = _.keys(scope.data.substanceNames[i]);
                              tempValues = _.union(keys, tempValues);
                            }
                            _.forEach(tempValues, function(v){
                                scope.values.push({display: v });
                            });
                            //  scope.values = tempValues;
                        });
                };

                scope.makeCV= function(){
                    var controlledVocab = {
                        domain: scope.cv.domainName,
                        terms:[]
                    };
                    var tempTerms=[];
                    _.forEach(scope.data.substanceNames, function(term){
                        var t = {
                            value: term[scope.cv.domain.display],
                            display: term[scope.cv.domain.display],
                            filters: []
                        };
                        if(scope.cv.dependencyField){
                            var filter = scope.cv.dependencyField.display+'='+term[scope.cv.dependencyField.display];
                            t.filters.push(filter);
                        }
                        tempTerms.push(t);
                    });

                    controlledVocab.terms = _.uniqWith(tempTerms, _.isEqual);
                    CVFields.addTerms(controlledVocab);
                };
            }
        };
    });

    ginasForms.directive('saveCvForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                cv: '='
            },
            templateUrl: baseurl + "assets/templates/admin/save-cv-form.html",
            link: function (scope, element, attrs) {
                    

            }
        };
    });

    ginasForms.directive('mixtureComponentSelectForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/mixture-component-select-form.html"
        };
    });

    ginasForms.directive('mixtureDetailsForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/mixture-details-form.html"
        };
    });

    ginasForms.directive('moietyForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/moiety-form.html"
        }
    });

    ginasForms.directive('structureForm', function ($http, $templateRequest, $compile, molChanger) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                type: '@'
            },
            link: function(scope, element){
                if(scope.parent.substanceClass ==='polymer'){
                    if(_.has(scope.parent.polymer.idealizedStructure)){
                        _.set(scope.parent, 'structure',scope.parent.polymer.idealizedStructure );
                    }else{
                        _.set(scope.parent, 'structure',scope.parent.polymer.displayStructure );
                    }
                }else{
                    scope.structure = scope.parent.structure;
                }
                
                scope.checkDuplicateChemicalSubstance = function () {
                    var sub = scope.parent.$$flattenSubstance(angular.copy(scope.parent));
                    scope.structureErrorsArray = [];
                    $http.post(baseurl + 'register/duplicateCheck', sub).success(function (response) {
                        var arr = [];

                        for (var i in response) {
                            if (response[i].messageType != "INFO")
                                arr.push(response[i]);
                            if (response[i].messageType == "WARNING")
                                response[i].class = "alert-warning";
                            if (response[i].messageType == "ERROR")
                                response[i].class = "alert-danger";
                            if (response[i].messageType == "INFO")
                                response[i].class = "alert-info";
                            if (response[i].messageType == "SUCCESS")
                                response[i].class = "alert-success";


                        }
                        scope.structureErrorsArray = arr;
                    });
                };
                $templateRequest(baseurl + "assets/templates/forms/structure-form.html").then(function (html) {
                    template = angular.element(html);
                    element.append(template);
                    $compile(template)(scope);
                });

            }
        };
    });

    ginasForms.directive('nameForm', function (substanceFactory, $q, $timeout, resolver, toggler, spinnerService) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/name-form.html",
            link: function(scope){
                scope.stage=true;
                scope.duplicateCheck = function(name) {
                    var result = angular.element(document.getElementsByClassName('nameForm'));
                    result.empty();
                        var resolve= resolver.resolve(name).then(function (response) {
                            if (response.data.length > 0) {
                               return response.data;
                            }
                            return [];
                        });
                        var duplicate = substanceFactory.getSubstances(name).then(function (response) {
                            var duplicate =[];
                            scope.nameForm.name.$error.duplicate = (response.data.count > 0);
                            if (response.data.count > 0) {
                                _.forEach(response.data.content, function (sub) {
                                    _.set(sub, 'refType', 'duplicate');
                                    duplicate.push(sub);
                                });
                            }
                            return duplicate;
                        });
                    if (!_.isUndefined(name) && name!=="") {
                        spinnerService.show('nameSpinner');
                        var template;
                        $q.all([resolve, duplicate]).then(function(results) {
                            scope.data = [];
                            var temp = [];
                            _.forEach(results, function(result){
                               if(!_.isUndefined(result)){
                                  temp.push(result);
                               }
                                scope.data = _.flattenDeep(temp);
                            });
                        }).finally(function(){
                                if(_.isEmpty(scope.data)){
                                    scope.data.push("empty");
                                }
                                template = angular.element('<substance-viewer data = data parent = parent obj = name></substance-viewer>');
                            toggler.refresh(scope, 'nameForm', template);
                        });
                    }else {
                            spinnerService.hideAll();
                    }
                };

            }
        };
    });

    ginasForms.directive('nameOrgForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                referenceobj: '='
            },
            templateUrl: baseurl + "assets/templates/forms/name-org-form.html",
            link:function (scope){
                scope.validate = function(){
                    if (_.has(scope.referenceobj, 'nameOrgs')) {
                        var temp = _.get(scope.referenceobj, 'nameOrgs');
                        temp.push(scope.org);
                        _.set(scope.referenceobj, 'nameOrgs',temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(scope.org));
                        _.set(scope.referenceobj, 'nameOrgs',  x);
                    }
                    scope.org = {};
                    scope.orgForm.$setPristine();
                };

            scope.deleteObj = function(obj,path){
                    var arr = _.get(scope.referenceobj, path);
                    arr.splice(arr.indexOf(obj), 1);
            };
            }
        };
    });

    ginasForms.directive('newCvForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/admin/new-cv-form.html",
            link: function (scope) {

            }
        };
    });

    ginasForms.directive('noteForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/note-form.html"
        };
    });

    ginasForms.directive('nucleicAcidDetailsForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/nucleic-acid-details-form.html"
        };
    });

    ginasForms.service('siteList', function () {

        //string to array hepler
        function siteDisplayToSite(site) {
            var subres = site.split("_");

            if (site.match(/^[0-9][0-9]*_[0-9][0-9]*$/g) === null) {
                throw "\"" + site + "\" is not a valid shorthand for a site. Must be of form \"{subunit}_{residue}\"";
            }

            return {
                subunitIndex: subres[0] - 0,
                residueIndex: subres[1] - 0
            };
        }

        //string to array
        this.siteList = function (slist) {
            var toks = slist.split(";");
            var sites = [];
            for (var i in toks) {
                var l = toks[i];
                if (l === "")continue;
                var rng = l.split("-");
                if (rng.length > 1) {
                    var site1 = siteDisplayToSite(rng[0]);
                    var site2 = siteDisplayToSite(rng[1]);
                    if (site1.subunitIndex != site2.subunitIndex) {
                        throw "\"" + rng + "\" is not a valid shorthand for a site range. Must be between the same subunits.";
                    }
                    if (site2.residueIndex <= site1.residueIndex) {
                        throw "\"" + rng + "\" is not a valid shorthand for a site range. Second residue index must be greater than first.";
                    }
                    sites.push(site1);
                    for (var j = site1.residueIndex + 1; j < site2.residueIndex; j++) {
                        sites.push({
                            subunitIndex: site1.subunitIndex,
                            residueIndex: j
                        });
                    }
                    sites.push(site2);
                } else {
                    sites.push(siteDisplayToSite(rng[0]));
                }
            }
            return sites;

        };

        //array to String
        this.siteString = function (sitest) {
            var sites = [];
            angular.extend(sites, sitest);
            sites.sort(function (site1, site2) {
                var d = site1.subunitIndex - site2.subunitIndex;
                if (d === 0) {
                    d = site1.residueIndex - site2.residueIndex;
                }
                return d;

            });
            var csub = 0;
            var cres = 0;
            var rres = 0;
            var finish = false;
            var disp = "";
            for (var i = 0; i < sites.length; i++) {

                var site = sites[i];
                if (site.subunitIndex == csub && site.residueIndex == cres)
                    continue;
                finish = false;
                if (site.subunitIndex == csub) {
                    if (site.residueIndex == cres + 1) {
                        if (rres === 0) {
                            rres = cres;
                        }
                    } else {
                        finish = true;
                    }
                } else {
                    finish = true;
                }
                if (finish && csub !== 0) {
                    if (rres !== 0) {
                        disp += csub + "_" + rres + "-" + csub + "_" + cres + ";";
                    } else {
                        disp += csub + "_" + cres + ";";
                    }
                    rres = 0;
                }
                csub = site.subunitIndex;
                cres = site.residueIndex;
            }
            if (sites.length > 0) {
                if (rres !== 0) {
                    disp += csub + "_" + rres + "-" + csub + "_" + cres;
                } else {
                    disp += csub + "_" + cres;
                }
            }
            return disp;
        };


        //make string from all indexes
        this.allSites = function (parent, type, linkage) {
            var sites = "";
            var subs = parent[type].subunits;
            for (var i in subs) {
                var subunit = subs[i];
                if (sites !== "") {
                    sites += ";";
                }
                if (linkage) {
                    sites += subunit.subunitIndex + "_1-" + subunit.subunitIndex + "_" + (subunit.sequence.length - 1);
                } else {
                    sites += subunit.subunitIndex + "_1-" + subunit.subunitIndex + "_" + subunit.sequence.length;
                }
            }
            return sites;
        };
    });

    ginasForms.service('siteAdder', function (siteList) {

        this.getAll = function (type, display) {
            var temp = [];
            _.forEach(display, function (arr) {
                _.forEach(arr, function (subunit) {
                    temp = _.filter(subunit, function (su) {
                        return su[type];
                    });
                });
            });
            return temp;
        };

        this.getCount= function(display){
            var temp = [];
            var count=0;
            _.forEach(display, function (arr) {
                _.forEach(arr, function (subunit) {
                    count += subunit.length;
                });
            });
            return count;
        };


        this.getAllSitesWithout = function (type, display) {
            var temp = [];
            _.forEach(display, function (arr) {
                _.forEach(arr, function (subunit) {
                //    console.log(subunit);
                    temp = _.reject(subunit, function (su) {
                    //    console.log(su[type]);
                        return su[type];
                    });
                });
            });
            if (type == 'linkage') {
                temp = _.dropRight(temp);
            }
           // console.log(temp);
            return temp;
        };

        this.applyAll = function (type, parent, obj) {
            var plural = type + "s";
            if (parent.nucleicAcid[plural].length == 0) {
                if (type == 'linkage') {
                    obj.$$displayString = siteList.allSites(parent, 'nucleicAcid', type);
                } else {
                    obj.$$displayString = siteList.allSites(parent, 'nucleicAcid');
                }
                obj.sites = siteList.siteList(obj.$$displayString);

                //this applies the sugar property to the display object
                _.forEach(obj.sites, function (site) {
                    _.set(parent.$$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1], type, true);
                });
            } else {
                obj.$$displayString = siteList.siteString(this.getAllSitesWithout(type, parent.$$subunitDisplay));
                obj.sites = siteList.siteList(obj.$$displayString);
            }
        };

        this.clearSites = function (type, parent, obj) {
            _.forEach(obj, function (site) {
                parent.$$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1] = _.omit(parent.$$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1], type);
            });
        };
    });

    ginasForms.directive('nucleicAcidSugarForm', function (siteList, siteAdder) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                referenceobj: '='
            },
            templateUrl: baseurl + "assets/templates/forms/nucleic-acid-sugar-form.html",
            link: function (scope, attrs, element) {
                scope.sugar = {};

                if (!scope.parent.nucleicAcid.sugars) {
                    scope.parent.nucleicAcid.sugars = [];
                }

                scope.getAllSites = function () {
                    return siteAdder.getCount(scope.parent.$$subunitDisplay);
                };

                scope.applyAll = function () {
                    siteAdder.applyAll('sugar', scope.parent, scope.sugar);
                //    scope.noSugars = siteAdder.getAllSitesWithout('sugar', scope.parent.$$subunitDisplay).length;
                };

                scope.validate = function () {
                    _.forEach(scope.sugar.sites.sites, function (site) {
                        _.set(scope.parent.$$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1], 'sugar', true);
                    });
                    scope.parent.nucleicAcid.sugars.push(scope.sugar);
                   // scope.noSugars = siteAdder.getAllSitesWithout('sugar', scope.parent.$$subunitDisplay).length;
                    scope.sugar = {};
                    scope.sugarForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.nucleicAcid.sugars.splice(scope.parent.nucleicAcid.sugars.indexOf(obj), 1);
                    siteAdder.clearSites('sugar', scope.parent, obj.sites);
                //    scope.noSugars = siteAdder.getAllSitesWithout('sugar', scope.parent.$$subunitDisplay).length;
                };

                scope.noSugars = function () {
                    var count = scope.getAllSites();
                    _.forEach(scope.parent.nucleicAcid.sugars,function(sugar){count -= sugar.sites.length});
                 return  count;
                }
            }
        };
    });

    ginasForms.directive('nucleicAcidLinkageForm', function (siteList, siteAdder) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/nucleic-acid-linkage-form.html",
            link: function (scope, attrs, element) {
                scope.linkage = {};
                scope.noLinkages = 0;
                if (!scope.parent.nucleicAcid.linkages) {
                    scope.parent.nucleicAcid.linkages = [];
                }

                scope.getAllSites = function () {
                   return siteAdder.getCount(scope.parent.$$subunitDisplay);
                };

                scope.applyAll = function () {
                    siteAdder.applyAll('linkage', scope.parent, scope.linkage, 'linkage');
                    scope.noLinkages = siteAdder.getAllSitesWithout('linkage', scope.parent.$$subunitDisplay).length;
                };

                scope.validate = function () {
                    _.forEach(scope.linkage.sites.sites, function (site) {
                        _.set(scope.parent.$$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1], 'linkage', true);
                    });
                    scope.parent.nucleicAcid.linkages.push(scope.linkage);
                    scope.noLinkages = siteAdder.getAllSitesWithout('linkage', scope.parent.$$subunitDisplay).length;
                    scope.linkage = {};
                    scope.linkageForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.nucleicAcid.linkages.splice(scope.parent.nucleicAcid.linkages.indexOf(obj), 1);
                    siteAdder.clearSites('linkage', scope.parent, obj.sites);
                    scope.noLinkages = siteAdder.getAllSitesWithout('linkage', scope.parent.$$subunitDisplay).length;
                };

                scope.noLinkages = function () {
                    var count = scope.getAllSites('linkage')-1;
                    _.forEach(scope.parent.nucleicAcid.linkages,function(link){count -= link.sites.length});
                    return  count;
                }
            }
        };
    });

    ginasForms.directive('otherLinksForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                referenceobj: '=',
                displayType: '='
            },
            templateUrl: baseurl + "assets/templates/forms/other-links-form.html",
            link: function (scope, element, attrs) {

                scope.validate = function () {
                    if (!scope.parent.protein.otherLinks) {
                        scope.parent.protein.otherLinks = [];
                    }
                    scope.parent.protein.otherLinks.push(scope.otherLink);
                    scope.otherLink = {};
                    scope.otherLinksForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.protein.otherLinks.splice(scope.parent.protein.otherLinks.indexOf(obj), 1);
                };
            }
        };
    });

    ginasForms.directive('parameterForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/parameter-form.html",
            link: function (scope, element, attrs) {

                scope.validate = function () {
                    if (_.has(scope.referenceobj, 'parameters')) {
                        var temp = _.get(scope.referenceobj, 'parameters');
                        temp.push(scope.parameter);
                        _.set(scope.referenceobj, 'parameters', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(scope.parameter));
                        _.set(scope.referenceobj, 'parameters', x);
                    }
                    scope.parameter = {};
                    scope.parameterForm.$setPristine();
                };

                scope.deleteObj = function (obj, parent) {
                    parent.splice(_.indexOf(parent, obj), 1);
                };
            }
        };
    });

    ginasForms.directive('parentForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/parent-form.html"
        };
    });

    ginasForms.directive('partForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/part-form.html"
        };
    });

    ginasForms.directive('physicalModificationForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/physical-modification-form.html"
        };
    });

    ginasForms.directive('physicalParameterForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/physical-parameter-form.html",
            link: function (scope, element, attrs) {

                scope.validate = function () {
                    if (_.has(scope.referenceobj, 'parameters')) {
                        var temp = _.get(scope.referenceobj, 'parameters');
                        temp.push(scope.physicalParameter);
                        _.set(scope.referenceobj, 'parameters', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(scope.physicalParameter));
                        _.set(scope.referenceobj, 'parameters', x);
                    }
                    scope.physicalParameter = {};
                    scope.physicalParameterForm.$setPristine();
                };

                scope.deleteObj = function (obj, parent) {
                    parent.splice(_.indexOf(parent, obj), 1);
                };
            }
        };
    });

    ginasForms.directive('polymerClassificationForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/polymer-classification-form.html"
        };
    });

    ginasForms.directive('polymerMonomerForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/polymer-monomer-form.html"
        };
    });

    ginasForms.directive('polymerSruForm', function (polymerUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/polymer-sru-form.html",
            link: function (scope) {
                scope.validateConnectivity=function(obj){
                	var map=polymerUtils.sruDisplayToConnectivity(obj._displayConnectivity);
                }
            }
        };
    });

    ginasForms.directive('propertyForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/property-form.html"
        };
    });

    ginasForms.directive('proteinDetailsForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            link: function(scope){
               /* var fields =['sequenceType','proteinType','proteinSubType','sequenceOrigin'];
                _.forEach(fields, function(field){
                   if(!_.isNull(scope.parent.protein[field])){
                       scope.iscollapsed=false;
                   }
                });*/
            },
            templateUrl: baseurl + "assets/templates/forms/protein-details-form.html"
        };
    });

    ginasForms.directive('referenceApply', function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                apply: '=?ngModel',
                obj: '=?',
                referenceobj: '=',
                parent: '='
            },
            link: function (scope, element, attrs) {
                var uuid;
                var index;
                var template;
                scope.apply = true;
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

                switch (attrs.type) {
                    case "view":
                        template = angular.element('<div class = "text-center"><label for="apply" class="text-capitalize">Apply</label><br/><input type="checkbox" ng-model= apply placeholder="Apply" title="Apply" id="apply" checked/></div>');
                        element.append(template);
                        $compile(template)(scope);
                        break;
                    case "edit":
                        template = angular.element('<div class = "text-center"><input type="checkbox" ng-model="obj.apply" ng-click="updateReference();" placeholder="{{field}}" title="{{field}}" id="{{field}}s"/></div>');
                        element.append(template);
                        $compile(template)(scope);
                        uuid = scope.obj.uuid;
                        index = _.indexOf(scope.referenceobj.references, uuid);
                        scope.obj.apply = scope.isReferenced();
                        scope.parent.references = _.orderBy(scope.parent.references, ['apply'], ['desc']);
                        break;
                }

                scope.updateReference = function () {
                    index = _.indexOf(scope.referenceobj.references, uuid);
                    if (index >= 0) {
                        scope.referenceobj.references.splice(index, 1);
                        scope.obj.apply = false;
                    } else {
                        scope.referenceobj.references.push(uuid);
                        scope.obj.apply = true;
                    }
                };

            }
        };
    });

    ginasForms.directive('referenceForm', function ($http, UUID) {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/reference-form.html",
            link: function (scope, element, attrs) {
                scope.submitFile = function (obj) {
                    //create form data object
                    var fd = new FormData();
                    if(obj){
                        scope.$$uploadFile = obj.$$uploadFile;
                    }
                    //  fd.append('file', scope.$$uploadFile);
                    fd.append('file-name', scope.$$uploadFile);
                    fd.append('file-type', scope.$$uploadFile.type);
                    //send the file / data to your server
                    $http.post(baseurl + 'upload', fd, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    }).success(function (data) {
                        if(obj){
                            _.set(obj, 'uploadedFile', data.url);
                            _.set(scope, 'uploadDoc', false);
                            delete obj.$$uploadFile;
                        }else {
                            _.set(scope.reference, 'uploadedFile', data.url);
                        }
                    }).error(function (err) {
                    });
                    _.set(scope, 'uploadDoc', false);
                    delete scope.$$uploadFile;
                };

                    scope.validate = function () {
                    if (!_.isUndefined(scope.reference.citation)) {
                        _.set(scope.reference, "uuid", UUID.newID());
                        if (scope.reference.apply) {
                            scope.saveReference(scope.reference.uuid, scope.referenceobj);
                            scope.saveReference(angular.copy(scope.reference), scope.parent);
                        } else {
                            scope.saveReference(scope.reference, scope.parent);
                        }
                        scope.reference = {};
                        scope.reference.apply = true;
                        scope.refForm.$setPristine();
                    }
                };
                //////////////////////////////////////////////////////////////////////////////////////////////////////////
                //why is the array fetched, then set?
                scope.saveReference = function (reference, parent) {
                    if (_.has(parent, 'references')) {
                        var temp = _.get(parent, 'references');
                        temp.push(reference);
                        _.set(parent, 'references', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(reference));
                        _.set(parent, 'references', x);
                    }
                };
            }
        };
    });
ginasForms.directive('referenceModalForm', function ($http, UUID) {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '=',
                edit: '=?'
            },
            templateUrl: baseurl + "assets/templates/modals/reference-modal-form.html",
            link: function (scope, element, attrs) {
                scope.reference={};

                if(scope.edit){;
                    scope.active = 2;
                }

                scope.submitFile = function (obj) {
                    //create form data object
                    var fd = new FormData();
                    if(obj){
                        scope.reference.$$uploadFile = obj.$$uploadFile;
                    }
                    //  fd.append('file', scope.$$uploadFile);
                    fd.append('file-name', scope.reference.$$uploadFile);
                    fd.append('file-type', scope.reference.$$uploadFile.type);
                    //send the file / data to your server
                    $http.post(baseurl + 'upload', fd, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    }).success(function (data) {
                        if(obj){
                            _.set(obj, 'uploadedFile', data.url);
                            _.set(scope, 'uploadDoc', false);
                           delete obj.$$uploadFile;
                        }else {
                            _.set(scope.reference, 'uploadedFile', data.url);
                        }
                    }).error(function (err) {
                    });
                    _.set(scope, 'uploadDoc', false);
                    delete scope.$$uploadFile;
                };

                scope.$on('save', function(e) {
                    scope.validate();
                });


                    scope.validate = function () {
                    if (!_.isUndefined(scope.reference.citation)) {
                        _.set(scope.reference, "uuid", UUID.newID());
                        if (scope.reference.apply) {
                            scope.saveReference(scope.reference.uuid, scope.referenceobj);
                            scope.saveReference(_.cloneDeep(scope.reference), scope.parent);
                        } else {
                            scope.saveReference(scope.reference, scope.parent);
                        }
                        scope.reference = {};
                        scope.reference.apply = true;
                      //  scope.refForm.$setPristine();
                    }
                };
                //////////////////////////////////////////////////////////////////////////////////////////////////////////
                //why is the array fetched, then set?
                scope.saveReference = function (reference, parent) {
                    if (_.has(parent, 'references')) {
                        var temp = _.get(parent, 'references');
                        temp.push(reference);
                        _.set(parent, 'references', temp);
                    } else {
                        var x = [];
                        x.push(_.cloneDeep(reference));
                        _.set(parent, 'references', x);
                    }
                };
            }
        };
    });

    ginasForms.directive('referenceFormOnly', function ($http) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/reference-form-only.html",
            link: function (scope, element, attrs) {

                scope.submitFile = function (obj) {
                    //create form data object
                    var fd = new FormData();
                    if(obj){
                        scope.$$uploadFile = obj.$$uploadFile;
                    }
                    fd.append('file-name', scope.$$uploadFile);
                    fd.append('file-type', scope.$$uploadFile.type);
                    //send the file / data to your server
                    $http.post(baseurl + 'upload', fd, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    }).success(function (data) {
                        if(obj){
                            _.set(obj, 'uploadedFile', data.url);
                            _.set(scope, 'uploadDoc', false);
                            delete obj.$$uploadFile;
                        }else {
                            _.set(scope.reference, 'uploadedFile', data.url);
                        }
                    }).error(function (err) {
                    });
                    _.set(scope, 'uploadDoc', false);
                    delete scope.$$uploadFile;
                };


                scope.validate = function () {
                    scope.saveReference(scope.reference, scope.parent);
                    scope.reference = {};
                    scope.reference.apply = true;
                    scope.refForm.$setPristine();
                };

                scope.saveReference = function (reference, parent) {
                    if (_.has(parent, 'references')) {
                        var temp = _.get(parent, 'references');
                        temp.push(reference);
                        _.set(parent, 'references', temp);
                    } else {
                        var x = [];
                        x.push(angular.copy(reference));
                        _.set(parent, 'references', x);
                    }
                };


                scope.deleteObjectReferences = function (obj, id) {
                    var index;
                    var refs = _.get((obj), 'references');
                    index = _.findIndex(refs, function (o) {
                        return o == id;
                    });
                    if (index > -1) {
                        refs.splice(index, 1);
                        obj.references = refs;
                    }
                };

                scope.deleteReference = function (ref) {
                    scope.parent.references.splice(scope.parent.references.indexOf(ref), 1);
                };

                scope.deleteObj = function (obj, ref) {
                    _.forEach(_.keysIn(obj), function (field) {
                        if (_.isObject(obj[field])) {
                            if (_.isArray(obj[field])) {
                                _.forEach((obj[field]), function (value, key) {
                                    if (_.isObject(value)) {
                                        if (_.indexOf(_.keysIn(value), 'references') > -1) {
                                            scope.deleteObjectReferences(value, ref.uuid);
                                        } else {
                                            scope.deleteObj(value, ref);
                                        }
                                    }
                                });
                            } else {
                                scope.deleteObj(obj[field], ref);
                            }
                        }
                    });
                };
            }
        };
    });

    ginasForms.directive('relationshipForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/relationship-form.html",
            link: function(scope){
              //  scope.filter = {};
            }
        };
    });

    ginasForms.directive('siteStringForm', function ($compile, $templateRequest, siteList) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                parent: '=',
                mode: '=',
                formtype: '=',
                residueregex: '=',
                field: '='
            },
            link: function (scope, element, attrs) {
                var template;
                scope.subunits = scope.parent[scope.parent.substanceClass].subunits;

                if (scope.formtype == "pair") {
                    $templateRequest(baseurl + "assets/templates/forms/site-dropdown-form.html").then(function (html) {
                        template = angular.element(html);
                        element.append(template);
                        $compile(template)(scope);
                    });
                } else {
                    $templateRequest(baseurl + "assets/templates/forms/site-string-form.html").then(function (html) {
                        template = angular.element(html);
                        element.append(template);
                        $compile(template)(scope);

                    });
                }

                ///////////////////////////////////////////////////////////////////////////////////////////////////////
                //this gets called when the siteform is open, and on hovering over subunits...
                scope.validResidues = function (su) {
                    if (!su)return [];
                    var list = [];
                    if (scope.residueregex) {
                        var ret = scope.parent[scope.parent.substanceClass].subunits[su - 1].$$cysteineIndices;
                        if (_.has(scope.parent.protein, 'disulfideLinks')) {
                            _.forEach(scope.parent.protein.disulfideLinks, function (siteList) {
                                _.forEach(siteList.sites, function (site) {
                                    var v;
                                    if (site.subunitIndex == (su)) {
                                        v = _.remove(ret, function (n) {
                                            return n == site.residueIndex
                                        });
                                        return v;
                                    }
                                });
                            });
                        }
                        return ret;
                    } else {
                        return _.range(1, scope.subunits[su - 1].sequence.length + 1);
                    }
                };

                scope.getSubunitRange = function () {
                    return _.range(1, scope.subunits.length + 1);
                };

                scope.makeSiteList = function () {
                        var temp= angular.copy(scope.referenceobj[scope.field].$$displayString);
                        _.set(scope.referenceobj, scope.field, siteList.siteList(scope.referenceobj[scope.field].$$displayString));
                        scope.referenceobj[scope.field].$$displayString = temp;
                };

                scope.redraw = function () {
                    scope.referenceobj.$$displayString = siteList.siteString(scope.referenceobj.sites);
                };

                scope.deleteObj = function (obj) {
                    scope.referenceobj.splice(scope.referenceobj.indexOf(obj), 1);
                };
            }
        };
    });

    ginasForms.directive('ssConstituentForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/ss-constituent-form.html"
        };
    });

    ginasForms.directive('structuralModificationForm', function (CVFields) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/structural-modifications-form.html",
            link: function(scope){
                scope.getCV = function(mod){
                    if(mod =="AMINO_ACID_SUBSTITUTION"){
                        return "AMINO_ACID_RESIDUE";
                    }else if(mod =="NUCLEOSIDE_SUBSTITUTION"){
                        return "NUCLEIC_ACID_BASE";
                    }else{
                        return null;
                    }
                };
            }
        };
    });

    ginasForms.directive('submitButtons', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                obj: '=submit',
                form: '=form',
                path: '@path',
                name:'@submit'
            },
            templateUrl: baseurl + "assets/templates/submit-buttons.html",
            link: function (scope, element, attrs) {
                scope.validate = function () {
                    if (!scope.$parent.validate) {
                        if (scope.$parent.$parent.validate(scope.obj, scope.form, scope.path)) {
                            scope.reset();
                        }
                    } else {
                        if (scope.$parent.validate(scope.obj, scope.form, scope.path)) {
                            scope.reset();
                        }
                    }
                    scope.$broadcast('show-errors-reset');
                };
                scope.reset = function () {
                    scope.obj = null;
                    if (!scope.$parent.reset) {
                        scope.$parent.$parent.reset(scope.form);

                    } else {
                        scope.$parent.reset(scope.form);
                    }
                };
            }
        };
    });

    ginasForms.directive('subunitForm', function ($compile, $templateRequest, $uibModal) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            link: function (scope, element, attrs) {
                scope.numbers = true;
                scope.parent.$$subunitDisplay = [];
                        $templateRequest(baseurl + "assets/templates/forms/subunit-form.html").then(function (html) {
                            scope.substanceClass = scope.parent.$$getClass();
                            var template = angular.element(html);
                            element.append(template);
                            $compile(template)(scope);
                        });

               scope.validate = function () {
                    if (scope.subunit.sequence.length > 10000) {
                        scope.open();
                    }
                    scope.subunit.subunitIndex = scope.parent[scope.parent.substanceClass].subunits.length + 1;
                    scope.parent[scope.parent.substanceClass].subunits.push(scope.subunit);
                    scope.subunit = {};
                    scope.subunitForm.$setPristine();
                };

                //******************************************************************this needs to reassign subunit indexes
                scope.deleteObj = function (obj) {
                    scope.parent[scope.parent.substanceClass].subunits.splice(scope.parent[scope.parent.substanceClass].subunits.indexOf(obj), 1);
                };

                scope.close = function () {
                    var obj = _.last(scope.parent[scope.parent.substanceClass].subunits);
                    scope.deleteObj(obj);
                    modalInstance.close();
                };

                scope.open = function () {
                    modalInstance = $uibModal.open({
                        templateUrl: baseurl + "assets/templates/modals/subunit-warning.html",
                        scope: scope
                    });
                };
            }
        };
    });

    ginasForms.directive('headerForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
               // name: '=',
                parent: '='
            },
            link: function (scope, element, attrs) {
                if (scope.parent._name) {
                    scope.formType ='Editing';
                    scope.name = scope.parent._name;
                } else {
                    scope.formType ='Registering new';
                    scope.name = scope.parent.substanceClass;
                }
            },
            templateUrl: baseurl + "assets/templates/forms/header-form.html"
        };
    });


    ginasForms.directive('isolateForm', [function () {
        return {
            restrict: 'A',
            require: '?form',
            link: function link(scope, element, iAttrs, formController) {

                if (!formController) {
                    return;
                }

                // Remove this form from parent controller
                formController.$$parentForm.$removeControl(formController);

                var _handler = formController.$setValidity;
                formController.$setValidity = function (validationErrorKey, isValid, cntrl) {
                    _handler(validationErrorKey, isValid, cntrl);
                    formController.$$parentForm.$setValidity(validationErrorKey, true, this);
                }
            }
        };
    }]);
})();