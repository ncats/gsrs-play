(window.webpackJsonp=window.webpackJsonp||[]).push([[52],{ME2J:function(l,n,u){"use strict";u.r(n);var e=u("CcnG"),b=function(){return function(){}}(),t=u("NcP4"),c=u("t68o"),s=u("pMnS"),a=u("BHnd"),r=u("y4qS"),o=u("Ip0R"),i=u("pIm3"),d=u("Fzqc"),m=u("dWZg"),D=u("mrSG"),f=u("EfhQ"),h=u("K9Ia"),p=function(l){function n(n,u){var e=l.call(this)||this;return e.substanceService=n,e.router=u,e.displayedColumns=["view","version","versionComments","editor","changeDate"],e.substanceUpdated=new h.a,e}return D.b(n,l),n.prototype.ngOnInit=function(){var l=this;this.substanceService.getEdits(this.substance.uuid).subscribe((function(n){l.versions=n}))},n.prototype.ngAfterViewInit=function(){var l=this;this.substanceUpdated.subscribe((function(n){l.substance=n}))},n.prototype.switchVersion=function(l){this.router.navigate(["/substances/"+this.substance.uuid+"/v/"+l])},n}(f.a),v=u("CQqH"),M=u("ZYCi"),g=e.rb({encapsulation:0,styles:[[".centered[_ngcontent-%COMP%]{text-align:center}"]],data:{}});function K(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.e,[r.d,e.k],null,null),(l()(),e.Nb(-1,null,[" Displayed "]))],null,null)}function j(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"span",[],null,null,null,null,null)),(l()(),e.tb(1,0,null,null,1,"button",[["class","mat-raised-button mat-primary"]],null,[[null,"click"]],(function(l,n,u){var e=!0;return"click"===n&&(e=!1!==l.component.switchVersion(l.parent.context.$implicit.version)&&e),e}),null,null)),(l()(),e.Nb(-1,null,["View"]))],null,null)}function C(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,1,"span",[["class","centered"]],null,null,null,null,null)),(l()(),e.Nb(-1,null,[" Viewing "]))],null,null)}function L(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,5,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.a,[r.d,e.k],null,null),(l()(),e.jb(16777216,null,null,1,null,j)),e.sb(3,16384,null,0,o.m,[e.P,e.M],{ngIf:[0,"ngIf"]},null),(l()(),e.jb(16777216,null,null,1,null,C)),e.sb(5,16384,null,0,o.m,[e.P,e.M],{ngIf:[0,"ngIf"]},null)],(function(l,n){var u=n.component;l(n,3,0,u.substance.version!=n.context.$implicit.version),l(n,5,0,u.substance.version==n.context.$implicit.version)}),null)}function k(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.e,[r.d,e.k],null,null),(l()(),e.Nb(-1,null,[" Version "]))],null,null)}function w(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.a,[r.d,e.k],null,null),(l()(),e.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.version)}))}function _(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.e,[r.d,e.k],null,null),(l()(),e.Nb(-1,null,[" Version Comments "]))],null,null)}function P(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.a,[r.d,e.k],null,null),(l()(),e.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.comments)}))}function E(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.e,[r.d,e.k],null,null),(l()(),e.Nb(-1,null,[" Editor"]))],null,null)}function N(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.a,[r.d,e.k],null,null),(l()(),e.Nb(2,null,[" "," "]))],null,(function(l,n){l(n,2,0,n.context.$implicit.editor)}))}function y(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"th",[["class","mat-header-cell"],["mat-header-cell",""],["role","columnheader"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.e,[r.d,e.k],null,null),(l()(),e.Nb(-1,null,[" Change Date "]))],null,null)}function O(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,3,"td",[["class","mat-cell"],["mat-cell",""],["role","gridcell"]],null,null,null,null,null)),e.sb(1,16384,null,0,a.a,[r.d,e.k],null,null),(l()(),e.Nb(2,null,[" "," "])),e.Jb(3,2)],null,(function(l,n){var u=e.Ob(n,2,0,l(n,3,0,e.Fb(n.parent,0),n.context.$implicit.created,"short"));l(n,2,0,u)}))}function R(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"tr",[["class","mat-header-row"],["mat-header-row",""],["role","row"]],null,null,null,i.d,i.a)),e.Kb(6144,null,r.k,null,[a.g]),e.sb(2,49152,null,0,a.g,[],null,null)],null,null)}function S(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,2,"tr",[["class","mat-row"],["mat-row",""],["role","row"]],null,null,null,i.e,i.b)),e.Kb(6144,null,r.m,null,[a.i]),e.sb(2,49152,null,0,a.i,[],null,null)],null,null)}function x(l){return e.Pb(0,[e.Hb(0,o.e,[e.u]),(l()(),e.tb(1,0,null,null,78,"div",[["class","responsive"]],null,null,null,null,null)),(l()(),e.tb(2,0,null,null,77,"table",[["class","mat-table"],["mat-table",""]],null,null,null,i.f,i.c)),e.Kb(6144,null,r.o,null,[a.k]),e.sb(4,2342912,null,4,a.k,[e.s,e.h,e.k,[8,null],[2,d.b],o.d,m.a],{dataSource:[0,"dataSource"]},null),e.Lb(603979776,1,{_contentColumnDefs:1}),e.Lb(603979776,2,{_contentRowDefs:1}),e.Lb(603979776,3,{_contentHeaderRowDefs:1}),e.Lb(603979776,4,{_contentFooterRowDefs:1}),(l()(),e.tb(9,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[a.c]),e.sb(11,16384,null,3,a.c,[],{name:[0,"name"]},null),e.Lb(603979776,5,{cell:0}),e.Lb(603979776,6,{headerCell:0}),e.Lb(603979776,7,{footerCell:0}),e.Kb(2048,[[1,4]],r.d,null,[a.c]),(l()(),e.jb(0,null,null,2,null,K)),e.sb(17,16384,null,0,a.f,[e.M],null,null),e.Kb(2048,[[6,4]],r.j,null,[a.f]),(l()(),e.jb(0,null,null,2,null,L)),e.sb(20,16384,null,0,a.b,[e.M],null,null),e.Kb(2048,[[5,4]],r.b,null,[a.b]),(l()(),e.tb(22,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[a.c]),e.sb(24,16384,null,3,a.c,[],{name:[0,"name"]},null),e.Lb(603979776,8,{cell:0}),e.Lb(603979776,9,{headerCell:0}),e.Lb(603979776,10,{footerCell:0}),e.Kb(2048,[[1,4]],r.d,null,[a.c]),(l()(),e.jb(0,null,null,2,null,k)),e.sb(30,16384,null,0,a.f,[e.M],null,null),e.Kb(2048,[[9,4]],r.j,null,[a.f]),(l()(),e.jb(0,null,null,2,null,w)),e.sb(33,16384,null,0,a.b,[e.M],null,null),e.Kb(2048,[[8,4]],r.b,null,[a.b]),(l()(),e.tb(35,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[a.c]),e.sb(37,16384,null,3,a.c,[],{name:[0,"name"]},null),e.Lb(603979776,11,{cell:0}),e.Lb(603979776,12,{headerCell:0}),e.Lb(603979776,13,{footerCell:0}),e.Kb(2048,[[1,4]],r.d,null,[a.c]),(l()(),e.jb(0,null,null,2,null,_)),e.sb(43,16384,null,0,a.f,[e.M],null,null),e.Kb(2048,[[12,4]],r.j,null,[a.f]),(l()(),e.jb(0,null,null,2,null,P)),e.sb(46,16384,null,0,a.b,[e.M],null,null),e.Kb(2048,[[11,4]],r.b,null,[a.b]),(l()(),e.tb(48,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[a.c]),e.sb(50,16384,null,3,a.c,[],{name:[0,"name"]},null),e.Lb(603979776,14,{cell:0}),e.Lb(603979776,15,{headerCell:0}),e.Lb(603979776,16,{footerCell:0}),e.Kb(2048,[[1,4]],r.d,null,[a.c]),(l()(),e.jb(0,null,null,2,null,E)),e.sb(56,16384,null,0,a.f,[e.M],null,null),e.Kb(2048,[[15,4]],r.j,null,[a.f]),(l()(),e.jb(0,null,null,2,null,N)),e.sb(59,16384,null,0,a.b,[e.M],null,null),e.Kb(2048,[[14,4]],r.b,null,[a.b]),(l()(),e.tb(61,0,null,null,12,null,null,null,null,null,null,null)),e.Kb(6144,null,"MAT_SORT_HEADER_COLUMN_DEF",null,[a.c]),e.sb(63,16384,null,3,a.c,[],{name:[0,"name"]},null),e.Lb(603979776,17,{cell:0}),e.Lb(603979776,18,{headerCell:0}),e.Lb(603979776,19,{footerCell:0}),e.Kb(2048,[[1,4]],r.d,null,[a.c]),(l()(),e.jb(0,null,null,2,null,y)),e.sb(69,16384,null,0,a.f,[e.M],null,null),e.Kb(2048,[[18,4]],r.j,null,[a.f]),(l()(),e.jb(0,null,null,2,null,O)),e.sb(72,16384,null,0,a.b,[e.M],null,null),e.Kb(2048,[[17,4]],r.b,null,[a.b]),(l()(),e.jb(0,null,null,2,null,R)),e.sb(75,540672,null,0,a.h,[e.M,e.s],{columns:[0,"columns"]},null),e.Kb(2048,[[3,4]],r.l,null,[a.h]),(l()(),e.jb(0,null,null,2,null,S)),e.sb(78,540672,null,0,a.j,[e.M,e.s],{columns:[0,"columns"]},null),e.Kb(2048,[[2,4]],r.n,null,[a.j])],(function(l,n){var u=n.component;l(n,4,0,u.versions),l(n,11,0,"view"),l(n,24,0,"version"),l(n,37,0,"versionComments"),l(n,50,0,"editor"),l(n,63,0,"changeDate"),l(n,75,0,u.displayedColumns),l(n,78,0,u.displayedColumns)}),null)}function A(l){return e.Pb(0,[(l()(),e.tb(0,0,null,null,1,"app-substance-history",[],null,null,null,x,g)),e.sb(1,4308992,null,0,p,[v.a,M.m],null,null)],(function(l,n){l(n,1,0)}),null)}var H=e.pb("app-substance-history",p,A,{},{countUpdate:"countUpdate"},[]),T=u("eDkP"),I=u("M2Lx"),U=u("uGex"),F=u("v9Dh"),V=u("ZYjt"),$=u("Wf4p"),q=u("4epT"),J=u("gIcY"),Y=u("o3x0"),z=u("OkvK"),G=u("lLAP"),Q=u("OBdK"),Z=u("EtvR"),B=u("UodH"),W=u("4c35"),X=u("qAlS"),ll=u("seP3"),nl=u("/VYK"),ul=u("b716"),el=u("3fDy"),bl=u("SMsm"),tl=u("J12g"),cl=u("6jyQ");u.d(n,"SubstanceHistoryModuleNgFactory",(function(){return sl}));var sl=e.qb(b,[],(function(l){return e.Cb([e.Db(512,e.j,e.bb,[[8,[t.a,c.a,s.a,H]],[3,e.j],e.x]),e.Db(4608,o.o,o.n,[e.u,[2,o.E]]),e.Db(4608,T.c,T.c,[T.i,T.e,e.j,T.h,T.f,e.r,e.z,o.d,d.b,[2,o.i]]),e.Db(5120,T.j,T.k,[T.c]),e.Db(4608,I.c,I.c,[]),e.Db(5120,U.a,U.b,[T.c]),e.Db(5120,F.b,F.c,[T.c]),e.Db(4608,V.e,$.e,[[2,$.i],[2,$.n]]),e.Db(5120,q.c,q.a,[[3,q.c]]),e.Db(4608,$.d,$.d,[]),e.Db(4608,J.e,J.e,[]),e.Db(4608,J.w,J.w,[]),e.Db(5120,Y.c,Y.d,[T.c]),e.Db(135680,Y.e,Y.e,[T.c,e.r,[2,o.i],[2,Y.b],Y.c,[3,Y.e],T.e]),e.Db(5120,z.d,z.a,[[3,z.d]]),e.Db(135680,G.f,G.f,[e.z,m.a]),e.Db(4608,Q.f,Q.f,[e.M]),e.Db(1073742336,o.c,o.c,[]),e.Db(1073742336,Z.a,Z.a,[]),e.Db(1073742336,r.p,r.p,[]),e.Db(1073742336,d.a,d.a,[]),e.Db(1073742336,$.n,$.n,[[2,$.f],[2,V.f]]),e.Db(1073742336,a.m,a.m,[]),e.Db(1073742336,m.b,m.b,[]),e.Db(1073742336,$.x,$.x,[]),e.Db(1073742336,B.c,B.c,[]),e.Db(1073742336,W.g,W.g,[]),e.Db(1073742336,X.c,X.c,[]),e.Db(1073742336,T.g,T.g,[]),e.Db(1073742336,$.v,$.v,[]),e.Db(1073742336,$.s,$.s,[]),e.Db(1073742336,I.d,I.d,[]),e.Db(1073742336,ll.e,ll.e,[]),e.Db(1073742336,U.d,U.d,[]),e.Db(1073742336,G.a,G.a,[]),e.Db(1073742336,F.e,F.e,[]),e.Db(1073742336,q.d,q.d,[]),e.Db(1073742336,nl.c,nl.c,[]),e.Db(1073742336,ul.b,ul.b,[]),e.Db(1073742336,J.v,J.v,[]),e.Db(1073742336,J.s,J.s,[]),e.Db(1073742336,J.k,J.k,[]),e.Db(1073742336,el.a,el.a,[]),e.Db(1073742336,bl.c,bl.c,[]),e.Db(1073742336,Y.k,Y.k,[]),e.Db(1073742336,z.e,z.e,[]),e.Db(1073742336,Q.d,Q.d,[]),e.Db(1073742336,tl.c,tl.c,[]),e.Db(1073742336,M.p,M.p,[[2,M.u],[2,M.m]]),e.Db(1073742336,b,b,[]),e.Db(1024,M.j,(function(){return[[]]}),[]),e.Db(256,cl.a,p,[])])}))}}]);
//# sourceMappingURL=52.7e662b73eb53d94e6e6f.js.map