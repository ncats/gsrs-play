(window.webpackJsonp=window.webpackJsonp||[]).push([[17],{Zrzf:function(n,e,l){"use strict";l.r(e);var t=l("CcnG"),i=function(){return function(){}}(),o=l("NcP4"),a=l("t68o"),u=l("pMnS"),c=l("+lnl"),s=l("EJ7M"),r=l("ap0P"),b=l("HE/B"),d=l("ThfK"),p=l("ldJ0"),m=l("OvbY"),h=l("Ok+c"),f=l("Pj+I"),g=l("Cka/"),k=l("UMU1"),v=l("dCG0"),D=l("B/2v"),y=l("S1Kd"),x=l("4z0a"),w=l("nFVu"),F=l("TtEo"),O=l("LC5p"),C=l("xZkp"),P=l("hifq"),L=l("bujt"),_=l("UodH"),S=l("lLAP"),M=l("wFw1"),I=l("v9Dh"),T=l("eDkP"),E=l("qAlS"),j=l("dWZg"),U=l("Fzqc"),z=l("ZYjt"),A=l("Mr+X"),N=l("SMsm"),Y=l("Ip0R"),G=l("s7Fu"),K=l("khmc"),H=l("YLZ7"),V=l("o3x0"),Z=l("6E2U"),q=l("4S5B"),R=l("Vurf"),$=l("rMNG"),B=function(){function n(n,e,l,i,o){this.cvService=n,this.dialog=e,this.utilsService=l,this.overlayContainerService=i,this.substanceFormService=o,this.linkDeleted=new t.n,this.subscriptions=[]}return n.prototype.ngOnInit=function(){this.getVocabularies(),this.overlayContainer=this.overlayContainerService.getContainerElement(),this.updateDisplay()},n.prototype.ngOnDestroy=function(){this.subscriptions.forEach((function(n){n.unsubscribe()}))},Object.defineProperty(n.prototype,"link",{get:function(){return this.privateLink},set:function(n){this.privateLink=n},enumerable:!0,configurable:!0}),n.prototype.updateDisplay=function(){this.siteDisplay=this.substanceFormService.siteString(this.privateLink.sites)},n.prototype.deleteLink=function(){var n=this;this.privateLink.$$deletedCode=this.utilsService.newUUID(),this.deleteTimer=setTimeout((function(){n.linkDeleted.emit(n.link),n.substanceFormService.emitOtherLinkUpdate()}),2e3)},n.prototype.undoDelete=function(){clearTimeout(this.deleteTimer),delete this.privateLink.$$deletedCode},n.prototype.getVocabularies=function(){var n=this,e=this.cvService.getDomainVocabulary("OTHER_LINKAGE_TYPE").subscribe((function(e){n.linkageTypes=e.OTHER_LINKAGE_TYPE.list}));this.subscriptions.push(e)},n.prototype.openDialog=function(){var n=this,e=this.dialog.open($.a,{data:{card:"other",link:this.privateLink.sites},width:"1040px",panelClass:"subunit-dialog"});this.overlayContainer.style.zIndex="1002";var l=e.afterClosed().subscribe((function(e){n.overlayContainer.style.zIndex=null,e&&(n.privateLink.sites=e,n.substanceFormService.emitOtherLinkUpdate()),n.updateDisplay()}));e.backdropClick().subscribe((function(n){})).unsubscribe(),this.subscriptions.push(l)},n}(),J=l("Jj5M"),W=t.rb({encapsulation:0,styles:[[".link-form-container[_ngcontent-%COMP%]{padding:30px 10px 12px;position:relative}.notification-backdrop[_ngcontent-%COMP%]{position:absolute;top:0;right:0;bottom:0;left:0;display:-webkit-box;display:-ms-flexbox;display:flex;z-index:10;background-color:rgba(255,255,255,.8);-webkit-box-pack:center;-ms-flex-pack:center;justify-content:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;font-size:30px;font-weight:700;color:#666}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.form-row[_ngcontent-%COMP%]   .delete-container[_ngcontent-%COMP%]{padding:0 10px 8px 0}.form-row[_ngcontent-%COMP%]   .note[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;padding-right:15px}.form-row[_ngcontent-%COMP%]   .links[_ngcontent-%COMP%], .form-row[_ngcontent-%COMP%]   .type[_ngcontent-%COMP%]{width:40%}.selectedSite[_ngcontent-%COMP%]{padding-left:2px;padding-right:2px}"]],data:{}});function X(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,6,"div",[["class","notification-backdrop"]],null,null,null,null,null)),(n()(),t.Nb(-1,null,[" Deleted  "])),(n()(),t.tb(2,16777216,null,null,4,"button",[["mat-icon-button",""],["matTooltip","Undo"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"],[null,"longpress"],[null,"keydown"],[null,"touchend"]],(function(n,e,l){var i=!0,o=n.component;return"longpress"===e&&(i=!1!==t.Fb(n,4).show()&&i),"keydown"===e&&(i=!1!==t.Fb(n,4)._handleKeydown(l)&&i),"touchend"===e&&(i=!1!==t.Fb(n,4)._handleTouchend()&&i),"click"===e&&(i=!1!==o.undoDelete()&&i),i}),L.d,L.b)),t.sb(3,180224,null,0,_.b,[t.k,S.h,[2,M.a]],null,null),t.sb(4,212992,null,0,I.d,[T.c,t.k,E.b,t.P,t.z,j.a,S.c,S.h,I.b,[2,U.b],[2,I.a],[2,z.f]],{message:[0,"message"]},null),(n()(),t.tb(5,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","undo"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,A.b,A.a)),t.sb(6,9158656,null,0,N.b,[t.k,N.d,[8,null],[2,N.a],[2,t.m]],{svgIcon:[0,"svgIcon"]},null)],(function(n,e){n(e,4,0,"Undo"),n(e,6,0,"undo")}),(function(n,e){n(e,2,0,t.Fb(e,3).disabled||null,"NoopAnimations"===t.Fb(e,3)._animationMode),n(e,5,0,t.Fb(e,6).inline,"primary"!==t.Fb(e,6).color&&"accent"!==t.Fb(e,6).color&&"warn"!==t.Fb(e,6).color)}))}function Q(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,21,"div",[["class","link-form-container"]],null,null,null,null,null)),(n()(),t.jb(16777216,null,null,1,null,X)),t.sb(2,16384,null,0,Y.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null),(n()(),t.tb(3,0,null,null,18,"div",[["class","form-row"]],null,null,null,null,null)),(n()(),t.tb(4,0,null,null,5,"div",[["class","delete-container"]],null,null,null,null,null)),(n()(),t.tb(5,16777216,null,null,4,"button",[["mat-icon-button",""],["matTooltip","Delete link"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"],[null,"longpress"],[null,"keydown"],[null,"touchend"]],(function(n,e,l){var i=!0,o=n.component;return"longpress"===e&&(i=!1!==t.Fb(n,7).show()&&i),"keydown"===e&&(i=!1!==t.Fb(n,7)._handleKeydown(l)&&i),"touchend"===e&&(i=!1!==t.Fb(n,7)._handleTouchend()&&i),"click"===e&&(i=!1!==o.deleteLink()&&i),i}),L.d,L.b)),t.sb(6,180224,null,0,_.b,[t.k,S.h,[2,M.a]],null,null),t.sb(7,212992,null,0,I.d,[T.c,t.k,E.b,t.P,t.z,j.a,S.c,S.h,I.b,[2,U.b],[2,I.a],[2,z.f]],{message:[0,"message"]},null),(n()(),t.tb(8,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","delete_forever"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,A.b,A.a)),t.sb(9,9158656,null,0,N.b,[t.k,N.d,[8,null],[2,N.a],[2,t.m]],{svgIcon:[0,"svgIcon"]},null),(n()(),t.tb(10,0,null,null,2,"div",[["class","type"]],null,null,null,null,null)),(n()(),t.tb(11,0,null,null,1,"app-cv-input",[["domain","OTHER_LINKAGE_TYPE"],["title","Sequence Type"]],null,[[null,"valueChange"]],(function(n,e,l){var t=!0;return"valueChange"===e&&(t=!1!==(n.component.link.linkageType=l)&&t),t}),G.b,G.a)),t.sb(12,245760,null,0,K.a,[H.a,V.e,Z.a,T.e,q.a,R.a],{title:[0,"title"],domain:[1,"domain"],model:[2,"model"]},{valueChange:"valueChange"}),(n()(),t.tb(13,0,null,null,8,"div",[["class","links"]],null,null,null,null,null)),(n()(),t.tb(14,0,null,null,1,"div",[["class","label"]],null,null,null,null,null)),(n()(),t.Nb(-1,null,["link"])),(n()(),t.Nb(16,null,[" "," "])),(n()(),t.tb(17,16777216,null,null,4,"button",[["mat-icon-button",""],["matTooltip","Select sites from display"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"],[null,"longpress"],[null,"keydown"],[null,"touchend"]],(function(n,e,l){var i=!0,o=n.component;return"longpress"===e&&(i=!1!==t.Fb(n,19).show()&&i),"keydown"===e&&(i=!1!==t.Fb(n,19)._handleKeydown(l)&&i),"touchend"===e&&(i=!1!==t.Fb(n,19)._handleTouchend()&&i),"click"===e&&(i=!1!==o.openDialog()&&i),i}),L.d,L.b)),t.sb(18,180224,null,0,_.b,[t.k,S.h,[2,M.a]],null,null),t.sb(19,212992,null,0,I.d,[T.c,t.k,E.b,t.P,t.z,j.a,S.c,S.h,I.b,[2,U.b],[2,I.a],[2,z.f]],{message:[0,"message"]},null),(n()(),t.tb(20,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","edit"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,A.b,A.a)),t.sb(21,9158656,null,0,N.b,[t.k,N.d,[8,null],[2,N.a],[2,t.m]],{svgIcon:[0,"svgIcon"]},null)],(function(n,e){var l=e.component;n(e,2,0,l.link.$$deletedCode),n(e,7,0,"Delete link"),n(e,9,0,"delete_forever"),n(e,12,0,"Sequence Type","OTHER_LINKAGE_TYPE",l.link.linkageType),n(e,19,0,"Select sites from display"),n(e,21,0,"edit")}),(function(n,e){var l=e.component;n(e,5,0,t.Fb(e,6).disabled||null,"NoopAnimations"===t.Fb(e,6)._animationMode),n(e,8,0,t.Fb(e,9).inline,"primary"!==t.Fb(e,9).color&&"accent"!==t.Fb(e,9).color&&"warn"!==t.Fb(e,9).color),n(e,16,0,l.siteDisplay),n(e,17,0,t.Fb(e,18).disabled||null,"NoopAnimations"===t.Fb(e,18)._animationMode),n(e,20,0,t.Fb(e,21).inline,"primary"!==t.Fb(e,21).color&&"accent"!==t.Fb(e,21).color&&"warn"!==t.Fb(e,21).color)}))}var nn=l("mrSG"),en=function(n){function e(e,l,t){var i=n.call(this,t)||this;return i.substanceFormOtherLinksService=e,i.scrollToService=l,i.gaService=t,i.subscriptions=[],i.analyticsEventCategory="substance form otherLinks",i}return nn.a(e,n),e.prototype.ngOnInit=function(){this.canAddItemUpdate.emit(!0),this.menuLabelUpdate.emit("Other Links")},e.prototype.ngAfterViewInit=function(){var n=this,e=this.substanceFormOtherLinksService.substanceOtherLinks.subscribe((function(e){n.otherLinks=e}));this.subscriptions.push(e)},e.prototype.ngOnDestroy=function(){this.componentDestroyed.emit(),this.subscriptions.forEach((function(n){n.unsubscribe()}))},e.prototype.addItem=function(){this.addOtherLink()},e.prototype.addOtherLink=function(){var n=this;this.substanceFormOtherLinksService.addSubstanceOtherLink(),setTimeout((function(){n.scrollToService.scrollToElement("substance-other-links-0","center")}))},e.prototype.deleteLink=function(n){this.substanceFormOtherLinksService.deleteSubstanceOtherLink(n)},e}(l("j/Lz").a),ln=l("QE93"),tn=l("HECD"),on=t.rb({encapsulation:0,styles:[[".mat-divider.mat-divider-inset[_ngcontent-%COMP%]{margin-left:0}.mat-divider[_ngcontent-%COMP%]{border-top-color:rgba(0,0,0,.12)}.code[_ngcontent-%COMP%]:nth-child(odd){background-color:rgba(68,138,255,.07)}.code[_ngcontent-%COMP%]:nth-child(odd)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(68,138,255,.15)}.code[_ngcontent-%COMP%]:nth-child(even)     .mat-expansion-panel:not(.mat-expanded):not([aria-disabled=true]) .mat-expansion-panel-header:hover{background-color:rgba(128,128,128,.15)}.code[_ngcontent-%COMP%]     .mat-expansion-panel, .code[_ngcontent-%COMP%]     .mat-table, .code[_ngcontent-%COMP%]     textarea{background-color:transparent}"]],data:{}});function an(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,1,"mat-divider",[["class","form-divider mat-divider"],["role","separator"]],[[1,"aria-orientation",0],[2,"mat-divider-vertical",null],[2,"mat-divider-horizontal",null],[2,"mat-divider-inset",null]],null,null,F.b,F.a)),t.sb(1,49152,null,0,O.a,[],{inset:[0,"inset"]},null)],(function(n,e){n(e,1,0,!0)}),(function(n,e){n(e,0,0,t.Fb(e,1).vertical?"vertical":"horizontal",t.Fb(e,1).vertical,!t.Fb(e,1).vertical,t.Fb(e,1).inset)}))}function un(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,5,"div",[["appScrollToTarget",""],["class","alternate-backgrounds"]],[[8,"id",0]],null,null,null,null)),t.sb(1,4341760,null,0,C.a,[t.k,P.a],null,null),(n()(),t.tb(2,0,null,null,1,"app-other-links-form",[],null,[[null,"linkDeleted"]],(function(n,e,l){var t=!0;return"linkDeleted"===e&&(t=!1!==n.component.deleteLink(l)&&t),t}),Q,W)),t.sb(3,245760,null,0,B,[H.a,V.e,Z.a,T.e,J.a],{link:[0,"link"]},{linkDeleted:"linkDeleted"}),(n()(),t.jb(16777216,null,null,1,null,an)),t.sb(5,16384,null,0,Y.n,[t.P,t.M],{ngIf:[0,"ngIf"]},null)],(function(n,e){n(e,3,0,e.context.$implicit),n(e,5,0,!e.context.last)}),(function(n,e){n(e,0,0,"substance-other-links-"+e.context.index)}))}function cn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,1,"div",[["class","flex-row"]],null,null,null,null,null)),(n()(),t.tb(1,0,null,null,0,"span",[["class","middle-fill"]],null,null,null,null,null)),(n()(),t.jb(16777216,null,null,1,null,un)),t.sb(3,278528,null,0,Y.m,[t.P,t.M,t.s],{ngForOf:[0,"ngForOf"]},null)],(function(n,e){n(e,3,0,e.component.otherLinks)}),null)}function sn(n){return t.Pb(0,[(n()(),t.tb(0,0,null,null,1,"app-substance-form-other-links-card",[],null,null,null,cn,on)),t.sb(1,4440064,null,0,en,[ln.a,P.a,tn.a],null,null)],(function(n,e){n(e,1,0)}),null)}var rn=t.pb("app-substance-form-other-links-card",en,sn,{},{menuLabelUpdate:"menuLabelUpdate",hiddenStateUpdate:"hiddenStateUpdate",canAddItemUpdate:"canAddItemUpdate",componentDestroyed:"componentDestroyed"},[]),bn=l("gIcY"),dn=l("M2Lx"),pn=l("mVsa"),mn=l("Wf4p"),hn=l("uGex"),fn=l("4tE/"),gn=l("4epT"),kn=l("EtvR"),vn=l("seP3"),Dn=l("4c35"),yn=l("de3e"),xn=l("La40"),wn=l("/VYK"),Fn=l("b716"),On=l("/dO6"),Cn=l("NYLF"),Pn=l("y4qS"),Ln=l("BHnd"),_n=l("YhbO"),Sn=l("jlZm"),Mn=l("6Wmm"),In=l("9It4"),Tn=l("PnCX"),En=l("IyAz"),jn=l("ZYCi"),Un=l("5uHe"),zn=l("vfGX"),An=l("0/Q6"),Nn=l("jS4w"),Yn=l("u7R8"),Gn=l("NnTW"),Kn=l("Z+uX"),Hn=l("Blfk"),Vn=l("7fs6"),Zn=l("YSh2"),qn=l("6jyQ");l.d(e,"SubstanceFormOtherLinksModuleNgFactory",(function(){return Rn}));var Rn=t.qb(i,[],(function(n){return t.Cb([t.Db(512,t.j,t.bb,[[8,[o.a,a.a,u.a,c.a,s.a,r.a,b.a,d.a,p.b,m.a,h.a,f.a,g.a,k.a,v.a,D.a,y.a,x.a,w.a,rn]],[3,t.j],t.x]),t.Db(4608,Y.p,Y.o,[t.u,[2,Y.G]]),t.Db(4608,bn.e,bn.e,[]),t.Db(4608,bn.w,bn.w,[]),t.Db(4608,dn.c,dn.c,[]),t.Db(4608,T.c,T.c,[T.i,T.e,t.j,T.h,T.f,t.r,t.z,Y.d,U.b,[2,Y.j]]),t.Db(5120,T.j,T.k,[T.c]),t.Db(5120,pn.c,pn.j,[T.c]),t.Db(5120,I.b,I.c,[T.c]),t.Db(4608,z.e,mn.e,[[2,mn.i],[2,mn.n]]),t.Db(5120,hn.a,hn.b,[T.c]),t.Db(4608,mn.d,mn.d,[]),t.Db(5120,fn.b,fn.c,[T.c]),t.Db(5120,V.c,V.d,[T.c]),t.Db(135680,V.e,V.e,[T.c,t.r,[2,Y.j],[2,V.b],V.c,[3,V.e],T.e]),t.Db(5120,gn.c,gn.a,[[3,gn.c]]),t.Db(1073742336,Y.c,Y.c,[]),t.Db(1073742336,kn.a,kn.a,[]),t.Db(1073742336,bn.v,bn.v,[]),t.Db(1073742336,bn.s,bn.s,[]),t.Db(1073742336,bn.k,bn.k,[]),t.Db(1073742336,dn.d,dn.d,[]),t.Db(1073742336,vn.e,vn.e,[]),t.Db(1073742336,U.a,U.a,[]),t.Db(1073742336,mn.n,mn.n,[[2,mn.f],[2,z.f]]),t.Db(1073742336,j.b,j.b,[]),t.Db(1073742336,mn.x,mn.x,[]),t.Db(1073742336,Dn.g,Dn.g,[]),t.Db(1073742336,E.c,E.c,[]),t.Db(1073742336,T.g,T.g,[]),t.Db(1073742336,pn.i,pn.i,[]),t.Db(1073742336,pn.f,pn.f,[]),t.Db(1073742336,yn.d,yn.d,[]),t.Db(1073742336,yn.c,yn.c,[]),t.Db(1073742336,_.c,_.c,[]),t.Db(1073742336,N.c,N.c,[]),t.Db(1073742336,S.a,S.a,[]),t.Db(1073742336,I.e,I.e,[]),t.Db(1073742336,xn.l,xn.l,[]),t.Db(1073742336,O.b,O.b,[]),t.Db(1073742336,mn.v,mn.v,[]),t.Db(1073742336,mn.s,mn.s,[]),t.Db(1073742336,hn.d,hn.d,[]),t.Db(1073742336,wn.c,wn.c,[]),t.Db(1073742336,Fn.c,Fn.c,[]),t.Db(1073742336,On.f,On.f,[]),t.Db(1073742336,fn.e,fn.e,[]),t.Db(1073742336,Cn.a,Cn.a,[]),t.Db(1073742336,V.k,V.k,[]),t.Db(1073742336,Pn.p,Pn.p,[]),t.Db(1073742336,Ln.m,Ln.m,[]),t.Db(1073742336,_n.c,_n.c,[]),t.Db(1073742336,Sn.d,Sn.d,[]),t.Db(1073742336,Mn.b,Mn.b,[]),t.Db(1073742336,In.d,In.d,[]),t.Db(1073742336,Tn.a,Tn.a,[]),t.Db(1073742336,En.a,En.a,[]),t.Db(1073742336,jn.p,jn.p,[[2,jn.u],[2,jn.m]]),t.Db(1073742336,Un.a,Un.a,[]),t.Db(1073742336,zn.a,zn.a,[]),t.Db(1073742336,mn.o,mn.o,[]),t.Db(1073742336,An.d,An.d,[]),t.Db(1073742336,Nn.b,Nn.b,[]),t.Db(1073742336,Yn.e,Yn.e,[]),t.Db(1073742336,Gn.b,Gn.b,[]),t.Db(1073742336,Kn.c,Kn.c,[]),t.Db(1073742336,Hn.c,Hn.c,[]),t.Db(1073742336,Vn.a,Vn.a,[]),t.Db(1073742336,gn.d,gn.d,[]),t.Db(1073742336,i,i,[]),t.Db(256,On.a,{separatorKeyCodes:[Zn.g]},[]),t.Db(1024,jn.j,(function(){return[[]]}),[]),t.Db(256,qn.a,en,[])])}))},"n67+":function(n,e,l){"use strict";l.d(e,"a",(function(){return i}));var t=l("CcnG"),i=function(){return function(){this.menuLabelUpdate=new t.n,this.hiddenStateUpdate=new t.n,this.canAddItemUpdate=new t.n,this.componentDestroyed=new t.n}}()}}]);
//# sourceMappingURL=17.46ea39fb1caa13f26930.js.map