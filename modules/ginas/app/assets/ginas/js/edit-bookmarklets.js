"use strict";

function _typeof2(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof2 = function _typeof2(obj) { return typeof obj; }; } else { _typeof2 = function _typeof2(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof2(obj); }

function _typeof(obj) {
    if (typeof Symbol === "function" && _typeof2(Symbol.iterator) === "symbol") {
        _typeof = function _typeof(obj) {
            return _typeof2(obj);
        };
    } else {
        _typeof = function _typeof(obj) {
            return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : _typeof2(obj);
        };
    }

    return _typeof(obj);
}

function disulfideLinks() {
    var KNOWN_DISULFIDE_PATTERNS = {};
    ("IGG4	0-1,11-12,13-31,14-15,18-19,2-26,20-21,22-23,24-25,27-28,29-30,3-4,5-16,6-17,7-8,9-10\n" + "IGG2	0-1,11-12,13-14,15-35,16-17,2-30,22-23,24-25,26-27,28-29,3-4,31-32,33-34,5-18,6-19,7-20,8-21,9-10\n" + "IGG1	0-1,11-12,13-14,15-31,18-19,2-3,20-21,22-23,24-25,27-28,29-30,4-26,5-16,6-17,7-8,9-10").split("\n").map(function (s) {
        var tup = s.split("\t");

        var list = _.chain(tup[1].split(",")).map(function (t) {
            return _.map(t.split("-"), function (ii) {
                return ii - 0;
            });
        }).value();

        KNOWN_DISULFIDE_PATTERNS[tup[0]] = list;
    });
    var proteinSubstance = getJson();
    var prot = proteinSubstance.protein;
    var pattern = KNOWN_DISULFIDE_PATTERNS[prot.proteinSubType];

    if (!pattern) {
        alert("Unknown disulfide pattern for protein subtype:\"" + prot.proteinSubType + "\"");
        return;
    } else {
        if (!confirm("Would you like to set the disulfide pattern for:\"" + prot.proteinSubType + "\"")) {
            return;
        }
    }

    var realList = [];
    var cst = [];

    var cs = _.chain(prot.subunits).map(function (s) {
        var sid = s.subunitIndex;
        var i1 = 1;

        var v = _.chain(s.sequence).map(function (r) {
            return {
                "i": i1++,
                "r": r
            };
        }).filter(function (r) {
            return r.r === "C";
        }).map(function (r) {
            return {
                "su": sid,
                "r": r.r,
                "ri": r.i
            };
        }).value();

        for (var i = 0; i < v.length; i++) {
            cst.push(v[i]);
        }

        return v;
    }).value();

    cs = cst;

    for (var i = 0; i < cs.length; i++) {
        var c1 = cs[i];
        var real = {};
        real["subunitIndex"] = c1.su;
        real["residueIndex"] = c1.ri;
        real["display"] = c1.su + "_" + c1.ri;
        real["value"] = real.display;
        realList.push(real);
    }

    var newDS = _.chain(pattern).map(function (sl) {
        return [realList[sl[0]], realList[sl[1]]];
    }).map(function (s) {
        return {
            "sites": s,
            "sitesShorthand": s[0].display + ";" + s[1].display
        };
    }).value();

    if (prot.glycosylation) {
        if (prot.glycosylation.NGlycosylationSites) {
            var s = prot.glycosylation.NGlycosylationSites;
            prot.glycosylation.NGlycosylationSites.$$displayString = _.chain(s).map(function (s1) {
                return s1.subunitIndex + "_" + s1.residueIndex;
            }).value().join(";");
        }

        if (prot.glycosylation.CGlycosylationSites) {
            var s = prot.glycosylation.CGlycosylationSites;
            prot.glycosylation.CGlycosylationSites.$$displayString = _.chain(s).map(function (s1) {
                return s1.subunitIndex + "_" + s1.residueIndex;
            }).value().join(";");
        }

        if (prot.glycosylation.OGlycosylationSites) {
            var s = prot.glycosylation.OGlycosylationSites;
            prot.glycosylation.OGlycosylationSites.$$displayString = _.chain(s).map(function (s1) {
                return s1.subunitIndex + "_" + s1.residueIndex;
            }).value().join(";");
        }
    }

    prot.disulfideLinks = newDS;
    setJson(proteinSubstance);
}

function changeApproval() {
    var apid = prompt("Enter new ApprovalID:");

    if (apid) {
        var old = angular.element(document.body).scope().substance.approvalID;
        angular.element(document.body).scope().substance.approvalID = apid;
        angular.element(document.body).scope().$apply();
        alert("Approval ID changed from'" + old + "' to '" + apid + "'. Submit changes to save");
    }
}

function restoreSRU() {
    var sub = getJson();

    var url = sub._self.split("?")[0];

    $.getJSON(url + "/@edits", function (d) {
        var aScope = angular.element(document.body).scope();

        var edits = _.chain(d).filter(function (e) {
            return e.version;
        }).value().sort(function (a, b) {
            return a.version - 0 - (b.version - 0);
        }); //get first SRU list


        var getNext = function getNext(cb) {
            if (edits.length == 0) return;
            var edit = edits.pop();
            var oldUrl = edit.oldValue;
            $.getJSON(oldUrl, function (old) {
                cb(old);
            });
        };

        var realAmtTester = function realAmtTester(amt) {
            if (!amt) return false;
            if (amt.units && amt.units !== "") return true;
            if (amt.average && amt.average !== 0) return true;
            if (amt.low && amt.low !== 0) return true;
            if (amt.high && amt.high !== 0) return true;
            if (amt.lowLimit && amt.lowLimit !== 0) return true;
            if (amt.highLimit && amt.highLimit !== 0) return true;
            if (amt.nonNumericValue && amt.nonNumericValue !== "") return true;
            return false;
        };

        var fetchCallback = function thisCallback(dat) {
            if (dat.polymer && dat.polymer.structuralUnits) {
                var amounts = {};
                var amountLabels = [];
                var amountLabelsRestored = [];
                var found = false;

                _.chain(dat.polymer.structuralUnits).filter(function (sru) {
                    return realAmtTester(sru.amount);
                }).map(function (sru) {
                    found = true;
                    amounts[sru.label] = sru.amount;
                    amountLabels.push(sru.label);
                }).value();

                if (found) {
                    alert("Found amounts in version:" + dat.version + " from " + new Date(dat.lastEdited));

                    _.chain(aScope.substance.polymer.structuralUnits).forEach(function (sru) {
                        var lab = sru.label;

                        if (amounts[lab]) {
                            sru.amount = amounts[lab];

                            _.remove(amountLabels, function (l) {
                                return l === lab;
                            });

                            amountLabelsRestored.push(lab);
                        }
                    }).value();

                    if (amountLabels.length > 0) {
                        alert("Couldn't find SRU units:" + JSON.stringify(amountLabels) + " in current units list.");
                    } else {
                        alert("Restored amounts for SRUs:" + JSON.stringify(amountLabelsRestored));
                    }

                    aScope.$apply();
                    return;
                }
            }

            getNext(thisCallback);
        };

        getNext(fetchCallback);
    });
}

function mergeConcept() {
    function guid() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
        }

        ;
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
    }

    function scrub(oldraw) {
        var old = JSON.parse(JSON.stringify(oldraw));
        var uuidHolders = JSON.search(old, "//*[uuid]");
        var map = {};

        for (var i = 0; i < uuidHolders.length; i++) {
            var ouuid = uuidHolders[i].uuid;

            if (map[ouuid]) {
                uuidHolders[i].uuid = map[ouuid];
            } else {
                var nid = guid();
                uuidHolders[i].uuid = nid;
                map[ouuid] = nid;
            }
        }

        var refHolders = JSON.search(old, "//*[references]");

        for (var i = 0; i < refHolders.length; i++) {
            var refs = refHolders[i].references;

            for (var j = 0; j < refs.length; j++) {
                var or = refs[j];
                if (_typeof(or) === "object") continue;
                refs[j] = map[or];
            }
        }

        JSON.search(old, "//*[uuid]");

        _.remove(old.codes, {
            codeSystem: "BDNUM"
        });

        old.approvalID = null;
        old.approved = null;
        old.approvedBy = null;
        old.status = "pending";
        delete old["$$update"];
        return old;
    }

    var updateRecord = function updateRecord(nsub, cb) {
        $.ajax({
            url: '/ginas/app/api/v1/substances',
            contentType: "application/json",
            type: 'PUT',
            data: JSON.stringify(nsub),
            success: function success(data) {
                alert('Deprecation was performed.');
                $(".classchng-overlay").remove();
                cb();
            }
        });
    };

    function addMergeButton(cb) {
        document.body.appendChild($("<div class=\"classchng-overlay\"  style=\"z-index:999999;position:fixed;padding:10px;bottom:10px;right:0px;height:50px;width:400px;background: rgba(0, 0, 0, 0.6);\">\n        <div style=\"text-align: center;\">\n        <button class=\"btn btn-danger\" id='merge-submit'>Confirm Deprecate Old Record</button>\n        </div>\n        ")[0]);
        $("#merge-submit").click(function (e) {
            cb();
        });
    }

    var BDNUM_MAPPER = function BDNUM_MAPPER(osub) {
        return _.chain(osub.codes).filter(function (cd) {
            return cd.codeSystem === "BDNUM";
        }).filter(function (cd) {
            return cd.type === "PRIMARY";
        }).map(function (cd) {
            return cd.code;
        }).value()[0];
    };

    var sub = getJson();

    var BASE_SUBSTANCE_URL = sub._self.split("(")[0];

    function $P(cbConsumer) {
        var p = {};
        p.$isPromise = true;

        p.get = function (cb) {
            cbConsumer(cb);
            return p;
        };

        p.andThen = function (m) {
            return $P(function (cb) {
                p.get(function (or) {
                    var mcb = function mcb(cc) {
                        if (cc && cc.$isPromise) {
                            cc.get(function (r) {
                                return cb(r);
                            });
                        } else {
                            cb(cc);
                        }
                    };

                    try {
                        if (or.$isPromise) {
                            or.get(function (nr) {
                                mcb(m(nr));
                            });
                        } else {
                            mcb(m(or));
                        }
                    } catch (e) {
                        mcb(m(or));
                    }
                });
            });
        };

        p.and = function (p2) {
            return p.andThen(function (o) {
                return p2;
            });
        };

        p.map = p.andThen;
        return p;
    }

    function BDNUM_ASYNC(ref) {
        return $P(function (cb) {
            $.getJSON(BASE_SUBSTANCE_URL + "(" + ref.refuuid + ")/codes(codeSystem:BDNUM)(type:PRIMARY)($0)/code", function (d) {
                if (d) {
                    cb(d);
                } else {
                    $.getJSON(BASE_SUBSTANCE_URL + "(" + ref.refPname + ")/codes(codeSystem:BDNUM)(type:PRIMARY)($0)/code", function (d) {
                        cb(d);
                    });
                }
            });
        });
    }

    var mergeTheRecord = function mergeTheRecord(uuid) {
        $.getJSON(BASE_SUBSTANCE_URL + "(" + uuid + ")?view=full", function (d) {
            var nsub = scrub(d);
            var osub = getJson();
            var nbdnum = BDNUM_MAPPER(d);
            var obdnum = BDNUM_MAPPER(osub);
            var mapName = nbdnum + " MAPS TO " + obdnum + " " + osub._name;

            if (!confirm("Going to merge names, codes, notes, relationships and references from:" + nbdnum + " with this record.")) {
                return;
            } else {
                alert("Fields merged. Click 'Confirm Deprecate old record' to to prevent duplicate collision");
            }

            var addAll = function addAll(l1, l2) {
                l2.map(function (o) {
                    return l1.push(o);
                });
            };

            var bdref = {
                "uuid": guid(),
                "docType": "BDNUM",
                "citation": "Imported during merging with record:[" + nbdnum + "].",
                "id": nbdnum,
                "publicDomain": false,
                "tags": ["RECORD_MERGE"],
                "access": ["protected"]
            };

            var oldref = _.chain(osub.relationships).filter(function (r) {
                return r.relatedSubstance.refuuid === uuid;
            }).value();

            if (oldref.length > 0) {
                oldref[0].relatedSubstance.refPname = mapName;
            }

            nsub.relationships = _.chain(nsub.relationships).filter(function (r) {
                return r.type !== "SUBSTANCE->SUB_CONCEPT";
            }).value();
            nsub.references.push(bdref);

            for (var i = 0; i < nsub.names.length; i++) {
                if (nsub.names[i].displayName == true) {
                    nsub.names[i].displayName = false;
                }
            }

            addAll(osub.names, nsub.names.map(function (n) {
                n.references.push(bdref.uuid);
                return n;
            }));
            addAll(osub.codes, nsub.codes.map(function (n) {
                n.references.push(bdref.uuid);
                return n;
            }));
            addAll(osub.notes, nsub.notes.map(function (n) {
                n.references.push(bdref.uuid);
                return n;
            }));
            addAll(osub.relationships, nsub.relationships.map(function (n) {
                n.references.push(bdref.uuid);
                return n;
            }));
            addAll(osub.references, nsub.references);
            osub.changeReason = "Merged with " + nbdnum;
            setJson(osub);
            addMergeButton(function () {
                //when merging happens, you first delete the OLD record, then update the new
                if (!confirm("Are you sure you want to deprecate the old record? You will still need to submit this new merged record as well.")) {
                    return;
                }

                var depRef = {
                    "uuid": guid(),
                    "docType": "FDA_SRS",
                    "citation": "Generated as part of migration merge to:" + obdnum + ".",
                    "publicDomain": false,
                    "tags": ["RECORD_MERGE"],
                    "access": ["protected"]
                };
                d.references.push(depRef);
                d.names = [{
                    "language": "en",
                    "name": mapName,
                    "access": ["admin", "protected"],
                    "references": [depRef.uuid]
                }];
                d.codes = _.chain(d.codes).filter(function (c) {
                    return c.codeSystem === "BDNUM";
                }).value();
                d.notes = [{
                    "note": "Data migrated to record:" + obdnum
                }];
                d.relationships = _.chain(d.relationships).filter(function (r) {
                    return r.type === "SUBSTANCE->SUB_CONCEPT";
                }).value();
                d.deprecated = true;

                if (!d.access) {
                    d.access = [];
                }

                d.access.push("protected");
                d.changeReason = "Migrated data into:" + obdnum;
                updateRecord(d, function () {
                    $(".classchng-overlay").remove();
                    alert("Old record deprecated, please save this record to complete merge.");
                });
            });
        });
    };

    var full_prom = null;

    var currentSubconcepts = _.chain(sub.relationships).filter(function (r) {
        return r.type === "SUB_CONCEPT->SUBSTANCE";
    }).map(function (r) {
        return r.relatedSubstance;
    }).map(function (r) {
        r["$bdnum"] = BDNUM_ASYNC(r).andThen(function (bd) {
            r["bdnum"] = bd;
        });

        if (full_prom) {
            full_prom = full_prom.and(r["$bdnum"]);
        } else {
            full_prom = r["$bdnum"];
        }

        return r;
    }).value();

    if (full_prom) {
        full_prom.get(function () {
            //TODO: get both the BDNUMs, etc from rest API
            var htmlForSelections = currentSubconcepts.map(function (c) {
                return "<div>\n                <button value=\"" + c.refuuid + "\" class=\"btn btn-primary merge-selection-item\">" + c.bdnum + "[" + c.refPname + "]" + "</button></div>";
            }).join("\n");
            document.body.appendChild($("<div class=\"classchng-overlay\" style=\"z-index:999999;position:fixed;top:0px;width:100%;height:100%;background: rgba(0, 0, 0, 0.6);\">\n                <div style=\" text-align: center;padding: 100px;\">\n                    <div><h3 style=\"color:white\">Which record would you like to merge into this record?</h3></div>\n                    <div class=\"merge-selection\">" + htmlForSelections + "</div>\n                    <div><button class=\"btn btn-danger classchng-cancel\">Cancel</button></div>\n                    </div>\n                </div>\n                ")[0]);
            $(".merge-selection-item").click(function (e) {
                $(".classchng-overlay").remove();
                mergeTheRecord(e.currentTarget.value);
            });
            $(".classchng-cancel").click(function (e) {
                $(".classchng-overlay").remove();
            });
        });
    } else {
        alert("No sub-concepts were found for this record");
    }
}

