webpackJsonp([1,4],{

/***/ 114:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__data__ = __webpack_require__(204);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ReferencedData; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var ReferencedData = (function (_super) {
    __extends(ReferencedData, _super);
    function ReferencedData() {
        _super.apply(this, arguments);
    }
    ReferencedData.prototype.hasReference = function (ref) {
        return this.references.indexOf(ref.uuid) > -1;
    };
    ReferencedData.prototype.removeReference = function (ref) {
        var loc = this.references.indexOf(ref.uuid);
        if (loc > -1) {
            this.references.splice(loc, 1);
            ref.removeData(this);
        }
        return this;
    };
    ReferencedData.prototype.addReference = function (ref) {
        var loc = this.references.indexOf(ref.uuid);
        if (loc == -1) {
            this.references.push(ref.uuid);
            ref.addData(this);
        }
        return this;
    };
    return ReferencedData;
}(__WEBPACK_IMPORTED_MODULE_0__data__["a" /* Data */]));
//# sourceMappingURL=referencedData.js.map

/***/ }),

/***/ 1254:
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(579);


/***/ }),

/***/ 145:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_forms__ = __webpack_require__(13);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_rxjs_add_operator_startWith__ = __webpack_require__(218);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_rxjs_add_operator_startWith___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_2_rxjs_add_operator_startWith__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__reference__ = __webpack_require__(146);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__reference_list_service__ = __webpack_require__(488);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__reference_component__ = __webpack_require__(489);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__angular_material__ = __webpack_require__(111);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_7_lodash__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__angular_http__ = __webpack_require__(108);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9__utils_utils_service__ = __webpack_require__(30);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ReferenceListDialog; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};










var ReferenceListDialog = (function () {
    function ReferenceListDialog(referenceService, refDialog, http, utilService) {
        this.referenceService = referenceService;
        this.refDialog = refDialog;
        this.http = http;
        this.utilService = utilService;
        this._referenceViewTab = "all";
        this._filterQuery = "";
        this.closeFunction = function () { }; //the specific function to call on close
        this.allReferences = []; //every reference currently present (master list) [full]
        this.allRefsForData = []; //those currently displayable
        this.allRefsForDataFiltered = []; //those currently displayed
        this.username = "";
        this.refCtrl = new __WEBPACK_IMPORTED_MODULE_1__angular_forms__["FormControl"]();
        //this.filteredRefs = this.refCtrl.valueChanges
        //  .startWith(null)
        //  .map(citation => citation ? this.filter(citation) : this.references);
    }
    Object.defineProperty(ReferenceListDialog.prototype, "referenceViewTab", {
        get: function () {
            return this._referenceViewTab;
        },
        set: function (value) {
            if (value !== this._referenceViewTab) {
                this._referenceViewTab = value;
                //should trigger view change here
                this.toggleReferences(this._referenceViewTab);
            }
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(ReferenceListDialog.prototype, "filterQuery", {
        get: function () {
            return this._filterQuery;
        },
        set: function (q) {
            this._filterQuery = q;
            this.filter(this._filterQuery);
        },
        enumerable: true,
        configurable: true
    });
    ReferenceListDialog.prototype.getUsername = function () {
        var _this = this;
        var endpoint = this.utilService.browserurl + this.utilService.appContxt + "/api/v1/whoami";
        this.http
            .get(endpoint)
            .subscribe(function (res) {
            console.log(res.json());
            console.log(res.json().identifier);
            _this.username = res.json().identifier;
            console.log(_this.username);
        });
    };
    ReferenceListDialog.prototype.toggleReferences = function (value) {
        console.log("Clicked:" + value);
        if (value == "all") {
            this.setRefsToAll();
        }
        else if (value == "last5") {
            console.log(this.username);
            this.setRefsFromAPIMatchingUser(this.username);
        }
        else if (value == "selected") {
            this.setRefsForData(this.data);
        }
    };
    //Set what references will be displayed
    ReferenceListDialog.prototype.setDisplayRefs = function (refs) {
        /*    refs.sort((a,b) => {
              return a.lastEdited - b.lastEdited;
            });*/
        this.allRefsForData = refs;
        this.allRefsForDataFiltered = refs;
    };
    //sets shortlist to be from global thing
    ReferenceListDialog.prototype.setRefsFromAPIMatchingUser = function (user) {
        var _this = this;
        this.referenceService.getLastFiveReferences(user)
            .then(function (refs) {
            __WEBPACK_IMPORTED_MODULE_7_lodash__["forEach"](refs, function (r) { return r.index = r.index + _this.allReferences.length; });
            _this.setDisplayRefs(refs);
        });
    };
    //sets the shortlist of checked references to be those
    //found in the given data
    ReferenceListDialog.prototype.setRefsForData = function (dat) {
        var refs = __WEBPACK_IMPORTED_MODULE_7_lodash__["chain"](this.allReferences)
            .filter(function (r) { return (dat.references.indexOf(r.uuid) >= 0); })
            .value();
        this.setDisplayRefs(refs);
    };
    //sets to all references
    ReferenceListDialog.prototype.setRefsToAll = function () {
        var refs = this.allReferences;
        this.setDisplayRefs(refs);
    };
    ReferenceListDialog.prototype.filter = function (q) {
        if (q === "") {
            this.allRefsForDataFiltered = this.allRefsForData;
        }
        else {
            this.allRefsForDataFiltered = __WEBPACK_IMPORTED_MODULE_7_lodash__["chain"](this.allRefsForData)
                .filter(function (r) { return (r.citation.indexOf(q) > -1 || (r.index + "") === q); })
                .value();
        }
    };
    ReferenceListDialog.prototype.ngOnInit = function () {
        this.toggleReferences(this._referenceViewTab);
        this.getUsername();
    };
    ReferenceListDialog.prototype.isChecked = function (ref) {
        var has = false;
        if (this.data) {
            has = this.data.hasReference(ref);
        }
        return has;
    };
    ReferenceListDialog.prototype.applySelectedToAllRecords = function () {
        var _this = this;
        var refs = __WEBPACK_IMPORTED_MODULE_7_lodash__["chain"](this.allReferences)
            .filter(function (r) { return (_this.data.references.indexOf(r.uuid) >= 0); })
            .value();
        console.log(refs.length);
        if (this.dataList) {
            console.log(this.dataList);
            var _loop_1 = function(name) {
                if (name.references.length <= 0) {
                    __WEBPACK_IMPORTED_MODULE_7_lodash__["forEach"](refs, function (r) { return name.addReference(r); });
                }
            };
            for (var _i = 0, _a = this.dataList; _i < _a.length; _i++) {
                var name = _a[_i];
                _loop_1(name);
            }
        }
    };
    ReferenceListDialog.prototype.debug = function () {
        console.log(this.allRefsForData);
    };
    ReferenceListDialog.prototype.close = function () {
        this.closeFunction();
    };
    // Adds/removes the reference from the list in
    // the data's references
    ReferenceListDialog.prototype.toggle = function (toggleReference) {
        console.log("data:");
        console.log(this.data);
        console.log("toggleref");
        console.log(toggleReference);
        var n = this.data;
        var ref = toggleReference;
        console.log("looking");
        if (this._referenceViewTab === "last5") {
            var match = __WEBPACK_IMPORTED_MODULE_7_lodash__["chain"](this.allReferences)
                .find(function (r) { return r.getFlag("ouuid") === ref.uuid; })
                .value();
            if (!match) {
                var r2 = toggleReference.clone();
                r2.generateNewUuid();
                r2.setFlag("ouuid", toggleReference.uuid);
                r2.index = this.allReferences.length + 1;
                this.allReferences.push(r2);
                ref = r2;
            }
            else {
                ref = match;
            }
        }
        if (n) {
            if (n.hasReference(ref)) {
                n.removeReference(ref);
            }
            else {
                n.addReference(ref);
            }
        }
        if (ref.isImportedOrphan()) {
            __WEBPACK_IMPORTED_MODULE_7_lodash__["remove"](this.allReferences, function (r) { return r.uuid === ref.uuid; });
        }
        console.log("toggled");
    };
    ReferenceListDialog.prototype.apply = function () {
        // do nothing for now, but need this method call
    };
    ReferenceListDialog.prototype.openRefEditDialog = function (selectedReference) {
        var _this = this;
        //if there is no reference, make one and add it
        if (!selectedReference) {
            selectedReference = new __WEBPACK_IMPORTED_MODULE_3__reference__["a" /* Reference */]();
            selectedReference.generateNewUuid();
            selectedReference.setFlag("new", true);
            selectedReference.index = this.allReferences.length + 1;
            selectedReference.lastEdited = new Date();
            this.allReferences.unshift(selectedReference);
        }
        var refEditDialog = this.refDialog.open(__WEBPACK_IMPORTED_MODULE_5__reference_component__["a" /* ReferenceEditDialog */], { height: '700px', width: '550px' });
        refEditDialog.componentInstance.selectedReference = selectedReference;
        refEditDialog.componentInstance.close = function () { refEditDialog.close(); };
        refEditDialog.afterClosed().subscribe(function (result) {
            console.log("Done");
            if (selectedReference.getFlag("delete")) {
                __WEBPACK_IMPORTED_MODULE_7_lodash__["remove"](_this.allReferences, function (r) { return r === selectedReference; });
            }
            else {
                selectedReference.setFlag("new", false);
            }
            _this.selectedOption = result; //not sure what this does
        });
    };
    ReferenceListDialog = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'reference-list-dialog',
            template: __webpack_require__(986),
            providers: [__WEBPACK_IMPORTED_MODULE_4__reference_list_service__["a" /* ReferenceListService */], __WEBPACK_IMPORTED_MODULE_9__utils_utils_service__["a" /* UtilService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_4__reference_list_service__["a" /* ReferenceListService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__reference_list_service__["a" /* ReferenceListService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_6__angular_material__["d" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__angular_material__["d" /* MdDialog */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_8__angular_http__["b" /* Http */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_8__angular_http__["b" /* Http */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_9__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_9__utils_utils_service__["a" /* UtilService */]) === 'function' && _d) || Object])
    ], ReferenceListDialog);
    return ReferenceListDialog;
    var _a, _b, _c, _d;
}());
//# sourceMappingURL=reference-list-dialog.component.js.map

/***/ }),

/***/ 146:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__data__ = __webpack_require__(204);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Reference; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var Reference = (function (_super) {
    __extends(Reference, _super);
    function Reference() {
        _super.apply(this, arguments);
    }
    Reference.prototype.addData = function (dat) {
        var dlist = this.getFlag("data");
        if (!dlist) {
            dlist = [];
            this.setFlag("data", dlist);
        }
        //TODO: should use UUIDs?
        dlist.push(dat);
        return this;
    };
    Reference.prototype.removeData = function (dat) {
        var dlist = this.getFlag("data");
        if (dlist) {
            var idx = dlist.indexOf(dat);
            if (idx > -1) {
                dlist.splice(idx, 1);
            }
        }
        return this;
    };
    Reference.prototype.getData = function () {
        var dlist = this.getFlag("data");
        if (typeof dlist === "undefined") {
            return [];
        }
        return dlist;
    };
    Reference.prototype.isImported = function () {
        return (typeof this.getFlag("ouuid") !== "undefined");
    };
    Reference.prototype.isImportedOrphan = function () {
        console.log("imported Orphan");
        return this.isImported() && this.getData().length === 0;
    };
    return Reference;
}(__WEBPACK_IMPORTED_MODULE_0__data__["a" /* Data */]));
//# sourceMappingURL=reference.js.map

/***/ }),

/***/ 204:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_0_lodash__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Data; });

var Data = (function () {
    function Data() {
        //processing helpers
        this.$specialFlags = {};
    }
    Data.prototype.merge = function (json) {
        return __WEBPACK_IMPORTED_MODULE_0_lodash__["merge"](this, json);
    };
    Data.prototype.generateNewUuid = function () {
        this.uuid = this.uuidHelper().randomUUID();
        console.log("made new uuid:" + this.uuid);
        return this;
    };
    Data.prototype.clone = function () {
        return __WEBPACK_IMPORTED_MODULE_0_lodash__["cloneDeep"](this);
    };
    Data.prototype.getFlag = function (key) {
        return this.$specialFlags[key];
    };
    Data.prototype.setFlag = function (key, val) {
        this.$specialFlags[key] = val;
        return this;
    };
    //TODO: move this somewhere more generic
    Data.prototype.uuidHelper = function () {
        var UUID = {
            randomUUID: function () {
                return UUID.s4() + UUID.s4() + '-' + UUID.s4() + '-' + UUID.s4() + '-' +
                    UUID.s4() + '-' + UUID.s4() + UUID.s4() + UUID.s4();
            },
            s4: function () {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            },
            isUUID: function (uuid) {
                if ((uuid + "").match(/^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/)) {
                    return true;
                }
                return false;
            }
        };
        return UUID;
    };
    return Data;
}());
//# sourceMappingURL=data.js.map

/***/ }),

/***/ 29:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_lodash__);
/* unused harmony export CV_DOMAIN_TYPES */
/* unused harmony export COMMON_TABS */
/* unused harmony export CHEMICAL_TABS */
/* unused harmony export PROTEIN_TABS */
/* unused harmony export PROPERTY_TYPE */
/* unused harmony export PARAMETER_TYPE */
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return CVService; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};


var CV_DOMAIN_TYPES;
(function (CV_DOMAIN_TYPES) {
    CV_DOMAIN_TYPES[CV_DOMAIN_TYPES["NAME_TYPE"] = 0] = "NAME_TYPE";
    CV_DOMAIN_TYPES[CV_DOMAIN_TYPES["ACCESS"] = 1] = "ACCESS";
    CV_DOMAIN_TYPES[CV_DOMAIN_TYPES["LANGUAGES"] = 2] = "LANGUAGES";
    CV_DOMAIN_TYPES[CV_DOMAIN_TYPES["DOMAINS"] = 3] = "DOMAINS";
    CV_DOMAIN_TYPES[CV_DOMAIN_TYPES["NAME_ORGS"] = 4] = "NAME_ORGS";
    CV_DOMAIN_TYPES[CV_DOMAIN_TYPES["NAME_JURISDICTION"] = 5] = "NAME_JURISDICTION";
})(CV_DOMAIN_TYPES || (CV_DOMAIN_TYPES = {}));
/*export const ACCESS: term[] = [
  {value: 'public', label:'Public'},
  {value: 'cber', label:'CBER'},
  {value: 'cder', label:'CDER'},
  {value: 'cvm', label:'CVM'},
  {value: 'protected', label:'Protected'},
  {value: 'admin', label:'Admin'}];*/
var COMMON_TABS = [
    { header: 'Names', content: 'names' },
    { header: 'References', content: 'references' },
    { header: 'Codes', content: 'codes' },
    { header: 'Relationships', content: 'relationships' },
    /*  {header: 'Notes', content:'notes'},
      {header: 'Properties', content:'properties'},*/
    { header: 'Expand View', content: 'summary' }
];
var CHEMICAL_TABS = [
    { header: 'Structure', content: 'CLINICAL' }];
var PROTEIN_TABS = [
    { header: 'Protein Details', content: 'CLINICAL' },
    { header: 'Subunits', content: 'CLINICAL' },
    { header: 'Disulfide Links', content: 'CLINICAL' },
    { header: 'Other Links', content: 'CLINICAL' },
    { header: 'Glycosylation', content: 'CLINICAL' },
    { header: 'Agent Modifications', content: 'CLINICAL' },
    { header: 'Structural Modifications', content: 'CLINICAL' },
    { header: 'Physical Modifications', content: 'CLINICAL' },];
//need to update the new values in the database for the api call
var PROPERTY_TYPE = [
    { value: "PHYSICAL MEASURED", label: "PHYSICAL MEASURED" },
    { value: "PHYSICAL CALCULATED", label: "PHYSICAL CALCULATED" },
    { value: "ENZYMATIC", label: "ENZYMATIC" },
    { value: "CHEMICAL CALCULATED", label: "CHEMICAL CALCULATED" },
    { value: "CHEMICAL MEASURED", label: "CHEMICAL MEASURED" }
];
var PARAMETER_TYPE = [
    { value: "PHYSICAL", label: "PHYSICAL" },
    { value: "CHEMICAL", label: "CHEMICAL" },
    { value: "ENZYMATIC", label: "ENZYMATIC" }
];
var CVService = (function () {
    function CVService() {
    }
    CVService.prototype.getList = function (listName) {
        return this.getCVLists(listName);
    };
    CVService.prototype.getParameterType = function () {
        return Promise.resolve(PARAMETER_TYPE);
    };
    CVService.prototype.getPropertyType = function () {
        return Promise.resolve(PROPERTY_TYPE);
    };
    CVService.prototype.getTabs = function () {
        return Promise.resolve(COMMON_TABS);
    };
    CVService.prototype.getCVLists = function (cvDomain) {
        var c = CVService.cache[cvDomain];
        if (c) {
            console.log("from cache " + cvDomain);
            return new Promise(function (resolve, reject) { resolve(c); });
        }
        return new Promise(function (resolve, reject) {
            console.log("from api" + cvDomain);
            window["GGlob"].CVFinder
                .searchByDomain(cvDomain)
                .andThen(function (cnt) { return cnt.content; })
                .andThen(function (cvs) {
                var mcvs = __WEBPACK_IMPORTED_MODULE_1_lodash__["chain"](cvs)
                    .flatMap(function (cv) { return cv.terms; })
                    .map(function (t) {
                    t.label = t.display;
                    return t;
                })
                    .sortBy(["label"])
                    .uniqBy("value")
                    .value();
                CVService.cache[cvDomain] = mcvs;
                return mcvs;
            })
                .get(function (cvs) { return resolve(cvs); });
        });
    };
    CVService.cache = {};
    CVService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], CVService);
    return CVService;
}());
//# sourceMappingURL=cv.service.js.map

/***/ }),

/***/ 30:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_lodash__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return UtilService; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};


var UtilService = (function () {
    function UtilService() {
        this.references = [];
        this.appContxt = "/app";
        this.browserurl = window.location.href.split(this.appContxt)[0];
    }
    // public browserurl: string ="http://localhost:9000";
    UtilService.prototype.getBrowserUrl = function () {
        return this.browserurl;
    };
    UtilService.prototype.changeAccess = function ($event, mod) {
        console.log(mod);
        console.log($event);
        if ($event.value.length == 0) {
            mod.access = [];
        }
        else if ($event.value.length > 1) {
            var index = $event.value.indexOf("public");
            if (index != -1) {
                mod.access.splice(index, 1);
            }
        }
    };
    UtilService.prototype.showReferenceIndexes = function (data) {
        if (data.references) {
            return __WEBPACK_IMPORTED_MODULE_1_lodash__["chain"](this.references)
                .filter(function (r) { return (data.references.indexOf(r.uuid) >= 0); })
                .map("index")
                .value();
        }
        else {
            return "";
        }
    };
    UtilService.prototype.displayAmount = function (amt) {
        var ret = "";
        if (amt) {
            if (amt.type) {
                ret += amt.type + " \n ";
            }
            if (amt.average || amt.low || amt.high) {
                if (amt.average) {
                    ret += amt.average + " ";
                }
                if (amt.high || amt.low) {
                    ret += " [";
                    if (amt.high && !amt.low) {
                        ret += "<" + amt.high;
                    }
                    else if (!amt.high && amt.low) {
                        ret += ">" + amt.low;
                    }
                    else if (amt.high && amt.low) {
                        ret += amt.low + " to " + amt.high;
                    }
                    ret += "] (average)";
                }
                if (amt.highLimit || amt.lowLimit) {
                    ret += "\n[";
                }
                if (amt.highLimit && !amt.lowLimit) {
                    ret += "<" + amt.highLimit;
                }
                else if (!amt.highLimit && amt.lowLimit) {
                    ret += ">" + amt.lowLimit;
                }
                else if (amt.highLimit && amt.lowLimit) {
                    ret += amt.lowLimit + " to " + amt.highLimit;
                }
                if (amt.highLimit || amt.lowLimit) {
                    ret += "] (limits)";
                }
                if (amt.units) {
                    ret += amt.units;
                }
                if (amt.nonNumericValue) {
                    ret += "\n" + amt.nonNumericValue;
                }
            }
        }
        return ret;
    };
    UtilService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], UtilService);
    return UtilService;
}());
//# sourceMappingURL=utils.service.js.map

/***/ }),

/***/ 302:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(114);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Code; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var Code = (function (_super) {
    __extends(Code, _super);
    function Code() {
        _super.call(this);
    }
    return Code;
}(__WEBPACK_IMPORTED_MODULE_0__references_referencedData__["a" /* ReferencedData */]));
//# sourceMappingURL=code.js.map

/***/ }),

