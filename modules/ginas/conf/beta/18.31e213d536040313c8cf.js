(window.webpackJsonp=window.webpackJsonp||[]).push([[18],{"n67+":function(n,l,t){"use strict";t.d(l,"a",(function(){return o}));var e=t("CcnG"),o=function(){return function(){this.menuLabelUpdate=new e.n,this.hiddenStateUpdate=new e.n,this.canAddItemUpdate=new e.n,this.componentDestroyed=new e.n}}()},scpu:function(n,l,t){"use strict";t.r(l);var e=t("CcnG"),o=function(){return function(){}}(),i=t("NcP4"),a=t("t68o"),u=t("pMnS"),c=t("+lnl"),r=t("EJ7M"),b=t("ap0P"),s=t("HE/B"),d=t("ThfK"),p=t("ldJ0"),m=t("OvbY"),f=t("Ok+c"),g=t("Pj+I"),x=t("Cka/"),h=t("UMU1"),v=t("dCG0"),P=t("B/2v"),y=t("S1Kd"),M=t("4z0a"),_=t("nFVu"),w=t("TtEo"),C=t("LC5p"),F=t("xZkp"),k=t("hifq"),O=t("bujt"),D=t("UodH"),I=t("lLAP"),$=t("wFw1"),j=t("Mr+X"),L=t("SMsm"),S=t("Ip0R"),N=t("s7Fu"),T=t("khmc"),U=t("YLZ7"),z=t("o3x0"),A=t("6E2U"),q=t("eDkP"),E=t("4S5B"),R=t("Vurf"),G=t("6UMx"),V=t("0/Q6"),Y=t("dJrM"),H=t("seP3"),J=t("Wf4p"),K=t("Fzqc"),Z=t("dWZg"),B=t("gIcY"),W=t("b716"),X=t("/VYK"),Q=t("o6iZ"),nn=function(){function n(n,l,t,o,i){this.cvService=n,this.dialog=l,this.utilsService=t,this.overlayContainerService=o,this.substanceFormService=i,this.modDeleted=new e.n,this.modTypeList=[],this.modRoleList=[],this.modProcessList=[],this.subscriptions=[]}return n.prototype.ngOnInit=function(){this.getVocabularies(),this.overlayContainer=this.overlayContainerService.getContainerElement()},Object.defineProperty(n.prototype,"mod",{get:function(){return this.privateMod},set:function(n){this.privateMod=n},enumerable:!0,configurable:!0}),n.prototype.getVocabularies=function(){var n=this;this.cvService.getDomainVocabulary("PHYSICAL_MODIFICATION_ROLE").subscribe((function(l){n.modRoleList=l.PHYSICAL_MODIFICATION_ROLE.list}))},n.prototype.deleteMod=function(){var n=this;this.privateMod.$$deletedCode=this.utilsService.newUUID(),this.privateMod||(this.deleteTimer=setTimeout((function(){n.modDeleted.emit(n.mod),n.substanceFormService.emitOtherLinkUpdate()}),1e3))},n.prototype.undoDelete=function(){clearTimeout(this.deleteTimer),delete this.privateMod.$$deletedCode},n.prototype.openParameterDialog=function(){var n=this;this.mod.parameters||(this.mod.parameters=[]);var l=this.dialog.open(Q.a,{data:this.mod.parameters,width:"1080px"});this.overlayContainer.style.zIndex="1002";var t=l.afterClosed().subscribe((function(l){n.overlayContainer.style.zIndex=null,l&&(n.mod.parameters=l)}));this.subscriptions.push(t)},n.prototype.openPropertyParameter=function(n){var l,t=this;null==n&&(l=!0,n={amount:{}});var e=JSON.stringify(n),o=this.dialog.open(Q.a,{data:JSON.parse(e),width:"1200px"});this.overlayContainer.style.zIndex="1002",o.afterClosed().subscribe((function(e){t.overlayContainer.style.zIndex=null,null!=e&&(null==t.mod.parameters&&(t.mod.parameters=[]),l?t.mod.parameters.unshift(e):Object.keys(e).forEach((function(l){n[l]=e[l]})))}))},n.prototype.deleteParameter=function(n){this.mod.parameters.splice(n,1)},n.prototype.displayAmount=function(n){return this.utilsService.displayAmount(n)},n}(),ln=t("Jj5M"),tn=e.rb({encapsulation:0,styles:[['.physical-form-container[_ngcontent-%COMP%]{padding:30px 10px 12px;position:relative}.form-sub-row[_ngcontent-%COMP%]{max-width:90%}.related-substance[_ngcontent-%COMP%]{max-width:175px}.related-substance[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{max-width:150px}.form-row[_ngcontent-%COMP%]   .code-system[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .code-system-type[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .type[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;padding-right:15px}.form-row[_ngcontent-%COMP%]   .code-text[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .url[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1}.key-value-pair[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column;-ms-flex-item-align:start;align-self:flex-start}.key-value-pair[_ngcontent-%COMP%]   .key[_ngcontent-%COMP%]{font-size:11px;padding-bottom:3.5px;line-height:11px;color:rgba(0,0,0,.54);font-weight:400;font-family:Roboto,"Helvetica Neue",sans-serif}.key-value-pair[_ngcontent-%COMP%]   .value[_ngcontent-%COMP%]{font-size:15.5px}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;padding:0 10px;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.form-actions[_ngcontent-%COMP%]{-webkit-box-pack:start;-ms-flex-pack:start;justify-content:flex-start;margin:5px 0 10px}.form-content[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1}.group-access[_ngcontent-%COMP%], .location-type[_ngcontent-%COMP%], .mod-type[_ngcontent-%COMP%], .sites[_ngcontent-%COMP%]{width:33%}.amount[_ngcontent-%COMP%], .extent[_ngcontent-%COMP%]{width:40%}.group[_ngcontent-%COMP%]{width:75px}.type[_ngcontent-%COMP%]{max-width:225px}.access[_ngcontent-%COMP%]{width:30%}.name-form-container[_ngcontent-%COMP%]{padding:30px 10px 12px;position:relative}.notification-backdrop[_ngcontent-%COMP%]{position:absolute;top:0;right:0;bottom:0;left:0;display:-webkit-box;display:-ms-flexbox;display:flex;z-index:10;background-color:rgba(255,255,255,.8);-webkit-box-pack:center;-ms-flex-pack:center;justify-content:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;font-size:30px;font-weight:700;color:#666}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.form-row[_ngcontent-%COMP%]   .delete-container[_ngcontent-%COMP%]{padding:0 10px 8px 0}.form-row[_ngcontent-%COMP%]   .checkbox-container[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .radio-container[_ngcontent-%COMP%]{padding-bottom:18px;padding-right:15px}.form-row[_ngcontent-%COMP%]   .amount[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .domains[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .name[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .param-display[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .tags[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .type[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;padding-right:15px}.form-row[_ngcontent-%COMP%]   .jurisdiction[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1}.nameorgs-container[_ngcontent-%COMP%], .references-container[_ngcontent-%COMP%]{width:100%}.column-radio[_ngcontent-%COMP%]     .mat-radio-label{-webkit-box-orient:vertical;-webkit-box-direction:reverse;-ms-flex-direction:column-reverse;flex-direction:column-reverse}.column-radio[_ngcontent-%COMP%]     .mat-radio-label-content{padding-left:0;font-size:11px;padding-bottom:4px;color:rgba(0,0,0,.54);font-weight:400;font-family:Roboto,"Helvetica Neue",sans-serif}.column-checkbox[_ngcontent-%COMP%]     .mat-checkbox-layout{-webkit-box-orient:vertical;-webkit-box-direction:reverse;-ms-flex-direction:column-reverse;flex-direction:column-reverse;-webkit-box-align:center;-ms-flex-align:center;align-items:center}.column-checkbox[_ngcontent-%COMP%]     .mat-checkbox-inner-container{margin-right:unset;margin-left:unset}.column-checkbox[_ngcontent-%COMP%]     .mat-checkbox-layout .mat-checkbox-label{padding-left:0;font-size:11px;padding-bottom:2px;color:rgba(0,0,0,.54);font-weight:400;font-family:Roboto,"Helvetica Neue",sans-serif}.amount[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;padding-bottom:10px}.amt-label[_ngcontent-%COMP%]{padding-top:11px}.param-display[_ngcontent-%COMP%]{padding-top:10px;width:100%;display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column}.param-container[_ngcontent-%COMP%]{width:100%;display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end;padding-bottom:7px;padding-top:7px}.param-container[_ngcontent-%COMP%]   .param-display[_ngcontent-%COMP%]{max-width:40%;padding-right:15px}.param-container[_ngcontent-%COMP%]   .param-amount[_ngcontent-%COMP%]{max-width:60%}']],data:{}});function en(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,5,"div",[["class","notification-backdrop"]],null,null,null,null,null)),(n()(),e.Nb(-1,null,[" Deleted  "])),(n()(),e.tb(2,0,null,null,3,"button",[["mat-icon-button",""],["matTooltip","Undo"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,l,t){var e=!0;return"click"===l&&(e=!1!==n.component.undoDelete()&&e),e}),O.d,O.b)),e.sb(3,180224,null,0,D.b,[e.k,I.h,[2,$.a]],null,null),(n()(),e.tb(4,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","undo"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,j.b,j.a)),e.sb(5,9158656,null,0,L.b,[e.k,L.d,[8,null],[2,L.a],[2,e.m]],{svgIcon:[0,"svgIcon"]},null)],(function(n,l){n(l,5,0,"undo")}),(function(n,l){n(l,2,0,e.Fb(l,3).disabled||null,"NoopAnimations"===e.Fb(l,3)._animationMode),n(l,4,0,e.Fb(l,5).inline,"primary"!==e.Fb(l,5).color&&"accent"!==e.Fb(l,5).color&&"warn"!==e.Fb(l,5).color)}))}function on(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(1,null,["  -   ","   "," "]))],null,(function(n,l){n(l,1,0,l.parent.parent.context.$implicit.amount.average,l.parent.parent.context.$implicit.amount.units)}))}function an(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(-1,null,["   - "]))],null,null)}function un(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(-1,null,[" > "]))],null,null)}function cn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(-1,null,[" < "]))],null,null)}function rn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(1,null,[" "," "]))],null,(function(n,l){n(l,1,0,l.parent.parent.parent.context.$implicit.amount.low)}))}function bn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(-1,null,["  to  "]))],null,null)}function sn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(1,null,[" "," "]))],null,(function(n,l){n(l,1,0,l.parent.parent.parent.context.$implicit.amount.high)}))}function dn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(1,null,["   ","   (average) "]))],null,(function(n,l){n(l,1,0,l.parent.parent.parent.context.$implicit.amount.units)}))}function pn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,16,"span",[],null,null,null,null,null)),(n()(),e.jb(16777216,null,null,1,null,an)),e.sb(2,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.Nb(-1,null,["   [ "])),(n()(),e.jb(16777216,null,null,1,null,un)),e.sb(5,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,cn)),e.sb(7,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,rn)),e.sb(9,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,bn)),e.sb(11,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,sn)),e.sb(13,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.Nb(-1,null,[" ] "])),(n()(),e.jb(16777216,null,null,1,null,dn)),e.sb(16,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(n,l){n(l,2,0,null==l.parent.parent.context.$implicit.amount.average||""==l.parent.parent.context.$implicit.amount.average),n(l,5,0,null==l.parent.parent.context.$implicit.amount.high||""==l.parent.parent.context.$implicit.amount.high),n(l,7,0,null==l.parent.parent.context.$implicit.amount.low||""==l.parent.parent.context.$implicit.amount.low),n(l,9,0,null!=l.parent.parent.context.$implicit.amount.low&&""!=l.parent.parent.context.$implicit.amount.low),n(l,11,0,null!=l.parent.parent.context.$implicit.amount.low&&""!=l.parent.parent.context.$implicit.amount.low&&null!=l.parent.parent.context.$implicit.amount.high&&""!=l.parent.parent.context.$implicit.amount.high),n(l,13,0,null!=l.parent.parent.context.$implicit.amount.high&&""!=l.parent.parent.context.$implicit.amount.high),n(l,16,0,null==l.parent.parent.context.$implicit.amount.average||""==l.parent.parent.context.$implicit.amount.average)}),null)}function mn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(-1,null,["   (average) "]))],null,null)}function fn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(-1,null,[" > "]))],null,null)}function gn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(-1,null,[" < "]))],null,null)}function xn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(1,null,[" "," "]))],null,(function(n,l){n(l,1,0,l.parent.parent.parent.context.$implicit.amount.lowLimit)}))}function hn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(-1,null,["  to  "]))],null,null)}function vn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(1,null,[" "," "]))],null,(function(n,l){n(l,1,0,l.parent.parent.parent.context.$implicit.amount.highLimit)}))}function Pn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,12,"span",[],null,null,null,null,null)),(n()(),e.Nb(-1,null,["  -   [ "])),(n()(),e.jb(16777216,null,null,1,null,fn)),e.sb(3,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,gn)),e.sb(5,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,xn)),e.sb(7,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,hn)),e.sb(9,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,vn)),e.sb(11,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.Nb(-1,null,[" ]  (limits) "]))],(function(n,l){n(l,3,0,null==l.parent.parent.context.$implicit.amount.highLimit||""==l.parent.parent.context.$implicit.amount.highLimit),n(l,5,0,null==l.parent.parent.context.$implicit.amount.lowLimit||""==l.parent.parent.context.$implicit.amount.lowLimit),n(l,7,0,null!=l.parent.parent.context.$implicit.amount.lowLimit&&""!=l.parent.parent.context.$implicit.amount.lowLimit),n(l,9,0,null!=l.parent.parent.context.$implicit.amount.lowLimit&&""!=l.parent.parent.context.$implicit.amount.lowLimit&&null!=l.parent.parent.context.$implicit.amount.highLimit&&""!=l.parent.parent.context.$implicit.amount.highLimit),n(l,11,0,null!=l.parent.parent.context.$implicit.amount.highLimit&&""!=l.parent.parent.context.$implicit.amount.highLimit)}),null)}function yn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(1,null,["  -  "," "]))],null,(function(n,l){n(l,1,0,l.parent.parent.context.$implicit.amount.nonNumericValue)}))}function Mn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,12,"span",[],null,null,null,null,null)),(n()(),e.tb(1,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(2,null,["   "," "])),(n()(),e.jb(16777216,null,null,1,null,on)),e.sb(4,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,pn)),e.sb(6,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,mn)),e.sb(8,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,Pn)),e.sb(10,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,yn)),e.sb(12,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(n,l){n(l,4,0,null!=l.parent.context.$implicit.amount.average&&""!=l.parent.context.$implicit.amount.average),n(l,6,0,null!=l.parent.context.$implicit.amount.low&&""!=l.parent.context.$implicit.amount.low||null!=l.parent.context.$implicit.amount.high&&""!=l.parent.context.$implicit.amount.high),n(l,8,0,null!=l.parent.context.$implicit.amount.average&&""!=l.parent.context.$implicit.amount.average),n(l,10,0,null!=l.parent.context.$implicit.amount.lowLimit&&""!=l.parent.context.$implicit.amount.lowLimit||null!=l.parent.context.$implicit.amount.highLimit&&""!=l.parent.context.$implicit.amount.highLimit),n(l,12,0,l.parent.context.$implicit.amount.nonNumericValue)}),(function(n,l){n(l,2,0,l.parent.context.$implicit.amount.type)}))}function _n(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,12,"div",[["mat-list-item",""]],null,null,null,null,null)),(n()(),e.tb(1,0,null,null,3,"button",[["class","parameter-delete-icon"],["mat-icon-button",""]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,l,t){var e=!0;return"click"===l&&(e=!1!==n.component.deleteParameter(n.context.index)&&e),e}),O.d,O.b)),e.sb(2,180224,null,0,D.b,[e.k,I.h,[2,$.a]],null,null),(n()(),e.tb(3,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","delete_forever"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,j.b,j.a)),e.sb(4,9158656,null,0,L.b,[e.k,L.d,[8,null],[2,L.a],[2,e.m]],{svgIcon:[0,"svgIcon"]},null),(n()(),e.tb(5,0,null,null,1,"span",[],null,null,null,null,null)),(n()(),e.Nb(6,null,["",""])),(n()(),e.jb(16777216,null,null,1,null,Mn)),e.sb(8,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.tb(9,0,null,null,3,"button",[["class","parameter-icon"],["mat-icon-button",""]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,l,t){var e=!0;return"click"===l&&(e=!1!==n.component.openPropertyParameter(n.context.$implicit)&&e),e}),O.d,O.b)),e.sb(10,180224,null,0,D.b,[e.k,I.h,[2,$.a]],null,null),(n()(),e.tb(11,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","edit"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,j.b,j.a)),e.sb(12,9158656,null,0,L.b,[e.k,L.d,[8,null],[2,L.a],[2,e.m]],{svgIcon:[0,"svgIcon"]},null)],(function(n,l){n(l,4,0,"delete_forever"),n(l,8,0,null!=l.context.$implicit.amount),n(l,12,0,"edit")}),(function(n,l){n(l,1,0,e.Fb(l,2).disabled||null,"NoopAnimations"===e.Fb(l,2)._animationMode),n(l,3,0,e.Fb(l,4).inline,"primary"!==e.Fb(l,4).color&&"accent"!==e.Fb(l,4).color&&"warn"!==e.Fb(l,4).color),n(l,6,0,l.context.$implicit.parameterName),n(l,9,0,e.Fb(l,10).disabled||null,"NoopAnimations"===e.Fb(l,10)._animationMode),n(l,11,0,e.Fb(l,12).inline,"primary"!==e.Fb(l,12).color&&"accent"!==e.Fb(l,12).color&&"warn"!==e.Fb(l,12).color)}))}function wn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,44,"div",[["class","physical-form-container"]],null,null,null,null,null)),(n()(),e.jb(16777216,null,null,1,null,en)),e.sb(2,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.tb(3,0,null,null,41,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),e.tb(4,0,null,null,4,"div",[["class","delete-container"]],null,null,null,null,null)),(n()(),e.tb(5,0,null,null,3,"button",[["mat-icon-button",""],["matTooltip","Delete name"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,l,t){var e=!0;return"click"===l&&(e=!1!==n.component.deleteMod()&&e),e}),O.d,O.b)),e.sb(6,180224,null,0,D.b,[e.k,I.h,[2,$.a]],null,null),(n()(),e.tb(7,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","delete_forever"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,j.b,j.a)),e.sb(8,9158656,null,0,L.b,[e.k,L.d,[8,null],[2,L.a],[2,e.m]],{svgIcon:[0,"svgIcon"]},null),(n()(),e.tb(9,0,null,null,1,"app-cv-input",[["class","type"],["title","Modification Role"]],null,[[null,"valueChange"]],(function(n,l,t){var e=!0;return"valueChange"===l&&(e=!1!==(n.component.mod.physicalModificationRole=t)&&e),e}),N.b,N.a)),e.sb(10,245760,null,0,T.a,[U.a,z.e,A.a,q.e,E.a,R.a],{vocabulary:[0,"vocabulary"],title:[1,"title"],model:[2,"model"]},{valueChange:"valueChange"}),(n()(),e.tb(11,0,null,null,11,"div",[["class","amount"]],null,null,null,null,null)),(n()(),e.tb(12,0,null,null,1,"div",[["class","label amt-label"]],null,null,null,null,null)),(n()(),e.Nb(-1,null,[" Parameters "])),(n()(),e.tb(14,0,null,null,3,"button",[["mat-icon-button",""],["matTooltip","add / edit parameters"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,l,t){var e=!0;return"click"===l&&(e=!1!==n.component.openPropertyParameter()&&e),e}),O.d,O.b)),e.sb(15,180224,null,0,D.b,[e.k,I.h,[2,$.a]],null,null),(n()(),e.tb(16,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","add_circle_outline"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,j.b,j.a)),e.sb(17,9158656,null,0,L.b,[e.k,L.d,[8,null],[2,L.a],[2,e.m]],{svgIcon:[0,"svgIcon"]},null),(n()(),e.tb(18,0,null,null,4,"div",[["class","param-display"]],null,null,null,null,null)),(n()(),e.tb(19,0,null,null,3,"mat-list",[["class","mat-list mat-list-base"]],null,null,null,G.e,G.a)),e.sb(20,704512,null,0,V.a,[e.k],null,null),(n()(),e.jb(16777216,null,0,1,null,_n)),e.sb(22,278528,null,0,S.m,[e.P,e.M,e.s],{ngForOf:[0,"ngForOf"]},null),(n()(),e.tb(23,0,null,null,21,"div",[],null,null,null,null,null)),(n()(),e.tb(24,0,null,null,20,"mat-form-field",[["class","group mat-form-field"]],[[2,"mat-form-field-appearance-standard",null],[2,"mat-form-field-appearance-fill",null],[2,"mat-form-field-appearance-outline",null],[2,"mat-form-field-appearance-legacy",null],[2,"mat-form-field-invalid",null],[2,"mat-form-field-can-float",null],[2,"mat-form-field-should-float",null],[2,"mat-form-field-has-label",null],[2,"mat-form-field-hide-placeholder",null],[2,"mat-form-field-disabled",null],[2,"mat-form-field-autofilled",null],[2,"mat-focused",null],[2,"mat-accent",null],[2,"mat-warn",null],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[2,"_mat-animation-noopable",null]],null,null,Y.b,Y.a)),e.sb(25,7520256,null,9,H.c,[e.k,e.h,[2,J.j],[2,K.b],[2,H.a],Z.a,e.z,[2,$.a]],null,null),e.Lb(603979776,1,{_controlNonStatic:0}),e.Lb(335544320,2,{_controlStatic:0}),e.Lb(603979776,3,{_labelChildNonStatic:0}),e.Lb(335544320,4,{_labelChildStatic:0}),e.Lb(603979776,5,{_placeholderChild:0}),e.Lb(603979776,6,{_errorChildren:1}),e.Lb(603979776,7,{_hintChildren:1}),e.Lb(603979776,8,{_prefixChildren:1}),e.Lb(603979776,9,{_suffixChildren:1}),(n()(),e.tb(35,0,null,1,9,"input",[["class","mat-input-element mat-form-field-autofill-control"],["matInput",""],["name","group"],["placeholder","Group"],["required",""]],[[1,"required",0],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[2,"mat-input-server",null],[1,"id",0],[1,"placeholder",0],[8,"disabled",0],[8,"required",0],[1,"readonly",0],[1,"aria-describedby",0],[1,"aria-invalid",0],[1,"aria-required",0]],[[null,"ngModelChange"],[null,"input"],[null,"blur"],[null,"compositionstart"],[null,"compositionend"],[null,"focus"]],(function(n,l,t){var o=!0,i=n.component;return"input"===l&&(o=!1!==e.Fb(n,36)._handleInput(t.target.value)&&o),"blur"===l&&(o=!1!==e.Fb(n,36).onTouched()&&o),"compositionstart"===l&&(o=!1!==e.Fb(n,36)._compositionStart()&&o),"compositionend"===l&&(o=!1!==e.Fb(n,36)._compositionEnd(t.target.value)&&o),"blur"===l&&(o=!1!==e.Fb(n,43)._focusChanged(!1)&&o),"focus"===l&&(o=!1!==e.Fb(n,43)._focusChanged(!0)&&o),"input"===l&&(o=!1!==e.Fb(n,43)._onInput()&&o),"ngModelChange"===l&&(o=!1!==(i.mod.modificationGroup=t)&&o),o}),null,null)),e.sb(36,16384,null,0,B.d,[e.E,e.k,[2,B.a]],null,null),e.sb(37,16384,null,0,B.t,[],{required:[0,"required"]},null),e.Kb(1024,null,B.l,(function(n){return[n]}),[B.t]),e.Kb(1024,null,B.m,(function(n){return[n]}),[B.d]),e.sb(40,671744,null,0,B.r,[[8,null],[6,B.l],[8,null],[6,B.m]],{name:[0,"name"],model:[1,"model"]},{update:"ngModelChange"}),e.Kb(2048,null,B.n,null,[B.r]),e.sb(42,16384,null,0,B.o,[[4,B.n]],null,null),e.sb(43,999424,null,0,W.b,[e.k,Z.a,[6,B.n],[2,B.q],[2,B.j],J.d,[8,null],X.a,e.z],{placeholder:[0,"placeholder"],required:[1,"required"]},null),e.Kb(2048,[[1,4],[2,4]],H.d,null,[W.b])],(function(n,l){var t=l.component;n(l,2,0,t.mod.$$deletedCode),n(l,8,0,"delete_forever"),n(l,10,0,t.modRoleList,"Modification Role",t.mod.physicalModificationRole),n(l,17,0,"add_circle_outline"),n(l,22,0,t.mod.parameters),n(l,37,0,""),n(l,40,0,"group",t.mod.modificationGroup),n(l,43,0,"Group","")}),(function(n,l){n(l,5,0,e.Fb(l,6).disabled||null,"NoopAnimations"===e.Fb(l,6)._animationMode),n(l,7,0,e.Fb(l,8).inline,"primary"!==e.Fb(l,8).color&&"accent"!==e.Fb(l,8).color&&"warn"!==e.Fb(l,8).color),n(l,14,0,e.Fb(l,15).disabled||null,"NoopAnimations"===e.Fb(l,15)._animationMode),n(l,16,0,e.Fb(l,17).inline,"primary"!==e.Fb(l,17).color&&"accent"!==e.Fb(l,17).color&&"warn"!==e.Fb(l,17).color),n(l,24,1,["standard"==e.Fb(l,25).appearance,"fill"==e.Fb(l,25).appearance,"outline"==e.Fb(l,25).appearance,"legacy"==e.Fb(l,25).appearance,e.Fb(l,25)._control.errorState,e.Fb(l,25)._canLabelFloat,e.Fb(l,25)._shouldLabelFloat(),e.Fb(l,25)._hasFloatingLabel(),e.Fb(l,25)._hideControlPlaceholder(),e.Fb(l,25)._control.disabled,e.Fb(l,25)._control.autofilled,e.Fb(l,25)._control.focused,"accent"==e.Fb(l,25).color,"warn"==e.Fb(l,25).color,e.Fb(l,25)._shouldForward("untouched"),e.Fb(l,25)._shouldForward("touched"),e.Fb(l,25)._shouldForward("pristine"),e.Fb(l,25)._shouldForward("dirty"),e.Fb(l,25)._shouldForward("valid"),e.Fb(l,25)._shouldForward("invalid"),e.Fb(l,25)._shouldForward("pending"),!e.Fb(l,25)._animationsEnabled]),n(l,35,1,[e.Fb(l,37).required?"":null,e.Fb(l,42).ngClassUntouched,e.Fb(l,42).ngClassTouched,e.Fb(l,42).ngClassPristine,e.Fb(l,42).ngClassDirty,e.Fb(l,42).ngClassValid,e.Fb(l,42).ngClassInvalid,e.Fb(l,42).ngClassPending,e.Fb(l,43)._isServer,e.Fb(l,43).id,e.Fb(l,43).placeholder,e.Fb(l,43).disabled,e.Fb(l,43).required,e.Fb(l,43).readonly&&!e.Fb(l,43)._isNativeSelect||null,e.Fb(l,43)._ariaDescribedby||null,e.Fb(l,43).errorState,e.Fb(l,43).required.toString()])}))}var Cn=t("mrSG"),Fn=function(n){function l(l,t,e){var o=n.call(this,e)||this;return o.substanceFormPhysicalModificationsService=l,o.scrollToService=t,o.gaService=e,o.subscriptions=[],o.analyticsEventCategory="substance form agent modifications",o}return Cn.a(l,n),l.prototype.ngOnInit=function(){this.canAddItemUpdate.emit(!0),this.menuLabelUpdate.emit("Physical Modifications")},l.prototype.ngAfterViewInit=function(){var n=this,l=this.substanceFormPhysicalModificationsService.substancePhysicalModifications.subscribe((function(l){n.modifications=l}));this.subscriptions.push(l)},l.prototype.ngOnDestroy=function(){this.componentDestroyed.emit(),this.subscriptions.forEach((function(n){n.unsubscribe()}))},l.prototype.addItem=function(){this.addStructuralModification()},l.prototype.addStructuralModification=function(){var n=this;this.substanceFormPhysicalModificationsService.addSubstancePhysicalModification(),setTimeout((function(){n.scrollToService.scrollToElement("substance-physical-modification-0","center")}))},l.prototype.deletePhysicalModification=function(n){this.substanceFormPhysicalModificationsService.deleteSubstancePhysicalModification(n)},l}(t("j/Lz").a),kn=t("vNul"),On=t("HECD"),Dn=e.rb({encapsulation:0,styles:[[".mat-divider.mat-divider-inset[_ngcontent-%COMP%]{margin-left:0}.substance-form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between}.mat-divider[_ngcontent-%COMP%]{border-top-color:rgba(0,0,0,.12)}.code[_ngcontent-%COMP%]:nth-child(odd){background-color:rgba(68,138,255,.07)}.code[_ngcontent-%COMP%]:nth-child(odd)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(68,138,255,.15)}.code[_ngcontent-%COMP%]:nth-child(even)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(128,128,128,.15)}.code[_ngcontent-%COMP%]     .mat-expansion-panel, .code[_ngcontent-%COMP%]     .mat-table, .code[_ngcontent-%COMP%]     textarea{background-color:transparent}"]],data:{}});function In(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"mat-divider",[["class","form-divider mat-divider"],["role","separator"]],[[1,"aria-orientation",0],[2,"mat-divider-vertical",null],[2,"mat-divider-horizontal",null],[2,"mat-divider-inset",null]],null,null,w.b,w.a)),e.sb(1,49152,null,0,C.a,[],{inset:[0,"inset"]},null)],(function(n,l){n(l,1,0,!0)}),(function(n,l){n(l,0,0,e.Fb(l,1).vertical?"vertical":"horizontal",e.Fb(l,1).vertical,!e.Fb(l,1).vertical,e.Fb(l,1).inset)}))}function $n(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,5,"div",[["appScrollToTarget",""],["class","alternate-backgrounds"]],[[8,"id",0]],null,null,null,null)),e.sb(1,4341760,null,0,F.a,[e.k,k.a],null,null),(n()(),e.tb(2,0,null,null,1,"app-physical-modification-form",[],null,[[null,"modDeleted"]],(function(n,l,t){var e=!0;return"modDeleted"===l&&(e=!1!==n.component.deletePhysicalModification(t)&&e),e}),wn,tn)),e.sb(3,114688,null,0,nn,[U.a,z.e,A.a,q.e,ln.a],{mod:[0,"mod"]},{modDeleted:"modDeleted"}),(n()(),e.jb(16777216,null,null,1,null,In)),e.sb(5,16384,null,0,S.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(n,l){n(l,3,0,l.context.$implicit),n(l,5,0,!l.context.last)}),(function(n,l){n(l,0,0,"substance-physical-modification-"+l.context.index)}))}function jn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"div",[["class","flex-row"]],null,null,null,null,null)),(n()(),e.tb(1,0,null,null,0,"span",[["class","middle-fill"]],null,null,null,null,null)),(n()(),e.jb(16777216,null,null,1,null,$n)),e.sb(3,278528,null,0,S.m,[e.P,e.M,e.s],{ngForOf:[0,"ngForOf"]},null)],(function(n,l){n(l,3,0,l.component.modifications)}),null)}function Ln(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"app-substance-form-physical-modifications-card",[],null,null,null,jn,Dn)),e.sb(1,4440064,null,0,Fn,[kn.a,k.a,On.a],null,null)],(function(n,l){n(l,1,0)}),null)}var Sn=e.pb("app-substance-form-physical-modifications-card",Fn,Ln,{},{menuLabelUpdate:"menuLabelUpdate",hiddenStateUpdate:"hiddenStateUpdate",canAddItemUpdate:"canAddItemUpdate",componentDestroyed:"componentDestroyed"},[]),Nn=t("M2Lx"),Tn=t("mVsa"),Un=t("v9Dh"),zn=t("ZYjt"),An=t("uGex"),qn=t("4tE/"),En=t("4epT"),Rn=t("EtvR"),Gn=t("4c35"),Vn=t("qAlS"),Yn=t("de3e"),Hn=t("La40"),Jn=t("/dO6"),Kn=t("NYLF"),Zn=t("y4qS"),Bn=t("BHnd"),Wn=t("YhbO"),Xn=t("jlZm"),Qn=t("6Wmm"),nl=t("9It4"),ll=t("PnCX"),tl=t("IyAz"),el=t("ZYCi"),ol=t("5uHe"),il=t("vfGX"),al=t("jS4w"),ul=t("u7R8"),cl=t("NnTW"),rl=t("Z+uX"),bl=t("Blfk"),sl=t("7fs6"),dl=t("YSh2"),pl=t("6jyQ");t.d(l,"SubstanceFormPhysicalModificationsModuleNgFactory",(function(){return ml}));var ml=e.qb(o,[],(function(n){return e.Cb([e.Db(512,e.j,e.bb,[[8,[i.a,a.a,u.a,c.a,r.a,b.a,s.a,d.a,p.b,m.a,f.a,g.a,x.a,h.a,v.a,P.a,y.a,M.a,_.a,Sn]],[3,e.j],e.x]),e.Db(4608,S.p,S.o,[e.u,[2,S.G]]),e.Db(4608,B.e,B.e,[]),e.Db(4608,B.w,B.w,[]),e.Db(4608,Nn.c,Nn.c,[]),e.Db(4608,q.c,q.c,[q.i,q.e,e.j,q.h,q.f,e.r,e.z,S.d,K.b,[2,S.j]]),e.Db(5120,q.j,q.k,[q.c]),e.Db(5120,Tn.c,Tn.j,[q.c]),e.Db(5120,Un.b,Un.c,[q.c]),e.Db(4608,zn.e,J.e,[[2,J.i],[2,J.n]]),e.Db(5120,An.a,An.b,[q.c]),e.Db(4608,J.d,J.d,[]),e.Db(5120,qn.b,qn.c,[q.c]),e.Db(5120,z.c,z.d,[q.c]),e.Db(135680,z.e,z.e,[q.c,e.r,[2,S.j],[2,z.b],z.c,[3,z.e],q.e]),e.Db(5120,En.c,En.a,[[3,En.c]]),e.Db(1073742336,S.c,S.c,[]),e.Db(1073742336,Rn.a,Rn.a,[]),e.Db(1073742336,B.v,B.v,[]),e.Db(1073742336,B.s,B.s,[]),e.Db(1073742336,B.k,B.k,[]),e.Db(1073742336,Nn.d,Nn.d,[]),e.Db(1073742336,H.e,H.e,[]),e.Db(1073742336,K.a,K.a,[]),e.Db(1073742336,J.n,J.n,[[2,J.f],[2,zn.f]]),e.Db(1073742336,Z.b,Z.b,[]),e.Db(1073742336,J.x,J.x,[]),e.Db(1073742336,Gn.g,Gn.g,[]),e.Db(1073742336,Vn.c,Vn.c,[]),e.Db(1073742336,q.g,q.g,[]),e.Db(1073742336,Tn.i,Tn.i,[]),e.Db(1073742336,Tn.f,Tn.f,[]),e.Db(1073742336,Yn.d,Yn.d,[]),e.Db(1073742336,Yn.c,Yn.c,[]),e.Db(1073742336,D.c,D.c,[]),e.Db(1073742336,L.c,L.c,[]),e.Db(1073742336,I.a,I.a,[]),e.Db(1073742336,Un.e,Un.e,[]),e.Db(1073742336,Hn.l,Hn.l,[]),e.Db(1073742336,C.b,C.b,[]),e.Db(1073742336,J.v,J.v,[]),e.Db(1073742336,J.s,J.s,[]),e.Db(1073742336,An.d,An.d,[]),e.Db(1073742336,X.c,X.c,[]),e.Db(1073742336,W.c,W.c,[]),e.Db(1073742336,Jn.f,Jn.f,[]),e.Db(1073742336,qn.e,qn.e,[]),e.Db(1073742336,Kn.a,Kn.a,[]),e.Db(1073742336,z.k,z.k,[]),e.Db(1073742336,Zn.p,Zn.p,[]),e.Db(1073742336,Bn.m,Bn.m,[]),e.Db(1073742336,Wn.c,Wn.c,[]),e.Db(1073742336,Xn.d,Xn.d,[]),e.Db(1073742336,Qn.b,Qn.b,[]),e.Db(1073742336,nl.d,nl.d,[]),e.Db(1073742336,ll.a,ll.a,[]),e.Db(1073742336,tl.a,tl.a,[]),e.Db(1073742336,el.p,el.p,[[2,el.u],[2,el.m]]),e.Db(1073742336,ol.a,ol.a,[]),e.Db(1073742336,il.a,il.a,[]),e.Db(1073742336,J.o,J.o,[]),e.Db(1073742336,V.d,V.d,[]),e.Db(1073742336,al.b,al.b,[]),e.Db(1073742336,ul.e,ul.e,[]),e.Db(1073742336,cl.b,cl.b,[]),e.Db(1073742336,rl.c,rl.c,[]),e.Db(1073742336,bl.c,bl.c,[]),e.Db(1073742336,sl.a,sl.a,[]),e.Db(1073742336,En.d,En.d,[]),e.Db(1073742336,o,o,[]),e.Db(256,Jn.a,{separatorKeyCodes:[dl.g]},[]),e.Db(1024,el.j,(function(){return[[]]}),[]),e.Db(256,pl.a,Fn,[])])}))}}]);
//# sourceMappingURL=18.31e213d536040313c8cf.js.map