function changeClass() {
    document.body.appendChild($("<div class=\"classchng-overlay\" style=\"z-index:999999;position:fixed;top:0px;width:100%;height:100%;background: rgba(0, 0, 0, 0.6);\">\n    <div style=\"text-align: center;padding: 100px;\">\n        <div>\n            <h3 style=\"color:white\">What class would you like to convert this record to?</h3>\n            </div>\n            <button value=\"protein\" class=\"classchng\" onclick = \"changeTheClass(this.value)\">Protein</button>\n            <button value=\"chemical\" class=\"classchng\" onclick = \"changeTheClass(this.value)\">Chemical</button>\n            <button value=\"structurallyDiverse\" class=\"classchng\" onclick = \"changeTheClass(this.value)\">Structurally Diverse</button>\n            <button value=\"polymer\" class=\"classchng\" onclick = \"changeTheClass(this.value)\">Polymer</button>\n            <button value=\"nucleicAcid\" class=\"classchng\" onclick = \"changeTheClass(this.value)\">Nucleic Acid</button>\n            <button value=\"mixture\" class=\"classchng\" onclick = \"changeTheClass(this.value)\">Mixture</button>\n            <button value=\"concept\" class=\"classchng\" onclick = \"changeTheClass(this.value)\">Concept</button>\n            <div>\n                <button class=\"classchng-cancel\">Cancel</button>\n            </div>\n        </div>\n</div>\n")[0]);
    $(".classchng-cancel").click(function (e) {
        $(".classchng-overlay").remove();
    });
}

