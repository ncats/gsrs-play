(function () {
    var ginasForms = angular.module('ginasForms', ['bootstrap.fileField']);

    ginasForms.service('toggler', function ($compile, $templateRequest, spinnerService) {

        var childScope;
        this.stageCheck = function () {
            return this.stage;
        };

        this.show = function (scope, element, url) {
            if (_.isUndefined(scope.type)) {
                scope.type = element;
            }
            if (_.isUndefined(scope.stage)) {
                scope.stage = true;
            }

            var template = "";
            var result = document.getElementsByClassName(scope.type);
            var elementResult = angular.element(result);
            if (scope.stage === true) {
                scope.stage = false;
                $templateRequest(url).then(function (html) {
                    console.log(html);
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
                if (childScope) {
                    childScope.$destroy();
                }
                elementResult.empty();
                scope.stage = true;
            }
        };
    });


    //generic controller that all forms will have. has acces to specific form scopes, and the more specific forms can call these methods
    ginasForms.controller('formController', function ($scope, $http, $anchorScroll, $location) {
        $scope.iscollapsed = true;

        //mainform is the form obj
        //location is the string of the array, can be nested
        //obj is a modified object that will be added
        $scope.addNew = function (mainForm, location, obj, begin) {
            var temp = {};
            if (obj) {
                temp = obj;
            }
            var listObj = _.get($scope.parent, location);
            if (!_.isUndefined(listObj) && listObj.length > 0) {
                if (mainForm.form && mainForm.form.$invalid) {
                    mainForm.form.$flagged = true;
                }
                if(begin){
                    listObj.unshift(temp);
                }else {
                listObj.push(temp);
                }
                _.set($scope.parent, location, listObj);
            } else {
                listObj = [];
                if(begin){
                    listObj.unshift(temp);
                }else {
                listObj.push(temp);
                }
                _.set($scope.parent, location, listObj);
            }
        };

        $scope.submitFile = function (obj) {
            //create form data object
            var fd = new FormData();
            if (obj) {
                $scope.$$uploadFile = obj.$$uploadFile;
            }
            fd.append('file-name', $scope.$$uploadFile);
            fd.append('file-type', $scope.$$uploadFile.type);
            //send the file / data to your server
            $http.post(baseurl + 'upload', fd, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            }).success(function (data) {
                if (obj) {
                    _.set(obj, 'uploadedFile', data.url);
                    delete obj.$$uploadFile;
                } else {
                    _.set($scope.obj, 'uploadedFile', data.url);
                }
            }).error(function (err) {
                _.set(obj, 'uploadedFile', false);
                _.set(obj, '$$uploadFile', false);
            });
            delete $scope.$$uploadFile;
        };

        $scope.scrollTo = function (prmElementToScrollTo) {
            $anchorScroll(prmElementToScrollTo);
        };
    });

    ginasForms.controller('cvFormController', function ($scope, CVFields, spinnerService) {
        $scope.load = function () {
            CVFields.all(false).then(function (response) {
                _.forEach(response.data.content, function (domain) {
                    CVFields.search("VOCAB_TYPE", domain.vocabularyTermType).then(function (response) {
                        domain.vocabularyTermType = response[0];
                    })
                });
                _.set($scope, 'cv', response.data.content);
                $scope.count = $scope.cv.length;
            });

        };

        $scope.download = function () {
            return $scope.cv;
        };


    });

    ginasForms.factory('subunitParser', function(CVFields){
        var factoryResidues;

        var factory = this;
        
        /**
         * Returns the *promise* of the CV elements for the
         * specified residues (NA or AA).
         *
         * This can be used to pre-fetch the residues to avoid
         * using the factory before it is ready.
         * 
         */
        factory.getResidues= function(substanceClass){
            var residues;
            var cls;
            if (substanceClass === 'protein') {
                cls = "AMINO_ACID_RESIDUE";
            }else {
                cls = "NUCLEIC_ACID_BASE";
            }
            return CVFields.getCV(cls).then(function (response) {
                factoryResidues = response.data.content[0].terms;
                return response.data.content[0].terms;
            });
        };

        factory.cleanSequence = function (sequence) {
            if(_.isUndefined(factoryResidues)) {
                factory.getResidues(subclass);
            }
            sequence = _.filter(sequence, function (aa) {
                var temp = (_.find(factoryResidues, ['value', aa.toUpperCase()]));
                if (!_.isUndefined(temp)) {
                    return temp;
                }
            }).toString().replace(/,/g, '');
            return sequence;
        };

        factory.getStereoType = function (aa) {
            if (aa == aa.toLowerCase()) {
                return 'D-';
            }
            else {
                return 'L-';
            }
        };

       
        factory.getOtherSite = function(site,sites){
                var retsite=site;
                _.forEach(sites, function(s){
                        if(s!==site){
                                retsite=s;
                        }
                });
                return retsite;
        };
        /**
          * Returns a list of all modified sites in the substance
          * including sugars, linkages, structural modifications,
          * disulfides, other links, and glycosylation sites
          *
          * Sites references may be duplicated, in that more than
          * one site may have the same shorthand notation.
          *
          * If "asmap" is true, the returned object will be an 
	  * associative array of the shorthand site keys to the 
          * objects
          *
          */
        factory.getAllModifiedSites = function (parent, asmap){
                var sites = [];

                if (_.has(parent, 'modifications.structuralModifications')) {
                        _.forEach(parent.modifications.structuralModifications, function(mod, index){
                                 sites=_.concat(sites,factory.markSites(mod.sites, "structuralModifications", mod.molecularFragment));
                        });
                }
                if (parent.substanceClass === 'protein') {
                        if (_.has(parent.protein, 'glycosylation')) {
                            sites=_.concat(sites,factory.markSites(parent.protein.glycosylation.CGlycosylationSites, "glycosylation"));
                            sites=_.concat(sites,factory.markSites(parent.protein.glycosylation.NGlycosylationSites, "glycosylation"));  
                            sites=_.concat(sites,factory.markSites(parent.protein.glycosylation.OGlycosylationSites, "glycosylation"));  
                        }
                        if (_.has(parent.protein, 'disulfideLinks')) {
                            _.forEach(parent.protein.disulfideLinks, function(link, index){
                                 sites=_.concat(sites,factory.markSites(link.sites, "disulfide", factory.getOtherSite));
                            });
                        }
                        if (_.has(parent.protein, 'otherLinks')) {
                            _.forEach(parent.protein.otherLinks, function(link, index){
                                  sites=_.concat(sites,factory.markSites(link.sites, "otherLinks", factory.getOtherSite));
                            });
                        }
                } else if (parent.substanceClass === 'nucleicAcid') {
                        if (_.has(parent.nucleicAcid, 'sugars')) {
                            _.forEach(parent.nucleicAcid.sugars, function(link, index){
                                 sites=_.concat(sites,factory.markSites(link.sites, "sugar"));
                            });
                        }
                        if (_.has(parent.nucleicAcid, 'linkages')) {
                            _.forEach(parent.nucleicAcid.linkages, function(link, index){
                                 sites=_.concat(sites,factory.markSites(link.sites, "linkage"));
                            });
                        }
                }
		if(asmap){
			return factory.sitesAsMap(sites);
		}
                return sites;
        };

        /**
          * Return a site array as a map, with the key being the
          * shorthand form of the site. All properties are merged
          * if there is a key collision, with the older list attributes
          * being honored preferentially
          */
        factory.sitesAsMap = function (sites){
                var map = {};
                _.forEach(sites, function(site, index){
                        var key=site.subunitIndex + "_" + site.residueIndex;
                        oldsite = map[key];
                        if(oldsite){
                                site=_.merge(oldsite,site);
                        }
                        map[key]=site;
                });
                map.get = function (site){
                        var key=site.subunitIndex + "_" + site.residueIndex;
                        return this[key];
                };
                return map;
         };

        factory.markSites = function (sites, mark, gen){
                var ret=[];
		var ogen=gen;
		if(ogen && typeof ogen !== "function"){
			gen = function(){
				return ogen;
			};
		}
                if(!gen){
                        gen = function(){
                                return true;
                        };
                }
                if(sites){
                        _.forEach(sites, function(site){
                                        var cp=angular.copy(site);
                                        _.set(cp,mark,gen(site,sites));
                                        ret.push(cp);
                                }
                        );
                }
                return ret;
        };

	 /**
          * Recalculates the subunit display chunks, used both as a rendering aid,
          * and in some other methods as a quick cache of what sites are modified
          * or otherwise enhanced.
          * 
          */
        factory.parseSubunits = function (substance) {
            var subclass = substance.substanceClass;
            var subunits;
            if(subclass === "protein"){
                subunits=substance.protein.subunits;
            }else if(subclass === "nucleicAcid"){
                subunits=substance.nucleicAcid.subunits;
            }        
            _.forEach(subunits, function (su, index) {
                factory.parseSubunit(substance,su,index+1);
            });
                
        };
        /**
          * This method accepts a substance, a subunit, and an optional subunit index
          * and then computes a chunked display cache of the residue sites contained
          * within, storing it as '$$subunitDisplay'
          *
          */
        factory.parseSubunit = function (parent, subunit, subunitIndex) {
            var subclass = parent.substanceClass;
            if(_.isUndefined(factoryResidues)) {
                factory.getResidues(subclass);
            }
            var display = [];
            var modifiedSitesMap = factory.sitesAsMap(factory.getAllModifiedSites(parent));

            _.forEach(subunit.sequence, function (aa, index) {
                var obj = {};
                obj.value = aa;
                var temp = (_.find(factoryResidues, ['value', aa.toUpperCase()]));
                if (!_.isUndefined(temp)) {
                    obj = _.pickBy(temp, _.isString);
                    obj.value = aa;
                    obj.valid = true;
                    
                    if (subunit.subunitIndex) {
                        obj.subunitIndex = subunit.subunitIndex;
                    } else {
                        obj.subunitIndex = subunitIndex;
                    }
                    obj.residueIndex = index - 0 + 1;
                     if (parent.substanceClass === 'protein') {
                        //parse out cysteines first
                        if (aa.toUpperCase() == 'C') {
                            obj.cysteine = true;
                        }else{
                            obj.cysteine = false;
                        }
                    }
                    var modc = modifiedSitesMap.get(obj);
                    if(modc){
                        obj=_.merge(modc,obj);
                    }

                } else {
                    obj.valid = false;
                }
                display.push(obj);
            });
            display = _.chunk(display, 10);
            _.set(subunit, '$$subunitDisplay', display);
        };
        return factory;
    });
    
    
    
    ginasForms.directive('accessForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/access-form.html"
        };
    });

    ginasForms.directive('agentModificationForm', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
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
                filter: '='
            },
            templateUrl: baseurl + "assets/templates/forms/amount-form.html"
        };
    });

    ginasForms.directive('codeForm', function (validatorFactory) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            templateUrl: baseurl + "assets/templates/forms/code-form.html",
            scope: {
                parent: '='
            },
            link: function (scope, element, attrs) {
                scope.parse = function (code) {
                    var errors = [];
                    if (_.isUndefined(code.formatter)) {
                        errors.push({text: 'no code system yet', type: 'warning'});
                    } else {
                        var valid = validatorFactory.validate(code.formatter.value, code.model);
                        if (valid == false) {
                            errors.push({text: 'invalid', type: 'danger'});
                        } else {
                            //  errors.push({text: 'valid', type: 'success'});
                        }
                    }
                    return errors;
                };
            }
        };
    });

    ginasForms.directive('conceptUpgradeForm', function ($window, $location, localStorageService) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: baseurl + "assets/templates/forms/concept-upgrade-form.html",
            scope: {
                parent: '='
            },
            link: function (scope, element, attrs) {

                scope.iscollapsed = false;
                if (scope.parent.uuid) {
                    scope.editid = scope.parent.uuid.split('-')[0];
                }

                scope.changeClass = function (newClass) {
                    var upgradeSub = scope.parent.$$setClass(newClass);
                    _.set(upgradeSub, 'update', true);
                    localStorageService.set('tempsubstance', upgradeSub);
                    
                    $location.search("kind",newClass);
                    window.location = $location.absUrl();
                };
            }
        };
    });

    ginasForms.directive('disulfideLinkForm', function (siteAdder, subunitParser) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                parent: '=',
                referenceobj: '=',
                formype: '@',
                residueregex: '@'

            },
            templateUrl: baseurl + "assets/templates/forms/disulfide-link-form.html",
            link: function (scope, element, attrs) {
                scope.addLink = function (form, path, begin) {
                    scope.removeUsed();
                    scope.addNew(form, path, '', begin);
                };

                scope.removeUsed = function(){
                    var ret = [];

                    var cys = angular.copy(scope.cysteines);

                    //this sets the array of used sites
                    _.forEach(scope.parent.protein.disulfideLinks, function (link) {
                        _.forEach(link.sites, function (site) {
                            ret.push(_.omit(site, '$$hashKey'));
                        });
                    });

                    //this removes the sites from the cv
                    _.forEach(ret, function (used) {
                         _.remove(cys, function (c) {
                            return c.value === used.value;
                        });
                    });
                    //set the cv to be the copied array
                    scope.cysteines = cys;
                };

                //this is called before the object is deleted, so removing used doesn't work
                scope.$on('delete', function (e) {

                   var cys = angular.copy(siteAdder.getAllSitesWithValue('C', scope.parent.protein.subunits));
                    _.forEach(cys, function (site) {
                        _.set(site, 'display', site.subunitIndex + '_' + site.residueIndex);
                        _.set(site, 'value', site.subunitIndex + '_' + site.residueIndex);
                    });

                    //this removes everything that has a cysteine set before deleting the selected one
                    scope.removeUsed();

                     //iterate over sites and remove the 2 sites contained in the delete obj
                          _.forEach(e.targetScope.obj.sites, function (site) {
                              scope.cysteines.push(site);
                          });
                    scope.cysteines  = _.orderBy(scope.cysteines, 'value');
                });

                scope.$on('removed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });
                

                scope.clean = function (model, site, index) {
                   scope.removeUsed();
                   subunitParser.parseSubunits(scope.parent);
                };

                scope.remove = function(tag){
                    scope.removeUsed();
                    scope.cysteines.push(tag);
                    scope.cysteines  = _.orderBy(scope.cysteines, 'value');
                }

                //set the views on loading/editing a substance
                if (scope.parent.protein.disulfideLinks) {
                    _.forEach(scope.parent.protein.disulfideLinks, function (link) {
                        _.forEach(link.sites, function (site) {
                            if (!site.display) {
                                _.set(site, 'display', site.subunitIndex + '_' + site.residueIndex);
                                _.set(site, 'value', site.subunitIndex + '_' + site.residueIndex);
                            }
                        });
                    });
               }

                var ff = function (newValue, oldValue, scope) {
                    //this will update the cv on subunit change, excluding used subunits.
                    //this doesn't remove them from the cv if they are added to the disulfide links

                    //have to use angular.copy so the display value doesn't change for the subunit display
                    var t = angular.copy(siteAdder.getAllSitesWithValue('C', scope.parent.protein.subunits));
                    _.forEach(t, function (site) {
                        _.set(site, 'display', site.subunitIndex + '_' + site.residueIndex);
                        _.set(site, 'value', site.subunitIndex + '_' + site.residueIndex);
                    });
                    scope.cysteines= t;
                    scope.removeUsed();
                };
                scope.$watch(function(scope){
                        return _.get(scope.parent, "protein.subunits",null);

                }, ff, true);


                ff(scope.parent.protein.subunits,null, scope);
                scope.clean();
            }
        }
    });

    ginasForms.directive('nucleicAcidSugarForm', function (siteList, siteAdder, subunitParser) {
        return {
            restrict: 'E',
            replace: true,
            controller:'formController',
            scope: {
                parent: '=',
                referenceobj: '='
            },
            templateUrl: baseurl + "assets/templates/forms/nucleic-acid-sugar-form.html",
            link: function (scope, attrs, element) {

                scope.getAllSites = function () {
                    return siteAdder.getCount(scope.parent.nucleicAcid.subunits);
                };

                scope.applyAll = function (obj, index) {
                    obj.$$displayString="";
                    obj.sites.$$displayString="";
                    obj.sites.length=0;
                    siteAdder.applyAll('sugar', scope.parent, obj);
                };


                //this is called every time something is hovered over -- need to fix
                scope.noSugars = function () {
                      return siteAdder.getAllSitesWithout('sugar',scope.parent.nucleicAcid.subunits).length;
                };
                scope.$on('removed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });
                scope.$on('changed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });

            }
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
            if(!slist)return [];
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

    ginasForms.service('siteAdder', function (siteList, subunitParser) {

        this.getAll = function (type, display) {
            var temp = [];
            _.forEach(display, function (subunit) {
                temp = _.filter(subunit, function (su) {
                    return su[type];
                });
            });
            return temp;
        };

        this.getAllSitesWith = function (type, display) {
            var ret = [];
            _.forEach(display, function (subunit) {
               var temp = _.filter(_.flattenDeep(subunit.$$subunitDisplay), function (su) {
                    return su[type];
                });
                ret = _.concat(ret, temp);
            });
            return ret;
        };
        
        this.getAllSitesWithout = function (type, display) {
            var temp = [];
            _.forEach(display, function (subunit) {
                if(!subunit) return;
                if(!subunit.sequence) return;
                _.forEach(subunit.$$subunitDisplay, function (chunk) {
                    var tempadd = _.reject(chunk, function (aa) {
                        return aa[type];
                    });
                    temp = _.concat(temp,tempadd);
                });
                if (type === 'linkage' && subunit.sequence.length>0) {
                        temp = _.dropRight(temp);
                    }
            });
            return temp;
        };

        
        this.getAllSitesWithValue = function (residue, subunits) {
            var ret = [];
            _.forEach(subunits, function (subunit) {
                var si = subunit.subunitIndex;

                if(subunit.sequence) {
                    for (var i = 0; i < subunit.sequence.length; i++) {
                        var r = subunit.sequence[i];
                        if (r === residue) {
                            ret.push({subunitIndex: si, residueIndex: i + 1});
                        }
                    }
                }
            });
            return ret;
        };

        this.getCount = function (subunits) {
            var count = 0;
            _.forEach(subunits, function (subunit) {
                _.forEach(subunit.$$subunitDisplay, function (chunk) {
                    count += chunk.length;
                });
            });
            return count;
        };



        this.applyAll = function (type, parent, obj) {
            subunitParser.parseSubunits(parent);
        
            var plural = type + "s";
            

            if (parent.nucleicAcid[plural].length == 0) {
                if (type == 'linkage') {
                    obj.$$displayString = siteList.allSites(parent, 'nucleicAcid', type);
                } else {
                    obj.$$displayString = siteList.allSites(parent, 'nucleicAcid');
                }
                obj.sites = siteList.siteList(obj.$$displayString);

            } else {
                var sites2=this.getAllSitesWithout(type, parent.nucleicAcid.subunits);
                obj.$$displayString = siteList.siteString(sites2);
                obj.sites = siteList.siteList(obj.$$displayString);                
                obj.sites.$$displayString=obj.$$displayString;
            }
            subunitParser.parseSubunits(parent);

        };


        this.toZeroIndexChunks = function (site){
                var chunkIndex=Math.floor((site.residueIndex-1) / 10);
                var residueSubIndex=Math.floor((site.residueIndex-1) % 10);
                var subIndex = site.subunitIndex -1;
                return [subIndex,chunkIndex,residueSubIndex];
        }
        
    });

    ginasForms.directive('nucleicAcidLinkageForm', function (siteList, siteAdder, subunitParser) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                referenceobj: '=',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/nucleic-acid-linkage-form.html",
            link: function (scope, attrs, element) {

                scope.getAllSites = function () {
                    return siteAdder.getCount(scope.parent.nucleicAcid.subunits) - scope.parent.nucleicAcid.subunits.length;
                };

                scope.applyAll = function (obj, index) {
                    obj.$$displayString="";
                    obj.sites.$$displayString="";
                    obj.sites.length=0;
                    siteAdder.applyAll('linkage', scope.parent, obj);
                };

                scope.noLinkages = function () {
                    return siteAdder.getAllSitesWithout('linkage',scope.parent.nucleicAcid.subunits).length;
                }

                 scope.$on('removed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });
                scope.$on('changed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });
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
            link: function (scope) {
                scope.iscollapsed = false;
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
                scope.parent.$$diverseType = "whole";
                if (_.isUndefined(scope.parent.structurallyDiverse.part)) {
                    scope.parent.structurallyDiverse.part = [];
                    _.set(scope.parent.structurallyDiverse, 'part', ['WHOLE']);
                }
                if (scope.parent.structurallyDiverse.part.length > 0 && scope.parent.structurallyDiverse.part[0] != 'WHOLE') {
                    _.set(scope.parent, '$$diverseType', 'part');
                    _.set(scope, '$$temp', scope.parent.structurallyDiverse.part);
                }
                scope.checkType = function () {
                    if (scope.parent.$$diverseType === 'whole') {
                        scope.parent.$$diverseType = 'whole';
                        _.set(scope.parent.structurallyDiverse, 'part', ['WHOLE']);
                    } else {
                        if (scope.$$temp) {
                            _.set(scope.parent.structurallyDiverse, 'part', scope.$$temp);
                        } else {
                            _.set(scope.parent.structurallyDiverse, 'part', []);
                        }
                    }
                };
            }
        };
    });

    ginasForms.directive('glycosylationForm', function (subunitParser) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                parent: '=',
                referenceobj: '='
            },
            templateUrl: baseurl + "assets/templates/forms/glycosylation-form.html",
            link: function (scope, element, attrs) {
                _.forEach(scope.parent.protein.glycosylation, function (value) {
                    if (_.isArray(value) && value.length > 0) {
                        scope.iscollapsed = false;
                    }
                });
                scope.$on('changed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });
            }

        };
    });

    ginasForms.directive('headerForm', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '='
            },
            link: function (scope, element, attrs) {
                if (scope.parent._name) {
                    scope.formType = 'Editing';
                    scope.name = scope.parent._name;
                } else {
                    scope.formType = 'Registering new';
                    scope.name = _.startCase(scope.parent.substanceClass);
                }
            },
            templateUrl: baseurl + "assets/templates/forms/header-form.html"
        };
    });

    ginasForms.directive('mixtureComponentSelectForm', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
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

    ginasForms.directive('nameForm', function ($q, substanceFactory, resolver, toggler, spinnerService) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/name-form.html",
            link: function (scope) {

                scope.duplicateCheck = function (name) {
                    var errors = [];
                    var result = angular.element(document.getElementsByClassName('nameForm'));
                    result.empty();
                    var resolve = resolver.resolve(name).then(function (response) {
                        if (response.data.length > 0) {
                            return response.data;
                        }
                        return [];
                    });
                    var duplicate = substanceFactory.getSubstances(name).then(function (response) {
                        var duplicate = [];
                        if (response.data.count > 0) {
                            errors.push({text: 'duplicate name', type: 'warning'});
                            _.forEach(response.data.content, function (sub) {
                                _.set(sub, 'refType', 'duplicate');
                                duplicate.push(sub);
                            });
                        }
                        return duplicate;
                    });
                    if (!_.isUndefined(name) && name !== "") {
                        spinnerService.show('nameSpinner');
                        var template;
                        $q.all([resolve, duplicate]).then(function (results) {
                            scope.data = [];
                            var temp = [];
                            _.forEach(results, function (result) {
                                if (!_.isUndefined(result)) {
                                    temp.push(result);
                                }
                                scope.data = _.flattenDeep(temp);
                            });
                        }).finally(function () {
                            if (_.isEmpty(scope.data)) {
                                scope.data.push("empty");
                            }
                            template = angular.element('<substance-viewer data = data parent = parent obj = name></substance-viewer>');
                            toggler.refresh(scope, 'nameForm', template);
                        });
                    } else {
                        spinnerService.hideAll();
                    }
                    return errors;
                };

            }
        };
    });

    ginasForms.directive('nameOrgForm', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                parent: '=',
                referenceobj: '=?'
            },
            templateUrl: baseurl + "assets/templates/forms/name-org-form.html"
        };
    });

    ginasForms.directive('noteForm', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
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

    ginasForms.directive('otherLinksForm', function (subunitParser) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
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
                scope.$on('changed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });
                scope.$on('removed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });

            }
        };
    });

    ginasForms.directive('parameterForm', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                parent: '=',
                referenceobj: '=?',
            },
            templateUrl: baseurl + "assets/templates/forms/parameter-form.html",
            link: function (scope) {
                console.log(scope);
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
            controller: 'formController',
            scope: {
                parent: '=',
                referenceobj: '=?'// this is the object from the form that is getting the property added to it
            },
            templateUrl: baseurl + "assets/templates/forms/physical-modification-form.html",
            link: function (scope) {

            }
        };
    });

    ginasForms.directive('physicalParameterForm', function () {
        return {
            restrict: 'E',
            replace: 'true',
            controller: 'formController',
            scope: {
                parent: '=',
                referenceobj: '=?'// this is the object from the form that is getting the parameter added to it
            },
            templateUrl: baseurl + "assets/templates/forms/physical-parameter-form.html"
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
            controller: 'formController',
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
            controller: 'formController',
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/polymer-sru-form.html",
            link: function (scope) {
                scope.validateConnectivity = function (obj) {
                    var map = polymerUtils.sruDisplayToConnectivity(obj);
                    return map.errors;
                }
            }
        };
    });

    ginasForms.directive('propertyForm', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
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
            link: function (scope) {
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

    ginasForms.directive('referenceForm', function (UUID) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                parent: '=',
                referenceobj: '=?',// this is the object from the form that is getting the references added to it
                iscollapsed: '=?'
            },
            templateUrl: baseurl + "assets/templates/forms/reference-form.html",
            link: function (scope, element, attrs) {

                console.log(scope);
                scope.addNewRef = function (mainform, list, begin) {
                    //passes a new uuid for reference tracking
                    var obj = {
                        uuid: UUID.newID(),
                        $$apply: true,
                        $$update: true
                    };
                    scope.addNew(mainform, list, obj, begin);
                };
                scope.applyRefs = attrs.apply;

                //called on close of the modal reference form. saves all applied references to the array
                scope.$on('save', function (e) {
                    scope.validate();
                });

                scope.validate = function () {
                    //grabs an array of all the uuids of the references where apply checkbox is true
                    var objreferences = _
                        .chain(scope.parent.references)
                        .filter(function (ref) {
                            if (ref.$$apply) {
                                return ref;
                            }
                        })
                        .map('uuid')
                        .value();
                    _.set(scope.referenceobj, 'references', objreferences);
                };

                //the delete button emits the delete object, which is used here to remove the reference form all arrays that use it
                scope.$on('delete', function (e) {
                    var obj = scope.parent;
                    var del = e.targetScope.obj.uuid;
                    scope.deleteObj(obj, del);
                });

                //recursive object iteration method
                scope.deleteObj = function (obj, ref) {
                    _.forEach(_.keysIn(obj), function (field) {
                        if (_.isObject(obj[field])) {
                            if (_.isArray(obj[field])) {
                                _.forEach((obj[field]), function (value, key) {
                                    if (_.isObject(value)) {
                                        if (_.indexOf(_.keysIn(value), 'references') > -1) {
                                            scope.deleteObjectReferences(value, ref);
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

                //actual method to delete the reference uuid from an object references array
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
            }
        };
    });

    ginasForms.directive('relationshipForm', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/relationship-form.html"
        };
    });

    ginasForms.directive('ssConstituentForm', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/ss-constituent-form.html"
        };
    });

    ginasForms.directive('structuralModificationForm', function (CVFields,subunitParser) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                referenceobj: '=?',
                parent: '='
            },
            templateUrl: baseurl + "assets/templates/forms/structural-modifications-form.html",
            link: function (scope) {
                scope.getCV = function (mod) {
                    if (mod == "AMINO_ACID_SUBSTITUTION") {
                        return "AMINO_ACID_RESIDUE";
                    } else if (mod == "NUCLEOSIDE_SUBSTITUTION") {
                        return "NUCLEIC_ACID_BASE";
                    } else {
                        return null;
                    }
                };
		scope.$on('removed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });
                scope.$on('changed', function (e) {
                    subunitParser.parseSubunits(scope.parent);
                });
            }
        };
    });

    ginasForms.directive('structureForm', function ($http, $templateRequest, $compile, molChanger) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                parent: '=',
                type: '@'
            },
            link: function (scope, element) {
                if (scope.parent.substanceClass === 'polymer') {
                    if (_.has(scope.parent.polymer.idealizedStructure)) {
                        _.set(scope.parent, 'structure', scope.parent.polymer.idealizedStructure);
                    } else {
                        _.set(scope.parent, 'structure', scope.parent.polymer.displayStructure);
                    }
                } else {
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
                    var template = angular.element(html);
                    element.append(template);
                    $compile(template)(scope);
                });

            }
        };
    });

    ginasForms.directive('subunitForm', function ($compile, $templateRequest, $uibModal) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'formController',
            scope: {
                parent: '=',
                view: '@',
                selected: '=',
                initcollapsed: '@iscollapsed'
            },
            templateUrl: baseurl + "assets/templates/forms/subunit-form.html",
            link: function (scope, element, attrs) {
                scope.substanceClass = scope.parent.$$getClass();
                scope.numbers = true;
                scope.viewchange = function(){
                    if(!scope.numbers){
                        console.log("fasta view");
                    }else{
                        console.log("number view");
                    }
                }
                scope.addNewSubunit = function (form) {
                    var r = scope.substanceClass + '.subunits';
                    scope.addNew(form, r);
                };
                if(scope.initcollapsed){
                        scope.iscollapsed=false;
                }

                scope.validate = function () {
                    if (scope.subunit.sequence.length > 4000) {
                        scope.open();
                    }
                    scope.subunit.subunitIndex = scope.parent[scope.parent.substanceClass].subunits.length + 1;
                    scope.parent[scope.parent.substanceClass].subunits.push(scope.subunit);
                    scope.subunit = {};
                    scope.subunitForm.$setPristine();
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
                /*$templateRequest(baseurl + "assets/templates/forms/subunit-form.html").then(function (html) {
                 var template = angular.element(html);
                 element.append(template);
                 $compile(template)(scope);
                 });*/
            }
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

    ginasForms.directive('cvForm', function ($compile, $uibModal, CVFields, spinnerService) {
        return {
            restrict: 'E',
            replace: true,
           controller: 'cvFormController',
            templateUrl: baseurl + "assets/templates/admin/cv-form.html",
            link: function (scope, element, attrs, ngModel) {
        //        spinnerService.show('cvSpinner');

                scope.vocabTermChange = function(model, obj){
                    var temp ={};
                    switch(model.value){
                        case "ix.ginas.models.v1.CodeSystemControlledVocabulary":
                            temp = {
                                systemCategory:'',
                                regex:''
                            };
                            break;
                        case "ix.ginas.models.v1.FragmentControlledVocabulary":
                            temp = {
                                fragmentStructure:'',
                                simplifiedStructure:''
                            };
                            break;


                    }
                    _.forEach(obj.terms, function(vocabTerm){
                        vocabTerm = _.merge(vocabTerm, temp);
                        return vocabTerm;
                    });
                    obj.$$changed = true;
                };
               
            }
        };
    });

    ginasForms.directive('cvTermsForm', function (CVFields) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: baseurl + "assets/templates/admin/cv-terms-form.html",
            link: function (scope) {
               /* scope.getCV = function (domain) {
                    if (domain == "CODE_SYSTEM") {
                        return "CODE_SYSTEM_TYPE";
                    } else if (domain == "DOCUMENT_TYPE") {
                        return "";
                    } else {
                        return null;
                    }
                };*/
            }
        };
    });

    ginasForms.directive('editCvForm', function ($templateRequest, CVFields, $uibModal) {
        return {
            restrict: 'E',
            replace: true,
            scope:{
                cv: '='
            },
          //  controller: 'cvFormController',
            templateUrl: baseurl + "assets/templates/admin/edit-cv-form.html",
            link: function (scope) {
                var modalInstance;
                var temp;

                scope.addNewTerm = function () {
                    scope.referenceobj.terms.push({});
                };

                scope.close = function () {
                    scope.opened = false;
                    scope.update(scope.referenceobj, scope.index);
                    modalInstance.close();
                };

                scope.cancel = function () {
                    scope.opened = false;
                    scope.load();
                    modalInstance.close();
                };

                scope.open = function (obj, index) {
                    temp = angular.copy(obj);
                    scope.referenceobj = obj;
                    scope.index = index;
                     modalInstance = $uibModal.open({
                        templateUrl: baseurl + "assets/templates/modals/cv-terms-modal.html",
                        size: 'xl',
                        scope: scope
                    });
                    //this handles clicking outside of the modal to close it
                    modalInstance.result.then(function(){
                    }, function(){
                        scope.cancel();
                    });

                };


                scope.load = function() {
                    CVFields.all(false).then(function (response) {
                               _.forEach(response.data.content, function (domain) {
                         CVFields.search("VOCAB_TYPE", domain.vocabularyTermType).then(function (response) {
                         domain.vocabularyTermType = response[0];
                         })
                         });
                        _.set(scope, 'cv', response.data.content);
                        scope.count = scope.cv.length;
                        //   spinnerService.hideAll();
                    });

                };
                scope.flattenFields = function (fields) {
                    _.forEach(fields, function (value, key) {
                        if (!value.value && value.display) {
                            fields[key] = value.display;
                        } else if (!value.value && !value.display) {
                            fields[key] = value;
                        } else {
                            fields[key] = value.value;
                        }
                    });
                    return fields;
                };

                scope.fieldChange = function (obj) {
                    obj.$$changed = true;
                };



                scope.update = function (domain, index) {
                    if (!domain.terms) {
                        _.set(domain, 'terms', []);
                    }
                    domain = scope.flattenFields(domain);
                    domain.fields = scope.flattenFields(domain.fields);

                    CVFields.updateCV(domain).then(function (response) {
                        domain.$$unlocked = false;
                        CVFields.search("VOCAB_TYPE", response.vocabularyTermType).then(function (response) {
                            domain.vocabularyTermType = response[0];
                        })
                    });
                };

                //the delete button emits the delete object, which is used here to remove the reference form all arrays that use it
                scope.$on('delete', function (e) {
                    scope.referenceobj.terms.splice(scope.referenceobj.terms.indexOf(e.targetScope.obj), 1);


                });

            }
        };
    });

    ginasForms.directive('loadCvForm', function (FileReader, CVFields, $http) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'cvFormController',
            templateUrl: baseurl + "assets/templates/admin/load-cv-form.html",
            link: function (scope, element, attrs) {

                scope.submitFile = function () {
                    var fd = new FormData();
                    fd.append('file-name', scope.cvFile);
                    fd.append('file-type', scope.cvFile.type);
                    //fd.append('file', scope.cvFile);
                    //send the file / data to your server
                    $http.post(baseurl + 'cv/upload', fd, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    }).success(function (data) {
                    });
                };


                //            scope.cv = {};

                scope.getPos = function (item, arr) {
                    if (scope.cv.domains.indexOf(item) == -1
                        && scope.cv.value[arr.indexOf(item)] == true) {
                        scope.cv.domains.push(item)
                    }
                    if (scope.cv.domains.indexOf(item) != -1
                        && !scope.cv.value[arr.indexOf(item)]) {
                        scope.cv.domains.splice(scope.cv.domains.indexOf(item), 1)
                    }
                    return arr.indexOf(item);
                };


                scope.loadCVFile = function (file) {
                    var tempValues = [];
                    scope.values = [];
                    FileReader.readAsText(file, scope).then(function (response) {
                        scope.data = angular.fromJson(response);
                        for (var i = 0; i < 10; i++) {
                            var keys = _.keys(scope.data.substanceNames[i]);
                            tempValues = _.union(keys, tempValues);
                        }
                        _.forEach(tempValues, function (v) {
                            scope.values.push({display: v});
                        });
                        //  scope.values = tempValues;
                    });
                };

                scope.makeCV = function () {
                    var controlledVocab = {
                        domain: scope.cv.domainName,
                        terms: []
                    };
                    var tempTerms = [];
                    _.forEach(scope.data.substanceNames, function (term) {
                        var t = {
                            value: term[scope.cv.domain.display],
                            display: term[scope.cv.domain.display],
                            filters: []
                        };
                        if (scope.cv.dependencyField) {
                            var filter = scope.cv.dependencyField.display + '=' + term[scope.cv.dependencyField.display];
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

    ginasForms.directive('saveCvForm', function ($compile, download) {
        return {
            restrict: 'E',
            replace: true,
            controller: 'cvFormController',
            templateUrl: baseurl + "assets/templates/admin/save-cv-form.html",
            link: function (scope, element, attrs) {


                scope.downloadCV = function () {
                    var json = JSON.stringify(scope.cv);
                    var b = new Blob([json], {type: "application/json"});
                    scope.url = URL.createObjectURL(b);
                    var download = angular.element(
                        '<a class="btn btn-primary" download="results.json"' +
                        'href="' + scope.url + '" target = "_self" id ="download">' +
                        '<i class="fa fa-download" uib-tooltip="Download Page Results"></i>' +
                        '</a>');
                    download[0].click();
                }

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

                    $templateRequest(baseurl + "assets/templates/forms/site-string-form.html").then(function (html) {
                        template = angular.element(html);
                        element.append(template);
                        $compile(template)(scope);

                    });


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
                    if(!scope.referenceobj[scope.field]){
                        scope.referenceobj[scope.field]=[];
                    }        
                    var temp = angular.copy(scope.referenceobj[scope.field].$$displayString);
                    _.set(scope.referenceobj, scope.field, siteList.siteList(scope.referenceobj[scope.field].$$displayString));
                    //why is this necessary?
                    scope.referenceobj[scope.field].$$displayString = temp;
                    scope.$emit("changed");
                };

                scope.redraw = function () {
                    if(!scope.referenceobj[scope.field]){
                        scope.referenceobj[scope.field]=[];
                    }
                    scope.referenceobj[scope.field].$$displayString = siteList.siteString(scope.referenceobj[scope.field]);
                };

                scope.deleteObj = function (obj) {
                    scope.referenceobj.splice(scope.referenceobj.indexOf(obj), 1);
                };
                scope.toggleSiteInclusion = function(site){
                    scope.makeSiteList();
                    var sites = scope.referenceobj[scope.field];
                    var alreadyIndex=-1;
                    _.forEach(sites, function(osite, index){
                        if(site.subunitIndex===osite.subunitIndex && site.residueIndex=== osite.residueIndex){
                                alreadyIndex=index;
                        }
                    });
                    if(alreadyIndex>=0){
                        sites.splice(alreadyIndex,1);
                    }else{
                        sites.push({subunitIndex:site.subunitIndex,residueIndex:site.residueIndex});
                    }
                    
                    scope.redraw();
                    scope.makeSiteList();
                };

                scope.$on('selected', function (e,a){
                    scope.toggleSiteInclusion(a);
                });

                //If there is no stored shorthand (handled by $$displayString)
                //then it needs to be generated. This call will do that:
                if(!scope.referenceobj[scope.field] || scope.referenceobj[scope.field].$$displayString){
                        scope.redraw();
                }
            }
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