/***/ 303:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(114);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Name; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var Name = (function (_super) {
    __extends(Name, _super);
    function Name() {
        _super.call(this);
    }
    Name.prototype.justATest = function () {
        return Promise.resolve("Test");
    };
    return Name;
}(__WEBPACK_IMPORTED_MODULE_0__references_referencedData__["a" /* ReferencedData */]));
//# sourceMappingURL=name.js.map

/***/ }),

/***/ 304:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(114);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Relationship; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var Relationship = (function (_super) {
    __extends(Relationship, _super);
    function Relationship() {
        _super.call(this);
    }
    return Relationship;
}(__WEBPACK_IMPORTED_MODULE_0__references_referencedData__["a" /* ReferencedData */]));
//# sourceMappingURL=relationship.js.map

/***/ }),

/***/ 481:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Amount; });
var Amount = (function () {
    function Amount() {
    }
    return Amount;
}());
//# sourceMappingURL=amount.model.js.map

/***/ }),

/***/ 482:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_primeng_primeng__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__references_reference_list_service__ = __webpack_require__(488);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__code__ = __webpack_require__(302);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__ = __webpack_require__(30);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__substanceedit_substanceedit_service__ = __webpack_require__(58);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7__references_reference_list_dialog_component__ = __webpack_require__(145);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__angular_material__ = __webpack_require__(111);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_9_lodash__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return CodeListComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};










var CodeListComponent = (function () {
    function CodeListComponent(referenceListService, confirmationService, cvService, dialog, utilService, substanceEditService) {
        this.referenceListService = referenceListService;
        this.confirmationService = confirmationService;
        this.cvService = cvService;
        this.dialog = dialog;
        this.utilService = utilService;
        this.substanceEditService = substanceEditService;
        this.codes = [];
        this.access = [];
        this.codeSystem = [];
        this.codeType = [];
        this.references = [];
    }
    CodeListComponent.prototype.ngOnInit = function () {
        this.getReferences();
        this.getAccess();
        this.getCodeSystem();
        this.getCodes();
        this.getCodeType();
    };
    CodeListComponent.prototype.confirmDeleteCode = function (code) {
        var _this = this;
        this.confirmationService.confirm({
            message: 'Do you want to delete this Code?',
            header: 'Delete Confirmation',
            icon: 'fa fa-trash',
            accept: function () {
                _this.deleteCode(code);
            }
        });
    };
    CodeListComponent.prototype.findSelectedCodeIndex = function (code) {
        return this.codes.indexOf(code);
    };
    CodeListComponent.prototype.deleteCode = function (code) {
        this.codes.splice(this.findSelectedCodeIndex(code), 1);
    };
    CodeListComponent.prototype.addCode = function (top) {
        console.log("add");
        this.newCode = true;
        this.nCode = new __WEBPACK_IMPORTED_MODULE_4__code__["a" /* Code */]();
        this.nCode.references = [];
        this.nCode.access = [];
        this.nCode.generateNewUuid();
        if (top) {
            this.codes.unshift(this.nCode);
        }
        else {
            this.codes.push(this.nCode);
        }
    };
    CodeListComponent.prototype.exportRefs = function () {
        console.log("export codes");
    };
    CodeListComponent.prototype.refresh = function () {
        this.ngOnInit();
    };
    CodeListComponent.prototype.showReferenceIndexes = function (data) {
        if (data.references) {
            return __WEBPACK_IMPORTED_MODULE_9_lodash__["chain"](this.references)
                .filter(function (r) { return (data.references.indexOf(r.uuid) >= 0); })
                .map("index")
                .value();
        }
        else {
            return "";
        }
    };
    CodeListComponent.prototype.search = function () {
        console.log("search codes");
    };
    CodeListComponent.prototype.getReferences = function () {
        var _this = this;
        this.substanceEditService
            .getReferences()
            .then(function (refs) {
            _this.references = refs;
        });
    };
    CodeListComponent.prototype.getCodes = function () {
        var _this = this;
        this.substanceEditService
            .getCodes()
            .then(function (codes) {
            _this.codes = codes;
            // this.codes = _.orderBy(this.codes, ['codeSystem'],['asc']);
            _this.codes.sort(function (a, b) {
                return a.codeSystem.localeCompare(b.codeSystem);
            });
        });
    };
    CodeListComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    CodeListComponent.prototype.getCodeSystem = function () {
        var _this = this;
        console.log("get code system");
        this.cvService
            .getList("CODE_SYSTEM")
            .then(function (cs) { return _this.codeSystem = cs; });
    };
    CodeListComponent.prototype.getCodeType = function () {
        var _this = this;
        this.cvService
            .getList("CODE_TYPE")
            .then(function (cs) { return _this.codeType = cs; });
    };
    CodeListComponent.prototype.changeAccess = function ($event, code) {
        this.utilService.changeAccess($event, code);
    };
    CodeListComponent.prototype.openRefListDialog = function (name) {
        console.log("reference list");
        var config = new __WEBPACK_IMPORTED_MODULE_8__angular_material__["e" /* MdDialogConfig */]();
        config.width = '400px';
        //config.height = '700px';
        var dialogRef = this.dialog.open(__WEBPACK_IMPORTED_MODULE_7__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], config);
        //TODO: should have a cleaner initialization process
        dialogRef.componentInstance.allReferences = this.references;
        console.log(name);
        dialogRef.componentInstance.data = name;
        dialogRef.componentInstance.dataList = this.codes;
        dialogRef.componentInstance.closeFunction = function () {
            dialogRef.close();
        };
    };
    CodeListComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'code-list',
            template: __webpack_require__(976),
            //styleUrls: ['../styles.css']
            providers: [__WEBPACK_IMPORTED_MODULE_2__references_reference_list_service__["a" /* ReferenceListService */], __WEBPACK_IMPORTED_MODULE_1_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_6__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__references_reference_list_service__["a" /* ReferenceListService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__references_reference_list_service__["a" /* ReferenceListService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_1_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1_primeng_primeng__["ConfirmationService"]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__["a" /* CVService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_8__angular_material__["d" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_8__angular_material__["d" /* MdDialog */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */]) === 'function' && _e) || Object, (typeof (_f = typeof __WEBPACK_IMPORTED_MODULE_6__substanceedit_substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _f) || Object])
    ], CodeListComponent);
    return CodeListComponent;
    var _a, _b, _c, _d, _e, _f;
}());
//# sourceMappingURL=code-list.component.js.map

/***/ }),

/***/ 483:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__name__ = __webpack_require__(303);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_material__ = __webpack_require__(111);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__references_reference_list_dialog_component__ = __webpack_require__(145);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5_primeng_primeng__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_5_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__ = __webpack_require__(30);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_rxjs_add_operator_switchMap__ = __webpack_require__(156);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_rxjs_add_operator_switchMap___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_7_rxjs_add_operator_switchMap__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__angular_router__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_9_lodash__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_10__substanceedit_substanceedit_service__ = __webpack_require__(58);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return NameListComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};











var NameListComponent = (function () {
    function NameListComponent(cvService, dialog, confirmationService, utilService, route, substanceEditService) {
        this.cvService = cvService;
        this.dialog = dialog;
        this.confirmationService = confirmationService;
        this.utilService = utilService;
        this.route = route;
        this.substanceEditService = substanceEditService;
        this.names = [];
        this.references = [];
        this.recordUUID = "";
        this.nName = new __WEBPACK_IMPORTED_MODULE_1__name__["a" /* Name */]();
        this.tabs = [];
        this.nameValidation = [];
        this.nameTypes = [];
        this.access = [];
        this.filteredAccessMultiple = [];
    }
    NameListComponent.prototype.getNameTypes = function () {
        var _this = this;
        this.cvService
            .getList("NAME_TYPE")
            .then(function (nameTypes) { return _this.nameTypes = nameTypes; });
    };
    NameListComponent.prototype.getAccess = function () {
        var _this = this;
        console.log("load access - name list");
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    NameListComponent.prototype.getReferences = function () {
        var _this = this;
        this.substanceEditService
            .getReferences()
            .then(function (refs) {
            _this.references = refs;
        });
    };
    NameListComponent.prototype.getNames = function () {
        var _this = this;
        this.substanceEditService
            .getNames()
            .then(function (nams) {
            _this.names = nams;
            _this.names.sort(function (a, b) {
                if (a.displayName)
                    return -1;
                if (b.displayName)
                    return 1;
                return a.name.localeCompare(b.name);
            });
        });
    };
    NameListComponent.prototype.getTabs = function () {
        var _this = this;
        this.cvService.getTabs().then(function (tabs) {
            _this.tabs = tabs;
        });
    };
    NameListComponent.prototype.showReferenceIndexes = function (data) {
        if (data.references) {
            return __WEBPACK_IMPORTED_MODULE_9_lodash__["chain"](this.references)
                .filter(function (r) { return (data.references.indexOf(r.uuid) >= 0); })
                .map("index")
                .value();
        }
        else {
            return "";
        }
    };
    /*  refresh(): void{
        this.ngOnInit();
      }*/
    NameListComponent.prototype.ngOnInit = function () {
        this.recordUUID = this.route.parent.snapshot.params['id'];
        this.getNameTypes();
        this.getAccess();
        this.getTabs();
        this.getNames();
        this.getReferences();
        console.log("Initializing names");
    };
    NameListComponent.prototype.openRefListDialog = function (name) {
        console.log("reference list");
        var config = new __WEBPACK_IMPORTED_MODULE_3__angular_material__["e" /* MdDialogConfig */]();
        config.width = '400px';
        //config.height = '700px';
        var dialogRef = this.dialog.open(__WEBPACK_IMPORTED_MODULE_4__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], config);
        //TODO: should have a cleaner initialization process
        dialogRef.componentInstance.allReferences = this.references;
        console.log(name);
        dialogRef.componentInstance.data = name;
        dialogRef.componentInstance.dataList = this.names;
        dialogRef.componentInstance.closeFunction = function () {
            dialogRef.close();
        };
    };
    NameListComponent.prototype.addName = function (top) {
        console.log("add name");
        this.nName = new __WEBPACK_IMPORTED_MODULE_1__name__["a" /* Name */]();
        this.nName.languages = ['en'];
        this.nName.access = [];
        this.nName.references = [];
        this.nName.generateNewUuid();
        if (top) {
            this.names.unshift(this.nName);
        }
        else {
            this.names.push(this.nName);
        }
    };
    NameListComponent.prototype.validateNames = function (names) {
        for (var _i = 0, names_1 = names; _i < names_1.length; _i++) {
            var n = names_1[_i];
            if (!n.name) {
                this.nameValidation.push("Name is a required field");
            }
        }
    };
    NameListComponent.prototype.findSelectedNameIndex = function (name) {
        return this.names.indexOf(name);
    };
    NameListComponent.prototype.deleteName = function (name) {
        console.log("delete:");
        console.log(name);
        this.names.splice(this.findSelectedNameIndex(name), 1);
    };
    NameListComponent.prototype.confirmDeleteName = function (name) {
        var _this = this;
        this.confirmationService.confirm({
            message: 'Do you want to delete this name?',
            header: 'Delete Confirmation',
            icon: 'fa fa-trash',
            accept: function () {
                console.log("confirm delete name");
                _this.deleteName(name);
            }
        });
    };
    NameListComponent.prototype.changeAccess = function ($event, name) {
        console.log("access change from name");
        this.utilService.changeAccess($event, name);
    };
    NameListComponent.prototype.changePreferTerm = function ($event, name) {
        for (var _i = 0, _a = this.names; _i < _a.length; _i++) {
            var n = _a[_i];
            n.displayName = false;
        }
        name.displayName = true;
    };
    NameListComponent.prototype.filterAccessMultiple = function ($event, name) {
        var _this = this;
        var query = $event.query;
        this.cvService.getList("ACCESS_GROUP").then(function (access) {
            var dif = __WEBPACK_IMPORTED_MODULE_9_lodash__["differenceWith"](access, name.access, __WEBPACK_IMPORTED_MODULE_9_lodash__["isEqual"]);
            _this.filteredAccessMultiple = _this.filterAccess(query, access);
        });
    };
    NameListComponent.prototype.filterAccess = function (query, access) {
        //in a real application, make a request to a remote url with the query and return filtered results, for demo we filter at client side
        var filtered = [];
        for (var i = 0; i < access.length; i++) {
            var ac = access[i];
            if (ac.label.toLowerCase().indexOf(query.toLowerCase()) == 0) {
                filtered.push(ac);
            }
        }
        return filtered;
    };
    NameListComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'name-list',
            template: __webpack_require__(978),
            providers: [__WEBPACK_IMPORTED_MODULE_5_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_10__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_3__angular_material__["d" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__angular_material__["d" /* MdDialog */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_5_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5_primeng_primeng__["ConfirmationService"]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__["a" /* UtilService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_8__angular_router__["ActivatedRoute"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_8__angular_router__["ActivatedRoute"]) === 'function' && _e) || Object, (typeof (_f = typeof __WEBPACK_IMPORTED_MODULE_10__substanceedit_substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_10__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _f) || Object])
    ], NameListComponent);
    return NameListComponent;
    var _a, _b, _c, _d, _e, _f;
}());
//# sourceMappingURL=name-list.component.js.map

/***/ }),

/***/ 484:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(114);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Notes; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var Notes = (function (_super) {
    __extends(Notes, _super);
    function Notes() {
        _super.apply(this, arguments);
    }
    return Notes;
}(__WEBPACK_IMPORTED_MODULE_0__references_referencedData__["a" /* ReferencedData */]));
//# sourceMappingURL=notes.js.map

/***/ }),

/***/ 485:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(114);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Property; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var Property = (function (_super) {
    __extends(Property, _super);
    function Property(propertyService) {
        _super.call(this);
        this.propertyService = propertyService;
    }
    return Property;
}(__WEBPACK_IMPORTED_MODULE_0__references_referencedData__["a" /* ReferencedData */]));
//# sourceMappingURL=property.model.js.map

/***/ }),

/***/ 486:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__data__ = __webpack_require__(204);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return PropertyService; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};


var PropertyService = (function (_super) {
    __extends(PropertyService, _super);
    function PropertyService() {
        _super.apply(this, arguments);
        this.properties = [];
    }
    PropertyService.prototype.saveProperty = function (property) {
        console.log("property save service");
    };
    PropertyService.prototype.deleteProperty = function (property) {
        console.log("delete property service");
        var loc = this.properties.indexOf(property);
        if (loc > -1) {
            this.properties.splice(loc, 1);
        }
        return this.properties;
    };
    PropertyService.prototype.addProperty = function (property) {
        if (!property.uuid) {
            property.generateNewUuid();
        }
        this.properties.push(property);
        return this;
    };
    PropertyService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], PropertyService);
    return PropertyService;
}(__WEBPACK_IMPORTED_MODULE_1__data__["a" /* Data */]));
//# sourceMappingURL=property.service.js.map

/***/ }),

/***/ 487:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__reference__ = __webpack_require__(146);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_primeng_primeng__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_2_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__ = __webpack_require__(58);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__ = __webpack_require__(30);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_6_lodash__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ReferenceListComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};







var ReferenceListComponent = (function () {
    function ReferenceListComponent(confirmationService, cvService, substanceEditService, utilService) {
        this.confirmationService = confirmationService;
        this.cvService = cvService;
        this.substanceEditService = substanceEditService;
        this.utilService = utilService;
        this.refs = [];
        this.access = [];
        this.sourceTypes = [];
    }
    ReferenceListComponent.prototype.refresh = function () {
        this.ngOnInit();
    };
    ReferenceListComponent.prototype.ngOnInit = function () {
        this.getReferences();
        this.getAccess();
        this.getSourceTypes();
    };
    ReferenceListComponent.prototype.confirmDeleteRef = function (ref) {
        var _this = this;
        this.confirmationService.confirm({
            message: 'Do you want to delete this Reference?',
            header: 'Delete Confirmation',
            icon: 'fa fa-trash',
            accept: function () {
                _this.deleteReference(ref);
            }
        });
    };
    ReferenceListComponent.prototype.findSelectedRefIndex = function (ref) {
        return this.refs.indexOf(ref);
    };
    ReferenceListComponent.prototype.deleteReference = function (ref) {
        this.refs.splice(this.findSelectedRefIndex(ref), 1);
        __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */].refs = this.refs;
    };
    ReferenceListComponent.prototype.addReference = function (top) {
        console.log("add reference");
        this.newRef = true;
        this.nRef = new __WEBPACK_IMPORTED_MODULE_1__reference__["a" /* Reference */]();
        this.nRef.generateNewUuid();
        this.nRef.setFlag("new", true);
        this.nRef.access = [];
        this.nRef.index = this.refs.length + 1;
        if (top) {
            this.refs.unshift(this.nRef);
        }
        else {
            this.refs.push(this.nRef);
        }
        __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */].refs = this.refs;
    };
    ReferenceListComponent.prototype.exportRefs = function () {
        console.log("export refs");
    };
    ReferenceListComponent.prototype.search = function () {
        console.log("search refs");
    };
    ReferenceListComponent.prototype.getReferences = function () {
        var _this = this;
        this.substanceEditService
            .getReferences()
            .then(function (refs) {
            _this.refs = refs;
            _this.refs = __WEBPACK_IMPORTED_MODULE_6_lodash__["orderBy"](_this.refs, ['citation'], ['asc']);
            _this.refs.sort(function (a, b) {
                return a.citation.localeCompare(b.citation);
            });
        });
    };
    ReferenceListComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    ReferenceListComponent.prototype.getSourceTypes = function () {
        var _this = this;
        this.cvService
            .getList("DOCUMENT_TYPE")
            .then(function (sourcetype) { return _this.sourceTypes = sourcetype; });
    };
    ReferenceListComponent.prototype.saveRefListChanges = function ($event, refs) {
        console.log("save ref list changes");
        console.log(refs);
    };
    ReferenceListComponent.prototype.changeAccess = function ($event, ref) {
        this.utilService.changeAccess($event, ref);
    };
    ReferenceListComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'reference-list',
            template: __webpack_require__(987),
            providers: [__WEBPACK_IMPORTED_MODULE_2_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */], __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2_primeng_primeng__["ConfirmationService"]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */]) === 'function' && _d) || Object])
    ], ReferenceListComponent);
    return ReferenceListComponent;
    var _a, _b, _c, _d;
}());
//# sourceMappingURL=reference-list.component.js.map

/***/ }),

/***/ 488:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__reference__ = __webpack_require__(146);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_2_lodash__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ReferenceListService; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var ReferenceListService = (function () {
    function ReferenceListService() {
    }
    //TODO: still need to add the logic, handle User session
    //gets all the references for a record
    /*  getAllReferences(uuid: string): Promise<Reference[]> {
        //should do something to pass the UUID somewhere
        return new Promise((resolve, reject) => {
                window["SubstanceFinder"].get(uuid)
                       .andThen(s=>s.fetch("/references!sort(created)"))
                       .andThen(refs=>{
                            var i=1;
                            var mrefs=_.chain(refs)
                                     .map(r=>new Reference().merge(r))
                                     .map(r=>{r.index=i++;return r;})
                                     .value();
                            return mrefs;
                        })
                       .get(refs=>resolve(refs));
            });
      }*/
    //gets the last 5 references by username "admin" for now
    //TODO: Needs sort
    ReferenceListService.prototype.getLastFiveReferences = function (username) {
        console.log("ref list service-last 5 refs");
        console.log(username);
        return new Promise(function (resolve, reject) {
            window["ReferenceFinder"]
                .searchByLastEdited(username)
                .andThen(function (cnt) { return cnt.content; })
                .andThen(function (refs) {
                var i = 1;
                var mrefs = __WEBPACK_IMPORTED_MODULE_2_lodash__["chain"](refs)
                    .map(function (r) { return new __WEBPACK_IMPORTED_MODULE_0__reference__["a" /* Reference */]().merge(r); })
                    .map(function (r) { r.index = i++; return r; })
                    .slice(0, 5)
                    .value();
                return mrefs;
            })
                .get(function (refs) { return resolve(refs); });
        });
    };
    ReferenceListService.prototype.saveChanges = function () {
        //save references
    };
    ReferenceListService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_1__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], ReferenceListService);
    return ReferenceListService;
}());
//# sourceMappingURL=reference-list.service.js.map

/***/ }),

