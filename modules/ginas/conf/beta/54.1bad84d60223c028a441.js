(window.webpackJsonp=window.webpackJsonp||[]).push([[54],{TRrv:function(l,n,u){"use strict";u.r(n);var e=u("CcnG"),t=function(){return function(){}}(),i=u("t68o"),a=u("o3x0"),s=u("nahG"),c=u("Pp4P"),b=u("CQqH"),o=u("Ip0R"),r=u("mrSG"),d=u("EfhQ"),f=u("K9Ia"),p=function(l){function n(n,u,e){var t=l.call(this)||this;return t.dialog=n,t.gaService=u,t.overlayContainerService=e,t.definition={},t.substanceUpdated=new f.a,t.count=0,t}return r.a(n,l),n.prototype.ngOnInit=function(){var l=this;this.substanceUpdated.subscribe((function(n){l.substance=n,null!=l.substance&&null!=l.substance.specifiedSubstanceG3&&l.substance.specifiedSubstanceG3.definition&&(l.definition=l.substance.specifiedSubstanceG3.definition,l.count=1),l.countUpdate.emit(l.count)})),this.overlayContainer=this.overlayContainerService.getContainerElement()},n.prototype.openModal=function(l){var n=this;this.gaService.sendEvent(this.analyticsEventCategory,"button","references view");var u=this.dialog.open(l,{minWidth:"40%",maxWidth:"90%"});this.overlayContainer.style.zIndex="1002",u.afterClosed().subscribe((function(l){n.overlayContainer.style.zIndex=null}))},n}(d.a),v=u("HECD"),g=u("eDkP"),m=e.rb({encapsulation:0,styles:[[".name[_ngcontent-%COMP%]{min-width:150px}"]],data:{}});function D(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"h2",[["class","mat-dialog-title"],["mat-dialog-title",""]],[[8,"id",0]],null,null,null,null)),e.sb(1,81920,null,0,a.m,[[2,a.l],e.k,a.e],null,null),(l()(),e.Nb(-1,null,["References"])),(l()(),e.tb(3,0,null,null,3,"div",[["class","mat-dialog-content"],["mat-dialog-content",""]],null,null,null,null,null)),e.sb(4,16384,null,0,a.j,[],null,null),(l()(),e.tb(5,0,null,null,1,"app-references-manager",[],null,null,null,s.b,s.a)),e.sb(6,114688,null,0,c.a,[b.a],{substance:[0,"substance"],references:[1,"references"]},null),(l()(),e.tb(7,0,null,null,5,"div",[["class","mat-dialog-actions"],["mat-dialog-actions",""]],null,null,null,null,null)),e.sb(8,16384,null,0,a.f,[],null,null),(l()(),e.tb(9,0,null,null,0,"span",[["class","middle-fill"]],null,null,null,null,null)),(l()(),e.tb(10,0,null,null,2,"button",[["class","mat-raised-button mat-primary"],["mat-dialog-close",""]],[[1,"aria-label",0],[1,"type",0]],[[null,"click"]],(function(l,n,u){var t=!0;return"click"===n&&(t=!1!==e.Fb(l,11).dialogRef.close(e.Fb(l,11).dialogResult)&&t),t}),null,null)),e.sb(11,606208,null,0,a.g,[[2,a.l],e.k,a.e],{dialogResult:[0,"dialogResult"]},null),(l()(),e.Nb(-1,null,["Close"]))],(function(l,n){var u=n.component;l(n,1,0),l(n,6,0,u.substance,u.substance.specifiedSubstanceG3.definition.references),l(n,11,0,"")}),(function(l,n){l(n,0,0,e.Fb(n,1).id),l(n,10,0,e.Fb(n,11).ariaLabel||null,e.Fb(n,11).type)}))}function h(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,12,"div",[],null,null,null,null,null)),(l()(),e.tb(1,0,null,null,4,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),e.tb(2,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),e.Nb(-1,null,["Definition"])),(l()(),e.tb(4,0,null,null,1,"div",[["class","value"]],null,null,null,null,null)),(l()(),e.Nb(5,null,["",""])),(l()(),e.tb(6,0,null,null,6,"div",[["class","name-value"]],null,null,null,null,null)),(l()(),e.tb(7,0,null,null,1,"div",[["class","name"]],null,null,null,null,null)),(l()(),e.Nb(-1,null,["References"])),(l()(),e.tb(9,0,null,null,3,"div",[["class","value"]],null,null,null,null,null)),(l()(),e.tb(10,0,null,null,1,"button",[["class","mat-raised-button mat-primary dialog-close"]],[[8,"disabled",0]],[[null,"click"]],(function(l,n,u){var t=!0;return"click"===n&&(t=!1!==l.component.openModal(e.Fb(l,12))&&t),t}),null,null)),(l()(),e.Nb(-1,null,["View"])),(l()(),e.jb(0,[["refTemplate",2]],null,0,null,D))],null,(function(l,n){var u=n.component;l(n,5,0,u.substance.specifiedSubstanceG3.definition.definition),l(n,10,0,u.substance.specifiedSubstanceG3.definition.references.length<=0)}))}function y(l){return e.Pb(0,[(l()(),e.jb(16777216,null,null,1,null,h)),e.sb(1,16384,null,0,o.n,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){l(n,1,0,n.component.substance.specifiedSubstanceG3.definition)}),null)}function S(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,1,"app-substance-ssg-definition",[],null,null,null,y,m)),e.sb(1,114688,null,0,p,[a.e,v.a,g.e],null,null)],(function(l,n){l(n,1,0)}),null)}var C=e.pb("app-substance-ssg-definition",p,S,{},{countUpdate:"countUpdate"},[]),j=u("Fzqc"),k=u("EtvR"),G=u("y4qS"),w=u("Wf4p"),P=u("ZYjt"),R=u("BHnd"),F=u("3fDy"),I=u("4c35"),N=u("dWZg"),x=u("qAlS"),E=u("ZYCi"),q=u("6jyQ");u.d(n,"SubstanceSsgDefinitionModuleNgFactory",(function(){return M}));var M=e.qb(t,[],(function(l){return e.Cb([e.Db(512,e.j,e.bb,[[8,[i.a,C]],[3,e.j],e.x]),e.Db(4608,o.p,o.o,[e.u,[2,o.G]]),e.Db(4608,g.c,g.c,[g.i,g.e,e.j,g.h,g.f,e.r,e.z,o.d,j.b,[2,o.j]]),e.Db(5120,g.j,g.k,[g.c]),e.Db(5120,a.c,a.d,[g.c]),e.Db(135680,a.e,a.e,[g.c,e.r,[2,o.j],[2,a.b],a.c,[3,a.e],g.e]),e.Db(1073742336,o.c,o.c,[]),e.Db(1073742336,k.a,k.a,[]),e.Db(1073742336,G.p,G.p,[]),e.Db(1073742336,j.a,j.a,[]),e.Db(1073742336,w.n,w.n,[[2,w.f],[2,P.f]]),e.Db(1073742336,R.m,R.m,[]),e.Db(1073742336,F.a,F.a,[]),e.Db(1073742336,I.g,I.g,[]),e.Db(1073742336,N.b,N.b,[]),e.Db(1073742336,x.c,x.c,[]),e.Db(1073742336,g.g,g.g,[]),e.Db(1073742336,a.k,a.k,[]),e.Db(1073742336,t,t,[]),e.Db(1024,E.j,(function(){return[[]]}),[]),e.Db(256,q.a,p,[])])}))}}]);
//# sourceMappingURL=54.1bad84d60223c028a441.js.map