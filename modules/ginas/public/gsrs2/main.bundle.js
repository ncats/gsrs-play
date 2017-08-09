webpackJsonp([1,4],{

/***/ 108:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_forms__ = __webpack_require__(7);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_rxjs_add_operator_startWith__ = __webpack_require__(277);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_rxjs_add_operator_startWith___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_2_rxjs_add_operator_startWith__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__reference__ = __webpack_require__(110);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__reference_list_service__ = __webpack_require__(109);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__reference_component__ = __webpack_require__(443);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__angular_material__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_lodash__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_7_lodash__);
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
    function ReferenceListDialog(referenceService, refDialog) {
        this.referenceService = referenceService;
        this.refDialog = refDialog;
        this._referenceViewTab = "all";
        this._filterQuery = "";
        this.closeFunction = function () { }; //the specific function to call on close
        this.allReferences = []; //every reference currently present (master list) [full]
        this.allRefsForData = []; //those currently displayable
        this.allRefsForDataFiltered = []; //those currently displayed
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
    ReferenceListDialog.prototype.toggleReferences = function (value) {
        console.log("Clicked:" + value);
        if (value == "all") {
            this.setRefsToAll();
        }
        else if (value == "last5") {
            this.setRefsFromAPIMatchingUser("admin");
        }
        else if (value == "selected") {
            this.setRefsForData(this.data);
        }
    };
    //Set what references will be displayed
    ReferenceListDialog.prototype.setDisplayRefs = function (refs) {
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
    };
    ReferenceListDialog.prototype.isChecked = function (ref) {
        var has = false;
        if (this.data) {
            has = this.data.hasReference(ref);
        }
        return has;
    };
    ReferenceListDialog.prototype.applySelectedToAllNames = function () {
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
    ReferenceListDialog.prototype.saveChanges = function () {
        //    this.close();
    };
    ReferenceListDialog.prototype.openRefEditDialog = function (selectedReference) {
        var _this = this;
        //if there is no reference, make one and add it
        if (!selectedReference) {
            selectedReference = new __WEBPACK_IMPORTED_MODULE_3__reference__["a" /* Reference */]();
            selectedReference.generateNewUuid();
            selectedReference.setFlag("new", true);
            selectedReference.index = this.allReferences.length + 1;
            this.allReferences.push(selectedReference);
        }
        var refEditDialog = this.refDialog.open(__WEBPACK_IMPORTED_MODULE_5__reference_component__["a" /* ReferenceEditDialog */], { height: '700px', width: '500px' });
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
            template: __webpack_require__(794),
            //styleUrls: ['../styles.css']
            providers: [__WEBPACK_IMPORTED_MODULE_4__reference_list_service__["a" /* ReferenceListService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_4__reference_list_service__["a" /* ReferenceListService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__reference_list_service__["a" /* ReferenceListService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_6__angular_material__["b" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__angular_material__["b" /* MdDialog */]) === 'function' && _b) || Object])
    ], ReferenceListDialog);
    return ReferenceListDialog;
    var _a, _b;
}());
//# sourceMappingURL=reference-list-dialog.component.js.map

/***/ }),

/***/ 109:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__reference__ = __webpack_require__(110);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_lodash__ = __webpack_require__(23);
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
    ReferenceListService.prototype.getAllReferences = function (uuid) {
        //should do something to pass the UUID somewhere
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(uuid)
                .andThen(function (s) { return s.fetch("/references!sort(created)"); })
                .andThen(function (refs) {
                var i = 1;
                var mrefs = __WEBPACK_IMPORTED_MODULE_2_lodash__["chain"](refs)
                    .map(function (r) { return new __WEBPACK_IMPORTED_MODULE_0__reference__["a" /* Reference */]().merge(r); })
                    .map(function (r) { r.index = i++; return r; })
                    .value();
                return mrefs;
            })
                .get(function (refs) { return resolve(refs); });
        });
    };
    //gets the last 5 references by username "admin" for now
    //TODO: Needs sort
    ReferenceListService.prototype.getLastFiveReferences = function (username) {
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

/***/ 110:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__data__ = __webpack_require__(37);
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

/***/ 111:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__codes_code__ = __webpack_require__(168);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__references_reference__ = __webpack_require__(110);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__names_name__ = __webpack_require__(169);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_lodash__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_4_lodash__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return SubstanceService; });
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var SubstanceService = (function () {
    function SubstanceService() {
    }
    SubstanceService.prototype.setUuid = function (uuid) {
        SubstanceService.uuid = uuid;
        console.log("set uuid");
        console.log(uuid);
    };
    SubstanceService.prototype.getNames = function () {
        var _this = this;
        if (SubstanceService.names !== null)
            return Promise.resolve(SubstanceService.names);
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(SubstanceService.uuid)
                .andThen(function (s) { return s.fetch("/names"); })
                .andThen(function (names) { return _this.storeNames(names); })
                .get(function (names) { return resolve(names); });
        });
    };
    SubstanceService.prototype.getReferences = function () {
        var _this = this;
        if (SubstanceService.refs !== null)
            return Promise.resolve(SubstanceService.refs);
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(SubstanceService.uuid)
                .andThen(function (s) { return s.fetch("/references!sort(created)"); })
                .andThen(function (refs) { return _this.storeReferences(refs); })
                .get(function (refs) { return resolve(refs); });
        });
    };
    SubstanceService.prototype.getCodes = function () {
        var _this = this;
        if (SubstanceService.codes !== null)
            return Promise.resolve(SubstanceService.codes);
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(SubstanceService.uuid)
                .andThen(function (s) { return s.fetch("/codes"); })
                .andThen(function (codes) { return _this.storeCodes(codes); })
                .get(function (codes) { return resolve(codes); });
        });
    };
    SubstanceService.prototype._getFullJson = function () {
        if (SubstanceService.substance)
            return Promise.resolve(SubstanceService.substance);
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(SubstanceService.uuid)
                .andThen(function (s) { return s.full(); })
                .andThen(function (sub) {
                SubstanceService.substance = sub;
            })
                .get(function (sub) { return resolve(sub); });
        });
    };
    SubstanceService.prototype.getFullJson = function () {
        return this._getFullJson()
            .then(function (s) {
            if (SubstanceService.names)
                s["names"] = SubstanceService.names;
            if (SubstanceService.refs)
                s["references"] = SubstanceService.refs;
            if (SubstanceService.codes)
                s["codes"] = SubstanceService.codes;
            return s;
        });
    };
    SubstanceService.prototype.storeCodes = function (codes) {
        var cds = __WEBPACK_IMPORTED_MODULE_4_lodash__["map"](codes, function (n) {
            return (new __WEBPACK_IMPORTED_MODULE_1__codes_code__["a" /* Code */]().merge(n));
        });
        SubstanceService.codes = cds;
        return cds;
    };
    SubstanceService.prototype.storeNames = function (names) {
        var mnames = __WEBPACK_IMPORTED_MODULE_4_lodash__["map"](names, function (n) {
            return (new __WEBPACK_IMPORTED_MODULE_3__names_name__["a" /* Name */]().merge(n));
        });
        SubstanceService.names = mnames;
        return mnames;
    };
    SubstanceService.prototype.storeReferences = function (refs) {
        var i = 1;
        var mrefs = __WEBPACK_IMPORTED_MODULE_4_lodash__["chain"](refs)
            .map(function (r) { return new __WEBPACK_IMPORTED_MODULE_2__references_reference__["a" /* Reference */]().merge(r); })
            .map(function (r) { r.index = i++; return r; })
            .value();
        SubstanceService.refs = mrefs;
        return mrefs;
    };
    SubstanceService.prototype.setFullJson = function (substance) {
        SubstanceService.substance = substance;
        this.storeCodes(substance["codes"]);
        this.storeNames(substance["names"]);
        this.storeReferences(substance["references"]);
        // SubstanceService.substance
    };
    SubstanceService.names = null;
    SubstanceService.refs = null;
    SubstanceService.codes = null;
    SubstanceService.relations = null;
    SubstanceService.substance = "";
    SubstanceService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], SubstanceService);
    return SubstanceService;
}());
//# sourceMappingURL=substanceedit.service.js.map

/***/ }),

/***/ 168:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(170);
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

/***/ 169:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(170);
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

/***/ 170:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__data__ = __webpack_require__(37);
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

/***/ 19:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_lodash__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_lodash__);
/* unused harmony export CV_DOMAIN_TYPES */
/* unused harmony export ACCESS */
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
var ACCESS = [
    //{value: 'public', label:'Public'},
    { value: 'cber', label: 'CBER' },
    { value: 'cder', label: 'CDER' },
    { value: 'cvm', label: 'CVM' },
    { value: 'protected', label: 'Protected' },
    { value: 'admin', label: 'Admin' }];
var COMMON_TABS = [
    { header: 'Names', content: 'names' },
    { header: 'References', content: 'references' },
    { header: 'Codes', content: 'codes' },
    /* {header: 'Relationships', content:'relationships'},
     {header: 'Notes', content:'notes'},
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
    CVService.prototype.getAccess = function () {
        //return this.getCVLists("ACCESS_GROUP");
        return Promise.resolve(ACCESS);
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
        console.log("in here");
        return new Promise(function (resolve, reject) {
            window["GGlob"].CVFinder
                .searchByDomain(cvDomain)
                .andThen(function (cnt) { return cnt.content; })
                .andThen(function (cvs) {
                console.log(cvs);
                var mcvs = __WEBPACK_IMPORTED_MODULE_1_lodash__["chain"](cvs)
                    .flatMap(function (cv) { return cv.terms; })
                    .map(function (t) {
                    t.label = t.display;
                    return t;
                })
                    .value();
                return mcvs;
            })
                .get(function (cvs) { return resolve(cvs); });
        });
    };
    CVService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], CVService);
    return CVService;
}());
//# sourceMappingURL=cv.service.js.map

/***/ }),

/***/ 37:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_lodash__ = __webpack_require__(23);
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

/***/ 434:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__amount_model__ = __webpack_require__(435);
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
        this.getAmountType();
        this.getAmountUnits();
    };
    AmountComponent.prototype.saveAmount = function (amount) {
        console.log("save amount");
        console.log(amount);
    };
    AmountComponent.prototype.clearAmount = function (amount) {
        console.log("clear amount");
        console.log(amount);
        this.amount = new __WEBPACK_IMPORTED_MODULE_2__amount_model__["a" /* Amount */]();
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__amount_model__["a" /* Amount */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__amount_model__["a" /* Amount */]) === 'function' && _a) || Object)
    ], AmountComponent.prototype, "amount", void 0);
    AmountComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'amount',
            template: __webpack_require__(782),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object])
    ], AmountComponent);
    return AmountComponent;
    var _a, _b;
}());
//# sourceMappingURL=amount.component.js.map

/***/ }),

/***/ 435:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__data__ = __webpack_require__(37);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Amount; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var Amount = (function (_super) {
    __extends(Amount, _super);
    function Amount() {
        _super.apply(this, arguments);
    }
    return Amount;
}(__WEBPACK_IMPORTED_MODULE_0__data__["a" /* Data */]));
//# sourceMappingURL=amount.model.js.map

/***/ }),