/***/ 489:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__ = __webpack_require__(30);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ReferenceEditDialog; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var ReferenceEditDialog = (function () {
    function ReferenceEditDialog(cvService, utilService) {
        this.cvService = cvService;
        this.utilService = utilService;
        this.sourceTypeList = [];
        this.documentSystemType = [];
        this.access = [];
        this.tags = [];
        this.close = function () { };
    }
    ReferenceEditDialog.prototype.updateRef = function (ref, event) {
        event.preventDefault();
        //TODO:should do something interesting here
        //right now, we are always modifying things, no matter what
        this.close();
    };
    ReferenceEditDialog.prototype.deleteRef = function (selectedReference) {
        selectedReference.setFlag("delete", true);
        this.close();
    };
    ReferenceEditDialog.prototype.getSourceType = function () {
        var _this = this;
        this.cvService
            .getList("DOCUMENT_TYPE")
            .then(function (st) { return _this.sourceTypeList = st; });
    };
    ReferenceEditDialog.prototype.getDocumentSystemType = function () {
        var _this = this;
        this.cvService
            .getList("DOCUMENT_SYSTEM_TYPE")
            .then(function (st) { return _this.documentSystemType = st; });
    };
    ReferenceEditDialog.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    ReferenceEditDialog.prototype.getTags = function () {
        var _this = this;
        this.cvService
            .getList("DOCUMENT_COLLECTION")
            .then(function (tags) { return _this.tags = tags; });
    };
    ReferenceEditDialog.prototype.ngOnInit = function () {
        this.getSourceType();
        this.getDocumentSystemType();
        this.getAccess();
        this.getTags();
    };
    ReferenceEditDialog.prototype.changeAccess = function ($event, ref) {
        this.utilService.changeAccess($event, ref);
    };
    ReferenceEditDialog = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'reference-edit-dialog',
            template: __webpack_require__(984),
            //styleUrls: ['../styles.css']
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */]) === 'function' && _b) || Object])
    ], ReferenceEditDialog);
    return ReferenceEditDialog;
    var _a, _b;
}());
//# sourceMappingURL=reference.component.js.map

/***/ }),

/***/ 490:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__relationship__ = __webpack_require__(304);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_material__ = __webpack_require__(111);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__references_reference_list_dialog_component__ = __webpack_require__(145);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5_primeng_primeng__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_5_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__ = __webpack_require__(30);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_7_lodash__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__substanceedit_substanceedit_service__ = __webpack_require__(58);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return RelationshipListComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};









var RelationshipListComponent = (function () {
    function RelationshipListComponent(cvService, dialog, confirmationService, utilService, substanceEditService) {
        this.cvService = cvService;
        this.dialog = dialog;
        this.confirmationService = confirmationService;
        this.utilService = utilService;
        this.substanceEditService = substanceEditService;
        this.relations = [];
        this.references = [];
        this.access = [];
        this.relationshipTypes = [];
        this.filterRelationTypes = [];
        this.selectedFilterType = null;
        this.browserurl = this.utilService.getBrowserUrl();
    }
    RelationshipListComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    RelationshipListComponent.prototype.getRelationshipTypes = function () {
        var _this = this;
        this.cvService
            .getList("RELATIONSHIP_TYPE")
            .then(function (relTypes) { return _this.relationshipTypes = relTypes; });
    };
    RelationshipListComponent.prototype.ngOnInit = function () {
        this.getReferences();
        this.getAccess();
        this.getRelationshipTypes();
        this.getRelationships();
        this.loadOptions();
        this.selectedFilterType = "";
    };
    RelationshipListComponent.prototype.getReferences = function () {
        var _this = this;
        this.substanceEditService
            .getReferences()
            .then(function (refs) {
            _this.references = refs;
        });
    };
    RelationshipListComponent.prototype.showReferenceIndexes = function (data) {
        if (data.references) {
            return __WEBPACK_IMPORTED_MODULE_7_lodash__["chain"](this.references)
                .filter(function (r) { return (data.references.indexOf(r.uuid) >= 0); })
                .map("index")
                .value();
        }
        else {
            return "";
        }
    };
    RelationshipListComponent.prototype.openRefListDialog = function (relation) {
        console.log("relationship list");
        var config = new __WEBPACK_IMPORTED_MODULE_3__angular_material__["e" /* MdDialogConfig */]();
        config.width = '400px';
        var dialogRef = this.dialog.open(__WEBPACK_IMPORTED_MODULE_4__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], config);
        //TODO: should have a cleaner initialization process
        dialogRef.componentInstance.allReferences = this.references;
        console.log(relation);
        dialogRef.componentInstance.data = relation;
        dialogRef.componentInstance.closeFunction = function () {
            dialogRef.close();
        };
    };
    RelationshipListComponent.prototype.addRelationship = function (top) {
        var nRelation = new __WEBPACK_IMPORTED_MODULE_1__relationship__["a" /* Relationship */]();
        nRelation = new __WEBPACK_IMPORTED_MODULE_1__relationship__["a" /* Relationship */]();
        nRelation.access = ['protected'];
        nRelation.references = [];
        nRelation.generateNewUuid();
        nRelation.relatedSubstance = null;
        nRelation.type = this.selectedFilterType;
        if (top) {
            this.relations.unshift(nRelation);
        }
        else {
            this.relations.push(nRelation);
        }
        console.log("add new relationship");
    };
    RelationshipListComponent.prototype.findSelectedRelationIndex = function (relation) {
        return this.relations.indexOf(relation);
    };
    RelationshipListComponent.prototype.deleteRelation = function (relation) {
        console.log("delete:");
        console.log(relation);
        this.relations.splice(this.findSelectedRelationIndex(relation), 1);
        console.log(this.relations);
    };
    RelationshipListComponent.prototype.confirmDeleteRelation = function (relation) {
        var _this = this;
        this.confirmationService.confirm({
            message: 'Do you want to delete this relationship?',
            header: 'Delete Confirmation',
            icon: 'fa fa-trash',
            accept: function () {
                console.log("confirm delete relation");
                _this.deleteRelation(relation);
            }
        });
    };
    RelationshipListComponent.prototype.getRelationships = function () {
        var _this = this;
        console.log("relationships...");
        this.substanceEditService
            .getRelationships()
            .then(function (rels) {
            _this.relations = rels;
            _this.loadOptions();
            //  this.relations = _.orderBy(this.relations, ['relatedSubstance.refPname'],['asc']);
            _this.relations.sort(function (a, b) {
                return a.relatedSubstance.refPname.localeCompare(b.relatedSubstance.refPname);
            });
        });
    };
    RelationshipListComponent.prototype.changeAccess = function ($event, relation) {
        this.utilService.changeAccess($event, relation);
    };
    RelationshipListComponent.prototype.showAmounts = function (amount) {
        return this.utilService.displayAmount(amount);
    };
    RelationshipListComponent.prototype.loadOptions = function () {
        var _this = this;
        var result = this.relationshipTypes
            .filter(function (o1) { return _this.relations.some(function (o2) { return o1.value === o2.type; }); })
            .sort(function (a, b) { return -a.label.localeCompare(b.label); });
        this.filterRelationTypes = result;
        this.filterRelationTypes.push({ label: "All Types", value: "" });
        this.filterRelationTypes.reverse();
    };
    RelationshipListComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'relationship-list',
            template: __webpack_require__(989),
            providers: [__WEBPACK_IMPORTED_MODULE_5_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_8__substanceedit_substanceedit_service__["a" /* SubstanceEditService */], __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_3__angular_material__["d" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__angular_material__["d" /* MdDialog */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_5_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5_primeng_primeng__["ConfirmationService"]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__["a" /* UtilService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_8__substanceedit_substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_8__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _e) || Object])
    ], RelationshipListComponent);
    return RelationshipListComponent;
    var _a, _b, _c, _d, _e;
}());
/*
 readFilterType(value: string){
 console.log("selected filter type");
 this.selectedFilterType = value;
 }*/
/*  showStructure(): void {
 console.log("showStructure");
 this.showRSStruct = !this.showRSStruct;
 console.log(this.showRSStruct);
 }*/
//# sourceMappingURL=relationship-list.component.js.map

/***/ }),

/***/ 491:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__substanceedit_service__ = __webpack_require__(58);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_router__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_4_lodash__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__ = __webpack_require__(30);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__angular_platform_browser__ = __webpack_require__(43);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return SubstanceeditComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};







var SubstanceeditComponent = (function () {
    function SubstanceeditComponent(cvService, substanceEditService, route, utilService, titleService) {
        this.cvService = cvService;
        this.substanceEditService = substanceEditService;
        this.route = route;
        this.utilService = utilService;
        this.titleService = titleService;
        this.msgs = [];
        //msgs: Message[][] = [];
        this.tabs = [];
        this.message = "";
        this.names = [];
        this.displayName = "";
        this.warningAck = false;
        this.state = "clean";
        this.load = this.substanceEditService.load;
        this.browserurl = this.utilService.getBrowserUrl();
    }
    SubstanceeditComponent.prototype.getTabs = function () {
        var _this = this;
        this.cvService.getTabs().then(function (tabs) {
            _this.tabs = tabs;
        });
    };
    SubstanceeditComponent.prototype.tabChange = function () {
        console.log("tab change");
        this.message = "";
        this.msgs = [];
    };
    SubstanceeditComponent.prototype.ngOnInit = function () {
        window["GGlob"].GlobalSettings.setBaseURL(this.browserurl + this.utilService.appContxt + "/api/v1/");
        this.uuid = this.route.snapshot.params['id'];
        this.getTabs();
        this.substanceEditService.setUuid(this.uuid);
        this.getDisplayName();
    };
    SubstanceeditComponent.prototype.getDisplayName = function () {
        var _this = this;
        this.substanceEditService
            .getFullJson()
            .then(function (s) {
            // this.substance = s;
            _this.displayName = s._name.toString();
            _this.titleService.setTitle(_this.displayName ? _this.displayName : "GSRS");
        });
        return this.displayName;
    };
    SubstanceeditComponent.prototype.save = function () {
        var _this = this;
        this.state = "submitting";
        this.msgs = [];
        this.substanceEditService.getFullJson()
            .then(function (s) {
            window["GGlob"]
                .SubstanceBuilder
                .fromSimple(s)
                .save()
                .get(function (r) {
                console.log(r);
                var detail = "";
                var messages = __WEBPACK_IMPORTED_MODULE_4_lodash__["chain"](r.validationMessages)
                    .filter(function (ms) { return ms.messageType != "INFO"; })
                    .value();
                r.validationMessages = messages;
                if (r.isError) {
                    console.log("error");
                    _this.state = "validated-error";
                    detail = "Submission Failed:  ";
                    detail += r.type;
                    _this.showMessage(r.message, r.message, r.type, r.links);
                }
                else if (r.valid === false) {
                    console.log("invalid");
                    detail = "Submission Failed:  ";
                    _this.state = "validated-error";
                    for (var _i = 0, _a = r.validationMessages; _i < _a.length; _i++) {
                        var m = _a[_i];
                        detail += m.message;
                        _this.showMessage(m.messageType, m.messageType, m.message, m.links);
                    }
                }
                else if (r.valid === true && r.validationMessages.length > 0) {
                    for (var _b = 0, _c = r.validationMessages; _b < _c.length; _b++) {
                        var m = _c[_b];
                        detail += m.message + "\n";
                        _this.showMessage(m.messageType, m.messageType, m.message, m.links);
                    }
                }
                else {
                    _this.state = "submitted";
                    console.log("success");
                    detail = "Submission Success";
                    _this.showMessage("success", "", detail);
                    _this.substanceEditService.refreshApi();
                }
                _this.message = detail;
            });
        });
    };
    SubstanceeditComponent.prototype.validate = function (validation) {
        var _this = this;
        this.state = "validating";
        //this.validated = true;
        //this.submitted = false;
        this.msgs = [];
        this.substanceEditService.getFullJson()
            .then(function (s) {
            console.log("substance json");
            console.log(s);
            window["GGlob"]
                .SubstanceBuilder
                .fromSimple(s)
                .validate(validation)
                .get(function (r) {
                console.log(r);
                var detail = "";
                var messages = __WEBPACK_IMPORTED_MODULE_4_lodash__["chain"](r.validationMessages)
                    .filter(function (ms) { return ms.messageType != "INFO"; })
                    .value();
                r.validationMessages = messages;
                if (r.isError) {
                    _this.state = "validated-error";
                    console.log("error");
                    detail = "Submission Failed: " + r.type;
                    _this.showMessage(r.message, r.message, r.type, r.links);
                }
                else if (r.valid === false) {
                    _this.state = "validated-error";
                    console.log("invalid");
                    detail = "Submission Failed:  ";
                    for (var _i = 0, _a = r.validationMessages; _i < _a.length; _i++) {
                        var m = _a[_i];
                        detail += m.message;
                        _this.showMessage(m.messageType, m.messageType, m.message, m.links);
                    }
                }
                else if (r.valid === true) {
                    if (r.validationMessages.length > 0) {
                        for (var _b = 0, _c = r.validationMessages; _b < _c.length; _b++) {
                            var m = _c[_b];
                            _this.showMessage(m.messageType, m.messageType, m.message, m.links);
                        }
                        _this.state = "validated-warning";
                    }
                    else if (r.validationMessages.length == 0) {
                        _this.save();
                    }
                }
                _this.message = detail;
            });
        });
    };
    SubstanceeditComponent.prototype.showMessage = function (sev, summary, detail, links) {
        console.log("show message");
        this.msgs.push({ links: links, msgs: [{ severity: sev, summary: __WEBPACK_IMPORTED_MODULE_4_lodash__["startCase"](__WEBPACK_IMPORTED_MODULE_4_lodash__["toLower"](summary)), detail: detail }] });
    };
    SubstanceeditComponent.prototype.msgclose = function (msgs) {
        //if(!this.submitted) {
        this.warningAck = true;
        //}
    };
    SubstanceeditComponent.prototype.closemsg = function (msg) {
        console.log("close");
        /*  this.warningAck = true;
          this.msgs = _.chain(this.msgs)
            .filter(m=>m!==msg)
            .value();*/
        this.state = "clean";
    };
    SubstanceeditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'substanceedit',
            template: __webpack_require__(990),
            styles: [__webpack_require__(908)],
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_2__substanceedit_service__["a" /* SubstanceEditService */], __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__angular_router__["ActivatedRoute"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__angular_router__["ActivatedRoute"]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_6__angular_platform_browser__["Title"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__angular_platform_browser__["Title"]) === 'function' && _e) || Object])
    ], SubstanceeditComponent);
    return SubstanceeditComponent;
    var _a, _b, _c, _d, _e;
}());
//# sourceMappingURL=substanceedit.component.js.map

/***/ }),

/***/ 492:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return SubstancelistComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};

var SubstancelistComponent = (function () {
    function SubstancelistComponent() {
    }
    SubstancelistComponent.prototype.ngOnInit = function () {
    };
    SubstancelistComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'app-substancelist',
            template: __webpack_require__(991),
            styles: [__webpack_require__(909)]
        }), 
        __metadata('design:paramtypes', [])
    ], SubstancelistComponent);
    return SubstancelistComponent;
}());
//# sourceMappingURL=substancelist.component.js.map

/***/ }),

/***/ 493:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap__ = __webpack_require__(156);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_router__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_platform_browser__ = __webpack_require__(43);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__ = __webpack_require__(58);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__ = __webpack_require__(30);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return SummaryComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};






var SummaryComponent = (function () {
    function SummaryComponent(route, router, sanitizer, substanceEditService, utilService) {
        this.route = route;
        this.router = router;
        this.sanitizer = sanitizer;
        this.substanceEditService = substanceEditService;
        this.utilService = utilService;
        this.recordUUID = "";
        this.icreate = 0;
        this.iframe = null;
        this.loaded = false;
        this.attached = true;
        console.log("Making a new Component");
        SummaryComponent.creation++;
        this.icreate = SummaryComponent.creation;
    }
    SummaryComponent.prototype.refresh = function () {
        if (this.attached)
            return;
        var me = this;
        this.substanceEditService.getFullJson()
            .then(function (s) {
            console.log("Setting JSON in leg forms");
            me.setLegacyJson(JSON.parse(JSON.stringify(s)));
        });
        document.getElementById("hidden-things").style.display = "";
        this.attached = true;
    };
    SummaryComponent.prototype.detach = function () {
        if (!this.attached)
            return;
        var me = this;
        console.log("Getting JSON from leg forms");
        this.substanceEditService.setFullJson(JSON.parse(JSON.stringify(me.getLegacyJson())));
        document.getElementById("hidden-things").style.display = "none";
        this.attached = false;
    };
    SummaryComponent.prototype.ngOnInit = function () {
        console.log("Making a new Component - init");
        var me = this;
        this.iframe = document.getElementById("subFrame");
        this.iframe.onload = function () {
            me.onLoad();
        };
        document.getElementById("sum-internal").removeChild(this.iframe);
        document.getElementById("hidden-things").appendChild(this.iframe);
        this.attached = true;
        if (!this.loaded) {
            //document.getElementById("hidden-things").appendChild(this.iframe);
            this.recordUUID = this.route.parent.snapshot.params['id'];
            console.log(this.recordUUID);
            this.url = window.location.href.split(this.utilService.appContxt)[0] + this.utilService.appContxt + "/substance/" + this.recordUUID + "/edit";
            console.log(this.url);
            this.iframe.src = this.url;
            this.loaded = true;
        }
        else {
            this.onLoad();
        }
    };
    SummaryComponent.prototype.getLegacyJson = function () {
        console.log("get legacy json");
        console.log(this.iframe["contentWindow"].getJson());
        return this.iframe["contentWindow"].getJson();
    };
    SummaryComponent.prototype.setLegacyJson = function (json) {
        console.log("set json");
        console.log(json);
        return this.iframe["contentWindow"].setJson(JSON.parse(JSON.stringify(json)));
    };
    SummaryComponent.prototype.onLoad = function () {
        var _this = this;
        this.substanceEditService.getFullJson()
            .then(function (s) {
            _this.setLegacyJson(s);
        });
        //read the json object
        // document.getElementById('subFrame').contentWindow.setJson();
    };
    SummaryComponent.creation = 0;
    SummaryComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'summary-view',
            template: __webpack_require__(992),
            providers: [__WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__angular_router__["ActivatedRoute"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__angular_router__["ActivatedRoute"]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__angular_router__["Router"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__angular_router__["Router"]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__angular_platform_browser__["DomSanitizer"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__angular_platform_browser__["DomSanitizer"]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */]) === 'function' && _e) || Object])
    ], SummaryComponent);
    return SummaryComponent;
    var _a, _b, _c, _d, _e;
}());
//# sourceMappingURL=summary.component.js.map

/***/ }),

/***/ 578:
/***/ (function(module, exports) {

function webpackEmptyContext(req) {
	throw new Error("Cannot find module '" + req + "'.");
}
webpackEmptyContext.keys = function() { return []; };
webpackEmptyContext.resolve = webpackEmptyContext;
module.exports = webpackEmptyContext;
webpackEmptyContext.id = 578;


/***/ }),

/***/ 579:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_platform_browser_dynamic__ = __webpack_require__(705);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__app_app_module__ = __webpack_require__(737);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__environments_environment__ = __webpack_require__(750);




if (__WEBPACK_IMPORTED_MODULE_3__environments_environment__["a" /* environment */].production) {
    __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["enableProdMode"])();
}
__webpack_require__.i(__WEBPACK_IMPORTED_MODULE_1__angular_platform_browser_dynamic__["a" /* platformBrowserDynamic */])().bootstrapModule(__WEBPACK_IMPORTED_MODULE_2__app_app_module__["a" /* AppModule */]);
//# sourceMappingURL=main.js.map

/***/ }),

/***/ 58:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__relationships_relationship__ = __webpack_require__(304);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__codes_code__ = __webpack_require__(302);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__references_reference__ = __webpack_require__(146);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__names_name__ = __webpack_require__(303);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_5_lodash__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return SubstanceEditService; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};






