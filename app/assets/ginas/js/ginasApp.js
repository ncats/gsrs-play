
(function () {
    var ginasApp = angular.module('ginas', ['ngMessages', 'ngResource', 'ui.bootstrap', 'ui.bootstrap.showErrors',
        'ui.bootstrap.datetimepicker', 'LocalStorageModule', 'ngTagsInput', 'xeditable', 'ui.select'
    ])
        .config(function (showErrorsConfigProvider, localStorageServiceProvider, $locationProvider) {
            showErrorsConfigProvider.showSuccess(true);
            localStorageServiceProvider
                .setPrefix('ginas');
            $locationProvider.html5Mode({
                enabled: true,
                hashPrefix: '!'
            });
        });

    ginasApp.filter('range', function () {
        return function (input, min, max) {
            console.log(input);
            min = parseInt(min); //Make string input int
            max = parseInt(max);
            for (var i = min; i < max; i++)
                input.push(i);
            return input;
        };
    });

    ginasApp.filter('arrays', function () {
        return function (input) {
            var obj = {};
            _.forIn(input, function (value, key) {
                if (_.isArray(value)) {
                    _.set(obj, key, value);
                }
            });
            return obj;
        };
    });

  

    ginasApp.factory('Substance', function () {
        var Substance = {};
        var substanceClass = window.location.search.split('=')[1];
        switch (substanceClass) {
            case "chemical":
                Substance.substanceClass = substanceClass;
                Substance.structure = {};
                Substance.moieties = [];
                break;
            case "protein":
                Substance.substanceClass = substanceClass;
                Substance.protein = {};
                var subunit = "";
                break;
            case "structurallyDiverse":
                Substance.substanceClass = substanceClass;
                Substance.structurallyDiverse = {};
                break;
            case "nucleicAcid":
                Substance.substanceClass = substanceClass;
                Substance.nucleicAcid = {};
                break;
            case "mixture":
                Substance.substanceClass = substanceClass;
                Substance.mixture = {};
                break;
            case "polymer":
                Substance.substanceClass = substanceClass;
                Substance.polymer = {};
                break;
            default:
                Substance.substanceClass = substanceClass;
//                Substance.polymer = {};
                console.log('invalid substance class');
                break;
        }

        return Substance;
    });
    
    ginasApp.factory('polymerUtils', function () {
        var  utils={};
        utils.getAttachmentMapUnits = function (srus){
                var rmap = {};
                for(var i in srus){
                        var lab =srus[i].label;
                        if(!lab){
                                lab= "{" + i + "}";
                        }
                        for(var k in srus[i].attachmentMap){
                                if(srus[i].attachmentMap.hasOwnProperty(k)){
                                        rmap[k]=lab;
                                }
                        }
                }
                return rmap;       
        };
        utils.sruConnectivityToDisplay = function (amap,rmap){
                var disp="";
                for(var k in amap){
                  if(amap.hasOwnProperty(k)){
                        var start=rmap[k] + "_" + k;
                        for(var i in amap[k]){
                                var end = rmap[amap[k][i]] + "_" + amap[k][i];
                                disp+=start + "-" +end + ";";
                        }
                  }
                }
                return disp;
        };
        utils.setSRUConnectivityDisplay = function (srus){
               var rmap=utils.getAttachmentMapUnits(srus);
                for(var i in srus){
                        var disp = utils.sruConnectivityToDisplay(srus[i].attachmentMap,rmap);
                        srus[i]._displayConnectivity=disp;
                }
        };

        return utils;
    });


    ginasApp.factory('lookup', function () {
        var lookup = {
            "names.type": "NAME_TYPE",
            "names.nameOrgs": "NAME_ORG",
            "names.nameJurisdiction": "JURISDICTION",
            "names.domains": "NAME_DOMAIN",
            "names.languages": "LANGUAGE",
            "codes.system": "CODE_SYSTEM",
            "codes.type": "CODE_TYPE",
            "relationships.type": "RELATIONSHIP_TYPE",
            "relationships.interactionType": "INTERACTION_TYPE",
            "relationships.qualification": "QUALIFICATION",
            "references.docType": "DOCUMENT_TYPE"
        };

        lookup.getFromName = function (field, val) {
            var domain = lookup[field];
            if (typeof domain !== "undefined") {
                return {
                    "display": getDisplayFromCV(domain, val),
                    "value": val,
                    "domain": domain
                };
            }
            return null;
        };
        lookup.expandCVValueDisplay = function(domain,value){
                var disp=getDisplayFromCV(domain,value);
                return {value:value, display:disp, domain:domain};
        };
        return lookup;
    });

    ginasApp.controller("GinasController", function ($scope, $resource, $parse, $location, $modal, $http, $window, $anchorScroll, localStorageService, Substance, data, substanceSearch, substanceIDRetriever, lookup) {

        var ginasCtrl = this;

        $scope.select = ['Substructure', 'Similarity'];
        $scope.type = 'Substructure';
        $scope.cutoff = 0.8;
        $scope.addAmount = false;

        $scope.range = function (min) {
            var input = [];
            for (var i = 1; i <= min; i++) input.push(i);
            return input;
        };


        $scope.openSelector = function (path) {
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: baseurl + 'assets/ginas/templates/substanceSelector.html',
                controller: 'SubstanceSelectorInstanceController',
                size: 'lg'

            });

            modalInstance.result.then(function (selectedItem) {
                var subref = {};
                subref.refuuid = selectedItem.uuid;
                subref.refPname = selectedItem.name;
                subref.approvalID = selectedItem.approvalID;
                subref.substanceClass = "reference";
                _.set($scope, path, subref);
            });
        };

        $scope.toFormSubstance = function (apiSub) {

            //first, flatten nameorgs, this is technically destructive
            //needs to be fixed.
            for (var i in apiSub.names) {
                if (typeof apiSub.names[i].nameOrgs != "undefined") {
                    for (var j in apiSub.names[i].nameOrgs) {
                        if (apiSub.names[i].nameOrgs[j].deprecated) {
                            apiSub.destructive = true;
                        }
                        apiSub.names[i].nameOrgs[j] = apiSub.names[i].nameOrgs[j].nameOrg;
                    }
                }
            }


            console.log($scope);
            apiSub = $scope.expandCV(apiSub, "");
            apiSub = $scope.splitNames(apiSub);

            var references = {};
            for (var v in apiSub.references) {
                references[apiSub.references[v].uuid] = apiSub.references[v];
                apiSub.references[v].id = v - 1 + 2;
            }
            apiSub = $scope.expandReferences(apiSub, references, 0);


            return apiSub;
        };

        $scope.fromFormSubstance = function (formSub) {

            if (formSub.officialNames || formSub.unofficialNames) {
                for (var n in formSub.officialNames) {
                    var name = formSub.officialNames[n];
                    name.type = "of";
                }
                formSub.names = formSub.officialNames.concat(formSub.unofficialNames);
                delete formSub.officialNames;
                delete formSub.unofficialNames;
            }
            if (formSub.q) {
                delete formSub.q;
            }
            if (formSub.subref) {
                delete formSub.subref;
            }

            formSub = $scope.flattenCV(formSub);
            formSub = $scope.collapseReferences(formSub, 0);
            return formSub;
        };

        //date picker
        $scope.open = function ($event) {
            $scope.status.opened = true;
        };

        $scope.status = {
            opened: false
        };

        //datepicker//

        //adds reference id//
        $scope.refLength = function () {
            if (!$scope.substance.references) {
                return 1;
            }
            return $scope.substance.references.length + 1;
        };
        //add reference id//

        //populates tag fields
        $scope.loadItems = function (field, $query) {
            data.load(field);
            return data.search(field, $query);
        };
        //populates tag fields//

        $scope.retrieveItems = function (field, $query) {
            data.load(field);
            return data.lookup(field, $query);
        };

        $scope.test = function (field) {


            data.load(field);
            return data.retrieve();
            // $scope.selectOptions = data.content[0].terms;
        };

        $scope.scrollTo = function (prmElementToScrollTo) {
            $location.hash(prmElementToScrollTo);
            $anchorScroll();
        };

        //local storage functions//
        $scope.unbind = localStorageService.bind($scope, 'enabled');
        this.enabled = function getItem(key) {
            return localStorageService.get('enabled') || false;
        };
        this.numbers = true;
        localStorageService.set('enabled', $scope.enabled);

        //passes structure id from chemlist search to structure search//
        $scope.passStructure = function (id) {
            localStorageService.set('structureid', id);
        };
        $scope.clearStructure = function () {
            localStorageService.remove('structureid');
        };
        ///


        $scope.proteinDetails = function (obj, form) {
            $scope.$broadcast('show-errors-check-validity');
            console.log(obj);
            if (form.$valid) {
                this.substance.protein.proteinType = obj.proteinType;
                this.substance.protein.proteinSubType = obj.proteinSubType;
                this.substance.protein.sequenceOrigin = obj.sequenceOrigin;
                this.substance.protein.sequenceType = obj.sequenceType;
                $scope.detailsSet = true;
            }
            $scope.$broadcast('show-errors-reset');
        };

        $scope.setLinkProperty = function (site, bridge, property) {
            _.set(site, property, true);
            site.bridge = bridge;
        };

        $scope.findSite = function (array, index) {
            var returnSite = {};
            _.forEach(array, function (n) {
                var temp = (_.find(n, 'residueIndex', index));
                if (typeof temp !== "undefined") {
                    returnSite = temp;
                }
            });
            return returnSite;
        };

        $scope.makeSite = function (obj) {
            var site = {};
            site.subunitIndex = obj.subunitIndex - 1 + 1;
            site.residueIndex = obj.residueIndex - 1 + 1;
            return site;
        };


        $scope.parseLink = function (obj, path) {
            var link = [];

            //PERSIST--add sites to link list
            var site = $scope.makeSite(obj);
            link.push(site);

            //DISPLAY--make array for bridge site
            var bridge = [obj.subunitIndexEnd, obj.residueIndexEnd];

            //DISPLAY-- find site, add link property and bridge
            var subunit = (_.pick($scope.substance.protein.subunits, 'index', obj.subunitIndex - 1)[0]);
            tempSite = $scope.findSite(subunit.display, obj.residueIndex - 1 + 1);
            $scope.setLinkProperty(tempSite, bridge);

            //DO IT AGAIN for ending index. can't really be done in a loop, can make and end site object and abstract these though
            var endSite = $scope.makeSite({
                'subunitIndex': obj.subunitIndexEnd - 1 + 1,
                'residueIndex': obj.residueIndexEnd - 1 + 1
            });

            link.push(endSite);
            var endBridge = [obj.subunitIndex, obj.residueIndex];
            subunit = (_.pick($scope.substance.protein.subunits, 'index', obj.subunitIndexEnd - 1)[0]);
            var tempEndSite = $scope.findSite(subunit.display, obj.residueIndexEnd - 1 + 1);

            //switch property if other link
            var prop = "disulfide";
            if (_.has(obj, "linkageType")) {
                prop = "otherLink";
            }

            //set display properties
            $scope.setLinkProperty(tempSite, bridge, prop);
            $scope.setLinkProperty(tempEndSite, endBridge, prop);

            return link;
        };


        $scope.parseGlycosylation = function (obj, path) {
            /*            if (!$scope.substance.protein.glycosylation.count) {
             $scope.substance.protein.glycosylation.count = 0;
             }*/
            var link = obj.link;
            var site = $scope.makeSite(obj);
            site.link = link;
            path = path + "." + link + "GlycosylationSites";
            console.log(path);

            //  $scope.substance.protein.glycosylation.count++;
            //DISPLAY-- find site, add glycosylation property
            var subunit = (_.pick($scope.substance.protein.subunits, 'index', obj.subunitIndex - 1)[0]);
            tempSite = $scope.findSite(subunit.display, obj.residueIndex - 1 + 1);
            tempSite.glycosylationSite = true;
            return site;
        };
        
        $scope.cleanSequence = function (s) {
            return s.replace(/[^A-Za-z]/g, '');
        };

        $scope.parseSubunit = function (sequence, subunit) {
            var split = sequence.replace(/[^A-Za-z]/g, '').split('');
            var display = [];
            var obj = {};
            var invalid = ['B', 'J', 'O', 'U', 'X', 'Z'];
            for (var i in split) {
                var aa = split[i];
                var valid = _.indexOf(invalid, aa.toUpperCase());
                if (valid >= 0) {
                    obj.value = aa;
                    obj.valid = false;
                    obj.subunitIndex = subunit;
                    obj.residueIndex = i - 1 + 2;
                    display.push(obj);
                    obj = {};
                } else {
                    obj.value = aa;
                    obj.valid = true;
                    obj.name = $scope.findName(aa);
                    if($scope.substance.protein){
                        obj.type = $scope.getType(aa);
                    }
                    obj.subunitIndex = subunit;
                    obj.residueIndex = i - 1 + 2;
                    if (aa.toUpperCase() == 'C') {
                        obj.cysteine = true;
                    }
                    display.push(obj);
                    obj = {};
                }
            }
            this.display = display;
            display = _.chunk(display, 10);
            return display;
        };

        $scope.findName = function (aa) {
            var ret;
            if($scope.substance.protein){
                ret=getDisplayFromCV("AMINO_ACID_RESIDUES",aa.toUpperCase());
            }else{
                ret=getDisplayFromCV("NUCLEIC_ACID_BASE",aa.toUpperCase());
            }
            /*
            switch (aa.toUpperCase()) {
                case 'A':
                    return "Alanine";
                case 'C':
                    return "Cysteine";
                case 'D':
                    return "Aspartic acid";
                case 'E':
                    return "Glutamic acid";
                case 'F':
                    return "Phenylalanine";
                case 'G':
                    return "Glycine";
                case 'H':
                    return "Histidine";
                case 'I':
                    return "Isoleucine";
                case 'K':
                    return "Lysine";
                case 'L':
                    return "Leucine";
                case 'M':
                    return "Methionine";
                case 'N':
                    return "Asparagine";
                case 'P':
                    return "Proline";
                case 'Q':
                    return "Glutamine";
                case 'R':
                    return "Arginine";
                case 'S':
                    return "Serine";
                case 'T':
                    return "Threonine";
                case 'V':
                    return "Valine";
                case 'W':
                    return "Tryptophan";
                case 'Y':
                    return "Tyrosine";

                default:
                    return "Tim forgot one";
            }
            */
            return ret;

        };

        $scope.getType = function (aa) {
            if (aa == aa.toLowerCase()) {
                return 'D';
            }
            else {
                return 'L';
            }
        };

        $scope.parseAgentModification = function (obj, path) {

        };

        $scope.defaultSave = function (obj, form, path, list, name) {
            $scope.$broadcast('show-errors-check-validity');
            if (form.$valid) {
                if (_.has($scope.substance, path)) {
                    if (!list) {
                        console.log(obj);
                        _.set($scope.substance, path, obj);
                        console.log($scope);
                    } else {
                        var temp = _.get($scope.substance, path);
                        temp.push(obj);
                        _.set($scope.substance, path, temp);
                    }
                } else {
                    if (!list) {
                        _.set($scope.substance, path, obj);
                    } else {
                        var x = [];
                        x.push(obj);
                        _.set($scope.substance, path, x);
                    }
                }
                $scope[name] = {};
                $scope.reset(form);
                form.$setSubmitted(true);
            }

        };


        $scope.siteDisplayListToSiteList= function (slist){
                var toks=slist.split(";");
                var sites=[];
                for(var i in toks){
                        var l=toks[i];
                        if(l === "")continue;
                        var rng=l.split("-");
                        if(rng.length>1){
                                var site1=$scope.siteDisplayToSite(rng[0]);
                                var site2=$scope.siteDisplayToSite(rng[1]);
                                if(site1.subunitIndex!=site2.subunitIndex){
                                       throw "\"" + rng + "\" is not a valid shorthand for a site range. Must be between the same subunits.";
                                }
                                if(site2.residueIndex<=site1.residueIndex){
                                       throw "\"" + rng + "\" is not a valid shorthand for a site range. Second residue index must be greater than first.";
                                }
                                sites.push(site1);
                                for(var j = site1.residueIndex+1; j<site2.residueIndex;j++){
                                        sites.push({
                                                subunitIndex:site1.subunitIndex,
                                                residueIndex:j
                                        });
                                }
                                sites.push(site2);
                        }else{
                                sites.push($scope.siteDisplayToSite(rng[0]));
                        }
                }
                return sites;
                      
        };

        $scope.addFields = function(obj, path){
            if(!_.has($scope.substance, path)){
                return obj;
            }
            var temp = _.get($scope.substance, [path]);
            console.log(temp);
            console.log(obj);
            _.forIn(obj, function(value, key){
                temp[key]=value;
                console.log(value, key);
            });
            console.log(temp);
            return temp;
        };


        //Method for pushing temporary objects into the final message
        //Note: This was changed to use a full path for type.
        //That means that passing something like "nucleicAcid.type"
        //for type, will store the form object into that path inside
        //the substance, unless otherwise caught in the switch.
        //This simplifies some things.
        $scope.validate = function (objName, form, path, list) {
            var obj = $scope[objName];
            console.log(obj);
            console.log(form);
            console.log($scope);
            var v = path.split(".");
            var type = _.last(v);
            console.log(type);
            var subClass = ($scope.substance.substanceClass);
            switch (type) {
                case "sugars":
                case "linkages":
                    //console.log($scope.checkSites(obj.displaySites,$scope.substance.nucleicAcid.subunits,obj));
                    //if(true)return "test";
                    $scope.updateSiteList(obj);
                    $scope.defaultSave(obj, form, path, list, objName);
                    break;
                case "subunits":
                    
                    if(!obj.subunitIndex){
                        var t=_.get($scope.substance,path);
                        if(t){
                                obj.subunitIndex=t.length+1;                                
                        }else{
                                obj.subunitIndex=1;
                        }
                    }else{
                    }
                    obj.display = $scope.parseSubunit(obj.sequence, obj.subunitIndex);
                    if(obj._editType !== "edit"){
                        $scope.defaultSave(obj, form, path, list, objName);
                    }
                    obj._editType="add";
                    break;
                case "protein":
/*                    $scope.proteinDetails(obj, form);*/
                    var prot = $scope.addFields(obj, path);
                    $scope.defaultSave(prot, form, path, list, objName);
                    break;
                case "disulfideLinks":
                    var d = $scope.parseLink(obj, path);
                    $scope.defaultSave(d, form, path, list, objName);
                    break;
                case "otherLinks":
                    var ol = {};
                    var otl = $scope.parseLink(obj, path);
                    _.set(ol, "sites", otl);
                    _.set(ol, "linkageType", obj.linkageType);
                    $scope.defaultSave(ol, form, path, list, objName);
                    break;
                case "glycosylation":
                    var g = $scope.parseGlycosylation(obj, path);
                    _.set($scope.substance, path + ".glycosylationType", obj.glycosylationType);
                    $scope.defaultSave(g, form, path + "." + obj.link + 'Glycosylation', list, objName);
                    break;
                case "structurallyDiverse":
                    var diverse = $scope.addFields(obj, path);
                    $scope.defaultSave(diverse, form, path, list, objName);

                    break;
                case "references":
                    _.set(obj, "uuid", uuid());
                    if (!$scope.substance[path]) {
                        _.set(obj, "id", 1);
                    } else {
                        _.set(obj, "id", $scope.substance.references.length + 1);
                    }
                    $scope.defaultSave(obj, form, path, list, objName);
                    break;
                 default:
                    if(obj._editType !== "edit"){
                        $scope.defaultSave(obj, form, path, list, objName);
                    }
                    obj._editType="add";
                    break;
            }
            $scope[objName] = {};
        };

        $scope.toggle = function (el) {
            if (el.selected) {
                el.selected = !el.selected;
            } else {
                el.selected = true;
            }
        };

        $scope.splitNames = function (sub) {
            var names = sub.names;
            var officialNames = [];
            var unofficialNames = [];
            if (names) {
                for (var n in names) {
                    var name = names[n];
                    if (name.type.value == "of") {
                        officialNames.push(name);
                    } else {
                        unofficialNames.push(name);
                    }
                    sub.unofficialNames = unofficialNames;
                    sub.officialNames = officialNames;
                    delete sub.names;
                }
            }
            return sub;

        };

        $scope.changeSelect = function (val) {
            console.log(val);
            val = !val;
            console.log(val);
        };

        $scope.remove = function (obj, field) {
            var index = Substance[field].indexOf(obj);
            Substance[field].splice(index, 1);
        };

        $scope.reset = function (form) {
            form.$setPristine();
            $scope.$broadcast('show-errors-reset');
        };

        $scope.selected = false;

        $scope.info = function (scope, element) {
            console.log($scope);
            console.log(scope);
            console.log(element);
            $scope.selected = !$scope.selected;
        };


        $scope.fetch = function ($query) {
            console.log($query);
            return substanceSearch.load($query);
            //return substanceSearch.search(field, $query);
        };
        

        $scope.expandCV = function (sub, path) {

            for (var v in sub) {

                var newpath = path;
                if (newpath.length >= 1) {
                    if (!angular.isArray(sub)) {
                        newpath += ".";
                    }
                }
                if (!angular.isArray(sub)) {
                    newpath = newpath + v;
                }
                var newcv = lookup.getFromName(newpath, sub[v]);
                if (angular.isArray(sub[v])) {
                    newcv = null;
                }

                if (newcv !== null) {
                    var w = getDisplayFromCV(newcv.domain, newcv.value);
                    newcv.display = w;
                    sub[v] = newcv;

                } else {
                    if (typeof sub[v] === "object") {
                        $scope.expandCV(sub[v], newpath);
                    }
                }
            }
            return sub;
        };

        $scope.expandReferences = function (sub, referenceMap, depth) {
            for (var v in sub) {
                if (depth > 0) {
                    if (v === "references") {
                        for (var r in sub[v]) {
                            sub[v][r] = referenceMap[sub[v][r]];
                        }
                    }
                }
                if (typeof sub[v] === "object") {
                    $scope.expandReferences(sub[v], referenceMap, depth + 1);
                }
            }
            return sub;
        };

        $scope.collapseReferences = function (sub, depth) {
            for (var v in sub) {
                if (depth > 0) {
                    if (v === "references") {
                        for (var r in sub[v]) {

                            sub[v][r] = sub[v][r].uuid;
                        }
                    }
                }
                if (typeof sub[v] === "object") {
                    $scope.collapseReferences(sub[v], depth + 1);
                }
            }
            return sub;
        };


        $scope.flattenCV = function (sub) {
            for (var v in sub) {
                if ($scope.isCV(sub[v])) {
                    sub[v] = sub[v].value;
                } else {
                    if (typeof sub[v] === "object") {
                        $scope.flattenCV(sub[v]);
                    }
                }
            }
            return sub;
        };

        $scope.isCV = function (ob) {
            if (typeof ob !== "object") return false;
            if (ob === null) return false;
            if (typeof ob.value !== "undefined") {
                if (typeof ob.display !== "undefined") {
                    return true;
                }
            }
            return false;
        };

        $scope.submitSubstance = function () {
            var sub = angular.copy($scope.substance);
            sub = $scope.fromFormSubstance(sub);
            $http.post(baseurl + 'submit', sub).success(function () {
                console.log("success");
                alert("submitted!");
            });
        };

        $scope.validateSubstance = function () {
            var sub = angular.copy($scope.substance);
            // console.log(angular.copy(sub));
            sub = $scope.fromFormSubstance(sub);
            //   console.log(sub);
            $http.post(baseurl + 'register/validate', sub).success(function (response) {
                var arr = [];
                for (var i in response) {
                    if (response[i].messageType != "INFO")
                        arr.push(response[i]);
                    if (response[i].messageType == "WARNING")
                        response[i].class = "alert-warning";
                    if (response[i].messageType == "ERROR")
                        response[i].class = "alert-danger";
                }
                $scope.errorsArray = arr;
            });
        };

        $scope.getSiteResidue = function(subunits, site){
                var si=site.subunitIndex;      
                var ri=site.residueIndex;      
                for(var i=0;i<subunits.length;i++){
                     if(subunits[i].subunitIndex === si){
                        var res=subunits[i].sequence.substr(ri-1,1);
                        return res;
                     }   
                }
                return "";
        };
        
         $scope.siteDisplayToSite= function (site){
                var subres=site.split("_");

                if(site.match(/^[0-9][0-9]*_[0-9][0-9]*$/g)===null){
                        throw "\"" + site + "\" is not a valid shorthand for a site. Must be of form \"{subunit}_{residue}\"";
                }

                return {
                        subunitIndex:subres[0]-0,
                        residueIndex:subres[1]-0
                };
        };
       $scope.sitesToDislaySites= function (sites){
                sites.sort(function (site1,site2){
                        var d=site1.subunitIndex - site2.subunitIndex;
                        if(d===0){
                                d= site1.residueIndex-site2.residueIndex;
                        }
                        return d;
                
                });
                var csub=0;
                var cres=0;
                var rres=0;
                var finish=false;
                var disp="";
                for(var i=0;i<sites.length;i++){
                        
                        var site=sites[i];
                        if(site.subunitIndex==csub && site.residueIndex == cres)
                                continue;
                        finish=false;
                        if(site.subunitIndex == csub){
                                if(site.residueIndex == cres+1){
                                        if(rres===0){
                                                rres=cres;
                                        }
                                }else{
                                        finish=true;                                               
                                }
                        }else{
                                finish=true;
                        }
                        if(finish && csub!==0){
                            if(rres!==0){
                                disp+= csub + "_" + rres +"-" + csub + "_" + cres + ";";
                            }else{
                                disp+=csub + "_" + cres +";";
                            }
                            rres=0;
                        }
                        csub=site.subunitIndex;
                        cres=site.residueIndex;                        
                }
                if(sites.length>0){
                        if(rres!==0){
                                disp+= csub + "_" + rres +"-" + csub + "_" + cres;        
                        }else{
                                disp+=csub + "_" + cres;
                        }
                }
                return disp;
        };
        $scope.getAllSitesDisplay = function(link){
                var sites="";
                if(!$scope.substance.nucleicAcid)return "";
                for(var i in $scope.substance.nucleicAcid.subunits){
                        var subunit=$scope.substance.nucleicAcid.subunits[i];
                        if(sites !== ""){
                                sites+=";";
                        }
                        if(link){
                                sites+=subunit.subunitIndex +"_1-" +subunit.subunitIndex + "_" + (subunit.sequence.length-1);
                        }else{
                                sites+=subunit.subunitIndex +"_1-" +subunit.subunitIndex + "_" + subunit.sequence.length;                        
                        }
                }
                return sites;
                
        };
        
        $scope.getAllSites = function(link){
                return $scope.siteDisplayListToSiteList($scope.getAllSitesDisplay(link));                
        };
        
        $scope.getAllLeftoverSitesDisplay = function(link){
                if(link){
                        return $scope.sitesToDislaySites($scope.getAllSitesWithoutLinkage());
                }else{
                        return $scope.sitesToDislaySites($scope.getAllSitesWithoutSugar());
                }
        };
        
        $scope.getAllSitesWithoutSugar = function(){
               var retsites=[];
               
               if($scope.substance.nucleicAcid && $scope.substance.nucleicAcid.sugars){
                                
                       var asites = [];
                       for(var i=0;i<$scope.substance.nucleicAcid.sugars.length;i++){
                                asites=asites.concat($scope.substance.nucleicAcid.sugars[i].sites);
                       }
                       var allsites=$scope.getAllSites();
                       var asitesmap = {};
                       var site;
                       for(var s in asites){

                                site=asites[s];
                                asitesmap[site.subunitIndex + "_" + site.residueIndex]=true;
                       }
                       for(s in allsites){
                                site=allsites[s];
                                if(!asitesmap[site.subunitIndex + "_" + site.residueIndex]){
                                        retsites.push(site);
                                }
                       }
               }else{
                      retsites= $scope.getAllSites();
               }
               return retsites;
               
               
        };
        $scope.getAllSitesWithoutLinkage = function(){
               var retsites=[];
               
               if($scope.substance.nucleicAcid && $scope.substance.nucleicAcid.linkages){
                                
                       var asites = [];
                       for(var i=0;i<$scope.substance.nucleicAcid.linkages.length;i++){
                                asites=asites.concat($scope.substance.nucleicAcid.linkages[i].sites);
                       }
                       var allsites=$scope.getAllSites(true);
                       var asitesmap = {};
                       var site;
                       for(var s in asites){

                                site=asites[s];
                                asitesmap[site.subunitIndex + "_" + site.residueIndex]=true;
                       }
                       for(s in allsites){
                                site=allsites[s];
                                if(!asitesmap[site.subunitIndex + "_" + site.residueIndex]){
                                        retsites.push(site);
                                }
                       }
               }else{
                      retsites= $scope.getAllSites(true);
               }
               return retsites;
               
               
        };
        
        $scope.isSiteSugarSpecified = function(site){
        
        
        
        };
        
        $scope.getSiteDuplicates = function(sites1,sites2){
                       var retsites=[];
                       var asitesmap = {};
                       var site;
                       for(var s in sites1){
                                site=sites1[s];
                                asitesmap[site.subunitIndex + "_" + site.residueIndex]=true;
                       }
                       for(s in sites2){
                                site=sites2[s];
                                if(asitesmap[site.subunitIndex + "_" + site.residueIndex]){
                                        retsites.push(site);
                                }
                       }
                       return retsites;
        }; 
        
        $scope.getAllSugarSitesExcept = function(sugar){
                       var asites = [];
                       if($scope.substance.nucleicAcid.sugars)
                               for(var i=0;i<$scope.substance.nucleicAcid.sugars.length;i++){
                                       if($scope.substance.nucleicAcid.sugars[i] != sugar){
                                                asites=asites.concat($scope.substance.nucleicAcid.sugars[i].sites);
                                       }
                               }
                       return asites;
        };
        
        $scope.getAllLinkageSitesExcept = function(linkage){
                       var asites = [];
                       if($scope.substance.nucleicAcid.linkages)
                               for(var i=0;i<$scope.substance.nucleicAcid.linkages.length;i++){
                                       if($scope.substance.nucleicAcid.linkages[i] != linkage){
                                                asites=asites.concat($scope.substance.nucleicAcid.linkages[i].sites);
                                       }
                               }
                       return asites;
        };
        
        $scope.removeItem = function(list, item){
                _.remove(list,function(someItem) {
                        return item === someItem;                 
                 });
        };
        $scope.setEditSubunit = function(sub){
                        if(sub){
                                $scope.subunit=sub;
                                $scope.subunit._editType="edit";
                        }else{
                                $scope.subunit=null;
                        }
                        
        };
        $scope.setEditMixtureComponent = function(mix){
                        if(mix){
                                $scope.mcomponent=mix;
                                $scope.mcomponent._editType="edit";
                        }else{
                                $scope.mcomponent=null;
                        }
        };
        $scope.setEditMonomer = function(mon){
                        if(mon){
                                $scope.component=mon;
                                $scope.component._editType="edit";
                        }else{
                                $scope.component=null;
                        }
                        
        };
        $scope.setEditSRU = function(sru){
                        if(sru){
                                $scope.srucomponent=sru;
                                $scope.srucomponent._editType="edit";
                        }else{
                                $scope.srucomponent=null;
                        }
                        
        };
        
        $scope.checkSites = function(dispSites, subunits, link) {
                try{

                        var sites=$scope.siteDisplayListToSiteList(dispSites);
                        var dsites;
                        
                        if(!link.linkage){
                                dsites=$scope.getSiteDuplicates($scope.getAllSugarSitesExcept(link),sites);
                        }else{
                                dsites=$scope.getSiteDuplicates($scope.getAllLinkageSitesExcept(link),sites);
                        }
                        if(dsites.length>0){
                                throw "Site(s) " + $scope.sitesToDislaySites(dsites) + " already specified!";
                        }
                        
                        
                        for(var s in sites){
                                var site = sites[s];
                                if(link.linkage){
                                        var sited = {
                                                subunitIndex:site.subunitIndex,
                                                residueIndex:site.residueIndex+1
                                        };
                                        site=sited;
                                }
                                if($scope.getSiteResidue(subunits,site) === ""){
                                        throw "Site " + sites[s].subunitIndex + "_" +sites[s].residueIndex + " does not exist in subunits";
                                }
                        }
                }catch(e){
                        return e;
                }
        };
        
        $scope.updateSiteList = function(obj) {
                try{
                        obj.sites=$scope.siteDisplayListToSiteList(obj.displaySites);
                        obj.displaySites = $scope.sitesToDislaySites(obj.sites);
                        obj.sites=$scope.siteDisplayListToSiteList(obj.displaySites);
                }catch(e){
                        return e;
                }
                
        };

        $scope.submitpaster = function (input) {
            console.log(input);
            var sub = JSON.parse(input);
            $scope.substance = sub;
            console.log($scope);
        };

        $scope.bugSubmit = function (bugForm) {
            console.log(bugForm);
        };

        $scope.setEditId = function (editid) {
            localStorageService.set('editID', editid);
        };

        if (typeof $window.loadjson !== "undefined" &&
            JSON.stringify($window.loadjson) !== "{}") {
            var sub = $scope.toFormSubstance($window.loadjson);
            $scope.substance = sub;
        } else {
            var edit = localStorageService.get('editID');
            if (edit) {
                localStorageService.remove('structureid');
                substanceIDRetriever.getSubstances(edit).then(function (data) {
                    var sub = $scope.toFormSubstance(data);
                    $scope.substance = sub;
                    localStorageService.remove('editID');
                });
            } else {
                $scope.substance = Substance;
            }
        }

        $scope.print = function (event) {
            console.log(event);
            console.log(event.currentTarget);
        };
    });

    var uuid = function uuid() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }

        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    };
     ginasApp.directive('loading', function ($http) {
        return {
            template: "<div class=\"sk-folding-cube\">\n" + 
		"  <div class=\"sk-cube1 sk-cube\"></div>\n" + 
		"  <div class=\"sk-cube2 sk-cube\"></div>\n" + 
		"  <div class=\"sk-cube4 sk-cube\"></div>\n" + 
		"  <div class=\"sk-cube3 sk-cube\"></div>\n" + 
		"</div>"
        };
    });
    
    