/***/ 436:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_primeng_primeng__ = __webpack_require__(59);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__references_reference_list_service__ = __webpack_require__(109);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__code__ = __webpack_require__(168);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__ = __webpack_require__(56);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__substanceedit_substanceedit_service__ = __webpack_require__(111);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7__references_reference_list_dialog_component__ = __webpack_require__(108);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__angular_material__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9_lodash__ = __webpack_require__(23);
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
    function CodeListComponent(referenceListService, confirmationService, cvService, dialog, utilService, substanceService) {
        this.referenceListService = referenceListService;
        this.confirmationService = confirmationService;
        this.cvService = cvService;
        this.dialog = dialog;
        this.utilService = utilService;
        this.substanceService = substanceService;
        this.codes = [];
        this.recordUUID = "00006eea-e2d2-4d79-99ff-30f17b3dd740";
        this.references = [];
    }
    CodeListComponent.prototype.ngOnInit = function () {
        this.getReferences();
        this.getAccess();
        this.getCodeSystem();
        this.getCodes();
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
    CodeListComponent.prototype.findSelectedRefIndex = function (code) {
        return this.codes.indexOf(code);
    };
    CodeListComponent.prototype.deleteCode = function (code) {
        this.codes.splice(this.findSelectedRefIndex(code), 1);
    };
    CodeListComponent.prototype.addCode = function () {
        console.log("add");
        this.newCode = true;
        this.nCode = new __WEBPACK_IMPORTED_MODULE_4__code__["a" /* Code */]();
        this.codes.push(this.nCode);
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
        this.substanceService
            .getReferences()
            .then(function (refs) {
            _this.references = refs;
        });
    };
    CodeListComponent.prototype.getCodes = function () {
        var _this = this;
        this.substanceService
            .getCodes()
            .then(function (codes) {
            _this.codes = codes;
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
        this.cvService
            .getList("CODE_SYSTEM")
            .then(function (codeSystem) { return _this.codeSystem = codeSystem; });
    };
    CodeListComponent.prototype.saveCodeListChanges = function ($event, codes) {
        console.log("save code list changes");
        console.log(codes);
    };
    CodeListComponent.prototype.changeAccess = function ($event, code) {
        this.utilService.changeAccess($event, code);
    };
    CodeListComponent.prototype.openRefListDialog = function (name) {
        console.log("reference list");
        var dialogRef = this.dialog.open(__WEBPACK_IMPORTED_MODULE_7__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], { height: '550px', width: '400px' });
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
            template: __webpack_require__(784),
            //styleUrls: ['../styles.css']
            providers: [__WEBPACK_IMPORTED_MODULE_2__references_reference_list_service__["a" /* ReferenceListService */], __WEBPACK_IMPORTED_MODULE_1_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_6__substanceedit_substanceedit_service__["a" /* SubstanceService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__references_reference_list_service__["a" /* ReferenceListService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__references_reference_list_service__["a" /* ReferenceListService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_1_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1_primeng_primeng__["ConfirmationService"]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__cv_cv_service__["a" /* CVService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_8__angular_material__["b" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_8__angular_material__["b" /* MdDialog */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */]) === 'function' && _e) || Object, (typeof (_f = typeof __WEBPACK_IMPORTED_MODULE_6__substanceedit_substanceedit_service__["a" /* SubstanceService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__substanceedit_substanceedit_service__["a" /* SubstanceService */]) === 'function' && _f) || Object])
    ], CodeListComponent);
    return CodeListComponent;
    var _a, _b, _c, _d, _e, _f;
}());
//# sourceMappingURL=code-list.component.js.map

/***/ }),

/***/ 437:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__name__ = __webpack_require__(169);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__name_service__ = __webpack_require__(438);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__references_reference_list_service__ = __webpack_require__(109);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__angular_material__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__references_reference_list_dialog_component__ = __webpack_require__(108);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_primeng_primeng__ = __webpack_require__(59);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_7_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__utils_utils_service__ = __webpack_require__(56);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9_rxjs_add_operator_switchMap__ = __webpack_require__(179);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9_rxjs_add_operator_switchMap___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_9_rxjs_add_operator_switchMap__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_10__angular_router__ = __webpack_require__(18);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_11_lodash__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_11_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_11_lodash__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_12__substanceedit_substanceedit_service__ = __webpack_require__(111);
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
    function NameListComponent(nameService, referenceListService, cvService, dialog, confirmationService, utilService, route, router, substanceService) {
        this.nameService = nameService;
        this.referenceListService = referenceListService;
        this.cvService = cvService;
        this.dialog = dialog;
        this.confirmationService = confirmationService;
        this.utilService = utilService;
        this.route = route;
        this.router = router;
        this.substanceService = substanceService;
        this.names = [];
        this.references = [];
        //recordUUID: string = "00006eea-e2d2-4d79-99ff-30f17b3dd740";
        this.recordUUID = "";
        this.nName = new __WEBPACK_IMPORTED_MODULE_1__name__["a" /* Name */]();
        this.nameValidation = [];
    }
    NameListComponent.prototype.getNameTypes = function () {
        var _this = this;
        this.cvService
            .getList("NAME_TYPE")
            .then(function (nameTypes) { return _this.nameTypes = nameTypes; });
    };
    NameListComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getList("ACCESS_GROUP")
            .then(function (access) { return _this.access = access; });
    };
    NameListComponent.prototype.getReferences = function () {
        var _this = this;
        this.substanceService
            .getReferences()
            .then(function (refs) {
            _this.references = refs;
        });
    };
    NameListComponent.prototype.getNames = function () {
        var _this = this;
        // console.log(this.recordUUID);
        //obj = document.getElementById('targetFrame').contentWindow.getJson();
        this.substanceService
            .getNames()
            .then(function (nams) {
            _this.names = nams;
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
            return __WEBPACK_IMPORTED_MODULE_11_lodash__["chain"](this.references)
                .filter(function (r) { return (data.references.indexOf(r.uuid) >= 0); })
                .map("index")
                .value();
        }
        else {
            return "";
        }
    };
    NameListComponent.prototype.refresh = function () {
        this.ngOnInit();
    };
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
        var dialogRef = this.dialog.open(__WEBPACK_IMPORTED_MODULE_6__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], { height: '550px', width: '400px' });
        //TODO: should have a cleaner initialization process
        dialogRef.componentInstance.allReferences = this.references;
        console.log(name);
        dialogRef.componentInstance.data = name;
        dialogRef.componentInstance.dataList = this.names;
        dialogRef.componentInstance.closeFunction = function () {
            dialogRef.close();
        };
    };
    NameListComponent.prototype.addName = function () {
        console.log("add");
        this.nName = new __WEBPACK_IMPORTED_MODULE_1__name__["a" /* Name */]();
        this.nName.type = 'cn';
        this.nName.languages = ['en'];
        //this.nName.name="enter....";
        this.nName.access = ['protected'];
        this.nName.references = [];
        this.nName.generateNewUuid();
        this.names.push(this.nName);
    };
    NameListComponent.prototype.validateNames = function (names) {
        for (var _i = 0, names_1 = names; _i < names_1.length; _i++) {
            var n = names_1[_i];
            if (!n.name) {
                this.nameValidation.push("Name is a required field");
            }
        }
    };
    NameListComponent.prototype.saveNameListChanges = function ($event, names) {
        //validate to make sure at least one name is marked as Preferred Term
        this.validateNames(names);
        console.log("save name list changes");
        console.log(names);
    };
    NameListComponent.prototype.findSelectedNameIndex = function (name) {
        return this.names.indexOf(name);
    };
    NameListComponent.prototype.deleteName = function (name) {
        console.log("delete:");
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
        this.utilService.changeAccess($event, name);
    };
    NameListComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'name-list',
            template: __webpack_require__(786),
            providers: [__WEBPACK_IMPORTED_MODULE_2__name_service__["a" /* NameService */], __WEBPACK_IMPORTED_MODULE_7_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_8__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_3__references_reference_list_service__["a" /* ReferenceListService */], __WEBPACK_IMPORTED_MODULE_12__substanceedit_substanceedit_service__["a" /* SubstanceService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__name_service__["a" /* NameService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__name_service__["a" /* NameService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_3__references_reference_list_service__["a" /* ReferenceListService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__references_reference_list_service__["a" /* ReferenceListService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__["a" /* CVService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_5__angular_material__["b" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__angular_material__["b" /* MdDialog */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_7_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_7_primeng_primeng__["ConfirmationService"]) === 'function' && _e) || Object, (typeof (_f = typeof __WEBPACK_IMPORTED_MODULE_8__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_8__utils_utils_service__["a" /* UtilService */]) === 'function' && _f) || Object, (typeof (_g = typeof __WEBPACK_IMPORTED_MODULE_10__angular_router__["ActivatedRoute"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_10__angular_router__["ActivatedRoute"]) === 'function' && _g) || Object, (typeof (_h = typeof __WEBPACK_IMPORTED_MODULE_10__angular_router__["Router"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_10__angular_router__["Router"]) === 'function' && _h) || Object, (typeof (_j = typeof __WEBPACK_IMPORTED_MODULE_12__substanceedit_substanceedit_service__["a" /* SubstanceService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_12__substanceedit_substanceedit_service__["a" /* SubstanceService */]) === 'function' && _j) || Object])
    ], NameListComponent);
    return NameListComponent;
    var _a, _b, _c, _d, _e, _f, _g, _h, _j;
}());
//# sourceMappingURL=name-list.component.js.map

/***/ }),

/***/ 438:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__name__ = __webpack_require__(169);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_lodash__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_2_lodash__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__data__ = __webpack_require__(37);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return NameService; });
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




var NameService = (function (_super) {
    __extends(NameService, _super);
    function NameService() {
        _super.apply(this, arguments);
        this.namesList = [];
    }
    NameService.prototype.getNames = function (uuid) {
        var me = this;
        //should do something to pass the UUID somewhere
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(uuid)
                .andThen(function (s) { return s.fetch("/names"); })
                .andThen(function (names) {
                return __WEBPACK_IMPORTED_MODULE_2_lodash__["map"](names, function (n) {
                    return (new __WEBPACK_IMPORTED_MODULE_1__name__["a" /* Name */]().merge(n));
                });
            })
                .get(function (names) { return resolve(names); });
        });
    };
    //TODO: probebly retire this
    NameService.prototype.getReferencesFromName = function (name) {
        var reflist = __WEBPACK_IMPORTED_MODULE_2_lodash__["map"](name.references, function (r) { return window["ReferenceFinder"].get(r); });
        var totalpromise = window["JPromise"].join(reflist);
        return new Promise(function (resolve, reject) {
            totalpromise.get(function (refs) { return resolve(refs); });
        });
    };
    //TODO: a service to create a new name
    NameService.prototype.saveName = function (name) {
        console.log("name save service");
    };
    NameService.prototype.deleteName = function (name) {
        console.log("delete name service");
        var loc = this.namesList.indexOf(name);
        if (loc > -1) {
            this.namesList.splice(loc, 1);
        }
        return this.namesList;
    };
    NameService.prototype.addName = function (name) {
        if (!name.uuid) {
            name.generateNewUuid();
        }
        this.namesList.push(name);
        return this;
    };
    NameService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], NameService);
    return NameService;
}(__WEBPACK_IMPORTED_MODULE_3__data__["a" /* Data */]));
//# sourceMappingURL=name.service.js.map

/***/ }),

/***/ 439:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__data__ = __webpack_require__(37);
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
}(__WEBPACK_IMPORTED_MODULE_0__data__["a" /* Data */]));
//# sourceMappingURL=notes.js.map

/***/ }),

/***/ 440:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(170);
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

/***/ 441:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__data__ = __webpack_require__(37);
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

/***/ 442:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__reference__ = __webpack_require__(110);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_primeng_primeng__ = __webpack_require__(59);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_2_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__reference_list_service__ = __webpack_require__(109);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__substanceedit_substanceedit_service__ = __webpack_require__(111);
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
    function ReferenceListComponent(referenceListService, confirmationService, cvService, substanceService) {
        this.referenceListService = referenceListService;
        this.confirmationService = confirmationService;
        this.cvService = cvService;
        this.substanceService = substanceService;
        this.refs = [];
        this.recordUUID = "00006eea-e2d2-4d79-99ff-30f17b3dd740";
        this.access = [];
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
    };
    ReferenceListComponent.prototype.addReference = function () {
        console.log("add");
        this.newRef = true;
        this.nRef = new __WEBPACK_IMPORTED_MODULE_1__reference__["a" /* Reference */]();
        this.nRef.generateNewUuid();
        this.nRef.setFlag("new", true);
        this.nRef.index = this.refs.length + 1;
        this.refs.push(this.nRef);
    };
    ReferenceListComponent.prototype.exportRefs = function () {
        console.log("export refs");
    };
    ReferenceListComponent.prototype.search = function () {
        console.log("search refs");
    };
    ReferenceListComponent.prototype.getReferences = function () {
        var _this = this;
        this.substanceService
            .getReferences()
            .then(function (refs) {
            _this.refs = refs;
        });
    };
    ReferenceListComponent.prototype.getAccess = function () {
        /*this.cvService
          .getAccess()
          .then(access => this.access = access);*/
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
    ReferenceListComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'reference-list',
            template: __webpack_require__(795),
            providers: [__WEBPACK_IMPORTED_MODULE_3__reference_list_service__["a" /* ReferenceListService */], __WEBPACK_IMPORTED_MODULE_2_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_5__substanceedit_substanceedit_service__["a" /* SubstanceService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_3__reference_list_service__["a" /* ReferenceListService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__reference_list_service__["a" /* ReferenceListService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2_primeng_primeng__["ConfirmationService"]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__["a" /* CVService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_5__substanceedit_substanceedit_service__["a" /* SubstanceService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__substanceedit_substanceedit_service__["a" /* SubstanceService */]) === 'function' && _d) || Object])
    ], ReferenceListComponent);
    return ReferenceListComponent;
    var _a, _b, _c, _d;
}());
//# sourceMappingURL=reference-list.component.js.map

/***/ }),

/***/ 443:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(19);
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
    function ReferenceEditDialog(cvService) {
        this.cvService = cvService;
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
            .getAccess()
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
    ReferenceEditDialog = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'reference-edit-dialog',
            template: __webpack_require__(792),
            //styleUrls: ['../styles.css']
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object])
    ], ReferenceEditDialog);
    return ReferenceEditDialog;
    var _a;
}());
//# sourceMappingURL=reference.component.js.map

/***/ }),

/***/ 444:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__data__ = __webpack_require__(37);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return RelationshipService; });
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