var SubstanceEditService = (function () {
    function SubstanceEditService() {
        this.load = "done";
    }
    SubstanceEditService.prototype.setUuid = function (uuid) {
        SubstanceEditService.uuid = uuid;
    };
    SubstanceEditService.prototype.getNames = function () {
        var _this = this;
        if (SubstanceEditService.names !== null)
            return Promise.resolve(SubstanceEditService.names);
        this.load = "loading";
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(SubstanceEditService.uuid)
                .andThen(function (s) { return s.fetch("names"); })
                .andThen(function (names) { return _this.storeNames(names); })
                .get(function (names) { return resolve(names); });
            _this.load = "done";
        });
    };
    SubstanceEditService.prototype.getReferences = function () {
        var _this = this;
        if (SubstanceEditService.refs !== null)
            return Promise.resolve(SubstanceEditService.refs);
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(SubstanceEditService.uuid)
                .andThen(function (s) { return s.fetch("references!sort(lastEdited)"); })
                .andThen(function (refs) { return _this.storeReferences(refs); })
                .get(function (refs) { return resolve(refs); });
        });
    };
    SubstanceEditService.prototype.getCodes = function () {
        var _this = this;
        if (SubstanceEditService.codes !== null)
            return Promise.resolve(SubstanceEditService.codes);
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(SubstanceEditService.uuid)
                .andThen(function (s) { return s.fetch("codes"); })
                .andThen(function (codes) { return _this.storeCodes(codes); })
                .get(function (codes) { return resolve(codes); });
        });
    };
    SubstanceEditService.prototype.getRelationships = function () {
        var _this = this;
        if (SubstanceEditService.relations !== null)
            return Promise.resolve(SubstanceEditService.relations);
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(SubstanceEditService.uuid)
                .andThen(function (s) { return s.fetch("relationships"); })
                .andThen(function (rels) { return _this.storeRelationships(rels); })
                .get(function (rels) { return resolve(rels); });
        });
    };
    SubstanceEditService.prototype._getFullJson = function () {
        if (SubstanceEditService.substance)
            return Promise.resolve(SubstanceEditService.substance);
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(SubstanceEditService.uuid)
                .andThen(function (s) { return s.full(); })
                .andThen(function (sub) {
                SubstanceEditService.substance = sub;
            })
                .get(function (sub) { return resolve(sub); });
        });
    };
    SubstanceEditService.prototype.refreshApi = function () {
        var _this = this;
        console.log("refresh");
        window["SubstanceFinder"].get(SubstanceEditService.uuid)
            .andThen(function (s) { return s.full(); })
            .andThen(function (sub) {
            _this.setFullJson(sub);
        }).get(function () { });
    };
    SubstanceEditService.prototype.getFullJson = function () {
        return this._getFullJson()
            .then(function (s) {
            if (SubstanceEditService.names)
                s["names"] = SubstanceEditService.names;
            if (SubstanceEditService.refs)
                s["references"] = SubstanceEditService.refs;
            if (SubstanceEditService.codes)
                s["codes"] = SubstanceEditService.codes;
            if (SubstanceEditService.relations)
                s["relationships"] = SubstanceEditService.relations;
            return s;
        });
    };
    SubstanceEditService.prototype.storeCodes = function (codes) {
        var cds = __WEBPACK_IMPORTED_MODULE_5_lodash__["map"](codes, function (n) {
            return (new __WEBPACK_IMPORTED_MODULE_2__codes_code__["a" /* Code */]().merge(n));
        });
        if (!SubstanceEditService.codes) {
            SubstanceEditService.codes = cds;
        }
        else {
            console.log("length");
            console.log(SubstanceEditService.codes.length);
            //SubstanceEditService.codes.slice(-SubstanceEditService.codes.length);
            SubstanceEditService.codes.length = 0;
            __WEBPACK_IMPORTED_MODULE_5_lodash__["forEach"](cds, function (c) {
                SubstanceEditService.codes.push(c);
            });
        }
        return SubstanceEditService.codes;
    };
    SubstanceEditService.prototype.storeRelationships = function (relations) {
        var rels = __WEBPACK_IMPORTED_MODULE_5_lodash__["map"](relations, function (r) {
            return (new __WEBPACK_IMPORTED_MODULE_1__relationships_relationship__["a" /* Relationship */]().merge(r));
        });
        if (!SubstanceEditService.relations) {
            SubstanceEditService.relations = rels;
        }
        else {
            SubstanceEditService.relations.length = 0;
            __WEBPACK_IMPORTED_MODULE_5_lodash__["forEach"](rels, function (n) {
                SubstanceEditService.relations.push(n);
            });
        }
        return SubstanceEditService.relations;
    };
    SubstanceEditService.prototype.storeNames = function (names) {
        var mnames = __WEBPACK_IMPORTED_MODULE_5_lodash__["map"](names, function (n) {
            return (new __WEBPACK_IMPORTED_MODULE_4__names_name__["a" /* Name */]().merge(n));
        });
        if (!SubstanceEditService.names) {
            SubstanceEditService.names = mnames;
        }
        else {
            //SubstanceEditService.names.slice(-SubstanceEditService.names.length);
            SubstanceEditService.names.length = 0;
            __WEBPACK_IMPORTED_MODULE_5_lodash__["forEach"](mnames, function (n) {
                SubstanceEditService.names.push(n);
            });
        }
        return SubstanceEditService.names;
    };
    SubstanceEditService.prototype.storeReferences = function (refs) {
        var i = 1;
        var mrefs = __WEBPACK_IMPORTED_MODULE_5_lodash__["chain"](refs)
            .map(function (r) { return new __WEBPACK_IMPORTED_MODULE_3__references_reference__["a" /* Reference */]().merge(r); })
            .map(function (r) { r.index = i++; return r; })
            .value();
        SubstanceEditService.refs = mrefs;
        return mrefs;
    };
    SubstanceEditService.prototype.setFullJson = function (substance) {
        SubstanceEditService.substance = substance;
        console.log("version");
        console.log(substance.version);
        this.storeCodes(substance["codes"]);
        this.storeNames(substance["names"]);
        this.storeReferences(substance["references"]);
    };
    SubstanceEditService.prototype.deleteName = function (name) {
        console.log("delete name service");
        var loc = SubstanceEditService.names.indexOf(name);
        if (loc > -1) {
            SubstanceEditService.names.splice(loc, 1);
        }
        return SubstanceEditService.names;
    };
    SubstanceEditService.prototype.deleteCode = function (code) {
        console.log("delete code service");
        var loc = SubstanceEditService.codes.indexOf(code);
        if (loc > -1) {
            SubstanceEditService.codes.splice(loc, 1);
        }
        return SubstanceEditService.codes;
    };
    SubstanceEditService.prototype.deleteRelationship = function (relation) {
        console.log("delete relation service");
        var loc = SubstanceEditService.relations.indexOf(relation);
        if (loc > -1) {
            SubstanceEditService.relations.splice(loc, 1);
        }
        return SubstanceEditService.relations;
    };
    SubstanceEditService.prototype.deleteReference = function (ref) {
        console.log("delete ref service");
        var loc = SubstanceEditService.refs.indexOf(ref);
        if (loc > -1) {
            SubstanceEditService.refs.splice(loc, 1);
        }
        return SubstanceEditService.refs;
    };
    SubstanceEditService.names = null;
    SubstanceEditService.refs = null;
    SubstanceEditService.codes = null;
    SubstanceEditService.relations = null;
    SubstanceEditService.substance = "";
    SubstanceEditService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], SubstanceEditService);
    return SubstanceEditService;
}());
//# sourceMappingURL=substanceedit.service.js.map

/***/ }),

/***/ 734:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__amount_model__ = __webpack_require__(481);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AmountComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var AmountComponent = (function () {
    function AmountComponent(cvService) {
        this.cvService = cvService;
        this.onSaved = new __WEBPACK_IMPORTED_MODULE_0__angular_core__["EventEmitter"]();
        /*  @Input()
          parent: any;*/
        this.amountType = [];
        this.amountUnits = [];
        this.amountDisplay = true;
        this.getAmountType();
        this.getAmountUnits();
    }
    AmountComponent.prototype.getAmountType = function () {
        var _this = this;
        console.log("amount types.................");
        this.cvService
            .getList("AMOUNT_TYPE")
            .then(function (at) { return _this.amountType = at; });
    };
    AmountComponent.prototype.getAmountUnits = function () {
        var _this = this;
        console.log("amount units.................");
        this.cvService
            .getList("AMOUNT_UNIT")
            .then(function (units) { return _this.amountUnits = units; });
    };
    AmountComponent.prototype.ngOnInit = function () {
        if (!this.amount) {
            this.amount = new __WEBPACK_IMPORTED_MODULE_2__amount_model__["a" /* Amount */]();
        }
    };
    AmountComponent.prototype.clearAmount = function () {
        this.amount = new __WEBPACK_IMPORTED_MODULE_2__amount_model__["a" /* Amount */]();
        this.onSaved.emit(this.amount);
    };
    AmountComponent.prototype.saveAmount = function (amount) {
        this.onSaved.emit(amount);
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', Object)
    ], AmountComponent.prototype, "amount", void 0);
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Output"])(), 
        __metadata('design:type', Object)
    ], AmountComponent.prototype, "onSaved", void 0);
    AmountComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'amount',
            template: __webpack_require__(974),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object])
    ], AmountComponent);
    return AmountComponent;
    var _a;
}());
//# sourceMappingURL=amount.component.js.map

/***/ }),

/***/ 735:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_router__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__names_name_list_component__ = __webpack_require__(483);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__codes_code_list_component__ = __webpack_require__(482);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__relationships_relationship_list_component__ = __webpack_require__(490);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__summaryview_summary_component__ = __webpack_require__(493);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__references_reference_list_component__ = __webpack_require__(487);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7__substancelist_substancelist_component__ = __webpack_require__(492);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__substanceedit_substanceedit_component__ = __webpack_require__(491);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9__route_reuse_strategy__ = __webpack_require__(749);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AppRoutingModule; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};










var appRoutes = [
    //{path: 'substances', component: SubstanceList },
    //  { path: 'substance/:id', component: AppComponent},
    /*  { path: ':id',
       component: AppComponent },*/
    /*  { path: 'test/:id', component: NameListTestComponent,
        resolve: {
          substance: SubstanceDetailResolver
        }},*/
    {
        path: 'substance/:id',
        component: __WEBPACK_IMPORTED_MODULE_8__substanceedit_substanceedit_component__["a" /* SubstanceeditComponent */],
        children: [
            { path: 'names', component: __WEBPACK_IMPORTED_MODULE_2__names_name_list_component__["a" /* NameListComponent */] },
            { path: 'references', component: __WEBPACK_IMPORTED_MODULE_6__references_reference_list_component__["a" /* ReferenceListComponent */] },
            { path: 'codes', component: __WEBPACK_IMPORTED_MODULE_3__codes_code_list_component__["a" /* CodeListComponent */] },
            { path: 'relationships', component: __WEBPACK_IMPORTED_MODULE_4__relationships_relationship_list_component__["a" /* RelationshipListComponent */] },
            /* { path: 'notes', component: NotesComponent },
             { path: 'properties', component: PropertiesListComponent },*/
            { path: 'summary', component: __WEBPACK_IMPORTED_MODULE_5__summaryview_summary_component__["a" /* SummaryComponent */] }
        ]
    },
    { path: 'substances', component: __WEBPACK_IMPORTED_MODULE_7__substancelist_substancelist_component__["a" /* SubstancelistComponent */] },
    { path: '',
        redirectTo: '/substances',
        pathMatch: 'full'
    },
];
var AppRoutingModule = (function () {
    function AppRoutingModule() {
    }
    AppRoutingModule = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["NgModule"])({
            imports: [__WEBPACK_IMPORTED_MODULE_1__angular_router__["RouterModule"].forRoot(appRoutes, { useHash: true, enableTracing: true })],
            exports: [__WEBPACK_IMPORTED_MODULE_1__angular_router__["RouterModule"]],
            providers: [
                {
                    provide: __WEBPACK_IMPORTED_MODULE_1__angular_router__["RouteReuseStrategy"],
                    useClass: __WEBPACK_IMPORTED_MODULE_9__route_reuse_strategy__["a" /* CustomRouteReuseStrategy */]
                }
            ]
        }), 
        __metadata('design:paramtypes', [])
    ], AppRoutingModule);
    return AppRoutingModule;
}());
//# sourceMappingURL=app-routing.module.js.map

/***/ }),

/***/ 736:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap__ = __webpack_require__(156);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__ = __webpack_require__(30);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AppComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var AppComponent = (function () {
    function AppComponent(utilService) {
        this.utilService = utilService;
        this.browserurl = this.utilService.getBrowserUrl() + this.utilService.appContxt;
    }
    AppComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'gsrs-app',
            template: "\n<h1 class=\"title\">G-SRS</h1>\n<br/>\n <nav>\n      <!--<a routerLink=\"/browse\" routerLinkActive=\"active\">Browser</a>\n      <a routerLink=\"/search\" routerLinkActive=\"active\">Search</a>\n      <a routerLink=\"/admin\" routerLinkActive=\"active\">Admin</a>\n      <a routerLink=\"/login\" routerLinkActive=\"active\">Login</a>-->\n      <!--<a routerLink=\"/browse\" routerLinkActive=\"active\">Go Back</a>-->\n\n      <a href=\"{{browserurl}}\" target=\"_blank\">Back to GSRS Home</a> &nbsp; &nbsp;\n      <a href=\"{{browserurl}}/substances\" target=\"_blank\">Browse Substances</a>\n  </nav>\n  <br/>\n        <router-outlet></router-outlet>\n  ",
            providers: [__WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */]) === 'function' && _a) || Object])
    ], AppComponent);
    return AppComponent;
    var _a;
}());
/*
@Component({
  selector: 'gsrs-app',
  templateUrl: './app.component.html',
  providers: [CVService]
})

export class AppComponent implements OnInit {
  names: Name[];
  tabs: any[];

  uuid: string;

 // recordUUID: string = "00006eea-e2d2-4d79-99ff-30f17b3dd740";

  constructor(private cvService: CVService
             ) {
    //console.log(this.route);
   /!* this.route.params.switchMap((params: Params) =>
       // console.log(params)
      this.uuid = params['id']
    );*!/
  }


  getTabs(): void {
    this.cvService.getTabs().then(tabs => {
      this.tabs = tabs;
    });
  }

  ngOnInit(): void {
    this.getTabs();
  }




}
*/
//# sourceMappingURL=app.component.js.map

/***/ }),

/***/ 737:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_platform_browser__ = __webpack_require__(43);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_forms__ = __webpack_require__(13);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_http__ = __webpack_require__(108);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__angular_material__ = __webpack_require__(111);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__app_routing_module__ = __webpack_require__(735);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__app_component__ = __webpack_require__(736);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_hammerjs__ = __webpack_require__(911);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_hammerjs___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_7_hammerjs__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__references_reference_list_dialog_component__ = __webpack_require__(145);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9__references_reference_component__ = __webpack_require__(489);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_10__names_name_component__ = __webpack_require__(739);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_11__names_name_list_component__ = __webpack_require__(483);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_12__references_reference_list_component__ = __webpack_require__(487);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_13__codes_code_list_component__ = __webpack_require__(482);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_14_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_14_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_15__relationships_relationship_list_component__ = __webpack_require__(490);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_16__notes_notes_component__ = __webpack_require__(742);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_17__notes_notes_edit_component__ = __webpack_require__(741);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_18__properties_properties_list_component__ = __webpack_require__(745);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_19__codes_code_edit_component__ = __webpack_require__(738);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_20__references_reference_edit_component__ = __webpack_require__(747);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_21__relationships_relationship_edit_component__ = __webpack_require__(748);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_22__amount_amount_component__ = __webpack_require__(734);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_23__properties_property_edit_component__ = __webpack_require__(746);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_24__parameters_parameter_component__ = __webpack_require__(743);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_25__summaryview_summary_component__ = __webpack_require__(493);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_26__substancelist_substancelist_component__ = __webpack_require__(492);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_27__substanceedit_substanceedit_component__ = __webpack_require__(491);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AppModule; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




























var AppModule = (function () {
    function AppModule() {
    }
    AppModule = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_1__angular_core__["NgModule"])({
            declarations: [
                __WEBPACK_IMPORTED_MODULE_6__app_component__["a" /* AppComponent */], __WEBPACK_IMPORTED_MODULE_8__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], __WEBPACK_IMPORTED_MODULE_9__references_reference_component__["a" /* ReferenceEditDialog */], __WEBPACK_IMPORTED_MODULE_10__names_name_component__["a" /* NameEditComponent */], __WEBPACK_IMPORTED_MODULE_11__names_name_list_component__["a" /* NameListComponent */], __WEBPACK_IMPORTED_MODULE_12__references_reference_list_component__["a" /* ReferenceListComponent */],
                __WEBPACK_IMPORTED_MODULE_13__codes_code_list_component__["a" /* CodeListComponent */], __WEBPACK_IMPORTED_MODULE_15__relationships_relationship_list_component__["a" /* RelationshipListComponent */], __WEBPACK_IMPORTED_MODULE_16__notes_notes_component__["a" /* NotesComponent */], __WEBPACK_IMPORTED_MODULE_18__properties_properties_list_component__["a" /* PropertiesListComponent */], __WEBPACK_IMPORTED_MODULE_19__codes_code_edit_component__["a" /* CodeEditComponent */], __WEBPACK_IMPORTED_MODULE_20__references_reference_edit_component__["a" /* ReferenceEdit */], __WEBPACK_IMPORTED_MODULE_21__relationships_relationship_edit_component__["a" /* RelationshipEditComponent */], __WEBPACK_IMPORTED_MODULE_22__amount_amount_component__["a" /* AmountComponent */], __WEBPACK_IMPORTED_MODULE_17__notes_notes_edit_component__["a" /* NotesEditComponent */],
                __WEBPACK_IMPORTED_MODULE_18__properties_properties_list_component__["a" /* PropertiesListComponent */], __WEBPACK_IMPORTED_MODULE_23__properties_property_edit_component__["a" /* PropertyEditComponent */], __WEBPACK_IMPORTED_MODULE_24__parameters_parameter_component__["a" /* ParameterComponent */], __WEBPACK_IMPORTED_MODULE_25__summaryview_summary_component__["a" /* SummaryComponent */], __WEBPACK_IMPORTED_MODULE_26__substancelist_substancelist_component__["a" /* SubstancelistComponent */], __WEBPACK_IMPORTED_MODULE_27__substanceedit_substanceedit_component__["a" /* SubstanceeditComponent */]
            ],
            imports: [
                __WEBPACK_IMPORTED_MODULE_0__angular_platform_browser__["BrowserModule"], __WEBPACK_IMPORTED_MODULE_2__angular_forms__["FormsModule"], __WEBPACK_IMPORTED_MODULE_3__angular_http__["a" /* HttpModule */], __WEBPACK_IMPORTED_MODULE_4__angular_material__["a" /* MaterialModule */], __WEBPACK_IMPORTED_MODULE_2__angular_forms__["ReactiveFormsModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["ToggleButtonModule"],
                __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["MultiSelectModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["OverlayPanelModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["DataTableModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["SharedModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["TabViewModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["RadioButtonModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["CheckboxModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["DropdownModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["DataGridModule"],
                __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["ConfirmDialogModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["MessagesModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["FileUploadModule"], __WEBPACK_IMPORTED_MODULE_5__app_routing_module__["a" /* AppRoutingModule */], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["AutoCompleteModule"], __WEBPACK_IMPORTED_MODULE_4__angular_material__["b" /* MdTooltipModule */], __WEBPACK_IMPORTED_MODULE_4__angular_material__["c" /* MdMenuModule */]],
            providers: [],
            bootstrap: [__WEBPACK_IMPORTED_MODULE_6__app_component__["a" /* AppComponent */]],
            entryComponents: [__WEBPACK_IMPORTED_MODULE_8__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], __WEBPACK_IMPORTED_MODULE_9__references_reference_component__["a" /* ReferenceEditDialog */]]
        }), 
        __metadata('design:paramtypes', [])
    ], AppModule);
    return AppModule;
}());
//# sourceMappingURL=app.module.js.map

/***/ }),

/***/ 738:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__code__ = __webpack_require__(302);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__substanceedit_substanceedit_service__ = __webpack_require__(58);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__ = __webpack_require__(30);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return CodeEditComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var CodeEditComponent = (function () {
    function CodeEditComponent(cvService, substanceEditService, utilService) {
        this.cvService = cvService;
        this.substanceEditService = substanceEditService;
        this.utilService = utilService;
        this.codeSystem = [];
        this.codeSystemType = [];
        this.access = [];
        this.codeType = [];
    }
    CodeEditComponent.prototype.getCodeSystemType = function () {
        var _this = this;
        this.cvService
            .getList("CODE_SYSTEM_TYPE")
            .then(function (codeSystemType) { return _this.codeSystemType = codeSystemType; });
    };
    CodeEditComponent.prototype.getCodeSystem = function () {
        var _this = this;
        this.cvService
            .getList("CODE_SYSTEM")
            .then(function (cs) { return _this.codeSystem = cs; });
    };
    CodeEditComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    CodeEditComponent.prototype.getCodeType = function () {
        var _this = this;
        this.cvService
            .getList("CODE_TYPE")
            .then(function (type) { return _this.codeType = type; });
    };
    CodeEditComponent.prototype.ngOnInit = function () {
        this.getCodeSystem();
        this.getAccess();
        this.getCodeType();
        this.getCodeSystemType();
    };
    CodeEditComponent.prototype.deleteCode = function (code) {
        console.log("delete");
        this.substanceEditService.deleteCode(code);
    };
    CodeEditComponent.prototype.changeAccess = function ($event, code) {
        this.utilService.changeAccess($event, code);
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__code__["a" /* Code */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__code__["a" /* Code */]) === 'function' && _a) || Object)
    ], CodeEditComponent.prototype, "code", void 0);
    CodeEditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'code-edit',
            template: __webpack_require__(975),
            providers: [__WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_3__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__substanceedit_substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__["a" /* UtilService */]) === 'function' && _d) || Object])
    ], CodeEditComponent);
    return CodeEditComponent;
    var _a, _b, _c, _d;
}());
//# sourceMappingURL=code-edit.component.js.map

