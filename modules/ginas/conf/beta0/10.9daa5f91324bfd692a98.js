(window.webpackJsonp=window.webpackJsonp||[]).push([[10],{SZB6:function(n,e,l){"use strict";l.r(e);var t=l("CcnG"),i=function(){return function(){}}(),a=l("NcP4"),s=l("t68o"),o=l("pMnS"),u=l("+lnl"),r=l("EJ7M"),c=l("ap0P"),d=l("HE/B"),b=l("ThfK"),p=l("ldJ0"),f=l("OvbY"),m=l("Ok+c"),g=l("Pj+I"),h=l("Cka/"),k=l("UMU1"),v=l("dCG0"),F=l("B/2v"),x=l("S1Kd"),y=l("4z0a"),D=l("nFVu"),w=l("HfPH"),C=l("TtEo"),L=l("LC5p"),_=l("Ip0R"),I=l("xZkp"),S=l("hifq"),P=l("bujt"),M=l("UodH"),O=l("lLAP"),j=l("wFw1"),A=l("v9Dh"),U=l("eDkP"),T=l("qAlS"),z=l("dWZg"),q=l("Fzqc"),N=l("ZYjt"),E=l("Mr+X"),K=l("SMsm"),V=l("MlvX"),B=l("Wf4p"),$=l("dJrM"),G=l("seP3"),Y=l("Azqq"),Z=l("uGex"),H=l("gIcY"),J=l("rMNG"),X=function(){function n(n,e,l,i,a,s){this.cvService=n,this.dialog=e,this.utilsService=l,this.overlayContainerService=i,this.substanceFormService=a,this.substanceFormDisulfideLinksService=s,this.cysteine=[],this.linkDeleted=new t.n,this.testForm=new H.i({site0:new H.f("",[H.u.required]),site1:new H.f("",[H.u.required])}),this.subscriptions=[]}return n.prototype.ngOnInit=function(){this.privateLink.sites?(this.testForm.controls.site0.setValue(this.privateLink.sites[0]),this.testForm.controls.site1.setValue(this.privateLink.sites[1])):this.privateLink.sites=[{},{}],this.overlayContainer=this.overlayContainerService.getContainerElement()},n.prototype.ngAfterViewInit=function(){var n=this;setTimeout((function(){var e=n.substanceFormDisulfideLinksService.substanceCysteineSites.subscribe((function(e){n.cysteine=e}));n.subscriptions.push(e)}))},n.prototype.ngOnDestroy=function(){this.subscriptions.forEach((function(n){n.unsubscribe()}))},Object.defineProperty(n.prototype,"link",{get:function(){return this.privateLink},set:function(n){this.privateLink=n},enumerable:!0,configurable:!0}),n.prototype.deleteLink=function(){var n=this;confirm("Are you sure you want to delete links?")&&(this.privateLink.$$deletedCode=this.utilsService.newUUID(),this.deleteTimer=setTimeout((function(){n.linkDeleted.emit(n.link)}),1e3),this.substanceFormDisulfideLinksService.emitDisulfideLinkUpdate())},n.prototype.undoDelete=function(){clearTimeout(this.deleteTimer),delete this.privateLink.$$deletedCode,this.substanceFormDisulfideLinksService.emitDisulfideLinkUpdate()},n.prototype.updateAccess=function(n){this.link.access=n},n.prototype.updateSuggestions=function(n,e){this.cysteine=this.cysteine.filter((function(e){return e.residueIndex!==n.residueIndex||e.subunitIndex!==n.subunitIndex})),this.privateLink.sites[e]!==n&&(this.privateLink.sites[e].residueIndex&&this.cysteine.push(this.privateLink.sites[e]),this.privateLink.sites[e]=n,this.substanceFormDisulfideLinksService.updateCysteine(this.cysteine)),this.testForm.controls["site"+e].setValue(n)},n.prototype.openDialog=function(){var n=this,e=this.privateLink.sites;e[0].residueIndex&&e[1].residueIndex||(e=[]);var l=this.dialog.open(J.a,{data:{card:"disulfide",link:e},width:"1040px",panelClass:"subunit-dialog"});this.overlayContainer.style.zIndex="1002";var t=l.afterClosed().subscribe((function(e){n.overlayContainer.style.zIndex=null,e&&(e[0]&&e[0].subunitIndex?(n.privateLink.sites[0]=e[0],n.testForm.controls.site0.setValue(n.privateLink.sites[0])):(n.privateLink.sites[0]={},n.testForm.controls.site0.reset()),e[1]&&e[1].subunitIndex?(n.privateLink.sites[1]=e[1],n.testForm.controls.site1.setValue(n.privateLink.sites[1])):(n.privateLink.sites[1]={},n.testForm.controls.site1.reset())),n.substanceFormDisulfideLinksService.emitDisulfideLinkUpdate()}));this.subscriptions.push(t)},n}(),R=l("YLZ7"),W=l("o3x0"),Q=l("6E2U"),nn=l("Jj5M"),en=l("t1Tb"),ln=t.rb({encapsulation:0,styles:[[".notification-backdrop[_ngcontent-%COMP%]{position:absolute;top:0;right:0;bottom:0;left:0;display:-webkit-box;display:-ms-flexbox;display:flex;z-index:10;background-color:rgba(255,255,255,.8);-webkit-box-pack:center;-ms-flex-pack:center;justify-content:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;font-size:30px;font-weight:700;color:#666}.link-form-container[_ngcontent-%COMP%]{padding:30px 10px 12px;position:relative}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-ms-flex-pack:distribute;justify-content:space-around;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.form-row[_ngcontent-%COMP%]   .delete-container[_ngcontent-%COMP%]{padding:0 10px 8px 0}.form-row[_ngcontent-%COMP%]   .site[_ngcontent-%COMP%]{max-width:80px}.form-row[_ngcontent-%COMP%]   sites[_ngcontent-%COMP%]{width:35%}"]],data:{}});function tn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,6,"div",[["class","notification-backdrop"]],null,null,null,null,null)),(n()(),t.Nb(-1,null,[" Deleted  "])),(n()(),t.tb(2,16777216,null,null,4,"button",[["mat-icon-button",""],["matTooltip","Undo"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"],[null,"longpress"],[null,"keydown"],[null,"touchend"]],(function(n,e,l){var i=!0,a=n.component;return"longpress"===e&&(i=!1!==t.Fb(n,4).show()&&i),"keydown"===e&&(i=!1!==t.Fb(n,4)._handleKeydown(l)&&i),"touchend"===e&&(i=!1!==t.Fb(n,4)._handleTouchend()&&i),"click"===e&&(i=!1!==a.undoDelete()&&i),i}),P.d,P.b)),t.sb(3,180224,null,0,M.b,[t.k,O.h,[2,j.a]],null,null),t.sb(4,212992,null,0,A.d,[U.c,t.k,T.b,t.P,t.z,z.a,O.c,O.h,A.b,[2,q.b],[2,A.a],[2,N.f]],{message:[0,"message"]},null),(n()(),t.tb(5,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","undo"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,E.b,E.a)),t.sb(6,9158656,null,0,K.b,[t.k,K.d,[8,null],[2,K.a],[2,t.m]],{svgIcon:[0,"svgIcon"]},null)],(function(n,e){n(e,4,0,"Undo"),n(e,6,0,"undo")}),(function(n,e){n(e,2,0,t.Fb(e,3).disabled||null,"NoopAnimations"===t.Fb(e,3)._animationMode),n(e,5,0,t.Fb(e,6).inline,"primary"!==t.Fb(e,6).color&&"accent"!==t.Fb(e,6).color&&"warn"!==t.Fb(e,6).color)}))}function an(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,2,"mat-option",[["class","mat-option"],["role","option"]],[[1,"tabindex",0],[2,"mat-selected",null],[2,"mat-option-multiple",null],[2,"mat-active",null],[8,"id",0],[1,"aria-selected",0],[1,"aria-disabled",0],[2,"mat-option-disabled",null]],[[null,"click"],[null,"keydown"]],(function(n,e,l){var i=!0;return"click"===e&&(i=!1!==t.Fb(n,1)._selectViaInteraction()&&i),"keydown"===e&&(i=!1!==t.Fb(n,1)._handleKeydown(l)&&i),i}),V.c,V.a)),t.sb(1,8568832,[[10,4]],0,B.r,[t.k,t.h,[2,B.l],[2,B.q]],{value:[0,"value"]},null),(n()(),t.Nb(2,0,[" ","_"," "]))],(function(n,e){n(e,1,0,e.context.$implicit)}),(function(n,e){n(e,0,0,t.Fb(e,1)._getTabIndex(),t.Fb(e,1).selected,t.Fb(e,1).multiple,t.Fb(e,1).active,t.Fb(e,1).id,t.Fb(e,1)._getAriaSelected(),t.Fb(e,1).disabled.toString(),t.Fb(e,1).disabled),n(e,2,0,e.context.$implicit.subunitIndex,e.context.$implicit.residueIndex)}))}function sn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,29,"div",[["class","sites"]],null,null,null,null,null)),(n()(),t.tb(1,0,null,null,28,"mat-form-field",[["class","site mat-form-field"]],[[2,"mat-form-field-appearance-standard",null],[2,"mat-form-field-appearance-fill",null],[2,"mat-form-field-appearance-outline",null],[2,"mat-form-field-appearance-legacy",null],[2,"mat-form-field-invalid",null],[2,"mat-form-field-can-float",null],[2,"mat-form-field-should-float",null],[2,"mat-form-field-has-label",null],[2,"mat-form-field-hide-placeholder",null],[2,"mat-form-field-disabled",null],[2,"mat-form-field-autofilled",null],[2,"mat-focused",null],[2,"mat-accent",null],[2,"mat-warn",null],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[2,"_mat-animation-noopable",null]],null,null,$.b,$.a)),t.sb(2,7520256,null,9,G.c,[t.k,t.h,[2,B.j],[2,q.b],[2,G.a],z.a,t.z,[2,j.a]],null,null),t.Lb(603979776,1,{_controlNonStatic:0}),t.Lb(335544320,2,{_controlStatic:0}),t.Lb(603979776,3,{_labelChildNonStatic:0}),t.Lb(335544320,4,{_labelChildStatic:0}),t.Lb(603979776,5,{_placeholderChild:0}),t.Lb(603979776,6,{_errorChildren:1}),t.Lb(603979776,7,{_hintChildren:1}),t.Lb(603979776,8,{_prefixChildren:1}),t.Lb(603979776,9,{_suffixChildren:1}),(n()(),t.tb(12,0,null,3,2,"mat-label",[],null,null,null,null,null)),t.sb(13,16384,[[3,4],[4,4]],0,G.g,[],null,null),(n()(),t.Nb(14,null,["",""])),(n()(),t.tb(15,0,null,1,14,"mat-select",[["class","site-select mat-select"],["role","listbox"]],[[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[1,"id",0],[1,"tabindex",0],[1,"aria-label",0],[1,"aria-labelledby",0],[1,"aria-required",0],[1,"aria-disabled",0],[1,"aria-invalid",0],[1,"aria-owns",0],[1,"aria-multiselectable",0],[1,"aria-describedby",0],[1,"aria-activedescendant",0],[2,"mat-select-disabled",null],[2,"mat-select-invalid",null],[2,"mat-select-required",null],[2,"mat-select-empty",null]],[[null,"selectionChange"],[null,"keydown"],[null,"focus"],[null,"blur"]],(function(n,e,l){var i=!0,a=n.component;return"keydown"===e&&(i=!1!==t.Fb(n,20)._handleKeydown(l)&&i),"focus"===e&&(i=!1!==t.Fb(n,20)._onFocus()&&i),"blur"===e&&(i=!1!==t.Fb(n,20)._onBlur()&&i),"selectionChange"===e&&(i=!1!==a.updateSuggestions(l.value,n.context.index)&&i),i}),Y.b,Y.a)),t.Kb(6144,null,B.l,null,[Z.c]),t.sb(17,671744,null,0,H.h,[[3,H.c],[8,null],[8,null],[8,null],[2,H.x]],{name:[0,"name"]},null),t.Kb(2048,null,H.n,null,[H.h]),t.sb(19,16384,null,0,H.o,[[4,H.n]],null,null),t.sb(20,2080768,null,3,Z.c,[T.e,t.h,t.z,B.d,t.k,[2,q.b],[2,H.q],[2,H.j],[2,G.c],[6,H.n],[8,null],Z.a,O.j],null,{selectionChange:"selectionChange"}),t.Lb(603979776,10,{options:1}),t.Lb(603979776,11,{optionGroups:1}),t.Lb(603979776,12,{customTrigger:0}),t.Kb(2048,[[1,4],[2,4]],G.d,null,[Z.c]),(n()(),t.jb(16777216,null,1,1,null,an)),t.sb(26,278528,null,0,_.m,[t.P,t.M,t.s],{ngForOf:[0,"ngForOf"]},null),(n()(),t.tb(27,0,null,1,2,"mat-option",[["class","mat-option"],["role","option"]],[[1,"tabindex",0],[2,"mat-selected",null],[2,"mat-option-multiple",null],[2,"mat-active",null],[8,"id",0],[1,"aria-selected",0],[1,"aria-disabled",0],[2,"mat-option-disabled",null]],[[null,"click"],[null,"keydown"]],(function(n,e,l){var i=!0;return"click"===e&&(i=!1!==t.Fb(n,28)._selectViaInteraction()&&i),"keydown"===e&&(i=!1!==t.Fb(n,28)._handleKeydown(l)&&i),i}),V.c,V.a)),t.sb(28,8568832,[[10,4]],0,B.r,[t.k,t.h,[2,B.l],[2,B.q]],{value:[0,"value"]},null),(n()(),t.Nb(29,0,[" ","_"," "]))],(function(n,e){var l=e.component;n(e,17,0,"site"+e.context.index),n(e,20,0),n(e,26,0,l.cysteine),n(e,28,0,e.context.$implicit)}),(function(n,e){n(e,1,1,["standard"==t.Fb(e,2).appearance,"fill"==t.Fb(e,2).appearance,"outline"==t.Fb(e,2).appearance,"legacy"==t.Fb(e,2).appearance,t.Fb(e,2)._control.errorState,t.Fb(e,2)._canLabelFloat,t.Fb(e,2)._shouldLabelFloat(),t.Fb(e,2)._hasFloatingLabel(),t.Fb(e,2)._hideControlPlaceholder(),t.Fb(e,2)._control.disabled,t.Fb(e,2)._control.autofilled,t.Fb(e,2)._control.focused,"accent"==t.Fb(e,2).color,"warn"==t.Fb(e,2).color,t.Fb(e,2)._shouldForward("untouched"),t.Fb(e,2)._shouldForward("touched"),t.Fb(e,2)._shouldForward("pristine"),t.Fb(e,2)._shouldForward("dirty"),t.Fb(e,2)._shouldForward("valid"),t.Fb(e,2)._shouldForward("invalid"),t.Fb(e,2)._shouldForward("pending"),!t.Fb(e,2)._animationsEnabled]),n(e,14,0,0===e.context.index?"To":"From"),n(e,15,1,[t.Fb(e,19).ngClassUntouched,t.Fb(e,19).ngClassTouched,t.Fb(e,19).ngClassPristine,t.Fb(e,19).ngClassDirty,t.Fb(e,19).ngClassValid,t.Fb(e,19).ngClassInvalid,t.Fb(e,19).ngClassPending,t.Fb(e,20).id,t.Fb(e,20).tabIndex,t.Fb(e,20)._getAriaLabel(),t.Fb(e,20)._getAriaLabelledby(),t.Fb(e,20).required.toString(),t.Fb(e,20).disabled.toString(),t.Fb(e,20).errorState,t.Fb(e,20).panelOpen?t.Fb(e,20)._optionIds:null,t.Fb(e,20).multiple,t.Fb(e,20)._ariaDescribedby||null,t.Fb(e,20)._getAriaActiveDescendant(),t.Fb(e,20).disabled,t.Fb(e,20).errorState,t.Fb(e,20).required,t.Fb(e,20).empty]),n(e,27,0,t.Fb(e,28)._getTabIndex(),t.Fb(e,28).selected,t.Fb(e,28).multiple,t.Fb(e,28).active,t.Fb(e,28).id,t.Fb(e,28)._getAriaSelected(),t.Fb(e,28).disabled.toString(),t.Fb(e,28).disabled),n(e,29,0,e.context.$implicit.subunitIndex,e.context.$implicit.residueIndex)}))}function on(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,19,"div",[["class","link-form-container"]],[[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null]],[[null,"submit"],[null,"reset"]],(function(n,e,l){var i=!0;return"submit"===e&&(i=!1!==t.Fb(n,1).onSubmit(l)&&i),"reset"===e&&(i=!1!==t.Fb(n,1).onReset()&&i),i}),null,null)),t.sb(1,540672,null,0,H.j,[[8,null],[8,null]],{form:[0,"form"]},null),t.Kb(2048,null,H.c,null,[H.j]),t.sb(3,16384,null,0,H.p,[[4,H.c]],null,null),(n()(),t.jb(16777216,null,null,1,null,tn)),t.sb(5,16384,null,0,_.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null),(n()(),t.tb(6,0,null,null,13,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),t.tb(7,0,null,null,5,"div",[["class","delete-container"]],null,null,null,null,null)),(n()(),t.tb(8,16777216,null,null,4,"button",[["mat-icon-button",""],["matTooltip","Delete link"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"],[null,"longpress"],[null,"keydown"],[null,"touchend"]],(function(n,e,l){var i=!0,a=n.component;return"longpress"===e&&(i=!1!==t.Fb(n,10).show()&&i),"keydown"===e&&(i=!1!==t.Fb(n,10)._handleKeydown(l)&&i),"touchend"===e&&(i=!1!==t.Fb(n,10)._handleTouchend()&&i),"click"===e&&(i=!1!==a.deleteLink()&&i),i}),P.d,P.b)),t.sb(9,180224,null,0,M.b,[t.k,O.h,[2,j.a]],null,null),t.sb(10,212992,null,0,A.d,[U.c,t.k,T.b,t.P,t.z,z.a,O.c,O.h,A.b,[2,q.b],[2,A.a],[2,N.f]],{message:[0,"message"]},null),(n()(),t.tb(11,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","delete_forever"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,E.b,E.a)),t.sb(12,9158656,null,0,K.b,[t.k,K.d,[8,null],[2,K.a],[2,t.m]],{svgIcon:[0,"svgIcon"]},null),(n()(),t.jb(16777216,null,null,1,null,sn)),t.sb(14,278528,null,0,_.m,[t.P,t.M,t.s],{ngForOf:[0,"ngForOf"]},null),(n()(),t.tb(15,16777216,null,null,4,"button",[["mat-icon-button",""],["matTooltip","Select from sequence view"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"],[null,"longpress"],[null,"keydown"],[null,"touchend"]],(function(n,e,l){var i=!0,a=n.component;return"longpress"===e&&(i=!1!==t.Fb(n,17).show()&&i),"keydown"===e&&(i=!1!==t.Fb(n,17)._handleKeydown(l)&&i),"touchend"===e&&(i=!1!==t.Fb(n,17)._handleTouchend()&&i),"click"===e&&(i=!1!==a.openDialog()&&i),i}),P.d,P.b)),t.sb(16,180224,null,0,M.b,[t.k,O.h,[2,j.a]],null,null),t.sb(17,212992,null,0,A.d,[U.c,t.k,T.b,t.P,t.z,z.a,O.c,O.h,A.b,[2,q.b],[2,A.a],[2,N.f]],{message:[0,"message"]},null),(n()(),t.tb(18,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","edit"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,E.b,E.a)),t.sb(19,9158656,null,0,K.b,[t.k,K.d,[8,null],[2,K.a],[2,t.m]],{svgIcon:[0,"svgIcon"]},null)],(function(n,e){var l=e.component;n(e,1,0,l.testForm),n(e,5,0,l.link.$$deletedCode),n(e,10,0,"Delete link"),n(e,12,0,"delete_forever"),n(e,14,0,l.link.sites),n(e,17,0,"Select from sequence view"),n(e,19,0,"edit")}),(function(n,e){n(e,0,0,t.Fb(e,3).ngClassUntouched,t.Fb(e,3).ngClassTouched,t.Fb(e,3).ngClassPristine,t.Fb(e,3).ngClassDirty,t.Fb(e,3).ngClassValid,t.Fb(e,3).ngClassInvalid,t.Fb(e,3).ngClassPending),n(e,8,0,t.Fb(e,9).disabled||null,"NoopAnimations"===t.Fb(e,9)._animationMode),n(e,11,0,t.Fb(e,12).inline,"primary"!==t.Fb(e,12).color&&"accent"!==t.Fb(e,12).color&&"warn"!==t.Fb(e,12).color),n(e,15,0,t.Fb(e,16).disabled||null,"NoopAnimations"===t.Fb(e,16)._animationMode),n(e,18,0,t.Fb(e,19).inline,"primary"!==t.Fb(e,19).color&&"accent"!==t.Fb(e,19).color&&"warn"!==t.Fb(e,19).color)}))}var un=l("MvMx"),rn=l("mrSG"),cn=function(n){function e(e,l,t,i,a){var s=n.call(this,t)||this;return s.substanceFormDisulfideLinksService=e,s.substanceFormService=l,s.gaService=t,s.dialog=i,s.overlayContainerService=a,s.subscriptions=[],s.analyticsEventCategory="substance form disulfide Links",s}return rn.a(e,n),e.prototype.ngOnInit=function(){this.canAddItemUpdate.emit(!0),this.menuLabelUpdate.emit("Disulfide Links"),this.overlayContainer=this.overlayContainerService.getContainerElement()},e.prototype.ngAfterViewInit=function(){var n=this,e=this.substanceFormDisulfideLinksService.substanceDisulfideLinks.subscribe((function(e){n.disulfideLinks=e,n.countCysteine()}));this.subscriptions.push(e);var l=this.substanceFormService.substanceSubunits.subscribe((function(e){n.subunits=e,n.countCysteine()}));this.subscriptions.push(l);var t=this.substanceFormDisulfideLinksService.substanceCysteineSites.subscribe((function(e){n.cysteine=e,n.countCysteine()}));this.subscriptions.push(t)},e.prototype.ngOnDestroy=function(){this.componentDestroyed.emit(),this.subscriptions.forEach((function(n){n.unsubscribe()}))},e.prototype.countCysteine=function(){var n=this;null!=this.disulfideLinks&&null!=this.subunits&&null!=this.cysteine&&(this.cysteineBonds=0,this.subunits&&this.subunits.forEach((function(e){n.cysteineBonds+=e.sequence.toUpperCase().split("C").length-1})),this.cysteine&&this.cysteine.length?this.cysteineBonds=this.cysteine.length:this.cysteineBonds-=2*this.disulfideLinks.length,this.getSuggestions())},e.prototype.getSuggestions=function(){var n=[];if(this.subunits)for(var e=0;e<this.subunits.length;e++){var l=this.subunits[e].sequence;if(null!=l&&l.length>0)for(var t=0;t<l.length;t++)"C"===l[t].toUpperCase()&&n.push({residueIndex:t+1,subunitIndex:e+1})}this.disulfideLinks.forEach((function(e){e.sites&&e.sites.forEach((function(e){n=n.filter((function(n){return n.residueIndex!==e.residueIndex||n.subunitIndex!==e.subunitIndex}))}))}))},e.prototype.addItem=function(){this.addLink()},e.prototype.addLink=function(){this.substanceFormDisulfideLinksService.addSubstanceDisulfideLink(),setTimeout((function(){})),this.substanceFormDisulfideLinksService.emitDisulfideLinkUpdate()},e.prototype.deleteDisulfideLink=function(n){this.substanceFormDisulfideLinksService.deleteSubstanceDisulfideLink(n),this.substanceFormDisulfideLinksService.emitDisulfideLinkUpdate()},e.prototype.deleteAllDisulfideLinks=function(){confirm("Are you sure you want to delete all links?")&&(this.substanceFormDisulfideLinksService.deleteAllDisulfideLinks(),this.substanceFormDisulfideLinksService.emitDisulfideLinkUpdate())},e.prototype.openDialog=function(){var n=this,e=this.dialog.open(J.a,{data:{card:"multi-disulfide",link:[]},width:"1040px",panelClass:"subunit-dialog"});this.overlayContainer.style.zIndex="1002";var l=e.afterClosed().subscribe((function(e){n.overlayContainer.style.zIndex=null,e&&n.substanceFormDisulfideLinksService.addCompleteDisulfideLinks(e)}));this.subscriptions.push(l)},e}(l("j/Lz").a),dn=l("HECD"),bn=t.rb({encapsulation:0,styles:[[".link-form-container[_ngcontent-%COMP%]{padding:30px 10px 12px;position:relative}.notification-backdrop[_ngcontent-%COMP%]{position:absolute;top:0;right:0;bottom:0;left:0;display:-webkit-box;display:-ms-flexbox;display:flex;z-index:10;background-color:rgba(255,255,255,.8);-webkit-box-pack:center;-ms-flex-pack:center;justify-content:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;font-size:30px;font-weight:700;color:#666}.form-row2[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end;-webkit-box-flex:1;-ms-flex:1 0 99%;flex:1 0 99%}.form-row2[_ngcontent-%COMP%]   .delete-container[_ngcontent-%COMP%]{padding:0 10px 8px 0}.form-row2[_ngcontent-%COMP%]   .add-buttons[_ngcontent-%COMP%]{width:40%}.form-row2[_ngcontent-%COMP%]   .cysteine[_ngcontent-%COMP%]{width:50%}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.site-set[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex:1 0 48%;flex:1 0 48%;margin:5px;max-width:50%}.columns[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-ms-flex-wrap:wrap;flex-wrap:wrap}.alternate-backgrounds2[_ngcontent-%COMP%]:nth-child(4n){background-color:rgba(68,138,255,.07)}.alternate-backgrounds2[_ngcontent-%COMP%]:nth-child(4n)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(68,138,255,.15)}.alternate-backgrounds2[_ngcontent-%COMP%]:nth-child(4n+3){background-color:rgba(68,138,255,.07)}.alternate-backgrounds2[_ngcontent-%COMP%]:nth-child(4n+3)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(68,138,255,.15)}.alternate-backgrounds2[_ngcontent-%COMP%]:nth-child(4n+1)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(128,128,128,.15)}.alternate-backgrounds2[_ngcontent-%COMP%]:nth-child(4n+2)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(128,128,128,.15)}.alternate-backgrounds2[_ngcontent-%COMP%]     .mat-expansion-panel, .alternate-backgrounds2[_ngcontent-%COMP%]     .mat-table, .alternate-backgrounds2[_ngcontent-%COMP%]     textarea{background-color:transparent}"]],data:{}});function pn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,1,"mat-divider",[["class","mat-divider"],["role","separator"],["style",""]],[[1,"aria-orientation",0],[2,"mat-divider-vertical",null],[2,"mat-divider-horizontal",null],[2,"mat-divider-inset",null]],null,null,C.b,C.a)),t.sb(1,49152,null,0,L.a,[],{vertical:[0,"vertical"],inset:[1,"inset"]},null)],(function(n,e){n(e,1,0,!0,!0)}),(function(n,e){n(e,0,0,t.Fb(e,1).vertical?"vertical":"horizontal",t.Fb(e,1).vertical,!t.Fb(e,1).vertical,t.Fb(e,1).inset)}))}function fn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,1,"mat-divider",[["class","form-divider mat-divider"],["role","separator"]],[[1,"aria-orientation",0],[2,"mat-divider-vertical",null],[2,"mat-divider-horizontal",null],[2,"mat-divider-inset",null]],null,null,C.b,C.a)),t.sb(1,49152,null,0,L.a,[],{inset:[0,"inset"]},null)],(function(n,e){n(e,1,0,!0)}),(function(n,e){n(e,0,0,t.Fb(e,1).vertical?"vertical":"horizontal",t.Fb(e,1).vertical,!t.Fb(e,1).vertical,t.Fb(e,1).inset)}))}function mn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,9,"div",[["appScrollToTarget",""],["class","site-set alternate-backgrounds2"]],[[8,"id",0]],null,null,null,null)),t.Kb(512,null,_.B,_.C,[t.s,t.t,t.k,t.E]),t.sb(2,278528,null,0,_.l,[_.B],{klass:[0,"klass"],ngClass:[1,"ngClass"]},null),t.sb(3,4341760,null,0,I.a,[t.k,S.a],null,null),(n()(),t.jb(16777216,null,null,1,null,pn)),t.sb(5,16384,null,0,_.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null),(n()(),t.tb(6,0,null,null,1,"app-disulfide-links-form",[],null,[[null,"linkDeleted"]],(function(n,e,l){var t=!0;return"linkDeleted"===e&&(t=!1!==n.component.deleteDisulfideLink(l)&&t),t}),on,ln)),t.sb(7,4440064,null,0,X,[R.a,W.e,Q.a,U.e,nn.a,en.a],{link:[0,"link"]},{linkDeleted:"linkDeleted"}),(n()(),t.jb(16777216,null,null,1,null,fn)),t.sb(9,16384,null,0,_.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null)],(function(n,e){var l=e.component;n(e,2,0,"site-set alternate-backgrounds2",e.context.index%2==0?"side-border":""),n(e,5,0,e.context.index%2==0),n(e,7,0,e.context.$implicit),n(e,9,0,!e.context.last&&e.context.index!==l.disulfideLinks.length-2)}),(function(n,e){n(e,0,0,"substance-disulfide-links-"+e.context.index)}))}function gn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,3,"div",[["class","flex-row"]],null,null,null,null,null)),(n()(),t.tb(1,0,null,null,2,"button",[["mat-primary",""],["mat-raised-button",""]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,e,l){var t=!0;return"click"===e&&(t=!1!==n.component.deleteAllDisulfideLinks()&&t),t}),P.d,P.b)),t.sb(2,180224,null,0,M.b,[t.k,O.h,[2,j.a]],null,null),(n()(),t.Nb(-1,0,[" Delete All Links\n"]))],null,(function(n,e){n(e,1,0,t.Fb(e,2).disabled||null,"NoopAnimations"===t.Fb(e,2)._animationMode)}))}function hn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,11,"div",[["class","responsive columns"]],null,null,null,null,null)),(n()(),t.tb(1,0,null,null,8,"div",[["class","flex-row form-row2"]],null,null,null,null,null)),(n()(),t.tb(2,0,null,null,5,"div",[["class","add-buttons"]],null,null,null,null,null)),(n()(),t.tb(3,0,null,null,4,"button",[["mat-button",""]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(n,e,l){var t=!0;return"click"===e&&(t=!1!==n.component.openDialog()&&t),t}),P.d,P.b)),t.sb(4,180224,null,0,M.b,[t.k,O.h,[2,j.a]],null,null),(n()(),t.Nb(-1,0,[" Add Multiple Links "])),(n()(),t.tb(6,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","add_circle_outline"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,E.b,E.a)),t.sb(7,9158656,null,0,K.b,[t.k,K.d,[8,null],[2,K.a],[2,t.m]],{svgIcon:[0,"svgIcon"]},null),(n()(),t.tb(8,0,null,null,1,"div",[["class","cysteine"]],null,null,null,null,null)),(n()(),t.Nb(9,null,[" Number of unspecified Cysteine residues: "," "])),(n()(),t.jb(16777216,null,null,1,null,mn)),t.sb(11,278528,null,0,_.m,[t.P,t.M,t.s],{ngForOf:[0,"ngForOf"]},null),(n()(),t.jb(16777216,null,null,1,null,gn)),t.sb(13,16384,null,0,_.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null),(n()(),t.tb(14,0,null,null,1,"app-audit-info",[],null,null,null,v.c,v.b)),t.sb(15,114688,null,0,un.a,[],{source:[0,"source"]},null)],(function(n,e){var l=e.component;n(e,7,0,"add_circle_outline"),n(e,11,0,l.disulfideLinks),n(e,13,0,l.disulfideLinks&&l.disulfideLinks.length>0),n(e,15,0,l.disulfideLinks)}),(function(n,e){var l=e.component;n(e,3,0,t.Fb(e,4).disabled||null,"NoopAnimations"===t.Fb(e,4)._animationMode),n(e,6,0,t.Fb(e,7).inline,"primary"!==t.Fb(e,7).color&&"accent"!==t.Fb(e,7).color&&"warn"!==t.Fb(e,7).color),n(e,9,0,l.cysteineBonds)}))}function kn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,1,"app-substance-form-disulfide-links-card",[],null,null,null,hn,bn)),t.sb(1,4440064,null,0,cn,[en.a,nn.a,dn.a,W.e,U.e],null,null)],(function(n,e){n(e,1,0)}),null)}var vn=t.pb("app-substance-form-disulfide-links-card",cn,kn,{},{menuLabelUpdate:"menuLabelUpdate",hiddenStateUpdate:"hiddenStateUpdate",canAddItemUpdate:"canAddItemUpdate",componentDestroyed:"componentDestroyed"},[]),Fn=l("M2Lx"),xn=l("mVsa"),yn=l("4tE/"),Dn=l("4epT"),wn=l("EtvR"),Cn=l("4c35"),Ln=l("de3e"),_n=l("La40"),In=l("/VYK"),Sn=l("b716"),Pn=l("/dO6"),Mn=l("NYLF"),On=l("y4qS"),jn=l("BHnd"),An=l("YhbO"),Un=l("jlZm"),Tn=l("6Wmm"),zn=l("9It4"),qn=l("PnCX"),Nn=l("IyAz"),En=l("ZYCi"),Kn=l("5uHe"),Vn=l("vfGX"),Bn=l("0/Q6"),$n=l("jS4w"),Gn=l("u7R8"),Yn=l("NnTW"),Zn=l("Z+uX"),Hn=l("Blfk"),Jn=l("7fs6"),Xn=l("YSh2"),Rn=l("6jyQ");l.d(e,"SubstanceFormDisulfideLinksModuleNgFactory",(function(){return Wn}));var Wn=t.qb(i,[],(function(n){return t.Cb([t.Db(512,t.j,t.bb,[[8,[a.a,s.a,o.a,u.a,r.a,c.a,d.a,b.a,p.b,f.a,m.a,g.a,h.a,k.a,v.a,F.a,x.a,y.a,D.a,w.a,vn]],[3,t.j],t.x]),t.Db(4608,_.p,_.o,[t.u,[2,_.G]]),t.Db(4608,H.e,H.e,[]),t.Db(4608,H.w,H.w,[]),t.Db(4608,Fn.c,Fn.c,[]),t.Db(4608,U.c,U.c,[U.i,U.e,t.j,U.h,U.f,t.r,t.z,_.d,q.b,[2,_.j]]),t.Db(5120,U.j,U.k,[U.c]),t.Db(5120,xn.c,xn.j,[U.c]),t.Db(5120,A.b,A.c,[U.c]),t.Db(4608,N.e,B.e,[[2,B.i],[2,B.n]]),t.Db(5120,Z.a,Z.b,[U.c]),t.Db(4608,B.d,B.d,[]),t.Db(5120,yn.b,yn.c,[U.c]),t.Db(5120,W.c,W.d,[U.c]),t.Db(135680,W.e,W.e,[U.c,t.r,[2,_.j],[2,W.b],W.c,[3,W.e],U.e]),t.Db(5120,Dn.c,Dn.a,[[3,Dn.c]]),t.Db(1073742336,_.c,_.c,[]),t.Db(1073742336,wn.a,wn.a,[]),t.Db(1073742336,H.v,H.v,[]),t.Db(1073742336,H.s,H.s,[]),t.Db(1073742336,H.k,H.k,[]),t.Db(1073742336,Fn.d,Fn.d,[]),t.Db(1073742336,G.e,G.e,[]),t.Db(1073742336,q.a,q.a,[]),t.Db(1073742336,B.n,B.n,[[2,B.f],[2,N.f]]),t.Db(1073742336,z.b,z.b,[]),t.Db(1073742336,B.x,B.x,[]),t.Db(1073742336,Cn.g,Cn.g,[]),t.Db(1073742336,T.c,T.c,[]),t.Db(1073742336,U.g,U.g,[]),t.Db(1073742336,xn.i,xn.i,[]),t.Db(1073742336,xn.f,xn.f,[]),t.Db(1073742336,Ln.d,Ln.d,[]),t.Db(1073742336,Ln.c,Ln.c,[]),t.Db(1073742336,M.c,M.c,[]),t.Db(1073742336,K.c,K.c,[]),t.Db(1073742336,O.a,O.a,[]),t.Db(1073742336,A.e,A.e,[]),t.Db(1073742336,_n.l,_n.l,[]),t.Db(1073742336,L.b,L.b,[]),t.Db(1073742336,B.v,B.v,[]),t.Db(1073742336,B.s,B.s,[]),t.Db(1073742336,Z.d,Z.d,[]),t.Db(1073742336,In.c,In.c,[]),t.Db(1073742336,Sn.c,Sn.c,[]),t.Db(1073742336,Pn.f,Pn.f,[]),t.Db(1073742336,yn.e,yn.e,[]),t.Db(1073742336,Mn.a,Mn.a,[]),t.Db(1073742336,W.k,W.k,[]),t.Db(1073742336,On.p,On.p,[]),t.Db(1073742336,jn.m,jn.m,[]),t.Db(1073742336,An.c,An.c,[]),t.Db(1073742336,Un.d,Un.d,[]),t.Db(1073742336,Tn.b,Tn.b,[]),t.Db(1073742336,zn.d,zn.d,[]),t.Db(1073742336,qn.a,qn.a,[]),t.Db(1073742336,Nn.a,Nn.a,[]),t.Db(1073742336,En.p,En.p,[[2,En.u],[2,En.m]]),t.Db(1073742336,Kn.a,Kn.a,[]),t.Db(1073742336,Vn.a,Vn.a,[]),t.Db(1073742336,B.o,B.o,[]),t.Db(1073742336,Bn.d,Bn.d,[]),t.Db(1073742336,$n.b,$n.b,[]),t.Db(1073742336,Gn.e,Gn.e,[]),t.Db(1073742336,Yn.b,Yn.b,[]),t.Db(1073742336,Zn.c,Zn.c,[]),t.Db(1073742336,Hn.c,Hn.c,[]),t.Db(1073742336,Jn.a,Jn.a,[]),t.Db(1073742336,Dn.d,Dn.d,[]),t.Db(1073742336,i,i,[]),t.Db(256,Pn.a,{separatorKeyCodes:[Xn.g]},[]),t.Db(1024,En.j,(function(){return[[]]}),[]),t.Db(256,Rn.a,cn,[])])}))},"n67+":function(n,e,l){"use strict";l.d(e,"a",(function(){return i}));var t=l("CcnG"),i=function(){return function(){this.menuLabelUpdate=new t.n,this.hiddenStateUpdate=new t.n,this.canAddItemUpdate=new t.n,this.componentDestroyed=new t.n}}()}}]);
//# sourceMappingURL=10.9daa5f91324bfd692a98.js.map