var RelationshipService = (function (_super) {
    __extends(RelationshipService, _super);
    function RelationshipService() {
        _super.apply(this, arguments);
        this.relationList = [];
    }
    /* getRelationships(uuid:string):Promise<Relationship[]> {
       let me = this;
   
       //should do something to pass the UUID somewhere
       return new Promise((resolve, reject) => {
         window["SubstanceFinder"].get(uuid)
           .andThen(s=>s.fetch("/relationships"))
           .andThen(relations=> {
             return _.map(relations, r=> {
               return (new Relationship().merge(r));
             });
           })
           .get(relations=>resolve(relations));
       });
     }*/
    RelationshipService.prototype.saveRelationship = function (relation) {
        console.log("relation save service");
    };
    RelationshipService.prototype.deleteRelationship = function (relation) {
        console.log("delete relation service");
        var loc = this.relationList.indexOf(relation);
        if (loc > -1) {
            this.relationList.splice(loc, 1);
        }
        return this.relationList;
    };
    RelationshipService.prototype.addRelationship = function (relation) {
        if (!relation.uuid) {
            relation.generateNewUuid();
        }
        this.relationList.push(relation);
        return this;
    };
    RelationshipService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], RelationshipService);
    return RelationshipService;
}(__WEBPACK_IMPORTED_MODULE_1__data__["a" /* Data */]));
//# sourceMappingURL=relationship.service.js.map

/***/ }),

/***/ 445:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__references_referencedData__ = __webpack_require__(170);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Relationship; });
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var Relationship = (function (_super) {
    __extends(Relationship, _super);
    function Relationship(relationService) {
        _super.call(this);
        this.relationService = relationService;
    }
    return Relationship;
}(__WEBPACK_IMPORTED_MODULE_0__references_referencedData__["a" /* ReferencedData */]));
//# sourceMappingURL=relationship.js.map

/***/ }),

/***/ 446:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__substanceedit_service__ = __webpack_require__(111);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_router__ = __webpack_require__(18);
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
    function SubstanceeditComponent(cvService, substanceService, route) {
        this.cvService = cvService;
        this.substanceService = substanceService;
        this.route = route;
    }
    SubstanceeditComponent.prototype.getTabs = function () {
        var _this = this;
        this.cvService.getTabs().then(function (tabs) {
            _this.tabs = tabs;
        });
    };
    SubstanceeditComponent.prototype.ngOnInit = function () {
        this.uuid = this.route.snapshot.params['id'];
        console.log("substance edit");
        console.log(this.uuid);
        this.getTabs();
        this.substanceService.setUuid(this.uuid);
    };
    SubstanceeditComponent.prototype.save = function () {
        this.substanceService.getFullJson()
            .then(function (s) {
            console.log(s);
            window["GGlob"]
                .SubstanceBuilder
                .fromSimple(s)
                .save()
                .get(function (r) {
                console.log(r);
            });
        });
    };
    SubstanceeditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'substanceedit',
            template: __webpack_require__(798),
            styles: [__webpack_require__(716)],
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_2__substanceedit_service__["a" /* SubstanceService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__substanceedit_service__["a" /* SubstanceService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__substanceedit_service__["a" /* SubstanceService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__angular_router__["ActivatedRoute"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__angular_router__["ActivatedRoute"]) === 'function' && _c) || Object])
    ], SubstanceeditComponent);
    return SubstanceeditComponent;
    var _a, _b, _c;
}());
//# sourceMappingURL=substanceedit.component.js.map

/***/ }),

/***/ 447:
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
            template: __webpack_require__(799),
            styles: [__webpack_require__(717)]
        }), 
        __metadata('design:paramtypes', [])
    ], SubstancelistComponent);
    return SubstancelistComponent;
}());
//# sourceMappingURL=substancelist.component.js.map

/***/ }),

/***/ 448:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap__ = __webpack_require__(179);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_router__ = __webpack_require__(18);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_platform_browser__ = __webpack_require__(28);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__ = __webpack_require__(111);
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
    function SummaryComponent(route, router, sanitizer, substanceService) {
        this.route = route;
        this.router = router;
        this.sanitizer = sanitizer;
        this.substanceService = substanceService;
        this.recordUUID = "";
        this.icreate = 0;
        this.iframe = null;
        this.loaded = false;
        console.log("Making a new Component");
        SummaryComponent.creation++;
        this.icreate = SummaryComponent.creation;
    }
    SummaryComponent.prototype.refresh = function () {
        var me = this;
        this.substanceService.getFullJson()
            .then(function (s) {
            console.log("Setting JSON in leg forms");
            me.setLegacyJson(JSON.parse(JSON.stringify(s)));
        });
        document.getElementById("hidden-things").style.display = "";
    };
    SummaryComponent.prototype.detach = function () {
        var me = this;
        console.log("Getting JSON from leg forms");
        this.substanceService.setFullJson(JSON.parse(JSON.stringify(me.getLegacyJson())));
        document.getElementById("hidden-things").style.display = "none";
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
        if (!this.loaded) {
            //document.getElementById("hidden-things").appendChild(this.iframe);
            this.recordUUID = this.route.parent.snapshot.params['id'];
            console.log(this.recordUUID);
            this.urlStr = "http://localhost:9000/ginas/app/substance/" + this.recordUUID + "/edit";
            console.log(this.urlStr);
            //this.url = this.sanitizer.bypassSecurityTrustResourceUrl(this.urlStr);
            this.iframe.src = this.urlStr;
            this.loaded = true;
        }
        else {
            this.onLoad();
        }
    };
    SummaryComponent.prototype.getLegacyJson = function () {
        return this.iframe["contentWindow"].getJson();
    };
    SummaryComponent.prototype.setLegacyJson = function (json) {
        return this.iframe["contentWindow"].setJson(json);
    };
    SummaryComponent.prototype.onLoad = function () {
        var _this = this;
        this.substanceService.getFullJson()
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
            template: __webpack_require__(800),
            providers: [__WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__angular_router__["ActivatedRoute"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__angular_router__["ActivatedRoute"]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__angular_router__["Router"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__angular_router__["Router"]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__angular_platform_browser__["DomSanitizer"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__angular_platform_browser__["DomSanitizer"]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__substanceedit_substanceedit_service__["a" /* SubstanceService */]) === 'function' && _d) || Object])
    ], SummaryComponent);
    return SummaryComponent;
    var _a, _b, _c, _d;
}());
//# sourceMappingURL=summary.component.js.map

/***/ }),

/***/ 449:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_router__ = __webpack_require__(18);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return SubstanceDetailResolver; });
/**
 * Created by sheilstk on 1/26/17.
 */
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};


var SubstanceDetailResolver = (function () {
    function SubstanceDetailResolver(router) {
        this.router = router;
    }
    SubstanceDetailResolver.prototype.resolve = function (route) {
        var id = route.params['id'];
        console.log(id);
        /* return this.ss.getSubstanceByID(id).map(substance => {
           if (substance) {
             return substance;
           } else { // id not found
             this.router.navigate(['/substances']);
             return null;
           }
         }).first();*/
        return;
    };
    SubstanceDetailResolver = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__angular_router__["Router"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__angular_router__["Router"]) === 'function' && _a) || Object])
    ], SubstanceDetailResolver);
    return SubstanceDetailResolver;
    var _a;
}());
//# sourceMappingURL=substance-detail-resolver.service.js.map

/***/ }),

/***/ 484:
/***/ (function(module, exports) {

function webpackEmptyContext(req) {
	throw new Error("Cannot find module '" + req + "'.");
}
webpackEmptyContext.keys = function() { return []; };
webpackEmptyContext.resolve = webpackEmptyContext;
module.exports = webpackEmptyContext;
webpackEmptyContext.id = 484;


/***/ }),

/***/ 485:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_platform_browser_dynamic__ = __webpack_require__(610);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__app_app_module__ = __webpack_require__(642);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__environments_environment__ = __webpack_require__(657);




if (__WEBPACK_IMPORTED_MODULE_3__environments_environment__["a" /* environment */].production) {
    __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["enableProdMode"])();
}
__webpack_require__.i(__WEBPACK_IMPORTED_MODULE_1__angular_platform_browser_dynamic__["a" /* platformBrowserDynamic */])().bootstrapModule(__WEBPACK_IMPORTED_MODULE_2__app_app_module__["a" /* AppModule */]);
//# sourceMappingURL=main.js.map

/***/ }),

/***/ 56:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_lodash__ = __webpack_require__(23);
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
    }
    UtilService.prototype.changeAccess = function ($event, mod) {
        if ($event.value.length == 0) {
            mod.access = ["public"];
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
        return "amt amt";
        //return amt.type + " " + amt.units + " " +  amt.average + "[" + amt.low + " to " + amt.high + "] (average)" + "[" + amt.lowLimit + " to " + amt.highLimit + "] (limits) " + amt.nonNumericValue ;
    };
    UtilService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], UtilService);
    return UtilService;
}());
//# sourceMappingURL=utils.service.js.map

/***/ }),

/***/ 640:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_router__ = __webpack_require__(18);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__names_name_list_component__ = __webpack_require__(437);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__codes_code_list_component__ = __webpack_require__(436);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__summaryview_summary_component__ = __webpack_require__(448);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__references_reference_list_component__ = __webpack_require__(442);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__utils_substance_detail_resolver_service__ = __webpack_require__(449);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7__substancelist_substancelist_component__ = __webpack_require__(447);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__substanceedit_substanceedit_component__ = __webpack_require__(446);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9__route_reuse_strategy__ = __webpack_require__(656);
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
            { path: 'references', component: __WEBPACK_IMPORTED_MODULE_5__references_reference_list_component__["a" /* ReferenceListComponent */] },
            { path: 'codes', component: __WEBPACK_IMPORTED_MODULE_3__codes_code_list_component__["a" /* CodeListComponent */] },
            /* { path: 'relationships', component: RelationshipListComponent},
             { path: 'notes', component: NotesComponent },
             { path: 'properties', component: PropertiesListComponent },*/
            { path: 'summary', component: __WEBPACK_IMPORTED_MODULE_4__summaryview_summary_component__["a" /* SummaryComponent */] }
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
            providers: [__WEBPACK_IMPORTED_MODULE_6__utils_substance_detail_resolver_service__["a" /* SubstanceDetailResolver */],
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

/***/ 641:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap__ = __webpack_require__(179);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_rxjs_add_operator_switchMap__);
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
    function AppComponent() {
    }
    AppComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'gsrs-app',
            template: "\n<h1 class=\"title\">G-SRS</h1>\n <nav>\n      <a routerLink=\"/browse\" routerLinkActive=\"active\">Browser</a>\n      <a routerLink=\"/search\" routerLinkActive=\"active\">Search</a>\n      <a routerLink=\"/admin\" routerLinkActive=\"active\">Admin</a>\n      <a routerLink=\"/login\" routerLinkActive=\"active\">Login</a>\n </nav>\n        <router-outlet></router-outlet>\n  "
        }), 
        __metadata('design:paramtypes', [])
    ], AppComponent);
    return AppComponent;
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