/***/ }),

/***/ 739:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__name__ = __webpack_require__(303);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__ = __webpack_require__(30);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__ = __webpack_require__(58);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_5_lodash__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return NameEditComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};






var NameEditComponent = (function () {
    function NameEditComponent(cvService, utilService, substanceEditService) {
        this.cvService = cvService;
        this.utilService = utilService;
        this.substanceEditService = substanceEditService;
        this.names = [];
        this.nameTypes = [];
        this.access = [];
        this.languages = [];
        this.nameOrgs = [];
        this.nameJuris = [];
        this.domains = [];
        this.getLanguages();
        this.getNameOrgs();
        this.getNameJurisdiction();
        this.getNameTypes();
        this.getAccess();
        this.getDomains();
    }
    NameEditComponent.prototype.getNameTypes = function () {
        var _this = this;
        this.cvService
            .getList("NAME_TYPE")
            .then(function (nameTypes) { return _this.nameTypes = nameTypes; });
    };
    NameEditComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) {
            _this.access = access;
        });
    };
    NameEditComponent.prototype.getLanguages = function () {
        var _this = this;
        this.cvService
            .getList("LANGUAGE")
            .then(function (lang) { return _this.languages = lang; });
    };
    NameEditComponent.prototype.getNameOrgs = function () {
        var _this = this;
        this.cvService
            .getList("NAME_ORG")
            .then(function (orgs) { return _this.nameOrgs = orgs; });
    };
    NameEditComponent.prototype.getDomains = function () {
        var _this = this;
        this.cvService
            .getList("NAME_DOMAIN")
            .then(function (domain) { return _this.domains = domain; });
    };
    NameEditComponent.prototype.getNameJurisdiction = function () {
        var _this = this;
        this.cvService
            .getList("JURISDICTION")
            .then(function (juris) { return _this.nameJuris = juris; });
    };
    NameEditComponent.prototype.ngOnInit = function () { };
    NameEditComponent.prototype.changeDisplay = function (name, $event) {
        console.log($event);
        for (var _i = 0, _a = this.names; _i < _a.length; _i++) {
            var n = _a[_i];
            n.displayName = false;
        }
        name.displayName = $event.checked;
        //TODO: iterate over all names and make sure atleast one name has displayName flag set to true
    };
    /*  setService(nameService: NameService){
        this.nameService=nameService;
    
      }*/
    NameEditComponent.prototype.deleteName = function (name) {
        console.log("delete");
        this.substanceEditService.deleteName(name);
    };
    NameEditComponent.prototype.changeAccess = function ($event, name) {
        this.utilService.changeAccess($event, name);
    };
    NameEditComponent.prototype.filterAccessMultiple = function ($event) {
        var _this = this;
        var query = $event.query;
        this.cvService.getList("ACCESS_GROUP").then(function (access) {
            var dif = __WEBPACK_IMPORTED_MODULE_5_lodash__["differenceWith"](access, _this.name.access, __WEBPACK_IMPORTED_MODULE_5_lodash__["isEqual"]);
            _this.filteredAccessMultiple = _this.filterAccess(query, dif);
        });
    };
    NameEditComponent.prototype.filterAccess = function (query, access) {
        //in a real application, make a request to a remote url with the query and return filtered results, for demo we filter at client side
        var filtered = [];
        console.log("filterAccessMultiple");
        console.log(query);
        if (!query) {
            console.log("kkk");
            return this.access;
        }
        for (var i = 0; i < access.length; i++) {
            var ac = access[i];
            if (ac.label.toLowerCase().indexOf(query.toLowerCase()) == 0) {
                filtered.push(ac);
            }
        }
        return filtered;
    };
    NameEditComponent.prototype.sayhello = function ($event) {
        console.log("hello");
        console.log($event.query);
        if (!$event.query) {
            console.log("suggestionlist");
            this.filteredAccessMultiple = this.access;
            console.log(this.filteredAccessMultiple.length);
        }
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__name__["a" /* Name */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__name__["a" /* Name */]) === 'function' && _a) || Object)
    ], NameEditComponent.prototype, "name", void 0);
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', Array)
    ], NameEditComponent.prototype, "names", void 0);
    NameEditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'name-edit',
            template: __webpack_require__(977),
            providers: [__WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _d) || Object])
    ], NameEditComponent);
    return NameEditComponent;
    var _a, _b, _c, _d;
}());
//# sourceMappingURL=name.component.js.map

/***/ }),

/***/ 740:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__data__ = __webpack_require__(204);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return NoteService; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};


var NoteService = (function (_super) {
    __extends(NoteService, _super);
    function NoteService() {
        _super.apply(this, arguments);
        this.noteList = [];
    }
    NoteService.prototype.saveNote = function (note) {
        console.log("note save service");
    };
    NoteService.prototype.deleteNote = function (note) {
        console.log("delete Notes service");
        var loc = this.noteList.indexOf(note);
        if (loc > -1) {
            this.noteList.splice(loc, 1);
        }
        return this.noteList;
    };
    NoteService.prototype.addNote = function (note) {
        if (!note.uuid) {
            note.generateNewUuid();
        }
        this.noteList.push(note);
        return this;
    };
    NoteService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], NoteService);
    return NoteService;
}(__WEBPACK_IMPORTED_MODULE_1__data__["a" /* Data */]));
//# sourceMappingURL=note.service.js.map

/***/ }),

/***/ 741:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__notes__ = __webpack_require__(484);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__note_service__ = __webpack_require__(740);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return NotesEditComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




var NotesEditComponent = (function () {
    function NotesEditComponent(cvService, noteService) {
        this.cvService = cvService;
        this.noteService = noteService;
    }
    NotesEditComponent.prototype.getAccess = function () {
        /*this.cvService
          .getAccess()
          .then(access => this.access = access);*/
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    NotesEditComponent.prototype.ngOnInit = function () {
        this.getAccess();
    };
    NotesEditComponent.prototype.saveNote = function (note) {
        console.log(note);
        console.log("save");
        this.noteService.saveNote(note);
    };
    NotesEditComponent.prototype.deleteNote = function (note) {
        console.log("delete");
        this.noteService.deleteNote(note);
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__notes__["a" /* Notes */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__notes__["a" /* Notes */]) === 'function' && _a) || Object)
    ], NotesEditComponent.prototype, "notes", void 0);
    NotesEditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'notes-edit',
            template: __webpack_require__(979),
            providers: [__WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_3__note_service__["a" /* NoteService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__note_service__["a" /* NoteService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__note_service__["a" /* NoteService */]) === 'function' && _c) || Object])
    ], NotesEditComponent);
    return NotesEditComponent;
    var _a, _b, _c;
}());
//# sourceMappingURL=notes-edit.component.js.map

/***/ }),

/***/ 742:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__notes__ = __webpack_require__(484);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__ = __webpack_require__(30);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_4_primeng_primeng__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return NotesComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var NotesComponent = (function () {
    function NotesComponent(cvService, confirmationService, utilService) {
        this.cvService = cvService;
        this.confirmationService = confirmationService;
        this.utilService = utilService;
        this.access = [];
        this.recordUUID = "00006eea-e2d2-4d79-99ff-30f17b3dd740";
        this.notes = [];
    }
    NotesComponent.prototype.ngOnInit = function () {
        this.getAccess();
    };
    NotesComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
        /*   this.cvService
             .getList("ACCESS_GROUP")
             .then(access => this.access = access);*/
    };
    NotesComponent.prototype.saveNoteListChanges = function ($event, notes) {
        console.log("save notes list changes");
        console.log(notes);
    };
    NotesComponent.prototype.changeAccess = function ($event, notes) {
        this.utilService.changeAccess($event, notes);
    };
    NotesComponent.prototype.addNotes = function () {
        console.log("Notes");
        this.newNotes = true;
        this.nNote = new __WEBPACK_IMPORTED_MODULE_2__notes__["a" /* Notes */]();
        this.nNote.access = ['protected'];
        this.notes.push(this.nNote);
    };
    NotesComponent.prototype.confirmDeleteNotes = function (notes) {
        var _this = this;
        this.confirmationService.confirm({
            message: 'Do you want to delete this notes?',
            header: 'Delete Confirmation',
            icon: 'fa fa-trash',
            accept: function () {
                _this.deleteNotes(notes);
            }
        });
    };
    NotesComponent.prototype.findSelectedNotesIndex = function (notes) {
        return this.notes.indexOf(notes);
    };
    NotesComponent.prototype.deleteNotes = function (notes) {
        console.log("delete:");
        console.log(notes);
        this.notes.splice(this.findSelectedNotesIndex(notes), 1);
    };
    NotesComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'notes',
            template: __webpack_require__(980),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__["ConfirmationService"]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__["ConfirmationService"]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */]) === 'function' && _c) || Object])
    ], NotesComponent);
    return NotesComponent;
    var _a, _b, _c;
}());
//# sourceMappingURL=notes.component.js.map

/***/ }),

/***/ 743:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__parameter__ = __webpack_require__(744);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ParameterComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var ParameterComponent = (function () {
    //params: Parameter[] = [];
    function ParameterComponent(cvService) {
        this.cvService = cvService;
        this.paramType = [];
    }
    ParameterComponent.prototype.ngOnInit = function () {
        this.getParamType();
    };
    ParameterComponent.prototype.getParamType = function () {
        var _this = this;
        this.cvService
            .getParameterType()
            .then(function (pt) { return _this.paramType = pt; });
    };
    ParameterComponent.prototype.addParam = function () {
        console.log("param");
        /* this.newParam = true;
         this.nParam = new Parameter();
         this.params.push(this.nParam);*/
    };
    /*findSelectedParamIndex(param: Parameter): number {
      return this.params.indexOf(param);
    }*/
    ParameterComponent.prototype.deleteParam = function (param) {
        console.log("delete:");
        console.log(param);
        //this.params.splice(this.findSelectedParamIndex(param), 1);
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__parameter__["a" /* Parameter */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__parameter__["a" /* Parameter */]) === 'function' && _a) || Object)
    ], ParameterComponent.prototype, "param", void 0);
    ParameterComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'param',
            template: __webpack_require__(981),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object])
    ], ParameterComponent);
    return ParameterComponent;
    var _a, _b;
}());
//# sourceMappingURL=parameter.component.js.map

/***/ }),

/***/ 744:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(114);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Parameter; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var Parameter = (function (_super) {
    __extends(Parameter, _super);
    function Parameter() {
        _super.apply(this, arguments);
    }
    return Parameter;
}(__WEBPACK_IMPORTED_MODULE_0__references_referencedData__["a" /* ReferencedData */]));
//# sourceMappingURL=parameter.js.map

/***/ }),

/***/ 745:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__ = __webpack_require__(30);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_primeng_primeng__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__property_model__ = __webpack_require__(485);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__property_service__ = __webpack_require__(486);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__angular_material__ = __webpack_require__(111);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7__references_reference_list_dialog_component__ = __webpack_require__(145);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_8_lodash__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return PropertiesListComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};









var PropertiesListComponent = (function () {
    function PropertiesListComponent(cvService, confirmationService, utilService, propertyService, dialog) {
        this.cvService = cvService;
        this.confirmationService = confirmationService;
        this.utilService = utilService;
        this.propertyService = propertyService;
        this.dialog = dialog;
        this.propName = [];
        this.propType = [];
        this.access = [];
        this.nProperty = new __WEBPACK_IMPORTED_MODULE_4__property_model__["a" /* Property */](this.propertyService);
        this.properties = [];
        this.references = [];
        this.recordUUID = "00006eea-e2d2-4d79-99ff-30f17b3dd740";
        this.showRSStruct = false;
    }
    PropertiesListComponent.prototype.ngOnInit = function () {
        this.getAccess();
        this.getPropertyName();
        this.getPropertyType();
    };
    PropertiesListComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    PropertiesListComponent.prototype.getPropertyName = function () {
        var _this = this;
        this.cvService
            .getList("PROPERTY_NAME")
            .then(function (pn) { return _this.propName = pn; });
    };
    PropertiesListComponent.prototype.getPropertyType = function () {
        var _this = this;
        this.cvService
            .getList("PROPERTY_TYPE")
            .then(function (pt) { return _this.propType = pt; });
    };
    PropertiesListComponent.prototype.showReferenceIndexes = function (data) {
        if (data.references) {
            return __WEBPACK_IMPORTED_MODULE_8_lodash__["chain"](this.references)
                .filter(function (r) { return (data.references.indexOf(r.uuid) >= 0); })
                .map("index")
                .value();
        }
        else {
            return "";
        }
    };
    PropertiesListComponent.prototype.openRefListDialog = function (property) {
        console.log("property list");
        var dialogRef = this.dialog.open(__WEBPACK_IMPORTED_MODULE_7__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], { height: '550px', width: '400px' });
        //TODO: should have a cleaner initialization process
        dialogRef.componentInstance.allReferences = this.references;
        console.log(property);
        dialogRef.componentInstance.data = property;
        dialogRef.componentInstance.closeFunction = function () {
            dialogRef.close();
        };
    };
    PropertiesListComponent.prototype.savePropertyListChanges = function ($event, properties) {
        console.log("save properties list changes");
        console.log(properties);
    };
    PropertiesListComponent.prototype.addProperty = function () {
        this.newProperty = true;
        this.nProperty = new __WEBPACK_IMPORTED_MODULE_4__property_model__["a" /* Property */](this.propertyService);
        this.nProperty.access = ['protected'];
        this.nProperty.references = [];
        this.properties.push(this.nProperty);
    };
    PropertiesListComponent.prototype.findSelectedPropertyIndex = function (property) {
        return this.properties.indexOf(property);
    };
    PropertiesListComponent.prototype.deleteProperty = function (property) {
        console.log("delete:");
        console.log(property);
        this.properties.splice(this.findSelectedPropertyIndex(property), 1);
    };
    PropertiesListComponent.prototype.confirmDeleteProperty = function (property) {
        var _this = this;
        this.confirmationService.confirm({
            message: 'Do you want to delete this Property?',
            header: 'Delete Confirmation',
            icon: 'fa fa-trash',
            accept: function () {
                console.log("confirm delete Property");
                _this.deleteProperty(property);
            }
        });
    };
    PropertiesListComponent.prototype.changeAccess = function ($event, property) {
        this.utilService.changeAccess($event, property);
    };
    PropertiesListComponent.prototype.showStructure = function () {
        console.log("showStructure");
        this.showRSStruct = !this.showRSStruct;
        console.log(this.showRSStruct);
    };
    PropertiesListComponent.prototype.showAmounts = function (amount) {
        return this.utilService.displayAmount(amount);
    };
    PropertiesListComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'properties-list',
            template: __webpack_require__(982),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_3_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_5__property_service__["a" /* PropertyService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_3_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3_primeng_primeng__["ConfirmationService"]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_5__property_service__["a" /* PropertyService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__property_service__["a" /* PropertyService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_6__angular_material__["d" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__angular_material__["d" /* MdDialog */]) === 'function' && _e) || Object])
    ], PropertiesListComponent);
    return PropertiesListComponent;
    var _a, _b, _c, _d, _e;
}());
//# sourceMappingURL=properties-list.component.js.map

/***/ }),

/***/ 746:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__property_service__ = __webpack_require__(486);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__property_model__ = __webpack_require__(485);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__ = __webpack_require__(30);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return PropertyEditComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var PropertyEditComponent = (function () {
    function PropertyEditComponent(cvService, propertyService, utilService) {
        this.cvService = cvService;
        this.propertyService = propertyService;
        this.utilService = utilService;
        this.propName = [];
        this.propType = [];
        this.access = [];
        this.amountDisplay = false;
        this.parameters = [];
    }
    PropertyEditComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    PropertyEditComponent.prototype.getPropertyName = function () {
        var _this = this;
        this.cvService
            .getList("PROPERTY_NAME")
            .then(function (pn) { return _this.propName = pn; });
    };
    PropertyEditComponent.prototype.getPropertyType = function () {
        var _this = this;
        this.cvService
            .getList("PROPERTY_TYPE")
            .then(function (pt) { return _this.propType = pt; });
    };
    PropertyEditComponent.prototype.ngOnInit = function () {
        this.getAccess();
        this.getPropertyName();
        this.getPropertyType();
    };
    PropertyEditComponent.prototype.saveProperty = function (property) {
        console.log(property);
        console.log("save");
        this.propertyService.saveProperty(property);
    };
    PropertyEditComponent.prototype.deleteProperty = function (property) {
        console.log("delete");
        this.propertyService.deleteProperty(property);
    };
    PropertyEditComponent.prototype.changeAccess = function ($event, property) {
        this.utilService.changeAccess($event, property);
    };
    PropertyEditComponent.prototype.showAmounts = function (amount) {
        return this.utilService.displayAmount(amount);
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_3__property_model__["a" /* Property */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__property_model__["a" /* Property */]) === 'function' && _a) || Object)
    ], PropertyEditComponent.prototype, "property", void 0);
    PropertyEditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'property-edit',
            template: __webpack_require__(983),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_2__property_service__["a" /* PropertyService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_2__property_service__["a" /* PropertyService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__property_service__["a" /* PropertyService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__["a" /* UtilService */]) === 'function' && _d) || Object])
    ], PropertyEditComponent);
    return PropertyEditComponent;
    var _a, _b, _c, _d;
}());
//# sourceMappingURL=property-edit.component.js.map

/***/ }),

/***/ 747:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__reference__ = __webpack_require__(146);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_http__ = __webpack_require__(108);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_rxjs_Rx__ = __webpack_require__(994);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_rxjs_Rx___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_4_rxjs_Rx__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__substanceedit_substanceedit_service__ = __webpack_require__(58);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__ = __webpack_require__(30);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ReferenceEdit; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};







var ReferenceEdit = (function () {
    function ReferenceEdit(cvService, http, substanceEditService, utilService) {
        this.cvService = cvService;
        this.http = http;
        this.substanceEditService = substanceEditService;
        this.utilService = utilService;
        this.sourceTypeList = [];
        this.documentSystemType = [];
        this.access = [];
        this.tags = [];
        this.apiEndPoint = this.utilService.browserurl + this.utilService.appContxt + "/upload";
        this.close = function () { };
    }
    ReferenceEdit.prototype.updateRef = function (ref, event) {
        event.preventDefault();
        //TODO:should do something interesting here
        //right now, we are always modifying things, no matter what
        this.close();
    };
    ReferenceEdit.prototype.deleteRef = function (selectedReference) {
        console.log(selectedReference);
        this.substanceEditService.deleteReference(selectedReference);
        //selectedReference.setFlag("delete",true);
        this.close();
    };
    ReferenceEdit.prototype.getSourceType = function () {
        var _this = this;
        this.cvService
            .getList("DOCUMENT_TYPE")
            .then(function (st) { return _this.sourceTypeList = st; });
    };
    ReferenceEdit.prototype.getDocumentSystemType = function () {
        var _this = this;
        this.cvService
            .getList("DOCUMENT_SYSTEM_TYPE")
            .then(function (st) { return _this.documentSystemType = st; });
    };
    ReferenceEdit.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    ReferenceEdit.prototype.getTags = function () {
        var _this = this;
        this.cvService
            .getList("DOCUMENT_COLLECTION")
            .then(function (tags) { return _this.tags = tags; });
    };
    ReferenceEdit.prototype.ngOnInit = function () {
        this.getSourceType();
        this.getDocumentSystemType();
        this.getAccess();
        this.getTags();
    };
    ReferenceEdit.prototype.fileUploader = function ($event) {
        console.log("file uploader");
        console.log($event);
        var fileList = $event.target.files;
        if (fileList.length > 0) {
            var file = fileList[0];
            console.log(file.name);
            var formData = new FormData();
            formData.append('uploadFile', file, file.name);
            var headers = new __WEBPACK_IMPORTED_MODULE_3__angular_http__["c" /* Headers */]();
            headers.append('Content-Type', 'multipart/form-data');
            headers.append('Accept', 'application/json');
            var options = new __WEBPACK_IMPORTED_MODULE_3__angular_http__["d" /* RequestOptions */]({ headers: headers });
            this.http.post("" + this.apiEndPoint, formData, options)
                .map(function (res) { return res.json(); })
                .catch(function (error) { return __WEBPACK_IMPORTED_MODULE_4_rxjs_Rx__["Observable"].throw(error); })
                .subscribe(function (data) { return console.log('success'); }, function (error) { return console.log(error); });
        }
    };
    ReferenceEdit.prototype.changeAccess = function ($event, ref) {
        this.utilService.changeAccess($event, ref);
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__reference__["a" /* Reference */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__reference__["a" /* Reference */]) === 'function' && _a) || Object)
    ], ReferenceEdit.prototype, "ref", void 0);
    ReferenceEdit = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'reference-edit',
            template: __webpack_require__(985),
            //styleUrls: ['../styles.css']
            providers: [__WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_5__substanceedit_substanceedit_service__["a" /* SubstanceEditService */], __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__["a" /* UtilService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__angular_http__["b" /* Http */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__angular_http__["b" /* Http */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_5__substanceedit_substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__utils_utils_service__["a" /* UtilService */]) === 'function' && _e) || Object])
    ], ReferenceEdit);
    return ReferenceEdit;
    var _a, _b, _c, _d, _e;
}());
/*$http.post(baseurl + 'upload', fd, {
  transformRequest: angular.identity,
  headers: {'Content-Type': undefined}
})*/
//# sourceMappingURL=reference-edit-component.js.map

