(window.webpackJsonp=window.webpackJsonp||[]).push([[22],{EfhQ:function(l,n,t){"use strict";t.d(n,"a",(function(){return e}));var u=t("CcnG"),e=function(){return function(){this.countUpdate=new u.n}}()},eUdr:function(l,n,t){"use strict";t.r(n);var u=t("CcnG"),e=function(){return function(){}}(),i=t("YBVn"),o=t("6E2U"),a=t("Ip0R"),s=t("mrSG"),c=t("EfhQ"),r=t("K9Ia"),b=t("SX2y"),m=function(l){function n(n,t,u,e,i){var o=l.call(this)||this;return o.utilService=n,o.overlayContainerService=t,o.structureService=u,o.dialog=e,o.configService=i,o.moieties=[],o.substanceUpdated=new r.a,o.rounding="1.0-2",o}return s.a(n,l),n.prototype.ngOnInit=function(){var l=this;this.substanceUpdated.subscribe((function(n){l.substance=n,null!=l.substance&&null!=l.substance.moieties&&(l.moieties=l.substance.moieties,l.moieties.forEach((function(n){n.formula=l.structureService.formatFormula(n)}))),l.countUpdate.emit(l.moieties.length)})),this.overlayContainer=this.overlayContainerService.getContainerElement(),this.configService.configData&&this.configService.configData.molWeightRounding&&(this.rounding="1.0-"+this.configService.configData.molWeightRounding)},n.prototype.openImageModal=function(l){var n=this,t=this.dialog.open(b.a,{height:"90%",width:"650px",panelClass:"structure-image-panel",data:{structure:l.uuid}});this.overlayContainer.style.zIndex="1002";var u=t.afterClosed().subscribe((function(){n.overlayContainer.style.zIndex=null,u.unsubscribe()}),(function(){n.overlayContainer.style.zIndex=null,u.unsubscribe()}))},n}(c.a),d=t("eDkP"),v=t("LNDF"),f=t("o3x0"),g=t("EdIQ"),x=u.rb({encapsulation:0,styles:[[".structure-image[_ngcontent-%COMP%]{width:100%;height:auto}.moiety[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:horizontal;-webkit-box-direction:normal;-ms-flex-direction:row;flex-direction:row;padding:20px 0;-webkit-box-align:center;-ms-flex-align:center;align-items:center}.moiety[_ngcontent-%COMP%]:not(:last-child){border-bottom:1px solid rgba(0,0,0,.12)}.zoom[_ngcontent-%COMP%]:hover{cursor:-webkit-zoom-in;cursor:zoom-in}.moiety-image[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;-ms-flex-preferred-size:33.333333%;flex-basis:33.333333%;-ms-flex-negative:0;flex-shrink:0}.moiety-details-container[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-flex:2;-ms-flex-positive:2;flex-grow:2;-ms-flex-preferred-size:66.6%;flex-basis:66.6%;-ms-flex-negative:0;flex-shrink:0}.moiety-details-container[_ngcontent-%COMP%]   .moiety-details[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;-ms-flex-preferred-size:50%;flex-basis:50%;-ms-flex-negative:0;flex-shrink:0}.moiety-details-container[_ngcontent-%COMP%]   .moiety-details[_ngcontent-%COMP%]:first-child{padding-right:5px}.moiety-details-container[_ngcontent-%COMP%]   .moiety-details[_ngcontent-%COMP%]:last-child{padding-left:5px}@media (max-width:900px){.moiety[_ngcontent-%COMP%]{-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column;-webkit-box-align:start;-ms-flex-align:start;align-items:flex-start}.moiety[_ngcontent-%COMP%]   .moiety-image[_ngcontent-%COMP%]{width:100%;-webkit-box-ordinal-group:3;-ms-flex-order:2;order:2;text-align:center}.moiety[_ngcontent-%COMP%]   .moiety-image[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{max-width:400px}.moiety[_ngcontent-%COMP%]   .moiety-details-container[_ngcontent-%COMP%]{-webkit-box-ordinal-group:2;-ms-flex-order:1;order:1;width:100%}}@media (max-width:615px){.moiety[_ngcontent-%COMP%]   .moiety-details-container[_ngcontent-%COMP%]{-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column}.moiety[_ngcontent-%COMP%]   .moiety-details-container[_ngcontent-%COMP%]   .moiety-details[_ngcontent-%COMP%]:first-child{margin-bottom:20px}}"]],data:{}});function p(l){return u.Pb(0,[(l()(),u.tb(0,0,null,null,4,"div",[],null,null,null,null,null)),(l()(),u.tb(1,0,null,null,1,"div",[],null,null,null,null,null)),(l()(),u.Nb(2,null,["",""])),(l()(),u.tb(3,0,null,null,1,"div",[],null,null,null,null,null)),(l()(),u.Nb(4,null,[""," "," (average)"]))],null,(function(l,n){l(n,2,0,n.parent.context.$implicit.countAmount.type),l(n,4,0,n.parent.context.$implicit.countAmount.average,n.parent.context.$implicit.countAmount.units)}))}function y(l){return u.Pb(0,[(l()(),u.tb(0,0,null,null,53,"div",[["class","moiety"]],null,null,null,null,null)),(l()(),u.tb(1,0,null,null,3,"div",[["class","moiety-image"]],null,null,null,null,null)),(l()(),u.tb(2,0,null,null,2,"a",[["class","zoom"]],null,[[null,"click"]],(function(l,n,t){var u=!0;return"click"===n&&(u=!1!==l.component.openImageModal(l.context.$implicit)&&u),u}),null,null)),(l()(),u.tb(3,0,null,null,1,"img",[["appSubstanceImage",""],["class","structure-image"],["stereo","true"]],null,null,null,null,null)),u.sb(4,4210688,null,0,i.a,[u.k,o.a],{entityId:[0,"entityId"],stereo:[1,"stereo"]},null),(l()(),u.tb(5,0,null,null,48,"div",[["class","moiety-details-container"]],null,null,null,null,null)),(l()(),u.tb(6,0,null,null,21,"div",[["class","moiety-details"]],null,null,null,null,null)),(l()(),u.tb(7,0,null,null,3,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),u.tb(8,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),u.Nb(-1,null,["Molecular Formula:"])),(l()(),u.tb(10,0,null,null,0,"div",[["class","value"]],[[8,"innerHTML",1]],null,null,null,null)),(l()(),u.tb(11,0,null,null,5,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),u.tb(12,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),u.Nb(-1,null,["Molecular Weight:"])),(l()(),u.tb(14,0,null,null,2,"div",[["class","value"]],null,null,null,null,null)),(l()(),u.Nb(15,null,["",""])),u.Jb(16,2),(l()(),u.tb(17,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),u.tb(18,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),u.Nb(-1,null,["Charge:"])),(l()(),u.tb(20,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),u.Nb(21,null,["",""])),(l()(),u.tb(22,0,null,null,5,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),u.tb(23,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),u.Nb(-1,null,["Count:"])),(l()(),u.tb(25,0,null,null,2,"div",[["class","value"]],null,null,null,null,null)),(l()(),u.jb(16777216,null,null,1,null,p)),u.sb(27,16384,null,0,a.n,[u.P,u.M],{ngIf:[0,"ngIf"]},null),(l()(),u.tb(28,0,null,null,25,"div",[["class","moiety-details"]],null,null,null,null,null)),(l()(),u.tb(29,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),u.tb(30,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),u.Nb(-1,null,["Stereochemistry:"])),(l()(),u.tb(32,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),u.Nb(33,null,["",""])),(l()(),u.tb(34,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),u.tb(35,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),u.Nb(-1,null,["Additional Stereochemistry:"])),(l()(),u.tb(37,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),u.Nb(38,null,["",""])),(l()(),u.tb(39,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),u.tb(40,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),u.Nb(-1,null,["Defined Stereocenters:"])),(l()(),u.tb(42,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),u.Nb(43,null,["","/",""])),(l()(),u.tb(44,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),u.tb(45,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),u.Nb(-1,null,["E/Z Centers:"])),(l()(),u.tb(47,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),u.Nb(48,null,["",""])),(l()(),u.tb(49,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),u.tb(50,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),u.Nb(-1,null,["Optical Activity:"])),(l()(),u.tb(52,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),u.Nb(53,null,["",""]))],(function(l,n){l(n,4,0,n.context.$implicit.uuid,"true"),l(n,27,0,n.context.$implicit.countAmount)}),(function(l,n){var t=n.component;l(n,10,0,n.context.$implicit.formula);var e=u.Ob(n,15,0,l(n,16,0,u.Fb(n.parent,0),n.context.$implicit.mwt,t.rounding));l(n,15,0,e),l(n,21,0,n.context.$implicit.charge),l(n,33,0,n.context.$implicit.stereochemistry),l(n,38,0,n.context.$implicit.atropisomerism),l(n,43,0,n.context.$implicit.stereoCenters,n.context.$implicit.definedStereo),l(n,48,0,n.context.$implicit.ezCenters),l(n,53,0,n.context.$implicit.opticalActivity)}))}function h(l){return u.Pb(0,[u.Hb(0,a.f,[u.u]),(l()(),u.jb(16777216,null,null,1,null,y)),u.sb(2,278528,null,0,a.m,[u.P,u.M,u.s],{ngForOf:[0,"ngForOf"]},null)],(function(l,n){l(n,2,0,n.component.moieties)}),null)}function C(l){return u.Pb(0,[(l()(),u.tb(0,0,null,null,1,"app-substance-moieties",[],null,null,null,h,x)),u.sb(1,114688,null,0,m,[o.a,d.e,v.a,f.e,g.a],null,null)],(function(l,n){l(n,1,0)}),null)}var w=u.pb("app-substance-moieties",m,C,{},{countUpdate:"countUpdate"},[]),M=t("EtvR"),P=t("5uHe"),O=t("ZYCi"),k=t("6jyQ");t.d(n,"SubstanceMoietiesModuleNgFactory",(function(){return _}));var _=u.qb(e,[],(function(l){return u.Cb([u.Db(512,u.j,u.bb,[[8,[w]],[3,u.j],u.x]),u.Db(4608,a.p,a.o,[u.u,[2,a.H]]),u.Db(1073742336,a.c,a.c,[]),u.Db(1073742336,M.a,M.a,[]),u.Db(1073742336,P.a,P.a,[]),u.Db(1073742336,e,e,[]),u.Db(1024,O.j,(function(){return[[]]}),[]),u.Db(256,k.a,m,[])])}))}}]);
//# sourceMappingURL=22.095a7c68cfd8157090a2.js.map