/***/ 642:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_platform_browser__ = __webpack_require__(28);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_forms__ = __webpack_require__(7);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_http__ = __webpack_require__(224);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__angular_material__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__app_routing_module__ = __webpack_require__(640);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__app_component__ = __webpack_require__(641);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_hammerjs__ = __webpack_require__(719);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_hammerjs___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_7_hammerjs__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__references_reference_list_dialog_component__ = __webpack_require__(108);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9__references_reference_component__ = __webpack_require__(443);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_10__names_name_component__ = __webpack_require__(645);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_11__names_name_list_component__ = __webpack_require__(437);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_12__references_reference_list_component__ = __webpack_require__(442);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_13__codes_code_list_component__ = __webpack_require__(436);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__ = __webpack_require__(59);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_14_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_14_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_15__relationships_relationship_list_component__ = __webpack_require__(655);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_16__notes_notes_component__ = __webpack_require__(648);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_17__notes_notes_edit_component__ = __webpack_require__(647);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_18__properties_properties_list_component__ = __webpack_require__(651);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_19__codes_code_edit_component__ = __webpack_require__(643);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_20__references_reference_edit_component__ = __webpack_require__(653);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_21__relationships_relationship_edit_component__ = __webpack_require__(654);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_22__amount_amount_component__ = __webpack_require__(434);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_23__properties_property_edit_component__ = __webpack_require__(652);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_24__parameters_parameter_component__ = __webpack_require__(649);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_25__summaryview_summary_component__ = __webpack_require__(448);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_26__utils_substance_detail_resolver_service__ = __webpack_require__(449);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_27__substancelist_substancelist_component__ = __webpack_require__(447);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_28__substanceedit_substanceedit_component__ = __webpack_require__(446);
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
                __WEBPACK_IMPORTED_MODULE_18__properties_properties_list_component__["a" /* PropertiesListComponent */], __WEBPACK_IMPORTED_MODULE_23__properties_property_edit_component__["a" /* PropertyEditComponent */], __WEBPACK_IMPORTED_MODULE_24__parameters_parameter_component__["a" /* ParameterComponent */], __WEBPACK_IMPORTED_MODULE_25__summaryview_summary_component__["a" /* SummaryComponent */], __WEBPACK_IMPORTED_MODULE_27__substancelist_substancelist_component__["a" /* SubstancelistComponent */], __WEBPACK_IMPORTED_MODULE_28__substanceedit_substanceedit_component__["a" /* SubstanceeditComponent */]
            ],
            imports: [
                __WEBPACK_IMPORTED_MODULE_0__angular_platform_browser__["BrowserModule"], __WEBPACK_IMPORTED_MODULE_2__angular_forms__["FormsModule"], __WEBPACK_IMPORTED_MODULE_3__angular_http__["a" /* HttpModule */], __WEBPACK_IMPORTED_MODULE_4__angular_material__["a" /* MaterialModule */], __WEBPACK_IMPORTED_MODULE_2__angular_forms__["ReactiveFormsModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["ToggleButtonModule"],
                __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["MultiSelectModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["OverlayPanelModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["DataTableModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["SharedModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["TabViewModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["RadioButtonModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["CheckboxModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["DropdownModule"], __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["DataGridModule"],
                __WEBPACK_IMPORTED_MODULE_14_primeng_primeng__["ConfirmDialogModule"], __WEBPACK_IMPORTED_MODULE_5__app_routing_module__["a" /* AppRoutingModule */]],
            providers: [__WEBPACK_IMPORTED_MODULE_26__utils_substance_detail_resolver_service__["a" /* SubstanceDetailResolver */]],
            bootstrap: [__WEBPACK_IMPORTED_MODULE_6__app_component__["a" /* AppComponent */]],
            entryComponents: [__WEBPACK_IMPORTED_MODULE_8__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], __WEBPACK_IMPORTED_MODULE_9__references_reference_component__["a" /* ReferenceEditDialog */]]
        }), 
        __metadata('design:paramtypes', [])
    ], AppModule);
    return AppModule;
}());
//# sourceMappingURL=app.module.js.map

/***/ }),

/***/ 643:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__code__ = __webpack_require__(168);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__code_service__ = __webpack_require__(644);
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
    function CodeEditComponent(cvService, codeService) {
        this.cvService = cvService;
        this.codeService = codeService;
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
            .then(function (codeSystem) { return _this.codeSystem = codeSystem; });
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
    CodeEditComponent.prototype.saveCode = function (code) {
        console.log(code);
        console.log("save");
        this.codeService.saveCode(code);
    };
    CodeEditComponent.prototype.deleteCode = function (code) {
        console.log("delete");
        this.codeService.deleteCode(code);
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__code__["a" /* Code */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__code__["a" /* Code */]) === 'function' && _a) || Object)
    ], CodeEditComponent.prototype, "code", void 0);
    CodeEditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'code-edit',
            template: __webpack_require__(783),
            providers: [__WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_3__code_service__["a" /* CodeService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__code_service__["a" /* CodeService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__code_service__["a" /* CodeService */]) === 'function' && _c) || Object])
    ], CodeEditComponent);
    return CodeEditComponent;
    var _a, _b, _c;
}());
//# sourceMappingURL=code-edit.component.js.map

/***/ }),

/***/ 644:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__code__ = __webpack_require__(168);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_lodash__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_2_lodash__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__data__ = __webpack_require__(37);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return CodeService; });
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




var CodeService = (function (_super) {
    __extends(CodeService, _super);
    function CodeService() {
        _super.apply(this, arguments);
        this.codeList = [];
    }
    CodeService.prototype.getCodes = function (uuid) {
        var me = this;
        //should do something to pass the UUID somewhere
        return new Promise(function (resolve, reject) {
            window["SubstanceFinder"].get(uuid)
                .andThen(function (s) { return s.fetch("/codes"); })
                .andThen(function (codes) {
                return __WEBPACK_IMPORTED_MODULE_2_lodash__["map"](codes, function (n) {
                    return (new __WEBPACK_IMPORTED_MODULE_1__code__["a" /* Code */]().merge(n));
                });
            })
                .get(function (codes) { return resolve(codes); });
        });
    };
    //TODO: a service to create a new name
    CodeService.prototype.saveCode = function (code) {
        console.log("name save service");
    };
    CodeService.prototype.deleteCode = function (code) {
        console.log("delete name service");
        var loc = this.codeList.indexOf(code);
        if (loc > -1) {
            this.codeList.splice(loc, 1);
        }
        return this.codeList;
    };
    CodeService.prototype.addCode = function (code) {
        if (!code.uuid) {
            code.generateNewUuid();
        }
        this.codeList.push(code);
        return this;
    };
    CodeService = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Injectable"])(), 
        __metadata('design:paramtypes', [])
    ], CodeService);
    return CodeService;
}(__WEBPACK_IMPORTED_MODULE_3__data__["a" /* Data */]));
//# sourceMappingURL=code.service.js.map

/***/ }),

/***/ 645:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__name__ = __webpack_require__(169);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__name_service__ = __webpack_require__(438);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__ = __webpack_require__(59);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_4_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__ = __webpack_require__(56);
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
    function NameEditComponent(cvService, nameService, utilService) {
        this.cvService = cvService;
        this.nameService = nameService;
        this.utilService = utilService;
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
            .then(function (access) { return _this.access = access; });
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
    NameEditComponent.prototype.ngOnInit = function () {
        this.getNameTypes();
        this.getAccess();
        this.getDomains();
        this.getLanguages();
        this.getNameOrgs();
        this.getNameJurisdiction();
    };
    NameEditComponent.prototype.changeDisplay = function (name, $event) {
        console.log("change");
        console.log($event);
        name.displayName = $event.checked;
        if ($event.checked) {
            name.preferred = true;
        }
        //TODO: iterate over all names and make sure atleast one name has displayName flag set to true
    };
    NameEditComponent.prototype.saveName = function (name) {
        console.log(name);
        console.log("save");
        this.nameService.saveName(name);
    };
    NameEditComponent.prototype.deleteName = function (name) {
        console.log("delete");
        this.nameService.deleteName(name);
    };
    NameEditComponent.prototype.changeAccess = function ($event, name) {
        this.utilService.changeAccess($event, name);
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__name__["a" /* Name */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__name__["a" /* Name */]) === 'function' && _a) || Object)
    ], NameEditComponent.prototype, "name", void 0);
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["ViewChild"])('multiselect'), 
        __metadata('design:type', (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__["MultiSelectModule"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__["MultiSelectModule"]) === 'function' && _b) || Object)
    ], NameEditComponent.prototype, "multi", void 0);
    NameEditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'name-edit',
            template: __webpack_require__(785),
            providers: [__WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_3__name_service__["a" /* NameService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_3__name_service__["a" /* NameService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__name_service__["a" /* NameService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__utils_utils_service__["a" /* UtilService */]) === 'function' && _e) || Object])
    ], NameEditComponent);
    return NameEditComponent;
    var _a, _b, _c, _d, _e;
}());
//# sourceMappingURL=name.component.js.map

/***/ }),

/***/ 646:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__data__ = __webpack_require__(37);
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

/***/ 647:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__notes__ = __webpack_require__(439);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__note_service__ = __webpack_require__(646);
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
        var _this = this;
        this.cvService
            .getAccess()
            .then(function (access) { return _this.access = access; });
        /* this.cvService
           .getList("ACCESS_GROUP")
           .then(access => this.access = access);*/
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
            template: __webpack_require__(787),
            providers: [__WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_3__note_service__["a" /* NoteService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__note_service__["a" /* NoteService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__note_service__["a" /* NoteService */]) === 'function' && _c) || Object])
    ], NotesEditComponent);
    return NotesEditComponent;
    var _a, _b, _c;
}());
//# sourceMappingURL=notes-edit.component.js.map

/***/ }),

/***/ 648:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__notes__ = __webpack_require__(439);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__ = __webpack_require__(56);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__ = __webpack_require__(59);
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
        this.recordUUID = "00006eea-e2d2-4d79-99ff-30f17b3dd740";
        this.notes = [];
    }
    NotesComponent.prototype.ngOnInit = function () {
        this.getAccess();
    };
    NotesComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getAccess()
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
            template: __webpack_require__(788),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__["ConfirmationService"]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4_primeng_primeng__["ConfirmationService"]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__utils_utils_service__["a" /* UtilService */]) === 'function' && _c) || Object])
    ], NotesComponent);
    return NotesComponent;
    var _a, _b, _c;
}());
//# sourceMappingURL=notes.component.js.map

/***/ }),

/***/ 649:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__parameter__ = __webpack_require__(650);
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
            template: __webpack_require__(789),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object])
    ], ParameterComponent);
    return ParameterComponent;
    var _a, _b;
}());
//# sourceMappingURL=parameter.component.js.map

/***/ }),

/***/ 650:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__data__ = __webpack_require__(37);
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
}(__WEBPACK_IMPORTED_MODULE_0__data__["a" /* Data */]));
//# sourceMappingURL=parameter.js.map

/***/ }),

/***/ 651:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__ = __webpack_require__(56);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_primeng_primeng__ = __webpack_require__(59);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__property_model__ = __webpack_require__(440);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__property_service__ = __webpack_require__(441);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__angular_material__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7__references_reference_list_dialog_component__ = __webpack_require__(108);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8_lodash__ = __webpack_require__(23);
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
        this.nProperty = new __WEBPACK_IMPORTED_MODULE_4__property_model__["a" /* Property */](this.propertyService);
        this.properties = [];
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
            template: __webpack_require__(790),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_3_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_5__property_service__["a" /* PropertyService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_3_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3_primeng_primeng__["ConfirmationService"]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__utils_utils_service__["a" /* UtilService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_5__property_service__["a" /* PropertyService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__property_service__["a" /* PropertyService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_6__angular_material__["b" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_6__angular_material__["b" /* MdDialog */]) === 'function' && _e) || Object])
    ], PropertiesListComponent);
    return PropertiesListComponent;
    var _a, _b, _c, _d, _e;
}());
//# sourceMappingURL=properties-list.component.js.map

/***/ }),

/***/ 652:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__property_service__ = __webpack_require__(441);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__property_model__ = __webpack_require__(440);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__ = __webpack_require__(56);
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
        this.amountDisplay = false;
        this.parameters = [];
    }
    PropertyEditComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getAccess()
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
            template: __webpack_require__(791),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_2__property_service__["a" /* PropertyService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_2__property_service__["a" /* PropertyService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__property_service__["a" /* PropertyService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__["a" /* UtilService */]) === 'function' && _d) || Object])
    ], PropertyEditComponent);
    return PropertyEditComponent;
    var _a, _b, _c, _d;
}());
//# sourceMappingURL=property-edit.component.js.map

/***/ }),

/***/ 653:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__reference__ = __webpack_require__(110);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__ = __webpack_require__(19);
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
    function ReferenceEdit(cvService) {
        this.cvService = cvService;
        this.close = function () { };
    }
    ReferenceEdit.prototype.updateRef = function (ref, event) {
        event.preventDefault();
        //TODO:should do something interesting here
        //right now, we are always modifying things, no matter what
        this.close();
    };
    ReferenceEdit.prototype.deleteRef = function (selectedReference) {
        selectedReference.setFlag("delete", true);
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
            .getAccess()
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
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_1__reference__["a" /* Reference */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__reference__["a" /* Reference */]) === 'function' && _a) || Object)
    ], ReferenceEdit.prototype, "ref", void 0);
    ReferenceEdit = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'reference-edit',
            template: __webpack_require__(793),
            //styleUrls: ['../styles.css']
            providers: [__WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__cv_cv_service__["a" /* CVService */]) === 'function' && _b) || Object])
    ], ReferenceEdit);
    return ReferenceEdit;
    var _a, _b;
}());
//# sourceMappingURL=reference-edit-component.js.map

/***/ }),