/***/ }),

/***/ 748:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(29);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__relationship__ = __webpack_require__(304);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__ = __webpack_require__(30);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__amount_amount_model__ = __webpack_require__(481);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__angular_http__ = __webpack_require__(108);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6_lodash__ = __webpack_require__(35);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_6_lodash__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7__substanceedit_substanceedit_service__ = __webpack_require__(58);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8_primeng_primeng__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_8_primeng_primeng__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return RelationshipEditComponent; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};









var RelationshipEditComponent = (function () {
    //amount: Amount = new Amount();
    function RelationshipEditComponent(cvService, utilService, http, substanceEditService) {
        this.cvService = cvService;
        this.utilService = utilService;
        this.http = http;
        this.substanceEditService = substanceEditService;
        this.qualification = [];
        this.relationshipTypes = [];
        this.access = [];
        this.interactionType = [];
        this.amountDisplay = false;
        this.searchResult = [];
        this.isEdit = true;
        this.filterSuggestions = [];
        this.newAmount = new __WEBPACK_IMPORTED_MODULE_4__amount_amount_model__["a" /* Amount */]();
        this.searchMResult = [];
        this.ismEdit = true;
        this.sstate = "clean";
        this.rowCount = 2;
        this.browserurl = this.utilService.getBrowserUrl();
        this.suggesturl = this.browserurl + this.utilService.appContxt + "/api/v1/suggest?q=";
        this.getAccess();
        this.getRelationshipTypes();
        this.getQualification();
        this.getIntercationType();
    }
    RelationshipEditComponent.prototype.closeRow = function (rel, closed) {
        console.log("close row");
        this.dt.toggleRow(rel);
    };
    RelationshipEditComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) {
            console.log(access);
            _this.access = access;
        });
    };
    RelationshipEditComponent.prototype.getRelationshipTypes = function () {
        var _this = this;
        this.cvService
            .getList("RELATIONSHIP_TYPE")
            .then(function (relTypes) { return _this.relationshipTypes = relTypes; });
    };
    RelationshipEditComponent.prototype.getQualification = function () {
        var _this = this;
        this.cvService
            .getList("QUALIFICATION")
            .then(function (qual) { return _this.qualification = qual; });
    };
    RelationshipEditComponent.prototype.getIntercationType = function () {
        var _this = this;
        this.cvService
            .getList("INTERACTION_TYPE")
            .then(function (inTypes) { return _this.interactionType = inTypes; });
    };
    RelationshipEditComponent.prototype.ngOnInit = function () {
        console.log(this);
        /*   if(this.relationship && !this.relationship.amount){
             this.relationship.amount = this.amount;
           }*/
    };
    RelationshipEditComponent.prototype.deleteRelationship = function (relation) {
        console.log("delete relationship");
        this.substanceEditService.deleteRelationship(relation);
    };
    RelationshipEditComponent.prototype.changeAccess = function ($event, relation) {
        console.log($event);
        this.utilService.changeAccess($event, relation);
    };
    RelationshipEditComponent.prototype.showAmounts = function (amount) {
        return this.utilService.displayAmount(amount);
    };
    RelationshipEditComponent.prototype.changeSelection = function () {
        this.isEdit = true;
        this.relationship.relatedSubstance = null;
        this.rowCount = 2;
    };
    RelationshipEditComponent.prototype.clearMSelection = function () {
        this.ismEdit = true;
        this.relationship.mediatorSubstance = null;
        this.rowCount = 2;
    };
    RelationshipEditComponent.prototype.cancelSearch = function () {
        this.isEdit = false;
        this.searchResult = [];
        this.rowCount = 2;
    };
    RelationshipEditComponent.prototype.cancelMSearch = function () {
        this.ismEdit = false;
        this.searchMResult = [];
        this.rowCount = 2;
    };
    RelationshipEditComponent.prototype.applySubstance = function (sr) {
        console.log(sr);
        this.relationship.relatedSubstance = sr;
        this.searchResult = [];
        this.isEdit = false;
        this.rowCount = 3;
    };
    RelationshipEditComponent.prototype.applyMSubstance = function (sr) {
        console.log(sr);
        this.relationship.mediatorSubstance = sr;
        this.searchMResult = [];
        this.ismEdit = false;
        this.rowCount = 3;
        console.log("apply mediator substance");
        console.log(this.relationship);
    };
    RelationshipEditComponent.prototype.getSuggestions = function (event, isMediator) {
        var _this = this;
        var endpoint = this.suggesturl + event.query;
        this.http
            .get(endpoint) //, {search: searchParams})
            .subscribe(function (res) {
            _this.filterSuggestions = [];
            if (res.json().Display_Name) {
                for (var _i = 0, _a = res.json().Display_Name; _i < _a.length; _i++) {
                    var n = _a[_i];
                    _this.filterSuggestions.push(n.key);
                }
            }
            if (res.json().Name) {
                for (var _b = 0, _c = res.json().Name; _b < _c.length; _b++) {
                    var n = _c[_b];
                    if (_this.filterSuggestions.indexOf(n.key) <= -1) {
                        _this.filterSuggestions.push(n.key);
                    }
                }
            }
        });
        // this.searchSubstance(event.query, isMediator);
    };
    RelationshipEditComponent.prototype.searchSubstance = function (query, isMediator) {
        var _this = this;
        this.sstate = "searching";
        console.log("search substance: " + query);
        //TODO: Move to js api
        var toSubRef = function (s) {
            var subref = {};
            subref.refuuid = s.uuid;
            subref.refPname = s._name;
            subref.approvalID = s.approvalID;
            return subref;
        };
        window["SubstanceFinder"]
            .searchByExactNameOrCode(query)
            .andThen(function (r) {
            console.log("Found:" + r.content.total);
            console.log(r);
            return r.content;
        })
            .andThen(function (c) {
            return __WEBPACK_IMPORTED_MODULE_6_lodash__["map"](c, function (r) { return toSubRef(r); });
        })
            .get(function (sref) {
            console.log(sref);
            if (sref.length > 1) {
                _this.rowCount = 4;
            }
            if (isMediator) {
                _this.searchMResult = sref;
                console.log(_this.searchMResult.length);
                if (sref.length == 1) {
                    _this.applyMSubstance(sref[0]);
                }
            }
            else {
                _this.searchResult = sref;
                console.log(_this.searchResult.length);
                if (sref.length == 1) {
                    _this.applySubstance(sref[0]);
                }
            }
        });
        this.sstate = "search-done";
    };
    RelationshipEditComponent.prototype.onSaved = function (amount) {
        this.relationship.amount = amount;
        this.amountDisplay = false;
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__relationship__["a" /* Relationship */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__relationship__["a" /* Relationship */]) === 'function' && _a) || Object)
    ], RelationshipEditComponent.prototype, "relationship", void 0);
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_8_primeng_primeng__["DataTable"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_8_primeng_primeng__["DataTable"]) === 'function' && _b) || Object)
    ], RelationshipEditComponent.prototype, "dt", void 0);
    RelationshipEditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'relation-edit',
            template: __webpack_require__(988),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_7__substanceedit_substanceedit_service__["a" /* SubstanceEditService */], __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_5__angular_http__["b" /* Http */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__angular_http__["b" /* Http */]) === 'function' && _e) || Object, (typeof (_f = typeof __WEBPACK_IMPORTED_MODULE_7__substanceedit_substanceedit_service__["a" /* SubstanceEditService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_7__substanceedit_substanceedit_service__["a" /* SubstanceEditService */]) === 'function' && _f) || Object])
    ], RelationshipEditComponent);
    return RelationshipEditComponent;
    var _a, _b, _c, _d, _e, _f;
}());
//# sourceMappingURL=relationship-edit.component.js.map

/***/ }),

/***/ 749:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return CustomRouteReuseStrategy; });
var CustomRouteReuseStrategy = (function () {
    function CustomRouteReuseStrategy() {
        this.handlers = {};
    }
    CustomRouteReuseStrategy.prototype.shouldDetach = function (route) {
        console.debug('CustomReuseStrategy:shouldDetach', route);
        return true;
    };
    CustomRouteReuseStrategy.prototype.store = function (route, det) {
        console.debug('CustomReuseStrategy:store', route, det);
        this.handlers[route.routeConfig.path] = det;
        if (det) {
            if (det["componentRef"]._component.detach) {
                det["componentRef"]._component.detach();
            }
        }
    };
    CustomRouteReuseStrategy.prototype.shouldAttach = function (route) {
        console.debug('CustomReuseStrategy:shouldAttach', route);
        if (route.routeConfig.path.indexOf("summary") >= 0) {
            return !!route.routeConfig && !!this.handlers[route.routeConfig.path];
        }
        return false;
    };
    CustomRouteReuseStrategy.prototype.retrieve = function (route) {
        console.debug('CustomReuseStrategy:retrieve', route);
        if (!route.routeConfig)
            return null;
        var det = this.handlers[route.routeConfig.path];
        if (det) {
            if (det["componentRef"]._component.refresh) {
                det["componentRef"]._component.refresh();
            }
        }
        return det;
    };
    CustomRouteReuseStrategy.prototype.shouldReuseRoute = function (future, curr) {
        console.debug('CustomReuseStrategy:shouldReuseRoute', future, curr);
        return future.routeConfig === curr.routeConfig;
    };
    return CustomRouteReuseStrategy;
}());
//# sourceMappingURL=route-reuse-strategy.js.map

/***/ }),

/***/ 750:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return environment; });
// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.
var environment = {
    production: false
};
//# sourceMappingURL=environment.js.map

/***/ }),

/***/ 908:
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(69)();
// imports


// module
exports.push([module.i, "", ""]);

// exports


/*** EXPORTS FROM exports-loader ***/
module.exports = module.exports.toString();

/***/ }),

/***/ 909:
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(69)();
// imports


// module
exports.push([module.i, "", ""]);

// exports


/*** EXPORTS FROM exports-loader ***/
module.exports = module.exports.toString();

/***/ }),

/***/ 974:
/***/ (function(module, exports) {

module.exports = "<br/>\n<div>\n<form class=\"amount\" #amountEditForm=\"ngForm\">\n  <br/><br/>\n   <md-select placeholder=\"Amount Type\" name=\"amountType\" [(ngModel)]=\"amount.type\">\n       <md-option *ngFor=\"let at of amountType\" [value]=\"at.value\">{{ at.label }}</md-option>\n   </md-select>\n\n  <md-input-container>\n   <input mdInput name=\"average\" placeholder=\"Average\" [(ngModel)]=\"amount.average\">\n  </md-input-container>\n\n  <md-input-container>\n  <input mdInput name=\"low\" placeholder=\"Low\" [(ngModel)]=\"amount.low\">\n  </md-input-container>\n\n  <md-input-container>\n  <input mdInput name=\"high\" placeholder=\"High\" [(ngModel)]=\"amount.high\">\n  </md-input-container>\n\n  <md-input-container>\n    <input mdInput name=\"lowLimit\"  placeholder=\"Low Limit\" [(ngModel)]=\"amount.lowLimit\">\n  </md-input-container>\n\n  <md-input-container>\n    <input mdInput name=\"highLimit\" placeholder=\"High Limit\" [(ngModel)]=\"amount.highLimit\">\n  </md-input-container>\n\n  <md-select placeholder=\"Amount Units\" name=\"amountUnits\" [(ngModel)]=\"amount.units\">\n       <md-option *ngFor=\"let au of amountUnits\" [value]=\"au.value\">{{ au.label }}</md-option>\n  </md-select>\n\n  <md-input-container>\n    <input mdInput name=\"nonNumericValue\" placeholder=\"Non Numeric Value\" [(ngModel)]=\"amount.nonNumericValue\">\n  </md-input-container>\n\n  <button type=\"button\" pButton icon=\"fa-check\" (click)=\"saveAmount(amount)\" label=\"Ok\"></button>\n  <button type=\"button\" pButton icon=\"fa-times\" (click)=\"clearAmount()\" label=\"Clear\"></button>\n  <br/><br/>\n</form></div>\n<br/>\n"

/***/ }),

/***/ 975:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"code-edit-form\" #codeEditForm=\"ngForm\">\n  <md-grid-list cols=\"10\" gutterSize=\"2px\" rowHeight=\"70px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Code System\" name=\"codeSystem\" [(ngModel)]=\"code.codeSystem\" [style.width]=\"'80%'\" >\n        <md-option *ngFor=\"let cs of codeSystem\" [value]=\"cs.value\">{{ cs.label }}</md-option>\n      </md-select></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n<!--    <md-grid-tile [colspan]=3 [rowspan]=1>\n    <md-select placeholder=\"Code System Type\" name=\"type\" [(ngModel)]=\"code.codeSystemType\" [style.width]=\"'80%'\" >\n      <md-option *ngFor=\"let type of codeSystemType\" [value]=\"type.value\">{{ type.label }}</md-option>\n    </md-select></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>-->\n\n      <md-grid-tile [colspan]=3 [rowspan]=1>\n         <md-select placeholder=\"Code Type\" name=\"type\" [(ngModel)]=\"code.type\" [style.width]=\"'80%'\" >\n           <md-option *ngFor=\"let type of codeType\" [value]=\"type.value\">{{ type.label }}</md-option>\n         </md-select></md-grid-tile>\n        <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"code.access\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"\n                     (onChange) =\"changeAccess($event, code)\"></p-multiSelect>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Code</b></md-grid-tile>\n    <md-grid-tile [colspan]=3 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"code\" value=\"{{code.code}}\" [(ngModel)]=\"code.code\">\n      </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Code URL</b></md-grid-tile>\n    <md-grid-tile [colspan]=4 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"url\" value=\"{{code.url}}\" [(ngModel)]=\"code.url\">\n      </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Code Text</b></md-grid-tile>\n    <md-grid-tile [colspan]=8 [rowspan]=1>\n    <md-input-container class=\"ref-full-width\">\n      <textarea mdInput name=\"comments\" value=\"{{code.comments}}\" [(ngModel)]=\"code.comments\"></textarea>\n    </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Comments</b></md-grid-tile>\n    <md-grid-tile [colspan]=8 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <textarea mdInput name=\"codeText\" value=\"{{code.codeText}}\" [(ngModel)]=\"code.codeText\"></textarea>\n      </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=10 [rowspan]=1 *ngIf=\"code.uuid\">\n      Created by <code>&nbsp;{{code.createdBy}}&nbsp;</code> on <code>&nbsp; {{code.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{code.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{code.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=7 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"deleteCode(code)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n    </md-grid-tile>\n<!--    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"saveCode(code)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n    </md-grid-tile>-->\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 976:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"codes\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\">\n  <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n     <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addCode(true)\" label=\"Add Code\"></button>\n      <!--   <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"codes.length > 0\" (click)=\"exportCodes()\" label=\"Export Codes\"></button>\n        <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"codes.length > 0\" (click)=\"search()\" label=\"Search\"></button>-->\n    </div>\n  </p-header>\n\n  <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n    <template pTemplate=\"header\">Edit</template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'80px'}\">\n    <template pTemplate=\"header\">Delete</template>\n    <template pTemplate=\"body\" let-code=\"rowData\">\n      <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteCode(code)\"></button>\n    </template>\n  </p-column>\n\n  <p-column field=\"codeSystem\" header=\"Code System\" [editable]=\"false\" [style]=\"{'overflow':'visible'}\" [sortable]=\"true\">\n    <template let-col let-code=\"rowData\" pTemplate=\"body\">\n\n      <p-dropdown [(ngModel)]=\"code[col.field]\" [options]=\"codeSystem\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\"\n                   filter=\"filter\" class=\"dropdown-text\" required=\"true\" placeholder=\"Select Code System\"></p-dropdown>\n    </template>\n  </p-column>\n\n  <p-column field=\"type\" header=\"Code Type\" [editable]=\"false\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-code=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"code[col.field]\" [options]=\"codeType\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\"\n                  class=\"dropdown-text\" required=\"true\" filter=\"filter\" placeholder=\"Select Code Type\"></p-dropdown>\n    </template>\n  </p-column>\n  <p-column field=\"code\" header=\"Code\"  [style]=\"{'width':'200px'}\" [editable]=\"true\"></p-column>\n  <p-column field=\"url\" header=\"Code URL\"  [style]=\"{'width':'300px'}\">\n    <template let-col let-code=\"rowData\" pTemplate=\"body\">\n      <a href=\"{{code.url}}\" target=\"_blank\">{{code.url}}</a>\n    </template>\n  </p-column>\n\n  <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-code=\"rowData\" pTemplate=\"body\">\n    <div *ngIf=\"code.access.length == 0\"> Public </div>\n    <div *ngIf=\"code.access.length > 0\"> {{code.access}} </div>\n      </template>\n\n    <template let-col let-code=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"code[col.field]\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                     (onChange) =\"changeAccess($event, code)\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column>\n    <template pTemplate=\"header\">References</template>\n    <template let-code=\"rowData\"  pTemplate=\"body\">\n      <div *ngIf=\"!code.references || code.references.length == 0\" (click)=\"openRefListDialog(code)\"><h3>Click to Add</h3></div>\n      <div (click)=\"openRefListDialog(code)\" *ngIf=\"code.references\">\n        {{showReferenceIndexes(code)}}\n      </div>\n    </template>\n\n  </p-column>\n\n  <template let-code pTemplate=\"rowexpansion\">\n    <div>\n      <code-edit [code]=\"code\"></code-edit>\n    </div>\n  </template>\n\n  <p-footer *ngIf=\"codes.length > 3\">\n    <div class=\"ui-dialog-buttonpane ui-helper-clearfix\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addCode(false)\" label=\"Add Code\"></button>\n    </div>\n  </p-footer>\n\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n"

/***/ }),

