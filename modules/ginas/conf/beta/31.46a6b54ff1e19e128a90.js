(window.webpackJsonp=window.webpackJsonp||[]).push([[31],{bBkp:function(n,e,t){"use strict";t.d(e,"a",(function(){return l}));var c=t("CcnG"),l=function(){return function(){this.menuLabelUpdate=new c.n,this.hiddenStateUpdate=new c.n,this.canAddItemUpdate=new c.n,this.componentDestroyed=new c.n}}()},m9jb:function(n,e,t){"use strict";t.r(e);var c=t("CcnG"),l=function(){return function(){}}(),a=t("NcP4"),i=t("t68o"),u=t("pMnS"),o=t("HvtJ"),b=t("/J3S"),s=t("R/n8"),d=t("ThfK"),p=t("ldJ0"),r=t("OvbY"),D=t("Ok+c"),f=t("Pj+I"),m=t("Cka/"),g=t("UMU1"),C=t("dCG0"),v=t("B/2v"),h=t("r3Nd"),y=t("5Z3t"),A=t("s7Fu"),S=t("khmc"),w=t("YLZ7"),O=t("o3x0"),P=t("6E2U"),E=t("eDkP"),U=t("4S5B"),x=t("Vurf"),_=t("92QI"),T=t("Sqm3"),I=t("jEQs"),N=t("gvL1"),M=t("Ip0R"),j=t("mrSG"),k=function(n){function e(e,t,c){var l=n.call(this,t)||this;return l.substanceFormService=e,l.gaService=t,l.cvService=c,l.subscriptions=[],l.dropdownSettings={},l.analyticsEventCategory="substance form Nucleic Acid Details",l}return j.b(e,n),e.prototype.ngOnInit=function(){var n=this;this.menuLabelUpdate.emit("Nucleic Acid Classification");var e=this.substanceFormService.substanceNucleicAcid.subscribe((function(e){n.nucleicAcid=e}));this.subscriptions.push(e),this.dropdownSettings={singleSelection:!1,idField:"value",textField:"display",selectAllText:"Select All",unSelectAllText:"UnSelect All",itemsShowLimit:3,allowSearchFilter:!0}},e.prototype.ngAfterViewInit=function(){},e.prototype.ngOnDestroy=function(){this.subscriptions.forEach((function(n){n.unsubscribe()}))},e.prototype.updateAccess=function(n){this.nucleicAcid.access=n},e.prototype.update=function(n,e){"nucleicAcidType"===n?this.nucleicAcid.nucleicAcidType=e:"sequenceOrigin"===n?this.nucleicAcid.sequenceOrigin=e:"sequenceType"===n&&(this.nucleicAcid.sequenceType=e)},e.prototype.updateSubtype=function(n){this.nucleicAcid.nucleicAcidSubType=n},e}(t("xhaW").a),q=t("Jj5M"),L=t("HECD"),Y=c.rb({encapsulation:0,styles:[[".subtype[_ngcontent-%COMP%]{width:33%}.tags[_ngcontent-%COMP%]{width:100%}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;padding:0 10px;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.form-row[_ngcontent-%COMP%]   .checkbox-container[_ngcontent-%COMP%]{padding-bottom:18px}.form-row[_ngcontent-%COMP%]   .origin[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .seqtype[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .sequence[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .subtype[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .type[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;padding-right:10px}"]],data:{}});function F(n){return c.Pb(0,[(n()(),c.tb(0,0,null,null,13,"div",[["class","na-form-container"]],null,null,null,null,null)),(n()(),c.tb(1,0,null,null,12,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),c.tb(2,0,null,null,1,"app-cv-input",[["class","type"],["domain","NUCLEIC_ACID_TYPE"],["title","Nucleic Acid Type"]],null,[[null,"valueChange"]],(function(n,e,t){var c=!0;return"valueChange"===e&&(c=!1!==n.component.update("nucleicAcidType",t)&&c),c}),A.b,A.a)),c.sb(3,245760,null,0,S.a,[w.a,O.e,P.a,E.e,U.a,x.a],{title:[0,"title"],domain:[1,"domain"],model:[2,"model"]},{valueChange:"valueChange"}),(n()(),c.tb(4,0,null,null,2,"div",[["class","mat-form-field-flex subtype"]],null,null,null,null,null)),(n()(),c.tb(5,0,null,null,1,"app-tag-selector",[["class","tags"],["cvDomain","NUCLEIC_ACID_SUBTYPE"],["placeholder","Nucleic Acid SubType"]],null,[[null,"tagsUpdate"]],(function(n,e,t){var c=!0;return"tagsUpdate"===e&&(c=!1!==n.component.updateSubtype(t)&&c),c}),_.b,_.a)),c.sb(6,4308992,null,0,T.a,[w.a],{cvDomain:[0,"cvDomain"],placeholder:[1,"placeholder"],tags:[2,"tags"]},{tagsUpdate:"tagsUpdate"}),(n()(),c.tb(7,0,null,null,2,"div",[["class","location-container origin"]],null,null,null,null,null)),(n()(),c.tb(8,0,null,null,1,"app-cv-input",[["domain","SEQUENCE_ORIGIN"],["title","Sequence Origin"]],null,[[null,"valueChange"]],(function(n,e,t){var c=!0;return"valueChange"===e&&(c=!1!==n.component.update("sequenceOrigin",t)&&c),c}),A.b,A.a)),c.sb(9,245760,null,0,S.a,[w.a,O.e,P.a,E.e,U.a,x.a],{title:[0,"title"],domain:[1,"domain"],model:[2,"model"]},{valueChange:"valueChange"}),(n()(),c.tb(10,0,null,null,1,"app-cv-input",[["class","seqtype"],["domain","SEQUENCE_TYPE"],["title","Sequence Type"]],null,[[null,"valueChange"]],(function(n,e,t){var c=!0;return"valueChange"===e&&(c=!1!==n.component.update("sequenceType",t)&&c),c}),A.b,A.a)),c.sb(11,245760,null,0,S.a,[w.a,O.e,P.a,E.e,U.a,x.a],{title:[0,"title"],domain:[1,"domain"],model:[2,"model"]},{valueChange:"valueChange"}),(n()(),c.tb(12,0,null,null,1,"app-access-manager",[],null,[[null,"accessOut"]],(function(n,e,t){var c=!0;return"accessOut"===e&&(c=!1!==n.component.updateAccess(t)&&c),c}),I.b,I.a)),c.sb(13,4308992,null,0,N.a,[w.a,c.k],{access:[0,"access"]},{accessOut:"accessOut"})],(function(n,e){var t=e.component;n(e,3,0,"Nucleic Acid Type","NUCLEIC_ACID_TYPE",t.nucleicAcid.nucleicAcidType),n(e,6,0,"NUCLEIC_ACID_SUBTYPE","Nucleic Acid SubType",t.nucleicAcid.nucleicAcidSubType),n(e,9,0,"Sequence Origin","SEQUENCE_ORIGIN",t.nucleicAcid.sequenceOrigin),n(e,11,0,"Sequence Type","SEQUENCE_TYPE",t.nucleicAcid.sequenceType),n(e,13,0,t.nucleicAcid.access)}),null)}function Q(n){return c.Pb(0,[(n()(),c.jb(16777216,null,null,1,null,F)),c.sb(1,16384,null,0,M.m,[c.P,c.M],{ngIf:[0,"ngIf"]},null)],(function(n,e){n(e,1,0,e.component.nucleicAcid)}),null)}function G(n){return c.Pb(0,[(n()(),c.tb(0,0,null,null,1,"app-nucleic-acid-details-form",[],null,null,null,Q,Y)),c.sb(1,4440064,null,0,k,[q.a,L.a,w.a],null,null)],(function(n,e){n(e,1,0)}),null)}var Z=c.pb("app-nucleic-acid-details-form",k,G,{},{menuLabelUpdate:"menuLabelUpdate",hiddenStateUpdate:"hiddenStateUpdate",canAddItemUpdate:"canAddItemUpdate",componentDestroyed:"componentDestroyed"},[]),B=t("M2Lx"),J=t("Wf4p"),R=t("gIcY"),H=t("Fzqc"),W=t("uGex"),V=t("mVsa"),z=t("v9Dh"),K=t("ZYjt"),X=t("4tE/"),$=t("EtvR"),nn=t("seP3"),en=t("dWZg"),tn=t("/VYK"),cn=t("b716"),ln=t("4c35"),an=t("qAlS"),un=t("de3e"),on=t("UodH"),bn=t("SMsm"),sn=t("lLAP"),dn=t("La40"),pn=t("LC5p"),rn=t("/dO6"),Dn=t("NYLF"),fn=t("y4qS"),mn=t("BHnd"),gn=t("YhbO"),Cn=t("jlZm"),vn=t("6Wmm"),hn=t("9It4"),yn=t("PnCX"),An=t("IyAz"),Sn=t("ZYCi"),wn=t("5uHe"),On=t("vfGX"),Pn=t("0/Q6"),En=t("jS4w"),Un=t("u7R8"),xn=t("NnTW"),_n=t("7fs6"),Tn=t("Z+uX"),In=t("shj5"),Nn=t("DD5N"),Mn=t("5NQ/"),jn=t("YSh2"),kn=t("6jyQ");t.d(e,"NucleicAcidDetailsFormModuleNgFactory",(function(){return qn}));var qn=c.qb(l,[],(function(n){return c.Cb([c.Db(512,c.j,c.bb,[[8,[a.a,i.a,u.a,o.a,b.a,s.a,d.a,p.b,r.a,D.a,f.a,m.a,g.a,C.a,v.a,h.a,y.a,Z]],[3,c.j],c.x]),c.Db(4608,M.o,M.n,[c.u,[2,M.E]]),c.Db(4608,B.c,B.c,[]),c.Db(4608,J.d,J.d,[]),c.Db(4608,R.e,R.e,[]),c.Db(4608,R.w,R.w,[]),c.Db(4608,E.c,E.c,[E.i,E.e,c.j,E.h,E.f,c.r,c.z,M.d,H.b,[2,M.i]]),c.Db(5120,E.j,E.k,[E.c]),c.Db(5120,W.a,W.b,[E.c]),c.Db(5120,V.c,V.j,[E.c]),c.Db(5120,z.b,z.c,[E.c]),c.Db(4608,K.e,J.e,[[2,J.i],[2,J.n]]),c.Db(5120,X.b,X.c,[E.c]),c.Db(5120,O.c,O.d,[E.c]),c.Db(135680,O.e,O.e,[E.c,c.r,[2,M.i],[2,O.b],O.c,[3,O.e],E.e]),c.Db(1073742336,M.c,M.c,[]),c.Db(1073742336,$.a,$.a,[]),c.Db(1073742336,B.d,B.d,[]),c.Db(1073742336,nn.e,nn.e,[]),c.Db(1073742336,en.b,en.b,[]),c.Db(1073742336,tn.c,tn.c,[]),c.Db(1073742336,cn.b,cn.b,[]),c.Db(1073742336,R.v,R.v,[]),c.Db(1073742336,R.s,R.s,[]),c.Db(1073742336,R.k,R.k,[]),c.Db(1073742336,H.a,H.a,[]),c.Db(1073742336,ln.g,ln.g,[]),c.Db(1073742336,an.c,an.c,[]),c.Db(1073742336,E.g,E.g,[]),c.Db(1073742336,J.n,J.n,[[2,J.f],[2,K.f]]),c.Db(1073742336,J.x,J.x,[]),c.Db(1073742336,J.v,J.v,[]),c.Db(1073742336,J.s,J.s,[]),c.Db(1073742336,W.d,W.d,[]),c.Db(1073742336,V.i,V.i,[]),c.Db(1073742336,V.f,V.f,[]),c.Db(1073742336,un.d,un.d,[]),c.Db(1073742336,un.c,un.c,[]),c.Db(1073742336,on.c,on.c,[]),c.Db(1073742336,bn.c,bn.c,[]),c.Db(1073742336,sn.a,sn.a,[]),c.Db(1073742336,z.e,z.e,[]),c.Db(1073742336,dn.j,dn.j,[]),c.Db(1073742336,pn.b,pn.b,[]),c.Db(1073742336,rn.f,rn.f,[]),c.Db(1073742336,X.e,X.e,[]),c.Db(1073742336,Dn.a,Dn.a,[]),c.Db(1073742336,O.k,O.k,[]),c.Db(1073742336,fn.p,fn.p,[]),c.Db(1073742336,mn.m,mn.m,[]),c.Db(1073742336,gn.c,gn.c,[]),c.Db(1073742336,Cn.d,Cn.d,[]),c.Db(1073742336,vn.b,vn.b,[]),c.Db(1073742336,hn.d,hn.d,[]),c.Db(1073742336,yn.a,yn.a,[]),c.Db(1073742336,An.a,An.a,[]),c.Db(1073742336,Sn.p,Sn.p,[[2,Sn.u],[2,Sn.m]]),c.Db(1073742336,wn.a,wn.a,[]),c.Db(1073742336,On.a,On.a,[]),c.Db(1073742336,J.o,J.o,[]),c.Db(1073742336,Pn.d,Pn.d,[]),c.Db(1073742336,En.b,En.b,[]),c.Db(1073742336,Un.e,Un.e,[]),c.Db(1073742336,xn.b,xn.b,[]),c.Db(1073742336,_n.a,_n.a,[]),c.Db(1073742336,Tn.c,Tn.c,[]),c.Db(1073742336,In.a,In.a,[]),c.Db(1073742336,Nn.a,Nn.a,[]),c.Db(1073742336,Mn.a,Mn.a,[]),c.Db(1073742336,l,l,[]),c.Db(256,rn.a,{separatorKeyCodes:[jn.f]},[]),c.Db(1024,Sn.j,(function(){return[[]]}),[]),c.Db(256,kn.a,k,[])])}))}}]);
//# sourceMappingURL=31.46a6b54ff1e19e128a90.js.map