/***/ 654:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__relationship_service__ = __webpack_require__(444);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__relationship__ = __webpack_require__(445);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__ = __webpack_require__(56);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__amount_amount_model__ = __webpack_require__(435);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__amount_amount_component__ = __webpack_require__(434);
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
    function RelationshipEditComponent(cvService, relationshipService, utilService) {
        this.cvService = cvService;
        this.relationshipService = relationshipService;
        this.utilService = utilService;
        this.amountDisplay = false;
        this.amount = new __WEBPACK_IMPORTED_MODULE_5__amount_amount_model__["a" /* Amount */]();
    }
    RelationshipEditComponent.prototype.getAccess = function () {
        var _this = this;
        this.cvService
            .getAccess()
            .then(function (access) { return _this.access = access; });
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
        this.getAccess();
        this.getRelationshipTypes();
        this.getQualification();
        this.getIntercationType();
    };
    RelationshipEditComponent.prototype.saveRelationship = function (relation) {
        console.log(relation);
        console.log("save");
        this.relationshipService.saveRelationship(relation);
    };
    RelationshipEditComponent.prototype.deleteRelationship = function (relation) {
        console.log("delete");
        this.relationshipService.deleteRelationship(relation);
    };
    RelationshipEditComponent.prototype.changeAccess = function ($event, relation) {
        this.utilService.changeAccess($event, relation);
    };
    RelationshipEditComponent.prototype.showAmounts = function (amount) {
        return this.utilService.displayAmount(amount);
    };
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Input"])(), 
        __metadata('design:type', (typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_3__relationship__["a" /* Relationship */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__relationship__["a" /* Relationship */]) === 'function' && _a) || Object)
    ], RelationshipEditComponent.prototype, "relationship", void 0);
    __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["ViewChild"])(__WEBPACK_IMPORTED_MODULE_6__amount_amount_component__["a" /* AmountComponent */]), 
        __metadata('design:type', (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_5__amount_amount_model__["a" /* Amount */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__amount_amount_model__["a" /* Amount */]) === 'function' && _b) || Object)
    ], RelationshipEditComponent.prototype, "amount", void 0);
    RelationshipEditComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'relation-edit',
            template: __webpack_require__(796),
            providers: [__WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */], __WEBPACK_IMPORTED_MODULE_2__relationship_service__["a" /* RelationshipService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_1__cv_cv_service__["a" /* CVService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_2__relationship_service__["a" /* RelationshipService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__relationship_service__["a" /* RelationshipService */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__utils_utils_service__["a" /* UtilService */]) === 'function' && _e) || Object])
    ], RelationshipEditComponent);
    return RelationshipEditComponent;
    var _a, _b, _c, _d, _e;
}());
//# sourceMappingURL=relationship-edit.component.js.map

/***/ }),

/***/ 655:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__relationship__ = __webpack_require__(445);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__relationship_service__ = __webpack_require__(444);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__references_reference_list_service__ = __webpack_require__(109);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__ = __webpack_require__(19);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__angular_material__ = __webpack_require__(83);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__references_reference_list_dialog_component__ = __webpack_require__(108);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_primeng_primeng__ = __webpack_require__(59);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7_primeng_primeng___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_7_primeng_primeng__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__utils_utils_service__ = __webpack_require__(56);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9_lodash__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9_lodash___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_9_lodash__);
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
    function RelationshipListComponent(relationService, referenceListService, cvService, dialog, confirmationService, utilService) {
        this.relationService = relationService;
        this.referenceListService = referenceListService;
        this.cvService = cvService;
        this.dialog = dialog;
        this.confirmationService = confirmationService;
        this.utilService = utilService;
        this.relations = [];
        this.references = [];
        this.newRelation = false;
        this.showRSStruct = false;
        this.nRelation = new __WEBPACK_IMPORTED_MODULE_1__relationship__["a" /* Relationship */](this.relationService);
        this.recordUUID = "00006eea-e2d2-4d79-99ff-30f17b3dd740";
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
    };
    RelationshipListComponent.prototype.getReferences = function () {
        var _this = this;
        this.referenceListService
            .getAllReferences(this.recordUUID)
            .then(function (refs) {
            _this.references = refs;
        });
    };
    RelationshipListComponent.prototype.showReferenceIndexes = function (data) {
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
    RelationshipListComponent.prototype.openRefListDialog = function (relation) {
        console.log("relationship list");
        var dialogRef = this.dialog.open(__WEBPACK_IMPORTED_MODULE_6__references_reference_list_dialog_component__["a" /* ReferenceListDialog */], { height: '550px', width: '400px' });
        //TODO: should have a cleaner initialization process
        dialogRef.componentInstance.allReferences = this.references;
        console.log(relation);
        dialogRef.componentInstance.data = relation;
        dialogRef.componentInstance.closeFunction = function () {
            dialogRef.close();
        };
    };
    RelationshipListComponent.prototype.saveRelationListChanges = function ($event, relations) {
        console.log("save relationship list changes");
        console.log(relations);
    };
    RelationshipListComponent.prototype.addRelationship = function () {
        this.newRelation = true;
        this.nRelation = new __WEBPACK_IMPORTED_MODULE_1__relationship__["a" /* Relationship */](this.relationService);
        this.nRelation.access = ['protected'];
        this.nRelation.references = [];
        this.relations.push(this.nRelation);
    };
    RelationshipListComponent.prototype.findSelectedRelationIndex = function (relation) {
        return this.relations.indexOf(relation);
    };
    RelationshipListComponent.prototype.deleteRelation = function (relation) {
        console.log("delete:");
        console.log(relation);
        this.relations.splice(this.findSelectedRelationIndex(relation), 1);
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
    RelationshipListComponent.prototype.changeAccess = function ($event, relation) {
        this.utilService.changeAccess($event, relation);
    };
    RelationshipListComponent.prototype.showStructure = function () {
        console.log("showStructure");
        this.showRSStruct = !this.showRSStruct;
        console.log(this.showRSStruct);
    };
    RelationshipListComponent.prototype.showAmounts = function (amount) {
        return this.utilService.displayAmount(amount);
    };
    RelationshipListComponent = __decorate([
        __webpack_require__.i(__WEBPACK_IMPORTED_MODULE_0__angular_core__["Component"])({
            selector: 'relationship-list',
            template: __webpack_require__(797),
            providers: [__WEBPACK_IMPORTED_MODULE_7_primeng_primeng__["ConfirmationService"], __WEBPACK_IMPORTED_MODULE_2__relationship_service__["a" /* RelationshipService */], __WEBPACK_IMPORTED_MODULE_3__references_reference_list_service__["a" /* ReferenceListService */], __WEBPACK_IMPORTED_MODULE_8__utils_utils_service__["a" /* UtilService */], __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__["a" /* CVService */]]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof __WEBPACK_IMPORTED_MODULE_2__relationship_service__["a" /* RelationshipService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_2__relationship_service__["a" /* RelationshipService */]) === 'function' && _a) || Object, (typeof (_b = typeof __WEBPACK_IMPORTED_MODULE_3__references_reference_list_service__["a" /* ReferenceListService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_3__references_reference_list_service__["a" /* ReferenceListService */]) === 'function' && _b) || Object, (typeof (_c = typeof __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__["a" /* CVService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_4__cv_cv_service__["a" /* CVService */]) === 'function' && _c) || Object, (typeof (_d = typeof __WEBPACK_IMPORTED_MODULE_5__angular_material__["b" /* MdDialog */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_5__angular_material__["b" /* MdDialog */]) === 'function' && _d) || Object, (typeof (_e = typeof __WEBPACK_IMPORTED_MODULE_7_primeng_primeng__["ConfirmationService"] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_7_primeng_primeng__["ConfirmationService"]) === 'function' && _e) || Object, (typeof (_f = typeof __WEBPACK_IMPORTED_MODULE_8__utils_utils_service__["a" /* UtilService */] !== 'undefined' && __WEBPACK_IMPORTED_MODULE_8__utils_utils_service__["a" /* UtilService */]) === 'function' && _f) || Object])
    ], RelationshipListComponent);
    return RelationshipListComponent;
    var _a, _b, _c, _d, _e, _f;
}());
//# sourceMappingURL=relationship-list.component.js.map

/***/ }),

/***/ 656:
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
        return !!route.routeConfig && !!this.handlers[route.routeConfig.path];
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

/***/ 657:
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

/***/ 716:
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(44)();
// imports


// module
exports.push([module.i, "", ""]);

// exports


/*** EXPORTS FROM exports-loader ***/
module.exports = module.exports.toString();

/***/ }),

/***/ 717:
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(44)();
// imports


// module
exports.push([module.i, "", ""]);

// exports


/*** EXPORTS FROM exports-loader ***/
module.exports = module.exports.toString();

/***/ }),

/***/ 782:
/***/ (function(module, exports) {

module.exports = "<br/>\n<div>\n<form class=\"amount\" #amountEditForm=\"ngForm\">\n\n   <!--<div>\n      <md-select placeholder=\"Amount Type\" name=\"amountType\" [(ngModel)]=\"amount.type\" [style.width]=\"'80%'\" >\n        <md-option *ngFor=\"let at of amountType\" [value]=\"at.value\">{{ at.label }}</md-option>\n      </md-select></div>\n\n   <div>\n      <input mdInput name=\"average\" value=\"{{amount.average}}\" [(ngModel)]=\"amount.average\" placeholder=\"Average\">\n    </div>\n\n   <div>\n      <input mdInput name=\"low\" value=\"{{amount.low}}\" [(ngModel)]=\"amount.low\" placeholder=\"Low\">\n      <input mdInput name=\"high\" value=\"{{amount.high}}\" [(ngModel)]=\"amount.high\" placeholder=\"High\">\n    </div>\n\n    <div>\n      <input mdInput name=\"lowLimit\" value=\"{{amount.lowLimit}}\" [(ngModel)]=\"amount.lowLimit\" placeholder=\"Low Limit\">\n      <input mdInput name=\"highLimit\" value=\"{{amount.highLimit}}\" [(ngModel)]=\"amount.highLimit\" placeholder=\"High Limit\">\n   </div>\n\n    <div>\n      <md-select placeholder=\"Amount Units\" name=\"amountUnits\" [(ngModel)]=\"amount.units\" [style.width]=\"'80%'\" >\n        <md-option *ngFor=\"let au of amountUnits\" [value]=\"au.value\">{{ au.label }}</md-option>\n      </md-select>\n    </div>>\n\n    <div>\n      <input mdInput name=\"nonNumericValue\" value=\"{{amount.nonNumericValue}}\" [(ngModel)]=\"amount.nonNumericValue\" placeholder=\"Non Numeric Value\">\n</div>-->\n\n   <div>\n     <md-select placeholder=\"Amount Type\" name=\"amountType\" [style.width]=\"'18%'\">\n       <md-option *ngFor=\"let at of amountType\" [value]=\"at.value\">{{ at.label }}</md-option>\n     </md-select>\n\n      <input mdInput name=\"average\" placeholder=\"Average\" [style.width]=\"'10%'\">\n\n      <input mdInput name=\"low\" placeholder=\"Low\" [style.width]=\"'10%'\">\n\n      <input mdInput name=\"high\" placeholder=\"High\" [style.width]=\"'10%'\">\n\n     <input mdInput name=\"lowLimit\"  placeholder=\"Low Limit\" [style.width]=\"'10%'\">\n     <input mdInput name=\"highLimit\" placeholder=\"High Limit\" [style.width]=\"'10%'\">\n\n     <md-select placeholder=\"Amount Units\" name=\"amountUnits\" [style.width]=\"'8%'\">\n       <md-option *ngFor=\"let au of amountUnits\" [value]=\"au.value\">{{ au.label }}</md-option>\n     </md-select>\n\n      <input mdInput name=\"nonNumericValue\" placeholder=\"Non Numeric Value\" [style.width]=\"'10%'\">\n\n     <button type=\"button\" pButton icon=\"fa-check\" (click)=\"saveAmount(this)\" label=\"Ok\"></button>\n\n     <button type=\"button\" pButton icon=\"fa-times\" (click)=\"clearAmount(this)\" label=\"Clear\"></button>\n  </div>\n</form></div>\n<br/>\n"

/***/ }),