/***/ 977:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"name-edit-form\" #nameEditForm=\"ngForm\">\n <md-grid-list cols=\"8\" gutterSize=\"2px\" rowHeight=\"70px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Name</b></md-grid-tile>\n    <md-grid-tile [colspan]=6 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"name\" value=\"{{name.name}}\" [(ngModel)]=\"name.name\" placeholder=\"name ...\" required>\n      </md-input-container>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n\n\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n     <md-select placeholder=\"Name Type\" name=\"type\" [(ngModel)]=\"name.type\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let type of nameTypes\" [value]=\"type.value\" overlayVisible=\"true\">{{ type.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n   <md-grid-tile [colspan]=2 [rowspan]=1>\n     <md-checkbox [(ngModel)]=\"name.displayName\" [checked]=\"name.displayName\" name=\"DisplayName\" (change)=\"changeDisplay(name, $event)\">\n       Display Name (Preferred Term)\n     </md-checkbox>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-checkbox [(ngModel)]=\"name.preferred\" [checked]=\"name.preferred\" name=\"PreferredName\">\n        Listing Term\n      </md-checkbox>\n    </md-grid-tile>\n   <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n\n\n\n\n\n   <md-grid-tile [colspan]=2 [rowspan]=1 *ngIf=\"name.type=='of'\" class=\"color-bg\">\n     <p-multiSelect [options]=\"nameJuris\" name=\"nameJurisdiction\" [(ngModel)]=\"name.nameJurisdiction\"\n                    defaultLabel=\"Name Jurisdiction\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n   </md-grid-tile>\n   <md-grid-tile [colspan]=2 [rowspan]=1 *ngIf=\"name.type=='of'\" class=\"color-bg\">\n     <p-multiSelect [options]=\"nameOrgs\" name=\"nameOrgs\" [(ngModel)]=\"name.nameOrgs\"\n                    defaultLabel=\"Name Orgs\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n   </md-grid-tile>\n   <md-grid-tile [colspan]=4 [rowspan]=1 *ngIf=\"name.type=='of'\" class=\"color-bg\"></md-grid-tile>\n              <md-grid-tile [colspan]=3 [rowspan]=1>\n                <p-multiSelect [options]=\"languages\" name=\"languages\" [(ngModel)]=\"name.languages\"\n                               defaultLabel=\"Languages\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n              </md-grid-tile>\n              <md-grid-tile [colspan]=3 [rowspan]=1>\n                <p-multiSelect [options]=\"domains\" name=\"domains\"  [(ngModel)]=\"name.domains\"\n                               defaultLabel=\"Domains\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n              </md-grid-tile>\n             <md-grid-tile [colspan]=2 [rowspan]=1>\n             <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"name.access\"\n                       defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\" (onChange)=\"changeAccess($event, name)\"></p-multiSelect>\n<!--\n               <p-autoComplete [(ngModel)]=\"name.access\" name=\"access\" [suggestions]=\"filteredAccessMultiple\" (completeMethod)=\"filterAccessMultiple($event)\" styleClass=\"wid100\"\n                               [minLength]=\"1\" placeholder=\"Access\" [multiple]=\"true\"\n                               scrollHeight=\"500px\" appendTo=\"body\" field=\"label\">\n               </p-autoComplete>\n-->\n             </md-grid-tile>\n\n\n    <md-grid-tile [colspan]=8 [rowspan]=1 *ngIf=\"name.createdBy\">\n      Created by <code>&nbsp;{{name.createdBy}}&nbsp;</code> on <code>&nbsp; {{name.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{name.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{name.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n  <md-grid-tile [colspan]=4 [rowspan]=1></md-grid-tile>\n  <md-grid-tile [colspan]=1 [rowspan]=1>\n    <button type=\"button\" (click)=\"deleteName(name)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n  </md-grid-tile>\n<!--  <md-grid-tile [colspan]=1 [rowspan]=1>\n    <button type=\"button\" (click)=\"saveName(name)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n  </md-grid-tile>-->\n  <md-grid-tile [colspan]=3 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 978:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"names\" expandableRows=\"true\" sizableColumns=\"true\"\n                 [responsive]=\"true\" [editable]=\"true\" id=\"nameListTable\" #datatable>\n  <!--[rows]=\"10\" [paginator]=\"true\" [pageLinks]=\"3\" [rowsPerPageOptions]=\"[5,10,20]\"-->\n      <p-header>\n        <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n        <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addName(true)\" label=\"Add Name\"></button>\n    <!--    <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"names.length > 0\" (click)=\"exportNames()\" label=\"Export Names\"></button>\n        <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"names.length > 0\" (click)=\"search()\" label=\"Search\"></button> -->\n        </div>\n      </p-header>\n\n      <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n        <template pTemplate=\"header\">Edit</template>\n      </p-column>\n\n      <p-column [style]=\"{'width':'80px'}\">\n        <template pTemplate=\"header\">Delete</template>\n        <template pTemplate=\"body\" let-name=\"rowData\">\n          <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteName(name)\"></button>\n        </template>\n      </p-column>\n      <p-column [style]=\"{'width':'38px'}\">\n        <template pTemplate=\"header\">PT</template>\n        <template pTemplate=\"body\" let-name=\"rowData\">\n          <md-radio-button name=\"display\" [value]=\"name.displayName\" [checked]=\"name.displayName\" title=\"Display Name (Preferred Term)\" (change)=\"changePreferTerm($event, name)\"></md-radio-button>\n        </template>\n      </p-column>\n\n     <p-column [style]=\"{'width':'38px'}\">\n        <template pTemplate=\"header\">LT</template>\n        <template let-name=\"rowData\" pTemplate=\"body\">\n          <md-checkbox [(ngModel)]=\"name.preferred\" [checked]=\"name.preferred\" name=\"listing\"  title=\"Listing Term\"></md-checkbox>\n        </template>\n      </p-column>\n\n      <p-column field=\"name\" header=\"Name\"  [style]=\"{'width':'500px'}\" [editable]=\"true\" [sortable]=\"true\" required>\n\n      </p-column>\n      <p-column field=\"type\" header=\"Type\" [editable]=\"false\" [style]=\"{'overflow':'visible'}\">\n        <template let-col let-name=\"rowData\" pTemplate=\"body\">\n          <p-dropdown [(ngModel)]=\"name[col.field]\" [options]=\"nameTypes\" [autoWidth]=\"false\"  filter=\"filter\" placeholder=\"Select Type\"\n                      [style]=\"{'width':'100%'}\" required=\"true\"><i class=\"fa fa-pencil-square-o\" aria-hidden=\"true\"></i></p-dropdown>\n        </template>\n      </p-column>\n\n      <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n\n        <template let-col let-name=\"rowData\" pTemplate=\"body\">\n          <div *ngIf=\"name.access.length == 0\"> Public </div>\n          <div *ngIf=\"name.access.length > 0\"> {{name.access}} </div>\n        </template>\n        <template let-col let-name=\"rowData\" pTemplate=\"editor\">\n          <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"name[col.field]\"\n                         defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                         (onLoad) =\"changeAccess($event, name)\" (onChange) =\"changeAccess($event, name)\"></p-multiSelect>\n         <!-- <p-autoComplete [(ngModel)]=\"name[col.field]\" name=\"access\" [suggestions]=\"filteredAccessMultiple\" (completeMethod)=\"filterAccessMultiple($event, name)\"\n                          [minLength]=\"1\" placeholder=\"Access\" [multiple]=\"true\" overlayVisible=\"true\"\n                          scrollHeight=\"500px\" appendTo=\"body\" field=\"label\" emptyMessage=\"Public\">\n          </p-autoComplete>-->\n       </template>\n      </p-column>\n      <p-column>\n        <template pTemplate=\"header\">References</template>\n        <template let-name=\"rowData\"  pTemplate=\"body\">\n          <div *ngIf=\"!name.references || name.references.length == 0\" (click)=\"openRefListDialog(name)\"><h3>Click to Add</h3></div>\n          <div (click)=\"openRefListDialog(name)\" *ngIf=\"name.references\">\n            {{showReferenceIndexes(name)}}\n          </div>\n        </template>\n\n      </p-column>\n      <template let-name pTemplate=\"rowexpansion\">\n        <div>\n          <name-edit [name]=name [names]=\"names\"></name-edit>\n         </div>\n      </template>\n  <p-footer *ngIf=\"names.length > 4\">\n    <div class=\"ui-dialog-buttonpane ui-helper-clearfix\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addName(false)\" label=\"Add Name\"></button>\n    </div>\n  </p-footer>\n    </p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n\n\n<!--<div>\n  <pre>\n  {{ names | json }}\n    </pre>\n</div>-->\n"

/***/ }),

/***/ 979:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"note-edit-form\" #noteEditForm=\"ngForm\">\n  <md-grid-list cols=\"8\" gutterSize=\"2px\" rowHeight=\"125px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=6 [rowspan]=1>\n      <textarea rows=\"5\" cols=\"100\" pInputTextarea name=\"note\" [(ngModel)]=\"notes.note\"></textarea>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"notes.access\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\">\n      </p-multiSelect>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=10 [rowspan]=1 *ngIf=\"notes.uuid\">\n      Created by <code>&nbsp;{{notes.createdBy}}&nbsp;</code> on <code>&nbsp; {{notes.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{notes.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{notes.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=3 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"deleteNote(notes)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"saveNote(notes)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 980:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"notes\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\">\n  <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addNotes()\" label=\"Add Notes\"></button>\n      <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"notes.length > 0\" (click)=\"exportNotes()\" label=\"Export Notes\"></button>\n      <!--  <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"notes.length > 0\" (click)=\"search()\" label=\"Search\"></button>-->\n    </div>\n  </p-header>\n\n  <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n    <template pTemplate=\"header\">Edit</template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'80px'}\">\n    <template pTemplate=\"header\">Delete</template>\n    <template pTemplate=\"body\" let-note=\"rowData\">\n      <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteNotes(this)\"></button>\n    </template>\n  </p-column>\n\n  <p-column field=\"note\" header=\"Notes\" [editable]=\"true\" [style]=\"{'width':'800px'}\"></p-column>\n\n  <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-note=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"note[col.field]\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                     (onChange) =\"changeAccess($event, name)\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'300px'}\">\n    <template pTemplate=\"header\">References</template>\n    <template let-note=\"rowData\"  pTemplate=\"body\">\n      <div *ngIf=\"!note.references || note.references.length == 0\" (click)=\"openRefListDialog(note)\">Click</div>\n      <div (click)=\"openRefListDialog(note)\" *ngIf=\"note.references\">\n        {{showReferenceIndexes(note)}}\n      </div>\n    </template>\n  </p-column>\n  <template let-note pTemplate=\"rowexpansion\">\n    <div>\n      <notes-edit [notes]=note></notes-edit>\n    </div>\n  </template>\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n<div><br/><button type=\"button\" pButton [style]=\"{display:block}\" (click)=\"saveNoteListChanges(event, notes)\" label=\"Save Changes\" *ngIf=\"notes.length > 0\"></button></div>\n"

/***/ }),

/***/ 981:
/***/ (function(module, exports) {

module.exports = "<br/>\n<div>\n  <form class=\"parameter\" #paramEditForm=\"ngForm\">\n   <div>\n     <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"deleteParam(this)\"></button>\n\n     <input mdInput name=\"Name\" placeholder=\"Name\" [style.width]=\"'10%'\">\n\n     <md-select placeholder=\"Parameter Type\" name=\"paramType\" [style.width]=\"'18%'\">\n        <md-option *ngFor=\"let pt of paramType\" [value]=\"pt.value\">{{ pt.label }}</md-option>\n      </md-select>\n\n     <span>Amount</span>\n\n      <button type=\"button\" pButton icon=\"fa-check\" (click)=\"saveParam(this)\" label=\"Ok\"></button>\n\n    </div>\n  </form></div>\n<br/>\n"

/***/ }),

/***/ 982:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"properties\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\" id=\"propertyListTable\" #dt>\n  <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addProperty()\" label=\"Add Property\"></button>\n      <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"properties.length > 0\" (click)=\"exportRelations()\" label=\"Export Propertys\"></button>\n      <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"properties.length > 0\" (click)=\"search()\" label=\"Search\"></button>\n    </div>\n  </p-header>\n  <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n    <template pTemplate=\"header\">Edit</template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'80px'}\">\n    <template pTemplate=\"header\">Delete</template>\n    <template pTemplate=\"body\" let-property=\"rowData\">\n      <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteProperty(this)\"></button>\n    </template>\n  </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\" [style]=\"{'width':'150px'}\">\n    <template pTemplate=\"header\">Referenced Substance <br/>\n      <md-checkbox [checked]=\"showRSStruct\" name=\"showRSStruct\" (change)=\"showStructure()\" *ngIf=\"properties.length > 0\">Show Structure</md-checkbox>\n    </template>\n    <template let-col let-property=\"rowData\" pTemplate=\"body\">\n      TEST TEST\n      <div *ngIf=\"showRSStruct\">\n        <img src=\"assets/images/protein.svg\" alt=\"structure\">\n      </div>\n    </template>\n  </p-column>\n\n  <p-column field=\"name\" header=\"Name\" [style]=\"{'overflow':'visible'}\" [style]=\"{'width':'300px'}\">\n    <template let-col let-property=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"property[col.field]\" [options]=\"propName\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\" required=\"true\" placeholder=\"name...\"></p-dropdown>\n    </template>\n  </p-column>\n\n  <p-column field=\"type\" header=\"Property Type\" [style]=\"{'overflow':'visible'}\" [style]=\"{'width':'150px'}\">\n    <template let-col let-property=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"property[col.field]\" [options]=\"propType\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\" required=\"true\" placeholder=\"type...\"></p-dropdown>\n    </template>\n  </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\">\n    <template pTemplate=\"header\">Amount</template>\n    <template let-col let-property=\"rowData\" pTemplate=\"body\">\n      {{showAmounts(property.value)}}\n    </template>\n  </p-column>\n\n <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-property=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"property[col.field]\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                     (onChange) =\"changeAccess($event, property)\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column>\n      <template pTemplate=\"header\">References</template>\n      <template let-property=\"rowData\"  pTemplate=\"body\">\n        <div *ngIf=\"!property.references || property.references.length == 0\" (click)=\"openRefListDialog(property)\">Click</div>\n        <div (click)=\"openRefListDialog(property)\" *ngIf=\"property.references\">\n          {{showReferenceIndexes(property)}}\n        </div>\n      </template>\n\n  </p-column>\n  <template let-property pTemplate=\"rowexpansion\">\n    <div>\n      <property-edit [property]=property></property-edit>\n    </div>\n  </template>\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n<div><br/><button type=\"button\" pButton [style]=\"{display:block}\" (click)=\"savePropertyListChanges(event, properties)\" label=\"Save Changes\" *ngIf=\"properties.length > 0\"></button></div>\n\n"

/***/ }),

/***/ 983:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"property-edit-form\" #propertyEditForm=\"ngForm\">\n\n  <md-grid-list cols=\"8\" gutterSize=\"2px\" rowHeight=\"70px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Property Name</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Property Name\" name=\"name\" [(ngModel)]=\"property.name\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let name of propName\" [value]=\"name.value\" overlayVisible=\"true\">{{ name.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Property Type</b></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <md-select placeholder=\"Property Type\" name=\"type\" [(ngModel)]=\"property.type\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let type of propType\" [value]=\"type.value\" overlayVisible=\"true\">{{ type.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Access</b></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"property.access\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"\n                     (onChange) =\"changeAccess($event, property)\">\n      </p-multiSelect></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <md-checkbox [(ngModel)]=\"property.defining\" [checked]=\"property.defining\" name=\"defining\"><b>Defining</b></md-checkbox>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=3 [rowspan]=1>REFERENCED SUBSTANCE</md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Amount</b></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <div (click)=\"amountDisplay = !amountDisplay\" *ngIf=\"!property.value\">Add Amount</div>\n      <div (click)=\"amountDisplay = !amountDisplay\" *ngIf=\"property.value\">\n        {{showAmounts()}}\n      </div>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n     <div (click)=\"paramDisplay = !paramDisplay\">Param</div>\n       <div (click)=\"paramDisplay = !paramDisplay\" *ngIf=\"property.parameters && property.parameters.length > 0\">\n        <div *ngFor=\"let param of paramList\" >\n          {{param.name}}\n        </div>\n      </div>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=8 [rowspan]=1 class=\"color-bg\" *ngIf=\"paramDisplay\">\n     parameter list\n      <param [param]=\"\">\n      <div *ngFor=\"let param of property.parameters\" >\n        <param [param]=\"param\">\n      </div>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=8 [rowspan]=1 class=\"color-bg\" *ngIf=\"amountDisplay\">\n      <amount [amount]=property.value></amount>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=8 [rowspan]=1 *ngIf=\"property.uuid\">\n      Created by <code>&nbsp;{{property.createdBy}}&nbsp;</code> on <code>&nbsp; {{property.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{property.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{property.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=4 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"deleteProperty(property)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"saveProperty(property)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 984:
/***/ (function(module, exports) {

module.exports = "<form (ngSubmit)=\"updateRef(selectedReference,$event)\" class=\"ref-edit-form\" #refEditForm=\"ngForm\">\n\n  <div md-dialog-title>\n    <p><span *ngIf=\"!selectedReference.getFlag('new')\">Edit <code>{{selectedReference.citation}}</code></span>\n      <span *ngIf=\"selectedReference.getFlag('new')\"><code>Create Reference</code></span>\n      <span><button type=\"button\" md-dialog-close><i class=\"material-icons md-18\">clear</i></button></span></p>\n  </div>\n\n  <md-dialog-content>\n    <md-input-container class=\"ref-full-width\">\n        <textarea mdInput name=\"citation\" placeholder=\"Citation\" required [(ngModel)]=\"selectedReference.citation\"></textarea>\n    </md-input-container>\n<p>\n    <md-select placeholder=\"Source Type\" name=\"sType\" [(ngModel)]=\"selectedReference.docType\" [style.width]=\"'100%'\">\n      <md-option *ngFor=\"let type of sourceTypeList\" [value]=\"type.value\">{{ type.label }}</md-option>\n    </md-select>\n  </p>\n\n<!--    <p><br/>\n      <md-select placeholder=\"Source Class\" name=\"sClass\">\n        <md-option *ngFor=\"let type of documentSystemType\" [value]=\"type.value\" disabled>{{ type.label }}</md-option>\n      </md-select>\n  </p>-->\n\n<p>\n    <md-input-container class=\"ref-full-width\">\n        <input mdInput placeholder=\"Source ID\" name=\"sourceId\" [(ngModel)]=\"selectedReference.id\">\n    </md-input-container>\n  </p>\n\n    <p>\n    <md-input-container class=\"ref-full-width\">\n        <input mdInput placeholder=\"Url\" type=\"url\" name=\"url\" [(ngModel)]=\"selectedReference.url\">\n    </md-input-container>\n    </p>\n\n    <!--<p><input type=\"file\" (change)=\"fileUploader($event)\" name=\"fileUpload\" [(ngModel)]=\"ref.uploadedFile\" placeholder=\"Upload file\" accept=\".pdf,.doc,.docx, .txt\"></p>-->\n\n    <p><md-checkbox class=\"ref-full-width\" name=\"pubDomain\" [(ngModel)]=\"selectedReference.publicDomain\">Public Domain</md-checkbox></p>\n\n    <p>\n      <span><p-multiSelect [options]=\"tags\" name=\"tags\" [(ngModel)]=\"selectedReference.tags\"\n                           defaultLabel=\"Tags\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect></span>\n\n      <span><p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"selectedReference.access\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"\n                           (onChange) =\"changeAccess($event, selectedReference)\"></p-multiSelect></span>\n    </p>\n</md-dialog-content>\n\n  <md-dialog-actions>\n  <span><button type=\"submit\" md-button>Save</button></span>\n     <span *ngIf=\"selectedReference.getFlag('new')\">\n           <button type=\"button\" md-button (click)=\"deleteRef(selectedReference)\">Cancel</button>\n     </span>\n     <span *ngIf=\"!selectedReference.getFlag('new')\">\n           <button type=\"button\" md-button (click)=\"deleteRef(selectedReference)\">Delete</button>\n     </span>\n  </md-dialog-actions>\n\n</form>\n"

/***/ }),

/***/ 985:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"ref-edit-form\" #refEditForm=\"ngForm\">\n  <br/>\n  <md-grid-list cols=\"10\" gutterSize=\"2px\" rowHeight=\"70px\" [style.background]=\"'#EBF5FC'\">\n    <md-grid-tile [colspan]=1 [rowspan]=1>Citation</md-grid-tile>\n    <md-grid-tile [colspan]=7 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <textarea mdInput name=\"citation\" required [(ngModel)]=\"ref.citation\"></textarea>\n      </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n\n   <md-grid-tile [colspan]=3 [rowspan]=1>\n     <md-select placeholder=\"Source Type\" name=\"type\" [(ngModel)]=\"ref.docType\" [style.width]=\"'80%'\">\n       <md-option *ngFor=\"let type of sourceTypeList\" [value]=\"type.value\">{{ type.label }}</md-option>\n     </md-select>\n   </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n    <!--<md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Source Class\" name=\"sClass\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let type of documentSystemType\" [value]=\"type.value\" disabled>{{ type.label }}</md-option>\n      </md-select>\n    </md-grid-tile>-->\n    <md-grid-tile [colspan]=1 [rowspan]=1>Source ID</md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1><md-input-container class=\"ref-full-width\">\n      <input mdInput placeholder=\"Source ID\" name=\"sourceId\" [(ngModel)]=\"ref.id\">\n    </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=3 [rowspan]=1></md-grid-tile>\n\n\n    <md-grid-tile [colspan]=1 [rowspan]=1>Reference URL</md-grid-tile>\n    <md-grid-tile [colspan]=5 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"url\" value=\"{{ref.url}}\" [(ngModel)]=\"ref.url\">\n      </md-input-container>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n   <!-- <input type=\"file\" (change)=\"fileUploader($event)\" name=\"fileUpload\" [(ngModel)]=\"ref.uploadedFile\" placeholder=\"Upload file\" accept=\".pdf,.doc,.docx, .txt\">\n      <p>test <a href={{ref.uploadedFile}} target=\"_blank\"><i class=\"fa fa-file-text fa-2x success\"></i></a> </p>-->\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <md-checkbox class=\"ref-full-width\" name=\"pubDomain\" [(ngModel)]=\"ref.publicDomain\">Public Domain</md-checkbox>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <p-multiSelect [options]=\"tags\" name=\"tags\" [(ngModel)]=\"ref.tags\"\n                             defaultLabel=\"Tags\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"ref.access\"\n                           defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"\n                     (onChange) =\"changeAccess($event, ref)\"></p-multiSelect>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n\n\n    <md-grid-tile [colspan]=10 [rowspan]=1 *ngIf=\"ref.uuid\">\n      Created by <code>&nbsp;{{ref.createdBy}}&nbsp;</code> on <code>&nbsp; {{ref.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{ref.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{ref.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=7 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"deleteRef(ref)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n    </md-grid-tile>\n<!--    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"updateRef(ref,$event)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n    </md-grid-tile>-->\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n</form><br/>\n"