function changeTheClass(nkind) {
    var nurl = location.href.split("\?")[0] + "?kind=" + nkind;
    angular.element(document.body).scope().updateNav = false;
    window.location.href = nurl;
}

function predictSites() {
    function setJson(json) {
        var molChanger = angular.element(document.body).injector().get("molChanger");
        var Substance = angular.element(document.body).injector().get("Substance");
        var $scope = angular.element(document.body).scope();
        Substance.$$setSubstance(json).then(function (data) {
            $scope.substance = data;

            if ($scope.substance.substanceClass == "chemical") {
                molChanger.setMol($scope.substance.structure.molfile);
            }
        });
    }

    var gfinder = function gfinder(sn, seq) {
        var re = new RegExp('N[^P][ST]', "g");
        var xArray;
        var sites = [];

        while (xArray = re.exec(seq)) {
            var ri = xArray.index + 1;
            sites.push({
                subunitIndex: sn,
                residueIndex: ri
            });
        }

        return sites;
    };

    var proteinGlycFinder = function proteinGlycFinder(proteinSubstance) {
        return _.chain(proteinSubstance.protein.subunits).flatMap(function (su) {
            return gfinder(su.subunitIndex, su.sequence);
        }).value();
    };

    var sub = angular.element(document.body).scope().substance.$$flattenSubstance();
    var gsites = proteinGlycFinder(sub);

    if (gsites.length == 0) {
        alert("No potential N-Glycosylation sites found");
        return;
    } else {
        alert("Found: " + gsites.length + " glycosylation sites. Submit record to save changes");
    }

    gsites.$$displayString = angular.element(document.body).injector().get("siteList").siteString(gsites);
    sub.protein.glycosylation.NGlycosylationSites = gsites;
    setJson(sub);
}

