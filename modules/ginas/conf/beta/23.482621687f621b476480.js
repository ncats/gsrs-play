(window.webpackJsonp=window.webpackJsonp||[]).push([[23],{L7yW:function(n,l,t){"use strict";t.r(l);var e=t("CcnG"),o=function(){return function(){}}(),a=t("NcP4"),i=t("t68o"),u=t("pMnS"),r=t("+lnl"),c=t("EJ7M"),d=t("ap0P"),s=t("HE/B"),b=t("ThfK"),m=t("ldJ0"),p=t("OvbY"),f=t("Ok+c"),g=t("Pj+I"),h=t("Cka/"),v=t("UMU1"),F=t("dCG0"),C=t("B/2v"),_=t("S1Kd"),y=t("4z0a"),x=t("nFVu"),M=t("TtEo"),D=t("LC5p"),w=t("xZkp"),P=t("hifq"),S=t("bujt"),k=t("UodH"),O=t("lLAP"),I=t("wFw1"),T=t("Mr+X"),L=t("SMsm"),E=t("dJrM"),j=t("seP3"),U=t("Wf4p"),A=t("Fzqc"),N=t("dWZg"),q=t("gIcY"),R=t("b716"),z=t("/VYK"),Y=t("Ip0R"),G=t("v0ZX"),V=t("Z16F"),K=t("CQqH"),Z=t("EdIQ"),H=t("s7Fu"),X=t("khmc"),$=t("YLZ7"),J=t("o3x0"),B=t("6E2U"),Q=t("eDkP"),W=t("4S5B"),nn=t("Vurf"),ln=t("jEQs"),tn=t("gvL1"),en=t("rMNG"),on=t("oY6q"),an=function(){function n(n,l,t,o,a){this.cvService=n,this.dialog=l,this.utilsService=t,this.overlayContainerService=o,this.substanceFormService=a,this.modDeleted=new e.n,this.modExtentList=[],this.modLocationList=[],this.modTypeList=[],this.subscriptions=[]}return n.prototype.ngOnInit=function(){this.getVocabularies(),this.overlayContainer=this.overlayContainerService.getContainerElement(),this.updateDisplay(),this.getSubstanceType()},n.prototype.ngAfterViewInit=function(){},Object.defineProperty(n.prototype,"mod",{get:function(){return this.privateMod},set:function(n){this.privateMod=n,this.relatedSubstanceUuid=this.privateMod.molecularFragment&&this.privateMod.molecularFragment.refuuid||""},enumerable:!0,configurable:!0}),n.prototype.getSubstanceType=function(){var n=this;this.substanceFormService.definition.subscribe((function(l){n.substanceType=l.substanceClass})).unsubscribe()},n.prototype.getVocabularies=function(){var n=this;this.cvService.getDomainVocabulary("STRUCTURAL_MODIFICATION_TYPE","LOCATION_TYPE","EXTENT_TYPE").subscribe((function(l){n.modTypeList=l.STRUCTURAL_MODIFICATION_TYPE.list,n.modLocationList=l.LOCATION_TYPE.list,n.modExtentList=l.EXTENT_TYPE.list}))},n.prototype.deleteMod=function(){var n=this;this.privateMod.$$deletedCode=this.utilsService.newUUID(),this.deleteTimer=setTimeout((function(){n.modDeleted.emit(n.privateMod)}),2e3)},n.prototype.undoDelete=function(){clearTimeout(this.deleteTimer),delete this.privateMod.$$deletedCode},n.prototype.updateAccess=function(n){this.mod.access=n},n.prototype.relatedSubstanceUpdated=function(n){null!==n?(this.mod.molecularFragment={refPname:n._name,name:n._name,refuuid:n.uuid,substanceClass:"reference",approvalID:n.approvalID},this.relatedSubstanceUuid=this.mod.molecularFragment.refuuid):(this.mod.molecularFragment={},this.relatedSubstanceUuid="")},n.prototype.openDialog=function(){var n=this,l=this.dialog.open(en.a,{data:{card:"other",link:this.mod.sites},width:"1040px",panelClass:"subunit-dialog"});this.overlayContainer.style.zIndex="1002";var t=l.afterClosed().subscribe((function(l){n.overlayContainer.style.zIndex=null,n.mod.sites=l,n.updateDisplay(),n.substanceFormService.emitStructuralModificationsUpdate()}));this.subscriptions.push(t)},n.prototype.openAmountDialog=function(){var n=this;this.mod.extentAmount||(this.mod.extentAmount={});var l=this.dialog.open(on.a,{data:{subsAmount:this.mod.extentAmount},width:"1040px"});this.overlayContainer.style.zIndex="1002";var t=l.afterClosed().subscribe((function(l){n.overlayContainer.style.zIndex=null,n.mod.extentAmount=l}));this.subscriptions.push(t)},n.prototype.updateDisplay=function(){this.siteDisplay=this.substanceFormService.siteString(this.mod.sites)},n.prototype.displayAmount=function(n){return this.utilsService.displayAmount(n)},n.prototype.formatValue=function(n){return n?"object"==typeof n?n.display?n.display:n.value?n.value:null:n:null},n}(),un=t("Jj5M"),rn=e.rb({encapsulation:0,styles:[['.code-form-container[_ngcontent-%COMP%]{padding:30px 10px 12px;position:relative;display:-webkit-box;display:-ms-flexbox;display:flex}.related-substance[_ngcontent-%COMP%]{max-width:25%;width:25%}  .related-substance img{max-width:125px!important;margin:auto}.notification-backdrop[_ngcontent-%COMP%]{position:absolute;top:0;right:0;bottom:0;left:0;display:-webkit-box;display:-ms-flexbox;display:flex;z-index:10;background-color:rgba(255,255,255,.8);-webkit-box-pack:center;-ms-flex-pack:center;justify-content:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;font-size:30px;font-weight:700;color:#666}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.form-row[_ngcontent-%COMP%]   .delete-container[_ngcontent-%COMP%]{padding:0 10px 8px 0}.form-row[_ngcontent-%COMP%]   .code-system[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .code-system-type[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .type[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;padding-right:15px}.form-row[_ngcontent-%COMP%]   .code-text[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .url[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1}.key-value-pair[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column;-ms-flex-item-align:start;align-self:flex-start}.key-value-pair[_ngcontent-%COMP%]   .key[_ngcontent-%COMP%]{font-size:11px;padding-bottom:3.5px;line-height:11px;color:rgba(0,0,0,.54);font-weight:400;font-family:Roboto,"Helvetica Neue",sans-serif}.key-value-pair[_ngcontent-%COMP%]   .value[_ngcontent-%COMP%]{font-size:15.5px}.references-container[_ngcontent-%COMP%]{width:100%}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;padding:0 10px;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.form-row[_ngcontent-%COMP%]   .checkbox-container[_ngcontent-%COMP%]{padding-bottom:18px}.form-row[_ngcontent-%COMP%]   .location-type[_ngcontent-%COMP%]   .extent[_ngcontent-%COMP%]   .sites[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1}.form-row[_ngcontent-%COMP%]   .amount[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .comment[_ngcontent-%COMP%]{width:45%;padding-right:15px;-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1}.padded[_ngcontent-%COMP%]{padding-right:20px}.amount-display[_ngcontent-%COMP%]{padding-top:11px}.form-actions[_ngcontent-%COMP%]{-webkit-box-pack:start;-ms-flex-pack:start;justify-content:flex-start;margin:5px 0 10px}.form-content[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1}.amount[_ngcontent-%COMP%], .extent[_ngcontent-%COMP%], .group-access[_ngcontent-%COMP%], .location-type[_ngcontent-%COMP%], .mod-type[_ngcontent-%COMP%], .sites[_ngcontent-%COMP%]{width:33%}.access[_ngcontent-%COMP%], .group[_ngcontent-%COMP%]{width:45%}.residues[_ngcontent-%COMP%]{width:60%}']],data:{}});function cn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,5,"div",[["class","notification-backdrop"]],null,null,null,null,null)),(n()(),e.Nb(-1,null,[" Deleted  "])),(n()(),e.tb(2,0,null,null,3,"button",[["mat-icon-button",""],["matTooltip","Undo"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,l,t){var e=!0;return"click"===l&&(e=!1!==n.component.undoDelete()&&e),e}),S.d,S.b)),e.sb(3,180224,null,0,k.b,[e.k,O.h,[2,I.a]],null,null),(n()(),e.tb(4,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","undo"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,T.b,T.a)),e.sb(5,9158656,null,0,L.b,[e.k,L.d,[8,null],[2,L.a],[2,e.m]],{svgIcon:[0,"svgIcon"]},null)],(function(n,l){n(l,5,0,"undo")}),(function(n,l){n(l,2,0,e.Fb(l,3).disabled||null,"NoopAnimations"===e.Fb(l,3)._animationMode),n(l,4,0,e.Fb(l,5).inline,"primary"!==e.Fb(l,5).color&&"accent"!==e.Fb(l,5).color&&"warn"!==e.Fb(l,5).color)}))}function dn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,7,"div",[["class","sites"]],null,null,null,null,null)),(n()(),e.tb(1,0,null,null,1,"div",[["class","label"]],null,null,null,null,null)),(n()(),e.Nb(-1,null,["Sites"])),(n()(),e.Nb(3,null,[" "," "])),(n()(),e.tb(4,0,null,null,3,"button",[["mat-icon-button",""],["matTooltip","Undo"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,l,t){var e=!0;return"click"===l&&(e=!1!==n.component.openDialog()&&e),e}),S.d,S.b)),e.sb(5,180224,null,0,k.b,[e.k,O.h,[2,I.a]],null,null),(n()(),e.tb(6,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","edit"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,T.b,T.a)),e.sb(7,9158656,null,0,L.b,[e.k,L.d,[8,null],[2,L.a],[2,e.m]],{svgIcon:[0,"svgIcon"]},null)],(function(n,l){n(l,7,0,"edit")}),(function(n,l){n(l,3,0,l.component.siteDisplay),n(l,4,0,e.Fb(l,5).disabled||null,"NoopAnimations"===e.Fb(l,5)._animationMode),n(l,6,0,e.Fb(l,7).inline,"primary"!==e.Fb(l,7).color&&"accent"!==e.Fb(l,7).color&&"warn"!==e.Fb(l,7).color)}))}function sn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,19,"div",[["class","residues"]],null,null,null,null,null)),(n()(),e.tb(1,0,null,null,18,"mat-form-field",[["class","group mat-form-field"]],[[2,"mat-form-field-appearance-standard",null],[2,"mat-form-field-appearance-fill",null],[2,"mat-form-field-appearance-outline",null],[2,"mat-form-field-appearance-legacy",null],[2,"mat-form-field-invalid",null],[2,"mat-form-field-can-float",null],[2,"mat-form-field-should-float",null],[2,"mat-form-field-has-label",null],[2,"mat-form-field-hide-placeholder",null],[2,"mat-form-field-disabled",null],[2,"mat-form-field-autofilled",null],[2,"mat-focused",null],[2,"mat-accent",null],[2,"mat-warn",null],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[2,"_mat-animation-noopable",null]],null,null,E.b,E.a)),e.sb(2,7520256,null,9,j.c,[e.k,e.h,[2,U.j],[2,A.b],[2,j.a],N.a,e.z,[2,I.a]],null,null),e.Lb(603979776,1,{_controlNonStatic:0}),e.Lb(335544320,2,{_controlStatic:0}),e.Lb(603979776,3,{_labelChildNonStatic:0}),e.Lb(335544320,4,{_labelChildStatic:0}),e.Lb(603979776,5,{_placeholderChild:0}),e.Lb(603979776,6,{_errorChildren:1}),e.Lb(603979776,7,{_hintChildren:1}),e.Lb(603979776,8,{_prefixChildren:1}),e.Lb(603979776,9,{_suffixChildren:1}),(n()(),e.tb(12,0,null,1,7,"input",[["class","mat-input-element mat-form-field-autofill-control"],["matInput",""],["name","Residue Modified"],["placeholder","Residue Modified"]],[[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[2,"mat-input-server",null],[1,"id",0],[1,"placeholder",0],[8,"disabled",0],[8,"required",0],[1,"readonly",0],[1,"aria-describedby",0],[1,"aria-invalid",0],[1,"aria-required",0]],[[null,"ngModelChange"],[null,"input"],[null,"blur"],[null,"compositionstart"],[null,"compositionend"],[null,"focus"]],(function(n,l,t){var o=!0,a=n.component;return"input"===l&&(o=!1!==e.Fb(n,13)._handleInput(t.target.value)&&o),"blur"===l&&(o=!1!==e.Fb(n,13).onTouched()&&o),"compositionstart"===l&&(o=!1!==e.Fb(n,13)._compositionStart()&&o),"compositionend"===l&&(o=!1!==e.Fb(n,13)._compositionEnd(t.target.value)&&o),"blur"===l&&(o=!1!==e.Fb(n,18)._focusChanged(!1)&&o),"focus"===l&&(o=!1!==e.Fb(n,18)._focusChanged(!0)&&o),"input"===l&&(o=!1!==e.Fb(n,18)._onInput()&&o),"ngModelChange"===l&&(o=!1!==(a.mod.residueModified=t)&&o),o}),null,null)),e.sb(13,16384,null,0,q.d,[e.E,e.k,[2,q.a]],null,null),e.Kb(1024,null,q.m,(function(n){return[n]}),[q.d]),e.sb(15,671744,null,0,q.r,[[8,null],[8,null],[8,null],[6,q.m]],{name:[0,"name"],model:[1,"model"]},{update:"ngModelChange"}),e.Kb(2048,null,q.n,null,[q.r]),e.sb(17,16384,null,0,q.o,[[4,q.n]],null,null),e.sb(18,999424,null,0,R.b,[e.k,N.a,[6,q.n],[2,q.q],[2,q.j],U.d,[8,null],z.a,e.z],{placeholder:[0,"placeholder"]},null),e.Kb(2048,[[1,4],[2,4]],j.d,null,[R.b])],(function(n,l){n(l,15,0,"Residue Modified",l.component.mod.residueModified),n(l,18,0,"Residue Modified")}),(function(n,l){n(l,1,1,["standard"==e.Fb(l,2).appearance,"fill"==e.Fb(l,2).appearance,"outline"==e.Fb(l,2).appearance,"legacy"==e.Fb(l,2).appearance,e.Fb(l,2)._control.errorState,e.Fb(l,2)._canLabelFloat,e.Fb(l,2)._shouldLabelFloat(),e.Fb(l,2)._hasFloatingLabel(),e.Fb(l,2)._hideControlPlaceholder(),e.Fb(l,2)._control.disabled,e.Fb(l,2)._control.autofilled,e.Fb(l,2)._control.focused,"accent"==e.Fb(l,2).color,"warn"==e.Fb(l,2).color,e.Fb(l,2)._shouldForward("untouched"),e.Fb(l,2)._shouldForward("touched"),e.Fb(l,2)._shouldForward("pristine"),e.Fb(l,2)._shouldForward("dirty"),e.Fb(l,2)._shouldForward("valid"),e.Fb(l,2)._shouldForward("invalid"),e.Fb(l,2)._shouldForward("pending"),!e.Fb(l,2)._animationsEnabled]),n(l,12,1,[e.Fb(l,17).ngClassUntouched,e.Fb(l,17).ngClassTouched,e.Fb(l,17).ngClassPristine,e.Fb(l,17).ngClassDirty,e.Fb(l,17).ngClassValid,e.Fb(l,17).ngClassInvalid,e.Fb(l,17).ngClassPending,e.Fb(l,18)._isServer,e.Fb(l,18).id,e.Fb(l,18).placeholder,e.Fb(l,18).disabled,e.Fb(l,18).required,e.Fb(l,18).readonly&&!e.Fb(l,18)._isNativeSelect||null,e.Fb(l,18)._ariaDescribedby||null,e.Fb(l,18).errorState,e.Fb(l,18).required.toString()])}))}function bn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"div",[["class","amount-display"]],null,null,null,null,null)),(n()(),e.Nb(1,null,[" "," "]))],null,(function(n,l){var t=l.component;n(l,1,0,t.displayAmount(t.mod.extentAmount))}))}function mn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,8,"div",[["class","amount"]],null,null,null,null,null)),(n()(),e.tb(1,0,null,null,1,"div",[["class","label padded"]],null,null,null,null,null)),(n()(),e.Nb(-1,null,[" Amount "])),(n()(),e.tb(3,0,null,null,3,"button",[["mat-icon-button",""],["matTooltip","add"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,l,t){var e=!0;return"click"===l&&(e=!1!==n.component.openAmountDialog()&&e),e}),S.d,S.b)),e.sb(4,180224,null,0,k.b,[e.k,O.h,[2,I.a]],null,null),(n()(),e.tb(5,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","edit"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,T.b,T.a)),e.sb(6,9158656,null,0,L.b,[e.k,L.d,[8,null],[2,L.a],[2,e.m]],{svgIcon:[0,"svgIcon"]},null),(n()(),e.jb(16777216,null,null,1,null,bn)),e.sb(8,16384,null,0,Y.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(n,l){var t=l.component;n(l,6,0,"edit"),n(l,8,0,t.mod.extentAmount)}),(function(n,l){n(l,3,0,e.Fb(l,4).disabled||null,"NoopAnimations"===e.Fb(l,4)._animationMode),n(l,5,0,e.Fb(l,6).inline,"primary"!==e.Fb(l,6).color&&"accent"!==e.Fb(l,6).color&&"warn"!==e.Fb(l,6).color)}))}function pn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,53,"div",[["class","code-form-container"]],null,null,null,null,null)),(n()(),e.jb(16777216,null,null,1,null,cn)),e.sb(2,16384,null,0,Y.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.tb(3,0,null,null,4,"div",[["class","delete-container"]],null,null,null,null,null)),(n()(),e.tb(4,0,null,null,3,"button",[["mat-icon-button",""],["matTooltip","Delete code"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,l,t){var e=!0;return"click"===l&&(e=!1!==n.component.deleteMod()&&e),e}),S.d,S.b)),e.sb(5,180224,null,0,k.b,[e.k,O.h,[2,I.a]],null,null),(n()(),e.tb(6,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","delete_forever"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,T.b,T.a)),e.sb(7,9158656,null,0,L.b,[e.k,L.d,[8,null],[2,L.a],[2,e.m]],{svgIcon:[0,"svgIcon"]},null),(n()(),e.tb(8,0,null,null,3,"div",[["class","flex-column related-substance"]],null,null,null,null,null)),(n()(),e.tb(9,0,null,null,2,"div",[["class","related-holder"]],null,null,null,null,null)),(n()(),e.tb(10,0,null,null,1,"app-substance-selector",[["eventCategory","substanceRelationshipRelatedSub"],["header","Molecular Fragment"],["placeholder","Molecular Fragment"]],null,[[null,"selectionUpdated"]],(function(n,l,t){var e=!0;return"selectionUpdated"===l&&(e=!1!==n.component.relatedSubstanceUpdated(t)&&e),e}),G.b,G.a)),e.sb(11,114688,null,0,V.a,[K.a,Z.a],{eventCategory:[0,"eventCategory"],placeholder:[1,"placeholder"],header:[2,"header"],subuuid:[3,"subuuid"]},{selectionUpdated:"selectionUpdated"}),(n()(),e.tb(12,0,null,null,41,"div",[["class","flex-column form-content"]],null,null,null,null,null)),(n()(),e.tb(13,0,null,null,6,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),e.tb(14,0,null,null,1,"app-cv-input",[["key","Structural Modification Structural Modification Type"],["title","Modification Type"]],null,[[null,"valueChange"]],(function(n,l,t){var e=!0;return"valueChange"===l&&(e=!1!==(n.component.mod.structuralModificationType=t)&&e),e}),H.b,H.a)),e.sb(15,245760,null,0,X.a,[$.a,J.e,B.a,Q.e,W.a,nn.a],{title:[0,"title"],key:[1,"key"],model:[2,"model"]},{valueChange:"valueChange"}),(n()(),e.jb(16777216,null,null,1,null,dn)),e.sb(17,16384,null,0,Y.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.jb(16777216,null,null,1,null,sn)),e.sb(19,16384,null,0,Y.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(n()(),e.tb(20,0,null,null,30,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),e.tb(21,0,null,null,1,"app-cv-input",[["title","Extent"]],null,[[null,"valueChange"]],(function(n,l,t){var e=!0;return"valueChange"===l&&(e=!1!==(n.component.mod.extent=t)&&e),e}),H.b,H.a)),e.sb(22,245760,null,0,X.a,[$.a,J.e,B.a,Q.e,W.a,nn.a],{vocabulary:[0,"vocabulary"],title:[1,"title"],model:[2,"model"]},{valueChange:"valueChange"}),(n()(),e.tb(23,0,null,null,1,"app-cv-input",[["title","Location"]],null,[[null,"valueChange"]],(function(n,l,t){var e=!0;return"valueChange"===l&&(e=!1!==(n.component.mod.locationType=t)&&e),e}),H.b,H.a)),e.sb(24,245760,null,0,X.a,[$.a,J.e,B.a,Q.e,W.a,nn.a],{vocabulary:[0,"vocabulary"],title:[1,"title"],model:[2,"model"]},{valueChange:"valueChange"}),(n()(),e.tb(25,0,null,null,25,"div",[["class","group-access"]],null,null,null,null,null)),(n()(),e.tb(26,0,null,null,24,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),e.tb(27,0,null,null,20,"mat-form-field",[["class","group mat-form-field"]],[[2,"mat-form-field-appearance-standard",null],[2,"mat-form-field-appearance-fill",null],[2,"mat-form-field-appearance-outline",null],[2,"mat-form-field-appearance-legacy",null],[2,"mat-form-field-invalid",null],[2,"mat-form-field-can-float",null],[2,"mat-form-field-should-float",null],[2,"mat-form-field-has-label",null],[2,"mat-form-field-hide-placeholder",null],[2,"mat-form-field-disabled",null],[2,"mat-form-field-autofilled",null],[2,"mat-focused",null],[2,"mat-accent",null],[2,"mat-warn",null],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[2,"_mat-animation-noopable",null]],null,null,E.b,E.a)),e.sb(28,7520256,null,9,j.c,[e.k,e.h,[2,U.j],[2,A.b],[2,j.a],N.a,e.z,[2,I.a]],null,null),e.Lb(603979776,10,{_controlNonStatic:0}),e.Lb(335544320,11,{_controlStatic:0}),e.Lb(603979776,12,{_labelChildNonStatic:0}),e.Lb(335544320,13,{_labelChildStatic:0}),e.Lb(603979776,14,{_placeholderChild:0}),e.Lb(603979776,15,{_errorChildren:1}),e.Lb(603979776,16,{_hintChildren:1}),e.Lb(603979776,17,{_prefixChildren:1}),e.Lb(603979776,18,{_suffixChildren:1}),(n()(),e.tb(38,0,null,1,9,"input",[["class","mat-input-element mat-form-field-autofill-control"],["matInput",""],["name","group"],["placeholder","Group"],["required",""]],[[1,"required",0],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[2,"mat-input-server",null],[1,"id",0],[1,"placeholder",0],[8,"disabled",0],[8,"required",0],[1,"readonly",0],[1,"aria-describedby",0],[1,"aria-invalid",0],[1,"aria-required",0]],[[null,"ngModelChange"],[null,"input"],[null,"blur"],[null,"compositionstart"],[null,"compositionend"],[null,"focus"]],(function(n,l,t){var o=!0,a=n.component;return"input"===l&&(o=!1!==e.Fb(n,39)._handleInput(t.target.value)&&o),"blur"===l&&(o=!1!==e.Fb(n,39).onTouched()&&o),"compositionstart"===l&&(o=!1!==e.Fb(n,39)._compositionStart()&&o),"compositionend"===l&&(o=!1!==e.Fb(n,39)._compositionEnd(t.target.value)&&o),"blur"===l&&(o=!1!==e.Fb(n,46)._focusChanged(!1)&&o),"focus"===l&&(o=!1!==e.Fb(n,46)._focusChanged(!0)&&o),"input"===l&&(o=!1!==e.Fb(n,46)._onInput()&&o),"ngModelChange"===l&&(o=!1!==(a.mod.modificationGroup=t)&&o),o}),null,null)),e.sb(39,16384,null,0,q.d,[e.E,e.k,[2,q.a]],null,null),e.sb(40,16384,null,0,q.t,[],{required:[0,"required"]},null),e.Kb(1024,null,q.l,(function(n){return[n]}),[q.t]),e.Kb(1024,null,q.m,(function(n){return[n]}),[q.d]),e.sb(43,671744,null,0,q.r,[[8,null],[6,q.l],[8,null],[6,q.m]],{name:[0,"name"],model:[1,"model"]},{update:"ngModelChange"}),e.Kb(2048,null,q.n,null,[q.r]),e.sb(45,16384,null,0,q.o,[[4,q.n]],null,null),e.sb(46,999424,null,0,R.b,[e.k,N.a,[6,q.n],[2,q.q],[2,q.j],U.d,[8,null],z.a,e.z],{placeholder:[0,"placeholder"],required:[1,"required"]},null),e.Kb(2048,[[10,4],[11,4]],j.d,null,[R.b]),(n()(),e.tb(48,0,null,null,2,"div",[["class","access"]],null,null,null,null,null)),(n()(),e.tb(49,0,null,null,1,"app-access-manager",[],null,[[null,"accessOut"]],(function(n,l,t){var e=!0;return"accessOut"===l&&(e=!1!==n.component.updateAccess(t)&&e),e}),ln.b,ln.a)),e.sb(50,4308992,null,0,tn.a,[$.a,e.k],{access:[0,"access"]},{accessOut:"accessOut"}),(n()(),e.tb(51,0,null,null,2,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),e.jb(16777216,null,null,1,null,mn)),e.sb(53,16384,null,0,Y.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(n,l){var t=l.component;n(l,2,0,t.mod.$$deletedCode),n(l,7,0,"delete_forever"),n(l,11,0,"substanceRelationshipRelatedSub","Molecular Fragment","Molecular Fragment",t.relatedSubstanceUuid),n(l,15,0,"Modification Type","Structural Modification Structural Modification Type",t.mod.structuralModificationType),n(l,17,0,"RESIDUE_SPECIFIC"!==t.mod.locationType),n(l,19,0,"RESIDUE_SPECIFIC"===t.mod.locationType),n(l,22,0,t.modExtentList,"Extent",t.mod.extent),n(l,24,0,t.modLocationList,"Location",t.mod.locationType),n(l,40,0,""),n(l,43,0,"group",t.mod.modificationGroup),n(l,46,0,"Group",""),n(l,50,0,t.mod.access),n(l,53,0,"PARTIAL"==t.mod.extent)}),(function(n,l){n(l,4,0,e.Fb(l,5).disabled||null,"NoopAnimations"===e.Fb(l,5)._animationMode),n(l,6,0,e.Fb(l,7).inline,"primary"!==e.Fb(l,7).color&&"accent"!==e.Fb(l,7).color&&"warn"!==e.Fb(l,7).color),n(l,27,1,["standard"==e.Fb(l,28).appearance,"fill"==e.Fb(l,28).appearance,"outline"==e.Fb(l,28).appearance,"legacy"==e.Fb(l,28).appearance,e.Fb(l,28)._control.errorState,e.Fb(l,28)._canLabelFloat,e.Fb(l,28)._shouldLabelFloat(),e.Fb(l,28)._hasFloatingLabel(),e.Fb(l,28)._hideControlPlaceholder(),e.Fb(l,28)._control.disabled,e.Fb(l,28)._control.autofilled,e.Fb(l,28)._control.focused,"accent"==e.Fb(l,28).color,"warn"==e.Fb(l,28).color,e.Fb(l,28)._shouldForward("untouched"),e.Fb(l,28)._shouldForward("touched"),e.Fb(l,28)._shouldForward("pristine"),e.Fb(l,28)._shouldForward("dirty"),e.Fb(l,28)._shouldForward("valid"),e.Fb(l,28)._shouldForward("invalid"),e.Fb(l,28)._shouldForward("pending"),!e.Fb(l,28)._animationsEnabled]),n(l,38,1,[e.Fb(l,40).required?"":null,e.Fb(l,45).ngClassUntouched,e.Fb(l,45).ngClassTouched,e.Fb(l,45).ngClassPristine,e.Fb(l,45).ngClassDirty,e.Fb(l,45).ngClassValid,e.Fb(l,45).ngClassInvalid,e.Fb(l,45).ngClassPending,e.Fb(l,46)._isServer,e.Fb(l,46).id,e.Fb(l,46).placeholder,e.Fb(l,46).disabled,e.Fb(l,46).required,e.Fb(l,46).readonly&&!e.Fb(l,46)._isNativeSelect||null,e.Fb(l,46)._ariaDescribedby||null,e.Fb(l,46).errorState,e.Fb(l,46).required.toString()])}))}var fn=t("mrSG"),gn=function(n){function l(l,t,e){var o=n.call(this,e)||this;return o.substanceFormStructuralModificationsService=l,o.scrollToService=t,o.gaService=e,o.subscriptions=[],o.analyticsEventCategory="substance form structural modifications",o}return fn.a(l,n),l.prototype.ngOnInit=function(){this.canAddItemUpdate.emit(!0),this.menuLabelUpdate.emit("Structural Modifications")},l.prototype.ngAfterViewInit=function(){var n=this,l=this.substanceFormStructuralModificationsService.substanceStructuralModifications.subscribe((function(l){n.modifications=l}));this.subscriptions.push(l)},l.prototype.ngOnDestroy=function(){this.componentDestroyed.emit(),this.subscriptions.forEach((function(n){n.unsubscribe()}))},l.prototype.addItem=function(){this.addStructuralModification()},l.prototype.addStructuralModification=function(){var n=this;this.substanceFormStructuralModificationsService.addSubstanceStructuralModification(),setTimeout((function(){n.scrollToService.scrollToElement("substance-structural-modification-0","center")}))},l.prototype.deleteStructuralModification=function(n){this.substanceFormStructuralModificationsService.deleteSubstanceStructuralModification(n)},l}(t("j/Lz").a),hn=t("NRox"),vn=t("HECD"),Fn=e.rb({encapsulation:0,styles:[[".mat-divider.mat-divider-inset[_ngcontent-%COMP%]{margin-left:0}.mat-divider[_ngcontent-%COMP%]{border-top-color:rgba(0,0,0,.12)}.code[_ngcontent-%COMP%]:nth-child(odd){background-color:rgba(68,138,255,.07)}.code[_ngcontent-%COMP%]:nth-child(odd)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(68,138,255,.15)}.code[_ngcontent-%COMP%]:nth-child(even)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(128,128,128,.15)}.code[_ngcontent-%COMP%]     .mat-expansion-panel, .code[_ngcontent-%COMP%]     .mat-table, .code[_ngcontent-%COMP%]     textarea{background-color:transparent}.search[_ngcontent-%COMP%]{width:400px;max-width:100%}"]],data:{}});function Cn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"mat-divider",[["class","form-divider mat-divider"],["role","separator"]],[[1,"aria-orientation",0],[2,"mat-divider-vertical",null],[2,"mat-divider-horizontal",null],[2,"mat-divider-inset",null]],null,null,M.b,M.a)),e.sb(1,49152,null,0,D.a,[],{inset:[0,"inset"]},null)],(function(n,l){n(l,1,0,!0)}),(function(n,l){n(l,0,0,e.Fb(l,1).vertical?"vertical":"horizontal",e.Fb(l,1).vertical,!e.Fb(l,1).vertical,e.Fb(l,1).inset)}))}function _n(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,5,"div",[["appScrollToTarget",""],["class","alternate-backgrounds"]],[[8,"id",0]],null,null,null,null)),e.sb(1,4341760,null,0,w.a,[e.k,P.a],null,null),(n()(),e.tb(2,0,null,null,1,"app-structural-modification-form",[],null,[[null,"modDeleted"]],(function(n,l,t){var e=!0;return"modDeleted"===l&&(e=!1!==n.component.deleteStructuralModification(t)&&e),e}),pn,rn)),e.sb(3,4308992,null,0,an,[$.a,J.e,B.a,Q.e,un.a],{mod:[0,"mod"]},{modDeleted:"modDeleted"}),(n()(),e.jb(16777216,null,null,1,null,Cn)),e.sb(5,16384,null,0,Y.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(n,l){n(l,3,0,l.context.$implicit),n(l,5,0,!l.context.last)}),(function(n,l){n(l,0,0,"substance-structural-modification-"+l.context.index)}))}function yn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"div",[["class","flex-row"]],null,null,null,null,null)),(n()(),e.tb(1,0,null,null,0,"span",[["class","middle-fill"]],null,null,null,null,null)),(n()(),e.jb(16777216,null,null,1,null,_n)),e.sb(3,278528,null,0,Y.m,[e.P,e.M,e.s],{ngForOf:[0,"ngForOf"]},null)],(function(n,l){n(l,3,0,l.component.modifications)}),null)}function xn(n){return e.Pb(0,[(n()(),e.tb(0,0,null,null,1,"app-substance-form-structural-modifications-card",[],null,null,null,yn,Fn)),e.sb(1,4440064,null,0,gn,[hn.a,P.a,vn.a],null,null)],(function(n,l){n(l,1,0)}),null)}var Mn=e.pb("app-substance-form-structural-modifications-card",gn,xn,{},{menuLabelUpdate:"menuLabelUpdate",hiddenStateUpdate:"hiddenStateUpdate",canAddItemUpdate:"canAddItemUpdate",componentDestroyed:"componentDestroyed"},[]),Dn=t("M2Lx"),wn=t("mVsa"),Pn=t("v9Dh"),Sn=t("ZYjt"),kn=t("uGex"),On=t("4tE/"),In=t("4epT"),Tn=t("EtvR"),Ln=t("4c35"),En=t("qAlS"),jn=t("de3e"),Un=t("La40"),An=t("/dO6"),Nn=t("NYLF"),qn=t("y4qS"),Rn=t("BHnd"),zn=t("YhbO"),Yn=t("jlZm"),Gn=t("6Wmm"),Vn=t("9It4"),Kn=t("PnCX"),Zn=t("IyAz"),Hn=t("ZYCi"),Xn=t("5uHe"),$n=t("vfGX"),Jn=t("0/Q6"),Bn=t("jS4w"),Qn=t("u7R8"),Wn=t("NnTW"),nl=t("Z+uX"),ll=t("Blfk"),tl=t("7fs6"),el=t("YSh2"),ol=t("6jyQ");t.d(l,"SubstanceFormStructuralModificationsModuleNgFactory",(function(){return al}));var al=e.qb(o,[],(function(n){return e.Cb([e.Db(512,e.j,e.bb,[[8,[a.a,i.a,u.a,r.a,c.a,d.a,s.a,b.a,m.b,p.a,f.a,g.a,h.a,v.a,F.a,C.a,_.a,y.a,x.a,Mn]],[3,e.j],e.x]),e.Db(4608,Y.p,Y.o,[e.u,[2,Y.G]]),e.Db(4608,q.e,q.e,[]),e.Db(4608,q.w,q.w,[]),e.Db(4608,Dn.c,Dn.c,[]),e.Db(4608,Q.c,Q.c,[Q.i,Q.e,e.j,Q.h,Q.f,e.r,e.z,Y.d,A.b,[2,Y.j]]),e.Db(5120,Q.j,Q.k,[Q.c]),e.Db(5120,wn.c,wn.j,[Q.c]),e.Db(5120,Pn.b,Pn.c,[Q.c]),e.Db(4608,Sn.e,U.e,[[2,U.i],[2,U.n]]),e.Db(5120,kn.a,kn.b,[Q.c]),e.Db(4608,U.d,U.d,[]),e.Db(5120,On.b,On.c,[Q.c]),e.Db(5120,J.c,J.d,[Q.c]),e.Db(135680,J.e,J.e,[Q.c,e.r,[2,Y.j],[2,J.b],J.c,[3,J.e],Q.e]),e.Db(5120,In.c,In.a,[[3,In.c]]),e.Db(1073742336,Y.c,Y.c,[]),e.Db(1073742336,Tn.a,Tn.a,[]),e.Db(1073742336,q.v,q.v,[]),e.Db(1073742336,q.s,q.s,[]),e.Db(1073742336,q.k,q.k,[]),e.Db(1073742336,Dn.d,Dn.d,[]),e.Db(1073742336,j.e,j.e,[]),e.Db(1073742336,A.a,A.a,[]),e.Db(1073742336,U.n,U.n,[[2,U.f],[2,Sn.f]]),e.Db(1073742336,N.b,N.b,[]),e.Db(1073742336,U.x,U.x,[]),e.Db(1073742336,Ln.g,Ln.g,[]),e.Db(1073742336,En.c,En.c,[]),e.Db(1073742336,Q.g,Q.g,[]),e.Db(1073742336,wn.i,wn.i,[]),e.Db(1073742336,wn.f,wn.f,[]),e.Db(1073742336,jn.d,jn.d,[]),e.Db(1073742336,jn.c,jn.c,[]),e.Db(1073742336,k.c,k.c,[]),e.Db(1073742336,L.c,L.c,[]),e.Db(1073742336,O.a,O.a,[]),e.Db(1073742336,Pn.e,Pn.e,[]),e.Db(1073742336,Un.l,Un.l,[]),e.Db(1073742336,D.b,D.b,[]),e.Db(1073742336,U.v,U.v,[]),e.Db(1073742336,U.s,U.s,[]),e.Db(1073742336,kn.d,kn.d,[]),e.Db(1073742336,z.c,z.c,[]),e.Db(1073742336,R.c,R.c,[]),e.Db(1073742336,An.f,An.f,[]),e.Db(1073742336,On.e,On.e,[]),e.Db(1073742336,Nn.a,Nn.a,[]),e.Db(1073742336,J.k,J.k,[]),e.Db(1073742336,qn.p,qn.p,[]),e.Db(1073742336,Rn.m,Rn.m,[]),e.Db(1073742336,zn.c,zn.c,[]),e.Db(1073742336,Yn.d,Yn.d,[]),e.Db(1073742336,Gn.b,Gn.b,[]),e.Db(1073742336,Vn.d,Vn.d,[]),e.Db(1073742336,Kn.a,Kn.a,[]),e.Db(1073742336,Zn.a,Zn.a,[]),e.Db(1073742336,Hn.p,Hn.p,[[2,Hn.u],[2,Hn.m]]),e.Db(1073742336,Xn.a,Xn.a,[]),e.Db(1073742336,$n.a,$n.a,[]),e.Db(1073742336,U.o,U.o,[]),e.Db(1073742336,Jn.d,Jn.d,[]),e.Db(1073742336,Bn.b,Bn.b,[]),e.Db(1073742336,Qn.e,Qn.e,[]),e.Db(1073742336,Wn.b,Wn.b,[]),e.Db(1073742336,nl.c,nl.c,[]),e.Db(1073742336,ll.c,ll.c,[]),e.Db(1073742336,tl.a,tl.a,[]),e.Db(1073742336,In.d,In.d,[]),e.Db(1073742336,o,o,[]),e.Db(256,An.a,{separatorKeyCodes:[el.g]},[]),e.Db(1024,Hn.j,(function(){return[[]]}),[]),e.Db(256,ol.a,gn,[])])}))},"n67+":function(n,l,t){"use strict";t.d(l,"a",(function(){return o}));var e=t("CcnG"),o=function(){return function(){this.menuLabelUpdate=new e.n,this.hiddenStateUpdate=new e.n,this.canAddItemUpdate=new e.n,this.componentDestroyed=new e.n}}()}}]);
//# sourceMappingURL=23.482621687f621b476480.js.map