/***/ 783:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"code-edit-form\" #codeEditForm=\"ngForm\">\n  <md-grid-list cols=\"10\" gutterSize=\"2px\" rowHeight=\"70px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Code System\" name=\"codeSystem\" [(ngModel)]=\"code.codeSystem\" [style.width]=\"'80%'\" >\n        <md-option *ngFor=\"let cs of codeSystem\" [value]=\"cs.value\">{{ cs.label }}</md-option>\n      </md-select></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=3 [rowspan]=1>\n    <md-select placeholder=\"Code System Type\" name=\"type\" [(ngModel)]=\"code.codeSystemType\" [style.width]=\"'80%'\" >\n      <md-option *ngFor=\"let type of codeSystemType\" [value]=\"type.value\">{{ type.label }}</md-option>\n    </md-select></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"code.access\"\n                     defaultLabel=\"Access\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\">\n      </p-multiSelect>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Code</b></md-grid-tile>\n    <md-grid-tile [colspan]=3 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"code\" value=\"{{code.code}}\" [(ngModel)]=\"code.code\">\n      </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Code URL</b></md-grid-tile>\n    <md-grid-tile [colspan]=4 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"url\" value=\"{{code.url}}\" [(ngModel)]=\"code.url\">\n      </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Comments</b></md-grid-tile>\n    <md-grid-tile [colspan]=8 [rowspan]=1>\n    <md-input-container class=\"ref-full-width\">\n      <textarea mdInput name=\"comments\" value=\"{{code.comments}}\" [(ngModel)]=\"code.comments\"></textarea>\n    </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Code Text</b></md-grid-tile>\n    <md-grid-tile [colspan]=8 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <textarea mdInput name=\"codeText\" value=\"{{code.codeText}}\" [(ngModel)]=\"code.codeText\"></textarea>\n      </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=10 [rowspan]=1 *ngIf=\"code.uuid\">\n      Created by <code>&nbsp;{{code.createdBy}}&nbsp;</code> on <code>&nbsp; {{code.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{code.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{code.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=7 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"deleteCode(code)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"saveCode(code)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 784:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"codes\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\">\n  <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n     <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addCode()\" label=\"Add Code\"></button>\n      <!--   <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"codes.length > 0\" (click)=\"exportCodes()\" label=\"Export Codes\"></button>\n        <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"codes.length > 0\" (click)=\"search()\" label=\"Search\"></button>-->\n    </div>\n  </p-header>\n\n  <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n    <template pTemplate=\"header\">Edit</template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'80px'}\">\n    <template pTemplate=\"header\">Delete</template>\n    <template pTemplate=\"body\" let-code=\"rowData\">\n      <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteCode(this)\"></button>\n    </template>\n  </p-column>\n\n  <p-column field=\"codeSystem\" header=\"Code System\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-code=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"code[col.field]\" [options]=\"codeSystem\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\" required=\"true\"></p-dropdown>\n    </template>\n  </p-column>\n\n  <p-column field=\"code\" header=\"Code\"  [style]=\"{'width':'300px'}\" [editable]=\"true\"></p-column>\n  <p-column field=\"url\" header=\"Code URL\"  [style]=\"{'width':'300px'}\" [editable]=\"true\"></p-column>\n  <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-code=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"code[col.field]\"\n                     defaultLabel=\"Access\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                     (onChange) =\"changeAccess($event, name)\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column>\n    <template pTemplate=\"header\">References</template>\n    <template let-code=\"rowData\"  pTemplate=\"body\">\n      <div *ngIf=\"!code.references || code.references.length == 0\" (click)=\"openRefListDialog(code)\">Click</div>\n      <div (click)=\"openRefListDialog(code)\" *ngIf=\"code.references\">\n        {{showReferenceIndexes(code)}}\n      </div>\n    </template>\n\n  </p-column>\n\n  <template let-code pTemplate=\"rowexpansion\">\n    <div>\n      <code-edit [code]=\"code\"></code-edit>\n    </div>\n  </template>\n\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n"

/***/ }),

/***/ 785:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"name-edit-form\" #nameEditForm=\"ngForm\">\n\n <md-grid-list cols=\"8\" gutterSize=\"2px\" rowHeight=\"70px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Name</b></md-grid-tile>\n    <md-grid-tile [colspan]=6 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"name\" value=\"{{name.name}}\" [(ngModel)]=\"name.name\" placeholder=\"name ...\" required>\n      </md-input-container>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n     <md-select placeholder=\"Name Type\" name=\"type\" [(ngModel)]=\"name.type\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let type of nameTypes\" [value]=\"type.value\" overlayVisible=\"true\">{{ type.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n   <md-grid-tile [colspan]=2 [rowspan]=1>\n     <!--<p-toggleButton [(ngModel)]=\"name.displayName\" name=\"DisplayName\" onLabel=\"Preferred Term\" onIcon=\"fa-check-square\"\n                     offLabel=\"Not Preferred\" offIcon=\"fa-minus-square\" (onChange)=\"changeDisplay(name, $event)\"\n                     [style]=\"{'width':'200px'}\"></p-toggleButton>-->\n     <md-checkbox [(ngModel)]=\"name.displayName\" [checked]=\"name.displayName\" name=\"DisplayName\" (onChange)=\"changeDisplay(name, $event)\">\n       Preferred Term - {{name.displayName}}\n     </md-checkbox>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-checkbox [(ngModel)]=\"name.preferred\" [checked]=\"name.preferred || name.displayName\" name=\"PreferredName\" [disabled]=\"name.displayName\">\n        Listing Term\n      </md-checkbox>\n    </md-grid-tile>\n   <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n\n   <md-grid-tile [colspan]=2 [rowspan]=1 *ngIf=\"name.type=='of'\" class=\"color-bg\">\n     <p-multiSelect [options]=\"nameJuris\" name=\"nameJurisdiction\" [(ngModel)]=\"name.nameJurisdiction\"\n                    defaultLabel=\"Name Jurisdiction\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n   </md-grid-tile>\n   <md-grid-tile [colspan]=2 [rowspan]=1 *ngIf=\"name.type=='of'\" class=\"color-bg\">\n     <p-multiSelect [options]=\"nameOrgs\" name=\"nameOrgs\" [(ngModel)]=\"name.nameOrgs\"\n                    defaultLabel=\"Name Orgs\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n   </md-grid-tile>\n   <md-grid-tile [colspan]=4 [rowspan]=1 *ngIf=\"name.type=='of'\" class=\"color-bg\"></md-grid-tile>\n\n              <md-grid-tile [colspan]=2 [rowspan]=1>\n                <p-multiSelect [options]=\"languages\" name=\"languages\"\n                               defaultLabel=\"Languages\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n              </md-grid-tile>\n              <md-grid-tile [colspan]=2 [rowspan]=1>\n                <p-multiSelect [options]=\"domains\" name=\"domains\" [(ngModel)]=\"name.domains\"\n                               defaultLabel=\"Domains\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n              </md-grid-tile>\n           <!--  <md-grid-tile [colspan]=2 [rowspan]=1>\n             <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"name.access\"\n                       defaultLabel=\"Access\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\" (onChange)=\"changeAccess($event, name)\"></p-multiSelect>\n             </md-grid-tile>-->\n            <!--     <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n              <md-grid-tile [colspan]=5 [rowspan]=1 (click)=\"openRefListDialog(name)\">\n                <h5>References</h5>\n                {{showReferenceIndexes(name)}}\n              </md-grid-tile>-->\n\n\n    <md-grid-tile [colspan]=8 [rowspan]=1 *ngIf=\"name.uuid\">\n      Created by <code>&nbsp;{{name.createdBy}}&nbsp;</code> on <code>&nbsp; {{name.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{name.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{name.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n  <md-grid-tile [colspan]=4 [rowspan]=1></md-grid-tile>\n  <md-grid-tile [colspan]=1 [rowspan]=1>\n    <button type=\"button\" (click)=\"deleteName(name)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n  </md-grid-tile>\n  <md-grid-tile [colspan]=1 [rowspan]=1>\n    <button type=\"button\" (click)=\"saveName(name)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n  </md-grid-tile>\n  <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 786:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"names\" expandableRows=\"true\" sizableColumns=\"true\"\n                 [responsive]=\"true\" [editable]=\"true\" id=\"nameListTable\">\n      <p-header>\n        <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n        <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addName()\" label=\"Add Name\"></button>\n    <!--    <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"names.length > 0\" (click)=\"exportNames()\" label=\"Export Names\"></button>\n        <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"names.length > 0\" (click)=\"search()\" label=\"Search\"></button> -->\n        </div>\n      </p-header>\n\n      <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n        <template pTemplate=\"header\">Edit</template>\n      </p-column>\n\n      <p-column [style]=\"{'width':'80px'}\">\n        <template pTemplate=\"header\">Delete</template>\n        <template pTemplate=\"body\" let-name=\"rowData\">\n          <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteName(name)\"></button>\n        </template>\n      </p-column>\n      <p-column [style]=\"{'width':'38px'}\">\n        <template pTemplate=\"header\">PT</template>\n        <template pTemplate=\"body\" let-name=\"rowData\">\n          <md-radio-button name=\"display\" [value]=\"name.displayName\" pTooltip=\"Preferred Term\"></md-radio-button>\n        </template>\n      </p-column>\n\n     <p-column [style]=\"{'width':'38px'}\">\n        <template pTemplate=\"header\">LT</template>\n        <template let-name=\"rowData\" pTemplate=\"body\">\n          <md-checkbox [(ngModel)]=\"name.preferred\" [checked]=\"name.displayName || name.preferred\" name=\"listing\"  pTooltip=\"Listing Term\"></md-checkbox>\n        </template>\n      </p-column>\n\n      <p-column field=\"name\" header=\"Name\"  [style]=\"{'width':'500px'}\" [editable]=\"true\"></p-column>\n      <p-column field=\"type\" header=\"Type\" [editable]=\"false\" [style]=\"{'overflow':'visible'}\">\n        <template let-col let-name=\"rowData\" pTemplate=\"body\">\n          <p-dropdown [(ngModel)]=\"name[col.field]\" [options]=\"nameTypes\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\" required=\"true\"><i class=\"fa fa-pencil-square-o\" aria-hidden=\"true\"></i></p-dropdown>\n        </template>\n      </p-column>\n\n      <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n        <template let-col let-name=\"rowData\" pTemplate=\"editor\">\n          <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"name[col.field]\"\n                         defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                         (onChange) =\"changeAccess($event, name)\"></p-multiSelect>\n        </template>\n      </p-column>\n\n      <p-column>\n        <template pTemplate=\"header\">References</template>\n        <template let-name=\"rowData\"  pTemplate=\"body\">\n          <div *ngIf=\"!name.references || name.references.length == 0\" (click)=\"openRefListDialog(name)\">Click</div>\n          <div (click)=\"openRefListDialog(name)\" *ngIf=\"name.references\">\n            {{showReferenceIndexes(name)}}\n          </div>\n        </template>\n\n      </p-column>\n      <template let-name pTemplate=\"rowexpansion\">\n        <div>\n          <name-edit [name]=name></name-edit>\n         </div>\n      </template>\n    </p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n\n\n<!--<div>\n  <pre>\n  {{ names | json }}\n    </pre>\n</div>-->\n"

/***/ }),

/***/ 787:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"note-edit-form\" #noteEditForm=\"ngForm\">\n  <md-grid-list cols=\"8\" gutterSize=\"2px\" rowHeight=\"125px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=6 [rowspan]=1>\n      <textarea rows=\"5\" cols=\"100\" pInputTextarea name=\"note\" [(ngModel)]=\"notes.note\"></textarea>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"notes.access\"\n                     defaultLabel=\"Access\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\">\n      </p-multiSelect>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=10 [rowspan]=1 *ngIf=\"notes.uuid\">\n      Created by <code>&nbsp;{{notes.createdBy}}&nbsp;</code> on <code>&nbsp; {{notes.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{notes.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{notes.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=3 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"deleteNote(notes)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"saveNote(notes)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 788:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"notes\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\">\n  <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addNotes()\" label=\"Add Notes\"></button>\n      <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"notes.length > 0\" (click)=\"exportNotes()\" label=\"Export Notes\"></button>\n      <!--  <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"notes.length > 0\" (click)=\"search()\" label=\"Search\"></button>-->\n    </div>\n  </p-header>\n\n  <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n    <template pTemplate=\"header\">Edit</template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'80px'}\">\n    <template pTemplate=\"header\">Delete</template>\n    <template pTemplate=\"body\" let-note=\"rowData\">\n      <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteNotes(this)\"></button>\n    </template>\n  </p-column>\n\n  <p-column field=\"note\" header=\"Notes\" [editable]=\"true\" [style]=\"{'width':'800px'}\"></p-column>\n\n  <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-note=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"note[col.field]\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                     (onChange) =\"changeAccess($event, name)\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'300px'}\">\n    <template pTemplate=\"header\">References</template>\n    <template let-note=\"rowData\"  pTemplate=\"body\">\n      <div *ngIf=\"!note.references || note.references.length == 0\" (click)=\"openRefListDialog(note)\">Click</div>\n      <div (click)=\"openRefListDialog(note)\" *ngIf=\"note.references\">\n        {{showReferenceIndexes(note)}}\n      </div>\n    </template>\n  </p-column>\n  <template let-note pTemplate=\"rowexpansion\">\n    <div>\n      <notes-edit [notes]=note></notes-edit>\n    </div>\n  </template>\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n<div><br/><button type=\"button\" pButton [style]=\"{display:block}\" (click)=\"saveNoteListChanges(event, notes)\" label=\"Save Changes\" *ngIf=\"notes.length > 0\"></button></div>\n"

/***/ }),

/***/ 789:
/***/ (function(module, exports) {

module.exports = "<br/>\n<div>\n  <form class=\"parameter\" #paramEditForm=\"ngForm\">\n   <div>\n     <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"deleteParam(this)\"></button>\n\n     <input mdInput name=\"Name\" placeholder=\"Name\" [style.width]=\"'10%'\">\n\n     <md-select placeholder=\"Parameter Type\" name=\"paramType\" [style.width]=\"'18%'\">\n        <md-option *ngFor=\"let pt of paramType\" [value]=\"pt.value\">{{ pt.label }}</md-option>\n      </md-select>\n\n     <span>Amount</span>\n\n      <button type=\"button\" pButton icon=\"fa-check\" (click)=\"saveParam(this)\" label=\"Ok\"></button>\n\n    </div>\n  </form></div>\n<br/>\n"

/***/ }),

