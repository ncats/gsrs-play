(function () {
    var ginasForms = angular.module('ginasForms', ['bootstrap.fileField']);

    ginasForms.service('toggler', function ($compile, $templateRequest) {

        var childScope;
        this.stageCheck = function () {
            return this.stage;
        };

        this.show = function (scope, element, url) {
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

        this.toggle = function (scope, element, newForm) {
            console.log("in the toggler)");
            console.log(scope);
            var result = document.getElementsByClassName(element);
            var elementResult = angular.element(result);
            if (scope.stage === true) {
                scope.stage = false;
                childScope = scope.$new();
                var compiledDirective = $compile(newForm);
                var directiveElement = compiledDirective(childScope);
                elementResult.append(directiveElement);
                console.log(childScope);
            } else {
                console.log("destroy");
                console.log(childScope);
                childScope.$destroy();
                elementResult.empty();
                scope.stage = true;
                console.log(childScope);
                console.log(scope);

            }
        };
    });

    ginasForms.directive('formHeader', function ($compile, $templateRequest, toggler) {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                type: '@',
                referenceobj: '=',
                parent: '=',
                path: '@',
                iscollapsed: '='
            },
            templateUrl: baseurl + "assets/templates/selectors/form-header.html",
            link: function (scope, element, attrs) {
                scope.length = 0;
                scope.heading = _.startCase(scope.type);
                if (_.isUndefined(scope.path)) {
                    scope.path = scope.type;
                }
                if (!_.isUndefined(scope.parent[scope.path])) {
                    scope.length = _.get(scope.parent, scope.path).length;
                }
                if (scope.length == 0) {
                    scope.iscollapsed = true;
                }
                /*                console.log(scope.path);
                 console.log(_.get(scope.parent, scope.path));*/
                /*                if(_.isUndefined(scope.parent[scope.path]) || scope.parent[scope.path].length == 0){
                 scope.iscollapsed=false;
                 }*/
                /*
                 scope.showInfo = function () {
                 var url = baseurl + "assets/templates/info/code-info.html";
                 toggler.show(scope, type, url);
                 };*/

                scope.toggle = function () {
                    scope.iscollapsed = !scope.iscollapsed;
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
                    toggler.show(scope, type, url);
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
                console.log(scope);
                scope.validate = function () {
                    console.log(scope.referenceobj);
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
            templateUrl: baseurl + "assets/templates/forms/agent-modification-form.html",
            link: function (scope, element, attrs) {
                if (!scope.parent.protein.modifications) {
                    scope.parent.protein.modifications = {};

                }
                if (!scope.parent.protein.modifications.agentModifications) {
                    scope.parent.protein.modifications.agentModifications = [];
                }
                scope.validate = function () {

                    scope.parent.protein.modifications.agentModifications.push(scope.agentMod);
                    scope.agentMod = {};
                    scope.agentModForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.protein.modifications.agentModifications.splice(scope.parent.protein.modifications.agentModifications.indexOf(obj), 1);
                };
            }
        };
    });

    ginasForms.directive('amountForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                amount: '=',
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/amount-form.html",
            link: function (scope) {
                console.log(scope);
            }
        };
    });

    ginasForms.directive('codeForm', function ($compile, $templateRequest, toggler) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: baseurl + "assets/templates/forms/code-form.html",
            scope: {
                parent: '='
            },
            link: function (scope, element, attrs) {
                scope.iscollapsed = false;
            }
        };
    });

    ginasForms.directive('conceptUpgradeForm', function ($http, $location, toggler) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: baseurl + "assets/templates/forms/concept-upgrade-form.html",
            scope: {
                parent: '='
            },
            link: function (scope, element, attrs) {
                scope.iscollapsed = false;
                console.log($location);
                scope.editid = scope.parent.uuid.split('-')[0];
                scope.changeClass = function (newClass) {
                    var newSub = scope.$parent.fromFormSubstance(scope.parent);
                    console.log(scope);
                    newSub.substanceClass = newClass;
                    _.set(newSub, 'update', true);
                    console.log(newSub);
                    if (_.has(newSub, 'update')) {
                        $http.put(baseurl + 'api/v1/substances(' + scope.parent.uuid + ')/_', newSub, {
                            headers: {
                                'Content-Type': 'application/json'
                            }
                        }).success(function(data){
                                alert('Load was performed.');
                                $location.path('app/substance/{{editid}}/edit');
                                $location.replace();
                            });
                    } else {
                        $http.post(baseurl + 'register/submit', newSub).success(function () {
                            console.log("success");
                            alert("submitted!");
                            $location.path('app/substance/{{editid}}/edit');
                            $location.replace();
                        });
                    }


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
                label: '@',
                field: '='
            },
            templateUrl: baseurl + "assets/templates/forms/comment-form.html",
            link: function (scope, element, attrs) {
                console.log(scope);
            }
        };
    });

    ginasForms.directive('cvForm', function ($compile, CVFields) {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {},
            templateUrl: baseurl + "assets/templates/admin/cv-form.html",
            link: function (scope, element, attrs) {
                console.log(scope);
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
                    CVFields.all().then(function (response) {
                        console.log(response);
                        scope.cv = response.data.content;
                        formHolder = '<save-cv-form cv = cv></save-cv-form>';
                        scope.toggleStage();
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
                console.log(scope);

                if (!scope.parent.protein.disulfideLinks) {
                    scope.parent.protein.disulfideLinks = [];

                }

                scope.getAllCysteinesWithoutLinkage = function () {
                    var count = 0;
                    _.forEach(scope.parent.protein.subunits, function (subunit) {
                        if (!_.isUndefined(subunit.cysteineIndices)) {
                            count += subunit.cysteineIndices.length;
                        }
                    });
                    if (scope.parent.protein.disulfideLinks.length > 0) {
                        count -= scope.parent.protein.disulfideLinks.length * 2;
                    }
                    return count;
                };

                scope.validate = function () {
                    console.log(scope);
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

    ginasForms.directive('diverseOrganismForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/diverse-organism-form.html"
        };
    });

    ginasForms.directive('diverseSourceForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/diverse-source-form.html",
            link: function (scope, element) {
                console.log(scope);
            }
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
                console.log(scope);
                scope.parent.diverseType = "";

                scope.checkType = function () {
                    if (scope.parent.diverseType === 'whole') {
                        console.log("whole)");
                        _.set(scope.parent.structurallyDiverse, 'part', ['WHOLE']);
                    } else {
                        _.set(scope.parent.structurallyDiverse, 'part', []);
                    }
                };
            }
        };
    });

    ginasForms.directive('editCvForm', function (CVFields) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/admin/edit-cv-form.html",
            link: function (scope) {
                console.log(scope);
                scope.getValues = function () {
                    console.log(scope);
                    CVFields.fetch(scope.vocab.value).then(function (data) {
                        console.log(data);
                        scope.values = data.data.content[0].terms;
                    });
                    scope.create = true;
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
                parent: '=',
                field: '@',
                label: '@',
                divid: '@'
            },
            link: function (scope, element, attrs) {
//                console.log(scope);
                var formHolder;
                var childScope;
                var template;

                if (_.isUndefined(scope.referenceobj)) {
                    var x = {};
                    _.set(scope, 'referenceobj', x);
                }

                scope.toggle = function () {
                    console.log(scope);
                    toggler.toggle(scope, scope.divid, formHolder, scope.referenceobj);
                };
                scope.stage = true;

                switch (attrs.type) {
                    case "amount":
                        //    console.log(scope);
                        if (attrs.mode == "edit") {
                            template = angular.element('<a ng-click ="toggle()"><amount value ="referenceobj.amount" ></amount></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/selectors/amount-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);

                            });
                        }
                        formHolder = '<amount-form referenceobj = referenceobj parent = parent amount=referenceobj.amount></amount-form>';
                        break;
                    case "site":
                        scope.formtype = attrs.formtype;
                        scope.residueregex = attrs.residueregex;
                        scope.mode = attrs.mode;
                        /*                        if (attrs.mode == "edit") {
                         console.log(attrs);
                         template = angular.element('<a ng-click ="toggleStage()"><site-view referenceobj=referenceobj parent = parent></site-view></a>');
                         element.append(template);
                         $compile(template)(scope);
                         } else {*/
                        $templateRequest(baseurl + "assets/templates/selectors/site-selector.html").then(function (html) {
                            template = angular.element(html);
                            element.append(template);
                            $compile(template)(scope);

                        });
                        //                         }
                        formHolder = '<site-string-form referenceobj = referenceobj parent = parent mode=mode residueregex=residueregex formtype = formtype></site-string-form>';
                        break;
                    case "reference":
                        if (attrs.mode == "edit") {
                            $templateRequest(baseurl + "assets/templates/selectors/reference-selector-view.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        } else {
                            $templateRequest(baseurl + "assets/templates/selectors/reference-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<reference-form referenceobj = referenceobj parent = parent></reference-form>';
                        break;
                    case "parameter":
                        if (attrs.mode == "edit") {
                            template = angular.element('<a ng-click ="toggleStage()"><parameters parameters ="referenceobj"></parameters></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/selectors/parameter-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
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
                    case "access":
                        if (attrs.mode == "edit") {
                            template = angular.element('<a ng-click ="toggleStage()"><access value = referenceobj.access></access></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/selectors/access-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<access-form referenceobj = referenceobj parent = parent></access-form>';
                        break;
                    case "textbox":
                        if (attrs.mode == "edit") {
                            //this only works if the attribute is named "comments" will probably need to be addressed later
                            template = angular.element('<a ng-click ="toggle()"><comment value = "referenceobj[field]"></comment></a>');
                            element.append(template);
                            $compile(template)(scope);
                        } else {
                            $templateRequest(baseurl + "assets/templates/selectors/comment-selector.html").then(function (html) {
                                template = angular.element(html);
                                element.append(template);
                                $compile(template)(scope);
                            });
                        }
                        formHolder = '<comment-form referenceobj = referenceobj parent = parent label =label field = field></comment-form>';
                        break;
                }


                scope.toggleStage = function () {
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
                };
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
                //    console.log(scope);
                scope.count = 0;
                if (!scope.parent.protein.glycosylation) {
                    scope.parent.protein.glycosylation = {};

                }
                var arrays = _.pick(scope.parent.protein.glycosylation, _.isArray);
                scope.arrays = _.forOwn(arrays, function (value, key) {
                    scope.count += value.length;
                    var ret = _.set(arrays[key], 'field', _.startCase(key));
                    var ret = _.set(arrays[key], 'name', key);
                });

                scope.validate = function () {
                    if (!scope.parent.protein.glycosylation[scope.glyc.glycosylationSite.value]) {
                        scope.parent.protein.glycosylation[scope.glyc.glycosylationSite.value] = [];
                    }
                    scope.parent.protein.glycosylation[scope.glyc.glycosylationSite.value].push(scope.glyc);
                    scope.glyc = {};
                    scope.glycosylationForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.protein.glycosylation[field].splice(scope.parent.protein.glycosylation[field].indexOf(obj), 1);
                };
            }
        };
    });

    ginasForms.directive('loadCvForm', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: baseurl + "assets/templates/admin/load-cv-form.html"
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
                console.log(scope);


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

    ginasForms.directive('moietyForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/moiety-form.html"
        };
    });

    ginasForms.directive('nameForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/name-form.html"
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
            console.log(slist);
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
            console.log(parent);
            console.log(type);
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
            console.log(sites);
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

        this.getAllSitesWithout = function (type, display) {
            var temp = [];
            _.forEach(display, function (arr) {
                _.forEach(arr, function (subunit) {
                    temp = _.reject(subunit, function (su) {
                        return su[type];
                    });
                });
            });
            if (type == 'linkage') {
                temp = _.dropRight(temp);
            }
            return temp;
        };

        this.applyAll = function (type, parent, obj) {
            var plural = type + "s";
            if (parent.nucleicAcid[plural].length == 0) {
                if (type == 'linkage') {
                    obj.$displayString = siteList.allSites(parent, 'nucleicAcid', type);
                } else {
                    obj.$displayString = siteList.allSites(parent, 'nucleicAcid');
                }
                obj.sites = siteList.siteList(obj.$displayString);

                //this applies the sugar property to the display object
                _.forEach(obj.sites, function (site) {
                    _.set(parent.$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1], type, true);
                });
            } else {
                obj.$displayString = siteList.siteString(this.getAllSitesWithout(type, parent.$subunitDisplay));
                obj.sites = siteList.siteList(obj.$displayString);
            }
        };

        this.clearSites = function (type, parent, obj) {
            _.forEach(obj, function (site) {
                parent.$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1] = _.omit(parent.$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1], type);
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
                    return siteAdder.getAll('sugar', scope.parent.$subunitDisplay).length + siteAdder.getAllSitesWithout('sugar', scope.parent.$subunitDisplay).length;
                };

                scope.applyAll = function () {
                    siteAdder.applyAll('sugar', scope.parent, scope.sugar);
                    scope.noSugars = siteAdder.getAllSitesWithout('sugar', scope.parent.$subunitDisplay).length;
                };

                scope.validate = function () {
                    _.forEach(scope.sugar.sites, function (site) {
                        _.set(scope.parent.$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1], 'sugar', true);
                    });
                    scope.parent.nucleicAcid.sugars.push(scope.sugar);
                    scope.noSugars = siteAdder.getAllSitesWithout('sugar', scope.parent.$subunitDisplay).length;
                    scope.sugar = {};
                    scope.sugarForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.nucleicAcid.sugars.splice(scope.parent.nucleicAcid.sugars.indexOf(obj), 1);
                    siteAdder.clearSites('sugar', scope.parent, obj.sites);
                    scope.noSugars = siteAdder.getAllSitesWithout('sugar', scope.parent.$subunitDisplay).length;
                };

                scope.noSugars = siteAdder.getAllSitesWithout('sugar', scope.parent.$subunitDisplay).length;
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
                    return siteAdder.getAll('linkage', scope.parent.$subunitDisplay).length + siteAdder.getAllSitesWithout('linkage', scope.parent.$subunitDisplay).length;
                };

                scope.applyAll = function () {
                    siteAdder.applyAll('linkage', scope.parent, scope.linkage, 'linkage');
                    scope.noLinkages = siteAdder.getAllSitesWithout('linkage', scope.parent.$subunitDisplay).length;
                };

                scope.validate = function () {
                    _.forEach(scope.linkage.sites, function (site) {
                        _.set(scope.parent.$subunitDisplay[site.subunitIndex - 1][site.residueIndex - 1], 'linkage', true);
                    });
                    scope.parent.nucleicAcid.linkages.push(scope.linkage);
                    scope.noLinkages = siteAdder.getAllSitesWithout('linkage', scope.parent.$subunitDisplay).length;
                    scope.linkage = {};
                    scope.linkageForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.nucleicAcid.linkages.splice(scope.parent.nucleicAcid.linkages.indexOf(obj), 1);
                    siteAdder.clearSites('linkage', scope.parent, obj.sites);
                    scope.noLinkages = siteAdder.getAllSitesWithout('linkage', scope.parent.$subunitDisplay).length;
                };

                scope.noLinkages = siteAdder.getAllSitesWithout('linkage', scope.parent.$subunitDisplay).length;
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
                if (!scope.parent.protein.otherLinks) {
                    scope.parent.protein.otherLinks = [];
                }

                scope.validate = function () {
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
                console.log(scope);

                scope.validate = function () {
                    console.log(scope.referenceobj);
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
            templateUrl: baseurl + "assets/templates/forms/physical-modification-form.html",
            link: function (scope, element, attrs) {
                if (!scope.parent.protein.modifications) {
                    scope.parent.protein.modifications = {};
                }
                if (!scope.parent.protein.modifications.physicalModifications) {
                    scope.parent.protein.modifications.physicalModifications = [];
                }
                scope.validate = function () {
                    scope.parent.protein.modifications.physicalModifications.push(scope.physicalModification);
                    scope.physicalModification = {};
                    scope.physicalModForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.protein.modifications.physicalModifications.splice(scope.parent.protein.modifications.physicalModifications.indexOf(obj), 1);
                };
            }
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
                console.log(scope);

                scope.validate = function () {
                    console.log(scope.referenceobj);
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
            templateUrl: baseurl + "assets/templates/forms/polymer-monomer-form.html",
            link: function (scope) {
                console.log(scope);
            }
        };
    });

    ginasForms.directive('polymerSruForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/polymer-sru-form.html",
            link: function (scope) {
                console.log(scope);
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
            templateUrl: baseurl + "assets/templates/forms/protein-details-form.html"
        };
    });

    ginasForms.directive('referenceApply', function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                apply: '=ngModel',
                obj: '=',
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
                    console.log("update)");
                    console.log(scope);
                    index = _.indexOf(scope.referenceobj.references, uuid);
                    console.log(index);
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
                console.log(scope);

                scope.submitFile = function () {
                    //create form data object
                    var fd = new FormData();
                    //  fd.append('file', scope.uploadFile);
                    fd.append('file-name', scope.uploadFile);
                    fd.append('file-type', scope.uploadFile.type);
                    //send the file / data to your server
                    $http.post(baseurl + 'upload', fd, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    }).success(function (data) {
                        console.log(data);
                        _.set(scope.ref, 'uploadedFile', data.url);
                    }).error(function (err) {
                        console.log(err);
                    });
                };

                scope.validate = function () {
                    if (!_.isUndefined(scope.ref.citation)) {
                        _.set(scope.ref, "uuid", UUID.newID());
                        if (scope.ref.apply) {
                            scope.saveReference(scope.ref.uuid, scope.referenceobj);
                            scope.saveReference(angular.copy(scope.ref), scope.parent);
                        } else {
                            scope.saveReference(scope.ref, scope.parent);
                        }
                        scope.ref = {};
                        scope.ref.apply = true;
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

    ginasForms.directive('referenceFormOnly', function ($http) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/reference-form-only.html",
            link: function (scope, element, attrs) {
                console.log(scope);
                scope.submitFile = function () {
                    //create form data object
                    var fd = new FormData();
                    //  fd.append('file', scope.uploadFile);
                    fd.append('file-name', scope.uploadFile);
                    fd.append('file-type', scope.uploadFile.type);
                    //send the file / data to your server
                    $http.post(baseurl + 'upload', fd, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    }).success(function (data) {
                        console.log(data);
                        _.set(scope.ref, 'uploadedFile', data.url);
                    }).error(function (err) {
                        console.log(err);
                    });
                };


                scope.validate = function () {
                    scope.saveReference(scope.ref, scope.parent);
                    scope.ref = {};
                    scope.ref.apply = true;
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
            templateUrl: baseurl + "assets/templates/forms/relationship-form.html"
        };
    });

    ginasForms.directive('siteForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                sites: '=sites'
            },
            templateUrl: baseurl + "assets/templates/forms/site-form.html"
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
                residueregex: '='
            },
            link: function (scope, element, attrs) {
                console.log(scope);
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
                    //    console.log("hi");
                    if (!su)return [];
                    var list = [];
                    if (scope.residueregex) {
                        var ret = scope.parent[scope.parent.substanceClass].subunits[su - 1].cysteineIndices;
                        if (scope.parent.protein.disulfideLinks.length > 0) {
                            _.forEach(scope.parent.protein.disulfideLinks, function (siteList) {
                                _.forEach(siteList.sites, function (site) {
                                    var v;
                                    if (site.subunitIndex == (su)) {
                                        v = _.remove(ret, function (n) {
                                            return n == site.residueIndex
                                        });
                                    }
                                });
                            });
                        }
                        return ret;
                        /* console.log(scope.subunits[su-1]);
                         var re = new RegExp(scope.residueregex, 'ig');
                         var match;
                         while ((match = re.exec(scope.subunits[su - 1].sequence)) !== null) {
                         list.push(match.index + 1);
                         }
                         return list;*/
                    } else {
                        return _.range(1, scope.subunits[su - 1].sequence.length + 1);
                    }
                };

                scope.getSubunitRange = function () {
                    return _.range(1, scope.subunits.length + 1);
                };

                scope.makeSiteList = function () {
                    scope.referenceobj.sites = siteList.siteList(scope.referenceobj.$displayString);
                };

                scope.redraw = function () {
                    scope.referenceobj.$displayString = siteList.siteString(scope.referenceobj.sites);
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

    ginasForms.directive('structuralModificationForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/structural-modifications-form.html",
            link: function (scope, element, attrs) {
                console.log(scope);
                if (!scope.parent.protein.modifications) {
                    scope.parent.protein.modifications = {};
                }
                if (!scope.parent.protein.modifications.structuralModifications) {
                    scope.parent.protein.modifications.structuralModifications = [];
                }

                scope.validate = function () {
                    scope.parent.protein.modifications.structuralModifications.push(scope.mod);
                    scope.mod = {};
                    scope.strucModForm.$setPristine();
                };

                scope.deleteObj = function (obj) {
                    scope.parent.protein.modifications.structuralModifications.splice(scope.parent.protein.modifications.structuralModifications.indexOf(obj), 1);
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
                path: '@path'
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

    ginasForms.directive('subunitForm', function ($compile, $templateRequest, CVFields) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                residues: '='
            },
            link: function (scope, element, attrs) {
                scope.parent.$subunitDisplay = [];
                var template;
                if (scope.parent.substanceClass === 'protein') {
                    CVFields.getCV("AMINO_ACID_RESIDUES").then(function (data) {
                        console.log(data);
                        scope.residues = data.data.content[0].terms;
                        $templateRequest(baseurl + "assets/templates/forms/subunit-form.html").then(function (html) {
                            template = angular.element(html);
                            element.append(template);
                            $compile(template)(scope);
                        });
                    });
                } else {
                    CVFields.getCV("NUCLEIC_ACID_BASE").then(function (data) {
                        scope.residues = data.data.content[0].terms;
                        $templateRequest(baseurl + "assets/templates/forms/subunit-form.html").then(function (html) {
                            template = angular.element(html);
                            element.append(template);
                            $compile(template)(scope);
                        });
                    });
                }

                scope.validate = function () {
                    if (scope.subunit.sequence.length > 10000) {
                        alert('Ginas can currently only support sequences less than 10000 characters in length');
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
            },
            //  templateUrl: baseurl + "assets/templates/subunit-form.html"
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