/***/ }),

/***/ 986:
/***/ (function(module, exports) {

module.exports = "<form>\n  <div md-dialog-title align=\"center\">\n    <span>Select Reference</span> <span><button type=\"button\" md-button md-dialog-close><md-icon>clear</md-icon></button></span>\n    <br/><br/>\n    <div>\n      <md-button-toggle-group #group=\"mdButtonToggleGroup\" name=\"referenceView\" [(ngModel)]=\"referenceViewTab\">\n        <md-button-toggle value=\"all\">All</md-button-toggle>\n        <md-button-toggle value=\"selected\">Selected</md-button-toggle>\n        <md-button-toggle value=\"last5\">Last 5 Saved</md-button-toggle>\n      </md-button-toggle-group>\n    </div>\n  </div>\n\n\n  <md-dialog-content>\n    <md-input-container dense>\n      <input mdInput placeholder=\"Filter References\" [formControl]=\"refCtrl\"  [(ngModel)]=\"filterQuery\">\n     </md-input-container>\n    <p *ngIf= \"allRefsForDataFiltered.length == 0\">\n      No References to display\n    </p>\n    <md-list dense>\n      <md-list-item *ngFor=\"let ref of allRefsForDataFiltered\">\n\n        <p md-line class=\"title-name\" style=\"overflow-y: hidden;max-height: 20px;\">\n            <span><md-checkbox [checked]=isChecked(ref) (change)=\"toggle(ref)\" class=\"no-display\"></md-checkbox></span>\n             <span>{{ref.index}}. {{ref.citation}}</span> </p>\n        <span><b>{{ref.access}}</b></span>\n        <span><md-icon class=\"md-secondary\" (click)=\"openRefEditDialog(ref)\">\n          <i class=\"material-icons\" title=\"click to edit\">chevron_right</i>\n        </md-icon></span>\n      </md-list-item>\n    </md-list>\n</md-dialog-content>\n\n  <md-divider></md-divider>\n<md-dialog-actions class=\"ofhidden\">\n <button md-button (click)=\"openRefEditDialog()\">Create New</button>\n  <button md-button (click)=\"apply()\" md-dialog-close>Apply Selected</button>\n  <button md-button (click)=\"applySelectedToAllRecords()\" md-dialog-close>Apply to All New Names</button>\n  <!--<div><p><button md-button (click)=\"applySelectedToAllRecords()\" md-dialog-close>Apply Selected to All Records</button></p></div>-->\n <!-- <div>\n    <button md-button [mdMenuTriggerFor]=\"menu\">Select Action</button>\n    <md-menu #menu=\"mdMenu\">\n      <button md-menu-item (click)=\"apply()\" md-dialog-close> Apply </button>\n      <button md-menu-item (click)=\"applySelectedToAllRecords()\" md-dialog-close> Apply Selected to All Records </button>\n    </md-menu>\n  </div>-->\n</md-dialog-actions>\n</form>\n\n"

/***/ }),

/***/ 987:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"refs\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\" >\n  <!--[rows]=\"10\" [paginator]=\"true\" [pageLinks]=\"3\" [rowsPerPageOptions]=\"[5,10,20]\"-->\n <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addReference(true)\" label=\"Add Reference\"></button>\n    <!--  <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"refs.length > 0\" (click)=\"exportRefs()\" label=\"Export References\"></button>\n      <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"refs.length > 0\" (click)=\"search()\" label=\"Search\"></button>-->\n    </div>\n  </p-header>\n  <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n    <template pTemplate=\"header\">Edit</template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'80px'}\">\n    <template pTemplate=\"header\">Delete</template>\n    <template pTemplate=\"body\" let-ref=\"rowData\">\n      <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteRef(ref)\"></button>\n    </template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'38px'}\">\n    <template pTemplate=\"header\">PD</template>\n    <template let-ref=\"rowData\" pTemplate=\"body\">\n      <md-checkbox [(ngModel)]=\"ref.publicDomain\" [checked]=\"ref.publicDomain\" name=\"publicDomain\"  pTooltip=\"Public Domain\"></md-checkbox>\n    </template>\n  </p-column>\n\n  <p-column field=\"citation\" header=\"Citation / Source Text\"  [style]=\"{'width':'500px'}\" [editable]=\"true\" [sortable]=\"true\"></p-column>\n\n  <p-column field=\"docType\" header=\"Source Type\" [editable]=\"false\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-ref=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"ref[col.field]\" [options]=\"sourceTypes\" [autoWidth]=\"false\" filter=\"filter\" [style]=\"{'width':'100%'}\"\n                  class=\"dropdown-text\" required=\"true\" placeholder=\"Select Source Type\"></p-dropdown>\n    </template>\n  </p-column>\n\n  <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-ref=\"rowData\" pTemplate=\"body\">\n    <div *ngIf=\"ref.access.length == 0\"> Public </div>\n    <div *ngIf=\"ref.access.length > 0\"> {{ref.access}} </div>\n      </template>\n\n    <template let-col let-ref=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"ref[col.field]\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                     (onChange) =\"changeAccess($event, ref)\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column field=\"url\" header=\"Reference URL\"  [style]=\"{'width':'500px'}\" [editable]=\"true\">\n    <template let-col let-ref=\"rowData\" pTemplate=\"body\">\n      <a href=\"{{ref.url}}\" target=\"_blank\">{{ref.url}}</a>\n    </template>\n  </p-column>\n\n  <template let-ref pTemplate=\"rowexpansion\">\n    <div>\n      <reference-edit [ref]=\"ref\"></reference-edit>\n    </div>\n  </template>\n\n  <p-footer *ngIf=\"refs.length > 3\">\n    <div class=\"ui-dialog-buttonpane ui-helper-clearfix\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addReference(false)\" label=\"Add Reference\"></button>\n    </div>\n  </p-footer>\n\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n\n<!--<div>\n  <pre>\n  {{refs | json}}\n    </pre>\n</div>-->\n"

/***/ }),

/***/ 988:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"relationship-edit-form\" #relationEditForm=\"ngForm\">\n  <md-grid-list cols=\"6\" gutterSize=\"2px\" rowHeight=\"60px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=1 [rowspan]=rowCount> <b>Related Substance</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=rowCount>\n\n      <div *ngIf=\"relationship.relatedSubstance != null\" class=\"rel-sub-position\" >\n      <span>{{relationship.relatedSubstance.approvalID}}</span><br/>\n        <span>\n            <img src=\"{{browserurl}}/app/img/{{relationship.relatedSubstance.refuuid}}.svg?size=50\" alt=\"structure\"\n                 style=\"height: 100px; width: 100px;\">\n        </span><br/>\n        <span>{{relationship.relatedSubstance.refPname}}</span> &nbsp; &nbsp;\n\n        <span><button md-raised-button (click)=\"changeSelection()\">Change</button> &nbsp;</span>\n        <!--<span><button md-raised-button (click)=\"isEdit=true\">Clear</button> &nbsp; </span>-->\n      </div>\n      <div class=\"rel-sub-position\">\n        <div *ngIf=\"isEdit && !relationship.relatedSubstance\">\n        <p-autoComplete  name=\"autoComp\" [suggestions]=\"filterSuggestions\" class=\"dropdown-text\" title=\"Search by Name, UUID or Approval ID\"\n                      (completeMethod)=\"getSuggestions($event, false)\" (onSelect)=\"searchSubstance($event, false)\" [(ngModel)]=\"tmp\" overlayVisible=\"true\"></p-autoComplete>\n          <span><button (click)=\"searchSubstance(tmp)\" md-raised-button>Search</button> &nbsp;</span>\n          <span><button (click)=\"cancelSearch()\" md-raised-button>Cancel</button> &nbsp; </span>\n        </div>\n        <span *ngIf=\"!relationship.relatedSubstance && isEdit != true\">\n          <button md-raised-button (click)=\"isEdit=true\">Select Substance</button> &nbsp;\n        </span>\n      </div>\n      <div *ngIf=\"searchResult && searchResult.length > 0\" style=\"display: inherit;\">\n        <span *ngFor=\"let sr of searchResult \">\n        <div>{{sr.approvalID}}</div>\n        <div><img src=\"{{browserurl}}/app/img/{{sr.refuuid}}.svg?size=50\" alt=\"structure\"\n                  style=\"height: 100px; width: 100px;\"></div>\n        <p>{{sr.refPname}} &nbsp; &nbsp;\n          <button md-raised-button (click)=\"applySubstance(sr)\"> Apply </button>\n        </p>\n          </span>\n      </div>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=rowCount> <b>Mediator Substance</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=rowCount>\n\n      <div *ngIf=\"relationship.mediatorSubstance != null\" class=\"rel-sub-position\">\n        <span>{{relationship.mediatorSubstance.approvalID}}</span><br/>\n        <span>\n          <img src=\"{{browserurl}}/app/img/{{relationship.mediatorSubstance.refuuid}}.svg?size=50\" alt=\"structure\"\n               style=\"height: 100px; width: 100px;\">\n        </span>\n        <br/><span>{{relationship.mediatorSubstance.refPname}}</span> &nbsp; &nbsp;\n        <span><button md-raised-button (click)=\"changeMSelection()\">Change</button> &nbsp; </span>\n        <!--<span><button md-raised-button (click)=\"ismEdit=true\"\">Clear</button> &nbsp; </span>-->\n      </div>\n      <div class=\"rel-sub-position\">\n        <div *ngIf=\"ismEdit && !relationship.mediatorSubstance\">\n        <p-autoComplete name=\"autoComp\" [suggestions]=\"filterSuggestions\" class=\"dropdown-text\" title=\"Search by Name, UUID or Approval ID\"\n                        (completeMethod)=\"getSuggestions($event, true)\" (onSelect)=\"searchSubstance($event, true)\" [(ngModel)]=\"tmp1\"></p-autoComplete>\n          <span><button md-raised-button (click)=\"searchSubstance(tmp1, true)\">Search</button> &nbsp;</span>\n          <span><button md-raised-button (click)=\"cancelMSearch()\">Cancel</button> &nbsp; </span>\n        </div>\n        <span *ngIf=\"!relationship.mediatorSubstance && ismEdit != true\">\n            <button md-raised-button (click)=\"ismEdit=true\">Select Substance</button> &nbsp;\n        </span>\n      </div>\n      <div *ngIf=\"(sstate==='searching')\" style=\"text-transform: capitalize\">\n        <md-spinner strokeWidth=\"5px\"></md-spinner>\n        {{sstate }} ...</div>\n      <br/>\n      <div *ngIf=\"searchMResult && searchMResult.length > 0\" style=\"display: inherit;\" >\n          <div *ngFor=\"let msr of searchMResult\">\n          <p> {{msr.approvalID}}</p>\n          <p style=\"height: 100px; width: 100px;\"><img src=\"{{browserurl}}/app/img/{{msr.refuuid}}.svg?size=100\" alt=\"structure\"></p>\n          <p>{{msr.refPname}} &nbsp; &nbsp;\n            <button md-raised-button (click)=\"applyMSubstance(msr)\">Apply</button>\n        </p></div>\n      </div>\n    </md-grid-tile>\n\n\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Relationship Type</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Relationship Type\" name=\"type\" [(ngModel)]=\"relationship.type\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let type of relationshipTypes\" [value]=\"type.value\" overlayVisible=\"true\">{{ type.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Access</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"relationship.access\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"\n                     (onChange) =\"changeAccess($event, relationship)\">\n      </p-multiSelect>\n    </md-grid-tile>\n\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Qualification</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Qualification\" name=\"qualification\" [(ngModel)]=\"relationship.qualification\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let qual of qualification\" [value]=\"qual.value\" overlayVisible=\"true\">{{ qual.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Interaction Type</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Interaction Type\" name=\"interactionType\" [(ngModel)]=\"relationship.interactionType\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let iType of interactionType\" [value]=\"iType.value\" overlayVisible=\"true\">{{ iType.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Amount</b></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <div> <a class=\"anchor-underline\" (click)=\"amountDisplay = !amountDisplay\"> {{showAmounts(relationship.amount) || \"Add Amount\"}}</a></div>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Comments</b></md-grid-tile>\n    <md-grid-tile [colspan]=3 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"relationship\" value=\"{{relationship.comments}}\" [(ngModel)]=\"relationship.comments\" placeholder=\"comments ...\">\n      </md-input-container>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=6 [rowspan]=2 class=\"color-bg\" id=\"amountDisplayid\" *ngIf=\"amountDisplay\">\n      <div><span><amount [amount]=relationship.amount (onSaved)=\"onSaved($event)\"></amount></span></div>\n    </md-grid-tile>\n\n   <md-grid-tile [colspan]=6 [rowspan]=1 *ngIf=\"relationship.createdBy\">\n      Created by <code>&nbsp;{{relationship.createdBy}}&nbsp;</code> on <code>&nbsp; {{relationship.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{relationship.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{relationship.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n       <md-grid-tile [colspan]=2 [rowspan]=1>\n         <button type=\"button\" (click)=\"closeRow(relationship, true)\" pButton icon=\"fa-check\" label=\"Ok\"></button>\n         <button type=\"button\" (click)=\"deleteRelationship(relationship)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n       </md-grid-tile>\n       <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 989:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"relations\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\" id=\"relationListTable\" [style]=\"{'width':'100%'}\"\n              #dt>\n  <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addRelationship(true)\" label=\"Add Relationship\"></button>\n<!--      <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"relations.length > 0\" (click)=\"exportRelations()\" label=\"Export Relationships\"></button>\n      <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"relations.length > 0\" (click)=\"search()\" label=\"Search\"></button>-->\n    </div>\n  </p-header>\n    <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n      <template pTemplate=\"header\">Edit</template>\n    </p-column>\n\n    <p-column [style]=\"{'width':'80px'}\">\n      <template pTemplate=\"header\">Delete</template>\n      <template pTemplate=\"body\" let-relation=\"rowData\">\n        <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteRelation(relation)\"></button>\n      </template>\n    </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\" [style]=\"{'width':'180px'}\">\n    <template pTemplate=\"header\">Related Substance <br/>\n      <md-checkbox [checked]=\"showRSStruct\" name=\"showRSStruct\" (change)=\"this.showRSStruct = !this.showRSStruct;\" *ngIf=\"relations.length > 0\">Show Structure</md-checkbox>\n    </template>\n    <template let-col let-relation=\"rowData\" pTemplate=\"body\">\n      <a *ngIf=\"relation.relatedSubstance == '' || relation.relatedSubstance == null\" (click)=\"dt.toggleRow(relation)\" >Select...</a>\n      <div *ngIf=\"relation.relatedSubstance != '' && relation.relatedSubstance != null \">\n      <div *ngIf=\"showRSStruct\">\n          <img src=\"{{browserurl}}/app/img/{{relation.relatedSubstance.refuuid}}.svg?size=150\" alt=\"structure\"\n          class=\"small-image\">\n        </div>\n        <span (click)=\"dt.toggleRow(relation)\">{{relation.relatedSubstance.refPname}}</span>\n      </div>\n    </template>\n  </p-column>\n\n  <p-column field=\"type\" header=\"Relationship Type\" [filter]=\"true\" [style]=\"{'overflow':'visible'}\" filterMatchMode=\"equals\">\n    <template pTemplate=\"filter\" let-col>\n      <p-dropdown [options]=\"filterRelationTypes\" [style]=\"{'width':'100%'}\" defaultLabel=\"All Types\" class=\"dropdown-text\"\n                  (onChange)=\"dt.filter($event.value,col.field,col.filterMatchMode)\" [(ngModel)]=\"selectedFilterType\" styleClass=\"ui-column-filter\"></p-dropdown>\n    </template>\n    <template let-col let-relation=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"relation[col.field]\" [options]=\"relationshipTypes\" [style]=\"{'width':'100%'}\" required=\"true\" placeholder=\"type...\"\n      (onChange)=\"loadOptions()\" [autoWidth]=\"false\" class=\"dropdown-text\" filter=\"filter\"></p-dropdown>\n    </template>\n  </p-column>\n\n\n\n  <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-relation=\"rowData\" pTemplate=\"body\">\n      <div *ngIf=\"relation.access.length == 0\"> Public </div>\n      <div *ngIf=\"relation.access.length > 0\"> {{relation.access}} </div>\n    </template>\n\n    <template let-col let-relation=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"relation[col.field]\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                     (onChange) =\"changeAccess($event, relation)\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\">\n    <template pTemplate=\"header\">Amount</template>\n    <template let-col let-relation=\"rowData\" pTemplate=\"body\">\n      <div *ngIf=\"relation.amount\"> {{showAmounts(relation.amount)}}</div>\n    </template>\n  </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\">\n    <template pTemplate=\"header\">Qualification / <br/> Interaction Type</template>\n    <template let-col let-relation=\"rowData\" pTemplate=\"body\">\n      {{relation.qualification}}\n      <div *ngIf=\"relation.qualification && relation.interactionType\"> / </div>\n      {{relation.interactionType}}\n    </template>\n  </p-column>\n\n  <p-column>\n    <template pTemplate=\"header\">References</template>\n    <template let-relation=\"rowData\"  pTemplate=\"body\">\n      <div *ngIf=\"!relation.references || relation.references.length == 0\" (click)=\"openRefListDialog(relation)\">Click</div>\n      <div (click)=\"openRefListDialog(relation)\" *ngIf=\"relation.references\">\n        {{showReferenceIndexes(relation)}}\n      </div>\n    </template>\n\n  </p-column>\n  <template let-relation pTemplate=\"rowexpansion\">\n    <div>\n      <relation-edit [dt]=dt [relationship]=relation></relation-edit>\n    </div>\n  </template>\n  <p-footer *ngIf=\"relations.length > 2\">\n    <div class=\"ui-dialog-buttonpane ui-helper-clearfix\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addRelationship(false)\" label=\"Add Relationship\"></button>\n    </div>\n  </p-footer>\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n"

/***/ }),

/***/ 990:
/***/ (function(module, exports) {

module.exports = "<head>\n  <title>{{displayName}}</title>\n</head>\n<div class=\"demo-nav-bar\">\n  <nav md-tab-nav-bar aria-label=\"Substance properties\">\n    <a md-tab-link\n       *ngFor=\"let tabLink of tabs; let i = index\"\n       [routerLink]=\"tabLink.content\"\n       [active]=\"activeLinkIndex === i\"\n       (click)=\"activeLinkIndex = i; tabChange()\">\n      {{tabLink.header || tabLink.content | uppercase}} {{length || tabLink.value?.count || ''}}\n    </a>\n  </nav>\n  <!--<p-messages [(value)]=\"msgs\"></p-messages>-->\n  <!--<div>{{message}}</div>-->\n  <div class=\"text-bold\"><br/>{{displayName}}<br/><br/></div>\n  <router-outlet>\n    <div *ngIf=\"load==='loading'\">\n      <md-spinner strokeWidth=\"5px\"></md-spinner>\n      {{load}}\n    </div>\n  </router-outlet>\n  <div id=\"hidden-things\" ></div>\n\n  <div class=\"footer-controls\">\n  <div *ngFor=\"let mm of msgs\" style=\"padding-right:40px\">\n    <p-messages [(value)]=mm.msgs (click)=\"closemsg()\">\n    </p-messages>\n\n    <div *ngIf=\"mm.msgs.length > 0\">\n    <div *ngFor=\"let link of mm.links\" >\n    <a href=\"{{browserurl}}{{link.href}}\" target=\"_blank\">{{link.text}}</a>\n      </div>\n    </div>\n    </div>\n\n    <div *ngIf=\"(state==='validating' || state==='submitting')\" style=\"text-transform: capitalize\">\n      <md-spinner strokeWidth=\"5px\"></md-spinner>\n      {{state }} ...\n    </div>\n    <div>\n    <br/>\n    <button [disabled]=\"(state==='validating' || state==='submitting')\" type=\"submit\" pButton [style]=\"{display:block}\" (click)=\"validate(false)\" label=\"Save Changes\" class=\"button-down\"></button>\n    <button *ngIf=\"state==='validated-warning' || state==='validated-error'\"  [disabled]=\"state==='validated-error'\"\n            type=\"submit\" pButton [style]=\"{display:block}\" (click)=\"save()\" label=\"Dismiss Warnings & Submit\" class=\"button-down-right\"></button>\n    </div>\n  </div>\n</div>\n"

/***/ }),

/***/ 991:
/***/ (function(module, exports) {

module.exports = "<p>\n  Landing page for displaying substancelist!\n</p>\n"

/***/ }),

/***/ 992:
/***/ (function(module, exports) {

module.exports = "<div id=\"sum-internal\">\n<iframe id=\"subFrame\" load=\"onLoad()\" width=\"100%\" height=\"999\">\n  <p>Your browser does not support iframes.</p>\n</iframe>\n</div>\n"

/***/ })

},[1254]);
//# sourceMappingURL=main.bundle.js.map