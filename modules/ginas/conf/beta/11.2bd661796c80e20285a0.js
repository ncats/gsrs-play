(window.webpackJsonp=window.webpackJsonp||[]).push([[11],{NSYc:function(l,n,t){"use strict";t.r(n);var o=t("CcnG"),e=function(){return function(){}}(),i=t("NcP4"),a=t("t68o"),u=t("pMnS"),s=t("+lnl"),c=t("EJ7M"),b=t("ap0P"),r=t("HE/B"),d=t("ThfK"),y=t("ldJ0"),p=t("OvbY"),m=t("Ok+c"),g=t("Pj+I"),f=t("Cka/"),D=t("UMU1"),h=t("dCG0"),v=t("B/2v"),w=t("S1Kd"),k=t("4z0a"),C=t("nFVu"),S=t("HfPH"),x=t("s7Fu"),F=t("khmc"),G=t("YLZ7"),P=t("o3x0"),M=t("6E2U"),O=t("eDkP"),_=t("4S5B"),N=t("Vurf"),I=t("bujt"),j=t("UodH"),T=t("lLAP"),L=t("wFw1"),U=t("v9Dh"),Y=t("qAlS"),A=t("dWZg"),z=t("Fzqc"),E=t("ZYjt"),q=t("Mr+X"),H=t("SMsm"),V=t("MvMx"),K=t("Ip0R"),Z=t("mrSG"),B=t("j/Lz"),J=t("rMNG"),W=function(l){function n(n,t,o,e,i,a){var u=l.call(this,o)||this;return u.substanceFormGlycosylationService=n,u.substanceFormService=t,u.gaService=o,u.cvService=e,u.dialog=i,u.overlayContainerService=a,u.subscriptions=[],u.analyticsEventCategory="substance form glycosylation",u}return Z.a(n,l),n.prototype.ngOnInit=function(){this.menuLabelUpdate.emit("Glycosylation"),this.overlayContainer=this.overlayContainerService.getContainerElement(),this.getVocabularies()},n.prototype.ngAfterViewInit=function(){var l=this,n=this.substanceFormGlycosylationService.substanceGlycosylation.subscribe((function(n){l.glycosylation=n}));this.subscriptions.push(n)},n.prototype.ngOnDestroy=function(){this.subscriptions.forEach((function(l){l.unsubscribe()}))},n.prototype.clearAll=function(){this.glycosylation.CGlycosylationSites=[],this.glycosylation.NGlycosylationSites=[],this.glycosylation.OGlycosylationSites=[],this.glycosylation.glycosylationType=null,this.substanceFormGlycosylationService.emitGlycosylationUpdate()},n.prototype.getVocabularies=function(){var l=this,n=this.cvService.getDomainVocabulary("GLYCOSYLATION_TYPE").subscribe((function(n){l.glycosylationTypes=n.GLYCOSYLATION_TYPE.list}));this.subscriptions.push(n)},n.prototype.openDialog=function(l){var n=this,t=this.dialog.open(J.a,{data:{card:l,link:"N"===l?this.glycosylation.NGlycosylationSites:"C"===l?this.glycosylation.CGlycosylationSites:this.glycosylation.OGlycosylationSites},width:"1040px",panelClass:"subunit-dialog"});this.overlayContainer.style.zIndex="1002";var o=t.afterClosed().subscribe((function(t){n.overlayContainer.style.zIndex=null,t&&("N"===l?n.glycosylation.NGlycosylationSites=t:"C"===l?n.glycosylation.CGlycosylationSites=t:n.glycosylation.OGlycosylationSites=t,n.substanceFormGlycosylationService.emitGlycosylationUpdate())}));this.subscriptions.push(o)},n.prototype.siteDisplay=function(l){return this.substanceFormService.siteString(l)},n}(B.a),X=t("yLHp"),R=t("Jj5M"),Q=t("HECD"),$=o.rb({encapsulation:0,styles:[[".notification-backdrop[_ngcontent-%COMP%]{position:absolute;top:0;right:0;bottom:0;left:0;display:-webkit-box;display:-ms-flexbox;display:flex;z-index:10;background-color:rgba(255,255,255,.8);-webkit-box-pack:center;-ms-flex-pack:center;justify-content:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;font-size:30px;font-weight:700;color:#666}.sites[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:horizontal;-webkit-box-direction:normal;-ms-flex-direction:row;flex-direction:row}.row-label[_ngcontent-%COMP%]{width:25%;padding-top:10px}.row-sites[_ngcontent-%COMP%]{width:75%}.form-row[_ngcontent-%COMP%]{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;-webkit-box-align:end;-ms-flex-align:end;align-items:flex-end}.form-row[_ngcontent-%COMP%]   .delete-container[_ngcontent-%COMP%]{padding:0 10px 8px 0}.form-row[_ngcontent-%COMP%]   .note[_ngcontent-%COMP%]{-webkit-box-flex:1;-ms-flex-positive:1;flex-grow:1;padding-right:15px}.form-row[_ngcontent-%COMP%]   .name-type[_ngcontent-%COMP%]{width:25%}.form-row[_ngcontent-%COMP%]   .name-field[_ngcontent-%COMP%]{margin-bottom:60px}.form-row[_ngcontent-%COMP%]   .types[_ngcontent-%COMP%]{width:74%}.label[_ngcontent-%COMP%]{min-width:100px;padding-right:25px}"]],data:{}});function ll(l){return o.Pb(0,[(l()(),o.tb(0,0,null,null,49,"div",[],null,null,null,null,null)),(l()(),o.tb(1,0,null,null,46,"div",[["class","link-form-container"]],null,null,null,null,null)),(l()(),o.tb(2,0,null,null,45,"div",[["class","form-row"]],null,null,null,null,null)),(l()(),o.tb(3,0,null,null,7,"div",[["class","name-type"]],null,null,null,null,null)),(l()(),o.tb(4,0,null,null,1,"app-cv-input",[["key","Protein Glycosylation Type"],["title","Glycosylation Type"]],null,[[null,"valueChange"]],(function(l,n,t){var o=!0;return"valueChange"===n&&(o=!1!==(l.component.glycosylation.glycosylationType=t)&&o),o}),x.b,x.a)),o.sb(5,245760,null,0,F.a,[G.a,P.e,M.a,O.e,_.a,N.a],{title:[0,"title"],key:[1,"key"],model:[2,"model"]},{valueChange:"valueChange"}),(l()(),o.tb(6,0,null,null,0,"br",[],null,null,null,null,null)),(l()(),o.tb(7,0,null,null,0,"br",[],null,null,null,null,null)),(l()(),o.tb(8,0,null,null,2,"button",[["mat-primary",""],["mat-raised-button",""]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"]],(function(l,n,t){var o=!0;return"click"===n&&(o=!1!==l.component.clearAll()&&o),o}),I.d,I.b)),o.sb(9,180224,null,0,j.b,[o.k,T.h,[2,L.a]],null,null),(l()(),o.Nb(-1,0,["Clear Glycosylation"])),(l()(),o.tb(11,0,null,null,36,"div",[["class","types"]],null,null,null,null,null)),(l()(),o.tb(12,0,null,null,11,"div",[["class","sites"]],null,null,null,null,null)),(l()(),o.tb(13,0,null,null,3,"div",[["class","label row-label"]],null,null,null,null,null)),(l()(),o.tb(14,0,null,null,1,"b",[],null,null,null,null,null)),(l()(),o.Nb(-1,null,["C "])),(l()(),o.Nb(-1,null,["Glycosylation Sites "])),(l()(),o.tb(17,0,null,null,6,"div",[["class","row-sites"]],null,null,null,null,null)),(l()(),o.tb(18,16777216,null,null,4,"button",[["mat-icon-button",""],["matTooltip","Select sites from sequence"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"],[null,"longpress"],[null,"keydown"],[null,"touchend"]],(function(l,n,t){var e=!0,i=l.component;return"longpress"===n&&(e=!1!==o.Fb(l,20).show()&&e),"keydown"===n&&(e=!1!==o.Fb(l,20)._handleKeydown(t)&&e),"touchend"===n&&(e=!1!==o.Fb(l,20)._handleTouchend()&&e),"click"===n&&(e=!1!==i.openDialog("C")&&e),e}),I.d,I.b)),o.sb(19,180224,null,0,j.b,[o.k,T.h,[2,L.a]],null,null),o.sb(20,212992,null,0,U.d,[O.c,o.k,Y.b,o.P,o.z,A.a,T.c,T.h,U.b,[2,z.b],[2,U.a],[2,E.f]],{message:[0,"message"]},null),(l()(),o.tb(21,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","edit"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,q.b,q.a)),o.sb(22,9158656,null,0,H.b,[o.k,H.d,[8,null],[2,H.a],[2,o.m]],{svgIcon:[0,"svgIcon"]},null),(l()(),o.Nb(23,null,[" "," "])),(l()(),o.tb(24,0,null,null,11,"div",[["class","sites"]],null,null,null,null,null)),(l()(),o.tb(25,0,null,null,3,"div",[["class","label row-label"]],null,null,null,null,null)),(l()(),o.tb(26,0,null,null,1,"b",[],null,null,null,null,null)),(l()(),o.Nb(-1,null,["N "])),(l()(),o.Nb(-1,null,["Glycosylation Sites"])),(l()(),o.tb(29,0,null,null,6,"div",[["class","row-sites"]],null,null,null,null,null)),(l()(),o.tb(30,16777216,null,null,4,"button",[["mat-icon-button",""],["matTooltip","Select sites from sequence"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"],[null,"longpress"],[null,"keydown"],[null,"touchend"]],(function(l,n,t){var e=!0,i=l.component;return"longpress"===n&&(e=!1!==o.Fb(l,32).show()&&e),"keydown"===n&&(e=!1!==o.Fb(l,32)._handleKeydown(t)&&e),"touchend"===n&&(e=!1!==o.Fb(l,32)._handleTouchend()&&e),"click"===n&&(e=!1!==i.openDialog("N")&&e),e}),I.d,I.b)),o.sb(31,180224,null,0,j.b,[o.k,T.h,[2,L.a]],null,null),o.sb(32,212992,null,0,U.d,[O.c,o.k,Y.b,o.P,o.z,A.a,T.c,T.h,U.b,[2,z.b],[2,U.a],[2,E.f]],{message:[0,"message"]},null),(l()(),o.tb(33,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","edit"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,q.b,q.a)),o.sb(34,9158656,null,0,H.b,[o.k,H.d,[8,null],[2,H.a],[2,o.m]],{svgIcon:[0,"svgIcon"]},null),(l()(),o.Nb(35,null,[" "," "])),(l()(),o.tb(36,0,null,null,11,"div",[["class","sites"]],null,null,null,null,null)),(l()(),o.tb(37,0,null,null,3,"div",[["class","label row-label"]],null,null,null,null,null)),(l()(),o.tb(38,0,null,null,1,"b",[],null,null,null,null,null)),(l()(),o.Nb(-1,null,["O "])),(l()(),o.Nb(-1,null,["Glycosylation Sites"])),(l()(),o.tb(41,0,null,null,6,"div",[["class","row-sites"]],null,null,null,null,null)),(l()(),o.tb(42,16777216,null,null,4,"button",[["mat-icon-button",""],["matTooltip","Select sites from sequence"]],[[1,"disabled",0],[2,"_mat-animation-noopable",null]],[[null,"click"],[null,"longpress"],[null,"keydown"],[null,"touchend"]],(function(l,n,t){var e=!0,i=l.component;return"longpress"===n&&(e=!1!==o.Fb(l,44).show()&&e),"keydown"===n&&(e=!1!==o.Fb(l,44)._handleKeydown(t)&&e),"touchend"===n&&(e=!1!==o.Fb(l,44)._handleTouchend()&&e),"click"===n&&(e=!1!==i.openDialog("O")&&e),e}),I.d,I.b)),o.sb(43,180224,null,0,j.b,[o.k,T.h,[2,L.a]],null,null),o.sb(44,212992,null,0,U.d,[O.c,o.k,Y.b,o.P,o.z,A.a,T.c,T.h,U.b,[2,z.b],[2,U.a],[2,E.f]],{message:[0,"message"]},null),(l()(),o.tb(45,0,null,0,1,"mat-icon",[["class","mat-icon notranslate"],["role","img"],["svgIcon","edit"]],[[2,"mat-icon-inline",null],[2,"mat-icon-no-color",null]],null,null,q.b,q.a)),o.sb(46,9158656,null,0,H.b,[o.k,H.d,[8,null],[2,H.a],[2,o.m]],{svgIcon:[0,"svgIcon"]},null),(l()(),o.Nb(47,null,[" "," "])),(l()(),o.tb(48,0,null,null,1,"app-audit-info",[],null,null,null,h.c,h.b)),o.sb(49,114688,null,0,V.a,[],{source:[0,"source"]},null)],(function(l,n){var t=n.component;l(n,5,0,"Glycosylation Type","Protein Glycosylation Type",t.glycosylation.glycosylationType),l(n,20,0,"Select sites from sequence"),l(n,22,0,"edit"),l(n,32,0,"Select sites from sequence"),l(n,34,0,"edit"),l(n,44,0,"Select sites from sequence"),l(n,46,0,"edit"),l(n,49,0,t.privateLink)}),(function(l,n){var t=n.component;l(n,8,0,o.Fb(n,9).disabled||null,"NoopAnimations"===o.Fb(n,9)._animationMode),l(n,18,0,o.Fb(n,19).disabled||null,"NoopAnimations"===o.Fb(n,19)._animationMode),l(n,21,0,o.Fb(n,22).inline,"primary"!==o.Fb(n,22).color&&"accent"!==o.Fb(n,22).color&&"warn"!==o.Fb(n,22).color),l(n,23,0,t.siteDisplay(t.glycosylation.CGlycosylationSites)),l(n,30,0,o.Fb(n,31).disabled||null,"NoopAnimations"===o.Fb(n,31)._animationMode),l(n,33,0,o.Fb(n,34).inline,"primary"!==o.Fb(n,34).color&&"accent"!==o.Fb(n,34).color&&"warn"!==o.Fb(n,34).color),l(n,35,0,t.siteDisplay(t.glycosylation.NGlycosylationSites)),l(n,42,0,o.Fb(n,43).disabled||null,"NoopAnimations"===o.Fb(n,43)._animationMode),l(n,45,0,o.Fb(n,46).inline,"primary"!==o.Fb(n,46).color&&"accent"!==o.Fb(n,46).color&&"warn"!==o.Fb(n,46).color),l(n,47,0,t.siteDisplay(t.glycosylation.OGlycosylationSites))}))}function nl(l){return o.Pb(0,[(l()(),o.jb(16777216,null,null,1,null,ll)),o.sb(1,16384,null,0,K.n,[o.P,o.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,1,0,n.component.glycosylation)}),null)}function tl(l){return o.Pb(0,[(l()(),o.tb(0,0,null,null,1,"app-substance-form-glycosylation",[],null,null,null,nl,$)),o.sb(1,4440064,null,0,W,[X.a,R.a,Q.a,G.a,P.e,O.e],null,null)],(function(l,n){l(n,1,0)}),null)}var ol=o.pb("app-substance-form-glycosylation",W,tl,{},{menuLabelUpdate:"menuLabelUpdate",hiddenStateUpdate:"hiddenStateUpdate",canAddItemUpdate:"canAddItemUpdate",componentDestroyed:"componentDestroyed"},[]),el=t("gIcY"),il=t("M2Lx"),al=t("mVsa"),ul=t("Wf4p"),sl=t("uGex"),cl=t("4tE/"),bl=t("4epT"),rl=t("EtvR"),dl=t("seP3"),yl=t("4c35"),pl=t("de3e"),ml=t("La40"),gl=t("LC5p"),fl=t("/VYK"),Dl=t("b716"),hl=t("/dO6"),vl=t("NYLF"),wl=t("y4qS"),kl=t("BHnd"),Cl=t("YhbO"),Sl=t("jlZm"),xl=t("6Wmm"),Fl=t("9It4"),Gl=t("PnCX"),Pl=t("IyAz"),Ml=t("ZYCi"),Ol=t("5uHe"),_l=t("vfGX"),Nl=t("0/Q6"),Il=t("jS4w"),jl=t("u7R8"),Tl=t("NnTW"),Ll=t("Z+uX"),Ul=t("Blfk"),Yl=t("7fs6"),Al=t("YSh2"),zl=t("6jyQ");t.d(n,"SubstanceFormGlycosylationModuleNgFactory",(function(){return El}));var El=o.qb(e,[],(function(l){return o.Cb([o.Db(512,o.j,o.bb,[[8,[i.a,a.a,u.a,s.a,c.a,b.a,r.a,d.a,y.b,p.a,m.a,g.a,f.a,D.a,h.a,v.a,w.a,k.a,C.a,S.a,ol]],[3,o.j],o.x]),o.Db(4608,K.p,K.o,[o.u,[2,K.G]]),o.Db(4608,el.e,el.e,[]),o.Db(4608,el.w,el.w,[]),o.Db(4608,il.c,il.c,[]),o.Db(4608,O.c,O.c,[O.i,O.e,o.j,O.h,O.f,o.r,o.z,K.d,z.b,[2,K.j]]),o.Db(5120,O.j,O.k,[O.c]),o.Db(5120,al.c,al.j,[O.c]),o.Db(5120,U.b,U.c,[O.c]),o.Db(4608,E.e,ul.e,[[2,ul.i],[2,ul.n]]),o.Db(5120,sl.a,sl.b,[O.c]),o.Db(4608,ul.d,ul.d,[]),o.Db(5120,cl.b,cl.c,[O.c]),o.Db(5120,P.c,P.d,[O.c]),o.Db(135680,P.e,P.e,[O.c,o.r,[2,K.j],[2,P.b],P.c,[3,P.e],O.e]),o.Db(5120,bl.c,bl.a,[[3,bl.c]]),o.Db(1073742336,K.c,K.c,[]),o.Db(1073742336,rl.a,rl.a,[]),o.Db(1073742336,el.v,el.v,[]),o.Db(1073742336,el.s,el.s,[]),o.Db(1073742336,el.k,el.k,[]),o.Db(1073742336,il.d,il.d,[]),o.Db(1073742336,dl.e,dl.e,[]),o.Db(1073742336,z.a,z.a,[]),o.Db(1073742336,ul.n,ul.n,[[2,ul.f],[2,E.f]]),o.Db(1073742336,A.b,A.b,[]),o.Db(1073742336,ul.x,ul.x,[]),o.Db(1073742336,yl.g,yl.g,[]),o.Db(1073742336,Y.c,Y.c,[]),o.Db(1073742336,O.g,O.g,[]),o.Db(1073742336,al.i,al.i,[]),o.Db(1073742336,al.f,al.f,[]),o.Db(1073742336,pl.d,pl.d,[]),o.Db(1073742336,pl.c,pl.c,[]),o.Db(1073742336,j.c,j.c,[]),o.Db(1073742336,H.c,H.c,[]),o.Db(1073742336,T.a,T.a,[]),o.Db(1073742336,U.e,U.e,[]),o.Db(1073742336,ml.l,ml.l,[]),o.Db(1073742336,gl.b,gl.b,[]),o.Db(1073742336,ul.v,ul.v,[]),o.Db(1073742336,ul.s,ul.s,[]),o.Db(1073742336,sl.d,sl.d,[]),o.Db(1073742336,fl.c,fl.c,[]),o.Db(1073742336,Dl.c,Dl.c,[]),o.Db(1073742336,hl.f,hl.f,[]),o.Db(1073742336,cl.e,cl.e,[]),o.Db(1073742336,vl.a,vl.a,[]),o.Db(1073742336,P.k,P.k,[]),o.Db(1073742336,wl.p,wl.p,[]),o.Db(1073742336,kl.m,kl.m,[]),o.Db(1073742336,Cl.c,Cl.c,[]),o.Db(1073742336,Sl.d,Sl.d,[]),o.Db(1073742336,xl.b,xl.b,[]),o.Db(1073742336,Fl.d,Fl.d,[]),o.Db(1073742336,Gl.a,Gl.a,[]),o.Db(1073742336,Pl.a,Pl.a,[]),o.Db(1073742336,Ml.p,Ml.p,[[2,Ml.u],[2,Ml.m]]),o.Db(1073742336,Ol.a,Ol.a,[]),o.Db(1073742336,_l.a,_l.a,[]),o.Db(1073742336,ul.o,ul.o,[]),o.Db(1073742336,Nl.d,Nl.d,[]),o.Db(1073742336,Il.b,Il.b,[]),o.Db(1073742336,jl.e,jl.e,[]),o.Db(1073742336,Tl.b,Tl.b,[]),o.Db(1073742336,Ll.c,Ll.c,[]),o.Db(1073742336,Ul.c,Ul.c,[]),o.Db(1073742336,Yl.a,Yl.a,[]),o.Db(1073742336,bl.d,bl.d,[]),o.Db(1073742336,e,e,[]),o.Db(256,hl.a,{separatorKeyCodes:[Al.g]},[]),o.Db(1024,Ml.j,(function(){return[[]]}),[]),o.Db(256,zl.a,W,[])])}))},"n67+":function(l,n,t){"use strict";t.d(n,"a",(function(){return e}));var o=t("CcnG"),e=function(){return function(){this.menuLabelUpdate=new o.n,this.hiddenStateUpdate=new o.n,this.canAddItemUpdate=new o.n,this.componentDestroyed=new o.n}}()}}]);
//# sourceMappingURL=11.2bd661796c80e20285a0.js.map