//sugarSites
    ginasApp.directive('naSites', function() {
          return {
            require: 'ngModel',
            link: function(scope, ele, attrs, c) {
              scope.$watch(attrs.ngModel, function() {
                if(attrs.naSites.length<2)return;
                var repObj = JSON.parse(attrs.naSites);
                
                
                var ret = scope.checkSites(c.$modelValue,scope.substance.nucleicAcid.subunits,repObj);
                
                if(ret){
                        c.$setValidity('siteInvalid', false);                        
                }else{
                        c.$setValidity('siteInvalid', true);                        
                }                
                
                //hack to have dynamic messages
                if(!c.$errorMsg)c.$errorMsg={};
                if(c.$modelValue.length<1){
                        c.$errorMsg.naSites="";                
                }else{
                        c.$errorMsg.naSites=ret;
                }
                
                return ret;
                
              });
            }
          };
    });
    
    ginasApp.directive('scrollSpy', function ($timeout) {
        return function (scope, elem, attr) {
            scope.$watch(attr.scrollSpy, function (value) {
                $timeout(function () {
                    elem.scrollspy('refresh');
                }, 200);
            }, true);
        };
    });

    ginasApp.directive('duplicate', function (isDuplicate) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
                ngModel.$asyncValidators.duplicate = isDuplicate;
            }
        };
    });

    ginasApp.factory('isDuplicate', function ($q, substanceFactory) {
        return function dupCheck(modelValue) {
            var deferred = $q.defer();
            substanceFactory.getSubstances(modelValue)
                .success(function (response) {
                    console.log(response);
                    if (response.count >= 1) {
                        deferred.reject();
                    } else {
                        deferred.resolve();
                    }
                });
            return deferred.promise;
        };
    });

    ginasApp.factory('substanceFactory', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances?filter=names.name='";
        var substanceFactory = {};
        substanceFactory.getSubstances = function (name) {
            return $http.get(url + name.toUpperCase() + "'", {
                headers: {
                    'Content-Type': 'text/plain'
                }
            });
        };
        return substanceFactory;
    }]);
    

    ginasApp.service('substanceIDRetriever', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances(";
        var substanceIDRet = {
            getSubstances: function (editId) {
                console.log(editId);
                var promise = $http.get(url + editId + ")?view=full", {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    console.log(response);
                    return response.data;
                });
                return promise;
            }
        };
        return substanceIDRet;
    }]);

    ginasApp.service('substanceRetriever', ['$http', function ($http) {
        var url = baseurl + "api/v1/substances?filter=names.name='";
        var substanceRet = {
            getSubstances: function (name) {
                var promise = $http.get(url + name.toUpperCase() + "'", {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    return response.data;
                });
                return promise;
            }
        };
        return substanceRet;
    }]);

    ginasApp.service('data', function ($http) {
        var options = {};
        var url = baseurl + "api/v1/vocabularies?filter=domain='";

        this.load = function (field) {
            $http.get(url + field.toUpperCase() + "'", {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (data) {
                options[field] = data.content[0].terms;
            });
        };

        this.search = function (field, query) {
            return _.chain(options[field])
                .filter(function (x) {
                    return !query || x.display.toLowerCase().indexOf(query.toLowerCase()) > -1;
                })
                .sortBy('display')
                .value();
        };
        this.lookup = function (field, query) {
            console.log(options);
            return _.chain(options[field])
                .filter(function (x) {
                    return !query || x.value.toLowerCase().indexOf(query.toLowerCase()) > -1;
                })
                .sortBy('value')
                .value();
        };

        this.retrieve = function (field) {
            console.log(options[field]);
            return options;
        };
    });

    ginasApp.service('substanceSearch', function ($http) {
        var options = {};
        var url = baseurl + "api/v1/suggest/Name?q=";

        this.load = function (field) {
            $http.get(url + field.toUpperCase(), {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }).success(function (response) {
                options.data = response;
            });
        };

        this.search = function (query) {
            return options;
        };
    });

    ginasApp.directive('rendered', function ($http) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                id: '='
                /*                size: '=',
                 amap :'='*/

            },
            template: '<img src=\"' + baseurl + 'img/{{id}}.svg\">'
        };
    });

    ginasApp.directive('amount', function () {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                value: '='
            },
            template: '<div><span class="amt">{{value.nonNumericValue}} {{value.average}} ({{value.low}} to {{value.high}}) {{value.units.display}}</span></div>'
        };
    });


    ginasApp.directive('aminoAcid', function ($compile) {
        var div = '<div class = "col-md-1">';
        var validTool = '<a href="#" class= "aminoAcid" tooltip="{{acid.subunitIndex}}-{{acid.residueIndex}} : {{acid.value}} ({{acid.type}}-{{acid.name}})">{{acid.value}}</a>';
        var invalidTool = '<a href="#" class= "invalidAA" tooltip-class="invalidTool" tooltip="INVALID">{{acid.value}}</a>';
        var space = '&nbsp;';
        var close = '</div>';
        getTemplate = function (aa) {
            var template = '';
            var index = aa.index;
            if (index % 10 === 0) {
                template = space;
            } else {
                var valid = aa.valid;
                if (valid) {
                    template = validTool;
                } else {
                    template = invalidTool;
                }
            }
            return template;
        };

        return {
            restrict: 'E',
            scope: {
                acid: '='
            },
            link: function (scope, element, attrs) {
                var aa = scope.acid;
                element.html(getTemplate(aa)).show();
                $compile(element.contents())(scope);
            }
        };

    });

    ginasApp.directive('subunit', function () {

        return {
            restrict: 'E',
            require: 'ngModel',
            scope: '=',
            link: function (scope, element, attrs, ngModelCtrl) {
                    scope.$watch(function (scope) {
                             if(!scope.subunit)
                                scope.subunit={};
                             if(attrs.subindex === ""){
                                     //scope.subunit.subunitIndex=1;
                             }else{
                                     //scope.subunit.subunitIndex=attrs.subindex-0+1;
                             }
                     });
                     
            },
            template: '<textarea class="form-control string"  rows="5" ng-model="subunit.sequence" name="sequence" placeholder="Sequence" title="sequence" id="sequence" required></textarea>'
        };
    });

    //Ok, this needs to be re-evaluated a bit.
    //Right now, it always round trips, but that doesn't always make sense.
    ginasApp.directive('sketcher', function ($http, $timeout, localStorageService, Substance, lookup, polymerUtils) {
        return {
            restrict: 'E',
            require: "ngModel",
            scope: {
                formsubstance: '=structure'
            },
            template: "<div id='sketcherForm' dataformat='molfile' ondatachange='setMol(this)'></div>",
            link: function (scope, element, attrs, ngModelCtrl) {

                sketcher = new JSDraw("sketcherForm");
                var url = window.strucUrl; //baseurl + 'smiles';
                var structureid = (localStorageService.get('structureid') || false);
                if (localStorageService.get('editID'))
                    structureid = false;
                var lastmol = "";
                var ignorechange = false;
                window.setMol = function (sk) {
                    if (ignorechange)return;
                    //console.log(scope);

                    var mol = sk.getMolfile();
                    if (lastmol === mol)return;
                    $http({
                        method: 'POST',
                        url: url,
                        data: mol,
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    }).success(function (data) {
                        lastmol = data.structure.molfile;
                        if (scope.formsubstance === null || typeof scope.formsubstance === "undefined") {
                            scope.formsubstance = {};
                        }
                        console.log("Type:" + attrs.type);
                        if (attrs.type === "structure") {
                            scope.formsubstance = data.structure;
                        }else if(attrs.type === "polymer") {
                            scope.formsubstance.idealizedStructure = data.structure;
                            for(var i in data.structuralUnits){
                                data.structuralUnits[i].type=lookup.expandCVValueDisplay("POLYMER_SRU_TYPE",data.structuralUnits[i].type);
                            }
                            polymerUtils.setSRUConnectivityDisplay(data.structuralUnits);
                            scope.formsubstance.structuralUnits = data.structuralUnits;
                        
                        }else{
                            scope.formsubstance.structure = data.structure;
                            scope.formsubstance.moieties = data.moieties;
                            scope.formsubstance.q = data.structure.smiles;
                        }
                        console.log(scope);
                    });
                };

                scope.$watch(function (scope) {
                    if (typeof scope.formsubstance == "undefined") {
                        return "undefined";
                    }
                    if (typeof scope.formsubstance.structure == "undefined") {
                        return "undefined";
                    }
                    return scope.formsubstance.structure.molfile;

                }, function (value) {
                    //console.log("I SEE A CHANGE!");
                    if (lastmol !== value) {

                        ignorechange = true;
                        sketcher.setMolfile(value);
                        ignorechange = false;
                        lastmol = sketcher.getMolfile();
                    }
                });
                if (structureid) {
                    console.log("There is an id, it's:" + structureid);
                    $http({
                        method: 'GET',
                        url: baseurl + 'api/v1/structures/' + structureid
                    }).success(function (data) {
                        console.log(data);
                        console.log("fetched");
                        lastmol = data.molfile;
                        sketcher.setMolfile(data.molfile);
                        scope.formsubstance.q = data.smiles;
                        console.log(Substance);
                        localStorageService.remove('structureid');
                    });
                }

            }
        };
    });

    ginasApp.directive('switch', function () {
        return {
            restrict: 'AE',
            replace: true,
            transclude: true,
            template: function (element, attrs) {
                var html = '';
                html += '<span';
                html += ' class="toggleSwitch' + (attrs.class ? ' ' + attrs.class : '') + '"';
                html += attrs.ngModel ? ' ng-click="' + attrs.ngModel + '=!' + attrs.ngModel + (attrs.ngChange ? '; ' + attrs.ngChange + '()"' : '"') : '';
                html += ' ng-class="{ checked:' + attrs.ngModel + ' }"';
                html += '>';
                html += '<small></small>';
                html += '<input type="checkbox"';
                html += attrs.id ? ' id="' + attrs.id + '"' : '';
                html += attrs.name ? ' name="' + attrs.name + '"' : '';
                html += attrs.ngModel ? ' ng-model="' + attrs.ngModel + '"' : '';
                html += ' style="display:none" />';
                html += '<span class="switch-text">';
                /*adding new container for switch text*/
                html += attrs.on ? '<span class="on">' + attrs.on + '</span>' : '';
                /*switch text on value set by user in directive html markup*/
                html += attrs.off ? '<span class="off">' + attrs.off + '</span>' : ' ';
                /*switch text off value set by user in directive html markup*/
                html += '</span>';
                return html;
            }
        };
    });

    ginasApp.directive('exportButton', function () {
        return {
            restrict: 'E',
            scope: {
                structureid: '='
            },
            template: '<button type="button" class="btn btn-primary" structureid = structureid  export><i class="fa fa-external-link chem-button"></i></button>'
        };
    });

    ginasApp.directive('errorWindow', function () {
        return {
            restrict: 'E',
            scope: {
                error: '='
            },
            templateUrl: baseurl + "assets/ginas/templates/errorwindow.html"
        };
    });

    ginasApp.directive('export', function ($http) {
        return function (scope, element, attrs) {
            element.bind("click", function () {
                var modal = angular.element(document.getElementById('export-mol'));
                $http({
                    method: 'GET',
                    url: baseurl + 'export/' + scope.structureid + '.sdf',
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }).then(function (response) {
                    var warnHead = response.headers("EXPORT-WARNINGS").split("___")[0];
                    console.log(warnHead);
                    var warnings = JSON.parse(warnHead);

                    modal.find('#inputExport').text(response.data);
                    if (warnings.length > 0) {
                        var html = "<div class=\"alert alert-danger alert-dismissible\" role=\"alert\">\n" +
                            "                        <button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"> <span aria-hidden=\"true\">&times;</span>\n" +
                            "                        </button>\n" +
                            "                        <span><h4 class=\"warntype\"></h4><span class=\"message\">" + warnings[0].message + "</span></span>";
                        modal.find('.warn').html(html);
                    } else {
                        modal.find('.warn').html("");
                    }

                    modal.modal('show');
                }, function (response) {
                    alert("ERROR exporting data");
                });

            });
        };
    });

    ginasApp.directive('molExport', function ($http) {
        return {
            restrict: 'E',
            templateUrl: baseurl + "assets/ginas/templates/molexport.html"
        };
    });


    ginasApp.controller('DiverseController', function ($scope, Substance, $rootScope) {
        this.adding = true;

        this.toggleEdit = function () {
            this.editing = !this.editing;
        };

        this.toggleAdd = function () {
            this.adding = !this.adding;
        };

        this.reset = function () {
            $scope.diverse = {};
            $scope.$broadcast('show-errors-reset');
        };

        this.validate = function (obj) {
            $scope.$broadcast('show-errors-check-validity');
            if ($scope.diverseForm.$valid) {
                Substance.structurallyDiverse.sourceMaterialClass = obj.sourceMaterialClass;
                Substance.structurallyDiverse.sourceMaterialType = obj.sourceMaterialType;
                Substance.structurallyDiverse.sourceMaterialState = obj.sourceMaterialState;
                this.toggleAdd();
            }
        };

        this.setEdited = function (obj) {
            $scope.editObj = obj;
            $scope.tempCopy = angular.copy(obj);
        };

        this.update = function (reference) {
            console.log(reference);
            var index = Substance.references.indexOf(reference);
            Substance.references[index] = reference;
            $scope.editObj = null;
            this.toggleEdit();
        };

        this.remove = function (reference) {
            var index = Substance.references.indexOf(reference);
            Substance.references.splice(index, 1);
        };


    });

    ginasApp.controller('ProgressJobController', function ($scope, $http, $timeout) {
        $scope.max = 100;
        $scope.monitor = false;
        $scope.mess = "";
        $scope.dynamic = 0;
        $scope.status = "UNKNOWN";
        $scope.stat = {
            recordsPersistedSuccess: 0,
            recordsProcessedSuccess: 0,
            recordsExtractedSuccess: 0
        };
        $scope.init = function (id, pollin, status) {
            $scope.status = status;
            $scope.details = pollin;
            $scope.refresh(id, pollin);
        };
        $scope.refresh = function (id, pollin) {
            $scope.id = id;
            $scope.monitor = pollin;
            var responsePromise = $http.get(baseurl + "api/v1/jobs/" + id + "/");
            responsePromise.success(function (data, status, headers, config) {
                //$scope.myData.fromServer = data.title;
                if ($scope.status != data.status) {
                    //alert($scope.status + "!=" + data.status);
                    //location.reload();
                }
                if (data.status == "RUNNING" || data.status == "PENDING") {
                    $scope.mclass = "progress-striped active";
                } else {
                    if ($scope.stopnext) {
                        $scope.mclass = "";
                        $scope.monitor = false;
                        $scope.mess = "Process : " + data.status;
                    } else {
                        $scope.stopnext = true;
                    }
                }
                $scope.max = data.statistics.totalRecords.count;
                $scope.dynamic = data.statistics.recordsPersistedSuccess +
                    data.statistics.recordsPersistedFailed +
                    data.statistics.recordsProcessedFailed +
                    data.statistics.recordsExtractedFailed;
                $scope.max = data.statistics.totalRecords.count;
                $scope.stat = data.statistics;
                $scope.allExtracted = $scope.max;
                $scope.allPersisted = $scope.max;
                $scope.allProcessed = $scope.max;

                if ($scope.monitor) {
                    $scope.monitor = true;
                    $scope.mess = "Polling ... " + data.status;
                    $scope.refresh(id, $scope.monitor);
                }
            });
            responsePromise.error(function (data, status, headers, config) {
                $scope.monitor = false;
                $scope.mess = "Polling error!";
                $scope.monitor = false;
//                      refresh(id,pollin);
            });

        };
        $scope.stopMonitor = function () {
            $scope.monitor = false;
            $scope.mess = "";
        };
        var poll = function () {
            $timeout(function () {
                //console.log("they see me pollin'");

                $scope.refresh($scope.id, false);
                if ($scope.monitor) poll();
            }, 1000);
        };


    });

    ginasApp.factory('SDFFields', function () {
        var SDFFields = {};
        return SDFFields;
    });


    ginasApp.controller('SDFieldController', function ($scope) {
        $scope.radio = {
            model: 'NULL_TYPE'
        };
        $scope.path = "";
        $scope.radioModel = 'NULL_TYPE';

        $scope.checkModel = [
            "NULL_TYPE",
            "DONT_IMPORT",
            "ADD_CODE",
            "ADD_NAME",
            "ADD_NAME"
        ];

        $scope.init = function (path, model) {
            $scope.path = path;
            $scope.checkModel = model;
        };

        $scope.$watch('radio.model', function (newVal, oldVal) {
            var sdf = window.SDFFields[$scope.path];
            if (typeof sdf === "undefined") {
                sdf = {};
                window.SDFFields[$scope.path] = sdf;
            }
            sdf.path = $scope.path;
            sdf.method = $scope.radio.model;
            
            var l = [];
            for (var k in window.SDFFields) {
                l.push(window.SDFFields[k]);
            }
            //set the submission value
            $("#mappings").val(JSON.stringify(l));
        });


    });


    ginasApp.controller('SubstanceListController', function ($scope) {
        $scope.bigview = false;
        $scope.initialized = false;
        $scope.toggle = function (src) {
            $scope.initialized = true;
            $scope.bigview = !$scope.bigview;
            $scope.src = src;
        };

    });

    ginasApp.controller('SubstanceSelectorInstanceController', function ($scope, $modalInstance, $http) {

        //$scope.items = items;
        $scope.results = {};
        $scope.selected = null;
        $scope.searching = false;

        $scope.top = 8;
        $scope.testb = 0;

        $scope.select = function (item) {
            $scope.selected=item;
            $modalInstance.close(item);
        };

        $scope.ok = function () {
            $modalInstance.close($scope.selected.item);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.fetch = function (term, skip) {
            var url = baseurl + "api/v1/substances/search?q=" +
                term + "*&top=" + $scope.top + "&skip=" + skip;
            // console.log(url);
            var responsePromise = $http.get(url);

            responsePromise.success(function (data, status, headers, config) {
                console.log(data);
                $scope.searching=false;
                $scope.results = data;
            });

            responsePromise.error(function (data, status, headers, config) {
                $scope.searching=false;
            });
        };

        $scope.search = function () {
            $scope.searching=true;
            $scope.fetch($scope.term, 0);
        };


        $scope.nextPage = function () {
            $scope.fetch($scope.term, $scope.results.skip + $scope.results.top);
        };
        $scope.prevPage = function () {
            $scope.fetch($scope.term, $scope.results.skip - $scope.results.top);
        };


    });
})();
window.SDFFields = {};


function getDisplayFromCV(domain, value) {
    for (var i in window.CV_REQUEST.content) {
        if (window.CV_REQUEST.content[i].domain === domain) {
            var terms = window.CV_REQUEST.content[i].terms;
            for (var t in terms) {
                if (terms[t].value === value) {
                    return terms[t].display;
                }
            }
        }
    }
    return value;
}

function vocabsetup(cv) {
    window.CV_REQUEST = cv;
    console.log("finished");
}


function submitq(qinput) {
    if (qinput.value.indexOf("\"") < 0 && qinput.value.indexOf("*") < 0 && qinput.value.indexOf(":") < 0 && qinput.value.indexOf(" AND ") < 0 && qinput.value.indexOf(" OR ") < 0) {
        qinput.value = "\"" + qinput.value + "\"";
    }
    return true;
}