function setDefinitionPrivate() {
    var sss = angular.element(document.body).scope().substance;

    if (sss.structurallyDiverse) {
        setPrivate(sss.structurallyDiverse);
    } else if (sss.protein) {
        setPrivate(sss.protein);
    } else if (sss.structure) {
        setPrivate(sss.structure);
    } else if (sss.mixture) {
        setPrivate(sss.mixture);
    } else if (sss.polymer) {
        setPrivate(sss.polymer);
    } else if (sss.nucleicAcid) {
        setPrivate(sss.nucleicAcid);
    } else if (sss.specifiedSubstance) {
        setPrivate(sss.specifiedSubstance);
    } else {
        console.log('not found');
    }

    function setPrivate(e) {
        e.access = ["protected"];
        alert("Substance definition now set to protected, please submit to save change");
    }
}

function conceptNonApproved() {
    var sss = angular.element(document.body).scope().substance;

    if (sss.substanceClass === "concept") {
        sss.status = "non-approved";
        alert("Concept status set to \"non approved\", please submit to save changes");
    } else {
        alert("Can only change status of concept records");
    }
}

function unapproveRecord() {
    var apid = prompt("Are you sure you'd like to remove the approvalID? This will \"unapprove\" the record. (Type \"YES\" to confirm)");

    if (apid === "YES") {
        var old = angular.element(document.body).scope().substance.approvalID;
        angular.element(document.body).scope().substance.approvalID = null;
        angular.element(document.body).scope().substance.status = null;
        angular.element(document.body).scope().substance.approved = null;
        angular.element(document.body).scope().substance.approvedBy = null;
        angular.element(document.body).scope().$apply();
        alert("Removed approvalID '" + old + "'. Submit record to save.");
    }
}

function setDefinitionPublic() {
    var sss = angular.element(document.body).scope().substance;

    if (sss.structurallyDiverse) {
        setPublic(sss.structurallyDiverse);
    } else if (sss.protein) {
        setPublic(sss.protein);
    } else if (sss.structure) {
        setPublic(sss.structure);
    } else if (sss.mixture) {
        setPublic(sss.mixture);
    } else if (sss.polymer) {
        setPublic(sss.polymer);
    } else if (sss.nucleicAcid) {
        setPublic(sss.nucleicAcid);
    } else if (sss.specifiedSubstance) {
        setPublic(sss.specifiedSubstance);
    } else {
        console.log('not found');
    }

    function setPublic(e) {
        e.access = [];
        console.log(e.access);
        alert("Substance definition set to be PUBLIC, please submit to save change");
    }
}

function setStatusToPending() {
    angular.element(document.body).scope().substance.status = "pending";
    alert("Substance set to pending, please submit to save change");
}

function setStatusToApproved() {
    angular.element(document.body).scope().substance.status = "approved";
    alert("Substance set to approved, please submit to save change");
}

