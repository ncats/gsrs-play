(window.webpackJsonp=window.webpackJsonp||[]).push([[68],{"kNY/":function(n,l,e){"use strict";e.r(l);var a=e("CcnG"),t=function(){return function(){}}(),i=e("NcP4"),u=e("t68o"),o=e("pMnS"),b=e("+lnl"),c=e("EJ7M"),r=e("ap0P"),d=e("HE/B"),s=e("ThfK"),f=e("ldJ0"),p=e("OvbY"),m=e("Ok+c"),D=e("Pj+I"),g=e("Cka/"),h=e("UMU1"),F=e("dCG0"),v=e("B/2v"),_=e("S1Kd"),C=e("4z0a"),w=e("nFVu"),y=e("HfPH"),S=e("dJrM"),x=e("seP3"),k=e("Wf4p"),L=e("Fzqc"),P=e("dWZg"),j=e("wFw1"),M=e("gIcY"),O=e("b716"),I=e("/VYK"),U=e("jEQs"),E=e("gvL1"),q=e("YLZ7"),Y=e("RLOM"),G=e("OixI"),A=e("cbEn"),N=e("o3x0"),H=e("6E2U"),K=e("eDkP"),z=e("Jj5M"),J=e("Ip0R"),Z=e("mrSG"),V=function(n){function l(l,e,a){var t=n.call(this)||this;return t.substanceFormService=l,t.gaService=e,t.cvService=a,t.subscriptions=[],t.analyticsEventCategory="substance form ssg 3 definition",t}return Z.a(l,n),l.prototype.ngOnInit=function(){var n=this;this.menuLabelUpdate.emit("Definition");var l=this.substanceFormService.substance.subscribe((function(l){null==l.specifiedSubstanceG3.definition&&(l.specifiedSubstanceG3.definition={}),n.substanceFormService.resetState(),n.definition=l.specifiedSubstanceG3.definition}));this.subscriptions.push(l)},l.prototype.ngAfterViewInit=function(){},l.prototype.ngOnDestroy=function(){this.subscriptions.forEach((function(n){n.unsubscribe()}))},l.prototype.updateAccess=function(n){this.definition.access=n},l}(e("n67+").a),B=e("HECD"),R=a.rb({encapsulation:0,styles:[[".name-form-container[_ngcontent-%COMP%]{padding:30px 10px 12px;position:relative}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;padding:0 10px;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.form-row[_ngcontent-%COMP%]   .name[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;padding-right:15px}.cv-input[_ngcontent-%COMP%]{margin-right:15px}.references-container[_ngcontent-%COMP%]{width:100%}"]],data:{}});function T(n){return a.Pb(0,[(n()(),a.tb(0,0,null,null,26,"div",[["class","name-form-container"]],null,null,null,null,null)),(n()(),a.tb(1,0,null,null,21,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),a.tb(2,0,null,null,18,"mat-form-field",[["class","name mat-form-field"]],[[2,"mat-form-field-appearance-standard",null],[2,"mat-form-field-appearance-fill",null],[2,"mat-form-field-appearance-outline",null],[2,"mat-form-field-appearance-legacy",null],[2,"mat-form-field-invalid",null],[2,"mat-form-field-can-float",null],[2,"mat-form-field-should-float",null],[2,"mat-form-field-has-label",null],[2,"mat-form-field-hide-placeholder",null],[2,"mat-form-field-disabled",null],[2,"mat-form-field-autofilled",null],[2,"mat-focused",null],[2,"mat-accent",null],[2,"mat-warn",null],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null],[2,"_mat-animation-noopable",null]],null,null,S.b,S.a)),a.sb(3,7520256,null,9,x.c,[a.k,a.h,[2,k.j],[2,L.b],[2,x.a],P.a,a.z,[2,j.a]],null,null),a.Lb(603979776,1,{_controlNonStatic:0}),a.Lb(335544320,2,{_controlStatic:0}),a.Lb(603979776,3,{_labelChildNonStatic:0}),a.Lb(335544320,4,{_labelChildStatic:0}),a.Lb(603979776,5,{_placeholderChild:0}),a.Lb(603979776,6,{_errorChildren:1}),a.Lb(603979776,7,{_hintChildren:1}),a.Lb(603979776,8,{_prefixChildren:1}),a.Lb(603979776,9,{_suffixChildren:1}),(n()(),a.tb(13,0,null,1,7,"input",[["class","mat-input-element mat-form-field-autofill-control"],["matInput",""],["name","definition"],["placeholder","Definition"]],[[2,"mat-input-server",null],[1,"id",0],[1,"placeholder",0],[8,"disabled",0],[8,"required",0],[1,"readonly",0],[1,"aria-describedby",0],[1,"aria-invalid",0],[1,"aria-required",0],[2,"ng-untouched",null],[2,"ng-touched",null],[2,"ng-pristine",null],[2,"ng-dirty",null],[2,"ng-valid",null],[2,"ng-invalid",null],[2,"ng-pending",null]],[[null,"ngModelChange"],[null,"input"],[null,"blur"],[null,"compositionstart"],[null,"compositionend"],[null,"focus"]],(function(n,l,e){var t=!0,i=n.component;return"input"===l&&(t=!1!==a.Fb(n,14)._handleInput(e.target.value)&&t),"blur"===l&&(t=!1!==a.Fb(n,14).onTouched()&&t),"compositionstart"===l&&(t=!1!==a.Fb(n,14)._compositionStart()&&t),"compositionend"===l&&(t=!1!==a.Fb(n,14)._compositionEnd(e.target.value)&&t),"blur"===l&&(t=!1!==a.Fb(n,18)._focusChanged(!1)&&t),"focus"===l&&(t=!1!==a.Fb(n,18)._focusChanged(!0)&&t),"input"===l&&(t=!1!==a.Fb(n,18)._onInput()&&t),"ngModelChange"===l&&(t=!1!==(i.definition.definition=e)&&t),t}),null,null)),a.sb(14,16384,null,0,M.d,[a.E,a.k,[2,M.a]],null,null),a.Kb(1024,null,M.m,(function(n){return[n]}),[M.d]),a.sb(16,671744,null,0,M.r,[[8,null],[8,null],[8,null],[6,M.m]],{name:[0,"name"],model:[1,"model"]},{update:"ngModelChange"}),a.Kb(2048,null,M.n,null,[M.r]),a.sb(18,999424,null,0,O.b,[a.k,P.a,[6,M.n],[2,M.q],[2,M.j],k.d,[8,null],I.a,a.z],{placeholder:[0,"placeholder"]},null),a.sb(19,16384,null,0,M.o,[[4,M.n]],null,null),a.Kb(2048,[[1,4],[2,4]],x.d,null,[O.b]),(n()(),a.tb(21,0,null,null,1,"app-access-manager",[],null,[[null,"accessOut"]],(function(n,l,e){var a=!0;return"accessOut"===l&&(a=!1!==n.component.updateAccess(e)&&a),a}),U.b,U.a)),a.sb(22,4308992,null,0,E.a,[q.a,a.k],{access:[0,"access"]},{accessOut:"accessOut"}),(n()(),a.tb(23,0,null,null,3,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),a.tb(24,0,null,null,2,"div",[["class","references-container"]],null,null,null,null,null)),(n()(),a.tb(25,0,null,null,1,"app-domain-references",[],null,null,null,Y.b,Y.a)),a.sb(26,245760,[["references",4]],0,G.a,[q.a,A.a,N.e,a.k,H.a,K.e,z.a],{referencesUuids:[0,"referencesUuids"]},null)],(function(n,l){var e=l.component;n(l,16,0,"definition",e.definition.definition),n(l,18,0,"Definition"),n(l,22,0,e.definition.access),n(l,26,0,e.definition.references)}),(function(n,l){n(l,2,1,["standard"==a.Fb(l,3).appearance,"fill"==a.Fb(l,3).appearance,"outline"==a.Fb(l,3).appearance,"legacy"==a.Fb(l,3).appearance,a.Fb(l,3)._control.errorState,a.Fb(l,3)._canLabelFloat,a.Fb(l,3)._shouldLabelFloat(),a.Fb(l,3)._hasFloatingLabel(),a.Fb(l,3)._hideControlPlaceholder(),a.Fb(l,3)._control.disabled,a.Fb(l,3)._control.autofilled,a.Fb(l,3)._control.focused,"accent"==a.Fb(l,3).color,"warn"==a.Fb(l,3).color,a.Fb(l,3)._shouldForward("untouched"),a.Fb(l,3)._shouldForward("touched"),a.Fb(l,3)._shouldForward("pristine"),a.Fb(l,3)._shouldForward("dirty"),a.Fb(l,3)._shouldForward("valid"),a.Fb(l,3)._shouldForward("invalid"),a.Fb(l,3)._shouldForward("pending"),!a.Fb(l,3)._animationsEnabled]),n(l,13,1,[a.Fb(l,18)._isServer,a.Fb(l,18).id,a.Fb(l,18).placeholder,a.Fb(l,18).disabled,a.Fb(l,18).required,a.Fb(l,18).readonly&&!a.Fb(l,18)._isNativeSelect||null,a.Fb(l,18)._ariaDescribedby||null,a.Fb(l,18).errorState,a.Fb(l,18).required.toString(),a.Fb(l,19).ngClassUntouched,a.Fb(l,19).ngClassTouched,a.Fb(l,19).ngClassPristine,a.Fb(l,19).ngClassDirty,a.Fb(l,19).ngClassValid,a.Fb(l,19).ngClassInvalid,a.Fb(l,19).ngClassPending])}))}function W(n){return a.Pb(0,[(n()(),a.jb(16777216,null,null,1,null,T)),a.sb(1,16384,null,0,J.n,[a.P,a.M],{ngIf:[0,"ngIf"]},null)],(function(n,l){n(l,1,0,l.component.definition)}),null)}function Q(n){return a.Pb(0,[(n()(),a.tb(0,0,null,null,1,"app-ssg-definition-form",[],null,null,null,W,R)),a.sb(1,4440064,null,0,V,[z.a,B.a,q.a],null,null)],(function(n,l){n(l,1,0)}),null)}var X=a.pb("app-ssg-definition-form",V,Q,{},{menuLabelUpdate:"menuLabelUpdate",hiddenStateUpdate:"hiddenStateUpdate",canAddItemUpdate:"canAddItemUpdate",componentDestroyed:"componentDestroyed"},[]),$=e("M2Lx"),nn=e("mVsa"),ln=e("v9Dh"),en=e("ZYjt"),an=e("uGex"),tn=e("4tE/"),un=e("EtvR"),on=e("4c35"),bn=e("qAlS"),cn=e("de3e"),rn=e("UodH"),dn=e("SMsm"),sn=e("lLAP"),fn=e("La40"),pn=e("LC5p"),mn=e("/dO6"),Dn=e("NYLF"),gn=e("y4qS"),hn=e("BHnd"),Fn=e("YhbO"),vn=e("jlZm"),_n=e("6Wmm"),Cn=e("9It4"),wn=e("PnCX"),yn=e("IyAz"),Sn=e("ZYCi"),xn=e("5uHe"),kn=e("vfGX"),Ln=e("0/Q6"),Pn=e("jS4w"),jn=e("u7R8"),Mn=e("NnTW"),On=e("Z+uX"),In=e("Blfk"),Un=e("7fs6"),En=e("YSh2"),qn=e("6jyQ");e.d(l,"SsgDefinitionFormModuleNgFactory",(function(){return Yn}));var Yn=a.qb(t,[],(function(n){return a.Cb([a.Db(512,a.j,a.bb,[[8,[i.a,u.a,o.a,b.a,c.a,r.a,d.a,s.a,f.b,p.a,m.a,D.a,g.a,h.a,F.a,v.a,_.a,C.a,w.a,y.a,X]],[3,a.j],a.x]),a.Db(4608,J.p,J.o,[a.u,[2,J.G]]),a.Db(4608,M.e,M.e,[]),a.Db(4608,M.w,M.w,[]),a.Db(4608,$.c,$.c,[]),a.Db(4608,K.c,K.c,[K.i,K.e,a.j,K.h,K.f,a.r,a.z,J.d,L.b,[2,J.j]]),a.Db(5120,K.j,K.k,[K.c]),a.Db(5120,nn.c,nn.j,[K.c]),a.Db(5120,ln.b,ln.c,[K.c]),a.Db(4608,en.e,k.e,[[2,k.i],[2,k.n]]),a.Db(5120,an.a,an.b,[K.c]),a.Db(4608,k.d,k.d,[]),a.Db(5120,tn.b,tn.c,[K.c]),a.Db(5120,N.c,N.d,[K.c]),a.Db(135680,N.e,N.e,[K.c,a.r,[2,J.j],[2,N.b],N.c,[3,N.e],K.e]),a.Db(1073742336,J.c,J.c,[]),a.Db(1073742336,un.a,un.a,[]),a.Db(1073742336,M.v,M.v,[]),a.Db(1073742336,M.s,M.s,[]),a.Db(1073742336,M.k,M.k,[]),a.Db(1073742336,$.d,$.d,[]),a.Db(1073742336,x.e,x.e,[]),a.Db(1073742336,L.a,L.a,[]),a.Db(1073742336,k.n,k.n,[[2,k.f],[2,en.f]]),a.Db(1073742336,P.b,P.b,[]),a.Db(1073742336,k.x,k.x,[]),a.Db(1073742336,on.g,on.g,[]),a.Db(1073742336,bn.c,bn.c,[]),a.Db(1073742336,K.g,K.g,[]),a.Db(1073742336,nn.i,nn.i,[]),a.Db(1073742336,nn.f,nn.f,[]),a.Db(1073742336,cn.d,cn.d,[]),a.Db(1073742336,cn.c,cn.c,[]),a.Db(1073742336,rn.c,rn.c,[]),a.Db(1073742336,dn.c,dn.c,[]),a.Db(1073742336,sn.a,sn.a,[]),a.Db(1073742336,ln.e,ln.e,[]),a.Db(1073742336,fn.l,fn.l,[]),a.Db(1073742336,pn.b,pn.b,[]),a.Db(1073742336,k.v,k.v,[]),a.Db(1073742336,k.s,k.s,[]),a.Db(1073742336,an.d,an.d,[]),a.Db(1073742336,I.c,I.c,[]),a.Db(1073742336,O.c,O.c,[]),a.Db(1073742336,mn.f,mn.f,[]),a.Db(1073742336,tn.e,tn.e,[]),a.Db(1073742336,Dn.a,Dn.a,[]),a.Db(1073742336,N.k,N.k,[]),a.Db(1073742336,gn.p,gn.p,[]),a.Db(1073742336,hn.m,hn.m,[]),a.Db(1073742336,Fn.c,Fn.c,[]),a.Db(1073742336,vn.d,vn.d,[]),a.Db(1073742336,_n.b,_n.b,[]),a.Db(1073742336,Cn.d,Cn.d,[]),a.Db(1073742336,wn.a,wn.a,[]),a.Db(1073742336,yn.a,yn.a,[]),a.Db(1073742336,Sn.q,Sn.q,[[2,Sn.v],[2,Sn.n]]),a.Db(1073742336,xn.a,xn.a,[]),a.Db(1073742336,kn.a,kn.a,[]),a.Db(1073742336,k.o,k.o,[]),a.Db(1073742336,Ln.d,Ln.d,[]),a.Db(1073742336,Pn.b,Pn.b,[]),a.Db(1073742336,jn.e,jn.e,[]),a.Db(1073742336,Mn.b,Mn.b,[]),a.Db(1073742336,On.c,On.c,[]),a.Db(1073742336,In.c,In.c,[]),a.Db(1073742336,Un.a,Un.a,[]),a.Db(1073742336,t,t,[]),a.Db(256,mn.a,{separatorKeyCodes:[En.g]},[]),a.Db(1024,Sn.k,(function(){return[[]]}),[]),a.Db(256,qn.a,V,[])])}))}}]);
//# sourceMappingURL=68.d963f357d92a1c2f6356.js.map