/***/ 790:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"properties\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\" id=\"propertyListTable\" #dt>\n  <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addProperty()\" label=\"Add Property\"></button>\n      <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"properties.length > 0\" (click)=\"exportRelations()\" label=\"Export Propertys\"></button>\n      <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"properties.length > 0\" (click)=\"search()\" label=\"Search\"></button>\n    </div>\n  </p-header>\n  <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n    <template pTemplate=\"header\">Edit</template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'80px'}\">\n    <template pTemplate=\"header\">Delete</template>\n    <template pTemplate=\"body\" let-property=\"rowData\">\n      <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteProperty(this)\"></button>\n    </template>\n  </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\" [style]=\"{'width':'150px'}\">\n    <template pTemplate=\"header\">Referenced Substance <br/>\n      <md-checkbox [checked]=\"showRSStruct\" name=\"showRSStruct\" (change)=\"showStructure()\" *ngIf=\"properties.length > 0\">Show Structure</md-checkbox>\n    </template>\n    <template let-col let-property=\"rowData\" pTemplate=\"body\">\n      TEST TEST\n      <div *ngIf=\"showRSStruct\">\n        <img src=\"assets/images/protein.svg\" alt=\"structure\">\n      </div>\n    </template>\n  </p-column>\n\n  <p-column field=\"name\" header=\"Name\" [style]=\"{'overflow':'visible'}\" [style]=\"{'width':'300px'}\">\n    <template let-col let-property=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"property[col.field]\" [options]=\"propName\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\" required=\"true\" placeholder=\"name...\"></p-dropdown>\n    </template>\n  </p-column>\n\n  <p-column field=\"type\" header=\"Property Type\" [style]=\"{'overflow':'visible'}\" [style]=\"{'width':'150px'}\">\n    <template let-col let-property=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"property[col.field]\" [options]=\"propType\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\" required=\"true\" placeholder=\"type...\"></p-dropdown>\n    </template>\n  </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\">\n    <template pTemplate=\"header\">Amount</template>\n    <template let-col let-property=\"rowData\" pTemplate=\"body\">\n      {{showAmounts(property.value)}}\n    </template>\n  </p-column>\n\n <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-property=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"property[col.field]\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                     (onChange) =\"changeAccess($event, property)\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column>\n      <template pTemplate=\"header\">References</template>\n      <template let-property=\"rowData\"  pTemplate=\"body\">\n        <div *ngIf=\"!property.references || property.references.length == 0\" (click)=\"openRefListDialog(property)\">Click</div>\n        <div (click)=\"openRefListDialog(property)\" *ngIf=\"property.references\">\n          {{showReferenceIndexes(property)}}\n        </div>\n      </template>\n\n  </p-column>\n  <template let-property pTemplate=\"rowexpansion\">\n    <div>\n      <property-edit [property]=property></property-edit>\n    </div>\n  </template>\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n<div><br/><button type=\"button\" pButton [style]=\"{display:block}\" (click)=\"savePropertyListChanges(event, properties)\" label=\"Save Changes\" *ngIf=\"properties.length > 0\"></button></div>\n\n"

/***/ }),

/***/ 791:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"property-edit-form\" #propertyEditForm=\"ngForm\">\n\n  <md-grid-list cols=\"8\" gutterSize=\"2px\" rowHeight=\"70px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Property Name</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Property Name\" name=\"name\" [(ngModel)]=\"property.name\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let name of propName\" [value]=\"name.value\" overlayVisible=\"true\">{{ name.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Property Type</b></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <md-select placeholder=\"Property Type\" name=\"type\" [(ngModel)]=\"property.type\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let type of propType\" [value]=\"type.value\" overlayVisible=\"true\">{{ type.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Access</b></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"property.access\"\n                     defaultLabel=\"Access\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"\n                     (onChange) =\"changeAccess($event, property)\">\n      </p-multiSelect></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <md-checkbox [(ngModel)]=\"property.defining\" [checked]=\"property.defining\" name=\"defining\"><b>Defining</b></md-checkbox>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=3 [rowspan]=1>REFERENCED SUBSTANCE</md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Amount</b></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <div (click)=\"amountDisplay = !amountDisplay\" *ngIf=\"!property.value\">Add Amount</div>\n      <div (click)=\"amountDisplay = !amountDisplay\" *ngIf=\"property.value\">\n        {{showAmounts()}}\n      </div>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n     <div (click)=\"paramDisplay = !paramDisplay\">Param</div>\n       <div (click)=\"paramDisplay = !paramDisplay\" *ngIf=\"property.parameters && property.parameters.length > 0\">\n        <div *ngFor=\"let param of paramList\" >\n          {{param.name}}\n        </div>\n      </div>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n    <md-grid-tile [colspan]=8 [rowspan]=1 class=\"color-bg\" *ngIf=\"paramDisplay\">\n     parameter list\n      <param [param]=\"\">\n      <div *ngFor=\"let param of property.parameters\" >\n        <param [param]=\"param\">\n      </div>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=8 [rowspan]=1 class=\"color-bg\" *ngIf=\"amountDisplay\">\n      <amount [amount]=property.value></amount>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=8 [rowspan]=1 *ngIf=\"property.uuid\">\n      Created by <code>&nbsp;{{property.createdBy}}&nbsp;</code> on <code>&nbsp; {{property.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{property.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{property.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=4 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"deleteProperty(property)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"saveProperty(property)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 792:
/***/ (function(module, exports) {

module.exports = "<form (ngSubmit)=\"updateRef(selectedReference,$event)\" class=\"ref-edit-form\" #refEditForm=\"ngForm\">\n\n  <div md-dialog-title>\n    <p><span *ngIf=\"!selectedReference.getFlag('new')\">Edit <code>{{selectedReference.citation}}</code></span>\n      <span *ngIf=\"selectedReference.getFlag('new')\"><code>Create Reference</code></span>\n      <span><button type=\"button\" md-dialog-close><i class=\"material-icons md-18\">clear</i></button></span></p>\n  </div>\n\n  <md-dialog-content>\n    <md-input-container class=\"ref-full-width\">\n        <textarea mdInput name=\"citation\" placeholder=\"Citation\" required [(ngModel)]=\"selectedReference.citation\"></textarea>\n    </md-input-container>\n<p>\n    <md-select placeholder=\"Source Type\" name=\"sType\" [(ngModel)]=\"selectedReference.docType\">\n      <md-option *ngFor=\"let type of sourceTypeList\" [value]=\"type.value\">{{ type.label }}</md-option>\n    </md-select>\n  </p>\n\n    <p><br/>\n      <md-select placeholder=\"Source Class\" name=\"sClass\">\n        <md-option *ngFor=\"let type of documentSystemType\" [value]=\"type.value\" disabled>{{ type.label }}</md-option>\n      </md-select>\n  </p>\n\n<p>\n    <md-input-container class=\"ref-full-width\">\n        <input mdInput placeholder=\"Source ID\" name=\"sourceId\" [(ngModel)]=\"selectedReference.id\">\n    </md-input-container>\n  </p>\n\n    <p>\n    <md-input-container class=\"ref-full-width\">\n        <input mdInput placeholder=\"Url\" name=\"url\" [(ngModel)]=\"selectedReference.url\">\n    </md-input-container>\n    </p>\n\n    <p><button md-button><md-icon>file_upload</md-icon></button>Upload Document</p>\n\n    <p><md-checkbox class=\"ref-full-width\" name=\"pubDomain\" [(ngModel)]=\"selectedReference.publicDomain\">Public Domain</md-checkbox></p>\n\n    <p>\n      <span><p-multiSelect [options]=\"tags\" name=\"tags\" [(ngModel)]=\"selectedReference.tags\"\n                           defaultLabel=\"Tags\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect></span>\n\n      <span><p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"selectedReference.access\"\n                     defaultLabel=\"Access\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect></span>\n    </p>\n</md-dialog-content>\n\n  <md-dialog-actions>\n  <span><button type=\"submit\" md-button>Save</button></span>\n     <span *ngIf=\"selectedReference.getFlag('new')\">\n           <button type=\"button\" md-button (click)=\"deleteRef(selectedReference)\">Cancel</button>\n     </span>\n     <span *ngIf=\"!selectedReference.getFlag('new')\">\n           <button type=\"button\" md-button (click)=\"deleteRef(selectedReference)\">Delete</button>\n     </span>\n  </md-dialog-actions>\n\n</form>\n"

/***/ }),

/***/ 793:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"ref-edit-form\" #refEditForm=\"ngForm\">\n  <br/>\n  <md-grid-list cols=\"10\" gutterSize=\"2px\" rowHeight=\"70px\" [style.background]=\"'#EBF5FC'\">\n    <md-grid-tile [colspan]=1 [rowspan]=1>Citation</md-grid-tile>\n    <md-grid-tile [colspan]=7 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <textarea mdInput name=\"citation\" required [(ngModel)]=\"ref.citation\"></textarea>\n      </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n\n   <md-grid-tile [colspan]=2 [rowspan]=1>\n     <md-select placeholder=\"Source Type\" name=\"type\" [(ngModel)]=\"ref.docType\" [style.width]=\"'80%'\">\n       <md-option *ngFor=\"let type of sourceTypeList\" [value]=\"type.value\">{{ type.label }}</md-option>\n     </md-select>\n   </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Source Class\" name=\"sClass\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let type of documentSystemType\" [value]=\"type.value\" disabled>{{ type.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>Source ID</md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1><md-input-container class=\"ref-full-width\">\n      <input mdInput placeholder=\"Source ID\" name=\"sourceId\" [(ngModel)]=\"ref.id\">\n    </md-input-container></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n\n\n    <md-grid-tile [colspan]=1 [rowspan]=1>Reference URL</md-grid-tile>\n    <md-grid-tile [colspan]=5 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"url\" value=\"{{ref.url}}\" [(ngModel)]=\"ref.url\">\n      </md-input-container>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n    <button md-button><md-icon>file_upload</md-icon>Upload Document</button></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <md-checkbox class=\"ref-full-width\" name=\"pubDomain\" [(ngModel)]=\"ref.publicDomain\">Public Domain</md-checkbox>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n    TAGS\n      <!--<p-multiSelect [options]=\"tags\" name=\"tags\" [(ngModel)]=\"ref.tags\"\n                             defaultLabel=\"Tags\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>-->\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"ref.access\"\n                           defaultLabel=\"Access\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"></p-multiSelect>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n\n\n    <md-grid-tile [colspan]=10 [rowspan]=1 *ngIf=\"ref.uuid\">\n      Created by <code>&nbsp;{{ref.createdBy}}&nbsp;</code> on <code>&nbsp; {{ref.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{ref.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{ref.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=7 [rowspan]=1></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"deleteRef(ref)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <button type=\"button\" (click)=\"updateRef(ref,$event)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n</form><br/>\n"

/***/ }),

/***/ 794:
/***/ (function(module, exports) {

module.exports = "<form>\n  <div md-dialog-title align=\"center\">\n    <span>Select Reference</span> <span><button type=\"button\" md-button md-dialog-close><md-icon>clear</md-icon></button></span>\n    <br/><br/>\n    <div>\n      <md-button-toggle-group #group=\"mdButtonToggleGroup\" name=\"referenceView\" [(ngModel)]=\"referenceViewTab\">\n        <md-button-toggle value=\"all\">All</md-button-toggle>\n        <md-button-toggle value=\"selected\">Selected</md-button-toggle>\n        <md-button-toggle value=\"last5\">Last 5</md-button-toggle>\n      </md-button-toggle-group>\n    </div>\n  </div>\n\n\n  <md-dialog-content>\n    <md-input-container dense>\n      <input mdInput placeholder=\"Filter References\" [formControl]=\"refCtrl\"  [(ngModel)]=\"filterQuery\">\n     </md-input-container>\n\n     <md-list dense>\n      <md-list-item *ngFor=\"let ref of allRefsForDataFiltered\">\n       <md-checkbox [checked]=isChecked(ref) (change)=\"toggle(ref)\">{{ref.index}}.</md-checkbox>\n       <span>{{ref.citation}}</span>\n        <template #nonPublic>NP</template>\n        <span>{{ref.access}}</span>\n        <button md-button (click)=\"openRefEditDialog(ref)\"><md-icon>chevron_right</md-icon></button>\n      </md-list-item>\n    </md-list>\n</md-dialog-content>\n\n  <md-divider></md-divider>\n<md-dialog-actions align=\"center\">\n  <div><p><button md-button (click)=\"openRefEditDialog()\">Create New Reference</button></p></div>\n  <div><p><button md-button (click)=\"applySelectedToAllNames()\">Apply Selected to All Names</button></p></div>\n</md-dialog-actions>\n  <md-dialog-actions align=\"center\">\n    <button md-button (click)=\"saveChanges()\">Save Changes</button>\n  </md-dialog-actions>\n</form>\n\n"

/***/ }),