function standardizeNames() {
    var ascii = /^[ -~\t\n\r]+$/;
    var bad = /[^ -~\t\n\r]/g;
    var rep = "\u2019;';\u03B1;.ALPHA.;\u03B2;.BETA.;\u03B3;.GAMMA.;\u03B4;.DELTA.;\u03B5;.EPSILON.;\u03B6;.ZETA.;\u03B7;.ETA.;\u03B8;.THETA.;\u03B9;.IOTA.;\u03BA;.KAPPA.;\u03BB;.LAMBDA.;\u03BC;.MU.;\u03BD;.NU.;\u03BE;.XI.;\u03BF;.OMICRON.;\u03C0;.PI.;\u03C1;.RHO.;\u03C2;.SIGMA.;\u03C3;.SIGMA.;\u03C4;.TAU.;\u03C5;.UPSILON.;\u03C6;.PHI.;\u03C7;.CHI.;\u03C8;.PSI.;\u03C9;.OMEGA.;\u0391;.ALPHA.;\u0392;.BETA.;\u0393;.GAMMA.;\u0394;.DELTA.;\u0395;.EPSILON.;\u0396;.ZETA.;\u0397;.ETA.;\u0398;.THETA.;\u0399;.IOTA.;\u039A;.KAPPA.;\u039B;.LAMBDA.;\u039C;.MU.;\u039D;.NU.;\u039E;.XI.;\u039F;.OMICRON.;\u03A0;.PI.;\u03A1;.RHO.;\u03A3;.SIGMA.;\u03A4;.TAU.;\u03A5;.UPSILON.;\u03A6;.PHI.;\u03A7;.CHI.;\u03A8;.PSI.;\u03A9;.OMEGA.;\u2192;->;\xB1;+/-;\u2190;<-;\xB2;2;\xB3;3;\xB9;1;\u2070;0;\u2071;1;\u2072;2;\u2073;3;\u2074;4;\u2075;5;\u2076;6;\u2077;7;\u2078;8;\u2079;9;\u207A;+;\u207B;-;\u2080;0;\u2081;1;\u2082;2;\u2083;3;\u2084;4;\u2085;5;\u2086;6;\u2087;7;\u2088;8;\u2089;9;\u208A;+;\u208B;-".split(";");

    var map = {};

    function replacer(match, got) {
        return map[got.charCodeAt(0)];
    }

    function scifind(str) {
        mk();
        var tmp = str.replace(/\n/g, ";").toUpperCase().split(";");
        var full = "";

        for (var t in tmp) {
            var name = tmp[t];
      name = name.replace(/([\u0390-\u03C9||\u2192|\u00B1-\u00B9|\u2070-\u208F|\u2190|])/g, replacer).trim();
            name = name.replace(bad, "");
            name = name.replace(/[[]([A-Z -.]*)\]$/g, " !!@!$1_!@!");
            name = name.replace(/[ \t]+/g, " ");
            name = name.replace(/[[]/g, "(");
            name = name.replace(/[{]/g, "(");
            name = name.replace(/\]/g, ")");
            name = name.replace(/\"/g, "''");
            name = name.replace(/[}]/g, ")");
            name = name.replace(/\(([0-9]*CI,)*([0-9]*CI)\)$/gm, "");
            name = name.replace(/[ ]*-[ ]*/g, "-");
            name = name.trim();
            name = name.replace("!!@!", "[");
            name = name.replace("_!@!", "]");
            full += name + "\n";
        }

        return full.trim();
    }

    function mk() {
        for (var s = 0; s < rep.length; s++) {
            if (s % 2 == 0) {
                var id = rep[s].charCodeAt(0);
                map[id] = rep[s + 1];
            }
        }

    }

    function update(input) {
        input.val(scifind(input.val()));
        input.trigger('input');
        input.trigger('change');
    }

    function updateAll() {
        var v = $("input[title='name']");

        for (var i = 0; i < v.length; i++) {
            update($(v[i]));
        }

        alert("Names have been standardized");
    }

    updateAll();

    _.chain(angular.element(document.body).scope().substance.names).filter(function (n) {
        return !n.uuid;
    }).forEach(function (n) {
        return n.uuid = angular.element(document.body).injector().get("UUID").newID();
    }).value();
}

function resetDefRef() {
    setTimeout(function () {
        var scope1 = angular.element(document.body).scope();
        var oldClass = scope1.substance.substanceClass;
        scope1.substance.substanceClass = "concept";
        scope1.$apply();
        setTimeout(function () {
            scope1.substance.substanceClass = oldClass;
            scope1.$apply();
        }, 1);
    }, 1);
}

function addFragment() {
    function getSketcher() {
        if (!window["JSDraw2"]) return null;

        for (var k in JSDraw2.Editor._allitems) {
            return JSDraw2.Editor._allitems[k];
        }

        return null;
    }

    ;

    if (getSketcher() == null) {
        alert("You must run this script from a window with a structure editor");
        return;
    }

    function addTerm(domain, term) {
        $.ajax({
            url: '/ginas/app/api/v1/vocabularies/search?q=root_domain:"^' + domain + '$"',
            success: function success(data) {
                var cvt = data.content[0].id;
                addTermToCVID(cvt, term);
            }
        });
    }

    function addTermToCVID(id, term) {
        $.ajax({
            url: '/ginas/app/api/v1/vocabularies(' + id + ')',
            success: function success(data) {
                var cv = data;
                cv.terms.push(term);
                $.ajax({
                    url: '/ginas/app/api/v1/vocabularies',
                    contentType: "application/json",
                    type: 'PUT',
                    data: JSON.stringify(cv),
                    success: function success(data) {
                        alert('Load was performed.');
                    }
                });
            }
        });
    }

    function getPossibleSmiles() {
        var smi = getSketcher().getSmiles();

        function getMarkers(smi) {
            var temp = smi.replace(/[^A-Z*]/g, "");
            var alias = {
                list: []
            };

            function getCombination(ll, i) {
                var cur = ll;
                var ret = [];

                for (var i2 = 0; i2 < ll.length; i2++) {
                    var elm = i % cur.length;
                    ret.push(cur[elm]);
                    var rr = [];

                    for (var n = 0; n < cur.length; n++) {
                        if (n !== elm) {
                            rr.push(cur[n]);
                        }
                    }

                    cur = rr;
                }

                return ret;
            }

            function fact(i) {
                var t = 1;

                for (; i > 1; i--) {
                    t = t * i;
                }

                return t;
            }

            function forEachCombination(ll, c) {
                for (var i = 0; i < fact(ll.length); i++) {
                    c(getCombination(ll, i));
                }
            }

            alias.smiles = smi;
            alias.stars = [];

            alias.add = function (a) {
                alias.list.push(a);

                if (a === "*") {
                    alias.stars.push(alias.list.length - 1);
                }

                return alias;
            };

            alias.asAlias = function () {
                return "|$" + alias.list.join(";") + "$|";
            };

            alias.eachForm = function (labs) {
                var tot = [];
                forEachCombination(labs, function (a) {
                    for (var i = 0; i < a.length; i++) {
                        alias.list[alias.stars[i]] = a[i];
                    }

                    tot.push(alias.asFullSmiles());
                });
                return tot;
            };

            alias.asFullSmiles = function () {
                return alias.smiles + " " + alias.asAlias();
            };

            for (var i in temp) {
                if (temp[i] === "*") {
                    alias.add("*");
                } else {
                    alias.add("");
                }
            }

            return alias;
        }

        return getMarkers(smi);
    }

    $.ajax({
        url: '/ginas/app/api/v1/vocabularies/search?facet=ix.Class/ix.ginas.models.v1.FragmentControlledVocabulary',
        success: function success(data) {
            var dat = {};

            var domains = _.chain(data.content).map(function (f) {
                dat[f.domain] = f;
                return f.domain;
            }).uniq().map(function (f) {
                return "<button value='" + f + "' class='frag_cv'>" + f + "</button>";
            }).value().join("\n");

            document.body.appendChild($("<div class=\"frag_cv-overlay\" style=\"z-index:999999;position:fixed;top:0px;width:100%;height:100%;background: rgba(0, 0, 0, 0.6);\">\n                    <div style=\"text-align: center;padding: 100px;\">\n                    <div>\n                    <h3 style=\"color:white\">What type of fragment do you want to add?</h3>\n                    </div>" + domains + "<div>\n                    <button class=\"frag_cv-cancel\">Cancel</button>\n                    </div>\n                    </div>\n                    </div>\n                    ")[0]);
            $(".frag_cv").click(function (e) {
                var domain = e.currentTarget.value;
                $(".frag_cv-overlay").remove();

                var rgs = _.chain(dat[domain].terms).map(function (t) {
                    return t.fragmentStructure;
                }).filter(function (t) {
                    return typeof t !== "undefined";
                }).map(function (t) {
                    return t.split(" ")[1];
                }).filter(function (t) {
                    return typeof t !== "undefined";
                }).flatMap(function (t) {
                    return t.replace(/[|]/g, "").replace(/[$]/g, "").split(";");
                }).uniq().filter(function (t) {
                    return t.indexOf("_") == 0;
                }).value();

                var tt = getPossibleSmiles();

                if (tt.stars.length <= 0) {
                    alert("No star atoms specified, expecting:" + rgs.length + " star atoms");
                    return;
                } else if (tt.stars.length != rgs.length) {
                    alert("Expected " + rgs.length + " star atoms, but found:" + tt.stars.length);
                }

                var smilesforms = tt.eachForm(rgs);
                $("body").append("<div class='smilesForm-overlay' style='z-index:999999;position:fixed;top:0px;width:100%;height:100%;background: rgba(0, 0, 0, 0.6);' id='testytest'><div class='cont' style='margin: 100px;background: white;padding: 10px;'></div></div>");
                $(".smilesForm-overlay .cont").append("Select which connectivity form you want to add:<br><br>" + _.chain(smilesforms).map(function (t) {
                    return [t, t.replace(/%/g, "%25").replace(/#/g, "%23").replace(/[;]/g, "%3B").replace(/[+]/g, "%2B")];
                }).map(function (t) {
                    return '<a href="#"><img onclick="_procDoSomething(\'' + t[0] + '\')" width="height=150" height="150" src="/ginas/app/render?structure=' + t[1] + '&amp;size=150&amp;standardize=true"></a>';
                }).value().join("\n") + "<br><br><a onclick='_procDoSomething(false)' href='#'>Cancel</a>");

                window["_procDoSomething"] = function (smi) {
                    $(".smilesForm-overlay").remove();
                    if (!smi) return;
                    var val = prompt("Enter code value for this fragment (e.g. 'R'):");
                    var newThing = {
                        "value": val,
                        "display": prompt("Enter display value for this fragment (e.g. 'Ribose')'"),
                        "description": val,
                        "fragmentStructure": smi,
                        "simplifiedStructure": smi
                    };

                    if (confirm("Are you sure you want to add this term?:\"" + newThing.display + "\" (" + newThing.value + ")")) {
                        addTerm(domain, newThing);
                    }
                };
            });
            $(".frag_cv-cancel").click(function (e) {
                $(".frag_cv-overlay").remove();
            });
        }
    });
}

function definitionSwitch() {
    var sub = getJson();
    var fieldGetter = {
        'protein': ['protein', 'modifications', 'properties'],
        'chemical': ['structure', 'moieties', 'modifications', 'properties'],
        'structurallyDiverse': ['structurallyDiverse', 'modifications', 'properties'],
        'polymer': ['polymer', 'modifications', 'properties'],
    'nucleicAcid': ['nucleicAcid', 'modifications', 'properties'],
    'mixture' : ['mixture', 'modifications', 'properties']
    };
    var primeVersion = sub.version;
    var altVersion = '';
    var full_prom = null;
    var oldAlt = {};
    var didStep5 = false;
    var oldPrime = sub;
    var uuidNew = guid();
    var newStructureID = guid();
    var structureuuid = guid();
    var structureid = guid();
    var alt = {};

    var currentAlts = _.chain(sub.relationships).filter(function (r) {
        if (r.type === "SUBSTANCE->SUB_ALTERNATE") {
            return r;
        }
    }).map(function (r) {
        return r.relatedSubstance;
    }).value();

    if (currentAlts.length > 0) {
        var htmlForSelections = currentAlts.map(function (c) {
            return "<button value=\"" + c.refuuid + "\" class=\"merge-selection-item\" style = \"margin:10px;margin-right:5px;padding:0px\"><div class = 'text-center thumb-col ' style=\"background-color:white;border-radius:3px;padding: 5px;max-width: 210px;display: inline-block;float: none;\" class=\"col-md-3\"><img class = 'struc-thumb img-responsive subref' style = \"width:200px\" src='app/img/" + c.refuuid + ".svg?size=200' /><span style = 'background-color:white;color:black' class = 'code'>" + c.linkingID + "</span><br/><span><a onclick=\"event.stopPropagation();\" target=\"_blank\" uib-tooltip=\"view substance in new window\" href=\"app/substance/" + c.refuuid + "\" aria-hidden=\"false\">View in new tab <i class=\"fa fa-external-link-square\" aria-hidden=\"true\"></i></a></span></div></button>";
        }).join("\n");
        document.body.appendChild($("<div class=\"classchng-overlay\" style=\"z-index:999999;position:fixed;top:0px;width:100%;height:100%;background: rgba(0, 0, 0, 0.6);\">\n                <div style=\" text-align: center;padding: 100px;\">\n                    <div><h3 style=\"color:white\">Which record would you like switch definitions with?</h3></div>\n                    <div class=\"merge-selection row\" style = \"text-align:center\">" + htmlForSelections + "</div>\n                    <div><button class=\"btn btn-danger classchng-cancel\">Cancel</button></div>\n                    </div>\n                </div>\n                ")[0]);
        $(".merge-selection-item").click(function (e) {
            $(".classchng-overlay").remove();

            if (confirm("This process involves multiple updates to both records and may take several minutes.\n If the switch fails at any stage, follow the instructions that appear to restore both records. \n\n Click 'OK' to proceed.")) {
                showLoadingWheel();
                tempPrimeChange(e.currentTarget.value);
            }
        });
        $(".classchng-cancel").click(function (e) {
            $(".classchng-overlay").remove();
        });
    } else {
        alert("No alternate definitions were found for this record");
    }

    var tempPrimeChange = function tempPrimeChange(uuid) {
        $.getJSON(sub._self.split("(")[0] + "(" + sub.uuid + ")?view=full", function (c) {
            oldPrime = _.cloneDeep(c);
      if(!fieldGetter[oldPrime.substanceClass]){
        alert('This substance is incompatible with the definition switch function');
        return;
      }
            fieldGetter[sub.substanceClass].forEach(function (x) {
        if(oldPrime[x]){
                delete oldPrime[x];
        }
            });
            console.log('setting primary to temporary substance type');
            var depRef = {
                "uuid": uuidNew,
                "docType": "FDA_SRS",
                "citation": "Generated to switch definition type",
                "publicDomain": true,
                "tags": ["RECORD_MERGE", "PUBLIC DOMAIN RELEASE"],
                "access": []
            };

            if (oldPrime.substanceClass != "structurallyDiverse") {
                oldPrime.substanceClass = "structurallyDiverse";
                oldPrime.structurallyDiverse = {
                    "uuid": guid(),
                    "created": 1567806115158,
                    "createdBy": "definitionSwitcher",
                    "lastEdited": 1567806115158,
                    "lastEditedBy": "definitionSwitcher",
                    "sourceMaterialClass": "Temporary class for definition switch",
                    "sourceMaterialType": "Temporary class for definition switch",
                    "part": ["WHOLE"],
                    "references": [uuidNew]
                };
            } else {
                oldPrime.substanceClass = "chemical";
                oldPrime.structure = {
                    "opticalActivity": "none",
                    "access": [],
                    "molfile": "\n   JSDraw209061916362D\n\n  6  6  0  0  0  0            999 V2000\n   28.8600   -9.2560    0.0000 He  0  0  0  0  0  0  0  0  0  0  0  0\n   30.2110   -8.4760    0.0000 He  0  0  0  0  0  0  0  0  0  0  0  0\n   30.2110   -6.9160    0.0000 He  0  0  0  0  0  0  0  0  0  0  0  0\n   28.8600   -6.1360    0.0000 He  0  0  0  0  0  0  0  0  0  0  0  0\n   27.5090   -8.4760    0.0000 He  0  0  0  0  0  0  0  0  0  0  0  0\n   27.5090   -6.9160    0.0000 He  0  0  0  0  0  0  0  0  0  0  0  0\n  1  2  1  0  0  0  0\n  2  3  1  0  0  0  0\n  3  4  1  0  0  0  0\n  1  5  1  0  0  0  0\n  5  6  1  0  0  0  0\n  4  6  1  0  0  0  0\nM  END",
                    "deprecated": false,
                    "digest": "4b4cb19b839f6eb23b836addbaa87729a9632a35",
                    "smiles": "[He]1[He][He][He][He][He]1",
                    "formula": "He6",
                    "stereoCenters": 0,
                    "definedStereo": 0,
                    "ezCenters": 0,
                    "charge": 0,
                    "mwt": 24.015612,
                    "count": 1,
                    "hash": "3ZYHCH786T4L",
                    "stereochemistry": "ACHIRAL",
                    "id": newStructureID,
                    "references": [uuidNew]
                };
            }

            oldPrime.references.push(depRef);
            $.getJSON(sub._self.split("(")[0] + "(" + uuid + ")?view=full", function (d) {
                oldAlt = _.cloneDeep(d);
        if(!fieldGetter[oldAlt.substanceClass]){
          alert('The selected alternative is incompatible with the definition switch function');
          return;
        }
                if (oldAlt.substanceClass == sub.substanceClass) {
                    updateRecord(oldPrime, function () {
                        AltNewType(oldAlt);
                    }, 1);
                } else {
                    updateRecord(oldPrime, function () {
                        AltNewDef(oldAlt);
                    }, 1);
                }
            });
        });
    };

    var AltNewType = function AltNewType(alt) {
        didStep5 = true;
        $.getJSON(sub._self.split("(")[0] + "(" + oldAlt.uuid + ")?view=full", function (d) {
            alt = _.cloneDeep(d);

            var altSwitch = _.cloneDeep(d);

            fieldGetter[altSwitch.substanceClass].forEach(function (x) {
        if(alt[x]){
                delete alt[x];
        }
            });

            if (altSwitch.substanceClass == 'structurallyDiverse') {
                console.log('deleting ' + altSwitch.substanceClass + ' adding temporary chemical');
                altSwitch.substanceClass = "chemical";
                altSwitch.structure = {
                    "opticalActivity": "none",
                    "access": [],
                    "molfile": "\n   JSDraw209051918142D\n\n  6  6  0  0  0  0            999 V2000\n   17.6800   -3.7440    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   16.3290   -2.9640    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   16.3290   -1.4040    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   17.6800   -0.6240    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   19.0310   -1.4040    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   19.0310   -2.9640    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  1  2  1  0  0  0  0\n  2  3  1  0  0  0  0\n  3  4  1  0  0  0  0\n  4  5  1  0  0  0  0\n  5  6  1  0  0  0  0\n  6  1  1  0  0  0  0\nM  END",
                    "id": structureid,
                    "references": [structureuuid]
                };
            } else {
                console.log('deleting ' + altSwitch.substanceClass + ' adding temporary structurallyDiverse');
                altSwitch.substanceClass = "structurallyDiverse";
                altSwitch.structurallyDiverse = {
                    "uuid": guid(),
                    "created": 1567806115158,
                    "createdBy": "definitionSwitcher",
                    "lastEdited": 1567806115158,
                    "lastEditedBy": "definitionSwitcher",
                    "deprecated": false,
                    "sourceMaterialClass": "Temporary class for definition switch",
                    "sourceMaterialType": "Temporary class for definition switch",
                    "part": ["WHOLE"],
                    "references": [structureuuid]
                };
            }

            var depRef = {
                "uuid": structureuuid,
                "docType": "FDA_SRS",
                "citation": "Generated to switch definition type",
                "publicDomain": true,
                "tags": ["RECORD_MERGE", "PUBLIC DOMAIN RELEASE"],
                "access": []
            };
            altSwitch.references.push(depRef);
            updateRecord(altSwitch, function () {
                AltNewDef(oldAlt);
            }, '2b');
        });
    };

    var AltNewDef = function AltNewDef(alt) {
        $.getJSON(sub._self.split("(")[0] + "(" + oldAlt.uuid + ")?view=full", function (d) {
            alt = _.cloneDeep(d);
            console.log('deleting ' + alt.substanceClass + ' adding ' + sub.substanceClass);
            fieldGetter[alt.substanceClass].forEach(function (x) {
        if(alt[x]){
                delete alt[x];
        }
            });
            fieldGetter[sub.substanceClass].forEach(function (x) {
        if(sub[x]){
                alt[x] = sub[x];
        }
            });
            alt.substanceClass = sub.substanceClass;
            var altReferences = JSON.search(alt, "//*[references]");
            var objectsA = altReferences.filter(function (e) {
                if (_typeof2(e) === "object") {
                    return true;
                } else {
                    return false;
                }
            });
            var toPush = [];

            for (var i = 0; i < objectsA.length; i++) {
                var current = objectsA[i].references;

                for (var k = 0; k < current.length; k++) {
                    for (var l = 0; l < sub.references.length; l++) {
                        if (sub.references[l].uuid == current[k]) {
                            var replace = guid();
                            current[k] = replace;
                            sub.references[l].uuid = replace;
                            toPush.push(sub.references[l]);
                        }
                    }
                }
            }

            toPush.forEach(function (ref) {
                alt.references.push(ref);
            });

            if (didStep5 === true) {
                alt.references = alt.references.filter(function (r) {
                    if (r.uuid != structureuuid) {
                        return r;
                    }
                });
            }

            updateRecord(alt, function () {
                primeNewDef(alt);
            }, 2);
        });
    };

    var primeNewDef = function primeNewDef(alt) {
        $.getJSON(sub._self.split("(")[0] + "(" + sub.uuid + ")?view=full", function (e) {
            $.getJSON(sub._self.split("(")[0] + "(" + oldAlt.uuid + ")?view=full", function (f) {
                var newSub = _.cloneDeep(e);

                newSub.substanceClass = oldAlt.substanceClass;
                fieldGetter[newSub.substanceClass].forEach(function (x) {
          if(newSub[x]){
                    delete newSub[x];
          }
                });
                fieldGetter[oldAlt.substanceClass].forEach(function (x) {
          if(oldAlt[x]){
                    newSub[x] = oldAlt[x];
          }
                });
                var subReferences = JSON.search(newSub, "//*[references]");
                var objectsA = subReferences.filter(function (e) {
                    if (_typeof2(e) === "object") {
                        return true;
                    } else {
                        return false;
                    }
                });
                var toPush = [];

                for (var i = 0; i < objectsA.length; i++) {
                    var current = objectsA[i].references;

                    for (var k = 0; k < current.length; k++) {
                        for (var l = 0; l < oldAlt.references.length; l++) {
                            if (oldAlt.references[l].uuid == current[k]) {
                                var replace = guid();
                                current[k] = replace;
                                oldAlt.references[l].uuid = replace;
                                toPush.push(oldAlt.references[l]);
                            }
                        }
                    }
                }

                toPush.forEach(function (ref) {
                    newSub.references.push(ref);
                });
                newSub.references = newSub.references.filter(function (r) {
                    if (r.uuid != uuidNew) {
                        return r;
                    }
                });
                updateRecord(newSub, function () {
                    location.reload();
                }, 3);
            });
        });
    };

    var updateRecord = function updateRecord(nsub, cb, step) {
        console.log('SENDING THE FOLLOWING DATA FOR STEP ' + step);
        console.log(nsub);
        $.ajax({
            url: '/ginas/app/api/v1/substances',
            contentType: "application/json",
            type: 'PUT',
            data: JSON.stringify(nsub),
            success: function success(data) {
                console.log('SUCCESS ON STEP ' + step + '. Response data:');
                console.log(data);

                if (step == 3) {
                    hideLoadingWheel();
                    setTimeout(function () {
            alert('Record definitions successfully switched. The page will now refresh. \n\n Please review and remove any unnecessary validation Notes created for each substance during the switch');
                        cb();
                    }, 1000);
                } else {
                    cb();
                }
            },
            error: function error(data) {
                $('.loading').finish();
                $('.loading').hide();
                console.log('FAILURE - SERVER RESPONSE');
                console.log(data);
                console.log('SENT SUBSTANCE');
                console.log(nsub);
                var errorString = "VALIDATION ERROR - \n\n";
                data.responseJSON.validationMessages.forEach(function (e) {
                    if (e.messageType == "ERROR") {
                        errorString += e.message + "\n\n";
                    }
                });
                alert(errorString);
                undo(step);
            }
        });
    };

    var undo = function undo(step) {
        var text = "";

        if (step == 1) {
            text = '<h3>There was a problem changing the primary definition.</h3> No changes were made to either record';
        } else if (step == 2 && didStep5 == false || step == '2b') {
            text = '<h3>There was a problem changing the substance definition.</h3><br/> <b>To Restore the records:</b><br/><ul><li>  Go to <a target = "_blank" href = "app/substance/' + sub.uuid + '/v/1#history" >the earliest alternative details history</a> and restore version ' + altVersion + '</a> and click "restore"<br/>If that version is not available, go to the <a target = "_blank" href = "app/substance/' + sub.uuid + '#history" > current version substance history card </a> and select a previous version to restore </li></ul>';
        } else {
            text = '<h3>There was a problem updating the new definition.</h3><br/> <b>To Restore the records:</b><br/><ul><li> Go to <a target = "_blank" href = "app/substance/' + alt.uuid + '/v/1#history" >the earliest alternative details history</a> and restore version ' + altVersion + '<br/>If that version is not available, go to the <a target = "_blank" href = "app/substance/' + alt.uuid + '#history" > current version substance history card </a> and select a previous version to restore </li><li> Go to <a target = "_blank" href = "app/substance/' + sub.uuid + '/v/1#history" > the earliest primary version and restore version ' + primeVersion + ' <br/>If that version is not available, go to the <a target = "_blank" href = "app/substance/' + sub.uuid + '#history" >current version </a> and select a previous version to restore ';
        }

        document.body.appendChild($("<div class=\"classchng-overlay\" style=\"z-index:999999;position:fixed;top:0px;width:100%;height:100%;background: rgba(0, 0, 0, 0.6);\">\n               <div style=\"padding: 100px;content-align:center\">\n                <div style=\" background-color:white;max-width:600px;padding:20px;margin:auto;line-height: 24px;\">\n                \t<div>" + text + "</div>\n                  <br/>\n                  <div style = 'float:right'><button class=\"btn btn-danger classchng-cancel\">Close</button></div><br/><br/>\n                </div>\n                </div>\n                </div>\n                ")[0]);
        $(".classchng-cancel").click(function (e) {
            $(".classchng-overlay").remove();
        });
    };

    function guid() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
        }

        return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
    }

    function getJson() {
        return angular.element(document.body).scope().substance.$$flattenSubstance();
    }

    function showLoadingWheel() {
        $('.loading').show();
        $('.loading').animate({
            'opacity': 1
        }, 200, function () {});
    }

    function hideLoadingWheel() {
        $('.loading').finish();
        $('.loading').hide();
    }
}