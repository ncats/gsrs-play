(window.webpackJsonp=window.webpackJsonp||[]).push([[34],{EfhQ:function(l,n,u){"use strict";u.d(n,"a",function(){return e});var t=u("CcnG"),e=function(){return function(){this.countUpdate=new t.o}}()},eUdr:function(l,n,u){"use strict";u.r(n);var t=u("CcnG"),e=function(){},i=u("Ip0R"),a=u("mrSG"),c=u("EfhQ"),o=u("6E2U"),s=u("K9Ia"),m=function(l){function n(n){var u=l.call(this)||this;return u.utilService=n,u.moieties=[],u.substanceUpdated=new s.a,u}return Object(a.a)(n,l),n.prototype.ngOnInit=function(){var l=this;this.substanceUpdated.subscribe(function(n){l.substance=n,null!=l.substance&&null!=l.substance.moieties&&(l.moieties=l.substance.moieties),l.countUpdate.emit(l.moieties.length)})},n.prototype.getSafeStructureImgUrl=function(l,n){return void 0===n&&(n=150),this.utilService.getSafeStructureImgUrl(l,n)},n}(c.a),r=t.Sa({encapsulation:0,styles:[[".structure-image[_ngcontent-%COMP%]{width:100%;height:auto}.moiety[_ngcontent-%COMP%]{display:flex;flex-direction:row;padding:20px 0;align-items:center}.moiety[_ngcontent-%COMP%]:not(:last-child){border-bottom:1px solid rgba(0,0,0,.12)}.moiety-image[_ngcontent-%COMP%]{flex-grow:1;flex-basis:33.333333%;flex-shrink:0}.moiety-details-container[_ngcontent-%COMP%]{display:flex;flex-grow:2;flex-basis:66.6%;flex-shrink:0}.moiety-details-container[_ngcontent-%COMP%]   .moiety-details[_ngcontent-%COMP%]{flex-grow:1;flex-basis:50%;flex-shrink:0}.moiety-details-container[_ngcontent-%COMP%]   .moiety-details[_ngcontent-%COMP%]:first-child{padding-right:5px}.moiety-details-container[_ngcontent-%COMP%]   .moiety-details[_ngcontent-%COMP%]:last-child{padding-left:5px}@media (max-width:900px){.moiety[_ngcontent-%COMP%]{flex-direction:column;align-items:flex-start}.moiety[_ngcontent-%COMP%]   .moiety-image[_ngcontent-%COMP%]{width:100%;order:2;text-align:center}.moiety[_ngcontent-%COMP%]   .moiety-image[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{max-width:400px}.moiety[_ngcontent-%COMP%]   .moiety-details-container[_ngcontent-%COMP%]{order:1;width:100%}}@media (max-width:615px){.moiety[_ngcontent-%COMP%]   .moiety-details-container[_ngcontent-%COMP%]{flex-direction:column}.moiety[_ngcontent-%COMP%]   .moiety-details-container[_ngcontent-%COMP%]   .moiety-details[_ngcontent-%COMP%]:first-child{margin-bottom:20px}}"]],data:{}});function d(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,4,"div",[],null,null,null,null,null)),(l()(),t.Ua(1,0,null,null,1,"div",[],null,null,null,null,null)),(l()(),t.mb(2,null,["",""])),(l()(),t.Ua(3,0,null,null,1,"div",[],null,null,null,null,null)),(l()(),t.mb(4,null,["","\xa0","\xa0(average)"]))],null,function(l,n){l(n,2,0,n.parent.context.$implicit.countAmount.type),l(n,4,0,n.parent.context.$implicit.countAmount.average,n.parent.context.$implicit.countAmount.units)})}function v(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,51,"div",[["class","moiety"]],null,null,null,null,null)),(l()(),t.Ua(1,0,null,null,1,"div",[["class","moiety-image"]],null,null,null,null,null)),(l()(),t.Ua(2,0,null,null,0,"img",[["class","structure-image"]],[[8,"src",4]],null,null,null,null)),(l()(),t.Ua(3,0,null,null,48,"div",[["class","moiety-details-container"]],null,null,null,null,null)),(l()(),t.Ua(4,0,null,null,21,"div",[["class","moiety-details"]],null,null,null,null,null)),(l()(),t.Ua(5,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),t.Ua(6,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),t.mb(-1,null,["Molecular Formula:"])),(l()(),t.Ua(8,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),t.mb(9,null,["",""])),(l()(),t.Ua(10,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),t.Ua(11,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),t.mb(-1,null,["Molecular Weight:"])),(l()(),t.Ua(13,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),t.mb(14,null,["",""])),(l()(),t.Ua(15,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),t.Ua(16,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),t.mb(-1,null,["Charge:"])),(l()(),t.Ua(18,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),t.mb(19,null,["",""])),(l()(),t.Ua(20,0,null,null,5,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),t.Ua(21,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),t.mb(-1,null,["Count:"])),(l()(),t.Ua(23,0,null,null,2,"div",[["class","value"]],null,null,null,null,null)),(l()(),t.Ma(16777216,null,null,1,null,d)),t.Ta(25,16384,null,0,i.l,[t.U,t.R],{ngIf:[0,"ngIf"]},null),(l()(),t.Ua(26,0,null,null,25,"div",[["class","moiety-details"]],null,null,null,null,null)),(l()(),t.Ua(27,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),t.Ua(28,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),t.mb(-1,null,["Stereochemistry:"])),(l()(),t.Ua(30,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),t.mb(31,null,["",""])),(l()(),t.Ua(32,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),t.Ua(33,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),t.mb(-1,null,["Additional Stereochemistry:"])),(l()(),t.Ua(35,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),t.mb(36,null,["",""])),(l()(),t.Ua(37,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),t.Ua(38,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),t.mb(-1,null,["Defined Stereocenters:"])),(l()(),t.Ua(40,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),t.mb(41,null,["","/",""])),(l()(),t.Ua(42,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),t.Ua(43,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),t.mb(-1,null,["E/Z Centers:"])),(l()(),t.Ua(45,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),t.mb(46,null,["",""])),(l()(),t.Ua(47,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),t.Ua(48,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),t.mb(-1,null,["Optical Activity:"])),(l()(),t.Ua(50,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),t.mb(51,null,["",""]))],function(l,n){l(n,25,0,n.context.$implicit.countAmount)},function(l,n){l(n,2,0,n.component.getSafeStructureImgUrl(n.context.$implicit.uuid)),l(n,9,0,n.context.$implicit.formula),l(n,14,0,n.context.$implicit.mwt),l(n,19,0,n.context.$implicit.charge),l(n,31,0,n.context.$implicit.stereochemistry),l(n,36,0,n.context.$implicit.atropisomerism),l(n,41,0,n.context.$implicit.stereoCenters,n.context.$implicit.definedStereo),l(n,46,0,n.context.$implicit.ezCenters),l(n,51,0,n.context.$implicit.opticalActivity)})}function g(l){return t.ob(0,[(l()(),t.Ma(16777216,null,null,1,null,v)),t.Ta(1,278528,null,0,i.k,[t.U,t.R,t.w],{ngForOf:[0,"ngForOf"]},null)],function(l,n){l(n,1,0,n.component.moieties)},null)}var p=t.Qa("app-substance-moieties",m,function(l){return t.ob(0,[(l()(),t.Ua(0,0,null,null,1,"app-substance-moieties",[],null,null,null,g,r)),t.Ta(1,114688,null,0,m,[o.a],null,null)],function(l,n){l(n,1,0)},null)},{},{countUpdate:"countUpdate"},[]),b=u("EtvR"),f=u("ZYCi"),U=u("6jyQ");u.d(n,"SubstanceMoietiesModuleNgFactory",function(){return y});var y=t.Ra(e,[],function(l){return t.bb([t.cb(512,t.k,t.Ha,[[8,[p]],[3,t.k],t.B]),t.cb(4608,i.n,i.m,[t.y,[2,i.y]]),t.cb(1073742336,i.c,i.c,[]),t.cb(1073742336,b.a,b.a,[]),t.cb(1073742336,e,e,[]),t.cb(1024,f.k,function(){return[[]]},[]),t.cb(256,U.a,m,[])])})}}]);