/***/ 795:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"refs\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\">\n <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addReference()\" label=\"Add Reference\"></button>\n    <!--  <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"refs.length > 0\" (click)=\"exportRefs()\" label=\"Export References\"></button>\n      <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"refs.length > 0\" (click)=\"search()\" label=\"Search\"></button>-->\n    </div>\n  </p-header>\n  <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n    <template pTemplate=\"header\">Edit</template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'80px'}\">\n    <template pTemplate=\"header\">Delete</template>\n    <template pTemplate=\"body\" let-ref=\"rowData\">\n      <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteRef(ref)\"></button>\n    </template>\n  </p-column>\n\n  <p-column [style]=\"{'width':'38px'}\">\n    <template pTemplate=\"header\">PD</template>\n    <template let-ref=\"rowData\" pTemplate=\"body\">\n      <md-checkbox [(ngModel)]=\"ref.publicDomain\" [checked]=\"ref.publicDomain\" name=\"publicDomain\"  pTooltip=\"Public Domain\"></md-checkbox>\n    </template>\n  </p-column>\n\n  <p-column field=\"citation\" header=\"Citation / Source Text\"  [style]=\"{'width':'500px'}\" [editable]=\"true\"></p-column>\n\n  <p-column field=\"docType\" header=\"Source Type\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-ref=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"ref[col.field]\" [options]=\"sourceTypes\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\" required=\"true\"></p-dropdown>\n    </template>\n  </p-column>\n\n  <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-ref=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"ref[col.field]\"\n                     defaultLabel=\"Access\" overlayVisible=\"true\" scrollHeight=\"500px\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column field=\"url\" header=\"Reference URL\"  [style]=\"{'width':'500px'}\" [editable]=\"true\"></p-column>\n\n  <template let-ref pTemplate=\"rowexpansion\">\n    <div>\n      <reference-edit [ref]=\"ref\"></reference-edit>\n    </div>\n  </template>\n\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n\n<!--<div>\n  <pre>\n  {{refs | json}}\n    </pre>\n</div>-->\n"

/***/ }),

/***/ 796:
/***/ (function(module, exports) {

module.exports = "<br/>\n<form class=\"relationship-edit-form\" #relationEditForm=\"ngForm\">\n\n  <md-grid-list cols=\"8\" gutterSize=\"2px\" rowHeight=\"70px\" [style.background]=\"'#EBF5FC'\">\n\n    <md-grid-tile [colspan]=2 [rowspan]=1>RELATED SUBSTANCE\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Relationsip Type</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Relationship Type\" name=\"type\" [(ngModel)]=\"relationship.type\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let type of relationshipTypes\" [value]=\"type.value\" overlayVisible=\"true\">{{ type.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Access</b></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"relationship.access\"\n                     defaultLabel=\"Access\" overlayVisible=\"true\" scrollHeight=\"500px\" appendTo=\"body\"\n                     (onChange) =\"changeAccess($event, relationship)\">\n      </p-multiSelect>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Qualification</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Qualification\" name=\"qualification\" [(ngModel)]=\"relationship.qualification\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let qual of qualification\" [value]=\"qual.value\" overlayVisible=\"true\">{{ qual.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Interaction Type</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1>\n      <md-select placeholder=\"Interaction Type\" name=\"interactionType\" [(ngModel)]=\"relationship.interactionType\" [style.width]=\"'80%'\">\n        <md-option *ngFor=\"let iType of interactionType\" [value]=\"iType.value\" overlayVisible=\"true\">{{ iType.label }}</md-option>\n      </md-select>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1><b>Amount</b></md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1>\n      <div (click)=\"amountDisplay = !amountDisplay\" *ngIf=\"!relationship.amount\">Add Amount</div>\n      <div (click)=\"amountDisplay = !amountDisplay\" *ngIf=\"relationship.amount\">\n        {{showAmounts(relationship.amount)}}\n      </div>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=8 [rowspan]=1 class=\"color-bg\" *ngIf=\"amountDisplay\">\n       <amount [amount]=relationship.amount></amount>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Mediator Substance</b></md-grid-tile>\n    <md-grid-tile [colspan]=2 [rowspan]=1> MEDIATOR SUBSTANCE</md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1> <b>Comments</b></md-grid-tile>\n    <md-grid-tile [colspan]=3 [rowspan]=1>\n      <md-input-container class=\"ref-full-width\">\n        <input mdInput name=\"relationship\" value=\"{{relationship.comments}}\" [(ngModel)]=\"relationship.comments\" placeholder=\"comments ...\">\n      </md-input-container>\n    </md-grid-tile>\n    <md-grid-tile [colspan]=1 [rowspan]=1></md-grid-tile>\n\n\n   <md-grid-tile [colspan]=8 [rowspan]=1 *ngIf=\"relationship.uuid\">\n      Created by <code>&nbsp;{{relationship.createdBy}}&nbsp;</code> on <code>&nbsp; {{relationship.created | date: 'MM/dd/yyyy'}}&nbsp;</code> , Edited by <code>&nbsp;{{relationship.lastEditedBy}}&nbsp;</code> on <code>&nbsp;{{relationship.lastEdited | date: 'MM/dd/yyyy'}}&nbsp;</code>\n    </md-grid-tile>\n\n    <md-grid-tile [colspan]=4 [rowspan]=1></md-grid-tile>\n       <md-grid-tile [colspan]=1 [rowspan]=1>\n         <button type=\"button\" (click)=\"deleteRelationship(relationship)\" pButton icon=\"fa-trash\" label=\"Delete\"></button>\n       </md-grid-tile>\n       <md-grid-tile [colspan]=1 [rowspan]=1>\n         <button type=\"button\" (click)=\"saveRelationship(relationship)\" pButton icon=\"fa-check\" label=\"Save\"></button>\n       </md-grid-tile>\n       <md-grid-tile [colspan]=2 [rowspan]=1></md-grid-tile>\n  </md-grid-list>\n\n</form><br/>\n\n"

/***/ }),

/***/ 797:
/***/ (function(module, exports) {

module.exports = "<p-dataTable [value]=\"relations\" expandableRows=\"true\" sizableColumns=\"true\"\n             [responsive]=\"true\" [editable]=\"true\" id=\"relationListTable\" #dt>\n  <p-header>\n    <div class=\"ui-helper-clearfix\" style=\"width:100%\">\n      <button type=\"button\" pButton icon=\"fa-plus\" style=\"float:left\" (click)=\"addRelationship()\" label=\"Add Relationship\"></button>\n      <button type=\"button\" pButton icon=\"fa-file-o\" style=\"float:left\" *ngIf=\"relations.length > 0\" (click)=\"exportRelations()\" label=\"Export Relationships\"></button>\n      <button type=\"button\" pButton icon=\"fa-search\" style=\"float:right\" *ngIf=\"relations.length > 0\" (click)=\"search()\" label=\"Search\"></button>\n    </div>\n  </p-header>\n    <p-column [style]=\"{'width':'38px'}\" expander=\"true\" styleClass=\"col-icon\">\n      <template pTemplate=\"header\">Edit</template>\n    </p-column>\n\n    <p-column [style]=\"{'width':'80px'}\">\n      <template pTemplate=\"header\">Delete</template>\n      <template pTemplate=\"body\" let-relation=\"rowData\">\n        <button type=\"button\" pButton icon=\"fa-trash\" (click)=\"confirmDeleteRelation(this)\"></button>\n      </template>\n    </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\" [style]=\"{'width':'150px'}\">\n    <template pTemplate=\"header\">Related Substance <br/>\n      <md-checkbox [checked]=\"showRSStruct\" name=\"showRSStruct\" (change)=\"showStructure()\" *ngIf=\"relations.length > 0\">Show Structure</md-checkbox>\n    </template>\n    <template let-col let-relation=\"rowData\" pTemplate=\"body\">\n      TEST TEST\n      <div *ngIf=\"showRSStruct\">\n        <img src=\"assets/images/protein.svg\" alt=\"structure\">\n      </div>\n    </template>\n  </p-column>\n\n  <p-column field=\"type\" header=\"Type\" [style]=\"{'overflow':'visible'}\" [style]=\"{'width':'300px'}\">\n    <template let-col let-relation=\"rowData\" pTemplate=\"body\">\n      <p-dropdown [(ngModel)]=\"relation[col.field]\" [options]=\"relationshipTypes\" [autoWidth]=\"false\" [style]=\"{'width':'100%'}\" required=\"true\" placeholder=\"type...\"></p-dropdown>\n    </template>\n  </p-column>\n\n<!--  <p-column field=\"type\" header=\"Type\" [editable]=\"true\" [filter]=\"true\" [style]=\"{'overflow':'visible'}\" filterMatchMode=\"equals\">\n    <template pTemplate=\"filter\" let-col let-relation=\"rowData\">\n      <p-dropdown [(ngModel)]=\"relation[col.field]\" [options]=\"relationshipTypes\" [style]=\"{'width':'100%'}\" (onChange)=\"dt.filter($event.value,col.field,col.filterMatchMode)\" styleClass=\"ui-column-filter\"></p-dropdown>\n    </template>\n  </p-column>-->\n\n\n\n  <p-column field=\"access\" header=\"Access\" [editable]=\"true\" [style]=\"{'overflow':'visible'}\">\n    <template let-col let-relation=\"rowData\" pTemplate=\"editor\">\n      <p-multiSelect [options]=\"access\" name=\"access\" [(ngModel)]=\"relation[col.field]\"\n                     defaultLabel=\"Public\" overlayVisible=\"true\" scrollHeight=\"500px\"\n                     (onChange) =\"changeAccess($event, relation)\"></p-multiSelect>\n    </template>\n  </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\">\n    <template pTemplate=\"header\">Amount</template>\n    <template let-col let-relation=\"rowData\" pTemplate=\"body\">\n      {{showAmounts(relation.amount)}}\n    </template>\n  </p-column>\n\n  <p-column field=\"\" [style]=\"{'overflow':'visible'}\">\n    <template pTemplate=\"header\">Qualification / <br/> Interaction Type</template>\n    <template let-col let-relation=\"rowData\" pTemplate=\"body\">\n      {{relation.qualificaion}} <div *ngIf=\"relation.interactionType\">/ {{relation.interactionType}}</div>\n    </template>\n  </p-column>\n\n  <p-column>\n    <template pTemplate=\"header\">References</template>\n    <template let-relation=\"rowData\"  pTemplate=\"body\">\n      <div *ngIf=\"!relation.references || relation.references.length == 0\" (click)=\"openRefListDialog(relation)\">Click</div>\n      <div (click)=\"openRefListDialog(relation)\" *ngIf=\"relation.references\">\n        {{showReferenceIndexes(relation)}}\n      </div>\n    </template>\n\n  </p-column>\n  <template let-relation pTemplate=\"rowexpansion\">\n    <div>\n      <relation-edit [relationship]=relation></relation-edit>\n    </div>\n  </template>\n</p-dataTable>\n<p-confirmDialog width=\"200px\"></p-confirmDialog>\n<div><br/><button type=\"button\" pButton [style]=\"{display:block}\" (click)=\"saveRelationListChanges(event, relations)\" label=\"Save Changes\" *ngIf=\"relations.length > 0\"></button></div>\n"

/***/ }),

/***/ 798:
/***/ (function(module, exports) {

module.exports = "<div class=\"demo-nav-bar\">\n  <div><pre>{{fjson}}</pre></div>\n\n\n  <nav md-tab-nav-bar aria-label=\"Substance properties\">\n    <a md-tab-link\n       *ngFor=\"let tabLink of tabs; let i = index\"\n       [routerLink]=\"tabLink.content\"\n       [active]=\"activeLinkIndex === i\"\n       (click)=\"activeLinkIndex = i\">\n      {{tabLink.header || tabLink.content | uppercase}} {{length || tabLink.value?.count || ''}}\n    </a>\n  </nav>\n  <router-outlet></router-outlet>\n  <div id=\"hidden-things\" ></div>\n  <div><br/><button type=\"submit\" pButton [style]=\"{display:block}\" (click)=\"save()\" label=\"Save Changes\"></button></div>\n</div>\n"

/***/ }),

/***/ 799:
/***/ (function(module, exports) {

module.exports = "<p>\n  Landing page for displaying substancelist!\n</p>\n"

/***/ }),

/***/ 800:
/***/ (function(module, exports) {

module.exports = "<div id=\"sum-internal\">\n<iframe id=\"subFrame\" load=\"onLoad()\" width=\"100%\" height=\"999\">\n  <p>Your browser does not support iframes.</p>\n</iframe>\n</div>\n"

/***/ }),

/***/ 851:
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(485);


/***/ })

},[851]);
//# sourceMappingURL=main.bundle.js.map